package de.dlyt.yanndroid.oneui.sesl.widget;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
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
import android.graphics.Region;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.LinearInterpolator;
import android.widget.AbsSeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.animation.SeslAnimationUtils;
import androidx.appcompat.graphics.drawable.DrawableWrapper;
import androidx.appcompat.widget.DrawableUtils;
import androidx.appcompat.widget.ViewUtils;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.util.Preconditions;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.reflect.view.SeslViewReflector;
import androidx.reflect.widget.SeslHoverPopupWindowReflector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.dlyt.yanndroid.oneui.R;
import de.dlyt.yanndroid.oneui.widget.ProgressBar;

public abstract class SeslAbsSeekBar extends ProgressBar {
    private boolean mIsOneUI4;
    private static final int HOVER_DETECT_TIME = 200;
    private static final int HOVER_POPUP_WINDOW_GRAVITY_CENTER_HORIZONTAL_ON_POINT = 513;
    private static final int HOVER_POPUP_WINDOW_GRAVITY_TOP_ABOVE = 12336;
    private static final boolean IS_BASE_SDK_VERSION = Build.VERSION.SDK_INT <= 23;
    private static final int MUTE_VIB_DISTANCE_LVL = 400;
    private static final int MUTE_VIB_DURATION = 500;
    private static final int MUTE_VIB_TOTAL = 4;
    private static final int NO_ALPHA = 255;
    static final float SCALE_FACTOR = 1000.0f;
    private static final String TAG = "SeslAbsSeekBar";
    private boolean mAllowedSeekBarAnimation = false;
    private int mCurrentProgressLevel;
    private ColorStateList mDefaultActivatedProgressColor;
    private ColorStateList mDefaultActivatedThumbColor;
    private ColorStateList mDefaultNormalProgressColor;
    private ColorStateList mDefaultSecondaryProgressColor;
    private float mDisabledAlpha;
    private Drawable mDivider;
    private final List<Rect> mGestureExclusionRects = new ArrayList();
    private boolean mHasThumbTint = false;
    private boolean mHasThumbTintMode = false;
    private boolean mHasTickMarkTint = false;
    private boolean mHasTickMarkTintMode = false;
    private int mHoveringLevel = 0;
    private boolean mIsDragging;
    private boolean mIsDraggingForSliding = false;
    private boolean mIsFirstSetProgress = false;
    private boolean mIsLightTheme;
    protected boolean mIsSeamless = false;
    private boolean mIsSetModeCalled = false;
    private boolean mIsTouchDisabled = false;
    boolean mIsUserSeekable = true;
    private int mKeyProgressIncrement = 1;
    private boolean mLargeFont = false;
    private AnimatorSet mMuteAnimationSet;
    private ColorStateList mOverlapActivatedProgressColor;
    private Drawable mOverlapBackground;
    private ColorStateList mOverlapNormalProgressColor;
    private int mOverlapPoint = -1;
    private int mPreviousHoverPopupType = 0;
    private int mScaledTouchSlop;
    private boolean mSetDualColorMode = false;
    private Drawable mSplitProgress;
    private boolean mSplitTrack;
    private final Rect mTempRect = new Rect();
    private Drawable mThumb;
    private int mThumbOffset;
    private int mThumbPosX;
    private int mThumbRadius;
    private final Rect mThumbRect = new Rect();
    private ColorStateList mThumbTintList = null;
    private PorterDuff.Mode mThumbTintMode = null;
    private Drawable mTickMark;
    private ColorStateList mTickMarkTintList = null;
    private PorterDuff.Mode mTickMarkTintMode = null;
    private float mTouchDownX;
    private float mTouchDownY;
    float mTouchProgressOffset;
    private int mTrackMaxWidth;
    private int mTrackMinWidth;
    private boolean mUseMuteAnimation = false;
    private List<Rect> mUserGestureExclusionRects = Collections.emptyList();
    private ValueAnimator mValueAnimator;

    public SeslAbsSeekBar(Context context) {
        super(context);
    }

    public SeslAbsSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SeslAbsSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    @SuppressLint("RestrictedApi")
    public SeslAbsSeekBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        mIsOneUI4 = context.getTheme().obtainStyledAttributes(new int[]{R.attr.isOneUI4}).getBoolean(0, false);

        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SeslAbsSeekBar, defStyleAttr, defStyleRes);
        if (Build.VERSION.SDK_INT >= 29) {
            saveAttributeDataForStyleable(context, R.styleable.SeslAbsSeekBar, attrs, a, defStyleAttr, defStyleRes);
        }

        final Drawable thumb = a.getDrawable(R.styleable.SeslAbsSeekBar_android_thumb);
        setThumb(thumb);

        if (a.hasValue(R.styleable.SeslAbsSeekBar_android_thumbTintMode)) {
            mThumbTintMode = DrawableUtils.parseTintMode(a.getInt(R.styleable.SeslAbsSeekBar_android_thumbTintMode, -1), mThumbTintMode);
            mHasThumbTintMode = true;
        }

        if (a.hasValue(R.styleable.SeslAbsSeekBar_android_thumbTint)) {
            mThumbTintList = a.getColorStateList(R.styleable.SeslAbsSeekBar_android_thumbTint);
            mHasThumbTint = true;
        }

        final Drawable tickMark = a.getDrawable(R.styleable.SeslAbsSeekBar_tickMark);
        setTickMark(tickMark);

        if (a.hasValue(R.styleable.SeslAbsSeekBar_tickMarkTintMode)) {
            mTickMarkTintMode = DrawableUtils.parseTintMode(a.getInt(R.styleable.SeslAbsSeekBar_tickMarkTintMode, -1), mTickMarkTintMode);
            mHasTickMarkTintMode = true;
        }

        if (a.hasValue(R.styleable.SeslAbsSeekBar_tickMarkTint)) {
            mTickMarkTintList = a.getColorStateList(R.styleable.SeslAbsSeekBar_tickMarkTint);
            mHasTickMarkTint = true;
        }

        mSplitTrack = a.getBoolean(R.styleable.SeslAbsSeekBar_android_splitTrack, false);

        mTrackMinWidth = a.getDimensionPixelSize(R.styleable.SeslAbsSeekBar_seslTrackMinWidth, Math.round(getResources().getDimension(mIsOneUI4 ? R.dimen.sesl4_seekbar_track_height : R.dimen.sesl_seekbar_track_height)));
        mTrackMaxWidth = a.getDimensionPixelSize(R.styleable.SeslAbsSeekBar_seslTrackMaxWidth, Math.round(getResources().getDimension(R.dimen.sesl_seekbar_track_height_expand)));
        mThumbRadius = a.getDimensionPixelSize(R.styleable.SeslAbsSeekBar_seslThumbRadius, Math.round(getResources().getDimension(mIsOneUI4 ? R.dimen.sesl4_seekbar_thumb_radius : R.dimen.sesl_seekbar_thumb_radius)));

        final int thumbOffset = a.getDimensionPixelOffset(R.styleable.SeslAbsSeekBar_android_thumbOffset, getThumbOffset());
        setThumbOffset(thumbOffset);

        if (a.hasValue(R.styleable.SeslAbsSeekBar_seslSeekBarMode)) {
            mCurrentMode = a.getInt(R.styleable.SeslAbsSeekBar_seslSeekBarMode, MODE_STANDARD);
        }

        final boolean useDisabledAlpha = a.getBoolean(R.styleable.SeslAbsSeekBar_useDisabledAlpha, true);
        a.recycle();

        if (useDisabledAlpha) {
            final TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.AppCompatTheme, 0, 0);
            mDisabledAlpha = ta.getFloat(R.styleable.AppCompatTheme_android_disabledAlpha, 0.5f);
            ta.recycle();
        } else {
            mDisabledAlpha = 1.0f;
        }

        applyThumbTint();
        applyTickMarkTint();

        mScaledTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

        mDefaultNormalProgressColor = colorToColorStateList(ResourcesCompat.getColor(getResources(), mIsOneUI4 ? R.color.sesl4_seekbar_control_color_default : R.color.sesl_seekbar_control_color_default, context.getTheme()));
        mDefaultSecondaryProgressColor = colorToColorStateList(getResources().getColor(R.color.sesl_seekbar_control_color_secondary));
        mDefaultActivatedProgressColor = colorToColorStateList(getColor(context, R.attr.colorPrimary));
        mOverlapNormalProgressColor = colorToColorStateList(getResources().getColor(R.color.sesl_seekbar_overlap_color_default));
        mOverlapActivatedProgressColor = colorToColorStateList(getResources().getColor(mIsOneUI4 ? R.color.sesl4_seekbar_overlap_color_activated : R.color.sesl_seekbar_overlap_color_activated));

        mDefaultActivatedThumbColor = getThumbTintList();
        if (mDefaultActivatedThumbColor == null) {
            int[][] state = {new int[]{android.R.attr.state_enabled}, new int[]{-android.R.attr.state_enabled}};
            int[] colors = new int[2];
            colors[0] = getColor(context, R.attr.colorPrimary);
            colors[1] = getResources().getColor(R.color.sesl_seekbar_disable_color_activated);
            mDefaultActivatedThumbColor = new ColorStateList(state, colors);
        }

        mAllowedSeekBarAnimation = getResources().getBoolean(R.bool.sesl_seekbar_sliding_animation);
        if (mAllowedSeekBarAnimation) {
            initMuteAnimation();
        }

        if (mCurrentMode != MODE_STANDARD) {
            setMode(mCurrentMode);
        }
    }

    public void setThumb(Drawable thumb) {
        final boolean needUpdate;
        if (mThumb != null && thumb != mThumb) {
            mThumb.setCallback(null);
            needUpdate = true;
        } else {
            needUpdate = false;
        }

        if (thumb != null) {
            thumb.setCallback(this);
            if (canResolveLayoutDirection()) {
                DrawableCompat.setLayoutDirection(thumb, ViewCompat.getLayoutDirection(this));
            }

            if (mCurrentMode == MODE_VERTICAL || mCurrentMode == MODE_EXPAND_VERTICAL) {
                mThumbOffset = thumb.getIntrinsicHeight() / 2;
            } else {
                mThumbOffset = thumb.getIntrinsicWidth() / 2;
            }

            if (needUpdate && (thumb.getIntrinsicWidth() != mThumb.getIntrinsicWidth() || thumb.getIntrinsicHeight() != mThumb.getIntrinsicHeight())) {
                requestLayout();
            }
        }

        mThumb = thumb;

        applyThumbTint();
        invalidate();

        if (needUpdate) {
            updateThumbAndTrackPos(getWidth(), getHeight());
            if (thumb != null && thumb.isStateful()) {
                int[] state = getDrawableState();
                thumb.setState(state);
            }
        }
    }

    public Drawable getThumb() {
        return mThumb;
    }

    public void setThumbTintList(@Nullable ColorStateList tint) {
        mThumbTintList = tint;
        mHasThumbTint = true;

        applyThumbTint();
        mDefaultActivatedThumbColor = tint;
    }

    @Nullable
    public ColorStateList getThumbTintList() {
        return mThumbTintList;
    }

    public void setThumbTintMode(@Nullable PorterDuff.Mode tintMode) {
        mThumbTintMode = tintMode;
        mHasThumbTintMode = true;
        applyThumbTint();
    }

    @Nullable
    public PorterDuff.Mode getThumbTintMode() {
        return mThumbTintMode;
    }

    private void applyThumbTint() {
        if (mThumb != null && (mHasThumbTint || mHasThumbTintMode)) {
            mThumb = mThumb.mutate();

            if (mHasThumbTint) {
                DrawableCompat.setTintList(mThumb, mThumbTintList);
            }

            if (mHasThumbTintMode) {
                DrawableCompat.setTintMode(mThumb, mThumbTintMode);
            }

            if (mThumb.isStateful()) {
                mThumb.setState(getDrawableState());
            }
        }
    }

    public int getThumbOffset() {
        return mThumbOffset;
    }

    public void setThumbOffset(int thumbOffset) {
        mThumbOffset = thumbOffset;
        invalidate();
    }

    public void setSplitTrack(boolean splitTrack) {
        mSplitTrack = splitTrack;
        invalidate();
    }

    public boolean getSplitTrack() {
        return mSplitTrack;
    }

    public void setTickMark(Drawable tickMark) {
        if (mTickMark != null) {
            mTickMark.setCallback(null);
        }

        mTickMark = tickMark;

        if (tickMark != null) {
            tickMark.setCallback(this);
            DrawableCompat.setLayoutDirection(tickMark, ViewCompat.getLayoutDirection(this));
            if (tickMark.isStateful()) {
                tickMark.setState(getDrawableState());
            }
            applyTickMarkTint();
        }

        invalidate();
    }

    public Drawable getTickMark() {
        return mTickMark;
    }

    public void showTickMarkDots(boolean show) {
        if (show) {
            setTickMark(getContext().getDrawable(R.drawable.oui_seekbar_tick_mark));
            super.setProgressTintList(colorToColorStateList(getResources().getColor(R.color.transparent)));
            super.setProgressBackgroundTintList(colorToColorStateList(getResources().getColor(R.color.sesl4_seekbar_control_color_default)));
        } else {
            setTickMark(null);
            super.setProgressTintList(mDefaultActivatedProgressColor);
            super.setProgressBackgroundTintList(mDefaultNormalProgressColor);
        }
    }

    public void setTickMarkTintList(@Nullable ColorStateList tint) {
        mTickMarkTintList = tint;
        mHasTickMarkTint = true;

        applyTickMarkTint();
    }

    @Nullable
    public ColorStateList getTickMarkTintList() {
        return mTickMarkTintList;
    }

    public void setTickMarkTintMode(@Nullable PorterDuff.Mode tintMode) {
        mTickMarkTintMode = tintMode;
        mHasTickMarkTintMode = true;

        applyTickMarkTint();
    }

    @Nullable
    public PorterDuff.Mode getTickMarkTintMode() {
        return mTickMarkTintMode;
    }

    private void applyTickMarkTint() {
        if (mTickMark != null && (mHasTickMarkTint || mHasTickMarkTintMode)) {
            mTickMark = mTickMark.mutate();

            if (mHasTickMarkTint) {
                DrawableCompat.setTintList(mTickMark, mTickMarkTintList);
            }

            if (mHasTickMarkTintMode) {
                DrawableCompat.setTintMode(mTickMark, mTickMarkTintMode);
            }

            if (mTickMark.isStateful()) {
                mTickMark.setState(getDrawableState());
            }
        }
    }

    public void setKeyProgressIncrement(int increment) {
        mKeyProgressIncrement = increment < 0 ? -increment : increment;
    }

    public int getKeyProgressIncrement() {
        return mKeyProgressIncrement;
    }

    @Override
    public synchronized void setMin(int min) {
        if (mIsSeamless) {
            min = Math.round(((float) min) * SCALE_FACTOR);
        }
        super.setMin(min);
        int range = getMax() - getMin();

        if ((mKeyProgressIncrement == 0) || (range / mKeyProgressIncrement > 20)) {
            setKeyProgressIncrement(Math.max(1, Math.round((float) range / 20)));
        }
    }

    @Override
    public synchronized void setMax(int max) {
        if (mIsSeamless) {
            max = Math.round(((float) max) * SCALE_FACTOR);
        }
        super.setMax(max);
        int range = getMax() - getMin();

        if ((mKeyProgressIncrement == 0) || (range / mKeyProgressIncrement > 20)) {
            setKeyProgressIncrement(Math.max(1, Math.round((float) range / 20)));
        }
    }

    @Override
    protected boolean verifyDrawable(@NonNull Drawable who) {
        return who == mThumb || who == mTickMark || super.verifyDrawable(who);
    }

    @Override
    public void jumpDrawablesToCurrentState() {
        super.jumpDrawablesToCurrentState();

        if (mThumb != null) {
            mThumb.jumpToCurrentState();
        }

        if (mTickMark != null) {
            mTickMark.jumpToCurrentState();
        }
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();

        final Drawable progressDrawable = getProgressDrawable();
        if (progressDrawable != null && mDisabledAlpha < 1.0f) {
            final int alpha = isEnabled() ? NO_ALPHA : (int) (NO_ALPHA * mDisabledAlpha);
            progressDrawable.setAlpha(alpha);
            if (mOverlapBackground != null) {
                mOverlapBackground.setAlpha(alpha);
            }
        }

        if (mThumb != null && mHasThumbTint) {
            if (!isEnabled()) {
                DrawableCompat.setTintList(mThumb, null);
            } else {
                DrawableCompat.setTintList(mThumb, mDefaultActivatedThumbColor);
                updateDualColorMode();
            }
        }
        if (mSetDualColorMode && progressDrawable != null && progressDrawable.isStateful() && mOverlapBackground != null) {
            mOverlapBackground.setState(getDrawableState());
        }

        final Drawable thumb = mThumb;
        if (thumb != null && thumb.isStateful() && thumb.setState(getDrawableState())) {
            invalidateDrawable(thumb);
        }

        final Drawable tickMark = mTickMark;
        if (tickMark != null && tickMark.isStateful() && tickMark.setState(getDrawableState())) {
            invalidateDrawable(tickMark);
        }
    }

    @Override
    public void drawableHotspotChanged(float x, float y) {
        super.drawableHotspotChanged(x, y);

        if (mThumb != null) {
            DrawableCompat.setHotspot(mThumb, x, y);
        }
    }

    @Override
    protected void onVisualProgressChanged(int id, float scale) {
        super.onVisualProgressChanged(id, scale);

        if (id == android.R.id.progress) {
            final Drawable thumb = mThumb;
            if (thumb != null) {
                setThumbPos(getWidth(), thumb, scale, Integer.MIN_VALUE);
                invalidate();
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        updateThumbAndTrackPos(w, h);
    }

    private void updateThumbAndTrackPos(int w, int h) {
        if (mCurrentMode == MODE_VERTICAL || mCurrentMode == MODE_EXPAND_VERTICAL) {
            updateThumbAndTrackPosInVertical(w, h);
            return;
        }

        final int paddedHeight = h - getPaddingTop() - getPaddingBottom();
        final Drawable track = getCurrentDrawable();
        final Drawable thumb = mThumb;

        final int trackHeight = Math.min(mMaxHeight, paddedHeight);
        final int thumbHeight = thumb == null ? 0 : thumb.getIntrinsicHeight();

        final int trackOffset;
        final int thumbOffset;
        if (thumbHeight > trackHeight) {
            final int offsetHeight = (paddedHeight - thumbHeight) / 2;
            trackOffset = offsetHeight + (thumbHeight - trackHeight) / 2;
            thumbOffset = offsetHeight;
        } else {
            final int offsetHeight = (paddedHeight - trackHeight) / 2;
            trackOffset = offsetHeight;
            thumbOffset = offsetHeight + (trackHeight - thumbHeight) / 2;
        }

        if (track != null) {
            final int trackWidth = w - getPaddingRight() - getPaddingLeft();
            track.setBounds(0, trackOffset, trackWidth, trackOffset + trackHeight);
        }

        if (thumb != null) {
            setThumbPos(w, thumb, getScale(), thumbOffset);
        }

        updateSplitProgress();
    }

    private void updateThumbAndTrackPosInVertical(int w, int h) {
        final int paddedWidth = w - getPaddingLeft() - getPaddingRight();
        final Drawable track = getCurrentDrawable();
        final Drawable thumb = mThumb;

        final int trackWidth = Math.min(mMaxWidth, paddedWidth);
        final int thumbWidth = thumb == null ? 0 : thumb.getIntrinsicWidth();

        final int trackOffset;
        final int thumbOffset;
        if (thumbWidth > trackWidth) {
            final int offsetHeight = (paddedWidth - thumbWidth) / 2;
            trackOffset = offsetHeight + (thumbWidth - trackWidth) / 2;
            thumbOffset = offsetHeight;
        } else {
            final int offsetHeight = (paddedWidth - trackWidth) / 2;
            trackOffset = offsetHeight;
            thumbOffset = offsetHeight + (trackWidth - thumbWidth) / 2;
        }

        if (track != null) {
            final int trackHeight = h - getPaddingBottom() - getPaddingTop();
            track.setBounds(trackOffset, 0, paddedWidth - trackOffset, trackHeight);
        }

        if (thumb != null) {
            setThumbPosInVertical(h, thumb, getScale(), thumbOffset);
        }
    }

    private float getScale() {
        int min = getMin();
        int max = getMax();
        int range = max - min;
        return range > 0 ? (getProgress() - min) / (float) range : 0;
    }

    @SuppressLint("RestrictedApi")
    private void setThumbPos(int w, Drawable thumb, float scale, int offset) {
        if (mCurrentMode == MODE_VERTICAL || mCurrentMode == MODE_EXPAND_VERTICAL) {
            setThumbPosInVertical(getHeight(), thumb, scale, offset);
            return;
        }

        int available = w - getPaddingLeft() - getPaddingRight();
        final int thumbWidth = thumb.getIntrinsicWidth();
        final int thumbHeight = thumb.getIntrinsicHeight();
        available -= thumbWidth;

        available += mThumbOffset * 2;

        final int thumbPos = (int) (scale * available + 0.5f);

        final int top, bottom;
        if (offset == Integer.MIN_VALUE) {
            final Rect oldBounds = thumb.getBounds();
            top = oldBounds.top;
            bottom = oldBounds.bottom;
        } else {
            top = offset;
            bottom = offset + thumbHeight;
        }

        final int left = (ViewUtils.isLayoutRtl(this) && mMirrorForRtl) ? available - thumbPos : thumbPos;
        final int right = left + thumbWidth;

        final Drawable background = getBackground();
        if (background != null) {
            final int offsetX = getPaddingLeft() - mThumbOffset;
            final int offsetY = getPaddingTop();
            DrawableCompat.setHotspotBounds(background, left + offsetX, top + offsetY, right + offsetX, bottom + offsetY);
        }

        thumb.setBounds(left, top, right, bottom);
        updateGestureExclusionRects();
        mThumbPosX = (left + getPaddingLeft()) - (getPaddingLeft() - (thumbWidth / 2));
        updateSplitProgress();
    }

    @SuppressLint("RestrictedApi")
    private void setThumbPosInVertical(int h, Drawable thumb, float scale, int offset) {
        int available = h - getPaddingTop() - getPaddingBottom();
        final int thumbWidth = thumb.getIntrinsicHeight();
        final int thumbHeight = thumb.getIntrinsicHeight();
        available -= thumbHeight;

        available += mThumbOffset * 2;

        final int thumbPos = (int) (scale * available + 0.5f);

        final int left, right;
        if (offset == Integer.MIN_VALUE) {
            final Rect oldBounds = thumb.getBounds();
            left = oldBounds.left;
            right = oldBounds.right;
        } else {
            left = offset;
            right = offset + thumbWidth;
        }

        final int top = available - thumbPos;
        final int bottom = top + thumbHeight;

        final Drawable background = getBackground();
        if (background != null) {
            final int offsetX = getPaddingLeft();
            final int offsetY = getPaddingTop() - mThumbOffset;
            DrawableCompat.setHotspotBounds(background, left + offsetX, top + offsetY, right + offsetX, bottom + offsetY);
        }

        thumb.setBounds(left, top, right, bottom);
        mThumbPosX = top + (thumbWidth / 2) + getPaddingLeft();
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void setSystemGestureExclusionRects(@NonNull List<Rect> rects) {
        Preconditions.checkNotNull(rects, "rects must not be null");
        mUserGestureExclusionRects = rects;
        updateGestureExclusionRects();
    }

    private void updateGestureExclusionRects() {
        if (Build.VERSION.SDK_INT >= 29) {
            final Drawable thumb = mThumb;
            if (thumb == null) {
                super.setSystemGestureExclusionRects(mUserGestureExclusionRects);
                return;
            }
            mGestureExclusionRects.clear();
            thumb.copyBounds(mThumbRect);
            mGestureExclusionRects.add(mThumbRect);
            mGestureExclusionRects.addAll(mUserGestureExclusionRects);
            super.setSystemGestureExclusionRects(mGestureExclusionRects);
        }
    }

    @Override
    public void onResolveDrawables(int layoutDirection) {
        super.onResolveDrawables(layoutDirection);

        if (mThumb != null) {
            DrawableCompat.setLayoutDirection(mThumb, layoutDirection);
        }
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (supportIsHoveringUIEnabled()) {
            int hoverPopupType = getHoverPopupType();
            if (isHoverPopupTypeUserCustom(hoverPopupType) && mPreviousHoverPopupType != hoverPopupType) {
                mPreviousHoverPopupType = hoverPopupType;
                setHoverPopupGravity(HOVER_POPUP_WINDOW_GRAVITY_TOP_ABOVE | HOVER_POPUP_WINDOW_GRAVITY_CENTER_HORIZONTAL_ON_POINT);
                setHoverPopupOffset(0, getMeasuredHeight() / 2);
                setHoverPopupDetectTime();
            }
        }
        if (mCurrentMode == MODE_SPLIT) {
            mSplitProgress.draw(canvas);
            mDivider.draw(canvas);
        }
        if (!mIsTouchDisabled) {
            drawThumb(canvas);
        }
    }

    @SuppressLint("RestrictedApi")
    @Override
    protected void drawTrack(Canvas canvas) {
        final Drawable thumbDrawable = mThumb;
        if (thumbDrawable != null && mSplitTrack) {
            final Rect insets = DrawableUtils.getOpticalBounds(thumbDrawable);
            final Rect tempRect = mTempRect;
            thumbDrawable.copyBounds(tempRect);
            tempRect.offset(getPaddingLeft() - mThumbOffset, getPaddingTop());
            tempRect.left += insets.left;
            tempRect.right -= insets.right;

            final int saveCount = canvas.save();
            canvas.clipRect(tempRect, Region.Op.DIFFERENCE);
            super.drawTrack(canvas);
            drawTickMarks(canvas);
            canvas.restoreToCount(saveCount);
        } else {
            super.drawTrack(canvas);
            drawTickMarks(canvas);
        }

        if (!checkInvalidatedDualColorMode()) {
            canvas.save();
            if (mMirrorForRtl && ViewUtils.isLayoutRtl(this)) {
                canvas.translate(getWidth() - getPaddingRight(), getPaddingTop());
                canvas.scale(-1.0f, 1.0f);
            } else {
                canvas.translate(getPaddingLeft(), getPaddingTop());
            }

            final Rect bounds = mOverlapBackground.getBounds();
            mOverlapBackground.copyBounds(mTempRect);

            int i;
            int i2;
            if (mIsSeamless) {
                i2 = Math.max(super.getProgress(), (int) (((float) mOverlapPoint) * SCALE_FACTOR));
                i = super.getMax();
            } else {
                i2 = Math.max(getProgress(), this.mOverlapPoint);
                i = getMax();
            }
            if (mCurrentMode == MODE_VERTICAL || mCurrentMode == MODE_EXPAND_VERTICAL) {
                mTempRect.bottom = (int) (((float) bounds.bottom) - (((float) bounds.height()) * (((float) i2) / ((float) i))));
            } else {
                mTempRect.left = (int) (((float) bounds.left) + (((float) bounds.width()) * (((float) i2) / ((float) i))));
            }
            canvas.clipRect(mTempRect);
            if (mDefaultNormalProgressColor.getDefaultColor() != mOverlapNormalProgressColor.getDefaultColor()) {
                mOverlapBackground.draw(canvas);
            }
            canvas.restore();
        }
    }

    protected void drawTickMarks(Canvas canvas) {
        if (mTickMark != null) {
            final int count = getMax() - getMin();
            if (count > 1) {
                final int w = mTickMark.getIntrinsicWidth();
                final int h = mTickMark.getIntrinsicHeight();
                final int halfW = w >= 0 ? w / 2 : 1;
                final int halfH = h >= 0 ? h / 2 : 1;
                mTickMark.setBounds(-halfW, -halfH, halfW, halfH);

                final float spacing = (getWidth() - getPaddingLeft() - getPaddingRight()) / (float) count;
                final int saveCount = canvas.save();
                canvas.translate(getPaddingLeft(), getHeight() / 2);
                for (int i = 0; i <= count; i++) {
                    mTickMark.draw(canvas);
                    canvas.translate(spacing, 0);
                }
                canvas.restoreToCount(saveCount);
            }
        }
    }

    void drawThumb(Canvas canvas) {
        if (mThumb != null) {
            final int saveCount = canvas.save();
            if (mCurrentMode == MODE_VERTICAL || mCurrentMode == MODE_EXPAND_VERTICAL) {
                canvas.translate(getPaddingLeft(), getPaddingTop() - mThumbOffset);
            } else {
                canvas.translate(getPaddingLeft() - mThumbOffset, getPaddingTop());
            }
            mThumb.draw(canvas);
            canvas.restoreToCount(saveCount);
        }
    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Drawable d = getCurrentDrawable();

        int thumbHeight = mThumb == null ? 0 : mThumb.getIntrinsicHeight();
        int dw = 0;
        int dh = 0;
        if (d != null) {
            if (mCurrentMode != MODE_VERTICAL && mCurrentMode != MODE_EXPAND_VERTICAL) {
                dw = Math.max(mMinWidth, Math.min(mMaxWidth, d.getIntrinsicWidth()));
                dh = Math.max(mMinHeight, Math.min(mMaxHeight, d.getIntrinsicHeight()));
                dh = Math.max(thumbHeight, dh);
            } else {
                dh = Math.max(mMinHeight, Math.min(mMaxHeight, d.getIntrinsicWidth()));
                dw = Math.max(mMinWidth, Math.min(mMaxWidth, d.getIntrinsicHeight()));
                dw = Math.max(thumbHeight, dw);
            }
        }
        dw += getPaddingLeft() + getPaddingRight();
        dh += getPaddingTop() + getPaddingBottom();

        setMeasuredDimension(resolveSizeAndState(dw, widthMeasureSpec, 0), resolveSizeAndState(dh, heightMeasureSpec, 0));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!mIsUserSeekable || mIsTouchDisabled || !isEnabled()) {
            return false;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mIsDraggingForSliding = false;
                if (mCurrentMode == MODE_EXPAND || mCurrentMode == MODE_EXPAND_VERTICAL || supportIsInScrollingContainer()) {
                    mTouchDownX = event.getX();
                    mTouchDownY = event.getY();
                } else {
                    startDrag(event);
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mIsDraggingForSliding) {
                    mIsDraggingForSliding = false;
                }
                if (mIsDragging) {
                    trackTouchEvent(event);
                    onStopTrackingTouch();
                    setPressed(false);
                } else {
                    onStartTrackingTouch();
                    trackTouchEvent(event);
                    onStopTrackingTouch();
                }
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                mIsDraggingForSliding = true;
                if (mIsDragging) {
                    trackTouchEvent(event);
                } else {
                    final float x = event.getX();
                    final float y = event.getY();
                    if (mCurrentMode != MODE_VERTICAL && mCurrentMode != MODE_EXPAND_VERTICAL && Math.abs(x - mTouchDownX) > (float) mScaledTouchSlop || (mCurrentMode == MODE_VERTICAL || mCurrentMode == MODE_EXPAND_VERTICAL) && Math.abs(y - mTouchDownY) > (float) mScaledTouchSlop) {
                        startDrag(event);
                    }
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                mIsDraggingForSliding = false;
                if (mIsDragging) {
                    onStopTrackingTouch();
                    setPressed(false);
                }
                invalidate();
                break;
        }

        return true;
    }

    private void startDrag(MotionEvent event) {
        setPressed(true);

        if (mThumb != null) {
            invalidate(mThumb.getBounds());
        }

        onStartTrackingTouch();
        trackTouchEvent(event);
        attemptClaimDrag();
    }

    private void setHotspot(float x, float y) {
        final Drawable bg = getBackground();
        if (bg != null) {
            DrawableCompat.setHotspot(bg, x, y);
        }
    }

    // kang
    @SuppressLint("RestrictedApi")
    private void trackTouchEvent(MotionEvent var1) {
        if (this.mCurrentMode != 3 && this.mCurrentMode != 6) {
            int var2;
            int var3;
            int var4;
            float var6;
            float var7;
            label55:
            {
                label63:
                {
                    var2 = Math.round(var1.getX());
                    var3 = Math.round(var1.getY());
                    var4 = this.getWidth();
                    int var5 = var4 - this.getPaddingLeft() - this.getPaddingRight();
                    if (ViewUtils.isLayoutRtl(this) && this.mMirrorForRtl) {
                        if (var2 > var4 - this.getPaddingRight()) {
                            break label63;
                        }

                        if (var2 >= this.getPaddingLeft()) {
                            var6 = (float) (var5 - var2 + this.getPaddingLeft()) / (float) var5;
                            var7 = this.mTouchProgressOffset;
                            break label55;
                        }
                    } else {
                        if (var2 < this.getPaddingLeft()) {
                            break label63;
                        }

                        if (var2 <= var4 - this.getPaddingRight()) {
                            var6 = (float) (var2 - this.getPaddingLeft()) / (float) var5;
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
            float var11;
            if (this.mIsSeamless) {
                var8 = (float) (super.getMax() - super.getMin());
                var9 = 1.0F / var8;
                var10 = var6;
                if (var6 > 0.0F) {
                    var10 = var6;
                    if (var6 < 1.0F) {
                        var11 = var6 % var9;
                        var10 = var6;
                        if (var11 > var9 / 2.0F) {
                            var10 = var6 + (var9 - var11);
                        }
                    }
                }

                var6 = var10 * var8;
                var4 = super.getMin();
            } else {
                var8 = (float) (this.getMax() - this.getMin());
                var11 = 1.0F / var8;
                var10 = var6;
                if (var6 > 0.0F) {
                    var10 = var6;
                    if (var6 < 1.0F) {
                        var9 = var6 % var11;
                        var10 = var6;
                        if (var9 > var11 / 2.0F) {
                            var10 = var6 + (var11 - var9);
                        }
                    }
                }

                var6 = var10 * var8;
                var4 = this.getMin();
            }

            var10 = (float) var4;
            this.setHotspot((float) var2, (float) var3);
            this.setProgressInternal(Math.round(var7 + var6 + var10), true, false);
        } else {
            this.trackTouchEventInVertical(var1);
        }
    }

    private void trackTouchEventInVertical(MotionEvent var1) {
        int var2 = this.getHeight();
        int var3 = this.getPaddingTop();
        int var4 = this.getPaddingBottom();
        int var5 = Math.round(var1.getX());
        int var6 = var2 - Math.round(var1.getY());
        float var7;
        float var8;
        if (var6 < this.getPaddingBottom()) {
            var7 = 0.0F;
            var8 = var7;
        } else if (var6 > var2 - this.getPaddingTop()) {
            var8 = 0.0F;
            var7 = 1.0F;
        } else {
            var7 = (float) (var6 - this.getPaddingBottom()) / (float) (var2 - var3 - var4);
            var8 = this.mTouchProgressOffset;
        }

        float var9;
        float var10;
        float var11;
        float var12;
        if (this.mIsSeamless) {
            var9 = (float) (super.getMax() - super.getMin());
            var10 = 1.0F / var9;
            var11 = var7;
            if (var7 > 0.0F) {
                var11 = var7;
                if (var7 < 1.0F) {
                    var12 = var7 % var10;
                    var11 = var7;
                    if (var12 > var10 / 2.0F) {
                        var11 = var7 + (var10 - var12);
                    }
                }
            }

            var7 = var11 * var9;
            var3 = super.getMin();
        } else {
            var9 = (float) (this.getMax() - this.getMin());
            var10 = 1.0F / var9;
            var11 = var7;
            if (var7 > 0.0F) {
                var11 = var7;
                if (var7 < 1.0F) {
                    var12 = var7 % var10;
                    var11 = var7;
                    if (var12 > var10 / 2.0F) {
                        var11 = var7 + (var10 - var12);
                    }
                }
            }

            var7 = var11 * var9;
            var3 = this.getMin();
        }

        var11 = (float) var3;
        this.setHotspot((float) var5, (float) var6);
        this.setProgressInternal(Math.round(var8 + var7 + var11), true, false);
    }
    // kang

    private void attemptClaimDrag() {
        if (getParent() != null) {
            getParent().requestDisallowInterceptTouchEvent(true);
        }
    }

    protected void onStartTrackingHover(int level, int x, int y) {
    }

    protected void onStartTrackingTouch() {
        mIsDragging = true;
        if (mValueAnimator != null) {
            mValueAnimator.cancel();
        }
    }

    protected void onStopTrackingHover() {
    }

    protected void onStopTrackingTouch() {
        mIsDragging = false;
        if (mIsSeamless && isPressed()) {
            mValueAnimator = ValueAnimator.ofInt(super.getProgress(), (int) (((float) Math.round(((float) super.getProgress()) / SCALE_FACTOR)) * SCALE_FACTOR));
            mValueAnimator.setDuration(300L);
            mValueAnimator.setInterpolator(SeslAnimationUtils.SINE_IN_OUT_90);
            mValueAnimator.start();
            mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    callSuperSetProgress((Integer) animation.getAnimatedValue());
                }
            });
        } else if (mIsSeamless) {
            setProgress(Math.round(((float) super.getProgress()) / SCALE_FACTOR));
        }
    }

    private void callSuperSetProgress(int progress) {
        super.setProgress(progress);
    }

    void onKeyChange() {
    }

    //kang
    @SuppressLint("RestrictedApi")
    @Override
    public boolean onKeyDown(int var1, KeyEvent var2) {
        if (this.isEnabled()) {
            int var3 = this.mKeyProgressIncrement;
            int var4;
            if (this.mCurrentMode != 3 && this.mCurrentMode != 6) {
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
    //kang

    @Override
    public CharSequence getAccessibilityClassName() {
        Log.d(TAG, "Stack:", new Throwable("stack dump"));
        return AbsSeekBar.class.getName();
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);

        if (isEnabled()) {
            final int progress = getProgress();
            if (progress > getMin()) {
                info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_BACKWARD);
            }
            if (progress < getMax()) {
                info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD);
            }
        }
    }

    @Override
    public boolean performAccessibilityAction(int action, Bundle arguments) {
        if (super.performAccessibilityAction(action, arguments)) {
            return true;
        }

        if (!isEnabled()) {
            return false;
        }

        switch (action) {
            case android.R.id.accessibilityActionSetProgress: {
                if (!canUserSetProgress()) {
                    return false;
                }
                if (arguments == null || !arguments.containsKey(AccessibilityNodeInfoCompat.ACTION_ARGUMENT_PROGRESS_VALUE)) {
                    return false;
                }
                float value = arguments.getFloat(AccessibilityNodeInfoCompat.ACTION_ARGUMENT_PROGRESS_VALUE);
                return setProgressInternal(mIsSeamless ? Math.round(value * SCALE_FACTOR) : (int) value, true, true);
            }
            case AccessibilityNodeInfo.ACTION_SCROLL_FORWARD:
            case AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD: {
                if (!canUserSetProgress()) {
                    return false;
                }
                int range = getMax() - getMin();
                int increment = Math.max(1, Math.round((float) range / 20));
                if (action == AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD) {
                    increment = -increment;
                }

                if (setProgressInternal(mIsSeamless ? Math.round(((float) (getProgress() + increment)) * SCALE_FACTOR) : getProgress() + increment, true, true)) {
                    onKeyChange();
                    return true;
                }
                return false;
            }
        }
        return false;
    }

    protected boolean canUserSetProgress() {
        return !isIndeterminate() && isEnabled();
    }

    @Override
    public void onRtlPropertiesChanged(int layoutDirection) {
        super.onRtlPropertiesChanged(layoutDirection);

        final Drawable thumb = mThumb;
        if (thumb != null) {
            setThumbPos(getWidth(), thumb, getScale(), Integer.MIN_VALUE);
            invalidate();
        }
    }

    @Override
    protected void onProgressRefresh(float scale, boolean fromUser, int progress) {
        final int level = (int) (scale * 10000.0f);
        if (!(mUseMuteAnimation && !mIsFirstSetProgress && !mIsDraggingForSliding) || mCurrentProgressLevel == 0 || level != 0) {
            cancelMuteAnimation();
            mIsFirstSetProgress = false;
            mCurrentProgressLevel = level;

            super.onProgressRefresh(scale, fromUser, progress);

            if (mThumb != null) {
                setThumbPos(getWidth(), mThumb, scale, Integer.MIN_VALUE);
                invalidate();
            }
        } else {
            startMuteAnimation();
        }
    }

    public void setThumbTintColor(int color) {
        ColorStateList colorStateList = colorToColorStateList(color);
        if (!mDefaultActivatedThumbColor.equals(colorStateList)) {
            mDefaultActivatedThumbColor = colorStateList;
        }
    }

    @SuppressLint("RestrictedApi")
    private void updateSplitProgress() {
        if (mCurrentMode == MODE_SPLIT) {
            if (mSplitProgress != null) {
                Rect bounds = getCurrentDrawable().getBounds();
                if (mMirrorForRtl && ViewUtils.isLayoutRtl(this)) {
                    mSplitProgress.setBounds(mThumbPosX, bounds.top, getWidth() - getPaddingRight(), bounds.bottom);
                } else {
                    mSplitProgress.setBounds(getPaddingLeft(), bounds.top, mThumbPosX, bounds.bottom);
                }
            }

            // kang
            if (mDivider != null) {
                float f = ((float) getWidth()) / 2.0f;
                float f2 = ((float) getHeight()) / 2.0f;
                mDivider.setBounds((int) (f - ((this.mDensity * 4.0f) / 2.0f)), (int) (f2 - ((this.mDensity * 22.0f) / 2.0f)), (int) (f + ((this.mDensity * 4.0f) / 2.0f)), (int) (f2 + ((this.mDensity * 22.0f) / 2.0f)));
            }
            // kang
        }
    }

    @Override
    protected boolean setProgressInternal(int progress, boolean fromUser, boolean animate) {
        boolean progressInternal = super.setProgressInternal(progress, fromUser, animate);
        updateWarningMode(progress);
        updateDualColorMode();
        return progressInternal;
    }

    // kang
    private void trackHoverEvent(int x) {
        float f;
        int width = getWidth();
        int paddingLeft = (width - getPaddingLeft()) - getPaddingRight();
        float f2 = 0.0f;
        if (x < getPaddingLeft()) {
            f = 0.0f;
        } else if (x > width - getPaddingRight()) {
            f = 1.0f;
        } else {
            f2 = this.mTouchProgressOffset;
            f = ((float) (x - getPaddingLeft())) / ((float) paddingLeft);
        }
        this.mHoveringLevel = (int) (f2 + (f * ((float) getMax())));
    }
    // kang

    protected void onHoverChanged(int level, int x, int y) {
    }

    @Override
    public boolean onHoverEvent(MotionEvent event) {
        if (supportIsHoveringUIEnabled()) {
            final int action = event.getAction();
            final int x = (int) event.getX();
            final int y = (int) event.getY();

            switch (action) {
                case MotionEvent.ACTION_HOVER_MOVE:
                    trackHoverEvent(x);
                    onHoverChanged(mHoveringLevel, x, y);
                    if (isHoverPopupTypeUserCustom(getHoverPopupType())) {
                        setHoveringPoint((int) event.getRawX(), (int) event.getRawY());
                        updateHoverPopup();
                    }
                    break;
                case MotionEvent.ACTION_HOVER_ENTER:
                    trackHoverEvent(x);
                    onStartTrackingHover(mHoveringLevel, x, y);
                    break;
                case MotionEvent.ACTION_HOVER_EXIT:
                    onStopTrackingHover();
                    break;
            }
        }

        return super.onHoverEvent(event);
    }

    @Override
    public void setProgressDrawable(Drawable d) {
        super.setProgressDrawable(d);
    }

    public Rect getThumbBounds() {
        return mThumb != null ? mThumb.getBounds() : null;
    }

    public int getThumbHeight() {
        return mThumb.getIntrinsicHeight();
    }

    @Override
    public void setMode(int mode) {
        if (mCurrentMode != mode || !mIsSetModeCalled) {
            super.setMode(mode);

            switch (mode) {
                case MODE_STANDARD:
                    setProgressTintList(mDefaultActivatedProgressColor);
                    setThumbTintList(mDefaultActivatedThumbColor);
                    break;
                case MODE_WARNING:
                    updateWarningMode(getProgress());
                    break;
                case MODE_VERTICAL:
                    setThumb(getContext().getResources().getDrawable(mIsOneUI4 ? R.drawable.sesl4_scrubber_control_anim : R.drawable.sesl_scrubber_control_anim, getContext().getTheme()));
                    break;
                case MODE_SPLIT:
                    mSplitProgress = getContext().getResources().getDrawable(R.drawable.sesl_split_seekbar_primary_progress);
                    mDivider = getContext().getResources().getDrawable(R.drawable.sesl_split_seekbar_vertical_bar);
                    updateSplitProgress();
                    break;
                case MODE_EXPAND:
                    initializeExpandMode();
                    break;
                case MODE_EXPAND_VERTICAL:
                    initializeExpandVerticalMode();
                    break;
            }

            invalidate();
            mIsSetModeCalled = true;
        } else {
            Log.w(TAG, "Seekbar mode is already set. Do not call this method redundant");
        }
    }

    @SuppressLint("RestrictedApi")
    private void initializeExpandMode() {
        SliderDrawable background = new SliderDrawable(this, (float) mTrackMinWidth, (float) mTrackMaxWidth, mDefaultNormalProgressColor);
        SliderDrawable secondaryProgress = new SliderDrawable(this, (float) mTrackMinWidth, (float) mTrackMaxWidth, mDefaultSecondaryProgressColor);
        SliderDrawable progress = new SliderDrawable(this, (float) mTrackMinWidth, (float) mTrackMaxWidth, mDefaultActivatedProgressColor);

        Drawable thumb = new DrawableWrapper(new ThumbDrawable(mThumbRadius, mDefaultActivatedThumbColor, false));

        LayerDrawable layerDrawable = new LayerDrawable(new Drawable[]{background,
                new ClipDrawable(secondaryProgress, Gravity.LEFT | Gravity.CENTER_VERTICAL, ClipDrawable.HORIZONTAL),
                new ClipDrawable(progress, Gravity.LEFT | Gravity.CENTER_VERTICAL, ClipDrawable.HORIZONTAL)});
        layerDrawable.setPaddingMode(LayerDrawable.PADDING_MODE_STACK);
        layerDrawable.setId(0, android.R.id.background);
        layerDrawable.setId(1, android.R.id.secondaryProgress);
        layerDrawable.setId(2, android.R.id.progress);

        setProgressDrawable(layerDrawable);
        setThumb(thumb);
        setBackgroundResource(R.drawable.sesl_seekbar_background_borderless_expand);

        if (getMaxHeight() > mTrackMaxWidth) {
            setMaxHeight(mTrackMaxWidth);
        }
    }

    @SuppressLint("RestrictedApi")
    private void initializeExpandVerticalMode() {
        SliderDrawable background = new SliderDrawable((float) mTrackMinWidth, (float) mTrackMaxWidth, mDefaultNormalProgressColor, true);
        SliderDrawable secondaryProgress = new SliderDrawable((float) mTrackMinWidth, (float) mTrackMaxWidth, mDefaultSecondaryProgressColor, true);
        SliderDrawable progress = new SliderDrawable((float) mTrackMinWidth, (float) mTrackMaxWidth, mDefaultActivatedProgressColor, true);

        Drawable thumb = new DrawableWrapper(new ThumbDrawable(mThumbRadius, mDefaultActivatedThumbColor, true));

        LayerDrawable layerDrawable = new LayerDrawable(new Drawable[]{background,
                new ClipDrawable(secondaryProgress, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, ClipDrawable.VERTICAL),
                new ClipDrawable(progress, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, ClipDrawable.VERTICAL)});
        layerDrawable.setPaddingMode(LayerDrawable.PADDING_MODE_STACK);
        layerDrawable.setId(0, android.R.id.background);
        layerDrawable.setId(1, android.R.id.secondaryProgress);
        layerDrawable.setId(2, android.R.id.progress);

        setProgressDrawable(layerDrawable);
        setThumb(thumb);
        setBackgroundResource(R.drawable.sesl_seekbar_background_borderless_expand);

        if (getMaxWidth() > mTrackMaxWidth) {
            setMaxWidth(mTrackMaxWidth);
        }
    }

    @Deprecated
    public void setOverlapBackgroundForDualColor(int color) {
        ColorStateList colorStateList = colorToColorStateList(color);
        if (!mOverlapNormalProgressColor.equals(colorStateList)) {
            mOverlapNormalProgressColor = colorStateList;
        }
        mOverlapActivatedProgressColor = mOverlapNormalProgressColor;
        mLargeFont = true;
    }

    public void setOverlapPointForDualColor(int point) {
        if (point < getMax()) {
            mSetDualColorMode = true;
            mOverlapPoint = point;
            if (point == -1) {
                setProgressTintList(mDefaultActivatedProgressColor);
                setThumbTintList(mDefaultActivatedThumbColor);
            } else {
                if (mOverlapBackground == null) {
                    initDualOverlapDrawable();
                }
                updateDualColorMode();
            }
            invalidate();
        }
    }

    private void updateDualColorMode() {
        if (!checkInvalidatedDualColorMode()) {
            DrawableCompat.setTintList(mOverlapBackground, mOverlapNormalProgressColor);
            if (!mLargeFont) {
                if ((!mIsSeamless || ((float) super.getProgress()) <= ((float) mOverlapPoint) * SCALE_FACTOR) && getProgress() <= mOverlapPoint) {
                    setProgressTintList(mDefaultActivatedProgressColor);
                    setThumbTintList(mDefaultActivatedThumbColor);
                } else {
                    setProgressOverlapTintList(mOverlapActivatedProgressColor);
                    setThumbOverlapTintList(mOverlapActivatedProgressColor);
                }
            }
            updateBoundsForDualColor();
        }
    }

    private void updateBoundsForDualColor() {
        if (getCurrentDrawable() != null && !checkInvalidatedDualColorMode()) {
            mOverlapBackground.setBounds(getCurrentDrawable().getBounds());
        }
    }

    public void setDualModeOverlapColor(int normalColor, int activatedColor) {
        ColorStateList normalColorStateList = colorToColorStateList(normalColor);
        ColorStateList activatedColorStateList = colorToColorStateList(activatedColor);
        if (!mOverlapNormalProgressColor.equals(normalColorStateList)) {
            mOverlapNormalProgressColor = normalColorStateList;
        }
        if (!mOverlapActivatedProgressColor.equals(activatedColorStateList)) {
            mOverlapActivatedProgressColor = activatedColorStateList;
        }
        updateDualColorMode();
        invalidate();
    }

    private boolean checkInvalidatedDualColorMode() {
        return mOverlapPoint == -1 || mOverlapBackground == null;
    }

    private void initDualOverlapDrawable() {
        if (mCurrentMode == MODE_EXPAND) {
            mOverlapBackground = new SliderDrawable(this, (float) mTrackMinWidth, (float) mTrackMaxWidth, mOverlapNormalProgressColor);
        } else if (mCurrentMode == MODE_EXPAND_VERTICAL) {
            mOverlapBackground = new SliderDrawable((float) mTrackMinWidth, (float) mTrackMaxWidth, mOverlapNormalProgressColor, true);
        } else if (getProgressDrawable() != null && getProgressDrawable().getConstantState() != null) {
            mOverlapBackground = getProgressDrawable().getConstantState().newDrawable().mutate();
        }
    }

    @Override
    protected void updateDrawableBounds(int w, int h) {
        super.updateDrawableBounds(w, h);
        updateThumbAndTrackPos(w, h);
        updateBoundsForDualColor();
    }

    private ColorStateList colorToColorStateList(int color) {
        return new ColorStateList(new int[][]{new int[0]}, new int[]{color});
    }

    private void updateWarningMode(int progress) {
        if (mCurrentMode == MODE_WARNING) {
            if (progress == getMax()) {
                setProgressOverlapTintList(mOverlapActivatedProgressColor);
                setThumbOverlapTintList(mOverlapActivatedProgressColor);
            } else {
                setProgressTintList(mDefaultActivatedProgressColor);
                setThumbTintList(mDefaultActivatedThumbColor);
            }
        }
    }

    // kang
    private void initMuteAnimation() {
        ValueAnimator valueAnimator;
        this.mMuteAnimationSet = new AnimatorSet();
        ArrayList arrayList = new ArrayList();
        int i = MUTE_VIB_DISTANCE_LVL;
        for (int i2 = 0; i2 < 8; i2++) {
            boolean z = i2 % 2 == 0;
            if (z) {
                valueAnimator = ValueAnimator.ofInt(0, i);
            } else {
                valueAnimator = ValueAnimator.ofInt(i, 0);
            }
            valueAnimator.setDuration((long) 62);
            valueAnimator.setInterpolator(new LinearInterpolator());
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: androidx.appcompat.widget.SeslAbsSeekBar.2
                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public void onAnimationUpdate(ValueAnimator valueAnimator2) {
                    SeslAbsSeekBar.this.mCurrentProgressLevel = ((Integer) valueAnimator2.getAnimatedValue()).intValue();
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
    // kang

    private void cancelMuteAnimation() {
        if (mMuteAnimationSet != null && mMuteAnimationSet.isRunning()) {
            mMuteAnimationSet.cancel();
        }
    }

    private void startMuteAnimation() {
        cancelMuteAnimation();
        if (mMuteAnimationSet != null) {
            mMuteAnimationSet.start();
        }
    }

    @Override
    protected void onSlidingRefresh(int level) {
        super.onSlidingRefresh(level);

        final float scale = ((float) level) / 10000.0f;
        if (mThumb != null) {
            setThumbPos(getWidth(), mThumb, scale, Integer.MIN_VALUE);
            invalidate();
        }
    }

    private void setThumbOverlapTintList(ColorStateList tint) {
        mThumbTintList = tint;
        mHasThumbTint = true;
        applyThumbTint();
    }

    @Override
    public void setProgressTintList(@Nullable ColorStateList tint) {
        super.setProgressTintList(tint);
        mDefaultActivatedProgressColor = tint;
    }

    private void setProgressOverlapTintList(ColorStateList list) {
        super.setProgressTintList(list);
    }

    private boolean supportIsHoveringUIEnabled() {
        return IS_BASE_SDK_VERSION && SeslViewReflector.isHoveringUIEnabled(this);
    }

    private void setHoverPopupGravity(int gravity) {
        if (IS_BASE_SDK_VERSION) {
            SeslHoverPopupWindowReflector.setGravity(SeslViewReflector.semGetHoverPopup(this, true), gravity);
        }
    }

    private void setHoverPopupOffset(int x, int y) {
        if (IS_BASE_SDK_VERSION) {
            SeslHoverPopupWindowReflector.setOffset(SeslViewReflector.semGetHoverPopup(this, true), x, y);
        }
    }

    private void setHoverPopupDetectTime() {
        if (IS_BASE_SDK_VERSION) {
            SeslHoverPopupWindowReflector.setHoverDetectTime(SeslViewReflector.semGetHoverPopup(this, true), HOVER_DETECT_TIME);
        }
    }

    private void setHoveringPoint(int x, int y) {
        if (IS_BASE_SDK_VERSION) {
            SeslHoverPopupWindowReflector.setHoveringPoint(this, x, y);
        }
    }

    public void updateHoverPopup() {
        if (IS_BASE_SDK_VERSION) {
            SeslHoverPopupWindowReflector.update(SeslViewReflector.semGetHoverPopup(this, true));
        }
    }

    private boolean isHoverPopupTypeUserCustom(int type) {
        return IS_BASE_SDK_VERSION && type == SeslHoverPopupWindowReflector.getField_TYPE_USER_CUSTOM();
    }

    private int getHoverPopupType() {
        if (IS_BASE_SDK_VERSION) {
            return SeslViewReflector.semGetHoverPopupType(this);
        }
        return 0;
    }

    private boolean supportIsInScrollingContainer() {
        return SeslViewReflector.isInScrollingContainer(this);
    }

    @Override
    public synchronized int getProgress() {
        return mIsSeamless ? Math.round(((float) super.getProgress()) / SCALE_FACTOR) : super.getProgress();
    }

    @Override
    public synchronized int getMin() {
        return mIsSeamless ? Math.round(((float) super.getMin()) / SCALE_FACTOR) : super.getMin();
    }

    @Override
    public synchronized int getMax() {
        return mIsSeamless ? Math.round(((float) super.getMax()) / SCALE_FACTOR) : super.getMax();
    }

    @Override
    public synchronized void setProgress(int progress) {
        if (mIsSeamless) {
            progress = Math.round(((float) progress) * SCALE_FACTOR);
        }
        super.setProgress(progress);
    }

    @Override
    public synchronized void setSecondaryProgress(int secondaryProgress) {
        if (mIsSeamless) {
            secondaryProgress = Math.round(((float) secondaryProgress) * SCALE_FACTOR);
        }
        super.setSecondaryProgress(secondaryProgress);
    }

    public void setSeamless(boolean seamless) {
        if (mIsSeamless != seamless) {
            mIsSeamless = seamless;

            if (mIsSeamless) {
                super.setMax(Math.round(((float) super.getMax()) * SCALE_FACTOR));
                super.setMin(Math.round(((float) super.getMin()) * SCALE_FACTOR));
                super.setProgress(Math.round(((float) super.getProgress()) * SCALE_FACTOR));
                super.setSecondaryProgress(Math.round(((float) super.getSecondaryProgress()) * SCALE_FACTOR));
            } else {
                super.setProgress(Math.round(((float) super.getProgress()) / SCALE_FACTOR));
                super.setSecondaryProgress(Math.round(((float) super.getSecondaryProgress()) / SCALE_FACTOR));
                super.setMax(Math.round(((float) super.getMax()) / SCALE_FACTOR));
                super.setMin(Math.round(((float) super.getMin()) / SCALE_FACTOR));
            }
        }
    }


    private class SliderDrawable extends Drawable {
        private final int ANIMATION_DURATION = 250;
        int mAlpha = 255;
        int mColor;
        ColorStateList mColorStateList;
        private boolean mIsStateChanged = false;
        private boolean mIsVertical;
        //private final Paint mPaint;
        ValueAnimator mPressedAnimator;
        private float mRadius;
        ValueAnimator mReleasedAnimator;
        private final float mSliderMaxWidth;
        private final float mSliderMinWidth;
        private final SliderState mState = new SliderState();
        private Drawable mDrawable;

        private int modulateAlpha(int paintAlpha, int alpha) {
            int scale = alpha + (alpha >>> 7);
            return (paintAlpha * scale) >>> 8;
        }

        @Override
        public boolean isStateful() {
            return true;
        }

        public SliderDrawable(SeslAbsSeekBar seekBar, float minWidth, float maxWidth, ColorStateList tint) {
            this(minWidth, maxWidth, tint, false);
        }

        public SliderDrawable(float minWidth, float maxWidth, ColorStateList tint, boolean isVertical) {
            //mPaint = new Paint();
            //mPaint.setStyle(Paint.Style.STROKE);
            //mPaint.setStrokeCap(Paint.Cap.ROUND);
            mColorStateList = tint;
            mColor = tint.getDefaultColor();
            //mPaint.setColor(mColor);
            //mPaint.setStrokeWidth(minWidth);
            mSliderMinWidth = minWidth;
            mSliderMaxWidth = maxWidth;
            mRadius = minWidth / 2.0f;
            mIsVertical = isVertical;

            this.mDrawable = getResources().getDrawable(R.drawable.sesl_progress_expand, null);
            this.mDrawable.setTint(mColor);

            initAnimator();
        }

        private void initAnimator() {
            mPressedAnimator = ValueAnimator.ofFloat(mSliderMinWidth, mSliderMaxWidth);
            mPressedAnimator.setDuration(ANIMATION_DURATION);
            mPressedAnimator.setInterpolator(SeslAnimationUtils.SINE_IN_OUT_80);
            mPressedAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    invalidateTrack((Float) animation.getAnimatedValue());
                }
            });

            mReleasedAnimator = ValueAnimator.ofFloat(mSliderMaxWidth, mSliderMinWidth);
            mReleasedAnimator.setDuration(ANIMATION_DURATION);
            mReleasedAnimator.setInterpolator(SeslAnimationUtils.SINE_IN_OUT_80);
            mReleasedAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    invalidateTrack((Float) animation.getAnimatedValue());
                }
            });
        }

        @Override
        public void draw(Canvas canvas) {
            //int prevAlpha = mPaint.getAlpha();
            int prevAlpha = mDrawable.getAlpha();
            //mPaint.setAlpha(modulateAlpha(prevAlpha, mAlpha));
            mDrawable.setAlpha(modulateAlpha(prevAlpha, mAlpha));

            canvas.save();
            if (!mIsVertical) {
                /*canvas.drawLine(
                        mRadius,
                        (float) (SeslAbsSeekBar.this.getHeight() - (SeslAbsSeekBar.this.getPaddingTop() + SeslAbsSeekBar.this.getPaddingBottom())) / 2.0f,
                        ((float) ((SeslAbsSeekBar.this.getWidth() - SeslAbsSeekBar.this.getPaddingLeft()) - SeslAbsSeekBar.this.getPaddingRight())) - mRadius,
                        (float) (SeslAbsSeekBar.this.getHeight() - (SeslAbsSeekBar.this.getPaddingTop() + SeslAbsSeekBar.this.getPaddingBottom())) / 2.0f,
                        mPaint);*/
                int center = (SeslAbsSeekBar.this.getHeight() - (SeslAbsSeekBar.this.getPaddingTop() + SeslAbsSeekBar.this.getPaddingBottom())) / 2;
                mDrawable.setBounds(
                        0,
                        (int) (center - mRadius),
                        (int) (float) (SeslAbsSeekBar.this.getWidth() - SeslAbsSeekBar.this.getPaddingLeft() - SeslAbsSeekBar.this.getPaddingRight()),
                        (int) (center + mRadius));
                mDrawable.draw(canvas);
            } else {
                /*canvas.drawLine(
                        (float) (SeslAbsSeekBar.this.getWidth() - (SeslAbsSeekBar.this.getPaddingLeft() + SeslAbsSeekBar.this.getPaddingRight())) / 2.0F,
                        ((float) ((SeslAbsSeekBar.this.getHeight() - SeslAbsSeekBar.this.getPaddingTop()) - SeslAbsSeekBar.this.getPaddingBottom())) - mRadius,
                        (float) (SeslAbsSeekBar.this.getWidth() - (SeslAbsSeekBar.this.getPaddingLeft() + SeslAbsSeekBar.this.getPaddingRight())) / 2.0F,
                        mRadius,
                        mPaint);*/
                int center = (SeslAbsSeekBar.this.getWidth() - (SeslAbsSeekBar.this.getPaddingLeft() + SeslAbsSeekBar.this.getPaddingRight())) / 2;
                mDrawable.setBounds(
                        (int) (center - mRadius),
                        0,
                        (int) (center + mRadius),
                        (int) (float) (SeslAbsSeekBar.this.getHeight() - SeslAbsSeekBar.this.getPaddingTop() - SeslAbsSeekBar.this.getPaddingBottom()));
                mDrawable.draw(canvas);
            }
            canvas.restore();
            //mPaint.setAlpha(prevAlpha);
            mDrawable.setAlpha(prevAlpha);
        }

        @Override
        public void setAlpha(int alpha) {
            mAlpha = alpha;
            invalidateSelf();
        }

        @Override
        public void setColorFilter(ColorFilter colorFilter) {
            //mPaint.setColorFilter(colorFilter);
            mDrawable.setColorFilter(colorFilter);
            invalidateSelf();
        }

        @Override
        public int getOpacity() {
            /*if (mPaint.getXfermode() != null) {
                return PixelFormat.TRANSLUCENT;
            }
            if (mPaint.getAlpha() == 0) {
                return PixelFormat.TRANSPARENT;
            }
            if (mPaint.getAlpha() == 255) {
                return PixelFormat.OPAQUE;
            } else {
                return PixelFormat.TRANSLUCENT;
            }*/
            return mDrawable.getOpacity();
        }

        @Override
        public void setTintList(ColorStateList tint) {
            super.setTintList(tint);
            if (tint != null) {
                mColorStateList = tint;
                mColor = tint.getDefaultColor();
                //mPaint.setColor(mColor);
                mDrawable.setTint(mColor);
                invalidateSelf();
            }
        }

        @Override
        protected boolean onStateChange(int[] state) {
            boolean onStateChange = super.onStateChange(state);

            int color = mColorStateList.getColorForState(state, mColor);
            if (mColor != color) {
                mColor = color;
                //mPaint.setColor(color);
                mDrawable.setTint(mColor);
                invalidateSelf();
            }

            boolean enabled = false;
            boolean pressed = false;
            for (int i : state) {
                if (i == android.R.attr.state_enabled) {
                    enabled = true;
                } else if (i == android.R.attr.state_pressed) {
                    pressed = true;
                }
            }

            startSliderAnimation(enabled && pressed);

            return onStateChange;
        }

        @Override
        public int getIntrinsicWidth() {
            return (int) mSliderMaxWidth;
        }

        @Override
        public int getIntrinsicHeight() {
            return (int) mSliderMaxWidth;
        }

        public void setStrokeWidth(float width) {
            //mPaint.setStrokeWidth(width);
            mRadius = width / 2.0f;
        }

        private void startSliderAnimation(boolean pressed) {
            if (mIsStateChanged != pressed) {
                if (pressed) {
                    startPressedAnimation();
                } else {
                    startReleasedAnimation();
                }
                mIsStateChanged = pressed;
            }
        }

        private void startPressedAnimation() {
            if (!mPressedAnimator.isRunning()) {
                if (mReleasedAnimator.isRunning()) {
                    mReleasedAnimator.cancel();
                }
                mPressedAnimator.setFloatValues(mSliderMinWidth, mSliderMaxWidth);
                mPressedAnimator.start();
            }
        }

        private void startReleasedAnimation() {
            if (!mReleasedAnimator.isRunning()) {
                if (mPressedAnimator.isRunning()) {
                    mPressedAnimator.cancel();
                }
                mReleasedAnimator.setFloatValues(mSliderMaxWidth, mSliderMinWidth);
                mReleasedAnimator.start();
            }
        }

        void invalidateTrack(float width) {
            setStrokeWidth(width);
            invalidateSelf();
        }

        @Override
        public Drawable.ConstantState getConstantState() {
            return mState;
        }

        private class SliderState extends Drawable.ConstantState {
            @Override
            public int getChangingConfigurations() {
                return 0;
            }

            @Override
            public Drawable newDrawable() {
                return SliderDrawable.this;
            }
        }
    }

    public class ThumbDrawable extends Drawable {
        int mColor;
        private ColorStateList mColorStateList;
        private boolean mIsVertical = false;
        private final Paint mPaint;
        private final int mRadius;
        private float mRadiusForAni;
        private ValueAnimator mThumbPressed;
        private ValueAnimator mThumbReleased;
        private final int PRESSED_DURATION = 100;
        private final int RELEASED_DURATION = 300;
        private boolean mIsStateChanged = false;
        private int mAlpha = 255;

        private int modulateAlpha(int paintAlpha, int alpha) {
            int scale = alpha + (alpha >>> 7);
            return (paintAlpha * scale) >>> 8;
        }

        @Override
        public boolean isStateful() {
            return true;
        }

        public ThumbDrawable(int radius, ColorStateList colorStateList, boolean isVertical) {
            mRadiusForAni = radius;
            mRadius = radius;
            mColorStateList = colorStateList;
            mColor = colorStateList.getDefaultColor();
            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(mColor);
            mIsVertical = isVertical;
            initAnimation();
        }

        void initAnimation() {
            mThumbPressed = ValueAnimator.ofFloat((float) mRadius, 0.0f);
            mThumbPressed.setDuration(PRESSED_DURATION);
            mThumbPressed.setInterpolator(new LinearInterpolator());
            mThumbPressed.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    setRadius((float) animation.getAnimatedValue());
                    invalidateSelf();
                }
            });

            mThumbReleased = ValueAnimator.ofFloat(0.0f, (float) mRadius);
            mThumbReleased.setDuration(RELEASED_DURATION);
            mThumbReleased.setInterpolator(SeslAnimationUtils.SINE_IN_OUT_90);
            mThumbReleased.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    setRadius((float) animation.getAnimatedValue());
                    invalidateSelf();
                }
            });
        }

        @Override
        public void draw(Canvas canvas) {
            int prevAlpha = mPaint.getAlpha();
            mPaint.setAlpha(modulateAlpha(prevAlpha, mAlpha));
            canvas.save();
            if (!mIsVertical) {
                canvas.drawCircle(
                        (float) SeslAbsSeekBar.this.mThumbPosX,
                        (float) (SeslAbsSeekBar.this.getHeight() - (SeslAbsSeekBar.this.getPaddingTop() + SeslAbsSeekBar.this.getPaddingBottom())) / 2.0f,
                        mRadiusForAni,
                        mPaint);
            } else {
                canvas.drawCircle(
                        (float) (SeslAbsSeekBar.this.getWidth() - (SeslAbsSeekBar.this.getPaddingLeft() + SeslAbsSeekBar.this.getPaddingRight())) / 2.0F,
                        (float) SeslAbsSeekBar.this.mThumbPosX - SeslAbsSeekBar.this.getPaddingLeft(),
                        this.mRadiusForAni,
                        this.mPaint);
            }
            canvas.restore();
            mPaint.setAlpha(prevAlpha);
        }

        @Override
        public int getIntrinsicWidth() {
            return mRadius * 2;
        }

        @Override
        public int getIntrinsicHeight() {
            return mRadius * 2;
        }

        @Override
        public void setTintList(ColorStateList tint) {
            super.setTintList(tint);
            if (tint != null) {
                mColorStateList = tint;
                mColor = tint.getColorForState(SeslAbsSeekBar.this.getDrawableState(), mColor);
                mPaint.setColor(tint.getColorForState(SeslAbsSeekBar.this.getDrawableState(), mColor));
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

            boolean enabled = false;
            boolean pressed = false;
            for (int i : state) {
                if (i == android.R.attr.state_enabled) {
                    enabled = true;
                } else if (i == android.R.attr.state_pressed) {
                    pressed = true;
                }
            }

            startThumbAnimation(enabled && pressed);

            return onStateChange;
        }

        private void startThumbAnimation(boolean pressed) {
            if (mIsStateChanged != pressed) {
                if (pressed) {
                    startPressedAnimation();
                } else {
                    startReleasedAnimation();
                }
                mIsStateChanged = pressed;
            }
        }

        private void startPressedAnimation() {
            if (!mThumbPressed.isRunning()) {
                if (mThumbReleased.isRunning()) {
                    mThumbReleased.cancel();
                }
                mThumbPressed.start();
            }
        }

        private void startReleasedAnimation() {
            if (!mThumbReleased.isRunning()) {
                if (mThumbPressed.isRunning()) {
                    mThumbPressed.cancel();
                }
                mThumbReleased.start();
            }
        }

        private void setRadius(float radius) {
            mRadiusForAni = radius;
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
    }

    private int getColor(Context context, int colorResId) {
        TypedValue typedValue = new TypedValue();
        TypedArray typedArray = context.obtainStyledAttributes(typedValue.data, new int[]{colorResId});
        int color = typedArray.getColor(0, 0);
        typedArray.recycle();
        return color;
    }

    public int getPaddingTop() {
        return (mCurrentMode == 3 || mCurrentMode == 6) ? super.getPaddingLeft() : super.getPaddingTop();
    }

    public int getPaddingBottom() {
        return (mCurrentMode == 3 || mCurrentMode == 6) ? super.getPaddingRight() : super.getPaddingBottom();
    }

    public int getPaddingLeft() {
        return (mCurrentMode == 3 || mCurrentMode == 6) ? super.getPaddingTop() : super.getPaddingLeft();
    }

    public int getPaddingRight() {
        return (mCurrentMode == 3 || mCurrentMode == 6) ? super.getPaddingBottom() : super.getPaddingRight();
    }
}
