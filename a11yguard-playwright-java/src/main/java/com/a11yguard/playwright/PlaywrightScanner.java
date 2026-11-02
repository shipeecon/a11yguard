package com.a11yguard.playwright;

import com.a11yguard.config.AccessibilityConfig;
import com.a11yguard.core.*;
import com.deque.html.axecore.playwright.AxeBuilder;
import com.deque.html.axecore.results.AxeResults;
import com.deque.html.axecore.results.Rule;
import com.microsoft.playwright.Page;
import java.time.Instant;
import java.util.List;

public final class PlaywrightScanner {

    public AccessibilityResult scan(
        Page page, String testName, AccessibilityConfig config
    ) {
        AxeBuilder builder = new AxeBuilder(page);

        if (!config.tags().isEmpty()) {
            builder.withTags(config.tags());
        }
        for (String selector : config.exclude()) {
            builder.exclude(selector);
        }

        AxeResults results = builder.analyze();

        List<AccessibilityViolation> violations = results.getViolations()
            .stream()
            .map(this::map)
            .toList();

        List<AccessibilityViolation> incomplete = results.getIncomplete()
            .stream()
            .map(this::map)
            .toList();

        AccessibilityResult result = new AccessibilityResult(
            "playwright-java", testName, page.url(),
            Instant.now(), config.standard(), violations, incomplete
        );
        return PlaywrightScreenshots.attach(page, result, config);
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
