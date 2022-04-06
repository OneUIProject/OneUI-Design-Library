package de.dlyt.yanndroid.oneui.sesl.swiperefreshlayout;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.PathInterpolator;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.core.util.Preconditions;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import de.dlyt.yanndroid.oneui.R;

public class CircularProgressDrawable extends Drawable implements Animatable {
    private static final int ANIMATION_DURATION = 1683;
    private static final int ARROW_HEIGHT = 5;
    private static final int ARROW_HEIGHT_LARGE = 6;
    private static final float ARROW_SCALE_DURATION_OFFSET = 0.2f;
    private static final int ARROW_WIDTH = 10;
    private static final int ARROW_WIDTH_LARGE = 12;
    private static final float CENTER_RADIUS = 7.5f;
    private static final float CENTER_RADIUS_LARGE = 11.0f;
    private static final float COLOR_CHANGE_OFFSET = 0.75f;
    public static final int DEFAULT = 1;
    private static final int FINISHING_ANIMATION_DURATION = 1000;
    private static final float GROUP_FULL_ROTATION = 216.0f;
    public static final int LARGE = 0;
    private static final Interpolator LINEAR_INTERPOLATOR = new LinearInterpolator();
    private static final Interpolator MATERIAL_INTERPOLATOR = new FastOutSlowInInterpolator();
    private static final float MAX_PROGRESS_ARC = 0.8f;
    private static final float MIN_PROGRESS_ARC = 0.01f;
    private static final float RING_ROTATION = 0.25f;
    private static final float SHRINK_OFFSET = 0.4f;
    private static final Interpolator SINE_IN_OUT_33 = new PathInterpolator(0.33f, 0.0f, 0.67f, 1.0f);
    private static final Interpolator SINE_IN_OUT_80 = new PathInterpolator(0.33f, 0.0f, 0.2f, 1.0f);
    private static final Interpolator SINE_OUT_60 = new PathInterpolator(0.17f, 0.17f, SHRINK_OFFSET, 1.0f);
    private static final float STROKE_WIDTH = 2.5f;
    private static final float STROKE_WIDTH_LARGE = 3.0f;
    private OnAnimationEndCallback mAnimationEndCallback = null;
    private Animator mAnimator;
    private boolean mEnding;
    boolean mFinishing;
    private Resources mResources;
    private final Ring mRing;
    private float mRotation;
    float mRotationCount;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({LARGE, DEFAULT})
    public @interface ProgressDrawableSize {
    }

    public interface OnAnimationEndCallback {
        void OnAnimationEnd();
    }

    @SuppressLint("RestrictedApi")
    public CircularProgressDrawable(@NonNull Context context) {
        mResources = Preconditions.checkNotNull(context).getResources();

        mRing = new Ring();

        TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorPrimary, value, true);
        mRing.setColors(new int[]{value.data, context.getResources().getColor(R.color.sesl_swipe_refresh_color1)});

        setStrokeWidth(STROKE_WIDTH);
        setupAnimators();
    }

    private void setSizeParameters(float centerRadius, float strokeWidth, float arrowWidth, float arrowHeight) {
        final Ring ring = mRing;
        final DisplayMetrics metrics = mResources.getDisplayMetrics();
        final float screenDensity = metrics.density;

        ring.setStrokeWidth(strokeWidth * screenDensity);
        ring.setCenterRadius(centerRadius * screenDensity);
        ring.setColorIndex(0);
        ring.setArrowDimensions(arrowWidth * screenDensity, arrowHeight * screenDensity);
    }

    public void setStyle(@ProgressDrawableSize int size) {
        if (size == LARGE) {
            setSizeParameters(CENTER_RADIUS_LARGE, STROKE_WIDTH_LARGE, ARROW_WIDTH_LARGE, ARROW_HEIGHT_LARGE);
        } else {
            setSizeParameters(CENTER_RADIUS, STROKE_WIDTH, ARROW_WIDTH, ARROW_HEIGHT);
        }
        invalidateSelf();
    }

    public float getStrokeWidth() {
        return mRing.getStrokeWidth();
    }

    public void setStrokeWidth(float strokeWidth) {
        mRing.setStrokeWidth(strokeWidth);
        invalidateSelf();
    }

    public float getCenterRadius() {
        return mRing.getCenterRadius();
    }

    public void setCenterRadius(float centerRadius) {
        mRing.setCenterRadius(centerRadius);
        invalidateSelf();
    }

    public void setStrokeCap(@NonNull Paint.Cap strokeCap) {
        mRing.setStrokeCap(strokeCap);
        invalidateSelf();
    }

    @NonNull
    public Paint.Cap getStrokeCap() {
        return mRing.getStrokeCap();
    }

    public float getArrowWidth() {
        return mRing.getArrowWidth();
    }

    public float getArrowHeight() {
        return mRing.getArrowHeight();
    }

    public void setArrowDimensions(float width, float height) {
        mRing.setArrowDimensions(width, height);
        invalidateSelf();
    }

    public boolean getArrowEnabled() {
        return mRing.getShowArrow();
    }

    public void setArrowEnabled(boolean show) {
        mRing.setShowArrow(show);
        invalidateSelf();
    }

    public float getArrowScale() {
        return mRing.getArrowScale();
    }

    public void setArrowScale(float scale) {
        mRing.setArrowScale(scale);
        invalidateSelf();
    }

    public float getStartTrim() {
        return mRing.getStartTrim();
    }

    public float getEndTrim() {
        return mRing.getEndTrim();
    }

    public void setStartEndTrim(float start, float end) {
        mRing.setStartTrim(start);
        mRing.setEndTrim(end);
        invalidateSelf();
    }

    public float getProgressRotation() {
        return mRing.getRotation();
    }

    public void setProgressRotation(float rotation) {
        mRing.setRotation(rotation);
        invalidateSelf();
    }

    public int getBackgroundColor() {
        return mRing.getBackgroundColor();
    }

    public void setBackgroundColor(int color) {
        mRing.setBackgroundColor(color);
        invalidateSelf();
    }

    @NonNull
    public int[] getColorSchemeColors() {
        return mRing.getColors();
    }

    public void setColorSchemeColors(@NonNull int... colors) {
        mRing.setColors(colors);
        mRing.setColorIndex(0);
        invalidateSelf();
    }

    @Override
    public void draw(Canvas canvas) {
        final Rect bounds = getBounds();
        canvas.save();
        canvas.rotate(mRotation, bounds.exactCenterX(), bounds.exactCenterY());
        mRing.draw(canvas, bounds);
        canvas.restore();
    }

    @Override
    public void setAlpha(int alpha) {
        mRing.setAlpha(alpha);
        invalidateSelf();
    }

    @Override
    public int getAlpha() {
        return mRing.getAlpha();
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        mRing.setColorFilter(colorFilter);
        invalidateSelf();
    }

    private void setRotation(float rotation) {
        mRotation = rotation;
    }

    @SuppressWarnings("UnusedMethod")
    private float getRotation() {
        return mRotation;
    }

    @Override
    @SuppressWarnings("deprecation")
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    public boolean isRunning() {
        return mAnimator.isRunning();
    }

    @Override
    public void start() {
        mAnimator.cancel();
        mRing.storeOriginals();
        if (mRing.getEndTrim() != mRing.getStartTrim()) {
            mFinishing = true;
            mAnimator.setDuration(FINISHING_ANIMATION_DURATION);
            ((ValueAnimator) mAnimator).setRepeatCount(mAnimationEndCallback != null ? 0 : -1);
            mEnding = false;
            mAnimator.start();
        } else {
            mRing.setColorIndex(0);
            mRing.resetOriginals();
            mAnimator.setDuration(ANIMATION_DURATION);
            mAnimator.start();
        }
    }

    @Override
    public void stop() {
        mAnimator.cancel();
        setRotation(0);
        mRing.setShowArrow(false);
        mRing.setColorIndex(0);
        mRing.resetOriginals();
        invalidateSelf();
    }

    private int evaluateColorChange(float fraction, int startValue, int endValue) {
        int startA = (startValue >> 24) & 0xff;
        int startR = (startValue >> 16) & 0xff;
        int startG = (startValue >> 8) & 0xff;
        int startB = startValue & 0xff;

        int endA = (endValue >> 24) & 0xff;
        int endR = (endValue >> 16) & 0xff;
        int endG = (endValue >> 8) & 0xff;
        int endB = endValue & 0xff;

        return (startA + (int) (fraction * (endA - startA))) << 24 | (startR + (int) (fraction * (endR - startR))) << 16 | (startG + (int) (fraction * (endG - startG))) << 8 | (startB + (int) (fraction * (endB - startB)));
    }

    @SuppressWarnings("WeakerAccess")
    void updateRingColor(float interpolatedTime, Ring ring) {
        if (interpolatedTime > COLOR_CHANGE_OFFSET) {
            ring.setColor(evaluateColorChange((interpolatedTime - COLOR_CHANGE_OFFSET) / (1f - COLOR_CHANGE_OFFSET), ring.getStartingColor(), ring.getNextColor()));
        } else {
            ring.setColor(ring.getStartingColor());
        }
    }

    private void applyFinishTranslation(float interpolatedTime, Ring ring) {
        updateRingColor(interpolatedTime, ring);
        if (interpolatedTime <= ARROW_SCALE_DURATION_OFFSET) {
            ring.setArrowScale(1.0f - SINE_IN_OUT_33.getInterpolation(interpolatedTime / ARROW_SCALE_DURATION_OFFSET));
        }
        float targetRotation = (float) (Math.floor(ring.getStartingRotation() / MAX_PROGRESS_ARC) + 1f);
        final float startTrim = ring.getStartingStartTrim() + (ring.getStartingEndTrim() - MIN_PROGRESS_ARC - ring.getStartingStartTrim()) * SINE_OUT_60.getInterpolation(interpolatedTime);
        ring.setStartTrim(startTrim);
        ring.setEndTrim(ring.getStartingEndTrim());
        final float rotation = ring.getStartingRotation() + ((targetRotation - ring.getStartingRotation()) * SINE_OUT_60.getInterpolation(interpolatedTime));
        ring.setRotation(rotation);
        if (mAnimationEndCallback != null && !mEnding && SINE_OUT_60.getInterpolation(interpolatedTime) > 0.7f) {
            mAnimationEndCallback.OnAnimationEnd();
            mEnding = true;
        }
    }

    @SuppressWarnings("WeakerAccess")
    void applyTransformation(float interpolatedTime, Ring ring, boolean lastFrame) {
        if (mFinishing) {
            applyFinishTranslation(interpolatedTime, ring);
        } else if (interpolatedTime != 1f || lastFrame) {
            final float startingRotation = ring.getStartingRotation();
            float startTrim, endTrim;

            if (interpolatedTime <= SHRINK_OFFSET) {
                final float scaledTime = interpolatedTime / SHRINK_OFFSET;
                startTrim = ring.getStartingStartTrim();
                endTrim = startTrim + ((MAX_PROGRESS_ARC - MIN_PROGRESS_ARC) * SINE_IN_OUT_80.getInterpolation(scaledTime) + MIN_PROGRESS_ARC);
            } else {
                float scaledTime = (interpolatedTime - SHRINK_OFFSET) / (1f - SHRINK_OFFSET);
                endTrim = ring.getStartingStartTrim();
                startTrim = endTrim - ((MAX_PROGRESS_ARC - MIN_PROGRESS_ARC) * (1f - SINE_IN_OUT_80.getInterpolation(scaledTime)) + MIN_PROGRESS_ARC);
            }

            final float rotation = startingRotation + (RING_ROTATION * SINE_IN_OUT_33.getInterpolation(interpolatedTime));
            float groupRotation = GROUP_FULL_ROTATION * (interpolatedTime + mRotationCount);

            ring.setStartTrim(startTrim);
            ring.setEndTrim(endTrim);
            ring.setRotation(rotation);
            setRotation(groupRotation);
        }
    }

    private void setupAnimators() {
        final Ring ring = mRing;
        final ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float interpolatedTime = (float) animation.getAnimatedValue();
                updateRingColor(interpolatedTime, ring);
                applyTransformation(interpolatedTime, ring, false);
                invalidateSelf();
            }
        });
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.RESTART);
        animator.setInterpolator(LINEAR_INTERPOLATOR);
        animator.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animator) {
                mRotationCount = 0;
            }

            @Override
            public void onAnimationEnd(Animator animator) {
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
                applyTransformation(1f, ring, true);
                ring.storeOriginals();
                ring.goToNextColor();
                if (mFinishing) {
                    mFinishing = false;
                    animator.cancel();
                    animator.setDuration(ANIMATION_DURATION);
                    animator.start();
                    ring.setShowArrow(false);
                } else {
                    mRotationCount = mRotationCount + 1;
                }
            }
        });
        mAnimator = animator;
    }

    public void setOnAnimationEndCallback(OnAnimationEndCallback onAnimationEndCallback) {
        mAnimationEndCallback = onAnimationEndCallback;
    }

    private static class Ring {
        int mAlpha = 255;
        Path mArrow;
        int mArrowHeight;
        final Paint mArrowPaint = new Paint();
        float mArrowScale = 1.0f;
        int mArrowWidth;
        final Paint mCirclePaint = new Paint();
        int mColorIndex;
        int[] mColors;
        int mCurrentColor;
        float mEndTrim = 0.0f;
        final Paint mPaint = new Paint();
        float mRingCenterRadius;
        float mRotation = 0.0f;
        boolean mShowArrow;
        float mStartTrim = 0.0f;
        float mStartingEndTrim;
        float mStartingRotation;
        float mStartingStartTrim;
        float mStrokeWidth = 5.0f;
        final RectF mTempBounds = new RectF();

        Ring() {
            mPaint.setStrokeCap(Paint.Cap.SQUARE);
            mPaint.setAntiAlias(true);
            mPaint.setStyle(Style.STROKE);

            mArrowPaint.setStyle(Paint.Style.FILL);
            mArrowPaint.setAntiAlias(true);

            mCirclePaint.setColor(Color.TRANSPARENT);
        }

        void setArrowDimensions(float width, float height) {
            mArrowWidth = (int) width;
            mArrowHeight = (int) height;
        }

        void setStrokeCap(Paint.Cap strokeCap) {
            mPaint.setStrokeCap(strokeCap);
        }

        Paint.Cap getStrokeCap() {
            return mPaint.getStrokeCap();
        }

        float getArrowWidth() {
            return mArrowWidth;
        }

        float getArrowHeight() {
            return mArrowHeight;
        }

        void draw(Canvas c, Rect bounds) {
            final RectF arcBounds = mTempBounds;
            float arcRadius = mRingCenterRadius + mStrokeWidth / 2f;
            if (mRingCenterRadius <= 0) {
                arcRadius = Math.min(bounds.width(), bounds.height()) / 2f - Math.max((mArrowWidth * mArrowScale) / 2f, mStrokeWidth / 2f);
            }
            arcBounds.set(bounds.centerX() - arcRadius, bounds.centerY() - arcRadius, bounds.centerX() + arcRadius, bounds.centerY() + arcRadius);

            final float startAngle = (mStartTrim + mRotation) * 360;
            final float endAngle = (mEndTrim + mRotation) * 360;
            float sweepAngle = endAngle - startAngle;

            mPaint.setColor(mCurrentColor);
            mPaint.setAlpha(mAlpha);

            float inset = mStrokeWidth / 2f;
            arcBounds.inset(inset, inset);
            c.drawCircle(arcBounds.centerX(), arcBounds.centerY(), arcBounds.width() / 2f, mCirclePaint);
            arcBounds.inset(-inset, -inset);

            c.drawArc(arcBounds, startAngle, sweepAngle, false, mPaint);

            drawTriangle(c, startAngle, sweepAngle, arcBounds);
        }

        void drawTriangle(Canvas c, float startAngle, float sweepAngle, RectF bounds) {
            if (mShowArrow) {
                if (mArrow == null) {
                    mArrow = new android.graphics.Path();
                    mArrow.setFillType(android.graphics.Path.FillType.EVEN_ODD);
                } else {
                    mArrow.reset();
                }
                float centerRadius = Math.min(bounds.width(), bounds.height()) / 2f;
                float inset = mArrowWidth * mArrowScale / 2f;
                mArrow.moveTo(0, 0);
                mArrow.lineTo(mArrowWidth * mArrowScale, 0);
                mArrow.lineTo((mArrowWidth * mArrowScale / 2), (mArrowHeight * mArrowScale));
                mArrow.offset(centerRadius + bounds.centerX() - inset, bounds.centerY() + mStrokeWidth / 2f);
                mArrow.close();
                mArrowPaint.setColor(mCurrentColor);
                mArrowPaint.setAlpha(mAlpha);
                c.save();
                c.rotate(startAngle + sweepAngle, bounds.centerX(), bounds.centerY());
                c.drawPath(mArrow, mArrowPaint);
                c.restore();
            }
        }

        void setColors(@NonNull int[] colors) {
            mColors = colors;
            setColorIndex(0);
        }

        int[] getColors() {
            return mColors;
        }

        void setColor(int color) {
            mCurrentColor = color;
        }

        void setBackgroundColor(int color) {
            mCirclePaint.setColor(color);
        }

        int getBackgroundColor() {
            return mCirclePaint.getColor();
        }

        void setColorIndex(int index) {
            mColorIndex = index;
            mCurrentColor = mColors[mColorIndex];
        }

        int getNextColor() {
            return mColors[getNextColorIndex()];
        }

        int getNextColorIndex() {
            return (mColorIndex + 1) % mColors.length;
        }

        void goToNextColor() {
            setColorIndex(getNextColorIndex());
        }

        void setColorFilter(ColorFilter filter) {
            mPaint.setColorFilter(filter);
        }

        void setAlpha(int alpha) {
            mAlpha = alpha;
        }

        int getAlpha() {
            return mAlpha;
        }

        void setStrokeWidth(float strokeWidth) {
            mStrokeWidth = strokeWidth;
            mPaint.setStrokeWidth(strokeWidth);
        }

        float getStrokeWidth() {
            return mStrokeWidth;
        }

        void setStartTrim(float startTrim) {
            mStartTrim = startTrim;
        }

        float getStartTrim() {
            return mStartTrim;
        }

        float getStartingStartTrim() {
            return mStartingStartTrim;
        }

        float getStartingEndTrim() {
            return mStartingEndTrim;
        }

        int getStartingColor() {
            return mColors[mColorIndex];
        }

        void setEndTrim(float endTrim) {
            mEndTrim = endTrim;
        }

        float getEndTrim() {
            return mEndTrim;
        }

        void setRotation(float rotation) {
            mRotation = rotation;
        }

        float getRotation() {
            return mRotation;
        }

        void setCenterRadius(float centerRadius) {
            mRingCenterRadius = centerRadius;
        }

        float getCenterRadius() {
            return mRingCenterRadius;
        }

        void setShowArrow(boolean show) {
            if (mShowArrow != show) {
                mShowArrow = show;
            }
        }

        boolean getShowArrow() {
            return mShowArrow;
        }

        void setArrowScale(float scale) {
            if (scale != mArrowScale) {
                mArrowScale = scale;
            }
        }

        float getArrowScale() {
            return mArrowScale;
        }

        float getStartingRotation() {
            return mStartingRotation;
        }

        void storeOriginals() {
            mStartingStartTrim = mStartTrim;
            mStartingEndTrim = mEndTrim;
            mStartingRotation = mRotation;
        }

        void resetOriginals() {
            mStartingStartTrim = 0;
            mStartingEndTrim = 0;
            mStartingRotation = 0;
            setStartTrim(0);
            setEndTrim(0);
            setRotation(0);
        }
    }
}