package de.dlyt.yanndroid.oneui.support;

import android.app.Activity;
import android.os.Build;

import com.samsung.android.sdk.multiwindow.SMultiWindowActivity;

public class WindowManagerSupport {
    public static boolean isMultiWindowMode(Activity activity) {
        if (Build.VERSION.SDK_INT >= 24) {
            return activity.isInMultiWindowMode();
        } else if (DeviceInfo.isSdlDevice()) {
            return new SMultiWindowActivity(activity).isMultiWindow();
        } else
            return false;
    }
}
