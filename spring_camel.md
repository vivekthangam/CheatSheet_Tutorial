[🏠 Back to Home](README.md)

# 🐪 Apache Camel 4 & Spring Boot 3 Enterprise Integration Master Guide

A production-grade engineering handbook for building resilient, event-driven, and enterprise integration solutions using **Apache Camel 4.x** with **Spring Boot 3.x** and **Java 17/21**. Covers Enterprise Integration Patterns (EIP), Java DSL, high-throughput streaming, fault tolerance, transaction management, and end-to-end testing.

---

## 📑 Table of Contents

### Track 1: Junior & Entry-Level Foundations

- [🌱 1. Real-World Mental Model (Airport Freight Hub & Plumbing)](#1-the-real-world-mental-model-the-universal-airport-cargo-hub--plumbing-system)
- [🧩 2. The 5 Core Building Blocks of Apache Camel](#the-5-core-building-blocks)
- [💻 3. Beginner Code Walkthrough: Your First Route](#2-beginner-code-walkthrough-your-first-route)
- [💥 4. What Happens When Things Break? (Dead Letter Channels & Retries)](#3-what-happens-when-things-break-dead-letter-channels--retries)
- [⚠️ 5. Top 5 Beginner Mistakes in Production](#4-top-5-beginner-mistakes-in-production)
- [🎯 6. Top 10 Junior Interview Questions (With "ELI5" Answers)](#5-top-10-junior-interview-questions-with-explain-like-im-5-answers)

### Track 2: Advanced Architecture & Enterprise Integration

1. [⚙️ 1. Architecture: CamelContext, Exchange & Endpoints](#️-1-architecture-camelcontext-exchange--endpoints)
2. [🔀 2. Enterprise Integration Patterns (EIP) in Practice](#-2-enterprise-integration-patterns-eip-in-practice)
3. [🔌 3. Production Connectors: File, Kafka, REST, JMS & SEDA](#-3-production-connectors-file-kafka-rest-jms--seda)
4. [🛡️ 4. Error Handling, Retries & Dead Letter Channels](#️-4-error-handling-retries--dead-letter-channels)
5. [💳 5. Idempotent Consumers & Distributed Transactions](#-5-idempotent-consumers--distributed-transactions)
6. [🧪 6. Testing Camel Routes with CamelTestSupport & Mocks](#-6-testing-camel-routes-with-cameltestsupport--mocks)
7. [🏭 7. Production Scenarios & War Room Failure Forensics](#-7-production-scenarios--war-room-failure-forensics)
8. [⚖️ 8. Apache Camel 4 Master Cheat Sheet](#️-8-apache-camel-4-master-cheat-sheet)

# TRACK 1: THE JUNIOR & ENTRY-LEVEL FOUNDATIONS (ZERO-TO-HERO)

## 1. The Real-World Mental Model (The Universal Airport Cargo Hub & Plumbing System)

### What Problem Does Apache Camel Solve?
Imagine you work at a busy international airport freight terminal:
- Every hour, packages arrive via **trains** (JMS), **cargo ships** (SFTP), **trucks** (Kafka), and **courier vans** (REST APIs).
- Some packages are wooden crates (XML), some are cardboard boxes (JSON), and some are envelopes (CSV).
- You have to:
  1. Unpack each crate,
  2. Inspect and validate the contents,
  3. Re-label it with a custom barcode,
  4. Ship it out on an outgoing cargo plane (AWS S3) or courier truck (PostgreSQL database).
- **Without Camel:** You would have to write hundreds of lines of custom networking glue code, socket listeners, and protocol adapters for each connection.
- **With Apache Camel:** Camel is the **Universal Plumbing & Logistics Engine**. It connects over 300 different protocols out of the box using simple, readable English-like pipelines called **Routes**:
  `from("file:orders/inbox").filter(...).to("kafka:orders-topic")`.

---

### The 5 Core Building Blocks

```
┌───────────────────────────────────────────────────────────────────────────────────┐
│                           CAMEL CONTEXT (The Postal Hub)                           │
│                                                                                   │
│  [Source: SFTP/Kafka] ──► [Envelope: Exchange] ──► [Sorting Belt: Route]         │
│                                  │                            │                   │
│                        ┌─────────┴─────────┐                  ▼                   │
│                        │ In Message Body   │         [Inspector: Processor]       │
│                        │ Headers / Metadata│                  │                   │
│                        │ Exception Details │                  ▼                   │
│                        └───────────────────┘       [Decision: Router/EIP]         │
│                                                               │                   │
│                                                               ▼                   │
│                                                    [Destination: DB/REST/JMS]     │
└───────────────────────────────────────────────────────────────────────────────────┘
```

| Term | What It Means | Real-World Analogy |
| :--- | :--- | :--- |
| **`Endpoint`** | A URI representing where data comes from or goes to (e.g. `kafka:orders`). | The loading dock where trucks dock. |
| **`Exchange`** | The carrier envelope holding the message body, headers, and error details. | The shipping container with a bill of lading taped to the side. |
| **`Route`** | The step-by-step pipeline defining where data travels (`from(...)` to `to(...)`). | The automated conveyor belt guiding the package through scanners. |
| **`Processor`** | A custom Java method that inspects, transforms, or enriches the message. | The inspector who opens the package, verifies contents, and stamps approval. |
| **`CamelContext`** | The master runtime container holding all routes, components, and thread pools. | The entire airport warehouse operations center. |

---

## 2. Beginner Code Walkthrough: Your First Route

### A Complete Spring Boot 3 RouteBuilder (`OrderIngestionRoute.java`)
```java
package com.example.camel.routes;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class OrderIngestionRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        // 1. Error Handling: If an unexpected error strikes, retry 3 times, then send to DLQ
        errorHandler(deadLetterChannel("kafka:orders-dlq")
            .maximumRedeliveries(3)
            .redeliveryDelay(1000)
            .retryAttemptedLogLevel(org.apache.camel.LoggingLevel.WARN));

        // 2. The Core Route
        from("file:data/inbox?noop=true&delay=5000") // Poll folder every 5 seconds
            .routeId("file-to-kafka-route")
            .log("📦 Discovered raw order file: ${file:name}")
            
            // Transform & Inspect
            .process(exchange -> {
                String originalBody = exchange.getIn().getBody(String.class);
                // Attach audit timestamp header
                exchange.getIn().setHeader("ingestionTimestamp", System.currentTimeMillis());
                // Transform to uppercase JSON
                exchange.getIn().setBody(originalBody.trim());
            })
            
            // Route to Kafka broker
            .to("kafka:validated-orders?brokers=localhost:9092")
            .log("✅ Successfully dispatched order to Kafka!");
    }
}
```

---

## 3. What Happens When Things Break? (Dead Letter Channels & Retries)

```
                            [ ERROR HANDLING FLOW ]
Message Fails in Processor ──► Retry 1 (1s wait) ──► Retry 2 (2s wait) ──► Retry 3
                                                                               │
                                            All Retries Exhausted!            ▼
                                    [ Sent to Dead Letter Queue (DLQ) ]
                                    (Alert on-call; human inspects payload)
```

- When an exception is thrown in a route, Camel catches it via `errorHandler` or `onException(MyException.class)`.
- You can configure exponential backoff retries.
- If all retries fail, Camel moves the original unmodified message to a **Dead Letter Channel** (e.g. `kafka:orders-dlq`), saving it from being lost forever.

---

## 4. Top 5 Beginner Mistakes in Production

1. **The Stream Caching Trap:** By default, reading an `InputStream` in a processor consumes the stream bytes. If the route tries to read the body again in a subsequent step, the stream is **empty**, leading to empty payloads or errors! **Fix:** Enable Stream Caching: `context.setStreamCaching(true)`.
2. **Blocking Routes with Synchronous I/O:** Using `direct:` routes for heavy external calls can block the producer thread. **Fix:** Use `seda:` (in-memory asynchronous queue) or real message brokers (`kafka:`, `jms:`) to decouple producers and consumers.
3. **Hardcoding Component URIs:** Writing `from("file:/var/data/inbox?delay=1000")` directly in Java code. **Fix:** Use property placeholders: `from("file:{{app.inbox.path}}?delay={{app.inbox.delay}}")`.
4. **Missing Idempotent Consumer on Retried Messages:** If an upstream system resends a duplicate message after a network glitch, Camel will process the order twice unless protected. **Fix:** Add `.idempotentConsumer(header("orderId"), memoryIdempotentRepository(10000))`.
5. **Throwing Raw Exceptions Without Dead Letter Channels:** Without an error handler, a poison-pill message will crash the route and cause the consumer to re-fetch the exact same bad message in an infinite CPU loop!

---

## 5. Top 10 Junior Interview Questions (With "Explain Like I'm 5" Answers)

### Q1: What is Apache Camel and what does it do?
- **ELI5 Answer:** *"Like a universal language translator and postal delivery truck for computers. It lets computers that speak different languages (like Files, Kafka, and HTTP) pass notes to each other effortlessly."*
- **Technical Answer:** *"Apache Camel is a lightweight, open-source integration framework based on Enterprise Integration Patterns (EIP). It provides a rule-based routing and mediation engine that bridges disparate systems using standardized URIs across 300+ protocol components."*

### Q2: What is an `Exchange` in Apache Camel?
- **ELI5 Answer:** *"The envelope that holds your letter. The envelope has your name and address on the outside (headers) and the actual letter inside (the message body)."*
- **Technical Answer:** *"An `Exchange` is the central message container created when an event arrives at an endpoint. It holds the `In` message (payload + headers), optional `Out` message, custom properties (persisted across the entire route lifecycle), and any exception thrown during execution."*

### Q3: What is the difference between `direct:`, `seda:`, and `vm:` in Camel?
- **ELI5 Answer:** *"`direct` is handing a letter directly to a person standing next to you. `seda` is putting the letter into an inbox tray so someone else can pick it up when they are free."*
- **Technical Answer:** *"`direct:` executes synchronously on the caller's thread with zero queueing. `seda:` (Staged Event-Driven Architecture) provides asynchronous, in-memory queueing within the same `CamelContext`. `vm:` extends `seda:` to allow asynchronous queueing between different CamelContexts in the same JVM."*

### Q4: What is an Enterprise Integration Pattern (EIP)?
- **ELI5 Answer:** *"A proven recipe book for fixing common plumbing problems in factories, so engineers don't have to invent new pipes from scratch."*
- **Technical Answer:** *"EIPs are standardized design patterns established by Gregor Hohpe and Bobby Woolf (e.g. Content-Based Router, Splitter, Aggregator, Recipient List) that solve common data-routing, transformation, and messaging challenges in enterprise architectures."*

### Q5: What is a Content-Based Router?
- **ELI5 Answer:** *"A mail sorter who looks at the zip code on an envelope: letters to California go into Box A; letters to New York go into Box B."*
- **Technical Answer:** *"The Content-Based Router EIP inspects the payload or headers of an incoming exchange and conditionally routes the message to different destination endpoints using `.choice().when(...).to(...).otherwise().to(...)`."*

### Q6: What is the Aggregator pattern in Camel?
- **ELI5 Answer:** *"Gathering 5 puzzle pieces sent from 5 different friends and taping them together to form 1 completed picture before hanging it on the wall."*
- **Technical Answer:** *"The Aggregator EIP combines multiple related incoming messages into a single unified exchange based on a correlation expression (e.g. `header("orderId")`) and a completion condition (e.g. batch size or timeout)."*

### Q7: What is the Splitter pattern in Camel?
- **ELI5 Answer:** *"Opening a box of 12 cupcakes and handing out 1 cupcake to 12 individual kids."*
- **Technical Answer:** *"The Splitter EIP takes a composite message (e.g. a list of 1,000 orders in a single XML/JSON payload) and decomposes it into individual exchanges, processing each item independently."*

### Q8: What is a Dead Letter Channel?
- **ELI5 Answer:** *"The 'Lost and Found' box at the post office. If a letter has an illegible address that cannot be delivered after 3 tries, it goes into the lost box so postal workers can inspect it manually."*
- **Technical Answer:** *"A Dead Letter Channel is an EIP error-handling pattern. When a message fails processing after exhausting all redelivery attempts, Camel captures the failed exchange and routes it to a designated dead-letter endpoint (e.g., a DLQ topic or error database) without interrupting the main route."*

### Q9: What is an Idempotent Consumer in Camel?
- **ELI5 Answer:** *"A bouncer with a memory who remembers your face. If you try to enter the club twice with the same ticket, the bouncer catches you and says 'you are already inside!'."*
- **Technical Answer:** *"The Idempotent Consumer EIP prevents duplicate processing of messages (e.g., retried payment webhooks). It checks a unique identifier (like a message ID or transaction hash) against an in-memory, Redis, or JDBC repository before executing the route."*

### Q10: What is Type Conversion in Camel?
- **ELI5 Answer:** *"A magical chef who instantly turns a bag of oranges into orange juice whenever a recipe asks for juice instead of fruit."*
- **Technical Answer:** *"Camel has a built-in `TypeConverter` registry. When a component expects a `String` but receives an `InputStream` or `byte[]`, Camel automatically converts the payload between types at runtime without requiring manual parsing code."*

---

# TRACK 2: ADVANCED ARCHITECTURE & ENTERPRISE INTEGRATION

## ⚙️ 1. Architecture: CamelContext, Exchange & Endpoints

### Maven Dependencies (`pom.xml`)
```xml
<properties>
    <camel.version>4.8.0</camel.version>
    <spring-boot.version>3.3.4</spring-boot.version>
</properties>

<dependencies>
    <!-- Core Camel Spring Boot Starter -->
    <dependency>
        <groupId>org.apache.camel.springboot</groupId>
        <artifactId>camel-spring-boot-starter</artifactId>
        <version>${camel.version}</version>
    </dependency>

    <!-- Connectors -->
    <dependency>
        <groupId>org.apache.camel.springboot</groupId>
        <artifactId>camel-kafka-starter</artifactId>
        <version>${camel.version}</version>
    </dependency>
    <dependency>
        <groupId>org.apache.camel.springboot</groupId>
        <artifactId>camel-http-starter</artifactId>
        <version>${camel.version}</version>
    </dependency>
    <dependency>
        <groupId>org.apache.camel.springboot</groupId>
        <artifactId>camel-jackson-starter</artifactId>
        <version>${camel.version}</version>
    </dependency>

    <!-- Testing -->
    <dependency>
        <groupId>org.apache.camel</groupId>
        <artifactId>camel-test-spring-junit5</artifactId>
        <version>${camel.version}</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### RouteBuilder Fundamentals
In Camel, routes are configured by extending `RouteBuilder` and overriding `configure()`.

```java
package com.example.camel.routes;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class BasicOrderRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        // Direct endpoint (synchronous in-memory pipeline)
        from("direct:processOrder")
            .routeId("process-order-route")
            .log("Incoming order payload: ${body}")
            // Modify or inspect using an inline Processor
            .process(exchange -> {
                String payload = exchange.getIn().getBody(String.class);
                exchange.getIn().setHeader("ProcessedTimestamp", System.currentTimeMillis());
                exchange.getIn().setBody(payload.toUpperCase());
            })
            .log("Transformed order payload: ${body}")
            .to("mock:orderOutput");
    }
}
```

---

## 🔀 2. Enterprise Integration Patterns (EIP) in Practice

### 2.1 Content-Based Router (`choice`, `when`, `otherwise`)
Routes messages to different downstream targets based on header or body evaluation.

```java
@Component
public class ContentBasedOrderRouter extends RouteBuilder {

    @Override
    public void configure() {
        from("kafka:orders-inbound?brokers=localhost:9092&groupId=order-router")
            .routeId("content-based-order-router")
            .unmarshal().json(OrderEvent.class)
            .choice()
                .when(simple("${body.orderType} == 'VIP'"))
                    .log("Routing VIP order [${body.orderId}] to high-priority queue")
                    .to("kafka:vip-orders?brokers=localhost:9092")
                .when(simple("${body.orderType} == 'STANDARD'"))
                    .log("Routing Standard order [${body.orderId}]")
                    .to("kafka:standard-orders?brokers=localhost:9092")
                .otherwise()
                    .log(LoggingLevel.WARN, "Unknown order type: ${body.orderType}. Diverting to review queue.")
                    .to("kafka:unrecognized-orders?brokers=localhost:9092")
            .end();
    }
}
```

### 2.2 Splitter (`split`)
Breaks a composite payload (e.g. JSON array, CSV lines, XML nodes) into individual messages for parallel or sequential processing.

```java
@Component
public class OrderBatchSplitterRoute extends RouteBuilder {

    @Override
    public void configure() {
        from("direct:bulkOrderUpload")
            .routeId("bulk-order-splitter")
            // Split JSON array using JsonPath or collection getter
            .split(body(), new BulkAggregationStrategy())
                .parallelProcessing() // Distribute across worker thread pool
                .streaming()          // Stream chunks without reading entire file into JVM memory
                .log("Processing item: ${body}")
                .to("direct:processIndividualItem")
            .end()
            .log("All items in batch processed successfully.");
    }
}
```

### 2.3 Aggregator (`aggregate`)
Merges multiple related messages into a single combined message based on a **Correlation Expression** and a **Completion Condition** (size, timeout, or predicate).

```java
package com.example.camel.aggregator;

import org.apache.camel.AggregationStrategy;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PaymentBatchAggregatorRoute extends RouteBuilder {

    @Override
    public void configure() {
        from("direct:paymentStream")
            .routeId("payment-aggregator")
            // Aggregate payments by 'MerchantId' header
            .aggregate(header("MerchantId"), new ListAggregationStrategy())
                .completionSize(100)               // Aggregate when 100 payments reached
                .completionTimeout(5000)           // Or emit batch every 5 seconds
                .log("Emitting batch for merchant ${header.MerchantId}, size: ${body.size}")
                .to("direct:settleMerchantBatch");
    }

    public static class ListAggregationStrategy implements AggregationStrategy {
        @Override
        @SuppressWarnings("unchecked")
        public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
            Object newBody = newExchange.getIn().getBody();
            List<Object> list;

            if (oldExchange == null) {
                list = new ArrayList<>();
                list.add(newBody);
                newExchange.getIn().setBody(list);
                return newExchange;
            }

            list = oldExchange.getIn().getBody(List.class);
            list.add(newBody);
            return oldExchange;
        }
    }
}
```

### 2.4 Wire Tap & Recipient List
- **Wire Tap:** Asynchronously taps a copy of the message for audit, telemetry, or shadow processing without altering the main route execution path.
- **Recipient List:** Dynamically determines target endpoints at runtime from message headers.

```java
@Component
public class WireTapAndRecipientRoute extends RouteBuilder {

    @Override
    public void configure() {
        from("direct:financialTransaction")
            // Asynchronously send copy to audit without slowing main execution
            .wireTap("direct:auditLogQueue")
            .process(exchange -> {
                // Determine destination dynamically
                boolean isEuCustomer = exchange.getIn().getHeader("Region", String.class).equals("EU");
                exchange.getIn().setHeader("TargetEndpoint", isEuCustomer ? "kafka:eu-clearing" : "kafka:us-clearing");
            })
            // Dynamic routing via Recipient List
            .recipientList(header("TargetEndpoint"));

        from("direct:auditLogQueue")
            .to("kafka:audit-events?brokers=localhost:9092");
    }
}
```

---

## 🔌 3. Production Connectors: File, Kafka, REST, JMS & SEDA

| Component URI | Protocol / Behavior | Threading Model | Common Production Option Flags |
| :--- | :--- | :--- | :--- |
| `file://target/input` | File Poller / Consumer | Polling consumer (Scheduled) | `noop=true`, `move=.done`, `readLock=changed`, `maxMessagesPerPoll=50` |
| `kafka:topic-name` | Kafka Consumer / Producer | Netty / Kafka polling threads | `brokers=...`, `groupId=...`, `autoOffsetReset=earliest`, `maxPollRecords=500` |
| `rest:get:/api/v1/orders` | HTTP Inbound Server | Embedded Servlet / Tomcat | `consumes=application/json`, `produces=application/json` |
| `jms:queue:paymentQueue`| ActiveMQ / Artemis | JMS MessageListener threads | `concurrentConsumers=5`, `transacted=true`, `acknowledgementModeName=CLIENT_ACKNOWLEDGE` |
| `seda:inMemoryQueue` | In-memory BlockingQueue | Decoupled Async Worker Pool | `size=1000`, `concurrentConsumers=10`, `blockWhenFull=true` |
| `direct:internal` | In-memory Synchronous | Runs on caller thread | Zero overhead, strict transactional propagation |

### 3.1 High-Throughput REST-to-Kafka Bridge
```java
package com.example.camel.routes;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.stereotype.Component;

@Component
public class RestToKafkaApiRoute extends RouteBuilder {

    @Override
    public void configure() {
        // Configure REST engine
        restConfiguration()
            .component("servlet")
            .bindingMode(RestBindingMode.json)
            .dataFormatProperty("prettyPrint", "true");

        rest("/v1/telemetry")
            .post("/event")
                .type(TelemetryPayload.class)
                .to("direct:dispatchTelemetry");

        from("direct:dispatchTelemetry")
            .routeId("dispatch-telemetry-route")
            .setHeader("KafkaKey", simple("${body.deviceId}"))
            .marshal().json()
            .to("kafka:telemetry-ingest?brokers=localhost:9092"
                + "&keySerializer=org.apache.kafka.common.serialization.StringSerializer"
                + "&valueSerializer=org.apache.kafka.common.serialization.StringSerializer")
            .setHeader("CamelHttpResponseCode", constant(202))
            .setBody(constant("{\"status\":\"ACCEPTED\"}"));
    }
}
```

---

## 🛡️ 4. Error Handling, Retries & Dead Letter Channels

Camel provides resilient, declarative exception handling with exponential backoff and dead-letter queueing.

```mermaid
flowchart TD
    A[Incoming Message] --> B[Route Processing]
    B -->|Exception Thrown| C{Transient Error?}
    C -->|Yes: Network Timeout| D[Redelivery Policy: 3 Retries, Exponential Backoff]
    D -->|Succeeds| E[Resume Normal Flow]
    D -->|Exhausted| F[Dead Letter Channel: kafka:dlq-events]
    C -->|No: Malformed JSON| F
    F --> G[Alerting / Manual Remediation]
```

### Production Fault-Tolerance Route Configuration
```java
package com.example.camel.routes;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;
import java.net.ConnectException;

@Component
public class ResilientPaymentRoute extends RouteBuilder {

    @Override
    public void configure() {
        // Global Route Error Handler: Dead Letter Channel
        errorHandler(deadLetterChannel("kafka:payment-dlq?brokers=localhost:9092")
            .useOriginalMessage()
            .maximumRedeliveries(3)
            .redeliveryDelay(1000)
            .backOffMultiplier(2.0)
            .retryAttemptedLogLevel(LoggingLevel.WARN)
            .logExhausted(true));

        // Specialized Exception Handling: Skip retries for business rule violations
        onException(IllegalArgumentException.class)
            .handled(true) // Mark as handled to prevent further redelivery loops
            .log(LoggingLevel.ERROR, "Validation failed: ${exception.message}. Diverting directly to reject topic.")
            .to("kafka:payment-rejected?brokers=localhost:9092");

        // Transient Connection Failures: Specific Retry Rules
        onException(ConnectException.class)
            .maximumRedeliveries(5)
            .redeliveryDelay(2000)
            .backOffMultiplier(2.5)
            .handled(false); // Let it propagate to DLQ if 5 retries fail

        from("kafka:payment-inbound?brokers=localhost:9092&groupId=payment-engine")
            .routeId("payment-processor-route")
            .to("http://payment-gateway.internal/api/charge?throwExceptionOnFailure=true")
            .log("Payment successfully authorized for order ${header.OrderId}");
    }
}
```

---

## 💳 5. Idempotent Consumers & Distributed Transactions

In distributed systems, duplicate messages arrive during network disconnects or rebalances. The **Idempotent Consumer** pattern deduplicates messages before processing.

```java
package com.example.camel.idempotent;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.processor.idempotent.kafka.KafkaIdempotentRepository;
import org.springframework.stereotype.Component;

@Component
public class IdempotentOrderConsumer extends RouteBuilder {

    @Override
    public void configure() {
        // Kafka-backed distributed Idempotent Repository (replicated across all nodes)
        KafkaIdempotentRepository kafkaRepo = new KafkaIdempotentRepository(
            "order-idempotency-cache",
            "localhost:9092"
        );

        from("kafka:orders-stream?brokers=localhost:9092&groupId=inventory-group")
            .routeId("idempotent-order-processing")
            // Deduplicate based on OrderId header
            .idempotentConsumer(header("OrderId"), kafkaRepo)
                .skipDuplicate(true)
                .log("Processing unique order: ${header.OrderId}")
                .to("direct:reserveInventory")
            .end();
    }
}
```

---

## 🧪 6. Testing Camel Routes with CamelTestSupport & Mocks

Camel routes can be tested in isolation by mocking endpoints and injecting test payloads via `ProducerTemplate`.

```java
package com.example.camel.routes;

import org.apache.camel.EndpointInject;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.apache.camel.test.spring.junit5.MockEndpoints;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.TimeUnit;

@SpringBootTest
@CamelSpringBootTest
@MockEndpoints("kafka:.*") // Intercept all endpoints matching regex with mock:kafka:...
class ResilientPaymentRouteTest {

    @Autowired
    private ProducerTemplate producerTemplate;

    @EndpointInject("mock:kafka:vip-orders")
    private MockEndpoint mockVipOrders;

    @EndpointInject("mock:kafka:standard-orders")
    private MockEndpoint mockStandardOrders;

    @Test
    void shouldRouteVipOrderToVipTopic() throws InterruptedException {
        // Given
        mockVipOrders.expectedMessageCount(1);
        mockStandardOrders.expectedMessageCount(0);
        mockVipOrders.expectedBodyReceived().body().contains("VIP_CUST_99");

        String orderJson = """
            {
                "orderId": "ORD-1001",
                "orderType": "VIP",
                "customerId": "VIP_CUST_99",
                "amount": 450.00
            }
            """;

        // When
        producerTemplate.sendBody("direct:processOrderTest", orderJson);

        // Then
        MockEndpoint.assertIsSatisfied(5, TimeUnit.SECONDS, mockVipOrders, mockStandardOrders);
    }
}
```

---

## 🏭 7. Production Scenarios & War Room Failure Forensics

### Scenario 1: SFTP / File Ingestion Out-of-Memory (OOM) Collapse
- **Root Cause:** Ingesting multi-gigabyte CSV/XML files using `body()` or reading the entire file into Java `String` / `byte[]`.
- **The Fix:** Enable `streaming()` mode on the split EIP with a custom token or line-based reader.

```java
from("file:/var/data/inbound?readLock=changed&move=.processed")
    .routeId("streaming-file-ingest")
    // Stream line by line without buffering 2GB file into heap
    .split(body().tokenize("\n")).streaming()
        .process(new FastLineValidatorProcessor())
        .to("kafka:raw-records?brokers=localhost:9092")
    .end();
```

### Scenario 2: Route Lockup During External REST Microservice Latency Spike
- **Root Cause:** Camel `http` component without strict timeouts causes consumer threads (e.g. SEDA / Kafka) to block indefinitely, triggering thread pool exhaustion.
- **The Fix:** Explicit socket, connect, and connection request timeouts + Resilience4j Circuit Breaker.

```java
from("direct:invokeExternalService")
    .circuitBreaker()
        .resilience4jConfiguration()
            .timeoutEnabled(true)
            .timeoutDuration(2000) // 2 sec timeout
            .slidingWindowSize(20)
            .failureRateThreshold(50.0f)
        .end()
        .to("http://partner-api.com/v1/verify?httpClient.connectTimeout=1000&httpClient.socketTimeout=2000")
    .onFallback()
        .log(LoggingLevel.ERROR, "Partner API unavailable, falling back to cached response")
        .setBody(constant("{\"status\":\"DEGRADED_MODE\"}"))
    .end();
```

---

## ⚖️ 8. Apache Camel 4 Master Cheat Sheet

| Task | Camel Java DSL Syntax |
| :--- | :--- |
| **Log with Headers** | `.log("Processing ID: ${header.Id}, Order: ${body.title}")` |
| **Convert Body Type** | `.convertBodyTo(String.class, "UTF-8")` |
| **JSON Serialization** | `.marshal().json()` / `.unmarshal().json(TargetClass.class)` |
| **Set Custom Header** | `.setHeader("CorrelationId", simple("${exchangeId}"))` |
| **Conditional Filter** | `.filter(simple("${header.Amount} > 1000")).to("direct:audit")` |
| **Wire Tap (Async)** | `.wireTap("seda:asyncAuditLog")` |
| **Parallel Split** | `.split(body()).parallelProcessing().to("direct:worker")` |
| **Circuit Breaker** | `.circuitBreaker().to("http://...").onFallback().to("direct:fallback").end()` |
| **Dead Letter Queue** | `errorHandler(deadLetterChannel("kafka:dlq").maximumRedeliveries(3))` |
| **Dynamic Destination**| `.recipientList(header("TargetQueue"))` |

---
[🏠 Back to Home](README.md)
