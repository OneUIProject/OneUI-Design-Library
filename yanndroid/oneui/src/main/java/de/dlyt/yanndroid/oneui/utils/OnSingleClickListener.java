package de.dlyt.yanndroid.oneui.utils;

import android.os.SystemClock;
import android.view.View;

public abstract class OnSingleClickListener implements View.OnClickListener {
    public long mLastClickTime;

    public OnSingleClickListener() {
    }

    public void onClick(View view) {
        long uptimeMillis = SystemClock.uptimeMillis();
        if (uptimeMillis - mLastClickTime > 600L) {
            onSingleClick(view);
        }
        mLastClickTime = uptimeMillis;
    }

    public abstract void onSingleClick(View view);
}
