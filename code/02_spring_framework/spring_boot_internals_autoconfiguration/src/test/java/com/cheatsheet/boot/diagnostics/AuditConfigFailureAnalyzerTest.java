package com.cheatsheet.boot.diagnostics;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.diagnostics.FailureAnalysis;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AuditConfigFailureAnalyzer Unit Tests")
class AuditConfigFailureAnalyzerTest {

    private final AuditConfigFailureAnalyzer analyzer = new AuditConfigFailureAnalyzer();

    @Test
    @DisplayName("Should extract diagnostic message and action from AuditConfigurationException")
    void shouldAnalyzeFailure() {
        AuditConfigurationException ex = new AuditConfigurationException(
                "Missing required table 'audit_log'",
                "cheatsheet.audit.database-table"
        );

        FailureAnalysis analysis = analyzer.analyze(ex);

        assertThat(analysis).isNotNull();
        assertThat(analysis.getDescription())
                .contains("Audit Auto-Configuration failed: Missing required table 'audit_log'")
                .contains("cheatsheet.audit.database-table");
        assertThat(analysis.getAction())
                .contains("Set 'cheatsheet.audit.enabled=false'")
                .contains("or verify the configuration property cheatsheet.audit.database-table");
    }
}
