package com.varlanv.snaptest;

import java.io.IOException;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import org.junit.jupiter.api.*;
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
                var file = fileDirFinal.resolve(fileName);
                SnapFile snapFile;
                if (Files.notExists(file)) {
                    snapFile = SnapFile.init(file, new SnapFile.Content(Constants.VERSION, new TreeMap<>()));
                } else {
                    snapFile = SnapFile.read(file);
                }
                if (operation.get() == Snap.Operation.RECORD) {
                    var initialMessage = Constants.SNAP_INIT_STRING;
                    Files.writeString(
                            snapFile.path,
                            initialMessage,
                            StandardOpenOption.TRUNCATE_EXISTING,
                            StandardOpenOption.CREATE);
                }
                var sortedMethods = parseTopLevel(topLevelClass);
                var items = new Snap[sortedMethods.size()];
                var ctx = new Ctx(topLevelClass, snapFile, fileDir, Arrays.asList(items));
                var position = 0;
                for (var method : sortedMethods) {
                    var item = new Snap(method, ctx.file, operation, AssertionFailedError::new, position);
                    itemCache.put(method.executable, item);
                    items[position] = item;
                    position++;
                }
                cache.put(topLevelClass, ctx);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private SortedSet<SnapExecutable> parseTopLevel(Class<?> toplevelClass) {
        var nestMembers = toplevelClass.getNestMembers();
        var itemMethods = new TreeSet<SnapExecutable>(Comparator.comparing(it -> it.name));
        for (var nestMember : nestMembers) {
            if (nestMember == toplevelClass || nestMember.isAnnotationPresent(Nested.class)) {
                for (var method : nestMember.getDeclaredMethods()) {
                    var testType = resolveTestType(method);
                    if (testType != TestType.NONE) {
                        if (method.getParameterCount() > 0) {
                            var countSnapParams = 0;
                            for (var parameterType : method.getParameterTypes()) {
                                if (parameterType == Snap.class) {
                                    countSnapParams++;
                                    itemMethods.add(new SnapExecutable(method, method.toString(), testType));
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

    private TestType resolveTestType(Method method) {
        if (method.isAnnotationPresent(Test.class)) {
            return TestType.SIMPLE;
        } else if (method.isAnnotationPresent(ParameterizedTest.class)) {
            return TestType.PARAMETERIZED;
        } else if (method.isAnnotationPresent(TestFactory.class)) {
            return TestType.FACTORY;
        } else if (method.isAnnotationPresent(RepeatedTest.class)) {
            return TestType.REPEATED;
        } else if (method.isAnnotationPresent(TestTemplate.class)) {
            return TestType.TEMPLATE;
        } else {
            return TestType.NONE;
        }
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
