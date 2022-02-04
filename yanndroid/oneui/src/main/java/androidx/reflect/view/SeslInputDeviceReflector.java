package androidx.reflect.view;

import android.os.Build;
import android.view.InputDevice;

import androidx.reflect.SeslBaseReflector;

import java.lang.reflect.Method;

public class SeslInputDeviceReflector {
    public static final Class<?> mClass = InputDevice.class;

    public static void semSetPointerType(InputDevice inputDevice, int i) {
        if (inputDevice != null) {
            Method method = null;
            int i2 = Build.VERSION.SDK_INT;
            if (i2 >= 29) {
                method = SeslBaseReflector.getDeclaredMethod(mClass, "hidden_setPointerType", Integer.TYPE);
            } else if (i2 >= 28) {
                method = SeslBaseReflector.getMethod(mClass, "semSetPointerType", Integer.TYPE);
            } else if (i2 >= 24) {
                method = SeslBaseReflector.getMethod(mClass, "setPointerType", Integer.TYPE);
            }
            if (method != null) {
                SeslBaseReflector.invoke(inputDevice, method, Integer.valueOf(i));
            }
        }
    }
}
