package com.varlanv.snaptest;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.extension.*;

public final class JSnap implements ParameterResolver, TestInstancePreConstructCallback, AfterAllCallback {

    private final MemoizedSupplier<Snap.Operation> operation;
    private final MemoizedSupplier<Path> workDir;
    private final JSnapPreConstruct preConstruct;
    private final JSnapParameterResolver parameterResolver;
    private final JSnapAfterAll afterAll;

    static {
        System.setProperty(
                Constants.WORKDIR_PROPERTY,
                Paths.get("")
                        .resolve("src")
                        .resolve("test")
                        .resolve("resources")
                        .resolve("snaptest")
                        .toString());
        System.setProperty(Constants.OPERATION_PROPERTY, Snap.Operation.RECORD.name());
    }

    public JSnap() {
        this.operation = new MemoizedSupplier<>(() -> {
            var systemOperationProperty = System.getProperty(Constants.OPERATION_PROPERTY);
            if (systemOperationProperty == null) {
                throw new IllegalStateException(
                        String.format("System property %s is not set", Constants.OPERATION_PROPERTY));
            }
            return Snap.Operation.valueOf(systemOperationProperty.toUpperCase());
        });
        this.workDir = new MemoizedSupplier<>(() -> Paths.get(System.getProperty(Constants.WORKDIR_PROPERTY)));
        this.preConstruct = new JSnapPreConstruct(operation, workDir);
        this.parameterResolver = new JSnapParameterResolver(operation, preConstruct);
        this.afterAll = new JSnapAfterAll(preConstruct);
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        return parameterResolver.supportsParameter(parameterContext, extensionContext);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        return parameterResolver.resolveParameter(parameterContext, extensionContext);
    }

    @Override
    public void preConstructTestInstance(TestInstanceFactoryContext factoryContext, ExtensionContext context)
            throws Exception {
        preConstruct.preConstructTestInstance(factoryContext, context);
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        var topLevel = InternalJUnitUtils.findTopLevel(context);
        var ctx = preConstruct.cache.get(topLevel);
        if (ctx != null) {}
    }
}
