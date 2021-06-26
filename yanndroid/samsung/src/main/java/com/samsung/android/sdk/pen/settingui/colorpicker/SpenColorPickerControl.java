package com.samsung.android.sdk.pen.settingui.colorpicker;

import com.samsung.android.sdk.pen.settingui.util.SpenSettingUtil;

public class SpenColorPickerControl {
    private float[] mHsv;
    private float[] mOldHsv;
    private SpenColorPickerActionListener mPickerActionListener;
    private SpenColorPickerChangedListener mPickerChangedListener;
    private int mPickerMode;
    private final SpenColorPickerView.ColorListener mPickerColorListener = new SpenColorPickerView.ColorListener() {

        @Override
        public void onColorSelected(float f, float f2, float f3, int i) {
            SpenColorPickerControl.this.mHsv[0] = f;
            SpenColorPickerControl.this.mHsv[1] = f2;
            SpenColorPickerControl.this.mHsv[2] = f3;
            if (SpenColorPickerControl.this.mPickerChangedListener != null) {
                float[] fArr = new float[3];
                System.arraycopy(SpenColorPickerControl.this.mHsv, 0, fArr, 0, 3);
                SpenColorPickerControl.this.mPickerChangedListener.onColorChanged(SpenSettingUtil.HSVToColor(fArr), fArr);
            }
            if (SpenColorPickerControl.this.mPickerActionListener == null) {
                return;
            }
            if (i == 3) {
                SpenColorPickerControl.this.mPickerActionListener.onRecentColorSelected();
            } else if (i == 1) {
                SpenColorPickerControl.this.mPickerActionListener.onColorPickerChanged(SpenColorPickerControl.this.mPickerMode);
            } else if (i == 2) {
                SpenColorPickerControl.this.mPickerActionListener.onColorSeekBarChanged();
            }
        }
    };
    private final SpenColorPickerView.OnModeChangeListener mPickerModeChangedListener = new SpenColorPickerView.OnModeChangeListener() {

        @Override
        public void onModeChanged(int i) {
            SpenColorPickerControl.this.mPickerMode = i;
            if (SpenColorPickerControl.this.mPickerChangedListener != null) {
                SpenColorPickerControl.this.mPickerChangedListener.onViewModeChanged(SpenColorPickerControl.this.mPickerMode);
            }
        }
    };
    private SpenColorPickerView mPickerView;

    SpenColorPickerControl(int i, float[] fArr) {
        this.mPickerMode = i;
        this.mOldHsv = new float[3];
        this.mHsv = new float[3];
        System.arraycopy(fArr, 0, this.mOldHsv, 0, 3);
        System.arraycopy(fArr, 0, this.mHsv, 0, 3);
    }

    public void close() {
        this.mPickerView = null;
        this.mHsv = null;
        this.mOldHsv = null;
    }

    public void setColor(float[] fArr, float[] fArr2) {
        float[] fArr3 = this.mOldHsv;
        if (fArr3 == null || fArr == null || fArr2 == null) {
            return;
        }
        System.arraycopy(fArr, 0, fArr3, 0, 3);
        System.arraycopy(fArr2, 0, this.mHsv, 0, 3);
        SpenColorPickerView spenColorPickerView = this.mPickerView;
        if (spenColorPickerView != null) {
            spenColorPickerView.setColor(fArr, fArr2);
        }
    }

    public boolean getOldColor(float[] fArr) {
        if (fArr == null || this.mOldHsv == null || fArr.length < 3) {
            return false;
        }
        for (int i = 0; i < 3; i++) {
            fArr[i] = this.mOldHsv[i];
        }
        return true;
    }

    public boolean getCurrentColor(float[] fArr) {
        if (fArr == null || this.mHsv == null || fArr.length < 3) {
            return false;
        }
        for (int i = 0; i < 3; i++) {
            fArr[i] = this.mHsv[i];
        }
        return true;
    }

    public void setCurrentColor(float[] fArr) {
        SpenColorPickerView spenColorPickerView;
        if (fArr == null || (spenColorPickerView = this.mPickerView) == null) {
            return;
        }
        spenColorPickerView.setCurrentColor(fArr);
        System.arraycopy(fArr, 0, this.mHsv, 0, 3);
    }

    public void setColorPickerChangeListener(SpenColorPickerChangedListener spenColorPickerChangedListener) {
        this.mPickerChangedListener = spenColorPickerChangedListener;
    }

    public void setColorPickerActionListener(SpenColorPickerActionListener spenColorPickerActionListener) {
        this.mPickerActionListener = spenColorPickerActionListener;
    }

    public int getViewMode() {
        return this.mPickerMode;
    }

    public void setViewMode(int i) {
        if (i != 2 && i != 1) {
        } else if (i == this.mPickerMode) {
        } else {
            this.mPickerMode = i;
            SpenColorPickerView spenColorPickerView = this.mPickerView;
            if (spenColorPickerView != null) {
                spenColorPickerView.setMode(this.mPickerMode);
            }
        }
    }

    public void setPickerView(SpenColorPickerView spenColorPickerView) {
        this.mPickerView = spenColorPickerView;
        SpenColorPickerView spenColorPickerView2 = this.mPickerView;
        if (spenColorPickerView2 != null) {
            spenColorPickerView2.setColorListener(this.mPickerColorListener);
            this.mPickerView.setModeChangeListener(this.mPickerModeChangedListener);
        }
    }
}
