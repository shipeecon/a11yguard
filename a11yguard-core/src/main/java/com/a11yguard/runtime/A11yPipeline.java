package com.a11yguard.runtime;

import com.a11yguard.annotation.Accessibility;
import com.a11yguard.ci.A11yGuardCi;
import com.a11yguard.config.AccessibilityConfig;
import com.a11yguard.config.Baseline;
import com.a11yguard.core.AccessibilityAssertions;
import com.a11yguard.core.AccessibilityResult;
import com.a11yguard.reporting.AccessibilityReporter;

import java.nio.file.Path;
import java.util.List;

public final class A11yPipeline {
    private A11yPipeline() {}

    public static void afterTest(Object testInstance, String testName, Accessibility annotation) {
        if (annotation == null || !annotation.value()) return;

        AccessibilityConfig config = AccessibilityConfig.from(annotation);
        AccessibilityResult result = Baseline.apply(
            AccessibilityScan.run(testInstance, testName, config), config
        );

        AccessibilityReporter.report(
            result, Path.of(config.reportDir()), config.reportFormats());
        A11yGuardCi.writeExitCode(
            config.reportDir(), AccessibilityReporter.results(), config.failOn());

        if (config.updateBaseline() || !config.failOnViolations()) return;
        AccessibilityAssertions.assertAccessible(result, config.failOn());
    }
}
