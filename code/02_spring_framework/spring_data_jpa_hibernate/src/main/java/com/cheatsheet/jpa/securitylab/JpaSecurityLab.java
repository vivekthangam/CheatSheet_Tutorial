package com.cheatsheet.jpa.securitylab;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 🛡️ JPA SecurityLab: Security Vulnerabilities & Hardened Mitigations
 */
public class JpaSecurityLab {

    public record UpdateProfileRequest(@NotBlank String displayName, @Email String alertEmail) {}

    // =========================================================================
    // SEC 1: SQL Injection via Native Query Concatenation
    // =========================================================================
    @Service
    public static class Sec1_SqlInjectionDefense {
        @PersistenceContext
        private EntityManager em;

        // 🚨 VULNERABILITY: Raw string concatenation in EntityManager native queries
        public List<?> searchEntriesUnsafe(String userDescription) {
            String sql = "SELECT * FROM ledger_entries WHERE description = '" + userDescription + "'";
            // 💥 Attacker inputs: "' OR 1=1 --" to extract all financial entries in the system!
            return em.createNativeQuery(sql).getResultList();
        }

        // 🛡️ MITIGATION: Use Typed Named Parameters with JPQL or CriteriaBuilder
        public List<?> searchEntriesSecure(String userDescription) {
            String jpql = "SELECT e FROM FinancialLedgerEngine$LedgerEntry e WHERE e.description = :desc";
            return em.createQuery(jpql)
                    .setParameter("desc", userDescription) // Driver escapes input safely
                    .getResultList();
        }
    }

    // =========================================================================
    // SEC 2: Mass Assignment & IDOR Defense
    // =========================================================================
    @Service
    public static class Sec2_MassAssignmentDefense {
        // 🛡️ MITIGATION: Bind to strict, immutable command Record DTO validating allowed fields
        public void updateAccountProfile(Long accountId, UpdateProfileRequest req) {
            // Only update allowed fields (displayName, alertEmail); protected fields (balance, version) untouched
            System.out.printf("Updated profile for account %d: %s (%s)%n", accountId, req.displayName(), req.alertEmail());
        }
    }
}
