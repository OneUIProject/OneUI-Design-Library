package de.dlyt.yanndroid.oneui.sesl.support;

import android.os.Build;
import android.view.PointerIcon;
import android.view.View;

public class ViewSupport {
    public static void setPointerIcon(View view, int type) {
        if (view != null && Build.VERSION.SDK_INT >= 24) {
            view.setPointerIcon(PointerIcon.getSystemIcon(view.getContext(), type));
        }
    }
}
