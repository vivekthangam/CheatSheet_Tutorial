[🏠 Back to Home](../README.md) | [🎨 Design Patterns 200 Scenarios](java_design_patterns_200_scenarios_master_guide.md) | [🧵 Concurrency 200 Scenarios](java_threads_concurrency_200_scenarios_master_guide.md) | [📦 Collections 200 Scenarios](java_collections_streams_200_scenarios_master_guide.md)

# Java System Design & Low-Level Design (LLD): 200 Real-World Interview Scenarios Master Guide

## Flagship Tier-1 Engineering Machine Coding & Object-Oriented Architecture Reference

[![Java](https://img.shields.io/badge/Java-17%20%2F%2021%20%2F%2025%20LTS-orange.svg?style=for-the-badge&logo=openjdk)](https://www.oracle.com/java/)
[![Low-Level Design](https://img.shields.io/badge/LLD-FAANG%20Machine%20Coding-blue.svg?style=for-the-badge)](https://github.com/)
[![Concurrency](https://img.shields.io/badge/Concurrency-Lock--Free%20%7C%20CAS%20%7C%20StampedLock-purple.svg?style=for-the-badge)](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/package-summary.html)
[![System Design](https://img.shields.io/badge/Algorithms-LRU%20%7C%20Snowflake%20%7C%20Min--Cash--Flow-green.svg?style=for-the-badge)](https://github.com/)
[![Level](https://img.shields.io/badge/Tier-1%20Panels-Staff%20%2F%20Principal%20%2F%20Architect-red.svg?style=for-the-badge)](https://github.com/)

An exhaustive, battle-tested compilation of **200 production-grade interview scenarios** covering Low-Level System Design (LLD), Object-Oriented Modeling, and FAANG machine coding challenges in Modern Java (Java 17, 21, and 25 LTS). This guide covers real-world implementations of the classic and modern canonical LLD architectures: **Smart Multi-Tier Parking Lot with Concurrency Control, Distributed Rate Limiters (Token Bucket, Leaky Bucket, Sliding Window Log), Splitwise Debt Simplification Graph Engine, Multi-Elevator SCAN/LOOK Scheduling Controller, $O(1)$ LRU & LFU Caches, Twitter Snowflake 64-bit ID Generators, Write-Ahead Log (WAL) Key-Value Storage Engines, and Distributed Cron Task Schedulers**.

Every scenario strictly follows the **5-Part Tier-1 Product Engineering Format**:
1. **Exact Question Asked by Tier-1 Product Panels** (Google, Meta, Uber, Amazon, Netflix, Stripe, Apple, Citadel, Jane Street)
2. **What the Interviewer Evaluates Under the Surface** (Encapsulation, SOLID principles, concurrency models, thread contention, time/space complexity, edge cases)
3. **Standout Technical Answer** (Data structure selection, algorithm math, lock-free primitives, failure boundaries)
4. **Follow-Up Trap Question & Winning Answer** (Catching surface memorizers on scale, race conditions, and deadlocks)
5. **Production Sample Code** with execution steps, complete robust Java code, and sample input/output verification.

---

## 📑 Master Architecture Navigation

- [🏛️ Architectural Blueprint: The Core LLD Engineering Taxonomy](#architectural-blueprint-the-core-lld-engineering-taxonomy)
- [📚 Track 0: Master Jargon-Buster & Rosetta Stone](#track-0-master-jargon-buster--rosetta-stone)
- [🚗 Category 1: Smart Multi-Tier Parking Lot System (Q1 – Q20)](#category-1-smart-multi-tier-parking-lot-system-q1--q20)
- [⏱️ Category 2: Rate Limiting & Traffic Shaping Algorithms (Q21 – Q40)](#category-2-rate-limiting--traffic-shaping-algorithms-q21--q40)
- [💸 Category 3: Splitwise Debt Simplification & Graph Engines (Q41 – Q60)](#category-3-splitwise-debt-simplification--graph-engines-q41--q60)
- [🛗 Category 4: Multi-Elevator Dispatching & Scheduling Engine (Q61 – Q80)](#category-4-multi-elevator-dispatching--scheduling-engine-q61--q80)
- [💾 Category 5: In-Memory Multi-Tier Caching Engines (LRU & LFU) (Q81 – Q100)](#category-5-in-memory-multi-tier-caching-engines-lru--lfu-q81--q100)
- [❄️ Category 6: Distributed Unique ID Generation & Bitwise Arithmetic (Q101 – Q120)](#category-6-distributed-unique-id-generation--bitwise-arithmetic-q101--q120)
- [📝 Category 7: Write-Ahead Log (WAL) & Key-Value Storage Engines (Q121 – Q140)](#category-7-write-ahead-log-wal--key-value-storage-engines-q121--q140)
- [⏰ Category 8: Distributed Task Scheduler & Priority Cron Engine (Q141 – Q160)](#category-8-distributed-task-scheduler--priority-cron-engine-q141--q160)
- [🛑 Category 9: Machine Coding Anti-Patterns & Concurrency Bugs (Q161 – Q180)](#category-9-machine-coding-anti-patterns--concurrency-bugs-q161--q180)
- [🔥 Category 10: Production War-Room Outage Incidents & SRE Post-Mortems (Q181 – Q200)](#category-10-production-war-room-outage-incidents--sre-post-mortems-q181--q200)
- [📊 Production LLD Decision Matrix & 25-Point Machine Coding Checklist](#production-lld-decision-matrix--25-point-machine-coding-checklist)

---

## 🏛️ Architectural Blueprint: The Core LLD Engineering Taxonomy

```mermaid
flowchart TD
    subgraph ClientTier["External Ingress & Traffic Management"]
        RateLimiter["Rate Limiting Engine<br/>Token Bucket / Sliding Window Log"]
        AuthGateway["API Security & Ingress Gates"]
    end

    subgraph CoreEngineTier["Stateful Domain Core & Machine Coding Models"]
        subgraph RealWorldSim["Physical Systems Simulation"]
            ParkingLot["Parking Lot Controller<br/>Multi-Floor, Dynamic Pricing, CAS Spots"]
            ElevatorCtrl["Elevator Dispatcher<br/>SCAN/LOOK Heuristic, Hall Call Queues"]
        end

        subgraph FinancialCoord["Financial & Accounting Systems"]
            Splitwise["Splitwise Debt Engine<br/>Min-Cash-Flow Heap/Graph Simplification"]
        end

        subgraph InfraPrimitives["Low-Latency Infrastructure Primitives"]
            CacheEngine["Multi-Tier Cache<br/>O(1) LRU & LFU Doubly-Linked Lists"]
            SnowflakeGen["Distributed Snowflake Gen<br/>64-bit Bitwise Atomic Sequencer"]
            WALStorage["WAL KV Storage Engine<br/>Append-only Log + MemTable SkipList"]
            TaskScheduler["Task Scheduler Engine<br/>DelayQueue Worker Lease Pool"]
        end
    end

    ClientTier --> CoreEngineTier
```

---

## 📚 Track 0: Master Jargon-Buster & Rosetta Stone

| Term / Acronym | Architectural Meaning | Production Failure if Ignored |
| :--- | :--- | :--- |
| **Token Bucket** | Rate limiting algorithm refilling tokens at fixed rate up to capacity; allows bursts equal to bucket size. | **Uncontrolled Spikes**: Naive fixed counters permit $2\times$ rate limit at window boundaries. |
| **Leaky Bucket** | Rate limiting algorithm processing requests at a fixed constant egress rate, buffering excess in a queue. | **Queue Exhaustion**: Unbounded queue creates high memory usage and artificial latency. |
| **Min-Cash-Flow** | Graph simplification algorithm reducing total settlement transactions in a debt mesh from $O(N^2)$ to $\le N-1$. | **Financial Friction**: Users forced to settle dozens of tiny redundant payment transactions. |
| **SCAN / LOOK** | Disk head / elevator scheduling algorithm servicing requests in the current direction until boundary before reversing. | **Elevator Starvation**: FCFS causes massive car travel bouncing between floor 1 and floor 50. |
| **LFU (Least Frequently Used)** | Cache eviction policy discarding elements with lowest access frequency; resolves ties via LRU. | **Cache Pollution**: Historical frequency spikes permanently lock obsolete entries in cache. |
| **Snowflake ID** | 64-bit time-ordered unique ID layout (timestamp + node ID + sequence counter) fitting in a 64-bit `long`. | **ID Collisions**: Backward NTP clock drift generates duplicate primary keys. |
| **Write-Ahead Log (WAL)** | Append-only sequential disk log recording mutations before applying them to in-memory state. | **Data Loss**: Process crash destroys uncommitted in-memory mutations. |
| **DelayQueue** | Unbounded blocking queue of `Delayed` elements ordered by expiration time; elements can only be polled when expired. | **Busy Waiting**: Spinning loops checking expiration burn 100% CPU. |

---

## 🚗 Category 1: Smart Multi-Tier Parking Lot System (Q1 – Q20)

### ❓ Scenario 1: How do you design a thread-safe multi-floor Parking Lot spot allocation engine without lock contention?
* **Interviewer Evaluation**: Tests object-oriented modeling (Floors, Spots, Vehicles, Tickets), spot sizing polymorphism, thread-safe atomic allocation under concurrent entry gates, and lock striping.
* **Standout Technical Answer**:
  A naive implementation synchronizes the entire `ParkingLot.park(Vehicle v)` method with a coarse-grained intrinsic lock (`synchronized`). When 20 entry gates admit cars concurrently, every car blocks all other cars across all floors, collapsing throughput!
  **High-Performance Architecture**:
  1. **Polymorphic Vehicle & Spot Sizing**: Enum `SpotType { MOTORCYCLE, COMPACT, LARGE }` matching `VehicleType { BIKE, CAR, TRUCK }`. A truck requires a `LARGE` spot; a car fits in `COMPACT` or `LARGE`.
  2. **Decentralized Spot Management**: Each floor manages its own spots using a `ConcurrentSkipListSet<ParkingSpot>` or segmented bucket lists ordered by distance to entrance.
  3. **Lock-Free Spot Acquisition (CAS)**:
     - Each `ParkingSpot` holds an `AtomicReference<Ticket> occupiedTicket = new AtomicReference<>(null)`.
     - When a gate finds a candidate spot, it attempts `spot.occupiedTicket.compareAndSet(null, newTicket)`.
     - If CAS returns `true`, the spot is atomically claimed in $<5\text{ns}$ with zero lock contention!
     - If CAS returns `false`, another gate claimed it simultaneously; the gate simply advances to the next spot in the sorted set.
* **Follow-Up Trap**: *"How do you prevent a scenario where a gate iterates through 10,000 occupied spots searching for an available spot on a full floor?"*
* **Winning Answer**: Maintain an `AtomicInteger availableCount` per `SpotType` per `Floor`. Before traversing spots, the gate checks `if (floor.availableCount(type).decrementAndGet() < 0) { floor.availableCount(type).incrementAndGet(); return FULL; }`. If counter is zero, fail immediately in $O(1)$ without touching any spot objects.
* **Production Sample Code**:
```java
package com.vortex.lld.parkinglot;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public enum VehicleType { MOTORCYCLE, CAR, TRUCK }
public enum SpotType { MOTORCYCLE, COMPACT, LARGE }

public final class ParkingSpot implements Comparable<ParkingSpot> {
    private final String spotId;
    private final int floorNumber;
    private final SpotType spotType;
    private final int distanceFactor;
    private final AtomicReference<ParkingTicket> currentTicket = new AtomicReference<>(null);

    public ParkingSpot(String spotId, int floorNumber, SpotType spotType, int distanceFactor) {
        this.spotId = spotId;
        this.floorNumber = floorNumber;
        this.spotType = spotType;
        this.distanceFactor = distanceFactor;
    }

    public boolean canFit(VehicleType vehicle) {
        return switch (vehicle) {
            case MOTORCYCLE -> true; // Fits in any spot
            case CAR -> spotType == SpotType.COMPACT || spotType == SpotType.LARGE;
            case TRUCK -> spotType == SpotType.LARGE;
        };
    }

    public boolean tryOccupy(ParkingTicket ticket) {
        return currentTicket.compareAndSet(null, ticket);
    }

    public ParkingTicket release() {
        return currentTicket.getAndSet(null);
    }

    @Override
    public int compareTo(ParkingSpot o) {
        int floorComp = Integer.compare(this.floorNumber, o.floorNumber);
        return floorComp != 0 ? floorComp : Integer.compare(this.distanceFactor, o.distanceFactor);
    }

    public String getSpotId() { return spotId; }
    public SpotType getSpotType() { return spotType; }
}

public record ParkingTicket(String ticketId, String licensePlate, String spotId, Instant entryTime) {}

public final class ParkingLotManager {
    private final Map<SpotType, List<ParkingSpot>> spotsByType = new ConcurrentHashMap<>();

    public ParkingLotManager(List<ParkingSpot> allSpots) {
        for (ParkingSpot s : allSpots) {
            spotsByType.computeIfAbsent(s.getSpotType(), k -> new ArrayList<>()).add(s);
        }
        spotsByType.values().forEach(Collections::sort); // Sort nearest first
    }

    public Optional<ParkingTicket> parkVehicle(String licensePlate, VehicleType vehicleType) {
        List<SpotType> candidateTypes = switch (vehicleType) {
            case MOTORCYCLE -> List.of(SpotType.MOTORCYCLE, SpotType.COMPACT, SpotType.LARGE);
            case CAR -> List.of(SpotType.COMPACT, SpotType.LARGE);
            case TRUCK -> List.of(SpotType.LARGE);
        };

        for (SpotType type : candidateTypes) {
            List<ParkingSpot> spots = spotsByType.getOrDefault(type, List.of());
            for (ParkingSpot spot : spots) {
                if (spot.canFit(vehicleType)) {
                    String ticketId = "TKT-" + UUID.randomUUID();
                    ParkingTicket ticket = new ParkingTicket(ticketId, licensePlate, spot.getSpotId(), Instant.now());
                    if (spot.tryOccupy(ticket)) {
                        return Optional.of(ticket); // Successfully parked via atomic CAS!
                    }
                }
            }
        }
        return Optional.empty(); // Lot full
    }
}
```
* **Sample Input & Output**:
  - *Input*: Concurrently park 10 cars across 2 entry gates into a lot with 10 compact spots.
  - *Output*: All 10 cars receive unique valid tickets in $<1\text{ms}$; zero duplicate spot assignments.

---

### ❓ Scenario 2: How do you implement dynamic pricing in a Parking Lot fee calculation engine using the Strategy Pattern?
* **Interviewer Evaluation**: Tests Strategy Pattern, Open-Closed Principle, time-duration rounding, and peak-hour surge pricing.
* **Standout Technical Answer**:
  Hardcoding hourly rates inside the `ParkingLot` class violates OCP: business requirements frequently introduce night discounts, weekend surcharges, and vehicle-specific multipliers.
  **Dynamic Pricing Architecture**:
  1. Define a `FeeCalculationStrategy` interface: `long calculateFeeCents(ParkingTicket ticket, Instant exitTime)`.
  2. Implement modular strategies:
     - `FlatHourlyFeeStrategy`: Flat $3/hour.
     - `VehicleScaledFeeStrategy`: Motorcycle $1/hr, Car $3/hr, Truck $7/hr.
     - `PeakSurgeFeeStrategy`: Wraps another strategy and applies a $1.5\times$ surge multiplier if entry or exit occurs during rush hour (08:00–10:00, 17:00–19:00).
  3. Fee calculator computes duration with ceiling math (`Math.ceil(minutes / 60.0)`), guaranteeing that a 61-minute stay charges for 2 full hours.
* **Follow-Up Trap**: *"How do you handle currency rounding to avoid losing fractions of a cent across millions of transactions?"*
* **Winning Answer**: **Never use `double` or `float` for financial calculations!** Always store monetary values as 64-bit integer cents (`long amountCents`) or `BigDecimal` with explicit rounding modes (`RoundingMode.HALF_UP`).
* **Production Sample Code**:
```java
package com.vortex.lld.parkinglot;

import java.time.Duration;
import java.time.Instant;

public interface ParkingFeeStrategy {
    long calculateFeeCents(VehicleType vehicleType, Instant entryTime, Instant exitTime);
}

public final class TieredHourlyFeeStrategy implements ParkingFeeStrategy {
    @Override
    public long calculateFeeCents(VehicleType vehicleType, Instant entryTime, Instant exitTime) {
        long durationMinutes = Duration.between(entryTime, exitTime).toMinutes();
        long hoursBilled = (long) Math.ceil(Math.max(1, durationMinutes) / 60.0);

        long hourlyRateCents = switch (vehicleType) {
            case MOTORCYCLE -> 150; // $1.50/hr
            case CAR        -> 300; // $3.00/hr
            case TRUCK      -> 600; // $6.00/hr
        };

        // First hour flat; subsequent hours discounted
        if (hoursBilled == 1) return hourlyRateCents;
        return hourlyRateCents + (hoursBilled - 1) * (long)(hourlyRateCents * 0.75);
    }
}
```
* **Sample Input & Output**:
  - *Input*: Car parked for 2 hours and 10 minutes (billed as 3 hours).
  - *Output*: `300 + 2 * 225 = 750` cents ($7.50).

---

## ⏱️ Category 2: Rate Limiting & Traffic Shaping Algorithms (Q21 – Q40)

### ❓ Scenario 21: How does the Token Bucket Algorithm work, and how do you implement it in thread-safe Java without background timer threads?
* **Interviewer Evaluation**: Tests token bucket physics, burst allowance, lazy refill math, atomic synchronization, and timer thread elimination.
* **Standout Technical Answer**:
  A naive token bucket implementation runs a background `ScheduledExecutorService` that adds 1 token every $100\text{ms}$.
  *The Flaw*: Having 100,000 active users means 100,000 background timer tasks firing 10 times a second $\to$ **Severe CPU Context Switching & Scheduler Starvation!**
  **Lazy Refill Mathematics**:
  Eliminate background timer threads entirely by computing token generation on-demand when a request arrives:
  1. Maintain `long lastRefillTimestampNanos` and `double currentTokens`.
  2. When `allowRequest(tokensRequired)` is called:
     $$\Delta t = \text{now} - \text{lastRefillTimestampNanos}$$
     $$\text{tokensToAdd} = \Delta t \times \text{refillRatePerNano}$$
     $$\text{currentTokens} = \min(\text{maxCapacity}, \text{currentTokens} + \text{tokensToAdd})$$
     $$\text{lastRefillTimestampNanos} = \text{now}$$
  3. If $\text{currentTokens} \ge \text{tokensRequired}$, decrement tokens and return `true`; else return `false`.
* **Follow-Up Trap**: *"How do you make the Token Bucket lock-free using `AtomicReference` CAS instead of `synchronized`?"*
* **Winning Answer**: Package `(currentTokens, lastRefillTimestamp)` into an immutable record `BucketState`. In `allowRequest()`, read current state, compute new state, and execute `atomicState.compareAndSet(oldState, newState)` inside a CAS retry loop.
* **Production Sample Code**:
```java
package com.vortex.lld.ratelimiter;

import java.util.concurrent.atomic.AtomicReference;

public final class LockFreeTokenBucket {
    private final double maxCapacity;
    private final double refillRatePerSecond;
    private final AtomicReference<State> stateRef;

    private record State(double tokens, long timestampNanos) {}

    public LockFreeTokenBucket(double maxCapacity, double refillRatePerSecond) {
        this.maxCapacity = maxCapacity;
        this.refillRatePerSecond = refillRatePerSecond;
        this.stateRef = new AtomicReference<>(new State(maxCapacity, System.nanoTime()));
    }

    public boolean tryAcquire(double tokensRequired) {
        while (true) {
            State current = stateRef.get();
            long now = System.nanoTime();
            long elapsedNanos = Math.max(0, now - current.timestampNanos());
            
            // Lazy Refill Math
            double tokensGenerated = (elapsedNanos / 1_000_000_000.0) * refillRatePerSecond;
            double refreshedTokens = Math.min(maxCapacity, current.tokens() + tokensGenerated);

            if (refreshedTokens < tokensRequired) {
                return false; // Rate limit exceeded!
            }

            State next = new State(refreshedTokens - tokensRequired, now);
            if (stateRef.compareAndSet(current, next)) {
                return true; // Atomically claimed tokens!
            }
        }
    }
}
```
* **Sample Input & Output**:
  - *Input*: Capacity 5, refill rate 1/sec. Rapidly fire 5 requests, then 6th request immediately.
  - *Output*: First 5 requests return `true`; 6th returns `false` (Rate limited). After 1s, next request returns `true`.

---

### ❓ Scenario 22: Sliding Window Log vs Sliding Window Counter: Why does Sliding Window Counter dominate in high-volume systems?
* **Interviewer Evaluation**: Tests time complexity, memory footprint, boundary burst vulnerabilities of Fixed Window, and Redis memory optimization.
* **Standout Technical Answer**:
  - **Fixed Window Counter**: Divides time into 1-minute blocks. Vulnerable to **Boundary Burst Attack**: if limit is 100 req/min, an attacker sends 100 requests at 00:59 and 100 requests at 01:00. Over the 2-second interval, 200 requests pass, exceeding the limit by $2\times$!
  - **Sliding Window Log**: Stores every request timestamp in a `TreeSet` or Redis Sorted Set (`ZSET`). Drops timestamps older than $\text{now} - \text{windowSize}$ and counts remaining entries.
    - *Drawback*: Huge memory consumption! If limit is 50,000 req/sec, storing 50,000 64-bit timestamps per user consumes megabytes of memory per active client.
  - **Sliding Window Counter (Weighted Interpolation)**:
    - Approximates the sliding window using only two integer counters: **Previous Window Count** and **Current Window Count**.
    - If current time is 30% into the current 1-minute window, the estimated count is:
      $$\text{Count} = \text{PrevCount} \times (1 - 0.3) + \text{CurrentCount}$$
    - Uses $O(1)$ memory (just 2 numbers), zero timestamp storage, and guarantees $<0.05\%$ approximation error!
* **Follow-Up Trap**: *"How do you implement a distributed Sliding Window in Redis atomically?"*
* **Winning Answer**: Use a **Redis Lua Script**. Because Redis executes Lua scripts as a single atomic command, fetching the counters, computing the weighted average, and incrementing current window occurs without race conditions.
* **Production Sample Code**:
```java
package com.vortex.lld.ratelimiter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public final class SlidingWindowCounterRateLimiter {
    private final long windowSizeMillis;
    private final long maxRequests;
    private final ConcurrentHashMap<Long, AtomicLong> windowCounts = new ConcurrentHashMap<>();

    public SlidingWindowCounterRateLimiter(long windowSizeMillis, long maxRequests) {
        this.windowSizeMillis = windowSizeMillis;
        this.maxRequests = maxRequests;
    }

    public boolean allowRequest() {
        long now = System.currentTimeMillis();
        long currentWindowKey = now / windowSizeMillis;
        long prevWindowKey = currentWindowKey - 1;

        double progressInCurrentWindow = (double) (now % windowSizeMillis) / windowSizeMillis;
        
        long prevCount = windowCounts.getOrDefault(prevWindowKey, new AtomicLong(0)).get();
        AtomicLong currentCounter = windowCounts.computeIfAbsent(currentWindowKey, k -> new AtomicLong(0));
        
        double estimatedCount = prevCount * (1.0 - progressInCurrentWindow) + currentCounter.get();

        if (estimatedCount >= maxRequests) {
            return false;
        }

        currentCounter.incrementAndGet();
        
        // Evict obsolete windows older than previous window
        windowCounts.keySet().removeIf(key -> key < prevWindowKey);
        return true;
    }
}
```
* **Sample Input & Output**:
  - *Input*: Limit 10 req/sec. Send 10 requests evenly spaced; send 11th request.
  - *Output*: First 10 allowed; 11th rejected. Memory strictly bounded to 2 entries per user.

---

## 💸 Category 3: Splitwise Debt Simplification & Graph Engines (Q41 – Q60)

### ❓ Scenario 41: How does Splitwise simplify a complex mesh of debts using the Min-Cash-Flow graph algorithm?
* **Interviewer Evaluation**: Tests graph modeling, net balance calculation, greedy algorithms, priority queues (Max-Heap & Min-Heap), and transaction count minimization.
* **Standout Technical Answer**:
  In a group of 5 friends (A, B, C, D, E), pairwise expenses often create a tangled cycle of debts:
  - A owes B $20, B owes C $20, C owes A $20. Total transfers: 3.
  - In reality, all three net balances are $0! Total transfers needed: 0.
  **The Min-Cash-Flow Simplification Algorithm**:
  1. **Compute Net Balance for each person**:
     $$\text{Net Balance}(u) = \sum \text{Amounts Owed to } u - \sum \text{Amounts } u \text{ Owes Others}$$
  2. Persons with $\text{Net Balance} = 0$ are immediately excluded.
  3. Divide users into two sets:
     - **Debtors** ($\text{Net} < 0$, need to pay)
     - **Creditors** ($\text{Net} > 0$, need to receive)
  4. Store Debtors in a Min-Heap (sorted by most negative balance) and Creditors in a Max-Heap (sorted by largest positive balance).
  5. In each iteration:
     - Pop max debtor $D$ and max creditor $C$.
     - Settle amount $M = \min(|D.\text{balance}|, C.\text{balance})$.
     - Emit transaction: "$D \text{ pays } C \text{ amount } M$".
     - Update balances: $D.\text{balance} += M$, $C.\text{balance} -= M$.
     - If balance remains non-zero, push back into respective heap.
  6. This reduces transaction count from $O(N^2)$ to at most $N-1$ transactions!
* **Follow-Up Trap**: *"Is the greedy Min-Cash-Flow algorithm guaranteed to produce the absolute theoretical minimum number of transactions?"*
* **Winning Answer**: No. The absolute theoretical minimum is an **NP-Hard Subset-Sum Problem** (finding disjoint sub-groups of users whose balances sum to zero). The greedy $O(N \log N)$ algorithm is universally used in production because it solves 99.9% of real-world cases with optimal or near-optimal transactions without exponential runtime.
* **Production Sample Code**:
```java
package com.vortex.lld.splitwise;

import java.util.*;

public record DebtTransfer(String debtor, String creditor, long amountCents) {}

public final class DebtSimplificationEngine {
    public static List<DebtTransfer> simplifyDebts(Map<String, Map<String, Long>> rawDebts) {
        // Step 1: Calculate Net Balance per user
        Map<String, Long> netBalances = new HashMap<>();
        for (var entry : rawDebts.entrySet()) {
            String person = entry.getKey();
            for (var debt : entry.getValue().entrySet()) {
                String owedTo = debt.getKey();
                long amount = debt.getValue();
                netBalances.put(person, netBalances.getOrDefault(person, 0L) - amount);
                netBalances.put(owedTo, netBalances.getOrDefault(owedTo, 0L) + amount);
            }
        }

        // Step 2: Separate into Creditors (+) and Debtors (-)
        PriorityQueue<UserBalance> creditors = new PriorityQueue<>((a, b) -> Long.compare(b.cents, a.cents));
        PriorityQueue<UserBalance> debtors = new PriorityQueue<>(Comparator.comparingLong(a -> a.cents));

        for (var entry : netBalances.entrySet()) {
            if (entry.getValue() > 0) {
                creditors.add(new UserBalance(entry.getKey(), entry.getValue()));
            } else if (entry.getValue() < 0) {
                debtors.add(new UserBalance(entry.getKey(), entry.getValue()));
            }
        }

        // Step 3: Greedy Settlement
        List<DebtTransfer> settlements = new ArrayList<>();
        while (!creditors.isEmpty() && !debtors.isEmpty()) {
            UserBalance maxCreditor = creditors.poll();
            UserBalance maxDebtor = debtors.poll();

            long settleAmount = Math.min(-maxDebtor.cents, maxCreditor.cents);
            settlements.add(new DebtTransfer(maxDebtor.userId, maxCreditor.userId, settleAmount));

            long remainingCredit = maxCreditor.cents - settleAmount;
            long remainingDebt = maxDebtor.cents + settleAmount;

            if (remainingCredit > 0) creditors.add(new UserBalance(maxCreditor.userId, remainingCredit));
            if (remainingDebt < 0) debtors.add(new UserBalance(maxDebtor.userId, remainingDebt));
        }

        return settlements;
    }

    private record UserBalance(String userId, long cents) {}
}
```
* **Sample Input & Output**:
  - *Input*: Alice owes Bob $50, Bob owes Charlie $50, Charlie owes Alice $50.
  - *Output*: `settlements.isEmpty() == true`. All circular debts cancelled out to zero transactions!

---

## 🛗 Category 4: Multi-Elevator Dispatching & Scheduling Engine (Q61 – Q80)

### ❓ Scenario 61: How do you design an Elevator Controller using the LOOK / SCAN scheduling algorithm?
* **Interviewer Evaluation**: Tests finite state machine (FSM), disk scheduling algorithms applied to elevators, direction maintenance, and request queue optimization.
* **Standout Technical Answer**:
  - **FCFS (First-Come, First-Served)**: Services requests in arrival order. Disastrous for elevators: an elevator at floor 1 receiving calls for floor 50, then floor 2, then floor 49 bounces violently up and down, wasting power and time.
  - **SSTF (Shortest Seek Time First)**: Services the closest floor next. Causes **Starvation**: passengers on distant top floors may never be serviced if continuous calls occur on middle floors.
  - **LOOK Algorithm (The Standard Elevator Algorithm)**:
    1. The elevator moves in its current direction (`UP` or `DOWN`).
    2. It services all pending internal car stops and external hall calls in that direction as it encounters them.
    3. It continues in that direction until there are **no more requests further along that path**.
    4. Only then does it reverse direction and service requests in the opposite direction.
    5. This eliminates starvation, minimizes motor reversal wear, and optimizes passenger transit times.
* **Follow-Up Trap**: *"How should the elevator queue differentiate between an external hall call wanting to go UP vs an internal passenger wanting to go DOWN?"*
* **Winning Answer**: Maintain **two separate priority sets per elevator**: `TreeSet<Integer> upStops` (sorted ascending) and `TreeSet<Integer> downStops` (sorted descending). When moving UP, only halt for external calls whose requested direction is ALSO `UP` or whose destination is on that floor.
* **Production Sample Code**:
```java
package com.vortex.lld.elevator;

import java.util.TreeSet;

public enum Direction { UP, DOWN, IDLE }

public final class ElevatorCar {
    private final int carId;
    private int currentFloor = 1;
    private Direction direction = Direction.IDLE;
    private final TreeSet<Integer> upRequests = new TreeSet<>(); // Ascending
    private final TreeSet<Integer> downRequests = new TreeSet<>((a, b) -> Integer.compare(b, a)); // Descending

    public ElevatorCar(int carId) { this.carId = carId; }

    public synchronized void addDestination(int targetFloor) {
        if (targetFloor > currentFloor) {
            upRequests.add(targetFloor);
            if (direction == Direction.IDLE) direction = Direction.UP;
        } else if (targetFloor < currentFloor) {
            downRequests.add(targetFloor);
            if (direction == Direction.IDLE) direction = Direction.DOWN;
        }
    }

    public synchronized void step() {
        if (direction == Direction.UP) {
            if (!upRequests.isEmpty()) {
                currentFloor = upRequests.pollFirst();
                System.out.println("Car " + carId + " arrived at Floor " + currentFloor + " (UP)");
            }
            if (upRequests.isEmpty()) {
                direction = downRequests.isEmpty() ? Direction.IDLE : Direction.DOWN;
            }
        } else if (direction == Direction.DOWN) {
            if (!downRequests.isEmpty()) {
                currentFloor = downRequests.pollFirst();
                System.out.println("Car " + carId + " arrived at Floor " + currentFloor + " (DOWN)");
            }
            if (downRequests.isEmpty()) {
                direction = upRequests.isEmpty() ? Direction.IDLE : Direction.UP;
            }
        }
    }

    public int getCurrentFloor() { return currentFloor; }
    public Direction getDirection() { return direction; }
}
```
* **Sample Input & Output**:
  - *Input*: Car at Floor 1. Requests added for 10, 5, 20, 2. Step elevator through run.
  - *Output*: Visited in order: 2, 5, 10, 20. Perfect sequential LOOK dispatching.

---

## 💾 Category 5: In-Memory Multi-Tier Caching Engines (LRU & LFU) (Q81 – Q100)

### ❓ Scenario 81: How do you implement a thread-safe $O(1)$ LRU Cache without using Java's built-in `LinkedHashMap`?
* **Interviewer Evaluation**: Tests data structure fundamentals (Doubly-Linked List + Hash Map), pointer manipulation under concurrency, lock striping vs ReentrantReadWriteLock, and memory footprint.
* **Standout Technical Answer**:
  To achieve strict $O(1)$ lookup and $O(1)$ eviction:
  1. **HashMap<K, Node<K, V>>**: Maps keys to their Doubly-Linked-List nodes in $O(1)$.
  2. **Doubly-Linked List**:
     - `Head` sentinel and `Tail` sentinel.
     - Most Recently Used (MRU) nodes sit near `Head`.
     - Least Recently Used (LRU) nodes sit near `Tail`.
  3. **Operations**:
     - `get(key)`: Lookup node in map. If present, detach node from its current position and prepend to `Head` (MRU). Return value.
     - `put(key, value)`: If key exists, update value and move node to `Head`. If key is new:
       - If size exceeds capacity, remove `Tail.prev` from both list and map (eviction).
       - Create new node, insert into map, and prepend to `Head`.
  4. **Concurrency**: Guard node pointer mutations with a `ReentrantReadWriteLock` or fine-grained mutex.
* **Follow-Up Trap**: *"Why are dummy Head and Tail sentinel nodes crucial in the Doubly-Linked List implementation?"*
* **Winning Answer**: Sentinels eliminate special-case edge handling for `null` checks when inserting at the head of an empty list or deleting the last remaining element. `head.next` and `tail.prev` are always non-null valid references.
* **Production Sample Code**:
```java
package com.vortex.lld.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public final class HighPerformanceLruCache<K, V> {
    private static class Node<K, V> {
        K key;
        V value;
        Node<K, V> prev, next;
        Node(K k, V v) { this.key = k; this.value = v; }
    }

    private final int capacity;
    private final Map<K, Node<K, V>> index = new HashMap<>();
    private final Node<K, V> headSentinel = new Node<>(null, null);
    private final Node<K, V> tailSentinel = new Node<>(null, null);
    private final ReentrantLock lock = new ReentrantLock();

    public HighPerformanceLruCache(int capacity) {
        this.capacity = capacity;
        headSentinel.next = tailSentinel;
        tailSentinel.prev = headSentinel;
    }

    public V get(K key) {
        lock.lock();
        try {
            Node<K, V> node = index.get(key);
            if (node == null) return null;
            moveToHead(node);
            return node.value;
        } finally {
            lock.unlock();
        }
    }

    public void put(K key, V value) {
        lock.lock();
        try {
            Node<K, V> existing = index.get(key);
            if (existing != null) {
                existing.value = value;
                moveToHead(existing);
                return;
            }
            if (index.size() >= capacity) {
                // Evict LRU element
                Node<K, V> lru = tailSentinel.prev;
                removeNode(lru);
                index.remove(lru.key);
            }
            Node<K, V> newNode = new Node<>(key, value);
            index.put(key, newNode);
            addFirst(newNode);
        } finally {
            lock.unlock();
        }
    }

    private void moveToHead(Node<K, V> node) {
        removeNode(node);
        addFirst(node);
    }

    private void addFirst(Node<K, V> node) {
        node.next = headSentinel.next;
        node.prev = headSentinel;
        headSentinel.next.prev = node;
        headSentinel.next = node;
    }

    private void removeNode(Node<K, V> node) {
        node.prev.next = node.next;
        node.next.prev = node.prev;
    }
}
```
* **Sample Input & Output**:
  - *Input*: Capacity 2. `put(1, A)`, `put(2, B)`, `get(1)`, `put(3, C)`.
  - *Output*: Key 2 evicted (LRU); Key 1 and Key 3 retained. Operation runs in strictly guaranteed $O(1)$ time.

---

### ❓ Scenario 82: How do you implement a strictly $O(1)$ LFU (Least Frequently Used) Cache using the Ketan Shah Frequency List algorithm?
* **Interviewer Evaluation**: Tests advanced data structures (Map of Frequency Doubly-Linked Lists), frequency tie-breaking with LRU, minimum frequency tracking, and strict $O(1)$ algorithmic complexity.
* **Standout Technical Answer**:
  A naive LFU implementation uses a `PriorityQueue` ordered by access count.
  *The Flaw*: `PriorityQueue` requires $O(\log N)$ on `get()` and `put()` to update frequency and re-heapify, which is unacceptable for ultra-high-throughput caches.
  **The Ketan Shah $O(1)$ Algorithm**:
  1. Maintain two hash maps:
     - `Map<K, Node<K, V>> keyToNode`: Maps key to its node.
     - `Map<Integer, DoublyLinkedList<Node>> freqToList`: Maps each access frequency ($1, 2, 3\dots$) to a Doubly-Linked List of nodes having that exact frequency.
  2. Maintain `int minFrequency`: Tracks the lowest frequency currently present in the cache.
  3. When `get(key)` is invoked:
     - Find node in `keyToNode`.
     - Remove node from `freqToList.get(oldFreq)`.
     - Increment `node.freq`.
     - Add node to `freqToList.computeIfAbsent(newFreq)`.
     - If `oldFreq == minFrequency` and its list is now empty, increment `minFrequency++`.
  4. When `put(key, value)` evicts:
     - Evict the **Least Recently Used** node from `freqToList.get(minFrequency).tail.prev`.
     - Remove from `keyToNode`.
     - Insert new node with `freq = 1`, and reset `minFrequency = 1`.
  5. Every operation (`get`, `put`, `evict`) runs in guaranteed $O(1)$ time!
* **Follow-Up Trap**: *"What is the Cache Pollution problem in LFU, and how do modern systems mitigate it?"*
* **Winning Answer**: In standard LFU, an item accessed 10,000 times during a morning flash sale accumulates a high frequency. Even if it is never accessed again, it occupies cache space for weeks because new items start with `freq = 1` and get evicted immediately. Production systems (like **Caffeine Cache**) use **Frequency Aging / Decay (TinyLFU)**, halving all counters periodically via a count-min sketch.
* **Production Sample Code**:
```java
package com.vortex.lld.cache;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public final class HighPerformanceLfuCache<K, V> {
    private static class Node<K, V> {
        final K key;
        V value;
        int frequency = 1;
        Node<K, V> prev, next;
        Node(K k, V v) { this.key = k; this.value = v; }
    }

    private static class DoublyLinkedList<K, V> {
        final Node<K, V> head = new Node<>(null, null);
        final Node<K, V> tail = new Node<>(null, null);
        int size = 0;

        DoublyLinkedList() {
            head.next = tail;
            tail.prev = head;
        }

        void addFirst(Node<K, V> node) {
            node.next = head.next;
            node.prev = head;
            head.next.prev = node;
            head.next = node;
            size++;
        }

        void remove(Node<K, V> node) {
            node.prev.next = node.next;
            node.next.prev = node.prev;
            size--;
        }

        Node<K, V> removeLast() {
            if (size == 0) return null;
            Node<K, V> last = tail.prev;
            remove(last);
            return last;
        }
    }

    private final int capacity;
    private int minFrequency = 0;
    private final Map<K, Node<K, V>> keyToNode = new HashMap<>();
    private final Map<Integer, DoublyLinkedList<K, V>> freqToList = new HashMap<>();
    private final ReentrantLock lock = new ReentrantLock();

    public HighPerformanceLfuCache(int capacity) {
        this.capacity = capacity;
    }

    public V get(K key) {
        lock.lock();
        try {
            Node<K, V> node = keyToNode.get(key);
            if (node == null) return null;
            updateFrequency(node);
            return node.value;
        } finally {
            lock.unlock();
        }
    }

    public void put(K key, V value) {
        if (capacity <= 0) return;
        lock.lock();
        try {
            Node<K, V> node = keyToNode.get(key);
            if (node != null) {
                node.value = value;
                updateFrequency(node);
                return;
            }

            if (keyToNode.size() >= capacity) {
                DoublyLinkedList<K, V> minList = freqToList.get(minFrequency);
                Node<K, V> evicted = minList.removeLast();
                if (evicted != null) {
                    keyToNode.remove(evicted.key);
                }
            }

            Node<K, V> newNode = new Node<>(key, value);
            keyToNode.put(key, newNode);
            minFrequency = 1;
            freqToList.computeIfAbsent(1, k -> new DoublyLinkedList<>()).addFirst(newNode);
        } finally {
            lock.unlock();
        }
    }

    private void updateFrequency(Node<K, V> node) {
        int oldFreq = node.frequency;
        DoublyLinkedList<K, V> oldList = freqToList.get(oldFreq);
        oldList.remove(node);

        if (oldFreq == minFrequency && oldList.size == 0) {
            minFrequency++;
        }

        node.frequency++;
        freqToList.computeIfAbsent(node.frequency, k -> new DoublyLinkedList<>()).addFirst(node);
    }
}
```
* **Sample Input & Output**:
  - *Input*: Capacity 2. `put(1, A)`, `put(2, B)`, `get(1)`, `put(3, C)`.
  - *Output*: Key 1 has frequency 2; Key 2 has frequency 1. Key 2 evicted (LFU); Keys 1 and 3 retained.

---

## ❄️ Category 6: Distributed Unique ID Generation & Bitwise Arithmetic (Q101 – Q120)

### ❓ Scenario 101: How do you implement Twitter Snowflake 64-bit ID Generator with NTP Clock Backwards Drift Defense?
* **Interviewer Evaluation**: Tests 64-bit binary bitwise manipulation, bitmasks, epoch offsets, millisecond sequence exhaustion, and NTP clock drift defenses.
* **Standout Technical Answer**:
  Twitter Snowflake generates 64-bit globally unique, roughly time-ordered integers fitting into a standard Java `long` (signed 64-bit):
  - **Bit 63 (1 bit)**: Sign bit (always `0` to keep the ID positive).
  - **Bits 62–22 (41 bits)**: Timestamp in milliseconds since a custom epoch (e.g. Nov 1, 2020). $2^{41}\text{ ms} \approx 69\text{ years}$ of unique IDs.
  - **Bits 21–17 (5 bits)**: Datacenter ID ($0\text{--}31$).
  - **Bits 16–12 (5 bits)**: Worker / Machine ID ($0\text{--}31$).
  - **Bits 11–0 (12 bits)**: Sequence counter ($0\text{--}4095$) per millisecond. Maximum throughput: $4,096,000\text{ IDs/sec}$ per node.
  **The Fatal Flaw: NTP Clock Backwards Drift**:
  Network Time Protocol (NTP) periodically synchronizes server clocks. If the clock jumps backwards by even $5\text{ms}$:
  - The generator will generate timestamps it already produced earlier, causing **duplicate ID collisions in production!**
  **Defense Invariants**:
  1. If `currentTimestamp < lastTimestamp`:
     - If drift $\le 5\text{ms}$, spin-wait / sleep until `currentTimestamp >= lastTimestamp`.
     - If drift $> 5\text{ms}$, reject and throw `ClockBackwardsException` or switch to a sequence borrowing logical clock.
  2. If requests in the same millisecond exhaust the 12-bit sequence (`sequence == 4096`), busy-spin until the next millisecond arrives.
* **Follow-Up Trap**: *"Why can't we use standard `UUID.randomUUID()` instead of Snowflake for database primary keys?"*
* **Winning Answer**: `UUID.randomUUID()` generates UUID v4, which is completely random. Inserting random UUIDs into a B-Tree clustered index (like MySQL InnoDB or Postgres) causes severe **Index Fragmentation and Page Splitting**: every insert lands on a random leaf page, forcing disk I/O and collapsing write performance. Snowflake IDs are roughly monotonically increasing, appending sequentially to the rightmost B-Tree page with $0$ page fragmentation!
* **Production Sample Code**:
```java
package com.vortex.lld.idgen;

public final class TwitterSnowflakeIdGenerator {
    private static final long CUSTOM_EPOCH = 1604188800000L; // 2020-11-01 00:00:00 UTC

    private static final long SEQUENCE_BITS = 12L;
    private static final long WORKER_ID_BITS = 5L;
    private static final long DATACENTER_ID_BITS = 5L;

    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);
    private static final long MAX_DATACENTER_ID = ~(-1L << DATACENTER_ID_BITS);
    private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);

    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
    private static final long DATACENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;
    private static final long TIMESTAMP_LEFT_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATACENTER_ID_BITS;

    private final long workerId;
    private final long datacenterId;
    private long sequence = 0L;
    private long lastTimestamp = -1L;

    public TwitterSnowflakeIdGenerator(long workerId, long datacenterId) {
        if (workerId > MAX_WORKER_ID || workerId < 0) {
            throw new IllegalArgumentException("Worker ID out of bounds: " + workerId);
        }
        if (datacenterId > MAX_DATACENTER_ID || datacenterId < 0) {
            throw new IllegalArgumentException("Datacenter ID out of bounds: " + datacenterId);
        }
        this.workerId = workerId;
        this.datacenterId = datacenterId;
    }

    public synchronized long nextId() {
        long currentTimestamp = timeGen();

        // NTP Clock Backward Drift Defense
        if (currentTimestamp < lastTimestamp) {
            long offset = lastTimestamp - currentTimestamp;
            if (offset <= 5) {
                try {
                    Thread.sleep(offset + 1);
                    currentTimestamp = timeGen();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            } else {
                throw new IllegalStateException("Clock moved backwards by " + offset + "ms. Refusing to generate ID.");
            }
        }

        if (lastTimestamp == currentTimestamp) {
            // Sequence increment within the same millisecond
            sequence = (sequence + 1) & SEQUENCE_MASK;
            if (sequence == 0) {
                // Sequence exhausted: Block until next millisecond
                currentTimestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }

        lastTimestamp = currentTimestamp;

        // 64-bit Bitwise Composition
        return ((currentTimestamp - CUSTOM_EPOCH) << TIMESTAMP_LEFT_SHIFT)
                | (datacenterId << DATACENTER_ID_SHIFT)
                | (workerId << WORKER_ID_SHIFT)
                | sequence;
    }

    private long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    private long timeGen() {
        return System.currentTimeMillis();
    }
}
```
* **Sample Input & Output**:
  - *Input*: Generate 5 successive IDs from `new TwitterSnowflakeIdGenerator(1, 1)`.
  - *Output*: Sequential, positive 64-bit numbers: e.g. `184518290123984896`, `184518290123984897`.

---

### ❓ Scenario 102: UUID v7 (Time-Ordered UUID) vs Twitter Snowflake: How do they compare in cloud architectures?
* **Interviewer Evaluation**: Tests RFC 9562 UUID v7 layout, distributed coordination costs, database storage overhead (64-bit vs 128-bit), and microservice autonomy.
* **Standout Technical Answer**:
  - **Snowflake (64-bit `BIGINT`)**:
    - *Pros*: Stored as an 8-byte primitive `long`. Extremely fast B-Tree indexing; compact storage in foreign key relations.
    - *Cons*: Requires **centralized worker ID coordination** (e.g. ZooKeeper or K8s StatefulSet pod ordinal) to ensure no two nodes share the same `(datacenterId, workerId)` pair.
  - **UUID v7 (128-bit RFC 9562)**:
    - *Structure*: 48 bits of Unix Epoch millisecond timestamp + 74 bits of cryptographically strong randomness + 6 version/variant bits.
    - *Pros*: **100% Autonomous Generation**. No worker ID coordination or ZooKeeper required! Any ephemeral serverless function or container can generate UUID v7 with zero collision risk.
    - *Cons*: Occupies 16 bytes (double the storage of Snowflake) and requires `UUID` or `BINARY(16)` database types.
* **Follow-Up Trap**: *"Why does UUID v7 solve the B-Tree index fragmentation problem that plagued UUID v4?"*
* **Winning Answer**: The most significant 48 bits of UUID v7 contain the Unix timestamp in milliseconds. Because the timestamp increases monotonically, new records are inserted at the end of the B-Tree index pages, preserving spatial locality and eliminating page splits!
* **Production Sample Code**:
```java
package com.vortex.lld.idgen;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.UUID;

public final class UuidV7Generator {
    private static final SecureRandom RANDOM = new SecureRandom();

    public static UUID generate() {
        long timestamp = System.currentTimeMillis();
        byte[] randomBytes = new byte[10];
        RANDOM.nextBytes(randomBytes);

        ByteBuffer buffer = ByteBuffer.allocate(16);
        // 48-bit timestamp
        buffer.putShort((short) (timestamp >>> 32));
        buffer.putInt((int) (timestamp & 0xFFFFFFFFL));
        // Random bytes
        buffer.put(randomBytes);

        buffer.flip();
        long mostSigBits = buffer.getLong();
        long leastSigBits = buffer.getLong();

        // Apply RFC 9562 Version 7 (0111) and Variant (10)
        mostSigBits = (mostSigBits & 0xFFFFFFFFFFFF0FFFL) | 0x0000000000007000L;
        leastSigBits = (leastSigBits & 0x3FFFFFFFFFFFFFFFL) | 0x8000000000000000L;

        return new UUID(mostSigBits, leastSigBits);
    }
}
```
* **Sample Input & Output**:
  - *Input*: `UuidV7Generator.generate()`.
  - *Output*: `019253f4-1234-7abc-89de-0123456789ab` (Monotonically time-ordered, zero coordination needed).

---

## 📝 Category 7: Write-Ahead Log (WAL) & Key-Value Storage Engines (Q121 – Q140)

### ❓ Scenario 121: How do you design an Append-Only Write-Ahead Log (WAL) with strict durability and crash recovery?
* **Interviewer Evaluation**: Tests file I/O channels, zero-copy, operating system page cache vs hardware disk sync (`FileChannel.force()`), binary serialization, and crash replay.
* **Standout Technical Answer**:
  In high-reliability databases (PostgreSQL, SQLite, Kafka, RocksDB), in-memory data structures are volatile. If the machine loses power, unsaved mutations vanish.
  **The Write-Ahead Log Contract**:
  1. **Log Before MemTable**: Before inserting a key-value pair into memory, the mutation MUST be sequentially written to the append-only WAL on disk.
  2. **Binary Frame Format**:
     - `[4 bytes: CRC32 Checksum] [4 bytes: OpCode] [4 bytes: KeyLength] [N bytes: Key] [4 bytes: ValLength] [M bytes: Value]`
  3. **Durability via fsync**:
     - Writing to a Java `FileOutputStream` or `FileChannel` merely writes bytes to the **OS Page Cache**, not the physical SSD!
     - Calling `fileChannel.force(false)` issues an `fsync` syscall, forcing the disk controller to flush its volatile cache to persistent flash memory.
  4. **Crash Recovery**:
     - On startup, the engine reads the WAL from byte 0 to EOF, verifies CRC32 checksums, and replays each operation into the in-memory MemTable. Corrupted trailing bytes from partial writes are truncated cleanly.
* **Follow-Up Trap**: *"Why is calling `fileChannel.force(true)` on every single write a performance killer, and how do databases optimize it?"*
* **Winning Answer**: An SSD `fsync` takes $0.5\text{--}2\text{ms}$, limiting throughput to only $\sim 500\text{ writes/sec}$. Production engines use **Group Commit**: multiple concurrent writing threads buffer their records into a shared queue, and a single thread calls `fileChannel.force()` once for the entire batch, boosting throughput to $100,000+\text{ writes/sec}$!
* **Production Sample Code**:
```java
package com.vortex.lld.storage;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.util.zip.CRC32;

public final class WriteAheadLog implements AutoCloseable {
    private final FileChannel channel;
    private final CRC32 crc = new CRC32();

    public WriteAheadLog(Path logPath) throws IOException {
        this.channel = FileChannel.open(logPath, 
            StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.READ);
        this.channel.position(channel.size()); // Append to end
    }

    public synchronized void appendPut(byte[] key, byte[] value) throws IOException {
        crc.reset();
        crc.update(1); // OpCode 1 = PUT
        crc.update(key);
        crc.update(value);
        long checksum = crc.getValue();

        // Total size: 8 (CRC) + 1 (Op) + 4 (kLen) + key + 4 (vLen) + val
        int recordSize = 8 + 1 + 4 + key.length + 4 + value.length;
        ByteBuffer buffer = ByteBuffer.allocate(recordSize);
        buffer.putLong(checksum);
        buffer.put((byte) 1);
        buffer.putInt(key.length);
        buffer.put(key);
        buffer.putInt(value.length);
        buffer.put(value);
        buffer.flip();

        while (buffer.hasRemaining()) {
            channel.write(buffer);
        }
        channel.force(false); // fsync to physical persistent disk!
    }

    @Override
    public void close() throws IOException {
        channel.close();
    }
}
```
* **Sample Input & Output**:
  - *Input*: Append `PUT key="usr_9" val="ACTIVE"` to WAL. Pull power simulation. Replay log.
  - *Output*: Record verified with matching CRC32; state fully restored into memory.

---

### ❓ Scenario 122: How does an LSM-Tree (Log-Structured Merge-Tree) MemTable balance fast writes with fast reads?
* **Interviewer Evaluation**: Tests MemTable mechanics, `ConcurrentSkipListMap`, SSTable immutability, Bloom Filters, and compaction basics.
* **Standout Technical Answer**:
  In traditional B-Tree databases, random writes require random disk I/O to locate and update pages.
  **LSM-Tree Storage Mechanics**:
  1. **MemTable**: All writes (`put`, `delete`) write to the append-only WAL first, then insert into an in-memory sorted map (**MemTable**), typically backed by a lock-free `ConcurrentSkipListMap`.
     - *Complexity*: Insertion in $O(\log N)$ with zero disk seeking.
  2. **SSTable Flush**: When MemTable reaches a size threshold (e.g. 64MB), it is frozen into a read-only Immutable MemTable and flushed sequentially to disk as a sorted file (**SSTable**).
  3. **Read Path Optimization**:
     - Check active MemTable.
     - Check immutable MemTable.
     - Search SSTable files on disk. To avoid checking every SSTable file, each SSTable has an in-memory **Bloom Filter**: if the Bloom filter says the key is absent, disk read is skipped entirely!
* **Follow-Up Trap**: *"How are deletions handled in an append-only LSM-Tree?"*
* **Winning Answer**: Via **Tombstones**. Deletions cannot delete disk records in place. The engine writes a special `DELETE` marker (tombstone) into the MemTable. During subsequent background **Compaction**, older SSTables containing the obsolete key are merged with the tombstone, permanently purging the record.
* **Production Sample Code**:
```java
package com.vortex.lld.storage;

import java.util.concurrent.ConcurrentSkipListMap;

public final class SimpleMemTable {
    private static final byte[] TOMBSTONE = new byte[0];
    private final ConcurrentSkipListMap<String, byte[]> memTable = new ConcurrentSkipListMap<>();
    private final long maxSizeBytes;
    private long currentSizeBytes = 0;

    public SimpleMemTable(long maxSizeBytes) {
        this.maxSizeBytes = maxSizeBytes;
    }

    public synchronized void put(String key, byte[] value) {
        byte[] old = memTable.put(key, value);
        currentSizeBytes += key.length() + value.length;
        if (old != null) currentSizeBytes -= old.length;
    }

    public synchronized void delete(String key) {
        put(key, TOMBSTONE); // Write Tombstone
    }

    public byte[] get(String key) {
        byte[] val = memTable.get(key);
        if (val == TOMBSTONE) return null; // Logically deleted
        return val;
    }

    public boolean isFlushNeeded() {
        return currentSizeBytes >= maxSizeBytes;
    }
}
```
* **Sample Input & Output**:
  - *Input*: `put("k1", "v1")`, `delete("k1")`, `get("k1")`.
  - *Output*: `get("k1")` returns `null` via tombstone resolution in $O(\log N)$ lock-free time.

---

## ⏰ Category 8: Distributed Task Scheduler & Priority Cron Engine (Q141 – Q160)

### ❓ Scenario 141: How do you design a high-precision Task Scheduler using Java's `DelayQueue`?
* **Interviewer Evaluation**: Tests `Delayed` interface implementation, nanosecond time comparison, `DelayQueue` blocking semantics, and dynamic cancellation.
* **Standout Technical Answer**:
  Standard timer loops sleep for fixed intervals and poll the database, wasting CPU cycles and causing up to $1\text{ second}$ of jitter.
  **`DelayQueue` Mechanics**:
  1. Backed by a thread-safe `PriorityQueue` of elements implementing `java.util.concurrent.Delayed`.
  2. Implements `long getDelay(TimeUnit unit)`: returns remaining duration until execution time.
  3. Implements `int compareTo(Delayed other)`: orders tasks by earliest execution timestamp.
  4. When worker threads call `delayQueue.take()`:
     - If the head element's delay $> 0$, the thread parks efficiently using `Condition.awaitNanos(delay)`.
     - The thread wakes up automatically when the earliest task expires or when a sooner task is offered!
     - Zero busy-waiting loops, sub-millisecond precision, and thread-safe dynamic task insertion.
* **Follow-Up Trap**: *"What happens if a task in `DelayQueue` throws an unhandled RuntimeException during execution?"*
* **Winning Answer**: In standard thread pools, an unhandled exception causes the worker thread to terminate silently without notifying supervisors. Production task schedulers must wrap every execution in a `try-catch(Throwable t)` block and log to an error monitoring sink.
* **Production Sample Code**:
```java
package com.vortex.lld.scheduler;

import java.time.Instant;
import java.util.concurrent.*;

public final class ScheduledTask implements Delayed {
    private final String taskId;
    private final Runnable action;
    private final long executionTimeNanos;

    public ScheduledTask(String taskId, Runnable action, Instant runAt) {
        this.taskId = taskId;
        this.action = action;
        long delayMillis = Math.max(0, runAt.toEpochMilli() - System.currentTimeMillis());
        this.executionTimeNanos = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(delayMillis);
    }

    @Override
    public long getDelay(TimeUnit unit) {
        long remaining = executionTimeNanos - System.nanoTime();
        return unit.convert(remaining, TimeUnit.NANOSECONDS);
    }

    @Override
    public int compareTo(Delayed o) {
        ScheduledTask other = (ScheduledTask) o;
        return Long.compare(this.executionTimeNanos, other.executionTimeNanos);
    }

    public void run() { action.run(); }
    public String getTaskId() { return taskId; }
}

public final class PrecisionTaskScheduler {
    private final DelayQueue<ScheduledTask> queue = new DelayQueue<>();
    private final ExecutorService workers = Executors.newFixedThreadPool(4);

    public PrecisionTaskScheduler() {
        Thread dispatcher = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    ScheduledTask task = queue.take(); // Blocks until task delay <= 0
                    workers.execute(task::run);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }, "Scheduler-Dispatcher");
        dispatcher.setDaemon(true);
        dispatcher.start();
    }

    public void schedule(String taskId, Runnable task, Instant runAt) {
        queue.put(new ScheduledTask(taskId, task, runAt));
    }
}
```
* **Sample Input & Output**:
  - *Input*: Schedule 3 tasks at $+1\text{s}$, $+500\text{ms}$, $+2\text{s}$.
  - *Output*: Executes in exact order: 500ms task, 1s task, 2s task with sub-millisecond accuracy.

---

### ❓ Scenario 142: How do you implement a Distributed Task Scheduler with Worker Leases and Fencing Tokens?
* **Interviewer Evaluation**: Tests distributed locks, worker node heartbeat leases, split-brain race conditions, and Martin Kleppmann's Fencing Token defense.
* **Standout Technical Answer**:
  In a multi-node cluster (K8s pods), multiple workers query a shared task queue.
  *The Distributed Race Condition*:
  Worker A claims Task #100 with a 30-second lease lock in Redis. Worker A encounters a 35-second Stop-The-World (STW) GC pause!
  The lock expires in Redis. Worker B claims Task #100, processes it, and writes results.
  Worker A wakes up from GC pause, believes it still owns the lock, and also writes results $\to$ **Data Corruption / Duplicate Execution!**
  **Fencing Token Architecture**:
  1. When acquiring a lease lock, the coordination store (PostgreSQL or Redis) increments and returns a monotonically increasing **Fencing Token** (e.g. `token = 101`).
  2. Every database write or side-effect includes this token: `UPDATE accounts SET balance = ... WHERE task_id = 100 AND last_fencing_token < 101`.
  3. When Worker A wakes up and attempts to commit with its stale `token = 100`, the storage engine rejects the write because `last_fencing_token` is already `101`. Stale worker mutations are blocked unconditionally!
* **Follow-Up Trap**: *"Why are Redis Redlock algorithms controversial for strict linearizable task leases?"*
* **Winning Answer**: As demonstrated by Martin Kleppmann, Redlock makes dangerous assumptions about synchronized physical clocks. Network delays, process pauses, and clock drift can cause two workers to believe they both hold the lock simultaneously. For strict correctness, use a consensus system with monotonic fencing tokens (ZooKeeper/Etcd/Raft) or a relational database with `SELECT ... FOR UPDATE`.
* **Production Sample Code**:
```java
package com.vortex.lld.scheduler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class FencedTaskExecutor {
    public boolean claimAndExecuteTask(Connection conn, String taskId, String workerId) throws SQLException {
        conn.setAutoCommit(false);
        try {
            // 1. Atomically claim task and increment fencing token using database lock
            String claimSql = """
                UPDATE distributed_tasks 
                SET worker_id = ?, 
                    fencing_token = fencing_token + 1, 
                    lease_expiry = NOW() + INTERVAL '30 SECONDS'
                WHERE task_id = ? AND (lease_expiry < NOW() OR worker_id IS NULL)
                RETURNING fencing_token
            """;
            
            long fencingToken;
            try (PreparedStatement ps = conn.prepareStatement(claimSql)) {
                ps.setString(1, workerId);
                ps.setString(2, taskId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();
                        return false; // Task already claimed by another active worker
                    }
                    fencingToken = rs.getLong(1);
                }
            }

            // 2. Perform business task
            System.out.println("Worker " + workerId + " executing task " + taskId + " with Fencing Token: " + fencingToken);

            // 3. Commit with fencing token validation
            String commitSql = """
                UPDATE task_results 
                SET status = 'COMPLETED', last_token = ? 
                WHERE task_id = ? AND last_token < ?
            """;
            try (PreparedStatement ps = conn.prepareStatement(commitSql)) {
                ps.setLong(1, fencingToken);
                ps.setString(2, taskId);
                ps.setLong(3, fencingToken);
                ps.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        }
    }
}
```
* **Sample Input & Output**:
  - *Input*: Worker 1 and Worker 2 contend for task `"TASK-99"`.
  - *Output*: Worker 1 claims token `101` and commits; Worker 2 is rejected. Zero duplicate processing.

---

## 🛑 Category 9: Machine Coding Anti-Patterns & Concurrency Bugs (Q161 – Q180)

### ❓ Scenario 161: Why does using a standard `HashMap` in concurrent environments cause infinite loops and 100% CPU usage?
* **Interviewer Evaluation**: Tests `HashMap` internal memory layout, bucket linked list / red-black tree resizing mechanics, circular pointer hazards, and `ConcurrentHashMap` CAS bin locking.
* **Standout Technical Answer**:
  In older JDK versions (JDK 7), concurrent `put()` invocations during table resizing (`transfer()` method) triggered a famous race condition:
  - When resizing, elements in a bucket were transferred to the new table using head-insertion (`newTable[i] = e; e.next = next;`).
  - If Thread A was preempted while reading pointers `e` and `next`, and Thread B completed resizing, the linked list pointers were reversed.
  - When Thread A resumed, it created a **circular linked list** (`A -> B -> A`).
  - Subsequent `map.get()` calls on that bucket entered an infinite loop traversing the cycle, consuming 100% CPU on all available cores!
  In JDK 8+, `HashMap` maintains insertion order during resize, preventing circular loops; however, concurrent writes cause **Silent Data Loss** and race conditions where tree nodes become corrupted, resulting in `ClassCastException` or missing entries.
  **The Fix**: Always use `ConcurrentHashMap`, which employs lock-free CAS on empty bucket heads and fine-grained `synchronized` bin locks on non-empty bins.
* **Follow-Up Trap**: *"Why is `concurrentHashMap.get()` completely lock-free, even while other threads are executing `put()`?"*
* **Winning Answer**: `Node.val` and `Node.next` are declared `volatile`. Volatile reads provide memory visibility guarantees without acquiring locks. If a bin is being converted to a red-black tree, `ConcurrentHashMap` uses a `TreeBin` with a read-write lock where readers verify a lock state without blocking writers unless a rebalance is underway.
* **Production Sample Code**:
```java
package com.vortex.lld.antipatterns;

import java.util.concurrent.ConcurrentHashMap;

public final class ThreadSafeMetricsCollector {
    // ❌ FATAL ANTI-PATTERN: private final Map<String, Long> counters = new HashMap<>();
    // ✅ PRODUCTION FIX: ConcurrentHashMap with atomic compute / merge
    private final ConcurrentHashMap<String, Long> counters = new ConcurrentHashMap<>();

    public void recordEvent(String eventType) {
        // Atomic lock-free increment
        counters.merge(eventType, 1L, Long::sum);
    }

    public long getCount(String eventType) {
        return counters.getOrDefault(eventType, 0L); // 100% lock-free read
    }
}
```
* **Sample Input & Output**:
  - *Input*: 1,000 threads invoke `recordEvent("LOGIN")` concurrently.
  - *Output*: Final count is exactly `1000`. Zero race conditions, zero CPU spinning.

---

### ❓ Scenario 162: Deadlock via Out-of-Order Lock Acquisition in Multi-Account Banking Transfers
* **Interviewer Evaluation**: Tests Coffman's deadlock conditions (Hold and Wait, Circular Wait), global resource ordering, and `System.identityHashCode` tie-breaking.
* **Standout Technical Answer**:
  Consider transferring funds between two bank accounts:
  ```java
  // ❌ FATAL ANTI-PATTERN: Inconsistent lock ordering!
  public void transfer(Account from, Account to, long amount) {
      synchronized(from) {
          synchronized(to) {
              from.debit(amount);
              to.credit(amount);
          }
      }
  }
  ```
  **The Deadlock**:
  - Thread 1: `transfer(AccountA, AccountB, 100)` $\to$ Acquires lock on `AccountA`, waits for `AccountB`.
  - Thread 2: `transfer(AccountB, AccountA, 200)` $\to$ Acquires lock on `AccountB`, waits for `AccountA`.
  - **Circular Wait Condition**: Neither thread can proceed. The entire banking system freezes!
  **The Universal Fix: Strict Global Lock Ordering**:
  Sort the locks by a unique immutable property (e.g. `accountId` or `System.identityHashCode`).
  Both threads acquire locks in the exact same deterministic order ($A$ before $B$). Circular wait is mathematically eliminated!
* **Follow-Up Trap**: *"What happens if two accounts have the exact same tie-breaker hash code?"*
* **Winning Answer**: Implement a **Tie-Breaking Lock**. Acquire a third dedicated static mutex lock (`synchronized(tieLock)`) before acquiring both account locks, guaranteeing serialized access during rare hash collisions.
* **Production Sample Code**:
```java
package com.vortex.lld.antipatterns;

public final class DeadlockFreeTransferService {
    private static final Object TIE_BREAKER_LOCK = new Object();

    public static class Account {
        private final String accountId;
        private long balanceCents;

        public Account(String id, long balance) { this.accountId = id; this.balanceCents = balance; }
        public String getId() { return accountId; }
        public void debit(long amt) { balanceCents -= amt; }
        public void credit(long amt) { balanceCents += amt; }
        public long getBalance() { return balanceCents; }
    }

    public void transfer(Account from, Account to, long amount) {
        int comparison = from.getId().compareTo(to.getId());

        if (comparison < 0) {
            // Lock 'from' first, then 'to'
            synchronized (from) {
                synchronized (to) {
                    executeTransfer(from, to, amount);
                }
            }
        } else if (comparison > 0) {
            // Lock 'to' first, then 'from'
            synchronized (to) {
                synchronized (from) {
                    executeTransfer(from, to, amount);
                }
            }
        } else {
            // Identical account IDs (rare collision): Serialized via tie-breaker
            synchronized (TIE_BREAKER_LOCK) {
                synchronized (from) {
                    synchronized (to) {
                        executeTransfer(from, to, amount);
                    }
                }
            }
        }
    }

    private void executeTransfer(Account from, Account to, long amount) {
        from.debit(amount);
        to.credit(amount);
    }
}
```
* **Sample Input & Output**:
  - *Input*: 500 threads execute cross transfers between Account 1 and Account 2.
  - *Output*: Zero deadlocks; 100% consistent ledger balances.

---

### ❓ Scenario 163: Floating-Point Math Corrupting Financial Ledger Calculations
* **Interviewer Evaluation**: Tests IEEE 754 binary floating-point representation, rounding drift, `double` vs `BigDecimal` vs integer cents, and monetary invariants.
* **Standout Technical Answer**:
  In IEEE 754, binary floating-point types (`float`, `double`) cannot accurately represent base-10 fractional decimals like $0.1$ or $0.01$.
  - In Java: `double total = 0.1 + 0.2;` evaluates to `0.30000000000000004`!
  - If a payroll or trading system computes $10,000,000$ transactions using `double`:
    - Fractional rounding errors compound across calculations.
    - Ledgers fail reconciliation checks: `sum(debits) != sum(credits)`.
    - Legal compliance and audit audits fail immediately.
  **The Standards**:
  1. **Option A (Preferred for High Performance)**: Store currency as **64-bit integer cents (`long`)** or micros ($10^{-6}$). $100.50$ is stored as `10050`. Operations use pure integer arithmetic with zero floating-point drift.
  2. **Option B (Preferred for Complex Taxes / Interest)**: Use `java.math.BigDecimal` with explicit scale and `RoundingMode.HALF_UP`. Always initialize `BigDecimal` via `BigDecimal.valueOf(val)` or `new BigDecimal("0.1")`, **never** `new BigDecimal(0.1)` (which preserves binary double inaccuracy).
* **Follow-Up Trap**: *"Why does `new BigDecimal(0.1)` produce `0.1000000000000000055511151231257827021181583404541015625`?"*
* **Winning Answer**: Because the double literal `0.1` is already inaccurate in binary before passing into the constructor! Passing the String `"0.1"` parses the decimal digits directly into exact arbitrary-precision representations.
* **Production Sample Code**:
```java
package com.vortex.lld.antipatterns;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class FinancialPrecisionCalculator {
    // Integer Cents: Ultra-fast, zero drift
    public static long addCents(long balanceCents, long depositCents) {
        return Math.addExact(balanceCents, depositCents); // Guards against long overflow!
    }

    // BigDecimal: Arbitrary precision for fractional interest rates
    public static BigDecimal calculateInterest(BigDecimal principal, BigDecimal ratePercent, int years) {
        BigDecimal rateFraction = ratePercent.divide(BigDecimal.valueOf(100), 8, RoundingMode.HALF_UP);
        BigDecimal onePlusRate = BigDecimal.ONE.add(rateFraction);
        BigDecimal compoundFactor = onePlusRate.pow(years);
        return principal.multiply(compoundFactor).setScale(2, RoundingMode.HALF_UP);
    }
}
```
* **Sample Input & Output**:
  - *Input*: Principal $1,000, Rate 5.25%, 3 years.
  - *Output*: Exactly `$1165.91` with zero rounding loss.

---

## 🔥 Category 10: Production War-Room Outage Incidents & SRE Post-Mortems (Q181 – Q200)

### ❓ Scenario 181: War-Room Forensics: Parking Lot Overbooking Race Condition Under 500 Concurrent Entry Gates
* **Incident Summary**: At the opening of a 100,000-seat stadium, 500 automated license plate entry gates admitted 10,450 cars into a 10,000-capacity lot. 450 vehicles were trapped in drive aisles unable to find spots, causing massive gridlock and local police intervention.
* **Root Cause Analysis (RCA)**:
  ```java
  // ❌ FATAL PRODUCTION BUG: Check-Then-Act Race Condition!
  public class ParkingFloor {
      private int availableSpots = 1000;
      
      public boolean parkCar(Vehicle v) {
          if (availableSpots > 0) { // Check
              Thread.sleep(10);     // Window of vulnerability: 50 gates check simultaneously!
              availableSpots--;     // Act (Non-atomic decrement!)
              return true;
          }
          return false;
      }
  }
  ```
  `availableSpots--` is not atomic (compiles to read, increment, write). When 500 gates executed concurrently, multiple gates observed `availableSpots > 0` before any gate wrote the decremented count, allowing hundreds of extra vehicles to enter!
* **Remediation**:
  ```java
  // ✅ PRODUCTION REFACTORED: AtomicInteger CAS Reservation
  public class ParkingFloor {
      private final AtomicInteger availableSpots = new AtomicInteger(1000);

      public boolean parkCar(Vehicle v) {
          int remaining;
          do {
              remaining = availableSpots.get();
              if (remaining <= 0) return false; // Strictly full
          } while (!availableSpots.compareAndSet(remaining, remaining - 1));
          return true; // Atomically claimed spot!
      }
  }
  ```

---

### ❓ Scenario 182: War-Room Forensics: Redis Lua Script Blocking Entire Cluster in Distributed Rate Limiter
* **Incident Summary**: During a Black Friday marketing push, a distributed rate limiter running inside an API gateway caused a complete outage of the entire Redis cluster. All microservices relying on Redis for session caching and product catalogs timed out with `JedisConnectionException: Read timed out`.
* **Root Cause Analysis (RCA)**:
  The rate limiter team implemented a Sliding Window Log in a Redis Lua script using `ZRANGEBYSCORE` and `ZREMRANGEBYSCORE`.
  Under 100,000 requests/sec from a scraping botnet, the sorted set for a specific IP grew to 2,000,000 elements. The Lua script executed an $O(N)$ sweep across 2,000,000 timestamps. Because Redis is single-threaded, the entire Redis instance was frozen for $4.2\text{ seconds}$ per execution!
* **Remediation**:
  Replaced Sliding Window Log with the **Sliding Window Counter** using simple `INCRBY` and integer interpolation. Maximum script execution time dropped from $4,200\text{ms}$ to $0.02\text{ms}$ ($20\,\mu\text{s}$), restoring cluster health.

---

### ❓ Scenario 183: War-Room Forensics: Snowflake ID Duplicate Collision during NTP Leap Second Adjustment
* **Incident Summary**: At 23:59:60 UTC during an international leap-second correction, a financial transaction database crashed with `DuplicateKeyException: PRIMARY KEY constraint violation on transaction_id`. Over 12,000 transactions were rejected.
* **Root Cause Analysis (RCA)**:
  The company's in-house Snowflake ID generator lacked backward clock drift handling. During the leap second, the Linux kernel NTP daemon stepped the clock backward by $1,000\text{ms}$. The generator re-generated the exact same sequence of timestamps it had issued one second earlier, colliding with existing primary keys!
* **Remediation**:
  Configured NTP servers to use **Clock Slewing (`-x` flag / Chrony slew)** rather than clock stepping, smoothing time adjustments over thousands of seconds. Updated the Snowflake generator to reject backward jumps $>5\text{ms}$ and sleep wait for minor drift.

---

### ❓ Scenario 3: How do you design an Electric Vehicle (EV) Charging Station spot allocator with Dynamic Power Grid Load Balancing?
* **Interviewer Evaluation**: Tests domain modeling of physical electrical constraints, multi-resource reservation (Parking Spot + Power Grid kW capacity), dynamic power throttling, and thread-safe reservation.
* **Standout Technical Answer**:
  In a smart parking lot with 50 EV charging spots, allocating a spot is not simply a physical geometry problem. The parking lot's electrical transformer has a hard physical capacity (e.g. $500\text{ kW}$ total power).
  - A Level-2 charger draws $7\text{ kW}$.
  - A DC Fast Charger draws $50\text{ kW}$.
  - An Ultra-Fast Charger draws $250\text{ kW}$.
  If 3 ultra-fast cars and 10 fast cars connect simultaneously, total load demands $1,250\text{ kW}$, blowing the commercial transformer fuse!
  **Multi-Resource Allocation Architecture**:
  1. `EvChargingSpot` encapsulates both a physical `SpotType` and an `ElectricCharger` entity.
  2. `PowerGridLoadManager` maintains an `AtomicInteger currentGridLoadKw` and `final int maxTransformerCapacityKw`.
  3. When an EV requests charging:
     - Reserve physical spot via CAS.
     - Check grid capacity: `currentGridLoadKw.addAndGet(requestedKw)`.
     - If load exceeds capacity: dynamically **throttle** existing charging cars (e.g. degrade $50\text{ kW}$ cars to $25\text{ kW}$) or queue the car for smart overnight delayed charging.
* **Follow-Up Trap**: *"How do you handle fair power distribution when 100 EVs are plugged in and grid capacity is insufficient for maximum speed?"*
* **Winning Answer**: Implement the **Proportional Fair-Sharing Algorithm**: compute each car's allocated power as $\text{AllocatedKw}_i = \text{MaxCapacity} \times \frac{\text{Deficit}_i}{\sum \text{Deficit}}$, prioritizing vehicles with the lowest current battery percentage (State of Charge - SoC) and earliest departure deadlines.
* **Production Sample Code**:
```java
package com.vortex.lld.parkinglot;

import java.util.concurrent.atomic.AtomicInteger;

public final class PowerGridLoadManager {
    private final int maxGridKw;
    private final AtomicInteger currentLoadKw = new AtomicInteger(0);

    public PowerGridLoadManager(int maxGridKw) {
        this.maxGridKw = maxGridKw;
    }

    public boolean tryAllocatePower(int requestedKw) {
        while (true) {
            int current = currentLoadKw.get();
            if (current + requestedKw > maxGridKw) {
                return false; // Grid transformer capacity exceeded!
            }
            if (currentLoadKw.compareAndSet(current, current + requestedKw)) {
                return true; // Atomically reserved power!
            }
        }
    }

    public void releasePower(int releasedKw) {
        currentLoadKw.addAndGet(-releasedKw);
    }

    public int getCurrentLoadKw() { return currentLoadKw.get(); }
}
```
* **Sample Input & Output**:
  - *Input*: Grid capacity 500 kW. Allocate two 250 kW chargers, then attempt 50 kW charger.
  - *Output*: First two allocated successfully (500 kW total); third rejected safely preventing transformer burnout.

---

### ❓ Scenario 23: How do you design a Tiered Multi-Tenant Rate Limiter with Dynamic Runtime Quota Reloading?
* **Interviewer Evaluation**: Tests multi-tenancy, dynamic configuration updates without server restart, tenant tier policies (Free vs Pro vs Enterprise), and thread-safe cache invalidation.
* **Standout Technical Answer**:
  In a SaaS platform (e.g. Stripe or AWS), thousands of tenants share the same API gateways.
  - Tenant A (Free Tier): $10\text{ req/min}$.
  - Tenant B (Pro Tier): $1,000\text{ req/min}$.
  - Tenant C (Enterprise Tier): $50,000\text{ req/min}$.
  **Requirements**:
  1. Rate limiter must isolate tenants: Tenant A exhausting their quota must never impact Tenant B.
  2. Upgrading a subscription from Free to Pro must immediately update the rate limit in memory without bouncing the JVM or dropping active TCP connections.
  **Architecture**:
  - `ConcurrentHashMap<String, TenantRateLimiterContext> tenantLimiters`.
  - `TenantRateLimiterContext` encapsulates a `LockFreeTokenBucket` and a `volatile TenantQuotaPolicy policy`.
  - When a tenant upgrades, a webhook publishes a message to Redis Pub/Sub; all gateway instances receive the event and call `context.updatePolicy(newPolicy)`, which seamlessly resizes bucket capacity in place.
* **Follow-Up Trap**: *"What happens to current tokens in the bucket when a tenant's capacity is downgraded from 10,000 to 10?"*
* **Winning Answer**: The token bucket's lazy refill formula uses `Math.min(newCapacity, currentTokens)`. Upon the very next request, `currentTokens` is capped at the new lower capacity (`10`), preventing the tenant from exploiting leftover tokens.
* **Production Sample Code**:
```java
package com.vortex.lld.ratelimiter;

import java.util.concurrent.ConcurrentHashMap;

public final class MultiTenantRateLimiterManager {
    public record TenantQuotaPolicy(double maxBurst, double refillPerSec) {}

    private final ConcurrentHashMap<String, LockFreeTokenBucket> limiters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, TenantQuotaPolicy> policies = new ConcurrentHashMap<>();

    public void registerTenant(String tenantId, TenantQuotaPolicy policy) {
        policies.put(tenantId, policy);
        limiters.put(tenantId, new LockFreeTokenBucket(policy.maxBurst(), policy.refillPerSec()));
    }

    public boolean allowTenantRequest(String tenantId) {
        LockFreeTokenBucket bucket = limiters.get(tenantId);
        if (bucket == null) {
            // Default unmetered or fallback policy
            return false;
        }
        return bucket.tryAcquire(1.0);
    }

    // Dynamic Runtime Hot-Reload
    public void updateTenantPolicy(String tenantId, TenantQuotaPolicy newPolicy) {
        policies.put(tenantId, newPolicy);
        limiters.put(tenantId, new LockFreeTokenBucket(newPolicy.maxBurst(), newPolicy.refillPerSec()));
    }
}
```
* **Sample Input & Output**:
  - *Input*: Free tenant fires 15 requests (capped at 10); admin upgrades to Pro; tenant fires 100 requests.
  - *Output*: First burst capped at 10; post-upgrade requests immediately succeed without restart.

---

### ❓ Scenario 62: Multi-Car Elevator Destination Dispatching: How does Schindler/Otis ETA Dispatching work?
* **Interviewer Evaluation**: Tests destination dispatch heuristics, passenger grouping, ETA minimization algorithms, and car load factor tracking.
* **Standout Technical Answer**:
  In modern skyscrapers (60+ floors), traditional elevators where users press "UP" or "DOWN" at the hall station result in massive congestion: 20 passengers crowd into the first car, and the car stops at every single floor from 2 to 21.
  **Destination Dispatch Architecture**:
  1. **Lobby Terminal Kiosk**: The passenger selects their destination floor (e.g. Floor 42) *before* boarding.
  2. The kiosk immediately responds: "Proceed to Elevator C".
  3. **The Dispatcher Cost Function**:
     For every elevator car $i$, the dispatcher calculates a cost score:
     $$\text{Cost}_i = \text{ETA}_i + \text{CapacityPenalty}_i + \text{DirectionPenalty}_i$$
     - $\text{ETA}_i$: Time for Car $i$ to reach the passenger's boarding floor given its current route.
     - $\text{DirectionPenalty}_i$: High penalty if car is moving in the opposite direction.
     - $\text{PassengerGroupingBonus}$: Substantial negative penalty (bonus) if Car $i$ is **already scheduled to stop at Floor 42**!
  4. The car with the minimum cost score is assigned.
  5. Passengers heading to floors 40–45 are grouped into Car C; passengers heading to floors 15–20 are grouped into Car A, slashing total building transit time by $40\%$.
* **Follow-Up Trap**: *"What happens if a passenger selects Floor 42 at the kiosk but walks into Elevator B instead?"*
* **Winning Answer**: Modern destination dispatch systems enforce car boarding using **Optical Turnstiles and Destination Indicators**: the interior of the elevator car has **no floor buttons**! The passenger cannot alter the car's destinations once inside; the car strictly travels to its assigned stops.
* **Production Sample Code**:
```java
package com.vortex.lld.elevator;

import java.util.List;

public final class DestinationDispatchEngine {
    public record HallRequest(int sourceFloor, int destinationFloor) {}

    public static int selectBestCar(List<ElevatorCar> cars, HallRequest request) {
        int bestCarId = -1;
        int lowestCost = Integer.MAX_VALUE;

        for (ElevatorCar car : cars) {
            int cost = calculateCost(car, request);
            if (cost < lowestCost) {
                lowestCost = cost;
                bestCarId = car.getCurrentFloor(); // In production, car.getId()
            }
        }
        return bestCarId;
    }

    private static int calculateCost(ElevatorCar car, HallRequest req) {
        int distance = Math.abs(car.getCurrentFloor() - req.sourceFloor());
        int directionPenalty = 0;

        if (car.getDirection() == Direction.UP && req.sourceFloor() < car.getCurrentFloor()) {
            directionPenalty = 50; // Car has already passed the floor
        } else if (car.getDirection() == Direction.DOWN && req.sourceFloor() > car.getCurrentFloor()) {
            directionPenalty = 50;
        }

        return distance + directionPenalty;
    }
}
```
* **Sample Input & Output**:
  - *Input*: Car 1 at Floor 5 moving UP; Car 2 at Floor 40 moving DOWN. Hall request at Floor 8 heading to 20.
  - *Output*: Selects Car 1 (lowest cost, natural direction alignment).

---

### ❓ Scenario 83: How do you design a 2-Tier Cache Architecture with Caffeine L1 Near-Cache and Redis L2 Distributed Cache?
* **Interviewer Evaluation**: Tests multi-tier caching, L1 cache invalidation pub/sub, cache consistency across nodes, and read-through mechanics.
* **Standout Technical Answer**:
  - **The Problem**: Querying a remote Redis cluster takes $1\text{--}3\text{ms}$ over the network. In ultra-high-throughput endpoints ($200,000\text{ req/sec}$), network socket I/O to Redis becomes the bottleneck.
  - **The 2-Tier Solution**:
    1. **L1 Near-Cache (In-Memory Caffeine)**: Lives inside the JVM process heap. Access takes $<50\text{ns}$ with zero network I/O.
    2. **L2 Distributed Cache (Redis)**: Shared across all 50 microservice instances. Access takes $1\text{--}2\text{ms}$.
    3. **Read Flow**: Check L1 $\to$ if miss, Check L2 $\to$ if miss, Query DB and populate both L2 and L1.
    4. **Write Flow & Invalidation**:
       - When a mutation occurs on Node A, Node A updates DB and deletes key in Redis L2.
       - Node A publishes a message to Redis Pub/Sub topic `"CACHE_INVALIDATE:user_123"`.
       - Nodes B, C, D listen to the topic and evict `user_123` from their local L1 Caffeine caches immediately.
* **Follow-Up Trap**: *"What happens if a network partition drops a Redis Pub/Sub invalidation message?"*
* **Winning Answer**: Node B would hold stale data indefinitely. To guard against dropped pub/sub messages, configure L1 Caffeine caches with a **short Time-To-Live (TTL)** (e.g. `expireAfterWrite = 30s`). Stale data can persist for at most 30 seconds before automatic eviction.
* **Production Sample Code**:
```java
package com.vortex.lld.cache;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public final class TwoTierCacheManager<K, V> {
    // L1: Fast In-Memory JVM Cache (<100ns)
    private final ConcurrentHashMap<K, V> l1LocalCache = new ConcurrentHashMap<>();
    // L2: Simulation of Distributed Cache (Redis)
    private final ConcurrentHashMap<K, V> l2DistributedCache = new ConcurrentHashMap<>();

    public V get(K key, Function<K, V> dbFallback) {
        // 1. Check L1
        V val = l1LocalCache.get(key);
        if (val != null) return val;

        // 2. Check L2
        val = l2DistributedCache.get(key);
        if (val != null) {
            l1LocalCache.put(key, val); // Populate L1
            return val;
        }

        // 3. Fallback to Database
        val = dbFallback.apply(key);
        if (val != null) {
            l2DistributedCache.put(key, val);
            l1LocalCache.put(key, val);
        }
        return val;
    }

    // Invalidation Handler triggered by Redis Pub/Sub subscriber
    public void onRemoteInvalidationReceived(K key) {
        l1LocalCache.remove(key); // Evict from local heap
    }
}
```
* **Sample Input & Output**:
  - *Input*: First read triggers DB query; second read hits L1 in 40ns; remote update triggers L1 eviction.
  - *Output*: Zero stale reads; sub-microsecond latency on 99% of requests.

---

### ❓ Scenario 84: Cache Stampede (Thundering Herd) Defense: How does the Singleflight Mutex Pattern prevent database collapse?
* **Interviewer Evaluation**: Tests cache stampede dynamics, hot key expiration, Singleflight promise pattern (`CompletableFuture`), and database protection.
* **Standout Technical Answer**:
  **The Cache Stampede Catastrophe**:
  A hot product page (e.g. iPhone 15 launch) has 50,000 requests/sec. Its cached entry has a 10-minute TTL.
  At 12:10:00, the cache key expires.
  In the next $100\text{ms}$, 5,000 concurrent requests hit the cache and find `null`. All 5,000 requests fire the identical heavy SQL query to PostgreSQL simultaneously $\to$ **Database Connection Pool Exhaustion, CPU 100%, and Total Platform Crash!**
  **The Singleflight Mutex Solution**:
  1. Maintain a map of in-flight promises: `ConcurrentHashMap<Key, CompletableFuture<Value>> inFlight`.
  2. When request arrives and cache is empty:
     - Atomically compute `inFlight.computeIfAbsent(key, k -> CompletableFuture.supplyAsync(() -> queryDb(k)))`.
     - The first request begins the database query.
     - The subsequent 4,999 requests receive the **exact same `CompletableFuture`** and block waiting on it without touching the database!
  3. When the query completes, the future completes, populating the cache and resolving all 5,000 requests simultaneously with **exactly 1 database query**!
* **Follow-Up Trap**: *"What happens if the single database query fails with an exception?"*
* **Winning Answer**: The future completes exceptionally, and all waiting callers fail. The in-flight map entry **must** be removed in a `whenComplete()` callback so subsequent requests retry instead of permanently failing on a cached exceptional future.
* **Production Sample Code**:
```java
package com.vortex.lld.cache;

import java.util.concurrent.*;
import java.util.function.Function;

public final class SingleflightCacheProtector<K, V> {
    private final ConcurrentHashMap<K, CompletableFuture<V>> inFlight = new ConcurrentHashMap<>();

    public V execute(K key, Function<K, V> expensiveDbLoader) throws Exception {
        CompletableFuture<V> future = inFlight.computeIfAbsent(key, k -> {
            CompletableFuture<V> promise = new CompletableFuture<>();
            CompletableFuture.runAsync(() -> {
                try {
                    V result = expensiveDbLoader.apply(k);
                    promise.complete(result);
                } catch (Throwable t) {
                    promise.completeExceptionally(t);
                } finally {
                    inFlight.remove(k); // Clean up in-flight token upon completion
                }
            });
            return promise;
        });

        return future.get(5, TimeUnit.SECONDS); // Await shared single DB query!
    }
}
```
* **Sample Input & Output**:
  - *Input*: 50 concurrent threads call `execute("HOT_KEY", k -> { sleep(200); return "DATA"; })`.
  - *Output*: Database loader executes exactly 1 time; all 50 threads return identical data in 200ms.

---

### ❓ Scenario 143: How do you design an In-Memory Cron Expression Evaluation Engine?
* **Interviewer Evaluation**: Tests parsing of 5-field Cron syntax (`minute, hour, day-of-month, month, day-of-week`), `java.time` calendar arithmetic, bitsets, and leap year handling.
* **Standout Technical Answer**:
  A Cron expression consists of 5 whitespace-separated fields:
  `[minute: 0-59] [hour: 0-23] [dayOfMonth: 1-31] [month: 1-12] [dayOfWeek: 0-7]`
  **Efficient Evaluation Mechanics**:
  1. **BitSet Representation**:
     - Pre-parse each field into a `java.util.BitSet` (e.g. `minuteBits.get(15) == true`).
     - Supports wildcards `*`, ranges `10-15`, steps `*/5`, and comma lists `1,15,30`.
  2. **Next Instant Search Algorithm**:
     - Start from `Instant.now().plus(Duration.ofMinutes(1)).truncatedTo(ChronoUnit.MINUTES)`.
     - Evaluate fields from coarsest to finest: Month $\to$ Day $\to$ Hour $\to$ Minute.
     - If month does not match, advance to 1st day of next month and restart.
     - If day does not match, advance to next day at 00:00 and restart.
     - If all fields match, return that `ZonedDateTime.toInstant()`.
* **Follow-Up Trap**: *"How do you handle Daylight Saving Time (DST) spring-forward jumps where an hour (02:00) does not exist?"*
* **Winning Answer**: Use `ZoneId` with `ZoneRules.getValidOffsets()`. If a cron is scheduled for 02:30 on the night the clock jumps from 02:00 to 03:00, the timestamp is invalid; the engine advances to the gap transition point (03:00) according to standard POSIX cron rules.
* **Production Sample Code**:
```java
package com.vortex.lld.scheduler;

import java.time.ZonedDateTime;
import java.util.BitSet;

public final class SimpleCronFieldMatcher {
    private final BitSet allowedMinutes = new BitSet(60);

    // Parses simple minutes format: e.g. "*/15" (every 15 minutes)
    public SimpleCronFieldMatcher(String minuteExpression) {
        if ("*".equals(minuteExpression)) {
            allowedMinutes.set(0, 60);
        } else if (minuteExpression.startsWith("*/")) {
            int step = Integer.parseInt(minuteExpression.substring(2));
            for (int i = 0; i < 60; i += step) allowedMinutes.set(i);
        } else {
            allowedMinutes.set(Integer.parseInt(minuteExpression));
        }
    }

    public boolean matchesMinute(int minute) {
        return allowedMinutes.get(minute);
    }

    public ZonedDateTime findNextMatch(ZonedDateTime from) {
        ZonedDateTime candidate = from.plusMinutes(1).withSecond(0).withNano(0);
        while (!matchesMinute(candidate.getMinute())) {
            candidate = candidate.plusMinutes(1);
        }
        return candidate;
    }
}
```
* **Sample Input & Output**:
  - *Input*: Expression `"*/15"`, current time 12:04.
  - *Output*: Next execution time calculated as 12:15:00.

---

### ❓ Scenario 184: War-Room Forensics: Cache Stampede Outage Melts PostgreSQL Primary on Cyber Monday
* **Incident Summary**: At 09:00 on Cyber Monday, an e-commerce site announced a 50% discount on an OLED TV. Within 3 seconds of the cached product record expiring, PostgreSQL CPU spiked to 100%, active database connections saturated the HikariCP pool limit of 150, and 12,000 customers encountered HTTP 504 Gateway Timeouts.
* **Root Cause Analysis (RCA)**:
  15,000 concurrent requests requested the expired product record. With zero cache stampede protection, all 15,000 requests issued `SELECT * FROM products p JOIN inventory i ...` simultaneously, crashing the database engine.
* **Remediation**:
  Wrapped product cache loading in the **Singleflight Mutex Pattern** (`CompletableFuture.computeIfAbsent`). During the next flash sale test, database load remained flat at exactly 1 query during cache rehydration, handling 40,000 RPS effortlessly.

---

### ❓ Scenario 185: War-Room Forensics: Elevator Destination Dispatch Deadlock during Morning Rush Hour
* **Incident Summary**: At 08:45 AM in an 80-story corporate headquarters, 6 elevator cars came to a complete halt across floors 12, 25, and 40 with doors closed, trapping 80 executives. The automated building management system raised critical alarm bells.
* **Root Cause Analysis (RCA)**:
  The destination dispatch software implemented cross-car rebalancing using nested locks:
  - Thread A (rebalancing Car 1 to Car 2): `synchronized(car1) { synchronized(car2) { ... } }`
  - Thread B (rebalancing Car 2 to Car 1): `synchronized(car2) { synchronized(car1) { ... } }`
  Under morning rush-hour traffic, Thread A and Thread B deadlocked each other on car state locks, freezing car dispatch routines!
* **Remediation**:
  Refactored elevator state management to use **Global Deterministic Lock Ordering by Car ID**:
  ```java
  ElevatorCar firstLock = car1.getId() < car2.getId() ? car1 : car2;
  ElevatorCar secondLock = car1.getId() < car2.getId() ? car2 : car1;
  synchronized(firstLock) { synchronized(secondLock) { ... } }
  ```
  Circular wait was mathematically eliminated.

---

### ❓ Scenario 186: How do you design a Finite State Machine Circuit Breaker Engine in pure Java?
* **Interviewer Evaluation**: Tests finite state machine (FSM) implementation, thread-safe state transitions, sliding window failure rate calculation, and fast-fail fallback execution.
* **Standout Technical Answer**:
  In distributed systems, calling an unhealthy dependency locks worker threads until timeout.
  **The 3-State FSM Engine**:
  1. **CLOSED**: All requests pass through. A ring buffer tracks the last $N$ execution outcomes (`SUCCESS` or `FAILURE`).
  2. If the failure rate in the ring buffer exceeds threshold (e.g. $\ge 50\%$), atomically transition state to **OPEN** via CAS: `stateRef.compareAndSet(State.CLOSED, State.OPEN)`. Record `openTimestamp = System.currentTimeMillis()`.
  3. **OPEN**: All requests fail-fast immediately, executing fallback logic in $<0.05\text{ms}$ with zero network delay.
  4. **HALF-OPEN**: When `now - openTimestamp >= cooldownMillis`, the next incoming request transitions state to **HALF-OPEN**. A probe trial of $K$ requests is permitted. If all probe calls succeed, transition to **CLOSED**; if any probe call fails, trip back to **OPEN** and reset cooldown timer.
* **Follow-Up Trap**: *"How do you prevent a thundering herd of 1,000 threads from entering the HALF-OPEN state simultaneously?"*
* **Winning Answer**: Use an `AtomicInteger halfOpenPermits = new AtomicInteger(10)`. In the HALF-OPEN state, only threads that successfully decrement `halfOpenPermits.decrementAndGet() >= 0` are allowed to probe downstream; all other concurrent threads fail-fast directly to fallback.
* **Production Sample Code**:
```java
package com.vortex.lld.resilience;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public final class ProductionCircuitBreaker {
    public enum State { CLOSED, OPEN, HALF_OPEN }

    private final AtomicReference<State> state = new AtomicReference<>(State.CLOSED);
    private final AtomicInteger failureCount = new AtomicInteger(0);
    private final int threshold = 5;
    private final long cooldownMs = 10_000;
    private volatile long lastTrippedTime = 0;

    public <T> T execute(Supplier<T> action, Supplier<T> fallback) {
        checkCooldown();
        State currentState = state.get();

        if (currentState == State.OPEN) {
            return fallback.get(); // Fail-fast!
        }

        try {
            T result = action.get();
            onSuccess();
            return result;
        } catch (Exception ex) {
            onError();
            return fallback.get();
        }
    }

    private void onSuccess() {
        failureCount.set(0);
        if (state.get() == State.HALF_OPEN) {
            state.compareAndSet(State.HALF_OPEN, State.CLOSED);
        }
    }

    private void onError() {
        int failures = failureCount.incrementAndGet();
        if (failures >= threshold) {
            if (state.compareAndSet(State.CLOSED, State.OPEN) || state.compareAndSet(State.HALF_OPEN, State.OPEN)) {
                lastTrippedTime = System.currentTimeMillis();
            }
        }
    }

    private void checkCooldown() {
        if (state.get() == State.OPEN && (System.currentTimeMillis() - lastTrippedTime > cooldownMs)) {
            state.compareAndSet(State.OPEN, State.HALF_OPEN);
        }
    }

    public State getState() { return state.get(); }
}
```
* **Sample Input & Output**:
  - *Input*: Downstream throws 5 exceptions; 6th call executed.
  - *Output*: Circuit trips to OPEN; 6th call executes fallback in $<1\mu\text{s}$.

---

### ❓ Scenario 187: How do you model a Multi-Channel Notification Fan-Out System with User Preferences and Priority Queues?
* **Interviewer Evaluation**: Tests Strategy Pattern (Channels), Composite Pattern (Multi-channel dispatch), Priority Queue (Urgent vs Marketing), and User Preference filters.
* **Standout Technical Answer**:
  In an enterprise notification hub (Uber/Netflix), a single event (e.g. "Ride Arrived") must notify the user via multiple channels according to their personal notification preferences.
  **Architecture Components**:
  1. **Notification Entity**: `record Notification(String userId, Priority priority, String message, Instant createdAt)`.
  2. **NotificationChannel Strategy**: `boolean send(String userId, String payload)`. Implementations: `SmsChannel`, `PushNotificationChannel`, `EmailChannel`.
  3. **User Preference Repository**: Maps `(userId, NotificationType)` to enabled channels (e.g. User enables Push & SMS for Security, but only Email for Marketing).
  4. **Priority Queueing**: Critical security OTPs and ride updates enter a High-Priority Lane (`Priority.CRITICAL`), while weekly promotional digests enter a Low-Priority Lane (`Priority.LOW`) to prevent starvation of transactional messages.
* **Follow-Up Trap**: *"How do you handle per-user rate limiting to prevent spamming a user with 50 SMS messages in 1 minute during an outage cascade?"*
* **Winning Answer**: Combine each user's channel dispatcher with a **Per-User Token Bucket Rate Limiter**: if SMS notifications for `userId` exceed $3/\text{minute}$, automatically suppress duplicate messages or fallback to cheap Silent Push notifications.
* **Production Sample Code**:
```java
package com.vortex.lld.notification;

import java.util.*;

public enum NotificationPriority { CRITICAL, STANDARD, MARKETING }
public enum ChannelType { SMS, EMAIL, IN_APP_PUSH }

public interface ChannelSender {
    ChannelType getChannelType();
    void send(String recipient, String message);
}

public final class NotificationDispatcher {
    private final Map<ChannelType, ChannelSender> channelSenders = new HashMap<>();

    public NotificationDispatcher(List<ChannelSender> senders) {
        senders.forEach(s -> channelSenders.put(s.getChannelType(), s));
    }

    public void dispatch(String userContact, String message, Set<ChannelType> userPreferredChannels) {
        for (ChannelType channel : userPreferredChannels) {
            ChannelSender sender = channelSenders.get(channel);
            if (sender != null) {
                sender.send(userContact, message);
            }
        }
    }
}
```
* **Sample Input & Output**:
  - *Input*: Critical security alert with user preference `Set.of(SMS, IN_APP_PUSH)`.
  - *Output*: Both SMS and Push notifications dispatched simultaneously; Email skipped according to user preference.

---

### ❓ Scenario 188: How do you design an In-Memory Two-Lock Bounded Queue (Michael-Scott / Doug Lea Algorithm)?
* **Interviewer Evaluation**: Tests advanced lock decoupling, separate Head-Lock and Tail-Lock, `LinkedBlockingQueue` internals, and non-blocking head/tail concurrent access.
* **Standout Technical Answer**:
  In a standard synchronized bounded queue (like `ArrayBlockingQueue`), a single `ReentrantLock` guards both `put()` and `take()`. Producers and consumers continuously contend on the exact same lock!
  **The Two-Lock Concurrent Queue Breakthrough**:
  1. Decouple enqueue and dequeue using **Two Independent Locks**:
     - `ReentrantLock putLock`: Acquired strictly by producers inserting at the tail.
     - `ReentrantLock takeLock`: Acquired strictly by consumers polling from the head.
  2. Backed by a singly-linked list with a **Dummy Sentinel Node**: `head` and `tail` never point to the same null reference.
  3. Producers and consumers execute in parallel on different CPU cores with **zero mutual lock contention**, doubling throughput on multi-core hardware!
  4. Track size via an `AtomicInteger count` to coordinate queue full/empty conditions across the two locks.
* **Follow-Up Trap**: *"How does the Two-Lock Queue coordinate signaling when transitioning between empty and non-empty?"*
* **Winning Answer**: **Cascading Signal Pattern**: When `put()` increments count from 0 to 1, it acquires `takeLock` to signal the `notEmpty` condition. If queue remains non-empty, future `take()` operations signal subsequent waiting consumers themselves, avoiding cross-lock acquisitions on every call.
* **Production Sample Code**:
```java
package com.vortex.lld.concurrency;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public final class TwoLockConcurrentQueue<E> {
    private static class Node<E> {
        E item;
        Node<E> next;
        Node(E x) { item = x; }
    }

    private final int capacity;
    private final AtomicInteger count = new AtomicInteger(0);

    private final Node<E> headSentinel = new Node<>(null);
    private Node<E> tail;

    private final ReentrantLock takeLock = new ReentrantLock();
    private final Condition notEmpty = takeLock.newCondition();

    private final ReentrantLock putLock = new ReentrantLock();
    private final Condition notFull = putLock.newCondition();

    public TwoLockConcurrentQueue(int capacity) {
        this.capacity = capacity;
        this.tail = headSentinel;
    }

    public void put(E item) throws InterruptedException {
        Node<E> node = new Node<>(java.util.Objects.requireNonNull(item));
        int c = -1;
        putLock.lockInterruptibly();
        try {
            while (count.get() == capacity) {
                notFull.await(); // Wait until space is freed
            }
            tail.next = node;
            tail = node;
            c = count.getAndIncrement();
            if (c + 1 < capacity) {
                notFull.signal(); // Signal sibling producers
            }
        } finally {
            putLock.unlock();
        }
        if (c == 0) {
            signalNotEmpty(); // Transitioned from 0 to 1: wake consumer
        }
    }

    public E take() throws InterruptedException {
        E item;
        int c = -1;
        takeLock.lockInterruptibly();
        try {
            while (count.get() == 0) {
                notEmpty.await(); // Wait until item is added
            }
            Node<E> first = headSentinel.next;
            headSentinel.next = first.next;
            item = first.item;
            first.item = null; // Clean GC
            if (headSentinel.next == null) tail = headSentinel;
            c = count.getAndDecrement();
            if (c > 1) {
                notEmpty.signal(); // Signal sibling consumers
            }
        } finally {
            takeLock.unlock();
        }
        if (c == capacity) {
            signalNotFull(); // Transitioned from full to space available: wake producer
        }
        return item;
    }

    private void signalNotEmpty() {
        takeLock.lock();
        try { notEmpty.signal(); } finally { takeLock.unlock(); }
    }

    private void signalNotFull() {
        putLock.lock();
        try { notFull.signal(); } finally { putLock.unlock(); }
    }
}
```
* **Sample Input & Output**:
  - *Input*: 8 producer threads and 8 consumer threads concurrently pushing and pulling 100,000 items.
  - *Output*: Zero deadlocks; linear scalability with separate put and take lock queues.

---



## 📊 Production LLD Decision Matrix & 25-Point Machine Coding Checklist

### Canonical LLD Component Decision Matrix

| Problem Domain | Recommended Architecture | Primary Concurrency Mechanism | Algorithmic Complexity |
| :--- | :--- | :--- | :--- |
| **Parking Lot Spot Sizing** | Enum SpotType + Polymorphic canFit() | `AtomicReference<Ticket>` CAS per Spot | $O(1)$ allocation |
| **High-Precision Rate Limiting** | Sliding Window Counter (Interpolated) | AtomicLong per Window Key | $O(1)$ time, $O(1)$ space |
| **Debt Simplification** | Min-Cash-Flow Heap Balancing | Thread-confined computation | $O(N \log N)$ transactions |
| **Elevator Car Scheduling** | LOOK Algorithm with Twin TreeSets | Synchronized State Engine / ReentrantLock | $O(\log S)$ per stop |
| **In-Memory Cache (LRU)** | Doubly-Linked-List + HashMap | ReentrantLock / Lock Striping | $O(1)$ get/put/evict |
| **In-Memory Cache (LFU)** | Frequency Map + Doubly-Linked-Lists | ReentrantLock | $O(1)$ get/put/evict |
| **Distributed Unique IDs** | Twitter Snowflake (64-bit) | Synchronized / Atomic Bitwise Shifts | $O(1)$ ($4\text{M IDs/sec}$) |
| **Durable Key-Value Store** | WAL Log + MemTable ConcurrentSkipList | Append-only FileChannel fsync | $O(\log N)$ writes |
| **Task Scheduler** | DelayQueue + Worker Thread Pool | Condition awaitNanos in DelayQueue | $O(\log N)$ priority queue |

---

### 25-Point Machine Coding Production Readiness Checklist

1. [ ] **Encapsulation**: Domain entity fields must be `private final`; eliminate all public setters.
2. [ ] **Currency Invariants**: Never use `double` or `float` for monetary amounts; use `long amountCents` or `BigDecimal`.
3. [ ] **Atomic Counter Safety**: Replace `count++` with `AtomicInteger` or CAS loops in multi-threaded controllers.
4. [ ] **Lock Ordering**: Enforce global deterministic resource ordering (`compareTo`) on multi-resource locks to prevent deadlocks.
5. [ ] **Dummy Sentinels**: Always use Head and Tail dummy sentinel nodes in Doubly-Linked Lists to eliminate null checks.
6. [ ] **LRU/LFU Invariants**: Verify that every `get()` updates node position or frequency; test eviction when `size == capacity`.
7. [ ] **NTP Clock Drift Defense**: Ensure Snowflake ID generators catch `currentTime < lastTime` and spin-wait or reject.
8. [ ] **Bitwise Mask Safety**: Verify Snowflake bitmask calculations (`~(-1L << BITS)`) and avoid signed negative shifts.
9. [ ] **Lazy Token Refill**: Eliminate background timer threads in Rate Limiters; calculate refills on-demand using elapsed time.
10. [ ] **Boundary Burst Defense**: Avoid naive Fixed Window Rate Limiters; use Sliding Window Counter with weighted decay.
11. [ ] **LOOK Direction Discipline**: Elevators must service all calls in the current direction until boundary before reversing.
12. [ ] **Elevator Starvation Guard**: Ensure hall calls waiting in the opposite direction are registered in secondary queues.
13. [ ] **Min-Cash-Flow Verification**: Confirm that debt simplification net balances sum to exactly zero before heap processing.
14. [ ] **Graph Cycle Cancellation**: Verify that circular debts ($A \to B \to C \to A$) reduce to zero transfers without infinite loops.
15. [ ] **WAL Append Durability**: Call `fileChannel.force(false)` to force OS page cache to physical disk during WAL writes.
16. [ ] **Tombstone Semantics**: Mark deletions in LSM MemTables with tombstones; do not remove keys directly from disk.
17. [ ] **DelayQueue Precision**: Verify that `ScheduledTask` implements `Delayed` with nanosecond precision math.
18. [ ] **Thread Pool Graceful Shutdown**: Implement `executor.shutdown()` followed by `awaitTermination()` in all scheduler managers.
19. [ ] **Fencing Tokens**: Guard distributed worker leases with monotonic fencing tokens to block stale post-GC writes.
20. [ ] **Immutable Records**: Use Java 16+ `record` for DTOs, tickets, and transfer commands to ensure thread-safe sharing.
21. [ ] **Defensive Copying**: Return unmodifiable views (`Collections.unmodifiableList()`) or copies of internal collections.
22. [ ] **Exception Handling**: Never swallow `InterruptedException`; always restore thread interrupt status (`Thread.currentThread().interrupt()`).
23. [ ] **Overflow Guards**: Use `Math.addExact()` and `Math.multiplyExact()` when accumulating large integer sums.
24. [ ] **Bounded Memory**: Enforce strict capacity limits on all queues, caches, and rate-limiting history maps.
25. [ ] **Comprehensive Unit Tests**: Include concurrent multi-threaded stress tests verifying zero race conditions under load.

---

## 🚀 Machine Coding Quick-Reference Cheat Sheet

### Time & Space Complexity of Canonical LLD Primitives

| Primitive / Engine | Algorithm / Mechanism | Read / Get | Write / Put | Space Complexity |
| :--- | :--- | :---: | :---: | :---: |
| **LRU Cache** | Doubly-Linked List + HashMap | $O(1)$ | $O(1)$ | $O(C)$ where $C$ is capacity |
| **LFU Cache** | Frequency Map + Doubly-Linked Lists | $O(1)$ | $O(1)$ | $O(C)$ |
| **Token Bucket** | Lazy Refill Math | $O(1)$ | $O(1)$ | $O(1)$ per client |
| **Sliding Window Counter** | Weighted Window Estimation | $O(1)$ | $O(1)$ | $O(1)$ (2 counters) |
| **Sliding Window Log** | Timestamp Sorted Set (ZSET) | $O(\log N)$ | $O(\log N)$ | $O(N)$ timestamps |
| **Debt Simplification** | Min-Cash-Flow Heaps | — | $O(N \log N)$ | $O(N)$ balances |
| **LOOK Elevator** | Twin Balanced BSTs (TreeSet) | $O(1)$ current | $O(\log S)$ insert | $O(S)$ floor stops |
| **Twitter Snowflake** | 64-bit Bitwise Shift | $O(1)$ | $O(1)$ | $O(1)$ |
| **LSM MemTable** | ConcurrentSkipListMap | $O(\log N)$ | $O(\log N)$ | $O(M)$ buffer size |
| **Two-Lock Queue** | Decoupled Put/Take Mutexes | $O(1)$ | $O(1)$ | $O(C)$ bounded |

---

### Machine Coding Interview Strategy: 45-Minute Game Plan

1. **Minutes 0–5: Scope & Invariant Clarification**:
   - Ask clarifying questions: Is it single-threaded or multi-threaded?
   - What are the scale limits ($100\text{ req/sec}$ vs $100,000\text{ req/sec}$)?
   - Identify edge cases (negative values, duplicate keys, capacity overflow).
2. **Minutes 5–15: Core Domain & Interface Modeling**:
   - Write interfaces and enums first (`SpotType`, `Direction`, `PaymentStrategy`).
   - Define immutable value objects using Java 16+ `record`.
   - Keep classes cohesive; follow Single Responsibility and Open-Closed principles.
3. **Minutes 15–35: Algorithmic Implementation & Concurrency Protection**:
   - Implement the core state transitions and eviction algorithms ($O(1)$ where required).
   - Use `AtomicInteger`, `AtomicReference`, or `ReentrantLock` for thread safety.
   - Avoid synchronized methods across broad scopes; lock only critical mutation blocks.
4. **Minutes 35–45: Edge Cases, Main Verification, & Unit Testing**:
   - Write a complete `main()` driver demonstrating typical and edge workflows.
   - Add multi-threaded stress validation with `CountDownLatch` or `ExecutorService`.
   - Walk through complexity analysis and production failure modes with the interviewer.
