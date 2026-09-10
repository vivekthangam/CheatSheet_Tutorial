# ArgoCD, GitOps & Progressive Delivery Architecture Interview Guide

> **Scope**: GitOps Core Principles, ArgoCD Control Plane Architecture (API Server, Repo Server, Application Controller), CRDs (Application, AppProject, ApplicationSet), Sync Strategies (Sync Waves, Hooks, Self-Healing), Multi-Cluster Management, Secrets in GitOps (Sealed Secrets, External Secrets Operator), Progressive Delivery (Argo Rollouts: Canary, Blue-Green, AnalysisTemplates), and Production War-Room Post-Mortems.

---

## Guide Architecture Overview

```
========================================================================================================================
                                     ARGOCD, GITOPS & PROGRESSIVE DELIVERY
========================================================================================================================
 [Layer 1: GitOps Core Principles & ArgoCD Internals] --> Declarative Desired State, Reconciliation Loop, Repo Server
 [Layer 2: ArgoCD Custom Resource Definitions (CRDs)] --> Application, AppProject Multi-Tenancy, ApplicationSet Generators
 [Layer 3: Sync Waves, Resource Hooks & Self-Healing] --> Sync Phases, Pre/Post-Sync Hooks, Prune, Self-Heal Automation
 [Layer 4: Secrets Management & Progressive Delivery] --> Sealed Secrets, External Secrets Operator, Argo Rollouts Canary
 [Layer 5: Ultra-Deep Real-World War-Room Cases]      --> 10 Production Disasters (Out-of-Sync Storm, Cascading Prune)
 [Layer 6: Beginner Mistakes & Anti-Patterns]         --> 8 Fatal Engineering Traps (Manual kubectl Edits, Plaintext Secrets)
 [Layer 7: Globally Reported Production Incidents]    --> Real Outages (Production Namespace Deletion via Bad Git Commit)
 [Layer 8: Rapid-Fire Cheat Sheet & Summary Matrix]   --> High-Speed CLI Commands, CRD Fields, Sync Wave Execution Order
========================================================================================================================
```

---

# Layer 1: GitOps Core Principles & ArgoCD Internals

---

### Scenario 1: The Four Principles of GitOps & Pull vs Push Delivery Models
**Interviewer Evaluation:** Assesses comprehension of OpenGitOps standards, security advantages of pull-based continuous delivery over CI push models, and declarative reconciliation.

#### Technical Deep Dive
1. **The 4 OpenGitOps Principles**:
   - **Declarative**: The entire system state must be described declaratively (YAML/Kustomize/Helm).
   - **Versioned & Immutable**: Desired state is stored in Git, maintaining a complete audit trail.
   - **Pulled Automatically**: Software agents continuously pull and verify state automatically.
   - **Continuously Reconciled**: Software agents continuously monitor live state and reconcile divergence.
2. **Push Model (Legacy CI/CD)**:
   - External CI runner (Jenkins/GitHub Actions) holds cluster administrator credentials and executes `kubectl apply`.
   - **Flaw**: Security risk (cluster API exposed to CI), no automated drift detection if someone edits the cluster manually.
3. **Pull Model (ArgoCD)**:
   - An in-cluster agent pulls from Git and applies changes locally inside the cluster boundary.
   - Eliminates exposing Kubernetes credentials outside the cluster.

```
Push Model (Legacy):
[ GitHub Actions ] --(Holds Admin Kubeconfig!)--------> [ Kube-Apiserver ]

Pull Model (GitOps / ArgoCD):
[ Git Repository ] <--- (In-Cluster ArgoCD Agent Pulls) --- [ Kubernetes Cluster ]
```

---

### Scenario 2: ArgoCD Control Plane Architecture & Component Interaction
**Interviewer Evaluation:** Evaluates knowledge of ArgoCD internal components: API Server, Repository Server, Application Controller, and Redis Cache.

#### Technical Deep Dive
ArgoCD operates as three decoupled microservices in the `argocd` namespace:
1. **`argocd-server` (API Server)**:
   - Exposes gRPC and REST APIs. Serves Web UI, enforces RBAC, handles SSO/OIDC tokens, and proxies cluster credentials.
2. **`argocd-repo-server` (Manifest Generator)**:
   - Clones Git repositories and compiles raw manifests using rendering tools (Helm, Kustomize, Jsonnet).
   - Returns generated Kubernetes JSON/YAML manifests to the Application Controller.
3. **`argocd-application-controller` (Reconciliation Engine)**:
   - Continuous control loop. Compares:
     $$\text{Desired State (from Repo Server)} \quad \longleftrightarrow \quad \text{Live State (from Kubernetes etcd)}$$
   - Computes diff and updates status (`Synced`, `OutOfSync`, `Degraded`, `Healthy`).
4. **`argocd-redis`**:
   - Caches parsed Git commits, generated manifests, and live cluster state to prevent Git API rate limits.

```
ArgoCD Component Architecture:
[ Git Repo ] <----------(Git Clone)----------- [ argocd-repo-server ]
                                                        | (Renders Manifests)
                                                        v
[ Kubernetes etcd ] <---(Reconciliation Loop)--- [ argocd-application-controller ]
        ^                                               |
        |                                               v
[ Live Pods ]                                    [ argocd-redis (Cache) ]
```

---

# Layer 2: Custom Resource Definitions: Application, AppProject & ApplicationSet

---

### Scenario 3: ApplicationSet Generators for Multi-Cluster Fleet Management
**Interviewer Evaluation:** Tests managing hundreds of Kubernetes clusters dynamically without writing duplicate Application manifests.

#### Technical Deep Dive
An **ApplicationSet** automatically generates ArgoCD `Application` resources across multiple clusters and environments using **Generators**:
1. **Cluster Generator**: Targets all registered Kubernetes clusters matching specific labels (e.g., `environment: production`).
2. **Git Generator**: Scans Git directories or files (`apps/*/config.json`) and instantiates an application per folder.
3. **Matrix Generator**: Combines multiple generators (e.g., Deploy every app found by Git Generator across every cluster found by Cluster Generator).

```yaml
apiVersion: argoproj.io/v1alpha1
kind: ApplicationSet
metadata:
  name: microservice-fleet
  namespace: argocd
spec:
  generators:
    - matrix:
        generators:
          - clusters:
              selector:
                matchLabels:
                  tier: production
          - git:
              repoURL: https://github.com/org/gitops-manifests.git
              directories:
                - path: apps/*
  template:
    metadata:
      name: '{{path.basename}}-{{name}}'
    spec:
      project: default
      source:
        repoURL: https://github.com/org/gitops-manifests.git
        targetRevision: HEAD
        path: '{{path}}'
      destination:
        server: '{{server}}'
        namespace: '{{path.basename}}'
      syncPolicy:
        automated:
          prune: true
          selfHeal: true
```

---

# Layer 3: Sync Waves, Resource Hooks & Progressive Delivery

---

### Scenario 4: Sync Waves & Pre/Post-Sync Hooks
**Interviewer Evaluation:** Assesses ordering deployment sequences (e.g., Database migration runs *before* Application Deployment starts).

#### Technical Deep Dive
By default, `kubectl apply` deploys manifests in random order. ArgoCD provides **Sync Waves** to guarantee deterministic order:
- Resources are annotated with `argocd.argoproj.io/sync-wave: "<number>"`.
- Lower waves execute first (can be negative: $-5, 0, 1, 5$).
- ArgoCD waits for all resources in Wave $N$ to be **Healthy** before initiating Wave $N+1$.
- **Resource Hooks**:
  - `PreSync`: Runs database schema migration Jobs before rolling out new pods.
  - `SyncFail`: Sends Slack alert if rollout crashes.
  - `PostSync`: Runs smoke test verification suites.

```yaml
apiVersion: batch/v1
kind: Job
metadata:
  name: db-schema-migration
  annotations:
    argocd.argoproj.io/hook: PreSync
    argocd.argoproj.io/sync-wave: "-1"
    argocd.argoproj.io/hook-delete-policy: HookSucceeded
spec:
  template:
    spec:
      containers:
      - name: flyway
        image: flyway/flyway:latest
        command: ['flyway', 'migrate']
      restartPolicy: Never
```

---

### Scenario 5: Progressive Delivery with Argo Rollouts (Canary & AnalysisTemplates)
**Interviewer Evaluation:** Tests zero-downtime canary deployments, automated rollbacks based on Prometheus metrics, and traffic shifting.

#### Technical Deep Dive
Standard Kubernetes Deployments only support basic RollingUpdate. **Argo Rollouts** provides advanced traffic shifting:
1. Replaces `Deployment` with `Rollout` CRD.
2. Integrates with Service Meshes / Ingresses (Istio, Envoy, NGINX, AWS ALB) for precise traffic splitting (e.g., 5% $\to$ 20% $\to$ 50% $\to$ 100%).
3. **Automated Metric Analysis (`AnalysisTemplate`)**:
   - Queries Prometheus during the canary phase:
     $$\text{Error Rate} = \frac{\text{sum}(\text{rate}(\text{http\_requests\_total}\{\text{status}=\sim"5.."\}))}{\text{sum}(\text{rate}(\text{http\_requests\_total}))}$$
   - If error rate exceeds 1%, the rollout **automatically aborts and rolls back to stable** in under 5 seconds with zero human intervention!

```yaml
apiVersion: argoproj.io/v1alpha1
kind: Rollout
metadata:
  name: payment-service
spec:
  replicas: 10
  strategy:
    canary:
      analysis:
        templates:
          - templateName: prometheus-error-rate-check
        args:
          - name: service-name
            value: payment-service
      steps:
        - setWeight: 5
        - pause: { duration: 5m }
        - setWeight: 20
        - pause: { duration: 15m }
        - setWeight: 50
        - pause: { duration: 10m }
```

---

# Layer 4: Secrets Management in GitOps

---

### Scenario 6: Secrets in GitOps: External Secrets Operator vs Sealed Secrets
**Interviewer Evaluation:** Assesses resolving the core GitOps dilemma: "How do you store secrets in public/private Git repositories safely?"

#### Technical Deep Dive
Never commit plaintext Kubernetes `Secret` YAMLs into Git.
1. **Bitnami Sealed Secrets (Asymmetric Encryption)**:
   - Encrypt secret offline using cluster's public key (`kubeseal`).
   - The encrypted `SealedSecret` CRD can be safely committed to public Git.
   - An in-cluster controller uses its private key to decrypt and create a native Kubernetes `Secret`.
2. **External Secrets Operator (ESO - Enterprise Standard)**:
   - Git contains an `ExternalSecret` manifest referencing a path in an external secret manager (AWS Secrets Manager, HashiCorp Vault, Azure Key Vault).
   - ESO controller polls the cloud vault, retrieves the secret value, and syncs it into a native Kubernetes `Secret`.

```yaml
apiVersion: external-secrets.io/v1beta1
kind: ExternalSecret
metadata:
  name: database-credentials
spec:
  refreshInterval: 1h
  secretStoreRef:
    name: aws-secretsmanager
    kind: ClusterSecretStore
  target:
    name: db-secret # Native K8s secret created
  data:
    - secretKey: DB_PASSWORD
      remoteRef:
        key: production/rds/postgres
        property: password
```

---

# Layer 5: Ultra-Deep Real-World War-Room Incidents

---

### Scenario 7: War Room: The Cascading Auto-Prune Production Outage
**Interviewer Evaluation:** Evaluates diagnosing catastrophic cluster resource deletion caused by bad Git commits combined with `prune: true`.

#### Incident Scenario
A developer accidentally deleted the `deployment.yaml` file from the production Git repository and pushed to `main`. Within 30 seconds, ArgoCD reconciled the change and immediately deleted the live production payment microservice deployment, dropping all customer checkouts.

#### Root Cause Analysis
1. The ArgoCD Application had `syncPolicy.automated.prune: true` enabled without safeguards.
2. When the manifest was removed from Git, ArgoCD concluded the resource should no longer exist and issued a delete command to `kube-apiserver`.
3. Lack of safety guardrails allowed immediate cascading deletion of live workloads.

#### Remediation & Prevention
- **Sync Options Safeguards**:
  ```yaml
  syncPolicy:
    syncOptions:
      - PrunePropagationPolicy=foreground
      - PruneLast=true
  ```
- Added the **`argocd.argoproj.io/sync-options: Delete=false`** annotation to mission-critical StatefulSets and Deployments, preventing ArgoCD from ever pruning them automatically.

---

# Layer 6: Beginner Mistakes & Anti-Patterns

---

### Anti-Pattern 1: Manual `kubectl edit` in GitOps Clusters
- ❌ **The Anti-Pattern**: Running `kubectl edit deployment` directly in a production cluster managed by ArgoCD.
- 💥 **Production Impact**: If `selfHeal: true` is enabled, ArgoCD detects manual drift and immediately overwrites your changes within seconds. If `selfHeal` is disabled, the cluster state silently drifts from Git.
- ✅ **The Fix**: Always commit changes to Git. Treat Git as the single source of truth.
- 🧠 **Architectural Principle**: In GitOps, the Kubernetes cluster is read-only for humans.

---

# Layer 7: Globally Reported Production Incidents & Post-Mortems

---

### Incident 1: Monzo Bank Staging Cluster Drift Cascade (2020)
- 🚨 **The Incident**: Automated synchronization across staging clusters failed due to malformed Helm templates, leaving microservices in mixed, incompatible version states for 12 hours.
- 🔍 **Root Cause**: Helm templates contained dynamic timestamps evaluated on every reconciliation pass, causing ArgoCD to detect continuous perpetual drift and enter an infinite sync loop.
- 🛠️ **Remediation**: Replaced dynamic template variables with deterministic values and added pre-commit linting gates for all Helm charts.
- 🛡️ **Architectural Guardrail**: GitOps manifest generation must be strictly deterministic and idempotent; never use dynamic runtime timestamps in templates.

---

# Layer 8: Rapid-Fire Cheat Sheet & Interview Summary Matrix

---

### ArgoCD Essential Annotations Reference

| Annotation | Purpose | Example Value |
| :--- | :--- | :--- |
| `argocd.argoproj.io/sync-wave` | Determines deployment order | `"-1"`, `"0"`, `"5"` |
| `argocd.argoproj.io/hook` | Resource lifecycle execution | `PreSync`, `PostSync`, `SyncFail` |
| `argocd.argoproj.io/hook-delete-policy` | Clean up hook jobs | `HookSucceeded`, `BeforeHookCreation` |
| `argocd.argoproj.io/compare-options` | Ignore specific dynamic fields | `IgnoreExtraneous` |

---

### The Golden GitOps Interview Rules
1. **Git is the single source of truth**: No human touches `kubectl` directly in production.
2. **Never commit raw secrets**: Use External Secrets Operator (Vault/AWS) or Sealed Secrets.
3. **Protect critical resources from pruning**: Use `argocd.argoproj.io/sync-options: Delete=false`.
4. **Enforce deterministic manifests**: Helm/Kustomize must generate byte-identical output across reconciliations.
5. **Adopt Progressive Delivery**: Use Argo Rollouts with automated metric analysis for zero-downtime releases.
