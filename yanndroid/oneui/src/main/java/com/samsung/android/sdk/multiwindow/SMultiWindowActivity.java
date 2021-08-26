package com.samsung.android.sdk.multiwindow;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

public class SMultiWindowActivity {
    private static final String TAG = "SMultiWindowActivity";
    public static final int ZONE_A = SMultiWindowReflator.WindowManagerPolicy.WINDOW_MODE_OPTION_SPLIT_ZONE_A;
    public static final int ZONE_B = SMultiWindowReflator.WindowManagerPolicy.WINDOW_MODE_OPTION_SPLIT_ZONE_B;
    public static final int ZONE_FULL = (ZONE_A | ZONE_B);
    private Context mContext;
    private Rect mDefaultSize;
    private float mDensity;
    private SMultiWindow mMultiWindow = new SMultiWindow();
    private SMultiWindowReflator mMultiWindowReflator = new SMultiWindowReflator();
    private StateChangeListener mStateChangeListener;
    private int mWindowMode;

    private boolean checkMode(int mode) {
        return (mode & mWindowMode) != 0;
    }

    private boolean checkOption(int option) {
        return (option & mWindowMode) != 0;
    }

    private void updateWindowMode() {
        Object invoke;
        if (mMultiWindow.isFeatureEnabled(SMultiWindow.MULTIWINDOW) && (invoke = mMultiWindowReflator.invoke("getWindowMode", null)) != null) {
            mWindowMode = (Integer) invoke;
        }
    }

    private void setWindowMode() {
        if (mMultiWindow.isFeatureEnabled(SMultiWindow.MULTIWINDOW)) {
            mMultiWindowReflator.invoke("setWindowMode", mWindowMode, true);
        }
    }

    private Bundle getWindowInfo() {
        return (Bundle) mMultiWindowReflator.invoke("getWindowInfo", null);
    }

    private Rect getLastSize() {
        Bundle windowInfo = getWindowInfo();
        Rect rect = windowInfo != null ? (Rect) windowInfo.getParcelable(SMultiWindowReflator.Intent.EXTRA_WINDOW_LAST_SIZE) : null;
        return rect != null ? rect : mDefaultSize;
    }

    private Object getMultiPhoneWindowEvent() {
        return mMultiWindowReflator.invoke("getMultiPhoneWindowEvent", null);
    }

    public SMultiWindowActivity(Activity activity) {
        Class<?> cls = activity.getClass();
        Class<?>[] clsArr = null;
        mMultiWindowReflator.putMethod(cls, activity, "getWindowMode", clsArr);
        mMultiWindowReflator.putMethod(cls, activity, "setWindowMode", new Class[]{Integer.TYPE, Boolean.TYPE});
        mMultiWindowReflator.putMethod(cls, activity, "getWindowInfo", clsArr);
        mMultiWindowReflator.putMethod(cls, activity, "getWindow", clsArr);
        try {
            Class<?> cls2 = Class.forName("com.samsung.android.multiwindow.MultiWindowStyle");
            mMultiWindowReflator.putMethod(cls, activity, "getMultiWindowStyle", null);
            mMultiWindowReflator.putMethod(cls, activity, "setMultiWindowStyle", new Class[]{cls2});
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Class<?> cls3 = activity.getWindow().getClass();
        mMultiWindowReflator.putMethod(cls3, activity.getWindow(), "getMultiPhoneWindowEvent", clsArr);
        mMultiWindowReflator.putMethod(cls3, activity.getWindow(), "getWindowManager", clsArr);
        mMultiWindowReflator.putMethod(cls3, activity.getWindow(), "getAttributes", clsArr);
        mDensity = activity.getResources().getDisplayMetrics().density;
        try {
            Object multiPhoneWindowEvent = getMultiPhoneWindowEvent();
            if (multiPhoneWindowEvent != null) {
                Class<?> cls4 = multiPhoneWindowEvent.getClass();
                mMultiWindowReflator.putMethod(cls4, multiPhoneWindowEvent, "setStateChangeListener", new Class[]{StateChangeListener.class});
                mMultiWindowReflator.putMethod(cls4, multiPhoneWindowEvent, "minimizeWindow", new Class[]{Integer.TYPE, Boolean.TYPE});
                mMultiWindowReflator.putMethod(cls4, multiPhoneWindowEvent, "multiWindow", new Class[]{Integer.TYPE, Boolean.TYPE});
                mMultiWindowReflator.putMethod(cls4, multiPhoneWindowEvent, "normalWindow", new Class[]{Integer.TYPE});
                mMultiWindowReflator.putMethod(cls4, multiPhoneWindowEvent, "getScaleInfo", null);
            }
        } catch (NoClassDefFoundError e) {
            e.printStackTrace();
        }
        Bundle windowInfo = getWindowInfo();
        if (windowInfo != null) {
            mDefaultSize = (Rect) windowInfo.getParcelable(SMultiWindowReflator.Intent.EXTRA_WINDOW_DEFAULT_SIZE);
        }
        try {
            mContext = activity;
            insertLogForAPI(TAG);
        } catch (SecurityException unused) {
            throw new SecurityException("com.samsung.android.providers.context.permission.WRITE_USE_APP_FEATURE_SURVEY permission is required.");
        }
    }

    public boolean isNormalWindow() {
        if (!mMultiWindow.isFeatureEnabled(SMultiWindow.MULTIWINDOW)) {
            return true;
        }
        updateWindowMode();
        return checkMode(SMultiWindowReflator.WindowManagerPolicy.WINDOW_MODE_NORMAL);
    }

    public boolean isMultiWindow() {
        if (!mMultiWindow.isFeatureEnabled(SMultiWindow.MULTIWINDOW)) {
            return false;
        }
        updateWindowMode();
        return checkMode(SMultiWindowReflator.WindowManagerPolicy.WINDOW_MODE_FREESTYLE);
    }

    public boolean isScaleWindow() {
        if (!mMultiWindow.isFeatureEnabled(SMultiWindow.FREE_STYLE)) {
            return false;
        }
        updateWindowMode();
        return checkOption(SMultiWindowReflator.WindowManagerPolicy.WINDOW_MODE_OPTION_COMMON_SCALE);
    }

    public boolean isMinimized() {
        if (!mMultiWindow.isFeatureEnabled(SMultiWindow.FREE_STYLE)) {
            return false;
        }
        updateWindowMode();
        if (!checkMode(SMultiWindowReflator.WindowManagerPolicy.WINDOW_MODE_FREESTYLE) || !checkOption(SMultiWindowReflator.WindowManagerPolicy.WINDOW_MODE_OPTION_COMMON_MINIMIZED)) {
            return false;
        }
        return true;
    }

    public void normalWindow() {
        if (mMultiWindow.isFeatureEnabled(SMultiWindow.MULTIWINDOW)) {
            updateWindowMode();
            if (!checkMode(SMultiWindowReflator.WindowManagerPolicy.WINDOW_MODE_FREESTYLE)) {
                return;
            }
            if (mMultiWindowReflator.checkMethod("normalWindow")) {
                mMultiWindowReflator.invoke("normalWindow", mWindowMode);
                return;
            }
            mWindowMode &= ~SMultiWindowReflator.WindowManagerPolicy.WINDOW_MODE_OPTION_COMMON_UNIQUEOP_MASK;
            mWindowMode &= ~SMultiWindowReflator.WindowManagerPolicy.WINDOW_MODE_OPTION_SPLIT_ZONE_MASK;
            mWindowMode &= ~SMultiWindowReflator.WindowManagerPolicy.WINDOW_MODE_MASK;
            mWindowMode |= SMultiWindowReflator.WindowManagerPolicy.WINDOW_MODE_NORMAL;
            setWindowMode();
        }
    }

    public void multiWindow(float scale) {
        if (mMultiWindow.isFeatureEnabled(SMultiWindow.FREE_STYLE)) {
            updateWindowMode();
            if ((!checkMode(SMultiWindowReflator.WindowManagerPolicy.WINDOW_MODE_FREESTYLE) || checkOption(SMultiWindowReflator.WindowManagerPolicy.WINDOW_MODE_OPTION_SPLIT_ZONE_MASK)) && mMultiWindowReflator.checkMethod("setMultiWindowStyle")) {
                try {
                    Class<?> cls = Class.forName("com.samsung.android.multiwindow.MultiWindowStyle");
                    if (cls != null) {
                        Object newInstance = cls.newInstance();
                        SMultiWindowReflator.invoke(cls, newInstance, "setType", new Class[]{Integer.TYPE}, SMultiWindowReflator.MultiWindowStyle.TYPE_CASCADE);
                        SMultiWindowReflator.invoke(cls, newInstance, "setOption", new Class[]{Integer.TYPE, Boolean.TYPE}, SMultiWindowReflator.MultiWindowStyle.OPTION_SCALE, true);
                        SMultiWindowReflator.invoke(cls, newInstance, "setScale", new Class[]{Float.TYPE}, scale);
                        mMultiWindowReflator.invoke("setMultiWindowStyle", newInstance);
                    }
                } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void minimizeWindow() {
        if (mMultiWindow.isFeatureEnabled(SMultiWindow.FREE_STYLE)) {
            updateWindowMode();
            if (checkMode(SMultiWindowReflator.WindowManagerPolicy.WINDOW_MODE_FREESTYLE) && !checkOption(SMultiWindowReflator.WindowManagerPolicy.WINDOW_MODE_OPTION_SPLIT_ZONE_MASK)) {
                if (mMultiWindowReflator.checkMethod("minimizeWindow")) {
                    mMultiWindowReflator.invoke("minimizeWindow", mWindowMode, false);
                    return;
                }
                mWindowMode &= ~SMultiWindowReflator.WindowManagerPolicy.WINDOW_MODE_MASK;
                mWindowMode |= SMultiWindowReflator.WindowManagerPolicy.WINDOW_MODE_FREESTYLE;
                mWindowMode |= SMultiWindowReflator.WindowManagerPolicy.WINDOW_MODE_OPTION_COMMON_MINIMIZED;
                mWindowMode &= ~SMultiWindowReflator.WindowManagerPolicy.WINDOW_MODE_OPTION_SPLIT_ZONE_MASK;
                setWindowMode();
            }
        }
    }

    public Rect getRectInfo() {
        if (isMultiWindow()) {
            return getLastSize();
        }
        Point point = new Point();
        Object[] objArr = null;
        ((WindowManager) mMultiWindowReflator.invoke("getWindowManager", objArr)).getDefaultDisplay().getSize(point);
        if ((((WindowManager.LayoutParams) mMultiWindowReflator.invoke("getAttributes", objArr)).flags & 1024) == 0) {
            return new Rect(0, (int) (mDensity * 25.0f), point.x, point.y);
        }
        return new Rect(0, 0, point.x, point.y);
    }

    public int getZoneInfo() {
        updateWindowMode();
        return mWindowMode & SMultiWindowReflator.WindowManagerPolicy.WINDOW_MODE_OPTION_SPLIT_ZONE_MASK;
    }

    public PointF getScaleInfo() {
        if (!mMultiWindow.isFeatureEnabled(SMultiWindow.FREE_STYLE)) {
            return new PointF(1.0f, 1.0f);
        }
        return (PointF) mMultiWindowReflator.invoke("getScaleInfo", null);
    }

    public boolean setStateChangeListener(StateChangeListener listener) {
        if (!mMultiWindow.isFeatureEnabled(SMultiWindow.MULTIWINDOW) || !mMultiWindowReflator.checkMethod("setStateChangeListener")) {
            return false;
        }
        mStateChangeListener = listener;
        if (mStateChangeListener == null) {
            mMultiWindowReflator.invoke("setStateChangeListener", null);
        } else {
            mMultiWindowReflator.invoke("setStateChangeListener", new SMultiWindowActivity.StateChangeListener() {
                public void onModeChanged(boolean z) {
                    mStateChangeListener.onModeChanged(z);
                }

                public void onZoneChanged(int i) {
                    mStateChangeListener.onZoneChanged(i);
                }

                public void onSizeChanged(Rect rect) {
                    mStateChangeListener.onSizeChanged(rect);
                }
            });
        }
        return true;
    }

    @SuppressLint("WrongConstant")
    public static Intent makeMultiWindowIntent(Intent intent, int zone) {
        int i;
        if (intent == null) {
            intent = new Intent();
        }
        if (!new SMultiWindow().isFeatureEnabled(SMultiWindow.MULTIWINDOW)) {
            return intent;
        }
        intent.addFlags(268435456);
        if (zone == ZONE_FULL) {
            i = SMultiWindowReflator.WindowManagerPolicy.WINDOW_MODE_NORMAL;
        } else {
            i = zone | SMultiWindowReflator.WindowManagerPolicy.WINDOW_MODE_FREESTYLE;
        }
        intent.putExtra(SMultiWindowReflator.Intent.EXTRA_WINDOW_MODE, i | 0);
        return intent;
    }

    @SuppressLint("WrongConstant")
    public static Intent makeMultiWindowIntent(Intent intent, float windowScale) {
        if (intent == null) {
            intent = new Intent();
        }
        SMultiWindow sMultiWindow = new SMultiWindow();
        if (!sMultiWindow.isFeatureEnabled(SMultiWindow.MULTIWINDOW) || !sMultiWindow.isFeatureEnabled(SMultiWindow.FREE_STYLE)) {
            return intent;
        }
        intent.addFlags(268435456);
        intent.putExtra(SMultiWindowReflator.Intent.EXTRA_WINDOW_MODE, SMultiWindowReflator.WindowManagerPolicy.WINDOW_MODE_FREESTYLE | SMultiWindowReflator.WindowManagerPolicy.WINDOW_MODE_OPTION_COMMON_PINUP | SMultiWindowReflator.WindowManagerPolicy.WINDOW_MODE_OPTION_COMMON_SCALE | 0);
        intent.putExtra(SMultiWindowReflator.Intent.EXTRA_WINDOW_SCALE, windowScale);
        return intent;
    }

    private void insertLogForAPI(String extra) {
        if (mContext != null) {
            int versionCode = -1;
            SMultiWindow sMultiWindow = new SMultiWindow();
            String app_id = sMultiWindow.getClass().getPackage().getName();
            String feature = mContext.getPackageName() + "#" + sMultiWindow.getVersionCode();
            try {
                versionCode = mContext.getPackageManager().getPackageInfo("com.samsung.android.providers.context", PackageManager.GET_META_DATA).versionCode;
            } catch (PackageManager.NameNotFoundException unused) {
                Log.d("SM_SDK", "Could not find ContextProvider");
            }
            Log.d("SM_SDK", "context framework's  versionCode: " + versionCode);
            if (versionCode <= 1) {
                Log.d("SM_SDK", "Add com.samsung.android.providers.context.permission.WRITE_USE_APP_FEATURE_SURVEY permission");
            } else if (mContext.checkCallingOrSelfPermission("com.samsung.android.providers.context.permission.WRITE_USE_APP_FEATURE_SURVEY") == PackageManager.PERMISSION_GRANTED) {
                ContentValues contentValues = new ContentValues();
                contentValues.put("app_id", app_id);
                contentValues.put("feature", feature);
                contentValues.put("extra", extra);
                Log.d(TAG, app_id + ", " + feature + ", " + extra);
                Intent intent = new Intent();
                intent.setAction("com.samsung.android.providers.context.log.action.USE_APP_FEATURE_SURVEY");
                intent.putExtra("data", contentValues);
                intent.setPackage("com.samsung.android.providers.context");
                mContext.sendBroadcast(intent);
            } else {
                throw new SecurityException();
            }
        }
    }


    public interface StateChangeListener {
        void onModeChanged(boolean z);

        void onSizeChanged(Rect rect);

        void onZoneChanged(int i);
    }
}