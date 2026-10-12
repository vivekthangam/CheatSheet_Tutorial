package com.cheatsheet.k8s.engine;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Fabric8 Kubernetes Canary Reconciler & Health Verifier Engine
 */
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

        // Fetch individual pod probe statuses
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
        System.err.println("🚨 INITIATING AUTOMATED ROLLBACK for " + deploymentName + " in " + namespace);
        client.apps().deployments()
                .inNamespace(namespace)
                .withName(deploymentName)
                .rolling()
                .undo();
        System.out.println("✅ Rollback command dispatched to kube-apiserver.");
    }

    @Override
    public void run(String... args) {
        System.out.println("Kubernetes Canary Reconciler initialized with cluster apiserver: " +
                client.getMasterUrl());
    }
}
