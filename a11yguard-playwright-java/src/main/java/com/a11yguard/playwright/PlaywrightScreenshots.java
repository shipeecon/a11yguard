package com.a11yguard.playwright;

import com.a11yguard.config.AccessibilityConfig;
import com.a11yguard.core.AccessibilityResult;
import com.a11yguard.core.AccessibilityViolation;
import com.a11yguard.reporting.ReportUtil;
import com.a11yguard.reporting.Screenshots;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

final class PlaywrightScreenshots {
    private PlaywrightScreenshots() {}

    static AccessibilityResult attach(
        Page page, AccessibilityResult result, AccessibilityConfig config
    ) {
        if (!config.screenshots()) return result;
        Path reportDir = Path.of(config.reportDir());
        String pageShot = Screenshots.save(
            page.screenshot(), reportDir, result.testName() + "-page"
        );

        List<AccessibilityViolation> withShots = new ArrayList<>();
        int i = 0;
        for (AccessibilityViolation v : result.violations()) {
            String elementShot = elementShot(page, v, reportDir, result.testName() + "-" + v.id() + "-" + i);
            withShots.add(v.withScreenshot(elementShot.isBlank() ? pageShot : elementShot));
            i++;
        }
        return result.withViolations(withShots, result.suppressed()).withPageScreenshot(pageShot);
    }

    private static String elementShot(
        Page page, AccessibilityViolation v, Path reportDir, String name
    ) {
        try {
            String selector = css(ReportUtil.target(v));
            if (selector.isBlank()) return "";
            Locator loc = page.locator(selector).first();
            if (loc.count() == 0) return "";
            return Screenshots.save(loc.screenshot(), reportDir, name);
        } catch (Exception e) {
            return "";
        }
    }

    private static String css(String target) {
        if (target == null) return "";
        String cleaned = target.replace("[", "").replace("]", "").trim();
        if (cleaned.contains(",")) cleaned = cleaned.split(",")[0].trim();
        if ((cleaned.startsWith("\"") && cleaned.endsWith("\""))
            || (cleaned.startsWith("'") && cleaned.endsWith("'"))) {
            cleaned = cleaned.substring(1, cleaned.length() - 1);
        }
        return cleaned;
    }
}
