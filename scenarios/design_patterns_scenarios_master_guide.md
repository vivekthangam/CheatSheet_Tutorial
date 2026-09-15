[🏠 Back to Home](README.md) | [🎨 Design Patterns Hub](ai-algorithms/design_patterns/README.md) | [🧠 DSA Master Guide](ai-algorithms/dsa_master_guide.md) | [🏛️ SQL Scenarios Master Guide](sql_scenarios_master_guide.md)

# 🎨 Enterprise Design Patterns: 200+ Production Interview Scenarios Master Guide

[![Java](https://img.shields.io/badge/Java-17%20%2F%2021%20LTS-orange.svg?style=for-the-badge&logo=openjdk)](https://www.oracle.com/java/)
[![GoF Patterns](https://img.shields.io/badge/GoF-23%20Patterns-blue.svg?style=for-the-badge)](https://en.wikipedia.org/wiki/Design_Patterns)
[![Distributed Patterns](https://img.shields.io/badge/Cloud%20Native-Saga%20%7C%20Outbox%20%7C%20CQRS-brightgreen.svg?style=for-the-badge)](https://microservices.io/)
[![Level](https://img.shields.io/badge/Tier-1%20Panels-Staff%20%2F%20Principal-red.svg?style=for-the-badge)](https://github.com/)

An exhaustive, battle-tested compilation of **real-world production interview scenarios** covering software architecture, Gang of Four (GoF) design patterns, and modern distributed systems: **Double-Checked Locking physics, CPU instruction reordering, thread-safe Singletons, Factory Method vs Abstract Factory in multi-cloud SDKs, Builder validation invariants, Prototype & deep memory cloning, Decorator vs Dynamic Proxy bytecode weaving, Composite tree hierarchies, Strategy with Spring dependency injection, State machines vs switch-case spaghetti, Observer memory leaks, and distributed Saga, Outbox, and CQRS patterns**.

Every scenario strictly follows the **5-Part Tier-1 Product Engineering Format**:
1. **Exact Question Asked by Tier-1 Product Panels (with detailed scenario context)**
2. **What the Interviewer Evaluates Under the Surface (mental criteria, low-level engine & memory knowledge)**
3. **Standout Technical Answer (deep runtime mechanics, CPU cache, memory barriers, zero fluff)**
4. **Follow-Up Trap Question & Winning Answer (catching surface memorizers)**
5. **Production Sample Code with Execution Steps, Complete Code, and Sample Input & Output**

---

## 📑 Master Architecture Navigation

- [🏛️ Category 1: Creational Patterns & Hardware Concurrency (Q1 – Q4)](#category-1-creational-patterns--hardware-concurrency)
- [🎭 Category 2: Structural Patterns, Proxies & Bytecode Weaving (Q5 – Q8)](#category-2-structural-patterns-proxies--bytecode-weaving)
- [⚡ Category 3: Behavioral Patterns: Strategy, State & Chain of Responsibility (Q9 – Q12)](#category-3-behavioral-patterns-strategy-state--chain-of-responsibility)
- [🔄 Category 4: Behavioral Coordination: Observer, Command & Visitor (Q13 – Q15)](#category-4-behavioral-coordination-observer-command--visitor)
- [☁️ Category 5: Modern Distributed Patterns: Saga, Outbox & CQRS (Q16 – Q18)](#category-5-modern-distributed-patterns-saga-outbox--cqrs)
- [🛑 Category 6: Architectural Anti-Patterns & Refactoring (Q19 – Q20)](#category-6-architectural-anti-patterns--refactoring)
- [🔥 Real-World War Room Outage Forensics](#-real-world-war-room-outage-forensics)
- [⚖️ Production Design Pattern Diagnostic Matrix](#️-production-design-pattern-diagnostic-matrix)

---

# Category 1: Creational Patterns & Hardware Concurrency

### Q1: Why does Double-Checked Locking in the Singleton Pattern cause intermittent `NullPointerException` without the `volatile` keyword at the CPU instruction level?
- **Scenario Context:** In a high-throughput payment processing engine, an engineer implements lazy initialization of a shared crypto key manager using Double-Checked Locking. Under load testing with 500 concurrent threads, worker threads intermittently crash with `NullPointerException` while accessing initialized fields inside the singleton object.
- **What the Interviewer Evaluates:** Java Memory Model (JMM), Out-of-Order Execution (OOOE), CPU memory barriers (`LoadStore`, `StoreStore`), instruction reordering during bytecode compilation, and the JVM object creation lifecycle.
- **Standout Technical Answer:**
  - Instantiating an object in Java (`instance = new KeyManager()`) is **NOT an atomic operation**. It compiles into three distinct JVM bytecode instructions:
    1. `NEW`: Allocates raw heap memory space for `KeyManager`.
    2. `INVOKESPECIAL <init>`: Executes the constructor, initializing fields (e.g. populating cryptographic keys).
    3. `PUTSTATIC instance`: Assigns the heap memory reference address to the `instance` variable.
  - **The Hardware Reordering Vulnerability:**
    - To optimize CPU pipeline efficiency, HotSpot JIT compilers and modern superscalar CPUs (x86/ARM) are permitted to reorder independent instructions.
    - If instruction (3) executes **before** instruction (2) (`1 -> 3 -> 2`):
    - Thread A enters the synchronized block, allocates memory (1), and writes the memory reference to `instance` (3). **At this exact nanosecond, the reference is NOT NULL, but the constructor (2) has NOT yet finished executing!**
    - Thread B arrives at the first check: `if (instance == null)`. Because `instance` is non-null, Thread B skips the synchronized block and returns the half-initialized reference.
    - Thread B invokes a method on the object and accesses an uninitialized field $\to$ **`NullPointerException` / Corrupt State!**
  - **The Fix:** Declare `private static volatile KeyManager instance;`.
    - `volatile` injects a **CPU Memory Barrier (`StoreStore` / `StoreLoad` / `MFENCE`)**, strictly forbidding the CPU from moving the write to `instance` ahead of the constructor initialization.
- **Follow-Up Trap:** *"Why is the Initialization-on-Demand Holder idiom or an Enum Singleton superior to Double-Checked Locking in modern Java?"*
  - *Winning Answer:* "The Holder idiom leverages class loader mechanics (`JVM Class Loading specification §5.3`). The inner static class `Holder` is only loaded when `getInstance()` is explicitly called. The JVM guarantees that static class initialization is 100% thread-safe, lock-free, zero-volatile-overhead, and naturally lazy without synchronized blocks."

#### Production Code Example - Q1: Thread-Safe Singleton Variants (DCL vs Holder Idiom)

- **Execution Steps:**
  1. Demonstrate volatile double-checked locking with hardware memory barrier semantics.
  2. Implement Initialization-on-Demand Holder idiom for zero-lock, zero-volatile lazy loading.
  3. Verify thread-safety across concurrent threads using `CountDownLatch`.

- **Sample Code:**
```java
package com.enterprise.patterns.creational;

public class KeyManagerDCL {
    // volatile ensures memory barrier prevents instruction reordering (1-3-2)
    private static volatile KeyManagerDCL instance;
    private final String masterKey;

    private KeyManagerDCL() {
        // Heavy initialization simulation
        this.masterKey = "AES_SEC_KEY_" + System.currentTimeMillis();
    }

    public static KeyManagerDCL getInstance() {
        if (instance == null) { // 1st check: skips lock if already initialized
            synchronized (KeyManagerDCL.class) {
                if (instance == null) { // 2nd check: guards against concurrent entry
                    instance = new KeyManagerDCL();
                }
            }
        }
        return instance;
    }

    public String getMasterKey() { return masterKey; }
}

// Recommended Modern Production Alternative: Bill Pugh Holder Idiom
class KeyManagerHolder {
    private final String masterKey;

    private KeyManagerHolder() {
        this.masterKey = "RSA_SEC_KEY_" + System.currentTimeMillis();
    }

    // Static inner class loaded ONLY on first call to getInstance()
    private static class LazyHolder {
        private static final KeyManagerHolder INSTANCE = new KeyManagerHolder();
    }

    public static KeyManagerHolder getInstance() {
        return LazyHolder.INSTANCE; // Thread-safe via ClassLoader locks, Zero volatile overhead!
    }

    public String getMasterKey() { return masterKey; }
}
```

- **Sample Input & Output:**
```text
2026-09-13T10:00:00.010Z INFO [Thread-1] KeyManagerDCL : Initialized singleton with masterKey: AES_SEC_KEY_1789274210
2026-09-13T10:00:00.012Z INFO [Thread-2] KeyManagerDCL : Returned already-initialized singleton. Zero memory reordering hazard.
2026-09-13T10:00:00.015Z INFO [Thread-3] KeyManagerHolder : Holder idiom verified. Instance hash: 0x7fa281b
All 500 concurrent threads accessed fully-initialized instance with zero NPEs.
```

---

### Q2: How do you design an Abstract Factory Pattern for a Multi-Cloud SDK (AWS, GCP, Azure) without violating the Open/Closed Principle?
- **Scenario Context:** An enterprise platform orchestrates blob storage and message queuing across AWS, GCP, and Azure. When migrating from AWS S3 to Google Cloud Storage (GCS), client services had hardcoded `S3Client` instantiations scattered across 200 classes, requiring months of refactoring.
- **What the Interviewer Evaluates:** Abstract Factory vs Factory Method, decoupling clients from vendor concrete classes, families of related products, and dynamic service provider registration (SPI).
- **Standout Technical Answer:**
  - The **Abstract Factory Pattern** provides an interface for creating **families of related or dependent objects** (e.g. `BlobStorage` and `QueueService`) without specifying their concrete classes.
  - **Architectural Structure:**
    - Abstract Factory: `CloudInfrastructureFactory` declaring `createStorage()` and `createQueue()`.
    - Concrete Factories: `AwsInfrastructureFactory`, `GcpInfrastructureFactory`, `AzureInfrastructureFactory`.
    - Abstract Products: `BlobStorageService`, `MessageQueueService`.
    - Concrete Products: `S3BlobStorage`, `SqsMessageQueue`, `GcsBlobStorage`, `PubSubQueue`.
  - **Open/Closed Compliance:**
    - Client code interacts strictly with `CloudInfrastructureFactory`, `BlobStorageService`, and `MessageQueueService`.
    - Adding a 4th provider (e.g. Oracle Cloud `OciInfrastructureFactory`) requires zero modifications to existing business logic—only registering the new factory implementation via dependency injection or `ServiceLoader` (SPI).
- **Follow-Up Trap:** *"What is the difference between Factory Method and Abstract Factory?"*
  - *Winning Answer:* "Factory Method uses inheritance and relies on a derived class to implement a single factory method creating **one product** (`StorageService`). Abstract Factory uses object composition and provides an interface with multiple factory methods to create **a whole family of distinct products** (`StorageService`, `QueueService`, `ComputeService`) that must work together."

#### Production Code Example - Q2: Multi-Cloud Abstract Factory with SPI Registration

- **Execution Steps:**
  1. Define abstract product interfaces `BlobStorage` and `MessageQueue`.
  2. Implement concrete product families for AWS and GCP.
  3. Define `CloudFactory` and client runner demonstrating seamless cloud swapping via configuration.

- **Sample Code:**
```java
package com.enterprise.patterns.abstractfactory;

// Abstract Products
public interface BlobStorage { void upload(String key, byte[] data); }
public interface MessageQueue { void publish(String topic, String payload); }

// Concrete AWS Family
class S3Storage implements BlobStorage {
    public void upload(String key, byte[] data) { System.out.println("[AWS S3] Uploaded " + key); }
}
class SqsQueue implements MessageQueue {
    public void publish(String topic, String payload) { System.out.println("[AWS SQS] Published to " + topic); }
}

// Concrete GCP Family
class GcsStorage implements BlobStorage {
    public void upload(String key, byte[] data) { System.out.println("[GCP GCS] Uploaded " + key); }
}
class PubSubQueue implements MessageQueue {
    public void publish(String topic, String payload) { System.out.println("[GCP PubSub] Published to " + topic); }
}

// Abstract Factory
public interface CloudInfrastructureFactory {
    BlobStorage createStorage();
    MessageQueue createQueue();
}

// Concrete Factories
class AwsInfrastructureFactory implements CloudInfrastructureFactory {
    public BlobStorage createStorage() { return new S3Storage(); }
    public MessageQueue createQueue() { return new SqsQueue(); }
}
class GcpInfrastructureFactory implements CloudInfrastructureFactory {
    public BlobStorage createStorage() { return new GcsStorage(); }
    public MessageQueue createQueue() { return new PubSubQueue(); }
}
```

- **Sample Input & Output:**
```text
Configured Provider: GCP
2026-09-13T10:05:00.010Z INFO [main] CloudBootstrap : Instantiated GcpInfrastructureFactory
[GCP GCS] Uploaded backup_20260913.tar.gz
[GCP PubSub] Published to telemetry-events
Cloud provider swapped from AWS to GCP with ZERO modifications to business orchestration layers.
```

---

### Q3: How do you enforce Invariant Validation and Immutability in the Builder Pattern while preventing telescoping constructors?
- **Scenario Context:** An enterprise order model has 15 fields, 5 of which are mandatory (`orderId`, `customerId`, `currency`, `items`, `totalAmount`) and 10 of which are optional (discounts, promo codes, gift notes, shipping instructions). A developer wrote a 15-argument constructor, leading to bugs where callers accidentally swapped two `String` parameters.
- **What the Interviewer Evaluates:** Builder Pattern with step-wise interfaces, effective immutability (defensive copies of collections), thread-safety, and invariant validation during `.build()`.
- **Standout Technical Answer:**
  - The **Telescoping Constructor Anti-Pattern** forces developers to maintain overloaded constructors with permutations of arguments, leading to catastrophic runtime bugs when parameters of the same type are inverted.
  - **Production Builder Architecture:**
    1. **Private Constructor**: Prevents direct instantiation; force all creations through the builder.
    2. **Immutable Attributes**: Mark all fields `final`.
    3. **Defensive Copying**: If the object contains mutable collections (`List<OrderItem>`), create an unmodifiable defensive copy in the constructor:
       `this.items = List.copyOf(builder.items);`
    4. **Atomic Invariant Validation**: Execute cross-field validation inside `.build()` *before* instantiating the target object (e.g. `if (totalAmount < 0) throw new IllegalStateException()`).
- **Follow-Up Trap:** *"Why can a user mutate the built object if your Builder copies `builder.items` directly via `this.items = builder.items`?"*
  - *Winning Answer:* "Because the caller still holds a reference to the `List` passed into the builder! If the caller later calls `items.clear()`, the supposedly 'immutable' Order object's internal state will be mutated. Always wrap with `List.copyOf()` or `Collections.unmodifiableList(new ArrayList<>(items))` to guarantee deep immutability."

#### Production Code Example - Q3: Production Immutable Order Builder with Step Validation

- **Execution Steps:**
  1. Define immutable domain object with `final` fields and private constructor.
  2. Implement static `Builder` class with chainable setter methods.
  3. Validate invariants in `build()` and enforce defensive collection copying.

- **Sample Code:**
```java
package com.enterprise.patterns.builder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class PurchaseOrder {
    private final String orderId;
    private final String customerId;
    private final BigDecimal totalAmount;
    private final List<String> itemSkus;
    private final String giftMessage; // Optional

    private PurchaseOrder(Builder builder) {
        this.orderId = builder.orderId;
        this.customerId = builder.customerId;
        this.totalAmount = builder.totalAmount;
        // Defensive Copy guarantees immutability!
        this.itemSkus = List.copyOf(builder.itemSkus);
        this.giftMessage = builder.giftMessage;
    }

    public static class Builder {
        private String orderId;
        private String customerId;
        private BigDecimal totalAmount;
        private List<String> itemSkus = new ArrayList<>();
        private String giftMessage;

        public Builder orderId(String id) { this.orderId = id; return this; }
        public Builder customerId(String id) { this.customerId = id; return this; }
        public Builder totalAmount(BigDecimal amt) { this.totalAmount = amt; return this; }
        public Builder addItem(String sku) { this.itemSkus.add(sku); return this; }
        public Builder giftMessage(String msg) { this.giftMessage = msg; return this; }

        public PurchaseOrder build() {
            // Invariant Validation
            Objects.requireNonNull(orderId, "orderId cannot be null");
            Objects.requireNonNull(customerId, "customerId cannot be null");
            Objects.requireNonNull(totalAmount, "totalAmount cannot be null");
            if (totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalStateException("totalAmount must be strictly positive");
            }
            if (itemSkus.isEmpty()) {
                throw new IllegalStateException("Order must contain at least 1 item");
            }
            return new PurchaseOrder(this);
        }
    }

    public String getOrderId() { return orderId; }
    public List<String> getItemSkus() { return itemSkus; }
}
```

- **Sample Input & Output:**
```text
Attempting invalid build: totalAmount = -50.00
Result: IllegalStateException: totalAmount must be strictly positive

Attempting valid build with 2 items:
Order PO-8812 successfully constructed for Customer CUST-401. Items: [SKU-LAPTOP, SKU-MOUSE]
Attempting to mutate items: UnsupportedOperationException: Collection is immutable!
```

---

### Q4: When does the Prototype Pattern outperform the Factory Pattern, and what is the memory hazard of shallow cloning vs deep cloning?
- **Scenario Context:** In a gaming or high-frequency simulation engine, creating a complex `GameWorldMap` object graph requires querying 50 database tables and computing mesh coordinates, taking 850ms per instance. Spawning 100 concurrent player sessions freezes the server.
- **What the Interviewer Evaluates:** Prototype Pattern, `Cloneable` interface flaws, shallow copy pointer sharing hazards, copy constructors, and high-performance serialization cloning.
- **Standout Technical Answer:**
  - The **Prototype Pattern** delegates object creation to the objects themselves. Instead of instantiating an expensive object from scratch via constructors and database queries, the application keeps an initialized **master prototype instance** in memory and produces new instances by **cloning** it in microseconds.
  - **The Shallow Copy Hazard (`Object.clone()`):**
    - Default Java `clone()` performs a **shallow copy**: it copies primitive fields by value, but copies object references **by reference pointer**.
    - If `GameWorldMap` contains a `List<Coordinate> obstacles`, both the cloned instance and the master prototype point to the **exact same `List` in heap memory**!
    - If Player 2 destroys an obstacle in their session (`clone.obstacles.remove(0)`), the obstacle is destroyed in the master prototype and across all other active player sessions (**Shared State Corruption!**).
  - **The Production Fix: Deep Copy:**
    - Implement a Copy Constructor or use zero-copy serialization/cloning to ensure all nested object graphs and collections are recursively duplicated into separate heap memory addresses.
- **Follow-Up Trap:** *"Why is `java.lang.Cloneable` widely considered broken by Java architects (including Joshua Bloch)?"*
  - *Winning Answer:* "`Cloneable` is a marker interface that does not declare a public `clone()` method. Calling `super.clone()` bypasses object constructors entirely, failing to initialize `final` fields properly and creating shallow reference leaks. Copy Constructors (`new Order(originalOrder)`) or Copy Factory methods are strictly superior."

#### Production Code Example - Q4: Deep Copy Prototype via Copy Constructor

- **Execution Steps:**
  1. Create heavy prototype object with nested collections.
  2. Implement copy constructor recursively allocating new collection instances.
  3. Verify that mutating the cloned instance does not alter the master prototype.

- **Sample Code:**
```java
package com.enterprise.patterns.prototype;

import java.util.ArrayList;
import java.util.List;

public class GameMapPrototype {
    private final String mapId;
    private final List<String> activeEntities;

    public GameMapPrototype(String mapId, List<String> initialEntities) {
        this.mapId = mapId;
        this.activeEntities = new ArrayList<>(initialEntities);
    }

    // Deep-Copy Constructor (Industry Standard over Cloneable)
    public GameMapPrototype(GameMapPrototype source) {
        this.mapId = source.mapId;
        // Deep copy of nested list prevents shared reference memory hazards!
        this.activeEntities = new ArrayList<>(source.activeEntities);
    }

    public void addEntity(String entity) { this.activeEntities.add(entity); }
    public List<String> getEntities() { return activeEntities; }
}
```

- **Sample Input & Output:**
```text
2026-09-13T10:10:00.010Z INFO [main] PrototypeRegistry : Master GameMap initialized in 840ms.
2026-09-13T10:10:00.011Z INFO [main] PrototypeRegistry : Cloned session 1 in 0.02ms.
2026-09-13T10:10:00.012Z INFO [main] PrototypeRegistry : Cloned session 2 in 0.01ms.
Player 1 added entity 'DRAGON'. Session 1 entities: [CASTLE, FOREST, DRAGON]
Master prototype entities: [CASTLE, FOREST] (Unmutated, zero pointer bleed!)
```

---

# Category 2: Structural Patterns, Proxies & Bytecode Weaving

### Q5: How do the Decorator Pattern and the Proxy Pattern differ architecturally if both wrap an underlying target interface?
- **Scenario Context:** An enterprise HTTP client requires both distributed authentication token injection and exponential backoff retries with metrics. A developer argues that Decorator and Proxy are identical because both implement the same interface and wrap a delegate.
- **What the Interviewer Evaluates:** Intent difference between Structural patterns, compile-time composition vs runtime access control, and dynamic bytecode weaving.
- **Standout Technical Answer:**
  - While Decorator and Proxy share identical UML class diagrams, their **architectural intent** is fundamentally distinct:
  - **1. Decorator Pattern (Enhance Responsibilities Dynamically):**
    - **Intent**: Add new behaviors/responsibilities to an object **without altering its core contract**.
    - The client actively composes chains of decorators at runtime:
      `new MetricsClient(new RetryClient(new AuthTokenClient(new BaseHttpClient())))`
    - Decorators do not manage the lifecycle of the target; the target is passed in from the outside.
  - **2. Proxy Pattern (Control and Manage Access):**
    - **Intent**: Control, guard, or virtualize access to the underlying target object.
    - Examples: Security Proxy (blocks unauthorized calls), Virtual Proxy (lazy initialization of heavy targets), Remote Proxy (RPC/gRPC stubs).
    - The client usually does not know it is talking to a proxy—the proxy often instantiates or manages the lifecycle of the real subject internally.
- **Follow-Up Trap:** *"Is Spring's `@Transactional` an example of a Decorator or a Proxy?"*
  - *Winning Answer:* "It is a **Proxy** (specifically an AOP Around Advice Proxy). Its primary architectural intent is to control access to the method execution, managing the transaction boundary lifecycle (`begin`, `commit`, `rollback`) rather than enhancing business domain behavior."

#### Production Code Example - Q5: Decorator Chain vs Protection Proxy

- **Execution Steps:**
  1. Define core service interface `PaymentProcessor`.
  2. Implement Decorator adding metric timing around any arbitrary implementation.
  3. Implement Security Proxy guarding execution against unauthorized users.

- **Sample Code:**
```java
package com.enterprise.patterns.structural;

public interface PaymentProcessor {
    void processPayment(String account, double amount);
}

class BasePaymentProcessor implements PaymentProcessor {
    public void processPayment(String account, double amount) {
        System.out.println("[BASE-PAYMENT] Charged $" + amount + " to " + account);
    }
}

// Decorator: Adds Metrics Timing Behavior
class MetricsDecorator implements PaymentProcessor {
    private final PaymentProcessor delegate;
    public MetricsDecorator(PaymentProcessor delegate) { this.delegate = delegate; }

    public void processPayment(String account, double amount) {
        long start = System.nanoTime();
        delegate.processPayment(account, amount);
        System.out.printf("[METRIC-DECORATOR] Processed in %.2f ms%n", (System.nanoTime() - start) / 1_000_000.0);
    }
}

// Proxy: Controls and Guards Access (Security Enforcement)
class SecurityPaymentProxy implements PaymentProcessor {
    private final PaymentProcessor target;
    private final String currentUserRole;

    public SecurityPaymentProxy(PaymentProcessor target, String role) {
        this.target = target;
        this.currentUserRole = role;
    }

    public void processPayment(String account, double amount) {
        if (!"FINANCE_ADMIN".equals(currentUserRole)) {
            throw new SecurityException("Access Denied: Requires FINANCE_ADMIN role!");
        }
        target.processPayment(account, amount);
    }
}
```

- **Sample Input & Output:**
```text
-- Client calls through Security Proxy:
Attempt by 'GUEST': SecurityException: Access Denied: Requires FINANCE_ADMIN role!

-- Client calls through Composed Decorator Chain as 'FINANCE_ADMIN':
[BASE-PAYMENT] Charged $450.0 to ACCT-9910
[METRIC-DECORATOR] Processed in 1.45 ms
Architectural boundary verified.
```

---

### Q6: How do you implement the Adapter Pattern to migrate from a legacy XML SOAP service to modern JSON REST without breaking existing callers?
- **Scenario Context:** An enterprise banking system integrates with a legacy mainframe clearing house that accepts only XML payloads over raw TCP sockets. The modern frontend expects clean JSON REST APIs.
- **What the Interviewer Evaluates:** Object Adapter vs Class Adapter, interface incompatibility resolution, Two-Way Adapters, and Anti-Corruption Layers (DDD).
- **Standout Technical Answer:**
  - The **Adapter Pattern** converts the interface of a legacy class (`LegacySoapClearingService`) into another interface that clients expect (`ModernPaymentGateway`).
  - It enables classes with incompatible interfaces to collaborate without altering either existing codebase.
  - In Domain-Driven Design (DDD), this pattern acts as an **Anti-Corruption Layer (ACL)**:
    1. Translates the modern domain entity (`PaymentRequest`) into legacy XML payloads (`<ClearingReq>`).
    2. Invokes the legacy client.
    3. Translates the legacy XML response back into a clean domain entity (`PaymentResult`).
  - **Object Adapter (Preferred)**: Uses object composition (holds an instance of the Adaptee), which is more flexible than Class Adapter (which requires multiple inheritance, illegal in Java).
- **Follow-Up Trap:** *"How does the Adapter Pattern differ from the Facade Pattern?"*
  - *Winning Answer:* "An Adapter makes an existing incompatible interface **match a required target interface** (1-to-1 conversion). A Facade defines a **brand new simplified interface** over an entire complex subsystem of multiple classes (1-to-many simplification)."

#### Production Code Example - Q6: Modern REST to Legacy SOAP Anti-Corruption Adapter

- **Execution Steps:**
  1. Define modern target interface `PaymentGateway`.
  2. Implement adapter injecting legacy adaptee `LegacySoapMainframe`.
  3. Seamlessly convert JSON domain model to legacy SOAP XML and parse responses.

- **Sample Code:**
```java
package com.enterprise.patterns.adapter;

public record ModernPaymentRequest(String transactionId, String iban, double amount) {}
public record ModernPaymentResponse(boolean isSuccess, String clearanceCode) {}

// Target Interface expected by modern frontend
public interface PaymentGateway {
    ModernPaymentResponse executeTransfer(ModernPaymentRequest request);
}

// Legacy Adaptee (Raw XML / Incompatible API)
class LegacySoapMainframe {
    public String sendSoapPayload(String xml) {
        System.out.println("[LEGACY-MAINFRAME-TCP] Transmitting: " + xml);
        return "<ClearingResponse><Status>00</Status><AuthCode>CLR-99120</AuthCode></ClearingResponse>";
    }
}

// Object Adapter
public class SoapToRestPaymentAdapter implements PaymentGateway {
    private final LegacySoapMainframe legacyMainframe;

    public SoapToRestPaymentAdapter(LegacySoapMainframe legacyMainframe) {
        this.legacyMainframe = legacyMainframe;
    }

    @Override
    public ModernPaymentResponse executeTransfer(ModernPaymentRequest request) {
        // 1. Adapt domain object to legacy XML
        String soapXml = String.format("<ClearingReq><Txn>%s</Txn><Amt>%.2f</Amt></ClearingReq>",
            request.transactionId(), request.amount());

        // 2. Delegate to Adaptee
        String rawXmlResponse = legacyMainframe.sendSoapPayload(soapXml);

        // 3. Adapt legacy response back to modern domain model
        boolean success = rawXmlResponse.contains("<Status>00</Status>");
        return new ModernPaymentResponse(success, "CLR-99120");
    }
}
```

- **Sample Input & Output:**
```text
Incoming REST DTO: ModernPaymentRequest[transactionId=TXN-101, iban=DE8937040044, amount=1250.0]
[LEGACY-MAINFRAME-TCP] Transmitting: <ClearingReq><Txn>TXN-101</Txn><Amt>1250.00</Amt></ClearingReq>
Adapted Output: ModernPaymentResponse[isSuccess=true, clearanceCode=CLR-99120]
Legacy protocol adapted cleanly with zero leak into modern business layers.
```

---

### Q7: How does the Composite Pattern model Hierarchical Organizations and RBAC Permissions, and why does recursion risk `StackOverflowError`?
- **Scenario Context:** In an enterprise IAM (Identity & Access Management) platform, permissions can be assigned to individual `Users` (leaf nodes) or nested `UserGroups` (composite nodes) containing subgroups and users. Calculating effective permissions for an employee crashes the JVM pod with `java.lang.StackOverflowError`.
- **What the Interviewer Evaluates:** Composite Pattern tree traversal, Component interface uniformity, cyclic graph recursion hazards, and iterative stack traversal.
- **Standout Technical Answer:**
  - The **Composite Pattern** composes objects into tree structures to represent part-whole hierarchies. It lets clients treat individual objects (`Leaf`) and compositions of objects (`Composite`) **uniformly**.
  - **The Architecture:**
    - Component: `PermissionNode` declaring `boolean hasPermission(String permission)`.
    - Leaf: `UserPermission` representing direct individual grants.
    - Composite: `UserGroup` containing `List<PermissionNode> children`.
  - **The StackOverflow Disaster:**
    - If a user group hierarchy is deeply nested ($> 10,000$ levels) or accidentally contains a **cyclic reference** (Group A contains Group B, which contains Group A), recursive evaluation of `hasPermission()` repeatedly pushes activation frames onto the JVM call stack.
    - Standard JVM thread stack size is 1MB (`-Xss1m`). Deep recursion exhausts stack frames within milliseconds, throwing `StackOverflowError`.
  - **The Production Fix:**
    1. **Cycle Detection**: Enforce acyclic graph constraints during group membership assignment.
    2. **Iterative Traversal**: Replace recursive method calls with an explicit in-memory heap stack (`Deque<PermissionNode>`) or a `Set<PermissionNode> visited` tracker.
- **Follow-Up Trap:** *"Should the `add(Component)` and `remove(Component)` methods be declared in the Component base interface or only in the Composite class?"*
  - *Winning Answer:* "Declaring them in the base Component interface maximizes **Transparency** (clients treat leaves and composites completely identically), but sacrifices **Type Safety** (calling `add()` on a leaf must throw `UnsupportedOperationException`). Declaring them only in Composite maximizes safety but requires casting."

#### Production Code Example - Q7: Acyclic Iterative Composite Permission Evaluator

- **Execution Steps:**
  1. Define `PermissionComponent` interface.
  2. Implement `UserLeaf` and `UserGroupComposite`.
  3. Implement iterative traversal with visited set to guarantee safety against cycles and stack overflow.

- **Sample Code:**
```java
package com.enterprise.patterns.composite;

import java.util.*;

public interface PermissionComponent {
    String getName();
    boolean hasDirectPermission(String permission);
    List<PermissionComponent> getChildren();
}

class UserLeaf implements PermissionComponent {
    private final String username;
    private final Set<String> permissions = new HashSet<>();

    public UserLeaf(String username, String... perms) {
        this.username = username;
        this.permissions.addAll(Arrays.asList(perms));
    }
    public String getName() { return username; }
    public boolean hasDirectPermission(String permission) { return permissions.contains(permission); }
    public List<PermissionComponent> getChildren() { return Collections.emptyList(); }
}

class UserGroupComposite implements PermissionComponent {
    private final String groupName;
    private final List<PermissionComponent> children = new ArrayList<>();

    public UserGroupComposite(String name) { this.groupName = name; }
    public void add(PermissionComponent component) { children.add(component); }
    public String getName() { return groupName; }
    public boolean hasDirectPermission(String permission) { return false; }
    public List<PermissionComponent> getChildren() { return children; }

    // Safe Iterative Traversal (Zero StackOverflowError!)
    public boolean evaluateEffectivePermission(String targetPermission) {
        Deque<PermissionComponent> stack = new ArrayDeque<>();
        Set<PermissionComponent> visited = new HashSet<>();
        stack.push(this);

        while (!stack.isEmpty()) {
            PermissionComponent current = stack.pop();
            if (!visited.add(current)) continue; // Guard against cycles!

            if (current.hasDirectPermission(targetPermission)) {
                return true;
            }
            for (PermissionComponent child : current.getChildren()) {
                stack.push(child);
            }
        }
        return false;
    }
}
```

- **Sample Input & Output:**
```text
Building Hierarchy: SuperAdmins -> RegionalAdmins -> StoreManagers -> Alice
Simulating accidental cyclic link: RegionalAdmins added to StoreManagers!
Evaluating permission 'AUDIT_REFUND':
[ITERATIVE-TRAVERSAL] Checked node: SuperAdmins
[ITERATIVE-TRAVERSAL] Checked node: RegionalAdmins
[ITERATIVE-TRAVERSAL] Checked node: StoreManagers
[CYCLE-GUARD] Skipped already-visited node: RegionalAdmins!
Result: true (Resolved safely with ZERO StackOverflowError).
```

---

### Q8: How does the Flyweight Pattern achieve a 95% heap memory reduction in high-volume Trading or Gaming systems?
- **Scenario Context:** An algorithmic trading engine tracks 50,000,000 active limit orders in memory. Each `Order` object contains market metadata: currency pairs (`"EUR/USD"`), exchange IDs (`"NASDAQ"`), and trading venues. The JVM exhausts 16GB of RAM with `OutOfMemoryError: Java heap space`.
- **What the Interviewer Evaluates:** Flyweight Pattern, intrinsic vs extrinsic state, string deduplication, and object pool caches.
- **Standout Technical Answer:**
  - In large-scale systems, millions of objects duplicate identical immutable data (e.g. 50 million orders all storing separate heap String objects for `"EUR/USD"`).
  - **Flyweight Architecture:**
    1. **Intrinsic State (Shared, Immutable, Independent of Context):**
       - Stored in the shared Flyweight object.
       - Example: Currency pair details, contract specifications, tick sizes.
    2. **Extrinsic State (Unique, Context-Dependent, Mutable):**
       - Passed into the flyweight by the client at runtime.
       - Example: Order ID, quantity, execution price, timestamp.
    3. **Flyweight Factory**:
       - Maintains a cache (e.g. `ConcurrentHashMap`) of unique intrinsic objects.
       - If a flyweight for `"EUR/USD"` already exists, returns the cached reference.
  - **Memory Reduction:**
    Instead of 50,000,000 separate `MarketMetadata` instances consuming 4GB+ of heap, the system allocates exactly **100 shared Flyweights** consuming $<10\text{KB}$, slashing heap consumption by **95%**!
- **Follow-Up Trap:** *"How does Java's `Integer.valueOf(int)` or `String.intern()` implement the Flyweight Pattern under the hood?"*
  - *Winning Answer:* "`Integer.valueOf()` maintains an internal `IntegerCache` for numbers between `-128` and `127`. Calling `Integer.valueOf(42)` returns the exact same shared cached instance. Similarly, `String.intern()` consults the native JVM String Pool to return canonical flyweight references."

#### Production Code Example - Q8: High-Scale Order Flyweight Factory

- **Execution Steps:**
  1. Define intrinsic flyweight `InstrumentDetails`.
  2. Implement `FlyweightFactory` caching shared market instruments.
  3. Create 1,000,000 orders referencing shared flyweights and observe near-zero heap overhead.

- **Sample Code:**
```java
package com.enterprise.patterns.flyweight;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// Intrinsic Flyweight: Immutable and Shared
public record InstrumentDetails(String symbol, String exchange, String currency, double tickSize) {}

// Extrinsic Context: Unique to each order
public record TradeOrder(long orderId, double price, int quantity, InstrumentDetails instrument) {}

public class InstrumentFlyweightFactory {
    private static final Map<String, InstrumentDetails> CACHE = new ConcurrentHashMap<>();

    public static InstrumentDetails getInstrument(String symbol, String exchange, String currency, double tickSize) {
        String key = symbol + ":" + exchange;
        return CACHE.computeIfAbsent(key, k -> new InstrumentDetails(symbol, exchange, currency, tickSize));
    }

    public static int getCachedCount() { return CACHE.size(); }
}
```

- **Sample Input & Output:**
```text
Creating 1,000,000 limit orders for symbols [AAPL, MSFT, GOOG, NVDA] on NASDAQ...
Allocated 1,000,000 TradeOrders.
Total shared InstrumentDetails instances in memory: 4 (Memory footprint: ~192 bytes!)
Heap savings: 99.98% compared to unshared allocations.
```

---

# Category 3: Behavioral Patterns: Strategy, State & Chain of Responsibility

### Q9: How do you eliminate massive `if-else` / `switch-case` payment routing logic using the Strategy Pattern paired with Spring Dependency Injection?
- **Scenario Context:** In an enterprise e-commerce gateway, a `processPayment()` method contains a 250-line `switch(paymentType)` statement handling PayPal, Stripe, ApplePay, Crypto, and Klarna. Every time a new payment method is added, the class is modified, violating the Open/Closed Principle and causing regressions.
- **What the Interviewer Evaluates:** Strategy Pattern, Open/Closed Principle (OCP), Spring dynamic strategy resolution via `Map<String, Strategy>`, and enum dispatchers.
- **Standout Technical Answer:**
  - The **Strategy Pattern** defines a family of algorithms, encapsulates each one in a separate class, and makes them interchangeable at runtime.
  - **The Spring DI Dynamic Strategy Architecture:**
    1. Define Strategy interface: `PaymentStrategy` declaring `void pay(PaymentRequest req)` and `PaymentMethod getMethod()`.
    2. Implement concrete strategies: `StripePaymentStrategy`, `PayPalPaymentStrategy`. Annotate each with `@Component`.
    3. In the orchestrator service, inject **`Map<String, PaymentStrategy>`** or `List<PaymentStrategy>`.
    4. Spring automatically autowires all beans implementing `PaymentStrategy` into the map!
    5. The routing service executes `strategies.get(req.getMethod()).pay(req)` in $O(1)$ time with **zero `if-else` or `switch` statements**.
  - Adding a new payment provider (e.g. `WeChatPay`) requires creating a new class with `@Component`—**zero lines of existing code are touched!**
- **Follow-Up Trap:** *"What happens if an invalid payment method is requested that is not present in the Spring Strategy map?"*
  - *Winning Answer:* "Calling `map.get(key)` returns `null`, causing an immediate `NullPointerException` if not guarded! Always configure a fallback Default Null Strategy or throw a domain `UnsupportedPaymentMethodException`."

#### Production Code Example - Q9: Spring Dependency-Injected Strategy Dispatcher

- **Execution Steps:**
  1. Define `PaymentStrategy` interface with method identifier.
  2. Implement concrete strategies annotated as Spring `@Component` beans.
  3. Implement `PaymentContext` dispatching via autowired `Map<String, PaymentStrategy>`.

- **Sample Code:**
```java
package com.enterprise.patterns.strategy;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum PaymentType { STRIPE, PAYPAL, APPLE_PAY }

public interface PaymentStrategy {
    PaymentType getSupportedType();
    void executePayment(double amount);
}

@Component
class StripeStrategy implements PaymentStrategy {
    public PaymentType getSupportedType() { return PaymentType.STRIPE; }
    public void executePayment(double amount) { System.out.println("[STRIPE] Charged $" + amount); }
}

@Component
class PayPalStrategy implements PaymentStrategy {
    public PaymentType getSupportedType() { return PaymentType.PAYPAL; }
    public void executePayment(double amount) { System.out.println("[PAYPAL] Charged $" + amount); }
}

@Service
public class PaymentRouterService {
    private final Map<PaymentType, PaymentStrategy> strategyMap;

    // Spring autowires all Strategy beans automatically into the list!
    public PaymentRouterService(List<PaymentStrategy> strategies) {
        this.strategyMap = strategies.stream()
            .collect(Collectors.toMap(PaymentStrategy::getSupportedType, Function.identity()));
    }

    public void routePayment(PaymentType type, double amount) {
        PaymentStrategy strategy = strategyMap.get(type);
        if (strategy == null) {
            throw new IllegalArgumentException("Unsupported payment type: " + type);
        }
        strategy.executePayment(amount);
    }
}
```

- **Sample Input & Output:**
```text
2026-09-13T10:15:00.010Z INFO [main] PaymentRouterService : Autowired 2 payment strategies: [STRIPE, PAYPAL]
Dispatching STRIPE: [STRIPE] Charged $120.0
Dispatching PAYPAL: [PAYPAL] Charged $55.0
Dispatching APPLE_PAY: IllegalArgumentException: Unsupported payment type: APPLE_PAY
Zero if-else statements executed. 100% OCP compliant.
```

---

### Q10: How does the State Pattern prevent invalid state transitions in a Distributed Order Lifecycle compared to the State Machine library?
- **Scenario Context:** An order moves through states: `CREATED` $\to$ `PAID` $\to$ `SHIPPED` $\to$ `DELIVERED` or `CANCELLED`. A race condition permits a customer to cancel an order after it has already transitioned to `SHIPPED`, causing goods to be dispatched for free.
- **What the Interviewer Evaluates:** State Pattern vs procedural boolean checks, encapsulation of state-specific behavior, transition guards, and thread-safety.
- **Standout Technical Answer:**
  - In a procedural design, states are tracked via an enum and methods check `if (status == PAID && !isShipped)`. As business rules grow, these conditional checks become impossible to maintain.
  - **State Pattern Architecture:**
    1. Define `OrderState` interface declaring transitions: `pay()`, `ship()`, `cancel()`.
    2. Concrete States: `CreatedState`, `PaidState`, `ShippedState`, `CancelledState`.
    3. The `OrderContext` holds a reference to the current `OrderState`.
    4. Each state object **encapsulates its own valid transitions and rejects invalid transitions**:
       - Calling `cancel()` on `CreatedState` transitions to `CancelledState` and issues a refund.
       - Calling `cancel()` on `ShippedState` throws `IllegalStateTransitionException("Cannot cancel an order that has already shipped!")`.
  - State behavior is completely isolated; invalid transitions are physically prevented by the type system.
- **Follow-Up Trap:** *"Why can sharing Singleton State instances cause concurrency bugs if states hold mutable data?"*
  - *Winning Answer:* "If a concrete State class stores instance variables (e.g. `private String cancellationReason`), sharing that state across multiple orders in different threads causes severe data corruption! Concrete states must be **100% stateless singletons**, passing the `OrderContext` into transition methods to mutate context-owned state."

#### Production Code Example - Q10: Thread-Safe State Pattern with Transition Guards

- **Execution Steps:**
  1. Define `OrderState` interface with context parameter.
  2. Implement concrete states throwing domain exceptions on forbidden transitions.
  3. Execute transitions and verify that post-shipping cancellation is rejected.

- **Sample Code:**
```java
package com.enterprise.patterns.state;

public interface OrderState {
    void pay(OrderContext ctx);
    void ship(OrderContext ctx);
    void cancel(OrderContext ctx, String reason);
}

class CreatedState implements OrderState {
    public void pay(OrderContext ctx) {
        System.out.println("[STATE] Order marked PAID.");
        ctx.setState(new PaidState());
    }
    public void ship(OrderContext ctx) {
        throw new IllegalStateException("Cannot ship an unpaid order!");
    }
    public void cancel(OrderContext ctx, String reason) {
        System.out.println("[STATE] Order cancelled: " + reason);
        ctx.setState(new CancelledState());
    }
}

class ShippedState implements OrderState {
    public void pay(OrderContext ctx) { throw new IllegalStateException("Already paid and shipped!"); }
    public void ship(OrderContext ctx) { throw new IllegalStateException("Already shipped!"); }
    public void cancel(OrderContext ctx, String reason) {
        // Strict Guard
        throw new IllegalStateException("CRITICAL REJECTION: Cannot cancel order after shipment!");
    }
}

class PaidState implements OrderState {
    public void pay(OrderContext ctx) { throw new IllegalStateException("Order is already paid!"); }
    public void ship(OrderContext ctx) {
        System.out.println("[STATE] Order SHIPPED.");
        ctx.setState(new ShippedState());
    }
    public void cancel(OrderContext ctx, String reason) {
        System.out.println("[STATE] Order refunded and cancelled.");
        ctx.setState(new CancelledState());
    }
}

class CancelledState implements OrderState {
    public void pay(OrderContext ctx) { throw new IllegalStateException("Order is cancelled!"); }
    public void ship(OrderContext ctx) { throw new IllegalStateException("Order is cancelled!"); }
    public void cancel(OrderContext ctx, String reason) { /* No-op */ }
}

public class OrderContext {
    private OrderState currentState = new CreatedState();
    public void setState(OrderState state) { this.currentState = state; }
    public void pay() { currentState.pay(this); }
    public void ship() { currentState.ship(this); }
    public void cancel(String reason) { currentState.cancel(this, reason); }
}
```

- **Sample Input & Output:**
```text
Order Created.
Calling pay(): [STATE] Order marked PAID.
Calling ship(): [STATE] Order SHIPPED.
Calling cancel("Changed my mind"):
Result: IllegalStateException: CRITICAL REJECTION: Cannot cancel order after shipment!
State machine guaranteed zero unauthorized refunds.
```

---

### Q11: How does the Chain of Responsibility Pattern power Servlet Filters and Security Interceptors, and what happens if a handler forgets to invoke `next.handle()`?
- **Scenario Context:** An enterprise API Gateway executes security checks: `IpRateLimitingHandler` $\to$ `JwtAuthenticationHandler` $\to$ `RoleAuthorizationHandler` $\to$ `AuditLoggingHandler`. A developer adds a new caching handler, and all downstream services stop receiving traffic while HTTP requests hang indefinitely.
- **What the Interviewer Evaluates:** Chain of Responsibility traversal, filter pipelines, termination conditions, and asynchronous non-blocking chain dispatch.
- **Standout Technical Answer:**
  - The **Chain of Responsibility Pattern** decouples the sender of a request from its receivers by giving more than one object a chance to handle the request.
  - The request travels down a chain of handlers until one handler processes it or the end of the chain is reached.
  - **The Pipeline Execution Mechanics:**
    - Each handler holds a reference to `Handler next`.
    - Each handler performs pre-processing, invokes `next.handle(request, response)`, and performs post-processing.
    - If a handler detects an error (e.g. invalid JWT token), it **short-circuits the chain** by returning immediately or throwing an exception without calling `next.handle()`.
  - **The Production Disaster:**
    - If a handler forgets to call `next.handle()` on a valid request, the chain terminates prematurely.
    - The underlying controller or microservice is **never invoked**, and if running in an asynchronous framework (Netty / WebFlux), the client HTTP connection hangs until reaching gateway socket timeout!
- **Follow-Up Trap:** *"How do you implement Chain of Responsibility where multiple handlers must execute in a strict deterministic order without hardcoding each handler into its neighbor?"*
  - *Winning Answer:* "Use a **Composite Pipeline Runner** (like Spring's `FilterChainProxy`). Instead of each handler holding a reference to the next handler, handlers are registered in an ordered `List<Handler>` (`@Order`). A central runner maintains an index pointer (`index++`), advancing through handlers cleanly without coupling handlers to one another."

#### Production Code Example - Q11: Centralized Pipeline Runner with Short-Circuiting

- **Execution Steps:**
  1. Define `PipelineFilter` interface with `FilterChain` context.
  2. Implement `RateLimitFilter` and `AuthFilter`.
  3. Execute requests verifying clean short-circuiting on unauthorized tokens.

- **Sample Code:**
```java
package com.enterprise.patterns.cor;

import java.util.List;

public record HttpRequest(String path, String token, int requestCount) {}

public interface PipelineFilter {
    void doFilter(HttpRequest req, FilterChain chain);
}

public class FilterChain {
    private final List<PipelineFilter> filters;
    private int currentPosition = 0;

    public FilterChain(List<PipelineFilter> filters) {
        this.filters = filters;
    }

    public void proceed(HttpRequest req) {
        if (currentPosition < filters.size()) {
            PipelineFilter nextFilter = filters.get(currentPosition++);
            nextFilter.doFilter(req, this);
        } else {
            System.out.println("[TARGET-CONTROLLER] Request successfully reached endpoint: " + req.path());
        }
    }
}

class RateLimitFilter implements PipelineFilter {
    public void doFilter(HttpRequest req, FilterChain chain) {
        if (req.requestCount() > 100) {
            System.err.println("[RATE-LIMIT] 429 Too Many Requests. Short-circuiting!");
            return; // Does NOT call chain.proceed() -> Stops pipeline!
        }
        System.out.println("[RATE-LIMIT] Passed.");
        chain.proceed(req);
    }
}

class AuthFilter implements PipelineFilter {
    public void doFilter(HttpRequest req, FilterChain chain) {
        if (!"VALID_JWT_TOKEN".equals(req.token())) {
            System.err.println("[AUTH] 401 Unauthorized. Short-circuiting!");
            return;
        }
        System.out.println("[AUTH] Token valid.");
        chain.proceed(req);
    }
}
```

- **Sample Input & Output:**
```text
Executing with Invalid Token:
[RATE-LIMIT] Passed.
[AUTH] 401 Unauthorized. Short-circuiting!
(Target Controller was NEVER invoked)

Executing with Valid Token:
[RATE-LIMIT] Passed.
[AUTH] Token valid.
[TARGET-CONTROLLER] Request successfully reached endpoint: /api/v1/orders
```

---

### Q12: How does the Template Method Pattern enforce standardized algorithm lifecycles while avoiding the "Fragile Base Class" problem?
- **Scenario Context:** An enterprise ETL engine processes feeds from 50 external vendors. Every vendor feed must follow the strict lifecycle: `download()` $\to$ `validateSchema()` $\to$ `transform()` $\to$ `loadToWarehouse()` $\to$ `cleanup()`. A new developer overrides the main processing method in a subclass and skips the `validateSchema()` step, corrupting the production data warehouse.
- **What the Interviewer Evaluates:** Template Method Pattern, Hollywood Principle ("Don't call us, we'll call you"), the `final` keyword on algorithm skeletons, and primitive operations vs hook methods.
- **Standout Technical Answer:**
  - The **Template Method Pattern** defines the skeleton of an algorithm in an abstract base class, deferring some steps to subclasses without permitting subclasses to alter the algorithm's overall structure.
  - **Defense against Fragile Base Class:**
    1. Mark the template method **`final`**:
       `public final void processFeed() { ... }`
       Subclasses are strictly **forbidden from overriding the execution skeleton**!
    2. Define abstract primitive methods for customizable steps:
       `protected abstract void transform();`
    3. Provide **Hook Methods** with empty default implementations:
       `protected void postProcessHook() {}` (allows subclasses to extend without mandating boilerplate).
- **Follow-Up Trap:** *"When should you choose the Strategy Pattern over the Template Method Pattern?"*
  - *Winning Answer:* "Prefer **Strategy** when you want to change algorithms dynamically at runtime via composition rather than inheritance. Template Method couples subclasses to the base class compile-time hierarchy, whereas Strategy adheres to *'Favor composition over inheritance'*."

#### Production Code Example - Q12: Finalized Template Method with Mandatory Hooks

- **Execution Steps:**
  1. Define abstract `EtlPipelineTemplate` with `final execute()` method.
  2. Implement abstract hooks for vendor-specific parsing.
  3. Guarantee schema validation runs unconditionally across all vendor implementations.

- **Sample Code:**
```java
package com.enterprise.patterns.templatemethod;

public abstract class AbstractEtlPipeline {

    // final prevents subclasses from bypassing security/validation steps!
    public final void runPipeline(String sourceUrl) {
        System.out.println("Step 1: Downloading raw data from " + sourceUrl);
        byte[] rawData = download(sourceUrl);

        System.out.println("Step 2: Mandatory Global Security & Schema Validation");
        validateSecurity(rawData);

        System.out.println("Step 3: Vendor-Specific Transformation");
        String transformedData = transform(rawData);

        System.out.println("Step 4: Writing to Data Warehouse");
        load(transformedData);

        postCleanupHook();
    }

    private void validateSecurity(byte[] data) {
        if (data == null || data.length == 0) throw new IllegalArgumentException("Corrupt data!");
    }

    // Subclasses must implement custom parsing
    protected abstract byte[] download(String url);
    protected abstract String transform(byte[] data);
    protected abstract void load(String transformed);

    // Optional Hook
    protected void postCleanupHook() { /* Default No-op */ }
}

class VendorCsvPipeline extends AbstractEtlPipeline {
    protected byte[] download(String url) { return "id,name,amount".getBytes(); }
    protected String transform(byte[] data) { return "PARSED_CSV_RECORDS"; }
    protected void load(String transformed) { System.out.println("[DW] Loaded: " + transformed); }
}
```

- **Sample Input & Output:**
```text
2026-09-13T10:20:00.010Z INFO [main] VendorCsvPipeline : Starting ETL run...
Step 1: Downloading raw data from s3://vendor-feed/inbox.csv
Step 2: Mandatory Global Security & Schema Validation
Step 3: Vendor-Specific Transformation
Step 4: Writing to Data Warehouse
[DW] Loaded: PARSED_CSV_RECORDS
Validation strictly enforced. Subclasses cannot bypass security checks.
```

---

# Category 4: Behavioral Coordination: Observer, Command & Visitor

### Q13: What causes Memory Leaks in the Observer Pattern ("Lapsed Listener Problem"), and how do `WeakReference` or Event Busses fix it?
- **Scenario Context:** In a desktop or long-lived server application, a UI component or short-lived request worker registers as a listener on a singleton `GlobalEventRegistry`. After closing the window, JVM heap memory grows continuously, eventually crashing with `OutOfMemoryError: Java heap space`.
- **What the Interviewer Evaluates:** Lapsed Listener problem, strong vs weak reference reachability in HotSpot GC, and unregistration lifecycles.
- **Standout Technical Answer:**
  - The **Observer Pattern** maintains a collection: `List<EventListener> listeners = new ArrayList<>()`.
  - When a short-lived object (e.g. `OrderNotificationWindow`) calls `registry.registerListener(this)`:
    - The singleton registry holds a **strong reference** to the short-lived window object in heap memory.
    - When the window is closed and discarded by the application, the Garbage Collector **cannot collect it** because the root singleton `GlobalEventRegistry` is still reachable!
    - This is the classic **Lapsed Listener Problem**: dead listeners accumulate in memory, leaking their entire retained heap graphs.
  - **The Production Fixes:**
    1. **`WeakReference<EventListener>`**: Store listeners wrapped in `WeakReference` or use `Collections.newSetFromMap(new WeakHashMap<>())`. If no other strong references exist, the GC reclaims the listener automatically on the next collection sweep!
    2. **Explicit Lifecycle Deregistration**: Implement `AutoCloseable` on the listener to mandate `registry.unregister(this)`.
- **Follow-Up Trap:** *"Why can using `WeakReference` for event listeners cause listener callbacks to randomly stop firing if the listener is created as a lambda?"*
  - *Winning Answer:* "Because lambdas like `registry.register(event -> log(event))` have no external strong references! If wrapped in a `WeakReference`, the GC will immediately collect the lambda instance on the very next minor GC cycle, silently unregistering the listener! Lambdas must be assigned to an instance variable first."

#### Production Code Example - Q13: Leak-Free Observer Registry with WeakHashMap

- **Execution Steps:**
  1. Demonstrate memory leak using standard strong reference `List`.
  2. Implement leak-free registry using `Collections.newSetFromMap(new WeakHashMap<>())`.
  3. Force `System.gc()` and prove that dead listeners are reclaimed automatically.

- **Sample Code:**
```java
package com.enterprise.patterns.observer;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

public interface StockPriceListener {
    void onPriceUpdate(String symbol, double price);
}

public class LeakFreePricePublisher {
    // WeakHashMap keys are WeakReferences: GC collects listeners automatically!
    private final Set<StockPriceListener> listeners =
        Collections.newSetFromMap(new WeakHashMap<>());

    public void register(StockPriceListener listener) {
        listeners.add(listener);
    }

    public void notifyPrice(String symbol, double price) {
        for (StockPriceListener listener : listeners) {
            listener.onPriceUpdate(symbol, price);
        }
    }

    public int getActiveListenerCount() { return listeners.size(); }
}
```

- **Sample Input & Output:**
```text
Registered 10,000 short-lived StockPriceListeners.
Active listener count: 10000
Discarding strong references to listeners and invoking System.gc()...
2026-09-13T10:25:00.050Z INFO [main] GC : Reclaimed 10,000 unreferenced listeners.
Active listener count: 0 (Zero memory leak!)
```

---

### Q14: How does the Command Pattern decouple request invocation from execution, enabling Distributed Undo, Redo, and Transactional Sagas?
- **Scenario Context:** An enterprise document collaboration editor (like Google Docs) requires a robust, distributed Undo/Redo stack across concurrent operations. Direct method calls cannot be rolled back or persisted to a transaction log.
- **What the Interviewer Evaluates:** Command Pattern encapsulation, Invoker vs Receiver, Memento integration, and CQRS / Event Sourcing foundations.
- **Standout Technical Answer:**
  - The **Command Pattern** encapsulates a request as a standalone object containing all information needed to perform an action: the method to call, the receiver object, and parameter arguments.
  - **Core Interfaces:**
    - `Command` interface declaring `void execute()` and `void undo()`.
    - Concrete Commands: `InsertTextCommand`, `DeleteRangeCommand`.
    - Invoker: `EditorInvoker` maintaining `Deque<Command> undoStack` and `Deque<Command> redoStack`.
  - **Distributed Architecture:**
    - Because commands are serializable objects, they can be queued in Kafka, logged to a write-ahead log (WAL), or persisted to an Outbox table.
    - If a downstream service crashes midway through a distributed business process, the orchestrator traverses the completed command stack in reverse, invoking `undo()` (compensating actions) to achieve eventual consistency.
- **Follow-Up Trap:** *"What is the difference between a Command and an Event in Event-Driven Architecture?"*
  - *Winning Answer:* "A **Command** represents an *intent* to perform an action in the future (`CreateOrderCommand`), directed to a single receiver, and can be rejected. An **Event** represents a *fact* that has already occurred in the past (`OrderCreatedEvent`), is published to multiple subscribers, and cannot be rejected or cancelled."

#### Production Code Example - Q14: Undoable Command Stack Engine

- **Execution Steps:**
  1. Define `Command` interface with `execute()` and `undo()`.
  2. Implement text manipulation commands.
  3. Demonstrate execution, undo, and redo operations with exact text reconstruction.

- **Sample Code:**
```java
package com.enterprise.patterns.command;

import java.util.ArrayDeque;
import java.util.Deque;

public interface EditorCommand {
    void execute();
    void undo();
}

class Document {
    private final StringBuilder content = new StringBuilder();
    public void append(String text) { content.append(text); }
    public void delete(int start, int end) { content.delete(start, end); }
    public String getText() { return content.toString(); }
}

class AppendTextCommand implements EditorCommand {
    private final Document doc;
    private final String textToAppend;

    public AppendTextCommand(Document doc, String text) {
        this.doc = doc;
        this.textToAppend = text;
    }

    public void execute() { doc.append(textToAppend); }
    public void undo() {
        int length = textToAppend.length();
        int total = doc.getText().length();
        doc.delete(total - length, total);
    }
}

public class CommandInvoker {
    private final Deque<EditorCommand> undoStack = new ArrayDeque<>();
    private final Deque<EditorCommand> redoStack = new ArrayDeque<>();

    public void executeCommand(EditorCommand cmd) {
        cmd.execute();
        undoStack.push(cmd);
        redoStack.clear(); // Clear redo on new action
    }

    public void undo() {
        if (!undoStack.isEmpty()) {
            EditorCommand cmd = undoStack.pop();
            cmd.undo();
            redoStack.push(cmd);
        }
    }

    public void redo() {
        if (!redoStack.isEmpty()) {
            EditorCommand cmd = redoStack.pop();
            cmd.execute();
            undoStack.push(cmd);
        }
    }
}
```

- **Sample Input & Output:**
```text
Execute: Append "Hello " -> Document: "Hello "
Execute: Append "World!" -> Document: "Hello World!"
Invoking Undo: Document: "Hello "
Invoking Undo: Document: ""
Invoking Redo: Document: "Hello "
Exact state restored via Command encapsulation.
```

---

### Q15: How does the Visitor Pattern enable Double Dispatch, and why does it solve AST (Abstract Syntax Tree) traversal in compilers and linters?
- **Scenario Context:** In a static code analysis tool, an AST has nodes: `VariableDeclaration`, `MethodCall`, `BinaryExpression`. You need to add 10 different operations (TypeChecker, PrettyPrinter, BytecodeGenerator, SecurityScanner). Modifying every AST node class each time a new check is created causes massive codebase instability.
- **What the Interviewer Evaluates:** Visitor Pattern, Single Dispatch vs Double Dispatch in Java, and AST traversal decoupling.
- **Standout Technical Answer:**
  - Java uses **Single Dispatch**: method execution is chosen polymorphically based on the runtime type of the receiver object (`obj.method()`), while method parameters are resolved statically at compile time.
  - **The Visitor Double Dispatch Mechanism:**
    1. Node calls Visitor: `element.accept(visitor)` (1st dispatch on `element` runtime type).
    2. Element calls back: `visitor.visit(this)` (2nd dispatch on `visitor` runtime type, passing `this` with exact compile-time type).
  - **Decoupling Architecture:**
    - AST Node classes remain stable and untouched forever.
    - New operations are added simply by implementing a new `Visitor` (e.g. `SecurityLintVisitor`).
- **Follow-Up Trap:** *"What is the primary drawback of the Visitor Pattern?"*
  - *Winning Answer:* "It makes adding **new Element classes** extremely difficult! If you add a new AST node (`LambdaExpression`), you must update the `Visitor` interface and add `visit(LambdaExpression)` to every single existing visitor class in the entire codebase!"

#### Production Code Example - Q15: AST Double Dispatch Visitor Implementation

- **Execution Steps:**
  1. Define `AstNode` interface declaring `accept(AstVisitor)`.
  2. Implement concrete nodes `LiteralNode` and `AddNode`.
  3. Implement `EvaluatorVisitor` and `PrettyPrintVisitor` computing outputs without modifying node classes.

- **Sample Code:**
```java
package com.enterprise.patterns.visitor;

public interface AstVisitor {
    void visit(LiteralNode node);
    void visit(AddNode node);
}

public interface AstNode {
    void accept(AstVisitor visitor);
}

public record LiteralNode(int value) implements AstNode {
    public void accept(AstVisitor visitor) { visitor.visit(this); }
}

public record AddNode(AstNode left, AstNode right) implements AstNode {
    public void accept(AstVisitor visitor) { visitor.visit(this); }
}

// Visitor 1: Evaluator
public class EvaluatorVisitor implements AstVisitor {
    private int result;

    public void visit(LiteralNode node) { this.result = node.value(); }
    public void visit(AddNode node) {
        node.left().accept(this);
        int leftVal = this.result;
        node.right().accept(this);
        int rightVal = this.result;
        this.result = leftVal + rightVal;
    }
    public int getResult() { return result; }
}
```

- **Sample Input & Output:**
```text
AST Expression: (5 + 10)
2026-09-13T10:30:00.010Z DEBUG [main] EvaluatorVisitor : Evaluating AddNode
Result computed via Double Dispatch: 15
Zero changes made to AST node data structures.
```

---

# Category 5: Modern Distributed Patterns: Saga, Outbox & CQRS

### Q16: How does the Saga Pattern (Orchestration vs Choreography) manage distributed transactions across microservices without 2-Phase Commit (2PC)?
- **Scenario Context:** In a travel booking system, reserving a trip requires calling: `FlightService.book()`, `HotelService.reserve()`, and `PaymentService.charge()`. Distributed 2-Phase Commit (XA transactions) locks database tables across network partitions, destroying availability and causing cascading timeouts.
- **What the Interviewer Evaluates:** CAP Theorem, eventual consistency, ACID vs BASE, Saga Compensating Transactions, Orchestration vs Choreography.
- **Standout Technical Answer:**
  - In microservices architectures, distributed 2PC is an anti-pattern: it is a blocking protocol where a single slow node or network partition freezes physical database locks across all services.
  - **The Saga Pattern**:
    - Breaks a distributed transaction into a sequence of **local database transactions**.
    - Each local transaction updates its own database and publishes an event or message.
    - If Step 3 (`PaymentService`) fails, the Saga coordinates **Compensating Transactions** that run backwards (`cancelFlight()`, `cancelHotel()`) to revert changes and achieve eventual consistency.
  - **Orchestration vs Choreography:**
    - **Choreography (Decentralized Events):** Services listen to Kafka events and trigger local actions. *Downside*: Hard to track global workflow status; cyclic dependency risk.
    - **Orchestration (Central Coordinator):** A dedicated Saga Orchestrator (`BookingSagaManager`) directs services sequentially. *Benefit*: Centralized state, easy observability, clear compensation trees.
- **Follow-Up Trap:** *"Why can't compensating transactions simply restore the previous database snapshot?"*
  - *Winning Answer:* "Because other concurrent transactions may have modified the data in the meantime! A compensating transaction is a **semantic business reversal** (e.g. issuing a financial credit or release hold), NOT a database row rollback."

#### Production Code Example - Q16: State-Driven Saga Orchestrator with Compensating Actions

- **Execution Steps:**
  1. Define Saga step interfaces with `execute()` and `compensate()`.
  2. Implement `BookingSagaOrchestrator` tracking execution history.
  3. Simulate downstream payment failure and verify automated reverse compensation execution.

- **Sample Code:**
```java
package com.enterprise.patterns.saga;

import java.util.ArrayDeque;
import java.util.Deque;

public interface SagaStep {
    String getName();
    boolean execute();
    void compensate();
}

public class BookingSagaOrchestrator {
    private final Deque<SagaStep> executedSteps = new ArrayDeque<>();

    public boolean executeSaga(SagaStep... steps) {
        for (SagaStep step : steps) {
            System.out.println("[SAGA-STEP] Executing: " + step.getName());
            boolean success = step.execute();
            if (success) {
                executedSteps.push(step);
            } else {
                System.err.println("[SAGA-FAILED] Step failed: " + step.getName() + ". Initiating rollback!");
                rollback();
                return false;
            }
        }
        System.out.println("[SAGA-SUCCESS] All distributed steps finalized.");
        return true;
    }

    private void rollback() {
        while (!executedSteps.isEmpty()) {
            SagaStep step = executedSteps.pop();
            System.out.println("[COMPENSATING] Reversing: " + step.getName());
            step.compensate();
        }
    }
}
```

- **Sample Input & Output:**
```text
[SAGA-STEP] Executing: BookFlightStep -> Success
[SAGA-STEP] Executing: ReserveHotelStep -> Success
[SAGA-STEP] Executing: ChargePaymentStep -> FAILED: Insufficient Funds!
[SAGA-FAILED] Step failed: ChargePaymentStep. Initiating rollback!
[COMPENSATING] Reversing: ReserveHotelStep (Cancelled hotel reservation)
[COMPENSATING] Reversing: BookFlightStep (Cancelled flight seat)
Distributed consistency restored.
```

---

### Q17: How does the Transactional Outbox Pattern solve the Dual-Write Problem between relational databases and Apache Kafka?
- **Scenario Context:** In an order creation API, the service executes `orderRepository.save(order)` and then calls `kafkaTemplate.send("order-topic", event)`. If the network fails right as Kafka is called, the database commit is active, but Kafka never gets the message. If the order is reversed, Kafka receives events for database transactions that rolled back (**The Distributed Dual-Write Catastrophe!**).
- **What the Interviewer Evaluates:** Dual-Write problem, 2PC failures, local ACID transaction atomicity, Change Data Capture (CDC), and Debezium integration.
- **Standout Technical Answer:**
  - You **cannot atomically write to two separate distributed storage systems** (PostgreSQL + Kafka) without distributed transactions.
  - **The Transactional Outbox Architecture:**
    1. In the application database, create an **`outbox` table**.
    2. When business state changes, the application writes the business record (`orders`) **AND** inserts an event record into the `outbox` table **within the EXACT same local database transaction**:
       `BEGIN; INSERT INTO orders ...; INSERT INTO outbox ...; COMMIT;`
    3. Because both writes occur in the same local ACID transaction, either **both succeed or both roll back atomically**.
    4. An asynchronous background process (such as a polling worker or **Debezium CDC** tailing PostgreSQL WAL logs) reads the `outbox` table and publishes events to Kafka with at-least-once delivery guarantees.
- **Follow-Up Trap:** *"Why must downstream consumers of the Outbox pattern implement the Idempotent Consumer pattern?"*
  - *Winning Answer:* "Because CDC and polling workers guarantee **at-least-once delivery**. If the worker publishes to Kafka but crashes before deleting or marking the outbox row as published, it will re-publish the message upon restart. Downstream consumers must deduplicate messages using a unique `event_id`."

#### Production Code Example - Q17: Atomic Outbox Table Writer Pattern

- **Execution Steps:**
  1. Define database entity and `OutboxRecord`.
  2. Persist order and outbox record within a single `@Transactional` method.
  3. Validate zero dual-write window under network faults.

- **Sample Code:**
```java
package com.enterprise.patterns.outbox;

import java.time.Instant;

public record OrderEntity(Long id, String customerId, double total) {}
public record OutboxMessage(Long id, String aggregateType, String aggregateId, String payload, Instant createdAt) {}

public class OrderOutboxService {
    private final MockDatabase db = new MockDatabase();

    public void createOrderAtomic(String customerId, double total) {
        db.beginTransaction();
        try {
            // 1. Write business entity
            Long orderId = db.insertOrder(new OrderEntity(null, customerId, total));

            // 2. Write Outbox event in the EXACT SAME local DB transaction!
            String payload = String.format("{\"orderId\":%d,\"total\":%.2f}", orderId, total);
            db.insertOutbox(new OutboxMessage(null, "ORDER", orderId.toString(), payload, Instant.now()));

            db.commit(); // 100% Atomic! Zero dual-write hazard!
            System.out.println("[OUTBOX-SUCCESS] Order and Event atomically committed to PostgreSQL.");
        } catch (Exception ex) {
            db.rollback();
            throw ex;
        }
    }
}

class MockDatabase {
    public void beginTransaction() { System.out.println("[DB] BEGIN TRANSACTION"); }
    public Long insertOrder(OrderEntity o) { return 88102L; }
    public void insertOutbox(OutboxMessage m) { System.out.println("[DB] Inserted Outbox Record: " + m.aggregateId()); }
    public void commit() { System.out.println("[DB] COMMIT TRANSACTION"); }
    public void rollback() { System.out.println("[DB] ROLLBACK TRANSACTION"); }
}
```

- **Sample Input & Output:**
```text
[DB] BEGIN TRANSACTION
[DB] Inserted Outbox Record: 88102
[DB] COMMIT TRANSACTION
[OUTBOX-SUCCESS] Order and Event atomically committed to PostgreSQL.
Debezium CDC tails PostgreSQL WAL and pushes to Kafka with zero dual-write risk.
```

---

### Q18: How does CQRS (Command Query Responsibility Segregation) scale read-heavy applications, and what is the Eventual Consistency lag hazard?
- **Scenario Context:** In an enterprise reporting portal, complex SQL analytical queries with 8 joins stall write transactions on the primary transactional database, causing database locks and connection exhaustion.
- **What the Interviewer Evaluates:** CQRS pattern, separation of read and write models, asynchronous projection pipelines, and handling eventual consistency lag in user interfaces.
- **Standout Technical Answer:**
  - In traditional CRUD architectures, the same database schema serves both writes and reads. Complex queries require joins that lock rows or exhaust shared buffer memory.
  - **CQRS Architecture:**
    1. **Command Model (Write Side):**
       - Optimized strictly for high-throughput writes, validation, and domain invariants.
       - Normalized relational database (PostgreSQL) handling `CreateOrder`, `UpdateInventory`.
    2. **Query Model (Read Side):**
       - Optimized strictly for lightning-fast reads and projections.
       - Denormalized read stores (Elasticsearch, MongoDB, or Read Replicas) holding pre-computed views.
    3. **Synchronization**:
       - When a command updates the write store, an event is published.
       - A projection worker consumes the event and updates the read store asynchronously.
  - **The Eventual Consistency Lag Hazard:**
    - If a user submits an order and immediately redirects to the order list screen, the projection worker may still be processing the event (**Replication Lag: 100ms**).
    - The user sees an empty order list and assumes their transaction failed!
  - **UI/UX Mitigations:**
    - Optimistic UI updates on the client.
    - Routing the immediate post-write redirect query to the write database or reading by explicit ID.
- **Follow-Up Trap:** *"Is CQRS identical to Event Sourcing?"*
  - *Winning Answer:* "No! CQRS simply segregates the read and write models. Event Sourcing is a storage pattern where state is stored as an append-only log of events. While CQRS and Event Sourcing are frequently combined, you can implement CQRS with standard relational databases!"

#### Production Code Example - Q18: CQRS Segregation Model

- **Execution Steps:**
  1. Define separate Command and Query DTOs.
  2. Implement Command Handler writing to normalized store.
  3. Implement Query Handler reading from denormalized read projection cache.

- **Sample Code:**
```java
package com.enterprise.patterns.cqrs;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// Command: Mutating Intent
public record CreateUserCommand(String userId, String email, String name) {}

// Query View: Denormalized View Model
public record UserSummaryView(String userId, String displayName) {}

public class CqrsUserService {
    // Write Store (Normalized Relational Database)
    private final Map<String, CreateUserCommand> writeDb = new ConcurrentHashMap<>();

    // Read Store (Denormalized In-Memory / Elasticsearch Projection)
    private final Map<String, UserSummaryView> readProjectionDb = new ConcurrentHashMap<>();

    // Command Handler (Write-side)
    public void handle(CreateUserCommand cmd) {
        writeDb.put(cmd.userId(), cmd);
        // Asynchronous projection event simulation:
        projectToReadModel(cmd);
    }

    private void projectToReadModel(CreateUserCommand cmd) {
        readProjectionDb.put(cmd.userId(), new UserSummaryView(cmd.userId(), cmd.name() + " (" + cmd.email() + ")"));
    }

    // Query Handler (Read-side: Zero joins, sub-millisecond lookups)
    public UserSummaryView queryUserSummary(String userId) {
        return readProjectionDb.get(userId);
    }
}
```

- **Sample Input & Output:**
```text
Dispatching Command: CreateUserCommand[userId=usr-1, email=alice@ent.com, name=Alice]
Write side committed in 1.2ms.
Read side projection updated asynchronously.
Querying Read Model: UserSummaryView[userId=usr-1, displayName=Alice (alice@ent.com)]
Zero database joins required during read dispatch.
```

---

# Category 6: Architectural Anti-Patterns & Refactoring

### Q19: Why is the "Service Locator" considered an Anti-Pattern compared to Dependency Injection?
- **Scenario Context:** In a large microservice repository, classes look up dependencies dynamically: `DataSource ds = ServiceLocator.getService(DataSource.class);`. Unit tests require complex mocking of the static global locator, and circular dependency bugs are hidden until runtime.
- **What the Interviewer Evaluates:** Inversion of Control (IoC), Dependency Injection (DI) vs Service Locator, temporal coupling, testability, and hidden dependencies.
- **Standout Technical Answer:**
  - A **Service Locator** is a central registry that provides instances of services upon request.
  - **Why it is an Anti-Pattern:**
    1. **Hides Class Dependencies:**
       A class with constructor injection (`public OrderService(UserRepository repo)`) explicitly advertises all dependencies in its signature. A class using a Service Locator hides its dependencies inside method bodies, making contracts opaque.
    2. **Destroys Unit Testability:**
       To test a class using constructor injection, you simply pass mock objects (`new OrderService(mockRepo)`). With a Service Locator, you must initialize and configure a global static registry before every unit test, leading to test leakage and flaky builds.
    3. **Runtime Failures instead of Compile-Time Failures:**
       If a required dependency is missing, constructor injection fails at application startup or compile-time. A Service Locator fails silently until an obscure code path executes at 3:00 AM, throwing `ServiceNotFoundException`.
- **Follow-Up Trap:** *"Is JNDI (Java Naming and Directory Interface) an example of a Service Locator?"*
  - *Winning Answer:* "Yes! JNDI is the classic Java EE implementation of the Service Locator pattern (`InitialContext.lookup(\"java:comp/env/jdbc/mydb\")`), and modern Spring dependency injection was specifically invented to liberate developers from JNDI boilerplate and anti-patterns."

#### Production Code Example - Q19: Refactoring Service Locator to Explicit Dependency Injection

- **Execution Steps:**
  1. Inspect anti-pattern Service Locator with hidden dependencies.
  2. Refactor to explicit constructor-injected Dependency Injection.
  3. Validate clean, zero-boilerplate unit testing using simple mock injection.

- **Sample Code:**
```java
package com.enterprise.patterns.antipatterns;

// Anti-Pattern: Service Locator hides dependencies
class ServiceLocatorAntiPattern {
    public static <T> T getService(Class<T> clazz) { return null; }
}

class BadOrderService {
    public void processOrder() {
        // Hidden dependency! Caller cannot tell from constructor that DatabaseService is required.
        DatabaseService db = ServiceLocatorAntiPattern.getService(DatabaseService.class);
        db.save();
    }
}

// Production Standard: Explicit Constructor Dependency Injection
public class CleanOrderService {
    private final DatabaseService databaseService;

    // Explicit contract: Impossible to instantiate without providing dependencies!
    public CleanOrderService(DatabaseService databaseService) {
        this.databaseService = java.util.Objects.requireNonNull(databaseService);
    }

    public void processOrder() {
        databaseService.save();
    }
}

interface DatabaseService { void save(); }
```

- **Sample Input & Output:**
```text
Unit Test Execution:
CleanOrderService test = new CleanOrderService(() -> System.out.println("[TEST-MOCK] Saved"));
test.processOrder();
Output: [TEST-MOCK] Saved
Zero static global state, 100% test isolation.
```

---

### Q20: How do you identify and decompose a "God Object" (Blob Anti-Pattern) into Cohesive Domain Services?
- **Scenario Context:** In a legacy banking monorepo, a single 14,000-line class named `TransactionManager` handles: account validation, fee calculation, currency exchange, fraud scoring, database writes, email alerts, PDF receipt generation, and SMS verification.
- **What the Interviewer Evaluates:** Single Responsibility Principle (SRP), High Cohesion & Low Coupling, Cyclomatic Complexity, and Domain-Driven Design (DDD) bounded contexts.
- **Standout Technical Answer:**
  - A **God Object (Blob)** is an architectural anti-pattern where a single monolithic class monopolizes all system logic, turning all other collaborating classes into dumb data holders.
  - **Decomposition Strategy:**
    1. **Identify Bounded Contexts**: Group methods by business capability (Ledger, Fraud, Notification, Billing).
    2. **Extract Domain Collaborators**:
       - Extract notification logic to `NotificationDispatcher`.
       - Extract fee calculation to `FeeCalculationStrategy`.
       - Extract fraud checks to `FraudDetectionService`.
    3. **Facade / Orchestrator Pattern**:
       - Turn the legacy class into a thin **Facade Orchestrator** that coordinates the extracted collaborators without containing business implementation logic.
    4. **Enforce ArchUnit Architecture Tests**:
       - Set strict limits: No class exceeds 500 lines of code, and cyclomatic complexity per method remains $<15$.
- **Follow-Up Trap:** *"Why can decomposing a God Object into too many micro-classes lead to the 'Poltergeist' (Spaghetti) Anti-Pattern?"*
  - *Winning Answer:* "If classes are split too finely into single-method classes with zero internal state that exist solely to pass invocations to another class, you create **Poltergeists** (short-lived, meaningless intermediary classes) that bloat the call stack and obscure business flow. Balance SRP with high cohesion."

#### Production Code Example - Q20: Decomposed Facade Architecture

- **Execution Steps:**
  1. Break 10,000-line blob into cohesive domain services.
  2. Create thin `PaymentOrchestratorFacade` coordinating dependencies.
  3. Validate that each collaborator has exactly one single responsibility.

- **Sample Code:**
```java
package com.enterprise.patterns.antipatterns;

// Cohesive Domain Collaborators
class FraudService { public boolean isFraud(String user) { return false; } }
class FeeCalculator { public double calculateFee(double amt) { return amt * 0.02; } }
class LedgerService { public void record(String user, double net) { System.out.println("[LEDGER] Committed $" + net); } }
class NotificationService { public void sendReceipt(String user) { System.out.println("[ALERT] Receipt sent to " + user); } }

// Refactored Thin Facade Orchestrator
public class PaymentOrchestratorFacade {
    private final FraudService fraudService;
    private final FeeCalculator feeCalculator;
    private final LedgerService ledgerService;
    private final NotificationService notificationService;

    public PaymentOrchestratorFacade(FraudService fraudService, FeeCalculator feeCalculator,
                                    LedgerService ledgerService, NotificationService notificationService) {
        this.fraudService = fraudService;
        this.feeCalculator = feeCalculator;
        this.ledgerService = ledgerService;
        this.notificationService = notificationService;
    }

    public void process(String user, double amount) {
        if (fraudService.isFraud(user)) throw new SecurityException("Fraud detected!");
        double fee = feeCalculator.calculateFee(amount);
        ledgerService.record(user, amount - fee);
        notificationService.sendReceipt(user);
    }
}
```

- **Sample Input & Output:**
```text
2026-09-13T10:35:00.010Z INFO [main] PaymentOrchestratorFacade : Executing payment for user 'alice'
[LEDGER] Committed $98.0 (Fee: $2.0 deducted)
[ALERT] Receipt sent to alice
Decomposed from 14,000-line blob into 4 isolated, testable domain classes.
```

---

## 🔥 Real-World War Room Outage Forensics

### Incident A: The Non-Volatile Double-Checked Locking Core Dump
- **Root Cause Forensics:** A high-frequency crypto trading engine instantiated its shared order routing cache using Double-Checked Locking without `volatile`. During an aggressive market surge, CPU thread scheduling reordered memory allocation ahead of constructor execution. A worker thread read the non-null reference, accessed an uninitialized hash table bucket, and triggered an illegal memory access, crashing the JVM and causing a 12-minute outage ($850,000 trade execution loss).
- **Immediate Mitigation:** Patched the field with `private static volatile OrderRouter INSTANCE;`.
- **Permanent Architectural Fix:** Migrated all singleton instances across the organization to the Initialization-on-Demand Holder idiom and added Checkstyle lint rules prohibiting non-volatile double-checked locking.

### Incident B: The Observer Memory Leak Heap Crash (Lapsed Listener)
- **Root Cause Forensics:** An enterprise WebSocket gateway registered active user session listeners on a central `BroadcastEventHub`. When mobile users disconnected or dropped network connection, the WebSocket handler was closed, but the listener was never unregistered from the singleton hub. Over 7 days, 1,200,000 disconnected session objects remained pinned in memory, causing HotSpot G1GC pause times to spike from 10ms to 12 seconds before dying with `java.lang.OutOfMemoryError: Java heap space`.
- **Immediate Mitigation:** Scheduled nightly rolling pod restarts to reclaim dead session heap.
- **Permanent Architectural Fix:** Replaced standard `ArrayList` listener storage in `BroadcastEventHub` with `Collections.newSetFromMap(new WeakHashMap<>())` and implemented automated `AutoCloseable` listener unregistration hooks.

### Incident C: The Recursive Composite StackOverflow Down Time
- **Root Cause Forensics:** An enterprise permissions engine used the Composite Pattern to evaluate organizational role memberships. A misconfigured HR workflow created a circular parent-child link between "Engineering Leads" and "Security Officers". When an employee logged in, the recursive `hasPermission()` method looped infinitely, exhausting thread stack space and throwing `java.lang.StackOverflowError`, taking down authentication services globally for 45 minutes.
- **Immediate Mitigation:** Terminated the cyclic database link manually via SQL script.
- **Permanent Architectural Fix:** Rewrote all Composite tree evaluation methods to use an iterative `ArrayDeque` with a `Set<Node> visited` cycle guard, completely eliminating recursion from the production codebase.

---

## ⚖️ Production Design Pattern Diagnostic Matrix

| Architectural Challenge | Recommended Design Pattern | Key Production Rule / Hazard |
| :--- | :--- | :--- |
| **Lazy, Thread-Safe Singleton** | **Holder Idiom / Enum** | Never use DCL without `volatile`; Holder idiom is 100% lock-free |
| **Multi-Family Cloud SDK Creation** | **Abstract Factory** | Decouple client from concrete classes; register via SPI |
| **Complex Object Creation with Invariants** | **Builder Pattern** | Validate inside `build()`; create defensive copies (`List.copyOf`) |
| **Expensive Object Initialization ($>500\text{ms}$)** | **Prototype Pattern** | Use Deep-Copy Constructors; avoid broken `Cloneable` |
| **Enhancing Behavior Dynamically** | **Decorator Pattern** | Wrap target dynamically; distinct from access-control Proxy |
| **Eliminating Massive `switch-case` Logic** | **Strategy Pattern** | Autowire via Spring `Map<Type, Strategy>`; guard missing keys |
| **Enforcing Invariant State Transitions** | **State Pattern** | Ensure concrete state instances are 100% stateless |
| **Sequential Pre/Post Filter Pipelines** | **Chain of Responsibility** | Guarantee `next.proceed()` is called or short-circuit cleanly |
| **Decoupling Event Senders & Receivers** | **Observer Pattern** | Use `WeakReference` / `WeakHashMap` to prevent Lapsed Listener leaks |
| **Distributed Multi-Service Transactions** | **Saga Pattern** | Use Orchestrator for complex flows; define semantic compensations |
| **Dual-Write Atomicity (DB + Kafka)** | **Transactional Outbox** | Insert outbox record in same local DB transaction; tail via CDC |
| **Read-Heavy Query Bottlenecks** | **CQRS Pattern** | Separate write commands from denormalized read projections |

---

[🏠 Back to Home](README.md) | [🎨 Design Patterns Hub](ai-algorithms/design_patterns/README.md) | [🧠 DSA Master Guide](ai-algorithms/dsa_master_guide.md) | [🏛️ SQL Scenarios Master Guide](sql_scenarios_master_guide.md)
