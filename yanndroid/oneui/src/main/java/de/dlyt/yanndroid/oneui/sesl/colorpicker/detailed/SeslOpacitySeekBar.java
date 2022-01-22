package de.dlyt.yanndroid.oneui.sesl.colorpicker.detailed;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;

import androidx.core.view.ViewCompat;

import de.dlyt.yanndroid.oneui.R;
import de.dlyt.yanndroid.oneui.widget.SeekBar;

@SuppressLint("AppCompatCustomView")
class SeslOpacitySeekBar extends SeekBar {
    private static final int SEEKBAR_MAX_VALUE = 255;
    private static final String TAG = "SeslOpacitySeekBar";
    private int[] mColors = {-1, ViewCompat.MEASURED_STATE_MASK};
    private GradientDrawable mProgressDrawable;

    public SeslOpacitySeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    void init(Integer num) {
        setMax(255);
        if (num != null) {
            initColor(num);
        }
        mProgressDrawable = (GradientDrawable) getContext().getDrawable(R.drawable.sesl_color_picker_opacity_seekbar);
        setProgressDrawable(mProgressDrawable);
        setThumb(getContext().getResources().getDrawable(R.drawable.sesl_color_picker_seekbar_cursor));
        setThumbOffset(0);
    }

    private void initColor(int i) {
        float[] fArr = new float[3];
        Color.colorToHSV(i, fArr);
        int alpha = Color.alpha(i);
        mColors[0] = Color.HSVToColor(0, fArr);
        mColors[1] = Color.HSVToColor(255, fArr);
        setProgress(alpha);
    }

    void restoreColor(int i) {
        initColor(i);
        mProgressDrawable.setColors(mColors);
        setProgressDrawable(mProgressDrawable);
    }

    void changeColorBase(int i, int i2) {
        if (mProgressDrawable != null) {
            int[] iArr = mColors;
            iArr[1] = i;
            mProgressDrawable.setColors(iArr);
            setProgressDrawable(mProgressDrawable);
            float[] fArr = new float[3];
            Color.colorToHSV(i, fArr);
            mColors[0] = Color.HSVToColor(0, fArr);
            mColors[1] = Color.HSVToColor(255, fArr);
            setProgress(i2);
        }
    }
}
