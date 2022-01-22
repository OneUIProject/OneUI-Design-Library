package de.dlyt.yanndroid.oneui.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableContainer;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.TransformationMethod;
import android.util.AttributeSet;
import android.util.Property;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.CompoundButton;

import androidx.appcompat.animation.SeslAnimationUtils;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.text.AllCapsTransformationMethod;
import androidx.appcompat.widget.DrawableUtils;
import androidx.appcompat.widget.TintTypedArray;
import androidx.appcompat.widget.ViewUtils;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.ViewCompat;
import androidx.reflect.view.SeslHapticFeedbackConstantsReflector;

import de.dlyt.yanndroid.oneui.R;

@SuppressLint("RestrictedApi")
public class Switch extends CompoundButton {
    private static final String ACCESSIBILITY_EVENT_CLASS_NAME = "android.widget.Switch";
    private static final int CHANGE_FLING_VELOCITY = 2000;
    private static final int[] CHECKED_STATE_SET = {
            android.R.attr.state_checked
    };
    private static final int MIN_FLING_VELOCITY = 500;
    private static final int SANS = 1;
    private static final int SERIF = 2;
    private static final int MONOSPACE = 3;
    private static final int THUMB_ANIMATION_DURATION = 150;
    private static final int THUMB_ANIMATION_DURATION_ABOVE_M = 500;
    private static final Property<Switch, Float> THUMB_POS =
            new Property<Switch, Float>(Float.class, "thumbPos") {
                @Override
                public Float get(Switch object) {
                    return object.mThumbPosition;
                }

                @Override
                public void set(Switch object, Float value) {
                    object.setThumbPosition(value);
                }
            };
    private static final int TOUCH_MODE_IDLE = 0;
    private static final int TOUCH_MODE_DOWN = 1;
    private static final int TOUCH_MODE_DRAGGING = 2;
    private final Rect mTempRect = new Rect();
    private final TextPaint mTextPaint;
    ThumbAnimation mPositionAnimator;
    private boolean mHasThumbTint = false;
    private boolean mHasThumbTintMode = false;
    private boolean mHasTrackTint = false;
    private boolean mHasTrackTintMode = false;
    private int mMinFlingVelocity;
    private Layout mOffLayout;
    private Layout mOnLayout;
    private boolean mShowText;
    private boolean mSplitTrack;
    private int mSwitchBottom;
    private int mSwitchHeight;
    private int mSwitchLeft;
    private int mSwitchMinWidth;
    private int mSwitchPadding;
    private int mSwitchRight;
    private int mSwitchTop;
    private TransformationMethod mSwitchTransformationMethod;
    private int mSwitchWidth;
    private ColorStateList mTextColors;
    private CharSequence mTextOff;
    private CharSequence mTextOn;
    private Drawable mThumbDrawable;
    private float mThumbPosition;
    private int mThumbTextPadding;
    private ColorStateList mThumbTintList = null;
    private PorterDuff.Mode mThumbTintMode = null;
    private int mThumbWidth;
    private int mTouchMode;
    private int mTouchSlop;
    private float mTouchX;
    private float mTouchY;
    private Drawable mTrackDrawable;
    private Drawable mTrackOffDrawable;
    private Drawable mTrackOnDrawable;
    private ColorStateList mTrackTintList = null;
    private PorterDuff.Mode mTrackTintMode = null;
    private VelocityTracker mVelocityTracker = VelocityTracker.obtain();

    public Switch(Context context) {
        this(context, null);
    }

    public Switch(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.switchStyle);
    }

    public Switch(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);

        final Resources res = getResources();
        mTextPaint.density = res.getDisplayMetrics().density;

        final TintTypedArray a = TintTypedArray.obtainStyledAttributes(context, attrs, R.styleable.Switch, defStyleAttr, 0);
        mThumbDrawable = a.getDrawable(R.styleable.Switch_android_thumb);
        if (mThumbDrawable != null) {
            mThumbDrawable.setCallback(this);
        }
        mTrackDrawable = a.getDrawable(R.styleable.Switch_track);
        if (mTrackDrawable != null) {
            mTrackDrawable.setCallback(this);
            mTrackOnDrawable = mTrackDrawable.getConstantState().newDrawable();
            mTrackOffDrawable = mTrackDrawable.getConstantState().newDrawable();
            mTrackOnDrawable.setState(new int[]{android.R.attr.state_enabled, android.R.attr.state_checked});
            mTrackOffDrawable.setState(new int[]{android.R.attr.state_enabled, -android.R.attr.state_checked});
        }
        mTextOn = a.getText(R.styleable.Switch_android_textOn);
        mTextOff = a.getText(R.styleable.Switch_android_textOff);
        mShowText = a.getBoolean(R.styleable.Switch_showText, true);
        mThumbTextPadding = a.getDimensionPixelSize(R.styleable.Switch_thumbTextPadding, 0);
        mSwitchMinWidth = a.getDimensionPixelSize(R.styleable.Switch_switchMinWidth, 0);
        mSwitchPadding = a.getDimensionPixelSize(R.styleable.Switch_switchPadding, 0);
        mSplitTrack = a.getBoolean(R.styleable.Switch_splitTrack, false);

        ColorStateList thumbTintList = a.getColorStateList(R.styleable.Switch_thumbTint);
        if (thumbTintList != null) {
            mThumbTintList = thumbTintList;
            mHasThumbTint = true;
        }
        PorterDuff.Mode thumbTintMode = DrawableUtils.parseTintMode(a.getInt(R.styleable.Switch_thumbTintMode, -1), null);
        if (mThumbTintMode != thumbTintMode) {
            mThumbTintMode = thumbTintMode;
            mHasThumbTintMode = true;
        }
        if (mHasThumbTint || mHasThumbTintMode) {
            applyThumbTint();
        }

        ColorStateList trackTintList = a.getColorStateList(R.styleable.Switch_trackTint);
        if (trackTintList != null) {
            mTrackTintList = trackTintList;
            mHasTrackTint = true;
        }
        PorterDuff.Mode trackTintMode = DrawableUtils.parseTintMode(a.getInt(R.styleable.Switch_trackTintMode, -1), null);
        if (mTrackTintMode != trackTintMode) {
            mTrackTintMode = trackTintMode;
            mHasTrackTintMode = true;
        }
        if (mHasTrackTint || mHasTrackTintMode) {
            applyTrackTint();
        }

        final int appearance = a.getResourceId(R.styleable.Switch_switchTextAppearance, 0);
        if (appearance != 0) {
            setSwitchTextAppearance(context, appearance);
        }

        a.recycle();

        final ViewConfiguration config = ViewConfiguration.get(context);
        mTouchSlop = config.getScaledTouchSlop();
        mMinFlingVelocity = config.getScaledMinimumFlingVelocity();

        refreshDrawableState();
        setChecked(isChecked());
    }

    private static float constrain(float amount, float low, float high) {
        return amount < low ? low : (amount > high ? high : amount);
    }

    public void setSwitchTextAppearance(Context context, int resid) {
        final TintTypedArray appearance = TintTypedArray.obtainStyledAttributes(context, resid, R.styleable.TextAppearance);

        ColorStateList colors;
        int ts;

        colors = appearance.getColorStateList(R.styleable.TextAppearance_android_textColor);
        if (colors != null) {
            mTextColors = colors;
        } else {
            mTextColors = getTextColors();
        }

        ts = appearance.getDimensionPixelSize(R.styleable.TextAppearance_android_textSize, 0);
        if (ts != 0) {
            if (ts != mTextPaint.getTextSize()) {
                mTextPaint.setTextSize(ts);
                requestLayout();
            }
        }

        int typefaceIndex, styleIndex;
        typefaceIndex = appearance.getInt(R.styleable.TextAppearance_android_typeface, -1);
        styleIndex = appearance.getInt(R.styleable.TextAppearance_android_textStyle, -1);

        setSwitchTypefaceByIndex(typefaceIndex, styleIndex);

        boolean allCaps = appearance.getBoolean(R.styleable.TextAppearance_textAllCaps, false);
        if (allCaps) {
            mSwitchTransformationMethod = new AllCapsTransformationMethod(getContext());
        } else {
            mSwitchTransformationMethod = null;
        }

        appearance.recycle();
    }

    private void setSwitchTypefaceByIndex(int typefaceIndex, int styleIndex) {
        Typeface tf = null;
        switch (typefaceIndex) {
            case SANS:
                tf = Typeface.SANS_SERIF;
                break;

            case SERIF:
                tf = Typeface.SERIF;
                break;

            case MONOSPACE:
                tf = Typeface.MONOSPACE;
                break;
        }

        setSwitchTypeface(tf, styleIndex);
    }

    public void setSwitchTypeface(Typeface tf, int style) {
        Typeface tf2;
        if (style > 0) {
            if (tf == null) {
                tf2 = Typeface.defaultFromStyle(style);
            } else {
                tf2 = Typeface.create(tf, style);
            }

            setSwitchTypeface(tf2);

            int typefaceStyle = tf2 != null ? tf2.getStyle() : 0;
            int need = style & ~typefaceStyle;
            mTextPaint.setFakeBoldText((need & Typeface.BOLD) != 0);
            mTextPaint.setTextSkewX((need & Typeface.ITALIC) != 0 ? -0.25f : 0);
        } else {
            mTextPaint.setFakeBoldText(false);
            mTextPaint.setTextSkewX(0);
            setSwitchTypeface(tf);
        }
    }

    public void setSwitchTypeface(Typeface typeface) {
        if ((mTextPaint.getTypeface() != null && !mTextPaint.getTypeface().equals(typeface)) || (mTextPaint.getTypeface() == null && typeface != null)) {
            mTextPaint.setTypeface(typeface);

            requestLayout();
            invalidate();
        }
    }

    public int getSwitchPadding() {
        return mSwitchPadding;
    }

    public void setSwitchPadding(int pixels) {
        mSwitchPadding = pixels;
        requestLayout();
    }

    public int getSwitchMinWidth() {
        return mSwitchMinWidth;
    }

    public void setSwitchMinWidth(int pixels) {
        mSwitchMinWidth = pixels;
        requestLayout();
    }

    public int getThumbTextPadding() {
        return mThumbTextPadding;
    }

    public void setThumbTextPadding(int pixels) {
        mThumbTextPadding = pixels;
        requestLayout();
    }

    public void setTrackResource(int resId) {
        setTrackDrawable(AppCompatResources.getDrawable(getContext(), resId));
    }

    public Drawable getTrackDrawable() {
        return mTrackDrawable;
    }

    public void setTrackDrawable(Drawable track) {
        if (mTrackDrawable != null) {
            mTrackDrawable.setCallback(null);
        }
        mTrackDrawable = track;
        if (track != null) {
            track.setCallback(this);
        }
        requestLayout();
    }

    public ColorStateList getTrackTintList() {
        return mTrackTintList;
    }

    public void setTrackTintList(ColorStateList tint) {
        mTrackTintList = tint;
        mHasTrackTint = true;

        applyTrackTint();
    }

    public PorterDuff.Mode getTrackTintMode() {
        return mTrackTintMode;
    }

    public void setTrackTintMode(PorterDuff.Mode tintMode) {
        mTrackTintMode = tintMode;
        mHasTrackTintMode = true;

        applyTrackTint();
    }

    private void applyTrackTint() {
        if (mTrackDrawable != null && (mHasTrackTint || mHasTrackTintMode)) {
            mTrackDrawable = mTrackDrawable.mutate();

            if (mHasTrackTint) {
                DrawableCompat.setTintList(mTrackDrawable, mTrackTintList);
            }

            if (mHasTrackTintMode) {
                DrawableCompat.setTintMode(mTrackDrawable, mTrackTintMode);
            }

            if (mTrackDrawable.isStateful()) {
                mTrackDrawable.setState(getDrawableState());
            }
        }
    }

    public void setThumbResource(int resId) {
        setThumbDrawable(AppCompatResources.getDrawable(getContext(), resId));
    }

    public Drawable getThumbDrawable() {
        return mThumbDrawable;
    }

    public void setThumbDrawable(Drawable thumb) {
        if (mThumbDrawable != null) {
            mThumbDrawable.setCallback(null);
        }
        mThumbDrawable = thumb;
        if (thumb != null) {
            thumb.setCallback(this);
        }
        requestLayout();
    }

    public ColorStateList getThumbTintList() {
        return mThumbTintList;
    }

    public void setThumbTintList(ColorStateList tint) {
        mThumbTintList = tint;
        mHasThumbTint = true;

        applyThumbTint();
    }

    public void setThumbTintMode(PorterDuff.Mode tintMode) {
        mThumbTintMode = tintMode;
        mHasThumbTintMode = true;

        applyThumbTint();
    }

    private void applyThumbTint() {
        if (mThumbDrawable != null && (mHasThumbTint || mHasThumbTintMode)) {
            mThumbDrawable = mThumbDrawable.mutate();

            if (mHasThumbTint) {
                DrawableCompat.setTintList(mThumbDrawable, mThumbTintList);
            }

            if (mHasThumbTintMode) {
                DrawableCompat.setTintMode(mThumbDrawable, mThumbTintMode);
            }

            if (mThumbDrawable.isStateful()) {
                mThumbDrawable.setState(getDrawableState());
            }
        }
    }

    public boolean getSplitTrack() {
        return mSplitTrack;
    }

    public void setSplitTrack(boolean splitTrack) {
        mSplitTrack = splitTrack;
        invalidate();
    }

    public CharSequence getTextOn() {
        return mTextOn;
    }

    public void setTextOn(CharSequence textOn) {
        mTextOn = textOn;
        requestLayout();
    }

    public CharSequence getTextOff() {
        return mTextOff;
    }

    public void setTextOff(CharSequence textOff) {
        mTextOff = textOff;
        requestLayout();
    }

    public boolean getShowText() {
        return mShowText;
    }

    public void setShowText(boolean showText) {
        if (mShowText != showText) {
            mShowText = showText;
            requestLayout();
        }
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mShowText) {
            if (mOnLayout == null) {
                mOnLayout = makeLayout(mTextOn);
            }

            if (mOffLayout == null) {
                mOffLayout = makeLayout(mTextOff);
            }
        }

        final Rect padding = mTempRect;
        final int thumbWidth;
        final int thumbHeight;
        if (mThumbDrawable != null) {
            mThumbDrawable.getPadding(padding);
            thumbWidth = mThumbDrawable.getIntrinsicWidth() - padding.left - padding.right;
            thumbHeight = mThumbDrawable.getIntrinsicHeight();
        } else {
            thumbWidth = 0;
            thumbHeight = 0;
        }

        final int maxTextWidth;
        if (mShowText) {
            maxTextWidth = Math.max(mOnLayout.getWidth(), mOffLayout.getWidth()) + mThumbTextPadding * 2;
        } else {
            maxTextWidth = 0;
        }

        mThumbWidth = Math.max(maxTextWidth, thumbWidth);

        final int trackHeight;
        if (mTrackDrawable != null) {
            mTrackDrawable.getPadding(padding);
            trackHeight = mTrackDrawable.getIntrinsicHeight();
        } else {
            padding.setEmpty();
            trackHeight = 0;
        }

        int paddingLeft = padding.left;
        int paddingRight = padding.right;
        if (mThumbDrawable != null) {
            final Rect inset = DrawableUtils.getOpticalBounds(mThumbDrawable);
            paddingLeft = Math.max(paddingLeft, inset.left);
            paddingRight = Math.max(paddingRight, inset.right);
        }

        final int switchWidth = getResources().getDimensionPixelSize(R.dimen.sesl_switch_width);
        final int switchHeight = Math.max(trackHeight, thumbHeight);
        mSwitchWidth = switchWidth;
        mSwitchHeight = switchHeight;

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        final int measuredHeight = getMeasuredHeight();
        if (measuredHeight < switchHeight) {
            setMeasuredDimension(getMeasuredWidthAndState(), switchHeight);
        }
    }

    @Override
    public void onPopulateAccessibilityEvent(AccessibilityEvent event) {
        super.onPopulateAccessibilityEvent(event);

        final CharSequence text;
        final CharSequence textOn = getResources().getString(R.string.sesl_on_text);
        final CharSequence textOff = getResources().getString(R.string.sesl_off_text);
        text = isChecked() ? textOn : textOff;
        if (text != null) {
            event.getText().add(text);
        }
    }

    private Layout makeLayout(CharSequence text) {
        final CharSequence transformed = (mSwitchTransformationMethod != null) ? mSwitchTransformationMethod.getTransformation(text, this) : text;

        return new StaticLayout(transformed, mTextPaint, transformed != null ? (int) Math.ceil(Layout.getDesiredWidth(transformed, mTextPaint)) : 0, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0, true);
    }

    private boolean hitThumb(float x, float y) {
        if (mThumbDrawable == null) {
            return false;
        }

        final int thumbOffset = getThumbOffset();

        mThumbDrawable.getPadding(mTempRect);
        final int thumbTop = mSwitchTop - mTouchSlop;
        final int thumbLeft = mSwitchLeft + thumbOffset - mTouchSlop;
        final int thumbRight = thumbLeft + mThumbWidth + mTempRect.left + mTempRect.right + mTouchSlop;
        final int thumbBottom = mSwitchBottom + mTouchSlop;
        return x > thumbLeft && x < thumbRight && y > thumbTop && y < thumbBottom;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        mVelocityTracker.addMovement(ev);
        final int action = ev.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                final float x = ev.getX();
                final float y = ev.getY();
                if (isEnabled() && hitThumb(x, y)) {
                    mTouchMode = TOUCH_MODE_DOWN;
                    mTouchX = x;
                    mTouchY = y;
                }
                break;
            }

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                if (mTouchMode == TOUCH_MODE_DRAGGING) {
                    if (Build.VERSION.SDK_INT >= 28 && action == MotionEvent.ACTION_UP && (mTouchMode == TOUCH_MODE_DRAGGING || mTouchMode == TOUCH_MODE_DOWN)) {
                        performHapticFeedback(SeslHapticFeedbackConstantsReflector.semGetVibrationIndex(27));
                    }
                    stopDrag(ev);
                    super.onTouchEvent(ev);
                    return true;
                }
                mTouchMode = TOUCH_MODE_IDLE;
                mVelocityTracker.clear();
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                switch (mTouchMode) {
                    case TOUCH_MODE_DOWN: {
                        final float x = ev.getX();
                        final float y = ev.getY();
                        if (Math.abs(x - mTouchX) > mTouchSlop || Math.abs(y - mTouchY) > mTouchSlop) {
                            mTouchMode = TOUCH_MODE_DRAGGING;
                            getParent().requestDisallowInterceptTouchEvent(true);
                            mTouchX = x;
                            mTouchY = y;
                            return true;
                        }
                        break;
                    }

                    case TOUCH_MODE_DRAGGING: {
                        final float x = ev.getX();
                        final int thumbScrollRange = getThumbScrollRange();
                        final float thumbScrollOffset = x - mTouchX;
                        float dPos;
                        if (thumbScrollRange != 0) {
                            dPos = thumbScrollOffset / thumbScrollRange;
                        } else {
                            dPos = thumbScrollOffset > 0 ? 1 : -1;
                        }
                        if (ViewUtils.isLayoutRtl(this)) {
                            dPos = -dPos;
                        }
                        final float newPos = constrain(mThumbPosition + dPos, 0, 1);
                        if (newPos != mThumbPosition) {
                            mTouchX = x;
                            setThumbPosition(newPos);
                        }
                        return true;
                    }
                }
                break;
            }
        }

        return super.onTouchEvent(ev);
    }

    private void cancelSuperTouch(MotionEvent ev) {
        MotionEvent cancel = MotionEvent.obtain(ev);
        cancel.setAction(MotionEvent.ACTION_CANCEL);
        super.onTouchEvent(cancel);
        cancel.recycle();
    }

    private void stopDrag(MotionEvent ev) {
        mTouchMode = TOUCH_MODE_IDLE;

        final boolean commitChange = ev.getAction() == MotionEvent.ACTION_UP && isEnabled();
        final boolean oldState = isChecked();
        final boolean newState;
        if (commitChange) {
            mVelocityTracker.computeCurrentVelocity(1000);
            final float xvel = mVelocityTracker.getXVelocity();
            if (Math.abs(xvel) > 2000.0f || (Math.abs(xvel) > 500.0f || mThumbPosition != 0.0f || mThumbPosition != 1.0f)) {
                newState = ViewUtils.isLayoutRtl(this) ? (xvel < 0) : (xvel > 0);
            } else {
                newState = getTargetCheckedState();
            }

        } else {
            newState = oldState;
        }

        if (newState != oldState) {
            playSoundEffect(SoundEffectConstants.CLICK);
        }
        setChecked(newState);
        cancelSuperTouch(ev);
    }

    private void animateThumbToCheckedState(final boolean newCheckedState) {
        if (mPositionAnimator != null) {
            cancelPositionAnimator();
        }

        final float targetPosition = newCheckedState ? 1 : 0;
        mPositionAnimator = new ThumbAnimation(mThumbPosition, targetPosition);
        mPositionAnimator.setInterpolator(SeslAnimationUtils.ELASTIC_50);
        mPositionAnimator.setDuration(THUMB_ANIMATION_DURATION_ABOVE_M);
        mPositionAnimator.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationStart(Animation animation) {
            }

            public void onAnimationEnd(Animation animation) {
                if (mPositionAnimator == animation) {
                    setThumbPosition(targetPosition);
                    mPositionAnimator = null;
                }
            }

            public void onAnimationRepeat(Animation animation) {
            }
        });

        startAnimation(mPositionAnimator);
    }

    private void cancelPositionAnimator() {
        if (mPositionAnimator != null) {
            clearAnimation();
            mPositionAnimator.cancel();
        }
    }

    private boolean getTargetCheckedState() {
        return mThumbPosition > 0.5f;
    }

    void setThumbPosition(float position) {
        mThumbPosition = position;
        invalidate();
    }

    @Override
    public void toggle() {
        setChecked(!isChecked());
    }

    @Override
    public void setChecked(boolean checked) {
        super.setChecked(checked);

        boolean checked2 = isChecked();

        if (getWindowToken() != null && ViewCompat.isLaidOut(this)) {
            animateThumbToCheckedState(checked2);
        } else {
            cancelPositionAnimator();
            setThumbPosition(checked2 ? 1 : 0);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        int opticalInsetLeft = 0;
        int opticalInsetRight = 0;
        if (mThumbDrawable != null) {
            final Rect trackPadding = mTempRect;
            if (mTrackDrawable != null) {
                mTrackDrawable.getPadding(trackPadding);
            } else {
                trackPadding.setEmpty();
            }

            final Rect insets = DrawableUtils.getOpticalBounds(mThumbDrawable);
            opticalInsetLeft = Math.max(0, insets.left - trackPadding.left);
            opticalInsetRight = Math.max(0, insets.right - trackPadding.right);
        }

        final int switchRight;
        final int switchLeft;
        if (ViewUtils.isLayoutRtl(this)) {
            switchLeft = getPaddingLeft() + opticalInsetLeft;
            switchRight = switchLeft + mSwitchWidth - opticalInsetLeft - opticalInsetRight;
        } else {
            switchRight = getWidth() - getPaddingRight() - opticalInsetRight;
            switchLeft = switchRight - mSwitchWidth + opticalInsetLeft + opticalInsetRight;
        }

        final int switchTop;
        final int switchBottom;
        switch (getGravity() & Gravity.VERTICAL_GRAVITY_MASK) {
            default:
                switchTop = getPaddingTop();
                switchBottom = switchTop + mSwitchHeight;
                break;

            case Gravity.CENTER_VERTICAL:
                switchTop = (getPaddingTop() + getHeight() - getPaddingBottom()) / 2 - mSwitchHeight / 2;
                switchBottom = switchTop + mSwitchHeight;
                break;

            case Gravity.BOTTOM:
                switchBottom = getHeight() - getPaddingBottom();
                switchTop = switchBottom - mSwitchHeight;
                break;
        }

        mSwitchLeft = switchLeft;
        mSwitchTop = switchTop;
        mSwitchBottom = switchBottom;
        mSwitchRight = switchRight;
    }

    @Override
    public void draw(Canvas c) {
        final Rect padding = mTempRect;
        final int switchLeft = mSwitchLeft;
        final int switchTop = mSwitchTop;
        final int switchRight = mSwitchRight;
        final int switchBottom = mSwitchBottom;

        int thumbInitialLeft = switchLeft + getThumbOffset();

        final Rect thumbInsets;
        if (mThumbDrawable != null) {
            thumbInsets = DrawableUtils.getOpticalBounds(mThumbDrawable);
        } else {
            thumbInsets = DrawableUtils.INSETS_NONE;
        }

        if (mTrackDrawable != null) {
            mTrackDrawable.getPadding(padding);

            thumbInitialLeft += padding.left;

            int trackLeft = switchLeft;
            int trackTop = switchTop;
            int trackRight = switchRight;
            int trackBottom = switchBottom;
            if (thumbInsets != null) {
                if (thumbInsets.left > padding.left) {
                    trackLeft += thumbInsets.left - padding.left;
                }
                if (thumbInsets.top > padding.top) {
                    trackTop += thumbInsets.top - padding.top;
                }
                if (thumbInsets.right > padding.right) {
                    trackRight -= thumbInsets.right - padding.right;
                }
                if (thumbInsets.bottom > padding.bottom) {
                    trackBottom -= thumbInsets.bottom - padding.bottom;
                }
            }
            mTrackDrawable.setBounds(trackLeft, trackTop, trackRight, trackBottom);
        }

        if (mThumbDrawable != null) {
            mThumbDrawable.getPadding(padding);

            final int thumbLeft = thumbInitialLeft - padding.left;
            final int thumbRight = thumbInitialLeft + mThumbWidth + padding.right;
            mThumbDrawable.setBounds(thumbLeft, switchTop, thumbRight, switchBottom);

            final Drawable background = getBackground();
            if (background != null) {
                DrawableCompat.setHotspotBounds(background, thumbLeft, switchTop, thumbRight, switchBottom);
            }
        }

        super.draw(c);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        final Rect padding = mTempRect;
        final Drawable trackDrawable = mTrackDrawable;
        if (trackDrawable != null) {
            trackDrawable.getPadding(padding);
        } else {
            padding.setEmpty();
        }

        final int switchInnerTop = mSwitchTop + padding.top;
        final int switchInnerBottom = mSwitchBottom - padding.bottom;

        final Drawable thumbDrawable = mThumbDrawable;
        if (trackDrawable != null) {
            if (mSplitTrack && thumbDrawable != null) {
                final Rect insets = DrawableUtils.getOpticalBounds(thumbDrawable);
                thumbDrawable.copyBounds(padding);
                padding.left += insets.left;
                padding.right -= insets.right;

                final int saveCount = canvas.save();
                canvas.clipRect(padding, Region.Op.DIFFERENCE);
                trackDrawable.draw(canvas);
                canvas.restoreToCount(saveCount);
            } else {
                Drawable overDrawable = isChecked() ? mTrackOffDrawable : mTrackOnDrawable;
                overDrawable.setBounds(trackDrawable.getBounds());

                int alpha = (int) (mThumbPosition * 255.0f);
                if (alpha > 255) {
                    alpha = 255;
                } else if (alpha < 0) {
                    alpha = 0;
                }

                int r_alpah = 255 - alpha;
                if (isChecked()) {
                    trackDrawable.setAlpha(alpha);
                    overDrawable.setAlpha(r_alpah);
                } else {
                    trackDrawable.setAlpha(r_alpah);
                    overDrawable.setAlpha(alpha);
                }

                trackDrawable.draw(canvas);
                overDrawable.draw(canvas);
            }
        }

        final int saveCount2 = canvas.save();

        if (thumbDrawable != null) {
            thumbDrawable.draw(canvas);
        }

        final Layout switchText = getTargetCheckedState() ? mOnLayout : mOffLayout;
        if (switchText != null) {
            final int drawableState[] = getDrawableState();
            if (mTextColors != null) {
                mTextPaint.setColor(mTextColors.getColorForState(drawableState, 0));
            }
            mTextPaint.drawableState = drawableState;

            final int cX;
            if (thumbDrawable != null) {
                final Rect bounds = thumbDrawable.getBounds();
                cX = bounds.left + bounds.right;
            } else {
                cX = getWidth();
            }

            final int left = cX / 2 - switchText.getWidth() / 2;
            final int top = (switchInnerTop + switchInnerBottom) / 2 - switchText.getHeight() / 2;
            canvas.translate(left, top);
            switchText.draw(canvas);
        }

        canvas.restoreToCount(saveCount2);
    }

    @Override
    public int getCompoundPaddingLeft() {
        if (!ViewUtils.isLayoutRtl(this)) {
            return super.getCompoundPaddingLeft();
        }
        int padding = super.getCompoundPaddingLeft() + mSwitchWidth;
        if (!TextUtils.isEmpty(getText())) {
            padding += mSwitchPadding;
        }
        return padding;
    }

    @Override
    public int getCompoundPaddingRight() {
        if (ViewUtils.isLayoutRtl(this)) {
            return super.getCompoundPaddingRight();
        }
        int padding = super.getCompoundPaddingRight() + mSwitchWidth;
        if (!TextUtils.isEmpty(getText())) {
            padding += mSwitchPadding;
        }
        return padding;
    }

    private int getThumbOffset() {
        final float thumbPosition;
        if (ViewUtils.isLayoutRtl(this)) {
            thumbPosition = 1 - mThumbPosition;
        } else {
            thumbPosition = mThumbPosition;
        }
        return (int) (thumbPosition * getThumbScrollRange() + 0.5f);
    }

    private int getThumbScrollRange() {
        if (mTrackDrawable != null) {
            final Rect padding = mTempRect;
            mTrackDrawable.getPadding(padding);

            final Rect insets;
            if (mThumbDrawable != null) {
                insets = DrawableUtils.getOpticalBounds(mThumbDrawable);
            } else {
                insets = DrawableUtils.INSETS_NONE;
            }

            return mSwitchWidth - mThumbWidth - padding.left - padding.right - insets.left - insets.right;
        } else {
            return 0;
        }
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (isChecked()) {
            mergeDrawableStates(drawableState, CHECKED_STATE_SET);
        }
        return drawableState;
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();

        final int[] state = getDrawableState();
        boolean changed = false;

        final Drawable thumbDrawable = mThumbDrawable;
        if (thumbDrawable != null && thumbDrawable.isStateful()) {
            changed |= thumbDrawable.setState(state);
        }

        final Drawable trackDrawable = mTrackDrawable;
        if (trackDrawable != null && trackDrawable.isStateful()) {
            changed |= trackDrawable.setState(state);
        }

        if (changed) {
            invalidate();
        }
    }

    @Override
    public void drawableHotspotChanged(float x, float y) {
        super.drawableHotspotChanged(x, y);

        if (mThumbDrawable != null) {
            DrawableCompat.setHotspot(mThumbDrawable, x, y);
        }

        if (mTrackDrawable != null) {
            DrawableCompat.setHotspot(mTrackDrawable, x, y);
        }
    }

    @Override
    protected boolean verifyDrawable(Drawable who) {
        return super.verifyDrawable(who) || who == mThumbDrawable || who == mTrackDrawable;
    }

    @Override
    public void jumpDrawablesToCurrentState() {
        super.jumpDrawablesToCurrentState();

        if (mThumbDrawable != null) {
            mThumbDrawable.jumpToCurrentState();
        }

        if (mTrackDrawable != null) {
            mTrackDrawable.jumpToCurrentState();
        }

        cancelPositionAnimator();
        setThumbPosition(isChecked() ? 1.0f : 0.0f);
    }

    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(ACCESSIBILITY_EVENT_CLASS_NAME);
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(ACCESSIBILITY_EVENT_CLASS_NAME);
        CharSequence switchText;
        CharSequence textOn = getResources().getString(R.string.sesl_on_text);
        CharSequence textOff = getResources().getString(R.string.sesl_off_text);
        switchText = isChecked() ? textOn : textOff;
        if (!TextUtils.isEmpty(switchText)) {
            CharSequence oldText = info.getText();
            if (TextUtils.isEmpty(oldText)) {
                info.setText(switchText);
            } else {
                StringBuilder newText = new StringBuilder();
                newText.append(oldText).append(' ').append(switchText);
                info.setText(newText);
            }
        }
    }

    public void seslSetTrackStrokeColor(int color) {
        ((GradientDrawable) ((LayerDrawable) ((DrawableContainer.DrawableContainerState) mTrackDrawable.getConstantState()).getChildren()[2]).findDrawableByLayerId(R.id.sesl_switch_track_on)).setStroke(4, color);
    }


    private class ThumbAnimation extends Animation {
        final float mDiff;
        final float mEndPosition;
        final float mStartPosition;

        ThumbAnimation(float startPosition, float endPosition) {
            mStartPosition = startPosition;
            mEndPosition = endPosition;
            mDiff = endPosition - startPosition;
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            setThumbPosition(mStartPosition + (mDiff * interpolatedTime));
        }
    }
}