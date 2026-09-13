[🏠 Back to Home](README.md) | [🍃 Spring Boot Master Guide](spring_master_guide.md) | [🏛️ Spring Data JPA Guide](spring_data_jpa.md) | [🛡️ Spring Security Guide](spring_security.md)

# ⚡ Spring Data Redis & Distributed Caching Master Guide

A production-grade engineering handbook for architecting low-latency caching, distributed state, distributed locking, and event streaming using **Spring Data Redis**, **Lettuce**, **Redisson**, **Spring Boot 3.x**, and **Java 17/21**. Covers cache failure patterns (Avalanche, Breakdown, Penetration), Redisson locks, Lua scripting, and Redis Streams.

---

## 📑 Table of Contents

1. [🧠 Zero-to-Hero Mental Model: The Sticky Note on the Monitor vs The Basement Filing Cabinet](#-the-sticky-note-on-the-monitor-vs-the-basement-filing-cabinet)
2. [🛠️ Prerequisites & Foundational Knowledge](#️-prerequisites--foundational-knowledge)
3. [📦 Track 1: The Junior & Entry-Level Foundations](#track-1-the-junior--entry-level-foundations-zero-to-hero)
4. [🚀 Track 2: Master Spring Data Redis Feature Catalog](#track-2-master-spring-data-redis-feature-catalog)
5. [🏗️ Track 3: Framework Internals & Lettuce Netty Pipeline](#track-3-framework-internals--lettuce-netty-pipeline)
6. [⚙️ Track 4: Production Engineering, Memory Sizing & SRE Operations](#track-4-production-engineering-memory-sizing--sre-operations)
7. [🚨 Track 5: War Room Post-Mortems & Root Cause Analysis (RCAs)](#track-5-war-room-post-mortems--root-cause-analysis-rcas)
8. [🎓 Track 6: Crack-The-Interview Question Bank (Senior & Staff+ Level)](#track-6-crack-the-interview-question-bank-senior--staff-level)
9. [⚖️ Spring Data Redis Master Cheat Sheet](#️-spring-data-redis-master-cheat-sheet)

---

## 🛠️ Prerequisites & Foundational Knowledge

Before configuring caching in Spring Boot 3, engineers must understand in-memory memory models and distributed cache failure modes:

### 1. Redis Single-Threaded Event Loop & RESP Protocol
- **Non-Blocking I/O Multiplexing**: Redis executes core command operations in a single thread using OS event demultiplexing (`epoll` on Linux, `kqueue` on macOS).
- **The RESP Protocol (REdis Serialization Protocol)**: A lightweight, human-readable wire protocol transmitting strings, arrays, and integers over TCP sockets with minimal CPU parsing overhead.
- **The Single-Threaded Danger**: Because commands run sequentially on a single CPU thread, executing an $O(N)$ command (such as `KEYS *` or huge `HGETALL`) **blocks all other clients on the entire database for seconds or minutes!**

### 2. The Big 3 Distributed Cache Pathologies
1. **Cache Avalanche (Stampede)**:
   - *Problem*: Thousands of cached keys are initialized with the exact same TTL (e.g. 1 hour). When that hour elapses, all keys expire at the same instant, sending millions of requests directly to the relational database, causing immediate connection pool starvation.
   - *Solution*: **Random Jitter**. Add random time variance to TTLs: $\text{TTL} = \text{Base TTL} + \text{random}(0, 300\text{s})$.
2. **Cache Breakdown (Hotspot Key)**:
   - *Problem*: A single ultra-hot key (e.g., Black Friday product page) expires. Thousands of concurrent requests experience a cache miss simultaneously and rush to query the database.
   - *Solution*: Mutex lock via Redisson, or the **XFetch probabilistic early expiration algorithm**.
3. **Cache Penetration**:
   - *Problem*: An attacker requests non-existent IDs (e.g., `id = -99999`). The key does not exist in Redis, so requests bypass the cache and query the database every time.
   - *Solution*: **Bloom Filters** to reject non-existent IDs before touching Redis, or caching `null` values with a short 60-second TTL.

### 3. Lettuce vs Jedis
- **Jedis**: Uses traditional blocking I/O. Each Java thread requires a dedicated socket connection managed via an Apache Commons Pool. Under high concurrency, connection contention degrades throughput.
- **Lettuce (Default in Spring Boot)**: Built on **Netty**. It shares a single thread-safe, non-blocking TCP socket across all worker threads using asynchronous pipelining, delivering superior throughput with lower resource utilization.

---

# TRACK 1: THE JUNIOR & ENTRY-LEVEL FOUNDATIONS (ZERO-TO-HERO)

## 1. The Real-World Mental Model (The Sticky Note on the Monitor)

- **Querying PostgreSQL / MySQL (The File Cabinet in the Dusty Basement):** Every time a customer asks for the Wi-Fi password, you stand up, walk down three flights of stairs into a dark basement, unlock a giant steel cabinet, flip through 50,000 paper folders, find the password, walk back upstairs, and answer the customer.
  - *Time taken:* 5 minutes ($50\text{ms} - 200\text{ms}$ in computer time).
  - If 5,000 customers ask at once, the stairs are jammed and the desk collapses!
- **With Redis (The Bright Yellow Sticky Note on your Monitor):**
  - The first time someone asks, you walk to the basement once and write the password on a sticky note pasted right next to your keyboard (**In-Memory RAM**).
  - The next 5,000 customers get their answer in **$<1$ millisecond** without you ever leaving your chair!

### Cache-Aside (Lazy Loading) Execution Matrix

| Request Stage | Cache State | System Behavior & Action Taken | Latency & Database Impact |
| :--- | :--- | :--- | :--- |
| **Lookup Phase** | Query Redis via Key | App issues non-blocking in-memory `GET` command | $< 1\text{ms}$; 0 SQL queries generated |
| **Cache Hit** | Key Present | Returns deserialized value directly to client | Fast path; zero disk I/O, zero database connection pool usage |
| **Cache Miss** | Key Absent / Expired | 1. Queries primary relational database (PostgreSQL/MySQL)<br>2. Writes result to Redis with configured TTL<br>3. Returns fresh entity to client | Slow path ($10-50\text{ms}$); hydrates cache to protect DB against future stampedes |

---

## 2. The 5 Core Building Blocks

| Term | What It Means | Real-World Analogy |
| :--- | :--- | :--- |
| **`RedisTemplate<K, V>`** | High-level Spring wrapper for executing typed commands across Redis data structures. | The all-in-one universal remote control for your home entertainment system. |
| **`@Cacheable`** | Declarative annotation that intercepts method calls, returning cached results if present. | A smart secretary who hands you the pre-printed answer before you pick up the phone. |
| **`TTL (Time To Live)`** | Expiration timer assigned to a key after which Redis deletes it from memory. | The "Best Before" expiration date stamped on a milk carton. |
| **`Redisson`** | Advanced Redis Java client providing distributed locks, semaphores, and collections. | A certified digital locksmith who hands out master keys across multiple servers. |
| **`Lua Script`** | A lightweight script executed atomically on the Redis server without network round-trips. | A sealed envelope of instructions that the teller must execute without interruption. |

---

## 3. Beginner Code Walkthrough: Declarative Caching

```java
package com.example.redis.service;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.Serializable;

public record ProductDto(Long id, String name, double price) implements Serializable {}

@Service
public class ProductCatalogService {

    // 1. Cache HIT returns instantly. Cache MISS executes method and stores in Redis.
    @Cacheable(value = "products", key = "#id", unless = "#result == null")
    public ProductDto getProductById(Long id) {
        simulateSlowDatabaseCall();
        return new ProductDto(id, "MacBook Pro", 2499.00);
    }

    // 2. Updates database AND overwrites cached value in Redis
    @CachePut(value = "products", key = "#product.id()")
    public ProductDto updateProduct(ProductDto product) {
        return product;
    }

    // 3. Deletes item from database AND purges key from Redis
    @CacheEvict(value = "products", key = "#id")
    public void deleteProduct(Long id) {
        // Purged from cache
    }

    private void simulateSlowDatabaseCall() {
        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
    }
}
```

---

## 4. Top 10 Junior Interview Questions

### Q1: What is the difference between `@Cacheable` and `@CachePut`?
- **ELI5 Answer:** *"`@Cacheable` checks if the answer is already on the sticky note and skips the work; `@CachePut` always does the work and updates the sticky note."*
- **Technical Answer:** *"`@Cacheable` skips method execution if the key exists in the cache (Cache Hit). `@CachePut` always executes the method and updates the cache with the method's return value."*

### Q2: Why does Redis store binary garble (`\xac\xed\x00\x05...`) by default?
- **ELI5 Answer:** *"Speaking in proprietary alien code instead of clean human English."*
- **Technical Answer:** *"By default, Spring Boot uses `JdkSerializationRedisSerializer`, which converts Java objects into raw Java binary serialization format. To store readable JSON, configure `GenericJackson2JsonRedisSerializer` or `Jackson2JsonRedisSerializer`."*

### Q3: What happens when Redis runs out of memory?
- **ELI5 Answer:** *"When the closet fills up, you either throw out the oldest coat or stop accepting new clothes."*
- **Technical Answer:** *"Redis evaluates its configured `maxmemory-policy`. Under `allkeys-lru`, it evicts the least recently used keys. Under `noeviction` (default), it rejects write operations with an `OOM command not allowed` error while continuing to serve read queries."*

### Q4: Why is `KEYS *` forbidden in production Redis?
- **ELI5 Answer:** *"Stopping the entire factory assembly line for 10 minutes to count every bolt in the building."*
- **Technical Answer:** *"`KEYS *` is an $O(N)$ command that scans the entire keyspace synchronously. In a Redis instance with millions of keys, it blocks the single-threaded event loop for seconds, causing timeouts across all microservices. Always use `SCAN` instead."*

### Q5: What is a Distributed Lock?
- **ELI5 Answer:** *"A single glowing microphone in a dark room: only the person holding the microphone is allowed to speak."*
- **Technical Answer:** *"A mechanism that ensures only one instance across a distributed cluster of microservices can execute a critical section at any given time (e.g. inventory decrement)."*

### Q6: What does `unless` vs `condition` mean in `@Cacheable`?
- **ELI5 Answer:** *"`condition` decides whether to check the note before starting; `unless` inspects the answer before deciding to write it down."*
- **Technical Answer:** *"`condition` is evaluated *before* method execution; if false, caching is skipped entirely. `unless` is evaluated *after* method execution; if true (e.g. `#result == null`), the result is not cached."*

### Q7: What is the Redisson Watchdog?
- **ELI5 Answer:** *"A faithful dog who keeps adding wood to the campfire so it doesn't go out while you are still working."*
- **Technical Answer:** *"A background timer in Redisson that automatically extends the lease time of a distributed lock every 10 seconds as long as the owning thread is still alive and working, preventing premature lock release."*

### Q8: How does Redis achieve atomic operations?
- **ELI5 Answer:** *"The chef closes the kitchen door and makes your entire sandwich without answering the phone until it's finished."*
- **Technical Answer:** *"Because Redis processes commands on a single thread, native commands (`INCR`, `SETNX`, `HSET`) are inherently atomic. For multi-step workflows, Redis executes Lua scripts atomically in a single pass."*

### Q9: What is the difference between a Redis Set and a Sorted Set (ZSet)?
- **ELI5 Answer:** *"A Set is a bag of unique marbles; a Sorted Set gives each marble a score so they line up in rank order."*
- **Technical Answer:** *"A Set (`SADD`) stores unique, unordered strings in $O(1)$ time. A Sorted Set (`ZADD`) associates each member with a floating-point score, maintaining elements ordered by score using a SkipList and HashTable ($O(\log N)$ lookup)."*

### Q10: Why should you use `StringRedisTemplate` instead of raw `RedisTemplate`?
- **ELI5 Answer:** *"A specialized wrench that only fits standard bolts, eliminating setup confusion."*
- **Technical Answer:** *"`StringRedisTemplate` is pre-configured with `StringRedisSerializer` for both keys and values, eliminating serialization overhead and guaranteeing human-readable strings across the Redis CLI."*

---

# TRACK 2: MASTER SPRING DATA REDIS FEATURE CATALOG

## Master Redis Data Structure Decision Matrix

| Data Structure | Redis Commands | Java Spring Interface | Best Used For | Anti-Pattern For |
| :--- | :--- | :--- | :--- | :--- |
| **String** | `SET`, `GET`, `INCR` | `opsForValue()` | Cached DTOs, API responses, counters | Complex nested object graphs |
| **Hash** | `HSET`, `HGETALL` | `opsForHash()` | User profiles, cart objects (partial updates) | Storing massive arrays |
| **List** | `LPUSH`, `RPOP` | `opsForList()` | Task queues, activity feeds | Random access by index ($O(N)$) |
| **Set** | `SADD`, `SINTER` | `opsForSet()` | Unique tags, friend circles, intersections | Ordered paging |
| **Sorted Set (ZSet)**| `ZADD`, `ZRANGE` | `opsForZSet()` | Real-time leaderboards, sliding window rate limiters| Unordered bulk tags |
| **Stream** | `XADD`, `XREADGROUP` | `opsForStream()` | Distributed event sourcing, Kafka alternative | Simple ephemeral key-value caching |

---

## 2.1 Redis Strings & Simple Dynamic Strings (SDS)

1. **Architectural Overview & Purpose**:
   - The fundamental binary-safe byte sequence primitive in Redis. Can store raw text, integers, floating-point numbers, or serialized binary blobs (JSON, Protobuf, images) up to **512 MB**.

2. **Underlying Algorithm, Data Structure & Design Pattern**:
   - **C-String Limitations**: Standard C strings (`char*`) are null-terminated (`\0`), cannot contain binary data, and calculating length requires $O(N)$ string scans.
   - **Simple Dynamic String (SDS)**:
| SDS Memory Field | Field Type & Size | Purpose & Behavioral Mechanics | Algorithmic Complexity |
| :--- | :--- | :--- | :--- |
| **`len`** | `uint8_t` to `uint64_t` | Byte length of payload currently stored in buffer | $O(1)$ length calculation (vs $O(N)$ for C `strlen`) |
| **`alloc`** | `uint8_t` to `uint64_t` | Total memory pre-allocated for the buffer | Enables amortized $O(1)$ appends; prevents constant `realloc()` calls |
| **`flags`** | 3 bits (`uint8_t`) | Header type discriminator (`sdshdr8`, `sdshdr16`, `sdshdr32`, `sdshdr64`) | Optimizes memory footprint for small keys |
| **`buf[]`** | Binary byte array | Raw string or serialized payload | Binary-safe: can contain null bytes (`\0`), JSON, or raw images |
| **`\0`** | 1 byte delimiter | Null terminator appended automatically | Compatible with standard C library `printf` functions |
     - `len`: Length of the string in bytes ($O(1)$ length lookup).
     - `alloc`: Total memory allocated, including unused buffer space (pre-allocation strategy to minimize `realloc()` calls).
     - `flags`: Header type (`sdshdr8`, `sdshdr16`, `sdshdr32`, `sdshdr64`) to minimize memory header overhead.
     - **Integer Encoding (`OBJ_ENCODING_INT`)**: If the string is an integer between $-2^{63}$ and $2^{63}-1$, Redis stores it directly as a 64-bit primitive integer in the `ptr` field of the Redis Object (`robj`), consuming **zero auxiliary heap memory**!

3. **Full Syntax & Method Signatures**:
   ```java
   // StringRedisTemplate opsForValue()
   void set(String key, String value);
   void set(String key, String value, Duration timeout);
   Boolean setIfAbsent(String key, String value, Duration timeout); // SET key val NX PX ms
   String get(Object key);
   List<String> multiGet(Collection<String> keys);                  // MGET k1 k2 k3
   Long increment(String key);                                      // INCR
   Long increment(String key, long delta);                          // INCRBY
   ```

4. **Concrete Code Examples with Sample Values & Expected Results**:
   ```java
   @Autowired
   private StringRedisTemplate redisTemplate;

   public void runStringExamples() {
       ValueOperations<String, String> ops = redisTemplate.opsForValue();

       // 1. Basic Caching with TTL
       ops.set("user:101:profile", "{\"name\":\"Alice\",\"role\":\"ADMIN\"}", Duration.ofMinutes(15));
       String profile = ops.get("user:101:profile");
       System.out.println("Cached Profile: " + profile);
       // Output: Cached Profile: {"name":"Alice","role":"ADMIN"}

       // 2. Atomic Distributed Counter
       Long visitCount = ops.increment("page:views:home", 1);
       ops.increment("page:views:home", 5);
       System.out.println("Total Visits: " + ops.get("page:views:home"));
       // Output: Total Visits: 6

       // 3. Atomic Distributed Mutex (SETNX with TTL)
       Boolean acquired = ops.setIfAbsent("lock:invoice:789", "INSTANCE_A", Duration.ofSeconds(10));
       System.out.println("Lock Acquired: " + acquired);
       // Output: Lock Acquired: true
       Boolean secondAttempt = ops.setIfAbsent("lock:invoice:789", "INSTANCE_B", Duration.ofSeconds(10));
       System.out.println("Second Attempt Acquired: " + secondAttempt);
       // Output: Second Attempt Acquired: false
   }
   ```

5. **Pros & Cons (Benefits vs. Drawbacks & Hard Limits)**:
   | Metric | Advantage | Drawback / Limit |
   | :--- | :--- | :--- |
   | **Latency** | Sub-millisecond ($<0.5\text{ms}$) reads and writes. | Storing massive JSON blobs burns excessive network bandwidth. |
   | **Atomic Counters** | Native `INCRBY` / `DECRBY` eliminates database update locks. | Maximum size: 512 MB per key (production rule: keep $<100\text{ KB}$). |
   | **Object Overhead** | Integer encoding saves memory. | Small strings incur 16-byte `robj` + dict entry overhead ($\approx 96$ bytes). |

6. **Production Efficiency: When to Use vs. Anti-Patterns**:
   - **Ideal Scenarios**: High-frequency read-through API caching, idempotency keys, atomic distributed sequence numbers, temporary auth tokens.
   - **Fatal Anti-Pattern**: Serializing a 50-field User DTO into a JSON string when the application frequently updates only one field (`lastLoginTimestamp`). Deserializing 100 KB, updating 1 timestamp, and re-serializing 100 KB back to Redis wastes CPU, network I/O, and causes race condition overwrites! (Use a Redis **Hash** instead).

7. **How It Breaks: Top Beginner Mistakes & Failure Dynamics**:
   - **The Non-Atomic `SETNX` without TTL Leak**:
     ```java
     // 💥 DISASTER: Two-step non-atomic locking!
     redisTemplate.opsForValue().setIfAbsent("lock:order", "1");
     // If JVM crashes, OOMs, or is killed HERE, the lock key NEVER expires!
     redisTemplate.expire("lock:order", Duration.ofSeconds(10));
     // ✅ FIX: Always use the atomic single-command overload:
     redisTemplate.opsForValue().setIfAbsent("lock:order", "1", Duration.ofSeconds(10));
     ```

8. **Tricky Interview Questions & Gotchas**:
   - **Q: How does Redis handle integer increments on strings without parsing overhead?**
     - *Answer*: If a string contains a numeric value within 64-bit integer limits, Redis encodes it internally as `OBJ_ENCODING_INT` directly inside the pointer field. Calling `INCR` executes direct CPU integer addition without any string parsing or memory reallocation.

---

## 2.2 Redis Hashes & ZipList/Dict Optimization

1. **Architectural Overview & Purpose**:
   - Maps string fields to string values (`Field -> Value`). Engineered to represent objects (User Profiles, Shopping Carts, Account Balances) and update individual fields without modifying the entire object.

2. **Underlying Algorithm, Data Structure & Design Pattern**:
   - **Dual Encoding Mechanism**:
     - **`ziplist` / `listpack` (Memory-Compressed Array)**: When a Hash contains fewer than 512 fields (`hash-max-ziplist-entries`) and all field/value sizes are $<64$ bytes (`hash-max-ziplist-value`), Redis stores the entire hash in a contiguous linear memory buffer. This eliminates pointer overhead and dictionary headers, **saving up to 80% RAM**!
     - **`hashtable` (SipHash)**: When thresholds are exceeded, Redis automatically migrates the hash to a dynamic hash table with bucket array, collision chaining, and incremental rehash.

3. **Full Syntax & Method Signatures**:
   ```java
   // StringRedisTemplate opsForHash()
   void put(String key, Object hashKey, Object value);             // HSET key field val
   void putAll(String key, Map<?, ?> m);                          // HMSET key f1 v1 f2 v2
   Object get(String key, Object hashKey);                        // HGET key field
   Map<Object, Object> entries(String key);                       // HGETALL key (DANGER!)
   Boolean hasKey(String key, Object hashKey);                    // HEXISTS key field
   Long increment(String key, Object hashKey, long delta);        // HINCRBY key field delta
   ```

4. **Concrete Code Examples with Sample Values & Expected Results**:
   ```java
   public void runHashExamples() {
       HashOperations<String, Object, Object> hashOps = redisTemplate.opsForHash();
       String cartKey = "cart:user:405";

       // 1. Partial Object Updates
       Map<String, String> initialCart = Map.of(
           "item:SKU-100", "2",
           "item:SKU-200", "1",
           "couponCode", "SPRING20"
       );
       hashOps.putAll(cartKey, initialCart);

       // 2. Increment item quantity directly inside the hash
       hashOps.increment(cartKey, "item:SKU-100", 3); // Quantity becomes 2 + 3 = 5

       System.out.println("SKU-100 Qty: " + hashOps.get(cartKey, "item:SKU-100"));
       // Output: SKU-100 Qty: 5

       // 3. Fast Field Existence Check without loading whole object
       Boolean hasCoupon = hashOps.hasKey(cartKey, "couponCode");
       System.out.println("Has Coupon: " + hasCoupon);
       // Output: Has Coupon: true
   }
   ```

5. **Pros & Cons (Benefits vs. Drawbacks & Hard Limits)**:
   | Feature | Advantage | Drawback / Limit |
   | :--- | :--- | :--- |
   | **Granular Updates** | Mutate a single field without touching others. | Expiration (`TTL`) cannot be set on individual fields (applies to whole hash). |
   | **Memory Packing** | Ziplist encoding reduces RAM footprint by $5\times$. | `HGETALL` on massive hashes blocks single-threaded event loop. |

6. **Production Efficiency: When to Use vs. Anti-Patterns**:
   - **Ideal Scenarios**: User shopping carts, user sessions, live IoT sensor metrics, dynamic account attributes.
   - **Fatal Anti-Pattern**: Calling `HGETALL` on a Hash containing 100,000 fields! `HGETALL` is $O(N)$ and blocks the single-threaded Redis engine for 200ms–2 seconds, timing out all upstream microservices. Always use `HSCAN` or `HMGET` for selective field reads.

7. **Tricky Interview Questions & Gotchas**:
   - **Q: Can you expire a single field inside a Redis Hash?**
     - *Answer*: Prior to Redis 7.4, native TTL applied strictly to the top-level key; individual field expiration required storing timestamp fields and checking them manually or storing each field as a separate String key. Redis 7.4 introduced field-level expiration (`HEXPIRE`, `HTTL`).

---

## 2.3 Redis Lists & QuickLists: Queues & Activity Feeds

1. **Architectural Overview & Purpose**:
   - Ordered collection of strings sorted by insertion order. Supports high-speed $O(1)$ push and pop operations at both the head and tail, making it ideal for message queues, task buffers, and capped activity logs.

2. **Underlying Algorithm, Data Structure & Design Pattern**:
   - **`quicklist` (Doubly-Linked List of `ziplist` nodes)**:
     - Traditional linked lists waste 24 bytes per node and cause CPU cache misses.
     - Pure ziplists require expensive memory reallocations when resizing large arrays.
     - **The QuickList Hybrid**: A doubly-linked list where each individual node is a compact `ziplist` containing multiple elements. This provides fast $O(1)$ head/tail operations while retaining contiguous CPU cache locality and zero memory fragmentation.

3. **Full Syntax & Method Signatures**:
   ```java
   // StringRedisTemplate opsForList()
   Long leftPush(String key, String value);                       // LPUSH (Head)
   Long rightPush(String key, String value);                      // RPUSH (Tail)
   String leftPop(String key);                                    // LPOP
   String rightPop(String key);                                   // RPOP (FIFO Queue with LPUSH)
   String rightPop(String key, Duration timeout);                 // BRPOP (Blocking wait)
   List<String> range(String key, long start, long end);          // LRANGE (Paging)
   void trim(String key, long start, long end);                   // LTRIM (Fixed-size buffer)
   ```

4. **Concrete Code Examples with Sample Values & Expected Results**:
   ```java
   public void runListExamples() {
       ListOperations<String, String> listOps = redisTemplate.opsForList();
       String auditKey = "audit:recent:events";

       // Capped Ring Buffer: Always keep strictly the last 3 events
       listOps.leftPush(auditKey, "EVENT_LOGIN");
       listOps.leftPush(auditKey, "EVENT_PASSWORD_CHANGE");
       listOps.leftPush(auditKey, "EVENT_PAYMENT");
       listOps.leftPush(auditKey, "EVENT_LOGOUT");
       listOps.trim(auditKey, 0, 2); // Keeps only indices 0, 1, 2 (drops EVENT_LOGIN)

       List<String> recent = listOps.range(auditKey, 0, -1);
       System.out.println("Recent Events: " + recent);
       // Output: Recent Events: [EVENT_LOGOUT, EVENT_PAYMENT, EVENT_PASSWORD_CHANGE]
   }
   ```

5. **Pros & Cons (Benefits vs. Drawbacks & Hard Limits)**:
   - **Pros**: $O(1)$ push/pop at ends; `BRPOP` provides zero-CPU blocking waits for background workers.
   - **Cons**: Random index access (`LINDEX`, `LINSERT`) is $O(N)$. Never use Redis Lists as a random-access indexed array!

---

## 2.4 Redis Sets & IntSets: Deduplication & Set Algebra

1. **Architectural Overview & Purpose**:
   - Unordered collection of unique strings. Provides $O(1)$ time complexity for adding, removing, and testing existence of members, as well as powerful set operations (Intersection, Union, Difference).

2. **Underlying Algorithm, Data Structure & Design Pattern**:
   - **`intset` (Sorted Integer Array)**: If a Set consists solely of 64-bit integers and contains fewer than 512 members, Redis stores it in a tightly packed binary array using binary search ($O(\log N)$).
   - **`hashtable`**: Upgrades to a hash table with empty values when string elements are added or capacity exceeds thresholds.

3. **Full Syntax & Method Signatures**:
   ```java
   // StringRedisTemplate opsForSet()
   Long add(String key, String... values);                        // SADD
   Boolean isMember(String key, Object o);                        // SISMEMBER (O(1))
   Set<String> members(String key);                               // SMEMBERS (DANGER if large)
   Set<String> intersect(String key, String otherKey);            // SINTER
   Set<String> union(String key, String otherKey);                // SUNION
   Set<String> difference(String key, String otherKey);           // SDIFF
   ```

4. **Concrete Code Examples with Sample Values & Expected Results**:
   ```java
   public void runSetExamples() {
       SetOperations<String, String> setOps = redisTemplate.opsForSet();

       setOps.add("user:101:skills", "JAVA", "SPRING", "DOCKER", "KUBERNETES");
       setOps.add("user:102:skills", "PYTHON", "JAVA", "DOCKER", "AWS");

       // Calculate Mutual / Common Skills (Intersection)
       Set<String> mutualSkills = setOps.intersect("user:101:skills", "user:102:skills");
       System.out.println("Common Skills: " + mutualSkills);
       // Output: Common Skills: [JAVA, DOCKER]

       // Deduplication test
       Boolean knowsKafka = setOps.isMember("user:101:skills", "KAFKA");
       System.out.println("Knows Kafka: " + knowsKafka);
       // Output: Knows Kafka: false
   }
   ```

5. **Pros & Cons**:
   - **Pros**: Blazingly fast membership tests; server-side set algebra eliminates client-side looping.
   - **Cons**: `SMEMBERS` on sets with 1,000,000 items blocks the event loop. Always use `SSCAN` in production.

---

## 2.5 Redis Sorted Sets (ZSets) & SkipLists: Leaderboards & Rate Limiters

1. **Architectural Overview & Purpose**:
   - Every element in a Sorted Set is associated with a floating-point **score**. Elements are maintained in sorted order by score. Ideal for real-time leaderboards, priority queues, and sliding-window rate limiters.

2. **Underlying Algorithm, Data Structure & Design Pattern**:
   - **The Dual Data Structure**:
     - **Hash Table**: Maps `member -> score` in $O(1)$ time.
     - **SkipList (Probabilistic Multilevel Linked List)**:
       ```
       Level 3: [Head] ------------------------------> [Node 75] -> NIL
       Level 2: [Head] -------------> [Node 40] ------> [Node 75] -> NIL
       Level 1: [Head] -> [Node 10] -> [Node 40] ------> [Node 75] -> NIL
       ```
       Provides $O(\log N)$ search, insertion, deletion, and range scans without the complex rebalancing rotations of AVL or Red-Black trees.

3. **Full Syntax & Method Signatures**:
   ```java
   // StringRedisTemplate opsForZSet()
   Boolean add(String key, String value, double score);           // ZADD
   Double incrementScore(String key, String value, double delta); // ZINCRBY
   Long rank(String key, Object o);                               // ZRANK (0-based ascending)
   Long reverseRank(String key, Object o);                        // ZREVRANK (0-based descending)
   Set<String> reverseRange(String key, long start, long end);    // ZREVRANGE (Top-N)
   Long removeRangeByScore(String key, double min, double max);   // ZREMRANGEBYSCORE
   ```

4. **Concrete Code Examples with Sample Values & Expected Results**:
   ```java
   public void runZSetExamples() {
       ZSetOperations<String, String> zsetOps = redisTemplate.opsForZSet();
       String leaderboardKey = "leaderboard:gaming:season1";

       zsetOps.add(leaderboardKey, "Player_Alpha", 1250);
       zsetOps.add(leaderboardKey, "Player_Bravo", 2400);
       zsetOps.add(leaderboardKey, "Player_Charlie", 1850);
       zsetOps.add(leaderboardKey, "Player_Delta", 3100);

       // 1. Get Rank of Player_Charlie (Descending Order, Top rank is 0)
       Long rank = zsetOps.reverseRank(leaderboardKey, "Player_Charlie");
       System.out.println("Player_Charlie Rank: #" + (rank + 1));
       // Output: Player_Charlie Rank: #3 (Delta=1, Bravo=2, Charlie=3, Alpha=4)

       // 2. Fetch Top 3 Players
       Set<String> top3 = zsetOps.reverseRange(leaderboardKey, 0, 2);
       System.out.println("Top 3 Players: " + top3);
       // Output: Top 3 Players: [Player_Delta, Player_Bravo, Player_Charlie]
   }
   ```

5. **Sliding Window Rate Limiter Blueprint (Using ZSet & Unix Milliseconds)**:
   ```java
   public boolean isAllowed(String userId, int maxRequests, long windowMillis) {
       String key = "ratelimit:sliding:" + userId;
       long now = System.currentTimeMillis();
       long clearBefore = now - windowMillis;

       ZSetOperations<String, String> zset = redisTemplate.opsForZSet();
       // Remove all requests outside the sliding window
       zset.removeRangeByScore(key, 0, clearBefore);

       // Count requests remaining in the window
       Long currentCount = zset.zCard(key);
       if (currentCount != null && currentCount >= maxRequests) {
           return false; // Rate limit exceeded!
       }

       // Add current request with current timestamp as score
       zset.add(key, String.valueOf(now), now);
       redisTemplate.expire(key, Duration.ofMillis(windowMillis));
       return true;
   }
   ```

---

## 2.6 Redis Streams & Radix Trees: Event Sourcing & Consumer Groups

1. **Architectural Overview & Purpose**:
   - Append-only log data structure introduced in Redis 5.0. Models Apache Kafka semantics inside Redis: supports Consumer Groups, message offsets, delivery acknowledgments (`XACK`), and automatic message replay via the **Pending Entries List (PEL)**.

2. **Underlying Algorithm, Data Structure & Design Pattern**:
   - **Radix Tree (Rax Engine)**: Memory-optimized tree data structure indexing Stream entries by timestamp and sequence number (`<millisecondsTime>-<sequenceNumber>`, e.g., `1717654321000-0`).
   - **Pending Entries List (PEL)**: Tracks messages delivered to a consumer but not yet acknowledged via `XACK`. If a worker pod crashes, pending messages in the PEL are reclaimed via `XCLAIM`.

3. **Full Syntax & Method Signatures**:
   ```java
   // StringRedisTemplate opsForStream()
   RecordId add(String key, Map<String, String> content);         // XADD key * f1 v1
   List<MapRecord<String, Object, Object>> read(Consumer consumer, StreamOffset<String> offset);
   Long acknowledge(String key, String group, RecordId... ids);   // XACK key group id
   PendingMessagesSummary pending(String key, String group);      // XPENDING summary
   ```

4. **Concrete Code Examples with Sample Values & Expected Results**:
   ```java
   public void runStreamExamples() {
       StreamOperations<String, Object, Object> streamOps = redisTemplate.opsForStream();
       String streamKey = "stream:orders";

       // Producer: Publish Order Placed Event
       Map<String, String> orderEvent = Map.of("orderId", "ORD-9901", "amount", "450.00", "currency", "USD");
       RecordId messageId = streamOps.add(streamKey, orderEvent);
       System.out.println("Published Event ID: " + messageId);
       // Output: Published Event ID: 1717654321000-0

       // Consumer: Read events with auto-ack
       List<MapRecord<String, Object, Object>> messages = streamOps.read(
           Consumer.from("order_processors", "worker_pod_1"),
           StreamReadOptions.empty().count(10),
           StreamOffset.create(streamKey, ReadOffset.lastConsumed())
       );
   }
   ```

---

## 2.7 Redis Memory Eviction Policies & Sizing Formulas

1. **Eviction Algorithms & Mechanics**:
   When `used_memory` reaches `maxmemory`, Redis evaluates its eviction policy:
   - **`noeviction` (Default)**: Rejects writes with `(error) OOM command not allowed`, serves reads.
   - **`allkeys-lru`**: Evicts least recently used keys across all keys.
   - **`volatile-lru`**: Evicts least recently used keys among keys with an expiration (`TTL`) set.
   - **`allkeys-lfu`**: Evicts least frequently used keys across all keys.
   - **`volatile-ttl`**: Evicts keys with shortest remaining TTL.

2. **The Sampling Approximation Algorithm**:
   - Redis does NOT maintain a global doubly-linked list of all keys for LRU (which would burn 16 bytes per key).
   - Instead, it uses **Random Sampling**: It randomly picks $N$ keys (default `maxmemory-samples 5`), examines their idle times, and evicts the oldest key among the sample. With 10 samples, accuracy closely approximates theoretical true LRU with zero memory overhead!

3. **Production Memory Sizing Formula**:
   $$\text{RAM Needed} = \left( \sum (\text{Key Count} \times (\text{Key Bytes} + \text{Val Bytes} + 96\text{ bytes overhead})) \right) \times 1.3\text{ (Buffer)}$$

---

## 2.8 Distributed Locking Architecture: Redisson Watchdog vs Redlock

1. **The Native Redis Lock Algorithm (Single Node)**:
   - **Acquire**: `SET lock:order:101 UUID_TOKEN NX PX 10000` (Atomic Set if Not Exists with 10s lease).
   - **Release**: Must be executed via **Lua script** to ensure the client only releases the lock if the value matches its own `UUID_TOKEN` (preventing deleting another client's lock):
     ```lua
     if redis.call('get', KEYS[1]) == ARGV[1] then
         return redis.call('del', KEYS[1])
     else
         return 0
     end
     ```

2. **The Redisson Watchdog Mechanics**:
   - *Problem*: What if the business operation takes 15 seconds, but lock lease was 10 seconds? The lock expires, another worker acquires the lock, and concurrent execution corrupts state!
   - *Watchdog Solution*: If no explicit lease time is provided (`lock.lock()`), Redisson starts a background Netty timer task that periodically renews the lock expiration every **10 seconds** (`lockWatchdogTimeout / 3`) as long as the holding thread is alive.

3. **Production Flash Sale Code Blueprint**:
   ```java
   @Service
   public class InventoryBookingService {
       private final RedissonClient redisson;

       public InventoryBookingService(RedissonClient redisson) {
           this.redisson = redisson;
       }

       public boolean reserveStock(String productId, int quantity) {
           RLock lock = redisson.getLock("lock:inventory:" + productId);
           try {
               // Wait up to 3s to acquire lock; Watchdog automatically extends lease!
               boolean acquired = lock.tryLock(3, -1, TimeUnit.SECONDS);
               if (!acquired) return false;

               // Critical Section: Decrement stock safely
               return executeDecrement(productId, quantity);

           } catch (InterruptedException e) {
               Thread.currentThread().interrupt();
               return false;
           } finally {
               if (lock.isHeldByCurrentThread()) {
                   lock.unlock(); // Safe unlock
               }
           }
       }
       private boolean executeDecrement(String id, int qty) { return true; }
   }
   ```

---

## 2.9 Atomic Lua Scripting & Redis Transactions

1. **Why Lua Scripting Over `MULTI`/`EXEC`**:
   - `MULTI`/`EXEC` queues commands, but **does NOT support rollback** if a command fails logically, nor does it allow using the result of Step 1 to decide Step 2 within the same transaction.
   - Lua scripts run atomically inside the single-threaded Redis engine: no other command can execute while a Lua script is running!

2. **Production Rate Limiter Lua Script**:
   ```java
   @Service
   public class RedisLuaRateLimiter {
       private final StringRedisTemplate redisTemplate;

       private static final String SCRIPT = """
           local key = KEYS[1]
           local limit = tonumber(ARGV[1])
           local window = tonumber(ARGV[2])
           local current = redis.call('INCR', key)
           if current == 1 then
               redis.call('EXPIRE', key, window)
           end
           if current > limit then
               return 0
           else
               return 1
           end
           """;

       public RedisLuaRateLimiter(StringRedisTemplate redisTemplate) {
           this.redisTemplate = redisTemplate;
       }

       public boolean allow(String clientId, int limit, int windowSec) {
           DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(SCRIPT, Long.class);
           Long result = redisTemplate.execute(
               redisScript,
               Collections.singletonList("ratelimit:" + clientId),
               String.valueOf(limit),
               String.valueOf(windowSec)
           );
           return result != null && result == 1L;
       }
   }
   ```

---

## 2.10 Redis Persistence & High Availability Topologies

1. **Persistence Mechanics (RDB vs AOF vs Hybrid)**:
   - **RDB (Snapshotting)**: Calls `bgsave` via `fork()` system call. Copy-On-Write (COW) dumps point-in-time binary snapshot to `dump.rdb`. Fast recovery, but risks losing minutes of data between snapshots.
   - **AOF (Append Only File)**: Logs every write command to `appendonly.aof`.
     - `appendfsync always`: Slow, guaranteed zero data loss.
     - `appendfsync everysec`: **Production Standard** (max 1 second of data loss).
   - **Hybrid Persistence (Redis 4.0+)**: RDB preamble + AOF incremental log. Fast restarts with minimal data loss.

2. **Sentinel vs Redis Cluster**:
   - **Redis Sentinel**: Master-Replica architecture with automated failover via quorum voting. Best for data sets $<50\text{ GB}$.
   - **Redis Cluster (Sharding)**: Multi-master distributed hash ring with **16,384 hash slots**. Shard determined via $\text{CRC16}(\text{key}) \pmod{16384}$. Scales horizontally beyond RAM limits of a single machine.

---

# TRACK 3: FRAMEWORK INTERNALS & LETTUCE NETTY PIPELINE

## 3.1 Lettuce Connection Pipeline & Asynchronous Dispatch

### Lettuce Multiplexed Client Architecture

| Architecture Layer | Core Component | Mechanics & Concurrency Behavior | Throughput & Thread Benefit |
| :--- | :--- | :--- | :--- |
| **Caller Layer** | Java Threads (1..N) | Concurrent callers invoke `RedisTemplate` / reactive commands without blocking | Lock-free; callers register callbacks and continue execution |
| **Dispatch Layer** | `CommandDispatcher` | Enqueues outbound command frames onto thread-safe internal queues | Batches pipeline commands automatically under peak load |
| **Transport Layer** | Shared Netty `EventLoopGroup` | Manages non-blocking I/O over an OS socket selector (`epoll`/`kqueue`) | Multiplexes hundreds of threads across 1 single TCP connection |
| **Server Layer** | Redis Server Instance | Executes single-threaded event loop operations in memory | Ultra-low context-switching; zero per-thread connection overhead |
| **Response Layer** | `CompletableFuture` Callback | Decodes binary RESP2/RESP3 response frames and resolves caller promises | Asynchronous non-blocking return with zero thread pinning |

---

# TRACK 4: PRODUCTION ENGINEERING, MEMORY SIZING & SRE OPERATIONS

## 4.1 Memory Estimation Formula

$$\text{Total RAM} = \Big(\text{Key Count} \times (\text{Avg Key Size} + \text{Avg Value Size} + 96\text{ bytes overhead})\Big) \times 1.3\text{ (Buffer)}$$

### Production Redis Best Practices:
1. **Never use `KEYS *`**: Always use `SCAN` with cursor iteration.
2. **Configure `maxmemory-policy: allkeys-lru`**: Drops least recently accessed cached items rather than throwing OOM crashes.
3. **Monitor `used_memory_rss` vs `used_memory`**: High fragmentation ratio ($>1.5$) indicates memory allocator fragmentation. Run `MEMORY PURGE`.

---

# TRACK 5: WAR ROOM POST-MORTEMS & ROOT CAUSE ANALYSIS (RCAs)

## Incident 1: Production Database Crash via Cache Avalanche

- **Severity:** P0 Outage (Relational database connection pool collapsed)
- **Mean Time to Recovery (MTTR):** 22 minutes
- **Symptoms:** At exactly 00:00 UTC, the PostgreSQL database CPU spiked to 100%, and API gateway returned HTTP 504 Gateway Timeout.
- **Root Cause:** A nightly batch job populated 500,000 product catalog entries with an exact fixed expiration of 24 hours (`entryTtl(Duration.ofHours(24))`). At midnight, all 500,000 keys expired at the same millisecond, sending a thundering herd directly to PostgreSQL.
- **The Permanent Fix:**
  Added random jitter to the TTL configuration:
  ```java
  Duration jitter = Duration.ofSeconds(ThreadLocalRandom.current().nextInt(0, 300));
  redisTemplate.opsForValue().set(key, value, Duration.ofHours(24).plus(jitter));
  ```

---

# TRACK 6: CRACK-THE-INTERVIEW QUESTION BANK (SENIOR & STAFF+ LEVEL)

### 1. How does the Redlock algorithm work and why is it controversial?
Redlock was proposed by Salvatore Sanfilippo (antirez) for distributed locking across $N$ independent master nodes. The client attempts to acquire the lock in all $N$ nodes sequentially within a timeout. If it acquires a majority ($\ge N/2 + 1$), the lock is granted. It was criticized by distributed systems researcher Martin Kleppmann because asynchronous clock drift and GC pauses can invalidate safety guarantees, recommending consensus-based fencing tokens instead.

### 2. How do you implement the Cache-Aside pattern safely with concurrent writes?
To prevent stale reads:
1. **Update the database first**.
2. **Delete (evict) the cache key second**, rather than updating the cache.
3. Subsequent reads will fetch the fresh database value and repopulate the cache.

---

## ⚖️ Spring Data Redis Master Cheat Sheet

| Task / Feature | Production Implementation |
| :--- | :--- |
| **Cache Aside** | `@Cacheable(value = "cacheName", key = "#id")` |
| **Cache Evict** | `@CacheEvict(value = "cacheName", key = "#id")` |
| **Clear All Cache** | `@CacheEvict(value = "cacheName", allEntries = true)` |
| **Distributed Lock** | `redisson.getLock("lock:key").tryLock(3, 10, TimeUnit.SECONDS)` |
| **TTL with Jitter** | `Duration.ofMinutes(10).plusSeconds(random(0, 60))` |
| **Atomic Counter** | `redisTemplate.opsForValue().increment("counter:key")` |
| **Leaderboard ZSet**| `redisTemplate.opsForZSet().add("leaderboard", userId, score)` |

---
[🏠 Back to Home](README.md) | [🍃 Spring Boot Master Guide](spring_master_guide.md) | [🏛️ Spring Data JPA Guide](spring_data_jpa.md)
