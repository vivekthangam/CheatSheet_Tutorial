[🏠 Back to Home](README.md)

# ⚡ Spring WebFlux & Reactive Systems Architecture Master Guide

A production-grade engineering handbook for building ultra-high-throughput, non-blocking, asynchronous microservices using **Spring WebFlux**, **Project Reactor (`Mono` & `Flux`)**, **R2DBC**, **Reactive WebClient**, and **Netty**. Learn backpressure management, non-blocking relational persistence, event streaming (SSE), and concurrency debugging.

---

## 📑 Table of Contents
1. [🧠 Zero-to-Hero Mental Model: Thread-per-Request vs Event Loop](#-zero-to-hero-mental-model-thread-per-request-vs-event-loop)
2. [⚙️ 1. Project Reactor Core: Mono, Flux & Functional Operators](#️-1-project-reactor-core-mono-flux--functional-operators)
3. [🛑 2. Backpressure & Thread Scheduling (Schedulers)](#-2-backpressure--thread-scheduling-schedulers)
4. [🌐 3. Reactive Web Controllers & Functional Router Functions](#-3-reactive-web-controllers--functional-router-functions)
5. [📡 4. High-Performance Asynchronous HTTP: Reactive WebClient](#-4-high-performance-asynchronous-http-reactive-webclient)
6. [🗄️ 5. Non-Blocking Relational Persistence with R2DBC](#-5-non-blocking-relational-persistence-with-r2dbc)
7. [🌊 6. Real-Time Streaming: Server-Sent Events (SSE) & NDJSON](#-6-real-time-streaming-server-sent-events-sse--ndjson)
8. [🏭 7. Production Scenarios & War Room Incident Forensics](#-7-production-scenarios--war-room-incident-forensics)
9. [⚖️ 8. Spring WebFlux Master Cheat Sheet](#️-8-spring-webflux-master-cheat-sheet)

---

## 🧠 Zero-to-Hero Mental Model: Thread-per-Request vs Event Loop

```
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                        TRADITIONAL SPRING MVC (Thread-per-Request)                     │
│                                                                                        │
│   Request 1 ──> [ Thread 1 ] ──> Blocks on DB Query (100ms) ──────────> Response 1    │
│   Request 2 ──> [ Thread 2 ] ──> Blocks on Remote REST (200ms) ───────> Response 2    │
│   Request N ──> 200 Threads Max (Tomcat pool exhausted ──> Queue Full ──> Latency Spike│
└────────────────────────────────────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────────────────────────────────────────┐
│                        SPRING WEBFLUX (Netty Non-Blocking Event Loop)                  │
│                                                                                        │
│   Request 1 ──┐                                                                        │
│   Request 2 ──┼──> [ 1 Netty Event Loop Thread ] ──> Registers Socket Callback         │
│   Request N ──┘            │                                    │                      │
│                            ▼                                    ▼                      │
│                  Zero Thread Blocking!             Socket emits data ready event       │
│                  Handles 50,000+ concurrent        Event Loop dispatches response      │
│                  connections on 8 CPU cores!                                           │
└────────────────────────────────────────────────────────────────────────────────────────┘
```

1. **Spring MVC:** Uses 1 OS thread per active connection. When waiting on database I/O or network calls, threads sit idle in blocked state.
2. **Spring WebFlux:** Uses a small pool of worker threads (typically equal to available CPU cores). Operates asynchronously via POSIX epoll/kqueue event demultiplexing.

---

## ⚙️ 1. Project Reactor Core: Mono, Flux & Functional Operators

### Maven Dependencies (`pom.xml`)
```xml
<dependencies>
    <!-- WebFlux Starter (includes Netty and Project Reactor) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-webflux</artifactId>
    </dependency>

    <!-- Reactive Relational Database (R2DBC) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-r2dbc</artifactId>
    </dependency>
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>r2dbc-postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>
</dependencies>
```

### Mono vs Flux
- **`Mono<T>`:** A reactive stream emitting **0 or 1 item**, followed by an `onComplete` or `onError` signal.
- **`Flux<T>`:** A reactive stream emitting **0 to $N$ items**, followed by an `onComplete` or `onError` signal.

### Essential Reactor Transformation Pipeline
```java
package com.example.reactive.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
public class ReactiveOrderPipeline {

    public record Order(String id, double amount, String status) {}

    public Flux<Order> processOrderStream(Flux<Order> rawOrders) {
        return rawOrders
            // 1. Filter out cancelled orders
            .filter(order -> !"CANCELLED".equals(order.status()))
            // 2. Transform or enrich order
            .map(order -> new Order(order.id(), order.amount() * 1.05, "TAX_APPLIED"))
            // 3. FlatMap: Asynchronously call downstream non-blocking validation
            .flatMap(this::validateFraudAsync)
            // 4. Timeouts & Fallback
            .timeout(Duration.ofSeconds(3))
            .onErrorResume(ex -> Flux.empty());
    }

    private Mono<Order> validateFraudAsync(Order order) {
        return Mono.just(order)
            .delayElement(Duration.ofMillis(20)); // Simulates non-blocking async network check
    }
}
```

---

## 🛑 2. Backpressure & Thread Scheduling (Schedulers)

### What is Backpressure?
In traditional streaming, if a producer emits 100,000 items/sec but the consumer can only write 1,000 items/sec, the consumer runs out of memory. **Reactive Streams Backpressure** allows the consumer to tell the producer: *"Send me only 10 items (`request(10)`); I will ask for more when finished."*

```java
public Flux<TelemetryEvent> handleHighSpeedTelemetry(Flux<TelemetryEvent> stream) {
    return stream
        // Drop incoming events if consumer is too slow to keep up
        .onBackpressureDrop(dropped -> log.warn("Dropped telemetry event: {}", dropped.id()))
        // Or buffer up to 1000 items, discarding oldest on overflow
        // .onBackpressureBuffer(1000, BufferOverflowStrategy.DROP_OLDEST)
        .publishOn(Schedulers.boundedElastic());
}
```

### Reactor Schedulers: When to Switch Execution Contexts
- **`Schedulers.immediate()`:** Runs on the caller's current thread.
- **`Schedulers.parallel()`:** Fixed pool of worker threads sized to CPU cores. Ideal for CPU-intensive calculation.
- **`Schedulers.boundedElastic()`:** Dynamic, growable thread pool. **Must be used whenever wrapping legacy blocking code (e.g., blocking JDBC, legacy SOAP clients, local disk I/O)**:

```java
// ✅ Safely isolate legacy blocking call without freezing the Netty Event Loop
public Mono<String> readLegacyDiskFile(String path) {
    return Mono.fromCallable(() -> Files.readString(Path.of(path)))
        .subscribeOn(Schedulers.boundedElastic());
}
```

---

## 🌐 3. Reactive Web Controllers & Functional Router Functions

### 3.1 Annotated Reactive Controller
```java
package com.example.reactive.controller;

import com.example.reactive.entity.Customer;
import com.example.reactive.repository.CustomerReactiveRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerReactiveController {

    private final CustomerReactiveRepository repository;

    public CustomerReactiveController(CustomerReactiveRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Customer>> getCustomer(@PathVariable Long id) {
        return repository.findById(id)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping
    public Flux<Customer> getAllActive() {
        return repository.findAllByActiveTrue();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Customer> create(@RequestBody Customer customer) {
        return repository.save(customer);
    }
}
```

### 3.2 Modern Functional Endpoints (`RouterFunction`)
```java
package com.example.reactive.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class OrderRouterConfig {

    @Bean
    public RouterFunction<ServerResponse> orderRoutes(OrderHandler handler) {
        return route(GET("/routes/orders").and(accept(MediaType.APPLICATION_JSON)), handler::listOrders)
            .andRoute(POST("/routes/orders").and(contentType(MediaType.APPLICATION_JSON)), handler::createOrder);
    }
}
```

---

## 📡 4. High-Performance Asynchronous HTTP: Reactive WebClient

`WebClient` is the modern, non-blocking replacement for `RestTemplate`.

```java
package com.example.reactive.client;

import io.netty.channel.ChannelOption;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Service
public class PricingServiceClient {

    private final WebClient webClient;

    public PricingServiceClient(WebClient.Builder builder) {
        HttpClient httpClient = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2000)
            .responseTimeout(Duration.ofSeconds(3));

        this.webClient = builder
            .baseUrl("https://pricing-api.internal")
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .build();
    }

    public record PriceQuote(String sku, double price, String currency) {}

    public Mono<PriceQuote> fetchPrice(String sku) {
        return webClient.get()
            .uri("/v1/quotes/{sku}", sku)
            .retrieve()
            // Handle HTTP 4xx / 5xx non-blocking errors
            .onStatus(status -> status.is4xxClientError(), response -> 
                Mono.error(new IllegalArgumentException("Invalid SKU requested: " + sku)))
            .bodyToMono(PriceQuote.class)
            .timeout(Duration.ofSeconds(2))
            .onErrorReturn(new PriceQuote(sku, 0.0, "FALLBACK"));
    }
}
```

---

## 🗄️ 5. Non-Blocking Relational Persistence with R2DBC

Traditional JDBC blocks threads. **R2DBC** allows non-blocking relational database access via reactive drivers.

```java
package com.example.reactive.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("customers")
public record Customer(@Id Long id, String email, String name, boolean active) {}
```

```java
package com.example.reactive.repository;

import com.example.reactive.entity.Customer;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface CustomerReactiveRepository extends ReactiveCrudRepository<Customer, Long> {

    Flux<Customer> findAllByActiveTrue();

    @Query("SELECT * FROM customers WHERE email = :email LIMIT 1")
    Mono<Customer> findByEmail(String email);
}
```

---

## 🌊 6. Real-Time Streaming: Server-Sent Events (SSE) & NDJSON

Stream live data directly to browsers or upstream microservices over a single persistent HTTP connection.

```java
@GetMapping(value = "/stream/prices", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public Flux<ServerSentEvent<StockTick>> streamStockTicks() {
    return Flux.interval(Duration.ofSeconds(1))
        .map(sequence -> new StockTick("AAPL", 220.0 + Math.random() * 5, Instant.now()))
        .map(tick -> ServerSentEvent.<StockTick>builder()
            .id(String.valueOf(tick.timestamp().toEpochMilli()))
            .event("stock-update")
            .data(tick)
            .build());
}
```

---

## 🏭 7. Production Scenarios & War Room Incident Forensics

### Scenario 1: Accidental Blocking Call Freezes All Event Loops (`block()`)
- **Symptom:** API latency spikes from 5ms to 30 seconds for ALL users under only 20 concurrent requests.
- **Root Cause:** A developer wrote `mono.block()` or called a legacy blocking DB method on a Netty worker thread. Because Netty has only 8 worker threads, blocking 8 threads freezes the entire server.
- **The Fix:**
  1. Never call `.block()` or `.toStream()` on reactive types.
  2. Use **BlockHound** in unit tests to detect blocking calls during CI/CD:
  ```java
  @BeforeAll
  static void setupBlockHound() {
      BlockHound.install();
  }
  ```

---

## ⚖️ 8. Spring WebFlux Master Cheat Sheet

| Reactive Operator | Description / Purpose |
| :--- | :--- |
| **`.map(fn)`** | Synchronous 1-to-1 payload transformation |
| **`.flatMap(fn)`** | Asynchronous 1-to-1 transformation returning `Mono`/`Flux` |
| **`.delayElement(d)`** | Non-blocking pause without sleeping thread |
| **`.timeout(d)`** | Emits `TimeoutException` if signal not received within duration |
| **`.onErrorResume(fn)`**| Catches exception and switches to alternative reactive stream |
| **`.subscribeOn(sch)`** | Determines thread on which subscription begins |
| **`.publishOn(sch)`** | Switches downstream execution to a different thread pool |
| **`Mono.zip(m1, m2)`** | Executes multiple async Monos concurrently and joins results |

---
[🏠 Back to Home](README.md)
