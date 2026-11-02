package com.a11yguard.config;

import com.a11yguard.annotation.Accessibility;
import com.a11yguard.core.Severity;
import com.a11yguard.reporting.ReportFormat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public record AccessibilityConfig(
    String standard,
    Set<Severity> failOn,
    List<String> tags,
    List<String> exclude,
    Set<ReportFormat> reportFormats,
    String reportDir,
    boolean screenshots,
    String baselineFile,
    boolean updateBaseline,
    List<Suppression> suppress,
    boolean failOnViolations
) {
    public static AccessibilityConfig from(Accessibility a) {
        YamlFile yaml = YamlLoader.load();

        String standard = first(
            emptyToNull(a.standard()), yaml.standard, "WCAG2AA");

        List<String> failOnRaw = a.failOn().length > 0 ? List.of(a.failOn())
            : yaml.failOn != null && !yaml.failOn.isEmpty() ? yaml.failOn
            : List.of("CRITICAL", "SERIOUS");

        List<String> tags = a.tags().length > 0 ? List.of(a.tags())
            : yaml.tags != null && !yaml.tags.isEmpty() ? yaml.tags
            : List.of("wcag2a", "wcag2aa");

        List<String> exclude = new ArrayList<>();
        if (yaml.exclude != null) exclude.addAll(yaml.exclude);
        exclude.addAll(List.of(a.exclude()));

        List<String> formatsRaw = a.reportFormats().length > 0 ? List.of(a.reportFormats())
            : yaml.reportFormats != null && !yaml.reportFormats.isEmpty() ? yaml.reportFormats
            : List.of("HTML", "JSON", "JUNIT_XML", "SARIF", "CSV", "MARKDOWN");

        String reportDir = !"target/a11yguard-report".equals(a.reportDir())
            ? a.reportDir()
            : first(yaml.reportDir, "target/a11yguard-report");

        boolean screenshots = yaml.screenshots == null ? a.screenshots() : yaml.screenshots;
        String baseline = !a.baseline().isBlank() ? a.baseline()
            : yaml.baseline != null ? yaml.baseline : "a11yguard-baseline.json";
        boolean updateBaseline = a.updateBaseline() || Boolean.TRUE.equals(yaml.updateBaseline);
        boolean failOnViolations = yaml.failOnViolations == null || yaml.failOnViolations;

        EnumSet<Severity> severities = EnumSet.noneOf(Severity.class);
        for (String s : failOnRaw) severities.add(Severity.valueOf(s.toUpperCase(Locale.ROOT)));
        EnumSet<ReportFormat> formats = EnumSet.noneOf(ReportFormat.class);
        for (String s : formatsRaw) formats.add(ReportFormat.valueOf(s.toUpperCase(Locale.ROOT)));

        List<Suppression> suppress = yaml.suppress == null ? List.of() : List.copyOf(yaml.suppress);

        return new AccessibilityConfig(
            standard,
            Collections.unmodifiableSet(severities),
            List.copyOf(tags),
            List.copyOf(exclude),
            Collections.unmodifiableSet(formats),
            reportDir,
            screenshots,
            baseline,
            updateBaseline,
            suppress,
            failOnViolations
        );
    }

    private static String emptyToNull(String s) {
        return s == null || s.isBlank() ? null : s;
    }

    private static String first(String... values) {
        for (String v : values) if (v != null && !v.isBlank()) return v;
        return "";
    }
}
