package com.samsung.android.sdk.pen.settingui.colorpicker;

import android.content.Context;
import android.graphics.Color;

import com.samsung.android.sdk.pen.util.color.SpenColorMatching;
import com.samsung.android.sdk.pen.util.color.SpenReverseColorTheme;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Set;

public class SpenPickerReverseColorTheme extends SpenReverseColorTheme implements SpenIPickerColorTheme {
    private final int MAX_CURRENT_VALUE = 1200;
    private SpenColorMatching mColorMatching = new SpenColorMatching();
    private float[][] mPaletteColor;

    public SpenPickerReverseColorTheme(Context context) {
        super(context);
        initPaletteColor();
    }

    @Override
    public void close() {
        super.close();
        this.mColorMatching = null;
        this.mPaletteColor = null;
    }

    public boolean getOldColor(float[] fArr, float[] fArr2) {
        float[] fArr3 = new float[3];
        matchColor(fArr, fArr3);
        getColor(fArr3, fArr2);
        return true;
    }

    public boolean getColorWithinPicker(float[] fArr, float[] fArr2) {
        setSearchScope(1);
        getColor(fArr, fArr2);
        setSearchScope(8);
        return true;
    }

    @Override
    public boolean getContentColor(float[] fArr, float[] fArr2) {
        setSearchScope(8);
        getColor(fArr, fArr2);
        setSearchScope(8);
        return true;
    }

    private double findInPickerColor(float[] fArr, float[] fArr2) {
        SpenColorMatching spenColorMatching = this.mColorMatching;
        if (spenColorMatching == null || !spenColorMatching.matchColor(fArr)) {
            return 1200.0d;
        }
        this.mColorMatching.getResultColor(fArr2);
        double resultValue = this.mColorMatching.getResultValue();
        this.mColorMatching.clearMatchedData();
        return resultValue;
    }

    public boolean matchColor(float[] fArr, float[] fArr2) {
        Point3 point3 = new Point3(fArr);
        Point3 point32 = null;
        double findInPickerColor = findInPickerColor(fArr, fArr2);
        int i = 0;
        int i2 = -1;
        while (true) {
            float[][] fArr3 = this.mPaletteColor;
            if (i >= fArr3.length) {
                i = i2;
                break;
            }
            if (point32 == null) {
                point32 = new Point3(fArr3[i]);
            } else {
                point32.setColor(fArr3[i][0], fArr3[i][1], fArr3[i][2]);
            }
            double distance = point3.getDistance(point32);
            if (distance == 0.0d) {
                break;
            }
            if (findInPickerColor > distance) {
                i2 = i;
                findInPickerColor = distance;
            }
            i++;
        }
        if (i > -1) {
            copyColor(this.mPaletteColor[i], fArr2);
        }
        return findInPickerColor != 1200.0d;
    }

    private void copyColor(float[] fArr, float[] fArr2) {
        if (fArr != null && fArr2 != null) {
            fArr2[0] = fArr[0];
            fArr2[1] = fArr[1];
            fArr2[2] = fArr[2];
        }
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
