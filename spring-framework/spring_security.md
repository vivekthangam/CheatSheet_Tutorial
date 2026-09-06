[🏠 Back to Home](README.md) | [🍃 Spring Boot Master Guide](spring_master_guide.md) | [🏛️ Spring Data JPA Guide](spring_data_jpa.md) | [🔐 Cryptography Guide](java_spring_cryptography_master_guide.md)

# 🛡️ Spring Security 6 & OAuth2 / JWT Enterprise Master Guide

A production-grade engineering handbook for securing modern Spring Boot microservices, APIs, and cloud-native applications using **Spring Security 6.x**, **Spring Boot 3.x**, **OAuth 2.0**, and **Stateless JWTs**. Covers `SecurityFilterChain` architecture, custom authentication filters, RBAC vs ABAC, method security, CORS/CSRF configurations, and zero-trust authentication.

---

## 📑 Table of Contents

1. [🧠 Zero-to-Hero Mental Model: The Airport Security & VIP Boarding Gate](#-the-airport-security--vip-boarding-gate)
2. [🛠️ Prerequisites & Foundational Knowledge](#️-prerequisites--foundational-knowledge)
3. [📦 Track 1: The Junior & Entry-Level Foundations](#track-1-the-junior--entry-level-foundations-zero-to-hero)
4. [🚀 Track 2: Master Spring Security 6 Feature Catalog](#track-2-master-spring-security-6-feature-catalog)
5. [🏗️ Track 3: Framework Internals & Under-the-Hood Architecture](#track-3-framework-internals--under-the-hood-architecture)
6. [⚙️ Track 4: Production Engineering, Key Rotation & Zero-Trust Hardening](#track-4-production-engineering-key-rotation--zero-trust-hardening)
7. [🚨 Track 5: War Room Post-Mortems & Root Cause Analysis (RCAs)](#track-5-war-room-post-mortems--root-cause-analysis-rcas)
8. [🎓 Track 6: Crack-The-Interview Question Bank (Senior & Staff+ Level)](#track-6-crack-the-interview-question-bank-senior--staff-level)
9. [⚖️ Spring Security 6 Master Cheat Sheet](#️-spring-security-6-master-cheat-sheet)

---

## 🛠️ Prerequisites & Foundational Knowledge

Before configuring security filters in Spring Boot 3, engineers must understand core HTTP security specifications and servlet container mechanics:

### 1. HTTP Authentication Protocol & Status Codes
- **RFC 7235 Specifications**: The client sends credentials via the `Authorization: Bearer <token>` or `Authorization: Basic <base64>` header.
- **401 Unauthorized vs 403 Forbidden**:
  - `401 Unauthorized`: **Missing or Invalid Identity**. The client has not authenticated, or their token has expired. Accompanied by a `WWW-Authenticate` header indicating acceptable authentication schemes.
  - `403 Forbidden`: **Sufficient Identity, Insufficient Permissions**. The user's identity is verified, but their roles/authorities do not grant access to the requested resource.

### 2. Servlet Filter Architecture & FilterChain
- **`jakarta.servlet.Filter`**: Low-level servlet component intercepting requests before reaching Spring MVC's `DispatcherServlet`.
- **`DelegatingFilterProxy`**: A standard Servlet Filter registered in the servlet container (Tomcat) that delegates all execution to a Spring-managed Bean named `springSecurityFilterChain`.
- **Filter Precedence**: Security filters execute in strict order. Attempting to check authorization before authentication filters have populated the user's identity results in immediate 401/403 rejections.

### 3. Statefulness vs Stateless Token Architectures
- **Stateful (Session-based)**: The server allocates memory for `HttpSession` and sets a `JSESSIONID` cookie in the browser. In microservice architectures, this requires sticky sessions or distributed session storage (Spring Session Redis).
- **Stateless (Token-based / JWT)**: The server holds zero session state. Every incoming request carries a self-contained, digitally signed JSON Web Token (JWT). The server verifies the signature mathematically without performing database lookups.

### 4. Cross-Origin Resource Sharing (CORS) Mechanics
- **The Same-Origin Policy (SOP)**: Enforced by web browsers to prevent a script on `https://evil.com` from making requests to `https://bank.com`.
- **Preflight `OPTIONS` Requests**: For non-simple requests (methods other than GET/POST or custom headers like `Authorization`), the browser emits an HTTP `OPTIONS` request. If Spring Security blocks the `OPTIONS` request with a 401/403, the browser refuses to send the actual request!

### 5. Cross-Site Request Forgery (CSRF)
- **The Attack**: An attacker tricks an authenticated browser into making an unwanted state-changing request (e.g., transferring funds) using automatically submitted session cookies.
- **Stateless APIs**: Pure REST APIs authenticated strictly via `Authorization: Bearer <token>` headers (not cookies) are **immune to CSRF** because browsers do not automatically attach bearer headers.

---

# TRACK 1: THE JUNIOR & ENTRY-LEVEL FOUNDATIONS (ZERO-TO-HERO)

## 1. The Real-World Mental Model (The Airport Security & VIP Boarding Gate)

Imagine going to an international airport for a flight:
1. **Authentication (Who are you?):**
   - You present your passport to the border security officer. The officer verifies that your photo matches your face. You are now **Authenticated** (your identity is verified).
2. **Authorization (What are you allowed to do?):**
   - You attempt to enter the VIP Champagne Lounge or sit in the cockpit. The gatekeeper checks your boarding pass: *"Your passport is valid, but your ticket is Economy Class. You cannot enter the Cockpit!"* You are **Denied Authorization** (`HTTP 403 Forbidden`).
3. **The Filter Chain (The Security Checkpoints):**
   - You cannot teleport to the departure gate; you must walk through a metal detector (`CorsFilter`), baggage scanner (`CsrfFilter`), passport control (`JwtAuthenticationFilter`), and ticket inspection (`AuthorizationFilter`).

```
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                      SPRING SECURITY FILTER CHAIN (The Security Checkpoint)            │
│                                                                                        │
│  Inbound HTTP Request ──► [ 1. CorsFilter (Border Gate) ]                              │
│                                  │                                                     │
│                                  ▼                                                     │
│                           [ 2. CsrfFilter (Anti-Forged Token Check) ]                  │
│                                  │                                                     │
│                                  ▼                                                     │
│                           [ 3. JwtAuthenticationFilter (Passport & Biometrics) ]       │
│                                  │ Extract Bearer Token, Validate Signature            │
│                                  ▼                                                     │
│                           [ 4. SecurityContextHolder ] (Passenger Cleared & Tagged)    │
│                                  │ Holds: Principal, GrantedAuthorities, Credentials   │
│                                  ▼                                                     │
│                           [ 5. AuthorizationFilter (Boarding Gate: Checks Ticket Role) │
│                                  │ hasRole('ADMIN') / hasAuthority('order:write')      │
│                                  ▼                                                     │
│                     [ DispatcherServlet ──► Your Controller ]                          │
└────────────────────────────────────────────────────────────────────────────────────────┘
```

---

## 2. The 5 Core Building Blocks

| Term | What It Means | Real-World Analogy |
| :--- | :--- | :--- |
| **`SecurityFilterChain`** | The pipeline of servlet filters that checks every request before Spring MVC sees it. | The physical metal detectors and baggage scanners at the airport. |
| **`Authentication`** | The verified identity of the user (e.g. `UsernamePasswordAuthenticationToken`). | The stamped boarding pass in your hand. |
| **`SecurityContextHolder`** | The storage strategy holding the current user's authentication details during a request. | The personal pocket where you keep your boarding pass while walking through the airport. |
| **`GrantedAuthority` / Role** | Specific permissions or roles assigned to the user (e.g. `ROLE_ADMIN`). | The seat assignment and class printed on your ticket (e.g. First Class vs Economy). |
| **JWT (JSON Web Token)** | A digitally signed, tamper-proof token carrying user claims (stateless). | A plastic festival wristband with an un-forgeable holographic seal. |

---

## 3. Beginner Code Walkthrough: Spring Security 6 Configuration

In Spring Security 6 (Spring Boot 3), the legacy `WebSecurityConfigurerAdapter` is completely removed. Security is configured declaratively using `@Bean SecurityFilterChain` and Lambda DSL:

```java
package com.example.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 1. Disable CSRF for stateless REST APIs using Bearer tokens
            .csrf(csrf -> csrf.disable())

            // 2. Configure session management to be strictly stateless
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // 3. Define URL-level authorization rules
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**", "/public/**", "/actuator/health").permitAll()
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            );

        return http.build();
    }
}
```

---

## 4. Top 10 Junior Interview Questions

### Q1: What is the difference between `hasRole('ADMIN')` and `hasAuthority('ADMIN')`?
- **ELI5 Answer:** *"`hasRole` automatically looks for a police badge stamped with 'ROLE_ADMIN', while `hasAuthority` looks for the exact word 'ADMIN'."*
- **Technical Answer:** *"`hasRole('ADMIN')` automatically prepends the prefix `ROLE_` to the parameter, checking if the user holds the authority `ROLE_ADMIN`. `hasAuthority('ADMIN')` checks for the exact raw string without prepending any prefix."*

### Q2: Why is `WebSecurityConfigurerAdapter` deprecated and removed in Spring Boot 3?
- **ELI5 Answer:** *"Instead of forcing you to inherit a giant old toolbox and override tools you don't need, Spring now lets you assemble only the exact tools you want using simple building blocks."*
- **Technical Answer:** *"It was deprecated in Spring Security 5.7 and removed in 6.0 to encourage component-based, functional configuration using `@Bean SecurityFilterChain`, eliminating tight inheritance coupling and improving container startup performance."*

### Q3: Why do we disable CSRF in stateless REST APIs?
- **ELI5 Answer:** *"CSRF tricks a browser into sending your cookies behind your back. If you don't use cookies and only use bearer tokens stored in memory, the trick is impossible."*
- **Technical Answer:** *"CSRF attacks rely on browsers automatically attaching session cookies (`JSESSIONID`) to cross-site requests. Pure REST APIs authenticate using `Authorization: Bearer <JWT>` headers, which browsers never attach automatically, rendering CSRF protection redundant and needlessly restrictive."*

### Q4: What is the role of `SecurityContextHolder`?
- **ELI5 Answer:** *"The designated tray where the security guard places your approved ID so any room you enter can quickly verify who you are."*
- **Technical Answer:** *"`SecurityContextHolder` provides access to the `SecurityContext`, which holds the `Authentication` object. By default, it uses `MODE_THREADLOCAL`, meaning the security context is isolated to the current executing worker thread."*

### Q5: How do you handle CORS in Spring Security 6?
- **ELI5 Answer:** *"Put the welcoming bouncer at the very front of the line so visitors from other websites aren't kicked out before they even say hello."*
- **Technical Answer:** *"Configure a `CorsConfigurationSource` bean and attach it to the filter chain via `http.cors(cors -> cors.configurationSource(...))`. This ensures the `CorsFilter` executes at the highest priority before authentication checks, allowing preflight `OPTIONS` requests through."*

### Q6: What does `@EnableWebSecurity` do?
- **ELI5 Answer:** *"Flips the master power switch that turns on all the security cameras, locks, and metal detectors in the building."*
- **Technical Answer:** *"It imports Spring Security's configuration classes (`WebSecurityConfiguration`, `HttpSecurityConfiguration`), activating the servlet filter chain and registering `springSecurityFilterChain` into the application context."*

### Q7: What is the difference between 401 Unauthorized and 403 Forbidden?
- **ELI5 Answer:** *"401 is: 'I don't know who you are, show me your ID.' 403 is: 'I know who you are, but you're not allowed in this VIP room.'"*
- **Technical Answer:** *"401 indicates missing or invalid authentication credentials (`AuthenticationEntryPoint`). 403 indicates that the user is authenticated, but lacks sufficient permissions or roles to access the resource (`AccessDeniedHandler`)."*

### Q8: What does `OncePerRequestFilter` guarantee?
- **ELI5 Answer:** *"Making sure the guard stamps your hand only once per visit, even if you walk through multiple doorways inside the park."*
- **Technical Answer:** *"Standard servlet filters can be invoked multiple times within a single request dispatch (e.g. during forward or error dispatches). `OncePerRequestFilter` guarantees that the filter's `doFilterInternal` method executes exactly once per incoming HTTP request."*

### Q9: Why should you never use plain MD5 or SHA-256 for passwords in Spring Security?
- **ELI5 Answer:** *"A computer can guess billions of simple passwords a second. You need a lock that makes the computer sweat and take time on each guess."*
- **Technical Answer:** *"MD5 and SHA-256 are fast cryptographic hashes. Attackers with GPUs can test billions of guesses per second. Spring Security uses `PasswordEncoder` implementations like `BCryptPasswordEncoder` or `Argon2PasswordEncoder`, which incorporate random salts and adjustable work factors."*

### Q10: What is a `UserDetailsService`?
- **ELI5 Answer:** *"The librarian who looks up an employee's paper profile in the filing cabinet when they give their username."*
- **Technical Answer:** *"A core Spring Security interface with a single method: `loadUserByUsername(String username)`. It fetches user details (password hash, enabled status, granted authorities) from a data source (database, LDAP) to be verified by an `AuthenticationProvider`."*

---

# TRACK 2: MASTER SPRING SECURITY 6 FEATURE CATALOG

## Master Security Architecture Decision Matrix

| Strategy / Feature | Performance Overhead | Security Posture | Best Used For | Anti-Pattern For |
| :--- | :--- | :--- | :--- | :--- |
| **Stateless JWT (`Bearer`)** | Minimal ($<1\text{ms}$ signature check) | Zero-trust, stateless | High-scale REST APIs, Microservices | Traditional monoliths with server-rendered HTML |
| **OAuth2 Resource Server** | Depends on JWKS caching | Enterprise Single Sign-On | Auth0, Okta, Keycloak integrations | Self-contained single-database apps |
| **Method Security (`@PreAuthorize`)** | Nanosecond reflection/SpEL | Granular defense-in-depth | Service-layer domain permission checks | Replacing URL-level endpoint filters |
| **CORS `CorsConfigurationSource`** | 0ms after browser cache | Browser cross-origin compliance | Single Page Apps (React/Angular) on separate domains | Internal microservice-to-microservice traffic |
| **`BCryptPasswordEncoder`** | Configurable ($\approx 100\text{ms}$) | High (Salted, adaptive) | General password storage | Real-time HMAC token generation |
| **`Argon2PasswordEncoder`** | Memory-hard ($\approx 64\text{MB}$) | Highest (PHC Winner) | High-assurance government/financial systems | Resource-constrained embedded systems |

---

## 2.1 `SecurityFilterChain` Bean & Lambda DSL

Spring Security 6 enforces functional lambda configuration to avoid chaining confusion:

```java
package com.example.security.config;

import com.example.security.filter.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class EnterpriseSecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    public EnterpriseSecurityConfig(JwtAuthenticationFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**", "/actuator/health").permitAll()
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/v1/billing/**").hasAuthority("billing:read")
                .anyRequest().authenticated()
            )
            // Insert custom JWT filter before the standard UsernamePasswordAuthenticationFilter
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12); // Work factor 12
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
```

---

## 2.2 Stateless JWT Authentication Filter (`OncePerRequestFilter`)

```java
package com.example.security.filter;

import com.example.security.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);
        final String userEmail = jwtService.extractUsername(jwt);

        // If username exists and user is not already authenticated in this thread context
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

            if (jwtService.isTokenValid(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                // Establish identity in the SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}
```

---

## 2.3 Method-Level Security (`@EnableMethodSecurity`)

URL authorization provides broad perimeter defense, while Method Security provides fine-grained domain-level protection:

```java
@Configuration
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class MethodSecurityConfig {}
```

```java
@Service
public class OrderService {

    // Only allow users with ROLE_ADMIN or matching owner ID
    @PreAuthorize("hasRole('ADMIN') or #customerId == authentication.principal.id")
    public Order findOrderById(Long orderId, Long customerId) {
        return orderRepository.findById(orderId).orElseThrow();
    }

    // Filter return collections based on user identity
    @PostAuthorize("returnObject.ownerUsername == authentication.name")
    public Invoice getInvoice(Long invoiceId) {
        return invoiceRepository.findById(invoiceId).orElseThrow();
    }
}
```

---

## 2.4 OAuth2 Resource Server & OIDC Integration

Integrate with identity providers (Keycloak, Auth0, Okta, Entra ID) using asymmetric public keys (JWKS):

```yaml
# application.yml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: https://auth.company.com/realms/enterprise
          jwk-set-uri: https://auth.company.com/realms/enterprise/protocol/openid-connect/certs
```

```java
@Configuration
public class OAuth2ResourceServerConfig {

    @Bean
    public SecurityFilterChain resourceServerSecurity(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/api/orders/**").hasAuthority("SCOPE_orders:read")
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter())));

        return http.build();
    }

    // Maps Keycloak realm roles to Spring Security GrantedAuthorities
    private JwtAuthenticationConverter jwtAuthConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");
        grantedAuthoritiesConverter.setAuthoritiesClaimName("roles");

        JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
        jwtConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return jwtConverter;
    }
}
```

---

## 2.5 Production CORS Hardening (`CorsConfigurationSource`)

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(List.of("https://app.company.com", "https://admin.company.com"));
    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With", "Accept"));
    configuration.setExposedHeaders(List.of("X-Total-Count", "Content-Disposition"));
    configuration.setAllowCredentials(true); // Mandatory for cookies, but forbidden with wildcard "*" origin!
    configuration.setMaxAge(3600L);          // Preflight cached for 1 hour

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}
```

---

# TRACK 3: FRAMEWORK INTERNALS & UNDER-THE-HOOD ARCHITECTURE

## 3.1 The Delegation Pipeline: Tomcat to `SecurityFilterChain`

```
┌────────────────────────────────────────────────────────────────────────┐
│                        SERVLET CONTAINER (Tomcat)                      │
│                                                                        │
│   HTTP Request ──► [ Standard Servlet Filters (Logging, Tracing) ]     │
│                             │                                          │
│                             ▼                                          │
│                  [ DelegatingFilterProxy ]                             │
│                             │ (Bridge: looks up Spring Bean)           │
│                             ▼                                          │
│                  [ FilterChainProxy (Bean) ]                           │
│                             │                                          │
│                             ▼                                          │
│                  List<SecurityFilterChain>                             │
│                             │ Matches RequestMatcherPattern            │
│                             ▼                                          │
│         [ SecurityFilterChain (14 - 18 Security Filters) ]             │
│           ├── DisableEncodeUrlFilter                                   │
│           ├── CorsFilter                                               │
│           ├── CsrfFilter                                               │
│           ├── JwtAuthenticationFilter                                  │
│           ├── ExceptionTranslationFilter                               │
│           └── AuthorizationFilter                                      │
│                             │                                          │
│                             ▼                                          │
│                     [ DispatcherServlet ]                              │
└────────────────────────────────────────────────────────────────────────┘
```

---

## 3.2 `AuthenticationManager` & `AuthenticationProvider` Chain

Authentication is decoupled from authorization via the `AuthenticationManager` interface. The standard implementation is **`ProviderManager`**:

```
[ AuthenticationManager (ProviderManager) ]
       │
       ├──► Try Provider 1: [ JwtAuthenticationProvider ]  ──► (Cannot handle UsernamePassword -> skips)
       ├──► Try Provider 2: [ DaoAuthenticationProvider ]  ──► (Validates username/hash against DB)
       └──► Try Provider 3: [ LdapAuthenticationProvider ] ──► (Fallback)
```

---

## 3.3 `SecurityContextHolder` Storage Strategies

`SecurityContextHolder` delegates storage to a pluggable strategy:
1. `MODE_THREADLOCAL` (Default): Binds the context to the current thread via `ThreadLocal<SecurityContext>`.
2. `MODE_INHERITABLETHREADLOCAL`: Inherits the context to child threads spawned by the current thread (caution: connection pool worker thread reuse can cause memory leaks!).
3. `MODE_GLOBAL`: Shared statically across all JVM threads (used in desktop client apps).

*Virtual Threads Warning (Java 21)*: Virtual threads do not share thread pools, making `MODE_THREADLOCAL` safe, but caution is required when using asynchronous parallel streams (`CompletableFuture.supplyAsync()`). Always use `DelegatingSecurityContextExecutor` when propagating security contexts across thread boundaries.

---

# TRACK 4: PRODUCTION ENGINEERING, KEY ROTATION & ZERO-TRUST HARDENING

## 4.1 Token Security: Secure Cookie Pattern vs LocalStorage

Storing JWT access tokens in `localStorage` exposes them to complete theft via any Cross-Site Scripting (XSS) vulnerability.

### The Zero-Trust Cookie Defense:
1. **Access Token**: Short-lived (15 minutes), stored in memory or sent via `Authorization` header.
2. **Refresh Token**: Long-lived (7 days), stored in an `HttpOnly`, `Secure`, `SameSite=Strict` cookie. JavaScript has zero access to this cookie, eliminating XSS token harvesting.

```java
ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
    .httpOnly(true)
    .secure(true) // HTTPS only
    .path("/api/v1/auth/refresh")
    .maxAge(Duration.ofDays(7))
    .sameSite("Strict")
    .build();

response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
```

---

# TRACK 5: WAR ROOM POST-MORTEMS & ROOT CAUSE ANALYSIS (RCAs)

## Incident 1: SecurityContext ThreadLocal Leak across Virtual Threads

- **Severity:** P0 Security Incident (Cross-Tenant Account Takeover)
- **Mean Time to Recovery (MTTR):** 28 minutes
- **Symptoms:** Under high load, User A saw User B's billing records after refreshing the page.
- **Root Cause:** A developer used a custom thread pool executor to process async order checks without wrapping it in `DelegatingSecurityContextExecutor`. When worker threads were returned to the pool without calling `SecurityContextHolder.clearContext()`, subsequent requests executed with the previous user's cached identity.
- **The Permanent Fix:**
  1. Enforce `finally { SecurityContextHolder.clearContext(); }` in all custom filters.
  2. Use Spring's `DelegatingSecurityContextAsyncTaskExecutor` for all async task execution.

---

## Incident 2: CORS Preflight 403 Forbidden Outage on React Frontend

- **Severity:** P1 Outage (Complete frontend API failure)
- **Symptoms:** All `POST` and `PUT` requests failed from the React single-page app with `CORS policy: Response to preflight request doesn't pass access control check: It does not have HTTP ok status`.
- **Root Cause:** A developer placed an endpoint authorization rule:
  ```java
  .requestMatchers("/api/**").authenticated()
  ```
  This rule intercepted and blocked the browser's HTTP `OPTIONS` preflight request with a `401 Unauthorized` before the `CorsFilter` could process the headers.
- **The Permanent Fix:**
  Add `.requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()` or configure `http.cors(Customizer.withDefaults())` with a proper `CorsConfigurationSource` bean.

---

# TRACK 6: CRACK-THE-INTERVIEW QUESTION BANK (SENIOR & STAFF+ LEVEL)

### 1. How does Spring Security handle authorization decisions internally in Spring Security 6?
Spring Security 6 replaced the legacy `AccessDecisionManager` and `AccessDecisionVoter` architecture with the streamlined **`AuthorizationManager<T>`** interface. During request evaluation, `AuthorizationFilter` delegates directly to an `AuthorizationManager` (e.g. `AuthorityAuthorizationManager`), which evaluates the user's `GrantedAuthority` collection and returns an `AuthorizationDecision(boolean granted)`.

### 2. What happens if you fail to call `SecurityContextHolder.clearContext()`?
Because worker threads are pooled by servlet containers (Tomcat), failing to clear the context causes the authenticated user's state to persist on that thread. When the thread is subsequently assigned to a different client request, that request may inherit the previous user's permissions, causing catastrophic privilege escalation and cross-tenant data leaks.

### 3. How do you secure Actuator endpoints without breaking Prometheus scraping?
Isolate management port security by defining two separate `SecurityFilterChain` beans using `@Order`:
```java
@Bean
@Order(1)
public SecurityFilterChain actuatorFilterChain(HttpSecurity http) throws Exception {
    return http
        .securityMatcher(EndpointRequest.toAnyEndpoint())
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(EndpointRequest.to(HealthEndpoint.class)).permitAll()
            .anyRequest().hasRole("MONITORING")
        )
        .httpBasic(Customizer.withDefaults())
        .build();
}
```

---

## ⚖️ Spring Security 6 Master Cheat Sheet

| Task / Configuration | Production Implementation |
| :--- | :--- |
| **Stateless Session** | `session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)` |
| **Disable CSRF for REST** | `csrf -> csrf.disable()` |
| **Method Security** | `@EnableMethodSecurity` + `@PreAuthorize("hasRole('ADMIN')")` |
| **CORS Preflight** | `http.cors(Customizer.withDefaults())` + `CorsConfigurationSource` |
| **Password Encoding** | `new BCryptPasswordEncoder(12)` |
| **Filter Placement** | `.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)` |
| **Clear Context Hygiene**| `SecurityContextHolder.clearContext()` in filter `finally` block |
| **OAuth2 Resource Server**| `http.oauth2ResourceServer(oauth -> oauth.jwt(Customizer.withDefaults()))` |

---
[🏠 Back to Home](README.md) | [🍃 Spring Boot Master Guide](spring_master_guide.md) | [🏛️ Spring Data JPA Guide](spring_data_jpa.md)
