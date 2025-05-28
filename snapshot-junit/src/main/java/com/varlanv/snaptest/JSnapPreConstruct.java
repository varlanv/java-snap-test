package com.varlanv.snaptest;

import java.io.IOException;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstanceFactoryContext;
import org.junit.jupiter.api.extension.TestInstancePreConstructCallback;
import org.junit.jupiter.params.ParameterizedTest;
import org.opentest4j.AssertionFailedError;

final class JSnapPreConstruct implements TestInstancePreConstructCallback {

    private final MemoizedSupplier<Snap.Operation> operation;
    private final MemoizedSupplier<Path> workDir;
    final Map<Class<?>, Ctx> cache = new HashMap<>();
    final Map<Executable, Snap> itemCache = new HashMap<>();

    JSnapPreConstruct(MemoizedSupplier<Snap.Operation> operation, MemoizedSupplier<Path> workDir) {
        this.operation = operation;
        this.workDir = workDir;
    }

    @Override
    public void preConstructTestInstance(TestInstanceFactoryContext factoryContext, ExtensionContext context)
            throws Exception {
        var outerInstance = factoryContext.getOuterInstance();
        if (outerInstance.isEmpty()) {
            var topLevelClass = factoryContext.getTestClass();
            try {
                var fileName = topLevelClass.getName() + ".snaptest";
                var packageParts = topLevelClass.getPackageName().split("\\.");
                var fileDir = this.workDir.get();
                for (var packagePart : packageParts) {
                    fileDir = fileDir.resolve(packagePart);
                }
                Files.createDirectories(fileDir);
                var fileDirFinal = fileDir;
                var file = SnapFile.read(fileDirFinal.resolve(fileName));
                if (operation.get() == Snap.Operation.RECORD) {
                    var initialMessage = Constants.SNAP_INIT_STRING;
                    Files.writeString(
                            file.path, initialMessage, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
                }
                var sortedMethods = parseTopLevel(topLevelClass);
                var items = new Snap[sortedMethods.size()];
                var ctx = new Ctx(topLevelClass, file, fileDir, Arrays.asList(items));
                var position = 0;
                for (var method : sortedMethods) {
                    var item = new Snap(method.toString(), ctx.file, operation, AssertionFailedError::new, position);
                    itemCache.put(method, item);
                    items[position] = item;
                    position++;
                }
                cache.put(topLevelClass, ctx);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private SortedSet<Method> parseTopLevel(Class<?> toplevelClass) {
        var nestMembers = toplevelClass.getNestMembers();
        var itemMethods = new TreeSet<>(Comparator.comparing(Method::getName));
        for (var nestMember : nestMembers) {
            if (nestMember == toplevelClass || nestMember.isAnnotationPresent(Nested.class)) {
                for (var method : nestMember.getDeclaredMethods()) {
                    if (isTestMethod(method)) {
                        if (method.getParameterCount() > 0) {
                            var countSnapParams = 0;
                            for (var parameterType : method.getParameterTypes()) {
                                if (parameterType == Snap.class) {
                                    countSnapParams++;
                                    itemMethods.add(method);
                                }
                            }
                            if (countSnapParams > 1) {
                                throw new IllegalArgumentException("Only one Snap parameter is allowed");
                            }
                        }
                    }
                }
            }
        }
        return itemMethods;
    }

    private boolean isTestMethod(Method method) {
        return method.isAnnotationPresent(Test.class)
                || method.isAnnotationPresent(ParameterizedTest.class)
                || method.isAnnotationPresent(TestFactory.class);
    }

    static final class Ctx {

        final Class<?> topLevelClass;
        final SnapFile file;
        final Path fileDir;
        final List<Snap> items;

        Ctx(Class<?> topLevelClass, SnapFile file, Path fileDir, List<Snap> items) {
            this.topLevelClass = topLevelClass;
            this.file = file;
            this.fileDir = fileDir;
            this.items = items;
        }
    }
}
