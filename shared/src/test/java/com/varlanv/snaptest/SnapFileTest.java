package com.varlanv.snaptest;

import static org.assertj.core.api.Assertions.*;

import com.varlanv.snaptest.commontest.FastTest;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class SnapFileTest implements FastTest {

    @Test
    @DisplayName("`read` should throw exception when file not exists")
    void read_should_throw_exception_when_file_not_exists() {
        var path = Paths.get("123qwAasdZzx1$c5.1123wekiCnrtXfo4weAasasdP");
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> SnapFile.read(path))
                .withMessage(
                        "Snaptest file does not exist: %s",
                        path.toAbsolutePath().toString());
    }

    @Test
    @DisplayName("should be able to init file without content")
    void should_be_able_to_init_file_without_content() {
        consumeTempFile(path -> assertThatNoException()
                .isThrownBy(() -> SnapFile.init(path, new SnapFile.Content(Constants.VERSION, new TreeMap<>()))));
    }

    @Test
    @DisplayName("after init empty file, content should be same as provided")
    void after_init_empty_file_content_should_be_same_as_provided() {
        consumeTempFile(path -> {
            var expected = new SnapFile.Content(Constants.VERSION, new TreeMap<>());
            var subject = SnapFile.init(path, expected);

            assertThat(subject.content.version).isEqualTo(expected.version);
            assertThat(subject.content.assertions).isEqualTo(expected.assertions);
        });
    }

    @Test
    @DisplayName("when init and then read empty file, content should be same")
    void when_init_and_then_read_empty_file_content_should_be_same() {
        consumeTempFile(path -> {
            var expected = new SnapFile.Content(Constants.VERSION, new TreeMap<>());
            SnapFile.init(path, expected);
            var actual = SnapFile.read(path);

            assertThat(actual.content.version).isEqualTo(expected.version);
            assertThat(actual.content.assertions).isEqualTo(expected.assertions);
        });
    }

    @Test
    @DisplayName("when many positions are written under different id, should read all")
    void when_many_positions_are_written_under_different_id_should_read_all() {
        consumeTempFile(path -> {
            var expectedMap = new TreeMap<SnapExpected.Key, List<SnapExpected>>();
            var count = 10;
            for (var i = 0; i < count; i++) {
                var key = new SnapExpected.Key("id" + i, i, 0);
                expectedMap.put(key, List.of(new SnapExpected(key, "some string" + i)));
            }
            var expected = new SnapFile.Content(Constants.VERSION, expectedMap);
            SnapFile.init(path, expected);
            var actual = SnapFile.read(path);

            assertThat(actual.content.version).isEqualTo(expected.version);
            assertThat(actual.content.assertions).isEqualTo(expected.assertions);
        });
    }

    @Test
    @DisplayName("should be able to overwrite single position in file")
    void should_be_able_to_overwrite_single_position_in_file() {
        consumeTempFile(path -> {
            var key = new SnapExpected.Key("id", 0, 0);
            var expectedBefore = "some string";
            var expectedAfter = "changed value";
            var expectedMap = new TreeMap<>(Map.of(key, List.of(new SnapExpected(key, expectedBefore))));
            SnapFile.init(path, new SnapFile.Content(Constants.VERSION, expectedMap));
            var file = SnapFile.read(path);
            file.recordAppend(new SnapExpected(key, expectedAfter));
            var actual = SnapFile.read(path);

            //            assertThat(actual.content.version).isEqualTo(expected.version);
            //            assertThat(actual.content.assertions).isEqualTo(expected.assertions);
        });
    }

    @ParameterizedTest
    @MethodSource("contentStrings")
    @DisplayName("when many positions are written under different id, should read all parameterized")
    void when_many_positions_are_written_under_different_id_should_read_all_parameterized(String expectedString) {
        consumeTempFile(path -> {
            var expectedMap = new TreeMap<SnapExpected.Key, List<SnapExpected>>();
            var count = 10;
            for (var i = 0; i < count; i++) {
                var key = new SnapExpected.Key("id" + i, i, 0);
                expectedMap.put(key, List.of(new SnapExpected(key, expectedString)));
            }
            var expected = new SnapFile.Content(Constants.VERSION, expectedMap);
            SnapFile.init(path, expected);
            var actual = SnapFile.read(path);

            assertThat(actual.content.version).isEqualTo(expected.version);
            assertThat(actual.content.assertions).isEqualTo(expected.assertions);
        });
    }

    @ParameterizedTest
    @MethodSource("contentStrings")
    @DisplayName("when many positions are written under different id at position `1`, should read all parameterized")
    void when_many_positions_are_written_under_different_id_at_position_1_should_read_all_parameterized(String expectedString) {
        consumeTempFile(path -> {
            var expectedMap = new TreeMap<SnapExpected.Key, List<SnapExpected>>();
            var count = 10;
            var position = 1;
            for (var i = 0; i < count; i++) {
                var key = new SnapExpected.Key("id" + i, i, position);
                expectedMap.put(key, List.of(new SnapExpected(key, expectedString)));
            }
            var expected = new SnapFile.Content(Constants.VERSION, expectedMap);
            SnapFile.init(path, expected);
            var actual = SnapFile.read(path);

            assertThat(actual.content.version).isEqualTo(expected.version);
            assertThat(actual.content.assertions).isEqualTo(expected.assertions);
        });
    }

    @ParameterizedTest
    @MethodSource("contentStrings")
    @DisplayName("when init and then read file with one element, content should be same")
    void when_init_and_then_read_file_with_one_element_content_should_be_same(String expectedString) {
        consumeTempFile(path -> {
            var key = new SnapExpected.Key("id", 0, 0);
            var expected = new SnapFile.Content(
                    Constants.VERSION, new TreeMap<>(Map.of(key, List.of(new SnapExpected(key, expectedString)))));
            SnapFile.init(path, expected);
            var actual = SnapFile.read(path);

            assertThat(actual.content.version).isEqualTo(expected.version);
            assertThat(actual.content.assertions).isEqualTo(expected.assertions);
        });
    }

    @ParameterizedTest
    @MethodSource("contentStrings")
    @DisplayName("when init and then read file with one element and position `1`, content should be same")
    void when_init_and_then_read_file_with_one_element_and_position_1_content_should_be_same(String expectedString) {
        consumeTempFile(path -> {
            var iteration = 1;
            var key = new SnapExpected.Key("id", iteration, iteration);
            var expected = new SnapFile.Content(
                    Constants.VERSION, new TreeMap<>(Map.of(key, List.of(new SnapExpected(key, expectedString)))));
            SnapFile.init(path, expected);
            var actual = SnapFile.read(path);

            assertThat(actual.content.version).isEqualTo(expected.version);
            assertThat(actual.content.assertions).isEqualTo(expected.assertions);
        });
    }

    static Stream<String> contentStrings() {
        return Stream.of(
                "\r",
                "\n",
                "\r\n",
                "\r\r\r\r",
                "\n\n\n\n\n",
                "\r\n\r\n\n\n\r",
                "some string",
                "\n some \n string",
                "1234$12sm a,sa, M:sd ]a daq we qw[]e ';",
                "\nstr\nstr\n",
                "\r\nstr\nstr\r\n",
                "\r\nstr\nstr\n",
                "\nstr\nstr\r\n");
    }
}
