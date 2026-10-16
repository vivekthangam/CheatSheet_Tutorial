package com.cheatsheet.boot;

import com.cheatsheet.boot.autoconfigure.audit.AuditService;
import com.cheatsheet.boot.autoconfigure.ratelimit.RateLimiter;
import com.cheatsheet.boot.diagnostics.ConditionEvaluationReportViewer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Optional;

/**
 * Main application demonstrating Spring Boot Autoconfiguration & Internals.
 */
@SpringBootApplication
public class SpringBootInternalsApplication {

    private static final Logger log = LoggerFactory.getLogger(SpringBootInternalsApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(SpringBootInternalsApplication.class, args);
    }

    @Bean
    public CommandLineRunner demoRunner(
            Optional<AuditService> auditService,
            Optional<RateLimiter> rateLimiter,
            ConditionEvaluationReportViewer reportViewer) {
        return args -> {
            log.info("------------------------------------------------------------------");
            log.info("SPRING BOOT AUTOCONFIGURATION INTERNALS DEMO RUNNER");
            log.info("------------------------------------------------------------------");

            auditService.ifPresentOrElse(
                    audit -> {
                        log.info("Audit Service Bean Present: {}", audit.getClass().getSimpleName());
                        audit.recordEvent("SYSTEM_BOOT", "Application initialized successfully with custom autoconfig");
                    },
                    () -> log.warn("Audit Service Bean NOT configured.")
            );

            rateLimiter.ifPresentOrElse(
                    limiter -> {
                        log.info("Rate Limiter Bean Present: {}", limiter.getClass().getSimpleName());
                        log.info("Capacity: {}, Permitted: {}", limiter.getCapacity(), limiter.tryAcquire("client-1"));
                    },
                    () -> log.warn("Rate Limiter Bean NOT configured.")
            );

            log.info("Matched custom auto-configs: {}",
                    reportViewer.getEvaluationsForPackage("com.cheatsheet.boot").stream()
                            .filter(ConditionEvaluationReportViewer.EvaluationSummary::fullMatch)
                            .map(ConditionEvaluationReportViewer.EvaluationSummary::sourceName)
                            .toList());

            log.info("------------------------------------------------------------------");
        };
    }
}
