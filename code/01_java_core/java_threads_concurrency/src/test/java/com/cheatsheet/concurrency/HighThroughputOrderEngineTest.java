package com.cheatsheet.concurrency;

import com.cheatsheet.concurrency.buglab.ConcurrencyBugLab;
import com.cheatsheet.concurrency.designlab.ConcurrencyDesignLab;
import com.cheatsheet.concurrency.engine.HighThroughputOrderEngine;
import com.cheatsheet.concurrency.securitylab.ConcurrencySecurityLab;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class HighThroughputOrderEngineTest {

    @Test
    @DisplayName("Test RingBuffer: Publish and Poll Orders In FIFO Order")
    void testRingBufferPublishAndPoll() {
        try (HighThroughputOrderEngine engine = new HighThroughputOrderEngine(4)) {
            HighThroughputOrderEngine.Order o1 = new HighThroughputOrderEngine.Order(1, "AAPL", 150.0, 100);
            HighThroughputOrderEngine.Order o2 = new HighThroughputOrderEngine.Order(2, "GOOG", 2800.0, 50);

            assertTrue(engine.publishOrder(o1));
            assertTrue(engine.publishOrder(o2));

            HighThroughputOrderEngine.Order polled1 = engine.pollOrder();
            assertNotNull(polled1);
            assertEquals(1, polled1.orderId());

            HighThroughputOrderEngine.Order polled2 = engine.pollOrder();
            assertNotNull(polled2);
            assertEquals(2, polled2.orderId());

            assertNull(engine.pollOrder());
        }
    }

    @Test
    @DisplayName("Test RingBuffer Backpressure: Rejects When Capacity Exceeded")
    void testRingBufferBackpressure() {
        try (HighThroughputOrderEngine engine = new HighThroughputOrderEngine(2)) {
            HighThroughputOrderEngine.Order o1 = new HighThroughputOrderEngine.Order(1, "AAPL", 150.0, 100);
            HighThroughputOrderEngine.Order o2 = new HighThroughputOrderEngine.Order(2, "MSFT", 300.0, 200);
            HighThroughputOrderEngine.Order o3 = new HighThroughputOrderEngine.Order(3, "AMZN", 180.0, 300);

            assertTrue(engine.publishOrder(o1));
            assertTrue(engine.publishOrder(o2));
            assertFalse(engine.publishOrder(o3)); // Buffer full, backpressure triggered
        }
    }

    @Test
    @DisplayName("Test StampedLock: Optimistic Read & Update")
    void testStampedLockPriceValidation() {
        try (HighThroughputOrderEngine engine = new HighThroughputOrderEngine(4)) {
            assertEquals(100.0, engine.getMarketPrice());

            engine.updateMarketPrice(105.50);
            assertEquals(105.50, engine.getMarketPrice());
        }
    }

    @Test
    @DisplayName("Test Structured Concurrency: Execute Order Async with Loom")
    void testStructuredConcurrencyExecution() throws Exception {
        try (HighThroughputOrderEngine engine = new HighThroughputOrderEngine(4)) {
            HighThroughputOrderEngine.Order order = new HighThroughputOrderEngine.Order(10, "NVDA", 120.0, 500);
            var result = engine.executeOrderAsync(order);

            assertNotNull(result);
            assertEquals(order, result.order());
            assertEquals(100.0, result.price());
            assertTrue(result.fraudApproved());
        }
    }

    @Test
    @DisplayName("Test BugLab: Bounded ThreadPool Prevents OOM")
    void testBoundedPoolCreation() {
        ConcurrencyBugLab.UnboundedQueueOomBug bug = new ConcurrencyBugLab.UnboundedQueueOomBug();
        ExecutorService executor = bug.createHardenedPool();

        assertInstanceOf(ThreadPoolExecutor.class, executor);
        ThreadPoolExecutor tpe = (ThreadPoolExecutor) executor;
        assertEquals(5000, tpe.getQueue().remainingCapacity());
        assertInstanceOf(ThreadPoolExecutor.CallerRunsPolicy.class, tpe.getRejectedExecutionHandler());
        tpe.shutdown();
    }

    @Test
    @DisplayName("Test BugLab: StampedLock Spread Hardened Read")
    void testStampedLockSpreadHardened() {
        ConcurrencyBugLab.StampedLockTearingBug bug = new ConcurrencyBugLab.StampedLockTearingBug();
        double[] spread = bug.hardenedGetSpread();

        assertEquals(100.0, spread[0]);
        assertEquals(101.0, spread[1]);

        bug.updateSpread(102.0, 103.0);
        double[] updatedSpread = bug.hardenedGetSpread();
        assertEquals(102.0, updatedSpread[0]);
        assertEquals(103.0, updatedSpread[1]);
    }

    @Test
    @DisplayName("Test SecurityLab: ScopedValue Lifecycle Bound Execution")
    void testScopedValueExecution() {
        ConcurrencySecurityLab.TenantContextSecurity security = new ConcurrencySecurityLab.TenantContextSecurity();
        AtomicBoolean executed = new AtomicBoolean(false);

        security.hardenedExecute("tenant-prod-99", () -> {
            assertEquals("tenant-prod-99", ConcurrencySecurityLab.TenantContextSecurity.SECURE_TENANT_HOLDER.get());
            executed.set(true);
        });

        assertTrue(executed.get());
    }

    @Test
    @DisplayName("Test DesignLab: Initialization-on-demand Holder Idiom")
    void testHolderIdiomLazyInitialization() {
        var resource1 = ConcurrencyDesignLab.DoubleCheckedLockingPattern.getHardenedInstance();
        var resource2 = ConcurrencyDesignLab.DoubleCheckedLockingPattern.getHardenedInstance();

        assertNotNull(resource1);
        assertSame(resource1, resource2);
    }
}
