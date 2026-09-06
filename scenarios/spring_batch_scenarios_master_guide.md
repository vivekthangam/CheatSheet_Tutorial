[🏠 Back to Home](README.md) | [📦 Spring Batch Master Guide](spring_batch.md) | [🍃 Spring Boot Master Guide](spring_master_guide.md)

# 📦 Spring Batch 5+: 50+ Real-World Production Interview Scenarios Master Guide

[![Spring Batch](https://img.shields.io/badge/Spring%20Batch-5.1%2B-blue.svg?style=for-the-badge&logo=springboot)](https://spring.io/projects/spring-batch)
[![Java](https://img.shields.io/badge/Java-17%20%2F%2021%20LTS-orange.svg?style=for-the-badge&logo=openjdk)](https://www.oracle.com/java/)
[![Level](https://img.shields.io/badge/Tier-1%20Panels-Staff%20%2F%20Principal-red.svg?style=for-the-badge)](https://github.com/)

An exhaustive, battle-tested compilation of **real-world production interview scenarios** covering Spring Batch 5.x, chunk-oriented processing, transaction boundaries, stateful readers, skip/retry policies, multi-threaded step deadlocks, database partitioning, and metadata checkpointing.

Every scenario strictly follows the **5-Part Tier-1 Product Engineering Format**:
1. **Exact Question Asked by Tier-1 Product Panels (with detailed scenario context)**
2. **What the Interviewer Evaluates Under the Surface (mental criteria, low-level runtime knowledge)**
3. **Standout Technical Answer (deep runtime mechanics, low-level database/transaction details, zero fluff)**
4. **Follow-Up Trap Question & Winning Answer (catching surface memorizers)**
5. **Production Sample Code with Detailed Technical Explanation & Pitfall Avoidance**

---

## 📑 Category Navigation

- [Category 1: Chunk Architecture, Boundaries & Commit Intervals (Q1 – Q10)](#category-1-chunk-architecture-boundaries--commit-intervals)
- [Category 2: Readers, Writers & Stream State Management (Q11 – Q20)](#category-2-readers-writers--stream-state-management)
- [Category 3: Fault Tolerance: Skip, Retry & Deadlocks (Q21 – Q30)](#category-3-fault-tolerance-skip-retry--deadlocks)
- [Category 4: Multi-Threading, Partitioning & Remote Chunking (Q31 – Q40)](#category-4-multi-threading-partitioning--remote-chunking)
- [Category 5: JobRepository Metadata, Restarts & Parameters (Q41 – Q50)](#category-5-jobrepository-metadata-restarts--parameters)
- [Category 6: Production War Room Incidents & Outage Forensics (Q51 – Q60)](#category-6-production-war-room-incidents--outage-forensics)

---

# Category 1: Chunk Architecture, Boundaries & Commit Intervals

### Q1: How does Spring Batch 5 demarcate Transaction Boundaries in Chunk-Oriented Processing, and what happens to the Database Connection during `ItemProcessor` execution?
- **Scenario Context:** In a bank ledger batch job, the `ItemProcessor` makes an external HTTP call to verify account sanctions. Under high volume, the database runs out of connections, throwing `HikariPool-1 - Connection is not available`.
- **What the Interviewer Evaluates:** Exact transaction boundary demarcation in chunk processing, connection checkout lifecycle, and how remote I/O inside processors starves database pools.
- **Standout Technical Answer:**
  - In a standard chunk-oriented step:
    ```
    Reader (read 1 by 1) ──► Processor (process 1 by 1) ──► Writer (write all 1000 items)
    ```
  - **Transaction Demarcation Lifecycle:**
    1. By default, Spring Batch starts a **new database transaction** at the beginning of the chunk (before reading items).
    2. A database connection is borrowed from HikariCP and held open by the active transaction for the **entire duration of the chunk**, including all 1,000 `ItemReader.read()` and `ItemProcessor.process()` invocations!
    3. If `ItemProcessor` calls an external REST API taking 200ms per item:
       $$1,000\text{ items} \times 200\text{ms} = 200\text{ seconds!}$$
    4. The database connection remains checked out and idle for 3+ minutes, holding database locks and starving other application threads.
  - **The Production Fix:**
    Configure `.processorNonTransactional()`. This instructs Spring Batch to read and process items **outside** of a database transaction. The database transaction is opened *only* when the chunk is passed to the `ItemWriter`, reducing connection hold time from 200 seconds to 50 milliseconds!
- **Follow-Up Trap:** *"What happens if you use an in-memory `ResourcelessTransactionManager` in Spring Batch?"*
  - *Winning Answer:* "A `ResourcelessTransactionManager` commits transactions only in memory and never touches the database. If you use it, your database operations are executed in autocommit mode without any real transaction rollback guarantees, leading to partial commits if a chunk fails midway!"
- **Production Sample Code & Walkthrough:**
```java
@Configuration
public class OptimizedBatchStepConfig {

    @Bean
    public Step paymentLedgerStep(JobRepository jobRepository,
                                  PlatformTransactionManager txManager,
                                  ItemReader<PaymentRecord> reader,
                                  ItemProcessor<PaymentRecord, LedgerEntity> processor,
                                  ItemWriter<LedgerEntity> writer) {
        return new StepBuilder("paymentLedgerStep", jobRepository)
            .<PaymentRecord, LedgerEntity>chunk(1000, txManager)
            .reader(reader)
            .processor(processor)
            .writer(writer)
            .faultTolerant()
            // CRITICAL: Keeps DB connection closed during remote HTTP processor calls!
            .processorNonTransactional()
            .build();
    }
}
```

---

# Category 2: Readers, Writers & Stream State Management

### Q2: Why does `JdbcCursorItemReader` cause database connection timeouts or OOM on 10-million row tables, and when should you switch to `JdbcPagingItemReader`?
- **Scenario Context:** A nightly batch job reads 10 million transactions using `JdbcCursorItemReader`. Midway through, the database terminates the cursor with `ORA-01555: snapshot too old` or PostgreSQL `canceling statement due to statement timeout`.
- **What the Interviewer Evaluates:** Cursor-based vs paging-based streaming, database undo/rollback segment limits, and memory consumption profiles.
- **Standout Technical Answer:**
  - **`JdbcCursorItemReader` (Cursor-Based):**
    - Opens a single persistent cursor over a single database connection and streams rows row-by-row via the network socket.
    - *Advantages:* Fastest possible throughput; constant $O(1)$ JVM memory consumption if `fetchSize` is configured.
    - *Disadvantages:* Requires an open connection and database transaction for the entire duration of the job (hours). In PostgreSQL or Oracle, holding a long-running snapshot blocks database garbage collection (MVCC VACUUM) and causes `ORA-01555: snapshot too old` when rollback segments wrap around.
  - **`JdbcPagingItemReader` (Paging-Based):**
    - Fetches data in discrete SQL pages (e.g. 1,000 rows per query using keyset pagination or `LIMIT/OFFSET`).
    - *Advantages:* Releases the database connection and transaction between pages, completely eliminating long-running transaction snapshot timeouts. Highly compatible with multi-threaded steps!
    - *Disadvantages:* Slightly higher CPU overhead due to re-executing query sorting and index scans.
- **Follow-Up Trap:** *"Why must `JdbcPagingItemReader` have an unambiguous, unique `ORDER BY` clause?"*
  - *Winning Answer:* "If the sort key is not unique (e.g. sorting only by `creation_date`), rows with identical timestamps can shift between pages when records are inserted or modified during job execution, resulting in duplicate processing or skipped records!"
- **Production Sample Code & Walkthrough:**
```java
@Configuration
public class PagingReaderConfig {

    @Bean
    public JdbcPagingItemReader<CustomerOrder> orderPagingReader(DataSource dataSource) {
        return new JdbcPagingItemReaderBuilder<CustomerOrder>()
            .name("orderPagingReader")
            .dataSource(dataSource)
            .fetchSize(1000)
            .pageSize(1000)
            .rowMapper((rs, rowNum) -> new CustomerOrder(
                rs.getLong("id"),
                rs.getString("order_number"),
                rs.getBigDecimal("total_amount")
            ))
            .queryProvider(createQueryProvider(dataSource))
            .build();
    }

    private PagingQueryProvider createQueryProvider(DataSource dataSource) {
        PostgresPagingQueryProviderFactoryBean provider = new PostgresPagingQueryProviderFactoryBean();
        provider.setDataSource(dataSource);
        provider.setSelectClause("SELECT id, order_number, total_amount");
        provider.setFromClause("FROM customer_orders");
        provider.setWhereClause("WHERE status = 'PENDING'");
        // CRITICAL: Sort key MUST be strictly unique (primary key)!
        provider.setSortKeys(Map.of("id", Order.ASCENDING));
        try {
            return provider.getObject();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize query provider", e);
        }
    }
}
```

---

# Category 3: Fault Tolerance: Skip, Retry & Deadlocks

### Q3: What is the exact sequence of events when `skipLimit` is exceeded in Spring Batch, and how do you implement a Dead Letter Queue (DLQ) pattern for batch processing?
- **Scenario Context:** A billing file contains 50 corrupted lines. The step has `.skipLimit(10)`. When corrupt line 11 is encountered, the job terminates immediately with `SkipLimitExceededException`, leaving the remaining 900,000 valid records unbilled.
- **What the Interviewer Evaluates:** Skip threshold budgeting, `ItemSkipListener` error auditing, and implementing robust quarantine architectures.
- **Standout Technical Answer:**
  - Spring Batch tracks skipped items per step in `StepExecution.getSkipCount()`.
  - When a skipable exception occurs in `ItemReader`, `ItemProcessor`, or `ItemWriter`:
    1. If `skipCount < skipLimit`, Spring Batch increments the skip counter, invokes the appropriate `ItemSkipListener` hook (`onSkipInRead`, `onSkipInProcess`, `onSkipInWrite`), and continues execution.
    2. Once `skipCount >= skipLimit`, the very next exception triggers **`SkipLimitExceededException`**. The active transaction is rolled back, the step status is marked `FAILED`, and the entire job halts.
  - **The Production Pattern:**
    1. Set `skipLimit` based on acceptable data error ratios (e.g. 100 per million).
    2. Implement an **`ItemSkipListener`** that writes failed records, exception stack traces, and line numbers to a dedicated **`batch_error_quarantine` table** or Kafka Dead Letter Queue (DLQ).
    3. Run a secondary reconciliation job to review and re-ingest quarantined records without blocking the main pipeline.
- **Follow-Up Trap:** *"Does `ItemReader` skip re-read the failed item?"*
  - *Winning Answer:* "No! When an exception occurs during `ItemReader.read()`, the reader simply drops that item, logs the skip, and immediately attempts to read the next item. Only write failures trigger the scan-and-retry mechanism."
- **Production Sample Code & Walkthrough:**
```java
@Component
public class QuarantineSkipListener implements ItemSkipListener<BillingRecord, InvoiceEntity> {

    private final JdbcTemplate jdbcTemplate;

    public QuarantineSkipListener(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void onSkipInProcess(BillingRecord item, Throwable t) {
        quarantineRecord(item.accountId(), "PROCESS_ERROR", t.getMessage());
    }

    @Override
    public void onSkipInWrite(InvoiceEntity item, Throwable t) {
        quarantineRecord(item.getAccountId(), "WRITE_ERROR", t.getMessage());
    }

    private void quarantineRecord(Long accountId, String stage, String reason) {
        String sql = "INSERT INTO batch_quarantine (account_id, stage, reason, quarantined_at) VALUES (?, ?, ?, NOW())";
        jdbcTemplate.update(sql, accountId, stage, reason);
    }
}
```

---

# Category 4: Multi-Threading, Partitioning & Remote Chunking

### Q4: What is the architectural difference between a Multi-Threaded Step and a Partitioned Step in Spring Batch, and why does Partitioning scale better?
- **Scenario Context:** An enterprise needs to reduce batch ingestion time from 4 hours to 20 minutes across 50 million records. A developer proposes adding a `TaskExecutor` to the step.
- **What the Interviewer Evaluates:** Concurrency trade-offs, stateful reader safety, database lock contention, and horizontal scaling patterns.
- **Standout Technical Answer:**
  - **Multi-Threaded Step (Single Process, Multiple Threads):**
    - A single `ItemReader` is shared across multiple worker threads via a synchronized wrapper.
    - *Bottleneck 1:* Contention on the synchronized `read()` lock limits scaling to 4–8 threads.
    - *Bottleneck 2:* Chunks are committed out of order. If thread 2 commits chunk 2 before thread 1 commits chunk 1, **restartability is permanently broken**!
  - **Partitioned Step (Master-Worker Architecture):**
    - The master step uses a **`Partitioner`** to divide the dataset into discrete, non-overlapping segments (e.g. Partition 1: IDs 1–1,000,000; Partition 2: IDs 1,000,001–2,000,000).
    - Each partition is assigned to an independent **Worker Step**.
    - *Advantages:*
      1. Every worker step has its own dedicated, unsynchronized `ItemReader`, `ItemProcessor`, and `ItemWriter` instance.
      2. Zero thread contention on readers.
      3. Each worker step maintains its own independent `StepExecution` context in the database, preserving 100% restartability!
      4. Can be scaled across multiple machines via **Remote Partitioning** using Kafka or RabbitMQ!
- **Follow-Up Trap:** *"How does Remote Chunking differ from Remote Partitioning?"*
  - *Winning Answer:* "In Remote Chunking, reading happens centrally on the master node, and chunks of items are sent across a message broker to remote workers for processing and writing. In Remote Partitioning, only metadata instructions (ranges/queries) are sent to workers, and each worker reads its own data locally, resulting in vastly lower network overhead!"
- **Production Sample Code & Walkthrough:**
```java
@Configuration
public class PartitionScalingConfig {

    @Bean
    public Step masterStep(JobRepository jobRepository, Step workerStep, Partitioner dbPartitioner) {
        return new StepBuilder("masterStep", jobRepository)
            .partitioner("workerStep", dbPartitioner)
            .step(workerStep)
            .gridSize(10) // 10 parallel execution threads
            .taskExecutor(taskExecutor())
            .build();
    }

    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setThreadNamePrefix("batch-worker-");
        executor.initialize();
        return executor;
    }
}
```

---

# Category 5: JobRepository Metadata, Restarts & Parameters

### Q5: What is the exact relationship between `JobInstance`, `JobExecution`, and `JobParameters`, and how do you make a failed Job restartable?
- **Scenario Context:** A daily reconciliation job with parameter `date=2026-09-06` fails halfway through. When triggering the job again with the exact same parameter, Spring Batch throws `JobInstanceAlreadyCompleteException`.
- **What the Interviewer Evaluates:** Understanding of Spring Batch metadata tables (`BATCH_JOB_INSTANCE`, `BATCH_JOB_EXECUTION`, `BATCH_JOB_EXECUTION_PARAMS`).
- **Standout Technical Answer:**
  - **`JobInstance`**: Represents the logical run of a job identified uniquely by:
    $$\text{Job Name} + \text{Identifying JobParameters}$$
  - **`JobExecution`**: Represents a physical execution attempt of a `JobInstance`. One `JobInstance` can have multiple `JobExecution` entries (e.g. Execution 1: FAILED; Execution 2: COMPLETED).
  - **The Restart Rules:**
    1. If a `JobExecution` completes with status **`COMPLETED`**, Spring Batch considers that `JobInstance` finished forever. Attempting to run with identical parameters throws `JobInstanceAlreadyCompleteException`.
    2. If a `JobExecution` terminates with status **`FAILED`**, Spring Batch allows you to launch the job again with the **exact same parameters**. It reuses the existing `JobInstance`, reads the last committed checkpoint from `BATCH_STEP_EXECUTION_CONTEXT`, and resumes from that point!
  - **Non-Identifying Parameters:**
    In Spring Batch 5, parameters can be marked non-identifying: `new JobParametersBuilder().addString("runId", UUID.randomUUID().toString(), false)`. Non-identifying parameters are stored in the database but ignored when computing the `JobInstance` hash!
- **Follow-Up Trap:** *"What happens if a job crashes due to an OS `kill -9` signal while in state `STARTED`?"*
  - *Winning Answer:* "Because the process was killed instantly, Spring Batch could not update the database state from `STARTED` to `FAILED`. When you try to restart, Spring Batch sees an active `STARTED` execution and rejects the run! An administrator must manually update `BATCH_JOB_EXECUTION.STATUS` to `FAILED` via SQL or `JobExplorer` before restarting."
- **Production Sample Code & Walkthrough:**
```java
@Service
public class BatchJobLauncherService {

    private final JobLauncher jobLauncher;
    private final Job billingJob;

    public BatchJobLauncherService(JobLauncher jobLauncher, Job billingJob) {
        this.jobLauncher = jobLauncher;
        this.billingJob = billingJob;
    }

    public JobExecution launchOrRestartJob(LocalDate businessDate) throws Exception {
        JobParameters params = new JobParametersBuilder()
            // Identifying parameter: defines unique business run for that date
            .addLocalDate("businessDate", businessDate)
            // Non-identifying parameter: tracking metadata only (does not alter JobInstance key)
            .addString("triggeredBy", "CRON_ORCHESTRATOR", false)
            .toJobParameters();

        // If previous run with this date failed, it automatically resumes from last checkpoint!
        return jobLauncher.run(billingJob, params);
    }
}
```

---

# Category 6: Production War Room Incidents & Outage Forensics

### Q6: WAR ROOM RCA: Infinite Memory Leak & Pod OOMKilled via Unclosed FlatFileItemReader in Cloud Pods
- **Severity:** P0 Crash (Tomcat pods killed by Linux Kernel OOM killer)
- **Mean Time to Recovery (MTTR):** 38 minutes
- **Symptoms:** Nightly file ingestion jobs caused worker pods to crash repeatedly with exit code 137 (OOMKilled) after processing 300,000 lines of a 5GB file.
- **Root Cause Forensics:**
  A developer constructed the `FlatFileItemReader` inside a `@Bean` without registering it as a Spring step-scoped bean or maintaining the `ItemStream` lifecycle:
  ```java
  // ANTI-PATTERN: Not step-scoped, missing ItemStream registration!
  @Bean
  public ItemReader<String> badFileReader() {
      FlatFileItemReader<String> reader = new FlatFileItemReader<>();
      reader.setResource(new FileSystemResource("/large_file.csv"));
      return reader;
  }
  ```
  1. The reader was not managed as an `ItemStream`. As a result, its internal buffered stream reader was never closed or garbage-collected.
  2. The chunk size was set to 50,000, forcing millions of String objects to accumulate in old generation heap memory.
- **The Permanent Fix:**
  1. Annotate reader with **`@StepScope`** to bind its lifecycle strictly to step execution.
  2. Tune chunk size to an optimal `1,000`.
  3. Ensure the reader implements `ItemStreamReader` so Spring Batch automatically manages file descriptors and buffer flushes.

---

## ⚖️ Spring Batch 5 Production Architecture Matrix

| Requirement / Pattern | High-Performance Production Syntax |
| :--- | :--- |
| **Keep DB Connection Closed During I/O** | `.faultTolerant().processorNonTransactional()` |
| **Paging Reader with Stable Sorting** | `JdbcPagingItemReader` + Unique `ORDER BY id ASC` |
| **Quarantine Skipped Records** | Custom `ItemSkipListener` writing to DLQ table |
| **High-Throughput Concurrency** | Partitioned Step via `Partitioner` + `ThreadPoolTaskExecutor` |
| **Dynamic Job Parameters** | `@StepScope` + `@Value("#{jobParameters['date']}")` |
| **Prevent Restart Collisions** | Non-identifying parameters: `.addString("uuid", id, false)` |

---
[🏠 Back to Home](README.md) | [📦 Spring Batch Master Guide](spring_batch.md) | [🍃 Spring Boot Master Guide](spring_master_guide.md)
