package com.varlanv.snaptest;

public final class Constants {

    private Constants() {}

    public static final String VERSION = "0.0.1";
    public static final String SNAP_INIT_STRING = Constants.VERSION + "\n\n\n";
    public static final String OPERATION_PROPERTY = "com.varlanv.snaptest.operation";
    public static final String WORKDIR_PROPERTY = "com.varlanv.snaptest.workdir";
    public static final String SNAP_START_MARKER = "$$$-SNAP_$$$_TEST_$$$_START_$$$_INVOCATION-$$$";
    public static final int SNAP_START_MARKER_LEN = 46;
}
