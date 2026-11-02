package com.a11yguard.config;

public record Suppression(String id, String target, String reason) {
    public boolean matches(String ruleId, String violationTarget) {
        if (id == null || !id.equalsIgnoreCase(ruleId)) return false;
        if (target == null || target.isBlank() || "*".equals(target)) return true;
        String haystack = violationTarget == null ? "" : violationTarget;
        return haystack.contains(target);
    }
}
