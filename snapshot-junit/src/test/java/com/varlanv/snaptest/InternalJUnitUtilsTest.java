package com.varlanv.snaptest;

import static org.assertj.core.api.Assertions.assertThat;

import com.varlanv.snaptest.commontest.FastTest;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

class InternalJUnitUtilsTest implements FastTest {

    @TestFactory
    @DisplayName("`extractIterationFromId` should be able to extract id")
    Stream<DynamicTest> extractIterationFromId() {
        return Stream.of(
                        Map.entry(
                                "[engine:junit-jupiter]/[class:com.varlanv.snaptest.SnapTest]/[test-template:parameterized_string(java.lang.String, com.varlanv.snaptest.Snap)]/[test-template-invocation:#7]",
                                7),
                        Map.entry(
                                "[engine:junit-jupiter]/[class:com.varlanv.snaptest.SnapTest]/[test-template:parameterized_string(java.lang.String, com.varlanv.snaptest.Snap)]/[test-template-invocation:#1]",
                                1),
                        Map.entry(
                                "[engine:junit-jupiter]/[class:com.varlanv.snaptest.SnapTest]/[test-template:parameterized_string(java.lang.String, com.varlanv.snaptest.Snap)]/[test-template-invocation:#0]",
                                0),
                        Map.entry(
                                "[engine:junit-jupiter]/[class:com.varlanv.snaptest.SnapTest]/[test-template:parameterized_string(java.lang.String, com.varlanv.snaptest.Snap)]/[test-template-invocation:#10]",
                                10),
                        Map.entry(
                                "[engine:junit-jupiter]/[class:com.varlanv.snaptest.SnapTest]/[test-template:parameterized_string(java.lang.String, com.varlanv.snaptest.Snap)]/[test-template-invocation:#100]",
                                100),
                        Map.entry(
                                "[engine:junit-jupiter]/[class:com.varlanv.snaptest.SnapTest]/[test-template:parameterized_string(java.lang.String, com.varlanv.snaptest.Snap)]/[test-template-invocation:#1000]",
                                1000),
                        Map.entry(
                                "[engine:junit-jupiter]/[class:com.varlanv.snaptest.SnapTest]/[test-template:parameterized_string(java.lang.String, com.varlanv.snaptest.Snap)]/[test-template-invocation:#42]",
                                42))
                .map(entry -> DynamicTest.dynamicTest(
                        entry.getKey(), () -> assertThat(InternalJUnitUtils.extractIterationFromId(entry.getKey()))
                                .isEqualTo(entry.getValue())));
    }
}
