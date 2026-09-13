[🏠 Back to Home](README.md) | [🍃 Spring Boot Master Guide](spring_master_guide.md) | [🏛️ Spring Data JPA Guide](spring_data_jpa.md)

# 🗄️ Spring SQL & JDBC Enterprise Architecture Master Guide

A production-grade engineering handbook for high-performance relational database programming in Java using **Spring JDBC**, **`NamedParameterJdbcTemplate`**, and **HikariCP**. Learn how to bypass ORM overhead, write ultra-fast batch operations, stream millions of rows without Out-Of-Memory (OOM) errors, and tune connection pools for enterprise scale.

---

## 📑 Table of Contents

1. [🧠 Zero-to-Hero Mental Model: The Formula 1 Race Car vs The Luxury Limousine](#-the-formula-1-race-car-vs-the-luxury-limousine)
2. [🛠️ Prerequisites & Foundational Knowledge](#️-prerequisites--foundational-knowledge)
3. [📦 Track 1: The Junior & Entry-Level Foundations](#track-1-the-junior--entry-level-foundations-zero-to-hero)
4. [🚀 Track 2: Master Spring SQL Feature Catalog](#track-2-master-spring-sql-feature-catalog)
5. [🏗️ Track 3: Framework Internals & Exception Translation](#track-3-framework-internals--exception-translation)
6. [⚙️ Track 4: Production Engineering & HikariCP Sizing](#track-4-production-engineering--hikaricp-sizing)
7. [🚨 Track 5: War Room Post-Mortems & Root Cause Analysis (RCAs)](#track-5-war-room-post-mortems--root-cause-analysis-rcas)
8. [🎓 Track 6: Crack-The-Interview Question Bank (Senior & Staff+ Level)](#track-6-crack-the-interview-question-bank-senior--staff-level)
9. [⚖️ Spring SQL Master Cheat Sheet](#️-spring-sql-master-cheat-sheet)

---

## 🛠️ Prerequisites & Foundational Knowledge

Before writing custom SQL in Spring, developers must understand the foundational relational driver communication and database engine internals:

### 1. JDBC Protocol & Statement Compilation
- **Socket Network Communication**: Java communicates with databases via client-server network socket streams (e.g. PostgreSQL Frontend/Backend Protocol 3.0).
- **`Statement` vs `PreparedStatement`**:
  - `Statement`: Sends raw text SQL to the database on every execution. The database must parse the SQL AST, check permissions, and recompute the execution plan every single time. Vulnerable to SQL injection.
  - `PreparedStatement`: Pre-compiles parameterized SQL templates (`?` or `:param`) once. Bind variables are sent separately over the wire as typed binary parameters, neutralizing SQL injection and enabling database statement plan caching.

### 2. Result Streaming & Cursor Memory Limits
- **The Default Driver Trap**: By default, many JDBC drivers (including MySQL and PostgreSQL) pull the **entire SQL result set into JVM heap memory** before returning control to your Java code. If a query returns 2,000,000 rows, the JVM will crash with `OutOfMemoryError: Java heap space`.
- **Cursor Streaming (`setFetchSize`)**: By configuring `fetchSize` (e.g. 500 rows) and disabling autocommit, the database driver streams rows in bounded chunks, maintaining constant $O(1)$ memory consumption.

### 3. Connection Sockets & HikariCP Pooling
- **TCP Handshake Cost**: Establishing a new database connection requires a TCP 3-way handshake, TLS cryptographic negotiation, authentication exchange, session variable configuration, and server-side backend process fork—costing 30ms to 120ms.
- **HikariCP Zero-Overhead Pooling**: HikariCP maintains pre-warmed database sockets, lending connections to worker threads in $<100\text{ns}$ using lock-free thread-local `FastList` tracking.

---

# TRACK 1: THE JUNIOR & ENTRY-LEVEL FOUNDATIONS (ZERO-TO-HERO)

## 1. The Real-World Mental Model (The Formula 1 Race Car vs The Luxury Limousine)

- **Spring Data JPA / Hibernate (The Luxury Limousine):** It has heated leather seats, automatic parking, and a chauffeur. It makes day-to-day driving comfortable, but it is heavy and has hundreds of moving parts (dirty checking, L1 cache snapshots, Byte Buddy proxies). Great for standard CRUD and business domain entities.
- **Spring SQL / `NamedParameterJdbcTemplate` (The Formula 1 Race Car):** It has no leather seats or automatic air conditioning. It is stripped down to bare carbon fiber and a roaring engine.
  - *When do you use it?* When you need to insert 500,000 records in 2 seconds, execute analytical reporting queries across 15 tables, or stream millions of rows without blowing up your JVM Heap RAM, **Spring JDBC provides raw, blazing-fast speed and 100% control over the generated SQL**!

| Ingestion Pathway | Processing Pipeline & Mechanics | Reflection & Proxy Overhead | Memory & Heap Footprint | Throughput & Batch Performance | Determinism & Tuning Scope |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **High Overhead / Complex Graph (Hibernate / JPA)** | Java Entity ──► Persistence Context ──► Dirty-Check Snapshot ──► ActionQueue ──► Flush | High (Byte Buddy runtime proxies, reflection field access, interceptors) | High (Entity duplicates maintained in L1 cache for snapshot comparison) | Moderate ($\sim 5,000 - 15,000 \text{ ops/sec}$); batch insert requires explicit clear/flush | Low (Hibernate generates queries dynamically; implicit N+1 risks) |
| **Zero Overhead / High Performance (Spring JDBC Template)** | Java Record ──► `NamedParameterJdbcTemplate` ──► Direct Parameter Index Binding ──► JDBC Socket Packet | Zero (Direct getter access on Java Records/DTOs; zero dynamic proxies) | Minimal (Transient record streamed directly into PreparedStatement buffer) | Ultra-High ($\sim 100,000+ \text{ ops/sec}$); bulk array batch updates via wire protocol | Deterministic (100% hand-crafted SQL; predictable query execution plans) |

> [!NOTE]
> **Data Ingestion Pipelines:**
> - **ORM Graph Pathway:** `Java Entity` ──► `Hibernate ActionQueue` ──► `Dirty Checking Snapshot` ──► `L1 Cache Hydration` ──► `Flush Engine` ──► `SQL Driver`
> - **Direct JDBC Pathway:** `Java Record` ──► `NamedParameterJdbcTemplate` ──► `Direct Parameter Index Binding` ──► `Binary PreparedStatement Batch` ──► `SQL Socket`

---

## 2. The 5 Core Building Blocks

| Building Block | Responsibility | Real-World Analogy |
| :--- | :--- | :--- |
| **`NamedParameterJdbcTemplate`** | The premier Spring JDBC client for executing parameterized SQL using named parameters (`:id`). | A high-precision digital power tool. |
| **`RowMapper<T>`** | Maps a single database row (`ResultSet`) to a Java object/record. | An assembly line worker putting engine parts into a box. |
| **`ResultSetExtractor<T>`** | Extracts an entire multi-row result set into an arbitrary structure (e.g. grouped Map). | A cargo sorter organizing thousands of packages into categories. |
| **`RowCallbackHandler`** | Streams rows one-by-one without holding them in memory (ideal for writing CSV exports). | An auditor stamping documents as they glide past on a conveyor belt. |
| **`SqlParameterSource`** | Supplies parameter values to SQL statements (`MapSqlParameterSource`, `BeanPropertySqlParameterSource`). | The fuel intake pipe delivering gasoline to the engine. |

---

## 3. Beginner Code Walkthrough: Safe Parameterized Queries

```java
package com.example.sql.repository;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

public record AccountRecord(Long id, String accountNumber, BigDecimal balance, String status) {}

@Repository
public class AccountJdbcRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public AccountJdbcRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<AccountRecord> findByAccountNumber(String accountNumber) {
        String sql = """
            SELECT id, account_number, balance, status
            FROM accounts
            WHERE account_number = :accountNumber
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("accountNumber", accountNumber);

        return jdbcTemplate.query(sql, params, (rs, rowNum) -> new AccountRecord(
            rs.getLong("id"),
            rs.getString("account_number"),
            rs.getBigDecimal("balance"),
            rs.getString("status")
        )).stream().findFirst();
    }
}
```

---

## 4. Top 10 Junior Interview Questions

### Q1: Why should you use `NamedParameterJdbcTemplate` instead of classic `JdbcTemplate`?
- **ELI5 Answer:** *"Calling people by their names instead of shouting 'Hey you number 1, you number 2!' so you never mix up people if they change seats."*
- **Technical Answer:** *"Classic `JdbcTemplate` uses positional `?` placeholders, which are brittle and error-prone when query columns change. `NamedParameterJdbcTemplate` uses descriptive names (`:username`, `:email`), making code self-documenting and eliminating parameter index misalignment bugs."*

### Q2: What is the difference between `RowMapper` and `ResultSetExtractor`?
- **ELI5 Answer:** *"`RowMapper` paints one toy at a time; `ResultSetExtractor` looks at the entire toy box at once to count, group, or bundle them."*
- **Technical Answer:** *"`RowMapper<T>` is invoked once per row and maps each row independently to an object of type `T`. `ResultSetExtractor<T>` takes control of the entire `ResultSet` iteration, allowing you to construct complex non-1:1 hierarchical data structures (e.g. 1 parent with nested child lists) in a single pass."*

### Q3: How do you prevent SQL Injection in Spring JDBC?
- **ELI5 Answer:** *"Never paste raw notes written by strangers directly onto the commander's speech podium."*
- **Technical Answer:** *"Never concatenate user input directly into SQL strings (`"WHERE name = '" + input + "'"`). Always pass values as bind parameters using `NamedParameterJdbcTemplate` with `MapSqlParameterSource` or `BeanPropertySqlParameterSource`."*

### Q4: How does Spring handle `SQLException`?
- **ELI5 Answer:** *"Translating 50 different foreign languages into one simple English dictionary."*
- **Technical Answer:** *"Spring intercepts vendor-specific checked `java.sql.SQLException` instances and automatically translates them into Spring's unchecked `DataAccessException` hierarchy (e.g. `DuplicateKeyException`, `CannotAcquireLockException`) via `SQLErrorCodeSQLExceptionTranslator`."*

### Q5: What is the best way to insert 100,000 records using Spring JDBC?
- **ELI5 Answer:** *"Loading all 100,000 letters into 20 big shipping crates rather than walking to the mailbox 100,000 times."*
- **Technical Answer:** *"Use `batchUpdate()` with `SqlParameterSourceUtils.createBatch()` paired with JDBC batching configuration (`rewriteBatchedStatements=true` for MySQL or PostgreSQL batch execution)."*

### Q6: What does `GeneratedKeyHolder` do?
- **ELI5 Answer:** *"Asking the ticket counter for the seat number that was just printed on your boarding pass."*
- **Technical Answer:** *"It captures auto-generated primary keys returned by the database table engine upon executing an `INSERT` statement (`rs.getGeneratedKeys()`)."*

### Q7: Why is `RowCallbackHandler` used for large file exports?
- **ELI5 Answer:** *"Watching runners cross the finish line and writing their time on paper one by one, rather than trying to carry all 10,000 runners on your shoulders at the same time."*
- **Technical Answer:** *"`RowCallbackHandler` processes rows as an event-driven stream. Because it does not construct or accumulate an in-memory collection of objects, memory overhead remains $O(1)$ constant, completely preventing `OutOfMemoryError`."*

### Q8: What is `TransactionTemplate` and when do you use it?
- **ELI5 Answer:** *"Writing down your instructions on a piece of paper that says: 'Do all of these steps, and if anything catches fire, tear up the paper and start over.'"*
- **Technical Answer:** *"`TransactionTemplate` provides programmatic transaction demarcation. It is used when you need fine-grained control over transaction boundaries within a single method, or when self-invocation prevents `@Transactional` AOP proxies from intercepting calls."*

### Q9: What does `rewriteBatchedStatements=true` do in MySQL?
- **ELI5 Answer:** *"Merging 50 separate tiny letters into 1 single giant document with 50 paragraphs before mailing."*
- **Technical Answer:** *"By default, the MySQL JDBC driver executes batch updates as separate sequential statements over the socket. Setting `rewriteBatchedStatements=true` rewrites multiple `INSERT INTO t VALUES (1)` statements into a single multi-value `INSERT INTO t VALUES (1), (2), (3)...` query, increasing batch insert throughput by up to $10\times$."*

### Q10: How does `DataSourceUtils.getConnection(dataSource)` differ from `dataSource.getConnection()`?
- **ELI5 Answer:** *"Asking the office manager if there's already an active shared car for your team's current trip, rather than renting a brand-new car on your personal credit card."*
- **Technical Answer:** *"`dataSource.getConnection()` unconditionally fetches a new physical connection from the pool. `DataSourceUtils.getConnection(dataSource)` checks Spring's `TransactionSynchronizationManager` to retrieve the active transaction-bound connection for the current thread, ensuring multi-step repository calls participate in the same transaction."*

---

# TRACK 2: MASTER SPRING SQL FEATURE CATALOG

## Master Database Access Decision Matrix

| Technology | Execution Overhead | Memory Profile | Type-Safety | Best Used For | Anti-Pattern For |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **`NamedParameterJdbcTemplate`** | Zero ($<100\text{ns}$ reflection)| $O(1)$ streaming | Compile-time strings | High-throughput batch writes, complex joins, reports | Simple rapid CRUD scaffolding |
| **Spring Data JPA (Hibernate)** | High (Snapshots, L1 cache) | High | Entity-level | Domain entity state machines, enterprise CRUD | Bulk inserts of $>50,000$ rows |
| **jOOQ** | Low (DSL compilation) | Moderate | Full Compile-time SQL | Complex type-safe SQL, stored procedures | Rapid low-budget prototyping |
| **MyBatis** | Low | Low | XML/Annotation SQL | Legacy systems with externalized SQL | Compile-time safe domain models |

---

## 2.1 `NamedParameterJdbcTemplate` & Parameter Mapping

1. **Architectural Overview & Purpose**:
   - Eliminates positional index mistakes (`?` parameter 1, parameter 2) by binding parameters by explicit name (`:accountId`, `:amount`). It uses `PreparedStatement` under the hood, completely neutralizing SQL injection attacks while allowing database query plan caching.

2. **Underlying Parameter Binding Mechanism**:
   - Parses the SQL template string at startup into a tokenized AST.
   - When executed, it extracts named parameters from `SqlParameterSource` (e.g., `MapSqlParameterSource` or `BeanPropertySqlParameterSource`), rewrites the SQL to standard JDBC positional `?` markers, and binds parameters using exact JDBC data types (`Types.BIGINT`, `Types.VARCHAR`).

3. **Concrete Code Examples with Sample Values & Expected Results**:
   ```java
   @Repository
   public class AccountJdbcRepository {
       private final NamedParameterJdbcTemplate jdbcTemplate;

       public AccountJdbcRepository(NamedParameterJdbcTemplate jdbcTemplate) {
           this.jdbcTemplate = jdbcTemplate;
       }

       public Optional<Account> findAccount(String accountNumber) {
           String sql = """
               SELECT id, account_number, balance, status
               FROM accounts
               WHERE account_number = :accountNumber
               """;

           SqlParameterSource params = new MapSqlParameterSource("accountNumber", accountNumber);

           List<Account> accounts = jdbcTemplate.query(sql, params, (rs, rowNum) -> new Account(
               rs.getLong("id"),
               rs.getString("account_number"),
               rs.getBigDecimal("balance"),
               rs.getString("status")
           ));
           return accounts.stream().findFirst();
       }
   }
   ```
   - **Sample Output**:
     ```text
     Account[id=101, accountNumber="ACC-8921", balance=15450.50, status="ACTIVE"]
     ```

4. **Pros & Cons**:
   - **Pros**: 100% immune to SQL injection; zero ORM reflection overhead; full control over database-specific SQL features.
   - **Cons**: Requires manual SQL writing and `RowMapper` boilerplate (unless using Java Records with `DataClassRowMapper`).

---

## 2.2 HikariCP Pool Sizing Formula & Leak Detection

1. **Architectural Overview & Purpose**:
   - HikariCP is the default high-performance JDBC connection pool in Spring Boot. It uses lock-free micro-optimizations, volatile reads, and custom byte-code generation to deliver connections in under 100 nanoseconds.

2. **The Famous PostgreSQL Pool Sizing Formula**:
   $$\text{Pool Size} = (2 \times \text{CPU Cores}) + \text{Effective Spindle Count}$$
   - For an 8-core database server with SSD storage:
     $$\text{Pool Size} = (2 \times 8) + 1 = 17 \text{ connections}$$
   - *Why smaller pools are faster:* An oversized pool (e.g. 200 connections) causes the database CPU to thrash on context switching, disk I/O lock queues, and memory buffer contention.

3. **Production `application.yml` Setup with Leak Detection**:
   ```yaml
   spring:
     datasource:
       hikari:
         maximum-pool-size: 20
         minimum-idle: 10
         idle-timeout: 300000        # 5 minutes
         max-lifetime: 1800000       # 30 minutes (must be shorter than DB timeout!)
         connection-timeout: 2000    # 2 seconds (fails fast if pool exhausted)
         leak-detection-threshold: 5000 # Warns if connection held >5s without closing!
   ```

---

## 2.3 Ultra-Fast Batch Inserts (100,000 Rows in $<2$ Seconds)

1. **Architectural Overview & Purpose**:
   - Inserting 100,000 rows one-by-one requires 100,000 network round-trips. Using JDBC Batching groups rows into a single multi-row TCP payload.

2. **Crucial Driver Rewrite Settings**:
   - **PostgreSQL**: Must add `?reWriteBatchedInserts=true` to JDBC URL!
   - **MySQL**: Must add `?rewriteBatchedStatements=true` to JDBC URL!
   - Without these driver flags, standard JDBC drivers still send individual insert statements over the socket!

3. **Production Batch Insert Blueprint**:
   ```java
   @Service
   public class BulkPaymentIngestionService {
       private final NamedParameterJdbcTemplate jdbcTemplate;
       public BulkPaymentIngestionService(NamedParameterJdbcTemplate jdbcTemplate) {
           this.jdbcTemplate = jdbcTemplate;
       }

       @Transactional
       public void ingestPayments(List<PaymentRecord> payments) {
           String sql = """
               INSERT INTO payments (payment_id, account_id, amount, status, created_at)
               VALUES (:paymentId, :accountId, :amount, :status, NOW())
               """;

           SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(payments);
           jdbcTemplate.batchUpdate(sql, batch);
       }
   }
   ```

---

## 2.4 Streaming 10M Rows to CSV with Constant $O(1)$ Memory

1. **Architectural Overview & Purpose**:
   - Traditional `jdbcTemplate.query()` loads all rows into a `List<T>` on the heap. If querying 10,000,000 rows, the JVM crashes with `OutOfMemoryError`.
   - Using `RowCallbackHandler` with cursor streaming guarantees constant $O(1)$ heap memory.

2. **Production Streaming Blueprint**:
   ```java
   @Service
   public class AuditReportExportService {
       private final NamedParameterJdbcTemplate jdbcTemplate;
       public AuditReportExportService(NamedParameterJdbcTemplate jdbcTemplate) {
           this.jdbcTemplate = jdbcTemplate;
       }

       @Transactional(readOnly = true) // Crucial: maintains cursor open
       public void exportLargeAuditLog(Writer csvWriter) {
           String sql = "SELECT id, user_id, action, created_at FROM audit_logs ORDER BY id ASC";

           // Set cursor fetch size to stream in chunks of 1000 from database socket
           jdbcTemplate.getJdbcTemplate().setFetchSize(1000);

           jdbcTemplate.query(sql, Collections.emptyMap(), rs -> {
               try {
                   // Invoked once per row; row is immediately written to disk and garbage collected!
                   csvWriter.write(String.format("%d,%d,%s,%s\n",
                       rs.getLong("id"),
                       rs.getLong("user_id"),
                       rs.getString("action"),
                       rs.getTimestamp("created_at")
                   ));
               } catch (IOException e) {
                   throw new RuntimeException("CSV stream error", e);
               }
           });
       }
   }
   ```

---

## 2.5 1-to-Many Hierarchies with `ResultSetExtractor`

1. **Architectural Overview**:
   - Avoids the N+1 problem by joining parent and child tables in a single SQL query and grouping the hierarchical graph in Java memory:
   ```java
   public record CustomerWithOrders(Long id, String name, List<String> orders) {}

   @Component
   public class CustomerOrdersExtractor implements ResultSetExtractor<List<CustomerWithOrders>> {
       @Override
       public List<CustomerWithOrders> extractData(ResultSet rs) throws SQLException {
           Map<Long, CustomerWithOrders> customerMap = new LinkedHashMap<>();

           while (rs.next()) {
               Long customerId = rs.getLong("customer_id");
               CustomerWithOrders customer = customerMap.computeIfAbsent(customerId, id -> {
                   try {
                       return new CustomerWithOrders(id, rs.getString("name"), new ArrayList<>());
                   } catch (SQLException e) {
                       throw new RuntimeException(e);
                   }
               });
               String orderNumber = rs.getString("order_number");
               if (orderNumber != null) {
                   customer.orders().add(orderNumber);
               }
           }
           return new ArrayList<>(customerMap.values());
       }
   }
   ```

---

## 2.6 Programmatic Transaction Demarcation via `TransactionTemplate`

1. **Architectural Overview & Purpose**:
   - `@Transactional` annotations rely on Spring AOP proxies and fail if called internally (`this.method()`).
   - `TransactionTemplate` provides programmatic, fine-grained demarcation without proxy traps.

2. **Production Blueprint**:
   ```java
   @Service
   public class WalletTransferService {
       private final TransactionTemplate txTemplate;
       public WalletTransferService(PlatformTransactionManager txManager) {
           this.txTemplate = new TransactionTemplate(txManager);
           this.txTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
           this.txTemplate.setTimeout(5); // 5s timeout
       }

       public boolean executeTransfer(Long fromId, Long toId, BigDecimal amount) {
           return Boolean.TRUE.equals(txTemplate.execute(status -> {
               try {
                   debitAccount(fromId, amount);
                   creditAccount(toId, amount);
                   return true;
               } catch (Exception ex) {
                   status.setRollbackOnly(); // Trigger explicit rollback
                   return false;
               }
           }));
       }
       private void debitAccount(Long id, BigDecimal amt) {}
       private void creditAccount(Long id, BigDecimal amt) {}
   }
   ```

---

## 2.7 Transaction Propagation Behaviors

1. **The 3 Most Critical Propagations**:
   - **`REQUIRED` (Default)**: Joins current active transaction if one exists; creates a new one if none exists.
   - **`REQUIRES_NEW`**: Suspends current transaction and opens a brand new, independent physical transaction. **Essential for audit logging** (audit record must persist even if outer business transaction rolls back).
   - **`NESTED`**: Executes within a nested transaction using JDBC **Savepoints**. Rolling back the nested transaction rolls back only to the savepoint without aborting the outer transaction.

---

## 2.8 Database Generated Keys & Sequences (`KeyHolder`)

1. **Retrieving Auto-Increment / Identity IDs**:
   ```java
   public Long insertAccount(String name) {
       String sql = "INSERT INTO accounts (name) VALUES (:name)";
       KeyHolder keyHolder = new GeneratedKeyHolder();

       MapSqlParameterSource params = new MapSqlParameterSource("name", name);
       jdbcTemplate.update(sql, params, keyHolder, new String[]{"id"});

       return keyHolder.getKey().longValue();
   }
   ```

---

## 2.9 Stored Procedures & Multi-Result Sets with `SimpleJdbcCall`

1. **Architectural Overview**:
   - `SimpleJdbcCall` reads database catalog metadata on initialization, auto-detecting parameters and return types:
   ```java
   @Service
   public class StoredProcService {
       private final SimpleJdbcCall procReadUserBalance;

       public StoredProcService(DataSource dataSource) {
           this.procReadUserBalance = new SimpleJdbcCall(dataSource)
               .withProcedureName("get_customer_balance")
               .declareParameters(
                   new SqlParameter("p_customer_id", Types.BIGINT),
                   new SqlOutParameter("p_balance", Types.DECIMAL)
               );
       }

       public BigDecimal getCustomerBalance(Long customerId) {
           Map<String, Object> out = procReadUserBalance.execute(Map.of("p_customer_id", customerId));
           return (BigDecimal) out.get("p_balance");
       }
   }
   ```

---

## 2.10 Mapping Postgres JSONB & Custom Types

1. **Architectural Overview**:
   - Modern microservices store unstructured metadata in PostgreSQL `JSONB` columns.
   - JDBC handles JSON via `PGobject`:
   ```java
   public record UserPreferences(String theme, boolean emailNotifications) {}

   // Mapping to PGobject for insert
   PGobject jsonObject = new PGobject();
   jsonObject.setType("jsonb");
   jsonObject.setValue(objectMapper.writeValueAsString(preferences));
   params.addValue("preferences", jsonObject);

   // Reading JSONB in RowMapper
   String rawJson = rs.getString("preferences");
   UserPreferences prefs = objectMapper.readValue(rawJson, UserPreferences.class);
   ```

---

# TRACK 3: FRAMEWORK INTERNALS & EXCEPTION TRANSLATION

## 3.1 Spring JDBC Exception Translation Architecture

| Vendor SQLException Component | Translation Mechanism | Resolution Rules (`sql-error-codes.xml`) | Unified Spring Exception | Handling & Recovery Strategy |
| :--- | :--- | :--- | :--- | :--- |
| **PostgreSQL Error 23505 / MySQL 1062** | `SQLErrorCodeSQLExceptionTranslator` | Matches vendor unique constraint violation error code | `DuplicateKeyException` (extends `DataIntegrityViolationException`) | Idempotent upsert fallback or client 409 Conflict HTTP response |
| **PostgreSQL Error 23503 / MySQL 1452** | `SQLErrorCodeSQLExceptionTranslator` | Matches foreign key constraint failure code | `DataIntegrityViolationException` | Fail-fast validation; reject invalid relational reference |
| **PostgreSQL Error 40P01 / MySQL 1213** | `SQLErrorCodeSQLExceptionTranslator` | Matches deadlock detected error code | `CannotAcquireLockException` | Exponential backoff retry via Spring `@Retryable` |
| **PostgreSQL Error 57014 / MySQL 1317** | `SQLErrorCodeSQLExceptionTranslator` | Matches statement execution timeout | `QueryTimeoutException` | Circuit breaker trip; review query index cardinality and execution plan |
| **Generic / Unknown Vendor Code** | `SQLStateSQLExceptionTranslator` | Fallback translator evaluating 5-character ANSI SQLState string | `UncategorizedSQLException` | Alert on-call SRE; log complete SQLState and error code for triage |

> [!IMPORTANT]
> **Exception Translation Pipeline:**
> `Raw java.sql.SQLException (Vendor Error Code)` ──► `SQLErrorCodeSQLExceptionTranslator` ──► `sql-error-codes.xml Evaluation` ──► `Spring org.springframework.dao.DataAccessException (Unchecked Hierarchy)`

---

## 3.2 Thread-Bound Connection Management

Spring coordinates connections using **`TransactionSynchronizationManager`**:
- When a `@Transactional` boundary starts, Spring borrows a connection from HikariCP and binds it to a `ThreadLocal<Map<Object, Object>>`.
- Every `jdbcTemplate.query()` invocation calls `DataSourceUtils.getConnection(dataSource)`, which inspects the thread-local map and reuses the active connection.
- Upon commit or rollback, Spring unbinds the connection and returns it to HikariCP.

---

# TRACK 4: PRODUCTION ENGINEERING & HIKARICP SIZING

## 4.1 HikariCP Connection Pool Sizing Formula

Setting `maximum-pool-size: 100` on a standard database server causes severe disk thrashing and CPU context-switch overhead.

### The PostgreSQL & MySQL Pool Sizing Formula:
$$\text{Pool Size} = (\text{CPU Cores} \times 2) + \text{Effective Spindle Count}$$
*Example:* A dedicated 8-core database server with SSD storage ($1\text{ spindle}$):
$$\text{Pool Size} = (8 \times 2) + 1 = 17\text{ connections}$$

### Production `application.yml` Tuning
```yaml
spring:
  datasource:
    hikari:
      pool-name: ProductionHikariPool
      maximum-pool-size: 20
      minimum-idle: 20                  # Fixed-size pool prevents scale-up latency
      idle-timeout: 300000              # 5 minutes
      max-lifetime: 1800000             # 30 minutes
      connection-timeout: 3000          # 3 seconds: Fail fast if pool exhausted
      leak-detection-threshold: 2000    # Warns if connection held > 2000ms!
      data-source-properties:
        cachePrepStmts: true
        prepStmtCacheSize: 250
        prepStmtCacheSqlLimit: 2048
        rewriteBatchedStatements: true  # Mandatory for MySQL batching!
```

---

# TRACK 5: WAR ROOM POST-MORTEMS & ROOT CAUSE ANALYSIS (RCAs)

## Incident 1: Connection Pool Starvation via Missing Transaction Timeout

- **Severity:** P0 Outage (API frozen, all threads blocked)
- **Mean Time to Recovery (MTTR):** 24 minutes
- **Symptoms:** Under high traffic, all incoming requests threw `SQLTransientConnectionException: Connection is not available, request timed out after 3000ms`.
- **Root Cause:** A long-running reporting batch job held an open `@Transactional` connection while making a 45-second HTTP call to an external CRM. HikariCP connections remained checked out, starving client endpoints.
- **The Permanent Fix:**
  1. Never make remote network/REST calls inside database transactions.
  2. Set `spring.datasource.hikari.leak-detection-threshold: 2000` to automatically log stack traces of threads holding connections $>2\text{s}$.

---

## Incident 2: OutOfMemoryError via Unbounded `queryForList()`

- **Severity:** P0 Crash (Tomcat pods killed by Linux OOM killer)
- **Symptoms:** Daily CSV export job crashed pods every midnight.
- **Root Cause:** A developer executed `jdbcTemplate.queryForList("SELECT * FROM audit_logs")` over a 5,000,000-row table. Jackson attempted to serialize the list, consuming 6 GB of heap memory in seconds.
- **The Permanent Fix:**
  Switched to `RowCallbackHandler` with `setFetchSize(1000)` to stream rows directly to disk.

---

# TRACK 6: CRACK-THE-INTERVIEW QUESTION BANK (SENIOR & STAFF+ LEVEL)

### 1. What is the fundamental difference between `execute()`, `query()`, and `update()` in `JdbcTemplate`?
`update()` executes DML statements (`INSERT`, `UPDATE`, `DELETE`) and returns the number of rows affected (`int`). `query()` executes queries returning rows (`SELECT`) and delegates to a mapper/extractor. `execute()` executes arbitrary DDL/SQL (`CREATE TABLE`, stored procedures) and accepts low-level callbacks like `ConnectionCallback` or `StatementCallback`.

### 2. How do you handle deadlocks gracefully in Spring SQL?
Catch Spring's `CannotAcquireLockException` or `DeadlockLoserDataAccessException` and apply Spring Retry:
```java
@Retryable(retryFor = {CannotAcquireLockException.class}, maxAttempts = 3, backoff = @Backoff(delay = 100))
public void updateBalanceWithRetry(Long accountId, BigDecimal amount) { ... }
```

---

## ⚖️ Spring SQL Master Cheat Sheet

| Task / Feature | High-Performance Production Syntax |
| :--- | :--- |
| **Named Parameter Query** | `jdbcTemplate.query(sql, params, rowMapper)` |
| **Single Row Query** | `jdbcTemplate.queryForObject(sql, params, Class)` |
| **Bulk Batch Insert** | `jdbcTemplate.batchUpdate(sql, SqlParameterSourceUtils.createBatch(list))` |
| **O(1) Memory Stream** | `jdbcTemplate.query(sql, params, (RowCallbackHandler) rs -> write(rs))` |
| **Get Inserted ID** | `jdbcTemplate.update(sql, params, keyHolder, new String[]{"id"})` |
| **Programmatic Tx** | `transactionTemplate.execute(status -> { ... })` |
| **Connection Leak Alert**| `spring.datasource.hikari.leak-detection-threshold: 2000` |

---
[🏠 Back to Home](README.md) | [🍃 Spring Boot Master Guide](spring_master_guide.md) | [🏛️ Spring Data JPA Guide](spring_data_jpa.md)
