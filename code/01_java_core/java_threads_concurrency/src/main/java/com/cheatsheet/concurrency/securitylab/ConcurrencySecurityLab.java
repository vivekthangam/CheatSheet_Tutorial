package com.cheatsheet.concurrency.securitylab;

import java.util.concurrent.*;

/**
 * Real-World Concurrency Security Vulnerabilities & Mitigations (SecurityLab)
 */
public class ConcurrencySecurityLab {

    // =========================================================================
    // VULNERABILITY 1: ThreadLocal Cross-Tenant Context Contamination
    // =========================================================================

    public static class TenantContextSecurity {
        private static final ThreadLocal<String> VULNERABLE_TENANT_HOLDER = new ThreadLocal<>();
        public static final ScopedValue<String> SECURE_TENANT_HOLDER = ScopedValue.newInstance();

        // 🚨 VULNERABILITY: If an unhandled exception occurs, Tenant ID is not removed from worker thread
        public void vulnerableExecute(String tenantId, Runnable action) {
            VULNERABLE_TENANT_HOLDER.set(tenantId);
            action.run(); // Unhandled exception skips .remove()!
            VULNERABLE_TENANT_HOLDER.remove();
        }

        public String getVulnerableTenant() {
            return VULNERABLE_TENANT_HOLDER.get();
        }

        // 🛡️ MITIGATION: Use Java 21+ ScopedValue for immutable, lifecycle-bound isolation
        public void hardenedExecute(String tenantId, Runnable action) {
            ScopedValue.where(SECURE_TENANT_HOLDER, tenantId)
                       .run(action);
        }
    }

    // =========================================================================
    // VULNERABILITY 2: Shared ForkJoinPool.commonPool() DoS Starvation
    // =========================================================================

    public static class CommonPoolStarvationDefense {
        // 🚨 VULNERABILITY: Parallel stream blocks commonPool shared across entire JVM
        public void vulnerableParallelExecution(java.util.List<Runnable> tasks) {
            tasks.parallelStream().forEach(Runnable::run);
        }

        // 🛡️ MITIGATION: Isolate async computation into bounded dedicated executor
        private final ExecutorService isolatedExecutor = Executors.newFixedThreadPool(16);

        public CompletableFuture<Void> hardenedAsyncExecution(Runnable task) {
            return CompletableFuture.runAsync(task, isolatedExecutor);
        }
    }
}
