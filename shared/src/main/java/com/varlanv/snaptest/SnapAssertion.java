package com.varlanv.snaptest;

final class SnapAssertion {

    private final String uniqueId;
    private final String expected;

    SnapAssertion(String uniqueId, String expected) {
        this.uniqueId = uniqueId;
        this.expected = expected;
    }

    String expected() {
        return expected;
    }
}
