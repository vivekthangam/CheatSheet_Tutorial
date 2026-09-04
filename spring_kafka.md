[🏠 Back to Home](README.md)

# 📬 Spring for Apache Kafka Enterprise Messaging Master Guide

A production-grade engineering handbook for architecting high-throughput, event-driven microservices using **Spring for Apache Kafka**, **Spring Boot 3.x**, and **Java 17/21**. Covers producer resilience, consumer concurrency, manual acknowledgments, Dead Letter Topics (DLT), transactions, and poison-pill remediation.

---

## 📑 Table of Contents

### 🟢 Track 1: Junior & Entry-Level Foundations
1. [🧠 The Real-World Mental Model (The Infinite Cassette Tape & Airport Baggage Belts)](#1-the-real-world-mental-model-the-infinite-cassette-tape--airport-baggage-belts)
2. [🧱 The 5 Core Building Blocks](#2-the-5-core-building-blocks)
3. [💻 Beginner Code Walkthrough: Clean Spring Producer & Consumer](#3-beginner-code-walkthrough-clean-spring-producer--consumer)
4. [💥 What Happens When Things Break? (Top 3 Disasters)](#4-what-happens-when-things-break-top-3-disasters)
5. [⚠️ Top 5 Beginner Mistakes in Production](#5-top-5-beginner-mistakes-in-production)
6. [🎯 Top 10 Junior Interview Questions (With "Explain Like I'm 5" Answers)](#6-top-10-junior-interview-questions-with-explain-like-im-5-answers)

### 🔴 Track 2: Advanced Architecture & Production Engineering
1. [⚙️ 1. High-Performance Producer Architecture & Idempotence](#️-1-high-performance-producer-architecture--idempotence)
2. [📥 2. Consumer Concurrency, Partitions & Manual Acknowledgments](#-2-consumer-concurrency-partitions--manual-acknowledgments)
3. [🛡️ 3. Fault Tolerance: Retries & Dead Letter Topic (DLT) Recoverer](#️-3-fault-tolerance-retries--dead-letter-topic-dlt-recoverer)
4. [🔄 4. Exactly-Once Semantics (EOS) & Kafka Transactions](#-4-exactly-once-semantics-eos--kafka-transactions)
5. [📦 5. Serialization, Deserialization & Poison Pill Defense](#-5-serialization-deserialization--poison-pill-defense)
6. [🏭 6. Production Scenarios & War Room Incident Forensics](#-6-production-scenarios--war-room-incident-forensics)
7. [⚖️ 7. Spring Kafka Master Cheat Sheet](#️-7-spring-kafka-master-cheat-sheet)

---

---

# TRACK 1: THE JUNIOR & ENTRY-LEVEL FOUNDATIONS (ZERO-TO-HERO)

## 1. The Real-World Mental Model (The Infinite Cassette Tape & Airport Baggage Belts)

### How Kafka Differs from Traditional Queues (RabbitMQ)
- **Traditional Queue (RabbitMQ / The Eraser Board):** Messages are put into a box. When a worker reads a message, the worker **erases it from the board**. If a second worker comes along 5 minutes later, the message is gone forever.
- **Apache Kafka (The Infinite Cassette Tape / The Stone Carving):**
  - Kafka does **NOT delete messages when they are read**!
  - Kafka is an append-only commit log recorded on disk, like an infinite cassette tape.
  - When you read a message, you just move your finger (your **Offset**) along the tape.
  - 10 different applications (Payment, Analytics, Fraud Detection) can all read the exact same tape independently at their own speed without interfering with each other!
  - If your analytics service crashes, you simply rewind your offset finger back 1 hour and replay the events!

---

### Partitions: The Airport Baggage Conveyor Belts
Imagine an airport baggage claim with 3 conveyor belts:
- If 10,000 bags arrive, putting all bags onto 1 belt causes a massive human traffic jam!
- Instead, the airport sorts bags by **Passenger Ticket ID (Message Key)**:
  - Belt 0: Tickets A–H.
  - Belt 1: Tickets I–P.
  - Belt 2: Tickets Q–Z.
- **Key Rule:** All bags for the *same customer* always arrive on the *same belt in exact chronological order*. 3 workers can unload bags simultaneously!

```
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                                 APACHE KAFKA TOPIC (orders-v1)                         │
│                                                                                        │
│   Partition 0: [ 0 | 1 | 2 | 3 | 4 | 5 ] ──► Consumer Thread 1 (Pod A)                │
│   Partition 1: [ 0 | 1 | 2 | 3 ]         ──► Consumer Thread 2 (Pod A)                 │
│   Partition 2: [ 0 | 1 | 2 | 3 | 4 ]     ──► Consumer Thread 1 (Pod B)                 │
│                                                                                        │
│   Incoming Producers:                                                                  │
│   KafkaTemplate.send("orders-v1", orderId, payload)                                    │
│   - Consistent Hash(orderId) guarantees same order always hits same partition!         │
│   - Preserves strict per-key ordering!                                                 │
└────────────────────────────────────────────────────────────────────────────────────────┘
```

---

## 2. The 5 Core Building Blocks

| Term | What It Means | Real-World Analogy |
| :--- | :--- | :--- |
| **Topic** | A logical category or stream name (e.g. `orders`, `payments`). | A dedicated TV channel or newspaper section. |
| **Partition** | The physical ordered commit log file on disk; unit of parallelism. | Individual lanes on a multi-lane highway. |
| **Offset** | A sequential ID assigned to each record in a partition (0, 1, 2, 3...). | A bookmark page number in a book. |
| **`KafkaTemplate`** | Spring's high-level helper to publish events to topics. | The post office drop box where you deposit outgoing letters. |
| **`@KafkaListener`** | Spring annotation that continuously polls and processes messages. | An eager worker waiting at the conveyor belt to pick up boxes. |

---

## 3. Beginner Code Walkthrough: Clean Spring Producer & Consumer

### Step 1: Producing Events (`OrderProducer.java`)
```java
package com.example.kafka.producer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class OrderProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public OrderProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishOrder(String orderId, String jsonPayload) {
        // 🌟 Passing orderId as the KEY guarantees all updates for this order 
        // land on the EXACT SAME partition in strict chronological order!
        kafkaTemplate.send("orders-v1", orderId, jsonPayload)
            .whenComplete((result, ex) -> {
                if (ex == null) {
                    System.out.println("✅ Sent order " + orderId + " to partition " 
                        + result.getRecordMetadata().partition() + " at offset " 
                        + result.getRecordMetadata().offset());
                } else {
                    System.err.println("❌ Failed to publish order: " + ex.getMessage());
                }
            });
    }
}
```

### Step 2: Consuming Events (`OrderConsumer.java`)
```java
package com.example.kafka.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class OrderConsumer {

    // groupId ensures load-balancing across instances of your service!
    @KafkaListener(topics = "orders-v1", groupId = "order-fulfillment-group")
    public void handleOrder(String message, Acknowledgment ack) {
        try {
            System.out.println("📦 Processing incoming order: " + message);
            // Execute business logic (e.g. charge card, send email)
            
            // Commit offset to Kafka only AFTER successful processing!
            ack.acknowledge();
        } catch (Exception e) {
            System.err.println("💥 Processing error, will not commit offset!");
            throw e; // Triggers Spring Kafka retry & DLT recovery!
        }
    }
}
```

---

## 4. What Happens When Things Break? (Top 3 Disasters)

1. **The Poison Pill Deserialization Loop:**
   A producer accidentally sends bad JSON or XML into a topic expecting a Java class. The consumer's Jackson deserializer crashes with `SerializationException` **before your `@KafkaListener` code even runs**! Kafka rolls back, re-fetches the same bad record, crashes again, and loops forever, pinning CPU at 100%! **Fix:** Use Spring's `ErrorHandlingDeserializer`.
2. **Consumer Group Rebalance Storm:**
   A consumer takes 60 seconds to process a large batch, exceeding `max.poll.interval.ms` (default: 5 minutes, or configured lower). The Kafka broker assumes the consumer is dead, kicks it out, and triggers a cluster-wide **Rebalance**, halting consumption across all pods!
3. **Consumer Lag Explosion:**
   Producers write 5,000 messages/sec, but consumers can only process 500 messages/sec. The Consumer Lag (unread message backlog) grows by millions, delaying operations by hours. **Fix:** Increase partition count and add consumer pods.

---

## 5. Top 5 Beginner Mistakes in Production

1. **Publishing Messages Without a Key (`kafkaTemplate.send(topic, value)`):** When the key is `null`, Kafka distributes records in a round-robin fashion across partitions. Order update #2 can reach Partition 1 and be processed *before* Order creation #1 on Partition 0! **Fix:** Always provide an entity business key (e.g. `orderId`, `userId`).
2. **Adding More Consumers than Partitions:** If your topic has 3 partitions, and you spin up 10 Spring Boot pods in the same consumer group, **7 pods will sit 100% idle doing zero work**! Kafka enforces a maximum of 1 consumer thread per partition in a consumer group.
3. **Leaving Auto-Commit Enabled (`enable-auto-commit: true`):** The consumer automatically commits offsets every 5 seconds regardless of whether your code finished processing. If the pod crashes midway through saving to the database, the message is permanently lost! **Fix:** Use `AckMode.MANUAL_IMMEDIATE`.
4. **Blocking the Listener Thread with Synchronous Work:** Doing heavy calculations or long sleeps inside `@KafkaListener`. This delays the next `poll()`, causing Kafka brokers to think the node died and triggering rebalances.
5. **Not Having a Dead Letter Topic (DLT):** Letting failed retries block the partition forever. After 3 retries, failed messages should be routed to `orders-v1.DLT` so normal traffic continues moving.

---

## 6. Top 10 Junior Interview Questions (With "Explain Like I'm 5" Answers)

### Q1: What is Apache Kafka and why is it called a distributed commit log?
- **ELI5 Answer:** *"An indestructible cassette tape that records every event that ever happened in your company in the exact order it occurred, and never erases anything."*
- **Technical Answer:** *"Kafka is an open-source distributed event streaming platform built as an append-only commit log on disk. Producers append immutable records to the end of partition logs, and consumers read logs sequentially using position pointers called offsets."*

### Q2: What is a Partition and why does Kafka use partitions?
- **ELI5 Answer:** *"Dividing a 1-lane highway into a 10-lane superhighway so 10 cars can drive side-by-side at the same time."*
- **Technical Answer:** *"A partition is the physical unit of scalability and parallelism in Kafka. A topic is split across multiple partitions distributed across broker nodes. Each partition is strictly ordered and can be consumed by at most one consumer thread within a consumer group."*

### Q3: What is a Consumer Group?
- **ELI5 Answer:** *"A team of workers splitting up a giant pile of chores so no two people do the exact same chore twice."*
- **Technical Answer:** *"A consumer group is a set of consumers cooperating to consume data from a topic. Kafka assigns each partition to exactly one consumer in the group, enabling horizontal scale-out of consumption. Multiple different consumer groups can read the same topic independently."*

### Q4: How does Kafka guarantee message ordering?
- **ELI5 Answer:** *"By writing the same person's name on all their envelopes. All letters with the name 'Alice' go into Alice's personal mailbox in the exact order they were sent."*
- **Technical Answer:** *"Kafka guarantees total ordering **within a single partition**, but NOT across different partitions. By providing a message key, Kafka's default murmur2 partitioner hashes the key to deterministically map all messages with that key to the same partition."*

### Q5: What is an Offset in Kafka?
- **ELI5 Answer:** *"A bookmark page number that tells you where you stopped reading in your book before you went to sleep."*
- **Technical Answer:** *"An offset is a monotonically increasing 64-bit integer assigned to each record as it is written to a partition. Consumers track their progress by committing their current offset back to the internal `__consumer_offsets` topic."*

### Q6: What is the difference between `ack=0`, `ack=1`, and `ack=all` (`-1`)?
- **ELI5 Answer:** *"`ack=0` is throwing a letter out the window and hoping it lands in the mailbox. `ack=1` is waiting for the mailman to nod. `ack=all` is waiting until 3 different postal supervisors sign a receipt!"*
- **Technical Answer:** *"`acks=0` (fire-and-forget; highest speed, highest data loss risk). `acks=1` (producer waits for partition leader to write to local disk). `acks=all` / `-1` (producer waits for leader and all In-Sync Replicas (ISR) to commit, guaranteeing zero data loss)."*

### Q7: What is a Rebalance in a Consumer Group?
- **ELI5 Answer:** *"When a worker leaves early or a new worker joins the shift, the manager pauses work for 2 seconds to re-assign conveyor belts fairly to everyone."*
- **Technical Answer:** *"A rebalance occurs when consumers join, leave, or crash, or when new partitions are added. The Group Coordinator redistributes partition ownership among the currently active members of the group."*

### Q8: What is a Poison Pill message and how do you handle it?
- **ELI5 Answer:** *"A jagged rock hidden inside a bag of flour that breaks the baker's mixing machine every time they turn it on."*
- **Technical Answer:** *"A poison pill is a malformed message (e.g. invalid JSON) that consistently throws an unhandled exception during deserialization or processing. Without an `ErrorHandlingDeserializer` and DLT (Dead Letter Topic), the consumer will endlessly retry the same failed record, freezing the entire partition."*

### Q9: What is Consumer Lag?
- **ELI5 Answer:** *"The number of unread emails sitting in your inbox that you haven't opened yet."*
- **Technical Answer:** *"Consumer lag is the numerical difference between the latest offset produced to a partition (Log End Offset / LEO) and the last offset committed by the consumer group. High or growing lag indicates consumers cannot keep up with write throughput."*

### Q10: What is Idempotent Producer in Kafka?
- **ELI5 Answer:** *"A stamp machine that checks if an envelope was already stamped so it never stamps the exact same letter twice even if the machine hiccups."*
- **Technical Answer:** *"Enabled via `enable.idempotence=true`, the broker assigns each producer a unique Producer ID (PID) and sequence numbers to every message. If a network retry occurs, the broker detects duplicate sequence numbers and discards duplicates, ensuring exactly-once delivery per partition."*

---

# TRACK 2: ADVANCED ARCHITECTURE & HIGH-THROUGHPUT STREAMING

## ⚙️ 1. High-Performance Producer Architecture & Idempotence

### Maven Configuration (`pom.xml`)
```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.kafka</groupId>
        <artifactId>spring-kafka</artifactId>
    </dependency>
</dependencies>
```

### Production `application.yml` Producer Properties
```yaml
spring:
  kafka:
    bootstrap-servers: kafka-broker1:9092,kafka-broker2:9092,kafka-broker3:9092
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
      acks: all                   # Wait for leader AND all in-sync replicas (ISR) to acknowledge
      retries: 10
      properties:
        enable.idempotence: true  # Prevents duplicate messages on network retries
        max.in.flight.requests.per.connection: 5
        compression.type: zstd    # High-ratio compression reducing network bandwidth
        linger.ms: 20             # Micro-batching: wait up to 20ms to group records
        batch.size: 65536         # 64 KB batch buffer
```

### Asynchronous Producer with CompletableFuture Callbacks
```java
package com.example.kafka.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class OrderEventProducer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventProducer.class);
    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    public OrderEventProducer(KafkaTemplate<String, OrderEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public record OrderEvent(String orderId, String customerId, double totalAmount) {}

    public CompletableFuture<SendResult<String, OrderEvent>> sendOrderEvent(OrderEvent event) {
        return kafkaTemplate.send("orders-v1", event.orderId(), event)
            .whenComplete((result, ex) -> {
                if (ex == null) {
                    var metadata = result.getRecordMetadata();
                    log.info("Produced order [{}] to partition [{}] at offset [{}]",
                        event.orderId(), metadata.partition(), metadata.offset());
                } else {
                    log.error("Failed to deliver order event [{}] to Kafka: {}", event.orderId(), ex.getMessage());
                }
            });
    }
}
```

---

## 📥 2. Consumer Concurrency, Partitions & Manual Acknowledgments

### High-Throughput Manual Acknowledgment Configuration
```yaml
spring:
  kafka:
    consumer:
      group-id: inventory-fulfillment-group
      auto-offset-reset: earliest
      enable-auto-commit: false   # Disable auto-commit to prevent data loss on crashes!
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.ErrorHandlingDeserializer
      properties:
        spring.deserializer.value.delegate.class: org.springframework.kafka.support.serializer.JsonDeserializer
        spring.json.trusted.packages: "com.example.kafka.*"
        max.poll.records: 100
        max.poll.interval.ms: 300000 # 5 minutes max per batch before consumer rebalance
    listener:
      ack-mode: MANUAL_IMMEDIATE # Acknowledge message manually only after business logic completes
      concurrency: 3             # 3 worker threads per pod
```

### Consumer Implementation with Manual ACK
```java
package com.example.kafka.consumer;

import com.example.kafka.producer.OrderEventProducer.OrderEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class OrderFulfillmentConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderFulfillmentConsumer.class);

    @KafkaListener(topics = "orders-v1", groupId = "inventory-fulfillment-group")
    public void consume(
            @Payload OrderEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment ack) {

        try {
            log.info("Received order [{}] from partition {} offset {}", event.orderId(), partition, offset);
            
            // Execute business logic (e.g. reserve inventory)
            processOrderFulfillment(event);

            // Commit offset to broker only after successful execution
            ack.acknowledge();

        } catch (Exception ex) {
            log.error("Failed to process order [{}]. Re-throwing to trigger ErrorHandler DLT", event.orderId(), ex);
            throw ex;
        }
    }

    private void processOrderFulfillment(OrderEvent event) {
        // Business logic
    }
}
```

---

## 🛡️ 3. Fault Tolerance: Retries & Dead Letter Topic (DLT) Recoverer

When transient exceptions occur (e.g., downstream microservice down), retry 3 times with exponential backoff. If exhausted, publish to a **Dead Letter Topic (`orders-v1.DLT`)**.

```java
package com.example.kafka.config;

import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.ExponentialBackOff;

@Configuration
public class KafkaFaultToleranceConfig {

    private static final Logger log = LoggerFactory.getLogger(KafkaFaultToleranceConfig.class);

    @Bean
    public CommonErrorHandler commonErrorHandler(KafkaOperations<Object, Object> template) {
        // Publishes exhausted failed records to <originalTopic>.DLT
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(template,
            (record, ex) -> {
                log.warn("Moving exhausted record [key={}] to DLT due to: {}", record.key(), ex.getMessage());
                return new TopicPartition(record.topic() + ".DLT", record.partition());
            });

        // 3 retries with exponential backoff: 1s, 2s, 4s
        ExponentialBackOff backOff = new ExponentialBackOff(1000L, 2.0);
        backOff.setMaxElapsedTime(10000L);

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, backOff);

        // Do not retry fatal business validation errors; send directly to DLT!
        errorHandler.addNotRetryableExceptions(IllegalArgumentException.class);

        return errorHandler;
    }
}
```

---

## 🔄 4. Exactly-Once Semantics (EOS) & Kafka Transactions

Guarantees that consumed messages, database mutations, and produced downstream messages either **all commit or all roll back** together.

```java
@Service
public class TransactionalPaymentProcessor {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final PaymentLedgerRepository ledgerRepository;

    public TransactionalPaymentProcessor(KafkaTemplate<String, Object> kafkaTemplate,
                                        PaymentLedgerRepository ledgerRepository) {
        this.kafkaTemplate = kafkaTemplate;
        this.ledgerRepository = ledgerRepository;
    }

    @KafkaListener(topics = "inbound-payments", groupId = "clearing-house")
    @Transactional("kafkaTransactionManager")
    public void processPayment(PaymentRequest request) {
        // 1. Mutate Database
        ledgerRepository.save(new LedgerEntry(request.id(), request.amount()));

        // 2. Publish to outbound Kafka topic
        kafkaTemplate.send("settled-payments", request.id(), new SettlementEvent(request.id(), "SETTLED"));

        // If an exception occurs, both the DB insert and the Kafka message abort!
    }
}
```

---

## 📦 5. Serialization, Deserialization & Poison Pill Defense

### The "Poison Pill" Problem
If a producer writes an invalid JSON payload or non-deserializable class to a topic, standard Spring Kafka consumers crash inside the deserializer *before* reaching `@KafkaListener`. On restart, they read the exact same corrupt record, entering an **infinite crash loop**.

### The Defense: `ErrorHandlingDeserializer`
Wraps the actual deserializer. If deserialization fails, it catches the error and passes a `DeserializationException` header to the listener so the `DefaultErrorHandler` can route it straight to the DLT!

```yaml
spring:
  kafka:
    consumer:
      value-deserializer: org.springframework.kafka.support.serializer.ErrorHandlingDeserializer
      properties:
        spring.deserializer.value.delegate.class: org.springframework.kafka.support.serializer.JsonDeserializer
```

---

## 🏭 6. Production Scenarios & War Room Incident Forensics

### Scenario 1: Rebalance Storm Caused by Slow Consumer Processing
- **Symptom:** Kafka consumer group keeps constantly rebalancing every 5 minutes. Logs show `CommitFailedException: Offset commit cannot be completed since the group has already rebalanced and assigned the partitions to another member`.
- **Root Cause:** A batch of 500 records took 6 minutes to process, exceeding `max.poll.interval.ms` (default: 300,000 ms). The broker marked the consumer dead and revoked its partitions.
- **The Fix:**
  1. Reduce `max.poll.records` to a manageable size (e.g., 50).
  2. Increase `max.poll.interval.ms` to 600,000 ms.
  3. Offload heavy downstream processing to an async worker thread pool.

### Scenario 2: Partition Skew & Starvation
- **Symptom:** 1 pod in a 10-pod cluster has 99% CPU utilization while the other 9 sit idle.
- **Root Cause:** All produced messages used a constant key (e.g. `orderEvent.getTenantId() = "DEFAULT"`), causing Kafka's default murmur2 hash partitioner to dump all traffic onto a single partition.
- **The Fix:** Ensure high-cardinality keys (e.g., `UUID`, `orderId`, `customerId`) are used for message partitioning.

---

## ⚖️ 7. Spring Kafka Master Cheat Sheet

| Task | Configuration / API |
| :--- | :--- |
| **Send Async** | `kafkaTemplate.send("topic", key, payload).whenComplete(...)` |
| **Set Concurrency** | `@KafkaListener(topics = "...", concurrency = "3")` |
| **Manual Commit** | `ack.acknowledge()` with `ack-mode: MANUAL_IMMEDIATE` |
| **Dead Letter Topic** | `new DeadLetterPublishingRecoverer(template)` |
| **Idempotent Producer**| `spring.kafka.producer.properties.enable.idempotence: true` |
| **Kafka Headers** | `@Header(KafkaHeaders.RECEIVED_PARTITION) int partition` |
| **Batch Consumer** | `@KafkaListener(...) public void listen(List<Message> batch)` |

---
[🏠 Back to Home](README.md)
