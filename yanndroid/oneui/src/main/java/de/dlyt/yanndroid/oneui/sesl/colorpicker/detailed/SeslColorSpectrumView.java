package de.dlyt.yanndroid.oneui.sesl.colorpicker.detailed;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;

import de.dlyt.yanndroid.oneui.R;

class SeslColorSpectrumView extends View {
    private static final int ROUNDED_CORNER_RADIUS = 4;
    private static final float STROKE_WIDTH = 2.0f;
    private static final String TAG = "SeslColorSpectrumView";
    private final int[] HUE_COLORS = {-65281, -16776961, -16711681, -16711936, -256, -65536};
    private final int ROUNDED_CORNER_RADIUS_IN_Px;
    private Drawable cursorDrawable;
    private final Context mContext;
    private Paint mCursorPaint;
    private final int mCursorPaintSize;
    private float mCursorPosX;
    private float mCursorPosY;
    private final int mCursorStrokeSize;
    private Paint mHuePaint;
    private SpectrumColorChangedListener mListener;
    private final Resources mResources;
    private Paint mSaturationPaint;
    private Rect mSpectrumRect;
    private Paint mStrokePaint;

    interface SpectrumColorChangedListener {
        void onSpectrumColorChanged(float f, float f2);
    }

    void setOnSpectrumColorChangedListener(SpectrumColorChangedListener spectrumColorChangedListener) {
        mListener = spectrumColorChangedListener;
    }

    public SeslColorSpectrumView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mResources = context.getResources();
        mSpectrumRect = new Rect(0, 0, mResources.getDimensionPixelSize(R.dimen.sesl_color_picker_oneui_3_color_swatch_view_width), mResources.getDimensionPixelSize(R.dimen.sesl_color_picker_oneui_3_color_spectrum_view_height));
        mCursorPaintSize = mResources.getDimensionPixelSize(R.dimen.sesl_color_picker_spectrum_cursor_paint_size);
        mCursorStrokeSize = mResources.getDimensionPixelSize(R.dimen.sesl_color_picker_spectrum_cursor_paint_size) + (mResources.getDimensionPixelSize(R.dimen.sesl_color_picker_spectrum_cursor_out_stroke_size) * 2);
        ROUNDED_CORNER_RADIUS_IN_Px = dpToPx(ROUNDED_CORNER_RADIUS);
        init();
    }

    private void init() {
        mCursorPaint = new Paint();
        mStrokePaint = new Paint();
        mStrokePaint.setStyle(Paint.Style.STROKE);
        mStrokePaint.setColor(mResources.getColor(R.color.sesl_color_picker_stroke_color_spectrumview));
        mStrokePaint.setStrokeWidth(STROKE_WIDTH);
        cursorDrawable = mResources.getDrawable(R.drawable.sesl_color_picker_gradient_wheel_cursor);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //Math.sqrt(Math.pow((double) event.getX(), 2.0d) + Math.pow((double) event.getY(), 2.0d));
        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            playSoundEffect(SoundEffectConstants.CLICK);
        } else if (action == MotionEvent.ACTION_MOVE && getParent() != null) {
            getParent().requestDisallowInterceptTouchEvent(true);
        }

        float x = event.getX();
        float y = event.getY();
        if (x > ((float) mSpectrumRect.width())) {
            x = (float) mSpectrumRect.width();
        }
        if (y > ((float) mSpectrumRect.height())) {
            y = (float) mSpectrumRect.height();
        }
        if (x < 0.0f) {
            x = 0.0f;
        }
        if (y <= 7.0f) {
            y = 7.0f;
        }

        mCursorPosX = x;
        mCursorPosY = y;
        float width = ((x - ((float) mSpectrumRect.left)) / ((float) mSpectrumRect.width())) * 300.0f;
        float height = (mCursorPosY - ((float) mSpectrumRect.top)) / ((float) mSpectrumRect.height());
        float[] fArr = new float[3];
        fArr[0] = Math.max(width, 0.0f);
        fArr[1] = height;

        if (mListener != null) {
            mListener.onSpectrumColorChanged(fArr[0], fArr[1]);
        } else {
            Log.d(TAG, "Listener is not set.");
        }
        invalidate();
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        mSpectrumRect = canvas.getClipBounds();

        mHuePaint = new Paint(1);
        mHuePaint.setShader(new LinearGradient((float) mSpectrumRect.right, (float) mSpectrumRect.top, (float) mSpectrumRect.left, (float) mSpectrumRect.top, HUE_COLORS, (float[]) null, Shader.TileMode.CLAMP));
        mHuePaint.setStyle(Paint.Style.FILL);

        mSaturationPaint = new Paint(1);
        mSaturationPaint.setShader(new LinearGradient((float) mSpectrumRect.left, (float) mSpectrumRect.top, (float) mSpectrumRect.left, (float) mSpectrumRect.bottom, -1, 0, Shader.TileMode.CLAMP));

        canvas.drawRoundRect(mSpectrumRect.left, mSpectrumRect.top, mSpectrumRect.right, mSpectrumRect.bottom, (float) ROUNDED_CORNER_RADIUS_IN_Px, (float) ROUNDED_CORNER_RADIUS_IN_Px, mHuePaint);
        canvas.drawRoundRect(mSpectrumRect.left, mSpectrumRect.top, mSpectrumRect.right, mSpectrumRect.bottom, (float) ROUNDED_CORNER_RADIUS_IN_Px, (float) ROUNDED_CORNER_RADIUS_IN_Px, mSaturationPaint);
        canvas.drawRoundRect(mSpectrumRect.left, mSpectrumRect.top, mSpectrumRect.right, mSpectrumRect.bottom, (float) ROUNDED_CORNER_RADIUS_IN_Px, (float) ROUNDED_CORNER_RADIUS_IN_Px, mStrokePaint);

        if (mCursorPosX < ((float) mSpectrumRect.left)) {
            mCursorPosX = (float) mSpectrumRect.left;
        }
        if (mCursorPosY <= ((float) (mSpectrumRect.top + 7))) {
            mCursorPosY = (float) (mSpectrumRect.top + 7);
        }
        if (mCursorPosX > ((float) mSpectrumRect.right)) {
            mCursorPosX = (float) mSpectrumRect.right;
        }
        if (mCursorPosY > ((float) mSpectrumRect.bottom)) {
            mCursorPosY = (float) mSpectrumRect.bottom;
        }

        canvas.drawCircle(mCursorPosX, mCursorPosY, ((float) mCursorPaintSize) / STROKE_WIDTH, mCursorPaint);

        cursorDrawable.setBounds(((int) mCursorPosX) - (mCursorPaintSize / 2), ((int) mCursorPosY) - (mCursorPaintSize / 2), ((int) mCursorPosX) + (mCursorPaintSize / 2), ((int) mCursorPosY) + (mCursorPaintSize / 2));
        cursorDrawable.draw(canvas);

        setDrawingCacheEnabled(true);
    }

    void setColor(int i) {
        float[] fArr = new float[3];
        Color.colorToHSV(i, fArr);
        updateCursorPosition(i, fArr);
    }

    public void updateCursorPosition(int i, float[] fArr) {
        if (mSpectrumRect != null) {
            mCursorPosX = ((float) mSpectrumRect.left) + ((((float) mSpectrumRect.width()) * fArr[0]) / 300.0f);
            mCursorPosY = ((float) mSpectrumRect.top) + (((float) mSpectrumRect.height()) * fArr[1]);
            Log.d(TAG, "updateCursorPosition() HSV[" + fArr[0] + ", " + fArr[1] + ", " + fArr[1] + "] mCursorPosX=" + mCursorPosX + " mCursorPosY=" + mCursorPosY);
        }
        invalidate();
    }

    void updateCursorColor(int i) {
        Log.i(TAG, "updateCursorColor color " + i);
        mCursorPaint.setColor(i);
    }

    private static int dpToPx(int i) {
        return (int) (((float) i) * Resources.getSystem().getDisplayMetrics().density);
    }
}
