package androidx.reflect.content.res;

import android.content.res.Resources;

import androidx.reflect.SeslBaseReflector;

import java.lang.reflect.Method;

public class SeslResourcesReflector {
    private static final Class<?> mClass = Resources.class;

    private SeslResourcesReflector() {
    }

    static Object getCompatibilityInfo(Resources resources) {
        Method method = SeslBaseReflector.getMethod(mClass, "getCompatibilityInfo", new Class[0]);
        if (method == null) {
            return null;
        }
        Object invoke = SeslBaseReflector.invoke(resources, method, new Object[0]);
        if (invoke.getClass().getName().equals("android.content.res.CompatibilityInfo")) {
            return invoke;
        }
        return null;
    }
}
