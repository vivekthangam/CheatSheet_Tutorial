package com.cheatsheet.jpa.engine;

import jakarta.persistence.*;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

public class FinancialLedgerEngine {

    // ============================================================================
    // 1. DOMAIN ENTITIES
    // ============================================================================
    @Entity
    @Table(name = "ledger_accounts", indexes = {
        @Index(name = "idx_account_number", columnList = "account_number", unique = true)
    })
    public static class LedgerAccount {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(name = "account_number", nullable = false, unique = true, length = 32)
        private String accountNumber;

        @Column(name = "currency", nullable = false, length = 3)
        private String currency;

        @Column(name = "balance", nullable = false, precision = 19, scale = 4)
        private BigDecimal balance;

        @Version
        @Column(name = "version", nullable = false)
        private Long version;

        @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
        @OrderBy("id ASC")
        private List<LedgerEntry> entries = new ArrayList<>();

        protected LedgerAccount() {}

        public LedgerAccount(String accountNumber, String currency, BigDecimal initialBalance) {
            this.accountNumber = accountNumber;
            this.currency = currency;
            this.balance = initialBalance;
        }

        public void postEntry(BigDecimal amount, String description) {
            this.balance = this.balance.add(amount);
            LedgerEntry entry = new LedgerEntry(this, amount, this.balance, description, Instant.now());
            this.entries.add(entry);
        }

        public Long getId() { return id; }
        public String getAccountNumber() { return accountNumber; }
        public BigDecimal getBalance() { return balance; }
        public Long getVersion() { return version; }
        public List<LedgerEntry> getEntries() { return Collections.unmodifiableList(entries); }
    }

    @Entity
    @Table(name = "ledger_entries", indexes = {
        @Index(name = "idx_account_id_id", columnList = "account_id, id")
    })
    public static class LedgerEntry {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "account_id", nullable = false)
        private LedgerAccount account;

        @Column(name = "amount", nullable = false, precision = 19, scale = 4)
        private BigDecimal amount;

        @Column(name = "post_balance", nullable = false, precision = 19, scale = 4)
        private BigDecimal postBalance;

        @Column(name = "description", nullable = false, length = 255)
        private String description;

        @Column(name = "created_at", nullable = false)
        private Instant createdAt;

        protected LedgerEntry() {}

        public LedgerEntry(LedgerAccount account, BigDecimal amount, BigDecimal postBalance, String description, Instant createdAt) {
            this.account = account;
            this.amount = amount;
            this.postBalance = postBalance;
            this.description = description;
            this.createdAt = createdAt;
        }

        public Long getId() { return id; }
        public BigDecimal getAmount() { return amount; }
        public BigDecimal getPostBalance() { return postBalance; }
        public String getDescription() { return description; }
        public Instant getCreatedAt() { return createdAt; }
    }

    // ============================================================================
    // 2. REPOSITORY INTERFACE
    // ============================================================================
    @Repository
    public interface LedgerAccountRepository extends JpaRepository<LedgerAccount, Long> {

        @EntityGraph(attributePaths = {"entries"})
        @Query("SELECT a FROM FinancialLedgerEngine$LedgerAccount a WHERE a.accountNumber = :accountNumber")
        Optional<LedgerAccount> findByAccountNumberWithEntries(@Param("accountNumber") String accountNumber);

        @Query("""
            SELECT e FROM FinancialLedgerEngine$LedgerEntry e
            WHERE e.account.id = :accountId AND e.id > :lastSeenId
            ORDER BY e.id ASC
        """)
        List<LedgerEntry> findNextEntriesKeyset(@Param("accountId") Long accountId,
                                                @Param("lastSeenId") Long lastSeenId,
                                                Pageable pageable);
    }

    // ============================================================================
    // 3. SERVICE TIER
    // ============================================================================
    @Service
    public static class FinancialLedgerService {

        private final LedgerAccountRepository accountRepo;
        private final SessionFactory sessionFactory;

        public FinancialLedgerService(LedgerAccountRepository accountRepo, SessionFactory sessionFactory) {
            this.accountRepo = accountRepo;
            this.sessionFactory = sessionFactory;
        }

        @Transactional
        public LedgerAccount recordTransaction(String accountNumber, BigDecimal amount, String description) {
            LedgerAccount account = accountRepo.findByAccountNumberWithEntries(accountNumber)
                    .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountNumber));

            account.postEntry(amount, description);
            return account;
        }

        public void bulkInsertLedgerEntries(List<LedgerEntry> entries) {
            try (StatelessSession statelessSession = sessionFactory.openStatelessSession()) {
                statelessSession.beginTransaction();
                for (LedgerEntry entry : entries) {
                    statelessSession.insert(entry);
                }
                statelessSession.getTransaction().commit();
            }
        }
    }
}
