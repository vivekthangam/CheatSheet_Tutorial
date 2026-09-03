[🏠 Back to Home](README.md)

# ⚡ Spring Data Redis & Distributed Caching Master Guide

A production-grade engineering handbook for architecting low-latency caching, distributed state, distributed locking, and event streaming using **Spring Data Redis**, **Lettuce**, **Redisson**, **Spring Boot 3.x**, and **Java 17/21**. Covers cache failure patterns (Avalanche, Breakdown, Penetration), Redisson locks, Lua scripting, and Redis Streams.

---

## 📑 Table of Contents
1. [🧠 Zero-to-Hero Mental Model: In-Memory Speed vs Distributed Consistency](#-zero-to-hero-mental-model-in-memory-speed-vs-distributed-consistency)
2. [⚙️ 1. Architecture: Lettuce vs Jedis & Connection Configuration](#️-1-architecture-lettuce-vs-jedis--connection-configuration)
3. [📦 2. Production Serialization & RedisTemplate Configuration](#-2-production-serialization--redistemplate-configuration)
4. [⚡ 3. Redis Data Structures in Spring (Strings, Hashes, Lists, ZSets)](#-3-redis-data-structures-in-spring-strings-hashes-lists-zsets)
5. [🏷️ 4. Declarative Caching (@Cacheable, TTLs, SpEL Keys)](#️-4-declarative-caching-cacheable-ttls-spel-keys)
6. [🛡️ 5. Defending Against Cache Avalanche, Breakdown & Penetration](#️-5-defending-against-cache-avalanche-breakdown--penetration)
7. [🔒 6. Distributed Locks: Pure Redis Lua vs Redisson Watchdog](#-6-distributed-locks-pure-redis-lua-vs-redisson-watchdog)
8. [🌊 7. Redis Streams & Consumer Groups](#-7-redis-streams--consumer-groups)
9. [🏭 8. Production Scenarios & War Room Incident Forensics](#-8-production-scenarios--war-room-incident-forensics)
10. [⚖️ 9. Spring Data Redis Master Cheat Sheet](#️-9-spring-data-redis-master-cheat-sheet)

---

## 🧠 Zero-to-Hero Mental Model: In-Memory Speed vs Distributed Consistency

```
┌──────────────────────────────────────────────────────────────────────────────────┐
│                            SPRING BOOT APPLICATION CLUSTER                       │
│                                                                                  │
│    [ Instance 1 ]                [ Instance 2 ]                [ Instance 3 ]    │
│           │                             │                             │          │
│           └─────────────────────────────┼─────────────────────────────┘          │
│                                         ▼ (Netty Non-Blocking TCP)               │
│                        ┌─────────────────────────────────┐                       │
│                        │       REDIS CLUSTER / PRIMARY   │                       │
│                        │   Sub-Millisecond In-Memory RAM │                       │
│                        └─────────────────────────────────┘                       │
│                                         │                                        │
│           ┌─────────────────────────────┼─────────────────────────────┐          │
│           ▼                             ▼                             ▼          │
│     [ L1 Cache ]               [ Distributed Lock ]           [ Rate Limiter ]   │
│  @Cacheable(products)          Redisson RLock(order-99)       Sliding Window Log │
└──────────────────────────────────────────────────────────────────────────────────┘
```

Redis serves three primary architectural roles in a Spring ecosystem:
1. **Low-Latency Cache:** Eliminates redundant database reads, serving responses in $< 1\text{ ms}$.
2. **Distributed Synchronization:** Coordinates state across multiple horizontal Spring Boot microservice instances (e.g. locks, leader election, rate limiters).
3. **Event Transport:** High-throughput async communication via Redis Pub/Sub and Redis Streams.

---

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
