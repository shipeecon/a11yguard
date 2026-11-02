package com.a11yguard.config;

import com.a11yguard.core.AccessibilityResult;
import com.a11yguard.core.AccessibilityViolation;
import com.a11yguard.reporting.ReportUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class Baseline {
    private Baseline() {}

    public static AccessibilityResult apply(AccessibilityResult result, AccessibilityConfig config) {
        List<AccessibilityViolation> kept = new ArrayList<>();
        List<AccessibilityViolation> suppressed = new ArrayList<>(result.suppressed());

        List<Fingerprint> baseline = read(config.baselineFile());

        for (AccessibilityViolation v : result.violations()) {
            String target = ReportUtil.target(v);
            boolean yamlMatch = config.suppress().stream().anyMatch(s -> s.matches(v.id(), target));
            boolean baseMatch = baseline.stream().anyMatch(f -> f.matches(v.id(), target, result.url()));
            if (yamlMatch || baseMatch) suppressed.add(v);
            else kept.add(v);
        }

        AccessibilityResult next = result.withViolations(List.copyOf(kept), List.copyOf(suppressed));
        if (config.updateBaseline()) write(config.baselineFile(), next);
        return next;
    }

    private static List<Fingerprint> read(String file) {
        if (file == null || file.isBlank()) return List.of();
        Path path = Path.of(file);
        if (!Files.isRegularFile(path)) return List.of();
        try {
            FileFormat parsed = new ObjectMapper().readValue(path.toFile(), FileFormat.class);
            return parsed.violations == null ? List.of() : parsed.violations;
        } catch (Exception e) {
            throw new IllegalStateException("Cannot read baseline file: " + file, e);
        }
    }

    private static void write(String file, AccessibilityResult result) {
        try {
            Path path = Path.of(file);
            if (path.getParent() != null) Files.createDirectories(path.getParent());
            FileFormat out = new FileFormat();
            out.violations = result.violations().stream()
                .map(v -> new Fingerprint(v.id(), ReportUtil.target(v), result.url()))
                .toList();
            new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT)
                .writeValue(path.toFile(), out);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot write baseline file: " + file, e);
        }
    }

    public static final class FileFormat {
        public List<Fingerprint> violations = new ArrayList<>();
    }

    public static final class Fingerprint {
        public String id;
        public String target;
        public String url;

        public Fingerprint() {}

        public Fingerprint(String id, String target, String url) {
            this.id = id;
            this.target = target;
            this.url = url;
        }

        boolean matches(String ruleId, String violationTarget, String pageUrl) {
            if (id == null || !id.equalsIgnoreCase(ruleId)) return false;
            boolean targetOk = target == null || target.isBlank() || "*".equals(target)
                || (violationTarget != null && violationTarget.contains(target));
            boolean urlOk = url == null || url.isBlank() || "*".equals(url)
                || (pageUrl != null && pageUrl.equals(url));
            return targetOk && urlOk;
        }
    }
}
