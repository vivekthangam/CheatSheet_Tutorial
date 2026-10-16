package com.cheatsheet.boot.starter.tenant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.HashMap;
import java.util.Map;

/**
 * Enterprise Multi-Tenant Configuration Properties.
 */
@Validated
@ConfigurationProperties(prefix = "enterprise.tenant")
public class TenantProperties {

    /** Whether enterprise multi-tenant auto-configuration is enabled. */
    private boolean enabled = true;

    /** Default fallback tenant key when no explicit header is present. */
    @NotBlank
    @Pattern(regexp = "^[a-zA-Z0-9_-]{3,30}$", message = "Tenant ID must be alphanumeric (3-30 chars)")
    private String defaultTenant = "primary";

    /** HTTP header used to propagate tenant identifier. */
    private String headerName = "X-Tenant-ID";

    /** Enforce strict mode: reject requests without valid tenant header with 400 Bad Request. */
    private boolean strictMode = false;

    /** Per-tenant connection pool configurations. */
    private Map<String, String> dataSources = new HashMap<>();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getDefaultTenant() {
        return defaultTenant;
    }

    public void setDefaultTenant(String defaultTenant) {
        this.defaultTenant = defaultTenant;
    }

    public String getHeaderName() {
        return headerName;
    }

    public void setHeaderName(String headerName) {
        this.headerName = headerName;
    }

    public boolean isStrictMode() {
        return strictMode;
    }

    public void setStrictMode(boolean strictMode) {
        this.strictMode = strictMode;
    }

    public Map<String, String> getDataSources() {
        return dataSources;
    }

    public void setDataSources(Map<String, String> dataSources) {
        this.dataSources = dataSources;
    }
}
