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

    /* JADX WARNING: Code restructure failed: missing block: B:3:0x0004, code lost: todo
        r2 = (android.view.inputmethod.InputMethodManager) r2.getSystemService("input_method");
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean forceHideSoftInput(Context r2, View r3) {
        /*
            r0 = 0
            if (r2 != 0) goto L_0x0004
            return r0
        L_0x0004:
            java.lang.String r1 = "input_method"
            java.lang.Object r2 = r2.getSystemService(r1)
            android.view.inputmethod.InputMethodManager r2 = (android.view.inputmethod.InputMethodManager) r2
            if (r2 == 0) goto L_0x001f
            boolean r1 = isSIPShowing(r2)
            if (r1 == 0) goto L_0x001f
            boolean r2 = r2.semForceHideSoftInput()     // Catch:{ NoSuchMethodError -> 0x0019 }
            return r2
        L_0x0019:
            r0 = 1
            boolean r2 = hideSoftInput(r2, r3, r0)
            return r2
        L_0x001f:
            return r0
        */


        //throw new UnsupportedOperationException("Method not decompiled: com.samsung.android.sdk.pen.settingui.util.SpenSettingUtilSIP.forceHideSoftInput(android.content.Context, android.view.View):boolean");

        return false;
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
        if (getSIPVisibleHeight(inputMethodManager) <= 0) {
            return false;
        }
        return true;
    }

    private static int getSIPVisibleHeight(InputMethodManager inputMethodManager) {
        if (inputMethodManager == null) {
            return 0;
        }
        try {
            try {
                return ((Integer) inputMethodManager.getClass().getMethod("getSIPVisibleHeight", new Class[0]).invoke(inputMethodManager, new Object[0])).intValue();
            } catch (InvocationTargetException unused) {
                try {
                    try {
                        return ((Integer) inputMethodManager.getClass().getMethod("getInputMethodWindowVisibleHeight", new Class[0]).invoke(inputMethodManager, new Object[0])).intValue();
                    } catch (InvocationTargetException unused2) {
                        return 0;
                    } catch (IllegalAccessException unused3) {
                        return 0;
                    }
                } catch (NoSuchMethodException unused4) {
                    return 0;
                }
            } catch (IllegalAccessException unused5) {
                return ((Integer) inputMethodManager.getClass().getMethod("getInputMethodWindowVisibleHeight", new Class[0]).invoke(inputMethodManager, new Object[0])).intValue();
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException unused6) {
            try {
                return ((Integer) inputMethodManager.getClass().getMethod("getInputMethodWindowVisibleHeight", new Class[0]).invoke(inputMethodManager, new Object[0])).intValue();
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
                return 0;
            }
        }
    }
}
