package com.samsung.android.sdk.pen.settingui.colorpicker;

import com.samsung.android.sdk.pen.settingui.util.SpenSettingUtil;

class SpenColorSwatchItem {
    private static final int ADAPTIVE_SELECTOR_COLOR = 1291845632;
    private static final int DEFAULT_SELECTOR_COLOR = -1;
    private static final String TAG = "SpenColorSwathItem";
    int mSelectorColor;
    private float mHue;
    private int mRGBColor;
    private float mSaturation;
    private float mValue;

    public SpenColorSwatchItem(float f, float f2, float f3) {
        setColor(f, f2, f3);
        setSelectorColor(f2, f3);
    }


    public int getColor() {
        return this.mRGBColor;
    }

    public int getSelectorColor() {
        return this.mSelectorColor;
    }


    private void setColor(float f, float f2, float f3) {
        this.mHue = f;
        this.mSaturation = f2;
        this.mValue = f3;
        this.mRGBColor = SpenSettingUtil.HSVToColor(new float[]{f, f2, f3});
    }

    private void setSelectorColor(float f, float f2) {
        if (f2 < 0.98f || f < 0.0f || f >= 0.19f) {
            this.mSelectorColor = -1;
        } else {
            this.mSelectorColor = ADAPTIVE_SELECTOR_COLOR;
        }
    }
}
