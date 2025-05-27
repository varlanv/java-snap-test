package com.varlanv.snaptest.commontest;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.immutables.value.Value;

@Value.Immutable(builder = false)
public interface DataTables {

    @Value.Parameter
    List<Boolean> isCiList();

    @Value.Parameter
    List<Boolean> configurationCacheList();

    @Value.Parameter
    List<Boolean> buildCacheList();

    @Value.Parameter
    List<String> gradleVersions();

    static Stream<DataTable> streamDefault() {
        return getDefault().list().stream();
    }

    static DataTables getDefault() {
        //        if (Objects.equals(System.getenv("CI"), "true")) {
        //        if (true) {
        return ImmutableDataTables.of(
                List.of(false), List.of(false), List.of(false), List.of(TestGradleVersions.current()));
        //        } else {
        //            return new DataTables(
        //                List.of(true, false),
        //                List.of(true, false),
        //                List.of(true, false),
        //                TestGradleVersions.list()
        //            );
    }

    default List<DataTable> list() {
        List<DataTable> result = new ArrayList<>();
        gradleVersions().forEach(gradleVersion -> isCiList()
                .forEach(isCi -> configurationCacheList().forEach(configurationCache -> buildCacheList()
                        .forEach(buildCache ->
                                result.add(new DataTable(isCi, configurationCache, buildCache, gradleVersion))))));
        return result;
    }
}
