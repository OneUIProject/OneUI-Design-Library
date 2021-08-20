package de.dlyt.yanndroid.oneui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.EdgeEffect;

import androidx.reflect.view.SeslHapticFeedbackConstantsReflector;

public class SamsungEdgeEffect extends EdgeEffect {
    private static final double ANGLE = 0.5235987755982988d;
    private static final int[] ATTRS = {android.R.attr.colorEdgeEffect};
    private static final float COS = ((float) Math.cos(ANGLE));
    private static final float EDGE_CONTROL_POINT_HEIGHT_NON_TAB_IN_DIP = 29.0f;
    private static final float EDGE_CONTROL_POINT_HEIGHT_TAB_IN_DIP = 19.0f;
    private static final float EDGE_PADDING_NON_TAB_IN_DIP = 5.0f;
    private static final float EDGE_PADDING_TAB_IN_DIP = 3.0f;
    private static final float MAX_GLOW_SCALE = 2.0f;
    private static final int MAX_VELOCITY = 10000;
    private static final int MIN_VELOCITY = 100;
    private static final int MSG_CALL_ONRELEASE = 1;
    private static final float PULL_GLOW_BEGIN = 0.0f;
    private static final int SESL_STATE_APPEAR = 5;
    private static final int SESL_STATE_KEEP = 6;
    private static final float SIN = ((float) Math.sin(ANGLE));
    private static final int STATE_ABSORB = 2;
    private static final int STATE_IDLE = 0;
    private static final int STATE_PULL = 1;
    private static final int STATE_PULL_DECAY = 4;
    private static final int STATE_RECEDE = 3;
    private static final float TAB_HEIGHT_BUFFER_IN_DIP = 5.0f;
    private float SESL_MAX_ALPHA = 0.15f;
    private float SESL_MAX_SCALE = 1.0f;
    private final Rect mBounds = new Rect();
    private float mDisplacement = 0.5f;
    private final DisplayMetrics mDisplayMetrics;
    private float mDuration;
    private float mEdgeControlPointHeight;
    private float mEdgePadding;

    private Runnable mForceCallOnRelease = new Runnable() {
        public void run() {
            SamsungEdgeEffect.this.mOnReleaseCalled = true;
            SamsungEdgeEffect.this.onPull(SamsungEdgeEffect.this.mTempDeltaDistance, SamsungEdgeEffect.this.mTempDisplacement);
            SamsungEdgeEffect.this.mHandler.sendEmptyMessageDelayed(MSG_CALL_ONRELEASE, 700);
        }
    };

    private float mGlowAlpha;
    private float mGlowAlphaFinish;
    private float mGlowAlphaStart;
    private float mGlowScaleY;
    private float mGlowScaleYFinish;
    private float mGlowScaleYStart;

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    SamsungEdgeEffect.this.onRelease();
                    return;
                default:
                    return;
            }
        }
    };

    private final Interpolator mInterpolator;
    private boolean mOnReleaseCalled = false;
    private final Paint mPaint = new Paint();
    private final Path mPath = new Path();
    private float mPullDistance;
    private View mSeslHostView;
    private long mStartTime;
    private int mState = STATE_IDLE;
    private final float mTabHeight;
    private final float mTabHeightBuffer;
    private float mTargetDisplacement = 0.5f;
    private float mTempDeltaDistance;
    private float mTempDisplacement;

    public SamsungEdgeEffect(Context context) {
        super(context);
        mPaint.setAntiAlias(true);
        final TypedArray a = context.getTheme().obtainStyledAttributes(ATTRS);
        final int themeColor = a.getColor(0, -10066330);
        a.recycle();
        mPaint.setColor((0xFFFFFF & themeColor) | 0x33000000);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
        mInterpolator = new DecelerateInterpolator();
        mDisplayMetrics = context.getResources().getDisplayMetrics();
        mTabHeight = dipToPixels(85.0f);
        mTabHeightBuffer = dipToPixels(TAB_HEIGHT_BUFFER_IN_DIP);
    }

    @Override
    public void finish() {
        mState = STATE_IDLE;
    }

    @Override
    public int getColor() {
        return mPaint.getColor();
    }

    @Override
    public int getMaxHeight() {
        return (int) ((((float) mBounds.height()) * MAX_GLOW_SCALE) + 0.5f);
    }

    @Override
    public boolean isFinished() {
        return mState == STATE_IDLE;
    }

    @Override
    public void onAbsorb(int velocity) {
        if (!isEdgeEffectRunning()) {
            if (mSeslHostView != null) {
                mSeslHostView.performHapticFeedback(SeslHapticFeedbackConstantsReflector.semGetVibrationIndex(28));


            }
            mOnReleaseCalled = true;
            mState = STATE_ABSORB;
            int velocity2 = Math.min(Math.max(MIN_VELOCITY, Math.abs(velocity)), MAX_VELOCITY);
            mStartTime = AnimationUtils.currentAnimationTimeMillis();
            mDuration = 250.0f;
            mGlowAlphaStart = 0.0f;
            mGlowScaleYStart = PULL_GLOW_BEGIN;
            mGlowScaleYFinish = SESL_MAX_SCALE;
            mGlowAlphaFinish = SESL_MAX_ALPHA;
            mTargetDisplacement = 0.5f;
            mHandler.sendEmptyMessageDelayed(MSG_CALL_ONRELEASE, 700);
        }
    }

    @Override
    public void onPull(float deltaDistance) {
        onPull(deltaDistance, 0.5f);
    }

    @Override
    public void onPull(float deltaDistance, float displacement) {
        if (mPullDistance == 0.0f) {
            mOnReleaseCalled = false;
            if (isEdgeEffectRunning()) {
                mPullDistance += deltaDistance;
            }
        }
        long now = AnimationUtils.currentAnimationTimeMillis();
        mTargetDisplacement = displacement;
        if (mState != STATE_PULL_DECAY || ((float) (now - mStartTime)) >= mDuration) {
            if (mState != STATE_PULL) {
                mGlowScaleY = Math.max(PULL_GLOW_BEGIN, mGlowScaleY);
            }
            if (isEdgeEffectRunning()) {
                return;
            }
            if (mPullDistance == 0.0f || mOnReleaseCalled) {
                if (mSeslHostView != null) {
                    int indexOfHaptic = SeslHapticFeedbackConstantsReflector.semGetVibrationIndex(28);
                    if (indexOfHaptic != -1) {
                        mSeslHostView.performHapticFeedback(indexOfHaptic);
                    }
                }
                mState = STATE_PULL;
                mStartTime = now;
                mDuration = 167.0f;
                mPullDistance += deltaDistance;
            }
        }
    }

    @Override
    public void onRelease() {
        mPullDistance = 0.0f;
        mOnReleaseCalled = true;
        if (mState == STATE_PULL || mState == STATE_PULL_DECAY) {
            mState = STATE_RECEDE;
            mGlowAlphaStart = mGlowAlpha;
            mGlowScaleYStart = mGlowScaleY;
            mGlowAlphaFinish = 0.0f;
            mGlowScaleYFinish = PULL_GLOW_BEGIN;
            mStartTime = AnimationUtils.currentAnimationTimeMillis();
            mDuration = 450.0f;
        }
    }

    @Override
    public void setColor(int color) {
        mPaint.setColor(color);
    }

    @Override
    public void setSize(int width, int height) {
        float r = (((float) width) * 0.75f) / SIN;
        float h = r - (COS * r);
        float or = (((float) height) * 0.75f) / SIN;
        float f = or - (COS * or);
        if (((float) width) <= mTabHeight + mTabHeightBuffer) {
            mEdgePadding = dipToPixels(EDGE_PADDING_TAB_IN_DIP);
            mEdgeControlPointHeight = dipToPixels(EDGE_CONTROL_POINT_HEIGHT_TAB_IN_DIP);
        } else {
            mEdgePadding = dipToPixels(EDGE_PADDING_NON_TAB_IN_DIP);
            mEdgeControlPointHeight = dipToPixels(EDGE_CONTROL_POINT_HEIGHT_NON_TAB_IN_DIP);
        }
        mBounds.set(mBounds.left, mBounds.top, width, (int) Math.min((float) height, h));
    }

    private float dipToPixels(float dipValue) {
        return TypedValue.applyDimension(1, dipValue, mDisplayMetrics);
    }

    public boolean draw(Canvas canvas) {
        update();
        int count = canvas.save();
        float centerX = (float) mBounds.centerX();
        canvas.scale(1.0f, Math.min(mGlowScaleY, 1.0f), centerX, PULL_GLOW_BEGIN);
        float max = Math.max(PULL_GLOW_BEGIN, Math.min(mDisplacement, 1.0f)) - 0.5f;
        float controlX = centerX;
        float controlY = mEdgeControlPointHeight + mEdgePadding;
        float topDistance = ((float) mBounds.width()) * 0.2f;
        mPath.reset();
        mPath.moveTo(0.0f, 0.0f);
        mPath.lineTo(0.0f, mEdgePadding);
        mPath.cubicTo(controlX - topDistance, controlY, controlX + topDistance, controlY, (float) mBounds.width(), mEdgePadding);
        mPath.lineTo((float) mBounds.width(), 0.0f);
        mPath.close();
        mPaint.setAlpha((int) (255.0f * mGlowAlpha));
        canvas.drawPath(mPath, mPaint);
        canvas.restoreToCount(count);
        boolean oneLastFrame = false;
        if (mState == STATE_RECEDE && mGlowScaleY == PULL_GLOW_BEGIN) {
            mState = STATE_IDLE;
            oneLastFrame = true;
        }
        return mState != STATE_IDLE || oneLastFrame;
    }

    private boolean isEdgeEffectRunning() {
        return mState == SESL_STATE_APPEAR || mState == SESL_STATE_KEEP || mState == STATE_RECEDE || mState == STATE_ABSORB;
    }

    public void onPullCallOnRelease(float deltaDistance, float displacement, int delayTime) {
        mTempDeltaDistance = deltaDistance;
        mTempDisplacement = displacement;
        mHandler.postDelayed(mForceCallOnRelease, (long) delayTime);
    }

    public void setSeslHostView(View hostView) {
        mSeslHostView = hostView;
    }

    private void update() {
        float t = Math.min(((float) (AnimationUtils.currentAnimationTimeMillis() - mStartTime)) / mDuration, 1.0f);
        float interp = mInterpolator.getInterpolation(t);
        mGlowAlpha = mGlowAlphaStart + ((mGlowAlphaFinish - mGlowAlphaStart) * interp);
        mGlowScaleY = mGlowScaleYStart + ((mGlowScaleYFinish - mGlowScaleYStart) * interp);
        mDisplacement = (mDisplacement + mTargetDisplacement) / MAX_GLOW_SCALE;
        if (t >= 0.999f || mState == STATE_PULL) {
            switch (mState) {
                case STATE_PULL:
                    mState = SESL_STATE_APPEAR;
                    mStartTime = AnimationUtils.currentAnimationTimeMillis();
                    mDuration = 250.0f;
                    mGlowAlphaStart = 0.0f;
                    mGlowScaleYStart = PULL_GLOW_BEGIN;
                    mGlowAlphaFinish = SESL_MAX_ALPHA;
                    mGlowScaleYFinish = SESL_MAX_SCALE;
                    mGlowScaleY = PULL_GLOW_BEGIN;
                    mOnReleaseCalled = false;
                    return;
                case STATE_ABSORB:
                    mState = SESL_STATE_KEEP;
                    mStartTime = AnimationUtils.currentAnimationTimeMillis();
                    mDuration = 0.0f;
                    float f = SESL_MAX_ALPHA;
                    mGlowAlphaStart = f;
                    mGlowAlphaFinish = f;
                    float f2 = SESL_MAX_SCALE;
                    mGlowScaleYStart = f2;
                    mGlowScaleYFinish = f2;
                    return;
                case STATE_RECEDE:
                    mState = STATE_IDLE;
                    return;
                case STATE_PULL_DECAY:
                    mState = STATE_RECEDE;
                    return;
                case SESL_STATE_APPEAR:
                    mState = SESL_STATE_KEEP;
                    mStartTime = AnimationUtils.currentAnimationTimeMillis();
                    mDuration = 0.0f;
                    float f3 = SESL_MAX_ALPHA;
                    mGlowAlphaStart = f3;
                    mGlowAlphaFinish = f3;
                    float f4 = SESL_MAX_SCALE;
                    mGlowScaleYStart = f4;
                    mGlowScaleYFinish = f4;
                    return;
                case SESL_STATE_KEEP:
                    mState = STATE_RECEDE;
                    mStartTime = AnimationUtils.currentAnimationTimeMillis();
                    mDuration = 450.0f;
                    mGlowAlphaStart = mGlowAlpha;
                    mGlowScaleYStart = mGlowScaleY;
                    mGlowAlphaFinish = 0.0f;
                    mGlowScaleYFinish = PULL_GLOW_BEGIN;
                    return;
                default:
                    return;
            }
        }
    }
}
