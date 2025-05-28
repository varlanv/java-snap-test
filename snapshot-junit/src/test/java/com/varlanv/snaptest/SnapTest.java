package com.varlanv.snaptest;

import com.varlanv.snaptest.commontest.FastTest;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

@ExtendWith(JSnap.class)
class SnapTest implements FastTest {

    @ParameterizedTest(name = "kek {0} {0}")
    @ValueSource(ints = {5, 10, 15, 20})
    @DisplayName("parameterized {0} {0}")
    void parameterized_0_0(int i, Snap snap) {
        snap.apply(String.valueOf(i));
    }

    @MethodSource
    @ParameterizedTest
    @DisplayName("parameterized string")
    void parameterized_string(String expectedString, Snap snap) {
        snap.apply(expectedString);
    }

    Stream<String> parameterized_string() {
        return IntStream.rangeClosed(1, 10).mapToObj(i -> String.valueOf(i).repeat(10));
    }

    @TestFactory
    @DisplayName("whatever")
    Stream<DynamicTest> whatever(Snap snap) {
        return IntStream.rangeClosed(1, 10)
                .mapToObj(i -> String.valueOf(i).repeat(10))
                .map(string -> DynamicTest.dynamicTest(string, () -> snap.apply(string)));
    }

    @Test
    @DisplayName("multiline string")
    void multiline_string(Snap snap) {
        snap.apply(
                """
                Some java
                Multi
                Line string -- \r

                with \n\r
                additional
                crlfs


                like so
                """);
    }

    @Nested
    class Nested1Test implements FastTest {

        @Test
        @DisplayName("nested1")
        void nested1(Snap snap) {
            snap.apply("asd");
            snap.apply("\n\n");
            snap.apply("\n");
            snap.apply("\r\n");
            snap.apply("\nasdad\r");
            snap.apply("\nasd\nad\r\n");
        }

        @Nested
        class Nested2Test implements FastTest {

            @Test
            @DisplayName("nested2")
            void nested2(Snap snap) {
                snap.apply("asd");
                snap.apply("\n\n");
                snap.apply("\n");
                snap.apply("\r\n");
                snap.apply("\nasdad\r");
                snap.apply("\nasd\nad\r\n");
            }

            @Test
            @DisplayName("nested3")
            void nested3() {}
        }
    }
}
