package androidx.reflect.view;

import android.os.Build;
import android.util.Log;
import android.view.View;

import androidx.reflect.SeslBaseReflector;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class SeslSemBlurInfoReflector {
    private static final String TAG = "SeslSemBlurInfoReflector";
    private static final String mBuilderClass = "android.view.SemBlurInfo$Builder";

    private SeslSemBlurInfoReflector() {
    }

    public static Object semCreateBlurBuilder(int i) {
        Constructor<?> constructor = SeslBaseReflector.getConstructor(mBuilderClass, Integer.TYPE);
        if (Build.VERSION.SDK_INT >= 31 && constructor != null) {
            try {
                return constructor.newInstance(Integer.valueOf(i));
            } catch (IllegalAccessException e) {
                Log.e(TAG, "semCreateBlurBuilder IllegalAccessException", e);
            } catch (InstantiationException e2) {
                Log.e(TAG, "semCreateBlurBuilder InstantiationException", e2);
            } catch (InvocationTargetException e3) {
                Log.e(TAG, "semCreateBlurBuilder InvocationTargetException", e3);
            }
        }
        return null;
    }

    public static Object semSetBuilderBlurRadius(Object obj, int i) {
        Method declaredMethod = Build.VERSION.SDK_INT >= 31 ? SeslBaseReflector.getDeclaredMethod(mBuilderClass, "hidden_setRadius", Integer.TYPE) : null;
        if (declaredMethod != null) {
            declaredMethod.setAccessible(true);
            SeslBaseReflector.invoke(obj, declaredMethod, Integer.valueOf(i));
        }
        return obj;
    }

    public static Object semSetBuilderBlurBackgroundColor(Object obj, int i) {
        Method declaredMethod = Build.VERSION.SDK_INT >= 31 ? SeslBaseReflector.getDeclaredMethod(mBuilderClass, "hidden_setBackgroundColor", Integer.TYPE) : null;
        if (declaredMethod != null) {
            declaredMethod.setAccessible(true);
            SeslBaseReflector.invoke(obj, declaredMethod, Integer.valueOf(i));
        }
        return obj;
    }

    public static Object semSetBuilderBlurBackgroundCornerRadius(Object obj, float f) {
        Method declaredMethod = Build.VERSION.SDK_INT >= 31 ? SeslBaseReflector.getDeclaredMethod(mBuilderClass, "hidden_setBackgroundCornerRadius", Float.TYPE) : null;
        if (declaredMethod != null) {
            declaredMethod.setAccessible(true);
            SeslBaseReflector.invoke(obj, declaredMethod, Float.valueOf(f));
        }
        return obj;
    }

    public static void semBuildSetBlurInfo(Object obj, View view) {
        Method declaredMethod = Build.VERSION.SDK_INT >= 31 ? SeslBaseReflector.getDeclaredMethod(mBuilderClass, "hidden_build", new Class[0]) : null;
        if (declaredMethod != null) {
            declaredMethod.setAccessible(true);
            SeslViewReflector.semSetBlurInfo(view, SeslBaseReflector.invoke(obj, declaredMethod, new Object[0]));
        }
    }
}
