package com.cheatsheet.aop.engine;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.annotation.Order;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.SimpleEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

// ============================================================================
// 1. DOMAIN MODELS & CUSTOM AOP ANNOTATIONS
// ============================================================================
record PaymentOrderRequest(String orderId, String accountId, BigDecimal amount, String idempotencyKey) {}
record PaymentOrderResponse(String transactionId, String orderId, String status, Instant settledAt) {}

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@interface IdempotentOperation {
    /** SpEL expression to extract the unique idempotency token from method arguments. */
    String keyExpression() default "#request.idempotencyKey()";
    long ttlSeconds() default 120;
}

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@interface AuditedTransaction {
    String action();
    boolean maskSensitiveArgs() default true;
}

// Custom exception thrown when duplicate in-flight or completed operations occur
class DuplicateOperationException extends RuntimeException {
    public DuplicateOperationException(String message) { super(message); }
}

// ============================================================================
// 2. ASPECT 1: DISTRIBUTED IDEMPOTENCY ASPECT (@Order(100) - Outermost Guard)
// ============================================================================
@Aspect
@Order(100)
@Component
class DistributedIdempotencyAspect {
    private static final Logger log = LoggerFactory.getLogger(DistributedIdempotencyAspect.class);
    private final ExpressionParser parser = new SpelExpressionParser();

    // High-performance thread-safe state store (emulating Redis SETNX with TTL)
    private final Map<String, IdempotencyRecord> lockStore = new ConcurrentHashMap<>();

    private record IdempotencyRecord(Object response, Instant expiresAt, boolean inFlight) {}

    @Around("@annotation(idempotentOp)")
    public Object enforceIdempotency(ProceedingJoinPoint joinPoint, IdempotentOperation idempotentOp) throws Throwable {
        String key = resolveSpelKey(joinPoint, idempotentOp.keyExpression());
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Idempotency key resolved to null or empty string");
        }

        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(idempotentOp.ttlSeconds());

        // Atomic Check-and-Set: Emulates Redis SET key in_flight NX EX ttl
        IdempotencyRecord existing = lockStore.putIfAbsent(key, new IdempotencyRecord(null, expiresAt, true));
        if (existing != null) {
            if (existing.expiresAt().isAfter(now)) {
                if (existing.inFlight()) {
                    log.warn("Concurrent duplicate request detected for idempotency key: {}", key);
                    throw new DuplicateOperationException("Operation already in-flight for key: " + key);
                }
                log.info("Returning cached response for completed idempotent operation: {}", key);
                return existing.response();
            } else {
                // Key expired, evict and replace
                lockStore.put(key, new IdempotencyRecord(null, expiresAt, true));
            }
        }

        try {
            Object result = joinPoint.proceed();
            // Cache the successful outcome
            lockStore.put(key, new IdempotencyRecord(result, expiresAt, false));
            return result;
        } catch (Throwable ex) {
            // On business failure, release the lock so the client may safely retry
            lockStore.remove(key);
            log.error("Idempotent operation failed for key: {}. Lock released.", key, ex);
            throw ex;
        }
    }

    private String resolveSpelKey(ProceedingJoinPoint joinPoint, String expressionStr) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] paramNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();

        // Safe evaluation context preventing SpEL injection attacks
        Map<String, Object> variables = new HashMap<>();
        for (int i = 0; i < paramNames.length; i++) {
            variables.put(paramNames[i], args[i]);
        }

        var context = SimpleEvaluationContext.forReadOnlyDataBinding().build();
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            context.setVariable(entry.getKey(), entry.getValue());
        }

        Object evaluated = parser.parseExpression(expressionStr).getValue(context);
        return evaluated != null ? evaluated.toString() : null;
    }
}

// ============================================================================
// 3. ASPECT 2: ENTERPRISE AUDIT & TELEMETRY ASPECT (@Order(200) - Inner Guard)
// ============================================================================
@Aspect
@Order(200)
@Component
class EnterpriseAuditAspect {
    private static final Logger log = LoggerFactory.getLogger(EnterpriseAuditAspect.class);

    @Around("@annotation(audited)")
    public Object auditMethodInvocation(ProceedingJoinPoint joinPoint, AuditedTransaction audited) throws Throwable {
        String correlationId = UUID.randomUUID().toString();
        MDC.put("correlationId", correlationId);
        MDC.put("action", audited.action());

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getDeclaringType().getSimpleName() + "." + signature.getName();
        long startNano = System.nanoTime();

        log.info("[AUDIT-START] Action: {} | Method: {} | CorrelationId: {}",
                audited.action(), methodName, correlationId);

        try {
            Object result = joinPoint.proceed();
            long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNano);

            log.info("[AUDIT-SUCCESS] Action: {} | Method: {} | Duration: {}ms | CorrelationId: {}",
                    audited.action(), methodName, durationMs, correlationId);
            return result;
        } catch (Throwable ex) {
            long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNano);
            log.error("[AUDIT-FAILURE] Action: {} | Method: {} | Duration: {}ms | Error: {} | CorrelationId: {}",
                    audited.action(), methodName, durationMs, ex.getMessage(), correlationId, ex);
            throw ex;
        } finally {
            MDC.remove("correlationId");
            MDC.remove("action");
        }
    }
}

// ============================================================================
// 4. ENTERPRISE BUSINESS SERVICE: Self-Invocation Resolution & Contract
// ============================================================================
interface PaymentSettlementOperations {
    PaymentOrderResponse processSettlement(PaymentOrderRequest request);
    PaymentOrderResponse internalSubAccountDebit(PaymentOrderRequest request);
}

@Service
class PaymentSettlementService implements PaymentSettlementOperations {
    private static final Logger log = LoggerFactory.getLogger(PaymentSettlementService.class);

    // Technique 1: Self-injection via ObjectProvider to cleanly avoid circular dependencies
    private final ObjectProvider<PaymentSettlementOperations> selfProvider;

    public PaymentSettlementService(ObjectProvider<PaymentSettlementOperations> selfProvider) {
        this.selfProvider = selfProvider;
    }

    @Override
    @IdempotentOperation(keyExpression = "#request.idempotencyKey()", ttlSeconds = 60)
    @AuditedTransaction(action = "PROCESS_PAYMENT_SETTLEMENT")
    public PaymentOrderResponse processSettlement(PaymentOrderRequest request) {
        log.info("Executing core settlement logic for order: {}", request.orderId());

        // ❌ ANTI-PATTERN: Calling this.internalSubAccountDebit(request) completely BYPASSES proxies!
        // ✅ REFACTORED: Invoke via selfProvider proxy or AopContext.currentProxy()
        PaymentSettlementOperations proxy = selfProvider.getIfAvailable();
        if (proxy != null) {
            return proxy.internalSubAccountDebit(request);
        } else {
            // Fallback to AopContext if selfProvider unavailable
            PaymentSettlementOperations aopProxy = (PaymentSettlementOperations) AopContext.currentProxy();
            return aopProxy.internalSubAccountDebit(request);
        }
    }

    @Override
    @AuditedTransaction(action = "SUB_ACCOUNT_DEBIT")
    public PaymentOrderResponse internalSubAccountDebit(PaymentOrderRequest request) {
        log.info("Executing audited sub-account debit for account: {}, amount: ${}",
                request.accountId(), request.amount());

        return new PaymentOrderResponse(
                "TXN-" + UUID.randomUUID().toString().substring(0, 8),
                request.orderId(),
                "COMPLETED",
                Instant.now()
        );
    }
}

// ============================================================================
// 5. SPRING BOOT APPLICATION BOOTSTRAPPER (exposeProxy = true Enabled)
// ============================================================================
@SpringBootApplication
@EnableAspectJAutoProxy(exposeProxy = true, proxyTargetClass = true)
public class AuditIdempotencyApplication {
    private static final Logger log = LoggerFactory.getLogger(AuditIdempotencyApplication.class);

    public static void main(String[] args) {
        var context = SpringApplication.run(AuditIdempotencyApplication.class, args);
        PaymentSettlementOperations service = context.getBean(PaymentSettlementOperations.class);

        var request = new PaymentOrderRequest("ORD-1001", "ACC-789", BigDecimal.valueOf(250.00), "IDEM-KEY-999");
        log.info("First Call: {}", service.processSettlement(request));
        log.info("Duplicate Retry Call (Returns Cached Response): {}", service.processSettlement(request));
    }
}
