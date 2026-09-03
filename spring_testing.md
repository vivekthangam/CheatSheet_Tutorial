[🏠 Back to Home](README.md)

# 🧪 Spring Boot Testing, Testcontainers & Quality Engineering Master Guide

A production-grade engineering handbook for testing modern Spring Boot microservices using **JUnit 5**, **Mockito**, **MockMvc**, **Testcontainers**, and **WireMock**. Covers sliced testing (`@WebMvcTest`, `@DataJpaTest`), containerized integration testing with Spring Boot 3.1+ `@ServiceConnection`, and test suite performance optimization.

---

## 📑 Table of Contents
1. [🧠 Zero-to-Hero Mental Model: The Spring Testing Pyramid](#-zero-to-hero-mental-model-the-spring-testing-pyramid)
2. [🔪 1. Sliced Testing: @WebMvcTest & MockMvc](#-1-sliced-testing-webmvctest--mockmvc)
3. [💾 2. Persistence Testing: @DataJpaTest & TestEntityManager](#-2-persistence-testing-datajpatest--testentitymanager)
4. [🐳 3. Modern Testcontainers with @ServiceConnection](#-3-modern-testcontainers-with-serviceconnection)
5. [🔌 4. HTTP Mocking for External Microservices: WireMock](#-4-http-mocking-for-external-microservices-wiremock)
6. [⏱️ 5. Testing Asynchronous Systems with Awaitility](#-5-testing-asynchronous-systems-with-awaitility)
7. [🏭 6. Production Scenarios & War Room Incident Forensics](#-6-production-scenarios--war-room-incident-forensics)
8. [⚖️ 7. Spring Testing Master Cheat Sheet](#️-7-spring-testing-master-cheat-sheet)

---

## 🧠 Zero-to-Hero Mental Model: The Spring Testing Pyramid

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
