package com.cheatsheet.collections.engine;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Enterprise Tiered Analytics Engine demonstrating Concurrent O(1) LRU Caching,
 * Zero-Allocation Primitive Streams, and Custom Batching Spliterators.
 */
@Service
public class TieredAnalyticsEngine<K, V> implements AutoCloseable {

    private final int maxCapacity;
    private final ConcurrentLRUCache<K, V> lruCache;
    private final ConcurrentHashMap<String, Long> aggregationMetrics = new ConcurrentHashMap<>();
    private final ForkJoinPool customStreamPool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());

    public TieredAnalyticsEngine() {
        this(500);
    }

    public TieredAnalyticsEngine(int maxCapacity) {
        this.maxCapacity = maxCapacity;
        this.lruCache = new ConcurrentLRUCache<>(maxCapacity);
    }

    public static class ConcurrentLRUCache<K, V> extends LinkedHashMap<K, V> {
        private final int capacity;
        private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

        public ConcurrentLRUCache(int capacity) {
            super(capacity, 0.75f, true); // true = access-order for LRU
            this.capacity = capacity;
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return size() > capacity;
        }

        public V getSafe(K key) {
            rwLock.writeLock().lock(); // write lock needed because access-order mutates ribbon links!
            try {
                return super.get(key);
            } finally {
                rwLock.writeLock().unlock();
            }
        }

        public void putSafe(K key, V value) {
            rwLock.writeLock().lock();
            try {
                super.put(key, value);
            } finally {
                rwLock.writeLock().unlock();
            }
        }

        public int sizeSafe() {
            rwLock.readLock().lock();
            try {
                return super.size();
            } finally {
                rwLock.readLock().unlock();
            }
        }
    }

    public void recordMetric(String category, long value) {
        // Atomic lock-free counter reduction across worker threads
        aggregationMetrics.merge(category, value, Long::sum);
    }

    public long getMetricTotal(String category) {
        return aggregationMetrics.getOrDefault(category, 0L);
    }

    // ⚡ Zero-Allocation Primitive Financial Analytics
    public record AnalyticsSummary(double min, double max, double average, long count) {}

    public AnalyticsSummary computePrimitiveStats(double[] tickPrices) {
        DoubleSummaryStatistics stats = DoubleStream.of(tickPrices)
                .summaryStatistics(); // Bypasses Double wrapper boxing entirely!

        return new AnalyticsSummary(stats.getMin(), stats.getMax(), stats.getAverage(), stats.getCount());
    }

    // 🚀 Custom Batching Spliterator to Prevent ForkJoin Task Over-Fragmentation
    public static class BatchSpliterator<T> implements Spliterator<List<T>> {
        private final Spliterator<T> source;
        private final int batchSize;

        public BatchSpliterator(Spliterator<T> source, int batchSize) {
            this.source = source;
            this.batchSize = batchSize;
        }

        @Override
        public boolean tryAdvance(java.util.function.Consumer<? super List<T>> action) {
            List<T> batch = new ArrayList<>(batchSize);
            for (int i = 0; i < batchSize && source.tryAdvance(batch::add); i++);
            if (batch.isEmpty()) return false;
            action.accept(batch);
            return true;
        }

        @Override
        public Spliterator<List<T>> trySplit() {
            Spliterator<T> split = source.trySplit();
            return split == null ? null : new BatchSpliterator<>(split, batchSize);
        }

        @Override
        public long estimateSize() {
            long sourceSize = source.estimateSize();
            return sourceSize == Long.MAX_VALUE ? Long.MAX_VALUE : (sourceSize + batchSize - 1) / batchSize;
        }

        @Override
        public int characteristics() {
            return source.characteristics() & ~(Spliterator.SIZED | Spliterator.SUBSIZED);
        }
    }

    public <T> Stream<List<T>> batchStream(List<T> source, int batchSize) {
        return StreamSupport.stream(new BatchSpliterator<>(source.spliterator(), batchSize), false);
    }

    public ConcurrentLRUCache<K, V> getLruCache() {
        return lruCache;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    @Override
    public void close() {
        customStreamPool.shutdown();
    }
}
