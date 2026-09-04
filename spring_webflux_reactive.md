[🏠 Back to Home](README.md)

# ⚡ Spring WebFlux & Reactive Systems Architecture Master Guide

A production-grade engineering handbook for building ultra-high-throughput, non-blocking, asynchronous microservices using **Spring WebFlux**, **Project Reactor (`Mono` & `Flux`)**, **R2DBC**, **Reactive WebClient**, and **Netty**. Learn backpressure management, non-blocking relational persistence, event streaming (SSE), and concurrency debugging.

---

## 📑 Table of Contents

### Track 1: Junior & Entry-Level Foundations

- [🌱 1. Real-World Mental Model (Sit-Down Restaurant vs Drive-Through)](#1-the-real-world-mental-model-the-sit-down-restaurant-vs-the-fast-food-drive-through)
- [🧩 2. The 5 Core Building Blocks of Reactive Systems](#2-the-5-core-building-blocks)
- [💻 3. Beginner Code Walkthrough: Spring WebFlux & WebClient](#3-beginner-code-walkthrough-spring-webflux--webclient)
- [💥 4. What Happens When Things Break? (The Deadly `.block()` Trap)](#4-what-happens-when-things-break-the-deadly-block-trap)
- [⚠️ 5. Top 5 Beginner Mistakes in Production](#5-top-5-beginner-mistakes-in-production)
- [🎯 6. Top 10 Junior Interview Questions (With "ELI5" Answers)](#6-top-10-junior-interview-questions-with-explain-like-im-5-answers)

### Track 2: Advanced Architecture & Reactive Systems

1. [⚙️ 1. Project Reactor Core: Mono, Flux & Functional Operators](#️-1-project-reactor-core-mono-flux--functional-operators)
2. [🛑 2. Backpressure & Thread Scheduling (Schedulers)](#-2-backpressure--thread-scheduling-schedulers)
3. [🌐 3. Reactive Web Controllers & Functional Router Functions](#-3-reactive-web-controllers--functional-router-functions)
4. [📡 4. High-Performance Asynchronous HTTP: Reactive WebClient](#-4-high-performance-asynchronous-http-reactive-webclient)
5. [🗄️ 5. Non-Blocking Relational Persistence with R2DBC](#-5-non-blocking-relational-persistence-with-r2dbc)
6. [🌊 6. Real-Time Streaming: Server-Sent Events (SSE) & NDJSON](#-6-real-time-streaming-server-sent-events-sse--ndjson)
7. [🏭 7. Production Scenarios & War Room Incident Forensics](#-7-production-scenarios--war-room-incident-forensics)
8. [⚖️ 8. Spring WebFlux Master Cheat Sheet](#️-8-spring-webflux-master-cheat-sheet)

# TRACK 1: THE JUNIOR & ENTRY-LEVEL FOUNDATIONS (ZERO-TO-HERO)

## 1. The Real-World Mental Model (The Sit-Down Restaurant vs The Fast Food Drive-Through)

### Why Reactive Programming?
- **Traditional Spring MVC (The Fancy Sit-Down Restaurant):**
  - You hire 200 waiters (Tomcat 200 thread pool).
  - A waiter takes Customer 1's order and walks to the kitchen door.
  - The chef says: *"The steak takes 20 minutes to cook."*
  - The waiter **stands frozen in place outside the kitchen door for 20 minutes**, doing absolutely nothing while waiting for the steak!
  - If 201 customers arrive, Customer 201 has to wait outside in the rain because every single waiter is frozen waiting on food!
- **Spring WebFlux (The Fast Food Drive-Through with Buzzers):**
  - You only have **4 cashiers (Netty Event Loop Threads = CPU Core count)**!
  - Cashier 1 takes your order, hands you a vibrating pager (**`Mono` / `Flux`**), and immediately takes the next customer's order without waiting.
  - While the chef cooks, the cashiers handle 50,000 customers!
  - When your steak is ready, your pager vibrates (`onNext()`), and the cashier hands you your tray. **Nobody ever sits frozen waiting!**

---

### The Architecture Comparison

```
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                        TRADITIONAL SPRING MVC (Thread-per-Request)                     │
│                                                                                        │
│   Request 1 ──► [ Thread 1 ] ──► Blocks on DB Query (100ms) ──────────► Response 1    │
│   Request 2 ──► [ Thread 2 ] ──► Blocks on Remote REST (200ms) ───────► Response 2    │
│   Request N ──► 200 Threads Max (Tomcat pool exhausted ──► Queue Full ──► Latency Spike│
└────────────────────────────────────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────────────────────────────────────────┐
│                        SPRING WEBFLUX (Netty Non-Blocking Event Loop)                  │
│                                                                                        │
│   Request 1 ──┐                                                                        │
│   Request 2 ──┼──► [ 1 Netty Event Loop Thread ] ──► Registers Socket Callback         │
│   Request N ──┘            │                                    │                      │
│                            ▼                                    ▼                      │
│                  Zero Thread Blocking!             Socket emits data ready event       │
│                  Handles 50,000+ concurrent        Event Loop dispatches response      │
│                  connections on 8 CPU cores!                                           │
└────────────────────────────────────────────────────────────────────────────────────────┘
```

---

## 2. The 5 Core Building Blocks

| Term | What It Means | Real-World Analogy |
| :--- | :--- | :--- |
| **`Mono<T>`** | An asynchronous publisher that emits **0 or 1** item, or an error. | Ordering a single package from Amazon (it either arrives or fails). |
| **`Flux<T>`** | An asynchronous publisher that emits **0 to N** items in a continuous stream. | A water tap: turning it on streams water drops continuously. |
| **Event Loop (Netty)** | A single thread continuously polling OS network sockets using `epoll`. | A fast-food cashier ringing up customers without ever cooking food. |
| **Backpressure** | A mechanism where the consumer tells the producer: *"Slow down, I can only handle 5 items at a time!"* | A kid asking the candy dispenser to only drop 1 candy at a time so they don't choke. |
| **Reactive Streams** | The specification defining 4 interfaces: `Publisher`, `Subscriber`, `Subscription`, `Processor`. | The universal electrical socket standard allowing any appliance to plug in. |

---

## 3. Beginner Code Walkthrough: Spring WebFlux & WebClient

### Step 1: Clean Reactive Controller (`ProductReactiveController.java`)
```java
package com.example.webflux.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@RestController
@RequestMapping("/api/products")
public class ProductReactiveController {

    private final WebClient webClient;

    public ProductReactiveController(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://api.inventory.com").build();
    }

    // 1. Returns 0 or 1 item asynchronously (Non-blocking!)
    @GetMapping("/{id}")
    public Mono<ProductDto> getProduct(@PathVariable String id) {
        return webClient.get()
            .uri("/items/{id}", id)
            .retrieve()
            .bodyToMono(ProductDto.class)
            .timeout(Duration.ofSeconds(3))
            .onErrorReturn(new ProductDto(id, "Fallback Product", 0.0));
    }

    // 2. Returns 0 to N items streaming over time
    @GetMapping("/stream")
    public Flux<ProductDto> streamProducts() {
        return Flux.interval(Duration.ofSeconds(1)) // Emits tick every second
            .map(tick -> new ProductDto("prod-" + tick, "Item #" + tick, tick * 10.0))
            .take(5); // Stop after 5 items
    }
}

record ProductDto(String id, String name, Double price) {}
```

---

## 4. What Happens When Things Break? (The Deadly `.block()` Trap)

1. **Calling `.block()` on an Event Loop Thread:**
   ```java
   // ❌ THE PRODUCTION DEATH TRAP:
   @GetMapping("/{id}")
   public ProductDto badMethod(@PathVariable String id) {
       // CRASH: IllegalStateException: block()/blockFirst()/blockLast() are blocking, 
       // which is not supported in thread reactor-http-nio-1
       return webClient.get().uri("/...").retrieve().bodyToMono(ProductDto.class).block();
   }
   ```
   *Why this destroys your app:* Netty only has 4 to 8 threads for the *entire application*. If you call `.block()`, you freeze 1 of those 4 threads. 4 blocked requests will completely **freeze your entire server**, causing 100% outage for all users!
2. **"Nothing happens until you subscribe":**
   If you build a reactive pipeline: `mono.map(x -> x * 2);` but forget to return it from the controller or forget to call `.subscribe()`, **the code will NEVER execute**! Reactive streams are lazy pipelines.

---

## 5. Top 5 Beginner Mistakes in Production

1. **Calling `.block()` inside WebFlux Pipelines:** Never call `.block()`, `Thread.sleep()`, or blocking I/O on Netty worker threads.
2. **Using Traditional JDBC/JPA with WebFlux:** Standard JPA (Hibernate, PostgreSQL JDBC) is blocking. Using JPA inside WebFlux freezes Netty worker threads. **Fix:** Use **R2DBC** (Reactive Relational Database Connectivity) or offload blocking JPA to `Schedulers.boundedElastic()`.
3. **Using `try-catch` Blocks:** In reactive pipelines, errors travel as event signals (`onError`), not thrown Java exceptions! Standard `try-catch` blocks will NOT catch errors inside Mono/Flux. **Fix:** Use `.onErrorResume()` or `.onErrorReturn()`.
4. **Ignoring Backpressure on High-Volume Streams:** If a sensor emits 100,000 events/second and your database consumer can only write 1,000/second, the JVM will run out of memory buffering events. **Fix:** Use `.onBackpressureDrop()` or `.sample()`.
5. **Over-Using WebFlux for Simple CRUD Applications:** If your application is a simple internal CRUD app talking to a traditional relational database with moderate traffic, traditional Spring MVC with Virtual Threads (Java 21) is much simpler to read, debug, and maintain than WebFlux.

---

## 6. Top 10 Junior Interview Questions (With "Explain Like I'm 5" Answers)

### Q1: What is the difference between `Mono` and `Flux`?
- **ELI5 Answer:** *"`Mono` is a box that holds either 0 or 1 toy (or an empty box). `Flux` is a conveyor belt that can send 10, 100, or a million toys one-by-one."*
- **Technical Answer:** *"Both are Project Reactor `Publisher` implementations implementing the Reactive Streams specification. `Mono<T>` represents an asynchronous sequence of 0 or 1 element, terminating with an `onComplete` or `onError` signal. `Flux<T>` represents an asynchronous sequence of 0 to $N$ elements."*

### Q2: What does "Nothing happens until you subscribe" mean?
- **ELI5 Answer:** *"Writing a recipe on paper doesn't make a cake. You only get a cake when you actually turn on the oven and start baking!"*
- **Technical Answer:** *"Reactive pipelines are lazy declarations of intent. Until a subscriber attaches via `.subscribe()` (or Spring WebFlux subscribes on HTTP request processing), no data flows, no HTTP calls are made, and no computation occurs."*

### Q3: Why is calling `.block()` dangerous in Spring WebFlux?
- **ELI5 Answer:** *"If the only 4 cashiers in the store fall asleep waiting for a delivery, nobody can pay for their groceries and the store shuts down."*
- **Technical Answer:** *"Spring WebFlux runs on an event-loop architecture with a tiny thread pool (typically equal to the number of CPU cores). Calling `.block()` blocks an event-loop thread. If a few requests block simultaneously, the entire Netty event loop is starved, halting all concurrent request processing system-wide."*

### Q4: What is Backpressure and why is it essential?
- **ELI5 Answer:** *"If your friend shoots 50 tennis balls at your face in 1 second, you shout 'STOP, throw 1 at a time!' so you don't get hurt."*
- **Technical Answer:** *"Backpressure is a flow-control mechanism defined in the Reactive Streams specification. The `Subscriber` requests a specific demand (`request(n)`), ensuring the `Publisher` never pushes data faster than the downstream consumer can process, preventing heap exhaustion."*

### Q5: How does Spring WebFlux achieve high concurrency with few threads?
- **ELI5 Answer:** *"1 smart waiter holding an order pad who takes everyone's order instantly, instead of 200 lazy waiters standing frozen outside the kitchen door."*
- **Technical Answer:** *"WebFlux uses Netty's non-blocking I/O event demultiplexer (POSIX `epoll` or `kqueue`). When a request waits for external network or disk I/O, the OS notifies the event loop via callbacks when data is ready, freeing threads to handle other active socket channels."*

### Q6: What is R2DBC and why is it needed instead of JDBC?
- **ELI5 Answer:** *"JDBC is an old wooden pipe that blocks the hallway. R2DBC is a modern fiber-optic cable that lets multiple signals pass through simultaneously."*
- **Technical Answer:** *"Traditional JDBC is fundamentally synchronous and blocking at the socket level. R2DBC (Reactive Relational Database Connectivity) is a non-blocking, asynchronous reactive driver specification that enables fully non-blocking SQL queries without tying up operating system threads."*

### Q7: What is the difference between `map()` and `flatMap()` in Project Reactor?
- **ELI5 Answer:** *"`map` is painting an apple red ($1 \to 1$). `flatMap` is opening a bag of 5 apples and putting each apple onto the conveyor belt asynchronously ($1 \to \text{Publisher}$)."*
- **Technical Answer:** *"`map()` is synchronous 1-to-1 transformation ($T \to R$). `flatMap()` is asynchronous 1-to-$N$ transformation ($T \to \text{Publisher}<R>$) that subscribes to inner publishers concurrently, flattening emissions into a single output stream."*

### Q8: What are `Schedulers` in Project Reactor?
- **ELI5 Answer:** *"The manager who decides which room and desk each worker should sit at to do their chores."*
- **Technical Answer:** *"`Schedulers` manage execution context and thread pools in Reactor. Common schedulers include `Schedulers.parallel()` (CPU-bound work, size = CPU cores), `Schedulers.boundedElastic()` (blocking I/O work, elastic pool), and `Schedulers.immediate()`."*

### Q9: How do you handle exceptions in a reactive stream?
- **ELI5 Answer:** *"Putting a safety net under the tightrope walker so if they slip, they bounce into a soft cushion instead of hitting the floor."*
- **Technical Answer:** *"In reactive streams, exceptions are emitted as terminal `onError` signals. You handle them using reactive operators like `.onErrorReturn(fallback)`, `.onErrorResume(e -> fallbackPublisher)`, `.retry(3)`, or `.onErrorMap()`."*

### Q10: What are Server-Sent Events (SSE) and how does WebFlux support them?
- **ELI5 Answer:** *"A walkie-talkie where the server keeps talking to your web browser with new updates without the browser ever asking again."*
- **Technical Answer:** *"Server-Sent Events (SSE) is an HTTP standard (`text/event-stream`) for pushing one-way real-time data from server to client over a long-lived HTTP connection. WebFlux supports SSE natively by returning a `Flux<ServerSentEvent<T>>` from a `@GetMapping` endpoint."*

---

# TRACK 2: ADVANCED ARCHITECTURE & REACTIVE SYSTEMS

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
