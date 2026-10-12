# Kubernetes & Cloud Orchestration: Production Operations Cheatsheet

> **Curriculum Milestone**: Cloud & Kubernetes Infrastructure  
> **Topic Coverage**: Pod Lifecycle, Zero-Downtime Rolling Updates, PodDisruptionBudgets, Resource Requests/Limits & CFS Bandwidth Quotas, Probes (Startup/Liveness/Readiness), NetworkPolicies, SecurityContext (Restricted PSS), and Fabric8 Java Reconciliation Controllers.  
> 🛠️ **Real-World Project Cheatsheet:** [Jump to Attached Production Kubernetes Cheatsheet](#-real-world-production-project--code-cheatsheet-zero-downtime-kubernetes-orchestrator) | [Companion IDE Project](file:///d:/project/github/1/CheatSheet_Tutorial/code/03_cloud_infrastructure/kubernetes_orchestration/)  
> 🔗 **Flagship Guide:** [Kubernetes CKA/CKAD/CKS Architecture & Container Orchestration Master Guide (2,500+ Lines)](file:///d:/project/github/1/CheatSheet_Tutorial/interview_prep/03_cloud_infrastructure/kubernetes_orchestration.md)

---

## 🛠️ Real-World Production Project & Code Cheatsheet: Zero-Downtime Kubernetes Orchestrator

> **Attached Domain Project:** `Production Kubernetes Zero-Downtime Canary Reconciler & Manifest Suite` (`com.cheatsheet.k8s`)  
> **Target Tooling:** Kubernetes v1.28–v1.31+ | Fabric8 Kubernetes Client 6.13+ | Kube-Proxy eBPF / iptables  
> **Companion Code Mirror:** [`code/03_cloud_infrastructure/kubernetes_orchestration/`](file:///d:/project/github/1/CheatSheet_Tutorial/code/03_cloud_infrastructure/kubernetes_orchestration/)  
> **Key Technologies Demonstrated:** Zero-Downtime Rolling Deployments, PreStop Hook (`sleep 15`), PodDisruptionBudget (`maxUnavailable: 1`), TopologySpreadConstraints across AZs, Startup/Readiness/Liveness Probes, Restricted Pod Security Standards (`runAsNonRoot`, `readOnlyRootFilesystem`), Default-Deny Egress/Ingress NetworkPolicies, Fabric8 Canary Rollout Watch Controller.

---

### 1. Complete Runnable Topic Code Cheatsheet (Fabric8 Reconciler & Manifest)

```java
package com.cheatsheet.k8s.engine;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class KubernetesCanaryReconciler implements CommandLineRunner {

    private final KubernetesClient client;

    public KubernetesCanaryReconciler() {
        this.client = new KubernetesClientBuilder().build();
    }

    public record CanaryRolloutStatus(boolean isHealthy, int readyReplicas, int desiredReplicas, String message) {}

    public CanaryRolloutStatus evaluateCanaryHealth(String namespace, String deploymentName) {
        Deployment deployment = client.apps().deployments()
                .inNamespace(namespace)
                .withName(deploymentName)
                .get();

        if (deployment == null) {
            return new CanaryRolloutStatus(false, 0, 0, "Deployment not found: " + deploymentName);
        }

        int desired = deployment.getSpec().getReplicas() != null ? deployment.getSpec().getReplicas() : 1;
        Integer ready = deployment.getStatus() != null && deployment.getStatus().getReadyReplicas() != null
                ? deployment.getStatus().getReadyReplicas() : 0;

        PodList podList = client.pods().inNamespace(namespace)
                .withLabels(Map.of("app", deploymentName))
                .list();

        long crashLoopCount = podList.getItems().stream()
                .filter(this::isPodInCrashLoop)
                .count();

        if (crashLoopCount > 0) {
            return new CanaryRolloutStatus(false, ready, desired,
                    "Detected " + crashLoopCount + " pods in CrashLoopBackOff! Rollback required.");
        }

        boolean healthy = ready.equals(desired);
        return new CanaryRolloutStatus(healthy, ready, desired,
                healthy ? "Canary stable: all " + ready + " replicas Ready." : "Canary warming up: " + ready + "/" + desired);
    }

    private boolean isPodInCrashLoop(Pod pod) {
        if (pod.getStatus() == null || pod.getStatus().getContainerStatuses() == null) return false;
        return pod.getStatus().getContainerStatuses().stream()
                .anyMatch(status -> status.getState() != null &&
                        status.getState().getWaiting() != null &&
                        "CrashLoopBackOff".equalsIgnoreCase(status.getState().getWaiting().getReason()));
    }

    public void rollbackDeployment(String namespace, String deploymentName) {
        client.apps().deployments()
                .inNamespace(namespace)
                .withName(deploymentName)
                .rolling()
                .undo();
    }

    @Override
    public void run(String... args) {
        System.out.println("Kubernetes Canary Reconciler active on: " + client.getMasterUrl());
    }
}
```

---

### 2. Positive vs Negative Execution Paths (Failure Boundaries)

| Phase | Positive Path (Staff-Grade Standard) | Negative Path (Chaos & Failure Invariants) | Detection & Automated Recovery Mechanism |
| :--- | :--- | :--- | :--- |
| **Rolling Deployment** | `maxUnavailable: 0` spins up new replica first; `readinessProbe` passes; traffic seamlessly routes. | New replica starts taking traffic immediately without warmup; client gets `502 Bad Gateway`. | `startupProbe` buffers JVM startup; `preStop: sleep 15` ensures kube-proxy flushes iptables before `SIGTERM`. |
| **CFS CPU Quota Throttling** | Container runs with properly sized CPU requests and generous limits. | Strict CPU limit (`cpu: 200m`) hits CFS quota period ($100\text{ms}$); thread stalls 80ms per tick. | Monitored via `container_cpu_cfs_throttled_periods_total`; eliminate artificial CPU limits or increase allocation. |
| **Node Maintenance Drain** | `kubectl drain node-1` respects PodDisruptionBudget (`minAvailable: 3`); pods migrate cleanly. | `minAvailable: 100%` configured on single-replica service; drain command hangs indefinitely. | PDB eviction API rejects eviction with `429 Too Many Requests`; alert platform SRE on persistent drain locks. |
| **Database Network Outage** | Downstream Postgres DB drops; `readinessProbe` fails on `/actuator/health/readiness`. | Pod fails `livenessProbe` on DB failure; kubelet restarts all 100 pods simultaneously (Thundering Herd). | Separate Liveness (process health) from Readiness (traffic availability); endpoints removed without crash loop. |
| **Metadata SSRF Exfiltration** | Web application has SSRF vulnerability; egress NetworkPolicy blocks `169.254.169.254`. | Workload fetches IAM role credentials from Cloud Metadata endpoint; cluster hijacked. | NetworkPolicy drops metadata IP egress; IMDSv2 requires PUT hop-limit tokens. |

---

### 3. Real-World Mistakes, Outage Bugs & Fixes (BugLab)

* **Bug 1: Missing `preStop` Hook Causing 502 Bad Gateway Spikes During Rolling Deployments**  
  * *Root Cause*: Kubelet sends `SIGTERM` concurrently while kube-proxy updates iptables asynchronously. Dropped in-flight connections.  
  * *Fix*: Configure container `preStop: sleep 15` to let proxy remove pod IP before shutdown.
* **Bug 2: PodDisruptionBudget Deadlocking Node Drain & Autoscaling Operations**  
  * *Root Cause*: `minAvailable: 2` on 2-replica Deployment prevents node eviction API from draining node.  
  * *Fix*: Use `maxUnavailable: 1` or size `minAvailable` strictly less than total replicas.
* **Bug 3: Coupling Liveness Probe to Downstream Database Health**  
  * *Root Cause*: External DB slowdown failed all pod liveness checks, triggering cluster-wide container crash restart storm.  
  * *Fix*: Keep Liveness checking internal process only (`/actuator/health/liveness`); use Readiness for dependencies.

---

### 4. Security Vulnerabilities & Mitigations (SecurityLab)

* **Sec 1: Root Container Breakout**: Default root user and writable rootfs allow container escape.  
  * *Mitigation*: Enforce Restricted Pod Security Standard (`runAsNonRoot: true`, `readOnlyRootFilesystem: true`, drop all capabilities).
* **Sec 2: Cloud Instance Metadata Service SSRF**: Vulnerable container fetches AWS IAM credentials from `169.254.169.254`.  
  * *Mitigation*: Apply default-deny Egress `NetworkPolicy` excluding `169.254.169.254/32`.

---

### 5. Architectural Anti-Patterns & Refactorings (DesignLab)

* **Arch 1: Missing Resource Requests**: Kubernetes scheduler packs pods blindly onto single node until host OOM killer strikes.  
  * *Refactor*: Specify explicit guaranteed `resources.requests.cpu` and `resources.requests.memory`.
* **Arch 2: StatefulSets with Ephemeral Node-Pinned Storage**: `hostPath` traps state on single physical machine.  
  * *Refactor*: Use `volumeClaimTemplates` backed by Cloud CSI (gp3 EBS / Ceph RBD) with dynamic volume provisioning.
