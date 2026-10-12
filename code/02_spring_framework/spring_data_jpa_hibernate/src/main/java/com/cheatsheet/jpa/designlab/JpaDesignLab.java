package com.cheatsheet.jpa.designlab;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * 🏗️ JPA DesignLab: Architectural Anti-Patterns & Refactorings
 */
public class JpaDesignLab {

    public record LedgerSummaryReport(String accountNumber, BigDecimal totalCredits, BigDecimal totalDebits) {}

    // =========================================================================
    // ARCH 1: Disabling OSIV (Open-Session-In-View) & Boundary Fetching
    // =========================================================================
    @Service
    public static class ReportGenerationService {

        // 🏗️ REFACTOR: Explicit transactional service boundary guarantees all relations loaded before view rendering
        @Transactional(readOnly = true)
        public LedgerSummaryReport generateReport(String accountNumber) {
            // Database connection acquired and released strictly inside this method when open-in-view=false
            return new LedgerSummaryReport(accountNumber, BigDecimal.valueOf(15000), BigDecimal.valueOf(10000));
        }
    }
}
