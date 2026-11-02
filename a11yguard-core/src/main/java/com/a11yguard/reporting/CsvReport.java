package com.a11yguard.reporting;

import com.a11yguard.core.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;

public final class CsvReport implements AccessibilityReport {
    @Override public ReportFormat format(){return ReportFormat.CSV;}
    @Override public Path write(List<AccessibilityResult> rs,Path dir)throws Exception{
        Files.createDirectories(dir); Path out=dir.resolve("a11yguard-report.csv");
        StringBuilder s=new StringBuilder("framework,testName,url,standard,timestamp,status,type,impact,ruleId,description,help,helpUrl,target,html\n");
        for(AccessibilityResult r:rs){
            for(AccessibilityViolation v:r.violations()) line(s,r,"VIOLATION",v);
            for(AccessibilityViolation v:r.incomplete()) line(s,r,"INCOMPLETE",v);
            if(r.violations().isEmpty()&&r.incomplete().isEmpty()) s.append(ReportUtil.csv(r.framework())).append(",").append(ReportUtil.csv(r.testName())).append(",").append(ReportUtil.csv(r.url())).append(",").append(ReportUtil.csv(r.standard())).append(",").append(ReportUtil.csv(String.valueOf(r.timestamp()))).append(",PASS,,,,,,,,\n");
        }
        Files.writeString(out,s.toString(),StandardCharsets.UTF_8); return out;
    }
    private static void line(StringBuilder s,AccessibilityResult r,String type,AccessibilityViolation v){
        String status=type.equals("INCOMPLETE")?"REVIEW":"FAIL";
        s.append(ReportUtil.csv(r.framework())).append(',').append(ReportUtil.csv(r.testName())).append(',').append(ReportUtil.csv(r.url())).append(',').append(ReportUtil.csv(r.standard())).append(',').append(ReportUtil.csv(String.valueOf(r.timestamp()))).append(',').append(status).append(',').append(type).append(',').append(ReportUtil.csv(v.impact())).append(',').append(ReportUtil.csv(v.id())).append(',').append(ReportUtil.csv(v.description())).append(',').append(ReportUtil.csv(v.help())).append(',').append(ReportUtil.csv(v.helpUrl())).append(',').append(ReportUtil.csv(ReportUtil.target(v))).append(',').append(ReportUtil.csv(v.html())).append('\n');
    }
}
