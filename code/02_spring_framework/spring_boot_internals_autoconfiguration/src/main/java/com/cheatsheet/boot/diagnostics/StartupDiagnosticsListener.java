package com.cheatsheet.boot.diagnostics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionEvaluationReport;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Diagnostic component that programmatically queries Spring Boot's
 * ConditionEvaluationReport to display which auto-configuration classes
 * matched and which were skipped during container startup.
 */
@Component
public class StartupDiagnosticsListener implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger log = LoggerFactory.getLogger(StartupDiagnosticsListener.class);

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        ConfigurableApplicationContext context = event.getApplicationContext();
        if (context.containsBean("autoConfigurationReport") || context.getBeanFactory() != null) {
            ConditionEvaluationReport report = ConditionEvaluationReport.get(context.getBeanFactory());
            summarizeReport(report);
        }
    }

    public void summarizeReport(ConditionEvaluationReport report) {
        log.info("===============================================================================");
        log.info("SPRING BOOT INTERNALS: CONDITION EVALUATION REPORT SUMMARY");
        log.info("===============================================================================");

        int positiveMatches = 0;
        int negativeMatches = 0;

        for (Map.Entry<String, ConditionEvaluationReport.ConditionAndOutcomes> entry :
                report.getConditionAndOutcomesBySource().entrySet()) {

            String source = entry.getKey();
            // Filter to our package or relevant auto-configurations to avoid flooding logs
            if (source.contains("cheatsheet") || source.contains("Audit") || source.contains("RateLimit") || source.contains("Tenant")) {
                boolean isMatch = entry.getValue().isFullMatch();
                if (isMatch) {
                    positiveMatches++;
                    log.info("  [MATCHED] {}", source);
                    for (ConditionEvaluationReport.ConditionAndOutcome cao : entry.getValue()) {
                        log.info("    -> Condition: {} | Outcome: {}",
                                cao.getCondition().getClass().getSimpleName(),
                                cao.getOutcome().getMessage());
                    }
                } else {
                    negativeMatches++;
                    log.info("  [DID NOT MATCH] {}", source);
                    for (ConditionEvaluationReport.ConditionAndOutcome cao : entry.getValue()) {
                        if (!cao.getOutcome().isMatch()) {
                            log.info("    -> Missed Reason: {}", cao.getOutcome().getMessage());
                        }
                    }
                }
            }
        }

        log.info("Diagnostic scan finished: {} target classes matched, {} skipped.", positiveMatches, negativeMatches);
        log.info("===============================================================================");
    }
}
