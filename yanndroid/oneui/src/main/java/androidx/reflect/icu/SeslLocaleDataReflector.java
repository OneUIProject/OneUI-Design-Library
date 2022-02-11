package androidx.reflect.icu;

import android.os.Build;
import android.util.Log;

import androidx.reflect.SeslBaseReflector;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.DateFormatSymbols;
import java.util.Locale;

public class SeslLocaleDataReflector {
    public static String mClassName = "libcore.icu.LocaleData";
    public static String mDateFormatSymbolsClass = "android.icu.text.DateFormatSymbols";
    public static String mSemClassName = "com.samsung.sesl.icu.SemLocaleData";
    public static String mSemDateFormatSymbolsClass = "com.samsung.sesl.icu.SemDateFormatSymbols";

    public static Object get(Locale locale) {
        Method method;
        if (Build.VERSION.SDK_INT >= 29) {
            method = SeslBaseReflector.getDeclaredMethod(mSemClassName, "get", Locale.class);
        } else {
            method = SeslBaseReflector.getMethod(mClassName, "get", Locale.class);
        }
        if (method != null) {
            Object invoke = SeslBaseReflector.invoke(null, method, locale);
            if (invoke.getClass().getName().equals(mClassName)) {
                return invoke;
            }
        }
        return null;
    }

    public static String[] getField_amPm(Object obj) {
        Object obj2 = null;
        if (Build.VERSION.SDK_INT >= 29) {
            Method declaredMethod = SeslBaseReflector.getDeclaredMethod(mSemClassName, "getAmPm", SeslBaseReflector.getClass(mClassName));
            if (declaredMethod != null) {
                obj2 = SeslBaseReflector.invoke(null, declaredMethod, obj);
            }
        } else {
            Field field = SeslBaseReflector.getField(mClassName, "amPm");
            if (field != null) {
                obj2 = SeslBaseReflector.get(obj, field);
            }
        }
        if (obj2 instanceof String[]) {
            return (String[]) obj2;
        }
        Log.e("SeslLocaleDataReflector", "amPm failed. Use DateFormatSymbols for ampm");
        return new DateFormatSymbols().getAmPmStrings();
    }

    public static String getField_narrowAm(Object obj) {
        Object obj2 = null;
        if (Build.VERSION.SDK_INT >= 29) {
            Method declaredMethod = SeslBaseReflector.getDeclaredMethod(mSemClassName, "getNarrowAm", SeslBaseReflector.getClass(mClassName));
            if (declaredMethod != null) {
                obj2 = SeslBaseReflector.invoke(null, declaredMethod, obj);
            }
        } else {
            Field field = SeslBaseReflector.getField(mClassName, "narrowAm");
            if (field != null) {
                obj2 = SeslBaseReflector.get(obj, field);
            }
        }
        return obj2 instanceof String ? (String) obj2 : "Am";
    }

    public static String getField_narrowPm(Object obj) {
        Object obj2 = null;
        if (Build.VERSION.SDK_INT >= 29) {
            Method declaredMethod = SeslBaseReflector.getDeclaredMethod(mSemClassName, "getNarrowPm", SeslBaseReflector.getClass(mClassName));
            if (declaredMethod != null) {
                obj2 = SeslBaseReflector.invoke(null, declaredMethod, obj);
            }
        } else {
            Field field = SeslBaseReflector.getField(mClassName, "narrowPm");
            if (field != null) {
                obj2 = SeslBaseReflector.get(obj, field);
            }
        }
        return obj2 instanceof String ? (String) obj2 : "Pm";
    }

    public static String[] getAmpmNarrowStrings(Object obj) {
        Method declaredMethod = SeslBaseReflector.getDeclaredMethod(mSemDateFormatSymbolsClass, "getAmpmNarrowStrings", SeslBaseReflector.getClass(mDateFormatSymbolsClass));
        Object obj2 = null;
        if (declaredMethod != null) {
            obj2 = SeslBaseReflector.invoke(null, declaredMethod, obj);
        }
        if (obj2 instanceof String[]) {
            return (String[]) obj2;
        }
        Log.e("SeslLocaleDataReflector", "amPm narrow strings failed. Use getAmPmStrings for ampm");
        return new DateFormatSymbols().getAmPmStrings();
    }
}
