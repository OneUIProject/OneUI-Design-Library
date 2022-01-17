package de.dlyt.yanndroid.oneui.sesl.colorpicker.detailed;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;

import androidx.core.view.ViewCompat;

import de.dlyt.yanndroid.oneui.R;
import de.dlyt.yanndroid.oneui.view.SeekBar;

@SuppressLint("AppCompatCustomView")
class SeslGradientColorSeekBar extends SeekBar {
    private static final int SEEKBAR_MAX_VALUE = 100;
    private static final String TAG = "SeslGradientColorSeekBar";
    private final Context mContext;
    private final Resources mResources;
    private int[] mColors = {ViewCompat.MEASURED_STATE_MASK, -1};
    private GradientDrawable mProgressDrawable = (GradientDrawable) getContext().getDrawable(R.drawable.sesl_color_picker_gradient_seekbar_drawable);

    public SeslGradientColorSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mResources = context.getResources();
    }

    void init(Integer num) {
        setMax(100);
        if (num != null) {
            initColor(num);
        }
        initProgressDrawable();
        initThumb();
    }

    private void initColor(int i) {
        float[] fArr = new float[3];
        Color.colorToHSV(i, fArr);
        float f = fArr[2];
        fArr[2] = 1.0f;
        mColors[1] = Color.HSVToColor(fArr);
        setProgress(Math.round(f * ((float) getMax())));
    }

    void restoreColor(int i) {
        if (mProgressDrawable != null) {
            initColor(i);
            mProgressDrawable.setColors(mColors);
            setProgressDrawable(mProgressDrawable);
        }
    }

    void changeColorBase(int i) {
        if (mProgressDrawable != null) {
            int[] iArr = mColors;
            iArr[1] = i;
            mProgressDrawable.setColors(iArr);
            setProgressDrawable(mProgressDrawable);
            float[] fArr = new float[3];
            Color.colorToHSV(i, fArr);
            float f = fArr[2];
            fArr[2] = 1.0f;
            mColors[1] = Color.HSVToColor(fArr);
            setProgress(Math.round(f * ((float) getMax())));
        }
    }

    private void initProgressDrawable() {
        setProgressDrawable(mProgressDrawable);
    }

    private void initThumb() {
        setThumb(getContext().getDrawable(R.drawable.sesl_color_picker_seekbar_cursor));
        setThumbOffset(0);
    }

    private static Drawable resizeDrawable(Context context, BitmapDrawable bitmapDrawable, int i, int i2) {
        if (bitmapDrawable == null) {
            return null;
        }
        Bitmap bitmap = bitmapDrawable.getBitmap();
        Matrix matrix = new Matrix();
        float f = 0.0f;
        float width = i > 0 ? ((float) i) / ((float) bitmap.getWidth()) : 0.0f;
        if (i2 > 0) {
            f = ((float) i2) / ((float) bitmap.getHeight());
        }
        matrix.postScale(width, f);
        return new BitmapDrawable(context.getResources(), Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true));
    }
}
