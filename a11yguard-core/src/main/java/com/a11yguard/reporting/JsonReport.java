package com.a11yguard.reporting;

import com.a11yguard.core.AccessibilityResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.nio.file.*;
import java.util.List;

public final class JsonReport implements AccessibilityReport {
    private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules()
        .enable(SerializationFeature.INDENT_OUTPUT);
    @Override public ReportFormat format() { return ReportFormat.JSON; }
    @Override public Path write(List<AccessibilityResult> results, Path dir) throws Exception {
        Files.createDirectories(dir);
        Path out=dir.resolve("a11yguard-report.json");
        mapper.writeValue(out.toFile(), results);
        return out;
    }
}
