package com.cheatsheet.jvm.securitylab;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Security Vulnerabilities & Mitigations in Native Memory & JVM (SecurityLab)
 */
public class JvmSecurityLab {

    // =========================================================================
    // VULNERABILITY 1: Direct Memory Residual Data Leak (Unzeroed Buffers)
    // =========================================================================

    public static class DirectMemorySanitizer {
        // 🚨 VULNERABILITY: Discarding buffer without wiping leaves sensitive tokens in RAM
        public void vulnerableDiscardToken(byte[] secretBytes) {
            // Memory left dirty for garbage collector or off-heap scanners!
        }

        // 🛡️ MITIGATION: Deterministically zero out memory before dereferencing
        public void hardenedZeroMemory(ByteBuffer directBuffer) {
            directBuffer.clear();
            while (directBuffer.hasRemaining()) {
                directBuffer.put((byte) 0);
            }
        }

        public void hardenedZeroByteArray(byte[] secret) {
            Arrays.fill(secret, (byte) 0);
        }
    }

    // =========================================================================
    // VULNERABILITY 2: Unbounded Off-Heap Memory DoS Protection
    // =========================================================================

    public static class DirectMemoryAllocationGuard {
        public static final int MAX_ALLOCATION_LIMIT = 5 * 1024 * 1024; // 5MB limit

        // 🛡️ MITIGATION: Validate requested off-heap buffer size to prevent host RAM starvation
        public ByteBuffer allocateSafeDirect(int requestedBytes) {
            if (requestedBytes <= 0 || requestedBytes > MAX_ALLOCATION_LIMIT) {
                throw new IllegalArgumentException("Requested buffer size exceeds security limit: " + requestedBytes);
            }
            return ByteBuffer.allocateDirect(requestedBytes);
        }
    }
}
