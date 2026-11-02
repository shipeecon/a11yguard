package com.a11yguard.reporting;

import com.a11yguard.core.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;

public final class JunitXmlReport implements AccessibilityReport {
    @Override public ReportFormat format(){return ReportFormat.JUNIT_XML;}
    @Override public Path write(List<AccessibilityResult> rs, Path dir)throws Exception{
        Files.createDirectories(dir); Path out=dir.resolve("a11yguard-report.xml");
        long failures=rs.stream().filter(r->!r.violations().isEmpty()).count();
        StringBuilder x=new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        x.append("<testsuite name=\"A11yGuard\" tests=\"").append(rs.size()).append("\" failures=\"").append(failures).append("\" skipped=\"0\">\n");
        for(AccessibilityResult r:rs){
            x.append("  <testcase classname=\"").append(ReportUtil.esc(r.framework())).append("\" name=\"").append(ReportUtil.esc(r.testName())).append("\">\n");
            if(!r.violations().isEmpty()){
                x.append("    <failure message=\"Accessibility violations found\"><![CDATA[");
                for(AccessibilityViolation v:r.violations()) x.append("[").append(v.impact()).append("] ").append(v.id()).append(" - ").append(v.help()).append(" - ").append(ReportUtil.target(v)).append("\n");
                x.append("]]></failure>\n");
            }
            if(!r.incomplete().isEmpty()) x.append("    <system-out>Incomplete/manual review items: ").append(r.incomplete().size()).append("</system-out>\n");
            x.append("  </testcase>\n");
        }
        x.append("</testsuite>\n"); Files.writeString(out,x.toString(),StandardCharsets.UTF_8); return out;
    }
}
