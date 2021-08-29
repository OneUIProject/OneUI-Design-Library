package de.dlyt.yanndroid.oneui.sesl.colorpicker.util.color;

import android.graphics.Color;
import android.os.Build;

import androidx.core.graphics.ColorUtils;

public class SpenColorUtil {
    public static int HSVToColor(float[] fArr) {
        return HSVToColor(255, fArr);
    }

    public static int HSVToColor(int i, float[] fArr) {
        int i2;
        int i3;
        int i4 = 0;
        if (Build.VERSION.SDK_INT >= 26) {
            return Color.HSVToColor(i, fArr);
        }
        if (fArr == null || fArr.length < 3) {
            throw new RuntimeException("Invalid hsv color");
        }
        float f = 0.0f;
        float max = Math.max(Math.min(fArr[1], 1.0f), 0.0f);
        float max2 = Math.max(Math.min(fArr[2], 1.0f), 0.0f);
        int round = Math.round(max2 * 255.0f);
        if (max <= 2.4414062E-4f) {
            i3 = round & 255;
            i2 = ((i & 255) << 24) | (i3 << 16) | (i3 << 8);
        } else {
            if (fArr[0] >= 0.0f && fArr[0] < 360.0f) {
                f = fArr[0] / 60.0f;
            }
            int floor = (int) Math.floor(f);
            float f2 = f - ((float) floor);
            int round2 = Math.round((1.0f - max) * max2 * 255.0f);
            int round3 = Math.round((1.0f - (max * f2)) * max2 * 255.0f);
            int round4 = Math.round((1.0f - (max * (1.0f - f2))) * max2 * 255.0f);
            if (floor >= 6 || floor < 0) {
                throw new RuntimeException("Invalid hsv color");
            }
            if (floor != 0) {
                if (floor == 1) {
                    round4 = round2;
                    round2 = round;
                    round = round3;
                } else if (floor == 2) {
                    round2 = round;
                    round = round2;
                } else if (floor == 3) {
                    round4 = round;
                    round = round2;
                    round2 = round3;
                } else if (floor == 4) {
                    i4 = round;
                    round = round4;
                } else if (floor != 5) {
                    round4 = 0;
                    round = 0;
                    round2 = 0;
                } else {
                    round4 = round3;
                }
                i2 = ((i & 255) << 24) | ((round & 255) << 16) | ((round2 & 255) << 8);
                i3 = round4 & 255;
            } else {
                i4 = round2;
                round2 = round4;
            }
            round4 = i4;
            i2 = ((i & 255) << 24) | ((round & 255) << 16) | ((round2 & 255) << 8);
            i3 = round4 & 255;
        }
        return i2 | i3;
    }

    public static void RGBToHSL(int i, float[] fArr) {
        ColorUtils.RGBToHSL(Color.red(i), Color.green(i), Color.blue(i), fArr);
    }

    public static int HSLToRGB(float[] fArr) {
        return ColorUtils.HSLToColor(fArr);
    }
}
