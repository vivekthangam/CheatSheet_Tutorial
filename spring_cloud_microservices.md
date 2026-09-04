[🏠 Back to Home](README.md)

# ☁️ Spring Cloud & Microservices Architecture Master Guide

A production-grade engineering handbook for building resilient, scalable, and observable distributed systems using **Spring Cloud 2023+**, **Spring Boot 3.x**, and **Java 17/21**. Covers Spring Cloud Gateway, OpenFeign, Resilience4j Circuit Breakers, Service Discovery, Distributed Tracing with Micrometer, and zero-downtime config management.

---

## 📑 Table of Contents

### Track 1: Junior & Entry-Level Foundations

- [🌱 1. Real-World Mental Model (Castle vs Archipelago)](#1-the-real-world-mental-model-the-monolithic-castle-vs-the-archipelago-of-merchant-islands)
- [🧩 2. The 5 Core Building Blocks of Spring Cloud](#2-the-5-core-building-blocks-of-spring-cloud)
- [💻 3. Beginner Code Walkthrough: Resilient Microservice Client](#3-beginner-code-walkthrough-resilient-microservice-client)
- [💥 4. What Happens When Things Break? (Top 3 Disasters)](#4-what-happens-when-things-break-top-3-disasters)
- [⚠️ 5. Top 5 Beginner Mistakes in Production](#5-top-5-beginner-mistakes-in-production)
- [🎯 6. Top 10 Junior Interview Questions (With "ELI5" Answers)](#6-top-10-junior-interview-questions-with-explain-like-im-5-answers)

### Track 2: Advanced Mechanics & Production Scenarios

1. [🚪 1. Spring Cloud Gateway: Predicates, Filters & Rate Limiting](#-1-spring-cloud-gateway-predicates-filters--rate-limiting)
2. [📡 2. Declarative HTTP Clients: Spring Cloud OpenFeign](#-2-declarative-http-clients-spring-cloud-openfeign)
3. [🛡️ 3. Fault Tolerance & Circuit Breaking: Resilience4j](#️-3-fault-tolerance--circuit-breaking-resilience4j)
4. [🧭 4. Service Registry & Discovery (Eureka / Consul)](#-4-service-registry--discovery-eureka--consul)
5. [🔍 5. Distributed Tracing & Observability (Micrometer & Zipkin)](#-5-distributed-tracing--observability-micrometer--zipkin)
6. [🏭 6. Production Scenarios & War Room Incident Forensics](#-6-production-scenarios--war-room-incident-forensics)
7. [⚖️ 7. Spring Cloud Master Cheat Sheet](#️-7-spring-cloud-master-cheat-sheet)

---

# TRACK 1: THE JUNIOR & ENTRY-LEVEL FOUNDATIONS (ZERO-TO-HERO)

## 1. The Real-World Mental Model (The Monolithic Castle vs The Archipelago of Merchant Islands)

### Why Microservices?
- **The Monolith (The Giant Medieval Castle):** All departments (kitchen, treasury, armory, guards) live inside one single stone fortress.
  - *The Danger:* If a grease fire starts in the kitchen, the **entire castle burns down**, and everyone flees together! If you need to upgrade the kitchen stove, you have to shut down the entire castle.
- **Microservices (The Archipelago of Merchant Islands):** Each department lives on its own island:
  - Island 1: The Order Service.
  - Island 2: The Payment Service.
  - Island 3: The Inventory Service.
  - *The Advantage:* If Island 2 has a power outage, Island 1 and Island 3 keep running. You can independently deploy and scale Island 1 without touching Island 2!
  - *The New Challenge:* How do boats (network calls) find each island? What happens if high waves (network timeouts) sink a boat? This is where **Spring Cloud** comes in.

---

### The 5 Core Building Blocks of Spring Cloud

```
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                                 THE MICROSERVICES TOPOLOGY                             │
│                                                                                        │
│   Client Request (Mobile / Web)                                                        │
│           │                                                                            │
│           ▼                                                                            │
│   ┌─────────────────────────────────────────────────────────┐                          │
│   │               SPRING CLOUD API GATEWAY                  │                          │
│   │  - Single entry door (Airport Security & Ticket Check)  │                          │
│   │  - Rate Limiting (Redis token bucket)                   │                          │
│   │  - Path Routing (Routes /orders to Order Service)       │                          │
│   └─────────────────────────────────────────────────────────┘                          │
│           │                                      │                                     │
│           ▼ (Load Balanced: lb://)               ▼                                     │
│   ┌───────────────────────┐              ┌───────────────────────┐                     │
│   │     Order Service     │ ──Feign───►  │    Payment Service    │                     │
│   │  - Resilience4j CB    │              │  - Standalone service │                     │
│   │  - Micrometer Tracing │              │  - Distributed DB     │                     │
│   └───────────────────────┘              └───────────────────────┘                     │
│           ▲                                      ▲                                     │
│           └───────────────────┬──────────────────┘                                     │
│                               ▼                                                        │
│                 [ Eureka Service Registry ]                                            │
└────────────────────────────────────────────────────────────────────────────────────────┘
```

---

## 2. The 5 Core Building Blocks of Spring Cloud

| Component | What It Does | Real-World Analogy | Spring Cloud Technology |
| :--- | :--- | :--- | :--- |
| **API Gateway** | Single reverse-proxy entry door for all external clients; handles SSL, auth, routing, and rate limiting. | Airport security checkpoint and terminal boarding gate. | `Spring Cloud Gateway` (Reactor / Netty) |
| **Service Registry & Discovery** | Dynamic phonebook tracking IP addresses and health of every ephemeral microservice instance. | The hotel front-desk concierge matching guests to current room numbers. | `Netflix Eureka`, `HashiCorp Consul` |
| **Declarative REST Client** | Generates HTTP client implementations from simple annotated interfaces without boilerplate. | A walkie-talkie where you press a button and speak directly to a colleague. | `Spring Cloud OpenFeign` |
| **Circuit Breaker & Resilience** | Halts outbound calls to failing downstreams to prevent thread pool exhaustion and cascading crashes. | An electrical fuse box cutting power to a smoking appliance before the house burns down. | `Resilience4j` |
| **Distributed Tracing** | Propagates unique `traceId` and `spanId` across asynchronous network hops for latency visualization. | A package tracking number stamped on a parcel as it travels between sorting hubs. | `Micrometer Tracing` + `Zipkin` / `Tempo` |

---

## 3. Beginner Code Walkthrough: Resilient Microservice Client

Here is how an **Order Service** safely talks to a remote **Payment Service** in modern Spring Boot 3 / Spring Cloud:

```java
package com.example.orderservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

// 🌟 Trainer Rule #1: Use declarative Feign with a FallbackFactory!
// "PAYMENT-SERVICE" resolves dynamically through Eureka discovery.
@FeignClient(name = "PAYMENT-SERVICE", fallbackFactory = PaymentClientFallbackFactory.class)
public interface PaymentClient {

    @PostMapping("/api/v1/payments/process")
    PaymentResponse processPayment(@RequestBody PaymentRequest request);
}

// 🌟 Trainer Rule #2: Fallback Factory captures the ROOT CAUSE exception
@Component
class PaymentClientFallbackFactory implements org.springframework.cloud.openfeign.FallbackFactory<PaymentClient> {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PaymentClientFallbackFactory.class);

    @Override
    public PaymentClient create(Throwable cause) {
        return request -> {
            log.error("🚨 Fallback triggered for Order [{}]. Downstream Payment Service failed: {}",
                request.orderId(), cause.getMessage());

            // 🌟 Trainer Rule #3: Graceful degradation instead of crashing the customer!
            // Return a PENDING/QUEUED state so the customer order is not lost.
            return new PaymentResponse(request.orderId(), "PAYMENT_QUEUED_OFFLINE", 0.0);
        };
    }
}
```

---

## 4. What Happens When Things Break? (Top 3 Disasters)

1. **The Cascading Failure Death Spiral:**
   The Inventory Service experiences a slow database query (latency jumps from 50ms to 9 seconds). The Order Service calls Inventory on every checkout. Because Tomcat has a default pool of 200 threads, within 30 seconds all 200 threads in Order Service are blocked waiting for Inventory. The API Gateway then exhausts its own connections waiting for Order Service. **Result: The entire company website crashes because of one slow table!**
   - *Fix:* Implement Resilience4j Circuit Breaker with a strict 2-second timeout and fail-fast fallbacks.
2. **The Eureka Phantom Instance Zombie:**
   A microservice container crashes violently (OOMKilled by Kubernetes). However, Eureka’s lease renewal hasn't expired yet (30-second window), or Eureka enters **Self-Preservation Mode** because of network blips. The API Gateway continues routing 50% of user traffic to a dead IP address, generating thousands of HTTP 500/503 errors!
   - *Fix:* Tune Eureka client refresh intervals (`registry-fetch-interval-seconds: 5`) and enable active health checks via Spring Boot Actuator.
3. **The Silent Security Token Drop:**
   A user logs in at the frontend, receives a JWT Bearer token, and hits the API Gateway. The Gateway routes to the Order Service (Token intact). The Order Service then calls Payment Service via OpenFeign. But Feign creates a brand new HTTP request **without headers**! Payment Service returns HTTP 401 Unauthorized, and developers spend hours debugging why "auth isn't working."
   - *Fix:* Register a Feign `RequestInterceptor` that copies the `Authorization` header from `RequestContextHolder` to outgoing requests.

---

## 5. Top 5 Beginner Mistakes in Production

1. **Building Distributed Monoliths:** Creating 20 microservices that share a single MySQL database! If Service A alters a table schema, Service B, C, and D immediately crash. Rule: *Every microservice must own its own private database schema.*
2. **Synchronous REST Chains (HTTP Spaghetti):** Service A calls B, which calls C, which calls D, which calls E synchronously. If each call has 99.9% availability, the 5-step chain has only $0.999^5 \approx 99.5\%$ availability, and latency is cumulative. Use asynchronous event-driven messaging (Kafka/RabbitMQ) for decoupled operations.
3. **Using Blocking `Thread.sleep()` or Blocking I/O inside Spring Cloud Gateway:** Spring Cloud Gateway runs on Netty with a tiny thread pool (equal to CPU cores). If you execute a blocking JDBC query or `Thread.sleep()` in a Gateway filter, you freeze the entire server! Gateway filters must be 100% non-blocking Reactive (`Mono`/`Flux`).
4. **Hardcoding IP Addresses or URLs:** Writing `@FeignClient(url = "http://192.168.1.50:8081")`. In Kubernetes or cloud environments, container IPs change constantly. Always route via Service Discovery or Kubernetes DNS (`name = "payment-service"`).
5. **Ignoring Distributed Tracing Early On:** Deploying microservices without correlation IDs (`traceId`). When a customer reports an order error, you have to manually open logs across 8 different servers trying to match timestamps.

---

## 6. Top 10 Junior Interview Questions (With "ELI5" Answers)

### Q1: What is the fundamental difference between a Monolith and Microservices?

- **ELI5 Answer:** *"A Monolith is an all-in-one Swiss Army knife—if the main blade snaps, the whole tool is broken. Microservices are a set of specialized screwdrivers, scissors, and knives in a toolbox—if one tool breaks, you still have all the others."*
- **Technical Answer:** *"A monolith bundles UI, business logic, and database access into a single deployable artifact running in one process. Microservices decompose a domain into independent, loosely coupled, autonomous services communicating over lightweight protocols (HTTP/gRPC/Kafka), each possessing its own database, build pipeline, and scaling lifecycle."*

### Q2: What is an API Gateway and why don't frontend apps call microservices directly?

- **ELI5 Answer:** *"An airport security gate and departure board. Instead of passengers wandering randomly onto the tarmac looking for their airplane, everyone enters through one secure door where tickets are checked, and you are pointed to the exact gate."*
- **Technical Answer:** *"An API Gateway is a reverse proxy acting as a single entry point for external clients. It encapsulates internal topology, prevents CORS issues, offloads cross-cutting concerns (SSL termination, rate limiting, authentication/JWT verification, metrics), and avoids forcing mobile clients to make 10 separate chatty network round-trips to assemble one screen."*

### Q3: How does Service Discovery (Eureka) work?

- **ELI5 Answer:** *"Like a school attendance register. Every morning each student reports 'Present!' to the teacher. When someone wants to play catch with Johnny, they ask the teacher which desk Johnny is sitting at today."*
- **Technical Answer:** *"When a microservice starts, it registers its hostname, IP, and port with the Eureka server. Periodically (default every 30s), it sends a heartbeat ping (lease renewal). Client services fetch the registry cache locally and use client-side load balancing (Spring Cloud LoadBalancer) to route requests without hardcoded IPs."*

### Q4: What is OpenFeign and how does it compare to RestTemplate?

- **ELI5 Answer:** *"RestTemplate is like dialing every digit of a phone number manually every time. OpenFeign is speed-dial: you just tap 'Mom' and the phone handles the dialing."*
- **Technical Answer:** *"OpenFeign is a declarative HTTP client. Developers write a Java interface annotated with Spring MVC annotations (`@GetMapping`, `@PostMapping`), and Spring Cloud dynamically generates the runtime proxy implementation, integrating seamlessly with Eureka discovery and Resilience4j circuit breaking."*

### Q5: What is a Circuit Breaker and what are its three states?

- **ELI5 Answer:** *"An electrical fuse in your house. When a toaster shorts out, the fuse trips so the whole house doesn't catch fire. It gives the toaster time to cool down before you test it again."*
- **Technical Answer:** *"A Circuit Breaker prevents cascading failures when downstream services become unresponsive. Its three states are:
  1. **CLOSED:** Normal operation; all requests pass through. Metrics (failures/timeouts) are recorded.
  2. **OPEN:** Failure rate exceeds threshold (e.g., >50%). Inbound requests immediately fail-fast (or trigger fallbacks) without hitting the dying service.
  3. **HALF-OPEN:** After a wait duration, a limited trial batch of requests is allowed through. If successful, the circuit resets to CLOSED; if failures persist, it flips back to OPEN."*

### Q6: What is the difference between `@Retry` and `@CircuitBreaker`?

- **ELI5 Answer:** *"`Retry` is knocking on a door 3 times because your friend might have been in the bathroom. `CircuitBreaker` is noticing the house is on fire and stopping everyone from knocking so nobody gets burned."*
- **Technical Answer:** *"`@Retry` re-executes transient, idempotent failures (e.g., momentary network glitches or 503 drops) up to $N$ times with exponential backoff. `@CircuitBreaker` monitors systemic downstream health over a sliding time window and actively blocks calls when the downstream service is degraded to prevent thread exhaustion."*

### Q7: How do you trace a single user request across 5 microservices?

- **ELI5 Answer:** *"Stamping a unique postal tracking barcode on the box at the first post office. Every warehouse along the route scans the exact same barcode into the central system."*
- **Technical Answer:** *"Using Distributed Tracing (Micrometer Tracing + OpenTelemetry / Zipkin). The edge gateway generates a unique W3C `traceId` (identifying the entire distributed transaction) and a `spanId` (identifying a single service hop). These identifiers are passed in HTTP headers (`traceparent`) across all Feign and Kafka hops, aggregating logs into a waterfall graph."*

### Q8: What is Eureka Self-Preservation Mode?

- **ELI5 Answer:** *"A lifeguard who suddenly loses sight of all swimmers due to heavy fog. Instead of assuming all 100 swimmers drowned at once, the lifeguard assumes the fog is the problem and waits before sounding the death alarm."*
- **Technical Answer:** *"If the Eureka server stops receiving heartbeats from more than 15% of registered instances within 15 minutes, it assumes a local network partition has occurred rather than all services dying simultaneously. Eureka enters Self-Preservation Mode and pauses instance eviction to prevent routing catastrophic dropouts."*

### Q9: How do you pass authentication from the Gateway to downstream microservices?

- **ELI5 Answer:** *"Showing your ID badge at the main building entrance, where they give you an all-access visitor wristband that guards at every internal door inspect."*
- **Technical Answer:** *"The API Gateway verifies the user's credentials or JWT at the edge. Downstream routing is configured with the `TokenRelay` filter or a custom Feign `RequestInterceptor` that reads the `Authorization: Bearer <token>` from the `SecurityContext` and relays it to all internal HTTP requests."*

### Q10: What is the CAP Theorem and what does a standard Spring Cloud architecture choose?

- **ELI5 Answer:** *"You can only pick two at a restaurant: Fast Food, Cheap Food, or High Quality Food. You cannot have all three at once."*
- **Technical Answer:** *"The CAP Theorem states that a distributed data store can guarantee at most two of three properties: **Consistency**, **Availability**, and **Partition Tolerance**. Because network partitions ($P$) are unavoidable in distributed cloud systems, Spring Cloud and Eureka typically choose **AP (Availability and Partition tolerance)** with eventual consistency over CP."*

---

# TRACK 2: ADVANCED MECHANICS & PRODUCTION SCENARIOS

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
