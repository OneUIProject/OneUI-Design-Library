package com.samsung.android.sdk.pen.settingui.colorpicker;

import android.graphics.Color;
import android.util.Log;

import com.samsung.android.sdk.pen.settingui.util.SpenSettingUtil;

public class SpenPickerColor {
    private static final String TAG = "SpenPickerColor";
    private String mChangedWho = null;
    private int mColor = 0;
    private SpenPickerColorEventManager mEventManager = new SpenPickerColorEventManager();
    private SpenHSVColor mHSVColor = new SpenHSVColor(new float[]{0.0f, 0.0f, 0.0f});

    private boolean isSameOpacityColor(int i, int i2) {
        return ((i & 16777215) | -16777216) == ((i2 & 16777215) | -16777216);
    }

    public void close() {
        SpenPickerColorEventManager spenPickerColorEventManager = this.mEventManager;
        if (spenPickerColorEventManager != null) {
            spenPickerColorEventManager.close();
            this.mEventManager = null;
        }
        this.mHSVColor = null;
        this.mChangedWho = null;
    }

    public int getAlpha() {
        return (this.mColor >> 24) & 255;
    }

    public int getColor() {
        return this.mColor;
    }

    public boolean getColor(float[] fArr) {
        SpenHSVColor spenHSVColor = this.mHSVColor;
        if (spenHSVColor != null) {
            return spenHSVColor.getHSV(fArr);
        }
        return false;
    }

    public String whoChanged() {
        return this.mChangedWho;
    }

    public void setColor(String str, int i) {
        float[] fArr = {0.0f, 0.0f, 0.0f};
        Log.d(TAG, "setColor() int color=" + i + " [" + String.format("#%X", Integer.valueOf(i)) + "]");
        if (this.mChangedWho == null || this.mColor != i) {
            this.mHSVColor.getHSV(fArr);
            Log.i(TAG, String.format("setColor(int) OLD[#%X, %f, %f, %f]", Integer.valueOf(this.mColor), Float.valueOf(fArr[0]), Float.valueOf(fArr[1]), Float.valueOf(fArr[2])));
            this.mChangedWho = str;
            this.mColor = i;
            Color.colorToHSV(i, fArr);
            this.mHSVColor.setColor(fArr);
            Log.i(TAG, String.format("setColor(int) NEW[#%X, %f, %f, %f]", Integer.valueOf(this.mColor), Float.valueOf(fArr[0]), Float.valueOf(fArr[1]), Float.valueOf(fArr[2])));
            notifyChanged(this.mChangedWho);
            return;
        }
        Log.i(TAG, "Not Changed. color=" + i + String.format("#%X", Integer.valueOf(i)));
    }

    public void setColor(String str, int i, float f, float f2, float f3) {
        float[] fArr = {f, f2, f3};
        if (this.mChangedWho == null || !this.mHSVColor.isSameColor(fArr)) {
            float[] fArr2 = {0.0f, 0.0f, 0.0f};
            this.mHSVColor.getHSV(fArr2);
            Log.i(TAG, String.format("setColor(int, float, float, float) OLD[#%X, %f, %f, %f]", Integer.valueOf(this.mColor), Float.valueOf(fArr2[0]), Float.valueOf(fArr2[1]), Float.valueOf(fArr2[2])));
            this.mChangedWho = str;
            this.mHSVColor.setColor(fArr);
            this.mColor = SpenSettingUtil.HSVToColor(i, fArr);
            Log.i(TAG, String.format("setColor(int, float, float, float) NEW[#%X, %f, %f, %f]", Integer.valueOf(this.mColor), Float.valueOf(fArr[0]), Float.valueOf(fArr[1]), Float.valueOf(fArr[2])));
            notifyChanged(this.mChangedWho);
            return;
        }
        Log.i(TAG, String.format("Not Changed. Color[%f, %f, %f] Alpha=%d", Float.valueOf(f), Float.valueOf(f2), Float.valueOf(f3), Integer.valueOf(i)));
    }

    public void addEventListener(SpenPickerColorEventListener spenPickerColorEventListener) {
        SpenPickerColorEventManager spenPickerColorEventManager = this.mEventManager;
        if (spenPickerColorEventManager != null) {
            spenPickerColorEventManager.subscribe(spenPickerColorEventListener);
        }
    }

    public void removeEventListener(SpenPickerColorEventListener spenPickerColorEventListener) {
        SpenPickerColorEventManager spenPickerColorEventManager = this.mEventManager;
        if (spenPickerColorEventManager != null) {
            spenPickerColorEventManager.unsubscribe(spenPickerColorEventListener);
        }
    }

    private void notifyChanged(String str) {
        if (this.mEventManager != null) {
            float[] fArr = {0.0f, 0.0f, 0.0f};
            if (this.mHSVColor.getHSV(fArr)) {
                this.mEventManager.notify(str, this.mColor, fArr);
            }
        }
    }
}
