package de.dlyt.yanndroid.oneui.colorpicker;

import android.content.Context;
import android.graphics.Color;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Set;

import de.dlyt.yanndroid.oneui.colorpicker.util.color.SpenReverseColorTheme;

public class SpenPickerReverseColorTheme extends SpenReverseColorTheme implements SpenIPickerColorTheme {
    private final int MAX_CURRENT_VALUE = 1200;
    private float[][] mPaletteColor;

    public SpenPickerReverseColorTheme(Context context) {
        super(context);
        initPaletteColor();
    }

    @Override
    public void close() {
        super.close();
        this.mPaletteColor = null;
    }

    @Override
    public boolean getContentColor(float[] fArr, float[] fArr2) {
        setSearchScope(8);
        getColor(fArr, fArr2);
        setSearchScope(8);
        return true;
    }

    private void initPaletteColor() {
        HashMap<Integer, Integer> paletteHash = getPaletteHash();
        if (paletteHash != null) {
            Set<Integer> keySet = paletteHash.keySet();
            this.mPaletteColor = (float[][]) Array.newInstance(float.class, keySet.size(), 3);
            float[] fArr = {0.0f, 0.0f, 0.0f};
            int i = 0;
            for (Integer num : keySet) {
                Color.colorToHSV(num.intValue(), fArr);
                float[][] fArr2 = this.mPaletteColor;
                fArr2[i][0] = fArr[0];
                fArr2[i][1] = fArr[1];
                fArr2[i][2] = fArr[2];
                i++;
            }
        }
    }

    public class Point3 {
        private static final double PI = 3.14159d;
        private double x;
        private double y;
        private double z;

        Point3(float[] fArr) {
            setColor(fArr[0], fArr[1], fArr[2]);
        }

        public void setColor(float f, float f2, float f3) {
            double d = f2;
            double d2 = (float) ((((double) f) * PI) / 180.0d);
            this.x = (float) (Math.cos(d2) * d);
            this.y = (float) (d * Math.sin(d2));
            this.z = f3;
        }

        public double getDistance(Point3 point3) {
            return Math.sqrt(Math.pow(Math.abs(this.x - point3.x), 2.0d) + Math.pow(Math.abs(this.y - point3.y), 2.0d) + Math.pow(Math.abs(this.z - point3.z), 2.0d));
        }
    }
}
