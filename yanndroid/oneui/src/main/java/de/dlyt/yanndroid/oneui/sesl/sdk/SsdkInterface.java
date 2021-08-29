package de.dlyt.yanndroid.oneui.sesl.sdk;

import android.content.Context;

public interface SsdkInterface {
    int getVersionCode();

    String getVersionName();

    void initialize(Context context) throws SsdkUnsupportedException;

    boolean isFeatureEnabled(int i);
}