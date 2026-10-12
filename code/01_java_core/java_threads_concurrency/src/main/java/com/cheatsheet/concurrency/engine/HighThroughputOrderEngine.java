package com.cheatsheet.concurrency.engine;

import org.springframework.stereotype.Service;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.StampedLock;

/**
 * Ultra-Low-Latency Order Engine utilizing Lock-Free CAS Ring Buffer,
 * StampedLock Optimistic Reads, and Project Loom Structured Concurrency.
 */
@Service
public class HighThroughputOrderEngine implements AutoCloseable {

    private final int bufferSize;
    private final int bufferMask;
    private final Order[] ringBuffer;
    private final AtomicLong writeCursor = new AtomicLong(0);
    private final AtomicLong readCursor = new AtomicLong(0);
    private final StampedLock priceBookLock = new StampedLock();
    private volatile double currentMarketPrice = 100.00;

    private final ExecutorService virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();

    public record Order(long orderId, String symbol, double price, int quantity) {}

    public HighThroughputOrderEngine() {
        this(1024); // Default power of two capacity
    }

    public HighThroughputOrderEngine(int capacityPowerOfTwo) {
        if (Integer.bitCount(capacityPowerOfTwo) != 1) {
            throw new IllegalArgumentException("Capacity must be a power of 2 for bitmask indexing");
        }
        this.bufferSize = capacityPowerOfTwo;
        this.bufferMask = capacityPowerOfTwo - 1;
        this.ringBuffer = new Order[bufferSize];
    }

    public boolean publishOrder(Order order) {
        long currentWrite = writeCursor.get();
        long currentRead = readCursor.get();

        if (currentWrite - currentRead >= bufferSize) {
            return false; // Ring buffer full: apply backpressure immediately
        }

        if (writeCursor.compareAndSet(currentWrite, currentWrite + 1)) {
            int slot = (int) (currentWrite & bufferMask);
            ringBuffer[slot] = order;
            return true;
        }
        return false;
    }

    public Order pollOrder() {
        long currentRead = readCursor.get();
        long currentWrite = writeCursor.get();

        if (currentRead >= currentWrite) {
            return null; // Empty ring buffer
        }

        if (readCursor.compareAndSet(currentRead, currentRead + 1)) {
            int slot = (int) (currentRead & bufferMask);
            Order item = ringBuffer[slot];
            ringBuffer[slot] = null; // GC eligible
            return item;
        }
        return null;
    }

    // ⚡ Optimistic Read Validation with StampedLock (Lock-Free Path)
    public double getMarketPrice() {
        long stamp = priceBookLock.tryOptimisticRead();
        double price = this.currentMarketPrice;
        if (!priceBookLock.validate(stamp)) {
            // Optimistic read invalidated by concurrent writer: Fallback to Pessimistic Read Lock
            stamp = priceBookLock.readLock();
            try {
                price = this.currentMarketPrice;
            } finally {
                priceBookLock.unlockRead(stamp);
            }
        }
        return price;
    }

    public void updateMarketPrice(double newPrice) {
        long stamp = priceBookLock.writeLock();
        try {
            this.currentMarketPrice = newPrice;
        } finally {
            priceBookLock.unlockWrite(stamp);
        }
    }

    // 🚀 Structured Concurrency Pipeline (Java 21+)
    public record OrderExecutionResult(Order order, double price, boolean fraudApproved) {}

    public OrderExecutionResult executeOrderAsync(Order order) throws Exception {
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            StructuredTaskScope.Subtask<Double> priceTask = scope.fork(this::getMarketPrice);
            StructuredTaskScope.Subtask<Boolean> riskTask = scope.fork(() -> verifyRiskWithVirtualThread(order));

            scope.join();           // Concurrently wait for subtasks
            scope.throwIfFailed();  // Propagate fast if any task encounters failure

            return new OrderExecutionResult(order, priceTask.get(), riskTask.get());
        }
    }

    private boolean verifyRiskWithVirtualThread(Order order) throws InterruptedException {
        // Virtual threads unmount safely on blocking operations without carrier thread pinning
        Thread.sleep(5);
        return order.quantity() <= 10_000;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public long getPublishedCount() {
        return writeCursor.get();
    }

    public long getConsumedCount() {
        return readCursor.get();
    }

    @Override
    public void close() {
        virtualThreadExecutor.shutdown();
    }
}
