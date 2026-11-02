package com.a11yguard.reporting;

import com.a11yguard.core.AccessibilityResult;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Main reporting facade. Reports are generated after every scan and also
 * aggregate all results recorded in the current JVM.
 */
public final class AccessibilityReporter {
    private static final List<AccessibilityResult> RESULTS = new CopyOnWriteArrayList<>();
    private static final Map<ReportFormat, AccessibilityReport> WRITERS = Map.of(
        ReportFormat.HTML,new HtmlReport(), ReportFormat.JSON,new JsonReport(),
        ReportFormat.JUNIT_XML,new JunitXmlReport(), ReportFormat.SARIF,new SarifReport(),
        ReportFormat.CSV,new CsvReport(), ReportFormat.MARKDOWN,new MarkdownReport()
    );
    private AccessibilityReporter(){}

    public static synchronized List<Path> report(AccessibilityResult result,
                                                   Path outputDirectory,
                                                   Set<ReportFormat> formats) {
        RESULTS.add(result);
        return write(RESULTS, outputDirectory, formats);
    }

    public static synchronized List<Path> write(List<AccessibilityResult> results,
                                                 Path outputDirectory,
                                                 Set<ReportFormat> formats) {
        try {
            List<Path> paths=new ArrayList<>();
            Set<ReportFormat> requested=(formats==null||formats.isEmpty())?EnumSet.of(ReportFormat.HTML,ReportFormat.JSON):formats;
            for(ReportFormat f:requested){
                AccessibilityReport writer=WRITERS.get(f);
                if(writer==null) throw new IllegalArgumentException("Unsupported report format: "+f);
                paths.add(writer.write(List.copyOf(results),outputDirectory));
            }
            return paths;
        } catch(Exception e){ throw new RuntimeException("A11yGuard report generation failed",e); }
    }

    public static List<AccessibilityResult> results(){return List.copyOf(RESULTS);}
    public static void clear(){RESULTS.clear();}
}
