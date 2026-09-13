[🏠 Back to Home](README.md) | [🍃 Spring Boot Master Guide](spring_master_guide.md) | [🏛️ Spring Data JPA Guide](spring_data_jpa.md) | [🛡️ Spring Security Guide](spring_security.md)

# 🧪 Spring Boot Testing, Testcontainers & Quality Engineering Master Guide

A production-grade engineering handbook for testing modern Spring Boot microservices using **JUnit 5**, **Mockito**, **MockMvc**, **Testcontainers**, and **WireMock**. Covers sliced testing (`@WebMvcTest`, `@DataJpaTest`), containerized integration testing with Spring Boot 3.1+ `@ServiceConnection`, and test suite performance optimization.

---

## 📑 Table of Contents

1. [🧠 Zero-to-Hero Mental Model: The Car Crash Test Dummy & The Testing Pyramid](#-the-car-crash-test-dummy--the-testing-pyramid)
2. [🛠️ Prerequisites & Foundational Knowledge](#️-prerequisites--foundational-knowledge)
3. [📦 Track 1: The Junior & Entry-Level Foundations](#track-1-the-junior--entry-level-foundations-zero-to-hero)
4. [🚀 Track 2: Master Spring Boot Testing Feature Catalog](#track-2-master-spring-boot-testing-feature-catalog)
5. [🏗️ Track 3: Framework Internals & Context Caching Engine](#track-3-framework-internals--context-caching-engine)
6. [⚙️ Track 4: Production Engineering & Suite Optimization](#track-4-production-engineering--suite-optimization)
7. [🚨 Track 5: War Room Post-Mortems & Root Cause Analysis (RCAs)](#track-5-war-room-post-mortems--root-cause-analysis-rcas)
8. [🎓 Track 6: Crack-The-Interview Question Bank (Senior & Staff+ Level)](#track-6-crack-the-interview-question-bank-senior--staff-level)
9. [⚖️ Spring Testing Master Cheat Sheet](#️-spring-testing-master-cheat-sheet)

---

## 🛠️ Prerequisites & Foundational Knowledge

Before engineering test suites in Spring Boot 3, developers must master context caching dynamics and containerized isolation mechanics:

### 1. The Enterprise Testing Pyramid
- **Unit Tests (70%)**: Pure Java, zero Spring context, runs in $<1\text{ms}$ with JUnit 5 and Mockito. Validates isolated class logic.
- **Sliced Context Tests (20%)**: Boots a targeted subset of beans (e.g. `@WebMvcTest` boots controllers and security; `@DataJpaTest` boots Hibernate and datasource). Runs in $<500\text{ms}$.
- **Full Integration Tests (10%)**: Boots the complete `@SpringBootTest` with real Dockerized dependencies (PostgreSQL, Kafka, Redis) via Testcontainers. Slowest ($3\text{s} - 10\text{s}$), but provides 100% production fidelity.

### 2. Spring Test Context Caching Dynamics
- **`DefaultTestContextBootstrapper`**: Spring caches the initialized `ApplicationContext` in memory across test classes. If 50 integration test classes share the identical context configuration, the container boots **only once**, saving tens of minutes of CI execution time.
- **The Context Invalidation Danger**: Adding `@DirtiesContext` or using different `@MockBean` configurations forces Spring to destroy and recreate the entire context, degrading CI pipeline speeds.

### 3. Testcontainers & The Ryuk Cleanup Daemon
- **Real Production Databases**: Replaces flawed in-memory H2 databases with production-grade Dockerized PostgreSQL/MySQL instances.
- **Ryuk Container**: Testcontainers starts a tiny sidecar container (`testcontainers/ryuk`) that connects to `/var/run/docker.sock`. Even if the JVM test process crashes or is killed by an OS signal, Ryuk terminates and removes all spun-up test containers and networks, preventing orphan resource leaks.

---

# TRACK 1: THE JUNIOR & ENTRY-LEVEL FOUNDATIONS (ZERO-TO-HERO)

## 1. The Real-World Mental Model (The Car Crash Test Dummy)

- **Unit Testing (Testing the Spark Plug on a Workbench):** You take 1 spark plug out of the box, connect it to a 9V battery on your desk, and see if it sparks. It takes 1 millisecond. You don't need a car, an engine, or gasoline.
- **Sliced Testing (Testing the Dashboard Electronics):** You connect the car dashboard to a test battery. You press the speedometer button to verify the needle moves, without starting the real gas engine.
- **Integration Testing with Testcontainers (The Crash Test Track):** You put the entire assembled car on a real road track, fill the tank with real gasoline (real Dockerized PostgreSQL database), and verify that pressing the brake pedal actually brings the car to a safe stop.

| Tier | Test Layer | Framework Tooling & Annotations | Execution Latency | Context Footprint | Production Fidelity | Primary Focus & Verification Scope |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Tier 3 (Top 10%)** | **Full Integration Tests** | `@SpringBootTest`, Testcontainers, `@ServiceConnection` | Slow ($3\text{s} - 10\text{s}$) | Complete `ApplicationContext` + Docker containers (PostgreSQL, Kafka, Redis) | **Highest (100%)** | End-to-end user transactions, cross-service workflows, real database schema constraints, wire-level protocol serialization. |
| **Tier 2 (Mid 20%)** | **Sliced Context Tests** | `@WebMvcTest`, `@DataJpaTest`, `@JsonTest`, MockMvc | Fast ($200\text{ms} - 800\text{ms}$) | Targeted slice only (e.g., Controllers + Security filters, or JPA Repositories + EntityManager) | **Medium (High for targeted slice)** | HTTP request routing, input validation, serialization/deserialization, query generation, SQL dialect execution. |
| **Tier 1 (Base 70%)** | **Pure Unit Tests** | Plain JUnit 5 (`@ExtendWith(MockitoExtension.class)`), AssertJ | Instant ($<1\text{ms} - 10\text{ms}$) | Zero Spring context; pure JVM heap allocation | **Isolated** | Business domain algorithms, math calculation, branching logic, boundary conditions, edge cases without I/O overhead. |

> [!NOTE]
> **Execution Flow Pipeline:**
> `Pure Unit Tests (Plain JUnit 5 / Mockito)` ──► `Sliced Context Tests (@WebMvcTest / @DataJpaTest)` ──► `Full Integration Tests (@SpringBootTest + Testcontainers @ServiceConnection)`

---

## 2. The 5 Core Building Blocks

| Term | What It Means | Real-World Analogy |
| :--- | :--- | :--- |
| **`@SpringBootTest`** | Boots the entire Spring application context for full end-to-end integration tests. | Starting the complete engine and driving the car out of the factory. |
| **`@WebMvcTest`** | Boots only the web layer (Controllers, Security, Filters, Jackson) without loading databases. | Testing the car's steering wheel and radio without assembling the engine. |
| **`@DataJpaTest`** | Boots only the persistence layer (Repositories, Entities, DataSource) and rolls back transactions after each test. | Testing the fuel injection system on an isolated test bench. |
| **`@ServiceConnection`** | Spring Boot 3.1+ feature that automatically configures datasources from Testcontainers without boilerplate properties. | Plugging a universal power adapter into a wall socket. |
| **`WireMock`** | Mocks external HTTP microservices by running a local HTTP server that returns canned JSON responses. | A recorded automated telephone message answering external phone calls. |

---

## 3. Beginner Code Walkthrough: Pure Unit Test vs Sliced WebMvc Test

### 3.1 Fast Pure Unit Test (JUnit 5 + Mockito)
```java
package com.example.testing.unit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class) // Zero Spring context! Runs in 2ms
class OrderServiceUnitTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    void shouldCalculateTotalWithDiscount() {
        when(orderRepository.findById(101L))
            .thenReturn(Optional.of(new Order(101L, 100.0, "STANDARD")));

        double total = orderService.calculateFinalPrice(101L, 0.10);

        assertThat(total).isEqualTo(90.0);
    }
}
```

---

## 4. Top 10 Junior Interview Questions

### Q1: What is the difference between `@Mock` and `@MockBean`?
- **ELI5 Answer:** *"`@Mock` is a fake cardboard cutout you hold in your hands. `@MockBean` places that cutout inside the school play (Spring Context) so all the other actors think it's real."*
- **Technical Answer:** *"`@Mock` is a pure Mockito annotation creating a standalone mock object for unit tests. `@MockBean` is a Spring Boot annotation that registers the mock as a bean inside the Spring `ApplicationContext`, replacing any existing bean of that type."*

### Q2: Why is testing against an in-memory H2 database considered an anti-pattern today?
- **ELI5 Answer:** *"Practicing landing a plane on a video game simulator, then flying a real Boeing 747 in a thunderstorm."*
- **Technical Answer:** *"H2 does not implement database-specific SQL features (e.g. PostgreSQL JSONB, sequence optimizers, locking semantics, window functions). Tests pass on H2 in CI, but fail catastrophically when deployed to real PostgreSQL in production. Use **Testcontainers** instead."*

### Q3: What does `@ServiceConnection` do in Spring Boot 3.1+?
- **ELI5 Answer:** *"Automatically plugging in the battery cables without having to write down the voltage numbers on three different papers."*
- **Technical Answer:** *"`@ServiceConnection` eliminates verbose `@DynamicPropertySource` methods by automatically extracting the container's dynamic JDBC URL, username, and password and injecting them into Spring Boot's autoconfigured `DataSourceProperties`."*

### Q4: Why does `@DataJpaTest` roll back transactions automatically?
- **ELI5 Answer:** *"Drawing on a magical dry-erase whiteboard that wipes clean as soon as you finish each math problem."*
- **Technical Answer:** *"By default, `@DataJpaTest` annotates every test method with `@Transactional`. When the test completes, Spring rolls back the transaction, keeping the database clean for the next test method."*

### Q5: What is the purpose of `Awaitility` in testing?
- **ELI5 Answer:** *"A patient friend who peeks at the oven every half second until the cake is baked, instead of staring at a timer for 10 minutes."*
- **Technical Answer:** *"`Awaitility` provides non-blocking polling assertions for asynchronous systems (e.g. Kafka event consumers). It polls periodically until a condition matches or a timeout elapses, avoiding brittle `Thread.sleep()` calls."*

### Q6: What does `@AutoConfigureMockMvc` do?
- **ELI5 Answer:** *"Setting up a fake post office counter so you can send fake HTTP letters directly to the mailroom without starting a delivery truck."*
- **Technical Answer:** *"It configures and injects a `MockMvc` instance into `@SpringBootTest`, allowing you to execute HTTP requests through Spring MVC's full filter chain and `DispatcherServlet` without starting an actual embedded Tomcat HTTP server."*

### Q7: What is the difference between `verify()` and `when()` in Mockito?
- **ELI5 Answer:** *"`when` tells the actor what line to say during the play; `verify` asks the director after the play if the actor actually said the line."*
- **Technical Answer:** *"`when().thenReturn()` stubs behavior beforehand. `verify()` checks afterward whether a specific method was invoked, with what arguments, and how many times."*

### Q8: What does `@DirtiesContext` do and why should you avoid it?
- **ELI5 Answer:** *"Burning down the entire school building and rebuilding it from scratch between every single class."*
- **Technical Answer:** *"It marks the Spring `ApplicationContext` as modified, forcing the test runner to close and re-create the context for the next test. Overusing it turns a 30-second test suite into a 15-minute pipeline."*

### Q9: How do you mock external HTTP REST APIs in Spring tests?
- **ELI5 Answer:** *"Hiring an actor to answer the phone and read a script whenever your program dials an external phone number."*
- **Technical Answer:** *"Using **WireMock**. It starts a lightweight HTTP server on a local port that intercepts outbound HTTP client requests and returns canned JSON payloads with specific status codes."*

### Q10: What is the difference between `ArgumentCaptor` and argument matchers (`any()`)?
- **ELI5 Answer:** *"`any()` says 'I accept any birthday gift.' `ArgumentCaptor` unboxes the gift to inspect and verify exactly what was inside."*
- **Technical Answer:** *"`any()` simply matches a parameter during stubbing/verification. `ArgumentCaptor` captures the actual argument passed into the mock method, allowing deep programmatic assertions on complex nested object fields."*

---

# TRACK 2: MASTER SPRING BOOT TESTING FEATURE CATALOG

## Master Testing Strategy Decision Matrix

| Strategy | Spring Context Booted? | Database Used | Speed (per test) | Best Used For | Anti-Pattern For |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **JUnit 5 + Mockito** | **No** (Zero overhead) | None (Mocked) | $<5\text{ms}$ | Complex business logic, calculation engines | Testing database queries or HTTP endpoints |
| **`@WebMvcTest`** | Sliced (Web layer only)| None (Mocked service) | $200\text{ms}$ | HTTP controllers, validation, security, JSON mapping | Testing database transaction rollback |
| **`@DataJpaTest`** | Sliced (JPA layer only)| Testcontainers PostgreSQL | $500\text{ms}$ | Custom repository queries, JPA mapping, DB constraints| Testing REST controller serialization |
| **`@SpringBootTest`** | **Full Context** | Testcontainers All | $2\text{s} - 5\text{s}$ | End-to-end integration flows, Kafka event pipelines | Unit testing simple helper methods |
| **WireMock** | Optional | None (HTTP stub) | $<10\text{ms}$ | Third-party payment gateways, external partner APIs | Mocking internal database calls |
| **Awaitility** | Depends on test | Real/Container | Variable | Event-driven eventually consistent Kafka consumers | Synchronous REST endpoints |
| **EmbeddedKafka** | Context-bound | In-Memory Broker | $1\text{s}$ | Fast local queue routing tests | Realistic multi-partition rebalance tests |
| **Contract (Pact)** | Sliced/Unit | None | $<50\text{ms}$ | Consumer-driven API contract validation across teams | Heavy performance load testing |
| **`@Sql` Cleaners** | Integrated | Testcontainers DB | $20\text{ms}$ | Deterministic dataset seeding before complex tests | Unit tests with zero database requirements |
| **Parallel Execution**| Engine-level | Isolated containers | $4\times$ Speedup | Mass CI test suites with $>500$ test classes | Shared mutable static singleton state |

---

## 2.1 Modern Testcontainers Integration with `@ServiceConnection`

1. **Architectural Overview & Purpose**:
   - In Spring Boot 3.1+, `@ServiceConnection` completely eliminates verbose `@DynamicPropertySource` methods (`registry.add("spring.datasource.url", ...)`).
   - Automatically inspects the container image (e.g. `postgres:16-alpine`), extracts JDBC URL, credentials, and driver class, and injects them directly into the Spring environment.

2. **Production Blueprint**:
   ```java
   @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
   @Testcontainers
   class OrderIntegrationTest {

       @Container
       @ServiceConnection
       static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

       @Autowired
       private OrderRepository orderRepository;

       @Test
       void shouldPersistAndRetrieveOrder() {
           Order saved = orderRepository.save(new Order(null, "ORD-999", 199.99));
           assertThat(saved.getId()).isNotNull();

           Order found = orderRepository.findById(saved.getId()).orElseThrow();
           assertThat(found.getOrderNumber()).isEqualTo("ORD-999");
       }
   }
   ```

---

## 2.2 Controller Sliced Testing with `@WebMvcTest` & `MockMvc`

1. **Architectural Overview & Purpose**:
   - Boots only the web layer (`DispatcherServlet`, `@Controller`, `@ControllerAdvice`, `SecurityFilterChain`, Jackson converters) without loading database connections or service implementations:
   ```java
   @WebMvcTest(OrderController.class)
   class OrderControllerTest {

       @Autowired
       private MockMvc mockMvc;

       @MockBean
       private OrderService orderService;

       @Test
       @WithMockUser(username = "admin", roles = {"ADMIN"})
       void shouldReturnOrderJson() throws Exception {
           when(orderService.getOrder("ORD-101"))
               .thenReturn(new OrderDto("ORD-101", 99.50));

           mockMvc.perform(get("/api/v1/orders/ORD-101").accept(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(content().contentType(MediaType.APPLICATION_JSON))
               .andExpect(jsonPath("$.orderNumber").value("ORD-101"))
               .andExpect(jsonPath("$.price").value(99.50));
       }
   }
   ```

---

## 2.3 Persistence Sliced Testing with `@DataJpaTest`

1. **Architectural Overview & Purpose**:
   - Boots only `@Entity` classes and Spring Data JPA repositories.
   - Automatically wraps each `@Test` method in an atomic database transaction that is **automatically rolled back** upon completion, ensuring zero test data pollution.
   ```java
   @DataJpaTest
   @AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // Uses real Testcontainers DB
   @Testcontainers
   class CustomerRepositoryTest {

       @Container
       @ServiceConnection
       static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

       @Autowired
       private CustomerRepository customerRepository;

       @Test
       void shouldFindActiveCustomers() {
           customerRepository.save(new Customer("alice@corp.com", true));
           List<Customer> active = customerRepository.findAllByActiveTrue();
           assertThat(active).hasSize(1);
       }
   }
   ```

---

## 2.4 External HTTP Mocking with WireMock

1. **Architectural Overview & Purpose**:
   - Tests HTTP client integrations (`RestClient`, `WebClient`) against a local mock HTTP server that simulates real network responses, timeouts, and HTTP 5xx errors:
   ```java
   @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
   @WireMockTest(httpPort = 8089)
   class PaymentGatewayClientTest {

       @Autowired
       private PaymentClient paymentClient;

       @Test
       void shouldHandleSuccessfulPayment() {
           stubFor(post(urlEqualTo("/v1/charges"))
               .withHeader("Authorization", equalTo("Bearer token-secret"))
               .willReturn(aResponse()
                   .withStatus(200)
                   .withHeader("Content-Type", "application/json")
                   .withBody("""
                       { "chargeId": "ch_99", "status": "succeeded" }
                       """)));

           PaymentResult res = paymentClient.charge(50.0);
           assertThat(res.isSuccess()).isTrue();
       }
   }
   ```

---

## 2.5 Asynchronous Event Pipeline Testing with Awaitility

1. **Architectural Overview & Purpose**:
   - In distributed event-driven systems (Kafka/RabbitMQ), assertions fail if executed immediately because message consumption is asynchronous.
   - **Awaitility** polls the condition periodically until it evaluates to true or times out:
   ```java
   @Test
   void shouldProcessKafkaEventEventually() {
       kafkaProducer.send("orders", new OrderEvent("ORD-500"));

       await()
           .atMost(Duration.ofSeconds(5))
           .pollInterval(Duration.ofMillis(100))
           .untilAsserted(() -> {
               Optional<Order> order = orderRepository.findByOrderNumber("ORD-500");
               assertThat(order).isPresent();
               assertThat(order.get().getStatus()).isEqualTo("PROCESSED");
           });
   }
   ```

---

## 2.6 Consumer-Driven Contract Testing (Pact / Spring Cloud Contract)

1. **Architectural Overview**:
   - Validates that API provider responses match consumer expectations without running expensive full-system end-to-end environments.
   - Prevents breaking API changes across independent microservice deployment pipelines.

---

## 2.7 Sliced Security Testing with Custom Security Context Factories

1. **Architectural Overview**:
   - Custom annotations like `@WithMockCustomUser` inject realistic user identities, tenant IDs, and permissions directly into `SecurityContextHolder`:
   ```java
   @Retention(RetentionPolicy.RUNTIME)
   @WithSecurityContext(factory = WithMockCustomUserSecurityContextFactory.class)
   public @interface WithMockCustomUser {
       String username() default "alice";
       String tenantId() default "CORP-ACME";
   }
   ```

---

## 2.8 Kafka Integration Testing: EmbeddedKafka vs Testcontainers

1. **Architectural Comparison**:
   - `@EmbeddedKafka`: Lightweight in-JVM broker; fast startup ($<1\text{s}$), but shares JVM heap and lacks exact production broker features.
   - **Testcontainers Kafka / Confluent**: Real Dockerized Kafka broker with ZooKeeper/KRaft; tests multi-partition rebalance storms and real network latency.

---

## 2.9 Test Data Management with `@Sql` Scripts

1. **Deterministic Dataset Seeding**:
   ```java
   @Test
   @Sql(scripts = "/test-data/seed-high-value-customers.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
   @Sql(scripts = "/test-data/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
   void shouldGenerateTierReport() {
       Report report = reportService.generateReport();
       assertThat(report.totalRevenue()).isGreaterThan(100000.0);
   }
   ```

---

## 2.10 Parallel Test Execution & CI Suite Acceleration

1. **JUnit 5 Parallel Execution Configuration (`junit-platform.properties`)**:
   ```properties
   junit.jupiter.execution.parallel.enabled = true
   junit.jupiter.execution.parallel.mode.default = concurrent
   junit.jupiter.execution.parallel.mode.classes.default = concurrent
   junit.jupiter.execution.parallel.config.strategy = dynamic
   junit.jupiter.execution.parallel.config.dynamic.factor = 1.5
   ```
   - Reduces multi-thousand test suite run times from 25 minutes to 4 minutes by utilizing all CPU cores concurrently.

---

# TRACK 3: FRAMEWORK INTERNALS & CONTEXT CACHING ENGINE

## 3.1 Spring TestContext Framework Lifecycle

| Lifecycle Phase | Engine Component | Operational Mechanics & Responsibilities | Cache State & Thread Safety | Latency Impact |
| :--- | :--- | :--- | :--- | :--- |
| **1. Class Loading** | `TestContextManager` | Instantiated per test class. Coordinates test execution listeners, prepares test instance, and registers test context lifecycle callbacks. | Thread-safe test runner coordinator | Negligible ($<1\text{ms}$) |
| **2. Key Generation** | `MergedContextConfiguration` | Synthesizes test class annotations, `@ContextConfiguration`, `@ActiveProfiles`, property overrides, and bean overriding configurations into an immutable cache key. | Computes deterministic hash code across profiles, initializers, and locations | Microseconds ($<0.5\text{ms}$) |
| **3. Cache Lookup** | `ContextCache` (`LruCache`) | Checks the static concurrent in-memory `ContextCache` map using the `MergedContextConfiguration` key. | Concurrent read lock on internal LRU cache map (default max 32 contexts) | Instant ($<0.1\text{ms}$) |
| **4a. Cache HIT** | `ApplicationContext` Reuse | If an active `ApplicationContext` matching the key already exists, it is retrieved and reused directly for the test suite without re-initialization. | Reuses existing JVM singleton beans and connection pools across test classes | **Zero boot cost** ($<2\text{ms}$) |
| **4b. Cache MISS** | Cold Boot & Registration | Cold boots a brand new `ApplicationContext`, executes all `BeanFactoryPostProcessor` and `BeanPostProcessor` beans, and registers the context in `ContextCache`. | Synchronized write lock into static `ContextCache` map | **High boot penalty** ($2\text{s} - 8\text{s}$) |
| **5. Test Execution** | `TestExecutionListener` Chain | Triggers `beforeTestMethod`, injects autowired test fields, executes `@Test` method, and executes `afterTestMethod`. | Invokes rollback on `@Transactional` test methods | Test logic bound |
| **6. Context Eviction** | Invalidation Handler | If `@DirtiesContext` is annotated or an unrecoverable failure occurs, evicts the context from `ContextCache` and shuts down bean destruction callbacks. | Context destroyed; JVM garbage collector reclaims memory | High subsequent cost (forces cold reboot on next class) |

> [!IMPORTANT]
> **TestContext Execution Pipeline:**
> `Test Class Loaded` ──► `TestContextManager` ──► `MergedContextConfiguration Key Computed` ──► `ContextCache Lookup` ──► `[Cache HIT: Reuse Active Context | Cache MISS: Bootstrap New Context & Store]` ──► `TestExecutionListeners (Before -> Run -> After)`

---

# TRACK 4: PRODUCTION ENGINEERING & SUITE OPTIMIZATION

## 4.1 Reusing Testcontainers Across the Entire Suite (Singleton Pattern)

Instead of spinning up a new PostgreSQL container for every test class, declare a singleton base class:

```java
public abstract class AbstractIntegrationTest {

    static final PostgreSQLContainer<?> POSTGRES;

    static {
        POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withReuse(true); // Enables reuse across local runs
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }
}
```

---

# TRACK 5: WAR ROOM POST-MORTEMS & ROOT CAUSE ANALYSIS (RCAs)

## Incident 1: 45-Minute CI Pipeline Outage via `@MockBean` Sprawl

- **Severity:** P1 Developer Productivity Outage (CI builds timed out)
- **Mean Time to Recovery (MTTR):** 2 hours
- **Symptoms:** Adding 10 new integration tests caused overall CI build duration to explode from 4 minutes to 45 minutes.
- **Root Cause:** Developers used `@MockBean` with different combinations of mocked services in each of the 10 test classes. Because `@MockBean` alters the bean definition graph, Spring considered each class to have a unique `MergedContextConfiguration`, booting 10 completely separate `ApplicationContext` instances from scratch.
- **The Permanent Fix:**
  1. Consolidated mocks into a single shared test configuration.
  2. Inherited test classes from `AbstractIntegrationTest`, reducing context boots back to 1.

---

# TRACK 6: CRACK-THE-INTERVIEW QUESTION BANK (SENIOR & STAFF+ LEVEL)

### 1. How does `@MockBean` impact Spring Test context caching?
Each time `@MockBean` is introduced, it alters the bean definitions of the target context. The `MergedContextConfiguration` uses bean definition overrides as part of its cache key. If Test Class A mocks Service 1 and Test Class B mocks Service 2, Spring cannot reuse the context, forcing a cold boot for each class.

### 2. What is the difference between `@SpringBootTest` and `@WebMvcTest`?
`@SpringBootTest` loads the complete application context including all repositories, services, security filters, and datasources, making it suitable for end-to-end integration tests. `@WebMvcTest` loads only the web layer (controllers, controller advice, custom filters), mocking the service and database layers to provide fast, isolated controller testing.

---

## ⚖️ Spring Testing Master Cheat Sheet

| Testing Need | Production Annotation / Tool |
| :--- | :--- |
| **Pure Unit Test** | `@ExtendWith(MockitoExtension.class)` |
| **Web Sliced Test** | `@WebMvcTest(MyController.class)` |
| **JPA Sliced Test** | `@DataJpaTest` + `@AutoConfigureTestDatabase(replace = NONE)` |
| **Real Database** | Testcontainers `@ServiceConnection` |
| **External REST Mock**| WireMock `@WireMockTest` |
| **Async Event Test** | `await().atMost(5, SECONDS).untilAsserted(...)` |
| **Security User** | `@WithMockUser(username = "admin", roles = {"ADMIN"})` |

---
[🏠 Back to Home](README.md) | [🍃 Spring Boot Master Guide](spring_master_guide.md) | [🏛️ Spring Data JPA Guide](spring_data_jpa.md)
