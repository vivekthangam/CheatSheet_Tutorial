package com.cheatsheet.collections.designlab;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.LongStream;
import java.util.stream.Stream;

/**
 * Architectural Anti-Patterns & Refactorings in Collections & Streams (DesignLab)
 */
public class CollectionsDesignLab {

    // =========================================================================
    // ANTI-PATTERN 1: Boxed Streams for Massive Numeric Aggregations
    // =========================================================================

    public static class NumericStreamOptimization {
        // ⚠️ ANTI-PATTERN: Boxed Long stream allocates heap wrapper objects on every element
        public long vulnerableBoxedSum(long count) {
            return Stream.iterate(1L, i -> i + 1)
                    .limit(count)
                    .reduce(0L, Long::sum);
        }

        // 🏗️ REFACTOR: Primitive LongStream executed with zero heap object allocations
        public long hardenedPrimitiveSum(long count) {
            return LongStream.rangeClosed(1, count).sum();
        }
    }

    // =========================================================================
    // ANTI-PATTERN 2: LinkedList Heap Fragmentation vs Contiguous ArrayDeque
    // =========================================================================

    public static class QueueMemoryLocality {
        // ⚠️ ANTI-PATTERN: LinkedList incurs 24-byte node allocations + pointer indirection per element
        public List<String> createLinkedListQueue() {
            return new LinkedList<>();
        }

        // 🏗️ REFACTOR: ArrayDeque allocates contiguous ring buffer with spatial cache locality
        public Deque<String> createContiguousRingBuffer(int initialCapacity) {
            return new ArrayDeque<>(initialCapacity);
        }
    }
}
