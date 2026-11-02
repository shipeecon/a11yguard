package com.a11yguard.testng;

import com.a11yguard.annotation.Accessibility;
import com.a11yguard.runtime.A11yPipeline;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;

import java.lang.reflect.Method;

public final class A11yTestNGListener implements IInvokedMethodListener {

    @Override
    public void afterInvocation(IInvokedMethod method, ITestResult result) {
        if (!method.isTestMethod()) return;

        Method javaMethod = result.getMethod()
            .getConstructorOrMethod().getMethod();

        Accessibility annotation = javaMethod.getAnnotation(Accessibility.class);
        if (annotation == null) {
            annotation = result.getTestClass()
                .getRealClass().getAnnotation(Accessibility.class);
        }
        A11yPipeline.afterTest(result.getInstance(), javaMethod.getName(), annotation);
    }
}
