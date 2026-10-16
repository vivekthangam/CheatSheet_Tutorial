package com.cheatsheet.boot.diagnostics;

import org.springframework.boot.diagnostics.AbstractFailureAnalyzer;
import org.springframework.boot.diagnostics.FailureAnalysis;

/**
 * Custom FailureAnalyzer that intercepts MissingTenantConfigurationException
 * during bootstrap and formats a clean, actionable error message in the console
 * instead of an unreadable 50-line stack trace.
 *
 * Registered in META-INF/spring.factories under org.springframework.boot.diagnostics.FailureAnalyzer
 */
public class MissingTenantFailureAnalyzer extends AbstractFailureAnalyzer<MissingTenantConfigurationException> {

    @Override
    protected FailureAnalysis analyze(Throwable rootFailure, MissingTenantConfigurationException cause) {
        String description = String.format(
                "Tenant configuration failure: Tenant '%s' is missing mandatory property '%s'. Details: %s",
                cause.getTenantId(), cause.getMissingProperty(), cause.getMessage()
        );

        String action = String.format(
                "1. Add the missing property to application.yml:\n" +
                "   multitenancy:\n" +
                "     tenants:\n" +
                "       %s:\n" +
                "         %s: <value>\n" +
                "2. Or disable multi-tenancy datasource routing by setting multitenancy.datasource-routing=false.",
                cause.getTenantId(), cause.getMissingProperty()
        );

        return new FailureAnalysis(description, action, cause);
    }
}
