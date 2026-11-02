package com.a11yguard.junit5;

import com.a11yguard.annotation.Accessibility;
import com.a11yguard.runtime.A11yPipeline;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.lang.reflect.Method;

public final class A11yJUnitExtension implements AfterTestExecutionCallback {

    @Override
    public void afterTestExecution(ExtensionContext context) {
        Method method = context.getRequiredTestMethod();
        Accessibility annotation = method.getAnnotation(Accessibility.class);
        if (annotation == null) {
            annotation = context.getRequiredTestClass()
                .getAnnotation(Accessibility.class);
        }
        A11yPipeline.afterTest(
            context.getRequiredTestInstance(), method.getName(), annotation);
    }
}
