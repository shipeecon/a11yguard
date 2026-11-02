package com.a11yguard.runtime;

import com.a11yguard.config.AccessibilityConfig;
import com.a11yguard.core.AccessibilityResult;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Discovers a Selenium {@code WebDriver} or Playwright {@code Page} on the
 * test instance and dispatches to the matching adapter.
 */
public final class AccessibilityScan {
    private AccessibilityScan() {}

    public static AccessibilityResult run(
        Object testInstance, String testName, AccessibilityConfig config
    ) {
        Object driver = findField(testInstance, "org.openqa.selenium.WebDriver");
        if (driver != null) {
            return invoke(
                "com.a11yguard.selenium.SeleniumScanner",
                "org.openqa.selenium.WebDriver",
                driver, testName, config
            );
        }

        Object page = findField(testInstance, "com.microsoft.playwright.Page");
        if (page != null) {
            return invoke(
                "com.a11yguard.playwright.PlaywrightScanner",
                "com.microsoft.playwright.Page",
                page, testName, config
            );
        }

        throw new IllegalStateException(
            "A11yGuard could not find a WebDriver or Playwright Page field on the test instance."
        );
    }

    private static AccessibilityResult invoke(
        String scannerClass,
        String targetClass,
        Object target,
        String testName,
        AccessibilityConfig config
    ) {
        try {
            Class<?> scannerType = Class.forName(scannerClass);
            Class<?> pageType = Class.forName(targetClass);
            Object scanner = scannerType.getConstructor().newInstance();
            Method scan = scannerType.getMethod(
                "scan", pageType, String.class, AccessibilityConfig.class
            );
            return (AccessibilityResult) scan.invoke(scanner, target, testName, config);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                "A11yGuard adapter missing from the classpath: " + scannerClass
                    + ". Add the matching A11yGuard module.",
                e
            );
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("A11yGuard scan failed", e);
        }
    }

    private static Object findField(Object instance, String typeName) {
        Class<?> wanted;
        try {
            wanted = Class.forName(typeName);
        } catch (ClassNotFoundException e) {
            return null;
        }

        Class<?> type = instance.getClass();
        while (type != null) {
            for (Field field : type.getDeclaredFields()) {
                if (wanted.isAssignableFrom(field.getType())) {
                    try {
                        field.setAccessible(true);
                        Object value = field.get(instance);
                        if (value != null) return value;
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("Cannot access " + typeName + " field", e);
                    }
                }
            }
            type = type.getSuperclass();
        }
        return null;
    }
}
