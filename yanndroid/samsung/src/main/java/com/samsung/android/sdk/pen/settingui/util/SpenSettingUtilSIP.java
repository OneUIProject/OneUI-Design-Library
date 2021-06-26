package com.samsung.android.sdk.pen.settingui.util;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.lang.reflect.InvocationTargetException;

public class SpenSettingUtilSIP {

    public static boolean showSoftInput(Context context, View view, int i) {
        InputMethodManager inputMethodManager;
        if (context == null || (inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE)) == null) {
            return false;
        }
        return inputMethodManager.showSoftInput(view, i);
    }

    public static boolean hideSoftInput(Context context, View view, int i) {
        if (context == null) {
            return false;
        }
        return hideSoftInput((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE), view, i);
    }

    public static boolean isSIPShowing(Context context) {
        if (context == null) {
            return false;
        }
        return isSIPShowing((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE));
    }

    private static boolean hideSoftInput(InputMethodManager inputMethodManager, View view, int i) {
        if (inputMethodManager == null || !isSIPShowing(inputMethodManager)) {
            return false;
        }
        if (view != null) {
            return inputMethodManager.hideSoftInputFromWindow(view.getRootView().getWindowToken(), i);
        }
        inputMethodManager.toggleSoftInput(0, 0);
        return true;
    }

    private static boolean isSIPShowing(InputMethodManager inputMethodManager) {
        if (inputMethodManager == null) {
            return false;
        }
        return getSIPVisibleHeight(inputMethodManager) > 0;
    }

    private static int getSIPVisibleHeight(InputMethodManager inputMethodManager) {
        if (inputMethodManager == null) {
            return 0;
        }
        try {
            try {
                return ((Integer) inputMethodManager.getClass().getMethod("getSIPVisibleHeight").invoke(inputMethodManager, new Object[0])).intValue();
            } catch (InvocationTargetException unused) {
                try {
                    try {
                        return ((Integer) inputMethodManager.getClass().getMethod("getInputMethodWindowVisibleHeight").invoke(inputMethodManager, new Object[0])).intValue();
                    } catch (InvocationTargetException unused2) {
                        return 0;
                    } catch (IllegalAccessException unused3) {
                        return 0;
                    }
                } catch (NoSuchMethodException unused4) {
                    return 0;
                }
            } catch (IllegalAccessException unused5) {
                return ((Integer) inputMethodManager.getClass().getMethod("getInputMethodWindowVisibleHeight").invoke(inputMethodManager, new Object[0])).intValue();
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException unused6) {
            try {
                return ((Integer) inputMethodManager.getClass().getMethod("getInputMethodWindowVisibleHeight").invoke(inputMethodManager, new Object[0])).intValue();
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
                return 0;
            }
        }
    }
}
