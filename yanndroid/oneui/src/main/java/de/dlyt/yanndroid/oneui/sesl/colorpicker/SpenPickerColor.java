package de.dlyt.yanndroid.oneui.sesl.colorpicker;

import android.graphics.Color;

import de.dlyt.yanndroid.oneui.sesl.colorpicker.util.SpenSettingUtil;

public class SpenPickerColor {
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
        if (this.mChangedWho == null || this.mColor != i) {
            this.mHSVColor.getHSV(fArr);
            this.mChangedWho = str;
            this.mColor = i;
            Color.colorToHSV(i, fArr);
            this.mHSVColor.setColor(fArr);
            notifyChanged(this.mChangedWho);
            return;
        }
    }

    public void setColor(String str, int i, float f, float f2, float f3) {
        float[] fArr = {f, f2, f3};
        if (this.mChangedWho == null || !this.mHSVColor.isSameColor(fArr)) {
            float[] fArr2 = {0.0f, 0.0f, 0.0f};
            this.mHSVColor.getHSV(fArr2);
            this.mChangedWho = str;
            this.mHSVColor.setColor(fArr);
            this.mColor = SpenSettingUtil.HSVToColor(i, fArr);
            notifyChanged(this.mChangedWho);
            return;
        }
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
