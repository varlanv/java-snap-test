package com.varlanv.snaptest;

import java.util.function.Supplier;
import org.jspecify.annotations.Nullable;

final class MemoizedSupplier<T> {

    private final Supplier<T> delegate;

    @Nullable private T value;

    MemoizedSupplier(Supplier<T> delegate) {
        this.delegate = delegate;
    }

    T get() {
        T val = value;
        if (val == null) {
            val = delegate.get();
            value = val;
        }
        return val;
    }
}
