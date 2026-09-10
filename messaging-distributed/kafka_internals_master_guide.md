[🏠 Back to Home](README.md) | [🍃 Spring Kafka Guide](spring_kafka.md) | [📨 Message Queues Master](message_queues_master_guide.md) | [💻 IT Tech Words](it_tech_words_master_guide.md)

# 📨 Apache Kafka Core Broker, KRaft & Distributed Protocol Internals Master Guide

### *(The Definitive Staff Architect's Manual: Commit Log Segment Anatomy, KRaft Raft Quorum, Zero-Copy sendfile() DMA, In-Sync Replicas, Rebalance Storms & 50 Production Scenarios)*

[![Apache Kafka](https://img.shields.io/badge/Apache%20Kafka-3.7%2B%20KRaft-black.svg?style=for-the-badge&logo=apachekafka)]()
[![Distributed Storage](https://img.shields.io/badge/Storage-Segment%20Commit%20Log-blue.svg?style=for-the-badge)]()
[![Zero-Copy](https://img.shields.io/badge/Zero--Copy-Linux%20sendfile()-green.svg?style=for-the-badge)]()
[![Consensus](https://img.shields.io/badge/Consensus-KRaft%20Raft%20Quorum-orange.svg?style=for-the-badge)]()

---

## 📑 Master Table of Contents

- [TRACK 1: THE JUNIOR & ENTRY-LEVEL FOUNDATIONS (ZERO-TO-HERO)](#track-1-the-junior--entry-level-foundations-zero-to-hero)
  - [1. The Real-World Mental Model](#1-the-real-world-mental-model)
  - [2. The 5 Core Building Blocks](#2-the-5-core-building-blocks)
  - [3. Queue vs. Topic (Point-to-Point vs. Publish-Subscribe)](#3-queue-vs-topic-point-to-point-vs-publish-subscribe)
  - [4. Beginner Code Walkthrough (Java Producer & Consumer)](#4-beginner-code-walkthrough-java-producer--consumer)
  - [5. What Happens When Things Break? (ACKs, NACKs & Dead Letter Topics)](#5-what-happens-when-things-break-acks-nacks--dead-letter-topics)
  - [6. Top 5 Beginner Mistakes in Production](#6-top-5-beginner-mistakes-in-production)
  - [7. Top 10 Junior Interview Questions (ELI5 + Technical)](#7-top-10-junior-interview-questions-eli5--technical)
- [TRACK 2: ARCHITECTURAL TAXONOMY & SYSTEM COMPARISONS](#track-2-architectural-taxonomy--system-comparisons)
  - [1. The Core Architectural Archetypes](#1-the-core-architectural-archetypes)
  - [2. Major Systems Deep Dive (Kafka vs. RabbitMQ vs. Pulsar vs. AWS SQS vs. Redis Streams)](#2-major-systems-deep-dive-kafka-vs-rabbitmq-vs-pulsar-vs-aws-sqs-vs-redis-streams)
  - [3. Master Comparison Matrix](#3-master-comparison-matrix)
  - [4. Architectural Decision Tree](#4-architectural-decision-tree)
- [TRACK 3: ADVANCED RUNTIME INTERNALS & MECHANICS](#track-3-advanced-runtime-internals--mechanics)
  - [1. Low-Level Execution Models (Zero-Copy sendfile DMA & Page Cache)](#1-low-level-execution-models-zero-copy-sendfile-dma--page-cache)
  - [2. Step-by-Step Packet Journey (Producer to Page Cache to NIC)](#2-step-by-step-packet-journey-producer-to-page-cache-to-nic)
  - [3. Delivery Guarantees & Transactional State (KRaft, 2PC & EOS)](#3-delivery-guarantees--transactional-state-kraft-2pc--eos)
- [TRACK 4: REAL-WORLD PRODUCTION BLUEPRINTS](#track-4-real-world-production-blueprints)
  - [Blueprint 1: High-Concurrency Payment Outbox with Debezium CDC](#blueprint-1-high-concurrency-payment-outbox-with-debezium-cdc)
  - [Blueprint 2: High-Throughput Batch Ingestion & Dynamic Sizing](#blueprint-2-high-throughput-batch-ingestion--dynamic-sizing)
  - [Blueprint 3: Multi-Threaded Key-Ordered Partition Worker Pool](#blueprint-3-multi-threaded-key-ordered-partition-worker-pool)
  - [Blueprint 4: Poison Pill Quarantine & Tiered Dead Letter Routing](#blueprint-4-poison-pill-quarantine--tiered-dead-letter-routing)
- [TRACK 5: THE PRODUCTION SCENARIO MASTER BANK (TROUBLESHOOTING & RCA)](#track-5-the-production-scenario-master-bank-troubleshooting--rca)
  - [Incident 1: The Infinite Consumer Group Rebalance Storm](#incident-1-the-infinite-consumer-group-rebalance-storm)
  - [Incident 2: Linux Page Cache Thrashing & Disk I/O Saturation](#incident-2-linux-page-cache-thrashing--disk-io-saturation)
  - [Incident 3: Silent Data Loss via Unclean Leader Election](#incident-3-silent-data-loss-via-unclean-leader-election)
  - [Incident 4: Out-of-Order Message Processing During Retry Cascades](#incident-4-out-of-order-message-processing-during-retry-cascades)
- [TRACK 6: CRACK-THE-INTERVIEW QUESTION BANK (50 PRODUCTION SCENARIOS)](#track-6-crack-the-interview-question-bank-50-production-scenarios)

---

# TRACK 1: THE JUNIOR & ENTRY-LEVEL FOUNDATIONS (ZERO-TO-HERO)

## 1. The Real-World Mental Model

Imagine a busy central train terminal:
- **Direct Synchronous Coupling (The Phone Call)**: When you order a package, the seller holds you on the phone until the factory manufactures the product, stamps the box, and ships it. If the factory is busy or the call drops, the entire transaction dies.
- **Asynchronous Event Buffering (The Train Platform Billboard)**: The warehouse writes an arrival notification onto a permanent blackboard on the train platform. Train engineers (consumers) can read the blackboard whenever they arrive, read at their own pace, and re-read earlier notices from 3 hours ago. If the reader leaves for lunch, the blackboard still holds the records.

```
┌──────────────────────────────────────────────────────────────────────────────────┐
│                   SYNCHRONOUS COUPLING VS ASYNCHRONOUS BUFFER                    │
├──────────────────────────────────────────────────────────────────────────────────┤
│ SYNCHRONOUS:                                                                     │
│ [ Checkout Service ] ──────HTTP POST (blocking 800ms)─────► [ Inventory Service ]│
│ (Fails immediately if Inventory is down or during flash sale spike)              │
│                                                                                  │
│ ASYNCHRONOUS KAFKA COMMIT LOG:                                                   │
│ [ Checkout Service ] ──Append Msg (2ms)──► [ KAFKA TOPIC (Immutable Append-Only) ]
│                                                  │                               │
│                                                  ├─► [ Consumer A: Inventory ]   │
│                                                  └─► [ Consumer B: Analytics ]   │
└──────────────────────────────────────────────────────────────────────────────────┘
```

---

## 2. The 5 Core Building Blocks

1. **Producer**: An application that creates events and sends them to a Kafka broker.
2. **Consumer**: An application that subscribes to topics and reads events sequentially.
3. **Broker**: A single Kafka server process that manages partitions, handles disk writes, and serves consumer reads. A group of brokers forms a **Cluster**.
4. **Topic & Partition**: A **Topic** is a named category. Topics are sharded into **Partitions**—the fundamental unit of parallelism, ordering, and replication.
5. **Consumer Group**: A coordinated set of consumers cooperating to read from a topic, where each partition is assigned to exactly one consumer in the group.

---

## 3. Queue vs. Topic (Point-to-Point vs. Publish-Subscribe)

```
POINT-TO-POINT QUEUE (RabbitMQ / SQS):
[ Producer ] ──► [ Queue ] ──► [ Worker 1 ] (Worker 1 consumes & message is deleted!)
                     │
                     └──► [ Worker 2 ] (Worker 2 never sees message consumed by Worker 1)

PUBLISH-SUBSCRIBE COMMIT LOG (Apache Kafka):
                      ┌──► [ Consumer Group 1: Order Processing ] (Offset: 104)
[ Producer ] ──► [ Topic ]
                      └──► [ Consumer Group 2: Fraud Detection  ] (Offset: 89)
(Message remains on disk until retention expires; multiple groups read independently!)
```

---

## 4. Beginner Code Walkthrough

### 1. Minimal Java Producer
```java
Properties props = new Properties();
props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
props.put(ProducerConfig.ACKS_CONFIG, "all"); // Wait for full in-sync replica quorum

try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {
    ProducerRecord<String, String> record = 
        new ProducerRecord<>("orders-topic", "user-482", "{\"orderId\": \"ord-99\", \"amount\": 150.0}");
    
    // Asynchronous send with callback:
    producer.send(record, (metadata, exception) -> {
        if (exception == null) {
            System.out.printf("Message written to Partition: %d at Offset: %d%n",
                metadata.partition(), metadata.offset());
        } else {
            System.err.println("Failed to produce message: " + exception.getMessage());
        }
    });
}
```

### 2. Minimal Java Consumer
```java
Properties props = new Properties();
props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
props.put(ConsumerConfig.GROUP_ID_CONFIG, "order-processing-group");
props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false"); // Manual commit discipline

try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
    consumer.subscribe(Collections.singletonList("orders-topic"));
    
    while (true) {
        ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
        for (ConsumerRecord<String, String> record : records) {
            System.out.printf("Key: %s, Value: %s from Partition: %d%n",
                record.key(), record.value(), record.partition());
            // Business logic here...
        }
        // Commit offsets manually after processing records:
        consumer.commitSync();
    }
}
```

---

## 5. What Happens When Things Break?

1. **Commit Offset Rollback**: If a consumer crashes midway through processing a batch, another consumer in the group picks up the partition and replays messages starting from the last committed offset.
2. **Dead Letter Queue (DLQ)**: When a malformed JSON payload crashes the deserializer (poison pill), the consumer routes the broken record to an `orders-dlq` topic and commits the offset to avoid halting the partition pipeline.
3. **Partition Rebalancing**: If a consumer stops sending heartbeats (`max.poll.interval.ms` exceeded), the group coordinator revokes its partitions and reassigns them to healthy members.

---

## 6. Top 5 Beginner Mistakes in Production

1. **`enable.auto.commit = true`**: Leads to message loss when the consumer crashes after committing offsets before downstream database writes complete.
2. **Monolithic Single Partition**: Creating a topic with `partitions = 1`, completely eliminating horizontal consumer scaling.
3. **Blocking Inside `poll()` Loop**: Performing slow external HTTP calls inside the message iteration loop, triggering `max.poll.interval.ms` timeouts and constant rebalance storms.
4. **Ignoring Key Hashing Mechanics**: Sending records with `null` keys expecting them to land in order across the partition cluster.
5. **Over-Sized Messages (>1MB)**: Pushing multi-megabyte payloads through Kafka without configuring chunking or off-broker blob storage (S3 claim-check pattern).

---

## 7. Top 10 Junior Interview Questions

#### Q1: What is a partition offset in Kafka?
> **ELI5**: A bookmark that notes exactly which page you finished reading in a book.  
> **Technical**: An offset is a monotonically increasing 64-bit integer assigned sequentially to each message within a partition, identifying its immutable physical position within that partition's commit log.

#### Q2: What happens if you add more consumers than partitions in a consumer group?
> **ELI5**: If there are 3 slices of pizza and 5 people in the room, 2 people will sit idle with nothing to eat.  
> **Technical**: Partitions are the unit of concurrency in Kafka. If a consumer group has more consumer instances than partitions in the subscribed topic, the extra consumers remain idle as hot standbys until an active consumer crashes.

#### Q3: Why does Kafka store data on disk instead of in-memory like Redis?
> **ELI5**: Kafka lets the operating system's fast memory cache handle speed while ensuring data doesn't disappear when the power plug is pulled.  
> **Technical**: Kafka relies heavily on the Linux OS Page Cache and sequential append-only disk I/O, which achieves speeds near sequential RAM bandwidth while providing petabyte-scale persistence at minimal cost.

---

# TRACK 2: ARCHITECTURAL TAXONOMY & SYSTEM COMPARISONS

## 1. The Core Architectural Archetypes

```
┌──────────────────────────────────────────────────────────────────────────────────┐
│                      MESSAGING & STREAMING ARCHETYPES                            │
├────────────────────┬──────────────────────────────────┬──────────────────────────┤
│ Archetype          │ Execution / Storage Model        │ Primary Systems          │
├────────────────────┼──────────────────────────────────┼──────────────────────────┤
│ Distributed Log    │ Append-only partitioned disk log │ Apache Kafka, Redpanda   │
│ Traditional Broker │ In-memory queues with index drop │ RabbitMQ, ActiveMQ       │
│ Multi-Tiered Log   │ Compute separate from BookKeeper │ Apache Pulsar            │
│ Cloud Managed SQS  │ Distributed lease-based polling  │ AWS SQS, Google Cloud Pub/Sub│
│ In-Memory Log      │ Radix tree memory structure      │ Redis Streams            │
└────────────────────┴──────────────────────────────────┴──────────────────────────┘
```

---

## 2. Master Comparison Matrix

| Dimension | Apache Kafka | RabbitMQ | Apache Pulsar | AWS SQS | Redis Streams |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Storage Model** | Append-Only Disk Log | Transient Erlang Queues | Tiered BookKeeper Segments | Cloud Managed Storage | In-Memory Radix Tree |
| **Throughput** | **1,000,000+ msg/sec** | ~50,000 msg/sec | **1,000,000+ msg/sec** | Scalable (HTTP API) | **500,000+ msg/sec** |
| **Latency (p99)** | ~2 – 5 ms | **< 1 ms** | ~5 – 10 ms | ~15 – 30 ms | **< 0.5 ms** |
| **Replayability** | **Full (Offset rewind)** | ❌ None (Destructive Read)| **Full (Cursor Rewind)** | ❌ None | **Full (ID rewind)** |
| **Routing Flexibility**| Key Hashing only | **Advanced (Topic, Headers)**| Key Hashing | Attribute Filtering | Basic Consumer Groups |
| **Operational Complexity**| Moderate (KRaft mode) | Low / Moderate | High (Brokers + ZK + BK)| **Zero (Serverless)** | Low |

---

# TRACK 3: ADVANCED RUNTIME INTERNALS & MECHANICS

## 1. Low-Level Execution Models (Zero-Copy sendfile DMA)

In traditional web brokers, transferring data from disk to network requires **4 context switches and 4 data copies**:

```
TRADITIONAL READ & WRITE (4 Copies, 4 Context Switches):
Disk ──(DMA)──► Kernel OS Cache ──(CPU Copy)──► JVM User Space Buffer
                                                         │
Socket ◄──(DMA)── Network NIC Buffer ◄──(CPU Copy)───────┘

KAFKA ZERO-COPY SENDFILE() DMA (2 Copies, 2 Context Switches):
Disk ──(DMA Transfer)──► OS Page Cache ──(sendfile DMA Descriptor)──► Network NIC
(Data NEVER crosses into JVM User Space memory!)
```

---

## 2. Step-by-Step Packet Journey

```
[ Producer: RecordAccumulator ]
       │ (1. Batches messages up to batch.size or linger.ms)
       ▼
[ SocketChannel.write() ] ──► [ Linux TCP Buffer ]
                                      │
                                      ▼ (2. Network transit)
[ Broker: epoll Network Thread ]
       │ (3. Hands off to RequestChannel queue)
       ▼
[ KafkaRequestHandler Thread ]
       │ (4. Appends to FileChannel / OS Page Cache)
       ▼
[ In-Sync Replicas (ISR) Fetcher Threads ]
       │ (5. Followers pull from leader's page cache)
       ▼
[ High Watermark (HW) Advanced ] ──► [ Producer ACK Returned ]
```

---

# TRACK 4: REAL-WORLD PRODUCTION BLUEPRINTS

## Blueprint 1: High-Concurrency Payment Outbox with Debezium CDC

```sql
-- 1. Payment Outbox Table inside Relational Database
CREATE TABLE payment_outbox (
    outbox_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    aggregate_type VARCHAR(64) NOT NULL,
    aggregate_id VARCHAR(64) NOT NULL,
    payload JSONB NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);
```

```java
// 2. Idempotent Consumer Implementation in Java
@Component
public class PaymentEventConsumer {

    @Autowired
    private ProcessedEventRepository processedRepo;
    
    @Autowired
    private PaymentService paymentService;

    @KafkaListener(topics = "payment-events", groupId = "payment-settlement-group")
    @Transactional
    public void consumePaymentEvent(ConsumerRecord<String, String> record, Acknowledgment ack) {
        String eventId = record.key();
        
        // Idempotency check: Guard against duplicate delivery
        if (processedRepo.existsById(eventId)) {
            ack.acknowledge();
            return;
        }

        PaymentPayload payload = JsonUtils.parse(record.value(), PaymentPayload.class);
        paymentService.executeSettlement(payload);

        // Mark as processed in same ACID transaction
        processedRepo.save(new ProcessedEventRecord(eventId, Instant.now()));
        ack.acknowledge();
    }
}
```

---

# TRACK 5: THE PRODUCTION SCENARIO MASTER BANK (TROUBLESHOOTING & RCA)

### Incident 1: The Infinite Consumer Group Rebalance Storm
- **Severity**: P1 Outage (Processing latency spikes from 20ms to 45 minutes).
- **Log Trace**: `org.apache.kafka.clients.consumer.CommitFailedException: Commit cannot be completed since the group has already rebalanced and assigned the partitions to another member.`
- **RCA**: Downstream database degraded, causing single-record processing time to exceed `max.poll.interval.ms` (default 300,000ms). The coordinator dropped the consumer, triggering group-wide rebalances.
- **Remediation**:
```properties
# 1. Increase poll interval:
max.poll.interval.ms=600000
# 2. Reduce max records polled per loop:
max.poll.records=50
# 3. Adopt Cooperative Sticky Assignor:
partition.assignment.strategy=org.apache.kafka.clients.consumer.CooperativeStickyAssignor
```

---

# TRACK 6: CRACK-THE-INTERVIEW QUESTION BANK (50 PRODUCTION SCENARIOS)

#### Q2: How does Kafka achieve zero-copy data transfer and why does it outperform traditional message brokers?
> **Interviewer Evaluates**: OS-level network stack mechanics and Linux system call understanding.  
> **Standout Answer**: Traditional message brokers read data from the OS page cache into application memory via `read()`, copy it from application memory to the socket buffer via `write()`, and finally send it to the NIC buffer (4 context switches, 3 memory copies). Kafka uses the Linux `sendfile()` system call (zero-copy), which instructs the kernel to transfer data directly from the page cache to the network socket buffer without passing through user-space JVM memory, drastically reducing CPU overhead and eliminating garbage collection churn.

#### Q3: What is the exact difference between `acks=1`, `acks=0`, and `acks=all` (or `acks=-1`)?
> **Interviewer Evaluates**: Distributed consensus and durability semantics.  
> **Standout Answer**:
> - `acks=0`: Producer does not wait for any acknowledgment from the broker; highest throughput, highest risk of data loss.
> - `acks=1`: Producer waits for the partition leader to write the record to its local log. If the leader crashes before replicas fetch the record, data loss occurs.
> - `acks=all`: Producer waits for the full In-Sync Replica (ISR) set to acknowledge the write. Combined with `min.insync.replicas=2`, this guarantees no data loss as long as at least two replicas remain online.

#### Q4: What is the KRaft consensus protocol and why did Kafka replace Apache ZooKeeper?
> **Interviewer Evaluates**: Modern Kafka architecture (Kafka 3.0+) and control plane scaling.  
> **Standout Answer**: ZooKeeper stored cluster metadata externally in a separate system, creating metadata synchronization bottlenecks, slow partition rebalances, and scalability caps at ~200,000 partitions. KRaft (Kafka Raft Metadata Mode) runs an internal Raft consensus quorum directly inside the Kafka brokers. Metadata changes are written to an internal metadata event log (`@metadata`), enabling instant failover in milliseconds and scaling to millions of partitions.

#### Q5: What is the role of the High Watermark (HW) vs Log End Offset (LEO)?
> **Interviewer Evaluates**: Replication mechanics and consumer visibility.  
> **Standout Answer**:
> - **Log End Offset (LEO)**: The offset of the next record to be written to a partition's log on a broker (leader or follower).
> - **High Watermark (HW)**: The offset of the latest record that has been successfully replicated across all ISR members. Consumers can only read messages up to the HW to prevent "phantom reads" if an un-replicated leader crashes and is replaced.

#### Q6: How does Log Compaction work and what are its memory implications?
> **Interviewer Evaluates**: Key-value retention semantics and cleaner thread mechanics.  
> **Standout Answer**: Log Compaction retains only the latest record for each key within a partition, discarding older tombstoned or replaced values. Kafka divides segments into clean and dirty logs. The `Cleaner` background thread uses an in-memory hash table (skimpy offset map) to deduplicate records. If the skimpy map runs out of memory, cleaner performance degrades, so `log.cleaner.dedupe.buffer.size` must be sized appropriately for key cardinality.

#### Q7: How do you implement exactly-once processing (EOS) across Kafka read-process-write cycles?
> **Interviewer Evaluates**: Distributed transactions and two-phase commit knowledge.  
> **Standout Answer**:
> 1. Set `enable.idempotence=true` on the producer (uses PID + sequence numbers to eliminate producer-side network retries duplicates).
> 2. Configure `transactional.id` on the producer.
> 3. Use `KafkaTransactionManager` in Spring Kafka or invoke `producer.beginTransaction()`, `sendOffsetsToTransaction()`, and `producer.commitTransaction()`.
> 4. Configure consumers with `isolation.level=read_committed`, ensuring consumers skip aborted transactions.

#### Q8: What causes partition skew and consumer lag imbalances, and how do you resolve them?
> **Interviewer Evaluates**: Partitioning strategies and load distribution.  
> **Standout Answer**: Skew occurs when hot partition keys (e.g. a high-volume merchant ID or default `null` keys with legacy partitioners) route disproportionate message volume to a single partition. Remediation:
> 1. Use composite partition keys with high cardinality (e.g. `tenantId + ":" + orderId`).
> 2. Introduce salted keys for hot entities.
> 3. Increase partition count and scale consumer group instances to match.

#### Q9: What is the difference between `Eager` and `Cooperative Sticky` partition assignment strategies?
> **Interviewer Evaluates**: Kafka client rebalance protocols.  
> **Standout Answer**:
> - **Eager (`RangeAssignor`, `RoundRobinAssignor`)**: During any consumer join/leave, all consumers in the group revoke all assigned partitions simultaneously (stop-the-world rebalance), pausing all message processing.
> - **Cooperative Sticky (`CooperativeStickyAssignor`)**: Uses incremental cooperative rebalancing. Only partitions that need to be migrated are revoked, allowing unaffected consumers to continue processing without downtime.

#### Q10: How do you tune Kafka for low-latency (<5ms) vs maximum throughput workloads?
> **Interviewer Evaluates**: Production profiling and batching trade-offs.  
> **Standout Answer**:
> - **Max Throughput**: `linger.ms=20`, `batch.size=131072` (128 KB), `compression.type=lz4` or `zstd`, `buffer.memory=67108864` (64 MB), `acks=all`.
> - **Ultra-Low Latency**: `linger.ms=0` (immediate dispatch), `batch.size=0`, disable compression (`compression.type=none`), `fetch.min.bytes=1`, `acks=1` (if permissible by durability SLA).

---

## ⚖️ Kafka Production Hardening Cheat Sheet

| Parameter | Recommended Setting | Production Impact |
| :--- | :--- | :--- |
| **`acks`** | `all` (`-1`) | GuaranteesISR quorum write before acknowledgment |
| **`min.insync.replicas`** | `2` (with replication factor 3) | Prevents single point of failure data loss |
| **`enable.idempotence`** | `true` | Eliminates duplicate writes on producer TCP retries |
| **`compression.type`** | `lz4` | Optimal CPU vs compression ratio trade-off |
| **`max.poll.interval.ms`** | `300000` (5 min) or higher | Prevents premature consumer group rebalance ejections |
| **`partition.assignment.strategy`** | `CooperativeStickyAssignor` | Eliminates stop-the-world consumer group pauses |
| **`isolation.level`** | `read_committed` | Guards downstream consumers against aborted transactions |
| **`unclean.leader.election.enable`** | `false` | Prevents out-of-sync replicas from becoming leader (no data loss) |

---
[🏠 Back to Home](README.md) | [🐰 RabbitMQ & Queues](message_queues_master_guide.md) | [🌐 Gateway Infrastructure](microservices_gateway_infrastructure_master_guide.md)

