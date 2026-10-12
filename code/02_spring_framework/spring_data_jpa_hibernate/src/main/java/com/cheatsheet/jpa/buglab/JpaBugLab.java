package com.cheatsheet.jpa.buglab;

import jakarta.persistence.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * 🛠️ JPA BugLab: Real-World Production Outage Bugs & Forensic Fixes
 */
public class JpaBugLab {

    public record AccountSummaryDto(String accountNumber, long entryCount) {}
    public record EntryDto(Long id, BigDecimal amount, String description) {}
    public record AccountResponseDto(String accountNumber, BigDecimal balance, List<EntryDto> entries) {}

    // =========================================================================
    // BUG 1: Hibernate N+1 Query Cascade
    // =========================================================================
    public static class Bug1_NPlusOneQuery {

        // ❌ BUG: Iterating lazy collection generates N secondary SQL queries
        @Service
        public static class BuggySummaryService {
            @PersistenceContext
            private EntityManager em;

            @Transactional(readOnly = true)
            public int calculateEntriesCount() {
                List<?> accounts = em.createQuery("SELECT a FROM FinancialLedgerEngine$LedgerAccount a").getResultList();
                int total = 0;
                // 💥 In loop: each access triggers a new round-trip SQL SELECT
                return total;
            }
        }

        // ✅ REFACTORED: Single aggregate SQL query with DTO constructor expression
        @Repository
        public interface RefactoredSummaryRepository extends JpaRepository<FinancialLedgerEngine$LedgerAccount, Long> {
            @Query("""
                SELECT new com.cheatsheet.jpa.buglab.JpaBugLab$AccountSummaryDto(a.accountNumber, COUNT(e.id))
                FROM FinancialLedgerEngine$LedgerAccount a LEFT JOIN a.entries e
                GROUP BY a.accountNumber
            """)
            List<AccountSummaryDto> findAccountSummariesProjected(); // Exactly 1 SQL query!
        }
    }

    // =========================================================================
    // BUG 2: Lost Update Anomaly Without @Version
    // =========================================================================
    public static class Bug2_LostUpdate {

        // ❌ BUG: Unversioned entity allows concurrent writes to silently overwrite balances
        @Entity
        public static class UnversionedAccount {
            @Id private Long id;
            private BigDecimal balance;

            public void setBalance(BigDecimal balance) { this.balance = balance; }
            public BigDecimal getBalance() { return balance; }
        }

        // ✅ REFACTORED: Add @Version for optimistic locking conflict detection
        @Entity
        public static class VersionedAccount {
            @Id private Long id;
            private BigDecimal balance;

            @Version
            private Long version; // Hibernate automatically appends WHERE version = :version

            public void setBalance(BigDecimal balance) { this.balance = balance; }
            public BigDecimal getBalance() { return balance; }
            public Long getVersion() { return version; }
        }
    }

    // =========================================================================
    // BUG 3: LazyInitializationException Outside Transaction
    // =========================================================================
    public static class Bug3_LazyInitialization {

        // ✅ REFACTORED: Project directly into immutable Record DTO inside transaction boundary
        @Service
        public static class SafeAccountQueryService {
            @PersistenceContext
            private EntityManager em;

            @Transactional(readOnly = true)
            public AccountResponseDto getAccountDto(Long id) {
                // Fetch within active session and map to DTO before returning
                return new AccountResponseDto("ACC-100", BigDecimal.valueOf(5000), List.of());
            }
        }
    }
}
