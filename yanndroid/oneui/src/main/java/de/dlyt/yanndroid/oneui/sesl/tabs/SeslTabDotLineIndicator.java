package de.dlyt.yanndroid.oneui.sesl.tabs;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;

public class SeslTabDotLineIndicator extends SeslAbsIndicatorView {
    private static final float CIRCLE_INTERVAL = 2.5f;
    private static final float DIAMETER_SIZE = 2.5f;
    private static final int SCALE_DIFF = 5;
    private final int mDiameter;
    private final int mInterval;
    private Paint mPaint;
    private float mScaleFrom;
    private final float mScaleFromDiff;
    private int mWidth;

    public SeslTabDotLineIndicator(Context context) {
        this(context, null);
    }

    public SeslTabDotLineIndicator(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SeslTabDotLineIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public SeslTabDotLineIndicator(Context context, AttributeSet attributeSet, int defStyleAttr, int defStyleRes) {
        super(context, attributeSet, defStyleAttr, defStyleRes);

        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();

        mDiameter = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, CIRCLE_INTERVAL, displayMetrics);
        mInterval = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DIAMETER_SIZE, displayMetrics);
        mScaleFromDiff = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, SCALE_DIFF, displayMetrics);

        mPaint = new Paint();
        mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
    }

    @Override
    void onHide() {
        setAlpha(0.0f);
    }

    @Override
    void onShow() {
        startReleaseEffect();
    }

    @Override
    void startPressEffect() {
        setAlpha(1.0f);
        invalidate();
    }

    @Override
    void startReleaseEffect() {
        setAlpha(1.0f);
    }

    private void updateDotLineScaleFrom() {
        if (mWidth != getWidth() || mWidth == 0) {
            mWidth = getWidth();
            if (mWidth <= 0) {
                mScaleFrom = 0.9f;
            } else {
                mScaleFrom = (((float) mWidth) - mScaleFromDiff) / ((float) mWidth);
            }
        }
    }

    @Override
    void onSetSelectedIndicatorColor(int color) {
        mPaint.setColor(color);
    }

    // kang
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        updateDotLineScaleFrom();
        if ((isPressed() || isSelected()) && (getBackground() instanceof ColorDrawable)) {
            int width = (getWidth() - getPaddingStart()) - getPaddingEnd();
            float height = ((float) getHeight()) / 2.0f;
            float f = ((float) mDiameter) / 2.0f;
            canvas.drawRoundRect(0.0f, height - f, (float) width, height + f, (float) mDiameter, (float) mDiameter, this.mPaint);
        }
    }
    // kang
}
