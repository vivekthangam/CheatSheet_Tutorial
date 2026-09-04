[🏠 Back to Home](README.md)

# 🛡️ Spring Security 6 & OAuth2 / JWT Enterprise Master Guide

A production-grade engineering handbook for securing modern Spring Boot microservices, APIs, and cloud-native applications using **Spring Security 6.x**, **Spring Boot 3.x**, **OAuth 2.0**, and **Stateless JWTs**. Covers `SecurityFilterChain` architecture, custom authentication filters, RBAC vs ABAC, method security, CORS/CSRF configurations, and zero-trust authentication.

---

## 📑 Table of Contents

### Track 1: Junior & Entry-Level Foundations

- [🌱 1. Real-World Mental Model (Airport Security & Boarding Gate)](#1-the-real-world-mental-model-the-international-airport-security--vip-boarding-gate)
- [🧩 2. The 5 Core Building Blocks of Spring Security](#2-the-5-core-building-blocks)
- [💻 3. Beginner Code Walkthrough: Spring Security 6 Configuration](#3-beginner-code-walkthrough-spring-security-6-configuration)
- [💥 4. What Happens When Things Break? (401 vs 403 & CORS)](#4-what-happens-when-things-break-401-vs-403--cors)
- [⚠️ 5. Top 5 Beginner Mistakes in Production](#5-top-5-beginner-mistakes-in-production)
- [🎯 6. Top 10 Junior Interview Questions (With "ELI5" Answers)](#6-top-10-junior-interview-questions-with-explain-like-im-5-answers)

### Track 2: Advanced Architecture & Zero-Trust Engineering

1. [⚙️ 1. Spring Security 6 Architecture & Filter Chain](#️-1-spring-security-6-architecture--filter-chain)
2. [🔑 2. Stateless JWT Authentication Filter Implementation](#-2-stateless-jwt-authentication-filter-implementation)
3. [🛡️ 3. Role-Based Access Control (RBAC) & Method Security](#️-3-role-based-access-control-rbac--method-security)
4. [🌐 4. Production CORS & CSRF Hardening](#-4-production-cors--csrf-hardening)
5. [🎫 5. OAuth2 Resource Server & OpenID Connect (OIDC)](#-5-oauth2-resource-server--openid-connect-oidc)
6. [🏭 6. Production Scenarios & War Room Incident Forensics](#-6-production-scenarios--war-room-incident-forensics)
7. [⚖️ 7. Spring Security 6 Master Cheat Sheet](#️-7-spring-security-6-master-cheat-sheet)

# TRACK 1: THE JUNIOR & ENTRY-LEVEL FOUNDATIONS (ZERO-TO-HERO)

## 1. The Real-World Mental Model (The International Airport Security & VIP Boarding Gate)

### Authentication vs Authorization: What's the Difference?
Imagine going to the airport to catch an international flight:
- **Authentication (Who are you?):**
  - You show your passport and driver's license to the border officer at the security gate.
  - The officer verifies your face matches your passport photo.
  - *Result:* The officer stamps your boarding pass. You are **Authenticated** (your identity is verified).
- **Authorization (What are you allowed to do?):**
  - You try to walk into the First-Class VIP Champagne Lounge or sit in the cockpit of the plane.
  - The flight attendant inspects your ticket: *"Your passport is valid, but your ticket says Economy Class. You cannot enter the Cockpit!"*
  - *Result:* You are **Denied Authorization** (`HTTP 403 Forbidden`).

---

### The Filter Chain: Airport Security Metal Detectors
Before you ever reach your airplane seat (your `@RestController`), you must pass through a strict sequence of security checkpoints (**The `SecurityFilterChain`**):

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
| **`SecurityContextHolder`** | The `ThreadLocal` storage holding the current user's authentication details during a request. | The personal pocket where you keep your boarding pass while walking through the airport. |
| **`GrantedAuthority` / Role** | Specific permissions or roles assigned to the user (e.g. `ROLE_ADMIN`). | The seat assignment and class printed on your ticket (e.g. First Class vs Economy). |
| **JWT (JSON Web Token)** | A digitally signed, tamper-proof token carrying user claims (stateless). | A plastic festival wristband with an un-forgeable holographic seal. |

---

## 3. Beginner Code Walkthrough: Spring Security 6 Configuration

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
public class ModernSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 1. Disable CSRF (Safe for stateless REST APIs using JWT Bearer headers)
            .csrf(csrf -> csrf.disable())
            
            // 2. Stateless session policy (No HTTP sessions created on the server!)
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // 3. Define URL authorization rules
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**", "/public/**").permitAll() // Public
                .requestMatchers("/api/admin/**").hasRole("ADMIN")          // Admin only
                .anyRequest().authenticated()                               // Everything else requires login
            );

        return http.build();
    }
}
```

---

## 4. What Happens When Things Break? (401 vs 403 & CORS)

1. **HTTP 401 Unauthorized vs HTTP 403 Forbidden:**
   - **401 Unauthorized:** *"I don't know who you are. Please provide a valid username/password or JWT token."*
   - **403 Forbidden:** *"I know who you are, but you do NOT have permission to access this page/resource."*
2. **CORS Error (`Access to XMLHttpRequest has been blocked by CORS policy`):**
   The frontend runs on `http://localhost:3000` and the backend runs on `http://localhost:8080`. The browser sends an `OPTIONS` preflight check. If Spring Security does not configure `CorsConfigurationSource`, the preflight fails with 403/401!
3. **Empty `SecurityContext` in `@Async` Threads:**
   `SecurityContextHolder` uses `ThreadLocal` by default. When you call an `@Async` method, Spring runs it on a separate thread where `SecurityContextHolder.getContext().getAuthentication()` returns `null`! **Fix:** Configure `MODE_INHERITABLETHREADLOCAL`.

---

## 5. Top 5 Beginner Mistakes in Production

1. **Confusing `hasRole()` with `hasAuthority()`:** In Spring Security, `hasRole("ADMIN")` automatically checks for the prefix `ROLE_ADMIN` in the database. If your database authority is plain `"ADMIN"`, `hasRole("ADMIN")` returns 403 Forbidden! **Rule:** Use `hasAuthority("ADMIN")` or save roles with the `ROLE_` prefix.
2. **Disabling CSRF on Cookie-Based Apps:** Beginners disable CSRF (`csrf.disable()`) because it throws 403 on POST requests. For stateless JWTs in headers, this is fine. But for apps using session cookies, disabling CSRF leaves users open to bank transfer theft via malicious links!
3. **Hardcoding Secret Keys in Git:** Storing JWT secret keys (`"mySuperSecretKey123"`) inside `application.yml` committed to GitHub. Attackers clone the repo, forge their own Admin JWTs, and take over the system.
4. **Storing Sensitive Data in JWT Payload:** JWT payloads are Base64 encoded, **NOT ENCRYPTED**! Anyone can paste a JWT into `jwt.io` and read the user's email, phone, or internal IDs. Never store passwords or private credit cards inside a JWT.
5. **Using `permitAll()` on Sub-Paths Without Regex/AntMatcher Knowledge:** Writing `.requestMatchers("/admin").permitAll()` when the real endpoint is `/admin/users`. Child paths are NOT matched unless you specify `/admin/**`.

---

## 6. Top 10 Junior Interview Questions (With "Explain Like I'm 5" Answers)

### Q1: What is the difference between Authentication and Authorization?
- **ELI5 Answer:** *"Authentication is showing your ID card to prove who you are. Authorization is checking if your ticket lets you into the VIP roller coaster."*
- **Technical Answer:** *"Authentication verifies the identity of the user (e.g. matching credentials or verifying a signature). Authorization verifies whether the authenticated identity possesses the necessary privileges or roles (`GrantedAuthority`) to perform a specific action on a protected resource."*

### Q2: What is `SecurityContextHolder` and how does it store user data?
- **ELI5 Answer:** *"A pocket inside the worker's jacket where they keep your name badge while helping you carry your bags."*
- **Technical Answer:** *"`SecurityContextHolder` is the central storage class of Spring Security. By default, it uses a `ThreadLocal` strategy to bind the current `SecurityContext` (containing the active `Authentication` object) to the specific operating system thread handling the HTTP request."*

### Q3: What is the difference between HTTP 401 and HTTP 403?
- **ELI5 Answer:** *"401 means 'Who are you? Please show your ticket.' 403 means 'I see your ticket, but you are not allowed in this VIP room!'"*
- **Technical Answer:** *"`401 Unauthorized` indicates missing or invalid authentication credentials (the user is anonymous or token expired). `403 Forbidden` indicates the user is authenticated, but lacks the necessary roles or permissions to access the requested URI."*

### Q4: What is a JWT and what are its three parts?
- **ELI5 Answer:** *"A signed digital passport made of 3 puzzle pieces: Header (type of lock), Payload (your name and seat number), and Signature (the wax seal stamped by the king)."*
- **Technical Answer:** *"A JSON Web Token (JWT) is a compact, URL-safe means of representing claims statelessly between parties. It consists of three parts separated by dots (`.`): (1) **Header** (algorithm and token type), (2) **Payload** (claims such as user ID, roles, expiration), and (3) **Signature** (HMAC/RSA hash verifying the token was not tampered with)."*

### Q5: What is CSRF and why do we disable it in stateless JWT APIs?
- **ELI5 Answer:** *"A trick where a fake letter tricks your bank into sending money because your browser automatically sent your login cookie. If you don't use login cookies, the trick doesn't work!"*
- **Technical Answer:** *"CSRF (Cross-Site Request Forgery) tricks a browser into executing unauthorized commands on a site where the user is currently authenticated via session cookies. In stateless REST APIs where authentication is sent via explicit `Authorization: Bearer <token>` headers, browsers do not attach tokens automatically, making CSRF defense redundant."*

### Q6: How does CORS work and what is a Preflight Request?
- **ELI5 Answer:** *"A browser knocking on the neighbor's door to ask: 'Is it okay if my website from house A borrows sugar from your house B?'"*
- **Technical Answer:** *"CORS (Cross-Origin Resource Sharing) is a browser security mechanism restricting cross-origin HTTP requests. For complex requests (custom headers, PUT/DELETE), the browser sends an HTTP `OPTIONS` preflight request asking the server for permitted origins, headers, and methods before sending the actual request."*

### Q7: What is the difference between `hasRole('ADMIN')` and `hasAuthority('ADMIN')`?
- **ELI5 Answer:** *"`hasRole` automatically adds the word 'ROLE_' to the front of the name tag. `hasAuthority` looks for the exact name tag with zero changes."*
- **Technical Answer:** *"`hasRole('ADMIN')` automatically prepends the `ROLE_` prefix, checking for `ROLE_ADMIN` in the user's `GrantedAuthority` collection. `hasAuthority('ADMIN')` performs an exact string match for `'ADMIN'` with no prefix modification."*

### Q8: What does `@PreAuthorize` do in method security?
- **ELI5 Answer:** *"A security guard standing in front of a specific office door checking badges before letting anyone turn the doorknob."*
- **Technical Answer:** *"`@PreAuthorize` enables method-level security using Spring Expression Language (SpEL), e.g., `@PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")`. It executes before the method runs, blocking unauthorized invocations even within internal service layers."*

### Q9: What is the difference between symmetric and asymmetric encryption in JWT?
- **ELI5 Answer:** *"Symmetric is 1 shared key that both locks and unlocks the treasure chest. Asymmetric is a public padlock that anyone can lock, but only the bank manager has the secret key to open."*
- **Technical Answer:** *"Symmetric (e.g. HS256) uses a single shared secret key for both signing and verifying tokens. Asymmetric (e.g. RS256) uses a Private Key to sign tokens (Auth Server) and a Public Key distributed to microservices to verify tokens, preventing microservices from forging tokens."*

### Q10: Why does `@Async` break Spring Security authentication context?
- **ELI5 Answer:** *"Because the security badge was in Worker 1's pocket. When Worker 1 asks Worker 2 to do a chore, Worker 2 doesn't have the badge in their pocket!"*
- **Technical Answer:** *"Because `SecurityContextHolder` defaults to `ThreadLocal`. When execution branches onto an asynchronous worker thread pool managed by Spring's `@Async`, the new thread does not inherit the parent thread's `ThreadLocal` context, leaving `SecurityContext` empty unless `SecurityContextHolderStrategy` is configured to `MODE_INHERITABLETHREADLOCAL`."*

---

# TRACK 2: ADVANCED ARCHITECTURE & ZERO-TRUST ENGINEERING

## ⚙️ 1. Spring Security 6 Architecture & Filter Chain

> [!IMPORTANT]
> **Spring Security 6 Breaking Changes:**
> - `WebSecurityConfigurerAdapter` is completely removed.
> - `authorizeRequests()` is deprecated; use `authorizeHttpRequests()`.
> - Lambda DSL is strictly required (`cors(Customizer.withDefaults())`, `csrf(AbstractHttpConfigurer::disable)`).

### Maven Dependencies (`pom.xml`)
```xml
<dependencies>
    <!-- Core Security Starter -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>

    <!-- JJWT for High-Speed JWT Generation & Validation -->
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
        <version>0.12.6</version>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-impl</artifactId>
        <version>0.12.6</version>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-jackson</artifactId>
        <version>0.12.6</version>
        <scope>runtime</scope>
    </dependency>
</dependencies>
```

---

## 🔑 2. Stateless JWT Authentication Filter Implementation

### 2.1 JWT Service (Token Parsing & Validation)
```java
package com.example.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {

    private final SecretKey signingKey;

    public JwtService(@Value("${app.security.jwt.secret-key}") String secret) {
        // Secret must be at least 256 bits (32 bytes) for HMAC-SHA256
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = Jwts.parser()
            .verifyWith(signingKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
        return claimsResolver.apply(claims);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
            .subject(userDetails.getUsername())
            .issuedAt(new Date())
            .expiration(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)))
            .signWith(signingKey)
            .compact();
    }
}
```

### 2.2 Custom OncePerRequestFilter
```java
package com.example.security.jwt;

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
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);
        final String userEmail = jwtService.extractUsername(jwt);

        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

            if (jwtService.isTokenValid(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                // Set the validated authentication token in the thread-local SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        filterChain.doFilter(request, response);
    }
}
```

---

## 🛡️ 3. Role-Based Access Control (RBAC) & Method Security

### 3.1 SecurityFilterChain Bean Definition
```java
package com.example.security.config;

import com.example.security.jwt.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Activates @PreAuthorize and @PostAuthorize
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 1. Disable CSRF (Stateless REST APIs using JWTs do not store cookies in browser)
            .csrf(AbstractHttpConfigurer::disable)
            // 2. Route authorization rules
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**", "/v3/api-docs/**", "/swagger-ui/**").permitAll()
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/v1/manager/**").hasAnyRole("ADMIN", "MANAGER")
                .anyRequest().authenticated()
            )
            // 3. Enforce Stateless Session Policy
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // 4. Inject JWT filter before standard UsernamePasswordAuthenticationFilter
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
```

### 3.2 Granular Method Security with `@PreAuthorize`
```java
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    // Only users with write permission or ADMIN role can execute
    @PostMapping
    @PreAuthorize("hasAuthority('order:write') or hasRole('ADMIN')")
    public ResponseEntity<OrderDto> createOrder(@RequestBody CreateOrderRequest request) {
        return ResponseEntity.ok(orderService.create(request));
    }

    // Access control evaluating method arguments (SpEL)
    @GetMapping("/{orderId}")
    @PreAuthorize("hasRole('ADMIN') or #username == authentication.name")
    public ResponseEntity<OrderDto> getOrder(@PathVariable String orderId, @RequestParam String username) {
        return ResponseEntity.ok(orderService.getById(orderId));
    }
}
```

---

## 🌐 4. Production CORS & CSRF Hardening

### Production-Grade CORS Configuration
```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(List.of("https://dashboard.example.com", "https://api.example.com"));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With"));
    config.setExposedHeaders(List.of("Authorization"));
    config.setAllowCredentials(true);
    config.setMaxAge(3600L); // Cache pre-flight response for 1 hour

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
}
```

---

## 🎫 5. OAuth2 Resource Server & OpenID Connect (OIDC)

When integrating with Keycloak, Okta, Auth0, or Azure AD, Spring Boot can validate incoming JWT tokens directly via JWKS (JSON Web Key Sets).

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
public SecurityFilterChain oauth2ResourceServerFilterChain(HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/public/**").permitAll()
            .anyRequest().authenticated()
        )
        .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
    return http.build();
}
```

---

## 🏭 6. Production Scenarios & War Room Incident Forensics

### Scenario 1: `SecurityContextHolder` Empty Inside `@Async` Methods
- **Symptom:** Inside a method annotated with `@Async`, `SecurityContextHolder.getContext().getAuthentication()` returns `null`, throwing access denied exceptions.
- **Root Cause:** By default, `SecurityContextHolder` uses `MODE_THREADLOCAL`, which does not propagate context to new worker threads created by an `Executor`.
- **The Fix:** Configure the SecurityContext strategy to propagate to child threads at application startup:
```java
@PostConstruct
public void enableSecurityContextInheritance() {
    SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
}
```

### Scenario 2: Browser Pre-Flight `OPTIONS` Returning HTTP 403 Forbidden
- **Root Cause:** Spring Security intercepted the CORS pre-flight `OPTIONS` request before the CORS filter processed it, rejecting it because it lacked an `Authorization` header.
- **The Fix:** Explicitly allow `HttpMethod.OPTIONS` or ensure `.cors(Customizer.withDefaults())` is configured *before* authorization rules.

---

## ⚖️ 7. Spring Security 6 Master Cheat Sheet

| Task | Security 6 Syntax |
| :--- | :--- |
| **Permit Endpoint** | `.requestMatchers("/public/**").permitAll()` |
| **Role Restriction** | `.requestMatchers("/admin/**").hasRole("ADMIN")` |
| **Authority Check** | `.requestMatchers(POST, "/items").hasAuthority("items:create")` |
| **Stateless Session**| `.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))` |
| **Method Security** | `@EnableMethodSecurity` + `@PreAuthorize("hasRole('VIP')")` |
| **Get Authenticated User** | `SecurityContextHolder.getContext().getAuthentication().getName()` |
| **Password Encoder** | `@Bean public PasswordEncoder encoder() { return new BCryptPasswordEncoder(); }` |

---
[🏠 Back to Home](README.md)
