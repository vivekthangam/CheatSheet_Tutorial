[Back to Home](../README.md) | [Tech Glossary](glossary.md) | [Interview Prep](interview_prep.md) | [Troubleshooting Guide](troubleshooting_mastery.md)

# 🌪️ Performance Testing, Chaos Engineering & Microservice Forensics: Zero to Hero

A comprehensive masterclass on load testing, chaos experiment automation, distributed failure injection, thread/heap dump forensics, and microservice bottleneck diagnostics.

---

## 📑 Table of Contents
1. [🧠 Zero-to-Hero Performance & Chaos Mental Model](#-zero-to-hero-performance--chaos-mental-model)
2. [⚡ 1. Modern Load & Stress Testing: k6, JMeter & Gatling](#-1-modern-load--stress-testing-k6-jmeter--gatling)
3. [🧪 2. Chaos Engineering Tooling: Chaos Mesh & Toxiproxy](#-2-chaos-engineering-tooling-chaos-mesh--toxiproxy)
4. [💥 3. 5+ Production Chaos Experiment Playbooks](#-3-5-production-chaos-experiment-playbooks)
5. [🔬 4. Deep JVM Forensics: Thread Dumps, Heap Dumps & MAT](#-4-deep-jvm-forensics-thread-dumps-heap-dumps--mat)
6. [🔥 5. CPU & Memory Profiling: Async-Profiler Flame Graphs](#-5-cpu--memory-profiling-async-profiler-flame-graphs)
7. [🎓 6. Senior Chaos & Performance Interview Preparation Q&A](#-6-senior-chaos--performance-interview-preparation-qa)
8. [🔄 7. Architectural Transferability: Where & How to Apply Elsewhere](#-7-architectural-transferability-where--how-to-apply-elsewhere)

---

## 🧠 Zero-to-Hero Performance & Chaos Mental Model

### 🏛️ The Vaccine Analogy of Chaos Engineering
- **Definition:** Chaos Engineering is **not** breaking things in production randomly; it is the discipline of experimenting on a software system to build confidence in its capability to withstand turbulent conditions.
- **Analogy (The Flu Vaccine):**
  - Injecting a tiny, controlled dose of a dead virus into the human body trains the immune system to create antibodies.
  - Injecting controlled network latency or killing 1 pod in staging/canary trains Kubernetes auto-scalers, circuit breakers, and retry policies to maintain 100% uptime.

### 🎯 The 4 Steps of a Chaos Experiment
```
1. Define Steady State  ──>  2. Formulate Hypothesis  ──>  3. Inject Chaos  ──>  4. Verify & Fix
(Normal RPS & Latency)       (Circuit breaker trips;       (Latency / Packet      (Automate recovery
                              fallback works in <50ms)      Loss / Kill Pod)       or fix timeout)
```

---

## ⚡ 1. Modern Load & Stress Testing: k6, JMeter & Gatling

### 1.1 High-Performance k6 Load Test Script (JavaScript/ES6)
```javascript
import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '30s', target: 50 },  // Ramp-up to 50 virtual users
    { duration: '1m',  target: 500 }, // Spike to 500 virtual users (Peak Load)
    { duration: '30s', target: 0 },   // Ramp-down
  ],
  thresholds: {
    http_req_duration: ['p(95)<200', 'p(99)<500'], // 95% of requests must complete under 200ms
    http_req_failed: ['rate<0.01'],                 // Error rate must be < 1%
  },
};

export default function () {
  const payload = JSON.stringify({
    userId: `user_${__VU}`,
    amount: 150.00,
    currency: 'USD'
  });

  const params = {
    headers: {
      'Content-Type': 'application/json',
      'X-Idempotency-Key': `req_${__VU}_${__ITER}`
    },
  };

  const res = http.post('http://api-gateway.internal/api/v1/checkout', payload, params);
  
  check(res, {
    'status is 200': (r) => r.status === 200,
    'response body has orderId': (r) => r.json().orderId !== undefined,
  });

  sleep(0.5); // 500ms think time between requests
}
```

---

## 🧪 2. Chaos Engineering Tooling: Chaos Mesh & Toxiproxy

### 2.1 Toxiproxy: Simulating Network Chaos in Automated Integration Tests
Toxiproxy runs as a proxy in front of databases/APIs to simulate real network conditions:
```java
// Java TestContainers + Toxiproxy Setup
ToxiproxyContainer toxiproxy = new ToxiproxyContainer("ghcr.io/shopify/toxiproxy:2.5.0");
toxiproxy.start();

final Proxy postgresProxy = toxiproxy.getProxy("postgres", 5432);

// Inject 2000ms latency on all database queries
postgresProxy.toxics().latency("latency-toxic", ToxicDirection.DOWNSTREAM, 2000);

// Verify your Spring Boot service handles the timeout gracefully without crashing
assertThrows(DatabaseTimeoutException.class, () -> orderService.getOrderByUser(123));

// Remove toxic
postgresProxy.toxics().get("latency-toxic").remove();
```

---

## 💥 3. 5+ Production Chaos Experiment Playbooks

### 3.1 Playbook 1: Injecting Network Latency (Chaos Mesh YAML)
```yaml
apiVersion: chaos-mesh.org/v1alpha1
kind: NetworkChaos
metadata:
  name: payment-latency-chaos
  namespace: ecommerce
spec:
  action: delay
  mode: all
  selector:
    namespaces:
      - ecommerce
    labelSelectors:
      app: payment-service
  delay:
    latency: '1500ms'
    jitter: '200ms'
    correlation: '50'
  duration: '5m'
  direction: to
  target:
    selector:
      labelSelectors:
        app: bank-gateway
```

### 3.2 Playbook 2: Random Pod Termination (Chaos Mesh PodChaos)
```yaml
apiVersion: chaos-mesh.org/v1alpha1
kind: PodChaos
metadata:
  name: order-service-pod-kill
  namespace: ecommerce
spec:
  action: pod-kill
  mode: fixed
  value: '1'
  selector:
    namespaces:
      - ecommerce
    labelSelectors:
      app: order-service
  scheduler:
    cron: '@every 1m'
  duration: '10m'
```

### 3.3 Playbook 3: Simulating DNS Resolution Failure
```yaml
apiVersion: chaos-mesh.org/v1alpha1
kind: DNSChaos
metadata:
  name: dns-failure-chaos
  namespace: ecommerce
spec:
  action: error
  mode: all
  selector:
    namespaces:
      - ecommerce
    labelSelectors:
      app: checkout-service
  patterns:
    - 'auth.internal.corp'
  duration: '3m'
```

---

## 🔬 4. Deep JVM Forensics: Thread Dumps, Heap Dumps & MAT

### 4.1 Generating Thread Dumps Live without Service Restart
```bash
# 1. List Java processes
jcmd

# 2. Capture thread dump to file
jcmd <PID> Thread.print > /tmp/thread_dump.tdump

# Alternative using jstack
jstack -l <PID> > /tmp/thread_dump.txt
```

### 4.2 Diagnosing Thread States in Thread Dumps
- **`RUNNABLE`:** Thread is executing code or waiting for OS I/O (epoll socket read).
- **`TIMED_WAITING (sleeping or parking)`:** Normal state for idle pool threads waiting for work.
- **`BLOCKED (on object monitor)`:** 🚨 **DANGER!** Thread is stuck trying to enter a `synchronized` block locked by another thread.

```
"http-nio-8080-exec-1" #42 daemon prio=5 os_prio=0 cpu=142.12ms elapsed=182.20s tid=0x00007f9c88001000 nid=0x1a2b waiting for monitor entry [0x00007f9c3e4f8000]
   java.lang.Thread.State: BLOCKED (on object monitor)
        at com.ecommerce.service.InventoryService.reserveStock(InventoryService.java:45)
        - waiting to lock <0x000000070bc48238> (a java.lang.Object)
        - locked by "http-nio-8080-exec-2" #43
```

### 4.3 Triggering and Analyzing Heap Dumps with Eclipse MAT
```bash
# 1. Trigger live heap dump (active live objects only)
jcmd <PID> GC.heap_dump /tmp/heap_dump.hprof

# 2. Automatically dump on OOM in production (JVM Flags):
-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/var/log/jvm_dumps/
```

#### Eclipse MAT (Memory Analyzer Tool) OQL (Object Query Language) Queries:
```sql
-- Find all byte arrays larger than 5MB
SELECT * FROM byte[] b WHERE b.@usedHeapSize > 5242880

-- Find all String instances containing authorization tokens
SELECT toString(s) FROM java.lang.String s WHERE s.value LIKE "%Bearer%"

-- Inspect largest HashMaps and their entry counts
SELECT m, m.size FROM java.util.HashMap m WHERE m.size > 10000
```

---

## 🔥 5. CPU & Memory Profiling: Async-Profiler Flame Graphs

**Async-profiler** is a low-overhead sampling profiler for Java that does not suffer from the **Safepoint Bias** problem of traditional profilers.

```bash
# Profile CPU usage for 30 seconds and output an interactive SVG FlameGraph
./profiler.sh -d 30 -f /tmp/flamegraph_cpu.svg <PID>

# Profile Memory Allocations (finds where garbage collection pressure comes from)
./profiler.sh -d 30 -e alloc -f /tmp/flamegraph_alloc.svg <PID>

# Profile Lock Contention (identifies which locks threads spend time waiting for)
./profiler.sh -d 30 -e lock -f /tmp/flamegraph_lock.svg <PID>
```

---

## 🎓 6. Senior Chaos & Performance Interview Preparation Q&A

### 📌 Core Conceptual Interview Questions

#### Q1: What is the "Safepoint Bias" in JVM Profilers and why does async-profiler avoid it?
> **Answer & Explanation:**
> - Traditional JVM sampling profilers (VisualVM, older JProfiler versions) use JVM TI `GetStackTrace()`.
> - JVM TI can only capture a thread's stack trace when the thread reaches a **Safepoint** (e.g. method returns, loop iterations).
> - If a method has tight uncounted loops without safepoints, the profiler falsely attributes CPU time to the *next* method that hits a safepoint (**Safepoint Bias**).
> - **Async-profiler** uses Linux kernel `perf_events` and HotSpot `AsyncGetCallTrace` signals, sampling CPU instructions directly at hardware timer ticks without waiting for safepoints.

#### Q2: How do you design a Cascading Failure Blast Radius defense for 3rd-party Payment Gateways?
> **Answer & Explanation:**
> 1. **Timeouts:** Aggressive HTTP connect ($500\text{ms}$) and read ($2000\text{ms}$) timeouts.
> 2. **Bulkhead:** Dedicate a separate thread pool / WebClient connection pool (e.g. max 15 concurrent calls) so payment slowdowns never starve user browsing threads.
> 3. **Circuit Breaker (Resilience4j):** If 5 consecutive calls fail or exceed timeout, open the circuit and return immediate fallback ("Payment queued for asynchronous processing").

---

### 🚨 Real-World Scenario-Based Interview Questions

#### Scenario Q1: Diagnosing 100% CPU Utilization with Healthy Heap Memory
> **Interviewer Question:** *"A production microservice CPU suddenly pins at 100%. Throughput drops to 0. Heap memory usage is only 20%. How do you identify the exact line of code causing this in under 2 minutes?"*
>
> **Senior Architect Answer:**
> 1. **Find the High-CPU Native Thread ID:**
>    ```bash
>    top -H -p <PID>
>    ```
>    Locate the thread with the highest CPU % (e.g. TID `4921`).
> 2. **Convert Thread ID to Hexadecimal:**
>    $$4921_{10} = \text{0x1339}_{16}$$
> 3. **Search for `nid=0x1339` in Thread Dump:**
>    ```bash
>    jcmd <PID> Thread.print | grep -A 25 "nid=0x1339"
>    ```
>    *Discovery:* The stack trace points directly to `HashMap.get()` spinning in an infinite loop due to unsafe multi-threaded mutation on a non-concurrent map.

---

## 🔄 7. Architectural Transferability: Where & How to Apply Elsewhere

1. **Game Server Infrastructure:** Simulating high-packet-loss mobile WiFi disconnects using Toxiproxy to verify lockstep multiplayer state synchronization.
2. **Fintech Stock Trading Engine:** Running async-profiler lock contention profiling to eliminate nanosecond mutex overhead in order matching engines.
3. **Database Failover Drills:** Automating PostgreSQL master pod kills during peak simulated write load to verify 0-second Patroni/Raft automated leader elections.

---

[⬆️ Back to Top](#-performance-testing-chaos-engineering--microservice-forensics-zero-to-hero)
