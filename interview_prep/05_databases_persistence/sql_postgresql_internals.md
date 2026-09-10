# SQL, PostgreSQL & Database Internals: Enterprise Interview Guide

> **Curriculum Milestone**: Module 05 — Databases & Persistence  
> **Topic Coverage**: ACID Properties, MVCC (`xmin`, `xmax`, `ctid`), Transaction Isolation Levels (Read Committed, Repeatable Read, Serializable, SSI), B-Tree / GIN / GiST / BRIN Indexes, `EXPLAIN (ANALYZE, BUFFERS)` Execution Plans, Query Planner Cost Model, Write-Ahead Logging (WAL) & ARIES Crash Recovery, Join Algorithms (Nested Loop, Hash Join, Merge Join), Window Functions & Framing, Common Table Expressions (`WITH RECURSIVE`), Normalization (1NF→3NF→BCNF) vs Denormalization, Declarative Partitioning, PostgreSQL JSONB vs MongoDB Document Model, Connection Pooling (HikariCP, PgBouncer), Optimistic/Pessimistic Locking (`SELECT FOR UPDATE`), Deadlock Resolution, Zero-Downtime Schema Migrations (Flyway), Streaming vs Logical Replication, TimescaleDB, and Disaster Recovery.  
> **Target Audience**: Senior Software Engineers, Lead Database Architects, Staff Backend Engineers, SREs.  
> **Target Depth**: 50 Comprehensive Scenario-Based Q&As (Tiers 1–4), 7 Fatal Beginner Anti-Patterns, 4 Real-World War-Room Outages, and Rapid-Fire Interview Matrix.

---

## Architecture Blueprint: PostgreSQL Internal Execution Stack

```
+---------------------------------------------------------------------------------------------------------+
|                                    PostgreSQL Internal Execution Stack                                  |
|                                                                                                         |
|  Client Layer (psql, JDBC / HikariCP Pool, PgBouncer Proxy on Port 6432)                              |
|  └─► TCP Port 5432                                                                                      |
|                                                                                                         |
|  PostgreSQL Postmaster & Backend Process Pipeline (One backend per client connection)                   |
|  +──────────────────────────────────────────────────────────────────────────────────────────────────+   |
|  │  [1] Parser          SQL query string -> Abstract Syntax Tree (Syntax verification)             │   |
|  │  [2] Analyzer        Semantic analysis (table/column name resolution against pg_catalog)          │   |
|  │  [3] Rewriter        View expansion, security policies, rule rewriting                           │   |
|  │  [4] Planner/Cost    Query Planner & Cost Optimizer                                              │   |
|  │      ├─ Statistics:  pg_statistic / pg_stats (MCV lists, histogram bounds, correlation)         │   |
|  │      ├─ Cost Model:  seq_page_cost (1.0), random_page_cost (4.0), cpu_tuple_cost (0.01)          │   |
|  │      ├─ Access Path: SeqScan, IndexScan, IndexOnlyScan, BitmapIndexScan -> BitmapHeapScan        │   |
|  │      └─ Join Strategy: Nested Loop (small sets), Hash Join (large unsorted), Merge Join (sorted)│   |
|  │  [5] Executor        Iterative execution engine (Volcano Iterator Model: Next(), Exec())         │   |
|  +──────────────────────────────────────────────────────────────────────────────────────────────────+   |
|                                │                                                                        |
|                                ▼ Reads & Writes via 8KB Buffer Pages                                    |
|  In-Memory Storage Layer (Host RAM)                                                                     |
|  +──────────────────────────────────────────────────────────────────────────────────────────────────+   |
|  │  Shared Buffer Pool (shared_buffers - 25% of Host RAM, e.g., 16GB on 64GB node)                  │   |
|  │  ├─ Hash table lookup: (RelFileNode, ForkNum, BlockNum) -> Buffer Tag (8KB Page)                │   |
|  │  ├─ Clock-Sweep Eviction Algorithm (replaces LRU to prevent buffer pollution on large scans)      │   |
|  │  └─ Dirty Buffers: Background Writer (bgwriter) & Checkpointer flush dirty pages to disk        │   |
|  +──────────────────────────────────────────────────────────────────────────────────────────────────+   |
|                                │                                                                        |
|                                ▼ (fsync flush on commit)                                                |
|  Physical Storage Layer (Disk Subsystem)                                                                |
|  +─────────────────────────────────+  +─────────────────────────────────────────────────────────────+   |
|  │  Write-Ahead Log (WAL / pg_wal) │  │  Heap Files (8KB Disk Pages containing Row Tuples)          │   |
|  │  - Sequential append-only log   │  │  - Page Header: lsn, checksum, lower/upper pointers         │   |
|  │  - Crash recovery (ARIES Redo)  │  │  - Line Pointers (ItemIdData): (offset, length)             │   |
|  │  - Streaming replication source │  │  - Tuples: xmin, xmax, t_cid, infomask, user column data    │   |
|  +─────────────────────────────────+  │  - TOAST Files: Compressed storage for data > 2KB           │   |
|                                       │  - Index Files: B-Tree, GIN, GiST, BRIN, Hash               │   |
|                                       +─────────────────────────────────────────────────────────────+   |
+---------------------------------------------------------------------------------------------------------+
```

---

## Section 1: Progressive Scenario-Based Master Q&A (50 Scenarios)

### Tier 1: ACID, MVCC & Transaction Isolation (Q1 – Q15)

#### Q1: MVCC Deep Dive — `xmin`, `xmax`, `ctid` & Tuple Visibility

##### 1. Exact Scenario & Question
A senior engineer claims: "Our read-heavy analytics queries are blocked because writers are updating the orders table." Explain why PostgreSQL's Multi-Version Concurrency Control (MVCC) architecture means **readers never block writers and writers never block readers**. How do hidden system columns (`xmin`, `xmax`, `ctid`) determine row visibility for a transaction snapshot?

##### 2. What the Interviewer Evaluates
- Understanding of row versioning: Updates create new row tuples rather than overwriting in-place.
- Internal tuple header structure: `xmin` (creation transaction ID), `xmax` (deletion/update transaction ID), `ctid` (physical page and offset pointer).
- How `pg_snapshot` determines whether a tuple is visible to an active transaction.

##### 3. Standout Technical Answer
PostgreSQL implements MVCC by retaining multiple versions of a single row concurrently:
1. **INSERT**: Writes a new row version with `xmin = CurrentTxID` and `xmax = 0` (live).
2. **DELETE**: Does not delete the row; marks `xmax = CurrentTxID`.
3. **UPDATE**: Implemented as **DELETE + INSERT**:
   - The existing tuple has its `xmax` set to `CurrentTxID` (marked dead for future transactions).
   - A brand new tuple is inserted with `xmin = CurrentTxID` and `xmax = 0`, assigned a new physical `ctid`.

```sql
-- Inspect MVCC hidden system columns on live data:
SELECT xmin, xmax, ctid, id, status, amount FROM orders WHERE id = 101;
-- Output:
--  xmin  | xmax |  ctid   | id  | status  | amount
-- -------+------+---------+-----+---------+--------
--  15020 |    0 | (0, 1)  | 101 | PENDING | 149.99

-- After Transaction 15025 executes: UPDATE orders SET status = 'PAID' WHERE id = 101;
-- Output:
--  xmin  | xmax |  ctid   | id  | status  | amount
-- -------+------+---------+-----+---------+--------
--  15025 |    0 | (0, 14) | 101 | PAID    | 149.99

-- Old tuple still exists in page 0 slot 1: (xmin=15020, xmax=15025, ctid=(0,1))
```

**How Snapshot Visibility Works:**
When a transaction begins, it takes a snapshot: `pg_snapshot(xmin: 15022, xmax: 15030, xip_list: [15024, 15028])`.
A row is visible if:
- `row.xmin` is committed AND `row.xmin < snapshot.xmin` (or committed before snapshot taken).
- `row.xmin` is NOT in `snapshot.xip_list` (was not an active in-flight transaction when snapshot was created).
- `row.xmax` is either 0, uncommitted, or `> snapshot.xmax`.

Because readers merely inspect these integer comparisons against their private snapshot without acquiring read locks on table data, **readers never block writers, and writers never block readers**.

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "If readers never block writers, can a `SELECT` query ever be blocked by another operation in PostgreSQL?"
- **Winning Answer**: "Yes! A `SELECT` query acquires an `AccessShareLock` on the table. It is blocked if a concurrent transaction holds an `AccessExclusiveLock`, which is acquired by DDL statements such as `ALTER TABLE`, `DROP TABLE`, `TRUNCATE`, or `VACUUM FULL`. Standard DML `UPDATE` and `INSERT` do not block `SELECT`."

---

#### Q2: Isolation Levels: Read Committed vs Repeatable Read vs Serializable (SSI)

##### 1. Exact Scenario & Question
A banking system executes an account transfer. An engineer sets `ISOLATION LEVEL REPEATABLE READ` and assumes the system is completely safe from concurrency bugs. Demonstrate an exact scenario where Repeatable Read permits a **Write Skew anomaly**, and explain how **Serializable Snapshot Isolation (SSI)** prevents it using `SIREAD` locks.

##### 2. What the Interviewer Evaluates
- Knowledge of the 4 ANSI SQL isolation levels and the 4 concurrency anomalies: Dirty Read, Non-Repeatable Read, Phantom Read, and Serialization Anomaly (Write Skew).
- Understanding that PostgreSQL Repeatable Read prevents Phantom Reads, but still allows Write Skew.
- How SSI detects dependency cycles in the Serialization Graph without physical lock contention.

##### 3. Standout Technical Answer
**The Write Skew Anomaly in Repeatable Read:**
*Business Rule*: The sum of Checking ($C) + Savings ($S) balances must never drop below $0.
- Initial State: $C = 100$, $S = 100$. Total = $200.
- **Transaction 1 (Withdraw $150 from Checking)**:
  - Checks total: $100 + 100 = 200 \ge 150$ (Valid).
  - Updates Checking: $100 - 150 = -50$.
- **Transaction 2 Concurrent (Withdraw $150 from Savings)**:
  - Checks total: $100 + 100 = 200 \ge 150$ (Valid in its snapshot!).
  - Updates Savings: $100 - 150 = -50$.
- **Both Transactions Commit!**
  - Final State: $C = -50$, $S = -50$. Total = **-$100** (Violated business rule!).

```sql
-- Why Repeatable Read Failed:
-- Tx 1 modified row 'Checking'; Tx 2 modified row 'Savings'.
-- Because they modified DIFFERENT rows, row-level write locks never conflicted!

-- How SERIALIZABLE (SSI) Prevents This:
BEGIN TRANSACTION ISOLATION LEVEL SERIALIZABLE;
-- PostgreSQL tracks read dependencies using non-blocking SIREAD locks in memory.
-- It detects a rw-antidependency cycle between Tx 1 and Tx 2 in the Serialization Graph.
-- When Tx 2 attempts to commit, PostgreSQL aborts it immediately:
-- ERROR: 40001: could not serialize access due to read/write dependencies among transactions
```

| Isolation Level | Dirty Read | Non-Repeatable Read | Phantom Read | Write Skew | PostgreSQL Mechanism |
|---|---|---|---|---|---|
| **Read Committed** (Default) | Prevented | **Possible** | **Possible** | **Possible** | New snapshot per statement |
| **Repeatable Read** | Prevented | Prevented | Prevented | **Possible** | Snapshot taken at transaction start |
| **Serializable** | Prevented | Prevented | Prevented | Prevented | SSI (`SIREAD` locks + conflict graph) |

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "Does Serializable isolation eliminate the need for application-level error handling?"
- **Winning Answer**: "No! It actually makes application-level retry logic **mandatory**. When PostgreSQL detects a serialization anomaly, it terminates the conflicting transaction with error code `40001 (serialization_failure)`. Applications using Serializable isolation must wrap transactions in an automated exponential-backoff retry loop."

---

#### Q3: Table Bloat & Autovacuum Architecture: The Wraparound Outage

##### 1. Exact Scenario & Question
A high-throughput PostgreSQL database processing 500 million updates per day suddenly enters an emergency read-only shutdown with the error: `database is not accepting commands to avoid wraparound data loss in database "prod"`. Explain the 32-bit transaction ID wraparound mechanism, why dead tuples accumulate, and how to tune Autovacuum to prevent cluster meltdowns.

##### 2. What the Interviewer Evaluates
- 32-bit Transaction ID arithmetic ($2^{32} \approx 4.29$ billion transaction IDs).
- Transaction ID "freezing" (`FrozenTransactionId = 2`).
- Autovacuum cost model: `autovacuum_vacuum_cost_limit` and `autovacuum_vacuum_scale_factor`.

##### 3. Standout Technical Answer
**The Transaction ID Wraparound Emergency:**
PostgreSQL compares transaction IDs using modulo arithmetic ($2^{31}$ in the past, $2^{31}$ in the future).
If the transaction ID counter advances by 2.1 billion transactions without freezing old tuples:
$$\text{Old historical tuples appear to have been created in the FUTURE} \longrightarrow \text{They vanish completely!}$$
To prevent silent data loss, PostgreSQL enters **emergency fail-safe mode**: shuts down all writes and only accepts connections in single-user mode to run emergency `VACUUM FREEZE`.

```sql
-- Monitor Transaction ID Wraparound Risk:
SELECT datname, age(datfrozenxid) AS xid_age,
       2147483648 - age(datfrozenxid) AS xids_until_catastrophe
FROM pg_database
ORDER BY xid_age DESC;
-- Alert if age > 1.5 billion!
```

**Autovacuum Responsibilities:**
1. Reclaims disk space by unlinking dead tuples and making space available for future inserts in the Page Free Space Map (FSM).
2. Updates table statistics in `pg_statistic` so the query planner makes accurate join decisions.
3. Freezes old `xmin` values to `FrozenTransactionId` (marking them permanently older than all transactions).

**Production Autovacuum Tuning:**
Default settings are too conservative for enterprise loads (they throttle vacuuming to avoid disk I/O, allowing dead tuples to pile up into massive table bloat):
```ini
# /etc/postgresql/postgresql.conf
# Run vacuum workers aggressively
autovacuum_max_workers = 6
# Increase I/O budget before throttling worker (default 200 is way too slow)
autovacuum_vacuum_cost_limit = 2000
autovacuum_vacuum_cost_delay = 2ms

# Scale factor: vacuum table when 2% of rows change (default 20% is fatal on 100M rows!)
autovacuum_vacuum_scale_factor = 0.02
autovacuum_analyze_scale_factor = 0.01
```

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "Does running a standard `VACUUM` return freed disk space to the operating system filesystem?"
- **Winning Answer**: "No. Standard `VACUUM` only reclaims space *inside* the 8KB page and records it in the Free Space Map (FSM) so future `INSERT` operations can reuse the empty slots without growing the file. To return physical disk space to the OS filesystem, you must run `VACUUM FULL` (which acquires an exclusive lock and rewrites the entire table) or use the non-blocking extension `pg_repack`."

---

#### Q4: Write-Ahead Logging (WAL) & ARIES Crash Recovery

##### 1. Exact Scenario & Question
A physical server loses power while committing a 50MB batch insert. When power is restored, how does PostgreSQL guarantee durability and consistency without corrupting data pages? Walk through the **ARIES (Algorithm for Recovery and Isolation Exploiting Semantics)** recovery phases.

##### 2. What the Interviewer Evaluates
- Decoupling of sequential WAL writes from random heap page writes.
- Buffer cache checkpoints (`checkpoint_timeout`, `max_wal_size`).
- ARIES 3-pass recovery: Analysis -> Redo (Repeat history) -> Undo (Rollback uncommitted).

##### 3. Standout Technical Answer
Writing modified 8KB heap pages randomly across a multi-terabyte disk during every commit is too slow.
Instead, PostgreSQL uses **Write-Ahead Logging (WAL)**:
1. When a row changes, PostgreSQL writes a small, append-only binary delta record to the in-memory **WAL buffer**.
2. **Commit Guarantee**: On `COMMIT`, PostgreSQL executes an `fsync()` system call flushing the sequential WAL records to physical disk (`pg_wal/`). Once flushed, the transaction returns success to the client, even though the modified 8KB heap page is still dirty in `shared_buffers`!
3. **Checkpoint Process**: Periodically (e.g., every 15 minutes or 1GB of WAL), the background **Checkpointer** process flushes all dirty pages from `shared_buffers` to physical table files and writes a `CHECKPOINT` record to WAL.

```
On Crash & Reboot (ARIES Recovery):
Checkpoint Marker ────────────────► WAL Records ────────────────► Crash Point
 (Known clean state)                 (Redo Phase)                 (Power Loss)
```
- **Phase 1 (Analysis)**: Scans forward from the last known clean `CHECKPOINT` record to determine active transactions and dirty pages at the time of the crash.
- **Phase 2 (Redo / Repeat History)**: Replays every WAL record forward from the checkpoint to the crash point, restoring physical disk pages to the exact state they were in at the millisecond of power failure.
- **Phase 3 (Undo)**: Rolls back all changes made by transactions that were still active (uncommitted) at the moment of the crash, restoring strict transactional consistency.

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "What is `synchronous_commit = off` and what is the trade-off?"
- **Winning Answer**: "Setting `synchronous_commit = off` causes `COMMIT` to return success immediately after writing to the in-memory WAL buffer, without waiting for the disk `fsync()` to complete. This boosts write throughput by 5x–10x, but risks losing the last few milliseconds of committed transactions in the event of a power crash (durability loss, though database internal consistency remains 100% uncorrupted)."

---

#### Q5: B-Tree Index Architecture & O(log N) Search Mechanics

##### 1. Exact Scenario & Question
Explain the internal on-disk physical structure of a PostgreSQL **B-Tree index**. Walk through how the database finds a row with `id = 45290` across Root, Internal, and Leaf pages. What causes an index page split, and how does the right-sibling pointer prevent deadlocks during concurrent reads?

##### 2. What the Interviewer Evaluates
- Physical layout: Lehman & Yao B-tree variant used by PostgreSQL.
- Page anatomy: High Key, Item pointers, right-sibling links.
- 50/50 page split mechanics during insert spikes.

##### 3. Standout Technical Answer
PostgreSQL implements the **Lehman-Yao B-Tree algorithm**:
- Stored as standard 8KB pages.
- Divided into: **Meta Page** (points to root), **Internal Pages** (routing keys + down-pointers), and **Leaf Pages** (indexed keys + heap `ctid` pointers `(block_number, offset)`).

```
                      [ Root Page (Level 2) ]
                      Keys: [ 20,000 | 50,000 ]
                             │          │
                 ┌───────────┘          └───────────┐
                 ▼                                  ▼
      [ Internal Page (Level 1) ]        [ Internal Page (Level 1) ]
      Keys: [ 30,000 | 40,000 ]          Keys: [ 60,000 | 80,000 ]
                 │
                 ▼
      [ Leaf Page (Level 0) ] ──Right Pointer──► [ Sibling Leaf Page ]
      Keys & ctids:                              Keys & ctids:
      [ 40,001 -> (12, 1) ]                     [ 45,001 -> (14, 2) ]
      [ 45,290 -> (12, 8) ] ──► Points to Row!  [ 49,999 -> (14, 9) ]
```

**Traversal Sequence for `id = 45290`**:
1. Reads Meta Page to locate the Root Page.
2. Root Page: $20,000 < 45,290 < 50,000$. Follows internal pointer to Level 1 page.
3. Internal Page: $45,290 > 40,000$. Follows pointer to Leaf Page.
4. Leaf Page: Executes binary search inside the 8KB page. Finds matching key `45290` and extracts `ctid = (12, 8)`.
5. Reads Heap block 12, tuple offset 8 to retrieve full row data. Total I/O: 3 index page reads + 1 heap read = **4 buffer hits (O(log N))**.

**Page Splits & Lehman-Yao Right Pointers:**
When a leaf page fills its 8KB capacity and a new key is inserted:
- It splits 50/50 into a new sibling page.
- In traditional B-Trees, a split requires write-locking the parent page and child pages, causing reader deadlocks.
- Lehman-Yao B-Trees add a **High Key** and a **Right-Sibling Pointer** on every page. If a reader searches for key `45290` while a split is occurring, it detects that $45290 > \text{High Key}$ and simply walks the right-sibling pointer directly to the new page **without blocking or backtracking**.

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "Why does ordering columns as `(tenant_id, created_at)` in a composite B-tree index support queries filtering on `tenant_id` alone, but fails for queries filtering on `created_at` alone?"
- **Winning Answer**: "Because composite B-trees are sorted lexicographically, analogous to a phone book sorted by `(LastName, FirstName)`. You can efficiently find all people named 'Smith' (leading column), but finding all people with the first name 'John' requires scanning the entire phone book from cover to cover."

---

#### Q6: Specialized Indexes: GIN vs GiST vs BRIN vs Hash

##### 1. Exact Scenario & Question
Compare PostgreSQL's specialized index engines: **GIN**, **GiST**, **BRIN**, and **Hash**. Provide a concrete enterprise scenario where a **BRIN index** reduces index size from 50GB to 50MB while outperforming a B-Tree.

##### 2. What the Interviewer Evaluates
- Understanding when B-Trees fail (e.g., massive append-only audit logs, geometric coordinates, JSONB paths, full-text search).
- Structural trade-offs: GIN inverted indexes vs BRIN block-range summaries.

##### 3. Standout Technical Answer
- **GIN (Generalized Inverted Index)**:
  - An inverted index mapping single elements (words, JSONB keys/values, array items) to a list of matching row IDs.
  - *Best For*: JSONB queries (`attributes @> '{"status": "active"}'`), full-text search (`tsvector`), array inclusion (`tags && ARRAY['java']`).
  - *Trade-off*: Slower update throughput (every insert modifies multiple inverted tree leaves).
- **GiST (Generalized Search Tree)**:
  - Balanced tree supporting hierarchical and non-overlapping/overlapping data representations.
  - *Best For*: Geometric / PostGIS spatial coordinates (`ST_DWithin`), range types (`daterange`), nearest-neighbor search (`<->` operator).
- **Hash Index**:
  - In-memory/disk bucket hashing for exact equality (`=`). O(1) lookup.
  - *Trade-off*: Does not support range queries (`<`, `>`) or `ORDER BY`.
- **BRIN (Block Range Index) — The 50GB -> 50MB Solution**:
  - Instead of indexing every single row, BRIN stores only the **minimum and maximum value** for a block range of pages (default 128 heap pages = 1MB of disk).
  - *Enterprise Scenario*: A 1-billion-row `audit_logs` table (500GB heap) where `created_at` is monotonically increasing.
  - A B-Tree index on `created_at` stores 1 billion pointers = **~30GB index size**.
  - A BRIN index on `created_at` stores min/max dates for each 1MB range = **~40MB index size (99.8% smaller!)**.

```sql
-- Creating BRIN index on append-only time-series data:
CREATE INDEX idx_audit_logs_created_at_brin
ON audit_logs USING brin (created_at)
WITH (pages_per_range = 128);

-- Query execution:
SELECT * FROM audit_logs
WHERE created_at BETWEEN '2026-09-01' AND '2026-09-02';
-- The executor checks BRIN summary: skips 95% of heap blocks entirely!
```

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "What happens to a BRIN index if rows are frequently UPDATED or deleted, destroying the physical ordering of the data on disk?"
- **Winning Answer**: "BRIN index performance completely collapses. If updates move rows across pages, the min/max range of every block broadens to cover the entire date spectrum. The executor can no longer skip blocks and degrades into scanning almost every page in the table. BRIN is strictly suited for **append-only, physically correlated data**."

---

#### Q7: Query Optimization: Interpreting `EXPLAIN (ANALYZE, BUFFERS)`

##### 1. Exact Scenario & Question
A production reporting query takes 35 seconds to run. You run `EXPLAIN (ANALYZE, BUFFERS)`. Below is the output. Walk through every line, explain the mathematical cost formula, identify the primary performance bottleneck, and provide the fix.

```text
Hash Join  (cost=45210.00..182450.50 rows=150000 width=72) (actual time=812.340..34821.120 rows=12 loops=1)
  Hash Cond: (o.customer_id = c.id)
  Buffers: shared hit=4210 read=145200, temp read=28450 written=28450
  ->  Seq Scan on orders o  (cost=0.00..125000.00 rows=5000000 width=40) (actual time=0.080..21450.200 rows=5000000 loops=1)
        Buffers: shared read=95000
  ->  Hash  (cost=25000.00..25000.00 rows=100000 width=36) (actual time=750.120..750.120 rows=100000 loops=1)
        Buckets: 131072  Batches: 2  Memory Usage: 6400kB
        Buffers: shared hit=4210 read=12000, temp written=14200
        ->  Seq Scan on customers c  (cost=0.00..25000.00 rows=100000 width=36) (actual time=0.040..520.100 rows=100000 loops=1)
              Filter: (country = 'FRANCE'::text)
              Rows Removed by Filter: 900000
```

##### 2. What the Interviewer Evaluates
- Ability to read real-world query execution trees inside-out.
- Decoupling cost estimates from actual wall-clock runtimes.
- Identification of I/O spill to disk (`temp read/written`, `Batches: 2`).

##### 3. Standout Technical Answer
**Analysis Breakdown:**
1. **The Cost Formula**:
   $$\text{Total Cost} = (\text{Page Fetches} \times \text{page\_cost}) + (\text{Tuples Processed} \times \text{cpu\_tuple\_cost}) + (\text{Operators} \times \text{cpu\_operator\_cost})$$
   `cost=45210.00..182450.50`: The first number (45210) is **startup cost** (time to produce first row); second is **total estimated cost**.
2. **Bottleneck 1: Memory Spill to Disk (`work_mem` exhaustion)**:
   - Notice: `Batches: 2`, `temp written=14200`, `temp read=28450`.
   - The Hash table for `customers` exceeded the allocated `work_mem`. The hash join had to partition data and **spill to physical disk temporary files**, multiplying I/O latency.
3. **Bottleneck 2: Massive Sequential Scan & Filter Waste**:
   - `Seq Scan on customers`: Evaluated 1,000,000 rows, but `Rows Removed by Filter: 900,000`! Only 100,000 rows survived.
   - `Seq Scan on orders`: Read all 5,000,000 orders sequentially (`shared read=95000` pages from disk) when only 12 rows were ultimately produced!

**The Optimizations:**
1. **Increase `work_mem` for the query**:
   ```sql
   SET work_mem = '128MB'; -- Allows the hash table to fit in RAM (Batches: 1, temp read=0!)
   ```
2. **Add an Index on Filtered Columns**:
   ```sql
   CREATE INDEX CONCURRENTLY idx_customers_country ON customers (country);
   CREATE INDEX CONCURRENTLY idx_orders_customer_id ON orders (customer_id);
   ```
After adding the indexes and increasing `work_mem`, the planner switches from a SeqScan + Disk-Spilling HashJoin to an **Index Scan + in-memory Hash Join / Nested Loop**, reducing execution time from **35 seconds to 12 milliseconds**!

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "If `rows=150000` was estimated by the planner, but `actual rows=12` was returned, what is the root cause of this massive miscalculation?"
- **Winning Answer**: "Stale table statistics in `pg_statistic`. The Autovacuum analyze worker has not updated column correlation and frequency histograms recently, or the sample size was too small. Running `ANALYZE customers; ANALYZE orders;` or increasing `default_statistics_target` from 100 to 500 recalibrates the statistics, allowing the planner to choose the correct plan."

---

#### Q8: Join Strategies: Nested Loop vs Hash Join vs Merge Join

##### 1. Exact Scenario & Question
Compare the execution mechanics, memory requirements, and planner cost conditions for **Nested Loop**, **Hash Join**, and **Merge Join**. When does the planner force a Nested Loop even on large tables?

##### 2. What the Interviewer Evaluates
- Algorithmic complexity ($O(N \times M)$, $O(N + M)$, $O(N \log N + M \log M)$).
- Sorting prerequisites and pipeline streaming behavior.

##### 3. Standout Technical Answer
- **Nested Loop Join**:
  - *Algorithm*: For each outer row, search the inner table.
  - *Best For*: Small outer table joining an inner table with an efficient **Index Scan** ($O(N \log M)$).
  - *Streaming*: Emits first rows immediately (zero startup cost).
  - *When Forced on Large Tables*: When combined with `LIMIT` (e.g., `LIMIT 10`), the planner assumes it will find 10 matches quickly and picks Nested Loop, which can catastrophically degrade into a multi-hour scan if matches are sparse.
- **Hash Join**:
  - *Algorithm*: Scans inner table, builds in-memory hash table on join key in `work_mem`. Scans outer table and probes hash table ($O(N + M)$).
  - *Prerequisite*: Can only join on **equality conditions** (`=`).
  - *Trade-off*: High startup cost (must read entire inner table before emitting first row). Spills to disk if hash table exceeds `work_mem`.
- **Merge Join**:
  - *Algorithm*: Both relations must be **pre-sorted** on the join keys. Two cursors walk down the tables in tandem ($O(N + M)$).
  - *Best For*: Large tables that are already sorted by an existing B-Tree index or an explicit `ORDER BY`.
  - *Streaming*: Emits rows as it walks with minimal memory consumption.

| Join Type | Equality Only? | Requires Pre-Sorted Input? | Memory Usage | Startup Cost |
|---|---|---|---|---|
| **Nested Loop** | No (supports `>`, `<`) | No | $O(1)$ Minimal | Near Zero |
| **Hash Join** | **Yes (`=`)** | No | High (`work_mem`) | High (Build Phase) |
| **Merge Join** | **Yes (`=`)** | **Yes (Mandatory)** | Low | Low (if pre-sorted) |

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "Can a Hash Join be used for a non-equality join condition like `WHERE a.val BETWEEN b.low AND b.high`?"
- **Winning Answer**: "No! Hash algorithms rely on hashing identical equality values into matching hash buckets. Range conditions cannot use hash lookups and must be executed via **Nested Loop Join** or **Merge Join**."

---

#### Q9: Window Functions & The Power of the Window Frame

##### 1. Exact Scenario & Question
Write a single SQL query using window functions that calculates: (1) Running total of customer spend, (2) 3-month moving average of monthly revenue, and (3) Top 3 highest spending customers per country without subqueries. Explain the performance difference between `ROWS` and `RANGE` frame clauses.

##### 2. What the Interviewer Evaluates
- Framing syntax: `ROWS BETWEEN ... AND ...`.
- Default framing behavior (`RANGE BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW`) performance trap.
- Ranking functions (`DENSE_RANK()`).

##### 3. Standout Technical Answer

```sql
WITH customer_orders AS (
  SELECT 
    c.country,
    c.id AS customer_id,
    c.name,
    o.created_at,
    o.total_amount,
    -- 1. Running total per customer
    SUM(o.total_amount) OVER (
      PARTITION BY c.id 
      ORDER BY o.created_at
      ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW -- Explicit ROWS frame!
    ) AS customer_running_total,

    -- 2. 3-Month Moving Average of spend
    AVG(o.total_amount) OVER (
      PARTITION BY c.id
      ORDER BY o.created_at
      ROWS BETWEEN 2 PRECEDING AND CURRENT ROW
    ) AS moving_avg_3_orders,

    -- 3. Dense rank per country based on total spend
    DENSE_RANK() OVER (
      PARTITION BY c.country
      ORDER BY SUM(o.total_amount) OVER (PARTITION BY c.id) DESC
    ) AS country_spend_rank
  FROM customers c
  JOIN orders o ON o.customer_id = c.id
)
SELECT * FROM customer_orders
WHERE country_spend_rank <= 3
ORDER BY country, country_spend_rank, created_at;
```

**The `ROWS` vs `RANGE` Performance Trap:**
- **`ROWS`**: Evaluates physical row offsets. The executor simply counts $N$ preceding rows in the memory buffer. Extremely fast ($O(1)$ per row).
- **`RANGE` (The Dangerous Default)**: Evaluates value offsets. It must examine peer rows to check for duplicate values on the `ORDER BY` column.
- *Production Hazard*: `SUM(...) OVER (ORDER BY date)` defaults to `RANGE BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW`. On large tables, the duplicate checking in `RANGE` mode can make the query **10x to 50x slower** than explicitly specifying `ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW`!

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "Can you use a Window Function directly inside a `WHERE` or `HAVING` clause?"
- **Winning Answer**: "No! Window functions execute during the `SELECT` phase, **after** `WHERE`, `GROUP BY`, and `HAVING` have already finished filtering rows. To filter on the result of a window function, you must wrap it in a Common Table Expression (CTE) or derived subquery."

---

#### Q10: Recursive CTEs: Hierarchical Graph Traversal

##### 1. Exact Scenario & Question
Write a PostgreSQL **Recursive Common Table Expression (CTE)** that traverses an organizational employee hierarchy (`employees: id, manager_id, name, salary`) to find all direct and indirect reports of the CEO, calculate their management depth level, and detect circular loops.

##### 2. What the Interviewer Evaluates
- Recursive CTE structure: Anchor Member -> `UNION ALL` -> Recursive Member -> Termination condition.
- Preventing infinite loops using path arrays (`CYCLE` detection).

##### 3. Standout Technical Answer

```sql
WITH RECURSIVE OrgChart AS (
    -- 1. ANCHOR MEMBER: Select the top-level CEO (manager_id IS NULL)
    SELECT 
        id, 
        manager_id, 
        name, 
        salary, 
        1 AS depth_level,
        ARRAY[id] AS path_tracker -- Array tracks visited IDs to detect cycles!
    FROM employees
    WHERE manager_id IS NULL

    UNION ALL

    -- 2. RECURSIVE MEMBER: Join recursive result set back to employees table
    SELECT 
        e.id, 
        e.manager_id, 
        e.name, 
        e.salary, 
        o.depth_level + 1,
        o.path_tracker || e.id
    FROM employees e
    JOIN OrgChart o ON e.manager_id = o.id
    -- 3. TERMINATION & CYCLE GUARD: Stop recursion if ID already in path!
    WHERE NOT (e.id = ANY(o.path_tracker))
)
SELECT 
    depth_level,
    REPEAT('  ', depth_level - 1) || name AS organizational_tree,
    salary,
    array_to_string(path_tracker, ' -> ') AS reporting_chain
FROM OrgChart
ORDER BY path_tracker;
```

**Internal Mechanics:**
1. Evaluates Anchor Query, places rows into **Working Table** and **Result Table**.
2. Executes Recursive Query replacing `OrgChart` with current contents of **Working Table**.
3. Empties Working Table, fills it with new rows generated in step 2, and appends them to Result Table.
4. Repeats until the Working Table is empty.

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "In PostgreSQL 12+, how does the `MATERIALIZED` vs `NOT MATERIALIZED` keyword change the behavior of CTEs?"
- **Winning Answer**: "Prior to PG 12, all CTEs were an **optimization fence**: PostgreSQL materialized the CTE into a temporary table in memory, preventing the optimizer from pushing outer `WHERE` filters down into the CTE. In PG 12+, non-recursive CTEs are inlined by default (`NOT MATERIALIZED`). Specifying `WITH my_cte AS MATERIALIZED (...)` forces PostgreSQL to evaluate the CTE once and store it as a temporary buffer, which is useful when the CTE is referenced multiple times in the query."

---

#### Q11: Normalization (1NF→3NF→BCNF) vs Pragmatic Denormalization

##### 1. Exact Scenario & Question
Define **1NF**, **2NF**, **3NF**, and **Boyce-Codd Normal Form (BCNF)**. Demonstrate a real-world table that satisfies 3NF but violates BCNF. When does an enterprise intentionally choose denormalization, and how do you protect data integrity?

##### 2. What the Interviewer Evaluates
- Mathematical definitions of functional dependencies and candidate keys.
- Anomalies: Insertion, Update, and Deletion anomalies.
- Denormalization trade-offs: Read performance vs write amplification and stale data.

##### 3. Standout Technical Answer
- **1NF**: Atomic values (no repeating groups, arrays, or comma-separated lists in columns); primary key defined.
- **2NF**: Satisfies 1NF + **No Partial Dependencies** (all non-key attributes must depend on the *entire* composite primary key, not a subset).
- **3NF**: Satisfies 2NF + **No Transitive Dependencies** (non-key attributes depend *only* on candidate keys: $A \rightarrow B \rightarrow C$ is forbidden).
- **BCNF**: Strict version of 3NF. **Every determinant must be a candidate key** (if $X \rightarrow Y$, then $X$ must be a superkey).

**The Classic 3NF vs BCNF Violation Scenario:**
A clinic scheduling table: `Appointments(patient_id, doctor_id, specialty)`
- Constraints:
  1. A patient can see at most one doctor per specialty: `(patient_id, specialty) -> doctor_id`.
  2. Each doctor has exactly one specialty: `doctor_id -> specialty`.
- Candidate Keys: `(patient_id, specialty)` and `(patient_id, doctor_id)`.
- **3NF Evaluation**: Specialty is a prime attribute (part of candidate key `patient_id, specialty`), so 3NF is satisfied!
- **BCNF Evaluation**: In functional dependency `doctor_id -> specialty`, `doctor_id` is a determinant, but it is **NOT a candidate key by itself**! Violates BCNF.
  - *Anomaly*: If Doctor Smith changes specialty from Pediatrics to Dermatology, multiple rows must be updated. If a doctor has no patients scheduled, you cannot record their specialty without creating a null patient!
  - *Fix for BCNF*: Decompose into two tables: `Doctor(doctor_id, specialty)` and `Appointments(patient_id, doctor_id)`.

**Pragmatic Denormalization Patterns:**
In high-throughput e-commerce, joining 8 tables for every catalog page view is too expensive.
- *Pattern*: Store computed totals (`total_spent`, `order_count`) directly on the `customers` table.
- *Protection*: Protect data integrity using database triggers or asynchronous reconciliation batch jobs (e.g., nightly audit comparing sum of order items vs denormalized header).

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "Why is storing order item price at checkout inside the `order_items` table NOT considered a denormalization violation?"
- **Winning Answer**: "Because `order_items.unit_price` is **historical point-in-time snapshot data**, not a duplicate of `products.current_price`. Product prices change dynamically over time. The price at the moment of purchase is an immutable attribute of that specific transaction."

---

#### Q12: The N+1 Query Problem: Detection & Elimination in SQL & JPA

##### 1. Exact Scenario & Question
A REST endpoint `/orders` returns 100 orders with their associated order items. The page takes 6 seconds to load. Database query logs reveal **101 SQL queries executed** for a single HTTP request. Explain the N+1 problem, show how to diagnose it using SQL logs, and implement the three distinct fixes: `JOIN FETCH`, `@EntityGraph`, and `hibernate.default_batch_fetch_size`.

##### 2. What the Interviewer Evaluates
- ORM lazy-loading mechanics: Proxy generation vs batch loading.
- `MultipleBagFetchException` in Hibernate when fetching multiple collections.
- Subselect and IN-clause batching mechanics.

##### 3. Standout Technical Answer
**The Root Cause:**
Application executes 1 initial query to fetch 100 orders:
```sql
SELECT * FROM orders LIMIT 100;
```
Then, as code iterates through each order calling `order.getItems()`, Hibernate's lazy proxy intercepts the getter and issues **100 individual queries**:
```sql
SELECT * FROM order_items WHERE order_id = 1;
SELECT * FROM order_items WHERE order_id = 2;
... (x100 queries!)
```
Total queries = $1 + N = 101$. Database latency is multiplied by 100 network round-trips.

**Fix 1: JPQL `JOIN FETCH`**:
```java
@Query("SELECT DISTINCT o FROM Order o JOIN FETCH o.items WHERE o.status = :status")
List<Order> findAllWithItems(@Param("status") OrderStatus status);
```
- Compiles into a single SQL `INNER JOIN`.
- *Limitation*: Cannot fetch more than one `@OneToMany` List concurrently (`MultipleBagFetchException` cartesian product explosion).

**Fix 2: `@EntityGraph` (Ad-Hoc Eager Fetching)**:
```java
@EntityGraph(attributePaths = {"items", "customer"})
@Query("SELECT o FROM Order o")
List<Order> findAllOrders();
```
- Overrides `FetchType.LAZY` dynamically for this specific repository method without altering entity annotations.

**Fix 3: Batch Fetching (`default_batch_fetch_size = 50`)**:
```properties
# application.properties
spring.jpa.properties.hibernate.default_batch_fetch_size=50
```
- When Hibernate loads lazy proxies, it groups them into an `IN` clause:
  ```sql
  SELECT * FROM order_items WHERE order_id IN (1, 2, 3, ... 50);
  SELECT * FROM order_items WHERE order_id IN (51, 52, ... 100);
  ```
- Reduces 101 queries down to **3 queries** without risking cartesian product row multiplication.

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "If you use `JOIN FETCH` with Pagination (`Pageable pageable`), what severe warning does Hibernate log in production?"
- **Winning Answer**: "Hibernate logs: `HHH000104: firstResult/maxResults specified with collection fetch; applying in memory!`. Because the SQL JOIN produces multiple rows per parent entity, SQL-level `LIMIT` would truncate child rows prematurely. Hibernate is forced to **fetch all 1 million rows from the database into JVM memory** and perform pagination in memory, which immediately causes a catastrophic OutOfMemoryError."

---

#### Q13: Declarative Partitioning: Range vs Hash vs List & Pruning

##### 1. Exact Scenario & Question
Your financial ledger table has 800 million rows and is growing by 50 million rows per month. Queries filtering by `transaction_date` are experiencing high latency due to massive B-Tree index traversal. Design a **Declarative Range Partitioning** strategy in PostgreSQL with automated partition creation and explain how **Run-Time Partition Pruning** works.

##### 2. What the Interviewer Evaluates
- PostgreSQL native partitioning syntax (PG 11+).
- Partition pruning: Static (plan-time) vs Dynamic (run-time execution).
- Maintenance automation via `pg_partman`.

##### 3. Standout Technical Answer

```sql
-- 1. Create Root Partitioned Table (Cannot hold data directly)
CREATE TABLE transactions (
    id               BIGSERIAL,
    transaction_date DATE NOT NULL,
    customer_id      BIGINT NOT NULL,
    amount           DECIMAL(12,2) NOT NULL,
    status           VARCHAR(20) NOT NULL,
    PRIMARY KEY (id, transaction_date) -- Partition key MUST be part of Primary Key!
) PARTITION BY RANGE (transaction_date);

-- 2. Create Concrete Monthly Partitions
CREATE TABLE transactions_2026_08 PARTITION OF transactions
    FOR VALUES FROM ('2026-08-01') TO ('2026-09-01');

CREATE TABLE transactions_2026_09 PARTITION OF transactions
    FOR VALUES FROM ('2026-09-01') TO ('2026-10-01');

-- 3. Default Partition (Catch-all for safety)
CREATE TABLE transactions_default PARTITION OF transactions DEFAULT;

-- 4. Local Indexes are created automatically on each partition!
CREATE INDEX idx_trans_customer_id ON transactions (customer_id);
```

**Partition Pruning Mechanics:**
When a query executes:
```sql
SELECT * FROM transactions WHERE transaction_date = '2026-09-15';
```
1. **Static Pruning (Plan Time)**: The query planner evaluates constant expressions in `WHERE`, determines that only `transactions_2026_09` can possibly contain matching rows, and completely removes all other partition scans from the execution plan.
2. **Run-Time Pruning (Execution Time)**: Used when filtering by parameterized queries (`WHERE transaction_date = $1` or subqueries). During query initialization, the executor evaluates the bound parameter and deactivates non-matching partition sub-plans dynamically.

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "Can you enforce a global `UNIQUE (email)` constraint across all partitions in a partitioned table in PostgreSQL?"
- **Winning Answer**: "No. PostgreSQL enforces unique constraints locally per partition. To enforce a unique constraint on a partitioned table, the partition key **must be included** in the unique constraint (`UNIQUE (email, partition_key)`). Enforcing global uniqueness across partitions without the partition key requires an external advisory lock or application-level table check."

---

#### Q14: PostgreSQL JSONB vs Relational Data Modeling

##### 1. Exact Scenario & Question
A team proposes storing all user profile attributes inside a single `JSONB` column instead of creating normalized relational tables. Contrast PostgreSQL `JSONB` with traditional relational modeling. How does `JSONB` differ from `JSON` at the binary storage level, and how does a **GIN index** accelerate JSON path queries?

##### 2. What the Interviewer Evaluates
- Internal binary representation: `JSON` (raw text storage, re-parsed on every read) vs `JSONB` (deconstructed binary format with indexed keys).
- GIN index operator classes: `jsonb_ops` vs `jsonb_path_ops`.

##### 3. Standout Technical Answer
- **`JSON` (Text Storage)**:
  - Exact copy of input text (preserves whitespace and key order).
  - Fast to insert (raw string write).
  - Slow to query (must re-parse the JSON text on every single row inspection).
- **`JSONB` (Decomposed Binary Format)**:
  - Parses JSON into a structured binary tree on write. Removes whitespace and duplicate keys.
  - Significantly faster to process (supports direct dictionary key lookup without parsing).
  - Supports powerful GIN indexing.

```sql
CREATE TABLE product_catalog (
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(200) NOT NULL,
    specs      JSONB NOT NULL
);

-- GIN Index on JSONB for containment queries:
CREATE INDEX idx_product_specs ON product_catalog USING GIN (specs jsonb_path_ops);

-- High-performance query leveraging the GIN index:
SELECT name FROM product_catalog
WHERE specs @> '{"screen": "OLED", "features": ["5G"]}';
-- Uses Bitmap Index Scan on idx_product_specs in O(log N) time!
```

**Decision Matrix: Relational vs JSONB:**
- Use **Relational Tables** when: Attributes are structured, frequently filtered/aggregated, require foreign key referential integrity, or have strict schema constraints.
- Use **JSONB** when: Attributes are polymorphic, highly dynamic (e.g., custom attributes across 10,000 different e-commerce product categories), or ingested from third-party vendor payloads.

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "What is the difference between GIN index operator classes `jsonb_ops` and `jsonb_path_ops`?"
- **Winning Answer**: "`jsonb_ops` (default) creates index entries for every key, value, and path element, supporting key-exists (`?`), any-key (`?|`), and containment (`@>`). `jsonb_path_ops` only hashes complete path-value pairs (e.g., hash of `{"screen": "OLED"}`); it is **up to 60% smaller and faster**, but supports **only** the containment operator (`@>`)."

---

#### Q15: Connection Pooling Internals: HikariCP & PgBouncer

##### 1. Exact Scenario & Question
Your microservice cluster scales to 150 pods, each configured with a default HikariCP pool of 50 connections ($150 \times 50 = 7,500$ connections). The PostgreSQL server hits 100% CPU and crashes. Explain why PostgreSQL cannot handle 7,500 connections natively, and design a two-tier pooling architecture using **HikariCP** and **PgBouncer**.

##### 2. What the Interviewer Evaluates
- PostgreSQL process model: Forked process per connection (`backend`) vs lightweight threads.
- Context-switching overhead and memory footprint of PostgreSQL backends (~10MB RAM per idle connection).
- PgBouncer pooling modes: Session vs Transaction vs Statement.

##### 3. Standout Technical Answer
**Why 7,500 Connections Crash PostgreSQL:**
Unlike MySQL or Oracle which use lightweight threads, PostgreSQL spawns a **heavyweight independent OS process** for every client connection:
1. **Memory Overhead**: Each backend process allocates ~10MB baseline RAM. 7,500 connections $\times 10\text{MB} = \mathbf{75\text{GB of RAM}}$ consumed purely by idle connection processes!
2. **Context-Switching Thrashing**: When 7,500 processes contend for 32 CPU cores, the Linux kernel spends 90% of CPU time executing **process context switches** rather than executing SQL.
3. **Lock Contention**: Internal shared buffer pool spinlocks become saturated.

**The Two-Tier Enterprise Solution:**
- **Tier 1 (Application-Side: HikariCP)**:
  - Shrink pool size! HikariCP's golden rule formula:
    $$\text{Connections} = (\text{CPU Cores} \times 2) + \text{Disk Spindle Count}$$
  - Set `maximumPoolSize = 10` per pod.
- **Tier 2 (Database-Side: PgBouncer Proxy)**:
  - Sits as a lightweight multiplexing proxy between application pods and PostgreSQL.
  - 150 pods $\times$ 10 connections = 1,500 client connections hit PgBouncer.
  - PgBouncer multiplexes these down to **50 real backend connections** to PostgreSQL!

```ini
# /etc/pgbouncer/pgbouncer.ini
[databases]
orderdb = host=127.0.0.1 port=5432 dbname=orderdb

[pgbouncer]
listen_port = 6432
listen_addr = *
auth_type = scram-sha-256
# CRITICAL: Transaction mode allocates server connection only for active transaction!
pool_mode = transaction
max_client_conn = 5000
default_pool_size = 50
reserve_pool_size = 5
```

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "What common PostgreSQL features break when you run PgBouncer in `pool_mode = transaction`?"
- **Winning Answer**: "Session-level state features break because subsequent queries in the same client session may execute across different server backend connections. Features broken include: `LISTEN / NOTIFY`, temporary tables (`CREATE TEMP TABLE`), `SET SESSION AUTHORIZATION`, and session-level advisory locks (`pg_advisory_lock`). The fix is to use transaction-level alternatives (e.g., `SET LOCAL` and `pg_advisory_xact_lock`)."

---

### Tier 2: Intermediate Enterprise Persistence & Concurrency (Q16 – Q30)

#### Q16: Locking Mechanics: `SELECT ... FOR UPDATE` vs `SKIP LOCKED`

##### 1. Exact Scenario & Question
You are building a distributed message queue worker system inside PostgreSQL where 20 concurrent worker pods pull jobs from a `job_queue` table. How do you prevent workers from processing the same job simultaneously without causing lock contention and query blocking? Contrast `FOR UPDATE`, `FOR UPDATE NOWAIT`, and `FOR UPDATE SKIP LOCKED`.

##### 2. What the Interviewer Evaluates
- Pessimistic row locking levels in PostgreSQL: `FOR UPDATE`, `FOR NO KEY UPDATE`, `FOR SHARE`, `FOR KEY SHARE`.
- Building high-throughput queue architectures in relational databases using `SKIP LOCKED`.

##### 3. Standout Technical Answer
If workers simply query `SELECT * FROM job_queue WHERE status = 'PENDING' LIMIT 1`, all 20 workers read the same row, causing duplicate executions.
- **`FOR UPDATE`**:
  - Worker 1 locks row 1. Worker 2 attempts to lock row 1 and **blocks (waits)** until Worker 1 commits. All 20 workers queue up behind each other sequentially, destroying throughput.
- **`FOR UPDATE NOWAIT`**:
  - Worker 2 attempts to lock row 1. If locked, it immediately throws an exception: `ERROR: could not obtain lock on row in relation "job_queue"`. Requires complex application retries.
- **`FOR UPDATE SKIP LOCKED` (The High-Throughput Queue Pattern)**:
  - Worker 1 locks row 1.
  - Worker 2 executes the query: the database engine **skips locked rows** and immediately locks and returns the next unlocked row (row 2)!
  - All 20 workers process independent jobs concurrently with zero blocking, zero lock contention, and zero exceptions!

```sql
-- Production Queue Consumer Worker Query:
BEGIN;
SELECT id, payload
FROM job_queue
WHERE status = 'PENDING'
ORDER BY priority DESC, created_at ASC
LIMIT 1
FOR UPDATE SKIP LOCKED; -- Locks next available job instantly!

-- Worker processes job in application code...

UPDATE job_queue SET status = 'COMPLETED', updated_at = NOW() WHERE id = :job_id;
COMMIT;
```

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "What is the difference between `FOR UPDATE` and `FOR NO KEY UPDATE`?"
- **Winning Answer**: "`FOR UPDATE` acquires a row lock that blocks concurrent foreign key checks on referenced tables. `FOR NO KEY UPDATE` acquires a weaker lock that allows concurrent foreign key validations to proceed unimpeded, significantly reducing lock contention when updating rows whose primary keys are not changing."

---

#### Q17: Deadlock Detection & Lock Graphs

##### 1. Exact Scenario & Question
Two transactions update customer accounts:
- Transaction 1: Updates Account A, then updates Account B.
- Transaction 2: Updates Account B, then updates Account A.
Explain the internal deadlock detection algorithm in PostgreSQL, the role of `deadlock_timeout`, and how to eliminate deadlocks permanently.

##### 2. What the Interviewer Evaluates
- Directed lock wait-for graphs and cycle detection algorithms (Tarjan's algorithm).
- Lock ordering best practices in application architecture.

##### 3. Standout Technical Answer
**The Deadlock Cycle:**
1. Tx 1 acquires exclusive row lock on Account A.
2. Tx 2 acquires exclusive row lock on Account B.
3. Tx 1 requests lock on Account B $\longrightarrow$ **Blocks waiting for Tx 2**.
4. Tx 2 requests lock on Account A $\longrightarrow$ **Blocks waiting for Tx 1**.
Neither transaction can ever make progress.

**PostgreSQL Deadlock Detection:**
1. When a transaction blocks on a lock, it does not run the deadlock detector immediately (running graph cycle detection on every lock wait would kill CPU).
2. It starts a countdown timer: **`deadlock_timeout`** (default **1000ms**).
3. If the lock is still blocked after 1 second, PostgreSQL wakes the deadlock detector thread.
4. The detector constructs a **Directed Wait-For Graph** of transactions and locks.
5. If a cycle is discovered ($T_1 \rightarrow T_2 \rightarrow T_1$), PostgreSQL breaks the deadlock by terminating the youngest transaction with:
   ```text
   ERROR: 40P01: deadlock detected
   Detail: Process 1421 waits for ExclusiveLock on tuple; blocked by process 1422.
   Process 1422 waits for ExclusiveLock on tuple; blocked by process 1421.
   ```

**Architectural Prevention (Deterministic Lock Ordering):**
Enforce that all transactions lock resources in a **strictly identical global order** (e.g., sorted by numerical Primary Key):
```java
// Always sort IDs before locking multiple accounts!
List<Long> accountIds = Arrays.asList(accountIdA, accountIdB);
Collections.sort(accountIds); // Enforces deterministic lock ordering!

for (Long id : accountIds) {
    accountRepository.findAndLockById(id); // Both transactions lock A, then B!
}
```
Because both transactions attempt to acquire Account A first, Transaction 2 blocks *before* locking Account B, completely preventing circular wait cycles!

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "Why should you NOT set `deadlock_timeout` to 5 milliseconds to detect deadlocks faster?"
- **Winning Answer**: "Because building and traversing the Directed Wait-For Graph acquires shared lwlocks across the entire PostgreSQL lock manager. Running deadlock detection every 5ms on a high-concurrency database causes extreme CPU spinlock contention and kills query throughput."

---

#### Q18: Replication Architectures: Physical Streaming vs Logical Replication

##### 1. Exact Scenario & Question
Compare **Physical Streaming Replication** with **Logical Replication**. An enterprise needs to replicate data from an on-premises PostgreSQL 12 database to an AWS PostgreSQL 16 database, while transforming sensitive PII columns. Which replication model must you choose and why?

##### 2. What the Interviewer Evaluates
- Byte-level WAL streaming vs WAL logical decoding (pgoutput plugin).
- Cross-major version upgrade capabilities.
- CDC (Change Data Capture) integration with Apache Kafka / Debezium.

##### 3. Standout Technical Answer
- **Physical Streaming Replication**:
  - Replicates exact 8KB disk block bytes via WAL.
  - The replica is an **exact bit-for-bit clone** of the primary.
  - *Constraints*: Primary and replica **must be on the exact same major version** of PostgreSQL (e.g., both 15) and identical OS architectures. Replica is strictly read-only.
- **Logical Replication (Mandatory for this Scenario)**:
  - Uses the `pgoutput` plugin to decode WAL records into logical SQL row events (INSERT, UPDATE, DELETE).
  - Operates on a **Publish / Subscribe** model (`CREATE PUBLICATION` / `CREATE SUBSCRIPTION`).
  - *Advantages for this Scenario*:
    1. **Cross-Major Version**: Replicates seamlessly from PostgreSQL 12 to 16.
    2. **Selective Replication**: Allows replicating specific tables while excluding non-essential tables.
    3. **Schema Transformation**: Supports replicating to different column layouts and integrating with Kafka via Debezium for real-time PII masking.
    4. **Read-Write Replica**: The subscriber database can have its own local indexes, triggers, and accept independent local writes.

```sql
-- On Source PostgreSQL 12 (Publisher):
CREATE PUBLICATION customer_pub FOR TABLE customers (id, name, email);

-- On Destination PostgreSQL 16 (Subscriber):
CREATE SUBSCRIPTION customer_sub
CONNECTION 'host=onprem.db port=5432 dbname=prod user=rep_user password=secret'
PUBLICATION customer_pub;
```

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "What critical prerequisite table configuration is required for a table to support `UPDATE` and `DELETE` events in Logical Replication?"
- **Winning Answer**: "The table **must have a Primary Key or a Replica Identity** configured (`REPLICA IDENTITY FULL` or unique index). Logical replication requires a unique identifier to locate the corresponding row on the subscriber to apply the update or delete."

---

#### Q19: High Availability: Patroni, etcd & Split-Brain Fencing

##### 1. Exact Scenario & Question
Describe an enterprise High Availability (HA) architecture for PostgreSQL using **Patroni** and **etcd**. What happens when a network partition isolates the Primary database from its replicas, and how does Patroni prevent **Split-Brain disaster**?

##### 2. What the Interviewer Evaluates
- Distributed Consensus Stores (DCS) for leader election.
- Split-brain failure mode (two primaries accepting conflicting writes).
- Fencing mechanisms: Watchdog / STONITH (Shoot The Other Node In The Head).

##### 3. Standout Technical Answer

```
                   Patroni + etcd HA Topology
                   
                 [ etcd Cluster (3 Nodes) ]
                 Leader Lease: Key 'service/leader'
                             │
            ┌────────────────┼────────────────┐
            ▼                                 ▼
    [ Patroni Node 1 ]                [ Patroni Node 2 ]
    PostgreSQL (PRIMARY)              PostgreSQL (STANDBY)
    Holds etcd lease (TTL: 10s)       Watches etcd lease
            │                                 ▲
            └────── Streaming Replication ────┘
```

**Split-Brain Prevention Sequence:**
1. **Normal Operation**: Patroni Node 1 holds a leader lock key in `etcd` with a 10-second Time-To-Live (TTL). It continuously renews the lease heartbeat every 2 seconds.
2. **Network Partition Occurs**: Node 1 is severed from etcd and Node 2.
3. **Lease Expiration**: Node 1 cannot reach etcd to renew its lease. After 10 seconds, the etcd lease expires.
4. **Primary Demotion (Self-Fencing)**: Patroni Node 1 detects that it lost its etcd lease. It **immediately demotes its local PostgreSQL instance to read-only mode** or issues an OS kernel watchdog trigger to reboot itself.
5. **Failover**: Node 2 observes the expired lease, checks WAL replay position to ensure it is the most up-to-date standby, claims the etcd leader lease, and promotes itself to the new Primary.
6. **Result**: Zero split-brain. At no point are two nodes accepting writes simultaneously.

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "What is the Linux Watchdog device and why does Patroni use it for hardware fencing?"
- **Winning Answer**: "A Linux software/hardware watchdog (`/dev/watchdog`) is a kernel timer that reboots the server if it is not pinged regularly. Patroni pings the watchdog while holding the etcd lease. If Patroni hangs, freezes in a JVM pause, or loses etcd, the watchdog timer expires and the Linux kernel immediately resets the machine at the hardware level, guaranteeing the partitioned primary cannot write stale data."

---

#### Q20: Zero-Downtime Schema Migrations: Safe DDL Patterns

##### 1. Exact Scenario & Question
You are adding a new required column `account_tier VARCHAR(20) NOT NULL DEFAULT 'STANDARD'` to a table with 200 million rows in production. Why does running this naive DDL freeze your application with `504 Gateway Timeouts`, and what is the safe, zero-downtime execution pattern?

##### 2. What the Interviewer Evaluates
- Table lock levels: `AccessExclusiveLock` vs table rewrites.
- The 4-step safe migration pattern in PostgreSQL.

##### 3. Standout Technical Answer
**Why the Naive DDL Fails:**
Prior to PostgreSQL 11, adding a column with a default value rewrote every single 8KB heap page on disk.
Even in modern PostgreSQL (which stores default values in catalog metadata), the `ALTER TABLE` statement requires an **`AccessExclusiveLock`**.
If an active reporting query is running, the `ALTER TABLE` statement blocks waiting for it. All subsequent `SELECT` queries queue up behind the `ALTER TABLE`, saturating the connection pool and causing complete service failure.

**The Production Zero-Downtime Runbook:**
1. **Set Lock Timeout**: Prevent migration from blocking queue:
   ```sql
   SET lock_timeout = '2s'; -- Fails fast rather than blocking the application!
   ```
2. **Add Column as Nullable**:
   ```sql
   ALTER TABLE accounts ADD COLUMN account_tier VARCHAR(20);
   ```
3. **Add CHECK Constraint as NOT VALID (Instantaneous, zero lock wait!)**:
   ```sql
   ALTER TABLE accounts ADD CONSTRAINT chk_tier_not_null
   CHECK (account_tier IS NOT NULL) NOT VALID;
   ```
4. **Backfill Existing Rows Asynchronously in Batches**:
   ```sql
   -- Run in background loop (5,000 rows at a time with pauses)
   UPDATE accounts SET account_tier = 'STANDARD'
   WHERE id BETWEEN 1 AND 5000 AND account_tier IS NULL;
   ```
5. **Validate Constraint Concurrently (Zero Table Lock!)**:
   ```sql
   -- Validates existing rows without acquiring AccessExclusiveLock!
   ALTER TABLE accounts VALIDATE CONSTRAINT chk_tier_not_null;
   ```

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "How do you create an index on a 100-million-row production table without blocking ongoing writes?"
- **Winning Answer**: "Always use `CREATE INDEX CONCURRENTLY`. Standard `CREATE INDEX` acquires a `ShareLock`, which allows reads but blocks all `INSERT`, `UPDATE`, and `DELETE` operations. `CONCURRENTLY` performs two table scans and builds the index in the background without blocking writes (at the cost of taking ~2x–3x longer to complete)."

---

### Tier 3: Advanced Operations, Performance & NoSQL (Q31 – Q45)

#### Q31: MongoDB vs PostgreSQL JSONB: Architecture & Storage Engines

##### 1. Exact Scenario & Question
Compare **PostgreSQL JSONB** with **MongoDB** (WiredTiger engine). In what specific enterprise scenarios is MongoDB's native document model superior to PostgreSQL JSONB, and when is PostgreSQL JSONB the better architectural choice?

##### 2. What the Interviewer Evaluates
- WiredTiger B-tree engine vs PostgreSQL MVCC append-only heap.
- In-place document updates vs PostgreSQL write amplification.
- Native horizontal sharding.

##### 3. Standout Technical Answer
- **When MongoDB Wins**:
  1. **High-Frequency In-Place Updates**: MongoDB's WiredTiger engine can update a single field inside an embedded document **in-place** on disk. In PostgreSQL, updating a single key in a 50KB JSONB document rewrites the **entire row tuple** in the heap via MVCC, generating massive WAL overhead and dead tuple bloat.
  2. **Native Horizontal Auto-Sharding**: MongoDB provides built-in distributed sharding (`mongos` routers and chunk balancing) out of the box. PostgreSQL requires third-party extensions (Citus) or manual partition routing.
- **When PostgreSQL JSONB Wins**:
  1. **Hybrid Relational + Document Needs**: Joining structured relational tables (orders, customers) with flexible semi-structured attributes in a single ACID transaction.
  2. **Strict Transactional Guarantees**: Mature MVCC, multi-table foreign keys, complex window functions, and rich analytical extensions (TimescaleDB, PostGIS).

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "What is the MongoDB Unbounded Array Anti-Pattern?"
- **Winning Answer**: "Storing an unbounded array inside a document (e.g., embedding all historical customer comments inside a `User` document). As the array grows, the document exceeds MongoDB's **16MB maximum document size limit**, WiredTiger must repeatedly relocate the document on disk, and index update performance degrades exponentially."

---

#### Q32: MongoDB Indexing: The ESR (Equality, Sort, Range) Rule

##### 1. Exact Scenario & Question
You are optimizing a query in MongoDB: `db.orders.find({ status: "PAID", created_at: { $gte: ISODate("2026-09-01") } }).sort({ total_amount: -1 })`. How do you structure the compound index using the **ESR Rule** to achieve an index-covered sort and eliminate in-memory sorting?

##### 2. What the Interviewer Evaluates
- Compound index ordering in B-tree document stores.
- ESR Rule: Equality -> Sort -> Range.
- Preventing `Blocking Sort` in MongoDB.

##### 3. Standout Technical Answer
**The ESR Rule Mandate:**
1. **E (Equality)**: Fields queried by exact equality matches (`status: "PAID"`).
2. **S (Sort)**: Fields used in the `sort()` clause (`total_amount: -1`).
3. **R (Range)**: Fields queried by range operators (`created_at: { $gte: ... }`).

```javascript
// ✅ Correct Compound Index following ESR Rule:
db.orders.createIndex({ 
  status: 1,         // [E] Equality
  total_amount: -1,  // [S] Sort
  created_at: 1      // [R] Range
});
```

**Why Ordering Matters:**
If you place Range before Sort (`{ status: 1, created_at: 1, total_amount: -1 }`), the index engine filters by `created_at` range first. Once a range is evaluated, the index loses its sorted order on subsequent fields. MongoDB is forced to load all matching documents into memory and execute an **in-memory blocking sort** (which fails if it exceeds the 100MB RAM limit). Following ESR guarantees the database walks the index pre-sorted.

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "What happens in MongoDB if an in-memory sort exceeds 100MB of RAM?"
- **Winning Answer**: "The query aborts immediately with: `Executor error during query: Sort exceeded memory limit of 104857600 bytes`. Unless `allowDiskUse(true)` is explicitly passed, the operation fails."

---

#### Q33: Redis Architecture: Event Loop & Invalidation Strategies

##### 1. Exact Scenario & Question
Why is Redis single-threaded for command execution, yet capable of processing 100,000 requests per second? Contrast the **Cache-Aside**, **Write-Through**, and **Write-Behind (Write-Back)** caching patterns, and explain how to mitigate a **Cache Stampede (Thundering Herd)**.

##### 2. What the Interviewer Evaluates
- Non-blocking I/O multiplexing (`epoll` / `kqueue`).
- Cache consistency patterns and trade-offs.
- Cache Stampede mitigation: Mutex locking vs probabilistic early expiration (XFetch algorithm).

##### 3. Standout Technical Answer
**Why Single-Threaded Redis is Blazing Fast:**
1. **Zero Context Switching**: Thread context switching consumes CPU registers and invalidates CPU L1/L2 caches. Single-threaded execution eliminates this overhead entirely.
2. **Zero Lock Contention**: No mutexes, spinlocks, or read-write locks required on internal memory structures.
3. **I/O Multiplexing (`epoll`)**: A single thread uses `epoll` to monitor thousands of connected TCP sockets, processing ready read/write events in a tight, non-blocking event loop directly in RAM.

**Caching Patterns:**
- **Cache-Aside (Lazy Loading)**: App checks Redis. On miss, reads DB, writes to Redis, returns data. *Trade-off*: Stale data if DB is updated out-of-band.
- **Write-Through**: App writes to cache; cache synchronously writes to DB before returning. *Advantage*: Consistency. *Trade-off*: High write latency.
- **Write-Behind (Write-Back)**: App writes to cache immediately; cache asynchronously batches writes to DB. *Advantage*: Ultra-fast writes. *Risk*: Data loss if Redis crashes before DB sync.

**Mitigating Cache Stampede (Thundering Herd):**
When an expensive cached item (e.g., homepage product feed) expires, 10,000 concurrent requests miss simultaneously and slam the database.
- *Fix: Distributed Mutex with Redis `SET NX`*:
  ```java
  String data = redis.get("homepage_feed");
  if (data == null) {
      // Acquire distributed lock: Only 1 thread gets permission to rebuild cache!
      if (redis.set("lock:homepage", "true", "NX", "EX", 10)) {
          try {
              data = database.calculateExpensiveFeed();
              redis.set("homepage_feed", data, "EX", 300);
          } finally {
              redis.del("lock:homepage");
          }
      } else {
          // Other 9,999 threads sleep 50ms and read the refreshed cache!
          Thread.sleep(50);
          return redis.get("homepage_feed");
      }
  }
  return data;
  ```

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "In Redis, what is the fatal risk of running the `KEYS *` command in production?"
- **Winning Answer**: "Because Redis is single-threaded, `KEYS *` performs an $O(N)$ synchronous scan across all millions of keys in memory. It **blocks the entire Redis event loop for seconds or minutes**, freezing all client commands cluster-wide and triggering failovers. You must use the non-blocking cursor-based `SCAN` command instead."

---

#### Q34: Redis Eviction Policies & Memory Pressure

##### 1. Exact Scenario & Question
A Redis instance with `maxmemory 8gb` fills up to 100% capacity. Explain how Redis handles writes under `allkeys-lru`, `volatile-lru`, `allkeys-lfu`, and `noeviction`. How does the approximation algorithm for LRU work in Redis?

##### 2. What the Interviewer Evaluates
- Eviction policy mechanics.
- LFU (Least Frequently Used) vs LRU (Least Recently Used).
- Memory management without full pointer-linked LRU lists.

##### 3. Standout Technical Answer
When `maxmemory` is reached, Redis evaluates its configured eviction policy:
- **`noeviction`** (Default): Refuses all new writes. Any command that increases memory (`SET`, `HSET`, `LPUSH`) returns: `OOM command not allowed when used memory > 'maxmemory'`. Read queries still succeed.
- **`allkeys-lru`**: Evicts least recently used keys across the entire dataset. Ideal for general-purpose caching.
- **`volatile-lru`**: Evicts least recently used keys, but **only among keys with an explicit TTL (expiration) set**. Permanent keys are preserved.
- **`allkeys-lfu`**: Evicts least *frequently* used keys using a logarithmic access counter. Prevents evicting a popular key that was simply not accessed in the last 2 minutes.

**The Approximated LRU Algorithm:**
True LRU requires maintaining a double-linked list of all keys and moving a key to the head on every single read, which consumes massive memory and creates lock contention.
Instead, Redis uses an **approximated sample algorithm**:
- It randomly samples $K$ keys (default $K=5$).
- Evicts the key with the oldest idle time among those 5 keys.
- Mathematically, testing 5 random keys produces a behavior curve almost indistinguishable from true LRU while consuming zero extra memory pointers!

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "If an item in Redis reaches its TTL, is it deleted from memory at that exact millisecond?"
- **Winning Answer**: "No. Redis uses a combination of **Passive and Active expiration**. Passively, an expired key is deleted only when a client attempts to read it. Actively, Redis tests 20 random keys with TTLs 10 times per second and purges expired ones. Expired keys can linger in memory until touched or sampled."

---

#### Q35: TimescaleDB: Hypertables & Automated Columnar Compression

##### 1. Exact Scenario & Question
An IoT platform ingests 200 million sensor metrics per day into PostgreSQL. Disk storage usage reaches 2TB per month, and query latency degrades. Explain how **TimescaleDB** transforms PostgreSQL into a high-performance time-series database using **Hypertables** and **Automated Chunk Compression**.

##### 2. What the Interviewer Evaluates
- Extension architecture in PostgreSQL.
- Two-dimensional chunk partitioning (time + space).
- Row-to-columnar compression algorithms (Delta-of-delta, XOR Gorillas, Run-length encoding).

##### 3. Standout Technical Answer
TimescaleDB operates as a native PostgreSQL extension:
1. **Hypertables**: Exposes a standard SQL table abstraction that automatically partitions incoming data into internal 2-dimensional **Chunks** based on time intervals (e.g., 1 chunk per day).
2. **Chunk Execution**: Queries with time boundaries route strictly to relevant chunks, keeping active B-tree indexes small enough to fit completely in `shared_buffers` RAM.

**Automated Columnar Compression (90%+ Disk Savings):**
Relational rows are row-oriented (uncompressed). After a time window passes (e.g., data older than 7 days):
- TimescaleDB's compression engine converts old chunks from row-store format into **compressed columnar format**:
  - Timestamps: Compressed using **Delta-of-Delta** compression.
  - Floating-point metrics: Compressed using **Gorilla XOR** compression.
  - Device IDs / Statuses: Compressed using **Dictionary / Run-Length Encoding (RLE)**.

```sql
-- Convert standard table to Hypertable:
SELECT create_hypertable('sensor_readings', 'time');

-- Enable columnar compression on data older than 7 days:
ALTER TABLE sensor_readings SET (
  timescaledb.compress,
  timescaledb.compress_segmentby = 'device_id',
  timescaledb.compress_orderby = 'time DESC'
);

SELECT add_compression_policy('sensor_readings', INTERVAL '7 days');
```
Storage drops from **2TB down to ~150GB per month**, and analytical queries (`AVG(cpu) WHERE time > NOW() - INTERVAL '30 days'`) execute 20x faster because the columnar layout reads only the target column bytes from disk.

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "Can you execute standard SQL `UPDATE` or `DELETE` statements on compressed chunks in TimescaleDB?"
- **Winning Answer**: "Prior to TimescaleDB 2.1+, updates on compressed chunks were forbidden. In modern versions, TimescaleDB automatically decompresses target rows behind the scenes, executes the update, and re-compresses, though it incurs an I/O penalty."

---

### Tier 4: Elite Architecture, Low-Level Storage & Edge Cases (Q46 – Q50)

#### Q46: PostgreSQL 8KB Page Layout & Heap-Only Tuples (HOT)

##### 1. Exact Scenario & Question
Explain the physical byte layout of an 8KB PostgreSQL heap page. What is the **Heap-Only Tuples (HOT)** optimization, and why does an `UPDATE` that modifies an indexed column destroy HOT efficiency?

##### 2. What the Interviewer Evaluates
- Binary page anatomy: `PageHeaderData`, `ItemIdData` (line pointers), lower/upper free space hole, tuple body.
- HOT update mechanics: Eliminating index updates and vacuuming overhead.

##### 3. Standout Technical Answer

```
                        PostgreSQL 8KB Page Physical Layout
+-------------------------------------------------------------------------------+
|  PageHeaderData (24 bytes: LSN, checksum, lower, upper pointers, special)     |
+-------------------------------------------------------------------------------+
|  ItemIdData Line Pointers Array: [ (offset, len) | (offset, len) | ... ]       |
|  (Grows DOWNWARD: pointer 1, pointer 2, pointer 3...)                         |
|  ────────────────────────────► lower pointer offset                           |
|                                                                               |
|                      [ Unallocated Free Space Hole ]                          |
|                                                                               |
|  ◄─────────────────────────── upper pointer offset                            |
|  (Grows UPWARD: tuple data filled from bottom of page)                        |
|  [ Tuple 2 Data: xmin, xmax, infomask, user columns ]                         |
|  [ Tuple 1 Data: xmin, xmax, infomask, user columns ]                         |
+-------------------------------------------------------------------------------+
|  Special Space (Index-specific page data, 0 bytes in heap)                    |
+-------------------------------------------------------------------------------+
```

**Heap-Only Tuples (HOT) Optimization:**
Standard `UPDATE` creates a new tuple version and **must insert a new pointer into EVERY index on the table**, causing massive index bloat.
**HOT Update** bypasses index updates:
- **Conditions**:
  1. No column modified by the `UPDATE` is indexed.
  2. The same 8KB page has enough free space to hold the new tuple version.
- **The HOT Link**:
  - The old tuple's line pointer is turned into an internal pointer chain pointing directly to the new tuple in the same page.
  - Indexes continue pointing to the original Line Pointer 1!
  - When an index scan hits Line Pointer 1, it follows the internal HOT link directly to Tuple 2 **without modifying a single index entry**.
  - Pruning occurs automatically during normal reads, eliminating bloat.

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "What table parameter must you tune to guarantee that pages have enough free space for HOT updates?"
- **Winning Answer**: "The **`fillfactor`** parameter. By default, `fillfactor = 100` (tables fill 100% of the 8KB page on insert). For tables experiencing heavy updates, set `WITH (fillfactor = 80)` to reserve 20% of every page for future HOT update tuple versions."

---

#### Q47: Disaster Recovery: Point-in-Time Recovery (PITR)

##### 1. Exact Scenario & Question
At 14:15:22 UTC, an engineer accidentally executed `DROP TABLE users;` on the production database. The last physical base backup was taken at midnight (00:00:00 UTC). Walk through the exact operational recovery steps to perform a **Point-in-Time Recovery (PITR)** to 14:15:20 UTC without losing any prior transactions.

##### 2. What the Interviewer Evaluates
- Combining physical base backups (`pg_basebackup`) with continuous WAL archiving.
- Recovery target configuration (`recovery_target_time`, `recovery_target_action`).

##### 3. Standout Technical Answer
**PITR Operational Procedure:**
1. **Halt Database Immediately**: Prevent any further writes or log recycling.
2. **Preserve Current WAL Logs**: Copy all active WAL segments from `pg_wal/` to a safe backup directory.
3. **Restore Physical Base Backup**:
   - Wipe the corrupted data directory `/var/lib/postgresql/data/`.
   - Untar the midnight physical base backup into the data directory.
4. **Configure Recovery Target (`postgresql.auto.conf` / `recovery.signal`)**:
   ```ini
   # Create empty marker file: touch /var/lib/postgresql/data/recovery.signal

   # In postgresql.conf:
   restore_command = 'cp /mnt/wal_archive/%f %p'
   recovery_target_time = '2026-09-10 14:15:20 UTC' # 2 seconds BEFORE the DROP TABLE!
   recovery_target_action = 'promote' # Automatically promote to read-write once reached
   recovery_target_inclusive = false
   ```
5. **Start PostgreSQL**:
   - The engine detects `recovery.signal`.
   - It replays WAL files from midnight continuously, restoring all 14 hours of transactions.
   - It reaches `14:15:20 UTC` and **halts replay immediately before the DROP TABLE statement**.
   - Promotes the database to live operational status with 100% of data restored!

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "Can you perform Point-in-Time Recovery if you only have daily `pg_dump` logical backup files?"
- **Winning Answer**: "No! `pg_dump` is a logical SQL snapshot taken at a single point in time. It does not record transaction timestamps or continuous WAL delta streams. PITR **strictly requires** physical continuous WAL archiving (`archive_mode = on`)."

---

#### Q48: Cassandra / ScyllaDB Wide-Column Architecture & LSM-Trees

##### 1. Exact Scenario & Question
Contrast the storage architecture of an **LSM-Tree (Log-Structured Merge-Tree)** used by Apache Cassandra and ScyllaDB with PostgreSQL's B-Tree engine. How do Partition Keys, Clustering Columns, and Tunable Consistency (`QUORUM`) operate during write operations?

##### 2. What the Interviewer Evaluates
- Write path: Memtable -> CommitLog -> SSTable flush -> Background compaction.
- Distributed hashing via Murmur3 partition token rings.

##### 3. Standout Technical Answer
- **PostgreSQL (B-Tree & Heap)**:
  - Writes modify existing 8KB pages on disk (random I/O). Read-optimized.
- **Cassandra / ScyllaDB (LSM-Tree)**:
  - **Write-Optimized**: All writes are purely **append-only sequential I/O**.
  - Write path:
    1. Writes record sequentially to the on-disk **CommitLog** (durability).
    2. Writes to an in-memory sorted structure: **Memtable**.
    3. The write returns success in < 1ms! Zero disk seeks.
    4. When Memtable fills, it flushes to disk as an immutable **SSTable (Sorted String Table)**.
    5. Background **Compaction** merges overlapping SSTables and purges tombstoned records.

**Primary Key Architecture:**
```sql
PRIMARY KEY ((user_id), created_at, event_id)
--           └────────┘  └──────────────────┘
--         Partition Key    Clustering Columns
```
- **Partition Key (`user_id`)**: Hashed via Murmur3 to determine which physical nodes in the cluster own the data.
- **Clustering Columns (`created_at`, `event_id`)**: Determines the physical sort order of rows *within* the partition on disk.

**Tunable Consistency:**
In a cluster with Replication Factor $RF = 3$:
- `Write: LOCAL_QUORUM` ($\lfloor 3/2 \rfloor + 1 = 2$ nodes must acknowledge write).
- `Read: LOCAL_QUORUM` (2 nodes must respond).
- Since $\text{Read Quorum} + \text{Write Quorum} = 4 > RF (3)$, the client is mathematically guaranteed to read the latest written data (**Strong Consistency**)!

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "What is a Tombstone in Cassandra and how does deleting millions of rows cause query timeouts?"
- **Winning Answer**: "Because SSTables are immutable, a `DELETE` cannot erase data on disk. Instead, it writes a marker called a **Tombstone**. When a query scans a range, it must read and process all tombstones in memory. If a query encounters $> 100,000$ tombstones, Cassandra aborts with `TombstoneOverwhelmingException` to prevent node OOM."

---

#### Q49: Extreme Transaction Wraparound Disaster Recovery in Single-User Mode

##### 1. Exact Scenario & Question
A production PostgreSQL database has completely shutdown with `FATAL: database is not accepting commands to avoid wraparound data loss`. Standard client connections are rejected. How do you recover the database using **PostgreSQL Single-User Mode**?

##### 2. What the Interviewer Evaluates
- Emergency low-level database recovery when postmaster refuses all connections.
- Executing `VACUUM FREEZE` in single-user mode.

##### 3. Standout Technical Answer
When the wraparound emergency threshold is breached, the `postmaster` daemon refuses all incoming TCP connections.

**Emergency Standalone Protocol:**
1. **Stop the PostgreSQL Service**:
   ```bash
   sudo systemctl stop postgresql
   ```
2. **Launch Standalone Single-User Mode**:
   Execute the core `postgres` binary directly as the `postgres` OS user, bypassing the postmaster network listener:
   ```bash
   sudo -u postgres postgres --single -D /var/lib/postgresql/data prod_db
   ```
3. **Execute Emergency Vacuum Freeze**:
   Inside the single-user shell:
   ```sql
   VACUUM FREEZE VERBOSE;
   ```
4. Once completed, exit the shell (`Ctrl+D`).
5. Restart standard PostgreSQL service:
   ```bash
   sudo systemctl start postgresql
   ```
The database is back online with transaction ages reset safely below the threshold.

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "Can Autovacuum run while the database is in Single-User Mode?"
- **Winning Answer**: "No. Single-user mode runs as a single standalone thread with all background processes (Autovacuum, Checkpointer, Bgwriter, Stats collector) completely disabled. All operations must be typed manually."

---

#### Q50: Enterprise Database Migration: Oracle PL/SQL to PostgreSQL

##### 1. Exact Scenario & Question
Architect a large-scale database migration moving a core banking ledger from **Oracle Database 19c** to **PostgreSQL**. Detail data type translations, handling Oracle `VARCHAR2(empty string is NULL)` quirks, and replacing Oracle Packages and Autonomous Transactions (`PRAGMA AUTONOMOUS_TRANSACTION`).

##### 2. What the Interviewer Evaluates
- Deep differences between Oracle and PostgreSQL semantics.
- `dblink` / foreign data wrappers vs AWS DMS.
- Emulating Oracle autonomous transactions.

##### 3. Standout Technical Answer
**Key Architectural Semantic Traps:**
1. **Empty Strings vs NULL**:
   - In Oracle, `''` (empty string) is treated as `NULL`.
   - In PostgreSQL, `''` is a valid, non-null string of zero length! A query `WHERE col IS NULL` in Postgres will **miss** empty string records!
   - *Fix*: Enforce database check constraints or rewrite queries using `NULLIF(col, '')`.
2. **Data Type Mappings**:
   - Oracle `NUMBER` $\longrightarrow$ PostgreSQL `DECIMAL` or `BIGINT`.
   - Oracle `VARCHAR2(100)` $\longrightarrow$ PostgreSQL `VARCHAR(100)` or `TEXT`.
   - Oracle `SYSDATE` $\longrightarrow$ PostgreSQL `CURRENT_TIMESTAMP` or `NOW()`.
3. **Replacing `PRAGMA AUTONOMOUS_TRANSACTION`**:
   - Oracle allows a function to commit an audit log independently without committing the outer parent transaction.
   - PostgreSQL functions execute inside the caller's transaction context.
   - *Solution*: Use **`dblink`** to open an independent loopback TCP connection to execute and commit the autonomous audit log, or move audit logging to an asynchronous messaging queue (Kafka/Outbox).

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "How do you translate Oracle Packages (which group types, variables, and procedures into a shared namespace) into PostgreSQL?"
- **Winning Answer**: "PostgreSQL does not have a native `PACKAGE` object. The standard architectural translation is to create a dedicated **PostgreSQL Schema** (`CREATE SCHEMA my_package;`) and place the functions and procedures inside that schema (`my_package.my_function()`), using schema-level privileges to control access."

---

## Section 2: Beginner Mistakes & Anti-Patterns

### ❌ Mistake 1: Running `SELECT *` in Production APIs

```sql
-- ❌ FATAL ANTI-PATTERN: Blindly selecting all columns
SELECT * FROM users WHERE email = 'test@company.com';
```
💥 **Why It Fails**: If the table contains large text/JSONB columns or TOAST data, `SELECT *` fetches megabytes of unnecessary data across the network. Furthermore, it completely prevents the query planner from using **Index-Only Scans**.
```sql
-- ✅ PRODUCTION HARDENED: Select ONLY the required columns
SELECT id, username, password_hash FROM users WHERE email = 'test@company.com';
```
🧠 **Lesson**: Explicitly specify column names. Enable Index-Only Scans where all requested data is satisfied directly from the B-Tree index without touching the heap.

---

### ❌ Mistake 2: Missing Index on Foreign Keys

```sql
-- ❌ ANTI-PATTERN: Foreign Key created without an Index
CREATE TABLE order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT REFERENCES orders(id) -- PostgreSQL does NOT index this automatically!
);
```
💥 **Why It Fails**: Unlike MySQL, PostgreSQL does **NOT** automatically create an index on Foreign Key columns. Deleting a row from `orders` executes a **full sequential scan** on `order_items` to verify referential integrity, locking the entire table.
```sql
-- ✅ PRODUCTION FIX: Always index Foreign Keys explicitly
CREATE INDEX idx_order_items_order_id ON order_items(order_id);
```
🧠 **Lesson**: Always create a matching B-Tree index on all Foreign Key columns.

---

### ❌ Mistake 3: Storing Timestamps without Timezone (`TIMESTAMP` vs `TIMESTAMPTZ`)

```sql
-- ❌ DANGEROUS: Unzoned timestamp
created_at TIMESTAMP DEFAULT NOW();
```
💥 **Why It Fails**: `TIMESTAMP` stores date and time stripped of timezone data. If servers change DST or data is queried across international offices, timestamps become ambiguous and inaccurate.
```sql
-- ✅ PRODUCTION FIX: Always use TIMESTAMPTZ
created_at TIMESTAMPTZ DEFAULT NOW();
```
🧠 **Lesson**: Always use `TIMESTAMPTZ`. Internally, PostgreSQL converts it to UTC and stores it as an integer offset, converting to client timezone on display.

---

### ❌ Mistake 4: Holding Database Connections During External HTTP Calls

```java
// ❌ FATAL ANTI-PATTERN: Transaction wraps long-lived external REST call
@Transactional
public void processPayment(OrderRequest req) {
    Order order = orderRepo.save(new Order(req)); // DB op: 2ms
    stripeClient.chargeCreditCard(req.getCard()); // Network I/O: 800ms! (Holds DB connection idle!)
    order.setStatus("PAID");
    orderRepo.save(order);
}
```
💥 **Why It Fails**: The HikariCP database connection is held open and completely idle for 800ms while waiting on Stripe. Under 100 concurrent requests, the entire pool is exhausted, causing cluster-wide 503 errors.
```java
// ✅ PRODUCTION FIX: Isolate DB transaction from Network calls
public void processPayment(OrderRequest req) {
    Order order = orderService.createPendingOrder(req); // Tx 1: 5ms
    PaymentResult res = stripeClient.charge(req.getCard()); // No DB connection held!
    orderService.finalizeOrder(order.getId(), res); // Tx 2: 5ms
}
```
🧠 **Lesson**: Never execute third-party API calls, disk operations, or slow computations inside an open database transaction.

---

### ❌ Mistake 5: Using `COUNT(*)` to Check Existence

```sql
-- ❌ ANTI-PATTERN: Full table scan to check if a user exists
SELECT COUNT(*) FROM users WHERE email = 'user@company.com';
```
💥 **Why It Fails**: `COUNT(*)` counts all matching records. If 1,000 matches exist, it traverses all 1,000 index pointers.
```sql
-- ✅ PRODUCTION FIX: SELECT 1 with LIMIT 1
SELECT 1 FROM users WHERE email = 'user@company.com' LIMIT 1;
-- Or in SQL: SELECT EXISTS(SELECT 1 FROM users WHERE email = 'user@company.com');
```
🧠 **Lesson**: Use `EXISTS` or `LIMIT 1` for existence checks; it halts traversal on the very first match.

---

### ❌ Mistake 6: Pagination Using High `OFFSET` Values

```sql
-- ❌ FATAL SCALING BUG: High offset pagination
SELECT * FROM products ORDER BY id LIMIT 20 OFFSET 500000;
```
💥 **Why It Fails**: PostgreSQL must read and sort all 500,020 rows, then discard the first 500,000 rows! Query time degrades linearly as pages increase.
```sql
-- ✅ PRODUCTION FIX: Keyset (Seek) Pagination
SELECT * FROM products
WHERE id > 500000
ORDER BY id ASC
LIMIT 20;
```
🧠 **Lesson**: Use Keyset Pagination (`WHERE id > last_seen_id`) for massive datasets. It executes in constant $O(1)$ time via index seek.

---

### ❌ Mistake 7: Unbounded String Columns (`VARCHAR(255)`)

```sql
-- ❌ CARGO-CULT ANTI-PATTERN: Arbitrary VARCHAR(255) in PostgreSQL
name VARCHAR(255),
description VARCHAR(255)
```
💥 **Why It Fails**: In PostgreSQL, `VARCHAR(n)` and `TEXT` have the exact same underlying physical storage implementation. Arbitrary limits like `255` are legacy MySQL habits that cause unnecessary schema migrations when strings exceed the limit.
```sql
-- ✅ POSTGRESQL STANDARD: Use TEXT with CHECK constraints if needed
name TEXT NOT NULL,
description TEXT
```
🧠 **Lesson**: In PostgreSQL, use `TEXT` freely unless a strict business length limit is explicitly required.

---

## Section 3: Globally Reported Production Incidents & War-Room Post-Mortems

### 🚨 Incident 1: The Transaction ID Wraparound Emergency Shutdown

- **The Outage**: A Fortune 500 payment engine went completely dark. Database logs output: `FATAL: database is not accepting commands to avoid wraparound data loss`. All reads and writes failed.
- **Root Cause**: An unvacuumed table with 2 billion updates reached the transaction ID horizon. Autovacuum was misconfigured with a high `vacuum_cost_delay`, preventing it from freezing tuples before the 2.1-billion transaction limit was breached.
- **The War-Room Fix**:
  1. Booted PostgreSQL in standalone single-user mode (`postgres --single`).
  2. Executed `VACUUM FREEZE VERBOSE` across all databases.
  3. Reconfigured `autovacuum_vacuum_cost_limit = 2000` and `autovacuum_freeze_max_age = 200000000`.
- **Architectural Prevention**: Set automated Prometheus alerts on `age(datfrozenxid) > 1,000,000,000`.

---

### 🚨 Incident 2: The Cascading Connection Pool Meltdown (HikariCP Exhaustion)

- **The Outage**: An e-commerce platform crashed during a marketing campaign. API servers returned HTTP 503 `ConnectionTimeoutException: Connection is not available, request timed out after 30000ms`.
- **Root Cause**: Developers set `maximumPoolSize = 100` on 80 application pods ($80 \times 100 = 8,000$ connections). When traffic surged, 8,000 PostgreSQL backend processes saturated the database CPU with context-switching, driving server CPU to 100% and stalling all queries.
- **The War-Room Fix**:
  1. Scaled down HikariCP pool sizes from 100 to **10 connections per pod**.
  2. Deployed **PgBouncer** in transaction pooling mode to cap real server connections at 64.
  3. Query throughput immediately increased by **400%** and CPU dropped to 35%.
- **Architectural Prevention**: Never size database pools to exceed $2 \times \text{CPU cores}$ without a pooling proxy like PgBouncer.

---

### 🚨 Incident 3: The Unindexed Foreign Key Lock Cascade

- **The Outage**: A customer deletion script initiated in a microservice froze all checkout operations across the platform, generating massive lock timeouts.
- **Root Cause**: `orders` had a Foreign Key referencing `customers(id)`, but the `customer_id` column on `orders` was not indexed. When `DELETE FROM customers WHERE id = 45` executed, PostgreSQL acquired a **ShareLock on the entire `orders` table** to check for orphaned records, blocking all concurrent orders.
- **The War-Room Fix**:
  1. Terminated the deletion transaction via `pg_terminate_backend()`.
  2. Created index concurrently: `CREATE INDEX CONCURRENTLY idx_orders_customer_id ON orders(customer_id);`.
- **Architectural Prevention**: Add automated CI linting (e.g., `pg_dump` schema scanners) that blocks PRs with unindexed foreign keys.

---

### 🚨 Incident 4: The Out-of-Memory Killer Terminating PostgreSQL

- **The Outage**: A primary PostgreSQL node was suddenly terminated by the Linux OS kernel, causing abrupt ungraceful failover.
- **Root Cause**: `work_mem` was configured to `512MB` on a server with 100 connections. A complex reporting query contained 6 parallel sort and hash operations ($6 \times 512\text{MB} = 3\text{GB}$ for a single query!). Multiple concurrent users triggered 40GB of memory allocation, breaching host RAM. The Linux Kernel OOM Killer terminated the `postgres` postmaster process.
- **The War-Room Fix**:
  1. Lowered global `work_mem` to `32MB`.
  2. Allowed specific heavy reporting scripts to set `SET work_mem = '256MB'` locally in their own sessions.
  3. Configured Linux `vm.overcommit_memory = 2` to prevent memory overcommitment.
- **Architectural Prevention**: Remember that `work_mem` is allocated **per sort/hash operation per query**, not per connection.

---

## Section 4: Rapid-Fire Cheat Sheet & Interview Summary Matrix

| Concept | Golden Rule / Critical Syntax | Fatal Trap to Avoid |
|---|---|---|
| **MVCC Read/Write** | Readers never block writers; writers never block readers | DDL (`ALTER TABLE`) acquires `AccessExclusiveLock`, blocking reads |
| **Serializable SSI** | Prevents Write Skew using non-blocking `SIREAD` locks | Failing to catch and retry error code `40001` in code |
| **Autovacuum** | Tune `autovacuum_vacuum_cost_limit = 2000` | Default cost settings allow dead tuples to cause table bloat |
| **B-Tree Indexing** | Traversals are $O(\log N)$ via Lehman-Yao B-trees | Index on `(A, B)` cannot satisfy queries filtering only on `B` |
| **BRIN Indexing** | 99% smaller for append-only, ordered time-series | Using BRIN on randomly updated tables causes full scans |
| **Query Tuning** | Buffers `temp written` indicates memory spill to disk | Reading only estimated cost rather than `actual time` in EXPLAIN |
| **Window Frames** | Always use `ROWS BETWEEN ...` instead of default `RANGE` | `RANGE` mode evaluates duplicates, causing 10x-50x slowdowns |
| **N+1 Elimination** | Use `JOIN FETCH` or `default_batch_fetch_size = 50` | `JOIN FETCH` with `Pageable` forces in-memory pagination (OOM) |
| **Partitioning** | Partition key MUST be part of composite Primary Key | Enforcing global unique constraints across partitions fails |
| **JSONB Storage** | `JSONB` is binary decomposed; indexed via GIN | Storing everything in JSONB destroys relational constraints |
| **Connection Pooling** | Size pool to $(\text{CPU cores} \times 2) + \text{Disk Count}$ | 100 connections per pod saturates PostgreSQL backend processes |
| **Pessimistic Locking** | Use `FOR UPDATE SKIP LOCKED` for message queues | Standard `FOR UPDATE` blocks all concurrent worker threads |
| **Safe DDL** | Add `CHECK ... NOT VALID`, then `VALIDATE CONSTRAINT` | Naive `ADD COLUMN NOT NULL DEFAULT` blocks production tables |
| **Redis Invalidation** | Use distributed mutex (`SET NX`) to prevent stampedes | Running `KEYS *` blocks single-threaded event loop completely |
