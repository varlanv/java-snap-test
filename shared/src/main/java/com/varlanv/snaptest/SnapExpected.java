package com.varlanv.snaptest;

import java.util.Objects;

final class SnapExpected {

    final Key key;
    final String expected;

    SnapExpected(Key key, String expected) {
        this.key = key;
        this.expected = expected;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SnapExpected)) {
            return false;
        }
        var that = (SnapExpected) o;
        return Objects.equals(key, that.key) && Objects.equals(expected, that.expected);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, expected);
    }

    @Override
    public String toString() {
        return "SnapExpected{" + "key=" + key + ", expected='" + expected + '\'' + '}';
    }

    static final class Key implements Comparable<Key> {

        final String id;
        final int positionInClass;
        final int iterationInMethod;
        final int countInTest;

        Key(String id, int positionInClass, int iterationInMethod, int countInTest) {
            this.id = id;
            this.positionInClass = positionInClass;
            this.iterationInMethod = iterationInMethod;
            this.countInTest = countInTest;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Key)) {
                return false;
            }
            var key = (Key) o;
            return positionInClass == key.positionInClass && iterationInMethod == key.iterationInMethod && countInTest == key.countInTest && Objects.equals(id, key.id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, positionInClass, iterationInMethod, countInTest);
        }

        @Override
        public String toString() {
            return "Key{" +
                "id='" + id + '\'' +
                ", positionInClass=" + positionInClass +
                ", iterationInMethod=" + iterationInMethod +
                ", countInTest=" + countInTest +
                '}';
        }

        @Override
        public int compareTo(SnapExpected.Key o) {
            var inClass = Integer.compare(positionInClass, o.positionInClass);
            if (inClass != 0) {
                return inClass;
            } else {
                var inMethod = Integer.compare(iterationInMethod, o.iterationInMethod);
                if (inMethod != 0) {
                    return inMethod;
                } else{
                    return Integer.compare(countInTest, o.countInTest);
                }
            }
        }
    }
}
