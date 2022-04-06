package androidx.reflect.lunarcalendar;

import androidx.reflect.SeslBaseReflector;
import androidx.reflect.SeslPathClassReflector;

import java.lang.reflect.Method;

import dalvik.system.PathClassLoader;

public class SeslSolarLunarConverterReflector {
    private static final String mClassName = "com.samsung.android.calendar.secfeature.lunarcalendar.SolarLunarConverter";

    private SeslSolarLunarConverterReflector() {
    }

    public static void convertLunarToSolar(PathClassLoader pathClassLoader, Object obj, int i, int i2, int i3, boolean z) {
        Method method = SeslPathClassReflector.getMethod(pathClassLoader, mClassName, "convertLunarToSolar", Integer.TYPE, Integer.TYPE, Integer.TYPE, Boolean.TYPE);
        if (method != null) {
            SeslBaseReflector.invoke(obj, method, Integer.valueOf(i), Integer.valueOf(i2), Integer.valueOf(i3), Boolean.valueOf(z));
        }
    }

    public static void convertSolarToLunar(PathClassLoader pathClassLoader, Object obj, int i, int i2, int i3) {
        Method method = SeslPathClassReflector.getMethod(pathClassLoader, mClassName, "convertSolarToLunar", Integer.TYPE, Integer.TYPE, Integer.TYPE);
        if (method != null) {
            SeslBaseReflector.invoke(obj, method, Integer.valueOf(i), Integer.valueOf(i2), Integer.valueOf(i3));
        }
    }

    public static int getWeekday(PathClassLoader pathClassLoader, Object obj, int i, int i2, int i3) {
        Method method = SeslPathClassReflector.getMethod(pathClassLoader, mClassName, "getWeekday", Integer.TYPE, Integer.TYPE, Integer.TYPE);
        if (method != null) {
            Object invoke = SeslBaseReflector.invoke(obj, method, Integer.valueOf(i), Integer.valueOf(i2), Integer.valueOf(i3));
            if (invoke instanceof Integer) {
                return ((Integer) invoke).intValue();
            }
        }
        return 0;
    }

    public static int getDayLengthOf(PathClassLoader pathClassLoader, Object obj, int i, int i2, boolean z) {
        Method method = SeslPathClassReflector.getMethod(pathClassLoader, mClassName, "getDayLengthOf", Integer.TYPE, Integer.TYPE, Boolean.TYPE);
        if (method == null) {
            return 30;
        }
        Object invoke = SeslBaseReflector.invoke(obj, method, Integer.valueOf(i), Integer.valueOf(i2), Boolean.valueOf(z));
        if (invoke instanceof Integer) {
            return ((Integer) invoke).intValue();
        }
        return 30;
    }

    public static int getYear(PathClassLoader pathClassLoader, Object obj) {
        Method method = SeslPathClassReflector.getMethod(pathClassLoader, mClassName, "getYear", new Class[0]);
        if (method == null) {
            return 2019;
        }
        Object invoke = SeslBaseReflector.invoke(obj, method, new Object[0]);
        if (invoke instanceof Integer) {
            return ((Integer) invoke).intValue();
        }
        return 2019;
    }

    public static int getMonth(PathClassLoader pathClassLoader, Object obj) {
        Method method = SeslPathClassReflector.getMethod(pathClassLoader, mClassName, "getMonth", new Class[0]);
        if (method == null) {
            return 10;
        }
        Object invoke = SeslBaseReflector.invoke(obj, method, new Object[0]);
        if (invoke instanceof Integer) {
            return ((Integer) invoke).intValue();
        }
        return 10;
    }

    public static int getDay(PathClassLoader pathClassLoader, Object obj) {
        Method method = SeslPathClassReflector.getMethod(pathClassLoader, mClassName, "getDay", new Class[0]);
        if (method == null) {
            return 19;
        }
        Object invoke = SeslBaseReflector.invoke(obj, method, new Object[0]);
        if (invoke instanceof Integer) {
            return ((Integer) invoke).intValue();
        }
        return 19;
    }

    public static boolean isLeapMonth(PathClassLoader pathClassLoader, Object obj) {
        Method method = SeslPathClassReflector.getMethod(pathClassLoader, mClassName, "isLeapMonth", new Class[0]);
        if (method != null) {
            Object invoke = SeslBaseReflector.invoke(obj, method, new Object[0]);
            if (invoke instanceof Boolean) {
                return ((Boolean) invoke).booleanValue();
            }
        }
        return false;
    }
}
