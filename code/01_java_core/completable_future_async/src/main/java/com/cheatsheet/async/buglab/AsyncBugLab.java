package com.cheatsheet.async.buglab;

import java.util.concurrent.*;

/**
 * Real-World Outage Bugs, Anti-Patterns & Hardened Fixes in CompletableFuture & Async
 */
public class AsyncBugLab {

    // =========================================================================
    // BUG 1: Double-Wrapping CompletionException Unwrapping
    // =========================================================================

    public static class CustomBusinessException extends RuntimeException {
        public CustomBusinessException(String message) {
            super(message);
        }
    }

    public static class ExceptionUnwrapper {
        // ❌ BUG: Direct check fails when wrapped in CompletionException or ExecutionException
        public static boolean vulnerableIsBusinessException(Throwable t) {
            return t instanceof CustomBusinessException; // Always false if wrapped!
        }

        // ✅ REFACTORED: Recursively unwrap CompletionException / ExecutionException
        public static Throwable unwrapRootCause(Throwable throwable) {
            Throwable current = throwable;
            while ((current instanceof CompletionException || current instanceof ExecutionException)
                    && current.getCause() != null) {
                current = current.getCause();
            }
            return current;
        }

        public static boolean hardenedIsBusinessException(Throwable t) {
            return unwrapRootCause(t) instanceof CustomBusinessException;
        }
    }

    // =========================================================================
    // BUG 2: Context Propagation Across Async Boundaries
    // =========================================================================

    public static class SecurityContextHolder {
        private static final ThreadLocal<String> CONTEXT = new ThreadLocal<>();

        public static void set(String user) {
            CONTEXT.set(user);
        }

        public static String get() {
            return CONTEXT.get();
        }

        public static void clear() {
            CONTEXT.remove();
        }
    }

    public static class ContextPropagator {
        // ✅ REFACTORED: Wrap supplier to capture and restore context
        public static <T> java.util.function.Supplier<T> wrapWithContext(java.util.function.Supplier<T> supplier) {
            String captured = SecurityContextHolder.get();
            return () -> {
                String prev = SecurityContextHolder.get();
                try {
                    SecurityContextHolder.set(captured);
                    return supplier.get();
                } finally {
                    if (prev != null) {
                        SecurityContextHolder.set(prev);
                    } else {
                        SecurityContextHolder.clear();
                    }
                }
            };
        }
    }
}
