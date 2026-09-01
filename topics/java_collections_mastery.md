# 📚 Java Collections: The 100+ Expert Scenarios

Master the most critical part of Java development. From basic Lists to lock-free Concurrent Collections and internal algorithmic mechanics.

---

## 📑 Table of Contents

1. [🏆 The "Which Collection?" Decision Matrix](#-the-which-collection-decision-matrix)
2. [🏗️ Phase 1 & 2: Core & Performance (Scenarios 1-60)](#️-phase-1--2-core--performance-scenarios-1-60)
3. [🐙 Phase 3: Concurrent & Thread-Safe (Scenarios 61-90)](#-phase-3-concurrent--thread-safe-scenarios-61-90)
4. [🧠 Phase 4: Modern Java & Sequenced (Scenarios 91-100)](#-phase-4-modern-java--sequenced-scenarios-91-100)
5. [⚖️ Data Structure Comparison Grid (The Cheat Sheet)](#️-data-structure-comparison-grid-the-cheat-sheet)

---

## 🏆 The "Which Collection?" Decision Matrix

| Requirement | Best Collection | Logic |
| :--- | :--- | :--- |
| **Duplicates Allowed?** | `ArrayList` 📦 | **Default choice.** Fast random access O(1). |
| **Unique Items Only?** | `HashSet` 🔍 | **Fast & Unordered.** Mathematical set logic. |
| **Key-Value Pairs?** | `HashMap` 🗺️ | **High Performance.** Lookup by key in O(1). |
| **Sorted by Value?** | `TreeSet` 🌲 | **O(log n) performance.** Uses Red-Black Trees. |
| **Queue / Stack?** | `ArrayDeque` 🎢 | **Faster than Stack/LinkedList.** |
| **Thread-Safety?** | `ConcurrentHashMap` 🐙 | **Segmented Locking.** High concurrency. |

[⬆️ Back to Top](#-java-collections-the-100-expert-scenarios)

---

## 🏗️ Phase 1 & 2: Core & Performance (Scenarios 1-60)

### 📦 The "List & Set" Diagnostic Table (1-40)

| # | Scenario | Diagnosis | Fix |
| :--- | :--- | :--- | :--- |
| 01 | **SubList Memory Leak** | `subList` keeps reference to HUGE parent array. | `new ArrayList<>(list.subList(0, 5))` |
| 02 | **Integer remove(1) trap** | Removes by **index**, not value `1`. | `list.remove(Integer.valueOf(1))` |
| 05 | **ConcurrentModifyExc** | Removing items while looping with `for-each`. | Use `list.removeIf()` or `Iterator.remove()`. |
| 11 | **HashCode Contract** | Overriding `.equals()` but not `hashCode()`. | Always override both to keep `HashSet` unique. |
| 15 | **TreeSet ClassCastExc** | Adding non-Comparable objects to `TreeSet`. | Implement `Comparable` or provide `Comparator`. |
| 30 | **ArrayList Copy Storm** | Adding 1M items without initial capacity. | Initialize with `new ArrayList<>(1_000_000)`. |
| 40 | **LinkedList Slowness** | Massive memory overhead per node (Pointer hell). | Avoid `LinkedList` unless doing constant-time head/tail ops. |

### 🗺️ The "Map & Cache" Performance Table (41-60)

| # | Scenario | Diagnosis | Fix |
| :--- | :--- | :--- | :--- |
| 41 | **HashMap Bucket Bomb** | All keys have same `hashCode`, making lookup O(n). | Use quality fields in `hashCode` or Java 8+ features. |
| 42 | **IdentityHashMap** | Need to check `==` instead of `.equals()`. | Use for reference-tracking (Serialization graphs). |
| 45 | **EnumMap Speed** | Using Enum as Key in a regular `HashMap`. | `EnumMap` uses a simple array; 10x faster. |
| 50 | **LRU Cache Leak** | Cache grows forever without eviction. | Use `LinkedHashMap` with `removeEldestEntry`. |

---

## 🐙 Phase 3: Concurrent & Thread-Safe (Scenarios 61-90)

| # | Scenario | Mechanism | Tradeoff |
| :--- | :--- | :--- | :--- |
| 61 | **ConcurrentHashMap** | CAS + Segmented Locking. | No `null` keys/values; much faster than `Hashtable`. |
| 65 | **CopyOnWriteArrayList** | Creates new array on every write. | **Fast reads (O(1))**; very slow/expensive writes. |
| 70 | **BlockingQueue** | `put()` and `take()` block indefinitely. | Best/Standard for Producer-Consumer logic. |
| 80 | **SynchronousQueue** | Size is 0. Hand-off only. | Producer waits for consumer to "touch" the item. |
| 90 | **ConcurrentSkipList** | Lock-free sorted map/set. | Uses Skip-List algorithm; O(log n) for threads. |

---

## 🧠 Phase 4: Modern Java & Sequenced (Scenarios 91-100)

### 🧩 Java 21 Sequenced Collections

- **Scenario 91:** Get the first element of a Set without iteration. **Fix:** `set.getFirst()`.
- **Scenario 92:** Reverse a Map predictably. **Fix:** `map.reversed()`.
- **Scenario 95:** `Arrays.asList()` vs `List.of()`. `List.of()` is **Immutable** (no `set()` allowed).

---

## ⚖️ Data Structure Comparison Grid (The Cheat Sheet)

| Collection | Ordering | Uniqueness | Nulls | Fast Op | Algorithmic Backing |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **ArrayList** | Yes (Insertion) | No | Yes | Random Access | Resizable Array |
| **HashSet** | No | Yes | One | Lookup | HashMap (buckets) |
| **TreeSet** | Yes (Natural) | Yes | No | Range Query | Red-Black Tree |
| **LinkedHashSet** | Yes (Insertion) | Yes | One | Iteration | Hash + Linked List |
| **EnumMap** | Yes (Ordinal) | Yes | No | Lookup | Bit-mapped Array |
| **Deque** | Yes (LIFO/FIFO) | No | No | Head/Tail | Double-Linked Array/List |

[⬆️ Back to Top](#-java-collections-the-100-expert-scenarios)

---

## 🎨 Icon Atlas for your Code

| Entity | Icon | Use Case |
| :--- | :--- | :--- |
| **List** | 📦 | Ordering/Index |
| **Set** | 🔍 | Uniqueness |
| **Map** | 🗺️ | Lookup/Key |
| **Queue** | 🎢 | Flow/Pipeline |
| **Thread-Safe** | 🐙 | Multi-core/High Load |

---
Queue** | 🎢 | Flow/Pipeline |
| **Thread-Safe** | 🐙 | Multi-core/High Load |

---
