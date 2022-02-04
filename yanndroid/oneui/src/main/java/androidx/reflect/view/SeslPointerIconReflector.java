package androidx.reflect.view;

import android.os.Build;

import androidx.reflect.SeslBaseReflector;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class SeslPointerIconReflector {
    public static String mClassName = "android.view.PointerIcon";

    public static int getField_SEM_TYPE_STYLUS_DEFAULT() {
        int i = Build.VERSION.SDK_INT;
        Object obj = null;
        if (i >= 29) {
            Method declaredMethod = SeslBaseReflector.getDeclaredMethod(mClassName, "hidden_SEM_TYPE_STYLUS_DEFAULT", new Class[0]);
            if (declaredMethod != null) {
                obj = SeslBaseReflector.invoke(null, declaredMethod, new Object[0]);
            }
        } else {
            Field field = SeslBaseReflector.getField(mClassName, i >= 24 ? "SEM_TYPE_STYLUS_DEFAULT" : "HOVERING_SPENICON_DEFAULT");
            if (field != null) {
                obj = SeslBaseReflector.get(null, field);
            }
        }
        if (obj instanceof Integer) {
            return ((Integer) obj).intValue();
        }
        return 1;
    }

    public static int getField_SEM_TYPE_STYLUS_SCROLL_UP() {
        int i = Build.VERSION.SDK_INT;
        Object obj = null;
        if (i >= 29) {
            Method declaredMethod = SeslBaseReflector.getDeclaredMethod(mClassName, "hidden_SEM_TYPE_STYLUS_SCROLL_UP", new Class[0]);
            if (declaredMethod != null) {
                obj = SeslBaseReflector.invoke(null, declaredMethod, new Object[0]);
            }
        } else {
            Field field = SeslBaseReflector.getField(mClassName, i >= 24 ? "SEM_TYPE_STYLUS_SCROLL_UP" : "HOVERING_SCROLLICON_POINTER_01");
            if (field != null) {
                obj = SeslBaseReflector.get(null, field);
            }
        }
        if (obj instanceof Integer) {
            return ((Integer) obj).intValue();
        }
        return 11;
    }

    public static int getField_SEM_TYPE_STYLUS_SCROLL_DOWN() {
        int i = Build.VERSION.SDK_INT;
        Object obj = null;
        if (i >= 29) {
            Method declaredMethod = SeslBaseReflector.getDeclaredMethod(mClassName, "hidden_SEM_TYPE_STYLUS_SCROLL_DOWN", new Class[0]);
            if (declaredMethod != null) {
                obj = SeslBaseReflector.invoke(null, declaredMethod, new Object[0]);
            }
        } else {
            Field field = SeslBaseReflector.getField(mClassName, i >= 24 ? "SEM_TYPE_STYLUS_SCROLL_DOWN" : "HOVERING_SCROLLICON_POINTER_05");
            if (field != null) {
                obj = SeslBaseReflector.get(null, field);
            }
        }
        if (obj instanceof Integer) {
            return ((Integer) obj).intValue();
        }
        return 15;
    }

    public static int getField_SEM_TYPE_STYLUS_SCROLL_LEFT() {
        int i = Build.VERSION.SDK_INT;
        Object obj = null;
        if (i >= 29) {
            Method declaredMethod = SeslBaseReflector.getDeclaredMethod(mClassName, "hidden_SEM_TYPE_STYLUS_SCROLL_LEFT", new Class[0]);
            if (declaredMethod != null) {
                obj = SeslBaseReflector.invoke(null, declaredMethod, new Object[0]);
            }
        } else {
            Field field = SeslBaseReflector.getField(mClassName, i >= 24 ? "SEM_TYPE_STYLUS_SCROLL_LEFT" : "HOVERING_SCROLLICON_POINTER_07");
            if (field != null) {
                obj = SeslBaseReflector.get(null, field);
            }
        }
        if (obj instanceof Integer) {
            return ((Integer) obj).intValue();
        }
        return 17;
    }

    public static int getField_SEM_TYPE_STYLUS_SCROLL_RIGHT() {
        int i = Build.VERSION.SDK_INT;
        Object obj = null;
        if (i >= 29) {
            Method declaredMethod = SeslBaseReflector.getDeclaredMethod(mClassName, "hidden_SEM_TYPE_STYLUS_SCROLL_RIGHT", new Class[0]);
            if (declaredMethod != null) {
                obj = SeslBaseReflector.invoke(null, declaredMethod, new Object[0]);
            }
        } else {
            Field field = SeslBaseReflector.getField(mClassName, i >= 24 ? "SEM_TYPE_STYLUS_SCROLL_RIGHT" : "HOVERING_SCROLLICON_POINTER_03");
            if (field != null) {
                obj = SeslBaseReflector.get(null, field);
            }
        }
        if (obj instanceof Integer) {
            return ((Integer) obj).intValue();
        }
        return 13;
    }

    public static int getField_SEM_TYPE_STYLUS_PEN_SELECT() {
        int i = Build.VERSION.SDK_INT;
        Object obj = null;
        if (i >= 29) {
            Method declaredMethod = SeslBaseReflector.getDeclaredMethod(mClassName, "hidden_SEM_TYPE_STYLUS_PEN_SELECT", new Class[0]);
            if (declaredMethod != null) {
                obj = SeslBaseReflector.invoke(null, declaredMethod, new Object[0]);
            }
        } else {
            Field field = SeslBaseReflector.getField(mClassName, i >= 24 ? "SEM_TYPE_STYLUS_PEN_SELECT" : "HOVERING_PENSELECT_POINTER_01");
            if (field != null) {
                obj = SeslBaseReflector.get(null, field);
            }
        }
        if (obj instanceof Integer) {
            return ((Integer) obj).intValue();
        }
        return 21;
    }

    public static int getField_SEM_TYPE_STYLUS_MORE() {
        int i = Build.VERSION.SDK_INT;
        Object obj = null;
        if (i >= 29) {
            Method declaredMethod = SeslBaseReflector.getDeclaredMethod(mClassName, "hidden_SEM_TYPE_STYLUS_MORE", new Class[0]);
            if (declaredMethod != null) {
                obj = SeslBaseReflector.invoke(null, declaredMethod, new Object[0]);
            }
        } else {
            Field field = SeslBaseReflector.getField(mClassName, i >= 24 ? "SEM_TYPE_STYLUS_MORE" : "HOVERING_SPENICON_MORE");
            if (field != null) {
                obj = SeslBaseReflector.get(null, field);
            }
        }
        if (obj instanceof Integer) {
            return ((Integer) obj).intValue();
        }
        return 20010;
    }
}
