package com.cheatsheet.ioc.engine;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.annotation.Scope;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.lang.annotation.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

// ============================================================================
// 1. DOMAIN MODELS & CUSTOM AUDIT ANNOTATION
// ============================================================================
public class OrderInvoicingEngine {

    public record Invoice(String id, String customerId, BigDecimal amount, Instant timestamp) {}
    public record InvoiceGeneratedEvent(String invoiceId, BigDecimal amount, Instant timestamp) {}

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    public @interface AuditedOperation {
        String action();
    }

    // ============================================================================
    // 2. BEAN FACTORY POST-PROCESSOR (BFPP)
    // ============================================================================
    @Component
    public static class TenantSchemaResolverBFPP implements BeanFactoryPostProcessor {
        @Override
        public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
            for (String beanName : beanFactory.getBeanDefinitionNames()) {
                BeanDefinition bd = beanFactory.getBeanDefinition(beanName);
                if (InvoiceProcessingService.class.getName().equals(bd.getBeanClassName())) {
                    bd.getPropertyValues().add("activeTenantRegion", "US-EAST-VA");
                }
            }
        }
    }

    // ============================================================================
    // 3. BEAN POST-PROCESSOR (BPP): Dynamic CGLIB Proxy Wrapping
    // ============================================================================
    @Component
    public static class AuditLoggingBeanPostProcessor implements BeanPostProcessor {
        @Override
        public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
            boolean hasAuditedMethod = Arrays.stream(bean.getClass().getMethods())
                    .anyMatch(m -> m.isAnnotationPresent(AuditedOperation.class));

            if (!hasAuditedMethod) {
                return bean;
            }

            Enhancer enhancer = new Enhancer();
            enhancer.setSuperclass(bean.getClass());
            enhancer.setCallback((MethodInterceptor) (obj, method, args, proxy) -> {
                if (method.isAnnotationPresent(AuditedOperation.class)) {
                    AuditedOperation audit = method.getAnnotation(AuditedOperation.class);
                    long start = System.nanoTime();
                    try {
                        Object result = proxy.invokeSuper(obj, args);
                        long durationMs = (System.nanoTime() - start) / 1_000_000;
                        System.out.printf("[AUDIT LOG] Action: %s | Method: %s | Duration: %d ms%n",
                                audit.action(), method.getName(), durationMs);
                        return result;
                    } catch (Exception ex) {
                        System.err.printf("[AUDIT ALERT] Action %s failed: %s%n", audit.action(), ex.getMessage());
                        throw ex;
                    }
                }
                return proxy.invokeSuper(obj, args);
            });

            return enhancer.create();
        }
    }

    // ============================================================================
    // 4. PROTOTYPE GENERATOR & SINGLETON FACADE
    // ============================================================================
    @Component
    @Scope("prototype")
    public static class CurrencyNormalizer {
        private final String calculationSessionId = UUID.randomUUID().toString();

        public BigDecimal normalizeToUsd(BigDecimal amount, String sourceCurrency) {
            if ("EUR".equalsIgnoreCase(sourceCurrency)) return amount.multiply(BigDecimal.valueOf(1.08));
            if ("GBP".equalsIgnoreCase(sourceCurrency)) return amount.multiply(BigDecimal.valueOf(1.27));
            return amount;
        }

        public String getCalculationSessionId() { return calculationSessionId; }
    }

    @Service
    public static class InvoiceProcessingService {
        private final ObjectProvider<CurrencyNormalizer> normalizerProvider;
        private final ApplicationEventPublisher eventPublisher;
        private String activeTenantRegion;

        public InvoiceProcessingService(ObjectProvider<CurrencyNormalizer> normalizerProvider,
                                        ApplicationEventPublisher eventPublisher) {
            this.normalizerProvider = normalizerProvider;
            this.eventPublisher = eventPublisher;
        }

        public void setActiveTenantRegion(String activeTenantRegion) {
            this.activeTenantRegion = activeTenantRegion;
        }

        public String getActiveTenantRegion() {
            return activeTenantRegion;
        }

        @AuditedOperation(action = "GENERATE_ENTERPRISE_INVOICE")
        public Invoice generateInvoice(String customerId, BigDecimal rawAmount, String currency) {
            CurrencyNormalizer normalizer = normalizerProvider.getObject();
            BigDecimal normalizedAmount = normalizer.normalizeToUsd(rawAmount, currency);

            String invoiceId = "INV-" + UUID.randomUUID().toString().substring(0, 8);
            Invoice invoice = new Invoice(invoiceId, customerId, normalizedAmount, Instant.now());

            eventPublisher.publishEvent(new InvoiceGeneratedEvent(invoiceId, normalizedAmount, Instant.now()));
            return invoice;
        }
    }

    // ============================================================================
    // 5. SMART LIFECYCLE & ASYNC EVENT LISTENER
    // ============================================================================
    @Component
    public static class InvoicingOrderProcessorLifecycle implements SmartLifecycle {
        private final AtomicBoolean isRunning = new AtomicBoolean(false);

        @Override
        public void start() {
            if (isRunning.compareAndSet(false, true)) {
                System.out.println("[SMART LIFECYCLE] Invoicing Order Processor warm-up complete. Accepting traffic.");
            }
        }

        @Override
        public void stop() {
            if (isRunning.compareAndSet(true, false)) {
                System.out.println("[SMART LIFECYCLE] Invoicing Order Processor shutting down. Draining queues.");
            }
        }

        @Override
        public boolean isRunning() { return isRunning.get(); }

        @Override
        public int getPhase() { return Integer.MAX_VALUE - 100; }

        @EventListener
        public void onInvoiceGenerated(InvoiceGeneratedEvent event) {
            System.out.printf("[EVENT NOTIFIER] Dispatched Ledger Notification for Invoice: %s | Amount: $%s%n",
                    event.invoiceId(), event.amount().toPlainString());
        }
    }
}
