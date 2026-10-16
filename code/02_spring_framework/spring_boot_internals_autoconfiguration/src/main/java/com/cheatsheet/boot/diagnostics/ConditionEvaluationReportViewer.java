package com.cheatsheet.boot.diagnostics;

import org.springframework.boot.autoconfigure.condition.ConditionEvaluationReport;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service providing structured access to ConditionEvaluationReport data.
 * Useful for building internal monitoring endpoints, administrative diagnostics,
 * or validating configuration states during test assertions.
 */
@Service
public class ConditionEvaluationReportViewer {

    private final ApplicationContext applicationContext;

    public ConditionEvaluationReportViewer(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public record ConditionDetail(String conditionClass, boolean matched, String outcomeMessage) {}

    public record EvaluationSummary(
            String sourceName,
            boolean fullMatch,
            List<ConditionDetail> details
    ) {}

    /**
     * Retrieves all condition evaluation results for custom or specified packages.
     */
    public List<EvaluationSummary> getEvaluationsForPackage(String packagePrefix) {
        if (!(applicationContext instanceof ConfigurableApplicationContext configurableContext)) {
            return Collections.emptyList();
        }

        ConditionEvaluationReport report = ConditionEvaluationReport.get(configurableContext.getBeanFactory());
        List<EvaluationSummary> results = new ArrayList<>();

        for (Map.Entry<String, ConditionEvaluationReport.ConditionAndOutcomes> entry :
                report.getConditionAndOutcomesBySource().entrySet()) {

            String source = entry.getKey();
            if (packagePrefix == null || packagePrefix.isEmpty() || source.contains(packagePrefix)) {
                List<ConditionDetail> details = new ArrayList<>();
                for (ConditionEvaluationReport.ConditionAndOutcome cao : entry.getValue()) {
                    details.add(new ConditionDetail(
                            cao.getCondition().getClass().getSimpleName(),
                            cao.getOutcome().isMatch(),
                            cao.getOutcome().getMessage()
                    ));
                }
                results.add(new EvaluationSummary(source, entry.getValue().isFullMatch(), details));
            }
        }

        return results;
    }

    /**
     * Returns all matched configuration sources.
     */
    public List<String> getMatchedSources() {
        return getEvaluationsForPackage("")
                .stream()
                .filter(EvaluationSummary::fullMatch)
                .map(EvaluationSummary::sourceName)
                .collect(Collectors.toList());
    }

    /**
     * Returns all skipped configuration sources.
     */
    public List<String> getSkippedSources() {
        return getEvaluationsForPackage("")
                .stream()
                .filter(s -> !s.fullMatch())
                .map(EvaluationSummary::sourceName)
                .collect(Collectors.toList());
    }
}
