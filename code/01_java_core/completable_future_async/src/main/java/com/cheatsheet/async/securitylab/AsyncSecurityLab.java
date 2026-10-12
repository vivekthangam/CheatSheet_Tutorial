package com.cheatsheet.async.securitylab;

import java.util.concurrent.*;

/**
 * Security Vulnerabilities & Mitigations in Asynchronous Orchestration (SecurityLab)
 */
public class AsyncSecurityLab {

    // =========================================================================
    // VULNERABILITY 1: Unbounded Async Task Submission Denial-of-Service
    // =========================================================================

    public static class BoundedAsyncScheduler {
        private final ExecutorService securePool;

        public BoundedAsyncScheduler(int queueLimit) {
            this.securePool = new ThreadPoolExecutor(
                    2, 4, 30L, TimeUnit.SECONDS,
                    new ArrayBlockingQueue<>(queueLimit),
                    new ThreadPoolExecutor.AbortPolicy() // Throws RejectedExecutionException under overload
            );
        }

        // 🛡️ MITIGATION: Reject surplus requests when bounded queue is saturated
        public CompletableFuture<String> submitSecureTask(java.util.function.Supplier<String> supplier) {
            try {
                return CompletableFuture.supplyAsync(supplier, securePool);
            } catch (RejectedExecutionException e) {
                CompletableFuture<String> rejected = new CompletableFuture<>();
                rejected.completeExceptionally(new ResponseStatusException("Capacity exceeded, rejected due to backpressure"));
                return rejected;
            }
        }

        public void shutdown() {
            securePool.shutdown();
        }

        public static class ResponseStatusException extends RuntimeException {
            public ResponseStatusException(String msg) {
                super(msg);
            }
        }
    }

    // =========================================================================
    // VULNERABILITY 2: Sanitized Error Boundaries (Preventing Leakage in AltResult)
    // =========================================================================

    public static class SanitizedAsyncBoundary {
        // 🛡️ MITIGATION: Sanitize internal exception message before returning to clients
        public static CompletableFuture<String> sanitizeError(CompletableFuture<String> upstream) {
            return upstream.exceptionally(ex -> "ERR_UPSTREAM_UNAVAILABLE");
        }
    }
}
