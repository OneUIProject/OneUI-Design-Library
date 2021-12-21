package de.dlyt.yanndroid.oneui.sesl.swiperefreshlayout;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Animatable2;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
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

public class OUI4CircularProgressDrawable extends Drawable implements Animatable {
    private static final int ANIMATION_DURATION = 200;
    private static final float CENTER_RADIUS = 14.0f;
    private static final float CENTER_RADIUS_LARGE = 20.0f;
    public static final int DEFAULT = 1;
    public static final int LARGE = 0;
    private static final Interpolator LINEAR_INTERPOLATOR = new LinearInterpolator();
    private static final Interpolator MATERIAL_INTERPOLATOR = new FastOutSlowInInterpolator();
    private static final Interpolator SINE_OUT_60 = new PathInterpolator(0.17f, 0.17f, 0.4f, 1.0f);
    private OnAnimationEndCallback mAnimationEndCallback = null;
    private Animator mAnimator;
    private Drawable mDotAnimation;
    private final FourDot mFourDot;
    private Resources mResources;
    private Animator mRotateAnimtior;
    private float mRotation;
    float mRotationCount;
    private final float mScreenDensity;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({LARGE, DEFAULT})
    public @interface ProgressDrawableSize {
    }

    public interface OnAnimationEndCallback {
        void OnAnimationEnd();
    }

    @SuppressLint("RestrictedApi")
    public OUI4CircularProgressDrawable(@NonNull Context context) {
        mResources = Preconditions.checkNotNull(context).getResources();
        mFourDot = new FourDot();
        TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorPrimary, value, true);
        mFourDot.setColors(new int[]{context.getResources().getColor(R.color.sesl_swipe_refresh_color1), value.data});
        mDotAnimation = context.getResources().getDrawable(R.drawable.sesl_swipe_refresh_animated, context.getTheme());
        mScreenDensity = mResources.getDisplayMetrics().density;
        mDotAnimation.setAlpha(0);
        mFourDot.setDotAnimtion(this.mDotAnimation);
        setupAnimators();
    }

    private void setSizeParameters(float centerRadius) {
        mFourDot.setDotRadius(mScreenDensity * 2.25f);
        mFourDot.setCenterRadius(centerRadius * this.mScreenDensity);
    }

    public void setStyle(@ProgressDrawableSize int size) {
        if (size == LARGE) {
            setSizeParameters(CENTER_RADIUS_LARGE);
        } else {
            setSizeParameters(CENTER_RADIUS);
        }
        invalidateSelf();
    }

    public float getCenterRadius() {
        return mFourDot.getCenterRadius();
    }

    public void setCenterRadius(float centerRadius) {
        mFourDot.setCenterRadius(centerRadius);
        invalidateSelf();
    }

    public void setScale(float scale) {
        if (scale == 0.0f) {
            mFourDot.setScale(0.0f);
        } else {
            mFourDot.setScale(Math.min(scale * 11.0f * mScreenDensity, mScreenDensity * 11.0f));
        }
        invalidateSelf();
    }

    public float getProgressRotation() {
        return mFourDot.getRotation();
    }

    public void setProgressRotation(float rotation) {
        mFourDot.setRotation(rotation);
        invalidateSelf();
    }

    @NonNull
    public int[] getColorSchemeColors() {
        return mFourDot.getColors();
    }

    public void setColorSchemeColors(@NonNull int... colors) {
        mFourDot.setColors(colors);
        invalidateSelf();
    }

    @Override
    public void draw(Canvas canvas) {
        final Rect bounds = getBounds();
        canvas.save();
        canvas.rotate(mRotation, bounds.exactCenterX(), bounds.exactCenterY());
        mFourDot.draw(canvas, bounds);
        canvas.restore();
    }

    @Override
    public void setAlpha(int alpha) {
        mFourDot.setAlpha(alpha);
        invalidateSelf();
    }

    @Override
    public int getAlpha() {
        return mFourDot.getAlpha();
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        mFourDot.setColorFilter(colorFilter);
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
        return mAnimator.isRunning() || ((AnimatedVectorDrawable) mDotAnimation).isRunning();
    }

    @Override
    public void start() {
        mAnimator.cancel();
        mRotateAnimtior.cancel();
        mAnimator.start();
        mRotateAnimtior.start();
    }

    @Override
    public void stop() {
        ((AnimatedVectorDrawable) mDotAnimation).stop();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ((AnimatedVectorDrawable) mDotAnimation).clearAnimationCallbacks();
        }
        mDotAnimation.setAlpha(0);
        mFourDot.setPosition(0.0f);
        mFourDot.setIsRunning(false);
        mAnimator.cancel();
        mRotateAnimtior.cancel();
        setRotation(0.0f);
        invalidateSelf();
    }

    private void setupAnimators() {
        ValueAnimator ofInt = ValueAnimator.ofInt(0, 90);
        ValueAnimator ofFloat = ValueAnimator.ofFloat(0.0f, 10.0f);
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float floatValue = (Float) animation.getAnimatedValue();
                mFourDot.setPosition(mScreenDensity * floatValue);
                mFourDot.setScale((mScreenDensity * 11.0f) + (((floatValue * 0.75f) * mScreenDensity) / 10.0f));
                invalidateSelf();
            }
        });
        ofInt.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mFourDot.setRotation((float) (Integer) animation.getAnimatedValue());
                invalidateSelf();
            }
        });
        ofInt.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mFourDot.setRotation(0.0f);
            }
        });
        ofFloat.setInterpolator(SINE_OUT_60);
        ofFloat.setDuration(200L);
        ofInt.setInterpolator(LINEAR_INTERPOLATOR);
        ofInt.setDuration(200L);
        ofFloat.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mRotationCount = 0.0f;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            public void onAnimationEnd(Animator animation) {
                mFourDot.setRotation(0.0f);
                mFourDot.setIsRunning(true);
                mDotAnimation.setAlpha(255);
                mFourDot.setAlpha(0);
                startDotAnimation();
            }
        });
        mAnimator = ofFloat;
        mRotateAnimtior = ofInt;
    }

    private void startDotAnimation() {
        ((AnimatedVectorDrawable) mDotAnimation).start();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ((AnimatedVectorDrawable) mDotAnimation).registerAnimationCallback(new Animatable2.AnimationCallback() {
                @Override
                public void onAnimationEnd(Drawable drawable) {
                    if (mAnimationEndCallback != null) {
                        mAnimationEndCallback.OnAnimationEnd();
                    }
                    ((AnimatedVectorDrawable) mDotAnimation).start();
                    invalidateSelf();
                }
            });
        }
    }

    public void setOnAnimationEndCallback(OnAnimationEndCallback callback) {
        mAnimationEndCallback = callback;
    }

    private static class FourDot {
        int mAlpha = 255;
        float mArrowScale = 1.0f;
        float mCenterRadius;
        int mColorIndex;
        int[] mColors;
        Drawable mDotAnimation;
        final Paint mDotPaint = new Paint();
        float mDotRadius;
        boolean mIsRunning;
        final Paint mPaint = new Paint();
        float mPosition = 0.0f;
        float mRotation = 0.0f;
        float mScale = 1.0f;

        FourDot() {
            mPaint.setStrokeCap(Paint.Cap.SQUARE);
            mPaint.setAntiAlias(true);
            mPaint.setStyle(Paint.Style.FILL);

            mDotPaint.setStrokeCap(Paint.Cap.SQUARE);
            mDotPaint.setAntiAlias(true);
            mDotPaint.setStyle(Paint.Style.FILL);
        }

        void draw(Canvas c, Rect bounds) {
            RectF rectF = new RectF();
            rectF.set(((float) bounds.centerX()) - mCenterRadius, ((float) bounds.centerY()) - mCenterRadius, ((float) bounds.centerX()) + mCenterRadius, ((float) bounds.centerY()) + mCenterRadius);

            mPaint.setColor(mColors[0]);
            mDotPaint.setColor(mColors[1]);
            mPaint.setAlpha(mAlpha);
            mDotPaint.setAlpha(mAlpha);

            c.rotate(mRotation, rectF.centerX(), rectF.centerY());

            if (mScale != 0.0f) {
                c.drawCircle(rectF.centerX(), rectF.centerY() + mPosition, mDotRadius, mDotPaint);
                c.drawCircle(rectF.centerX() - mPosition, rectF.centerY(), mDotRadius, mDotPaint);
                c.drawCircle(rectF.centerX() + mPosition, rectF.centerY(), mDotRadius, mDotPaint);
            }
            c.drawCircle(rectF.centerX(), rectF.centerY() - mPosition, mCenterRadius - mScale, mPaint);

            if (mIsRunning) {
                mDotAnimation.setBounds((int) rectF.left, (int) rectF.top, (int) rectF.right, (int) rectF.bottom);
                mDotAnimation.draw(c);
            }
        }

        void setColors(@NonNull int[] colors) {
            mColors = colors;
        }

        int[] getColors() {
            return mColors;
        }

        void setPosition(float position) {
            mPosition = position;
        }

        void setDotAnimtion(Drawable drawable) {
            mDotAnimation = drawable;
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

        int getStartingColor() {
            return mColors[mColorIndex];
        }

        void setRotation(float rotation) {
            mRotation = rotation;
        }

        float getRotation() {
            return mRotation;
        }

        void setIsRunning(boolean z) {
            mIsRunning = z;
        }

        void setCenterRadius(float centerRadius) {
            mCenterRadius = centerRadius;
        }

        void setDotRadius(float radius) {
            mDotRadius = radius;
        }

        float getCenterRadius() {
            return mCenterRadius;
        }

        void setScale(float scale) {
            if (scale != mScale) {
                mScale = scale;
            }
        }

        float getArrowScale() {
            return mArrowScale;
        }
    }
}