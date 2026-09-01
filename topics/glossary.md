[Back to Home](../README.md) | [Interview Prep Guide](interview_prep.md) | [Observability Guide](observability_splunk_mastery.md) | [Troubleshooting Guide](troubleshooting_mastery.md)

# 📖 The Architect's Technical Glossary (2026 Edition)

A high-fidelity reference guide for the modern Distributed Systems, Cloud Native, Observability, Chaos Engineering, and AI Engineering domains.

---

## 📑 Table of Contents
1. [🧬 AI & Large Language Models (LLMs)](#-ai--large-language-models-llms)
2. [☸️ Kubernetes & Cloud Native](#️-kubernetes--cloud-native)
3. [☕ Java & JVM Ecosystem](#-java--jvm-ecosystem)
4. [📊 Observability & Telemetry](#-observability--telemetry)
5. [🌪️ Chaos Engineering & Resilience](#️-chaos-engineering--resilience)
6. [🛠️ Systems Troubleshooting & Kernel Diagnostics](#️-systems-troubleshooting--kernel-diagnostics)
7. [🐙 Git & Version Control](#-git--version-control)

---

## 🧬 AI & Large Language Models (LLMs)

| Term | Definition | Context |
| :--- | :--- | :--- |
| **RAG** | Retrieval-Augmented Generation. | Enhancing an LLM by giving it access to private data. |
| **Fine-Tuning** | Re-training a model on a niche dataset. | Specialty tasks like "Advanced Java Coding." |
| **Hallucination** | When an AI generates factually incorrect data. | Mitigation includes Grounding and RAG. |
| **Vector DB** | A database designed for "Embeddings" (Pinecone, Chroma). | Semantic search for AI memory. |
| **Agentic AI** | AI that can take actions (tool-use) independently. | Antigravity is an Agentic AI. |

---

## ☸️ Kubernetes & Cloud Native

| Term | Definition | Context |
| :--- | :--- | :--- |
| **Control Plane** | The "Brain" of the cluster (API Server, etcd). | State management and scheduling. |
| **Data Plane** | The "Muscle" (Worker Nodes, kubelet). | Running the actual containers. |
| **CRD** | Custom Resource Definition. | Extending the K8s API with custom types (e.g., KafkaCluster). |
| **Service Mesh** | Traffic control layer (Istio, Linkerd). | mTLS, retries, and deep observability. |
| **Sidecar** | A helper container running in the same Pod. | Logging, Proxying, or Security. |

---

## ☕ Java & JVM Ecosystem

| Term | Definition | Context |
| :--- | :--- | :--- |
| **JIT Compiler** | Just-In-Time Compiler. | Converts "Hot" bytecode into native machine code. |
| **Metaspace** | Non-heap memory. | Stores Class metadata (blueprints). |
| **GC Root** | An active object (Thread, Static) that keeps others alive. | Investigating memory leaks. |
| **Stop-the-World** | When the JVM freezes to perform a Full GC. | Low-latency optimization. |
| **BOM** | Bill of Materials. | Standardizing versions across complex projects. |

---

## 📊 Observability & Telemetry

| Term | Definition | Context |
| :--- | :--- | :--- |
| **High Cardinality** | Unbounded unique label combinations in metrics. | Can crash Prometheus/Mimir memory if user IDs are used. |
| **RED Method** | Rate, Errors, Duration. | Golden signals for request-driven microservices. |
| **USE Method** | Utilization, Saturation, Errors. | Golden signals for hardware resources (CPU, RAM, Disk). |
| **W3C Trace Context** | Standardized `traceparent` header format. | Propagating distributed trace context across HTTP/gRPC. |
| **Tail Sampling** | Making trace retention decisions after request completes. | Persisting 100% of errors and slow requests while dropping 99% of fast 200 OK traces. |

---

## 🌪️ Chaos Engineering & Resilience

| Term | Definition | Context |
| :--- | :--- | :--- |
| **Blast Radius** | The maximum impact area of a chaos experiment. | Keeping experiments isolated to canary or single namespace. |
| **Steady State** | Baseline measurable normal behavior (e.g. 99.9% 200 OK). | Hypotheses test if steady state holds under chaos. |
| **Bulkhead** | Isolating resource pools (thread pools, connection pools). | Prevents slow 3rd-party APIs from crashing the entire app. |
| **Circuit Breaker** | Automatically failing fast when downstream is degraded. | Resilience4j state transitions (Closed -> Open -> Half-Open). |

---

## 🛠️ Systems Troubleshooting & Kernel Diagnostics

| Term | Definition | Context |
| :--- | :--- | :--- |
| **Exit Code 137** | Process terminated by SIGKILL (OOMKilled). | Container exceeded Linux cgroups memory limit. |
| **CrashLoopBackOff** | K8s pod repeatedly starting, failing, and restarting. | Diagnosed via `kubectl logs --previous` and `describe pod`. |
| **iowait (`wa`)** | CPU time spent waiting for outstanding disk I/O. | Diagnosed via `iotop` and `lsof`. |
| **File Descriptor Leak** | Unclosed sockets/files reaching process `ulimit -n`. | Diagnosed via `lsof -p <PID> | wc -l`. |

---

## 🐙 Git & Version Control

| Term | Definition | Context |
| :--- | :--- | :--- |
| **Rebase** | Re-writing commit history to keep it linear. | Clean development logs. |
| **Cherry-Pick** | Applying a single commit from one branch to another. | Hotfixing v1 from v2. |
| **Detached Head**| When the HEAD is pointing to a commit, not a branch. | Investigating older code versions. |
| **Force-Push** | Overwriting a remote branch (Danger!). | Use with `--lease` for safety. |

---

