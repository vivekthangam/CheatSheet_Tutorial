package com.cheatsheet.concurrency.buglab;

import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.StampedLock;

/**
 * Real-World Concurrency Outages, Bugs & Fixes (BugLab)
 */
public class ConcurrencyBugLab {

    // =========================================================================
    // BUG 1: Virtual Thread Carrier Pinning under Synchronized Blocks
    // =========================================================================

    public static class CarrierPinningBug {
        // ❌ BUG: Synchronized block pins Virtual Thread to underlying Carrier Thread
        public synchronized String vulnerableBlockingFetch() throws InterruptedException {
            Thread.sleep(10); // Pins OS carrier thread!
            return "RESULT_PINNED";
        }

        // ✅ REFACTORED: ReentrantLock allows Virtual Thread unmounting on blocking calls
        private final ReentrantLock lock = new ReentrantLock();

        public String hardenedBlockingFetch() throws InterruptedException {
            lock.lock();
            try {
                Thread.sleep(10); // Safely unmounts continuation without carrier starvation!
                return "RESULT_UNMOUNTED";
            } finally {
                lock.unlock();
            }
        }
    }

    // =========================================================================
    // BUG 2: Unbounded Queue OutOfMemoryError in ThreadPoolExecutor
    // =========================================================================

    public static class UnboundedQueueOomBug {
        // ❌ BUG: FixedThreadPool creates unbounded LinkedBlockingQueue causing OOM
        public ExecutorService createVulnerablePool() {
            return Executors.newFixedThreadPool(64);
        }

        // ✅ REFACTORED: Bounded queue with CallerRunsPolicy backpressure
        public ExecutorService createHardenedPool() {
            return new ThreadPoolExecutor(
                    16, 64,
                    60L, TimeUnit.SECONDS,
                    new ArrayBlockingQueue<>(5000),
                    Executors.defaultThreadFactory(),
                    new ThreadPoolExecutor.CallerRunsPolicy()
            );
        }
    }

    // =========================================================================
    // BUG 3: StampedLock Multi-Field Optimistic Read State Tearing
    // =========================================================================

    public static class StampedLockTearingBug {
        private final StampedLock lock = new StampedLock();
        private double bidPrice = 100.0;
        private double askPrice = 101.0;

        // ❌ BUG: Leaks inconsistent spread because validate() is omitted
        public double[] vulnerableGetSpread() {
            long stamp = lock.tryOptimisticRead();
            double bid = this.bidPrice;
            double ask = this.askPrice; // Reader could observe half-updated state here
            return new double[]{bid, ask};
        }

        // ✅ REFACTORED: Validates stamp and safely falls back to pessimistic read lock
        public double[] hardenedGetSpread() {
            long stamp = lock.tryOptimisticRead();
            double bid = this.bidPrice;
            double ask = this.askPrice;
            if (!lock.validate(stamp)) {
                stamp = lock.readLock();
                try {
                    bid = this.bidPrice;
                    ask = this.askPrice;
                } finally {
                    lock.unlockRead(stamp);
                }
            }
            return new double[]{bid, ask};
        }

        public void updateSpread(double newBid, double newAsk) {
            long stamp = lock.writeLock();
            try {
                this.bidPrice = newBid;
                this.askPrice = newAsk;
            } finally {
                lock.unlockWrite(stamp);
            }
        }
    }
}
