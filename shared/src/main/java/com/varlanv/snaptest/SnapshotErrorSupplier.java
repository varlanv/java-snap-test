package com.varlanv.snaptest;

interface SnapshotErrorSupplier {

    Error get(String message, String expected, String actual);
}
