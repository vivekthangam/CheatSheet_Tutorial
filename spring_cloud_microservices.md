[🏠 Back to Home](README.md)

# ☁️ Spring Cloud & Microservices Architecture Master Guide

A production-grade engineering handbook for building resilient, scalable, and observable distributed systems using **Spring Cloud 2023+**, **Spring Boot 3.x**, and **Java 17/21**. Covers Spring Cloud Gateway, OpenFeign, Resilience4j Circuit Breakers, Service Discovery, Distributed Tracing with Micrometer, and zero-downtime config management.

---

## 📑 Table of Contents
1. [🧠 Zero-to-Hero Mental Model: The Distributed Enterprise Archipelago](#-zero-to-hero-mental-model-the-distributed-enterprise-archipelago)
2. [🚪 1. Spring Cloud Gateway: Predicates, Filters & Rate Limiting](#-1-spring-cloud-gateway-predicates-filters--rate-limiting)
3. [📡 2. Declarative HTTP Clients: Spring Cloud OpenFeign](#-2-declarative-http-clients-spring-cloud-openfeign)
4. [🛡️ 3. Fault Tolerance & Circuit Breaking: Resilience4j](#️-3-fault-tolerance--circuit-breaking-resilience4j)
5. [🧭 4. Service Registry & Discovery (Eureka / Consul)](#-4-service-registry--discovery-eureka--consul)
6. [🔍 5. Distributed Tracing & Observability (Micrometer & Zipkin)](#-5-distributed-tracing--observability-micrometer--zipkin)
7. [🏭 6. Production Scenarios & War Room Incident Forensics](#-6-production-scenarios--war-room-incident-forensics)
8. [⚖️ 7. Spring Cloud Master Cheat Sheet](#️-7-spring-cloud-master-cheat-sheet)

---

## 🧠 Zero-to-Hero Mental Model: The Distributed Enterprise Archipelago

```
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                                 THE MICROSERVICES TOPOLOGY                             │
│                                                                                        │
│   Client Request (Mobile / Web)                                                        │
│           │                                                                            │
│           ▼                                                                            │
│   ┌─────────────────────────────────────────────────────────┐                          │
│   │               SPRING CLOUD API GATEWAY                  │                          │
│   │  - Auth Validation (JWT Token Relay)                    │                          │
│   │  - Redis Distributed Rate Limiter                       │                          │
│   │  - Dynamic Path Routing & Canary Splits                 │                          │
│   └─────────────────────────────────────────────────────────┘                          │
│           │                                      │                                     │
│           ▼ (Load Balanced: lb://)               ▼                                     │
│   ┌───────────────────────┐              ┌───────────────────────┐                     │
│   │     Order Service     │ ──Feign───>  │    Payment Service    │                     │
│   │  - Resilience4j CB    │              │  - Distributed Tx     │                     │
│   │  - Micrometer Tracing │              │  - OpenTelemetry Span │                     │
│   └───────────────────────┘              └───────────────────────┘                     │
│           ▲                                      ▲                                     │
│           └───────────────────┬──────────────────┘                                     │
│                               ▼                                                        │
│                 [ Eureka Service Registry ]                                            │
└────────────────────────────────────────────────────────────────────────────────────────┘
```

---

## 🚪 1. Spring Cloud Gateway: Predicates, Filters & Rate Limiting

Spring Cloud Gateway is built on **Project Reactor** and **Netty**, providing a non-blocking, asynchronous reverse proxy.

### Maven Dependencies (`pom.xml`)
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-gateway</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis-reactive</artifactId>
</dependency>
```

### Production Route Configuration (`application.yml`)
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: order-service-route
          uri: lb://ORDER-SERVICE # Routes dynamically via Eureka load balancer
          predicates:
            - Path=/api/v1/orders/**
            - Method=GET,POST,PUT
          filters:
            - StripPrefix=0
            - AddRequestHeader=X-Gateway-Timestamp, ${system.currentTimeMillis}
            # Distributed Redis Rate Limiting
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 100 # 100 requests per second
                redis-rate-limiter.burstCapacity: 200 # Allow bursts up to 200 reqs
                key-resolver: "#{@apiKeyResolver}"
```

### Custom KeyResolver Bean (Rate Limiting by Client IP or API Key)
```java
package com.example.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Configuration
public class GatewayRateLimitConfig {

    @Bean
    public KeyResolver apiKeyResolver() {
        return exchange -> {
            String apiKey = exchange.getRequest().getHeaders().getFirst("X-API-KEY");
            if (apiKey != null && !apiKey.isBlank()) {
                return Mono.just(apiKey);
            }
            // Fall back to remote client IP
            return Mono.just(Objects.requireNonNull(
                exchange.getRequest().getRemoteAddress()).getAddress().getHostAddress());
        };
    }
}
```

---

## 📡 2. Declarative HTTP Clients: Spring Cloud OpenFeign

OpenFeign allows developers to write declarative HTTP clients simply by declaring Java interfaces.

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
```

### Feign Client Declaration with Resilience4j Fallback
```java
package com.example.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
    name = "PAYMENT-SERVICE",
    path = "/api/v1/payments",
    fallbackFactory = PaymentClientFallbackFactory.class
)
public interface PaymentClient {

    @GetMapping("/{orderId}/status")
    PaymentStatusDto getPaymentStatus(@PathVariable("orderId") String orderId);
}
```

### Fallback Factory (Handles Fallback Logic & Logs Root Cause)
```java
package com.example.order.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class PaymentClientFallbackFactory implements FallbackFactory<PaymentClient> {

    private static final Logger log = LoggerFactory.getLogger(PaymentClientFallbackFactory.class);

    @Override
    public PaymentClient create(Throwable cause) {
        return orderId -> {
            log.warn("Payment-Service unavailable for order [{}]. Fallback triggered! Reason: {}",
                orderId, cause.getMessage());
            // Return cached, degraded or default response
            return new PaymentStatusDto(orderId, "UNKNOWN_OFFLINE", 0.0);
        };
    }
}
```

---

## 🛡️ 3. Fault Tolerance & Circuit Breaking: Resilience4j

Resilience4j implements stateful circuit breaking to prevent cascading failures across downstream microservices.

```
                  ┌───────────────────────────────┐
                  │          CLOSED               │
                  │ (Normal Operation: All pass)  │
                  └───────────────────────────────┘
                     │                         ▲
           Failure   │                         │ Success Rate
           Rate >50% │                         │ Recovered
                     ▼                         │
                  ┌───────────────────────────────┐
                  │            OPEN               │
                  │ (Fail-Fast: Calls rejected)   │
                  └───────────────────────────────┘
                     │
         Wait 10 sec │
                     ▼
                  ┌───────────────────────────────┐
                  │          HALF-OPEN            │
                  │ (Test sample of 5 requests)   │
                  └───────────────────────────────┘
```

### Configuration (`application.yml`)
```yaml
resilience4j:
  circuitbreaker:
    instances:
      inventoryService:
        sliding-window-type: COUNT_BASED
        sliding-window-size: 20
        minimum-number-of-calls: 10
        failure-rate-threshold: 50.0 # Open circuit if 50% calls fail
        wait-duration-in-open-state: 10000ms
        permitted-number-of-calls-in-half-open-state: 5
  retry:
    instances:
      inventoryService:
        max-attempts: 3
        wait-duration: 1000ms
        enable-exponential-backoff: true
```

### Service Method Protected by CircuitBreaker & Retry
```java
package com.example.order.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.stereotype.Service;

@Service
public class InventoryProxyService {

    @CircuitBreaker(name = "inventoryService", fallbackMethod = "reserveFallback")
    @Retry(name = "inventoryService")
    public boolean reserveStock(String sku, int quantity) {
        // Calls remote service via HTTP
        return inventoryClient.reserve(sku, quantity);
    }

    // Fallback must match method signature + Throwable parameter
    public boolean reserveFallback(String sku, int quantity, Throwable t) {
        log.error("Circuit breaker OPEN for SKU [{}]. Falling back to queued reservation.", sku);
        return false;
    }
}
```

---

## 🧭 4. Service Registry & Discovery (Eureka / Consul)

Enables dynamic IP/port resolution so services can locate instances without hardcoding static URLs.

```yaml
eureka:
  client:
    service-url:
      defaultZone: http://eureka-primary:8761/eureka/,http://eureka-secondary:8761/eureka/
    registry-fetch-interval-seconds: 15
  instance:
    prefer-ip-address: true
    lease-renewal-interval-in-seconds: 10
    lease-expiration-duration-in-seconds: 30
```

---

## 🔍 5. Distributed Tracing & Observability (Micrometer & Zipkin)

In Spring Boot 3, **Micrometer Tracing** replaces Spring Cloud Sleuth, adopting the open **W3C TraceContext** standard (`traceparent` header).

```xml
<!-- Distributed Tracing with Micrometer & OpenTelemetry -->
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-tracing-bridge-otel</artifactId>
</dependency>
<dependency>
    <groupId>io.opentelemetry</groupId>
    <artifactId>opentelemetry-exporter-zipkin</artifactId>
</dependency>
```

```yaml
management:
  tracing:
    sampling:
      probability: 1.0 # Sample 100% of requests in dev/staging (adjust to 0.1 in prod)
  zipkin:
    tracing:
      endpoint: http://zipkin-server:9411/api/v2/spans
```

Every outgoing Feign call or incoming REST request automatically propagates:
`traceparent: 00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01`
Allowing end-to-end distributed latency waterfalls in Zipkin or Jaeger!

---

## 🏭 6. Production Scenarios & War Room Incident Forensics

### Scenario 1: Cascading Failure Outage
- **Symptom:** Payment service slows down from 50ms to 8 seconds. Within 2 minutes, Order Service, Cart Service, and API Gateway all crash with HTTP 504 Gateway Timeouts.
- **Root Cause:** Missing Circuit Breakers. Order Service worker threads remained blocked waiting for Payment Service responses until Tomcat's 200 request threads were exhausted.
- **The Fix:** Implement Resilience4j Circuit Breaker with a strict 2-second timeout and fail-fast fallback.

### Scenario 2: Security Token Drop in Feign Calls
- **Symptom:** An authenticated user calls Order Service, but Order Service's Feign call to Payment Service fails with HTTP 401 Unauthorized.
- **The Fix:** Register a Feign `RequestInterceptor` that automatically extracts the `Bearer` token from the current `SecurityContext` and relays it:

```java
@Component
public class FeignAuthRelayInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate template) {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            String authHeader = attrs.getRequest().getHeader("Authorization");
            if (authHeader != null) {
                template.header("Authorization", authHeader);
            }
        }
    }
}
```

---

## ⚖️ 7. Spring Cloud Master Cheat Sheet

| Feature / Pattern | Key Annotation / Syntax |
| :--- | :--- |
| **Gateway Routing** | `spring.cloud.gateway.routes[0].uri: lb://SERVICE-NAME` |
| **Declarative Feign**| `@FeignClient(name = "SERVICE-NAME", fallbackFactory = ...)` |
| **Circuit Breaker** | `@CircuitBreaker(name = "backendA", fallbackMethod = "fallback")` |
| **Retry Policy** | `@Retry(name = "backendA")` |
| **Token Relay** | `- name: TokenRelay` in Gateway filter definition |
| **Enable Clients** | `@EnableFeignClients`, `@EnableDiscoveryClient` |
| **Trace ID Injection**| `%X{traceId:-}` in Logback pattern |

---
[🏠 Back to Home](README.md)
