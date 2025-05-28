package com.varlanv.snaptest;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

final class JSnapAfterAll implements AfterAllCallback {

    private final JSnapPreConstruct parent;

    JSnapAfterAll(JSnapPreConstruct parent) {
        this.parent = parent;
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        for (var cacheCtx : parent.cache.values()) {
            //            cacheCtx.file.save();
        }
    }
}
