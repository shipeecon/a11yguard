package com.a11yguard.reporting;

import com.a11yguard.core.AccessibilityResult;
import com.a11yguard.core.AccessibilityViolation;
import java.util.*;
import java.util.stream.Collectors;

public final class ReportUtil {
    private ReportUtil() {}

    static String safe(String s) {
        if (s == null || s.isBlank()) return "unknown";
        return s.replaceAll("[^a-zA-Z0-9._-]+", "_");
    }

    static String esc(String s) {
        if (s == null) return "";
        return s.replace("&","&amp;").replace("<","&lt;")
            .replace(">","&gt;").replace("\"","&quot;").replace("'","&#39;");
    }

    static String csv(String s) {
        String v = s == null ? "" : s.replace("\"","\"\"");
        return "\"" + v + "\"";
    }

    static long violationCount(List<AccessibilityResult> rs) {
        return rs.stream().mapToLong(r -> r.violations().size()).sum();
    }

    static long incompleteCount(List<AccessibilityResult> rs) {
        return rs.stream().mapToLong(r -> r.incomplete().size()).sum();
    }

    static Map<String, Long> impacts(List<AccessibilityResult> rs) {
        return rs.stream().flatMap(r -> r.violations().stream())
            .collect(Collectors.groupingBy(v -> v.impact() == null ? "unknown" : v.impact(),
                     TreeMap::new, Collectors.counting()));
    }

    public static String target(AccessibilityViolation v) {
        return v.target() == null ? "" : String.join(" | ", v.target());
    }
}
