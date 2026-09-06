[🏠 Back to Home](README.md) | [🌐 Microservices & Infrastructure Guide](microservices_gateway_infrastructure_master_guide.md) | [🔬 200 Scenarios & Setup Labs](security_infra_200_scenarios_master_guide.md) | [🏛️ System Design Guide](system_design.md) | [📖 Tech Glossary](topics/glossary.md)

# 🛡️ Enterprise Security, Authentication & Authorization: The Architect's Zero-to-Hero Masterclass

> **Target Audience:** Software Engineers, Tech Leads, Security Architects, and Cloud Practitioners.  
> **Prerequisites:** **Zero.** This guide assumes you know nothing about identity, cryptography, or security. Every topic is taught from fundamental first principles using relatable real-world analogies, historical evolution, step-by-step mechanics, architecture diagrams, production configurations, and trade-off matrices.  
> **Pedagogical Standard:** Every concept strictly answers:
> 1. **What is it?** (Plain-English definition + Real-world analogy)
> 2. **What did we have before?** (The historical legacy approach)
> 3. **What problem does it solve & Why do we need it?** (The fatal flaw of the legacy way)
> 4. **How does it work?** (Internal architecture, step-by-step lifecycle flows, ASCII/packet diagrams)
> 5. **How to make it work?** (Production code, configuration snippets, cURL recipes)
> 6. **Pros & Cons** (Architectural trade-off analysis)

---

## 📑 Master Table of Contents
1. [🧠 Phase 0: Foundations — Authentication (AuthN) vs. Authorization (AuthZ)](#-phase-0-foundations--authentication-authn-vs-authorization-authz)
2. [🔑 Phase 1: All Available Authentication Mechanisms](#-phase-1-all-available-authentication-mechanisms)
   - [1.1 HTTP Basic Authentication](#11-http-basic-authentication)
   - [1.2 HTTP Digest Authentication](#12-http-digest-authentication)
   - [1.3 Session-Cookie Authentication (Stateful)](#13-session-cookie-authentication-stateful)
   - [1.4 Bearer Tokens & API Keys](#14-bearer-tokens--api-keys)
   - [1.5 HMAC (Hash-based Message Authentication Code) Request Signing](#15-hmac-hash-based-message-authentication-code-request-signing)
   - [1.6 Mutual TLS (mTLS) & X.509 Client Certificates](#16-mutual-tls-mtls--x509-client-certificates)
   - [1.7 Passwordless & Modern MFA (FIDO2, WebAuthn, Passkeys, TOTP)](#17-passwordless--modern-mfa-fido2-webauthn-passkeys-totp)
3. [🛡️ Phase 2: All Available Authorization Models](#️-phase-2-all-available-authorization-models)
   - [2.1 DAC (Discretionary Access Control)](#21-dac-discretionary-access-control)
   - [2.2 MAC (Mandatory Access Control)](#22-mac-mandatory-access-control)
   - [2.3 RBAC (Role-Based Access Control) & The Role Explosion Trap](#23-rbac-role-based-access-control--the-role-explosion-trap)
   - [2.4 ABAC (Attribute-Based Access Control)](#24-abac-attribute-based-access-control)
   - [2.5 ReBAC (Relationship-Based Access Control / Google Zanzibar)](#25-rebac-relationship-based-access-control--google-zanzibar)
   - [2.6 PBAC (Policy-Based Access Control / Open Policy Agent Rego)](#26-pbac-policy-based-access-control--open-policy-agent-rego)
4. [🏢 Phase 3: Single Sign-On (SSO) & Enterprise Identity Federation](#-phase-3-single-sign-on-sso--enterprise-identity-federation)
   - [3.1 What is SSO & The Multi-App Nightmare](#31-what-is-sso--the-multi-app-nightmare)
   - [3.2 The Cross-Domain Cookie Barrier & Federation](#32-the-cross-domain-cookie-barrier--federation)
   - [3.3 Identity Provider (IdP) vs. Service Provider (SP)](#33-identity-provider-idp-vs-service-provider-sp)
5. [📜 Phase 4: SAML 2.0 (Security Assertion Markup Language)](#-phase-4-saml-20-security-assertion-markup-language)
   - [4.1 What is SAML 2.0?](#41-what-is-saml-20)
   - [4.2 SP-Initiated SSO Flow (Step-by-Step)](#42-sp-initiated-sso-flow-step-by-step)
   - [4.3 IdP-Initiated SSO Flow](#43-idp-initiated-sso-flow)
   - [4.4 Anatomy of a SAML XML Assertion](#44-anatomy-of-a-saml-xml-assertion)
   - [4.5 SAML Single Logout (SLO) & Security Traps (XSW Attacks)](#45-saml-single-logout-slo--security-traps-xsw-attacks)
   - [4.6 How to Configure SAML 2.0 in Production](#46-how-to-configure-saml-20-in-production)
6. [🌲 Phase 5: Directory Services: Active Directory, LDAP, Kerberos & Azure AD (Entra ID)](#-phase-5-directory-services-active-directory-ldap-kerberos--azure-ad-entra-id)
   - [5.1 LDAP (Lightweight Directory Access Protocol)](#51-ldap-lightweight-directory-access-protocol)
   - [5.2 Kerberos: The Three-Headed Dog (Tickets, TGT, KDC, ST)](#52-kerberos-the-three-headed-dog-tickets-tgt-kdc-st)
   - [5.3 Active Directory Domain Services (AD DS)](#53-active-directory-domain-services-ad-ds)
   - [5.4 Modern Evolution: Azure AD / Microsoft Entra ID](#54-modern-evolution-azure-ad--microsoft-entra-id)
   - [5.5 Practical Enterprise Integration Pattern](#55-practical-enterprise-integration-pattern)
7. [⚡ Phase 6: OAuth 2.0 & OpenID Connect (OIDC)](#-phase-6-oauth-20--openid-connect-oidc)
   - [6.1 The Valet Key Analogy: Why OAuth 2.0 is NOT Authentication](#61-the-valet-key-analogy-why-oauth-20-is-not-authentication)
   - [6.2 OpenID Connect (OIDC): Identity on Top of OAuth 2.0](#62-openid-connect-oidc-identity-on-top-of-oauth-20)
   - [6.3 Token Trio: ID Token vs. Access Token vs. Refresh Token](#63-token-trio-id-token-vs-access-token-vs-refresh-token)
   - [6.4 OAuth 2.0 Grant Flows (Code + PKCE, Client Credentials, Refresh)](#64-oauth-20-grant-flows-code--pkce-client-credentials-refresh)
   - [6.5 Deprecated & Forbidden Flows (Implicit & Password Grant)](#65-deprecated--forbidden-flows-implicit--password-grant)
   - [6.6 OIDC Discovery (`.well-known`) & JWKS Key Rotation](#66-oidc-discovery-well-known--jwks-key-rotation)
   - [6.7 Production Spring Security 6 / OIDC Implementation](#67-production-spring-security-6--oidc-implementation)
8. [🕸️ Phase 7: Microservices Security & Zero-Trust Architecture](#️-phase-7-microservices-security--zero-trust-architecture)
   - [7.1 The Death of the "Castle-and-Moat" Perimeter](#71-the-death-of-the-castle-and-moat-perimeter)
   - [7.2 Stateless JWT vs. Distributed Sessions](#72-stateless-jwt-vs-distributed-sessions)
   - [7.3 Token Revocation Problem & Distributed Blacklists](#73-token-revocation-problem--distributed-blacklists)
   - [7.4 Token Propagation vs. Token Exchange (RFC 8693 On-Behalf-Of)](#74-token-propagation-vs-token-exchange-rfc-8693-on-behalf-of)
   - [7.5 Service-to-Service Zero-Trust (mTLS, SPIFFE/SPIRE, Service Mesh)](#75-service-to-service-zero-trust-mtls-spiffespire-service-mesh)
   - [7.6 Secrets Management (Vault, AWS Secrets Manager)](#76-secrets-management-vault-aws-secrets-manager)
9. [🚪 Phase 8: API Gateway Security Architecture](#-phase-8-api-gateway-security-architecture)
   - [8.1 The Gateway as Policy Enforcement Point (PEP)](#81-the-gateway-as-policy-enforcement-point-pep)
   - [8.2 The Phantom Token Pattern (Opaque at Edge -> JWT Internally)](#82-the-phantom-token-pattern-opaque-at-edge---jwt-internally)
   - [8.3 Distributed Rate Limiting & Throttling Algorithms](#83-distributed-rate-limiting--throttling-algorithms)
   - [8.4 Web Application Firewall (WAF) & OWASP Top 10 Mitigation](#84-web-application-firewall-waf--owasp-top-10-mitigation)
   - [8.5 IP Whitelisting, Geo-Fencing, and CORS Hardening](#85-ip-whitelisting-geo-fencing-and-cors-hardening)
10. [📊 Phase 9: Enterprise Decision Matrix & Cheat Sheet](#-phase-9-enterprise-decision-matrix--cheat-sheet)

---

# 🧠 Phase 0: Foundations — Authentication (AuthN) vs. Authorization (AuthZ)

Before building any secure system, you must eliminate the single most dangerous confusion in software engineering: confusing **who you are** with **what you are allowed to do**.

```
+---------------------------------------------------------------------------------------+
|                                    SECURITY GATEWAY                                   |
|                                                                                       |
|   1. AUTHENTICATION (AuthN)                 2. AUTHORIZATION (AuthZ)                  |
|   "Who are you?"                            "What are you permitted to do?"           |
|                                                                                       |
|   +--------------------------+              +-------------------------------------+   |
|   | Identification & Proof   |              | Policy, Permissions & Privileges    |   |
|   | - Passwords              |              | - Read financial reports? (YES)     |   |
|   | - Biometrics / Passkeys  |  --------->  | - Delete user account? (NO)         |   |
|   | - X.509 Certificates     |              | - Transfer > $10,000? (NO)          |   |
|   | Output: Verified Identity|              | Output: Allow / Deny Decision       |   |
|   +--------------------------+              +-------------------------------------+   |
+---------------------------------------------------------------------------------------+
```

### The Real-World Airport Analogy
Imagine arriving at an international airport:
1. **Airport Passport Control (Authentication - AuthN)**:
   - The border officer looks at your passport and scans your face.
   - The officer verifies that you are indeed *John Doe* and that your passport is genuine and unexpired.
   - *Result*: You have proven your identity. But your passport alone does **not** allow you to board any flight!
2. **Flight Boarding Gate (Authorization - AuthZ)**:
   - At Gate 24B, the airline attendant scans your **Boarding Pass**.
   - The scanner checks: Does *John Doe* hold a seat on Flight AA100? Is he in First Class or Economy? Is he allowed to enter the cockpit? (Answer: Never).
   - *Result*: Your permissions and rights for that specific flight are enforced.

### Core Comparison Matrix
| Dimension | Authentication (AuthN) | Authorization (AuthZ) |
| :--- | :--- | :--- |
| **Core Question** | *"Who are you?"* | *"What are you allowed to do?"* |
| **Execution Timing** | Always happens **first**. | Always happens **after** identity is established. |
| **Input Data** | Passwords, OTP, Biometrics, Client Certificates, SAML Assertion, OIDC ID Token. | Roles, Scopes, Access Tokens, Group Memberships, ACLs, Policies. |
| **Common Protocols** | SAML 2.0, OpenID Connect (OIDC), Kerberos, LDAP, FIDO2/WebAuthn. | OAuth 2.0 Scopes, XACML, OPA (Rego), AWS IAM Policies, RBAC/ABAC. |
| **Data Artifact** | Identity Token (`id_token`), Session Cookie, Kerberos TGT. | Access Token (`access_token`), Capability List, Permissions Bitmask. |
| **Failure Response** | `401 Unauthorized` (Properly named: "Unauthenticated"). | `403 Forbidden` (Known identity, but access denied). |

---

# 🔑 Phase 1: All Available Authentication Mechanisms

## 1.1 HTTP Basic Authentication

### 1. What is it?
HTTP Basic Auth is the oldest and simplest web authentication mechanism defined in RFC 7617. The client sends a username and password in every single HTTP request inside the `Authorization` header, separated by a colon and encoded in Base64.
- **Analogy**: Showing your laminated company name badge with your photo and ID number printed directly on the front to the building guard every time you walk through any door.

### 2. What did we have before?
Before HTTP Basic Auth (HTTP/0.9 and early HTTP/1.0), web pages had zero native authentication concept. Every page was strictly public.

### 3. What problem does it solve & Why do we need it?
It provided a standardized, browser-native mechanism for servers to challenge unauthenticated users (`401 Unauthorized` with `WWW-Authenticate: Basic realm="Secure Area"`) triggering the browser's built-in login prompt without requiring custom HTML forms or JavaScript.

### 4. How does it work?
```
Client (Browser / App)                             Server (API / Web)
      |                                                    |
      | 1. GET /api/v1/orders                              |
      |--------------------------------------------------->|
      |                                                    | 2. Checks header: None found
      | 3. HTTP 401 Unauthorized                           |
      |    WWW-Authenticate: Basic realm="Finance"         |
      |<---------------------------------------------------|
      |                                                    |
[User types admin : secret123]                             |
[Encodes: base64("admin:secret123") -> "YWRtaW46c2VjcmV0MTIz"]
      |                                                    |
      | 4. GET /api/v1/orders                              |
      |    Authorization: Basic YWRtaW46c2VjcmV0MTIz       |
      |--------------------------------------------------->|
      |                                                    | 5. Decodes base64
      |                                                    | 6. Verifies password against DB
      | 7. HTTP 200 OK (Orders JSON)                       |
      |<---------------------------------------------------|
```

### 5. How to make it work?
```bash
# Generating the header manually via curl:
curl -v -H "Authorization: Basic $(echo -n 'admin:secret123' | base64)" https://api.enterprise.com/orders

# Or using curl's native basic auth flag:
curl -u admin:secret123 https://api.enterprise.com/orders
```
In Java (Spring Security 6):
```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    return http
        .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
        .httpBasic(Customizer.withDefaults())
        .build();
}
```

### 6. Pros & Cons
- **Pros**:
  - Extremely simple to understand and implement; zero state maintained on the server.
  - Universally supported by every HTTP client, language, and browser since 1996.
- **Cons**:
  - **Base64 is NOT encryption**: Anyone capturing the HTTP packet can decode the password instantly in 1 millisecond. **MUST** be run over HTTPS (TLS).
  - **Credentials sent on EVERY request**: Increases attack surface; cannot implement fine-grained scopes or short expiration.
  - **No programmatic logout**: Browsers cache Basic Auth credentials until the browser window is completely killed.

---

## 1.2 HTTP Digest Authentication

### 1. What is it?
Defined in RFC 7616, Digest Auth is an upgrade to Basic Auth where credentials are never sent across the wire in plaintext. Instead, the server sends a unique, randomized challenge (called a **nonce** — number used once), and the client hashes the password with this nonce before sending the response.
- **Analogy**: A bank guard gives you a random word "TIGER". You take your secret password and combine it with "TIGER", calculate a unique mathematical signature, and show that signature. The guard does the same math. If the signatures match, you prove you know the password without ever saying it out loud.

### 2. What did we have before?
Plaintext Basic Auth, where network sniffers on public Wi-Fi could read credentials directly out of HTTP headers.

### 3. What problem does it solve?
Prevents eavesdropping attacks and replay attacks by incorporating a server-generated nonce and request counter.

### 4. How does it work?
1. Client requests protected resource without credentials.
2. Server responds with `401 Unauthorized` + `WWW-Authenticate: Digest realm="users", nonce="dcd98b7102dd2f0e8b11d0f600bfb0c093"`.
3. Client computes hash:
   - $\text{HA1} = \text{MD5}(\text{username} : \text{realm} : \text{password})$
   - $\text{HA2} = \text{MD5}(\text{HTTP\_METHOD} : \text{digest\_URI})$
   - $\text{Response} = \text{MD5}(\text{HA1} : \text{nonce} : \text{nc} : \text{cnonce} : \text{qop} : \text{HA2})$
4. Client sends the computed `Response` string in the `Authorization: Digest ...` header.

### 5. Pros & Cons
- **Pros**: Password is never transmitted across the network in plaintext. Resistant to basic replay attacks.
- **Cons**: Requires the server to store passwords in plaintext or in reversibly hashed MD5 format (cannot use modern bcrypt/Argon2 one-way hashes). Severely vulnerable to Man-In-The-Middle downgrade attacks. Largely deprecated in favor of TLS + OAuth2/OIDC.

---

## 1.3 Session-Cookie Authentication (Stateful)

### 1. What is it?
The classic stateful web architecture where a user submits credentials via an HTML form, the server verifies them, generates a cryptographically random **Session ID**, stores the session in server memory or database (e.g., Redis), and returns the Session ID to the browser inside an HTTP response header: `Set-Cookie: JSESSIONID=xyz789; HttpOnly; Secure; SameSite=Strict`.
- **Analogy**: A coat check at a theatre. You give the attendant your heavy coat (your password). The attendant hangs your coat in a locked room and gives you a small plastic numbered token #42 (Session ID). Every time you want a drink at the bar, you show token #42. The staff knows you are a paid guest. When you leave, token #42 is destroyed.

### 2. What did we have before?
Sending the username and password on every single request (Basic Auth).

### 3. What problem does it solve?
Credentials are typed once. The server controls session lifecycle (inactivity timeout, immediate server-side revocation on logout).

### 4. How does it work?
```
Browser                                               Web Server & Redis
   |                                                          |
   | 1. POST /login (username="alice", password="secret")     |
   |--------------------------------------------------------->|
   |                                                          | 2. Verifies password (bcrypt)
   |                                                          | 3. Generates SessionID: "sess_99a8b"
   |                                                          | 4. Writes to Redis:
   |                                                          |    "sess_99a8b" -> {userId: 101, role: "USER"}
   | 5. HTTP 200 OK                                           |
   |    Set-Cookie: SID=sess_99a8b; HttpOnly; Secure          |
   |<---------------------------------------------------------|
   |                                                          |
[Browser automatically stores cookie in secure jar]           |
   |                                                          |
   | 6. GET /dashboard                                        |
   |    Cookie: SID=sess_99a8b                                |
   |--------------------------------------------------------->|
   |                                                          | 7. Reads Redis for "sess_99a8b"
   |                                                          | 8. Session found! User is Alice.
   | 9. HTTP 200 OK (Dashboard HTML)                          |
   |<---------------------------------------------------------|
```

### 5. How to make it work securely?
```http
Set-Cookie: SID=e83649a8b49c; Path=/; Domain=.example.com; Secure; HttpOnly; SameSite=Strict; Max-Age=3600
```
- `HttpOnly`: Prevents JavaScript from reading the cookie (`document.cookie`), neutralizing **Cross-Site Scripting (XSS)** token theft.
- `Secure`: Ensures the cookie is only transmitted over encrypted **HTTPS** connections.
- `SameSite=Strict`: Prevents the browser from sending the cookie on cross-site requests, mitigating **Cross-Site Request Forgery (CSRF)**.

### 6. Pros & Cons
- **Pros**:
  - **Instant Revocation**: To revoke access, delete the key from Redis; the user is kicked out on their next request.
  - **Small Footprint**: The cookie is only a small 32-byte opaque identifier.
  - **Automatic Browser Management**: The browser handles storage and injection automatically.
- **Cons**:
  - **Stateful Bottleneck**: The server must perform a database/cache lookup on *every single incoming HTTP request*.
  - **Horizontal Scaling Complexity**: Requires centralized session stores (Redis cluster) or sticky load-balancer sessions.
  - **Cross-Domain & Mobile Friction**: Native mobile apps do not handle cookies smoothly; cookies fail across different domain boundaries without complex CORS and domain nesting.

---

## 1.4 Bearer Tokens & API Keys

### 1. What is it?
- **API Key**: A long, static string assigned to a client application (e.g., `sk_live_51Hz...`).
- **Bearer Token**: A security token where *the bearer (holder) of the token is granted access*, typically formatted as an HTTP header: `Authorization: Bearer <token>`.
- **Analogy**: A $100 bill or a concert wristband. The cashier does not care who you are, what your name is, or where you bought the bill. If you hold the $100 bill in your hand, you can spend it.

### 2. What did we have before?
Stateful browser sessions that failed for third-party developer integrations and headless batch jobs.

### 3. What problem does it solve?
Allows programmatic, machine-to-machine, and mobile-friendly access to APIs without browser cookie dependencies.

### 4. How does it work?
```
API Consumer                                           API Gateway / Resource Server
     |                                                              |
     | GET /v1/customers                                            |
     | X-API-Key: app_live_891023812093                             |
     | (OR Authorization: Bearer eyJhbGciOi...)                     |
     |------------------------------------------------------------->|
     |                                                              | 1. Hashes key: SHA256(key)
     |                                                              | 2. Validates against key store
     |                                                              | 3. Checks rate limits (e.g., 50 req/min)
     | 200 OK (Customer records)                                    |
     |<-------------------------------------------------------------|
```

### 5. Production Security Rules for API Keys:
1. **Never store API keys in plaintext**: Store their SHA-256 hash in the database. When a request arrives, hash the incoming key and compare hashes.
2. **Prefix your keys**: Use identifiable prefixes like `stripe_live_` or `ghp_` (GitHub personal access token) to enable automated secret scanners (e.g., GitHub Secret Scanning) to detect accidental commits.
3. **Enforce automatic key rotation**: Give each key a creation date and mandate 90-day rotations.

---

## 1.5 HMAC (Hash-based Message Authentication Code) Request Signing

### 1. What is it?
A cryptographic technique (RFC 2104) where the client and server share a secret key. Instead of sending the secret over the wire, the client hashes the HTTP request body, HTTP method, URL, and timestamp using the secret key (`HMAC-SHA256`) and sends the resulting signature. The server performs the identical calculation.
- **Analogy**: A wax seal stamped with the King's personal signet ring on an envelope containing an order. If anyone intercepts the letter and alters a single letter of the message, the broken wax seal proves the message was tampered with.

### 2. What did we have before?
Sending static API keys in headers. If a malicious actor intercepted the API key (e.g., via a compromised proxy or TLS interception), they could impersonate the client forever.

### 3. What problem does it solve?
1. **Confidentiality of the Secret**: The secret key is **never sent over the network**.
2. **Integrity Protection**: Guarantees that neither the URL, headers, nor the payload was modified in transit.
3. **Replay Attack Defense**: Incorporates timestamps and nonces so an eavesdropped packet cannot be resent 5 minutes later.

### 4. Step-by-Step Flow (The AWS Signature V4 Pattern)
```
Client (Has Secret Key "K_secret")               Server (Has Secret Key "K_secret")
  |                                                              |
  | 1. Creates Canonical Request:                                |
  |    Method: POST                                              |
  |    Path: /v1/payment                                         |
  |    PayloadHash: SHA256('{"amount":500}')                     |
  |    Timestamp: "2026-09-03T08:00:00Z"                         |
  |                                                              |
  | 2. Calculates Signature:                                     |
  |    Sig = HMAC_SHA256(K_secret, CanonicalRequest)             |
  |                                                              |
  | 3. Sends Request:                                            |
  |    POST /v1/payment                                          |
  |    X-Timestamp: 2026-09-03T08:00:00Z                         |
  |    X-Signature: a3f591c0b...                                 |
  |    Body: {"amount":500}                                      |
  |------------------------------------------------------------->|
  |                                                              | 4. Checks timestamp: |Now - RequestTime| < 5 min?
  |                                                              |    If > 5 min: REJECT (Prevent Replay)
  |                                                              | 5. Reconstructs Canonical Request
  |                                                              | 6. Calculates HMAC with stored K_secret
  |                                                              | 7. Compares signatures:
  |                                                              |    If LocalSig == X-Signature: ACCEPT!
  | 200 OK                                                       |
  |<-------------------------------------------------------------|
```

### 5. How to make it work (Java Production Code)
```java
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

public class HmacSigner {
    public static String calculateHmac(String data, String secretKey) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(
            secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256"
        );
        mac.init(secretKeySpec);
        byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(rawHmac);
    }
}
```

### 6. Pros & Cons
- **Pros**: Extreme security; zero credential leakage over wire; mathematically guarantees message integrity; used by AWS, Stripe, and high-security banking APIs.
- **Cons**: High computational overhead on both client and server; clock drift between servers causes legitimate requests to fail; complex client SDK development.

---

## 1.6 Mutual TLS (mTLS) & X.509 Client Certificates

### 1. What is it?
Standard TLS (HTTPS) only authenticates the **server** (your browser verifies Google's certificate). **Mutual TLS (mTLS)** forces **both sides** (Client and Server) to present, exchange, and cryptographically verify each other's X.509 digital certificates signed by a trusted Certificate Authority (CA).
- **Analogy**: In normal HTTPS, you walk up to a bank teller and ask to see their official badge (Server Auth). In Mutual TLS, the bank teller also demands your official government-issued biometric cryptographic smartcard before unlocking the front door (Mutual Auth).

### 2. What did we have before?
Application-layer authentication (passwords, tokens) over one-way TLS. If the application had a bug in token parsing, an attacker could bypass authentication entirely.

### 3. What problem does it solve?
Authentication occurs at **Layer 4 / Layer 7 Transport Handshake**, long before the application parses HTTP headers or executes business logic. Completely eliminates credential theft via application bugs.

### 4. How does it work?
```
Client (Has Client Cert + Private Key)         Server (Has Server Cert + Private Key)
       |                                                    |
       | 1. ClientHello (TLS Version, Ciphers)              |
       |--------------------------------------------------->|
       | 2. ServerHello, Server Certificate,                |
       |    CertificateRequest (Demands Client Cert!)       |
       |<---------------------------------------------------|
       |                                                    |
 [Client verifies Server Cert against Root CA]              |
       |                                                    |
       | 3. Client Certificate,                             |
       |    CertificateVerify (Signed with Client's PrivKey)|
       |--------------------------------------------------->|
       |                                                    |
       |                        [Server verifies Client Cert against Internal CA]
       | 4. Session Keys Established (mTLS Tunnel Open)     |
       |<==================================================>|
```

### 5. Pros & Cons
- **Pros**: Unbreakable cryptographic authentication; zero passwords to leak; hardware-backed security (keys stored on TPM or YubiKey); default standard for zero-trust microservice meshes (Istio/Linkerd).
- **Cons**: Massive operational complexity; managing certificate expiration and revocation (CRLs / OCSP); difficult to implement on public consumer devices.

---

## 1.7 Passwordless & Modern MFA (FIDO2, WebAuthn, Passkeys, TOTP)

### 1. What is it?
- **TOTP (Time-based One-Time Password - RFC 6238)**: A shared secret seed + current 30-second Unix time window hashed via HMAC to generate a 6-digit code (Google Authenticator).
- **FIDO2 / WebAuthn / Passkeys**: Asymmetric public-key cryptography built into operating systems and hardware chips (Apple FaceID, Windows Hello, YubiKeys). The private key never leaves the device's secure enclave; only the public key is registered with the server.
- **Analogy**: Instead of memorizing a 16-character password, your laptop’s biometric fingerprint chip signs a cryptographic challenge directly for the website.

### 2. What did we have before?
SMS 2FA (vulnerable to SIM-swapping) and passwords (vulnerable to phishing, credential stuffing, and data breaches).

### 3. Why Passkeys are 100% Phishing-Resistant:
During a WebAuthn ceremony, the browser cryptographically binds the domain name (`example.com`) to the challenge signature. If an attacker tricks you into visiting `examp1e.com`, the browser refuses to use the passkey for `example.com`.

---

# 🛡️ Phase 2: All Available Authorization Models

Once a user’s identity is proven (AuthN), the system must decide: **Is this user allowed to perform this operation on this specific resource?**

```
+---------------------------------------------------------------------------------------+
|                              AUTHORIZATION EVOLUTION                                  |
|                                                                                       |
|   DAC / MAC              RBAC                   ABAC                   ReBAC / PBAC   |
|  (File/OS Level)     (Role-Based)         (Attribute-Based)         (Graph & Policy)  |
|  1970s - 1980s       1990s - 2000s          2010s - 2020s             2020s - 2026+   |
|                                                                                       |
|   Owner decides /     User -> Role ->       Rules on User,          Relationship graph|
|   Hardcoded labels.   Permission.           Resource, Time,         (Google Zanzibar) |
|   Rigid.              Role Explosion!       Location, Device.       or OPA Rego code. |
+---------------------------------------------------------------------------------------+
```

## 2.1 DAC (Discretionary Access Control)
- **Concept**: The **owner** of the resource has complete discretion to grant access to others.
- **Example**: UNIX file permissions (`chmod 755 report.pdf`). Alice owns the file; Alice decides Bob can read it.
- **Downside**: Zero centralized governance; if a rogue employee marks confidential files public, central IT cannot easily stop it.

## 2.2 MAC (Mandatory Access Control)
- **Concept**: Access is governed by a central authority based on fixed security classifications and labels (e.g., Top Secret, Secret, Unclassified).
- **Rule (Bell-LaPadula model)**: "No read up, no write down". A user with "Secret" clearance cannot read "Top Secret" files, and cannot write to "Unclassified" files (to prevent data leaks).
- **Where used**: SELinux, military operating systems, government defense intelligence networks.

## 2.3 RBAC (Role-Based Access Control) & The Role Explosion Trap

### 1. What is it?
Users are assigned **Roles**, and Roles are assigned **Permissions**. The application code checks roles, not individual users.
- **Analogy**: A hospital. You don't grant "Alice" the right to view patient charts. You give Alice the role of `DOCTOR`. The `DOCTOR` role has the permission `CHART_READ`.

### 2. The Fatal Flaw: The Role Explosion Trap
As business requirements grow, RBAC collapses under its own weight:
- "Doctors can edit charts" -> `ROLE_DOCTOR`
- "Only doctors in Oncology can edit oncology charts" -> `ROLE_ONCOLOGY_DOCTOR`
- "Only doctors in Oncology on night shift in Building B can edit charts" -> `ROLE_ONCOLOGY_DOCTOR_NIGHT_BLDG_B`
Within 3 years, an enterprise accumulates 5,000 distinct roles for 1,000 employees. Managing it becomes impossible.

---

## 2.4 ABAC (Attribute-Based Access Control)

### 1. What is it?
Instead of static roles, access decisions are computed dynamically at runtime using Boolean expressions over **Attributes**:
1. **Subject Attributes**: User's department, clearance, seniority, title.
2. **Resource Attributes**: Document classification, document owner, department.
3. **Action Attributes**: Read, Write, Delete, Approve.
4. **Environment Attributes**: Current time, user's IP geolocation, device compliance state (is disk encrypted?).

### 2. The ABAC Rule Example:
$$\text{ALLOW if } (\text{Subject.Department} == \text{Resource.Department}) \land (\text{Time} \text{ between } 08:00\text{ and } 18:00) \land (\text{Device.Compliant} == \text{true})$$
- **Pros**: Solves the Role Explosion trap; infinite expressiveness.
- **Cons**: Complex to audit; high latency when evaluating complex rules across multiple microservices.

---

## 2.5 ReBAC (Relationship-Based Access Control / Google Zanzibar)

### 1. What is it?
Pioneered by Google's Zanzibar paper (used across Google Drive, YouTube, Google Cloud), ReBAC determines access based on **relationships in a graph**.
- **Analogy**: Google Drive. Document $D$ is inside Folder $F$. Folder $F$ is shared with Group $G$. User Alice is a member of Group $G$. Therefore, Alice can read Document $D$.

### 2. How it works?
Access is stored as a tuple: `<object>#<relation>@<subject>`:
- `document:q3_budget#parent@folder:finance_2026`
- `folder:finance_2026#viewer@group:accounting_team`
- `group:accounting_team#member@user:alice`
To check `Can Alice view document:q3_budget?`, the engine executes a fast distributed graph traversal.
- **Modern Implementations**: OpenFGA, Ory Keto, Auth0 Fine-Grained Authorization (FGA).

---

## 2.6 PBAC (Policy-Based Access Control / Open Policy Agent Rego)

### 1. What is it?
Decouples authorization logic completely from application code by treating **Policy as Code**. The application makes an HTTP/gRPC call to a dedicated policy engine (such as **Open Policy Agent - OPA**) passing the input context. OPA evaluates the policy written in a declarative language (**Rego**) and returns `{"allow": true}` or `{"allow": false}`.

### 2. Practical OPA Rego Policy:
```rego
package authz

default allow = false

# Allow access if user is admin
allow {
    input.user.role == "ADMIN"
}

# Allow doctors to view patients only if assigned to that patient
allow {
    input.user.role == "DOCTOR"
    input.action == "READ"
    input.patient.assigned_doctor_id == input.user.id
}
```

---

# 🏢 Phase 3: Single Sign-On (SSO) & Enterprise Identity Federation

## 3.1 What is SSO & The Multi-App Nightmare

### 1. What is it?
Single Sign-On (SSO) is an architectural pattern that allows a user to authenticate once with a centralized authority and gain seamless, authenticated access to dozens of independent, unrelated software applications without re-typing their username and password.
- **Analogy**: A universal ski pass or a Disney World MagicBand. You buy and activate your wristband once at the resort entrance (Central IdP). As you move between Space Mountain, Epcot restaurants, and the hotel pool, you tap your wristband. You never purchase individual tickets or prove who you are again at each gate.

### 2. What did we have before? (The Multi-App Nightmare)
In the 1990s and early 2000s, every company had 20 different internal web applications:
- Webmail on `mail.company.com`
- HR Portal on `hr.workplace.com`
- Jira on `jira.company.com`
- Salesforce on `salesforce.com`

Each application had its own independent MySQL database table containing usernames and passwords.
- **Disasters**:
  1. **Password Fatigue**: Employees had 20 different passwords, wrote them on sticky notes stuck to monitors.
  2. **Termination Security Nightmare**: When an employee was fired, IT had to manually log into 20 different admin consoles to deactivate the account. If IT forgot one tool (e.g., Salesforce), the ex-employee retained access to company IP.
  3. **Credential Theft**: A breach in one low-security internal app leaked the employee's corporate password.

```
THE OLD WAY (Siloed Credentials):
User ---> [App 1: Jira]       ---> Local DB 1 (Users & Passwords)
User ---> [App 2: Salesforce] ---> Local DB 2 (Users & Passwords)
User ---> [App 3: HR Portal]  ---> Local DB 3 (Users & Passwords)
(User has 3 passwords. IT manages 3 databases.)

THE MODERN SSO WAY (Federated Identity):
User ---> [CENTRAL IDENTITY PROVIDER (Okta / Azure AD)] ---> Single Directory (Active Directory)
                   |               |               |
                   v (Tokens)      v (Tokens)      v (Tokens)
              [App 1: Jira]   [App 2: Salesforce] [App 3: HR Portal]
(User logs in ONCE. IT deactivates account in ONE place.)
```

---

## 3.2 The Cross-Domain Cookie Barrier & Federation

### Why couldn't we just use regular cookies for SSO?
Web browsers enforce the strict **Same-Origin Policy (SOP)**. A cookie created by `okta.enterprise.com` **can never be read** by `salesforce.com` or `servicenow.com`. If browsers allowed cross-domain cookie reading, any malicious website could read your bank's session cookies.

### How Federation Solves This
Identity Federation bypasses the cookie boundary using standard browser redirects and cryptographically signed security tokens (SAML Assertions or OIDC ID Tokens). The browser acts as an intermediary messenger carrying signed verification tickets between the domain boundaries.

---

## 3.3 Identity Provider (IdP) vs. Service Provider (SP)
- **Identity Provider (IdP)**: The authoritative system that stores credentials, manages multi-factor authentication, and issues identity proofs. Examples: **Microsoft Entra ID (Azure AD), Okta, Ping Identity, Keycloak**.
- **Service Provider (SP) / Relying Party (RP)**: The target application that provides business services to the user and delegates login decisions to the IdP. Examples: **Salesforce, GitHub Enterprise, Zoom, Workday, your custom Spring Boot API**.

---

# 📜 Phase 4: SAML 2.0 (Security Assertion Markup Language)

## 4.1 What is SAML 2.0?
SAML 2.0 (OASIS Standard, 2005) is an enterprise standard for exchanging authentication and authorization data between an IdP and an SP using **XML** documents that are cryptographically signed using **XML Digital Signatures (XMLDSig)**.

---

## 4.2 SP-Initiated SSO Flow (Step-by-Step)
This is the most common SSO flow in enterprise IT:

```
User Browser                     Service Provider (e.g., Zoom)              Identity Provider (e.g., Okta)
     |                                        |                                        |
     | 1. User browses to zoom.us             |                                        |
     |--------------------------------------->|                                        |
     |                                        | 2. SP generates <AuthnRequest>         |
     |                                        |    deflated & base64 encoded           |
     | 3. HTTP 302 Redirect to Okta           |                                        |
     |    Location: okta.com/sso?SAMLRequest=...                                       |
     |<---------------------------------------|                                        |
     |                                                                                 |
     | 4. Browser follows redirect to Okta: GET /sso?SAMLRequest=...                   |
     |-------------------------------------------------------------------------------->|
     |                                                                                 | 5. IdP challenges user:
     |                                                                                 |    Prompts for MFA / Password
     | 6. User enters credentials & MFA                                                |
     |<===============================================================================>|
     |                                                                                 | 7. IdP verifies credentials
     |                                                                                 | 8. IdP builds SAML Response XML
     |                                                                                 | 9. IdP signs XML with Private Key
     | 10. HTTP 200 OK with Auto-submitting HTML Form                                  |
     |     <form action="zoom.us/saml/sso" method="POST">                              |
     |       <input type="hidden" name="SAMLResponse" value="PD94bWwg...base64..." />  |
     |     </form>                                                                     |
     |<--------------------------------------------------------------------------------|
     |                                                                                 |
     | 11. Browser automatically executes form.submit() via JS:                        |
     |     POST zoom.us/saml/sso with SAMLResponse payload                             |
     |--------------------------------------->|                                        |
     |                                        | 12. Zoom parses XML                    |
     |                                        | 13. Zoom verifies signature with       |
     |                                        |     Okta's Public X.509 Certificate    |
     |                                        | 14. Checks expiration, audience & nonce|
     |                                        | 15. Extracts User: "alice@company.com" |
     |                                        | 16. Creates local session cookie       |
     | 17. HTTP 302 Redirect to /dashboard    |                                        |
     |<---------------------------------------|                                        |
     |                                        |                                        |
     | 18. GET zoom.us/dashboard (Logged In!) |                                        |
     |--------------------------------------->|                                        |
```

---

## 4.3 IdP-Initiated SSO Flow
In IdP-Initiated SSO, the user does not start at Zoom. Instead, the user logs into their corporate Okta/Azure portal, sees an icon grid of 50 apps, and clicks the **"Zoom"** button. Okta immediately generates the signed `SAMLResponse` and posts it directly to Zoom without an initial `AuthnRequest`.
- **Security Warning**: IdP-initiated SSO is vulnerable to **CSRF attacks** because the SP cannot match the response against an in-flight request nonce. Modern security architectures prefer **SP-Initiated SSO**.

---

## 4.4 Anatomy of a SAML XML Assertion
Below is what the base64-decoded `SAMLResponse` actually looks like under the hood:

```xml
<samlp:Response xmlns:samlp="urn:oasis:names:tc:SAML:2.0:protocol"
                ID="_d3e4f5a6" Version="2.0" IssueInstant="2026-09-03T08:15:00Z"
                Destination="https://zoom.us/saml/sso">
    <saml:Issuer xmlns:saml="urn:oasis:names:tc:SAML:2.0:assertion">
        https://identity.okta.com/app/zoom/12345/sso/saml
    </saml:Issuer>
    <samlp:Status>
        <samlp:StatusCode Value="urn:oasis:names:tc:SAML:2.0:status:Success"/>
    </samlp:Status>
    
    <!-- THE ASSERTION (IDENTITY PROOF) -->
    <saml:Assertion ID="_a1b2c3d4" IssueInstant="2026-09-03T08:15:00Z" Version="2.0">
        <saml:Issuer>https://identity.okta.com/app/zoom/12345/sso/saml</saml:Issuer>
        
        <!-- WHO IS THE USER? -->
        <saml:Subject>
            <saml:NameID Format="urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress">
                alice.smith@enterprise.com
            </saml:NameID>
        </saml:Subject>
        
        <!-- TIME VALIDITY & AUDIENCE RESTRICTIONS -->
        <saml:Conditions NotBefore="2026-09-03T08:14:30Z" NotOnOrAfter="2026-09-03T08:20:00Z">
            <saml:AudienceRestriction>
                <saml:Audience>https://zoom.us</saml:Audience>
            </saml:AudienceRestriction>
        </saml:Conditions>
        
        <!-- ATTRIBUTES PASSED TO SERVICE PROVIDER -->
        <saml:AttributeStatement>
            <saml:Attribute Name="firstName"><saml:AttributeValue>Alice</saml:AttributeValue></saml:Attribute>
            <saml:Attribute Name="lastName"><saml:AttributeValue>Smith</saml:AttributeValue></saml:Attribute>
            <saml:Attribute Name="department"><saml:AttributeValue>Engineering</saml:AttributeValue></saml:Attribute>
            <saml:Attribute Name="role"><saml:AttributeValue>LEAD_ARCHITECT</saml:AttributeValue></saml:Attribute>
        </saml:AttributeStatement>

        <!-- CRYPTOGRAPHIC DIGITAL SIGNATURE -->
        <ds:Signature xmlns:ds="http://www.w3.org/2000/09/xmldsig#">
            <ds:SignedInfo>...</ds:SignedInfo>
            <ds:SignatureValue>MIIB6wYJKoZIhvcNAQcCoIIB3zCCAdcCAQEx...</ds:SignatureValue>
        </ds:Signature>
    </saml:Assertion>
</samlp:Response>
```

---

## 4.5 SAML Single Logout (SLO) & Security Traps (XSW Attacks)

### SAML Single Logout (SLO)
When Alice logs out of Zoom, she expects to be logged out of Okta and Salesforce too. The SP sends a `<LogoutRequest>` to the IdP. The IdP broadcasts `<LogoutRequest>` messages to all active SPs where Alice logged in, terminating every session.

### The XML Signature Wrapping (XSW) Vulnerability
One of the most famous vulnerabilities in SAML history. Because XML allows nested elements, an attacker intercepts a legitimate SAML response from Okta for `user: bob`. The attacker clones the signed assertion, moves it into an unverified wrapper, and injects a fake un-signed assertion for `user: admin`. If the XML parser validates the signature on the original block but extracts the username from the unverified wrapper, **the attacker gains instant full Admin access!**
- **Prevention**: Always use hardened, battle-tested SAML libraries (e.g., Spring Security SAML, Shibboleth); never write your own XML DOM parsing logic.

---

## 4.6 How to Configure SAML 2.0 in Production
In modern Spring Boot 3 applications, SAML 2.0 SP integration is entirely metadata-driven:

```yaml
# application.yml
spring:
  security:
    saml2:
      relyingparty:
        registration:
          okta:
            signing:
              credentials:
                - private-key-location: "classpath:credentials/sp-private-key.pem"
                  certificate-location: "classpath:credentials/sp-certificate.crt"
            assertingparty:
              metadata-uri: "https://dev-12345.okta.com/app/exk.../sso/saml/metadata"
```

---

# 🌲 Phase 5: Directory Services: Active Directory, LDAP, Kerberos & Azure AD (Entra ID)

Every Fortune 500 company runs on Directory Services. To understand enterprise identity, you must master the relationship between **LDAP**, **Kerberos**, **Active Directory**, and **Azure AD**.

```
+---------------------------------------------------------------------------------------+
|                       THE ENTERPRISE DIRECTORY STACK                                  |
|                                                                                       |
|   1. LDAP (The Protocol)                   2. KERBEROS (The Authenticator)            |
|   "Querying the Phonebook"                 "The 3-Headed Ticket Guard"                |
|   Hierarchical database search:            High-speed LAN ticket exchange.            |
|   dc=corp, dc=com -> ou=Eng -> cn=Alice    No passwords sent over wire.               |
|                                                                                       |
|   3. ACTIVE DIRECTORY (AD DS)                                                         |
|   Microsoft's on-premises empire combining LDAP + Kerberos + DNS into one server.     |
|                                                                                       |
|   4. AZURE AD / ENTRA ID (The Cloud Modernizer)                                       |
|   Bridges on-prem Kerberos/LDAP to modern cloud SAML 2.0 and OpenID Connect (OIDC).   |
+---------------------------------------------------------------------------------------+
```

---

## 5.1 LDAP (Lightweight Directory Access Protocol)

### 1. What is it?
Defined in RFC 4511, LDAP is an application protocol for querying and modifying a centralized, hierarchical directory service optimized for **extremely fast read operations**.
- **Analogy**: The corporate phone directory. When an employee logs in or an app needs to check "Which department is Bob in?", it queries the directory tree.

### 2. The LDAP Directory Information Tree (DIT)
Data in LDAP is organized as an inverted tree using **Distinguished Names (DN)**:
```
               dc=enterprise, dc=com              (Domain Component)
                        |
            +-----------+-----------+
            |                       |
       ou=Engineering          ou=Finance         (Organizational Unit)
            |                       |
       +----+----+                  |
       |         |                  |
    cn=Alice  cn=Bob             cn=Charlie       (Common Name)
```
- Full Distinguished Name for Alice: `cn=Alice Smith,ou=Engineering,dc=enterprise,dc=com`

### 3. Core LDAP Operations:
- **Bind**: Authenticate a client to the directory (e.g., verifying user credentials).
- **Search**: Query the directory using filter expressions:
  `(&(objectClass=user)(department=Engineering)(mail=*@enterprise.com))`

---

## 5.2 Kerberos: The Three-Headed Dog (Tickets, TGT, KDC, ST)

### 1. What is it?
Named after the mythological three-headed guard dog of Hades, Kerberos is a computer network authentication protocol developed at MIT that operates on the basis of **Tickets** to allow nodes communicating over a non-secure network to prove their identity to one another securely.
- **Analogy**: An amusement park. Instead of paying cash at every roller coaster, you go to the Central Ticket Booth once. The booth checks your ID and gives you a special waterproof stamped wristband (TGT). At each ride, you show your wristband to get a ride token (Service Ticket). You never show cash or ID again.

### 2. The Three Heads of Kerberos:
1. **Client**: The workstation or user requesting access.
2. **KDC (Key Distribution Center)**: The trusted third-party server running two services:
   - **AS (Authentication Server)**: Issues the initial Ticket Granting Ticket (TGT).
   - **TGS (Ticket Granting Server)**: Issues specific Service Tickets (ST).
3. **Application Server (Target Service)**: The file server, SQL database, or web server the client wants to reach.

### 3. Step-by-Step Kerberos Ticket Exchange
```
Client (Alice)                        KDC: AS / TGS                     Target Server (File Server)
   |                                        |                                        |
   | 1. AS-REQ: "I am Alice, give me a TGT" |                                        |
   |--------------------------------------->|                                        |
   |                                        | 2. KDC looks up Alice's password hash  |
   |                                        | 3. Creates TGT (encrypted with KDC key)|
   |                                        | 4. Creates Session Key                 |
   | 5. AS-REP: Returns TGT + SessionKey    |                                        |
   |<---------------------------------------|                                        |
   |                                                                                 |
[Alice's PC decrypts SessionKey using Alice's password hash. TGT remains encrypted]  |
   |                                                                                 |
   | 6. TGS-REQ: "Here is my TGT. Give me a Service Ticket for FileServer!"          |
   |--------------------------------------->|                                        |
   |                                        | 7. KDC decrypts TGT with its secret key|
   |                                        | 8. Verifies Alice's identity           |
   |                                        | 9. Generates Service Ticket (ST)       |
   |                                        |    (encrypted with FileServer's key)   |
   | 10. TGS-REP: Returns Service Ticket    |                                        |
   |<---------------------------------------|                                        |
   |                                                                                 |
   | 11. AP-REQ: "Here is my Service Ticket! Let me read the financial files."       |
   |-------------------------------------------------------------------------------->|
   |                                                                                 | 12. FileServer decrypts
   |                                                                                 |     ticket with its secret key
   |                                                                                 | 13. Confirms Alice is legit!
   | 14. Access Granted! (File stream begins)                                        |
   |<--------------------------------------------------------------------------------|
```

### 4. Why Kerberos Fails on the Public Internet:
1. Requires direct UDP/TCP access on port 88 to the internal KDC (you cannot expose KDC port 88 to the public internet).
2. Requires tight clock synchronization (clocks must be synchronized within 5 minutes or replay protection rejects the tickets).
3. Not firewall/HTTP friendly (relies on raw binary socket connections).

---

## 5.3 Active Directory Domain Services (AD DS)
Microsoft took **LDAP** (for storage/queries), **Kerberos** (for authentication), and **DNS** (for location resolution) and wrapped them into a unified, enterprise-grade operating system service: **Active Directory**.
When you log into your Windows corporate laptop in the morning with `CORP\asmith`, Windows negotiates a Kerberos ticket with the local Active Directory Domain Controller (DC).

---

## 5.4 Modern Evolution: Azure AD / Microsoft Entra ID
On-premises Active Directory cannot authenticate mobile phones on 5G or SaaS apps like Salesforce and Zoom over the public web.
Enter **Microsoft Entra ID (formerly Azure Active Directory)**:
- **Cloud-Native Identity**: Speaks modern internet protocols: **HTTPS, SAML 2.0, OAuth 2.0, and OpenID Connect (OIDC)**.
- **Azure AD Connect**: A sync engine that continuously hashes and synchronizes on-prem AD passwords to the cloud.
- **Seamless SSO**: When a corporate laptop on a Kerberos network visits a web app, the browser transparently obtains an Azure AD Kerberos ticket and exchanges it for a modern OIDC/SAML token!

---

# ⚡ Phase 6: OAuth 2.0 & OpenID Connect (OIDC)

## 6.1 The Valet Key Analogy: Why OAuth 2.0 is NOT Authentication

### 1. What is OAuth 2.0?
OAuth 2.0 (RFC 6749) is a **Delegated Authorization Framework**. It enables a third-party application to obtain limited access to an HTTP service on behalf of a resource owner without sharing credentials.
- **The Classic Valet Key Analogy**: When you valet park your luxury car, you do not give the valet your master key (which unlocks your glove compartment, trunk, and ignition). You give the valet a special **Valet Key**. The valet key can start the engine and drive 1 mile, but cannot open the trunk or glove box. **OAuth Access Tokens are valet keys.**

```
+---------------------------------------------------------------------------------------+
|                           THE DANGEROUS SECURITY MYTH:                                |
|                        "We use OAuth 2.0 for Logging In!"                             |
|                                                                                       |
|   OAuth 2.0 alone DOES NOT tell you WHO the user is!                                  |
|   An OAuth Access Token is a capability credential ("Can read photos").               |
|   It does NOT contain identity proof, authentication time, or user email.             |
|                                                                                       |
|   SOLUTION: OPENID CONNECT (OIDC) = OAuth 2.0 + Identity Layer (ID Token)             |
+---------------------------------------------------------------------------------------+
```

---

## 6.2 OpenID Connect (OIDC): Identity on Top of OAuth 2.0
OpenID Connect 1.0 is a simple identity layer built on top of the OAuth 2.0 protocol. It extends OAuth 2.0 by introducing:
1. An **ID Token**: A cryptographically signed JSON Web Token (JWT) containing verified claims about the user's identity.
2. A **UserInfo Endpoint**: A protected HTTP endpoint to fetch additional profile data.
3. Standardized scopes: `openid`, `profile`, `email`.

---

## 6.3 Token Trio: ID Token vs. Access Token vs. Refresh Token
| Token | Type | Intended Audience | Purpose | Lifespan | Format |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **ID Token** | Authentication | The **Client Application** (SPA, Mobile App, Backend). | Proves the user logged in; contains user claims (`sub`, `email`, `name`). | Short (5 - 15 min). | Strictly **JWT** (JSON Web Token). |
| **Access Token** | Authorization | The **Resource Server** (The backend microservices / API). | Grants permission to execute operations (`scope: read:orders`). | Very Short (5 - 60 min). | JWT or Opaque string. |
| **Refresh Token** | Delegation | The **Authorization Server** (Okta, Keycloak, Auth0). | Used silently to request new Access/ID tokens when they expire. | Long (Days to Months). | Strictly Opaque string. |

---

## 6.4 OAuth 2.0 Grant Flows

```
+------------------------------------------------------------------------------------+
|                       MODERN OAUTH 2.0 GRANT FLOWS (2026)                          |
|                                                                                    |
|   1. Authorization Code Flow with PKCE  -> For SPAs, Mobile Apps & Web Apps        |
|   2. Client Credentials Flow            -> For Microservice to Microservice (M2M)  |
|   3. Refresh Token Flow                 -> For Silent Token Renewal                |
+------------------------------------------------------------------------------------+
```

### Flow 1: Authorization Code with PKCE (Proof Key for Code Exchange)
PKCE (RFC 7636, pronounced "pixy") is the **mandatory gold standard** for all modern applications to prevent Authorization Code interception attacks.

```
Browser / Mobile App                   Authorization Server (Auth0 / Okta)             Resource Server (API)
       |                                                |                                       |
[Generates Code Verifier: random string]                |                                       |
[Generates Code Challenge: SHA256(Verifier)]            |                                       |
       |                                                |                                       |
       | 1. GET /authorize?                             |                                       |
       |    response_type=code                          |                                       |
       |    &client_id=my_client                        |                                       |
       |    &scope=openid profile orders                |                                       |
       |    &code_challenge=xyz789                      |                                       |
       |    &code_challenge_method=S256                 |                                       |
       |----------------------------------------------->|                                       |
       |                                                | 2. User logs in & grants consent      |
       | 3. HTTP 302 Redirect to Client with Auth Code: |                                       |
       |    /callback?code=AUTH_CODE_42                 |                                       |
       |<-----------------------------------------------|                                       |
       |                                                |                                       |
       | 4. POST /oauth/token                           |                                       |
       |    grant_type=authorization_code               |                                       |
       |    &code=AUTH_CODE_42                          |                                       |
       |    &code_verifier=ORIGINAL_RANDOM_STRING       |                                       |
       |----------------------------------------------->|                                       |
       |                                                | 5. Compares:                          |
       |                                                |    SHA256(verifier) == Challenge?     |
       |                                                |    If MATCH: Issues tokens!           |
       | 6. Returns: {id_token, access_token, refresh}  |                                       |
       |<-----------------------------------------------|                                       |
       |                                                                                        |
       | 7. GET /api/v1/orders (Authorization: Bearer <access_token>)                           |
       |--------------------------------------------------------------------------------------->|
       |                                                                                        | 8. Verifies JWT
       | 9. HTTP 200 OK (Orders JSON data)                                                      |
       |<---------------------------------------------------------------------------------------|
```

### Flow 2: Client Credentials Flow (Machine-to-Machine)
Used when there is **no user involved**. Microservice A (e.g., Inventory Worker) needs to talk to Microservice B (e.g., Shipping Service).
```http
POST /oauth/token HTTP/1.1
Host: auth.enterprise.com
Content-Type: application/x-www-form-urlencoded

grant_type=client_credentials
&client_id=inventory_service_id
&client_secret=super_secret_password_123
&scope=shipping:create
```

---

## 6.5 Deprecated & Forbidden Flows (Implicit & Password Grant)
1. **Implicit Grant (DEPRECATED & BANNED)**:
   - Returned access tokens directly in the URL hash (`#access_token=...`).
   - Vulnerable to browser history snooping, referrer header leaks, and access token theft.
   - **Replacement**: Authorization Code with PKCE.
2. **Resource Owner Password Credentials - ROPC (DEPRECATED & BANNED)**:
   - Client asks the user for their username and password directly and sends them to the token endpoint.
   - Destroys the entire purpose of OAuth (delegation without sharing passwords); makes MFA impossible.

---

## 6.6 OIDC Discovery (`.well-known`) & JWKS Key Rotation
How does a backend microservice verify that an incoming JWT was genuinely signed by the Authorization Server without storing a shared password?
1. The Authorization Server signs tokens using an **asymmetric private key (RSA/ECDSA)**.
2. The server exposes a public discovery document:
   `https://auth.company.com/.well-known/openid-configuration`
3. This links to the **JWKS (JSON Web Key Set)** endpoint:
   `https://auth.company.com/.well-known/jwks.json`
```json
{
  "keys": [
    {
      "kty": "RSA",
      "e": "AQAB",
      "use": "sig",
      "kid": "auth-key-2026-09",
      "alg": "RS256",
      "n": "u1P5Qe7sL9...public_modulus..."
    }
  ]
}
```
4. Microservices cache these public keys. When a token arrives with `kid: "auth-key-2026-09"`, the service verifies the signature locally in **0.1 milliseconds** with zero network calls!

---

## 6.7 Production Spring Security 6 / OIDC Implementation
```yaml
# application.yml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: "https://auth.enterprise.com"
          jwk-set-uri: "https://auth.enterprise.com/.well-known/jwks.json"
```
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/public/**").permitAll()
                .requestMatchers("/api/orders/**").hasAuthority("SCOPE_orders:read")
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
            .build();
    }
}
```

---

# 🕸️ Phase 7: Microservices Security & Zero-Trust Architecture

## 7.1 The Death of the "Castle-and-Moat" Perimeter
The traditional security model was the **Castle and Moat**:
- Everything inside the corporate network or Kubernetes cluster was considered "trusted".
- Firewalls guarded the edge.
- **Why this caused catastrophes**: Once an attacker breached a single low-security microservice (or via an employee phishing email), the entire internal network was wide open. The attacker moved laterally across databases with zero internal resistance.

**The Zero-Trust Architecture (NIST SP 800-207)**:
> *"Never Trust, Always Verify."*  
> Treat every internal network packet as if it is traversing the hostile public internet. Every service call must be authenticated, authorized, and encrypted.

```
THE FLAWED CASTLE-AND-MOAT MODEL:
[Public Internet] ===(Firewall)====> [Internal Cluster: All Services Trust Each Other In Cleartext HTTP]
                                      Service A ---> Service B ---> Database (No Auth!)

THE ZERO-TRUST MODEL:
[Public Internet] ===(Gateway)=====> [Service A] ===(mTLS + JWT)===> [Service B] ===(mTLS + Auth)===> [Database]
                                     (Every single internal link is encrypted & authenticated)
```

---

## 7.2 Stateless JWT vs. Distributed Sessions
| Dimension | Stateless JWT Tokens | Distributed Sessions (Redis) |
| :--- | :--- | :--- |
| **Validation Latency** | **$0.1\text{ ms}$** (Local cryptographic signature math). | **$2 - 5\text{ ms}$** (Network round-trip to Redis cluster). |
| **Database Scalability** | Infinite (Zero database hits on read). | Redis memory & connection pool limits under 100,000 RPS. |
| **Revocation Speed** | **Hard**. Token remains valid until expiration. | **Instantaneous** (Delete session key from Redis). |
| **Token Size** | Large ($1 - 2\text{ KB}$ HTTP header overhead). | Tiny ($32\text{ bytes}$ session ID). |

---

## 7.3 Token Revocation Problem & Distributed Blacklists
Because JWTs are stateless, if Alice's laptop is stolen at 12:00 PM, and her JWT expires at 1:00 PM, the thief can execute API calls for 60 minutes even if the admin clicks "Deactivate Account".
### The 3 Enterprise Mitigation Strategies:
1. **Ultra-Short Lifespans**: Set Access Token TTL to **5 to 10 minutes**, paired with silent refresh token rotation. The window of exposure is minimal.
2. **Distributed Redis Blacklist**:
   - When a user logs out or is revoked, publish their `jti` (JWT ID) or `userId` to a Redis cluster with an expiration equal to the token's remaining lifespan.
   - Microservices check Redis for blacklisted `jti`s. (Introduces a Redis lookup, but only for revoked tokens or edge gateways).
3. **Bloom Filters in Memory**:
   - For ultra-high-throughput systems, propagate revoked `jti` values to service memory using a replicated Bloom Filter.

---

## 7.4 Token Propagation vs. Token Exchange (RFC 8693 On-Behalf-Of)

```
Client -> [API Gateway] -> [Order Service] -> [Payment Service]
```
- **Anti-Pattern (Blind Token Forwarding)**: The Order Service passes the user's raw incoming JWT directly to the Payment Service.
  - *Risk*: The Payment Service receives permissions to do things the Order Service should never have access to. If the Order Service is compromised, an attacker abuses the forwarded token.
- **The Modern Pattern (RFC 8693 Token Exchange / On-Behalf-Of Flow)**:
  - Order Service calls the Authorization Server:
    *"I am Order Service. I have User Alice's token. Exchange this for a down-scoped token specifically for Payment Service."*
  - The new token has:
    `aud: payment-service`, `act: {sub: "order-service"}`, `sub: "alice"`.

---

## 7.5 Service-to-Service Zero-Trust (mTLS, SPIFFE/SPIRE, Service Mesh)
In a modern Kubernetes cluster, service-to-service communication is secured via a **Service Mesh (Istio, Linkerd)**:
1. Every microservice pod contains an **Envoy Proxy Sidecar**.
2. **SPIFFE (Secure Production Identity Framework for Everyone)** issues every pod an X.509 SVID (SPIFFE Verifiable Identity Document):
   `spiffe://cluster.local/ns/prod/sa/order-service-sa`
3. When `Order Service` calls `Payment Service`:
   - The two Envoy sidecars perform an automated **mTLS handshake**.
   - Envoy verifies the cryptographic identity and enforces authorization policies before the request touches application code.

---

# 🚪 Phase 8: API Gateway Security Architecture

## 8.1 The Gateway as Policy Enforcement Point (PEP)
The API Gateway acts as the secure front door of the enterprise, acting as the **PEP (Policy Enforcement Point)** while internal microservices act as **Resource Servers**.

```
                           THE ENTERPRISE PERIMETER
                                      │
[Untrusted Public Internet]           ▼
┌──────────────────────────────────────────────────────────────────────────┐
│                           API GATEWAY (PEP)                              │
│  1. TLS 1.3 Termination (Certificates)                                   │
│  2. Web Application Firewall (WAF - SQLi, XSS, Bot detection)            │
│  3. IP Geo-Fencing & Rate Limiting (Token Bucket / Redis)                │
│  4. Phantom Token Exchange (Opaque Token -> Internal Signed JWT)        │
│  5. CORS Policy Enforcement                                              │
└────────────────────────────────────┬─────────────────────────────────────┘
                                     │ (mTLS + Internal Signed JWT)
                ┌────────────────────┴────────────────────┐
                ▼                                         ▼
     ┌─────────────────────┐                   ┌─────────────────────┐
     │   Order Service     │                   │   Catalog Service   │
     └─────────────────────┘                   └─────────────────────┘
```

---

## 8.2 The Phantom Token Pattern (Opaque at Edge -> JWT Internally)

### 1. The Problem
Exposing signed JWTs directly to public web browsers or mobile apps leaks internal architecture details (microservice names, internal roles, database IDs) and increases payload size over cellular networks.

### 2. The Phantom Token Architecture:
1. **At the Edge (Public Internet)**: The client only receives a 32-byte cryptographically random, **opaque reference token** (e.g., `ref_77a9b0c2e`).
2. **At the API Gateway**:
   - The Gateway intercepts the incoming `Authorization: Bearer ref_77a9b0c2e`.
   - The Gateway looks up or introspects the token against the authorization server (or memory cache).
   - The Gateway swaps the opaque token for a full, signed **JWT** containing user claims and scopes.
3. **Internally**: The Gateway forwards the rich JWT to internal microservices over mTLS. Internal microservices remain 100% stateless and fast!

---

## 8.3 Distributed Rate Limiting & Throttling Algorithms

To protect against denial of service, brute force attacks, and noisy neighbors, Gateways enforce rate limits.

### The 4 Core Algorithms:
1. **Token Bucket**: Tokens are added to a bucket at a constant rate $r$. Each request consumes 1 token. Allows bursts up to bucket capacity $b$.
2. **Leaky Bucket**: Requests enter a queue; requests exit at a strict, smooth constant rate. Smooths traffic bursts.
3. **Fixed Window Counter**: Divides time into 1-minute blocks. Vulnerable to edge bursts (2x limit at window boundaries).
4. **Sliding Window Counter**: Hybrid algorithm tracking sub-minute timestamps. Highly accurate and memory efficient.

### Production Redis Lua Script for Atomic Sliding Window Rate Limiting:
```lua
-- KEYS[1]: Rate limit key (e.g., "rate:user_101")
-- ARGV[1]: Current timestamp (milliseconds)
-- ARGV[2]: Window size (milliseconds, e.g., 60000 for 1 min)
-- ARGV[3]: Max requests allowed in window

local key = KEYS[1]
local now = tonumber(ARGV[1])
local window = tonumber(ARGV[2])
local limit = tonumber(ARGV[3])
local clearBefore = now - window

-- Remove timestamps older than the current sliding window
redis.call('ZREMRANGEBYSCORE', key, '-inf', clearBefore)

-- Count remaining requests in current window
local currentRequests = redis.call('ZCARD', key)

if currentRequests < limit then
    -- Add current request timestamp
    redis.call('ZADD', key, now, now)
    redis.call('PEXPIRE', key, window)
    return 1 -- ALLOWED
else
    return 0 -- REJECTED (HTTP 429 Too Many Requests)
end
```

---

## 8.4 Web Application Firewall (WAF) & OWASP Top 10 Mitigation
The API Gateway integrates with a WAF (e.g., AWS WAF, Cloudflare WAF, ModSecurity) to inspect HTTP request bodies and headers before routing:
- **SQL Injection (SQLi)**: Blocks regex patterns matching `' OR 1=1; DROP TABLE users;`.
- **Cross-Site Scripting (XSS)**: Strips or rejects `<script>` tags and malicious JavaScript payloads.
- **Path Traversal**: Blocks `../../../../etc/passwd`.
- **Server-Side Request Forgery (SSRF)**: Prevents clients from forcing the gateway to fetch internal metadata URLs (`http://169.254.169.254/latest/meta-data/`).

---

## 8.5 IP Whitelisting, Geo-Fencing, and CORS Hardening
1. **Geo-Fencing**: Blocking traffic originating from countries where the business does not operate.
2. **CORS (Cross-Origin Resource Sharing)**:
   - **Never use** `Access-Control-Allow-Origin: *` for authenticated endpoints!
   - Explicitly validate origins against a whitelist: `https://app.enterprise.com`.
   - Set `Access-Control-Allow-Credentials: true`.

---

# 📊 Phase 9: Enterprise Decision Matrix & Cheat Sheet

### Which Authentication / Authorization Technology Should You Use?

```
                                  START HERE
                                      │
                         What are you authenticating?
                                      │
            ┌─────────────────────────┴─────────────────────────┐
            ▼                                                   ▼
     HUMAN USERS                                       MACHINES / SERVICES
            │                                                   │
   Is it an Enterprise                               Is it Internal Service-to-Service
   Workforce or Consumer App?                         or External Developer API?
            │                                                   │
     ┌──────┴──────┐                                     ┌──────┴──────┐
     ▼             ▼                                     ▼             ▼
 WORKFORCE      CONSUMER                             INTERNAL      EXTERNAL
 (Employees)    (Public)                             SERVICES      DEVELOPERS
     │             │                                     │             │
Legacy AD?    Modern OIDC                           Zero-Trust    OAuth 2.0 Client
     │        (OAuth2 + PKCE)                       Service Mesh   Credentials /
 ┌───┴───┐    (Passkeys, Google,                    with mTLS     HMAC Signatures
 ▼       ▼     Apple Login)                         (SPIFFE/Envoy)(Stripe-style)
SAML 2.0 Azure AD /
(Okta)   Entra ID
```

### Comprehensive Protocol Comparison Cheat Sheet
| Protocol / Model | Layer | Primary Use Case | Payload Format | Token Lifespan | Statefulness |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **HTTP Basic** | Transport/App | Quick debugging, legacy internal scrapers. | Base64 header | Permanent | Stateless |
| **Session Cookie**| Application | Monolithic web apps, server-rendered HTML. | Opaque Cookie | Hours / Days | **Stateful** (Redis/DB) |
| **SAML 2.0** | Application | Enterprise Workforce SSO (Okta to Workday).| Large XML | Minutes (Exchange) | Stateless / Federated |
| **Kerberos** | Network/OS | Windows Domain on-premise LAN authentication.| Binary Tickets| 10 Hours | Stateful (KDC) |
| **OAuth 2.0** | Application | Delegated API authorization (Valet key). | JSON / JWT | 15 - 60 Minutes | Stateless |
| **OpenID Connect**| Application | Modern User Login & Mobile/SPA Authentication.| **JWT** (`id_token`)| 15 - 60 Minutes | Stateless |
| **mTLS** | Transport (L4)| Microservice Zero-Trust & Banking gateways. | X.509 Certs | Months / Years | Stateless (TLS handshakes) |
| **HMAC Signing**| Application | High-security financial APIs (AWS, Stripe). | SHA-256 Sig | 5 Minutes (Window) | Stateless |
| **RBAC** | Authorization| Small to medium apps with static roles. | Roles/Groups | N/A | App Logic |
| **ABAC / PBAC**| Authorization| Enterprise fine-grained compliance (OPA/Rego).| Context JSON | Evaluated on demand| External Policy Engine |
| **ReBAC** | Authorization| Google Drive / Social relationship hierarchies.| Graph Tuples | Graph Traversal | Distributed Graph DB |

---
[🏠 Back to Central Home Documentation Hub](README.md)
