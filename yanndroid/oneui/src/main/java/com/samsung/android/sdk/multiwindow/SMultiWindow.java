package com.samsung.android.sdk.multiwindow;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import com.samsung.android.sdk.SsdkInterface;
import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.SsdkVendorCheck;

import de.dlyt.yanndroid.oneui.utils.ReflectUtils;

public final class SMultiWindow implements SsdkInterface {
    public static final int FREE_STYLE = 2;
    public static final int MULTIWINDOW = 1;
    private static final String TAG = "SMultiWindow";
    private static boolean enableQueried = false;
    private static boolean isFreeStyleEnabled = false;
    private static boolean isMultiWindowEnabled = false;
    private static int mVersionCode = 5;
    private static String mVersionName = "1.3.1";
    private boolean mInsertLog = false;
    private SMultiWindowReflator mMultiWindowReflator = new SMultiWindowReflator();

    public SMultiWindow() {
        try {
            Class<?> cls = Class.forName("android.app.ActivityThread");
            Object currentActivityThread = ReflectUtils.genericInvokeMethod("android.app.ActivityThread", "currentActivityThread");
            mMultiWindowReflator.putMethod(cls, currentActivityThread, "getApplication", null);
            mMultiWindowReflator.putMethod(cls, currentActivityThread, "getSystemContext", null);
        } catch (Exception unused) {
        }
        initMultiWindowFeature();
    }

    @Override
    public void initialize(Context context) throws SsdkUnsupportedException {
        if (!SsdkVendorCheck.isSamsungDevice()) {
            throw new SsdkUnsupportedException(Build.BRAND + " is not supported.", SsdkUnsupportedException.VENDOR_NOT_SUPPORTED);
        } else if (isMultiWindowEnabled) {
            try {
                if (!mInsertLog) {
                    insertLog(context);
                }
            } catch (SecurityException unused) {
                throw new SecurityException("com.samsung.android.providers.context.permission.WRITE_USE_APP_FEATURE_SURVEY permission is required.");
            }
        } else {
            throw new SsdkUnsupportedException("The device is not supported.", SsdkUnsupportedException.DEVICE_NOT_SUPPORTED);
        }
    }

    @Override
    public boolean isFeatureEnabled(int feature) {
        if (feature == MULTIWINDOW) {
            return isMultiWindowEnabled;
        }
        if (feature != FREE_STYLE) {
            return false;
        }
        return isFreeStyleEnabled;
    }

    @Override
    public int getVersionCode() {
        return mVersionCode;
    }

    @Override
    public String getVersionName() {
        return mVersionName;
    }

    private void initMultiWindowFeature() {
        PackageManager packageManager;
        try {
            if (!enableQueried) {
                enableQueried = true;
                Context context = mMultiWindowReflator.checkMethod("getApplication") ? (Context) mMultiWindowReflator.invoke("getApplication", null) : null;
                if (context == null && mMultiWindowReflator.checkMethod("getSystemContext")) {
                    context = (Context) mMultiWindowReflator.invoke("getSystemContext", null);
                }
                if (context != null && (packageManager = context.getPackageManager()) != null) {
                    isMultiWindowEnabled = packageManager.hasSystemFeature(SMultiWindowReflator.PackageManager.FEATURE_MULTIWINDOW);
                    isFreeStyleEnabled = packageManager.hasSystemFeature(SMultiWindowReflator.PackageManager.FEATURE_MULTIWINDOW_FREESTYLE);
                }
            }
        } catch (Exception unused) {
        }
    }

    private void insertLog(Context context) {
        int versionCode;
        try {
            versionCode = context.getPackageManager().getPackageInfo("com.samsung.android.providers.context", PackageManager.GET_META_DATA).versionCode;
        } catch (PackageManager.NameNotFoundException unused) {
            Log.d(TAG, "Could not find ContextProvider");
            versionCode = -1;
        }
        Log.d(TAG, "versionCode: " + versionCode);
        if (versionCode <= 1) {
            Log.d(TAG, "Add com.samsung.android.providers.context.permission.WRITE_USE_APP_FEATURE_SURVEY permission");
        } else if (context.checkCallingOrSelfPermission("com.samsung.android.providers.context.permission.WRITE_USE_APP_FEATURE_SURVEY") == PackageManager.PERMISSION_GRANTED) {
            ContentValues contentValues = new ContentValues();
            contentValues.put("app_id", getClass().getPackage().getName());
            contentValues.put("feature", context.getPackageName() + "#" + getVersionCode());
            Intent intent = new Intent();
            intent.setAction("com.samsung.android.providers.context.log.action.USE_APP_FEATURE_SURVEY");
            intent.putExtra("data", contentValues);
            intent.setPackage("com.samsung.android.providers.context");
            context.sendBroadcast(intent);
            mInsertLog = true;
        } else {
            throw new SecurityException();
        }
    }
}