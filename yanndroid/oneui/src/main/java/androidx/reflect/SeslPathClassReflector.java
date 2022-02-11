package androidx.reflect;

import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import dalvik.system.PathClassLoader;

public class SeslPathClassReflector {
    public static Class<?> getClass(PathClassLoader pathClassLoader, String str) {
        try {
            return Class.forName(str, true, pathClassLoader);
        } catch (ClassNotFoundException e) {
            Log.e("SeslPathClassReflector", "Fail to get class", e);
            return null;
        }
    }

    public static Method getMethod(PathClassLoader pathClassLoader, String str, String str2, Class<?>... clsArr) {
        Class<?> cls = getClass(pathClassLoader, str);
        if (cls == null) {
            return null;
        }
        try {
            return cls.getMethod(str2, clsArr);
        } catch (NoSuchMethodException e) {
            Log.e("SeslPathClassReflector", str2 + " NoSuchMethodException", e);
            return null;
        }
    }

    public static Field getField(PathClassLoader pathClassLoader, String str, String str2) {
        Class<?> cls = getClass(pathClassLoader, str);
        if (cls == null) {
            return null;
        }
        try {
            return cls.getField(str2);
        } catch (NoSuchFieldException e) {
            Log.e("SeslPathClassReflector", str2 + " NoSuchMethodException", e);
            return null;
        }
    }
}
