package com.cheatsheet.k8s.buglab;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.Lifecycle;
import io.fabric8.kubernetes.api.model.Probe;
import io.fabric8.kubernetes.api.model.policy.v1.PodDisruptionBudgetSpec;

import java.util.List;

/**
 * Real-World Kubernetes Outage Bugs & Fixes (BugLab)
 */
public class K8sBugLab {

    // =========================================================================
    // BUG 1: Missing preStop Hook Causing 502 Bad Gateway Spikes During Rollout
    // =========================================================================

    public static class ZeroDowntimeRolloutBug {
        // ❌ BUG: Container terminates immediately upon SIGTERM while iptables still routes traffic
        public void configureVulnerableContainer(Container container) {
            container.setLifecycle(null);
        }

        // ✅ REFACTORED: Add preStop hook to allow kube-proxy iptables propagation
        public void configureHardenedContainer(Container container) {
            Lifecycle lifecycle = new Lifecycle();
            lifecycle.setPreStop(new io.fabric8.kubernetes.api.model.LifecycleHandlerBuilder()
                    .withNewExec()
                    .withCommand(List.of("/bin/sh", "-c", "sleep 15"))
                    .endExec()
                    .build());
            container.setLifecycle(lifecycle);
        }
    }

    // =========================================================================
    // BUG 2: PodDisruptionBudget Deadlocking Node Drain Operations
    // =========================================================================

    public static class PodDisruptionBudgetDeadlockBug {
        // ❌ BUG: minAvailable equals replicas; node drain CANNOT evict ANY pod without breaching PDB
        public void configureDeadlockedPdb(PodDisruptionBudgetSpec spec, int replicaCount) {
            spec.setMinAvailable(new io.fabric8.kubernetes.api.model.IntOrString(replicaCount));
        }

        // ✅ REFACTORED: Use maxUnavailable: 1 allowing safe single-pod node eviction
        public void configureHardenedPdb(PodDisruptionBudgetSpec spec) {
            spec.setMaxUnavailable(new io.fabric8.kubernetes.api.model.IntOrString(1));
        }
    }

    // =========================================================================
    // BUG 3: Coupling Liveness Probe to Downstream Database Health
    // =========================================================================

    public static class CascadingRestartProbeBug {
        // ❌ BUG: Liveness probe checks external database; causes cascading restart storm
        public void configureVulnerableLiveness(Probe livenessProbe) {
            livenessProbe.getHttpGet().setPath("/actuator/health"); // includes DB, Redis, etc.
        }

        // ✅ REFACTORED: Liveness checks ONLY internal process health; Readiness checks external DB
        public void configureHardenedProbes(Probe livenessProbe, Probe readinessProbe) {
            livenessProbe.getHttpGet().setPath("/actuator/health/liveness");
            readinessProbe.getHttpGet().setPath("/actuator/health/readiness");
        }
    }
}
