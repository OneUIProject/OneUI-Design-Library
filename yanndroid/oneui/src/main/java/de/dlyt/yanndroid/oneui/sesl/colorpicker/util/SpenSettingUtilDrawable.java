package de.dlyt.yanndroid.oneui.sesl.colorpicker.util;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.view.View;

public class SpenSettingUtilDrawable {
    private static final boolean SUPPORT_FOREGROUND = (Build.VERSION.SDK_INT >= 23);

    public static void setRoundedCornerBackground(View view, int i, int i2, int i3, int i4) {
        if (view != null) {
            if (SUPPORT_FOREGROUND) {
                view.setBackground(getRoundedCornerDrawable(i, i2, 0, 0));
                view.setForeground(getRoundedCornerDrawable(i, 0, i3, i4));
                return;
            }
            view.setBackground(getRoundedCornerDrawable(i, i2, i3, i4));
        }
    }

    public static Drawable getRoundedCornerDrawable(int i, int i2, int i3, int i4) {
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setShape(GradientDrawable.RECTANGLE);
        gradientDrawable.setCornerRadius((float) i);
        gradientDrawable.setColor(i2);
        if (i3 > 0) {
            gradientDrawable.setStroke(i3, i4);
        }
        return gradientDrawable;
    }

    public static Drawable getRoundedRectDrawable(float f, float f2, float f3, float f4) {
        float[] fArr = {f, f, f2, f2, f4, f4, f3, f3};
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setShape(GradientDrawable.RECTANGLE);
        gradientDrawable.setCornerRadii(fArr);
        return gradientDrawable;
    }

    public static Drawable getRoundedRectDrawable(float f, float f2, float f3, float f4, int i, int i2) {
        GradientDrawable gradientDrawable = (GradientDrawable) getRoundedRectDrawable(f, f2, f3, f4);
        gradientDrawable.setStroke(i, i2);
        return gradientDrawable;
    }

    public static void setForeground(View view, Drawable drawable, ColorStateList colorStateList) {
        if (view != null && SUPPORT_FOREGROUND) {
            view.setForeground(drawable);
            view.setForegroundTintList(colorStateList);
        }
    }
}
