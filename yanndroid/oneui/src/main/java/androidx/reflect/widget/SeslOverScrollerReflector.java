package androidx.reflect.widget;

import android.os.Build;
import android.widget.OverScroller;

import androidx.reflect.SeslBaseReflector;

import java.lang.reflect.Method;

public class SeslOverScrollerReflector {
    public static final Class<?> mClass = OverScroller.class;

    public static void fling(OverScroller overScroller, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8, boolean z, float f) {
        if (Build.VERSION.SDK_INT >= 30) {
            Class<?> cls = mClass;
            Class cls2 = Integer.TYPE;
            Method declaredMethod = SeslBaseReflector.getDeclaredMethod(cls, "hidden_fling", cls2, cls2, Boolean.TYPE, Float.TYPE);
            if (declaredMethod != null) {
                SeslBaseReflector.invoke(overScroller, declaredMethod, Integer.valueOf(i3), Integer.valueOf(i4), Boolean.valueOf(z), Float.valueOf(f));
                return;
            }
        }
        overScroller.fling(i, i2, i3, i4, i5, i6, i7, i8);
    }

    public static void setSmoothScrollEnabled(OverScroller overScroller, boolean z) {
        Method declaredMethod;
        if (Build.VERSION.SDK_INT >= 26 && (declaredMethod = SeslBaseReflector.getDeclaredMethod(mClass, "semSetSmoothScrollEnabled", Boolean.TYPE)) != null) {
            SeslBaseReflector.invoke(overScroller, declaredMethod, Boolean.valueOf(z));
        }
    }
}
