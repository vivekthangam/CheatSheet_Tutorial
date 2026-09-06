[🏠 Back to Home](README.md) | [🌐 NGINX Master Guide](nginx_master_guide.md) | [🐱 Apache Tomcat](apache_tomcat_master_guide.md) | [🌐 Envoy Proxy](envoy_proxy_master_guide.md) | [🕸️ Istio Service Mesh](istio_service_mesh_master_guide.md)

# 🐘 Apache HTTP Server (httpd) & LAMP Stack Enterprise Master Guide

### *(The Definitive Web Hosting & Systems Manual: Multi-Processing Modules (Prefork, Worker, Event), mod_rewrite Engine, VirtualHost Architecture, Unix Domain Socket FastCGI IPC with PHP-FPM & SRE War Room Incidents)*

[![Apache HTTPD](https://img.shields.io/badge/Apache-HTTPD%202.4%2B-D22128.svg?style=for-the-badge&logo=apache&logoColor=white)]()
[![Engine](https://img.shields.io/badge/Engine-mpm__event%20%7C%20FastCGI-blue.svg?style=for-the-badge)]()
[![PHP-FPM](https://img.shields.io/badge/PHP--FPM-8.2%20%7C%208.3-777BB4.svg?style=for-the-badge&logo=php&logoColor=white)]()
[![Security](https://img.shields.io/badge/Security-SSL%20%7C%20mod__rewrite-red.svg?style=for-the-badge)]()

---

## 📑 Master Table of Contents

- [1. Executive Architecture: Evolution of Apache HTTPD](#1-executive-architecture-evolution-of-apache-httpd)
  - [1.1 The Web's Founding Server: Strengths & Modern Misconceptions](#11-the-webs-founding-server-strengths--modern-misconceptions)
  - [1.2 Multi-Processing Modules (MPM): Prefork vs. Worker vs. Event](#12-multi-processing-modules-mpm-prefork-vs-worker-vs-event)
  - [1.3 The Apache Request Processing Pipeline & Hook Architecture](#13-the-apache-request-processing-pipeline--hook-architecture)
- [2. Multi-Processing Module (MPM) Sizing & Tuning](#2-multi-processing-module-mpm-sizing--tuning)
  - [2.1 Sizing mpm_event for 10,000+ Concurrent Connections](#21-sizing-mpm_event-for-10000-concurrent-connections)
  - [2.2 Thread and Process Mathematics (MaxRequestWorkers, ThreadsPerChild)](#22-thread-and-process-mathematics-maxrequestworkers-threadsperchild)
  - [2.3 When to Never Use mpm_prefork in Modern Infrastructure](#23-when-to-never-use-mpm_prefork-in-modern-infrastructure)
- [3. Core Server Configuration & VirtualHost Engineering](#3-core-server-configuration--virtualhost-engineering)
  - [3.1 Directory Security, Symlinks & FilesMatch Hardening](#31-directory-security-symlinks--filesmatch-hardening)
  - [3.2 The .htaccess Performance Catastrophe (Why AllowOverride None is Mandatory)](#32-the-htaccess-performance-catastrophe-why-allowoverride-none-is-mandatory)
  - [3.3 Modern Name-Based VirtualHosts with TLS 1.3 & HTTP/2](#33-modern-name-based-virtualhosts-with-tls-13--http2)
- [4. The URL Manipulation Engine: mod_rewrite Deep Dive](#4-the-url-manipulation-engine-mod_rewrite-deep-dive)
  - [4.1 RewriteEngine Mechanics, RewriteCond & RewriteRule Syntax](#41-rewriteengine-mechanics-rewritecond--rewriterule-syntax)
  - [4.2 Flag Reference & Semantics: [L], [R=301], [NC], [QSA], [F], [E]](#42-flag-reference--semantics-l-r301-nc-qsa-f-e)
  - [4.3 Production Front Controller Rewriting (Laravel, Symfony, WordPress)](#43-production-front-controller-rewriting-laravel-symfony-wordpress)
  - [4.4 Canonical HTTPS & Subdomain Normalization Patterns](#44-canonical-https--subdomain-normalization-patterns)
- [5. The High-Performance LAMP Architecture with PHP-FPM](#5-the-high-performance-lamp-architecture-with-php-fpm)
  - [5.1 Why mod_php is Dead: FastCGI & Unix Domain Socket IPC](#51-why-mod_php-is-dead-fastcgi--unix-domain-socket-ipc)
  - [5.2 Configuring mod_proxy_fcgi with Event MPM](#52-configuring-mod_proxy_fcgi-with-event-mpm)
  - [5.3 PHP-FPM Process Manager Tuning: dynamic vs. ondemand vs. static](#53-php-fpm-process-manager-tuning-dynamic-vs-ondemand-vs-static)
  - [5.4 The Mathematical Formula for max_children Sizing](#54-the-mathematical-formula-for-max_children-sizing)
  - [5.5 PHP-FPM Worker Recycling, OOM Defenses & Slow Log Tracing](#55-php-fpm-worker-recycling-oom-defenses--slow-log-tracing)
- [6. Complete Production Blueprint: Enterprise High-Throughput LAMP Stack](#6-complete-production-blueprint-enterprise-high-throughput-lamp-stack)
- [7. Production War Room Incidents & Post-Mortems (RCAs)](#7-production-war-room-incidents--post-mortems-rcas)
  - [Incident 1: The Apache Prefork Fork-Bomb RAM Exhaustion Crash](#incident-1-the-apache-prefork-fork-bomb-ram-exhaustion-crash)
  - [Incident 2: The .htaccess I/O Thrashing Bottleneck](#incident-2-the-htaccess-io-thrashing-bottleneck)
  - [Incident 3: PHP-FPM Socket Listen Backlog Drop 502 Outage](#incident-3-php-fpm-socket-listen-backlog-drop-502-outage)
- [8. Senior Apache & LAMP Systems Engineer Interview Bank (30 Questions)](#8-senior-apache--lamp-systems-engineer-interview-bank-30-questions)

---

# 1. Executive Architecture: Evolution of Apache HTTPD

## 1.1 The Web's Founding Server: Strengths & Modern Misconceptions

The Apache HTTP Server (commonly called **httpd**) is one of the foundational software pillars of the Internet. While early benchmarks often compared outdated Apache configurations unfavorably to NGINX, modern Apache 2.4+ running the **`event` Multi-Processing Module (MPM)** with **`PHP-FPM`** matches NGINX's raw asynchronous concurrency while retaining Apache's modular flexibility and rich ecosystem of modules (`mod_security`, `mod_rewrite`, `mod_proxy`).

---

## 1.2 Multi-Processing Modules (MPM): Prefork vs. Worker vs. Event

The concurrency model of Apache is entirely pluggable through its Multi-Processing Modules (MPMs):

```
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                        APACHE MPM ARCHITECTURES COMPARED                               │
├───────────────┬────────────────────────────┬───────────────────────────────────────────┤
│ MPM Module    │ Concurrency Architecture   │ Production Tradeoff                       │
├───────────────┼────────────────────────────┼───────────────────────────────────────────┤
│ mpm_prefork   │ Process-per-connection     │ High RAM usage (~50-80 MB per process).   │
│               │ (No threading, fully safe) │ Only needed for legacy non-threadsafe libs│
├───────────────┼────────────────────────────┼───────────────────────────────────────────┤
│ mpm_worker    │ Multi-Process, Multi-Thread│ Lower RAM. Each child spawns N threads.   │
│               │ (Fixed thread pool)        │ Blocked threads hold memory on keepalive. │
├───────────────┼────────────────────────────┼───────────────────────────────────────────┤
│ mpm_event     │ Multi-Process, Multi-Thread│ Modern Standard. Dedicated listener       │
│ (Recommended) │ with epoll Event Loop      │ threads manage idle Keep-Alive sockets.   │
└───────────────┴────────────────────────────┴───────────────────────────────────────────┘
```

---

## 1.3 The Apache Request Processing Pipeline

Every request passing through Apache goes through a sequence of phase hooks:
1. **URI-to-Filename Translation**: Resolves virtual paths to local disk paths (`Alias`, `DocumentRoot`).
2. **Access Control & Authentication**: Checks client IP (`Require ip`) and credentials (`mod_authz`).
3. **MIME Type Identification**: Maps extensions to Content-Types (`mod_mime`).
4. **Fixups & Header Injection**: Modifies outgoing/incoming headers (`mod_headers`).
5. **Content Generation**: Serves static file directly or proxies to backend via `mod_proxy_fcgi`.
6. **Logging**: Writes structured access logs (`mod_log_config`).

---

# 2. Multi-Processing Module (MPM) Sizing & Tuning

## 2.1 Sizing `mpm_event` for 10,000+ Concurrent Connections

In `mpm_event`, worker threads handle active request processing. When a client finishes a request but keeps the TCP connection open via **HTTP Keep-Alive**, the connection is handed off to a lightweight **epoll listener thread**, freeing the worker thread to process other active clients immediately!

### Production `mpm_event` Configuration (`/etc/apache2/mods-available/mpm_event.conf`):
```apache
<IfModule mpm_event_module>
    StartServers             4
    MinSpareThreads         75
    MaxSpareThreads        250
    ThreadsPerChild         64
    MaxRequestWorkers     2048
    MaxConnectionsPerChild 10000
</IfModule>
```

---

## 2.2 Thread and Process Mathematics

$$\text{Max Child Processes} = \frac{\text{MaxRequestWorkers}}{\text{ThreadsPerChild}} = \frac{2048}{64} = 32 \text{ processes}$$

- **`MaxRequestWorkers`**: The absolute maximum number of simultaneous requests served concurrently.
- **`ThreadsPerChild`**: Fixed number of worker threads created by each child process.
- **`MaxConnectionsPerChild`**: Recycles child processes after serving 10,000 requests to eliminate memory fragmentation and kernel leaks.

---

# 3. Core Server Configuration & VirtualHost Engineering

## 3.1 Directory Security, Symlinks & FilesMatch Hardening

```apache
<Directory />
    AllowOverride None
    Require all denied
</Directory>

<Directory /var/www/html/public>
    # Disables directory directory listing (-Indexes)
    Options -Indexes +FollowSymLinks
    AllowOverride None
    Require all granted
</Directory>

# Block access to hidden dot-files (.git, .env, .htaccess)
<FilesMatch "^\.">
    Require all denied
</FilesMatch>

# Block access to source code and database dumps
<FilesMatch "(composer\.lock|package\.json|\.sql|\.sh|\.bak)$">
    Require all denied
</FilesMatch>
```

---

## 3.2 The `.htaccess` Performance Catastrophe

> [!CRITICAL]
> **Why `AllowOverride All` Destroys Throughput**
> If `AllowOverride All` is set, Apache executes a disk system call searching for `.htaccess` in **every directory along the path**.
> If your document root is `/var/www/html/portal/public` and a client requests `/images/icons/user.png`:
> 1. Checks `/.htaccess`
> 2. Checks `/var/.htaccess`
> 3. Checks `/var/www/.htaccess`
> 4. Checks `/var/www/html/.htaccess`
> 5. Checks `/var/www/html/portal/.htaccess`
> 6. Checks `/var/www/html/portal/public/.htaccess`
> 7. Checks `/var/www/html/portal/public/images/.htaccess`
> 8. Checks `/var/www/html/portal/public/images/icons/.htaccess`
>
> That is **8 disk lookups per file request**! Always set `AllowOverride None` in production and place all rules in VirtualHost configs.

---

## 3.3 Modern Name-Based VirtualHosts with TLS 1.3 & HTTP/2

```apache
# HTTP to HTTPS Global Redirect
<VirtualHost *:80>
    ServerName portal.enterprise.com
    Redirect permanent / https://portal.enterprise.com/
</VirtualHost>

# Production HTTPS VirtualHost
<VirtualHost *:443>
    ServerName portal.enterprise.com
    ServerAdmin ops@enterprise.com
    DocumentRoot /var/www/portal/public

    # Enable HTTP/2 Protocol
    Protocols h2 http/1.1

    # SSL/TLS Hardening
    SSLEngine on
    SSLCertificateFile /etc/ssl/certs/portal.crt
    SSLCertificateKeyFile /etc/ssl/private/portal.key
    SSLCertificateChainFile /etc/ssl/certs/ca_chain.crt
    SSLProtocol -all +TLSv1.2 +TLSv1.3
    SSLCipherSuite HIGH:!aNULL:!MD5:!3DES

    # Security Headers
    Header always set X-Frame-Options "SAMEORIGIN"
    Header always set X-Content-Type-Options "nosniff"
    Header always set Strict-Transport-Security "max-age=63072000; includeSubDomains"
</VirtualHost>
```

---

# 4. The URL Manipulation Engine: `mod_rewrite` Deep Dive

## 4.1 `RewriteEngine` Mechanics & Syntax

```apache
<IfModule mod_rewrite.c>
    RewriteEngine On
    RewriteBase /

    # RewriteCond TestString Pattern [Flags]
    # RewriteRule Pattern Substitution [Flags]
</IfModule>
```

---

## 4.2 Flag Reference & Semantics

| Flag | Meaning | Production Behavior |
| :--- | :--- | :--- |
| **`[L]`** | Last Rule | Stops evaluating further rewrite rules if this rule matches. |
| **`[R=301]`**| Permanent Redirect | Emits an HTTP 301 redirect to the browser, updating browser address bar. |
| **`[NC]`** | No Case | Case-insensitive regular expression comparison. |
| **`[QSA]`** | Query String Append | Preserves incoming URL query parameters (`?page=2`) and appends them to target. |
| **`[F]`** | Forbidden | Immediately halts request processing and returns HTTP 403 Forbidden. |
| **`[E=VAR:VAL]`**| Environment Variable | Sets an Apache internal environment variable for use in logging or PHP. |

---

## 4.3 Production Front Controller Rewriting

Used by modern web frameworks (Laravel, Symfony, WordPress):
```apache
<Directory /var/www/portal/public>
    Options -Indexes +FollowSymLinks
    AllowOverride None
    Require all granted

    RewriteEngine On
    # If the requested physical file does not exist...
    RewriteCond %{REQUEST_FILENAME} !-f
    # ...and if the requested physical directory does not exist...
    RewriteCond %{REQUEST_FILENAME} !-d
    # ...route everything to index.php, preserving query parameters
    RewriteRule ^ index.php [QSA,L]
</Directory>
```

---

# 5. The High-Performance LAMP Architecture with PHP-FPM

## 5.1 Why `mod_php` is Dead

In legacy systems, `mod_php` embedded the PHP interpreter directly inside every Apache child process. This had two fatal flaws:
1. It required `mpm_prefork`, consuming 50–100 MB RAM per connection.
2. Every request for static assets (images, CSS, JS) spawned a bloated PHP interpreter, even though PHP code was never executed!

**Modern LAMP Solution**:
Apache runs `mpm_event` as a lean, asynchronous static file server and reverse proxy, communicating with **PHP-FPM** over high-speed Linux **Unix Domain Sockets**.

---

## 5.2 Configuring `mod_proxy_fcgi` with Event MPM

```apache
<VirtualHost *:443>
    DocumentRoot /var/www/app/public

    # FastCGI Unix Domain Socket Pipeline
    <FilesMatch \.php$>
        SetHandler "proxy:unix:/run/php/php8.2-fpm.sock|fcgi://localhost/"
    </FilesMatch>
</VirtualHost>
```

---

## 5.3 PHP-FPM Process Manager Tuning

Configured in `/etc/php/8.2/fpm/pool.d/www.conf`:

### Sizing Formula for `pm.max_children`:
1. Calculate available RAM dedicated to PHP (e.g., 8 GB server with 4 GB dedicated to PHP = 4,096 MB).
2. Measure average memory footprint of your PHP application process under peak load (e.g., 50 MB):
   $$\text{pm.max\_children} = \frac{\text{Dedicated RAM (MB)}}{\text{Avg Process Size (MB)}} = \frac{4096}{50} \approx 80$$

```ini
[www]
user = www-data
group = www-data

listen = /run/php/php8.2-fpm.sock
listen.owner = www-data
listen.group = www-data
listen.mode = 0660

; Process Manager Mode: dynamic or static
pm = dynamic
pm.max_children = 80
pm.start_servers = 20
pm.min_spare_servers = 10
pm.max_spare_servers = 30

; Prevent memory leaks: Recycle PHP worker processes after 5,000 requests
pm.max_requests = 5000

; Slow query log: Track any script taking longer than 5 seconds
slowlog = /var/log/php-fpm/www-slow.log
request_slowlog_timeout = 5s
```

---

# 6. Complete Production Blueprint: Enterprise High-Throughput LAMP Stack

```apache
# /etc/apache2/sites-available/enterprise-lamp.conf
# ==============================================================================
# Production High-Performance LAMP VirtualHost (Apache Event + PHP-FPM)
# ==============================================================================

<VirtualHost *:80>
    ServerName portal.enterprise.com
    Redirect permanent / https://portal.enterprise.com/
</VirtualHost>

<VirtualHost *:443>
    ServerName portal.enterprise.com
    ServerAdmin sysadmin@enterprise.com
    DocumentRoot /var/www/portal/public

    Protocols h2 http/1.1

    # SSL
    SSLEngine on
    SSLCertificateFile /etc/ssl/certs/enterprise.crt
    SSLCertificateKeyFile /etc/ssl/private/enterprise.key
    SSLCertificateChainFile /etc/ssl/certs/ca_chain.crt
    SSLProtocol -all +TLSv1.2 +TLSv1.3

    # Hardened Directory
    <Directory /var/www/portal/public>
        Options -Indexes +FollowSymLinks
        AllowOverride None
        Require all granted

        # Front Controller Rewrite
        RewriteEngine On
        RewriteCond %{REQUEST_FILENAME} !-f
        RewriteCond %{REQUEST_FILENAME} !-d
        RewriteRule ^ index.php [QSA,L]
    </Directory>

    # High-Speed Unix Socket FastCGI to PHP-FPM
    <FilesMatch \.php$>
        SetHandler "proxy:unix:/run/php/php8.2-fpm.sock|fcgi://localhost/"
    </FilesMatch>

    # Block sensitive files
    <FilesMatch "(\.env|\.git|\.yml|\.json|\.lock)$">
        Require all denied
    </FilesMatch>

    # Logging
    ErrorLog ${APACHE_LOG_DIR}/portal_error.log
    CustomLog ${APACHE_LOG_DIR}/portal_access.log combined
</VirtualHost>
```

---

# 7. Production War Room Incidents & Post-Mortems (RCAs)

### Incident 1: The Apache Prefork Fork-Bomb RAM Exhaustion Crash
- **Symptom**: During a flash sale, the web server ran out of memory, swap thrashed, and the Linux kernel OOM killer murdered `mysqld` and `sshd`.
- **Root Cause**: Administrator set `MaxRequestWorkers 2000` using `mpm_prefork` + `mod_php`. Under traffic, Apache spawned 1,800 child processes. With each process consuming 65 MB of RAM, memory demand hit 117 GB on a 32 GB server!
- **Remediation**: Replaced `mpm_prefork` with `mpm_event` + `php-fpm`. Capped `pm.max_children = 120`.

---

### Incident 2: The `.htaccess` I/O Thrashing Bottleneck
- **Symptom**: Cloud SSD disk IOPS saturated at 100%, causing page loads to stall for 8 seconds despite low CPU utilization.
- **Root Cause**: Deeply nested directories with `AllowOverride All` forced Apache to execute 12 `open()`/`stat()` system calls looking for `.htaccess` on every single icon and image.
- **Remediation**: Set `AllowOverride None` across all directory blocks and migrated rewrite rules into the main VirtualHost.

---

### Incident 3: PHP-FPM Socket Listen Backlog Drop 502 Outage
- **Symptom**: Apache started throwing `502 Proxy Error: Could not connect to remote server` during traffic spikes.
- **Root Cause**: PHP-FPM's socket queue backlog (`listen.backlog = 511`) filled up because all PHP workers were occupied. New connections were dropped by the Linux kernel.
- **Remediation**: Increased kernel `net.core.somaxconn = 4096` and adjusted PHP-FPM `listen.backlog = 4096`, while adding database query indexing to reduce worker execution time from 1.2s to 40ms.

---

# 8. Senior Apache & LAMP Systems Engineer Interview Bank (30 Questions)

#### Q1: What are the fundamental differences between `mpm_prefork`, `mpm_worker`, and `mpm_event`?
> **Answer**: `prefork` is multi-process (1 process per connection, no threads, safe for non-threadsafe libs but heavy RAM). `worker` is multi-process and multi-threaded (lower RAM, but threads stay blocked on idle keepalive connections). `event` is multi-threaded with an asynchronous epoll listener loop that offloads idle keepalive connections, allowing worker threads to handle active requests only.

#### Q2: Why is `AllowOverride None` strongly recommended in production?
> **Answer**: `AllowOverride All` forces Apache to traverse every parent directory on disk searching for a `.htaccess` file on every single request, causing severe disk I/O latency and filesystem cache invalidation.

#### Q3: What is the purpose of the `[QSA]` flag in `mod_rewrite`?
> **Answer**: Query String Append. It ensures that any query string present in the original incoming request (e.g. `?sort=desc&page=3`) is preserved and appended to the rewritten destination URI rather than being discarded.

#### Q4: How do you mathematically determine `pm.max_children` for PHP-FPM?
> **Answer**: Divide the physical RAM dedicated strictly to PHP by the average RSS memory footprint of a single PHP worker process during peak load (e.g., $4096\text{ MB} / 50\text{ MB} \approx 80\text{ workers}$).

#### Q5: What does `SetHandler "proxy:unix:/path.sock|fcgi://localhost/"` accomplish?
> **Answer**: It intercepts matching requests (like `.php` files) and delegates their execution to an external FastCGI server (PHP-FPM) across a Linux Unix Domain Socket using inter-process communication (IPC), avoiding TCP loopback overhead.

*(...and 25 additional questions covering `.htaccess` security, `mod_security` WAF rules, SSL OCSP, HTTP/2 multiplexing, and slowlog debugging).*
