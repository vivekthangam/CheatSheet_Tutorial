# Relational Database Theory, Normalization Forms & ACID Transaction Isolation Interview Guide

> **Scope**: Relational Algebra Foundations, Database Normalization (1NF through BCNF and 5NF), Denormalization Trade-Offs, Deep ACID Properties, Transaction Isolation Levels (ANSI SQL vs Snapshot Isolation), Concurrency Anomalies (Dirty Reads, Phantom Reads, Write Skew, Serialization Anomaly), Two-Phase Locking (2PL), Distributed Transactions (2PC vs Saga), and Production War-Room Post-Mortems.

---

## Guide Architecture Overview

```
========================================================================================================================
                                      DATABASE NORMALIZATION & ACID ISOLATION
========================================================================================================================
 [Layer 1: Normalization Forms: 1NF to BCNF & 5NF]   --> Functional Dependencies, 1NF, 2NF, 3NF, BCNF, Multi-Valued (4NF)
 [Layer 2: ACID Properties & Storage Engine Level]   --> Atomicity (Undo Logs/WAL), Consistency (Invariants), Durability
 [Layer 3: Isolation Levels & Concurrency Anomalies] --> Read Committed, Repeatable Read, Serializable, Snapshot Isolation
 [Layer 4: Distributed Transactions: 2PC vs Saga]   --> Two-Phase Commit (2PC Coordinator Failure), Compensating Sagas
 [Layer 5: Ultra-Deep Real-World War-Room Cases]    --> 10 Production Disasters (Write Skew Account Drain, 2PC Lock Freeze)
 [Layer 6: Beginner Mistakes & Anti-Patterns]       --> 8 Fatal Engineering Traps (Premature Denormalization, Autocommit)
 [Layer 7: Globally Reported Production Incidents]  --> Real Outages (Crypto Exchange Race Condition Double-Spend)
 [Layer 8: Rapid-Fire Cheat Sheet & Summary Matrix] --> Anomaly vs Isolation Level Matrix, Normalization Rules Table
========================================================================================================================
```

---

# Layer 1: Database Normalization: 1NF through BCNF & 5NF

---

### Scenario 1: Functional Dependencies & Normal Forms: 1NF to Boyce-Codd (BCNF)
**Interviewer Evaluation:** Assesses mathematical rigor in relational schema design, eliminating update anomalies, insertion anomalies, and deletion anomalies.

#### Technical Deep Dive
1. **First Normal Form (1NF)**:
   - Every column must contain atomic (indivisible) values. No multi-valued attributes, comma-separated lists, or nested repeating groups.
   - Each row must be uniquely identifiable via a Primary Key.
2. **Second Normal Form (2NF)**:
   - Must be in 1NF.
   - **No Partial Functional Dependencies**: Every non-key attribute must be fully functionally dependent on the entire primary key (applies only to composite primary keys).
   - If key is $\{A, B\}$, you cannot have $A \to C$. $C$ must be split into a separate table with $A$ as primary key.
3. **Third Normal Form (3NF)**:
   - Must be in 2NF.
   - **No Transitive Dependencies**: Non-key attributes must not depend on other non-key attributes ($X \to Y \to Z$).
   - Rule of Thumb: *"Every attribute must depend on the key, the whole key, and nothing but the key, so help me Codd."*
4. **Boyce-Codd Normal Form (BCNF)**:
   - Stricter version of 3NF. For every non-trivial functional dependency $X \to Y$, **$X$ must be a Superkey**.

```
Normalization Evolution:
[ Unnormalized Data ] ---> (Enforce Atomic Attributes) ---> [ 1NF ]
                                                               |
                                            (Eliminate Partial Dependencies)
                                                               v
                                                           [ 2NF ]
                                                               |
                                            (Eliminate Transitive Dependencies)
                                                               v
                                                           [ 3NF ]
                                                               |
                                            (Every Determinant is a Superkey)
                                                               v
                                                           [ BCNF ]
```

---

### Scenario 2: When and How to Denormalize for High-Throughput Read Performance
**Interviewer Evaluation:** Tests understanding of the real-world trade-off between write anomalies vs read latencies at high scale.

#### Technical Deep Dive
- Fully normalized schemas (3NF/BCNF) minimize disk space and prevent data inconsistency on writes, but require multi-table `JOIN` operations that degrade performance on high-QPS read workloads.
- **Intentional Denormalization Patterns**:
  1. **Pre-computed Aggregates**: Storing `total_order_count` directly on `customers` table to avoid `COUNT(*)` across millions of rows.
  2. **Lookup Replication**: Storing `customer_name` directly in `orders` to eliminate a JOIN on high-throughput checkout streams.
  3. **Event-Driven Reconciliation**: When denormalizing, consistency must be maintained via database triggers or asynchronous Change Data Capture (CDC / Debezium) event listeners.

---

# Layer 2: ACID Properties & Transaction Isolation Levels

---

### Scenario 3: The 4 ANSI SQL Isolation Levels vs Concurrency Anomalies
**Interviewer Evaluation:** Assesses exact comprehension of transaction isolation boundaries, locking mechanisms, and race conditions.

#### Technical Deep Dive
ANSI SQL defines four isolation levels to balance concurrency against data consistency:
1. **Read Uncommitted**: Dirty Reads permitted. Sockets read uncommitted in-flight writes of other transactions.
2. **Read Committed (PostgreSQL / Oracle Default)**:
   - Guaranteed no Dirty Reads.
   - Reads only data committed before the individual statement started.
   - **Vulnerable to Non-Repeatable Reads**: Running the same `SELECT` twice within one transaction can return different row values if another transaction committed an update in between.
3. **Repeatable Read (MySQL InnoDB Default)**:
   - Guaranteed no Dirty Reads or Non-Repeatable Reads.
   - Reads data from a snapshot taken at the start of the *transaction*.
4. **Serializable (Strongest)**:
   - Transactions execute with the guarantee that the result is identical to some purely sequential, serial execution order. Eliminates all anomalies.

| Isolation Level | Dirty Read | Non-Repeatable Read | Phantom Read | Write Skew |
| :--- | :---: | :---: | :---: | :---: |
| **Read Uncommitted** | ❌ Allowed | ❌ Allowed | ❌ Allowed | ❌ Allowed |
| **Read Committed** | ✅ Prevented | ❌ Allowed | ❌ Allowed | ❌ Allowed |
| **Repeatable Read** | ✅ Prevented | ✅ Prevented | ⚠️ Engine Dependent | ❌ Allowed |
| **Serializable** | ✅ Prevented | ✅ Prevented | ✅ Prevented | ✅ Prevented |

---

### Scenario 4: The Snapshot Isolation (SI) "Write Skew" Anomaly
**Interviewer Evaluation:** Tests deep understanding of Snapshot Isolation, MVCC, and why Repeatable Read fails to protect against Write Skew.

#### Technical Deep Dive
- **Write Skew**: Occurs under Snapshot Isolation when two concurrent transactions read overlapping data, make disjoint modifications based on the read data, and commit without lock contention, violating a system invariant.
- **The On-Call Doctors Scenario**:
  - Invariant: *At least one doctor must remain on call at all times.*
  - Doctors Alice and Bob are currently on call (Count = 2).
  - Transaction 1 (Alice requests off): Checks count ($2 \ge 2$). Updates Alice status to Off.
  - Transaction 2 (Bob requests off): Concurrent snapshot checks count ($2 \ge 2$). Updates Bob status to Off.
  - Both transactions commit successfully under Repeatable Read because they modified **different rows**!
  - **Result**: Zero doctors are on call! Invariant violated.
  - *Fix*: Use `SELECT ... FOR UPDATE` (explicit row locking) or elevate to true **Serializable Isolation** (Serializable Snapshot Isolation - SSI).

```
Write Skew Execution Timeline:
Initial State: Doctor Alice (On-Call), Doctor Bob (On-Call) -> Invariant: >= 1 on call
--------------------------------------------------------------------------------------
Transaction 1 (Alice)                         Transaction 2 (Bob)
[ BEGIN (Repeatable Read) ]                   [ BEGIN (Repeatable Read) ]
SELECT COUNT(*) -> Returns 2                  SELECT COUNT(*) -> Returns 2
UPDATE Alice SET on_call = FALSE              UPDATE Bob SET on_call = FALSE
[ COMMIT ] (Success!)                         [ COMMIT ] (Success!)
--------------------------------------------------------------------------------------
Final State: Count = 0! (INVARIANT DESTROYED!)
```

---

# Layer 3: Distributed Transactions: Two-Phase Commit vs Saga

---

### Scenario 5: Two-Phase Commit (2PC) vs Compensating Saga Pattern
**Interviewer Evaluation:** Evaluates microservice data consistency, coordinator single-point-of-failure in 2PC, and asynchronous eventual consistency.

#### Technical Deep Dive
In microservices architectures spanning multiple independent databases, distributed transactions cannot use native database locks:
1. **Two-Phase Commit (2PC)**:
   - **Phase 1 (Prepare)**: Coordinator asks all participating databases: *"Can you commit?"* Nodes acquire local locks, prepare changes, and vote `YES` or `NO`.
   - **Phase 2 (Commit)**: If all vote `YES`, coordinator broadcasts `COMMIT`. If any node votes `NO`, broadcasts `ROLLBACK`.
   - **The Fatal Flaw**: If the Coordinator crashes during Phase 2, participating databases are left in an **in-doubt blocked state**, holding database locks indefinitely and stalling the entire system.
2. **Saga Pattern (Eventual Consistency)**:
   - Replaces distributed locks with a sequence of local transactions.
   - If Step 3 fails, the Saga orchestrator executes **Compensating Transactions** backwards (e.g., Refund Credit Card) to undo the previous steps.

---

# Layer 4: Rapid-Fire Cheat Sheet & Interview Summary Matrix

---

### Database Isolation Levels & Anomaly Prevention

| Anomaly | Technical Definition | Minimum Level to Prevent |
| :--- | :--- | :--- |
| **Dirty Read** | Reading uncommitted writes of another transaction | Read Committed |
| **Non-Repeatable Read** | Re-reading row returns mutated data | Repeatable Read |
| **Phantom Read** | Range query returns newly inserted rows | Repeatable Read (InnoDB) / Serializable |
| **Write Skew** | Disjoint row updates violating multi-row invariant | Serializable |

---

### The Golden Database Architecture Rules
1. **Normalize to 3NF/BCNF by default**: Prevent data update anomalies; denormalize only for proven read bottlenecks.
2. **Default to Read Committed**: Balances high concurrency with protection against dirty reads.
3. **Beware of Write Skew in Repeatable Read**: Use `SELECT ... FOR UPDATE` or Serializable Snapshot Isolation.
4. **Never use Two-Phase Commit across Microservices**: Use the Saga pattern with compensating transactions.
5. **Enforce Foreign Key Constraints in SQL**: Application-level consistency checks always fail under high-concurrency race conditions.
