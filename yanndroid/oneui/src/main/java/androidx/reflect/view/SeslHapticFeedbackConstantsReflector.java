package androidx.reflect.view;

import android.os.Build;
import android.view.HapticFeedbackConstants;

import androidx.reflect.SeslBaseReflector;

import java.lang.reflect.Method;

public class SeslHapticFeedbackConstantsReflector {
    private static final Class<?> mClass = HapticFeedbackConstants.class;

    private SeslHapticFeedbackConstantsReflector() {
    }

    public static int semGetVibrationIndex(int i) {
        Method method;
        int i2 = Build.VERSION.SDK_INT;
        if (i2 >= 29) {
            method = SeslBaseReflector.getDeclaredMethod(mClass, "hidden_semGetVibrationIndex", Integer.TYPE);
        } else if (i2 >= 28) {
            method = SeslBaseReflector.getMethod(mClass, "semGetVibrationIndex", Integer.TYPE);
        } else {
            method = null;
        }
        if (method == null) {
            return -1;
        }
        Object invoke = SeslBaseReflector.invoke(null, method, Integer.valueOf(i));
        if (invoke instanceof Integer) {
            return ((Integer) invoke).intValue();
        }
        return -1;
    }
}
