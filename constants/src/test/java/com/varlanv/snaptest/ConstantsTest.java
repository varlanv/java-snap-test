package com.varlanv.snaptest;

import static org.assertj.core.api.Assertions.assertThat;

import com.varlanv.snaptest.commontest.FastTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ConstantsTest implements FastTest {

    @Test
    @DisplayName("marker length should match")
    void constants() {
        assertThat(Constants.SNAP_START_MARKER_LEN).isEqualTo(Constants.SNAP_START_MARKER.length());
    }
}
