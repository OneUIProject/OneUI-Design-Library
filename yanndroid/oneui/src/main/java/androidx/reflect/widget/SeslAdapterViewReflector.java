package androidx.reflect.widget;

import android.os.Build;
import android.widget.AdapterView;

import androidx.reflect.SeslBaseReflector;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class SeslAdapterViewReflector {
    private static final Class<?> mClass = AdapterView.class;

    private SeslAdapterViewReflector() {
    }

    public static void semSetBottomColor(AdapterView adapterView, int i) {
        String str;
        if (Build.VERSION.SDK_INT >= 29) {
            str = "hidden_semSetBottomColor";
        } else {
            str = Build.VERSION.SDK_INT >= 28 ? "semSetBottomColor" : null;
        }
        Method declaredMethod = SeslBaseReflector.getDeclaredMethod(mClass, str, Integer.TYPE);
        if (declaredMethod != null) {
            SeslBaseReflector.invoke(adapterView, declaredMethod, Integer.valueOf(i));
        }
    }

    public static int getField_mSelectedPosition(AdapterView adapterView) {
        Field declaredField = SeslBaseReflector.getDeclaredField(mClass, "mSelectedPosition");
        if (declaredField == null) {
            return -1;
        }
        Object obj = SeslBaseReflector.get(adapterView, declaredField);
        if (obj instanceof Integer) {
            return ((Integer) obj).intValue();
        }
        return -1;
    }
}
