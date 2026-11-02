package com.a11yguard.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Inherited
public @interface Accessibility {
    boolean value() default true;
    String standard() default "WCAG2AA";
    String[] failOn() default {"CRITICAL", "SERIOUS"};
    String[] tags() default {"wcag2a", "wcag2aa"};
    String[] exclude() default {};
    String[] reportFormats() default {"HTML", "JSON", "JUNIT_XML", "SARIF", "CSV", "MARKDOWN"};
    String reportDir() default "target/a11yguard-report";
    boolean screenshots() default true;
    String baseline() default "";
    boolean updateBaseline() default false;
}
