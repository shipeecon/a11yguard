package com.a11yguard.core;

import java.time.Instant;
import java.util.List;

public record AccessibilityResult(
    String framework, String testName, String url,
    Instant timestamp, String standard,
    List<AccessibilityViolation> violations,
    List<AccessibilityViolation> incomplete,
    List<AccessibilityViolation> suppressed,
    String pageScreenshot
) {
    public AccessibilityResult(
        String framework, String testName, String url,
        Instant timestamp, String standard,
        List<AccessibilityViolation> violations,
        List<AccessibilityViolation> incomplete
    ) {
        this(framework, testName, url, timestamp, standard, violations, incomplete, List.of(), "");
    }

    public AccessibilityResult withPageScreenshot(String path) {
        return new AccessibilityResult(
            framework, testName, url, timestamp, standard,
            violations, incomplete, suppressed, path
        );
    }

    public AccessibilityResult withViolations(
        List<AccessibilityViolation> nextViolations,
        List<AccessibilityViolation> nextSuppressed
    ) {
        return new AccessibilityResult(
            framework, testName, url, timestamp, standard,
            nextViolations, incomplete, nextSuppressed, pageScreenshot
        );
    }

    public boolean shouldFail(java.util.Set<Severity> failOn) {
        return violations.stream().anyMatch(v -> failOn.contains(v.severity()));
    }
}
