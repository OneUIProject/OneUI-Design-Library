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
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.LinearInterpolator;
import android.widget.AbsSeekBar;

import androidx.appcompat.animation.SeslAnimationUtils;
import androidx.appcompat.graphics.drawable.DrawableWrapper;
import androidx.appcompat.util.SeslMisc;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.util.Preconditions;
import androidx.core.view.ViewCompat;
import androidx.reflect.view.SeslViewReflector;
import androidx.reflect.widget.SeslHoverPopupWindowReflector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.dlyt.yanndroid.oneui.view.ProgressBar;
import de.dlyt.yanndroid.oneui.R;

public abstract class SeslAbsSeekBar extends ProgressBar {
    public static final boolean IS_BASE_SDK_VERSION;

    static {
        boolean var0;
        if (Build.VERSION.SDK_INT <= 23) {
            var0 = true;
        } else {
            var0 = false;
        }

        IS_BASE_SDK_VERSION = var0;
    }

    public final List<Rect> mGestureExclusionRects;
    public final Rect mTempRect;
    public final Rect mThumbRect;
    public boolean mAllowedSeekBarAnimation;
    public int mCurrentProgressLevel;
    public ColorStateList mDefaultActivatedProgressColor;
    public ColorStateList mDefaultActivatedThumbColor;
    public ColorStateList mDefaultNormalProgressColor;
    public ColorStateList mDefaultSecondaryProgressColor;
    public float mDisabledAlpha;
    public Drawable mDivider;
    public boolean mHasThumbTint;
    public boolean mHasThumbTintMode;
    public boolean mHasTickMarkTint;
    public boolean mHasTickMarkTintMode;
    public int mHoveringLevel;
    public boolean mIsDragging;
    public boolean mIsDraggingForSliding;
    public boolean mIsFirstSetProgress;
    public boolean mIsLightTheme;
    public boolean mIsSeamless;
    public boolean mIsTouchDisabled;
    public boolean mIsUserSeekable;
    public int mKeyProgressIncrement;
    public boolean mLargeFont;
    public AnimatorSet mMuteAnimationSet;
    public ColorStateList mOverlapActivatedProgressColor;
    public ColorStateList mOverlapActivatedThumbColor;
    public Drawable mOverlapBackground;
    public ColorStateList mOverlapNormalProgressColor;
    public int mOverlapPoint;
    public int mPreviousHoverPopupType;
    public int mScaledTouchSlop;
    public boolean mSetDualColorMode;
    public Drawable mSplitProgress;
    public boolean mSplitTrack;
    public Drawable mThumb;
    public int mThumbOffset;
    public int mThumbPosX;
    public int mThumbRadius;
    public ColorStateList mThumbTintList;
    public PorterDuff.Mode mThumbTintMode;
    public Drawable mTickMark;
    public ColorStateList mTickMarkTintList;
    public PorterDuff.Mode mTickMarkTintMode;
    public float mTouchDownX;
    public float mTouchDownY;
    public float mTouchProgressOffset;
    public int mTrackMaxWidth;
    public int mTrackMinWidth;
    public boolean mUseMuteAnimation;
    public List<Rect> mUserGestureExclusionRects;
    public ValueAnimator mValueAnimator;

    public SeslAbsSeekBar(Context var1) {
        super(var1);
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
        this.mIsSeamless = false;
    }

    public SeslAbsSeekBar(Context var1, AttributeSet var2) {
        super(var1, var2);
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
        this.mIsSeamless = false;
    }

    public SeslAbsSeekBar(Context var1, AttributeSet var2, int var3) {
        this(var1, var2, var3, 0);
    }

    @SuppressLint("RestrictedApi")
    public SeslAbsSeekBar(Context var1, AttributeSet var2, int var3, int var4) {
        super(var1, var2, var3, var4);
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
        this.mIsSeamless = false;
        TypedArray var5 = var1.obtainStyledAttributes(var2, R.styleable.SeslAbsSeekBar, var3, var4);
        if (Build.VERSION.SDK_INT >= 29) {
            this.saveAttributeDataForStyleable(var1, R.styleable.SeslAbsSeekBar, var2, var5, var3, var4);
        }

        Resources var6 = var1.getResources();
        this.setThumb(var5.getDrawable(R.styleable.SeslAbsSeekBar_android_thumb));
        if (var5.hasValue(R.styleable.SeslAbsSeekBar_android_thumbTintMode)) {
            this.mThumbTintMode = DrawableUtils.parseTintMode(var5.getInt(R.styleable.SeslAbsSeekBar_android_thumbTintMode, -1), this.mThumbTintMode);
            this.mHasThumbTintMode = true;
        }

        if (var5.hasValue(R.styleable.SeslAbsSeekBar_android_thumbTint)) {
            this.mThumbTintList = var5.getColorStateList(R.styleable.SeslAbsSeekBar_android_thumbTint);
            this.mHasThumbTint = true;
        }

        this.setTickMark(var5.getDrawable(R.styleable.SeslAbsSeekBar_tickMark));
        if (var5.hasValue(R.styleable.SeslAbsSeekBar_tickMarkTintMode)) {
            this.mTickMarkTintMode = DrawableUtils.parseTintMode(var5.getInt(R.styleable.SeslAbsSeekBar_tickMarkTintMode, -1), this.mTickMarkTintMode);
            this.mHasTickMarkTintMode = true;
        }

        if (var5.hasValue(R.styleable.SeslAbsSeekBar_tickMarkTint)) {
            this.mTickMarkTintList = var5.getColorStateList(R.styleable.SeslAbsSeekBar_tickMarkTint);
            this.mHasTickMarkTint = true;
        }

        this.mSplitTrack = var5.getBoolean(R.styleable.SeslAbsSeekBar_android_splitTrack, false);
        this.mTrackMinWidth = var5.getDimensionPixelSize(R.styleable.SeslAbsSeekBar_seslTrackMinWidth, Math.round(var6.getDimension(R.dimen.sesl_seekbar_track_height)));
        this.mTrackMaxWidth = var5.getDimensionPixelSize(R.styleable.SeslAbsSeekBar_seslTrackMaxWidth, Math.round(var6.getDimension(R.dimen.sesl_seekbar_track_height_expand)));
        this.mThumbRadius = var5.getDimensionPixelSize(R.styleable.SeslAbsSeekBar_seslThumbRadius, Math.round(var6.getDimension(R.dimen.sesl_seekbar_thumb_radius)));
        this.setThumbOffset(var5.getDimensionPixelOffset(R.styleable.SeslAbsSeekBar_android_thumbOffset, this.getThumbOffset()));
        if (var5.hasValue(R.styleable.SeslAbsSeekBar_seslSeekBarMode)) {
            super.mCurrentMode = var5.getInt(R.styleable.SeslAbsSeekBar_seslSeekBarMode, 0);
        }

        boolean var7 = var5.getBoolean(R.styleable.SeslAbsSeekBar_useDisabledAlpha, true);
        var5.recycle();
        if (var7) {
            TypedArray var9 = var1.obtainStyledAttributes(var2, R.styleable.AppCompatTheme, 0, 0);
            this.mDisabledAlpha = var9.getFloat(R.styleable.AppCompatTheme_android_disabledAlpha, 0.5F);
            var9.recycle();
        } else {
            this.mDisabledAlpha = 1.0F;
        }

        this.applyThumbTint();
        this.applyTickMarkTint();
        this.mScaledTouchSlop = ViewConfiguration.get(var1).getScaledTouchSlop();
        this.mIsLightTheme = SeslMisc.isLightTheme(var1);
        this.mDefaultNormalProgressColor = this.colorToColorStateList(var6.getColor(R.color.sesl_seekbar_control_color_default, var1.getTheme()));
        this.mDefaultSecondaryProgressColor = this.colorToColorStateList(var6.getColor(R.color.sesl_seekbar_control_color_secondary, var1.getTheme()));
        this.mDefaultActivatedProgressColor = this.colorToColorStateList(getColor(var1, R.attr.colorPrimary));
        this.mOverlapNormalProgressColor = this.colorToColorStateList(var6.getColor(R.color.sesl_seekbar_overlap_color_default, var1.getTheme()));
        this.mOverlapActivatedProgressColor = this.colorToColorStateList(var6.getColor(R.color.sesl_seekbar_overlap_color_activated, var1.getTheme()));
        this.mOverlapActivatedThumbColor = this.mOverlapActivatedProgressColor;
        this.mDefaultActivatedThumbColor = this.getThumbTintList();
        if (this.mDefaultActivatedThumbColor == null) {
            int[] var8 = new int[]{16842910};
            int[] var10 = new int[]{-16842910};
            var4 = getColor(var1, R.attr.colorPrimary);
            ;
            var3 = var6.getColor(R.color.sesl_seekbar_disable_color_activated, var1.getTheme());
            this.mDefaultActivatedThumbColor = new ColorStateList(new int[][]{var8, var10}, new int[]{var4, var3});
        }

        this.mAllowedSeekBarAnimation = var6.getBoolean(R.bool.sesl_seekbar_sliding_animation);
        if (this.mAllowedSeekBarAnimation) {
            this.initMuteAnimation();
        }

        var3 = super.mCurrentMode;
        if (var3 != 0) {
            this.setMode(var3);
        }

    }

    private int getHoverPopupType() {
        if (IS_BASE_SDK_VERSION) {
            return SeslViewReflector.semGetHoverPopupType(this);
        }
        return 0;
    }

    private float getScale() {
        int var1 = this.getMin();
        int var2 = this.getMax() - var1;
        float var3;
        if (var2 > 0) {
            var3 = (float) (this.getProgress() - var1) / (float) var2;
        } else {
            var3 = 0.0F;
        }

        return var3;
    }

    private void setProgressOverlapTintList(ColorStateList var1) {
        super.setProgressTintList(var1);
    }

    private void setThumbOverlapTintList(ColorStateList var1) {
        this.mThumbTintList = var1;
        this.mHasThumbTint = true;
        this.applyThumbTint();
    }

    public final void applyThumbTint() {
        if (this.mThumb != null && (this.mHasThumbTint || this.mHasThumbTintMode)) {
            this.mThumb = this.mThumb.mutate();
            if (this.mHasThumbTint) {
                DrawableCompat.setTintList(this.mThumb, this.mThumbTintList);
            }

            if (this.mHasThumbTintMode) {
                DrawableCompat.setTintMode(this.mThumb, this.mThumbTintMode);
            }

            if (this.mThumb.isStateful()) {
                this.mThumb.setState(this.getDrawableState());
            }
        }

    }

    public final void applyTickMarkTint() {
        if (this.mTickMark != null && (this.mHasTickMarkTint || this.mHasTickMarkTintMode)) {
            this.mTickMark = this.mTickMark.mutate();
            if (this.mHasTickMarkTint) {
                DrawableCompat.setTintList(this.mTickMark, this.mTickMarkTintList);
            }

            if (this.mHasTickMarkTintMode) {
                DrawableCompat.setTintMode(this.mTickMark, this.mTickMarkTintMode);
            }

            if (this.mTickMark.isStateful()) {
                this.mTickMark.setState(this.getDrawableState());
            }
        }

    }

    public final void attemptClaimDrag() {
        if (this.getParent() != null) {
            this.getParent().requestDisallowInterceptTouchEvent(true);
        }

    }

    public final void callSuperSetProgress(int var1) {
        super.setProgress(var1);
    }

    public boolean canUserSetProgress() {
        boolean var1;
        if (!this.isIndeterminate() && this.isEnabled()) {
            var1 = true;
        } else {
            var1 = false;
        }

        return var1;
    }

    public final void cancelMuteAnimation() {
        AnimatorSet var1 = this.mMuteAnimationSet;
        if (var1 != null && var1.isRunning()) {
            this.mMuteAnimationSet.cancel();
        }

    }

    public final boolean checkInvalidatedDualColorMode() {
        boolean var1;
        if (this.mOverlapPoint != -1 && this.mOverlapBackground != null) {
            var1 = false;
        } else {
            var1 = true;
        }

        return var1;
    }

    public final ColorStateList colorToColorStateList(int var1) {
        return new ColorStateList(new int[][]{new int[0]}, new int[]{var1});
    }

    public void drawThumb(Canvas var1) {
        if (this.mThumb != null) {
            int var2 = var1.save();
            int var3 = super.mCurrentMode;
            if (var3 != 3 && var3 != 6) {
                var1.translate((float) (this.getPaddingLeft() - this.mThumbOffset), (float) this.getPaddingTop());
            } else {
                var1.translate((float) this.getPaddingLeft(), (float) (this.getPaddingTop() - this.mThumbOffset));
            }

            this.mThumb.draw(var1);
            var1.restoreToCount(var2);
        }

    }

    public void drawTickMarks(Canvas var1) {
        if (this.mTickMark != null) {
            int var2 = this.getMax() - this.getMin();
            int var3 = 1;
            if (var2 > 1) {
                int var4 = this.mTickMark.getIntrinsicWidth();
                int var5 = this.mTickMark.getIntrinsicHeight();
                if (var4 >= 0) {
                    var4 /= 2;
                } else {
                    var4 = 1;
                }

                if (var5 >= 0) {
                    var3 = var5 / 2;
                }

                this.mTickMark.setBounds(-var4, -var3, var4, var3);
                float var6 = (float) (this.getWidth() - this.getPaddingLeft() - this.getPaddingRight()) / (float) var2;
                var3 = var1.save();
                var1.translate((float) this.getPaddingLeft(), (float) this.getHeight() / 2.0F);

                for (var4 = 0; var4 <= var2; ++var4) {
                    this.mTickMark.draw(var1);
                    var1.translate(var6, 0.0F);
                }

                var1.restoreToCount(var3);
            }
        }

    }

    @SuppressLint("RestrictedApi")
    public void drawTrack(Canvas var1) {
        Drawable var2 = this.mThumb;
        Rect var3;
        Rect var4;
        int var5;
        if (var2 != null && this.mSplitTrack) {
            var3 = DrawableUtils.getOpticalBounds(var2);
            var4 = this.mTempRect;
            var2.copyBounds(var4);
            var4.offset(this.getPaddingLeft() - this.mThumbOffset, this.getPaddingTop());
            var4.left += var3.left;
            var4.right -= var3.right;
            var5 = var1.save();
            var1.clipRect(var4, Region.Op.DIFFERENCE);
            super.drawTrack(var1);
            this.drawTickMarks(var1);
            var1.restoreToCount(var5);
        } else {
            super.drawTrack(var1);
            this.drawTickMarks(var1);
        }

        if (!this.checkInvalidatedDualColorMode()) {
            var1.save();
            if (super.mMirrorForRtl && ViewUtils.isLayoutRtl(this)) {
                var1.translate((float) (this.getWidth() - this.getPaddingRight()), (float) this.getPaddingTop());
                var1.scale(-1.0F, 1.0F);
            } else {
                var1.translate((float) this.getPaddingLeft(), (float) this.getPaddingTop());
            }

            var4 = this.mOverlapBackground.getBounds();
            var3 = this.mTempRect;
            this.mOverlapBackground.copyBounds(var3);
            int var6 = Math.max(this.getProgress(), this.mOverlapPoint);
            int var7 = this.getMax();
            var5 = super.mCurrentMode;
            if (var5 != 3 && var5 != 6) {
                var3.left = (int) ((float) var4.left + (float) var4.width() * ((float) var6 / (float) var7));
            } else {
                var3.bottom = (int) ((float) var4.bottom - (float) var4.height() * ((float) var6 / (float) var7));
            }

            var1.clipRect(var3);
            this.mOverlapBackground.draw(var1);
            var1.restore();
        }

    }

    public void drawableHotspotChanged(float var1, float var2) {
        super.drawableHotspotChanged(var1, var2);
        Drawable var3 = this.mThumb;
        if (var3 != null) {
            DrawableCompat.setHotspot(var3, var1, var2);
        }

    }

    public void drawableStateChanged() {
        super.drawableStateChanged();
        Drawable var1 = this.getProgressDrawable();
        Drawable var3;
        if (var1 != null && this.mDisabledAlpha < 1.0F) {
            int var2;
            if (this.isEnabled()) {
                var2 = 255;
            } else {
                var2 = (int) (this.mDisabledAlpha * 255.0F);
            }

            var1.setAlpha(var2);
            var3 = this.mOverlapBackground;
            if (var3 != null) {
                var3.setAlpha(var2);
            }
        }

        if (this.mThumb != null && this.mHasThumbTint) {
            if (!this.isEnabled()) {
                DrawableCompat.setTintList(this.mThumb, (ColorStateList) null);
            } else {
                DrawableCompat.setTintList(this.mThumb, this.mDefaultActivatedThumbColor);
                this.updateDualColorMode();
            }
        }

        if (this.mSetDualColorMode && var1 != null && var1.isStateful()) {
            var3 = this.mOverlapBackground;
            if (var3 != null) {
                var3.setState(this.getDrawableState());
            }
        }

        var3 = this.mThumb;
        if (var3 != null && var3.isStateful() && var3.setState(this.getDrawableState())) {
            this.invalidateDrawable(var3);
        }

        var3 = this.mTickMark;
        if (var3 != null && var3.isStateful() && var3.setState(this.getDrawableState())) {
            this.invalidateDrawable(var3);
        }

    }

    public CharSequence getAccessibilityClassName() {
        Log.d("SeslAbsSeekBar", "Stack:", new Throwable("stack dump"));
        return AbsSeekBar.class.getName();
    }

    public int getKeyProgressIncrement() {
        return this.mKeyProgressIncrement;
    }

    public void setKeyProgressIncrement(int var1) {
        int var2 = var1;
        if (var1 < 0) {
            var2 = -var1;
        }

        this.mKeyProgressIncrement = var2;
    }

    public int getMax() {
        synchronized (this) {
        }

        int var1;
        try {
            if (this.mIsSeamless) {
                var1 = Math.round((float) super.getMax() / 1000.0F);
            } else {
                var1 = super.getMax();
            }
        } finally {
            ;
        }

        return var1;
    }

    public void setMax(int var1) {
        synchronized (this) {
        }
        int var2 = var1;

        try {
            if (this.mIsSeamless) {
                var2 = Math.round((float) var1 * 1000.0F);
            }

            super.setMax(var2);
            this.mIsFirstSetProgress = true;
            var1 = this.getMax() - this.getMin();
            if (this.mKeyProgressIncrement == 0 || var1 / this.mKeyProgressIncrement > 20) {
                this.setKeyProgressIncrement(Math.max(1, Math.round((float) var1 / 20.0F)));
            }
        } finally {
            ;
        }

    }

    public int getMin() {
        synchronized (this) {
        }

        int var1;
        try {
            if (this.mIsSeamless) {
                var1 = Math.round((float) super.getMin() / 1000.0F);
            } else {
                var1 = super.getMin();
            }
        } finally {
            ;
        }

        return var1;
    }

    public void setMin(int var1) {
        synchronized (this) {
        }
        int var2 = var1;

        try {
            if (this.mIsSeamless) {
                var2 = Math.round((float) var1 * 1000.0F);
            }

            super.setMin(var2);
            var1 = this.getMax() - this.getMin();
            if (this.mKeyProgressIncrement == 0 || var1 / this.mKeyProgressIncrement > 20) {
                this.setKeyProgressIncrement(Math.max(1, Math.round((float) var1 / 20.0F)));
            }
        } finally {
            ;
        }

    }

    public int getProgress() {
        synchronized (this) {
        }

        int var1;
        try {
            if (this.mIsSeamless) {
                var1 = Math.round((float) super.getProgress() / 1000.0F);
            } else {
                var1 = super.getProgress();
            }
        } finally {
            ;
        }

        return var1;
    }

    public void setProgress(int var1) {
        synchronized (this) {
        }
        int var2 = var1;

        try {
            if (this.mIsSeamless) {
                var2 = Math.round((float) var1 * 1000.0F);
            }

            super.setProgress(var2);
        } finally {
            ;
        }

    }

    public boolean getSplitTrack() {
        return this.mSplitTrack;
    }

    public void setSplitTrack(boolean var1) {
        this.mSplitTrack = var1;
        this.invalidate();
    }

    public Drawable getThumb() {
        return this.mThumb;
    }

    public void setThumb(Drawable var1) {
        Drawable var2 = this.mThumb;
        boolean var3;
        if (var2 != null && var1 != var2) {
            var2.setCallback((Drawable.Callback) null);
            var3 = true;
        } else {
            var3 = false;
        }

        if (var1 != null) {
            var1.setCallback(this);
            if (this.canResolveLayoutDirection()) {
                DrawableCompat.setLayoutDirection(var1, ViewCompat.getLayoutDirection(this));
            }

            int var4 = super.mCurrentMode;
            if (var4 != 3 && var4 != 6) {
                this.mThumbOffset = var1.getIntrinsicWidth() / 2;
            } else {
                this.mThumbOffset = var1.getIntrinsicHeight() / 2;
            }

            if (var3 && (var1.getIntrinsicWidth() != this.mThumb.getIntrinsicWidth() || var1.getIntrinsicHeight() != this.mThumb.getIntrinsicHeight())) {
                this.requestLayout();
            }
        }

        this.mThumb = var1;
        this.applyThumbTint();
        this.invalidate();
        if (var3) {
            this.updateThumbAndTrackPos(this.getWidth(), this.getHeight());
            if (var1 != null && var1.isStateful()) {
                var1.setState(this.getDrawableState());
            }
        }

    }

    public Rect getThumbBounds() {
        Drawable var1 = this.mThumb;
        Rect var2;
        if (var1 != null) {
            var2 = var1.getBounds();
        } else {
            var2 = null;
        }

        return var2;
    }

    public int getThumbHeight() {
        return this.mThumb.getIntrinsicHeight();
    }

    public int getThumbOffset() {
        return this.mThumbOffset;
    }

    public void setThumbOffset(int var1) {
        this.mThumbOffset = var1;
        this.invalidate();
    }

    public ColorStateList getThumbTintList() {
        return this.mThumbTintList;
    }

    public void setThumbTintList(ColorStateList var1) {
        this.mThumbTintList = var1;
        this.mHasThumbTint = true;
        this.applyThumbTint();
        this.mDefaultActivatedThumbColor = var1;
    }

    public PorterDuff.Mode getThumbTintMode() {
        return this.mThumbTintMode;
    }

    public void setThumbTintMode(PorterDuff.Mode var1) {
        this.mThumbTintMode = var1;
        this.mHasThumbTintMode = true;
        this.applyThumbTint();
    }

    public Drawable getTickMark() {
        return this.mTickMark;
    }

    public void setTickMark(Drawable var1) {
        Drawable var2 = this.mTickMark;
        if (var2 != null) {
            var2.setCallback((Drawable.Callback) null);
        }

        this.mTickMark = var1;
        if (var1 != null) {
            var1.setCallback(this);
            DrawableCompat.setLayoutDirection(var1, ViewCompat.getLayoutDirection(this));
            if (var1.isStateful()) {
                var1.setState(this.getDrawableState());
            }

            this.applyTickMarkTint();
        }

        this.invalidate();
    }

    public ColorStateList getTickMarkTintList() {
        return this.mTickMarkTintList;
    }

    public void setTickMarkTintList(ColorStateList var1) {
        this.mTickMarkTintList = var1;
        this.mHasTickMarkTint = true;
        this.applyTickMarkTint();
    }

    public PorterDuff.Mode getTickMarkTintMode() {
        return this.mTickMarkTintMode;
    }

    public void setTickMarkTintMode(PorterDuff.Mode var1) {
        this.mTickMarkTintMode = var1;
        this.mHasTickMarkTintMode = true;
        this.applyTickMarkTint();
    }

    public final void initDualOverlapDrawable() {
        int var1 = super.mCurrentMode;
        if (var1 == 5) {
            this.mOverlapBackground = new SeslAbsSeekBar.SliderDrawable((float) this.mTrackMinWidth, (float) this.mTrackMaxWidth, this.mOverlapNormalProgressColor);
        } else if (var1 == 6) {
            this.mOverlapBackground = new SeslAbsSeekBar.SliderDrawable((float) this.mTrackMinWidth, (float) this.mTrackMaxWidth, this.mOverlapNormalProgressColor, true);
        } else if (this.getProgressDrawable() != null && this.getProgressDrawable().getConstantState() != null) {
            this.mOverlapBackground = this.getProgressDrawable().getConstantState().newDrawable().mutate();
        }

    }

    public final void initMuteAnimation() {
        this.mMuteAnimationSet = new AnimatorSet();
        ArrayList var1 = new ArrayList();
        int var2 = 400;

        int var6;
        for (int var3 = 0; var3 < 8; var2 = var6) {
            boolean var4;
            if (var3 % 2 == 0) {
                var4 = true;
            } else {
                var4 = false;
            }

            ValueAnimator var5;
            if (var4) {
                var5 = ValueAnimator.ofInt(new int[]{0, var2});
            } else {
                var5 = ValueAnimator.ofInt(new int[]{var2, 0});
            }

            var5.setDuration((long) 62);
            var5.setInterpolator(new LinearInterpolator());
            var5.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator var1) {
                    SeslAbsSeekBar.this.mCurrentProgressLevel = (Integer) var1.getAnimatedValue();
                    SeslAbsSeekBar var2 = SeslAbsSeekBar.this;
                    var2.onSlidingRefresh(var2.mCurrentProgressLevel);
                }
            });
            var1.add(var5);
            var6 = var2;
            if (var4) {
                var6 = (int) ((double) var2 * 0.6D);
            }

            ++var3;
        }

        this.mMuteAnimationSet.playSequentially(var1);
    }

    public final void initializeExpandMode() {
        SeslAbsSeekBar.SliderDrawable var1 = new SeslAbsSeekBar.SliderDrawable((float) this.mTrackMinWidth, (float) this.mTrackMaxWidth, this.mDefaultNormalProgressColor);
        SeslAbsSeekBar.SliderDrawable var2 = new SeslAbsSeekBar.SliderDrawable((float) this.mTrackMinWidth, (float) this.mTrackMaxWidth, this.mDefaultSecondaryProgressColor);
        SeslAbsSeekBar.SliderDrawable var3 = new SeslAbsSeekBar.SliderDrawable((float) this.mTrackMinWidth, (float) this.mTrackMaxWidth, this.mDefaultActivatedProgressColor);
        @SuppressLint("RestrictedApi") DrawableWrapper var4 = new DrawableWrapper(new SeslAbsSeekBar.ThumbDrawable(this.mThumbRadius, this.mDefaultActivatedThumbColor, false));
        LayerDrawable var7 = new LayerDrawable(new Drawable[]{var1, new ClipDrawable(var2, 19, 1), new ClipDrawable(var3, 19, 1)});
        var7.setPaddingMode(1);
        var7.setId(0, R.id.background);
        var7.setId(1, R.id.secondaryProgress);
        var7.setId(2, R.id.progress);
        this.setProgressDrawable(var7);
        this.setThumb(var4);
        this.setBackgroundResource(R.drawable.sesl_seekbar_background_borderless_expand);
        int var5 = this.getMaxHeight();
        int var6 = this.mTrackMaxWidth;
        if (var5 > var6) {
            this.setMaxHeight(var6);
        }

    }

    public final void initializeExpandVerticalMode() {
        SeslAbsSeekBar.SliderDrawable var1 = new SeslAbsSeekBar.SliderDrawable((float) this.mTrackMinWidth, (float) this.mTrackMaxWidth, this.mDefaultNormalProgressColor, true);
        SeslAbsSeekBar.SliderDrawable var2 = new SeslAbsSeekBar.SliderDrawable((float) this.mTrackMinWidth, (float) this.mTrackMaxWidth, this.mDefaultSecondaryProgressColor, true);
        SeslAbsSeekBar.SliderDrawable var3 = new SeslAbsSeekBar.SliderDrawable((float) this.mTrackMinWidth, (float) this.mTrackMaxWidth, this.mDefaultActivatedProgressColor, true);
        @SuppressLint("RestrictedApi") DrawableWrapper var4 = new DrawableWrapper(new SeslAbsSeekBar.ThumbDrawable(this.mThumbRadius, this.mDefaultActivatedThumbColor, true));
        LayerDrawable var7 = new LayerDrawable(new Drawable[]{var1, new ClipDrawable(var2, 81, 2), new ClipDrawable(var3, 81, 2)});
        var7.setPaddingMode(1);
        var7.setId(0, R.id.background);
        var7.setId(1, R.id.secondaryProgress);
        var7.setId(2, R.id.progress);
        this.setProgressDrawable(var7);
        this.setThumb(var4);
        this.setBackgroundResource(R.drawable.sesl_seekbar_background_borderless_expand);
        int var5 = this.getMaxWidth();
        int var6 = this.mTrackMaxWidth;
        if (var5 > var6) {
            this.setMaxWidth(var6);
        }

    }

    public final boolean isHoverPopupTypeUserCustom(int var1) {
        boolean var2;
        if (IS_BASE_SDK_VERSION && var1 == 3 /*SeslHoverPopupWindowReflector.getField_TYPE_USER_CUSTOM()*/) {
            var2 = true;
        } else {
            var2 = false;
        }

        return var2;
    }

    public void jumpDrawablesToCurrentState() {
        super.jumpDrawablesToCurrentState();
        Drawable var1 = this.mThumb;
        if (var1 != null) {
            var1.jumpToCurrentState();
        }

        var1 = this.mTickMark;
        if (var1 != null) {
            var1.jumpToCurrentState();
        }

    }

    public void onDraw(Canvas var1) {
        synchronized (this) {
        }

        try {
            super.onDraw(var1);
            if (this.supportIsHoveringUIEnabled()) {
                int var2 = this.getHoverPopupType();
                if (this.isHoverPopupTypeUserCustom(var2) && this.mPreviousHoverPopupType != var2) {
                    this.mPreviousHoverPopupType = var2;
                    this.setHoverPopupGravity(12849);
                    this.setHoverPopupOffset(0, this.getMeasuredHeight() / 2);
                    this.setHoverPopupDetectTime();
                }
            }

            if (super.mCurrentMode == 4) {
                this.mSplitProgress.draw(var1);
                this.mDivider.draw(var1);
            }

            if (!this.mIsTouchDisabled) {
                this.drawThumb(var1);
            }
        } finally {
            ;
        }

    }

    public void onHoverChanged(int var1, int var2, int var3) {
    }

    public boolean onHoverEvent(MotionEvent var1) {
        if (this.supportIsHoveringUIEnabled()) {
            int var2 = var1.getAction();
            int var3 = (int) var1.getX();
            int var4 = (int) var1.getY();
            if (var2 != 7) {
                if (var2 != 9) {
                    if (var2 == 10) {
                        this.onStopTrackingHover();
                    }
                } else {
                    this.trackHoverEvent(var3);
                    this.onStartTrackingHover(this.mHoveringLevel, var3, var4);
                }
            } else {
                this.trackHoverEvent(var3);
                this.onHoverChanged(this.mHoveringLevel, var3, var4);
                if (this.isHoverPopupTypeUserCustom(this.getHoverPopupType())) {
                    this.setHoveringPoint((int) var1.getRawX(), (int) var1.getRawY());
                    this.updateHoverPopup();
                }
            }
        }

        return super.onHoverEvent(var1);
    }

    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo var1) {
        super.onInitializeAccessibilityNodeInfo(var1);
        if (this.isEnabled()) {
            int var2 = this.getProgress();
            if (var2 > this.getMin()) {
                var1.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_BACKWARD);
            }

            if (var2 < this.getMax()) {
                var1.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD);
            }
        }

    }

    public void onKeyChange() {
    }

    @SuppressLint("RestrictedApi")
    public boolean onKeyDown(int var1, KeyEvent var2) {
        if (this.isEnabled()) {
            int var3 = this.mKeyProgressIncrement;
            int var4 = super.mCurrentMode;
            if (var4 != 3 && var4 != 6) {
                label64:
                {
                    if (var1 != 21) {
                        var4 = var3;
                        if (var1 == 22) {
                            break label64;
                        }

                        if (var1 != 69) {
                            var4 = var3;
                            if (var1 != 70) {
                                var4 = var3;
                                if (var1 != 81) {
                                    return super.onKeyDown(var1, var2);
                                }
                            }
                            break label64;
                        }
                    }

                    var4 = -var3;
                }

                var3 = var4;
                if (ViewUtils.isLayoutRtl(this)) {
                    var3 = -var4;
                }

                if (this.mIsSeamless) {
                    var4 = Math.round((float) (this.getProgress() + var3) * 1000.0F);
                } else {
                    var4 = var3 + this.getProgress();
                }

                if (this.setProgressInternal(var4, true, true)) {
                    this.onKeyChange();
                    return true;
                }
            } else {
                var4 = var3;
                if (var1 != 19) {
                    if (var1 != 20 && var1 != 69) {
                        var4 = var3;
                        if (var1 != 70) {
                            var4 = var3;
                            if (var1 != 81) {
                                return super.onKeyDown(var1, var2);
                            }
                        }
                    } else {
                        var4 = -var3;
                    }
                }

                var3 = var4;
                if (ViewUtils.isLayoutRtl(this)) {
                    var3 = -var4;
                }

                if (this.mIsSeamless) {
                    var4 = Math.round((float) (this.getProgress() + var3) * 1000.0F);
                } else {
                    var4 = var3 + this.getProgress();
                }

                if (this.setProgressInternal(var4, true, true)) {
                    this.onKeyChange();
                    return true;
                }
            }
        }

        return super.onKeyDown(var1, var2);
    }

    public void onMeasure(int var1, int var2) {
        synchronized (this) {
        }

        Throwable var10000;
        label788:
        {
            Drawable var3;
            boolean var10001;
            try {
                var3 = this.getCurrentDrawable();
            } catch (Throwable var99) {
                var10000 = var99;
                var10001 = false;
                break label788;
            }

            int var4;
            int var5;
            int var6;
            if (var3 != null) {
                label789:
                {
                    label791:
                    {
                        label775:
                        try {
                            if (super.mCurrentMode != 3 && super.mCurrentMode != 6) {
                                break label775;
                            }
                            break label791;
                        } catch (Throwable var98) {
                            var10000 = var98;
                            var10001 = false;
                            break label788;
                        }

                        label760:
                        {
                            label759:
                            {
                                try {
                                    if (this.mThumb != null) {
                                        break label759;
                                    }
                                } catch (Throwable var96) {
                                    var10000 = var96;
                                    var10001 = false;
                                    break label788;
                                }

                                var4 = 0;
                                break label760;
                            }

                            try {
                                var4 = this.mThumb.getIntrinsicHeight();
                            } catch (Throwable var95) {
                                var10000 = var95;
                                var10001 = false;
                                break label788;
                            }
                        }

                        try {
                            var5 = Math.max(super.mMinWidth, Math.min(super.mMaxWidth, var3.getIntrinsicWidth()));
                            var4 = Math.max(var4, Math.max(super.mMinHeight, Math.min(super.mMaxHeight, var3.getIntrinsicHeight())));
                            break label789;
                        } catch (Throwable var94) {
                            var10000 = var94;
                            var10001 = false;
                            break label788;
                        }
                    }

                    label767:
                    {
                        label766:
                        {
                            try {
                                if (this.mThumb == null) {
                                    break label766;
                                }
                            } catch (Throwable var97) {
                                var10000 = var97;
                                var10001 = false;
                                break label788;
                            }

                            try {
                                var4 = this.mThumb.getIntrinsicHeight();
                                break label767;
                            } catch (Throwable var93) {
                                var10000 = var93;
                                var10001 = false;
                                break label788;
                            }
                        }

                        var4 = 0;
                    }

                    try {
                        var6 = Math.max(super.mMinWidth, Math.min(super.mMaxWidth, var3.getIntrinsicHeight()));
                        var5 = Math.max(super.mMinHeight, Math.min(super.mMaxHeight, var3.getIntrinsicWidth()));
                        var6 = Math.max(var4, var6);
                    } catch (Throwable var92) {
                        var10000 = var92;
                        var10001 = false;
                        break label788;
                    }

                    var4 = var5;
                    var5 = var6;
                }
            } else {
                var4 = 0;
                var5 = var4;
            }

            label747:
            try {
                int var7 = this.getPaddingLeft();
                var6 = this.getPaddingRight();
                int var8 = this.getPaddingTop();
                int var9 = this.getPaddingBottom();
                this.setMeasuredDimension(View.resolveSizeAndState(var5 + var7 + var6, var1, 0), View.resolveSizeAndState(var4 + var8 + var9, var2, 0));
                return;
            } catch (Throwable var91) {
                var10000 = var91;
                var10001 = false;
                break label747;
            }
        }

        Throwable var100 = var10000;
        try {
            throw var100;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    public void onProgressRefresh(float var1, boolean var2, int var3) {
        int var4 = (int) (10000.0F * var1);
        boolean var5;
        if (this.mUseMuteAnimation && !this.mIsFirstSetProgress && !this.mIsDraggingForSliding) {
            var5 = true;
        } else {
            var5 = false;
        }

        if (var5 && this.mCurrentProgressLevel != 0 && var4 == 0) {
            this.startMuteAnimation();
        } else {
            this.cancelMuteAnimation();
            this.mIsFirstSetProgress = false;
            this.mCurrentProgressLevel = var4;
            super.onProgressRefresh(var1, var2, var3);
            Drawable var6 = this.mThumb;
            if (var6 != null) {
                this.setThumbPos(this.getWidth(), var6, var1, -2147483648);
                this.invalidate();
            }
        }

    }

    public void onRtlPropertiesChanged(int var1) {
        super.onRtlPropertiesChanged(var1);
        Drawable var2 = this.mThumb;
        if (var2 != null) {
            this.setThumbPos(this.getWidth(), var2, this.getScale(), -2147483648);
            this.invalidate();
        }

    }

    public void onSizeChanged(int var1, int var2, int var3, int var4) {
        super.onSizeChanged(var1, var2, var3, var4);
        this.updateThumbAndTrackPos(var1, var2);
    }

    public void onSlidingRefresh(int var1) {
        super.onSlidingRefresh(var1);
        float var2 = (float) var1 / 10000.0F;
        Drawable var3 = this.mThumb;
        if (var3 != null) {
            this.setThumbPos(this.getWidth(), var3, var2, -2147483648);
            this.invalidate();
        }

    }

    public void onStartTrackingHover(int var1, int var2, int var3) {
    }

    public void onStartTrackingTouch() {
        this.mIsDragging = true;
        ValueAnimator var1 = this.mValueAnimator;
        if (var1 != null) {
            var1.cancel();
        }

    }

    public void onStopTrackingHover() {
    }

    public void onStopTrackingTouch() {
        this.mIsDragging = false;
        if (this.mIsSeamless && this.isPressed()) {
            int var1 = (int) ((float) Math.round((float) super.getProgress() / 1000.0F) * 1000.0F);
            this.mValueAnimator = ValueAnimator.ofInt(new int[]{super.getProgress(), var1});
            this.mValueAnimator.setDuration(300L);
            this.mValueAnimator.setInterpolator(SeslAnimationUtils.SINE_IN_OUT_90);
            this.mValueAnimator.start();
            this.mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator var1) {
                    SeslAbsSeekBar.this.callSuperSetProgress((Integer) var1.getAnimatedValue());
                }
            });
        } else if (this.mIsSeamless) {
            this.setProgress(Math.round((float) super.getProgress() / 1000.0F));
        }

    }

    public boolean onTouchEvent(MotionEvent var1) {
        if (this.mIsUserSeekable && !this.mIsTouchDisabled && this.isEnabled()) {
            int var2 = var1.getAction();
            if (var2 != 0) {
                if (var2 != 1) {
                    if (var2 != 2) {
                        if (var2 == 3) {
                            this.mIsDraggingForSliding = false;
                            if (this.mIsDragging) {
                                this.onStopTrackingTouch();
                                this.setPressed(false);
                            }

                            this.invalidate();
                        }
                    } else {
                        this.mIsDraggingForSliding = true;
                        if (this.mIsDragging) {
                            this.trackTouchEvent(var1);
                        } else {
                            float var3 = var1.getX();
                            float var4 = var1.getY();
                            var2 = super.mCurrentMode;
                            if (var2 == 3 || var2 == 6 || Math.abs(var3 - this.mTouchDownX) <= (float) this.mScaledTouchSlop) {
                                var2 = super.mCurrentMode;
                                if (var2 != 3 && var2 != 6 || Math.abs(var4 - this.mTouchDownY) <= (float) this.mScaledTouchSlop) {
                                    return true;
                                }
                            }

                            this.startDrag(var1);
                        }
                    }
                } else {
                    if (this.mIsDraggingForSliding) {
                        this.mIsDraggingForSliding = false;
                    }

                    if (this.mIsDragging) {
                        this.trackTouchEvent(var1);
                        this.onStopTrackingTouch();
                        this.setPressed(false);
                    } else {
                        this.onStartTrackingTouch();
                        this.trackTouchEvent(var1);
                        this.onStopTrackingTouch();
                    }

                    this.invalidate();
                }
            } else {
                this.mIsDraggingForSliding = false;
                var2 = super.mCurrentMode;
                if (var2 != 5 && var2 != 6 && !this.supportIsInScrollingContainer()) {
                    this.startDrag(var1);
                } else {
                    this.mTouchDownX = var1.getX();
                    this.mTouchDownY = var1.getY();
                }
            }

            return true;
        } else {
            return false;
        }
    }

    public void onVisualProgressChanged(int var1, float var2) {
        super.onVisualProgressChanged(var1, var2);
        if (var1 == R.id.progress) {
            Drawable var3 = this.mThumb;
            if (var3 != null) {
                this.setThumbPos(this.getWidth(), var3, var2, -2147483648);
                this.invalidate();
            }
        }

    }

    public boolean performAccessibilityAction(int var1, Bundle var2) {
        if (super.performAccessibilityAction(var1, var2)) {
            return true;
        } else if (!this.isEnabled()) {
            return false;
        } else if (var1 != 4096 && var1 != 8192) {
            if (var1 != 16908349) {
                return false;
            } else if (!this.canUserSetProgress()) {
                return false;
            } else {
                return var2 != null && var2.containsKey("android.view.accessibility.action.ARGUMENT_PROGRESS_VALUE") ? this.setProgressInternal((int) var2.getFloat("android.view.accessibility.action.ARGUMENT_PROGRESS_VALUE"), true, true) : false;
            }
        } else if (!this.canUserSetProgress()) {
            return false;
        } else {
            int var3 = Math.max(1, Math.round((float) (this.getMax() - this.getMin()) / 20.0F));
            int var4 = var3;
            if (var1 == 8192) {
                var4 = -var3;
            }

            if (this.setProgressInternal(this.getProgress() + var4, true, true)) {
                this.onKeyChange();
                return true;
            } else {
                return false;
            }
        }
    }

    public final void setHotspot(float var1, float var2) {
        Drawable var3 = this.getBackground();
        if (var3 != null) {
            DrawableCompat.setHotspot(var3, var1, var2);
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

    public void setMode(int var1) {
        super.setMode(var1);
        if (var1 != 0) {
            if (var1 != 1) {
                if (var1 != 3) {
                    if (var1 != 4) {
                        if (var1 != 5) {
                            if (var1 == 6) {
                                this.initializeExpandVerticalMode();
                            }
                        } else {
                            this.initializeExpandMode();
                        }
                    } else {
                        this.mSplitProgress = this.getContext().getResources().getDrawable(R.drawable.sesl_split_seekbar_primary_progress, null);
                        this.mDivider = this.getContext().getResources().getDrawable(R.drawable.sesl_split_seekbar_vertical_bar, null);
                        this.updateSplitProgress();
                    }
                } else {
                    Resources var2 = this.getContext().getResources();
                    this.setThumb(var2.getDrawable(R.drawable.sesl_scrubber_control_anim, null));
                }
            } else {
                this.updateWarningMode(this.getProgress());
            }
        } else {
            this.setProgressTintList(this.mDefaultActivatedProgressColor);
            this.setThumbTintList(this.mDefaultActivatedThumbColor);
        }

        this.invalidate();
    }

    public void setOverlapBackgroundForDualColor(int var1) {
        ColorStateList var2 = this.colorToColorStateList(var1);
        if (!var2.equals(this.mOverlapNormalProgressColor)) {
            this.mOverlapNormalProgressColor = var2;
        }

        this.mOverlapActivatedProgressColor = this.mOverlapNormalProgressColor;
        this.mLargeFont = true;
    }

    public void setOverlapPointForDualColor(int var1) {
        if (var1 < this.getMax()) {
            this.mSetDualColorMode = true;
            this.mOverlapPoint = var1;
            if (var1 == -1) {
                this.setProgressTintList(this.mDefaultActivatedProgressColor);
                this.setThumbTintList(this.mDefaultActivatedThumbColor);
            } else {
                if (this.mOverlapBackground == null) {
                    this.initDualOverlapDrawable();
                }

                this.updateDualColorMode();
            }

            this.invalidate();
        }
    }

    public void setProgressDrawable(Drawable var1) {
        super.setProgressDrawable(var1);
    }

    public boolean setProgressInternal(int var1, boolean var2, boolean var3) {
        var2 = super.setProgressInternal(var1, var2, var3);
        this.updateWarningMode(var1);
        this.updateDualColorMode();
        return var2;
    }

    public void setProgressTintList(ColorStateList var1) {
        super.setProgressTintList(var1);
        this.mDefaultActivatedProgressColor = var1;
    }

    public void setSeamless(boolean var1) {
        if (this.mIsSeamless != var1) {
            this.mIsSeamless = var1;
            if (var1) {
                super.setMax(Math.round((float) super.getMax() * 1000.0F));
                super.setMin(Math.round((float) super.getMin() * 1000.0F));
                super.setProgress(Math.round((float) super.getProgress() * 1000.0F));
            } else {
                super.setProgress(Math.round((float) super.getProgress() / 1000.0F));
                super.setMax(Math.round((float) super.getMax() / 1000.0F));
                super.setMin(Math.round((float) super.getMin() / 1000.0F));
            }
        }

    }

    @SuppressLint("RestrictedApi")
    public void setSystemGestureExclusionRects(List<Rect> var1) {
        Preconditions.checkNotNull(var1, "rects must not be null");
        this.mUserGestureExclusionRects = var1;
        this.updateGestureExclusionRects();
    }

    @SuppressLint("RestrictedApi")
    public final void setThumbPos(int var1, Drawable var2, float var3, int var4) {
        int var5 = super.mCurrentMode;
        if (var5 != 3 && var5 != 6) {
            int var6 = this.getPaddingLeft();
            int var7 = this.getPaddingRight();
            int var8 = var2.getIntrinsicWidth();
            var5 = var2.getIntrinsicHeight();
            var7 = var1 - var6 - var7 - var8 + this.mThumbOffset * 2;
            var6 = (int) (var3 * (float) var7 + 0.5F);
            if (var4 == -2147483648) {
                Rect var9 = var2.getBounds();
                var4 = var9.top;
                var1 = var9.bottom;
            } else {
                var1 = var5 + var4;
            }

            var5 = var6;
            if (ViewUtils.isLayoutRtl(this)) {
                var5 = var6;
                if (super.mMirrorForRtl) {
                    var5 = var7 - var6;
                }
            }

            var6 = var5 + var8;
            Drawable var11 = this.getBackground();
            if (var11 != null) {
                var7 = this.getPaddingLeft() - this.mThumbOffset;
                int var10 = this.getPaddingTop();
                DrawableCompat.setHotspotBounds(var11, var5 + var7, var4 + var10, var7 + var6, var10 + var1);
            }

            var2.setBounds(var5, var4, var6, var1);
            this.updateGestureExclusionRects();
            this.mThumbPosX = var5 + this.getPaddingLeft() - (this.getPaddingLeft() - var8 / 2);
            this.updateSplitProgress();
        } else {
            this.setThumbPosInVertical(this.getHeight(), var2, var3, var4);
        }
    }

    public final void setThumbPosInVertical(int var1, Drawable var2, float var3, int var4) {
        int var5 = this.getPaddingTop();
        int var6 = this.getPaddingBottom();
        int var7 = var2.getIntrinsicHeight();
        int var8 = var2.getIntrinsicHeight();
        var6 = var1 - var5 - var6 - var8 + this.mThumbOffset * 2;
        var5 = (int) (var3 * (float) var6 + 0.5F);
        if (var4 == -2147483648) {
            Rect var9 = var2.getBounds();
            var4 = var9.left;
            var1 = var9.right;
        } else {
            var1 = var4 + var7;
        }

        var5 = var6 - var5;
        var8 += var5;
        Drawable var11 = this.getBackground();
        if (var11 != null) {
            int var10 = this.getPaddingLeft();
            var6 = this.getPaddingTop() - this.mThumbOffset;
            DrawableCompat.setHotspotBounds(var11, var4 + var10, var5 + var6, var10 + var1, var6 + var8);
        }

        var2.setBounds(var4, var5, var1, var8);
        this.mThumbPosX = var5 + var7 / 2 + this.getPaddingLeft();
    }

    public void setThumbTintColor(int var1) {
        ColorStateList var2 = this.colorToColorStateList(var1);
        if (!var2.equals(this.mDefaultActivatedThumbColor)) {
            this.mDefaultActivatedThumbColor = var2;
        }

    }

    public final void startDrag(MotionEvent var1) {
        this.setPressed(true);
        Drawable var2 = this.mThumb;
        if (var2 != null) {
            this.invalidate(var2.getBounds());
        }

        this.onStartTrackingTouch();
        this.trackTouchEvent(var1);
        this.attemptClaimDrag();
    }

    public final void startMuteAnimation() {
        this.cancelMuteAnimation();
        AnimatorSet var1 = this.mMuteAnimationSet;
        if (var1 != null) {
            var1.start();
        }

    }

    private boolean supportIsHoveringUIEnabled() {
        return IS_BASE_SDK_VERSION && SeslViewReflector.isHoveringUIEnabled(this);
    }

    private boolean supportIsInScrollingContainer() {
        return SeslViewReflector.isInScrollingContainer(this);
    }

    public final void trackHoverEvent(int var1) {
        int var2 = this.getWidth();
        int var3 = this.getPaddingLeft();
        int var4 = this.getPaddingRight();
        int var5 = this.getPaddingLeft();
        float var6 = 0.0F;
        float var7;
        if (var1 < var5) {
            var7 = 0.0F;
        } else if (var1 > var2 - this.getPaddingRight()) {
            var7 = 1.0F;
        } else {
            var7 = (float) (var1 - this.getPaddingLeft()) / (float) (var2 - var3 - var4);
            var6 = this.mTouchProgressOffset;
        }

        this.mHoveringLevel = (int) (var6 + var7 * (float) this.getMax());
    }

    @SuppressLint("RestrictedApi")
    public final void trackTouchEvent(MotionEvent var1) {
        int var2 = super.mCurrentMode;
        if (var2 != 3 && var2 != 6) {
            int var3;
            int var4;
            int var5;
            float var6;
            float var7;
            label55:
            {
                label63:
                {
                    var3 = Math.round(var1.getX());
                    var4 = Math.round(var1.getY());
                    var2 = this.getWidth();
                    var5 = var2 - this.getPaddingLeft() - this.getPaddingRight();
                    if (ViewUtils.isLayoutRtl(this) && super.mMirrorForRtl) {
                        if (var3 > var2 - this.getPaddingRight()) {
                            break label63;
                        }

                        if (var3 >= this.getPaddingLeft()) {
                            var6 = (float) (var5 - var3 + this.getPaddingLeft()) / (float) var5;
                            var7 = this.mTouchProgressOffset;
                            break label55;
                        }
                    } else {
                        if (var3 < this.getPaddingLeft()) {
                            break label63;
                        }

                        if (var3 <= var2 - this.getPaddingRight()) {
                            var6 = (float) (var3 - this.getPaddingLeft()) / (float) var5;
                            var7 = this.mTouchProgressOffset;
                            break label55;
                        }
                    }

                    var7 = 0.0F;
                    var6 = 1.0F;
                    break label55;
                }

                var6 = 0.0F;
                var7 = var6;
            }

            float var8;
            float var9;
            float var10;
            if (this.mIsSeamless) {
                var2 = super.getMax();
                var5 = super.getMin();
                var8 = 1.0F / (float) super.getMax();
                var9 = var6;
                if (var6 > 0.0F) {
                    var9 = var6;
                    if (var6 < 1.0F) {
                        var10 = var6 % var8;
                        var9 = var6;
                        if (var10 > var8 / 2.0F) {
                            var9 = var6 + (var8 - var10);
                        }
                    }
                }

                var6 = var9 * (float) (var2 - var5);
                var2 = super.getMin();
            } else {
                var5 = this.getMax();
                var2 = this.getMin();
                var8 = 1.0F / (float) this.getMax();
                var9 = var6;
                if (var6 > 0.0F) {
                    var9 = var6;
                    if (var6 < 1.0F) {
                        var10 = var6 % var8;
                        var9 = var6;
                        if (var10 > var8 / 2.0F) {
                            var9 = var6 + (var8 - var10);
                        }
                    }
                }

                var6 = var9 * (float) (var5 - var2);
                var2 = this.getMin();
            }

            var9 = (float) var2;
            this.setHotspot((float) var3, (float) var4);
            this.setProgressInternal(Math.round(var7 + var6 + var9), true, false);
        } else {
            this.trackTouchEventInVertical(var1);
        }
    }

    public final void trackTouchEventInVertical(MotionEvent var1) {
        int var2 = this.getHeight();
        int var3 = this.getPaddingTop();
        int var4 = this.getPaddingBottom();
        int var5 = Math.round(var1.getX());
        int var6 = var2 - Math.round(var1.getY());
        int var7 = this.getPaddingBottom();
        float var8 = 0.0F;
        float var9;
        if (var6 < var7) {
            var9 = 0.0F;
        } else if (var6 > var2 - this.getPaddingTop()) {
            var9 = 1.0F;
        } else {
            var9 = (float) (var6 - this.getPaddingBottom()) / (float) (var2 - var3 - var4);
            var8 = this.mTouchProgressOffset;
        }

        float var10 = (float) this.getMax();
        this.setHotspot((float) var5, (float) var6);
        this.setProgressInternal((int) (var8 + var9 * var10), true, false);
    }

    public final void updateBoundsForDualColor() {
        if (this.getCurrentDrawable() != null && !this.checkInvalidatedDualColorMode()) {
            Rect var1 = this.getCurrentDrawable().getBounds();
            this.mOverlapBackground.setBounds(var1);
        }

    }

    public void updateDrawableBounds(int var1, int var2) {
        super.updateDrawableBounds(var1, var2);
        this.updateThumbAndTrackPos(var1, var2);
        this.updateBoundsForDualColor();
    }

    public final void updateDualColorMode() {
        if (!this.checkInvalidatedDualColorMode()) {
            DrawableCompat.setTintList(this.mOverlapBackground, this.mOverlapNormalProgressColor);
            if (!this.mLargeFont) {
                if (this.getProgress() > this.mOverlapPoint) {
                    this.setProgressOverlapTintList(this.mOverlapActivatedProgressColor);
                    this.setThumbOverlapTintList(this.mOverlapActivatedThumbColor);
                } else {
                    this.setProgressTintList(this.mDefaultActivatedProgressColor);
                    this.setThumbTintList(this.mDefaultActivatedThumbColor);
                }
            }

            this.updateBoundsForDualColor();
        }
    }

    public final void updateGestureExclusionRects() {
        if (Build.VERSION.SDK_INT >= 29) {
            Drawable var1 = this.mThumb;
            if (var1 == null) {
                super.setSystemGestureExclusionRects(this.mUserGestureExclusionRects);
                return;
            }

            this.mGestureExclusionRects.clear();
            var1.copyBounds(this.mThumbRect);
            this.mGestureExclusionRects.add(this.mThumbRect);
            this.mGestureExclusionRects.addAll(this.mUserGestureExclusionRects);
            super.setSystemGestureExclusionRects(this.mGestureExclusionRects);
        }

    }

    public void updateHoverPopup() {
        if (IS_BASE_SDK_VERSION) {
            SeslHoverPopupWindowReflector.update(SeslViewReflector.semGetHoverPopup(this, true));
        }
    }

    @SuppressLint("RestrictedApi")
    public final void updateSplitProgress() {
        if (super.mCurrentMode == 4) {
            Drawable var1 = this.mSplitProgress;
            Rect var2 = this.getCurrentDrawable().getBounds();
            if (var1 != null) {
                if (super.mMirrorForRtl && ViewUtils.isLayoutRtl(this)) {
                    var1.setBounds(this.mThumbPosX, var2.top, this.getWidth() - this.getPaddingRight(), var2.bottom);
                } else {
                    var1.setBounds(this.getPaddingLeft(), var2.top, this.mThumbPosX, var2.bottom);
                }
            }

            int var3 = this.getWidth();
            int var4 = this.getHeight();
            var1 = this.mDivider;
            if (var1 != null) {
                float var5 = (float) var3 / 2.0F;
                float var6 = super.mDensity;
                var3 = (int) (var5 - var6 * 4.0F / 2.0F);
                float var7 = (float) var4 / 2.0F;
                var1.setBounds(var3, (int) (var7 - var6 * 22.0F / 2.0F), (int) (var5 + 4.0F * var6 / 2.0F), (int) (var7 + var6 * 22.0F / 2.0F));
            }

        }
    }

    public final void updateThumbAndTrackPos(int var1, int var2) {
        int var3 = super.mCurrentMode;
        if (var3 != 3 && var3 != 6) {
            var3 = var2 - this.getPaddingTop() - this.getPaddingBottom();
            Drawable var4 = this.getCurrentDrawable();
            Drawable var5 = this.mThumb;
            int var6 = Math.min(super.mMaxHeight, var3);
            if (var5 == null) {
                var2 = 0;
            } else {
                var2 = var5.getIntrinsicHeight();
            }

            int var7;
            if (var2 > var6) {
                var3 = (var3 - var2) / 2;
                var7 = (var2 - var6) / 2;
                var7 += var3;
                var3 = var3;
                var2 = var7;
            } else {
                var7 = (var3 - var6) / 2;
                var3 = (var6 - var2) / 2 + var7;
                var2 = var7;
            }

            if (var4 != null) {
                var4.setBounds(0, var2, var1 - this.getPaddingRight() - this.getPaddingLeft(), var6 + var2);
            }

            if (var5 != null) {
                this.setThumbPos(var1, var5, this.getScale(), var3);
            }

            this.updateSplitProgress();
        } else {
            this.updateThumbAndTrackPosInVertical(var1, var2);
        }
    }

    public final void updateThumbAndTrackPosInVertical(int var1, int var2) {
        int var3 = var1 - this.getPaddingLeft() - this.getPaddingRight();
        Drawable var4 = this.getCurrentDrawable();
        Drawable var5 = this.mThumb;
        int var6 = Math.min(super.mMaxWidth, var3);
        if (var5 == null) {
            var1 = 0;
        } else {
            var1 = var5.getIntrinsicWidth();
        }

        int var7;
        if (var1 > var6) {
            var7 = (var3 - var1) / 2;
            var1 = (var1 - var6) / 2 + var7;
        } else {
            var7 = (var3 - var6) / 2;
            var6 = (var6 - var1) / 2 + var7;
            var1 = var7;
            var7 = var6;
        }

        if (var4 != null) {
            var4.setBounds(var1, 0, var3 - var1, var2 - this.getPaddingBottom() - this.getPaddingTop());
        }

        if (var5 != null) {
            this.setThumbPosInVertical(var2, var5, this.getScale(), var7);
        }

    }

    public final void updateWarningMode(int var1) {
        int var2 = super.mCurrentMode;
        boolean var3 = true;
        if (var2 == 1) {
            boolean var4;
            if (var1 == this.getMax()) {
                var4 = var3;
            } else {
                var4 = false;
            }

            if (var4) {
                this.setProgressOverlapTintList(this.mOverlapActivatedProgressColor);
                this.setThumbOverlapTintList(this.mOverlapActivatedThumbColor);
            } else {
                this.setProgressTintList(this.mDefaultActivatedProgressColor);
                this.setThumbTintList(this.mDefaultActivatedThumbColor);
            }
        }

    }

    public boolean verifyDrawable(Drawable var1) {
        boolean var2;
        if (var1 != this.mThumb && var1 != this.mTickMark && !super.verifyDrawable(var1)) {
            var2 = false;
        } else {
            var2 = true;
        }

        return var2;
    }

    private int getColor(Context context, int colorResId) {
        TypedValue typedValue = new TypedValue();
        TypedArray typedArray = context.obtainStyledAttributes(typedValue.data, new int[]{colorResId});
        int color = typedArray.getColor(0, 0);
        typedArray.recycle();
        return color;
    }

    private class SliderDrawable extends Drawable {
        public final int ANIMATION_DURATION;
        public final Paint mPaint;
        public final float mSliderMaxWidth;
        public final float mSliderMinWidth;
        public final SeslAbsSeekBar.SliderDrawable.SliderState mState;
        public int mAlpha;
        public int mColor;
        public ColorStateList mColorStateList;
        public boolean mIsStateChanged;
        public boolean mIsVertical;
        public ValueAnimator mPressedAnimator;
        public float mRadius;
        public ValueAnimator mReleasedAnimator;

        public SliderDrawable(float var2, float var3, ColorStateList var4) {
            this(var2, var3, var4, false);
        }

        public SliderDrawable(float var2, float var3, ColorStateList var4, boolean var5) {
            this.mPaint = new Paint();
            this.ANIMATION_DURATION = 250;
            this.mIsStateChanged = false;
            this.mAlpha = 255;
            this.mState = new SeslAbsSeekBar.SliderDrawable.SliderState();
            this.mPaint.setStyle(Paint.Style.STROKE);
            this.mPaint.setStrokeCap(Paint.Cap.ROUND);
            this.mColorStateList = var4;
            this.mColor = var4.getDefaultColor();
            this.mPaint.setColor(this.mColor);
            this.mPaint.setStrokeWidth(var2);
            this.mSliderMinWidth = var2;
            this.mSliderMaxWidth = var3;
            this.mRadius = var2 / 2.0F;
            this.mIsVertical = var5;
            this.initAnimator();
        }

        public void draw(Canvas var1) {
            int var2 = this.mPaint.getAlpha();
            this.mPaint.setAlpha(this.modulateAlpha(var2, this.mAlpha));
            var1.save();
            float var3;
            float var4;
            if (!this.mIsVertical) {
                var3 = (float) (SeslAbsSeekBar.this.getWidth() - SeslAbsSeekBar.this.getPaddingLeft() - SeslAbsSeekBar.this.getPaddingRight());
                var4 = this.mRadius;
                var1.drawLine(var4, (float) SeslAbsSeekBar.this.getHeight() / 2.0F, var3 - var4, (float) SeslAbsSeekBar.this.getHeight() / 2.0F, this.mPaint);
            } else {
                var4 = (float) (SeslAbsSeekBar.this.getHeight() - SeslAbsSeekBar.this.getPaddingTop() - SeslAbsSeekBar.this.getPaddingBottom());
                var3 = this.mRadius;
                var1.drawLine((float) SeslAbsSeekBar.this.getWidth() / 2.0F, var4 - var3, (float) SeslAbsSeekBar.this.getWidth() / 2.0F, this.mRadius, this.mPaint);
            }

            var1.restore();
            this.mPaint.setAlpha(var2);
        }

        public ConstantState getConstantState() {
            return this.mState;
        }

        public int getIntrinsicHeight() {
            return (int) this.mSliderMaxWidth;
        }

        public int getIntrinsicWidth() {
            return (int) this.mSliderMaxWidth;
        }

        @SuppressLint("WrongConstant")
        public int getOpacity() {
            Paint var1 = this.mPaint;
            if (var1.getXfermode() == null) {
                int var2 = var1.getAlpha();
                if (var2 == 0) {
                    return -2;
                }

                if (var2 == 255) {
                    return -1;
                }
            }

            return -3;
        }

        public final void initAnimator() {
            float var1 = this.mSliderMinWidth;
            float var2 = this.mSliderMaxWidth;
            this.mPressedAnimator = ValueAnimator.ofFloat(new float[]{var1, var2});
            this.mPressedAnimator.setDuration(250L);
            this.mPressedAnimator.setInterpolator(SeslAnimationUtils.SINE_IN_OUT_80);
            this.mPressedAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator var1) {
                    float var2 = (Float) var1.getAnimatedValue();
                    SliderDrawable.this.invalidateTrack(var2);
                }
            });
            this.mReleasedAnimator = ValueAnimator.ofFloat(new float[]{var2, var1});
            this.mReleasedAnimator.setDuration(250L);
            this.mReleasedAnimator.setInterpolator(SeslAnimationUtils.SINE_IN_OUT_80);
            this.mReleasedAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator var1) {
                    float var2 = (Float) var1.getAnimatedValue();
                    SliderDrawable.this.invalidateTrack(var2);
                }
            });
        }

        public void invalidateTrack(float var1) {
            this.setStrokeWidth(var1);
            this.invalidateSelf();
        }

        public boolean isStateful() {
            return true;
        }

        public final int modulateAlpha(int var1, int var2) {
            return var1 * (var2 + (var2 >>> 7)) >>> 8;
        }

        public boolean onStateChange(int[] var1) {
            boolean var2 = super.onStateChange(var1);
            int var3 = this.mColorStateList.getColorForState(var1, this.mColor);
            if (this.mColor != var3) {
                this.mColor = var3;
                this.mPaint.setColor(this.mColor);
                this.invalidateSelf();
            }

            int var4 = var1.length;
            boolean var5 = false;
            byte var6 = 0;
            byte var7 = var6;
            byte var8 = var6;

            for (var3 = var6; var3 < var4; var8 = var6) {
                int var9 = var1[var3];
                if (var9 == 16842910) {
                    var6 = 1;
                } else {
                    var6 = var8;
                    if (var9 == 16842919) {
                        var7 = 1;
                        var6 = var8;
                    }
                }

                ++var3;
            }

            boolean var10 = var5;
            if (var8 != 0) {
                var10 = var5;
                if (var7 != 0) {
                    var10 = true;
                }
            }

            this.startSliderAnimation(var10);
            return var2;
        }

        public void setAlpha(int var1) {
            this.mAlpha = var1;
            this.invalidateSelf();
        }

        public void setColorFilter(ColorFilter var1) {
            this.mPaint.setColorFilter(var1);
            this.invalidateSelf();
        }

        public void setStrokeWidth(float var1) {
            this.mPaint.setStrokeWidth(var1);
            this.mRadius = var1 / 2.0F;
        }

        public void setTintList(ColorStateList var1) {
            super.setTintList(var1);
            if (var1 != null) {
                this.mColorStateList = var1;
                this.mColor = this.mColorStateList.getDefaultColor();
                this.mPaint.setColor(this.mColor);
                this.invalidateSelf();
            }

        }

        public final void startPressedAnimation() {
            if (!this.mPressedAnimator.isRunning()) {
                if (this.mReleasedAnimator.isRunning()) {
                    this.mReleasedAnimator.cancel();
                }

                this.mPressedAnimator.setFloatValues(new float[]{this.mSliderMinWidth, this.mSliderMaxWidth});
                this.mPressedAnimator.start();
            }
        }

        public final void startReleasedAnimation() {
            if (!this.mReleasedAnimator.isRunning()) {
                if (this.mPressedAnimator.isRunning()) {
                    this.mPressedAnimator.cancel();
                }

                this.mReleasedAnimator.setFloatValues(new float[]{this.mSliderMaxWidth, this.mSliderMinWidth});
                this.mReleasedAnimator.start();
            }
        }

        public final void startSliderAnimation(boolean var1) {
            if (this.mIsStateChanged != var1) {
                if (var1) {
                    this.startPressedAnimation();
                } else {
                    this.startReleasedAnimation();
                }

                this.mIsStateChanged = var1;
            }

        }

        private class SliderState extends ConstantState {
            public SliderState() {
            }

            public int getChangingConfigurations() {
                return 0;
            }

            public Drawable newDrawable() {
                return SliderDrawable.this;
            }
        }
    }

    private class ThumbDrawable extends Drawable {
        public final int PRESSED_DURATION = 100;
        public final int RELEASED_DURATION = 300;
        public final Paint mPaint = new Paint(1);
        public final int mRadius;
        public int mAlpha = 255;
        public int mColor;
        public ColorStateList mColorStateList;
        public boolean mIsStateChanged = false;
        public boolean mIsVertical = false;
        public int mRadiusForAni;
        public ValueAnimator mThumbPressed;
        public ValueAnimator mThumbReleased;

        public ThumbDrawable(int var2, ColorStateList var3, boolean var4) {
            this.mRadiusForAni = var2;
            this.mRadius = var2;
            this.mColorStateList = var3;
            this.mColor = var3.getDefaultColor();
            this.mPaint.setStyle(Paint.Style.FILL);
            this.mPaint.setColor(this.mColor);
            this.mIsVertical = var4;
            this.initAnimation();
        }

        public void draw(Canvas var1) {
            int var2 = this.mPaint.getAlpha();
            this.mPaint.setAlpha(this.modulateAlpha(var2, this.mAlpha));
            var1.save();
            if (!this.mIsVertical) {
                var1.drawCircle((float) SeslAbsSeekBar.this.mThumbPosX, (float) SeslAbsSeekBar.this.getHeight() / 2.0F, (float) this.mRadiusForAni, this.mPaint);
            } else {
                var1.drawCircle((float) SeslAbsSeekBar.this.getWidth() / 2.0F, (float) SeslAbsSeekBar.this.mThumbPosX, (float) this.mRadiusForAni, this.mPaint);
            }

            var1.restore();
            this.mPaint.setAlpha(var2);
        }

        public int getIntrinsicHeight() {
            return this.mRadius * 2;
        }

        public int getIntrinsicWidth() {
            return this.mRadius * 2;
        }

        @SuppressLint("WrongConstant")
        public int getOpacity() {
            Paint var1 = this.mPaint;
            if (var1.getXfermode() == null) {
                int var2 = var1.getAlpha();
                if (var2 == 0) {
                    return -2;
                }

                if (var2 == 255) {
                    return -1;
                }
            }

            return -3;
        }

        public void initAnimation() {
            this.mThumbPressed = ValueAnimator.ofFloat(new float[]{(float) this.mRadius, 0.0F});
            this.mThumbPressed.setDuration(100L);
            this.mThumbPressed.setInterpolator(new LinearInterpolator());
            this.mThumbPressed.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator var1) {
                    float var2 = (Float) var1.getAnimatedValue();
                    ThumbDrawable.this.setRadius((int) var2);
                    ThumbDrawable.this.invalidateSelf();
                }
            });
            this.mThumbReleased = ValueAnimator.ofFloat(new float[]{0.0F, (float) this.mRadius});
            this.mThumbReleased.setDuration(300L);
            this.mThumbReleased.setInterpolator(SeslAnimationUtils.SINE_IN_OUT_90);
            this.mThumbReleased.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator var1) {
                    float var2 = (Float) var1.getAnimatedValue();
                    ThumbDrawable.this.setRadius((int) var2);
                    ThumbDrawable.this.invalidateSelf();
                }
            });
        }

        public boolean isStateful() {
            return true;
        }

        public final int modulateAlpha(int var1, int var2) {
            return var1 * (var2 + (var2 >>> 7)) >>> 8;
        }

        public boolean onStateChange(int[] var1) {
            boolean var2 = super.onStateChange(var1);
            int var3 = this.mColorStateList.getColorForState(var1, this.mColor);
            if (this.mColor != var3) {
                this.mColor = var3;
                this.mPaint.setColor(this.mColor);
                this.invalidateSelf();
            }

            int var4 = var1.length;
            boolean var5 = false;
            byte var6 = 0;
            byte var7 = var6;
            byte var8 = var6;

            for (var3 = var6; var3 < var4; var8 = var6) {
                int var9 = var1[var3];
                if (var9 == 16842910) {
                    var6 = 1;
                } else {
                    var6 = var8;
                    if (var9 == 16842919) {
                        var7 = 1;
                        var6 = var8;
                    }
                }

                ++var3;
            }

            boolean var10 = var5;
            if (var8 != 0) {
                var10 = var5;
                if (var7 != 0) {
                    var10 = true;
                }
            }

            this.startThumbAnimation(var10);
            return var2;
        }

        public void setAlpha(int var1) {
            this.mAlpha = var1;
            this.invalidateSelf();
        }

        public void setColorFilter(ColorFilter var1) {
            this.mPaint.setColorFilter(var1);
            this.invalidateSelf();
        }

        public final void setRadius(int var1) {
            this.mRadiusForAni = var1;
        }

        public void setTintList(ColorStateList var1) {
            super.setTintList(var1);
            if (var1 != null) {
                this.mColorStateList = var1;
                this.mColor = this.mColorStateList.getDefaultColor();
                this.mPaint.setColor(this.mColor);
                this.invalidateSelf();
            }

        }

        public final void startPressedAnimation() {
            if (!this.mThumbPressed.isRunning()) {
                if (this.mThumbReleased.isRunning()) {
                    this.mThumbReleased.cancel();
                }

                this.mThumbPressed.start();
            }
        }

        public final void startReleasedAnimation() {
            if (!this.mThumbReleased.isRunning()) {
                if (this.mThumbPressed.isRunning()) {
                    this.mThumbPressed.cancel();
                }

                this.mThumbReleased.start();
            }
        }

        public final void startThumbAnimation(boolean var1) {
            if (this.mIsStateChanged != var1) {
                if (var1) {
                    this.startPressedAnimation();
                } else {
                    this.startReleasedAnimation();
                }

                this.mIsStateChanged = var1;
            }

        }
    }
}
