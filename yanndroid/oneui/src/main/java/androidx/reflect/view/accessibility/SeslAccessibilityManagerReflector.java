package androidx.reflect.view.accessibility;

import android.view.accessibility.AccessibilityManager;

import androidx.reflect.SeslBaseReflector;

import java.lang.reflect.Method;

public class SeslAccessibilityManagerReflector {
    public static String mClassName = "android.view.accessibility.AccessibilityManager";

    public static boolean isScreenReaderEnabled(AccessibilityManager accessibilityManager, boolean z) {
        Method declaredMethod = SeslBaseReflector.getDeclaredMethod(mClassName, "semIsScreenReaderEnabled", new Class[0]);
        return (declaredMethod == null || accessibilityManager == null) ? z : ((Boolean) SeslBaseReflector.invoke(accessibilityManager, declaredMethod, new Object[0])).booleanValue();
    }
}
