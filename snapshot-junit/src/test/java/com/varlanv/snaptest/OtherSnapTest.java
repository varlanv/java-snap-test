package com.varlanv.snaptest;

import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@ExtendWith(JSnap.class)
class OtherSnapTest {

    @TestFactory
    Stream<DynamicTest> dynamic(Snap snap) {
        return IntStream.range(0, 10)
                .mapToObj(i -> String.valueOf(i).repeat(10))
                .map(string -> DynamicTest.dynamicTest(string, () -> snap.apply(string)));
    }

    @ParameterizedTest
    @ValueSource(ints = {5, 10, 15, 20})
    @DisplayName("parameterized")
    void parameterized(int i, Snap snap) {
        snap.apply(String.valueOf(i));
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
    class Nested1Test {

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
        class Nested2Test {

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
