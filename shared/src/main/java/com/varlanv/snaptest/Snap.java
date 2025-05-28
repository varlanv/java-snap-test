package com.varlanv.snaptest;

import java.util.concurrent.atomic.AtomicInteger;

public final class Snap {

    private final SnapExecutable snapExecutable;
    private final SnapFile snapFile;
    private final MemoizedSupplier<Operation> operationSupplier;
    private final SnapshotErrorSupplier exceptionSupplier;
    final int position;
    private final AtomicInteger counter = new AtomicInteger();

    Snap(
            SnapExecutable snapExecutable,
            SnapFile snapFile,
            MemoizedSupplier<Operation> operationSupplier,
            SnapshotErrorSupplier exceptionSupplier,
            int position) {
        this.snapExecutable = snapExecutable;
        this.snapFile = snapFile;
        this.operationSupplier = operationSupplier;
        this.exceptionSupplier = exceptionSupplier;
        this.position = position;
    }

    public void apply(String actual) {
        try {
            var count = counter.getAndIncrement();
            var operation = operationSupplier.get();
            if (operation == Operation.RECORD) {
                //                snapFile.recordAppend(actual, name, position);
            } else if (operation == Operation.VERIFY) {
                //                if (snapFile.assertions.isEmpty()) {
                //                    throw new AssertionError("There was no snapshot available");
                //                }
                //                var snapAssertions = snapFile.assertions.get(name);
                //                if (snapAssertions == null || snapAssertions.isEmpty() || snapAssertions.size() <=
                // count) {
                //                    throw new AssertionError("There was no snapshot available");
                //                }
                //                var snapAssertion = snapAssertions.get(count);
                //                var expected = snapAssertion.expected();
                //                if (!Objects.equals(actual, expected)) {
                //                    throw exceptionSupplier.get(
                //                            String.format(
                //                                    "Snapshots did not match:\nExpected:\n%s\nBut actual was:\n%s",
                // expected, actual),
                //                            expected,
                //                            actual);
                //                }
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
