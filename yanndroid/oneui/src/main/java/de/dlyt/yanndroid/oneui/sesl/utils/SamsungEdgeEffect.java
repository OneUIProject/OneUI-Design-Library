package de.dlyt.yanndroid.oneui.sesl.utils;

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

import androidx.annotation.ColorInt;
import androidx.reflect.view.SeslHapticFeedbackConstantsReflector;

import de.dlyt.yanndroid.oneui.R;

public class SamsungEdgeEffect extends EdgeEffect {
    private boolean mIsOneUI4;
    public static final double ANGLE = 0.5235987755982988d;
    public static final int APPEAR_TIME = 250;
    public static final int[] ATTRS = {android.R.attr.colorEdgeEffect};
    public static final float COS = ((float) Math.cos(0.5235987755982988d));
    public static final boolean DEBUG = false;
    public static final float EDGE_CONTROL_POINT_HEIGHT_WITHOUT_TAB_IN_DIP = 29.0f;
    public static final float EDGE_CONTROL_POINT_HEIGHT_WITH_TAB_IN_DIP = 19.0f;
    public static final float EDGE_MAX_ALPAH_DARK = 0.08f;
    public static final float EDGE_MAX_ALPAH_LIGHT = 0.05f;
    public static final float EDGE_PADDING_WITHOUT_TAB_IN_DIP = 5.0f;
    public static final float EDGE_PADDING_WITH_TAB_IN_DIP = 3.0f;
    public static final float EPSILON = 0.001f;
    public static final int KEEP_TIME = 0;
    public static final float MAX_GLOW_SCALE = 2.0f;
    public static final int MAX_VELOCITY = 10000;
    public static final int MIN_VELOCITY = 100;
    public static final int MSG_CALL_ONRELEASE = 1;
    public static final float PULL_GLOW_BEGIN = 0.0f;
    public static final int PULL_TIME = 167;
    public static final float RADIUS_FACTOR = 0.75f;
    public static final int RECEDE_TIME = 450;
    public static final float SIN = ((float) Math.sin(0.5235987755982988d));
    public static final int STATE_ABSORB = 2;
    public static final int STATE_APPEAR = 5;
    public static final int STATE_IDLE = 0;
    public static final int STATE_KEEP = 6;
    public static final int STATE_PULL = 1;
    public static final int STATE_PULL_DECAY = 4;
    public static final int STATE_RECEDE = 3;
    public static final float TAB_HEIGHT_BUFFER_IN_DIP = 5.0f;
    public static final float TAB_HEIGHT_IN_DIP = 85.0f;
    public static final String TAG = "SeslEdgeEffect";
    public static float sMaxAlpha;
    public float MAX_SCALE = 1.0f;
    public final Rect mBounds = new Rect();
    public boolean mCanVerticalScroll = true;
    public float mDisplacement = 0.5f;
    public final DisplayMetrics mDisplayMetrics;
    public float mDuration;
    public float mEdgeControlPointHeight;
    public float mEdgeEffectMargin = 0.0f;
    public float mEdgePadding;
    public Runnable mForceCallOnRelease = new Runnable() {
        public void run() {
            SamsungEdgeEffect.this.mOnReleaseCalled = true;
            SamsungEdgeEffect seslEdgeEffect = SamsungEdgeEffect.this;
            seslEdgeEffect.onPull(seslEdgeEffect.mTempDeltaDistance, SamsungEdgeEffect.this.mTempDisplacement);
            SamsungEdgeEffect.this.mHandler.sendEmptyMessageDelayed(1, 700);
        }
    };
    public float mGlowAlpha;
    public float mGlowAlphaFinish;
    public float mGlowAlphaStart;
    public float mGlowScaleY;
    public float mGlowScaleYFinish;
    public float mGlowScaleYStart;
    public Handler mHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message message) {
            if (message.what == 1) {
                SamsungEdgeEffect.this.onRelease();
            }
        }
    };
    public View mHostView;
    public final Interpolator mInterpolator;
    public boolean mOnReleaseCalled = false;
    public final Paint mPaint = new Paint();
    public final Path mPath = new Path();
    public float mPullDistance;
    public long mStartTime;
    public int mState = 0;
    public final float mTabHeight;
    public final float mTabHeightBuffer;
    public float mTargetDisplacement = 0.5f;
    public float mTempDeltaDistance;
    public float mTempDisplacement;

    public SamsungEdgeEffect(Context context) {
        super(context);

        mIsOneUI4 = context.getTheme().obtainStyledAttributes(new int[]{R.attr.isOneUI4}).getBoolean(0, false);

        this.mPaint.setAntiAlias(true);
        TypedArray obtainStyledAttributes = context.getTheme().obtainStyledAttributes(ATTRS);
        int color = obtainStyledAttributes.getColor(0, -10066330);
        obtainStyledAttributes.recycle();
        if (mIsOneUI4)
            this.mPaint.setColor(color);
        else
            mPaint.setColor((0xFFFFFF & color) | 0x33000000);
        this.mPaint.setStyle(Paint.Style.FILL);
        this.mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
        sMaxAlpha = isLightTheme(context) ? 0.05f : 0.08f;
        this.mInterpolator = new DecelerateInterpolator();
        this.mDisplayMetrics = context.getResources().getDisplayMetrics();
        this.mTabHeight = dipToPixels(85.0f);
        this.mTabHeightBuffer = dipToPixels(5.0f);
    }

    private float calculateEdgeEffectMargin(int i) {
        return ((float) (((double) i) * 0.136d)) / 2.0f;
    }

    private float dipToPixels(float f) {
        return TypedValue.applyDimension(1, f, this.mDisplayMetrics);
    }

    private boolean isEdgeEffectRunning() {
        int i = this.mState;
        return i == 5 || i == 6 || i == 3 || i == 2;
    }

    private boolean isLightTheme(Context context) {
        TypedValue typedValue = new TypedValue();
        return !context.getTheme().resolveAttribute(16844176, typedValue, true) || typedValue.data != 0;
    }

    private void update() {
        float min = Math.min(((float) (AnimationUtils.currentAnimationTimeMillis() - this.mStartTime)) / this.mDuration, 1.0f);
        float interpolation = this.mInterpolator.getInterpolation(min);
        float f = this.mGlowAlphaStart;
        this.mGlowAlpha = f + ((this.mGlowAlphaFinish - f) * interpolation);
        float f2 = this.mGlowScaleYStart;
        this.mGlowScaleY = f2 + ((this.mGlowScaleYFinish - f2) * interpolation);
        this.mDisplacement = (this.mDisplacement + this.mTargetDisplacement) / 2.0f;
        if (min >= 0.999f || this.mState == 1) {
            switch (this.mState) {
                case 1:
                    this.mState = 5;
                    this.mStartTime = AnimationUtils.currentAnimationTimeMillis();
                    this.mDuration = 250.0f;
                    this.mGlowAlphaStart = 0.0f;
                    this.mGlowScaleYStart = 0.0f;
                    this.mGlowAlphaFinish = sMaxAlpha;
                    this.mGlowScaleYFinish = this.MAX_SCALE;
                    this.mGlowScaleY = 0.0f;
                    this.mOnReleaseCalled = false;
                    return;
                case 2:
                    this.mState = 6;
                    this.mStartTime = AnimationUtils.currentAnimationTimeMillis();
                    this.mDuration = 0.0f;
                    float f3 = sMaxAlpha;
                    this.mGlowAlphaStart = f3;
                    this.mGlowAlphaFinish = f3;
                    float f4 = this.MAX_SCALE;
                    this.mGlowScaleYStart = f4;
                    this.mGlowScaleYFinish = f4;
                    return;
                case 3:
                    this.mState = 0;
                    return;
                case 4:
                    this.mState = 3;
                    return;
                case 5:
                    this.mState = 6;
                    this.mStartTime = AnimationUtils.currentAnimationTimeMillis();
                    this.mDuration = 0.0f;
                    float f5 = sMaxAlpha;
                    this.mGlowAlphaStart = f5;
                    this.mGlowAlphaFinish = f5;
                    float f6 = this.MAX_SCALE;
                    this.mGlowScaleYStart = f6;
                    this.mGlowScaleYFinish = f6;
                    return;
                case 6:
                    this.mState = 3;
                    this.mStartTime = AnimationUtils.currentAnimationTimeMillis();
                    this.mDuration = 450.0f;
                    this.mGlowAlphaStart = this.mGlowAlpha;
                    this.mGlowScaleYStart = this.mGlowScaleY;
                    this.mGlowAlphaFinish = 0.0f;
                    this.mGlowScaleYFinish = 0.0f;
                    return;
                default:
                    return;
            }
        }
    }

    public boolean draw(Canvas canvas) {
        boolean z;
        update();
        int save = canvas.save();
        float centerX = (float) this.mBounds.centerX();
        canvas.scale(1.0f, Math.min(this.mGlowScaleY, 1.0f), centerX, 0.0f);
        Math.max(0.0f, Math.min(this.mDisplacement, 1.0f));
        float f = this.mEdgeControlPointHeight;
        float width = ((float) this.mBounds.width()) * 0.2f;
        this.mPath.reset();
        this.mPath.moveTo(this.mEdgeEffectMargin, 0.0f);
        this.mPath.lineTo(this.mEdgeEffectMargin, 0.0f);
        this.mPath.cubicTo(centerX - width, f, centerX + width, f, ((float) this.mBounds.width()) - this.mEdgeEffectMargin, 0.0f);
        this.mPath.lineTo(((float) this.mBounds.width()) - this.mEdgeEffectMargin, 0.0f);
        this.mPath.close();
        this.mPaint.setAlpha((int) (this.mGlowAlpha * 255.0f));
        canvas.drawPath(this.mPath, this.mPaint);
        canvas.restoreToCount(save);
        if (this.mState == 3 && this.mGlowScaleY == 0.0f) {
            this.mState = 0;
            z = true;
        } else {
            z = false;
        }
        return this.mState != 0 || z;
    }

    public void finish() {
        this.mState = 0;
    }

    @ColorInt
    public int getColor() {
        return this.mPaint.getColor();
    }

    public int getMaxHeight() {
        return (int) ((((float) this.mBounds.height()) * 2.0f) + 0.5f);
    }

    public boolean isFinished() {
        return this.mState == 0;
    }

    public void onAbsorb(int i) {
        if (!isEdgeEffectRunning()) {
            View view = this.mHostView;
            if (view != null) {
                view.performHapticFeedback(SeslHapticFeedbackConstantsReflector.semGetVibrationIndex(28));
            }
            this.mOnReleaseCalled = true;
            this.mState = 2;
            Math.min(Math.max(100, Math.abs(i)), 10000);
            this.mStartTime = AnimationUtils.currentAnimationTimeMillis();
            this.mDuration = 250.0f;
            this.mGlowAlphaStart = 0.0f;
            this.mGlowScaleYStart = 0.0f;
            this.mGlowScaleYFinish = this.MAX_SCALE;
            this.mGlowAlphaFinish = sMaxAlpha;
            this.mTargetDisplacement = 0.5f;
            this.mHandler.sendEmptyMessageDelayed(1, 700);
        }
    }

    public void onPull(float f) {
        onPull(f, 0.5f);
    }

    public void onPull(float f, float f2) {
        int semGetVibrationIndex;
        if (this.mPullDistance == 0.0f) {
            this.mOnReleaseCalled = false;
            if (isEdgeEffectRunning()) {
                this.mPullDistance += f;
            }
        }
        long currentAnimationTimeMillis = AnimationUtils.currentAnimationTimeMillis();
        this.mTargetDisplacement = f2;
        if (this.mState != 4 || ((float) (currentAnimationTimeMillis - this.mStartTime)) >= this.mDuration) {
            if (this.mState != 1) {
                this.mGlowScaleY = Math.max(0.0f, this.mGlowScaleY);
            }
            if (isEdgeEffectRunning()) {
                return;
            }
            if (this.mPullDistance == 0.0f || this.mOnReleaseCalled) {
                if (!(this.mHostView == null || (semGetVibrationIndex = SeslHapticFeedbackConstantsReflector.semGetVibrationIndex(28)) == -1)) {
                    this.mHostView.performHapticFeedback(semGetVibrationIndex);
                }
                this.mState = 1;
                this.mStartTime = currentAnimationTimeMillis;
                this.mDuration = 167.0f;
                this.mPullDistance += f;
            }
        }
    }

    public void onPullCallOnRelease(float f, float f2, int i) {
        this.mTempDeltaDistance = f;
        this.mTempDisplacement = f2;
        if (i == 0) {
            this.mOnReleaseCalled = true;
            onPull(f, f2);
            this.mHandler.sendEmptyMessageDelayed(1, 700);
            return;
        }
        this.mHandler.postDelayed(this.mForceCallOnRelease, (long) i);
    }

    public void onRelease() {
        this.mPullDistance = 0.0f;
        this.mOnReleaseCalled = true;
        int i = this.mState;
        if (i == 1 || i == 4) {
            this.mState = 3;
            this.mGlowAlphaStart = this.mGlowAlpha;
            this.mGlowScaleYStart = this.mGlowScaleY;
            this.mGlowAlphaFinish = 0.0f;
            this.mGlowScaleYFinish = 0.0f;
            this.mStartTime = AnimationUtils.currentAnimationTimeMillis();
            this.mDuration = 450.0f;
        }
    }

    public void setColor(@ColorInt int i) {
        this.mPaint.setColor(i);
    }

    public void setHostView(View view, boolean z) {
        this.mHostView = view;
        this.mCanVerticalScroll = z;
    }

    public void setSize(int i, int i2) {
        float f = (float) i;
        float f2 = (0.75f * f) / SIN;
        float f3 = f2 - (COS * f2);
        float f4 = (float) i2;
        if (f <= this.mTabHeight + this.mTabHeightBuffer) {
            this.mEdgePadding = dipToPixels(3.0f);
            this.mEdgeControlPointHeight = dipToPixels(19.0f);
        } else {
            this.mEdgePadding = dipToPixels(5.0f);
            this.mEdgeControlPointHeight = dipToPixels(29.0f);
        }
        if (mIsOneUI4 && this.mCanVerticalScroll) {
            this.mEdgeEffectMargin = calculateEdgeEffectMargin(i);
        }
        Rect rect = this.mBounds;
        rect.set(rect.left, rect.top, i, (int) Math.min(f4, f3));
    }
}