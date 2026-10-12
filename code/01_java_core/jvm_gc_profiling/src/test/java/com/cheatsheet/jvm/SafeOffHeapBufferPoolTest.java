package com.cheatsheet.jvm;

import com.cheatsheet.jvm.buglab.JvmBugLab;
import com.cheatsheet.jvm.designlab.JvmDesignLab;
import com.cheatsheet.jvm.engine.SafeOffHeapBufferPool;
import com.cheatsheet.jvm.securitylab.JvmSecurityLab;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SafeOffHeapBufferPoolTest {

    @Test
    @DisplayName("Test Safe Off-Heap Buffer Allocation & Explicit Cleaner Release")
    void testOffHeapAllocationAndRelease() {
        long limit = 1024 * 1024; // 1MB limit
        SafeOffHeapBufferPool pool = new SafeOffHeapBufferPool(limit);

        assertEquals(0, pool.getAllocatedDirectMemoryBytes());

        try (SafeOffHeapBufferPool.DirectBufferLease lease = pool.allocate(256 * 1024)) {
            assertEquals(256 * 1024, pool.getAllocatedDirectMemoryBytes());
            assertNotNull(lease.getBuffer());
            assertFalse(lease.isClosed());

            // Write and read data off-heap
            lease.getBuffer().putInt(42);
            lease.getBuffer().flip();
            assertEquals(42, lease.getBuffer().getInt());
        }

        // After close, tracked direct memory must return to 0
        assertEquals(0, pool.getAllocatedDirectMemoryBytes());
    }

    @Test
    @DisplayName("Test Off-Heap Buffer Exceeding Max Limit Throws OutOfMemoryError")
    void testDirectMemoryLimitExceeded() {
        SafeOffHeapBufferPool pool = new SafeOffHeapBufferPool(500);

        assertThrows(OutOfMemoryError.class, () -> pool.allocate(1000));
    }

    @Test
    @DisplayName("Test Runtime GC Telemetry Snapshot Capture")
    void testGcTelemetrySnapshot() {
        var snapshot = SafeOffHeapBufferPool.captureSnapshot();

        assertNotNull(snapshot);
        assertTrue(snapshot.heapMaxBytes() > 0);
        assertTrue(snapshot.heapUsedBytes() > 0);
        assertTrue(snapshot.nonHeapUsedBytes() > 0);
    }

    @Test
    @DisplayName("Test BugLab: Dynamic Class Cache Prevents Metaspace Churn")
    void testMetaspaceDynamicClassLeakFix() {
        JvmBugLab.MetaspaceDynamicClassLeak fixer = new JvmBugLab.MetaspaceDynamicClassLeak();

        String class1 = fixer.hardenedGetOrCompileClass("DynamicScriptA");
        String class2 = fixer.hardenedGetOrCompileClass("DynamicScriptA");

        assertSame(class1, class2);
        assertEquals(1, fixer.getCacheSize());
    }

    @Test
    @DisplayName("Test BugLab: Direct Memory Leak Prevented via AutoCloseable")
    void testDirectMemoryReferenceLeakFix() {
        JvmBugLab.DirectMemoryReferenceLeak fixer = new JvmBugLab.DirectMemoryReferenceLeak();

        try (var packet = fixer.hardenedAllocatePacket()) {
            assertEquals(1, fixer.getActiveLeases());
        }

        assertEquals(0, fixer.getActiveLeases());
    }

    @Test
    @DisplayName("Test SecurityLab: Direct Memory Sanitizer Overwrite")
    void testDirectMemorySanitizer() {
        JvmSecurityLab.DirectMemorySanitizer sanitizer = new JvmSecurityLab.DirectMemorySanitizer();
        ByteBuffer buf = ByteBuffer.allocateDirect(16);
        buf.put("sensitivePasswd!".getBytes());

        sanitizer.hardenedZeroMemory(buf);

        buf.clear();
        while (buf.hasRemaining()) {
            assertEquals(0, buf.get());
        }
    }

    @Test
    @DisplayName("Test SecurityLab: Direct Memory Guard Blocks Unbounded Allocations")
    void testDirectMemoryGuard() {
        JvmSecurityLab.DirectMemoryAllocationGuard guard = new JvmSecurityLab.DirectMemoryAllocationGuard();

        assertThrows(IllegalArgumentException.class, () -> guard.allocateSafeDirect(100 * 1024 * 1024));
        assertNotNull(guard.allocateSafeDirect(1024));
    }

    @Test
    @DisplayName("Test DesignLab: Container Memory Sizing Calculation")
    void testContainerMemoryBudget() {
        JvmDesignLab.ContainerMemoryBudgetCalculator calc = new JvmDesignLab.ContainerMemoryBudgetCalculator();
        long heap = 4L * 1024 * 1024 * 1024; // 4GB
        var budget = calc.computeSafeContainerBudget(heap);

        assertTrue(budget.totalContainerLimitBytes() > heap);
        assertTrue(budget.nativeBufferBytes() > 0);
    }
}
