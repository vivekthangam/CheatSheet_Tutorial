[🏠 Back to Home](README.md)

# 🗄️ Spring SQL & JDBC Enterprise Architecture Master Guide

A production-grade engineering handbook for high-performance relational database programming in Java using **Spring JDBC**, **`NamedParameterJdbcTemplate`**, and **HikariCP**. Learn how to bypass ORM overhead, write ultra-fast batch operations, stream millions of rows without Out-Of-Memory (OOM) errors, and tune connection pools for enterprise scale.

---

## 📑 Table of Contents

### 🟢 Track 1: Junior & Entry-Level Foundations
1. [🧠 The Real-World Mental Model (The Luxury Limousine vs The Formula 1 Race Car)](#1-the-real-world-mental-model-the-luxury-limousine-vs-the-formula-1-race-car)
2. [🧱 The 5 Core Building Blocks](#2-the-5-core-building-blocks)
3. [💻 Beginner Code Walkthrough: Safe Parameterized Queries](#3-beginner-code-walkthrough-safe-parameterized-queries)
4. [💥 What Happens When Things Break? (Connection Leaks & SQL Injection)](#4-what-happens-when-things-break-connection-leaks--sql-injection)
5. [⚠️ Top 5 Beginner Mistakes in Production](#5-top-5-beginner-mistakes-in-production)
6. [🎯 Top 10 Junior Interview Questions (With "Explain Like I'm 5" Answers)](#6-top-10-junior-interview-questions-with-explain-like-im-5-answers)

### 🔴 Track 2: Advanced Architecture & Production Engineering
1. [⚙️ 1. Core APIs: JdbcTemplate vs NamedParameterJdbcTemplate](#️-1-core-apis-jdbctemplate-vs-namedparameterjdbctemplate)
2. [🗺️ 2. Result Mapping: RowMapper, ResultSetExtractor & RowCallbackHandler](#-2-result-mapping-rowmapper-resultsetextractor--rowcallbackhandler)
3. [⚡ 3. Ultra-Fast Batch Inserts & Bulk Updates](#-3-ultra-fast-batch-inserts--bulk-updates)
4. [💧 4. HikariCP Connection Pool Architecture & Performance Tuning](#-4-hikaricp-connection-pool-architecture--performance-tuning)
5. [🛡️ 5. Programmatic vs Declarative Transactions (TransactionTemplate)](#-5-programmatic-vs-declarative-transactions-transactiontemplate)
6. [🏭 6. Production Scenarios & War Room Incident Forensics](#-6-production-scenarios--war-room-incident-forensics)
7. [⚖️ 7. Spring SQL Master Cheat Sheet](#️-7-spring-sql-master-cheat-sheet)

---

# TRACK 1: THE JUNIOR & ENTRY-LEVEL FOUNDATIONS (ZERO-TO-HERO)

## 1. The Real-World Mental Model (The Luxury Limousine vs The Formula 1 Race Car)

### JPA vs Spring SQL (JDBC): When Do You Use Which?
- **Spring Data JPA / Hibernate (The Luxury Limousine):** It has heated leather seats, air conditioning, automatic transmission, and a chauffeur. It makes driving comfortable, but it is heavy and has a lot of moving parts. Great for typical CRUD operations and managing entity business rules.
- **Spring SQL / `NamedParameterJdbcTemplate` (The Formula 1 Race Car):** It has no leather seats, no air conditioning, and no automatic radio. It is stripped down to bare carbon fiber and a roaring engine.
  - *Why do we use it?* When you need to insert 500,000 payment records in 2 seconds, execute complex analytical reporting joins across 15 tables, or stream millions of rows without blowing up your JVM Heap RAM, **Spring JDBC provides raw, blazing-fast speed and 100% control over the generated SQL**!

---

### The Two Pathways

```
┌──────────────────────────────────────────────────────────────────────────────────┐
│                             DATABASE INGESTION PATHWAYS                          │
│                                                                                  │
│  [ High Overhead / Complex Graph ]                                               │
│  Java Object ──► [ Hibernate/JPA ] ──► Dirty Checking ──► 1st Level Cache ──┐    │
│                                                                              │    │
│                                                                              ▼    │
│  [ Zero Overhead / High Performance ]                                   [ SQL Engine ]
│  Java Record ──► [ NamedParameterJdbcTemplate ] ──► Direct Batch Packet ─────┘    │
│                  - No Reflection Session Overhead                                │
│                  - Direct PreparedStatement Binding                              │
│                  - Deterministic Query Execution Plans                           │
└──────────────────────────────────────────────────────────────────────────────────┘
```

---

## 2. The 5 Core Building Blocks

| Term | What It Means | Real-World Analogy |
| :--- | :--- | :--- |
| **`JdbcTemplate`** | Spring's core class that manages opening/closing DB connections, statements, and exceptions. | A certified power tool that handles all the safety switches for you. |
| **`NamedParameterJdbcTemplate`** | A wrapper allowing named variables (`:userId`) instead of confusing positional `?` marks. | Writing names on envelopes instead of just numbering them 1, 2, 3. |
| **`RowMapper<T>`** | A single functional method `mapRow(rs, rowNum)` that turns 1 SQL row into 1 Java object. | A factory worker taking raw dough from the oven and putting it into a bread box. |
| **HikariCP** | The ultra-fast, zero-overhead database connection pool (default in Spring Boot). | A carpool station keeping 10 warm running cars ready for workers to drive. |
| **`PreparedStatement`** | A pre-compiled SQL template that prevents SQL injection attacks. | A fill-in-the-blank form where user input is treated strictly as data, never code. |

---

## 3. Beginner Code Walkthrough: Safe Parameterized Queries

### Clean Spring Boot 3 DAO (`UserJdbcRepository.java`)
```java
package com.example.sql.repository;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserJdbcRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    // 1. Reusable RowMapper: maps SQL ResultSet to Java Record
    private static final RowMapper<UserRecord> USER_ROW_MAPPER = (rs, rowNum) -> new UserRecord(
        rs.getLong("id"),
        rs.getString("username"),
        rs.getString("email"),
        rs.getDouble("balance")
    );

    public UserJdbcRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // 2. Safe Parameterized Query (NO SQL INJECTION RISK!)
    public Optional<UserRecord> findByUsername(String username) {
        String sql = """
            SELECT id, username, email, balance 
            FROM app_users 
            WHERE username = :username
            """;

        MapSqlParameterSource params = new MapSqlParameterSource("username", username);

        return jdbcTemplate.query(sql, params, USER_ROW_MAPPER)
            .stream()
            .findFirst();
    }
}

// Java 17+ Record for zero-boilerplate DTO
record UserRecord(Long id, String username, String email, Double balance) {}
```

---

## 4. What Happens When Things Break? (Connection Leaks & SQL Injection)

1. **SQL Injection Vulnerability:**
   ```java
   // ❌ NEVER DO THIS: Attackers can pass "' OR '1'='1" and dump the entire database!
   String sql = "SELECT * FROM users WHERE email = '" + userInput + "'";
   
   // ✅ ALWAYS DO THIS: PreparedStatement parameterization separates code from data!
   String sql = "SELECT * FROM users WHERE email = :email";
   ```
2. **HikariCP Pool Exhaustion (`ConnectionTimeoutException: Connection is not available, request timed out after 30000ms`):**
   All connections in the pool are checked out and never returned. Usually caused by doing slow external work (like calling a 5-second third-party REST payment API) inside an open `@Transactional` database method, holding the connection hostage!

---

## 5. Top 5 Beginner Mistakes in Production

1. **SQL Injection via String Concatenation:** Concatenating user inputs into SQL strings. Always use `:namedParameters` or `?` placeholders.
2. **Confusing Positional Parameters:** In `JdbcTemplate`, using `?` for 15 columns. If you swap parameter #4 and #5, you save phone numbers into the address column with zero compiler errors! **Fix:** Always use `NamedParameterJdbcTemplate`.
3. **Over-Sizing HikariCP (`maximumPoolSize = 500`):** Beginners think 500 connections = faster app. In reality, a database disk and CPU can only handle a few dozen concurrent queries. 500 connections cause CPU core thrashing, context switches, and database lock starvation. **Hikari Rule:** $\text{Pool Size} = (\text{CPU Cores} \times 2) + \text{Disk Spindles}$ (usually 10–30 is plenty!).
4. **Fetching 1,000,000 Rows into Memory with `queryForList()`:** Loads 1,000,000 Java objects into JVM heap at once, crashing the server with `OutOfMemoryError`. **Fix:** Stream large datasets using `RowCallbackHandler` or pagination.
5. **Slow External Network Calls Inside Database Transactions:** Opening a transaction, reserving a DB connection, and then calling an external HTTP API. If the API takes 5 seconds, you exhaust your Hikari connection pool in seconds!

---

## 6. Top 10 Junior Interview Questions (With "Explain Like I'm 5" Answers)

### Q1: What is the difference between `JdbcTemplate` and `NamedParameterJdbcTemplate`?
- **ELI5 Answer:** *"`JdbcTemplate` uses question marks `?` where you must remember the exact order of numbers. `NamedParameterJdbcTemplate` lets you write names like `:username`, so you never mix them up."*
- **Technical Answer:** *"`JdbcTemplate` uses standard JDBC positional placeholders (`?`), which are prone to ordering mistakes when queries change. `NamedParameterJdbcTemplate` replaces named placeholders (e.g. `:email`) with bound parameters at runtime, improving code readability and maintainability."*

### Q2: What is a `RowMapper` and how does it work?
- **ELI5 Answer:** *"A translation machine that reads one row of spreadsheet cells and builds a toy out of them."*
- **Technical Answer:** *"`RowMapper<T>` is a core Spring callback interface. Its `mapRow(ResultSet rs, int rowNum)` method is invoked once for each row in the SQL `ResultSet`, extracting column values and instantiating a typed domain object or DTO."*

### Q3: What is the difference between `RowMapper`, `ResultSetExtractor`, and `RowCallbackHandler`?
- **ELI5 Answer:** *"`RowMapper` translates 1 row at a time. `ResultSetExtractor` looks at the whole table at once to build a family tree. `RowCallbackHandler` writes each row straight to a CSV file without saving anything in memory."*
- **Technical Answer:** *"`RowMapper` maps each individual row to an object, accumulating them in a `List`. `ResultSetExtractor` gives you full control over the entire `ResultSet` (useful for mapping 1:N parent-child hierarchies in 1 query). `RowCallbackHandler` streams rows row-by-row with no return value, ideal for writing huge datasets directly to disk/stream."*

### Q4: How does a Database Connection Pool like HikariCP work?
- **ELI5 Answer:** *"Instead of buying a new car every morning and throwing it in the river every evening, you keep 10 cars in a garage and borrow one whenever you need to drive to the store."*
- **Technical Answer:** *"Opening a TCP database connection and performing TLS/auth handshakes takes tens of milliseconds. A connection pool (HikariCP) pre-allocates a pool of active physical connections, lending them to threads for queries and returning them to the pool on close, achieving sub-millisecond connection checkout."*

### Q5: What causes SQL Injection and how do PreparedStatements stop it?
- **ELI5 Answer:** *"If a bad guy writes a computer command on their name tag, and the computer blindly executes the name tag! A PreparedStatement treats the name tag strictly as text, never as a command."*
- **Technical Answer:** *"SQL injection occurs when unsanitized user inputs are concatenated into raw SQL, allowing malicious input to alter query syntax. A `PreparedStatement` pre-compiles the SQL query structure in the database engine first, binding user inputs strictly as literal values through parameter slots."*

### Q6: What is the difference between `execute()`, `update()`, and `query()` in `JdbcTemplate`?
- **ELI5 Answer:** *"`execute` is for building tables (`CREATE TABLE`). `update` is for changing data (`INSERT`, `UPDATE`, `DELETE`). `query` is for asking questions (`SELECT`)."*
- **Technical Answer:** *"`execute()` runs arbitrary DDL/DML statements with no mapped return value. `update()` executes DML statements (`INSERT`, `UPDATE`, `DELETE`) returning the number of affected rows (`int`). `query()` executes SQL `SELECT` queries, mapping returned rows to objects via `RowMapper` or `ResultSetExtractor`."*

### Q7: What is JDBC Batching and why is it faster?
- **ELI5 Answer:** *"Instead of mailing 1,000 letters by sending 1,000 mailmen on 1,000 separate bicycles, you put all 1,000 letters into 1 single mail truck."*
- **Technical Answer:** *"JDBC batching (`batchUpdate`) combines multiple `INSERT` or `UPDATE` operations into a single network packet sent over TCP to the database server, reducing network round-trip latency and boosting write throughput by up to 50x."*

### Q8: How does Spring translate vendor-specific SQL Exceptions?
- **ELI5 Answer:** *"Postgres, Oracle, and MySQL all speak different dialects when angry. Spring translates all their different complaints into one single clear English warning."*
- **Technical Answer:** *"Raw JDBC throws checked `SQLException` containing vendor error codes. Spring's `SQLErrorCodeSQLExceptionTranslator` maps these vendor codes into a clean, unchecked `DataAccessException` hierarchy (e.g., `DuplicateKeyException`, `DataIntegrityViolationException`)."*

### Q9: What is `TransactionTemplate` and when do you use it over `@Transactional`?
- **ELI5 Answer:** *"`@Transactional` wraps your entire room in bubble wrap. `TransactionTemplate` lets you wrap just one tiny fragile glass inside the room."*
- **Technical Answer:** *"`TransactionTemplate` provides programmatic transaction management via `execute(status -> { ... })`. It is preferred when you need fine-grained control over transaction boundaries (e.g. executing slow non-DB tasks before or after the transaction without holding a database connection)."*

### Q10: Why should you avoid holding a DB connection during external HTTP calls?
- **ELI5 Answer:** *"Sitting at a table in a crowded restaurant while waiting for a phone call from someone in another country. Other hungry customers cannot sit down because you are hogging the chair!"*
- **Technical Answer:** *"Database connection pools have limited size (e.g. 20 connections). If a thread holds a DB connection inside a `@Transactional` block while making an external REST API call that takes 3 seconds, concurrent traffic will exhaust the entire pool in moments, leading to catastrophic system-wide connection timeouts."*

---

# TRACK 2: ADVANCED ARCHITECTURE & HIGH-PERFORMANCE JDBC

## ⚙️ 1. Core APIs: JdbcTemplate vs NamedParameterJdbcTemplate

### Maven Configuration (`pom.xml`)
```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-jdbc</artifactId>
    </dependency>
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>
</dependencies>
```

### Why NamedParameterJdbcTemplate is Industry Best Practice
Standard `JdbcTemplate` uses positional placeholders (`?`), which are brittle, prone to off-by-one indexing errors, and unreadable when queries have 15+ parameters. `NamedParameterJdbcTemplate` binds values by parameter name (`:orderId`).

```java
package com.example.sql.repository;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public class OrderJdbcRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public OrderJdbcRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int updateOrderStatus(String orderId, String status, BigDecimal minAmount) {
        String sql = """
            UPDATE orders
            SET status = :status, updated_at = NOW()
            WHERE id = :orderId AND total_amount >= :minAmount
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("status", status)
            .addValue("orderId", orderId)
            .addValue("minAmount", minAmount);

        return jdbcTemplate.update(sql, params);
    }
}
```

---

## 🗺️ 2. Result Mapping: RowMapper, ResultSetExtractor & RowCallbackHandler

Choosing the wrong mapping strategy can cause catastrophic JVM Out-Of-Memory (OOM) errors when queries return 100,000+ records.

| Mapper Type | Return Type | Memory Characteristics | When to Use |
| :--- | :--- | :--- | :--- |
| **`RowMapper<T>`** | `List<T>` | Accumulates entire result set in memory | Small-to-medium lists ($< 5,000$ rows) |
| **`ResultSetExtractor<T>`** | `T` (Arbitrary single object or complex map) | Fully custom aggregation over entire cursor | Constructing Parent-Child hierarchical trees (e.g. Order + OrderItems) |
| **`RowCallbackHandler`** | `void` (Streaming) | $O(1)$ memory, streams row by row | Exporting 100,000+ rows to CSV, JSON, or Kafka without loading into heap |

### 2.1 Clean Modern RowMapper (Java Record)
```java
public record CustomerSummary(String id, String name, String email, BigDecimal balance) {}

public List<CustomerSummary> findTopCustomers(BigDecimal threshold) {
    String sql = "SELECT id, name, email, balance FROM customers WHERE balance >= :threshold ORDER BY balance DESC";
    
    return jdbcTemplate.query(
        sql,
        new MapSqlParameterSource("threshold", threshold),
        (rs, rowNum) -> new CustomerSummary(
            rs.getString("id"),
            rs.getString("name"),
            rs.getString("email"),
            rs.getBigDecimal("balance")
        )
    );
}
```

### 2.2 ResultSetExtractor: Aggregating 1-to-Many Relationships in a Single Query
Avoids running $N$ secondary queries to fetch child items.

```java
public record OrderWithItems(String orderId, String customerId, List<String> itemSkus) {}

public OrderWithItems getOrderWithItems(String orderId) {
    String sql = """
        SELECT o.id, o.customer_id, i.sku
        FROM orders o
        LEFT JOIN order_items i ON o.id = i.order_id
        WHERE o.id = :orderId
        """;

    return jdbcTemplate.query(sql, new MapSqlParameterSource("orderId", orderId), rs -> {
        String ordId = null;
        String custId = null;
        List<String> skus = new java.util.ArrayList<>();

        while (rs.next()) {
            if (ordId == null) {
                ordId = rs.getString("id");
                custId = rs.getString("customer_id");
            }
            String sku = rs.getString("sku");
            if (sku != null) {
                skus.add(sku);
            }
        }
        return (ordId != null) ? new OrderWithItems(ordId, custId, skus) : null;
    });
}
```

### 2.3 Streaming 1,000,000 Rows Without OOM via RowCallbackHandler
```java
public void streamLargeExportToCsv(java.io.Writer csvWriter) {
    String sql = "SELECT id, email, signup_date FROM users ORDER BY id ASC";

    // Crucial: Fetch size must be set to prevent PostgreSQL driver from buffering all rows
    jdbcTemplate.getJdbcTemplate().setFetchSize(1000);

    jdbcTemplate.getJdbcTemplate().query(sql, rs -> {
        // Invoked once per row as the cursor streams from the network socket
        try {
            csvWriter.write(String.format("%s,%s,%s\n",
                rs.getString("id"),
                rs.getString("email"),
                rs.getString("signup_date")
            ));
        } catch (java.io.IOException e) {
            throw new RuntimeException("CSV stream error", e);
        }
    });
}
```

---

## ⚡ 3. Ultra-Fast Batch Inserts & Bulk Updates

Executing 10,000 individual `INSERT` statements requires 10,000 network round trips. JDBC Batching groups them into single TCP packets.

```java
package com.example.sql.batch;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public class BulkAuditRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public BulkAuditRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public record AuditEntry(String id, String eventType, String payload, long timestamp) {}

    @Transactional
    public int[] batchInsertAudits(List<AuditEntry> entries) {
        String sql = """
            INSERT INTO audit_logs (id, event_type, payload, created_at)
            VALUES (:id, :eventType, :payload, TO_TIMESTAMP(:timestamp / 1000.0))
            """;

        // Converts collection of POJOs/Records into batch parameter array
        var batchParams = SqlParameterSourceUtils.createBatch(entries);

        // Executes single batch update
        return jdbcTemplate.batchUpdate(sql, batchParams);
    }
}
```

---

## 💧 4. HikariCP Connection Pool Architecture & Performance Tuning

HikariCP is the default, ultra-fast connection pool in Spring Boot. Improper sizing is the #1 cause of production latency spikes.

```
Incoming Threads ──> [ Connection Pool: 10 Max Connections ] ──> PostgreSQL Database
 (e.g. 200 req/s)               │
                                ▼
                       (Thread waits up to connectionTimeout=3000ms)
                       If exceeded: SQLTransientConnectionException
```

### HikariCP Pool Sizing Golden Formula
According to PostgreSQL and HikariCP benchmarks:
$$\text{Pool Size} = (\text{CPU Cores} \times 2) + \text{Effective Spindle / SSD Count}$$

> [!TIP]
> Setting pool size to 100 on an 8-core database server **degrades** throughput due to CPU context switching and disk I/O thrashing. A pool size of **16 to 25** typically handles thousands of requests per second.

### Production `application.yml` Tuning
```yaml
spring:
  datasource:
    url: jdbc:postgresql://db-primary.internal:5432/finance_db?reWriteBatchedInserts=true
    username: ${DB_USER}
    password: ${DB_PASS}
    hikari:
      pool-name: EnterpriseHikariPool
      maximum-pool-size: 20
      minimum-idle: 10
      idle-timeout: 300000        # 5 minutes
      max-lifetime: 1800000       # 30 minutes (must be shorter than database server wait_timeout)
      connection-timeout: 3000    # 3 seconds (fail fast if pool is exhausted)
      leak-detection-threshold: 2000 # Warn if a connection is checked out longer than 2 seconds!
```

> [!IMPORTANT]
> For PostgreSQL, always append `?reWriteBatchedInserts=true` to the JDBC URL! Without this parameter, the driver breaks multi-row batch inserts into individual statements behind the scenes.

---

## 🛡️ 5. Programmatic vs Declarative Transactions (TransactionTemplate)

While `@Transactional` is convenient, it can lead to accidental long-running transactions if external REST calls or slow disk operations are performed inside the method. `TransactionTemplate` provides fine-grained boundaries.

```java
package com.example.sql.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class AccountTransferService {

    private final TransactionTemplate transactionTemplate;
    private final NotificationClient notificationClient;
    private final LedgerRepository ledgerRepository;

    public AccountTransferService(TransactionTemplate transactionTemplate,
                                  NotificationClient notificationClient,
                                  LedgerRepository ledgerRepository) {
        this.transactionTemplate = transactionTemplate;
        this.notificationClient = notificationClient;
        this.ledgerRepository = ledgerRepository;
    }

    public void transferFunds(String fromAcc, String toAcc, long amountCents) {
        // 1. Critical DB Operations kept in tight, atomic transaction
        transactionTemplate.execute(status -> {
            ledgerRepository.debit(fromAcc, amountCents);
            ledgerRepository.credit(toAcc, amountCents);
            return true;
        });

        // 2. Slow external network call executed OUTSIDE transaction boundary
        // Database connection was already released back to the pool!
        notificationClient.sendSmsAlert(fromAcc, "Funds transferred successfully");
    }
}
```

---

## 🏭 6. Production Scenarios & War Room Incident Forensics

### Scenario 1: Connection Leak Detection Alert Triggered
- **Symptom in Logs:**
  `WARN com.zaxxer.hikari.pool.ProxyLeakTask: Connection leak detection triggered for connection org.postgresql.jdbc.PgConnection@1a2b3c, stack trace follows...`
- **Root Cause:** A developer opened a raw JDBC connection via `dataSource.getConnection()` and did not use try-with-resources, or an unhandled exception bypassed `connection.close()`.
- **The Fix:** Rely on `JdbcTemplate` or `TransactionTemplate` which guarantees automatic connection cleanup in a `finally` block.

### Scenario 2: Dynamic Search Filter with Optional SQL Parameters
- **Challenge:** A user can filter orders by status, minAmount, startDate, or any combination. Concatenating strings directly causes SQL injection risks.
- **The Clean Solution:** Dynamic `MapSqlParameterSource` with conditional SQL fragments:

```java
public List<OrderSummary> searchOrders(String status, BigDecimal minAmount, String country) {
    StringBuilder sql = new StringBuilder("SELECT id, status, total_amount, country FROM orders WHERE 1=1");
    MapSqlParameterSource params = new MapSqlParameterSource();

    if (status != null && !status.isBlank()) {
        sql.append(" AND status = :status");
        params.addValue("status", status);
    }
    if (minAmount != null) {
        sql.append(" AND total_amount >= :minAmount");
        params.addValue("minAmount", minAmount);
    }
    if (country != null && !country.isBlank()) {
        sql.append(" AND country = :country");
        params.addValue("country", country);
    }

    return jdbcTemplate.query(sql.toString(), params, (rs, i) -> new OrderSummary(
        rs.getString("id"),
        rs.getString("status"),
        rs.getBigDecimal("total_amount"),
        rs.getString("country")
    ));
}
```

---

## ⚖️ 7. Spring SQL Master Cheat Sheet

| Operation | Syntax Example |
| :--- | :--- |
| **Execute Single Row Query** | `jdbcTemplate.queryForObject(sql, params, (rs, rowNum) -> rs.getString("name"))` |
| **Execute Single Scalar** | `jdbcTemplate.queryForObject("SELECT count(*) FROM users", Map.of(), Long.class)` |
| **Execute Multi-Row Query** | `jdbcTemplate.query(sql, params, (rs, rowNum) -> new Record(...))` |
| **Single Update / Insert** | `jdbcTemplate.update(sql, params)` |
| **Batch Update** | `jdbcTemplate.batchUpdate(sql, SqlParameterSourceUtils.createBatch(list))` |
| **Generate Primary Key** | `SimpleJdbcInsert.withTableName("users").usingGeneratedKeyColumns("id")` |
| **Set Cursor Fetch Size** | `jdbcTemplate.getJdbcTemplate().setFetchSize(1000)` |
| **Programmatic Tx** | `transactionTemplate.execute(status -> { ... return val; })` |

---
[🏠 Back to Home](README.md)
