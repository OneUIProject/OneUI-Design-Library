package androidx.reflect;

import android.util.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class SeslBaseReflector {
    private static final String TAG = "SeslBaseReflector";

    private SeslBaseReflector() {
    }

    public static Class<?> getClass(String str) {
        try {
            return Class.forName(str);
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "Fail to get class", e);
            return null;
        }
    }

    public static Method getMethod(String str, String str2, Class<?>... clsArr) {
        if (str == null || str2 == null) {
            Log.d(TAG, "className = " + str + ", methodName = " + str2);
            return null;
        }
        Class<?> cls = getClass(str);
        if (cls == null) {
            return null;
        }
        try {
            return cls.getMethod(str2, clsArr);
        } catch (NoSuchMethodException e) {
            Log.e(TAG, str2 + " NoSuchMethodException", e);
            return null;
        }
    }

    public static <T> Method getMethod(Class<T> cls, String str, Class<?>... clsArr) {
        if (cls == null || str == null) {
            Log.d(TAG, "classT = " + cls + ", methodName = " + str);
            return null;
        }
        try {
            return cls.getMethod(str, clsArr);
        } catch (NoSuchMethodException e) {
            Log.e(TAG, str + " NoSuchMethodException", e);
            return null;
        }
    }

    public static Method getDeclaredMethod(String str, String str2, Class<?>... clsArr) {
        Method method = null;
        if (str == null || str2 == null) {
            Log.d(TAG, "className = " + str + ", methodName = " + str2);
            return null;
        }
        Class<?> cls = getClass(str);
        if (cls != null) {
            try {
                method = cls.getDeclaredMethod(str2, clsArr);
                if (method != null) {
                    method.setAccessible(true);
                }
            } catch (NoSuchMethodException e) {
                Log.e(TAG, str2 + " NoSuchMethodException", e);
            }
        }
        return method;
    }

    public static <T> Method getDeclaredMethod(Class<T> cls, String str, Class<?>... clsArr) {
        Method method = null;
        if (cls == null || str == null) {
            Log.d(TAG, "classT = " + cls + ", methodName = " + str);
            return null;
        }
        try {
            method = cls.getDeclaredMethod(str, clsArr);
            if (method != null) {
                method.setAccessible(true);
            }
        } catch (NoSuchMethodException e) {
            Log.e(TAG, str + " NoSuchMethodException", e);
        }
        return method;
    }

    public static Object invoke(Object obj, Method method, Object... objArr) {
        if (method == null) {
            Log.d(TAG, "method is null");
            return null;
        }
        try {
            return method.invoke(obj, objArr);
        } catch (IllegalAccessException e) {
            Log.e(TAG, method.getName() + " IllegalAccessException", e);
            return null;
        } catch (IllegalArgumentException e2) {
            Log.e(TAG, method.getName() + " IllegalArgumentException", e2);
            return null;
        } catch (InvocationTargetException e3) {
            Log.e(TAG, method.getName() + " InvocationTargetException", e3);
            return null;
        }
    }

    public static Field getField(String str, String str2) {
        if (str == null || str2 == null) {
            Log.d(TAG, "className = " + str + ", fieldName = " + str2);
            return null;
        }
        Class<?> cls = getClass(str);
        if (cls == null) {
            return null;
        }
        try {
            return cls.getField(str2);
        } catch (NoSuchFieldException e) {
            Log.e(TAG, str2 + " NoSuchMethodException", e);
            return null;
        }
    }

    public static <T> Field getField(Class<T> cls, String str) {
        if (cls == null || str == null) {
            Log.d(TAG, "classT = " + cls + ", fieldName = " + str);
            return null;
        }
        try {
            return cls.getField(str);
        } catch (NoSuchFieldException e) {
            Log.e(TAG, str + " NoSuchMethodException", e);
            return null;
        }
    }

    public static Field getDeclaredField(String str, String str2) {
        Field field = null;
        if (str == null || str2 == null) {
            Log.d(TAG, "className = " + str + ", fieldName = " + str2);
            return null;
        }
        Class<?> cls = getClass(str);
        if (cls != null) {
            try {
                field = cls.getDeclaredField(str2);
                if (field != null) {
                    field.setAccessible(true);
                }
            } catch (NoSuchFieldException e) {
                Log.e(TAG, str2 + " NoSuchMethodException", e);
            }
        }
        return field;
    }

    public static <T> Field getDeclaredField(Class<T> cls, String str) {
        Field field = null;
        if (cls == null || str == null) {
            Log.d(TAG, "classT = " + cls + ", fieldName = " + str);
            return null;
        }
        try {
            field = cls.getDeclaredField(str);
            if (field != null) {
                field.setAccessible(true);
            }
        } catch (NoSuchFieldException e) {
            Log.e(TAG, str + " NoSuchMethodException", e);
        }
        return field;
    }

    public static Object get(Object obj, Field field) {
        if (field == null) {
            Log.e(TAG, "field is null");
            return null;
        }
        try {
            return field.get(obj);
        } catch (IllegalAccessException e) {
            Log.e(TAG, field.getName() + " IllegalAccessException", e);
            return null;
        } catch (IllegalArgumentException e2) {
            Log.e(TAG, field.getName() + " IllegalArgumentException", e2);
            return null;
        }
    }

    public static void set(Object obj, Field field, Object obj2) {
        if (field == null) {
            Log.e(TAG, "field is null");
            return;
        }
        try {
            field.set(obj, obj2);
        } catch (IllegalAccessException e) {
            Log.e(TAG, field.getName() + " IllegalAccessException", e);
        } catch (IllegalArgumentException e2) {
            Log.e(TAG, field.getName() + " IllegalArgumentException", e2);
        }
    }

    public static Constructor<?> getConstructor(String str, Class<?>... clsArr) {
        try {
            return Class.forName(str).getDeclaredConstructor(clsArr);
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        }
    }
}
