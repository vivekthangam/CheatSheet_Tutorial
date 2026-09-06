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

```
┌────────────────────────────────────────────────────────────────────────┐
│                          CACHE-ASIDE PATTERN                           │
│                                                                        │
│   Client Request ──► Check Redis Cache                                 │
│                            │                                           │
│           ┌────────────────┴────────────────┐                          │
│           ▼                                 ▼                          │
│     [ Cache HIT ]                     [ Cache MISS ]                   │
│     Returns in <1ms!                  1. Query PostgreSQL              │
│                                       2. Save in Redis (with TTL)      │
│                                       3. Return data to client         │
└────────────────────────────────────────────────────────────────────────┘
```

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

## 2.1 Production Redis Configuration & Clean JSON Serialization

```java
package com.example.redis.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
@EnableCaching
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        // Configure hardened ObjectMapper for JSON serialization
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        mapper.activateDefaultTyping(
            BasicPolymorphicTypeValidator.builder().allowIfBaseType(Object.class).build(),
            ObjectMapper.DefaultTyping.NON_FINAL,
            JsonTypeInfo.As.PROPERTY
        );
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(mapper);

        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory factory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(10)) // Default 10 min TTL
            .disableCachingNullValues()
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));

        return RedisCacheManager.builder(factory)
            .cacheDefaults(config)
            // Per-cache specific TTL overrides
            .withCacheConfiguration("products", config.entryTtl(Duration.ofHours(1)))
            .withCacheConfiguration("exchangeRates", config.entryTtl(Duration.ofSeconds(30)))
            .build();
    }
}
```

---

## 2.2 Distributed Locks with Redisson (Reentrant & Watchdog)

```java
package com.example.redis.lock;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class FlashSaleBookingService {

    private final RedissonClient redisson;

    public FlashSaleBookingService(RedissonClient redisson) {
        this.redisson = redisson;
    }

    public boolean purchaseLimitedStock(String productId, int quantity) {
        String lockKey = "lock:product:" + productId;
        RLock lock = redisson.getLock(lockKey);

        try {
            // Wait up to 3 seconds to acquire lock; lock holds for 10 seconds
            boolean isLocked = lock.tryLock(3, 10, TimeUnit.SECONDS);
            if (!isLocked) {
                return false; // Could not acquire lock, system busy
            }

            // Critical Section: Decrement stock safely
            return executeInventoryDecrement(productId, quantity);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock(); // Safe unlock
            }
        }
    }

    private boolean executeInventoryDecrement(String productId, int qty) {
        // Business logic...
        return true;
    }
}
```

---

## 2.3 Atomic Rate Limiting with Redis Lua Scripting

```java
package com.example.redis.ratelimit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class RedisRateLimiterService {

    private final StringRedisTemplate redisTemplate;

    // Atomic Token Bucket Lua script
    private static final String LUA_RATE_LIMIT = """
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

    public RedisRateLimiterService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean isAllowed(String apiKey, int maxRequests, int windowSeconds) {
        String key = "ratelimit:" + apiKey;
        DefaultRedisScript<Long> script = new DefaultRedisScript<>(LUA_RATE_LIMIT, Long.class);

        Long result = redisTemplate.execute(
            script,
            Collections.singletonList(key),
            String.valueOf(maxRequests),
            String.valueOf(windowSeconds)
        );

        return result != null && result == 1L;
    }
}
```

---

# TRACK 3: FRAMEWORK INTERNALS & LETTUCE NETTY PIPELINE

## 3.1 Lettuce Connection Pipeline & Asynchronous Dispatch

```
┌────────────────────────────────────────────────────────────────────────┐
│                        LETTUCE CLIENT ARCHITECTURE                     │
│                                                                        │
│   Java Thread 1 ──► [ Command Dispatcher ]                             │
│   Java Thread 2 ──►          │                                         │
│   Java Thread 3 ──►          ▼                                         │
│                    [ Shared Netty EventLoop ]                          │
│                              │                                         │
│                              ▼ Multiplexed single TCP Socket           │
│                    [ Redis Server Instance ]                           │
│                              │                                         │
│                              ▼ Asynchronous Response Frame             │
│                    [ CompletableFuture callback ]                      │
└────────────────────────────────────────────────────────────────────────┘
```

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
