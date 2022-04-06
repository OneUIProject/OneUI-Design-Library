package androidx.reflect.graphics;

import android.graphics.Paint;

import androidx.reflect.SeslBaseReflector;

import java.lang.reflect.Method;

public class SeslPaintReflector {
    private static final Class<?> mClass = Paint.class;

    private SeslPaintReflector() {
    }

    public static float getHCTStrokeWidth(Paint paint) {
        Method method = SeslBaseReflector.getMethod(mClass, "getHCTStrokeWidth", new Class[0]);
        if (method == null) {
            return 0.0f;
        }
        Object invoke = SeslBaseReflector.invoke(paint, method, new Object[0]);
        if (invoke instanceof Float) {
            return ((Float) invoke).floatValue();
        }
        return 0.0f;
    }
}
