[🏠 Back to Home](README.md) | [🗄️ Spring SQL Master Guide](spring_sql.md) | [🍃 Spring Boot Master Guide](spring_master_guide.md) | [🏛️ SQL Scenarios Master Guide](sql_scenarios_master_guide.md)

# 🗄️ Spring SQL & JDBC Enterprise: 50+ Real-World Production Interview Scenarios Master Guide

[![Spring JDBC](https://img.shields.io/badge/Spring%20JDBC-6.1%2B-blue.svg?style=for-the-badge&logo=spring)](https://spring.io/projects/spring-framework)
[![HikariCP](https://img.shields.io/badge/HikariCP-5.1%2B-black.svg?style=for-the-badge)](https://github.com/brettwooldridge/HikariCP)
[![Java](https://img.shields.io/badge/Java-17%20%2F%2021%20LTS-orange.svg?style=for-the-badge&logo=openjdk)](https://www.oracle.com/java/)
[![Level](https://img.shields.io/badge/Tier-1%20Panels-Staff%20%2F%20Principal-red.svg?style=for-the-badge)](https://github.com/)

An exhaustive, battle-tested compilation of **real-world production interview scenarios** covering Spring JDBC, `NamedParameterJdbcTemplate`, HikariCP connection pool mathematical sizing, leak detection thresholds, programmatic `TransactionTemplate` boundary isolation, $O(1)$ memory streaming with `RowCallbackHandler`, dynamic SQL whitelisting, and multi-database routing.

Every scenario strictly follows the **5-Part Tier-1 Product Engineering Format**:
1. **Exact Question Asked by Tier-1 Product Panels (with detailed scenario context)**
2. **What the Interviewer Evaluates Under the Surface (mental criteria, low-level runtime knowledge)**
3. **Standout Technical Answer (deep runtime mechanics, low-level JDBC/driver details, zero fluff)**
4. **Follow-Up Trap Question & Winning Answer (catching surface memorizers)**
5. **Production Sample Code with Execution Steps, Complete Code, and Sample Input & Output**

---

## 📑 Master Architecture Navigation

- [🏛️ Category 1: HikariCP Mathematical Sizing, Leaks & Timeout Chains (Q1 – Q4)](#category-1-hikaricp-mathematical-sizing-leaks--timeout-chains)
- [⚡ Category 2: NamedParameterJdbcTemplate, Batching & Driver Rewrites (Q5 – Q8)](#category-2-namedparameterjdbctemplate-batching--driver-rewrites)
- [🛡️ Category 3: Transaction Demarcation: TransactionTemplate vs @Transactional (Q9 – Q12)](#category-3-transaction-demarcation-transactiontemplate-vs-transactional)
- [🌊 Category 4: O(1) Memory Streaming: RowCallbackHandler & Cursors (Q13 – Q15)](#category-4-o1-memory-streaming-rowcallbackhandler--cursors)
- [🔒 Category 5: Dynamic SQL Generation, Whitelisting & SQL Injection Armor (Q16 – Q18)](#category-5-dynamic-sql-generation-whitelisting--sql-injection-armor)
- [🔀 Category 6: Dynamic Routing DataSources & Read/Write Splitting (Q19 – Q20)](#category-6-dynamic-routing-datasources--readwrite-splitting)
- [🔥 Real-World War Room Outage Forensics](#-real-world-war-room-outage-forensics)
- [⚖️ Spring SQL Production Diagnostic Matrix](#️-spring-sql-production-diagnostic-matrix)

---

# Category 1: HikariCP Mathematical Sizing, Leaks & Timeout Chains

### Q1: Why does setting `maximumPoolSize = 100` in HikariCP degrade throughput compared to `maximumPoolSize = 15`, and what is the PostgreSQL sizing formula?
- **Scenario Context:** Under a high-traffic peak of 15,000 req/sec, an engineer increases HikariCP `maximum-pool-size` from 15 to 100 on all 10 microservice pods (creating 1,000 active database connections). Database query latency immediately spikes from 4ms to 1,200ms, and PostgreSQL CPU utilization hits 100% with massive context-switching thrash.
- **What the Interviewer Evaluates:** Understanding of CPU core scheduling, disk spindle contention, database backend worker process memory (PostgreSQL `work_mem`), and the PostgreSQL / HikariCP Pool Sizing Formula.
- **Standout Technical Answer:**
  - A database server has a fixed physical limit of CPU cores and disk I/O channels.
  - In PostgreSQL, **each connection is a dedicated heavy OS process** (`fork()`), consuming 10MB+ of private RAM (`work_mem`).
  - When 1,000 connections execute queries simultaneously on a 16-core database server:
    1. The Linux OS kernel spends 70% of its CPU time performing **CPU thread context switching** rather than executing SQL queries!
    2. Disk read heads or SSD storage controllers thrash trying to satisfy 1,000 competing random I/O requests.
    3. CPU L1/L2/L3 caches are thrashed continuously across 1,000 process memory pages.
  - **The Golden Sizing Formula:**
    $$\text{Connections} = (\text{CPU Cores} \times 2) + \text{Effective Spindle Count}$$
    On a modern 16-core cloud database with NVMe SSDs ($1\text{ spindle}$):
    $$\text{Optimal Total DB Connections} = (16 \times 2) + 1 = 33\text{ connections!}$$
  - A small, well-tuned pool of 10–15 connections per pod ensures queries execute sequentially at full CPU cache speed without queuing inside the database kernel, increasing total throughput by up to $10\times$!
- **Follow-Up Trap:** *"What happens if `leakDetectionThreshold` is set to 0 in HikariCP?"*
  - *Winning Answer:* "Setting it to 0 completely disables connection leak tracking! If a developer opens a JDBC connection without closing it in a `finally` block, HikariCP will never warn you. In production, always set `leakDetectionThreshold = 2000` (2 seconds); HikariCP captures the exact allocation stack trace and logs a warning with line numbers when a connection remains unreturned!"

#### Production Code Example - Q1: Production-Hardened HikariCP Configuration

- **Execution Steps:**
  1. Size connection pool to 15 based on the hardware core formula.
  2. Configure fixed pool (`minimumIdle == maximumPoolSize`) to avoid runtime connection allocation jitter.
  3. Set `leak-detection-threshold: 2000` and `connection-timeout: 3000` to fail fast before upstream HTTP gateways timeout.

- **Sample Code:**
```yaml
# application.yml
spring:
  datasource:
    hikari:
      pool-name: EnterprisePrimaryPool
      maximum-pool-size: 15          # Sized according to (Cores * 2) + Spindles
      minimum-idle: 15               # Fixed pool size eliminates connection allocation jitter
      connection-timeout: 3000       # 3s wait before throwing SQLTransientConnectionException
      idle-timeout: 600000           # 10 minutes
      max-lifetime: 1800000          # 30 mins: refreshes connections before TCP keepalive drop
      leak-detection-threshold: 2000 # 2s: Captures stack trace of unclosed connections!
      connection-test-query: SELECT 1
```

- **Sample Input & Output:**
```text
2026-09-13T10:00:00.010Z INFO [main] com.zaxxer.hikari.HikariDataSource : EnterprisePrimaryPool - Starting...
2026-09-13T10:00:00.125Z INFO [main] com.zaxxer.hikari.HikariDataSource : EnterprisePrimaryPool - Start completed. Active=0, Idle=15, Total=15.
2026-09-13T10:05:02.100Z WARN [HouseKeeper] com.zaxxer.hikari.pool.ProxyLeakTask : Connection leak detection triggered for Connection [PgConnection:1] on thread http-nio-8080-exec-3, held for 2050ms!
```

---

### Q2: How do you configure HikariCP `maxLifetime` and cloud load balancer idle TCP timeouts to avoid `Connection reset by peer` errors?
- **Scenario Context:** An application deployed in AWS connects to Aurora PostgreSQL via an AWS Network Load Balancer (NLB). Randomly every 350 seconds, incoming queries fail with `PSQLException: This connection has been closed` or `SocketTimeoutException: Connection reset by peer`.
- **What the Interviewer Evaluates:** TCP socket idle timeouts, AWS NAT Gateway / NLB 350-second idle connection drops, and HikariCP `maxLifetime` mathematical coordination.
- **Standout Technical Answer:**
  - Cloud firewalls, AWS NLB, and NAT Gateways silently drop idle TCP connections after 350 seconds (default TCP idle timeout) by terminating the connection state in their conntrack table without sending a TCP `FIN` or `RST` packet to the client.
  - When the Spring application tries to reuse this idle socket from HikariCP, it sends a query down a dead socket, causing the OS TCP stack to hang until timing out or throwing `Connection reset by peer`.
  - **The Invariant Rule:**
    $$\text{HikariCP } \texttt{maxLifetime} < \text{Cloud/Firewall Idle Timeout}$$
  - Configure `maxLifetime` to 300,000ms (5 minutes) or less, and set `keepaliveTime` to 60,000ms (1 minute). HikariCP will proactively retire connections from the pool before the cloud firewall can sever them.
- **Follow-Up Trap:** *"Why does HikariCP add a random jitter of up to 2.5% to `maxLifetime`?"*
  - *Winning Answer:* "To prevent a 'connection retirement storm'! If all 50 connections in the pool were established at 9:00:00 AM with a fixed 30-minute `maxLifetime`, all 50 connections would expire simultaneously at 9:30:00 AM, causing a severe connection creation spike and latency stall. The random jitter desynchronizes their retirement."

#### Production Code Example - Q2: Anti-Connection Reset Configuration

- **Execution Steps:**
  1. Configure `max-lifetime: 270000` (4.5 minutes) to precede AWS NLB's 350-second timeout.
  2. Enable TCP keep-alive via `keepalive-time: 45000` (45 seconds).
  3. Validate database socket health without running expensive `SELECT 1` queries by leveraging JDBC 4 `isValid()`.

- **Sample Code:**
```java
package com.enterprise.sql.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class ResilientDataSourceConfig {

    @Bean
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:postgresql://aurora-cluster.internal:5432/trading");
        config.setUsername("app_user");
        config.setPassword(System.getenv("DB_PASSWORD"));
        
        // AWS NLB timeout is 350s. maxLifetime MUST be strictly lower!
        config.setMaxLifetime(270_000); // 4.5 minutes
        config.setKeepaliveTime(45_000); // 45 seconds sends TCP keepalive probes
        config.setConnectionTimeout(3_000);
        config.setMaximumPoolSize(20);
        config.setMinimumIdle(20);
        config.setPoolName("NLBResilientPool");

        return new HikariDataSource(config);
    }
}
```

- **Sample Input & Output:**
```text
2026-09-13T10:10:00.010Z DEBUG [HouseKeeper] com.zaxxer.hikari.pool.HikariPool : NLBResilientPool - Retiring connection [PgConnection:2] (lifetime exceeded 268420ms with jitter)
2026-09-13T10:10:00.025Z DEBUG [HouseKeeper] com.zaxxer.hikari.pool.HikariPool : NLBResilientPool - Added fresh connection [PgConnection:18]
Zero 'Connection reset by peer' exceptions observed over 48 hours of idle testing.
```

---

### Q3: What is the exact timeout hierarchy required to prevent orphaned thread hangs between HikariCP, Spring JDBC, and PostgreSQL?
- **Scenario Context:** A downstream PostgreSQL query experiences an exclusive table lock wait. Client requests timeout in the browser after 5 seconds, but backend worker threads and database locks remain frozen for 15 minutes, exhausting all application threads.
- **What the Interviewer Evaluates:** Timeout alignment across HTTP Gateway, Tomcat, HikariCP, JDBC `Statement.setQueryTimeout()`, and PostgreSQL `statement_timeout`.
- **Standout Technical Answer:**
  - When timeouts are misconfigured, upstream clients disconnect, but the database continues executing the expensive query, wasting CPU and holding table locks indefinitely.
  - **The Strict Timeout Hierarchy:**
    $$\text{Client Gateway Timeout (e.g. 5s)} \ge \text{HikariCP connection-timeout (3s)} > \text{JDBC queryTimeout (2.5s)} > \text{PostgreSQL statement\_timeout (2s)}$$
  - If a query exceeds 2 seconds, PostgreSQL cancels the query kernel-side via `statement_timeout` (`ERROR: canceling statement due to statement timeout`).
  - The JDBC driver receives the cancellation, and Spring's `JdbcTemplate` translates it to a `QueryTimeoutException`, returning the connection back to HikariCP in milliseconds.
- **Follow-Up Trap:** *"Why does setting `jdbcTemplate.setQueryTimeout(5)` fail to cancel a query stuck waiting for a connection from HikariCP?"*
  - *Winning Answer:* "`queryTimeout` only applies *after* a connection is checked out and the SQL statement is sent over the wire! It does not govern the time spent waiting in the HikariCP queue. HikariCP's `connectionTimeout` governs pool wait time."

#### Production Code Example - Q3: Coordinated Multi-Layer Timeout Configuration

- **Execution Steps:**
  1. Define global `statement_timeout` in the PostgreSQL connection string.
  2. Set `queryTimeout` on `JdbcTemplate` to abort slow queries client-side.
  3. Configure HikariCP `connection-timeout` to fail before HTTP gateway cutoff.

- **Sample Code:**
```java
package com.enterprise.sql.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class TimeoutConfig {

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        JdbcTemplate template = new JdbcTemplate(dataSource);
        // Aborts SQL execution if query takes longer than 3 seconds
        template.setQueryTimeout(3);
        return template;
    }
}
```

```yaml
# application.yml - Connection String Driver Timeout
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/orders?options=-c%20statement_timeout=2500
    hikari:
      connection-timeout: 3000
```

- **Sample Input & Output:**
```text
2026-09-13T10:15:00.120Z WARN [http-nio-8080-exec-1] o.s.j.c.JdbcTemplate : Query failed with timeout:
org.springframework.dao.QueryTimeoutException: StatementCallback; SQL [SELECT * FROM heavy_ledger FOR UPDATE]; 
ERROR: canceling statement due to statement timeout
Time taken: 2502ms. Worker thread unblocked immediately.
```

---

### Q4: How does HikariCP's `FastList` and `ConcurrentBag` design achieve zero-lock concurrency compared to Commons-DBCP and Tomcat JDBC?
- **Scenario Context:** During high-concurrency benchmarks, legacy Apache Commons DBCP experiences high lock contention on `borrowObject()`, while HikariCP processes 200,000 borrow operations per second with near-zero CPU lock contention.
- **What the Interviewer Evaluates:** Low-level lock-free data structures, `ThreadLocal` connection caching, and `ConcurrentBag` stealing algorithms.
- **Standout Technical Answer:**
  - **1. Elimination of `synchronized`:** Traditional connection pools lock the entire pool collection with a single mutex or `ReentrantLock` during `getConnection()`.
  - **2. `ConcurrentBag` Architecture:**
    - HikariCP maintains a 3-tier lookup in `ConcurrentBag`:
      1. **ThreadLocal cache:** If the current thread previously used a connection and it is free, it re-acquires it with zero locks or volatile reads!
      2. **Shared Lock-Free Queue:** If ThreadLocal misses, it checks a lock-free list using CAS (`AtomicInteger` state transitions: `STATE_NOT_IN_USE` $\to$ `STATE_IN_USE`).
      3. **Synchronous Queue Hand-off:** If all connections are in use, the thread parks on a `SynchronousQueue` waiting for another thread to return a connection.
  - **3. `FastList` over `ArrayList`:**
    - Standard `ArrayList.remove(Object)` scans sequentially from index 0 ($O(N)$).
    - When closing JDBC Statements, statements are closed in LIFO order (last opened is closed first). HikariCP's custom `FastList` scans from the tail ($O(1)$), eliminating millions of range checks and array shifts.
- **Follow-Up Trap:** *"Can a thread leak a connection in HikariCP if it crashes while holding a connection?"*
  - *Winning Answer:* "Yes! If an unhandled thread crash occurs or a thread terminates without returning its connection, the connection state in `ConcurrentBag` remains `STATE_IN_USE`. This is why `leakDetectionThreshold` is essential—it detects and logs the leaked connection, allowing developers to identify the unclosed code path."

#### Production Code Example - Q4: Microbenchmark Verification of Connection Acquisition

- **Execution Steps:**
  1. Initialize HikariCP pool and warm up connections.
  2. Execute 100,000 concurrent connection acquire and release operations across 16 threads.
  3. Measure latency metrics confirming sub-microsecond overhead.

- **Sample Code:**
```java
package com.enterprise.sql.benchmark;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PoolAcquireBenchmark {

    public static void main(String[] args) throws InterruptedException {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:mem:bench;DB_CLOSE_DELAY=-1");
        config.setMaximumPoolSize(20);
        config.setMinimumIdle(20);
        config.setPoolName("BenchPool");

        try (HikariDataSource ds = new HikariDataSource(config)) {
            int iterations = 100_000;
            int threads = 16;
            ExecutorService executor = Executors.newFixedThreadPool(threads);
            CountDownLatch latch = new CountDownLatch(threads);

            long start = System.nanoTime();
            for (int t = 0; t < threads; t++) {
                executor.submit(() -> {
                    try {
                        for (int i = 0; i < iterations / threads; i++) {
                            try (Connection conn = ds.getConnection()) {
                                // FastList and ConcurrentBag allocate in nanoseconds
                            }
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await();
            long totalNanos = System.nanoTime() - start;
            System.out.printf("Completed %d borrows across %d threads in %.2f ms (%.2f ops/sec)%n",
                iterations, threads, totalNanos / 1_000_000.0, (iterations / (totalNanos / 1_000_000_000.0)));
            executor.shutdown();
        }
    }
}
```

- **Sample Input & Output:**
```text
2026-09-13T10:20:00.010Z INFO [main] com.zaxxer.hikari.HikariDataSource : BenchPool - Starting...
2026-09-13T10:20:00.050Z INFO [main] com.zaxxer.hikari.HikariDataSource : BenchPool - Start completed.
Completed 100000 borrows across 16 threads in 68.45 ms (1460920.38 ops/sec)
Zero lock thrash observed via ConcurrentBag thread-local hand-off.
```

---

# Category 2: NamedParameterJdbcTemplate, Batching & Driver Rewrites

### Q5: How does `NamedParameterJdbcTemplate.batchUpdate()` execute 50,000 records in 1.2 seconds, and why does PostgreSQL require `reWriteBatchedInserts=true`?
- **Scenario Context:** In a financial ledger reconciliation job, executing 50,000 inserts using standard JDBC `Statement.executeUpdate()` takes 85 seconds over the network. Switching to `NamedParameterJdbcTemplate.batchUpdate()` drops time to 1.2 seconds.
- **What the Interviewer Evaluates:** JDBC batch wire protocols, PostgreSQL client driver parameter rewrite mechanics, and `SqlParameterSourceUtils.createBatch`.
- **Standout Technical Answer:**
  - Standard JDBC execution sends 1 network packet per row:
    $$\text{Total Time} = 50,000 \times \text{Network Round-Trip Time (RTT)} \approx 50,000 \times 1.5\text{ms} = 75\text{ seconds!}$$
  - **`NamedParameterJdbcTemplate.batchUpdate()`**:
    - Buffers rows into JDBC's `PreparedStatement.addBatch()`.
    - Transmits batches of 1,000 rows in a single network frame.
  - **The PostgreSQL Secret (`reWriteBatchedInserts=true`):**
    - By default, the PostgreSQL JDBC driver executes batch statements as multiple individual queries within a single round trip:
      `INSERT INTO t VALUES (1); INSERT INTO t VALUES (2);`
    - When you append **`?reWriteBatchedInserts=true`** to the JDBC connection string, the driver physically rewrites the SQL into a single multi-row insert:
      `INSERT INTO t (id, amt) VALUES (1, 10.0), (2, 20.0), (3, 30.0)...`
    - This bypasses 99% of database query parsing and transaction overhead, accelerating batch insert speed by **$400\%$**!
- **Follow-Up Trap:** *"Why can't you use `reWriteBatchedInserts=true` with statements containing `RETURNING id`?"*
  - *Winning Answer:* "In older PostgreSQL driver versions, rewriting batch inserts into a single multi-value insert altered how `RETURNING` keys were mapped back to individual batch indices. In modern drivers (42.5+), it is supported, but batch return count arrays will return `-2` (`SUCCESS_NO_INFO`) instead of exact row counts."

#### Production Code Example - Q5: High-Throughput Batch Insert with SqlParameterSourceUtils

- **Execution Steps:**
  1. Define model record `LedgerEntry`.
  2. Batch convert records using `SqlParameterSourceUtils.createBatch()`.
  3. Execute bulk write with batch size 1,000 and verify 50,000 rows commit in $<1.5$ seconds.

- **Sample Code:**
```java
package com.enterprise.sql.repository;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

public record LedgerEntry(Long id, String accountId, Double amount, String currency, Instant createdAt) {}

@Repository
public class LedgerBatchRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public LedgerBatchRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public int[] batchInsertTransactions(List<LedgerEntry> transactions) {
        String sql = """
            INSERT INTO ledger_entries (id, account_id, amount, currency, created_at)
            VALUES (:id, :accountId, :amount, :currency, :createdAt)
            """;

        // Efficiently wraps List<POJO> without reflection overhead per row
        SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(transactions);

        // Executes in batch chunks over wire
        return jdbcTemplate.batchUpdate(sql, batch);
    }
}
```

- **Sample Input & Output:**
```text
2026-09-13T10:25:00.010Z INFO [main] c.e.s.r.LedgerBatchRepository : Initiating batch insert for 50,000 ledger records...
2026-09-13T10:25:01.215Z INFO [main] c.e.s.r.LedgerBatchRepository : Successfully committed 50,000 records in 1,205 ms. Throughput: 41,493 rows/sec.
```

---

### Q6: How do you handle `IN (:ids)` clauses with 10,000 items in Spring JDBC without crashing PostgreSQL?
- **Scenario Context:** An application executes `SELECT * FROM orders WHERE id IN (:ids)`. When a batch of 25,000 IDs is passed, PostgreSQL crashes with `Stack overflow` or `PreparedStatement parameter limit exceeded (max 32767)`, and query plan compilation freezes the CPU.
- **What the Interviewer Evaluates:** Parameter binding cardinality, query plan cache thrashing, and PostgreSQL `ANY(:array)` optimization.
- **Standout Technical Answer:**
  - In standard SQL, `IN (?, ?, ... 25,000 times)` generates a massive AST with 25,000 expression nodes.
  - This causes three severe production failures:
    1. **Parameter limit exhaustion**: JDBC drivers fail if parameters exceed 32,767.
    2. **Plan cache explosion**: Every unique list size (`IN (3 items)` vs `IN (4 items)`) produces a distinct SQL string, polluting PostgreSQL's `pg_prepared_statements` cache.
    3. **CPU compilation spike**: Building the parse tree takes hundreds of milliseconds.
  - **The Production Fix:**
    - In PostgreSQL, replace `IN (:ids)` with **`= ANY(:idsArray)`**.
    - Pass a single SQL Array: `conn.createArrayOf("bigint", ids)`.
    - This binds as **ONE single parameter**, compiles instantaneously, and produces an optimal `Index Scan` using the index.
- **Follow-Up Trap:** *"How do you solve this in MySQL, which does not support `= ANY(array)`?"*
  - *Winning Answer:* "Partition the 25,000 IDs into fixed sub-chunks of 500 or 1,000 items using Guava `Lists.partition(ids, 1000)` or Java Streams, execute queries in parallel or batch, and merge results in memory."

#### Production Code Example - Q6: ANY(:array) Optimization for Large Collections

- **Execution Steps:**
  1. Convert `List<Long>` into a native SQL Array.
  2. Write query using `WHERE id = ANY(:ids)`.
  3. Verify execution plan utilizes Index Scan with a single parameter binding.

- **Sample Code:**
```java
package com.enterprise.sql.repository;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class OrderQueryRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public OrderQueryRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Long> findMatchingOrderIds(List<Long> orderIds) {
        // Optimized for PostgreSQL: 1 parameter instead of 25,000 placeholders!
        String sql = "SELECT id FROM orders WHERE id = ANY(:ids)";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("ids", orderIds.toArray(new Long[0]));

        return jdbcTemplate.query(sql, params, (rs, rowNum) -> rs.getLong("id"));
    }
}
```

- **Sample Input & Output:**
```text
EXPLAIN ANALYZE SELECT id FROM orders WHERE id = ANY('{1001, 1002, ..., 35000}'::bigint[]);
Execution Plan:
Bitmap Heap Scan on orders (cost=25.40..1540.20 rows=25000 width=8)
  Recheck Cond: (id = ANY ('{1001, 1002, ...}'::bigint[]))
  -> Bitmap Index Scan on pk_orders (cost=0.00..18.50 rows=25000 width=0)
Execution Time: 4.82 ms (vs 1,450 ms with standard IN clause)
```

---

### Q7: How does `SqlParameterSource` handle `NULL` values, and why does untyped `params.addValue("status", null)` trigger `PreparedStatement.setNull(index, Types.OTHER)` errors?
- **Scenario Context:** In Oracle or PostgreSQL, an insert statement fails with `PSQLException: ERROR: could not determine data type of parameter $3` when passing `null` for an optional column.
- **What the Interviewer Evaluates:** JDBC type inference, SQL `NULL` typing, and `java.sql.Types` explicit mapping.
- **Standout Technical Answer:**
  - In JDBC, `NULL` is not untyped. When setting a null parameter, the driver calls `PreparedStatement.setNull(parameterIndex, sqlType)`.
  - When you call `params.addValue("status", null)` without specifying the SQL type:
    - Spring JDBC defaults to passing `Types.OTHER`.
    - Strict database engines (PostgreSQL, Oracle, DB2) cannot infer the column data type (is it `VARCHAR`, `INTEGER`, or `JSONB`?) and reject the query.
  - **The Solution:** Always supply explicit SQL types when passing nulls:
    `params.addValue("status", null, Types.VARCHAR);`
    Or use `BeanPropertySqlParameterSource`, which automatically inspects Java reflection metadata to map the correct `java.sql.Types`.
- **Follow-Up Trap:** *"What happens if you insert a null Java `Instant` into a `TIMESTAMPTZ` column without an explicit type?"*
  - *Winning Answer:* "Spring passes `Types.OTHER`, causing PostgreSQL to throw `column is of type timestamp with time zone but expression is of type bytea or other`. You must explicitly specify `Types.TIMESTAMP_WITH_TIMEZONE`."

#### Production Code Example - Q7: Explicit Type Mapping with MapSqlParameterSource

- **Execution Steps:**
  1. Construct `MapSqlParameterSource`.
  2. Bind null values with explicit `Types.VARCHAR` and `Types.TIMESTAMP_WITH_TIMEZONE`.
  3. Execute insert and confirm error-free execution across strict SQL drivers.

- **Sample Code:**
```java
package com.enterprise.sql.repository;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Types;
import java.time.Instant;

@Repository
public class CustomerAuditRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public CustomerAuditRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void recordAudit(Long userId, String action, String notes, Instant completionTime) {
        String sql = """
            INSERT INTO user_audit_log (user_id, action, notes, completed_at)
            VALUES (:userId, :action, :notes, :completedAt)
            """;

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("userId", userId, Types.BIGINT);
        params.addValue("action", action, Types.VARCHAR);
        // Explicit types prevent Types.OTHER failure when variables are null!
        params.addValue("notes", notes, Types.VARCHAR);
        params.addValue("completedAt", completionTime, Types.TIMESTAMP_WITH_TIMEZONE);

        jdbcTemplate.update(sql, params);
    }
}
```

- **Sample Input & Output:**
```text
2026-09-13T10:30:00.010Z DEBUG [main] o.s.jdbc.core.JdbcTemplate : Executing prepared SQL statement
2026-09-13T10:30:00.015Z DEBUG [main] o.s.jdbc.core.StatementCreatorUtils : Setting SQL statement parameter value: column index 3, parameter value [null], value class [null], SQL type 12 (VARCHAR)
Insert succeeded with typed NULL.
```

---

### Q8: How do you capture Auto-Generated Primary Keys in Spring JDBC across MySQL and PostgreSQL using `GeneratedKeyHolder`?
- **Scenario Context:** An enterprise order service inserts a newly created order and requires its database-generated `id` (PostgreSQL `BIGSERIAL` / MySQL `AUTO_INCREMENT`) to publish downstream Kafka events.
- **What the Interviewer Evaluates:** `GeneratedKeyHolder`, JDBC `RETURN_GENERATED_KEYS`, and dialect differences between MySQL and PostgreSQL multi-column returning.
- **Standout Technical Answer:**
  - `GeneratedKeyHolder` captures keys returned by the database during `executeUpdate()`.
  - In PostgreSQL, you must provide the exact generated column names:
    `jdbcTemplate.update(sql, paramSource, keyHolder, new String[]{"id"});`
    This causes the PostgreSQL driver to append `RETURNING id` under the hood.
  - In MySQL, passing column names is ignored; MySQL returns the `AUTO_INCREMENT` value via the JDBC connection's metadata channel.
  - Safe extraction requires inspecting `keyHolder.getKey()` (for single keys) or `keyHolder.getKeys()` (for composite/multi-column keys).
- **Follow-Up Trap:** *"Why does `keyHolder.getKey()` throw `DataRetrievalFailureException` if your insert statement triggers a database trigger that also inserts rows into an audit table?"*
  - *Winning Answer:* "Because the audit trigger generates its own keys! `keyHolder.getKeyList()` receives multiple rows of generated keys from both tables. Calling `getKey()` expects exactly one row and throws an exception. You must use `keyHolder.getKeyList()` and extract the primary row explicitly."

#### Production Code Example - Q8: Resilient GeneratedKeyHolder Implementation

- **Execution Steps:**
  1. Initialize `GeneratedKeyHolder`.
  2. Execute update with column name array `new String[]{"id"}`.
  3. Extract generated `Number` safely and return the allocated ID.

- **Sample Code:**
```java
package com.enterprise.sql.repository;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.util.Objects;

@Repository
public class OrderCommandRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public OrderCommandRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Long createOrder(String customerNumber, Double amount) {
        String sql = "INSERT INTO orders (customer_number, amount) VALUES (:customer, :amount)";
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("customer", customerNumber)
            .addValue("amount", amount);

        KeyHolder keyHolder = new GeneratedKeyHolder();
        // Passing new String[]{"id"} enables RETURNING id in PostgreSQL!
        jdbcTemplate.update(sql, params, keyHolder, new String[]{"id"});

        Number key = keyHolder.getKey();
        return Objects.requireNonNull(key, "Database failed to generate primary key").longValue();
    }
}
```

- **Sample Input & Output:**
```text
2026-09-13T10:35:00.010Z DEBUG [main] o.s.j.c.n.NamedParameterJdbcTemplate : Executing SQL INSERT with KeyHolder
Hibernate/JDBC: INSERT INTO orders (customer_number, amount) VALUES ('CUST-9921', 450.00) RETURNING id
Generated primary key extracted: 8841029
```

---

# Category 3: Transaction Demarcation: TransactionTemplate vs @Transactional

### Q9: Why does invoking an external HTTP payment call inside `@Transactional` cause Database Connection Pool Exhaustion, and how does `TransactionTemplate` solve it?
- **Scenario Context:** In a checkout service:
  ```java
  @Transactional
  public void checkoutOrder(OrderReq req) {
      orderRepo.insertOrder(req);             // 1. Takes DB connection
      stripeClient.chargeCard(req.card());   // 2. Slow external HTTP call (3 seconds!)
      orderRepo.updateStatus(COMPLETED);     // 3. Updates order
  }
  ```
  During peak hours, Stripe latency increases to 4 seconds. Within 1 minute, the application runs out of database connections and crashes.
- **What the Interviewer Evaluates:** Transaction demarcation scopes, holding physical JDBC connections during non-database blocking operations, and programmatic transaction narrowing.
- **Standout Technical Answer:**
  - When `@Transactional` is placed at the method level, Spring acquires a physical database connection from HikariCP **immediately upon entering the method**.
  - That connection is held exclusively by the worker thread for the **entire duration of the method**.
  - When calling an external HTTP service (Stripe, Paypal) taking 3 seconds:
    - The thread holds an idle database connection doing **ZERO database work** for 3 whole seconds!
    - At 20 HikariCP connections, just 20 concurrent users will lock 100% of your database connection pool, causing all other API endpoints to fail with `SQLTransientConnectionException`.
  - **The Production Fix: Programmatic `TransactionTemplate`:**
    - Narrow database operations strictly to the exact moments database I/O is performed.
    - Execute external HTTP calls **outside** of any database transaction!
- **Follow-Up Trap:** *"What happens if the external payment succeeds, but the second `TransactionTemplate` database update fails?"*
  - *Winning Answer:* "This is the classic distributed Dual-Write problem! You must make the payment call idempotent by passing a unique idempotency key, record a `PENDING` state in the first transaction, and use a background reconciler job or saga pattern to handle recovery if the second transaction fails."

#### Production Code Example - Q9: Programmatic TransactionTemplate Boundary Isolation

- **Execution Steps:**
  1. Break monolithic transaction into two micro-transactions using `TransactionTemplate`.
  2. Execute external HTTP call outside any transaction context.
  3. Verify that the physical connection is returned to HikariCP during the remote HTTP call.

- **Sample Code:**
```java
package com.enterprise.sql.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

public record CheckoutReq(String customerId, Double amount, String cardNumber) {}
public record PaymentRes(boolean success, String chargeId, String error) {}

@Service
public class ResilientCheckoutService {

    private final TransactionTemplate txTemplate;
    private final OrderRepository orderRepository;
    private final PaymentGatewayClient paymentClient;

    public ResilientCheckoutService(TransactionTemplate txTemplate,
                                    OrderRepository orderRepository,
                                    PaymentGatewayClient paymentClient) {
        this.txTemplate = txTemplate;
        this.orderRepository = orderRepository;
        this.paymentClient = paymentClient;
    }

    public Long processCheckout(CheckoutReq req) {
        // Step 1: Micro-transaction (holds DB connection for ~2ms)
        Long orderId = txTemplate.execute(status -> orderRepository.insertInitialOrder(req));

        // Step 2: External HTTP call (holds ZERO DB connections during 3s network wait!)
        PaymentRes payment = paymentClient.charge(req.cardNumber(), req.amount(), "idemp-" + orderId);

        // Step 3: Second micro-transaction (holds DB connection for ~2ms)
        txTemplate.executeWithoutResult(status -> {
            if (payment.success()) {
                orderRepository.markPaid(orderId, payment.chargeId());
            } else {
                orderRepository.markFailed(orderId, payment.error());
            }
        });

        return orderId;
    }
}
```

- **Sample Input & Output:**
```text
2026-09-13T10:40:00.001Z DEBUG [exec-1] o.s.j.d.DataSourceTransactionManager : Acquired Connection for transaction 1 (Order reserve)
2026-09-13T10:40:00.003Z DEBUG [exec-1] o.s.j.d.DataSourceTransactionManager : Committed & Returned Connection to HikariCP in 2.1ms
2026-09-13T10:40:00.004Z INFO  [exec-1] c.e.s.s.PaymentGatewayClient : Invoking Stripe API (External HTTP). ZERO DB connections checked out!
2026-09-13T10:40:03.100Z DEBUG [exec-1] o.s.j.d.DataSourceTransactionManager : Acquired Connection for transaction 2 (Order finalize)
2026-09-13T10:40:03.102Z DEBUG [exec-1] o.s.j.d.DataSourceTransactionManager : Committed & Returned Connection to HikariCP in 1.8ms
HikariCP pool remains 90% idle throughout 3-second Stripe latency.
```

---

### Q10: How does `TransactionSynchronizationManager` execute logic strictly after a database transaction has successfully committed?
- **Scenario Context:** In a user registration flow, after inserting a `User` entity, the service publishes a message to Kafka. If an exception occurs right as the database transaction commits, the Kafka message is already published, notifying external services about a user that does not exist in the database.
- **What the Interviewer Evaluates:** Post-commit hooks, `TransactionSynchronizationAdapter`, and `TransactionPhase.AFTER_COMMIT`.
- **Standout Technical Answer:**
  - Publishing events or sending emails inside a transaction creates dirty non-rollbackable side effects.
  - If you publish to Kafka *before* `COMMIT`, and the commit fails due to a database constraint violation, external listeners receive phantom messages.
  - **The Solution:** Register a synchronization hook via **`TransactionSynchronizationManager.registerSynchronization()`** overriding `afterCommit()`.
  - Spring guarantees that `afterCommit()` executes **only after the underlying database driver has successfully committed the SQL transaction** over the wire.
- **Follow-Up Trap:** *"What happens if the code inside `afterCommit()` throws an unhandled RuntimeException?"*
  - *Winning Answer:* "The database transaction is ALREADY committed! The exception will NOT roll back the database transaction. It will bubble up to the caller, causing the HTTP response to fail even though database state was persisted. Catch and handle exceptions inside `afterCommit()`."

#### Production Code Example - Q10: TransactionSynchronizationManager Post-Commit Hook

- **Execution Steps:**
  1. Inspect `TransactionSynchronizationManager.isActualTransactionActive()`.
  2. Register `TransactionSynchronization` overriding `afterCommit()`.
  3. Verify via execution logs that Kafka dispatch occurs strictly after the database commit log.

- **Sample Code:**
```java
package com.enterprise.sql.service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class AccountRegistrationService {

    private final AccountRepository accountRepo;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public AccountRegistrationService(AccountRepository accountRepo, KafkaTemplate<String, String> kafkaTemplate) {
        this.accountRepo = accountRepo;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    public void registerAccount(Long accountId, String email) {
        accountRepo.insertAccount(accountId, email);

        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    // Guaranteed to execute strictly AFTER DB commit has finalized!
                    System.out.println("[POST-COMMIT] DB committed. Publishing Kafka welcome event for: " + email);
                    kafkaTemplate.send("user-registered-topic", email);
                }
            });
        }
    }
}
```

- **Sample Input & Output:**
```text
2026-09-13T10:45:00.010Z DEBUG [main] o.s.j.d.DataSourceTransactionManager : Initiating transaction commit
2026-09-13T10:45:00.015Z DEBUG [main] o.s.j.d.DataSourceTransactionManager : Committed JDBC transaction
[POST-COMMIT] DB committed. Publishing Kafka welcome event for: alice@enterprise.com
2026-09-13T10:45:00.020Z INFO  [main] o.a.k.clients.producer.KafkaProducer : Sent record to user-registered-topic
Zero phantom messages on DB rollback.
```

---

### Q11: How do `Propagation.REQUIRES_NEW` and `Propagation.NESTED` differ at the JDBC connection and Savepoint levels?
- **Scenario Context:** In an audit logging feature, an inner method saves an audit record. If the outer business method fails, the audit record must still be committed. However, if the inner audit method fails, it should not fail the outer method.
- **What the Interviewer Evaluates:** Physical JDBC connection allocation, savepoint semantics, and connection pool starvation hazards under `REQUIRES_NEW`.
- **Standout Technical Answer:**
  - **`Propagation.REQUIRES_NEW`**:
    - **Suspends** the outer transaction.
    - Checks out a **SECOND physical JDBC connection** from HikariCP!
    - Runs in an independent, isolated physical database transaction.
    - *Danger*: If your HikariCP pool has 10 connections, 5 concurrent threads executing `REQUIRES_NEW` will consume all 10 connections (2 per thread), causing a pool deadlock where outer transactions wait for inner connections that can never be acquired!
  - **`Propagation.NESTED`**:
    - Uses the **SAME physical JDBC connection** as the outer transaction!
    - Creates a JDBC **`Savepoint`** (`SAVEPOINT savepoint_1`).
    - If the nested method throws an exception, Spring executes `ROLLBACK TO SAVEPOINT savepoint_1`.
    - The outer transaction can catch the exception and proceed to commit without consuming additional pool connections.
- **Follow-Up Trap:** *"Does PostgreSQL or Hibernate JPA support `Propagation.NESTED`?"*
  - *Winning Answer:* "Hibernate JPA explicitly does NOT support `NESTED` because JPA's Persistence Context cannot track partial rollbacks of entity state. However, Spring's `DataSourceTransactionManager` using pure Spring JDBC / `JdbcTemplate` supports `NESTED` flawlessly using native JDBC savepoints."

#### Production Code Example - Q11: Nested Savepoint Demarcation via DataSourceTransactionManager

- **Execution Steps:**
  1. Configure `DataSourceTransactionManager` with `setNestedTransactionAllowed(true)`.
  2. Implement outer method invoking inner method under `Propagation.NESTED`.
  3. Catch inner exception and commit outer transaction successfully.

- **Sample Code:**
```java
package com.enterprise.sql.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderProcessingService {

    private final OrderRepository orderRepository;
    private final NotificationLogRepository notificationRepo;

    public OrderProcessingService(OrderRepository orderRepository, NotificationLogRepository notificationRepo) {
        this.orderRepository = orderRepository;
        this.notificationRepo = notificationRepo;
    }

    @Transactional
    public void processOrderWithOptionalNotification(Long orderId) {
        orderRepository.insertOrder(orderId);

        try {
            // Uses JDBC Savepoint on the SAME connection; zero connection starvation!
            notificationRepo.saveNotificationNested(orderId);
        } catch (Exception ex) {
            System.err.println("[NESTED-RECOVERY] Notification failed; rolled back to savepoint. Order proceeds!");
        }
    }
}

@Service
class NotificationLogRepository {

    @Transactional(propagation = Propagation.NESTED)
    public void saveNotificationNested(Long orderId) {
        // Creates SAVEPOINT; rolls back only this method on failure!
        throw new RuntimeException("Notification service DB constraint failed");
    }
}
```

- **Sample Input & Output:**
```text
2026-09-13T10:50:00.010Z DEBUG [main] o.s.j.d.DataSourceTransactionManager : Creating new transaction for [processOrderWithOptionalNotification]
2026-09-13T10:50:00.012Z DEBUG [main] o.s.j.d.DataSourceTransactionManager : Creating nested transaction with savepoint [savepoint_1]
2026-09-13T10:50:00.015Z DEBUG [main] o.s.j.d.DataSourceTransactionManager : Rolling back transaction to savepoint [savepoint_1]
[NESTED-RECOVERY] Notification failed; rolled back to savepoint. Order proceeds!
2026-09-13T10:50:00.018Z DEBUG [main] o.s.j.d.DataSourceTransactionManager : Initiating transaction commit
Order row persisted successfully. Zero HikariCP connection starvation.
```

---

### Q12: Why does an unhandled checked exception NOT trigger a transaction rollback in Spring `@Transactional` by default?
- **Scenario Context:** In a banking service, an `InsufficientBalanceException` (which extends `java.lang.Exception`) is thrown. The method terminates, but Spring commits the transaction anyway, causing a financial discrepancy.
- **What the Interviewer Evaluates:** Spring EJB legacy rollback rules, `RollbackRuleAttribute`, and `@Transactional(rollbackFor = Throwable.class)`.
- **Standout Technical Answer:**
  - By default, Spring's transaction infrastructure follows the historic EJB convention:
    - **`RuntimeException` and `Error` trigger an automatic rollback.**
    - **Checked `Exception` is considered an anticipated business condition and triggers a COMMIT!**
  - If your custom business exceptions extend `Exception` (checked), Spring's `TransactionAspectSupport` catches it, notes that it is not an instance of `RuntimeException`, and proceeds to call `commit()`.
  - **The Production Standard:** Always explicitly declare:
    `@Transactional(rollbackFor = Throwable.class)`
    Or ensure all custom enterprise exceptions extend `RuntimeException`.
- **Follow-Up Trap:** *"What happens if you catch the exception in a `try-catch` block inside the method and log it without rethrowing?"*
  - *Winning Answer:* "Because the exception was swallowed, it never escapes the proxy method. Spring's `TransactionInterceptor` assumes the method finished normally and commits the transaction! Always rethrow or explicitly call `TransactionAspectSupport.currentTransactionStatus().setRollbackOnly()`."

#### Production Code Example - Q12: Explicit Rollback Configuration & Programmatic RollbackOnly

- **Execution Steps:**
  1. Annotate service method with `@Transactional(rollbackFor = Exception.class)`.
  2. Implement conditional fallback that sets rollback status programmatically without throwing.
  3. Verify transaction rolls back in database logs.

- **Sample Code:**
```java
package com.enterprise.sql.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

class BusinessException extends Exception {
    public BusinessException(String message) { super(message); }
}

@Service
public class FinancialTransferService {

    // rollbackFor ensures checked exceptions roll back the transaction!
    @Transactional(rollbackFor = Exception.class)
    public void transferFunds(Long fromId, Long toId, Double amount) throws BusinessException {
        if (amount > 10_000.0) {
            throw new BusinessException("AML Review Required: Transfer exceeds threshold");
        }
        // Deduct and Credit...
    }

    @Transactional
    public boolean transferWithGracefulRollback(Long fromId, Long toId, Double amount) {
        if (amount <= 0) {
            // Programmatically triggers rollback without throwing an exception
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return false;
        }
        return true;
    }
}
```

- **Sample Input & Output:**
```text
2026-09-13T10:55:00.010Z DEBUG [main] o.s.j.d.DataSourceTransactionManager : Participating in existing transaction
2026-09-13T10:55:00.012Z DEBUG [main] o.s.j.d.DataSourceTransactionManager : Setting rollback-only on transaction
2026-09-13T10:55:00.015Z DEBUG [main] o.s.j.d.DataSourceTransactionManager : Initiating transaction rollback due to rollback-only marker
Rollback completed cleanly.
```

---

# Category 4: O(1) Memory Streaming: RowCallbackHandler & Cursors

### Q13: How do you stream 10,000,000 database rows to a CSV export without running out of JVM Heap Space (`OutOfMemoryError`)?
- **Scenario Context:** A daily financial report exports all 10 million transactions from the database into an S3 bucket. Using `jdbcTemplate.queryForList()` or `findAll()`, the pod crashes with `OutOfMemoryError: Java heap space` after reading 400,000 rows.
- **What the Interviewer Evaluates:** Cursor vs in-memory caching, JDBC fetch size (`Statement.setFetchSize`), `ResultSet` streaming, and `RowCallbackHandler` vs `RowMapper`.
- **Standout Technical Answer:**
  - **Why `RowMapper` / `queryForList()` Fails:**
    - `RowMapper` materializes a Java object for every row and appends it to a `List<T>`.
    - Holding 10,000,000 objects in a single `ArrayList` requires 4GB+ of JVM heap memory, causing GC thrashing and immediate OOM.
  - **Why Default MySQL / PostgreSQL Drivers Buffer Everything:**
    - By default, MySQL and PostgreSQL JDBC drivers download **the entire query result set into client RAM** before returning from `executeQuery()`!
  - **The O(1) Memory Streaming Solution:**
    1. Set **`Statement.setFetchSize(1000)`** (or `Integer.MIN_VALUE` for MySQL). This instructs the database cursor to stream results over the TCP socket in chunks of 1,000 rows.
    2. Use **`RowCallbackHandler`**:
       - It does **NOT** build an in-memory collection.
       - It receives a single `ResultSet` row, writes the CSV line directly into a streaming output buffer (`BufferedWriter` connected to S3/HTTP response), and immediately discards the row from memory!
    3. Memory consumption remains strictly **constant ($O(1)$)** at ~5MB of RAM whether streaming 100 rows or 100,000,000 rows!
- **Follow-Up Trap:** *"Why must the database connection remain open for the entire duration of `RowCallbackHandler` streaming?"*
  - *Winning Answer:* "Because the database cursor is actively streaming rows over the open JDBC socket. If the connection or transaction closes prematurely, the cursor is destroyed and the stream terminates with `SQLException: ResultSet is closed`."

#### Production Code Example - Q13: Constant O(1) Memory CSV Streaming Service

- **Execution Steps:**
  1. Annotate method with `@Transactional(readOnly = true)` to keep cursor open.
  2. Configure `jdbcTemplate.setFetchSize(1000)`.
  3. Stream rows directly into a `Writer` using `RowCallbackHandler`.

- **Sample Code:**
```java
package com.enterprise.sql.streaming;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.sql.ResultSet;
import java.sql.SQLException;

@Service
public class TransactionExportService {

    private final JdbcTemplate jdbcTemplate;

    public TransactionExportService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional(readOnly = true) // In PostgreSQL, cursors require an active transaction!
    public void exportLargeLedgerToCsv(Writer rawWriter) {
        BufferedWriter writer = new BufferedWriter(rawWriter);
        String sql = "SELECT id, account_id, amount, currency, created_at FROM ledger_entries";

        // CRITICAL: fetchSize forces cursor-based streaming over the wire in 1,000 row chunks
        jdbcTemplate.setFetchSize(1000);

        jdbcTemplate.query(sql, new RowCallbackHandler() {
            @Override
            public void processRow(ResultSet rs) throws SQLException {
                try {
                    writer.write(String.format("%d,%s,%.2f,%s,%s\n",
                        rs.getLong("id"),
                        rs.getString("account_id"),
                        rs.getDouble("amount"),
                        rs.getString("currency"),
                        rs.getTimestamp("created_at")
                    ));
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        });

        try {
            writer.flush();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
```

- **Sample Input & Output:**
```text
2026-09-13T11:00:00.010Z INFO [export-worker-1] c.e.s.s.TransactionExportService : Beginning streaming of 10,000,000 ledger rows...
2026-09-13T11:00:45.320Z INFO [export-worker-1] c.e.s.s.TransactionExportService : Finished exporting 10,000,000 rows.
JVM Heap RSS: 142MB constant throughout entire export. Zero OOM.
```

---

### Q14: How does MySQL differ from PostgreSQL in enabling JDBC cursor-based ResultSet streaming?
- **Scenario Context:** An application that successfully streamed 5,000,000 rows on PostgreSQL is ported to MySQL. Despite setting `jdbcTemplate.setFetchSize(1000)`, MySQL crashes with `java.lang.OutOfMemoryError: Java heap space`.
- **What the Interviewer Evaluates:** MySQL Connector/J driver idiosyncrasies, `Integer.MIN_VALUE` streaming flag, and socket buffering.
- **Standout Technical Answer:**
  - In PostgreSQL, setting `setFetchSize(n)` inside an active transaction (`readOnly = true`) automatically creates a named database cursor and streams $n$ rows at a time.
  - In MySQL Connector/J, setting `setFetchSize(1000)` is **silently ignored** by default! The driver still allocates an array for all 5,000,000 rows in heap memory!
  - **The MySQL Rule:** To enable true streaming in MySQL Connector/J:
    1. `setFetchSize` must be set to **`Integer.MIN_VALUE`** (`-2147483648`).
    2. The statement must be created with `ResultSet.TYPE_FORWARD_ONLY` and `ResultSet.CONCUR_READ_ONLY`.
  - When `Integer.MIN_VALUE` is set, the MySQL driver switches to streaming mode, reading row-by-row directly from the network socket without any client-side buffering.
- **Follow-Up Trap:** *"Can you execute another query on the same MySQL connection while a streaming ResultSet is open?"*
  - *Winning Answer:* "No! In MySQL, while a streaming `ResultSet` is active, the TCP socket is locked streaming rows. If you attempt to issue another query on that connection, MySQL Connector/J throws `Streaming result set com.mysql.cj.protocol.a.result.ResultsetRowsStreaming@... is still active. No further queries may be run`."

#### Production Code Example - Q14: Dialect-Aware MySQL and PostgreSQL Streaming Statement Creator

- **Execution Steps:**
  1. Define custom `PreparedStatementCreator` detecting database type.
  2. For MySQL, explicitly apply `Integer.MIN_VALUE` fetch size.
  3. Execute streaming pipeline safely without driver heap buffering.

- **Sample Code:**
```java
package com.enterprise.sql.streaming;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Service
public class DialectAwareStreamer {

    private final JdbcTemplate jdbcTemplate;

    public DialectAwareStreamer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void streamFromMySQL(String sql) {
        PreparedStatementCreator psc = (Connection con) -> {
            PreparedStatement ps = con.prepareStatement(
                sql,
                ResultSet.TYPE_FORWARD_ONLY,
                ResultSet.CONCUR_READ_ONLY
            );
            // CRITICAL FOR MYSQL: Integer.MIN_VALUE enables true socket streaming!
            ps.setFetchSize(Integer.MIN_VALUE);
            return ps;
        };

        jdbcTemplate.query(psc, rs -> {
            // Process row directly from socket
            String id = rs.getString(1);
        });
    }
}
```

- **Sample Input & Output:**
```text
2026-09-13T11:05:00.010Z DEBUG [main] com.mysql.cj.jdbc.StatementImpl : Streaming result set enabled via Integer.MIN_VALUE
2026-09-13T11:05:00.025Z DEBUG [main] com.mysql.cj.protocol.a.NativeProtocol : Fetching row directly from socket buffer
5,000,000 rows streamed from MySQL with zero client-side memory buffering.
```

---

### Q15: Why does `ResultSetExtractor` differ from `RowCallbackHandler` and `RowMapper` in Spring JDBC memory dynamics?
- **Scenario Context:** In a report generation endpoint, an engineer must choose between `RowMapper`, `RowCallbackHandler`, and `ResultSetExtractor` to calculate rolling aggregations across 2,000,000 rows.
- **What the Interviewer Evaluates:** Spring JDBC template abstractions, memory allocation models, and stateful aggregation mechanics.
- **Standout Technical Answer:**
  - **`RowMapper<T>`**:
    - Invoked once per row.
    - Designed to return an individual mapped object `T`.
    - `JdbcTemplate` automatically accumulates every `T` into an in-memory `List<T>`.
    - *Memory Complexity*: $O(N)$. Unsuitable for millions of records.
  - **`RowCallbackHandler`**:
    - Invoked once per row.
    - Void return type.
    - `JdbcTemplate` collects nothing.
    - Ideal for streaming rows to disk, sockets, or updating external counters.
    - *Memory Complexity*: $O(1)$.
  - **`ResultSetExtractor<T>`**:
    - Invoked **only once** for the entire `ResultSet`.
    - Developer has full control over the `while(rs.next())` iteration loop.
    - Ideal for constructing complex hierarchical parent-child object graphs (e.g., mapping 1 Order with 10 Line Items without duplicates).
- **Follow-Up Trap:** *"Why should you avoid calling `rs.last()` or `rs.getRow()` inside a `ResultSetExtractor` on a large dataset?"*
  - *Winning Answer:* "Calling `rs.last()` requires a scrollable cursor (`TYPE_SCROLL_INSENSITIVE`), which forces database engines to materialize and cache the entire result set in temp files or memory, destroying streaming performance!"

#### Production Code Example - Q15: Parent-Child Hierarchical Extraction via ResultSetExtractor

- **Execution Steps:**
  1. Define composite record `OrderWithItems`.
  2. Implement `ResultSetExtractor` to collapse multiple join rows into hierarchical objects in a single iteration pass.
  3. Verify zero Cartesian object duplication and $O(N)$ single-pass processing.

- **Sample Code:**
```java
package com.enterprise.sql.extractor;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public record LineItem(Long id, String sku, Integer qty) {}
public record OrderWithItems(Long orderId, String customer, List<LineItem> items) {}

@Repository
public class OrderHierarchicalRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public OrderHierarchicalRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<OrderWithItems> findOrdersWithItems(List<Long> orderIds) {
        String sql = """
            SELECT o.id as order_id, o.customer, li.id as item_id, li.sku, li.qty
            FROM orders o
            LEFT JOIN line_items li ON o.id = li.order_id
            WHERE o.id = ANY(:orderIds)
            ORDER BY o.id
            """;

        Map<String, Object> params = Map.of("orderIds", orderIds.toArray(new Long[0]));

        return jdbcTemplate.query(sql, params, new ResultSetExtractor<List<OrderWithItems>>() {
            @Override
            public List<OrderWithItems> extractData(ResultSet rs) throws SQLException, DataAccessException {
                Map<Long, OrderWithItems> orderMap = new LinkedHashMap<>();

                while (rs.next()) {
                    Long orderId = rs.getLong("order_id");
                    OrderWithItems order = orderMap.computeIfAbsent(orderId, id -> {
                        try {
                            return new OrderWithItems(id, rs.getString("customer"), new ArrayList<>());
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    });

                    long itemId = rs.getLong("item_id");
                    if (!rs.wasNull()) {
                        order.items().add(new LineItem(itemId, rs.getString("sku"), rs.getInt("qty")));
                    }
                }
                return new ArrayList<>(orderMap.values());
            }
        });
    }
}
```

- **Sample Input & Output:**
```text
2026-09-13T11:10:00.010Z DEBUG [main] o.s.j.c.n.NamedParameterJdbcTemplate : Executing query using ResultSetExtractor
Mapped 250 orders with 1,840 associated line items in a single iteration pass (3.4ms).
```

---

# Category 5: Dynamic SQL Generation, Whitelisting & SQL Injection Armor

### Q16: How do you safely build dynamic SQL filter queries with optional parameters without exposing the application to SQL Injection?
- **Scenario Context:** An admin search screen has 8 optional filter fields (name, date range, min price, max price, status). A junior developer constructs the query using string concatenation (`"WHERE 1=1 " + (name != null ? "AND name = '" + name + "'" : "")`), exposing the database to SQL injection attacks.
- **What the Interviewer Evaluates:** Parameter binding safety, `NamedParameterJdbcTemplate` with `MapSqlParameterSource`, and avoiding string interpolation.
- **Standout Technical Answer:**
  - String concatenation in SQL allows attackers to inject malicious fragments (e.g. `' OR '1'='1' --`).
  - **The Safe Dynamic Pattern:**
    1. Dynamically append parameterized clauses (`AND status = :status`).
    2. Bind the user's input into a **`MapSqlParameterSource`**.
    3. The JDBC driver transmits the SQL template and user values separately; the database engine strictly treats user values as literal data, making SQL injection mathematically impossible!
- **Follow-Up Trap:** *"Can you use SQL parameters (`:columnName`) for dynamic `ORDER BY` column names?"*
  - *Winning Answer:* "No! SQL standards do not allow parameter placeholders for table names, column names, or sort directions (`ASC`/`DESC`). Dynamic sort columns must be validated against a strict **hard-coded whitelist** (e.g. `Set.of(\"id\", \"created_at\", \"amount\")`) before concatenating into the query!"

#### Production Code Example - Q16: Safe Dynamic Query Builder with Column Whitelist

- **Execution Steps:**
  1. Define allowed sort columns in an immutable `Set`.
  2. Dynamically append parameterized conditions to `StringBuilder`.
  3. Bind user arguments into `MapSqlParameterSource` and execute securely.

- **Sample Code:**
```java
package com.enterprise.sql.security;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public record OrderSearchCriteria(String status, BigDecimal minAmount, String sortBy, boolean ascending) {}
public record OrderSummary(Long id, String status, BigDecimal amount) {}

@Repository
public class SafeOrderSearchRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    // Strict whitelist: Prevents SQL injection in dynamic ORDER BY clauses!
    private static final Set<String> ALLOWED_SORT_COLUMNS = Set.of("id", "amount", "created_at");

    public SafeOrderSearchRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<OrderSummary> searchOrders(OrderSearchCriteria criteria) {
        StringBuilder sql = new StringBuilder("SELECT id, status, amount FROM orders WHERE 1=1 ");
        MapSqlParameterSource params = new MapSqlParameterSource();

        if (criteria.status() != null && !criteria.status().isBlank()) {
            sql.append("AND status = :status ");
            params.addValue("status", criteria.status());
        }

        if (criteria.minAmount() != null) {
            sql.append("AND amount >= :minAmount ");
            params.addValue("minAmount", criteria.minAmount());
        }

        // Whitelist validation
        String sortColumn = ALLOWED_SORT_COLUMNS.contains(criteria.sortBy()) ? criteria.sortBy() : "id";
        String direction = criteria.ascending() ? "ASC" : "DESC";
        sql.append("ORDER BY ").append(sortColumn).append(" ").append(direction).append(" LIMIT 50");

        return jdbcTemplate.query(sql.toString(), params, (rs, rowNum) ->
            new OrderSummary(rs.getLong("id"), rs.getString("status"), rs.getBigDecimal("amount"))
        );
    }
}
```

- **Sample Input & Output:**
```text
Criteria: { status: "PAID", minAmount: 100.0, sortBy: "amount; DROP TABLE orders; --", ascending: false }
Generated SQL: SELECT id, status, amount FROM orders WHERE 1=1 AND status = :status AND amount >= :minAmount ORDER BY id DESC LIMIT 50
Malicious injection thwarted! Safely defaulted to fallback column 'id'.
```

---

### Q17: How do you prevent Second-Order SQL Injection when persisting and later retrieving dynamic search expressions?
- **Scenario Context:** An enterprise reporting engine allows administrators to save custom SQL filter criteria into a `report_configs` table. When the daily report runs, the stored expression is loaded and dynamically concatenated into an active SQL query. An attacker stores `1=1 UNION SELECT password FROM users` in the config table.
- **What the Interviewer Evaluates:** Second-order injection vectors, stored query template risks, and AST expression parsing (e.g. JSqlParser).
- **Standout Technical Answer:**
  - **Second-Order SQL Injection** occurs when malicious payload is safely stored in the database (e.g., using parameterized queries), but later read out and concatenated into another dynamic query without re-validation.
  - **Defense Architecture:**
    1. **Never store raw SQL fragments:** Store structured JSON filters (`{"field": "status", "op": "EQ", "value": "ACTIVE"}`).
    2. **Expression AST Parsing**: If custom expressions must be supported, parse them through an AST validator (like **JSqlParser**) that strictly rejects unauthorized AST nodes (`Union`, `SubSelect`, `Drop`, `Alter`).
    3. **Schema Metadata Validation**: Validate all dynamic field names against the database's `DatabaseMetaData.getColumns()`.
- **Follow-Up Trap:** *"Why is escaping single quotes with `replace(\"'\", \"''\")` insufficient to prevent SQL injection?"*
  - *Winning Answer:* "Because escaping quotes only protects string literals! It does not protect numeric literals, column names, table names, or multi-byte character encoding exploits (such as `GBK` charset bypasses like `%bf%27`). Only parameter binding or AST validation provides mathematical defense."

#### Production Code Example - Q17: Safe JSON-Driven Dynamic Query Engine

- **Execution Steps:**
  1. Define structured `FilterRule` DTO.
  2. Map operators (`EQ`, `GTE`, `LTE`) to strict pre-compiled parameter templates.
  3. Validate field names against hard-coded schema catalog before execution.

- **Sample Code:**
```java
package com.enterprise.sql.security;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

public record FilterRule(String field, String operator, Object value) {}

@Service
public class SafeReportQueryEngine {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private static final Map<String, String> COLUMN_CATALOG = Map.of(
        "total", "order_total",
        "status", "order_status",
        "created", "created_at"
    );

    public SafeReportQueryEngine(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Map<String, Object>> executeSafeReport(List<FilterRule> rules) {
        StringBuilder sql = new StringBuilder("SELECT order_id, order_total, order_status FROM orders WHERE 1=1 ");
        MapSqlParameterSource params = new MapSqlParameterSource();

        for (int i = 0; i < rules.size(); i++) {
            FilterRule rule = rules.get(i);
            String dbColumn = COLUMN_CATALOG.get(rule.field());
            if (dbColumn == null) {
                throw new IllegalArgumentException("Unauthorized column field: " + rule.field());
            }

            String paramName = "param_" + i;
            switch (rule.operator()) {
                case "EQ" -> sql.append("AND ").append(dbColumn).append(" = :").append(paramName).append(" ");
                case "GTE" -> sql.append("AND ").append(dbColumn).append(" >= :").append(paramName).append(" ");
                default -> throw new UnsupportedOperationException("Operator not supported: " + rule.operator());
            }
            params.addValue(paramName, rule.value());
        }

        return jdbcTemplate.queryForList(sql.toString(), params);
    }
}
```

- **Sample Input & Output:**
```text
Incoming JSON Rules: [{"field": "status", "operator": "EQ", "value": "COMPLETED"}, {"field": "total", "operator": "GTE", "value": 500}]
Generated SQL: SELECT order_id, order_total, order_status FROM orders WHERE 1=1 AND order_status = :param_0 AND order_total >= :param_1
Safe execution guaranteed.
```

---

### Q18: How do you implement Multi-Tenant Data Isolation in Spring JDBC using PostgreSQL Row-Level Security (RLS)?
- **Scenario Context:** In a multi-tenant SaaS application, all tenant data resides in shared tables with a `tenant_id` column. A developer misses adding `WHERE tenant_id = :tenantId` in a new repository query, leaking Tenant B's data to Tenant A.
- **What the Interviewer Evaluates:** PostgreSQL Row-Level Security (`ENABLE ROW LEVEL SECURITY`), `SET LOCAL app.current_tenant`, and Spring JDBC connection interception.
- **Standout Technical Answer:**
  - Relying on developers to remember `WHERE tenant_id = ?` on every query is a recipe for catastrophic data leaks.
  - **PostgreSQL Row-Level Security (RLS) Defense:**
    1. On the table: `ALTER TABLE orders ENABLE ROW LEVEL SECURITY;`
    2. Create policy:
       `CREATE POLICY tenant_isolation_policy ON orders USING (tenant_id = current_setting('app.current_tenant'));`
    3. In Spring JDBC, whenever a connection is acquired from HikariCP, execute:
       `SET LOCAL app.current_tenant = '<tenant_id>';`
    4. PostgreSQL's kernel **automatically appends the tenant filter to every SQL query, insert, update, and delete**, making cross-tenant data leaks impossible even if the developer writes `SELECT * FROM orders`!
- **Follow-Up Trap:** *"Why must you use `SET LOCAL` instead of plain `SET`?"*
  - *Winning Answer:* "`SET app.current_tenant` sets the session variable for the entire physical connection! Because HikariCP pools and reuses connections across threads, subsequent requests on that connection would inherit the previous tenant's ID. `SET LOCAL` is scoped strictly to the current transaction and clears automatically on commit/rollback!"

#### Production Code Example - Q18: PostgreSQL RLS with Spring TransactionSynchronization

- **Execution Steps:**
  1. Enable RLS on database table.
  2. Implement repository executing `SET LOCAL app.current_tenant` at the start of transaction.
  3. Verify that queries return only the authenticated tenant's rows.

- **Sample Code:**
```java
package com.enterprise.sql.multitenancy;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Repository
public class MultiTenantOrderRepository {

    private final JdbcTemplate jdbcTemplate;

    public MultiTenantOrderRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public List<Map<String, Object>> getOrdersForTenant(String tenantId) {
        // Sets transaction-scoped session variable in PostgreSQL kernel
        jdbcTemplate.execute("SET LOCAL app.current_tenant = '" + tenantId + "'");

        // PostgreSQL RLS automatically enforces tenant_id = current_setting('app.current_tenant')!
        return jdbcTemplate.queryForList("SELECT id, customer, amount FROM orders");
    }
}
```

- **Sample Input & Output:**
```text
SET LOCAL app.current_tenant = 'tenant_corp_alpha';
SELECT id, customer, amount FROM orders;
PostgreSQL Kernel Rewritten Plan:
Seq Scan on orders (filter: tenant_id = 'tenant_corp_alpha')
Output: Only rows belonging to 'tenant_corp_alpha'. Cross-tenant leakage impossible.
```

---

# Category 6: Dynamic Routing DataSources & Read/Write Splitting

### Q19: How do you implement Dynamic Read/Write Database Splitting in Spring using `AbstractRoutingDataSource`?
- **Scenario Context:** A high-traffic system has 1 Primary writer database and 3 Read Replicas. You need all `@Transactional(readOnly = true)` methods to automatically route to a read replica, while all mutating transactions route to the primary writer.
- **What the Interviewer Evaluates:** `AbstractRoutingDataSource`, `TransactionSynchronizationManager.isCurrentTransactionReadOnly()`, and dynamic datasource routing.
- **Standout Technical Answer:**
  - Spring provides `AbstractRoutingDataSource`, which determines the target `DataSource` at runtime based on a dynamic lookup key returned by `determineCurrentLookupKey()`.
  - Spring's transaction manager inspects `@Transactional(readOnly = true)` and sets a flag in `TransactionSynchronizationManager`.
  - By checking `TransactionSynchronizationManager.isCurrentTransactionReadOnly()`, our routing datasource can return `DataSourceType.REPLICA` for read-only transactions and `DataSourceType.PRIMARY` for writes.
  - **The Catch:** Spring binds the database connection to the thread at the start of the transaction before business logic runs. Therefore, the routing decision is locked in for the entire duration of that transaction boundary.
- **Follow-Up Trap:** *"What happens if a `@Transactional(readOnly = false)` method calls a helper method annotated with `@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)`?"*
  - *Winning Answer:* "Under `Propagation.SUPPORTS`, the helper joins the outer transaction. The connection was already established to the Primary writer at the start of the outer transaction; thus, the read query executes on the Primary writer without routing to the replica."

#### Production Code Example - Q19: Dynamic Routing DataSource with Read/Write Splitting

- **Execution Steps:**
  1. Define `DataSourceType` enum (`PRIMARY`, `REPLICA`).
  2. Implement `AbstractRoutingDataSource` inspecting `TransactionSynchronizationManager.isCurrentTransactionReadOnly()`.
  3. Wrap with `LazyConnectionDataSourceProxy` to delay physical connection borrowing until first SQL execution.

- **Sample Code:**
```java
package com.enterprise.sql.routing;

import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public enum DataSourceType { PRIMARY, REPLICA }

public class DynamicRoutingDataSource extends AbstractRoutingDataSource {

    private final AtomicInteger counter = new AtomicInteger(0);

    @Override
    protected Object determineCurrentLookupKey() {
        boolean isReadOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly();
        return isReadOnly ? DataSourceType.REPLICA : DataSourceType.PRIMARY;
    }

    public static DataSource buildRoutingProxy(DataSource primary, DataSource replica) {
        DynamicRoutingDataSource routing = new DynamicRoutingDataSource();
        routing.setTargetDataSources(Map.of(
            DataSourceType.PRIMARY, primary,
            DataSourceType.REPLICA, replica
        ));
        routing.setDefaultTargetDataSource(primary);
        routing.afterPropertiesSet();

        // CRITICAL: LazyConnectionDataSourceProxy delays acquiring connection until first query!
        return new LazyConnectionDataSourceProxy(routing);
    }
}
```

- **Sample Input & Output:**
```text
2026-09-13T11:15:00.010Z DEBUG [main] c.e.s.r.DynamicRoutingDataSource : determineCurrentLookupKey() -> REPLICA (readOnly=true detected)
2026-09-13T11:15:00.025Z DEBUG [main] c.e.s.r.DynamicRoutingDataSource : determineCurrentLookupKey() -> PRIMARY (mutating transaction detected)
Read/Write queries cleanly routed to respective database clusters.
```

---

### Q20: Why is `LazyConnectionDataSourceProxy` strictly mandatory when using `AbstractRoutingDataSource` with Spring's `@Transactional`?
- **Scenario Context:** An engineer configures `AbstractRoutingDataSource` to route `@Transactional(readOnly = true)` to replicas. However, all queries continue hitting the Primary database writer.
- **What the Interviewer Evaluates:** Spring transaction initialization order, early connection checkout, and lazy proxy interception.
- **Standout Technical Answer:**
  - When a `@Transactional` method is entered:
    1. Spring's `DataSourceTransactionManager` immediately calls `dataSource.getConnection()`.
    2. At this moment, Spring has **not yet initialized the transaction synchronization state**!
    3. `AbstractRoutingDataSource.determineCurrentLookupKey()` runs, sees `isCurrentTransactionReadOnly() == false`, and checks out a connection to the **Primary**!
    4. Afterwards, Spring sets `isCurrentTransactionReadOnly = true`, but the connection is already acquired and bound to the thread!
  - **The Mandatory Fix: `LazyConnectionDataSourceProxy`**:
    - Wraps the routing datasource in a lazy proxy.
    - When Spring calls `getConnection()`, it receives a lightweight Java dynamic proxy without touching physical HikariCP pools.
    - Only when the application executes its first SQL statement (`Statement.execute()`) does the proxy actually call `determineCurrentLookupKey()`.
    - By that time, the transaction synchronization state is fully active, and the connection is correctly acquired from the **Replica**!
- **Follow-Up Trap:** *"Does `LazyConnectionDataSourceProxy` introduce any performance penalty?"*
  - *Winning Answer:* "No! In fact, it improves performance. If a `@Transactional` method contains input validation logic that fails and returns early before executing any SQL, zero physical connections are ever checked out from HikariCP!"

#### Production Code Example - Q20: Lazy Connection Routing Configuration Verification

- **Execution Steps:**
  1. Wrap `DynamicRoutingDataSource` bean in `LazyConnectionDataSourceProxy`.
  2. Execute read-only transaction and verify that replica connection acquisition is deferred until query dispatch.
  3. Validate zero connection checkout on pre-validation errors.

- **Sample Code:**
```java
package com.enterprise.sql.config;

import com.enterprise.sql.routing.DynamicRoutingDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;

import javax.sql.DataSource;

@Configuration
public class RoutingDataSourceConfig {

    @Bean
    @Primary
    public DataSource dataSource(@Qualifier("primaryDataSource") DataSource primary,
                                 @Qualifier("replicaDataSource") DataSource replica) {
        // Essential wrapper for dynamic routing!
        return DynamicRoutingDataSource.buildRoutingProxy(primary, replica);
    }
}
```

- **Sample Input & Output:**
```text
2026-09-13T11:20:00.001Z DEBUG [exec-1] o.s.j.d.DataSourceTransactionManager : Began @Transactional(readOnly = true)
2026-09-13T11:20:00.002Z DEBUG [exec-1] o.s.j.d.LazyConnectionDataSourceProxy : Connecting to target DataSource deferred until first SQL statement
2026-09-13T11:20:00.005Z DEBUG [exec-1] c.e.s.r.DynamicRoutingDataSource : determineCurrentLookupKey() -> REPLICA
2026-09-13T11:20:00.006Z DEBUG [exec-1] o.s.j.d.LazyConnectionDataSourceProxy : Obtained physical connection from REPLICA pool
Routing works 100% reliably.
```

---

## 🔥 Real-World War Room Outage Forensics

### Incident A: Connection Leak Outage via Missing Finally Block in Raw JDBC Cursors
- **Root Cause Forensics:** An engineer wrote an ETL data cleanup job using raw `dataSource.getConnection()`. Inside a `while(rs.next())` loop, an unhandled `NumberFormatException` was thrown on a corrupt row. Because the code did not use `try-with-resources`, the exception skipped `conn.close()`. Within 25 minutes, all 20 HikariCP connections leaked permanently.
- **Immediate Mitigation:** Restarted pods and deployed emergency hotfix wrapping the JDBC connection in `try-with-resources`.
- **Permanent Architectural Fix:** Enforced ArchUnit tests prohibiting direct calls to `DataSource.getConnection()`; all database queries must flow through Spring's `JdbcTemplate`, which enforces `finally` connection release at the bytecode level.

### Incident B: The 4-Hour Database Freeze (Missing statement_timeout on Migration Lock)
- **Root Cause Forensics:** A Flyway migration script issued `ALTER TABLE users ADD COLUMN vip BOOLEAN DEFAULT false`. An uncommitted background analytics transaction was holding an `ACCESS SHARE` lock on `users`. The `ALTER TABLE` requested an `ACCESS EXCLUSIVE` lock and was queued. Because PostgreSQL queues all subsequent read queries behind exclusive lock requests, every query in the application backed up, freezing the entire platform for 4 hours.
- **Immediate Mitigation:** Terminated the blocking background transaction via `SELECT pg_terminate_backend(pid)`.
- **Permanent Architectural Fix:** Configured `options=-c statement_timeout=5000 -c lock_timeout=2000` on all migration and application datasources to fail fast rather than queueing locks.

### Incident C: The 1,000-Connection Cascade Collapse (Oversized HikariCP Pools)
- **Root Cause Forensics:** Following a traffic surge, DevOps scaled microservice replicas from 5 to 50 pods while leaving `maximum-pool-size: 50`. This resulted in $50 \times 50 = 2,500$ connections targeting a 16-core PostgreSQL instance. PostgreSQL CPU hit 100% in OS thread scheduling, disk I/O queue length exceeded 400, and throughput plummeted from 12,000 queries/sec to 400 queries/sec.
- **Immediate Mitigation:** Reduced `maximum-pool-size` on all pods to 8 immediately.
- **Permanent Architectural Fix:** Deployed PgBouncer connection pooler in front of PostgreSQL and codified the mathematical sizing formula $(\text{Cores} \times 2) + \text{Spindles}$ into Helm chart values.

---

## ⚖️ Spring SQL Production Diagnostic Matrix

| Production Symptom | Low-Level Root Cause | Immediate Mitigation | Permanent Architectural Fix |
| :--- | :--- | :--- | :--- |
| **`SQLTransientConnectionException: Connection is not available`** | Threads holding connections during slow external HTTP/RPC calls. | Temporarily boost pool size | Use `TransactionTemplate` to isolate DB calls from HTTP |
| **`Connection reset by peer` after 5 min** | AWS NLB/Firewall dropping idle TCP socket before HikariCP retires it. | Restart pods | Set `max-lifetime: 270000` ($< 350\text{s}$) & `keepalive-time: 45000` |
| **PostgreSQL 100% CPU Context Switching** | Pool over-allocation ($> 500$ connections competing for 16 CPU cores). | Lower `maximum-pool-size` | Sizing formula: $(\text{Cores} \times 2) + \text{Spindles}$ (10–15/pod) |
| **`OutOfMemoryError: Java heap space` on export** | `RowMapper` or default driver buffering full 10M rows in client RAM. | Increase heap temporarily | Set `fetchSize(1000)` and stream via `RowCallbackHandler` |
| **`PSQLException: could not determine data type`** | Passing untyped `null` via `addValue("col", null)` defaulting to `Types.OTHER`. | Pass empty string | Explicitly pass SQL type: `addValue("col", null, Types.VARCHAR)` |
| **Read/Write Splitting always hitting Primary** | `AbstractRoutingDataSource` called before transaction synchronization active. | N/A | Wrap routing datasource in `LazyConnectionDataSourceProxy` |
| **`NestedTransactionNotSupportedException`** | Attempting `Propagation.NESTED` with JPA/Hibernate `JpaTransactionManager`. | Change to `REQUIRES_NEW` | Use `DataSourceTransactionManager` with pure Spring JDBC |
| **Unclosed Connection Leaks in Logs** | Raw JDBC connections missing `try-with-resources`. | Restart pods | Set `leak-detection-threshold: 2000` and mandate `JdbcTemplate` |

---

[🏠 Back to Home](README.md) | [🗄️ Spring SQL Master Guide](spring_sql.md) | [🍃 Spring Boot Master Guide](spring_master_guide.md) | [🏛️ SQL Scenarios Master Guide](sql_scenarios_master_guide.md)
