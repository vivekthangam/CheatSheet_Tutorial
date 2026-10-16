package com.cheatsheet.boot.starter.tenant;

import org.slf4j.MDC;

/**
 * Thread-local context holder for multi-tenant identity.
 * Propagates tenant key into SLF4J MDC for unified distributed tracing.
 */
public final class TenantContextHolder {

    private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();
    public static final String MDC_KEY = "tenantId";

    private TenantContextHolder() {
        // Utility class
    }

    /**
     * Bind tenant to the current thread context and MDC.
     */
    public static void setTenantId(String tenantId) {
        if (tenantId == null || tenantId.isBlank()) {
            clear();
        } else {
            CURRENT_TENANT.set(tenantId);
            MDC.put(MDC_KEY, tenantId);
        }
    }

    /**
     * Retrieve tenant bound to the current thread.
     */
    public static String getTenantId() {
        return CURRENT_TENANT.get();
    }

    /**
     * Clear thread context and MDC to prevent thread pool leakage.
     */
    public static void clear() {
        CURRENT_TENANT.remove();
        MDC.remove(MDC_KEY);
    }

    /**
     * AutoCloseable scope for structured concurrency and safe resource cleanup.
     */
    public static TenantScope withTenant(String tenantId) {
        String previous = CURRENT_TENANT.get();
        setTenantId(tenantId);
        return new TenantScope(previous);
    }

    public static class TenantScope implements AutoCloseable {
        private final String previousTenant;

        TenantScope(String previousTenant) {
            this.previousTenant = previousTenant;
        }

        @Override
        public void close() {
            if (previousTenant != null) {
                setTenantId(previousTenant);
            } else {
                clear();
            }
        }
    }
}
