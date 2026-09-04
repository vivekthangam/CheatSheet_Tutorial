[🏠 Back to Home](README.md)

# ⚡ Spring Data Redis & Distributed Caching Master Guide

A production-grade engineering handbook for architecting low-latency caching, distributed state, distributed locking, and event streaming using **Spring Data Redis**, **Lettuce**, **Redisson**, **Spring Boot 3.x**, and **Java 17/21**. Covers cache failure patterns (Avalanche, Breakdown, Penetration), Redisson locks, Lua scripting, and Redis Streams.

---

## 📑 Table of Contents

### Track 1: Junior & Entry-Level Foundations

- [🌱 1. Real-World Mental Model (Sticky Note vs Basement Cabinet)](#1-the-real-world-mental-model-the-sticky-note-on-your-monitor-vs-the-file-cabinet-in-the-basement)
- [🧩 2. The 5 Core Building Blocks of Redis & Caching](#2-the-5-core-building-blocks)
- [💻 3. Beginner Code Walkthrough: Spring Boot Declarative Caching](#3-beginner-code-walkthrough-spring-boot-declarative-caching)
- [💥 4. What Happens When Things Break? (The Big 3 Cache Disasters)](#4-what-happens-when-things-break-the-big-3-cache-disasters)
- [⚠️ 5. Top 5 Beginner Mistakes in Production](#5-top-5-beginner-mistakes-in-production)
- [🎯 6. Top 10 Junior Interview Questions (With "ELI5" Answers)](#6-top-10-junior-interview-questions-with-explain-like-im-5-answers)

### Track 2: Advanced Architecture & Distributed Systems

1. [⚙️ 1. Architecture: Lettuce vs Jedis & Connection Configuration](#️-1-architecture-lettuce-vs-jedis--connection-configuration)
2. [📦 2. Production Serialization & RedisTemplate Configuration](#-2-production-serialization--redistemplate-configuration)
3. [⚡ 3. Redis Data Structures in Spring (Strings, Hashes, Lists, ZSets)](#-3-redis-data-structures-in-spring-strings-hashes-lists-zsets)
4. [🏷️ 4. Declarative Caching (@Cacheable, TTLs, SpEL Keys)](#️-4-declarative-caching-cacheable-ttls-spel-keys)
5. [🛡️ 5. Defending Against Cache Avalanche, Breakdown & Penetration](#️-5-defending-against-cache-avalanche-breakdown--penetration)
6. [🔒 6. Distributed Locks: Pure Redis Lua vs Redisson Watchdog](#-6-distributed-locks-pure-redis-lua-vs-redisson-watchdog)
7. [🌊 7. Redis Streams & Consumer Groups](#-7-redis-streams--consumer-groups)
8. [🏭 8. Production Scenarios & War Room Incident Forensics](#-8-production-scenarios--war-room-incident-forensics)
9. [⚖️ 9. Spring Data Redis Master Cheat Sheet](#️-9-spring-data-redis-master-cheat-sheet)

# TRACK 1: THE JUNIOR & ENTRY-LEVEL FOUNDATIONS (ZERO-TO-HERO)

## 1. The Real-World Mental Model (The Sticky Note on your Monitor vs The File Cabinet in the Basement)

### Why Do We Need Redis?
Imagine you work at a customer service desk:
- Every time a customer asks for the Wi-Fi password, you stand up, walk down three flights of stairs into a dusty basement, unlock a giant steel filing cabinet, flip through 50,000 folders, find the password, walk back upstairs, and answer the customer (**Querying a Relational Database / PostgreSQL / MySQL on SSD/HDD**).
  - Time taken: 5 minutes ($50\text{ms} - 200\text{ms}$ in computer time).
  - If 500 customers ask at once, the stairs are jammed and the desk collapses!
- **With Redis (The Sticky Note on your Monitor):**
  - The first time someone asks, you look it up in the basement once and write the password on a **bright yellow sticky note right next to your keyboard (In-Memory RAM)**.
  - The next 50,000 customers get their answer in **1 microsecond** without you ever leaving your chair!
  - But what if your desk runs out of room for sticky notes? Or what if someone changes the Wi-Fi password in the basement? That is what **Cache Management** is all about.

---

### The Cache-Aside Pattern (Everyday Workflow)

```
                            [ CACHE-ASIDE WORKFLOW ]
Client Request ──► Check Redis Cache
                         │
        ┌────────────────┴────────────────┐
        ▼                                 ▼
   [ Cache HIT ]                    [ Cache MISS ]
   Returns data in                  1. Query SQL Database (PostgreSQL)
   < 1 millisecond!                 2. Save result into Redis with TTL (e.g. 10m)
                                    3. Return data to client
```

---

## 2. The 5 Core Building Blocks

| Term | What It Means | Real-World Analogy |
| :--- | :--- | :--- |
| **In-Memory RAM** | Data lives entirely in RAM (nanosecond access), not disk. | A whiteboard in front of your face vs books in the attic. |
| **TTL (Time To Live)** | An automatic expiration timer on a key (e.g. 10 minutes). | Milk expiration date: throws itself into the trash when expired. |
| **Data Types** | Strings, Hashes, Lists, Sets, Sorted Sets (ZSets). | Post-it notes, address books, conveyor belts, unique guest lists, and leaderboards. |
| **`@Cacheable`** | Spring annotation that checks Redis first before executing the method. | Telling your assistant: *"If you already know the answer, don't wake me up."* |
| **Distributed Lock** | A single mutex lock shared across all 20 of your Spring Boot pods. | The single shared key to the office copy machine. |

---

## 3. Beginner Code Walkthrough: Spring Boot Declarative Caching

### Step 1: Cache Configuration (`RedisCacheConfig.java`)
```java
package com.example.redis.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

@Configuration
public class RedisCacheConfig {

    @Bean
    public RedisCacheConfiguration defaultCacheConfig() {
        return RedisCacheConfiguration.defaultCacheConfig()
            // Set default TTL to 10 minutes (Prevents memory leaks!)
            .entryTtl(Duration.ofMinutes(10))
            // Store data as clean human-readable JSON instead of Java binary gibberish!
            .serializeValuesWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new GenericJackson2JsonRedisSerializer()))
            .disableCachingNullValues();
    }
}
```

### Step 2: Using `@Cacheable` and `@CacheEvict` (`ProductService.java`)
```java
package com.example.redis.service;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class ProductService {

    // 1. If product is in Redis, returns instantly! If not, queries DB and caches result.
    @Cacheable(value = "products", key = "#id")
    public Product getProductById(Long id) {
        System.out.println("🐌 Querying SLOW relational database for product: " + id);
        return dbQuery(id);
    }

    // 2. When product price updates, evict (delete) stale cache so next read gets fresh data!
    @CacheEvict(value = "products", key = "#product.id")
    public void updateProduct(Product product) {
        dbUpdate(product);
        System.out.println("🧹 Evicted stale cache for product: " + product.getId());
    }
}
```

---

## 4. What Happens When Things Break? (The Big 3 Cache Disasters)

1. **Cache Avalanche:** 100,000 keys were all saved with the exact same TTL (e.g. 1 hour). Exactly 60 minutes later, **all 100,000 keys expire at the same millisecond**! 50,000 incoming requests hit a cache miss simultaneously and smash into the database, knocking it offline. **Fix:** Add random jitter to TTLs (`10m + random(1-60s)`).
2. **Cache Breakdown (Hotspot Key):** A single viral product (e.g. Taylor Swift concert tickets) is cached. The key expires. 10,000 concurrent threads detect a cache miss at the exact same millisecond and all execute the slow database query together. **Fix:** Use a Mutex Lock (Redisson) or logical soft-expiration.
3. **Cache Penetration:** An attacker sends millions of requests for non-existent IDs (`id = -99999`). Redis has no entry, so every request bypasses the cache and queries the database. **Fix:** Use a **Bloom Filter** or cache empty/null results (`set(id, null, 60s)`).

---

## 5. Top 5 Beginner Mistakes in Production

1. **Running `KEYS *` in Production:** The `KEYS *` command scans every single key in Redis. Because Redis is single-threaded, `KEYS *` blocks all other operations for 5 to 30 seconds, causing production outages! **Fix:** Use `SCAN` instead.
2. **Using Default Java Binary Serialization:** By default, `RedisTemplate` uses `JdkSerializationRedisSerializer`, producing unreadable bytes like `\xac\xed\x00\x05sr\x00...` in `redis-cli` and breaking across Java version upgrades. **Fix:** Use `StringRedisSerializer` and `GenericJackson2JsonRedisSerializer`.
3. **No TTL on Dynamic Keys:** Writing keys like `user:session:XYZ` without setting an expiration time. Eventually, Redis hits `maxmemory` and starts throwing `OOM command not allowed`!
4. **Treating Redis as a Permanent Relational Database:** While Redis supports RDB/AOF persistence, it is not designed to replace PostgreSQL for complex multi-table ACID transactions.
5. **Writing Home-Grown Distributed Locks with `SETNX`:** Beginners use `SETNX` but forget to make the TTL assignment atomic or forget a renewal watchdog. If the service crashes midway, the lock is never freed! **Fix:** Use **Redisson** with its built-in lease renewal watchdog.

---

## 6. Top 10 Junior Interview Questions (With "Explain Like I'm 5" Answers)

### Q1: Why is Redis so fast?
- **ELI5 Answer:** *"Because it reads straight from memory (RAM) instead of spinning disks, and has 1 single dedicated worker who never gets confused by other people talking."*
- **Technical Answer:** *"Redis achieves sub-millisecond latency because: (1) all data is held in-memory (RAM), (2) it uses a non-blocking I/O multiplexing event loop (epoll), and (3) its core execution engine is single-threaded, avoiding OS thread context-switching and lock contention."*

### Q2: What is the difference between Memcached and Redis?
- **ELI5 Answer:** *"Memcached is a simple locker that only holds plain text strings. Redis is a Swiss Army knife that holds lists, dictionaries, leaderboards, and streams."*
- **Technical Answer:** *"Memcached is a multi-threaded, simple key-value store limited to strings. Redis supports rich data structures (Hashes, Lists, Sets, Sorted Sets, Bitmaps, Streams), persistence (RDB snapshots and AOF logs), pub/sub messaging, Lua scripting, and cluster clustering."*

### Q3: What are the two persistence mechanisms in Redis?
- **ELI5 Answer:** *"RDB is taking a photo of your room every hour. AOF is writing down every single time you move a pencil in a notebook."*
- **Technical Answer:** *"RDB (Redis Database) takes periodic point-in-time snapshots of dataset state to a compact `.rdb` file. AOF (Append Only File) logs every write command received by the server sequentially. RDB offers faster restarts; AOF offers maximum durability (minimal data loss)."*

### Q4: What is the difference between Cache Avalanche and Cache Breakdown?
- **ELI5 Answer:** *"Avalanche is when 1,000 different snowballs fall off the mountain at once (many keys expire together). Breakdown is when 1 giant boulder crashes through the fence (1 super-popular key expires)."*
- **Technical Answer:** *"Cache Avalanche occurs when a large batch of cached keys expire simultaneously, overwhelming the database with queries for many resources. Cache Breakdown occurs when a single hot key receives massive concurrent traffic at the exact moment it expires."*

### Q5: What is a Bloom Filter and how does it prevent Cache Penetration?
- **ELI5 Answer:** *"A bouncer with a memory who can instantly say '100% NO, that person is NOT on the list', without checking the heavy binder inside the club."*
- **Technical Answer:** *"A Bloom Filter is a space-efficient probabilistic data structure that tests set membership. It can definitively tell you if an element is NOT present (preventing database queries for invalid IDs), but may have false positives. It never produces false negatives."*

### Q6: How does Redisson Distributed Lock work with the Watchdog?
- **ELI5 Answer:** *"You rent a bike with a timer. A friendly robot dog checks on you every 10 seconds. As long as you are still pedaling, the dog adds 30 more seconds to your timer so nobody steals your bike while you work."*
- **Technical Answer:** *"When a thread acquires an `RLock`, Redisson sets a default 30-second lock lease in Redis via Lua script. An internal background thread (the Watchdog) periodically extends the lock's expiration every 10 seconds as long as the holding thread is alive. If the holding node crashes, the watchdog dies, and the lock auto-expires in 30 seconds."*

### Q7: What are Redis Eviction Policies?
- **ELI5 Answer:** *"The rules for deciding which old toy gets donated when your toy shelf is completely full."*
- **Technical Answer:** *"When Redis reaches `maxmemory`, it evicts keys based on policies: `noeviction` (returns errors on new writes), `allkeys-lru` (evicts least recently used keys), `volatile-lru` (evicts least recently used keys with an expiry), and `allkeys-lfu` (evicts least frequently used keys)."*

### Q8: What is the difference between Redis Pub/Sub and Redis Streams?
- **ELI5 Answer:** *"Pub/Sub is a live radio broadcast: if your radio is turned off, you miss the song forever. Streams is a tape recorder: you can rewind and listen to past songs anytime you want."*
- **Technical Answer:** *"Pub/Sub is at-most-once, fire-and-forget messaging with no persistence; if a subscriber is offline, the message is lost. Redis Streams is an append-only log with persistent storage, message IDs, consumer groups, offset tracking, and acknowledgments (ACKs)."*

### Q9: Why is `KEYS *` dangerous in production?
- **ELI5 Answer:** *"If you ask a librarian to read the titles of all 1,000,000 books out loud, the line behind you stops and nobody else can check out a book until the librarian finishes talking!"*
- **Technical Answer:** *"`KEYS *` performs an $O(N)$ linear scan of the entire keyspace. Because Redis executes commands on a single thread, `KEYS *` blocks all other concurrent requests for seconds or minutes, leading to connection timeouts and cascading service failures."*

### Q10: What is Redis Pipeline and how does it improve performance?
- **ELI5 Answer:** *"Instead of walking to the grocery store 10 times to buy 10 individual apples, you write a shopping list, walk to the store once, and carry all 10 apples home in 1 bag."*
- **Technical Answer:** *"Pipelining allows a client to send multiple commands to the Redis server without waiting for individual replies, and reads all responses in a single step. This dramatically reduces Round-Trip Time (RTT) network overhead, multiplying throughput by 5x to 10x."*

---

# TRACK 2: ADVANCED ARCHITECTURE & DISTRIBUTED SYSTEMS

## ⚙️ 1. Architecture: Lettuce vs Jedis & Connection Configuration

| Driver | Architecture | Thread-Safety | Connection Management | Default in Spring Boot |
| :--- | :--- | :--- | :--- | :--- |
| **Lettuce** | Netty-based, Async & Reactive | **Yes** (Single connection shared across all threads) | Low memory footprint; one TCP channel handles thousands of operations | **Yes (Recommended)** |
| **Jedis** | Blocking I/O | **No** (Thread-unsafe) | Requires heavy `GenericObjectPool` (1 socket per thread) | No (Legacy) |

### Maven Dependencies (`pom.xml`)
```xml
<dependencies>
    <!-- Spring Boot 3 Redis Starter (uses Lettuce by default) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis</artifactId>
    </dependency>

    <!-- Redisson for Production Distributed Locks & Advanced Structures -->
    <dependency>
        <groupId>org.redisson</groupId>
        <artifactId>redisson-spring-boot-starter</artifactId>
        <version>3.35.0</version>
    </dependency>

    <!-- Jackson for JSON Serialization -->
    <dependency>
        <groupId>com.fasterxml.jackson.datatype</groupId>
        <artifactId>jackson-datatype-jsr310</artifactId>
    </dependency>
</dependencies>
```

---

## 📦 2. Production Serialization & RedisTemplate Configuration

> [!CAUTION]
> **Never use default `JdkSerializationRedisSerializer`!**
> 1. It serializes objects into unreadable binary blobs (`\xac\xed\x00\x05...`).
> 2. It breaks compatibility if class bytecode changes (`InvalidClassException`).
> 3. It exposes Java Deserialization Remote Code Execution (RCE) vulnerabilities.

### Type-Safe JSON `RedisTemplate` Configuration
```java
package com.example.redis.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        // Polymorphic typing so Jackson knows how to deserialize subclasses
        objectMapper.activateDefaultTyping(
            LaissezFaireSubTypeValidator.instance,
            ObjectMapper.DefaultTyping.NON_FINAL,
            JsonTypeInfo.As.PROPERTY
        );

        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);
        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Keys stored as human-readable Strings
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        // Values stored as human-readable, typed JSON
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }
}
```

---

## ⚡ 3. Redis Data Structures in Spring (Strings, Hashes, Lists, ZSets)

```java
package com.example.redis.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;

@Service
public class RedisDataStructuresService {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisDataStructuresService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // 1. Strings: ValueOperations (KeyValue with TTL)
    public void setSessionToken(String userId, String token, Duration ttl) {
        redisTemplate.opsForValue().set("session:" + userId, token, ttl);
    }

    // 2. Hashes: HashOperations (Fast lookup of individual fields without reading whole object)
    public void updateUserField(String userId, String field, Object value) {
        redisTemplate.opsForHash().put("user:" + userId, field, value);
    }

    // 3. Lists: ListOperations (FIFO Queue / LIFO Stack)
    public void pushAuditTask(String taskPayload) {
        redisTemplate.opsForList().rightPush("queue:audit_tasks", taskPayload);
    }

    // 4. Sorted Sets (ZSet): Leaderboards & Priority Ranking
    public void addLeaderboardScore(String player, double score) {
        redisTemplate.opsForZSet().add("leaderboard:global", player, score);
    }

    public Set<ZSetOperations.TypedTuple<Object>> getTopPlayers(int count) {
        // Returns top players descending by score (O(log(N) + M))
        return redisTemplate.opsForZSet().reverseRangeWithScores("leaderboard:global", 0, count - 1);
    }
}
```

---

## 🏷️ 4. Declarative Caching (@Cacheable, TTLs, SpEL Keys)

### Multi-TTL `CacheManager` Configuration
In production, different business domains require different TTLs (e.g. Reference lookups = 24h, Stock prices = 30s).

```java
package com.example.redis.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(10)) // Default TTL
            .disableCachingNullValues()
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));

        // Custom TTL policies per cache name
        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();
        cacheConfigs.put("products", defaultConfig.entryTtl(Duration.ofHours(1)));
        cacheConfigs.put("exchange_rates", defaultConfig.entryTtl(Duration.ofSeconds(30)));
        cacheConfigs.put("permissions", defaultConfig.entryTtl(Duration.ofMinutes(5)));

        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(defaultConfig)
            .withInitialCacheConfigurations(cacheConfigs)
            .build();
    }
}
```

### Declarative Cache Annotations in Service
```java
@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // sync = true prevents Cache Stampede / Breakdown by locking locally on cache miss!
    @Cacheable(value = "products", key = "#id", sync = true)
    public Product getProductById(Long id) {
        return productRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Product not found: " + id));
    }

    // Updates cache when entity is modified
    @CachePut(value = "products", key = "#product.id")
    public Product updateProduct(Product product) {
        return productRepository.save(product);
    }

    // Evicts cache when entity is deleted
    @CacheEvict(value = "products", key = "#id")
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
}
```

---

## 🛡️ 5. Defending Against Cache Avalanche, Breakdown & Penetration

| Disaster Pattern | Mechanism / Root Cause | Production Defense Strategy |
| :--- | :--- | :--- |
| **Cache Avalanche** | Thousands of cached keys have the exact same TTL (e.g. 1 hour). They all expire simultaneously, sending a sudden tidal wave of requests directly to the database. | **TTL Jitter (Randomization):** Add a random 10-20% delta to TTLs (`Duration.ofMinutes(60 + random.nextInt(10))`). |
| **Cache Breakdown (Stampede)** | A single ultra-hot key (e.g., iPhone launch event) expires. 10,000 concurrent threads miss cache at the exact same millisecond and all query the DB. | **Mutex / Distributed Lock:** Enable `@Cacheable(..., sync = true)` or use a Redisson mutex. |
| **Cache Penetration** | Malicious users query non-existent IDs (e.g. `id = -99999`). Cache never contains it, so every request hits the relational DB. | **Bloom Filter** to verify key existence before DB query, or cache empty null objects with short TTL (2 min). |

---

## 🔒 6. Distributed Locks: Pure Redis Lua vs Redisson Watchdog

### 6.1 The Risk with Naive Redis Locks
If an instance acquires a lock with `SET key token NX PX 5000` (5 second TTL), but a Full GC pause or slow external API causes the thread to take 7 seconds, the lock expires automatically. Another instance acquires the lock, leading to **split-brain state corruption**.

### 6.2 The Production Solution: Redisson Watchdog Auto-Renewal
Redisson automatically extends lock lease time every 10 seconds as long as the owning thread is alive.

```java
package com.example.redis.lock;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class OrderSettlementService {

    private final RedissonClient redissonClient;

    public OrderSettlementService(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    public void settleOrder(String orderId) {
        String lockKey = "lock:order:" + orderId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // Wait up to 5 seconds to acquire; hold lock with automatic Watchdog renewal
            boolean acquired = lock.tryLock(5, -1, TimeUnit.SECONDS);
            if (!acquired) {
                throw new IllegalStateException("Could not acquire lock for order: " + orderId + ". Another node is settling.");
            }

            // Critical Section: Only one node in the entire cluster can execute this
            processPaymentAndInventoryDeduction(orderId);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Lock acquisition interrupted", e);
        } finally {
            // Always release lock in finally block if held by current thread
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private void processPaymentAndInventoryDeduction(String orderId) {
        // Business logic
    }
}
```

---

## 🌊 7. Redis Streams & Consumer Groups

Redis Streams provide an append-only log with Kafka-like consumer groups, persistence, and message acknowledgments (`XACK`).

```java
package com.example.redis.stream;

import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class EventStreamProducer {

    private final RedisTemplate<String, Object> redisTemplate;

    public EventStreamProducer(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public record PaymentCompletedEvent(String paymentId, String customerId, double amount) {}

    public void publishPaymentEvent(PaymentCompletedEvent event) {
        ObjectRecord<String, PaymentCompletedEvent> record = StreamRecords.newRecord()
            .ofObject(event)
            .withStreamKey("stream:payment-events");

        redisTemplate.opsForStream().add(record);
    }
}
```

---

## 🏭 8. Production Scenarios & War Room Incident Forensics

### Scenario 1: Redis `OOM command not allowed when used memory > 'maxmemory'`
- **Root Cause:** Redis server filled up RAM because keys lacked TTLs, and `maxmemory-policy` was set to `noeviction`.
- **The Fix:**
  1. Configure eviction policy in `redis.conf`:
     `maxmemory-policy allkeys-lru` (or `volatile-lru`).
  2. Enforce strict TTLs on all cache writes in Spring via `RedisCacheConfiguration`.

### Scenario 2: Distributed Sliding Window Rate Limiter via Lua Script
To prevent race conditions during rate-limiting checks, execute the check and increment atomically via Redis Lua.

```java
package com.example.redis.ratelimit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DistributedRateLimiter {

    private final StringRedisTemplate redisTemplate;
    private final DefaultRedisScript<Long> rateLimitScript;

    public DistributedRateLimiter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;

        String lua = """
            local key = KEYS[1]
            local limit = tonumber(ARGV[1])
            local current = tonumber(redis.call('get', key) or "0")

            if current + 1 > limit then
                return 0
            else
                redis.call('incrby', key, 1)
                if current == 0 then
                    redis.call('expire', key, ARGV[2])
                end
                return 1
            end
            """;
        this.rateLimitScript = new DefaultRedisScript<>(lua, Long.class);
    }

    public boolean isAllowed(String apiKey, int maxRequestsPerMinute) {
        String key = "ratelimit:" + apiKey;
        Long result = redisTemplate.execute(rateLimitScript, List.of(key), String.valueOf(maxRequestsPerMinute), "60");
        return result != null && result == 1L;
    }
}
```

---

## ⚖️ 9. Spring Data Redis Master Cheat Sheet

| Operation / Need | Syntax Example |
| :--- | :--- |
| **String Set with TTL** | `redisTemplate.opsForValue().set("key", value, Duration.ofMinutes(5))` |
| **Atomic Increment** | `redisTemplate.opsForValue().increment("counter", 1L)` |
| **Hash Put Field** | `redisTemplate.opsForHash().put("user:1", "email", "test@domain.com")` |
| **ZSet Add Score** | `redisTemplate.opsForZSet().add("leaderboard", "player1", 1500.0)` |
| **Lock with Redisson** | `RLock lock = redisson.getLock("key"); lock.tryLock(5, -1, SECONDS);` |
| **Prevent Stampede** | `@Cacheable(value = "data", key = "#id", sync = true)` |
| **Custom SpEL Key** | `@Cacheable(value = "users", key = "#root.methodName + ':' + #id")` |
| **Stream Add** | `redisTemplate.opsForStream().add(record)` |

---
[🏠 Back to Home](README.md)
