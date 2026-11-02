package com.a11yguard.ci;

import com.a11yguard.core.AccessibilityResult;
import com.a11yguard.core.Severity;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public final class A11yGuardCi {
    private A11yGuardCi() {}

    public static int exitCode(List<AccessibilityResult> results, Set<Severity> failOn) {
        boolean fail = results.stream().anyMatch(r -> r.shouldFail(failOn));
        return fail ? 1 : 0;
    }

    public static void writeExitCode(String reportDir, List<AccessibilityResult> results, Set<Severity> failOn) {
        try {
            Path dir = Path.of(reportDir);
            Files.createDirectories(dir);
            int code = exitCode(results, failOn);
            Files.writeString(dir.resolve("a11yguard-exit-code"), code + "\n", StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot write A11yGuard exit code", e);
        }
    }

    public static void main(String[] args) throws Exception {
        Path report = Path.of(args.length > 0 ? args[0] : "target/a11yguard-report/a11yguard-report.json");
        if (!Files.isRegularFile(report)) {
            System.err.println("A11yGuard report not found: " + report);
            System.exit(1);
        }
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        List<AccessibilityResult> results = mapper.readValue(
            report.toFile(), new TypeReference<List<AccessibilityResult>>() {}
        );
        Set<Severity> failOn = EnumSet.of(Severity.CRITICAL, Severity.SERIOUS);
        int code = exitCode(results, failOn);
        System.out.println("A11yGuard CI exit code: " + code);
        System.exit(code);
    }
}
