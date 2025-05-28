package com.varlanv.snaptest;

final class SnapAppend {

    final SnapExpected snapExpected;
    final int count;

    SnapAppend(SnapExpected snapExpected, int count) {
        this.snapExpected = snapExpected;
        this.count = count;
    }
}
