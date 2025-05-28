package com.varlanv.snaptest;

import java.lang.reflect.Executable;

final class SnapExecutable {

    final Executable executable;
    final String name;
    final TestType testType;

    SnapExecutable(Executable executable, String name, TestType testType) {
        this.executable = executable;
        this.name = name;
        this.testType = testType;
    }
}
