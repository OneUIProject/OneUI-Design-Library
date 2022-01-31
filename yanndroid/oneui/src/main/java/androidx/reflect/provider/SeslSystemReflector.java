package androidx.reflect.provider;

import android.os.Build;
import android.provider.Settings;

import androidx.reflect.SeslBaseReflector;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class SeslSystemReflector {
    public static final Class<?> mClass = Settings.System.class;

    public static String getField_SEM_PEN_HOVERING() {
        int i = Build.VERSION.SDK_INT;
        Object obj = null;
        if (i >= 29) {
            Method declaredMethod = SeslBaseReflector.getDeclaredMethod(mClass, "hidden_SEM_PEN_HOVERING", new Class[0]);
            if (declaredMethod != null) {
                obj = SeslBaseReflector.invoke(null, declaredMethod, new Object[0]);
            }
        } else {
            Field field = SeslBaseReflector.getField(mClass, i >= 24 ? "SEM_PEN_HOVERING" : "PEN_HOVERING");
            if (field != null) {
                obj = SeslBaseReflector.get(null, field);
            }
        }
        return obj instanceof String ? (String) obj : "pen_hovering";
    }

    public static String getField_SEM_ACCESSIBILITY_REDUCE_TRANSPARENCY() {
        Method declaredMethod;
        Object obj = null;
        if (Build.VERSION.SDK_INT >= 31 && (declaredMethod = SeslBaseReflector.getDeclaredMethod(mClass, "hidden_SEM_ACCESSIBILITY_REDUCE_TRANSPARENCY", new Class[0])) != null) {
            obj = SeslBaseReflector.invoke(null, declaredMethod, new Object[0]);
        }
        return obj instanceof String ? (String) obj : "not_supported";
    }
}