# Distributed Systems: Apache Kafka Event-Driven Streaming Master Cheatsheet

> **Curriculum Milestone**: Distributed Event Streaming & Messaging Core  
> **Topic Coverage**: Kafka Broker Internals, KRaft Consensus, Producer Idempotence, Exactly-Once Semantics (EOS), Consumer Group Rebalancing (`CooperativeStickyAssignor`), Dead Letter Topics (DLT), High Availability, and Poison Pill Remediation.  
> 🛠️ **Real-World Project Cheatsheet:** [Jump to Attached Payment Event Streaming Cheatsheet](#-real-world-production-project--code-cheatsheet-resilient-payment-event-streaming) | [Companion IDE Project](file:///d:/project/github/1/CheatSheet_Tutorial/code/02_spring_framework/spring_kafka_events/)  
> 🔗 **Flagship Guides:**  
> - [Spring for Apache Kafka & Event-Driven Architecture (2,500+ Lines)](file:///d:/project/github/1/CheatSheet_Tutorial/interview_prep/02_spring_framework/spring_kafka_events.md)  
> - [Distributed Messaging & Event Queues Master Guide (1,500+ Lines)](file:///d:/project/github/1/CheatSheet_Tutorial/interview_prep/06_messaging_distributed/messaging_distributed_systems.md)  
> - [Apache Kafka Architecture, Storage Internals & Stream Engineering](file:///d:/project/github/1/CheatSheet_Tutorial/interview_prep/06_messaging_distributed/kafka_internals.md)

---

## 🛠️ Real-World Production Project & Code Cheatsheet: Resilient Payment Event Streaming

> **Attached Domain Project:** `Mission-Critical Financial Payment Event Streaming Engine` (`com.cheatsheet.kafka`)  
> **Companion Code Mirror:** [`code/02_spring_framework/spring_kafka_events/`](file:///d:/project/github/1/CheatSheet_Tutorial/code/02_spring_framework/spring_kafka_events/)  
> **Key Technologies Demonstrated:** Idempotent Producer, Exactly-Once Semantics (EOS), `CooperativeStickyAssignor`, Manual Immediate Acknowledgment (`AckMode.MANUAL_IMMEDIATE`), `DefaultErrorHandler` with Exponential DLT Routing, Poison-Pill Deserialization Armor, Transactional Outbox Pattern Bridge.

---

### 1. Complete Runnable Topic Code Cheatsheet (Payment Event Streaming)

```java
package com.cheatsheet.kafka.engine;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.stereotype.Service;
import org.springframework.util.backoff.ExponentialBackOff;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PaymentEventStreamingEngine {

    public record PaymentInitiatedEvent(
        String paymentId,
        String sourceAccount,
        String targetAccount,
        BigDecimal amount,
        String currency,
        Instant timestamp
    ) {}

    public record PaymentSettledEvent(String paymentId, String status, Instant settledAt) {}

    @Configuration
    public static class ResilientProducerConfig {
        @Bean
        public ProducerFactory<String, Object> producerFactory() {
            Map<String, Object> props = new HashMap<>();
            props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
            props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
            props.put(ProducerConfig.ACKS_CONFIG, "all");
            props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
            props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);
            props.put(ProducerConfig.RETRIES_CONFIG, Integer.MAX_VALUE);
            props.put(ProducerConfig.BATCH_SIZE_CONFIG, 65536);
            props.put(ProducerConfig.LINGER_MS_CONFIG, 20);
            props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "zstd");
            return new DefaultKafkaProducerFactory<>(props);
        }

        @Bean
        public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> factory) {
            return new KafkaTemplate<>(factory);
        }
    }

    @Configuration
    @EnableKafka
    public static class ResilientConsumerConfig {
        @Bean
        public ConsumerFactory<String, PaymentInitiatedEvent> consumerFactory() {
            Map<String, Object> props = new HashMap<>();
            props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
            props.put(ConsumerConfig.GROUP_ID_CONFIG, "payment-settlement-service");
            props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
            props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
            props.put(ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG,
                    "org.apache.kafka.clients.consumer.CooperativeStickyAssignor");

            props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
            props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
            props.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class.getName());
            props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class.getName());
            props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.cheatsheet.kafka.*");
            return new DefaultKafkaConsumerFactory<>(props);
        }

        @Bean
        public ConcurrentKafkaListenerContainerFactory<String, PaymentInitiatedEvent> kafkaListenerContainerFactory(
                ConsumerFactory<String, PaymentInitiatedEvent> consumerFactory,
                KafkaTemplate<String, Object> kafkaTemplate) {
            var factory = new ConcurrentKafkaListenerContainerFactory<String, PaymentInitiatedEvent>();
            factory.setConsumerFactory(consumerFactory);
            factory.setConcurrency(3);
            factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);

            DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate,
                    (record, ex) -> new TopicPartition(record.topic() + ".DLT", record.partition()));
            ExponentialBackOff backOff = new ExponentialBackOff(1000L, 2.0);
            backOff.setMaxElapsedTime(10000L);
            DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, backOff);
            errorHandler.addNotRetryableExceptions(org.springframework.kafka.support.serializer.DeserializationException.class);
            factory.setCommonErrorHandler(errorHandler);
            return factory;
        }
    }

    @Service
    public static class PaymentProcessingConsumer {
        private final Set<String> processedTransactions = ConcurrentHashMap.newKeySet();
        private final KafkaTemplate<String, Object> kafkaTemplate;

        public PaymentProcessingConsumer(KafkaTemplate<String, Object> kafkaTemplate) {
            this.kafkaTemplate = kafkaTemplate;
        }

        @KafkaListener(topics = "payments.initiated", groupId = "payment-settlement-service")
        public void processPaymentEvent(ConsumerRecord<String, PaymentInitiatedEvent> record, Acknowledgment ack) {
            PaymentInitiatedEvent event = record.value();
            if (!processedTransactions.add(event.paymentId())) {
                ack.acknowledge();
                return;
            }
            kafkaTemplate.send("payments.settled", event.paymentId(),
                    new PaymentSettledEvent(event.paymentId(), "COMPLETED", Instant.now()));
            ack.acknowledge();
        }

        @KafkaListener(topics = "payments.initiated.DLT", groupId = "payment-dlt-monitoring")
        public void processDeadLetterQueue(ConsumerRecord<String, Object> record, Acknowledgment ack) {
            System.err.println("ALARM: Poison Pill routed to DLT from offset: " + record.offset());
            ack.acknowledge();
        }
    }
}
```

---

### 2. Positive vs Negative Execution Paths (Failure Boundaries)

| Pipeline Phase | Positive Path (Staff-Grade Standard) | Negative Path (Chaos & Failure Invariants) | Detection & Automated Recovery Mechanism |
| :--- | :--- | :--- | :--- |
| **Producer Record Dispatch** | `acks=all` waits for quorum of In-Sync Replicas; message committed in $< 5\text{ms}$. | Network partition isolates broker; message pending in `RecordAccumulator`. | Producer retries with exponential backoff up to `delivery.timeout.ms=120s`; throws `TimeoutException` if ISR unreachable. |
| **Consumer Partition Balancing** | Rolling deployment triggers incremental migration via `CooperativeStickyAssignor`. | Eager rebalance causes Stop-The-World pause across all consumers for 45s. | GroupCoordinator only revokes displaced partitions in 2-phase non-blocking handshake; unaffected partitions continue streaming. |
| **Corrupt Payload Ingress** | Well-formed JSON deserialized into typed Java record. | Bad producer emits malformed byte array ("Poison Pill"). | `ErrorHandlingDeserializer` catches parse error before listener; routes directly to `.DLT` without crashing consumer thread. |
| **Long-Running Database I/O** | Downstream processing finishes within 200ms; thread polls next batch. | Downstream DB locks for 350s ($> \text{max.poll.interval.ms}=300\text{s}$). | Heartbeat thread stays alive, but Coordinator evicts consumer from group; logs `CommitFailedException`. |
| **Offset Commit Integrity** | Offsets committed synchronously upon database commit (`AckMode.MANUAL_IMMEDIATE`). | Auto-commit fires every 5s; application crashes while batch half-processed. | Strict manual acknowledgment ensures uncommitted messages are safely re-delivered to another healthy consumer instance. |

---

### 3. Real-World Mistakes, Outage Bugs & Fixes (BugLab)

* **Bug 1: Sequential Blocking I/O Inside Listener Exceeding `max.poll.interval.ms`**  
  * *Root Cause*: Synchronous calls took 500ms per record on 500-record batch = 250s, triggering unexpected consumer group rebalance storms.  
  * *Fix*: Offload batch processing to bounded parallel worker pool with strict per-message timeouts.
* **Bug 2: `acks=all` with Default `min.insync.replicas=1` Losing Data**  
  * *Root Cause*: Leader broker acknowledged single-node write and crashed before replication.  
  * *Fix*: Always enforce `min.insync.replicas=2` on topic/broker level when using `replication.factor=3`.
* **Bug 3: Poison Pill Halting Consumer Group with Infinite Deserialization Loop**  
  * *Root Cause*: Unhandled JSON parse exception threw before user listener code was reached, restarting poll at same offset.  
  * *Fix*: Configure `ErrorHandlingDeserializer` delegating invalid payloads directly to `.DLT`.

---

### 4. Security Vulnerabilities & Mitigations (SecurityLab)

* **Sec 1: Plaintext Transport Sniffing**: Plaintext port 9092 exposes sensitive transaction payloads on internal subnet.  
  * *Mitigation*: Enable mTLS mutual authentication with SASL/SCRAM-SHA-512 over port 9093.
* **Sec 2: Replay Attack on Payment Consumer**: Attacker replays captured transaction event.  
  * *Mitigation*: Validate timestamp skew threshold ($\le 300\text{s}$) and enforce distributed atomic nonce deduplication in Redis (`SETNX`).

---

### 5. Architectural Anti-Patterns & Refactorings (DesignLab)

* **Arch 1: Dual-Write Hazard inside `@Transactional`**: Direct `kafkaTemplate.send()` risks emitting events for transactions that roll back.  
  * *Refactor*: Implement Transactional Outbox Pattern with Debezium CDC for guaranteed atomicity.
* **Arch 2: Unkeyed Messages Creating Out-of-Order Execution**: Null key round-robins across partitions, destroying entity event order.  
  * *Refactor*: Provide deterministic entity identifier as partition key to preserve FIFO order within partition.
