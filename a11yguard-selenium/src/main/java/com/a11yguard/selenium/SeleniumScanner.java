package com.a11yguard.selenium;

import com.a11yguard.config.AccessibilityConfig;
import com.a11yguard.core.*;
import com.deque.html.axecore.selenium.AxeBuilder;
import com.deque.html.axecore.results.Results;
import com.deque.html.axecore.results.Rule;
import org.openqa.selenium.WebDriver;
import java.time.Instant;
import java.util.List;

public final class SeleniumScanner {

    public AccessibilityResult scan(
        WebDriver driver, String testName, AccessibilityConfig config
    ) {
        AxeBuilder builder = new AxeBuilder();

        if (!config.tags().isEmpty()) {
            builder.withTags(config.tags());
        }

        for (String selector : config.exclude()) {
            builder.exclude(selector);
        }

        Results results = builder.analyze(driver);

        List<AccessibilityViolation> violations = results.getViolations()
            .stream()
            .map(this::map)
            .toList();

        List<AccessibilityViolation> incomplete = results.getIncomplete()
            .stream()
            .map(this::map)
            .toList();

        AccessibilityResult result = new AccessibilityResult(
            "selenium", testName, driver.getCurrentUrl(),
            Instant.now(), config.standard(), violations, incomplete
        );
        return SeleniumScreenshots.attach(driver, result, config);
    }

    private AccessibilityViolation map(Rule rule) {
        String target = rule.getNodes() == null || rule.getNodes().isEmpty()
            ? "" : rule.getNodes().get(0).getTarget().toString();

        String html = rule.getNodes() == null || rule.getNodes().isEmpty()
            ? "" : rule.getNodes().get(0).getHtml();

        return new AccessibilityViolation(
            rule.getId(), rule.getImpact(), rule.getDescription(),
            rule.getHelp(), rule.getHelpUrl(),
            List.of(target), html
        );
    }
}
