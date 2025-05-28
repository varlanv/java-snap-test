package com.varlanv.snaptest;

import java.util.Objects;

final class SnapExpected {

    final String id;
    final String expected;
    final int position;

    SnapExpected(String id, String expected, int position) {
        this.id = id;
        this.expected = expected;
        this.position = position;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        SnapExpected that = (SnapExpected) o;
        return position == that.position && Objects.equals(id, that.id) && Objects.equals(expected, that.expected);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, expected, position);
    }

    @Override
    public String toString() {
        return "SnapExpected{" + "id='" + id + '\'' + ", expected='" + expected + '\'' + ", position=" + position + '}';
    }
}
