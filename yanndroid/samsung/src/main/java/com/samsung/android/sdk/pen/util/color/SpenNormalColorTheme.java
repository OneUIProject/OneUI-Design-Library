package com.samsung.android.sdk.pen.util.color;

public class SpenNormalColorTheme implements SpenIColorTheme {
    @Override // com.samsung.android.sdk.pen.util.color.SpenIColorTheme
    public void close() {
    }

    @Override // com.samsung.android.sdk.pen.util.color.SpenIColorTheme
    public int getColor(int i) {
        return i;
    }

    @Override // com.samsung.android.sdk.pen.util.color.SpenIColorTheme
    public boolean getColor(float[] fArr, float[] fArr2) {
        if (fArr == null || fArr2 == null) {
            return false;
        }
        System.arraycopy(fArr, 0, fArr2, 0, fArr.length);
        return true;
    }
}
