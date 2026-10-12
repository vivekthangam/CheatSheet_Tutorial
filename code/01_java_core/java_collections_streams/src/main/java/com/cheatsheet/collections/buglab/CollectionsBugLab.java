package com.cheatsheet.collections.buglab;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Real-World Outage Bugs, Anti-Patterns & Hardened Fixes in Collections & Streams
 */
public class CollectionsBugLab {

    // =========================================================================
    // BUG 1: The Cyclic Pointer Loop in Non-Thread-Safe LinkedHashMap
    // =========================================================================

    public static class LinkedHashMapConcurrencyBug {
        // ❌ BUG: Access-order LinkedHashMap mutates doubly-linked pointers without synchronization
        private final Map<String, String> vulnerableCache = new LinkedHashMap<>(16, 0.75f, true);

        public String vulnerableGet(String key) {
            return vulnerableCache.get(key); // Multithreaded calls mutate before/after links simultaneously!
        }

        // ✅ REFACTORED: Guard with ReentrantReadWriteLock write lock
        private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

        public String hardenedGet(String key) {
            rwLock.writeLock().lock(); // Write lock is required because access-order mutates ribbon links!
            try {
                return vulnerableCache.get(key);
            } finally {
                rwLock.writeLock().unlock();
            }
        }

        public void put(String key, String value) {
            rwLock.writeLock().lock();
            try {
                vulnerableCache.put(key, value);
            } finally {
                rwLock.writeLock().unlock();
            }
        }
    }

    // =========================================================================
    // BUG 2: JVM CommonPool Starvation via Parallel Streams
    // =========================================================================

    public static class ParallelStreamStarvationBug {
        // ❌ BUG: Blocking I/O inside parallelStream consumes shared global ForkJoinPool.commonPool()
        public List<String> vulnerableProcess(List<String> items) {
            return items.parallelStream()
                    .map(item -> simulateBlockingCall(item))
                    .toList();
        }

        // ✅ REFACTORED: Isolate stream execution inside a dedicated bounded ForkJoinPool
        private final ForkJoinPool isolatedPool = new ForkJoinPool(4);

        public List<String> hardenedProcess(List<String> items) {
            try {
                return isolatedPool.submit(() ->
                        items.parallelStream()
                                .map(this::simulateBlockingCall)
                                .toList()
                ).get();
            } catch (Exception e) {
                throw new RuntimeException("Stream processing failed", e);
            }
        }

        private String simulateBlockingCall(String input) {
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "PROCESSED_" + input;
        }
    }

    // =========================================================================
    // BUG 3: ConcurrentHashMap Non-Atomic Check-Then-Act Race Condition
    // =========================================================================

    public static class ConcurrentHashMapNonAtomicBug {
        private final ConcurrentHashMap<String, Integer> inventory = new ConcurrentHashMap<>();

        // ❌ BUG: Check-then-act is not atomic; race condition causes overselling!
        public boolean vulnerableDecrement(String sku, int quantity) {
            if (inventory.containsKey(sku)) {
                int current = inventory.get(sku);
                if (current >= quantity) {
                    inventory.put(sku, current - quantity);
                    return true;
                }
            }
            return false;
        }

        // ✅ REFACTORED: Atomic compute() ensuring lock-free CAS atomicity
        public boolean hardenedDecrement(String sku, int quantity) {
            var result = inventory.compute(sku, (k, current) -> {
                if (current == null || current < quantity) {
                    return current; // Not enough stock or missing
                }
                return current - quantity;
            });
            return result != null;
        }

        public void initStock(String sku, int stock) {
            inventory.put(sku, stock);
        }

        public int getStock(String sku) {
            return inventory.getOrDefault(sku, 0);
        }
    }
}
