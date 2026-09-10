[🏠 Back to Home](README.md) | [🌐 Envoy Proxy](envoy_proxy_master_guide.md) | [🕸️ Istio Service Mesh](istio_service_mesh_master_guide.md) | [🐘 Apache & LAMP](apache_httpd_lamp_master_guide.md) | [🐱 Apache Tomcat](apache_tomcat_master_guide.md)

# 🌐 NGINX High-Concurrency Reverse Proxy, Caching & L7 Load Balancing Master Guide

### *(The Definitive SRE & Systems Architecture Manual: Asynchronous epoll Event Loops, Context Hierarchy Precedence, TLS 1.3 / HTTP/3 QUIC, Persistent Keepalive Pools, Leaky Bucket Rate Limiting, Micro-Caching & Production War Room Incidents)*

[![NGINX Core](https://img.shields.io/badge/NGINX-1.25%2B%20Mainline-009639.svg?style=for-the-badge&logo=nginx&logoColor=white)]()
[![Architecture](https://img.shields.io/badge/Architecture-Master--Worker%20%7C%20epoll-blue.svg?style=for-the-badge)]()
[![Protocols](https://img.shields.io/badge/Protocols-HTTP%2F2%20%7C%20HTTP%2F3%20QUIC-orange.svg?style=for-the-badge)]()
[![Security](https://img.shields.io/badge/Security-TLS%201.3%20%7C%20OCSP%20Stapling-red.svg?style=for-the-badge)]()

---

## 📑 Master Table of Contents

- [1. Executive Architecture: The Asynchronous Event-Driven Paradigm](#1-executive-architecture-the-asynchronous-event-driven-paradigm)
  - [1.1 The C10K Problem & How NGINX Solved It](#11-the-c10k-problem--how-nginx-solved-it)
  - [1.2 Master-Worker Process Model & CPU Affinity](#12-master-worker-process-model--cpu-affinity)
  - [1.3 Linux epoll & BSD kqueue Non-Blocking Multiplexing](#13-linux-epoll--bsd-kqueue-non-blocking-multiplexing)
  - [1.4 The Mathematics of Sizing NGINX Workers & Connections](#14-the-mathematics-of-sizing-nginx-workers--connections)
- [2. Context Hierarchy & Configuration Architecture](#2-context-hierarchy--configuration-architecture)
  - [2.1 The Core Directives: main, events, http, server, location](#21-the-core-directives-main-events-http-server-location)
  - [2.2 Location Block Matching Precedence Rules (The Golden Order)](#22-location-block-matching-precedence-rules-the-golden-order)
  - [2.3 URI Rewriting, Returns & Internal Redirects](#23-uri-rewriting-returns--internal-redirects)
- [3. High-Performance Static Asset Delivery](#3-high-performance-static-asset-delivery)
  - [3.1 Zero-Copy Static Delivery: sendfile, tcp_nopush & tcp_nodelay](#31-zero-copy-static-delivery-sendfile-tcp_nopush--tcp_nodelay)
  - [3.2 Kernel File Descriptor Caching: open_file_cache](#32-kernel-file-descriptor-caching-open_file_cache)
  - [3.3 Gzip & Brotli Dynamic / Static Compression](#33-gzip--brotli-dynamic--static-compression)
- [4. Upstream Load Balancing & Proxy Engineering](#4-upstream-load-balancing--proxy-engineering)
  - [4.1 Load Balancing Algorithms: Round-Robin, least_conn, ip_hash, hash](#41-load-balancing-algorithms-round-robin-least_conn-ip_hash-hash)
  - [4.2 The Crucial Upstream Keepalive Pool (Preventing TIME_WAIT Storms)](#42-the-crucial-upstream-keepalive-pool-preventing-time_wait-storms)
  - [4.3 Passive Health Checks: max_fails & fail_timeout](#43-passive-health-checks-max_fails--fail_timeout)
  - [4.4 Reverse Proxy Header Engineering & WebSocket Upgrading](#44-reverse-proxy-header-engineering--websocket-upgrading)
- [5. Cryptography, SSL/TLS Termination & Modern Protocols](#5-cryptography-ssltls-termination--modern-protocols)
  - [5.1 Modern TLS 1.3 & Cipher Suite Configuration](#51-modern-tls-13--cipher-suite-configuration)
  - [5.2 SSL Session Caching & Session Tickets](#52-ssl-session-caching--session-tickets)
  - [5.3 OCSP Stapling: Eliminating CA Roundtrips](#53-ocsp-stapling-eliminating-ca-roundtrips)
  - [5.4 Enabling HTTP/2 & HTTP/3 (QUIC over UDP)](#54-enabling-http2--http3-quic-over-udp)
- [6. Traffic Control, Security & High-Performance Caching](#6-traffic-control-security--high-performance-caching)
  - [6.1 Leaky Bucket Rate Limiting: limit_req & limit_conn](#61-leaky-bucket-rate-limiting-limit_req--limit_conn)
  - [6.2 Micro-Caching Architecture (Sub-Second In-Memory Offloading)](#62-micro-caching-architecture-sub-second-in-memory-offloading)
  - [6.3 DDoS Mitigation, Buffer Overflow Defenses & Security Headers](#63-ddos-mitigation-buffer-overflow-defenses--security-headers)
- [7. Complete Production Blueprint: Enterprise Edge Reverse Proxy](#7-complete-production-blueprint-enterprise-edge-reverse-proxy)
- [8. Production War Room Incidents & Post-Mortems (RCAs)](#8-production-war-room-incidents--post-mortems-rcas)
  - [Incident 1: The Upstream DNS Caching 502 Disaster on Kubernetes](#incident-1-the-upstream-dns-caching-502-disaster-on-kubernetes)
  - [Incident 2: The TIME_WAIT Port Exhaustion Outage from Missing Keepalives](#incident-2-the-time_wait-port-exhaustion-outage-from-missing-keepalives)
  - [Incident 3: PII Data Leak via Uncontrolled Micro-Cache Headers](#incident-3-pii-data-leak-via-uncontrolled-micro-cache-headers)
- [9. Senior NGINX & SRE Engineer Interview Bank (30 Questions)](#9-senior-nginx--sre-engineer-interview-bank-30-questions)

---

# 1. Executive Architecture: The Asynchronous Event-Driven Paradigm

## 1.1 The C10K Problem & How NGINX Solved It

In 1999, Dan Kegel formulated the **C10K problem**: web servers operating on the traditional process-per-connection or thread-per-connection model (like Apache HTTPD prefork) could not handle 10,000 concurrent idle clients. Each connection consumed 2–8 MB of OS stack space and caused catastrophic CPU scheduler thrashing through continuous kernel context switching.

Igor Sysoev built **NGINX** to solve this fundamentally:
- **Decoupled Connections from Threads**: Instead of 1 thread per client, NGINX uses a non-blocking, event-driven architecture.
- **State Machine Engine**: A single worker thread handles thousands of active connections by advancing them through state machines as network packets arrive.

---

## 1.2 Master-Worker Process Model & CPU Affinity

```
                       ┌────────────────────────────┐
                       │    NGINX Master Process    │
                       │    (Runs as root / PID 1)  │
                       │   - Reads configuration    │
                       │   - Binds low network ports│
                       │   - Manages worker states  │
                       └─────────────┬──────────────┘
                                     │
         ┌───────────────────────────┼───────────────────────────┐
         │ Spawns & Monitors         │ Spawns & Monitors         │ Spawns & Monitors
         ▼                           ▼                           ▼
┌─────────────────┐         ┌─────────────────┐         ┌─────────────────┐
│ Worker Process 1│         │ Worker Process 2│         │ Worker Process N│
│ (Unprivileged)  │         │ (Unprivileged)  │         │ (Unprivileged)  │
│ - epoll eventloop         │ - epoll eventloop         │ - epoll eventloop
│ - 16,384 conns  │         │ - 16,384 conns  │         │ - 16,384 conns  │
│ - Bound to CPU 0│         │ - Bound to CPU 1│         │ - Bound to CPU N│
└─────────────────┘         └─────────────────┘         └─────────────────┘
```

- **Master Process**: Operates as `root`. Validates config syntax, opens privileged ports (80, 443), and orchestrates worker lifecycles. Does not process network client bytes.
- **Worker Processes**: Run as an unprivileged user (e.g., `nginx` or `www-data`). Each worker runs a single-threaded infinite event loop.
- **`worker_cpu_affinity`**: Pins each worker process directly to a specific physical CPU core, eliminating cache invalidation and inter-core memory bus snooping.

---

## 1.3 Linux `epoll` & BSD `kqueue` Non-Blocking Multiplexing

Traditional I/O using `select()` or `poll()` scales at $O(N)$ because the kernel must scan through every file descriptor in an array to see which one has data.

Linux **`epoll`** operates at $O(1)$ complexity:
1. The NGINX worker registers 10,000 listening sockets with an epoll instance via `epoll_ctl()`.
2. When an Ethernet packet arrives at the NIC, the hardware interrupt invokes the kernel network driver.
3. The kernel adds *only* the active socket directly to an epoll ready-list.
4. `epoll_wait()` returns immediately with the exact list of descriptors that have data ready to read, with zero wasted CPU loops.

---

## 1.4 The Mathematics of Sizing NGINX Workers & Connections

Configured in `nginx.conf`:
```nginx
worker_processes auto;
events {
    worker_connections 16384;
    use epoll;
    multi_accept on;
}
```

### The Sizing Formulas:
1. **Static Web Server (Direct Client to Disk)**:
   $$\text{Max Clients} = \text{worker\_processes} \times \text{worker\_connections}$$
2. **Reverse Proxy (Client $\to$ NGINX $\to$ Upstream Backend)**:
   $$\text{Max Concurrent Clients} = \frac{\text{worker\_processes} \times \text{worker\_connections}}{2}$$
   *(Because each incoming HTTP request requires **two file descriptors**: 1 client-to-NGINX socket and 1 NGINX-to-upstream socket!)*

3. **OS File Descriptor Limit (`/etc/security/limits.conf`)**:
   $$\text{worker\_rlimit\_nofile} \ge \text{worker\_connections} \times 2$$

---

# 2. Context Hierarchy & Configuration Architecture

## 2.1 The Core Directives: `main`, `events`, `http`, `server`, `location`

NGINX configuration is structured as a tree of nested contexts:
- **`main`**: Global process settings (`worker_processes`, `pid`, `user`).
- **`events`**: Network connection mechanics (`worker_connections`, `use epoll`).
- **`http`**: HTTP engine settings, MIME types, upstream clusters, caches, rate limits.
- **`server`**: VirtualHost definition (binds IP/Port, `server_name`, SSL certificates).
- **`location`**: URI routing blocks within a virtual server.

---

## 2.2 Location Block Matching Precedence Rules (The Golden Order)

When an incoming request arrives, NGINX evaluates location blocks in a **strict 5-step order**, regardless of how they appear in the file:

```
[ Incoming Request URI ]
          │
          ▼
1. Exact Match: location = /path ───────► (Matches? STOP immediately! Highest priority)
          │ No
          ▼
2. Preferential Prefix: location ^~ /path ──► (Matches longest prefix? STOP! Skip regex)
          │ No
          ▼
3. Regular Expression: location ~ /path ───► (Evaluated in TOP-TO-BOTTOM order in file.
   (Case-sensitive ~ or insensitive ~*)      First matching regex WINS and STOPS!)
          │ No
          ▼
4. Generic Prefix: location /path ────────► (Longest matching prefix wins)
          │ No
          ▼
5. Fallback: location / ───────────────────► (Default catch-all)
```

### Examples & Edge Cases:
```nginx
location = /login {
    # 1. Matches ONLY exact URI "/login". Will NOT match "/login?id=1" or "/login/user"
}

location ^~ /images/ {
    # 2. Matches "/images/logo.png". Skips all regex blocks below!
}

location ~* \.(gif|jpg|jpeg|png)$ {
    # 3. Matches any case-insensitive image regex across the rest of the site
}

location /api/v1/ {
    # 4. Standard prefix match. Overridden if an image regex matches!
}
```

---

## 2.3 URI Rewriting, Returns & Internal Redirects

- **`return 301 https://$host$request_uri;`**: Fast, lightweight HTTP redirect. Stops processing immediately.
- **`rewrite ^/old-path/(.*)$ /new-path/$1 permanent;`**: Modifies the internal URI buffer.
- **`try_files $uri $uri/ /index.php?$args;`**: Tests if a physical file exists on disk; falls back down the chain.

---

# 3. High-Performance Static Asset Delivery

## 3.1 Zero-Copy Static Delivery: `sendfile`, `tcp_nopush` & `tcp_nodelay`

```nginx
http {
    # Zero-Copy: Streams bytes from kernel disk page cache directly to network socket buffer
    sendfile on;

    # Collects outgoing HTTP headers and packet payload to transmit as a single MTU frame
    tcp_nopush on;

    # Disables Nagle's algorithm for interactive low latency (sends packets immediately)
    tcp_nodelay on;
}
```

```
TRADITIONAL READ/WRITE:
[ Disk ] ──► [ Kernel Buffer ] ──► [ User-Space NGINX ] ──► [ Socket Buffer ] ──► [ NIC ]
(4 Context switches, 2 Memory copies across bus)

SENDFILE ZERO-COPY:
[ Disk ] ──► [ Kernel Page Cache ] ───────────────────────► [ Socket Buffer ] ──► [ NIC ]
(2 Context switches, 0 User-space copies)
```

---

## 3.2 Kernel File Descriptor Caching: `open_file_cache`

Eliminates expensive `stat()` and `open()` system calls on frequently requested static assets:
```nginx
open_file_cache max=50000 inactive=60s;
open_file_cache_valid 120s;
open_file_cache_min_uses 2;
open_file_cache_errors on;
```
- Caches file descriptors, file size, modification timestamps, and directory presence.

---

# 4. Upstream Load Balancing & Proxy Engineering

## 4.1 Load Balancing Algorithms

```nginx
upstream api_cluster {
    # Algorithms:
    # 1. round-robin (default)
    # 2. least_conn (routes to backend with fewest active requests)
    # 3. ip_hash (sticky sessions by client IPv4/IPv6 prefix)
    # 4. hash $request_uri consistent (Ketama consistent hashing for cache backends)
    least_conn;

    server backend-01.internal:8080 weight=5 max_fails=3 fail_timeout=10s;
    server backend-02.internal:8080 weight=3 max_fails=3 fail_timeout=10s;
    server backend-backup.internal:8080 backup;
}
```

---

## 4.2 The Crucial Upstream Keepalive Pool (Preventing TIME_WAIT Storms)

By default, NGINX opens a brand-new TCP socket for *every single incoming request* to upstream servers and immediately closes it. Under 10,000 RPS, the server exhausts the Linux ephemeral port range ($32,768 - 60,999$), leaving thousands of sockets stuck in the kernel **`TIME_WAIT`** state!

### The Production Remedy:
```nginx
upstream api_cluster {
    server 10.0.1.10:8080;
    server 10.0.1.11:8080;

    # Keep up to 128 idle keepalive TCP connections open per NGINX worker process!
    keepalive 128;
    keepalive_requests 10000;
    keepalive_timeout 60s;
}

server {
    location / {
        proxy_pass http://api_cluster;

        # MANDATORY: Enable HTTP/1.1 and clear Connection header to maintain persistent sockets!
        proxy_http_version 1.1;
        proxy_set_header Connection "";
    }
}
```

---

## 4.3 Reverse Proxy Header Engineering & WebSocket Upgrading

```nginx
# Map directive for dynamic WebSocket upgrade
map $http_upgrade $connection_upgrade {
    default upgrade;
    ''      close;
}

server {
    location /ws/ {
        proxy_pass http://api_cluster;
        proxy_http_version 1.1;

        # Forward real client identifiers
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;

        # WebSocket support
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection $connection_upgrade;
        proxy_read_timeout 3600s;
    }
}
```

---

# 5. Cryptography, SSL/TLS Termination & Modern Protocols

## 5.1 Modern TLS 1.3 & Cipher Suite Configuration

```nginx
server {
    listen 443 ssl http2;
    listen [::]:443 ssl http2;
    server_name api.company.com;

    ssl_certificate /etc/ssl/certs/fullchain.pem;
    ssl_certificate_key /etc/ssl/private/privkey.pem;

    # Only secure protocols
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers ECDHE-ECDSA-AES128-GCM-SHA256:ECDHE-RSA-AES128-GCM-SHA256:ECDHE-ECDSA-AES256-GCM-SHA384:ECDHE-RSA-AES256-GCM-SHA384:ECDHE-ECDSA-CHACHA20-POLY1305:ECDHE-RSA-CHACHA20-POLY1305;
    ssl_prefer_server_ciphers off;

    # SSL Session Cache: Caches negotiated handshakes in shared RAM across all workers
    ssl_session_cache shared:SSL:50m;
    ssl_session_timeout 1d;
    ssl_session_tickets off; # Prevents forward-secrecy invalidation

    # OCSP Stapling
    ssl_stapling on;
    ssl_stapling_verify on;
    ssl_trusted_certificate /etc/ssl/certs/ca_chain.pem;
    resolver 1.1.1.1 8.8.8.8 valid=300s;
    resolver_timeout 5s;

    # HSTS
    add_header Strict-Transport-Security "max-age=63072000; includeSubDomains; preload" always;
}
```

---

# 6. Traffic Control, Security & High-Performance Caching

## 6.1 Leaky Bucket Rate Limiting: `limit_req` & `limit_conn`

```nginx
http {
    # Shared memory zone 'api_ratelimit' (20MB holds ~320,000 client IP states)
    limit_req_zone $binary_remote_addr zone=api_ratelimit:20m rate=20r/s;
    limit_conn_zone $binary_remote_addr zone=conn_ratelimit:20m;

    server {
        location /api/v1/auth/ {
            # Leaky bucket: 20 r/s sustained, allows immediate bursts up to 40 without delays
            limit_req zone=api_ratelimit burst=40 nodelay;
            limit_conn conn_ratelimit 10;
            limit_req_status 429;

            proxy_pass http://api_cluster;
        }
    }
}
```

---

## 6.2 Micro-Caching Architecture (Sub-Second In-Memory Offloading)

By caching dynamic API responses for just **1 to 3 seconds**, 98% of backend database read pressure is eliminated during traffic spikes:

```nginx
http {
    proxy_cache_path /var/cache/nginx/api levels=1:2 keys_zone=micro_cache:50m max_size=2g inactive=10m use_temp_path=off;

    server {
        location /api/v1/catalog {
            proxy_pass http://api_cluster;

            proxy_cache micro_cache;
            proxy_cache_valid 200 302 2s;    # Cache successful catalog responses for 2 seconds
            proxy_cache_valid 404 1m;        # Negative caching for 1 minute
            proxy_cache_use_stale error timeout updating http_500 http_502 http_503;

            # Bypass cache if authentication headers or explicit session cookies exist
            proxy_cache_bypass $http_authorization $cookie_session_id;
            proxy_no_cache $http_authorization $cookie_session_id;

            add_header X-Cache-Status $upstream_cache_status;
        }
    }
}
```

---

# 7. Complete Production Blueprint: Enterprise Edge Reverse Proxy

```nginx
# ==============================================================================
# Hardened Enterprise NGINX Edge Reverse Proxy Configuration
# ==============================================================================
user nginx;
worker_processes auto;
worker_rlimit_nofile 65535;
pid /var/run/nginx.pid;

events {
    worker_connections 16384;
    use epoll;
    multi_accept on;
}

http {
    include /etc/nginx/mime.types;
    default_type application/octet-stream;

    # Performance
    sendfile on;
    tcp_nopush on;
    tcp_nodelay on;
    keepalive_timeout 65;
    server_tokens off;

    # Rate Limiting
    limit_req_zone $binary_remote_addr zone=global_limit:20m rate=100r/s;
    limit_conn_zone $binary_remote_addr zone=conn_limit:20m;

    # Micro-Cache Path
    proxy_cache_path /var/cache/nginx/edge levels=1:2 keys_zone=edge_cache:50m max_size=5g inactive=15m use_temp_path=off;

    # Upstream with Persistent Keepalive
    upstream backend_nodes {
        least_conn;
        server 10.0.10.21:8080 max_fails=3 fail_timeout=10s;
        server 10.0.10.22:8080 max_fails=3 fail_timeout=10s;
        keepalive 128;
    }

    # HTTP Redirect to HTTPS
    server {
        listen 80;
        listen [::]:80;
        server_name api.enterprise.com;
        return 301 https://$host$request_uri;
    }

    # Production HTTPS Server
    server {
        listen 443 ssl http2;
        listen [::]:443 ssl http2;
        server_name api.enterprise.com;

        # TLS Hardening
        ssl_certificate /etc/ssl/certs/api_enterprise.crt;
        ssl_certificate_key /etc/ssl/private/api_enterprise.key;
        ssl_protocols TLSv1.2 TLSv1.3;
        ssl_ciphers HIGH:!aNULL:!MD5;
        ssl_session_cache shared:SSL:50m;
        ssl_session_timeout 1d;

        # Security Headers
        add_header X-Frame-Options "DENY" always;
        add_header X-Content-Type-Options "nosniff" always;
        add_header X-XSS-Protection "1; mode=block" always;
        add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;

        location / {
            limit_req zone=global_limit burst=150 nodelay;
            limit_conn conn_limit 30;

            proxy_pass http://backend_nodes;
            proxy_http_version 1.1;
            proxy_set_header Connection "";

            # Forward Headers
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;

            # Caching
            proxy_cache edge_cache;
            proxy_cache_valid 200 3s;
            proxy_cache_use_stale error timeout updating http_500 http_502 http_503;
            add_header X-Cache-Status $upstream_cache_status;
        }
    }
}
```

---

# 8. Production War Room Incidents & Post-Mortems (RCAs)

### Incident 1: The Upstream DNS Caching 502 Disaster on Kubernetes
- **Symptom**: During a routine Kubernetes rolling restart, NGINX returned `502 Bad Gateway` on 100% of API requests.
- **Root Cause**: By default, standard NGINX resolves upstream domain names *only once* at startup. When Kubernetes pods rescheduled to new IP addresses, NGINX continued sending packets to dead IPs.
- **Remediation**: Use dynamic DNS variables with a designated resolver:
  ```nginx
  resolver 10.96.0.10 valid=10s;
  set $upstream_target "http://service.default.svc.cluster.local:8080";
  proxy_pass $upstream_target;
  ```

---

### Incident 2: The `TIME_WAIT` Port Exhaustion Outage from Missing Keepalives
- **Symptom**: Under peak traffic, NGINX failed with `connect() failed (99: Cannot assign requested address)`.
- **Root Cause**: Missing `proxy_http_version 1.1;` and `proxy_set_header Connection "";` in location blocks. Every upstream request opened and closed an ephemeral TCP socket, exhausting all 28,000 local ports in `TIME_WAIT`.
- **Remediation**: Configure `keepalive 128;` in the upstream block and enforce persistent HTTP/1.1 connections.

---

### Incident 3: PII Data Leak via Uncontrolled Micro-Cache Headers
- **Symptom**: User A refreshed their account dashboard and saw User B's credit card and profile details!
- **Root Cause**: Developers enabled `proxy_cache` on `/api/v1/user/profile` without adding `proxy_cache_bypass $http_authorization;`. The first user's authenticated response was cached and served to all subsequent users.
- **Remediation**: Enforce `proxy_cache_bypass $http_authorization $cookie_session_id;` and configure backend servers to emit `Cache-Control: private, no-store`.

---

# 9. Senior NGINX & SRE Engineer Interview Bank (30 Questions)

#### Q1: How does NGINX achieve high concurrency without thread-per-connection?
> **Answer**: NGINX uses a single-threaded, non-blocking event-driven loop in each worker process, relying on kernel multiplexers (`epoll` on Linux, `kqueue` on BSD). A worker registers thousands of open sockets with the kernel and only processes descriptors that have ready I/O events, eliminating context-switching and stack allocation overhead.

#### Q2: What is the exact evaluation order of `location` blocks?
> **Answer**: 
> 1. Exact match (`=`)
> 2. Preferential prefix match (`^~`)
> 3. Regular expressions in order of appearance (`~` case-sensitive, `~*` case-insensitive)
> 4. Longest generic prefix match.

#### Q3: Why is `proxy_set_header Connection "";` necessary for upstream keepalive?
> **Answer**: HTTP/1.1 persistent connections require clearing the `Connection: close` header that NGINX sets by default when proxying requests. Setting it to empty string allows persistent HTTP/1.1 connections to remain open in the keepalive pool.

#### Q4: What does `sendfile on;` do at the Linux kernel level?
> **Answer**: It performs zero-copy data transfer using the `sendfile()` system call, streaming data directly from the OS page cache to the network socket buffer, completely bypassing user-space memory copies and context switches.

#### Q5: What is the difference between `proxy_cache_bypass` and `proxy_no_cache`?
> **Answer**:
> - `proxy_cache_bypass`: Decides whether to fetch the response from the cache or query the upstream backend.
> - `proxy_no_cache`: Decides whether the response received from the upstream backend should be saved into the cache.

#### Q6: What is OCSP Stapling and how does `ssl_stapling on;` improve TLS handshake latency?
> **Answer**: In standard TLS handshakes, the client browser must pause and query the Certificate Authority's OCSP responder to verify that the server's TLS certificate hasn't been revoked, adding 100–500ms of latency and risking CA downtime. With **OCSP Stapling**, the NGINX server periodically contacts the CA in the background, caches the cryptographically signed time-stamped OCSP revocation status, and "staples" this proof directly into the TLS Certificate Status request during the initial TLS handshake. This eliminates client CA roundtrips and protects user privacy.

#### Q7: How do NGINX master process signals differ (`SIGHUP`, `SIGUSR1`, `SIGUSR2`, and `SIGQUIT`)?
> **Answer**:
> - `SIGHUP` (or `nginx -s reload`): Re-reads configuration files, starts new worker processes with the new config, and gracefully shuts down old workers once they finish in-flight requests.
> - `SIGQUIT`: Gracefully shuts down workers after completing active connections.
> - `SIGUSR1`: Re-opens log files on disk (used during logrotate to prevent writing to deleted file handles).
> - `SIGUSR2`: Executes an on-the-fly binary upgrade (spawns a brand-new NGINX master binary without dropping any client sockets).

#### Q8: How does the leaky-bucket rate limiting algorithm in `limit_req_zone` handle bursts without dropping traffic?
> **Answer**: `limit_req_zone` sets a fixed processing rate (e.g. `rate=10r/s`, allowing 1 request every 100ms). When a traffic burst arrives, the `burst=20` parameter creates a queue holding up to 20 excess requests. With `nodelay`, burst requests are processed immediately as long as the burst bucket isn't full, but subsequent requests from that client must wait until the bucket leaks. If the burst bucket overflows, incoming requests are immediately rejected with HTTP 429 or 503.

#### Q9: What causes upstream buffer spilling to disk (`an upstream response is buffered to a temporary file`) and how do you resolve it?
> **Answer**: When a backend upstream server (e.g. Spring Boot or Node.js) generates a large response faster than the slow client can download it over the Internet, NGINX buffers the response in memory (`proxy_buffers`). If the payload exceeds the allocated RAM buffer size (`proxy_buffer_size + proxy_buffers`), NGINX writes the excess payload to a temporary file on disk (`/var/cache/nginx/proxy_temp`), causing high disk I/O latency.
> **Remediation**:
> 1. Increase memory buffers for large API payloads: `proxy_buffers 16 32k; proxy_buffer_size 64k;`.
> 2. For heavy file streaming endpoints (videos, large CSVs), disable buffering entirely: `proxy_buffering off;`.

#### Q10: How do you configure NGINX for zero-downtime canary traffic splitting?
> **Answer**: Use the `split_clients` module based on a consistent request attribute (e.g. client IP or session cookie):
> ```nginx
> split_clients "${remote_addr}${http_user_agent}" $upstream_variant {
>     10%     canary_backend;
>     *       production_backend;
> }
> 
> upstream production_backend {
>     server 10.0.1.10:8080 max_fails=3 fail_timeout=10s;
>     keepalive 32;
> }
> upstream canary_backend {
>     server 10.0.2.20:8080 max_fails=3 fail_timeout=10s;
>     keepalive 32;
> }
> 
> server {
>     location /api/ {
>         proxy_pass http://$upstream_variant;
>         proxy_set_header Connection "";
>         proxy_http_version 1.1;
>     }
> }
> ```
> This deterministically routes 10% of users to the canary backend while ensuring that an individual user's subsequent requests stay pinned to the same version.

---

## ⚖️ NGINX Edge Proxy Production Hardening Cheat Sheet

| Parameter / Directive | Recommended Setting | Production Impact |
| :--- | :--- | :--- |
| **`worker_processes`** | `auto` | Binds one worker process per CPU core, maximizing cache locality |
| **`worker_connections`** | `10240` (with `ulimit -n 65535`) | Prevents `worker_connections are not enough` 500 errors during traffic spikes |
| **`sendfile` & `tcp_nopush`** | `sendfile on; tcp_nopush on;` | Enables zero-copy kernel streaming and packs packets to MTU size |
| **`tcp_nodelay`** | `on` | Disables Nagle's algorithm for interactive, low-latency API responses |
| **`server_tokens`** | `off` | Hides NGINX version string in HTTP headers and default error pages |
| **`ssl_protocols`** | `TLSv1.2 TLSv1.3` | Eliminates obsolete, vulnerable SSLv3, TLS 1.0, and TLS 1.1 ciphers |
| **`ssl_stapling`** | `ssl_stapling on; ssl_stapling_verify on;` | Eliminates 100–500ms client OCSP lookup pauses during TLS handshakes |
| **Upstream Keepalive** | `proxy_http_version 1.1; proxy_set_header Connection "";` | Reuses persistent TCP connections to backends; prevents socket starvation |

---
[🏠 Back to Home](README.md) | [🌐 Envoy Proxy Guide](envoy_proxy_master_guide.md) | [🕸️ Istio Service Mesh Guide](istio_service_mesh_master_guide.md)

