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
        return "SnapExpected{" +
            "key=" + key +
            ", expected='" + expected + '\'' +
            '}';
    }

    static final class Key implements Comparable<Key> {

        final String id;
        final int position;

        Key(String id, int position) {
            this.id = id;
            this.position = position;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Key)) {
                return false;
            }
            var key = (Key) o;
            return position == key.position && Objects.equals(id, key.id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, position);
        }

        @Override
        public String toString() {
            return "Key{" +
                "id='" + id + '\'' +
                ", position=" + position +
                '}';
        }

        @Override
        public int compareTo(SnapExpected.Key o) {
            return Integer.compare(position, o.position);
        }
    }
}
