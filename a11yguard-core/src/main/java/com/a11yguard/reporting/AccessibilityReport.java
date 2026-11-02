package com.a11yguard.reporting;

import com.a11yguard.core.AccessibilityResult;
import java.nio.file.Path;
import java.util.List;

/** Low-level report writer contract. */
public interface AccessibilityReport {
    ReportFormat format();
    Path write(List<AccessibilityResult> results, Path outputDirectory) throws Exception;
}
