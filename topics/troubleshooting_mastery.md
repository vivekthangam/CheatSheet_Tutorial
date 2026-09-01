[Back to Home](../README.md) | [Tech Glossary](glossary.md) | [Interview Prep](interview_prep.md) | [Observability Guide](observability_splunk_mastery.md)

# 🛠️ Universal Systems Troubleshooting & Error Forensics Guide

A master forensic manual for diagnosing and resolving failures across Docker, Kubernetes, JVM/Spring Boot, Databases, and Distributed Networks, featuring comprehensive **Scenario-to-Command decision playbooks**.

---

## 📑 Table of Contents
1. [🧭 1. The Universal Error Triage Flowchart](#-1-the-universal-error-triage-flowchart)
2. [🐳 2. Docker Container Crash & Error Diagnostics](#-2-docker-container-crash--error-diagnostics)
   - [2.1 Container Exit Codes Cheat Sheet](#21-container-exit-codes-cheat-sheet)
   - [2.2 Docker Diagnostic Command Toolbox](#22-docker-diagnostic-command-toolbox)
   - [2.3 🎯 Docker Scenario-Based Command Playbook ("What Scenario → What Command?")](#23--docker-scenario-based-command-playbook-what-scenario--what-command)
3. [☸️ 3. Kubernetes Pod & Node Failure Master Grid](#️-3-kubernetes-pod--node-failure-master-grid)
   - [3.1 Resolving DNS Failures in Kubernetes (CoreDNS)](#31-resolving-dns-failures-in-kubernetes-coredns)
   - [3.2 🎯 Kubernetes Scenario-Based Command Playbook ("What Scenario → What Command?")](#32--kubernetes-scenario-based-command-playbook-what-scenario--what-command)
4. [☕ 4. JVM, Memory & Spring Boot Error Forensics](#-4-jvm-memory--spring-boot-error-forensics)
5. [💾 5. Database, Connection Pool & Lock Contention Diagnostics](#-5-database-connection-pool--lock-contention-diagnostics)
6. [🌐 6. Network, DNS & Linux Kernel Forensics](#-6-network-dns--linux-kernel-forensics)
7. [🎓 7. Senior Troubleshooting Interview Preparation Q&A](#-7-senior-troubleshooting-interview-preparation-qa)
8. [🔄 8. Architectural Transferability: Where & How to Apply Elsewhere](#-8-architectural-transferability-where--how-to-apply-elsewhere)

---

## 🧭 1. The Universal Error Triage Flowchart

When an incident occurs, systematically isolate which layer is failing rather than guessing:

```
[ Client Request ] ──> [ 1. DNS / CDN ] ──> [ 2. Ingress / Load Balancer ]
                                                         │
                                                         ▼
                                                [ 3. Kubernetes Service / Pod ]
                                                         │
                                                         ▼
                                                [ 4. Container / OS Runtime ]
                                                         │
                                                         ▼
                                                [ 5. Application / JVM Engine ]
                                                         │
                                                         ▼
                                                [ 6. Database / Microservice Mesh ]
```

---

## 🐳 2. Docker Container Crash & Error Diagnostics

### 2.1 Container Exit Codes Cheat Sheet
| Exit Code | Meaning | Root Cause | Diagnostic & Fix Command |
| :--- | :--- | :--- | :--- |
| **`0`** | Clean Exit | Container finished its foreground task. | Normal for batch Jobs. If a web server exits with 0, foreground process daemonized. |
| **`1`** | Application Error | Uncaught exception in code or missing configuration file. | `docker logs <container_id> --tail 100` |
| **`125`** | Docker CLI Error | `docker run` failed (e.g. invalid flag or syntax). | Check `docker run` arguments and syntax. |
| **`126`** | Permission Denied | Container command cannot be invoked (missing `+x` executable permission). | Add `RUN chmod +x /entrypoint.sh` in Dockerfile. |
| **`127`** | Command Not Found | Executable specified in `ENTRYPOINT` or `CMD` does not exist. | Check binary path inside base image (`/bin/sh` vs `/bin/bash`). |
| **`137`** | **SIGKILL / OOMKilled** | Linux kernel killed container because memory exceeded container limit. | `docker inspect <container> | grep OOMKilled`<br>Increase `-m 2g` or tune JVM heap `-XX:MaxRAMPercentage=75.0`. |
| **`139`** | **Segmentation Fault** | Native C/C++ library memory corruption (JNI, SQLite, CGo). | Inspect core dump with `gdb` or update native library. |
| **`143`** | **SIGTERM** | Container received graceful termination signal from Docker / K8s. | Normal during container shutdown or rolling restart. |

### 2.2 Docker Diagnostic Command Toolbox
```bash
# 1. Inspect why a container stopped
docker inspect <container_name> --format='ExitCode: {{.State.ExitCode}}, OOMKilled: {{.State.OOMKilled}}, Error: {{.State.Error}}'

# 2. View live CPU, Memory, Network, and Block I/O usage
docker stats --no-stream

# 3. Inspect container logs with timestamps
docker logs -f --tail=200 --timestamps <container_name>

# 4. Clean up disk space from dangling images and overlay2 cache
docker system prune -a --volumes --force
```

---

### 2.3 🎯 Docker Scenario-Based Command Playbook ("What Scenario → What Command?")

| Scenario ID | Problem Scenario Description | What Command Should I Use? | Command Breakdown & Operational Logic |
| :--- | :--- | :--- | :--- |
| **DOC-01** | Container crashes immediately on boot; cannot execute `docker exec`. | `docker logs --tail 100 <cid>`<br>`docker inspect <cid> --format '{{.State.ExitCode}} {{.State.Error}}'` | Reads the stdout/stderr prior to crash and extracts the exact kernel exit code and error string. |
| **DOC-02** | Container is completely frozen, hanging on I/O, and ignores `docker stop`. | `docker kill -s SIGKILL <cid>` | Bypasses graceful 10s SIGTERM timeout and sends an uncatchable SIGKILL directly to the PID namespace. |
| **DOC-03** | Need to launch and debug a crashing image without running its faulty entrypoint. | `docker run --rm -it --entrypoint /bin/sh <image_name>` | Overrides `ENTRYPOINT` and `CMD` with an interactive shell for manual filesystem inspection. |
| **DOC-04** | Host storage is 100% full (`no space left on device` error during build or pull). | `docker system df`<br>`docker system prune -a --volumes --force` | `df` displays space used by images/containers/buildcache; `prune -a --volumes` cleans all stopped containers, dead volumes, and unused layers. |
| **DOC-05** | Need to extract a `.hprof` heap dump or log file from a stopped or dead container. | `docker cp <cid>:/app/dumps/heap.hprof ./heap.hprof` | Copies files directly between host and container filesystem even when the container is in `Exited` status. |
| **DOC-06** | Need to inspect what files were created, modified, or deleted inside the writable container layer. | `docker diff <cid>` | Displays modified (`C`), added (`A`), and deleted (`D`) file paths compared to the base image. |
| **DOC-07** | Port conflict error: `driver failed programming external connectivity: bind: address already in use`. | `docker ps --filter "publish=<port>"`<br>`netstat -tulpn \| grep :<port>` | Finds which container or host daemon is already bound to that host port. |
| **DOC-08** | Two containers on the same host cannot communicate by name or IP. | `docker network create app-net`<br>`docker network connect app-net <cid>` | Default `bridge` network does not support DNS resolution; custom user-defined bridge network enables automatic DNS naming. |
| **DOC-09** | Live container is consuming 100% CPU; need to identify the rogue internal process. | `docker top <cid> aux` | Lists the host PIDs and resource consumption of processes running inside the container's PID namespace. |
| **DOC-10** | Need to dynamically increase CPU or memory limits on a live container without restarting it. | `docker update --memory 4g --cpus 2.0 <cid>` | Live-tunes cgroups memory and CPU shares on the running container on-the-fly. |
| **DOC-11** | Need to capture network packets directly inside a container with no networking tools installed. | `docker run --rm --net=container:<cid> nicolaka/netshoot tcpdump -i eth0 -nn -vv` | Joins the target container's network namespace (`--net=container:`) to run diagnostic packet sniffers. |
| **DOC-12** | Docker build picking up stale dependencies or cached layers incorrectly. | `docker build --no-cache --pull --progress=plain -t app:v1 .` | Bypasses local layer cache, pulls latest base image, and outputs unbuffered plaintext build logs. |
| **DOC-13** | Need to inspect Docker image layer history to identify why the image is bloated (e.g. 2 GB). | `docker history --human --no-trunc <image_name>` | Displays each Dockerfile build instruction, resulting layer size, and creation timestamp. |
| **DOC-14** | Security audit: scan container image for known CVE vulnerabilities before deploying. | `docker scout cves <image_name>` | Generates a Software Bill of Materials (SBOM) and flags Critical/High severity vulnerabilities. |
| **DOC-15** | Need to monitor live streaming resource consumption across all containers in real time. | `docker stats --format "table {{.Name}}\t{{.CPUPerc}}\t{{.MemUsage}}\t{{.NetIO}}\t{{.BlockIO}}"` | Generates a live top-like terminal dashboard showing CPU%, RAM usage, Network I/O, and Disk I/O. |

---

## ☸️ 3. Kubernetes Pod & Node Failure Master Grid

| Pod State | Root Cause | Exact Forensic Commands & Fix |
| :--- | :--- | :--- |
| **`CrashLoopBackOff`** | Application starts and crashes repeatedly (e.g. Spring Boot failed DB connection). | `kubectl logs <pod> --previous`<br>`kubectl describe pod <pod>` |
| **`ImagePullBackOff`** / `ErrImagePull` | Wrong image tag, image does not exist, or missing image pull secret for private registry. | `kubectl describe pod <pod>` $\rightarrow$ check `Events:`<br>Fix: Verify registry credentials in `imagePullSecrets:`. |
| **`CreateContainerConfigError`** | Referenced `ConfigMap` or `Secret` does not exist or has a misspelled key. | `kubectl describe pod <pod>` $\rightarrow$ check missing ConfigMap/Secret name. |
| **`Pending`** | No worker node has enough CPU/Memory requests, or node has un-tolerated taints. | `kubectl describe pod <pod>` $\rightarrow$ check `0/10 nodes are available: insufficient memory`.<br>Fix: Lower `resources.requests` or add worker nodes. |
| **`OOMKilled (Exit 137)`** | Container memory exceeded `resources.limits.memory`. | `kubectl get pod <pod> -o jsonpath='{.status.containerStatuses[0].lastState.terminated.reason}'`<br>Fix: Increase memory limit or tune `-XX:MaxRAMPercentage`. |
| **`Evicted`** | Worker node ran out of disk space (`DiskPressure`) or memory (`MemoryPressure`). | `kubectl describe node <node-name>` $\rightarrow$ check `Conditions`.<br>Fix: Prune unused images or expand node storage. |
| **`NodeNotReady`** | `kubelet` stopped on worker node, or network partition between master and worker. | `ssh <node>` $\rightarrow$ `systemctl status kubelet`<br>`journalctl -u kubelet -e` |

### 3.1 Resolving DNS Failures in Kubernetes (`CoreDNS`)
```bash
# 1. Test DNS resolution from inside a debug pod
kubectl run dnsutils --image=tianon/true --restart=Never
kubectl exec -i -t dnsutils -- nslookup kubernetes.default

# 2. Check CoreDNS pod logs for timeouts
kubectl logs -n kube-system -l k8s-app=kube-dns --tail=100
```

---

### 3.2 🎯 Kubernetes Scenario-Based Command Playbook ("What Scenario → What Command?")

| Scenario ID | Problem Scenario Description | What Command Should I Use? | Command Breakdown & Operational Logic |
| :--- | :--- | :--- | :--- |
| **K8S-01** | Pod is stuck in `Pending` state and won't get scheduled onto any worker node. | `kubectl describe pod <pod_name> \| grep -A 10 "Events:"` | Checks scheduler events (e.g. `0/24 nodes available: Insufficient cpu`, `node(s) had untolerated taint`). |
| **K8S-02** | Pod is stuck in `Terminating` state due to unmounting volume or hanging preStop hook. | `kubectl delete pod <pod_name> --grace-period=0 --force -n <ns>` | Forces immediate deletion from etcd, bypassing kubelet graceful unmount confirmation. |
| **K8S-03** | Pod crashed 30 seconds ago; current logs are empty because container restarted. | `kubectl logs <pod_name> --previous -c <container_name> -n <ns>` | Retrieves logs from the previous dead container instance prior to the latest restart. |
| **K8S-04** | Container in `ImagePullBackOff`; need to inspect exact registry error. | `kubectl get events --field-selector involvedObject.name=<pod_name> -n <ns>` | Pinpoints exact error (`ImagePullBackOff: rpc error: code = NotFound`, `401 Unauthorized`). |
| **K8S-05** | Production release causing HTTP 500 errors; need immediate emergency rollback. | `kubectl rollout undo deployment/<deployment_name> -n <ns>` | Rolls back deployment immediately to the previous ReplicaSet revision. |
| **K8S-06** | Need to check deployment revision history and annotations before rolling back. | `kubectl rollout history deployment/<deployment_name> -n <ns>` | Displays list of revision numbers and change-cause annotations. |
| **K8S-07** | Need to restart all pods in a deployment gracefully without changing configuration. | `kubectl rollout restart deployment/<deployment_name> -n <ns>` | Triggers a rolling update restart by injecting a restart timestamp annotation into pod template. |
| **K8S-08** | Ingress or Service returning `503 Service Unavailable` / `502 Bad Gateway`. | `kubectl get endpoints <service_name> -n <ns>` | Checks if Service has active target Pod IPs. If endpoints are `<none>`, readiness probe is failing or labels mismatch. |
| **K8S-09** | Need to test an internal cluster Service or Pod API directly from your local laptop. | `kubectl port-forward svc/<service_name> 8080:<target_port> -n <ns>` | Establishes a secure encrypted localhost tunnel forwarding local port 8080 to cluster Service port. |
| **K8S-10** | Debugging a distroless or scratch container that has no `sh`, `bash`, or `curl`. | `kubectl debug -it <pod_name> --image=busybox:1.36 --target=<container_name> -n <ns>` | Attaches an ephemeral debug container sharing the process namespace (`--target`) of the running container. |
| **K8S-11** | Need to drain a worker node for maintenance/OS upgrade without causing application outage. | `kubectl cordon <node_name>`<br>`kubectl drain <node_name> --ignore-daemonsets --delete-emptydir-data --force` | Marks node unschedulable (`cordon`), then safely evicts all pods respecting PodDisruptionBudgets (`drain`). |
| **K8S-12** | Node maintenance complete; need to allow pods to be scheduled on the node again. | `kubectl uncordon <node_name>` | Removes the `SchedulingDisabled` taint, allowing scheduler to place pods on node again. |
| **K8S-13** | Cluster running slow; find the Top 10 most CPU-consuming and Memory-consuming Pods. | `kubectl top pods -A --sort-by=cpu \| head -n 11`<br>`kubectl top pods -A --sort-by=memory \| head -n 11` | Queries metrics-server for real-time CPU (millicores) and RAM (MiB) utilization per Pod. |
| **K8S-14** | Check if a specific ServiceAccount has permission to create or delete a resource (RBAC check). | `kubectl auth can-i delete pods --as=system:serviceaccount:<ns>:<sa_name> -n <ns>` | Simulates Kubernetes API authorization engine to verify RBAC Role/ClusterRole bindings. |
| **K8S-15** | Need to decode a base64 Kubernetes Secret value directly in the terminal without manual decoding. | `kubectl get secret <secret_name> -n <ns> -o jsonpath='{.data.<key>}' \| base64 --decode` | Extracts the raw base64 string directly from JSON and streams it into the base64 decoding utility. |
| **K8S-16** | Intermittent NetworkPolicy blocking traffic between frontend and backend pods. | `kubectl run tmp-netshoot --rm -i --tty --image=nicolaka/netshoot -n <ns> -- nc -zv <service_name> <port>` | Spins up an ephemeral networking diagnostic container to test DNS and TCP port connectivity. |
| **K8S-17** | Need to stream logs from ALL pods in a deployment simultaneously with colored prefixes. | `kubectl logs -f -l app=<app_label> --max-log-requests=10 -n <ns>` | Streams multi-pod aggregated stdout/stderr across all replica pods matching the label selector. |
| **K8S-18** | Need to copy application configuration or debug core dumps between Pod and local laptop. | `kubectl cp <ns>/<pod_name>:/app/logs/app.log ./local_app.log` | Uses tar stream over apiserver exec subresource to transfer files in either direction. |
| **K8S-19** | Extract all cluster-wide Warning and Error events that occurred in the last 15 minutes. | `kubectl get events -A --field-selector type=Warning --sort-by='.lastTimestamp'` | Lists all cluster-wide failure events (OOM, FailedScheduling, FailedMount, Unhealthy) chronologically. |
| **K8S-20** | Worker node is in `NotReady` state; need to check why kubelet is failing. | `kubectl describe node <node_name> \| grep -A 10 "Conditions:"` | Checks whether node is experiencing `DiskPressure`, `MemoryPressure`, `PIDPressure`, or NetworkUnavailable. |

---

## ☕ 4. JVM, Memory & Spring Boot Error Forensics

### 4.1 Java Memory Errors & Solutions
| Error | Subsystem | Root Cause | Architectural Fix |
| :--- | :--- | :--- | :--- |
| **`java.lang.OutOfMemoryError: Java heap space`** | Heap (`-Xmx`) | Memory leak (static collections holding references, unclosed streams, massive query result sets). | Analyze `.hprof` heap dump using Eclipse MAT; fix unbounded collections. |
| **`java.lang.OutOfMemoryError: Metaspace`** | Metaspace | ClassLoader leak from dynamic proxy generation (CGLIB, Spring AOP, reflection). | Increase `-XX:MaxMetaspaceSize=512m` or eliminate ClassLoader leaks. |
| **`java.lang.OutOfMemoryError: Direct buffer memory`** | Off-Heap Direct Buffer | Netty / NIO off-heap buffers allocated via `ByteBuffer.allocateDirect()` not released. | Tune `-XX:MaxDirectMemorySize=1g` and audit Netty buffer reference counts (`ReferenceCountUtil.release()`). |
| **`java.lang.OutOfMemoryError: unable to create native thread`** | OS Threads | Process hit OS max user processes limit (`ulimit -u`) or physical RAM exhausted. | Check `ulimit -u`; replace unbounded thread pools with fixed pools or Java 21 **Virtual Threads**. |

### 4.2 Spring Boot Thread Pool Starvation
```
org.springframework.core.task.TaskRejectedException: Executor [java.util.concurrent.ThreadPoolExecutor@7b2...] did not accept task
```
- **Root Cause:** Thread pool queue is full and `maxPoolSize` threads are all busy.
- **Fix:** Configure custom `ThreadPoolTaskExecutor` with a bounded queue and **CallerRunsPolicy**:
```java
@Bean(name = "customExecutor")
public Executor customExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(16);
    executor.setMaxPoolSize(64);
    executor.setQueueCapacity(500);
    executor.setThreadNamePrefix("AsyncWorker-");
    executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy()); // Backpressure!
    executor.initialize();
    return executor;
}
```

---

## 💾 5. Database, Connection Pool & Lock Contention Diagnostics

### 5.1 HikariCP Connection Pool Exhaustion
```
java.sql.SQLTransientConnectionException: HikariPool-1 - Connection is not available, request timed out after 30000ms.
```
- **Diagnostic Step 1:** Enable connection leak detection: `spring.datasource.hikari.leak-detection-threshold: 2000` (prints stack trace of code holding connection $>2\text{s}$).
- **Diagnostic Step 2:** Check for long-running uncommitted transactions:
```sql
-- PostgreSQL: Find active queries running longer than 10 seconds
SELECT pid, now() - query_start AS duration, query, state
FROM pg_stat_activity
WHERE state != 'idle' AND (now() - query_start) > interval '10 seconds';

-- Kill blocking query
SELECT pg_cancel_backend(<pid>);
```

### 5.2 Database Deadlock Diagnostics
```
org.postgresql.util.PSQLException: ERROR: deadlock detected
Detail: Process 18420 waits for ShareLock on transaction 98412; blocked by process 18421.
```
- **Root Cause:** Two transactions updating the same rows in reverse order.
- **Fix (Lock Ordering):** Ensure all application transactions always update tables and rows in the **exact same deterministic order** (e.g. `ORDER BY id ASC`).

---

## 🌐 6. Network, DNS & Linux Kernel Forensics

### 6.1 Network & Socket Diagnostic Cheat Sheet
```bash
# 1. Check if port 8080 is listening and which process owns it
ss -tulpn | grep :8080

# 2. Check established socket connection counts per state
netstat -an | awk '/tcp/ {print $6}' | sort | uniq -c

# 3. Test TCP connection latency and handshake to remote service
nc -zv -w 3 database.internal.corp 5432

# 4. Trace HTTP request timings (DNS, Connect, TLS, TTFB, Total)
curl -w "\nDNS: %{time_namelookup}s\nConnect: %{time_connect}s\nTLS: %{time_appconnect}s\nTTFB: %{time_starttransfer}s\nTotal: %{time_total}s\n" -o /dev/null -s https://api.stripe.com/healthcheck
```

---

## 🎓 7. Senior Troubleshooting Interview Preparation Q&A

### 📌 Core Conceptual Interview Questions

#### Q1: A web service responds with HTTP 504 Gateway Timeout intermittently under load. How do you isolate whether the bottleneck is NGINX, the Application, or the Database?
> **Answer & Explanation:**
> 1. **Check NGINX `upstream_response_time` vs `request_time`:**
>    - If `upstream_response_time` is high ($>60\text{s}$), the backend application is taking too long to respond.
>    - If `upstream_response_time` is fast ($50\text{ms}$) but `request_time` is slow ($60\text{s}$), the client has a slow upload or NGINX is waiting on connection queue capacity.
> 2. **Check Application Thread Dumps:** Check if threads are stuck in `BLOCKED` state waiting on DB connections (`HikariCP getConnection()`).
> 3. **Check Database Active Queries:** Query `pg_stat_activity` / `v$session` to identify if slow sequential table scans or row locks are holding transactions open.

---

### 🚨 Real-World Scenario-Based Interview Questions

#### Scenario Q1: High Disk I/O Wait (iowait) Freezing Linux Node
> **Interviewer Question:** *"A worker node CPU shows 80% `wa` (I/O Wait). Application response times degrade from 20ms to 5000ms. How do you identify which process and files are saturating the disk?"*
>
> **Senior Architect Answer:**
> 1. **Identify Process Causing Heavy Disk Writes:**
>    ```bash
>    iotop -o -b -n 3
>    ```
>    Look for the process with the highest `DISK WRITE` bandwidth.
> 2. **Inspect Open File Descriptors of That Process:**
>    ```bash
>    lsof -p <PID> | grep REG
>    ```
> 3. **Check Filesystem Space & Inode Exhaustion:**
>    ```bash
>    df -h   # Checks disk space
>    df -i   # Checks inode count (if inodes hit 100%, writes fail even if disk has free GB)
>    ```

---

## 🔄 8. Architectural Transferability: Where & How to Apply Elsewhere

1. **Enterprise Incident Commander Playbooks:** Standardizing triage runbooks across SRE teams to reduce MTTR (Mean Time to Resolution) from 45 minutes to $<5$ minutes.
2. **Automated Self-Healing Infrastructure:** Writing Kubernetes custom operators to detect stuck thread dumps or OOM loops and automatically execute diagnostic snapshots before restarting containers.
3. **CI/CD Flaky Test Debugging:** Using exit code forensics and socket inspection to diagnose parallel test harness port conflicts in GitHub Actions.

---

[⬆️ Back to Top](#️-universal-systems-troubleshooting--error-forensics-guide)
