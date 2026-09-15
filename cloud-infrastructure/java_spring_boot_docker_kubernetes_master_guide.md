# 🐳 Java & Spring Boot Docker & Kubernetes (K8s) Production CheatSheet & Master Guide

> **Target Audience**: Staff Software Architects, Principal DevOps/Platform Engineers, Senior Java Developers, and SREs.  
> **Prerequisites**: Working knowledge of Java and Spring Boot. Zero prior deep container runtime or Kubernetes internals knowledge required. We build systematically from the Linux kernel cgroups and namespaces, to JVM container memory mechanics (`MaxRAMPercentage`, OOMKilled 137), layered multi-stage Dockerfiles (Temurin, Distroless, GraalVM), and dissect zero-downtime rolling updates, health probe traps, HPA sizing, and production Kubernetes manifests.

---

## 🗺️ Master Catalog & Repository Index Updates

This flagship master guide is fully indexed across the repository:

* **Repository Categorization Index**: [`all_markdown_files_categorized.md`](../all_markdown_files_categorized.md) — Category 7 (Cloud Native, Containers & Infrastructure) updated to 17 documents with this entry added.
* **Root Repository Architecture Index**: [`README.md`](../README.md) — Section 7 table updated with technical highlights and breakdown.
* **Omni-Protocol Platform Documentation**: [`projects/omni-api-realtime-platform/README.md`](../projects/omni-api-realtime-platform/README.md) — Section 7.13 added detailing Java Containerization & K8s deployment patterns.
* **Platform Walkthrough Artifact**: `walkthrough.md` — Registered as Guide #13 with complete verification status.

---

## ⚡ The Architectural Delivery Pipeline

```
┌─────────────────────────────────────────────────────────────────────────────────────────────────────────────────┐
│                               THE END-TO-END JAVA & SPRING BOOT K8S PIPELINE                                    │
├─────────────────────────────────────────────────────────────────────────────────────────────────────────────────┤
│ [1. Spring Boot 3 Layered Jar]   ──► dependencies | spring-boot-loader | snapshot-dependencies | application   │
│                                      (Extracted via: java -Djarmode=tools -jar app.jar extract)                 │
│ [2. Multi-Stage Dockerfile]       ──► Build Stage (Maven/Gradle) ──► Runtime Stage (Eclipse Temurin/Distroless) │
│                                      Non-root user (USER spring:spring), minimal layers, zero CVEs             │
│ [3. OCI Image Registry]          ──► Signed image push with SHA256 digest pinning (GHCR / ECR / Docker Hub)     │
│ [4. Kubernetes Pod Scheduling]   ──► kube-scheduler binds Pod to Node based on resource requests & affinity     │
│ [5. Linux cgroups & Namespaces]  ──► Kubelet (CRI containerd) enforces CPU quota (CFS) & memory limit (cgroups)│
│ [6. JVM Container Ergonomics]    ──► -XX:+UseContainerSupport reads cgroup limits -> sets Heap & Thread pools    │
│ [7. Startup & Health Probes]     ──► Startup Probe ──► Readiness Probe (/actuator/health/readiness)             │
│                                      EndpointSlice Controller adds Pod IP to K8s Service Endpoints              │
│ [8. Active Traffic Routing]      ──► Ingress / Service Mesh routes live user traffic to Tomcat socket           │
│ [9. Zero-Downtime Termination]   ──► preStop sleep 15s ──► Deregister from Service ──► SIGTERM ──►              │
│                                      Spring Boot graceful shutdown (server.shutdown=graceful) ──► Clean Exit 🚀 │
└─────────────────────────────────────────────────────────────────────────────────────────────────────────────────┘
```

---

# Track 1: JVM Container Physics & Linux cgroups Mechanics

## 1.1 The OOMKilled (Exit Code 137) Mystery
When running Java inside a Docker container or Kubernetes Pod, the most common Sev-1 outage is:
```
State:          Terminated
  Reason:       OOMKilled
  Exit Code:    137
```

### Why Does This Happen?
In classical bare-metal or VM environments, the HotSpot JVM looks at the **total host hardware memory**.
If your Kubernetes worker node has **64 GB RAM**, and your Pod has a limit of **2 GB**:
* Early Java (Java 8 before 8u191) queried the OS kernel via `/proc/meminfo`. It saw 64 GB!
* HotSpot by default set maximum heap to **25% of total memory**: $64\text{ GB} \times 0.25 = \mathbf{16\text{ GB}}$!
* The JVM happily grew its heap past 2 GB.
* The Linux kernel **cgroup OOM Killer** detected the Pod exceeding its 2 GB container limit.
* The kernel sent `SIGKILL` (Signal 9) immediately. Exit code: $128 + 9 = \mathbf{137}$!

---

## 1.2 The Real JVM Memory Consumption Formula

A common developer misconception is: `Container Memory Limit = Maximum Heap (-Xmx)`.
**This is false and guarantees an OOMKill.** The JVM consumes memory across multiple distinct areas:

$$\mathbf{\text{Total Container Memory}} = \text{Heap} + \text{Metaspace} + (\text{Thread Stack} \times \text{Thread Count}) + \text{Direct Buffers} + \text{Native Memory}$$

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                           JVM CONTAINER MEMORY ANATOMY                          │
├────────────────────────────────────────────────────────┬────────────────────────┤
│ 1. Heap Memory (-Xmx / MaxRAMPercentage)               │ ~60% – 75% of Limit    │
│    Young Gen (Eden, S0, S1) + Old Gen                  │                        │
├────────────────────────────────────────────────────────┼────────────────────────┤
│ 2. Metaspace (-XX:MaxMetaspaceSize)                    │ ~128 MB – 384 MB       │
│    Class metadata, method bytecodes, constant pools    │                        │
├────────────────────────────────────────────────────────┼────────────────────────┤
│ 3. Thread Stacks (-Xss1m * Active Threads)             │ ~100 MB – 250 MB       │
│    Tomcat 200 workers + GC + JIT + internal threads    │                        │
├────────────────────────────────────────────────────────┼────────────────────────┤
│ 4. Direct Memory / Off-Heap (-XX:MaxDirectMemorySize)  │ ~64 MB – 256 MB        │
│    Netty, Tomcat NIO byte buffers, gRPC socket buffers │                        │
├────────────────────────────────────────────────────────┼────────────────────────┤
│ 5. Native Code & JIT (-XX:ReservedCodeCacheSize)       │ ~128 MB – 240 MB       │
│    JIT compiled native assembly instructions           │                        │
├────────────────────────────────────────────────────────┼────────────────────────┤
│ 6. Garbage Collector & JVM Internal Overhead           │ ~64 MB – 128 MB        │
│    Card tables, mark-bit maps, HotSpot C++ runtime     │                        │
└────────────────────────────────────────────────────────┴────────────────────────┘
```

---

## 1.3 The Golden JVM Container Flags

Since Java 8u191, Java 11, Java 17, and Java 21, the JVM is container-aware via `-XX:+UseContainerSupport` (enabled by default).

### Recommended Production Flags:
```bash
JAVA_OPTS="\
  -XX:+UseContainerSupport \
  -XX:MaxRAMPercentage=75.0 \
  -XX:InitialRAMPercentage=50.0 \
  -XX:MinRAMPercentage=50.0 \
  -XX:MaxMetaspaceSize=384m \
  -XX:ReservedCodeCacheSize=240m \
  -XX:+UseG1GC \
  -XX:+ExitOnOutOfMemoryError \
  -Djava.security.egd=file:/dev/urandom \
  -Dnetworkaddress.cache.ttl=30"
```

* **Why `MaxRAMPercentage=75.0`?**
  If your Pod memory limit is **2,048 MB**, HotSpot caps the Heap at **1,536 MB** (75%), leaving **512 MB** (25%) for Metaspace, thread stacks, direct byte buffers, and JIT code cache.
* **Why `-XX:+ExitOnOutOfMemoryError`?**
  If an application runs out of heap, HotSpot terminates the process immediately, allowing Kubernetes to restart the Pod cleanly instead of leaving a zombie process returning HTTP 500 errors.
* **Why `-Dnetworkaddress.cache.ttl=30`?**
  The JVM by default caches DNS lookups **forever** (`ttl = -1`). In Kubernetes, Pod IPs and internal Service VIPs change constantly. Setting TTL to 30 seconds prevents stale connection blackouts.

---

## 1.4 Linux cgroups v1 vs. cgroups v2

Kubernetes 1.25+ and modern Linux distributions (RHEL 9, Ubuntu 22.04+, Amazon Linux 2023) use **cgroups v2**:

| Feature | cgroups v1 | cgroups v2 |
| :--- | :--- | :--- |
| **Path** | `/sys/fs/cgroup/memory/memory.limit_in_bytes` | `/sys/fs/cgroup/memory.max` |
| **Hierarchy** | Separate controllers per subsystem | Unified single hierarchy |
| **Memory Pressure** | Hard cut-offs; OOM Killer triggers abruptly | Memory pressure stalls (`memory.high` throttling before OOM) |
| **JVM Support** | Java 8u191+, 11.0.16+, 17.0.3+ | Java 11.0.16+, 17.0.3+, 21+ |

---

## 1.5 CPU Throttling & CFS Quota Physics

When you configure `resources.limits.cpu: "2"` in Kubernetes, the Linux kernel CFS (Completely Fair Scheduler) enforces:
$$\text{Quota} = \text{Limits} \times \text{Period} = 2 \times 100\text{ms} = \mathbf{200\text{ms per 100ms window}}$$

* If your Spring Boot app bursts across 8 active threads during JSON parsing and consumes 200ms within the first 30ms, the kernel **pauses all threads for the remaining 70ms**!
* **Symptoms**: Latency p99 spikes to hundreds of milliseconds while CPU utilization appears low.
* **Production Recommendation**: Set `resources.requests.cpu` equal to anticipated average load, and either omit `resources.limits.cpu` or set it high enough to prevent CFS throttling.

---

# Track 2: Multi-Stage Dockerfile Engineering for Spring Boot

## 2.1 The Layered JAR Advantage
In standard Spring Boot fat JARs (`app.jar`), business code and third-party dependencies are packed into a single 80 MB file. Every 1-line code change forces Docker to upload and download 80 MB!

Spring Boot provides **Layered JARs** to optimize caching:
```bash
# Extract layers from Spring Boot JAR
java -Djarmode=tools -jar application.jar extract --layers --launcher
```
Layers generated:
1. `dependencies/`: Spring framework, Jackson, Netty, HikariCP (Changes rarely).
2. `spring-boot-loader/`: Jar launcher infrastructure (Changes on Boot upgrade).
3. `snapshot-dependencies/`: Internal organization snapshots.
4. `application/`: Your compiled `.class` files and properties (Changes every build, < 500 KB!).

---

## 2.2 Production Multi-Stage Dockerfile (Eclipse Temurin JRE)

```dockerfile
# ==============================================================================
# Stage 1: Build & Layer Extraction
# ==============================================================================
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /workspace

# Copy POM and download dependencies (Cached Docker Layer!)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build executable JAR
COPY src ./src
RUN mvn clean package -DskipTests

# Extract Spring Boot Layered JAR
WORKDIR /workspace/target
RUN java -Djarmode=tools -jar *.jar extract --layers --launcher --destination /workspace/extracted

# ==============================================================================
# Stage 2: Hardened Runtime Container
# ==============================================================================
FROM eclipse-temurin:21-jre-jammy AS runtime

# Security: Create non-root system user and group
RUN groupadd -r spring && useradd -r -g spring spring

WORKDIR /app

# Copy layered components in order of change frequency (Least -> Most)
COPY --from=builder --chown=spring:spring /workspace/extracted/dependencies/ ./
COPY --from=builder --chown=spring:spring /workspace/extracted/spring-boot-loader/ ./
COPY --from=builder --chown=spring:spring /workspace/extracted/snapshot-dependencies/ ./
COPY --from=builder --chown=spring:spring /workspace/extracted/application/ ./

# Switch to unprivileged user
USER spring:spring

# Expose HTTP and Actuator ports
EXPOSE 8080 8081

# Production JVM Ergonomics Environment
ENV JAVA_OPTS="\
  -XX:+UseContainerSupport \
  -XX:MaxRAMPercentage=75.0 \
  -XX:InitialRAMPercentage=50.0 \
  -XX:+UseG1GC \
  -XX:+ExitOnOutOfMemoryError \
  -Djava.security.egd=file:/dev/urandom \
  -Dnetworkaddress.cache.ttl=30"

# Launch using Spring Boot JarLauncher (Preserves classloader ordering)
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]
```

---

## 2.3 Google Distroless Dockerfile (Zero-CVE Posture)

For zero-trust security environments, Distroless images contain **only the JRE and runtime libraries**—no package manager, no shell (`/bin/sh`), and no curl.

```dockerfile
FROM gcr.io/distroless/java21-debian12:nonroot
WORKDIR /app

# Copy pre-extracted layers directly
COPY --chown=nonroot:nonroot target/extracted/dependencies/ ./
COPY --chown=nonroot:nonroot target/extracted/spring-boot-loader/ ./
COPY --chown=nonroot:nonroot target/extracted/snapshot-dependencies/ ./
COPY --chown=nonroot:nonroot target/extracted/application/ ./

USER nonroot:nonroot
EXPOSE 8080

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-XX:+UseG1GC", \
  "-XX:+ExitOnOutOfMemoryError", \
  "org.springframework.boot.loader.launch.JarLauncher"]
```

---

## 2.4 GraalVM Native Image vs. HotSpot Comparison

| Metric | Standard JVM (HotSpot) | GraalVM Native Image (AOT) |
| :--- | :--- | :--- |
| **Startup Time** | 3.5 – 12 seconds | **0.035 – 0.080 seconds (35–80ms)** |
| **Memory Footprint (RSS)** | 350 MB – 800 MB | **35 MB – 90 MB** |
| **Image Size** | 220 MB – 350 MB | **35 MB – 65 MB** |
| **Build Time** | 30 – 90 seconds | 4 – 9 minutes |
| **Peak Throughput** | Tier-1 (JIT runtime optimization) | Tier-1.5 (PGO required for matching JIT) |
| **Best Used For** | High-throughput long-running APIs | Serverless, Knative, scale-from-zero pods |

---

# Track 3: The Kubernetes Pod Lifecycle & Zero-Downtime Deployment

## 3.1 The 502 Bad Gateway Race Condition

During a standard rolling update (`kubectl rollout restart deployment`), users often report **intermittent 502 Bad Gateway or 504 Gateway Timeout errors**.

```
[kubectl delete pod]
        │
        ├──► 1. kube-apiserver sets Pod state to "Terminating"
        │       EndpointSlice controller starts propagating removal to IPVS/iptables & Ingress
        │       (PROPAGATION DELAY: TAKES 1 TO 4 SECONDS ACROSS THE CLUSTER!)
        │
        └──► 2. Kubelet SIMULTANEOUSLY sends SIGTERM to the Spring Boot Container!
                │
                ▼
      [Spring Boot stops listening on port 8080]
                │
                ▼
[INCOMING TRAFFIC STILL IN FLIGHT FROM INGRESS HITS CLOSED PORT: 💥 502 BAD GATEWAY!]
```

---

## 3.2 The 3-Pronged Zero-Downtime Cure

To achieve 100% zero-drop rolling deployments, three configurations must work together:

```
[1. Ingress] ───────── Still sending requests for 5s while route table updates
                            │
                            ▼
[2. preStop Hook] ──── Executes "sleep 15"
                            │ Holds container alive, completes in-flight requests,
                            │ gives Ingress plenty of time to drain endpoints!
                            ▼
[3. SIGTERM Sent] ──── Kubelet sends SIGTERM after sleep finishes
                            │
                            ▼
[4. Spring Graceful] ─ server.shutdown=graceful
                            │ Rejects new connections, allows active HTTP threads to finish
                            ▼
[5. Clean Exit 0] ──── Pod terminates with ZERO dropped packets!
```

### 1. Spring Boot Configuration (`application.yml`):
```yaml
server:
  port: 8080
  shutdown: graceful  # Rejects new connections, finishes in-flight requests

spring:
  lifecycle:
    timeout-per-shutdown-phase: 30s  # Maximum wait time for active threads
```

### 2. Kubernetes Pod Spec (`deployment.yaml`):
```yaml
spec:
  terminationGracePeriodSeconds: 60  # Must be > (preStop sleep + Spring timeout)
  containers:
    - name: api-service
      lifecycle:
        preStop:
          exec:
            command: ["sh", "-c", "sleep 15"]
```

---

# Track 4: Health Probes Mastery (Liveness vs. Readiness vs. Startup)

Spring Boot Actuator integrates natively with Kubernetes probes via:
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health, prometheus, info
  endpoint:
    health:
      probes:
        enabled: true
      show-details: always
  health:
    livenessstate:
      enabled: true
    readinessstate:
      enabled: true
```

## 4.1 Probe Architecture Comparison Matrix

| Probe Type | Actuator Endpoint | What It Checks | Action on Failure | Golden Rule |
| :--- | :--- | :--- | :--- | :--- |
| **Startup Probe** | `/actuator/health/liveness` | Has Spring completed context initialization? | Delays liveness & readiness checks | Set high `failureThreshold` for slow boots (60s) |
| **Liveness Probe** | `/actuator/health/liveness` | Is internal JVM process stuck in a deadlock? | **Kills and restarts the container!** | **NEVER check external DB, Redis, or Kafka!** |
| **Readiness Probe**| `/actuator/health/readiness`| Is the Pod ready to accept incoming HTTP traffic? | **Removes Pod IP from Service routing** | Checks DB pool, cache connections, warmup state |

> [!CAUTION]
> **The Liveness Probe Cascading Meltdown Trap**:
> If your Liveness probe queries PostgreSQL, and the database slows down under load:
> 1. The liveness probe times out on Pod 1.
> 2. Kubernetes kills Pod 1 and restarts it.
> 3. Traffic redirects to Pod 2, 3, and 4, increasing their load!
> 4. Pod 2, 3, and 4 fail their liveness probes and are killed!
> 5. **Result**: Your entire production cluster crashes simultaneously in a self-inflicted restart loop.
> **Fix**: Keep Liveness purely internal (`/actuator/health/liveness`). Use Readiness for dependency checking.

---

# Track 5: Complete Production Kubernetes Manifests

## 5.1 Production `deployment.yaml`

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: spring-api-service
  namespace: production
  labels:
    app.kubernetes.io/name: spring-api-service
    app.kubernetes.io/version: "1.0.0"
spec:
  replicas: 3
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 25%
      maxUnavailable: 0       # Zero downtime: never destroy before new is ready
  selector:
    matchLabels:
      app: spring-api-service
  template:
    metadata:
      labels:
        app: spring-api-service
      annotations:
        prometheus.io/scrape: "true"
        prometheus.io/path: "/actuator/prometheus"
        prometheus.io/port: "8081"
    spec:
      terminationGracePeriodSeconds: 60
      
      # Pod Anti-Affinity: Spread replicas across different physical nodes
      affinity:
        podAntiAffinity:
          preferredDuringSchedulingIgnoredDuringExecution:
            - weight: 100
              podAffinityTerm:
                labelSelector:
                  matchExpressions:
                    - key: app
                      operator: In
                      values:
                        - spring-api-service
                topologyKey: kubernetes.io/hostname

      securityContext:
        runAsNonRoot: true
        runAsUser: 10001
        runAsGroup: 10001
        fsGroup: 10001

      containers:
        - name: spring-api
          image: ghcr.io/enterprise/spring-api-service:1.0.0@sha256:4f3c...
          imagePullPolicy: IfNotPresent
          
          ports:
            - name: http
              containerPort: 8080
            - name: management
              containerPort: 8081

          # Environment configuration
          env:
            - name: SPRING_PROFILES_ACTIVE
              value: "production"
            - name: JAVA_OPTS
              value: "-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -XX:+ExitOnOutOfMemoryError"
            - name: DB_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: db-credentials
                  key: password

          # Resource bounds: Prevents noisy neighbors and OOMKills
          resources:
            requests:
              cpu: "500m"
              memory: "1536Mi"
            limits:
              cpu: "2000m"
              memory: "2048Mi"

          # Zero-Downtime Draining Hook
          lifecycle:
            preStop:
              exec:
                command: ["sh", "-c", "sleep 15"]

          # 1. Startup Probe: Allows 60s for Spring to load before Liveness kicks in
          startupProbe:
            httpGet:
              path: /actuator/health/liveness
              port: management
            initialDelaySeconds: 10
            periodSeconds: 5
            failureThreshold: 10

          # 2. Liveness Probe: Internal deadlock detection only
          livenessProbe:
            httpGet:
              path: /actuator/health/liveness
              port: management
            periodSeconds: 10
            timeoutSeconds: 3
            failureThreshold: 3

          # 3. Readiness Probe: Gatekeeper for incoming traffic
          readinessProbe:
            httpGet:
              path: /actuator/health/readiness
              port: management
            periodSeconds: 5
            timeoutSeconds: 2
            failureThreshold: 2

          # Read-only root filesystem hardening
          securityContext:
            readOnlyRootFilesystem: true
            allowPrivilegeEscalation: false
            capabilities:
              drop:
                - ALL

          # Writable volume for Spring Boot embedded Tomcat /tmp directory
          volumeMounts:
            - name: tmp-volume
              mountPath: /tmp
            - name: config-volume
              mountPath: /app/config
              readOnly: true

      volumes:
        - name: tmp-volume
          emptyDir: {}
        - name: config-volume
          configMap:
            name: spring-api-config
```

---

## 5.2 Production `hpa.yaml` (Horizontal Pod Autoscaler)

```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: spring-api-hpa
  namespace: production
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: spring-api-service
  minReplicas: 3
  maxReplicas: 20
  metrics:
    - type: Resource
      resource:
        name: cpu
        target:
          type: Utilization
          averageUtilization: 70
    - type: Resource
      resource:
        name: memory
        target:
          type: Utilization
          averageUtilization: 80
  behavior:
    scaleDown:
      stabilizationWindowSeconds: 300  # Prevent flapping / thrashing
      policies:
        - type: Percent
          value: 10
          periodSeconds: 60
    scaleUp:
      stabilizationWindowSeconds: 0    # Scale up immediately under spike
      policies:
        - type: Percent
          value: 100
          periodSeconds: 15
```

---

## 5.3 Production `pdb.yaml` (Pod Disruption Budget)

```yaml
apiVersion: policy/v1
kind: PodDisruptionBudget
metadata:
  name: spring-api-pdb
  namespace: production
spec:
  minAvailable: 2  # Always keep at least 2 pods alive during node maintenance/drains
  selector:
    matchLabels:
      app: spring-api-service
```

---

# Track 6: Docker & Kubernetes CLI Rosetta Stone

### Docker Diagnostic Commands for Java
```bash
# 1. Inspect live container memory usage against cgroup limit
docker stats <container_id>

# 2. Check cgroups v2 memory max limit on Linux host
cat /sys/fs/cgroup/system.slice/docker-<container_id>.scope/memory.max

# 3. View layer size breakdown of built image
docker history --human --format "{{.Size}}\t{{.CreatedBy}}" my-spring-app:latest

# 4. Run interactive shell as root for container debugging (if available)
docker exec -u 0 -it <container_id> /bin/sh

# 5. Build multi-arch image (AMD64 + ARM64 Apple Silicon) using Buildx
docker buildx build --platform linux/amd64,linux/arm64 -t myrepo/spring-api:1.0.0 --push .
```

### Kubernetes Diagnostic Commands for Java
```bash
# 1. Capture thread dump from running Java Pod without restarting
kubectl exec -it <pod_name> -n production -- jcmd 1 Thread.print > threaddump.txt

# 2. Check if Pod was killed by OOM Killer (Exit Code 137)
kubectl describe pod <pod_name> -n production | grep -E "OOMKilled|Exit Code"

# 3. Stream real-time logs with correlation ID filter
kubectl logs -f <pod_name> -c spring-api -n production | grep "ORDER-9481"

# 4. View CPU and Memory usage across all pods in namespace
kubectl top pods -n production --sort-by=memory

# 5. Port-forward internal Actuator port to localhost
kubectl port-forward <pod_name> 8081:8081 -n production

# 6. Check EndpointSlice to verify if Pod is currently receiving traffic
kubectl get endpointslices -l kubernetes.io/service-name=spring-api-service -n production
```

---

# Track 7: 10 Deep Production Failure Modes & War Stories

### 1. The Read-Only Root Filesystem Tomcat `/tmp` Crash
* **Failure**: Container crashes during boot with `java.io.IOException: Permission denied` creating temporary Tomcat work directories.
* **Mechanism**: Security policy set `readOnlyRootFilesystem: true`, but Spring Boot embedded Tomcat requires writing `.war`/temp files to `/tmp`.
* **Fix**: Mount an `emptyDir: {}` volume at `/tmp`.

### 2. The Broken DNS Caching Outage
* **Failure**: After a PostgreSQL failover or Aurora blue/green switch, Java pods continue trying to connect to the dead database IP indefinitely.
* **Mechanism**: JVM defaults to caching DNS lookups forever (`networkaddress.cache.ttl = -1`).
* **Fix**: Pass `-Dnetworkaddress.cache.ttl=30` to JVM flags.

### 3. Database Connection Pool Starvation Under HPA Scale-Out
* **Failure**: Traffic spikes $\to$ HPA scales Pods from 5 to 50 $\to$ Database crashes under 5,000 active connections ($50 \times 100\text{ Hikari connections}$).
* **Mechanism**: Sizing HikariCP pool at 100 per pod without calculating database max connections limit:
  $$\text{Total Connections} = \text{Max Replicas} \times \text{maximumPoolSize}$$
* **Fix**: Reduce `maximumPoolSize` to 10–20, and deploy **PgBouncer** or **AWS RDS Proxy** in front of PostgreSQL.

### 4. Premature Pod Termination Dropping Active HTTP Traffic
* **Failure**: Rolling deployment causes 0.5% packet drop and 502 errors at Ingress.
* **Mechanism**: Pod receives `SIGTERM` and closes port before Ingress routing tables update.
* **Fix**: Add `lifecycle.preStop.exec.command: ["sh", "-c", "sleep 15"]`.

### 5. Liveness Probe Thundering Herd Cascade
* **Failure**: Database latency spike causes all Java pods to fail liveness probes and restart simultaneously.
* **Mechanism**: Including database health check in `/actuator/health/liveness`.
* **Fix**: Split probes: Liveness checks internal JVM only (`/actuator/health/liveness`); Readiness checks external dependencies (`/actuator/health/readiness`).

### 6. ForkJoinPool Core Count Miscalculation Under CFS Throttling
* **Failure**: Batch processing is 4x slower in container than on local laptop.
* **Mechanism**: Linux CFS quota limits CPU to 1 core, but node has 64 cores. Early JVM sized ForkJoinPool based on 64 cores, causing massive thread context-switch thrashing.
* **Fix**: Java 17+ automatically configures `Runtime.getRuntime().availableProcessors()` from cgroups. For older JVMs, set `-XX:ActiveProcessorCount=N`.

### 7. ThreadLocal Memory Leaks in Recycled Tomcat Threads
* **Failure**: Pod memory grows continuously over 48 hours until OOMKilled, despite low traffic.
* **Mechanism**: Custom filter sets `ThreadLocal<UserContext>` without calling `.remove()` in `finally`. Tomcat worker threads retain large object graphs.
* **Fix**: Always call `threadLocal.remove()` in `HandlerInterceptor.afterCompletion()` or a Servlet Filter `finally` block.

### 8. Node Eviction Cascades Due to Missing Resource Requests
* **Failure**: Pods randomly killed with `Evicted: The node had condition: [DiskPressure / MemoryPressure]`.
* **Mechanism**: Specifying `limits` but omitting `requests`. Kubernetes assigns the Pod to the `BestEffort` QoS class, making it the **first candidate for eviction**.
* **Fix**: Always specify both `requests` and `limits`.

### 9. Metaspace Leak from Dynamic Class Generation
* **Failure**: Pod crashes with `java.lang.OutOfMemoryError: Metaspace`.
* **Mechanism**: Libraries using CGLIB, Byte Buddy, or dynamic proxies generate classes at runtime without releasing ClassLoaders.
* **Fix**: Set `-XX:MaxMetaspaceSize=384m` and profile with `jcmd <pid> VM.classloader_stats`.

### 10. Spring Cloud Kubernetes Overriding Local Configuration
* **Failure**: Staging pods connect to production Kafka brokers upon startup.
* **Mechanism**: `spring-cloud-starter-kubernetes-fabric8` automatically reloads ConfigMaps from the cluster, overwriting local `application-staging.yml`.
* **Fix**: Standardize on native Kubernetes ConfigMap mounts (`volumeMounts`) instead of heavyweight Spring Cloud client libraries.

---

# Track 8: 10 Beginner Mistakes vs. 10 Advanced Anti-Patterns

```
┌─────────────────────────────────────────────────────────────────────────────────────────────────┐
│                                   BEGINNER MISTAKES VS. ADVANCED TRAPS                           │
├────────────────────────────────┬────────────────────────────────────────────────────────────────┤
│ Beginner Mistake               │ Why It Bites & What Happens                                    │
├────────────────────────────────┼────────────────────────────────────────────────────────────────┤
│ 1. Running container as root   │ Security vulnerability: container breakout compromises node    │
│ 2. -Xmx == Memory Limit        │ Guaranteed OOMKill: leaves zero RAM for Metaspace and stacks   │
│ 3. Fat JAR in single layer     │ 80MB uploaded on every 1-line change; kills CI/CD cache        │
│ 4. Missing preStop sleep       │ Ingress drops active HTTP connections with 502 Bad Gateway     │
│ 5. DB check in Liveness probe  │ DB blip triggers simultaneous cluster-wide restart cascade     │
│ 6. No CPU/Memory requests      │ Pod treated as BestEffort QoS; evicted under node pressure     │
│ 7. Forever DNS caching         │ Stale IP connections after K8s Service / DB failovers          │
│ 8. Hardcoded ports in code     │ Breaks standard container port remapping and Helm templates    │
│ 9. Using latest Docker tag     │ Non-deterministic builds; breaks rollbacks in production       │
│ 10. Storing logs in container  │ Fills container overlay filesystem; triggers DiskPressure      │
├────────────────────────────────┼────────────────────────────────────────────────────────────────┤
│ Advanced Enterprise Trap       │ Architectural Pathology                                        │
├────────────────────────────────┼────────────────────────────────────────────────────────────────┤
│ 1. CFS Quota Latency Spikes    │ Restrictive CPU limits cause thread throttling and p99 spikes  │
│ 2. Unsynchronized Grace Period │ terminationGracePeriodSeconds < (preStop + graceful shutdown)  │
│ 3. HPA Database Exhaustion     │ Pod auto-scaling multiplies HikariCP connections beyond DB max │
│ 4. Missing Read-Only /tmp Mount│ Read-only container crashes when Tomcat creates work dir       │
│ 5. Virtual Thread Pinning      │ Synchronized blocks pin carrier threads during container I/O   │
│ 6. Missing PodAntiAffinity     │ All 3 replicas scheduled on same physical node; node down = 500│
│ 7. Spring Cloud K8s Bloat      │ Requires cluster-wide RBAC permissions for basic config access │
│ 8. Missing PDB on Node Drain   │ Cluster autoscaler drains node, killing all active replicas    │
│ 9. Uncapped Metaspace / Code   │ Native memory leaks cause cgroup OOMKill before GC triggers    │
│ 10. Multi-arch Architecture Mix│ Building on Apple Silicon ARM64, deploying to Linux AMD64 node │
└────────────────────────────────┴────────────────────────────────────────────────────────────────┘
```

---

# Track 9: 2 Real-World Sev-1 Outage Post-Mortems

## Post-Mortem 1: The Black Friday 137 OOMKilled Rolling Deployment Cascade
* **Incident Summary**: During high-volume Black Friday sales, a routine canary release triggered a rolling deployment. Within 3 minutes, all 24 production Pods were OOMKilled, resulting in a 14-minute total checkout outage.
* **Root Cause Analysis**:
  ```yaml
  # Flawed Configuration
  resources:
    limits:
      memory: "2048Mi"
  env:
    - name: JAVA_OPTS
      value: "-Xmx2048m" # FATAL TRAP!
  ```
  The team set `-Xmx2048m` equal to the container limit of `2048Mi`. During startup under heavy traffic, Jackson JSON deserialization allocated **180 MB of direct byte buffers**, and 200 active Tomcat threads allocated **200 MB of thread stack space**. Total memory reached **2,428 MB**, exceeding the 2,048 MB cgroup limit. The Linux kernel sent `SIGKILL` (Exit Code 137). As Kubernetes rescheduled pods, the surge in startup load killed each new pod sequentially.
* **Resolution**: Removed hardcoded `-Xmx2048m` and replaced with `-XX:MaxRAMPercentage=75.0`. Increased container limit to `2560Mi`.

## Post-Mortem 2: The 3.2-Second 502 Bad Gateway Ingress Race Condition
* **Incident Summary**: Every CI/CD automated deployment produced a brief 3 to 5-second burst of HTTP 502 errors, generating 2,000+ failed user checkouts per day.
* **Root Cause Analysis**:
  The Pod specification lacked a `preStop` hook:
  ```yaml
  # Flawed Deployment Spec
  containers:
    - name: api
      # No lifecycle preStop hook!
  ```
  When `kubectl set image` executed, Kubelet immediately sent `SIGTERM` to the container while NGINX Ingress still had the Pod IP in its upstream pool. For 3.2 seconds, NGINX routed traffic to a closed socket.
* **Resolution**: Added `lifecycle.preStop.exec.command: ["sh", "-c", "sleep 15"]` and configured Spring Boot `server.shutdown=graceful`. 502 errors dropped to **0.00%**.

---

# Track 10: 40+ Core Terms Technical Glossary

1. **`cgroup` (Control Group)**: Linux kernel mechanism limiting and isolating resource usage (CPU, memory, disk I/O) of process groups.
2. **`cgroups v2`**: Modern unified single-hierarchy Linux control group architecture providing pressure stall metrics.
3. **`namespace`**: Linux kernel feature partitioning global system resources (PID, Mount, Net, IPC, UTS, User) into isolated workspaces.
4. **`OOMKilled` (Exit Code 137)**: Termination signal sent by Linux kernel when a container exceeds its memory limit ($128 + 9 = 137$).
5. **`MaxRAMPercentage`**: JVM flag dynamically calculating maximum heap size as a percentage of container cgroup memory.
6. **`UseContainerSupport`**: HotSpot flag enabling automatic detection of cgroup CPU and memory limits.
7. **`Metaspace`**: Native memory area storing loaded class definitions, method metadata, and constant pools.
8. **`Direct Memory`**: Off-heap memory allocated via `ByteBuffer.allocateDirect()` used by Netty and NIO channels.
9. **`Layered JAR`**: Spring Boot packaging strategy separating dependencies from application code for Docker caching.
10. **`Distroless`**: Minimal container images containing only the application and runtime dependencies without package managers or shells.
11. **`Eclipse Temurin`**: Enterprise-ready, TCK-certified open-source JDK distribution from the Adoptium project.
12. **`GraalVM Native Image`**: Ahead-of-Time (AOT) compiler producing standalone native executables from Java bytecode.
13. **`CFS` (Completely Fair Scheduler)**: Linux kernel CPU scheduler enforcing quota allocations over fixed time periods.
14. **`CPU Throttling`**: Forced suspension of container threads when CFS quota is exhausted within a 100ms window.
15. **`Kubelet`**: Primary node agent registering node with apiserver and managing container runtimes (CRI).
16. **`CRI` (Container Runtime Interface)**: Kubernetes API enabling pluggable container runtimes like `containerd` and `CRI-O`.
17. **`EndpointSlice`**: Scalable Kubernetes resource tracking network endpoints and Pod IPs backing a Service.
18. **`kube-proxy`**: Network proxy managing iptables or IPVS packet filtering rules on each cluster node.
19. **`preStop` Hook**: Container lifecycle event executed synchronously before sending `SIGTERM`.
20. **`SIGTERM` (Signal 15)**: Standard Unix termination request allowing process to perform graceful shutdown.
21. **`SIGKILL` (Signal 9)**: Uncatchable kernel signal terminating a process immediately.
22. **`terminationGracePeriodSeconds`**: Time Kubernetes waits between `SIGTERM` and `SIGKILL` (default: 30s).
23. **`server.shutdown=graceful`**: Spring Boot configuration allowing active HTTP requests to complete before exiting.
24. **`Liveness Probe`**: Health check determining if container process requires a restart.
25. **`Readiness Probe`**: Health check determining if Pod IP should receive traffic from Service endpoints.
26. **`Startup Probe`**: Health check pausing liveness/readiness evaluations during slow application boot.
27. **`Actuator Probes`**: Spring Boot endpoints (`/actuator/health/liveness`, `/actuator/health/readiness`).
28. **`HPA` (Horizontal Pod Autoscaler)**: Kubernetes controller scaling pod replicas based on CPU, memory, or custom metrics.
29. **`PDB` (Pod Disruption Budget)**: Policy ensuring minimum number of available pods during voluntary disruptions (node drains).
30. **`PodAntiAffinity`**: Scheduling rule preventing identical pod replicas from sharing the same physical node.
31. **`TopologySpreadConstraints`**: Rule distributing pods evenly across failure domains (regions, zones, racks).
32. **`QoS` (Quality of Service)**: Kubernetes pod classification (`Guaranteed`, `Burstable`, `BestEffort`) dictating eviction priority.
33. **`ConfigMap`**: Kubernetes object storing non-confidential configuration key-value pairs.
34. **`Secret`**: Kubernetes object storing sensitive data (passwords, tokens, TLS keys) encoded in Base64.
35. **`EmptyDir`**: Ephemeral volume created when a Pod is assigned to a node and deleted when the Pod leaves.
36. **`ReadOnlyRootFilesystem`**: Security context preventing containers from writing to root directory.
37. **`Drop Capabilities`**: Security practice dropping Linux root capabilities (`ALL`) to prevent privilege escalation.
38. **`Class Data Sharing` (CDS)**: JVM feature dumping loaded classes to an archive file for faster startup and memory sharing.
39. **`jlink`**: Tool generating custom minimal runtime images containing only the modules required by an application.
40. **`ForkJoinPool`**: JVM work-stealing thread pool backing parallel streams and asynchronous computations.

---

# Track 11: 30-Point Enterprise Production Kubernetes Audit Checklist

```
┌────────────────────────────────────────────────────────────────────────────────────────┐
│             JAVA & SPRING BOOT KUBERNETES ENTERPRISE PRODUCTION AUDIT CHECKLIST        │
├────────────────────────────────────────────────────────────────────────────────────────┤
│ [ ] 01. Container runs as non-root (USER 10001:10001 / runAsNonRoot: true).           │
│ [ ] 02. JVM memory is configured via -XX:MaxRAMPercentage=75.0, not hardcoded -Xmx.   │
│ [ ] 03. Both resources.requests and resources.limits are explicitly declared.          │
│ [ ] 04. Multi-stage Dockerfile uses Spring Boot Layered JAR extraction.                │
│ [ ] 05. Base image is pinned to specific digest or immutable tag (never :latest).      │
│ [ ] 06. preStop hook includes sleep 15 to prevent 502 Bad Gateway race conditions.     │
│ [ ] 07. server.shutdown=graceful and spring.lifecycle.timeout-per-shutdown-phase set.  │
│ [ ] 08. terminationGracePeriodSeconds is greater than (preStop sleep + shutdown wait). │
│ [ ] 09. Liveness probe checks /actuator/health/liveness (zero external dependencies).  │
│ [ ] 10. Readiness probe checks /actuator/health/readiness (dependency validation).     │
│ [ ] 11. Startup probe is configured for slow-starting applications (> 15s boot).       │
│ [ ] 12. Actuator management port is isolated from public HTTP traffic.                 │
│ [ ] 13. JVM DNS TTL is set to 30s (-Dnetworkaddress.cache.ttl=30).                     │
│ [ ] 14. -XX:+ExitOnOutOfMemoryError is enabled to kill deadlocked JVMs cleanly.        │
│ [ ] 15. PodAntiAffinity is configured to spread replicas across physical nodes/zones.  │
│ [ ] 16. PodDisruptionBudget (PDB) guarantees minimum available replicas during drains. │
│ [ ] 17. HorizontalPodAutoscaler (HPA) stabilization windows prevent scale-down thrashing.│
│ [ ] 18. Database connection pool max size is calibrated against HPA maxReplicas.       │
│ [ ] 19. readOnlyRootFilesystem is enabled with an emptyDir volume mounted at /tmp.     │
│ [ ] 20. Linux security capabilities are dropped (securityContext.capabilities.drop: ALL).│
│ [ ] 21. Sensitive credentials are mounted from Kubernetes Secrets, never in plain env. │
│ [ ] 22. Prometheus metric scraping annotations and port are configured correctly.     │
│ [ ] 23. RollingUpdate strategy specifies maxUnavailable: 0 for zero-downtime updates.  │
│ [ ] 24. Image vulnerability scanning (Trivy / Snyk) reports 0 Critical and High CVEs.  │
│ [ ] 25. Java GC is set to G1GC (-XX:+UseG1GC) or ZGC for large heaps (> 4GB).          │
│ [ ] 26. Heap dumps on OOM are directed to an emptyDir or PVC volume if required.       │
│ [ ] 27. Spring Cloud Kubernetes fabric8 client libraries are avoided for plain mounts. │
│ [ ] 28. Ingress annotations configure appropriate proxy-read and proxy-connect timeouts│
│ [ ] 29. Log output formats JSON to stdout for aggregation (FluentBit / Datadog).       │
│ [ ] 30. Virtual Threads (Java 21+) are validated against blocking synchronized blocks. │
└────────────────────────────────────────────────────────────────────────────────────────┘
```
