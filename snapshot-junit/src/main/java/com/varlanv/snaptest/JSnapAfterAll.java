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
        var topLevel = InternalJUnitUtils.findTopLevel(context);
        var ctx = parent.cache.get(topLevel);
        if (ctx != null) {
            ctx.file.save();
        }
    }
}
