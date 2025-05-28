package com.varlanv.snaptest;

import java.lang.reflect.Method;
import org.junit.jupiter.api.extension.*;

final class JSnapParameterResolver implements ParameterResolver, InvocationInterceptor {

    private final MemoizedSupplier<Snap.Operation> operation;
    private final JSnapPreConstruct preConstruct;

    public JSnapParameterResolver(MemoizedSupplier<Snap.Operation> operation, JSnapPreConstruct preConstruct) {
        this.preConstruct = preConstruct;
        this.operation = operation;
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        if (parameterContext.getParameter().getType() == Snap.class) {
            var count = 0;
            for (var parameter : parameterContext.getDeclaringExecutable().getParameters()) {
                if (parameter.getType() == Snap.class) {
                    count++;
                }
            }
            if (count > 1) {
                throw new IllegalArgumentException("Only one Snap parameter is allowed");
            }
            return true;
        }
        return false;
    }

    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        return preConstruct.itemCache.get(parameterContext.getDeclaringExecutable());
    }

    @Override
    public void interceptTestMethod(
            Invocation<Void> invocation,
            ReflectiveInvocationContext<Method> invocationContext,
            ExtensionContext extensionContext)
            throws Throwable {
        invocation.proceed();
    }

    @Override
    public <T> T interceptTestFactoryMethod(
            Invocation<T> invocation,
            ReflectiveInvocationContext<Method> invocationContext,
            ExtensionContext extensionContext)
            throws Throwable {
        return invocation.proceed();
    }

    @Override
    public void interceptTestTemplateMethod(
            Invocation<Void> invocation,
            ReflectiveInvocationContext<Method> invocationContext,
            ExtensionContext extensionContext)
            throws Throwable {
        invocation.proceed();
    }

    @Override
    public void interceptDynamicTest(
            Invocation<Void> invocation,
            DynamicTestInvocationContext invocationContext,
            ExtensionContext extensionContext)
            throws Throwable {
        invocation.proceed();
    }
}
