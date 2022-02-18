package de.dlyt.yanndroid.oneui.widget;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.FloatProperty;
import android.util.IntProperty;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewDebug;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;
import android.widget.RemoteViews;

import androidx.annotation.IntDef;
import androidx.annotation.InterpolatorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.DrawableUtils;
import androidx.appcompat.widget.ViewUtils;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.util.Pools;
import androidx.core.view.ViewCompat;
import androidx.reflect.graphics.drawable.SeslStateListDrawableReflector;
import androidx.reflect.view.SeslViewReflector;
import androidx.vectordrawable.graphics.drawable.Animatable2Compat;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import de.dlyt.yanndroid.oneui.R;

@RemoteViews.RemoteView
public class ProgressBar extends View {
    private boolean mIsOneUI4;
    private static final int MAX_LEVEL = 10000;
    public static final int MODE_CIRCLE = 7;
    public static final int MODE_DUAL_COLOR = 2;
    public static final int MODE_EXPAND = 5;
    public static final int MODE_EXPAND_VERTICAL = 6;
    public static final int MODE_SPLIT = 4;
    protected static final int MODE_STANDARD = 0;
    public static final int MODE_VERTICAL = 3;
    public static final int MODE_WARNING = 1;
    @IntDef(value = {MODE_STANDARD, MODE_WARNING, MODE_DUAL_COLOR, MODE_VERTICAL, MODE_SPLIT, MODE_EXPAND, MODE_EXPAND_VERTICAL, MODE_CIRCLE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface SeekBarMode { }
    private static final int PROGRESS_ANIM_DURATION = 80;
    private static final DecelerateInterpolator PROGRESS_ANIM_INTERPOLATOR = new DecelerateInterpolator();
    private static final int TIMEOUT_SEND_ACCESSIBILITY_EVENT = 200;
    private AccessibilityEventSender mAccessibilityEventSender;
    private boolean mAggregatedIsVisible;
    private AlphaAnimation mAnimation;
    private boolean mAttached;
    private int mBehavior;
    private CircleAnimationCallback mCircleAnimationCallback;
    private int mCirclePadding;
    private Drawable mCurrentDrawable;
    protected int mCurrentMode = MODE_STANDARD;
    protected float mDensity;
    private int mDuration;
    private boolean mHasAnimation;
    private boolean mInDrawing;
    private boolean mIndeterminate;
    private Drawable mIndeterminateDrawable;
    private Drawable mIndeterminateHorizontalLarge;
    private Drawable mIndeterminateHorizontalMedium;
    private Drawable mIndeterminateHorizontalSmall;
    private Drawable mIndeterminateHorizontalXlarge;
    private Drawable mIndeterminateHorizontalXsmall;
    private Interpolator mInterpolator;
    private int mMax;
    protected int mMaxHeight;
    private boolean mMaxInitialized;
    protected int mMaxWidth;
    private int mMin;
    protected int mMinHeight;
    private boolean mMinInitialized;
    protected int mMinWidth;
    protected boolean mMirrorForRtl = false;
    private boolean mNoInvalidate;
    private boolean mOnlyIndeterminate;
    private int mProgress;
    private Drawable mProgressDrawable;
    private ProgressTintInfo mProgressTintInfo;
    private final ArrayList<RefreshData> mRefreshData = new ArrayList<RefreshData>();
    private boolean mRefreshIsPosted;
    private RefreshProgressRunnable mRefreshProgressRunnable;
    private int mRoundStrokeWidth;
    int mSampleWidth = 0;
    private int mSecondaryProgress;
    private boolean mShouldStartAnimationDrawable;
    private Transformation mTransformation;
    private long mUiThreadId;
    private boolean mUseHorizontalProgress = false;
    private float mVisualProgress;

    private final FloatProperty<ProgressBar> VISUAL_PROGRESS = new FloatProperty<ProgressBar>("visual_progress") {
        @Override
        public void setValue(ProgressBar object, float value) {
            object.setVisualProgress(android.R.id.progress, value);
            object.mVisualProgress = value;
        }

        @Override
        public Float get(ProgressBar object) {
            return object.mVisualProgress;
        }
    };

    public ProgressBar(Context context) {
        this(context, null);
    }

    public ProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.progressBarStyle);
    }

    public ProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    @SuppressLint("RestrictedApi")
    public ProgressBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        mIsOneUI4 = context.getTheme().obtainStyledAttributes(new int[]{R.attr.isOneUI4}).getBoolean(0, false);

        mUiThreadId = Thread.currentThread().getId();
        initProgressBar();

        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ProgressBar, defStyleAttr, defStyleRes);
        if (Build.VERSION.SDK_INT >= 29) {
            saveAttributeDataForStyleable(context, R.styleable.ProgressBar, attrs, a, defStyleAttr, defStyleRes);
        }

        mNoInvalidate = true;

        final Drawable progressDrawable = a.getDrawable(R.styleable.ProgressBar_android_progressDrawable);
        if (progressDrawable != null) {
            if (needsTileify(progressDrawable)) {
                setProgressDrawableTiled(progressDrawable);
            } else {
                setProgressDrawable(progressDrawable);
            }
        }

        mDuration = a.getInt(R.styleable.ProgressBar_android_indeterminateDuration, mDuration);

        mMinWidth = a.getDimensionPixelSize(R.styleable.ProgressBar_android_minWidth, mMinWidth);
        mMaxWidth = a.getDimensionPixelSize(R.styleable.ProgressBar_android_maxWidth, mMaxWidth);
        mMinHeight = a.getDimensionPixelSize(R.styleable.ProgressBar_android_minHeight, mMinHeight);
        mMaxHeight = a.getDimensionPixelSize(R.styleable.ProgressBar_android_maxHeight, mMaxHeight);

        mBehavior = a.getInt(R.styleable.ProgressBar_android_indeterminateBehavior, mBehavior);

        final int resID = a.getResourceId(R.styleable.ProgressBar_android_interpolator, android.R.anim.linear_interpolator);
        if (resID > 0) {
            setInterpolator(context, resID);
        }

        setMin(a.getInt(R.styleable.ProgressBar_android_min, mMin));
        setMax(a.getInt(R.styleable.ProgressBar_android_max, mMax));

        setProgress(a.getInt(R.styleable.ProgressBar_android_progress, mProgress));

        setSecondaryProgress(a.getInt(R.styleable.ProgressBar_android_secondaryProgress, mSecondaryProgress));

        final Drawable indeterminateDrawable = a.getDrawable(R.styleable.ProgressBar_android_indeterminateDrawable);
        if (indeterminateDrawable != null) {
            if (needsTileify(indeterminateDrawable)) {
                setIndeterminateDrawableTiled(indeterminateDrawable);
            } else {
                setIndeterminateDrawable(indeterminateDrawable);
            }
        }

        mOnlyIndeterminate = a.getBoolean(R.styleable.ProgressBar_android_indeterminateOnly, mOnlyIndeterminate);

        mNoInvalidate = false;

        setIndeterminate(mOnlyIndeterminate || a.getBoolean(R.styleable.ProgressBar_android_indeterminate, mIndeterminate));

        mMirrorForRtl = a.getBoolean(R.styleable.ProgressBar_android_mirrorForRtl, mMirrorForRtl);

        if (a.hasValue(R.styleable.ProgressBar_android_progressTintMode)) {
            if (mProgressTintInfo == null) {
                mProgressTintInfo = new ProgressTintInfo();
            }
            mProgressTintInfo.mProgressTintMode = DrawableUtils.parseTintMode(a.getInt(R.styleable.ProgressBar_android_progressTintMode, -1), null);
            mProgressTintInfo.mHasProgressTintMode = true;
        }

        if (a.hasValue(R.styleable.ProgressBar_android_progressTint)) {
            if (mProgressTintInfo == null) {
                mProgressTintInfo = new ProgressTintInfo();
            }
            mProgressTintInfo.mProgressTintList = a.getColorStateList(R.styleable.ProgressBar_android_progressTint);
            mProgressTintInfo.mHasProgressTint = true;
        }

        if (a.hasValue(R.styleable.ProgressBar_android_progressBackgroundTintMode)) {
            if (mProgressTintInfo == null) {
                mProgressTintInfo = new ProgressTintInfo();
            }
            mProgressTintInfo.mProgressBackgroundTintMode = DrawableUtils.parseTintMode(a.getInt(R.styleable.ProgressBar_android_progressBackgroundTintMode, -1), null);
            mProgressTintInfo.mHasProgressBackgroundTintMode = true;
        }

        if (a.hasValue(R.styleable.ProgressBar_android_progressBackgroundTint)) {
            if (mProgressTintInfo == null) {
                mProgressTintInfo = new ProgressTintInfo();
            }
            mProgressTintInfo.mProgressBackgroundTintList = a.getColorStateList(R.styleable.ProgressBar_android_progressBackgroundTint);
            mProgressTintInfo.mHasProgressBackgroundTint = true;
        }

        if (a.hasValue(R.styleable.ProgressBar_android_secondaryProgressTintMode)) {
            if (mProgressTintInfo == null) {
                mProgressTintInfo = new ProgressTintInfo();
            }
            mProgressTintInfo.mSecondaryProgressTintMode = DrawableUtils.parseTintMode(a.getInt(R.styleable.ProgressBar_android_secondaryProgressTintMode, -1), null);
            mProgressTintInfo.mHasSecondaryProgressTintMode = true;
        }

        if (a.hasValue(R.styleable.ProgressBar_android_secondaryProgressTint)) {
            if (mProgressTintInfo == null) {
                mProgressTintInfo = new ProgressTintInfo();
            }
            mProgressTintInfo.mSecondaryProgressTintList = a.getColorStateList(R.styleable.ProgressBar_android_secondaryProgressTint);
            mProgressTintInfo.mHasSecondaryProgressTint = true;
        }

        if (a.hasValue(R.styleable.ProgressBar_android_indeterminateTintMode)) {
            if (mProgressTintInfo == null) {
                mProgressTintInfo = new ProgressTintInfo();
            }
            mProgressTintInfo.mIndeterminateTintMode = DrawableUtils.parseTintMode(a.getInt(R.styleable.ProgressBar_android_indeterminateTintMode, -1), null);
            mProgressTintInfo.mHasIndeterminateTintMode = true;
        }

        if (a.hasValue(R.styleable.ProgressBar_android_indeterminateTint)) {
            if (mProgressTintInfo == null) {
                mProgressTintInfo = new ProgressTintInfo();
            }
            mProgressTintInfo.mIndeterminateTintList = a.getColorStateList(R.styleable.ProgressBar_android_indeterminateTint);
            mProgressTintInfo.mHasIndeterminateTint = true;
        }

        mUseHorizontalProgress = a.getBoolean(R.styleable.ProgressBar_useHorizontalProgress, mUseHorizontalProgress);
        
        mIndeterminateHorizontalXsmall = getResources().getDrawable(R.drawable.sesl_progress_bar_indeterminate_xsmall_transition, context.getTheme());
        mIndeterminateHorizontalSmall = getResources().getDrawable(R.drawable.sesl_progress_bar_indeterminate_small_transition, context.getTheme());
        mIndeterminateHorizontalMedium = getResources().getDrawable(R.drawable.sesl_progress_bar_indeterminate_medium_transition, context.getTheme());
        mIndeterminateHorizontalLarge = getResources().getDrawable(R.drawable.sesl_progress_bar_indeterminate_large_transition, context.getTheme());
        mIndeterminateHorizontalXlarge = getResources().getDrawable(R.drawable.sesl_progress_bar_indeterminate_xlarge_transition, context.getTheme());

        a.recycle();

        applyProgressTints();
        applyIndeterminateTint();

        if (getImportantForAccessibility() == View.IMPORTANT_FOR_ACCESSIBILITY_AUTO) {
            setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
        }

        mDensity = context.getResources().getDisplayMetrics().density;
        mCircleAnimationCallback = new CircleAnimationCallback(this);
    }

    public void setMinWidth(@Px int minWidth) {
        mMinWidth = minWidth;
        requestLayout();
    }

    @Px public int getMinWidth() {
        return mMinWidth;
    }

    public void setMaxWidth(@Px int maxWidth) {
        mMaxWidth = maxWidth;
        requestLayout();
    }

    @Px public int getMaxWidth() {
        return mMaxWidth;
    }

    public void setMinHeight(@Px int minHeight) {
        mMinHeight = minHeight;
        requestLayout();
    }

    @Px public int getMinHeight() {
        return mMinHeight;
    }

    public void setMaxHeight(@Px int maxHeight) {
        mMaxHeight = maxHeight;
        requestLayout();
    }

    @Px public int getMaxHeight() {
        return mMaxHeight;
    }

    private static boolean needsTileify(Drawable dr) {
        if (dr instanceof LayerDrawable) {
            final LayerDrawable orig = (LayerDrawable) dr;
            final int N = orig.getNumberOfLayers();
            for (int i = 0; i < N; i++) {
                if (needsTileify(orig.getDrawable(i))) {
                    return true;
                }
            }
            return false;
        }

        if (dr instanceof StateListDrawable) {
            final StateListDrawable in = (StateListDrawable) dr;
            final int N = StateListDrawableCompat.getStateCount(in);
            for (int i = 0; i < N; i++) {
                if (needsTileify(StateListDrawableCompat.getStateDrawable(in, i))) {
                    return true;
                }
            }
            return false;
        }

        if (dr instanceof BitmapDrawable) {
            return true;
        }

        return false;
    }

    private Drawable tileify(Drawable drawable, boolean clip) {
        if (drawable instanceof LayerDrawable) {
            final LayerDrawable orig = (LayerDrawable) drawable;
            final int N = orig.getNumberOfLayers();
            final Drawable[] outDrawables = new Drawable[N];

            for (int i = 0; i < N; i++) {
                final int id = orig.getId(i);
                outDrawables[i] = tileify(orig.getDrawable(i), (id == android.R.id.progress || id == android.R.id.secondaryProgress));
            }

            final LayerDrawable clone = new LayerDrawable(outDrawables);
            if (Build.VERSION.SDK_INT >= 23) {
                for (int i = 0; i < N; i++) {
                    clone.setId(i, orig.getId(i));
                    clone.setLayerGravity(i, orig.getLayerGravity(i));
                    clone.setLayerWidth(i, orig.getLayerWidth(i));
                    clone.setLayerHeight(i, orig.getLayerHeight(i));
                    clone.setLayerInsetLeft(i, orig.getLayerInsetLeft(i));
                    clone.setLayerInsetRight(i, orig.getLayerInsetRight(i));
                    clone.setLayerInsetTop(i, orig.getLayerInsetTop(i));
                    clone.setLayerInsetBottom(i, orig.getLayerInsetBottom(i));
                    clone.setLayerInsetStart(i, orig.getLayerInsetStart(i));
                    clone.setLayerInsetEnd(i, orig.getLayerInsetEnd(i));
                }
            }

            return clone;
        }

        if (drawable instanceof StateListDrawable) {
            final StateListDrawable in = (StateListDrawable) drawable;
            final StateListDrawable out = new StateListDrawable();
            final int N = StateListDrawableCompat.getStateCount(in);
            for (int i = 0; i < N; i++) {
                out.addState(StateListDrawableCompat.getStateSet(in, i), tileify(StateListDrawableCompat.getStateDrawable(in, i), clip));
            }

            return out;
        }

        if (drawable instanceof BitmapDrawable) {
            final Drawable.ConstantState cs = drawable.getConstantState();
            final BitmapDrawable clone = (BitmapDrawable) cs.newDrawable(getResources());
            clone.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.CLAMP);

            if (mSampleWidth <= 0) {
                mSampleWidth = clone.getIntrinsicWidth();
            }

            if (clip) {
                return new ClipDrawable(clone, Gravity.LEFT, ClipDrawable.HORIZONTAL);
            } else {
                return clone;
            }
        }

        return drawable;
    }

    private Drawable tileifyIndeterminate(Drawable drawable) {
        if (drawable instanceof AnimationDrawable) {
            AnimationDrawable background = (AnimationDrawable) drawable;
            final int N = background.getNumberOfFrames();
            AnimationDrawable newBg = new AnimationDrawable();
            newBg.setOneShot(background.isOneShot());

            for (int i = 0; i < N; i++) {
                Drawable frame = tileify(background.getFrame(i), true);
                frame.setLevel(MAX_LEVEL);
                newBg.addFrame(frame, background.getDuration(i));
            }
            newBg.setLevel(MAX_LEVEL);
            drawable = newBg;
        }
        return drawable;
    }

    private void initProgressBar() {
        mMin = 0;
        mMax = 100;
        mProgress = 0;
        mSecondaryProgress = 0;
        mIndeterminate = false;
        mOnlyIndeterminate = false;
        mDuration = 4000;
        mBehavior = AlphaAnimation.RESTART;
        mMinWidth = 24;
        mMaxWidth = 48;
        mMinHeight = 24;
        mMaxHeight = 48;
    }

    @ViewDebug.ExportedProperty(category = "progress")
    public synchronized boolean isIndeterminate() {
        return mIndeterminate;
    }

    public synchronized void setIndeterminate(boolean indeterminate) {
        if ((!mOnlyIndeterminate || !mIndeterminate) && indeterminate != mIndeterminate) {
            mIndeterminate = indeterminate;

            if (indeterminate) {
                swapCurrentDrawable(mIndeterminateDrawable);
                startAnimation();
            } else {
                swapCurrentDrawable(mProgressDrawable);
                stopAnimation();
            }
        }
    }

    private void swapCurrentDrawable(Drawable newDrawable) {
        final Drawable oldDrawable = mCurrentDrawable;
        mCurrentDrawable = newDrawable;

        if (oldDrawable != mCurrentDrawable) {
            if (oldDrawable != null) {
                oldDrawable.setVisible(false, false);
            }
            if (mCurrentDrawable != null) {
                mCurrentDrawable.setVisible(getWindowVisibility() == VISIBLE && isShown(), false);
            }
        }
    }

    public Drawable getIndeterminateDrawable() {
        return mIndeterminateDrawable;
    }

    public void setIndeterminateDrawable(Drawable d) {
        if (mIndeterminateDrawable != d) {
            if (mIndeterminateDrawable != null) {
                if (mUseHorizontalProgress) {
                    stopAnimation();
                }
                mIndeterminateDrawable.setCallback(null);
                unscheduleDrawable(mIndeterminateDrawable);
            }

            mIndeterminateDrawable = d;

            if (d != null) {
                d.setCallback(this);
                DrawableCompat.setLayoutDirection(d, getLayoutDirection());
                if (d.isStateful()) {
                    d.setState(getDrawableState());
                }
                applyIndeterminateTint();
            }

            if (mIndeterminate) {
                if (mUseHorizontalProgress) {
                    startAnimation();
                }
                swapCurrentDrawable(d);
                postInvalidate();
            }
        }
    }

    public void setIndeterminateTintList(@Nullable ColorStateList tint) {
        if (mProgressTintInfo == null) {
            mProgressTintInfo = new ProgressTintInfo();
        }
        mProgressTintInfo.mIndeterminateTintList = tint;
        mProgressTintInfo.mHasIndeterminateTint = true;

        applyIndeterminateTint();
    }

    @Nullable
    public ColorStateList getIndeterminateTintList() {
        return mProgressTintInfo != null ? mProgressTintInfo.mIndeterminateTintList : null;
    }

    public void setIndeterminateTintMode(@Nullable PorterDuff.Mode tintMode) {
        if (mProgressTintInfo == null) {
            mProgressTintInfo = new ProgressTintInfo();
        }
        mProgressTintInfo.mIndeterminateTintMode = tintMode;
        mProgressTintInfo.mHasIndeterminateTintMode = true;

        applyIndeterminateTint();
    }

    @Nullable
    public PorterDuff.Mode getIndeterminateTintMode() {
        return mProgressTintInfo != null ? mProgressTintInfo.mIndeterminateTintMode : null;
    }

    private void applyIndeterminateTint() {
        if (mIndeterminateDrawable != null && mProgressTintInfo != null) {
            final ProgressTintInfo tintInfo = mProgressTintInfo;
            if (tintInfo.mHasIndeterminateTint || tintInfo.mHasIndeterminateTintMode) {
                mIndeterminateDrawable = mIndeterminateDrawable.mutate();

                if (tintInfo.mHasIndeterminateTint) {
                    DrawableCompat.setTintList(mIndeterminateDrawable, tintInfo.mIndeterminateTintList);
                }

                if (tintInfo.mHasIndeterminateTintMode) {
                    DrawableCompat.setTintMode(mIndeterminateDrawable, tintInfo.mIndeterminateTintMode);
                }

                if (mIndeterminateDrawable.isStateful()) {
                    mIndeterminateDrawable.setState(getDrawableState());
                }
            }
        }
    }

    public void setIndeterminateDrawableTiled(Drawable d) {
        if (d != null) {
            d = tileifyIndeterminate(d);
        }

        setIndeterminateDrawable(d);
    }

    public Drawable getProgressDrawable() {
        return mProgressDrawable;
    }

    public void setProgressDrawable(Drawable d) {
        if (mProgressDrawable != d) {
            if (mProgressDrawable != null) {
                mProgressDrawable.setCallback(null);
                unscheduleDrawable(mProgressDrawable);
            }

            mProgressDrawable = d;

            if (d != null) {
                d.setCallback(this);
                DrawableCompat.setLayoutDirection(d, getLayoutDirection());
                if (d.isStateful()) {
                    d.setState(getDrawableState());
                }

                if (mCurrentMode == MODE_VERTICAL || mCurrentMode == MODE_EXPAND_VERTICAL) {
                    int drawableWidth = d.getMinimumWidth();
                    if (mMaxWidth < drawableWidth) {
                        mMaxWidth = drawableWidth;
                        requestLayout();
                    }
                } else {
                    int drawableHeight = d.getMinimumHeight();
                    if (mMaxHeight < drawableHeight) {
                        mMaxHeight = drawableHeight;
                        requestLayout();
                    }
                }

                applyProgressTints();
            }

            if (!mIndeterminate) {
                swapCurrentDrawable(d);
                postInvalidate();
            }

            updateDrawableBounds(getWidth(), getHeight());
            updateDrawableState();

            doRefreshProgress(android.R.id.progress, mProgress, false, false, false);
            doRefreshProgress(android.R.id.secondaryProgress, mSecondaryProgress, false, false, false);
        }
    }

    public boolean getMirrorForRtl() {
        return mMirrorForRtl;
    }

    private void applyProgressTints() {
        if (mProgressDrawable != null && mProgressTintInfo != null) {
            applyPrimaryProgressTint();
            applyProgressBackgroundTint();
            applySecondaryProgressTint();
        }
    }

    private void applyPrimaryProgressTint() {
        if (mProgressTintInfo.mHasProgressTint || mProgressTintInfo.mHasProgressTintMode) {
            final Drawable target = getTintTarget(android.R.id.progress, true);
            if (target != null) {
                if (mProgressTintInfo.mHasProgressTint) {
                    target.setTintList(mProgressTintInfo.mProgressTintList);
                }
                if (mProgressTintInfo.mHasProgressTintMode) {
                    DrawableCompat.setTintMode(target, mProgressTintInfo.mProgressTintMode);
                }

                if (target.isStateful()) {
                    target.setState(getDrawableState());
                }
            }
        }
    }

    private void applyProgressBackgroundTint() {
        if (mProgressTintInfo.mHasProgressBackgroundTint || mProgressTintInfo.mHasProgressBackgroundTintMode) {
            final Drawable target = getTintTarget(android.R.id.background, false);
            if (target != null) {
                if (mProgressTintInfo.mHasProgressBackgroundTint) {
                    target.setTintList(mProgressTintInfo.mProgressBackgroundTintList);
                }
                if (mProgressTintInfo.mHasProgressBackgroundTintMode) {
                    DrawableCompat.setTintMode(target, mProgressTintInfo.mProgressBackgroundTintMode);
                }

                if (target.isStateful()) {
                    target.setState(getDrawableState());
                }
            }
        }
    }

    private void applySecondaryProgressTint() {
        if (mProgressTintInfo.mHasSecondaryProgressTint || mProgressTintInfo.mHasSecondaryProgressTintMode) {
            final Drawable target = getTintTarget(android.R.id.secondaryProgress, false);
            if (target != null) {
                if (mProgressTintInfo.mHasSecondaryProgressTint) {
                    target.setTintList(mProgressTintInfo.mSecondaryProgressTintList);
                }
                if (mProgressTintInfo.mHasSecondaryProgressTintMode) {
                    DrawableCompat.setTintMode(target, mProgressTintInfo.mSecondaryProgressTintMode);
                }

                if (target.isStateful()) {
                    target.setState(getDrawableState());
                }
            }
        }
    }

    public void setProgressTintList(@Nullable ColorStateList tint) {
        if (mProgressTintInfo == null) {
            mProgressTintInfo = new ProgressTintInfo();
        }
        mProgressTintInfo.mProgressTintList = tint;
        mProgressTintInfo.mHasProgressTint = true;

        if (mProgressDrawable != null) {
            applyPrimaryProgressTint();
        }
    }

    @Nullable
    public ColorStateList getProgressTintList() {
        return mProgressTintInfo != null ? mProgressTintInfo.mProgressTintList : null;
    }

    public void setProgressTintMode(@Nullable PorterDuff.Mode tintMode) {
        if (mProgressTintInfo == null) {
            mProgressTintInfo = new ProgressTintInfo();
        }
        mProgressTintInfo.mProgressTintMode = tintMode;
        mProgressTintInfo.mHasProgressTintMode = true;

        if (mProgressDrawable != null) {
            applyPrimaryProgressTint();
        }
    }

    @Nullable
    public PorterDuff.Mode getProgressTintMode() {
        return mProgressTintInfo != null ? mProgressTintInfo.mProgressTintMode : null;
    }

    public void setProgressBackgroundTintList(@Nullable ColorStateList tint) {
        if (mProgressTintInfo == null) {
            mProgressTintInfo = new ProgressTintInfo();
        }
        mProgressTintInfo.mProgressBackgroundTintList = tint;
        mProgressTintInfo.mHasProgressBackgroundTint = true;

        if (mProgressDrawable != null) {
            applyProgressBackgroundTint();
        }
    }

    @Nullable
    public ColorStateList getProgressBackgroundTintList() {
        return mProgressTintInfo != null ? mProgressTintInfo.mProgressBackgroundTintList : null;
    }

    public void setProgressBackgroundTintMode(@Nullable PorterDuff.Mode tintMode) {
        if (mProgressTintInfo == null) {
            mProgressTintInfo = new ProgressTintInfo();
        }
        mProgressTintInfo.mProgressBackgroundTintMode = tintMode;
        mProgressTintInfo.mHasProgressBackgroundTintMode = true;

        if (mProgressDrawable != null) {
            applyProgressBackgroundTint();
        }
    }

    @Nullable
    public PorterDuff.Mode getProgressBackgroundTintMode() {
        return mProgressTintInfo != null ? mProgressTintInfo.mProgressBackgroundTintMode : null;
    }

    public void setSecondaryProgressTintList(@Nullable ColorStateList tint) {
        if (mProgressTintInfo == null) {
            mProgressTintInfo = new ProgressTintInfo();
        }
        mProgressTintInfo.mSecondaryProgressTintList = tint;
        mProgressTintInfo.mHasSecondaryProgressTint = true;

        if (mProgressDrawable != null) {
            applySecondaryProgressTint();
        }
    }

    @Nullable
    public ColorStateList getSecondaryProgressTintList() {
        return mProgressTintInfo != null ? mProgressTintInfo.mSecondaryProgressTintList : null;
    }

    public void setSecondaryProgressTintMode(@Nullable PorterDuff.Mode tintMode) {
        if (mProgressTintInfo == null) {
            mProgressTintInfo = new ProgressTintInfo();
        }
        mProgressTintInfo.mSecondaryProgressTintMode = tintMode;
        mProgressTintInfo.mHasSecondaryProgressTintMode = true;

        if (mProgressDrawable != null) {
            applySecondaryProgressTint();
        }
    }

    @Nullable
    public PorterDuff.Mode getSecondaryProgressTintMode() {
        return mProgressTintInfo != null ? mProgressTintInfo.mSecondaryProgressTintMode : null;
    }

    @Nullable
    private Drawable getTintTarget(int layerId, boolean shouldFallback) {
        Drawable layer = null;

        final Drawable d = mProgressDrawable;
        if (d != null) {
            mProgressDrawable = d.mutate();

            if (d instanceof LayerDrawable) {
                layer = ((LayerDrawable) d).findDrawableByLayerId(layerId);
            }

            if (shouldFallback && layer == null) {
                layer = d;
            }
        }

        return layer;
    }

    public void setProgressDrawableTiled(Drawable d) {
        if (d != null) {
            d = tileify(d, false);
        }

        setProgressDrawable(d);
    }

    @Nullable
    public Drawable getCurrentDrawable() {
        return mCurrentDrawable;
    }

    @Override
    protected boolean verifyDrawable(@NonNull Drawable who) {
        return who == mProgressDrawable || who == mIndeterminateDrawable || super.verifyDrawable(who);
    }

    @Override
    public void jumpDrawablesToCurrentState() {
        super.jumpDrawablesToCurrentState();
        if (mProgressDrawable != null) mProgressDrawable.jumpToCurrentState();
        if (mIndeterminateDrawable != null) mIndeterminateDrawable.jumpToCurrentState();
    }

    // @Override : hidden method
    @SuppressWarnings("unused")
    public void onResolveDrawables(int layoutDirection) {
        final Drawable d = mCurrentDrawable;
        if (d != null) {
            DrawableCompat.setLayoutDirection(d, layoutDirection);
        }
        if (mIndeterminateDrawable != null) {
            DrawableCompat.setLayoutDirection(mIndeterminateDrawable, layoutDirection);
        }
        if (mProgressDrawable != null) {
            DrawableCompat.setLayoutDirection(mProgressDrawable, layoutDirection);
        }
    }

    @Override
    public void postInvalidate() {
        if (!mNoInvalidate) {
            super.postInvalidate();
        }
    }

    private class RefreshProgressRunnable implements Runnable {
        public void run() {
            synchronized (ProgressBar.this) {
                final int count = mRefreshData.size();
                for (int i = 0; i < count; i++) {
                    final RefreshData rd = mRefreshData.get(i);
                    doRefreshProgress(rd.id, rd.progress, rd.fromUser, true, rd.animate);
                    rd.recycle();
                }
                mRefreshData.clear();
                mRefreshIsPosted = false;
            }
        }
    }

    private static class RefreshData {
        private static final int POOL_MAX = 24;
        private static final Pools.SynchronizedPool<RefreshData> sPool = new Pools.SynchronizedPool<RefreshData>(POOL_MAX);
        public int id;
        public int progress;
        public boolean fromUser;
        public boolean animate;

        public static RefreshData obtain(int id, int progress, boolean fromUser, boolean animate) {
            RefreshData rd = sPool.acquire();
            if (rd == null) {
                rd = new RefreshData();
            }
            rd.id = id;
            rd.progress = progress;
            rd.fromUser = fromUser;
            rd.animate = animate;
            return rd;
        }

        public void recycle() {
            sPool.release(this);
        }
    }

    private synchronized void doRefreshProgress(int id, int progress, boolean fromUser, boolean callBackToApp, boolean animate) {
        int range = mMax - mMin;
        final float scale = range > 0 ? (progress - mMin) / (float) range : 0;
        final boolean isPrimary = id == android.R.id.progress;

        Drawable drawable = mCurrentDrawable;
        if (drawable != null) {
            final int level = (int) (scale * 10000.0f);

            if (drawable instanceof LayerDrawable) {
                Drawable layer = ((LayerDrawable) drawable).findDrawableByLayerId(id);
                if (layer != null && Build.VERSION.SDK_INT > 19 && canResolveLayoutDirection()) {
                    DrawableCompat.setLayoutDirection(layer, ViewCompat.getLayoutDirection(this));
                }
                if (layer != null) {
                    drawable = layer;
                }
                drawable.setLevel(level);
            } else if (drawable instanceof StateListDrawable) {
                for (int i = 0; i < StateListDrawableCompat.getStateCount((StateListDrawable) drawable); i++) {
                    Drawable stateD = StateListDrawableCompat.getStateDrawable((StateListDrawable) drawable, i);
                    Drawable layer = ((LayerDrawable) stateD).findDrawableByLayerId(i);
                    if (stateD != null) {
                        if ((stateD instanceof LayerDrawable) && layer != null && Build.VERSION.SDK_INT > 19 && canResolveLayoutDirection()) {
                            DrawableCompat.setLayoutDirection(layer, ViewCompat.getLayoutDirection(this));
                        }
                        if (layer == null) {
                            layer = drawable;
                        }
                        layer.setLevel(level);
                    } else {
                        return;
                    }
                }
            } else {
                drawable.setLevel(level);
            }
        } else {
            invalidate();
        }

        if (isPrimary && animate) {
            final ObjectAnimator animator = ObjectAnimator.ofFloat(this, VISUAL_PROGRESS, scale);
            if (Build.VERSION.SDK_INT > 18) {
                animator.setAutoCancel(true);
            }
            animator.setDuration(PROGRESS_ANIM_DURATION);
            animator.setInterpolator(PROGRESS_ANIM_INTERPOLATOR);
            animator.start();
        } else {
            setVisualProgress(id, scale);
        }

        if (isPrimary && callBackToApp) {
            onProgressRefresh(scale, fromUser, progress);
        }
    }

    private float getPercent(int progress) {
        final float maxProgress = getMax();
        final float minProgress = getMin();
        final float currentProgress = progress;
        final float diffProgress = maxProgress - minProgress;
        if (diffProgress <= 0.0f) {
            return 0.0f;
        }
        final float percent = (currentProgress - minProgress) / diffProgress;
        return Math.max(0.0f, Math.min(1.0f, percent));
    }

    protected void onProgressRefresh(float scale, boolean fromUser, int progress) {
        if (((AccessibilityManager) getContext().getSystemService(Context.ACCESSIBILITY_SERVICE)).isEnabled()) {
            scheduleAccessibilityEventSender();
        }
        if (mSecondaryProgress > mProgress && !fromUser) {
            refreshProgress(android.R.id.secondaryProgress, mSecondaryProgress, false, false);
        }
    }

    private void setVisualProgress(int id, float progress) {
        mVisualProgress = progress;

        Drawable d = mCurrentDrawable;

        if (d instanceof LayerDrawable) {
            d = ((LayerDrawable) d).findDrawableByLayerId(id);
            if (d == null) {
                d = mCurrentDrawable;
            }
        }

        if (d != null) {
            final int level = (int) (progress * MAX_LEVEL);
            d.setLevel(level);
        } else {
            invalidate();
        }

        onVisualProgressChanged(id, progress);
    }

    protected void onVisualProgressChanged(int id, float progress) {
    }

    private synchronized void refreshProgress(int id, int progress, boolean fromUser, boolean animate) {
        if (mUiThreadId == Thread.currentThread().getId()) {
            doRefreshProgress(id, progress, fromUser, true, animate);
        } else {
            if (mRefreshProgressRunnable == null) {
                mRefreshProgressRunnable = new RefreshProgressRunnable();
            }

            final RefreshData rd = RefreshData.obtain(id, progress, fromUser, animate);
            mRefreshData.add(rd);
            if (mAttached && !mRefreshIsPosted) {
                post(mRefreshProgressRunnable);
                mRefreshIsPosted = true;
            }
        }
    }

    public synchronized void setProgress(int progress) {
        setProgressInternal(progress, false, false);
    }

    public void setProgress(int progress, boolean animate) {
        setProgressInternal(progress, false, animate);
    }

    protected synchronized boolean setProgressInternal(int progress, boolean fromUser, boolean animate) {
        if (mIndeterminate) {
            return false;
        }

        progress = constrain(progress, mMin, mMax);

        if (progress == mProgress) {
            return false;
        }

        mProgress = progress;
        if (mCurrentMode == MODE_CIRCLE) {
            if (getProgressDrawable() instanceof LayerDrawable) {
                Drawable d = ((LayerDrawable) getProgressDrawable()).findDrawableByLayerId(android.R.id.progress);
                if (d != null && d instanceof CirCleProgressDrawable) {
                    ((CirCleProgressDrawable) d).setProgress(mProgress, animate);
                }
            }
        }
        refreshProgress(android.R.id.progress, mProgress, fromUser, animate);
        return true;
    }

    public synchronized void setSecondaryProgress(int secondaryProgress) {
        if (mIndeterminate) {
            return;
        }

        if (secondaryProgress < mMin) {
            secondaryProgress = mMin;
        }

        if (secondaryProgress > mMax) {
            secondaryProgress = mMax;
        }

        if (secondaryProgress != mSecondaryProgress) {
            mSecondaryProgress = secondaryProgress;
            refreshProgress(android.R.id.secondaryProgress, mSecondaryProgress, false, false);
        }
    }

    @ViewDebug.ExportedProperty(category = "progress")
    public synchronized int getProgress() {
        return mIndeterminate ? 0 : mProgress;
    }

    @ViewDebug.ExportedProperty(category = "progress")
    public synchronized int getSecondaryProgress() {
        return mIndeterminate ? 0 : mSecondaryProgress;
    }

    @ViewDebug.ExportedProperty(category = "progress")
    public synchronized int getMin() {
        return mMin;
    }

    @ViewDebug.ExportedProperty(category = "progress")
    public synchronized int getMax() {
        return mMax;
    }

    public synchronized void setMin(int min) {
        if (mMaxInitialized) {
            if (min > mMax) {
                min = mMax;
            }
        }
        mMinInitialized = true;
        if (mMaxInitialized && min != mMin) {
            mMin = min;
            postInvalidate();

            if (mProgress < min) {
                mProgress = min;
            }
            refreshProgress(android.R.id.progress, mProgress, false, false);
        } else {
            mMin = min;
        }
    }

    public synchronized void setMax(int max) {
        if (mMinInitialized) {
            if (max < mMin) {
                max = mMin;
            }
        }
        mMaxInitialized = true;
        if (mMinInitialized && max != mMax) {
            mMax = max;
            postInvalidate();

            if (mProgress > max) {
                mProgress = max;
            }
            refreshProgress(android.R.id.progress, mProgress, false, false);
        } else {
            mMax = max;
        }
    }

    public synchronized final void incrementProgressBy(int diff) {
        setProgress(mProgress + diff);
    }

    public synchronized final void incrementSecondaryProgressBy(int diff) {
        setSecondaryProgress(mSecondaryProgress + diff);
    }

    void startAnimation() {
        if (getVisibility() != VISIBLE || getWindowVisibility() != VISIBLE) {
            return;
        }

        if (Build.VERSION.SDK_INT > 23) {
            if (mIndeterminateDrawable instanceof Animatable) {
                mShouldStartAnimationDrawable = true;
                mHasAnimation = false;
                if (mIndeterminateDrawable instanceof AnimatedVectorDrawable) {
                    AnimatedVectorDrawableCompat.registerAnimationCallback(mIndeterminateDrawable, mCircleAnimationCallback);
                }
            } else {
                mHasAnimation = true;

                if (mInterpolator == null) {
                    mInterpolator = new LinearInterpolator();
                }

                if (mTransformation == null) {
                    mTransformation = new Transformation();
                } else {
                    mTransformation.clear();
                }

                if (mAnimation == null) {
                    mAnimation = new AlphaAnimation(0.0f, 1.0f);
                } else {
                    mAnimation.reset();
                }

                mAnimation.setRepeatMode(mBehavior);
                mAnimation.setRepeatCount(Animation.INFINITE);
                mAnimation.setDuration(mDuration);
                mAnimation.setInterpolator(mInterpolator);
                mAnimation.setStartTime(Animation.START_ON_FIRST_FRAME);
            }
            postInvalidate();
        }
    }

    void stopAnimation() {
        mHasAnimation = false;
        if (mIndeterminateDrawable instanceof Animatable) {
            ((Animatable) mIndeterminateDrawable).stop();
            if (mIndeterminateDrawable instanceof AnimatedVectorDrawable) {
                AnimatedVectorDrawableCompat.unregisterAnimationCallback(mIndeterminateDrawable, mCircleAnimationCallback);
            }
            mShouldStartAnimationDrawable = false;
        }
        postInvalidate();
    }

    public void setInterpolator(Context context, @InterpolatorRes int resID) {
        setInterpolator(AnimationUtils.loadInterpolator(context, resID));
    }

    public void setInterpolator(Interpolator interpolator) {
        mInterpolator = interpolator;
    }

    public Interpolator getInterpolator() {
        return mInterpolator;
    }

    @Override
    public void onVisibilityAggregated(boolean isVisible) {
        super.onVisibilityAggregated(isVisible);

        if (isVisible != mAggregatedIsVisible) {
            mAggregatedIsVisible = isVisible;

            if (mIndeterminate) {
                if (isVisible) {
                    startAnimation();
                } else {
                    stopAnimation();
                }
            }

            if (mCurrentDrawable != null) {
                mCurrentDrawable.setVisible(isVisible, false);
            }
        }
    }

    @Override
    public void invalidateDrawable(@NonNull Drawable dr) {
        if (!mInDrawing) {
            if (verifyDrawable(dr)) {
                final Rect dirty = dr.getBounds();
                final int scrollX = getScrollX() + getPaddingLeft();
                final int scrollY = getScrollY() + getPaddingTop();

                invalidate(dirty.left + scrollX, dirty.top + scrollY, dirty.right + scrollX, dirty.bottom + scrollY);
            } else {
                super.invalidateDrawable(dr);
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        updateDrawableBounds(w, h);
    }

    // kang
    @SuppressLint("RestrictedApi")
    protected void updateDrawableBounds(int w, int h) {
        int var3 = w - (this.getPaddingRight() + this.getPaddingLeft());
        int var4 = h - (this.getPaddingTop() + this.getPaddingBottom());
        Drawable var5 = this.mIndeterminateDrawable;
        h = var3;
        w = var4;
        if (var5 != null) {
            int var10;
            int var11;
            label29: {
                if (this.mOnlyIndeterminate && !(var5 instanceof AnimationDrawable)) {
                    h = var5.getIntrinsicWidth();
                    w = this.mIndeterminateDrawable.getIntrinsicHeight();
                    float var6 = (float)h / (float)w;
                    float var7 = (float)var3;
                    float var8 = (float)var4;
                    float var9 = var7 / var8;
                    if ((double)Math.abs(var6 - var9) < 1.0E-7D) {
                        if (var9 > var6) {
                            var10 = (int)(var8 * var6);
                            h = (var3 - var10) / 2;
                            w = h;
                            h += var10;
                            var10 = 0;
                        } else {
                            var11 = (int)(var7 * (1.0F / var6));
                            var4 = (var4 - var11) / 2;
                            h = var3;
                            w = 0;
                            var10 = var4;
                            var4 += var11;
                        }
                        break label29;
                    }
                }

                h = var3;
                var10 = 0;
                w = var10;
            }

            if (this.mMirrorForRtl && ViewUtils.isLayoutRtl(this)) {
                var11 = var3 - w;
                w = var3 - h;
                h = var11;
            }

            this.mIndeterminateDrawable.setBounds(w, var10, h, var4);
            w = var4;
        }

        var5 = this.mProgressDrawable;
        if (var5 != null) {
            var5.setBounds(0, 0, h, w);
        }
    }
    // kang

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawTrack(canvas);
    }

    @SuppressLint("RestrictedApi")
    protected void drawTrack(Canvas canvas) {
        final Drawable d = mCurrentDrawable;
        if (d != null) {
            final int saveCount = canvas.save();

            if (mCurrentMode != MODE_VERTICAL && ViewUtils.isLayoutRtl(this) && mMirrorForRtl) {
                canvas.translate(getWidth() - getPaddingRight(), getPaddingTop());
                canvas.scale(-1.0f, 1.0f);
            } else {
                canvas.translate(getPaddingLeft(), getPaddingTop());
            }

            final long time = getDrawingTime();
            if (mHasAnimation) {
                mAnimation.getTransformation(time, mTransformation);
                final float scale = mTransformation.getAlpha();
                try {
                    mInDrawing = true;
                    d.setLevel((int) (scale * MAX_LEVEL));
                    mInDrawing = false;
                    ViewCompat.postInvalidateOnAnimation(this);
                } finally {
                    mInDrawing = false;
                }
                postInvalidateOnAnimation();
            }

            d.draw(canvas);
            canvas.restoreToCount(saveCount);

            if (mShouldStartAnimationDrawable && d instanceof Animatable) {
                ((Animatable) d).start();
                mShouldStartAnimationDrawable = false;
            }
        }
    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int dw = 0;
        int dh = 0;

        final Drawable d = mCurrentDrawable;
        if (d != null) {
            dw = Math.max(mMinWidth, Math.min(mMaxWidth, d.getIntrinsicWidth()));
            dh = Math.max(mMinHeight, Math.min(mMaxHeight, d.getIntrinsicHeight()));
        }

        updateDrawableState();

        dw += getPaddingLeft() + getPaddingRight();
        dh += getPaddingTop() + getPaddingBottom();

        final int measuredWidth = resolveSizeAndState(dw, widthMeasureSpec, 0);
        final int measuredHeight = resolveSizeAndState(dh, heightMeasureSpec, 0);
        initCirCleStrokeWidth(measuredWidth - getPaddingLeft() - getPaddingRight());
        if (mIsOneUI4 && mIndeterminate && mUseHorizontalProgress) {
            seslSetIndeterminateProgressDrawable(measuredWidth - getPaddingLeft() - getPaddingRight());
        }
        setMeasuredDimension(measuredWidth, measuredHeight);
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        updateDrawableState();
    }

    private void updateDrawableState() {
        final int[] state = getDrawableState();
        boolean changed = false;

        final Drawable progressDrawable = mProgressDrawable;
        if (progressDrawable != null && progressDrawable.isStateful()) {
            changed |= progressDrawable.setState(state);
        }

        final Drawable indeterminateDrawable = mIndeterminateDrawable;
        if (indeterminateDrawable != null && indeterminateDrawable.isStateful()) {
            changed |= indeterminateDrawable.setState(state);
        }

        if (changed) {
            invalidate();
        }
    }

    @Override
    public void drawableHotspotChanged(float x, float y) {
        super.drawableHotspotChanged(x, y);

        if (mProgressDrawable != null) {
            mProgressDrawable.setHotspot(x, y);
        }

        if (mIndeterminateDrawable != null) {
            mIndeterminateDrawable.setHotspot(x, y);
        }
    }

    static class SavedState extends BaseSavedState {
        int progress;
        int secondaryProgress;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            progress = in.readInt();
            secondaryProgress = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(progress);
            out.writeInt(secondaryProgress);
        }

        public static final @NonNull Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);

        ss.progress = mProgress;
        ss.secondaryProgress = mSecondaryProgress;

        return ss;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());

        setProgress(ss.progress);
        setSecondaryProgress(ss.secondaryProgress);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mIndeterminate) {
            startAnimation();
        }
        if (mRefreshData != null) {
            synchronized (this) {
                final int count = mRefreshData.size();
                for (int i = 0; i < count; i++) {
                    final RefreshData rd = mRefreshData.get(i);
                    doRefreshProgress(rd.id, rd.progress, rd.fromUser, true, rd.animate);
                    rd.recycle();
                }
                mRefreshData.clear();
            }
        }
        mAttached = true;
    }

    @Override
    protected void onDetachedFromWindow() {
        if (mIndeterminate) {
            stopAnimation();
        } else {
            mCircleAnimationCallback = null;
        }
        if (mRefreshProgressRunnable != null) {
            removeCallbacks(mRefreshProgressRunnable);
            mRefreshIsPosted = false;
        }
        if (mAccessibilityEventSender != null) {
            removeCallbacks(mAccessibilityEventSender);
        }
        super.onDetachedFromWindow();
        mAttached = false;
    }

    @Override
    public CharSequence getAccessibilityClassName() {
        return android.widget.ProgressBar.class.getName();
    }

    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setItemCount(mMax - mMin);
        event.setCurrentItemIndex(mProgress);
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);

        if (Build.VERSION.SDK_INT >= 19 && !isIndeterminate()) {
            AccessibilityNodeInfo.RangeInfo rangeInfo = AccessibilityNodeInfo.RangeInfo.obtain(AccessibilityNodeInfo.RangeInfo.RANGE_TYPE_INT, getMin(), getMax(), getProgress());
            info.setRangeInfo(rangeInfo);
        }
    }

    private void scheduleAccessibilityEventSender() {
        if (mAccessibilityEventSender == null) {
            mAccessibilityEventSender = new AccessibilityEventSender();
        } else {
            removeCallbacks(mAccessibilityEventSender);
        }
        postDelayed(mAccessibilityEventSender, TIMEOUT_SEND_ACCESSIBILITY_EVENT);
    }

    public boolean isAnimating() {
        return isIndeterminate() && getWindowVisibility() == VISIBLE && isShown();
    }

    private class AccessibilityEventSender implements Runnable {
        @Override
        public void run() {
            sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_SELECTED);
        }
    }

    private static class ProgressTintInfo {
        ColorStateList mIndeterminateTintList;
        PorterDuff.Mode mIndeterminateTintMode;
        boolean mHasIndeterminateTint;
        boolean mHasIndeterminateTintMode;

        ColorStateList mProgressTintList;
        PorterDuff.Mode mProgressTintMode;
        boolean mHasProgressTint;
        boolean mHasProgressTintMode;

        ColorStateList mProgressBackgroundTintList;
        PorterDuff.Mode mProgressBackgroundTintMode;
        boolean mHasProgressBackgroundTint;
        boolean mHasProgressBackgroundTintMode;

        ColorStateList mSecondaryProgressTintList;
        PorterDuff.Mode mSecondaryProgressTintMode;
        boolean mHasSecondaryProgressTint;
        boolean mHasSecondaryProgressTintMode;
    }

    public void setMode(@SeekBarMode int mode) {
        mCurrentMode = mode;

        switch (mode) {
            case MODE_VERTICAL:
                setProgressDrawableTiled(ContextCompat.getDrawable(getContext(), mIsOneUI4 ? R.drawable.sesl4_scrubber_progress_vertical : R.drawable.sesl_scrubber_progress_vertical));
                break;
            case MODE_SPLIT:
                setProgressDrawableTiled(ContextCompat.getDrawable(getContext(), mIsOneUI4 ? R.drawable.sesl4_split_seekbar_background_progress : R.drawable.sesl_split_seekbar_background_progress));
                break;
            case MODE_CIRCLE:
                initializeRoundCicleMode();
                break;
        }
    }

    protected void onSlidingRefresh(int level) {
        if (mCurrentDrawable != null) {
            final Drawable layer = mCurrentDrawable instanceof LayerDrawable ? ((LayerDrawable) mCurrentDrawable).findDrawableByLayerId(android.R.id.progress) : null;
            if (layer != null) {
                layer.setLevel(level);
            }
        }
    }

    @Override
    public int getPaddingLeft() {
        return SeslViewReflector.getField_mPaddingLeft(this);
    }

    @Override
    public int getPaddingRight() {
        return SeslViewReflector.getField_mPaddingRight(this);
    }

    private void initCirCleStrokeWidth(int size) {
        if (size == getResources().getDimensionPixelSize(R.dimen.sesl_progress_bar_size_small)) {
            mRoundStrokeWidth = getResources().getDimensionPixelSize(R.dimen.sesl_progress_circle_size_small_width);
            mCirclePadding = getResources().getDimensionPixelOffset(R.dimen.sesl_progress_circle_size_small_padding);
        } else if (size == getResources().getDimensionPixelSize(R.dimen.sesl_progress_bar_size_small_title)) {
            mRoundStrokeWidth = getResources().getDimensionPixelSize(R.dimen.sesl_progress_circle_size_small_title_width);
            mCirclePadding = getResources().getDimensionPixelOffset(R.dimen.sesl_progress_circle_size_small_title_padding);
        } else if (size == getResources().getDimensionPixelSize(R.dimen.sesl_progress_bar_size_large)) {
            mRoundStrokeWidth = getResources().getDimensionPixelSize(R.dimen.sesl_progress_circle_size_large_width);
            mCirclePadding = getResources().getDimensionPixelOffset(R.dimen.sesl_progress_circle_size_large_padding);
        } else if (size == getResources().getDimensionPixelSize(R.dimen.sesl_progress_bar_size_xlarge)) {
            mRoundStrokeWidth = getResources().getDimensionPixelSize(R.dimen.sesl_progress_circle_size_xlarge_width);
            mCirclePadding = getResources().getDimensionPixelOffset(R.dimen.sesl_progress_circle_size_xlarge_padding);
        } else {
            mRoundStrokeWidth = (getResources().getDimensionPixelSize(R.dimen.sesl_progress_circle_size_small_width) * size) / getResources().getDimensionPixelSize(R.dimen.sesl_progress_bar_size_small);
            mCirclePadding = (size * getResources().getDimensionPixelOffset(R.dimen.sesl_progress_circle_size_small_padding)) / getResources().getDimensionPixelSize(R.dimen.sesl_progress_bar_size_small);
        }
    }

    private void seslSetIndeterminateProgressDrawable(int size) {
        if (getResources().getDimensionPixelSize(R.dimen.sesl_progress_bar_indeterminate_xsmall) >= size) {
            setIndeterminateDrawable(mIndeterminateHorizontalXsmall);
        } else if (getResources().getDimensionPixelSize(R.dimen.sesl_progress_bar_indeterminate_small) >= size) {
            setIndeterminateDrawable(mIndeterminateHorizontalSmall);
        } else if (getResources().getDimensionPixelSize(R.dimen.sesl_progress_bar_indeterminate_medium) >= size) {
            setIndeterminateDrawable(mIndeterminateHorizontalMedium);
        } else if (getResources().getDimensionPixelSize(R.dimen.sesl_progress_bar_indeterminate_large) >= size) {
            setIndeterminateDrawable(mIndeterminateHorizontalLarge);
        } else {
            setIndeterminateDrawable(mIndeterminateHorizontalXlarge);
        }
    }

    private static class StateListDrawableCompat {
        private static final boolean IS_BASE_SDK_VERSION = Build.VERSION.SDK_INT <= 23;

        static int getStateCount(StateListDrawable drawable) {
            if (!IS_BASE_SDK_VERSION) {
                return 0;
            }
            SeslStateListDrawableReflector.getStateCount(drawable);
            return 0;
        }

        static Drawable getStateDrawable(StateListDrawable drawable, int index) {
            if (IS_BASE_SDK_VERSION) {
                return SeslStateListDrawableReflector.getStateDrawable(drawable, index);
            }
            return null;
        }

        static int[] getStateSet(StateListDrawable drawable, int index) {
            if (IS_BASE_SDK_VERSION) {
                return SeslStateListDrawableReflector.getStateSet(drawable, index);
            }
            return null;
        }
    }

    private static class CircleAnimationCallback extends Animatable2Compat.AnimationCallback {
        final Handler mHandler = new Handler();
        private WeakReference<ProgressBar> mProgressBar;

        public CircleAnimationCallback(ProgressBar progressBar) {
            mProgressBar = new WeakReference<>(progressBar);
        }

        @Override
        public void onAnimationEnd(Drawable drawable) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    ProgressBar progressBar = (ProgressBar) mProgressBar.get();
                    if (progressBar != null) {
                        ((AnimatedVectorDrawable) progressBar.mIndeterminateDrawable).start();
                    }
                }
            });
        }
    }

    private ColorStateList colorToColorStateList(int color) {
        return new ColorStateList(new int[][]{new int[0]}, new int[]{color});
    }

    private void initializeRoundCicleMode() {
        mOnlyIndeterminate = false;
        setIndeterminate(false);

        TypedValue value = new TypedValue();
        getContext().getTheme().resolveAttribute(R.attr.colorPrimary, value, true);

        LayerDrawable d = new LayerDrawable(new Drawable[]{new CirCleProgressDrawable(true, colorToColorStateList(getResources().getColor(R.color.sesl_progress_control_color_background))),
                new CirCleProgressDrawable(false, colorToColorStateList(value.data))});
        d.setPaddingMode(LayerDrawable.PADDING_MODE_STACK);
        d.setId(0, android.R.id.background);
        d.setId(1, android.R.id.progress);
        setProgressDrawable(d);
    }

    private class CirCleProgressDrawable extends Drawable {
        int mColor;
        ColorStateList mColorStateList;
        private boolean mIsBackground;
        private final Paint mPaint;
        int mAlpha = 255;
        private RectF mArcRect = new RectF();
        private final ProgressState mState = new ProgressState();
        private final IntProperty<CirCleProgressDrawable> VISUAL_CIRCLE_PROGRESS = new IntProperty<CirCleProgressDrawable>("visual_progress") {
            public void setValue(CirCleProgressDrawable d, int value) {
                mProgress = value;
                invalidateSelf();
            }

            public Integer get(CirCleProgressDrawable d) {
                return Integer.valueOf(d.mProgress);
            }
        };
        public int mProgress = 0;

        private int modulateAlpha(int paintAlpha, int alpha) {
            int scale = alpha + (alpha >>> 7);
            return (paintAlpha * scale) >>> 8;
        }

        @Override
        public boolean isStateful() {
            return true;
        }

        public CirCleProgressDrawable(boolean isBackground, ColorStateList colorStateList) {
            mPaint = new Paint();
            mIsBackground = isBackground;
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeCap(Paint.Cap.ROUND);
            mColorStateList = colorStateList;
            mColor = colorStateList.getDefaultColor();
            mPaint.setColor(mColor);
        }

        @Override
        public void draw(Canvas canvas) {
            mPaint.setStrokeWidth(mRoundStrokeWidth);
            int prevAlpha = mPaint.getAlpha();
            mPaint.setAlpha(modulateAlpha(prevAlpha, mAlpha));
            mPaint.setAntiAlias(true);
            mArcRect.set((((float) mRoundStrokeWidth) / 2.0f) + ((float) mCirclePadding),
                    (((float) mRoundStrokeWidth) / 2.0f) + ((float) mCirclePadding),
                    (((float) ProgressBar.this.getWidth()) - (((float) mRoundStrokeWidth) / 2.0f)) - ((float) mCirclePadding),
                    (((float) ProgressBar.this.getWidth()) - (((float) mRoundStrokeWidth) / 2.0f)) - ((float) mCirclePadding));
            int range = mMax - mMin;
            float angle = range > 0 ? ((float) (mProgress - mMin)) / ((float) range) : 0.0f;
            canvas.save();
            if (mIsBackground) {
                canvas.drawArc(mArcRect, 270.0f, 360.0f, false, mPaint);
            } else {
                canvas.drawArc(mArcRect, 270.0f, angle * 360.0f, false, mPaint);
            }
            canvas.restore();
            mPaint.setAlpha(prevAlpha);
        }

        public void setProgress(int progress, boolean animate) {
            if (animate) {
                ObjectAnimator ofInt = ObjectAnimator.ofInt(this, VISUAL_CIRCLE_PROGRESS, progress);
                if (Build.VERSION.SDK_INT > 18) {
                    ofInt.setAutoCancel(true);
                }
                ofInt.setDuration(PROGRESS_ANIM_DURATION);
                ofInt.setInterpolator(PROGRESS_ANIM_INTERPOLATOR);
                ofInt.start();
            } else {
                mProgress = progress;
                ProgressBar.this.invalidate();
            }
        }

        @Override
        public void setAlpha(int alpha) {
            mAlpha = alpha;
            invalidateSelf();
        }

        @Override
        public void setColorFilter(ColorFilter colorFilter) {
            mPaint.setColorFilter(colorFilter);
            invalidateSelf();
        }

        @Override
        public int getOpacity() {
            if (mPaint.getXfermode() != null) {
                return PixelFormat.TRANSLUCENT;
            }
            if (mPaint.getAlpha() == 0) {
                return PixelFormat.TRANSPARENT;
            }
            if (mPaint.getAlpha() == 255) {
                return PixelFormat.OPAQUE;
            } else {
                return PixelFormat.TRANSLUCENT;
            }
        }

        @Override
        public void setTintList(ColorStateList tint) {
            super.setTintList(tint);
            if (tint != null) {
                mColorStateList = tint;
                mColor = tint.getDefaultColor();
                mPaint.setColor(mColor);
                invalidateSelf();
            }
        }

        @Override
        protected boolean onStateChange(int[] state) {
            boolean onStateChange = super.onStateChange(state);
            int color = mColorStateList.getColorForState(state, mColor);
            if (mColor != color) {
                mColor = color;
                mPaint.setColor(color);
                invalidateSelf();
            }
            return onStateChange;
        }

        @Override
        public Drawable.ConstantState getConstantState() {
            return mState;
        }

        private class ProgressState extends Drawable.ConstantState {
            @Override
            public int getChangingConfigurations() {
                return 0;
            }

            @Override
            public Drawable newDrawable() {
                return CirCleProgressDrawable.this;
            }
        }
    }

    /*kang from MathUtils.smali*/
    private int constrain(int amount, int low, int high) {
        return amount < low ? low : (amount > high ? high : amount);
    }
}
