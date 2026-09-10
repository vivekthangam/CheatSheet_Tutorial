# Redis In-Memory Architecture, Advanced Data Structures & Caching Patterns Interview Guide

> **Scope**: Single-Threaded Event Loop & Redis 6.0+ I/O Threading, Core Data Structures Internals (SDS, Dict & Incremental Rehashing, Skiplist, Quicklist, Listpack), Persistence Engine (RDB Snapshots with Copy-On-Write, AOF Fsync Policies, Background Rewrites), Eviction Algorithms (`maxmemory`, Approximated LRU, LFU), High Availability & Clustering (Sentinel Consensus, Redis Cluster 16,384 Hash Slots, MOVED vs ASK Redirection), Cache Anti-Patterns & Defenses (Cache Penetration, Cache Breakdown, Cache Avalanche, XFetch Probabilistic Expiration), Distributed Locking (Redlock & Fencing Tokens), and Mission-Critical War-Room Post-Mortems.

---

## Guide Architecture Overview

```
========================================================================================================================
                                     REDIS IN-MEMORY ARCHITECTURE & CACHING
========================================================================================================================
 [Layer 1: Event Loop, Memory Model & I/O Threading]--> ae Event Library, epoll Multiplexing, Redis 6 Threaded I/O
 [Layer 2: Deep Data Structures & Memory Encoding]  --> SDS (Binary Safe), Dict (Rehashing), Skiplist (ZSET), Listpack
 [Layer 3: Persistence Mechanics: RDB vs AOF]       --> BGSAVE (fork COW), AOF fsync (always/everysec), AOF Rewrite
 [Layer 4: High Availability: Sentinel & Cluster]   --> Raft Sentinel Quorum, 16,384 Hash Slots, MOVED vs ASK Redirect
 [Layer 5: Cache Failure Topologies & Mitigation]   --> Penetration (Bloom), Breakdown (Mutex), Avalanche (Jitter TTL)
 [Layer 6: Ultra-Deep Real-World War-Room Cases]    --> 10 Production Disasters (Bigkey Event Loop Freeze, Fork COW OOM)
 [Layer 7: Beginner Mistakes & Anti-Patterns]       --> 8 Fatal Engineering Traps (KEYS * in Prod, Unbounded HSET)
 [Layer 8: Globally Reported Production Incidents]  --> Real Outages (GitHub Redis Cluster Primary Failover Cascade)
========================================================================================================================
```

---

# Layer 1: Event Loop, Memory Model & I/O Threading

---

### Scenario 1: Why Redis is Single-Threaded for Command Execution: The Event Loop
**Interviewer Evaluation:** Assesses mechanical understanding of CPU cache line efficiency, eliminating thread contention, and Redis 6.0+ threaded I/O.

#### Technical Deep Dive
1. **The Single-Threaded Command Loop**:
   - Redis processes all database commands (`GET`, `SET`, `HSET`) sequentially on a **single main thread**.
   - **Why?**: Memory access latencies are nanoseconds. CPU is almost never the bottleneck in Redis; network I/O and memory bandwidth are.
   - Eliminates thread synchronization locks (`mutex`, `synchronized`), condition variables, and expensive CPU context switches. Guarantees that individual commands and Lua scripts are **100% atomic**.
2. **Redis 6.0+ Multi-Threaded Network I/O**:
   - In modern multi-gigabit networks, reading packet bytes from the network socket and serializing responses to the client consumes significant CPU cycles.
   - Redis 6.0 introduced background **I/O Threads** dedicated strictly to reading socket buffers and writing serialized responses.
   - **Crucial Rule**: The actual command execution itself **remains strictly single-threaded** on the main thread!

```
Redis 6.0 Threaded I/O Architecture:
Network Sockets ---> [ Thread 1 (Read I/O) ] --\
Network Sockets ---> [ Thread 2 (Read I/O) ] ----> [ Main Thread: Pure Atomic Command Execution ]
Network Sockets ---> [ Thread 3 (Read I/O) ] --/                  |
                                                                  v
Network Sockets <--- [ Thread 1, 2, 3 (Write I/O) ] <-------------+
```

---

### Scenario 2: Internal Memory Encodings: SDS, Dict, and Skiplist
**Interviewer Evaluation:** Tests C-level knowledge of memory compacting, binary safety, incremental rehashing, and ordered sets.

#### Technical Deep Dive
1. **Simple Dynamic String (SDS)**:
   - Replaces standard C strings (`char*`). Stores string length, allocated capacity, and binary data.
   - $O(1)$ length lookup (`strlen` is $O(1)$ vs C's $O(N)$). Binary-safe (can store binary images or Protobuf payloads with null bytes `\0`).
2. **Dict (Hash Table & Incremental Rehashing)**:
   - Implements two hash tables: `ht[0]` (active) and `ht[1]` (rehashing target).
   - When load factor breaches thresholds, Redis resizes `ht[1]` to double capacity.
   - **Incremental Rehashing**: Instead of migrating 10 million keys at once (which would freeze Redis for seconds), Redis migrates a small batch of bucket indices on every client query and background cron, spreading migration cost over thousands of operations.
3. **Skiplist (Sorted Set / ZSet)**:
   - Probabilistic data structure replacing balanced trees (AVL/Red-Black).
   - Multi-level linked list providing $O(\log N)$ search, insertion, and range scans (`ZRANGEBYSCORE`) with far less memory overhead and easier concurrent implementation than B-Trees.

```
Skiplist Level Hierarchy:
Level 3: [ Head ] -----------------------------------------> [ Node 8 ] -> NULL
Level 2: [ Head ] ------------------> [ Node 4 ] ----------> [ Node 8 ] -> NULL
Level 1: [ Head ] ----> [ Node 2 ] -> [ Node 4 ] -> [ Node 6 ] -> [ Node 8 ] -> NULL
Level 0: [ Head ] -> [1] -> [2] -> [3] -> [4] -> [5] -> [6] -> [7] -> [8] -> NULL
```

---

# Layer 2: Persistence & Copy-On-Write Memory Spikes

---

### Scenario 3: RDB Snapshots via `BGSAVE`: The Copy-On-Write (COW) Memory Spike
**Interviewer Evaluation:** Assesses understanding of Linux `fork()`, OS memory paging, memory exhaustion, and configuring Linux swap/overcommit.

#### Technical Deep Dive
When Redis executes `BGSAVE` to create an RDB disk snapshot:
1. The main Redis process calls `fork()` to spawn a child process.
2. `fork()` duplicates only the page tables via **Copy-On-Write (COW)**; child process shares physical memory pages with the parent.
3. The child process iterates through memory and writes the `.rdb` file to disk.
4. **The Memory Explosion Trap**:
   - If the database is experiencing 50,000 writes/sec during `BGSAVE`, every parent write modifies a memory page.
   - The OS kernel allocates a new physical 4KB page for every write.
   - If 30% of keys are updated during the snapshot on a 40GB Redis instance, memory consumption surges by an extra **12GB to 15GB**!
   - If total memory exceeds host RAM, the Linux kernel **OOM Killer immediately terminates the Redis main process**!
   - *Fix*: Keep Redis memory usage $\le 60\%$ of total server RAM to provide headroom for COW page duplication.

---

# Layer 3: Cache Failure Topologies & Defensive Patterns

---

### Scenario 4: Cache Penetration vs Cache Breakdown vs Cache Avalanche
**Interviewer Evaluation:** Tests diagnosing and architecting defenses against the three classic caching collapse scenarios.

#### Technical Deep Dive
1. **Cache Penetration (Querying Non-Existent Keys)**:
   - An attacker queries millions of non-existent keys (e.g., `user_id = -9999`).
   - Requests miss Redis, bypass the cache, and flood the backend relational database, crashing it under load.
   - **Defenses**:
     - **Bloom Filter**: In-memory probabilistic filter in front of Redis; instantly rejects keys guaranteed not to exist with zero DB hits.
     - **Null Value Caching**: Cache the missing result with a short TTL: `SET user:-9999 "NULL" EX 60`.
2. **Cache Breakdown (Hotspot Key Expiration)**:
   - A super-hot key (e.g., Black Friday homepage deal receiving 100,000 QPS) suddenly expires.
   - All 100,000 concurrent threads miss the cache simultaneously and slam the database.
   - **Defense**: **Mutex Locking (`SET NX`)** or **Probabilistic Early Expiration (XFetch)**:
     $$\Delta t - \beta \times \ln(\text{rand}()) > \text{TTL}$$
     A single worker asynchronously refreshes the cache *before* it physically expires.
3. **Cache Avalanche (Mass Simultaneous Key Expiration)**:
   - Thousands of keys are saved with the exact same 1-hour TTL (`EX 3600`).
   - At $T+3600$, thousands of keys expire at the exact same second, transferring entire query volume to the database.
   - **Defense**: **TTL Jitter**: Add random variance to expiration times: `TTL = 3600 + rand(0, 300)` seconds.

```
Cache Failure Matrix & Architectural Defenses:
+-------------------+---------------------------------------+---------------------------------------+
| Failure Mode      | Root Cause                            | Architectural Defense                 |
+-------------------+---------------------------------------+---------------------------------------+
| Cache Penetration | Non-existent keys missing cache to DB | Bloom Filters + Null Object Caching   |
| Cache Breakdown   | Super-hot key expires under peak load | Mutex Locking (SET NX) / XFetch Early |
| Cache Avalanche   | Mass simultaneous expiration of keys  | Random TTL Jitter (+/- 10% delta)     |
+-------------------+---------------------------------------+---------------------------------------+
```

---

# Layer 4: Rapid-Fire Cheat Sheet & Interview Summary Matrix

---

### Critical Redis Operational Commands & Directives

| Command / Directive | Purpose | Production Best Practice |
| :--- | :--- | :--- |
| `maxmemory-policy` | Memory eviction strategy | `allkeys-lru` or `volatile-lfu` |
| `SCAN` vs `KEYS *` | Safe iteration vs blocking scan | **NEVER run `KEYS *` in production!** |
| `appendfsync everysec`| AOF write durability policy | Best balance: max 1-second data loss |
| `MEMORY USAGE <key>`| Computes exact bytes allocated | Identify Bigkeys degrading latency |
| `CLUSTER NODES` | Displays cluster topology & slots | Verify 16,384 slots are fully covered |

---

### The Golden Redis Architecture Rules
1. **Never run blocking commands in production**: Ban `KEYS *`, `FLUSHALL`, and `HGETALL` on massive maps.
2. **Always set a `maxmemory` ceiling and eviction policy**: Prevent OS-level OOM kills.
3. **Apply TTL Jitter to all cached entries**: Prevent simultaneous Cache Avalanche events.
4. **Cap Redis memory at 60% of host RAM**: Leave headroom for `BGSAVE` Copy-On-Write memory spikes.
5. **Use Bloom Filters against Cache Penetration**: Protect backend relational databases from malicious missing key scans.
