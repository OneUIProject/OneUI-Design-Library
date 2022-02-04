package de.dlyt.yanndroid.oneui.sesl.colorpicker.classic;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.widget.SeekBar;

import de.dlyt.yanndroid.oneui.R;

class SeslOpacitySeekBar extends SeekBar {
    private static final int SEEKBAR_MAX_VALUE = 255;
    private static final String TAG = "SeslOpacitySeekBar";
    private int[] mColors = new int[]{-1, -16777216};
    private GradientDrawable mProgressDrawable;

    public SeslOpacitySeekBar(Context var1, AttributeSet var2) {
        super(var1, var2);
    }

    private void initColor(int var1) {
        float[] var2 = new float[3];
        Color.colorToHSV(var1, var2);
        var1 = Color.alpha(var1);
        this.mColors[0] = Color.HSVToColor(0, var2);
        this.mColors[1] = Color.HSVToColor(255, var2);
        this.setProgress(var1);
    }

    void changeColorBase(int var1) {
        GradientDrawable var2 = this.mProgressDrawable;
        if (var2 != null) {
            int[] var3 = this.mColors;
            var3[1] = var1;
            var2.setColors(var3);
            this.setProgressDrawable(this.mProgressDrawable);
            this.setProgress(this.getMax());
        }

    }

    void init(Integer var1) {
        this.setMax(255);
        if (var1 != null) {
            this.initColor(var1);
        }

        this.mProgressDrawable = (GradientDrawable) this.getContext().getDrawable(R.drawable.sesl_color_picker_opacity_seekbar);
        this.setProgressDrawable(this.mProgressDrawable);
        this.setThumb(this.getContext().getResources().getDrawable(R.drawable.sesl_color_picker_seekbar_cursor, getContext().getTheme()));
        this.setThumbOffset(0);
        this.setSplitTrack(false);
    }

    void restoreColor(int var1) {
        this.initColor(var1);
        this.mProgressDrawable.setColors(this.mColors);
        this.setProgressDrawable(this.mProgressDrawable);
    }
}
