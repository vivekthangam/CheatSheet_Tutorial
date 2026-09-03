[🏠 Back to Home](README.md)

# 📬 Spring for Apache Kafka Enterprise Messaging Master Guide

A production-grade engineering handbook for architecting high-throughput, event-driven microservices using **Spring for Apache Kafka**, **Spring Boot 3.x**, and **Java 17/21**. Covers producer resilience, consumer concurrency, manual acknowledgments, Dead Letter Topics (DLT), transactions, and poison-pill remediation.

---

## 📑 Table of Contents
1. [🧠 Zero-to-Hero Mental Model: The Distributed Commit Log](#-zero-to-hero-mental-model-the-distributed-commit-log)
2. [⚙️ 1. High-Performance Producer Architecture & Idempotence](#️-1-high-performance-producer-architecture--idempotence)
3. [📥 2. Consumer Concurrency, Partitions & Manual Acknowledgments](#-2-consumer-concurrency-partitions--manual-acknowledgments)
4. [🛡️ 3. Fault Tolerance: Retries & Dead Letter Topic (DLT) Recoverer](#️-3-fault-tolerance-retries--dead-letter-topic-dlt-recoverer)
5. [🔄 4. Exactly-Once Semantics (EOS) & Kafka Transactions](#-4-exactly-once-semantics-eos--kafka-transactions)
6. [📦 5. Serialization, Deserialization & Poison Pill Defense](#-5-serialization-deserialization--poison-pill-defense)
7. [🏭 6. Production Scenarios & War Room Incident Forensics](#-6-production-scenarios--war-room-incident-forensics)
8. [⚖️ 7. Spring Kafka Master Cheat Sheet](#️-7-spring-kafka-master-cheat-sheet)

---

## 🧠 Zero-to-Hero Mental Model: The Distributed Commit Log

```
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                                 APACHE KAFKA TOPIC (orders-v1)                         │
│                                                                                        │
│   Partition 0: [ 0 | 1 | 2 | 3 | 4 | 5 ] ──> Consumer Thread 1 (Pod A)                │
│   Partition 1: [ 0 | 1 | 2 | 3 ]         ──> Consumer Thread 2 (Pod A)                 │
│   Partition 2: [ 0 | 1 | 2 | 3 | 4 ]     ──> Consumer Thread 1 (Pod B)                 │
│                                                                                        │
│   Incoming Producers:                                                                  │
│   KafkaTemplate.send("orders-v1", orderId, payload)                                    │
│   - Consistent Hash(orderId) guarantees same order always hits same partition!         │
│   - Preserves strict per-key ordering!                                                 │
└────────────────────────────────────────────────────────────────────────────────────────┘
```

1. **`Topic & Partitions`:** Partitions are the unit of parallelism. 1 partition can only be read by 1 consumer thread in a consumer group at any given time.
2. **`Message Key`:** Used by the partitioner. Records with the same key always land in the same partition, guaranteeing chronological order.
3. **`Offset`:** Sequential sequence number assigned to each record. Consumers track progress by committing offsets.

---

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
