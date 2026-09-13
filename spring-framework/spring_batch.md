[🏠 Back to Home](README.md)

# 📦 Spring Batch 5+ & Spring Boot 3 Enterprise Processing Master Guide

A production-grade handbook for architecting high-throughput, fault-tolerant batch workloads, ETL pipelines, and massive data transformations using **Spring Batch 5.x**, **Spring Boot 3.x**, and **Java 17/21**. Covers chunk architecture, skip/retry policies, transaction boundaries, partitioning, and zero-downtime restartability.

---

## 📑 Table of Contents

### Track 1: Junior & Entry-Level Foundations

- [🌱 1. Real-World Mental Model (Amazon Warehouse Assembly Line)](#1-the-real-world-mental-model-the-amazon-warehouse-packing-assembly-line)
- [🧩 2. The 5 Core Building Blocks of Spring Batch](#2-the-5-core-building-blocks)
- [💻 3. Beginner Code Walkthrough: Spring Batch 5 Chunk Configuration](#3-beginner-code-walkthrough-spring-batch-5-chunk-configuration)
- [💥 4. What Happens When Things Break? (Skip vs Retry vs Fail)](#4-what-happens-when-things-break-skip-vs-retry-vs-fail)
- [⚠️ 5. Top 5 Beginner Mistakes in Production](#5-top-5-beginner-mistakes-in-production)
- [🎯 6. Top 10 Junior Interview Questions (With "ELI5" Answers)](#6-top-10-junior-interview-questions-with-explain-like-im-5-answers)

### Track 2: Advanced Architecture & Enterprise Pipelines

1. [⚙️ 1. Spring Batch 5 Core Architecture & Metadata Schema](#️-1-spring-batch-5-core-architecture--metadata-schema)
2. [🔄 2. Chunk-Oriented Processing vs Tasklet](#-2-chunk-oriented-processing-vs-tasklet)
3. [📖 3. Production Readers & Writers (FlatFile, JDBC, JPA, Kafka)](#-3-production-readers--writers-flatfile-jdbc-jpa-kafka)
4. [🛡️ 4. Fault Tolerance: Skip, Retry & Transaction Boundaries](#️-4-fault-tolerance-skip-retry--transaction-boundaries)
5. [👂 5. Lifecycle Interception with Listeners](#-5-lifecycle-interception-with-listeners)
6. [🚀 6. High-Throughput Scaling: Multi-Threading & Partitioning](#-6-high-throughput-scaling-multi-threading--partitioning)
7. [🏭 7. Production Scenarios & War Room Incident Forensics](#-7-production-scenarios--war-room-incident-forensics)
8. [⚖️ 8. Spring Batch 5 Master Annotation & API Cheat Sheet](#️-8-spring-batch-5-master-annotation--api-cheat-sheet)

# TRACK 1: THE JUNIOR & ENTRY-LEVEL FOUNDATIONS (ZERO-TO-HERO)

## 1. The Real-World Mental Model (The Amazon Warehouse Packing Assembly Line)

### What Is Batch Processing?
Imagine you are the manager of an Amazon fulfillment warehouse:
- During the day, individual customers buy items one-by-one (**Online Transaction Processing / OLTP**).
- At midnight, you have to generate invoices, calculate sales taxes, and sync 10,000,000 records with bank partners (**Batch Processing**).
- If you process 10,000,000 items one-by-one with individual database commits, it will take 3 days!
- If you try loading all 10,000,000 records into memory at the same second, your computer runs out of RAM and crashes with `OutOfMemoryError`.

---

### The Solution: Chunk-Oriented Processing
Instead of loading everything or processing one-by-one, you use an **Automated Conveyor Belt (Chunks)**:
1. **`ItemReader`:** Pick up 1 item from the input crate.
2. **`ItemProcessor`:** Inspect and polish 1 item.
3. Repeat steps 1 and 2 until a box is full (e.g. **Chunk Size = 100 items**).
4. **`ItemWriter`:** Seal the box and ship all 100 items to the truck in **one single database transaction commit**!
5. If the power cuts out at item 5,400, Spring Batch looks at its clipboard (**`JobRepository`**), skips the first 5,300 successfully committed items, and resumes right at item 5,301!

### Spring Batch Chunk Processing & Transaction Demarcation Architecture

| Processing Phase | Core Component | Granularity & Cardinality | Technical Operation & Database Boundary |
| :--- | :--- | :--- | :--- |
| **1. Job Orchestration** | `Job` / `JobLauncher` | Single Batch Run | Initiates batch execution; creates `JobExecution` and `JobInstance` records in `JobRepository` |
| **2. Step Execution** | `TaskletStep` / `StepBuilder` | Sequenced Batch Phase | Manages step lifecycle, restart counters, and transactional execution context |
| **3. Item Extraction** | `ItemReader<I>` | Item-by-item (`read()`) | Reads a single item per call from stream/cursor; returns `null` at EOF |
| **4. Item Transformation**| `ItemProcessor<I, O>` | Item-by-item (`process()`) | Validates, enriches, or filters items; returning `null` drops item from the chunk |
| **5. Chunk Aggregation** | `Chunk<O>` Accumulator | In-memory List (`N` items) | Loops steps 3 & 4 until chunk size interval (e.g., 100 or 500 records) is reached |
| **6. Transactional Write**| `ItemWriter<O>` | Chunk batch (`write(Chunk)`) | Flushes entire chunk in a single batch database operation via `PlatformTransactionManager` |
| **7. State Checkpoint** | `JobRepository` | ACID DB Commit | Commits chunk and persists step execution context checkpoint (`BATCH_STEP_EXECUTION`) to database |

---

## 2. The 5 Core Building Blocks

| Term | What It Means | Real-World Analogy |
| :--- | :--- | :--- |
| **`Job`** | The entire batch execution workflow composed of one or more steps. | The complete night shift at the warehouse. |
| **`Step`** | An isolated, independent phase of a job (Chunk-based or Tasklet). | A specific assembly line station. |
| **`ItemReader`** | Reads 1 record at a time. Returns `null` when data is exhausted. | The worker taking items out of the supply box. |
| **`ItemProcessor`** | Transforms, cleanses, or validates 1 record. Returning `null` filters it out. | The quality inspector stamping or rejecting items. |
| **`ItemWriter`** | Receives a List of chunk items and writes them all in 1 batch. | The loader stacking the completed pallet onto the truck. |
| **`JobRepository`** | The relational database tables storing execution state, commits, and skips. | The supervisor's logbook tracking progress for restartability. |

---

## 3. Beginner Code Walkthrough: Spring Batch 5 Chunk Configuration

```java
package com.example.batch.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

@Configuration
public class SimpleBatchConfig {

    // 1. Reader: Reads numbers from memory list
    @Bean
    public ItemReader<Integer> itemReader() {
        return new ListItemReader<>(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
    }

    // 2. Processor: Multiplies number by 10 (or filters odd numbers by returning null)
    @Bean
    public ItemProcessor<Integer, String> itemProcessor() {
        return item -> "Result: " + (item * 10);
    }

    // 3. Writer: Writes the batch chunk to console / DB
    @Bean
    public ItemWriter<String> itemWriter() {
        return chunk -> {
            System.out.println("📦 Committing Chunk of size " + chunk.size() + ": " + chunk.getItems());
        };
    }

    // 4. Step: Process in chunks of 3 items per transaction commit!
    @Bean
    public Step processNumbersStep(JobRepository jobRepository, 
                                  PlatformTransactionManager transactionManager) {
        return new StepBuilder("processNumbersStep", jobRepository)
            .<Integer, String>chunk(3, transactionManager) // Chunk size: 3
            .reader(itemReader())
            .processor(itemProcessor())
            .writer(itemWriter())
            .build();
    }

    // 5. Job: Execute the step
    @Bean
    public Job processNumbersJob(JobRepository jobRepository, Step processNumbersStep) {
        return new JobBuilder("processNumbersJob", jobRepository)
            .start(processNumbersStep)
            .build();
    }
}
```

---

## 4. What Happens When Things Break? (Skip vs Retry vs Fail)

When processing millions of records, bad records (e.g. corrupt CSV lines or missing phone numbers) are inevitable:
1. **Fail Fast (Default):** If item #4,500 throws an exception, the entire job aborts immediately and transaction rolls back.
2. **Skip Policy:** You tell Spring Batch: *"If you see a `NumberFormatException`, skip it, record it in the skip counter, and continue to item #4,501."* (e.g. `.faultTolerant().skip(NumberFormatException.class).skipLimit(10)`).
3. **Retry Policy:** If a network call times out, retry up to 3 times before failing or skipping.
4. **Restartability:** When a failed job is re-run with the same `JobParameters`, Spring Batch looks at `BATCH_STEP_EXECUTION` and automatically jumps straight to the last committed chunk offset!

---

## 5. Top 5 Beginner Mistakes in Production

1. **Chunk Size of 1:** Setting chunk size to 1 commits a database transaction for every single row. Inserting 100,000 records takes 30 minutes instead of 4 seconds! **Rule of thumb:** Chunk size should typically be between 100 and 1,000.
2. **Chunk Size of 1,000,000:** Setting chunk size too large holds database row locks for too long, starves other queries, and crashes the JVM with `OutOfMemoryError`.
3. **Using Non-Thread-Safe Readers in Multi-Threaded Steps:** Standard readers like `FlatFileItemReader` or `JdbcCursorItemReader` maintain internal state (`read()` incrementing current line). Using them across multiple threads causes race conditions and dropped rows! **Fix:** Use `SynchronizedItemStreamReader` or `JdbcPagingItemReader`.
4. **Not Knowing Returning `null` Filters Records:** If an `ItemProcessor` returns `null`, Spring Batch intentionally drops that item and does NOT pass it to the `ItemWriter`. Beginners often think the record was lost due to a bug.
5. **Re-Running a Completed Job with the Same Parameters:** Spring Batch enforces that a `JobInstance` that completed with status `COMPLETED` cannot be run again with the exact same parameters. You must pass a new identifying parameter (e.g. `System.currentTimeMillis()`) or use `RunIdIncrementer`.

---

## 6. Top 10 Junior Interview Questions (With "Explain Like I'm 5" Answers)

### Q1: What is Chunk-Oriented Processing in Spring Batch?
- **ELI5 Answer:** *"Instead of carrying 1,000 bricks to the truck one-by-one (slow) or trying to lift all 1,000 at once (breaks your back), you put 100 bricks in a wheelbarrow, walk to the truck, dump them in, and go back for the next 100."*
- **Technical Answer:** *"Chunk-oriented processing reads data item-by-item via `ItemReader`, passes each item to `ItemProcessor`, and aggregates processed items in memory until reaching the chunk commit interval. It then hands the entire chunk list to `ItemWriter` to be persisted within a single transaction boundary."*

### Q2: What is the difference between a Tasklet and a Chunk step?
- **ELI5 Answer:** *"A Tasklet is doing one simple chore (like deleting an old file or sending an email). A Chunk step is an assembly line processing millions of items in boxes."*
- **Technical Answer:** *"A `Tasklet` executes a single method (`execute()`) once to perform a simple, discrete task (e.g. file cleanup, stored procedure call). A `Chunk` step is designed for high-volume streaming data pipelines involving repeated read-process-write cycles with pagination and transaction management."*

### Q3: What is the purpose of the `JobRepository`?
- **ELI5 Answer:** *"The teacher's gradebook that records which students finished their homework, who was absent, and where to start reading tomorrow if the fire alarm goes off."*
- **Technical Answer:** *"`JobRepository` provides CRUD operations for Spring Batch metadata tables (`BATCH_JOB_INSTANCE`, `BATCH_JOB_EXECUTION`, `BATCH_STEP_EXECUTION`). It persists job execution status, commit counts, skip counts, timestamps, and execution context checkpoints for restartability."*

### Q4: What is the difference between `JobInstance` and `JobExecution`?
- **ELI5 Answer:** *"`JobInstance` is the movie script ('End of Month Payroll'). `JobExecution` is each time the movie is played in the theater ('Playing on March 31', 'Playing on April 30', or 'Retry after projector broke')."*
- **Technical Answer:** *"A `JobInstance` represents a logical job run identified by unique `JobParameters`. A `JobExecution` represents an individual physical attempt to run that instance. If a `JobExecution` fails, running the job again creates a new `JobExecution` tied to the same `JobInstance` until it succeeds."*

### Q5: How does Spring Batch handle failures and restartability?
- **ELI5 Answer:** *"If you are reading a 500-page book and drop it on page 200, you don't start over on page 1; you pick it up and resume reading from page 200."*
- **Technical Answer:** *"Spring Batch saves reader offsets and step state in `ExecutionContext` within `BATCH_STEP_EXECUTION_CONTEXT` at each chunk commit. When a failed job is restarted with identical parameters, it reads the persisted offset and resumes processing from the last committed chunk."*

### Q6: What is the Skip Policy in Spring Batch?
- **ELI5 Answer:** *"If you find one rotten apple in a basket, you throw it in the compost bin and keep washing the rest of the good apples instead of throwing away the entire basket."*
- **Technical Answer:** *"Skip policies allow steps to tolerate specific exceptions (e.g. `FlatFileParseException`) up to a configured `skipLimit`. Instead of rolling back the step, the offending record is discarded, a `SkipListener` is notified, and processing continues."*

### Q7: What is the difference between `Cursor` and `Paging` ItemReaders in JDBC?
- **ELI5 Answer:** *"`Cursor` keeps a door open to the warehouse and carries items out one-by-one. `Paging` runs in, grabs 500 items, closes the door, and runs back in later for the next 500."*
- **Technical Answer:** *"`JdbcCursorItemReader` opens a single streaming database cursor over a long-lived connection, reading rows via `ResultSet.next()`. `JdbcPagingItemReader` executes separate SQL queries using `LIMIT / OFFSET` pagination, closing database connections between pages, making it safer for multi-threaded and long-running steps."*

### Q8: What does returning `null` from an `ItemProcessor` do?
- **ELI5 Answer:** *"Throwing away junk mail before putting the important letters into the mailbox."*
- **Technical Answer:** *"Returning `null` acts as a record filter. Spring Batch detects `null` and omits that item from the current chunk, meaning it will never be sent to the `ItemWriter`."*

### Q9: How can you scale a Spring Batch job to handle 50,000,000 records?
- **ELI5 Answer:** *"Instead of 1 worker packing boxes, you hire 10 workers, divide the warehouse into 10 sections, and let each worker pack their own section simultaneously."*
- **Technical Answer:** *"Spring Batch provides multiple scaling options: (1) Multi-threaded Step (shared reader/writer with synchronized lock), (2) Partitioning (master step splits data into independent subsets processed concurrently by worker steps), (3) Remote Chunking (master reads, workers process over Kafka/JMS), and (4) Parallel Steps."*

### Q10: What is a `TaskletStep` and when should you use it?
- **ELI5 Answer:** *"A single one-time errand like 'turn off the lights' or 'zip this folder'."*
- **Technical Answer:** *"A `TaskletStep` executes a single `Tasklet.execute()` method returning `RepeatStatus.FINISHED`. It is used for operations that do not fit chunk-based iteration, such as unzipping an incoming archive, running a table cleanup `TRUNCATE` script, or notifying Slack after job completion."*

---

# TRACK 2: ADVANCED ARCHITECTURE & MASTER BATCH FEATURE CATALOG

## Master Spring Batch Architecture Decision Matrix

| Architectural Pattern | Core Mechanism | Memory Footprint | Concurrency Model | Ideal Production Use Case | Anti-Pattern For |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Chunk Processing** | Reader $\to$ Processor $\to$ Writer | $O(\text{chunk size})$ constant | Single Thread (or worker pool) | Millions of CSV, database or JSON rows | One-off discrete task (e.g. file delete) |
| **Tasklet Step** | Single `execute()` lambda | Depends on code | Single Thread | Staging folder cleanup, table truncate | Iterating 1,000,000 DB records |
| **Cursor Reader** | Persistent DB socket cursor | Low ($O(1)$ stream) | Single Thread ONLY (Not thread-safe)| Ordered sequential processing | Multi-threaded concurrent steps |
| **Paging Reader** | SQL `LIMIT/OFFSET` or Keyset | $O(\text{page size})$ | Thread-Safe | Concurrent multi-threaded steps | Massive offsets in MySQL (use keyset) |
| **Multi-Threaded Step**| Worker ThreadPoolExecutor | Medium | Concurrent workers on 1 JVM | High-CPU transformations on 1 box | State-tracking non-thread-safe readers |
| **Partitioned Step** | Master splits into $N$ slices | Low per slice | Multi-thread or Multi-JVM | 100M+ records sharded by ID range | Small datasets under 10,000 records |
| **Skip Policy** | Catches corrupt lines | Minimal | Handled in transaction loop | Dirty CSV files with malformed rows | Systemic database outages |
| **Retry Policy** | Re-executes chunk on lock | Minimal | Backoff wait loop | Transient DB deadlocks, network blips| Validation errors (e.g. invalid email) |
| **Composite Processor**| Pipeline of processors | Low | Chained sequential | Clean separation: Validate $\to$ Enrich | Heavy blocking network calls |
| **ExecutionContext**| DB serialized state map | Tiny ($<10\text{ KB}$) | Per Step / Job Execution | Checkpointing cursor for restarts | Storing massive entity lists |

---

## 2.1 Chunk-Oriented Processing & Transaction Demarcation

1. **Architectural Overview & Purpose**:
   - Instead of reading an entire dataset into memory or executing individual database commits per record, Chunk-Oriented Processing streams data in discrete units of size $N$ (e.g. 500 items). It provides constant $O(\text{chunk size})$ heap usage and commits transactions at exact chunk boundaries.

2. **Underlying Algorithm & Execution Loop**:
   ```text
   [ Open Transaction ]
   Loop chunk_size times:
       item = ItemReader.read()
       if item == null: break (End of dataset)
       transformed = ItemProcessor.process(item)
       if transformed != null: chunkList.add(transformed)
   ItemWriter.write(chunkList)
   [ Commit Transaction & Checkpoint ExecutionContext ]
   ```
   - If an unhandled exception throws during the write, the entire transaction of 500 items rolls back automatically.

3. **Full Syntax & Method Signatures**:
   ```java
   // Spring Batch 5 StepBuilder
   public <I, O> SimpleStepBuilder<I, O> chunk(int chunkSize, PlatformTransactionManager transactionManager);
   ```

4. **Concrete Code Examples with Sample Values & Expected Results**:
   ```java
   @Bean
   public Step orderProcessingStep(JobRepository jobRepository,
                                   PlatformTransactionManager txManager,
                                   ItemReader<RawOrder> reader,
                                   ItemProcessor<RawOrder, ProcessedOrder> processor,
                                   ItemWriter<ProcessedOrder> writer) {
       return new StepBuilder("orderProcessingStep", jobRepository)
           .<RawOrder, ProcessedOrder>chunk(100, txManager)
           .reader(reader)
           .processor(processor)
           .writer(writer)
           .build();
   }
   ```
   - **Execution Log Output**:
     ```text
     INFO  o.s.b.c.s.c.TaskletStep - Step: [orderProcessingStep] executed in 450ms
     INFO  o.s.b.c.s.c.TaskletStep - Read: 100, Filtered: 5, Written: 95, Commit Count: 1
     ```

5. **Pros & Cons**:
   - **Pros**: Zero memory explosion regardless of dataset size; automatic checkpointing.
   - **Cons**: Choosing the wrong chunk size causes performance bottlenecks (too small = excessive DB commits; too large = long lock times).

6. **How It Breaks: Top Beginner Mistakes**:
   - **Modifying Entities in Memory Without Saving in Writer**:
     Relying on JPA dirty-checking across chunk boundaries without explicit session management causes uncommitted detached entity state.

7. **Tricky Interview Questions & Gotchas**:
   - **Q: What happens if `ItemProcessor` returns `null`?**
     - *Answer*: Returning `null` explicitly signals to Spring Batch that the record should be **filtered out**. The item is silently dropped from the current chunk and will NOT be passed to the `ItemWriter`.

---

## 2.2 Paging vs Cursor Database Readers

1. **Architectural Overview & Purpose**:
   - Extracting millions of rows from a relational database requires streaming:
     - `JdbcCursorItemReader`: Maintains a single open database cursor connection, streaming records over the network buffer.
     - `JdbcPagingItemReader`: Executes paginated queries (`LIMIT ? OFFSET ?` or keyset pagination) pulling pages into memory.

2. **Underlying Mechanics**:
   - **Cursor Reader**: Extremely fast ($O(1)$ stream). **Strictly single-threaded and NOT thread-safe**! The database connection must remain open throughout the entire step.
   - **Paging Reader**: Completely **thread-safe**. Multiple threads can fetch different pages concurrently.

3. **Production Blueprint (Thread-Safe Keyset Paging Reader)**:
   ```java
   @Bean
   public JdbcPagingItemReader<CustomerDto> customerPagingReader(DataSource dataSource) {
       PostgresPagingQueryProvider queryProvider = new PostgresPagingQueryProvider();
       queryProvider.setSelectClause("customer_id, email, balance");
       queryProvider.setFromClause("FROM customers");
       queryProvider.setSortKeys(Map.of("customer_id", Order.ASCENDING));

       return new JdbcPagingItemReaderBuilder<CustomerDto>()
           .name("customerPagingReader")
           .dataSource(dataSource)
           .pageSize(500)
           .queryProvider(queryProvider)
           .rowMapper((rs, rowNum) -> new CustomerDto(
               rs.getLong("customer_id"),
               rs.getString("email"),
               rs.getDouble("balance")
           ))
           .saveState(true)
           .build();
   }
   ```

---

## 2.3 Fault Tolerance: Skip & Retry Policies

1. **Architectural Overview & Purpose**:
   - In a 10,000,000-record batch, 3 corrupt CSV lines or a 500ms database deadlock must not abort the entire 4-hour job. Spring Batch provides granular **Skip** (discard bad record and continue) and **Retry** (re-execute transient failure).

2. **Skip vs Retry Invariant**:
   - **Skip**: Used for deterministic, non-retryable bad data (e.g. `FlatFileParseException`, `NumberFormatException`).
   - **Retry**: Used for non-deterministic, transient infrastructure failures (e.g. `CannotAcquireLockException`, `TransientDataAccessException`).

3. **Production Fault-Tolerant Step Blueprint**:
   ```java
   @Bean
   public Step resilientStep(JobRepository jobRepository,
                            PlatformTransactionManager txManager,
                            ItemReader<CustomerInput> reader,
                            ItemWriter<CustomerInput> writer) {
       return new StepBuilder("resilientStep", jobRepository)
           .<CustomerInput, CustomerInput>chunk(500, txManager)
           .reader(reader)
           .writer(writer)
           .faultTolerant()
           // Skip configuration
           .skip(FlatFileParseException.class)
           .skip(IllegalArgumentException.class)
           .skipLimit(50) // Allow up to 50 bad lines
           // Retry configuration
           .retry(CannotAcquireLockException.class)
           .retryLimit(3) // Retry up to 3 times on lock acquisition
           .backOffPolicy(new ExponentialBackOffPolicy() {{
               setInitialInterval(1000L);
               setMultiplier(2.0);
           }})
           .build();
   }
   ```

---

## 2.4 JobRepository State Machine & Metadata Schema

1. **The 6 Core Tables**:
   - `BATCH_JOB_INSTANCE`: Logical job identity. Keyed by Job Name + identifying `JobParameters`.
   - `BATCH_JOB_EXECUTION`: Physical execution attempt. Tracks status (`STARTING`, `STARTED`, `COMPLETED`, `FAILED`).
   - `BATCH_JOB_EXECUTION_PARAMS`: Key-value pairs passed at job launch.
   - `BATCH_STEP_EXECUTION`: Metric telemetry: `READ_COUNT`, `WRITE_COUNT`, `COMMIT_COUNT`, `ROLLBACK_COUNT`, `FILTER_COUNT`, `SKIP_COUNT`.
   - `BATCH_STEP_EXECUTION_CONTEXT`: Serialized JSON/Base64 key-value machine checkpoints for restartability.

2. **The Restartability Guard**:
   - If a job fails at Step 2 after committing 100,000 rows in Step 1, re-launching the job with identical parameters will **skip Step 1 entirely** and resume Step 2 from the exact offset recorded in `BATCH_STEP_EXECUTION_CONTEXT`!

---

## 2.5 ExecutionContext Checkpoints & PromotionListener

1. **Architectural Overview**:
   - `ExecutionContext` is a persistent state dictionary stored in PostgreSQL. It allows readers and steps to persist bookmarks (e.g. last processed line number or database primary key).

2. **Sample Usage**:
   ```java
   // In ItemReader or StepExecutionListener:
   stepExecution.getExecutionContext().putLong("lastCommittedId", 450123L);

   // On job restart after crash, the reader retrieves this checkpoint:
   long startId = stepExecution.getExecutionContext().getLong("lastCommittedId", 0L);
   ```

---

## 2.6 Master-Worker Partitioning (Divide & Conquer)

1. **Architectural Overview & Purpose**:
   - When processing 50,000,000 rows, a single process cannot finish within nightly batch windows. Partitioning divides the dataset into independent slices and executes them concurrently across worker threads or remote worker nodes.

2. **Production Range Partitioner**:
   ```java
   public class CustomerIdRangePartitioner implements Partitioner {
       @Override
       public Map<String, ExecutionContext> partition(int gridSize) {
           Map<String, ExecutionContext> result = new HashMap<>();
           long minId = 1;
           long maxId = 1000000;
           long targetSize = (maxId - minId) / gridSize + 1;

           long start = minId;
           long end = start + targetSize - 1;

           for (int i = 0; i < gridSize; i++) {
               ExecutionContext context = new ExecutionContext();
               context.putLong("minId", start);
               context.putLong("maxId", Math.min(end, maxId));
               result.put("partition_" + i, context);

               start += targetSize;
               end += targetSize;
           }
           return result;
       }
   }
   ```

3. **Step Manager Configuration**:
   ```java
   @Bean
   public Step managerStep(JobRepository jobRepository, Step workerStep) {
       return new StepBuilder("managerStep", jobRepository)
           .partitioner("workerStep", new CustomerIdRangePartitioner())
           .step(workerStep)
           .gridSize(8) // 8 parallel worker threads
           .taskExecutor(new ThreadPoolTaskExecutor() {{
               setCorePoolSize(8);
               setMaxPoolSize(16);
               initialize();
           }})
           .build();
   }
   ```

---

## 2.7 Composite Processors & Validation Pipelines

1. **Architectural Overview**:
   - Enables chaining multiple independent single-responsibility processors (e.g., Step 1: Validation $\to$ Step 2: Currency Conversion $\to$ Step 3: Fraud Scoring).

2. **Production Blueprint**:
   ```java
   @Bean
   public ItemProcessor<RawTransaction, EnrichedTransaction> compositeProcessor() {
       CompositeItemProcessor<RawTransaction, EnrichedTransaction> composite = new CompositeItemProcessor<>();
       composite.setDelegates(List.of(
           new ValidationProcessor(),
           new CurrencyNormalizationProcessor(),
           new FraudScoringProcessor()
       ));
       return composite;
   }
   ```

---

## 2.8 FlatFileItemReader & Streaming Ingestion

1. **Production Delimited CSV Configuration**:
   ```java
   @Bean
   public FlatFileItemReader<InvoiceDto> invoiceCsvReader() {
       return new FlatFileItemReaderBuilder<InvoiceDto>()
           .name("invoiceCsvReader")
           .resource(new FileSystemResource("inbound/invoices.csv"))
           .linesToSkip(1) // Skip header
           .delimited()
           .delimiter(",")
           .names("invoiceId", "customerId", "amount", "dueDate")
           .targetType(InvoiceDto.class)
           .saveState(true) // Enables checkpoint restart from line number!
           .build();
   }
   ```

---

## 2.9 Multi-Threaded Steps & SynchronizedItemStreamReader

1. **The Concurrency Trap**:
   - `FlatFileItemReader` and `JdbcCursorItemReader` maintain internal state (`lineCount`, cursor index). If 8 threads invoke `read()` concurrently on the same reader, lines are skipped, read out of order, or cause index corruption!

2. **The Solution**: Wrap in `SynchronizedItemStreamReader`:
   ```java
   @Bean
   public SynchronizedItemStreamReader<InvoiceDto> synchronizedReader(ItemReader<InvoiceDto> rawReader) {
       SynchronizedItemStreamReader<InvoiceDto> reader = new SynchronizedItemStreamReader<>();
       reader.setDelegate(rawReader);
       return reader;
   }
   ```

---

## 2.10 Spring Batch 5 & Boot 3 Migration Architecture

1. **What Changed in Spring Batch 5**:
   - `@EnableBatchProcessing` is NO LONGER required in Spring Boot 3 (auto-configures `JobLauncher`, `JobRepository`, `TransactionManager`).
   - `JobBuilderFactory` and `StepBuilderFactory` are completely removed. Always use explicit constructors:
     ```java
     new JobBuilder("jobName", jobRepository);
     new StepBuilder("stepName", jobRepository);
     ```
   - Native support for **Java 21 Virtual Threads**: pass `new VirtualThreadTaskExecutor()` to multi-threaded steps for massive I/O concurrency with zero OS thread bloat.

---

## 🏭 7. Production Scenarios & War Room Incident Forensics

### Scenario 1: Out Of Memory (OOM) via Unbounded Hibernate / JPA Caching
- **The Symptom:** Batch process crashes with `java.lang.OutOfMemoryError: Java heap space` after processing 150,000 entities.
- **Root Cause:** Hibernate's First-Level Session cache stores every read/written entity in memory. Even though chunks commit to the database, the `EntityManager` session retains object references until explicitly cleared.
- **The Fix:** Inject a custom `ItemWriter` or `ChunkListener` that invokes `entityManager.clear()`, or use `JpaItemWriter` which clears the session automatically after each chunk write.

### Scenario 2: Duplicate Execution Exception (`JobInstanceAlreadyCompleteException`)
- **The Symptom:** Triggering a daily batch fails with: `A job instance already exists and is complete for parameters={date=2026-09-03}`.
- **Root Cause:** Spring Batch prevents re-running a completed job instance with identical identifying parameters to enforce idempotency.
- **The Fix:**
  1. If re-running is intended, pass a unique non-identifying parameter:
  ```java
  JobParameters params = new JobParametersBuilder()
      .addString("date", "2026-09-03", true) // identifying
      .addLong("run.id", System.currentTimeMillis(), false) // non-identifying
      .toJobParameters();
  jobLauncher.run(job, params);
  ```

---

## ⚖️ 8. Spring Batch 5 Master Annotation & API Cheat Sheet

| Task / Concept | Spring Batch 5+ API Construct |
| :--- | :--- |
| **Declare Job** | `new JobBuilder("jobName", jobRepository).start(step1).next(step2).build()` |
| **Declare Chunk Step** | `new StepBuilder("stepName", jobRepository).<I, O>chunk(chunkSize, txManager)...` |
| **Fault Tolerance** | `.faultTolerant().skip(DataAccessException.class).skipLimit(10)` |
| **Retry on Lock** | `.retry(CannotAcquireLockException.class).retryLimit(3)` |
| **Prevent Restart** | `new JobBuilder(...).preventRestart().start(...)` |
| **Non-Identifying Param**| `new JobParametersBuilder().addString("token", uuid, false)` |
| **Async Step** | `AsyncItemProcessor<I, O>` + `AsyncItemWriter<O>` |
| **Step Execution Context**| `stepExecution.getExecutionContext().put("lastProcessedIndex", index)` |

---
[🏠 Back to Home](README.md)
