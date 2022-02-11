package androidx.reflect.lunarcalendar;

import androidx.reflect.SeslBaseReflector;
import androidx.reflect.SeslPathClassReflector;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import dalvik.system.PathClassLoader;

public class SeslSolarLunarTablesReflector {
    public static int getDayLengthOf(PathClassLoader pathClassLoader, Object obj, int i, int i2, boolean z) {
        Class cls = Integer.TYPE;
        Method method = SeslPathClassReflector.getMethod(pathClassLoader, "com.samsung.android.calendar.secfeature.lunarcalendar.SolarLunarTables", "getDayLengthOf", cls, cls, Boolean.TYPE);
        if (method == null) {
            return 29;
        }
        Object invoke = SeslBaseReflector.invoke(obj, method, Integer.valueOf(i), Integer.valueOf(i2), Boolean.valueOf(z));
        if (invoke instanceof Integer) {
            return ((Integer) invoke).intValue();
        }
        return 29;
    }

    public static boolean isLeapMonth(PathClassLoader pathClassLoader, Object obj, int i, int i2) {
        Class cls = Integer.TYPE;
        Method method = SeslPathClassReflector.getMethod(pathClassLoader, "com.samsung.android.calendar.secfeature.lunarcalendar.SolarLunarTables", "isLeapMonth", cls, cls);
        if (method != null) {
            Object invoke = SeslBaseReflector.invoke(obj, method, Integer.valueOf(i), Integer.valueOf(i2));
            if (invoke instanceof Boolean) {
                return ((Boolean) invoke).booleanValue();
            }
        }
        return false;
    }

    public static byte getLunar(PathClassLoader pathClassLoader, Object obj, int i) {
        Method method = SeslPathClassReflector.getMethod(pathClassLoader, "com.samsung.android.calendar.secfeature.lunarcalendar.SolarLunarTables", "getLunar", Integer.TYPE);
        if (method == null) {
            return Byte.MAX_VALUE;
        }
        Object invoke = SeslBaseReflector.invoke(obj, method, Integer.valueOf(i));
        if (invoke instanceof Byte) {
            return ((Byte) invoke).byteValue();
        }
        return Byte.MAX_VALUE;
    }

    public static int getField_START_OF_LUNAR_YEAR(PathClassLoader pathClassLoader, Object obj) {
        Field field = SeslPathClassReflector.getField(pathClassLoader, "com.samsung.android.calendar.secfeature.lunarcalendar.SolarLunarTables", "START_OF_LUNAR_YEAR");
        if (field == null) {
            return 1881;
        }
        Object obj2 = SeslBaseReflector.get(obj, field);
        if (obj2 instanceof Integer) {
            return ((Integer) obj2).intValue();
        }
        return 1881;
    }

    public static int getField_WIDTH_PER_YEAR(PathClassLoader pathClassLoader, Object obj) {
        Field field = SeslPathClassReflector.getField(pathClassLoader, "com.samsung.android.calendar.secfeature.lunarcalendar.SolarLunarTables", "WIDTH_PER_YEAR");
        if (field == null) {
            return 14;
        }
        Object obj2 = SeslBaseReflector.get(obj, field);
        if (obj2 instanceof Integer) {
            return ((Integer) obj2).intValue();
        }
        return 14;
    }

    public static int getField_INDEX_OF_LEAP_MONTH(PathClassLoader pathClassLoader, Object obj) {
        Field field = SeslPathClassReflector.getField(pathClassLoader, "com.samsung.android.calendar.secfeature.lunarcalendar.SolarLunarTables", "INDEX_OF_LEAP_MONTH");
        if (field == null) {
            return 13;
        }
        Object obj2 = SeslBaseReflector.get(obj, field);
        if (obj2 instanceof Integer) {
            return ((Integer) obj2).intValue();
        }
        return 13;
    }
}
