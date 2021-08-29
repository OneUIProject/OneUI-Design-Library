package de.dlyt.yanndroid.oneui.sesl.colorpicker;

import android.graphics.Color;

import de.dlyt.yanndroid.oneui.sesl.colorpicker.util.SpenSettingUtil;

public class SpenHSVColor {
    float[] mHsvColor = {0.0f, 0.0f, 0.0f};

    SpenHSVColor(int i) {
        Color.colorToHSV(i, this.mHsvColor);
    }

    SpenHSVColor(float f, float f2, float f3) {
        float[] fArr = this.mHsvColor;
        fArr[0] = f;
        fArr[1] = f2;
        fArr[2] = f3;
    }

    SpenHSVColor(float[] fArr) {
        setColor(fArr);
    }

    public boolean setColor(float[] fArr) {
        if (fArr.length < 3) {
            return false;
        }
        for (int i = 0; i < 3; i++) {
            this.mHsvColor[i] = fArr[i];
        }
        return true;
    }

    public int getRGB() {
        return SpenSettingUtil.HSVToColor(this.mHsvColor);
    }

    public boolean getHSV(float[] fArr) {
        if (fArr.length < 3) {
            return false;
        }
        for (int i = 0; i < 3; i++) {
            fArr[i] = this.mHsvColor[i];
        }
        return true;
    }

    public boolean isSameColor(float[] fArr) {
        float[] fArr2 = this.mHsvColor;
        return fArr2[0] == fArr[0] && fArr2[1] == fArr[1] && fArr2[2] == fArr[2];
    }
}
