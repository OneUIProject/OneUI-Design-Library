package de.dlyt.yanndroid.oneui.recyclerview;

import android.content.Context;
import android.util.Log;
import android.view.ViewConfiguration;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;

import java.lang.reflect.Array;

import de.dlyt.yanndroid.oneui.utils.ReflectUtils;

public class SeslOverScroller {
    private static final String TAG = "SeslOverScroller";
    private static final int DEFAULT_DURATION = 250;
    private static final int FLING_MODE = 1;
    private static final int SCROLL_MODE = 0;
    private final boolean mFlywheel;
    private final SplineOverScroller mScrollerX;
    private final SplineOverScroller mScrollerY;
    private Interpolator mInterpolator;
    private int mMode;

    public SeslOverScroller(Context context) {
        this(context, null);
    }

    public SeslOverScroller(Context context, Interpolator interpolator) {
        this(context, interpolator, true);
    }

    public SeslOverScroller(Context context, Interpolator interpolator, boolean flywheel) {
        if (interpolator == null) {
            interpolator = new SeslScroller.ViscousFluidInterpolator();
        }
        mInterpolator = interpolator;
        mFlywheel = flywheel;
        mScrollerX = new SplineOverScroller(context);
        mScrollerY = new SplineOverScroller(context);

        // FIX
        boolean isSmoothScrollEnabled = true;
        try {
            isSmoothScrollEnabled = (boolean) ReflectUtils.genericInvokeMethod("com.samsung.android.os.SemPerfManager", "onSmoothScrollEvent", false);
        } catch (NullPointerException e) {
            Log.e(TAG, "isSmoothScrollEnabled: genericInvokeMethod returned null!!!");
            isSmoothScrollEnabled = false;
        }

        if (!isSmoothScrollEnabled) {
            setSmoothScrollEnabled(false);
            Log.e(TAG, "does NOT support Smoothscroll booster thus Smoothscroll's disabled");
        }
    }

    public SeslOverScroller(Context context, Interpolator interpolator, float bounceCoefficientX, float bounceCoefficientY) {
        this(context, interpolator, true);
    }

    public SeslOverScroller(Context context, Interpolator interpolator, float bounceCoefficientX, float bounceCoefficientY, boolean flywheel) {
        this(context, interpolator, flywheel);
    }

    void setInterpolator(Interpolator interpolator) {
        if (interpolator == null) {
            interpolator = new SeslScroller.ViscousFluidInterpolator();
        }
        mInterpolator = interpolator;
    }

    public final void setFriction(float friction) {
        mScrollerX.setFriction(friction);
        mScrollerY.setFriction(friction);
    }

    public final boolean isFinished() {
        return mScrollerX.mFinished && mScrollerY.mFinished;
    }

    public final void forceFinished(boolean finished) {
        mScrollerX.mFinished = mScrollerY.mFinished = finished;
    }

    public final int getCurrX() {
        return mScrollerX.mCurrentPosition;
    }

    public final int getCurrY() {
        return mScrollerY.mCurrentPosition;
    }

    public float getCurrVelocity() {
        return (float) Math.hypot((double) mScrollerX.mCurrVelocity, (double) mScrollerY.mCurrVelocity);
    }

    public final int getStartX() {
        return mScrollerX.mStart;
    }

    public final int getStartY() {
        return mScrollerY.mStart;
    }

    public final int getFinalX() {
        return mScrollerX.mFinal;
    }

    @Deprecated
    public void setFinalX(int newX) {
        mScrollerX.setFinalPosition(newX);
    }

    public final int getFinalY() {
        return mScrollerY.mFinal;
    }

    @Deprecated
    public void setFinalY(int newY) {
        mScrollerY.setFinalPosition(newY);
    }

    @Deprecated
    public final int getDuration() {
        return Math.max(mScrollerX.mDuration, mScrollerY.mDuration);
    }

    @Deprecated
    public void extendDuration(int extend) {
        mScrollerX.extendDuration(extend);
        mScrollerY.extendDuration(extend);
    }

    public boolean computeScrollOffset() {
        if (isFinished()) {
            return false;
        }
        switch (mMode) {
            case SCROLL_MODE:
                long elapsedTime = AnimationUtils.currentAnimationTimeMillis() - mScrollerX.mStartTime;
                int duration = mScrollerX.mDuration;
                if (elapsedTime >= ((long) duration)) {
                    abortAnimation();
                    break;
                } else {
                    float q = mInterpolator.getInterpolation(((float) elapsedTime) / ((float) duration));
                    mScrollerX.updateScroll(q);
                    mScrollerY.updateScroll(q);
                    break;
                }
            case FLING_MODE:
                if (!mScrollerX.mFinished && !mScrollerX.update() && !mScrollerX.continueWhenFinished()) {
                    mScrollerX.finish();
                }
                if (!mScrollerY.mFinished && !mScrollerY.update() && !mScrollerY.continueWhenFinished()) {
                    mScrollerY.finish();
                    break;
                }
        }
        return true;
    }

    public void startScroll(int startX, int startY, int dx, int dy) {
        startScroll(startX, startY, dx, dy, DEFAULT_DURATION);
    }

    public void startScroll(int startX, int startY, int dx, int dy, int duration) {
        mMode = SCROLL_MODE;
        mScrollerX.startScroll(startX, dx, duration);
        mScrollerY.startScroll(startY, dy, duration);
    }

    public boolean springBack(int startX, int startY, int minX, int maxX, int minY, int maxY) {
        mMode = FLING_MODE;
        boolean spingbackX = mScrollerX.springback(startX, minX, maxX);
        boolean spingbackY = mScrollerY.springback(startY, minY, maxY);
        if (spingbackX || spingbackY) {
            return true;
        }
        return false;
    }

    public void fling(int startX, int startY, int velocityX, int velocityY, int minX, int maxX, int minY, int maxY) {
        fling(startX, startY, velocityX, velocityY, minX, maxX, minY, maxY, 0, 0);
    }

    protected void fling(int startX, int startY, int velocityX, int velocityY, int minX, int maxX, int minY, int maxY, boolean accDisabled) {
        fling(startX, startY, velocityX, velocityY, minX, maxX, minY, maxY, 0, 0, accDisabled);
    }

    public void fling(int startX, int startY, int velocityX, int velocityY, int minX, int maxX, int minY, int maxY, int overX, int overY) {
        if (mFlywheel && !isFinished()) {
            float oldVelocityX = mScrollerX.mCurrVelocity;
            float oldVelocityY = mScrollerY.mCurrVelocity;
            if (Math.signum((float) velocityX) == Math.signum(oldVelocityX) && Math.signum((float) velocityY) == Math.signum(oldVelocityY)) {
                velocityX = (int) (((float) velocityX) + oldVelocityX);
                velocityY = (int) (((float) velocityY) + oldVelocityY);
            }
        }
        mMode = FLING_MODE;
        mScrollerX.fling(startX, velocityX, minX, maxX, overX);
        mScrollerY.fling(startY, velocityY, minY, maxY, overY);
    }

    public void fling(int startX, int startY, int velocityX, int velocityY, int minX, int maxX, int minY, int maxY, int overX, int overY, boolean accDisabled) {
        if (mFlywheel && !isFinished() && !accDisabled) {
            float oldVelocityX = mScrollerX.mCurrVelocity;
            float oldVelocityY = mScrollerY.mCurrVelocity;
            if (Math.signum((float) velocityX) == Math.signum(oldVelocityX) && Math.signum((float) velocityY) == Math.signum(oldVelocityY)) {
                velocityX = (int) (((float) velocityX) + oldVelocityX);
                velocityY = (int) (((float) velocityY) + oldVelocityY);
            }
        }
        mMode = FLING_MODE;
        mScrollerX.fling(startX, velocityX, minX, maxX, overX);
        mScrollerY.fling(startY, velocityY, minY, maxY, overY);
    }

    public void notifyHorizontalEdgeReached(int startX, int finalX, int overX) {
        mScrollerX.notifyEdgeReached(startX, finalX, overX);
    }

    public void notifyVerticalEdgeReached(int startY, int finalY, int overY) {
        mScrollerY.notifyEdgeReached(startY, finalY, overY);
    }

    public boolean isOverScrolled() {
        return (!mScrollerX.mFinished && mScrollerX.mState != 0) || (!mScrollerY.mFinished && mScrollerY.mState != 0);
    }

    public void abortAnimation() {
        mScrollerX.finish();
        mScrollerY.finish();
    }

    public int timePassed() {
        return (int) (AnimationUtils.currentAnimationTimeMillis() - Math.min(mScrollerX.mStartTime, mScrollerY.mStartTime));
    }

    public boolean isScrollingInDirection(float xvel, float yvel) {
        return !isFinished() && Math.signum(xvel) == Math.signum((float) (mScrollerX.mFinal - mScrollerX.mStart)) && Math.signum(yvel) == Math.signum((float) (mScrollerY.mFinal - mScrollerY.mStart));
    }

    public void setSmoothScrollEnabled(boolean enabled) {
        int mode = enabled ? FLING_MODE : SCROLL_MODE;
        mScrollerX.setMode(mode);
        mScrollerY.setMode(mode);
    }

    public void setRegulationEnabled(boolean enabled) {
        mScrollerX.setRegulationEnabled(enabled);
        mScrollerY.setRegulationEnabled(enabled);
    }


    static class SplineOverScroller {
        private static final float DISTANCE_M2 = 1.5f;
        private static final float DURATION_M2 = 1.8f;
        private static final float END_TENSION = 1.0f;
        private static final float GRAVITY = 2000.0f;
        private static final float[] INFLEXIONS = {0.35f, 0.22f};
        private static final float[][] SPLINE_POSITIONS = ((float[][]) Array.newInstance(Float.TYPE, new int[]{2, 101}));
        private static final float[][] SPLINE_TIMES = ((float[][]) Array.newInstance(Float.TYPE, new int[]{2, 101}));
        private static final float START_TENSION = 0.5f;
        private static float DECELERATION_RATE = ((float) (Math.log(0.78d) / Math.log(0.9d)));
        private static float INFLEXION = INFLEXIONS[1];
        private static float[] SPLINE_POSITION = SPLINE_POSITIONS[1];
        private static float[] SPLINE_TIME = SPLINE_TIMES[1];
        private static boolean sEnableSmoothFling = true;
        private static boolean sRegulateCurrentTimeInterval = true;

        static {
            float x, y, coef;
            for (int mode = 0; mode < 2; mode++) {
                float P1 = START_TENSION * INFLEXIONS[mode];
                float P2 = END_TENSION - (END_TENSION * (END_TENSION - INFLEXIONS[mode]));
                float x_min = 0.0f, y_min = 0.0f;
                for (int i = 0; i < 100; i++) {
                    float alpha = ((float) i) / 100.0f;
                    float x_max = END_TENSION;
                    while (true) {
                        x = x_min + ((x_max - x_min) / 2.0f);
                        coef = 3.0f * x * (END_TENSION - x);
                        float tx = ((((END_TENSION - x) * P1) + (x * P2)) * coef) + (x * x * x);
                        if (((double) Math.abs(tx - alpha)) < 1.0E-5d) {
                            break;
                        } else if (tx > alpha) {
                            x_max = x;
                        } else {
                            x_min = x;
                        }
                    }
                    SPLINE_POSITIONS[mode][i] = ((((END_TENSION - x) * START_TENSION) + x) * coef) + (x * x * x);
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
                    SPLINE_TIMES[mode][i] = ((((END_TENSION - y) * P1) + (y * P2)) * coef) + (y * y * y);
                }
                float[] fArr = SPLINE_POSITIONS[mode];
                SPLINE_TIMES[mode][100] = 1.0f;
                fArr[100] = 1.0f;
            }
        }

        private float mCurrVelocity;
        private int mCurrentPosition;
        private float mDeceleration;
        private int mDuration;
        private int mFinal;
        private boolean mFinished = true;
        private float mFlingFriction = ViewConfiguration.getScrollFriction();
        private boolean mIsDVFSBoosting = false;
        private int mMaximumVelocity;
        private int mOver;
        private float mPhysicalCoeff;
        private long mPrevTime = 0;
        private long mPrevTimeGap = 0;
        private int mSplineDistance;
        private int mSplineDuration;
        private int mStart;
        private long mStartTime;
        private int mState = 0;
        private int mUpdateCount = 0;
        private int mVelocity;

        SplineOverScroller(Context context) {
            mPhysicalCoeff = 386.0878f * context.getResources().getDisplayMetrics().density * 160.0f * 0.84f;
            if (sEnableSmoothFling) {
                mMaximumVelocity = ViewConfiguration.get(context).getScaledMaximumFlingVelocity();
            }
        }

        private static float getDeceleration(int velocity) {
            if (velocity > 0) {
                return -2000.0f;
            }
            return GRAVITY;
        }

        public void setMode(int mode) {
            if (mode >= 0 && mode <= 1) {
                if (mode == 0) {
                    sEnableSmoothFling = false;
                    sRegulateCurrentTimeInterval = false;
                } else {
                    sEnableSmoothFling = true;
                    sRegulateCurrentTimeInterval = true;
                }
                INFLEXION = INFLEXIONS[mode];
                SPLINE_POSITION = SPLINE_POSITIONS[mode];
                SPLINE_TIME = SPLINE_TIMES[mode];
            }
        }

        public void setRegulationEnabled(boolean enabled) {
            sRegulateCurrentTimeInterval = sEnableSmoothFling && enabled;
        }

        void setFriction(float friction) {
            mFlingFriction = friction;
        }

        void updateScroll(float q) {
            mCurrentPosition = mStart + Math.round(((float) (mFinal - mStart)) * q);
        }

        private void adjustDuration(int start, int oldFinal, int newFinal) {
            float x = Math.abs(((float) (newFinal - start)) / ((float) (oldFinal - start)));
            int index = (int) (100.0f * x);
            if (index < 100) {
                float x_inf = ((float) index) / 100.0f;
                float x_sup = ((float) (index + 1)) / 100.0f;
                float t_inf = SPLINE_TIME[index];
                mDuration = (int) (((float) mDuration) * (t_inf + (((x - x_inf) / (x_sup - x_inf)) * (SPLINE_TIME[index + 1] - t_inf))));
            }
        }

        void startScroll(int start, int distance, int duration) {
            mFinished = false;
            mStart = start;
            mCurrentPosition = start;
            mFinal = start + distance;
            mStartTime = AnimationUtils.currentAnimationTimeMillis();
            mDuration = duration;
            mDeceleration = 0.0f;
            mVelocity = 0;
        }

        void finish() {
            if (mIsDVFSBoosting) {
                ReflectUtils.genericInvokeMethod("com.samsung.android.os.SemPerfManager", "onSmoothScrollEvent", false);
                mIsDVFSBoosting = false;
            }
            mCurrentPosition = mFinal;
            mFinished = true;
        }

        void setFinalPosition(int position) {
            mFinal = position;
            mFinished = false;
        }

        void extendDuration(int extend) {
            mDuration = ((int) (AnimationUtils.currentAnimationTimeMillis() - mStartTime)) + extend;
            mFinished = false;
        }

        boolean springback(int start, int min, int max) {
            mFinished = true;
            mFinal = start;
            mStart = start;
            mCurrentPosition = start;
            mVelocity = 0;
            mStartTime = AnimationUtils.currentAnimationTimeMillis();
            mDuration = 0;
            if (start < min) {
                startSpringback(start, min, 0);
            } else if (start > max) {
                startSpringback(start, max, 0);
            }
            if (!mFinished) {
                return true;
            }
            return false;
        }

        private void startSpringback(int start, int end, int velocity) {
            mFinished = false;
            mState = 1;
            mStart = start;
            mCurrentPosition = start;
            mFinal = end;
            int delta = start - end;
            mDeceleration = getDeceleration(delta);
            mVelocity = -delta;
            mOver = Math.abs(delta);
            mDuration = (int) (1000.0d * Math.sqrt((-2.0d * ((double) delta)) / ((double) mDeceleration)));
        }

        void fling(int start, int velocity, int min, int max, int over) {
            mOver = over;
            mFinished = false;
            mVelocity = velocity;
            mCurrVelocity = (float) velocity;
            mSplineDuration = 0;
            mDuration = 0;
            mStartTime = AnimationUtils.currentAnimationTimeMillis();
            mStart = start;
            mCurrentPosition = start;
            if (start > max || start < min) {
                startAfterEdge(start, min, max, velocity);
                return;
            }
            mState = 0;
            double totalDistance = 0.0d;
            if (velocity != 0) {
                int splineFlingDuration = getSplineFlingDuration(velocity);
                mSplineDuration = splineFlingDuration;
                mDuration = splineFlingDuration;
                totalDistance = getSplineFlingDistance(velocity);
                if (sEnableSmoothFling && !mIsDVFSBoosting && (velocity >= 800 || velocity <= -800)) {
                    ReflectUtils.genericInvokeMethod("com.samsung.android.os.SemPerfManager", "onSmoothScrollEvent", true);
                    mIsDVFSBoosting = true;
                }
            }
            mSplineDistance = (int) (((double) Math.signum((float) velocity)) * totalDistance);
            mFinal = mSplineDistance + start;
            if (mFinal < min) {
                adjustDuration(mStart, mFinal, min);
                mFinal = min;
            }
            if (mFinal > max) {
                adjustDuration(mStart, mFinal, max);
                mFinal = max;
            }
            if (sRegulateCurrentTimeInterval) {
                mUpdateCount = 0;
            }
        }

        private double getSplineDeceleration(int velocity) {
            return Math.log((double) ((INFLEXION * ((float) Math.abs(velocity))) / (mFlingFriction * mPhysicalCoeff)));
        }

        private double getSplineFlingDistance(int velocity) {
            double l = getSplineDeceleration(velocity);
            double decelMinusOne = ((double) DECELERATION_RATE) - 1.0d;
            if (!sEnableSmoothFling) {
                return ((double) (mFlingFriction * mPhysicalCoeff)) * Math.exp((((double) DECELERATION_RATE) / decelMinusOne) * l);
            }
            int index = (int) (100.0f * (((float) Math.abs(velocity)) / ((float) mMaximumVelocity)));
            if (index > 100) {
                index = 100;
            }
            return ((double) mFlingFriction) * ((double) ((3.0f * (END_TENSION - SPLINE_POSITION[index])) + DISTANCE_M2)) * ((double) mPhysicalCoeff) * Math.exp((((double) DECELERATION_RATE) / decelMinusOne) * l);
        }

        private int getSplineFlingDuration(int velocity) {
            double l = getSplineDeceleration(velocity);
            double decelMinusOne = ((double) DECELERATION_RATE) - 1.0d;
            if (!sEnableSmoothFling) {
                return (int) (1000.0d * Math.exp(l / decelMinusOne));
            }
            int index = (int) (100.0f * (((float) Math.abs(velocity)) / ((float) mMaximumVelocity)));
            if (index > 100) {
                index = 100;
            }
            return (int) (1000.0d * ((double) ((3.0f * (END_TENSION - SPLINE_POSITION[index])) + DURATION_M2)) * Math.exp(l / decelMinusOne));
        }

        private void fitOnBounceCurve(int start, int end, int velocity) {
            float totalDuration = (float) Math.sqrt((2.0d * ((double) ((((((float) velocity) * ((float) velocity)) / 2.0f) / Math.abs(mDeceleration)) + ((float) Math.abs(end - start))))) / ((double) Math.abs(mDeceleration)));
            mStartTime -= (long) ((int) (1000.0f * (totalDuration - (((float) (-velocity)) / mDeceleration))));
            mStart = end;
            mCurrentPosition = end;
            mVelocity = (int) ((-mDeceleration) * totalDuration);
        }

        private void startBounceAfterEdge(int start, int end, int velocity) {
            int i;
            if (velocity == 0) {
                i = start - end;
            } else {
                i = velocity;
            }
            mDeceleration = getDeceleration(i);
            fitOnBounceCurve(start, end, velocity);
            onEdgeReached();
        }

        private void startAfterEdge(int start, int min, int max, int velocity) {
            int edge;
            if (start <= min || start >= max) {
                boolean positive = start > max;
                if (positive) {
                    edge = max;
                } else {
                    edge = min;
                }
                int overDistance = start - edge;
                if (overDistance * velocity >= 0) {
                    startBounceAfterEdge(start, edge, velocity);
                } else if (getSplineFlingDistance(velocity) > ((double) Math.abs(overDistance))) {
                    fling(start, velocity, positive ? min : start, positive ? start : max, mOver);
                } else {
                    startSpringback(start, edge, velocity);
                }
            } else {
                Log.e(TAG, "startAfterEdge called from a valid position");
                mFinished = true;
            }
        }

        void notifyEdgeReached(int start, int end, int over) {
            if (mState == 0) {
                mOver = over;
                mStartTime = AnimationUtils.currentAnimationTimeMillis();
                startAfterEdge(start, end, end, (int) mCurrVelocity);
            }
        }

        private void onEdgeReached() {
            float velocitySquared = ((float) mVelocity) * ((float) mVelocity);
            float distance = velocitySquared / (Math.abs(mDeceleration) * 2.0f);
            float sign = Math.signum((float) mVelocity);
            if (distance > ((float) mOver)) {
                mDeceleration = ((-sign) * velocitySquared) / (((float) mOver) * 2.0f);
                distance = (float) mOver;
            }
            mOver = (int) distance;
            mState = 2;
            int i = mStart;
            if (mVelocity <= 0) {
                distance = -distance;
            }
            mFinal = i + ((int) distance);
            mDuration = -((int) ((1000.0f * ((float) mVelocity)) / mDeceleration));
            if (sRegulateCurrentTimeInterval) {
                mUpdateCount = 0;
            }
        }

        boolean continueWhenFinished() {
            switch (mState) {
                case 0:
                    if (mDuration < mSplineDuration) {
                        int i = mFinal;
                        mStart = i;
                        mCurrentPosition = i;
                        mVelocity = (int) mCurrVelocity;
                        mDeceleration = getDeceleration(mVelocity);
                        mStartTime += (long) mDuration;
                        onEdgeReached();
                        break;
                    } else {
                        return false;
                    }
                case 1:
                    return false;
                case 2:
                    mStartTime += (long) mDuration;
                    startSpringback(mFinal, mStart, 0);
                    break;
            }
            update();
            return true;
        }

        boolean update() {
            long currentTime = AnimationUtils.currentAnimationTimeMillis() - mStartTime;
            long j = currentTime;
            if (sRegulateCurrentTimeInterval && mState == 0) {
                if (mUpdateCount > 0) {
                    currentTime = (mPrevTime + currentTime) / 2;
                }
                if (mUpdateCount > 30) {
                    long currentTimeGap = currentTime - mPrevTime;
                    if (currentTimeGap > mPrevTimeGap + 1) {
                        currentTime = mPrevTime + mPrevTimeGap + 1;
                    } else if (currentTimeGap < mPrevTimeGap - 1) {
                        currentTime = (mPrevTime + mPrevTimeGap) - 1;
                    }
                }
                if (currentTime < 0) {
                    currentTime = 0;
                }
                mPrevTimeGap = currentTime - mPrevTime;
                mPrevTime = currentTime;
                mUpdateCount++;
            }
            if (currentTime == 0) {
                if (mDuration > 0) {
                    return true;
                }
                return false;
            } else if (currentTime > ((long) mDuration)) {
                return false;
            } else {
                double distance = 0.0d;
                switch (mState) {
                    case 0: {
                        float t = ((float) currentTime) / ((float) mSplineDuration);
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
                        distance = (double) (((float) mSplineDistance) * distanceCoef);
                        mCurrVelocity = ((((float) mSplineDistance) * velocityCoef) / ((float) mSplineDuration)) * 1000.0f;
                        break;
                    }
                    case 1: {
                        float t = ((float) currentTime) / ((float) mDuration);
                        float t2 = t * t;
                        float sign = Math.signum((float) mVelocity);
                        distance = (double) (((float) mOver) * sign * ((3.0f * t2) - ((2.0f * t) * t2)));
                        mCurrVelocity = ((float) mOver) * sign * 6.0f * ((-t) + t2);
                        break;
                    }
                    case 2: {
                        float t = ((float) currentTime) / 1000.0f;
                        mCurrVelocity = ((float) mVelocity) + (mDeceleration * t);
                        distance = (double) ((((float) mVelocity) * t) + (((mDeceleration * t) * t) / 2.0f));
                        break;
                    }
                }
                mCurrentPosition = mStart + ((int) Math.round(distance));
                return true;
            }
        }
    }
}