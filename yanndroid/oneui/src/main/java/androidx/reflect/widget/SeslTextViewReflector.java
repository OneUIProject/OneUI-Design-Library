package androidx.reflect.widget;

import android.os.Build;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.reflect.SeslBaseReflector;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class SeslTextViewReflector {
    private static final Class<?> mClass = TextView.class;

    private SeslTextViewReflector() {
    }

    public static int getField_SEM_AUTOFILL_ID() {
        Field declaredField;
        int i = Build.VERSION.SDK_INT;
        Object obj = null;
        if (i >= 29) {
            Method declaredMethod = SeslBaseReflector.getDeclaredMethod(mClass, "hidden_SEM_AUTOFILL_ID", new Class[0]);
            if (declaredMethod != null) {
                obj = SeslBaseReflector.invoke(null, declaredMethod, new Object[0]);
            }
        } else if (i >= 24 && (declaredField = SeslBaseReflector.getDeclaredField(mClass, "SEM_AUTOFILL_ID")) != null) {
            obj = SeslBaseReflector.get(null, declaredField);
        }
        if (obj instanceof Integer) {
            return ((Integer) obj).intValue();
        }
        return 0;
    }

    public static boolean getField_mSingleLine(@NonNull TextView textView) {
        Field declaredField = SeslBaseReflector.getDeclaredField(mClass, "mSingleLine");
        if (declaredField == null) {
            return false;
        }
        Object obj = SeslBaseReflector.get(textView, declaredField);
        if (obj instanceof Boolean) {
            return ((Boolean) obj).booleanValue();
        }
        return false;
    }

    public static boolean semIsTextSelectionProgressing() {
        int i = Build.VERSION.SDK_INT;
        Method declaredMethod = i >= 29 ? SeslBaseReflector.getDeclaredMethod(mClass, "hidden_semIsTextSelectionProgressing", new Class[0]) : i >= 24 ? SeslBaseReflector.getMethod(mClass, "semIsTextSelectionProgressing", new Class[0]) : null;
        if (declaredMethod != null) {
            Object invoke = SeslBaseReflector.invoke(null, declaredMethod, new Object[0]);
            if (invoke instanceof Boolean) {
                return ((Boolean) invoke).booleanValue();
            }
        }
        return false;
    }

    public static boolean semIsTextViewHovered() {
        int i = Build.VERSION.SDK_INT;
        Method declaredMethod = i >= 29 ? SeslBaseReflector.getDeclaredMethod(mClass, "hidden_semIsTextViewHovered", new Class[0]) : i >= 24 ? SeslBaseReflector.getMethod(mClass, "semIsTextViewHovered", new Class[0]) : null;
        if (declaredMethod != null) {
            Object invoke = SeslBaseReflector.invoke(null, declaredMethod, new Object[0]);
            if (invoke instanceof Boolean) {
                return ((Boolean) invoke).booleanValue();
            }
        }
        return false;
    }

    public static void semSetActionModeMenuItemEnabled(@NonNull TextView textView, int i, boolean z) {
        Method method;
        int i2 = Build.VERSION.SDK_INT;
        if (i2 >= 29) {
            method = SeslBaseReflector.getDeclaredMethod(mClass, "hidden_semSetActionModeMenuItemEnabled", Integer.TYPE, Boolean.TYPE);
        } else if (i2 >= 24) {
            method = SeslBaseReflector.getMethod(mClass, "semSetActionModeMenuItemEnabled", Integer.TYPE, Boolean.TYPE);
        } else {
            method = SeslBaseReflector.getMethod(mClass, "setNewActionPopupMenu", Integer.TYPE, Boolean.TYPE);
        }
        if (method != null) {
            SeslBaseReflector.invoke(textView, method, Integer.valueOf(i), Boolean.valueOf(z));
        }
    }

    public static void semSetButtonShapeEnabled(@NonNull TextView textView, boolean z) {
        Method method;
        int i = Build.VERSION.SDK_INT;
        if (i >= 29) {
            method = SeslBaseReflector.getDeclaredMethod(mClass, "hidden_semSetButtonShapeEnabled", Boolean.TYPE);
        } else if (i >= 26) {
            method = SeslBaseReflector.getMethod(mClass, "semSetButtonShapeEnabled", Boolean.TYPE);
        } else {
            method = null;
        }
        if (method != null) {
            SeslBaseReflector.invoke(textView, method, Boolean.valueOf(z));
        }
    }

    public static void semSetButtonShapeEnabled(@NonNull TextView textView, boolean z, int i) {
        Method method;
        int i2 = Build.VERSION.SDK_INT;
        if (i2 >= 29) {
            method = SeslBaseReflector.getDeclaredMethod(mClass, "hidden_semSetButtonShapeEnabled", Boolean.TYPE, Integer.TYPE);
        } else if (i2 >= 26) {
            method = SeslBaseReflector.getMethod(mClass, "semSetButtonShapeEnabled", Boolean.TYPE, Integer.TYPE);
        } else {
            method = null;
        }
        if (method != null) {
            SeslBaseReflector.invoke(textView, method, Boolean.valueOf(z), Integer.valueOf(i));
        }
    }
}