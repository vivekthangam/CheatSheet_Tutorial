[🏠 Back to Home](README.md) | [⚡ Spring Redis Master Guide](spring_redis.md) | [🍃 Spring Boot Master Guide](spring_master_guide.md)

# ⚡ Spring Data Redis & Caching: 50+ Real-World Production Interview Scenarios Master Guide

[![Redis](https://img.shields.io/badge/Redis-7.2%2B-red.svg?style=for-the-badge&logo=redis)](https://redis.io/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3%2B-brightgreen.svg?style=for-the-badge&logo=springboot)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17%20%2F%2021%20LTS-orange.svg?style=for-the-badge&logo=openjdk)](https://www.oracle.com/java/)
[![Level](https://img.shields.io/badge/Tier-1%20Panels-Staff%20%2F%20Principal-red.svg?style=for-the-badge)](https://github.com/)

An exhaustive, battle-tested compilation of **real-world production interview scenarios** covering Spring Data Redis, Lettuce Netty pipelining, Redisson distributed locks, Watchdog lease extensions, Lua scripting, Redis Streams consumer groups, cache stampede mutexes, and cache avalanche jitter.

Every scenario strictly follows the **5-Part Tier-1 Product Engineering Format**:
1. **Exact Question Asked by Tier-1 Product Panels (with detailed scenario context)**
2. **What the Interviewer Evaluates Under the Surface (mental criteria, low-level runtime knowledge)**
3. **Standout Technical Answer (deep runtime mechanics, low-level socket/memory details, zero fluff)**
4. **Follow-Up Trap Question & Winning Answer (catching surface memorizers)**
5. **Production Sample Code with Detailed Technical Explanation & Pitfall Avoidance**

---

## 📑 Category Navigation

- [Category 1: Lettuce Netty Pipelining vs Jedis Thread Pools (Q1 – Q10)](#category-1-lettuce-netty-pipelining-vs-jedis-thread-pools)
- [Category 2: Distributed Locking with Redisson & The Watchdog Algorithm (Q11 – Q20)](#category-2-distributed-locking-with-redisson--the-watchdog-algorithm)
- [Category 3: Cache Failure Dynamics: Stampede, Avalanche & Penetration (Q21 – Q30)](#category-3-cache-failure-dynamics-stampede-avalanche--penetration)
- [Category 4: Redis Streams, Consumer Groups & Pending Entries Lists (Q31 – Q40)](#category-4-redis-streams-consumer-groups--pending-entries-lists)
- [Category 5: Atomic Lua Scripting & Memory Eviction Policies (Q41 – Q50)](#category-5-atomic-lua-scripting--memory-eviction-policies)
- [Category 6: Production War Room Incidents & Outage Forensics (Q51 – Q60)](#category-6-production-war-room-incidents--outage-forensics)

---

# Category 1: Lettuce Netty Pipelining vs Jedis Thread Pools

### Q1: Why does Spring Data Redis default to Lettuce over Jedis, and what causes Lettuce Socket Timeout freezes under Thread Starvation?
- **Scenario Context:** Under a surge of 30,000 req/sec, Lettuce Redis operations begin throwing `RedisCommandTimeoutException: Command timed out after 3000ms`. When checking the Redis server CPU, it is idle at 5%! The bottleneck is entirely on the client application side.
- **What the Interviewer Evaluates:** Threading models: Jedis connection-per-thread model vs Lettuce non-blocking multiplexed Netty channels, and Netty EventLoop thread starvation.
- **Standout Technical Answer:**
  - **Jedis Architecture (Blocking I/O):**
    - Jedis uses standard Java blocking sockets (`java.net.Socket`).
    - It is **thread-unsafe**; each concurrent thread requires a dedicated TCP socket borrowed from a `JedisPool`.
    - If you have 500 worker threads, you need 500 open TCP connections to Redis, wasting server file descriptors and memory.
  - **Lettuce Architecture (Non-Blocking Multiplexing):**
    - Lettuce is built on **Netty**.
    - Multiple application threads share a **single TCP connection** through Netty's channel pipelining, issuing commands concurrently without blocking.
  - **The Socket Timeout Root Cause:**
    - By default, Lettuce shares the **Netty EventLoopGroup** or allocates a small default pool (`DefaultEventLoopGroup`).
    - If a developer executes CPU-intensive computation or blocking code inside a reactive Redis callback or on the shared EventLoop, **the Netty I/O thread is starved!**
    - Network packets arrive in the OS TCP socket buffer, but the frozen Netty event loop thread cannot read and decode them, causing `RedisCommandTimeoutException` while the Redis server sits idle!
  - **The Production Fix:**
    Configure a dedicated `ClientResources` bean allocating an isolated `EventLoopGroup` for Lettuce, and enable **connection pooling** for high-volume transactions:
    `LettucePoolingClientConfiguration.builder().poolConfig(poolConfig).build();`
- **Follow-Up Trap:** *"Is Lettuce connection pooling necessary if Lettuce already multiplexes commands over a single connection?"*
  - *Winning Answer:* "Yes! Under heavy write traffic or slow network bandwidth, multiplexing hundreds of threads over a single TCP socket creates Head-of-Line (HoL) blocking on the OS socket write buffer. A pool of 8 to 16 Lettuce connections allows multiple parallel TCP channels, eliminating socket congestion while keeping connection counts minimal compared to Jedis!"
- **Production Sample Code & Walkthrough:**
```java
@Configuration
public class OptimizedLettuceRedisConfig {

    @Bean(destroyMethod = "shutdown")
    public ClientResources clientResources() {
        return DefaultClientResources.builder()
            .ioThreadPoolSize(Runtime.getRuntime().availableProcessors() * 2)
            .computationThreadPoolSize(Runtime.getRuntime().availableProcessors() * 2)
            .build();
    }

    @Bean
    public LettuceConnectionFactory redisConnectionFactory(ClientResources clientResources) {
        GenericObjectPoolConfig<?> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMaxTotal(16);
        poolConfig.setMaxIdle(8);
        poolConfig.setMinIdle(4);

        LettucePoolingClientConfiguration clientConfig = LettucePoolingClientConfiguration.builder()
            .clientResources(clientResources)
            .poolConfig(poolConfig)
            .commandTimeout(Duration.ofSeconds(2))
            .build();

        return new LettuceConnectionFactory(new RedisStandaloneConfiguration("localhost", 6379), clientConfig);
    }
}
```

---

# Category 2: Distributed Locking with Redisson & The Watchdog Algorithm

### Q2: What causes Distributed Lock Corruption during JVM Garbage Collection (STW) pauses, and how does Redisson's Watchdog prevent it?
- **Scenario Context:** In a flight booking system, Pod 1 acquires a distributed lock: `SET lock:flight:100 uuid NX PX 5000`. Pod 1 encounters a 6-second Stop-The-World (STW) GC pause. During the pause, the lock expires. Pod 2 acquires the lock and sells the seat. Pod 1 wakes up and also sells the seat. Double booking occurs!
- **What the Interviewer Evaluates:** Split-brain concurrency, lock expiration mechanics, fencing tokens, and the Redisson Watchdog background heartbeat.
- **Standout Technical Answer:**
  - When Pod 1 acquires a lock with a hard-coded 5-second TTL, Redis evicts the key as soon as the timer elapses.
  - While Pod 1 is frozen in an OS context switch or JVM GC pause, it cannot execute work, but Redis continues counting down.
  - When Pod 2 sees the key is absent, it acquires the lock and enters the critical section.
  - When Pod 1 wakes up, it resumes execution **believing it still holds the lock**, leading to concurrent writes (**Data Corruption**).
  - Furthermore, when Pod 1 finishes, calling `DEL lock:flight:100` **deletes Pod 2's active lock!**
  - **The Redisson Watchdog Solution:**
    1. Do NOT specify a `leaseTime`. Call `lock.lock()` or `lock.tryLock(wait, unit)`.
    2. Redisson acquires the lock with a default 30-second lease (`lockWatchdogTimeout`).
    3. Redisson launches a background daemon timer using Netty's `HashedWheelTimer`.
    4. Every $\frac{1}{3}\text{rd}$ of the watchdog timeout (every **10 seconds**), the watchdog issues an atomic Lua script extending the lock's expiration back to 30 seconds.
    5. As long as Pod 1's JVM and thread are alive and running, the lock **never expires**!
    6. If Pod 1 crashes, the watchdog dies, and Redis releases the lock automatically after 30 seconds.
- **Follow-Up Trap:** *"Why does passing `lock.lock(10, TimeUnit.SECONDS)` completely disable the Watchdog?"*
  - *Winning Answer:* "If you pass an explicit `leaseTime` parameter, Redisson assumes you intentionally want a hard deadline and explicitly **disables the automatic background lease renewal timer**. Never pass an explicit `leaseTime` if tasks could take longer than anticipated!"
- **Production Sample Code & Walkthrough:**
```java
@Service
public class FlightBookingService {

    private final RedissonClient redisson;

    public FlightBookingService(RedissonClient redisson) {
        this.redisson = redisson;
    }

    public boolean bookSeat(String flightId, String seatNumber) {
        RLock lock = redisson.getLock("lock:flight:" + flightId + ":" + seatNumber);

        try {
            // tryLock(waitTime, unit): NO leaseTime specified -> WATCHDOG IS ACTIVE!
            boolean acquired = lock.tryLock(5, TimeUnit.SECONDS);
            if (!acquired) {
                return false; // Lock busy
            }

            // Critical section is protected against STW GC expiration
            return executeBooking(flightId, seatNumber);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock(); // Safe unlock via atomic internal Lua script
            }
        }
    }

    private boolean executeBooking(String flight, String seat) {
        // Business logic...
        return true;
    }
}
```

---

# Category 3: Cache Failure Dynamics: Stampede, Avalanche & Penetration

### Q3: How do you architect a Multi-Layer Cache Defense against Cache Stampede, Cache Avalanche, and Cache Penetration in Spring Boot 3?
- **Scenario Context:** At midnight, a popular Black Friday product's cache key expires. Within 500 milliseconds, 20,000 concurrent requests miss the cache and hit the PostgreSQL database simultaneously, causing database CPU to hit 100% and taking the site down (**The Thundering Herd**).
- **What the Interviewer Evaluates:**
  - **Cache Stampede (Thundering Herd)**: Millions hitting 1 expired key.
  - **Cache Avalanche**: Thousands of keys expiring at the exact same second.
  - **Cache Penetration**: Malicious queries for non-existent IDs bypassing cache.
- **Standout Technical Answer:**
  1. **Defeating Cache Stampede (Mutex / Probabilistic Early Re-computation):**
     - Use Spring's `@Cacheable(sync = true)`. This installs an in-memory lock (`ConcurrentHashMap` mutex) per cache key inside the JVM. When the key expires, **only 1 thread is allowed to query the database**; all other 19,999 threads wait and read the freshly computed value.
  2. **Defeating Cache Avalanche (Randomized Expiration Jitter):**
     - Never configure a static TTL (e.g. `Duration.ofHours(1)`).
     - Add random jitter to key expiration:
       $$\text{TTL} = \text{Base TTL} + \text{random}(0, 300\text{ seconds})$$
     - Keys expire smoothly across a 5-minute window rather than collapsing at once.
  3. **Defeating Cache Penetration (Bloom Filter + Null Caching):**
     - Pass incoming IDs through a **Redis Bloom Filter** (`RBloomFilter`). If the filter says the ID does not exist, reject the request immediately without touching cache or DB!
     - If an ID is queried that genuinely returns null from the database, cache a sentinel null value: `cache.put(key, NULL_SENTINEL, 60s)`.
- **Follow-Up Trap:** *"Does `@Cacheable(sync = true)` prevent cache stampede across MULTIPLE Kubernetes pods?"*
  - *Winning Answer:* "No! `@Cacheable(sync = true)` synchronizes threads within a single JVM instance. If you have 50 Kubernetes pods, 50 database queries will still execute (one per pod). To enforce a cluster-wide single query, use a distributed Redis lock or probabilistic early expiration (XFetch algorithm)."
- **Production Sample Code & Walkthrough:**
```java
@Service
public class ProductCatalogService {

    private final ProductRepository productRepository;
    private final RedissonClient redisson;

    public ProductCatalogService(ProductRepository productRepository, RedissonClient redisson) {
        this.productRepository = productRepository;
        this.redisson = redisson;
    }

    // sync = true prevents cache stampede within the JVM!
    @Cacheable(value = "products", key = "#productId", sync = true)
    public ProductDto getProductDetails(Long productId) {
        // Bloom filter check prevents cache penetration
        RBloomFilter<Long> bloomFilter = redisson.getBloomFilter("bloom:product:ids");
        if (!bloomFilter.contains(productId)) {
            throw new EntityNotFoundException("Product ID does not exist!");
        }

        return productRepository.findById(productId)
            .map(p -> new ProductDto(p.getId(), p.getName(), p.getPrice()))
            .orElse(null);
    }
}
```

```java
// Adding Expiration Jitter Configuration
@Bean
public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {
    return builder -> builder
        .withCacheConfiguration("products",
            RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(60).plusSeconds(ThreadLocalRandom.current().nextInt(0, 300))));
}
```

---

# Category 4: Redis Streams, Consumer Groups & Pending Entries Lists

### Q4: How do Redis Streams Consumer Groups achieve Message Acknowledgments, and how do you recover abandoned messages using `XPENDING` and `XCLAIM`?
- **Scenario Context:** An asynchronous order processing pipeline uses Redis Streams (`XADD`). Worker Pod 3 consumes 50 orders, but crashes midway through execution. The messages remain unacknowledged, causing orders to freeze in an unprocessed state.
- **What the Interviewer Evaluates:** Redis Streams data structures, `XREADGROUP`, Pending Entries List (PEL), and consumer recovery using `XPENDING` and `XCLAIM` / `XAUTOCLAIM`.
- **Standout Technical Answer:**
  - Redis Streams provide Kafka-like append-only logs with consumer group semantics.
  - When a worker in a consumer group reads messages via `XREADGROUP`:
    1. Redis delivers the message to that worker.
    2. Redis records the message ID in the group's **Pending Entries List (PEL)**. The message is now "in-flight" and will not be delivered to other consumers.
    3. When the worker finishes, it must call **`XACK stream group id`** to remove the message from the PEL.
  - **Recovering Orphaned / Abandoned Messages:**
    - If Worker 3 crashes before calling `XACK`, its messages remain locked in the PEL forever.
    - A secondary supervisor thread periodically inspects the PEL using **`XPENDING`**:
      `XPENDING order_stream order_group - + 10`
    - If a message has been idle in the PEL longer than 60 seconds (idle time $>60,000\text{ms}$), a healthy worker invokes **`XAUTOCLAIM`** (or `XCLAIM`):
      `XAUTOCLAIM order_stream order_group worker_healthy 60000 0-0 COUNT 10`
    - Ownership of the message is transferred to `worker_healthy`, which reprocesses and acknowledges it.
- **Follow-Up Trap:** *"What is the difference between `StreamMessageListenerContainer` in Spring Data Redis and `@KafkaListener` in Spring Kafka?"*
  - *Winning Answer:* "`StreamMessageListenerContainer` polls Redis using `XREADGROUP` in a non-blocking loop managed by a Spring `TaskExecutor`. However, unlike Kafka which auto-commits offsets by default, Redis Streams requires an explicit `container.acknowledge(...)` call to issue `XACK`, otherwise messages accumulate in the PEL indefinitely!"
- **Production Sample Code & Walkthrough:**
```java
@Component
public class StreamOrphanRecoverySupervisor {

    private final StringRedisTemplate redisTemplate;

    public StreamOrphanRecoverySupervisor(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // Runs every 30 seconds to reclaim abandoned orders from crashed pods
    @Scheduled(fixedDelay = 30000)
    public void reclaimAbandonedOrders() {
        StreamOperations<String, Object, Object> streamOps = redisTemplate.opsForStream();

        // Check for pending entries idle > 60 seconds
        PendingMessages pending = streamOps.pending(
            "order_stream",
            "order_group",
            Range.unbounded(),
            20
        );

        for (PendingMessage msg : pending) {
            if (msg.getElapsedTimeSinceLastDelivery().getSeconds() > 60) {
                // Reclaim message to current supervisor worker!
                List<MapRecord<String, Object, Object>> claimed = streamOps.claim(
                    "order_stream",
                    "order_group",
                    "supervisor-worker-1",
                    Duration.ofSeconds(60),
                    msg.getId()
                );
                reprocessOrders(claimed);
            }
        }
    }

    private void reprocessOrders(List<MapRecord<String, Object, Object>> records) {
        // Business reprocessing and XACK...
    }
}
```

---

# Category 5: Atomic Lua Scripting & Memory Eviction Policies

### Q5: What happens when Redis memory exceeds `maxmemory`, and how does `allkeys-lru` differ from `volatile-lfu` under high eviction pressure?
- **Scenario Context:** A Redis cluster holding 16GB of cache data experiences a surge. Memory reaches 100%. Write commands start failing with `OOM command not allowed when used memory > 'maxmemory'`.
- **What the Interviewer Evaluates:** Redis memory eviction algorithms, LRU (Least Recently Used) vs LFU (Least Frequently Used), and avoiding OOM write rejections.
- **Standout Technical Answer:**
  - When Redis reaches `maxmemory`, its behavior is governed by `maxmemory-policy`:
    1. **`noeviction` [Default]**: Rejects all write commands with an OOM error. Reads continue working. Catastrophic for cache services.
    2. **`allkeys-lru`**: Evicts the least recently accessed keys across the entire dataset. Ideal when keys follow a power-law (Pareto) access distribution.
    3. **`volatile-lru`**: Evicts least recently accessed keys *only among keys that have an expiration (TTL) set*.
    4. **`allkeys-lfu`**: Evicts keys with the **lowest access frequency** (logarithmic access counter), protecting frequently accessed items from eviction even if not touched for a short period.
    5. **`volatile-ttl`**: Evicts keys with the shortest remaining TTL first.
  - **Memory Eviction Approximation:**
    Redis does not maintain a true global LRU linked list (which would require 24 bytes of pointer overhead per key). Instead, it uses an **approximated LRU algorithm**: it samples 5 random keys (configurable via `maxmemory-samples 5`) and evicts the best candidate among the sample.
- **Follow-Up Trap:** *"Why can a long-running Redis Lua script block the entire server and cause clients to disconnect?"*
  - *Winning Answer:* "Redis is single-threaded for command execution! When a Lua script runs (`EVAL`), Redis executes the entire script atomically without interleaving other commands. If a Lua script contains an accidental infinite loop or takes 3 seconds, all other Redis clients are blocked, triggering client socket timeouts!"
- **Production Sample Code & Walkthrough:**
```java
@Component
public class AtomicRateLimiter {

    private final StringRedisTemplate redisTemplate;

    // Atomic Token Bucket / Sliding Window in a single round-trip Lua Script!
    private static final String LUA_RATE_LIMIT = """
        local key = KEYS[1]
        local limit = tonumber(ARGV[1])
        local current = tonumber(redis.call('get', key) or "0")
        if current + 1 > limit then
            return 0
        else
            redis.call('incrby', key, 1)
            if current == 0 then
                redis.call('expire', key, 60)
            end
            return 1
        end
        """;

    public AtomicRateLimiter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean isAllowed(String userId, int maxRequestsPerMinute) {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>(LUA_RATE_LIMIT, Long.class);
        Long result = redisTemplate.execute(script, List.of("rate:limit:" + userId), String.valueOf(maxRequestsPerMinute));
        return result != null && result == 1L;
    }
}
```

---

# Category 6: Production War Room Incidents & Outage Forensics

### Q6: WAR ROOM RCA: Redis Cluster OOM Cascade via Infinite Hash Key Bloat
- **Severity:** P0 Outage (Session cluster crashed, user login failed globally)
- **Mean Time to Recovery (MTTR):** 40 minutes
- **Symptoms:** The Redis session cluster memory utilization climbed from 2GB to 32GB in 4 hours, rejecting all writes with `OOM command not allowed`.
- **Root Cause Forensics:**
  A developer stored user cart data inside a Redis Hash without setting an expiration on the parent key:
  ```java
  // ANTI-PATTERN: Redis Hashes do NOT support field-level TTL!
  redisTemplate.opsForHash().put("user:cart:" + userId, itemId, itemJson);
  // Forgot to call: redisTemplate.expire("user:cart:" + userId, 7, TimeUnit.DAYS);
  ```
  1. Redis **does not support expiring individual fields inside a Hash**.
  2. The parent Hash key `"user:cart:" + userId` had no TTL set.
  3. Millions of abandoned cart hashes accumulated indefinitely, consuming all 32GB of RAM under `noeviction` policy.
- **The Permanent Fix:**
  1. Set an explicit expiration on the parent key every time a field is updated: `redisTemplate.expire(key, Duration.ofDays(7))`.
  2. Configure `maxmemory-policy allkeys-lru` on the cluster so abandoned keys are automatically reclaimed when memory reaches capacity.

---

## ⚖️ Spring Data Redis Production Engineering Matrix

| Requirement / Pattern | High-Performance Production Syntax |
| :--- | :--- |
| **Pipelined Connection Pool** | `LettucePoolingClientConfiguration` (8–16 connections) |
| **Distributed Mutex with Watchdog** | Redisson `RLock.tryLock(wait, unit)` without explicit lease |
| **Cache Stampede In-JVM Defense** | `@Cacheable(value = "cache", sync = true)` |
| **Cache Avalanche Prevention** | Base TTL + `ThreadLocalRandom.current().nextInt(0, 300)` |
| **Cache Penetration Defense** | Redisson `RBloomFilter` + Null value caching |
| **Atomic Operations** | Single-shot Lua script via `DefaultRedisScript` |

---
[🏠 Back to Home](README.md) | [⚡ Spring Redis Master Guide](spring_redis.md) | [🍃 Spring Boot Master Guide](spring_master_guide.md)
