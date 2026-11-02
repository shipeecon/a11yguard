package com.a11yguard.selenium;

import com.a11yguard.config.AccessibilityConfig;
import com.a11yguard.core.AccessibilityResult;
import com.a11yguard.core.AccessibilityViolation;
import com.a11yguard.reporting.ReportUtil;
import com.a11yguard.reporting.Screenshots;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

final class SeleniumScreenshots {
    private SeleniumScreenshots() {}

    static AccessibilityResult attach(
        WebDriver driver, AccessibilityResult result, AccessibilityConfig config
    ) {
        if (!config.screenshots() || !(driver instanceof TakesScreenshot ts)) return result;
        Path reportDir = Path.of(config.reportDir());
        String pageShot = Screenshots.save(
            ts.getScreenshotAs(OutputType.BYTES), reportDir, result.testName() + "-page"
        );

        List<AccessibilityViolation> withShots = new ArrayList<>();
        int i = 0;
        for (AccessibilityViolation v : result.violations()) {
            String elementShot = elementShot(driver, v, reportDir, result.testName() + "-" + v.id() + "-" + i);
            withShots.add(v.withScreenshot(elementShot.isBlank() ? pageShot : elementShot));
            i++;
        }
        return result.withViolations(withShots, result.suppressed()).withPageScreenshot(pageShot);
    }

    private static String elementShot(
        WebDriver driver, AccessibilityViolation v, Path reportDir, String name
    ) {
        try {
            String selector = css(ReportUtil.target(v));
            if (selector.isBlank()) return "";
            WebElement el = driver.findElement(By.cssSelector(selector));
            return Screenshots.save(el.getScreenshotAs(OutputType.BYTES), reportDir, name);
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
