# 📖 The Architect's Technical Glossary (2026 Edition)

A high-fidelity reference guide for the modern Distributed Systems, Cloud Native, and AI Engineering domains.

---

## 📑 Table of Contents
1. [🧬 AI & Large Language Models (LLMs)](#-ai--large-language-models-llms)
2. [☸️ Kubernetes & Cloud Native](#️-kubernetes--cloud-native)
3. [☕ Java & JVM Ecosystem](#-java--jvm-ecosystem)
4. [🐙 Git & Version Control](#-git--version-control)

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

## 🐙 Git & Version Control

| Term | Definition | Context |
| :--- | :--- | :--- |
| **Rebase** | Re-writing commit history to keep it linear. | Clean development logs. |
| **Cherry-Pick** | Applying a single commit from one branch to another. | Hotfixing v1 from v2. |
| **Detached Head**| When the HEAD is pointing to a commit, not a branch. | Investigating older code versions. |
| **Force-Push** | Overwriting a remote branch (Danger!). | Use with `--lease` for safety. |

---
