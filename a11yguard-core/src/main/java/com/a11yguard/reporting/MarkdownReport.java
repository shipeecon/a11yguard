package com.a11yguard.reporting;

import com.a11yguard.core.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;

public final class MarkdownReport implements AccessibilityReport {
    @Override public ReportFormat format(){return ReportFormat.MARKDOWN;}
    @Override public Path write(List<AccessibilityResult> rs,Path dir)throws Exception{
        Files.createDirectories(dir); Path out=dir.resolve("a11yguard-report.md"); StringBuilder s=new StringBuilder();
        s.append("# A11yGuard Accessibility Report\n\n")
         .append("| Metric | Count |\n|---|---:|\n")
         .append("| Tests | ").append(rs.size()).append(" |\n")
         .append("| Violations | ").append(ReportUtil.violationCount(rs)).append(" |\n")
         .append("| Incomplete / manual review | ").append(ReportUtil.incompleteCount(rs)).append(" |\n\n");
        s.append("## Test executions\n\n| Framework | Test | URL | Standard | Violations | Incomplete |\n|---|---|---|---|---:|---:|\n");
        for(AccessibilityResult r:rs)s.append("| ").append(md(r.framework())).append(" | ").append(md(r.testName())).append(" | ").append(md(r.url())).append(" | ").append(md(r.standard())).append(" | ").append(r.violations().size()).append(" | ").append(r.incomplete().size()).append(" |\n");
        s.append("\n## Violation details\n\n");
        for(AccessibilityResult r:rs)for(AccessibilityViolation v:r.violations()){
            s.append("### ").append(md(v.id())).append(" — ").append(md(v.impact())).append("\n")
             .append("- **Test:** ").append(md(r.testName())).append("\n")
             .append("- **Description:** ").append(md(v.description())).append("\n")
             .append("- **Help:** ").append(md(v.help())).append("\n")
             .append("- **Help URL:** ").append(md(v.helpUrl())).append("\n")
             .append("- **Target:** `").append(md(ReportUtil.target(v))).append("`\n")
             .append("- **HTML:** `").append(md(v.html())).append("`\n\n");
        }
        Files.writeString(out,s.toString(),StandardCharsets.UTF_8);return out;
    }
    private static String md(String s){return s==null?"":s.replace("|","\\|").replace("\n"," "); }
}
