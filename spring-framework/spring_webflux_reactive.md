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

# TRACK 2: ADVANCED ARCHITECTURE & MASTER REACTIVE FEATURE CATALOG

## Master Reactive Architecture Decision Matrix

| Reactive Component | Core Mechanism | Concurrency Model | Best Used For | Anti-Pattern For |
| :--- | :--- | :--- | :--- | :--- |
| **Netty EventLoop** | Non-blocking `epoll` socket loop| 1 Thread per CPU core | Ultra-high I/O concurrency ($>50\text{k req/s}$) | Long-running CPU math or blocking JDBC |
| **`Mono<T>`** | 0 or 1 Item Publisher | Asynchronous push | Single REST payload, DB row lookup | Streaming multi-item collections |
| **`Flux<T>`** | 0 to $N$ Stream Publisher | Push with Demand pull | Continuous live feeds, SSE, chunked DB | Single entity responses (use Mono) |
| **Backpressure** | Reactive Streams `request(N)` | Dynamic flow control | Preventing fast producers from OOMing workers | Fixed-capacity unbounded memory queues |
| **`boundedElastic()`** | Growable ThreadPool (10x cores)| Thread offloading | Wrapping legacy blocking disk/JDBC I/O | Pure non-blocking Netty operations |
| **WebClient** | Reactor Netty HTTP Client | Connection Pool + Zero-Copy | High-throughput non-blocking microservice RPC | Simple CLI scripts with 1 request |
| **R2DBC** | Reactive Relational Driver | Socket event streaming | Non-blocking Postgres/MySQL transactional apps | Hibernate ORM rich graph mappings |
| **Server-Sent Events** | Single persistent HTTP connection| Text Event Stream | Real-time dashboards, stock tickers | Bidirectional binary gaming (use WebSocket)|
| **BlockHound** | Bytecode Instrumentation | JVM runtime interceptor | Automated CI/CD regression defense | Production runtime overhead (dev/test only) |
| **Reactor Context** | Immutable Key-Value Map | Pipeline-propagated context| Distributed tracing (MDC), SecurityContext | Mutable global state machines |

---

## 2.1 Netty EventLoop & Channel Pipelines

1. **Architectural Overview & Purpose**:
   - WebFlux does not allocate 200 Tomcat worker threads. Instead, it runs on an event loop multiplexer powered by **Netty** (using Linux `epoll` or macOS `kqueue`). A small pool of threads (typically 1 per CPU core) manages tens of thousands of open TCP connections without blocking.

2. **Underlying Algorithm: Non-Blocking Socket Multiplexing**:
   - The OS kernel maintains readiness lists for network sockets.
   - When a client sends bytes, the kernel wakes the Netty Event Loop. The Event Loop reads the byte buffer, invokes the reactive pipeline, and dispatches the thread immediately to the next ready socket.
   - **The Golden Rule of Reactive**: A Netty thread **must NEVER sleep, wait on a lock, or call blocking I/O**. Blocking one thread freezes 25% to 50% of the entire application!

3. **Production Server Sizing**:
   ```yaml
   server:
     port: 8080
     netty:
       connection-timeout: 2000ms
       idle-timeout: 60s
   ```

---

## 2.2 Project Reactor Primitives: `Mono<T>` & `Flux<T>`

1. **Architectural Overview & Purpose**:
   - `Mono<T>` and `Flux<T>` are lazy asynchronous publishers implementing the Reactive Streams specification. Nothing executes until **`.subscribe()`** is called (which Spring WebFlux handles automatically when returning from a `@RestController`).

2. **Core Transformation Pipeline Operators**:
   - `.map()`: Synchronous 1-to-1 payload transformation on the current thread.
   - `.flatMap()`: Asynchronous 1-to-1 transformation where the mapper returns another `Mono`/`Flux`. Flattens concurrent async inner streams.
   - `.concatMap()`: Asynchronous transformation preserving strict sequence ordering (waits for inner Mono to complete before starting next).
   - `.zip()`: Combines multiple independent Monos concurrently and emits a joined tuple.

3. **Concrete Code Examples with Sample Values & Expected Results**:
   ```java
   @Service
   public class OrderEnrichmentService {

       public Mono<EnrichedOrder> enrichOrder(Mono<RawOrder> rawOrderMono) {
           return rawOrderMono
               .filter(order -> order.amount() > 0)
               .flatMap(order -> Mono.zip(
                   fetchCustomerRatingAsync(order.customerId()),
                   fetchTaxRateAsync(order.countryCode()),
                   (rating, tax) -> new EnrichedOrder(order.id(), order.amount() * (1 + tax), rating)
               ))
               .timeout(Duration.ofMillis(500))
               .onErrorResume(TimeoutException.class, ex -> Mono.just(EnrichedOrder.fallback()));
       }
       private Mono<String> fetchCustomerRatingAsync(String id) { return Mono.just("GOLD"); }
       private Mono<Double> fetchTaxRateAsync(String country) { return Mono.just(0.18); }
   }
   ```
   - **Sample Output**:
     ```
     EnrichedOrder[id="ORD-99", finalAmount=118.0, customerRating="GOLD"]
     ```

---

## 2.3 Backpressure Management & Overflow Strategies

1. **Architectural Overview & Purpose**:
   - If an upstream producer emits 50,000 events/sec but a downstream database writer can only insert 2,000 events/sec, traditional systems crash with `OutOfMemoryError`.
   - Backpressure enables the subscriber to dictate throughput via `request(n)` signals.

2. **Reactor Overflow Strategies**:
   ```java
   public Flux<StockPrice> consumeHighSpeedTicker(Flux<StockPrice> marketStream) {
       return marketStream
           // Buffer up to 1000 items; drop oldest if capacity breached
           .onBackpressureBuffer(1000, 
               dropped -> log.warn("Buffer full! Dropped stock tick: {}", dropped.ticker()),
               BufferOverflowStrategy.DROP_OLDEST)
           // Or drop entirely if downstream cannot keep up:
           // .onBackpressureDrop(tick -> log.warn("Dropped tick {}", tick.ticker()))
           // Or keep only the latest tick, discarding stale historical ticks:
           // .onBackpressureLatest()
           .publishOn(Schedulers.boundedElastic());
   }
   ```

---

## 2.4 Reactor Schedulers & Thread Pool Isolation

1. **Architectural Overview**:
   - Schedulers control the execution context (thread pool) of reactive stages:
     - `Schedulers.immediate()`: Executes on caller thread.
     - `Schedulers.parallel()`: Fixed worker threads sized to `Runtime.getRuntime().availableProcessors()`. For CPU-intensive algorithms.
     - `Schedulers.boundedElastic()`: Dynamically sized thread pool (default $10 \times \text{cores}$, capped queue of 100,000 tasks). **Mandatory when wrapping blocking legacy libraries (JDBC, disk I/O, SOAP)**.

2. **`publishOn` vs `subscribeOn` Invariant**:
   - `subscribeOn(scheduler)`: Changes the thread on which the source **subscription begins** (upstream).
   - `publishOn(scheduler)`: Switches execution to a new thread for all **downstream operators** following the call.

3. **Safe Legacy Blocking Wrapper Blueprint**:
   ```java
   public Mono<byte[]> readLegacyReportFromDisk(String filePath) {
       // Offload blocking disk read from Netty thread to boundedElastic thread
       return Mono.fromCallable(() -> Files.readAllBytes(Path.of(filePath)))
           .subscribeOn(Schedulers.boundedElastic());
   }
   ```

---

## 2.5 Reactive WebClient Asynchronous HTTP

1. **Architectural Overview**:
   - Non-blocking, reactive HTTP client replacing legacy blocking `RestTemplate`. Backed by Reactor Netty with connection pooling and DNS caching.

2. **Production WebClient Blueprint with Non-Blocking Retries**:
   ```java
   @Configuration
   public class WebClientConfig {

       @Bean
       public WebClient paymentGatewayClient(WebClient.Builder builder) {
           HttpClient httpClient = HttpClient.create()
               .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
               .responseTimeout(Duration.ofSeconds(5));

           return builder
               .baseUrl("https://payments.partner.com")
               .clientConnector(new ReactorClientHttpConnector(httpClient))
               .build();
       }
   }
   ```
   ```java
   @Service
   public class PaymentGatewayService {
       private final WebClient client;
       public PaymentGatewayService(WebClient client) { this.client = client; }

       public Mono<PaymentResponse> chargeCard(PaymentRequest req) {
           return client.post()
               .uri("/v1/charges")
               .bodyValue(req)
               .retrieve()
               .onStatus(HttpStatusCode::is5xxServerError, res -> 
                   Mono.error(new RemoteServiceUnavailableException("Partner gateway down")))
               .bodyToMono(PaymentResponse.class)
               .retryWhen(Retry.backoff(3, Duration.ofMillis(200))
                   .filter(RemoteServiceUnavailableException.class::isInstance));
       }
   }
   ```

---

## 2.6 Non-Blocking Relational Persistence with R2DBC

1. **Architectural Overview**:
   - JDBC is inherently blocking (calls `SocketInputStream.read()` synchronously).
   - **R2DBC (Reactive Relational Database Connectivity)** provides a 100% non-blocking protocol driver for PostgreSQL, MySQL, SQL Server, and Oracle.

2. **Reactive Entity & Repository**:
   ```java
   @Table("accounts")
   public record Account(@Id Long id, String accountNumber, Double balance) {}

   @Repository
   public interface AccountReactiveRepository extends ReactiveCrudRepository<Account, Long> {
       @Query("SELECT * FROM accounts WHERE balance > :minBalance")
       Flux<Account> findHighValueAccounts(Double minBalance);
   }
   ```

3. **Transactional Demarcation with `TransactionalOperator`**:
   ```java
   @Service
   public class ReactiveAccountTransferService {
       private final AccountReactiveRepository repo;
       private final TransactionalOperator txOperator;

       public ReactiveAccountTransferService(AccountReactiveRepository repo, TransactionalOperator tx) {
           this.repo = repo;
           this.txOperator = tx;
       }

       public Mono<Void> transferFunds(Long fromId, Long toId, Double amount) {
           return repo.findById(fromId)
               .flatMap(from -> repo.findById(toId)
                   .flatMap(to -> {
                       Account updatedFrom = new Account(from.id(), from.accountNumber(), from.balance() - amount);
                       Account updatedTo = new Account(to.id(), to.accountNumber(), to.balance() + amount);
                       return repo.save(updatedFrom).then(repo.save(updatedTo)).then();
                   }))
               .as(txOperator::transactional); // Atomic reactive transaction rollback on error!
       }
   }
   ```

---

## 2.7 Real-Time Streaming: Server-Sent Events (SSE) & NDJSON

1. **Architectural Overview**:
   - Stream data continuously over a single HTTP connection:
     - `text/event-stream`: SSE for web browsers (auto-reconnects, event IDs).
     - `application/x-ndjson`: Newline Delimited JSON for high-throughput service-to-service streaming.

2. **Production SSE Controller Blueprint**:
   ```java
   @GetMapping(value = "/api/v1/telemetry", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
   public Flux<ServerSentEvent<TelemetryData>> streamTelemetry() {
       return Flux.interval(Duration.ofSeconds(1))
           .map(sequence -> new TelemetryData("CPU", 45.0 + Math.random() * 10, Instant.now()))
           .map(data -> ServerSentEvent.<TelemetryData>builder()
               .id(String.valueOf(data.timestamp().toEpochMilli()))
               .event("telemetry-event")
               .data(data)
               .build());
   }
   ```

---

## 2.8 Functional Endpoints & RouterFunctions

1. **Architectural Overview**:
   - A lightweight, functional alternative to `@RestController` annotation reflection, offering faster startup and zero annotation overhead:
   ```java
   @Configuration
   public class ProductRouter {

       @Bean
       public RouterFunction<ServerResponse> productRoutes(ProductHandler handler) {
           return RouterFunctions.route()
               .GET("/routes/products/{id}", handler::getProduct)
               .POST("/routes/products", RequestPredicates.contentType(MediaType.APPLICATION_JSON), handler::createProduct)
               .build();
       }
   }
   ```

---

## 2.9 BlockHound Detection Engine: Catching Illegal Blocking Calls

1. **The Architecture of BlockHound**:
   - Instruments JVM bytecode at runtime. If any thread starting with `reactor-http-epoll` or `parallel` attempts to call a blocking method (`Thread.sleep()`, `SocketInputStream.read()`, `ReentrantLock.lock()`), BlockHound intercepts the call and immediately throws `BlockingOperationError` with a full stack trace!

2. **Integration in Unit Tests**:
   ```java
   @Test
   void shouldVerifyNoBlockingCalls() {
       BlockHound.install();
       assertDoesNotThrow(() -> {
           orderService.processOrderReactive(new OrderDto("123")).block();
       });
   }
   ```

---

## 2.10 Reactor Context & Distributed Tracing

1. **The Problem with ThreadLocal in Reactive Systems**:
   - In Spring MVC, `SecurityContextHolder` and SLF4J MDC use `ThreadLocal`.
   - In Spring WebFlux, a single request jumps across multiple threads (e.g. Netty Thread $1 \to$ BoundedElastic Thread $4 \to$ Netty Thread $2$). `ThreadLocal` values are lost!

2. **The Solution: Reactor Context**:
   - Context is an immutable key-value dictionary tied to the **Subscriber**, propagated upwards across all thread switches.
   ```java
   public Mono<String> processWithTenant() {
       return Mono.deferContextual(ctx -> {
           String tenantId = ctx.get("TENANT_ID");
           return Mono.just("Processed for tenant: " + tenantId);
       })
       .contextWrite(Context.of("TENANT_ID", "CORP-ACME"));
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
