package de.dlyt.yanndroid.oneui.sesl.recyclerview;

import android.content.Context;
import android.view.ViewConfiguration;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;

public class SeslScroller {
    public static final int DEFAULT_DURATION = 250;
    public static final float END_TENSION = 1.0f;
    public static final int FLING_MODE = 1;
    public static final float INFLEXION = 0.35f;
    public static final int NB_SAMPLES = 100;
    public static final float P1 = 0.175f;
    public static final float P2 = 0.35000002f;
    public static final int SCROLL_MODE = 0;
    public static final float[] SPLINE_POSITION = new float[101];
    public static final float[] SPLINE_TIME = new float[101];
    public static final float START_TENSION = 0.5f;
    public static float DECELERATION_RATE = ((float) (Math.log(0.78d) / Math.log(0.9d)));

    static {
        float x, y, coef;
        float x_min = 0.0f, y_min = 0.0f;
        for (int i = 0; i < 100; i++) {
            float alpha = ((float) i) / 100.0f;
            float x_max = END_TENSION;
            while (true) {
                x = x_min + ((x_max - x_min) / 2.0f);
                coef = 3.0f * x * (END_TENSION - x);
                float tx = ((((END_TENSION - x) * P1) + (P2 * x)) * coef) + (x * x * x);
                if (((double) Math.abs(tx - alpha)) < 1.0E-5d) {
                    break;
                } else if (tx > alpha) {
                    x_max = x;
                } else {
                    x_min = x;
                }
            }
            SPLINE_POSITION[i] = ((((END_TENSION - x) * START_TENSION) + x) * coef) + (x * x * x);
            float y_max = END_TENSION;
            while (true) {
                y = y_min + ((y_max - y_min) / 2.0f);
                coef = 3.0f * y * (END_TENSION - y);
                float dy = ((((END_TENSION - y) * START_TENSION) + y) * coef) + (y * y * y);
                if (((double) Math.abs(dy - alpha)) < 1.0E-5d) {
                    break;
                } else if (dy > alpha) {
                    y_max = y;
                } else {
                    y_min = y;
                }
            }
            SPLINE_TIME[i] = ((((END_TENSION - y) * P1) + (P2 * y)) * coef) + (y * y * y);
        }
        float[] fArr = SPLINE_POSITION;
        SPLINE_TIME[100] = 1.0f;
        fArr[100] = 1.0f;
    }

    public final Interpolator mInterpolator;
    public final float mPpi;
    public float mCurrVelocity;
    public int mCurrX;
    public int mCurrY;
    public float mDeceleration;
    public float mDeltaX;
    public float mDeltaY;
    public int mDistance;
    public int mDuration;
    public float mDurationReciprocal;
    public int mFinalX;
    public int mFinalY;
    public boolean mFinished;
    public float mFlingFriction;
    public boolean mFlywheel;
    public int mMaxX;
    public int mMaxY;
    public int mMinX;
    public int mMinY;
    public int mMode;
    public float mPhysicalCoeff;
    public long mStartTime;
    public int mStartX;
    public int mStartY;
    public float mVelocity;

    public SeslScroller(Context context) {
        this(context, null);
    }

    public SeslScroller(Context context, Interpolator interpolator) {
        this(context, interpolator, true);
    }

    public SeslScroller(Context context, Interpolator interpolator, boolean flywheel) {
        mFlingFriction = ViewConfiguration.getScrollFriction();
        mFinished = true;
        if (interpolator == null) {
            mInterpolator = new ViscousFluidInterpolator();
        } else {
            mInterpolator = interpolator;
        }
        mPpi = context.getResources().getDisplayMetrics().density * 160.0f;
        mDeceleration = computeDeceleration(ViewConfiguration.getScrollFriction());
        mFlywheel = flywheel;
        mPhysicalCoeff = computeDeceleration(0.84f);
    }

    public final void setFriction(float friction) {
        mDeceleration = computeDeceleration(friction);
        mFlingFriction = friction;
    }

    private float computeDeceleration(float friction) {
        return mPpi * 386.0878f * friction;
    }

    public final boolean isFinished() {
        return mFinished;
    }

    public final void forceFinished(boolean finished) {
        mFinished = finished;
    }

    public final int getDuration() {
        return mDuration;
    }

    public final int getCurrX() {
        return mCurrX;
    }

    public final int getCurrY() {
        return mCurrY;
    }

    public float getCurrVelocity() {
        if (mMode == FLING_MODE) {
            return mCurrVelocity;
        }
        return mVelocity - ((mDeceleration * ((float) timePassed())) / 2000.0f);
    }

    public final int getStartX() {
        return mStartX;
    }

    public final int getStartY() {
        return mStartY;
    }

    public final int getFinalX() {
        return mFinalX;
    }

    public void setFinalX(int newX) {
        mFinalX = newX;
        mDeltaX = (float) (mFinalX - mStartX);
        mFinished = false;
    }

    public final int getFinalY() {
        return mFinalY;
    }

    public void setFinalY(int newY) {
        mFinalY = newY;
        mDeltaY = (float) (mFinalY - mStartY);
        mFinished = false;
    }

    public boolean computeScrollOffset() {
        if (mFinished) {
            return false;
        }
        int timePassed = (int) (AnimationUtils.currentAnimationTimeMillis() - mStartTime);
        if (timePassed < mDuration) {
            switch (mMode) {
                case 0:
                    float x = mInterpolator.getInterpolation(((float) timePassed) * mDurationReciprocal);
                    mCurrX = mStartX + Math.round(mDeltaX * x);
                    mCurrY = mStartY + Math.round(mDeltaY * x);
                    break;
                case 1:
                    float t = ((float) timePassed) / ((float) mDuration);
                    int index = (int) (100.0f * t);
                    float distanceCoef = END_TENSION;
                    float velocityCoef = 0.0f;
                    if (index < 100) {
                        float t_inf = ((float) index) / 100.0f;
                        float t_sup = ((float) (index + 1)) / 100.0f;
                        float d_inf = SPLINE_POSITION[index];
                        velocityCoef = (SPLINE_POSITION[index + 1] - d_inf) / (t_sup - t_inf);
                        distanceCoef = d_inf + ((t - t_inf) * velocityCoef);
                    }
                    mCurrVelocity = ((((float) mDistance) * velocityCoef) / ((float) mDuration)) * 1000.0f;
                    mCurrX = mStartX + Math.round(((float) (mFinalX - mStartX)) * distanceCoef);
                    mCurrX = Math.min(mCurrX, mMaxX);
                    mCurrX = Math.max(mCurrX, mMinX);
                    mCurrY = mStartY + Math.round(((float) (mFinalY - mStartY)) * distanceCoef);
                    mCurrY = Math.min(mCurrY, mMaxY);
                    mCurrY = Math.max(mCurrY, mMinY);
                    if (mCurrX == mFinalX && mCurrY == mFinalY) {
                        mFinished = true;
                        break;
                    }
            }
        } else {
            mCurrX = mFinalX;
            mCurrY = mFinalY;
            mFinished = true;
        }
        return true;
    }

    public void startScroll(int startX, int startY, int dx, int dy) {
        startScroll(startX, startY, dx, dy, DEFAULT_DURATION);
    }

    public void startScroll(int startX, int startY, int dx, int dy, int duration) {
        mMode = SCROLL_MODE;
        mFinished = false;
        mDuration = duration;
        mStartTime = AnimationUtils.currentAnimationTimeMillis();
        mStartX = startX;
        mStartY = startY;
        mFinalX = startX + dx;
        mFinalY = startY + dy;
        mDeltaX = (float) dx;
        mDeltaY = (float) dy;
        mDurationReciprocal = END_TENSION / ((float) mDuration);
    }

    public void fling(int startX, int startY, int velocityX, int velocityY, int minX, int maxX, int minY, int maxY) {
        if (mFlywheel && !mFinished) {
            float oldVel = getCurrVelocity();
            float dx = (float) (mFinalX - mStartX);
            float dy = (float) (mFinalY - mStartY);
            float hyp = (float) Math.hypot((double) dx, (double) dy);
            float oldVelocityX = (dx / hyp) * oldVel;
            float oldVelocityY = (dy / hyp) * oldVel;
            if (Math.signum((float) velocityX) == Math.signum(oldVelocityX) && Math.signum((float) velocityY) == Math.signum(oldVelocityY)) {
                velocityX = (int) (((float) velocityX) + oldVelocityX);
                velocityY = (int) (((float) velocityY) + oldVelocityY);
            }
        }
        mMode = 1;
        mFinished = false;
        float velocity = (float) Math.hypot((double) velocityX, (double) velocityY);
        mVelocity = velocity;
        mDuration = getSplineFlingDuration(velocity);
        mStartTime = AnimationUtils.currentAnimationTimeMillis();
        mStartX = startX;
        mStartY = startY;
        float coeffX = velocity == 0.0f ? END_TENSION : ((float) velocityX) / velocity;
        float coeffY = velocity == 0.0f ? END_TENSION : ((float) velocityY) / velocity;
        double totalDistance = getSplineFlingDistance(velocity);
        mDistance = (int) (((double) Math.signum(velocity)) * totalDistance);
        mMinX = minX;
        mMaxX = maxX;
        mMinY = minY;
        mMaxY = maxY;
        mFinalX = ((int) Math.round(((double) coeffX) * totalDistance)) + startX;
        mFinalX = Math.min(mFinalX, mMaxX);
        mFinalX = Math.max(mFinalX, mMinX);
        mFinalY = ((int) Math.round(((double) coeffY) * totalDistance)) + startY;
        mFinalY = Math.min(mFinalY, mMaxY);
        mFinalY = Math.max(mFinalY, mMinY);
    }

    private double getSplineDeceleration(float velocity) {
        return Math.log((double) ((INFLEXION * Math.abs(velocity)) / (mFlingFriction * mPhysicalCoeff)));
    }

    private int getSplineFlingDuration(float velocity) {
        return (int) (1000.0d * Math.exp(getSplineDeceleration(velocity) / (((double) DECELERATION_RATE) - 1.0d)));
    }

    private double getSplineFlingDistance(float velocity) {
        return ((double) (mFlingFriction * mPhysicalCoeff)) * Math.exp((((double) DECELERATION_RATE) / (((double) DECELERATION_RATE) - 1.0d)) * getSplineDeceleration(velocity));
    }

    public void abortAnimation() {
        mCurrX = mFinalX;
        mCurrY = mFinalY;
        mFinished = true;
    }

    public void extendDuration(int extend) {
        mDuration = timePassed() + extend;
        mDurationReciprocal = END_TENSION / ((float) mDuration);
        mFinished = false;
    }

    public int timePassed() {
        return (int) (AnimationUtils.currentAnimationTimeMillis() - mStartTime);
    }

    public boolean isScrollingInDirection(float xvel, float yvel) {
        return !mFinished && Math.signum(xvel) == Math.signum((float) (mFinalX - mStartX)) && Math.signum(yvel) == Math.signum((float) (mFinalY - mStartY));
    }


    static class ViscousFluidInterpolator implements Interpolator {
        public static final float VISCOUS_FLUID_SCALE = 8.0f;
        public static final float VISCOUS_FLUID_NORMALIZE = 1.0f / viscousFluid(1.0f);
        public static final float VISCOUS_FLUID_OFFSET = 1.0f - VISCOUS_FLUID_NORMALIZE * viscousFluid(1.0f);

        public static float viscousFluid(float x) {
            x *= VISCOUS_FLUID_SCALE;
            if (x < 1.0f) {
                x -= (1.0f - (float) Math.exp(-x));
            } else {
                float start = 0.36787945f;
                x = 1.0f - (float) Math.exp(1.0f - x);
                x = start + x * (1.0f - start);
            }
            return x;
        }

        @Override
        public float getInterpolation(float input) {
            final float interpolated = VISCOUS_FLUID_NORMALIZE * viscousFluid(input);
            if (interpolated > 0) {
                return interpolated + VISCOUS_FLUID_OFFSET;
            }
            return interpolated;
        }
    }
}