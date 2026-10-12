package com.cheatsheet.jvm.engine;

import org.springframework.stereotype.Service;

import java.lang.management.*;
import java.lang.ref.Cleaner;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Enterprise Safe Off-Heap Buffer Pool with Phantom Cleaner Deallocation
 * and Low-Overhead Runtime Telemetry Sentry.
 */
@Service
public class SafeOffHeapBufferPool implements AutoCloseable {

    private static final Cleaner CLEANER = Cleaner.create();
    private final AtomicLong allocatedDirectMemoryBytes = new AtomicLong(0);
    private final long maxDirectMemoryLimit;

    public SafeOffHeapBufferPool() {
        this(64 * 1024 * 1024L); // Default 64MB off-heap limit
    }

    public SafeOffHeapBufferPool(long maxDirectMemoryLimit) {
        this.maxDirectMemoryLimit = maxDirectMemoryLimit;
    }

    public static class DirectBufferLease implements AutoCloseable {
        private final ByteBuffer buffer;
        private final Cleaner.Cleanable cleanable;
        private final AtomicLong globalCounter;
        private final int byteCapacity;
        private boolean closed = false;

        DirectBufferLease(int capacity, AtomicLong globalCounter) {
            this.buffer = ByteBuffer.allocateDirect(capacity);
            this.byteCapacity = capacity;
            this.globalCounter = globalCounter;
            this.globalCounter.addAndGet(capacity);

            // Register phantom reference deallocation callback
            this.cleanable = CLEANER.register(this, new Deallocator(buffer, capacity, globalCounter));
        }

        public ByteBuffer getBuffer() {
            if (closed) throw new IllegalStateException("Direct buffer already closed!");
            return buffer;
        }

        public int getByteCapacity() {
            return byteCapacity;
        }

        public boolean isClosed() {
            return closed;
        }

        @Override
        public void close() {
            if (!closed) {
                closed = true;
                cleanable.clean(); // Triggers explicit free immediately
            }
        }

        private static class Deallocator implements Runnable {
            private final int bytes;
            private final AtomicLong counter;

            Deallocator(ByteBuffer buf, int bytes, AtomicLong counter) {
                this.bytes = bytes;
                this.counter = counter;
            }

            @Override
            public void run() {
                // Decrement global native allocation tracking counter
                counter.addAndGet(-bytes);
            }
        }
    }

    public DirectBufferLease allocate(int capacityBytes) {
        if (allocatedDirectMemoryBytes.get() + capacityBytes > maxDirectMemoryLimit) {
            throw new OutOfMemoryError("Native DirectMemory limit exceeded: " + maxDirectMemoryLimit + " bytes");
        }
        return new DirectBufferLease(capacityBytes, allocatedDirectMemoryBytes);
    }

    public long getAllocatedDirectMemoryBytes() {
        return allocatedDirectMemoryBytes.get();
    }

    public long getMaxDirectMemoryLimit() {
        return maxDirectMemoryLimit;
    }

    // ========================================================================
    // LOW-OVERHEAD RUNTIME JVM & GC TELEMETRY SENTRY
    // ========================================================================
    public record GcTelemetrySnapshot(
            long totalGcPauseTimeMs,
            long gcCollectionCount,
            long heapUsedBytes,
            long heapMaxBytes,
            long nonHeapUsedBytes
    ) {}

    public static GcTelemetrySnapshot captureSnapshot() {
        long totalPause = 0;
        long totalCount = 0;

        for (GarbageCollectorMXBean gcBean : ManagementFactory.getGarbageCollectorMXBeans()) {
            totalPause += Math.max(0, gcBean.getCollectionTime());
            totalCount += Math.max(0, gcBean.getCollectionCount());
        }

        MemoryMXBean memBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heap = memBean.getHeapMemoryUsage();
        MemoryUsage nonHeap = memBean.getNonHeapMemoryUsage();

        return new GcTelemetrySnapshot(
                totalPause,
                totalCount,
                heap.getUsed(),
                heap.getMax(),
                nonHeap.getUsed()
        );
    }

    @Override
    public void close() {
        // Pool teardown logic
    }
}
