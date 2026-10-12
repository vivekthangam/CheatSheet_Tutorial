package com.cheatsheet.concurrency.designlab;

import java.util.concurrent.atomic.LongAdder;

/**
 * Architectural Anti-Patterns & Refactorings for Concurrency (DesignLab)
 */
public class ConcurrencyDesignLab {

    // =========================================================================
    // ANTI-PATTERN 1: False Sharing CPU Cache Line Ping-Pong Thrashing
    // =========================================================================

    public static class CacheLineContentionPattern {
        // ⚠️ ANTI-PATTERN: Adjacent volatile longs share the same 64-byte L1 cache line
        public static class ContendedMetrics {
            public volatile long metricA = 0;
            public volatile long metricB = 0;
        }

        // 🏗️ REFACTOR: LongAdder isolates cell increments onto separate cache lines
        public static class HardenedMetrics {
            public final LongAdder metricA = new LongAdder();
            public final LongAdder metricB = new LongAdder();
        }
    }

    // =========================================================================
    // ANTI-PATTERN 2: Double-Checked Locking Without volatile
    // =========================================================================

    public static class DoubleCheckedLockingPattern {
        public static class Resource {
            public final long creationTimestamp = System.currentTimeMillis();
        }

        // 🏗️ REFACTOR: Initialization-on-demand Holder Idiom (Thread-safe, lazy, zero lock contention)
        private static class Holder {
            static final Resource INSTANCE = new Resource();
        }

        public static Resource getHardenedInstance() {
            return Holder.INSTANCE;
        }
    }
}
