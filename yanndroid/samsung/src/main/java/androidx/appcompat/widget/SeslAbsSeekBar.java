package androidx.appcompat.widget;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.LinearInterpolator;
import android.widget.AbsSeekBar;

import androidx.Styleable;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.appcompat.animation.SeslAnimationUtils;
import androidx.appcompat.graphics.drawable.DrawableWrapper;
import androidx.appcompat.util.SeslMisc;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.util.Preconditions;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.reflect.view.SeslViewReflector;
import androidx.reflect.widget.SeslHoverPopupWindowReflector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.dlyt.yanndroid.samsung.ProgressBar;
import de.dlyt.yanndroid.samsung.R;

public abstract class SeslAbsSeekBar extends ProgressBar {
    static final float SCALE_FACTOR = 1000.0f;
    private static final int HOVER_DETECT_TIME = 200;
    private static final int HOVER_POPUP_WINDOW_GRAVITY_CENTER_HORIZONTAL_ON_POINT = 513;
    private static final int HOVER_POPUP_WINDOW_GRAVITY_TOP_ABOVE = 12336;
    private static final boolean IS_BASE_SDK_VERSION = (Build.VERSION.SDK_INT <= 23);
    private static final int MUTE_VIB_DISTANCE_LVL = 400;
    private static final int MUTE_VIB_DURATION = 500;
    private static final int MUTE_VIB_TOTAL = 4;
    private static final int NO_ALPHA = 255;
    private static final String TAG = "SeslAbsSeekBar";
    private final List<Rect> mGestureExclusionRects;
    private final Rect mTempRect;
    private final Rect mThumbRect;
    public boolean mIsSeamless;
    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP_PREFIX})
    boolean mIsUserSeekable;
    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP_PREFIX})
    float mTouchProgressOffset;
    private boolean mAllowedSeekBarAnimation;
    private int mCurrentProgressLevel;
    private ColorStateList mDefaultActivatedProgressColor;
    private ColorStateList mDefaultActivatedThumbColor;
    private ColorStateList mDefaultNormalProgressColor;
    private ColorStateList mDefaultSecondaryProgressColor;
    private float mDisabledAlpha;
    private Drawable mDivider;
    private boolean mHasThumbTint;
    private boolean mHasThumbTintMode;
    private boolean mHasTickMarkTint;
    private boolean mHasTickMarkTintMode;
    private int mHoveringLevel;
    private boolean mIsDragging;
    private boolean mIsDraggingForSliding;
    private boolean mIsFirstSetProgress;
    private boolean mIsLightTheme;
    private boolean mIsSetModeCalled;
    private boolean mIsTouchDisabled;
    private int mKeyProgressIncrement;
    private boolean mLargeFont;
    private AnimatorSet mMuteAnimationSet;
    private ColorStateList mOverlapActivatedProgressColor;
    private ColorStateList mOverlapActivatedThumbColor;
    private Drawable mOverlapBackground;
    private ColorStateList mOverlapNormalProgressColor;
    private int mOverlapPoint;
    private int mPreviousHoverPopupType;
    private int mScaledTouchSlop;
    private boolean mSetDualColorMode;
    private Drawable mSplitProgress;
    private boolean mSplitTrack;
    private Drawable mThumb;
    private int mThumbOffset;
    private int mThumbPosX;
    private int mThumbRadius;
    private ColorStateList mThumbTintList;
    private PorterDuff.Mode mThumbTintMode;
    private Drawable mTickMark;
    private ColorStateList mTickMarkTintList;
    private PorterDuff.Mode mTickMarkTintMode;
    private float mTouchDownX;
    private float mTouchDownY;
    private int mTrackMaxWidth;
    private int mTrackMinWidth;
    private boolean mUseMuteAnimation;
    private List<Rect> mUserGestureExclusionRects;
    private ValueAnimator mValueAnimator;

    public SeslAbsSeekBar(Context context) {
        super(context);
        this.mTempRect = new Rect();
        this.mThumbTintList = null;
        this.mThumbTintMode = null;
        this.mHasThumbTint = false;
        this.mHasThumbTintMode = false;
        this.mTickMarkTintList = null;
        this.mTickMarkTintMode = null;
        this.mHasTickMarkTint = false;
        this.mHasTickMarkTintMode = false;
        this.mIsUserSeekable = true;
        this.mKeyProgressIncrement = 1;
        this.mUserGestureExclusionRects = Collections.emptyList();
        this.mGestureExclusionRects = new ArrayList();
        this.mThumbRect = new Rect();
        this.mHoveringLevel = 0;
        this.mOverlapPoint = -1;
        this.mAllowedSeekBarAnimation = false;
        this.mUseMuteAnimation = false;
        this.mIsFirstSetProgress = false;
        this.mIsDraggingForSliding = false;
        this.mLargeFont = false;
        this.mIsTouchDisabled = false;
        this.mSetDualColorMode = false;
        this.mPreviousHoverPopupType = 0;
        this.mIsSetModeCalled = false;
        this.mIsSeamless = false;
    }

    public SeslAbsSeekBar(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mTempRect = new Rect();
        this.mThumbTintList = null;
        this.mThumbTintMode = null;
        this.mHasThumbTint = false;
        this.mHasThumbTintMode = false;
        this.mTickMarkTintList = null;
        this.mTickMarkTintMode = null;
        this.mHasTickMarkTint = false;
        this.mHasTickMarkTintMode = false;
        this.mIsUserSeekable = true;
        this.mKeyProgressIncrement = 1;
        this.mUserGestureExclusionRects = Collections.emptyList();
        this.mGestureExclusionRects = new ArrayList();
        this.mThumbRect = new Rect();
        this.mHoveringLevel = 0;
        this.mOverlapPoint = -1;
        this.mAllowedSeekBarAnimation = false;
        this.mUseMuteAnimation = false;
        this.mIsFirstSetProgress = false;
        this.mIsDraggingForSliding = false;
        this.mLargeFont = false;
        this.mIsTouchDisabled = false;
        this.mSetDualColorMode = false;
        this.mPreviousHoverPopupType = 0;
        this.mIsSetModeCalled = false;
        this.mIsSeamless = false;
    }

    public SeslAbsSeekBar(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    @SuppressLint("RestrictedApi")
    public SeslAbsSeekBar(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mTempRect = new Rect();
        this.mThumbTintList = null;
        this.mThumbTintMode = null;
        this.mHasThumbTint = false;
        this.mHasThumbTintMode = false;
        this.mTickMarkTintList = null;
        this.mTickMarkTintMode = null;
        this.mHasTickMarkTint = false;
        this.mHasTickMarkTintMode = false;
        this.mIsUserSeekable = true;
        this.mKeyProgressIncrement = 1;
        this.mUserGestureExclusionRects = Collections.emptyList();
        this.mGestureExclusionRects = new ArrayList();
        this.mThumbRect = new Rect();
        this.mHoveringLevel = 0;
        this.mOverlapPoint = -1;
        this.mAllowedSeekBarAnimation = false;
        this.mUseMuteAnimation = false;
        this.mIsFirstSetProgress = false;
        this.mIsDraggingForSliding = false;
        this.mLargeFont = false;
        this.mIsTouchDisabled = false;
        this.mSetDualColorMode = false;
        this.mPreviousHoverPopupType = 0;
        this.mIsSetModeCalled = false;
        this.mIsSeamless = false;
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, Styleable.styleable.AppCompatSeekBar, i, i2);
        if (Build.VERSION.SDK_INT >= 29) {
            saveAttributeDataForStyleable(context, Styleable.styleable.AppCompatSeekBar, attributeSet, obtainStyledAttributes, i, i2);
        }
        Resources resources = context.getResources();
        setThumb(obtainStyledAttributes.getDrawable(Styleable.styleable.AppCompatSeekBar_android_thumb));
        if (obtainStyledAttributes.hasValue(Styleable.styleable.AppCompatSeekBar_android_thumbTintMode)) {
            this.mThumbTintMode = DrawableUtils.parseTintMode(obtainStyledAttributes.getInt(Styleable.styleable.AppCompatSeekBar_android_thumbTintMode, -1), this.mThumbTintMode);
            this.mHasThumbTintMode = true;
        }
        if (obtainStyledAttributes.hasValue(Styleable.styleable.AppCompatSeekBar_android_thumbTint)) {
            this.mThumbTintList = obtainStyledAttributes.getColorStateList(Styleable.styleable.AppCompatSeekBar_android_thumbTint);
            this.mHasThumbTint = true;
        }
        setTickMark(obtainStyledAttributes.getDrawable(Styleable.styleable.AppCompatSeekBar_tickMark));
        if (obtainStyledAttributes.hasValue(Styleable.styleable.AppCompatSeekBar_tickMarkTintMode)) {
            this.mTickMarkTintMode = DrawableUtils.parseTintMode(obtainStyledAttributes.getInt(Styleable.styleable.AppCompatSeekBar_tickMarkTintMode, -1), this.mTickMarkTintMode);
            this.mHasTickMarkTintMode = true;
        }
        if (obtainStyledAttributes.hasValue(Styleable.styleable.AppCompatSeekBar_tickMarkTint)) {
            this.mTickMarkTintList = obtainStyledAttributes.getColorStateList(Styleable.styleable.AppCompatSeekBar_tickMarkTint);
            this.mHasTickMarkTint = true;
        }
        this.mSplitTrack = obtainStyledAttributes.getBoolean(Styleable.styleable.AppCompatSeekBar_android_splitTrack, false);
        this.mTrackMinWidth = obtainStyledAttributes.getDimensionPixelSize(Styleable.styleable.AppCompatSeekBar_seslTrackMinWidth, Math.round(resources.getDimension(R.dimen.sesl_seekbar_track_height)));
        this.mTrackMaxWidth = obtainStyledAttributes.getDimensionPixelSize(Styleable.styleable.AppCompatSeekBar_seslTrackMaxWidth, Math.round(resources.getDimension(R.dimen.sesl_seekbar_track_height_expand)));
        this.mThumbRadius = obtainStyledAttributes.getDimensionPixelSize(Styleable.styleable.AppCompatSeekBar_seslThumbRadius, Math.round(resources.getDimension(R.dimen.sesl_seekbar_thumb_radius)));
        setThumbOffset(obtainStyledAttributes.getDimensionPixelOffset(Styleable.styleable.AppCompatSeekBar_android_thumbOffset, getThumbOffset()));
        if (obtainStyledAttributes.hasValue(Styleable.styleable.AppCompatSeekBar_seslSeekBarMode)) {
            this.mCurrentMode = obtainStyledAttributes.getInt(Styleable.styleable.AppCompatSeekBar_seslSeekBarMode, 0);
        }
        boolean z = obtainStyledAttributes.getBoolean(Styleable.styleable.AppCompatSeekBar_useDisabledAlpha, true);
        obtainStyledAttributes.recycle();
        if (z) {
            TypedArray obtainStyledAttributes2 = context.obtainStyledAttributes(attributeSet, R.styleable.AppCompatTheme, 0, 0);
            this.mDisabledAlpha = obtainStyledAttributes2.getFloat(Styleable.styleable.AppCompatTheme_android_disabledAlpha, 0.5f);
            obtainStyledAttributes2.recycle();
        } else {
            this.mDisabledAlpha = 1.0f;
        }
        applyThumbTint();
        applyTickMarkTint();
        this.mScaledTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        this.mIsLightTheme = SeslMisc.isLightTheme(context);
        this.mDefaultNormalProgressColor = colorToColorStateList(resources.getColor(R.color.sesl_seekbar_control_color_default, context.getTheme()));
        this.mDefaultSecondaryProgressColor = colorToColorStateList(resources.getColor(R.color.sesl_seekbar_control_color_secondary));
        this.mDefaultActivatedProgressColor = colorToColorStateList(getColor(context, R.attr.colorPrimary));
        this.mOverlapNormalProgressColor = colorToColorStateList(resources.getColor(this.mIsLightTheme ? R.color.sesl_seekbar_overlap_color_default_light : R.color.sesl_seekbar_overlap_color_default_dark));
        this.mOverlapActivatedProgressColor = colorToColorStateList(resources.getColor(R.color.sesl_seekbar_overlap_color_activated));
        int[][] iArr = {new int[]{16842910}, new int[]{-16842910}};
        int[] iArr2 = new int[2];
        iArr2[0] = resources.getColor(R.color.sesl_seekbar_overlap_color_activated);
        iArr2[1] = resources.getColor(this.mIsLightTheme ? R.color.sesl_seekbar_disable_color_activated_light : R.color.sesl_seekbar_disable_color_activated_dark);
        this.mOverlapActivatedThumbColor = new ColorStateList(iArr, iArr2);
        ColorStateList thumbTintList = getThumbTintList();
        this.mDefaultActivatedThumbColor = thumbTintList;
        if (thumbTintList == null) {
            int[][] iArr3 = {new int[]{16842910}, new int[]{-16842910}};
            int[] iArr4 = new int[2];
            iArr4[0] = getColor(context, R.attr.colorPrimary);
            iArr4[1] = resources.getColor(this.mIsLightTheme ? R.color.sesl_seekbar_disable_color_activated_light : R.color.sesl_seekbar_disable_color_activated_dark);
            this.mDefaultActivatedThumbColor = new ColorStateList(iArr3, iArr4);
        }
        boolean z2 = true;
        this.mAllowedSeekBarAnimation = z2;
        if (z2) {
            initMuteAnimation();
        }
        int i3 = this.mCurrentMode;
        if (i3 != 0) {
            setMode(i3);
        }
    }

    private int getColor(Context context, int colorResId) {
        TypedValue typedValue = new TypedValue();
        TypedArray typedArray = context.obtainStyledAttributes(typedValue.data, new int[]{colorResId});
        int color = typedArray.getColor(0, 0);
        typedArray.recycle();
        return color;
    }


    private void applyThumbTint() {
        if (this.mThumb == null) {
            return;
        }
        if (this.mHasThumbTint || this.mHasThumbTintMode) {
            Drawable mutate = this.mThumb.mutate();
            this.mThumb = mutate;
            if (this.mHasThumbTint) {
                DrawableCompat.setTintList(mutate, this.mThumbTintList);
            }
            if (this.mHasThumbTintMode) {
                DrawableCompat.setTintMode(this.mThumb, this.mThumbTintMode);
            }
            if (this.mThumb.isStateful()) {
                this.mThumb.setState(getDrawableState());
            }
        }
    }

    private void applyTickMarkTint() {
        if (this.mTickMark == null) {
            return;
        }
        if (this.mHasTickMarkTint || this.mHasTickMarkTintMode) {
            Drawable mutate = this.mTickMark.mutate();
            this.mTickMark = mutate;
            if (this.mHasTickMarkTint) {
                DrawableCompat.setTintList(mutate, this.mTickMarkTintList);
            }
            if (this.mHasTickMarkTintMode) {
                DrawableCompat.setTintMode(this.mTickMark, this.mTickMarkTintMode);
            }
            if (this.mTickMark.isStateful()) {
                this.mTickMark.setState(getDrawableState());
            }
        }
    }

    private void attemptClaimDrag() {
        if (getParent() != null) {
            getParent().requestDisallowInterceptTouchEvent(true);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void callSuperSetProgress(int i) {
        super.setProgress(i);
    }

    private void cancelMuteAnimation() {
        AnimatorSet animatorSet = this.mMuteAnimationSet;
        if (animatorSet != null && animatorSet.isRunning()) {
            this.mMuteAnimationSet.cancel();
        }
    }

    private boolean checkInvalidatedDualColorMode() {
        return this.mOverlapPoint == -1 || this.mOverlapBackground == null;
    }

    private ColorStateList colorToColorStateList(int i) {
        return new ColorStateList(new int[][]{new int[0]}, new int[]{i});
    }

    private int getHoverPopupType() {
        if (IS_BASE_SDK_VERSION) {
            return SeslViewReflector.semGetHoverPopupType(this);
        }
        return 0;
    }

    private float getScale() {
        int min = getMin();
        int max = getMax() - min;
        if (max > 0) {
            return ((float) (getProgress() - min)) / ((float) max);
        }
        return 0.0f;
    }

    private void initDualOverlapDrawable() {
        Drawable mutate;
        int i = this.mCurrentMode;
        if (i == 5) {
            mutate = new SliderDrawable(this, (float) this.mTrackMinWidth, (float) this.mTrackMaxWidth, this.mOverlapNormalProgressColor);
        } else if (i == 6) {
            mutate = new SliderDrawable((float) this.mTrackMinWidth, (float) this.mTrackMaxWidth, this.mOverlapNormalProgressColor, true);
        } else if (getProgressDrawable() != null && getProgressDrawable().getConstantState() != null) {
            mutate = getProgressDrawable().getConstantState().newDrawable().mutate();
        } else {
            return;
        }
        this.mOverlapBackground = mutate;
    }

    private void initMuteAnimation() {
        ValueAnimator valueAnimator;
        this.mMuteAnimationSet = new AnimatorSet();
        ArrayList arrayList = new ArrayList();
        int i = MUTE_VIB_DISTANCE_LVL;
        for (int i2 = 0; i2 < 8; i2++) {
            boolean z = i2 % 2 == 0;
            int[] iArr = new int[2];
            if (z) {
                iArr[0] = 0;
                iArr[1] = i;
                valueAnimator = ValueAnimator.ofInt(iArr);
            } else {
                iArr[0] = i;
                iArr[1] = 0;
                valueAnimator = ValueAnimator.ofInt(iArr);
            }
            valueAnimator.setDuration((long) 62);
            valueAnimator.setInterpolator(new LinearInterpolator());
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                /* class androidx.appcompat.widget.SeslAbsSeekBar.AnonymousClass2 */

                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    SeslAbsSeekBar.this.mCurrentProgressLevel = ((Integer) valueAnimator.getAnimatedValue()).intValue();
                    SeslAbsSeekBar seslAbsSeekBar = SeslAbsSeekBar.this;
                    seslAbsSeekBar.onSlidingRefresh(seslAbsSeekBar.mCurrentProgressLevel);
                }
            });
            arrayList.add(valueAnimator);
            if (z) {
                i = (int) (((double) i) * 0.6d);
            }
        }
        this.mMuteAnimationSet.playSequentially(arrayList);
    }

    private void initializeExpandMode() {
        SliderDrawable sliderDrawable = new SliderDrawable(this, (float) this.mTrackMinWidth, (float) this.mTrackMaxWidth, this.mDefaultNormalProgressColor);
        SliderDrawable sliderDrawable2 = new SliderDrawable(this, (float) this.mTrackMinWidth, (float) this.mTrackMaxWidth, this.mDefaultSecondaryProgressColor);
        SliderDrawable sliderDrawable3 = new SliderDrawable(this, (float) this.mTrackMinWidth, (float) this.mTrackMaxWidth, this.mDefaultActivatedProgressColor);
        @SuppressLint("RestrictedApi") Drawable drawableWrapper = new DrawableWrapper(new ThumbDrawable(this.mThumbRadius, this.mDefaultActivatedThumbColor, false));
        LayerDrawable layerDrawable = new LayerDrawable(new Drawable[]{sliderDrawable, new ClipDrawable(sliderDrawable2, 19, 1), new ClipDrawable(sliderDrawable3, 19, 1)});
        layerDrawable.setPaddingMode(1);
        layerDrawable.setId(0, R.id.background);
        layerDrawable.setId(1, R.id.secondaryProgress);
        layerDrawable.setId(2, R.id.progress);
        setProgressDrawable(layerDrawable);
        setThumb(drawableWrapper);
        setBackgroundResource(R.drawable.sesl_seekbar_background_borderless_expand);
        int maxHeight = getMaxHeight();
        int i = this.mTrackMaxWidth;
        if (maxHeight > i) {
            setMaxHeight(i);
        }
    }

    private void initializeExpandVerticalMode() {
        SliderDrawable sliderDrawable = new SliderDrawable((float) this.mTrackMinWidth, (float) this.mTrackMaxWidth, this.mDefaultNormalProgressColor, true);
        SliderDrawable sliderDrawable2 = new SliderDrawable((float) this.mTrackMinWidth, (float) this.mTrackMaxWidth, this.mDefaultSecondaryProgressColor, true);
        SliderDrawable sliderDrawable3 = new SliderDrawable((float) this.mTrackMinWidth, (float) this.mTrackMaxWidth, this.mDefaultActivatedProgressColor, true);
        @SuppressLint("RestrictedApi") Drawable drawableWrapper = new DrawableWrapper(new ThumbDrawable(this.mThumbRadius, this.mDefaultActivatedThumbColor, true));
        LayerDrawable layerDrawable = new LayerDrawable(new Drawable[]{sliderDrawable, new ClipDrawable(sliderDrawable2, 81, 2), new ClipDrawable(sliderDrawable3, 81, 2)});
        layerDrawable.setPaddingMode(1);
        layerDrawable.setId(0, R.id.background);
        layerDrawable.setId(1, R.id.secondaryProgress);
        layerDrawable.setId(2, R.id.progress);
        setProgressDrawable(layerDrawable);
        setThumb(drawableWrapper);
        setBackgroundResource(R.drawable.sesl_seekbar_background_borderless_expand);
        int maxWidth = getMaxWidth();
        int i = this.mTrackMaxWidth;
        if (maxWidth > i) {
            setMaxWidth(i);
        }
    }

    private boolean isHoverPopupTypeUserCustom(int i) {
        return IS_BASE_SDK_VERSION && i == SeslHoverPopupWindowReflector.getField_TYPE_USER_CUSTOM();
    }

    private void setHotspot(float f, float f2) {
        Drawable background = getBackground();
        if (background != null) {
            DrawableCompat.setHotspot(background, f, f2);
        }
    }

    private void setHoverPopupDetectTime() {
        if (IS_BASE_SDK_VERSION) {
            SeslHoverPopupWindowReflector.setHoverDetectTime(SeslViewReflector.semGetHoverPopup(this, true), 200);
        }
    }

    private void setHoverPopupGravity(int i) {
        if (IS_BASE_SDK_VERSION) {
            SeslHoverPopupWindowReflector.setGravity(SeslViewReflector.semGetHoverPopup(this, true), i);
        }
    }

    private void setHoverPopupOffset(int i, int i2) {
        if (IS_BASE_SDK_VERSION) {
            SeslHoverPopupWindowReflector.setOffset(SeslViewReflector.semGetHoverPopup(this, true), i, i2);
        }
    }

    private void setHoveringPoint(int i, int i2) {
        if (IS_BASE_SDK_VERSION) {
            SeslHoverPopupWindowReflector.setHoveringPoint(this, i, i2);
        }
    }

    private void setProgressOverlapTintList(@Nullable ColorStateList colorStateList) {
        super.setProgressTintList(colorStateList);
    }

    private void setThumbOverlapTintList(@Nullable ColorStateList colorStateList) {
        this.mThumbTintList = colorStateList;
        this.mHasThumbTint = true;
        applyThumbTint();
    }

    @SuppressLint("RestrictedApi")
    private void setThumbPos(int i, Drawable drawable, float f, int i2) {
        int i3;
        int i4 = this.mCurrentMode;
        if (i4 == 3 || i4 == 6) {
            setThumbPosInVertical(getHeight(), drawable, f, i2);
            return;
        }
        int paddingLeft = (i - getPaddingLeft()) - getPaddingRight();
        int intrinsicWidth = drawable.getIntrinsicWidth();
        int intrinsicHeight = drawable.getIntrinsicHeight();
        int i5 = (paddingLeft - intrinsicWidth) + (this.mThumbOffset * 2);
        int i6 = (int) ((f * ((float) i5)) + 0.5f);
        if (i2 == Integer.MIN_VALUE) {
            Rect bounds = drawable.getBounds();
            int i7 = bounds.top;
            i3 = bounds.bottom;
            i2 = i7;
        } else {
            i3 = intrinsicHeight + i2;
        }
        if (ViewUtils.isLayoutRtl(this) && this.mMirrorForRtl) {
            i6 = i5 - i6;
        }
        int i8 = i6 + intrinsicWidth;
        Drawable background = getBackground();
        if (background != null) {
            int paddingLeft2 = getPaddingLeft() - this.mThumbOffset;
            int paddingTop = getPaddingTop();
            DrawableCompat.setHotspotBounds(background, i6 + paddingLeft2, i2 + paddingTop, paddingLeft2 + i8, paddingTop + i3);
        }
        drawable.setBounds(i6, i2, i8, i3);
        updateGestureExclusionRects();
        this.mThumbPosX = (i6 + getPaddingLeft()) - (getPaddingLeft() - (intrinsicWidth / 2));
        updateSplitProgress();
    }

    private void setThumbPosInVertical(int i, Drawable drawable, float f, int i2) {
        int i3;
        int paddingTop = (i - getPaddingTop()) - getPaddingBottom();
        int intrinsicHeight = drawable.getIntrinsicHeight();
        int intrinsicHeight2 = drawable.getIntrinsicHeight();
        int i4 = (paddingTop - intrinsicHeight2) + (this.mThumbOffset * 2);
        int i5 = (int) ((f * ((float) i4)) + 0.5f);
        if (i2 == Integer.MIN_VALUE) {
            Rect bounds = drawable.getBounds();
            int i6 = bounds.left;
            i3 = bounds.right;
            i2 = i6;
        } else {
            i3 = i2 + intrinsicHeight;
        }
        int i7 = i4 - i5;
        int i8 = intrinsicHeight2 + i7;
        Drawable background = getBackground();
        if (background != null) {
            int paddingLeft = getPaddingLeft();
            int paddingTop2 = getPaddingTop() - this.mThumbOffset;
            DrawableCompat.setHotspotBounds(background, i2 + paddingLeft, i7 + paddingTop2, paddingLeft + i3, paddingTop2 + i8);
        }
        drawable.setBounds(i2, i7, i3, i8);
        this.mThumbPosX = i7 + (intrinsicHeight / 2) + getPaddingLeft();
    }

    private void startDrag(MotionEvent motionEvent) {
        setPressed(true);
        Drawable drawable = this.mThumb;
        if (drawable != null) {
            invalidate(drawable.getBounds());
        }
        onStartTrackingTouch();
        trackTouchEvent(motionEvent);
        attemptClaimDrag();
    }

    private void startMuteAnimation() {
        cancelMuteAnimation();
        AnimatorSet animatorSet = this.mMuteAnimationSet;
        if (animatorSet != null) {
            animatorSet.start();
        }
    }

    private boolean supportIsHoveringUIEnabled() {
        return IS_BASE_SDK_VERSION && SeslViewReflector.isHoveringUIEnabled(this);
    }

    private boolean supportIsInScrollingContainer() {
        return SeslViewReflector.isInScrollingContainer(this);
    }

    private void trackHoverEvent(int i) {
        float f;
        int width = getWidth();
        int paddingLeft = (width - getPaddingLeft()) - getPaddingRight();
        float f2 = 0.0f;
        if (i < getPaddingLeft()) {
            f = 0.0f;
        } else if (i > width - getPaddingRight()) {
            f = 1.0f;
        } else {
            float paddingLeft2 = ((float) (i - getPaddingLeft())) / ((float) paddingLeft);
            f2 = this.mTouchProgressOffset;
            f = paddingLeft2;
        }
        this.mHoveringLevel = (int) (f2 + (f * ((float) getMax())));
    }

    @SuppressLint("RestrictedApi")
    private void trackTouchEvent(MotionEvent motionEvent) {
        int i = this.mCurrentMode;
        if (i != 3) {
            if (i != 6) {
                float f;
                float f2;
                float max;
                float f3;
                float f4;
                int min;
                i = Math.round(motionEvent.getX());
                int round = Math.round(motionEvent.getY());
                int width = getWidth();
                int paddingLeft = (width - getPaddingLeft()) - getPaddingRight();
                if (ViewUtils.isLayoutRtl(this) && this.mMirrorForRtl) {
                    if (i <= width - getPaddingRight()) {
                        if (i < getPaddingLeft()) {
                            f = 0.0f;
                            f2 = 1.0f;
                            if (this.mIsSeamless) {
                                max = (float) (super.getMax() - super.getMin());
                                f3 = 1.0f / max;
                                f4 = f2 % f3;
                                if (f4 > f3 / 2.0f) {
                                    f2 += f3 - f4;
                                }
                                f2 *= max;
                                min = super.getMin();
                            } else {
                                max = (float) (getMax() - getMin());
                                f3 = 1.0f / max;
                                f4 = f2 % f3;
                                if (f4 > f3 / 2.0f) {
                                    f2 += f3 - f4;
                                }
                                f2 *= max;
                                min = getMin();
                            }
                            f += f2 + ((float) min);
                            setHotspot((float) i, (float) round);
                            setProgressInternal(Math.round(f), true, IS_BASE_SDK_VERSION);
                            return;
                        }
                        width = (paddingLeft - i) + getPaddingLeft();
                        f2 = ((float) width) / ((float) paddingLeft);
                        f = this.mTouchProgressOffset;
                        if (this.mIsSeamless) {
                            max = (float) (getMax() - getMin());
                            f3 = 1.0f / max;
                            f4 = f2 % f3;
                            if (f4 > f3 / 2.0f) {
                                f2 += f3 - f4;
                            }
                            f2 *= max;
                            min = getMin();
                        } else {
                            max = (float) (super.getMax() - super.getMin());
                            f3 = 1.0f / max;
                            f4 = f2 % f3;
                            if (f4 > f3 / 2.0f) {
                                f2 += f3 - f4;
                            }
                            f2 *= max;
                            min = super.getMin();
                        }
                        f += f2 + ((float) min);
                        setHotspot((float) i, (float) round);
                        setProgressInternal(Math.round(f), true, IS_BASE_SDK_VERSION);
                        return;
                    }
                } else if (i >= getPaddingLeft()) {
                    if (i <= width - getPaddingRight()) {
                        width = i - getPaddingLeft();
                        f2 = ((float) width) / ((float) paddingLeft);
                        f = this.mTouchProgressOffset;
                        if (this.mIsSeamless) {
                            max = (float) (super.getMax() - super.getMin());
                            f3 = 1.0f / max;
                            if (f2 > 0.0f && f2 < 1.0f) {
                                f4 = f2 % f3;
                                if (f4 > f3 / 2.0f) {
                                    f2 += f3 - f4;
                                }
                            }
                            f2 *= max;
                            min = super.getMin();
                        } else {
                            max = (float) (getMax() - getMin());
                            f3 = 1.0f / max;
                            if (f2 > 0.0f && f2 < 1.0f) {
                                f4 = f2 % f3;
                                if (f4 > f3 / 2.0f) {
                                    f2 += f3 - f4;
                                }
                            }
                            f2 *= max;
                            min = getMin();
                        }
                        f += f2 + ((float) min);
                        setHotspot((float) i, (float) round);
                        setProgressInternal(Math.round(f), true, IS_BASE_SDK_VERSION);
                        return;
                    }
                    f = 0.0f;
                    f2 = 1.0f;
                    if (this.mIsSeamless) {
                        max = (float) (getMax() - getMin());
                        f3 = 1.0f / max;
                        f4 = f2 % f3;
                        if (f4 > f3 / 2.0f) {
                            f2 += f3 - f4;
                        }
                        f2 *= max;
                        min = getMin();
                    } else {
                        max = (float) (super.getMax() - super.getMin());
                        f3 = 1.0f / max;
                        f4 = f2 % f3;
                        if (f4 > f3 / 2.0f) {
                            f2 += f3 - f4;
                        }
                        f2 *= max;
                        min = super.getMin();
                    }
                    f += f2 + ((float) min);
                    setHotspot((float) i, (float) round);
                    setProgressInternal(Math.round(f), true, IS_BASE_SDK_VERSION);
                    return;
                }
                f2 = 0.0f;
                f = f2;
                if (this.mIsSeamless) {
                    max = (float) (super.getMax() - super.getMin());
                    f3 = 1.0f / max;
                    f4 = f2 % f3;
                    if (f4 > f3 / 2.0f) {
                        f2 += f3 - f4;
                    }
                    f2 *= max;
                    min = super.getMin();
                } else {
                    max = (float) (getMax() - getMin());
                    f3 = 1.0f / max;
                    f4 = f2 % f3;
                    if (f4 > f3 / 2.0f) {
                        f2 += f3 - f4;
                    }
                    f2 *= max;
                    min = getMin();
                }
                f += f2 + ((float) min);
                setHotspot((float) i, (float) round);
                setProgressInternal(Math.round(f), true, IS_BASE_SDK_VERSION);
                return;
            }
        }
        trackTouchEventInVertical(motionEvent);
    }

    private void trackTouchEventInVertical(MotionEvent motionEvent) {
        float f;
        float f2;
        int i;
        float f3;
        int height = getHeight();
        int paddingTop = (height - getPaddingTop()) - getPaddingBottom();
        int round = Math.round(motionEvent.getX());
        int round2 = height - Math.round(motionEvent.getY());
        if (round2 < getPaddingBottom()) {
            f2 = 0.0f;
            f = 0.0f;
        } else if (round2 > height - getPaddingTop()) {
            f = 0.0f;
            f2 = 1.0f;
        } else {
            f2 = ((float) (round2 - getPaddingBottom())) / ((float) paddingTop);
            f = this.mTouchProgressOffset;
        }
        if (this.mIsSeamless) {
            float max = (float) (super.getMax() - super.getMin());
            float f4 = 1.0f / max;
            if (f2 > 0.0f && f2 < 1.0f) {
                float f5 = f2 % f4;
                if (f5 > f4 / 2.0f) {
                    f2 += f4 - f5;
                }
            }
            f3 = f2 * max;
            i = super.getMin();
        } else {
            float max2 = (float) (getMax() - getMin());
            float f6 = 1.0f / max2;
            if (f2 > 0.0f && f2 < 1.0f) {
                float f7 = f2 % f6;
                if (f7 > f6 / 2.0f) {
                    f2 += f6 - f7;
                }
            }
            f3 = f2 * max2;
            i = getMin();
        }
        setHotspot((float) round, (float) round2);
        setProgressInternal(Math.round(f + f3 + ((float) i)), true, false);
    }

    private void updateBoundsForDualColor() {
        if (getCurrentDrawable() != null && !checkInvalidatedDualColorMode()) {
            this.mOverlapBackground.setBounds(getCurrentDrawable().getBounds());
        }
    }

    private void updateDualColorMode() {
        if (!checkInvalidatedDualColorMode()) {
            DrawableCompat.setTintList(this.mOverlapBackground, this.mOverlapNormalProgressColor);
            if (!this.mLargeFont) {
                if ((!this.mIsSeamless || ((float) super.getProgress()) <= ((float) this.mOverlapPoint) * SCALE_FACTOR) && getProgress() <= this.mOverlapPoint) {
                    setProgressTintList(this.mDefaultActivatedProgressColor);
                    setThumbTintList(this.mDefaultActivatedThumbColor);
                } else {
                    setProgressOverlapTintList(this.mOverlapActivatedProgressColor);
                    setThumbOverlapTintList(this.mOverlapActivatedThumbColor);
                }
            }
            updateBoundsForDualColor();
        }
    }

    private void updateGestureExclusionRects() {
        if (Build.VERSION.SDK_INT >= 29) {
            Drawable drawable = this.mThumb;
            if (drawable == null) {
                super.setSystemGestureExclusionRects(this.mUserGestureExclusionRects);
                return;
            }
            this.mGestureExclusionRects.clear();
            drawable.copyBounds(this.mThumbRect);
            this.mGestureExclusionRects.add(this.mThumbRect);
            this.mGestureExclusionRects.addAll(this.mUserGestureExclusionRects);
            super.setSystemGestureExclusionRects(this.mGestureExclusionRects);
        }
    }

    @SuppressLint("RestrictedApi")
    private void updateSplitProgress() {
        int i;
        int i2;
        int i3;
        if (this.mCurrentMode == 4) {
            Drawable drawable = this.mSplitProgress;
            Rect bounds = getCurrentDrawable().getBounds();
            if (drawable != null) {
                if (!this.mMirrorForRtl || !ViewUtils.isLayoutRtl(this)) {
                    i3 = getPaddingLeft();
                    i2 = bounds.top;
                    i = this.mThumbPosX;
                } else {
                    i3 = this.mThumbPosX;
                    i2 = bounds.top;
                    i = getWidth() - getPaddingRight();
                }
                drawable.setBounds(i3, i2, i, bounds.bottom);
            }
            int width = getWidth();
            int height = getHeight();
            Drawable drawable2 = this.mDivider;
            if (drawable2 != null) {
                float f = ((float) width) / 2.0f;
                float f2 = this.mDensity;
                float f3 = ((float) height) / 2.0f;
                drawable2.setBounds((int) (f - ((f2 * 4.0f) / 2.0f)), (int) (f3 - ((f2 * 22.0f) / 2.0f)), (int) (f + ((4.0f * f2) / 2.0f)), (int) (f3 + ((f2 * 22.0f) / 2.0f)));
            }
        }
    }

    private void updateThumbAndTrackPos(int i, int i2) {
        int i3;
        int i4;
        int i5 = this.mCurrentMode;
        if (i5 == 3 || i5 == 6) {
            updateThumbAndTrackPosInVertical(i, i2);
            return;
        }
        int paddingTop = (i2 - getPaddingTop()) - getPaddingBottom();
        Drawable currentDrawable = getCurrentDrawable();
        Drawable drawable = this.mThumb;
        int min = Math.min(this.mMaxHeight, paddingTop);
        int intrinsicHeight = drawable == null ? 0 : drawable.getIntrinsicHeight();
        if (intrinsicHeight > min) {
            i3 = (paddingTop - intrinsicHeight) / 2;
            i4 = ((intrinsicHeight - min) / 2) + i3;
        } else {
            int i6 = (paddingTop - min) / 2;
            int i7 = ((min - intrinsicHeight) / 2) + i6;
            i4 = i6;
            i3 = i7;
        }
        if (currentDrawable != null) {
            currentDrawable.setBounds(0, i4, (i - getPaddingRight()) - getPaddingLeft(), min + i4);
        }
        if (drawable != null) {
            setThumbPos(i, drawable, getScale(), i3);
        }
        updateSplitProgress();
    }

    private void updateThumbAndTrackPosInVertical(int i, int i2) {
        int i3;
        int i4;
        int paddingLeft = (i - getPaddingLeft()) - getPaddingRight();
        Drawable currentDrawable = getCurrentDrawable();
        Drawable drawable = this.mThumb;
        int min = Math.min(this.mMaxWidth, paddingLeft);
        int intrinsicWidth = drawable == null ? 0 : drawable.getIntrinsicWidth();
        if (intrinsicWidth > min) {
            i3 = (paddingLeft - intrinsicWidth) / 2;
            i4 = ((intrinsicWidth - min) / 2) + i3;
        } else {
            int i5 = (paddingLeft - min) / 2;
            i4 = i5;
            i3 = ((min - intrinsicWidth) / 2) + i5;
        }
        if (currentDrawable != null) {
            currentDrawable.setBounds(i4, 0, paddingLeft - i4, (i2 - getPaddingBottom()) - getPaddingTop());
        }
        if (drawable != null) {
            setThumbPosInVertical(i2, drawable, getScale(), i3);
        }
    }

    private void updateWarningMode(int i) {
        boolean z = true;
        if (this.mCurrentMode == 1) {
            if (i != getMax()) {
                z = false;
            }
            if (z) {
                setProgressOverlapTintList(this.mOverlapActivatedProgressColor);
                setThumbOverlapTintList(this.mOverlapActivatedThumbColor);
                return;
            }
            setProgressTintList(this.mDefaultActivatedProgressColor);
            setThumbTintList(this.mDefaultActivatedThumbColor);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean canUserSetProgress() {
        return !isIndeterminate() && isEnabled();
    }

    /* access modifiers changed from: package-private */
    public void drawThumb(Canvas canvas) {
        int i;
        float f;
        if (this.mThumb != null) {
            int save = canvas.save();
            int i2 = this.mCurrentMode;
            if (i2 == 3 || i2 == 6) {
                f = (float) getPaddingLeft();
                i = getPaddingTop() - this.mThumbOffset;
            } else {
                f = (float) (getPaddingLeft() - this.mThumbOffset);
                i = getPaddingTop();
            }
            canvas.translate(f, (float) i);
            this.mThumb.draw(canvas);
            canvas.restoreToCount(save);
        }
    }

    /* access modifiers changed from: protected */
    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP_PREFIX})
    public void drawTickMarks(Canvas canvas) {
        if (this.mTickMark != null) {
            int max = getMax() - getMin();
            int i = 1;
            if (max > 1) {
                int intrinsicWidth = this.mTickMark.getIntrinsicWidth();
                int intrinsicHeight = this.mTickMark.getIntrinsicHeight();
                int i2 = intrinsicWidth >= 0 ? intrinsicWidth / 2 : 1;
                if (intrinsicHeight >= 0) {
                    i = intrinsicHeight / 2;
                }
                this.mTickMark.setBounds(-i2, -i, i2, i);
                float width = ((float) ((getWidth() - getPaddingLeft()) - getPaddingRight())) / ((float) max);
                int save = canvas.save();
                canvas.translate((float) getPaddingLeft(), ((float) getHeight()) / 2.0f);
                for (int i3 = 0; i3 <= max; i3++) {
                    this.mTickMark.draw(canvas);
                    canvas.translate(width, 0.0f);
                }
                canvas.restoreToCount(save);
            }
        }
    }

    /* access modifiers changed from: package-private */
    @SuppressLint("RestrictedApi")
    @Override // de.dlyt.yanndroid.samsung.SeslProgressBar
    public void drawTrack(Canvas canvas) {
        int i;
        int i2;
        Drawable drawable = this.mThumb;
        if (drawable == null || !this.mSplitTrack) {
            super.drawTrack(canvas);
            drawTickMarks(canvas);
        } else {
            Rect opticalBounds = DrawableUtils.getOpticalBounds(drawable);
            Rect rect = this.mTempRect;
            drawable.copyBounds(rect);
            rect.offset(getPaddingLeft() - this.mThumbOffset, getPaddingTop());
            rect.left += opticalBounds.left;
            rect.right -= opticalBounds.right;
            int save = canvas.save();
            canvas.clipRect(rect, Region.Op.DIFFERENCE);
            super.drawTrack(canvas);
            drawTickMarks(canvas);
            canvas.restoreToCount(save);
        }
        if (!checkInvalidatedDualColorMode()) {
            canvas.save();
            if (!this.mMirrorForRtl || !ViewUtils.isLayoutRtl(this)) {
                canvas.translate((float) getPaddingLeft(), (float) getPaddingTop());
            } else {
                canvas.translate((float) (getWidth() - getPaddingRight()), (float) getPaddingTop());
                canvas.scale(-1.0f, 1.0f);
            }
            Rect bounds = this.mOverlapBackground.getBounds();
            Rect rect2 = this.mTempRect;
            this.mOverlapBackground.copyBounds(rect2);
            if (this.mIsSeamless) {
                i2 = Math.max(super.getProgress(), (int) (((float) this.mOverlapPoint) * SCALE_FACTOR));
                i = super.getMax();
            } else {
                i2 = Math.max(getProgress(), this.mOverlapPoint);
                i = getMax();
            }
            int i3 = this.mCurrentMode;
            if (i3 == 3 || i3 == 6) {
                rect2.bottom = (int) (((float) bounds.bottom) - (((float) bounds.height()) * (((float) i2) / ((float) i))));
            } else {
                rect2.left = (int) (((float) bounds.left) + (((float) bounds.width()) * (((float) i2) / ((float) i))));
            }
            canvas.clipRect(rect2);
            this.mOverlapBackground.draw(canvas);
            canvas.restore();
        }
    }

    @Override // de.dlyt.yanndroid.samsung.SeslProgressBar
    public void drawableHotspotChanged(float f, float f2) {
        super.drawableHotspotChanged(f, f2);
        Drawable drawable = this.mThumb;
        if (drawable != null) {
            DrawableCompat.setHotspot(drawable, f, f2);
        }
    }

    /* access modifiers changed from: protected */
    @Override // de.dlyt.yanndroid.samsung.SeslProgressBar
    public void drawableStateChanged() {
        Drawable drawable;
        super.drawableStateChanged();
        Drawable progressDrawable = getProgressDrawable();
        if (progressDrawable != null && this.mDisabledAlpha < 1.0f) {
            int i = isEnabled() ? 255 : (int) (this.mDisabledAlpha * 255.0f);
            progressDrawable.setAlpha(i);
            Drawable drawable2 = this.mOverlapBackground;
            if (drawable2 != null) {
                drawable2.setAlpha(i);
            }
        }
        if (this.mThumb != null && this.mHasThumbTint) {
            if (!isEnabled()) {
                DrawableCompat.setTintList(this.mThumb, null);
            } else {
                DrawableCompat.setTintList(this.mThumb, this.mDefaultActivatedThumbColor);
                updateDualColorMode();
            }
        }
        if (this.mSetDualColorMode && progressDrawable != null && progressDrawable.isStateful() && (drawable = this.mOverlapBackground) != null) {
            drawable.setState(getDrawableState());
        }
        Drawable drawable3 = this.mThumb;
        if (drawable3 != null && drawable3.isStateful() && drawable3.setState(getDrawableState())) {
            invalidateDrawable(drawable3);
        }
        Drawable drawable4 = this.mTickMark;
        if (drawable4 != null && drawable4.isStateful() && drawable4.setState(getDrawableState())) {
            invalidateDrawable(drawable4);
        }
    }

    @Override // de.dlyt.yanndroid.samsung.SeslProgressBar
    public CharSequence getAccessibilityClassName() {
        Log.d(TAG, "Stack:", new Throwable("stack dump"));
        return AbsSeekBar.class.getName();
    }

    public int getKeyProgressIncrement() {
        return this.mKeyProgressIncrement;
    }

    public void setKeyProgressIncrement(int i) {
        if (i < 0) {
            i = -i;
        }
        this.mKeyProgressIncrement = i;
    }

    @Override // de.dlyt.yanndroid.samsung.SeslProgressBar
    public synchronized int getMax() {
        return this.mIsSeamless ? Math.round(((float) super.getMax()) / SCALE_FACTOR) : super.getMax();
    }

    @Override // de.dlyt.yanndroid.samsung.SeslProgressBar
    public synchronized void setMax(int i) {
        if (this.mIsSeamless) {
            i = Math.round(((float) i) * SCALE_FACTOR);
        }
        super.setMax(i);
        this.mIsFirstSetProgress = true;
        int max = getMax() - getMin();
        if (this.mKeyProgressIncrement == 0 || max / this.mKeyProgressIncrement > 20) {
            setKeyProgressIncrement(Math.max(1, Math.round(((float) max) / 20.0f)));
        }
    }

    @Override // de.dlyt.yanndroid.samsung.SeslProgressBar
    public synchronized int getMin() {
        return this.mIsSeamless ? Math.round(((float) super.getMin()) / SCALE_FACTOR) : super.getMin();
    }

    @Override // de.dlyt.yanndroid.samsung.SeslProgressBar
    public synchronized void setMin(int i) {
        if (this.mIsSeamless) {
            i = Math.round(((float) i) * SCALE_FACTOR);
        }
        super.setMin(i);
        int max = getMax() - getMin();
        if (this.mKeyProgressIncrement == 0 || max / this.mKeyProgressIncrement > 20) {
            setKeyProgressIncrement(Math.max(1, Math.round(((float) max) / 20.0f)));
        }
    }

    @Override // de.dlyt.yanndroid.samsung.SeslProgressBar
    public synchronized int getProgress() {
        return this.mIsSeamless ? Math.round(((float) super.getProgress()) / SCALE_FACTOR) : super.getProgress();
    }

    @Override // de.dlyt.yanndroid.samsung.SeslProgressBar
    public synchronized void setProgress(int i) {
        if (this.mIsSeamless) {
            i = Math.round(((float) i) * SCALE_FACTOR);
        }
        super.setProgress(i);
    }

    public boolean getSplitTrack() {
        return this.mSplitTrack;
    }

    public void setSplitTrack(boolean z) {
        this.mSplitTrack = z;
        invalidate();
    }

    public Drawable getThumb() {
        return this.mThumb;
    }

    public void setThumb(Drawable drawable) {
        boolean z;
        Drawable drawable2 = this.mThumb;
        if (drawable2 == null || drawable == drawable2) {
            z = false;
        } else {
            drawable2.setCallback(null);
            z = true;
        }
        if (drawable != null) {
            drawable.setCallback(this);
            if (canResolveLayoutDirection()) {
                DrawableCompat.setLayoutDirection(drawable, ViewCompat.getLayoutDirection(this));
            }
            int i = this.mCurrentMode;
            this.mThumbOffset = ((i == 3 || i == 6) ? drawable.getIntrinsicHeight() : drawable.getIntrinsicWidth()) / 2;
            if (z && !(drawable.getIntrinsicWidth() == this.mThumb.getIntrinsicWidth() && drawable.getIntrinsicHeight() == this.mThumb.getIntrinsicHeight())) {
                requestLayout();
            }
        }
        this.mThumb = drawable;
        applyThumbTint();
        invalidate();
        if (z) {
            updateThumbAndTrackPos(getWidth(), getHeight());
            if (drawable != null && drawable.isStateful()) {
                drawable.setState(getDrawableState());
            }
        }
    }

    public Rect getThumbBounds() {
        Drawable drawable = this.mThumb;
        if (drawable != null) {
            return drawable.getBounds();
        }
        return null;
    }

    public int getThumbHeight() {
        return this.mThumb.getIntrinsicHeight();
    }

    public int getThumbOffset() {
        return this.mThumbOffset;
    }

    public void setThumbOffset(int i) {
        this.mThumbOffset = i;
        invalidate();
    }

    @Nullable
    public ColorStateList getThumbTintList() {
        return this.mThumbTintList;
    }

    public void setThumbTintList(@Nullable ColorStateList colorStateList) {
        this.mThumbTintList = colorStateList;
        this.mHasThumbTint = true;
        applyThumbTint();
        this.mDefaultActivatedThumbColor = colorStateList;
    }

    @Nullable
    public PorterDuff.Mode getThumbTintMode() {
        return this.mThumbTintMode;
    }

    public void setThumbTintMode(@Nullable PorterDuff.Mode mode) {
        this.mThumbTintMode = mode;
        this.mHasThumbTintMode = true;
        applyThumbTint();
    }

    public Drawable getTickMark() {
        return this.mTickMark;
    }

    public void setTickMark(Drawable drawable) {
        Drawable drawable2 = this.mTickMark;
        if (drawable2 != null) {
            drawable2.setCallback(null);
        }
        this.mTickMark = drawable;
        if (drawable != null) {
            drawable.setCallback(this);
            DrawableCompat.setLayoutDirection(drawable, ViewCompat.getLayoutDirection(this));
            if (drawable.isStateful()) {
                drawable.setState(getDrawableState());
            }
            applyTickMarkTint();
        }
        invalidate();
    }

    @Nullable
    public ColorStateList getTickMarkTintList() {
        return this.mTickMarkTintList;
    }

    public void setTickMarkTintList(@Nullable ColorStateList colorStateList) {
        this.mTickMarkTintList = colorStateList;
        this.mHasTickMarkTint = true;
        applyTickMarkTint();
    }

    @Nullable
    public PorterDuff.Mode getTickMarkTintMode() {
        return this.mTickMarkTintMode;
    }

    public void setTickMarkTintMode(@Nullable PorterDuff.Mode mode) {
        this.mTickMarkTintMode = mode;
        this.mHasTickMarkTintMode = true;
        applyTickMarkTint();
    }

    @Override // de.dlyt.yanndroid.samsung.SeslProgressBar
    public void jumpDrawablesToCurrentState() {
        super.jumpDrawablesToCurrentState();
        Drawable drawable = this.mThumb;
        if (drawable != null) {
            drawable.jumpToCurrentState();
        }
        Drawable drawable2 = this.mTickMark;
        if (drawable2 != null) {
            drawable2.jumpToCurrentState();
        }
    }

    /* access modifiers changed from: protected */
    @Override // de.dlyt.yanndroid.samsung.SeslProgressBar
    public synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (supportIsHoveringUIEnabled()) {
            int hoverPopupType = getHoverPopupType();
            if (isHoverPopupTypeUserCustom(hoverPopupType) && this.mPreviousHoverPopupType != hoverPopupType) {
                this.mPreviousHoverPopupType = hoverPopupType;
                setHoverPopupGravity(12849);
                setHoverPopupOffset(0, getMeasuredHeight() / 2);
                setHoverPopupDetectTime();
            }
        }
        if (this.mCurrentMode == 4) {
            this.mSplitProgress.draw(canvas);
            this.mDivider.draw(canvas);
        }
        if (!this.mIsTouchDisabled) {
            drawThumb(canvas);
        }
    }

    /* access modifiers changed from: package-private */
    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP_PREFIX})
    public void onHoverChanged(int i, int i2, int i3) {
    }

    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP_PREFIX})
    public boolean onHoverEvent(MotionEvent motionEvent) {
        if (supportIsHoveringUIEnabled()) {
            int action = motionEvent.getAction();
            int x = (int) motionEvent.getX();
            int y = (int) motionEvent.getY();
            if (action == 7) {
                trackHoverEvent(x);
                onHoverChanged(this.mHoveringLevel, x, y);
                if (isHoverPopupTypeUserCustom(getHoverPopupType())) {
                    setHoveringPoint((int) motionEvent.getRawX(), (int) motionEvent.getRawY());
                    updateHoverPopup();
                }
            } else if (action == 9) {
                trackHoverEvent(x);
                onStartTrackingHover(this.mHoveringLevel, x, y);
            } else if (action == 10) {
                onStopTrackingHover();
            }
        }
        return super.onHoverEvent(motionEvent);
    }

    @Override // de.dlyt.yanndroid.samsung.SeslProgressBar
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo accessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(accessibilityNodeInfo);
        if (isEnabled()) {
            int progress = getProgress();
            if (progress > getMin()) {
                accessibilityNodeInfo.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_BACKWARD);
            }
            if (progress < getMax()) {
                accessibilityNodeInfo.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void onKeyChange() {
    }

    @SuppressLint("RestrictedApi")
    public boolean onKeyDown(int i, android.view.KeyEvent keyEvent) {
        if (isEnabled()) {
            int i2 = this.mKeyProgressIncrement;
            int i3 = this.mCurrentMode;
            if (i3 != 3) {
                if (i3 != 6) {
                    if (i != 21) {
                        if (i != 22) {
                            if (i != 69) {
                                if (!(i == 70 || i == 81)) {
                                }
                            }
                        }
                        if (ViewUtils.isLayoutRtl(this)) {
                            i2 = -i2;
                        }
                        if (setProgressInternal(this.mIsSeamless ? Math.round(((float) (getProgress() + i2)) * SCALE_FACTOR) : i2 + getProgress(), true, true)) {
                            onKeyChange();
                            return true;
                        }
                    }
                    i2 = -i2;
                    if (ViewUtils.isLayoutRtl(this)) {
                        i2 = -i2;
                    }
                    if (this.mIsSeamless) {
                    }
                    if (setProgressInternal(this.mIsSeamless ? Math.round(((float) (getProgress() + i2)) * SCALE_FACTOR) : i2 + getProgress(), true, true)) {
                        onKeyChange();
                        return true;
                    }
                }
            }
            if (i != 19) {
                if (i == 20 || i == 69) {
                    i2 = -i2;
                } else if (!(i == 70 || i == 81)) {
                }
            }
            if (ViewUtils.isLayoutRtl(this)) {
                i2 = -i2;
            }
            if (setProgressInternal(this.mIsSeamless ? Math.round(((float) (getProgress() + i2)) * SCALE_FACTOR) : i2 + getProgress(), true, true)) {
                onKeyChange();
                return true;
            }
        }
        return super.onKeyDown(i, keyEvent);
    }

    /* access modifiers changed from: protected */
    @Override // de.dlyt.yanndroid.samsung.SeslProgressBar
    public synchronized void onMeasure(int i, int i2) {
        int i3;
        int i4;
        Drawable currentDrawable = getCurrentDrawable();
        if (currentDrawable != null) {
            if (this.mCurrentMode != 3) {
                if (this.mCurrentMode != 6) {
                    int intrinsicHeight = this.mThumb == null ? 0 : this.mThumb.getIntrinsicHeight();
                    i3 = Math.max(this.mMinWidth, Math.min(this.mMaxWidth, currentDrawable.getIntrinsicWidth()));
                    i4 = Math.max(intrinsicHeight, Math.max(this.mMinHeight, Math.min(this.mMaxHeight, currentDrawable.getIntrinsicHeight())));
                }
            }
            int intrinsicHeight2 = this.mThumb == null ? 0 : this.mThumb.getIntrinsicHeight();
            int max = Math.max(this.mMinWidth, Math.min(this.mMaxWidth, currentDrawable.getIntrinsicHeight()));
            i4 = Math.max(this.mMinHeight, Math.min(this.mMaxHeight, currentDrawable.getIntrinsicWidth()));
            i3 = Math.max(intrinsicHeight2, max);
        } else {
            i4 = 0;
            i3 = 0;
        }
        setMeasuredDimension(View.resolveSizeAndState(i3 + getPaddingLeft() + getPaddingRight(), i, 0), View.resolveSizeAndState(i4 + getPaddingTop() + getPaddingBottom(), i2, 0));
    }

    /* access modifiers changed from: package-private */
    @Override // de.dlyt.yanndroid.samsung.SeslProgressBar
    public void onProgressRefresh(float f, boolean z, int i) {
        int i2 = (int) (10000.0f * f);
        if (!(this.mUseMuteAnimation && !this.mIsFirstSetProgress && !this.mIsDraggingForSliding) || this.mCurrentProgressLevel == 0 || i2 != 0) {
            cancelMuteAnimation();
            this.mIsFirstSetProgress = false;
            this.mCurrentProgressLevel = i2;
            super.onProgressRefresh(f, z, i);
            Drawable drawable = this.mThumb;
            if (drawable != null) {
                setThumbPos(getWidth(), drawable, f, Integer.MIN_VALUE);
                invalidate();
                return;
            }
            return;
        }
        startMuteAnimation();
    }

    @Override // de.dlyt.yanndroid.samsung.SeslProgressBar
    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP_PREFIX})
    public void onResolveDrawables(int i) {
        super.onResolveDrawables(i);
        Drawable drawable = this.mThumb;
        if (drawable != null) {
            DrawableCompat.setLayoutDirection(drawable, i);
        }
    }

    public void onRtlPropertiesChanged(int i) {
        super.onRtlPropertiesChanged(i);
        Drawable drawable = this.mThumb;
        if (drawable != null) {
            setThumbPos(getWidth(), drawable, getScale(), Integer.MIN_VALUE);
            invalidate();
        }
    }

    /* access modifiers changed from: protected */
    @Override // de.dlyt.yanndroid.samsung.SeslProgressBar
    public void onSizeChanged(int i, int i2, int i3, int i4) {
        super.onSizeChanged(i, i2, i3, i4);
        updateThumbAndTrackPos(i, i2);
    }

    /* access modifiers changed from: protected */
    @Override // de.dlyt.yanndroid.samsung.SeslProgressBar
    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP_PREFIX})
    public void onSlidingRefresh(int i) {
        super.onSlidingRefresh(i);
        float f = ((float) i) / 10000.0f;
        Drawable drawable = this.mThumb;
        if (drawable != null) {
            setThumbPos(getWidth(), drawable, f, Integer.MIN_VALUE);
            invalidate();
        }
    }

    /* access modifiers changed from: package-private */
    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP_PREFIX})
    public void onStartTrackingHover(int i, int i2, int i3) {
    }

    /* access modifiers changed from: package-private */
    public void onStartTrackingTouch() {
        this.mIsDragging = true;
        ValueAnimator valueAnimator = this.mValueAnimator;
        if (valueAnimator != null) {
            valueAnimator.cancel();
        }
    }

    /* access modifiers changed from: package-private */
    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP_PREFIX})
    public void onStopTrackingHover() {
    }

    /* access modifiers changed from: package-private */
    public void onStopTrackingTouch() {
        this.mIsDragging = false;
        if (this.mIsSeamless && isPressed()) {
            ValueAnimator ofInt = ValueAnimator.ofInt(super.getProgress(), (int) (((float) Math.round(((float) super.getProgress()) / SCALE_FACTOR)) * SCALE_FACTOR));
            this.mValueAnimator = ofInt;
            ofInt.setDuration(300L);
            this.mValueAnimator.setInterpolator(SeslAnimationUtils.SINE_IN_OUT_90);
            this.mValueAnimator.start();
            this.mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                /* class androidx.appcompat.widget.SeslAbsSeekBar.AnonymousClass1 */

                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    SeslAbsSeekBar.this.callSuperSetProgress(((Integer) valueAnimator.getAnimatedValue()).intValue());
                }
            });
        } else if (this.mIsSeamless) {
            setProgress(Math.round(((float) super.getProgress()) / SCALE_FACTOR));
        }
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        int i;
        if (!this.mAllowedSeekBarAnimation || this.mIsTouchDisabled || !isEnabled()) {
            return false;
        }
        int action = motionEvent.getAction();
        if (action == 0) {
            this.mIsDraggingForSliding = false;
            int i2 = this.mCurrentMode;
            if (i2 == 5 || i2 == 6 || supportIsInScrollingContainer()) {
                this.mTouchDownX = motionEvent.getX();
                this.mTouchDownY = motionEvent.getY();
            } else {
                startDrag(motionEvent);
            }
        } else if (action == 1) {
            if (this.mIsDraggingForSliding) {
                this.mIsDraggingForSliding = false;
            }
            if (this.mIsDragging) {
                trackTouchEvent(motionEvent);
                onStopTrackingTouch();
                setPressed(false);
            } else {
                onStartTrackingTouch();
                trackTouchEvent(motionEvent);
                onStopTrackingTouch();
            }
            invalidate();
        } else if (action == 2) {
            this.mIsDraggingForSliding = true;
            if (this.mIsDragging) {
                trackTouchEvent(motionEvent);
            } else {
                float x = motionEvent.getX();
                float y = motionEvent.getY();
                int i3 = this.mCurrentMode;
                if (!(i3 == 3 || i3 == 6 || Math.abs(x - this.mTouchDownX) <= ((float) this.mScaledTouchSlop)) || (((i = this.mCurrentMode) == 3 || i == 6) && Math.abs(y - this.mTouchDownY) > ((float) this.mScaledTouchSlop))) {
                    startDrag(motionEvent);
                }
            }
        } else if (action == 3) {
            this.mIsDraggingForSliding = false;
            if (this.mIsDragging) {
                onStopTrackingTouch();
                setPressed(false);
            }
            invalidate();
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    @Override // de.dlyt.yanndroid.samsung.SeslProgressBar
    public void onVisualProgressChanged(int i, float f) {
        Drawable drawable;
        super.onVisualProgressChanged(i, f);
        if (i == R.id.progress && (drawable = this.mThumb) != null) {
            setThumbPos(getWidth(), drawable, f, Integer.MIN_VALUE);
            invalidate();
        }
    }

    public boolean performAccessibilityAction(int i, Bundle bundle) {
        if (super.performAccessibilityAction(i, bundle)) {
            return true;
        }
        if (!isEnabled()) {
            return false;
        }
        if (i == 4096 || i == 8192) {
            if (!canUserSetProgress()) {
                return false;
            }
            int max = Math.max(1, Math.round(((float) (getMax() - getMin())) / 20.0f));
            if (i == 8192) {
                max = -max;
            }
            if (!setProgressInternal(this.mIsSeamless ? Math.round(((float) (getProgress() + max)) * SCALE_FACTOR) : getProgress() + max, true, true)) {
                return false;
            }
            onKeyChange();
            return true;
        } else if (i != 16908349 || !canUserSetProgress() || bundle == null || !bundle.containsKey(AccessibilityNodeInfoCompat.ACTION_ARGUMENT_PROGRESS_VALUE)) {
            return false;
        } else {
            float f = bundle.getFloat(AccessibilityNodeInfoCompat.ACTION_ARGUMENT_PROGRESS_VALUE);
            return setProgressInternal(this.mIsSeamless ? Math.round(f * SCALE_FACTOR) : (int) f, true, true);
        }
    }

    public void setDualModeOverlapColor(int i, int i2) {
        ColorStateList colorToColorStateList = colorToColorStateList(i);
        ColorStateList colorToColorStateList2 = colorToColorStateList(i2);
        if (!colorToColorStateList.equals(this.mOverlapNormalProgressColor)) {
            this.mOverlapNormalProgressColor = colorToColorStateList;
        }
        if (!colorToColorStateList2.equals(this.mOverlapActivatedProgressColor)) {
            this.mOverlapActivatedProgressColor = colorToColorStateList2;
        }
        updateDualColorMode();
        invalidate();
    }

    @Override // de.dlyt.yanndroid.samsung.SeslProgressBar
    public void setMode(int i) {
        if (this.mCurrentMode != i || !this.mIsSetModeCalled) {
            super.setMode(i);
            if (i == 0) {
                setProgressTintList(this.mDefaultActivatedProgressColor);
                setThumbTintList(this.mDefaultActivatedThumbColor);
            } else if (i == 1) {
                updateWarningMode(getProgress());
            } else if (i == 3) {
                setThumb(getContext().getResources().getDrawable(this.mIsLightTheme ? R.drawable.sesl_scrubber_control_anim_light : R.drawable.sesl_scrubber_control_anim_dark));
            } else if (i == 4) {
                this.mSplitProgress = getContext().getResources().getDrawable(R.drawable.sesl_split_seekbar_primary_progress);
                this.mDivider = getContext().getResources().getDrawable(R.drawable.sesl_split_seekbar_vertical_bar);
                updateSplitProgress();
            } else if (i == 5) {
                initializeExpandMode();
            } else if (i == 6) {
                initializeExpandVerticalMode();
            }
            invalidate();
            this.mIsSetModeCalled = true;
            return;
        }
        Log.w(TAG, "Seekbar mode is already set. Do not call this method redundant");
    }

    public void setOverlapBackgroundForDualColor(int i) {
        ColorStateList colorToColorStateList = colorToColorStateList(i);
        if (!colorToColorStateList.equals(this.mOverlapNormalProgressColor)) {
            this.mOverlapNormalProgressColor = colorToColorStateList;
        }
        this.mOverlapActivatedProgressColor = this.mOverlapNormalProgressColor;
        this.mLargeFont = true;
    }

    public void setOverlapPointForDualColor(int i) {
        if (i < getMax()) {
            this.mSetDualColorMode = true;
            this.mOverlapPoint = i;
            if (i == -1) {
                setProgressTintList(this.mDefaultActivatedProgressColor);
                setThumbTintList(this.mDefaultActivatedThumbColor);
            } else {
                if (this.mOverlapBackground == null) {
                    initDualOverlapDrawable();
                }
                updateDualColorMode();
            }
            invalidate();
        }
    }

    @Override // de.dlyt.yanndroid.samsung.SeslProgressBar
    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP_PREFIX})
    public void setProgressDrawable(Drawable drawable) {
        super.setProgressDrawable(drawable);
    }

    @Override // de.dlyt.yanndroid.samsung.SeslProgressBar
    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP_PREFIX})
    public boolean setProgressInternal(int i, boolean z, boolean z2) {
        boolean progressInternal = super.setProgressInternal(i, z, z2);
        updateWarningMode(i);
        updateDualColorMode();
        return progressInternal;
    }

    @Override // de.dlyt.yanndroid.samsung.SeslProgressBar
    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP_PREFIX})
    public void setProgressTintList(@Nullable ColorStateList colorStateList) {
        super.setProgressTintList(colorStateList);
        this.mDefaultActivatedProgressColor = colorStateList;
    }

    public void setSeamless(boolean z) {
        if (this.mIsSeamless != z) {
            this.mIsSeamless = z;
            if (z) {
                super.setMax(Math.round(((float) super.getMax()) * SCALE_FACTOR));
                super.setMin(Math.round(((float) super.getMin()) * SCALE_FACTOR));
                super.setProgress(Math.round(((float) super.getProgress()) * SCALE_FACTOR));
                super.setSecondaryProgress(Math.round(((float) super.getSecondaryProgress()) * SCALE_FACTOR));
                return;
            }
            super.setProgress(Math.round(((float) super.getProgress()) / SCALE_FACTOR));
            super.setSecondaryProgress(Math.round(((float) super.getSecondaryProgress()) / SCALE_FACTOR));
            super.setMax(Math.round(((float) super.getMax()) / SCALE_FACTOR));
            super.setMin(Math.round(((float) super.getMin()) / SCALE_FACTOR));
        }
    }

    @Override // de.dlyt.yanndroid.samsung.SeslProgressBar
    public synchronized void setSecondaryProgress(int i) {
        if (this.mIsSeamless) {
            i = Math.round(((float) i) * SCALE_FACTOR);
        }
        super.setSecondaryProgress(i);
    }

    @SuppressLint("RestrictedApi")
    @Override // android.view.View
    public void setSystemGestureExclusionRects(@NonNull List<Rect> list) {
        Preconditions.checkNotNull(list, "rects must not be null");
        this.mUserGestureExclusionRects = list;
        updateGestureExclusionRects();
    }

    public void setThumbTintColor(int i) {
        ColorStateList colorToColorStateList = colorToColorStateList(i);
        if (!colorToColorStateList.equals(this.mDefaultActivatedThumbColor)) {
            this.mDefaultActivatedThumbColor = colorToColorStateList;
        }
    }

    /* access modifiers changed from: protected */
    @Override // de.dlyt.yanndroid.samsung.SeslProgressBar
    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP_PREFIX})
    public void updateDrawableBounds(int i, int i2) {
        super.updateDrawableBounds(i, i2);
        updateThumbAndTrackPos(i, i2);
        updateBoundsForDualColor();
    }

    public void updateHoverPopup() {
        if (IS_BASE_SDK_VERSION) {
            SeslHoverPopupWindowReflector.update(SeslViewReflector.semGetHoverPopup(this, true));
        }
    }

    /* access modifiers changed from: protected */
    @Override // de.dlyt.yanndroid.samsung.SeslProgressBar
    public boolean verifyDrawable(@NonNull Drawable drawable) {
        return drawable == this.mThumb || drawable == this.mTickMark || super.verifyDrawable(drawable);
    }

    /* access modifiers changed from: private */
    public class SliderDrawable extends Drawable {
        private final int ANIMATION_DURATION;
        private final Paint mPaint;
        private final float mSliderMaxWidth;
        private final float mSliderMinWidth;
        private final SliderState mState;
        int mAlpha;
        @ColorInt
        int mColor;
        ColorStateList mColorStateList;
        ValueAnimator mPressedAnimator;
        ValueAnimator mReleasedAnimator;
        private boolean mIsStateChanged;
        private boolean mIsVertical;
        private float mRadius;

        public SliderDrawable(SeslAbsSeekBar seslAbsSeekBar, float f, float f2, ColorStateList colorStateList) {
            this(f, f2, colorStateList, false);
        }

        public SliderDrawable(float f, float f2, ColorStateList colorStateList, boolean z) {
            this.mPaint = new Paint();
            this.ANIMATION_DURATION = ItemTouchHelper.Callback.DEFAULT_SWIPE_ANIMATION_DURATION;
            this.mIsStateChanged = false;
            this.mAlpha = 255;
            this.mState = new SliderState();
            this.mPaint.setStyle(Paint.Style.STROKE);
            this.mPaint.setStrokeCap(Paint.Cap.ROUND);
            this.mColorStateList = colorStateList;
            int defaultColor = colorStateList.getDefaultColor();
            this.mColor = defaultColor;
            this.mPaint.setColor(defaultColor);
            this.mPaint.setStrokeWidth(f);
            this.mSliderMinWidth = f;
            this.mSliderMaxWidth = f2;
            this.mRadius = f / 2.0f;
            this.mIsVertical = z;
            initAnimator();
        }

        private void initAnimator() {
            float f = this.mSliderMinWidth;
            float f2 = this.mSliderMaxWidth;
            ValueAnimator ofFloat = ValueAnimator.ofFloat(f, f2);
            this.mPressedAnimator = ofFloat;
            ofFloat.setDuration(250L);
            this.mPressedAnimator.setInterpolator(SeslAnimationUtils.SINE_IN_OUT_80);
            this.mPressedAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                /* class androidx.appcompat.widget.SeslAbsSeekBar.SliderDrawable.AnonymousClass1 */

                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    SliderDrawable.this.invalidateTrack(((Float) valueAnimator.getAnimatedValue()).floatValue());
                }
            });
            ValueAnimator ofFloat2 = ValueAnimator.ofFloat(f2, f);
            this.mReleasedAnimator = ofFloat2;
            ofFloat2.setDuration(250L);
            this.mReleasedAnimator.setInterpolator(SeslAnimationUtils.SINE_IN_OUT_80);
            this.mReleasedAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                /* class androidx.appcompat.widget.SeslAbsSeekBar.SliderDrawable.AnonymousClass2 */

                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    SliderDrawable.this.invalidateTrack(((Float) valueAnimator.getAnimatedValue()).floatValue());
                }
            });
        }

        private int modulateAlpha(int i, int i2) {
            return (i * (i2 + (i2 >>> 7))) >>> 8;
        }

        private void startPressedAnimation() {
            if (!this.mPressedAnimator.isRunning()) {
                if (this.mReleasedAnimator.isRunning()) {
                    this.mReleasedAnimator.cancel();
                }
                this.mPressedAnimator.setFloatValues(this.mSliderMinWidth, this.mSliderMaxWidth);
                this.mPressedAnimator.start();
            }
        }

        private void startReleasedAnimation() {
            if (!this.mReleasedAnimator.isRunning()) {
                if (this.mPressedAnimator.isRunning()) {
                    this.mPressedAnimator.cancel();
                }
                this.mReleasedAnimator.setFloatValues(this.mSliderMaxWidth, this.mSliderMinWidth);
                this.mReleasedAnimator.start();
            }
        }

        private void startSliderAnimation(boolean z) {
            if (this.mIsStateChanged != z) {
                if (z) {
                    startPressedAnimation();
                } else {
                    startReleasedAnimation();
                }
                this.mIsStateChanged = z;
            }
        }

        public void draw(Canvas canvas) {
            int alpha = this.mPaint.getAlpha();
            this.mPaint.setAlpha(modulateAlpha(alpha, this.mAlpha));
            canvas.save();
            if (!this.mIsVertical) {
                float f = this.mRadius;
                canvas.drawLine(f, ((float) SeslAbsSeekBar.this.getHeight()) / 2.0f, ((float) ((SeslAbsSeekBar.this.getWidth() - SeslAbsSeekBar.this.getPaddingLeft()) - SeslAbsSeekBar.this.getPaddingRight())) - f, ((float) SeslAbsSeekBar.this.getHeight()) / 2.0f, this.mPaint);
            } else {
                canvas.drawLine(((float) SeslAbsSeekBar.this.getWidth()) / 2.0f, ((float) ((SeslAbsSeekBar.this.getHeight() - SeslAbsSeekBar.this.getPaddingTop()) - SeslAbsSeekBar.this.getPaddingBottom())) - this.mRadius, ((float) SeslAbsSeekBar.this.getWidth()) / 2.0f, this.mRadius, this.mPaint);
            }
            canvas.restore();
            this.mPaint.setAlpha(alpha);
        }

        @Nullable
        public ConstantState getConstantState() {
            return this.mState;
        }

        public int getIntrinsicHeight() {
            return (int) this.mSliderMaxWidth;
        }

        public int getIntrinsicWidth() {
            return (int) this.mSliderMaxWidth;
        }

        public int getOpacity() {
            Paint paint = this.mPaint;
            if (paint.getXfermode() != null) {
                return PixelFormat.TRANSLUCENT;
            }
            int alpha = paint.getAlpha();
            if (alpha == 0) {
                return PixelFormat.TRANSPARENT;
            }
            return alpha == 255 ? PixelFormat.OPAQUE : PixelFormat.TRANSLUCENT;
        }

        /* access modifiers changed from: package-private */
        public void invalidateTrack(float f) {
            setStrokeWidth(f);
            invalidateSelf();
        }

        public boolean isStateful() {
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean onStateChange(int[] iArr) {
            boolean onStateChange = super.onStateChange(iArr);
            int colorForState = this.mColorStateList.getColorForState(iArr, this.mColor);
            if (this.mColor != colorForState) {
                this.mColor = colorForState;
                this.mPaint.setColor(colorForState);
                invalidateSelf();
            }
            boolean z = false;
            boolean z2 = false;
            boolean z3 = false;
            for (int i : iArr) {
                if (i == 16842910) {
                    z2 = true;
                } else if (i == 16842919) {
                    z3 = true;
                }
            }
            if (z2 && z3) {
                z = true;
            }
            startSliderAnimation(z);
            return onStateChange;
        }

        public void setAlpha(int i) {
            this.mAlpha = i;
            invalidateSelf();
        }

        public void setColorFilter(@Nullable ColorFilter colorFilter) {
            this.mPaint.setColorFilter(colorFilter);
            invalidateSelf();
        }

        public void setStrokeWidth(float f) {
            this.mPaint.setStrokeWidth(f);
            this.mRadius = f / 2.0f;
        }

        public void setTintList(@Nullable ColorStateList colorStateList) {
            super.setTintList(colorStateList);
            if (colorStateList != null) {
                this.mColorStateList = colorStateList;
                int defaultColor = colorStateList.getDefaultColor();
                this.mColor = defaultColor;
                this.mPaint.setColor(defaultColor);
                invalidateSelf();
            }
        }

        private class SliderState extends ConstantState {
            private SliderState() {
            }

            public int getChangingConfigurations() {
                return 0;
            }

            @NonNull
            public Drawable newDrawable() {
                return SliderDrawable.this;
            }
        }
    }

    /* access modifiers changed from: private */
    public class ThumbDrawable extends Drawable {
        private final int PRESSED_DURATION = 100;
        private final int RELEASED_DURATION = 300;
        private final Paint mPaint = new Paint(1);
        private final int mRadius;
        @ColorInt
        int mColor;
        private int mAlpha = 255;
        private ColorStateList mColorStateList;
        private boolean mIsStateChanged = false;
        private boolean mIsVertical = false;
        private int mRadiusForAni;
        private ValueAnimator mThumbPressed;
        private ValueAnimator mThumbReleased;

        public ThumbDrawable(int i, ColorStateList colorStateList, boolean z) {
            this.mRadiusForAni = i;
            this.mRadius = i;
            this.mColorStateList = colorStateList;
            this.mColor = colorStateList.getDefaultColor();
            this.mPaint.setStyle(Paint.Style.FILL);
            this.mPaint.setColor(this.mColor);
            this.mIsVertical = z;
            initAnimation();
        }

        private int modulateAlpha(int i, int i2) {
            return (i * (i2 + (i2 >>> 7))) >>> 8;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setRadius(int i) {
            this.mRadiusForAni = i;
        }

        private void startPressedAnimation() {
            if (!this.mThumbPressed.isRunning()) {
                if (this.mThumbReleased.isRunning()) {
                    this.mThumbReleased.cancel();
                }
                this.mThumbPressed.start();
            }
        }

        private void startReleasedAnimation() {
            if (!this.mThumbReleased.isRunning()) {
                if (this.mThumbPressed.isRunning()) {
                    this.mThumbPressed.cancel();
                }
                this.mThumbReleased.start();
            }
        }

        private void startThumbAnimation(boolean z) {
            if (this.mIsStateChanged != z) {
                if (z) {
                    startPressedAnimation();
                } else {
                    startReleasedAnimation();
                }
                this.mIsStateChanged = z;
            }
        }

        public void draw(@NonNull Canvas canvas) {
            int alpha = this.mPaint.getAlpha();
            this.mPaint.setAlpha(modulateAlpha(alpha, this.mAlpha));
            canvas.save();
            if (!this.mIsVertical) {
                canvas.drawCircle((float) SeslAbsSeekBar.this.mThumbPosX, ((float) SeslAbsSeekBar.this.getHeight()) / 2.0f, (float) this.mRadiusForAni, this.mPaint);
            } else {
                canvas.drawCircle(((float) SeslAbsSeekBar.this.getWidth()) / 2.0f, (float) SeslAbsSeekBar.this.mThumbPosX, (float) this.mRadiusForAni, this.mPaint);
            }
            canvas.restore();
            this.mPaint.setAlpha(alpha);
        }

        public int getIntrinsicHeight() {
            return this.mRadius * 2;
        }

        public int getIntrinsicWidth() {
            return this.mRadius * 2;
        }

        public int getOpacity() {
            Paint paint = this.mPaint;
            if (paint.getXfermode() != null) {
                return PixelFormat.TRANSLUCENT;
            }
            int alpha = paint.getAlpha();
            if (alpha == 0) {
                return PixelFormat.TRANSPARENT;
            }
            return alpha == 255 ? PixelFormat.OPAQUE : PixelFormat.TRANSLUCENT;
        }

        /* access modifiers changed from: package-private */
        public void initAnimation() {
            ValueAnimator ofFloat = ValueAnimator.ofFloat((float) this.mRadius, 0.0f);
            this.mThumbPressed = ofFloat;
            ofFloat.setDuration(100L);
            this.mThumbPressed.setInterpolator(new LinearInterpolator());
            this.mThumbPressed.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                /* class androidx.appcompat.widget.SeslAbsSeekBar.ThumbDrawable.AnonymousClass1 */

                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    ThumbDrawable.this.setRadius((int) ((Float) valueAnimator.getAnimatedValue()).floatValue());
                    ThumbDrawable.this.invalidateSelf();
                }
            });
            ValueAnimator ofFloat2 = ValueAnimator.ofFloat(0.0f, (float) this.mRadius);
            this.mThumbReleased = ofFloat2;
            ofFloat2.setDuration(300L);
            this.mThumbReleased.setInterpolator(SeslAnimationUtils.SINE_IN_OUT_90);
            this.mThumbReleased.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                /* class androidx.appcompat.widget.SeslAbsSeekBar.ThumbDrawable.AnonymousClass2 */

                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    ThumbDrawable.this.setRadius((int) ((Float) valueAnimator.getAnimatedValue()).floatValue());
                    ThumbDrawable.this.invalidateSelf();
                }
            });
        }

        public boolean isStateful() {
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean onStateChange(int[] iArr) {
            boolean onStateChange = super.onStateChange(iArr);
            int colorForState = this.mColorStateList.getColorForState(iArr, this.mColor);
            if (this.mColor != colorForState) {
                this.mColor = colorForState;
                this.mPaint.setColor(colorForState);
                invalidateSelf();
            }
            boolean z = false;
            boolean z2 = false;
            boolean z3 = false;
            for (int i : iArr) {
                if (i == 16842910) {
                    z2 = true;
                } else if (i == 16842919) {
                    z3 = true;
                }
            }
            if (z2 && z3) {
                z = true;
            }
            startThumbAnimation(z);
            return onStateChange;
        }

        public void setAlpha(int i) {
            this.mAlpha = i;
            invalidateSelf();
        }

        public void setColorFilter(@Nullable ColorFilter colorFilter) {
            this.mPaint.setColorFilter(colorFilter);
            invalidateSelf();
        }

        public void setTintList(@Nullable ColorStateList colorStateList) {
            super.setTintList(colorStateList);
            if (colorStateList != null) {
                this.mColorStateList = colorStateList;
                int colorForState = colorStateList.getColorForState(SeslAbsSeekBar.this.getDrawableState(), this.mColor);
                this.mColor = colorForState;
                this.mPaint.setColor(colorForState);
                invalidateSelf();
            }
        }
    }
}
