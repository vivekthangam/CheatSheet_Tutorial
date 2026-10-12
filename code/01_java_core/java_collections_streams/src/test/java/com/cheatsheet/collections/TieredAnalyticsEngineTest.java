package com.cheatsheet.collections;

import com.cheatsheet.collections.buglab.CollectionsBugLab;
import com.cheatsheet.collections.designlab.CollectionsDesignLab;
import com.cheatsheet.collections.engine.TieredAnalyticsEngine;
import com.cheatsheet.collections.securitylab.CollectionsSecurityLab;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TieredAnalyticsEngineTest {

    @Test
    @DisplayName("Test Concurrent LRU Cache: Eviction on Maximum Capacity")
    void testLRUCacheEviction() {
        try (TieredAnalyticsEngine<String, String> engine = new TieredAnalyticsEngine<>(2)) {
            var cache = engine.getLruCache();

            cache.putSafe("k1", "v1");
            cache.putSafe("k2", "v2");
            assertEquals(2, cache.sizeSafe());

            // Access k1 to make k2 least recently used
            assertEquals("v1", cache.getSafe("k1"));

            // Insert k3 -> should evict k2
            cache.putSafe("k3", "v3");
            assertEquals(2, cache.sizeSafe());

            assertNotNull(cache.getSafe("k1"));
            assertNotNull(cache.getSafe("k3"));
            assertNull(cache.getSafe("k2")); // k2 was evicted!
        }
    }

    @Test
    @DisplayName("Test Primitive Analytics: Zero-Boxing Summary Statistics")
    void testPrimitiveSummaryStatistics() {
        try (TieredAnalyticsEngine<String, String> engine = new TieredAnalyticsEngine<>(10)) {
            double[] tickPrices = {100.5, 102.0, 99.5, 105.0, 103.0};
            var stats = engine.computePrimitiveStats(tickPrices);

            assertEquals(99.5, stats.min());
            assertEquals(105.0, stats.max());
            assertEquals(102.0, stats.average());
            assertEquals(5, stats.count());
        }
    }

    @Test
    @DisplayName("Test Batch Spliterator: Chunks Stream into Fixed Sized Batches")
    void testBatchSpliterator() {
        try (TieredAnalyticsEngine<String, String> engine = new TieredAnalyticsEngine<>(10)) {
            List<String> items = List.of("A", "B", "C", "D", "E", "F", "G");
            List<List<String>> batches = engine.batchStream(items, 3).toList();

            assertEquals(3, batches.size());
            assertEquals(List.of("A", "B", "C"), batches.get(0));
            assertEquals(List.of("D", "E", "F"), batches.get(1));
            assertEquals(List.of("G"), batches.get(2));
        }
    }

    @Test
    @DisplayName("Test BugLab: ConcurrentHashMap Atomic Compute Decrement")
    void testConcurrentHashMapAtomicDecrement() {
        CollectionsBugLab.ConcurrentHashMapNonAtomicBug inventory = new CollectionsBugLab.ConcurrentHashMapNonAtomicBug();
        inventory.initStock("SKU-100", 10);

        assertTrue(inventory.hardenedDecrement("SKU-100", 4));
        assertEquals(6, inventory.getStock("SKU-100"));

        assertTrue(inventory.hardenedDecrement("SKU-100", 6));
        assertEquals(0, inventory.getStock("SKU-100"));

        // Next decrement fails because stock is 0
        assertFalse(inventory.hardenedDecrement("SKU-100", 1));
    }

    @Test
    @DisplayName("Test BugLab: Hardened Parallel Stream Isolated Pool")
    void testHardenedParallelStream() {
        CollectionsBugLab.ParallelStreamStarvationBug processor = new CollectionsBugLab.ParallelStreamStarvationBug();
        List<String> input = List.of("task1", "task2");
        List<String> output = processor.hardenedProcess(input);

        assertEquals(2, output.size());
        assertTrue(output.contains("PROCESSED_task1"));
        assertTrue(output.contains("PROCESSED_task2"));
    }

    @Test
    @DisplayName("Test SecurityLab: HashDoS Comparable Key Treeification")
    void testHashDosComparableKey() {
        Map<CollectionsSecurityLab.SecureComparableKey, String> map = new HashMap<>();
        var k1 = new CollectionsSecurityLab.SecureComparableKey("api-key-alpha");
        var k2 = new CollectionsSecurityLab.SecureComparableKey("api-key-beta");

        map.put(k1, "secret-1");
        map.put(k2, "secret-2");

        assertEquals("secret-1", map.get(k1));
        assertEquals("secret-2", map.get(k2));
        assertTrue(k1.compareTo(k2) < 0);
    }

    @Test
    @DisplayName("Test SecurityLab: Immutable Snapshot Defense")
    void testImmutableSnapshot() {
        CollectionsSecurityLab.UnmodifiableWrapperLeak leak = new CollectionsSecurityLab.UnmodifiableWrapperLeak();
        leak.addRole("ADMIN");

        List<String> snapshot = leak.hardenedGetRoles();
        assertEquals(1, snapshot.size());

        // Modify underlying state
        leak.addRole("GUEST");

        // Hardened snapshot remains unchanged
        assertEquals(1, snapshot.size());
    }

    @Test
    @DisplayName("Test DesignLab: Primitive Stream Sum vs Boxed")
    void testPrimitiveStreamSum() {
        CollectionsDesignLab.NumericStreamOptimization opt = new CollectionsDesignLab.NumericStreamOptimization();
        long sum = opt.hardenedPrimitiveSum(100);
        assertEquals(5050, sum);
    }
}
