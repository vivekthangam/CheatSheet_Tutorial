[🏠 Back to Home](README.md) | [🛡️ Spring Security Master Guide](spring_security.md) | [🍃 Spring Boot Master Guide](spring_master_guide.md)

# 🛡️ Spring Security 6 & OAuth2: 50+ Real-World Production Interview Scenarios Master Guide

[![Spring Security](https://img.shields.io/badge/Spring%20Security-6.3%2B-green.svg?style=for-the-badge&logo=springsecurity)](https://spring.io/projects/spring-security)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3%2B-brightgreen.svg?style=for-the-badge&logo=springboot)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17%20%2F%2021%20LTS-orange.svg?style=for-the-badge&logo=openjdk)](https://www.oracle.com/java/)
[![Level](https://img.shields.io/badge/Tier-1%20Panels-Staff%20%2F%20Principal-red.svg?style=for-the-badge)](https://github.com/)

An exhaustive, battle-tested compilation of **real-world production interview scenarios** covering Spring Security 6, the component-based Lambda DSL, `SecurityFilterChain` internals, stateless JWT authentication, OAuth2/OIDC JWKS resource servers, `@PreAuthorize` method security, CORS preflight 403 bypasses, and Virtual Thread `ThreadLocal` security context leaks.

Every scenario strictly follows the **5-Part Tier-1 Product Engineering Format**:
1. **Exact Question Asked by Tier-1 Product Panels (with detailed scenario context)**
2. **What the Interviewer Evaluates Under the Surface (mental criteria, low-level runtime knowledge)**
3. **Standout Technical Answer (deep runtime mechanics, low-level HTTP/filter details, zero fluff)**
4. **Follow-Up Trap Question & Winning Answer (catching surface memorizers)**
5. **Production Sample Code with Detailed Technical Explanation & Pitfall Avoidance**

---

## 📑 Category Navigation

- [Category 1: SecurityFilterChain, Filter Ordering & Servlet Architecture (Q1 – Q10)](#category-1-securityfilterchain-filter-ordering--servlet-architecture)
- [Category 2: Stateless JWT Authentication, Expiration & Refresh Flows (Q11 – Q20)](#category-2-stateless-jwt-authentication-expiration--refresh-flows)
- [Category 3: Method Security, SpEL Injection & PostFilter (Q21 – Q30)](#category-3-method-security-spel-injection--postfilter)
- [Category 4: CORS Preflight, CSRF Defense & Zero-Trust Cookies (Q31 – Q40)](#category-4-cors-preflight-csrf-defense--zero-trust-cookies)
- [Category 5: Multi-Tenancy & OAuth2 Resource Server JWKS Validation (Q41 – Q50)](#category-5-multi-tenancy--oauth2-resource-server-jwks-validation)
- [Category 6: Production War Room Incidents & Outage Forensics (Q51 – Q60)](#category-6-production-war-room-incidents--outage-forensics)

---

# Category 1: SecurityFilterChain, Filter Ordering & Servlet Architecture

### Q1: How does `DelegatingFilterProxy` bridge the Servlet Container and the Spring `ApplicationContext`, and why does filter ordering cause unauthenticated requests to bypass security?
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
- **Production Sample Code & Walkthrough:**
```java
@Configuration
@EnableWebSecurity
public class ModernSecurityFilterChainConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/public/**", "/actuator/health").permitAll()
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            // Add custom JWT filter BEFORE the standard username/password filter
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }

    // Prevents Spring Boot from registering jwtFilter twice in Tomcat!
    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtFilterRegistration(JwtAuthenticationFilter filter) {
        FilterRegistrationBean<JwtAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false); // Disables auto-registration in root Servlet container
        return registration;
    }
}
```

---

# Category 2: Stateless JWT Authentication, Expiration & Refresh Flows

### Q2: How do you implement safe, atomic Token Refresh without race conditions when 10 concurrent AJAX requests arrive with an expired Access Token?
- **Scenario Context:** In a single-page application (SPA), a dashboard loads 8 widgets concurrently. The user's access token expires. All 8 widgets make concurrent HTTP calls. All 8 receive HTTP 401 and concurrently attempt to exchange the single-use Refresh Token. The OAuth2 server invalidates the refresh token due to suspected token replay, logging the user out.
- **What the Interviewer Evaluates:** Refresh token rotation (RTR), token replay detection, handling concurrency in stateless gateways, and Redis mutex locking during refresh.
- **Standout Technical Answer:**
  - Under **Refresh Token Rotation (RTR)**, every time a refresh token is used, it is invalidated and replaced with a new one. If an invalidated refresh token is presented a second time, the authorization server assumes an attacker stole it and revokes the user's entire token family (**Token Replay Attack Detection**).
  - When 8 concurrent requests hit an expired access token simultaneously:
    1. **Client-Side Queue Mutex (Axios/Fetch Interceptor)**: The first 401 triggers the refresh call; all subsequent 401s are enqueued into a pending Promise queue until the new token arrives, then replayed with the new token.
    2. **Backend Grace Period Buffer**: The OAuth2 server provides an atomic **10-second grace window**: when a refresh token is rotated, the old token remains valid for 10 seconds specifically to tolerate in-flight parallel requests without triggering replay revocation.
- **Follow-Up Trap:** *"Why is storing JWT access tokens in browser `localStorage` a critical security vulnerability?"*
  - *Winning Answer:* "`localStorage` is globally accessible to all JavaScript running in that origin. Any Cross-Site Scripting (XSS) vulnerability in a third-party npm package can instantly exfiltrate the JWT. Always store tokens in **`HttpOnly; Secure; SameSite=Strict` cookies**, which the browser transmits automatically but JavaScript cannot access."*
- **Production Sample Code & Walkthrough:**
```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = resolveToken(request);

        if (token != null && tokenProvider.validateToken(token)) {
            Authentication auth = tokenProvider.getAuthentication(token);
            // Sets context on the current worker thread
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        try {
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

---

# Category 3: Method Security, SpEL Injection & PostFilter

### Q3: What is the security vulnerability of `@PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")` if method arguments can be tampered with?
- **Scenario Context:** In a REST endpoint `@GetMapping("/users/{id}/profile")`, an engineer secures the method using SpEL. A standard user with ID 10 sends a request with `/users/10/profile`, but alters their payload or headers to modify another user's profile.
- **What the Interviewer Evaluates:** Spring Security method security, SpEL expression evaluation safety, `@PostAuthorize`, and `@PreFilter` vs `@PostFilter`.
- **Standout Technical Answer:**
  - `@PreAuthorize` executes **before** method entry using an AOP proxy interceptor (`MethodSecurityInterceptor` or `AuthorizationManagerBeforeMethodInterceptor`).
  - SpEL expressions evaluate against the method parameters discovered via `DefaultParameterNameDiscoverer`.
  - **The Vulnerabilities:**
    1. **Parameter Spoofing**: If the controller method parameter `#id` is taken from user input (e.g. path variable) without validating that the authenticated principal actually owns that entity in the database, users can manipulate parameters to access unauthorized records.
    2. **Collection Leaks**: Using `@PreAuthorize` does not filter what is *returned* from a database query. If a method returns `List<Account>`, `@PreAuthorize` checks permission to invoke the method, but cannot ensure every account in the returned list belongs to the user.
  - **The Solution:**
    Use **`@PostAuthorize("returnObject.ownerId == authentication.principal.id")`** for single entity reads, or **`@PostFilter("filterObject.ownerId == authentication.principal.id")`** to filter returned collections automatically.
- **Follow-Up Trap:** *"Why does `@EnableMethodSecurity` in Spring Security 6 supersede the deprecated `@EnableGlobalMethodSecurity`?"*
  - *Winning Answer:* "`@EnableMethodSecurity` defaults `useAuthorizationManager=true`, replacing the legacy `AccessDecisionManager` / `Voter` architecture with modern, lightweight `AuthorizationManager` beans. It also activates JSR-250 and `@Secured` support natively without verbose configuration."*
- **Production Sample Code & Walkthrough:**
```java
@Service
public class AccountReportService {

    // Ensures only the account owner or an admin can access this specific entity
    @PostAuthorize("returnObject.ownerUsername == authentication.name or hasRole('ADMIN')")
    public AccountReport getAccountReport(Long reportId) {
        return loadReportFromDatabase(reportId);
    }

    // Automatically filters out any accounts not belonging to the caller
    @PostFilter("filterObject.ownerUsername == authentication.name")
    public List<AccountReport> getActiveAccounts() {
        return loadAllActiveReports();
    }

    private AccountReport loadReportFromDatabase(Long id) {
        return new AccountReport(id, "john_doe", 5000.00);
    }

    private List<AccountReport> loadAllActiveReports() {
        return List.of(
            new AccountReport(1L, "john_doe", 5000.00),
            new AccountReport(2L, "jane_smith", 12000.00)
        );
    }
}
```

---

# Category 4: CORS Preflight, CSRF Defense & Zero-Trust Cookies

### Q4: Why does a browser receive HTTP 403 Forbidden on an `OPTIONS` CORS preflight request before the `Authorization` header is even sent?
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
- **Production Sample Code & Walkthrough:**
```java
@Configuration
public class ProductionCorsSecurityConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // Explicit origin: NEVER use "*" with allowCredentials(true)!
        config.setAllowedOrigins(List.of("https://app.company.com"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L); // Caches preflight response for 1 hour

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
```

---

# Category 5: Multi-Tenancy & OAuth2 Resource Server JWKS Validation

### Q6: How do you configure Spring Security 6 as an OAuth2 Resource Server validating JWTs across Multiple Identity Providers (Multi-Tenant JWKS)?
- **Scenario Context:** A SaaS platform serves two corporate clients: Tenant A uses Azure AD, and Tenant B uses Okta. Incoming JWTs have different issuers (`iss`). The application must dynamically resolve and validate JWKS certificates based on the incoming token's issuer.
- **What the Interviewer Evaluates:** `JwtDecoder`, `AuthenticationManagerResolver`, dynamic JWKS discovery, and multi-tenant security architecture.
- **Standout Technical Answer:**
  - In a single-tenant setup, Spring Boot auto-configures a single `JwtDecoder` using `spring.security.oauth2.resourceserver.jwt.issuer-uri`.
  - In a multi-tenant setup:
    1. Implement an **`AuthenticationManagerResolver<HttpServletRequest>`**.
    2. Extract the unverified JWT token from the `Authorization` header and parse its `iss` claim using Nimbus JOSE: `SignedJWT.parse(token).getJWTClaimsSet().getIssuer()`.
    3. Maintain an in-memory cache of `JwtAuthenticationProvider` instances, each configured with that tenant's specific JWKS public key endpoint:
       `https://login.microsoftonline.com/{tenantA}/discovery/v2.0/keys`
       `https://company.okta.com/oauth2/default/v1/keys`
    4. Delegate token signature verification to the appropriate tenant's provider dynamically.
- **Follow-Up Trap:** *"Why is it safe to read the `iss` claim before validating the cryptographic signature of the token?"*
  - *Winning Answer:* "Reading the `iss` claim is used strictly to look up *which* public key to verify against. The token is NOT trusted or authenticated until the cryptographic signature is verified against that issuer's official JWKS certificate."
- **Production Sample Code & Walkthrough:**
```java
@Configuration
public class MultiTenantOAuth2Config {

    @Bean
    public SecurityFilterChain multiTenantFilterChain(HttpSecurity http,
                                                     AuthenticationManagerResolver<HttpServletRequest> resolver) throws Exception {
        return http
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
                JwtDecoder decoder = JwtDecoders.fromIssuerLocation(iss);
                JwtAuthenticationProvider provider = new JwtAuthenticationProvider(decoder);
                return provider::authenticate;
            });
        };
    }

    private String extractToken(HttpServletRequest req) {
        String auth = req.getHeader("Authorization");
        return (auth != null && auth.startsWith("Bearer ")) ? auth.substring(7) : "";
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

---

# Category 6: Production War Room Incidents & Outage Forensics

### Q7: WAR ROOM RCA: SecurityContext ThreadLocal Leak in Java 21 Virtual Threads
- **Severity:** P0 Security Breach (Data leak across unrelated customer tenants)
- **Mean Time to Recovery (MTTR):** 1 hour
- **Symptoms:** Under high load on Java 21, Customer B intermittently loaded sensitive medical records belonging to Customer A.
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

## ⚖️ Spring Security 6 Production Security Matrix

| Security Goal | Production Standard |
| :--- | :--- |
| **Stateless API** | `SessionCreationPolicy.STATELESS` + `csrf().disable()` |
| **CORS Preflight** | `http.cors(...)` configured with explicit origins, never `*` |
| **Safe JWT Storage** | `HttpOnly; Secure; SameSite=Strict` browser cookies |
| **Entity-Level Authorization**| `@PostAuthorize("returnObject.owner == authentication.name")` |
| **Multi-Tenant JWKS** | `JwtIssuerAuthenticationManagerResolver` |
| **Virtual Thread Safety** | Java 21 `ScopedValue` or strict `try-finally clearContext()` |

---
[🏠 Back to Home](README.md) | [🛡️ Spring Security Master Guide](spring_security.md) | [🍃 Spring Boot Master Guide](spring_master_guide.md)
