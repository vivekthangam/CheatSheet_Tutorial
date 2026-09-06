[🏠 Back to Home](README.md) | [🍃 Spring Boot Master Guide](spring_master_guide.md) | [☕ Java Interview Master Guide](java_interview_master_guide.md)

# 📖 Enterprise Java, Spring & Distributed Systems: Technical Terms & Core Concepts Encyclopedia

[![Java](https://img.shields.io/badge/Java-17%20%2F%2021%20LTS-orange.svg?style=for-the-badge&logo=openjdk)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3%2B-brightgreen.svg?style=for-the-badge&logo=springboot)](https://spring.io/projects/spring-boot)
[![Architecture](https://img.shields.io/badge/Architecture-Zero%20Jargon%20Encyclopedia-blue.svg?style=for-the-badge)](https://github.com/)
[![Level](https://img.shields.io/badge/Target-Junior%20to%20Staff%2B-red.svg?style=for-the-badge)](https://github.com/)

Welcome to the **Foundational Technical Terms Encyclopedia**. If you have ever read enterprise documentation or senior interview scenarios and felt lost seeing terms like **JDK Dynamic Proxy**, **CGLIB**, **AspectJ Weaving**, **Dirty Checking**, **Pooled-lo**, **HikariCP Sizing**, **AEAD Nonce**, **WiredTiger Eviction**, **Netty EventLoops**, or **Transactional Outbox**, this guide is built specifically for you.

Every single term in this guide strictly follows the **6-Part Zero-Ambiguity Breakdown**:
1. **Plain-English Definition & Real-World Analogy** (Zero circular jargon)
2. **Why It Exists & The Exact Problem It Solves** (What broke before this existed?)
3. **Under-the-Hood Mechanics** (How it works in JVM memory, bytecode, OS, or network)
4. **How To Use It** (Clean, minimal, copy-pasteable production code blueprint)
5. **Common Issues, Traps & "Gotchas"** (What catches developers off-guard?)
6. **Comparison Matrix & Key Takeaway** (How it compares to alternatives)

---

## 📑 Master Table of Contents

- [Section 1: Spring AOP & Bytecode Engineering Terms](#section-1-spring-aop--bytecode-engineering-terms)
  - [1.1 The Proxy Pattern (Mental Model)](#11-the-proxy-pattern-mental-model)
  - [1.2 JDK Dynamic Proxy (`java.lang.reflect.Proxy`)](#12-jdk-dynamic-proxy-javalangreflectproxy)
  - [1.3 CGLIB & Byte Buddy Class Subclassing](#13-cglib--byte-buddy-class-subclassing)
  - [1.4 AspectJ & Bytecode Weaving (CTW vs LTW)](#14-aspectj--bytecode-weaving-ctw-vs-ltw)
  - [1.5 AOP Anatomy: Aspect, Advice, JoinPoint, Pointcut](#15-aop-anatomy-aspect-advice-joinpoint-pointcut)
  - [1.6 Self-Invocation & Proxy Bypass Trap (`this.method()`)](#16-self-invocation--proxy-bypass-trap-thismethod)
- [Section 2: Spring IoC Container & Bean Architecture Terms](#section-2-spring-ioc-container--bean-architecture-terms)
  - [2.1 Inversion of Control (IoC) & Dependency Injection (DI)](#21-inversion-of-control-ioc--dependency-injection-di)
  - [2.2 BeanPostProcessor (BPP) vs BeanFactoryPostProcessor (BFPP)](#22-beanpostprocessor-bpp-vs-beanfactorypostprocessor-bfpp)
  - [2.3 Circular Dependencies & The 3-Level Singleton Cache](#23-circular-dependencies--the-3-level-singleton-cache)
  - [2.4 `@Configuration(proxyBeanMethods = true)` (Full vs Lite Mode)](#24-configurationproxybeanmethods--true-full-vs-lite-mode)
- [Section 3: JPA, Hibernate & Database ORM Terms](#section-3-jpa-hibernate--database-orm-terms)
  - [3.1 Persistence Context & Entity Lifecycle States](#31-persistence-context--entity-lifecycle-states)
  - [3.2 Dirty Checking & Hibernate Snapshot Tracking](#32-dirty-checking--hibernate-snapshot-tracking)
  - [3.3 Hibernate Byte Buddy Proxy & `LazyInitializationException`](#33-hibernate-byte-buddy-proxy--lazyinitializationexception)
  - [3.4 The N+1 Query Problem & Solutions (`JOIN FETCH` vs EntityGraph)](#34-the-n1-query-problem--solutions-join-fetch-vs-entitygraph)
  - [3.5 Optimistic (`@Version`) vs Pessimistic Locking (`SELECT FOR UPDATE`)](#35-optimistic-version-vs-pessimistic-locking-select-for-update)
  - [3.6 Primary Key Generation: `IDENTITY` vs `SEQUENCE` (pooled-lo)](#36-primary-key-generation-identity-vs-sequence-pooled-lo)
- [Section 4: Database Connection Pooling & JDBC Terms](#section-4-database-connection-pooling--jdbc-terms)
  - [4.1 Connection Pooling & The HikariCP Sizing Formula](#41-connection-pooling--the-hikaricp-sizing-formula)
  - [4.2 Connection Leak Detection Threshold](#42-connection-leak-detection-threshold)
  - [4.3 `TransactionTemplate` vs Declarative `@Transactional`](#43-transactiontemplate-vs-declarative-transactional)
  - [4.4 Transaction Propagation Scopes (`REQUIRED` vs `REQUIRES_NEW`)](#44-transaction-propagation-scopes-required-vs-requires_new)
- [Section 5: JSON & Serialization Terms (Jackson)](#section-5-json--serialization-terms-jackson)
  - [5.1 Streaming API vs Tree Model vs Databind](#51-streaming-api-vs-tree-model-vs-databind)
  - [5.2 Type Erasure & `TypeReference<T>`](#52-type-erasure--typereferencet)
  - [5.3 Polymorphic Deserialization & RCE Gadget Chains](#53-polymorphic-deserialization--rce-gadget-chains)
  - [5.4 `@JsonView` Dynamic Filtering & Cyclic References](#54-jsonview-dynamic-filtering--cyclic-references)
- [Section 6: Enterprise Cryptography Terms](#section-6-enterprise-cryptography-terms)
  - [6.1 Symmetric vs Asymmetric Cryptography](#61-symmetric-vs-asymmetric-cryptography)
  - [6.2 Block Cipher Modes: ECB vs CBC vs AES-GCM (AEAD)](#62-block-cipher-modes-ecb-vs-cbc-vs-aes-gcm-aead)
  - [6.3 Initialization Vector (IV) / Nonce & Nonce Reuse Disaster](#63-initialization-vector-iv--nonce--nonce-reuse-disaster)
  - [6.4 Password Hashing: BCrypt vs Argon2id (Memory Hardness)](#64-password-hashing-bcrypt-vs-argon2id-memory-hardness)
  - [6.5 Digital Signatures: RSA-4096 vs Ed25519](#65-digital-signatures-rsa-4096-vs-ed25519)
  - [6.6 Envelope Encryption (KMS KEK + Local DEK)](#66-envelope-encryption-kms-kek--local-dek)
  - [6.7 Timing Attacks & Constant-Time Comparisons](#67-timing-attacks--constant-time-comparisons)
- [Section 7: Distributed Systems, Messaging & Kafka Terms](#section-7-distributed-systems-messaging--kafka-terms)
  - [7.1 Message Queue vs Distributed Append-Only Event Log](#71-message-queue-vs-distributed-append-only-event-log)
  - [7.2 Consumer Rebalance Storms & Cooperative Sticky Assignor](#72-consumer-rebalance-storms--cooperative-sticky-assignor)
  - [7.3 Delivery Guarantees: At-Least-Once vs Exactly-Once (EOS)](#73-delivery-guarantees-at-least-once-vs-exactly-once-eos)
  - [7.4 Poison Pills & Dead Letter Topics (DLT)](#74-poison-pills--dead-letter-topics-dlt)
  - [7.5 The Transactional Outbox Pattern & Debezium CDC](#75-the-transactional-outbox-pattern--debezium-cdc)
- [Section 8: Reactive Systems & Concurrency Terms](#section-8-reactive-systems--concurrency-terms)
  - [8.1 Thread-per-Request (Tomcat) vs Non-Blocking EventLoop (Netty)](#81-thread-per-request-tomcat-vs-non-blocking-eventloop-netty)
  - [8.2 Reactive Streams Backpressure](#82-reactive-streams-backpressure)
  - [8.3 `subscribeOn()` vs `publishOn()` Thread Hopping](#83-subscribeon-vs-publishon-thread-hopping)
  - [8.4 Virtual Threads (Project Loom) vs Reactive Programming](#84-virtual-threads-project-loom-vs-reactive-programming)
- [Section 9: NoSQL & Polyglot Database Terms (MongoDB)](#section-9-nosql--polyglot-database-terms-mongodb)
  - [9.1 BSON & The 16MB Document Hard Limit](#91-bson--the-16mb-document-hard-limit)
  - [9.2 The WiredTiger Storage Engine & Cache Eviction](#92-the-wiredtiger-storage-engine--cache-eviction)
  - [9.3 The ESR Indexing Rule (Equality, Sort, Range)](#93-the-esr-indexing-rule-equality-sort-range)
  - [9.4 Write Concerns (`w:majority`, `j:true`) & ACID Transactions](#94-write-concerns-wmajority-jtrue--acid-transactions)
- [Section 10: Testing & Quality Engineering Terms](#section-10-testing--quality-engineering-terms)
  - [10.1 Test Slicing (`@WebMvcTest`) vs Full `@SpringBootTest`](#101-test-slicing-webmvctest-vs-full-springboottest)
  - [10.2 ApplicationContext Cache Pollution](#102-applicationcontext-cache-pollution)
  - [10.3 Testcontainers & `@ServiceConnection`](#103-testcontainers--serviceconnection)
  - [10.4 Mock vs Spy (`@MockBean` vs `@SpyBean` Method Evaluation Trap)](#104-mock-vs-spy-mockbean-vs-spybean-method-evaluation-trap)

---

# Section 1: Spring AOP & Bytecode Engineering Terms

---

### 1.1 The Proxy Pattern (Mental Model)
- **Plain-English Definition & Real-World Analogy:**
  A **Proxy** is a middleman or stand-in object that intercepts calls to a real target object.
  *Real-World Analogy:* Think of a VIP celebrity (the Real Object) and their Talent Agent (the Proxy). When a producer calls the phone number, the Agent answers first. The Agent checks security, checks calendar availability, and logs the call. Only if everything is approved does the Agent pass the message to the celebrity.
- **Why It Exists & What It Solves:**
  Without proxies, cross-cutting concerns (like opening database transactions, checking JWT security tokens, or logging execution times) would have to be copy-pasted manually inside every single method of your business logic! Proxies keep your business code 100% clean by intercepting invocations transparently.
- **Under-the-Hood Mechanics:**
```
Caller ---> [ Proxy Object (Checks @Transactional / @PreAuthorize) ]
                  | (Before Advice: Begins DB Transaction)
                  v
            [ Real Target Bean (Runs business code: orderRepository.save()) ]
                  | (After Returning Advice: Commits DB Transaction)
                  v
Caller <--- [ Returns Result ]
```
- **Key Takeaway:** When you inject `@Autowired private OrderService orderService;`, Spring never injects your real class directly; it injects a **Proxy Wrapper** that intercepts every method call!

---

### 1.2 JDK Dynamic Proxy (`java.lang.reflect.Proxy`)
- **Plain-English Definition & Real-World Analogy:**
  A proxy created purely using built-in standard Java reflection APIs. It **strictly requires an Interface**.
  *Real-World Analogy:* An actor hired to play a specific job role (e.g. "Security Guard Interface"). The actor doesn't need to be related to the original guard by family blood (subclass); they just need to wear the uniform (implement the interface).
- **Why It Exists & What It Solves:**
  Before third-party bytecode libraries existed, Java needed a way to create proxy objects on the fly in memory without compiling new `.java` files to disk.
- **Under-the-Hood Mechanics:**
  1. You supply a list of Java interfaces and an `InvocationHandler`.
  2. The JVM compiles brand-new bytecode in memory for a class named `$Proxy0`.
  3. Class `$Proxy0 extends java.lang.reflect.Proxy implements YourInterface`.
  4. When you call `$Proxy0.saveOrder()`, it delegates directly to your `InvocationHandler.invoke(proxy, method, args)`.
- **How To Use It in Plain Java:**
```java
// 1. Interface is MANDATORY for JDK Dynamic Proxy!
public interface PaymentService {
    void processPayment(double amount);
}

// 2. Real Implementation
public class PaymentServiceImpl implements PaymentService {
    public void processPayment(double amount) {
        System.out.println("Processing payment: $" + amount);
    }
}

// 3. Generating the Proxy on the fly:
PaymentService realService = new PaymentServiceImpl();
PaymentService proxyInstance = (PaymentService) Proxy.newProxyInstance(
    PaymentService.class.getClassLoader(),
    new Class<?>[] { PaymentService.class },
    (proxy, method, args) -> {
        System.out.println("[LOG BEFORE] Starting transaction for " + method.getName());
        Object result = method.invoke(realService, args); // Calls real method
        System.out.println("[LOG AFTER] Transaction committed");
        return result;
    }
);

proxyInstance.processPayment(100.0);
```
- **Common Issues & Gotchas:**
  - **ClassCastException Trap:** If your class does not implement an interface, JDK Dynamic Proxy **cannot be used**!
  - If you try to cast the proxy to the concrete class:
    `(PaymentServiceImpl) proxyInstance` $\to$ **Crashes with `ClassCastException: com.sun.proxy.$Proxy0 cannot be cast to PaymentServiceImpl`**! It can ONLY be cast to the interface `PaymentService`.

---

### 1.3 CGLIB & Byte Buddy Class Subclassing
- **Plain-English Definition & Real-World Analogy:**
  A proxy mechanism that generates an **in-memory subclass** (child class) of your concrete class. It does **NOT** require any interfaces!
  *Real-World Analogy:* A stunt double who is a literal biological clone (subclass) of the main actor. They inherit all the actor's physical traits directly.
- **Why It Exists & What It Solves:**
  Most developers write classes without interfaces (e.g. `public class OrderService {}`). JDK Dynamic Proxies failed completely on these classes. CGLIB (and its modern successor **Byte Buddy**) generates bytecode that subclasses the class directly.
- **Under-the-Hood Mechanics:**
  1. CGLIB inspects `OrderService.class`.
  2. Using low-level bytecode manipulation (ASM / Byte Buddy), it dynamically defines a new class in the JVM:
     `public class OrderService$$EnhancerBySpringCGLIB extends OrderService`
  3. It overrides every public method and replaces the method body with an interceptor callback (`MethodInterceptor`).
  4. When a method is invoked, it runs the interceptor and calls `super.method()` to reach your code!
- **CGLIB vs JDK Proxy in Spring Boot:**
  > [!NOTE]
  > Since **Spring Boot 2.0+**, Spring Boot sets `spring.aop.proxy-target-class=true` **by default**! This means Spring Boot uses **CGLIB / Byte Buddy subclassing for ALL beans**, even if they implement interfaces!
- **Common Issues & Gotchas:**
  - **`final` Methods/Classes Break CGLIB:** In Java, a `final class` cannot be subclassed, and a `final method` cannot be overridden! If you put `@Transactional` on a `public final void charge()`, CGLIB cannot override it. The method executes **without any transaction interceptor**, causing silent transaction failures!
  - **Constructor Double Invocation:** Because CGLIB creates a subclass (`extends MyService`), the target class's constructor is called twice: once for the real bean instance, and once for the CGLIB proxy instance! Keep constructors lean and never put heavy business logic in constructors.

---

### 1.4 AspectJ & Bytecode Weaving (CTW vs LTW)
- **Plain-English Definition & Real-World Analogy:**
  While Spring AOP creates runtime wrappers (proxies), **AspectJ** physically modifies the compiled `.class` bytecode directly, injecting interceptor instructions straight into the method bodies!
  *Real-World Analogy:*
  - **Spring Proxy**: Putting an armored bodyguard in front of the door (a separate wrapper object).
  - **AspectJ Weaving**: Giving the person bionic implants directly inside their DNA (modifying the bytecode itself). No bodyguard needed!
- **Why It Exists & What It Solves:**
  Spring AOP proxies have two severe limitations:
  1. They only intercept `public` methods on Spring beans.
  2. They cannot intercept self-invocations (`this.methodB()`).
  3. They cannot intercept objects created with `new MyObject()`.
  AspectJ solves all of this by modifying the bytecode directly.
- **The Weaving Types Explained:**
  - **CTW (Compile-Time Weaving):** The AspectJ compiler (`ajc`) weaves advice directly into your `.class` files during `mvn compile`.
  - **LTW (Load-Time Weaving):** The standard Java compiler compiles normally, but when the JVM starts, a Java agent (`-javaagent:aspectjweaver.jar`) intercepts class loading and weaves the bytecode as classes are loaded into RAM.
- **Comparison Matrix:**

| Feature | Spring AOP (Proxies) | AspectJ (Weaving) |
| :--- | :--- | :--- |
| **Mechanism** | Runtime Proxy (JDK or CGLIB) | Bytecode Modification (CTW or LTW) |
| **Interface Required?** | Only for JDK Proxy (CGLIB does not need it) | Never |
| **Self-Invocation?** | ❌ Bypassed (fails) | ✅ Intercepted perfectly |
| **Private/Static Methods?** | ❌ Cannot intercept | ✅ Can intercept |
| **Performance** | Slight reflection/call overhead | ⚡ Maximum speed (zero proxy overhead) |
| **Complexity** | Extremely simple (zero build plugins) | Requires `ajc` compiler or `-javaagent` |

---

### 1.5 AOP Anatomy: Aspect, Advice, JoinPoint, Pointcut
Here is the definitive guide to memorizing the 4 fundamental AOP terms without confusion:

```
[ POINTCUT ] -------------------> WHICH methods should be targeted?
(e.g., @annotation(Transactional))

[ ADVICE ] ---------------------> WHAT code should run, and WHEN?
(@Before, @After, @Around)

[ JOINPOINT ] ------------------> The EXACT execution instant currently running.
(method name, arguments, target object)

[ ASPECT ] ---------------------> The CONTAINER class combining Pointcut + Advice.
(@Aspect public class SecurityAspect)
```

1. **Aspect:** The Java class that contains your cross-cutting feature (e.g. `LoggingAspect`, `TransactionAspect`). Annotated with `@Aspect`.
2. **Advice:** The action taken at a specific point.
   - `@Before`: Runs before the target method executes.
   - `@AfterReturning`: Runs after the method returns successfully.
   - `@AfterThrowing`: Runs only if the method throws an exception.
   - `@After`: Runs always (like a `finally` block).
   - `@Around`: The most powerful advice. Wraps the method completely. You must explicitly call `proceedingJoinPoint.proceed()` to let the real method run!
3. **Pointcut:** A predicate or filter expression that matches target methods (e.g. `execution(* com.example.service.*.*(..))`).
4. **JoinPoint:** The specific point during program execution (in Spring AOP, always a method execution). The `JoinPoint` object gives you access to the method arguments, method name, and target object.

---

### 1.6 Self-Invocation & Proxy Bypass Trap (`this.method()`)
- **The Exact Scenario:**
  ```java
  @Service
  public class OrderService {

      public void processOrder() {
          // CALLING INTERNAL METHOD DIRECTLY VIA 'this':
          this.saveToDatabase(); 
      }

      @Transactional // <--- BUG: THIS ANNOTATION IS COMPLETELY IGNORED!
      public void saveToDatabase() {
          orderRepository.save(new Order());
      }
  }
  ```
- **Why It Fails Under the Hood:**
  1. An external controller calls `orderService.processOrder()`.
  2. The call hits the **CGLIB Proxy**.
  3. `processOrder()` has no `@Transactional`, so the proxy simply forwards the call to the **Real Target Object**.
  4. Inside the Real Target Object, Java executes `this.saveToDatabase()`.
  5. **`this` points to the RAW INSTANCE, NOT the Proxy!**
  6. The proxy is completely bypassed! No database transaction is ever started. If an exception occurs, no rollback happens!
- **The Three Production Fixes:**
  1. **Architectural Separation (Recommended):** Move `saveToDatabase()` into a separate bean (`OrderPersistenceService`) so it is invoked through an injected proxy.
  2. **Self-Injection:** Inject `OrderService` into itself using `@Lazy`:
     ```java
     @Autowired @Lazy private OrderService self;
     public void processOrder() { self.saveToDatabase(); } // Calls via proxy!
     ```
  3. **AopContext.currentProxy():**
     Enable `@EnableAspectJAutoProxy(exposeProxy = true)` and call:
     `((OrderService) AopContext.currentProxy()).saveToDatabase();`

---

# Section 2: Spring IoC Container & Bean Architecture Terms

---

### 2.1 Inversion of Control (IoC) & Dependency Injection (DI)
- **Plain-English Definition & Real-World Analogy:**
  - **Traditional Code:** You write `new DatabaseConnection()` inside your class. Your class controls its dependencies.
  - **Inversion of Control (IoC):** You surrender control. You say *"I need a database connection, give me one."* An external framework (the Spring IoC Container) instantiates it and injects it into you.
  *Real-World Analogy:* Traditional code is going to the forest, cutting down a tree, building a chair, and sitting on it. IoC is moving into a fully furnished apartment where the furniture is already provided for you.
- **Dependency Injection (DI):** The specific design pattern used to implement IoC. Spring passes the dependency into your class via **Constructor Injection** (the enterprise gold standard):
```java
@Service
public class UserService {
    private final UserRepository userRepository; // Immutable, clean, unit-testable!

    // Constructor Injection: Spring injects UserRepository automatically
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
}
```

---

### 2.2 BeanPostProcessor (BPP) vs BeanFactoryPostProcessor (BFPP)
- **BeanFactoryPostProcessor (BFPP):**
  - Runs **EARLY**, before any bean instances are created!
  - It operates on **BeanDefinition** metadata (configuration instructions).
  - *Example:* `PropertySourcesPlaceholderConfigurer` resolves `${db.url}` placeholders inside `@Value` annotations before objects are constructed.
- **BeanPostProcessor (BPP):**
  - Runs **LATE**, on every single bean instance after it is physically instantiated in heap memory!
  - Contains two hooks:
    1. `postProcessBeforeInitialization(Object bean, String beanName)`
    2. `postProcessAfterInitialization(Object bean, String beanName)` $\to$ **THIS IS WHERE SPRING PROXIES ARE CREATED!**
  - If a bean has `@Transactional`, `@Async`, or custom aspects, Spring's `AnnotationAwareAspectJAutoProxyCreator` (a BPP) intercepts the bean in `postProcessAfterInitialization` and swaps the raw bean with a CGLIB or JDK Proxy!

---

### 2.3 Circular Dependencies & The 3-Level Singleton Cache
- **The Problem:** Service A requires Service B in its constructor, and Service B requires Service A in its constructor. Who gets instantiated first? Chicken or egg deadlock!
- **How Spring Resolves Setter/Field Circular Dependencies:**
  Spring uses a **3-Level Cache** inside `DefaultSingletonBeanRegistry`:
  1. `singletonObjects` (1st Level): Fully initialized, ready-to-use beans.
  2. `earlySingletonObjects` (2nd Level): Raw bean instance instantiated, but dependencies not yet injected.
  3. `singletonFactories` (3rd Level): Object factories (`ObjectFactory<?>`) capable of wrapping an early bean in a CGLIB proxy if circular dependency occurs!
- **Spring Boot 2.6+ Default:** Circular dependencies are **strictly disabled by default** in modern Spring Boot! If you have circular dependencies, refactor your architecture using Domain-Driven Design (events or mediator pattern).

---

### 2.4 `@Configuration(proxyBeanMethods = true)` (Full vs Lite Mode)
- **The Trap:**
  ```java
  @Configuration
  public class AppConfig {

      @Bean
      public SecurityService securityService() {
          return new SecurityService();
      }

      @Bean
      public OrderService orderService() {
          // Calling securityService() directly as a Java method!
          return new OrderService(securityService());
      }

      @Bean
      public UserService userService() {
          // Calling securityService() a SECOND time!
          return new UserService(securityService());
      }
  }
  ```
- **Does `new SecurityService()` get called twice, creating two separate objects?**
  - If `proxyBeanMethods = true` (Default Full Mode): **NO!** Spring wraps `AppConfig` in a CGLIB proxy. When `orderService()` calls `securityService()`, CGLIB intercepts the call, checks the Spring container, and returns the **exact same singleton instance**!
  - If `proxyBeanMethods = false` (Lite Mode / GraalVM Native Friendly): **YES!** No CGLIB proxy is generated; it behaves like standard Java code, instantiating multiple instances. Lite mode accelerates startup time and saves RAM.

---

# Section 3: JPA, Hibernate & Database ORM Terms

---

### 3.1 Persistence Context & Entity Lifecycle States
- **Persistence Context:** A first-level in-memory cache and staging area managed by Hibernate's `EntityManager`. Every database row read or written during a transaction lives here.
- **The 4 Entity Lifecycle States:**
```
  [ New / Transient ] --( persist() )--> [ Managed ] <--( find() / query )-- [ Database ]
           |                                  |
           |                               ( detach() / tx commit )
           |                                  v
           +---------------------------> [ Detached ] --( merge() )--> [ Managed ]
                                              |
                                           ( remove() )
                                              v
                                         [ Removed ]
```
1. **Transient / New:** Created with `new User()`. Has no database ID and is NOT tracked by Hibernate.
2. **Managed / Persistent:** Associated with an active `EntityManager` session and has a database ID. **Any change to its fields is automatically saved to the database on transaction commit via Dirty Checking!**
3. **Detached:** Session is closed. The object has a database ID, but Hibernate is no longer tracking field changes.
4. **Removed:** Marked for deletion. Hibernate will issue a SQL `DELETE` on commit.

---

### 3.2 Dirty Checking & Hibernate Snapshot Tracking
- **Plain-English Definition:**
  You never need to call `orderRepository.save(order)` after updating an entity inside a `@Transactional` method! Hibernate automatically detects that you changed a field and writes the SQL `UPDATE` statement for you.
- **Under-the-Hood Mechanics:**
  1. When an entity is loaded from the database, Hibernate puts the entity into the Persistence Context.
  2. Simultaneously, Hibernate takes an exact **deep copy (snapshot)** of the entity's initial state in memory.
  3. When the transaction prepares to commit, Hibernate initiates a **Flush**.
  4. Hibernate iterates over every managed entity, compares its current state property-by-property against the initial snapshot.
  5. If any field differs (it is "dirty"), Hibernate dynamically generates and executes:
     `UPDATE customer SET status = 'ACTIVE' WHERE id = 1;`

---

### 3.3 Hibernate Byte Buddy Proxy & `LazyInitializationException`
- **Plain-English Definition:**
  When you declare `@ManyToOne(fetch = FetchType.LAZY) private Department department;`, Hibernate does **NOT** query the `departments` table immediately. Instead, it injects a **Byte Buddy HibernateProxy** subclass placeholder!
- **Under-the-Hood Mechanics:**
  - The proxy holds the department `id`, but all other fields (`name`, `budget`) are `null`.
  - The first time your code invokes `department.getName()`, the proxy intercepts the method call, checks if the Hibernate `Session` is still open, queries the database transparently, and populates the fields.
- **The Infamous `LazyInitializationException: could not initialize proxy - no Session`:**
  - If you fetch an `Employee` inside a `@Transactional` service, and the transaction commits (closing the `EntityManager` session)...
  - Later, inside your REST Controller or Jackson JSON serializer, code executes `employee.getDepartment().getName()`...
  - The proxy attempts to fetch the department from the database, discovers the **session is closed and connection returned to pool**, and throws `LazyInitializationException`!
  - **The Fix:** Fetch the relationship eagerly inside the transaction query using `JOIN FETCH` or DTO Projections!

---

### 3.4 The N+1 Query Problem & Solutions (`JOIN FETCH` vs EntityGraph)
- **The Problem:**
  You fetch 100 `Orders` from the database:
  `List<Order> orders = orderRepository.findAll();` (1 Query: `SELECT * FROM orders;`)
  Then your code loops over them to print customer names:
  `for (Order o : orders) { System.out.println(o.getCustomer().getName()); }`
  Because `customer` is lazy-loaded, Hibernate executes **100 individual SQL queries** (`SELECT * FROM customers WHERE id = ?`), one for every single order!
  $$\text{Total Queries} = 1 + 100 = 101\text{ SQL queries over the network!}$$
  This destroys database performance and saturates network connections.
- **The Two Production Solutions:**
  1. **`JOIN FETCH` in JPQL:**
     ```java
     @Query("SELECT o FROM Order o JOIN FETCH o.customer WHERE o.status = :status")
     List<Order> findAllWithCustomer(@Param("status") String status);
     ```
     Executes a single SQL `INNER JOIN`, loading both Orders and Customers in **1 single network round-trip**!
  2. **`@EntityGraph`:** Declaratively instructs Hibernate to join the relationship without writing custom JPQL:
     ```java
     @EntityGraph(attributePaths = {"customer", "items"})
     List<Order> findByStatus(String status);
     ```

---

### 3.5 Optimistic (`@Version`) vs Pessimistic Locking (`SELECT FOR UPDATE`)
- **Optimistic Locking (`@Version`):**
  - **Assumption:** Collisions are rare. Do not lock database rows.
  - **Mechanics:** Add an integer `@Version private Long version;` to the entity.
  - When updating:
    `UPDATE account SET balance = 150, version = 2 WHERE id = 1 AND version = 1;`
  - If another transaction modified the row first, `version` is already `2`. The `UPDATE` modifies 0 rows, and Hibernate throws `OptimisticLockException`!
  - Ideal for high-throughput read-heavy web applications.
- **Pessimistic Locking (`PESSIMISTIC_WRITE`):**
  - **Assumption:** Collisions are frequent (e.g. ticket booking, flash sales).
  - **Mechanics:** Instructs the database engine to acquire an exclusive row-level lock:
    `SELECT * FROM tickets WHERE id = 1 FOR UPDATE;`
  - All other transactions trying to read or update this row are physically **blocked at the database level** until your transaction commits!
  - Prevents race conditions, but risks database deadlocks if not used carefully.

---

### 3.6 Primary Key Generation: `IDENTITY` vs `SEQUENCE` (pooled-lo)
- **`GenerationType.IDENTITY` (The Batching Killer):**
  - Delegates ID generation to the database auto-increment column (`AUTO_INCREMENT` / `SERIAL`).
  - **The Catch:** Hibernate cannot know the ID of a newly saved entity until the database generates it.
  - Therefore, Hibernate **MUST execute `INSERT` immediately on `save()`**, completely bypassing JDBC batching! If you save 10,000 entities, Hibernate executes 10,000 separate network calls!
- **`GenerationType.SEQUENCE` with `pooled-lo` Optimizer (Enterprise Standard):**
  - Allocates IDs from a database sequence in blocks (e.g. 50 at a time).
  - Hibernate calls `nextval('my_seq')` once to reserve IDs 100 to 149.
  - Hibernate assigns IDs in JVM memory instantly and batches all 50 `INSERT` statements into a single network packet, accelerating write throughput by **$10\times - 50\times$**!

---

# Section 4: Database Connection Pooling & JDBC Terms

---

### 4.1 Connection Pooling & The HikariCP Sizing Formula
- **What is a Connection Pool?**
  Opening a physical TCP connection to PostgreSQL or MySQL requires a three-way TCP handshake, TLS negotiation, authentication credentials verification, and process/thread allocation on the database server (taking 20ms to 100ms!).
  A connection pool (**HikariCP**, Spring Boot's default) maintains a warm pool of pre-established database connections, lending them to worker threads in 0.1ms.
- **The Sizing Formula (Why 10 Connections Beat 100):**
  Developers often mistakenly believe: *"More traffic = more database connections!"*
  In reality, database servers have fixed CPU cores and disk I/O channels. Running 1,000 concurrent database connections on a 16-core database server causes catastrophic **CPU thread context switching and disk thrashing**, slowing queries to a crawl.
  $$\text{Optimal Connections} = (\text{CPU Cores} \times 2) + \text{Effective Spindle Count (Disks)}$$
  On a 16-core cloud database with SSDs:
  $$(16 \times 2) + 1 = \mathbf{33\text{ connections max across all pods!}}$$

---

### 4.2 Connection Leak Detection Threshold
- **What is a Connection Leak?**
  A thread borrows a physical JDBC connection from HikariCP, but due to an unhandled exception or missing `finally` block, the connection is never closed or returned to the pool. Over time, all connections are leaked, and the application freezes with `Connection is not available, request timed out after 30000ms`.
- **The Fix (`leakDetectionThreshold`):**
  Configure in `application.yml`:
  ```yaml
  spring.datasource.hikari.leak-detection-threshold: 2000 # 2 seconds
  ```
  If any thread holds a connection longer than 2 seconds, HikariCP prints an actionable warning log with the **exact line of code and stack trace** where the connection was checked out!

---

### 4.3 `TransactionTemplate` vs Declarative `@Transactional`
- **The Problem with `@Transactional` on Large Methods:**
  When a method is annotated with `@Transactional`, Spring acquires a physical database connection **upon entering the method**.
  If that method executes a slow 4-second external REST API call (e.g. Stripe payment), the thread holds a database connection **idle doing zero database work for 4 seconds**! Under moderate load, your database pool is exhausted instantly.
- **The Fix: Programmatic `TransactionTemplate`:**
  Keep external calls completely outside the transaction; narrow database work to minimal milliseconds:
  ```java
  // 1. External call: ZERO database connection held!
  PaymentResponse res = stripeClient.charge(card, amount);

  // 2. Brief 2ms database transaction:
  transactionTemplate.executeWithoutResult(status -> {
      orderRepository.updateStatus(orderId, res.status());
  });
  ```

---

### 4.4 Transaction Propagation Scopes (`REQUIRED` vs `REQUIRES_NEW`)
- **`REQUIRED` (Default):**
  - If a transaction already exists, join it. If not, create a new one.
  - If Child method throws an exception and catches it, the transaction is marked **Rollback-Only**; Parent will fail on commit!
- **`REQUIRES_NEW`:**
  - Suspends the existing transaction and opens an **independent, isolated physical transaction** on a second database connection.
  - Commits or rolls back independently of Parent.
  - *Ideal for:* Audit logging, security event tracking, or billing logs that must persist even if the main business transaction fails!

---

# Section 5: JSON & Serialization Terms (Jackson)

---

### 5.1 Streaming API vs Tree Model vs Databind
Jackson offers 3 completely different modes to process JSON:
1. **Streaming API (`JsonParser` / `JsonGenerator`):**
   - High-speed, token-by-token pull parser.
   - Allocates virtually zero heap memory ($O(1)$ RAM).
   - *Best for:* Processing multi-gigabyte files that would crash the JVM with OutOfMemoryError.
2. **Tree Model (`JsonNode` / `ObjectMapper.readTree`):**
   - Builds an in-memory document object model (DOM) of the entire JSON file.
   - Flexible, but incurs massive memory amplification ($5\times - 7\times$ file size in RAM).
   - *Best for:* Ad-hoc queries where you only need a few nested fields from a dynamic, unpredictable schema.
3. **Databind (`ObjectMapper.readValue(json, Class)`):**
   - Maps JSON directly to strongly typed Java POJOs / Records.
   - Built on top of the Streaming API. The standard for 99% of enterprise REST APIs.

---

### 5.2 Type Erasure & `TypeReference<T>`
- **The Java Generic Trap:**
  In Java, generic types (e.g. `List<Order>`) exist only at compile-time. At runtime, the JVM strips them via **Type Erasure**, turning them into raw `List<Object>`.
- **Why Jackson Fails:**
  If you call `objectMapper.readValue(json, List.class);`, Jackson has no idea what object is inside the list! It defaults to `List<LinkedHashMap>`, causing `ClassCastException` when you treat elements as `Order`.
- **The Solution: Super Type Tokens (`TypeReference`):**
  ```java
  // Anonymous inner class captures generic type information at runtime!
  List<Order> orders = objectMapper.readValue(json, new TypeReference<List<Order>>() {});
  ```

---

### 5.3 Polymorphic Deserialization & RCE Gadget Chains
- **What is Polymorphic Deserialization?**
  Deserializing an interface into one of several concrete child classes based on a type identifier (e.g. `DomainEvent` $\to$ `OrderCreatedEvent` or `OrderCancelledEvent`).
- **The RCE (Remote Code Execution) Vulnerability:**
  In older Jackson setups, developers enabled `objectMapper.enableDefaultTyping()`. This allowed the JSON payload to specify **ANY arbitrary Java class name**!
  Attackers sent malicious payloads containing "gadget classes" (classes available on the classpath that execute code in their constructors/setters, like JNDI lookup classes), allowing attackers to execute remote shell commands on your server!
- **The Defense:**
  1. Never use `enableDefaultTyping()`.
  2. Use explicit `@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)` with `@JsonSubTypes` using logical string aliases (`"type": "ORDER_CREATED"`).
  3. If dynamic typing is needed, configure `BasicPolymorphicTypeValidator` with a strict package allowlist!

---

### 5.4 `@JsonView` Dynamic Filtering & Cyclic References
- **`@JsonView`:**
  Allows a single Java class / Record to serialize different fields depending on the user's role:
  - Public view: Shows `username`, `email`.
  - Admin view: Shows `username`, `email`, `ssn`, `creditCardNumber`.
- **Cyclic References (`StackOverflowError`):**
  When JPA Entity A references Entity B, and Entity B references Entity A.
  Jackson serializes A $\to$ B $\to$ A $\to$ B in an infinite loop until the thread stack explodes.
  *Fix:* Annotate the child with `@JsonIgnore`, use `@JsonManagedReference` / `@JsonBackReference`, or declare `@JsonIdentityInfo` to serialize IDs on cyclic loops.

---

# Section 6: Enterprise Cryptography Terms

---

### 6.1 Symmetric vs Asymmetric Cryptography
- **Symmetric Encryption (Single Shared Secret):**
  - The same secret key is used to encrypt AND decrypt data.
  - Algorithms: **AES-256-GCM**, ChaCha20.
  - *Pros:* Ultra-fast (hardware accelerated by CPU AES-NI instructions; gigabytes per second).
  - *Cons:* Both sender and receiver must share the secret key securely.
- **Asymmetric Encryption (Public / Private Key Pair):**
  - **Public Key:** Shared with the world; used to encrypt data or verify signatures.
  - **Private Key:** Guarded secretly; used to decrypt data or generate signatures.
  - Algorithms: **RSA-4096**, **Ed25519**, **ECDSA**.
  - *Pros:* No need to share private keys across untrusted networks.
  - *Cons:* Computationally slow ($100\times - 1000\times$ slower than AES).

---

### 6.2 Block Cipher Modes: ECB vs CBC vs AES-GCM (AEAD)
- **ECB (Electronic Codebook) [DANGEROUS / BROKEN]:**
  - Encrypts each 16-byte block independently with the same key.
  - Identical plaintext blocks produce identical ciphertext blocks! An attacker can see patterns in the data (the classic ECB Penguin visual vulnerability). **Forbidden in production!**
- **CBC (Cipher Block Chaining):**
  - XORs each plaintext block with the previous ciphertext block.
  - Vulnerable to **Padding Oracle Attacks** if ciphertext integrity is not verified with an HMAC.
- **GCM (Galois/Counter Mode) [Enterprise Gold Standard]:**
  - An **AEAD** (Authenticated Encryption with Associated Data) mode.
  - Turns a block cipher into a stream cipher via counter increments.
  - Simultaneously produces an **Authentication Tag (MAC)** over Galois Field $\text{GF}(2^{128})$.
  - Guarantees both **Confidentiality** (data cannot be read) and **Integrity** (any tampering with even 1 bit of ciphertext is instantly detected!).

---

### 6.3 Initialization Vector (IV) / Nonce & Nonce Reuse Disaster
- **What is an IV / Nonce?**
  A **Nonce** ("Number used ONCE") is a random, non-secret byte array passed into AES-GCM alongside the key (recommended size: **12 bytes / 96 bits**).
- **The Nonce Reuse Disaster in AES-GCM:**
  If you encrypt two different plaintexts ($P_1$ and $P_2$) using the **exact same key and exact same IV**:
  1. The AES-GCM keystream ($S$) is identical.
  2. XORing both ciphertexts cancels out the keystream: $C_1 \oplus C_2 = P_1 \oplus P_2$.
  3. The secret authentication key $H$ is mathematically recoverable over $\text{GF}(2^{128})$!
  4. An attacker can forge authentication tags and decrypt your data!
  - *Rule:* **Always generate a fresh `SecureRandom` 12-byte IV for EVERY single encryption operation!**

---

### 6.4 Password Hashing: BCrypt vs Argon2id (Memory Hardness)
- **Why Standard Hashes (SHA-256 / MD5) Fail for Passwords:**
  SHA-256 is designed to be ultra-fast for file integrity. An attacker with a modern NVIDIA RTX 4090 GPU cluster can compute **10 billion SHA-256 hashes per second**, cracking 8-character passwords in minutes.
- **BCrypt (CPU-Hard):**
  Uses the Blowfish key schedule to slow down hashing. However, it requires only **4 KB of RAM**, meaning GPUs can still run thousands of guesses in parallel.
- **Argon2id (Memory-Hard - Winner of Password Hashing Competition):**
  Forces the hashing algorithm to allocate a large block of RAM (e.g. **64 MB of RAM per hash**).
  Because GPU cores have tiny local memory, a GPU that could run 1,000 parallel BCrypt hashes can only run a handful of Argon2id hashes, **crushing attacker hardware efficiency by $99.9\%$!**

---

### 6.5 Digital Signatures: RSA-4096 vs Ed25519
- **RSA-4096:**
  - Based on the mathematical difficulty of factoring large prime numbers.
  - Massive key sizes (4096 bits = 512 bytes).
  - Computationally heavy; high CPU usage when signing high-frequency JWT tokens.
- **Ed25519 (Edwards-curve DSA on Curve25519):**
  - Based on Twisted Edwards elliptic curves.
  - Tiny keys: **256-bit (32 bytes)** providing 128 bits of cryptographic security.
  - **$10\times - 20\times$ faster than RSA-4096** with zero vulnerability to Bleichenbacher timing attacks! Native in Java 15+.

---

### 6.6 Envelope Encryption (KMS KEK + Local DEK)
- **The Problem:** Calling AWS KMS or HashiCorp Vault over the network to encrypt millions of database rows causes high network latency, expensive KMS per-request API costs, and breaches KMS 4KB payload limits.
- **The Envelope Pattern Solution:**
  1. Application calls KMS: `GenerateDataKey(masterKeyId)`.
  2. KMS returns:
     - **Plaintext Data Key (DEK)**: A fast, local 32-byte AES key.
     - **Encrypted Data Key**: The DEK encrypted by KMS HSM master key (KEK).
  3. The application encrypts the customer data locally in RAM using AES-GCM with the Plaintext DEK at gigabytes per second!
  4. Application wipes the Plaintext DEK from RAM.
  5. Store the **Encrypted DEK alongside the ciphertext** in the database row!

---

### 6.7 Timing Attacks & Constant-Time Comparisons
- **The Vulnerability:**
  Standard `String.equals()` or `Arrays.equals()` checks bytes one-by-one and **returns `false` immediately on the first mismatched byte** (Early Exit).
  An attacker measures the response time of your server in nanoseconds. If Byte 0 is correct, the server takes slightly longer before returning `401 Unauthorized`. Byte-by-byte, the attacker reconstructs secret HMAC webhook signatures!
- **The Fix:**
  Use **`MessageDigest.isEqual(byte[] a, byte[] b)`**!
  It checks all bytes unconditionally using bitwise OR accumulators, guaranteeing the execution time is **strictly identical** regardless of where mismatches occur.

---

# Section 7: Distributed Systems, Messaging & Kafka Terms

---

### 7.1 Message Queue vs Distributed Append-Only Event Log
- **Message Queue (e.g. RabbitMQ):**
  - "Smart Broker, Dumb Consumer".
  - Messages are stored in queues. Once a consumer processes and acknowledges a message, **the broker physically deletes the message**.
  - *Best for:* Transient task worker queues.
- **Distributed Event Log (e.g. Apache Kafka):**
  - "Dumb Broker, Smart Consumer".
  - Topics are partitioned, immutable, append-only commit logs written to disk.
  - Messages are **NEVER deleted when read**! They are retained based on time (e.g. 7 days).
  - Consumers track their own reading position via an **Offset**. Multiple independent consumer groups can read the exact same event stream at their own pace.

---

### 7.2 Consumer Rebalance Storms & Cooperative Sticky Assignor
- **What is a Rebalance?**
  When a Kafka consumer pod crashes, scales up, or exceeds `max.poll.interval.ms`, the Kafka Group Coordinator reassigns topic partitions across the remaining healthy consumer pods.
- **The Legacy Eager Rebalance Disaster:**
  Legacy assignors (Range, RoundRobin) revoke **ALL partitions from ALL consumers** (Stop-The-World pause). All data processing halts until the rebalance finishes.
- **The Cooperative Sticky Assignor Solution:**
  Reassigns **ONLY the specific partitions** that moved. All consumers assigned to unchanged partitions continue processing messages with zero pause! Set in Spring Kafka:
  ```yaml
  spring.kafka.consumer.properties.partition.assignment.strategy: org.apache.kafka.clients.consumer.CooperativeStickyAssignor
  ```

---

### 7.3 Delivery Guarantees: At-Least-Once vs Exactly-Once (EOS)
- **At-Most-Once:** Commit offset *before* processing message. If pod crashes, message is lost. Zero duplicates, potential data loss.
- **At-Least-Once (Default):** Process message *then* commit offset. If pod crashes before offset commit, message is re-delivered. Zero data loss, potential duplicates. Requires idempotent consumers.
- **Exactly-Once Semantics (EOS):** Uses Kafka's Transaction Coordinator. Consumer offsets and outgoing messages are committed together in a two-phase commit transaction. Downstream consumers configured with `isolation.level=read_committed` skip aborted transactions.

---

### 7.4 Poison Pills & Dead Letter Topics (DLT)
- **What is a Poison Pill?**
  A malformed message (corrupted bytes, unparseable JSON, schema mismatch) that arrives on a Kafka topic.
  When the consumer deserializer runs inside the poll loop, it throws `SerializationException` *before* reaching your code. Because the offset is never committed, the consumer crashes, restarts, reads the same poison pill, and enters an **infinite crash loop**!
- **The Fix (`ErrorHandlingDeserializer` + DLT):**
  Spring's `ErrorHandlingDeserializer` catches the exception, wraps the failure in headers, passes it safely to `DefaultErrorHandler`, which immediately routes the bad message to a **Dead Letter Topic (`topic.DLT`)**, allowing healthy traffic to continue moving!

---

### 7.5 The Transactional Outbox Pattern & Debezium CDC
- **The Dual-Write Vulnerability:**
  ```java
  orderRepository.save(order); // 1. Writes to PostgreSQL
  kafkaTemplate.send("orders", event); // 2. Writes to Kafka (NETWORK FAILS!)
  ```
  If Kafka is down, the order is saved in the database, but no event is ever published! The database and message broker are permanently desynchronized.
- **The Outbox Solution:**
  1. Create an `outbox_table` in your relational database.
  2. Inside a single local ACID database transaction, save the entity AND insert the event into `outbox_table`. Both succeed or both roll back atomically!
  3. A Change Data Capture (CDC) engine (**Debezium**) reads PostgreSQL's Write-Ahead Log (WAL) directly and streams the committed outbox events to Kafka with guaranteed ordering and zero data loss!

---

# Section 8: Reactive Systems & Concurrency Terms

---

### 8.1 Thread-per-Request (Tomcat) vs Non-Blocking EventLoop (Netty)
- **Tomcat Thread-per-Request (Spring MVC):**
  - Allocates a dedicated thread for every HTTP request (pool size: 200).
  - If a thread calls a slow database or sleeps, that thread blocks and sits idle in OS memory.
  - 200 slow requests saturate the pool, and new incoming requests are rejected.
- **Netty EventLoop (Spring WebFlux):**
  - Allocates only **1 worker thread per CPU core** (e.g. 8 threads on an 8-core CPU).
  - Runs an OS socket multiplexer (`epoll` / `kqueue`).
  - When an I/O request is made, the thread registers a callback with the OS kernel and immediately handles other requests!
  - Handles 100,000 concurrent socket connections with a tiny memory footprint.
  - *Hard Rule:* **Never execute blocking code (`Thread.sleep()`, JDBC) on a Netty EventLoop thread! Doing so freezes the entire server!**

---

### 8.2 Reactive Streams Backpressure
- **The Problem:** A fast publisher emits 50,000 events/sec, but a slow database consumer can only process 1,000 events/sec. Without backpressure, the consumer's internal RAM buffer grows until the JVM crashes with `OutOfMemoryError`.
- **The Reactive Solution:**
  Under the Reactive Streams specification, the consumer pulls data by requesting items: `subscription.request(n)`. The publisher is forbidden from sending more items than requested!
  Strategies when buffers fill:
  - `.onBackpressureBuffer(limit, DROP_OLDEST)`
  - `.onBackpressureDrop()`
  - `.onBackpressureLatest()`

---

### 8.3 `subscribeOn()` vs `publishOn()` Thread Hopping
- **`subscribeOn(Scheduler)`:**
  - Influences **upstream** operators: tells the source publisher which thread pool to start emitting data on.
  - Acts globally backwards.
- **`publishOn(Scheduler)`:**
  - Influences **downstream** operators: switches the execution thread for all operators that appear *after* it in the reactive pipeline!

---

### 8.4 Virtual Threads (Project Loom) vs Reactive Programming
- **Project Reactor (WebFlux):**
  - Maximum efficiency, but requires completely rewriting code into functional reactive chains (`Mono` / `Flux`).
  - Stack traces are notoriously hard to debug.
- **Virtual Threads (Java 21 LTS):**
  - Lightweight JVM-managed threads ($1\text{KB}$ RAM vs $1\text{MB}$ for platform OS threads).
  - You write standard, synchronous, readable blocking code (`orderRepository.findById()`).
  - When a virtual thread blocks on socket I/O, the JVM unmounts it from the underlying carrier OS thread and mounts another virtual thread!
  - Delivers reactive scalability with traditional readable code!

---

# Section 9: NoSQL & Polyglot Database Terms (MongoDB)

---

### 9.1 BSON & The 16MB Document Hard Limit
- **What is BSON?**
  **Binary JSON**: A binary serialization format used by MongoDB to store documents. Adds support for data types not present in JSON (e.g. `Date`, `ObjectId`, raw `BinData`, 64-bit integers).
- **The 16MB Hard Limit:**
  MongoDB strictly limits a single document to **16,777,216 bytes (16MB)**.
  If an embedded array (like user comments or audit events) grows unboundedly, the document breaches 16MB and MongoDB throws `BSONObjectTooLarge`.
  *Design Pattern:* Use the **Subset Pattern** (embed the top 10 most recent items, move the rest to a separate collection).

---

### 9.2 The WiredTiger Storage Engine & Cache Eviction
- **WiredTiger:** MongoDB's default pluggable storage engine.
- **Cache Management:**
  WiredTiger allocates 50% of available RAM (minus 1GB) to its in-memory page cache.
  When documents are modified, pages become "dirty". Background checkpoint threads flush dirty pages to disk every 60 seconds.
  If large unindexed queries force millions of cold documents into RAM, WiredTiger enters an **Eviction Storm**, dumping active working sets and causing database latency to skyrocket.

---

### 9.3 The ESR Indexing Rule (Equality, Sort, Range)
When creating compound indexes in MongoDB for queries containing equality, sort, and range predicates, the index fields **MUST be ordered in this exact sequence**:
1. **E - Equality First:** Exact matches (`status: "ACTIVE"`). Narrows the index search space immediately.
2. **S - Sort Second:** Sort fields (`createdAt: -1`). Allows walking the index B-Tree directly in sorted order, **completely eliminating in-memory sorting**!
3. **R - Range Last:** Range filters (`amount: { $gte: 100 }`, `$in`).
Placing a Range field *before* a Sort field breaks index sort ordering and forces an in-memory sort (which crashes if results exceed 32MB).

---

### 9.4 Write Concerns (`w:majority`, `j:true`) & ACID Transactions
- **Write Concerns:**
  - `w:1`: Acknowledged as soon as Primary writes to memory. (Risk: Data loss if Primary crashes before replication).
  - `w:majority`: Acknowledged only after a majority of replica set nodes receive the write in RAM. Protects against election rollbacks.
  - `j:true` (Journaling): Acknowledged only after the write is physically flushed to the on-disk Write-Ahead Log (WAL) journal file.
- **ACID Transactions in MongoDB:**
  Multi-document ACID transactions across replica sets and sharded clusters are executed via `ClientSession`. Reads operate under **Snapshot Isolation**.

---

# Section 10: Testing & Quality Engineering Terms

---

### 10.1 Test Slicing (`@WebMvcTest`) vs Full `@SpringBootTest`
- **`@SpringBootTest`:** Starts the entire Spring container: loads JPA, database connection pools, security, Kafka, and background workers. Slower startup (5–15 seconds).
- **`@WebMvcTest(MyController.class)`:** Slices the application context. Loads **ONLY** the web layer (`@Controller`, `MockMvc`, validation, filters). Skips all `@Service` and `@Repository` beans. Fast execution (300ms).

---

### 10.2 ApplicationContext Cache Pollution
- **The Problem:** Spring's TestContext framework maintains an internal **`ContextCache`** that reuses the warm `ApplicationContext` across test classes to keep CI builds fast.
- **The Trap:**
  When a test class uses `@MockBean` on a service that isn't mocked in other tests, or uses `@DirtiesContext`, Spring calculates a different cache key!
  Spring must tear down the old context and boot a brand new Spring context from scratch. If done across 50 test classes, build time explodes from 2 minutes to 40 minutes!
  *Fix:* Define all common `@MockBean`s on a shared `AbstractIntegrationTest` base class.

---

### 10.3 Testcontainers & `@ServiceConnection`
- **Testcontainers:** A Java library that spins up real Docker containers (real PostgreSQL, real Kafka, real Redis) during integration tests.
- **Spring Boot 3.1+ `@ServiceConnection`:**
  Eliminates the legacy boilerplate of writing `@DynamicPropertySource` to map dynamic Docker ports to Spring configuration.
  Simply annotate the container:
  `@Container @ServiceConnection static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");`
  Spring Boot auto-configures HikariCP and JPA connection details directly from the running container!

---

### 10.4 Mock vs Spy (`@MockBean` vs `@SpyBean` Method Evaluation Trap)
- **`@MockBean`:** A dummy placeholder. All methods return default values (`null`, `0`, `false`) unless explicitly stubbed with `when()`.
- **`@SpyBean`:** A wrapper around a **real instance**. Unmocked methods execute the real implementation.
- **The Critical Spy Trap:**
  ```java
  // DANGEROUS: Executes the real method FIRST before Mockito stubs it!
  when(spyEmailService.sendEmail(any())).thenReturn(true);

  // SAFE PRODUCTION SYNTAX: Stubs first without invoking real implementation!
  doReturn(true).when(spyEmailService).sendEmail(any());
  ```

---

## 🧭 Terminology Quick Reference Cheat Sheet

| Domain | Key Term | One-Sentence Summary |
| :--- | :--- | :--- |
| **AOP** | **CGLIB / Byte Buddy** | Dynamic bytecode subclassing generating proxy objects when interfaces are absent. |
| **AOP** | **Self-Invocation** | Calling `this.method()` bypasses proxies and silences annotations like `@Transactional`. |
| **IoC** | **BeanPostProcessor** | Spring extension hook that intercepts beans during creation to generate proxies. |
| **JPA** | **Dirty Checking** | Automatic SQL `UPDATE` generation by comparing managed entities against initial snapshots. |
| **JPA** | **N+1 Problem** | 1 parent query triggering $N$ individual child queries; fixed via `JOIN FETCH`. |
| **JDBC** | **HikariCP Sizing** | Database connections must equal $(\text{Cores} \times 2) + \text{Disks}$ to avoid CPU thrashing. |
| **Crypto** | **AEAD (AES-GCM)** | Authenticated symmetric encryption guaranteeing both confidentiality and tamper detection. |
| **Crypto** | **Nonce Reuse** | Using the same IV twice in AES-GCM destroys authentication keys and reveals plaintext XOR. |
| **Kafka** | **Poison Pill** | Malformed message causing deserialization crashes; quarantined via DLT. |
| **Kafka** | **Transactional Outbox** | Saving entity and event in a single ACID DB transaction, streamed via Debezium CDC. |
| **WebFlux**| **EventLoop** | Single-threaded non-blocking socket multiplexer; must NEVER execute blocking I/O. |
| **Mongo** | **ESR Rule** | Compound index ordering rule: Equality first, Sort second, Range last. |

---
[🏠 Back to Home](README.md) | [🍃 Spring Boot Master Guide](spring_master_guide.md) | [☕ Java Interview Master Guide](java_interview_master_guide.md)
