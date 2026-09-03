[🏠 Back to Home](README.md)

# 🏛️ Spring Data JPA & Hibernate 6 Enterprise Architecture Master Guide

A production-grade engineering handbook for building robust, high-performance data persistence layers using **Spring Data JPA**, **Hibernate 6.x**, **Spring Boot 3.x**, and **Java 17/21**. Covers entity lifecycle states, solving the N+1 problem, pessimistic vs optimistic locking, JPA Specifications, and zero-leak auditing.

---

## 📑 Table of Contents
1. [🧠 Zero-to-Hero Mental Model: The Persistence Context & Dirty Checking](#-zero-to-hero-mental-model-the-persistence-context--dirty-checking)
2. [🗺️ 1. Entity Relationships: OneToMany, ManyToOne & Pitfalls](#️-1-entity-relationships-onetomany-manytoone--pitfalls)
3. [⚡ 2. The N+1 Query Problem & The 4 Proven Solutions](#-2-the-n1-query-problem--the-4-proven-solutions)
4. [🎯 3. High-Performance Projections: Records, DTOs & Interfaces](#-3-high-performance-projections-records-dtos--interfaces)
5. [📄 4. Pagination & Slicing: Page vs Slice vs Keysets](#-4-pagination--slicing-page-vs-slice-vs-keysets)
6. [🔒 5. Concurrency Control: Optimistic vs Pessimistic Locking](#-5-concurrency-control-optimistic-vs-pessimistic-locking)
7. [🔍 6. Dynamic Queries with JPA Specifications & Criteria API](#-6-dynamic-queries-with-jpa-specifications--criteria-api)
8. [🕒 7. Enterprise Auditing & Soft Deletes in Hibernate 6](#-7-enterprise-auditing--soft-deletes-in-hibernate-6)
9. [🏭 8. Production Scenarios & War Room Incident Forensics](#-8-production-scenarios--war-room-incident-forensics)
10. [⚖️ 9. Spring Data JPA Master Cheat Sheet](#️-9-spring-data-jpa-master-cheat-sheet)

---

## 🧠 Zero-to-Hero Mental Model: The Persistence Context & Dirty Checking

```
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                        TRANSACTION BOUNDARY (@Transactional)                           │
│                                                                                        │
│  1. DB Read ──> SQL SELECT ──> [ First-Level Cache (Persistence Context) ]             │
│                                      │                                                 │
│                                      ▼                                                 │
│                        Original Snapshot vs Managed Entity                             │
│                                      │                                                 │
│  2. Business Logic ──> entity.setStatus("APPROVED")  (No repo.save() needed!)          │
│                                      │                                                 │
│  3. Tx Commit ──> Dirty Checking detects modification                                 │
│                                      │                                                 │
│                                      ▼                                                 │
│  4. Flush ──────> SQL UPDATE emitted to Database                                       │
└────────────────────────────────────────────────────────────────────────────────────────┘
```

### Entity Lifecycle States
1. **`Transient / New`:** Object instantiated via `new Customer()` — not tracked by EntityManager, has no DB identity.
2. **`Managed`:** Loaded from database or passed to `entityManager.persist()`. Any change to its getters/setters will trigger an automatic `UPDATE` on transaction commit (**Dirty Checking**).
3. **`Detached`:** Transaction closed or `entityManager.clear()` called. The object exists in memory but modifications are not synced to the DB.
4. **`Removed`:** Scheduled for deletion on flush via `entityManager.remove()`.

> [!WARNING]
> Calling `repository.save(entity)` when the entity is already **Managed** within a `@Transactional` boundary is completely redundant! Hibernate automatically flushes changes via Dirty Checking at commit time.

---

## 🗺️ 1. Entity Relationships: OneToMany, ManyToOne & Pitfalls

### The Golden Rules of JPA Relationships
- **Always declare `@ManyToOne` as `FetchType.LAZY`!** By default, `@ManyToOne` and `@OneToOne` are `EAGER`, causing hidden join queries across your entire database.
- **Never use Lombok `@Data` or `@ToString` / `@EqualsAndHashCode` on circular bidirectional entities!** This triggers infinite recursive loops leading to `StackOverflowError`.
- **Bidirectional `@OneToMany` must specify `mappedBy` on the parent and manage both sides of the relationship helper methods.**

```java
package com.example.jpa.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String orderNumber;

    @Column(nullable = false)
    private String status;

    // ✅ Always LAZY on to-many collections
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrderItem> items = new ArrayList<>();

    // Helper synchronization methods to prevent desynchronized memory states
    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }

    public void removeItem(OrderItem item) {
        items.remove(item);
        item.setOrder(null);
    }

    // Getters and setters omitted for brevity
}
```

```java
@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String sku;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private BigDecimal price;

    // ✅ CRITICAL: Default for @ManyToOne is EAGER. Must override to LAZY!
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    // Getters and setters omitted
}
```

---

## ⚡ 2. The N+1 Query Problem & The 4 Proven Solutions

### The Problem Defined
You execute 1 query to fetch 100 Orders:
`SELECT * FROM orders LIMIT 100;`
Then, when your code accesses `order.getItems()`, Hibernate emits **100 individual queries** to fetch the items for each order:
`SELECT * FROM order_items WHERE order_id = ?;` (x100)
Result: **1 + 100 = 101 queries** hitting the database, crashing API latency.

### Solution 1: `JOIN FETCH` (The Definitive JPQL Solution)
```java
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("SELECT DISTINCT o FROM Order o JOIN FETCH o.items WHERE o.status = :status")
    List<Order> findAllByStatusWithItems(@Param("status") String status);
}
```

### Solution 2: `@EntityGraph` (Declarative Fetch Plan)
Dynamically overrides lazy fetching without writing custom JPQL joins.

```java
public interface OrderRepository extends JpaRepository<Order, Long> {

    @EntityGraph(attributePaths = {"items"})
    List<Order> findByStatus(String status);
}
```

### Solution 3: Global Batch Fetching (`application.yml`)
Instructs Hibernate to group lazy loads using an `IN (?, ?, ...)` clause instead of 1-by-1 queries.
```yaml
spring:
  jpa:
    properties:
      hibernate:
        default_batch_fetch_size: 50
```
*Result:* 100 orders will now fetch items in only 2 queries:
`SELECT * FROM order_items WHERE order_id IN (?, ?, ... 50 IDs);`

### Solution 4: DTO Projections (Zero Entity Overhead)
If you only need read-only data for an API response, fetch directly into a DTO (see Section 3).

---

## 🎯 3. High-Performance Projections: Records, DTOs & Interfaces

Loading full JPA entities into memory when you only need 3 fields wastes CPU, memory, and database I/O.

### 3.1 Java 17/21 Record DTO Constructor Projection (Fastest & Type-Safe)
```java
package com.example.jpa.dto;

import java.math.BigDecimal;

public record OrderSummaryDto(Long id, String orderNumber, String status, BigDecimal totalAmount) {}
```

```java
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("""
        SELECT new com.example.jpa.dto.OrderSummaryDto(o.id, o.orderNumber, o.status, SUM(i.price * i.quantity))
        FROM Order o
        JOIN o.items i
        WHERE o.status = :status
        GROUP BY o.id, o.orderNumber, o.status
        """)
    List<OrderSummaryDto> findSummariesByStatus(@Param("status") String status);
}
```

### 3.2 Dynamic Interface Projection
```java
public interface CustomerView {
    String getEmail();
    String getFirstName();
    
    // Open projection with SpEL computation
    @Value("#{target.firstName + ' ' + target.lastName}")
    String getFullName();
}

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    List<CustomerView> findByActiveTrue();
}
```

---

## 📄 4. Pagination & Slicing: Page vs Slice vs Keysets

| Return Type | Emits `COUNT(*)` Query? | Knows Total Elements & Pages? | Ideal Use Case |
| :--- | :--- | :--- | :--- |
| **`Page<T>`** | **Yes** (Heavy overhead on 1M+ tables) | Yes (`getTotalElements()`, `getTotalPages()`) | Traditional desktop UI with numbered pagination buttons |
| **`Slice<T>`** | **No** (Fetches $limit + 1$ to check `hasNext()`) | No | Mobile apps, Infinite scrolling, "Load More" buttons |
| **Keyset Pagination** | **No** (`WHERE id > :lastSeenId`) | No | Ultra-high scale ($O(1)$ performance regardless of page depth) |

### High-Performance `Slice` Implementation
```java
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Emits: SELECT * FROM products WHERE category = ? LIMIT 21 (for size=20)
    // Avoids costly: SELECT COUNT(*) FROM products WHERE category = ?
    Slice<Product> findByCategory(String category, Pageable pageable);
}
```

---

## 🔒 5. Concurrency Control: Optimistic vs Pessimistic Locking

```mermaid
sequenceDiagram
    participant User A
    participant User B
    participant Database

    Note over Database: Initial Stock: 10
    User A->>Database: SELECT FOR UPDATE (Pessimistic Write Lock)
    Note over Database: Row LOCKED by User A
    User B->>Database: SELECT FOR UPDATE (Blocks & Waits)
    User A->>Database: UPDATE stock = 9; COMMIT
    Note over Database: Row UNLOCKED
    User B->>Database: Lock Acquired. Reads updated stock: 9
    User B->>Database: UPDATE stock = 8; COMMIT
```

### 5.1 Optimistic Locking with `@Version`
Use when collisions are rare. Fails fast without database-level row locks.

```java
@Entity
public class Wallet {
    @Id
    private Long id;

    private BigDecimal balance;

    @Version // Automatically checked and incremented by Hibernate
    private Long version;
}
```
If two transactions update the same wallet concurrently, the second transaction throws `OptimisticLockException`.

### 5.2 Pessimistic Locking (`SELECT FOR UPDATE`)
Use for high-contention financial ledgers or flash sales where updates must queue sequentially.

```java
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Inventory i WHERE i.sku = :sku")
    Optional<Inventory> findBySkuForUpdate(@Param("sku") String sku);
}
```

---

## 🔍 6. Dynamic Queries with JPA Specifications & Criteria API

Avoid building brittle query strings. `Specification<T>` allows composable, reusable, and type-safe query predicates.

```java
package com.example.jpa.spec;

import com.example.jpa.entity.Order;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.Instant;

public class OrderSpecifications {

    public static Specification<Order> hasStatus(String status) {
        return (root, query, cb) -> status == null ? cb.conjunction() : cb.equal(root.get("status"), status);
    }

    public static Specification<Order> createdAfter(Instant date) {
        return (root, query, cb) -> date == null ? cb.conjunction() : cb.greaterThanOrEqualTo(root.get("createdAt"), date);
    }

    public static Specification<Order> totalAmountGreaterThan(BigDecimal amount) {
        return (root, query, cb) -> amount == null ? cb.conjunction() : cb.greaterThan(root.get("totalAmount"), amount);
    }
}
```

### Dynamic Execution in Service
```java
@Service
public class OrderSearchService {

    private final OrderRepository orderRepository;

    public OrderSearchService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public Page<Order> search(String status, Instant startDate, BigDecimal minAmount, Pageable pageable) {
        Specification<Order> spec = Specification
            .where(OrderSpecifications.hasStatus(status))
            .and(OrderSpecifications.createdAfter(startDate))
            .and(OrderSpecifications.totalAmountGreaterThan(minAmount));

        return orderRepository.findAll(spec, pageable);
    }
}
```

---

## 🕒 7. Enterprise Auditing & Soft Deletes in Hibernate 6

### 7.1 Automatic JPA Auditing (`@EntityListeners`)
```java
@Configuration
@EnableJpaAuditing
public class JpaConfig {}
```

```java
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseAuditableEntity {

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;

    @CreatedBy
    @Column(updatable = false)
    private String createdBy;
}
```

### 7.2 Native Soft Deletes in Hibernate 6
```java
@Entity
@Table(name = "accounts")
@SQLDelete(sql = "UPDATE accounts SET deleted = true WHERE id = ?")
@SQLRestriction("deleted = false") // Hibernate 6 replacement for deprecated @Where
public class Account extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private boolean deleted = Boolean.FALSE;
}
```

---

## 🏭 8. Production Scenarios & War Room Incident Forensics

### Scenario 1: Bulk Update Bypassing First-Level Session Cache
- **Symptom:** You execute `@Modifying @Query("UPDATE User u SET u.active = false WHERE ...")`, but subsequent calls to `userRepository.findById()` in the same transaction still return `active = true`!
- **Root Cause:** Direct DML queries update the database directly without updating entities already loaded into Hibernate's First-Level Cache.
- **The Fix:** Add `clearAutomatically = true`:
```java
@Modifying(clearAutomatically = true, flushAutomatically = true)
@Query("UPDATE User u SET u.active = false WHERE u.lastLoginDate < :cutoff")
int deactivateInactiveUsers(@Param("cutoff") Instant cutoff);
```

### Scenario 2: Flash Sale Stock Race Condition Under 5,000 req/s
- **Symptom:** Stock reaches negative numbers (`stock = -35`).
- **Root Cause:** Read-modify-write without row locks: Two threads read `stock = 1` simultaneously, check `stock > 0`, and decrement.
- **The Fix:** Pessimistic write locking or atomic database decrement:
```java
@Modifying
@Query("UPDATE Product p SET p.stock = p.stock - :quantity WHERE p.id = :id AND p.stock >= :quantity")
int decrementStockAtomic(@Param("id") Long id, @Param("quantity") int quantity);
```
Check if returned rows updated == 1. If 0, throw `OutOfStockException`.

---

## ⚖️ 9. Spring Data JPA Master Cheat Sheet

| Task / Feature | Code Construct |
| :--- | :--- |
| **Fetch Join** | `@Query("SELECT u FROM User u JOIN FETCH u.roles")` |
| **Entity Graph** | `@EntityGraph(attributePaths = {"orders.items"})` |
| **Pessimistic Lock** | `@Lock(LockModeType.PESSIMISTIC_WRITE)` |
| **Optimistic Version** | `@Version private Long version;` |
| **Skip Count in Paging**| Return `Slice<T>` instead of `Page<T>` |
| **Soft Delete Filter** | `@SQLRestriction("deleted = false")` (Hibernate 6) |
| **Auto-clear Cache** | `@Modifying(clearAutomatically = true)` |
| **DTO Projection** | `@Query("SELECT new com.dto.UserDto(u.id, u.name) FROM User u")` |

---
[🏠 Back to Home](README.md)
