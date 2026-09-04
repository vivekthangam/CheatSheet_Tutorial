[🏠 Back to Home](README.md)

# 🧪 Spring Boot Testing, Testcontainers & Quality Engineering Master Guide

A production-grade engineering handbook for testing modern Spring Boot microservices using **JUnit 5**, **Mockito**, **MockMvc**, **Testcontainers**, and **WireMock**. Covers sliced testing (`@WebMvcTest`, `@DataJpaTest`), containerized integration testing with Spring Boot 3.1+ `@ServiceConnection`, and test suite performance optimization.

---

## 📑 Table of Contents

### Track 1: Junior & Entry-Level Foundations

- [🌱 1. Real-World Mental Model (Crash Test Dummy & Pyramid)](#1-the-real-world-mental-model-the-car-crash-test-dummy--the-testing-pyramid)
- [🧩 2. The 5 Core Building Blocks of Testing](#2-the-5-core-building-blocks)
- [💻 3. Beginner Code Walkthrough: Unit Test vs WebMvc Sliced Test](#3-beginner-code-walkthrough-unit-test-vs-webmvc-sliced-test)
- [💥 4. What Happens When Things Break? (Top 3 Test Disasters)](#4-what-happens-when-things-break-top-3-test-disasters)
- [⚠️ 5. Top 5 Beginner Mistakes in Production](#5-top-5-beginner-mistakes-in-production)
- [🎯 6. Top 10 Junior Interview Questions (With "ELI5" Answers)](#6-top-10-junior-interview-questions-with-explain-like-im-5-answers)

### Track 2: Advanced Architecture & Quality Engineering

1. [🔪 1. Sliced Testing: @WebMvcTest & MockMvc](#-1-sliced-testing-webmvctest--mockmvc)
2. [💾 2. Persistence Testing: @DataJpaTest & TestEntityManager](#-2-persistence-testing-datajpatest--testentitymanager)
3. [🐳 3. Modern Testcontainers with @ServiceConnection](#-3-modern-testcontainers-with-serviceconnection)
4. [🔌 4. HTTP Mocking for External Microservices: WireMock](#-4-http-mocking-for-external-microservices-wiremock)
5. [⏱️ 5. Testing Asynchronous Systems with Awaitility](#-5-testing-asynchronous-systems-with-awaitility)
6. [🏭 6. Production Scenarios & War Room Incident Forensics](#-6-production-scenarios--war-room-incident-forensics)
7. [⚖️ 7. Spring Testing Master Cheat Sheet](#️-7-spring-testing-master-cheat-sheet)

# TRACK 1: THE JUNIOR & ENTRY-LEVEL FOUNDATIONS (ZERO-TO-HERO)

## 1. The Real-World Mental Model (The Car Crash Test Dummy & The Testing Pyramid)

### Why Do We Test?
Imagine building a new car:
- **Unit Testing (Checking the Spark Plug on a Workbench):** You take 1 spark plug out of the box, connect it to a 9V battery on your workbench, and see if it sparks. It takes 1 millisecond. You don't need a car, an engine, or gasoline.
- **Sliced Testing (Testing the Dashboard Electronics):** You plug the dashboard display into a test battery. You press the speedometer button to make sure the needle moves, without starting the real gas engine.
- **Integration Testing with Testcontainers (The Crash Test Track):** You put the entire assembled car on a real track, put in real gasoline (real Dockerized PostgreSQL database), and test that when you press the brake pedal, the car actually stops!

---

### The Enterprise Testing Pyramid

```
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                              THE ENTERPRISE TEST PYRAMID                               │
│                                                                                        │
│                                  / \                                                   │
│                                 /   \   Full Integration Tests                         │
│                                /     \  @SpringBootTest + Testcontainers               │
│                               /       \ (Slowest, Highest Fidelity)                    │
│                              /─────────\                                               │
│                             /           \  Sliced Context Tests                        │
│                            /             \ @WebMvcTest, @DataJpaTest                   │
│                           /               \ (Fast, Isolated Slice)                     │
│                          /─────────────────\                                           │
│                         /                   \  Pure Unit Tests                         │
│                        /                     \ Plain JUnit 5 + Mockito                 │
│                       /                       \ (Instant, No Spring Context)           │
│                      /─────────────────────────\                                       │
└────────────────────────────────────────────────────────────────────────────────────────┘
```

---

## 2. The 5 Core Building Blocks

| Term | What It Means | Real-World Analogy |
| :--- | :--- | :--- |
| **JUnit 5** | The testing framework executing assertions (`assertEquals`, `@Test`). | The inspector's clipboard checking items off a checklist. |
| **Mockito** | A mocking library that simulates dependencies (`when(...).thenReturn(...)`). | A stunt double or wooden dummy standing in for a real actor. |
| **`MockMvc`** | Simulates HTTP requests and assertions without starting a real HTTP server. | A flight simulator cockpit: feels like flying, but never leaves the room. |
| **Sliced Tests** | Boots only a specific slice of the Spring Context (`@WebMvcTest`, `@DataJpaTest`). | Testing only the car's headlights using a battery, ignoring the engine. |
| **Testcontainers** | Spins up real Docker containers (PostgreSQL, Kafka, Redis) during tests. | Testing your boat in real ocean water rather than in a bathtub. |

---

## 3. Beginner Code Walkthrough: Unit Test vs WebMvc Sliced Test

### Step 1: Pure Unit Test with Mockito (No Spring Context = 10ms execution!)
```java
package com.example.testing.service;

import com.example.testing.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Zero Spring overhead!
class UserServiceUnitTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void shouldReturnUserWhenExists() {
        // Arrange
        User dummy = new User(1L, "Alice");
        when(userRepository.findById(1L)).thenReturn(Optional.of(dummy));

        // Act
        User result = userService.getUserById(1L);

        // Assert
        assertNotNull(result);
        assertEquals("Alice", result.getName());
        verify(userRepository, times(1)).findById(1L);
    }
}
```

### Step 2: Sliced Controller Test with `MockMvc` (`< 1 second` boot time!)
```java
package com.example.testing.controller;

import com.example.testing.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class) // Only loads Web Layer!
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean // Replaces real service with Mockito mock in Spring context
    private UserService userService;

    @Test
    void shouldReturnUserJson() throws Exception {
        when(userService.getUserById(1L)).thenReturn(new User(1L, "Alice"));

        mockMvc.perform(get("/api/users/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Alice"));
    }
}
```

---

## 4. What Happens When Things Break? (Top 3 Test Disasters)

1. **Context Caching Thrashing (`@DirtiesContext` Nightmare):**
   Spring caches `ApplicationContext` across test classes so it boots once. If you add `@DirtiesContext` on every class, Spring destroys and re-creates the entire container for every test class. A 2-minute test suite turns into a **45-minute agonizing crawl**!
2. **The H2 Database Illusion:**
   Using an in-memory H2 database for testing when production runs PostgreSQL. Your tests pass, but your production deployment crashes because H2 does not support Postgres `JSONB`, partial indexes, or Postgres stored procedures! **Fix:** Use **Testcontainers** with real PostgreSQL.
3. **Flaky Tests with `Thread.sleep()`:**
   Waiting for an async event with `Thread.sleep(2000)`. On developer laptops, it takes 500ms (passes). On busy CI/CD servers, it takes 2100ms (fails!). **Fix:** Use `Awaitility.await().atMost(5, SECONDS).until(...)`.

---

## 5. Top 5 Beginner Mistakes in Production

1. **Slapping `@SpringBootTest` on Every Single Test Class:** Boots the entire 500-bean enterprise context, database connections, and security for testing a 3-line utility method. Use pure JUnit 5 + Mockito!
2. **Testing Against H2 Instead of Testcontainers:** Testing against a mock or H2 database hides SQL syntax errors and dialect mismatches until you hit production.
3. **Mocking Everything (Testing Your Mocks, Not Reality):** Mocking the repository, mocking the service, and mocking the database until your test only verifies that Mockito returned what you told Mockito to return!
4. **Using Hardcoded Ports in Integration Tests:** Setting `server.port=8080` in test properties. If another service is running or two tests run in parallel, builds fail with `PortInUseException`. **Fix:** Use `@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)`.
5. **Not Cleaning Up Shared State Between Tests:** Leaving rows inserted into a database or keys in Redis. Test B fails only because Test A ran before it and didn't clean up its state!

---

## 6. Top 10 Junior Interview Questions (With "Explain Like I'm 5" Answers)

### Q1: What is the Test Pyramid and why is it important?
- **ELI5 Answer:** *"Eat lots of vegetables at the bottom (fast unit tests), some bread in the middle (sliced tests), and a tiny piece of candy at the top (slow full-system tests). If you only eat candy, your teeth rot and everything slows down!"*
- **Technical Answer:** *"The Test Pyramid is a testing strategy advocating for a large base of fast, isolated Unit Tests, a moderate layer of Sliced Integration Tests, and a small peak of End-to-End/Full-Context Tests. This minimizes CI/CD build times while maximizing code coverage and defect detection."*

### Q2: What is the difference between `@Mock` and `@MockBean`?
- **ELI5 Answer:** *"`@Mock` is a wooden toy made by Mockito on your desk. `@MockBean` puts that wooden toy inside Spring's official toy box so other workers in Spring can use it."*
- **Technical Answer:** *"`@Mock` is a pure Mockito annotation; it creates a mock instance with zero Spring involvement. `@MockBean` is a Spring Test annotation; it creates a Mockito mock and registers it into the Spring `ApplicationContext`, replacing any existing bean of the same type."*

### Q3: What is Sliced Testing in Spring Boot?
- **ELI5 Answer:** *"Taking only 1 slice of pizza instead of eating the whole giant 20-slice pizza box."*
- **Technical Answer:** *"Sliced testing loads only a subset of Spring's `ApplicationContext` relevant to the layer under test. For example, `@WebMvcTest` configures controllers and Jackson without instantiating database repositories or services, resulting in sub-second test startup."*

### Q4: What is Testcontainers and why is it better than H2?
- **ELI5 Answer:** *"Instead of testing a toy boat in a bathtub with soap bubbles, Testcontainers puts real ocean water and real waves in a tank so you know the boat won't sink in the real sea."*
- **Technical Answer:** *"Testcontainers is a Java library that manages lightweight, disposable Docker containers during integration tests. It allows tests to run against identical production infrastructure (real PostgreSQL, Kafka, Redis) rather than in-memory mocks like H2, catching dialect and driver bugs before production."*

### Q5: How does Spring Context Caching work?
- **ELI5 Answer:** *"Spring builds the Lego castle once at the start of school, and lets all the kids look at it without taking it apart and rebuilding it every hour."*
- **Technical Answer:** *"Spring Test Framework caches the `ApplicationContext` between test classes that share identical context configurations. Subsequent test classes reuse the already-booted context, saving tens of seconds per class."*

### Q6: What does `@DirtiesContext` do and why should it be used sparingly?
- **ELI5 Answer:** *"A kid who knocks down the Lego castle so the teacher has to rebuild it from scratch for the next kid, wasting 10 minutes of recess."*
- **Technical Answer:** *"`@DirtiesContext` indicates that a test modified the Spring context (e.g. altered bean state). Spring marks the context as dirty, closes it, and forces a complete re-instantiation for subsequent tests, destroying context caching benefits and inflating build times."*

### Q7: What is `MockMvc` and what is `TestRestTemplate`?
- **ELI5 Answer:** *"`MockMvc` pretends to send an HTTP letter without putting it in a real mailbox. `TestRestTemplate` actually drops a real letter in a real mailbox over a real network socket."*
- **Technical Answer:** *"`MockMvc` simulates HTTP requests directly against Spring's `DispatcherServlet` in memory without starting an embedded servlet container. `TestRestTemplate` (or `WebTestClient`) makes actual HTTP network socket calls against a running server (`RANDOM_PORT`)."*

### Q8: What is `@DataJpaTest`?
- **ELI5 Answer:** *"A test room with only a filing cabinet and an accountant, but no telephone or customer doors."*
- **Technical Answer:** *"`@DataJpaTest` is a sliced test annotation that configures `@Entity` classes, Spring Data JPA repositories, and an `EntityManager`, while disabling controllers and normal services. Tests run inside transactions that roll back automatically at the end of each test method."*

### Q9: What is Awaitility and why is it used in async testing?
- **ELI5 Answer:** *"A smart stopwatch that checks every 100 milliseconds if the cake is done, instead of blindly sleeping for 10 minutes while the cake burns."*
- **Technical Answer:** *"Awaitility is a DSL for testing asynchronous systems. Instead of non-deterministic `Thread.sleep()` calls that cause test flakiness or inflate test suite duration, Awaitility polls a condition until it becomes true or times out."*

### Q10: What is WireMock?
- **ELI5 Answer:** *"A pretend bank teller behind a fake counter who gives you the exact fake receipts you asked for so you can practice your speech."*
- **Technical Answer:** *"WireMock is an HTTP mock server that stubs external third-party HTTP APIs. It simulates HTTP responses, latency, 500 errors, and timeouts, allowing your microservice to test network edge cases deterministically without calling live third-party services."*

---

# TRACK 2: ADVANCED ARCHITECTURE & QUALITY ENGINEERING

## 🔪 1. Sliced Testing: @WebMvcTest & MockMvc

`@WebMvcTest` starts only the web layer (Controllers, Jackson, Security, Filters) while mocking out all database and business services. Tests boot in $< 1$ second.

### Maven Dependencies (`pom.xml`)
```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    <!-- Testcontainers -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-testcontainers</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>postgresql</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### MockMvc Controller Slice Test
```java
package com.example.testing.controller;

import com.example.order.controller.OrderController;
import com.example.order.dto.OrderDto;
import com.example.order.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @Test
    void shouldReturnOrderWhenExists() throws Exception {
        // Given
        String orderId = "ORD-999";
        given(orderService.getOrder(orderId))
            .willReturn(new OrderDto(orderId, "COMPLETED", 199.99));

        // When & Then
        mockMvc.perform(get("/api/v1/orders/{orderId}", orderId)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.orderId").value("ORD-999"))
            .andExpect(jsonPath("$.status").value("COMPLETED"))
            .andExpect(jsonPath("$.totalAmount").value(199.99));
    }
}
```

---

## 💾 2. Persistence Testing: @DataJpaTest & TestEntityManager

`@DataJpaTest` configures an embedded database or container, scans for `@Entity` classes, and enables Spring Data repositories with rollback after each test.

```java
package com.example.testing.repository;

import com.example.order.entity.Customer;
import com.example.order.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CustomerRepositoryTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void shouldFindCustomerByEmail() {
        // Given
        Customer customer = new Customer("alice@example.com", "Alice Smith");
        entityManager.persistAndFlush(customer);

        // When
        Optional<Customer> found = customerRepository.findByEmail("alice@example.com");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Alice Smith");
    }
}
```

---

## 🐳 3. Modern Testcontainers with @ServiceConnection

> [!IMPORTANT]
> **Spring Boot 3.1+ Feature:**
> You no longer need manual `@DynamicPropertySource` to wire database host, port, and password!
> Using `@ServiceConnection`, Spring Boot automatically detects the container and injects datasource connection details.

```java
package com.example.testing.integration;

import com.example.order.entity.Product;
import com.example.order.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class ProductIntegrationTest {

    // Automatically spins up real PostgreSQL in Docker and binds JDBC connection
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private ProductRepository productRepository;

    @Test
    void shouldPersistAndRetrieveProductInRealPostgres() {
        Product product = new Product("PROD-01", "Mechanical Keyboard", 129.99);
        productRepository.save(product);

        Product retrieved = productRepository.findBySku("PROD-01").orElseThrow();
        assertThat(retrieved.getPrice()).isEqualTo(129.99);
    }
}
```

---

## 🔌 4. HTTP Mocking for External Microservices: WireMock

Mock external partner REST APIs without hitting the real internet or spinning up external servers.

```java
package com.example.testing.wiremock;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@WireMockTest(httpPort = 8089)
class PaymentGatewayClientTest {

    @Autowired
    private PaymentClient paymentClient;

    @Test
    void shouldHandleSuccessfulPaymentFromRemoteGateway() {
        // Stub external endpoint
        stubFor(post(urlEqualTo("/v1/charges"))
            .withHeader("Content-Type", equalTo("application/json"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"chargeId\":\"CH_123\",\"status\":\"SUCCESS\"}")));

        // Execute Client
        PaymentResponse response = paymentClient.charge(100.0);

        // Verify
        assertThat(response.getStatus()).isEqualTo("SUCCESS");
        verify(postRequestedFor(urlEqualTo("/v1/charges")));
    }
}
```

---

## ⏱️ 5. Testing Asynchronous Systems with Awaitility

Avoid `Thread.sleep(5000)` in async tests! `Awaitility` polls condition predicates cleanly with configurable timeouts.

```java
package com.example.testing.async;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;

import static org.awaitility.Awaitility.await;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class AsyncOrderNotificationTest {

    @Autowired
    private OrderEventPublisher publisher;

    @Autowired
    private AuditRepository auditRepository;

    @Test
    void shouldProcessAsyncOrderEventWithinThreeSeconds() {
        publisher.publish("ORD-777");

        // Wait until async consumer writes audit record to DB
        await()
            .atMost(Duration.ofSeconds(3))
            .pollInterval(Duration.ofMillis(100))
            .untilAsserted(() -> {
                assertThat(auditRepository.findByOrderId("ORD-777")).isPresent();
            });
    }
}
```

---

## 🏭 6. Production Scenarios & War Room Incident Forensics

### Scenario 1: Test Suite Slowdown via `@DirtiesContext` Abuse
- **Symptom:** CI build time grew from 3 minutes to 45 minutes as the test suite expanded.
- **Root Cause:** Developers placed `@DirtiesContext` on every test class. Spring normally caches the `ApplicationContext` across test classes. `@DirtiesContext` forces Spring to destroy and recreate all beans, connection pools, and caches on every single test!
- **The Fix:** Remove `@DirtiesContext`. Clean up state using `@Transactional` rollback or explicit repository truncation in `@AfterEach`.

### Scenario 2: Embedded H2 Divergence from Production Database
- **Symptom:** All tests pass in CI using in-memory H2, but production fails with SQL syntax errors on PostgreSQL-specific JSONB queries and Window Functions.
- **The Fix:** Eliminate H2. Use **Testcontainers PostgreSQL** with `@ServiceConnection` so tests run against the exact same engine and version as production.

---

## ⚖️ 7. Spring Testing Master Cheat Sheet

| Annotation / Utility | Purpose / Behavior |
| :--- | :--- |
| **`@SpringBootTest`** | Boots complete ApplicationContext for integration tests |
| **`@WebMvcTest`** | Slices web layer only (mocks services with `@MockBean`) |
| **`@DataJpaTest`** | Slices JPA repositories with automatic transaction rollback |
| **`@ServiceConnection`**| Auto-injects Testcontainers JDBC/Kafka/Redis connection properties |
| **`@MockBean`** | Replaces a bean in ApplicationContext with a Mockito mock |
| **`@SpyBean`** | Wraps real Spring bean with a Mockito spy |
| **`MockMvc`** | Simulates HTTP calls without binding to a network port |
| **`WireMock`** | Mocks external downstream third-party REST services |

---
[🏠 Back to Home](README.md)
