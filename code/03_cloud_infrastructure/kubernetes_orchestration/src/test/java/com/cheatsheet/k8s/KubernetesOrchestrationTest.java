package com.cheatsheet.k8s;

import com.cheatsheet.k8s.buglab.K8sBugLab;
import com.cheatsheet.k8s.designlab.K8sDesignLab;
import com.cheatsheet.k8s.securitylab.K8sSecurityLab;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.Probe;
import io.fabric8.kubernetes.api.model.ProbeBuilder;
import io.fabric8.kubernetes.api.model.ResourceRequirements;
import io.fabric8.kubernetes.api.model.SecurityContext;
import io.fabric8.kubernetes.api.model.policy.v1.PodDisruptionBudgetSpec;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KubernetesOrchestrationTest {

    @Test
    @DisplayName("Test BugLab: PreStop hook correctly attached to container")
    void testPreStopHookAttached() {
        K8sBugLab.ZeroDowntimeRolloutBug bug = new K8sBugLab.ZeroDowntimeRolloutBug();
        Container container = new Container();

        bug.configureHardenedContainer(container);
        assertNotNull(container.getLifecycle());
        assertNotNull(container.getLifecycle().getPreStop());
        assertTrue(container.getLifecycle().getPreStop().getExec().getCommand().contains("sleep 15"));
    }

    @Test
    @DisplayName("Test BugLab: PodDisruptionBudget configured with maxUnavailable: 1 to prevent deadlock")
    void testPodDisruptionBudgetNonDeadlock() {
        K8sBugLab.PodDisruptionBudgetDeadlockBug bug = new K8sBugLab.PodDisruptionBudgetDeadlockBug();
        PodDisruptionBudgetSpec spec = new PodDisruptionBudgetSpec();

        bug.configureHardenedPdb(spec);
        assertNotNull(spec.getMaxUnavailable());
        assertEquals(1, spec.getMaxUnavailable().getIntVal());
    }

    @Test
    @DisplayName("Test BugLab: Decoupled Liveness vs Readiness Probes")
    void testDecoupledProbes() {
        K8sBugLab.CascadingRestartProbeBug bug = new K8sBugLab.CascadingRestartProbeBug();
        Probe liveness = new ProbeBuilder().withNewHttpGet().withPath("/actuator/health").endHttpGet().build();
        Probe readiness = new ProbeBuilder().withNewHttpGet().withPath("/actuator/health").endHttpGet().build();

        bug.configureHardenedProbes(liveness, readiness);
        assertEquals("/actuator/health/liveness", liveness.getHttpGet().getPath());
        assertEquals("/actuator/health/readiness", readiness.getHttpGet().getPath());
    }

    @Test
    @DisplayName("Test SecurityLab: Restricted Pod Security Standard")
    void testSecurityContextHardened() {
        K8sSecurityLab.ContainerPrivilegeDefense defense = new K8sSecurityLab.ContainerPrivilegeDefense();
        SecurityContext sc = defense.createHardenedSecurityContext();

        assertTrue(sc.getRunAsNonRoot());
        assertEquals(10001L, sc.getRunAsUser());
        assertTrue(sc.getReadOnlyRootFilesystem());
        assertFalse(sc.getAllowPrivilegeEscalation());
        assertTrue(sc.getCapabilities().getDrop().contains("ALL"));
    }

    @Test
    @DisplayName("Test DesignLab: Explicit Resource Requests & Limits")
    void testResourceRequirements() {
        K8sDesignLab.ResourceRequirementsPattern pattern = new K8sDesignLab.ResourceRequirementsPattern();
        ResourceRequirements rr = pattern.hardenedResources();

        assertNotNull(rr.getRequests());
        assertEquals("1000m", rr.getRequests().get("cpu").getAmount());
        assertEquals("1024Mi", rr.getRequests().get("memory").getAmount());
        assertNotNull(rr.getLimits());
        assertEquals("2000m", rr.getLimits().get("cpu").getAmount());
        assertEquals("1536Mi", rr.getLimits().get("memory").getAmount());
    }
}
