package com.a11yguard.reporting;

import com.a11yguard.core.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.nio.file.*;
import java.util.*;

public final class SarifReport implements AccessibilityReport {
    private final ObjectMapper mapper=new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    @Override public ReportFormat format(){return ReportFormat.SARIF;}
    @Override public Path write(List<AccessibilityResult> rs,Path dir)throws Exception{
        Files.createDirectories(dir); Path out=dir.resolve("a11yguard-report.sarif");
        List<Map<String,Object>> results=new ArrayList<>();
        for(AccessibilityResult r:rs) for(AccessibilityViolation v:r.violations()){
            Map<String,Object> loc=new LinkedHashMap<>();
            loc.put("physicalLocation",Map.of("artifactLocation",Map.of("uri",r.url()==null?"":r.url())));
            Map<String,Object> m=new LinkedHashMap<>();
            m.put("ruleId",v.id()); m.put("level",level(v.impact()));
            m.put("message",Map.of("text",v.help()==null?v.description():v.help()));
            m.put("locations",List.of(loc));
            if (v.helpUrl()!=null && !v.helpUrl().isBlank()) m.put("helpUri",v.helpUrl());
            m.put("properties",Map.of("description",v.description()==null?"":v.description(),"target",ReportUtil.target(v),"html",v.html()==null?"":v.html(),"framework",r.framework(),"testName",r.testName()));
            results.add(m);
        }
        Map<String,Object> tool=Map.of("driver",Map.of("name","A11yGuard"));
        Map<String,Object> run=new LinkedHashMap<>(); run.put("tool",tool); run.put("results",results);
        Map<String,Object> root=Map.of("$schema","https://json.schemastore.org/sarif-2.1.0.json","version","2.1.0","runs",List.of(run));
        mapper.writeValue(out.toFile(),root); return out;
    }
    private static String level(String impact){if(impact==null)return"warning"; return switch(impact.toLowerCase()){case"critical","serious"->"error";default->"warning";};}
}
