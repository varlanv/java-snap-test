package com.varlanv.snaptest;

import java.lang.reflect.Executable;

interface InternalUtil {

    static Class<?> findTopLevelClass(Executable executable) {
        var result = executable.getDeclaringClass();
        while (true) {
            var tmp = result.getDeclaringClass();
            if (tmp == null) {
                return result;
            }
            result = tmp;
        }
    }
}
