package com.samsung.android.sdk.pen.settingui.colorpicker;

import android.content.Context;

import com.samsung.android.sdk.pen.util.color.SpenIColorTheme;
import com.samsung.android.sdk.pen.util.color.SpenNormalColorTheme;
import com.samsung.android.sdk.pen.util.color.SpenReverseColorTheme;

public class SpenColorPickerTheme {
    private SpenIColorTheme mColorTheme;
    private Context mContext;
    private float[] mOldColor = new float[3];
    private SpenIPickerColorTheme mPickerColorTheme;

    SpenColorPickerTheme(Context context, float[] fArr) {
        this.mContext = context;
        System.arraycopy(fArr, 0, this.mOldColor, 0, 3);
        this.mColorTheme = new SpenPickerNormalColorTheme();
        this.mPickerColorTheme = (SpenIPickerColorTheme) this.mColorTheme;
    }

    public void close() {
        this.mContext = null;
        this.mOldColor = null;
        closeTheme();
    }

    private void closeTheme() {
        SpenIColorTheme spenIColorTheme = this.mColorTheme;
        if (spenIColorTheme != null) {
            spenIColorTheme.close();
            this.mColorTheme = null;
            this.mPickerColorTheme = null;
        }
    }

    public int getTheme() {
        SpenIColorTheme spenIColorTheme = this.mColorTheme;
        if (spenIColorTheme == null) {
            return -1;
        }
        if (spenIColorTheme instanceof SpenNormalColorTheme) {
            return 0;
        }
        return spenIColorTheme instanceof SpenReverseColorTheme ? 1 : -1;
    }

    public void setTheme(int i) {
        int theme = getTheme();
        if (theme != -1 && theme != i) {
            closeTheme();
            if (i == 0) {
                this.mColorTheme = new SpenPickerNormalColorTheme();
                this.mPickerColorTheme = (SpenIPickerColorTheme) this.mColorTheme;
            } else if (i == 1) {
                this.mColorTheme = new SpenPickerReverseColorTheme(this.mContext);
                this.mPickerColorTheme = (SpenIPickerColorTheme) this.mColorTheme;
            }
        }
    }

    public boolean getColor(float[] fArr, float[] fArr2) {
        SpenIColorTheme spenIColorTheme = this.mColorTheme;
        if (spenIColorTheme != null) {
            return spenIColorTheme.getColor(fArr, fArr2);
        }
        return false;
    }

    public boolean getContentColor(float[] fArr, float[] fArr2) {
        SpenIPickerColorTheme spenIPickerColorTheme = this.mPickerColorTheme;
        if (spenIPickerColorTheme != null) {
            return spenIPickerColorTheme.getContentColor(fArr, fArr2);
        }
        return false;
    }

    public boolean getOldVisibleColor(float[] fArr) {
        SpenIColorTheme spenIColorTheme = this.mColorTheme;
        if (spenIColorTheme != null) {
            return spenIColorTheme.getColor(this.mOldColor, fArr);
        }
        return false;
    }
}
