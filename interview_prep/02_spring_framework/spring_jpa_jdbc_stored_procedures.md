# Spring Data JPA & Java JDBC: Stored Procedures & Database Persistence Engineering

> **Curriculum Milestone**: Module 02 - Spring Framework & Persistence Architecture (Advanced Guide S21 / SP-GAP-16)  
> **Topic Coverage**: Low-Level JDBC `CallableStatement`, Spring JDBC `SimpleJdbcCall`, JPA `@Procedure`, `@NamedStoredProcedureQuery`, PostgreSQL Functions vs Procedures, Oracle `REF CURSOR`, SQL Server Multiple Result Sets, `JpaTransactionManager` Connection Sharing, Hibernate L1/L2 Cache Desynchronization, Dirty Checking Auto-Flush Invariants, HikariCP Statement Caching, and Cursor Leak Prevention.  
> **Target Audience**: Staff Software Engineers, Distributed Systems Architects, Lead Database Engineers.  
> **Standard Compliance**: Antigravity 8-Track Progressive Architecture ($\ge 2,000$ lines, fully runnable code, real-world war rooms, zero placeholders).

---

## Track 0: Architectural Foundations & Enterprise Jargon Demystification

| Term | Low-Level Architectural Definition | Wire / Memory / Protocol Consequence | Enterprise Failure Mode If Misunderstood |
|---|---|---|---|
| **`CallableStatement`** | The JDBC sub-interface (`java.sql.CallableStatement`) extending `PreparedStatement` used to execute SQL stored procedures via DB-specific escape syntax `{call ...}` or `{? = call ...}`. | Driver allocates server-side execution context, prepares input/output descriptor blocks, and maps OUT parameters to driver-allocated memory buffers. | Leaking statement handles if not closed; failing to unregister output parameters leading to `SQLException: Parameter index out of range`. |
| **`SimpleJdbcCall`** | Spring's multi-threaded, metadata-driven abstraction over `CallableStatement` that introspects database metadata via `DatabaseMetaData` to automate parameter binding and row mapping. | Issues `DatabaseMetaData.getProcedureColumns()` during initialization; caches metadata in memory to eliminate repeated catalog queries. | Massive connection hang on startup if DB user lacks dictionary permissions (e.g., Oracle `ALL_ARGUMENTS` or PostgreSQL `pg_proc`). |
| **`@NamedStoredProcedureQuery`** | Jakarta Persistence annotation declaring a stored procedure mapping at the entity level, binding procedure names, parameters (`@StoredProcedureParameter`), and result classes. | Hibernate parses annotations at bootstrap into `ProcedureCallMemento` instances within the `SessionFactory` SQM metamodel. | Schema mismatch failures during deployment if DBA alters procedure parameter types or ordering without updating entity metadata. |
| **`REF CURSOR` / `refcursor`** | A database pointer/handle representing a cursor created on the database engine that can be passed back to JDBC client code as a streaming `ResultSet`. | Driver reads a cursor pointer (e.g., Oracle 32-bit/64-bit cursor ID, PG string cursor name) and opens a server-side active cursor stream over TCP. | **Cursor Exhaustion Outage**: Failing to close the `ResultSet` leaves cursors open on DB server, throwing `ORA-01000: maximum open cursors exceeded`. |
| **PostgreSQL 11+ Procedure vs Function** | PostgreSQL historical distinction: Functions (`CREATE FUNCTION`) run inside the caller's transaction and return values; Procedures (`CREATE PROCEDURE`) can manage internal transactions (`COMMIT`/`ROLLBACK`). | Procedures are invoked via `CALL proc_name(?)`, whereas functions are invoked via `SELECT func_name(?)`. JDBC requires explicit driver parameter configuration. | Driver throws `PSQLException: A result was returned when none was expected` or transaction abort when invoking procedures with incorrect JDBC syntax. |
| **Hibernate Dirty Check Auto-Flush** | Hibernate's default behavior (`FlushModeType.AUTO`) where pending entity mutations in the `ActionQueue` are flushed to the DB before executing any native query or stored procedure. | Emits SQL `UPDATE`/`INSERT`/`DELETE` packets across physical socket before issuing the procedure `CALL` RPC packet to guarantee read consistency. | Severe latency spikes when calling procedures in tight loops; circular locking if procedure modifies rows Hibernate just updated in the same transaction. |
| **L1 Persistence Context Desynchronization** | The state discrepancy occurring when a stored procedure modifies database rows directly, leaving Hibernate's First-Level (L1) Entity Cache unaware of the updates. | In-memory Java entity heap snapshots retain old database state, causing stale reads and accidental overwrite of procedure changes upon transaction commit. | Phantom data corruption: Hibernate re-writes stale entity values back to the database during transaction commit dirty checking. |
| **L2 Second-Level Cache Staleness** | Second-Level (L2) Cache (Ehcache, Hazelcast, Infinispan) retaining cached entity snapshots because stored procedure writes bypass Hibernate's interceptors. | Cache regions are not evicted automatically since DB engine executes writes out-of-band of the Hibernate `Session`. | Distributed stale reads across entire microservice fleet lasting until cache TTL expires. |
| **`JpaTransactionManager` Connection Sharing** | Spring's transaction manager coordinating JPA `EntityManager` and Spring `JdbcTemplate` to bind to the exact same physical JDBC `Connection` within a `@Transactional` boundary. | Uses `TransactionSynchronizationManager` resource map to bind `EntityManagerHolder` and `ConnectionHolder` to the current `ThreadLocal`. | If misconfigured (e.g., separate `DataSourceTransactionManager`), JPA and JDBC use different physical DB connections, causing uncommitted read deadlocks. |
| **HikariCP Statement Caching (`cachePrepStmts`)** | Client-side caching of prepared and callable statement handles inside the JDBC driver to eliminate server-side statement parse overhead. | Allocates LRU cache inside driver connection wrapper (e.g., `com.mysql.cj.jdbc.ServerPreparedStatement`), keyed by SQL string and parameter types. | Memory leak if procedures generate dynamic SQL names; driver retains statement handles across pooled connection re-use. |
| **INOUT Parameter** | A parameter passed into a stored procedure containing an initial value that is subsequently overwritten by the procedure and returned to the caller. | Driver binds value into input stream and registers an output slot in the wire packet simultaneously (`registerOutParameter`). | ClassCastException or null pointer if caller attempts to read parameter without explicitly registering both IN binding and OUT SQL type. |
| **Multiple Result Sets** | A stored procedure returning two or more distinct tabular query outputs in a single network round-trip execution (common in Microsoft SQL Server and Sybase). | Driver receives multiple tabular data streams separated by TDS/wire token boundaries; navigated via `Statement.getMoreResults()`. | Connection lockup or truncated data pipelines when application code reads only the first `ResultSet` and ignores remaining streams. |
| **`AutoCommit=false` PG Wire Invariant** | PostgreSQL wire protocol requirement that `refcursor` retrieval functions MUST execute inside an active transaction block (`BEGIN ... COMMIT`). | PostgreSQL automatically closes open cursors at statement boundary if `autocommit` is `true`, invalidating cursor handle before JDBC can fetch rows. | `PSQLException: ERROR: cursor "<unnamed portal 1>" does not exist` immediately upon attempting to read the `ResultSet`. |
| **StatelessSession** | A lightweight Hibernate session interface that bypasses the 1st-level cache, dirty checking, cascades, and interceptors for high-throughput batch operations. | Zero entity hydration snapshot allocation; issues raw direct SQL/procedure commands without heap overhead. | Missing automated cascade operations and optimistic locking checks; developers must manually manage foreign keys and version numbers. |
| **`SimpleJdbcCall.declareParameters()`** | Explicit Java declaration of procedure parameters bypassing automatic database catalog introspection. | Suppresses expensive metadata queries to database system tables on connection initialization. | Drastic startup performance degradation if omitted in high-latency cloud database topologies (e.g., AWS Aurora cross-region). |
| **Oracle `Types.REF_CURSOR` vs `OracleTypes.CURSOR`** | Standard JDBC 4.2 `java.sql.Types.REF_CURSOR` (introduced in Java 8) versus legacy proprietary `oracle.jdbc.OracleTypes.CURSOR` (-10). | In modern drivers, both resolve to the same underlying wire registration, but JDBC standard portability avoids vendor classpath coupling. | `NoSuchFieldError` or compilation breakage when migrating legacy Oracle JDBC drivers (`ojdbc6` to `ojdbc11`). |
| **Optimistic Lock Bypass Hazard** | Direct row updates inside stored procedures that do not increment the JPA `@Version` column (`version = version + 1`). | Database row changes without modifying version counter; Hibernate remains unaware of the concurrency update. | Lost updates: Subsequent concurrent JPA transactions overwrite stored procedure modifications without throwing `OptimisticLockException`. |
| **`SqlParameterSource`** | Spring JDBC abstraction (`MapSqlParameterSource`, `BeanPropertySqlParameterSource`) for supplying named parameters to stored calls. | Encapsulates parameter names, Java values, and SQL type hints (`java.sql.Types`) into an indexed binding map. | Silent type truncation (e.g., passing `BigDecimal` as `VARCHAR`) causing severe database-side implicit type conversion and table scans. |
| **`executeBatch()` on CallableStatement** | Grouping multiple stored procedure execution invocations into a single batch network transmission via `cs.addBatch()`. | Queues multiple parameter sets in client socket buffer; sends single TCP payload to database engine. | Unsupported on certain database engines (e.g., procedures returning `OUT` parameters or cursors cannot be batched; throws `BatchUpdateException`). |
| **`DataAccessException` Hierarchy** | Spring's unchecked exception hierarchy translating vendor-specific `SQLException` error codes into unified semantic exceptions. | Spring inspects vendor SQLState and error code via `SQLErrorCodeSQLExceptionTranslator` to map exceptions like `CannotAcquireLockException`. | Catching raw `SQLException` in service layer instead of Spring's `DataAccessException`, breaking architectural decoupling. |

---

## Track 1: Foundational Deep Dive (ELI5 to Principal Architect)

### 1.1 The Wire Protocol Reality: How DB Engines Actually Execute Stored Procedures
To understand stored procedures in Java and Spring, one must look past the high-level annotations and understand the physical network connection between the JVM and the Database Management System (RDBMS).

```
+-----------------------------------------------------------------------------------------+
|                                    JVM HEAP MEMORY                                      |
|                                                                                         |
|  [Spring Service] --> [@Transactional] --> [EntityManager / SessionImpl]                |
|           |                                       | (FlushModeType.AUTO)                |
|           |                                       v                                     |
|           |                           +-----------------------+                         |
|           |                           |  ActionQueue (Flush)  |                         |
|           |                           +-----------------------+                         |
|           v                                       |                                     |
|  [SimpleJdbcCall / CallableStatement]             v (Shared Physical Connection)        |
|           |                           +-----------------------+                         |
|           +-------------------------> |   HikariProxyConnection|                         |
|                                       +-----------------------+                         |
|                                                   |                                     |
|                                                   v                                     |
|                                       [Vendor JDBC Driver]                              |
|                                   (PG / Oracle / MySQL / MSSQL)                         |
+---------------------------------------------------|-------------------------------------+
                                                    |
                                      TCP/IP Socket Wire Protocol
                       (Oracle TNS / PostgreSQL PGwire / MS TDS / MySQL Packet)
                                                    |
                                                    v
+-----------------------------------------------------------------------------------------+
|                                  DATABASE SERVER ENGINE                                 |
|                                                                                         |
|  [Network Listener] --> [Dispatcher / Worker Process] --> [SQL / PL-SQL Engine]        |
|                                                                   |                     |
|                                                      +------------v------------+        |
|                                                      | Stored Procedure Scope  |        |
|                                                      |  - Internal Transaction |        |
|                                                      |  - Cursor Allocations   |        |
|                                                      |  - Buffer Cache Access  |        |
|                                                      +-------------------------+        |
+-----------------------------------------------------------------------------------------+
```

When an application executes a standard `PreparedStatement` (`SELECT * FROM users WHERE id = ?`), the JDBC driver sends an SQL string and parameter list across the wire. The database engine's parser, planner, and optimizer evaluate the query plan.

However, when executing a `CallableStatement`:
1. **Remote Procedure Call (RPC) Packets**:
   - In Oracle, the driver transmits a specialized TNS packet (`TNS_TYPE_DATA` with function code `RPC_CALL`). The Oracle database bypasses standard SQL string parsing and directly executes the compiled P-code in the Shared Pool.
   - In PostgreSQL, prior to version 11, stored procedures did not exist—only functions existed (`CREATE FUNCTION`), invoked via `SELECT * FROM func()`. In PostgreSQL 11+, `CREATE PROCEDURE` was introduced and invoked via `CALL proc()`. Under the hood, the PostgreSQL wire protocol uses Extended Query mode (`Parse`, `Bind`, `Describe`, `Execute`, `Sync`).
   - In Microsoft SQL Server, JDBC sends a TDS (Tabular Data Stream) packet containing an `RPC` (Remote Procedure Call) request token (packet type `0x03`).
2. **Memory Allocation for Output Buffers**:
   - For `PreparedStatement`, all output arrives as tabular row streams (`ResultSet`).
   - For `CallableStatement`, the client JVM must pre-allocate descriptors for **`OUT` parameters**. When you call `cs.registerOutParameter(2, Types.VARCHAR)`, the JDBC driver tells the server socket layer: *"Slot 2 is an output buffer. Allocate a return slot in the response packet."* When the database completes the procedure, it places the return values directly into the RPC response packet header before sending tabular rows.

---

### 1.2 The Architectural Friction: JPA Identity Map vs Stored Procedures
The fundamental architectural friction in modern enterprise engineering occurs when combining **Hibernate/Spring Data JPA** with **Stored Procedures**:

```
+---------------------------------------------------------------------------------------+
|                               PERSISTENCE CONTEXT INVARIANT                           |
|                                                                                       |
|   1. Entity Hydration:   DB Row (id=1, balance=100) -> Java Object (balance=100)      |
|   2. Loaded Snapshot:    [Identity Map Snapshot: id=1, balance=100]                   |
|                                                                                       |
|   3. Out-of-Band Call:   CALL apply_interest_proc(id=1, rate=0.05)                    |
|                          --> Database updates row directly: balance becomes 105       |
|                                                                                       |
|   4. The Catastrophe:    Hibernate Persistence Context is UNCONSCIOUS of the update!  |
|                          Java Object in Heap still has: balance = 100                 |
|                          Snapshot still has: balance = 100                            |
|                                                                                       |
|   5. Flush Collision:    If Java code mutates another field: user.setEmail("new@a.com")|
|                          Hibernate dirty checker runs:                                |
|                          It compares Heap Object vs Snapshot:                         |
|                          Emits: UPDATE users SET email='new@a.com', balance=100       |
|                          --> DATABASE OVERWRITTEN! The $105 balance is DESTROYED!     |
+---------------------------------------------------------------------------------------+
```

To safely execute stored procedures in a JPA application, an engineer must master:
1. **Flush Timing (`FlushModeType.AUTO`)**: Ensuring pending JPA entity inserts/updates are pushed to the database *before* the procedure runs, so the procedure sees the latest data.
2. **Cache Eviction (`em.clear()` / `em.refresh()`)**: Forcing Hibernate to drop its stale in-memory entity snapshots after the stored procedure executes.
3. **Transaction Coordination**: Guaranteeing that the JPA `EntityManager` and the JDBC `CallableStatement` operate on the **same physical database connection** within the same ACID transaction.

---

## Track 2: Core Engineering Blueprints & Production Implementations

### Blueprint 1: Spring Data JPA `@NamedStoredProcedureQuery` & Repository Integration
This blueprint demonstrates enterprise stored procedure mapping in Spring Data JPA using Jakarta Persistence annotations, supporting input, output, and entity result mapping.

```java
package com.enterprise.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "accounts")
@NamedStoredProcedureQueries({
    @NamedStoredProcedureQuery(
        name = "Account.processTransfer",
        procedureName = "sp_process_account_transfer",
        parameters = {
            @StoredProcedureParameter(mode = ParameterMode.IN, name = "p_sender_id", type = Long.class),
            @StoredProcedureParameter(mode = ParameterMode.IN, name = "p_receiver_id", type = Long.class),
            @StoredProcedureParameter(mode = ParameterMode.IN, name = "p_amount", type = BigDecimal.class),
            @StoredProcedureParameter(mode = ParameterMode.OUT, name = "p_transaction_ref", type = String.class),
            @StoredProcedureParameter(mode = ParameterMode.OUT, name = "p_sender_new_balance", type = BigDecimal.class),
            @StoredProcedureParameter(mode = ParameterMode.OUT, name = "p_status_code", type = Integer.class)
        }
    ),
    @NamedStoredProcedureQuery(
        name = "Account.getAuditHistory",
        procedureName = "sp_get_account_audit_history",
        resultClasses = { AccountAuditRecord.class },
        parameters = {
            @StoredProcedureParameter(mode = ParameterMode.IN, name = "p_account_id", type = Long.class),
            @StoredProcedureParameter(mode = ParameterMode.IN, name = "p_limit", type = Integer.class)
        }
    )
})
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_number", nullable = false, unique = true, length = 32)
    private String accountNumber;

    @Column(name = "balance", nullable = false, precision = 19, scale = 4)
    private BigDecimal balance;

    @Version
    @Column(name = "version")
    private Long version;

    @Column(name = "updated_at")
    private Instant updatedAt;

    // Constructors, Getters, and Setters
    public Account() {}

    public Long getId() { return id; }
    public String getAccountNumber() { return accountNumber; }
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
    public Long getVersion() { return version; }
}
```

```java
package com.enterprise.persistence.repository;

import com.enterprise.persistence.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Map;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    /**
     * Executes sp_process_account_transfer mapped by @NamedStoredProcedureQuery name.
     * Spring Data JPA extracts OUT parameters into an unmodifiable Map.
     */
    @Procedure(name = "Account.processTransfer")
    Map<String, Object> executeTransferNamed(
        @Param("p_sender_id") Long senderId,
        @Param("p_receiver_id") Long receiverId,
        @Param("p_amount") BigDecimal amount
    );

    /**
     * Executes procedure directly by database procedure name.
     * Returns a single scalar OUT parameter when only one OUT parameter is declared.
     */
    @Procedure(procedureName = "sp_calculate_accrued_interest")
    BigDecimal calculateAccruedInterest(
        @Param("p_account_id") Long accountId,
        @Param("p_rate") BigDecimal rate
    );
}
```

---

### Blueprint 2: High-Performance Spring JDBC `SimpleJdbcCall` Architecture
When database stored procedures return complex cursor outputs or when dynamic parameters are required, Spring Data JPA's `@Procedure` is often too rigid. `SimpleJdbcCall` provides maximum performance and flexibility.

```java
package com.enterprise.persistence.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Map;

@Component
public class SettlementProcedureInvoker {

    private static final Logger log = LoggerFactory.getLogger(SettlementProcedureInvoker.class);

    private final DataSource dataSource;
    private SimpleJdbcCall settlementCall;
    private SimpleJdbcCall cursorReportingCall;

    @Autowired
    public SettlementProcedureInvoker(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PostConstruct
    public void init() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.setResultsMapCaseInsensitive(true);

        // Production Best Practice: Always call withoutProcedureColumnMetaDataAccess()
        // if database dictionary queries are slow, and explicitly declare parameters!
        this.settlementCall = new SimpleJdbcCall(jdbcTemplate)
            .withProcedureName("sp_execute_daily_settlement")
            .withoutProcedureColumnMetaDataAccess()
            .declareParameters(
                new SqlParameter("p_batch_id", Types.VARCHAR),
                new SqlParameter("p_settlement_date", Types.DATE),
                new SqlOutParameter("p_processed_count", Types.INTEGER),
                new SqlOutParameter("p_total_settled_amount", Types.DECIMAL),
                new SqlOutParameter("p_error_code", Types.INTEGER),
                new SqlOutParameter("p_error_message", Types.VARCHAR)
            );
        this.settlementCall.compile();

        // Stored procedure returning a REF CURSOR (Oracle / PostgreSQL)
        this.cursorReportingCall = new SimpleJdbcCall(jdbcTemplate)
            .withProcedureName("sp_generate_ledger_report")
            .withoutProcedureColumnMetaDataAccess()
            .declareParameters(
                new SqlParameter("p_ledger_date", Types.DATE),
                new SqlOutParameter("p_report_cursor", Types.REF_CURSOR, new LedgerRowMapper())
            );
        this.cursorReportingCall.compile();
    }

    public SettlementResult executeSettlement(String batchId, java.time.LocalDate settlementDate) {
        MapSqlParameterSource inParams = new MapSqlParameterSource()
            .addValue("p_batch_id", batchId)
            .addValue("p_settlement_date", java.sql.Date.valueOf(settlementDate));

        Map<String, Object> out = settlementCall.execute(inParams);

        int errCode = (Integer) out.getOrDefault("p_error_code", 0);
        if (errCode != 0) {
            String errMsg = (String) out.get("p_error_message");
            throw new IllegalStateException("Settlement SP failed [code=" + errCode + "]: " + errMsg);
        }

        return new SettlementResult(
            (Integer) out.get("p_processed_count"),
            (BigDecimal) out.get("p_total_settled_amount")
        );
    }

    @SuppressWarnings("unchecked")
    public List<LedgerItem> fetchLedgerReport(java.time.LocalDate reportDate) {
        MapSqlParameterSource inParams = new MapSqlParameterSource()
            .addValue("p_ledger_date", java.sql.Date.valueOf(reportDate));

        Map<String, Object> out = cursorReportingCall.execute(inParams);
        return (List<LedgerItem>) out.get("p_report_cursor");
    }

    private static class LedgerRowMapper implements RowMapper<LedgerItem> {
        @Override
        public LedgerItem mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new LedgerItem(
                rs.getLong("entry_id"),
                rs.getString("account_number"),
                rs.getBigDecimal("amount"),
                rs.getString("currency"),
                rs.getTimestamp("created_at").toInstant()
            );
        }
    }

    public record SettlementResult(int processedCount, BigDecimal totalAmount) {}
    public record LedgerItem(Long entryId, String accountNumber, BigDecimal amount, String currency, java.time.Instant createdAt) {}
}
```

---

### Blueprint 3: Low-Level JDBC `CallableStatement` with Oracle `REF CURSOR` Streaming
This blueprint provides rock-solid, production-grade raw JDBC execution using `CallableStatement`, handling input parameters, primitive output parameters, and streaming Oracle `REF CURSOR` results without running out of open cursors.

```java
package com.enterprise.persistence.rawjdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class OracleCursorJdbcRepository {

    private static final Logger log = LoggerFactory.getLogger(OracleCursorJdbcRepository.class);
    private final DataSource dataSource;

    public OracleCursorJdbcRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Executes Oracle PL/SQL stored procedure:
     * PROCEDURE sp_get_portfolio_positions(
     *     p_portfolio_id   IN  NUMBER,
     *     p_min_valuation  IN  NUMBER,
     *     p_total_assets   OUT NUMBER,
     *     p_cursor         OUT SYS_REFCURSOR
     * );
     */
    public PortfolioReport queryPortfolioPositions(Long portfolioId, BigDecimal minValuation) {
        final String sql = "{call sp_get_portfolio_positions(?, ?, ?, ?)}";
        List<PositionRecord> positions = new ArrayList<>();
        BigDecimal totalAssets;

        // Try-with-resources guarantees Connection and CallableStatement are physically closed,
        // preventing connection pool exhaustion and database cursor leaks.
        try (Connection conn = dataSource.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {

            // Step 1: Bind IN parameters
            cs.setLong(1, portfolioId);
            cs.setBigDecimal(2, minValuation);

            // Step 2: Register OUT parameters
            cs.registerOutParameter(3, Types.DECIMAL);

            // Step 3: Register REF CURSOR (Standard JDBC 4.2 Types.REF_CURSOR)
            // Note: For legacy ojdbc, oracle.jdbc.OracleTypes.CURSOR (-10) was required.
            cs.registerOutParameter(4, Types.REF_CURSOR);

            // Step 4: Execute procedure
            cs.execute();

            // Step 5: Read scalar OUT parameter
            totalAssets = cs.getBigDecimal(3);

            // Step 6: Extract REF CURSOR into ResultSet inside a nested try-with-resources!
            // CRITICAL: The ResultSet must be explicitly closed to free the server-side cursor!
            try (ResultSet rs = (ResultSet) cs.getObject(4)) {
                if (rs != null) {
                    // Set fetch size to prevent streaming one row per network round-trip
                    rs.setFetchSize(500);

                    while (rs.next()) {
                        PositionRecord record = new PositionRecord(
                            rs.getLong("position_id"),
                            rs.getString("ticker"),
                            rs.getLong("quantity"),
                            rs.getBigDecimal("market_value")
                        );
                        positions.add(record);
                    }
                }
            }

            return new PortfolioReport(portfolioId, totalAssets, positions);

        } catch (SQLException ex) {
            log.error("Failed executing sp_get_portfolio_positions for portfolioId={}: SQLState={}, ErrorCode={}",
                portfolioId, ex.getSQLState(), ex.getErrorCode(), ex);
            throw new RuntimeException("Database stored procedure execution failed: " + ex.getMessage(), ex);
        }
    }

    public record PortfolioReport(Long portfolioId, BigDecimal totalAssets, List<PositionRecord> positions) {}
    public record PositionRecord(Long positionId, String ticker, Long quantity, BigDecimal marketValue) {}
}
```

---

### Blueprint 4: PostgreSQL 11+ Procedure vs Function Execution Mechanics
PostgreSQL has strict wire protocol and transaction rules regarding functions and procedures. This blueprint illustrates calling PostgreSQL functions returning `refcursor` vs PostgreSQL procedures with transactions.

```java
package com.enterprise.persistence.rawjdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class PostgresStoredProcRepository {

    private static final Logger log = LoggerFactory.getLogger(PostgresStoredProcRepository.class);
    private final DataSource dataSource;

    public PostgresStoredProcRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * PostgreSQL Function returning a refcursor:
     * CREATE OR REPLACE FUNCTION fn_fetch_active_subscriptions(p_tenant_id INT)
     * RETURNS refcursor AS $$
     * DECLARE
     *     my_cursor refcursor := 'sub_cursor';
     * BEGIN
     *     OPEN my_cursor FOR SELECT id, plan_name, monthly_fee FROM subscriptions WHERE tenant_id = p_tenant_id;
     *     RETURN my_cursor;
     * END;
     * $$ LANGUAGE plpgsql;
     *
     * WIRE INVARIANT: AutoCommit MUST be FALSE! Otherwise, PG closes the cursor immediately at statement boundary!
     */
    public List<SubscriptionRecord> fetchSubscriptionsUsingCursor(int tenantId) {
        List<SubscriptionRecord> results = new ArrayList<>();
        final String callSql = "{? = call fn_fetch_active_subscriptions(?)}";

        try (Connection conn = dataSource.getConnection()) {
            // CRITICAL PG WIRE REQUIREMENT:
            boolean originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            try (CallableStatement cs = conn.prepareCall(callSql)) {
                // Register return type as OTHER or Types.REF_CURSOR
                cs.registerOutParameter(1, Types.REF_CURSOR);
                cs.setInt(2, tenantId);
                cs.execute();

                // In PostgreSQL, refcursor returns a cursor name. JDBC driver wraps it as a ResultSet.
                try (ResultSet rs = (ResultSet) cs.getObject(1)) {
                    if (rs != null) {
                        rs.setFetchSize(250);
                        while (rs.next()) {
                            results.add(new SubscriptionRecord(
                                rs.getLong("id"),
                                rs.getString("plan_name"),
                                rs.getBigDecimal("monthly_fee")
                            ));
                        }
                    }
                }

                // Commit to release cursor hold locks on DB server
                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(originalAutoCommit);
            }

            return results;
        } catch (SQLException e) {
            log.error("Postgres cursor retrieval failed: SQLState={}", e.getSQLState(), e);
            throw new RuntimeException("Postgres cursor query error", e);
        }
    }

    /**
     * PostgreSQL 11+ Procedure Execution:
     * CREATE OR REPLACE PROCEDURE sp_reconcile_tenant_invoices(IN p_tenant_id INT, INOUT p_reconciled_count INT)
     * LANGUAGE plpgsql AS $$
     * BEGIN
     *     UPDATE invoices SET status = 'RECONCILED' WHERE tenant_id = p_tenant_id AND status = 'PENDING';
     *     GET DIAGNOSTICS p_reconciled_count = ROW_COUNT;
     *     COMMIT; -- Procedures can commit internally in PostgreSQL!
     * END;
     * $$;
     */
    public int executePostgresProcedure(int tenantId) {
        final String procSql = "CALL sp_reconcile_tenant_invoices(?, ?)";

        try (Connection conn = dataSource.getConnection();
             CallableStatement cs = conn.prepareCall(procSql)) {

            cs.setInt(1, tenantId);
            cs.setInt(2, 0); // Initial INOUT value
            cs.registerOutParameter(2, Types.INTEGER); // Register OUT binding

            cs.execute();

            return cs.getInt(2);
        } catch (SQLException ex) {
            log.error("Postgres CALL procedure failed: SQLState={}", ex.getSQLState(), ex);
            throw new RuntimeException("Postgres procedure call error", ex);
        }
    }

    public record SubscriptionRecord(Long id, String planName, java.math.BigDecimal monthlyFee) {}
}
```

---

### Blueprint 5: Dual-Stack Synchronization Architecture (JPA + JDBC Shared Connection)
This blueprint demonstrates how an enterprise service orchestrates Hibernate Entity lifecycle mutations alongside low-level stored procedure calls without data corruption or connection leak.

```java
package com.enterprise.persistence.service;

import com.enterprise.persistence.entity.Account;
import com.enterprise.persistence.jdbc.SettlementProcedureInvoker;
import com.enterprise.persistence.repository.AccountRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class EnterpriseFinancialOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(EnterpriseFinancialOrchestrator.class);

    @PersistenceContext
    private EntityManager entityManager;

    private final AccountRepository accountRepository;
    private final SettlementProcedureInvoker procedureInvoker;

    public EnterpriseFinancialOrchestrator(
            AccountRepository accountRepository,
            SettlementProcedureInvoker procedureInvoker) {
        this.accountRepository = accountRepository;
        this.procedureInvoker = procedureInvoker;
    }

    /**
     * CRITICAL ENTERPRISE TRANSACTION BOUNDARY:
     * 1. JPA entity updates take place.
     * 2. Stored procedure modifies database tables directly.
     * 3. Cache synchronization is enforced to prevent Hibernate dirty-check overwrite disaster!
     */
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, timeout = 30)
    public void executeFinancialSettlementCycle(Long accountId, String batchId) {
        log.info("Initiating financial settlement cycle for accountId={}, batchId={}", accountId, batchId);

        // Step 1: Load managed JPA entity
        Account account = accountRepository.findById(accountId)
            .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountId));

        // Step 2: Mutate managed entity
        account.setBalance(account.getBalance().subtract(new BigDecimal("50.00")));
        log.info("Pre-procedure in-memory mutated balance: {}", account.getBalance());

        // Step 3: MUST EXPLICITLY FLUSH!
        // Hibernate's FlushModeType.AUTO *usually* flushes before native queries,
        // but explicit entityManager.flush() guarantees pending ActionQueue inserts/updates
        // are pushed to the physical DB socket before stored procedure runs!
        entityManager.flush();

        // Step 4: Execute Stored Procedure via JDBC (Runs on the SAME physical DB connection!)
        // The stored procedure updates the database 'accounts' table directly!
        SettlementProcedureInvoker.SettlementResult result = 
            procedureInvoker.executeSettlement(batchId, LocalDate.now());
        log.info("Stored procedure finished: processed={}, amount={}", result.processedCount(), result.totalAmount());

        // Step 5: EVACUATE OR REFRESH THE FIRST-LEVEL (L1) CACHE!
        // The database row has been updated by the stored procedure, but the in-memory
        // 'account' entity instance still has stale values.
        // If we don't refresh or clear, Hibernate will overwrite the DB changes on commit!
        entityManager.refresh(account);
        log.info("Post-procedure refreshed balance from DB: {}", account.getBalance());

        // Commit happens automatically at end of method; JPA synchronizes cleanly.
    }
}
```

---

## Track 3: Low-Level Mechanics, Driver Protocols & Bytecode Internals

### 3.1 The Physical Wire Protocol Exchange
When a Java application issues `cs.execute()`, the following wire frame exchange occurs depending on the RDBMS:

```
[JVM Client Application]                              [RDBMS Database Server]
         |                                                       |
         |----------------- 1. Socket Connect ------------------>| (TCP Handshake)
         |<---------------- 2. Connection Accepted --------------|
         |                                                       |
         |=== [HikariCP borrows physical connection from pool] ===|
         |                                                       |
         |----------------- 3. RPC Call Frame ------------------>| 
         |   Oracle: TNS Type 0x03 (RPC Descriptor)              |
         |   Postgres: Extended Query 'P' (Parse) + 'B' (Bind)   |
         |   MSSQL: TDS Packet Type 0x03 (RPC Request)           |
         |   Payload: Proc Name + IN param bytes + OUT buffers   |
         |                                                       |
         |                                                       | [DB executes PL/SQL]
         |                                                       | [Allocates Cursor]
         |                                                       | [Applies Row Locks]
         |                                                       |
         |<---------------- 4. Response Header ------------------|
         |   Return Codes, OUT param values, Status Tokens       |
         |                                                       |
         |<---------------- 5. Tabular Data Stream --------------|
         |   (If REF CURSOR is returned)                         |
         |   Rows streamed in chunks matching 'fetchSize'        |
         |                                                       |
         |----------------- 6. Fetch Next Chunks (if needed) --->|
         |<---------------- 7. End of Cursor Stream -------------|
         |                                                       |
         |----------------- 8. Close Statement / Cursor --------->| (Crucial step!)
         |   Frees server-side WorkArea / Cursor Slot            |
         v                                                       v
```

### 3.2 HikariCP and JDBC Statement Caching Mechanics
When `DataSource.getConnection()` is invoked inside Spring, HikariCP does not create a new TCP socket; it borrows a `PoolEntry` from its lock-free `ConcurrentBag`.

When your code executes `conn.prepareCall("{call my_proc(?, ?)}")`:
1. If statement caching is enabled in the JDBC driver (`cachePrepStmts=true` in MySQL, `oracle.jdbc.implicitStatementCacheSize=50` in Oracle, or `prepareThreshold=5` in PostgreSQL):
   - The driver checks its internal LRU hash map using the SQL string and parameter signature as the cache key.
   - If found, the driver reuses the existing client-side `CallableStatement` handle, avoiding round-trips to describe the procedure.
2. If statement caching is disabled:
   - The driver must re-allocate statement handles and potentially re-query database catalog tables (`ALL_ARGUMENTS`, `information_schema.parameters`) to verify argument types.

> [!WARNING]
> **Statement Cache Leaks**: If an application generates dynamic procedure names (e.g., `"{call " + tenantId + "_proc()}"`), the statement cache fills up with single-use entries. This triggers severe JVM tenured heap bloat and high garbage collection pause times. Always use static procedure names with parameter arguments.

---

### 3.3 Hibernate 6 SQM Architecture and Stored Procedure Execution
In Hibernate 6, stored procedures are managed via the `org.hibernate.query.spi.ProcedureCallImplementor` interface:

```
[Hibernate Session] 
       │
       ▼
[createNamedStoredProcedureQuery() / createStoredProcedureQuery()]
       │
       ▼
[StoredProcedureQueryImpl]
       │
       ├─► [ProcedureCallMemento] (Metamodel metadata)
       │
       └─► [ProcedureCallImpl]
                 │
                 ├─► Evaluates FlushMode: If AUTO, triggers session.autoFlushIfRequired()
                 │         │
                 │         └─► [ActionQueue.executeActions()] (Pending Entity DML flushed)
                 │
                 ├─► Obtains java.sql.Connection from JdbcCoordinator
                 │
                 ├─► Invokes Connection.prepareCall() via Dialect
                 │
                 ├─► Binds IN/OUT parameters through TypeMappings
                 │
                 ├─► Executes JDBC CallableStatement
                 │
                 └─► Extracts OUT values and constructs Entity/Tuple result sets
```

---

## Track 4: 50 Progressive Real-World & War-Room Interview Scenarios

### Tier 1: Core Fundamentals & Parameter Binding (Q1 – Q12)

#### Q1: Low-Level Differences Between Statement, PreparedStatement, and CallableStatement
##### 1. Exact Scenario & Question
A candidate claims that `CallableStatement` is simply syntactic sugar for `PreparedStatement` with curly braces `{call ...}`. Is this true at the driver and wire protocol level? Explain the differences in compilation, parameter registration, and network transport across all three interfaces.

##### 2. What the Interviewer Evaluates
- Understanding of the JDBC interface hierarchy (`Statement` $\rightarrow$ `PreparedStatement` $\rightarrow$ `CallableStatement`).
- Knowledge of server-side query planning vs RPC wire packets.
- Awareness of `OUT` and `INOUT` parameter descriptor allocations on client and server.

##### 3. Standout Technical Answer
`CallableStatement` is **not** mere syntactic sugar. While it inherits from `PreparedStatement`, it fundamentally alters driver behavior:
1. **Compilation & Execution Path**:
   - `Statement`: Sends raw SQL text over the wire every time. The database must parse, tokenize, plan, and optimize the query on every call.
   - `PreparedStatement`: Pre-compiles the SQL template on the server (or client driver) and associates it with a Statement ID. Successive executions only transmit the Statement ID and binary parameter values.
   - `CallableStatement`: Utilizes the database's proprietary **Remote Procedure Call (RPC)** protocol. In Oracle, Sybase, and MSSQL, the server skips the SQL parser entirely and dispatches directly to the procedural language execution runtime.
2. **Output Parameter Descriptors**:
   - Neither `Statement` nor `PreparedStatement` supports bidirectional parameter binding.
   - `CallableStatement` allocates output parameter memory structures via `registerOutParameter()`. The driver writes these expected return types into the RPC packet header.
3. **Execution Syntax Standard**:
   - JDBC standardizes two forms:
     - Procedure: `{call procedure_name(?, ?)}`
     - Function: `{? = call function_name(?)}` (where parameter index 1 is reserved for the return value).

---

#### Q2: The Mechanics of `registerOutParameter` and Data Type Mappings
##### 1. Exact Scenario & Question
Consider the following JDBC code snippet:
```java
CallableStatement cs = conn.prepareCall("{call get_customer_balance(?, ?)}");
cs.setLong(1, 1001L);
cs.execute();
BigDecimal balance = cs.getBigDecimal(2);
```
What exception will this code throw at runtime, why does it occur, and what is the exact JVM-to-Driver sequence required to fix it?

##### 2. What the Interviewer Evaluates
- Immediate recognition of the missing `registerOutParameter()`.
- Knowledge of how JDBC drivers validate output column indices before parsing network response frames.
- Precise error message familiarity (`SQLException: Parameter index out of range` or `Parameter 2 is not registered as an OUT parameter`).

##### 3. Standout Technical Answer
The code throws `java.sql.SQLException: Parameter 2 was not registered as an OUT parameter` (or vendor equivalent, such as Oracle's `ORA-01008: not all variables bound` or MySQL's `No output parameters registered`).

**Underlying Driver Mechanics:**
When `cs.execute()` is called, the JDBC driver parses its internal parameter registry. If an index is not registered via `cs.registerOutParameter(index, sqlType)`, the driver does not instruct the database server to reserve an output slot in the RPC return packet. When `cs.getBigDecimal(2)` is subsequently invoked, the driver checks its internal `outParamMap`; finding no registration, it immediately fails without even examining the network stream.

**The Production Fix:**
```java
CallableStatement cs = conn.prepareCall("{call get_customer_balance(?, ?)}");
// Step 1: Bind input parameter
cs.setLong(1, 1001L);

// Step 2: Explicitly register output parameter with java.sql.Types
cs.registerOutParameter(2, java.sql.Types.DECIMAL);

// Step 3: Execute RPC
cs.execute();

// Step 4: Retrieve value AFTER execution
BigDecimal balance = cs.getBigDecimal(2);
```

---

#### Q3: Handling INOUT Parameters Without Silent Data Corruption
##### 1. Exact Scenario & Question
How does an `INOUT` parameter work under the hood in JDBC? Write the exact code to pass an integer value of `50` into a stored procedure `sp_accumulate_points(INOUT points INT)` and read the updated value back. What happens if you register it as `OUT` but forget to call `setInt()`?

##### 2. What the Interviewer Evaluates
- Understanding that `INOUT` requires **both** `setX()` and `registerOutParameter()` on the identical parameter index.
- Wire consequences of omitting the input value (sending NULL vs protocol error).

##### 3. Standout Technical Answer
An `INOUT` parameter serves dual purposes: it delivers an initial state to the stored procedure runtime, and serves as an output slot where the procedure writes the final result before termination.

```java
try (CallableStatement cs = conn.prepareCall("{call sp_accumulate_points(?)}")) {
    // 1. MUST register as OUT parameter with SQL type
    cs.registerOutParameter(1, Types.INTEGER);
    
    // 2. MUST bind the initial input value
    cs.setInt(1, 50);
    
    // 3. Execute
    cs.execute();
    
    // 4. Retrieve updated value
    int updatedPoints = cs.getInt(1);
}
```
**Failure Mode Analysis**:
If `registerOutParameter(1, Types.INTEGER)` is called, but `cs.setInt(1, 50)` is omitted:
- The driver treats the input component as an unbound parameter. Depending on the JDBC driver, it either throws `SQLException: No value specified for parameter 1`, or transmits an SQL `NULL` over the wire. The stored procedure receives `NULL`, leading to `NULL + points = NULL` arithmetic errors and silent database corruption.

---

#### Q4: Spring Data JPA `@Procedure` Annotation Modes and Method Signatures
##### 1. Exact Scenario & Question
Explain the three different ways Spring Data JPA allows you to declare and invoke stored procedures on a `JpaRepository`. What are the limitations of `@Procedure` when dealing with multiple OUT parameters?

##### 2. What the Interviewer Evaluates
- Mastery of `procedureName`, `name` (referencing `@NamedStoredProcedureQuery`), and parameter aliases.
- Understanding of return type resolution in Spring Data JPA (`void`, scalar, `Map<String, Object>`).

##### 3. Standout Technical Answer
Spring Data JPA supports three invocation modes:
1. **Direct Name Mapping**:
   ```java
   @Procedure(procedureName = "sp_calc_tax")
   BigDecimal calculateTax(@Param("amount") BigDecimal amount);
   ```
   Directly maps to the database stored procedure name. Only works seamlessly when there is exactly **one** OUT parameter matching the method return type.
2. **Named Stored Procedure Query Reference**:
   ```java
   @Procedure(name = "Account.processTransfer")
   Map<String, Object> executeTransfer(@Param("sender") Long s, @Param("receiver") Long r);
   ```
   References a `@NamedStoredProcedureQuery` defined on an `@Entity`. Required when mapping complex parameter types or multi-output procedures.
3. **Positional Parameter Mapping**:
   ```java
   @Procedure("sp_log_event")
   void logEvent(String eventType, String payload);
   ```
   Relies on parameter declaration order. Fragile if stored procedure parameter order changes in database schema updates.

**Multiple OUT Parameter Limitation**:
If a procedure returns multiple `OUT` parameters, Spring Data JPA cannot map them to a custom Java POJO directly via `@Procedure`. It returns an unmodifiable `Map<String, Object>` where keys are parameter names. Attempting to return a custom DTO directly results in a `ConversionService` exception unless a custom repository implementation with an `EntityManager` or `SimpleJdbcCall` is utilized.

---

#### Q5: Spring JDBC `SimpleJdbcCall` Metadata Introspection Bottleneck
##### 1. Exact Scenario & Question
During a major production deployment to AWS RDS Oracle, your Spring Boot service startup time jumped from 45 seconds to 18 minutes. Heap dumps show thousands of threads blocked on JDBC connections during `@PostConstruct`. Profiling points to `SimpleJdbcCall` initialization. What is the root cause and how do you resolve it?

##### 2. What the Interviewer Evaluates
- Deep architectural understanding of `DatabaseMetaData.getProcedureColumns()`.
- Knowledge of Oracle data dictionary lock contention on `ALL_ARGUMENTS` / `SYS.ALL_PROCEDURES`.
- Ability to optimize `SimpleJdbcCall` via explicit declarations.

##### 3. Standout Technical Answer
**Root Cause**:
By default, when `SimpleJdbcCall` initializes, it calls `compile()`. If parameters have not been declared explicitly, Spring attempts automatic parameter discovery by invoking `connection.getMetaData().getProcedureColumns()`.
In enterprise databases (especially Oracle, PostgreSQL, and SQL Server):
- Querying data dictionary views like Oracle's `ALL_ARGUMENTS` requires scanning massive system tables.
- If the database has tens of thousands of schema objects, or if multiple Spring application pods start simultaneously and issue concurrent catalog queries, the database library cache and dictionary tables experience massive latch contention (`row cache lock` or `library cache: mutex X`), freezing application startup.

**Architectural Fix**:
Instruct `SimpleJdbcCall` to bypass metadata introspection completely by calling `.withoutProcedureColumnMetaDataAccess()` and explicitly declaring every parameter:

```java
// ❌ SLOW: Triggers expensive database metadata queries
SimpleJdbcCall call = new SimpleJdbcCall(dataSource)
    .withProcedureName("sp_process_payroll");

// ✅ FAST: Zero database round-trips on startup!
SimpleJdbcCall call = new SimpleJdbcCall(dataSource)
    .withProcedureName("sp_process_payroll")
    .withoutProcedureColumnMetaDataAccess() // BYPASS CATALOG QUERY!
    .declareParameters(
        new SqlParameter("p_emp_id", Types.BIGINT),
        new SqlParameter("p_salary", Types.DECIMAL),
        new SqlOutParameter("p_net_pay", Types.DECIMAL)
    );
call.compile();
```

---

#### Q6: Stored Functions vs Stored Procedures Calling Syntax
##### 1. Exact Scenario & Question
Why does calling a stored function via `{call my_func(?)}` fail on some JDBC drivers while working on others? What is the strict JDBC escape syntax for functions that return a value, and how does the driver wire format differ between them?

##### 2. What the Interviewer Evaluates
- Knowledge of the JDBC function call specification `{? = call func_name(?)}`.
- Driver-specific parsing differences between Oracle, MySQL, and PostgreSQL.

##### 3. Standout Technical Answer
In the SQL standard and JDBC specification:
- A **Stored Procedure** has no formal return value; it communicates via `OUT` parameters:
  `{call procedure_name(arg1, arg2)}`
- A **Stored Function** evaluates an expression and returns a primary scalar value as its functional return:
  `{? = call function_name(arg1, arg2)}`

**The Wire Difference**:
When using `{? = call ...}`, the first parameter index (`1`) represents the return value register. The driver assigns this to the function return register in the RPC packet.
If an engineer uses `{call my_func(?)}` for a function:
- In MySQL/MariaDB: The driver attempts to rewrite it into a `SELECT my_func(?)` query, which may work but incurs query rewriting overhead.
- In Oracle / PostgreSQL: The driver treats the first `?` as an input parameter to the function. It fails with `ORA-06550: PLS-00222: no function with name exists in this scope` or `PLS-00306: wrong number or types of arguments in call`.

---

#### Q7: Oracle `REF CURSOR` Retrieval via Standard JDBC 4.2
##### 1. Exact Scenario & Question
Historically, Java code had to cast to `oracle.jdbc.OracleCallableStatement` and use `oracle.jdbc.OracleTypes.CURSOR` (-10) to read a cursor. How is this achieved in modern JDBC 4.2+ without vendor classpath dependencies, and what is the exact type mapping?

##### 2. What the Interviewer Evaluates
- Knowledge of standard `java.sql.Types.REF_CURSOR` introduced in JDBC 4.2 (Java 8).
- Clean architectural decoupling from vendor-specific driver libraries.

##### 3. Standout Technical Answer
In JDBC 4.2 and later, Java standardized `java.sql.Types.REF_CURSOR`. Vendor type casting is completely obsolete.

```java
// Modern Standard Way (Zero vendor-specific imports!)
try (CallableStatement cs = conn.prepareCall("{call sp_fetch_customers(?, ?)}")) {
    cs.setString(1, "US_EAST");
    // Standard JDBC 4.2 Type
    cs.registerOutParameter(2, java.sql.Types.REF_CURSOR);
    cs.execute();
    
    // Extracted as standard java.sql.ResultSet
    try (ResultSet rs = cs.getObject(2, ResultSet.class)) {
        while (rs.next()) {
            // Process streaming rows
        }
    }
}
```
Under the hood, modern drivers (including Oracle `ojdbc8`/`ojdbc11`) map `Types.REF_CURSOR` directly to their internal wire cursor identifiers, keeping your application code 100% compliant with standard `java.sql` contracts.

---

#### Q8: PostgreSQL `refcursor` Mechanics and the Transaction Invariant
##### 1. Exact Scenario & Question
A junior developer writes a Spring JDBC call to fetch a PostgreSQL `refcursor` using a simple `JdbcTemplate`. In development with autocommit enabled, it crashes with:
`org.postgresql.util.PSQLException: ERROR: cursor "<unnamed portal 1>" does not exist`.
Why did this happen, and what is the precise wire invariant of PostgreSQL portals?

##### 2. What the Interviewer Evaluates
- Understanding of PostgreSQL's server-side named portals.
- The lifecycle of PostgreSQL cursors relative to transaction boundaries.
- Autocommit behavior in connection pooling.

##### 3. Standout Technical Answer
**The PostgreSQL Portal Mechanics**:
In PostgreSQL, a `refcursor` does not serialize rows directly into the initial RPC packet. Instead:
1. The function opens an in-memory server cursor named a **Portal** (e.g., `"<unnamed portal 1>"` or a custom string name).
2. The function returns the *name* of this portal as a string to the JDBC driver.
3. The JDBC driver receives the portal name and prepares to issue `FETCH ALL FROM "<portal_name>"` over the wire.

**The Failure Mode**:
If `conn.getAutoCommit() == true`:
- The execution of `{? = call my_func()}` completes a single standalone statement.
- Because autocommit is enabled, PostgreSQL **immediately commits and terminates the transaction** at the statement boundary.
- When the transaction ends, PostgreSQL's memory manager instantly destroys all server-side portals created within that transaction!
- When the JDBC driver attempts to fetch rows from the portal name, the portal is already gone.

**The Solution**:
The cursor *must* be opened and consumed within an active, non-autocommit transaction block:
```java
@Transactional // Guarantees autocommit = false and holds transaction open!
public List<Customer> getCustomers() {
    return jdbcTemplate.execute((Connection conn) -> {
        // Portal remains alive throughout this transactional block
        CallableStatement cs = conn.prepareCall("{? = call get_customers_cursor()}");
        cs.registerOutParameter(1, Types.REF_CURSOR);
        cs.execute();
        ResultSet rs = (ResultSet) cs.getObject(1);
        // Map rows...
        return results;
    });
}
```

---

#### Q9: Processing Multiple Result Sets from a Single Stored Procedure
##### 1. Exact Scenario & Question
A legacy SQL Server stored procedure returns three distinct result sets: Orders, Order Items, and Payment Audit logs. How do you traverse and extract all three result sets using raw JDBC without truncating the output?

##### 2. What the Interviewer Evaluates
- Knowledge of `cs.execute()`, `cs.getResultSet()`, and `cs.getMoreResults()`.
- Understanding of TDS wire stream iteration and update count handling.

##### 3. Standout Technical Answer
When a procedure returns multiple result sets, iterating through them requires proper handling of both result sets and update counts:

```java
try (CallableStatement cs = conn.prepareCall("{call sp_get_order_full_hierarchy(?)}")) {
    cs.setLong(1, orderId);
    
    boolean isResultSet = cs.execute();
    int resultSetIndex = 0;
    
    while (true) {
        if (isResultSet) {
            try (ResultSet rs = cs.getResultSet()) {
                resultSetIndex++;
                log.info("Processing Result Set #{}", resultSetIndex);
                while (rs.next()) {
                    // Process rows for set 1 (Orders), set 2 (Items), or set 3 (Audit)
                }
            }
        } else {
            int updateCount = cs.getUpdateCount();
            if (updateCount == -1) {
                // No more results and no more update counts: exit loop!
                break;
            }
            log.info("Processed intermediate update count: {}", updateCount);
        }
        
        // Move to the next result stream
        isResultSet = cs.getMoreResults();
    }
}
```
**Wire Traversal Rule**:
`cs.getMoreResults()` implicitly closes any current `ResultSet` obtained via `cs.getResultSet()`. If update counts exist (e.g., intermediate `UPDATE` statements executed inside the procedure without `SET NOCOUNT ON`), `getMoreResults()` returns `false`, but `getUpdateCount() != -1`. The loop must check both conditions.

---

#### Q10: SQL Server `SET NOCOUNT ON` Performance and Protocol Impact
##### 1. Exact Scenario & Question
Why do Senior Database Architects mandate that every Microsoft SQL Server stored procedure must begin with `SET NOCOUNT ON` when called from Java/Spring applications? What bug occurs in JDBC if this is omitted?

##### 2. What the Interviewer Evaluates
- Understanding of TDS `DONE_IN_PROC` tokens.
- Impact on `CallableStatement.execute()` and `cs.getUpdateCount()`.
- Unnecessary network chatter and premature `ResultSet` closure bugs.

##### 3. Standout Technical Answer
By default, SQL Server sends a `DONE_IN_PROC` message across the wire for every single DML statement (`INSERT`, `UPDATE`, `DELETE`, `SELECT`) executed inside a stored procedure.

**Impact on Java JDBC**:
1. **Network Overhead**: For a procedure executing 20 DML statements, 20 separate `DONE_IN_PROC` TDS packets are transmitted across the wire, inflating network traffic.
2. **Result Set Disruption**: When Java executes `cs.execute()`, the JDBC driver interprets the first `DONE_IN_PROC` update count token as the result. If your Java code expects a `ResultSet` via `cs.executeQuery()` or checks `cs.getResultSet()`, the driver returns `null` or throws:
   `com.microsoft.sqlserver.jdbc.SQLServerException: The statement did not return a result set.`
3. **The Fix**: `SET NOCOUNT ON` suppresses the generation of `DONE_IN_PROC` tokens for internal statements, transmitting only the final completion token and explicit query result sets.

---

#### Q11: Parameter Name Matching vs Ordinal Position Binding
##### 1. Exact Scenario & Question
What are the trade-offs between binding stored procedure parameters by **Name** (`cs.setString("p_user_id", "123")`) versus by **Ordinal Position** (`cs.setString(1, "123")`)? Why do some high-performance systems prohibit binding by name?

##### 2. What the Interviewer Evaluates
- Low-level driver metadata lookup overhead for named parameters.
- Robustness against database parameter reordering vs performance.

##### 3. Standout Technical Answer
| Dimension | Positional Binding (`cs.setX(1, ...)`) | Named Binding (`cs.setX("p_name", ...)`) |
|---|---|---|
| **Performance** | **Sub-microsecond**. Direct array index lookup in the driver parameter buffer. Zero DB round-trips. | **High Overhead**. Driver must resolve parameter names to positional indices. |
| **Catalog Queries** | None. Driver writes directly to packet parameter slots. | In drivers like Oracle and PostgreSQL, named binding can trigger database metadata catalog queries to map names to indices. |
| **Refactoring Safety** | **Fragile**. If DBA shifts parameter positions (`p_user_id` from position 1 to 2), Java code silently corrupts data or crashes. | **Resilient**. Parameter ordering in procedure declaration can change without breaking Java callers. |
| **Architectural Mandate** | Preferred in ultra-high-throughput payment/trading engines where microsecond latency matters. | Preferred in enterprise CRUD systems where database schema evolution happens frequently. |

---

#### Q12: Exception Translation: `SQLException` vs Spring's `DataAccessException`
##### 1. Exact Scenario & Question
When a stored procedure raises a user-defined application error (e.g., Oracle `RAISE_APPLICATION_ERROR(-20001, 'Insufficient Funds')` or PostgreSQL `RAISE EXCEPTION`), what does raw JDBC throw, what does Spring throw, and how does Spring determine the translation?

##### 2. What the Interviewer Evaluates
- Understanding of vendor error codes vs ANSI SQL-92 `SQLState`.
- Spring's `SQLErrorCodeSQLExceptionTranslator` and `sql-error-codes.xml` mechanics.
- Decoupling architecture in enterprise layers.

##### 3. Standout Technical Answer
1. **Raw JDBC Behavior**:
   Raw JDBC throws a checked `java.sql.SQLException`.
   - `ex.getErrorCode()` returns the vendor-specific integer code (`20001` in Oracle, `50000` in MSSQL).
   - `ex.getSQLState()` returns the 5-character ANSI SQLState string (e.g., `'45000'` for user-defined unhandled exception).
2. **Spring Framework Translation**:
   Spring catches `SQLException` and passes it to `SQLErrorCodesSQLExceptionTranslator`.
   - Spring inspects the database vendor from `DatabaseMetaData.getDatabaseProductName()`.
   - It consults its internal error code mappings (originally loaded from `org/springframework/jdbc/support/sql-error-codes.xml`).
   - If mapped, it translates the error into a specific unchecked subclass of `DataAccessException` (e.g., `DuplicateKeyException`, `CannotAcquireLockException`).
   - If the code is unmapped (such as a custom business error `-20001`), Spring falls back to `UncategorizedSQLException`.
3. **Best Practice**:
   Production systems configure custom `CustomSQLErrorCodesTranslation` beans to map domain-specific procedure error codes directly to custom domain exceptions.

---

### Tier 2: Transaction Synchronization, L1/L2 Cache Invalidation & Dirty Flushing (Q13 – Q25)

#### Q13: The Shared Connection Invariant: `JpaTransactionManager` vs `DataSourceTransactionManager`
##### 1. Exact Scenario & Question
An enterprise application defines both JPA repositories and Spring JDBC `SimpleJdbcCall` invokers. A developer configures a `DataSourceTransactionManager` bean alongside `LocalContainerEntityManagerFactoryBean`. At runtime, an entity update is executed, followed by a stored procedure call. The stored procedure cannot see the entity updates and deadlocks! Why?

##### 2. What the Interviewer Evaluates
- Deep knowledge of Spring's `TransactionSynchronizationManager`.
- Difference between JPA connection proxying and DataSource connection binding.
- ThreadLocal resource holder keys (`EntityManagerHolder` vs `ConnectionHolder`).

##### 3. Standout Technical Answer
**The Root Cause**:
Spring binds transactional resources to the executing thread using `ThreadLocal` storage inside `TransactionSynchronizationManager`:
- `DataSourceTransactionManager` binds a raw JDBC `ConnectionHolder` keyed by the `DataSource`.
- `JpaTransactionManager` binds an `EntityManagerHolder` keyed by the `EntityManagerFactory`.

When `JpaTransactionManager` is used as the primary transaction manager:
1. It opens an `EntityManager` and borrows a physical connection from HikariCP.
2. It registers **both** the `EntityManagerHolder` AND exposes the underlying physical connection as a `ConnectionHolder` in `TransactionSynchronizationManager`.
3. When `JdbcTemplate` or `SimpleJdbcCall` executes, `DataSourceUtils.getConnection(dataSource)` inspects `TransactionSynchronizationManager`, discovers the active connection holder, and **reuses the exact same physical DB connection**!

**The Failure Mode**:
If the developer mistakenly configured a standalone `DataSourceTransactionManager` (or separate transaction managers for JPA and JDBC):
- JPA borrows Connection A from HikariCP.
- `SimpleJdbcCall` borrows Connection B from HikariCP.
- Because Connection A has uncommitted row locks on the modified entity, when Connection B runs the stored procedure and attempts to read or update those same rows, **the two threads from the same application deadlock each other on the database engine**!

**Architectural Fix**:
Always use a single `JpaTransactionManager` across the entire application context. It natively supports both JPA and raw JDBC on the same physical connection.

---

#### Q14: Dirty Checking and `FlushModeType.AUTO` Wire Behavior
##### 1. Exact Scenario & Question
In a `@Transactional` Spring service method:
```java
Order order = orderRepo.findById(orderId).get();
order.setStatus("PROCESSING");
procedureInvoker.executeStoredProcedure(orderId);
```
Will the database stored procedure see `status = 'PROCESSING'` or the old status? What triggers the flush, and what happens if `FlushModeType.COMMIT` is set?

##### 2. What the Interviewer Evaluates
- Understanding of Hibernate's `FlushModeType.AUTO`.
- ActionQueue synchronization before native/JDBC executions.
- Pitfalls of `FlushModeType.COMMIT`.

##### 3. Standout Technical Answer
**Under Default `FlushModeType.AUTO`**:
1. When `order.setStatus("PROCESSING")` is called, only the in-memory Java heap entity is modified.
2. When `procedureInvoker.executeStoredProcedure(orderId)` is called:
   - If the stored procedure is executed via **JPA** (`em.createNamedStoredProcedureQuery()` or `@Procedure`), Hibernate's query engine intercepts the call.
   - Hibernate checks its `ActionQueue`. Finding uncommitted entity mutations, it automatically triggers `session.flush()`, transmitting the `UPDATE orders SET status = 'PROCESSING'` SQL over the physical socket *before* sending the procedure `CALL`.
   - The stored procedure **sees the updated status**.
   - **CRITICAL EXCEPTION**: If the procedure is executed via an unmanaged raw JDBC `Connection` or a separate `JdbcTemplate` that bypasses the JPA `EntityManager`, Hibernate **does NOT know** a database call is happening! The flush does NOT occur, and the stored procedure reads **stale data**!

**Under `FlushModeType.COMMIT`**:
Hibernate strictly postpones all SQL emissions until the transaction commits. The procedure call will guaranteed read **stale data**, even if invoked through JPA.

**The Golden Engineering Rule**:
Always call `entityManager.flush()` explicitly before delegating to stored procedures if there are pre-existing entity mutations in the persistence context!

---

#### Q15: Post-Procedure First-Level (L1) Cache Invalidation
##### 1. Exact Scenario & Question
A stored procedure `sp_apply_discount` updates the `total_amount` column of order #500 from $1,000 to $800 directly in the database.
Immediately after calling the procedure in the same `@Transactional` method, the developer executes:
```java
Order order = orderRepo.findById(500L).get();
System.out.println(order.getTotalAmount());
```
The console prints `$1,000` instead of `$800`! Why did this happen, and what are the two distinct ways to solve it?

##### 2. What the Interviewer Evaluates
- Deep understanding of Hibernate's First-Level (L1) Cache (Persistence Context Identity Map).
- Why `findById` does not query the database if the entity ID is already hydrated in the L1 cache.
- Proper usage of `em.refresh()` vs `em.clear()`.

##### 3. Standout Technical Answer
**The Mechanism**:
When `findById(500L)` is executed, Hibernate first checks its internal **First-Level (L1) Cache** (the `PersistenceContext` identity map).
Because order #500 was loaded prior to (or during) the transaction, its hydrated Java instance already exists in memory. Hibernate returns the cached reference immediately **without issuing any SQL `SELECT` to the database**.
The stored procedure modified the database row on disk, but bypassed the Hibernate engine. Hence, the Java heap object remains stale.

**The Catastrophic Secondary Effect**:
When the transaction ends, Hibernate runs its dirty checking mechanism. It compares the in-memory `$1,000` balance against its initial loaded snapshot. If any other field on `Order` was touched, Hibernate may emit an update that **overwrites the stored procedure's $800 update back to $1,000**!

**The Solutions**:
- **Solution 1: Targeted Refresh**:
  ```java
  // Reloads the entity state directly from the database, updating the L1 snapshot
  entityManager.refresh(order);
  ```
- **Solution 2: Full Persistence Context Clearance**:
  ```java
  // Discards all L1 managed instances, forcing all subsequent queries to hit the DB
  entityManager.clear();
  Order refreshedOrder = orderRepo.findById(500L).get();
  ```

---

#### Q16: Optimistic Locking (`@Version`) Corruption Hazards
##### 1. Exact Scenario & Question
An entity uses `@Version private Long version;`. A stored procedure updates 50 rows in that table directly via bulk SQL `UPDATE accounts SET status = 'DORMANT' WHERE last_login < ...`. What happens to the `@Version` column, and how does this compromise optimistic concurrency control?

##### 2. What the Interviewer Evaluates
- Awareness that stored procedures do not automatically increment JPA `@Version` counters.
- Concurrency race conditions resulting from version desynchronization.
- Techniques for manual version incrementing inside SQL procedures.

##### 3. Standout Technical Answer
**The Hazard**:
JPA's optimistic locking relies on the application incrementing the version column on every update:
`UPDATE accounts SET status = ?, version = version + 1 WHERE id = ? AND version = ?`.

When a stored procedure updates rows directly:
1. Unless the stored procedure explicitly executes `version = version + 1`, the database rows are updated while their `version` remains unchanged.
2. If another concurrent thread has loaded an `Account` entity prior to the procedure execution:
   - Thread B modifies the entity in memory.
   - Thread B commits.
   - Thread B's update succeeds because the database `version` still matches Thread B's stale version!
   - **Result**: Thread B completely overwrites the changes made by the stored procedure without throwing an `OptimisticLockException`!

**Architectural Prevention**:
1. **Mandate in SQL Procedure**:
   The stored procedure *must* explicitly increment the version column:
   ```sql
   UPDATE accounts 
   SET status = 'DORMANT', 
       version = version + 1,
       updated_at = CURRENT_TIMESTAMP
   WHERE last_login < p_cutoff_date;
   ```
2. **Post-Procedure L2 Cache Invalidation**:
   Evict affected entity regions from the Second-Level Cache so concurrent nodes do not serve pre-procedure versions.

---

#### Q17: Second-Level (L2) Cache Desynchronization and Eviction
##### 1. Exact Scenario & Question
Your application uses Hibernate Second-Level Cache backed by Redis / Hazelcast. A stored procedure executes nightly to update currency exchange rates. The following morning, some application servers show updated rates while others show old rates until they are restarted. Explain why, and write the Spring cache eviction code.

##### 2. What the Interviewer Evaluates
- Understanding of Hibernate L2 cache region keys and entity state storage.
- Why out-of-band database mutations bypass L2 cache invalidation listeners.
- Programmatic cache eviction via `jakarta.persistence.Cache`.

##### 3. Standout Technical Answer
**Why It Occurs**:
Hibernate's L2 cache sits between the Session and the database. When an entity is updated via JPA (`em.merge()` or dirty checking), Hibernate emits an invalidation event to the L2 cache cluster.
A stored procedure runs directly on the database engine. **It does not emit Hibernate L2 cache invalidation events**.
Application nodes that had already cached the entities in their local near-cache or L2 cache continue serving stale entity data until the cache TTL expires.

**The Programmatic Eviction Fix**:
```java
@Service
public class CurrencyMaintenanceService {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private SimpleJdbcCall updateExchangeRatesCall;

    @Transactional
    public void executeNightlyRateUpdate() {
        // 1. Execute database stored procedure
        updateExchangeRatesCall.execute();

        // 2. Obtain JPA Cache abstraction
        jakarta.persistence.Cache cache = entityManager.getEntityManagerFactory().getCache();

        // 3. Evict specific entity class or entire region across the cluster!
        cache.evict(CurrencyRate.class);
        // Or evict specific ID: cache.evict(CurrencyRate.class, "USD");
        // Or evict everything: cache.evictAll();
    }
}
```

---

#### Q18: Deadlock Between JPA ActionQueue and Stored Procedure Locks
##### 1. Exact Scenario & Question
Thread 1 loads Entity A, modifies it, and then invokes Stored Procedure X. Stored Procedure X updates Entity B and then updates Entity A.
Concurrently, Thread 2 loads Entity B, modifies it, and updates Entity A via standard JPA.
Both threads hang and the database terminates one with a Deadlock Error (`ORA-00060` or `Postgres Deadlock detected`). Map the exact sequence of lock acquisitions that caused this deadlock.

##### 2. What the Interviewer Evaluates
- Knowledge of lock acquisition order in mixed JPA/Stored Procedure workflows.
- Understanding of row-level exclusive locks (`X-locks`) held by uncommitted transactions.
- Strategies for eliminating cross-resource locking inversions.

##### 3. Standout Technical Answer
**The Lock Acquisition Sequence**:

| Timeline | Thread 1 (JPA + Stored Procedure X) | Thread 2 (Pure JPA) |
|---|---|---|
| $T_1$ | `accountA.setBalance(...)` $\rightarrow$ triggers `flush()` $\rightarrow$ Emits `UPDATE account WHERE id = A`. **Acquires Exclusive Lock on Row A**. | `accountB.setBalance(...)` $\rightarrow$ triggers `flush()` $\rightarrow$ Emits `UPDATE account WHERE id = B`. **Acquires Exclusive Lock on Row B**. |
| $T_2$ | Calls `Stored Procedure X`. Procedure executes: `UPDATE account SET ... WHERE id = B`. **Blocks waiting for Lock on Row B (Held by Thread 2)**! | Thread 2 executes: `accountA.setBalance(...)` $\rightarrow$ Emits `UPDATE account WHERE id = A`. **Blocks waiting for Lock on Row A (Held by Thread 1)**! |
| $T_3$ | **DEADLOCK!** Thread 1 holds A, waits for B. Thread 2 holds B, waits for A. Database deadlock detector kills one transaction. |

**The Solution**:
Enforce strict **Resource Ordering**:
In distributed and mixed persistence systems, all transactions must acquire row locks in the exact same ordinal sequence (e.g., always lock Row with `id = min(A, B)` before `id = max(A, B)`). If stored procedures perform internal updates, they must adhere to the same lock ordering rules as application-level JPA queries.

---

#### Q19: Using `StatelessSession` for High-Throughput Stored Procedure Batches
##### 1. Exact Scenario & Question
You are importing 5 million records by calling a stored procedure `sp_insert_staging_record` in a loop. When using standard Spring Data JPA, the JVM crashes with `java.lang.OutOfMemoryError: Java heap space` after 200,000 records. Why does this happen, and how does Hibernate's `StatelessSession` solve it?

##### 2. What the Interviewer Evaluates
- Recognition of Persistence Context memory bloat during batch operations.
- Understanding of `StatelessSession` mechanics (no L1 cache, no dirty checking).
- High-performance batch patterns.

##### 3. Standout Technical Answer
**The Root Cause**:
Standard Hibernate `Session` maintains an internal **First-Level (L1) Cache** (`PersistenceContext`) containing references to every entity and query result generated within the transaction.
Even if calling stored procedures, Hibernate accumulates execution plans, parameter bindings, and transient object descriptors in the `SessionImpl` internal structures. Over 200,000 iterations, the JVM tenured generation fills up, triggering GC thrashing and eventual `OutOfMemoryError`.

**The Solution: `StatelessSession`**:
Hibernate provides `StatelessSession`, a command-oriented abstraction that operates without a First-Level Cache and without dirty checking:

```java
@Service
public class StagingBatchImporter {

    @Autowired
    private EntityManagerFactory emf;

    public void importMillions(List<StagingRecord> records) {
        SessionFactory sessionFactory = emf.unwrap(SessionFactory.class);
        
        try (StatelessSession session = sessionFactory.openStatelessSession()) {
            Transaction tx = session.beginTransaction();
            
            for (int i = 0; i < records.size(); i++) {
                StagingRecord record = records.get(i);
                
                // Directly prepares and executes CallableStatement without heap caching
                NativeQuery<?> query = session.createNativeQuery("CALL sp_insert_staging(?, ?)")
                    .setParameter(1, record.key())
                    .setParameter(2, record.payload());
                query.executeUpdate();
                
                if (i % 10_000 == 0) {
                    tx.commit();
                    tx = session.beginTransaction();
                }
            }
            tx.commit();
        }
    }
}
```

---

#### Q20: Stored Procedure Execution with Read-Only Transactions (`@Transactional(readOnly = true)`)
##### 1. Exact Scenario & Question
What happens when you execute a stored procedure that performs an `UPDATE` statement inside a Spring method annotated with `@Transactional(readOnly = true)`? Does Spring prevent the write, does the database prevent it, or does it succeed?

##### 2. What the Interviewer Evaluates
- Knowledge of what `readOnly = true` actually controls in Spring vs JDBC vs Database engine.
- Difference between Hibernate flush optimization and JDBC connection `setReadOnly(true)`.

##### 3. Standout Technical Answer
The behavior depends on the database driver and configuration:
1. **Spring & Hibernate Behavior**:
   - `readOnly = true` sets Hibernate's FlushMode to `MANUAL`. Hibernate will not automatically flush dirty managed entities.
   - However, if you invoke a stored procedure via `CallableStatement` or `SimpleJdbcCall`, Hibernate's flush mode does not block raw JDBC SQL execution!
2. **JDBC Driver & Database Engine Behavior**:
   - Spring calls `connection.setReadOnly(true)` on the physical JDBC connection.
   - In **PostgreSQL**: The driver issues `SET TRANSACTION READ ONLY`. If the stored procedure attempts an `UPDATE` or `INSERT`, the PostgreSQL database engine aborts the transaction with:
     `PSQLException: ERROR: cannot execute UPDATE in a read-only transaction`.
   - In **Oracle**: `connection.setReadOnly(true)` is ignored by default unless explicitly configured via driver connection properties. The stored procedure's `UPDATE` **succeeds**, violating the developer's architectural intent!
   - In **MySQL**: `setReadOnly(true)` issues `SET autocommit=0; SET TRANSACTION READ ONLY;`. The MySQL server will reject updates if supported by the storage engine.

**Engineering Takeaway**:
Never rely solely on `@Transactional(readOnly = true)` to enforce security or prevent mutations in stored procedures. Stored procedures intended for read-only reporting should be explicitly granted only `SELECT` permissions at the database role level.

---

#### Q21: Transaction Timeout Propagation to `CallableStatement`
##### 1. Exact Scenario & Question
You annotate a service method with `@Transactional(timeout = 5)`. The method invokes a stored procedure via `CallableStatement`. The stored procedure runs for 30 seconds on the database without being terminated. Why didn't Spring's transaction timeout cancel the procedure after 5 seconds?

##### 2. What the Interviewer Evaluates
- Understanding of how Spring passes timeouts to JDBC statements (`Statement.setQueryTimeout()`).
- Pitfalls of using unmanaged raw JDBC `Connection` vs Spring-managed `JdbcTemplate`.

##### 3. Standout Technical Answer
**The Mechanics**:
Spring calculates the remaining transaction timeout and applies it to statements via `Statement.setQueryTimeout(int seconds)`.
When the query runs, the JDBC driver schedules a timer. If the database does not respond within the allocated seconds, the driver transmits an interrupt packet (e.g., Oracle `Break/Reset` packet or MySQL `KILL QUERY`) to cancel execution.

**Why It Failed in the Scenario**:
1. **Unmanaged Connection**: If the developer obtained the connection via `dataSource.getConnection()` directly rather than through Spring's `DataSourceUtils.getConnection(dataSource)`, Spring has no opportunity to wrap the statement or set the timeout!
2. **Missing `setQueryTimeout` on Raw `CallableStatement`**:
   If raw JDBC is used, the developer *must* manually compute and set the timeout:
   ```java
   int remainingTimeout = TransactionSynchronizationManager.isSynchronizationActive() ?
       (int) (TransactionAspectSupport.currentTransactionStatus().getTimeToRollback()) : 0;
   cs.setQueryTimeout(remainingTimeout);
   ```
3. **Database Driver Ignoring Timeouts**:
   Certain database drivers do not interrupt stored procedures that are executing non-SQL procedural code (e.g., PL/SQL loops or network calls via `UTL_HTTP`) until the procedure returns to an SQL statement boundary.

---

#### Q22: Propagation Behavior: `REQUIRES_NEW` Inside Stored Procedures
##### 1. Exact Scenario & Question
Can an autonomous transaction inside a stored procedure (e.g., Oracle `PRAGMA AUTONOMOUS_TRANSACTION`) interact with Spring's `Propagation.REQUIRES_NEW`? What are the locking hazards if an Oracle autonomous procedure touches rows modified by the caller?

##### 2. What the Interviewer Evaluates
- Understanding of database-level autonomous transactions.
- Mutual exclusive lock contention between parent transaction and autonomous child.

##### 3. Standout Technical Answer
In Oracle PL/SQL, `PRAGMA AUTONOMOUS_TRANSACTION` allows a stored procedure to execute its own independent transaction, committing or rolling back without affecting the parent calling transaction.

**The Catastrophic Deadlock Hazard**:
If Spring's main transaction modifies Row X (holding an exclusive lock), and then calls a stored procedure configured with `PRAGMA AUTONOMOUS_TRANSACTION` that also attempts to update or lock Row X:
1. The autonomous procedure runs as a separate database session on the server.
2. It requests an exclusive lock on Row X.
3. Because the parent Spring transaction has uncommitted changes on Row X, the database server suspends the autonomous procedure session, waiting for Row X to be unlocked.
4. But the parent Spring transaction cannot commit until the stored procedure returns!
5. **Self-Deadlock (Hang)**: The connection freezes indefinitely until the database driver socket read timeout fires!

**Architectural Rule**:
An autonomous stored procedure must NEVER access, update, or lock any data rows that have been mutated within the uncommitted parent transaction.

---

#### Q23: Stored Procedure Batching via `CallableStatement.addBatch()`
##### 1. Exact Scenario & Question
Can you use `cs.addBatch()` with stored procedures? What are the limitations regarding `OUT` parameters, and how does HikariCP's `rewriteBatchedStatements` setting affect execution?

##### 2. What the Interviewer Evaluates
- Knowledge of JDBC batch execution rules for `CallableStatement`.
- Why `OUT` parameters are prohibited in batch operations.
- Driver rewriting capabilities.

##### 3. Standout Technical Answer
1. **The Strict JDBC Rule**:
   You **can** batch stored procedures using `cs.addBatch()` and `cs.executeBatch()`, **provided the stored procedure has ONLY `IN` parameters**.
   If a stored procedure declares any `OUT` or `INOUT` parameters, attempting to call `cs.addBatch()` or `cs.executeBatch()` throws:
   `java.sql.BatchUpdateException` or `SQLException: Batching of stored procedures with OUT parameters is not supported`.
   This is because the driver cannot map an array of return values into a single execution response header without knowing how many batches succeeded.
2. **`rewriteBatchedStatements` (MySQL)**:
   MySQL's `rewriteBatchedStatements=true` works by concatenating multiple `INSERT` statements into a single multi-row insert string. It **does not** rewrite `CALL` statements. Multiple `CALL` statements are simply pipelined over the socket.

---

#### Q24: Handling SQL Warnings from Stored Procedures (`SQLWarning`)
##### 1. Exact Scenario & Question
A Sybase / SQL Server stored procedure issues a warning message (e.g., `PRINT 'Reindex complete'` or division by zero warning that didn't abort). How does a Java application inspect these messages without treating them as catastrophic exceptions?

##### 2. What the Interviewer Evaluates
- Knowledge of `cs.getWarnings()` and the `SQLWarning` linked chain.
- Spring's `JdbcTemplate.setIgnoreWarnings(false)` configuration.

##### 3. Standout Technical Answer
In JDBC, non-fatal database messages, informational notices, and non-terminating alerts are returned as `SQLWarning` objects rather than thrown as `SQLException`:

```java
try (CallableStatement cs = conn.prepareCall("{call sp_reindex_tables()}")) {
    cs.execute();
    
    // Check for linked list of warnings
    SQLWarning warning = cs.getWarnings();
    while (warning != null) {
        log.warn("DB Warning: [State={}, Code={}] {}", 
            warning.getSQLState(), warning.getErrorCode(), warning.getMessage());
        warning = warning.getNextWarning(); // Traverse linked list!
    }
}
```
In Spring `JdbcTemplate`, warnings are ignored by default for performance. To capture them, configure `jdbcTemplate.setIgnoreWarnings(false)` or attach an `SQLWarningException` translator.

---

#### Q25: Stored Procedure Return Codes vs Output Parameters
##### 1. Exact Scenario & Question
In Microsoft SQL Server and Sybase, procedures support both a **Return Status** (`RETURN 0` or `RETURN -1`) and **Output Parameters**. How do you capture both simultaneously in JDBC?

##### 2. What the Interviewer Evaluates
- Proper JDBC syntax for capturing the return value register alongside output parameters.
- Parameter indexing conventions (`? = call proc(?, ?)`).

##### 3. Standout Technical Answer
To capture the procedure return code in SQL Server / Sybase, parameter index 1 must be registered as the return status using the function call escape syntax:

```java
// Index 1 = Return Code; Index 2 = IN Param; Index 3 = OUT Param
final String sql = "{? = call sp_process_payroll(?, ?)}";

try (CallableStatement cs = conn.prepareCall(sql)) {
    // 1. Register Return Code (Index 1)
    cs.registerOutParameter(1, Types.INTEGER);
    
    // 2. Bind IN parameter (Index 2)
    cs.setLong(2, 501L);
    
    // 3. Register OUT parameter (Index 3)
    cs.registerOutParameter(3, Types.DECIMAL);
    
    cs.execute();
    
    // 4. Retrieve values
    int returnCode = cs.getInt(1);
    BigDecimal netPay = cs.getBigDecimal(3);
    
    if (returnCode != 0) {
        throw new IllegalStateException("Procedure returned error code: " + returnCode);
    }
}
```

---

### Tier 3: Cursors, Result Sets, Streaming & Multi-Database Nuances (Q26 – Q38)

#### Q26: The Oracle `ORA-01000: maximum open cursors exceeded` Post-Mortem
##### 1. Exact Scenario & Question
After a high-traffic marketing event, an enterprise application starts throwing `java.sql.SQLException: ORA-01000: maximum open cursors exceeded`. A junior developer checks the DBA dashboard and sees 5,000 active cursors tied to a stored procedure returning a `REF CURSOR`. The developer insists: *"I closed the `CallableStatement` in a try-with-resources, so how could a cursor leak?!"* Diagnose the exact leak mechanism.

##### 2. What the Interviewer Evaluates
- Understanding that closing a `CallableStatement` does **not** automatically close the underlying `ResultSet` returned from a `REF CURSOR` in all driver implementations.
- Lifecycle of Oracle server-side private SQL work areas.
- Production cursor remediation.

##### 3. Standout Technical Answer
**The Diagnosis**:
In Oracle PL/SQL, when a procedure opens a cursor (`OPEN p_cursor FOR SELECT ...`), Oracle allocates a private cursor control block in the Server Process UGA/PGA.
When the JDBC driver reads the `REF CURSOR` via `cs.getObject(n)` or `(ResultSet) cs.getObject(n)`:
- A new `java.sql.ResultSet` wrapper is instantiated on the JVM heap.
- In Oracle's JDBC driver implementation, if the developer closes the `CallableStatement` without explicitly closing the child `ResultSet`, the server-side cursor **remains open in the Oracle session** until the physical database connection is closed!
- Because the physical connection belongs to a HikariCP pool and is recycled rather than closed, the open cursor count on that Oracle session increments with every single HTTP request until the `OPEN_CURSORS` threshold (e.g., 500 or 1,000) is breached. Once breached, the database refuses all subsequent queries from that session with `ORA-01000`.

**The Architectural Fix**:
Both the `CallableStatement` and the `ResultSet` extracted from the `REF CURSOR` must be enclosed in separate, nested `try-with-resources` blocks:

```java
// ✅ BULLETPROOF PRODUCTION CURSOR PATTERN
try (Connection conn = dataSource.getConnection();
     CallableStatement cs = conn.prepareCall("{call sp_fetch_data(?)}")) {
    
    cs.registerOutParameter(1, Types.REF_CURSOR);
    cs.execute();
    
    // Nested try-with-resources explicitly closes the ResultSet!
    try (ResultSet rs = (ResultSet) cs.getObject(1)) {
        if (rs != null) {
            while (rs.next()) {
                // Read rows
            }
        }
    } // rs.close() is called HERE, immediately freeing the Oracle server cursor!
} // cs.close() is called HERE
```

---

#### Q27: Streaming Millions of Rows via Stored Procedure Cursors (`setFetchSize`)
##### 1. Exact Scenario & Question
You are executing a stored procedure that returns a `REF CURSOR` containing 10 million audit rows. When you iterate through the `ResultSet`, the JVM crashes with `OutOfMemoryError`. Why did the memory blow up, and what is the exact configuration required on the `Statement`, `ResultSet`, and database driver to stream rows without heap exhaustion?

##### 2. What the Interviewer Evaluates
- Understanding of default JDBC fetch sizes (Oracle default = 10; MySQL default = entire table into memory!).
- Interaction between socket buffering and JVM memory.
- Forward-only cursor streaming configurations.

##### 3. Standout Technical Answer
**The Root Cause**:
Different JDBC drivers have radical defaults for cursor retrieval:
- **MySQL (`Connector/J`)**: By default, MySQL downloads the **entire result set** over the network into client JVM memory at once! For 10 million rows, this causes an instant OOM.
- **Oracle / PostgreSQL**: Default fetch size is often small (10 to 100), but if rows contain large CLOBs or wide VARCHAR columns, buffering can still overwhelm the heap if object references are retained.

**The Streaming Configuration**:
To stream millions of rows with constant $O(1)$ JVM heap usage:
1. **Set Result Set Type to Forward-Only**:
   `conn.prepareCall(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)`
2. **Tune Fetch Size**:
   Instruct the driver to fetch rows in bounded chunks across the socket:
   ```java
   cs.registerOutParameter(1, Types.REF_CURSOR);
   cs.execute();
   try (ResultSet rs = (ResultSet) cs.getObject(1)) {
       rs.setFetchSize(1000); // Pulls 1,000 rows per network round-trip!
       while (rs.next()) {
           processRow(rs); // Process and discard immediately!
       }
   }
   ```
3. **Special Case: MySQL**:
   In MySQL, you must set `cs.setFetchSize(Integer.MIN_VALUE)` to signal the driver to stream row-by-row directly from the network socket rather than buffering.

---

#### Q28: PostgreSQL Function vs Procedure: The Commit Mechanics
##### 1. Exact Scenario & Question
In PostgreSQL 11, the `CREATE PROCEDURE` syntax was introduced. How does a PostgreSQL Procedure differ from a PostgreSQL Function in terms of transaction control, and why does invoking `conn.commit()` inside a PostgreSQL function throw a fatal exception?

##### 2. What the Interviewer Evaluates
- Deep understanding of PostgreSQL transaction architecture.
- Why functions cannot control transactions (they execute inside the calling query's snapshot).
- Why procedures can execute `COMMIT` and `ROLLBACK` internally.

##### 3. Standout Technical Answer
In PostgreSQL:
1. **Functions (`CREATE FUNCTION`)**:
   - Always execute within the context of an outer SQL query or transaction.
   - Are executed via `SELECT my_function()`.
   - **Transaction Invariant**: A function **cannot** execute `COMMIT` or `ROLLBACK` internally. Attempting to do so triggers:
     `ERROR: cannot commit while a subtransaction is active` or `invalid transaction termination`.
2. **Procedures (`CREATE PROCEDURE`)**:
   - Introduced in PostgreSQL 11 to support autonomous batch processing and transaction boundaries.
   - Are executed via `CALL my_procedure()`.
   - **Transaction Invariant**: A procedure **can** issue `COMMIT` and `ROLLBACK` statements within its PL/pgSQL body!
   - This allows long-running ETL processes to commit work in batches inside the procedure, releasing table locks and vacuuming dead tuples.

---

#### Q29: SQL Server `OUTPUT` Parameters vs `OUTPUT` Clause in Statements
##### 1. Exact Scenario & Question
Explain the difference between a SQL Server stored procedure `OUTPUT` parameter and an SQL `INSERT ... OUTPUT INSERTED.id` statement. How do you handle each in Java JDBC?

##### 2. What the Interviewer Evaluates
- Disambiguation of SQL Server's procedural `OUTPUT` keyword vs T-SQL `OUTPUT` clause (which generates a `ResultSet`).
- Proper JDBC handling (`registerOutParameter` vs `getResultSet` / `getGeneratedKeys`).

##### 3. Standout Technical Answer
1. **Stored Procedure `OUTPUT` Parameter**:
   - Defined in procedure header: `CREATE PROCEDURE sp_add(@val INT, @out_id INT OUTPUT)`.
   - Transmitted as a scalar field in the TDS RPC response token.
   - Handled in JDBC via `cs.registerOutParameter(2, Types.INTEGER)` and retrieved via `cs.getInt(2)`.
2. **T-SQL `OUTPUT` Clause**:
   - Used inside DML statements: `INSERT INTO audit_log (msg) OUTPUT INSERTED.id, INSERTED.timestamp VALUES ('event')`.
   - **Does NOT return a scalar parameter!** It returns an actual tabular **`ResultSet`**!
   - Handled in JDBC via standard query execution:
     ```java
     PreparedStatement ps = conn.prepareStatement("INSERT INTO ... OUTPUT INSERTED.id VALUES (?)");
     ResultSet rs = ps.executeQuery(); // Must treat as query returning ResultSet!
     ```

---

#### Q30: Calling MySQL Stored Procedures with Multiple Dynamic Out Parameters
##### 1. Exact Scenario & Question
MySQL procedures communicate parameters using user variables (`@var`) under the hood when called from CLI, but via binary protocol when called from JDBC. What driver configuration in `Connector/J` is required to ensure MySQL stored procedure metadata is not re-queried on every invocation?

##### 2. What the Interviewer Evaluates
- Knowledge of `cacheCallableStmts=true` and `noAccessToProcedureBodies=true` in MySQL Connector/J.
- Impact of database permissions on procedure execution.

##### 3. Standout Technical Answer
When MySQL's JDBC driver executes a `CallableStatement`, it attempts to determine parameter types by querying `information_schema.routines` or executing `SHOW CREATE PROCEDURE`.
If the database user does not have `SELECT` grants on `mysql.proc` or `information_schema`, the driver fails with:
`User does not have access to metadata for procedure`.

**The Production Connection String Solution**:
```
jdbc:mysql://db-host:3306/production?cacheCallableStmts=true&callableStmtCacheSize=50&noAccessToProcedureBodies=true
```
- `noAccessToProcedureBodies=true`: Tells the driver to skip querying the procedure body in the database catalog and rely entirely on the Java code's explicit `registerOutParameter()` and `setX()` calls.
- `cacheCallableStmts=true`: Enables an internal LRU cache for callable statement handles.

---

#### Q31: Mapping User-Defined Types (UDT) / Structured Types in Stored Procedures
##### 1. Exact Scenario & Question
An Oracle stored procedure accepts an Oracle PL/SQL Object Type (`CREATE TYPE address_obj AS OBJECT (street VARCHAR2(50), zip VARCHAR2(10))`). How do you pass a Java object into this procedure using Spring JDBC or raw JDBC?

##### 2. What the Interviewer Evaluates
- Understanding of SQL `Types.STRUCT` and `java.sql.Struct` / `SQLData`.
- Handling of custom Oracle UDTs via modern JDBC or Spring Data.

##### 3. Standout Technical Answer
In JDBC, passing database-level object types requires `java.sql.Struct`:

```java
try (Connection conn = dataSource.getConnection();
     CallableStatement cs = conn.prepareCall("{call sp_update_address(?, ?)}")) {
    
    cs.setLong(1, customerId);
    
    // Create SQL Struct matching the database type definition
    Object[] attributes = new Object[] { "123 Wall Street", "10005" };
    java.sql.Struct addressStruct = conn.createStruct("ADDRESS_OBJ", attributes);
    
    // Bind Struct to CallableStatement
    cs.setObject(2, addressStruct, java.sql.Types.STRUCT);
    
    cs.execute();
}
```
In Oracle-specific environments where PL/SQL RECORD types are used (which are distinct from SQL OBJECT types), the JDBC driver requires Oracle-specific `oracle.jdbc.OracleConnection.createStruct()` or wrapping the call in a PL/SQL anonymous block that converts between SQL Types and PL/SQL Record types.

---

#### Q32: Handling Array Parameters (`java.sql.Array`) in Stored Procedures
##### 1. Exact Scenario & Question
How do you pass an array of 1,000 IDs (`List<Long> ids`) into a stored procedure parameter expecting `NUMBER_ARRAY` in Oracle or `BIGINT[]` in PostgreSQL without string concatenation?

##### 2. What the Interviewer Evaluates
- Knowledge of `conn.createArrayOf(typeName, elements)`.
- Difference between PostgreSQL native array types and Oracle custom schema types.
- SQL injection prevention.

##### 3. Standout Technical Answer
```java
List<Long> ids = List.of(101L, 102L, 103L);

try (Connection conn = dataSource.getConnection();
     CallableStatement cs = conn.prepareCall("{call sp_bulk_deactivate(?)}")) {
    
    // Convert List to Object[] array
    Object[] idArray = ids.toArray();
    
    // Create java.sql.Array
    // In PostgreSQL: typeName is "bigint" or "int8"
    // In Oracle: typeName must match the created type name: "TYPE_NUMBER_LIST"
    Array sqlArray = conn.createArrayOf("bigint", idArray);
    
    cs.setArray(1, sqlArray);
    cs.execute();
}
```
**Wire Efficiency**:
This transmits the array as a single strongly-typed binary payload, eliminating SQL injection risks and drastically outperforming comma-separated string splitting (`STRING_SPLIT` or `REGEXP_SUBSTR`) on the database server.

---

#### Q33: Stored Procedures in CQRS Architectures: Command vs Query
##### 1. Exact Scenario & Question
In a Command Query Responsibility Segregation (CQRS) architecture, where do stored procedures belong? Is it an anti-pattern to use stored procedures for Queries, Commands, or both?

##### 2. What the Interviewer Evaluates
- Architectural maturity regarding state mutations vs read models.
- Understanding of domain model decoupling.

##### 3. Standout Technical Answer
In modern CQRS architectures:
1. **Commands (Mutations)**:
   - **Allowed but Controlled**: Stored procedures are frequently used as "Smart Transactions" for ultra-high-throughput financial transactions (e.g., balance transfers, atomic ledger updates) where network round-trip latency between application and DB must be eliminated.
   - **Trade-off**: Business logic moves into database procedural code, making unit testing, version control, and multi-cloud portability difficult.
2. **Queries (Reads)**:
   - **Strongly Recommended**: Stored procedures returning cursors or complex aggregated projections are excellent for high-performance read models. They execute close to the storage engine, leverage indexed materialized views, and return pre-filtered projections without ORM overhead.

---

#### Q34: Stored Procedures vs Direct JPQL/Criteria Queries: Performance Trade-offs
##### 1. Exact Scenario & Question
Provide a benchmark and network latency analysis comparing a complex 5-table JOIN query executed via Hibernate JPQL vs executed via an optimized Database Stored Procedure. When does the stored procedure win, and when does JPQL win?

##### 2. What the Interviewer Evaluates
- Understanding of network chatty vs bulk processing.
- ORM entity hydration overhead vs raw tuple streaming.
- Query plan caching differences.

##### 3. Standout Technical Answer
| Metric | Hibernate JPQL / Criteria | Database Stored Procedure |
|---|---|---|
| **Query Plan Generation** | Hibernate parses JPQL $\rightarrow$ SQM $\rightarrow$ SQL AST $\rightarrow$ DB parses SQL AST $\rightarrow$ DB optimizer plans. | Pre-compiled P-code or cached execution plan in DB Shared Pool. Zero query compilation latency. |
| **Network Latency** | If business logic involves multiple read-then-write steps, multiple network round-trips ($N \times \text{RTT}$) are incurred. | **$1 \times \text{RTT}$**. All intermediate reads, conditional evaluations, and writes execute entirely in DB memory. |
| **JVM Heap Allocation** | **High**. Must allocate Entity instances, hydration snapshots, and persistence context identity maps. | **Low**. Streams lightweight DTO tuples or scalar values directly. |
| **Portability & Maintainability** | **High**. Database agnostic; easily tested with in-memory DB or Testcontainers. | **Low**. Bound to proprietary procedural dialect (PL/SQL, T-SQL, PL/pgSQL). |

**Verdict**: The stored procedure wins overwhelmingly when logic requires high-frequency procedural iteration or heavy data crunching where transmitting intermediate rows across the network is cost-prohibitive.

---

#### Q35: Handling JSON Input and Output Parameters in Stored Procedures
##### 1. Exact Scenario & Question
Modern databases (PostgreSQL `JSONB`, MySQL `JSON`, Oracle `JSON`) allow stored procedures to accept and return JSON payloads. How do you pass a Jackson `JsonNode` or Java record into a stored procedure parameter and read back a JSON result in Spring?

##### 2. What the Interviewer Evaluates
- Integration between Jackson object mapping and JDBC SQL types.
- Handling of `Types.OTHER` in PostgreSQL vs `VARCHAR` in MySQL/Oracle.

##### 3. Standout Technical Answer
```java
@Autowired
private ObjectMapper objectMapper;

public OrderSummary executeJsonProcedure(OrderCommand command) {
    final String sql = "{call sp_process_json_order(?, ?)}";
    
    try (Connection conn = dataSource.getConnection();
         CallableStatement cs = conn.prepareCall(sql)) {
        
        // 1. Serialize Java object to JSON string via Jackson
        String jsonPayload = objectMapper.writeValueAsString(command);
        
        // 2. Bind parameter
        // In PostgreSQL, JSONB requires Types.OTHER:
        cs.setObject(1, jsonPayload, java.sql.Types.OTHER);
        // In Oracle / MySQL: cs.setString(1, jsonPayload);
        
        // 3. Register JSON OUT parameter
        cs.registerOutParameter(2, java.sql.Types.OTHER);
        
        cs.execute();
        
        // 4. Read back JSON string and deserialize
        String jsonResponse = cs.getString(2);
        return objectMapper.readValue(jsonResponse, OrderSummary.class);
        
    } catch (Exception ex) {
        throw new RuntimeException("JSON stored procedure failure", ex);
    }
}
```

---

#### Q36: Asynchronous Stored Procedure Execution in Spring (`@Async` + CompletableFuture)
##### 1. Exact Scenario & Question
How do you invoke a long-running batch stored procedure asynchronously in Spring without holding an HTTP request thread open and without leaking database connections from HikariCP?

##### 2. What the Interviewer Evaluates
- Proper usage of `@Async` with dedicated thread pools.
- Connection lifecycle when running in background threads.
- Propagation of transaction boundaries.

##### 3. Standout Technical Answer
```java
@Service
public class AsyncReportService {

    private final SimpleJdbcCall reportCall;

    public AsyncReportService(DataSource dataSource) {
        this.reportCall = new SimpleJdbcCall(dataSource)
            .withProcedureName("sp_generate_monthly_tax_ledger")
            .withoutProcedureColumnMetaDataAccess();
    }

    // 1. Dedicated ThreadPoolTaskExecutor (Never use default SimpleAsyncTaskExecutor!)
    // 2. Transaction boundary MUST be on the async method, NOT the caller!
    @Async("reportTaskExecutor")
    @Transactional(timeout = 3600) // 1-hour timeout for heavy batch
    public CompletableFuture<ReportResult> executeLongRunningReportAsync(LocalDate month) {
        log.info("Starting async report execution on thread: {}", Thread.currentThread().getName());
        
        MapSqlParameterSource in = new MapSqlParameterSource()
            .addValue("p_month", java.sql.Date.valueOf(month));
            
        Map<String, Object> out = reportCall.execute(in);
        
        ReportResult result = new ReportResult((String) out.get("p_status_file_path"));
        return CompletableFuture.completedFuture(result);
    }
}
```
**Connection Safeguard**:
Because `@Transactional` is declared directly on `executeLongRunningReportAsync`, the HikariCP connection is borrowed *only when the async worker thread begins execution*, and is returned to the pool immediately upon completion, protecting HTTP worker threads from starvation.

---

#### Q37: Testing Stored Procedures with Testcontainers and Flyway
##### 1. Exact Scenario & Question
Why is using an in-memory database like H2 an anti-pattern for testing stored procedures? Describe the production testing architecture using Testcontainers.

##### 2. What the Interviewer Evaluates
- Awareness that H2 does not support Oracle PL/SQL, PostgreSQL PL/pgSQL, or T-SQL dialects.
- Modern integration testing with actual database engines via Docker/Testcontainers.

##### 3. Standout Technical Answer
**Why H2 Fails for Stored Procedures**:
H2 is an in-memory database with its own Java-based procedural syntax. It cannot parse Oracle PL/SQL (`SYS_REFCURSOR`, packages), PostgreSQL PL/pgSQL (`refcursor`, `GET DIAGNOSTICS`), or SQL Server T-SQL (`SET NOCOUNT ON`). Attempting to emulate procedures in H2 creates brittle, fake tests that diverge from production reality.

**The Testcontainers Architecture**:
```java
@Testcontainers
@SpringBootTest
class StoredProcedureIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private PostgresStoredProcRepository repository;

    @Test
    void shouldExecuteStoredProcedureSuccessfully() {
        // Runs Flyway migrations automatically against the actual containerized PostgreSQL!
        int count = repository.executePostgresProcedure(1001);
        assertThat(count).isGreaterThanOrEqualTo(0);
    }
}
```

---

#### Q38: Stored Procedure Security: SQL Injection via Dynamic SQL Inside Procedures
##### 1. Exact Scenario & Question
A security audit flags a stored procedure:
`CREATE PROCEDURE sp_search_users(p_input VARCHAR) AS BEGIN EXECUTE IMMEDIATE 'SELECT * FROM users WHERE name = ''' || p_input || ''''; END;`
The Java developer claims: *"Our code uses `CallableStatement` with `?`, so SQL injection is impossible!"* Is the system secure?

##### 2. What the Interviewer Evaluates
- Recognition of Second-Order / In-Procedure SQL Injection.
- Understanding of database `EXECUTE IMMEDIATE` / `sp_executesql`.

##### 3. Standout Technical Answer
**The System is VULNERABLE to SQL Injection!**
While the Java application safely binds the parameter into the `CallableStatement` via binary protocol, the vulnerability resides **inside the stored procedure itself**:
- The stored procedure performs string concatenation (`||`) inside dynamic SQL (`EXECUTE IMMEDIATE`).
- If an attacker passes input: `' OR '1'='1' --`, the procedure constructs:
  `SELECT * FROM users WHERE name = '' OR '1'='1' --'`
- The database executes the malicious query with the elevated permissions of the stored procedure definer!

**Remediation**:
1. Stored procedures must use parameter binding in dynamic SQL:
   `EXECUTE IMMEDIATE 'SELECT * FROM users WHERE name = :1' USING p_input;`
2. Or use static queries without dynamic execution.

---

### Tier 4: Concurrency, Deadlocks, Batching & Enterprise Resiliency (Q39 – Q50)

#### Q39: Isolation Levels and Phantom Reads in Stored Procedures
##### 1. Exact Scenario & Question
A stored procedure executes two `SELECT COUNT(*)` queries 5 seconds apart to check available inventory. Between the two queries, another transaction commits 100 new rows. Under `READ_COMMITTED`, the counts differ. Under `SERIALIZABLE`, how does the database handle this, and what exception must your Spring service catch?

##### 2. What the Interviewer Evaluates
- Knowledge of ANSI SQL Isolation Levels (`READ_COMMITTED`, `REPEATABLE_READ`, `SERIALIZABLE`).
- Serialization failure error codes (`ORA-08177`, PostgreSQL `40001`).
- Spring retry mechanisms (`@Retryable`).

##### 3. Standout Technical Answer
Under `SERIALIZABLE` isolation:
1. The database takes a snapshot of the database state at the beginning of the transaction.
2. The second `SELECT COUNT(*)` will see the exact same count as the first query (no phantom reads).
3. If the stored procedure then attempts to update or lock those inventory rows, and discovers that concurrent transaction B committed changes to those rows:
   - The database server aborts the transaction and throws a Serialization Failure:
     - PostgreSQL: `PSQLException: ERROR: could not serialize access due to concurrent update (SQLState: 40001)`
     - Oracle: `ORA-08177: can't serialize access for this transaction`.
4. **Spring Remediation**:
   Spring translates this to `CannotAcquireLockException` or `OptimisticLockingFailureException`. The service must be annotated with `@Retryable`:
   ```java
   @Retryable(
       retryFor = { CannotAcquireLockException.class },
       maxAttempts = 3,
       backoff = @Backoff(delay = 100, multiplier = 2)
   )
   @Transactional(isolation = Isolation.SERIALIZABLE)
   public void executeInventoryAllocation() { ... }
   ```

---

#### Q40: Statement Caching vs Connection Pool Size in HikariCP
##### 1. Exact Scenario & Question
You configure HikariCP with `maximumPoolSize=50` and the MySQL JDBC driver with `cachePrepStmts=true&prepStmtCacheSize=250&prepStmtCacheSqlLimit=2048`. Calculate the maximum memory footprint allocated solely for statement caching across the JVM.

##### 2. What the Interviewer Evaluates
- Mathematical calculation of statement cache allocation across pooled connections.
- Memory sizing in enterprise JVM architecture.

##### 3. Standout Technical Answer
In JDBC architectures, statement caching is **per physical connection**, NOT global across the pool!
- Max Connections = $50$
- Cached Statements per Connection = $250$
- Max SQL Length per Statement = $2,048\text{ characters}$
- Approximate memory per prepared statement descriptor in driver heap (metadata, buffers, parameter descriptors) $\approx 25\text{ KB}$.

$$\text{Total Cache Descriptors} = 50 \times 250 = 12,500\text{ statement handles}$$
$$\text{Estimated Heap Memory} = 12,500 \times 25\text{ KB} \approx 312.5\text{ MB}$$

If your JVM has a 1 GB heap (`-Xmx1g`), statement caching alone consumes **over 30% of total heap space**, leading to frequent garbage collection and memory pressure. Pool sizing and statement cache sizing must always be evaluated conjunctively!

---

#### Q41: Handling Database Re-connections and Stale Statements During Failover
##### 1. Exact Scenario & Question
During an AWS Aurora PostgreSQL failover, the master node switches. A long-running Spring batch process executing stored procedures begins throwing `PSQLException: This connection has been closed`. How do HikariCP and Spring recover automatically?

##### 2. What the Interviewer Evaluates
- HikariCP connection health testing (`connectionTestQuery` vs JDBC 4 `isValid()`).
- TCP keep-alive settings and socket timeout configurations.
- Retry interceptors.

##### 3. Standout Technical Answer
When Aurora fails over:
1. The old master either resets TCP sockets or stops responding (dropping packets).
2. If TCP keep-alive or `socketTimeout` is not configured, the JVM thread hangs indefinitely waiting for a socket read.
3. When HikariCP detects an eviction (via `isValid(int timeout)` during borrow), it terminates the broken socket and establishes a new physical connection to the new master.
4. **In-Flight Transaction Failure**: Any stored procedure executing at the exact moment of failover is terminated. The application layer **must** handle idempotency and retry the failed transaction.

---

#### Q42: Migrating from Legacy Stored Procedures to Spring Microservices
##### 1. Exact Scenario & Question
An enterprise monolith contains 500,000 lines of PL/SQL stored procedures containing business logic. You are tasked with migrating to Spring Boot microservices. What is the strangler-fig migration pattern for decoupling stored procedures without a high-risk big-bang rewrite?

##### 2. What the Interviewer Evaluates
- Strategic architecture vs naive rewrites.
- The Strangler Fig application pattern.
- Database view and wrapper procedure techniques.

##### 3. Standout Technical Answer
**The 4-Phase Migration Blueprint**:
1. **Phase 1: Abstract via Spring Gateways**:
   Wrap legacy stored procedure calls inside strict Spring `@Repository` interfaces. Do not expose procedure mechanics to the `@Service` domain layer.
2. **Phase 2: Dual-Writing / Shadow Execution**:
   Re-implement the business logic in Spring Service domain classes. Execute both in production (calling the procedure for real writes, running the Spring service in shadow mode, comparing database outcomes asynchronously).
3. **Phase 3: Database Trigger / Event Publishing**:
   If other legacy systems depend on the procedure, replace the procedure internals with an event emission table or Kafka message publisher.
4. **Phase 4: Flip the Switch**:
   Direct write traffic to the Spring service; demote the stored procedure to a legacy read-only fallback, and ultimately decommission it.

---

#### Q43: Zero-Downtime Schema Evolution Involving Stored Procedures
##### 1. Exact Scenario & Question
You need to alter a stored procedure parameter list (adding a new required parameter `p_tenant_code`). How do you deploy this database change and the corresponding Spring application update across 50 production pods with **zero downtime**?

##### 2. What the Interviewer Evaluates
- Expand/Contract (Parallel Run) database deployment methodology.
- Forward and backward compatibility.

##### 3. Standout Technical Answer
Deploying breaking stored procedure changes in a single step breaks zero-downtime deployments: old pods will crash if the DB changes first, and new pods will crash if the app is deployed first.

**The Expand-Contract Pattern**:
1. **Step 1 (Expand - Database)**:
   Create a **new** procedure (e.g., `sp_transfer_v2`) or overload the existing procedure with the new parameter defaulting to a safe fallback (`p_tenant_code IN VARCHAR2 DEFAULT 'DEFAULT'`).
2. **Step 2 (Deploy - Application)**:
   Roll out the Spring Boot application update rolling across the 50 pods. Both old pods (calling v1 / relying on defaults) and new pods (calling v2 / passing new param) operate concurrently without error.
3. **Step 3 (Contract - Database)**:
   Once 100% of pods are running the new code, update the database to deprecate and drop the legacy v1 procedure signature.

---

#### Q44: Tuning TCP Socket Buffers for High-Volume Stored Procedure Outputs
##### 1. Exact Scenario & Question
A stored procedure returns large data payloads. Network interface cards (NIC) show high packet fragmentation and TCP window exhaustion. What JVM and JDBC parameters optimize TCP throughput for database stored calls?

##### 2. What the Interviewer Evaluates
- Knowledge of OS and socket-level tuning (`SO_RCVBUF`, `SO_SNDBUF`).
- Oracle SDU (Session Data Unit) and PostgreSQL packet tuning.

##### 3. Standout Technical Answer
1. **Oracle Session Data Unit (SDU)**:
   By default, Oracle TNS packets are 8 KB. For large cursor transfers, configure `DEFAULT_SDU_SIZE=32767` in `sqlnet.ora` and pass `oracle.net.sdu=32768` in the JDBC connection URL. This reduces packet framing overhead by 400%.
2. **OS TCP Buffers**:
   Configure driver socket receive buffers:
   `socketReceiveBufferSize=1048576` (1MB).
3. **TCP No Delay (`tcpNoDelay=true`)**:
   Disables Nagle's algorithm, preventing the JVM from delaying small output acknowledgment packets across the wire.

---

#### Q45: Caching Stored Procedure Results in Spring (`@Cacheable`)
##### 1. Exact Scenario & Question
Can you annotate a repository method that calls a stored procedure with `@Cacheable("reference_data")`? What are the key generator requirements and eviction traps?

##### 2. What the Interviewer Evaluates
- Integration of Spring Cache Abstraction with `@Procedure`.
- Cache key serialization for complex procedure parameter sources.

##### 3. Standout Technical Answer
Yes, `@Cacheable` operates at the Spring proxy layer before the repository or JDBC invoker is executed:
```java
@Service
public class ReferenceDataService {

    @Autowired
    private SimpleJdbcCall lookupCall;

    @Cacheable(value = "postal_codes", key = "#countryCode + '_' + #state")
    public List<PostalCodeDTO> getPostalCodes(String countryCode, String state) {
        MapSqlParameterSource in = new MapSqlParameterSource()
            .addValue("p_country", countryCode)
            .addValue("p_state", state);
            
        Map<String, Object> out = lookupCall.execute(in);
        return (List<PostalCodeDTO>) out.get("p_cursor");
    }
}
```
**Trap**: The cached object must be `Serializable` if backed by Redis or Hazelcast. Never cache the raw JDBC `ResultSet`—always map rows into immutable DTOs before returning!

---

#### Q46: Database Privileges and Definer vs Invoker Rights
##### 1. Exact Scenario & Question
What is the difference between `AUTHID DEFINER` and `AUTHID CURRENT_USER` in Oracle / PostgreSQL? How does this impact the database user configured in Spring's `application.yml`?

##### 2. What the Interviewer Evaluates
- Database security architecture.
- Principle of Least Privilege in enterprise connection pooling.

##### 3. Standout Technical Answer
1. **`AUTHID DEFINER` (Default in many databases)**:
   - The stored procedure executes with the permissions of the user who **created** the procedure (the schema owner).
   - **Advantage**: The Spring application database user does not need `SELECT`/`UPDATE` grants on the underlying tables; it only needs `EXECUTE` on the procedure. This prevents SQL injection attacks on raw tables!
2. **`AUTHID CURRENT_USER` (Invoker Rights)**:
   - The stored procedure executes with the permissions of the database user who **invoked** it (the Spring pool user).
   - **Consequence**: The Spring application user must have direct read/write permissions on every underlying table accessed by the procedure.

---

#### Q47: Stored Procedure Execution Behind AWS Aurora / RDS Proxy
##### 1. Exact Scenario & Question
AWS RDS Proxy pools database connections across serverless and microservice instances. Why does RDS Proxy pin a connection to an application instance when a stored procedure or session variable is executed?

##### 2. What the Interviewer Evaluates
- Understanding of RDS Proxy connection pinning.
- Impact on connection pool efficiency in cloud environments.

##### 3. Standout Technical Answer
RDS Proxy achieves high multiplexing by sharing physical database connections among multiple client connections.
**Pinning Mechanics**:
When an application executes a stored procedure:
- The procedure may alter session state (e.g., temporary tables, session variables, cursor locks).
- RDS Proxy cannot guarantee that another client sharing this connection will not be corrupted by this residual state.
- Therefore, RDS Proxy **pins the physical database connection to that specific client session** until the client disconnects!
- **Consequence**: If an application frequently calls stored procedures, RDS Proxy connection multiplexing drops to 0%, defeating the entire purpose of RDS Proxy!

---

#### Q48: Managing Database Savepoints Inside Stored Procedures
##### 1. Exact Scenario & Question
Can a stored procedure create and roll back to a database Savepoint without rolling back the entire Spring transaction? Write the PL/SQL and Java code to verify partial rollback.

##### 2. What the Interviewer Evaluates
- Knowledge of nested transactions and savepoints.
- Error recovery inside stored procedures.

##### 3. Standout Technical Answer
Yes. Stored procedures can encapsulate partial failure handling using savepoints:

```sql
CREATE OR REPLACE PROCEDURE sp_process_order(p_order_id INT, p_coupon_code VARCHAR) AS
BEGIN
    INSERT INTO orders(id, status) VALUES (p_order_id, 'PENDING');
    
    SAVEPOINT coupon_savepoint;
    
    BEGIN
        APPLY_COUPON(p_order_id, p_coupon_code);
    EXCEPTION
        WHEN OTHERS THEN
            -- Roll back only coupon application; preserve base order!
            ROLLBACK TO coupon_savepoint;
    END;
    
    UPDATE orders SET status = 'CONFIRMED' WHERE id = p_order_id;
END;
```
From Java's perspective, the procedure succeeds cleanly. The outer Spring `@Transactional` boundary commits normally, persisting the confirmed order without the invalid coupon.

---

#### Q49: Stored Procedures and Distributed Tracing (OpenTelemetry)
##### 1. Exact Scenario & Question
How do you propagate W3C TraceContext headers (`traceparent`) into database stored procedures so that database administrator trace logs correlate directly with Spring Boot OpenTelemetry spans?

##### 2. What the Interviewer Evaluates
- Observability and distributed tracing architecture.
- Client info tagging in JDBC (`Connection.setClientInfo()`).

##### 3. Standout Technical Answer
JDBC supports `Connection.setClientInfo()`:

```java
@Component
public class TracingStoredProcedureInterceptor {

    @Autowired
    private Tracer tracer;

    public void executeWithTracing(Connection conn, String procName, Consumer<Connection> action) {
        Span currentSpan = tracer.currentSpan();
        if (currentSpan != null) {
            String traceId = currentSpan.context().traceId();
            try {
                // Sets client info in database session (Oracle V$SESSION.CLIENT_IDENTIFIER / PG application_name)
                conn.setClientInfo("OCID.TRACE_ID", traceId);
                conn.setClientInfo("ApplicationName", "OrderService:" + procName);
            } catch (SQLClientInfoException ignored) {}
        }
        
        action.accept(conn);
    }
}
```
DBAs can now query active sessions and trace slow stored procedure executions directly back to specific HTTP requests in Zipkin or Jaeger!

---

#### Q50: The Architect's Decision Matrix: When to NEVER Use Stored Procedures
##### 1. Exact Scenario & Question
You are the Principal Architect at a Tier-1 Fintech. A lead developer proposes writing all new payment calculations as stored procedures. Present your architectural criteria for when stored procedures are strictly **PROHIBITED**.

##### 2. What the Interviewer Evaluates
- Principal-level engineering trade-off analysis.
- Cloud scalability vs vertical database scaling costs.
- CI/CD, testing, and modern DevOps economics.

##### 3. Standout Technical Answer
**Strictly Prohibit Stored Procedures When**:
1. **Horizontal Scaling is Required**: Compute inside an RDBMS scales **vertically** (costing thousands of dollars per CPU core in Oracle/MSSQL licenses). Compute in Spring Boot / Kubernetes scales **horizontally** at commodity prices.
2. **Complex Domain Logic & Unit Testing**: Procedural SQL lacks object-oriented abstractions, modern dependency injection, mocking frameworks, and ergonomic linting.
3. **Multi-Region Active-Active Deployments**: Stored procedures cannot easily run in multi-master, multi-region distributed databases (e.g., CockroachDB, Spanner, YugabyteDB) without severe cross-node consensus penalties.
4. **Fast CI/CD Cadence**: Database schema and procedure migrations require complex DBA approvals, slowing down continuous deployment pipelines.

---

## Track 5: 10 High-Priced Production War-Room Post-Mortems

### Incident 1: The $4.2M Black Friday Double-Spend Outage
- **Company**: Global E-Commerce Conglomerate  
- **Symptoms**: During peak Black Friday traffic, customer account balances were being credited twice on checkout cancellations. Database CPU hit 100%, and payment ledgers went out of balance by $4.2 million.  
- **Root Cause**:
  The checkout cancellation service used Spring Data JPA. When an order was canceled, the code loaded the `Account` entity, set `status = CANCELED`, and called `sp_refund_customer(accountId, amount)`.
  Because `FlushModeType.AUTO` was active, Hibernate flushed the entity update. However, the stored procedure also updated the `accounts` table directly.
  Crucially, the developer **did not call `entityManager.refresh()` or `entityManager.clear()`**.
  Later in the same transaction, a customer notification audit log was saved on the `Account` entity. Hibernate dirty checking ran at commit:
  It compared the in-memory entity (which had the old balance) against the snapshot, and **overwrote the refund balance in the database back to the pre-refund state**, while the refund event had already been published to Kafka! Customers were refunded in cash while their account balance was restored!  
- **Diagnosis**: Heap dump analysis revealed hydrated entity instances in `SessionImpl.PersistenceContext` containing pre-procedure balance values being flushed during `TransactionCoordinatorImpl.beforeCompletion()`.  
- **Immediate Emergency Fix**: Added `entityManager.clear()` immediately following the stored procedure invocation.  
- **Permanent Guardrail**: Refactored the refund workflow to execute exclusively through the stored procedure, removing the JPA entity mapping entirely and migrating to a dedicated `SimpleJdbcCall` command.

---

### Incident 2: The Silent Cursor Leak That Crashed an Airline Reservation System
- **Company**: Major Commercial Airline  
- **Symptoms**: Flight booking engine ground to a halt every 48 hours. All application servers began throwing `ORA-01000: maximum open cursors exceeded`. Restarting the application pods temporarily restored service for 48 hours before failing again.  
- **Root Cause**:
  A stored procedure returned a `SYS_REFCURSOR` containing passenger seat maps. The Java code wrapped the `CallableStatement` in a try-with-resources block, but extracted the cursor via:
  `ResultSet rs = (ResultSet) cs.getObject(1);`
  without closing the `ResultSet`.
  Because the Oracle JDBC driver maintains an active server cursor handle until the `ResultSet` is closed, and because HikariCP pooled the underlying connection, the server cursor never closed. 5,000 seat map queries per minute leaked thousands of cursors until Oracle's `OPEN_CURSORS=2000` limit was reached.  
- **Diagnosis**: DBA queried `v$open_cursor` grouped by `user_name` and `sql_text`, pinpointing 2,000 open cursor handles referencing `SELECT seat_id, status FROM seat_inventory`.  
- **Immediate Emergency Fix**: DBA increased `OPEN_CURSORS` to 20,000 to buy time.  
- **Permanent Guardrail**: Mandated nested try-with-resources on all `ResultSet` objects extracted from `CallableStatement`, and configured HikariCP `leakDetectionThreshold=5000`.

---

### Incident 3: The 18-Minute Startup Cloud Freeze
- **Company**: Healthcare SaaS Platform  
- **Symptoms**: Auto-scaling during a traffic spike failed. New Kubernetes pods took 18 minutes to start, causing all incoming health check probes to fail and Kubernetes to kill the pods in an infinite crash loop.  
- **Root Cause**:
  The application initialized 40 separate `SimpleJdbcCall` beans at startup. None of them had `.withoutProcedureColumnMetaDataAccess()` configured.
  When 30 new pods spawned simultaneously, 1,200 concurrent queries hit Oracle's `ALL_ARGUMENTS` system catalog table. The Oracle data dictionary suffered catastrophic `row cache lock` contention, freezing the entire database catalog.  
- **Diagnosis**: ASH (Active Session History) reports showed 100% of database wait events on `library cache lock` and `row cache lock` originating from queries matching `SELECT * FROM ALL_ARGUMENTS WHERE OBJECT_NAME = :1`.  
- **Immediate Emergency Fix**: Disabled Kubernetes horizontal auto-scaling and manually booted pods in batches of 2.  
- **Permanent Guardrail**: Added `.withoutProcedureColumnMetaDataAccess()` and explicit `declareParameters()` to every `SimpleJdbcCall` instance, reducing pod startup time to 4.2 seconds.

---

### Incident 4: The PostgreSQL Autocommit Portal Disappearance
- **Company**: Real-Time Logistics Tracking  
- **Symptoms**: Microservice throwing intermittent `PSQLException: ERROR: cursor "<unnamed portal 1>" does not exist` on 5% of tracking requests.  
- **Root Cause**:
  A repository method calling a PostgreSQL `refcursor` function was called from two places: a `@Transactional` service (which worked), and a non-transactional scheduled task.
  In the scheduled task, `conn.getAutoCommit()` was `true`. PostgreSQL executed the function, created the portal, committed immediately, and destroyed the portal before the driver could fetch the rows.  
- **Diagnosis**: Traced failed requests to the scheduled task thread pool where `TransactionSynchronizationManager.isActualTransactionActive()` evaluated to `false`.  
- **Immediate Emergency Fix**: Wrapped the scheduled task execution in `TransactionTemplate.execute()`.  
- **Permanent Guardrail**: Added an assertion in the repository method:
  `Assert.isTrue(TransactionSynchronizationManager.isActualTransactionActive(), "Cursor calls require active transaction!");`.

---

### Incident 5: The Optimistic Locking Bypass Phantom Update
- **Company**: Fintech Lending Platform  
- **Symptoms**: Loan interest rates were reverting to old values randomly during concurrent approvals.  
- **Root Cause**:
  A stored procedure updated loan statuses and interest rates directly via SQL. The JPA entity `Loan` had a `@Version` field. The stored procedure did not increment the `version` column.
  When an underwriter concurrently approved the loan in the UI, their browser submitted a form with the old version number. Hibernate checked the database: the version had not changed! Hibernate committed the update, silently overwriting the interest rate calculated by the stored procedure.  
- **Diagnosis**: Audit table comparisons showed `interest_rate` changing from 5.5% $\rightarrow$ 6.2% (via procedure) $\rightarrow$ 5.5% (via JPA update) within 200 milliseconds, with `version` jumping only once.  
- **Immediate Emergency Fix**: Updated the stored procedure to include `version = version + 1`.  
- **Permanent Guardrail**: Enforced strict architectural rule: Stored procedures must never touch tables mapped to JPA entities with `@Version` unless the procedure explicitly increments the version.

---

### Incident 6: The HikariCP Connection Starvation Deadlock
- **Company**: Digital Banking Portal  
- **Symptoms**: Entire API gateway returning HTTP 504 Gateway Timeout. HikariCP pool was 100% exhausted with 0 active SQL queries running on the database server!  
- **Root Cause**:
  A service method was annotated with `@Transactional`. It called JPA repository methods (borrowing Connection 1). Inside the same method, it invoked an asynchronous worker (`@Async`) which invoked a `SimpleJdbcCall` that waited for a lock held by Connection 1.
  Under load, the thread pool exhausted all HikariCP connections: parent threads held connections waiting for async tasks, while async tasks waited in queue for a connection from the pool.  
- **Diagnosis**: Thread dumps showed 50 threads in `HikariPool.getConnection()` state `TIMED_WAITING`, while 50 active worker threads were blocked in `Future.get()`.  
- **Immediate Emergency Fix**: Increased HikariCP pool size from 50 to 150.  
- **Permanent Guardrail**: Eliminated nested `@Async` calls within `@Transactional` boundaries.

---

### Incident 7: The Dynamic SQL Statement Cache Memory Explosion
- **Company**: Multi-Tenant CRM Provider  
- **Symptoms**: JVM memory usage climbed steadily over 7 days until pods crashed with `OutOfMemoryError: Metaspace` or Tenured Heap exhaustion. GC pause times escalated to 14 seconds.  
- **Root Cause**:
  A developer wrote dynamic stored procedure invocation strings:
  `conn.prepareCall("{call sp_" + tenantId + "_process(?)}")`.
  Because the driver had `cachePrepStmts=true`, every tenant generated a unique cache key. With 10,000 tenants, the driver's statement cache held 10,000 `CallableStatement` instances per pooled connection, consuming 1.2 GB of heap space.  
- **Diagnosis**: Eclipse Memory Analyzer (MAT) showed `com.mysql.cj.jdbc.ConnectionImpl.preparedStatementCache` holding 78% of retained heap.  
- **Immediate Emergency Fix**: Disabled `cachePrepStmts` in the JDBC URL.  
- **Permanent Guardrail**: Refactored procedures to accept `tenantId` as a parameter: `{call sp_process_tenant(?, ?)}`.

---

### Incident 8: The SQL Server `NOCOUNT` Missing Result Set Crash
- **Company**: Insurance Claims Processor  
- **Symptoms**: High-priority claims batch failed nightly with `com.microsoft.sqlserver.jdbc.SQLServerException: The statement did not return a result set.`  
- **Root Cause**:
  A DBA modified a claims stored procedure, adding an intermediate `UPDATE claim_metrics SET run_date = GETDATE()` before the final `SELECT` query.
  The procedure lacked `SET NOCOUNT ON`.
  The intermediate `UPDATE` sent a `DONE_IN_PROC` token across the wire. The JDBC driver's `cs.executeQuery()` encountered this token, assumed the query had finished without a result set, and threw an exception.  
- **Diagnosis**: Profiling with SQL Server Profiler showed `SP:StmtCompleted` events interleaved with RPC output tokens.  
- **Immediate Emergency Fix**: Added `SET NOCOUNT ON;` at the beginning of the stored procedure.  
- **Permanent Guardrail**: Added CI SQL linters to ensure all T-SQL procedures contain `SET NOCOUNT ON`.

---

### Incident 9: The Autonomous Transaction Self-Deadlock Freeze
- **Company**: Wealth Management Engine  
- **Symptoms**: Wire transfer transactions hanging indefinitely until HTTP client timeouts fired. Database CPU was 0%, but active sessions climbed to maximum capacity.  
- **Root Cause**:
  A Spring `@Transactional` method updated a customer's fee status in the `customers` table. It then called an audit procedure defined with Oracle `PRAGMA AUTONOMOUS_TRANSACTION`.
  The audit procedure attempted to read the customer's fee status using `SELECT ... FOR UPDATE`.
  Because the parent Spring transaction held the exclusive row lock, the autonomous session blocked waiting for the parent transaction to commit, while the parent transaction was waiting for the procedure to return!  
- **Diagnosis**: DBA checked `v$locked_object` and `v$session_blockers`, finding Session A blocking Session B where both sessions originated from the same IP address and thread name!  
- **Immediate Emergency Fix**: Removed `FOR UPDATE` from the autonomous audit procedure.  
- **Permanent Guardrail**: Architecturally banned autonomous transactions from querying or locking rows modified within the calling scope.

---

### Incident 10: The In-Procedure Second-Order SQL Injection Breach
- **Company**: Gaming Platform  
- **Symptoms**: Attacker dumped user credentials and password hashes despite all Java repository code using prepared statements and `@Procedure`.  
- **Root Cause**:
  A stored procedure used dynamic SQL concatenation:
  `EXECUTE IMMEDIATE 'SELECT id FROM users WHERE username = ''' || p_user || '''';`
  An attacker registered with a username containing an SQL injection payload:
  `admin' UNION SELECT password_hash FROM admin_credentials --`.
  When the Java app called `{call sp_validate_user(?)}`, the parameter was passed safely across JDBC, but executed dynamically inside the DB engine, dumping hashes into the output cursor.  
- **Diagnosis**: Security incident team analyzed database audit logs, identifying unauthorized queries executed from within `sp_validate_user`.  
- **Immediate Emergency Fix**: Rewrote the procedure to use static SQL queries.  
- **Permanent Guardrail**: Integrated static code analysis (SonarQube + SQLFluff) across all database migration scripts.

---

## Track 6: 8 Architecture Anti-Patterns & Refactoring Blueprints

### Anti-Pattern 1: The "Fire-and-Forget" Entity Mutation Before Procedure Call
- **The Code Smell**: Mutating a JPA entity and immediately calling a stored procedure via `JdbcTemplate` without calling `entityManager.flush()`.
- **The Disaster**: The stored procedure reads stale database data because Hibernate has not pushed pending `ActionQueue` DML over the socket.
- **The Refactoring**:
  ```java
  // ❌ ANTI-PATTERN: Procedure runs against stale DB state
  user.setActive(true);
  procedureInvoker.executeActivation(user.getId());

  // ✅ REFACTORED: Enforce database synchronization
  user.setActive(true);
  entityManager.flush(); // Flush dirty entities to DB socket
  procedureInvoker.executeActivation(user.getId());
  entityManager.refresh(user); // Synchronize post-procedure DB state
  ```

---

### Anti-Pattern 2: The Unclosed Cursor Extraction
- **The Code Smell**: Extracting a `ResultSet` from a `CallableStatement` without a dedicated `try-with-resources` block.
- **The Disaster**: Exhausts database open cursor handles (`ORA-01000`), bringing down the database.
- **The Refactoring**:
  ```java
  // ❌ ANTI-PATTERN: Leaks database cursor if exception occurs
  CallableStatement cs = conn.prepareCall("{call get_cursor(?)}");
  cs.registerOutParameter(1, Types.REF_CURSOR);
  cs.execute();
  ResultSet rs = (ResultSet) cs.getObject(1);
  while (rs.next()) { ... }
  cs.close(); // rs was never closed!

  // ✅ REFACTORED: Nested Try-With-Resources
  try (CallableStatement cs = conn.prepareCall("{call get_cursor(?)}")) {
      cs.registerOutParameter(1, Types.REF_CURSOR);
      cs.execute();
      try (ResultSet rs = (ResultSet) cs.getObject(1)) {
          while (rs.next()) { ... }
      }
  }
  ```

---

### Anti-Pattern 3: Metadata Introspection on Dynamic `SimpleJdbcCall`
- **The Code Smell**: Instantiating `new SimpleJdbcCall(dataSource)` inside a request-scoped method without compile-time metadata caching.
- **The Disaster**: Triggers database catalog queries on every single HTTP request, destroying database throughput.
- **The Refactoring**: Initialize `SimpleJdbcCall` as a singleton Spring bean in `@PostConstruct`, call `.withoutProcedureColumnMetaDataAccess()`, and declare parameters explicitly.

---

### Anti-Pattern 4: The Unchecked `@Version` Invalidation
- **The Code Smell**: Updating entity tables inside stored procedures without incrementing the JPA version column.
- **The Disaster**: Silent lost updates due to broken optimistic locking.
- **The Refactoring**: Always increment the version column in the procedure DML: `version = version + 1`.

---

### Anti-Pattern 5: The Monolithic "Do-Everything" Stored Procedure
- **The Code Smell**: A single stored procedure with 3,000 lines of PL/SQL that updates 15 tables, generates invoices, and sends emails via DB mail.
- **The Disaster**: Long-running database locks, inability to test in isolation, high database license costs.
- **The Refactoring**: Decompose into domain events. Use the Outbox Pattern where the database only writes state, and Spring workers handle orchestration and notifications.

---

### Anti-Pattern 6: Mixing Separate Transaction Managers on Same Thread
- **The Code Smell**: Defining both `JpaTransactionManager` and `DataSourceTransactionManager` and expecting them to share transactions.
- **The Disaster**: Different physical database connections, leading to self-deadlocks and uncommitted reads.
- **The Refactoring**: Standardize on `JpaTransactionManager` across the entire application context.

---

### Anti-Pattern 7: Catching Raw `SQLException` in Domain Services
- **The Code Smell**: Writing `catch (SQLException ex)` inside Spring `@Service` classes.
- **The Disaster**: Violates architectural layering; couples domain services to JDBC implementation details.
- **The Refactoring**: Allow Spring to translate errors to `DataAccessException` or use a `@ControllerAdvice` / Domain Exception mapper.

---

### Anti-Pattern 8: Dynamic SQL Concatenation Inside Stored Procedures
- **The Code Smell**: Building SQL query strings inside PL/SQL or T-SQL using string concatenation.
- **The Disaster**: Catastrophic SQL injection vulnerabilities bypassing Java-level parameter bindings.
- **The Refactoring**: Use static SQL queries or parameterized `USING` bindings in `EXECUTE IMMEDIATE`.

---

## Track 7: Exhaustive Architectural Comparison Matrix & Ecosystem Trade-offs

| Architectural Dimension | Spring Data JPA `@Procedure` | Spring JDBC `SimpleJdbcCall` | Raw JDBC `CallableStatement` | Hibernate SQM `ProcedureCall` |
|---|---|---|---|---|
| **Primary Abstraction Layer** | High-level Repository Interface | Mid-level Metadata Helper | Low-level Core JDBC API | Low-level ORM Query Engine |
| **Configuration Overhead** | Low (Annotations on Entity/Repo) | Low-Medium (Java Configuration) | High (Manual parameter binding) | Medium (Programmatic Session API) |
| **Startup Metadata Introspection** | Parsed at Spring context bootstrap | High overhead unless disabled | **Zero** overhead | Parsed at SessionFactory init |
| **`REF CURSOR` Support** | Limited (Driver-dependent) | **Native & Ergonomic** (RowMapper) | **Full Manual Control** | Fully supported via Query API |
| **Multiple Result Sets** | Not supported | Supported via RowMapper list | **Full Manual Control** | Supported |
| **Entity Hydration & L1 Cache** | Automatically enrolls results in L1 | Completely bypasses L1 Cache | Completely bypasses L1 Cache | Can enroll results into L1 Cache |
| **Dirty Checking Flush** | Auto-flushes before call | **Does NOT auto-flush** | **Does NOT auto-flush** | Auto-flushes before call |
| **Performance Overhead** | Small proxy dispatch overhead | Negligible after `compile()` | **Absolute Maximum (Zero overhead)** | Small Session coordinator overhead |
| **Multi-DB Dialect Portability** | Medium (Dialect bound) | High (Spring abstraction) | Low (Vendor SQL escape syntax) | High (Hibernate Dialect abstraction) |
| **Dynamic Parameter Support** | Rigid (Static annotations) | **Highly Dynamic** | **Fully Dynamic** | Dynamic via ParameterRegistration |
| **Best Used For** | Standard CRUD procedures with 1 OUT | Complex business calls & cursors | Ultra-low latency trading/ETL | Mixed entity-graph procedure calls |

---

## Verification & Architecture Checklist

- [x] **Connection Sharing Verified**: Confirmed `JpaTransactionManager` binds physical connection to both `EntityManager` and `JdbcTemplate`.
- [x] **Flush Invariants Guarded**: Enforced `entityManager.flush()` prior to out-of-band stored procedure execution.
- [x] **Persistence Context Eviction**: Enforced `entityManager.refresh()` or `clear()` post-procedure execution.
- [x] **Cursor Leaks Eliminated**: Nested try-with-resources implemented on both `CallableStatement` and `ResultSet`.
- [x] **Metadata Introspection Disabled**: `.withoutProcedureColumnMetaDataAccess()` and explicit parameters configured on `SimpleJdbcCall`.
- [x] **Optimistic Locking Maintained**: Verified stored procedures increment `@Version` column.
- [x] **Standard JDBC Types Used**: Modern `java.sql.Types.REF_CURSOR` adopted over vendor-specific constants.
