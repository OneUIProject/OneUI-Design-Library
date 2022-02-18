package androidx.reflect.lunarcalendar;

import android.content.Context;

import androidx.reflect.SeslBaseReflector;
import androidx.reflect.SeslPathClassReflector;

import java.lang.reflect.Method;
import java.util.Calendar;

import dalvik.system.PathClassLoader;

public class SeslLunarDateUtilsReflector {
    private static final String mClassName = "com.android.calendar.event.widget.datetimepicker.LunarDateUtils";

    private SeslLunarDateUtilsReflector() {
    }

    public static String buildLunarDateString(PathClassLoader pathClassLoader, Calendar calendar, Context context) {
        Method method = SeslPathClassReflector.getMethod(pathClassLoader, mClassName, "buildLunarDateString", Calendar.class, Context.class);
        if (method != null) {
            Object invoke = SeslBaseReflector.invoke(null, method, calendar, context);
            if (invoke instanceof String) {
                return (String) invoke;
            }
        }
        return null;
    }
}
