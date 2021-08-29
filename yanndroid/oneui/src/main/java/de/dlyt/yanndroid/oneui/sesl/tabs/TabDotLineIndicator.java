package de.dlyt.yanndroid.oneui.sesl.tabs;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.util.TypedValue;

public class TabDotLineIndicator extends AbsIndicatorView {
    private final float mScaleFromDiff;
    private int mDiameter;
    private int mInterval;
    private Paint mPaint;
    private float mScaleFrom;
    private int mWidth;

    public TabDotLineIndicator(Context context) {
        this(context, null);
    }

    public TabDotLineIndicator(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TabDotLineIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public TabDotLineIndicator(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        mDiameter = (int) TypedValue.applyDimension(1, 2.5f, context.getResources().getDisplayMetrics());
        mInterval = (int) TypedValue.applyDimension(1, 2.5f, context.getResources().getDisplayMetrics());

        mScaleFromDiff = TypedValue.applyDimension(1, 5.0f, context.getResources().getDisplayMetrics());

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

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        updateDotLineScaleFrom();
        if ((isPressed() || isSelected()) && (getBackground() instanceof ColorDrawable)) {
            int width = (getWidth() - getPaddingStart()) - getPaddingEnd();
            float height = ((float) getHeight()) / 2.0f;
            canvas.drawRoundRect(0.0f, height - (((float) mDiameter) / 2.0f), (float) width, height + (((float) mDiameter) / 2.0f), (float) mDiameter, (float) mDiameter, this.mPaint);
        }
    }
}
