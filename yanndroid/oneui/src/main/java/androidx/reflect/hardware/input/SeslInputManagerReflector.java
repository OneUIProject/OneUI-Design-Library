package androidx.reflect.hardware.input;

import android.hardware.input.InputManager;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.reflect.SeslBaseReflector;

import java.lang.reflect.Method;

public class SeslInputManagerReflector {
    @RequiresApi(21)
    private static final Class<?> mClass = InputManager.class;

    private SeslInputManagerReflector() {
    }

    @RequiresApi(21)
    private static Object getInstance() {
        Method method = SeslBaseReflector.getMethod(mClass, "getInstance", new Class[0]);
        if (method != null) {
            return SeslBaseReflector.invoke(null, method, new Object[0]);
        }
        return null;
    }

    public static void setPointerIconType(int i) {
        Object instance;
        if (Build.VERSION.SDK_INT >= 24 && (instance = getInstance()) != null) {
            Method method = null;
            int i2 = Build.VERSION.SDK_INT;
            if (i2 >= 29) {
                method = SeslBaseReflector.getDeclaredMethod(mClass, "hidden_setPointerIconType", Integer.TYPE);
            } else if (i2 >= 24) {
                method = SeslBaseReflector.getMethod(mClass, "setPointerIconType", Integer.TYPE);
            }
            if (method != null) {
                SeslBaseReflector.invoke(instance, method, Integer.valueOf(i));
            }
        }
    }
}
