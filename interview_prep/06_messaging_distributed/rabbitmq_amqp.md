# RabbitMQ, AMQP 0-9-1 Protocol & Distributed Broker Architecture Interview Guide

> **Scope**: AMQP 0-9-1 Architecture (Exchanges, Bindings, Queues), Erlang BEAM Runtime & Actor Model, Classic Queues vs Quorum Queues (Raft Consensus) vs Super Streams, Dead Letter Exchanges (DLX) & Retry Topologies, Publisher Confirms & Manual ACKs, Backpressure & Memory Alarms (`vm_memory_high_watermark`), Prefetch Count (`basic.qos`) Tuning, and Mission-Critical War-Room Post-Mortems.

---

## Guide Architecture Overview

```
========================================================================================================================
                                     RABBITMQ & AMQP 0-9-1 BROKER ARCHITECTURE
========================================================================================================================
 [Layer 1: AMQP 0-9-1 Protocol & Exchange Topologies] --> Direct, Fanout, Topic (Wildcards), Headers, Binding Keys
 [Layer 2: Erlang BEAM Runtime & Queue Internals]    --> BEAM Lightweight Processes, Quorum Queues (Raft), Khepri
 [Layer 3: Reliability: Confirms, ACKs & DLX]        --> Publisher Confirms, Manual basic.ack, Dead Letter Exchanges
 [Layer 4: Flow Control, Memory Alarms & QoS Tuning] --> vm_memory_high_watermark, basic.qos Prefetch, Backpressure
 [Layer 5: Ultra-Deep Real-World War-Room Cases]     --> 10 Production Disasters (Unacked Queue Memory Leak, Net-Split)
 [Layer 6: Beginner Mistakes & Anti-Patterns]        --> 8 Fatal Engineering Traps (Auto-ACK Data Loss, Huge Prefetch)
 [Layer 7: Globally Reported Production Incidents]   --> Real Outages (Classic Mirrored Queue Split-Brain Failure)
 [Layer 8: Rapid-Fire Cheat Sheet & Summary Matrix]  --> High-Speed rabbitmqctl Commands, Exchange Complexity Matrix
========================================================================================================================
```

---

# Layer 1: AMQP 0-9-1 Protocol & Exchange Topologies

---

### Scenario 1: AMQP 0-9-1 Protocol Architecture: The 4 Exchange Types
**Interviewer Evaluation:** Assesses mechanical routing logic, binding key evaluation, pattern matching performance, and routing keys.

#### Technical Deep Dive
In the AMQP 0-9-1 model, publishers **never publish directly to queues**; they publish to an **Exchange**, which inspects message metadata and routes copies to bound queues:
1. **Direct Exchange (Exact Match)**:
   - Routes messages to queues whose Binding Key exactly matches the message Routing Key (`payment.processed`).
   - High-speed $O(1)$ hash table lookup.
2. **Fanout Exchange (Broadcast / Publish-Subscribe)**:
   - Ignores Routing Keys completely. Copies and broadcasts the incoming message to **every queue bound to the exchange**.
   - Fastest routing performance ($O(1)$ pointer duplication).
3. **Topic Exchange (Pattern / Wildcard Match)**:
   - Evaluates dot-delimited routing keys against wildcard patterns:
     - `*` (asterisk): Matches **exactly one** word (e.g., `usa.*.orders` matches `usa.east.orders`).
     - `#` (hash): Matches **zero or more** words (e.g., `audit.#` matches `audit.orders.payment.success`).
   - Implemented via Trie data structure in Erlang.
4. **Headers Exchange**:
   - Routes based on key-value pairs in the message headers table rather than routing keys.

```
AMQP 0-9-1 Routing Architecture:
[ Publisher ] ---> (Routing Key: "eu.orders.created")
                         |
                         v
             [ Exchange: Topic (order_events) ]
                         |
           +-------------+-------------+
           | (Binding: "eu.#")         | (Binding: "*.orders.*")
           v                           v
   [ Queue: EU_Audit ]         [ Queue: Order_Processing ]
           |                           |
           v                           v
     [ Consumer A ]              [ Consumer B ]
```

---

# Layer 2: Erlang BEAM Runtime & Quorum Queues (Raft)

---

### Scenario 2: Classic Mirrored Queues vs Modern Quorum Queues (Raft)
**Interviewer Evaluation:** Assesses high-availability consensus, network partition recovery, and the migration from deprecated Mirrored Queues to Raft-based Quorum Queues.

#### Technical Deep Dive
1. **Classic Mirrored Queues (Deprecated & Flawed)**:
   - Synchronized a primary queue across multiple nodes using an ad-hoc master-slave replication protocol.
   - **Vulnerable to Network Partitions**: During network splits, both sides could promote mirrors, causing split-brain data loss.
2. **Quorum Queues (Modern Production Standard)**:
   - Implements the **Raft Consensus Protocol**.
   - A Quorum Queue consists of a Raft leader and followers across an odd number of cluster nodes (e.g., 3 or 5 nodes).
   - Writes require acknowledgment from a **Majority Quorum**:
     $$Q = \left\lfloor \frac{N}{2} \right\rfloor + 1$$
   - For a 3-node cluster, 2 nodes must write the message to disk before acknowledging the publisher. Completely immune to split-brain.

```yaml
# Declaring a Quorum Queue in Java Spring AMQP:
@Bean
public Queue paymentQueue() {
    return QueueBuilder.durable("payment.queue")
        .quorum() // Sets x-queue-type: quorum
        .deliveryLimit(5) // Poison pill circuit breaker: DLX after 5 retries!
        .deadLetterExchange("dlx.exchange")
        .build();
}
```

---

# Layer 3: Reliability, Flow Control & Prefetch Tuning

---

### Scenario 3: Consumer Prefetch Count (`basic.qos`) & Memory Backpressure
**Interviewer Evaluation:** Tests preventing consumer memory starvation, tuning throughput vs latency, and the `basic.qos` prefetch window.

#### Technical Deep Dive
By default, RabbitMQ attempts to push all available queue messages to connected consumers as fast as the network allows (`prefetch = 0` / unbounded):
- **The Disaster**: If a queue contains 200,000 heavy messages, RabbitMQ dumps all 200,000 messages into the consumer's TCP socket buffer. The consumer runs out of heap memory and crashes with `OutOfMemoryError`.
- **The Tuning Rule (`basic.qos(prefetchCount)`)**:
  - Sets the maximum number of unacknowledged messages allowed in flight to a consumer.
  - Sizing Formula:
    $$\text{Optimal Prefetch} = \text{Consumer Worker Threads} \times 2 \times \frac{\text{Round-Trip Latency}}{\text{Processing Time}}$$
  - In practice: Set `prefetch = 10` to `50` for standard tasks; set `prefetch = 1` for heavy multi-second jobs.

---

# Layer 4: Rapid-Fire Cheat Sheet & Interview Summary Matrix

---

### Critical RabbitMQ Operational Directives

| Directive / Parameter | Target Setting | Purpose |
| :--- | :--- | :--- |
| `x-queue-type` | `quorum` | Replaces classic mirrored queues with Raft consensus |
| `basic.qos(prefetchCount)` | `10` to `50` | Prevents overwhelming consumer memory |
| `basic.ack(deliveryTag, false)`| Manual ACK | Mandatory to prevent message loss on crashes |
| `vm_memory_high_watermark` | `0.40` (40% RAM) | Blocks publishers when broker RAM hits 40% |
| `x-delivery-limit` | `3` to `5` | Dead-letters poison messages after $N$ crashes |

---

### The Golden RabbitMQ Architecture Rules
1. **Never use auto-ack (`autoAck = true`)**: Always use manual consumer acknowledgments.
2. **Migrate to Quorum Queues**: Eliminate classic mirrored queues and split-brain risks.
3. **Always tune `basic.qos` prefetch count**: Prevent consumers from buffer-bloating and crashing.
4. **Configure Dead Letter Exchanges (DLX)**: Route poison pill messages out of active processing queues.
5. **Monitor `unacknowledged` message count**: Alert immediately when consumers hoard unacked messages.
