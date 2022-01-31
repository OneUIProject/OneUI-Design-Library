package androidx.reflect.view;

import android.graphics.Rect;
import android.os.Build;
import android.util.Log;
import android.view.PointerIcon;
import android.view.View;

import androidx.reflect.SeslBaseReflector;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class SeslViewReflector {
    private static final String TAG = "SeslViewReflector";
    private static final Class<?> mClass = View.class;

    private SeslViewReflector() {
    }

    public static void setField_mPaddingLeft(View view, int i) {
        Field declaredField = SeslBaseReflector.getDeclaredField(mClass, "mPaddingLeft");
        if (declaredField != null) {
            SeslBaseReflector.set(view, declaredField, Integer.valueOf(i));
        }
    }

    public static void setField_mPaddingRight(View view, int i) {
        Field declaredField = SeslBaseReflector.getDeclaredField(mClass, "mPaddingRight");
        if (declaredField != null) {
            SeslBaseReflector.set(view, declaredField, Integer.valueOf(i));
        }
    }

    public static int getField_mPaddingLeft(View view) {
        Field declaredField = SeslBaseReflector.getDeclaredField(mClass, "mPaddingLeft");
        if (declaredField == null) {
            return 0;
        }
        Object obj = SeslBaseReflector.get(view, declaredField);
        if (obj instanceof Integer) {
            return ((Integer) obj).intValue();
        }
        return 0;
    }

    public static int getField_mPaddingRight(View view) {
        Field declaredField = SeslBaseReflector.getDeclaredField(mClass, "mPaddingRight");
        if (declaredField == null) {
            return 0;
        }
        Object obj = SeslBaseReflector.get(view, declaredField);
        if (obj instanceof Integer) {
            return ((Integer) obj).intValue();
        }
        return 0;
    }

    public static boolean isInScrollingContainer(View view) {
        Method method = SeslBaseReflector.getMethod(mClass, "isInScrollingContainer", new Class[0]);
        if (method != null) {
            Object invoke = SeslBaseReflector.invoke(view, method, new Object[0]);
            if (invoke instanceof Boolean) {
                return ((Boolean) invoke).booleanValue();
            }
        }
        return false;
    }

    public static void clearAccessibilityFocus(View view) {
        Method method = SeslBaseReflector.getMethod(mClass, "clearAccessibilityFocus", new Class[0]);
        if (method != null) {
            SeslBaseReflector.invoke(view, method, new Object[0]);
        }
    }

    public static boolean requestAccessibilityFocus(View view) {
        Method method = SeslBaseReflector.getMethod(mClass, "requestAccessibilityFocus", new Class[0]);
        if (method != null) {
            Object invoke = SeslBaseReflector.invoke(view, method, new Object[0]);
            if (invoke instanceof Boolean) {
                return ((Boolean) invoke).booleanValue();
            }
        }
        return false;
    }

    public static void notifyViewAccessibilityStateChangedIfNeeded(View view, int i) {
        Method method = SeslBaseReflector.getMethod(mClass, "notifyViewAccessibilityStateChangedIfNeeded", Integer.TYPE);
        if (method != null) {
            SeslBaseReflector.invoke(view, method, Integer.valueOf(i));
        }
    }

    public static boolean isVisibleToUser(View view) {
        return isVisibleToUser(view, null);
    }

    public static boolean isVisibleToUser(View view, Rect rect) {
        Method declaredMethod = SeslBaseReflector.getDeclaredMethod(mClass, "isVisibleToUser", Rect.class);
        if (declaredMethod != null) {
            Object invoke = SeslBaseReflector.invoke(view, declaredMethod, rect);
            if (invoke instanceof Boolean) {
                return ((Boolean) invoke).booleanValue();
            }
        }
        return false;
    }

    public static int semGetHoverPopupType(View view) {
        if (Build.VERSION.SDK_INT >= 24) {
            Method method = SeslBaseReflector.getMethod(mClass, "semGetHoverPopupType", new Class[0]);
            if (method != null) {
                Object invoke = SeslBaseReflector.invoke(view, method, new Object[0]);
                if (invoke instanceof Integer) {
                    return ((Integer) invoke).intValue();
                }
            }
        } else {
            Field declaredField = SeslBaseReflector.getDeclaredField(mClass, "mHoverPopupType");
            if (declaredField != null) {
                Object obj = SeslBaseReflector.get(view, declaredField);
                if (obj instanceof Integer) {
                    return ((Integer) obj).intValue();
                }
            }
        }
        return 0;
    }

    public static void semSetHoverPopupType(View view, int i) {
        Method method;
        if (Build.VERSION.SDK_INT >= 29) {
            method = SeslBaseReflector.getDeclaredMethod(mClass, "hidden_semSetHoverPopupType", Integer.TYPE);
        } else if (Build.VERSION.SDK_INT >= 24) {
            method = SeslBaseReflector.getMethod(mClass, "semSetHoverPopupType", Integer.TYPE);
        } else {
            method = SeslBaseReflector.getMethod(mClass, "setHoverPopupType", Integer.TYPE);
        }
        if (method != null) {
            SeslBaseReflector.invoke(view, method, Integer.valueOf(i));
        }
    }

    public static void semSetDirectPenInputEnabled(View view, boolean z) {
        Method method;
        if (Build.VERSION.SDK_INT >= 29) {
            method = SeslBaseReflector.getDeclaredMethod(mClass, "hidden_semSetDirectPenInputEnabled", Boolean.TYPE);
        } else if (Build.VERSION.SDK_INT >= 24) {
            method = SeslBaseReflector.getMethod(mClass, "semSetDirectPenInputEnabled", Boolean.TYPE);
        } else {
            method = SeslBaseReflector.getMethod(mClass, "setWritingBuddyEnabled", Boolean.TYPE);
        }
        if (method != null) {
            SeslBaseReflector.invoke(view, method, Boolean.valueOf(z));
        }
    }

    public static boolean isHoveringUIEnabled(View view) {
        Method declaredMethod = SeslBaseReflector.getDeclaredMethod(mClass, "isHoveringUIEnabled", new Class[0]);
        if (declaredMethod != null) {
            Object invoke = SeslBaseReflector.invoke(view, declaredMethod, new Object[0]);
            if (invoke instanceof Boolean) {
                return ((Boolean) invoke).booleanValue();
            }
        }
        return false;
    }

    public static void semSetBlurInfo(View view, Object obj) {
        if (Build.VERSION.SDK_INT >= 31) {
            try {
                Method declaredMethod = SeslBaseReflector.getDeclaredMethod(mClass, "hidden_semSetBlurInfo", Class.forName("android.view.SemBlurInfo"));
                if (declaredMethod != null) {
                    SeslBaseReflector.invoke(view, declaredMethod, obj);
                }
            } catch (ClassNotFoundException e) {
                Log.e(TAG, "semSetBlurInfo ClassNotFoundException", e);
            }
        }
    }

    public static void semSetPointerIcon(View view, int i, PointerIcon pointerIcon) {
        Method method;
        if (Build.VERSION.SDK_INT >= 29) {
            method = SeslBaseReflector.getDeclaredMethod(mClass, "hidden_semSetPointerIcon", Integer.TYPE, PointerIcon.class);
        } else if (Build.VERSION.SDK_INT >= 24) {
            method = SeslBaseReflector.getMethod(mClass, "semSetPointerIcon", Integer.TYPE, PointerIcon.class);
        } else {
            method = null;
        }
        if (method != null) {
            SeslBaseReflector.invoke(view, method, Integer.valueOf(i), pointerIcon);
        }
    }

    public static boolean isHighContrastTextEnabled(View view) {
        Method method = SeslBaseReflector.getMethod(mClass, Build.VERSION.SDK_INT >= 29 ? "semIsHighContrastTextEnabled" : "isHighContrastTextEnabled", new Class[0]);
        if (method != null) {
            Object invoke = SeslBaseReflector.invoke(view, method, new Object[0]);
            if (invoke instanceof Boolean) {
                return ((Boolean) invoke).booleanValue();
            }
        }
        return false;
    }

    public static Object semGetHoverPopup(View view, boolean z) {
        if (Build.VERSION.SDK_INT >= 29) {
            Method declaredMethod = SeslBaseReflector.getDeclaredMethod(mClass, "hidden_semGetHoverPopup", Boolean.TYPE);
            if (declaredMethod == null) {
                return null;
            }
            return SeslBaseReflector.invoke(view, declaredMethod, Boolean.valueOf(z));
        } else if (Build.VERSION.SDK_INT >= 24) {
            Method method = SeslBaseReflector.getMethod(mClass, "semGetHoverPopup", Boolean.TYPE);
            if (method == null) {
                return null;
            }
            return SeslBaseReflector.invoke(view, method, Boolean.valueOf(z));
        } else {
            Method method2 = SeslBaseReflector.getMethod(mClass, "getHoverPopupWindow", new Class[0]);
            if (method2 != null) {
                return SeslBaseReflector.invoke(view, method2, new Object[0]);
            }
            return null;
        }
    }

    public static void resolvePadding(View view) {
        Method declaredMethod = SeslBaseReflector.getDeclaredMethod(mClass, "resolvePadding", new Class[0]);
        if (declaredMethod != null) {
            SeslBaseReflector.invoke(view, declaredMethod, new Object[0]);
        }
    }

    public static void resetPaddingToInitialValues(View view) {
        Method declaredMethod = SeslBaseReflector.getDeclaredMethod(mClass, "resetPaddingToInitialValues", new Class[0]);
        if (declaredMethod != null) {
            SeslBaseReflector.invoke(view, declaredMethod, new Object[0]);
        }
    }

    public static void getWindowDisplayFrame(View view, Rect rect) {
        Method method;
        if (Build.VERSION.SDK_INT >= 24) {
            method = SeslBaseReflector.getDeclaredMethod(mClass, "getWindowDisplayFrame", Rect.class);
        } else {
            method = SeslBaseReflector.getDeclaredMethod(mClass, "getWindowVisibleDisplayFrame", Rect.class);
        }
        if (method != null) {
            SeslBaseReflector.invoke(view, method, rect);
        }
    }

    public static class SeslMeasureSpecReflector {
        private static final Class<?> mClass = View.MeasureSpec.class;

        private SeslMeasureSpecReflector() {
        }

        public static int makeSafeMeasureSpec(int i, int i2) {
            if (!(Build.VERSION.SDK_INT < 23) || i2 != 0) {
                return View.MeasureSpec.makeMeasureSpec(i, i2);
            }
            return 0;
        }
    }
}
