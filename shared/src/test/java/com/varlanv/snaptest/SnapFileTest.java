package com.varlanv.snaptest;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.varlanv.snaptest.commontest.FastTest;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
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
                .isThrownBy(() -> SnapFile.init(path, new SnapFile.Content(Constants.VERSION, Map.of()))));
    }

    @Test
    @DisplayName("after init empty file, content should be same as provided")
    void after_init_empty_file_content_should_be_same_as_provided() {
        consumeTempFile(path -> {
            var expected = new SnapFile.Content(Constants.VERSION, Map.of());
            var subject = SnapFile.init(path, expected);

            assertThat(subject.content.version).isEqualTo(expected.version);
            assertThat(subject.content.assertions).isEqualTo(expected.assertions);
        });
    }

    @Test
    @DisplayName("when init and then read empty file, content should be same")
    void when_init_and_then_read_empty_file_content_should_be_same() {
        consumeTempFile(path -> {
            var expected = new SnapFile.Content(Constants.VERSION, Map.of());
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
            var expected = new SnapFile.Content(
                    Constants.VERSION, Map.of("id", List.of(new SnapExpected("id", expectedString, 0))));
            SnapFile.init(path, expected);
            var actual = SnapFile.read(path);

            assertThat(actual.content.version).isEqualTo(expected.version);
            assertThat(actual.content.assertions).isEqualTo(expected.assertions);
            System.out.println(Files.readString(path));
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
