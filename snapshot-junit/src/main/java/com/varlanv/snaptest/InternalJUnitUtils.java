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

    static int extractIterationFromId(String uniqueId) {
        var len = uniqueId.length();
        var result = 0;
        var multiplier = 1;
        for (var i = len - 1; i >= 0; i--) {
            char ch = uniqueId.charAt(i);
            if (ch == '#') {
                break;
            }
            if (ch >= '0' && ch <= '9') {
                var digit = ch - '0';
                result += digit * multiplier;
                multiplier *= 10;
            }
        }
        return result;
    }
}
