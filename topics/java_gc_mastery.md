[Back to Home](../README.md) | [Interview Prep Guide](interview_prep.md) | [Tech Glossary](glossary.md)

# 🧹 Java Garbage Collection Mastery: The Cleaning Engine

Master the JVM's memory management. From generational heap architecture and marking-sweeping-compacting to low-latency G1/ZGC tuning for high-throughput microservices.

---

## 📑 Table of Contents
1. [🏠 The Memory Architecture (Heap vs. Stack)](#the-memory-architecture-heap-vs-stack)
2. [⚙️ The GC Lifecycle: Mark, Sweep, Compact](#the-gc-lifecycle-mark-sweep-compact)
3. [🔌 Collector Types (G1 vs. ZGC vs. Parallel)](#collector-types-g1-vs-zgc-vs-parallel)
4. [🧪 Scenario-Based Troubleshooting (The "Police vs. Crime")](#scenario-based-troubleshooting)
5. [📡 Production Tuning & JVM Flags](#production-tuning--jvm-flags)

---

## The Memory Architecture (Heap vs. Stack)

Think of the JVM's memory as a house. The **Stack** is your personal workspace (temporary data for methods), while the **Heap** is the shared living area where long-term residents (Objects) stay. GC only cares about the **Heap**.

### The Generational Heap Layout
The Heap is divided into regions based on the "age" of objects:

| Region | Space Name | Description |
| :--- | :--- | :--- |
| **Young Gen** | **Eden Space** | Where objects are born. Most die here immediately. |
| **Young Gen** | **Survivor Spaces** | S0 and S1. Objects that survived a minor GC move here. |
| **Old (Tenured)** | **Old Gen** | If an object survives enough GC cycles, it is "promoted" here. |
| **Metaspace** | **Metaspace** | Stores class blueprints (metadata). Lives outside the heap. |

> [!TIP]
> **Performance Insight:** Minor GCs (Young Gen) are fast and common. Major GCs (Old Gen) are slow and should be rare. If the Old Gen fills up, you get a **Stop-the-World (STW)** pause.

---

## The GC Lifecycle: Mark, Sweep, Compact

The "Robot Vacuum" follows three strict rules:

1.  **Marking:** The GC starts at "GC Roots" (active threads, static vars) and follows all paths. If it can't find a path to an object, that object is marked as **trash**.
2.  **Sweeping:** It deletes the trash to free up memory blocks.
3.  **Compacting:** It moves the remaining "live" objects together to eliminate gaps (fragmentation), leaving a single large block of free space.

---

## Collector Types (G1 vs. ZGC vs. Parallel)

| Collector | Best For... | Key Strength |
| :--- | :--- | :--- |
| **G1 GC** (Default) | Microservices | The modern standard. Balances throughput and latency. |
| **Parallel GC** | Batch Processing | Maximizes throughput. Great for data science/heavy math. |
| **ZGC / Shenandoah** | Real-time APIs | **Low-latency (< 1ms pauses)** even for terabyte-sized heaps. |
| **Serial GC** | Small CLI Tools | Single-threaded. For when overhead matters more than speed. |

---

## Scenario-Based Troubleshooting

### Scenario 1: The "Eternal List" (Memory Leak)
*   **The Problem:** App works fine for 2 days, then crashes with `OutOfMemoryError`.
*   **The Description:** In VisualVM, you see the **"Infinite Staircase"** pattern. The memory "floor" keeps rising after every GC.
*   **Why it happens:** You're adding objects to a **Static Collection** (like a cache) but never removing them.
*   **The Fix:** Use a `WeakHashMap` or set a max size with a removal policy.
*   **Example Code:**
    ```java
    // FIX: Automatically drops old entries if size > 100
    private static Map<String, Object> cache = new LinkedHashMap<>() {
        protected boolean removeEldestEntry(Map.Entry eldest) { return size() > 100; }
    };
    ```

### Scenario 2: The "Death by a Thousand Cuts" (STW Pauses)
*   **The Problem:** Users complain about 3-second "lag spikes."
*   **The Description:** GC logs show multiple **Full GC** events lasting several seconds.
*   **Why it happens:** Your heap is too small, forcing the JVM to stop everything and do "heavy cleaning" too often.
*   **The Fix:** Increase the max heap size and use the G1 collector.
*   **JVM Flag:** `-Xmx4g -XX:+UseG1GC`

---

## Production Tuning & JVM Flags

Apply the **"Gold Standard"** flags for 2026 production systems:

```bash
# 1. Memory Setup (Start and Max same to avoid resizing overhead)
-Xms4g -Xmx4g

# 2. Collector Choice
-XX:+UseG1GC

# 3. Latency Target (Try keeping pauses under 100ms)
-XX:MaxGCPauseMillis=100

# 4. Safety & Diagnostics
-Xlog:gc*:file=gc.log                     # Record every "cleaning" cycle
-XX:+HeapDumpOnOutOfMemoryError           # Save "Autopsy" file if it crashes
-XX:HeapDumpPath=./java_pid.hprof         # Path to the dump
```

---

## Final Workflow: The "GC Autopsy"
1.  **Monitor:** Watch the "Sawtooth" in VisualVM.
2.  **Dump:** Right-click the app in VisualVM and select **"Heap Dump"** when memory is high.
3.  **Analyze:** Find the **"Path to GC Root."** If it leads to a `static` field, you've found the leak.
4.  **Tweak:** Apply the correct JVM flags to "smooth out" the cleaning cycles.

---

[⬆️ Back to Top](#🧹-java-garbage-collection-mastery-the-cleaning-engine)
