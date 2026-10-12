package com.cheatsheet.jvm.designlab;

import java.util.List;

/**
 * Architectural Anti-Patterns & Refactorings in JVM & Memory Allocation (DesignLab)
 */
public class JvmDesignLab {

    // =========================================================================
    // ANTI-PATTERN 1: Explicit System.gc() Freezing Hot Threads
    // =========================================================================

    public static class ExplicitGcAntiPattern {
        // ⚠️ ANTI-PATTERN: Explicit invocation causes full Stop-The-World pause
        public void vulnerablePostBatchCleanup(List<String> records) {
            records.clear();
            System.gc(); // Freezes all threads!
        }

        // 🏗️ REFACTOR: Let generational collector manage reclamation asynchronously
        public void hardenedPostBatchCleanup(List<String> records) {
            records.clear(); // Simply clear references without invoking STW GC!
        }
    }

    // =========================================================================
    // ANTI-PATTERN 2: Container Memory Limit Mismatch
    // =========================================================================

    public static class ContainerMemoryBudgetCalculator {
        public record ContainerBudget(long heapBytes, long nativeBufferBytes, long totalContainerLimitBytes) {}

        // 🏗️ REFACTOR: Compute safe container memory sizing with 30% native headroom
        public ContainerBudget computeSafeContainerBudget(long desiredHeapBytes) {
            long nativeHeadroom = (long) (desiredHeapBytes * 0.35) + (256 * 1024 * 1024L); // 35% + 256MB metaspace/stacks
            long totalLimit = desiredHeapBytes + nativeHeadroom;
            return new ContainerBudget(desiredHeapBytes, nativeHeadroom, totalLimit);
        }
    }
}
