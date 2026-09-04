[🏠 Back to Home](README.md)

# 🏛️ Spring Data JPA & Hibernate 6 Enterprise Architecture Master Guide

A production-grade engineering handbook for building robust, high-performance data persistence layers using **Spring Data JPA**, **Hibernate 6.x**, **Spring Boot 3.x**, and **Java 17/21**. Covers entity lifecycle states, solving the N+1 problem, pessimistic vs optimistic locking, JPA Specifications, and zero-leak auditing.

---

## 📑 Table of Contents

### Track 1: Junior & Entry-Level Foundations

- [🌱 1. Real-World Mental Model (Tracing Paper & Dirty Checking)](#1-the-real-world-mental-model-the-accountants-transparent-tracing-paper--dirty-checking)
- [🧩 2. The 5 Core Building Blocks of Spring Data JPA](#2-the-5-core-building-blocks)
- [💻 3. Beginner Code Walkthrough: Clean Entity & Dirty Checking](#3-beginner-code-walkthrough-clean-entity--dirty-checking)
- [💥 4. What Happens When Things Break? (Top 3 Disasters)](#4-what-happens-when-things-break-top-3-disasters)
- [⚠️ 5. Top 5 Beginner Mistakes in Production](#5-top-5-beginner-mistakes-in-production)
- [🎯 6. Top 10 Junior Interview Questions (With "ELI5" Answers)](#6-top-10-junior-interview-questions-with-explain-like-im-5-answers)

### Track 2: Advanced Architecture & High-Throughput Persistence

1. [🗺️ 1. Entity Relationships: OneToMany, ManyToOne & Pitfalls](#️-1-entity-relationships-onetomany-manytoone--pitfalls)
2. [⚡ 2. The N+1 Query Problem & The 4 Proven Solutions](#-2-the-n1-query-problem--the-4-proven-solutions)
3. [🎯 3. High-Performance Projections: Records, DTOs & Interfaces](#-3-high-performance-projections-records-dtos--interfaces)
4. [📄 4. Pagination & Slicing: Page vs Slice vs Keysets](#-4-pagination--slicing-page-vs-slice-vs-keysets)
5. [🔒 5. Concurrency Control: Optimistic vs Pessimistic Locking](#-5-concurrency-control-optimistic-vs-pessimistic-locking)
6. [🔍 6. Dynamic Queries with JPA Specifications & Criteria API](#-6-dynamic-queries-with-jpa-specifications--criteria-api)
7. [🕒 7. Enterprise Auditing & Soft Deletes in Hibernate 6](#-7-enterprise-auditing--soft-deletes-in-hibernate-6)
8. [🏭 8. Production Scenarios & War Room Incident Forensics](#-8-production-scenarios--war-room-incident-forensics)
9. [⚖️ 9. Spring Data JPA Master Cheat Sheet](#️-9-spring-data-jpa-master-cheat-sheet)

# TRACK 1: THE JUNIOR & ENTRY-LEVEL FOUNDATIONS (ZERO-TO-HERO)

## 1. The Real-World Mental Model (The Accountant's Transparent Tracing Paper & Dirty Checking)

### What Is an ORM (Object-Relational Mapping)?
- In Java, you think in **Objects and References** (`customer.getOrders().add(new Order())`).
- In SQL databases, you think in **Tables, Primary Keys, and Foreign Keys** (`SELECT * FROM orders WHERE customer_id = 42`).
- **JPA & Hibernate** act as a **Bilingual Translator** between your Java object model and SQL relational tables.

---

### The Persistence Context & Dirty Checking (The Accountant Analogy)
Imagine you are an accountant inspecting a client's paper invoice:
1. You pull the original invoice from the filing cabinet (**Database Read / SQL SELECT**).
2. You lay a sheet of **transparent tracing paper (First-Level Cache / Snapshot)** over the invoice in your office (**Persistence Context**).
3. You cross out the old price and write in the new price: `order.setPrice(99.00)`.
4. At 5:00 PM (**Transaction Commit**), you compare the tracing paper with the original invoice:
   - If nothing changed: you put the invoice back without doing anything.
   - If numbers changed (**Dirty Checking**): Hibernate automatically writes and runs `UPDATE orders SET price = 99.00 WHERE id = 1`!
   - *Notice:* You **NEVER need to call `repository.save(order)`** if the entity is already managed inside a `@Transactional` method!

```
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                        TRANSACTION BOUNDARY (@Transactional)                           │
│                                                                                        │
│  1. DB Read ──► SQL SELECT ──► [ First-Level Cache (Persistence Context) ]             │
│                                      │                                                 │
│                                      ▼                                                 │
│                        Original Snapshot vs Managed Entity                             │
│                                      │                                                 │
│  2. Business Logic ──► entity.setStatus("APPROVED")  (No repo.save() needed!)          │
│                                      │                                                 │
│  3. Tx Commit ──► Dirty Checking detects modification                                 │
│                                      │                                                 │
│                                      ▼                                                 │
│  4. Flush ──────► SQL UPDATE emitted to Database                                       │
└────────────────────────────────────────────────────────────────────────────────────────┘
```

---

## 2. The 5 Core Building Blocks

| Term | What It Means | Real-World Analogy |
| :--- | :--- | :--- |
| **`@Entity`** | A Java class mapped directly to a database table row. | A blueprint for an employee's paper profile. |
| **`PersistenceContext`** | The in-memory L1 cache holding all managed entity instances within a transaction. | The accountant's physical desk while working on open files. |
| **Entity States** | Transient, Managed, Detached, Removed. | Unemployed, Hired & Working, Resigned, Terminated. |
| **Dirty Checking** | Automatic detection of changed entity fields at transaction commit time. | Spotting differences between before-and-after photographs. |
| **`JpaRepository`** | An interface providing CRUD and pagination methods with zero boilerplate SQL. | A magic vending machine: you ask for `findById()`, it hands you the record. |

---

## 3. Beginner Code Walkthrough: Clean Entity & Dirty Checking

### Step 1: Declare the Entity (`UserAccount.java`)
```java
package com.example.jpa.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "user_accounts")
public class UserAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String status; // "PENDING", "ACTIVE", "SUSPENDED"

    // Standard getters and setters (Lombok @Getter/@Setter is fine, but avoid @Data!)
    public Long getId() { return id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
```

### Step 2: Service Layer Demonstrating Dirty Checking (`UserService.java`)
```java
package com.example.jpa.service;

import com.example.jpa.entity.UserAccount;
import com.example.jpa.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public void activateUser(Long userId) {
        // 1. Fetch entity (Moves into MANAGED state in L1 Persistence Context)
        UserAccount user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        // 2. Mutate state
        user.setStatus("ACTIVE");

        // 🌟 Notice: No userRepository.save(user) needed!
        // At the end of @Transactional, Dirty Checking detects user.status changed
        // and automatically executes: UPDATE user_accounts SET status = 'ACTIVE' WHERE id = ?
    }
}
```

---

## 4. What Happens When Things Break? (Top 3 Disasters)

1. **`LazyInitializationException: could not initialize proxy - no session`:**
   You have a `@OneToMany` lazy relationship (e.g. `customer.getOrders()`). Outside of the `@Transactional` boundary (e.g. inside a Controller or Thymeleaf template), you call `customer.getOrders().size()`. The database connection is already closed, crashing your app with `LazyInitializationException`! **Fix:** Fetch with `JOIN FETCH` or use DTO projections inside the service layer.
2. **The N+1 Query Problem:**
   Querying 100 Customers (`SELECT * FROM customer` = 1 query), then looping over them to get their orders. Hibernate fires 100 individual queries (`SELECT * FROM orders WHERE customer_id = ?`), resulting in $1 + 100 = 101$ SQL queries! **Fix:** Use `@Query("SELECT c FROM Customer c JOIN FETCH c.orders")` or `@EntityGraph`.
3. **`StackOverflowError` with Lombok `@Data`:**
   `Customer` has `List<Order> orders`, and `Order` has `Customer customer`. Lombok's generated `toString()` or `hashCode()` calls the other, causing an infinite loop until the JVM stack runs out of memory! **Fix:** Never use `@Data` on JPA entities; use `@Getter` and `@Setter`.

---

## 5. Top 5 Beginner Mistakes in Production

1. **Redundant `repository.save()` in `@Transactional` Methods:** Calling `repo.save(entity)` when the entity was already fetched from the database in the same transaction. It is 100% redundant, misleads other developers, and wastes CPU cycles.
2. **Leaving `@ManyToOne` as Default `FetchType.EAGER`:** Many developers do not know that `@ManyToOne` defaults to `EAGER`. Loading a single entity can trigger dozens of hidden SQL joins across unrelated tables! **Fix:** Always write `@ManyToOne(fetch = FetchType.LAZY)`.
3. **Using `FetchType.EAGER` to "Fix" `LazyInitializationException`:** Turning lazy into eager turns your entire database into a web of eager loads. Fetching 1 user can load half the entire database into memory!
4. **Using In-Memory Pagination with Joins (`HHH000104: firstResult/maxResults specified with collection fetch; applying in memory!`):** Using `Pageable` with a `JOIN FETCH` on a collection causes Hibernate to pull all 5,000,000 rows into JVM RAM and paginate in memory, causing immediate `OutOfMemoryError`!
5. **Modifying an Entity Without a Transaction:** Reading an entity outside a `@Transactional` block, altering its fields, and expecting it to auto-save. Without an active transaction, Dirty Checking never runs!

---

## 6. Top 10 Junior Interview Questions (With "Explain Like I'm 5" Answers)

### Q1: What is Dirty Checking in Hibernate?
- **ELI5 Answer:** *"The computer takes a photo of your toys when you take them out of the box. When you put them away, it looks for any toys that moved and only repaints the toys that were changed."*
- **Technical Answer:** *"Dirty Checking is an automated change-detection mechanism. When an entity is loaded into the Persistence Context, Hibernate creates an internal snapshot. Upon transaction commit or flush, Hibernate compares the current state of managed entities against the snapshot and emits SQL `UPDATE` statements for altered columns."*

### Q2: What are the 4 lifecycle states of a JPA Entity?
- **ELI5 Answer:** *"1. New baby (not registered anywhere), 2. Citizen with a passport (managed by government), 3. Citizen who moved to Mars (disconnected/detached), 4. Deceased (removed from the registry)."*
- **Technical Answer:** *"The states are: (1) **Transient/New** (instantiated in Java, has no DB identifier, not in context), (2) **Managed** (has DB identifier, tracked by `EntityManager` in L1 cache), (3) **Detached** (has DB identifier but context is closed or cleared), and (4) **Removed** (scheduled for SQL `DELETE` upon flush)."*

### Q3: What is the N+1 query problem and how do you solve it?
- **ELI5 Answer:** *"Going to the store to buy 1 carton of eggs, then driving back to the store 12 separate times to buy each egg one-by-one."*
- **Technical Answer:** *"The N+1 problem occurs when fetching 1 parent record (1 query), and then lazily fetching associated child records for each parent in a loop ($N$ queries). It is resolved by: (1) `JOIN FETCH` in JPQL, (2) `@EntityGraph`, or (3) `@BatchSize`."*

### Q4: Why should you never use Lombok's `@Data` on JPA Entities?
- **ELI5 Answer:** *"Two mirrors facing each other: reflection bounces back and forth forever until your eyes hurt."*
- **Technical Answer:** *"`@Data` automatically generates `toString()`, `equals()`, and `hashCode()` that inspect all fields. In bidirectional relationships (e.g., Parent has Children, Child has Parent), this triggers recursive circular references causing a `StackOverflowError`, and can prematurely trigger lazy loading of collections."*

### Q5: What is the difference between `FetchType.LAZY` and `FetchType.EAGER`?
- **ELI5 Answer:** *"Eager is packing your entire winter closet, tent, and ski boots for a 1-day trip to the beach. Lazy is only packing swimming trunks, and buying a coat later if it actually snows."*
- **Technical Answer:** *"`EAGER` loads the associated entity immediately when the parent is loaded (often via SQL JOIN). `LAZY` defers loading until the association is explicitly accessed in code, substituting a bytecode CGLIB proxy until initialized."*

### Q6: What causes `LazyInitializationException` and how do you fix it?
- **ELI5 Answer:** *"Asking the library to read page 50 of a book after the library has already turned off the lights and locked the front door for the night."*
- **Technical Answer:** *"It occurs when accessing a lazy-loaded association or collection after the underlying Hibernate `Session` / `EntityManager` has closed (e.g. outside of `@Transactional` boundary). Fix by fetching required associations eagerly via `JOIN FETCH` or returning DTO projections within the service layer."*

### Q7: What is the difference between `save()` and `saveAndFlush()` in Spring Data JPA?
- **ELI5 Answer:** *"`save()` puts a letter in your outbox to be mailed later when you leave the office. `saveAndFlush()` drops the letter and runs immediately down the street to the post office right this second."*
- **Technical Answer:** *"`save()` marks the entity as managed, queueing changes in Hibernate's action queue to be written to the database during the next normal transaction flush or commit. `saveAndFlush()` executes `save()` and immediately forces `entityManager.flush()`, pushing SQL statements to the DB right away."*

### Q8: What is the difference between Optimistic Locking and Pessimistic Locking?
- **ELI5 Answer:** *"Optimistic is trusting your friend not to edit the shared Google Doc at the same time, but checking the version number when you save. Pessimistic is physically taking the laptop away so nobody else can touch it."*
- **Technical Answer:** *"Optimistic locking uses a `@Version` column (number or timestamp) checked at commit time (`WHERE version = ?`); if another transaction incremented the version, `OptimisticLockException` is thrown. Pessimistic locking acquires a database row-level lock (`SELECT ... FOR UPDATE`), blocking all concurrent readers and writers."*

### Q9: Why is `CascadeType.REMOVE` dangerous in production?
- **ELI5 Answer:** *"Throwing away a broken pencil and accidentally throwing away your entire school backpack and laptop with it!"*
- **Technical Answer:** *"`CascadeType.REMOVE` automatically deletes all associated child entities when a parent entity is deleted. In large relational schemas, deleting one parent can trigger a cascading avalanche that wipes out thousands of historical records across multiple tables."*

### Q10: What is a DTO Projection and why is it faster than fetching Entities?
- **ELI5 Answer:** *"Instead of photocopying a 500-page book to find one phone number, you just write down the phone number on a tiny slip of paper."*
- **Technical Answer:** *"A DTO projection uses a Java Record or constructor expression (`SELECT new com.example.UserDto(u.id, u.email) FROM User u`) to query only the necessary columns from the database. It skips Hibernate entity management, snapshots, and L1 cache tracking, reducing heap memory and database I/O by 80%+."*

---

# TRACK 2: ADVANCED ARCHITECTURE & HIGH-THROUGHPUT PERSISTENCE

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
