[🏠 Back to Home](README.md) | [🍃 Spring Boot Master Guide](spring_master_guide.md) | [🏛️ Spring Data JPA Guide](spring_data_jpa.md) | [📦 Jackson Guide](jackson_master_guide.md) | [🔐 Cryptography Guide](java_spring_cryptography_master_guide.md)

# 🍃 MongoDB Polyglot Architecture & Developer Master Guide (Java & Node.js)

A production-grade distributed database engineering handbook covering BSON serialization, WiredTiger storage engine internals, aggregation pipelines, replica set consensus, horizontal sharding, multi-document ACID transactions, Change Streams, **Spring Data MongoDB (Java)**, and **Mongoose / Native Driver (Node.js)**.

---

## 📑 Table of Contents

1. [🧠 Zero-to-Hero Mental Model: The Flexible Manilla Folder vs The Rigid Ledger](#-the-flexible-manilla-folder-vs-the-rigid-ledger)
2. [🛠️ Prerequisites & Foundational Knowledge](#️-prerequisites--foundational-knowledge)
3. [📦 Track 1: The Junior & Entry-Level Foundations](#track-1-the-junior--entry-level-foundations-zero-to-hero)
4. [🚀 Track 2: Master MongoDB Polyglot Feature Catalog](#track-2-master-mongodb-polyglot-feature-catalog)
5. [🏗️ Track 3: WiredTiger Storage Engine & Driver Internals](#track-3-wiredtiger-storage-engine--driver-internals)
6. [⚙️ Track 4: Production Engineering, Sizing & Sharding Operations](#track-4-production-engineering-sizing--sharding-operations)
7. [🚨 Track 5: War Room Post-Mortems & Root Cause Analysis (RCAs)](#track-5-war-room-post-mortems--root-cause-analysis-rcas)
8. [🎓 Track 6: Crack-The-Interview Question Bank (Senior & Staff+ Level)](#track-6-crack-the-interview-question-bank-senior--staff-level)
9. [⚖️ MongoDB Polyglot Master Cheat Sheet (Java vs Node.js)](#️-mongodb-polyglot-master-cheat-sheet)

---

## 🛠️ Prerequisites & Foundational Knowledge

Before writing code in Java or Node.js, engineers must understand MongoDB's distributed document architecture:

### 1. The Document Model vs Relational Normalization
- **Relational (SQL)**: Deconstructs data into 3rd Normal Form across multiple rigid tables joined at query time (`users`, `user_addresses`, `phone_numbers`).
- **Document (MongoDB)**: Data that is **accessed together is stored together**. A document is a self-contained, flexible hierarchical tree structure.
- **The Hard Limit**: A single BSON document has a strict maximum size of **16 Megabytes**. For large binary files (videos, PDFs), use **GridFS**.

### 2. BSON (Binary JSON) & The `ObjectId`
- **BSON Advantages**: Adds traversal length headers and rich data types not found in standard JSON: `Int32`, `Int64`, `Decimal128` (arbitrary-precision currency), `ISODate`, `Binary`, and `ObjectId`.
- **The Anatomy of an `ObjectId` (12 Bytes / 24 Hex Characters)**:
  - **Bytes 0–3 (4 bytes)**: 32-bit Unix epoch timestamp (seconds since Jan 1, 1970).
  - **Bytes 4–8 (5 bytes)**: Random value generated once per process (machine + process identity).
  - **Bytes 9–11 (3 bytes)**: Monotonically incrementing counter, initialized to a random value.
  - *Key Takeaway*: Sorting by `_id` automatically sorts documents chronologically!

### 3. WiredTiger Storage Engine Mechanics
- **In-Memory Cache**: WiredTiger dedicates $\approx 50\%$ of physical RAM minus 1 GB to its uncompressed working cache.
- **Lock-Free Concurrency**: Uses hazard pointers and optimistic concurrency control instead of table-level locks.
- **Checkpoints**: Every 60 seconds (or 2GB of dirty data), WiredTiger flushes dirty cache pages to disk in a consistent snapshot.
- **Write-Ahead Journaling (WAL)**: Transactions are written to an append-only disk journal every 100ms, providing crash resilience between checkpoints.

### 4. Replica Set Consensus & Write/Read Concerns
- **Write Concern (`w`)**:
  - `w: 1` (Default): Acknowledged as soon as the Primary writes to memory. Vulnerable to rollback if the Primary crashes before replicating.
  - `w: "majority"`: Acknowledged only after a majority of voting replica set nodes commit the write to their journal. Immune to rollback.
  - `j: true`: Forces an immediate sync to the disk journal before acknowledging.
- **Read Concern**:
  - `local`: Returns the node's most recent data without consensus validation.
  - `majority`: Returns data committed by a majority of replica set nodes (guaranteed never rolled back).

---

# TRACK 1: THE JUNIOR & ENTRY-LEVEL FOUNDATIONS (ZERO-TO-HERO)

## 1. The Real-World Mental Model (The Flexible Manilla Folder)

- **SQL Database**: A rigid spreadsheet where every row must have the exact same columns. If you add "Twitter Handle" for one person, you must allocate an empty cell for all 10,000,000 customers.
- **MongoDB**: A filing cabinet containing **Manilla Folders (Documents)** inside **Drawers (Collections)**:
  - Alice's folder contains her contact card and two nested business cards (**Embedded Documents**).
  - Bob's folder contains his contact card and a medical history note.
  - No database migration or `ALTER TABLE` is required to store different attributes!

```
+-----------------------------------------------------------------------------------+
| SQL Table (Users)                     │ MongoDB Collection (users)                |
+---------------------------------------+-------------------------------------------+
| id | name  | email        | age | ... │ {                                         |
|----+-------+--------------+-----+ ... │   "_id": ObjectId("65e..."),              |
| 1  | Alice | a@corp.com   | 29  | ... │   "name": "Alice",                        |
| 2  | Bob   | NULL         | 34  | ... │   "email": "a@corp.com",                  |
+---------------------------------------+   "addresses": [                          |
| SQL Table (Addresses)                 │     { "city": "NYC", "zip": "10001" }     |
| user_id | city | zip                  │   ]                                       |
|---------+------+------                │ }                                         |
+-----------------------------------------------------------------------------------+
```

---

## 2. The 5 Core Building Blocks

| Term | Relational (SQL) Equivalent | Definition |
| :--- | :--- | :--- |
| **`Database`** | Database / Schema | Physical container grouping related collections. |
| **`Collection`** | Table | Group of documents; has dynamic schema with optional JSON Schema validation. |
| **`Document`** | Row / Record | Single BSON data record; maximum 16MB. |
| **`Field`** | Column | Key-value pair within a document. |
| **`_id`** | Primary Key | Mandatory unique identifier (default `ObjectId`). |

---

## 3. Beginner Code Walkthrough: Clean CRUD in Java & Node.js

### 3.1 Java Spring Data MongoDB (`MongoTemplate`)
```java
package com.example.mongo.service;

import com.example.mongo.model.Customer;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerMongoService {

    private final MongoTemplate mongoTemplate;

    public CustomerMongoService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public Customer createCustomer(Customer customer) {
        return mongoTemplate.save(customer); // Inserts or updates
    }

    public Customer findByEmail(String email) {
        Query query = new Query(Criteria.where("email").is(email));
        return mongoTemplate.findOne(query, Customer.class);
    }

    public long updateTier(String email, String newTier) {
        Query query = new Query(Criteria.where("email").is(email));
        Update update = new Update().set("tier", newTier).currentDate("updatedAt");
        return mongoTemplate.updateFirst(query, update, Customer.class).getModifiedCount();
    }
}
```

### 3.2 Node.js with Mongoose
```javascript
// models/Customer.js
const mongoose = require('mongoose');

const customerSchema = new mongoose.Schema({
  name: { type: String, required: true, trim: true },
  email: { type: String, required: true, unique: true, lowercase: true, index: true },
  tier: { type: String, enum: ['FREE', 'PREMIUM', 'ENTERPRISE'], default: 'FREE' },
  addresses: [{
    city: String,
    zip: String
  }]
}, { timestamps: true });

module.exports = mongoose.model('Customer', customerSchema);
```

```javascript
// services/customerService.js
const Customer = require('../models/Customer');

async function createCustomer(data) {
  return await Customer.create(data);
}

async function findByEmail(email) {
  return await Customer.findOne({ email }).lean(); // .lean() skips Mongoose wrapper overhead!
}

async function updateTier(email, newTier) {
  return await Customer.updateOne(
    { email },
    { $set: { tier: newTier } }
  );
}

module.exports = { createCustomer, findByEmail, updateTier };
```

---

## 4. Top 10 Junior MongoDB Interview Questions

### Q1: What is the 16MB document limit and how do you work around it?
- **ELI5 Answer:** *"An envelope can only hold 50 sheets of paper. If you need to mail an entire dictionary, you split it into separate envelopes or put it in a parcel box."*
- **Technical Answer:** *"MongoDB caps single documents at 16MB to prevent uncompressed documents from consuming excessive WiredTiger cache and choking network bandwidth. If storing files or unbounded arrays (e.g. millions of sensor logs), use **GridFS** (which chunks files into 255KB chunks across `fs.files` and `fs.chunks`) or the **Bucket Pattern**."*

### Q2: What is the difference between Embedding and Referencing?
- **ELI5 Answer:** *"Embedding is taping a receipt directly inside your journal. Referencing is writing down the receipt's tracking number so you can find it in another binder."*
- **Technical Answer:** *"Embedding stores child objects directly inside the parent document (1-to-Few), allowing atomic, single-query reads. Referencing stores the `_id` of child documents in a separate collection (1-to-Many / Many-to-Many), requiring `$lookup` joins or secondary queries."*

### Q3: How do you perform a JOIN in MongoDB?
- **ELI5 Answer:** *"Using the `$lookup` magnifying glass to peek into another drawer and pull matching folders into your report."*
- **Technical Answer:** *"MongoDB performs left outer joins using the `$lookup` aggregation stage, matching a local field to a foreign collection's field: `{$lookup: {from: 'orders', localField: '_id', foreignField: 'customerId', as: 'customerOrders'}}`."*

### Q4: Why is sorting by `_id` faster than sorting by `createdAt`?
- **ELI5 Answer:** *"The serial number already has the exact second of production stamped into the first four numbers, so the factory can sort without checking the date sticker."*
- **Technical Answer:** *"The first 4 bytes of a default 12-byte BSON `ObjectId` encode the Unix timestamp. Furthermore, the `_id` field has a mandatory unique clustered index created automatically on every collection."*

### Q5: What is the ESR rule for compound index creation?
- **ELI5 Answer:** *"Equal first, Sorted second, Ranged last (E-S-R)."*
- **Technical Answer:** *"The ESR rule dictates optimal index key ordering: (1) **Equality** tests first (`status: "ACTIVE"`), (2) **Sort** fields second (`orderDate: -1`), and (3) **Range** filters last (`amount: {$gt: 100}`). Violating ESR causes expensive in-memory sort steps."*

### Q6: What does `.lean()` do in Mongoose?
- **ELI5 Answer:** *"Ordering takeaway food in plain paper bags instead of bringing out the fine restaurant porcelain dishes."*
- **Technical Answer:** *"By default, Mongoose wraps query results in full Mongoose Document instances complete with change-tracking, virtuals, and middleware hooks. Calling `.lean()` returns high-performance plain JavaScript objects (POJOs), reducing CPU execution time and memory allocation by 70%+."*

### Q7: What is a Tailable Cursor?
- **ELI5 Answer:** *"A walkie-talkie channel that stays open so you hear new words the moment someone speaks into it."*
- **Technical Answer:** *"A tailable cursor remains open after reaching the end of data on a **Capped Collection** (fixed-size circular buffer). As new documents are inserted, the cursor streams them immediately to the client without polling (analogous to `tail -f` in Unix)."*

### Q8: What happens during a Replica Set Primary election?
- **ELI5 Answer:** *"When the captain faints, the crew votes for the crewmate with the most up-to-date logbook to become the new captain."*
- **Technical Answer:** *"If Secondaries stop receiving heartbeats from the Primary for $>10\text{ seconds}$, an election is called using the Raft consensus protocol. Secondaries vote for the candidate with the highest election priority and most up-to-date Oplog (`local.oplog.rs`). The process completes within 3 to 12 seconds."*

### Q9: What is the difference between `w: 1` and `w: "majority"`?
- **ELI5 Answer:** *"`w: 1` is one person shouting 'Got it!'. `w: majority` is waiting until more than half the team nods their head before continuing."*
- **Technical Answer:** *"`w: 1` acknowledges the write as soon as the standalone Primary accepts it into memory. `w: "majority"` waits until a majority of voting replica set members write the data to their journals, eliminating data rollback risks during network partitions."*

### Q10: How does MongoDB support ACID transactions?
- **ELI5 Answer:** *"Signing a contract in a private room where you can change multiple folders, and if anything goes wrong, you tear up the contract and nothing changes."*
- **Technical Answer:** *"MongoDB supports multi-document ACID transactions via `ClientSession`. Using WiredTiger snapshot isolation, all read and write operations execute within a snapshot view. Transactions coordinate across replica sets and sharded clusters using a Two-Phase Commit (2PC) protocol."*

---

# TRACK 2: MASTER MONGODB POLYGLOT FEATURE CATALOG

## Master MongoDB Feature Matrix

| Feature / Pattern | Java Spring Data Mongo | Node.js (Mongoose / Driver) | Performance / Resource Profile | Production Sweet Spot |
| :--- | :--- | :--- | :--- | :--- |
| **Document CRUD** | `MongoTemplate` / `MongoRepository` | `Model.create()` / `Model.find().lean()` | High (Microsecond BSON parsing) | Core application entities |
| **Aggregation Pipeline** | `Aggregation.newAggregation(...)` | `Model.aggregate([...])` | Memory-bound (100MB RAM limit) | Analytics, reports, faceted search |
| **ACID Transactions** | `MongoTransactionManager` / `@Transactional`| `session.withTransaction(async () => ...)` | Incurs snapshot lock overhead | Financial transfers, inventory bookings |
| **Change Streams** | `MessageListenerContainer` / Reactive flux | `Model.watch()` / `collection.watch()` | Low (Taps into replication Oplog) | Cache invalidation, event-driven webhooks |
| **Reactive Streaming** | `ReactiveMongoRepository` (Project Reactor) | Native Driver Async Iterators | Zero-blocking, high thread efficiency | Real-time dashboards, IoT ingest |
| **Bulk Operations** | `mongoTemplate.bulkOps()` | `Model.bulkWrite([...])` | 1 network round-trip for 1000s of writes | High-throughput batch ingestion |

---

## 2.1 Advanced Document Modeling: Embedding vs Referencing & The Bucket Pattern

```
EMBEDDING (1-to-Few)
{
  "_id": ObjectId("..."),
  "orderNumber": "ORD-99",
  "items": [                                  <-- Embedded: 1 Query retrieves everything
    { "sku": "IPHONE", "qty": 1, "price": 999 }
  ]
}

REFERENCING (1-to-Millions)
User Document:  { "_id": ObjectId("U1"), "name": "Alice" }
Log Document:   { "_id": ObjectId("L1"), "userId": ObjectId("U1"), "action": "LOGIN" }

THE BUCKET PATTERN (Time-Series Data)
{
  "_id": ObjectId("..."),
  "sensorId": "SENSOR-42",
  "date": ISODate("2026-09-06"),
  "count": 500,                               <-- Groups 500 readings into 1 document
  "readings": [
    { "t": ISODate("..."), "temp": 24.5 },
    { "t": ISODate("..."), "temp": 24.7 }
  ]
}
```

---

## 2.2 Indexing Mastery & The ESR Rule

```javascript
// Optimal Index for: db.orders.find({ status: "PAID", customerId: 101 }).sort({ createdAt: -1 })
// Follows ESR: Equality (status, customerId) -> Sort (createdAt) -> Range
db.orders.createIndex({ status: 1, customerId: 1, createdAt: -1 });

// Partial Index: Only indexes active accounts, reducing index RAM usage by 90%
db.users.createIndex(
  { email: 1 },
  { partialFilterExpression: { status: "ACTIVE" } }
);

// TTL Index: Automatically deletes session documents after 3600 seconds (1 hour)
db.sessions.createIndex(
  { createdAt: 1 },
  { expireAfterSeconds: 3600 }
);
```

---

## 2.3 The Aggregation Pipeline Framework

```javascript
// Complex Multi-stage Aggregation Pipeline
db.orders.aggregate([
  // Stage 1: Filter ($match) - Must leverage indexes!
  { $match: { status: "COMPLETED", orderDate: { $gte: ISODate("2026-01-01") } } },

  // Stage 2: Deconstruct items array ($unwind)
  { $unwind: "$items" },

  // Stage 3: Group & Calculate Revenue ($group)
  {
    $group: {
      _id: "$items.category",
      totalRevenue: { $sum: { $multiply: ["$items.price", "$items.quantity"] } },
      totalOrders: { $addToSet: "$_id" },
      avgItemPrice: { $avg: "$items.price" }
    }
  },

  // Stage 4: Reshape output ($project)
  {
    $project: {
      category: "$_id",
      totalRevenue: 1,
      orderCount: { $size: "$totalOrders" },
      avgItemPrice: { $round: ["$avgItemPrice", 2] }
    }
  },

  // Stage 5: Sort ($sort)
  { $sort: { totalRevenue: -1 } },

  // Stage 6: Bounded Limit ($limit)
  { $limit: 10 }
], { allowDiskUse: true }); // Bypasses the 100MB in-memory RAM barrier!
```

---

## 2.4 Java: Spring Data MongoDB Aggregation & Reactive Streams

```java
package com.example.mongo.advanced;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Service
public class OrderAnalyticsService {

    private final MongoTemplate mongoTemplate;

    public OrderAnalyticsService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public List<CategoryRevenueDto> calculateCategoryRevenue() {
        MatchOperation match = match(Criteria.where("status").is("COMPLETED"));
        UnwindOperation unwind = unwind("items");
        GroupOperation group = group("items.category")
            .sum(ArithmeticOperators.Multiply.valueOf("items.price").multiplyBy("items.quantity")).as("totalRevenue")
            .count().as("itemCount");
        SortOperation sort = sort(Sort.Direction.DESC, "totalRevenue");
        LimitOperation limit = limit(10);

        Aggregation aggregation = newAggregation(match, unwind, group, sort, limit)
            .withOptions(AggregationOptions.builder().allowDiskUse(true).build());

        return mongoTemplate.aggregate(aggregation, "orders", CategoryRevenueDto.class).getMappedResults();
    }
}
```

```java
public record CategoryRevenueDto(String id, double totalRevenue, long itemCount) {}
```

---

## 2.5 Node.js: Mongoose Advanced Schemas, Middleware & Virtuals

```javascript
const mongoose = require('mongoose');
const bcrypt = require('bcrypt');

const userSchema = new mongoose.Schema({
  email: { type: String, required: true, unique: true, index: true },
  passwordHash: { type: String, required: true },
  firstName: String,
  lastName: String
}, {
  toJSON: { virtuals: true },
  toObject: { virtuals: true }
});

// Virtual Field: Computed on the fly without DB storage
userSchema.virtual('fullName').get(function() {
  return `${this.firstName} ${this.lastName}`.trim();
});

// Pre-Save Middleware: Automatically hashes passwords before persistence
userSchema.pre('save', async function(next) {
  if (!this.isModified('passwordHash')) return next();
  this.passwordHash = await bcrypt.hash(this.passwordHash, 12);
  next();
});

// Query Middleware: Prevents leaking passwordHash in find operations
userSchema.pre(/^find/, function(next) {
  this.select('-__v');
  next();
});

module.exports = mongoose.model('User', userSchema);
```

---

## 2.6 Multi-Document ACID Transactions (Polyglot Blueprints)

### 2.6.1 Java Spring `@Transactional` Blueprint
```java
@Configuration
@EnableTransactionManagement
public class MongoTxConfig {
    @Bean
    public MongoTransactionManager transactionManager(MongoDatabaseFactory dbFactory) {
        return new MongoTransactionManager(dbFactory);
    }
}
```

```java
@Service
public class WalletTransferService {

    private final MongoTemplate mongoTemplate;

    public WalletTransferService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Transactional // Executes inside a Multi-Document ClientSession
    public void transferFunds(String fromId, String toId, double amount) {
        // Decrement source
        Query fromQuery = new Query(Criteria.where("_id").is(fromId).and("balance").gte(amount));
        Update fromUpdate = new Update().inc("balance", -amount);
        var result = mongoTemplate.updateFirst(fromQuery, fromUpdate, "wallets");
        if (result.getModifiedCount() == 0) {
            throw new IllegalStateException("Insufficient funds");
        }

        // Increment target
        Query toQuery = new Query(Criteria.where("_id").is(toId));
        Update toUpdate = new Update().inc("balance", amount);
        mongoTemplate.updateFirst(toQuery, toUpdate, "wallets");
    }
}
```

### 2.6.2 Node.js Mongoose Transaction Blueprint
```javascript
const mongoose = require('mongoose');
const Wallet = require('../models/Wallet');

async function transferFundsNode(fromId, toId, amount) {
  const session = await mongoose.startSession();
  session.startTransaction({
    readConcern: { level: 'snapshot' },
    writeConcern: { w: 'majority' }
  });

  try {
    const fromWallet = await Wallet.findOneAndUpdate(
      { _id: fromId, balance: { $gte: amount } },
      { $inc: { balance: -amount } },
      { session, new: true }
    );

    if (!fromWallet) {
      throw new Error('Insufficient funds');
    }

    await Wallet.findOneAndUpdate(
      { _id: toId },
      { $inc: { balance: amount } },
      { session, new: true }
    );

    // Commit both operations atomically
    await session.commitTransaction();
  } catch (error) {
    // Rollback on any failure
    await session.abortTransaction();
    throw error;
  } finally {
    session.endSession();
  }
}
```

---

## 2.7 Real-Time Change Streams: Event-Driven Microservices

Change streams allow applications to access real-time data changes without polling:

```javascript
// Node.js Change Stream Listener
const Order = require('./models/Order');

function listenToOrders() {
  const changeStream = Order.watch([
    { $match: { 'operationType': { $in: ['insert', 'update'] }, 'fullDocument.status': 'PAID' } }
  ], { fullDocument: 'updateLookup' });

  changeStream.on('change', (next) => {
    console.log('Real-time payment event detected:', next.fullDocument.orderNumber);
    // Publish to Kafka or RabbitMQ...
  });

  changeStream.on('error', (err) => console.error('Change stream error:', err));
}
```

---

# TRACK 3: WIREDTIGER STORAGE ENGINE & DRIVER INTERNALS

## 3.1 WiredTiger Cache Sizing Formula & Eviction Server

WiredTiger does not use standard operating system page caches; it maintains its own internal uncompressed cache.

### Sizing Formula:
$$\text{WT Cache Size} = \max\left(500\text{MB}, 0.5 \times (\text{Total RAM} - 1\text{GB})\right)$$
*Example:* On a 64 GB RAM production server:
$$\text{WT Cache Size} = 0.5 \times (64 - 1) = 31.5\text{ GB}$$

### Eviction Triggers:
- **80% Cache Filled**: Background eviction threads start writing clean pages out.
- **95% Cache Filled or 20% Dirty Data**: **Client Worker Threads are forced to stop and perform eviction!** This causes API latency to spike from 2ms to 5,000ms!

---

# TRACK 4: PRODUCTION ENGINEERING, SIZING & SHARDING OPERATIONS

## 4.1 Connection Pool Sizing & Sockets

Both the Java driver and Node.js driver maintain connection pools:
```yaml
# Java application.yml
spring:
  data:
    mongodb:
      uri: mongodb://user:pass@mongo1:27017,mongo2:27017/prod?replicaSet=rs0&maxPoolSize=50&minPoolSize=10&maxIdleTimeMS=300000
```

```javascript
// Node.js Mongoose connection options
mongoose.connect(process.env.MONGO_URI, {
  maxPoolSize: 50,       // Max concurrent sockets
  minPoolSize: 10,       // Pre-warmed sockets
  serverSelectionTimeoutMS: 5000,
  socketTimeoutMS: 45000,
});
```

---

# TRACK 5: WAR ROOM POST-MORTEMS & ROOT CAUSE ANALYSIS (RCAs)

## Incident 1: WiredTiger Cache Eviction Starvation Freeze

- **Severity:** P0 Outage (API latency exploded to $>30\text{s}$, database unresponsive)
- **Mean Time to Recovery (MTTR):** 35 minutes
- **Symptoms:** `mongostat` revealed `dirty` cache pegged at 25%, and `evict` tickets dropped to 0.
- **Root Cause:** A developer ran an unindexed update query on a 50,000,000-document collection (`db.users.updateMany({ active: true }, { $set: { verified: true } })`). This loaded millions of pages into the WiredTiger cache in seconds, dirtying them faster than disk I/O could flush, completely exhausting eviction tickets.
- **The Permanent Fix:**
  1. Kill long-running operations via `db.killOp()`.
  2. Perform mass updates in bounded batches of 1,000 documents with pauses.
  3. Enforce query indexing: `notablescan: true` in production configuration.

---

## Incident 2: Aggregation Pipeline 100MB In-Memory Exceeded Crash

- **Severity:** P1 Error (Nightly billing job failed with `PlanExecutor error during aggregation: QueryExceededMemoryLimitNoDiskUseAllowed`)
- **Symptoms:** Monthly financial reporting crashed on the 1st of the month.
- **Root Cause:** An aggregation pipeline executed a `$group` stage over 10M rows without an index. The grouped documents exceeded MongoDB's strict 100MB RAM limit for pipeline stages.
- **The Permanent Fix:**
  1. Add `{ allowDiskUse: true }` to write temporary spill files to disk.
  2. Pre-filter rows using indexed `$match` stages at the very beginning of the pipeline.

---

# TRACK 6: CRACK-THE-INTERVIEW QUESTION BANK (SENIOR & STAFF+ LEVEL)

### 1. How does MongoDB prevent dirty reads during replica set failovers?
Using Read Concern `majority`. A read with `majority` guarantees that the returned data has been written to a majority of replica set members and recorded in their Oplog, ensuring that even if the current Primary crashes and is replaced, the data will never be rolled back.

### 2. What is a "Jumbo Chunk" in a sharded cluster and how do you resolve it?
A chunk becomes a "Jumbo Chunk" when its size exceeds `maxChunkSize` (default 64MB) and it cannot be split because all documents in the chunk share the exact same shard key value. To resolve: refine the shard key by adding a high-cardinality suffix (e.g., `{ tenantId: 1, _id: 1 }`).

### 3. How does Mongoose change-tracking work under the hood?
Mongoose documents maintain an internal `$__delta()` state tracking which paths were modified via getters and setters. When `.save()` is called, Mongoose constructs a minimal `$set` and `$unset` update document rather than replacing the entire document.

---

## ⚖️ MongoDB Polyglot Master Cheat Sheet

| Operation | MongoDB Shell | Java Spring Data MongoDB | Node.js (Mongoose) |
| :--- | :--- | :--- | :--- |
| **Find One** | `db.c.findOne({ email: "a@b.com" })` | `mongoTemplate.findOne(query, C.class)` | `Model.findOne({ email }).lean()` |
| **Atomic Inc** | `db.c.updateOne({}, { $inc: { v: 1 } })`| `update.inc("v", 1)` | `Model.updateOne({}, { $inc: { v: 1 } })` |
| **Aggregation**| `db.c.aggregate([...])` | `mongoTemplate.aggregate(agg, C.class)` | `Model.aggregate([...])` |
| **Transaction**| `session.withTransaction(...)` | `@Transactional` with `MongoTransactionManager` | `session.withTransaction(async () => ...)` |
| **Realtime** | `db.c.watch([...])` | `ReactiveMongoTemplate.tail(...)` | `Model.watch([...])` |

---
[🏠 Back to Home](README.md) | [🍃 Spring Boot Master Guide](spring_master_guide.md) | [🏛️ Spring Data JPA Guide](spring_data_jpa.md) | [📦 Jackson Guide](jackson_master_guide.md) | [🔐 Cryptography Guide](java_spring_cryptography_master_guide.md)
