package com.samsung.android.sdk.pen.settingui.colorpicker;

public interface SpenColorPickerChangedListener {
    public static final int VIEW_MODE_GRADIENT = 1;
    public static final int VIEW_MODE_SWATCH = 2;

    void onColorChanged(int i, float[] fArr);

    void onViewModeChanged(int i);
}
