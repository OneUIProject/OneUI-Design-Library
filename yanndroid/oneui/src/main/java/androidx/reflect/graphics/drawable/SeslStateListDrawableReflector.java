package androidx.reflect.graphics.drawable;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;

import androidx.reflect.SeslBaseReflector;

import java.lang.reflect.Method;

public class SeslStateListDrawableReflector {
    private static final Class<?> mClass = StateListDrawable.class;

    private SeslStateListDrawableReflector() {
    }

    public static int getStateCount(StateListDrawable stateListDrawable) {
        Method method;
        if (Build.VERSION.SDK_INT >= 29) {
            method = SeslBaseReflector.getDeclaredMethod(mClass, "hidden_getStateCount", new Class[0]);
        } else {
            method = SeslBaseReflector.getMethod(mClass, "getStateCount", new Class[0]);
        }
        if (method != null) {
            Object invoke = SeslBaseReflector.invoke(stateListDrawable, method, new Object[0]);
            if (invoke instanceof Integer) {
                return ((Integer) invoke).intValue();
            }
        }
        return 0;
    }

    public static Drawable getStateDrawable(StateListDrawable stateListDrawable, int i) {
        Method method;
        if (Build.VERSION.SDK_INT >= 29) {
            method = SeslBaseReflector.getDeclaredMethod(mClass, "hidden_getStateDrawable", Integer.TYPE);
        } else {
            method = SeslBaseReflector.getMethod(mClass, "getStateDrawable", Integer.TYPE);
        }
        if (method == null) {
            return null;
        }
        Object invoke = SeslBaseReflector.invoke(stateListDrawable, method, Integer.valueOf(i));
        if (invoke instanceof Drawable) {
            return (Drawable) invoke;
        }
        return null;
    }

    public static int[] getStateSet(StateListDrawable stateListDrawable, int i) {
        Method method;
        if (Build.VERSION.SDK_INT >= 29) {
            method = SeslBaseReflector.getDeclaredMethod(mClass, "hidden_getStateSet", Integer.TYPE);
        } else {
            method = SeslBaseReflector.getMethod(mClass, "getStateSet", Integer.TYPE);
        }
        if (method != null) {
            Object invoke = SeslBaseReflector.invoke(stateListDrawable, method, Integer.valueOf(i));
            if (invoke instanceof int[]) {
                return (int[]) invoke;
            }
        }
        return new int[0];
    }
}
