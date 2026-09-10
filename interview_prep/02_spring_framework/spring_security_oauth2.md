# Spring Security 6, OAuth2 & JWT: Enterprise Interview Guide

> **Curriculum Milestone**: Module 02 - Spring Framework Engineering  
> **Topic Coverage**: SecurityFilterChain Architecture, DelegatingFilterProxy, Authentication vs Authorization, OAuth2 Resource Server, JWT Validation & JWKS, Method‑Level Security (`@PreAuthorize`, `@Secured`), CORS/CSRF Configuration, RBAC vs ABAC, Spring Security 6 Migration, Password Encoding (BCrypt/Argon2), Token Blacklisting, and Zero‑Trust Authentication Patterns.  
> **Target Audience**: Senior Software Engineers, Lead Architects, Staff & Principal Engineers.  
> **Target Depth**: 50 Progressive Technical Scenarios with Runtime Mechanics, Production Code Walkthroughs, Beginner Traps, and Real‑World Outage Post‑Mortems.

---

## Architecture Blueprint: The Spring Security Filter Chain

```
+---------------------------------------------------------------------------------------------+
|                              HTTP Request Lifecycle                                          |
|                                                                                              |
|  Client Request (HTTP/1.1, HTTP/2)                                                           |
|       │                                                                                      |
|       ▼                                                                                      |
|  +-------------------------------+                                                           |
|  | Servlet Container (Tomcat)    |                                                           |
|  |  └─ DelegatingFilterProxy     |  ← Bridges Servlet world to Spring WebApplicationContext   |
|  |       └─ FilterChainProxy     |  ← Spring Security's master filter ("springSecurity...FilterChain")
|  +-------------------------------+                                                           |
|       │                                                                                      |
|       ▼  (Ordered Security Filters)                                                          |
|  +-------------------------------+                                                           |
|  | 1. CorsFilter                 |  ← Handles CORS preflight (OPTIONS)                       |
|  | 2. HeaderWriterFilter         |  ← Adds security headers (HSTS, X-Frame-Options, CSP)     |
|  | 3. CsrfFilter                 |  ← Validates CSRF tokens (cookie-based stateful apps)     |
|  | 4. LogoutFilter               |  ← Intercepts /logout and clears security context         |
|  | 5. SecurityContextHolderFilter|  ← Loads SecurityContext from repository (Session/Token)  |
|  | 6. BearerTokenAuthFilter      |  ← Extracts & validates JWT from Authorization header     |
|  | 7. UsernamePasswordAuthFilter |  ← Form login (POST /login)                               |
|  | 8. RequestCacheAwareFilter    |  ← Replays saved request after successful login           |
|  | 9. AnonymousAuthFilter        |  ← Assigns AnonymousAuthenticationToken if unauthenticated|
|  | 10. ExceptionTranslationFilter|  ← Translates AccessDeniedException -> 403 / 401 response |
|  | 11. AuthorizationFilter       |  ← Evaluates hasRole/hasAuthority/SpEL rules              |
|  +-------------------------------+                                                           |
|       │                                                                                      |
|       ▼                                                                                      |
|  +-------------------------------+                                                           |
|  | DispatcherServlet (Spring MVC)|  ← Request reaches @Controller only if all filters pass   |
|  +-------------------------------+                                                           |
+---------------------------------------------------------------------------------------------+
```

---

## Section 1: Progressive Scenario‑Based Master Q&A (50 Scenarios)

### Tier 1: Core Fundamentals — Authentication, Authorization & Filter Chain (Q1 – Q16)

#### Q1: SecurityFilterChain Architecture — Tomcat to DispatcherServlet

##### 1. Exact Scenario & Question
A junior developer adds `spring-boot-starter-security` to a project, and suddenly all endpoints return 401 Unauthorized, even though they haven't written any configuration. Explain the entire request flow from Tomcat to your `@RestController`, how `DelegatingFilterProxy` bridges the Servlet and Spring containers, and why the auto-configured `SecurityFilterChain` blocks all endpoints by default.

##### 2. What the Interviewer Evaluates
- Understanding `DelegatingFilterProxy` → `FilterChainProxy` → ordered `SecurityFilterChain`.
- Understanding that Spring Boot auto-configures `anyRequest().authenticated()`.
- Tracing the exact filter execution order and exception translation.

##### 3. Standout Technical Answer
When `spring-boot-starter-security` is present on the classpath, Spring Boot automatically registers a Servlet Filter named `springSecurityFilterChain`.
1. **DelegatingFilterProxy**: Tomcat knows only standard Servlet filters. Spring registers `DelegatingFilterProxy` in `web.xml` / ServletContext. It intercepts incoming requests and delegates them to the Spring Bean named `springSecurityFilterChain`.
2. **FilterChainProxy**: This bean manages one or more `SecurityFilterChain` instances, matching request URIs against configured `RequestMatcher` patterns.
3. **Ordered Execution**: The request passes through the filter pipeline. By default, `AuthorizationFilter` is placed near the end of the chain with `anyRequest().authenticated()`.
4. If no authentication header or session is present, `AnonymousAuthenticationFilter` assigns an `AnonymousAuthenticationToken`. When `AuthorizationFilter` executes, it verifies that the principal is not anonymous. Because it is anonymous, it throws `AccessDeniedException`.
5. `ExceptionTranslationFilter` catches this exception and sends an HTTP 401 Unauthorized or redirects to the auto-generated `/login` page.

```java
@Configuration
@EnableWebSecurity
public class ProductionSecurityConfig {

    @Bean
    public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(AbstractHttpConfigurer::disable) // Safe for stateless Bearer token APIs
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/public/**", "/actuator/health/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
            .build();
    }
}
```

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "If you configure two `SecurityFilterChain` beans, how does Spring Security decide which one to execute?"
- **Winning Answer**: "`FilterChainProxy` evaluates chains in order of their `@Order` annotation. The **first** chain whose `securityMatcher()` matches the incoming request URL processes it; all subsequent chains are completely bypassed. Only one filter chain executes per request."

---

#### Q2: Authentication vs Authorization — 401 vs 403 & ExceptionTranslationFilter

##### 1. Exact Scenario & Question
A frontend engineer reports: "When a user logs in with an expired JWT, they receive 403 Forbidden. When they access an admin endpoint with a valid user token, they also receive 403 Forbidden." Explain the RFC 7235 semantic difference between 401 and 403, and show how to configure `AuthenticationEntryPoint` and `AccessDeniedHandler` to return standardized RFC 7807 Problem Details.

##### 2. What the Interviewer Evaluates
- Differentiating between **Authentication** (401 Unauthorized — Identity not established/expired) and **Authorization** (403 Forbidden — Identity verified, but insufficient privileges).
- Configuring custom `AuthenticationEntryPoint` (for 401) and `AccessDeniedHandler` (for 403).

##### 3. Standout Technical Answer
- **HTTP 401 Unauthorized**: "I do not know who you are (missing, invalid, or expired credentials). Provide valid credentials."
- **HTTP 403 Forbidden**: "I know who you are, but you do not have permission to access this resource."

```java
@Component
public class SecurityExceptionHandlerConfig {

    @Bean
    public AuthenticationEntryPoint customAuthenticationEntryPoint(ObjectMapper mapper) {
        return (request, response, authException) -> {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
            
            ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED, 
                "Authentication required or token expired: " + authException.getMessage()
            );
            problem.setTitle("Unauthorized");
            problem.setType(URI.create("https://api.enterprise.com/errors/unauthorized"));
            
            response.getWriter().write(mapper.writeValueAsString(problem));
        };
    }

    @Bean
    public AccessDeniedHandler customAccessDeniedHandler(ObjectMapper mapper) {
        return (request, response, accessDeniedException) -> {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
            
            ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.FORBIDDEN, 
                "Insufficient privileges: " + accessDeniedException.getMessage()
            );
            problem.setTitle("Forbidden");
            problem.setType(URI.create("https://api.enterprise.com/errors/forbidden"));
            
            response.getWriter().write(mapper.writeValueAsString(problem));
        };
    }
}
```

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "Why does an unauthenticated user get a 403 instead of 401 if you configure `accessDeniedHandler` but forget `authenticationEntryPoint`?"
- **Winning Answer**: "Because `AnonymousAuthenticationFilter` assigns an anonymous token. If unconfigured, the authorization filter treats the anonymous user as an authenticated user who simply lacks roles, triggering the `AccessDeniedHandler` (403). The entry point must be configured to check if the principal is anonymous and force a 401."

---

#### Q3: Method-Level Security — `@PreAuthorize`, `@PostAuthorize`, `@Secured`, `@RolesAllowed`

##### 1. Exact Scenario & Question
Your application has financial service methods that should only allow managers or account owners to execute. Compare `@PreAuthorize`, `@PostAuthorize`, `@Secured`, and JSR-250 `@RolesAllowed`. How does Spring AOP enforce these annotations, and what is the difference between `#authentication.name` and `#id` in SpEL?

##### 2. What the Interviewer Evaluates
- Understanding SpEL (Spring Expression Language) inside `@PreAuthorize`.
- Understanding that `@PostAuthorize` executes the method first, then validates the returned object before sending it to the caller.
- Enabling method security via `@EnableMethodSecurity`.

##### 3. Standout Technical Answer
```java
@Configuration
@EnableMethodSecurity(
    prePostEnabled = true, // Enables @PreAuthorize / @PostAuthorize (Default in Spring Security 6)
    securedEnabled = true, // Enables legacy @Secured
    jsr250Enabled = true   // Enables JSR-250 @RolesAllowed
)
public class MethodSecurityConfig {}

@Service
public class AccountService {

    // 1. @PreAuthorize: Evaluated BEFORE method execution
    @PreAuthorize("hasRole('ADMIN') or #account.ownerUsername == authentication.name")
    public void updateAccount(Account account) {
        // Business logic...
    }

    // 2. @PostAuthorize: Evaluated AFTER method executes; can inspect returnObject
    @PostAuthorize("returnObject.ownerUsername == authentication.name or hasRole('AUDITOR')")
    public Account getAccountDetails(Long accountId) {
        return accountRepository.findById(accountId).orElseThrow();
    }

    // 3. JSR-250 Standard: Strict string role check without SpEL
    @RolesAllowed("ROLE_MANAGER")
    public void closeAccount(Long accountId) {
        accountRepository.deleteById(accountId);
    }
}
```

| Annotation | Supports SpEL? | Evaluated When | Inspects Return Value? | Standard |
|---|---|---|---|---|
| `@PreAuthorize` | ✅ Yes | Before method | ❌ No | Spring Security |
| `@PostAuthorize` | ✅ Yes | After method | ✅ Yes (`returnObject`) | Spring Security |
| `@Secured` | ❌ No | Before method | ❌ No | Legacy Spring |
| `@RolesAllowed` | ❌ No | Before method | ❌ No | JSR-250 / Java EE |

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "If `@PostAuthorize` denies access, was the method already executed, and were database updates committed?"
- **Winning Answer**: "YES! `@PostAuthorize` executes the target method **completely**. If the method performed database writes, those writes were executed. If access is denied, Spring throws `AccessDeniedException`, which rolls back an active transaction if `@Transactional` is present, but side-effects (like sent emails or external API calls) cannot be undone. Never use `@PostAuthorize` on mutating methods; reserve it strictly for read operations."

---

#### Q4: Spring Security 6 Architecture — Component-Based Configuration

##### 1. Exact Scenario & Question
A legacy Spring Boot 2 application is upgraded to Spring Boot 3. The compiler fails on `extends WebSecurityConfigurerAdapter` and `antMatchers()`. Detail the architectural shift in Spring Security 6 to component-based security, and explain why `requestMatchers()` replaces `antMatchers()`.

##### 2. What the Interviewer Evaluates
- Understanding why `WebSecurityConfigurerAdapter` was removed in favor of `SecurityFilterChain` beans.
- Explaining the CVE vulnerability that prompted the transition from `antMatchers` to `requestMatchers`.
- Lambda DSL configuration style in Spring Security 6.

##### 3. Standout Technical Answer
In Spring Security 6:
1. **Removal of Adapter**: `WebSecurityConfigurerAdapter` encouraged tight coupling and multiple configuration overrides. It is replaced by standalone `@Bean` methods returning `SecurityFilterChain`.
2. **`antMatchers()` Vulnerability & Deprecation**: `antMatchers()` used raw path string patterns that frequently desynchronized with Spring MVC's path matching (e.g. `/admin` vs `/admin/` or path extensions). A path could bypass Spring Security's `antMatcher` while still reaching Spring MVC's `@GetMapping("/admin")`. `requestMatchers()` unifies URL resolution using the application's active `HandlerMappingIntrospector`.

```java
// Spring Security 6 Production Standard (Lambda DSL)
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.GET, "/public/**").permitAll()
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .build();
    }
}
```

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "Why does `hasRole(\"ADMIN\")` fail if the JWT token contains the authority `\"ADMIN\"`?"
- **Winning Answer**: "Spring Security's `hasRole(\"ADMIN\")` internally prefixes the string with `ROLE_`, looking for `ROLE_ADMIN`. If your JWT `roles` or `authorities` claim contains raw `\"ADMIN\"`, the check fails. You must either use `hasAuthority(\"ADMIN\")` or configure a `JwtAuthenticationConverter` to automatically prepend the `ROLE_` prefix."

---

#### Q5: CSRF Protection — Why SPAs with Bearer Tokens Don't Need CSRF

##### 1. Exact Scenario & Question
A security auditor flags that your microservice disables CSRF (`csrf.disable()`). The architect defends this decision because the API is stateless and uses JWT Bearer tokens. Explain the exact mechanism of a Cross-Site Request Forgery (CSRF) attack and why stateless Bearer token APIs are inherently immune, while cookie-authenticated APIs are vulnerable.

##### 2. What the Interviewer Evaluates
- Explaining how CSRF exploits automatic browser cookie attachment.
- Understanding why `Authorization: Bearer <token>` cannot be forged across origins.
- Configuring `CookieCsrfTokenRepository` when cookies ARE used for SPAs.

##### 3. Standout Technical Answer
A CSRF attack occurs when a victim visits a malicious site (`attacker.com`) while logged into a legitimate bank (`bank.com`).
1. **Cookie-Based Vulnerability**: The malicious site contains `<form action="https://bank.com/transfer" method="POST">`. When submitted, the browser **automatically attaches `bank.com` session cookies**. The bank server assumes the request was intentional.
2. **Bearer Token Immunity**: Modern SPAs store JWTs in memory or JavaScript storage and send them via `Authorization: Bearer <JWT>`. Browsers **NEVER** automatically attach the `Authorization` header on cross-origin form submissions or image tags. An attacker on `attacker.com` has no access to the client's in-memory token due to the Same-Origin Policy (SOP). Therefore, CSRF is impossible.

```java
// When cookies ARE used (e.g. BFF - Backend for Frontend):
@Bean
public SecurityFilterChain cookieSessionSecurity(HttpSecurity http) throws Exception {
    return http
        .csrf(csrf -> csrf
            // Store CSRF token in an accessible cookie (XSRF-TOKEN) that Angular/React reads
            .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            .csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler())
        )
        .build();
}
```

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "If an SPA stores a JWT in a cookie with `HttpOnly; SameSite=Strict`, is CSRF possible?"
- **Winning Answer**: "`SameSite=Strict` prevents the browser from sending the cookie on any cross-origin navigation, blocking classic CSRF. However, older browsers do not support `SameSite`, and sub-domain takeovers can bypass it. If you use cookies to store tokens, you should always enforce CSRF tokens."

---

#### Q6: OAuth2 Authorization Code Flow with PKCE — Security Mechanics

##### 1. Exact Scenario & Question
You are implementing authentication for a Single Page Application (React) and mobile app communicating with Keycloak/Auth0. Why is the legacy Implicit Flow deprecated by OAuth 2.1, and how does the Authorization Code Flow with **PKCE (Proof Key for Code Exchange)** prevent authorization code interception attacks?

##### 2. What the Interviewer Evaluates
- Understanding why returning tokens in URL fragments (Implicit Flow) causes token leakage via browser history and referrer headers.
- Explaining the mathematical relationship between `code_verifier` and `code_challenge`.
- End-to-end trace of the PKCE handshake.

##### 3. Standout Technical Answer
In the **Implicit Flow**, access tokens were returned directly in the redirect URL hash (`#access_token=...`), leaving them exposed in browser history, logs, and `Referer` headers.

**Authorization Code Flow with PKCE:**
1. **Code Verifier**: The client generates a high-entropy cryptographic random string (`code_verifier`).
2. **Code Challenge**: The client computes `code_challenge = BASE64URL(SHA256(code_verifier))`.
3. **Authorization Request**: Client redirects user to Authorization Server with `code_challenge` and `code_challenge_method=S256`.
4. **Authorization Code Issued**: User logs in; Auth Server returns a short-lived one-time `authorization_code` to the client redirect URI.
5. **Token Exchange**: Client sends `POST /token` containing `authorization_code` AND the plain `code_verifier`.
6. **Verification**: The Auth Server hashes the received `code_verifier` using SHA-256 and verifies it matches the previously stored `code_challenge`. If an attacker intercepted the authorization code, they cannot exchange it for a token because they do not know the `code_verifier`!

```
[SPA Client]                         [Authorization Server]
     │                                         │
     │ ─── 1. /authorize?code_challenge=XYZ ──► │ (Stores XYZ with auth request)
     │ ◄── 2. Redirect with ?code=ABC ──────── │
     │                                         │
     │ ─── 3. /token?code=ABC&verifier=123 ───► │ (Verifies SHA256(123) == XYZ)
     │ ◄── 4. Returns Access + Refresh Token ─ │
```

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "Why is `client_secret` useless in a Single Page Application or Mobile App?"
- **Winning Answer**: "Because SPAs and mobile apps are **Public Clients**. Any `client_secret` packaged in JavaScript bundles or APK binaries can be easily extracted via browser dev tools or decompilation. PKCE replaces static shared secrets with dynamically generated per-request cryptographic challenges."

---

#### Q7: JWT Validation & JWKS Endpoint — Public Key Caching & Rotation

##### 1. Exact Scenario & Question
Your Spring Boot Resource Server validates JWTs issued by Okta. A junior engineer writes custom code that calls Okta's `/oauth2/v1/keys` JWKS endpoint on every single incoming HTTP request, causing severe latency and hitting Okta rate limits. How does Spring Security's `NimbusJwtDecoder` automatically cache JWKS keys, and how does key rotation work when Okta rolls its signing keys?

##### 2. What the Interviewer Evaluates
- Understanding JSON Web Key Sets (JWKS) and the `kid` (Key ID) header claim.
- Configuring `NimbusJwtDecoder` with built-in caching.
- Handling key rotation gracefully without server restart.

##### 3. Standout Technical Answer
When verifying an asymmetric JWT (e.g. RS256), the Resource Server needs the Identity Provider's public key. The IdP publishes its public keys at the JWKS endpoint (`/.well-known/jwks.json`), where each key has a unique `kid` identifier.

Spring Security's `NimbusJwtDecoder` uses a thread-safe, in-memory **LRU cache** with a default TTL (typically 5 to 15 minutes) to cache public keys.

```java
@Configuration
public class JwtSecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwkSetUri;

    @Bean
    public JwtDecoder jwtDecoder() {
        // Creates a NimbusJwtDecoder backed by a cached RemoteJWKSet
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri)
            .jwsAlgorithm(SignatureAlgorithm.RS256)
            .cache(new ConcurrentMapCache("jwksCache")) // Custom caching if desired
            .build();

        // Enforce strict token validation rules
        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer("https://auth.enterprise.com");
        OAuth2TokenValidator<Jwt> withAudience = new DelegatingOAuth2TokenValidator<>(
            withIssuer, 
            new JwtClaimValidator<List<String>>("aud", aud -> aud != null && aud.contains("api://payment-service"))
        );

        jwtDecoder.setJwtValidator(withAudience);
        return jwtDecoder;
    }
}
```

*Key Rotation Mechanics:*
When Okta rotates keys, incoming tokens contain a new `kid` not present in the local cache. `NimbusJwtDecoder` detects the cache miss for that specific `kid` and immediately executes a single non-blocking refresh to the JWKS endpoint to fetch the new key, ensuring zero-downtime key rotation.

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "What attack occurs if an attacker sends an expired token with a fake `kid` pointing to their own public server?"
- **Winning Answer**: "This is a **JWKS Spoofing / SSRF attack**. If a server dynamically downloads keys from a URL in the token header (`jku` claim) without validation, it verifies the forged signature. Spring Security strictly prevents this by fetching public keys exclusively from the hardcoded, pre-configured `jwkSetUri` on the server."

---

#### Q8: Opaque Tokens vs Self-Contained JWTs — Introspection vs Cryptography

##### 1. Exact Scenario & Question
Your security team mandates that when an employee is terminated, their access must be revoked within 10 seconds across all 50 microservices. Why do stateless JWTs fail to meet this requirement without extra architecture, and how does **OAuth2 Token Introspection** (RFC 7662) solve it at the cost of network latency?

##### 2. What the Interviewer Evaluates
- Understanding stateless JWT trade-offs (fast, decentralized, but impossible to revoke before expiry).
- Implementing Opaque Token Introspection via Spring Security.
- Designing hybrid solutions (short-lived JWTs + Redis revocation lists).

##### 3. Standout Technical Answer
```
Stateless JWT (Cryptographic Verification):
[Microservice] ──(Validates signature locally via Public Key)──► Allowed!
Problem: Even if user is fired in IdP, token remains valid until `exp` timestamp!

Opaque Token (Token Introspection RFC 7662):
[Microservice] ──(POST /introspect to Auth Server)──────────────► Check active status!
Advantage: Immediate revocation! Disadvantage: Network round-trip on EVERY request.
```

```java
// Spring Security Opaque Token Resource Server Configuration:
@Bean
public SecurityFilterChain opaqueTokenFilterChain(HttpSecurity http) throws Exception {
    return http
        .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
        .oauth2ResourceServer(oauth2 -> oauth2
            .opaqueToken(opaque -> opaque
                .introspectionUri("https://auth.enterprise.com/oauth2/introspect")
                .introspectionClientCredentials("resource-server-id", "resource-server-secret")
            )
        )
        .build();
}
```

| Dimension | Self-Contained JWT | Opaque Token (Introspection) |
|---|---|---|
| Verification | Pure in-memory cryptographic check | Remote HTTP call to Identity Provider |
| Latency | < 1 ms | 10 ms – 50 ms (Network hop) |
| Revocation Speed | Delayed (Until `exp` passes, e.g. 15m) | **Instant (< 1 second)** |
| IdP Availability Dependency | Zero (Decentralized) | High (If IdP fails, all microservices fail) |

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "How can you achieve instant revocation while keeping the high performance of stateless JWTs?"
- **Winning Answer**: "Use **Short-Lived JWTs** (5-minute TTL) paired with a **Distributed Revocation Blacklist in Redis**. Microservices validate the JWT signature locally, and perform an O(1) in-memory Redis check only for tokens marked as revoked. When a user is fired, their `userId` or token `jti` is pushed to Redis with a TTL matching the token's remaining lifetime."

---

#### Q9: Custom `AuthenticationProvider` — Multi-Factor Authentication (MFA)

##### 1. Exact Scenario & Question
You are implementing an enterprise login flow where employees enter username, password, and a 6-digit Time-Based One-Time Password (TOTP) from Google Authenticator. How do you implement a custom `AuthenticationProvider` in Spring Security to validate both credentials atomically?

##### 2. What the Interviewer Evaluates
- Implementing `AuthenticationProvider.authenticate(Authentication)`.
- Creating a custom `AuthenticationToken` carrying the MFA code.
- Managing `BadCredentialsException` without leaking account existence.

##### 3. Standout Technical Answer
```java
// 1. Custom Authentication Token carrying username, password, and TOTP code
public class MfaAuthenticationToken extends UsernamePasswordAuthenticationToken {
    private final String totpCode;

    public MfaAuthenticationToken(String principal, String credentials, String totpCode) {
        super(principal, credentials);
        this.totpCode = totpCode;
    }

    public MfaAuthenticationToken(Object principal, Object credentials, String totpCode, 
                                  Collection<? extends GrantedAuthority> authorities) {
        super(principal, credentials, authorities);
        this.totpCode = totpCode;
    }

    public String getTotpCode() { return totpCode; }
}

// 2. Custom Authentication Provider validating both password and TOTP
@Component
public class MfaAuthenticationProvider implements AuthenticationProvider {

    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final TotpService totpService;

    public MfaAuthenticationProvider(UserDetailsService userDetailsService, 
                                   PasswordEncoder passwordEncoder, 
                                   TotpService totpService) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.totpService = totpService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        MfaAuthenticationToken token = (MfaAuthenticationToken) authentication;
        String username = token.getName();
        String rawPassword = token.getCredentials().toString();
        String totpCode = token.getTotpCode();

        UserDetails user = userDetailsService.loadUserByUsername(username);

        // Step 1: Validate Password
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new BadCredentialsException("Invalid username or credentials");
        }

        // Step 2: Validate TOTP Code
        String secretKey = ((MfaUser) user).getTotpSecretKey();
        if (!totpService.verifyCode(secretKey, totpCode)) {
            throw new BadCredentialsException("Invalid MFA TOTP Code");
        }

        // Return fully authenticated token with granted authorities
        return new MfaAuthenticationToken(user, null, null, user.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return MfaAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
```

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "Why should the error message be identical ('Invalid username or credentials') whether the password failed or the TOTP code failed?"
- **Winning Answer**: "To prevent **Account Enumeration and Timing Attacks**. If you return 'Invalid Password' vs 'Invalid MFA Code', an attacker learns that the password was correct and can focus their brute-force efforts strictly on cracking the 6-digit TOTP code."

---

#### Q10: `SecurityContextHolder` Strategies — ThreadLocal vs InheritableThreadLocal

##### 1. Exact Scenario & Question
A service method spawns background threads using `new Thread()` or `CompletableFuture.runAsync()`. Inside the background thread, `SecurityContextHolder.getContext().getAuthentication()` returns `null`. Explain the three `SecurityContextHolder` storage strategies, their threading implications, and why `MODE_INHERITABLETHREADLOCAL` causes security leaks in pooled thread environments.

##### 2. What the Interviewer Evaluates
- Understanding `MODE_THREADLOCAL` (default), `MODE_INHERITABLETHREADLOCAL`, and `MODE_GLOBAL`.
- Explaining why thread pool reuse (`ExecutorService`) corrupts `InheritableThreadLocal`.
- Using `DelegatingSecurityContextExecutor` to propagate security contexts safely.

##### 3. Standout Technical Answer
By default, Spring Security uses `SecurityContextHolder.MODE_THREADLOCAL`, which stores context in a standard `ThreadLocal`. Child threads do not inherit this context.

**Why `MODE_INHERITABLETHREADLOCAL` is Dangerous in Production:**
`MODE_INHERITABLETHREADLOCAL` copies the context only when a thread is **newly spawned**. In web applications, worker threads (Tomcat / thread pools) are **pooled and reused across thousands of requests**.
- Thread 1 processes Request A for User Alice and returns to the pool.
- Thread 1 is reused for Request B for User Bob. If context is inherited, User Bob can execute operations under Alice's identity!

```java
// ✅ PRODUCTION STANDARD: Explicit Context Propagation via DelegatingSecurityContextExecutor
@Configuration
@EnableAsync
public class AsyncSecurityConfig {

    @Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        executor.setThreadNamePrefix("async-sec-");
        executor.initialize();

        // Wraps tasks to copy SecurityContext on submit and CLEAR on finish!
        return new DelegatingSecurityContextAsyncTaskExecutor(executor);
    }
}
```

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "When is `SecurityContextHolder.MODE_GLOBAL` used?"
- **Winning Answer**: "`MODE_GLOBAL` stores a single static `SecurityContext` shared by all threads in the JVM. It is strictly used in standalone desktop applications (like JavaFX or Swing desktop apps) where a single human user logs into the entire JVM instance."

---

#### Q11: Spring Security Testing — `@WithMockUser` vs `@WithUserDetails`

##### 1. Exact Scenario & Question
You are writing integration tests for a controller protected by `@PreAuthorize("hasRole('ADMIN')")`. What is the difference between `@WithMockUser`, `@WithUserDetails`, and `SecurityMockMvcRequestPostProcessors.jwt()`, and how do you test custom claims?

##### 2. What the Interviewer Evaluates
- Using Spring Security Test annotations in `@WebMvcTest`.
- Differentiating synthetic mock users vs real `UserDetails` loaded from database.
- Testing OAuth2 JWT Resource Servers with mock JWT tokens.

##### 3. Standout Technical Answer
```java
@WebMvcTest(OrderController.class)
class OrderControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    // 1. @WithMockUser: Synthesizes a generic UsernamePasswordAuthenticationToken in SecurityContext
    @Test
    @WithMockUser(username = "admin_user", roles = {"ADMIN"})
    void shouldAllowAdminToAccessRestrictedEndpoint() throws Exception {
        mockMvc.perform(get("/api/admin/metrics"))
            .andExpect(status().isOk());
    }

    // 2. Testing OAuth2 JWT Resource Server with custom claims:
    @Test
    void shouldValidateCustomJwtClaims() throws Exception {
        mockMvc.perform(get("/api/orders/my-orders")
            .with(jwt()
                .jwt(builder -> builder
                    .subject("usr-12345")
                    .claim("email", "john@enterprise.com")
                    .claim("scope", "orders.read")
                )
                .authorities(new SimpleGrantedAuthority("ROLE_USER"))
            ))
            .andExpect(status().isOk());
    }
}
```

| Test Annotation / PostProcessor | Context Created | Database Access | Best Used For |
|---|---|---|---|
| `@WithMockUser` | Synthetic `UsernamePasswordAuthenticationToken` | None | Simple unit tests checking `@PreAuthorize("hasRole(...)")` |
| `@WithUserDetails` | Custom `UserDetails` loaded via `UserDetailsService` | Loads from DB / Mock | Testing custom principal domain methods (`user.getId()`) |
| `jwt().jwt(...)` | Real `JwtAuthenticationToken` with claims | None | OAuth2 Resource Server testing |

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "Why will `@WithMockUser(roles = \"ADMIN\")` fail if your controller checks `@PreAuthorize(\"hasAuthority('ADMIN')\")`?"
- **Winning Answer**: "Because `@WithMockUser(roles = \"ADMIN\")` automatically prepends `ROLE_`, injecting `ROLE_ADMIN` into the granted authorities. If your SpEL checks `hasAuthority('ADMIN')`, it expects exact string match `ADMIN` and fails. You must use `@WithMockUser(authorities = \"ADMIN\")` to set authorities without the prefix."

---

#### Q12: Brute-Force Login Protection — Bucket4j Rate Limiting Filter

##### 1. Exact Scenario & Question
Your application experiences a credential-stuffing attack: bots send 10,000 login attempts per second against `/api/auth/login`. Design a non-blocking rate-limiting security filter using **Bucket4j** that limits each IP address to at most 5 login attempts per minute, returning HTTP 429 Too Many Requests.

##### 2. What the Interviewer Evaluates
- Designing a custom Servlet Filter in the `SecurityFilterChain`.
- Implementing Token Bucket rate-limiting per client IP.
- Preventing memory leaks from stale IP keys.

##### 3. Standout Technical Answer
```java
@Component
public class LoginRateLimiterFilter extends OncePerRequestFilter {

    // In-memory cache of IP -> TokenBucket with 10-minute automatic eviction
    private final Map<String, Bucket> ipBucketCache = new ConcurrentHashMap<>();

    private Bucket createNewBucket() {
        // 5 tokens capacity; refills 5 tokens every 1 minute
        Bandwidth limit = Bandwidth.classic(5, Refill.intervally(5, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {

        if ("/api/auth/login".equalsIgnoreCase(request.getRequestURI()) && 
            "POST".equalsIgnoreCase(request.getMethod())) {

            String clientIp = extractClientIp(request);
            Bucket bucket = ipBucketCache.computeIfAbsent(clientIp, k -> createNewBucket());

            if (!bucket.tryConsume(1)) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setHeader("Retry-After", "60");
                response.getWriter().write("{\"error\":\"Too many login attempts. Please wait 60 seconds.\"}");
                return; // Short-circuit filter chain!
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
```

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "Why is `X-Forwarded-For` vulnerable to spoofing if your reverse proxy / Load Balancer is misconfigured?"
- **Winning Answer**: "An attacker can manually send an `X-Forwarded-For: 8.8.8.8` header in their HTTP request. If your reverse proxy (Nginx / Cloudflare) appends to the header rather than overwriting it, or if your application does not verify that the immediate peer connection originates from a trusted internal proxy IP, an attacker can rotate fake IP headers on every request to bypass rate limiting completely."

---

#### Q13: RBAC vs ABAC — Role-Based vs Attribute-Based Access Control

##### 1. Exact Scenario & Question
In a healthcare application, a Doctor role (RBAC) is insufficient: a doctor must only access medical records if: (1) The patient is assigned to that doctor, AND (2) The doctor is currently on duty in the hospital emergency ward. Explain why RBAC fails for contextual rules, and implement Attribute-Based Access Control (ABAC) in Spring Security.

##### 2. What the Interviewer Evaluates
- Understanding the architectural limitations of Role-Based Access Control (Role explosion).
- Implementing ABAC using SpEL beans or custom `AuthorizationManager`.
- Evaluating runtime context (time, location, resource attributes).

##### 3. Standout Technical Answer
- **RBAC (Role-Based Access Control)**: Permissions are mapped to static roles (`ROLE_DOCTOR`). It cannot express dynamic relationships between the subject (Doctor) and the resource (Patient). Trying to solve this with RBAC causes "Role Explosion" (`ROLE_DOCTOR_ON_DUTY_WARD_4`).
- **ABAC (Attribute-Based Access Control)**: Evaluates attributes of the **Subject** (Department, Shift), **Resource** (Assigned Doctor, Sensitivity), and **Environment** (Time, IP location).

```java
@Component("medicalAbacValidator")
public class MedicalAbacValidator {

    private final DutyRosterService rosterService;
    private final PatientRepository patientRepo;

    public MedicalAbacValidator(DutyRosterService rosterService, PatientRepository patientRepo) {
        this.rosterService = rosterService;
        this.patientRepo = patientRepo;
    }

    public boolean canAccessPatientRecord(Authentication auth, Long patientId) {
        String doctorUsername = auth.getName();

        // Attribute 1: Environmental context (Is doctor actively on duty?)
        if (!rosterService.isDoctorOnDuty(doctorUsername, LocalDateTime.now())) {
            return false;
        }

        // Attribute 2: Resource context (Is patient assigned to this doctor?)
        Patient patient = patientRepo.findById(patientId).orElse(null);
        if (patient == null) return false;

        return doctorUsername.equalsIgnoreCase(patient.getAssignedDoctorUsername());
    }
}

// Controller Method using SpEL ABAC:
@RestController
@RequestMapping("/patients")
public class PatientController {

    @GetMapping("/{id}/records")
    @PreAuthorize("@medicalAbacValidator.canAccessPatientRecord(authentication, #id)")
    public MedicalRecord getPatientRecords(@PathVariable Long id) {
        // Securely returns records
        return recordService.getRecords(id);
    }
}
```

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "What is the performance implication of calling database queries inside SpEL `@PreAuthorize` methods?"
- **Winning Answer**: "Every method call triggers the SpEL evaluation and subsequent database query. If a service method is called inside a loop, it creates an N+1 security query bottleneck. To mitigate this, cache patient-doctor relationships in Redis or pass pre-fetched domain context objects into the SpEL expression."

---

#### Q14: Multi-Tenant Security — Dynamic JWT Issuer Validation

##### 1. Exact Scenario & Question
You are building a multi-tenant SaaS API where each customer company has their own dedicated Okta/Keycloak realm (e.g., `https://auth.enterprise.com/realms/tenant-a` and `https://auth.enterprise.com/realms/tenant-b`). A single static `jwk-set-uri` cannot validate tokens from multiple realms. How do you configure Spring Security's `JwtIssuerAuthenticationManagerResolver` to dynamically validate JWTs from multiple issuers?

##### 2. What the Interviewer Evaluates
- Understanding dynamic tenant multi-issuer resolution.
- Using `JwtIssuerAuthenticationManagerResolver`.
- Whitelisting trusted issuer domains to prevent SSRF.

##### 3. Standout Technical Answer
```java
@Configuration
@EnableWebSecurity
public class MultiTenantSecurityConfig {

    private final Set<String> trustedIssuers = Set.of(
        "https://auth.enterprise.com/realms/tenant-a",
        "https://auth.enterprise.com/realms/tenant-b",
        "https://auth.enterprise.com/realms/tenant-c"
    );

    @Bean
    public SecurityFilterChain multiTenantFilterChain(HttpSecurity http) throws Exception {
        // Resolves the AuthenticationManager dynamically based on the JWT 'iss' claim
        JwtIssuerAuthenticationManagerResolver authenticationManagerResolver = 
            new JwtIssuerAuthenticationManagerResolver(this::resolveAuthenticationManager);

        return http
            .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
            .oauth2ResourceServer(oauth2 -> oauth2
                .authenticationManagerResolver(authenticationManagerResolver)
            )
            .build();
    }

    private AuthenticationManager resolveAuthenticationManager(String issuer) {
        // Enforce strict whitelisting to prevent SSRF attacks!
        if (!trustedIssuers.contains(issuer)) {
            throw new OAuth2AuthenticationException(
                new OAuth2Error("untrusted_issuer"), "Untrusted JWT Issuer: " + issuer
            );
        }

        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withIssuerLocation(issuer).build();
        JwtAuthenticationProvider provider = new JwtAuthenticationProvider(jwtDecoder);
        return provider::authenticate;
    }
}
```

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "Why is passing `NimbusJwtDecoder.withIssuerLocation(issuer)` directly without an allowlist check a critical security vulnerability?"
- **Winning Answer**: "It introduces a **Server-Side Request Forgery (SSRF) and Signature Bypass vulnerability**. An attacker could spin up a rogue authorization server `https://evil-auth.com`, issue their own signed token with `iss: https://evil-auth.com`, and pass it to your API. If your server blindly calls `withIssuerLocation(issuer)`, it will download the attacker's public key from `https://evil-auth.com` and accept the forged token as valid!"

---

#### Q15: Password Encoders — BCrypt vs Argon2 vs PBKDF2

##### 1. Exact Scenario & Question
A security compliance report rejects your application's use of standard SHA-256 for password hashing. Explain why fast hashing algorithms (MD5, SHA-256) are completely broken for password storage, compare BCrypt, PBKDF2, and Argon2, and configure a memory-hard `Argon2PasswordEncoder`.

##### 2. What the Interviewer Evaluates
- Understanding why GPUs and ASICs can crack billions of SHA-256 hashes per second.
- Explaining CPU-hard (BCrypt) vs Memory-hard (Argon2) hashing functions.
- Winner of the Password Hashing Competition (Argon2id).

##### 3. Standout Technical Answer
Fast cryptographic hashes (SHA-256, SHA-512) are designed to hash large files quickly. Modern consumer GPUs can compute **10 billion SHA-256 hashes per second**, allowing rainbow table and brute-force attacks to crack an 8-character password in minutes.

Password hashing requires **slow, salted, iterative, and memory-hard algorithms**:
1. **BCrypt**: CPU-hard algorithm based on Blowfish. Computationally expensive, but has low memory footprint (~4KB), making it vulnerable to custom ASIC mining hardware.
2. **Argon2 (Argon2id)**: Winner of the Password Hashing Competition. It is **memory-hard**, requiring hundreds of megabytes of RAM per hash. This defeats GPU and ASIC parallel cracking rigs because memory bus bandwidth becomes the bottleneck.

```java
@Configuration
public class PasswordEncoderConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Argon2id Configuration:
        return new Argon2PasswordEncoder(
            16,     // salt length in bytes
            32,     // hash length in bytes
            2,      // parallelism (threads)
            65536,  // memory cost in KB (64 MB of RAM per hash)
            3       // iterations
        );
    }
}
```

| Algorithm | Hardness Type | ASIC/GPU Resistance | OWASP Recommended? |
|---|---|---|---|
| SHA-256 / SHA-512 | None (Fast) | ❌ Zero (Billions/sec) | ❌ Broken / Banned |
| BCrypt | CPU-hard | ⚠️ Medium | ✅ Acceptable (work factor 12) |
| PBKDF2 | CPU-hard | ⚠️ Medium | ✅ Acceptable (600,000 iterations) |
| **Argon2id** | **Memory + CPU Hard** | ✅ **Highest (Industry Standard)** | ✅ **Top Recommendation** |

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "If you increase BCrypt work factor from 10 to 15, what will happen to your login API throughput?"
- **Winning Answer**: "BCrypt's cost factor is **exponential** ($2^{cost}$). Factor 10 takes ~100ms per hash. Factor 15 takes $2^5 = 32\times$ longer (~3.2 seconds per hash!). Under 50 concurrent login attempts, all 50 Tomcat worker threads will peg the CPU at 100% for 3+ seconds, causing total thread exhaustion and cascading HTTP 504 timeouts."

---

#### Q16: Remember-Me Authentication — Persistent Tokens vs Cookie Tampering

##### 1. Exact Scenario & Question
A developer configures Spring Security's Remember-Me feature using `http.rememberMe()`. The security team rejects it, explaining that the default cookie contains a Base64-encoded username and expiration timestamp signed with an MD5 hash, vulnerable to cookie theft and replay attacks. Implement a secure, database-backed `PersistentTokenRepository`.

##### 2. What the Interviewer Evaluates
- Understanding vulnerabilities of simple hashing remember-me cookies.
- Implementing the sliding token pair algorithm (`series` and `token`) proposed by Colin Percival.
- Detecting stolen cookie usage.

##### 3. Standout Technical Answer
The default Spring Security remember-me cookie is: `base64(username + ":" + expiryTime + ":" + md5(username + ":" + expiryTime + ":" + password + ":" + key))`. If an attacker intercepts this cookie, they can impersonate the user until it expires.

The enterprise standard is **Persistent Token Authentication** (Sliding Tokens):
- A persistent table stores: `username`, `series` (random UUID), `token` (random UUID), and `last_used`.
- Every time the user visits with the cookie, a **new `token` is generated**, while the `series` stays constant.
- **Theft Detection**: If an attacker steals the cookie and uses it, the `token` changes on the server. When the real user returns with the old `token` but valid `series`, the server detects token reuse, invalidates all sessions for that user, and forces an immediate re-login!

```sql
-- PostgreSQL Persistent Logins Table
CREATE TABLE persistent_logins (
    username VARCHAR(64) NOT NULL,
    series VARCHAR(64) PRIMARY KEY,
    token VARCHAR(64) NOT NULL,
    last_used TIMESTAMP NOT NULL
);
```

```java
@Configuration
public class RememberMeSecurityConfig {

    private final DataSource dataSource;
    private final UserDetailsService userDetailsService;

    public RememberMeSecurityConfig(DataSource dataSource, UserDetailsService userDetailsService) {
        this.dataSource = dataSource;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PersistentTokenRepository persistentTokenRepository() {
        JdbcTokenRepositoryImpl tokenRepo = new JdbcTokenRepositoryImpl();
        tokenRepo.setDataSource(dataSource);
        return tokenRepo;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .rememberMe(remember -> remember
                .tokenRepository(persistentTokenRepository())
                .userDetailsService(userDetailsService)
                .tokenValiditySeconds(14 * 24 * 60 * 60) // 14 days
                .key("enterprise-remember-me-secret")
            )
            .build();
    }
}
```

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "If an employee changes their password, does their Persistent Remember-Me session stay valid?"
- **Winning Answer**: "Yes, unless you explicitly revoke it! Because Persistent Remember-Me authenticates via the database `series` and `token` without verifying the password on every request, the stolen cookie remains valid. In your `changePassword()` service method, you must explicitly call `tokenRepository.removeUserTokens(username)` to revoke all existing remember-me sessions."

---

### Tier 2: Scale & Production (Q17 – Q34)

#### Q17: Custom JWT Authentication Converter — Mapping Keycloak / Okta Realm Roles

##### 1. Exact Scenario & Question
Keycloak emits JWT tokens with roles nested inside a custom JSON claim: `realm_access.roles: ["ADMIN", "BILLING"]`. By default, Spring Security ignores this claim and only reads `scope` or `scp`. Write a production `JwtAuthenticationConverter` that extracts nested Keycloak roles, prefixes them with `ROLE_`, and maps them to `GrantedAuthority`.

##### 2. What the Interviewer Evaluates
- Understanding `JwtAuthenticationConverter` and `Converter<Jwt, Collection<GrantedAuthority>>`.
- Parsing nested JSON claims from `jwt.getClaimAsMap()`.
- Merging OAuth2 scopes with Keycloak realm roles.

##### 3. Standout Technical Answer
```java
public class KeycloakJwtGrantedAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Set<GrantedAuthority> authorities = new HashSet<>();

        // 1. Extract default OAuth2 Scopes (e.g. "SCOPE_read", "SCOPE_write")
        JwtGrantedAuthoritiesConverter defaultConverter = new JwtGrantedAuthoritiesConverter();
        authorities.addAll(defaultConverter.convert(jwt));

        // 2. Extract nested Keycloak Realm Roles: realm_access.roles
        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
        if (realmAccess != null && realmAccess.containsKey("roles")) {
            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) realmAccess.get("roles");
            for (String role : roles) {
                // Prefix with ROLE_ to satisfy hasRole("ADMIN") checks
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
            }
        }

        return authorities;
    }
}

// Attach to SecurityFilterChain:
@Bean
public SecurityFilterChain resourceServerFilterChain(HttpSecurity http) throws Exception {
    JwtAuthenticationConverter jwtAuthConverter = new JwtAuthenticationConverter();
    jwtAuthConverter.setJwtGrantedAuthoritiesConverter(new KeycloakJwtGrantedAuthoritiesConverter());

    return http
        .oauth2ResourceServer(oauth2 -> oauth2
            .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter))
        )
        .build();
}
```

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "What happens if Keycloak also emits client-specific roles in `resource_access.account.roles`?"
- **Winning Answer**: "The converter must also check `jwt.getClaimAsMap(\"resource_access\")`, extract client-specific maps, and append them as `ROLE_CLIENT_<ROLE_NAME>`. Failing to do so prevents using granular client-level permissions."

---

#### Q18: Token Revocation & Blacklisting — Distributed Redis Bloom Filters

##### 1. Exact Scenario & Question
Your microservice cluster handles 200,000 requests/sec. Every request carries a stateless JWT. You need to check if a token has been revoked (logout or user banned) before processing. Calling `redis.exists(tokenId)` on every request adds 2ms latency and hits Redis with 200,000 queries/sec. How do you implement high-throughput revocation checking using a **Redis Bloom Filter**?

##### 2. What the Interviewer Evaluates
- Understanding the operational bottlenecks of checking distributed caches on every request.
- Using probabilistic data structures (Bloom Filters) for membership queries.
- Managing false positives in security systems.

##### 3. Standout Technical Answer
A standard Redis key lookup (`GET / EXISTS`) incurs a network TCP round-trip and Redis CPU cycle for every incoming request.

**The Scalable Architecture: Two-Tier Bloom Filter Check:**
1. **Local / Redis Bloom Filter**: A Bloom filter answers: "Is this token ID definitely NOT revoked, or MIGHT it be revoked?" in O(1) time and minimal memory.
   - If Bloom Filter returns `false`: Token is **guaranteed 100% NOT revoked**. Proceed immediately without hitting Redis storage!
   - If Bloom Filter returns `true`: There is a small probability (~0.1%) of a false positive. **Only in this rare case** does the service execute a direct `redis.get("blacklist:" + jti)` query to confirm revocation!

```java
@Component
public class TokenRevocationValidator {

    private final RedisTemplate<String, String> redisTemplate;
    // Guava or RedisBloom module
    private final BloomFilter<String> localRevocationBloomFilter;

    public boolean isTokenRevoked(String jti) {
        // Fast-path: If Bloom Filter says NO, it is 100% NOT revoked!
        if (!localRevocationBloomFilter.mightContain(jti)) {
            return false; // Zero Redis network calls!
        }

        // Slow-path: Confirmation query to eliminate false positives
        return Boolean.TRUE.equals(redisTemplate.hasKey("revocation:blacklist:" + jti));
    }
}
```

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "Can an item be deleted from a standard Bloom Filter when a blacklisted token reaches its natural expiration date?"
- **Winning Answer**: "No! Standard Bloom Filters do not support deletions; resetting bits would remove other hashed keys. To support item removal upon token expiration, you must use a **Counting Bloom Filter** or use rotating rolling Bloom filters (e.g. one filter per 15-minute window) that are discarded as time progresses."

---

#### Q19: Spring Authorization Server — Building an In-House OAuth2 Provider

##### 1. Exact Scenario & Question
Your enterprise must host its own OpenID Connect (OIDC) compliant Identity Provider on-premise rather than using cloud providers. How do you configure the official **Spring Authorization Server** (the successor to Spring Security OAuth) with client registrations, RSA key pair generation, and OIDC discovery endpoints?

##### 2. What the Interviewer Evaluates
- Understanding Spring Authorization Server architecture.
- Configuring `RegisteredClientRepository` and `JWKSource<SecurityContext>`.
- Exposing standard endpoints (`/oauth2/authorize`, `/oauth2/token`, `/.well-known/openid-configuration`).

##### 3. Standout Technical Answer
```java
@Configuration
public class AuthorizationServerConfig {

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
        http.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
            .oidc(Customizer.withDefaults()); // Enable OpenID Connect 1.0

        return http
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login"))
            )
            .build();
    }

    @Bean
    public RegisteredClientRepository registeredClientRepository(PasswordEncoder passwordEncoder) {
        RegisteredClient internalServiceClient = RegisteredClient.withId(UUID.randomUUID().toString())
            .clientId("payment-service-client")
            .clientSecret(passwordEncoder.encode("secret-key-123"))
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
            .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
            .redirectUri("https://payment.enterprise.com/login/oauth2/code/oidc")
            .scope(OidcScopes.OPENID)
            .scope(OidcScopes.PROFILE)
            .scope("payments.write")
            .tokenSettings(TokenSettings.builder()
                .accessTokenTimeToLive(Duration.ofMinutes(15))
                .refreshTokenTimeToLive(Duration.ofDays(7))
                .reuseRefreshTokens(false) // Rotate refresh tokens!
                .build())
            .build();

        return new InMemoryRegisteredClientRepository(internalServiceClient);
    }

    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        RSAKey rsaKey = JwkGenerator.generateRsa(); // Generates 2048-bit RSA key pair
        JWKSet jwkSet = new JWKSet(rsaKey);
        return (jwkSelector, context) -> jwkSelector.select(jwkSet);
    }
}
```

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "Why will an in-memory `JWKSource` cause all active client tokens to become invalid when the Authorization Server restarts?"
- **Winning Answer**: "Because `generateRsa()` generates a **new random RSA key pair in RAM** on every application restart. All previously issued JWTs were signed with the old private key. When resource servers download the new public key from JWKS, all previous signatures fail verification. Production setups must load fixed RSA keys from a secure store (e.g. HashiCorp Vault or Kubernetes Secrets)."

---

#### Q20: Secure Session Management — Session Fixation & Concurrency Limits

##### 1. Exact Scenario & Question
Explain how an attacker executes a **Session Fixation Attack**. How does Spring Security protect against session fixation by default, and how do you configure concurrent session control to restrict each user to at most 1 active login session?

##### 2. What the Interviewer Evaluates
- Understanding Session Fixation mechanics (attacker forces a known `JSESSIONID` onto victim).
- Differentiating between `changeSessionId()` (Servlet 3.1+ default) and `migrateSession()`.
- Configuring `ConcurrentSessionControlAuthenticationStrategy`.

##### 3. Standout Technical Answer
**Session Fixation Attack:**
1. Attacker obtains a clean session ID from `bank.com` (`JSESSIONID = 12345`).
2. Attacker tricks victim into clicking a link: `https://bank.com/login?JSESSIONID=12345`.
3. Victim enters their credentials and logs in.
4. If the server does NOT change the session ID upon login, the session is now authenticated under the victim's account, and the attacker uses the known `12345` ID to steal the account!

**Spring Security Protection:**
Upon successful authentication, Spring Security invokes `SessionFixationProtectionStrategy`. By default, it uses `changeSessionId()` (preserving session attributes while issuing a new random session ID to the browser).

```java
@Bean
public SecurityFilterChain sessionManagementFilterChain(HttpSecurity http) throws Exception {
    return http
        .sessionManagement(session -> session
            // 1. Session Fixation Defense
            .sessionFixation(SessionManagementConfigurer.SessionFixationConfigurer::changeSessionId)
            
            // 2. Limit user to max 1 concurrent session
            .maximumSessions(1)
            // If true: rejects 2nd login attempt; If false: kicks out 1st user
            .maxSessionsPreventsLogin(true)
            .sessionRegistry(sessionRegistry())
        )
        .build();
}

@Bean
public SessionRegistry sessionRegistry() {
    return new SessionRegistryImpl();
}

@Bean
public HttpSessionEventPublisher httpSessionEventPublisher() {
    // Crucial: Publishes session destroy events to update SessionRegistry!
    return new HttpSessionEventPublisher();
}
```

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "What happens if you configure `maximumSessions(1)` but forget to register the `HttpSessionEventPublisher` bean?"
- **Winning Answer**: "When users log out or their session times out, the `SessionRegistry` is never notified that the session closed. The server permanently believes the user still has an active session. When the user tries to log in again later, they are permanently blocked with `Maximum sessions exceeded`!"

---

#### Q21: WebSocket Security — STOMP Interceptors & Channel Authentication

##### 1. Exact Scenario & Question
In a real-time trading application using Spring WebSockets and STOMP over SockJS, a user connects via WebSocket. Traditional HTTP filters only execute during the initial HTTP handshake. How do you authenticate and authorize individual STOMP message frames (`CONNECT`, `SUBSCRIBE`, `SEND`) using `ChannelInterceptor`?

##### 2. What the Interviewer Evaluates
- Understanding that WebSockets upgrade from HTTP to TCP, bypassing subsequent HTTP filters.
- Intercepting STOMP command frames via `ChannelInterceptor`.
- Authenticating JWTs passed in STOMP `CONNECT` headers.

##### 3. Standout Technical Answer
```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketSecurityConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtDecoder jwtDecoder;

    public WebSocketSecurityConfig(JwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                // Intercept STOMP CONNECT frame
                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    String authHeader = accessor.getFirstNativeHeader("Authorization");
                    if (authHeader != null && authHeader.startsWith("Bearer ")) {
                        String token = authHeader.substring(7);
                        Jwt jwt = jwtDecoder.decode(token);

                        // Build authentication and inject into STOMP session
                        Authentication auth = new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority("ROLE_USER")));
                        accessor.setUser(auth);
                    } else {
                        throw new AuthenticationCredentialsNotFoundException("Missing JWT in STOMP headers");
                    }
                }

                // Intercept SUBSCRIBE frame to protect private trading channels
                if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
                    String destination = accessor.getDestination();
                    Principal principal = accessor.getUser();

                    if (destination != null && destination.startsWith("/user/") && principal == null) {
                        throw new AccessDeniedException("Unauthorized subscription to private channel");
                    }
                }

                return message;
            }
        });
    }
}
```

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "Can browser JavaScript pass custom HTTP headers (like `Authorization: Bearer ...`) during the standard native WebSocket handshake (`new WebSocket(...)`)?"
- **Winning Answer**: "No! The browser W3C WebSocket API does **not** allow adding custom headers during the HTTP upgrade request. This is why standard web apps must either pass the JWT in the initial STOMP `CONNECT` protocol frame, use a short-lived one-time ticket query parameter (`ws://api.com?ticket=XYZ`), or use cookie-based authentication."

---

#### Q22: Reactive Security in WebFlux — ReactiveSecurityContextHolder

##### 1. Exact Scenario & Question
A developer writes a custom WebFilter in Spring WebFlux:
```java
SecurityContext context = SecurityContextHolder.getContext();
```
In production, `context.getAuthentication()` is intermittently `null` or returns another user's authentication. Explain how `ReactiveSecurityContextHolder` uses Project Reactor's subscriber `Context` and write a non-blocking security filter.

##### 2. What the Interviewer Evaluates
- Explaining why `ThreadLocal` fails in non-blocking event-driven architectures.
- Using `ReactiveSecurityContextHolder.getContext()`.
- Applying `SecurityWebFilterChain` with ServerHttpSecurity.

##### 3. Standout Technical Answer
In Spring WebFlux, a single HTTP request hops between different Netty EventLoop worker threads. `ThreadLocal` cannot track asynchronous execution pipelines.

Spring Security Reactive solves this by storing the `SecurityContext` in the immutable **Reactor `Context`**:

```java
@Configuration
@EnableWebFluxSecurity
public class ReactiveSecurityConfiguration {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .authorizeExchange(exchanges -> exchanges
                .pathMatchers("/public/**").permitAll()
                .pathMatchers("/admin/**").hasAuthority("ROLE_ADMIN")
                .anyExchange().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
            .build();
    }
}

// Accessing Security Context Reactively inside a Service:
@Service
public class ReactiveOrderService {

    public Mono<Order> placeOrder(OrderRequest request) {
        return ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .map(Principal::getName)
            .flatMap(username -> executeOrder(username, request));
    }
}
```

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "If you call an external reactive library on `Schedulers.parallel()`, does the `ReactiveSecurityContextHolder` remain accessible?"
- **Winning Answer**: "Yes! Because the Reactor `Context` is bound to the reactive subscriber stream rather than an OS thread, the security context flows seamlessly across all scheduler transitions (`publishOn` and `subscribeOn`). It is only lost if you break the reactive chain (e.g. creating an unmanaged raw Java `Thread`)."

---

#### Q23: Security Response Headers — HSTS, CSP, and Clickjacking

##### 1. Exact Scenario & Question
Your enterprise penetration test fails with vulnerabilities: (1) Missing Content Security Policy (CSP), (2) Clickjacking via IFRAME embedding, and (3) Missing HTTP Strict Transport Security (HSTS). Show how Spring Security configures these security response headers declaratively.

##### 2. What the Interviewer Evaluates
- Configuring `HSTS` (`Strict-Transport-Security`).
- Preventing Clickjacking using `X-Frame-Options: DENY` and CSP `frame-ancestors 'none'`.
- Restricting script injection via `Content-Security-Policy`.

##### 3. Standout Technical Answer
```java
@Bean
public SecurityFilterChain securityHeadersFilterChain(HttpSecurity http) throws Exception {
    return http
        .headers(headers -> headers
            // 1. Enforce HSTS (Force HTTPS for 1 year including subdomains)
            .httpStrictTransportSecurity(hsts -> hsts
                .includeSubDomains(true)
                .maxAgeInSeconds(31536000)
                .preload(true)
            )
            // 2. Prevent Clickjacking
            .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
            
            // 3. Prevent MIME-sniffing
            .contentTypeOptions(Customizer.withDefaults())
            
            // 4. Content Security Policy (CSP)
            .contentSecurityPolicy(csp -> csp
                .policyDirectives("default-src 'self'; script-src 'self' https://trustedscripts.com; frame-ancestors 'none';")
            )
            // 5. Referrer Policy
            .referrerPolicy(referrer -> referrer
                .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
            )
        )
        .build();
}
```

| Security Header | Value Generated | Protection Provided |
|---|---|---|
| `Strict-Transport-Security` | `max-age=31536000; includeSubDomains; preload` | Prevents SSL Stripping attacks |
| `X-Frame-Options` | `DENY` | Prevents Clickjacking |
| `X-Content-Type-Options` | `nosniff` | Prevents browser MIME-type sniffing |
| `Content-Security-Policy` | `default-src 'self'; ...` | Prevents XSS, injection, data exfiltration |

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "Why is `X-Frame-Options: DENY` superseded by CSP `frame-ancestors 'none'` in modern browsers?"
- **Winning Answer**: "`X-Frame-Options` is legacy and only supports binary `DENY` or `SAMEORIGIN`. It cannot permit embedding from specific trusted third-party partners. CSP `frame-ancestors 'self' https://partner.com` allows fine-grained domain whitelisting and is the modern W3C standard."

---

#### Q24: Mutual TLS (mTLS) & X.509 Client Certificate Authentication

##### 1. Exact Scenario & Question
You are securing inter-service communications in a high-security banking network where API tokens are insufficient: services must authenticate using hardware-backed X.509 certificates. How do you configure Spring Security to authenticate client certificates via mTLS and extract the service identity from the certificate Subject DN?

##### 2. What the Interviewer Evaluates
- Understanding the mTLS handshake (server authenticates client certificate).
- Configuring `x509()` in Spring Security.
- Mapping certificate Subject Distinguished Name (DN) to Spring `UserDetails`.

##### 3. Standout Technical Answer
```yaml
# application.yml: Configure Embedded Tomcat / Netty for mTLS
server:
  port: 8443
  ssl:
    enabled: true
    client-auth: need # Enforce mandatory client certificate during TLS handshake
    key-store: classpath:server-keystore.p12
    key-store-password: serverPassword
    trust-store: classpath:trusted-ca-truststore.p12
    trust-store-password: trustPassword
```

```java
@Configuration
@EnableWebSecurity
public class MtlsSecurityConfig {

    @Bean
    public SecurityFilterChain mTcpFilterChain(HttpSecurity http) throws Exception {
        return http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/internal/**").hasRole("SERVICE")
                .anyRequest().authenticated()
            )
            // Enable X.509 Client Certificate Authentication:
            .x509(x509 -> x509
                .subjectPrincipalRegex("CN=(.*?)(?:,|$)") // Extract Common Name (CN) from cert DN
                .userDetailsService(x509UserDetailsService())
            )
            .build();
    }

    @Bean
    public UserDetailsService x509UserDetailsService() {
        return commonName -> {
            // Example CN: "payment-batch-service"
            if ("payment-batch-service".equalsIgnoreCase(commonName)) {
                return User.withUsername(commonName)
                    .password("") // No password; identity proven via TLS private key!
                    .roles("SERVICE", "BATCH")
                    .build();
            }
            throw new UsernameNotFoundException("Unauthorized client certificate CN: " + commonName);
        };
    }
}
```

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "If an AWS Application Load Balancer (ALB) terminates mTLS, how does the Spring Boot application behind it receive the client certificate?"
- **Winning Answer**: "When an ALB or reverse proxy terminates mTLS, the physical TLS handshake happens at the ALB. The ALB forwards the validated client certificate to the backend Spring Boot instance inside an HTTP header (e.g. `X-Amzn-Mtls-Clientcert` or `X-SSL-Cert`). In Spring Security, you must configure a custom filter to parse the certificate from the header and verify it arrived from a trusted private subnet."

---

#### Q25: Open Policy Agent (OPA) Integration — Dynamic Policy Evaluation

##### 1. Exact Scenario & Question
Your company standardizes on Open Policy Agent (OPA) with Rego policies for unified enterprise authorization across Kubernetes and microservices. How do you replace Spring Security's static `@PreAuthorize` with a dynamic `AuthorizationManager<RequestAuthorizationContext>` that evaluates authorization requests against an OPA sidecar?

##### 2. What the Interviewer Evaluates
- Understanding Spring Security 6's unified `AuthorizationManager` SPI.
- Calling OPA REST API (`POST /v1/data/httpapi/authz`) non-blockingly.
- Passing principal, method, path, and payload context to Rego.

##### 3. Standout Technical Answer
```java
@Component
public class OpaAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    private final RestClient opaRestClient;

    public OpaAuthorizationManager(RestClient.Builder builder) {
        this.opaRestClient = builder.baseUrl("http://localhost:8181/v1/data/httpapi/authz").build();
    }

    @Override
    public AuthorizationDecision check(Supplier<Authentication> authentication, RequestAuthorizationContext context) {
        HttpServletRequest request = context.getRequest();
        Authentication auth = authentication.get();

        // Build payload for OPA Rego evaluation
        Map<String, Object> input = Map.of(
            "user", auth.getName(),
            "roles", auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList(),
            "method", request.getMethod(),
            "path", request.getRequestURI()
        );

        OpaResponse response = opaRestClient.post()
            .body(Map.of("input", input))
            .retrieve()
            .body(OpaResponse.class);

        boolean allowed = response != null && response.result();
        return new AuthorizationDecision(allowed);
    }

    public record OpaResponse(boolean result) {}
}

// Register in SecurityFilterChain:
@Bean
public SecurityFilterChain filterChain(HttpSecurity http, OpaAuthorizationManager opaManager) throws Exception {
    return http
        .authorizeHttpRequests(auth -> auth
            .anyRequest().access(opaManager) // All endpoints governed by OPA Rego policies!
        )
        .build();
}
```

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "What happens if the OPA sidecar crashes or becomes unreachable?"
- **Winning Answer**: "Spring Security enforces **Fail-Closed** security by default. If `opaRestClient` throws an exception or fails, `AuthorizationDecision(false)` is returned (or an exception is propagated), immediately rejecting incoming traffic with HTTP 403. Authorization must never fail-open."

---

#### Q26: Securing Spring Boot Actuator Endpoints

##### 1. Exact Scenario & Question
A penetration test reveals that `/actuator/env` and `/actuator/heapdump` are publicly accessible, exposing database passwords and customer data from memory. How do you isolate Actuator endpoints to an internal management port and restrict sensitive endpoints?

##### 2. What the Interviewer Evaluates
- Segregating management traffic onto a separate network port (`management.server.port`).
- Securing Actuator endpoints via dedicated `SecurityFilterChain`.
- Sanitizing sensitive environment properties.

##### 3. Standout Technical Answer
```yaml
# application.yml: Port Isolation & Endpoint Exposure
management:
  server:
    port: 8081 # Separate internal port (Not exposed to public internet!)
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus # Expose only operational endpoints
  endpoint:
    health:
      show-details: when_authorized
  env:
    show-values: NEVER # Mask all passwords and secret keys
```

```java
@Configuration
public class ActuatorSecurityConfig {

    // Separate SecurityFilterChain strictly for management port (8081)
    @Bean
    @Order(1)
    public SecurityFilterChain actuatorFilterChain(HttpSecurity http) throws Exception {
        return http
            .securityMatcher(EndpointRequest.toAnyEndpoint())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(EndpointRequest.to(HealthEndpoint.class, InfoEndpoint.class)).permitAll()
                .anyRequest().hasRole("SRE_OPS") // All other actuator endpoints require SRE_OPS
            )
            .httpBasic(Customizer.withDefaults())
            .build();
    }
}
```

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "Can an attacker access sensitive heap dumps via `/actuator/heapdump` if `management.server.port` is isolated in Kubernetes?"
- **Winning Answer**: "If the Kubernetes Service and Ingress only route port 8080 to the public, external attackers cannot access port 8081. However, internal attackers or compromised pods on the same VPC can still access port 8081 unless NetworkPolicies and HTTP Basic/mTLS authentication are enforced on the management port."

---

#### Q27: OAuth2 Client Credentials Flow — Machine-to-Machine Authentication

##### 1. Exact Scenario & Question
A backend cron job microservice must call a third-party billing API every hour without any human user interaction. Implement the **OAuth2 Client Credentials Flow** using Spring Security's `OAuth2AuthorizedClientManager` and `WebClient`.

##### 2. What the Interviewer Evaluates
- Understanding the Client Credentials grant type (Machine-to-Machine).
- Configuring `AuthorizedClientServiceOAuth2AuthorizedClientManager`.
- Attaching `ServletOAuth2AuthorizedClientExchangeFilterFunction` to `WebClient`.

##### 3. Standout Technical Answer
```yaml
# application.yml: OAuth2 Client Registration
spring:
  security:
    oauth2:
      client:
        registration:
          billing-client:
            client-id: billing-cron-service
            client-secret: super-secret-billing-key
            authorization-grant-type: client_credentials
            scope: invoices.read,invoices.write
        provider:
          billing-client:
            token-uri: https://auth.enterprise.com/oauth2/token
```

```java
@Configuration
public class OAuth2WebClientConfig {

    @Bean
    public WebClient billingWebClient(OAuth2AuthorizedClientManager authorizedClientManager) {
        ServletOAuth2AuthorizedClientExchangeFilterFunction oauth2Filter =
            new ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);
        
        // Default to billing-client registration
        oauth2Filter.setDefaultClientRegistrationId("billing-client");

        return WebClient.builder()
            .baseUrl("https://billing-api.enterprise.com")
            .apply(oauth2Filter.oauth2Configuration()) // Automatically fetches and injects Bearer token!
            .build();
    }

    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManager(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientService authorizedClientService) {

        OAuth2AuthorizedClientProvider authorizedClientProvider =
            OAuth2AuthorizedClientProviderBuilder.builder()
                .clientCredentials() // Automatically manages token expiration and background refresh!
                .build();

        AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager =
            new AuthorizedClientServiceOAuth2AuthorizedClientManager(
                clientRegistrationRepository, authorizedClientService);

        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);
        return authorizedClientManager;
    }
}
```

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "Does the Client Credentials grant type issue a Refresh Token?"
- **Winning Answer**: "No! The OAuth 2.0 specification explicitly states that the Client Credentials grant does **not** issue a refresh token. Because the client holds its own `client_secret`, when an access token expires, the client simply makes another `POST /token` request with its credentials to obtain a fresh access token."

---

#### Q28: Refresh Token Rotation (RTR) & Reuse Detection

##### 1. Exact Scenario & Question
A mobile application stores a refresh token. An attacker steals the refresh token via physical device compromise. How does **Refresh Token Rotation (RTR) with Automatic Reuse Detection** catch the attack and immediately invalidate all tokens belonging to the legitimate user?

##### 2. What the Interviewer Evaluates
- Understanding OAuth 2.1 Refresh Token Rotation requirements.
- Detecting token family reuse.
- Revoking entire token lineage when replay occurs.

##### 3. Standout Technical Answer
In classic OAuth2, a refresh token was static and long-lived. If stolen, an attacker could maintain access indefinitely.

**Refresh Token Rotation (RTR) Protocol:**
1. Every time a client exchanges a refresh token for an access token, the Authorization Server **invalidates the old refresh token and issues a brand new one** (Sliding Window).
2. **Automatic Reuse Detection**:
   - Each refresh token belongs to a **Token Family**.
   - If an attacker intercepts Token $R_1$ and exchanges it, the server issues Token $R_2$ to the attacker.
   - When the legitimate mobile app attempts to exchange Token $R_1$, the server detects that Token $R_1$ was **already used**!
   - The server immediately flags the entire Token Family as compromised and **revokes all access tokens and refresh tokens across all devices** for that user, forcing a full credential re-authentication.

```java
// Spring Authorization Server Configuration:
TokenSettings.builder()
    .accessTokenTimeToLive(Duration.ofMinutes(15))
    .refreshTokenTimeToLive(Duration.ofDays(30))
    .reuseRefreshTokens(false) // MANDATORY for RTR: Issues new refresh token on every exchange!
    .build();
```

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "What concurrency issue can trigger false-positive reuse detection in a mobile app with network lag?"
- **Winning Answer**: "If a mobile app fires two concurrent API requests when its token expires, both requests may attempt to refresh using the same refresh token simultaneously. To prevent race conditions, Authorization Servers implement a **grace period** (e.g. 10 seconds) where multiple exchanges of the same refresh token return the already-issued successor token without triggering reuse revocation."

---

#### Q29: Cross-Site Scripting (XSS) Defense Layers in Spring Security

##### 1. Exact Scenario & Question
A developer asks: "Does Spring Security automatically sanitize user input against Stored and Reflected Cross-Site Scripting (XSS)?" Clarify Spring Security's boundaries regarding XSS and detail the defense-in-depth architecture.

##### 2. What the Interviewer Evaluates
- Understanding that Spring Security is an authentication/authorization framework, NOT an HTML input sanitizer.
- Configuring Content Security Policy (CSP).
- Using OWASP Java HTML Sanitizer / Jackson serializers.

##### 3. Standout Technical Answer
Spring Security does **NOT** automatically sanitize incoming JSON or form parameters against XSS attacks.
- Input validation and HTML output encoding belong to the application layer and frontend templates.

**The 3-Layer Enterprise XSS Defense Architecture:**
1. **Layer 1 (Browser Mitigation - Spring Security)**:
   - Configure a strict Content Security Policy (CSP):
     ```java
     headers.contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'; script-src 'self';"));
     ```
2. **Layer 2 (Output Encoding - Frontend / Template)**:
   - Modern frontend frameworks (React, Angular) automatically escape HTML data expressions (`{userInput}`).
3. **Layer 3 (Input Sanitization - Backend Jackson Deserializer)**:
   - Sanitize rich-text input using OWASP Java HTML Sanitizer:
     ```java
     public class XssSanitizerDeserializer extends JsonDeserializer<String> {
         private final PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);
         @Override
         public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
             return policy.sanitize(p.getText());
         }
     }
     ```

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "Why is `X-XSS-Protection: 1; mode=block` deprecated in modern browsers?"
- **Winning Answer**: "Because older browser XSS auditors were flawed and introduced new side-channel vulnerabilities (allowing attackers to steal user data via auditor blocking). Modern security guidelines mandate using a strong **Content Security Policy (CSP)** instead."

---

#### Q30: Single Logout (SLO) & Back-Channel Logout in OIDC

##### 1. Exact Scenario & Question
An enterprise user logs out of the central Keycloak Identity Provider. However, their active session in your Spring Boot application remains logged in for another 4 hours. How does **OIDC Back-Channel Logout (OpenID Connect Back-Channel Logout 1.0)** propagate logout events directly between servers?

##### 2. What the Interviewer Evaluates
- Differentiating Front-Channel (browser redirect) vs Back-Channel (direct server-to-server POST) logout.
- Validating the `logout_token` JWT.
- Invalidating local user sessions.

##### 3. Standout Technical Answer
In **Front-Channel Logout**, the user's browser is redirected through IFRAMEs to each application, which frequently fails if users close their browser or third-party cookies are blocked.

In **OIDC Back-Channel Logout**:
1. When a user logs out of Keycloak, the IdP sends a direct HTTP `POST` request to each registered application's logout endpoint (e.g. `/logout/connect/back-channel/{registrationId}`).
2. The payload is a signed JWT called a **`logout_token`**, containing:
   - `sub` (Subject ID)
   - `sid` (Session ID)
   - `events: { "http://schemas.openid.net/event/backchannel-logout": {} }`
3. Spring Security validates the `logout_token` signature against the IdP's JWKS and invalidates the local session matching that `sid` in the `SessionRegistry`.

```java
@Configuration
public class OidcLogoutSecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, 
                                           ClientRegistrationRepository clientRegistrationRepository) throws Exception {
        return http
            .oidcLogout(oidc -> oidc
                .backChannel(Customizer.withDefaults()) // Enables Spring Security OIDC Back-Channel endpoint!
            )
            .build();
    }
}
```

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "Does a `logout_token` contain a `nonce` claim?"
- **Winning Answer**: "No! The OIDC Back-Channel Logout specification explicitly forbids a `nonce` claim inside a `logout_token` to distinguish it from standard ID tokens and prevent token substitution attacks."

---

#### Q31: Zero-Downtime Secret & Key Rotation

##### 1. Exact Scenario & Question
Your microservice verifies symmetric HMAC-SHA256 tokens (`HS256`) signed with a shared secret. You need to rotate the secret key without invalidating active tokens issued in the last 24 hours. How do you implement dual-key validation?

##### 2. What the Interviewer Evaluates
- Managing active key vs retirement key transitions.
- Configuring multiple signing keys in `JwtDecoder`.
- Rolling deployments during secret updates.

##### 3. Standout Technical Answer
```java
@Configuration
public class DualKeyJwtDecoderConfig {

    @Value("${security.jwt.active-secret}")
    private String activeSecret;

    @Value("${security.jwt.previous-secret}")
    private String previousSecret;

    @Bean
    public JwtDecoder dualKeyJwtDecoder() {
        SecretKey activeKey = new SecretKeySpec(activeSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        SecretKey previousKey = new SecretKeySpec(previousSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");

        NimbusJwtDecoder activeDecoder = NimbusJwtDecoder.withSecretKey(activeKey).build();
        NimbusJwtDecoder previousDecoder = NimbusJwtDecoder.withSecretKey(previousKey).build();

        // Dual-Key Verification: Try active key first; fallback to previous key!
        return token -> {
            try {
                return activeDecoder.decode(token);
            } catch (JwtException ex) {
                // If active key fails signature check, fallback to previous key
                return previousDecoder.decode(token);
            }
        };
    }
}
```

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "Why is asymmetric cryptography (RS256 / ES256) vastly superior to symmetric keys (HS256) for key rotation?"
- **Winning Answer**: "With asymmetric keys, public keys are fetched dynamically from the JWKS endpoint using the token's `kid` (Key ID) header. The Identity Provider can publish both old and new public keys simultaneously in `jwks.json`. Resource servers rotate keys automatically without requiring code changes or dual-key fallbacks."

---

#### Q32: Asynchronous Security Context Propagation with `@Async`

##### 1. Exact Scenario & Question
A developer writes:
```java
@Async
public void sendSecurityNotification() {
    String user = SecurityContextHolder.getContext().getAuthentication().getName(); // NullPointerException!
}
```
Why is the SecurityContext null inside `@Async` methods, and how do you configure Spring to propagate security contexts across asynchronous executors?

##### 2. What the Interviewer Evaluates
- Explaining `@Async` thread switching.
- Wrapping executors with `DelegatingSecurityContextAsyncTaskExecutor`.
- Cleaning up thread contexts after task completion.

##### 3. Standout Technical Answer
`@Async` executes tasks on a separate thread pool managed by Spring's `TaskExecutor`. Because `SecurityContextHolder` uses a standard `ThreadLocal`, the new worker thread starts with an empty context.

```java
@Configuration
@EnableAsync
public class AsyncSecurityConfig implements AsyncConfigurer {

    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(20);
        executor.setThreadNamePrefix("async-exec-");
        executor.initialize();

        // Wraps all @Async invocations to propagate SecurityContext safely:
        return new DelegatingSecurityContextAsyncTaskExecutor(executor);
    }
}
```

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "What happens if an asynchronous task mutates the SecurityContext (e.g. calling `SecurityContextHolder.setContext(...)`)?"
- **Winning Answer**: "The mutation only affects that individual worker thread's copied context. The calling web request thread's context remains unmodified. When the task completes, `DelegatingSecurityContextAsyncTaskExecutor` automatically clears the worker thread's context to prevent leaking state to the next task."

---

#### Q33: API Gateway Security — The Token Relay Pattern

##### 1. Exact Scenario & Question
In a microservices architecture using Spring Cloud Gateway and Spring Boot backend services, how do you implement the **Token Relay Pattern** so that the Gateway authenticates external users and securely propagates Bearer tokens to downstream microservices?

##### 2. What the Interviewer Evaluates
- Configuring `TokenRelayGatewayFilterFactory`.
- Securing internal microservices behind a private subnet.
- Stripping sensitive client credentials at the gateway.

##### 3. Standout Technical Answer
```yaml
# Spring Cloud Gateway application.yml Configuration
spring:
  cloud:
    gateway:
      routes:
        - id: order-service-route
          uri: lb://ORDER-SERVICE
          predicates:
            - Path=/api/orders/**
          filters:
            # Automatically extracts the OAuth2 Access Token from the user's session
            # and injects it as 'Authorization: Bearer <token>' into downstream requests!
            - TokenRelay=
            - RemoveRequestHeader=Cookie # Strip external cookies before forwarding
```

```java
// Downstream Microservice: Simple OAuth2 Resource Server
@Bean
public SecurityFilterChain internalServiceSecurity(HttpSecurity http) throws Exception {
    return http
        .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
        .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
        .build();
}
```

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "What security risk exists if an internal microservice accepts tokens directly without checking if the request originated from the API Gateway?"
- **Winning Answer**: "If an attacker penetrates the internal network (e.g. via another compromised container), they can bypass gateway rate limiting, WAF inspection, and route restrictions. Internal services should enforce **Mutual TLS (mTLS)** or validate an internal gateway signature header (like a shared HMAC secret) to ensure requests exclusively originate from the API Gateway."

---

#### Q34: HashiCorp Vault Integration — Dynamic DB Credential Rotation

##### 1. Exact Scenario & Question
Static database passwords in `application.yml` violate enterprise compliance. How do you integrate Spring Cloud Vault so that Spring Security and Spring Data JPA dynamically fetch short-lived database credentials (TTL = 1 hour) and rotate them automatically without restarting the application?

##### 2. What the Interviewer Evaluates
- Integrating Spring Cloud Vault.
- Generating lease-based dynamic PostgreSQL credentials.
- Rotating database connection pools in HikariCP dynamically.

##### 3. Standout Technical Answer
```yaml
# bootstrap.yml: Spring Cloud Vault Configuration
spring:
  cloud:
    vault:
      uri: https://vault.enterprise.com:8200
      authentication: KUBERNETES
      database:
        enabled: true
        role: order-service-db-role
        backend: database
```

*How Dynamic Rotation Works:*
1. At application startup, Spring Cloud Vault authenticates via the Kubernetes Service Account token.
2. Vault generates a unique, temporary database username and password in PostgreSQL: `v-kub-order-ser-xyz123` with a 1-hour TTL.
3. Spring Cloud Vault injects these credentials into HikariCP.
4. When the lease reaches 75% of its lifetime (45 minutes), Spring Cloud Vault automatically renews the lease with Vault. If renewal fails, it generates a new credential set and smoothly rotates HikariCP's physical connection pool without dropping in-flight requests.

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "What happens to active long-running database queries when Vault revokes an expired database credential?"
- **Winning Answer**: "Vault issues a PostgreSQL `REVOKE` and `DROP USER` command. If active connections are executing queries under that user, PostgreSQL will terminate those connections. HikariCP must be configured with `maxLifetime < Vault TTL` so connections are recycled before Vault terminates the user."

---

### Tier 3: Staff/Principal Architecture, Zero-Trust & Incident Response (Q35 – Q50)

#### Q35: Zero-Trust Network Architecture (ZTNA) in Spring Microservices

##### 1. Exact Scenario & Question
Your CTO mandates a transition to **Zero-Trust Network Architecture (ZTNA)**: "Never trust, always verify." Perimeter firewalls are no longer considered sufficient. How must Spring Security and microservice communications be architected to enforce Zero-Trust principles?

##### 2. What the Interviewer Evaluates
- Understanding ZTNA pillars (identity-centric, per-request verification, least privilege, encrypted transport).
- Combining mTLS (transport identity) with OAuth2 JWTs (user/application identity).
- Enforcing microsegmentation.

##### 3. Standout Technical Answer
In a Zero-Trust architecture:
1. **Network Identity (mTLS)**: Every microservice has an X.509 certificate managed by an internal CA (or Istio/SPIFFE). Service A cannot establish a TCP socket with Service B unless both certificates are verified.
2. **User Identity (JWT)**: Every request carries a cryptographically signed OAuth2 token representing the human user or initiating client.
3. **Continuous Authorization**: Every individual service independently evaluates authorization rules (`@PreAuthorize`) rather than blindly trusting an upstream gateway.

```
[Service A] ─── (1. mTLS Handshake: Mutual Proof of Service Identity) ───► [Service B]
            ─── (2. Bearer JWT: Proof of End-User Authority) ────────────►
            ─── (3. Service B verifies both before executing logic) ─────►
```

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "Why is passing a user JWT alone insufficient for Zero-Trust in microservices?"
- **Winning Answer**: "Because a compromised service could forward a legitimate user's JWT to another service it should never communicate with (Confused Deputy Problem). Zero-Trust requires **both** Service-to-Service identity verification (mTLS / Audience restriction) and User Identity verification (JWT)."

---

#### Q36: JWT Cryptographic Attacks — The `none` Algorithm & Key Confusion

##### 1. Exact Scenario & Question
Explain two of the most infamous cryptographic vulnerabilities in naive JWT implementations: (1) The `alg: "none"` bypass attack, and (2) The RSA-HMAC Key Confusion Attack. How does Spring Security's `NimbusJwtDecoder` prevent these attacks?

##### 2. What the Interviewer Evaluates
- Understanding JWT header manipulation attacks.
- Explaining how attackers use public RSA keys as HMAC secrets.
- Configuring strict expected algorithms in Spring Security.

##### 3. Standout Technical Answer
1. **The `alg: "none"` Attack**:
   - The attacker takes a legitimate token, changes the payload to `{"role": "ADMIN"}`, and sets the header `{"alg": "none"}`.
   - Vulnerable libraries skip signature verification when `alg` is `"none"`, granting immediate root access!
   - **Defense**: Spring Security explicitly forbids `none`. If `alg: none` is passed, `NimbusJwtDecoder` rejects it with `BadJwtException`.
2. **The RSA-to-HMAC Key Confusion Attack**:
   - The server expects an asymmetric RSA signature (`RS256`) using a private key, verified with a public key.
   - The attacker changes the token header to symmetric `{"alg": "HS256"}` and signs the token using the server's **Public RSA Key as the HMAC secret key**!
   - Vulnerable servers look up the verification key (the public key), treat it as an HMAC byte array, and verify the attacker's signature successfully!
   - **Defense**: Spring Security strictly enforces algorithm pre-configuration:
     ```java
     NimbusJwtDecoder.withJwkSetUri(jwksUri)
         .jwsAlgorithm(SignatureAlgorithm.RS256) // ONLY permits RS256; rejects HS256!
         .build();
     ```

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "Can an attacker exploit timing attacks against JWT HMAC verification?"
- **Winning Answer**: "Yes! If signature verification uses standard `String.equals()`, it exits on the first mismatched byte. An attacker can measure nanosecond timing differences to guess signature bytes one by one. Spring Security uses `MessageDigest.isEqual()`, which executes in **constant time** regardless of where differences occur."

---

#### Q37: DDoS Mitigation at the Security Filter Level

##### 1. Exact Scenario & Question
During a distributed denial-of-service attack, attackers send millions of requests with malformed, multi-megabyte JWT tokens. Before Spring Security can reject them, the JVM runs out of CPU parsing massive cryptographic signatures. How do you protect the security filter chain against algorithmic complexity attacks?

##### 2. What the Interviewer Evaluates
- Enforcing strict header size limits.
- Validating token size before running expensive cryptographic parsers.
- Configuring connection throttling at the servlet container layer.

##### 3. Standout Technical Answer
```java
// Early Guard Filter placed BEFORE BearerTokenAuthenticationFilter:
@Component
public class PreAuthenticationDdosGuardFilter extends OncePerRequestFilter {

    private static final int MAX_HEADER_LENGTH = 2048; // Max 2KB for Auth Header

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.length() > MAX_HEADER_LENGTH) {
            // Reject immediately before CPU spends cycles parsing cryptographic signatures!
            response.setStatus(HttpStatus.REQUEST_HEADER_FIELDS_TOO_LARGE.value());
            response.getWriter().write("{\"error\":\"Authorization header exceeds maximum allowed size.\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
```

```yaml
# application.yml: Enforce Tomcat Header Limits
server:
  max-http-request-header-size: 8KB
```

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "Why is RSA-4096 signature verification vulnerable to asymmetric CPU exhaustion compared to ECDSA (ES256)?"
- **Winning Answer**: "RSA verification scales quadratically with key size. Verifying an RSA-4096 signature consumes roughly 5x more CPU cycles than RSA-2048. **ECDSA (ES256 / Ed25519)** provides equivalent or superior cryptographic strength with tiny keys (256 bits) and orders of magnitude lower verification CPU consumption, neutralizing cryptographic exhaustion attacks."

---

#### Q38: SAML 2.0 Integration with Spring Security

##### 1. Exact Scenario & Question
An enterprise client requires Single Sign-On (SSO) using their legacy corporate Active Directory Federation Services (ADFS) via **SAML 2.0**. How does Spring Security configure the Service Provider (SP) metadata, handle the SAML Assertion, and validate signed XML responses?

##### 2. What the Interviewer Evaluates
- Understanding SAML 2.0 Web Browser SSO profile.
- Configuring `OpenSaml4AuthenticationProvider`.
- Handling XML signature wrapping (XSW) attacks.

##### 3. Standout Technical Answer
```yaml
# application.yml: Spring Security SAML 2.0 Configuration
spring:
  security:
    saml2:
      relyingparty:
        registration:
          adfs:
            signing:
              credentials:
                - private-key-location: classpath:saml/sp-private-key.pem
                  certificate-location: classpath:saml/sp-cert.pem
            assertingparty:
              metadata-uri: https://adfs.enterprise.com/FederationMetadata/2007-06/FederationMetadata.xml
```

```java
@Bean
public SecurityFilterChain samlFilterChain(HttpSecurity http) throws Exception {
    return http
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/login/**", "/error").permitAll()
            .anyRequest().authenticated()
        )
        .saml2Login(Customizer.withDefaults()) // Exposes /saml2/authenticate/* and /login/saml2/*
        .build();
}
```

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "What is an XML Signature Wrapping (XSW) attack in SAML?"
- **Winning Answer**: "An attacker copies a legitimate signed SAML assertion and injects a second, modified assertion into the XML document. If the XML parser verifies the signature on the first assertion but the application logic reads attributes from the unverified second assertion, the attacker achieves authentication bypass. Spring Security uses OpenSAML 4 with strict schema validation to eliminate XSW vulnerabilities."

---

#### Q39: Fine-Grained Domain Object Security — Spring Security ACLs

##### 1. Exact Scenario & Question
In a document management system, users can create folders and grant read, write, or admin permissions to specific colleagues for specific individual files. Standard `@PreAuthorize("hasRole('USER')")` cannot enforce per-row object ownership. Explain how the Spring Security Access Control List (ACL) framework models domain permissions.

##### 2. What the Interviewer Evaluates
- Understanding the four core ACL database tables (`acl_sid`, `acl_class`, `acl_object_identity`, `acl_entry`).
- Using `@PreAuthorize("hasPermission(#document, 'WRITE')")`.
- Evaluating ACL performance trade-offs.

##### 3. Standout Technical Answer
Spring Security ACL provides database-backed, row-level permissions for domain objects:
1. `acl_sid`: Represents principals (users or roles).
2. `acl_class`: Identifies the domain entity class (e.g. `com.app.Document`).
3. `acl_object_identity`: Represents the specific domain object instance (ID = 402).
4. `acl_entry`: Individual bitmask permission grants (`READ = 1`, `WRITE = 2`, `DELETE = 4`).

```java
@Service
public class DocumentService {

    // Evaluates permission via AclPermissionEvaluator
    @PreAuthorize("hasPermission(#documentId, 'com.app.Document', 'WRITE')")
    public void editDocument(Long documentId, String content) {
        // Only executes if user has WRITE permission in acl_entry table
    }
}
```

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "Why do most modern architectures avoid the Spring Security ACL framework in favor of application-level authorization tables or OPA?"
- **Winning Answer**: "Because Spring Security ACL requires querying 4 normalized database join tables for **every single object checked**. Calling `findAll()` on 500 documents generates 500 ACL checks, causing catastrophic database load. Modern systems either enforce permissions at the SQL `WHERE` clause level or use external policy engines."

---

#### Q40: Hardware Security Modules (HSM) & Cloud KMS Key Management

##### 1. Exact Scenario & Question
Security compliance requires that private keys used for signing OAuth2 JWTs must **never exist in plaintext in server memory or disk storage**. How do you architect JWT signing using AWS KMS or HashiCorp Vault Transit engine?

##### 2. What the Interviewer Evaluates
- Understanding FIPS 140-2 Level 3 HSM compliance.
- Signing JWTs via remote KMS API (`kms:Sign`).
- Implementing custom `JWSSigner` in Nimbus.

##### 3. Standout Technical Answer
In an HSM/KMS architecture, the private key never leaves the tamper-proof hardware module:
1. The Spring Boot application builds the JWT header and payload.
2. Instead of computing the RSA signature locally, the application hashes the payload (`SHA256`) and sends the hash to AWS KMS via the `kms:Sign` API.
3. AWS KMS performs the cryptographic signing inside the hardware boundary and returns the raw signature bytes.
4. The application appends the signature to the JWT.

```java
public class AwsKmsRsaSigner implements JWSSigner {

    private final KmsClient kmsClient;
    private final String keyId;

    public AwsKmsRsaSigner(KmsClient kmsClient, String keyId) {
        this.kmsClient = kmsClient;
        this.keyId = keyId;
    }

    @Override
    public Base64URL sign(JWSHeader header, byte[] signingInput) throws JOSEException {
        SignRequest request = SignRequest.builder()
            .keyId(keyId)
            .message(SdkBytes.fromByteArray(signingInput))
            .messageType(MessageType.RAW)
            .signingAlgorithm(SigningAlgorithmSpec.RSASSA_PKCS1_V1_5_SHA_256)
            .build();

        SignResponse response = kmsClient.sign(request);
        return Base64URL.encode(response.signature().asByteArray());
    }

    @Override public Set<JWSAlgorithm> supportedJWSAlgorithms() { return Set.of(JWSAlgorithm.RS256); }
    @Override public JCAContext getJCAContext() { return new JCAContext(); }
}
```

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "What is the primary trade-off of using AWS KMS for every JWT signing request?"
- **Winning Answer**: "Latency and API cost. Calling AWS KMS over HTTPS adds ~10-25ms to every token generation call, and AWS charges per 10,000 API calls. To optimize, only generate tokens on login/refresh; verify signatures locally using the cached public key."

---

#### Q41: Handling Clock Skew in Distributed JWT Validation

##### 1. Exact Scenario & Question
An authorization server and resource server reside in different AWS regions. Due to slight NTP drift (2 seconds), clients intermittently receive `JwtValidationException: Jwt expired at ... current time is ...`. How do you configure acceptable clock skew in Spring Security?

##### 2. What the Interviewer Evaluates
- Understanding NTP drift in distributed systems.
- Configuring `OAuth2TokenValidator` with clock skew tolerance.
- Mitigating `nbf` (Not Before) and `exp` (Expiration) race conditions.

##### 3. Standout Technical Answer
```java
@Bean
public JwtDecoder jwtDecoderWithClockSkew() {
    NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();

    // Configure 60 seconds of clock skew tolerance:
    DelegatingOAuth2TokenValidator<Jwt> defaultValidator = new DelegatingOAuth2TokenValidator<>(
        new JwtTimestampValidator(Duration.ofSeconds(60)), // Tolerates up to 60s clock drift!
        new JwtIssuerValidator(expectedIssuer)
    );

    jwtDecoder.setJwtValidator(defaultValidator);
    return jwtDecoder;
}
```

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "What risk is introduced if you set clock skew tolerance to 10 minutes?"
- **Winning Answer**: "An expired token will remain accepted as valid for 10 minutes after its intended expiration timestamp, increasing the window of vulnerability if a token is compromised."

---

#### Q42: Passwordless Authentication — FIDO2 / WebAuthn in Spring Boot

##### 1. Exact Scenario & Question
Your bank mandates replacing SMS OTPs with phishing-resistant **FIDO2 / WebAuthn** hardware tokens (YubiKeys / FaceID). Explain how WebAuthn public-key cryptography works in browser authentication and how Spring Security integrates with WebAuthn.

##### 2. What the Interviewer Evaluates
- Understanding public-key credentials bound to origins.
- Preventing phishing (origin-bound signatures).
- Integrating WebAuthn registration and authentication ceremonies.

##### 3. Standout Technical Answer
WebAuthn eliminates shared secrets (passwords):
1. **Registration**: The browser/hardware authenticator generates an asymmetric key pair. The private key remains in the device secure enclave; the public key is sent to the Spring Boot server and stored in the database.
2. **Authentication Ceremony**:
   - Server generates a random cryptographic `challenge` and sends it to the browser.
   - The device prompts for biometric touch (FaceID / Fingerprint) to unlock the private key.
   - The device signs the `challenge` along with the browser's domain origin (`enterprise.com`).
   - The server verifies the signature using the stored public key.
3. **Phishing Resistance**: If an attacker creates `fake-enterprise.com`, the browser signs the origin `fake-enterprise.com`. When sent to the real server, the server detects origin mismatch and rejects the authentication!

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "Can an attacker intercept a WebAuthn signature and replay it against the server?"
- **Winning Answer**: "No! The server's challenge is a cryptographic nonce that is only valid once. Replaying a previously captured signature fails because the challenge has already expired or been consumed."

---

#### Q43: Threat Modeling (STRIDE) Applied to Spring Security

##### 1. Exact Scenario & Question
You are conducting a formal STRIDE threat model on a new Spring Boot microservice handling credit card payments. Walk through each letter of the STRIDE acronym and identify the Spring Security mitigation for each threat.

##### 2. What the Interviewer Evaluates
- Mastery of the Microsoft STRIDE methodology.
- Mapping threats to specific Spring Security filters and configurations.
- Senior architect level security posture.

##### 3. Standout Technical Answer
```
+─────────────────────────────────────────────────────────────────────────────────────────+
|                  STRIDE Threat Modeling Matrix for Spring Security                      |
+---+───────────────────────────+─────────────────────────────────────────────────────────+
| S | Spoofing Identity         | Mitigated via: JWT / mTLS / PKCE / Argon2 password hash |
| T | Tampering with Data       | Mitigated via: HMAC / Asymmetric Digital Signatures     |
| R | Repudiation               | Mitigated via: Spring Security Audit Events & Envers    |
| I | Information Disclosure    | Mitigated via: TLS 1.3 / Disabled Actuator / CSP        |
| D | Denial of Service (DoS)   | Mitigated via: Bucket4j Rate Limiting / Header Caps     |
| E | Elevation of Privilege    | Mitigated via: Method Security (@PreAuthorize / ABAC)   |
+---+───────────────────────────+─────────────────────────────────────────────────────────+
```

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "Which STRIDE category does a Broken Object Level Authorization (BOLA / IDOR) vulnerability belong to?"
- **Winning Answer**: "**Elevation of Privilege** (and Information Disclosure). A user authenticated with standard privileges tampers with an ID parameter to access another user's private data."

---

#### Q44: Credential Stuffing Countermeasures & Automated Account Lockout

##### 1. Exact Scenario & Question
An attacker uses a compromised database dump from another website to automate 1,000,000 login attempts against your login endpoint. Implement an automated account lockout and IP throttling listener using Spring Security's `AuthenticationFailureBadCredentialsEvent`.

##### 2. What the Interviewer Evaluates
- Listening to Spring Security application events.
- Implementing exponential account lockouts.
- Defending against denial-of-service via malicious lockouts.

##### 3. Standout Technical Answer
```java
@Component
public class LoginAttemptService implements ApplicationListener<AbstractAuthenticationEvent> {

    private final Map<String, Integer> attemptsCache = new ConcurrentHashMap<>();
    private final Set<String> lockedAccounts = ConcurrentHashMap.newKeySet();

    @Override
    public void onApplicationEvent(AbstractAuthenticationEvent event) {
        if (event instanceof AuthenticationFailureBadCredentialsEvent failure) {
            String username = failure.getAuthentication().getName();
            int attempts = attemptsCache.getOrDefault(username, 0) + 1;
            attemptsCache.put(username, attempts);

            if (attempts >= 5) {
                lockedAccounts.add(username);
                LoggerFactory.getLogger("SECURITY").warn("Account locked due to 5 failed attempts: {}", username);
            }
        } else if (event instanceof AuthenticationSuccessEvent success) {
            String username = success.getAuthentication().getName();
            attemptsCache.remove(username); // Reset on successful login
        }
    }

    public boolean isLocked(String username) {
        return lockedAccounts.contains(username);
    }
}
```

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "How can an attacker exploit an automated account lockout policy to cause a Denial of Service (DoS) against your company's CEO?"
- **Winning Answer**: "The attacker deliberately sends 5 incorrect passwords for the CEO's known username, locking out the executive. To prevent this, never lock accounts permanently based strictly on username. Combine username lockouts with IP reputation, CAPTCHAs, or step-up MFA verification rather than complete account locking."

---

#### Q45: Spring Security Audit Logging — Tracking Authentication Events

##### 1. Exact Scenario & Question
Compliance mandates that all successful logins, failed attempts, and authorization denials must be recorded in an immutable audit trail with IP address and User-Agent. Implement an asynchronous Spring Security audit listener.

##### 2. What the Interviewer Evaluates
- Subscribing to `AbstractAuthenticationEvent` and `AuthorizationEvents`.
- Extracting `WebAuthenticationDetails` (remote IP and session ID).
- Asynchronous non-blocking audit logging.

##### 3. Standout Technical Answer
```java
@Component
public class SecurityAuditLogger {

    private static final Logger auditLog = LoggerFactory.getLogger("SECURITY_AUDIT");

    @Async
    @EventListener
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
        Authentication auth = event.getAuthentication();
        String ip = extractIp(auth);
        auditLog.info("AUTH_SUCCESS | user={} | ip={} | timestamp={}", auth.getName(), ip, Instant.now());
    }

    @Async
    @EventListener
    public void onAuthenticationFailure(AbstractAuthenticationFailureEvent event) {
        Authentication auth = event.getAuthentication();
        String ip = extractIp(auth);
        auditLog.warn("AUTH_FAILURE | user={} | ip={} | reason={} | timestamp={}", 
            auth.getName(), ip, event.getException().getMessage(), Instant.now());
    }

    private String extractIp(Authentication auth) {
        if (auth.getDetails() instanceof WebAuthenticationDetails details) {
            return details.getRemoteAddress();
        }
        return "UNKNOWN";
    }
}
```

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "Why must audit logging methods be marked `@Async`?"
- **Winning Answer**: "If an audit log is written synchronously inside the event listener to a remote logging server or database, any latency or failure in the audit store will slow down or crash the user's login request."

---

#### Q46: Impersonation & "Run-As" Capabilities with Strict Security Constraints

##### 1. Exact Scenario & Question
Customer support representatives need to "impersonate" a user to troubleshoot complex UI bugs. How do you implement secure user impersonation in Spring Security using `SwitchUserFilter`, while ensuring the original representative's identity is retained in audit logs?

##### 2. What the Interviewer Evaluates
- Understanding `SwitchUserFilter`.
- Enforcing strict roles for who can impersonate (`ROLE_SUPPORT_ADMIN`).
- Inspecting `SwitchUserGrantedAuthority` to log real identity.

##### 3. Standout Technical Answer
```java
@Configuration
public class ImpersonationSecurityConfig {

    private final UserDetailsService userDetailsService;

    public ImpersonationSecurityConfig(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SwitchUserFilter switchUserFilter() {
        SwitchUserFilter filter = new SwitchUserFilter();
        filter.setUserDetailsService(userDetailsService);
        filter.setSwitchUserUrl("/admin/impersonate");
        filter.setExitUserUrl("/admin/impersonate/exit");
        filter.setTargetUrl("/dashboard");
        return filter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/admin/impersonate").hasRole("SUPPORT_SUPERVISOR")
                .anyRequest().authenticated()
            )
            // Add filter after normal authorization
            .addFilterAfter(switchUserFilter(), AuthorizationFilter.class)
            .build();
    }
}
```

*Extracting Original User in Audit Logs:*
```java
public String getRealAuthenticatedUser(Authentication auth) {
    for (GrantedAuthority authority : auth.getAuthorities()) {
        if (authority instanceof SwitchUserGrantedAuthority switchAuth) {
            return switchAuth.getSource().getName(); // Returns real support admin username!
        }
    }
    return auth.getName();
}
```

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "What dangerous loophole occurs if an impersonated session allows the support rep to change the user's password?"
- **Winning Answer**: "The support rep could permanently steal the user's account! Impersonated sessions must be restricted: security-critical endpoints (password reset, email change, 2FA management) must inspect `SwitchUserGrantedAuthority` and strictly deny execution if an impersonation session is detected."

---

#### Q47: Dynamic Permission Evaluation via SpEL (`hasPermission`)

##### 1. Exact Scenario & Question
You are implementing an enterprise document management system where permissions are stored in a database permission matrix. How do you write a custom `PermissionEvaluator` in Spring Security to support `@PreAuthorize("hasPermission(#documentId, 'DOCUMENT', 'EDIT')")`?

##### 2. What the Interviewer Evaluates
- Implementing `org.springframework.security.access.PermissionEvaluator`.
- Injecting `MethodSecurityExpressionHandler`.
- Handling target domain object resolution vs ID resolution.

##### 3. Standout Technical Answer
```java
@Component
public class CustomDatabasePermissionEvaluator implements PermissionEvaluator {

    private final DocumentAccessService accessService;

    public CustomDatabasePermissionEvaluator(DocumentAccessService accessService) {
        this.accessService = accessService;
    }

    @Override
    public boolean hasPermission(Authentication auth, Object targetDomainObject, Object permission) {
        if (targetDomainObject instanceof Document doc) {
            return accessService.hasAccess(auth.getName(), doc.getId(), permission.toString());
        }
        return false;
    }

    @Override
    public boolean hasPermission(Authentication auth, Serializable targetId, String targetType, Object permission) {
        if ("DOCUMENT".equalsIgnoreCase(targetType)) {
            return accessService.hasAccess(auth.getName(), (Long) targetId, permission.toString());
        }
        return false;
    }
}

// Register Custom Expression Handler:
@Configuration
@EnableMethodSecurity
public class MethodSecurityExpressionConfig {

    @Bean
    public MethodSecurityExpressionHandler methodSecurityExpressionHandler(PermissionEvaluator evaluator) {
        DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
        handler.setPermissionEvaluator(evaluator);
        return handler;
    }
}
```

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "Why will `hasPermission(#documentId, ...)` fail if you annotate a private method?"
- **Winning Answer**: "Spring Security's method security is powered by **Spring AOP proxies**. AOP proxies only intercept calls to `public` methods invoked from outside the class. Calls to private methods, or internal calls via `this.method()`, bypass the proxy completely."

---

#### Q48: Third-Party Social Login & Account Linking

##### 1. Exact Scenario & Question
Your application supports login via Google, GitHub, and email/password. A user creates an account with email `alex@enterprise.com`. Later, they click "Sign in with Google" using the same email address. How do you implement secure Account Linking without exposing users to account takeover attacks?

##### 2. What the Interviewer Evaluates
- Configuring Spring Security OAuth2 Login (`oauth2Login()`).
- Customizing `DefaultOAuth2UserService`.
- Preventing account takeover when a social provider does not verify emails.

##### 3. Standout Technical Answer
```java
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepo;

    public CustomOAuth2UserService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String email = oAuth2User.getAttribute("email");
        Boolean emailVerified = oAuth2User.getAttribute("email_verified");

        // CRITICAL: Never link accounts if the IdP has not verified the email address!
        if (Boolean.FALSE.equals(emailVerified)) {
            throw new OAuth2AuthenticationException(new OAuth2Error("unverified_email"), "Unverified email from social provider");
        }

        User user = userRepo.findByEmail(email).orElseGet(() -> createNewSocialUser(email, oAuth2User));
        return new CustomUserPrincipal(user, oAuth2User.getAttributes());
    }
}
```

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "What attack occurs if you automatically link an account from a social provider that allows unverified emails?"
- **Winning Answer**: "An attacker can register an account on an unverified provider with the victim's email address (`victim@gmail.com`). When the attacker clicks 'Sign in with that Provider', your system automatically links the accounts, giving the attacker **complete unauthorized access** to the victim's account without knowing their password!"

---

#### Q49: Cryptographic Performance Optimization — Offloading Signature Verification

##### 1. Exact Scenario & Question
Your microservice cluster handles 500,000 requests/sec. Profiling shows that 45% of total CPU time is spent computing RSA signature verification (`RSASignature.engineVerify()`). How do you architect signature caching without violating token expiration?

##### 2. What the Interviewer Evaluates
- Understanding that signature verification is computationally expensive.
- Caching cryptographic verification results based on token signature bytes.
- Enforcing expiration checks on cached results.

##### 3. Standout Technical Answer
```java
@Component
public class SignatureCachedJwtDecoder implements JwtDecoder {

    private final JwtDecoder delegate;
    // Local LRU Cache: Hash(Raw Signature) -> Expiration Instant
    private final Cache<String, Instant> verifiedSignatures = Caffeine.newBuilder()
        .maximumSize(500_000)
        .expireAfterWrite(Duration.ofMinutes(15))
        .build();

    public SignatureCachedJwtDecoder(JwtDecoder delegate) {
        this.delegate = delegate;
    }

    @Override
    public Jwt decode(String token) throws JwtException {
        String[] parts = token.split("\\.");
        if (parts.length < 3) throw new BadJwtException("Malformed JWT");

        String signature = parts[2];
        Instant cachedExpiry = verifiedSignatures.getIfPresent(signature);

        // If signature was previously verified and token is not expired:
        if (cachedExpiry != null && Instant.now().isBefore(cachedExpiry)) {
            // Parse claims without re-running expensive RSA verification!
            return parseWithoutVerification(token);
        }

        // Full cryptographic verification:
        Jwt jwt = delegate.decode(token);
        verifiedSignatures.put(signature, jwt.getExpiresAt());
        return jwt;
    }
}
```

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "Why must the cache key be the **signature string** and not the whole JWT?"
- **Winning Answer**: "The signature is a cryptographic hash of both the header and payload. Any alteration to the claims or permissions changes the signature completely, preventing cache tampering."

---

#### Q50: Enterprise Security Production Gate — The 10-Point Security Certification Checklist

##### 1. Exact Scenario & Question
You are the Chief Information Security Officer (CISO) conducting the final security review before an enterprise banking application launches on Spring Boot 3 and OAuth2. What are the 10 mandatory, non-negotiable architectural gates that must pass?

##### 2. What the Interviewer Evaluates
- Holistic mastery of authentication, authorization, cryptography, and network security.
- Comprehensive synthesis of all 50 scenarios.
- Executive architectural governance.

##### 3. Standout Technical Answer
To certify a Spring Security application for enterprise production, it must pass this **10-Point Security Gate**:

```
+─────────────────────────────────────────────────────────────────────────────────────────+
|                  Enterprise Security Production Gate Checklist                          |
+----+─────────────────────────────+──────────────────────────────────────────────────────+
| #  | Security Gate               | Production Standard Requirement                      |
+----+─────────────────────────────+──────────────────────────────────────────────────────+
| 1  | Zero Default Passwords      | Default passwords disabled; credentials in Vault     |
| 2  | CSRF Policy Verified        | Disabled for stateless Bearer; Enabled for Cookies   |
| 3  | Algorithm Restrictions      | 'none' algorithm banned; strict RS256/ES256 enforced |
| 4  | Session Fixation Protection | changeSessionId() enforced; Concurrent sessions capped|
| 5  | Password Hashing Sized      | Argon2id or BCrypt (cost 12) verified                |
| 6  | Security Headers Enforced   | HSTS (1 yr), CSP (frame-ancestors), nosniff active   |
| 7  | Actuator Port Isolation     | Management endpoints bound to internal port 8081     |
| 8  | Rate Limiting & DoS Guard   | Token bucket login rate limiting; max header size 8KB|
| 9  | Method Security Boundaries  | @EnableMethodSecurity active; SpEL validated         |
| 10 | Audit Logging Active        | Async listeners recording all auth failures & IP     |
+----+─────────────────────────────+──────────────────────────────────────────────────────+
```

##### 4. Follow-Up Trap Question & Winning Answer
- **Trap Question**: "If an application passes all 10 gates, what runtime threat can still compromise the application?"
- **Winning Answer**: "**Vulnerable Third-Party Dependencies (Supply Chain Attacks)**. A zero-day in a logging framework (like Log4Shell CVE-2021-44228) or serialized gadget chain bypasses all Spring Security application logic. Continuous dependency scanning (Snyk, Dependabot, Trivy) in the CI/CD pipeline is mandatory."

---

## Section 2: Beginner Mistakes & Anti-Patterns (7 Critical Traps)

### ❌ Mistake 1: Using `hasRole('ADMIN')` when JWT Has Authority `'ADMIN'`
```java
// ❌ WRONG: hasRole expects ROLE_ADMIN!
.requestMatchers("/admin/**").hasRole("ADMIN")
```
💥 **Why It Fails:** `hasRole("ADMIN")` automatically prepends `ROLE_`, checking for `ROLE_ADMIN`. If your JWT emits `"ADMIN"`, the check fails with a 403 Forbidden.
```java
// ✅ FIX: Use hasAuthority or configure JwtAuthenticationConverter
.requestMatchers("/admin/**").hasAuthority("ADMIN")
```
🧠 **The Lesson:** `hasRole("X")` = `ROLE_X`. `hasAuthority("X")` = exact string match `X`.

---

### ❌ Mistake 2: Storing Passwords with Raw SHA-256 or MD5
```java
// ❌ WRONG: Fast hashing algorithms can be cracked in seconds via GPUs
MessageDigest.getInstance("SHA-256").digest(password.getBytes());
```
💥 **Why It Fails:** GPUs can calculate 10 billion SHA-256 hashes per second, allowing rainbow table cracking.
```java
// ✅ FIX: Use Argon2id or BCrypt
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);
}
```
🧠 **The Lesson:** Always use slow, memory-hard, salted algorithms like Argon2id or BCrypt.

---

### ❌ Mistake 3: Disabling CSRF on Cookie-Based Session Applications
```java
// ❌ WRONG: Disabling CSRF on an application using cookies
http.csrf(AbstractHttpConfigurer::disable);
```
💥 **Why It Fails:** If your application uses browser cookies for session authentication, an attacker can trick the user into submitting unauthorized cross-origin requests.
```java
// ✅ FIX: Keep CSRF enabled for cookie-based apps
http.csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()));
```
🧠 **The Lesson:** Only disable CSRF if the API is 100% stateless and uses Bearer tokens.

---

### ❌ Mistake 4: Permitting CORS `*` with `AllowCredentials(true)`
```java
// ❌ WRONG: Illegal W3C CORS combination
corsConfig.addAllowedOrigin("*");
corsConfig.setAllowCredentials(true);
```
💥 **Why It Fails:** Modern browsers reject responses with an error when wildcard origins are combined with credentials.
```java
// ✅ FIX: Specify explicit origins
corsConfig.setAllowedOrigins(List.of("https://app.enterprise.com"));
corsConfig.setAllowCredentials(true);
```
🧠 **The Lesson:** Never mix wildcard origins with credentials in CORS.

---

### ❌ Mistake 5: Hardcoding Keys and Secrets in Source Code
```java
// ❌ WRONG: Secret checked into Git repository
String secret = "my-secret-key-12345678901234567890";
```
💥 **Why It Fails:** Exposed secrets in version control lead to credential leakage.
```java
// ✅ FIX: Inject from Vault or Environment
@Value("${security.jwt.secret}")
private String secret;
```
🧠 **The Lesson:** Always externalize secrets into KMS, Vault, or Kubernetes Secrets.

---

### ❌ Mistake 6: Assuming `@Async` Inherits SecurityContext Automatically
```java
// ❌ WRONG: Background thread has empty SecurityContext
@Async
public void sendEmail() {
    String user = SecurityContextHolder.getContext().getAuthentication().getName(); // NullPointerException!
}
```
💥 **Why It Fails:** `@Async` runs on a separate worker thread. `ThreadLocal` does not propagate to existing pooled threads.
```java
// ✅ FIX: Wrap task executor with DelegatingSecurityContextAsyncTaskExecutor
```
🧠 **The Lesson:** Explicitly configure security context propagation on asynchronous executors.

---

### ❌ Mistake 7: Placing CORS Filters AFTER Spring Security
```java
// ❌ WRONG: Security filter blocks preflight OPTIONS before CORS filter executes
.addFilterAfter(new CorsFilter(...), UsernamePasswordAuthenticationFilter.class);
```
💥 **Why It Fails:** Preflight `OPTIONS` requests carry no authorization headers and get rejected with 401/403.
```java
// ✅ FIX: Configure CORS directly in HttpSecurity or at HIGHEST_PRECEDENCE
http.cors(Customizer.withDefaults());
```
🧠 **The Lesson:** CORS must execute before all authentication filters.

---

## Section 3: Globally Reported Production Incidents & War-Room Outages

### 🚨 Incident 1: The Spring Security 6.1 CORS Preflight Lockout
- **The Incident**: After upgrading to Spring Boot 3.1 / Spring Security 6.1, a major retail API rejected 100% of frontend Single Page Application requests with HTTP 403 Forbidden on preflight `OPTIONS` calls.
- **Root Cause Analysis**: Spring Security 6.1 adjusted filter precedence. The authorization filter ran before the CORS handler, rejecting unauthenticated `OPTIONS` requests.
- **The War Room Fix**:
  ```java
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
      return http
          .cors(cors -> cors.configurationSource(corsConfigurationSource()))
          .authorizeHttpRequests(auth -> auth
              .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // Explicitly permit preflight
              .anyRequest().authenticated()
          )
          .build();
  }
  ```
- **Prevention Checklist**:
  - [ ] Add CI browser tests validating preflight CORS headers.
  - [ ] Read Spring Security migration guides before major version upgrades.

---

### 🚨 Incident 2: The BCrypt Cost 15 Server Freeze Outage
- **The Incident**: After increasing BCrypt strength from 10 to 15 for "extra security", a fintech platform suffered 100% CPU lockup and cascading 504 timeouts during morning login rush.
- **Root Cause Analysis**: BCrypt is exponential ($2^{cost}$). Sizing to cost 15 required 3.2 seconds of 100% CPU per login. With 200 Tomcat threads, maximum throughput collapsed to 60 logins/sec.
- **The War Room Fix**:
  Reduced BCrypt work factor back to 10–12 and offloaded hashing to Argon2 with tuned memory parameters.
- **Prevention Checklist**:
  - [ ] Benchmark password hashing latency under load in pre-prod.
  - [ ] Rate limit login endpoints to prevent concurrent CPU exhaustion.

---

### 🚨 Incident 3: The JWT Signature Bypass (The `none` Algorithm Exploit)
- **The Incident**: An attacker bypassed authentication on an enterprise SaaS portal, accessing arbitrary admin accounts by modifying JWT headers to `{"alg": "none"}`.
- **Root Cause Analysis**: An outdated custom parser library permitted unsigned tokens when `alg` was `"none"`.
- **The War Room Fix**:
  Replaced custom parser with Spring Security's `NimbusJwtDecoder` which enforces signature verification and rejects `alg: none`.
- **Prevention Checklist**:
  - [ ] Never parse or verify JWTs using custom homegrown code.
  - [ ] Enforce strict cryptographic validation libraries (Nimbus / Spring Security).

---

### 🚨 Incident 4: Spring4Shell (CVE-2022-22965) ClassLoader Injection
- **The Incident**: In 2022, a critical zero-day vulnerability in Spring Framework allowed remote code execution (RCE) via data binding parameter binding on JDK 9+.
- **Root Cause Analysis**: Attackers sent HTTP requests binding to `class.module.classLoader.resources.context.parent.pipeline.first.pattern`, writing a webshell directly into Tomcat's root directory.
- **The War Room Fix**:
  Upgraded Spring Framework to 5.3.18+ / 5.2.20+ and restricted dis-allowed fields in `WebDataBinder`.
- **Prevention Checklist**:
  - [ ] Run automated vulnerability scanning (Dependabot / Snyk) on all dependencies.
  - [ ] Implement Web Application Firewall (WAF) rules blocking classloader traversal attempts.

---

## Section 4: Pros, Cons & Decision Matrix

| Mechanism | Security Strength | Performance | Revocation Capability | Best Suited For |
|---|---|---|---|---|
| **Stateless JWT (RS256)** | High (Cryptographic proof) | **Fastest (Local verification)** | Delayed (Until token expires) | Distributed Microservices, Mobile APIs |
| **Opaque Token (Introspection)** | High | Slower (Network hop per call) | **Instant (< 1 second)** | High-security banking, sensitive portals |
| **HttpOnly Session Cookies** | High (Immune to XSS token theft) | Fast | **Instant (Session invalidate)** | Traditional Web Apps, Server-Rendered Views |
| **mTLS (X.509 Certificates)** | **Highest (Hardware backed)** | Fast (Hardware accelerated) | Instant (CRL / OCSP) | Service-to-Service Zero Trust, Banking cores |

---

## Section 5: Follow-Up Trap Question & Next Learning Step

- **Trap Question**: "Can an expired JWT token be refreshed using a Refresh Token if the user's password has changed in the meantime?"
- **Winning Answer**: "Yes, unless the Authorization Server verifies user credentials during the refresh grant! Because the refresh grant only validates the refresh token itself, changing a password does not invalidate issued refresh tokens unless the server tracks a `password_changed_at` timestamp and revokes all refresh tokens issued prior to that timestamp."

---

🔗 **Next Architectural Guide**: [Spring Kafka & Event-Driven Systems Architecture Guide](file:///d:/project/github/1/CheatSheet_Tutorial/interview_prep/02_spring_framework/spring_kafka_events.md)
