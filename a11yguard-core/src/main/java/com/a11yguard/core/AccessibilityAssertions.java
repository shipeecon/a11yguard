package com.a11yguard.core;

import java.util.stream.Collectors;

public final class AccessibilityAssertions {
    private AccessibilityAssertions() {}

    public static void assertAccessible(AccessibilityResult result,
                                        java.util.Set<Severity> failOn) {
        if (!result.shouldFail(failOn)) return;

        String details = result.violations().stream()
            .filter(v -> failOn.contains(v.severity()))
            .map(v -> "[" + v.severity() + "] " + v.id() +
                      " - " + v.help() + " - " + v.target())
            .collect(Collectors.joining("\n"));

        throw new AccessibilityFailure(
            "Accessibility violations found at " + result.url() + "\n" + details
        );
    }
}
