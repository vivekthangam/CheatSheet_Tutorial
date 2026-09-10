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

# TRACK 2: MASTER SPRING SECURITY 6 FEATURE CATALOG

## Master Security Architecture Decision Matrix

| Security Pattern | Primary Mechanism | Statefulness | Best Used For | Anti-Pattern For |
| :--- | :--- | :--- | :--- | :--- |
| **Stateless JWT** | Signed Bearer Token | Zero (Stateless) | High-scale REST APIs, Mobile Apps | Immediate server-side token revocations |
| **OAuth2 Resource Server** | JWKS Asymmetric Verification | Zero (Stateless) | Enterprise IdP (Keycloak, Okta, Entra)| Monolithic apps without an IdP |
| **Method Security** | AOP CGLIB / Byte Buddy Proxy | ThreadLocal Context | Domain-level fine-grained ABAC | High-frequency inner loop iterations |
| **CORS Configuration** | Preflight `OPTIONS` + Headers | Browser-enforced | Single-Page Apps (React, Angular) | Server-to-server internal microservices |
| **CSRF Protection** | Synchronizer Token Pattern | Stateful Session | Browser-based forms with cookies | Pure stateless `Authorization: Bearer` APIs |
| **Multi-Tenant Routing** | Dynamic TenantContextResolver | Stateless | Multi-tenant SaaS with isolated IdPs | Single-tenant monolithic applications |
| **Session Fixation Defense**| `changeSessionId()` on login | Stateful | Traditional server-rendered web apps | Stateless microservices |
| **Remember-Me** | Two-factor persistent token hash| DB Persistent Token | E-commerce shopping portals | High-security banking applications |
| **Problem Details 7807** | RFC 7807 JSON Error Payloads | Stateless | Uniform REST error contracts | HTML browser redirect logins |
| **Security 6 Lambda DSL** | Type-safe declarative lambdas | Compile-time | Spring Boot 3+ modern security | Legacy XML or `.and()` chaining |

---

## 2.1 DelegatingFilterProxy & SecurityFilterChain Pipeline

1. **Architectural Overview & Purpose**:
   - Acts as the architectural bridge connecting the Servlet Container (Tomcat/Jetty) with the Spring ApplicationContext. Standard Servlet Filters are managed by Tomcat, but security requires dependency injection. `DelegatingFilterProxy` intercepts HTTP traffic and delegates execution to the Spring bean named `springSecurityFilterChain` (`FilterChainProxy`).

2. **Underlying Algorithm & Filter Chain Execution**:
   - `FilterChainProxy` contains a `List<SecurityFilterChain>`. For each incoming request:
     1. It tests `requestMatcher.matches(request)` against each chain sequentially.
     2. The **first matching chain** is selected.
     3. It executes the chain's filters in strict priority order.
     4. If an unauthenticated request reaches an authorization filter, an `AccessDeniedException` or `AuthenticationException` is thrown, caught by `ExceptionTranslationFilter`.

3. **Full Syntax & Method Signatures**:
   ```java
   @Bean
   public SecurityFilterChain filterChain(HttpSecurity http) throws Exception;
   ```

4. **Concrete Code Examples with Sample Values & Expected Results**:
   ```java
   @Configuration
   @EnableWebSecurity
   public class SecurityConfig {

       @Bean
       public SecurityFilterChain apiSecurity(HttpSecurity http) throws Exception {
           return http
               .securityMatcher("/api/**") // Applies strictly to /api/** endpoints
               .csrf(csrf -> csrf.disable())
               .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
               .authorizeHttpRequests(auth -> auth
                   .requestMatchers("/api/auth/**").permitAll()
                   .requestMatchers("/api/admin/**").hasRole("ADMIN")
                   .anyRequest().authenticated()
               )
               .build();
       }
   }
   ```
   - **Sample HTTP Response**:
     ```http
     HTTP/1.1 401 Unauthorized
     WWW-Authenticate: Bearer
     Content-Type: application/json
     {"error": "Unauthorized", "message": "Full authentication is required to access this resource"}
     ```

5. **Pros & Cons**:
   - **Pros**: Clean modular security; multiple independent filter chains can protect different URL paths (e.g. `/api/**` vs `/admin/**`).
   - **Cons**: Filter ordering is strict; misplacing custom filters causes silent security bypasses.

---

## 2.2 Stateless JWT Authentication Filter (`OncePerRequestFilter`)

1. **Architectural Overview & Purpose**:
   - Intercepts every incoming HTTP request, extracts the `Authorization: Bearer <token>` header, verifies the cryptographic signature, and populates the `SecurityContextHolder`.

2. **Underlying Mechanics**:
   - Extends `OncePerRequestFilter` to guarantee single execution per request dispatch (preventing duplicate execution across internal MVC forwards or asynchronous dispatches).

3. **Production Implementation Blueprint**:
   ```java
   @Component
   public class JwtAuthenticationFilter extends OncePerRequestFilter {

       private final JwtTokenService jwtService;
       private final UserDetailsService userDetailsService;

       public JwtAuthenticationFilter(JwtTokenService jwtService, UserDetailsService userDetailsService) {
           this.jwtService = jwtService;
           this.userDetailsService = userDetailsService;
       }

       @Override
       protected void doFilterInternal(
               @NonNull HttpServletRequest request,
               @NonNull HttpServletResponse response,
               @NonNull FilterChain filterChain) throws ServletException, IOException {

           final String authHeader = request.getHeader("Authorization");
           if (authHeader == null || !authHeader.startsWith("Bearer ")) {
               filterChain.doFilter(request, response);
               return; // Skip filter if no bearer token present
           }

           final String jwt = authHeader.substring(7);
           final String username = jwtService.extractUsername(jwt);

           if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
               UserDetails userDetails = userDetailsService.loadUserByUsername(username);

               if (jwtService.validateToken(jwt, userDetails)) {
                   var authToken = new UsernamePasswordAuthenticationToken(
                       userDetails,
                       null,
                       userDetails.getAuthorities()
                   );
                   authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                   // Establish verified identity in ThreadLocal SecurityContext
                   SecurityContextHolder.getContext().setAuthentication(authToken);
               }
           }
           filterChain.doFilter(request, response);
       }
   }
   ```

---

## 2.3 Method-Level Security & SpEL Expressions (`@EnableMethodSecurity`)

1. **Architectural Overview & Purpose**:
   - Enforces Attribute-Based Access Control (ABAC) and Role-Based Access Control (RBAC) directly on Java Service methods via CGLIB dynamic proxies, ensuring security even if methods are invoked from non-HTTP entry points (Kafka consumers, batch jobs, CLI).

2. **Core Annotations**:
   - `@PreAuthorize`: Evaluated before method entry. Can inspect arguments (`#orderId`).
   - `@PostAuthorize`: Evaluated after method execution. Can inspect return value (`returnObject`).
   - `@PreFilter` / `@PostFilter`: Filters collection arguments or returned collections based on user ownership.

3. **Concrete Code Examples with Sample Values & Expected Results**:
   ```java
   @Configuration
   @EnableMethodSecurity(prePostEnabled = true)
   public class MethodSecurityConfig {}

   @Service
   public class InvoiceService {

       // Only allow ADMIN or the owner of the customer account
       @PreAuthorize("hasRole('ADMIN') or #customerId == authentication.principal.id")
       public Invoice generateInvoice(Long customerId, Double amount) {
           return new Invoice(customerId, amount, "PAID");
       }

       // Only return invoice if the current user owns it
       @PostAuthorize("returnObject.ownerUsername == authentication.name")
       public Invoice getInvoice(Long invoiceId) {
           return invoiceRepository.findById(invoiceId).orElseThrow();
       }
   }
   ```
   - **Sample Failure**: If user `bob` calls `generateInvoice(customerId=999)` where `principal.id=404`, Spring Security throws `AccessDeniedException: Access Denied`.

---

## 2.4 OAuth2 Resource Server & Dynamic JWKS Key Rotation

1. **Architectural Overview & Purpose**:
   - Validates incoming JWTs against an external OpenID Connect (OIDC) identity provider (Keycloak, Auth0, Okta, AWS Cognito) using asymmetric public keys fetched from the **JSON Web Key Set (JWKS)** endpoint.

2. **Underlying Cryptographic Verification**:
   - The token contains a Key ID in its header (`kid: "key-2026-rsa"`).
   - Spring Security queries the JWKS endpoint, retrieves the public RSA/EC key corresponding to `kid`, and verifies the signature off-line without making network calls for every request. Public keys are cached and refreshed automatically on key rotation!

3. **Production `application.yml` & Configuration**:
   ```yaml
   spring:
     security:
       oauth2:
         resourceserver:
           jwt:
             issuer-uri: https://auth.company.com/realms/enterprise
             jwk-set-uri: https://auth.company.com/realms/enterprise/protocol/openid-connect/certs
   ```
   ```java
   @Bean
   public SecurityFilterChain resourceServerFilterChain(HttpSecurity http) throws Exception {
       return http
           .oauth2ResourceServer(oauth2 -> oauth2
               .jwt(jwt -> jwt.jwtAuthenticationConverter(customJwtAuthenticationConverter()))
           )
           .authorizeHttpRequests(auth -> auth
               .requestMatchers("/api/orders/**").hasAuthority("SCOPE_orders:read")
               .anyRequest().authenticated()
           )
           .build();
   }
   ```

---

## 2.5 Production CORS & CSRF Defense

1. **CORS (Cross-Origin Resource Sharing)**:
   - Must be configured at the security filter level (`CorsFilter`), because preflight HTTP `OPTIONS` requests omit authentication headers and must pass through without 401 rejections.
   ```java
   @Bean
   public CorsConfigurationSource corsConfigurationSource() {
       CorsConfiguration config = new CorsConfiguration();
       config.setAllowedOrigins(List.of("https://dashboard.company.com"));
       config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
       config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With"));
       config.setAllowCredentials(true);
       config.setMaxAge(3600L); // Cache preflight for 1 hour

       UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
       source.registerCorsConfiguration("/**", config);
       return source;
   }
   ```

2. **CSRF (Cross-Site Request Forgery)**:
   - For stateless REST APIs with JWT in headers, disable CSRF: `csrf -> csrf.disable()`.
   - For session cookie apps, use the **CookieCsrfTokenRepository**:
     ```java
     .csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
     ```

---

## 2.6 Multi-Tenant Authentication Routing

1. **Architectural Overview**:
   - In B2B SaaS, each corporate customer has their own dedicated IdP (e.g. Tenant A uses Okta, Tenant B uses Microsoft Azure Entra ID).

2. **Dynamic JWT Decoder Routing**:
   ```java
   @Bean
   public AuthenticationManagerResolver<HttpServletRequest> tenantAuthManagerResolver() {
       return request -> {
           String tenantId = request.getHeader("X-Tenant-ID");
           String issuer = "https://auth.company.com/" + tenantId;
           return new JwtAuthenticationProvider(JwtDecoders.fromIssuerLocation(issuer))::authenticate;
       };
   }
   ```

---

## 2.7 Session Management & Fixation Protection

1. **Session Creation Policies**:
   - `SessionCreationPolicy.STATELESS`: Spring Security never creates or reads `HttpSession`. Ideal for REST APIs.
   - `SessionCreationPolicy.IF_REQUIRED`: Creates a session only when required (Default for web apps).
2. **Session Fixation Defense**:
   - When a user logs in, the attacker might know the pre-login session ID. Spring Security automatically calls `changeSessionId()` on authentication, issuing a brand new session cookie while retaining session attributes.

---

## 2.8 Remember-Me Authentication & Persistent Token Repository

1. **The Vulnerability in Cookie-Only Remember-Me**:
   - Standard cookie remember-me (`username + expiration + signature`) can be captured and reused until expiration.
2. **The Persistent Token Solution**:
   - Stores `(series, token, last_used)` in database. Every login presents the series and token. If token matches, a new token is generated. If an attacker reuses an old token, Spring Security detects token theft, invalidates all sessions for that user, and alerts security!

---

## 2.9 Custom AuthenticationEntryPoint & AccessDeniedHandler

1. **Uniform RFC 7807 Problem Details Response**:
   ```java
   @Bean
   public AuthenticationEntryPoint customAuthEntryPoint() {
       return (request, response, authException) -> {
           response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
           response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
           response.getWriter().write("""
               {
                   "type": "https://api.company.com/errors/unauthorized",
                   "title": "Unauthorized",
                   "status": 401,
                   "detail": "%s",
                   "instance": "%s"
               }
               """.formatted(authException.getMessage(), request.getRequestURI()));
       };
   }
   ```

---

## 2.10 Modern Spring Security 6 Lambda DSL & Migration Architecture

1. **Eliminating Deprecated Chaining**:
   - Old Spring Security used `.and()` chaining:
     ```java
     // ❌ DEPRECATED OLD SYNTAX
     http.csrf().disable().and().authorizeRequests().antMatchers("/api/**").authenticated();
     ```
   - Modern Spring Security 6 enforces **Lambda DSL**:
     ```java
     // ✅ MODERN SPRING SECURITY 6 SYNTAX
     http
         .csrf(AbstractHttpConfigurer::disable)
         .authorizeHttpRequests(auth -> auth
             .requestMatchers("/api/**").authenticated()
         );
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

Storing JWT access tokens in browser `localStorage` exposes them to complete theft via Cross-Site Scripting (XSS).

### The Zero-Trust Dual-Token Architecture:
1. **Access Token (Short-Lived: 15 mins)**:
   - Kept purely in client memory (React/Vue/Angular state) or sent via `Authorization: Bearer <token>` header.
2. **Refresh Token (Long-Lived: 7 days)**:
   - Stored in an `HttpOnly`, `Secure`, `SameSite=Strict` cookie bound to `/api/v1/auth/refresh`. JavaScript has zero access to this cookie, completely neutralizing XSS token harvesting.

```java
ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
    .httpOnly(true)
    .secure(true) // HTTPS only in production
    .path("/api/v1/auth/refresh")
    .maxAge(Duration.ofDays(7))
    .sameSite("Strict")
    .build();

response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
```

---

## 4.2 Dynamic Asymmetric Key Rotation (JWKS)

In cloud-native zero-trust architectures, identity providers (Keycloak, Okta, Entra ID) periodically rotate their RSA/EC private keys.
- **The Key ID (`kid`) Header**:
  Each issued JWT includes the `kid` in its header:
  ```json
  { "alg": "RS256", "typ": "JWT", "kid": "key-2026-q3" }
  ```
- **Caching & Eviction**:
  Spring Security's `NimbusJwtDecoder` automatically caches public keys fetched from the JWKS URI. If a token arrives with an unknown `kid`, Nimbus initiates an out-of-band HTTPS request to refresh its public key cache, achieving seamless zero-downtime key rotation without restarting backend microservices.

---

## 4.3 Mutual TLS (mTLS) & Zero-Trust Service-to-Service Security

For internal microservice-to-microservice traffic, perimeter firewalls are insufficient. Zero-Trust requires **mTLS**:
1. The client presents an X.509 certificate during the TLS handshake.
2. The server verifies the certificate against an internal corporate Certificate Authority (CA).
3. Spring Security extracts the client identity via `X509AuthenticationFilter`:

```java
@Bean
public SecurityFilterChain mTLSSecurityFilterChain(HttpSecurity http) throws Exception {
    return http
        .x509(x509 -> x509
            .subjectPrincipalRegex("CN=(.*?)(?:,|$)")
            .userDetailsService(clientCertificateUserDetailsService())
        )
        .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
        .build();
}
```

---

## 4.4 Brute-Force Defense & Rate Limiting with Redis & Bucket4j

To defend login endpoints (`/api/v1/auth/login`) against credential stuffing and brute-force attacks:
```java
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private final ProxyManager<String> buckets;

    public RateLimitingFilter(ProxyManager<String> buckets) {
        this.buckets = buckets;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if ("/api/v1/auth/login".equals(request.getRequestURI())) {
            String clientIp = request.getRemoteAddr();
            BucketConfiguration config = BucketConfiguration.builder()
                .addLimit(Bandwidth.builder().capacity(5).refillGreedy(5, Duration.ofMinutes(1)).build())
                .build();

            Bucket bucket = buckets.builder().build(clientIp, config);

            if (!bucket.tryConsume(1)) {
                response.setStatus(429); // HTTP 429 Too Many Requests
                response.getWriter().write("{\"error\": \"Too many login attempts. Please wait 1 minute.\"}");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}
```

---

# TRACK 5: WAR ROOM POST-MORTEMS & ROOT CAUSE ANALYSIS (RCAs)

## Incident 1: SecurityContext ThreadLocal Leak across Virtual Threads & Thread Pools

- **Severity:** P0 Security Incident (Cross-Tenant Account Takeover)
- **Mean Time to Recovery (MTTR):** 28 minutes
- **Symptoms:** Under high concurrent traffic, User A intermittently saw User B's private account records after refreshing their dashboard.
- **Root Cause:** A developer used a custom `ThreadPoolTaskExecutor` to execute asynchronous billing computations without wrapping the executor with `DelegatingSecurityContextAsyncTaskExecutor`. When worker threads were returned to the pool without invoking `SecurityContextHolder.clearContext()`, subsequent requests processed on those reused threads inherited the previous user's cached identity.
- **The Permanent Fix:**
  1. Enforce `finally { SecurityContextHolder.clearContext(); }` in all custom security filters.
  2. Register `DelegatingSecurityContextAsyncTaskExecutor` as the default asynchronous task executor:
     ```java
     @Bean
     public AsyncTaskExecutor applicationTaskExecutor() {
         ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
         executor.initialize();
         return new DelegatingSecurityContextAsyncTaskExecutor(executor);
     }
     ```

---

## Incident 2: CORS Preflight 403 Forbidden Outage on Single-Page Frontend

- **Severity:** P1 Outage (Complete frontend API failure)
- **Symptoms:** All `POST`, `PUT`, and `DELETE` requests failed from the React web client with `CORS policy: Response to preflight request doesn't pass access control check: It does not have HTTP ok status (403 Forbidden)`.
- **Root Cause:** The developer declared URL patterns using:
  ```java
  .authorizeHttpRequests(auth -> auth
      .requestMatchers("/api/**").authenticated()
  )
  ```
  Browsers automatically emit HTTP `OPTIONS` requests before sending actual requests with custom headers (`Authorization`). Because the `OPTIONS` request contains no bearer token, the security filter chain rejected it with `403 Forbidden` before the CORS filter could attach the required `Access-Control-Allow-Origin` headers.
- **The Permanent Fix:**
  1. Insert `http.cors(Customizer.withDefaults())` at the top of the security chain.
  2. Explicitly permit HTTP `OPTIONS` requests across all paths:
     ```java
     .authorizeHttpRequests(auth -> auth
         .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
         .requestMatchers("/api/**").authenticated()
     )
     ```

---

## Incident 3: CSRF Token Desynchronization in Cookie-Based Single Page Application

- **Severity:** P2 Production Defect (Checkout and payment submissions failed for 100% of returning users)
- **Symptoms:** Users could browse the catalogue, but clicking "Pay Now" threw an immediate `403 Forbidden` with `Invalid CSRF Token`.
- **Root Cause:** The backend configured `CookieCsrfTokenRepository.withHttpOnlyFalse()`, expecting the frontend to read the `XSRF-TOKEN` cookie and send it back as an `X-XSRF-TOKEN` header. However, Spring Security 6 introduced deferred CSRF tokens: the token is not written into the cookie until the token is explicitly accessed in the request.
- **The Permanent Fix:**
  Implement a `CsrfCookieFilter` ensuring the token is resolved on every request:
  ```java
  public class CsrfCookieFilter extends OncePerRequestFilter {
      @Override
      protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
              throws ServletException, IOException {
          CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
          if (csrfToken != null) {
              csrfToken.getToken(); // Forces lazy token resolution and cookie emission
          }
          filterChain.doFilter(request, response);
      }
  }
  ```

---

# TRACK 6: CRACK-THE-INTERVIEW QUESTION BANK (SENIOR & STAFF+ LEVEL)

### 1. How does Spring Security 6 evaluate authorization decisions internally?
Spring Security 6 replaced legacy `AccessDecisionManager` and `AccessDecisionVoter` components with the streamlined **`AuthorizationManager<T>`** interface. During request evaluation, `AuthorizationFilter` delegates directly to an `AuthorizationManager` (such as `AuthorityAuthorizationManager`), which evaluates the user's `GrantedAuthority` collection and returns an `AuthorizationDecision(boolean granted)`.

### 2. What is the fundamental difference between `hasRole('ADMIN')` and `hasAuthority('ADMIN')`?
`hasRole('ADMIN')` automatically prepends the prefix `ROLE_` to the argument, checking whether the user possesses the authority `ROLE_ADMIN`. In contrast, `hasAuthority('ADMIN')` performs an exact string match for `ADMIN` without prefix modification.

### 3. Why must `SecurityContextHolder.clearContext()` always be called in a `finally` block?
Because servlet containers (e.g. Tomcat) reuse worker threads across different client requests. If the security context is not explicitly cleared, subsequent requests dispatched on that thread inherit the previous client's credentials, causing critical cross-tenant data leaks and privilege escalation.

### 4. How does Method Security SpEL evaluation work under the hood?
Method security is enforced via Spring AOP proxies (CGLIB or JDK dynamic proxies). When a method annotated with `@PreAuthorize` is invoked, the proxy's `MethodSecurityInterceptor` parses the SpEL expression into an Abstract Syntax Tree (AST), constructs a `MethodSecurityEvaluationContext` containing the method arguments and `SecurityContext`, and evaluates the boolean expression prior to proceeding with target execution.

### 5. What are the dangers of using `MODE_INHERITABLETHREADLOCAL` in a modern microservice?
`InheritableThreadLocal` passes security context from a parent thread to child threads spawned via `new Thread()`. In thread-pooled environments (like `ForkJoinPool` or `ThreadPoolExecutor`), worker threads are long-lived and reused. Child threads retain stale parent contexts from previous invocations, causing severe context pollution and memory leaks.

### 6. How do you protect Spring Boot Actuator endpoints from unauthorized exposure?
Configure a dedicated, prioritized `SecurityFilterChain` bean marked with `@Order(1)`:
```java
@Bean
@Order(1)
public SecurityFilterChain actuatorSecurity(HttpSecurity http) throws Exception {
    return http
        .securityMatcher(EndpointRequest.toAnyEndpoint())
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(EndpointRequest.to(HealthEndpoint.class, InfoEndpoint.class)).permitAll()
            .anyRequest().hasRole("OPS_MONITORING")
        )
        .httpBasic(Customizer.withDefaults())
        .build();
}
```

### 7. How does OAuth2 Resource Server handle token validation without calling the Identity Provider on every request?
The Resource Server retrieves the IdP's public signing keys from its **JWKS (JSON Web Key Set)** endpoint upon startup and caches them. When a JWT arrives, the server mathematically verifies the cryptographic signature (e.g., using RSA public key or EC public key) and verifies claims (`exp`, `nbf`, `iss`, `aud`) locally in-memory without initiating network calls.

### 8. What is the difference between OAuth2 Introspection and local JWKS validation?
- **JWKS Local Validation**: Token must be a signed JWT. Verification is executed off-line in-memory. Ultra-fast ($<0.1\text{ms}$), but cannot detect immediate server-side revocations until token expiry.
- **Opaque Token Introspection (RFC 7662)**: The token is an arbitrary random string. The Resource Server makes an HTTP POST call to the Authorization Server's `/oauth/introspect` endpoint on every request. Higher latency ($20\text{ms}-50\text{ms}$), but supports instantaneous revocation.

### 9. Why should you never disable CSRF protection in server-side rendered cookie-based web apps?
In cookie-based applications, web browsers automatically attach stored session cookies to cross-origin requests. An attacker hosting `evil.com` can craft a hidden form targeting `bank.com/transfer` and trick the user into submitting it. Without a synchronizer CSRF token, the server cannot distinguish between legitimate user submissions and forged cross-site requests.

### 10. How does Reactive Spring Security operate in Spring WebFlux?
In Spring WebFlux, execution hops across multiple Netty event loop threads, rendering `ThreadLocal` ineffective. Reactive Spring Security leverages **Reactor Context** (`reactor.util.context.Context`), binding the `Mono<SecurityContext>` to the subscriber pipeline and propagating user identity across asynchronous non-blocking operators.

---

## ⚖️ Spring Security 6 Master Cheat Sheet

| Requirement | Modern Spring Security 6 Implementation |
| :--- | :--- |
| **Stateless REST API** | `sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))` |
| **Disable CSRF for REST** | `csrf(AbstractHttpConfigurer::disable)` |
| **CORS Configuration** | `cors(Customizer.withDefaults())` + registered `CorsConfigurationSource` |
| **Method Security** | `@EnableMethodSecurity(prePostEnabled = true)` on config class |
| **Custom Filter Position** | `.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)` |
| **Password Encoder** | `new BCryptPasswordEncoder(12)` |
| **Secure Actuator** | `@Order(1)` chain matching `EndpointRequest.toAnyEndpoint()` |
| **Clear Context** | `SecurityContextHolder.clearContext()` inside filter `finally` block |
| **Extract Principal** | `@AuthenticationPrincipal UserDetails user` in `@RestController` |
| **Problem Details 7807** | Custom `AuthenticationEntryPoint` writing `application/problem+json` |

---
[🏠 Back to Home](README.md) | [🍃 Spring Boot Master Guide](spring_master_guide.md) | [🏛️ Spring Data JPA Guide](spring_data_jpa.md)

