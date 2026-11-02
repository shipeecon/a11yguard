package com.a11yguard.core;

public class AccessibilityFailure extends AssertionError {
    public AccessibilityFailure(String message) {
        super(message);
    }
}
