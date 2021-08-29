package de.dlyt.yanndroid.oneui.sesl.colorpicker.util.color;

public class SpenNormalColorTheme implements SpenIColorTheme {
    @Override
    public void close() {
    }

    @Override
    public int getColor(int i) {
        return i;
    }

    @Override
    public boolean getColor(float[] fArr, float[] fArr2) {
        if (fArr == null || fArr2 == null) {
            return false;
        }
        System.arraycopy(fArr, 0, fArr2, 0, fArr.length);
        return true;
    }
}
