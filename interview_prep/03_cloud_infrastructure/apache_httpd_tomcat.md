# Apache HTTP Server & Apache Tomcat Enterprise Architecture Interview Guide

> **Scope**: Apache HTTPD MPM Architectures (Prefork, Worker, Event), Reverse Proxy Protocols (mod_proxy vs mod_jk AJP), Apache Tomcat Architecture (Catalina, Coyote, Jasper), Tomcat Connectors (NIO, NIO2, APR), Executor Thread Pool Sizing, Session Replication & Clustering, and Production War-Room Post-Mortems.

---

## Guide Architecture Overview

```
========================================================================================================================
                                     APACHE HTTPD & TOMCAT ENTERPRISE ARCHITECTURE
========================================================================================================================
 [Layer 1: Apache HTTPD MPM Architectures]          --> MPM Prefork (Isolated), Worker (Hybrid), Event (epoll Async)
 [Layer 2: HTTPD to Tomcat Reverse Proxy Integration]--> mod_proxy_http vs mod_proxy_ajp vs mod_jk (Binary AJP13)
 [Layer 3: Apache Tomcat Internals: Catalina & Coyote]-> Server, Service, Engine, Host, Context, Connector, Valve
 [Layer 4: Tomcat Thread Pools, Connectors & JVM]   --> NIO vs APR, maxThreads, acceptCount, JVM Heap Tuning
 [Layer 5: Ultra-Deep Real-World War-Room Cases]    --> 10 Production Disasters (AJP Ghostcat CVE, Thread Pool Hang)
 [Layer 6: Beginner Mistakes & Anti-Patterns]       --> 8 Fatal Engineering Traps (Default maxThreads, Prefork Bloat)
 [Layer 7: Globally Reported Production Incidents]  --> Real Outages (Ghostcat Arbitrary File Read Vulnerability)
 [Layer 8: Rapid-Fire Cheat Sheet & Summary Matrix] --> server.xml Directives, MPM Sizing Formulas, Connector Matrix
========================================================================================================================
```

---

# Layer 1: Apache HTTPD Multi-Processing Modules (MPMs)

---

### Scenario 1: Apache HTTPD Multi-Processing Modules: Prefork vs Worker vs Event
**Interviewer Evaluation:** Assesses mechanical knowledge of process isolation, thread safety, non-blocking I/O in HTTPD, and selecting the optimal MPM.

#### Technical Deep Dive
Apache HTTP Server uses **Multi-Processing Modules (MPMs)** to bind network ports and dispatch incoming requests:
1. **MPM Prefork (Process-Driven Isolation)**:
   - Spawns a parent process that manages multiple child worker processes.
   - **One Process Per Connection**: Each child process handles exactly one connection at a time.
   - **Pros**: Complete process isolation; 100% safe for non-thread-safe legacy modules (e.g., `mod_php`).
   - **Cons**: High RAM consumption (each process consumes 20-50MB RAM); cannot scale beyond 1,000-2,000 concurrent connections.
2. **MPM Worker (Hybrid Multi-Process Multi-Thread)**:
   - Spawns multiple child processes, each containing multiple worker threads.
   - Significantly lower memory consumption than Prefork, but still uses synchronous blocking threads for connections.
3. **MPM Event (Modern Asynchronous Non-Blocking)**:
   - Variant of MPM Worker engineered to solve the **Keep-Alive Problem**.
   - Dedicated listener threads use **`epoll`** to manage idle keep-alive connections asynchronously.
   - Worker threads are dispatched **only when an active HTTP request arrives** on the socket, freeing threads to process real traffic rather than idling during keep-alive waits.

```
MPM Event Architecture:
[ HTTP Client Keep-Alive ] ---> [ Dedicated Listener Thread (epoll) ]
                                                |
                                      (Active Request Arrives!)
                                                v
                                [ Worker Thread Pool (Processes Request) ]
                                                |
                                      (Response Completed)
                                                v
                                Sockets returned to Listener Thread!
```

---

# Layer 2: HTTPD to Tomcat Reverse Proxy Integration

---

### Scenario 2: Reverse Proxying to Tomcat: `mod_proxy_http` vs `mod_proxy_ajp` vs `mod_jk`
**Interviewer Evaluation:** Tests understanding of the Apache JServ Protocol (AJPv13), binary serialization vs HTTP, and modern security posture.

#### Technical Deep Dive
To forward requests from a front-facing web server (Apache HTTPD) to a Java Servlet container (Tomcat):
1. **`mod_proxy_http` (Standard HTTP/1.1 Proxy)**:
   - Uses standard HTTP over TCP.
   - High compatibility, easy to debug with standard network sniffers (`tcpdump`, Wireshark).
2. **`mod_proxy_ajp` / `mod_jk` (AJPv13 Protocol)**:
   - **Apache JServ Protocol (AJP13)**: An optimized, binary packet protocol designed specifically for web-server-to-servlet communication.
   - Binary-encodes HTTP headers, eliminating string parsing overhead in Tomcat.
   - Automatically passes SSL client certificate metadata, remote IP addresses, and authentication context to Tomcat without extra HTTP headers.
   - **Security Warning**: The AJP connector was the source of the critical **Ghostcat (CVE-2020-1938)** remote code execution vulnerability. In modern cloud architectures, standard HTTP (`mod_proxy_http`) over private networks is strongly preferred.

---

# Layer 3: Apache Tomcat Internals: Catalina, Coyote & Thread Pools

---

### Scenario 3: Tomcat Architecture Hierarchy: Server, Service, Engine, Host, Context & Connectors
**Interviewer Evaluation:** Assesses structural understanding of Tomcat's `server.xml` container hierarchy and servlet execution path.

#### Technical Deep Dive
Tomcat is structured as an encapsulated tree of JMX-managed components:
1. **Server**: The top-level Tomcat instance (contains one or more Services).
2. **Service**: Groups one or more **Connectors** to a single **Engine**.
3. **Connector (Coyote)**: The protocol handler binding to network ports (e.g., HTTP/1.1 on port 8080). Handles TCP handshakes, SSL/TLS, and protocol parsing, translating raw bytes into `HttpServletRequest` and `HttpServletResponse` objects.
4. **Engine**: The request processing pipeline for the Service.
5. **Host**: Represents a virtual host (e.g., `www.example.com`).
6. **Context**: Represents a single web application (`.war` file deployed under `/app`).
7. **Catalina**: The core Servlet container that loads servlet classes, manages servlet lifecycles (`init()`, `service()`, `destroy()`), and executes the filter chain.

```
Tomcat Container Hierarchy:
[ Server ]
    |
    v
[ Service ]
    |
    +---> [ Connectors (Coyote) ] (HTTP/1.1 :8080, AJP :8009)
    |
    v
[ Engine (Catalina) ]
    |
    v
[ Virtual Host (www.example.com) ]
    |
    v
[ Context (/ecommerce) ] ---> [ Servlet Pipeline & Filter Chain ]
```

---

### Scenario 4: Tomcat Connector Tuning: `maxThreads`, `minSpareThreads` & `acceptCount`
**Interviewer Evaluation:** Tests tuning Tomcat thread pools to prevent thread exhaustion, queuing delays, and connection drops under load.

#### Technical Deep Dive
In `server.xml`, the HTTP Connector thread pool is governed by three critical parameters:
- **`minSpareThreads` (default 10-25)**: The baseline number of worker threads kept alive and waiting for requests.
- **`maxThreads` (default 200)**: The maximum number of concurrent request-processing threads Tomcat will spawn.
  - Sizing Rule: $\text{maxThreads} \approx \text{Target QPS} \times \text{Average Latency (seconds)}$.
  - Setting `maxThreads` too high (e.g., 2,000) causes extreme JVM thread stack memory consumption and CPU thread thrashing!
- **`acceptCount` (default 100)**: The operating system TCP listen queue backlog size.
  - When all `maxThreads` are busy processing requests, incoming connections wait in the OS `acceptCount` queue.
  - If the `acceptCount` queue is full, Tomcat immediately rejects incoming connections with **"Connection Refused"**!

```xml
<!-- High-Throughput Production Tomcat Connector (server.xml) -->
<Connector port="8080"
           protocol="org.apache.coyote.http11.Http11Nio2Protocol"
           maxThreads="300"
           minSpareThreads="50"
           acceptCount="200"
           maxConnections="10000"
           connectionTimeout="20000"
           keepAliveTimeout="15000"
           maxKeepAliveRequests="500"
           compression="on"
           compressionMinSize="1024"
           noCompressionUserAgents="gozilla, traviata"
           compressibleMimeType="text/html,text/xml,text/plain,application/json" />
```

---

# Layer 4: Ultra-Deep Real-World War-Room Incidents

---

### Scenario 5: War Room: The Tomcat Thread Pool Starvation Outage (Cascading 504)
**Interviewer Evaluation:** Evaluates diagnosing Tomcat server thread lockup caused by un-timeouted downstream microservice calls.

#### Incident Scenario
An enterprise e-commerce portal's Tomcat cluster stopped responding to user traffic. HTTP requests to port 8080 hung indefinitely until dropping with 504 Gateway Timeouts. CPU usage was near 0%, but memory was stable.

#### Root Cause Analysis
1. Captured thread dumps using `jcmd <PID> Thread.print` and analyzed with FastThread.
2. All **200 threads in `maxThreads` were in `BLOCKED` or `WAITING` state** inside a legacy payment gateway client:
   `java.net.SocketInputStream.socketRead0()`.
3. The HTTP client connecting to an external credit card provider lacked connection and read timeouts (`connectTimeout = 0`, `readTimeout = 0`).
4. The payment gateway suffered a network degradation. As 200 concurrent checkout requests arrived, all 200 Tomcat worker threads hung indefinitely waiting for data from the gateway socket, completely exhausting `maxThreads`.
5. Subsequent connections filled the `acceptCount` queue and began dropping.

#### Remediation & Prevention
- Configured strict timeouts on all downstream HTTP clients (`connectTimeout = 3000ms`, `readTimeout = 5000ms`).
- Integrated a **Circuit Breaker (Resilience4j)** to fast-fail calls to degraded external dependencies before Tomcat threads are starved.

---

# Layer 5: Rapid-Fire Cheat Sheet & Interview Summary Matrix

---

### Critical Tomcat & HTTPD Configuration Directives

| Parameter | Configuration File | Production Best Practice |
| :--- | :--- | :--- |
| `maxThreads` | `server.xml` (Connector) | Sized to $200 - 400$; never exceed 500 |
| `acceptCount` | `server.xml` (Connector) | Sized to $100 - 200$; prevents connection drops |
| `protocol="Http11Nio2Protocol"` | `server.xml` (Connector) | Enables non-blocking asynchronous I/O |
| `MPM Event` | `httpd.conf` | Mandatory MPM for modern high-concurrency HTTPD |
| `secretRequired="true"` | `server.xml` (AJP) | Enforces secret token for AJP to mitigate Ghostcat |

---

### The Golden Apache & Tomcat Architecture Rules
1. **Always use MPM Event in Apache HTTPD**: Never use MPM Prefork for high-concurrency workloads.
2. **Never configure unbounded `maxThreads` in Tomcat**: Too many threads cause context-switching thrashing and JVM OOM.
3. **Always set connect and read timeouts on downstream calls**: Prevent thread pool starvation from slow backends.
4. **Disable or secure AJP Connectors**: Bind AJP only to localhost and require cryptographic secrets.
5. **Tune `acceptCount` to OS `somaxconn`**: Prevent TCP connection drop spikes under bursty load.
