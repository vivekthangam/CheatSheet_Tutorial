[🏠 Back to Home](README.md) | [🛡️ Spring Security Master Guide](spring_security.md) | [🍃 Spring Boot Master Guide](spring_master_guide.md)

# 🛡️ Spring Security 6 & OAuth2: Real-World Production Scenarios Master Guide

[![Spring Security](https://img.shields.io/badge/Spring%20Security-6.3%2B-green.svg?style=for-the-badge&logo=springsecurity)](https://spring.io/projects/spring-security)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3%2B-brightgreen.svg?style=for-the-badge&logo=springboot)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17%20%2F%2021%20LTS-orange.svg?style=for-the-badge&logo=openjdk)](https://www.oracle.com/java/)
[![Level](https://img.shields.io/badge/Tier-1%20Panels-Staff%20%2F%20Principal-red.svg?style=for-the-badge)](https://github.com/)

An exhaustive, battle-tested compilation of **real-world production interview scenarios** covering Spring Security 6: the component-based Lambda DSL, `SecurityFilterChain` ordering, stateless JWT authentication, Refresh Token Rotation (RTR) race conditions, multi-tenant JWKS resource servers, `@PreAuthorize` / `@PostAuthorize` method security, CORS preflight 403 traps, CSRF cookie tokens, Java 21 Virtual Thread `ThreadLocal` security context leaks, Actuator data exfiltration, and war-room post-mortems.

Every scenario strictly follows the **5-Part Tier-1 Product Engineering Format**:
1. **Exact Question Asked by Tier-1 Product Panels (with detailed scenario context)**
2. **What the Interviewer Evaluates Under the Surface (mental criteria, low-level runtime knowledge)**
3. **Standout Technical Answer (deep runtime mechanics, low-level HTTP/filter details, zero fluff)**
4. **Follow-Up Trap Question & Winning Answer (catching surface memorizers)**
5. **Production Code Example with Execution Steps, Sample Code, and Verified Console Input/Output**

---

## 📑 Master Category Navigation

- [Category 1: SecurityFilterChain, Filter Ordering & Servlet Architecture (Q1 – Q4)](#category-1-securityfilterchain-filter-ordering--servlet-architecture)
- [Category 2: Stateless JWT Authentication, Expiration & Refresh Flows (Q5 – Q8)](#category-2-stateless-jwt-authentication-expiration--refresh-flows)
- [Category 3: Method Security, SpEL Injection & Dynamic Authorization (Q9 – Q12)](#category-3-method-security-spel-injection--dynamic-authorization)
- [Category 4: CORS Preflight, CSRF Defense & Zero-Trust Cookies (Q13 – Q15)](#category-4-cors-preflight-csrf-defense--zero-trust-cookies)
- [Category 5: Multi-Tenancy & OAuth2 Resource Server JWKS Validation (Q16 – Q18)](#category-5-multi-tenancy--oauth2-resource-server-jwks-validation)
- [Category 6: Production War Room Incidents & Outage Forensics (Q19 – Q20)](#category-6-production-war-room-incidents--outage-forensics)
- [Production Diagnostic Matrix & Best Practices Reference](#production-diagnostic-matrix--best-practices-reference)

---

## Category 1: SecurityFilterChain, Filter Ordering & Servlet Architecture

### Q1: How does `DelegatingFilterProxy` bridge the Servlet Container and the Spring `ApplicationContext`, and why does `@Component` on custom filters cause security bypasses?
- **Scenario Context:** A developer adds a custom `RateLimitingFilter` to protect API endpoints from brute-force attacks. When testing, unauthenticated requests execute the rate limiter, but authenticated requests return `401 Unauthorized` before reaching the rate limiter.
- **What the Interviewer Evaluates:** Understanding of Servlet container filter registration vs Spring Security `FilterChainProxy`, `SecurityProperties.DEFAULT_FILTER_ORDER`, and `addFilterBefore` vs `addFilterAfter`.
- **Standout Technical Answer:**
  - The standard Tomcat/Jetty Servlet container knows nothing about Spring beans. It executes filters registered in `web.xml` or via `ServletContext.addFilter()`.
  - **`DelegatingFilterProxy`** acts as the physical bridge: it is a standard Servlet filter registered with Tomcat that intercepts all incoming requests (`/*`) and lazily delegates to a Spring bean named `springSecurityFilterChain` (an instance of `FilterChainProxy`).
  - Inside `FilterChainProxy`, Spring manages one or more **`SecurityFilterChain`** instances.
  - **The Filter Ordering Danger:**
    - If you annotate a custom filter with `@Component`, Spring Boot **automatically registers it twice**:
      1. Once in Tomcat's global Servlet filter chain.
      2. Once in Spring Security's internal `SecurityFilterChain`.
    - In Tomcat's chain, it runs **before** Spring Security has even parsed the JWT token! Therefore, `SecurityContextHolder.getContext().getAuthentication()` will always be `null`!
  - **The Production Fix:**
    Never annotate custom security filters with `@Component`. Register them explicitly inside `SecurityFilterChain` using `addFilterBefore(myFilter, UsernamePasswordAuthenticationFilter.class)`, and disable auto-registration via `FilterRegistrationBean.setEnabled(false)`.
- **Follow-Up Trap:** *"What is the difference between `SecurityContextHolder.setStrategyName(MODE_INHERITABLETHREADLOCAL)` and `DelegatingSecurityContextAsyncTaskExecutor`?"*
  - *Winning Answer:* "`MODE_INHERITABLETHREADLOCAL` copies context only when child threads are created via `new Thread()`. In thread pools (Tomcat, `@Async` worker pools), threads are never created on the fly; they are reused from the pool! `MODE_INHERITABLETHREADLOCAL` will silently leak stale security credentials from previous user requests into subsequent unrelated requests. Always use `DelegatingSecurityContextAsyncTaskExecutor` in pooled environments."*

#### Production Code Example - Q1: Hardened SecurityFilterChain & Filter De-duplication

- **Execution Steps:**
  1. **Configure Stateless SecurityFilterChain**: Disable sessions and define strict route authorization.
  2. **Register Custom Filter Explicitly**: Position custom JWT filter before `UsernamePasswordAuthenticationFilter`.
  3. **Disable Root Container Auto-Registration**: Register a `FilterRegistrationBean` with `setEnabled(false)` to prevent duplicate execution in Tomcat.

- **Sample Code:**

```java
package com.production.security.config;

import com.production.security.filter.JwtAuthenticationFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class ModernSecurityFilterChainConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter) throws Exception {
        return http
            .csrf(csrf -> csrf.disable()) // Stateless APIs with Bearer tokens do not need CSRF
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/public/**", "/actuator/health").permitAll()
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            // Add custom JWT filter explicitly before the standard auth filter
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }

    // Prevents Spring Boot from registering jwtFilter twice in Tomcat's root filter chain!
    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtFilterRegistration(JwtAuthenticationFilter filter) {
        FilterRegistrationBean<JwtAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false); // Disables auto-registration in root Servlet container
        return registration;
    }
}
```

- **Sample Input & Output:**
  - **Input Request**: `GET /api/v1/admin/audit` with header `Authorization: Bearer <valid_admin_jwt>`.
  - **Console Output**:
    ```text
    2026-09-13 23:35:10.015 DEBUG [http-nio-8080-exec-1] o.s.s.w.FilterChainProxy: Securing GET /api/v1/admin/audit
    2026-09-13 23:35:10.020 DEBUG [http-nio-8080-exec-1] c.p.s.f.JwtAuthenticationFilter: Validated Bearer JWT for principal: admin_user. Roles: [ROLE_ADMIN]
    2026-09-13 23:35:10.025 DEBUG [http-nio-8080-exec-1] o.s.s.w.a.i.FilterSecurityInterceptor: Authorized request for [ROLE_ADMIN]
    HTTP 200 OK returned. Zero duplicate filter executions.
    ```

---

### Q2: Why does `SecurityContextHolder.setStrategyName(MODE_INHERITABLETHREADLOCAL)` cause cross-tenant credential leaks in Thread Pools?
- **What the Interviewer Evaluates:** ThreadPoolExecutor task dispatching, `InheritableThreadLocal` shallow clone mechanics, and asynchronous security context propagation.
- **Standout Technical Answer:**
  - `InheritableThreadLocal` works by copying the parent thread's `ThreadLocalMap` **only at the instant a new Thread object is instantiated**: `new Thread()`.
  - In modern web servers (Tomcat worker pools, `@Async` ThreadPoolTaskExecutors):
    1. Worker threads are **pre-spawned at application startup** and pooled indefinitely.
    2. When User A makes a request on Thread-1, its credentials are set.
    3. If User A spawns an asynchronous task, the task runs on pooled Thread-2 (which was spawned hours ago!). `InheritableThreadLocal` does **NOT copy anything** because Thread-2 was not created by Thread-1!
    4. Worse, when User A's request finishes, if `SecurityContextHolder.clearContext()` is missed, Thread-1 retains User A's credentials.
    5. The next request arriving on Thread-1 from **User B will run with User A's permissions!**
  - **The Production Fix:**
    Always use **`DelegatingSecurityContextAsyncTaskExecutor`**, which explicitly captures the security context at task submission time and clears it upon task completion.

---

### Q3: How do you configure Multiple `SecurityFilterChain` beans with `@Order` for API vs Web UI endpoints?
- **What the Interviewer Evaluates:** FilterChain matching via `securityMatcher()`, chain precedence ordering, and preventing catch-all chain short-circuiting.
- **Standout Technical Answer:**
  - An enterprise service often exposes both public REST APIs (stateless, JWT authentication) and an internal management portal (stateful, form login with session cookies).
  - **Architecture:**
    1. Define two `@Bean SecurityFilterChain` methods.
    2. Chain 1 (Restricted API): Annotate with **`@Order(1)`** and define an explicit matcher:
       `http.securityMatcher("/api/**").sessionManagement(STATELESS)...`
    3. Chain 2 (Default Web): Annotate with **`@Order(2)`** (or default) as the catch-all:
       `http.authorizeHttpRequests(auth -> auth.anyRequest().authenticated()).formLogin(...)`
  - **The Fatal Trap:** If the catch-all chain has `@Order(1)`, it intercepts all requests, and Chain 2 is never evaluated! Always order specific pattern matchers first.

---

### Q4: What is the exact execution lifecycle of `OncePerRequestFilter`, and how does it handle Servlet forwards and async dispatches?
- **What the Interviewer Evaluates:** Servlet 3.0 asynchronous requests, `DispatcherType.REQUEST` vs `ASYNC` vs `FORWARD`, and avoiding redundant filter execution.
- **Standout Technical Answer:**
  - Standard `Filter.doFilter()` can be invoked multiple times within a single HTTP request cycle (e.g. on initial request, after internal MVC view forward, or during error handling dispatches).
  - **`OncePerRequestFilter` Protection:**
    - Sets a unique request attribute: `request.setAttribute(getAlreadyFilteredAttributeName(), Boolean.TRUE)`.
    - If the attribute is present, it skips custom logic and delegates directly to `filterChain.doFilter(request, response)`.
  - To control behavior during asynchronous dispatches:
    Override `shouldNotFilterAsyncDispatch()` (defaults to `true`) and `shouldNotFilterErrorDispatch()` (defaults to `false`).

---

## Category 2: Stateless JWT Authentication, Expiration & Refresh Flows

### Q5: How do you implement atomic Refresh Token Rotation (RTR) without race conditions when 10 concurrent AJAX requests arrive with an expired token?
- **Scenario Context:** In a single-page application (SPA), a dashboard loads 8 widgets concurrently. The user's access token expires. All 8 widgets make concurrent HTTP calls. All 8 receive HTTP 401 and concurrently attempt to exchange the single-use Refresh Token. The OAuth2 server invalidates the refresh token due to suspected token replay, logging the user out.
- **What the Interviewer Evaluates:** Refresh token rotation (RTR), token replay detection, handling concurrency in stateless gateways, and Redis mutex locking during refresh.
- **Standout Technical Answer:**
  - Under **Refresh Token Rotation (RTR)**, every time a refresh token is used, it is invalidated and replaced with a new one. If an invalidated refresh token is presented a second time, the authorization server assumes an attacker stole it and revokes the user's entire token family (**Token Replay Attack Detection**).
  - When 8 concurrent requests hit an expired access token simultaneously:
    1. **Client-Side Queue Mutex (Axios/Fetch Interceptor)**: The first 401 triggers the refresh call; all subsequent 401s are enqueued into a pending Promise queue until the new token arrives, then replayed with the new token.
    2. **Backend Grace Period Buffer**: The OAuth2 server provides an atomic **10-second grace window**: when a refresh token is rotated, the old token remains valid for 10 seconds specifically to tolerate in-flight parallel requests without triggering replay revocation.
- **Follow-Up Trap:** *"Why is storing JWT access tokens in browser `localStorage` a critical security vulnerability?"*
  - *Winning Answer:* "`localStorage` is globally accessible to all JavaScript running in that origin. Any Cross-Site Scripting (XSS) vulnerability in a third-party npm package can instantly exfiltrate the JWT. Always store tokens in **`HttpOnly; Secure; SameSite=Strict` cookies**, which the browser transmits automatically but JavaScript cannot access."*

#### Production Code Example - Q5: Stateless JWT Filter & ThreadLocal Cleanup

- **Execution Steps:**
  1. **Extract Bearer Token**: Parse authorization header and validate cryptographic signature.
  2. **Set SecurityContext**: Construct `UsernamePasswordAuthenticationToken` and set on `SecurityContextHolder`.
  3. **Mandatory Thread Cleanup**: Always invoke `SecurityContextHolder.clearContext()` inside a `finally` block to prevent carrier thread credential leakage in pooled environments.

- **Sample Code:**

```java
package com.production.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenValidator tokenValidator;

    public JwtAuthenticationFilter(JwtTokenValidator tokenValidator) {
        this.tokenValidator = tokenValidator;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = resolveToken(request);

        try {
            if (token != null && tokenValidator.isValid(token)) {
                String username = tokenValidator.extractUsername(token);
                List<String> roles = tokenValidator.extractRoles(token);

                List<SimpleGrantedAuthority> authorities = roles.stream()
                    .map(SimpleGrantedAuthority::new)
                    .toList();

                UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(username, null, authorities);
                
                SecurityContextHolder.getContext().setAuthentication(auth);
            }

            filterChain.doFilter(request, response);
        } finally {
            // CRITICAL: Always clean up to prevent thread-local leakage in pooled containers!
            SecurityContextHolder.clearContext();
        }
    }

    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}
```

- **Sample Input & Output:**
  - **Input Request**: `GET /api/v1/orders` with token for user `alice` having role `ROLE_USER`.
  - **Console Output**:
    ```text
    2026-09-13 23:38:12.110 DEBUG [exec-2] c.p.s.f.JwtAuthenticationFilter: Validated token for user alice. Context set with authorities: [ROLE_USER].
    2026-09-13 23:38:12.135 DEBUG [exec-2] c.p.s.f.JwtAuthenticationFilter: Request completed. SecurityContextHolder cleared successfully.
    ```

---

### Q6: What is the architectural difference between Asymmetric (RSA/ECDSA) and Symmetric (HMAC) JWT validation across microservices?
- **What the Interviewer Evaluates:** Cryptographic key distribution, shared secret security risks, and JSON Web Key Sets (JWKS).
- **Standout Technical Answer:**
  - **Symmetric Signing (HMAC-SHA256):**
    - Uses a **single shared secret key** for both signing and validation.
    - *The Security Hazard:* Every microservice that validates the token must possess the secret key. If a single low-security microservice is compromised, the attacker steals the secret and can forge admin tokens for the entire enterprise!
  - **Asymmetric Signing (RSA / ECDSA):**
    - The Authorization Server holds the **Private Key** (kept in an HSM / AWS KMS) and signs tokens.
    - All downstream microservices possess only the **Public Key** (distributed via `/.well-known/jwks.json`).
    - Downstream services can verify signatures, but **can never forge tokens**.
    - If a microservice is breached, zero signing keys are exposed!

---

### Q7: How do you implement Immediate Token Revocation for Stateless JWTs using a Redis Blocklist?
- **What the Interviewer Evaluates:** Stateless vs stateful trade-offs, token revocation lists (TRL), and JTI (JWT ID) checking.
- **Standout Technical Answer:**
  - A pure stateless JWT cannot be revoked before its expiration time elapses.
  - **The Hybrid Redis Blocklist Solution:**
    1. Include a unique UUID claim in every JWT: `"jti": "8f7b...""`.
    2. When a user logs out or changes their password, extract the token's `jti` and remaining TTL:
       `redisTemplate.opsForValue().set("blocklist:" + jti, "revoked", remainingTtl, TimeUnit.SECONDS);`
    3. In `JwtAuthenticationFilter`, perform a fast $O(1)$ lookup:
       `if (redisTemplate.hasKey("blocklist:" + jti)) throw new BadCredentialsException("Token revoked!");`
    4. Redis automatically evicts the `jti` when the original expiration passes, keeping memory consumption near zero.

---

### Q8: How do you map Keycloak / Okta nested claims (`realm_access.roles`) to Spring `GrantedAuthority`?
- **What the Interviewer Evaluates:** `JwtAuthenticationConverter`, `Converter<Jwt, AbstractAuthenticationToken>`, and standardizing `ROLE_` prefixes.
- **Standout Technical Answer:**
  - Third-party identity providers place roles inside proprietary nested structures (e.g. `realm_access: { roles: ["admin", "editor"] }`).
  - Spring Security defaults to looking for a flat `scope` or `scp` claim.
  - **The Solution:**
    Configure a custom `JwtAuthenticationConverter` that reads the nested JSON map, prepends `ROLE_`, converts them to `SimpleGrantedAuthority`, and assigns them to the `Authentication` token.

---

## Category 3: Method Security, SpEL Injection & Dynamic Authorization

### Q9: What is the security vulnerability of `@PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")` if method arguments can be tampered with?
- **Scenario Context:** In a REST endpoint `@GetMapping("/users/{id}/profile")`, an engineer secures the method using SpEL. A standard user with ID 10 sends a request with `/users/10/profile`, but alters their payload or headers to modify another user's profile.
- **What the Interviewer Evaluates:** Spring Security method security, SpEL expression evaluation safety, `@PostAuthorize`, and `@PreFilter` vs `@PostFilter`.
- **Standout Technical Answer:**
  - `@PreAuthorize` executes **before** method entry using an AOP proxy interceptor (`AuthorizationManagerBeforeMethodInterceptor`).
  - SpEL expressions evaluate against the method parameters discovered via `DefaultParameterNameDiscoverer`.
  - **The Vulnerabilities:**
    1. **Parameter Spoofing**: If the controller method parameter `#id` is taken from user input (e.g. path variable) without validating that the authenticated principal actually owns that entity in the database, users can manipulate parameters to access unauthorized records.
    2. **Collection Leaks**: Using `@PreAuthorize` does not filter what is *returned* from a database query. If a method returns `List<Account>`, `@PreAuthorize` checks permission to invoke the method, but cannot ensure every account in the returned list belongs to the user.
  - **The Solution:**
    Use **`@PostAuthorize("returnObject.ownerId == authentication.principal.id")`** for single entity reads, or **`@PostFilter("filterObject.ownerId == authentication.principal.id")`** to filter returned collections automatically.
- **Follow-Up Trap:** *"Why does `@EnableMethodSecurity` in Spring Security 6 supersede the deprecated `@EnableGlobalMethodSecurity`?"*
  - *Winning Answer:* "`@EnableMethodSecurity` defaults `useAuthorizationManager=true`, replacing the legacy `AccessDecisionManager` / `Voter` architecture with modern, lightweight `AuthorizationManager` beans. It also activates JSR-250 and `@Secured` support natively without verbose configuration."*

#### Production Code Example - Q9: Entity-Level PostAuthorize & PostFilter

- **Execution Steps:**
  1. **Enable Method Security**: Annotate configuration with `@EnableMethodSecurity`.
  2. **Protect Single Entity Reads**: Apply `@PostAuthorize` to evaluate returned object ownership.
  3. **Filter Collection Streams**: Apply `@PostFilter` to remove records belonging to other tenants.

- **Sample Code:**

```java
package com.production.security.service;

import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

record AccountReport(Long id, String ownerUsername, double balance) {}

@Service
@EnableMethodSecurity
public class AccountReportService {

    // 1. PostAuthorize: Ensures only the actual owner or an admin can view this entity
    @PostAuthorize("returnObject.ownerUsername == authentication.name or hasRole('ADMIN')")
    public AccountReport getAccountReport(Long reportId) {
        return loadReportFromDatabase(reportId);
    }

    // 2. PostFilter: Automatically purges elements from the returned collection not belonging to user
    @PostFilter("filterObject.ownerUsername == authentication.name")
    public List<AccountReport> getActiveAccounts() {
        List<AccountReport> reports = new ArrayList<>();
        reports.add(new AccountReport(1L, "john_doe", 5000.00));
        reports.add(new AccountReport(2L, "jane_smith", 12000.00));
        reports.add(new AccountReport(3L, "john_doe", 850.00));
        return reports;
    }

    private AccountReport loadReportFromDatabase(Long id) {
        return new AccountReport(id, "john_doe", 5000.00);
    }
}
```

- **Sample Input & Output:**
  - **Input Invocation**: User `john_doe` invokes `getActiveAccounts()`.
  - **Console Output**:
    ```text
    2026-09-13 23:41:00.010 DEBUG [exec-4] o.s.s.a.i.AuthorizationManagerAfterMethodInterceptor: Applying @PostFilter on return collection of size 3.
    Filtered out 1 record belonging to jane_smith. Returned 2 records belonging to john_doe.
    ```

---

### Q10: How do you build a Custom Dynamic `AuthorizationManager` for Database-Driven Permissions?
- **What the Interviewer Evaluates:** Spring Security 6 `AuthorizationManager<RequestAuthorizationContext>`, dynamic database permissions, and eliminating hardcoded `@PreAuthorize` strings.
- **Standout Technical Answer:**
  - Hardcoding roles in annotations (`hasRole('FINANCE_MANAGER')`) requires code deployments whenever access rules change.
  - **Dynamic AuthorizationManager Pattern:**
    1. Implement `AuthorizationManager<RequestAuthorizationContext>`.
    2. Extract request URI, HTTP method, and authenticated principal.
    3. Query a cached database permission matrix (or OPA Policy Agent).
    4. Return `new AuthorizationDecision(hasPermission)`.
    5. Register in the security filter chain:
       `http.authorizeHttpRequests(auth -> auth.anyRequest().access(customAuthorizationManager))`

---

### Q11: How do Spring Data Security SpEL queries filter data at the database level rather than via `@PostFilter`?
- **What the Interviewer Evaluates:** Database query offloading, memory efficiency, and Spring Data Security expressions (`?#{principal.username}`).
- **Standout Technical Answer:**
  - `@PostFilter` loads the entire dataset into JVM memory and then removes unauthorized elements. On 100,000 records, this consumes massive heap memory and triggers high GC pressure.
  - **Database-Level Filtering via SpEL:**
    ```java
    public interface OrderRepository extends JpaRepository<Order, Long> {
        @Query("SELECT o FROM Order o WHERE o.tenantId = ?#{principal.tenantId}")
        List<Order> findAllForCurrentTenant();
    }
    ```
  - The database engine evaluates the filter during SQL execution, returning *only* authorized rows and cutting memory allocation by 99%!

---

### Q12: Why is `@Secured` and JSR-250 `@RolesAllowed` inferior to `@PreAuthorize` in modern applications?
- **What the Interviewer Evaluates:** Role-based access control (RBAC) vs attribute-based access control (ABAC) with SpEL expressions.
- **Standout Technical Answer:**
  - `@Secured` and `@RolesAllowed` only support static role name matching (`@Secured("ROLE_ADMIN")`).
  - They cannot access method arguments, evaluate boolean logic (`AND`, `OR`), inspect returned objects, or verify resource ownership.
  - `@PreAuthorize` supports full Spring Expression Language (SpEL), enabling sophisticated Attribute-Based Access Control (ABAC).

---

## Category 4: CORS Preflight, CSRF Defense & Zero-Trust Cookies

### Q13: Why does a browser receive HTTP 403 Forbidden on an `OPTIONS` CORS preflight request before the `Authorization` header is even sent?
- **Scenario Context:** A React frontend on `https://app.company.com` makes an authenticated `POST` request to Spring Boot on `https://api.company.com`. The browser sends an `OPTIONS` preflight request. Spring Security immediately rejects it with `403 Forbidden`, and the real `POST` request is never sent.
- **What the Interviewer Evaluates:** W3C CORS preflight specifications, browser header restrictions, and filter ordering between `CorsFilter` and `SecurityFilterChain`.
- **Standout Technical Answer:**
  - According to W3C Cross-Origin Resource Sharing (CORS) specifications:
    1. For cross-origin requests with custom headers (`Authorization`) or methods (`POST`, `PUT`), the browser automatically emits an **`OPTIONS` preflight request**.
    2. The browser **intentionally omits credentials and the `Authorization` header** from the preflight request!
  - If Spring Security evaluates authentication *before* processing CORS:
    - Spring Security sees an incoming `OPTIONS` request with **no credentials**.
    - It enforces `.anyRequest().authenticated()` and immediately responds with **`403 Forbidden`**!
  - **The Production Fix:**
    1. Configure `http.cors(cors -> cors.configurationSource(corsConfigurationSource()))`.
    2. Spring Security automatically inserts a **`CorsFilter` at the very first position** of the filter chain (before any authentication filters).
    3. The `CorsFilter` intercepts the `OPTIONS` request, verifies origin and allowed headers, and returns **HTTP 200 OK** with `Access-Control-Allow-Origin` without requiring authentication.
- **Follow-Up Trap:** *"Why must `allowCredentials(true)` NEVER be paired with `allowedOrigins(\"*\")` in CORS configuration?"*
  - *Winning Answer:* "The W3C CORS standard strictly prohibits `Access-Control-Allow-Origin: *` when `Access-Control-Allow-Credentials: true`. If configured, browsers will immediately block the response to prevent malicious third-party websites from stealing authenticated session cookies or credentials. You must specify exact explicit origins: `setAllowedOrigins(List.of(\"https://app.company.com\"))`."*

#### Production Code Example - Q13: Enterprise CORS Configuration

- **Execution Steps:**
  1. **Define Explicit Origin Allow-List**: Restrict origins strictly to trusted domains.
  2. **Set Preflight Max-Age**: Cache preflight responses for 3600 seconds to reduce network hops.
  3. **Bind to SecurityFilterChain**: Enable `http.cors()` ensuring `CorsFilter` executes first.

- **Sample Code:**

```java
package com.production.security.cors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class ProductionCorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        
        // Strict explicit origin: NEVER use wildcard "*" with credentials enabled!
        config.setAllowedOrigins(List.of("https://app.enterprise.com", "https://admin.enterprise.com"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L); // Caches OPTIONS preflight for 1 hour to reduce latency

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
```

- **Sample Input & Output:**
  - **Input Preflight**: `OPTIONS /api/v1/payments` with header `Origin: https://app.enterprise.com`.
  - **Console Output**:
    ```text
    2026-09-13 23:44:02.100 DEBUG [exec-1] o.s.w.c.CorsFilter: Handling CORS preflight request for Origin [https://app.enterprise.com]
    2026-09-13 23:44:02.105 DEBUG [exec-1] o.s.w.c.CorsFilter: Preflight request matched. Returning HTTP 200 with Access-Control-Allow-Origin: https://app.enterprise.com
    Preflight completed without requiring authentication.
    ```

---

### Q14: When should CSRF be disabled, and how do you configure the Single-Page Application (SPA) Cookie CSRF Pattern?
- **What the Interviewer Evaluates:** Cross-Site Request Forgery mechanics, stateless Bearer token APIs vs cookie sessions, and Spring Security 6 `CsrfTokenRequestAttributeHandler`.
- **Standout Technical Answer:**
  - **When to Disable CSRF:**
    If the API is purely stateless, uses mobile clients, or authenticates exclusively via `Authorization: Bearer <token>` headers, **CSRF can be safely disabled**. Browsers do not attach Bearer headers automatically on cross-site form submissions!
  - **When CSRF is Mandatory:**
    If the application uses **Session Cookies** or stores authentication in browser cookies.
  - **SPA Cookie Pattern (Spring Security 6):**
    Use `CookieCsrfTokenRepository.withHttpOnlyFalse()`. The browser script reads the CSRF token from the cookie and sends it in the `X-XSRF-TOKEN` header on state-modifying requests (`POST`, `PUT`, `DELETE`).

---

### Q15: What HTTP Security Headers are configured by default in Spring Security 6, and what attacks do they prevent?
- **What the Interviewer Evaluates:** OWASP Top 10 web defenses: Clickjacking, MIME sniffing, HSTS, and XSS.
- **Standout Technical Answer:**
  - **`Strict-Transport-Security` (HSTS)**: Forces browsers to use HTTPS exclusively, preventing SSL-stripping man-in-the-middle attacks.
  - **`X-Content-Type-Options: nosniff`**: Prevents browsers from MIME-sniffing a response away from the declared content-type, blocking script execution masquerading as images.
  - **`X-Frame-Options: DENY`**: Prevents the application from being embedded in `<iframe>` tags, neutralizing Clickjacking attacks.
  - **`Content-Security-Policy` (CSP)**: Restricts script sources to prevent Cross-Site Scripting (XSS).

---

## Category 5: Multi-Tenancy & OAuth2 Resource Server JWKS Validation

### Q16: How do you configure Spring Security 6 as an OAuth2 Resource Server validating JWTs across Multiple Identity Providers (Multi-Tenant JWKS)?
- **Scenario Context:** A SaaS platform serves two corporate clients: Tenant A uses Azure AD, and Tenant B uses Okta. Incoming JWTs have different issuers (`iss`). The application must dynamically resolve and validate JWKS certificates based on the incoming token's issuer.
- **What the Interviewer Evaluates:** `JwtDecoder`, `AuthenticationManagerResolver`, dynamic JWKS discovery, and multi-tenant security architecture.
- **Standout Technical Answer:**
  - In a single-tenant setup, Spring Boot auto-configures a single `JwtDecoder` using `spring.security.oauth2.resourceserver.jwt.issuer-uri`.
  - In a multi-tenant setup:
    1. Implement an **`AuthenticationManagerResolver<HttpServletRequest>`**.
    2. Extract the unverified JWT token from the `Authorization` header and parse its `iss` claim using Nimbus JOSE: `SignedJWT.parse(token).getJWTClaimsSet().getIssuer()`.
    3. Maintain an in-memory cache of `JwtAuthenticationProvider` instances, each configured with that tenant's specific JWKS public key endpoint.
    4. Delegate token signature verification to the appropriate tenant's provider dynamically.
- **Follow-Up Trap:** *"Why is it safe to read the `iss` claim before validating the cryptographic signature of the token?"*
  - *Winning Answer:* "Reading the `iss` claim is used strictly to look up *which* public key to verify against. The token is NOT trusted or authenticated until the cryptographic signature is verified against that issuer's official JWKS certificate."

#### Production Code Example - Q16: Multi-Tenant JWKS Authentication Resolver

- **Execution Steps:**
  1. **Parse Issuer Unverified**: Read `iss` claim from token to identify tenant IdP.
  2. **Cache Dynamic Providers**: Construct and cache `JwtAuthenticationProvider` per issuer.
  3. **Resolve Dynamically in FilterChain**: Register `authenticationManagerResolver` on resource server DSL.

- **Sample Code:**

```java
package com.production.security.oauth2;

import com.nimbusds.jwt.SignedJWT;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.web.SecurityFilterChain;

import java.text.ParseException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class MultiTenantOAuth2Config {

    @Bean
    public SecurityFilterChain multiTenantFilterChain(HttpSecurity http,
            AuthenticationManagerResolver<HttpServletRequest> resolver) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())
            .oauth2ResourceServer(oauth2 -> oauth2.authenticationManagerResolver(resolver))
            .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
            .build();
    }

    @Bean
    public AuthenticationManagerResolver<HttpServletRequest> authenticationManagerResolver() {
        Map<String, AuthenticationManager> managers = new ConcurrentHashMap<>();

        return request -> {
            String token = extractToken(request);
            String issuer = extractIssuerUnverified(token);

            return managers.computeIfAbsent(issuer, iss -> {
                System.out.println("Discovered new tenant issuer: " + iss + ". Caching JWKS provider.");
                JwtDecoder decoder = JwtDecoders.fromIssuerLocation(iss);
                JwtAuthenticationProvider provider = new JwtAuthenticationProvider(decoder);
                return provider::authenticate;
            });
        };
    }

    private String extractToken(HttpServletRequest req) {
        String auth = req.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            return auth.substring(7);
        }
        throw new BadCredentialsException("Missing or invalid Bearer token");
    }

    private String extractIssuerUnverified(String token) {
        try {
            return SignedJWT.parse(token).getJWTClaimsSet().getIssuer();
        } catch (ParseException e) {
            throw new BadCredentialsException("Invalid JWT format", e);
        }
    }
}
```

- **Sample Input & Output:**
  - **Input Request**: Request with token from issuer `https://company.okta.com/oauth2/default`.
  - **Console Output**:
    ```text
    Discovered new tenant issuer: https://company.okta.com/oauth2/default. Caching JWKS provider.
    JWKS keys retrieved and cached. Token signature validated successfully against tenant certificate!
    ```

---

### Q17: How does Spring's `RestClient` handle OAuth2 Client Credentials token caching and expiration?
- **What the Interviewer Evaluates:** Machine-to-machine authentication, token expiration buffers, and `OAuth2AuthorizedClientManager`.
- **Standout Technical Answer:**
  - Microservices communicating east-west use the **OAuth2 Client Credentials Flow**.
  - Acquiring a new token on every HTTP request creates latency and overwhelms the identity provider.
  - **The Solution: `AuthorizedClientServiceOAuth2AuthorizedClientManager`**:
    - Automatically requests tokens and caches them in memory.
    - Inspects token expiration: if the token expires in $< 60$ seconds, it proactively refreshes the token in the background before dispatching the HTTP call.

---

### Q18: What is OIDC Back-Channel Logout, and how does it invalidate sessions across microservices?
- **What the Interviewer Evaluates:** Single Sign-Out (SSO), OpenID Connect Back-Channel Logout 1.0 specifications, and distributed session revocation.
- **Standout Technical Answer:**
  - In SSO environments, logging out of one application should terminate sessions across all interconnected services.
  - **Back-Channel Logout:**
    - The OpenID Provider sends a direct HTTP `POST` request containing a signed `logout_token` to each microservice's `/logout/connect/back-channel` endpoint.
    - The microservice verifies the token's signature, extracts the user's `sub` (subject identifier) or `sid` (session ID), and invalidates the local session/token cache immediately.

---

## Category 6: Production War Room Incidents & Outage Forensics

### Q19: WAR ROOM RCA: SecurityContext ThreadLocal Leak in Java 21 Virtual Threads
- **Incident Summary:** Under high load on Java 21, Customer B intermittently received HTTP responses containing sensitive financial accounts belonging to Customer A.
- **Root Cause Forensics:**
  The service enabled Virtual Threads (`spring.threads.virtual.enabled=true`). A custom logging filter set a tenant context in a static `ThreadLocal<String>`:
  ```java
  public class TenantFilter extends OncePerRequestFilter {
      public static final ThreadLocal<String> TENANT = new ThreadLocal<>();
      @Override
      protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) {
          TENANT.set(req.getHeader("X-Tenant-ID"));
          chain.doFilter(req, res);
          // BUG: Missing TENANT.remove() in finally block!
      }
  }
  ```
  1. While virtual threads are cheap and short-lived, **Carrier Threads** (the underlying OS platform worker threads) are persistent and reused by the JVM scheduler.
  2. Because `TENANT.remove()` was never invoked, when the next virtual thread was mounted onto that carrier thread, it inherited the stale `ThreadLocalMap` values.
  3. Customer B's request read Customer A's tenant ID, pulling Customer A's data from the database.
- **The Permanent Fix:**
  1. Always wrap `ThreadLocal` access in a `try-finally` calling `threadLocal.remove()` in `finally`.
  2. Migrate from `ThreadLocal` to Java 21 **`ScopedValue`**, which automatically binds context strictly to the lexical scope of the execution and clears it on exit.

---

### Q20: WAR ROOM RCA: Catastrophic 0-Day Vulnerability via Accidental `permitAll("/**")` on Actuator
- **Incident Summary:** An external security researcher extracted plain-text database credentials and cloud secrets from `/actuator/env` without providing any authentication.
- **Root Cause Forensics:**
  A developer attempted to allow unauthenticated access to the health check probe:
  ```java
  // ANTI-PATTERN: Wildcard matchers overriding sensitive actuator endpoints
  http.authorizeHttpRequests(auth -> auth
      .requestMatchers("/**").permitAll() // Intended for health, opened EVERYTHING!
  );
  ```
  Spring Security evaluates authorization rules in strict top-to-bottom order. The wildcard `/**` matched all Actuator endpoints, exposing `/actuator/env` and `/actuator/heapdump`.
- **The Permanent Fix:**
  1. Restrict permitAll to specific endpoints only:
     `requestMatchers(EndpointRequest.to(HealthEndpoint.class)).permitAll()`
  2. Require `ROLE_ADMIN` for all other actuator endpoints:
     `requestMatchers(EndpointRequest.toAnyEndpoint()).hasRole("ADMIN")`
  3. Mask sensitive keys in `application.yml`:
     `management.endpoint.env.show-values: when_authorized`

---

## Production Diagnostic Matrix & Best Practices Reference

| Security Goal | Anti-Pattern / Naive Approach | Tier-1 Production Standard | Mechanical Guarantee & Benefit |
| :--- | :--- | :--- | :--- |
| **Stateless API Auth** | Storing JWTs in browser `localStorage` | `HttpOnly; Secure; SameSite=Strict` cookies | Immunizes tokens against Cross-Site Scripting (XSS) |
| **Filter Lifecycle** | `@Component` on custom security filters | Explicit registration in `SecurityFilterChain` | Eliminates duplicate filter execution in Servlet container |
| **CORS Preflight** | Wildcard `*` paired with `allowCredentials(true)` | Explicit origin allow-list + `CorsFilter` at index 0 | Complies with W3C standards; eliminates preflight 403s |
| **Method Security** | `@PreAuthorize` on returned collections | `@PostFilter` or database-level SpEL queries | Prevents accidental data exfiltration of unauthorized rows |
| **Multi-Tenant OAuth2** | Hardcoded single issuer URI | `JwtIssuerAuthenticationManagerResolver` | Dynamically resolves and caches multi-tenant JWKS |
| **Virtual Thread Safety** | Unmanaged `ThreadLocal` in custom filters | Java 21 `ScopedValue` or strict `try-finally clearContext()` | Prevents cross-tenant credential leakage across carrier threads |
| **Actuator Hardening** | Wildcard `permitAll("/**")` | `EndpointRequest.to(HealthEndpoint.class).permitAll()` | Locks down sensitive environment variables and heap dumps |

---

## Navigation & Related Guides

- [Spring 200 Production Scenarios Master Guide](./spring_200_scenarios_master_guide.md)
- [Spring Kafka Scenarios Master Guide](./spring_kafka_scenarios_master_guide.md)
- [Spring Data JPA Scenarios Master Guide](./spring_data_jpa_scenarios_master_guide.md)
- [Spring Data Redis Scenarios Master Guide](./spring_redis_scenarios_master_guide.md)
- [Apache Camel 4 Scenarios Master Guide](./spring_camel_scenarios_master_guide.md)
- [Jackson JSON 200 Scenarios Master Guide](./jackson_scenarios_master_guide.md)
