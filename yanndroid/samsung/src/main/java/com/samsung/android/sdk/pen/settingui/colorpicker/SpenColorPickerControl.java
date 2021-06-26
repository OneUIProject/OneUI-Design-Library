package com.samsung.android.sdk.pen.settingui.colorpicker;

import android.util.Log;

import com.samsung.android.sdk.pen.settingui.util.SpenSettingUtil;

/* access modifiers changed from: package-private */
public class SpenColorPickerControl {
    private static final String TAG = "SpenColorPickerControl";
    private float[] mHsv;
    private float[] mOldHsv;
    private SpenColorPickerActionListener mPickerActionListener;
    private SpenColorPickerChangedListener mPickerChangedListener;
    private final SpenColorPickerView.ColorListener mPickerColorListener = new SpenColorPickerView.ColorListener() {
        /* class com.samsung.android.sdk.pen.settingui.colorpicker.SpenColorPickerControl.AnonymousClass1 */

        @Override // com.samsung.android.sdk.pen.settingui.colorpicker.SpenColorPickerView.ColorListener
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
    private int mPickerMode;
    private final SpenColorPickerView.OnModeChangeListener mPickerModeChangedListener = new SpenColorPickerView.OnModeChangeListener() {
        /* class com.samsung.android.sdk.pen.settingui.colorpicker.SpenColorPickerControl.AnonymousClass2 */

        @Override // com.samsung.android.sdk.pen.settingui.colorpicker.SpenColorPickerView.OnModeChangeListener
        public void onModeChanged(int i) {
            Log.i(SpenColorPickerControl.TAG, "onModeChanged() mode=" + i);
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

    /* access modifiers changed from: package-private */
    public void close() {
        this.mPickerView = null;
        this.mHsv = null;
        this.mOldHsv = null;
    }

    public void setColor(float[] fArr, float[] fArr2) {
        float[] fArr3 = this.mOldHsv;
        if (fArr3 == null || fArr == null || fArr2 == null) {
            Log.i(TAG, "setColor() invalid state.");
            return;
        }
        System.arraycopy(fArr, 0, fArr3, 0, 3);
        System.arraycopy(fArr2, 0, this.mHsv, 0, 3);
        SpenColorPickerView spenColorPickerView = this.mPickerView;
        if (spenColorPickerView != null) {
            spenColorPickerView.setColor(fArr, fArr2);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean getOldColor(float[] fArr) {
        if (fArr == null || this.mOldHsv == null || fArr.length < 3) {
            Log.i(TAG, "getOldColor() - array null");
            return false;
        }
        for (int i = 0; i < 3; i++) {
            fArr[i] = this.mOldHsv[i];
        }
        return true;
    }

    public boolean getCurrentColor(float[] fArr) {
        if (fArr == null || this.mHsv == null || fArr.length < 3) {
            Log.i(TAG, "getCurrentColor() - array null");
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
            Log.i(TAG, "setCurrentColor() invalid state.");
            return;
        }
        spenColorPickerView.setCurrentColor(fArr);
        System.arraycopy(fArr, 0, this.mHsv, 0, 3);
        Log.i(TAG, String.format("setCurrentColor() - step2 [H,S,V]=[%f,%f,%f]", Float.valueOf(this.mHsv[0]), Float.valueOf(this.mHsv[1]), Float.valueOf(this.mHsv[2])));
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
            Log.i(TAG, "Invalid view mode=" + i);
        } else if (i == this.mPickerMode) {
            Log.i(TAG, "[Same mode] current=" + this.mPickerMode);
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
