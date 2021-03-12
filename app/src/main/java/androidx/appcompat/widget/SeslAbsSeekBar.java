package androidx.appcompat.widget;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.LinearInterpolator;
import android.widget.AbsSeekBar;
import android.widget.SeekBar;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.reflect.view.SeslViewReflector;
import androidx.reflect.widget.SeslHoverPopupWindowReflector;
import java.util.ArrayList;

import de.dlyt.yanndroid.samsung.R;
import de.dlyt.yanndroid.samsung.RdotStyleable;

public abstract class SeslAbsSeekBar extends SeslProgressBar {
    private static final String CURRENT_SEC_ACTIVE_THEMEPACKAGE = "current_sec_active_themepackage";
    private static final int HOVER_DETECT_TIME = 200;
    private static final int HOVER_POPUP_WINDOW_GRAVITY_CENTER_HORIZONTAL_ON_POINT = 513;
    private static final int HOVER_POPUP_WINDOW_GRAVITY_TOP_ABOVE = 12336;
    private static final boolean IS_BASE_SDK_VERSION = (Build.VERSION.SDK_INT <= 23);
    private static final int MUTE_VIB_DISTANCE_LVL = 400;
    private static final int MUTE_VIB_DURATION = 500;
    private static final int MUTE_VIB_TOTAL = 4;
    private static final int NO_ALPHA = 255;
    private static final String TAG = "SeslAbsSeekBar";
    private boolean mAllowedSeekBarAnimation;
    private int mCurrentProgressLevel;
    private ColorStateList mDefaultActivatedProgressColor;
    private ColorStateList mDefaultActivatedThumbColor;
    private ColorStateList mDefaultNormalProgressColor;
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
    private boolean mIsTouchDisabled;
    boolean mIsUserSeekable;
    private int mKeyProgressIncrement;
    private boolean mLargeFont;
    private AnimatorSet mMuteAnimationSet;
    private ColorStateList mOverlapActivatedProgressColor;
    private ColorStateList mOverlapActivatedThumbColor;
    private Drawable mOverlapBackground;
    private ColorStateList mOverlapNormalProgressColor;
    private int mOverlapPoint;
    private Drawable mOverlapPrimary;
    private Paint mPaint;
    private int mPreviousHoverPopupType;
    private int mScaledTouchSlop;
    private Drawable mSplitProgress;
    private boolean mSplitTrack;
    private final Rect mTempRect;
    private Drawable mThumb;
    private int mThumbOffset;
    private int mThumbPosX;
    private float mThumbPosXFloat;
    private int mThumbPosY;
    private float mThumbPosYFloat;
    private ColorStateList mThumbTintList;
    private PorterDuff.Mode mThumbTintMode;
    private Drawable mTickMark;
    private ColorStateList mTickMarkTintList;
    private PorterDuff.Mode mTickMarkTintMode;
    private float mTouchDownX;
    private float mTouchDownY;
    float mTouchProgressOffset;
    private boolean mUseMuteAnimation;

    /* access modifiers changed from: package-private */
    public void onHoverChanged(int i, int i2, int i3) {
    }

    /* access modifiers changed from: package-private */
    public void onKeyChange() {
    }

    /* access modifiers changed from: package-private */
    public void onStartTrackingHover(int i, int i2, int i3) {
    }

    /* access modifiers changed from: package-private */
    public void onStopTrackingHover() {
    }

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
        this.mHoveringLevel = 0;
        this.mOverlapPoint = -1;
        this.mAllowedSeekBarAnimation = false;
        this.mUseMuteAnimation = false;
        this.mIsFirstSetProgress = false;
        this.mIsDraggingForSliding = false;
        this.mLargeFont = false;
        this.mIsTouchDisabled = false;
        this.mPreviousHoverPopupType = 0;
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
        this.mHoveringLevel = 0;
        this.mOverlapPoint = -1;
        this.mAllowedSeekBarAnimation = false;
        this.mUseMuteAnimation = false;
        this.mIsFirstSetProgress = false;
        this.mIsDraggingForSliding = false;
        this.mLargeFont = false;
        this.mIsTouchDisabled = false;
        this.mPreviousHoverPopupType = 0;
    }

    public SeslAbsSeekBar(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public SeslAbsSeekBar(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mTempRect = new Rect();
        this.mThumbTintList = null;
        this.mThumbTintMode = null;
        boolean z = false;
        this.mHasThumbTint = false;
        this.mHasThumbTintMode = false;
        this.mTickMarkTintList = null;
        this.mTickMarkTintMode = null;
        this.mHasTickMarkTint = false;
        this.mHasTickMarkTintMode = false;
        this.mIsUserSeekable = true;
        this.mKeyProgressIncrement = 1;
        this.mHoveringLevel = 0;
        this.mOverlapPoint = -1;
        this.mAllowedSeekBarAnimation = false;
        this.mUseMuteAnimation = false;
        this.mIsFirstSetProgress = false;
        this.mIsDraggingForSliding = false;
        this.mLargeFont = false;
        this.mIsTouchDisabled = false;
        this.mPreviousHoverPopupType = 0;
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.AppCompatSeekBar, i, i2);
        setThumb(obtainStyledAttributes.getDrawable(R.styleable.AppCompatSeekBar_android_thumb));
        if (obtainStyledAttributes.hasValue(RdotStyleable.styleable.AppCompatSeekBar_android_thumbTintMode)) {
            this.mThumbTintMode = DrawableUtils.parseTintMode(obtainStyledAttributes.getInt(RdotStyleable.styleable.AppCompatSeekBar_android_thumbTintMode, -1), this.mThumbTintMode);
            this.mHasThumbTintMode = true;
        }
        if (obtainStyledAttributes.hasValue(RdotStyleable.styleable.AppCompatSeekBar_android_thumbTint)) {
            this.mThumbTintList = obtainStyledAttributes.getColorStateList(RdotStyleable.styleable.AppCompatSeekBar_android_thumbTint);
            this.mHasThumbTint = true;
        }
        setTickMark(obtainStyledAttributes.getDrawable(R.styleable.AppCompatSeekBar_tickMark));
        if (obtainStyledAttributes.hasValue(R.styleable.AppCompatSeekBar_tickMarkTintMode)) {
            this.mTickMarkTintMode = DrawableUtils.parseTintMode(obtainStyledAttributes.getInt(R.styleable.AppCompatSeekBar_tickMarkTintMode, -1), this.mTickMarkTintMode);
            this.mHasTickMarkTintMode = true;
        }
        if (obtainStyledAttributes.hasValue(R.styleable.AppCompatSeekBar_tickMarkTint)) {
            this.mTickMarkTintList = obtainStyledAttributes.getColorStateList(R.styleable.AppCompatSeekBar_tickMarkTint);
            this.mHasTickMarkTint = true;
        }
        this.mSplitTrack = obtainStyledAttributes.getBoolean(RdotStyleable.styleable.AppCompatSeekBar_android_splitTrack, false);
        setThumbOffset(obtainStyledAttributes.getDimensionPixelOffset(RdotStyleable.styleable.AppCompatSeekBar_android_thumbOffset, getThumbOffset()));
        boolean z2 = obtainStyledAttributes.getBoolean(RdotStyleable.styleable.AppCompatSeekBar_useDisabledAlpha, true);
        obtainStyledAttributes.recycle();
        if (z2) {
            TypedArray obtainStyledAttributes2 = context.obtainStyledAttributes(attributeSet, R.styleable.AppCompatTheme, 0, 0);
            this.mDisabledAlpha = obtainStyledAttributes2.getFloat(RdotStyleable.styleable.AppCompatTheme_android_disabledAlpha, 0.5f);
            obtainStyledAttributes2.recycle();
        } else {
            this.mDisabledAlpha = 1.0f;
        }
        applyThumbTint();
        applyTickMarkTint();
        this.mScaledTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.isLightTheme, typedValue, true);
        this.mIsLightTheme = typedValue.data != 0 ? true : z;
        Resources resources = context.getResources();
        this.mDefaultNormalProgressColor = colorToColorStateList(resources.getColor(R.color.sesl_seekbar_control_color_normal));
        this.mDefaultActivatedProgressColor = colorToColorStateList(resources.getColor(R.color.sesl_seekbar_control_color_activated));
        this.mOverlapNormalProgressColor = colorToColorStateList(resources.getColor(this.mIsLightTheme ? R.color.sesl_seekbar_overlap_color_normal : R.color.sesl_seekbar_overlap_color_normal_dark));
        ColorStateList colorToColorStateList = colorToColorStateList(resources.getColor(R.color.sesl_seekbar_overlap_color_activated));
        this.mOverlapActivatedProgressColor = colorToColorStateList;
        this.mOverlapActivatedThumbColor = colorToColorStateList;
        ColorStateList thumbTintList = getThumbTintList();
        this.mDefaultActivatedThumbColor = thumbTintList;
        if (thumbTintList == null) {
            this.mDefaultActivatedThumbColor = colorToColorStateList(resources.getColor(R.color.sesl_thumb_control_color_activated));
        }
        boolean z3 = resources.getBoolean(R.bool.sesl_seekbar_sliding_animation);
        this.mAllowedSeekBarAnimation = z3;
        if (z3) {
            initMuteAnimation();
        }
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
            if (this.mCurrentMode == 3) {
                this.mThumbOffset = drawable.getIntrinsicHeight() / 2;
            } else {
                this.mThumbOffset = drawable.getIntrinsicWidth() / 2;
            }
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

    public Drawable getThumb() {
        return this.mThumb;
    }

    public void setThumbTintColor(int i) {
        ColorStateList colorToColorStateList = colorToColorStateList(i);
        if (!colorToColorStateList.equals(this.mDefaultActivatedThumbColor)) {
            this.mDefaultActivatedThumbColor = colorToColorStateList;
        }
    }

    public void setThumbTintList(ColorStateList colorStateList) {
        this.mThumbTintList = colorStateList;
        this.mHasThumbTint = true;
        applyThumbTint();
        this.mDefaultActivatedThumbColor = colorStateList;
    }

    public ColorStateList getThumbTintList() {
        return this.mThumbTintList;
    }

    public void setThumbTintMode(PorterDuff.Mode mode) {
        this.mThumbTintMode = mode;
        this.mHasThumbTintMode = true;
        applyThumbTint();
    }

    public PorterDuff.Mode getThumbTintMode() {
        return this.mThumbTintMode;
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

    public int getThumbOffset() {
        return this.mThumbOffset;
    }

    public void setThumbOffset(int i) {
        this.mThumbOffset = i;
        invalidate();
    }

    public void setSplitTrack(boolean z) {
        this.mSplitTrack = z;
        invalidate();
    }

    public boolean getSplitTrack() {
        return this.mSplitTrack;
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

    public Drawable getTickMark() {
        return this.mTickMark;
    }

    public void setTickMarkTintList(ColorStateList colorStateList) {
        this.mTickMarkTintList = colorStateList;
        this.mHasTickMarkTint = true;
        applyTickMarkTint();
    }

    public ColorStateList getTickMarkTintList() {
        return this.mTickMarkTintList;
    }

    public void setTickMarkTintMode(PorterDuff.Mode mode) {
        this.mTickMarkTintMode = mode;
        this.mHasTickMarkTintMode = true;
        applyTickMarkTint();
    }

    public PorterDuff.Mode getTickMarkTintMode() {
        return this.mTickMarkTintMode;
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

    public void setKeyProgressIncrement(int i) {
        if (i < 0) {
            i = -i;
        }
        this.mKeyProgressIncrement = i;
    }

    public int getKeyProgressIncrement() {
        return this.mKeyProgressIncrement;
    }

    @Override // androidx.appcompat.widget.SeslProgressBar
    public synchronized void setMin(int i) {
        super.setMin(i);
        int max = getMax() - getMin();
        if (this.mKeyProgressIncrement == 0 || max / this.mKeyProgressIncrement > 20) {
            setKeyProgressIncrement(Math.max(1, Math.round(((float) max) / 20.0f)));
        }
    }

    @Override // androidx.appcompat.widget.SeslProgressBar
    public synchronized void setMax(int i) {
        super.setMax(i);
        this.mIsFirstSetProgress = true;
        int max = getMax() - getMin();
        if (this.mKeyProgressIncrement == 0 || max / this.mKeyProgressIncrement > 20) {
            setKeyProgressIncrement(Math.max(1, Math.round(((float) max) / 20.0f)));
        }
    }

    /* access modifiers changed from: protected */
    @Override // androidx.appcompat.widget.SeslProgressBar
    public boolean verifyDrawable(Drawable drawable) {
        return drawable == this.mThumb || drawable == this.mTickMark || super.verifyDrawable(drawable);
    }

    @Override // androidx.appcompat.widget.SeslProgressBar
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
    @Override // androidx.appcompat.widget.SeslProgressBar
    public void drawableStateChanged() {
        super.drawableStateChanged();
        Drawable progressDrawable = getProgressDrawable();
        if (progressDrawable != null && this.mDisabledAlpha < 1.0f) {
            int i = isEnabled() ? 255 : (int) (this.mDisabledAlpha * 255.0f);
            progressDrawable.setAlpha(i);
            Drawable drawable = this.mOverlapPrimary;
            if (!(drawable == null || this.mOverlapBackground == null)) {
                drawable.setAlpha(i);
                this.mOverlapBackground.setAlpha(i);
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
        Drawable drawable2 = this.mThumb;
        if (drawable2 != null && drawable2.isStateful() && drawable2.setState(getDrawableState())) {
            invalidateDrawable(drawable2);
        }
        Drawable drawable3 = this.mTickMark;
        if (drawable3 != null && drawable3.isStateful() && drawable3.setState(getDrawableState())) {
            invalidateDrawable(drawable3);
        }
    }

    @Override // androidx.appcompat.widget.SeslProgressBar
    public void drawableHotspotChanged(float f, float f2) {
        super.drawableHotspotChanged(f, f2);
        Drawable drawable = this.mThumb;
        if (drawable != null) {
            DrawableCompat.setHotspot(drawable, f, f2);
        }
    }

    /* access modifiers changed from: package-private */
    @Override // androidx.appcompat.widget.SeslProgressBar
    public void onVisualProgressChanged(int i, float f) {
        Drawable drawable;
        super.onVisualProgressChanged(i, f);
        if (i == R.id.progress && (drawable = this.mThumb) != null) {
            setThumbPos(getWidth(), drawable, f, Integer.MIN_VALUE);
            invalidate();
        }
    }

    /* access modifiers changed from: package-private */
    @Override // androidx.appcompat.widget.SeslProgressBar
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

    /* access modifiers changed from: protected */
    @Override // androidx.appcompat.widget.SeslProgressBar
    public void onSizeChanged(int i, int i2, int i3, int i4) {
        super.onSizeChanged(i, i2, i3, i4);
        updateThumbAndTrackPos(i, i2);
    }

    private void updateThumbAndTrackPos(int i, int i2) {
        int i3;
        int i4;
        int i5;
        if (this.mCurrentMode == 3) {
            updateThumbAndTrackPosInVertical(i, i2);
            return;
        }
        int paddingTop = (i2 - getPaddingTop()) - getPaddingBottom();
        Drawable currentDrawable = getCurrentDrawable();
        Drawable drawable = this.mThumb;
        int min = Math.min(this.mMaxHeight, paddingTop);
        if (drawable == null) {
            i3 = 0;
        } else {
            i3 = drawable.getIntrinsicHeight();
        }
        if (i3 > min) {
            i4 = (paddingTop - i3) / 2;
            i5 = ((i3 - min) / 2) + i4;
        } else {
            int i6 = (paddingTop - min) / 2;
            int i7 = ((min - i3) / 2) + i6;
            i5 = i6;
            i4 = i7;
        }
        if (currentDrawable != null) {
            currentDrawable.setBounds(0, i5, (i - getPaddingRight()) - getPaddingLeft(), min + i5);
        }
        if (drawable != null) {
            setThumbPos(i, drawable, getScale(), i4);
        }
        updateSplitProgress();
    }

    private void updateThumbAndTrackPosInVertical(int i, int i2) {
        int i3;
        int i4;
        int i5;
        int paddingLeft = (i - getPaddingLeft()) - getPaddingRight();
        Drawable currentDrawable = getCurrentDrawable();
        Drawable drawable = this.mThumb;
        int min = Math.min(this.mMaxWidth, paddingLeft);
        if (drawable == null) {
            i3 = 0;
        } else {
            i3 = drawable.getIntrinsicWidth();
        }
        if (i3 > min) {
            i4 = (paddingLeft - i3) / 2;
            i5 = ((i3 - min) / 2) + i4;
        } else {
            int i6 = (paddingLeft - min) / 2;
            i5 = i6;
            i4 = ((min - i3) / 2) + i6;
        }
        if (currentDrawable != null) {
            currentDrawable.setBounds(i5, 0, paddingLeft - i5, (i2 - getPaddingBottom()) - getPaddingTop());
        }
        if (drawable != null) {
            setThumbPosInVertical(i2, drawable, getScale(), i4);
        }
    }

    private float getScale() {
        int min = getMin();
        int max = getMax() - min;
        if (max > 0) {
            return ((float) (getProgress() - min)) / ((float) max);
        }
        return 0.0f;
    }

    private void setThumbPos(int i, Drawable drawable, float f, int i2) {
        int i3;
        if (this.mCurrentMode == 3) {
            setThumbPosInVertical(getHeight(), drawable, f, i2);
            return;
        }
        int paddingLeft = (i - getPaddingLeft()) - getPaddingRight();
        int intrinsicWidth = drawable.getIntrinsicWidth();
        int intrinsicHeight = drawable.getIntrinsicHeight();
        int i4 = (paddingLeft - intrinsicWidth) + (this.mThumbOffset * 2);
        int i5 = (int) ((f * ((float) i4)) + 0.5f);
        if (i2 == Integer.MIN_VALUE) {
            Rect bounds = drawable.getBounds();
            int i6 = bounds.top;
            i3 = bounds.bottom;
            i2 = i6;
        } else {
            i3 = i2 + intrinsicHeight;
        }
        if (this.mMirrorForRtl && ViewUtils.isLayoutRtl(this)) {
            i5 = i4 - i5;
        }
        int i7 = i5 + intrinsicWidth;
        Drawable background = getBackground();
        if (background != null) {
            int paddingLeft2 = getPaddingLeft() - this.mThumbOffset;
            int paddingTop = getPaddingTop();
            DrawableCompat.setHotspotBounds(background, i5 + paddingLeft2, i2 + paddingTop, paddingLeft2 + i7, paddingTop + i3);
        }
        drawable.setBounds(i5, i2, i7, i3);
        this.mThumbPosX = i5 + getPaddingLeft();
        this.mThumbPosY = (intrinsicHeight / 2) + i2 + getPaddingTop();
        this.mThumbPosXFloat = (((float) this.mThumbPosX) + (((float) intrinsicWidth) / 2.0f)) - ((float) this.mThumbOffset);
        this.mThumbPosYFloat = ((float) i2) + (((float) intrinsicHeight) / 2.0f) + ((float) getPaddingTop());
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
        this.mThumbPosX = (intrinsicHeight / 2) + i2 + getPaddingLeft();
        this.mThumbPosY = i7 + getPaddingTop();
        this.mThumbPosXFloat = ((float) i2) + (((float) intrinsicHeight) / 2.0f) + ((float) getPaddingLeft());
        this.mThumbPosYFloat = (float) this.mThumbPosY;
    }

    private void updateSplitProgress() {
        if (this.mCurrentMode == 4) {
            Drawable drawable = this.mSplitProgress;
            Rect bounds = getCurrentDrawable().getBounds();
            if (drawable != null) {
                if (!this.mMirrorForRtl || !ViewUtils.isLayoutRtl(this)) {
                    drawable.setBounds(getPaddingLeft(), bounds.top, this.mThumbPosX, bounds.bottom);
                } else {
                    drawable.setBounds(this.mThumbPosX, bounds.top, getWidth() - getPaddingRight(), bounds.bottom);
                }
            }
            int width = getWidth();
            int height = getHeight();
            Drawable drawable2 = this.mDivider;
            if (drawable2 != null) {
                float f = ((float) width) / 2.0f;
                float f2 = ((float) height) / 2.0f;
                drawable2.setBounds((int) (f - ((this.mDensity * 4.0f) / 2.0f)), (int) (f2 - ((this.mDensity * 22.0f) / 2.0f)), (int) (f + ((this.mDensity * 4.0f) / 2.0f)), (int) (f2 + ((this.mDensity * 22.0f) / 2.0f)));
            }
        }
    }

    @Override // androidx.appcompat.widget.SeslProgressBar
    public void onResolveDrawables(int i) {
        super.onResolveDrawables(i);
        Drawable drawable = this.mThumb;
        if (drawable != null) {
            DrawableCompat.setLayoutDirection(drawable, i);
        }
    }

    /* access modifiers changed from: protected */
    @Override // androidx.appcompat.widget.SeslProgressBar
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
    @Override // androidx.appcompat.widget.SeslProgressBar
    public void drawTrack(Canvas canvas) {
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
            if (this.mCurrentMode == 3) {
                canvas.translate((float) getPaddingLeft(), (float) getPaddingTop());
            } else if (!this.mMirrorForRtl || !ViewUtils.isLayoutRtl(this)) {
                canvas.translate((float) getPaddingLeft(), (float) getPaddingTop());
            } else {
                canvas.translate((float) (getWidth() - getPaddingRight()), (float) getPaddingTop());
                canvas.scale(-1.0f, 1.0f);
            }
            this.mOverlapBackground.draw(canvas);
            if (getProgress() > this.mOverlapPoint) {
                this.mOverlapPrimary.draw(canvas);
            }
            canvas.restore();
        }
    }

    /* access modifiers changed from: protected */
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
    public void drawThumb(Canvas canvas) {
        if (this.mThumb != null) {
            int save = canvas.save();
            if (this.mCurrentMode == 3) {
                canvas.translate((float) getPaddingLeft(), (float) (getPaddingTop() - this.mThumbOffset));
            } else {
                canvas.translate((float) (getPaddingLeft() - this.mThumbOffset), (float) getPaddingTop());
            }
            this.mThumb.draw(canvas);
            canvas.restoreToCount(save);
        }
    }

    /* access modifiers changed from: protected */
    @Override // androidx.appcompat.widget.SeslProgressBar
    public synchronized void onMeasure(int i, int i2) {
        int i3;
        int i4;
        Drawable currentDrawable = getCurrentDrawable();
        if (currentDrawable == null) {
            i4 = 0;
            i3 = 0;
        } else if (this.mCurrentMode == 3) {
            int intrinsicHeight = this.mThumb == null ? 0 : this.mThumb.getIntrinsicHeight();
            int max = Math.max(this.mMinWidth, Math.min(this.mMaxWidth, currentDrawable.getIntrinsicHeight()));
            i4 = Math.max(this.mMinHeight, Math.min(this.mMaxHeight, currentDrawable.getIntrinsicWidth()));
            i3 = Math.max(intrinsicHeight, max);
        } else {
            int intrinsicHeight2 = this.mThumb == null ? 0 : this.mThumb.getIntrinsicHeight();
            int max2 = Math.max(this.mMinWidth, Math.min(this.mMaxWidth, currentDrawable.getIntrinsicWidth()));
            i4 = Math.max(intrinsicHeight2, Math.max(this.mMinHeight, Math.min(this.mMaxHeight, currentDrawable.getIntrinsicHeight())));
            i3 = max2;
        }
        setMeasuredDimension(resolveSizeAndState(i3 + getPaddingLeft() + getPaddingRight(), i, 0), resolveSizeAndState(i4 + getPaddingTop() + getPaddingBottom(), i2, 0));
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (!this.mIsUserSeekable || this.mIsTouchDisabled || !isEnabled()) {
            return false;
        }
        int action = motionEvent.getAction();
        if (action == 0) {
            this.mIsDraggingForSliding = false;
            if (supportIsInScrollingContainer()) {
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
                if ((this.mCurrentMode != 3 && Math.abs(x - this.mTouchDownX) > ((float) this.mScaledTouchSlop)) || (this.mCurrentMode == 3 && Math.abs(y - this.mTouchDownY) > ((float) this.mScaledTouchSlop))) {
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

    private void setHotspot(float f, float f2) {
        Drawable background = getBackground();
        if (background != null) {
            DrawableCompat.setHotspot(background, f, f2);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:26:0x0091  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void trackTouchEvent(android.view.MotionEvent r8) {
        /*
        // Method dump skipped, instructions count: 165
        */
        throw new UnsupportedOperationException("Method not decompiled: androidx.appcompat.widget.SeslAbsSeekBar.trackTouchEvent(android.view.MotionEvent):void");
    }

    private void trackTouchEventInVertical(MotionEvent motionEvent) {
        float f;
        int height = getHeight();
        int paddingTop = (height - getPaddingTop()) - getPaddingBottom();
        int round = Math.round(motionEvent.getX());
        int round2 = height - Math.round(motionEvent.getY());
        float f2 = 0.0f;
        if (round2 < getPaddingBottom()) {
            f = 0.0f;
        } else if (round2 > height - getPaddingTop()) {
            f = 1.0f;
        } else {
            float paddingBottom = ((float) (round2 - getPaddingBottom())) / ((float) paddingTop);
            f2 = this.mTouchProgressOffset;
            f = paddingBottom;
        }
        float max = f2 + (f * ((float) getMax()));
        setHotspot((float) round, (float) round2);
        setProgressInternal((int) max, true, false);
    }

    private void attemptClaimDrag() {
        if (getParent() != null) {
            getParent().requestDisallowInterceptTouchEvent(true);
        }
    }

    /* access modifiers changed from: package-private */
    public void onStartTrackingTouch() {
        this.mIsDragging = true;
    }

    /* access modifiers changed from: package-private */
    public void onStopTrackingTouch() {
        this.mIsDragging = false;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0020, code lost:
        if (r8 != 81) goto L_0x0060;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0046, code lost:
        if (r8 != 81) goto L_0x0060;
     */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x0050  */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x005c  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean onKeyDown(int r8, android.view.KeyEvent r9) {
        /*
        // Method dump skipped, instructions count: 101
        */
        throw new UnsupportedOperationException("Method not decompiled: androidx.appcompat.widget.SeslAbsSeekBar.onKeyDown(int, android.view.KeyEvent):boolean");
    }

    @Override // androidx.appcompat.widget.SeslProgressBar
    public boolean setProgressInternal(int i, boolean z, boolean z2) {
        boolean progressInternal = super.setProgressInternal(i, z, z2);
        updateWarningMode(i);
        updateDualColorMode();
        return progressInternal;
    }

    @Override // androidx.appcompat.widget.SeslProgressBar
    public CharSequence getAccessibilityClassName() {
        Log.d(TAG, "Stack:", new Throwable("stack dump"));
        return AbsSeekBar.class.getName();
    }

    @Override // androidx.appcompat.widget.SeslProgressBar
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo accessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(accessibilityNodeInfo);
        accessibilityNodeInfo.setClassName(SeekBar.class.getName());
        if (isEnabled()) {
            int progress = getProgress();
            if (progress > getMin()) {
                AccessibilityNodeInfoCompat.wrap(accessibilityNodeInfo).addAction(8192);
            }
            if (progress < getMax()) {
                AccessibilityNodeInfoCompat.wrap(accessibilityNodeInfo).addAction(4096);
            }
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
            if (!setProgressInternal(getProgress() + max, true, true)) {
                return false;
            }
            onKeyChange();
            return true;
        } else if (i == 16908349 && canUserSetProgress() && bundle != null && bundle.containsKey(AccessibilityNodeInfoCompat.ACTION_ARGUMENT_PROGRESS_VALUE)) {
            return setProgressInternal((int) bundle.getFloat(AccessibilityNodeInfoCompat.ACTION_ARGUMENT_PROGRESS_VALUE), true, true);
        } else {
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean canUserSetProgress() {
        return !isIndeterminate() && isEnabled();
    }

    public void onRtlPropertiesChanged(int i) {
        super.onRtlPropertiesChanged(i);
        Drawable drawable = this.mThumb;
        if (drawable != null) {
            setThumbPos(getWidth(), drawable, getScale(), Integer.MIN_VALUE);
            invalidate();
        }
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

    @Override // androidx.appcompat.widget.SeslProgressBar
    public void setProgressDrawable(Drawable drawable) {
        super.setProgressDrawable(drawable);
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

    @Override // androidx.appcompat.widget.SeslProgressBar
    public void setMode(int i) {
        super.setMode(i);
        if (i == 0) {
            setProgressTintList(this.mDefaultActivatedProgressColor);
            setThumbTintList(this.mDefaultActivatedThumbColor);
        } else if (i == 1) {
            updateWarningMode(getProgress());
        } else if (i == 3) {
            setThumb(getContext().getResources().getDrawable(R.drawable.sesl_scrubber_control_anim));
        } else if (i == 4) {
            this.mSplitProgress = getContext().getResources().getDrawable(R.drawable.sesl_split_seekbar_primary_progress);
            this.mDivider = getContext().getResources().getDrawable(R.drawable.sesl_split_seekbar_vertical_bar);
            updateSplitProgress();
        }
        invalidate();
    }

    public void setOverlapPointForDualColor(int i) {
        if (i < getMax()) {
            if (i == -1) {
                this.mOverlapPoint = i;
                setProgressTintList(this.mDefaultActivatedProgressColor);
                setThumbTintList(this.mDefaultActivatedThumbColor);
            } else {
                this.mOverlapPoint = i;
                getDualOverlapDrawable();
                updateDualColorMode();
            }
            invalidate();
        }
    }

    private void updateDualColorMode() {
        if (!checkInvalidatedDualColorMode()) {
            DrawableCompat.setTintList(this.mOverlapPrimary, this.mOverlapActivatedProgressColor);
            DrawableCompat.setTintList(this.mOverlapBackground, this.mOverlapNormalProgressColor);
            if (!this.mLargeFont) {
                if (getProgress() > this.mOverlapPoint) {
                    setProgressOverlapTintList(this.mOverlapActivatedProgressColor);
                    setThumbOverlapTintList(this.mOverlapActivatedThumbColor);
                } else {
                    setProgressTintList(this.mDefaultActivatedProgressColor);
                    setThumbTintList(this.mDefaultActivatedThumbColor);
                }
            }
            updateBoundsForDualColor();
        }
    }

    private void updateBoundsForDualColor() {
        int i;
        if (getCurrentDrawable() != null && !checkInvalidatedDualColorMode()) {
            Rect bounds = getCurrentDrawable().getBounds();
            int max = getMax();
            int progress = getProgress();
            if (this.mCurrentMode == 0) {
                int i2 = bounds.right - bounds.left;
                int i3 = this.mOverlapPoint;
                if (i3 == 0 || i3 >= getProgress() || this.mLargeFont) {
                    i = (int) (((float) bounds.left) + (((float) i2) * (((float) this.mOverlapPoint) / ((float) max))));
                } else {
                    i = bounds.left;
                }
                int min = Math.min((int) (((float) bounds.left) + (((float) i2) * (((float) progress) / ((float) max)))), bounds.right);
                this.mOverlapBackground.setBounds(i, bounds.top, bounds.right, bounds.bottom);
                this.mOverlapPrimary.setBounds(i, bounds.top, min, bounds.bottom);
            } else if (this.mCurrentMode == 3) {
                float f = (float) (bounds.bottom - bounds.top);
                float f2 = (float) max;
                int i4 = (int) (((float) bounds.top) + ((((float) (max - this.mOverlapPoint)) / f2) * f));
                int i5 = (int) (((float) bounds.top) + (f * (((float) (max - progress)) / f2)));
                this.mOverlapBackground.setBounds(bounds.left, bounds.top, bounds.right, i4);
                this.mOverlapPrimary.setBounds(bounds.left, i5, bounds.right, i4);
            }
        }
    }

    private boolean checkInvalidatedDualColorMode() {
        return this.mOverlapPoint == -1 || this.mOverlapBackground == null;
    }

    private void getDualOverlapDrawable() {
        if (this.mCurrentMode == 0) {
            this.mOverlapPrimary = getContext().getResources().getDrawable(R.drawable.sesl_scrubber_progress_horizontal_extra);
            this.mOverlapBackground = getContext().getResources().getDrawable(R.drawable.sesl_scrubber_progress_horizontal_extra);
        } else if (this.mCurrentMode == 3) {
            this.mOverlapPrimary = getContext().getResources().getDrawable(R.drawable.sesl_scrubber_progress_vertical_extra);
            this.mOverlapBackground = getContext().getResources().getDrawable(R.drawable.sesl_scrubber_progress_vertical_extra);
        }
    }

    /* access modifiers changed from: protected */
    @Override // androidx.appcompat.widget.SeslProgressBar
    public void updateDrawableBounds(int i, int i2) {
        super.updateDrawableBounds(i, i2);
        updateThumbAndTrackPos(i, i2);
        updateBoundsForDualColor();
    }

    private ColorStateList colorToColorStateList(int i) {
        return new ColorStateList(new int[][]{new int[0]}, new int[]{i});
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
                /* class androidx.appcompat.widget.SeslAbsSeekBar.AnonymousClass1 */

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

    private void cancelMuteAnimation() {
        AnimatorSet animatorSet = this.mMuteAnimationSet;
        if (animatorSet != null && animatorSet.isRunning()) {
            this.mMuteAnimationSet.cancel();
        }
    }

    private void startMuteAnimation() {
        cancelMuteAnimation();
        AnimatorSet animatorSet = this.mMuteAnimationSet;
        if (animatorSet != null) {
            animatorSet.start();
        }
    }

    /* access modifiers changed from: protected */
    @Override // androidx.appcompat.widget.SeslProgressBar
    public void onSlidingRefresh(int i) {
        super.onSlidingRefresh(i);
        float f = ((float) i) / 10000.0f;
        Drawable drawable = this.mThumb;
        if (drawable != null) {
            setThumbPos(getWidth(), drawable, f, Integer.MIN_VALUE);
            invalidate();
        }
    }

    private void setThumbOverlapTintList(ColorStateList colorStateList) {
        this.mThumbTintList = colorStateList;
        this.mHasThumbTint = true;
        applyThumbTint();
    }

    @Override // androidx.appcompat.widget.SeslProgressBar
    public void setProgressTintList(ColorStateList colorStateList) {
        super.setProgressTintList(colorStateList);
        this.mDefaultActivatedProgressColor = colorStateList;
    }

    private void setProgressOverlapTintList(ColorStateList colorStateList) {
        super.setProgressTintList(colorStateList);
    }

    public boolean supportIsHoveringUIEnabled() {
        return IS_BASE_SDK_VERSION && SeslViewReflector.isHoveringUIEnabled(this);
    }

    public void setHoverPopupGravity(int i) {
        if (IS_BASE_SDK_VERSION) {
            SeslHoverPopupWindowReflector.setGravity(SeslViewReflector.semGetHoverPopup(this, true), i);
        }
    }

    public void setHoverPopupOffset(int i, int i2) {
        if (IS_BASE_SDK_VERSION) {
            SeslHoverPopupWindowReflector.setOffset(SeslViewReflector.semGetHoverPopup(this, true), i, i2);
        }
    }

    public void setHoverPopupDetectTime() {
        if (IS_BASE_SDK_VERSION) {
            SeslHoverPopupWindowReflector.setHoverDetectTime(SeslViewReflector.semGetHoverPopup(this, true), 200);
        }
    }

    public void setHoveringPoint(int i, int i2) {
        if (IS_BASE_SDK_VERSION) {
            SeslHoverPopupWindowReflector.setHoveringPoint(this, i, i2);
        }
    }

    public void updateHoverPopup() {
        if (IS_BASE_SDK_VERSION) {
            SeslHoverPopupWindowReflector.update(SeslViewReflector.semGetHoverPopup(this, true));
        }
    }

    public boolean isHoverPopupTypeUserCustom(int i) {
        return IS_BASE_SDK_VERSION && i == SeslHoverPopupWindowReflector.getField_TYPE_USER_CUSTOM();
    }

    public boolean isHoverPopupTypeNone(int i) {
        return IS_BASE_SDK_VERSION && i == SeslHoverPopupWindowReflector.getField_TYPE_NONE();
    }

    public int getHoverPopupType() {
        if (IS_BASE_SDK_VERSION) {
            return SeslViewReflector.semGetHoverPopupType(this);
        }
        return 0;
    }

    public boolean supportIsInScrollingContainer() {
        return SeslViewReflector.isInScrollingContainer(this);
    }
}
