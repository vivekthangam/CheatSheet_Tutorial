# ☸️ Kubernetes Mastery: The Architect's Zero-to-Hero Guide

Master the orchestration logic, internal mechanics, and "Search & Destroy" troubleshooting tactics used by senior architects in production-grade Fintech environments.

---

## 📑 Table of Contents
1. [🚀 The 4-Phase Strategic Roadmap](#the-4-phase-strategic-roadmap)
2. [⚙️ Core Features & Internal Mechanics](#core-features--internal-mechanics)
3. [🧪 Scenario-Based Troubleshooting (Phase 1-4)](#scenario-based-troubleshooting)
4. [💾 The Storage Stack (PV, PVC, StatefulSets)](#the-storage-stack)
5. [🛡️ Security Hardening (RBAC & Network Policies)](#security-hardening)
6. [🛠️ The Ultimate Kubernetes Cheat Sheet](#the-ultimate-kubernetes-cheat-sheet)

---

## 🚀 The 4-Phase Strategic Roadmap

| Phase | Title | Focus Area | Why & When? |
| :--- | :--- | :--- | :--- |
| **Phase 1** | **Foundations** | Control Plane, Pods, Namespaces, Nodes. | Use when migrating a single container to a managed environment. |
| **Phase 2** | **State & Traffic** | Deployments, Services, Ingress, Volumes. | Use when your app needs a DB and a public domain name. |
| **Phase 3** | **Hardening** | RBAC, Network Policies, Resource Quotas. | Use before launching to Production to prevent hacks. |
| **Phase 4** | **Ecosystem** | Helm, GitOps (ArgoCD), Service Mesh (Istio). | Use when managing 50+ microservices at scale. |

---

## ⚙️ Core Features & Internal Mechanics

### 1. The Scheduler (kube-scheduler)
*   **The Problem:** Manually deciding which node has enough CPU/RAM for a workload is impossible at scale.
*   **Under the Hood:** Runs two cycles: **Filtering** (removes nodes that don't meet constraints like Taints or Resource limits) and **Scoring** (ranks remaining nodes based on soft preferences like spreading pods for High Availability).
*   **Real-World:** Using **PodAntiAffinity** to ensure two instances of a "Payment API" never land on the same physical rack.

### 2. Services & kube-proxy
*   **The Problem:** Pods are ephemeral; their IPs change. You need a stable endpoint to talk to a set of Pods.
*   **Under the Hood:** `kube-proxy` manages the Virtual IP (VIP) of the Service. In modern clusters, it uses **IPVS** or **eBPF** to intercept traffic at the kernel level and redirect it to a healthy Pod IP.
*   **Real-World:** A React frontend hits `http://backend-service`, which DNS resolves to a stable ClusterIP.

### 3. Controller Manager (The Harmony Maker)
*   **The Problem:** Maintaining "Desired State." If you want 3 replicas and one dies, something must fix it.
*   **Under the Hood:** Runs an infinite **Control Loop**. It queries `etcd` for the "Desired State" and queries Nodes for "Actual State." If `Desired != Actual`, it instructs the API server to bridge the gap.

---

## 🧪 Scenario-Based Troubleshooting

### Phase 1: Foundational Failures (The "Why won't it start?" Phase)

> [!CAUTION]
> **Scenario: The "Pending" Payment Service**
> *   **The Problem:** Pods stay in `Pending` state forever.
> *   **Root Cause:** **Resource Starvation.** You requested more CPU/RAM than any node has available.
> *   **The Fix:** Lower `requests` or add larger nodes.
> ```bash
> kubectl describe pod <name> # Look for: "Insufficient cpu"
> ```

> [!IMPORTANT]
> **Scenario: The "ImagePullBackOff" Mystery**
> *   **The Problem:** K8s can't pull your image from a private registry.
> *   **Root Cause:** Missing `imagePullSecrets`.
> *   **The Fix:** Create a docker-registry secret and link it in the YAML.
> ```bash
> kubectl create secret docker-registry regcred --docker-server=<url> --docker-username=<user> --docker-password=<pass>
> ```

### Phase 2: Traffic Failures (The "Why can't they see it?" Phase)

> [!WARNING]
> **Scenario: The "Empty" Load Balancer (502 Gateway)**
> *   **The Problem:** Traffic reaches the Ingress but "disappears" before hitting the app.
> *   **Root Cause:** **Label Selector Mismatch.** Service is looking for `app: pay`, but pods are labeled `app: fintech-api`.
> *   **The Fix:** Ensure Service `selector` matches Pod `labels`.
> ```bash
> kubectl get endpoints <svc> # If <none>, labels are wrong.
> ```

### Phase 3: State & Storage Failures

> [!TIP]
> **Scenario: The "Stuck" Volume (Multi-Attach Error)**
> *   **The Problem:** Database Pod stuck in `ContainerCreating` on a new node.
> *   **Root Cause:** **Volume Locking.** RWO disks (AWS EBS) can only be on one node. If the old node hasn't released it, the new one is blocked.
> *   **The Fix:** Force-delete the old Pod.
> ```bash
> kubectl delete pod <old-pod> --grace-period=0 --force
> ```

---

## 💾 The Storage Stack

### PV vs. PVC: The "Vending Machine" Analogy
1.  **Persistent Volume (PV):** The actual "Hard Drive" provided by the admin or cloud.
2.  **Persistent Volume Claim (PVC):** The developer's "Voucher" for storage (e.g., "I need 10GB of SSD").
3.  **StorageClass:** The blueprint that tells K8s how to automatically create the disk when a PVC is made.

### StatefulSets: For "Pet" Applications
*   **Stable Names:** Pods are named `db-0`, `db-1`, `db-2`.
*   **Sticky Storage:** If `db-1` restarts on a new node, K8s re-attaches the *exact same* physical disk.
*   **Ordered Deployment:** `db-1` won't start until `db-0` is healthy.

---

## 🛡️ Security Hardening

### RBAC (Who can do what?)
*   **Role:** Permissions within a Namespace (e.g., "can read pods").
*   **ClusterRole:** Permissions across the entire Cluster (e.g., "can read nodes").
*   **RoleBinding:** Attaching a Role to a User or ServiceAccount.

### Network Policies (The Internal Firewall)
By default, all pods can talk to all pods. **Zero Trust** requires NetworkPolicies.
```yaml
# Only allow Backend pods to talk to the Database
spec:
  podSelector:
    matchLabels:
      app: postgres
  ingress:
  - from:
    - podSelector:
        matchLabels:
          role: backend
```

---

## 🛠️ The Ultimate Kubernetes Cheat Sheet

### Critical Debugging
| Action | Command |
| :--- | :--- |
| **All Events** | `kubectl get events -A --sort-by='.lastTimestamp'` |
| **Resource Usage** | `kubectl top nodes` / `kubectl top pods` |
| **Inside the Pod** | `kubectl exec -it <name> -- /bin/sh` |
| **Live Logs** | `kubectl logs -f <name> -c <container>` |
| **Check RBAC** | `kubectl auth can-i create pods` |

### Production Do's and Don'ts
*   ✅ **DO:** Use `requests` for scheduling and `limits` for safety.
*   ✅ **DO:** Use `readinessProbes` to prevent traffic hitting a starting app.
*   ❌ **DON'T:** Use the `:latest` tag for images. It breaks rollbacks.
*   ❌ **DON'T:** Run containers as `root`. Use `securityContext`.

---

[⬆️ Back to Top](#☸️-kubernetes-mastery-the-architects-zero-to-hero-guide)
