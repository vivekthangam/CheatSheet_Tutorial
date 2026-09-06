# ☕ Master Java Scenario-Based & Deep Internals Interview Guide 🚀

[![Java](https://img.shields.io/badge/Language-Java%2021%20LTS-orange.svg?style=for-the-badge&logo=openjdk)](https://www.oracle.com/java/)
[![Collections](https://img.shields.io/badge/Collections-Deep%20Internals-blue.svg?style=for-the-badge&logo=apache)](https://github.com/)
[![Concurrency](https://img.shields.io/badge/Concurrency-JMM%20%26%20Virtual%20Threads-red.svg?style=for-the-badge&logo=google)](https://github.com/)
[![Design Patterns](https://img.shields.io/badge/Patterns-Singleton%20Masterclass-purple.svg?style=for-the-badge&logo=spring)](https://github.com/)
[![JVM & GC](https://img.shields.io/badge/JVM-ZGC%20%26%20Memory%20Tuning-green.svg?style=for-the-badge&logo=oracle)](https://github.com/)

---

```
==================================================================================================
      ██╗ █████╗ ██╗   ██╗ █████╗     ███╗   ███╗ █████╗ ███████╗████████╗███████╗██████╗ 
      ██║██╔══██╗██║   ██║██╔══██╗    ████╗ ████║██╔══██╗██╔════╝╚══██╔══╝██╔════╝██╔══██╗
      ██║███████║██║   ██║███████║    ██╔████╔██║███████║███████╗   ██║   █████╗  ██████╔╝
 ██   ██║██╔══██║╚██╗ ██╔╝██╔══██║    ██║╚██╔╝██║██╔══██║╚════██║   ██║   ██╔══╝  ██╔══██╗
 ╚█████╔╝██║  ██║ ╚████╔╝ ██║  ██║    ██║ ╚═╝ ██║██║  ██║███████║   ██║   ███████╗██║  ██║
  ╚════╝ ╚═╝  ╚═╝  ╚═══╝  ╚═╝  ╚═╝    ╚═╝     ╚═╝╚═╝  ╚═╝╚══════╝   ╚═╝   ╚══════╝╚═╝  ╚═╝
==================================================================================================
           200+ SCENARIO-BASED INTERVIEW QUESTIONS, DEEP INTERNALS & CODING CHALLENGES
==================================================================================================
```

---

## 📑 Master Table of Contents

- [🧠 Module 1: Java Collections Framework Deep Internals & Mechanics (Q1 – Q35)](#-module-1-java-collections-framework-deep-internals--mechanics)
- [⚡ Module 2: Java Multithreading, Concurrency & Java Memory Model (JMM) (Q36 – Q80)](#-module-2-java-multithreading-concurrency--java-memory-model-jmm)
- [🛡️ Module 3: Design Patterns & The Singleton Pattern Masterclass (Q81 – Q110)](#-module-3-design-patterns--the-singleton-pattern-masterclass)
- [🔬 Module 4: JVM Architecture, Memory Management & Garbage Collection (Q111 – Q135)](#-module-4-jvm-architecture-memory-management--garbage-collection)
- [🚀 Module 5: Modern Java (8–21), Streams, Generics & SOLID Principles (Q136 – Q155)](#-module-5-modern-java-821-streams-generics--solid-principles)
- [🏭 Module 6: Real-World Scenario-Based System & Production Debugging (Q156 – Q175)](#-module-6-real-world-scenario-based-system--production-debugging)
- [🧬 Module 8: Java Generics Deep Internals, Advanced Design & Interview Q&As (Q176 – Q185)](#-module-8-java-generics-deep-internals-advanced-design--interview-qas)
- [🔍 Module 9: Java Reflection Deep Dive & Spring Framework Architecture (Q186 – Q190)](#-module-9-java-reflection-deep-dive--spring-framework-architecture)
- [🎭 Module 10: Aspect-Oriented Programming (AOP) Masterclass & Spring AOP (Q191 – Q195)](#-module-10-aspect-oriented-programming-aop-masterclass--spring-aop)
- [🔒 Module 11: Thread-Safe Class Design & Concurrency Strategy Matrix (Q196 – Q200)](#-module-11-thread-safe-class-design--concurrency-strategy-matrix)
- [💻 Module 7: Flagship Java Coding Interview Challenges & Multi-Threaded Implementations (Challenges 1 – 15)](#-module-7-flagship-java-coding-interview-challenges--multi-threaded-implementations)
- [🧵 Dedicated Deep-Dive Concurrency Guide: `java_thread.md`](java_thread.md)

---

# 🧠 Module 1: Java Collections Framework Deep Internals & Mechanics

---

### Q1: How does Java `HashMap` work internally under the hood in JDK 8+? Explain bucket arrays, hashing, and treeification.
* **Internal Mechanics & Problem Context**:
  * In Java, a `HashMap` is an array-backed hash table implementing the `Map` interface using **Separate Chaining**.
  * The internal storage is a `Node<K,V>[] table` array initialized with a default capacity of $16$ (`1 << 4`) and a load factor of $0.75$.
  * When a key-value pair `(K, V)` is inserted via `put(K, V)`:
    1. The key's `hashCode()` is calculated and passed through a **Hash Perturbation Function**: `h ^ (h >>> 16)`. This spreads high-order bits into lower bits so that small tables take advantage of all bits during index masking.
    2. The exact bucket index is calculated using fast bitwise masking: `index = (n - 1) & hash` (where $n$ is table capacity, guaranteed to be a power of 2).
    3. If the bucket is empty, a new `Node<K,V>` is placed directly.
    4. If a collision occurs, the map traverses the existing chain:
       * If an identical key exists (`key.equals(existingKey)` or `key == existingKey`), the old value is overwritten.
       * If not, the new node is appended to the tail of the singly-linked list (tail-insertion in JDK 8+, unlike head-insertion in JDK 7 which suffered from infinite loop race conditions during resizing).
    5. **Treeification (`TREEIFY_THRESHOLD = 8`)**: If the number of collisions in a single bucket reaches $8$ AND total table capacity is at least $64$ (`MIN_TREEIFY_CAPACITY`), the bucket's linked list is converted into a balanced **Red-Black Tree (`TreeNode<K,V>`)**, dropping search time from $O(N)$ worst-case to $O(\log N)$.
    6. **Untreeification (`UNTREEIFY_THRESHOLD = 6`)**: If items are removed during deletion or rehashing and node count drops to $6$, the Red-Black Tree is converted back into a lightweight singly-linked list to save memory.

---

### Q2: Why does `HashMap` use bitwise `(n - 1) & hash` instead of standard modulo `%`? Why must capacity always be a power of 2?
* **Low-Level Explanation & Proof**:
  * In computer architecture, hardware integer division (`%`) is an expensive CPU instruction requiring $15\text{--}30$ clock cycles, whereas bitwise AND (`&`) is a single-cycle instruction ($< 1$ nanosecond).
  * Mathematically, if and only if $n$ is an exact power of $2$ ($n = 2^k$), the bitwise representation of $(n - 1)$ consists of exactly $k$ consecutive $1$s:
    $$\text{Example: } n = 16 = 00010000_2 \implies (n - 1) = 15 = 00001111_2$$
  * When any 32-bit hash is ANDed with `00001111_2`, all bits higher than bit 3 are zeroed out, retaining only the lowest 4 bits, which is strictly equivalent to `hash % 16` in the range $[0, 15]$.
  * If $n$ is **not** a power of 2 (e.g., $n = 15 \implies n-1 = 14 = 00001110_2$), the lowest bit is $0$. Any `(14 & hash)` operation will always produce an even index, meaning odd bucket indices (1, 3, 5, 7, 9, 11, 13) would NEVER receive elements, causing $50\%$ bucket waste and catastrophic collision spikes!

---

### Q3: Why is `HashMap`'s default load factor chosen as $0.75$, and why is `TREEIFY_THRESHOLD` set to $8$?
* **Mathematical & Statistical Proof**:
  * **Load Factor $\alpha = 0.75$ Trade-off**:
    * If $\alpha$ is too high (e.g., $0.95$), memory is tightly utilized, but hash collisions spike exponentially, leading to long search traversals and degraded lookup performance.
    * If $\alpha$ is too low (e.g., $0.50$), collisions are extremely rare, but half the allocated heap space remains completely empty, causing severe memory bloat.
    * Empirical benchmarks by Sun/Oracle engineers proved $0.75$ provides the golden ratio between time complexity (average $< 1.5$ memory probes per lookup) and space efficiency ($25\%$ empty headroom).
  * **Poisson Distribution Proof of `TREEIFY_THRESHOLD = 8`**:
    * Under uniform random hashing with $\lambda = 0.5$ (average nodes per bucket at $0.75$ load factor), the probability $P(k)$ of a bucket having $k$ colliding nodes follows the Poisson distribution:
      $$P(k) = \frac{e^{-\lambda} \lambda^k}{k!}$$
    * For $k = 8$, the probability is $P(8) = \frac{e^{-0.5} (0.5)^8}{8!} \approx 0.00000006$ (less than **1 in 10 million**).
    * Therefore, treeification almost never triggers in clean applications. It exists strictly as a security defense mechanism against **Hash Collision DoS Attacks** where malicious clients craft billions of strings hashing to the same bucket to force $O(N^2)$ CPU starvation.

---

### Q4: How does `TreeMap` work internally, and how does it differ from `HashMap`?
* **Internal Mechanics & Comparison**:
  * `TreeMap` is a `NavigableMap` implementation backed by a **Red-Black Tree (Self-Balancing Binary Search Tree)**.
  * Unlike `HashMap` which uses `hashCode()` and `equals()` for $O(1)$ average hash-bucket lookups, `TreeMap` compares keys using natural ordering (`Comparable<T>`) or an explicit custom `Comparator<T>`, guaranteeing strict **$O(\log N)$** time for `get()`, `put()`, `remove()`, and `containsKey()`.
  * **Key Differences**:
    1. **Key Ordering**: `HashMap` offers no ordering guarantees. `TreeMap` guarantees strictly sorted ascending key traversal (in-order tree traversal).
    2. **Null Keys**: `HashMap` permits one `null` key (stored at index 0). `TreeMap` throws `NullPointerException` upon inserting a `null` key if using natural ordering because `key.compareTo(null)` cannot be evaluated.
    3. **Range Queries & Navigable Operations**: `TreeMap` provides $O(\log N)$ range lookups like `subMap(from, to)`, `headMap(to)`, `tailMap(from)`, `floorKey(k)` (greatest key $\le k$), and `ceilingKey(k)` (smallest key $\ge k$), which are impossible in a flat `HashMap`.

```
====================== RED-BLACK TREE BALANCING IN TREEMAP ======================
Invariants:
1. Every node is either RED or BLACK.
2. Root is always BLACK.
3. No two consecutive RED nodes (Red parent cannot have a Red child).
4. Every path from root to leaf has the exact same number of BLACK nodes.
5. Tree height is strictly bounded by 2 * log2(N + 1).
=================================================================================
```

---

### Q5: How does `LinkedHashMap` maintain insertion order and access order (LRU)?
* **Internal Architecture & Mechanics**:
  * `LinkedHashMap` extends `HashMap<K, V>` and adds a globally maintained **bidirectional doubly-linked list** traversing across all bucket entries.
  * Each bucket node is an instance of `LinkedHashMap.Entry<K,V>` which extends `HashMap.Node<K,V>` with two extra pointer fields: `Entry<K,V> before, after`.
  * **Two Ordering Modes**:
    1. **Insertion-Order (`accessOrder = false`, Default)**: When a new key is inserted, it is appended to the tail of the global doubly-linked list (`after = tail`). Iterating over the map yields keys in the exact order they were inserted.
    2. **Access-Order (`accessOrder = true`, LRU Mode)**: When `get(key)` or `put(key, val)` is invoked on an existing key, the internal callback `afterNodeAccess(node)` unlinks the accessed node from its current position in the doubly-linked list and splices it to the very tail.
  * **LRU Cache Hook (`removeEldestEntry`)**:
    * By overriding `protected boolean removeEldestEntry(Map.Entry<K,V> eldest)`, developers can implement a production-grade $O(1)$ LRU cache in 5 lines of code:
    ```java
    public class SimpleLRUCache<K, V> extends LinkedHashMap<K, V> {
        private final int maxCapacity;
        public SimpleLRUCache(int maxCapacity) {
            super(maxCapacity, 0.75f, true); // true enables access-order
            this.maxCapacity = maxCapacity;
        }
        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return size() > maxCapacity; // Evicts head (least recently used)
        }
    }
    ```

---

### Q6: How does `ConcurrentHashMap` achieve thread-safety in JDK 8+ vs JDK 7?
* **Architecture Evolution & Deep Dive**:
  * **JDK 7 (Segmented Locking)**:
    * Used a fixed array of `Segment<K,V>[]` (default 16 segments), where each `Segment` extended `ReentrantLock`.
    * Up to 16 threads could write concurrently to different segments. However, resizing a segment was complex, and global operations like `size()` required acquiring all 16 locks simultaneously.
  * **JDK 8+ (Lock-Free CAS + Node-Level Fine-Grained `synchronized`)**:
    * Removed `Segment` locks completely. Uses a flat `Node<K,V>[] table` array identical to `HashMap`.
    * **Reads (`get`)**: Completely **lock-free**! Table references and node `val`/`next` fields are declared `volatile`. Reads use atomic memory loads (`Unsafe.getObjectVolatile`), ensuring immediate cross-CPU visibility without blocking.
    * **Writes (`put`)**:
      1. If the target bucket is empty (`null`), `ConcurrentHashMap` uses an atomic **Compare-And-Swap (`CAS`)** operation to insert the node without taking any lock.
      2. If the target bucket already contains a node (collision), it acquires a fine-grained `synchronized` lock **only on the first node (head) of that specific bucket chain**. Other threads writing to different buckets proceed concurrently without contention!
      3. If a resize is in progress, the bucket contains a `ForwardingNode` (`hash = -1`), and the calling thread immediately cooperates to help transfer buckets (`helpTransfer()`).
    * **High-Throughput Concurrent Sizing (`size()`)**:
      * Instead of a single atomic counter (which causes high CAS cache-line bouncing under thousands of threads), `ConcurrentHashMap` uses a distributed `CounterCell[]` array (similar to `LongAdder`). Threads update distinct array cells, and `size()` sums the base count and all active cells.

---

### Q7: What is the internal difference between `ArrayList` and `LinkedList` in Java, and why is `ArrayList` almost always faster in practice?
* **Hardware & Algorithmic Reality**:
  * **Data Structure**: `ArrayList` is backed by a contiguous dynamic array (`Object[] elementData`). `LinkedList` is a doubly-linked list where every element is wrapped in a distinct heap-allocated `Node` object (`item, prev, next`).
  * **Memory Overhead**:
    * `ArrayList`: $4$ bytes per reference in compressed OOPs.
    * `LinkedList`: $24\text{--}32$ bytes per node (16-byte object header + 8-byte item reference + 8-byte prev pointer + 8-byte next pointer) $\to$ $600\%$ to $800\%$ higher memory consumption!
  * **The CPU L1/L2 Cache Locality Advantage**:
    * When `ArrayList` iterates from index `0` to `N`, the CPU loads a contiguous **64-byte L1 Cache Line** from RAM, fetching 8 to 16 references simultaneously in a single clock cycle (~1 nanosecond).
    * In `LinkedList`, each `Node` is allocated at random, disparate addresses across the JVM Heap. Iterating through the chain requires dereferencing pointers, causing severe **CPU L1/L2 Cache Misses** and forcing the processor to stall for 50–100 nanoseconds on every single node traversal!
  * **Verdict**: Even for insertions in the middle of a list, `ArrayList`'s fast native memory block copy (`System.arraycopy`) often outperforms `LinkedList`'s pointer-chasing traversal to index $K$ for lists up to 100,000 elements!

---

### Q8: How does `CopyOnWriteArrayList` work, and what are its performance trade-offs?
* **Mechanics & Production Scenarios**:
  * `CopyOnWriteArrayList` is a thread-safe `List` implementation designed for **read-heavy, write-rare** scenarios (e.g., event listener lists, observer registries, authorization rule caches).
  * **Reads**: Completely lock-free and blazing fast! Returns a direct pointer to the current immutable underlying array (`volatile Object[] array`). Iterators operate on an immutable snapshot and **never throw `ConcurrentModificationException`**.
  * **Writes (`add`, `set`, `remove`)**:
    * Every mutating operation acquires a `ReentrantLock`, allocates an entirely new array of size $N+1$, copies all existing elements over via `Arrays.copyOf`, writes the new item, and atomically flips the underlying `volatile` array reference to point to the new array.
  * **Trade-Offs & Pitfalls**:
    * **Write Penalty**: For lists of size $100,000$, calling `add()` in a loop creates $100,000$ large heap arrays, triggering massive garbage collection pressure and CPU slowdowns.
    * **Memory Inconsistency (Eventual Consistency)**: A thread reading while a write is occurring will continue reading stale data from its old snapshot until the volatile reference flip completes.

---

### Q9: How does `HashSet` work internally, and why is inserting an existing key an $O(1)$ operation?
* **Internal Architecture**:
  * In the JDK, `HashSet<E>` is a lightweight wrapper that delegates directly to an internal `HashMap<E, Object>` instance:
    ```java
    public class HashSet<E> implements Set<E> {
        private transient HashMap<E, Object> map;
        private static final Object PRESENT = new Object(); // Dummy value

        public HashSet() { map = new HashMap<>(); }
        public boolean add(E e) { return map.put(e, PRESENT) == null; }
        public boolean contains(Object o) { return map.containsKey(o); }
        public boolean remove(Object o) { return map.remove(o) == PRESENT; }
    }
    ```
  * Every element added to the set becomes the **Key** of the internal `HashMap`.
  * A single static dummy `Object PRESENT` is shared across the entire JVM as the map's value.
  * When `add(e)` is called, `map.put(e, PRESENT)` returns `null` if the key was absent (successful insert) or the previous `PRESENT` object if the key already existed, yielding $O(1)$ constant time uniqueness checks.

---

### Q10: What is the difference between `Fail-Fast` and `Fail-Safe` (Weakly Consistent) iterators?
* **Mechanics & Root Cause**:
  * **Fail-Fast (`ArrayList`, `HashMap`, `HashSet`, `LinkedList`)**:
    * Maintained via an internal integer modification counter: `modCount`.
    * Every mutating method (`add`, `remove`, `clear`) increments `modCount++`.
    * When an `Iterator` is created, it records `expectedModCount = modCount`.
    * On every `iterator.next()` call, it checks: `if (modCount != expectedModCount) throw new ConcurrentModificationException()`.
    * **Why**: To quickly surface non-deterministic concurrent data corruption bugs in development rather than allowing silent data loss.
  * **Fail-Safe / Weakly Consistent (`ConcurrentHashMap`, `CopyOnWriteArrayList`, `ConcurrentLinkedQueue`)**:
    * Does not rely on `modCount`.
    * `CopyOnWriteArrayList` iterates over an immutable snapshot array captured at iterator construction time.
    * `ConcurrentHashMap` uses a weakly consistent iterator that traverses live volatile bucket nodes. It reflects changes made after iterator creation without throwing exceptions, but does not guarantee reflecting real-time concurrent mutations.

---

### Q11: How does `ArrayDeque` work internally, and why is it recommended over `Stack` and `LinkedList` for LIFO/FIFO operations?
* **Circular Buffer Architecture**:
  * `ArrayDeque` is backed by a resizable contiguous circular array (`Object[] elements`) with two integer head/tail pointers: `int head, tail`.
  * **Bitwise Wraparound**: Array capacity is always a power of 2. Pointer wraparound is achieved in 1 CPU cycle using bitwise masking: `head = (head - 1) & (elements.length - 1)` and `tail = (tail + 1) & (elements.length - 1)`.
  * **Why `ArrayDeque` Beats `Stack`**:
    * `java.util.Stack` extends `java.util.Vector`, where every method (`push`, `pop`, `peek`) is `synchronized`, introducing unnecessary lock acquisition overhead in single-threaded environments.
  * **Why `ArrayDeque` Beats `LinkedList` for Queues/Stacks**:
    * `ArrayDeque` has zero node allocations and perfect contiguous L1 cache locality, whereas `LinkedList` allocates a new 32-byte `Node` object on every single `offer()` / `push()`, generating massive GC churn.

---

### Q12: How does `PriorityQueue` work internally in Java? Is it thread-safe?
* **Internal Heap Mechanics**:
  * `PriorityQueue` is an unbounded priority queue backed by a flat contiguous array (`Object[] queue`) representing a **Complete Binary Min-Heap**.
  * Elements are ordered by natural ordering (`Comparable`) or a custom `Comparator`.
  * **Array Indexing Formulas**:
    * For node at index $i$: $\text{Parent} = \lfloor (i - 1) / 2 \rfloor$, $\text{Left Child} = 2i + 1$, $\text{Right Child} = 2i + 2$.
  * **Operations**:
    * `offer(E e)` / `add(E e)`: Appends element at `queue[size++]` and performs **Sift-Up (`siftUp`)** in $O(\log N)$ time to restore min-heap invariant.
    * `poll()`: Extracts root `queue[0]`, moves the last element `queue[--size]` to index 0, and performs **Sift-Down (`siftDown`)** in $O(\log N)$ time.
    * `peek()`: Returns root `queue[0]` in strict **$O(1)$** time.
  * **Thread Safety**: `PriorityQueue` is NOT thread-safe. For concurrent environments, use `PriorityBlockingQueue` which wraps heap operations in a `ReentrantLock`.

---

### Q13: What is `IdentityHashMap`, and how does it differ from standard `HashMap`?
* **Comparison & Use Cases**:
  * Standard `HashMap` compares keys using `k1.equals(k2)` and `k1.hashCode()`.
  * `IdentityHashMap` compares keys strictly using **Reference Equality (`k1 == k2`)** and `System.identityHashCode(k1)`.
  * **Internal Storage**: It does not use `Node` objects or linked lists. It uses a single flat array `Object[] table` where keys and values alternate contiguously: `[key0, val0, key1, val1, key2, val2, ...]`, resolving collisions via linear probing.
  * **Use Cases**: Object graph serialization frameworks (like Jackson, Kryo, JVM Serialization) to detect circular object references without triggering expensive overridden `equals()` methods.

---

### Q14: How do `EnumMap` and `EnumSet` achieve near-instantaneous memory and CPU performance?
* **Bit-Vector & Array Optimizations**:
  * Because all enum constants are fixed at compile time and assigned continuous zero-based ordinal values (`enum.ordinal()`), `EnumMap` and `EnumSet` bypass hashing completely:
  * **`EnumMap<K, V>`**: Internally backed by a simple 1D array `Object[] vals` sized exactly to the number of enum constants. Lookups and insertions are direct array index access: `vals[key.ordinal()] = value`, executing in strict **$O(1)$ with zero hash collisions**.
  * **`EnumSet<E>`**:
    * For enums with $\le 64$ constants, it is implemented as `RegularEnumSet`, backed by a single primitive **64-bit `long elements` bit-vector**.
    * Operations like `add()`, `contains()`, `remove()` are single-cycle bitwise shifts: `elements |= (1L << e.ordinal())`.
    * Bulk set operations (`containsAll`, `retainAll`) execute in a single CPU bitwise AND/OR instruction (`elements & other.elements`), running hundreds of times faster than standard `HashSet`!

---

### Q15: What happens when two distinct objects return the exact same `hashCode()` in a `HashMap`?
* **Hash Collision Resolution Mechanics**:
  1. Both objects hash to the same bucket index via `(n - 1) & hash`.
  2. The `HashMap` enters the bucket and iterates through the existing chain.
  3. For each existing node, it checks: `if (node.hash == hash && (node.key == key || key.equals(node.key)))`.
  4. Since the objects have the same hash but are distinct (`equals()` returns `false`), the new object does NOT overwrite the existing node; instead, it is appended to the chain as a collision.
  5. If collisions in this bucket exceed 8, the bucket converts to a Red-Black Tree. If the keys implement `Comparable<K>`, they are sorted using `compareTo()`; otherwise, ties are broken using `System.identityHashCode(key)`.

---

### Q16: Why should mutable objects NEVER be used as keys in a `HashMap`?
* **Memory Leak & Disappearing Key Hazard**:
  * When a key-value pair is inserted, its bucket index is computed based on the key's current `hashCode()`.
  * If the key object is mutated later such that its internal fields change, its `hashCode()` value also changes.
  * When calling `map.get(key)` or `map.containsKey(key)` later, the `HashMap` computes the *new* hash code, which routes the lookup to an entirely different bucket!
  * **Result**: The map returns `null`, reporting the key is missing even though the key-value pair is still sitting inside the old bucket in memory, creating a permanent **Memory Leak** and silent data retrieval failure.

---

### Q17: What is the contract between `equals()` and `hashCode()` in Java? What happens if you override `equals()` but forget `hashCode()`?
* **The Invariant Contract**:
  1. **Consistent**: If `a.equals(b)` is `true`, then `a.hashCode()` **MUST** equal `b.hashCode()`.
  2. **Non-unique**: If `a.hashCode() == b.hashCode()`, `a.equals(b)` does *not* necessarily have to be true (hash collision).
* **Failure Scenario if `hashCode()` is omitted**:
  * Suppose class `User` overrides `equals(Object o)` to check `id`, but uses default `Object.hashCode()` (which generates a hash based on heap memory address).
  * `User u1 = new User(101); User u2 = new User(101);` (Here `u1.equals(u2)` is `true`).
  * `map.put(u1, "Admin");` (Stored in bucket $A$ based on `u1`'s memory address).
  * `map.get(u2)`: Computes `u2`'s memory address hash, routing to bucket $B$. Bucket $B$ is empty, returning `null`!
  * **Verdict**: The `HashMap` fails to retrieve stored data for logically identical objects, violating the fundamental Java Collection specifications.

---

### Q18: How does `Collections.synchronizedMap()` differ from `ConcurrentHashMap`?
* **Locking Strategy & Throughput**:
  * `Collections.synchronizedMap(map)` wraps an existing map and guards **every single method** (`get`, `put`, `remove`, `size`) with a coarse-grained `synchronized (mutex)` global lock.
  * Every thread must wait in line for the single global lock, bottlenecking throughput in high-concurrency systems. Furthermore, iterating over `synchronizedMap` requires manual external synchronization, otherwise it throws `ConcurrentModificationException`.
  * `ConcurrentHashMap` uses bucket-level fine-grained `synchronized` locks, lock-free volatile reads, CAS operations, and lock-free concurrent iterators, allowing hundreds of threads to read and write simultaneously with near-linear scalability.

---

### Q19: What is the internal difference between `Comparable` and `Comparator` in Java?
* **Comparison**:
  * **`Comparable<T>` (Natural Ordering)**:
    * Implemented inside the domain class itself: `public int compareTo(T o)`.
    * Modifies the class definition. Defines a single intrinsic default ordering (e.g., `String.compareTo()`, `Integer.compareTo()`).
  * **`Comparator<T>` (Custom / External Strategy)**:
    * Implemented as an external standalone class or lambda: `(o1, o2) -> o1.getAge() - o2.getAge()`.
    * Allows defining multiple flexible sorting criteria (e.g., sort by name, sort by price descending, sort by date) without altering the target domain class.

---

### Q20: How does `LinkedHashSet` work internally?
* **Internal Architecture**:
  * `LinkedHashSet<E>` extends `HashSet<E>` and implements `Set<E>`.
  * Internally, its constructor calls a package-private `HashSet` constructor:
    ```java
    HashSet(int initialCapacity, float loadFactor, boolean dummy) {
        map = new LinkedHashMap<>(initialCapacity, loadFactor);
    }
    ```
  * Instead of backing with a standard `HashMap`, it backs with a **`LinkedHashMap`**.
  * Elements are stored as keys in the `LinkedHashMap`, which chains them in a doubly-linked list, guaranteeing iteration order matches insertion order in $O(1)$ time.

---

### Q21: How does Java 8 `Map.computeIfAbsent()` work internally, and why is it preferred over `containsKey()` followed by `put()`?
* **Atomic Bucket Evaluation**:
  * `map.computeIfAbsent(key, mappingFunction)` evaluates the key and, if missing, computes the value and inserts it in a single atomic step.
  * In `ConcurrentHashMap`, `computeIfAbsent()` computes the value **under the lock of that specific bucket node**. This eliminates race conditions where two threads simultaneously observe `containsKey() == false` and both execute expensive database queries or object initializations.

---

### Q22: How does `BlockingQueue` implement thread coordination? Compare `ArrayBlockingQueue` vs `LinkedBlockingQueue`.
* **Lock & Condition Mechanics**:
  * `BlockingQueue` blocks the producer thread when the queue is full (`put()`) and blocks the consumer thread when the queue is empty (`take()`).
  * **`ArrayBlockingQueue`**:
    * Bounded circular array.
    * Uses a **single `ReentrantLock`** with two `Condition` variables (`notEmpty`, `notFull`).
    * Because producers and consumers share the same lock, producers and consumers contend with each other.
  * **`LinkedBlockingQueue`**:
    * Optionally bounded linked list of nodes.
    * Uses **two separate `ReentrantLock`s**: `takeLock` (for consumers) and `putLock` (for producers).
    * Producers and consumers operate completely in parallel without blocking each other, yielding significantly higher concurrency throughput!

---

### Q23: How does `SynchronousQueue` work in Java ThreadPools?
* **Zero-Capacity Hand-off**:
  * A `SynchronousQueue` is a blocking queue with **zero internal storage capacity**.
  * An `offer()` or `put()` operation blocks indefinitely until an active consumer thread invokes `poll()` or `take()` to receive the element directly in a point-to-point hand-off.
  * Used by `Executors.newCachedThreadPool()`. Incoming tasks immediately spawn new worker threads if all existing threads are busy, achieving zero queue buffering latency.

---

### Q24: What is `WeakHashMap`, and how does it prevent memory leaks in caching?
* **Garbage Collection Cooperative Maps**:
  * `WeakHashMap` stores keys wrapped in `java.lang.ref.WeakReference<K>`.
  * If a key object has no strong references remaining outside the `WeakHashMap`, the JVM Garbage Collector automatically reclaims the key during the next GC cycle and enqueues its weak reference into an internal `ReferenceQueue`.
  * On subsequent calls to `WeakHashMap.get()` or `put()`, the map polls the `ReferenceQueue` and automatically cleans up and unlinks the corresponding dead entries (`expungeStaleEntries()`), preventing memory leaks in metadata associations and temporary caches.

---

### Q25: How does `EnumSet.allOf()` create an instance without knowing the enum size beforehand?
* **Class Metadata Reflection**:
  * `EnumSet.allOf(Class<E> elementType)` inspects `elementType.getEnumConstants()`.
  * If the total number of enum constants is $\le 64$, it instantiates a `RegularEnumSet` (backed by a single 64-bit `long` mask with all valid bits set to `1`).
  * If the number of enum constants is $> 64$, it instantiates a `JumboEnumSet` (backed by a `long[]` array mask where each array index covers 64 enum constants).

---

### Q26: What is the time and space complexity of `Collections.sort()` in Java?
* **TimSort Algorithmic Details**:
  * Since Java 7, `Collections.sort()` and `Arrays.sort(Object[])` use **TimSort** (an adaptive, stable hybrid of MergeSort and InsertionSort).
  * **Best-Case Time Complexity**: $O(N)$ for already sorted or partially sorted arrays (identifies natural ascending/descending "runs").
  * **Average & Worst-Case Time Complexity**: $O(N \log N)$ comparisons.
  * **Space Complexity**: $O(N)$ auxiliary buffer memory for temporary run merges.
  * It guarantees stability (equal elements maintain their relative original input positions).

---

### Q27: How does `BitSet` work in Java, and when should you use it over `boolean[]`?
* **Memory Compactness & Hardware Bitwise Math**:
  * A `boolean[]` array in Java uses **1 full byte (8 bits)** per boolean entry due to JVM memory word alignment rules.
  * A `java.util.BitSet` packs booleans into an underlying `long[] words` array where each bit represents a boolean flag ($0 = \text{false}, 1 = \text{true}$).
  * **Space Savings**: Storing 1,000,000 booleans takes $1\text{ MB}$ in `boolean[]`, but only **$125\text{ KB}$** in `BitSet` ($8\times$ memory reduction!).
  * Allows blazing-fast hardware-accelerated set intersections and unions via `bitSetA.and(bitSetB)`.

---

### Q28: How does `ConcurrentSkipListMap` work, and why does it scale better than `ConcurrentHashMap` for sorted keys?
* **Probabilistic Multi-Level Indexing**:
  * `ConcurrentSkipListMap` implements `ConcurrentNavigableMap` using a multi-level **Skip List**.
  * Inserting into a balanced tree (`TreeMap`) in multi-threaded environments requires complex lock coupling and tree rotations that block large subtrees.
  * In a Skip List, nodes are linked in forward lists across levels. Inserting or deleting a node only alters local adjacent pointers via atomic **Compare-And-Swap (CAS)** operations, providing lock-free concurrent sorted indexing and $O(\log N)$ range scans without thread contention.

---

### Q29: What is the difference between `poll()` and `remove()` in Java Queues?
* **Exception vs Sentinel Return**:
  * Both methods retrieve and remove the head element of the queue.
  * **`remove()`**: If the queue is empty, it throws a `NoSuchElementException`.
  * **`poll()`**: If the queue is empty, it returns `null` safely without throwing an exception.
  * Similarly, for insertions: `add()` throws `IllegalStateException` on full bounded queues, while `offer()` returns `false`.

---

### Q30: How does `Collections.unmodifiableList()` differ from `List.of()` in Java 9+?
* **Deep Immuntability vs View Wrapper**:
  * **`Collections.unmodifiableList(existingList)`**: Creates an unmodifiable *view* wrapper around the backing list. If the underlying `existingList` is modified elsewhere, the "unmodifiable" view reflects those modifications!
  * **`List.of(e1, e2, ...)` (Java 9+)**: Creates a truly immutable, compact, standalone list object. It contains no wrapper overhead, rejects `null` values (`NullPointerException`), and cannot be modified under any circumstances.

---

### Q31: How does `TreeSet` enforce uniqueness, and why does a broken `compareTo()` corrupt the set?
* **Binary Tree Search Invariant**:
  * `TreeSet` delegates to `TreeMap`.
  * When inserting element $X$, it traverses the Red-Black Tree using `comparator.compare(X, current)`.
  * If `compare(X, current) == 0`, `TreeSet` considers $X$ to be an exact duplicate and **rejects the insertion**, *regardless of whether `X.equals(current)` returns false*!
  * If `compareTo()` is inconsistent with `equals()` (e.g., comparing only a subset of fields), two non-equal objects returning comparison $0$ will result in the second object being silently discarded.

---

### Q32: What is the internal difference between `Vector` and `ArrayList`?
* **Synchronization & Resizing Factor**:
  * `Vector` is a legacy synchronized class from JDK 1.0. Every method contains `synchronized`, introducing heavy lock overhead.
  * When resized, `Vector` doubles its capacity by $100\%$ ($2.0\times$), whereas `ArrayList` grows by $50\%$ ($1.5\times \implies \text{newCapacity} = \text{oldCapacity} + (\text{oldCapacity} >> 1)$), reducing memory waste.

---

### Q33: How does `DelayQueue` work in Java Concurrency?
* **Time-Scheduled Expiration**:
  * `DelayQueue<E extends Delayed>` is an unbounded blocking queue backed by a `PriorityQueue`.
  * Elements can only be extracted via `poll()` or `take()` when their delay has expired (`getDelay(TimeUnit) <= 0`).
  * The leader-follower thread scheduling model minimizes thread wakeups, making it ideal for scheduled task execution, session timeouts, and connection keep-alive monitors.

---

### Q34: What is the internal structure of `ConcurrentLinkedQueue`?
* **Lock-Free Michael-Scott Queue**:
  * `ConcurrentLinkedQueue` is an unbounded thread-safe FIFO queue based on the **Michael-Scott Lock-Free Queue Algorithm**.
  * Uses singly-linked `Node` objects with `volatile Node<E> item, next` fields.
  * Both `offer()` and `poll()` mutate head and tail pointers using non-blocking atomic **Compare-And-Swap (`CAS`)** loops, providing maximum concurrency throughput without any operating system thread suspension.

---

### Q35: Summary Table: Java Collections Master Big-O Matrix

| Collection Class | Backing Data Structure | `get()` | `add()` | `remove()` | Thread-Safe | Ordering Guarantee |
|---|---|---|---|---|---|---|
| **`ArrayList`** | Contiguous Dynamic Array | $O(1)$ | Amortized $O(1)$ | $O(N)$ | ❌ No | Insertion Order |
| **`LinkedList`** | Doubly-Linked List | $O(N)$ | $O(1)$ (Ends) | $O(1)$ (Known node) | ❌ No | Insertion Order |
| **`ArrayDeque`** | Contiguous Circular Array | $O(1)$ (Ends) | Amortized $O(1)$ | $O(1)$ (Ends) | ❌ No | Insertion Order |
| **`HashMap`** | Array + LinkedList / Red-Black Tree | $O(1)$ | $O(1)$ | $O(1)$ | ❌ No | None |
| **`LinkedHashMap`** | Hash Table + Doubly-Linked List | $O(1)$ | $O(1)$ | $O(1)$ | ❌ No | Insertion or Access Order |
| **`TreeMap`** | Red-Black Tree | $O(\log N)$ | $O(\log N)$ | $O(\log N)$ | ❌ No | Sorted Natural / Custom |
| **`ConcurrentHashMap`** | Array + CAS + synchronized Bucket | $O(1)$ (Lock-Free) | $O(1)$ (Bucket Lock) | $O(1)$ | ✅ Yes | None |
| **`CopyOnWriteArrayList`**| Re-allocated Array Copy | $O(1)$ (Lock-Free) | $O(N)$ | $O(N)$ | ✅ Yes | Insertion Order |
| **`PriorityQueue`** | Array-Backed Min Binary Heap | $O(1)$ (`peek`) | $O(\log N)$ | $O(\log N)$ (`poll`) | ❌ No | Priority Order |

---

# ⚡ Module 2: Java Multithreading, Concurrency & Java Memory Model (JMM)

---

### Q36: What is the Java Memory Model (JMM), and why do multi-threaded programs suffer from visibility and instruction reordering issues?
* **Hardware & JMM Mechanics**:
  * In modern multi-core processors, each CPU core has its own private L1/L2 caches and write store buffers. RAM is relatively slow (~100 CPU cycles), so cores read from and write to their local L1/L2 caches.
  * **Visibility Problem**: When Thread A on Core 1 updates variable `x = 42`, the update may linger inside Core 1's local cache or store buffer without being flushed immediately to main RAM. Thread B running on Core 2 reads `x` from its own cache and sees stale data (`x = 0`).
  * **Instruction Reordering Problem**: Both the Java JIT compiler and hardware CPU execution pipelines reorder independent instructions out-of-order to maximize instruction pipelining and superscalar execution throughput.
  * **The JMM Specification (JSR-133)**: JMM provides a formal contract defining **Happens-Before** relationships. It specifies exactly when writes by one thread become visible to reads by another thread across hardware memory barriers.

```
====================== CPU CACHE COHERENCE & JMM ======================
 [ Thread A (Core 1) ]                      [ Thread B (Core 2) ]
        |                                          |
   [ L1/L2 Cache ] (x = 42)                   [ L1/L2 Cache ] (x = 0 - Stale!)
        |                                          |
 ══════════════════════ [ MAIN MEMORY (RAM) ] ═════════════════════════
                              (x = 0)
=======================================================================
```

---

### Q37: How does `volatile` work under the hood in Java? Explain Memory Barriers and Happens-Before rules.
* **Low-Level Assembly & Hardware Mechanics**:
  * Declaring a variable `volatile` guarantees two distinct properties: **Visibility** and **Ordering (Prevents Reordering)**.
  * **1. Visibility Guarantee**:
    * When a thread writes to a `volatile` variable, the JVM emits a hardware memory barrier instruction (e.g., `lock addl` on x86 or `dmb` on ARM) that immediately flushes the CPU store buffer out to Main Memory (L3/RAM).
    * When a thread reads a `volatile` variable, the CPU invalidates its local L1/L2 cache line, forcing an authoritative read directly from Main Memory.
  * **2. Memory Barriers (Preventing Reordering)**:
    * **StoreStore Barrier**: Placed before volatile write $\to$ ensures all preceding normal writes are flushed before the volatile write occurs.
    * **StoreLoad Barrier**: Placed after volatile write $\to$ prevents the volatile write from being reordered with subsequent volatile reads/writes.
    * **LoadLoad & LoadStore Barriers**: Placed after volatile read $\to$ prevents subsequent normal reads/writes from executing before the volatile read finishes.
  * **Does `volatile` guarantee atomicity?**
    * **NO!** Compound operations like `count++` consist of 3 distinct bytecode instructions: (1) read value, (2) increment by 1, (3) write back. If two threads execute `count++` concurrently on a volatile variable, they can both read the same value and overwrite each other, causing lost updates. Use `AtomicInteger` or `synchronized` for atomicity.

---

### Q38: How does `synchronized` work internally at the JVM bytecode and Object Monitor level?
* **Object Header & Monitor Inflation**:
  * In HotSpot JVM, every object in heap memory has an **Object Header** consisting of:
    1. **Mark Word** (64 bits on 64-bit architectures): Stores hash code, GC age, lock state flags, and monitor pointers.
    2. **Klass Word**: Pointer to class metadata in Metaspace.
  * **Bytecode Level**:
    * Synchronized code blocks compile into `monitorenter` and `monitorexit` bytecode instructions with an automated exception table to ensure `monitorexit` is executed even if runtime exceptions are thrown.
  * **Lock Escalation (Biased $\to$ Lightweight $\to$ Heavyweight)**:
    1. **Biased Locking (Deprecated in JDK 15+)**: Assumes a lock is accessed by only 1 thread. Sets thread ID in Mark Word via CAS with zero synchronization overhead.
    2. **Lightweight (Thin) Locking**: If another thread contends, the lock escalates. The contending thread uses **CAS** to store a pointer to its own execution stack frame (Lock Record) in the object's Mark Word.
    3. **Heavyweight Locking (OS Mutex)**: If CAS spinning fails under high contention, the JVM allocates an `ObjectMonitor` structure (C++ level) containing `_EntryList`, `_WaitSet`, and `_owner`. Contending threads are suspended at the OS kernel level (`pthread_mutex`), incurring context switch costs (~1-5 microseconds).

```
====================== HOTSPOT OBJECT MARK WORD LOCK STATES ======================
[ Bits 0..61: HashCode / Stack Pointer / Monitor Ptr ] [ Bit 62: Biased ] [ Bits 63-64: State ]
- 001: Normal (Unlocked)
- 000: Lightweight Locked (CAS on Stack Pointer)
- 010: Heavyweight Locked (Inflated to OS ObjectMonitor Mutex)
- 011: Marked for GC
==================================================================================
```

---

### Q39: Why must `wait()`, `notify()`, and `notifyAll()` be called inside a `synchronized` block? Why is a `while` loop mandatory for `wait()`?
* **Race Condition Prevention & Spurious Wakeups**:
  * **1. Why `synchronized` is required**:
    * `wait()` and `notify()` manipulate the internal `_WaitSet` of the object's OS Monitor.
    * If `wait()` could be called without holding the lock, a race condition called **"Lost Wakeup"** occurs: Thread A evaluates condition `queue.isEmpty() == true`, but before it calls `wait()`, Thread B produces an item and calls `notify()`. Thread A then calls `wait()`, missing the notification and sleeping forever!
  * **2. Why `while` loop is mandatory (Spurious Wakeups)**:
    * Operating systems and JVMs may occasionally wake up sleeping threads without any explicit `notify()` call (**Spurious Wakeup**).
    * Multiple waiting threads may be awakened by `notifyAll()`, but only one thread can consume the available resource.
    * Checking the condition inside a `while (condition) { obj.wait(); }` loop forces the awakened thread to re-verify that the business condition is still valid before continuing execution.

---

### Q40: What is the internal difference between `Thread.sleep()` and `Object.wait()`?
* **Comparison Table**:

| Feature | `Thread.sleep(ms)` | `Object.wait()` |
|---|---|---|
| **Class Origin** | Static method of `java.lang.Thread` | Instance method of `java.lang.Object` |
| **Lock Release** | **Does NOT release locks** held in current synchronized block | **Releases the object monitor lock** so other threads can enter |
| **Wakeup Mechanism**| Wakes automatically after timer expires | Wakes via `notify()`, `notifyAll()`, or timeout |
| **Prerequisites** | Can be called from anywhere | Must be called inside a `synchronized` block on that object |

---

### Q41: How does `ReentrantLock` work internally? Explain the AbstractQueuedSynchronizer (AQS) framework.
* **AQS Architecture & Mechanics**:
  * `ReentrantLock` delegates all locking logic to an internal subclass of **AbstractQueuedSynchronizer (AQS)**.
  * **1. State Variable (`volatile int state`)**:
    * $0$: Lock is free.
    * $\ge 1$: Lock is held. Reentrant acquisitions by the owning thread increment `state++` without blocking. Releases decrement `state--`. When `state == 0`, the lock is fully unlocked.
  * **2. The CLH Double-Linked FIFO Queue**:
    * Contending threads that fail an initial atomic CAS attempt on `state` are wrapped in a `Node` object (`thread, prev, next, waitStatus`).
    * The node is appended to the tail of the FIFO queue using a CAS loop.
    * The thread is suspended using `LockSupport.park(this)`.
  * **3. Fair vs Non-Fair Locking**:
    * **Non-Fair (Default)**: A newly arriving thread immediately attempts a CAS on `state` before checking the queue. If it succeeds, it "barges" ahead of queued threads, avoiding thread unpark context switch latency ($2\times\text{--}5\times$ higher throughput).
    * **Fair**: A newly arriving thread checks `hasQueuedPredecessors()`. If threads are already waiting in the CLH queue, it enqueues itself at the tail to preserve FIFO ordering.

```
====================== AQS CLH FIFO WAIT QUEUE ======================
 [ Head (Dummy Node) ] <===> [ Node (Thread 1) ] <===> [ Node (Thread 2) ] (Tail)
           ^
           | (Unparks successor on release via LockSupport.unpark())
=====================================================================
```

---

### Q42: What is `StampedLock`, and how does its Optimistic Reading mode outperform `ReentrantReadWriteLock`?
* **Optimistic Concurrency Control**:
  * In standard `ReentrantReadWriteLock`, a read lock blocks all write locks, and write locks block all read locks. Under heavy read traffic, writers experience **Write Starvation**.
  * `StampedLock` introduces an **Optimistic Read Mode** (`tryOptimisticRead()`) that acquires **zero locks**:
    1. The reader acquires a version stamp: `long stamp = lock.tryOptimisticRead()`.
    2. The reader reads data from shared memory into local variables.
    3. The reader validates whether a writer modified the state during reading: `if (!lock.validate(stamp))`.
    4. If validated (`true`), reading succeeded without a single lock acquisition or memory barrier overhead!
    5. If invalid (`false`), it gracefully falls back to acquiring a standard pessimistic read lock: `stamp = lock.readLock()`.

---

### Q43: What is the internal architecture of `ThreadPoolExecutor`? Explain the 4 worker pool parameters and task queuing behavior.
* **Lifecycle & Task Routing Rules**:
  * A `ThreadPoolExecutor` manages task execution using 4 key components: `corePoolSize`, `maximumPoolSize`, `keepAliveTime`, and `BlockingQueue<Runnable> workQueue`.
  * **When a new task `executor.execute(task)` arrives**:
    1. If `activeThreads < corePoolSize`: Spawns a new `Worker` thread (wrapping an OS thread) to execute the task immediately.
    2. If `activeThreads >= corePoolSize`: Attempts to enqueue the task into `workQueue.offer(task)`.
    3. If `workQueue` is full AND `activeThreads < maximumPoolSize`: Spawns a new temporary non-core `Worker` thread to execute the task.
    4. If `workQueue` is full AND `activeThreads == maximumPoolSize`: Triggers the configured **`RejectedExecutionHandler`**.
  * **The 4 Rejection Policies**:
    1. **`AbortPolicy` (Default)**: Throws `RejectedExecutionException`.
    2. **`CallerRunsPolicy`**: The thread calling `execute()` executes the task itself, applying natural backpressure and slowing down submission rate.
    3. **`DiscardPolicy`**: Silently drops the rejected task without error.
    4. **`DiscardOldestPolicy`**: Evicts the oldest unhandled task from the head of the queue and retries task submission.

---

### Q44: Why should you NEVER use `Executors.newFixedThreadPool()` or `Executors.newCachedThreadPool()` in production systems?
* **OutOfMemoryError Hazard**:
  * **`Executors.newFixedThreadPool(n)`**:
    * Backed by an **unbounded `LinkedBlockingQueue`** (`Integer.MAX_VALUE` capacity $\approx 2.14$ billion tasks).
    * If request traffic surges faster than worker threads can process, millions of tasks pile up in memory, causing an **`OutOfMemoryError: Java heap space`** and taking down the entire JVM microservice!
  * **`Executors.newCachedThreadPool()`**:
    * Configured with `maximumPoolSize = Integer.MAX_VALUE` backed by `SynchronousQueue`.
    * Under high concurrent load, it spawns an unlimited number of OS threads. Since each thread consumes ~1MB stack memory and OS thread handles, it rapidly triggers **`OutOfMemoryError: unable to create new native thread`**, crashing the operating system host.
  * **Production Standard**: Always instantiate a custom `ThreadPoolExecutor` with a bounded queue (`ArrayBlockingQueue(1000)`) and a sensible `CallerRunsPolicy`.

---

### Q45: How does `ThreadLocal` work internally, and why does it cause catastrophic memory leaks in web servers with thread pools?
* **Internal Structure & Root Cause**:
  * Every `Thread` object maintains its own private `ThreadLocal.ThreadLocalMap threadLocals` hash table.
  * `ThreadLocalMap` entries extend `WeakReference<ThreadLocal<?>>`, where the **Key** is a weak reference to the `ThreadLocal` instance, but the **Value** is a **Strong Reference** to the user data object.
  * **The Memory Leak Mechanism**:
    1. In container thread pools (Tomcat, Jetty, Netty), worker threads are reused indefinitely and never terminate.
    2. If a web request sets a value in `ThreadLocal` and the `ThreadLocal` variable is garbage collected, the Entry's weak key becomes `null`.
    3. However, because the worker thread is still alive in the pool, its `threadLocals` map maintains a strong reference chain: `Thread -> ThreadLocalMap -> Entry -> Value (UserData / ClassLoader)`.
    4. This prevents the large value object (and associated ClassLoaders) from ever being garbage collected, causing a progressive **Metaspace / Heap Memory Leak**!
  * **Mandatory Solution**: Always clean up `ThreadLocal` inside a `finally` block:
    ```java
    try {
        userContext.set(currentUser);
        processRequest();
    } finally {
        userContext.remove(); // Unlinks entry and prevents thread-pool leak
    }
    ```

---

### Q46: How does `CompletableFuture` work, and how do you compose complex asynchronous non-blocking pipelines?
* **Pipeline Mechanics & Callbacks**:
  * `CompletableFuture<T>` represents a promise of an asynchronous computation completing in the future, providing non-blocking functional composition.
  * **Key Pipeline Operations**:
    * `supplyAsync(Supplier<U>, Executor)`: Starts asynchronous task in specified thread pool.
    * `thenApply(Function<T, R>)`: Transforms result synchronously when previous stage completes ($T \to R$).
    * `thenCompose(Function<T, CompletableFuture<R>>)`: Flattens nested futures (similar to `flatMap`), executing dependent async task sequentially.
    * `thenCombine(CompletableFuture<U>, BiFunction<T, U, V>)`: Executes two independent futures in parallel and combines their results when both finish.
    * `CompletableFuture.allOf(f1, f2, f3)`: Returns a new future that completes only when all provided futures complete.
    * `exceptionally(Function<Throwable, T>)` / `handle()`: Catches and recovers from pipeline exceptions without breaking the stream.

---

### Q47: Compare `CountDownLatch` vs `CyclicBarrier` vs `Semaphore` vs `Phaser`.
* **Deep Architectural Comparison**:
  * **`CountDownLatch`**:
    * One-shot gate initialized with count $N$.
    * Threads call `countDown()` to decrement and `await()` to block until count reaches $0$.
    * **Cannot be reset or reused**.
  * **`CyclicBarrier`**:
    * Reusable synchronization point for $N$ threads.
    * Each thread calls `await()` and blocks until all $N$ threads reach the barrier, then releases all threads simultaneously and triggers an optional barrier action.
    * **Can be reset (`reset()`) and reused across iterations**.
  * **`Semaphore`**:
    * Manages a fixed number of permits ($K$).
    * Threads acquire permits (`acquire()`) and release permits (`release()`).
    * Used for resource rate-limiting (e.g., maximum 10 concurrent database connections).
  * **`Phaser`**:
    * Flexible multi-phase barrier supporting dynamic registration and deregistration of participating threads at runtime.

---

### Q48: How does `LongAdder` work, and why does it outperform `AtomicLong` under high concurrent contention?
* **Cell Striping & Cache Line Bouncing Defense**:
  * `AtomicLong` uses a single `volatile long value` updated via atomic CAS in a tight spin loop. Under hundreds of concurrent threads, multiple CPU cores constantly attempt CAS on the exact same cache line, causing extreme **CPU L1 Cache Coherence Bus Contention** (cache line bouncing).
  * `LongAdder` extends `Striped64` and uses **Cell Striping**:
    1. If there is no contention, threads update a single `base` value.
    2. When CAS contention is detected, `LongAdder` dynamically allocates a distributed `Cell[] cells` array.
    3. Each thread is hashed to a specific `Cell` based on its thread probe hash, and increments its own private cell.
    4. `sum()` computes total count by adding `base` and all active `Cell.value`s, achieving near-zero contention and up to **$10\times$ higher write throughput**!

---

### Q49: What is the ABA Problem in lock-free CAS algorithms, and how does `AtomicStampedReference` solve it?
* **Root Cause & Version Stamping**:
  * **The ABA Scenario**:
    1. Thread 1 reads pointer `A` from shared memory.
    2. Thread 1 is preempted by the OS.
    3. Thread 2 changes pointer `A` to `B`, and then changes it back from `B` to `A`.
    4. Thread 1 resumes and executes `CAS(expected=A, new=C)`.
    5. The CAS succeeds because the memory still holds `A`, even though the underlying data structure state was mutated and corrupted in the interim (critical in lock-free stacks and memory re-use).
  * **Solution**: `AtomicStampedReference<V>` binds an integer **version stamp / counter** to the reference: `[Reference, Stamp]`.
  * The CAS operation updates both simultaneously: `compareAndSet(expectedRef, newRef, expectedStamp, newStamp)`. Even if reference reverts to `A`, the incremented stamp ($1 \to 2$) prevents false CAS execution.

---

### Q50: How do **Java 21 Virtual Threads (Project Loom)** work under the hood? Compare Platform Threads vs Virtual Threads.
* **M:N Continuation & Carrier Thread Architecture**:
  * **Platform Threads (Classic Java Threads)**:
    * 1:1 mapping with Operating System Kernel Threads.
    * Memory overhead: ~1MB dedicated OS call stack allocated off-heap.
    * Context switching requires expensive OS kernel trap ($1\text{--}5\ \mu\text{s}$). Maximum ~5,000 threads per JVM before memory exhaustion.
  * **Virtual Threads (Java 21 LTS)**:
    * $M:N$ mapping: Millions of lightweight Virtual Threads multiplexed onto a tiny pool of OS **Carrier Threads** (default: `ForkJoinPool` sized to CPU core count).
    * Memory overhead: Only **a few hundred bytes** stored directly on the JVM Heap.
    * **Continuation Unmounting**: When a Virtual Thread executes a blocking operation (e.g., JDBC call, HTTP socket read, `Thread.sleep()`), the JVM **unmounts** the virtual thread from its OS carrier thread and saves its stack frames as a Continuation object on the heap. The carrier thread immediately executes other virtual threads!
    * When the I/O event finishes, the OS epoll/kqueue notifies the JVM, which remounts the virtual thread onto an available carrier thread, achieving **hundreds of thousands of concurrent connections with simple synchronous code**!

```
====================== VIRTUAL THREADS (M:N MULTIPLEXING) ======================
 [ Virtual Thread 1 ] [ Virtual Thread 2 ] ... [ Virtual Thread 1,000,000 ]
           \                  |                  /
            ═══════════ JVM SCHEDULER ═══════════
                              |
    [ Carrier OS Thread 1 ] [ Carrier OS Thread 2 ] (ForkJoinPool)
================================================================================
```

---

### Q51: What is "Thread Pinning" in Java 21 Virtual Threads, and how do you prevent it?
* **Carrier Thread Blockade**:
  * **Pinning**: Occurs when a Virtual Thread cannot be unmounted from its underlying OS Carrier Thread during a blocking operation. This hogs the carrier thread, degrading system concurrency.
  * **Two Causes of Pinning**:
    1. Executing a blocking operation inside a **`synchronized` block or method**.
    2. Executing blocking native calls (JNI / C-library bindings).
  * **How to Fix**: Replace all blocking `synchronized` blocks in library code with **`ReentrantLock`**, which fully supports virtual thread continuation unmounting without pinning!

---

### Q52: How do you detect and diagnose Deadlocks in a production Java application?
* **Diagnosis & Tooling**:
  * **1. Thread Dump Analysis (`jstack`)**:
    * Run `jstack <PID>` or `jcmd <PID> Thread.print`.
    * HotSpot JVM automatically runs cycle detection on OS monitors and logs:
      `Found 1 deadlock. Thread-1 waiting to lock <0x123> (held by Thread-2), Thread-2 waiting to lock <0x456> (held by Thread-1)`.
  * **2. Programmatic Detection (`ThreadMXBean`)**:
    * Use JMX `ThreadMXBean.findDeadlockedThreads()` to detect deadlocked thread IDs in real time and trigger automatic alerts.
  * **3. Prevention Strategies**:
    * **Strict Lock Ordering**: Always acquire multiple locks in identical global hierarchical order across all classes (e.g., lock Account with smaller ID first).
    * **Timed Lock Acquisition**: Use `lock.tryLock(5, TimeUnit.SECONDS)` instead of `lock.lock()`. If the lock cannot be acquired within 5 seconds, back off and release all acquired resources.

---

### Q53: What is the difference between `Runnable` and `Callable` in Java?
* **Exception & Return Type Handling**:
  * **`Runnable`**: Defines `public void run()`. Cannot return any result and cannot throw checked exceptions.
  * **`Callable<V>`**: Defines `public V call() throws Exception`. Returns a generic result of type `V` and can propagate checked exceptions directly to the calling future (`Future.get()`).

---

### Q54: How does `ForkJoinPool` work, and what is Work-Stealing?
* **Work-Stealing Architecture**:
  * `ForkJoinPool` is designed for recursive Divide-and-Conquer tasks (`RecursiveTask`, `RecursiveAction`).
  * Each worker thread maintains its own private double-ended queue (**Deque**).
  * When a thread forks subtasks, it pushes them to the **head** of its own Deque (LIFO access for maximum cache locality).
  * **Work-Stealing**: If a worker thread exhausts all its own tasks, it scans other worker threads' Deques and "steals" tasks from the **tail** (FIFO access via lock-free CAS) to keep all CPU cores at $100\%$ utilization.

---

### Q55: What is False Sharing in multi-threaded CPU architectures, and how does Java solve it?
* **Hardware Cache Line Invalidation**:
  * Modern CPUs load memory in 64-byte chunks called **Cache Lines**.
  * If two independent variables `x` and `y` (modified by Thread 1 on Core 1 and Thread 2 on Core 2) reside in the same 64-byte cache line:
  * Whenever Core 1 writes to `x`, the MESI cache coherence protocol invalidates the *entire* 64-byte cache line in Core 2, forcing Core 2 to reload `y` from RAM even though `y` was never modified!
  * **Java Solution**: The **`@jdk.internal.vm.annotation.Contended`** annotation inserts 128 bytes of padding bytes before and after the field, ensuring the variable resides in its own isolated cache line.

---

### Q56: What is the difference between Livelock and Deadlock?
* **Active State vs Blocked State**:
  * **Deadlock**: Two or more threads are permanently blocked, waiting for locks held by each other. CPU utilization drops to $0\%$.
  * **Livelock**: Two or more threads continuously change their state in response to each other without making any actual progress (e.g., two polite people in a hallway repeatedly stepping to the same side to let the other pass). CPU utilization spikes to $100\%$ while the system is completely frozen!

---

### Q57: How does `Exchanger` coordinate data exchange between two threads?
* **Bidirectional Synchronization Point**:
  * `Exchanger<V>` provides a synchronization rendezvous point where two threads atomically swap objects.
  * Thread A calls `exchanger.exchange(bufferA)` and blocks until Thread B calls `exchanger.exchange(bufferB)`.
  * When both arrive, the JVM atomically swaps references (Thread A receives `bufferB`, Thread B receives `bufferA`) and resumes both threads, making it ideal for genetic algorithms and dual-buffer pipelining.

---

### Q58: What is the difference between `submit()` and `execute()` in `ExecutorService`?
* **Future Return & Exception Handling**:
  * **`execute(Runnable)`**: Defined in `Executor`. Executes task asynchronously. Returns `void`. If an unchecked exception occurs, it is logged directly to console and may terminate the worker thread.
  * **`submit(Callable/Runnable)`**: Defined in `ExecutorService`. Returns a `Future<T>`. Any thrown exception is caught internally and wrapped in an `ExecutionException`, surfaced only when the client invokes `future.get()`.

---

### Q59: What is the purpose of `Thread.yield()`? Does it guarantee CPU release?
* **OS Scheduler Hint**:
  * `Thread.yield()` provides a non-binding hint to the Operating System thread scheduler that the current thread is willing to yield its current CPU time slice.
  * **No Guarantees**: The OS scheduler is completely free to ignore the hint. The yielding thread moves from `RUNNING` to `RUNNABLE` and may immediately be re-scheduled on the next clock cycle.

---

### Q60: How does `AtomicInteger.compareAndSet()` work at the machine instruction level?
* **Hardware CPU Instructions**:
  * `compareAndSet(int expect, int update)` invokes `Unsafe.compareAndSwapInt()` which compiles to a single atomic CPU hardware instruction:
    * **x86/x64**: `LOCK CMPXCHG` (Bus lock / Cache lock comparing memory address with expected value and updating atomically).
    * **ARM**: `LDREX / STREX` (Load-Linked / Store-Conditional pair).
  * If the value in memory matches `expect`, it updates to `update` and returns `true` in 1 clock cycle; otherwise returns `false` without locking.

---

### Q61: What is a Thread Group, and why is it deprecated in modern Java?
* **Flawed Security & Thread Safety**:
  * `ThreadGroup` was introduced in JDK 1.0 to group threads for management and security permissions.
  * It is obsolete because its methods (like `stop()`, `suspend()`, `resume()`) were inherently thread-unsafe and deadlock-prone. Modern applications use `ExecutorService` and `ThreadFactory` instead.

---

### Q62: What is the difference between User Threads and Daemon Threads?
* **JVM Shutdown Semantics**:
  * **User Threads (Default)**: High-priority threads executing application logic. The JVM will **NOT exit** as long as at least one non-daemon user thread is still running.
  * **Daemon Threads (`thread.setDaemon(true)`)**: Background support threads (e.g., GC thread, finalizer, JMX monitors). The JVM exits immediately when all user threads finish, terminating all daemon threads abruptly without executing their `finally` blocks!

---

### Q63: How do you safely stop a running thread in Java without using deprecated `Thread.stop()`?
* **Cooperative Thread Interruption**:
  * `Thread.stop()` is deprecated because it unceremoniously releases all locked monitors, leaving shared objects in corrupted, inconsistent states.
  * **Safe Pattern**: Use **Cooperative Interruption (`Thread.interrupt()`)**:
    1. The managing thread calls `workerThread.interrupt()`.
    2. The worker thread periodically checks `Thread.currentThread().isInterrupted()`.
    3. If blocked on `sleep()`, `wait()`, or I/O, an `InterruptedException` is thrown, allowing the thread to clean up resources in `finally` and exit gracefully.

---

### Q64: What is the difference between `interrupted()` and `isInterrupted()` in `Thread`?
* **Flag Clearing Semantics**:
  * **`thread.isInterrupted()`**: Instance method that queries whether the target thread has been interrupted. **Does NOT clear** the interrupt status flag.
  * **`Thread.interrupted()`**: Static method that queries the *current* thread's interrupt status AND **clears the interrupt flag** (resets it to `false`).

---

### Q65: What is Thread Starvation, and how do you prevent it?
* **CPU & Lock Inequity**:
  * **Starvation**: Occurs when a runnable thread is perpetually denied access to CPU cycles or locks because greedy high-priority threads dominate the system.
  * **Prevention**:
    * Avoid arbitrary `Thread.setPriority()` modifications.
    * Use Fair ReentrantLocks (`new ReentrantLock(true)`) or fair Semaphores.
    * Use standard FIFO queues (`LinkedBlockingQueue`).

---

### Q66: What is the difference between `Lock` and `synchronized` in terms of flexibility?
* **Capabilities Matrix**:
  * `Lock` (`ReentrantLock`) provides advanced capabilities unavailable in `synchronized`:
    1. **Timed Acquisition**: `tryLock(timeout, unit)`.
    2. **Interruptible Acquisition**: `lockInterruptibly()`.
    3. **Fairness Selection**: FIFO fair lock option.
    4. **Multiple Condition Variables**: Multiple independent `Condition` wait-sets per lock (e.g., `notEmpty`, `notFull`).
    5. **Non-block-structured Locking**: Ability to acquire lock in method A and release in method B.

---

### Q67: How does `Condition.await()` differ from `Object.wait()`?
* **Precision & Multiple Wait-Sets**:
  * An object monitor has only a **single wait-set** (`_WaitSet`), meaning `notifyAll()` wakes up both producers and consumers simultaneously, causing high contention.
  * A `ReentrantLock` can instantiate **multiple distinct `Condition` objects** (`lock.newCondition()`):
    * Producers wait on `notFull.await()` and signal consumers via `notEmpty.signal()`.
    * Consumers wait on `notEmpty.await()` and signal producers via `notFull.signal()`.
    * This ensures only the exact target thread class is awakened!

---

### Q68: What is Priority Inversion, and how do operating systems mitigate it?
* **Resource Inversion Hazard**:
  * Occurs when a low-priority thread holds a lock needed by a high-priority thread, but a medium-priority thread preempts the low-priority thread, indirectly starving the high-priority thread!
  * **Solution (Priority Inheritance Protocol)**: The OS temporarily elevates the priority of the lock-holding low-priority thread to match the high-priority thread until the lock is released.

---

### Q69: What is `InheritableThreadLocal`, and how does it propagate context to child threads?
* **Child Thread Context Cloning**:
  * `InheritableThreadLocal<T>` extends `ThreadLocal<T>`.
  * When a parent thread creates a new child thread (`new Thread()`), the JVM copies all entries from the parent's `inheritableThreadLocals` map into the child thread's map during initialization.
  * *Note*: In thread pools, tasks reuse pooled threads, so `InheritableThreadLocal` does not automatically update pooled worker threads. Use OpenTelemetry or MDC task wrappers instead.

---

### Q70: How does `ScheduledThreadPoolExecutor` implement delayed and periodic execution?
* **DelayedWorkQueue & Leader-Follower Pattern**:
  * Backed by a specialized `DelayedWorkQueue` (array-based binary min-heap sorted by task execution timestamp).
  * Worker threads sleep until the timestamp of the earliest scheduled task.
  * **Fixed Rate (`scheduleAtFixedRate`)**: Calculates next execution time based on initial scheduled start time ($T, T+P, T+2P$), catching up if delayed.
  * **Fixed Delay (`scheduleWithFixedDelay`)**: Calculates next execution time only *after* the previous execution completes ($T_{\text{finish}} + P$).

---

### Q71: What is a Thread-Safe Singleton without locks? Explain the Bill Pugh Static Holder Pattern.
* **JVM Class Initialization Guarantees**:
  * The **Bill Pugh Singleton** leverages the JVM's class-loading mechanism (JLS 12.4.2) which guarantees thread-safe class initialization:
    ```java
    public class BillPughSingleton {
        private BillPughSingleton() {}
        private static class Holder {
            private static final BillPughSingleton INSTANCE = new BillPughSingleton();
        }
        public static BillPughSingleton getInstance() {
            return Holder.INSTANCE; // Class loaded only upon first call!
        }
    }
    ```
  * The inner static class `Holder` is NOT loaded when `BillPughSingleton` is loaded. It is loaded only when `getInstance()` is called, achieving **$100\%$ lazy initialization and zero synchronization lock overhead**!

---

### Q72: What is the difference between `Callable` and `Future`?
* **Producer vs Consumer Handle**:
  * **`Callable`**: The task definition to be executed (`call()`).
  * **`Future`**: The handle to query the status (`isDone()`, `isCancelled()`), cancel the task (`cancel()`), or block and retrieve the result (`get()`).

---

### Q73: How does `AtomicReferenceFieldUpdater` optimize memory in high-performance libraries?
* **Zero Wrapper Overhead**:
  * Instead of creating a distinct `AtomicReference` wrapper object for every node in a data structure (which wastes 16-24 bytes per node), `AtomicReferenceFieldUpdater` allows atomic CAS operations directly on a `volatile` field inside existing objects via reflection metadata, saving gigabytes of heap memory in Netty and Akka.

---

### Q74: What is the difference between `ParallelStream` and `ExecutorService`?
* **Workload Affinity**:
  * **`ParallelStream`**: Designed for CPU-intensive, in-memory data processing. Uses the shared `ForkJoinPool.commonPool()`. If a parallel stream executes blocking I/O (e.g., HTTP/DB calls), it starves the global common pool and degrades performance across the entire JVM.
  * **`ExecutorService`**: Designed for I/O-intensive, heterogeneous asynchronous tasks with dedicated isolated thread pools.

---

### Q75: How does `Thread.join()` work internally?
* **Object Monitor Wait Loop**:
  * `thread.join()` synchronizes on the target `Thread` object instance and executes a `while (isAlive()) { wait(0); }` loop.
  * When the target thread terminates, the JVM internally invokes `notifyAll()` on that `Thread` object, waking up all threads waiting in `join()`.

---

### Q76: What is a Semaphore with 1 permit? Is it identical to a `ReentrantLock`?
* **Reentrancy & Ownership Differences**:
  * A Binary Semaphore (`new Semaphore(1)`) is **NOT identical** to a `ReentrantLock`:
    1. **Ownership**: A `ReentrantLock` has an owner thread. Only the thread that acquired the lock can release it. A `Semaphore` has no owner concept; Thread A can acquire the permit, and Thread B can release it!
    2. **Reentrancy**: If the owning thread calls `acquire()` twice on a binary semaphore, it deadlocks itself!

---

### Q77: What happens when an unhandled exception is thrown inside a Thread?
* **UncaughtExceptionHandler Dispatch**:
  * The thread terminates abruptly.
  * The JVM dispatches the exception to the thread's `UncaughtExceptionHandler`.
  * If none is set, it cascades to the `ThreadGroup` handler and finally to the default global handler (`Thread.setDefaultUncaughtExceptionHandler()`), logging the stack trace to `System.err`.

---

### Q78: What is the difference between `ConcurrentHashMap.size()` and `ConcurrentHashMap.mappingCount()`?
* **64-bit Count Return**:
  * `size()` returns an `int` (capped at `Integer.MAX_VALUE` $\approx 2.14$ billion).
  * `mappingCount()` returns a `long`, preventing integer overflow for massive in-memory caches holding more than 2 billion entries.

---

### Q79: How does `LockSupport.park()` and `unpark()` work under the hood?
* **Binary Token Semantics**:
  * `LockSupport` uses a permit token ($0$ or $1$) associated with each thread:
  * `unpark(thread)`: Sets the thread's token to $1$. If the thread was parked, it wakes up immediately.
  * `park()`: If token is $1$, consumes it and returns immediately without blocking. If token is $0$, suspends the thread until an `unpark()` occurs.
  * Unlike `wait()`/`notify()`, `unpark()` can be called **before** `park()`, completely eliminating lost-wakeup race conditions!

---

### Q80: Concurrency CheatSheet: Synchronization Primitives Quick Matrix

| Primitive | Mechanism | Reentrant | Fair Option | Non-blocking Try | Typical Use Case |
|---|---|---|---|---|---|
| **`synchronized`** | JVM Object Monitor | ✅ Yes | ❌ No | ❌ No | Simple block synchronization |
| **`ReentrantLock`** | AQS CLH Queue | ✅ Yes | ✅ Yes | ✅ Yes (`tryLock`) | Advanced lock control |
| **`CountDownLatch`** | AQS State Countdown | ❌ No | ❌ No | ✅ Yes (`await(time)`) | Service startup barrier |
| **`CyclicBarrier`** | ReentrantLock + Condition | ❌ No | ❌ No | ❌ No | Iterative multi-thread compute |

---

# 🛡️ Module 3: Design Patterns & The Singleton Pattern Masterclass

---

### Q81: What is the Singleton Design Pattern, and what are the 6 standard implementation strategies in Java?
* **Core Design Intent & Implementations**:
  * The **Singleton Pattern** ensures that a class has **only one active instance** in JVM memory and provides a global access point to it.
  * **1. Eager Initialization**: Instance is created at class-loading time:
    ```java
    public class EagerSingleton {
        private static final EagerSingleton INSTANCE = new EagerSingleton();
        private EagerSingleton() {}
        public static EagerSingleton getInstance() { return INSTANCE; }
    }
    ```
    * *Trade-off*: Thread-safe, but wastes memory if the instance is heavy and never used.
  * **2. Static Block Initialization**: Similar to eager, but allows error handling during instantiation.
  * **3. Lazy Initialization (Thread-Unsafe)**:
    ```java
    public class LazySingleton {
        private static LazySingleton instance;
        private LazySingleton() {}
        public static LazySingleton getInstance() {
            if (instance == null) { instance = new LazySingleton(); }
            return instance;
        }
    }
    ```
    * *Trade-off*: Two threads calling `getInstance()` simultaneously will create two distinct instances!
  * **4. Method-Synchronized Lazy Singleton**: Adding `public static synchronized LazySingleton getInstance()`.
    * *Trade-off*: Thread-safe, but causes $99\%$ unnecessary lock acquisition overhead on subsequent read calls after initialization.
  * **5. Double-Checked Locking (DCL) with `volatile`**: Described in Q82.
  * **6. Bill Pugh Singleton (Static Inner Helper Class)**: Described in Q83.

---

### Q82: How does Double-Checked Locking (DCL) Singleton work? Why does it CRITICALLY BREAK without `volatile`?
* **JVM Bytecode Instruction Reordering Breakdown**:
  * Double-Checked Locking attempts to minimize synchronization overhead by checking `instance == null` twice:
    ```java
    public class DclSingleton {
        private static volatile DclSingleton instance; // MUST BE VOLATILE!

        private DclSingleton() {}

        public static DclSingleton getInstance() {
            if (instance == null) { // First Check (No locking for high-speed reads)
                synchronized (DclSingleton.class) {
                    if (instance == null) { // Second Check (Ensures only 1 thread initializes)
                        instance = new DclSingleton();
                    }
                }
            }
            return instance;
        }
    }
    ```
  * **Why DCL Critically Fails Without `volatile`**:
    * In JVM bytecode, the instruction `instance = new DclSingleton()` is NOT an atomic operation. It consists of 3 distinct steps:
      1. `mem = allocate(sizeof(DclSingleton))` $\to$ Allocate heap memory block.
      2. `ctor(mem)` $\to$ Execute constructor to initialize object fields.
      3. `instance = mem` $\to$ Assign memory address to the reference variable `instance`.
    * The JIT Compiler and CPU out-of-order execution pipelines can reorder independent instructions **from `1 -> 2 -> 3` to `1 -> 3 -> 2`**!
    * **The Disaster Scenario**:
      1. Thread A enters synchronized block and executes Step 1 (memory allocated) and Step 3 (`instance` pointer assigned). `instance` is now **non-null**, but Step 2 (constructor execution) has NOT yet run!
      2. Thread B calls `getInstance()`. It executes the First Check: `if (instance == null)`.
      3. Since `instance != null` (due to Step 3), Thread B skips the synchronized block entirely and immediately returns the **half-initialized, corrupted object**!
      4. Thread B accesses fields on this phantom instance, triggering `NullPointerException`s, corrupted application state, or crashes.
    * **How `volatile` fixes it**: The `volatile` keyword inserts a **StoreStore memory barrier** before Step 3, strictly forbidding the JVM/hardware from publishing the reference until the constructor (Step 2) has completely finished.

```
====================== DCL INSTRUCTION REORDERING HAZARD ======================
Normal Order:     [ 1. Allocate Heap Memory ] -> [ 2. Run Constructor ] -> [ 3. Assign Reference ]
Reordered Order:  [ 1. Allocate Heap Memory ] -> [ 3. Assign Reference ] -> [ 2. Run Constructor ]
                                                        ^
                                                        |
 Thread B reads instance != null HERE! (Gets half-initialized corrupted object)
================================================================================
```

---

### Q83: How does the Bill Pugh Singleton (Static Inner Class) achieve thread safety without locks or `volatile`?
* **JLS Class Loading Invariants (JLS 12.4.2)**:
  * In Java, an inner static class is NOT loaded into memory when the outer class is loaded by the ClassLoader. It is loaded **lazily on-demand** only when its static members are explicitly referenced for the first time:
    ```java
    public class BillPughSingleton {
        private BillPughSingleton() {}

        private static class LazyHolder {
            // Initialized only when LazyHolder is loaded!
            private static final BillPughSingleton INSTANCE = new BillPughSingleton();
        }

        public static BillPughSingleton getInstance() {
            return LazyHolder.INSTANCE;
        }
    }
    ```
  * When `BillPughSingleton` is loaded, `LazyHolder` remains unloaded.
  * When `getInstance()` is called, the JVM loads `LazyHolder` and executes its static field initialization.
  * The Java Virtual Machine Specification strictly guarantees that **class initialization (`<clinit>`) is thread-safe and protected by internal JVM class-loading locks**, providing $100\%$ thread-safety, zero synchronization overhead, and lazy on-demand initialization.

---

### Q84: How to BREAK Singleton via Java Reflection, and how to defend against it?
* **Attack & Defensive Coding**:
  * **The Reflection Attack**: Java Reflection can bypass `private` access modifiers using `Constructor.setAccessible(true)`:
    ```java
    // ATTACK CODE:
    BillPughSingleton instance1 = BillPughSingleton.getInstance();
    Constructor<BillPughSingleton> constructor = BillPughSingleton.class.getDeclaredConstructor();
    constructor.setAccessible(true);
    BillPughSingleton instance2 = constructor.newInstance(); // Creates a 2nd instance!
    System.out.println(instance1 == instance2); // false (Singleton BROKEN!)
    ```
  * **The Solution (Constructor Guard)**: Throw an exception inside the private constructor if the singleton instance has already been instantiated:
    ```java
    public class SecureSingleton {
        private static boolean isInstantiated = false;

        private SecureSingleton() {
            synchronized (SecureSingleton.class) {
                if (isInstantiated) {
                    throw new IllegalStateException("Security Alert: Singleton instantiation via reflection is forbidden!");
                }
                isInstantiated = true;
            }
        }
    }
    ```

---

### Q85: How to BREAK Singleton via Serialization/Deserialization, and how to defend against it?
* **Attack & `readResolve()` Solution**:
  * **The Serialization Attack**: When a Singleton implementing `Serializable` is serialized to a byte stream and deserialized back via `ObjectInputStream.readObject()`, Java creates an **entirely new object in heap memory**, completely bypassing all constructors:
    ```java
    // ATTACK CODE:
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    new ObjectOutputStream(baos).writeObject(BillPughSingleton.getInstance());

    ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
    BillPughSingleton instance2 = (BillPughSingleton) new ObjectInputStream(bais).readObject();
    System.out.println(BillPughSingleton.getInstance() == instance2); // false (BROKEN!)
    ```
  * **The Solution (`readResolve`)**: Implement the special hook method `protected Object readResolve()`:
    ```java
    public class SerializableSingleton implements Serializable {
        private static final long serialVersionUID = 1L;
        private static final SerializableSingleton INSTANCE = new SerializableSingleton();

        private SerializableSingleton() {}
        public static SerializableSingleton getInstance() { return INSTANCE; }

        // JVM hook: Replaces deserialized object with the existing singleton instance!
        protected Object readResolve() {
            return getInstance();
        }
    }
    ```

---

### Q86: How to BREAK Singleton via Cloning, and how to defend against it?
* **Attack & `clone()` Defense**:
  * **The Cloning Attack**: If a singleton class extends a base class implementing `Cloneable`, calling `super.clone()` creates a shallow bitwise copy of the instance in heap memory without calling any constructor:
    ```java
    // ATTACK CODE:
    Singleton instance2 = (Singleton) instance1.clone(); // Creates 2nd instance!
    ```
  * **The Solution**: Explicitly override `clone()` to either throw `CloneNotSupportedException` or return the singleton instance directly:
    ```java
    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("Cloning of Singleton instances is forbidden!");
        // OR: return getInstance();
    }
    ```

---

### Q87: Why is the **Enum Singleton** (Joshua Bloch) considered the most bulletproof Singleton in Java?
* **Comprehensive JVM Immunity**:
  * In *Effective Java*, Joshua Bloch recommends a single-element Enum as the gold-standard Singleton:
    ```java
    public enum EnumSingleton {
        INSTANCE;

        public void executeBusinessLogic() {
            System.out.println("Executing thread-safe singleton business logic...");
        }
    }
    ```
  * **Why Enum Singleton CANNOT Be Broken**:
    1. **Immune to Reflection**: The JVM's `Constructor.newInstance()` explicitly checks `if ((clazz.getModifiers() & Modifier.ENUM) != 0) throw new IllegalArgumentException("Cannot reflectively create enum objects");`. Reflection attacks are impossible!
    2. **Immune to Serialization**: The JVM handles Enum serialization specially by writing only the enum constant's name (`"INSTANCE"`). During deserialization, `Enum.valueOf()` is called, ensuring the exact same singleton instance is returned without needing `readResolve()`.
    3. **Immune to Cloning**: `Enum.clone()` is `final` and throws `CloneNotSupportedException`.
    4. **Thread-Safe**: The JVM guarantees thread-safe initialization of static enum fields during class loading.

---

### Q88: How can Multiple ClassLoaders break a Singleton, and how do you resolve it?
* **Class Identity Invariant**:
  * In the JVM, a class's unique identity is defined by the tuple: `(Fully Qualified Class Name, ClassLoader Instance)`.
  * If two distinct ClassLoaders (e.g., in OSGi modules, Tomcat webapps, or plugin systems) load the same `Singleton.class` file, they create **two separate `Class` objects in Metaspace**, each having its own independent `static INSTANCE` field!
  * **Solutions**:
    1. Place the Singleton class in the shared Parent/Bootstrap ClassLoader path.
    2. Register the singleton in a shared JNDI or Spring `ApplicationContext` container rather than relying on JVM static variables.

---

### Q89: What is the Factory Method Pattern vs Abstract Factory Pattern?
* **Comparison**:
  * **Factory Method (Creational)**:
    * Uses **inheritance**. Defines an interface or abstract method for creating an object, letting subclasses decide which concrete class to instantiate (`DocumentCreator.createDocument()`).
  * **Abstract Factory (Creational)**:
    * Uses **composition**. Provides an interface for creating **families of related or dependent objects** without specifying their concrete classes (e.g., `GUIFactory` creates `Button`, `Checkbox`, and `ScrollBar` for Windows vs Mac).

---

### Q90: What is the Builder Pattern, and why is it preferred over Telescoping Constructors and JavaBeans Setters?
* **Immutability & Safety**:
  * **Telescoping Constructors**: Multiple constructors with 1, 2, 3, 4 arguments (`User(name)`, `User(name, age)`, `User(name, age, email)`). Hard to read and error-prone when multiple parameters share the same type (`int, int`).
  * **JavaBeans Pattern (Getters/Setters)**: Creating an empty object and calling setters (`user.setName(...)`). The object is in an **inconsistent, mutable state** during construction and cannot be made immutable (`final` fields).
  * **The Builder Pattern**:
    * Enforces mandatory fields and validates invariants before instantiation.
    * Allows fluent chaining: `User.builder().name("Alice").age(30).build()`.
    * Produces a fully initialized, thread-safe, **100% immutable** domain object.

---

### Q91: What is the Prototype Pattern, and what is the difference between Shallow Copy and Deep Copy?
* **Object Cloning Mechanics**:
  * **Prototype Pattern**: Creates new objects by copying/cloning an existing prototype instance (`clone()`).
  * **Shallow Copy**: Copies all primitive fields directly, but for object reference fields, it copies only the memory pointers. Both the original and cloned objects point to the exact same child objects in heap memory! Mutating a child object via the clone corrupts the original.
  * **Deep Copy**: Recursively clones all referenced objects, creating completely independent object graphs in heap memory (implemented via custom cloning, copy constructors, or Jackson JSON/Kryo serialization).

---

### Q92: What is the Adapter Pattern, and how does it bridge incompatible interfaces?
* **Structural Wrapper**:
  * The **Adapter Pattern** converts the interface of a class into another interface that clients expect.
  * **Real-World Analogy**: A European travel plug adapter allowing a 2-prong European laptop charger to plug into a 3-prong US wall socket.
  * **Java Example**: `Arrays.asList(T...)` acts as an adapter bridging a standard Java array (`T[]`) to the `java.util.List` interface.

---

### Q93: How does the Decorator Pattern work, and how is it used in the Java I/O Streams library?
* **Dynamic Behavior Composition**:
  * The **Decorator Pattern** attaches additional responsibilities and behavior to an object dynamically at runtime using composition instead of rigid subclassing.
  * **Java I/O Standard Example**:
    ```java
    InputStream in = new GZIPInputStream(
                        new BufferedInputStream(
                            new FileInputStream("data.gz")));
    ```
    * `FileInputStream`: Core component reading raw disk bytes.
    * `BufferedInputStream`: Decorator adding 8KB in-memory read buffering.
    * `GZIPInputStream`: Decorator transparently decompressing the gzipped byte stream.

---

### Q94: What is the Proxy Pattern? Compare Static Proxy, JDK Dynamic Proxy, and CGLIB Proxy.
* **Interception & Virtual Representatives**:
  * The **Proxy Pattern** provides a surrogate or placeholder object that controls and intercepts access to the real target object (used for lazy loading, access control, transaction management `@Transactional`, and logging).
  * **1. Static Proxy**: Developer manually writes a proxy class implementing the same interface as the target. Requires writing duplicate boilerplate for every class.
  * **2. JDK Dynamic Proxy (`java.lang.reflect.Proxy`)**:
    * Generates proxy bytecodes dynamically at runtime in memory.
    * **Requirement**: The target class **MUST implement at least one interface**.
    * Uses an `InvocationHandler.invoke(proxy, method, args)` callback.
  * **3. CGLIB Proxy (Bytecode Generation via ASM)**:
    * Generates a dynamic subclass of the target class at runtime and overrides methods.
    * **Advantage**: Does NOT require interfaces; can proxy concrete classes directly!
    * **Limitation**: Cannot proxy `final` classes or `final` methods.

---

### Q95: What is the Strategy Pattern, and how do Java 8 Lambdas simplify it?
* **Behavioral Algorithm Encapsulation**:
  * The **Strategy Pattern** defines a family of interchangeable algorithms, encapsulates each one in a separate class, and makes them interchangeable at runtime (e.g., `PaymentStrategy` with `CreditCardPayment`, `PayPalPayment`, `CryptoPayment`).
  * **Java 8 Simplification**: Because strategies are single-method functional interfaces, developers can pass lambdas directly without writing concrete strategy classes:
    ```java
    Collections.sort(users, (u1, u2) -> u1.getSalary() - u2.getSalary());
    ```

---

### Q96: What is the Observer Pattern, and how does it implement Event-Driven Architectures?
* **Publisher-Subscriber Mechanics**:
  * The **Observer Pattern** defines a one-to-many dependency between objects such that when the subject (publisher) changes state, all its registered observers (subscribers) are notified and updated automatically (`subject.notifyObservers(event)`).
  * Used extensively in Spring Application Events (`@EventListener`), GUI button click listeners, and reactive frameworks (RxJava, Project Reactor).

---

### Q97: What is the Chain of Responsibility Pattern? Give real-world examples.
* **Decoupled Sequential Processing**:
  * The **Chain of Responsibility Pattern** passes a request along a chain of potential handlers. Each handler decides either to process the request and pass it to the next handler, or to short-circuit and reject it.
  * **Real-World Examples**:
    * Servlet `FilterChain` (AuthFilter $\to$ LoggingFilter $\to$ RateLimitFilter $\to$ Servlet).
    * Spring Security Filter Chain.
    * Netty `ChannelPipeline` (`ChannelInboundHandler` chain).

---

### Q98: What is the Template Method Pattern vs Strategy Pattern?
* **Inheritance vs Composition**:
  * **Template Method (Behavioral - Class Level)**:
    * Defines the skeleton of an algorithm in an abstract base class (`abstract class DataMiner { public final void mine() { open(); parse(); close(); } }`).
    * Subclasses override specific step methods (`parse()`), but the overall workflow structure cannot be changed. Uses **Inheritance**.
  * **Strategy Pattern (Behavioral - Object Level)**:
    * Encapsulates entire interchangeable algorithms in standalone objects and injects them via constructor/setter. Uses **Composition**.

---

### Q99: What is the Flyweight Pattern, and how does the Java `Integer` Cache use it?
* **Memory Optimization via Object Sharing**:
  * The **Flyweight Pattern** minimizes memory usage by sharing as much common data as possible across large numbers of similar objects.
  * **Java `Integer` Cache Example**:
    * Autoboxing `Integer a = 100; Integer b = 100;` uses `Integer.valueOf(100)`.
    * The JVM pre-allocates and caches all `Integer` objects in the range $[-128, +127]$ in `IntegerCache.cache[]`.
    * Calling `Integer.valueOf(100)` returns a shared reference from the cache: `a == b` evaluates to `true`!
    * For numbers outside the range (e.g., `200`), new heap objects are allocated, so `Integer x = 200; Integer y = 200; x == y` evaluates to `false`!

---

### Q100: What is the Facade Pattern, and how does it promote loose coupling?
* **Unified Subsystem Interface**:
  * The **Facade Pattern** provides a simple, unified, high-level interface to a complex set of underlying subsystem classes (e.g., `OrderPlacementFacade` coordinates `InventoryService`, `PaymentService`, `NotificationService`, `ShippingService`).
  * Clients only talk to the facade, shielding them from the complexities and direct dependencies of the underlying subsystems.

---

### Q101: What is the Composite Pattern?
* **Tree-Structure Uniformity**:
  * The **Composite Pattern** composes objects into tree structures to represent part-whole hierarchies.
  * It allows clients to treat individual objects (leaves) and compositions of objects (containers/branches) uniformly through a shared interface (e.g., a File System where `File` and `Directory` both implement `FileSystemNode.getSize()`).

---

### Q102: What is the State Pattern vs Strategy Pattern?
* **Internal State vs External Interchangeability**:
  * **State Pattern**: The object's behavior changes dynamically based on its internal state (e.g., `VendingMachine` behaves differently in `NoCoinState`, `HasCoinState`, `SoldOutState`). The context object delegates to current state objects which can transition the context to other states.
  * **Strategy Pattern**: The client explicitly configures the context with a specific algorithm strategy, and the strategy rarely changes during the object's lifecycle.

---

### Q103: What is the Mediator Pattern, and how does it prevent Spaghetti Code?
* **Centralized Hub Communication**:
  * The **Mediator Pattern** restricts direct communications between components (colleagues) and forces them to collaborate only via a central mediator object.
  * **Analogy**: An Airport Air Traffic Control Tower. Airplanes do not communicate directly with each other to negotiate landing strips; they communicate solely with the tower, reducing connections from $O(N^2)$ to $O(N)$.

---

### Q104: What is the Memento Pattern, and how does it implement Undo/Redo?
* **State Capture without Encapsulation Breach**:
  * The **Memento Pattern** captures and externalizes an object's internal state so that the object can be restored to this state later (undo functionality), without violating encapsulation (private fields remain hidden).
  * Consists of: **Originator** (creates memento), **Memento** (immutable state snapshot), and **Caretaker** (manages history stack).

---

### Q105: What is the Command Pattern, and how does it support Queuing and Undoable Operations?
* **Encapsulating Method Invocations**:
  * The **Command Pattern** turns a request into a stand-alone object containing all information about the request (`receiver, action, parameters`).
  * Enables parameterizing clients with different requests, delaying or queuing a request's execution, logging transactions, and supporting undoable operations via `command.undo()`.

---

### Q106: What is the Bridge Pattern?
* **Decoupling Abstraction from Implementation**:
  * The **Bridge Pattern** splits a large class into two separate hierarchies: **Abstraction** (high-level control) and **Implementation** (low-level platform workers) that can be developed independently without combinatorial explosion of subclasses (e.g., `RemoteControl` abstraction bridged to `Device` implementation like `TV`, `Radio`).

---

### Q107: What is the Visitor Pattern, and how does Double Dispatch work?
* **Separating Algorithms from Data Structures**:
  * The **Visitor Pattern** allows adding new operations to existing object structures without modifying the structures themselves.
  * **Double Dispatch**: The client calls `element.accept(visitor)`, which in turn calls `visitor.visit(this)`, resolving the exact method at runtime based on both the element type and the visitor type.

---

### Q108: What is the Service Locator Pattern, and why is it considered an Anti-Pattern compared to Dependency Injection (DI)?
* **Hidden Dependencies**:
  * **Service Locator**: A central registry where classes manually query and look up dependencies (`ServiceLocator.get(PaymentGateway.class)`).
  * **Why it's an Anti-Pattern**: It hides class dependencies inside method bodies (black-box), makes unit testing difficult with mock objects, and couples classes to the global locator.
  * **Dependency Injection (Inversion of Control)**: Dependencies are explicitly passed into constructors (`public UserService(PaymentGateway gateway)`), making dependencies crystal clear and easily mockable.

---

### Q109: What is the Null Object Pattern?
* **Eliminating Defensive `null` Checks**:
  * Instead of returning `null` when a resource or handler is absent (forcing callers to write defensive `if (x != null)` checks), the system returns a **Null Object** that implements the target interface with neutral, no-op behavior (e.g., `NullLogger`, `Collections.emptyList()`).

---

### Q110: Design Patterns Master Classification Table

| Category | Patterns Included | Core Objective |
|---|---|---|
| **Creational** | Singleton, Factory Method, Abstract Factory, Builder, Prototype | Abstract the object instantiation process |
| **Structural** | Adapter, Bridge, Composite, Decorator, Facade, Flyweight, Proxy | Assemble classes and objects into larger structures |
| **Behavioral** | Chain of Responsibility, Command, Iterator, Mediator, Memento, Observer, State, Strategy, Template Method, Visitor | Manage algorithms, relationships, and responsibilities between objects |

---

# 🔬 Module 4: JVM Architecture, Memory Management & Garbage Collection

---

### Q111: Describe the JVM Runtime Data Areas. Differentiate between Thread-Shared and Thread-Private memory regions.
* **Architecture & Memory Partitioning**:
  * When a Java process runs, the HotSpot JVM divides system RAM into 5 distinct Runtime Data Areas:
  * **1. Thread-Shared Memory Areas (Shared across all threads)**:
    * **JVM Heap**: The largest memory region. Stores all instantiated class objects (`new Object()`), arrays, and instance variables. Managed and reclaimed exclusively by the Garbage Collector.
    * **Metaspace (Class Metadata)**: Replaced PermGen in Java 8. Allocated in native off-heap RAM. Stores loaded class bytecodes, method metadata, Constant Pool, annotations, and static variables.
  * **2. Thread-Private Memory Areas (Allocated per thread)**:
    * **JVM Stack**: Each thread has its own call stack. Every method invocation pushes a **Stack Frame** containing:
      * *Local Variable Table* (primitives: `int`, `long`, `double` and object heap references).
      * *Operand Stack* (intermediate bytecode computation work area).
      * *Frame Data / Dynamic Linking* (method return addresses and exception dispatch tables).
    * **Native Method Stack**: Manages C/C++ native JNI function call stack frames.
    * **Program Counter (PC) Register**: Stores the memory address of the JVM bytecode instruction currently being executed by that thread.

```
====================== JVM RUNTIME DATA AREAS ======================
 ┌───────────────────────────────────────────────────────────────┐
 │                     JVM HEAP (Thread-Shared)                  │
 │  ┌───────────────────────────────┐ ┌───────────────────────┐  │
 │  │ Young Gen (Eden, S0, S1)      │ │ Old Gen (Tenured)     │  │
 │  └───────────────────────────────┘ └───────────────────────┘  │
 ├───────────────────────────────────────────────────────────────┤
 │                   METASPACE (Native Off-Heap)                 │
 └───────────────────────────────────────────────────────────────┘
 ┌─────────────────────────┐ ┌─────────────────────────┐
 │  Thread 1 (Private)     │ │  Thread 2 (Private)     │
 │  - JVM Call Stack       │ │  - JVM Call Stack       │
 │  - Native Stack         │ │  - Native Stack         │
 │  - PC Register          │ │  - PC Register          │
 └─────────────────────────┘ └─────────────────────────┘
====================================================================
```

---

### Q112: What is the Weak Generational Hypothesis, and how does it drive JVM Generational Garbage Collection?
* **Statistical Invariant & Heap Division**:
  * The **Weak Generational Hypothesis** is an empirical statistical rule discovered across millions of enterprise Java programs:
    1. **Rule 1**: The vast majority of allocated objects die extremely young ($> 98\%$ of objects become garbage shortly after allocation inside their creating method).
    2. **Rule 2**: Objects that survive multiple GC cycles tend to live for a very long time (e.g., Spring singletons, connection pools, user sessions).
  * **How Heap is Divided**:
    * **Young Generation ($1/3$ of Heap)**: Optimized for ultra-fast allocation and cleanup of short-lived objects. Divided into:
      * **Eden Space ($80\%$)**: Where all new objects are born.
      * **Survivor From (`S0`, $10\%$)** & **Survivor To (`S1`, $10\%$)**: Copying buffers for survivors.
    * **Old (Tenured) Generation ($2/3$ of Heap)**: Stores long-lived survivors promoted from Young Gen, avoiding the overhead of repeatedly scanning them during Minor GCs.

---

### Q113: Trace the Complete Lifecycle of an Object from `new` to Garbage Collection.
* **Allocation Flow & Promotion Mechanics**:
  1. **TLAB (Thread-Local Allocation Buffer)**: To avoid multi-threaded synchronization locks on Eden, each thread has a private slice of Eden (TLAB). Small objects are allocated directly via pointer-bumping in $O(1)$ time.
  2. **Eden Space Fill & Minor GC**: When Eden fills up, a **Minor GC (Young GC)** is triggered (Stop-the-World pause, typically 1–10ms).
  3. **Survivor Copying**: The GC traces live objects using GC Roots. Surviving objects in Eden and `S0` are copied into `S1`, and their **GC Age counter** in the Mark Word is incremented (`age = 1`). Eden and `S0` are wiped clean in a single pass.
  4. **Survivor Role Swap**: In the next Minor GC, `S1` acts as the source and survivors are copied to `S0`.
  5. **Promotion to Old Gen (`MaxTenuringThreshold = 15`)**: When an object survives multiple Minor GCs and its age reaches the tenuring threshold (default: 15, or dynamic age threshold if a survivor space is $> 50\%$ full), it is promoted to the **Old Generation**.
  6. **Humongous Objects**: Extremely large objects (e.g., massive byte arrays) bypass Eden entirely and are allocated directly in Old Gen (`PretenureSizeThreshold`) to prevent expensive copying in survivor spaces.
  7. **Major / Full GC**: When Old Gen reaches capacity, a **Major GC / Full GC** cleans up Old Gen and Metaspace.

---

### Q114: What are GC Roots in Java Garbage Collection? What objects qualify as GC Roots?
* **Reachability Analysis Algorithm**:
  * Modern JVMs use **Reachability Analysis** instead of naive Reference Counting (which failed on circular references).
  * The GC traces a directed graph of references starting from authoritative root pointers called **GC Roots**. Any object that cannot be reached via an active chain of references from a GC Root is declared dead and reclaimed.
  * **The 4 Primary GC Roots**:
    1. **Local Variables & Parameters on Active Thread Call Stacks**: Variables currently referenced inside running method stack frames.
    2. **Active Java Threads**: Any live running `Thread` object.
    3. **Static Variables in Loaded Classes in Metaspace**: `public static final` fields.
    4. **JNI Native References**: C/C++ native pointers in JNI local and global tables.

---

### Q115: What is the difference between Strong, Soft, Weak, and Phantom References in Java?
* **Reference Strength & GC Reclamation Behavior**:
  * **1. Strong Reference (`Object o = new Object()`)**: Standard reference. The object will **NEVER be garbage collected**, even if the JVM runs out of memory and throws `OutOfMemoryError`.
  * **2. Soft Reference (`SoftReference<T>`)**: The object is kept alive during normal GCs, but is **guaranteed to be reclaimed by the GC before throwing an OutOfMemoryError**. Used for memory-sensitive in-memory image/document caches.
  * **3. Weak Reference (`WeakReference<T>`)**: The object is **eagerly reclaimed during the very next GC cycle**, regardless of available free heap memory. Used in `WeakHashMap` and `ThreadLocalMap`.
  * **4. Phantom Reference (`PhantomReference<T>`)**: `get()` always returns `null`. Used with a `ReferenceQueue` to perform pre-mortem off-heap native resource cleanup (replacing deprecated `finalize()`).

---

### Q116: How does G1 GC (Garbage-First) work under the hood? Explain Regions, SATB, and Remembered Sets.
* **Region-Based Compacting GC Architecture**:
  * **1. Region Partitioning**:
    * G1 divides the entire JVM Heap into $2048$ equal-sized independent memory blocks called **Regions** (ranging from $1\text{ MB}$ to $32\text{ MB}$).
    * Regions are dynamically assigned roles: **Eden (E)**, **Survivor (S)**, **Old (O)**, or **Humongous (H)** (for objects $> 50\%$ region size).
  * **2. Predictable Pause Time Model (`-XX:MaxGCPauseMillis=200`)**:
    * G1 tracks garbage density and reclamation costs across all regions.
    * During each pause, it prioritizes collecting the regions containing the **most garbage first** (hence *"Garbage-First"*), guaranteeing pause time constraints.
  * **3. Remembered Sets (RSet) & Card Tables**:
    * To collect a Young region without scanning the entire Old Generation to check for incoming pointers, each region maintains an **RSet** recording which Old Gen cards hold pointers into this region.
  * **4. Snapshot-At-The-Beginning (SATB) Concurrent Marking**:
    * Allows G1 to mark live objects concurrently while application threads are running without stopping the world.
    * If an application thread overwrites a reference during marking, a write barrier intercepts the write and saves the old reference to an SATB buffer, ensuring no live objects are missed.

```
====================== G1 GC REGION-BASED HEAP ======================
 [ E ] [ O ] [ E ] [ S ] [ O ] [ Free ] [ H ] [ E ] [ O ] [ S ]
 [ O ] [ Free ] [ E ] [ O ] [ E ] [ S ] [ Free ] [ O ] [ H ] [ E ]
=====================================================================
```

---

### Q117: How does ZGC (Z Garbage Collector) achieve sub-millisecond pauses on Terabyte Heaps?
* **Colored Pointers & Load Barriers**:
  * **ZGC** is a scalable, low-latency concurrent garbage collector designed for heaps ranging from $16\text{ MB}$ to $16\text{ TB}$.
  * **Max GC Pause Time**: Strictly **$< 1\text{ millisecond}$**, independent of heap size!
  * **Core Innovations**:
    1. **Colored Pointers (Reference Metadata)**:
       * ZGC stores GC metadata directly inside the unused high bits of 64-bit reference pointers:
       * Bits 0–41 ($4\text{ TB}$ address space), Bit 42 (`Marked0`), Bit 43 (`Marked1`), Bit 44 (`Remapped`).
    2. **Load Barriers (Self-Healing Pointers)**:
       * In ZGC, object relocation/compaction is done **concurrently with running application threads**.
       * When an application thread dereferences an object pointer (`user.getName()`), a tiny **Load Barrier** CPU check tests if the pointer's colored bits are valid.
       * If the object has been relocated to a new memory address, the load barrier intercepts the read, updates the reference to the new address (**Self-Healing**), and returns the object in nanoseconds without any Stop-the-World pause!

```
====================== ZGC 64-BIT COLORED POINTER ======================
[ 16 Bits: Unused ] [ 1 Bit: Finalizable ] [ 1 Bit: Remapped ] [ 1 Bit: Marked1 ] [ 1 Bit: Marked0 ] [ 44 Bits: Object Address (16TB) ]
========================================================================
```

---

### Q118: What is the difference between Minor GC, Major GC, and Full GC?
* **Scope & Latency Impact**:
  * **Minor GC (Young GC)**: Cleans only the Young Generation (Eden + Survivor). Extremely fast ($1\text{--}10\text{ ms}$).
  * **Major GC**: Cleans specifically the Old Generation.
  * **Full GC**: Cleans the **entire JVM process** (Young Gen, Old Gen, and Metaspace) and performs full heap compaction. Causes severe Stop-the-World pauses ($100\text{ ms}$ to several seconds), causing client request timeouts and microservice lag spikes.

---

### Q119: What causes `java.lang.OutOfMemoryError: Java heap space`, and how do you troubleshoot it?
* **Root Cause & Production Troubleshooting**:
  * **Causes**:
    1. **Memory Leak**: Application unintentionally holds strong references to dead objects (e.g., static collections, unevicted caches, unremoved `ThreadLocal` variables).
    2. **Undersized Heap**: Application legitimate memory requirements exceed `-Xmx`.
    3. **Sudden Spike**: A single endpoint queries 10 million database records into memory at once.
  * **Step-by-Step Production Diagnosis**:
    1. Enable JVM flags: `-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/var/dumps/heap.hprof`.
    2. Open the `.hprof` dump file in **Eclipse Memory Analyzer Tool (MAT)** or **IntelliJ Profiler**.
    3. Run the **Leak Suspects Report** and inspect the **Dominator Tree**.
    4. Identify the class consuming the largest **Retained Heap** and trace its **Shortest Paths to GC Roots** to find the offending static variable or leak source.

---

### Q120: What causes `java.lang.OutOfMemoryError: Metaspace`, and how do you resolve it?
* **Class Metadata Exhaustion**:
  * **Causes**:
    * Metaspace stores loaded class definitions.
    * Dynamic class generation libraries (CGLIB proxies, Javassist, Spring AOP, dynamic Groovy/JavaScript scripts) repeatedly generating new synthetic classes without unloading old `ClassLoader`s.
  * **Resolution**:
    * Increase Metaspace ceiling: `-XX:MaxMetaspaceSize=512m`.
    * Inspect class generation caches in Spring/Hibernate to ensure proxy classes are reused.
    * Check for ClassLoader memory leaks using `jcmd <PID> VM.classloader_stats`.

---

### Q121: What causes `java.lang.OutOfMemoryError: Direct buffer memory`?
* **Off-Heap Memory Leak**:
  * **Cause**: High-throughput network frameworks (like Netty, Kafka client, gRPC) allocate off-heap native memory using `ByteBuffer.allocateDirect()`.
  * If native direct memory exceeds `-XX:MaxDirectMemorySize` (defaults to `-Xmx`), the JVM throws this error.
  * **Troubleshooting**: Check for leaked Netty `ByteBuf`s where `ReferenceCountUtil.release(buf)` was omitted in exception pathways.

---

### Q122: What causes `java.lang.OutOfMemoryError: unable to create new native thread`?
* **Operating System Thread Limit**:
  * **Causes**:
    1. Application spawns too many OS threads (e.g., unbounded `newCachedThreadPool()`).
    2. Linux OS process thread ceiling reached (`ulimit -u` max user processes).
    3. Host OS 32-bit/64-bit virtual memory address space exhausted.
  * **Resolution**:
    * Switch unbounded thread pools to custom bounded `ThreadPoolExecutor`.
    * In Java 21, migrate I/O-bound tasks to **Virtual Threads** (`Executors.newVirtualThreadPerTaskExecutor()`).
    * Increase OS thread limit via `ulimit -u 65535` in `/etc/security/limits.conf`.

---

### Q123: What is the difference between `Shallow Heap` and `Retained Heap` in Memory Profiling?
* **Memory Metrics in Profilers**:
  * **Shallow Heap**: The exact memory consumed by the object itself (its object header + primitive field sizes + reference pointer sizes). It does **NOT** include the memory of referenced child objects.
  * **Retained Heap**: The total heap memory that would be **freed if this object were garbage collected**. It equals the object's shallow heap plus the shallow heap of all objects that are reachable *only* through this object.

---

### Q124: What is Escape Analysis, and how does the JIT Compiler optimize object allocations?
* **Scalar Replacement & Stack Allocation**:
  * **Escape Analysis**: A HotSpot JIT optimization that analyzes whether an object created inside a method escapes the method scope (e.g., returned to caller, passed to another thread, or assigned to a field).
  * **3 JIT Optimizations Triggered by Escape Analysis**:
    1. **Scalar Replacement**: If an object does not escape, the JIT decomposes the object into its individual primitive fields and stores them directly in CPU registers or stack memory without allocating heap memory!
    2. **Lock Elision**: If a lock object is created locally and never escapes the creating thread, the JIT compiler eliminates the synchronization locks completely.
    3. **Stack Allocation**: Allocates object stack frames that are reclaimed instantly on method exit without GC.

---

### Q125: How does the CMS (Concurrent Mark Sweep) Collector work, and why was it deprecated and removed in Java 14?
* **CMS Multi-Phase Lifecycle & Fatal Flaws**:
  * **CMS 4 Phases**: (1) Initial Mark (STW), (2) Concurrent Mark, (3) Remark (STW), (4) Concurrent Sweep.
  * **Why CMS Was Removed**:
    1. **Memory Fragmentation**: CMS is a non-compacting collector. It uses free-lists, creating swiss-cheese memory fragmentation. When a large object cannot find a contiguous block, CMS triggers a catastrophic single-threaded **Full GC STW pause** lasting tens of seconds!
    2. **Floating Garbage**: Garbage generated during Concurrent Sweep cannot be collected until the next cycle.
    3. High CPU consumption contending with application threads. Replaced by G1 and ZGC.

---

### Q126: What is a SafePoint in the JVM, and why do long SafePoint pauses cause latency spikes?
* **JVM Thread Suspension Rendezvous**:
  * A **SafePoint** is a point during bytecode execution where the execution state of all threads is completely known and consistent.
  * The JVM brings all threads to a SafePoint to perform GC root scanning, thread dumps (`jstack`), or code deoptimization.
  * **Latency Hazard (SafePoint Bias)**: If 99 threads reach a SafePoint instantly, but 1 thread is stuck in an uncounted counted loop (e.g., `for (int i=0; i<Integer.MAX_VALUE; i++)`), all 99 threads are blocked waiting for the single slow thread to poll the safepoint, causing huge latency spikes. Enabled via `-XX:+UseCountedLoopSafepoints`.

---

### Q127: What is the String Table (String Pool) in Java, and where is it located in memory?
* **Interning & Metaspace Relocation**:
  * The **String Pool** is a native C++ hash table (`StringTable`) storing unique string literal instances.
  * In Java 6, it was stored in PermGen (prone to `OutOfMemoryError: PermGen space`).
  * Since **Java 7+**, the String Pool is located directly inside the **Java Heap**, allowing unused interned strings to be garbage collected naturally.
  * Calling `string.intern()` returns the canonical instance from the pool if present; otherwise adds the string to the pool.

---

### Q128: What is Compressed OOPs (`-XX:+UseCompressedOops`) in 64-bit JVMs?
* **Pointer Compression Math**:
  * In 64-bit JVMs, 64-bit pointers consume 8 bytes instead of 4 bytes, wasting $30\text{--}40\%$ extra heap space and cache capacity.
  * **Compressed OOPs (Ordinary Object Pointers)**:
    * Since all JVM objects are aligned to 8-byte boundaries, the lowest 3 bits of any object address are always `000`.
    * The JVM shifts 32-bit pointers right by 3 bits (`address >> 3`) when storing in memory, and shifts left (`address << 3`) when dereferencing.
    * This allows a 32-bit pointer to address up to **$32\text{ GB}$ of heap memory** ($2^{32} \times 8 = 32\text{ GB}$) with 4-byte pointer efficiency!
    * *Rule of Thumb*: A heap size of $31\text{ GB}$ often holds MORE objects than a heap size of $33\text{ GB}$ because exceeding 32GB disables Compressed OOPs!

---

### Q129: What is the difference between `-Xms` and `-Xmx`, and why should they be set equal in production?
* **Heap Expansion Overhead**:
  * `-Xms`: Initial heap size allocated at JVM startup.
  * `-Xmx`: Maximum allowable heap size.
  * **Why Set `-Xms == -Xmx` in Production**:
    * If `-Xms < -Xmx`, whenever the heap fills up, the JVM pauses application threads to request additional memory blocks from the OS kernel and re-adjust generational boundaries.
    * Setting `-Xms == -Xmx` pre-allocates the entire heap at startup, completely eliminating heap resizing pauses and virtual memory fragmentation.

---

### Q130: What is the ClassLoader Delegation Hierarchy in Java? Explain the Parent-First Delegation Model.
* **Parent-First Delegation Protocol**:
  * When a ClassLoader is requested to load a class, it follows the **Parent-First Delegation Principle**:
    1. Check if the class is already loaded in local cache.
    2. Delegate the loading request upward to its **Parent ClassLoader**.
    3. Only if the parent ClassLoader fails (throws `ClassNotFoundException`) does the child ClassLoader attempt to find and load the class from its own classpath.
  * **Hierarchy**:
    * **Bootstrap ClassLoader** (Native C++ / `lib/modules` - loads `java.lang.*`, `java.util.*`).
    * $\uparrow$ **Platform / Extension ClassLoader** (loads Java SE platform extensions).
    * $\uparrow$ **Application / System ClassLoader** (loads application classpath / JARs).
    * $\uparrow$ **Custom ClassLoaders** (Tomcat webapps, OSGi plugins).
  * **Why**: Prevents untrusted third-party code from overriding core Java security classes (e.g., creating a malicious custom `java.lang.String`).

---

### Q131: What is the difference between `ClassNotFoundException` and `NoClassDefFoundError`?
* **Dynamic Loading vs Linkage Failure**:
  * **`ClassNotFoundException` (Checked Exception)**: Occurs at runtime when an application attempts to load a class by name using string reflection (`Class.forName("com.User")`, `ClassLoader.loadClass()`) and the class is not on the classpath.
  * **`NoClassDefFoundError` (Unchecked Error)**: Occurs when the class was present at **compile-time**, but at runtime the compiled class cannot be found or failed during static `<clinit>` initialization.

---

### Q132: What is the Just-In-Time (JIT) Compiler, and what is Tiered Compilation in HotSpot?
* **Bytecode Interpretation vs Native Machine Code**:
  * The JVM starts executing bytecode using a fast **Interpreter**.
  * As methods are executed repeatedly, the JVM profiles execution counters (method invocations and loop iterations).
  * **Tiered Compilation (`-XX:+TieredCompilation`)**:
    * **Tier 0**: Pure Bytecode Interpreter.
    * **Tier 1–3 (C1 / Client Compiler)**: Fast JIT compilation with basic profiling, producing optimized machine code quickly.
    * **Tier 4 (C2 / Server Compiler)**: Heavyweight optimizing JIT compiler that performs aggressive optimizations (Inlining, Escape Analysis, Loop Unrolling, Vectorization/SIMD) to produce maximum performance machine code for "hot" methods.

---

### Q133: What is Method Inlining in JIT Compilation?
* **Call Overhead Elimination**:
  * **Inlining**: The JIT compiler replaces a method invocation call site directly with the body of the target method.
  * **Benefits**:
    1. Eliminates CPU stack frame creation, parameter passing, and return branch instructions (~5–10ns savings per call).
    2. Enables subsequent downstream optimizations like constant folding and dead-code elimination across method boundaries.

---

### Q134: How do you capture and analyze a Thread Dump to diagnose High CPU Usage (100% CPU Spike)?
* **Step-by-Step Linux / JVM CPU Diagnosis**:
  1. Find the Java Process ID: `top` (identify process using high CPU, e.g., PID `12345`).
  2. Find the exact native OS thread consuming CPU: `top -H -p 12345` (identify thread ID, e.g., TID `12360`).
  3. Convert the TID to Hexadecimal: `printf "%x\n" 12360` $\to$ `0x3048`.
  4. Generate JVM Thread Dump: `jstack 12345 > threads.tdump`.
  5. Search thread dump for `nid=0x3048`: Locate the exact method name and line of code executing the CPU-consuming infinite loop or regex evaluation!

---

### Q135: JVM Essential Tuning Flags Quick Reference Matrix

| JVM Flag | Purpose | Recommended Production Setting |
|---|---|---|
| **`-Xms` / `-Xmx`** | Set Initial and Maximum Heap Size | Set equal: `-Xms8g -Xmx8g` |
| **`-XX:+UseG1GC`** | Enable G1 Garbage Collector | Default in Java 9–21 |
| **`-XX:+UseZGC`** | Enable Low-Latency Z Garbage Collector | For sub-ms pause SLAs: `-XX:+UseZGC` |
| **`-XX:MaxGCPauseMillis`** | Target GC pause time SLA | `-XX:MaxGCPauseMillis=200` |
| **`-XX:+HeapDumpOnOutOfMemoryError`** | Automatically dump heap on OOM | Mandatory for production debugging |
| **`-Xlog:gc*`** | Unified GC logging in Java 9+ | `-Xlog:gc*:file=/var/log/gc.log:time,uptime,pid:filecount=5,filesize=100M` |

---

# 🚀 Module 5: Modern Java (8–21), Streams, Generics & SOLID Principles

---

### Q136: How does the Java 8 Stream API work internally under the hood? Explain Pipeline Fusing and the `Sink` Chaining Model.
* **Internal Mechanics & Pipeline Construction**:
  * A Java `Stream` is NOT a data structure; it is a pipeline of computational stages that processes elements on-demand.
  * **1. Structure of a Stream Pipeline**:
    * **Source Stage**: Wraps the underlying collection or array in a `Spliterator`.
    * **Intermediate Stages (`map`, `filter`, `flatMap`)**: Return a new `ReferencePipeline` stage. They are **Lazy** and execute zero bytecode until a terminal operation is called.
    * **Terminal Stage (`collect`, `forEach`, `reduce`)**: Triggers evaluation.
  * **2. The `Sink` Chaining Model & Pipeline Fusing**:
    * When a terminal operation is invoked, the Stream engine traverses backwards through the pipeline stages to construct a linked chain of **`Sink<T>`** objects (`Sink.ChainedReference`).
    * Each `Sink` defines 3 lifecycle methods: `begin(size)`, `accept(element)`, and `cancellationRequested()`.
    * **Loop Fusion Optimization**: The JVM does NOT create intermediate collections between `filter()` and `map()`. Instead, each element flows sequentially through the entire chain of `Sink.accept()` calls in a single pass (`O(1)` memory overhead!).

```
====================== STREAM PIPELINE SINK CHAINING ======================
Stream: list.stream().filter(x -> x > 10).map(x -> x * 2).collect(toList());

Execution Flow:
Element '12' -> [ FilterSink.accept(12) ] (12 > 10? Yes)
                     |
                [ MapSink.accept(12) ] (12 * 2 = 24)
                     |
                [ CollectingSink.accept(24) ] (Appends to ArrayList)
==========================================================================
```

---

### Q137: What is the difference between Stateless and Stateful Intermediate Operations in Streams?
* **Memory & Performance Characteristics**:
  * **Stateless Operations (`filter`, `map`, `peek`, `flatMap`)**:
    * Process each element independently without retaining information about previous elements.
    * Elements flow continuously through the pipeline with $O(1)$ auxiliary memory.
  * **Stateful Operations (`sorted`, `distinct`, `limit`, `skip`)**:
    * Require knowledge of previous or all elements before passing them downstream.
    * For example, `stream.sorted()` must buffer and store **every single element in memory** before sorting and emitting the first element, breaking streaming memory benefits on unbounded infinite streams!

---

### Q138: How does `Spliterator` work, and how does it power `parallelStream()`?
* **Parallel Deconstruction**:
  * A `Spliterator` (Split-and-Iterate) is the foundational engine behind Java Collections and Streams.
  * **Core Methods**:
    * `tryAdvance(Consumer)`: Consumes the next element sequentially (like `Iterator.next()`).
    * `trySplit()`: Divides the current range into two halves. Returns a new `Spliterator` covering the first half, while the current `Spliterator` adjusts its range to cover the remaining half.
  * In a `parallelStream()`, the JVM recursively calls `trySplit()` to decompose the collection into sub-ranges and submits them as `ForkJoinTask`s to `ForkJoinPool.commonPool()`.

---

### Q139: Why does using `parallelStream()` for I/O operations cause system-wide performance degradation?
* **Global Common Pool Starvation**:
  * By default, `parallelStream()` executes all tasks on the global, JVM-wide **`ForkJoinPool.commonPool()`**, which is sized strictly to `Runtime.getRuntime().availableProcessors() - 1` (e.g., 7 threads on an 8-core CPU).
  * If a developer uses `parallelStream()` to make slow blocking HTTP calls or database queries, all 7 worker threads become blocked waiting for network I/O.
  * **Impact**: Because `commonPool()` is shared globally across the entire JVM process, other unrelated services (like parallel garbage collectors, background async tasks, and parallel computations) are completely starved of threads and freeze!
  * **Rule**: Always use custom `ExecutorService` thread pools for I/O tasks.

---

### Q140: What is the difference between `Optional.orElse()` and `Optional.orElseGet()`?
* **Eager vs Lazy Evaluation**:
  * **`optional.orElse(computeDefault())`**:
    * **Eagerly evaluates** `computeDefault()` *even if the Optional contains a valid non-null value*!
    * If `computeDefault()` makes an expensive database call or allocates memory, it wastes CPU cycles and resources unnecessarily.
  * **`optional.orElseGet(() -> computeDefault())`**:
    * **Lazily evaluates** the Supplier lambda *only and strictly when the Optional is empty*.
  * **`orElseThrow()`**: Best practice in Java 10+ to unpack values or throw descriptive domain exceptions.

---

### Q141: What are the 4 core Functional Interfaces in Java 8, and what are their method signatures?
* **Core Functional Interface Matrix**:

| Interface | Method Signature | Input Parameters | Return Type | Typical Use Case |
|---|---|---|---|---|
| **`Function<T, R>`** | `R apply(T t)` | 1 (`T`) | `R` | Transformation (`map(user -> user.getName())`) |
| **`Predicate<T>`** | `boolean test(T t)` | 1 (`T`) | `boolean` | Filtering (`filter(user -> user.isActive())`) |
| **`Consumer<T>`** | `void accept(T t)` | 1 (`T`) | `void` | Side effects (`forEach(System.out::println)`) |
| **`Supplier<T>`** | `T get()` | 0 | `T` | Factory instantiation / Lazy evaluation |
| **`BiFunction<T, U, R>`** | `R apply(T t, U u)`| 2 (`T, U`) | `R` | Combining two inputs into a result |

---

### Q142: How does Java Generics Type Erasure work under the hood? What are Synthetic Bridge Methods?
* **Compile-Time Erasure & Bridge Methods**:
  * Java Generics were introduced in Java 5 with **Backward Compatibility** for legacy pre-Java 5 bytecodes.
  * **Type Erasure**: At compile time, the Java compiler strips all generic type parameters (`<T>`) and replaces them with their bounding type (e.g., `Object` for `<T>`, or `Comparable` for `<T extends Comparable>`), inserting explicit type casts at call sites.
  * **Synthetic Bridge Methods**:
    * When a subclass overrides a generic method from a parent class with a concrete type:
    ```java
    public class Node<T> { public void set(T data) {} }
    public class StringNode extends Node<String> {
        @Override public void set(String data) {}
    }
    ```
    * Because `Node.set` compiles to `set(Object data)`, polymorphism would break if `StringNode` only defined `set(String data)`.
    * The compiler automatically synthesizes a hidden **Bridge Method** in `StringNode`:
      `public void set(Object data) { set((String) data); }` to preserve dynamic method dispatch!

---

### Q143: Explain the PECS Rule (Producer Extends, Consumer Super) in Java Generics.
* **Covariance vs Contravariance Invariance**:
  * In Java, generic types are **invariant** (`List<String>` is NOT a subtype of `List<Object>`).
  * **PECS Principle**:
    1. **Producer Extends (`? extends T`, Covariance)**:
       * Use when you only **READ** data out of a collection (`T item = list.get()`).
       * You CANNOT write/add elements to a `List<? extends Number>` (except `null`) because the compiler cannot guarantee the concrete subtype.
    2. **Consumer Super (`? super T`, Contravariance)**:
       * Use when you only **WRITE** data into a collection (`list.add(item)`).
       * You can safely add instances of `T` (or subtypes) into `List<? super Integer>`.
  * **Classic Example (`Collections.copy`)**:
    ```java
    public static <T> void copy(List<? super T> dest, List<? extends T> src) {
        for (int i = 0; i < src.size(); i++) dest.set(i, src.get(i));
    }
    ```

---

### Q144: What are Java Records (Java 14–16 LTS), and how do they differ from standard classes?
* **Immutable Data Carrier Mechanics**:
  * A `record` is a compact, transparent, immutable data carrier:
    ```java
    public record UserDto(Long id, String username, String email) {}
    ```
  * **Compiler-Generated Invariants**:
    1. Generates `private final` fields for all record components.
    2. Generates canonical constructor, public accessor methods (`id()`, `username()`), `equals()`, `hashCode()`, and `toString()`.
    3. The class is implicitly `final` and extends `java.lang.Record` (cannot extend any other class).
    4. Supports **Compact Constructors** for parameter validation:
       ```java
       public record UserDto {
           public UserDto {
               Objects.requireNonNull(username, "Username cannot be null");
           }
       }
       ```

---

### Q145: What are Sealed Classes and Interfaces (Java 17 LTS)?
* **Exhaustive Domain Hierarchy Control**:
  * **Sealed Classes (`sealed`, `permits`)** restrict which other classes or interfaces can extend or implement them:
    ```java
    public sealed interface PaymentStatus permits Success, Failed, Pending {}
    public final class Success implements PaymentStatus {}
    public final class Failed implements PaymentStatus {}
    public final class Pending implements PaymentStatus {}
    ```
  * **Permitted Subclasses MUST be**: `final`, `sealed`, or `non-sealed`.
  * **Compiler Exhaustiveness in Pattern Matching**: Enables exhaustive `switch` expressions without needing a dummy `default:` branch:
    ```java
    String message = switch (status) {
        case Success s -> "Payment successful";
        case Failed f -> "Payment failed";
        case Pending p -> "Payment in progress";
    };
    ```

---

### Q146: What is Pattern Matching for `switch` and `instanceof` (Java 17–21 LTS)?
* **Type-Testing & Destructuring**:
  * Eliminates verbose explicit type casting:
    ```java
    // Java 17 Pattern Matching for instanceof:
    if (obj instanceof String s && s.length() > 5) {
        System.out.println(s.toUpperCase());
    }

    // Java 21 Pattern Matching for switch with Guard Clauses (when):
    static String formatValue(Object obj) {
        return switch (obj) {
            case Integer i -> String.format("Integer: %d", i);
            case String s when s.isBlank() -> "Empty string";
            case String s -> String.format("String: %s", s);
            case UserDto(Long id, String name, _) -> "User: " + name; // Record Pattern
            case null -> "Null value";
            default -> obj.toString();
        };
    }
    ```

---

### Q147: What are Sequenced Collections in Java 21?
* **Unified Ordering APIs**:
  * Prior to Java 21, accessing first/last elements across `List`, `Deque`, and `SortedSet` had fragmented, inconsistent APIs (`list.get(0)`, `deque.getFirst()`, `sortedSet.first()`).
  * Java 21 introduces `SequencedCollection`, `SequencedSet`, and `SequencedMap`:
    * Uniform methods: `getFirst()`, `getLast()`, `addFirst()`, `addLast()`, `removeFirst()`, `removeLast()`, and `reversed()` (returns a live reverse-ordered view in $O(1)$ time).

---

### Q148: What are the 5 SOLID Principles? Give practical Java refactoring examples.
* **Architecture & Clean Code Principles**:
  * **S - Single Responsibility Principle (SRP)**: A class should have only one reason to change.
    * *Anti-pattern*: `OrderService` calculates totals, saves to DB, and sends email notifications.
    * *Refactor*: Split into `OrderService`, `OrderRepository`, and `EmailNotificationService`.
  * **O - Open/Closed Principle (OCP)**: Software entities should be open for extension, but closed for modification.
    * *Refactor*: Use interfaces/Strategy pattern (`DiscountStrategy`) instead of `if-else` cascades on customer types.
  * **L - Liskov Substitution Principle (LSP)**: Subtypes must be substitutable for their base types without altering program correctness.
    * *Anti-pattern*: `Square` extends `Rectangle` and breaks width/height setting expectations.
  * **I - Interface Segregation Principle (ISP)**: Clients should not be forced to depend on methods they do not use.
    * *Refactor*: Break fat interfaces (`Worker` with `work()`, `eat()`, `sleep()`) into smaller cohesive interfaces (`Workable`, `Eatable`).
  * **D - Dependency Inversion Principle (DIP)**: High-level modules should not depend on low-level modules; both should depend on abstractions.
    * *Refactor*: `UserService` depends on `UserRepository` interface, not concrete `MySQLUserRepository`.

---

### Q149: What is the difference between `default` methods in Java 8 interfaces and `abstract` methods? How is the Diamond Problem resolved?
* **Interface Evolution & Multiple Inheritance Rules**:
  * **`default` Methods**: Provide a concrete method implementation directly inside an interface (`default void log() { ... }`), enabling library developers to add new methods to existing interfaces without breaking backwards compatibility with existing implementing classes.
  * **Resolving Interface Diamond Problem Conflicts**:
    * If class `C` implements interfaces `A` and `B` which both define the exact same `default void foo()`:
    * The compiler flags a compilation error.
    * Class `C` **MUST explicitly override** the method and resolve the conflict manually:
      ```java
      public class C implements A, B {
          @Override
          public void foo() {
              A.super.foo(); // Explicitly delegates to interface A's implementation
          }
      }
      ```

---

### Q150: What are Scoped Values in Java 21 (Preview)? How do they replace `ThreadLocal`?
* **Lightweight Immutable Context Propagation**:
  * `ThreadLocal` has severe overhead when used with millions of Virtual Threads (mutability, memory leaks if `remove()` is omitted, inheritance cloning costs).
  * **Scoped Values (`ScopedValue<T>`)**:
    * Immutable and bound to a specific lexical execution scope:
      ```java
      private static final ScopedValue<UserContext> USER = ScopedValue.newInstance();

      ScopedValue.where(USER, loggedInUser).run(() -> {
          handleRequest(); // Read USER.get() anywhere inside this block!
      });
      ```
    * Automatically cleaned up on scope exit without memory leaks, and shared efficiently across child virtual threads without memory duplication.

---

### Q151: What is the difference between String `+` concatenation, `StringBuilder`, and `StringBuffer`?
* **Bytecode Compilation & Thread Safety**:
  * **`String +`**: In Java 9+, string concatenation uses `invokedynamic` (StringConcatFactory) which optimizes byte array allocation at runtime. In loops, however, `+` creates new `StringBuilder` instances per iteration.
  * **`StringBuilder`**: Mutable character buffer. **Not thread-safe**; fastest performance for single-threaded string manipulations.
  * **`StringBuffer`**: Thread-safe mutable character buffer. Every method contains `synchronized`, introducing synchronization lock overhead.

---

### Q152: What is the difference between `Checked` and `Unchecked` Exceptions in Java?
* **Compile-Time Enforcement**:
  * **Checked Exceptions (extends `java.lang.Exception`, excluding `RuntimeException`)**:
    * Enforced at compile-time. Caller must either handle via `try-catch` or declare in `throws` clause (e.g., `IOException`, `SQLException`). Used for recoverable conditions.
  * **Unchecked Exceptions (extends `java.lang.RuntimeException` or `java.lang.Error`)**:
    * Not checked at compile-time (e.g., `NullPointerException`, `IllegalArgumentException`). Used for programming errors and fatal system faults.

---

### Q153: How does `try-with-resources` work under the hood? What is `AutoCloseable`?
* **Automated Exception Suppression**:
  * Any class implementing `java.lang.AutoCloseable` can be used inside `try (Resource r = new Resource()) { ... }`.
  * The Java compiler automatically emits a `finally` block that invokes `r.close()`.
  * **Suppressed Exceptions**: If both the `try` block and the `close()` method throw exceptions, the compiler attaches the `close()` exception to the primary exception via **`primaryException.addSuppressed(closeException)`**, ensuring no root-cause stack traces are lost.

---

### Q154: What is the Java Module System (JPMS / Project Jigsaw) introduced in Java 9?
* **Strong Encapsulation & Reliable Configuration**:
  * Encapsulates packages inside named modules declared in `module-info.java`:
    ```java
    module com.app.orders {
        requires com.app.payments;
        exports com.app.orders.api; // Internal implementation packages remain hidden!
    }
    ```
  * **Benefits**:
    1. Strong encapsulation: Non-exported packages are invisible to other modules, even via reflection!
    2. Reliable dependencies: Prevents classpath hell and missing JARs at startup.
    3. Modular JVM runtime images via `jlink` (creates minimal 30MB custom JVM runtimes for Docker containers).

---

### Q155: Summary Matrix: Java 8 to 21 Evolution

| Java Version | Release Year | Flagship Features |
|---|---|---|
| **Java 8 LTS** | 2014 | Lambdas, Stream API, Functional Interfaces, CompletableFuture, Optional |
| **Java 9** | 2017 | JPMS Modules (Project Jigsaw), JShell REPL, Private interface methods |
| **Java 11 LTS** | 2018 | `var` in lambdas, HttpClient (HTTP/2), ZGC (Experimental), String methods |
| **Java 17 LTS** | 2021 | Sealed Classes, Records, Pattern Matching for `switch`, Mac/AArch64 port |
| **Java 21 LTS** | 2023 | **Virtual Threads (Project Loom)**, Sequenced Collections, Record Patterns |

---

# 🏭 Module 6: Real-World Scenario-Based System & Production Debugging

---

### Q156: Scenario: How do you design an Idempotent Payment Processing System in Java to prevent duplicate customer charges during network timeouts?
* **Problem & Production Architecture**:
  * **Scenario**: A user clicks "Pay $100". The backend charges the bank, but a network blip occurs before the HTTP 200 response reaches the client browser. The browser automatically retries or the user clicks "Pay" again, sending a duplicate charge request!
  * **3-Tier Idempotency Solution**:
    1. **Unique Idempotency Key**: The client generates a unique UUID `Idempotency-Key` (e.g., `req_987413abc`) sent in HTTP headers.
    2. **Distributed Atomic Lock (Redis / DB)**:
       * When the request hits the Java service, it attempts an atomic Redis `SET payment:lock:req_987413abc "PROCESSING" EX 300 NX`.
       * If `SETNX` returns `false`, another thread or pod is currently processing the payment. Return HTTP 409 Conflict or poll for the result.
    3. **Database Unique Constraint**: Insert a payment record with `UNIQUE (idempotency_key)`. If two requests bypass cache, the second triggers a database `DataIntegrityViolationException`, safely rolling back without double-charging.
    4. **Result Caching**: Store the final payment outcome in Redis against the key for 24 hours. Subsequent duplicate requests immediately return the cached response.

---

### Q157: Scenario: How do you design a High-Throughput Thread-Safe In-Memory Cache with TTL and Sliding Expiration in Java?
* **Problem & Architecture**:
  * **Requirements**: Concurrent access, $O(1)$ reads/writes, automatic removal of expired keys without full-table polling loops.
  * **Internal Architecture**:
    1. **Storage**: `ConcurrentHashMap<K, CacheEntry<V>>` for $O(1)$ lock-free concurrent reads and bucket-locked writes.
    2. **Expiration Schedule**: `DelayQueue<DelayedKey<K>>` storing keys sorted by expiration timestamp.
    3. **Background Cleanup Worker**: A single daemon thread continuously calls `delayQueue.take()` (which blocks until the earliest key expires) and removes the expired entry from the `ConcurrentHashMap`.
    4. **Sliding Expiration**: When `get(key)` is called, update `entry.lastAccessedTime = now()` and offer an updated `DelayedKey` to the `DelayQueue`.

---

### Q158: Scenario: How do you prevent Flash Sale Inventory Overselling (Race Conditions) under 100,000 requests/second?
* **Problem & Trade-Off Analysis**:
  * **The Overselling Hazard**: Multiple concurrent threads execute `if (inventory > 0) { inventory = inventory - 1; }`. Since checking and decrementing is not atomic, inventory drops below zero.
  * **3 Production Solutions**:
    1. **Pessimistic Locking (`SELECT FOR UPDATE`)**: Locks the DB row. Blocks all concurrent readers. Poor throughput (maximum ~500 req/sec).
    2. **Optimistic Locking with `@Version`**:
       * `UPDATE stock SET quantity = quantity - 1, version = version + 1 WHERE id = 1 AND version = :version AND quantity > 0;`
       * Better throughput, but high retry failure rate under heavy contention.
    3. **Redis Atomic Lua Script (Recommended - 100k+ req/sec)**:
       * Pre-load inventory into Redis (`stock:item:101 = 1000`).
       * Execute atomic Lua script on Redis single-threaded engine:
         ```lua
         local stock = tonumber(redis.call('get', KEYS[1]))
         if stock and stock > 0 then
             redis.call('decr', KEYS[1])
             return 1 -- Success
         else
             return 0 -- Sold Out
         end
         ```
       * If successful, enqueue the order asynchronously into Kafka to persist to MySQL database.

---

### Q159: Scenario: How do you diagnose and resolve HikariCP Database Connection Pool Starvation in production?
* **Problem & Root Cause**:
  * **Symptom**: Application throws `SQLTransientConnectionException: Connection is not available, request timed out after 30000ms`.
  * **Causes**:
    1. Long-running DB transactions holding connections while executing slow external HTTP calls.
    2. Connection leaks: Code opening connections without `try-with-resources`.
    3. Thread pool size ($T$) vastly exceeding DB connection pool size ($C$).
  * **Resolution Steps**:
    1. Enable HikariCP connection leak detection: `spring.datasource.hikari.leak-detection-threshold=2000` (logs stack trace if a connection is held $> 2\text{ seconds}$).
    2. Move all external HTTP calls and slow calculations **OUTSIDE** of `@Transactional` boundaries.
    3. Sizing Formula (PostgreSQL / MySQL):
       $$\text{Connections} = (\text{CPU Cores} \times 2) + \text{Effective Spindle Count}$$
       *For an 8-core DB server, a pool size of 16–32 connections delivers higher throughput than 500 connections due to OS thread context switching and disk I/O lock contention.*

---

### Q160: Scenario: How do you handle graceful shutdown of a Spring Boot / Java application running in Kubernetes?
* **Problem & Zero-Downtime Lifecycle**:
  * When Kubernetes scales down a Pod, it sends `SIGTERM`, removes the Pod from Service endpoints, and waits `terminationGracePeriodSeconds` (default 30s) before sending `SIGKILL`.
  * **Graceful Shutdown Steps in Java**:
    1. Enable Spring Boot Graceful Shutdown: `server.shutdown=graceful`.
    2. Stop accepting new incoming HTTP requests (return 503 to readiness probes).
    3. Allow existing inflight HTTP requests up to 20 seconds to complete.
    4. Shutdown worker thread pools cleanly:
       ```java
       executor.shutdown(); // Stop accepting new tasks
       if (!executor.awaitTermination(15, TimeUnit.SECONDS)) {
           executor.shutdownNow(); // Cancel active tasks
       }
       ```
    5. Flush pending Kafka producer buffers and close DB connection pools.

---

### Q161: Scenario: How do you prevent OutOfMemoryError when reading massive 10GB CSV/JSON files in Java?
* **Streaming vs Full-Memory Buffering**:
  * **The Mistake**: Calling `Files.readAllLines(path)` or `ObjectMapper.readValue(file)` loads all 10GB into JVM heap, triggering instant `OutOfMemoryError: Java heap space`.
  * **The Solution**: Use **Streaming Parsers**:
    1. **CSV Streaming**: Use `BufferedReader` with `Files.lines(path)`:
       ```java
       try (Stream<String> lines = Files.lines(Path.of("large.csv"))) {
           lines.forEach(this::processRecord); // Processes 1 line at a time (O(1) memory!)
       }
       ```
    2. **JSON Streaming**: Use Jackson `JsonParser` (token-based streaming parser) or Jackson `MappingIterator`, reading one JSON token/object at a time without building an in-memory tree.

---

### Q162: Scenario: How do you handle Circular Dependencies between Spring Beans?
* **Root Cause & Resolutions**:
  * **Scenario**: `ServiceA` injects `ServiceB`, and `ServiceB` injects `ServiceA`.
  * **Why it breaks**: In constructor injection, the Spring IoC container cannot determine which bean to instantiate first.
  * **3 Solutions**:
    1. **Refactor Design (Best Practice)**: Extract common logic into a third service (`ServiceC`) to break the circular dependency.
    2. **`@Lazy` Annotation**: Add `@Lazy` to one constructor: `@Autowired public ServiceA(@Lazy ServiceB b)`. Spring injects a dynamic proxy that resolves the real bean only when first called.
    3. **Setter / Field Injection**: Allows Spring's 3-level cache (`singletonFactories`) to resolve circular dependencies for singletons (disabled by default in Spring Boot 2.6+).

---

### Q163: Scenario: Why must `BigDecimal` ALWAYS be constructed with `String` instead of `double` in Financial Systems?
* **IEEE 754 Floating-Point Inaccuracy**:
  * Primitives `double` and `float` use binary floating-point representation (IEEE 754), which cannot represent fractions like $0.1$ or $0.01$ accurately:
    ```java
    double a = 0.02;
    double b = 0.03;
    System.out.println(b - a); // Prints 0.009999999999999998 (CORRUPT CENTS!)

    BigDecimal bdDouble = new BigDecimal(0.02); // Still corrupt!
    BigDecimal bdString = new BigDecimal("0.02"); // EXACT!
    BigDecimal result = new BigDecimal("0.03").subtract(bdString); // Prints 0.01
    ```
  * **Financial Standard**: Always use `new BigDecimal("0.02")` or `BigDecimal.valueOf(0.02)` with explicit `RoundingMode.HALF_EVEN` (Banker's Rounding).

---

### Q164: Scenario: Why should Sensitive Passwords be stored in `char[]` instead of `String` in Java?
* **String Immutability & Memory Footprint**:
  * **`String` is Immutable & Interned**: When a password is stored as a `String`, it resides in the JVM Heap and String Pool until garbage collected. If the JVM generates a heap dump or an attacker inspects process memory, the cleartext password remains visible in plain memory for hours!
  * **`char[]` Can Be Overwritten**: With a `char[]`, the developer can immediately overwrite the memory with zeros as soon as authentication finishes:
    ```java
    char[] password = getPasswordFromUser();
    authenticate(password);
    Arrays.fill(password, '\0'); // Memory wiped immediately!
    ```

---

### Q165: Scenario: How do you implement a Distributed Rate Limiter across multiple Java microservice instances?
* **Redis Token Bucket with Lua Script**:
  * Storing rate limit counters locally in JVM memory (`AtomicInteger`) fails because requests are load-balanced across 20 pods.
  * **Solution**: Maintain centralized token buckets in Redis using an atomic Lua script:
    * Key: `rate:user:101`
    * Script adds tokens based on elapsed time since `last_refreshed_timestamp` up to `bucket_capacity`, decrements 1 token, and returns remaining tokens in strict atomic fashion.

---

### Q166: Scenario: How do you diagnose a Slow Response Time (High P99 Latency Spike) in a Java Microservice?
* **Systematic Troubleshooting Workflow**:
  1. **Distributed Tracing (OpenTelemetry / Jaeger)**: Check spans to see whether latency is spent in DB query, downstream REST call, or local Java processing.
  2. **Check GC Logs**: Inspect if P99 spikes correlate with Stop-the-World GC pauses (e.g., G1 evacuation failure or Full GC).
  3. **Check Thread Contention (`jstack` / APM)**: Check if worker threads are blocked in `BLOCKED` or `WAITING` states contending for a shared `synchronized` monitor or database connection.
  4. **Check CPU Steal / Memory Throttling**: In Kubernetes containers, check if the pod is hitting CPU limits (`cgroups` CPU throttling).

---

### Q167: Scenario: How do you implement the Saga Pattern for Distributed Transactions across Microservices in Java?
* **Choreography vs Orchestration**:
  * In microservices where each service has its own database, 2-Phase Commit (2PC) is slow and non-scalable.
  * **Saga Pattern**: Breaks a distributed transaction into a series of local transactions:
    1. `OrderService` creates order in `PENDING` state $\to$ emits `OrderCreatedEvent`.
    2. `PaymentService` consumes event, charges card $\to$ emits `PaymentSuccessEvent`.
    3. `InventoryService` consumes event, fails to reserve stock $\to$ emits `InventoryFailedEvent`.
    4. **Compensating Transactions**: `PaymentService` listens to `InventoryFailedEvent` and automatically issues a **Refund**, while `OrderService` updates order to `CANCELLED`.

---

### Q168: Scenario: How do you prevent Cache Penetration, Cache Breakdown, and Cache Avalanche in Java?
* **Caching Failure Modes & Defenses**:
  * **1. Cache Penetration**: Client queries non-existent IDs (e.g., `id = -999`), bypassing cache and hitting DB repeatedly.
    * *Defense*: **Bloom Filter** in front of cache to intercept non-existent keys, or cache `null` values with a short TTL (60s).
  * **2. Cache Breakdown (Hotspot Key Expiration)**: A super-popular key (e.g., iPhone launch) expires, and 100,000 concurrent requests hammer the DB simultaneously.
    * *Defense*: Mutex Distributed Lock on cache miss so only 1 thread queries DB, or background thread refreshing cache before expiration.
  * **3. Cache Avalanche**: Millions of keys expire at the exact same second, overwhelming the DB.
    * *Defense*: Add a random jitter to TTL: `TTL = 3600 + Random.nextInt(600)` seconds.

---

### Q169: Scenario: How do you design an Asynchronous Event-Driven Producer-Consumer pipeline in Java with Backpressure?
* **Reactive Streams / Bounded Queue**:
  * Use a bounded `ArrayBlockingQueue` or Project Reactor `Flux.create(sink, OverflowStrategy.BUFFER)`.
  * When the queue reaches capacity, the producer applies backpressure (e.g., blocking the producer thread or returning HTTP 429 Too Many Requests to clients) rather than buffering unbounded tasks in memory.

---

### Q170: Scenario: How do you handle Database Read-Write Splitting (Primary/Replica) in Spring Boot?
* **AbstractRoutingDataSource**:
  * Extend `AbstractRoutingDataSource` and maintain a `ThreadLocal<DbContextType>` tracking whether the current transaction is read-only (`@Transactional(readOnly = true)`).
  * `determineCurrentLookupKey()` routes write queries to the Primary master database and routes read queries to read replicas.

---

### Q171: Scenario: What causes Thread Contention on `synchronized` vs `ConcurrentHashMap` in High-Throughput metrics logging?
* **Lock Granularity**:
  * Using a single `Collections.synchronizedMap` forces all 200 HTTP worker threads to contend for 1 monitor lock, causing $90\%$ of CPU time to be wasted on thread sleep/wakeup context switches.
  * Migrating to `ConcurrentHashMap` splits contention across 16+ separate bucket locks with lock-free reads, eliminating thread bottlenecks.

---

### Q172: Scenario: How do you secure a REST API against Replay Attacks in Java?
* **Timestamp & Nonce Validation**:
  * Client sends HTTP headers: `X-Timestamp` (current epoch ms) and `X-Nonce` (unique random string) signed with HMAC-SHA256.
  * Java service verifies:
    1. `Math.abs(now - timestamp) < 300_000` (rejects requests older than 5 minutes).
    2. Checks Redis: `redis.set("nonce:" + nonce, "1", "EX", 300, "NX")`. If nonce already exists, reject as Replay Attack!

---

### Q173: Scenario: How do you implement Resilience4j Circuit Breakers to prevent cascading failures?
* **State Machine Transitions**:
  * Wrap downstream RPC calls in a Circuit Breaker:
    * **CLOSED**: Normal operation. Requests pass through. Tracks failure rate over a sliding window of 100 calls.
    * **OPEN**: If failure rate exceeds $50\%$, the circuit trips to **OPEN**. All incoming calls fail immediately with `CallNotPermittedException` without making network calls, shielding the struggling downstream service.
    * **HALF-OPEN**: After a wait duration (e.g., 10s), the circuit transitions to **HALF-OPEN**, allowing 10 test calls. If successful, resets to **CLOSED**; otherwise reverts to **OPEN**.

---

### Q174: Scenario: How do you perform Zero-Downtime Database Schema Migrations with Flyway in Java?
* **Expand-and-Contract (Parallel Run) Pattern**:
  * Never drop or rename a column in a single deployment!
  * **Phase 1 (Expand)**: Add new column `new_name`. Deploy Java code that writes to both `old_name` and `new_name`, but reads from `old_name`.
  * **Phase 2 (Migrate Data)**: Run background migration script copying legacy data to `new_name`.
  * **Phase 3 (Switch Read)**: Deploy Java code reading from `new_name`.
  * **Phase 4 (Contract)**: Drop `old_name` column in database.

---

### Q175: Summary Matrix: Real-World Production Architecture Scenarios

| Scenario | Primary Bottleneck / Risk | Architectural Java Solution |
|---|---|---|
| **Payment Idempotency** | Double charges on network retry | Unique Idempotency Key + Redis `SETNX` + DB Unique Index |
| **Flash Sale Inventory** | Overselling race conditions | Redis Atomic Lua Script + Kafka Async DB Persistence |
| **Connection Starvation**| Leaked DB connections / timeouts | HikariCP Leak Detection + Short `@Transactional` scopes |
| **Large File Parsing** | `OutOfMemoryError: Java heap space` | Streaming parsers (`Files.lines()`, Jackson `JsonParser`) |
| **Hotspot Cache Miss** | Cache Avalanche / Breakdown | Jittered TTL + Distributed Lock on Cache Miss |
| **Slow P99 Latency** | Thread contention / GC pauses | OpenTelemetry tracing + ZGC + Non-blocking `ReentrantLock` |

---

# 💻 Module 7: Flagship Java Coding Interview Challenges & Multi-Threaded Implementations

---

### Challenge 1: Custom Thread-Safe Concurrent LRU Cache ($O(1)$ Operations)
```java
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Thread-Safe LRU Cache combining ConcurrentHashMap with a Doubly-Linked List.
 * Provides O(1) get() and put() with fine-grained lock synchronization.
 */
public class ConcurrentLruCache<K, V> {

    private static class Node<K, V> {
        final K key;
        V value;
        Node<K, V> prev;
        Node<K, V> next;

        Node(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    private final int capacity;
    private final ConcurrentHashMap<K, Node<K, V>> map;
    private final Node<K, V> head;
    private final Node<K, V> tail;
    private final ReentrantLock lock = new ReentrantLock();

    public ConcurrentLruCache(int capacity) {
        if (capacity <= 0) throw new IllegalArgumentException("Capacity must be positive");
        this.capacity = capacity;
        this.map = new ConcurrentHashMap<>(capacity);
        this.head = new Node<>(null, null); // Dummy head
        this.tail = new Node<>(null, null); // Dummy tail
        head.next = tail;
        tail.prev = head;
    }

    public V get(K key) {
        Node<K, V> node = map.get(key);
        if (node == null) return null;

        lock.lock();
        try {
            moveToHead(node);
        } finally {
            lock.unlock();
        }
        return node.value;
    }

    public void put(K key, V value) {
        Node<K, V> existing = map.get(key);
        if (existing != null) {
            lock.lock();
            try {
                existing.value = value;
                moveToHead(existing);
            } finally {
                lock.unlock();
            }
            return;
        }

        Node<K, V> newNode = new Node<>(key, value);
        lock.lock();
        try {
            if (map.size() >= capacity) {
                Node<K, V> lru = removeTail();
                if (lru != null) {
                    map.remove(lru.key);
                }
            }
            addToHead(newNode);
            map.put(key, newNode);
        } finally {
            lock.unlock();
        }
    }

    private void addToHead(Node<K, V> node) {
        node.next = head.next;
        node.prev = head;
        head.next.prev = node;
        head.next = node;
    }

    private void removeNode(Node<K, V> node) {
        node.prev.next = node.next;
        node.next.prev = node.prev;
    }

    private void moveToHead(Node<K, V> node) {
        removeNode(node);
        addToHead(node);
    }

    private Node<K, V> removeTail() {
        if (tail.prev == head) return null;
        Node<K, V> lru = tail.prev;
        removeNode(lru);
        return lru;
    }

    public int size() {
        return map.size();
    }
}
```
* **Internal Mechanics & Complexity**:
  * **$O(1)$ Lookups**: `ConcurrentHashMap` provides lock-free $O(1)$ key lookup.
  * **$O(1)$ Eviction**: Dummy sentinel nodes (`head` and `tail`) eliminate null-pointer checks when splicing nodes.
  * **Concurrency**: Node reordering on the doubly-linked list is protected by a `ReentrantLock`, ensuring zero pointer corruption under concurrent multi-threaded access.

---

### Challenge 2: Custom Bounded Blocking Queue using `ReentrantLock` & Dual `Condition`
```java
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Custom Bounded Blocking Queue implementing Producer-Consumer synchronization.
 */
public class CustomBlockingQueue<T> {

    private final Object[] items;
    private int head = 0;
    private int tail = 0;
    private int count = 0;

    private final ReentrantLock lock = new ReentrantLock(true); // Fair Lock
    private final Condition notEmpty = lock.newCondition();
    private final Condition notFull = lock.newCondition();

    public CustomBlockingQueue(int capacity) {
        if (capacity <= 0) throw new IllegalArgumentException("Capacity must be > 0");
        this.items = new Object[capacity];
    }

    public void put(T item) throws InterruptedException {
        lock.lockInterruptibly();
        try {
            while (count == items.length) { // While loop prevents spurious wakeups!
                notFull.await();
            }
            items[tail] = item;
            if (++tail == items.length) tail = 0; // Circular buffer wraparound
            count++;
            notEmpty.signal(); // Signal waiting consumers
        } finally {
            lock.unlock();
        }
    }

    @SuppressWarnings("unchecked")
    public T take() throws InterruptedException {
        lock.lockInterruptibly();
        try {
            while (count == 0) { // While loop check
                notEmpty.await();
            }
            T item = (T) items[head];
            items[head] = null; // Prevent memory leak
            if (++head == items.length) head = 0;
            count--;
            notFull.signal(); // Signal waiting producers
            return item;
        } finally {
            lock.unlock();
        }
    }

    public int size() {
        lock.lock();
        try { return count; }
        finally { lock.unlock(); }
    }
}
```
* **Internal Mechanics & Complexity**:
  * **Dual Condition Signalling**: Splits wait queues into `notEmpty` (consumers) and `notFull` (producers), avoiding expensive `signalAll()` thundering-herd context switches.
  * **Circular Buffer Array**: Avoids continuous array memory reallocation and garbage collection by wrapping array indices `(++tail == len ? 0 : tail)` in $O(1)$ time.

---

### Challenge 3: Custom Thread Pool Implementation from Scratch
```java
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Custom ThreadPool executing submitted Runnables across fixed worker threads.
 */
public class CustomThreadPool {

    private final BlockingQueue<Runnable> taskQueue;
    private final List<Worker> workers;
    private final AtomicBoolean isShutdown = new AtomicBoolean(false);

    public CustomThreadPool(int poolSize, int queueCapacity) {
        this.taskQueue = new LinkedBlockingQueue<>(queueCapacity);
        this.workers = new ArrayList<>(poolSize);

        for (int i = 0; i < poolSize; i++) {
            Worker worker = new Worker("CustomPool-Worker-" + i);
            workers.add(worker);
            worker.start();
        }
    }

    public void execute(Runnable task) {
        if (isShutdown.get()) {
            throw new IllegalStateException("ThreadPool is already shut down!");
        }
        if (!taskQueue.offer(task)) {
            throw new RuntimeException("Task Queue is full! Rejection policy triggered.");
        }
    }

    public void shutdown() {
        isShutdown.set(true);
        for (Worker worker : workers) {
            worker.interrupt();
        }
    }

    private class Worker extends Thread {
        Worker(String name) { super(name); }

        @Override
        public void run() {
            while (!isShutdown.get() || !taskQueue.isEmpty()) {
                try {
                    Runnable task = taskQueue.take();
                    task.run();
                } catch (InterruptedException e) {
                    if (isShutdown.get() && taskQueue.isEmpty()) break;
                } catch (Throwable t) {
                    System.err.println("Task execution failed: " + t.getMessage());
                }
            }
        }
    }
}
```
* **Internal Mechanics**:
  * **Continuous Worker Loop**: Worker threads stay alive in a `while` loop, blocking on `taskQueue.take()`.
  * **Graceful Draining**: During shutdown, threads interrupt blocking waits but finish executing all tasks remaining in the queue before terminating.

---

### Challenge 4: Multi-Threaded Sequential Number Printing (3 Threads printing 1 to 100)
```java
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 3 Threads printing 1 to 100 sequentially:
 * Thread 0 prints 1, 4, 7...
 * Thread 1 prints 2, 5, 8...
 * Thread 2 prints 3, 6, 9...
 */
public class SequentialNumberPrinter {

    private final int maxNumber;
    private int currentNumber = 1;
    private int currentTurn = 0; // 0 -> Thread 0, 1 -> Thread 1, 2 -> Thread 2

    private final ReentrantLock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();

    public SequentialNumberPrinter(int maxNumber) {
        this.maxNumber = maxNumber;
    }

    public void printNumbers(int threadId) {
        while (true) {
            lock.lock();
            try {
                while (currentTurn != threadId && currentNumber <= maxNumber) {
                    condition.await();
                }
                if (currentNumber > maxNumber) {
                    condition.signalAll();
                    break;
                }
                System.out.printf("Thread-%d: %d%n", threadId, currentNumber++);
                currentTurn = (currentTurn + 1) % 3;
                condition.signalAll();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } finally {
                lock.unlock();
            }
        }
    }

    public static void main(String[] args) {
        SequentialNumberPrinter printer = new SequentialNumberPrinter(20);
        new Thread(() -> printer.printNumbers(0), "T0").start();
        new Thread(() -> printer.printNumbers(1), "T1").start();
        new Thread(() -> printer.printNumbers(2), "T2").start();
    }
}
```
* **Internal Mechanics**:
  * **State Variable Modulo Routing**: `currentTurn = (currentTurn + 1) % 3` cleanly routes execution order across arbitrary numbers of worker threads.
  * **Atomic Condition Notification**: `condition.signalAll()` ensures all threads wake up to check if their turn or the termination condition (`currentNumber > maxNumber`) has been reached.

---

### Challenge 5: Producer-Consumer with Poison Pill Graceful Shutdown
```java
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Producer-Consumer pipeline terminating cleanly via special Sentinel "Poison Pill" objects.
 */
public class PoisonPillPipeline {

    public static final String POISON_PILL = new String("TERMINATE_PILL");

    public static void main(String[] args) throws InterruptedException {
        BlockingQueue<String> queue = new LinkedBlockingQueue<>(10);
        int numConsumers = 3;

        // Consumer Threads
        for (int i = 0; i < numConsumers; i++) {
            new Thread(() -> {
                try {
                    while (true) {
                        String item = queue.take();
                        if (item == POISON_PILL) { // Strict reference identity check!
                            System.out.println(Thread.currentThread().getName() + " received Poison Pill. Exiting.");
                            break;
                        }
                        System.out.println(Thread.currentThread().getName() + " processed: " + item);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "Consumer-" + i).start();
        }

        // Producer Thread
        Thread producer = new Thread(() -> {
            try {
                for (int i = 1; i <= 10; i++) {
                    queue.put("Data-Payload-" + i);
                }
                // Send 1 Poison Pill for EACH active consumer
                for (int i = 0; i < numConsumers; i++) {
                    queue.put(POISON_PILL);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Producer");

        producer.start();
        producer.join();
    }
}
```
* **Internal Mechanics**:
  * **Deterministic Teardown**: Eliminates thread interruption race conditions by passing 1 sentinel reference per consumer into the blocking queue, guaranteeing all items ahead of the pill are completely processed before termination.

---

### Challenge 6: Token Bucket Rate Limiter with Atomic Operations
```java
import java.util.concurrent.atomic.AtomicLong;

/**
 * High-performance, lock-free Token Bucket Rate Limiter.
 */
public class TokenBucketRateLimiter {

    private final long maxCapacity;
    private final long refillRatePerSecond;
    private final AtomicLong currentTokens;
    private final AtomicLong lastRefillTimestamp;

    public TokenBucketRateLimiter(long maxCapacity, long refillRatePerSecond) {
        this.maxCapacity = maxCapacity;
        this.refillRatePerSecond = refillRatePerSecond;
        this.currentTokens = new AtomicLong(maxCapacity);
        this.lastRefillTimestamp = new AtomicLong(System.nanoTime());
    }

    public boolean tryAcquire(long requestedTokens) {
        if (requestedTokens <= 0) return true;

        while (true) {
            refill();
            long available = currentTokens.get();
            if (available < requestedTokens) {
                return false; // Rate limit exceeded!
            }
            if (currentTokens.compareAndSet(available, available - requestedTokens)) {
                return true; // Token successfully acquired!
            }
        }
    }

    private void refill() {
        long now = System.nanoTime();
        long lastTime = lastRefillTimestamp.get();
        long nanosElapsed = now - lastTime;

        if (nanosElapsed <= 0) return;

        long tokensToAdd = (nanosElapsed * refillRatePerSecond) / 1_000_000_000L;
        if (tokensToAdd > 0) {
            if (lastRefillTimestamp.compareAndSet(lastTime, now)) {
                currentTokens.updateAndGet(curr -> Math.min(maxCapacity, curr + tokensToAdd));
            }
        }
    }
}
```
* **Internal Mechanics**:
  * **Lazy Token Generation**: Instead of running a dedicated background thread updating tokens every second, tokens are calculated lazily on-demand inside `refill()` based on elapsed nanoseconds, achieving $O(1)$ lock-free CAS throughput.

---

### Challenge 7: Deadlock Simulation & Programmatic Deadlock Detector using `ThreadMXBean`
```java
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

/**
 * Simulates a circular lock deadlock and programmatically diagnoses it using JMX.
 */
public class DeadlockDetectorDemo {

    private static final Object LockA = new Object();
    private static final Object LockB = new Object();

    public static void main(String[] args) {
        // Thread 1: Locks A -> wants B
        new Thread(() -> {
            synchronized (LockA) {
                try { Thread.sleep(50); } catch (InterruptedException ignored) {}
                synchronized (LockB) { System.out.println("T1 completed"); }
            }
        }, "Deadlock-Thread-1").start();

        // Thread 2: Locks B -> wants A
        new Thread(() -> {
            synchronized (LockB) {
                try { Thread.sleep(50); } catch (InterruptedException ignored) {}
                synchronized (LockA) { System.out.println("T2 completed"); }
            }
        }, "Deadlock-Thread-2").start();

        // Watchdog Thread: Periodically inspects JVM for deadlocks
        new Thread(() -> {
            ThreadMXBean bean = ManagementFactory.getThreadMXBean();
            while (true) {
                try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
                long[] deadlockedThreadIds = bean.findDeadlockedThreads();
                if (deadlockedThreadIds != null && deadlockedThreadIds.length > 0) {
                    System.err.println("🚨 CRITICAL ALERT: Deadlock Detected across " + deadlockedThreadIds.length + " threads!");
                    ThreadInfo[] infos = bean.getThreadInfo(deadlockedThreadIds);
                    for (ThreadInfo info : infos) {
                        System.err.printf("Thread '%s' is BLOCKED waiting on lock owned by '%s'%n",
                                info.getThreadName(), info.getLockOwnerName());
                    }
                    break;
                }
            }
        }, "Deadlock-Watchdog").start();
    }
}
```
* **Internal Mechanics**:
  * **JMX Diagnostics**: `ThreadMXBean.findDeadlockedThreads()` traverses the JVM object monitor dependency graph to detect circular wait cycles ($T_1 \to L_B \to T_2 \to L_A \to T_1$) in $O(V + E)$ time.

---

### Challenge 8: Asynchronous Multi-API Data Aggregator with `CompletableFuture`
```java
import java.util.concurrent.*;

/**
 * Fan-out Async API Aggregator fetching User, Orders, and Loyalty Points concurrently with Timeout & Fallback.
 */
public class AsyncDataAggregator {

    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    public record UserProfile(String user, String orders, int loyaltyPoints) {}

    public CompletableFuture<UserProfile> fetchUserProfileAsync(String userId) {
        // 1. Fetch User Info
        CompletableFuture<String> userFuture = CompletableFuture.supplyAsync(() -> {
            simulateLatency(150);
            return "User-" + userId;
        }, executor);

        // 2. Fetch User Orders
        CompletableFuture<String> ordersFuture = CompletableFuture.supplyAsync(() -> {
            simulateLatency(200);
            return "Orders:[#101, #102]";
        }, executor).exceptionally(ex -> "Orders:[Empty - Fallback]");

        // 3. Fetch Loyalty Points
        CompletableFuture<Integer> pointsFuture = CompletableFuture.supplyAsync(() -> {
            simulateLatency(100);
            return 450;
        }, executor).completeOnTimeout(0, 180, TimeUnit.MILLISECONDS); // Timeout after 180ms

        // Fan-in: Combine all 3 futures concurrently
        return CompletableFuture.allOf(userFuture, ordersFuture, pointsFuture)
                .thenApply(v -> new UserProfile(
                        userFuture.join(),
                        ordersFuture.join(),
                        pointsFuture.join()
                ));
    }

    private static void simulateLatency(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}
```
* **Internal Mechanics**:
  * **Non-blocking Fan-Out / Fan-In**: `CompletableFuture.allOf()` non-blockingly combines independent asynchronous futures. Total latency equals $\max(t_1, t_2, t_3)$ instead of sequential $t_1 + t_2 + t_3$.

---

### Challenge 9: Custom Read-Write Lock from Scratch using `wait()` & `notifyAll()`
```java
/**
 * Custom Read-Write Lock allowing concurrent reads or exclusive writes.
 */
public class CustomReadWriteLock {

    private int readers = 0;
    private int writers = 0;
    private int writeRequests = 0;

    public synchronized void lockRead() throws InterruptedException {
        // Readers wait if a writer is active OR writers are waiting (Writer Preference)
        while (writers > 0 || writeRequests > 0) {
            wait();
        }
        readers++;
    }

    public synchronized void unlockRead() {
        readers--;
        notifyAll(); // Wake up waiting writers
    }

    public synchronized void lockWrite() throws InterruptedException {
        writeRequests++;
        while (readers > 0 || writers > 0) {
            wait();
        }
        writeRequests--;
        writers++;
    }

    public synchronized void unlockWrite() {
        writers--;
        notifyAll(); // Wake up waiting readers and writers
    }
}
```
* **Internal Mechanics**:
  * **Writer Preference Invariant**: `writeRequests > 0` prevents reader threads from continuously starving waiting write threads under high read-frequency workloads.

---

### Challenge 10: Custom `HashMap` with Bitwise Buckets, Perturbation Hash & Rehashing
```java
import java.util.Objects;

/**
 * Custom HashMap implementation with power-of-two capacity, perturbation hashing, and dynamic rehashing.
 */
public class CustomHashMap<K, V> {

    static class Entry<K, V> {
        final K key;
        V value;
        Entry<K, V> next;

        Entry(K key, V value, Entry<K, V> next) {
            this.key = key;
            this.value = value;
            this.next = next;
        }
    }

    private Entry<K, V>[] table;
    private int size = 0;
    private static final int DEFAULT_CAPACITY = 16;
    private static final float LOAD_FACTOR = 0.75f;

    @SuppressWarnings("unchecked")
    public CustomHashMap() {
        this.table = new Entry[DEFAULT_CAPACITY];
    }

    private int hash(Object key) {
        if (key == null) return 0;
        int h = key.hashCode();
        return h ^ (h >>> 16); // Perturbation shift
    }

    private int getIndex(int hash, int capacity) {
        return (capacity - 1) & hash; // Bitwise masking
    }

    public void put(K key, V value) {
        if ((float) size / table.length >= LOAD_FACTOR) {
            resize();
        }
        int hash = hash(key);
        int index = getIndex(hash, table.length);

        Entry<K, V> current = table[index];
        while (current != null) {
            if (Objects.equals(current.key, key)) {
                current.value = value;
                return;
            }
            current = current.next;
        }

        table[index] = new Entry<>(key, value, table[index]); // Insert at head
        size++;
    }

    public V get(K key) {
        int hash = hash(key);
        int index = getIndex(hash, table.length);

        Entry<K, V> current = table[index];
        while (current != null) {
            if (Objects.equals(current.key, key)) {
                return current.value;
            }
            current = current.next;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private void resize() {
        int newCapacity = table.length * 2;
        Entry<K, V>[] newTable = new Entry[newCapacity];

        for (Entry<K, V> head : table) {
            while (head != null) {
                Entry<K, V> next = head.next;
                int newIndex = getIndex(hash(head.key), newCapacity);
                head.next = newTable[newIndex];
                newTable[newIndex] = head;
                head = next;
            }
        }
        this.table = newTable;
    }

    public int size() { return size; }
}
```
* **Internal Mechanics**:
  * **$O(1)$ Bitwise Indexing**: Implements `(capacity - 1) & hash` and `h ^ (h >>> 16)` perturbation shift to distribute bits uniformly across buckets.

---

### Challenge 11: Top-K Frequent Elements in Real-Time Stream (Min-Heap)
```java
import java.util.*;

/**
 * Finds Top K Frequent Elements in an array in O(N log K) time using a Min-Heap.
 */
public class TopKFrequentElements {

    public static List<Integer> topKFrequent(int[] nums, int k) {
        Map<Integer, Integer> freqMap = new HashMap<>();
        for (int n : nums) {
            freqMap.put(n, freqMap.getOrDefault(n, 0) + 1);
        }

        // Min-Heap sorting by frequency ascending
        PriorityQueue<Map.Entry<Integer, Integer>> minHeap =
                new PriorityQueue<>(Comparator.comparingInt(Map.Entry::getValue));

        for (Map.Entry<Integer, Integer> entry : freqMap.entrySet()) {
            minHeap.offer(entry);
            if (minHeap.size() > k) {
                minHeap.poll(); // Evict lowest frequency element
            }
        }

        List<Integer> result = new ArrayList<>(k);
        while (!minHeap.isEmpty()) {
            result.add(minHeap.poll().getKey());
        }
        Collections.reverse(result);
        return result;
    }
}
```
* **Complexity**: Time: $O(N \log K)$, Space: $O(N + K)$.

---

### Challenge 12: Distributed Idempotent Request Deduplicator with Sliding TTL
```java
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * Idempotency Deduplication Engine rejecting duplicate request keys within TTL window.
 */
public class IdempotencyDeduplicator {

    private static class DelayedKey implements Delayed {
        final String key;
        final long expiryTime;

        DelayedKey(String key, long ttlMillis) {
            this.key = key;
            this.expiryTime = System.currentTimeMillis() + ttlMillis;
        }

        @Override
        public long getDelay(TimeUnit unit) {
            return unit.convert(expiryTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
        }

        @Override
        public int compareTo(Delayed o) {
            return Long.compare(this.expiryTime, ((DelayedKey) o).expiryTime);
        }
    }

    private final ConcurrentHashMap<String, Boolean> activeKeys = new ConcurrentHashMap<>();
    private final DelayQueue<DelayedKey> delayQueue = new DelayQueue<>();

    public IdempotencyDeduplicator() {
        Thread cleaner = new Thread(() -> {
            while (true) {
                try {
                    DelayedKey expired = delayQueue.take();
                    activeKeys.remove(expired.key);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "Idempotency-Cleaner");
        cleaner.setDaemon(true);
        cleaner.start();
    }

    public boolean tryAcquire(String idempotencyKey, long ttlMillis) {
        if (activeKeys.putIfAbsent(idempotencyKey, Boolean.TRUE) == null) {
            delayQueue.offer(new DelayedKey(idempotencyKey, ttlMillis));
            return true; // First time seen (Allowed)
        }
        return false; // Duplicate request (Rejected)
    }
}
```

---

### Challenge 13: Parallel Multi-Threaded Chunk Downloader with `CountDownLatch`
```java
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Multi-Threaded Parallel Chunk Processor dividing tasks across worker threads.
 */
public class ParallelChunkProcessor {

    public static void processLargeDataset(int totalRecords, int chunkSize) throws InterruptedException {
        int numChunks = (int) Math.ceil((double) totalRecords / chunkSize);
        CountDownLatch latch = new CountDownLatch(numChunks);
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        for (int i = 0; i < numChunks; i++) {
            final int startIdx = i * chunkSize;
            final int endIdx = Math.min(startIdx + chunkSize, totalRecords);

            executor.submit(() -> {
                try {
                    System.out.printf("[%s] Processing chunk range [%d - %d]%n",
                            Thread.currentThread().getName(), startIdx, endIdx);
                    // Process chunk records...
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(); // Main thread awaits completion of all chunks
        executor.shutdown();
        System.out.println("All dataset chunks processed successfully.");
    }
}
```

---

### Challenge 14: Custom Lock-Free Spin-Lock using `AtomicBoolean`
```java
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * User-space Lock-Free SpinLock leveraging CPU CAS loops.
 */
public class SpinLock {

    private final AtomicBoolean isLocked = new AtomicBoolean(false);

    public void lock() {
        // Spin-wait until lock is acquired
        while (!isLocked.compareAndSet(false, true)) {
            Thread.onSpinWait(); // JVM Hint for CPU pause instruction (reduces bus contention)
        }
    }

    public void unlock() {
        isLocked.set(false);
    }
}
```

---

### Challenge 15: Thread-Safe Generic Object Pool with `Semaphore`
```java
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.function.Supplier;

/**
 * Thread-Safe Generic Resource Pool managing bounded expensive objects (e.g., DB connections).
 */
public class ObjectPool<T> {

    private final BlockingQueue<T> pool;
    private final Semaphore semaphore;

    public ObjectPool(int size, Supplier<T> factory) {
        this.pool = new LinkedBlockingQueue<>(size);
        this.semaphore = new Semaphore(size, true); // Fair Semaphore

        for (int i = 0; i < size; i++) {
            pool.offer(factory.get());
        }
    }

    public T acquire() throws InterruptedException {
        semaphore.acquire();
        return pool.take();
    }

    public void release(T resource) {
        if (resource != null) {
            pool.offer(resource);
            semaphore.release();
        }
    }
}
```

---

# 🧬 Module 8: Java Generics Deep Internals, Advanced Design & Interview Q&As

---

### Q176: How do Java Generics work internally under the hood? What is Type Erasure and why was it chosen?
* **JVM Bytecode Mechanics & Backward Compatibility**:
  * Java Generics were introduced in **Java 5 (JSR 14)** as a compile-time syntactic enhancement.
  * **How It Works Internally**:
    1. **Compile-Time Type Checking**: The Java compiler (`javac`) verifies all generic type constraints, ensuring type correctness before code is compiled.
    2. **Type Erasure**: Once validation succeeds, the compiler **erases all generic type parameters (`<T>`)** from class and method signatures.
    3. **Bounding Replacement**: Unbounded type parameters `<T>` are replaced with `java.lang.Object`. Bounded type parameters `<T extends Comparable<T>>` are replaced with their first upper bound (`Comparable`).
    4. **Synthetic Cast Insertion**: The compiler automatically inserts explicit `checkcast` bytecode instructions at call sites where generic return values are consumed:
       ```java
       // Source Code:
       List<String> list = new ArrayList<>();
       list.add("hello");
       String s = list.get(0);

       // Compiled JVM Bytecode equivalent (Decompiled):
       List list = new ArrayList();
       list.add("hello");
       String s = (String) list.get(0); // Compiler inserted checkcast!
       ```
  * **Why Type Erasure was Chosen**:
    * **100% Binary Backward Compatibility**: Allowed Java 5 applications to run seamlessly on legacy JVMs (Java 1.4 and older) and interact with pre-generic legacy libraries (`List`, `Vector`) without requiring JVM bytecode format redesign or separate runtime class instances (unlike C++ templates or C# reified generics which generate separate binary code per concrete type `List<int>` vs `List<string>`).

---

### Q177: What are Synthetic Bridge Methods in Java Generics? Why does polymorphism break without them?
* **Dynamic Method Dispatch & Method Signature Preservation**:
  * When a subclass extends a generic parent class and specifies a concrete type parameter, Type Erasure creates a signature mismatch:
    ```java
    public class Node<T> {
        public void setData(T data) { ... }
    }

    public class StringNode extends Node<String> {
        @Override
        public void setData(String data) { ... }
    }
    ```
  * **The Polymorphism Conflict**:
    * After Type Erasure, `Node.setData(T)` becomes `Node.setData(Object)`.
    * `StringNode.setData(String)` does NOT have the same parameter signature as `Node.setData(Object)`.
    * If a caller invokes `Node node = new StringNode(); node.setData("text");`, dynamic method dispatch would bypass `StringNode.setData(String)` because the parameter types differ!
  * **The Compiler's Solution (Synthetic Bridge Method)**:
    * The compiler automatically generates a hidden **Synthetic Bridge Method** in `StringNode` bytecode:
      ```java
      // Generated by javac (ACC_BRIDGE, ACC_SYNTHETIC):
      public void setData(Object data) {
          setData((String) data); // Casts and delegates to the concrete method!
      }
      ```
    * This guarantees polymorphic method dispatch works correctly at runtime.

---

### Q178: Why are `new T()`, `new T[]`, and `instanceof T` ILLEGAL in Java Generics? How do you bypass these restrictions?
* **Runtime Reification Limitations & Defensive Workarounds**:
  * **1. `new T()` is Illegal**: Because `T` is erased to `Object` at runtime, the JVM does not know what constructor to invoke or how much memory to allocate.
    * *Solution*: Pass a `Class<T>` token or `Supplier<T>` factory:
      ```java
      public <T> T createInstance(Class<T> clazz) throws Exception {
          return clazz.getDeclaredConstructor().newInstance();
      }
      // OR: public <T> T createInstance(Supplier<T> factory) { return factory.get(); }
      ```
  * **2. `new T[]` is Illegal**: Java arrays are **reified** (they store their concrete component type in memory at runtime to enforce type safety during `a[0] = obj`). Because `T` is erased, the JVM cannot create a typed array.
    * *Solution*: Use `Array.newInstance(Class<T>, int capacity)`:
      ```java
      @SuppressWarnings("unchecked")
      public <T> T[] createArray(Class<T> clazz, int capacity) {
          return (T[]) java.lang.reflect.Array.newInstance(clazz, capacity);
      }
      ```
  * **3. `obj instanceof T` is Illegal**: Since `T` does not exist at runtime, `instanceof` cannot test against an erased type.
    * *Solution*: Use `clazz.isInstance(obj)`.

---

### Q179: What is Heap Pollution in Java Generics? What is `@SafeVarargs`?
* **Type Safety Breach & Varargs Arrays**:
  * **Heap Pollution**: Occurs when a variable of a parameterized type refers to an object that is not of that parameterized type, leading to unexpected `ClassCastException`s at runtime:
    ```java
    List<String> stringList = new ArrayList<>();
    List rawList = stringList; // Warning: unchecked assignment
    rawList.add(Integer.valueOf(42)); // Heap Pollution!
    String s = stringList.get(0); // Throws ClassCastException: Integer cannot be cast to String!
    ```
  * **Generic Varargs Hazard**:
    * When a method accepts generic varargs `public static <T> void addToList(List<T> list, T... elements)`, the compiler creates an underlying array `T[] elements`.
    * Because generic arrays cannot be reified, the compiler emits an "unchecked generic array creation" warning.
  * **`@SafeVarargs` Annotation**:
    * Applied to `static`, `final`, or `private` methods.
    * Developer asserts to the compiler: *"I promise this method does NOT store anything into the varargs array and does NOT let the array escape to untrusted code."*
    * Suppresses heap pollution warnings safely.

---

### Q180: Explain the PECS Rule (Producer Extends, Consumer Super) with concrete production examples.
* **Covariance vs Contravariance Invariance Matrix**:
  * **The Problem**: In Java, generics are **invariant** (`List<Integer>` is NOT a subtype of `List<Number>`, even though `Integer` is a subtype of `Number`).
  * **The Solution (PECS)**:
    1. **Producer Extends (`? extends T` - Covariance)**:
       * Use when you only **READ (produce)** data from the collection.
       * You can safely read elements as type `T`: `T item = list.get(0)`.
       * You **CANNOT write/add** anything into `List<? extends T>` (except `null`) because the compiler cannot guarantee the concrete subtype!
    2. **Consumer Super (`? super T` - Contravariance)**:
       * Use when you only **WRITE (consume)** data into the collection.
       * You can safely add elements of type `T` (and subtypes): `list.add(new Integer(5))`.
       * When reading, elements return as `Object` (loss of concrete type).
  * **The Gold Standard: `Collections.copy()`**:
    ```java
    public static <T> void copy(List<? super T> dest, List<? extends T> src) {
        // src PRODUCES elements (read via get) -> ? extends T
        // dest CONSUMES elements (written via set) -> ? super T
        for (int i = 0; i < src.size(); i++) {
            dest.set(i, src.get(i));
        }
    }
    ```

---

### Q181: What are Multiple Bounds (`<T extends Class & Interface1 & Interface2>`) in Java Generics?
* **Intersection Types & Rule Constraints**:
  * Java allows a generic type parameter to have multiple bounds using the `&` operator:
    ```java
    public <T extends Number & Comparable<T> & Serializable> void process(T value) {
        int val = value.intValue(); // Accessible from Number
        value.compareTo(value);     // Accessible from Comparable
    }
    ```
  * **Rules**:
    1. Only **ONE class** can be specified, and it **MUST be listed FIRST** in the bounds (`<T extends MyClass & Interface1>` is valid; `<T extends Interface1 & MyClass>` is a syntax error!).
    2. Any number of interfaces can follow the class bound.

---

### Q182: What are Recursive Type Bounds? How do they enable Self-Referential Fluent Builders?
* **Self-Bounding Generics**:
  * A **Recursive Type Bound** is a type parameter that references itself in its own bound: `<T extends Comparable<T>>` or `<T extends Builder<T>>`.
  * **The Subclass Builder Problem**: When subclassing a fluent Builder, methods in the parent builder return `ParentBuilder`, breaking method chaining on subclass methods!
  * **The Recursive Generics Solution**:
    ```java
    public abstract class BaseEntityBuilder<T extends BaseEntityBuilder<T>> {
        protected Long id;

        @SuppressWarnings("unchecked")
        protected T self() { return (T) this; } // Returns concrete subclass type!

        public T id(Long id) {
            this.id = id;
            return self();
        }
    }

    public class UserBuilder extends BaseEntityBuilder<UserBuilder> {
        private String username;

        public UserBuilder username(String username) {
            this.username = username;
            return this;
        }
    }

    // Fluent Chaining seamlessly works across parent and child builder methods:
    UserBuilder builder = new UserBuilder().id(101L).username("alice");
    ```

---

### Q183: How do you implement a Type-Safe Heterogeneous Container in Java?
* **Dynamic Type Keying (`Map<Class<?>, Object>`)**:
  * Standard collections have fixed type parameters (`Map<String, Integer>`). A **Type-Safe Heterogeneous Container** allows storing and retrieving values of *different* types safely without unchecked casting:
    ```java
    import java.util.HashMap;
    import java.util.Map;
    import java.util.Objects;

    public class TypeSafeContainer {
        private final Map<Class<?>, Object> container = new HashMap<>();

        public <T> void put(Class<T> type, T instance) {
            container.put(Objects.requireNonNull(type), type.cast(instance));
        }

        public <T> T get(Class<T> type) {
            return type.cast(container.get(type)); // 100% type-safe cast!
        }

        public static void main(String[] args) {
            TypeSafeContainer context = new TypeSafeContainer();
            context.put(String.class, "Spring Context");
            context.put(Integer.class, 8080);

            String name = context.get(String.class); // No explicit casting needed!
            Integer port = context.get(Integer.class);
        }
    }
    ```

---

### Q184: How do you design a Generic Repository Pattern with Generic DAO in Java?
* **Clean Architecture Abstraction**:
  ```java
  import java.io.Serializable;
  import java.util.List;
  import java.util.Optional;

  public interface GenericRepository<T, ID extends Serializable> {
      T save(T entity);
      Optional<T> findById(ID id);
      List<T> findAll();
      void deleteById(ID id);
      boolean existsById(ID id);
  }

  // Domain Entity
  public class User implements Serializable {
      private Long id;
      private String email;
  }

  // Concrete Interface
  public interface UserRepository extends GenericRepository<User, Long> {
      Optional<User> findByEmail(String email);
  }
  ```

---

### Q185: How do you design a Generic Result / Response Envelope wrapper in Java for REST APIs?
* **Type-Safe API Envelopes**:
  ```java
  import java.time.Instant;

  public record ApiResponse<T>(
      boolean success,
      String message,
      T data,
      int statusCode,
      Instant timestamp
  ) {
      public static <T> ApiResponse<T> success(T data) {
          return new ApiResponse<>(true, "Operation successful", data, 200, Instant.now());
      }

      public static <T> ApiResponse<T> error(String message, int statusCode) {
          return new ApiResponse<>(false, message, null, statusCode, Instant.now());
      }
  }
  ```

---

# 🔍 Module 9: Java Reflection Deep Dive & Spring Framework Architecture

---

### Q186: How does Java Reflection work under the hood? Explain JVM `MethodAccessorImpl` and Bytecode Inflation.
* **HotSpot JVM Reflection Execution Mechanics**:
  * Reflection allows inspecting and invoking classes, methods, constructors, and fields at runtime via `java.lang.reflect`.
  * **How Method Invocations (`method.invoke(target, args)`) Work Under the Hood**:
    1. **Initial Native Dispatch (JNI)**: For the first 15 invocations (`-Dsun.reflect.inflationThreshold=15`), the JVM executes the method call through slow native C++ JNI code (`NativeMethodAccessorImpl`).
    2. **Bytecode Inflation**: After 15 invocations, the JVM HotSpot optimizer triggers **Inflation**:
       * It dynamically generates custom bytecode at runtime (`GeneratedMethodAccessor<N>`) that performs a direct bytecode call (`invokevirtual` / `invokestatic`).
       * Subsequent invocations run at near-native speeds (~10–20x faster than initial JNI calls).
  * **Why Reflection has Performance Overhead**:
    * Bypasses JIT optimizations (Inlining, Escape Analysis).
    * Requires runtime argument boxing/unboxing (`Object[]`).
    * Requires continuous security/access permission checks (`checkAccess()`).

---

### Q187: How does Spring Framework's IoC Container use Reflection for Dependency Injection?
* **Spring IoC Reflection Engine**:
  * When Spring starts up, its `ApplicationContext` uses Reflection across 4 foundational phases:
    1. **Classpath Scanning & Metadata Extraction**: Scans classpath JARs via ASM bytecode reading, loads class definitions via `Class.forName()`, and inspects annotations (`@Component`, `@Service`, `@Repository`).
    2. **Constructor Resolution**:
       * Uses `clazz.getDeclaredConstructors()` to find constructors annotated with `@Autowired` (or single public constructor).
       * Inspects constructor parameter types (`constructor.getParameterTypes()`) and resolves dependencies from the bean factory.
    3. **Bean Instantiation**: Calls `constructor.newInstance(resolvedArgs)` to instantiate the bean on the heap.
    4. **Field / Setter Injection**:
       * Finds fields annotated with `@Autowired` or `@Value`.
       * Bypasses `private` encapsulation by invoking `field.setAccessible(true)`.
       * Sets the dependency directly via `field.set(beanInstance, dependencyInstance)`.

```
====================== SPRING IOC REFLECTION WORKFLOW ======================
 [ @Component Scan ] ──> [ clazz.getDeclaredConstructors() ]
                                    │
                             [ constructor.newInstance(deps) ]
                                    │
                             [ field.setAccessible(true) ]
                                    │
                             [ field.set(bean, dependency) ]
                                    │
                             [ method.invoke(bean, @PostConstruct) ]
============================================================================
```

---

### Q188: How does Spring execute `@PostConstruct` and `@PreDestroy` using Reflection?
* **BeanPostProcessor & Reflection Invocation**:
  * During the bean initialization lifecycle, Spring executes `InitDestroyAnnotationBeanPostProcessor`:
  * It scans `clazz.getDeclaredMethods()` for methods annotated with `@PostConstruct` / `jakarta.annotation.PostConstruct`.
  * If found, it calls `method.setAccessible(true)` and invokes `method.invoke(beanInstance)` immediately after all dependency fields have been injected and before the bean is published for use.
  * During container shutdown, it executes methods annotated with `@PreDestroy`.

---

### Q189: How does Spring Data JPA dynamically implement Repositories from interfaces using Reflection and Proxies?
* **Dynamic Query Generation without Concrete Classes**:
  * In Spring Data JPA, developers define interfaces without writing any implementation code:
    ```java
    public interface UserRepository extends JpaRepository<User, Long> {
        List<User> findByLastNameAndAgeGreaterThan(String lastName, int age);
    }
    ```
  * **Under the Hood**:
    1. Spring Data JPA uses **JDK Dynamic Proxy (`Proxy.newProxyInstance()`)** to create a proxy implementing `UserRepository`.
    2. When `findByLastNameAndAgeGreaterThan(...)` is invoked:
       * The proxy's `InvocationHandler` intercepts the call.
       * It inspects the method name via Reflection (`method.getName()`).
       * A Query Parser decomposes the name: `findBy` $\to$ entity query, `LastName` $\to$ WHERE user.lastName = ?, `And` $\to$ AND, `AgeGreaterThan` $\to$ user.age > ?.
       * Constructs the JPQL/SQL AST, binds method arguments (`args[0]`, `args[1]`), executes against EntityManager, and maps result rows into domain objects.

---

### Q190: What is the difference between Java Reflection, `MethodHandles`, and `VarHandle`?
* **Modern Java Low-Level Access Primitives**:
  * **1. Traditional Reflection (`java.lang.reflect`)**: Performs access checks on *every single invocation*; returns generic `Object` requiring runtime type casting and boxing.
  * **2. `MethodHandles` (Java 7 / JSR 292)**:
    * Performs access checks **only once** at lookup time (`MethodHandles.lookup().findVirtual(...)`).
    * Produces strongly-typed bytecode invocations (`invokeExact()`) that can be heavily optimized and inlined by the JIT compiler.
  * **3. `VarHandle` (Java 9)**:
    * Replaces `sun.misc.Unsafe` for atomic variable access and memory fence controls.
    * Allows safe atomic CAS operations, volatile reads/writes, and acquire/release memory barriers directly on class fields.

---

# 🎭 Module 10: Aspect-Oriented Programming (AOP) Masterclass & Spring AOP

---

### Q191: What is Aspect-Oriented Programming (AOP)? Define the 6 Core AOP Concepts.
* **Cross-Cutting Concern Modularization**:
  * AOP complements OOP by isolating **cross-cutting concerns** (logging, security, transactions, auditing) that span across multiple unrelated service layers.
  * **The 6 Core Concepts**:
    1. **Aspect**: A module encapsulating cross-cutting concerns (e.g., `LoggingAspect` annotated with `@Aspect`).
    2. **JoinPoint**: A specific candidate point in program execution where an aspect can be plugged in (in Spring AOP, always a method execution).
    3. **Pointcut**: A predicate expression that matches specific JoinPoints (e.g., `execution(* com.app.service.*.*(..))`).
    4. **Advice**: The action taken by an aspect at a JoinPoint (`@Before`, `@After`, `@Around`).
    5. **Target Object**: The real business service bean being advised.
    6. **AOP Proxy**: The wrapper object generated by the AOP framework to intercept method calls.

---

### Q192: What are the 5 Spring AOP Advice Types? How does `@Around` work with `ProceedingJoinPoint`?
* **Advice Lifecycle Matrix**:
  * **1. `@Before`**: Runs before the target method executes (cannot stop execution unless throwing an exception).
  * **2. `@AfterReturning`**: Runs only after the target method successfully returns without throwing exceptions (accesses returned value).
  * **3. `@AfterThrowing`**: Runs only when the target method throws an exception.
  * **4. `@After` (Finally)**: Runs regardless of method outcome (success or exception).
  * **5. `@Around` (Most Powerful)**:
    * Surrounds the target method invocation.
    * Takes `ProceedingJoinPoint` as a parameter.
    * Can modify input arguments, intercept execution, measure execution time, catch/swallow exceptions, or replace return values:
      ```java
      @Around("@annotation(LogExecutionTime)")
      public Object logTime(ProceedingJoinPoint joinPoint) throws Throwable {
          long start = System.currentTimeMillis();
          try {
              Object result = joinPoint.proceed(); // Executes target method!
              return result;
          } finally {
              long duration = System.currentTimeMillis() - start;
              System.out.printf("%s executed in %d ms%n", joinPoint.getSignature(), duration);
          }
      }
      ```

---

### Q193: Compare Spring AOP vs AspectJ. When should each be used?
* **Architectural Comparison**:

| Feature | Spring AOP | AspectJ |
|---|---|---|
| **Mechanism** | Runtime Dynamic Proxies | Bytecode Weaving (Compile / Load-Time) |
| **JoinPoint Support** | Method execution on Spring beans only | Method calls, constructors, field get/set, static init |
| **Performance** | Slight proxy overhead on first call | Zero runtime proxy overhead (native bytecode) |
| **Dependencies** | Spring Context only | Requires AspectJ compiler (`ajc`) or Java Agent |
| **Use Case** | 99% of Enterprise Spring apps (`@Transactional`) | Ultra-fine-grained auditing / non-Spring POJOs |

---

### Q194: What is the difference between JDK Dynamic Proxy and CGLIB Proxy in Spring AOP?
* **Proxy Generation Strategies**:
  * **JDK Dynamic Proxy (`java.lang.reflect.Proxy`)**:
    * Generates proxy bytecodes in memory implementing target interfaces.
    * **Requirement**: The target class **MUST implement an interface**.
  * **CGLIB Proxy (Code Generation Library)**:
    * Dynamically generates a subclass of the target class at runtime using ASM bytecode manipulation.
    * Can proxy concrete classes directly without interfaces!
    * **Limitation**: Cannot override `final` methods or `final` classes.
  * **Spring Boot Default**: Since Spring Boot 2.x, **CGLIB is enabled by default** (`spring.aop.proxy-target-class=true`) for consistent proxying behavior across all beans.

---

### Q195: What is the "Self-Invocation" Trap in Spring AOP (e.g., `@Transactional` failing)? How do you solve it?
* **Proxy Bypass Hazard & Solutions**:
  * **The Problem**: A caller calls `service.publicMethodA()`. Inside `methodA()`, it calls `this.methodB()` (which is annotated with `@Transactional`):
    ```java
    @Service
    public class OrderService {
        public void placeOrder() {
            this.processPayment(); // @Transactional is IGNORED!
        }

        @Transactional
        public void processPayment() { ... }
    }
    ```
  * **Why It Fails**: In Java, `this` refers to the **underlying target instance**, NOT the Spring AOP Proxy wrapper. The method call `this.processPayment()` completely bypasses the proxy, and no transaction is opened!
  * **3 Production Solutions**:
    1. **Self-Injection (Recommended)**: Inject the bean into itself:
       ```java
       @Service
       public class OrderService {
           @Autowired @Lazy private OrderService self;
           public void placeOrder() { self.processPayment(); } // Routes through proxy!
       }
       ```
    2. **Refactoring (Best Practice)**: Move `processPayment()` to a separate service (`PaymentService`).
    3. **`AopContext.currentProxy()`**: `((OrderService) AopContext.currentProxy()).processPayment();` (requires `@EnableAspectJAutoProxy(exposeProxy = true)`).

---

# 🔒 Module 11: Thread-Safe Class Design & Concurrency Strategy Matrix

---

### Q196: What is the formal definition of a Thread-Safe Class in Java?
* **Invariants & Encapsulation**:
  * A class is **Thread-Safe** if it behaves correctly when accessed from multiple concurrent threads, regardless of the scheduling or interleaving of the execution of those threads by the operating system/JVM, without requiring any additional synchronization or coordination on the part of the calling code.
  * **Key Properties**:
    1. Preserves internal class invariants under concurrent mutation.
    2. Never exposes half-initialized or corrupted state to callers.
    3. Eliminates race conditions and memory visibility hazards.

---

### Q197: What are the 4 Fundamental Strategies to build Thread-Safe Classes in Java?
* **Core Strategy Spectrum**:
  * **1. Immutability Strategy**: Make state unchangeable after construction (`final` fields, no setters, defensive copies). If state cannot change, race conditions are mathematically impossible.
  * **2. Thread Confinement Strategy**: Restrict data access to a single thread:
    * *Stack Confinement*: Local method variables allocated on the private thread stack.
    * *ThreadLocal Confinement*: `ThreadLocal<SimpleDateFormat>` giving each thread its own isolated instance.
  * **3. Locking & Synchronization Strategy**: Guard shared mutable state with locks (`synchronized`, `ReentrantLock`, `StampedLock`).
  * **4. Lock-Free Non-Blocking Strategy**: Use hardware CAS primitives (`AtomicInteger`, `AtomicReference`, `LongAdder`) and concurrent data structures (`ConcurrentHashMap`, `ConcurrentLinkedQueue`).

---

### Q198: How do you design a 100% Immutable, Thread-Safe Class in Java?
* **The 5 Rules of Immutability (Joshua Bloch)**:
  1. Don't provide methods that modify the object's state (no setters).
  2. Ensure the class cannot be extended (`final class` or private constructors with factories).
  3. Make all fields `final` and `private`.
  4. Ensure exclusive access to any mutable components (perform **defensive copying** in constructors and getters).
  5. Prevent `this` reference escape during construction.

```java
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

public final class ImmutableUser {
    private final String username;
    private final Date registrationDate; // Mutable object!
    private final List<String> roles;     // Mutable collection!

    public ImmutableUser(String username, Date registrationDate, List<String> roles) {
        this.username = username;
        // Defensive Copy on input:
        this.registrationDate = new Date(registrationDate.getTime());
        this.roles = Collections.unmodifiableList(new ArrayList<>(roles));
    }

    public String getUsername() { return username; }
    // Defensive Copy on output:
    public Date getRegistrationDate() { return new Date(registrationDate.getTime()); }
    public List<String> getRoles() { return roles; }
}
```

---

### Q199: Concurrency Strategy Decision Matrix: When and where to use each approach?

| Scenario / Workload | Optimal Concurrency Strategy | Recommended Java Primitive | Rationale |
|---|---|---|---|
| **Constant / Configuration State** | Immutability | `record` or `final` class | Zero synchronization cost, thread-safe by design |
| **Single-Thread Isolation (Per-Request)** | Thread Confinement | `ThreadLocal` / `ScopedValue` | Eliminates contention entirely |
| **High Contention Counters (Metrics)** | Lock-Free Cell Striping | `LongAdder` / `LongAccumulator` | Eliminates CPU cache-line bouncing |
| **High Read, Low Write Caching** | Optimistic Locking | `StampedLock` / `ReadWriteLock` | Lock-free reads with version stamps |
| **Complex Multi-Field State Invariants**| Lock Synchronization | `ReentrantLock` with Conditions | Atomic multi-step coordination |
| **Producer-Consumer Queues** | Bounded Concurrent Queue | `ArrayBlockingQueue` / `Disruptor` | Backpressure with thread wakeups |

---

### Q200: Master Thread-Safe Class Implementation: High-Performance Concurrent Event Counter
```java
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

/**
 * Production-Grade Thread-Safe Multi-Category Event Counter.
 * Uses Lock-Free Striped LongAdders for zero-contention metrics accumulation.
 */
public class ConcurrentEventCounter {

    private final ConcurrentHashMap<String, LongAdder> counters = new ConcurrentHashMap<>();

    public void increment(String eventCategory) {
        // computeIfAbsent is atomic in ConcurrentHashMap!
        counters.computeIfAbsent(eventCategory, k -> new LongAdder()).increment();
    }

    public void add(String eventCategory, long count) {
        counters.computeIfAbsent(eventCategory, k -> new LongAdder()).add(count);
    }

    public long getCount(String eventCategory) {
        LongAdder adder = counters.get(eventCategory);
        return adder != null ? adder.sum() : 0L;
    }

    public void reset(String eventCategory) {
        LongAdder adder = counters.get(eventCategory);
        if (adder != null) {
            adder.reset();
        }
    }
}
```
* **Internal Mechanics**:
  * **$O(1)$ Lock-Free Writes**: `ConcurrentHashMap.computeIfAbsent()` guarantees single allocation per key, while `LongAdder.increment()` distributes write contention across internal CPU cache-line aligned cells, achieving tens of millions of increments per second.

---

# 🎓 Master Java Interview Checklist & Reference Architecture
* **Collections**: Master bucket masking `(n - 1) & hash`, perturbation shift, TreeBins, AQS, `ConcurrentHashMap` CAS loops.
* **Concurrency**: JMM memory barriers, Mark Word lock inflation, DCL reordering, Virtual Threads continuation unmounting.
* **Singleton**: 5 breaking mechanisms (Reflection, Serialization, Cloning, ClassLoaders, DCL reordering) & Enum Singleton.
* **Generics**: Type Erasure, Bridge Methods, PECS Rule, Type-Safe Heterogeneous Containers.
* **Reflection & AOP**: JVM MethodAccessor inflation, Spring IoC reflection, CGLIB proxies, Self-Invocation proxy bypass resolution.
* **JVM**: G1 regions, ZGC colored pointers/load barriers, Metaspace, and diagnosing 100% CPU thread dumps.
* **Thread-Safe Design**: Immutability, Thread Confinement, Locking, Lock-Free Atomics.
* **Deep Concurrency Reference**: Explore the dedicated 100+ scenario guide in **[`java_thread.md`](file:///d:/project/github/1/CheatSheet_Tutorial/java_thread.md)**.







