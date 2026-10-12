package com.cheatsheet.k8s.securitylab;

import io.fabric8.kubernetes.api.model.SecurityContext;
import io.fabric8.kubernetes.api.model.SecurityContextBuilder;

import java.util.List;

/**
 * Real-World Kubernetes Security Vulnerabilities & Mitigations (SecurityLab)
 */
public class K8sSecurityLab {

    // =========================================================================
    // VULNERABILITY 1: Root Container Breakout & Host Filesystem Modification
    // =========================================================================

    public static class ContainerPrivilegeDefense {
        // 🚨 VULNERABILITY: Root UID execution with writable root filesystem
        public SecurityContext createInsecureContext() {
            return new SecurityContextBuilder()
                    .withRunAsNonRoot(false)
                    .withReadOnlyRootFilesystem(false)
                    .withAllowPrivilegeEscalation(true)
                    .build();
        }

        // 🛡️ MITIGATION: Enforce Restricted Pod Security Standard
        public SecurityContext createHardenedSecurityContext() {
            return new SecurityContextBuilder()
                    .withRunAsNonRoot(true)
                    .withRunAsUser(10001L)
                    .withRunAsGroup(10001L)
                    .withReadOnlyRootFilesystem(true)
                    .withAllowPrivilegeEscalation(false)
                    .withNewCapabilities()
                    .withDrop(List.of("ALL"))
                    .endCapabilities()
                    .build();
        }
    }
}
