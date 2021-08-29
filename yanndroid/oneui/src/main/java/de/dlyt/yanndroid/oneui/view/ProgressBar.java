package de.dlyt.yanndroid.oneui.view;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.FloatProperty;
import android.view.View;
import android.view.ViewDebug;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;
import android.widget.RemoteViews;

import androidx.Styleable;
import androidx.annotation.InterpolatorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.annotation.RestrictTo;
import androidx.appcompat.widget.DrawableUtils;
import androidx.appcompat.widget.ViewUtils;
import androidx.core.app.NotificationCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.util.Pools;
import androidx.core.view.ViewCompat;
import androidx.reflect.graphics.drawable.SeslStateListDrawableReflector;
import androidx.reflect.view.SeslViewReflector;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;

import de.dlyt.yanndroid.oneui.R;

@RemoteViews.RemoteView
public class ProgressBar extends View {
    public static final int MODE_DUAL_COLOR = 2;
    public static final int MODE_EXPAND = 5;
    public static final int MODE_EXPAND_VERTICAL = 6;
    public static final int MODE_SPLIT = 4;
    public static final int MODE_VERTICAL = 3;
    public static final int MODE_WARNING = 1;
    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP_PREFIX})
    protected static final int MODE_STANDARD = 0;
    private static final int MAX_LEVEL = 10000;
    private static final int PROGRESS_ANIM_DURATION = 80;
    private static final DecelerateInterpolator PROGRESS_ANIM_INTERPOLATOR = new DecelerateInterpolator();
    private static final int TIMEOUT_SEND_ACCESSIBILITY_EVENT = 200;
    private final FloatProperty<ProgressBar> VISUAL_PROGRESS;
    private final ArrayList<RefreshData> mRefreshData;
    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP_PREFIX})
    public
    int mMaxHeight;
    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP_PREFIX})
    public
    int mMaxWidth;
    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP_PREFIX})
    public
    int mMinHeight;
    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP_PREFIX})
    public
    int mMinWidth;
    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP_PREFIX})
    public
    boolean mMirrorForRtl;
    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP_PREFIX})
    protected int mCurrentMode;
    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP_PREFIX})
    protected float mDensity;
    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP_PREFIX})
    int mSampleWidth;
    private AccessibilityEventSender mAccessibilityEventSender;
    private boolean mAggregatedIsVisible;
    private AlphaAnimation mAnimation;
    private boolean mAttached;
    private int mBehavior;
    private Drawable mCurrentDrawable;
    private int mDuration;
    private boolean mHasAnimation;
    private boolean mInDrawing;
    private boolean mIndeterminate;
    private Drawable mIndeterminateDrawable;
    private Interpolator mInterpolator;
    private int mMax;
    private boolean mMaxInitialized;
    private int mMin;
    private boolean mMinInitialized;
    private boolean mNoInvalidate;
    private boolean mOnlyIndeterminate;
    private int mProgress;
    private Drawable mProgressDrawable;
    private ProgressTintInfo mProgressTintInfo;
    private boolean mRefreshIsPosted;
    private RefreshProgressRunnable mRefreshProgressRunnable;
    private int mSecondaryProgress;
    private boolean mShouldStartAnimationDrawable;
    private Transformation mTransformation;
    private long mUiThreadId;
    private float mVisualProgress;

    public ProgressBar(Context context) {
        this(context, null);
    }

    public ProgressBar(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 16842871);
    }

    public ProgressBar(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    @SuppressLint("RestrictedApi")
    public ProgressBar(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        boolean z = false;
        this.mCurrentMode = 0;
        this.mSampleWidth = 0;
        this.mMirrorForRtl = false;
        this.mRefreshData = new ArrayList<>();
        this.VISUAL_PROGRESS = new FloatProperty<ProgressBar>("visual_progress") {
            /* class de.dlyt.yanndroid.samsung.SeslProgressBar.AnonymousClass1 */

            public Float get(ProgressBar seslProgressBar) {
                return Float.valueOf(seslProgressBar.mVisualProgress);
            }

            public void setValue(ProgressBar seslProgressBar, float f) {
                seslProgressBar.setVisualProgress(R.id.progress, f);
                seslProgressBar.mVisualProgress = f;
            }
        };
        this.mUiThreadId = Thread.currentThread().getId();
        initProgressBar();
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, Styleable.styleable.ProgressBar, i, i2);
        if (Build.VERSION.SDK_INT >= 29) {
            saveAttributeDataForStyleable(context, Styleable.styleable.ProgressBar, attributeSet, obtainStyledAttributes, i, i2);
        }
        this.mNoInvalidate = true;
        Drawable drawable = obtainStyledAttributes.getDrawable(Styleable.styleable.ProgressBar_android_progressDrawable);
        if (drawable != null) {
            if (needsTileify(drawable)) {
                setProgressDrawableTiled(drawable);
            } else {
                setProgressDrawable(drawable);
            }
        }
        this.mDuration = obtainStyledAttributes.getInt(Styleable.styleable.ProgressBar_android_indeterminateDuration, this.mDuration);
        this.mMinWidth = obtainStyledAttributes.getDimensionPixelSize(Styleable.styleable.ProgressBar_android_minWidth, this.mMinWidth);
        this.mMaxWidth = obtainStyledAttributes.getDimensionPixelSize(Styleable.styleable.ProgressBar_android_maxWidth, this.mMaxWidth);
        this.mMinHeight = obtainStyledAttributes.getDimensionPixelSize(Styleable.styleable.ProgressBar_android_minHeight, this.mMinHeight);
        this.mMaxHeight = obtainStyledAttributes.getDimensionPixelSize(Styleable.styleable.ProgressBar_android_maxHeight, this.mMaxHeight);
        this.mBehavior = obtainStyledAttributes.getInt(Styleable.styleable.ProgressBar_android_indeterminateBehavior, this.mBehavior);
        int resourceId = obtainStyledAttributes.getResourceId(Styleable.styleable.ProgressBar_android_interpolator, 17432587);
        if (resourceId > 0) {
            setInterpolator(context, resourceId);
        }
        setMin(obtainStyledAttributes.getInt(Styleable.styleable.ProgressBar_android_min, this.mMin));
        setMax(obtainStyledAttributes.getInt(Styleable.styleable.ProgressBar_android_max, this.mMax));
        setProgress(obtainStyledAttributes.getInt(Styleable.styleable.ProgressBar_android_progress, this.mProgress));
        setSecondaryProgress(obtainStyledAttributes.getInt(Styleable.styleable.ProgressBar_android_secondaryProgress, this.mSecondaryProgress));
        Drawable drawable2 = obtainStyledAttributes.getDrawable(Styleable.styleable.ProgressBar_android_indeterminateDrawable);
        if (drawable2 != null) {
            if (needsTileify(drawable2)) {
                setIndeterminateDrawableTiled(drawable2);
            } else {
                setIndeterminateDrawable(drawable2);
            }
        }
        boolean z2 = obtainStyledAttributes.getBoolean(Styleable.styleable.ProgressBar_android_indeterminateOnly, this.mOnlyIndeterminate);
        this.mOnlyIndeterminate = z2;
        this.mNoInvalidate = false;
        setIndeterminate((z2 || obtainStyledAttributes.getBoolean(Styleable.styleable.ProgressBar_android_indeterminate, this.mIndeterminate)) ? true : z);
        this.mMirrorForRtl = obtainStyledAttributes.getBoolean(Styleable.styleable.ProgressBar_android_mirrorForRtl, this.mMirrorForRtl);
        if (obtainStyledAttributes.hasValue(Styleable.styleable.ProgressBar_android_progressTintMode)) {
            if (this.mProgressTintInfo == null) {
                this.mProgressTintInfo = new ProgressTintInfo();
            }
            this.mProgressTintInfo.mProgressTintMode = DrawableUtils.parseTintMode(obtainStyledAttributes.getInt(Styleable.styleable.ProgressBar_android_progressTintMode, -1), null);
            this.mProgressTintInfo.mHasProgressTintMode = true;
        }
        if (obtainStyledAttributes.hasValue(Styleable.styleable.ProgressBar_android_progressTint)) {
            if (this.mProgressTintInfo == null) {
                this.mProgressTintInfo = new ProgressTintInfo();
            }
            this.mProgressTintInfo.mProgressTintList = obtainStyledAttributes.getColorStateList(Styleable.styleable.ProgressBar_android_progressTint);
            this.mProgressTintInfo.mHasProgressTint = true;
        }
        if (obtainStyledAttributes.hasValue(Styleable.styleable.ProgressBar_android_progressBackgroundTintMode)) {
            if (this.mProgressTintInfo == null) {
                this.mProgressTintInfo = new ProgressTintInfo();
            }
            this.mProgressTintInfo.mProgressBackgroundTintMode = DrawableUtils.parseTintMode(obtainStyledAttributes.getInt(Styleable.styleable.ProgressBar_android_progressBackgroundTintMode, -1), null);
            this.mProgressTintInfo.mHasProgressBackgroundTintMode = true;
        }
        if (obtainStyledAttributes.hasValue(Styleable.styleable.ProgressBar_android_progressBackgroundTint)) {
            if (this.mProgressTintInfo == null) {
                this.mProgressTintInfo = new ProgressTintInfo();
            }
            this.mProgressTintInfo.mProgressBackgroundTintList = obtainStyledAttributes.getColorStateList(Styleable.styleable.ProgressBar_android_progressBackgroundTint);
            this.mProgressTintInfo.mHasProgressBackgroundTint = true;
        }
        if (obtainStyledAttributes.hasValue(Styleable.styleable.ProgressBar_android_secondaryProgressTintMode)) {
            if (this.mProgressTintInfo == null) {
                this.mProgressTintInfo = new ProgressTintInfo();
            }
            this.mProgressTintInfo.mSecondaryProgressTintMode = DrawableUtils.parseTintMode(obtainStyledAttributes.getInt(Styleable.styleable.ProgressBar_android_secondaryProgressTintMode, -1), null);
            this.mProgressTintInfo.mHasSecondaryProgressTintMode = true;
        }
        if (obtainStyledAttributes.hasValue(Styleable.styleable.ProgressBar_android_secondaryProgressTint)) {
            if (this.mProgressTintInfo == null) {
                this.mProgressTintInfo = new ProgressTintInfo();
            }
            this.mProgressTintInfo.mSecondaryProgressTintList = obtainStyledAttributes.getColorStateList(Styleable.styleable.ProgressBar_android_secondaryProgressTint);
            this.mProgressTintInfo.mHasSecondaryProgressTint = true;
        }
        if (obtainStyledAttributes.hasValue(Styleable.styleable.ProgressBar_android_indeterminateTintMode)) {
            if (this.mProgressTintInfo == null) {
                this.mProgressTintInfo = new ProgressTintInfo();
            }
            this.mProgressTintInfo.mIndeterminateTintMode = DrawableUtils.parseTintMode(obtainStyledAttributes.getInt(Styleable.styleable.ProgressBar_android_indeterminateTintMode, -1), null);
            this.mProgressTintInfo.mHasIndeterminateTintMode = true;
        }
        if (obtainStyledAttributes.hasValue(Styleable.styleable.ProgressBar_android_indeterminateTint)) {
            if (this.mProgressTintInfo == null) {
                this.mProgressTintInfo = new ProgressTintInfo();
            }
            this.mProgressTintInfo.mIndeterminateTintList = obtainStyledAttributes.getColorStateList(Styleable.styleable.ProgressBar_android_indeterminateTint);
            this.mProgressTintInfo.mHasIndeterminateTint = true;
        }
        obtainStyledAttributes.recycle();
        applyProgressTints();
        applyIndeterminateTint();
        if (ViewCompat.getImportantForAccessibility(this) == ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_AUTO) {
            ViewCompat.setImportantForAccessibility(this, ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_YES);
        }
        this.mDensity = context.getResources().getDisplayMetrics().density;
    }

    private static boolean needsTileify(Drawable drawable) {
        if (drawable instanceof LayerDrawable) {
            LayerDrawable layerDrawable = (LayerDrawable) drawable;
            int numberOfLayers = layerDrawable.getNumberOfLayers();
            for (int i = 0; i < numberOfLayers; i++) {
                if (needsTileify(layerDrawable.getDrawable(i))) {
                    return true;
                }
            }
            return false;
        } else if (!(drawable instanceof StateListDrawable)) {
            return drawable instanceof BitmapDrawable;
        } else {
            StateListDrawable stateListDrawable = (StateListDrawable) drawable;
            int stateCount = StateListDrawableCompat.getStateCount(stateListDrawable);
            for (int i2 = 0; i2 < stateCount; i2++) {
                Drawable stateDrawable = StateListDrawableCompat.getStateDrawable(stateListDrawable, i2);
                if (stateDrawable != null && needsTileify(stateDrawable)) {
                    return true;
                }
            }
            return false;
        }
    }

    public static int MathUtilsdotconstrain(int i, int i2, int i3) {
        return i < i2 ? i2 : i > i3 ? i3 : i;
    }

    private void applyIndeterminateTint() {
        ProgressTintInfo progressTintInfo;
        if (this.mIndeterminateDrawable != null && (progressTintInfo = this.mProgressTintInfo) != null) {
            if (progressTintInfo.mHasIndeterminateTint || progressTintInfo.mHasIndeterminateTintMode) {
                Drawable mutate = this.mIndeterminateDrawable.mutate();
                this.mIndeterminateDrawable = mutate;
                if (progressTintInfo.mHasIndeterminateTint) {
                    DrawableCompat.setTintList(mutate, progressTintInfo.mIndeterminateTintList);
                }
                if (progressTintInfo.mHasIndeterminateTintMode) {
                    DrawableCompat.setTintMode(this.mIndeterminateDrawable, progressTintInfo.mIndeterminateTintMode);
                }
                if (this.mIndeterminateDrawable.isStateful()) {
                    this.mIndeterminateDrawable.setState(getDrawableState());
                }
            }
        }
    }

    private void applyPrimaryProgressTint() {
        Drawable tintTarget;
        ProgressTintInfo progressTintInfo = this.mProgressTintInfo;
        if ((progressTintInfo.mHasProgressTint || progressTintInfo.mHasProgressTintMode) && (tintTarget = getTintTarget(R.id.progress, true)) != null) {
            ProgressTintInfo progressTintInfo2 = this.mProgressTintInfo;
            if (progressTintInfo2.mHasProgressTint) {
                DrawableCompat.setTintList(tintTarget, progressTintInfo2.mProgressTintList);
            }
            ProgressTintInfo progressTintInfo3 = this.mProgressTintInfo;
            if (progressTintInfo3.mHasProgressTintMode) {
                DrawableCompat.setTintMode(tintTarget, progressTintInfo3.mProgressTintMode);
            }
            if (tintTarget.isStateful()) {
                tintTarget.setState(getDrawableState());
            }
        }
    }

    private void applyProgressBackgroundTint() {
        Drawable tintTarget;
        ProgressTintInfo progressTintInfo = this.mProgressTintInfo;
        if ((progressTintInfo.mHasProgressBackgroundTint || progressTintInfo.mHasProgressBackgroundTintMode) && (tintTarget = getTintTarget(R.id.background, false)) != null) {
            ProgressTintInfo progressTintInfo2 = this.mProgressTintInfo;
            if (progressTintInfo2.mHasProgressBackgroundTint) {
                DrawableCompat.setTintList(tintTarget, progressTintInfo2.mProgressBackgroundTintList);
            }
            ProgressTintInfo progressTintInfo3 = this.mProgressTintInfo;
            if (progressTintInfo3.mHasProgressBackgroundTintMode) {
                DrawableCompat.setTintMode(tintTarget, progressTintInfo3.mProgressBackgroundTintMode);
            }
            if (tintTarget.isStateful()) {
                tintTarget.setState(getDrawableState());
            }
        }
    }

    private void applyProgressTints() {
        if (this.mProgressDrawable != null && this.mProgressTintInfo != null) {
            applyPrimaryProgressTint();
            applyProgressBackgroundTint();
            applySecondaryProgressTint();
        }
    }

    private void applySecondaryProgressTint() {
        Drawable tintTarget;
        ProgressTintInfo progressTintInfo = this.mProgressTintInfo;
        if ((progressTintInfo.mHasSecondaryProgressTint || progressTintInfo.mHasSecondaryProgressTintMode) && (tintTarget = getTintTarget(R.id.secondaryProgress, false)) != null) {
            ProgressTintInfo progressTintInfo2 = this.mProgressTintInfo;
            if (progressTintInfo2.mHasSecondaryProgressTint) {
                DrawableCompat.setTintList(tintTarget, progressTintInfo2.mSecondaryProgressTintList);
            }
            ProgressTintInfo progressTintInfo3 = this.mProgressTintInfo;
            if (progressTintInfo3.mHasSecondaryProgressTintMode) {
                DrawableCompat.setTintMode(tintTarget, progressTintInfo3.mSecondaryProgressTintMode);
            }
            if (tintTarget.isStateful()) {
                tintTarget.setState(getDrawableState());
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private synchronized void doRefreshProgress(int i, int i2, boolean z, boolean z2, boolean z3) {
        int i3 = this.mMax - this.mMin;
        float f = i3 > 0 ? ((float) (i2 - this.mMin)) / ((float) i3) : 0.0f;
        boolean z4 = i == R.id.progress;
        Drawable drawable = this.mCurrentDrawable;
        if (drawable != null) {
            int i4 = (int) (10000.0f * f);
            if (drawable instanceof LayerDrawable) {
                Drawable findDrawableByLayerId = ((LayerDrawable) drawable).findDrawableByLayerId(i);
                if (findDrawableByLayerId != null && Build.VERSION.SDK_INT > 19 && canResolveLayoutDirection()) {
                    DrawableCompat.setLayoutDirection(findDrawableByLayerId, ViewCompat.getLayoutDirection(this));
                }
                if (findDrawableByLayerId != null) {
                    drawable = findDrawableByLayerId;
                }
            } else if (drawable instanceof StateListDrawable) {
                int stateCount = StateListDrawableCompat.getStateCount((StateListDrawable) drawable);
                for (int i5 = 0; i5 < stateCount; i5++) {
                    Drawable stateDrawable = StateListDrawableCompat.getStateDrawable((StateListDrawable) drawable, i5);
                    Drawable drawable2 = null;
                    if (stateDrawable != null) {
                        if ((stateDrawable instanceof LayerDrawable) && (drawable2 = ((LayerDrawable) stateDrawable).findDrawableByLayerId(i)) != null && Build.VERSION.SDK_INT > 19 && canResolveLayoutDirection()) {
                            DrawableCompat.setLayoutDirection(drawable2, ViewCompat.getLayoutDirection(this));
                        }
                        if (drawable2 == null) {
                            drawable2 = drawable;
                        }
                        drawable2.setLevel(i4);
                    } else {
                        return;
                    }
                }
            }
            drawable.setLevel(i4);
        } else {
            invalidate();
        }
        if (!z4 || !z3) {
            setVisualProgress(i, f);
        } else {
            ObjectAnimator ofFloat = ObjectAnimator.ofFloat(this, this.VISUAL_PROGRESS, f);
            if (Build.VERSION.SDK_INT > 18) {
                ofFloat.setAutoCancel(true);
            }
            ofFloat.setDuration(80L);
            ofFloat.setInterpolator(PROGRESS_ANIM_INTERPOLATOR);
            ofFloat.start();
        }
        if (z4 && z2) {
            onProgressRefresh(f, z, i2);
        }
    }

    @Nullable
    private Drawable getTintTarget(int i, boolean z) {
        Drawable drawable = this.mProgressDrawable;
        Drawable drawable2 = null;
        if (drawable != null) {
            this.mProgressDrawable = drawable.mutate();
            if (drawable instanceof LayerDrawable) {
                drawable2 = ((LayerDrawable) drawable).findDrawableByLayerId(i);
            }
            if (z && drawable2 == null) {
                return drawable;
            }
        }
        return drawable2;
    }

    private void initProgressBar() {
        this.mMin = 0;
        this.mMax = 100;
        this.mProgress = 0;
        this.mSecondaryProgress = 0;
        this.mIndeterminate = false;
        this.mOnlyIndeterminate = false;
        this.mDuration = 4000;
        this.mBehavior = 1;
        this.mMinWidth = 24;
        this.mMaxWidth = 48;
        this.mMinHeight = 24;
        this.mMaxHeight = 48;
    }

    private synchronized void refreshProgress(int i, int i2, boolean z, boolean z2) {
        if (this.mUiThreadId == Thread.currentThread().getId()) {
            doRefreshProgress(i, i2, z, true, z2);
        } else {
            if (this.mRefreshProgressRunnable == null) {
                this.mRefreshProgressRunnable = new RefreshProgressRunnable();
            }
            this.mRefreshData.add(RefreshData.obtain(i, i2, z, z2));
            if (this.mAttached && !this.mRefreshIsPosted) {
                post(this.mRefreshProgressRunnable);
                this.mRefreshIsPosted = true;
            }
        }
    }

    private void scheduleAccessibilityEventSender() {
        AccessibilityEventSender accessibilityEventSender = this.mAccessibilityEventSender;
        if (accessibilityEventSender == null) {
            this.mAccessibilityEventSender = new AccessibilityEventSender();
        } else {
            removeCallbacks(accessibilityEventSender);
        }
        postDelayed(this.mAccessibilityEventSender, 200);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setVisualProgress(int i, float f) {
        this.mVisualProgress = f;
        Drawable drawable = this.mCurrentDrawable;
        if ((drawable instanceof LayerDrawable) && (drawable = ((LayerDrawable) drawable).findDrawableByLayerId(i)) == null) {
            drawable = this.mCurrentDrawable;
        }
        if (drawable != null) {
            drawable.setLevel((int) (10000.0f * f));
        } else {
            invalidate();
        }
        onVisualProgressChanged(i, f);
    }

    private void startAnimation() {
        if (getVisibility() != VISIBLE) {
            return;
        }
        if (Build.VERSION.SDK_INT > 23 || getWindowVisibility() == VISIBLE) {
            if (this.mIndeterminateDrawable instanceof Animatable) {
                this.mShouldStartAnimationDrawable = true;
                this.mHasAnimation = false;
            } else {
                this.mHasAnimation = true;
                if (this.mInterpolator == null) {
                    this.mInterpolator = new LinearInterpolator();
                }
                Transformation transformation = this.mTransformation;
                if (transformation == null) {
                    this.mTransformation = new Transformation();
                } else {
                    transformation.clear();
                }
                AlphaAnimation alphaAnimation = this.mAnimation;
                if (alphaAnimation == null) {
                    this.mAnimation = new AlphaAnimation(0.0f, 1.0f);
                } else {
                    alphaAnimation.reset();
                }
                this.mAnimation.setRepeatMode(this.mBehavior);
                this.mAnimation.setRepeatCount(-1);
                this.mAnimation.setDuration((long) this.mDuration);
                this.mAnimation.setInterpolator(this.mInterpolator);
                this.mAnimation.setStartTime(-1);
            }
            postInvalidate();
        }
    }

    private void stopAnimation() {
        this.mHasAnimation = false;
        Drawable drawable = this.mIndeterminateDrawable;
        if (drawable instanceof Animatable) {
            ((Animatable) drawable).stop();
            this.mShouldStartAnimationDrawable = false;
        }
        postInvalidate();
    }

    private void swapCurrentDrawable(Drawable drawable) {
        Drawable drawable2 = this.mCurrentDrawable;
        this.mCurrentDrawable = drawable;
        if (drawable2 != drawable) {
            if (drawable2 != null) {
                drawable2.setVisible(false, false);
            }
            Drawable drawable3 = this.mCurrentDrawable;
            if (drawable3 != null) {
                drawable3.setVisible(getWindowVisibility() == VISIBLE && isShown(), false);
            }
        }
    }

    private Drawable tileify(Drawable drawable, boolean z) {
        int i = 0;
        if (drawable instanceof LayerDrawable) {
            LayerDrawable layerDrawable = (LayerDrawable) drawable;
            int numberOfLayers = layerDrawable.getNumberOfLayers();
            Drawable[] drawableArr = new Drawable[numberOfLayers];
            for (int i2 = 0; i2 < numberOfLayers; i2++) {
                int id = layerDrawable.getId(i2);
                drawableArr[i2] = tileify(layerDrawable.getDrawable(i2), id == R.id.progress || id == R.id.secondaryProgress);
            }
            LayerDrawable layerDrawable2 = new LayerDrawable(drawableArr);
            if (Build.VERSION.SDK_INT >= 23) {
                while (i < numberOfLayers) {
                    layerDrawable2.setId(i, layerDrawable.getId(i));
                    layerDrawable2.setLayerGravity(i, layerDrawable.getLayerGravity(i));
                    layerDrawable2.setLayerWidth(i, layerDrawable.getLayerWidth(i));
                    layerDrawable2.setLayerHeight(i, layerDrawable.getLayerHeight(i));
                    layerDrawable2.setLayerInsetLeft(i, layerDrawable.getLayerInsetLeft(i));
                    layerDrawable2.setLayerInsetRight(i, layerDrawable.getLayerInsetRight(i));
                    layerDrawable2.setLayerInsetTop(i, layerDrawable.getLayerInsetTop(i));
                    layerDrawable2.setLayerInsetBottom(i, layerDrawable.getLayerInsetBottom(i));
                    layerDrawable2.setLayerInsetStart(i, layerDrawable.getLayerInsetStart(i));
                    layerDrawable2.setLayerInsetEnd(i, layerDrawable.getLayerInsetEnd(i));
                    i++;
                }
            }
            return layerDrawable2;
        } else if (drawable instanceof StateListDrawable) {
            StateListDrawable stateListDrawable = (StateListDrawable) drawable;
            StateListDrawable stateListDrawable2 = new StateListDrawable();
            int stateCount = StateListDrawableCompat.getStateCount(stateListDrawable);
            while (i < stateCount) {
                int[] stateSet = StateListDrawableCompat.getStateSet(stateListDrawable, i);
                Drawable stateDrawable = StateListDrawableCompat.getStateDrawable(stateListDrawable, i);
                if (stateDrawable != null) {
                    stateListDrawable2.addState(stateSet, tileify(stateDrawable, z));
                }
                i++;
            }
            return stateListDrawable2;
        } else {
            if (drawable instanceof BitmapDrawable) {
                drawable = (BitmapDrawable) drawable.getConstantState().newDrawable(getResources());
                ((BitmapDrawable) drawable).setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.CLAMP);
                if (this.mSampleWidth <= 0) {
                    this.mSampleWidth = drawable.getIntrinsicWidth();
                }
                if (z) {
                    return new ClipDrawable(drawable, 3, 1);
                }
            }
            return drawable;
        }
    }

    private Drawable tileifyIndeterminate(Drawable drawable) {
        if (!(drawable instanceof AnimationDrawable)) {
            return drawable;
        }
        AnimationDrawable animationDrawable = (AnimationDrawable) drawable;
        int numberOfFrames = animationDrawable.getNumberOfFrames();
        AnimationDrawable animationDrawable2 = new AnimationDrawable();
        animationDrawable2.setOneShot(animationDrawable.isOneShot());
        for (int i = 0; i < numberOfFrames; i++) {
            Drawable tileify = tileify(animationDrawable.getFrame(i), true);
            tileify.setLevel(MAX_LEVEL);
            animationDrawable2.addFrame(tileify, animationDrawable.getDuration(i));
        }
        animationDrawable2.setLevel(MAX_LEVEL);
        return animationDrawable2;
    }

    private void updateDrawableState() {
        int[] drawableState = getDrawableState();
        Drawable drawable = this.mProgressDrawable;
        boolean z = false;
        if (drawable != null && drawable.isStateful()) {
            z = false | drawable.setState(drawableState);
        }
        Drawable drawable2 = this.mIndeterminateDrawable;
        if (drawable2 != null && drawable2.isStateful()) {
            z |= drawable2.setState(drawableState);
        }
        if (z) {
            invalidate();
        }
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: package-private */
    @SuppressLint("RestrictedApi")
    public void drawTrack(Canvas canvas) {
        Drawable drawable = this.mCurrentDrawable;
        if (drawable != null) {
            int save = canvas.save();
            if (this.mCurrentMode == 3 || !this.mMirrorForRtl || !ViewUtils.isLayoutRtl(this)) {
                canvas.translate((float) getPaddingLeft(), (float) getPaddingTop());
            } else {
                canvas.translate((float) (getWidth() - getPaddingRight()), (float) getPaddingTop());
                canvas.scale(-1.0f, 1.0f);
            }
            long drawingTime = getDrawingTime();
            if (this.mHasAnimation) {
                this.mAnimation.getTransformation(drawingTime, this.mTransformation);
                float alpha = this.mTransformation.getAlpha();
                try {
                    this.mInDrawing = true;
                    drawable.setLevel((int) (alpha * 10000.0f));
                    this.mInDrawing = false;
                    ViewCompat.postInvalidateOnAnimation(this);
                } catch (Throwable th) {
                    this.mInDrawing = false;
                    throw th;
                }
            }
            drawable.draw(canvas);
            canvas.restoreToCount(save);
            if (this.mShouldStartAnimationDrawable && (drawable instanceof Animatable)) {
                ((Animatable) drawable).start();
                this.mShouldStartAnimationDrawable = false;
            }
        }
    }

    public void drawableHotspotChanged(float f, float f2) {
        super.drawableHotspotChanged(f, f2);
        Drawable drawable = this.mProgressDrawable;
        if (drawable != null) {
            DrawableCompat.setHotspot(drawable, f, f2);
        }
        Drawable drawable2 = this.mIndeterminateDrawable;
        if (drawable2 != null) {
            DrawableCompat.setHotspot(drawable2, f, f2);
        }
    }

    /* access modifiers changed from: protected */
    public void drawableStateChanged() {
        super.drawableStateChanged();
        updateDrawableState();
    }

    public CharSequence getAccessibilityClassName() {
        return android.widget.ProgressBar.class.getName();
    }

    @Nullable
    public Drawable getCurrentDrawable() {
        return this.mCurrentDrawable;
    }

    public Drawable getIndeterminateDrawable() {
        return this.mIndeterminateDrawable;
    }

    public void setIndeterminateDrawable(Drawable drawable) {
        Drawable drawable2 = this.mIndeterminateDrawable;
        if (drawable2 != drawable) {
            if (drawable2 != null) {
                drawable2.setCallback(null);
                unscheduleDrawable(this.mIndeterminateDrawable);
            }
            this.mIndeterminateDrawable = drawable;
            if (drawable != null) {
                drawable.setCallback(this);
                DrawableCompat.setLayoutDirection(drawable, ViewCompat.getLayoutDirection(this));
                if (drawable.isStateful()) {
                    drawable.setState(getDrawableState());
                }
                applyIndeterminateTint();
            }
            if (this.mIndeterminate) {
                swapCurrentDrawable(drawable);
                postInvalidate();
            }
        }
    }

    @Nullable
    public ColorStateList getIndeterminateTintList() {
        ProgressTintInfo progressTintInfo = this.mProgressTintInfo;
        if (progressTintInfo != null) {
            return progressTintInfo.mIndeterminateTintList;
        }
        return null;
    }

    public void setIndeterminateTintList(@Nullable ColorStateList colorStateList) {
        if (this.mProgressTintInfo == null) {
            this.mProgressTintInfo = new ProgressTintInfo();
        }
        ProgressTintInfo progressTintInfo = this.mProgressTintInfo;
        progressTintInfo.mIndeterminateTintList = colorStateList;
        progressTintInfo.mHasIndeterminateTint = true;
        applyIndeterminateTint();
    }

    @Nullable
    public PorterDuff.Mode getIndeterminateTintMode() {
        ProgressTintInfo progressTintInfo = this.mProgressTintInfo;
        if (progressTintInfo != null) {
            return progressTintInfo.mIndeterminateTintMode;
        }
        return null;
    }

    public void setIndeterminateTintMode(@Nullable PorterDuff.Mode mode) {
        if (this.mProgressTintInfo == null) {
            this.mProgressTintInfo = new ProgressTintInfo();
        }
        ProgressTintInfo progressTintInfo = this.mProgressTintInfo;
        progressTintInfo.mIndeterminateTintMode = mode;
        progressTintInfo.mHasIndeterminateTintMode = true;
        applyIndeterminateTint();
    }

    public Interpolator getInterpolator() {
        return this.mInterpolator;
    }

    public void setInterpolator(Interpolator interpolator) {
        this.mInterpolator = interpolator;
    }

    @ViewDebug.ExportedProperty(category = NotificationCompat.CATEGORY_PROGRESS)
    public synchronized int getMax() {
        return this.mMax;
    }

    public synchronized void setMax(int i) {
        if (this.mMinInitialized && i < this.mMin) {
            i = this.mMin;
        }
        this.mMaxInitialized = true;
        if (!this.mMinInitialized || i == this.mMax) {
            this.mMax = i;
        } else {
            this.mMax = i;
            postInvalidate();
            if (this.mProgress > i) {
                this.mProgress = i;
            }
            refreshProgress(R.id.progress, this.mProgress, false, false);
        }
    }

    @Px
    public int getMaxHeight() {
        return this.mMaxHeight;
    }

    public void setMaxHeight(@Px int i) {
        this.mMaxHeight = i;
        requestLayout();
    }

    @Px
    public int getMaxWidth() {
        return this.mMaxWidth;
    }

    public void setMaxWidth(@Px int i) {
        this.mMaxWidth = i;
        requestLayout();
    }

    @ViewDebug.ExportedProperty(category = NotificationCompat.CATEGORY_PROGRESS)
    public synchronized int getMin() {
        return this.mMin;
    }

    public synchronized void setMin(int i) {
        if (this.mMaxInitialized && i > this.mMax) {
            i = this.mMax;
        }
        this.mMinInitialized = true;
        if (!this.mMaxInitialized || i == this.mMin) {
            this.mMin = i;
        } else {
            this.mMin = i;
            postInvalidate();
            if (this.mProgress < i) {
                this.mProgress = i;
            }
            refreshProgress(R.id.progress, this.mProgress, false, false);
        }
    }

    @Px
    public int getMinHeight() {
        return this.mMinHeight;
    }

    public void setMinHeight(@Px int i) {
        this.mMinHeight = i;
        requestLayout();
    }

    @Px
    public int getMinWidth() {
        return this.mMinWidth;
    }

    public void setMinWidth(@Px int i) {
        this.mMinWidth = i;
        requestLayout();
    }

    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP_PREFIX})
    public boolean getMirrorForRtl() {
        return this.mMirrorForRtl;
    }

    public int getPaddingLeft() {
        return SeslViewReflector.getField_mPaddingLeft(this);
    }

    public int getPaddingRight() {
        return SeslViewReflector.getField_mPaddingRight(this);
    }

    @ViewDebug.ExportedProperty(category = NotificationCompat.CATEGORY_PROGRESS)
    public synchronized int getProgress() {
        return this.mIndeterminate ? 0 : this.mProgress;
    }

    public synchronized void setProgress(int i) {
        setProgressInternal(i, false, false);
    }

    @Nullable
    public ColorStateList getProgressBackgroundTintList() {
        ProgressTintInfo progressTintInfo = this.mProgressTintInfo;
        if (progressTintInfo != null) {
            return progressTintInfo.mProgressBackgroundTintList;
        }
        return null;
    }

    public void setProgressBackgroundTintList(@Nullable ColorStateList colorStateList) {
        if (this.mProgressTintInfo == null) {
            this.mProgressTintInfo = new ProgressTintInfo();
        }
        ProgressTintInfo progressTintInfo = this.mProgressTintInfo;
        progressTintInfo.mProgressBackgroundTintList = colorStateList;
        progressTintInfo.mHasProgressBackgroundTint = true;
        if (this.mProgressDrawable != null) {
            applyProgressBackgroundTint();
        }
    }

    @Nullable
    public PorterDuff.Mode getProgressBackgroundTintMode() {
        ProgressTintInfo progressTintInfo = this.mProgressTintInfo;
        if (progressTintInfo != null) {
            return progressTintInfo.mProgressBackgroundTintMode;
        }
        return null;
    }

    public void setProgressBackgroundTintMode(@Nullable PorterDuff.Mode mode) {
        if (this.mProgressTintInfo == null) {
            this.mProgressTintInfo = new ProgressTintInfo();
        }
        ProgressTintInfo progressTintInfo = this.mProgressTintInfo;
        progressTintInfo.mProgressBackgroundTintMode = mode;
        progressTintInfo.mHasProgressBackgroundTintMode = true;
        if (this.mProgressDrawable != null) {
            applyProgressBackgroundTint();
        }
    }

    public Drawable getProgressDrawable() {
        return this.mProgressDrawable;
    }

    public void setProgressDrawable(Drawable drawable) {
        Drawable drawable2 = this.mProgressDrawable;
        if (drawable2 != drawable) {
            if (drawable2 != null) {
                drawable2.setCallback(null);
                unscheduleDrawable(this.mProgressDrawable);
            }
            this.mProgressDrawable = drawable;
            if (drawable != null) {
                drawable.setCallback(this);
                DrawableCompat.setLayoutDirection(drawable, ViewCompat.getLayoutDirection(this));
                if (drawable.isStateful()) {
                    drawable.setState(getDrawableState());
                }
                if (this.mCurrentMode == 3) {
                    int minimumWidth = drawable.getMinimumWidth();
                    if (this.mMaxWidth < minimumWidth) {
                        this.mMaxWidth = minimumWidth;
                    }
                    applyProgressTints();
                } else {
                    int minimumHeight = drawable.getMinimumHeight();
                    if (this.mMaxHeight < minimumHeight) {
                        this.mMaxHeight = minimumHeight;
                    }
                    applyProgressTints();
                }
                requestLayout();
                applyProgressTints();
            }
            if (!this.mIndeterminate) {
                swapCurrentDrawable(drawable);
                postInvalidate();
            }
            updateDrawableBounds(getWidth(), getHeight());
            updateDrawableState();
            doRefreshProgress(R.id.progress, this.mProgress, false, false, false);
            doRefreshProgress(R.id.secondaryProgress, this.mSecondaryProgress, false, false, false);
        }
    }

    @Nullable
    public ColorStateList getProgressTintList() {
        ProgressTintInfo progressTintInfo = this.mProgressTintInfo;
        if (progressTintInfo != null) {
            return progressTintInfo.mProgressTintList;
        }
        return null;
    }

    public void setProgressTintList(@Nullable ColorStateList colorStateList) {
        if (this.mProgressTintInfo == null) {
            this.mProgressTintInfo = new ProgressTintInfo();
        }
        ProgressTintInfo progressTintInfo = this.mProgressTintInfo;
        progressTintInfo.mProgressTintList = colorStateList;
        progressTintInfo.mHasProgressTint = true;
        if (this.mProgressDrawable != null) {
            applyPrimaryProgressTint();
        }
    }

    @Nullable
    public PorterDuff.Mode getProgressTintMode() {
        ProgressTintInfo progressTintInfo = this.mProgressTintInfo;
        if (progressTintInfo != null) {
            return progressTintInfo.mProgressTintMode;
        }
        return null;
    }

    public void setProgressTintMode(@Nullable PorterDuff.Mode mode) {
        if (this.mProgressTintInfo == null) {
            this.mProgressTintInfo = new ProgressTintInfo();
        }
        ProgressTintInfo progressTintInfo = this.mProgressTintInfo;
        progressTintInfo.mProgressTintMode = mode;
        progressTintInfo.mHasProgressTintMode = true;
        if (this.mProgressDrawable != null) {
            applyPrimaryProgressTint();
        }
    }

    @ViewDebug.ExportedProperty(category = NotificationCompat.CATEGORY_PROGRESS)
    public synchronized int getSecondaryProgress() {
        return this.mIndeterminate ? 0 : this.mSecondaryProgress;
    }

    public synchronized void setSecondaryProgress(int i) {
        if (!this.mIndeterminate) {
            if (i < this.mMin) {
                i = this.mMin;
            }
            if (i > this.mMax) {
                i = this.mMax;
            }
            if (i != this.mSecondaryProgress) {
                this.mSecondaryProgress = i;
                refreshProgress(R.id.secondaryProgress, i, false, false);
            }
        }
    }

    @Nullable
    public ColorStateList getSecondaryProgressTintList() {
        ProgressTintInfo progressTintInfo = this.mProgressTintInfo;
        if (progressTintInfo != null) {
            return progressTintInfo.mSecondaryProgressTintList;
        }
        return null;
    }

    public void setSecondaryProgressTintList(@Nullable ColorStateList colorStateList) {
        if (this.mProgressTintInfo == null) {
            this.mProgressTintInfo = new ProgressTintInfo();
        }
        ProgressTintInfo progressTintInfo = this.mProgressTintInfo;
        progressTintInfo.mSecondaryProgressTintList = colorStateList;
        progressTintInfo.mHasSecondaryProgressTint = true;
        if (this.mProgressDrawable != null) {
            applySecondaryProgressTint();
        }
    }

    @Nullable
    public PorterDuff.Mode getSecondaryProgressTintMode() {
        ProgressTintInfo progressTintInfo = this.mProgressTintInfo;
        if (progressTintInfo != null) {
            return progressTintInfo.mSecondaryProgressTintMode;
        }
        return null;
    }

    public void setSecondaryProgressTintMode(@Nullable PorterDuff.Mode mode) {
        if (this.mProgressTintInfo == null) {
            this.mProgressTintInfo = new ProgressTintInfo();
        }
        ProgressTintInfo progressTintInfo = this.mProgressTintInfo;
        progressTintInfo.mSecondaryProgressTintMode = mode;
        progressTintInfo.mHasSecondaryProgressTintMode = true;
        if (this.mProgressDrawable != null) {
            applySecondaryProgressTint();
        }
    }

    public final synchronized void incrementProgressBy(int i) {
        setProgress(this.mProgress + i);
    }

    public final synchronized void incrementSecondaryProgressBy(int i) {
        setSecondaryProgress(this.mSecondaryProgress + i);
    }

    public void invalidateDrawable(@NonNull Drawable drawable) {
        if (this.mInDrawing) {
            return;
        }
        if (verifyDrawable(drawable)) {
            Rect bounds = drawable.getBounds();
            int scrollX = getScrollX() + getPaddingLeft();
            int scrollY = getScrollY() + getPaddingTop();
            invalidate(bounds.left + scrollX, bounds.top + scrollY, bounds.right + scrollX, bounds.bottom + scrollY);
            return;
        }
        super.invalidateDrawable(drawable);
    }

    public boolean isAnimating() {
        return isIndeterminate() && getWindowVisibility() == VISIBLE && isShown();
    }

    @ViewDebug.ExportedProperty(category = NotificationCompat.CATEGORY_PROGRESS)
    public synchronized boolean isIndeterminate() {
        return this.mIndeterminate;
    }

    public synchronized void setIndeterminate(boolean z) {
        if ((!this.mOnlyIndeterminate || !this.mIndeterminate) && z != this.mIndeterminate) {
            this.mIndeterminate = z;
            if (z) {
                swapCurrentDrawable(this.mIndeterminateDrawable);
                startAnimation();
            } else {
                swapCurrentDrawable(this.mProgressDrawable);
                stopAnimation();
            }
        }
    }

    public void jumpDrawablesToCurrentState() {
        super.jumpDrawablesToCurrentState();
        Drawable drawable = this.mProgressDrawable;
        if (drawable != null) {
            drawable.jumpToCurrentState();
        }
        Drawable drawable2 = this.mIndeterminateDrawable;
        if (drawable2 != null) {
            drawable2.jumpToCurrentState();
        }
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (this.mIndeterminate) {
            startAnimation();
        }
        if (this.mRefreshData != null) {
            synchronized (this) {
                int size = this.mRefreshData.size();
                for (int i = 0; i < size; i++) {
                    RefreshData refreshData = this.mRefreshData.get(i);
                    doRefreshProgress(refreshData.id, refreshData.progress, refreshData.fromUser, true, refreshData.animate);
                    refreshData.recycle();
                }
                this.mRefreshData.clear();
            }
        }
        this.mAttached = true;
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        if (this.mIndeterminate) {
            stopAnimation();
        }
        RefreshProgressRunnable refreshProgressRunnable = this.mRefreshProgressRunnable;
        if (refreshProgressRunnable != null) {
            removeCallbacks(refreshProgressRunnable);
            this.mRefreshIsPosted = false;
        }
        AccessibilityEventSender accessibilityEventSender = this.mAccessibilityEventSender;
        if (accessibilityEventSender != null) {
            removeCallbacks(accessibilityEventSender);
        }
        super.onDetachedFromWindow();
        this.mAttached = false;
    }

    /* access modifiers changed from: protected */
    public synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawTrack(canvas);
    }

    public void onInitializeAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        super.onInitializeAccessibilityEvent(accessibilityEvent);
        accessibilityEvent.setItemCount(this.mMax - this.mMin);
        accessibilityEvent.setCurrentItemIndex(this.mProgress);
    }

    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo accessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(accessibilityNodeInfo);
        if (Build.VERSION.SDK_INT >= 19 && !isIndeterminate()) {
            accessibilityNodeInfo.setRangeInfo(AccessibilityNodeInfo.RangeInfo.obtain(0, (float) getMin(), (float) getMax(), (float) getProgress()));
        }
    }

    /* access modifiers changed from: protected */
    public synchronized void onMeasure(int i, int i2) {
        int i3;
        int i4;
        Drawable drawable = this.mCurrentDrawable;
        if (drawable != null) {
            i3 = Math.max(this.mMinWidth, Math.min(this.mMaxWidth, drawable.getIntrinsicWidth()));
            i4 = Math.max(this.mMinHeight, Math.min(this.mMaxHeight, drawable.getIntrinsicHeight()));
        } else {
            i4 = 0;
            i3 = 0;
        }
        updateDrawableState();
        setMeasuredDimension(View.resolveSizeAndState(i3 + getPaddingLeft() + getPaddingRight(), i, 0), View.resolveSizeAndState(i4 + getPaddingTop() + getPaddingBottom(), i2, 0));
    }

    /* access modifiers changed from: package-private */
    public void onProgressRefresh(float f, boolean z, int i) {
        if (((AccessibilityManager) getContext().getSystemService(Context.ACCESSIBILITY_SERVICE)).isEnabled()) {
            scheduleAccessibilityEventSender();
        }
        int i2 = this.mSecondaryProgress;
        if (i2 > this.mProgress && !z) {
            refreshProgress(R.id.secondaryProgress, i2, false, false);
        }
    }

    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP_PREFIX})
    public void onResolveDrawables(int i) {
        Drawable drawable = this.mCurrentDrawable;
        int layoutDirection = ViewCompat.getLayoutDirection(this);
        if (drawable != null) {
            DrawableCompat.setLayoutDirection(drawable, layoutDirection);
        }
        Drawable drawable2 = this.mIndeterminateDrawable;
        if (drawable2 != null) {
            DrawableCompat.setLayoutDirection(drawable2, layoutDirection);
        }
        Drawable drawable3 = this.mProgressDrawable;
        if (drawable3 != null) {
            DrawableCompat.setLayoutDirection(drawable3, layoutDirection);
        }
    }

    public void onRestoreInstanceState(Parcelable parcelable) {
        SavedState savedState = (SavedState) parcelable;
        super.onRestoreInstanceState(savedState.getSuperState());
        setProgress(savedState.progress);
        setSecondaryProgress(savedState.secondaryProgress);
    }

    public Parcelable onSaveInstanceState() {
        SavedState savedState = new SavedState(super.onSaveInstanceState());
        savedState.progress = this.mProgress;
        savedState.secondaryProgress = this.mSecondaryProgress;
        return savedState;
    }

    /* access modifiers changed from: protected */
    public void onSizeChanged(int i, int i2, int i3, int i4) {
        updateDrawableBounds(i, i2);
    }

    /* access modifiers changed from: protected */
    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP_PREFIX})
    public void onSlidingRefresh(int i) {
        Drawable drawable = this.mCurrentDrawable;
        if (drawable != null) {
            Drawable drawable2 = null;
            if (drawable instanceof LayerDrawable) {
                drawable2 = ((LayerDrawable) drawable).findDrawableByLayerId(R.id.progress);
            }
            if (drawable2 != null) {
                drawable2.setLevel(i);
            }
        }
    }

    public void onVisibilityAggregated(boolean z) {
        super.onVisibilityAggregated(z);
        if (z != this.mAggregatedIsVisible) {
            this.mAggregatedIsVisible = z;
            if (this.mIndeterminate) {
                if (z) {
                    startAnimation();
                } else {
                    stopAnimation();
                }
            }
            Drawable drawable = this.mCurrentDrawable;
            if (drawable != null) {
                drawable.setVisible(z, false);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void onVisualProgressChanged(int i, float f) {
    }

    public void postInvalidate() {
        if (!this.mNoInvalidate) {
            super.postInvalidate();
        }
    }

    public void setIndeterminateDrawableTiled(Drawable drawable) {
        if (drawable != null) {
            drawable = tileifyIndeterminate(drawable);
        }
        setIndeterminateDrawable(drawable);
    }

    public void setInterpolator(Context context, @InterpolatorRes int i) {
        setInterpolator(AnimationUtils.loadInterpolator(context, i));
    }

    public void setMode(int i2) {
        Drawable drawable;
        this.mCurrentMode = i2;
        if (i2 == 3) {
            drawable = androidx.core.content.ContextCompat.getDrawable(getContext(), R.drawable.sesl_scrubber_progress_vertical);
        } else if (i2 != 4) {
            drawable = null;
        } else {
            drawable = androidx.core.content.ContextCompat.getDrawable(getContext(), R.drawable.sesl_split_seekbar_background_progress);
        }
        if (drawable != null) {
            setProgressDrawableTiled(drawable);
        }
    }

    public void setProgress(int i, boolean z) {
        setProgressInternal(i, false, z);
    }

    public void setProgressDrawableTiled(Drawable drawable) {
        if (drawable != null) {
            drawable = tileify(drawable, false);
        }
        setProgressDrawable(drawable);
    }

    /* access modifiers changed from: package-private */
    public synchronized boolean setProgressInternal(int i, boolean z, boolean z2) {
        if (this.mIndeterminate) {
            return false;
        }
        int constrain = MathUtilsdotconstrain(i, this.mMin, this.mMax);
        if (constrain == this.mProgress) {
            return false;
        }
        this.mProgress = constrain;
        refreshProgress(R.id.progress, constrain, z, z2);
        return true;
    }

    /* access modifiers changed from: protected */
    @SuppressLint("RestrictedApi")
    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP_PREFIX})
    public void updateDrawableBounds(int i, int i2) {
        int i3;
        int i4;
        int i5;
        int paddingRight = i - (getPaddingRight() + getPaddingLeft());
        int paddingTop = i2 - (getPaddingTop() + getPaddingBottom());
        Drawable drawable = this.mIndeterminateDrawable;
        if (drawable != null) {
            if (this.mOnlyIndeterminate && !(drawable instanceof AnimationDrawable)) {
                float intrinsicWidth = ((float) drawable.getIntrinsicWidth()) / ((float) this.mIndeterminateDrawable.getIntrinsicHeight());
                float f = (float) paddingRight;
                float f2 = (float) paddingTop;
                float f3 = f / f2;
                if (((double) Math.abs(intrinsicWidth - f3)) < 1.0E-7d) {
                    if (f3 > intrinsicWidth) {
                        int i6 = (int) (f2 * intrinsicWidth);
                        int i7 = (paddingRight - i6) / 2;
                        i3 = i7;
                        i4 = i6 + i7;
                        i5 = 0;
                    } else {
                        int i8 = (int) (f * (1.0f / intrinsicWidth));
                        int i9 = (paddingTop - i8) / 2;
                        int i10 = i8 + i9;
                        i4 = paddingRight;
                        i3 = 0;
                        i5 = i9;
                        paddingTop = i10;
                    }
                    if (this.mMirrorForRtl || !ViewUtils.isLayoutRtl(this)) {
                        paddingRight = i4;
                    } else {
                        int i11 = paddingRight - i4;
                        paddingRight -= i3;
                        i3 = i11;
                    }
                    this.mIndeterminateDrawable.setBounds(i3, i5, paddingRight, paddingTop);
                }
            }
            i4 = paddingRight;
            i5 = 0;
            i3 = 0;
            if (this.mMirrorForRtl) {
            }
            paddingRight = i4;
            this.mIndeterminateDrawable.setBounds(i3, i5, paddingRight, paddingTop);
        }
        Drawable drawable2 = this.mProgressDrawable;
        if (drawable2 != null) {
            drawable2.setBounds(0, 0, paddingRight, paddingTop);
        }
    }

    /* access modifiers changed from: protected */
    public boolean verifyDrawable(@NonNull Drawable drawable) {
        return drawable == this.mProgressDrawable || drawable == this.mIndeterminateDrawable || super.verifyDrawable(drawable);
    }

    @Retention(RetentionPolicy.SOURCE)
    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP_PREFIX})
    public @interface SeekBarMode {
    }

    /* access modifiers changed from: private */
    public static class ProgressTintInfo {
        boolean mHasIndeterminateTint;
        boolean mHasIndeterminateTintMode;
        boolean mHasProgressBackgroundTint;
        boolean mHasProgressBackgroundTintMode;
        boolean mHasProgressTint;
        boolean mHasProgressTintMode;
        boolean mHasSecondaryProgressTint;
        boolean mHasSecondaryProgressTintMode;
        ColorStateList mIndeterminateTintList;
        PorterDuff.Mode mIndeterminateTintMode;
        ColorStateList mProgressBackgroundTintList;
        PorterDuff.Mode mProgressBackgroundTintMode;
        ColorStateList mProgressTintList;
        PorterDuff.Mode mProgressTintMode;
        ColorStateList mSecondaryProgressTintList;
        PorterDuff.Mode mSecondaryProgressTintMode;

        private ProgressTintInfo() {
        }
    }

    /* access modifiers changed from: private */
    public static class RefreshData {
        private static final int POOL_MAX = 24;
        private static final Pools.SynchronizedPool<RefreshData> sPool = new Pools.SynchronizedPool<>(24);
        public boolean animate;
        public boolean fromUser;
        public int id;
        public int progress;

        private RefreshData() {
        }

        public static RefreshData obtain(int i, int i2, boolean z, boolean z2) {
            RefreshData acquire = sPool.acquire();
            if (acquire == null) {
                acquire = new RefreshData();
            }
            acquire.id = i;
            acquire.progress = i2;
            acquire.fromUser = z;
            acquire.animate = z2;
            return acquire;
        }

        public void recycle() {
            sPool.release(this);
        }
    }

    /* access modifiers changed from: package-private */
    public static class SavedState extends BaseSavedState {
        @NonNull
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            /* class de.dlyt.yanndroid.samsung.SeslProgressBar.SavedState.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public SavedState createFromParcel(Parcel parcel) {
                return new SavedState(parcel);
            }

            @Override // android.os.Parcelable.Creator
            public SavedState[] newArray(int i) {
                return new SavedState[i];
            }
        };
        int progress;
        int secondaryProgress;

        private SavedState(Parcel parcel) {
            super(parcel);
            this.progress = parcel.readInt();
            this.secondaryProgress = parcel.readInt();
        }

        SavedState(Parcelable parcelable) {
            super(parcelable);
        }

        public void writeToParcel(Parcel parcel, int i) {
            super.writeToParcel(parcel, i);
            parcel.writeInt(this.progress);
            parcel.writeInt(this.secondaryProgress);
        }
    }

    /* access modifiers changed from: private */
    public static class StateListDrawableCompat {
        private static final boolean IS_BASE_SDK_VERSION = (Build.VERSION.SDK_INT <= 23);

        private StateListDrawableCompat() {
        }

        static int getStateCount(StateListDrawable stateListDrawable) {
            if (!IS_BASE_SDK_VERSION) {
                return 0;
            }
            SeslStateListDrawableReflector.getStateCount(stateListDrawable);
            return 0;
        }

        static Drawable getStateDrawable(StateListDrawable stateListDrawable, int i) {
            if (IS_BASE_SDK_VERSION) {
                return SeslStateListDrawableReflector.getStateDrawable(stateListDrawable, i);
            }
            return null;
        }

        static int[] getStateSet(StateListDrawable stateListDrawable, int i) {
            if (IS_BASE_SDK_VERSION) {
                return SeslStateListDrawableReflector.getStateSet(stateListDrawable, i);
            }
            return null;
        }
    }

    /* access modifiers changed from: private */
    public class AccessibilityEventSender implements Runnable {
        private AccessibilityEventSender() {
        }

        public void run() {
            ProgressBar.this.sendAccessibilityEvent(4);
        }
    }

    /* access modifiers changed from: private */
    public class RefreshProgressRunnable implements Runnable {
        private RefreshProgressRunnable() {
        }

        public void run() {
            synchronized (ProgressBar.this) {
                int size = ProgressBar.this.mRefreshData.size();
                for (int i = 0; i < size; i++) {
                    RefreshData refreshData = (RefreshData) ProgressBar.this.mRefreshData.get(i);
                    ProgressBar.this.doRefreshProgress(refreshData.id, refreshData.progress, refreshData.fromUser, true, refreshData.animate);
                    refreshData.recycle();
                }
                ProgressBar.this.mRefreshData.clear();
                ProgressBar.this.mRefreshIsPosted = false;
            }
        }
    }
}
