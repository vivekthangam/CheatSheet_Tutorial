package com.cheatsheet.jvm.buglab;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Real-World Outage Bugs, Anti-Patterns & Hardened Fixes in JVM Memory & GC
 */
public class JvmBugLab {

    // =========================================================================
    // BUG 1: Metaspace Exhaustion from Uncached Dynamic Class Generation
    // =========================================================================

    public static class MetaspaceDynamicClassLeak {
        private final AtomicInteger generatedCount = new AtomicInteger(0);

        // ❌ BUG: Emulated dynamic classloader churn allocating fresh metadata per request
        public String vulnerableGenerateClass(String scriptName) {
            generatedCount.incrementAndGet();
            return "ClassInstance@" + scriptName + "_" + System.nanoTime();
        }

        // ✅ REFACTORED: Cache compiled classes by signature to prevent Metaspace blowup
        private final ConcurrentHashMap<String, String> classCache = new ConcurrentHashMap<>();

        public String hardenedGetOrCompileClass(String scriptName) {
            return classCache.computeIfAbsent(scriptName, name -> "CachedClassInstance@" + name);
        }

        public int getCacheSize() {
            return classCache.size();
        }
    }

    // =========================================================================
    // BUG 2: Direct Memory Leak via Missing Reference Count Release
    // =========================================================================

    public static class DirectMemoryReferenceLeak {
        private final AtomicInteger activeLeases = new AtomicInteger(0);

        // ❌ BUG: Allocation without lifecycle guarantee
        public void vulnerableAllocatePacket() {
            activeLeases.incrementAndGet();
            // Dropped reference without decrementing!
        }

        // ✅ REFACTORED: Try-with-resources guarded lease
        public static class SafePacket implements AutoCloseable {
            private final AtomicInteger tracker;
            private boolean closed = false;

            public SafePacket(AtomicInteger tracker) {
                this.tracker = tracker;
                this.tracker.incrementAndGet();
            }

            @Override
            public void close() {
                if (!closed) {
                    closed = true;
                    tracker.decrementAndGet();
                }
            }
        }

        public SafePacket hardenedAllocatePacket() {
            return new SafePacket(activeLeases);
        }

        public int getActiveLeases() {
            return activeLeases.get();
        }
    }
}
