[🏠 Back to Home](README.md)

# 🛡️ Spring Security 6 & OAuth2 / JWT Enterprise Master Guide

A production-grade engineering handbook for securing modern Spring Boot microservices, APIs, and cloud-native applications using **Spring Security 6.x**, **Spring Boot 3.x**, **OAuth 2.0**, and **Stateless JWTs**. Covers `SecurityFilterChain` architecture, custom authentication filters, RBAC vs ABAC, method security, CORS/CSRF configurations, and zero-trust authentication.

---

## 📑 Table of Contents
1. [🧠 Zero-to-Hero Mental Model: The Airport Security Checkpoint](#-zero-to-hero-mental-model-the-airport-security-checkpoint)
2. [⚙️ 1. Spring Security 6 Architecture & Filter Chain](#️-1-spring-security-6-architecture--filter-chain)
3. [🔑 2. Stateless JWT Authentication Filter Implementation](#-2-stateless-jwt-authentication-filter-implementation)
4. [🛡️ 3. Role-Based Access Control (RBAC) & Method Security](#️-3-role-based-access-control-rbac--method-security)
5. [🌐 4. Production CORS & CSRF Hardening](#-4-production-cors--csrf-hardening)
6. [🎫 5. OAuth2 Resource Server & OpenID Connect (OIDC)](#-5-oauth2-resource-server--openid-connect-oidc)
7. [🏭 6. Production Scenarios & War Room Incident Forensics](#-6-production-scenarios--war-room-incident-forensics)
8. [⚖️ 7. Spring Security 6 Master Cheat Sheet](#️-7-spring-security-6-master-cheat-sheet)

---

## 🧠 Zero-to-Hero Mental Model: The Airport Security Checkpoint

```
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                      SPRING SECURITY FILTER CHAIN (The Security Checkpoint)            │
│                                                                                        │
│  Inbound HTTP Request ──> [ 1. CorsFilter (Border Gate) ]                              │
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
│                     [ DispatcherServlet ──> Your Controller ]                          │
└────────────────────────────────────────────────────────────────────────────────────────┘
```

1. **`SecurityFilterChain`:** An ordered chain of servlet filters that intercepts every HTTP request before it reaches Spring's `DispatcherServlet`.
2. **`Authentication` (Who are you?):** Establishes identity (validates username/password or JWT signature). Stored in `SecurityContextHolder`.
3. **`Authorization` (What are you allowed to do?):** Checks whether the authenticated principal has the required roles/privileges (`GrantedAuthority`) to access the requested resource.

---

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
