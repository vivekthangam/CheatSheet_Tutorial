package com.cheatsheet.boot.diagnostics;

/**
 * Custom runtime exception thrown when tenant configuration is invalid or missing.
 */
public class MissingTenantConfigurationException extends RuntimeException {

    private final String tenantId;
    private final String missingProperty;

    public MissingTenantConfigurationException(String tenantId, String missingProperty, String message) {
        super(message);
        this.tenantId = tenantId;
        this.missingProperty = missingProperty;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getMissingProperty() {
        return missingProperty;
    }
}
