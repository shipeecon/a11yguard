package com.a11yguard.core;

import java.util.List;

public record AccessibilityViolation(
    String id, String impact, String description, String help,
    String helpUrl, List<String> target, String html, String screenshot
) {
    public AccessibilityViolation(
        String id, String impact, String description, String help,
        String helpUrl, List<String> target, String html
    ) {
        this(id, impact, description, help, helpUrl, target, html, "");
    }

    public AccessibilityViolation withScreenshot(String path) {
        return new AccessibilityViolation(id, impact, description, help, helpUrl, target, html, path);
    }

    public Severity severity() {
        if (impact == null) return Severity.MINOR;
        return switch (impact.toLowerCase()) {
            case "critical" -> Severity.CRITICAL;
            case "serious" -> Severity.SERIOUS;
            case "moderate" -> Severity.MODERATE;
            default -> Severity.MINOR;
        };
    }
}
