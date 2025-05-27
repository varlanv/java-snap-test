package com.varlanv.snaptest;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public final class Snap {

    private final String uniqueId;
    private final SnapFile snapFile;
    private final Operation operation;
    private final SnapshotErrorSupplier exceptionSupplier;
    private final AtomicInteger counter = new AtomicInteger();

    Snap(String uniqueId, SnapFile snapFile, Operation operation, SnapshotErrorSupplier exceptionSupplier) {
        this.uniqueId = uniqueId;
        this.snapFile = snapFile;
        this.operation = operation;
        this.exceptionSupplier = exceptionSupplier;
    }

    public void apply(String actual) {
        try {
            var count = counter.getAndIncrement();
            if (operation == Operation.RECORD) {
                snapFile.append(actual, uniqueId, count);
            } else if (operation == Operation.VERIFY) {
                if (snapFile.assertions.isEmpty()) {
                    throw new AssertionError("There was no snapshot available");
                }
                var snapAssertions = snapFile.assertions.get(uniqueId);
                if (snapAssertions == null || snapAssertions.isEmpty() || snapAssertions.size() <= count) {
                    throw new AssertionError("There was no snapshot available");
                }
                var snapAssertion = snapAssertions.get(count);
                var expected = snapAssertion.expected();
                if (!Objects.equals(actual, expected)) {
                    throw exceptionSupplier.get(
                            String.format(
                                    "Snapshots did not match:\nExpected:\n%s\nBut actual was:\n%s", expected, actual),
                            expected,
                            actual);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public enum Operation {
        RECORD,
        VERIFY
    }
}
