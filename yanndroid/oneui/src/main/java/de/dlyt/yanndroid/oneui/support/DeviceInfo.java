package de.dlyt.yanndroid.oneui.support;

import android.os.Build;
import android.util.Log;

import de.dlyt.yanndroid.oneui.utils.ReflectUtils;

public class DeviceInfo {
    private static Integer BinaryMode = null;
    private static String ModelName = null;
    private static String ProductName = null;
    private static Integer SEM_INT = (Integer) ReflectUtils.genericGetField(Build.VERSION.class, "SEM_INT");
    private static int mIsSdlDevice = -1;

    public static boolean isSemDevice() {
        return SEM_INT != null;
    }

    public static boolean isSdlDevice() {
        if (mIsSdlDevice == -1) {
            if (Build.VERSION.SDK_INT == 23) {
                mIsSdlDevice = Build.MANUFACTURER.equals("samsung") ? 1 : 0;
            } else {
                try {
                    Class.forName("com.sec.android.secmediarecorder.SecMediaRecorder");
                    mIsSdlDevice = 1;
                } catch (Error | Exception unused) {
                    mIsSdlDevice = 0;
                }
            }
        }
        return mIsSdlDevice == 1;
    }

    public static int getSemPlatformVersionInt(int i) {
        return isSemDevice() ? (int) ReflectUtils.genericGetField(Build.VERSION.class, "SEM_PLATFORM_INT") : i;
    }

    public static String getProductName() {
        String str = ProductName;
        if (str != null) {
            return str;
        }
        ProductName = Build.DEVICE;
        return ProductName;
    }

    public static String getModelName() {
        String str = ModelName;
        if (str != null) {
            return str;
        }
        ModelName = Build.MODEL;
        return ModelName;
    }

    public static boolean isUserMode() {
        if (BinaryMode == null) {
            checkBinaryMode();
        }
        Integer num = BinaryMode;
        return num != null && num.equals(1);
    }

    public static boolean isEngMode() {
        if (BinaryMode == null) {
            checkBinaryMode();
        }
        Integer num = BinaryMode;
        return num != null && num.equals(2);
    }

    private static void checkBinaryMode() {
        String str = Build.TYPE;
        if (str == null) {
            BinaryMode = null;
        } else if ("user".equals(str)) {
            BinaryMode = 1;
        } else if ("eng".equals(str)) {
            BinaryMode = 2;
        } else {
            BinaryMode = 0;
        }
    }
}
