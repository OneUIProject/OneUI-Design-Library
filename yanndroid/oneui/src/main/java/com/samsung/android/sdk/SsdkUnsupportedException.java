package com.samsung.android.sdk;

public class SsdkUnsupportedException extends Exception {
    public static final int DEVICE_NOT_SUPPORTED = 1;
    public static final int SDK_VERSION_MISMATCH = 2;
    public static final int VENDOR_NOT_SUPPORTED = 0;
    private String mPackageName = null;
    private int mType = VENDOR_NOT_SUPPORTED;
    private int mVersionCode = 0;

    public SsdkUnsupportedException(String message, int type) {
        super(message);
        mType = type;
    }

    public SsdkUnsupportedException(String message, int type, String packageName, int versionCode) {
        super(message);
        mType = type;
        mPackageName = packageName;
        mVersionCode = versionCode;
    }

    public int getType() {
        return mType;
    }

    public String getPackageName() {
        return mPackageName;
    }

    public int getVersionCode() {
        return mVersionCode;
    }
}