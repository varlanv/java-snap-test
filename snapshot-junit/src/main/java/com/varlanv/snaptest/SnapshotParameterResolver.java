package com.varlanv.snaptest;

import java.io.IOException;
import java.lang.reflect.Executable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.opentest4j.AssertionFailedError;

public final class SnapshotParameterResolver implements ParameterResolver {

    private final ConcurrentHashMap<Class<?>, Ctx> cache = new ConcurrentHashMap<>();
    private final MemoizedSupplier<Snap.Operation> operation;
    private final MemoizedSupplier<Path> workDir;

    private static final class Ctx {

        private final SnapFile snapFile;
        private final Path fileDir;

        private Ctx(SnapFile snapFile, Path fileDir) {
            this.snapFile = snapFile;
            this.fileDir = fileDir;
        }
    }

    public SnapshotParameterResolver() {
        operation = new MemoizedSupplier<>(() -> {
            var systemOperationProperty = System.getProperty(Constants.OPERATION_PROPERTY);
            if (systemOperationProperty == null) {
                throw new IllegalStateException(
                        String.format("System property %s is not set", Constants.OPERATION_PROPERTY));
            }
            return Snap.Operation.valueOf(systemOperationProperty.toUpperCase());
        });
        workDir = new MemoizedSupplier<>(() -> Paths.get(System.getProperty(Constants.WORKDIR_PROPERTY)));
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

    private static Class<?> findTopLevelClass(Executable executable) {
        var result = executable.getDeclaringClass();
        while (true) {
            var tmp = result.getDeclaringClass();
            if (tmp == null) {
                return result;
            }
            result = tmp;
        }
    }

    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        var topLevelClass = findTopLevelClass(parameterContext.getDeclaringExecutable());
        var ctx = cache.computeIfAbsent(topLevelClass, k -> {
            try {
                var fileName = topLevelClass.getName() + ".snaptest";
                var packageParts = topLevelClass.getPackageName().split("\\.");
                var workDir = this.workDir.get();
                var fileDir = workDir;
                for (var packagePart : packageParts) {
                    fileDir = fileDir.resolve(packagePart);
                }
                Files.createDirectories(fileDir);
                var fileDirFinal = fileDir;
                var file = SnapFile.read(workDir, fileDirFinal, fileName, fileDirFinal.resolve(fileName));
                if (operation.get() == Snap.Operation.RECORD) {
                    var initialMessage = Constants.SNAP_INIT_STRING;
                    Files.writeString(
                            file.file, initialMessage, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
                }
                return new Ctx(file, fileDir);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        return new Snap(extensionContext.getUniqueId(), ctx.snapFile, operation.get(), AssertionFailedError::new);
    }
}
