# NGINX High-Performance Reverse Proxy, Load Balancing & Gateway Architecture Interview Guide

> **Scope**: NGINX Event-Driven Master-Worker Architecture, Asynchronous I/O Multiplexing (Epoll), Reverse Proxying & Upstream Keepalive, Load Balancing Algorithms, SSL/TLS 1.3 Termination & OCSP Stapling, Dynamic Rate Limiting (Token Bucket), Edge Caching (`proxy_cache`), and Mission-Critical War-Room Post-Mortems.

---

## Guide Architecture Overview

```
========================================================================================================================
                                     NGINX REVERSE PROXY & GATEWAY ARCHITECTURE
========================================================================================================================
 [Layer 1: Event-Driven Master-Worker Internals]    --> Master Process, Worker Processes, epoll Event Loop, Non-Blocking
 [Layer 2: Reverse Proxying & Upstream Architecture]--> proxy_pass, Upstream Keepalive, Load Balancing Algorithms
 [Layer 3: SSL/TLS Termination & Edge Caching]      --> TLS 1.3, Session Resumption, OCSP Stapling, proxy_cache Tuning
 [Layer 4: Traffic Shaping, Rate Limiting & Sec]    --> limit_req (Token Bucket), limit_conn, Custom Headers, WAF
 [Layer 5: Ultra-Deep Real-World War-Room Cases]    --> 10 Production Disasters (Upstream 502 Bad Gateway, DNS Cache TTL)
 [Layer 6: Beginner Mistakes & Anti-Patterns]       --> 8 Fatal Engineering Traps (Missing Keepalive, Trailing Slash Bugs)
 [Layer 7: Globally Reported Production Incidents]  --> Real Outages (NGINX Trailing Slash Path Traversal Security Breach)
 [Layer 8: Rapid-Fire Cheat Sheet & Summary Matrix] --> High-Speed Directives Reference, Load Balancing Complexity Table
========================================================================================================================
```

---

# Layer 1: Event-Driven Master-Worker Internals

---

### Scenario 1: The Master-Worker Architecture & Asynchronous Event Loop
**Interviewer Evaluation:** Assesses understanding of process separation, zero-downtime binary upgrades (`USR2`), CPU affinity, and non-blocking I/O.

#### Technical Deep Dive
Traditional web servers (Apache prefork) assign one thread or process per connection. If 10,000 clients connect, Apache spawns 10,000 processes, crashing under memory pressure and context switching.
1. **The Master Process**:
   - Runs as privileged user (root).
   - Reads and evaluates configuration files, binds to privileged network ports (80/443), and manages worker processes.
2. **Worker Processes**:
   - Run as unprivileged user (`nginx` / `www-data`).
   - Sized exactly to the number of physical CPU cores (`worker_processes auto;`) to prevent CPU context switching.
   - Each worker runs a **single-threaded, non-blocking asynchronous event loop** powered by Linux **`epoll`** (or BSD `kqueue`).
   - A single worker can easily handle 50,000 concurrent persistent connections.
3. **Zero-Downtime Reload (`nginx -s reload`)**:
   - Master checks syntax of new config.
   - Master spawns new worker processes with the new configuration.
   - Master sends `SIGQUIT` to old workers, instructing them to stop accepting new connections, finish handling active in-flight requests, and exit cleanly (**Graceful Drain**).

```
NGINX Master-Worker Architecture:
[ NGINX Master Process (root) ]
               |
    +----------+----------+
    v                     v
[ Worker 1 (Core 0) ]  [ Worker 2 (Core 1) ]
  - epoll Event Loop     - epoll Event Loop
  - 50,000 Conns         - 50,000 Conns
```

---

### Scenario 2: Upstream Connection Pooling & The Missing Keepalive Trap
**Interviewer Evaluation:** Tests diagnosing high backend latency caused by opening a brand new TCP handshake for every proxied request.

#### Technical Deep Dive
By default, `proxy_pass` uses HTTP/1.0 without keepalive connections to upstream backends:
- **The Anti-Pattern**: For every incoming client request, NGINX opens a fresh TCP connection to the backend microservice, executes a 3-way handshake, sends the request, and immediately closes the connection.
- Under 10,000 QPS, this causes extreme CPU churn on backends and exhausts ephemeral ports.
- **The Fix: Upstream Keepalive Directive**:
  1. Add `keepalive <count>;` in the `upstream` block to maintain a cache of open, reusable connections to backend servers.
  2. In the `location` block, configure `proxy_http_version 1.1;` and clear the `Connection` header (`proxy_set_header Connection "";`).

```nginx
upstream payment_backend {
    server 10.0.1.10:8080;
    server 10.0.1.11:8080;

    # Maintains up to 64 idle keepalive connections per worker process:
    keepalive 64;
    keepalive_requests 1000;
    keepalive_timeout 60s;
}

server {
    listen 443 ssl;
    server_name api.example.com;

    location / {
        proxy_pass http://payment_backend;
        proxy_http_version 1.1; # MANDATORY for keepalive!
        proxy_set_header Connection ""; # Clears "close" header!
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

---

# Layer 2: Traffic Shaping & Rate Limiting

---

### Scenario 3: Rate Limiting with `limit_req_zone`: Token Bucket Mechanics
**Interviewer Evaluation:** Assesses DDoS mitigation, token bucket algorithm implementation, burst allowances, and `nodelay` mechanics.

#### Technical Deep Dive
NGINX implements rate limiting using the **Leaky Bucket / Token Bucket** algorithm in shared memory:
- `limit_req_zone $binary_remote_addr zone=api_limit:10m rate=10r/s;`:
  - Allocates 10MB of shared memory (holds ~160,000 IP addresses).
  - Enforces a steady-state rate of 10 requests per second per IP.
- **Burst and Nodelay**:
  - `limit_req zone=api_limit burst=20 nodelay;`:
    - Allows a client to burst up to 20 requests immediately without delay.
    - If the burst capacity is exceeded, subsequent requests are immediately rejected with **HTTP 503 (or 429)**.

```nginx
http {
    # Rate limit by client IP address:
    limit_req_zone $binary_remote_addr zone=ip_limit:10m rate=5r/s;
    limit_req_status 429; # Return standard HTTP 429 instead of default 503

    server {
        listen 80;
        location /api/checkout {
            limit_req zone=ip_limit burst=10 nodelay;
            proxy_pass http://checkout_service;
        }
    }
}
```

---

# Layer 3: Ultra-Deep Real-World War-Room Incidents

---

### Scenario 4: War Room: The Upstream DNS Caching 502 Bad Gateway Outage
**Interviewer Evaluation:** Evaluates diagnosing NGINX 502 errors when backend AWS ALB or Kubernetes services change IP addresses.

#### Incident Scenario
A company deployed an AWS Application Load Balancer (ALB) as an upstream behind NGINX. Two weeks later, without any code changes, NGINX suddenly began returning **HTTP 502 Bad Gateway** for 100% of user traffic.

#### Root Cause Analysis
1. The NGINX configuration used a static domain in the upstream:
   `proxy_pass http://my-internal-alb.us-east-1.elb.amazonaws.com;`.
2. NGINX resolves domain names to IP addresses **only once at startup/reload** and caches the IP address indefinitely in memory!
3. AWS ALBs dynamically scale and cycle IP addresses. When AWS retired the old ALB IP address, NGINX continued sending traffic to the decommissioned IP, resulting in connection timeouts and 502 Bad Gateway errors.

#### Remediation & Prevention
- Force dynamic runtime DNS resolution using a variable inside `proxy_pass` combined with an explicit `resolver` directive:
  ```nginx
  location / {
      resolver 10.0.0.2 valid=10s ipv6=off; # Amazon VPC DNS resolver
      set $upstream_endpoint "http://my-internal-alb.us-east-1.elb.amazonaws.com";
      proxy_pass $upstream_endpoint; # Variable forces dynamic re-resolution every 10s!
  }
  ```

---

# Layer 4: Rapid-Fire Cheat Sheet & Interview Summary Matrix

---

### Essential NGINX Performance Directives

| Directive | Purpose | Production Best Practice |
| :--- | :--- | :--- |
| `worker_processes auto;` | Binds workers to CPU count | Prevents CPU core contention |
| `worker_connections 65535;` | Max connections per worker | Sized according to OS `ulimit -n` |
| `proxy_http_version 1.1;` | Enables HTTP/1.1 to upstream | Mandatory for backend keepalive |
| `ssl_session_cache shared:SSL:10m;` | Caches TLS handshake session | Cuts SSL handshake latency by 70% |
| `ssl_stapling on;` | Enables OCSP Stapling | Speeds up client certificate validation |

---

### The Golden NGINX Interview Rules
1. **Always enable upstream keepalive**: Eliminate TCP handshake overhead between proxy and backends.
2. **Watch the trailing slash in `proxy_pass`**: `proxy_pass http://backend/` rewrites URIs; `proxy_pass http://backend` preserves raw URIs.
3. **Use variables for dynamic cloud endpoints**: Prevent 502 outages caused by static startup DNS resolution.
4. **Tune `worker_processes` to CPU cores**: Use `worker_cpu_affinity auto;` for optimal L3 cache locality.
5. **Always return 429 for rate limiting**: Set `limit_req_status 429;` for REST API compliance.
