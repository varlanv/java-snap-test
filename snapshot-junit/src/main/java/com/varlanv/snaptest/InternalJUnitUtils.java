package com.varlanv.snaptest;

import org.junit.jupiter.api.extension.ExtensionContext;

interface InternalJUnitUtils {

    static Class<?> findTopLevel(ExtensionContext context) {
        var prevContext = context;
        var parentContext = context.getParent();
        while (true) {
            var parent = parentContext.orElseThrow().getParent();
            if (parent.isEmpty()) {
                return prevContext.getTestClass().orElseThrow();
            }
            prevContext = parentContext.get();
            parentContext = parent;
        }
    }
}
