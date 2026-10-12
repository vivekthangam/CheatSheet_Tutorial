package com.cheatsheet.k8s.designlab;

import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.ResourceRequirements;
import io.fabric8.kubernetes.api.model.ResourceRequirementsBuilder;

import java.util.Map;

/**
 * Architectural Anti-Patterns & Refactorings for Kubernetes (DesignLab)
 */
public class K8sDesignLab {

    // =========================================================================
    // ANTI-PATTERN 1: Missing Resource Requests Leading to Blind Overcommitment
    // =========================================================================

    public static class ResourceRequirementsPattern {
        // ⚠️ ANTI-PATTERN: Scheduler blindly overcommits nodes based on zero declared requests
        public ResourceRequirements unconstrainedResources() {
            return new ResourceRequirements();
        }

        // 🏗️ REFACTOR: Explicit Burstable QoS with requests and bounds
        public ResourceRequirements hardenedResources() {
            return new ResourceRequirementsBuilder()
                    .withRequests(Map.of(
                            "cpu", new Quantity("1000m"),
                            "memory", new Quantity("1024Mi")
                    ))
                    .withLimits(Map.of(
                            "cpu", new Quantity("2000m"),
                            "memory", new Quantity("1536Mi")
                    ))
                    .build();
        }
    }
}
