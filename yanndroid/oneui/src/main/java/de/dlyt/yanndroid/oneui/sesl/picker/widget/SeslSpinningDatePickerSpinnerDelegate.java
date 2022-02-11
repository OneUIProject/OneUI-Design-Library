package de.dlyt.yanndroid.oneui.sesl.picker.widget;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.icu.text.SimpleDateFormat;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeProvider;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.PathInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.OverScroller;
import android.widget.Scroller;

import androidx.appcompat.util.SeslMisc;
import androidx.core.content.res.ResourcesCompat;
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.FloatValueHolder;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;
import androidx.reflect.content.res.SeslCompatibilityInfoReflector;
import androidx.reflect.content.res.SeslConfigurationReflector;
import androidx.reflect.graphics.SeslPaintReflector;
import androidx.reflect.lunarcalendar.SeslSolarLunarConverterReflector;
import androidx.reflect.media.SeslAudioManagerReflector;
import androidx.reflect.view.SeslHapticFeedbackConstantsReflector;
import androidx.reflect.view.SeslViewReflector;

import java.io.File;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import dalvik.system.PathClassLoader;
import de.dlyt.yanndroid.oneui.R;
import de.dlyt.yanndroid.oneui.sesl.picker.util.SeslAnimationListener;

public class SeslSpinningDatePickerSpinnerDelegate extends SeslSpinningDatePickerSpinner.AbsDatePickerDelegate {
    public final PathInterpolator ALPHA_PATH_INTERPOLATOR;
    public final PathInterpolator SIZE_PATH_INTERPOLATOR;
    public AccessibilityManager mAccessibilityManager;
    public AccessibilityNodeProviderImpl mAccessibilityNodeProvider;
    public float mActivatedAlpha;
    public final Scroller mAdjustScroller;
    public float mAlpha;
    public SeslAnimationListener mAnimationListener;
    public AudioManager mAudioManager;
    public int mBottomSelectionDividerBottom;
    public ChangeCurrentByOneFromLongPressCommand mChangeCurrentByOneFromLongPressCommand;
    public int mChangeValueBy = 1;
    public ValueAnimator mColorInAnimator;
    public ValueAnimator mColorOutAnimator;
    public ValueAnimator.AnimatorUpdateListener mColorUpdateListener;
    public final boolean mComputeMaxWidth;
    public int mCurrentScrollOffset;
    public final Scroller mCustomScroller;
    public boolean mCustomTypefaceSet = false;
    public boolean mDecrementVirtualButtonPressed;
    public final Typeface mDefaultTypeface;
    public ValueAnimator mFadeInAnimator;
    public ValueAnimator mFadeOutAnimator;
    public Scroller mFlingScroller;
    public SeslSpinningDatePickerSpinner.Formatter mFormatter;
    public OverScroller mGravityScroller;
    public HapticPreDrawListener mHapticPreDrawListener;
    public Typeface mHcfFocusedTypefaceBold;
    public final int mHcfUnfocusedTextSizeDiff;
    public final float mHeightRatio;
    public FloatValueHolder mHolder;
    public float mIdleAlpha;
    public boolean mIgnoreMoveEvents;
    public boolean mIncrementVirtualButtonPressed;
    public float mInitialAlpha;
    public int mInitialScrollOffset = Integer.MIN_VALUE;
    public final EditText mInputText;
    public boolean mIsHcfEnabled;
    public boolean mIsLongClicked = false;
    public boolean mIsLongPressed = false;
    public boolean mIsLunar;
    public boolean mIsStartingAnimation = false;
    public boolean mIsValueChanged = false;
    public long mLastDownEventTime;
    public float mLastDownEventY;
    public float mLastDownOrMoveEventY;
    public int mLastFocusedChildVirtualViewId;
    public int mLastHoveredChildVirtualViewId;
    public final Typeface mLegacyTypeface;
    public final Scroller mLinearScroller;
    public String[] mLongMonths;
    public int mLongPressCount;
    public long mLongPressUpdateInterval = 300;
    public boolean mLongPressed_FIRST_SCROLL;
    public boolean mLongPressed_SECOND_SCROLL;
    public boolean mLongPressed_THIRD_SCROLL;
    public final int mMaxHeight;
    public Calendar mMaxValue;
    public int mMaxWidth;
    public int mMaximumFlingVelocity;
    public final int mMinHeight;
    public Calendar mMinValue;
    public final int mMinWidth;
    public int mMinimumFlingVelocity;
    public int mModifiedTxtHeight;
    public SeslSpinningDatePickerSpinner.OnScrollListener mOnScrollListener;
    public SeslSpinningDatePickerSpinner.OnSpinnerDateClickListener mOnSpinnerDateClickListener;
    public SeslSpinningDatePickerSpinner.OnValueChangeListener mOnValueChangeListener;
    public PathClassLoader mPathClassLoader = null;
    public boolean mPerformClickOnTap;
    public String mPickerContentDescription;
    public int mPickerSoundIndex;
    public Typeface mPickerSubTypeface;
    public Typeface mPickerTypeface;
    public int mPickerVibrateIndex;
    public final PressedStateHelper mPressedStateHelper;
    public int mPreviousScrollerY;
    public float mPreviousSpringY;
    public boolean mReservedStartAnimation = false;
    public int mScrollState = 0;
    public final int mSelectionDividerHeight;
    public int mSelectorElementHeight;
    public final HashMap<Calendar, String> mSelectorIndexToStringCache = new HashMap<>();
    public final Calendar[] mSelectorIndices = new Calendar[5];
    public int mSelectorTextGapHeight;
    public Paint mSelectorWheelPaint;
    public String[] mShortMonths;
    public boolean mSkipNumbers;
    public Object mSolarLunarConverter = null;
    public SpringAnimation mSpringAnimation;
    public DynamicAnimation.OnAnimationEndListener mSpringAnimationEndListener;
    public DynamicAnimation.OnAnimationUpdateListener mSpringAnimationUpdateListener;
    public boolean mSpringFlingRunning;
    public int mTextColor;
    public final int mTextColorIdle;
    public final int mTextColorScrolling;
    public int mTextSize;
    public int mTopSelectionDividerTop;
    public int mTouchSlop;
    public ValueAnimator.AnimatorUpdateListener mUpdateListener;
    public Calendar mValue;
    public int mValueChangeOffset;
    public VelocityTracker mVelocityTracker;
    public final Drawable mVirtualButtonFocusedDrawable;
    public boolean mWrapSelectorWheel;
    public boolean mWrapSelectorWheelPreferred = true;

    @Override // androidx.picker.widget.SeslSpinningDatePickerSpinner.DatePickerDelegate
    public boolean dispatchKeyEventPreIme(KeyEvent keyEvent) {
        return false;
    }

    @Override // androidx.picker.widget.SeslSpinningDatePickerSpinner.DatePickerDelegate
    public void onWindowVisibilityChanged(int i) {
    }

    public static boolean access$2080(SeslSpinningDatePickerSpinnerDelegate r1, int r2) {
        boolean r0 = (r2 == 1) ^ r1.mIncrementVirtualButtonPressed;
        r1.mIncrementVirtualButtonPressed = r0;
        return r0;
    }

    public static boolean access$2280(SeslSpinningDatePickerSpinnerDelegate r1, int r2) {
        boolean r0 = (r2 == 1) ^ r1.mDecrementVirtualButtonPressed;
        r1.mDecrementVirtualButtonPressed = r0;
        return r0;
    }

    public SeslSpinningDatePickerSpinnerDelegate(SeslSpinningDatePickerSpinner seslSpinningDatePickerSpinner, Context context, AttributeSet attributeSet, int i, int i2) {
        super(seslSpinningDatePickerSpinner, context);
        int i3;
        int i4;
        PathInterpolator pathInterpolator = new PathInterpolator(0.5f, 0.0f, 0.4f, 1.0f);
        this.SIZE_PATH_INTERPOLATOR = pathInterpolator;
        PathInterpolator pathInterpolator2 = new PathInterpolator(0.17f, 0.17f, 0.83f, 0.83f);
        this.ALPHA_PATH_INTERPOLATOR = pathInterpolator2;
        this.mActivatedAlpha = 0.4f;
        this.mIdleAlpha = 0.1f;
        this.mAlpha = 0.1f;
        this.mInitialAlpha = 1.0f;
        this.mUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
            /* class androidx.picker.widget.SeslSpinningDatePickerSpinnerDelegate.AnonymousClass2 */

            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                SeslSpinningDatePickerSpinnerDelegate.this.mAlpha = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                SeslSpinningDatePickerSpinnerDelegate.this.mDelegator.invalidate();
            }
        };
        this.mColorUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
            /* class androidx.picker.widget.SeslSpinningDatePickerSpinnerDelegate.AnonymousClass3 */

            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                SeslSpinningDatePickerSpinnerDelegate.this.mTextColor = ((Integer) valueAnimator.getAnimatedValue()).intValue();
                SeslSpinningDatePickerSpinnerDelegate.this.mDelegator.invalidate();
            }
        };
        this.mSpringAnimationUpdateListener = new DynamicAnimation.OnAnimationUpdateListener() {
            /* class androidx.picker.widget.SeslSpinningDatePickerSpinnerDelegate.AnonymousClass4 */

            @Override
            // androidx.dynamicanimation.animation.DynamicAnimation.OnAnimationUpdateListener
            public void onAnimationUpdate(DynamicAnimation dynamicAnimation, float f, float f2) {
                float f3 = f - SeslSpinningDatePickerSpinnerDelegate.this.mPreviousSpringY;
                if (SeslSpinningDatePickerSpinnerDelegate.this.mSpringFlingRunning || Math.round(f3) != 0) {
                    if (Math.round(f3) == 0) {
                        SeslSpinningDatePickerSpinnerDelegate.this.mSpringFlingRunning = false;
                    }
                    SeslSpinningDatePickerSpinnerDelegate.this.scrollBy(0, Math.round(f3));
                    SeslSpinningDatePickerSpinnerDelegate.this.mPreviousSpringY = f;
                    SeslSpinningDatePickerSpinnerDelegate.this.mDelegator.invalidate();
                    return;
                }
                dynamicAnimation.cancel();
                SeslSpinningDatePickerSpinnerDelegate.this.ensureScrollWheelAdjusted();
            }
        };
        this.mSpringAnimationEndListener = new DynamicAnimation.OnAnimationEndListener() {
            /* class androidx.picker.widget.SeslSpinningDatePickerSpinnerDelegate.AnonymousClass5 */

            @Override // androidx.dynamicanimation.animation.DynamicAnimation.OnAnimationEndListener
            public void onAnimationEnd(DynamicAnimation dynamicAnimation, boolean z, float f, float f2) {
                SeslSpinningDatePickerSpinnerDelegate.this.mSpringFlingRunning = false;
                SeslSpinningDatePickerSpinnerDelegate.this.mGravityScroller.forceFinished(true);
                SeslSpinningDatePickerSpinnerDelegate.this.startFadeAnimation(true);
            }
        };
        Resources resources = this.mContext.getResources();
        int dimensionPixelSize = resources.getDimensionPixelSize(R.dimen.sesl_number_picker_spinner_height);
        int dimensionPixelSize2 = resources.getDimensionPixelSize(R.dimen.sesl_number_picker_spinner_width);
        this.mHeightRatio = ((float) resources.getDimensionPixelSize(R.dimen.sesl_number_picker_spinner_edit_text_height)) / ((float) dimensionPixelSize);
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.NumberPicker, i, i2);
        int dimensionPixelSize3 = obtainStyledAttributes.getDimensionPixelSize(R.styleable.NumberPicker_internalMinHeight, -1);
        this.mMinHeight = dimensionPixelSize3;
        int dimensionPixelSize4 = obtainStyledAttributes.getDimensionPixelSize(R.styleable.NumberPicker_internalMaxHeight, dimensionPixelSize);
        this.mMaxHeight = dimensionPixelSize4;
        int dimensionPixelSize5 = obtainStyledAttributes.getDimensionPixelSize(R.styleable.NumberPicker_internalMinWidth, dimensionPixelSize2);
        this.mMinWidth = dimensionPixelSize5;
        this.mMaxWidth = obtainStyledAttributes.getDimensionPixelSize(R.styleable.NumberPicker_internalMaxWidth, -1);
        obtainStyledAttributes.recycle();
        this.mValue = getCalendarForLocale(this.mValue, Locale.getDefault());
        this.mMinValue = getCalendarForLocale(this.mMinValue, Locale.getDefault());
        this.mMaxValue = getCalendarForLocale(this.mMaxValue, Locale.getDefault());
        TypedArray obtainStyledAttributes2 = context.obtainStyledAttributes(attributeSet, R.styleable.DatePicker, i, i2);
        this.mMinValue.set(obtainStyledAttributes2.getInt(R.styleable.DatePicker_android_startYear, 1902), 0, 1);
        this.mMaxValue.set(obtainStyledAttributes2.getInt(R.styleable.DatePicker_android_endYear, 2100), 11, 31);
        if (dimensionPixelSize3 != -1 && dimensionPixelSize4 != -1 && dimensionPixelSize3 > dimensionPixelSize4) {
            throw new IllegalArgumentException("minHeight > maxHeight");
        } else if (dimensionPixelSize5 == -1 || (i4 = this.mMaxWidth) == -1 || dimensionPixelSize5 <= i4) {
            this.mSelectionDividerHeight = (int) TypedValue.applyDimension(1, 2.0f, resources.getDisplayMetrics());
            this.mComputeMaxWidth = this.mMaxWidth == -1;
            TypedValue typedValue = new TypedValue();
            context.getTheme().resolveAttribute(R.attr.colorPrimaryDark, typedValue, true);
            int i5 = typedValue.resourceId;
            if (i5 != 0) {
                i3 = ResourcesCompat.getColor(resources, i5, null);
            } else {
                i3 = typedValue.data;
            }
            this.mVirtualButtonFocusedDrawable = new ColorDrawable((i3 & 16777215) | 855638016);
            if (!SeslMisc.isLightTheme(this.mContext)) {
                this.mIdleAlpha = 0.2f;
                this.mAlpha = 0.2f;
            }
            this.mPressedStateHelper = new PressedStateHelper();
            this.mDelegator.setWillNotDraw(false);
            ((LayoutInflater) this.mContext.getSystemService("layout_inflater")).inflate(R.layout.sesl_spinning_date_picker_spinner, (ViewGroup) this.mDelegator, true);
            EditText editText = (EditText) this.mDelegator.findViewById(R.id.datepicker_input);
            this.mInputText = editText;
            editText.setIncludeFontPadding(false);
            Typeface defaultFromStyle = Typeface.defaultFromStyle(1);
            this.mDefaultTypeface = defaultFromStyle;
            Typeface create = Typeface.create("sec-roboto-condensed-light", 1);
            this.mLegacyTypeface = create;
            Typeface create2 = Typeface.create("sec-roboto-light", 1);
            this.mPickerTypeface = create2;
            if (defaultFromStyle.equals(create2)) {
                if (!create.equals(this.mPickerTypeface)) {
                    this.mPickerTypeface = create;
                } else {
                    this.mPickerTypeface = Typeface.create("sans-serif-thin", 1);
                }
            }
            this.mPickerSubTypeface = Typeface.create(this.mPickerTypeface, 0);
            if (!SeslConfigurationReflector.isDexEnabled(resources.getConfiguration())) {
                String string = Settings.System.getString(this.mContext.getContentResolver(), "theme_font_clock");
                if (string != null && !string.isEmpty()) {
                    Typeface fontTypeface = getFontTypeface(string);
                    this.mPickerTypeface = fontTypeface;
                    this.mPickerSubTypeface = Typeface.create(fontTypeface, 0);
                }
            } else {
                this.mIdleAlpha = 0.2f;
                this.mAlpha = 0.2f;
            }
            if (isCharacterNumberLanguage()) {
                this.mPickerTypeface = defaultFromStyle;
                this.mPickerSubTypeface = Typeface.create(defaultFromStyle, 0);
            }
            this.mIsHcfEnabled = isHighContrastFontEnabled();
            this.mHcfFocusedTypefaceBold = Typeface.create(this.mPickerTypeface, 1);
            this.mHcfUnfocusedTextSizeDiff = (int) TypedValue.applyDimension(1, 2.0f, this.mContext.getResources().getDisplayMetrics());
            setInputTextTypeface();
            int colorForState = editText.getTextColors().getColorForState(this.mDelegator.getEnableStateSet(), -1);
            this.mTextColorIdle = colorForState;
            int color = ResourcesCompat.getColor(resources, R.color.sesl_number_picker_text_color_scroll, context.getTheme());
            this.mTextColorScrolling = color;
            this.mTextColor = colorForState;
            ViewConfiguration viewConfiguration = ViewConfiguration.get(context);
            this.mTouchSlop = viewConfiguration.getScaledTouchSlop();
            this.mMinimumFlingVelocity = viewConfiguration.getScaledMinimumFlingVelocity() * 2;
            this.mMaximumFlingVelocity = viewConfiguration.getScaledMaximumFlingVelocity() / 4;
            this.mTextSize = (int) editText.getTextSize();
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTextSize((float) this.mTextSize);
            paint.setTypeface(this.mPickerTypeface);
            paint.setColor(this.mTextColor);
            this.mSelectorWheelPaint = paint;
            this.mInitialAlpha = ((float) paint.getAlpha()) / 255.0f;
            this.mCustomScroller = new Scroller(this.mContext, pathInterpolator, true);
            Scroller scroller = new Scroller(this.mContext, null, true);
            this.mLinearScroller = scroller;
            this.mFlingScroller = scroller;
            this.mAdjustScroller = new Scroller(this.mContext, new PathInterpolator(0.4f, 0.0f, 0.3f, 1.0f));
            this.mGravityScroller = new OverScroller(this.mContext, new DecelerateInterpolator());
            this.mHolder = new FloatValueHolder();
            SpringAnimation springAnimation = new SpringAnimation(this.mHolder);
            this.mSpringAnimation = springAnimation;
            springAnimation.setSpring(new SpringForce());
            this.mSpringAnimation.setMinimumVisibleChange(1.0f);
            this.mSpringAnimation.addUpdateListener(this.mSpringAnimationUpdateListener);
            this.mSpringAnimation.addEndListener(this.mSpringAnimationEndListener);
            this.mSpringAnimation.getSpring().setStiffness(7.0f);
            this.mSpringAnimation.getSpring().setDampingRatio(0.99f);
            setFormatter(SeslSpinningDatePickerSpinner.getDateFormatter());
            this.mDelegator.setVerticalScrollBarEnabled(false);
            if (this.mDelegator.getImportantForAccessibility() == 0) {
                this.mDelegator.setImportantForAccessibility(1);
            }
            this.mAudioManager = (AudioManager) this.mContext.getSystemService("audio");
            this.mHapticPreDrawListener = new HapticPreDrawListener();
            this.mPickerVibrateIndex = SeslHapticFeedbackConstantsReflector.semGetVibrationIndex(32);
            this.mPickerSoundIndex = SeslAudioManagerReflector.getField_SOUND_TIME_PICKER_SCROLL();
            this.mDelegator.setFocusableInTouchMode(false);
            this.mDelegator.setDescendantFocusability(131072);
            if (Build.VERSION.SDK_INT >= 26) {
                this.mDelegator.setDefaultFocusHighlightEnabled(false);
            }
            this.mPickerContentDescription = "";
            SeslViewReflector.semSetDirectPenInputEnabled(editText, false);
            this.mAccessibilityManager = (AccessibilityManager) this.mContext.getSystemService("accessibility");
            ValueAnimator ofFloat = ValueAnimator.ofFloat(this.mActivatedAlpha, this.mIdleAlpha);
            this.mFadeOutAnimator = ofFloat;
            ofFloat.setInterpolator(pathInterpolator2);
            this.mFadeOutAnimator.setDuration(200L);
            this.mFadeOutAnimator.setStartDelay(100);
            this.mFadeOutAnimator.addUpdateListener(this.mUpdateListener);
            ValueAnimator ofFloat2 = ValueAnimator.ofFloat(this.mIdleAlpha, this.mActivatedAlpha);
            this.mFadeInAnimator = ofFloat2;
            ofFloat2.setInterpolator(pathInterpolator2);
            this.mFadeInAnimator.setDuration(200L);
            this.mFadeInAnimator.addUpdateListener(this.mUpdateListener);
            ValueAnimator ofObject = ValueAnimator.ofObject(new ArgbEvaluator(), Integer.valueOf(colorForState), Integer.valueOf(color));
            this.mColorInAnimator = ofObject;
            ofObject.setInterpolator(pathInterpolator2);
            this.mColorInAnimator.setDuration(200L);
            this.mColorInAnimator.addUpdateListener(this.mColorUpdateListener);
            ValueAnimator ofObject2 = ValueAnimator.ofObject(new ArgbEvaluator(), Integer.valueOf(color), Integer.valueOf(colorForState));
            this.mColorOutAnimator = ofObject2;
            ofObject2.setInterpolator(pathInterpolator2);
            this.mColorOutAnimator.setDuration(200L);
            this.mColorOutAnimator.setStartDelay(100);
            this.mColorOutAnimator.addUpdateListener(this.mColorUpdateListener);
            this.mShortMonths = new DateFormatSymbols().getShortMonths();
            this.mLongMonths = new DateFormatSymbols().getMonths();
        } else {
            throw new IllegalArgumentException("minWidth > maxWidth");
        }
    }

    @Override // androidx.picker.widget.SeslSpinningDatePickerSpinner.DatePickerDelegate
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        int measuredWidth = this.mDelegator.getMeasuredWidth();
        int measuredHeight = this.mDelegator.getMeasuredHeight();
        int measuredWidth2 = this.mInputText.getMeasuredWidth();
        int max = Math.max(this.mInputText.getMeasuredHeight(), (int) Math.floor((double) (((float) measuredHeight) * this.mHeightRatio)));
        this.mModifiedTxtHeight = max;
        int i5 = (measuredWidth - measuredWidth2) / 2;
        int i6 = (measuredHeight - max) / 2;
        int i7 = max + i6;
        this.mInputText.layout(i5, i6, measuredWidth2 + i5, i7);
        if (z) {
            initializeSelectorWheel();
            if (this.mModifiedTxtHeight > this.mSelectorElementHeight) {
                int i8 = this.mValueChangeOffset;
                this.mTopSelectionDividerTop = i8;
                this.mBottomSelectionDividerBottom = i8 * 2;
                return;
            }
            this.mTopSelectionDividerTop = i6;
            this.mBottomSelectionDividerBottom = i7;
        }
    }

    @Override // androidx.picker.widget.SeslSpinningDatePickerSpinner.DatePickerDelegate
    public void onMeasure(int i, int i2) {
        this.mDelegator.superOnMeasure(makeMeasureSpec(i, this.mMaxWidth), makeMeasureSpec(i2, this.mMaxHeight));
        this.mDelegator.setMeasuredDimensionWrapper(resolveSizeAndStateRespectingMinSize(this.mMinWidth, this.mDelegator.getMeasuredWidth(), i), resolveSizeAndStateRespectingMinSize(this.mMinHeight, this.mDelegator.getMeasuredHeight(), i2));
    }

    public final boolean moveToFinalScrollerPosition(Scroller scroller) {
        int i;
        scroller.forceFinished(true);
        int finalY = scroller.getFinalY() - scroller.getCurrY();
        int i2 = this.mSelectorElementHeight;
        if (i2 == 0 || (i = this.mInitialScrollOffset - (this.mCurrentScrollOffset + finalY)) == 0) {
            return false;
        }
        int i3 = i % i2;
        int abs = Math.abs(i3);
        int i4 = this.mSelectorElementHeight;
        if (abs > i4 / 2) {
            i3 = i3 > 0 ? i3 - i4 : i3 + i4;
        }
        scrollBy(0, finalY + i3);
        return true;
    }

    @Override // androidx.picker.widget.SeslSpinningDatePickerSpinner.DatePickerDelegate
    public void onWindowFocusChanged(boolean z) {
        if (!this.mIsStartingAnimation) {
            if (!this.mFlingScroller.isFinished()) {
                this.mFlingScroller.forceFinished(true);
            }
            if (!this.mAdjustScroller.isFinished()) {
                this.mAdjustScroller.forceFinished(true);
            }
            if (!this.mGravityScroller.isFinished()) {
                this.mGravityScroller.forceFinished(true);
            }
            if (this.mSpringAnimation.isRunning()) {
                this.mSpringAnimation.cancel();
                this.mSpringFlingRunning = false;
            }
            ensureScrollWheelAdjusted();
        }
        this.mIsHcfEnabled = isHighContrastFontEnabled();
        this.mSelectorWheelPaint.setTextSize((float) this.mTextSize);
        this.mSelectorWheelPaint.setTypeface(this.mPickerTypeface);
        setInputTextTypeface();
    }

    @Override // androidx.picker.widget.SeslSpinningDatePickerSpinner.DatePickerDelegate
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        if (!this.mDelegator.isEnabled() || this.mIsStartingAnimation || motionEvent.getActionMasked() != 0) {
            return false;
        }
        removeAllCallbacks();
        float y = motionEvent.getY();
        this.mLastDownEventY = y;
        this.mLastDownOrMoveEventY = y;
        this.mLastDownEventTime = motionEvent.getEventTime();
        this.mIgnoreMoveEvents = false;
        this.mPerformClickOnTap = false;
        this.mIsValueChanged = false;
        float f = this.mLastDownEventY;
        if (f < ((float) this.mTopSelectionDividerTop)) {
            startFadeAnimation(false);
            if (this.mScrollState == 0) {
                this.mPressedStateHelper.buttonPressDelayed(2);
            }
        } else if (f > ((float) this.mBottomSelectionDividerBottom)) {
            startFadeAnimation(false);
            if (this.mScrollState == 0) {
                this.mPressedStateHelper.buttonPressDelayed(1);
            }
        }
        this.mDelegator.getParent().requestDisallowInterceptTouchEvent(true);
        if (!this.mFlingScroller.isFinished()) {
            this.mFlingScroller.forceFinished(true);
            this.mAdjustScroller.forceFinished(true);
            if (this.mScrollState == 2) {
                this.mFlingScroller.abortAnimation();
                this.mAdjustScroller.abortAnimation();
            }
            onScrollStateChange(0);
        } else if (this.mSpringAnimation.isRunning()) {
            this.mGravityScroller.forceFinished(true);
            this.mAdjustScroller.forceFinished(true);
            this.mSpringAnimation.cancel();
            this.mSpringFlingRunning = false;
            if (this.mScrollState == 2) {
                this.mGravityScroller.abortAnimation();
                this.mAdjustScroller.abortAnimation();
            }
            onScrollStateChange(0);
        } else if (!this.mAdjustScroller.isFinished()) {
            this.mFlingScroller.forceFinished(true);
            this.mAdjustScroller.forceFinished(true);
        } else {
            float f2 = this.mLastDownEventY;
            if (f2 < ((float) this.mTopSelectionDividerTop)) {
                postChangeCurrentByOneFromLongPress(false, (long) ViewConfiguration.getLongPressTimeout());
            } else if (f2 > ((float) this.mBottomSelectionDividerBottom)) {
                postChangeCurrentByOneFromLongPress(true, (long) ViewConfiguration.getLongPressTimeout());
            } else {
                this.mPerformClickOnTap = true;
            }
        }
        return true;
    }

    @Override // androidx.picker.widget.SeslSpinningDatePickerSpinner.DatePickerDelegate
    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (!this.mDelegator.isEnabled() || this.mIsStartingAnimation) {
            return false;
        }
        if (this.mVelocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        }
        this.mVelocityTracker.addMovement(motionEvent);
        int actionMasked = motionEvent.getActionMasked();
        if (actionMasked == 1) {
            removeChangeCurrentByOneFromLongPress();
            this.mPressedStateHelper.cancel();
            VelocityTracker velocityTracker = this.mVelocityTracker;
            velocityTracker.computeCurrentVelocity(1000, (float) this.mMaximumFlingVelocity);
            int yVelocity = (int) velocityTracker.getYVelocity();
            int y = (int) motionEvent.getY();
            int abs = (int) Math.abs(((float) y) - this.mLastDownEventY);
            if (Math.abs(yVelocity) <= this.mMinimumFlingVelocity) {
                long eventTime = motionEvent.getEventTime() - this.mLastDownEventTime;
                if (abs > this.mTouchSlop || eventTime >= ((long) ViewConfiguration.getLongPressTimeout())) {
                    if (this.mIsLongClicked) {
                        this.mIsLongClicked = false;
                    }
                    ensureScrollWheelAdjusted(abs);
                    startFadeAnimation(true);
                } else if (this.mPerformClickOnTap) {
                    this.mPerformClickOnTap = false;
                    performClick();
                } else {
                    if (y > this.mBottomSelectionDividerBottom) {
                        changeValueByOne(true);
                        this.mPressedStateHelper.buttonTapped(1);
                    } else if (y < this.mTopSelectionDividerTop) {
                        changeValueByOne(false);
                        this.mPressedStateHelper.buttonTapped(2);
                    } else {
                        ensureScrollWheelAdjusted(abs);
                    }
                    startFadeAnimation(true);
                }
                this.mIsValueChanged = false;
                onScrollStateChange(0);
            } else if (abs > this.mTouchSlop || !this.mPerformClickOnTap) {
                fling(yVelocity);
                onScrollStateChange(2);
            } else {
                this.mPerformClickOnTap = false;
                performClick();
                onScrollStateChange(0);
            }
            this.mVelocityTracker.recycle();
            this.mVelocityTracker = null;
        } else if (actionMasked != 2) {
            if (actionMasked == 3) {
                ensureScrollWheelAdjusted();
                startFadeAnimation(true);
                onScrollStateChange(0);
            }
        } else if (!this.mIgnoreMoveEvents) {
            float y2 = motionEvent.getY();
            if (this.mScrollState == 1) {
                scrollBy(0, (int) (y2 - this.mLastDownOrMoveEventY));
                this.mDelegator.invalidate();
            } else if (((int) Math.abs(y2 - this.mLastDownEventY)) > this.mTouchSlop) {
                removeAllCallbacks();
                startFadeAnimation(false);
                onScrollStateChange(1);
            }
            this.mLastDownOrMoveEventY = y2;
        }
        return true;
    }

    @Override // androidx.picker.widget.SeslSpinningDatePickerSpinner.DatePickerDelegate
    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        int actionMasked = motionEvent.getActionMasked();
        if (actionMasked != 1 && actionMasked != 3) {
            return false;
        }
        removeAllCallbacks();
        return false;
    }

    @Override // androidx.picker.widget.SeslSpinningDatePickerSpinner.DatePickerDelegate
    public boolean onGenericMotionEvent(MotionEvent motionEvent) {
        boolean z = false;
        if (this.mDelegator.isEnabled() && !this.mIsStartingAnimation && (motionEvent.getSource() & 2) != 0 && motionEvent.getAction() == 8) {
            float axisValue = motionEvent.getAxisValue(9);
            if (axisValue != 0.0f) {
                startFadeAnimation(false);
                if (axisValue < 0.0f) {
                    z = true;
                }
                changeValueByOne(z);
                startFadeAnimation(true);
                return true;
            }
        }
        return false;
    }

    @Override // androidx.picker.widget.SeslSpinningDatePickerSpinner.DatePickerDelegate
    public void onConfigurationChanged(Configuration configuration) {
        if (!this.mCustomTypefaceSet) {
            if (isCharacterNumberLanguage()) {
                this.mInputText.setIncludeFontPadding(true);
                Typeface typeface = this.mDefaultTypeface;
                this.mPickerTypeface = typeface;
                this.mPickerSubTypeface = Typeface.create(typeface, 0);
                this.mHcfFocusedTypefaceBold = Typeface.create(this.mPickerTypeface, 1);
                setInputTextTypeface();
                return;
            }
            this.mInputText.setIncludeFontPadding(false);
            setInputTextTypeface();
            tryComputeMaxWidth();
        }
    }

    @Override // androidx.picker.widget.SeslSpinningDatePickerSpinner.DatePickerDelegate
    public void onFocusChanged(boolean z, int i, Rect rect) {
        AccessibilityNodeProviderImpl accessibilityNodeProviderImpl;
        AccessibilityNodeProviderImpl accessibilityNodeProviderImpl2;
        if (!z) {
            if (this.mAccessibilityManager.isEnabled() && (accessibilityNodeProviderImpl2 = (AccessibilityNodeProviderImpl) getAccessibilityNodeProvider()) != null) {
                accessibilityNodeProviderImpl2.performAction(this.mLastFocusedChildVirtualViewId, 128, null);
            }
            this.mLastFocusedChildVirtualViewId = -1;
            this.mLastHoveredChildVirtualViewId = Integer.MIN_VALUE;
        } else {
            InputMethodManager inputMethodManager = (InputMethodManager) this.mContext.getSystemService("input_method");
            if (inputMethodManager != null) {
                inputMethodManager.hideSoftInputFromWindow(this.mDelegator.getWindowToken(), 0);
            }
            this.mLastFocusedChildVirtualViewId = 1;
            if (!this.mWrapSelectorWheel && getValue().equals(getMinValue())) {
                this.mLastFocusedChildVirtualViewId = 2;
            }
            if (this.mAccessibilityManager.isEnabled() && (accessibilityNodeProviderImpl = (AccessibilityNodeProviderImpl) getAccessibilityNodeProvider()) != null) {
                accessibilityNodeProviderImpl.performAction(this.mLastFocusedChildVirtualViewId, 64, null);
            }
        }
        this.mDelegator.invalidate();
    }

    @Override // androidx.picker.widget.SeslSpinningDatePickerSpinner.DatePickerDelegate
    public boolean dispatchKeyEvent(KeyEvent keyEvent) {
        int action = keyEvent.getAction();
        int keyCode = keyEvent.getKeyCode();
        if (!(keyCode == 66 || keyCode == 160)) {
            switch (keyCode) {
                case 19:
                case 20:
                    if (action == 0) {
                        if (keyCode == 20) {
                            int i = this.mLastFocusedChildVirtualViewId;
                            if (i == 1) {
                                this.mLastFocusedChildVirtualViewId = 2;
                                this.mDelegator.invalidate();
                                return true;
                            } else if (i == 2) {
                                if (!this.mWrapSelectorWheel && getValue().equals(getMaxValue())) {
                                    return false;
                                }
                                this.mLastFocusedChildVirtualViewId = 3;
                                this.mDelegator.invalidate();
                                return true;
                            }
                        } else if (keyCode == 19) {
                            int i2 = this.mLastFocusedChildVirtualViewId;
                            if (i2 != 2) {
                                if (i2 == 3) {
                                    this.mLastFocusedChildVirtualViewId = 2;
                                    this.mDelegator.invalidate();
                                    return true;
                                }
                            } else if (!this.mWrapSelectorWheel && getValue().equals(getMinValue())) {
                                return false;
                            } else {
                                this.mLastFocusedChildVirtualViewId = 1;
                                this.mDelegator.invalidate();
                                return true;
                            }
                        }
                    } else if (action == 1 && this.mAccessibilityManager.isEnabled()) {
                        AccessibilityNodeProviderImpl accessibilityNodeProviderImpl = (AccessibilityNodeProviderImpl) getAccessibilityNodeProvider();
                        if (accessibilityNodeProviderImpl != null) {
                            accessibilityNodeProviderImpl.performAction(this.mLastFocusedChildVirtualViewId, 64, null);
                        }
                        return true;
                    }
                    break;
                case 21:
                case 22:
                    if (action == 0) {
                        if (keyCode == 21) {
                            View focusSearch = this.mDelegator.focusSearch(17);
                            if (focusSearch != null) {
                                focusSearch.requestFocus(17);
                            }
                            return true;
                        } else if (keyCode == 22) {
                            View focusSearch2 = this.mDelegator.focusSearch(66);
                            if (focusSearch2 != null) {
                                focusSearch2.requestFocus(66);
                            }
                            return true;
                        }
                    }
                    break;
            }
            return false;
        }
        if (action == 0) {
            if (this.mLastFocusedChildVirtualViewId == 2) {
                performClick();
                removeAllCallbacks();
            } else if (this.mFlingScroller.isFinished()) {
                int i3 = this.mLastFocusedChildVirtualViewId;
                if (i3 == 1) {
                    startFadeAnimation(false);
                    changeValueByOne(false);
                    Calendar calendar = (Calendar) getMinValue().clone();
                    calendar.add(5, 1);
                    if (!this.mWrapSelectorWheel && getValue().equals(calendar)) {
                        this.mLastFocusedChildVirtualViewId = 2;
                    }
                    startFadeAnimation(true);
                } else if (i3 == 3) {
                    startFadeAnimation(false);
                    changeValueByOne(true);
                    Calendar calendar2 = (Calendar) getMaxValue().clone();
                    calendar2.add(5, -1);
                    if (!this.mWrapSelectorWheel && getValue().equals(calendar2)) {
                        this.mLastFocusedChildVirtualViewId = 2;
                    }
                    startFadeAnimation(true);
                }
            }
        }
        return false;
    }

    @Override // androidx.picker.widget.SeslSpinningDatePickerSpinner.DatePickerDelegate
    public void dispatchTrackballEvent(MotionEvent motionEvent) {
        int actionMasked = motionEvent.getActionMasked();
        if (actionMasked == 1 || actionMasked == 3) {
            removeAllCallbacks();
        }
    }

    @Override // androidx.picker.widget.SeslSpinningDatePickerSpinner.DatePickerDelegate
    public boolean dispatchHoverEvent(MotionEvent motionEvent) {
        int i;
        if (!this.mAccessibilityManager.isEnabled()) {
            return false;
        }
        int y = (int) motionEvent.getY();
        if (y <= this.mTopSelectionDividerTop) {
            i = 1;
        } else {
            i = this.mBottomSelectionDividerBottom <= y ? 3 : 2;
        }
        int actionMasked = motionEvent.getActionMasked();
        if (actionMasked == 7 || actionMasked == 9) {
            updateHoveredVirtualView(i);
            if (i != Integer.MIN_VALUE) {
                return true;
            }
            return false;
        } else if (actionMasked != 10 || this.mLastHoveredChildVirtualViewId == Integer.MIN_VALUE) {
            return false;
        } else {
            updateHoveredVirtualView(Integer.MIN_VALUE);
            return true;
        }
    }

    public final void updateHoveredVirtualView(int i) {
        int i2 = this.mLastHoveredChildVirtualViewId;
        if (i2 != i) {
            this.mLastHoveredChildVirtualViewId = i;
            AccessibilityNodeProviderImpl accessibilityNodeProviderImpl = (AccessibilityNodeProviderImpl) getAccessibilityNodeProvider();
            accessibilityNodeProviderImpl.sendAccessibilityEventForVirtualView(i, 128);
            accessibilityNodeProviderImpl.sendAccessibilityEventForVirtualView(i2, 256);
        }
    }

    public final void playSoundAndHapticFeedback() {
        this.mAudioManager.playSoundEffect(this.mPickerSoundIndex);
        if (!this.mHapticPreDrawListener.mSkipHapticCalls) {
            this.mDelegator.performHapticFeedback(this.mPickerVibrateIndex);
            this.mHapticPreDrawListener.mSkipHapticCalls = true;
        }
    }

    public static class HapticPreDrawListener implements ViewTreeObserver.OnPreDrawListener {
        public boolean mSkipHapticCalls;

        public HapticPreDrawListener() {
            this.mSkipHapticCalls = false;
        }

        public boolean onPreDraw() {
            this.mSkipHapticCalls = false;
            return true;
        }
    }

    @Override // androidx.picker.widget.SeslSpinningDatePickerSpinner.DatePickerDelegate
    public void computeScroll() {
        if (!this.mSpringFlingRunning) {
            Scroller scroller = this.mFlingScroller;
            if (scroller.isFinished()) {
                scroller = this.mAdjustScroller;
                if (scroller.isFinished()) {
                    return;
                }
            }
            scroller.computeScrollOffset();
            int currY = scroller.getCurrY();
            if (this.mPreviousScrollerY == 0) {
                this.mPreviousScrollerY = scroller.getStartY();
            }
            scrollBy(0, currY - this.mPreviousScrollerY);
            this.mPreviousScrollerY = currY;
            if (scroller.isFinished()) {
                onScrollerFinished(scroller);
            } else {
                this.mDelegator.invalidate();
            }
        }
    }

    @Override // androidx.picker.widget.SeslSpinningDatePickerSpinner.DatePickerDelegate
    public void setEnabled(boolean z) {
        if (!z && this.mScrollState != 0) {
            stopScrollAnimation();
            onScrollStateChange(0);
        }
    }

    @Override // androidx.picker.widget.SeslSpinningDatePickerSpinner.DatePickerDelegate
    public void scrollBy(int i, int i2) {
        Calendar[] calendarArr = this.mSelectorIndices;
        if (i2 != 0 && this.mSelectorElementHeight > 0) {
            if (!this.mWrapSelectorWheel && this.mCurrentScrollOffset + i2 > this.mInitialScrollOffset && calendarArr[2].compareTo(this.mMinValue) <= 0) {
                stopFlingAnimation();
                i2 = this.mInitialScrollOffset - this.mCurrentScrollOffset;
            }
            if (!this.mWrapSelectorWheel && this.mCurrentScrollOffset + i2 < this.mInitialScrollOffset && calendarArr[2].compareTo(this.mMaxValue) >= 0) {
                stopFlingAnimation();
                i2 = this.mInitialScrollOffset - this.mCurrentScrollOffset;
            }
            this.mCurrentScrollOffset += i2;
            while (true) {
                int i3 = this.mCurrentScrollOffset;
                if (i3 - this.mInitialScrollOffset < this.mValueChangeOffset) {
                    break;
                }
                this.mCurrentScrollOffset = i3 - this.mSelectorElementHeight;
                decrementSelectorIndices(calendarArr);
                if (!this.mIsStartingAnimation) {
                    setValueInternal(calendarArr[2], true);
                    this.mIsValueChanged = true;
                    int i4 = this.mLongPressCount;
                    if (i4 > 0) {
                        this.mLongPressCount = i4 - 1;
                    } else {
                        playSoundAndHapticFeedback();
                    }
                }
                if (!this.mWrapSelectorWheel && calendarArr[2].compareTo(this.mMinValue) <= 0) {
                    this.mCurrentScrollOffset = this.mInitialScrollOffset;
                }
            }
            while (true) {
                int i5 = this.mCurrentScrollOffset;
                if (i5 - this.mInitialScrollOffset <= (-this.mValueChangeOffset)) {
                    this.mCurrentScrollOffset = i5 + this.mSelectorElementHeight;
                    incrementSelectorIndices(calendarArr);
                    if (!this.mIsStartingAnimation) {
                        setValueInternal(calendarArr[2], true);
                        this.mIsValueChanged = true;
                        int i6 = this.mLongPressCount;
                        if (i6 > 0) {
                            this.mLongPressCount = i6 - 1;
                        } else {
                            playSoundAndHapticFeedback();
                        }
                    }
                    if (!this.mWrapSelectorWheel && calendarArr[2].compareTo(this.mMaxValue) >= 0) {
                        this.mCurrentScrollOffset = this.mInitialScrollOffset;
                    }
                } else {
                    return;
                }
            }
        }
    }

    @Override // androidx.picker.widget.SeslSpinningDatePickerSpinner.DatePickerDelegate
    public int computeVerticalScrollOffset() {
        return this.mCurrentScrollOffset;
    }

    @Override // androidx.picker.widget.SeslSpinningDatePickerSpinner.DatePickerDelegate
    public int computeVerticalScrollRange() {
        return (((int) TimeUnit.MILLISECONDS.toDays(this.mMaxValue.getTimeInMillis() - this.mMinValue.getTimeInMillis())) + 1) * this.mSelectorElementHeight;
    }

    @Override // androidx.picker.widget.SeslSpinningDatePickerSpinner.DatePickerDelegate
    public int computeVerticalScrollExtent() {
        return this.mDelegator.getHeight();
    }

    public void setFormatter(SeslSpinningDatePickerSpinner.Formatter formatter) {
        if (formatter != this.mFormatter) {
            this.mFormatter = formatter;
            initializeSelectorWheelIndices();
        }
    }

    @Override // androidx.picker.widget.SeslSpinningDatePickerSpinner.DatePickerDelegate
    public void performClick() {
        SeslSpinningDatePicker$LunarDate seslSpinningDatePicker$LunarDate;
        stopScrollAnimation();
        if (this.mOnSpinnerDateClickListener != null) {
            Calendar calendar = null;
            if (this.mIsLunar) {
                SeslSpinningDatePicker$LunarDate seslSpinningDatePicker$LunarDate2 = new SeslSpinningDatePicker$LunarDate();
                calendar = convertSolarToLunar(this.mValue, seslSpinningDatePicker$LunarDate2);
                seslSpinningDatePicker$LunarDate = seslSpinningDatePicker$LunarDate2;
            } else {
                seslSpinningDatePicker$LunarDate = null;
            }
            SeslSpinningDatePickerSpinner.OnSpinnerDateClickListener onSpinnerDateClickListener = this.mOnSpinnerDateClickListener;
            if (!this.mIsLunar) {
                calendar = this.mValue;
            }
            onSpinnerDateClickListener.onSpinnerDateClicked(calendar, seslSpinningDatePicker$LunarDate);
        }
    }

    @Override // androidx.picker.widget.SeslSpinningDatePickerSpinner.DatePickerDelegate
    public void performLongClick() {
        this.mIgnoreMoveEvents = true;
        this.mIsLongClicked = true;
    }

    public final void tryComputeMaxWidth() {
        if (this.mComputeMaxWidth) {
            float f = 0.0f;
            float f2 = 0.0f;
            for (int i = 0; i <= 9; i++) {
                float measureText = this.mSelectorWheelPaint.measureText(formatNumberWithLocale(i));
                if (measureText > f2) {
                    f2 = measureText;
                }
            }
            float f3 = (float) ((int) (((float) 2) * f2));
            float f4 = 0.0f;
            for (String str : new android.icu.text.DateFormatSymbols(Locale.getDefault()).getShortWeekdays()) {
                float measureText2 = this.mSelectorWheelPaint.measureText(str);
                if (measureText2 > f4) {
                    f4 = measureText2;
                }
            }
            for (String str2 : new android.icu.text.DateFormatSymbols(Locale.getDefault()).getShortMonths()) {
                float measureText3 = this.mSelectorWheelPaint.measureText(str2);
                if (measureText3 > f) {
                    f = measureText3;
                }
            }
            int measureText4 = ((int) (f3 + f4 + f + (this.mSelectorWheelPaint.measureText(" ") * 2.0f) + this.mSelectorWheelPaint.measureText(","))) + this.mInputText.getPaddingLeft() + this.mInputText.getPaddingRight();
            if (isHighContrastFontEnabled()) {
                measureText4 += ((int) Math.ceil((double) (SeslPaintReflector.getHCTStrokeWidth(this.mSelectorWheelPaint) / 2.0f))) * 13;
            }
            if (this.mMaxWidth != measureText4) {
                int i2 = this.mMinWidth;
                if (measureText4 > i2) {
                    this.mMaxWidth = measureText4;
                } else {
                    this.mMaxWidth = i2;
                }
                this.mDelegator.invalidate();
            }
        }
    }

    public boolean getWrapSelectorWheel() {
        return this.mWrapSelectorWheel;
    }

    public Calendar getValue() {
        return this.mValue;
    }

    public Calendar getMinValue() {
        return this.mMinValue;
    }

    public Calendar getMaxValue() {
        return this.mMaxValue;
    }

    public final void setInputTextTypeface() {
        if (this.mIsHcfEnabled) {
            this.mInputText.setTypeface(this.mHcfFocusedTypefaceBold);
        } else {
            this.mInputText.setTypeface(this.mPickerTypeface);
        }
    }

    public static Typeface getFontTypeface(String str) {
        if (!new File(str).exists()) {
            return null;
        }
        try {
            return Typeface.createFromFile(str);
        } catch (Exception unused) {
            return null;
        }
    }

    public void startAnimation(final int i, SeslAnimationListener seslAnimationListener) {
        this.mAnimationListener = seslAnimationListener;
        this.mAlpha = this.mActivatedAlpha;
        this.mDelegator.post(new Runnable() {
            /* class androidx.picker.widget.SeslSpinningDatePickerSpinnerDelegate.AnonymousClass1 */

            public void run() {
                if (SeslSpinningDatePickerSpinnerDelegate.this.mSelectorElementHeight == 0) {
                    SeslSpinningDatePickerSpinnerDelegate.this.mReservedStartAnimation = true;
                    return;
                }
                SeslSpinningDatePickerSpinnerDelegate.this.mIsStartingAnimation = true;
                SeslSpinningDatePickerSpinnerDelegate seslSpinningDatePickerSpinnerDelegate = SeslSpinningDatePickerSpinnerDelegate.this;
                seslSpinningDatePickerSpinnerDelegate.mFlingScroller = seslSpinningDatePickerSpinnerDelegate.mCustomScroller;
                final int i = (int) (((double) SeslSpinningDatePickerSpinnerDelegate.this.mSelectorElementHeight) * 5.4d);
                SeslSpinningDatePickerSpinnerDelegate.this.scrollBy(0, SeslSpinningDatePickerSpinnerDelegate.this.mSelectorElementHeight * 5);
                SeslSpinningDatePickerSpinnerDelegate.this.mDelegator.invalidate();
                new Handler().postDelayed(new Runnable() {
                    /* class androidx.picker.widget.SeslSpinningDatePickerSpinnerDelegate.AnonymousClass1.AnonymousClass1 */

                    public void run() {
                        new Handler().postDelayed(new Runnable() {
                            /* class androidx.picker.widget.SeslSpinningDatePickerSpinnerDelegate.AnonymousClass1.AnonymousClass1.AnonymousClass1 */

                            public void run() {
                                SeslSpinningDatePickerSpinnerDelegate seslSpinningDatePickerSpinnerDelegate = SeslSpinningDatePickerSpinnerDelegate.this;
                                if (!seslSpinningDatePickerSpinnerDelegate.moveToFinalScrollerPosition(seslSpinningDatePickerSpinnerDelegate.mFlingScroller)) {
                                    SeslSpinningDatePickerSpinnerDelegate seslSpinningDatePickerSpinnerDelegate2 = SeslSpinningDatePickerSpinnerDelegate.this;
                                    seslSpinningDatePickerSpinnerDelegate2.moveToFinalScrollerPosition(seslSpinningDatePickerSpinnerDelegate2.mAdjustScroller);
                                }
                                SeslSpinningDatePickerSpinnerDelegate.this.mPreviousScrollerY = 0;
                                SeslSpinningDatePickerSpinnerDelegate.this.mFlingScroller.startScroll(0, 0, 0, -i, 557);
                                SeslSpinningDatePickerSpinnerDelegate.this.mDelegator.invalidate();
                                new Handler().postDelayed(new Runnable() {
                                    /* class androidx.picker.widget.SeslSpinningDatePickerSpinnerDelegate.AnonymousClass1.AnonymousClass1.AnonymousClass1.AnonymousClass1 */

                                    public void run() {
                                        SeslSpinningDatePickerSpinnerDelegate seslSpinningDatePickerSpinnerDelegate = SeslSpinningDatePickerSpinnerDelegate.this;
                                        seslSpinningDatePickerSpinnerDelegate.moveToFinalScrollerPosition(seslSpinningDatePickerSpinnerDelegate.mFlingScroller);
                                        SeslSpinningDatePickerSpinnerDelegate.this.mFlingScroller.abortAnimation();
                                        SeslSpinningDatePickerSpinnerDelegate.this.mAdjustScroller.abortAnimation();
                                        SeslSpinningDatePickerSpinnerDelegate.this.ensureScrollWheelAdjusted();
                                        SeslSpinningDatePickerSpinnerDelegate seslSpinningDatePickerSpinnerDelegate2 = SeslSpinningDatePickerSpinnerDelegate.this;
                                        seslSpinningDatePickerSpinnerDelegate2.mFlingScroller = seslSpinningDatePickerSpinnerDelegate2.mLinearScroller;
                                        SeslSpinningDatePickerSpinnerDelegate.this.mIsStartingAnimation = false;
                                        SeslSpinningDatePickerSpinnerDelegate.this.mDelegator.invalidate();
                                        SeslSpinningDatePickerSpinnerDelegate.this.startFadeAnimation(true);
                                        if (SeslSpinningDatePickerSpinnerDelegate.this.mAnimationListener != null) {
                                            SeslSpinningDatePickerSpinnerDelegate.this.mAnimationListener.onAnimationEnd();
                                        }
                                    }
                                }, 857);
                            }
                        }, 100);
                    }
                }, (long) i);
            }
        });
    }

    public final void stopScrollAnimation() {
        this.mFlingScroller.abortAnimation();
        this.mAdjustScroller.abortAnimation();
        this.mGravityScroller.abortAnimation();
        this.mSpringAnimation.cancel();
        this.mSpringFlingRunning = false;
        if (!this.mIsStartingAnimation && !moveToFinalScrollerPosition(this.mFlingScroller)) {
            moveToFinalScrollerPosition(this.mAdjustScroller);
        }
        ensureScrollWheelAdjusted();
    }

    public final void stopFlingAnimation() {
        this.mFlingScroller.abortAnimation();
        this.mAdjustScroller.abortAnimation();
        this.mGravityScroller.abortAnimation();
        this.mSpringAnimation.cancel();
        this.mSpringFlingRunning = false;
    }

    public final void startFadeAnimation(boolean z) {
        int i = 0;
        if (z) {
            this.mFadeOutAnimator.setStartDelay((long) (this.mFlingScroller.getDuration() + 100));
            ValueAnimator valueAnimator = this.mColorOutAnimator;
            if (!this.mFlingScroller.isFinished()) {
                i = this.mFlingScroller.getDuration();
            }
            valueAnimator.setStartDelay((long) (i + 100));
            this.mColorOutAnimator.start();
            this.mFadeOutAnimator.start();
            return;
        }
        this.mFadeInAnimator.setFloatValues(this.mAlpha, this.mActivatedAlpha);
        this.mColorInAnimator.setIntValues(this.mTextColor, this.mTextColorScrolling);
        this.mColorOutAnimator.cancel();
        this.mFadeOutAnimator.cancel();
        this.mColorInAnimator.start();
        this.mFadeInAnimator.start();
    }

    @Override // androidx.picker.widget.SeslSpinningDatePickerSpinner.DatePickerDelegate
    public void onDetachedFromWindow() {
        this.mGravityScroller.abortAnimation();
        this.mSpringAnimation.cancel();
        this.mSpringFlingRunning = false;
        removeAllCallbacks();
        this.mDelegator.getViewTreeObserver().removeOnPreDrawListener(this.mHapticPreDrawListener);
    }

    @Override // androidx.picker.widget.SeslSpinningDatePickerSpinner.DatePickerDelegate
    public void onAttachedToWindow() {
        this.mDelegator.getViewTreeObserver().addOnPreDrawListener(this.mHapticPreDrawListener);
    }

    @Override // androidx.picker.widget.SeslSpinningDatePickerSpinner.DatePickerDelegate
    public void onDraw(Canvas canvas) {
        boolean z;
        int right = this.mDelegator.getRight();
        int left = this.mDelegator.getLeft();
        int bottom = this.mDelegator.getBottom();
        float f = ((float) (right - left)) / 2.0f;
        float f2 = (float) (this.mCurrentScrollOffset - this.mSelectorElementHeight);
        Drawable drawable = this.mVirtualButtonFocusedDrawable;
        if (drawable != null && this.mScrollState == 0) {
            int i = this.mLastFocusedChildVirtualViewId;
            if (i == 1) {
                drawable.setState(this.mDelegator.getDrawableState());
                this.mVirtualButtonFocusedDrawable.setBounds(0, 0, right, this.mTopSelectionDividerTop);
                this.mVirtualButtonFocusedDrawable.draw(canvas);
            } else if (i == 2) {
                drawable.setState(this.mDelegator.getDrawableState());
                this.mVirtualButtonFocusedDrawable.setBounds(0, this.mTopSelectionDividerTop, right, this.mBottomSelectionDividerBottom);
                this.mVirtualButtonFocusedDrawable.draw(canvas);
            } else if (i == 3) {
                drawable.setState(this.mDelegator.getDrawableState());
                this.mVirtualButtonFocusedDrawable.setBounds(0, this.mBottomSelectionDividerBottom, right, bottom);
                this.mVirtualButtonFocusedDrawable.draw(canvas);
            }
        }
        for (Calendar calendar : this.mSelectorIndices) {
            String str = this.mSelectorIndexToStringCache.get(calendar);
            float f3 = this.mAlpha;
            float f4 = this.mIdleAlpha;
            if (f3 < f4) {
                f3 = f4;
            }
            int descent = (int) ((((this.mSelectorWheelPaint.descent() - this.mSelectorWheelPaint.ascent()) / 2.0f) + f2) - this.mSelectorWheelPaint.descent());
            int i2 = this.mTopSelectionDividerTop;
            int i3 = this.mInitialScrollOffset;
            if (f2 >= ((float) (i2 - i3))) {
                int i4 = this.mBottomSelectionDividerBottom;
                if (f2 <= ((float) (i3 + i4))) {
                    if (f2 <= ((float) (i2 + i4)) / 2.0f) {
                        canvas.save();
                        canvas.clipRect(0, this.mTopSelectionDividerTop, right, this.mBottomSelectionDividerBottom);
                        this.mSelectorWheelPaint.setColor(this.mTextColor);
                        this.mSelectorWheelPaint.setTypeface(this.mPickerTypeface);
                        float f5 = (float) descent;
                        canvas.drawText(str, f, f5, this.mSelectorWheelPaint);
                        canvas.restore();
                        canvas.save();
                        canvas.clipRect(0, 0, right, this.mTopSelectionDividerTop);
                        this.mSelectorWheelPaint.setTypeface(this.mPickerSubTypeface);
                        this.mSelectorWheelPaint.setAlpha((int) (f3 * 255.0f * this.mInitialAlpha));
                        canvas.drawText(str, f, f5, this.mSelectorWheelPaint);
                        canvas.restore();
                        z = false;
                    } else {
                        canvas.save();
                        canvas.clipRect(0, this.mTopSelectionDividerTop, right, this.mBottomSelectionDividerBottom);
                        this.mSelectorWheelPaint.setTypeface(this.mPickerTypeface);
                        this.mSelectorWheelPaint.setColor(this.mTextColor);
                        float f6 = (float) descent;
                        canvas.drawText(str, f, f6, this.mSelectorWheelPaint);
                        canvas.restore();
                        canvas.save();
                        z = false;
                        canvas.clipRect(0, this.mBottomSelectionDividerBottom, right, bottom);
                        this.mSelectorWheelPaint.setAlpha((int) (f3 * 255.0f * this.mInitialAlpha));
                        this.mSelectorWheelPaint.setTypeface(this.mPickerSubTypeface);
                        canvas.drawText(str, f, f6, this.mSelectorWheelPaint);
                        canvas.restore();
                    }
                    f2 += (float) this.mSelectorElementHeight;
                }
            }
            z = false;
            canvas.save();
            this.mSelectorWheelPaint.setAlpha((int) (f3 * 255.0f * this.mInitialAlpha));
            this.mSelectorWheelPaint.setTypeface(this.mPickerSubTypeface);
            canvas.drawText(str, f, (float) descent, this.mSelectorWheelPaint);
            canvas.restore();
            f2 += (float) this.mSelectorElementHeight;
        }
    }

    @Override // androidx.picker.widget.SeslSpinningDatePickerSpinner.DatePickerDelegate
    public void onInitializeAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        accessibilityEvent.setClassName(SeslSpinningDatePickerSpinner.class.getName());
        accessibilityEvent.setScrollable(true);
        TimeUnit timeUnit = TimeUnit.MILLISECONDS;
        accessibilityEvent.setScrollY(((int) timeUnit.toDays(this.mValue.getTimeInMillis() - this.mMinValue.getTimeInMillis())) * this.mSelectorElementHeight);
        accessibilityEvent.setMaxScrollY(((int) timeUnit.toDays(this.mMaxValue.getTimeInMillis() - this.mMinValue.getTimeInMillis())) * this.mSelectorElementHeight);
    }

    @Override // androidx.picker.widget.SeslSpinningDatePickerSpinner.DatePickerDelegate
    public void onPopulateAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        accessibilityEvent.getText().add(((AccessibilityNodeProviderImpl) getAccessibilityNodeProvider()).getVirtualCurrentButtonText());
    }

    @Override // androidx.picker.widget.SeslSpinningDatePickerSpinner.DatePickerDelegate
    public AccessibilityNodeProvider getAccessibilityNodeProvider() {
        if (this.mAccessibilityNodeProvider == null) {
            this.mAccessibilityNodeProvider = new AccessibilityNodeProviderImpl();
        }
        return this.mAccessibilityNodeProvider;
    }

    public final int makeMeasureSpec(int i, int i2) {
        if (i2 == -1) {
            return i;
        }
        int size = View.MeasureSpec.getSize(i);
        int mode = View.MeasureSpec.getMode(i);
        if (mode == Integer.MIN_VALUE) {
            return View.MeasureSpec.makeMeasureSpec(Math.min(size, i2), 1073741824);
        }
        if (mode == 0) {
            return View.MeasureSpec.makeMeasureSpec(i2, 1073741824);
        }
        if (mode == 1073741824) {
            return i;
        }
        throw new IllegalArgumentException("Unknown measure mode: " + mode);
    }

    public final int resolveSizeAndStateRespectingMinSize(int i, int i2, int i3) {
        return i != -1 ? View.resolveSizeAndState(Math.max(i, i2), i3, 0) : i2;
    }

    public final void initializeSelectorWheelIndices() {
        this.mSelectorIndexToStringCache.clear();
        Calendar[] calendarArr = this.mSelectorIndices;
        Calendar value = getValue();
        for (int i = 0; i < this.mSelectorIndices.length; i++) {
            Calendar calendar = (Calendar) value.clone();
            calendar.add(5, i - 2);
            if (this.mWrapSelectorWheel) {
                calendar = getWrappedSelectorIndex(calendar);
            }
            calendarArr[i] = calendar;
            ensureCachedScrollSelectorValue(calendarArr[i]);
        }
    }

    public final void setValueInternal(Calendar calendar, boolean z) {
        Calendar calendar2;
        if (this.mWrapSelectorWheel) {
            calendar2 = getWrappedSelectorIndex(calendar);
        } else {
            int compareTo = calendar.compareTo(this.mMinValue);
            Calendar calendar3 = calendar;
            if (compareTo < 0) {
                calendar3 = (Calendar) this.mMinValue.clone();
            }
            Calendar calendar4 = calendar3;
            int compareTo2 = calendar4.compareTo(this.mMaxValue);
            Calendar calendar5 = calendar4;
            if (compareTo2 > 0) {
                calendar5 = (Calendar) this.mMaxValue.clone();
            }
            calendar2 = calendar5;
        }
        Calendar calendar6 = (Calendar) this.mValue.clone();
        clearCalendar(this.mValue, calendar2);
        if (z) {
            notifyChange(calendar6);
        }
        initializeSelectorWheelIndices();
        this.mDelegator.invalidate();
    }

    public final void changeValueByOne(boolean z) {
        if (!moveToFinalScrollerPosition(this.mFlingScroller)) {
            moveToFinalScrollerPosition(this.mAdjustScroller);
        }
        this.mPreviousScrollerY = 0;
        this.mChangeValueBy = 1;
        if (this.mLongPressed_FIRST_SCROLL) {
            this.mLongPressed_FIRST_SCROLL = false;
            this.mLongPressed_SECOND_SCROLL = true;
        } else if (this.mLongPressed_SECOND_SCROLL) {
            this.mLongPressed_SECOND_SCROLL = false;
            this.mLongPressed_THIRD_SCROLL = true;
            if (getValue().get(5) % 10 == 0) {
                this.mChangeValueBy = 10;
            } else if (z) {
                this.mChangeValueBy = 10 - (getValue().get(5) % 10);
            } else {
                this.mChangeValueBy = getValue().get(5) % 10;
            }
        } else if (this.mLongPressed_THIRD_SCROLL) {
            this.mChangeValueBy = 10;
        }
        int i = 500;
        boolean z2 = this.mIsLongPressed;
        if (z2 && this.mSkipNumbers) {
            i = 200;
            this.mLongPressUpdateInterval = 600;
        } else if (z2) {
            i = 100;
            this.mChangeValueBy = 1;
            this.mLongPressUpdateInterval = 300;
        }
        int i2 = this.mChangeValueBy;
        this.mLongPressCount = i2 - 1;
        if (z) {
            this.mFlingScroller.startScroll(0, 0, 0, (-this.mSelectorElementHeight) * i2, i);
        } else {
            this.mFlingScroller.startScroll(0, 0, 0, this.mSelectorElementHeight * i2, i);
        }
        this.mDelegator.invalidate();
    }

    public final void initializeSelectorWheel() {
        if (this.mIsStartingAnimation) {
            if (!moveToFinalScrollerPosition(this.mFlingScroller)) {
                moveToFinalScrollerPosition(this.mAdjustScroller);
            }
            stopScrollAnimation();
        }
        if (!this.mIsStartingAnimation) {
            initializeSelectorWheelIndices();
        }
        int bottom = (int) ((((float) ((this.mDelegator.getBottom() - this.mDelegator.getTop()) - (this.mTextSize * 3))) / 3.0f) + 0.5f);
        this.mSelectorTextGapHeight = bottom;
        int i = this.mTextSize + bottom;
        this.mSelectorElementHeight = i;
        int i2 = this.mModifiedTxtHeight;
        if (i2 > i) {
            i2 = this.mDelegator.getHeight() / 3;
        }
        this.mValueChangeOffset = i2;
        int top = (this.mInputText.getTop() + (this.mModifiedTxtHeight / 2)) - this.mSelectorElementHeight;
        this.mInitialScrollOffset = top;
        this.mCurrentScrollOffset = top;
        ((SeslSpinningDatePickerSpinner.CustomEditText) this.mInputText).setEditTextPosition(((int) (((this.mSelectorWheelPaint.descent() - this.mSelectorWheelPaint.ascent()) / 2.0f) - this.mSelectorWheelPaint.descent())) - (this.mInputText.getBaseline() - (this.mModifiedTxtHeight / 2)));
        if (this.mReservedStartAnimation) {
            startAnimation(0, this.mAnimationListener);
            this.mReservedStartAnimation = false;
        }
    }

    public final void onScrollerFinished(Scroller scroller) {
        if (scroller == this.mFlingScroller) {
            onScrollStateChange(0);
        }
    }

    public final void onScrollStateChange(int i) {
        if (this.mScrollState != i) {
            this.mScrollState = i;
            SeslSpinningDatePickerSpinner.OnScrollListener onScrollListener = this.mOnScrollListener;
            if (onScrollListener != null) {
                onScrollListener.onScrollStateChange(this.mDelegator, i);
            }
        }
    }

    public final void fling(int i) {
        int i2;
        if (!this.mWrapSelectorWheel && i > 0 && getValue().equals(getMinValue())) {
            startFadeAnimation(true);
        } else if (this.mWrapSelectorWheel || i >= 0 || !getValue().equals(getMaxValue())) {
            this.mPreviousScrollerY = 0;
            float f = (float) i;
            Math.round((((float) Math.abs(i)) / ((float) this.mMaximumFlingVelocity)) * f);
            this.mPreviousSpringY = (float) this.mCurrentScrollOffset;
            this.mSpringAnimation.setStartVelocity(f);
            this.mGravityScroller.forceFinished(true);
            this.mGravityScroller.fling(0, this.mCurrentScrollOffset, 0, i, 0, 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
            int round = Math.round(((float) (this.mGravityScroller.getFinalY() + this.mCurrentScrollOffset)) / ((float) this.mSelectorElementHeight));
            int i3 = this.mSelectorElementHeight;
            int i4 = this.mInitialScrollOffset;
            int i5 = (round * i3) + i4;
            if (i > 0) {
                i2 = Math.max(i5, i3 + i4);
            } else {
                i2 = Math.min(i5, (-i3) + i4);
            }
            this.mSpringAnimation.setStartValue((float) this.mCurrentScrollOffset);
            this.mSpringFlingRunning = true;
            this.mSpringAnimation.animateToFinalPosition((float) i2);
            this.mDelegator.invalidate();
        } else {
            startFadeAnimation(true);
        }
    }

    public final Calendar getWrappedSelectorIndex(Calendar calendar) {
        if (calendar.compareTo(this.mMaxValue) > 0) {
            Calendar calendar2 = (Calendar) this.mMinValue.clone();
            TimeUnit timeUnit = TimeUnit.MILLISECONDS;
            calendar2.add(5, ((int) timeUnit.toDays(calendar.getTimeInMillis() - this.mMinValue.getTimeInMillis())) % (((int) timeUnit.toDays(this.mMaxValue.getTimeInMillis() - this.mMinValue.getTimeInMillis())) + 1));
            return calendar2;
        } else if (calendar.compareTo(this.mMinValue) >= 0) {
            return calendar;
        } else {
            Calendar calendar3 = (Calendar) this.mMaxValue.clone();
            TimeUnit timeUnit2 = TimeUnit.MILLISECONDS;
            calendar3.add(5, -(((int) timeUnit2.toDays(this.mMaxValue.getTimeInMillis() - calendar.getTimeInMillis())) % (((int) timeUnit2.toDays(this.mMaxValue.getTimeInMillis() - this.mMinValue.getTimeInMillis())) + 1)));
            return calendar3;
        }
    }

    public final void incrementSelectorIndices(Calendar[] calendarArr) {
        System.arraycopy(calendarArr, 1, calendarArr, 0, calendarArr.length - 1);
        Calendar calendar = (Calendar) calendarArr[calendarArr.length - 2].clone();
        calendar.add(5, 1);
        if (this.mWrapSelectorWheel && calendar.compareTo(this.mMaxValue) > 0) {
            clearCalendar(calendar, this.mMinValue);
        }
        calendarArr[calendarArr.length - 1] = calendar;
        ensureCachedScrollSelectorValue(calendar);
    }

    public final void decrementSelectorIndices(Calendar[] calendarArr) {
        System.arraycopy(calendarArr, 0, calendarArr, 1, calendarArr.length - 1);
        Calendar calendar = (Calendar) calendarArr[1].clone();
        calendar.add(5, -1);
        if (this.mWrapSelectorWheel && calendar.compareTo(this.mMinValue) < 0) {
            clearCalendar(calendar, this.mMaxValue);
        }
        calendarArr[0] = calendar;
        ensureCachedScrollSelectorValue(calendar);
    }

    public final void ensureCachedScrollSelectorValue(Calendar calendar) {
        HashMap<Calendar, String> hashMap = this.mSelectorIndexToStringCache;
        if (hashMap.get(calendar) == null) {
            hashMap.put(calendar, (calendar.compareTo(this.mMinValue) < 0 || calendar.compareTo(this.mMaxValue) > 0) ? "" : this.mIsLunar ? formatDateForLunar(calendar) : formatDate(calendar));
        }
    }

    public final String formatDateForLunar(Calendar calendar) {
        String str;
        Calendar calendar2 = (Calendar) calendar.clone();
        SeslSpinningDatePicker$LunarDate seslSpinningDatePicker$LunarDate = new SeslSpinningDatePicker$LunarDate();
        convertSolarToLunar(calendar, seslSpinningDatePicker$LunarDate);
        SeslSpinningDatePickerSpinner.Formatter formatter = this.mFormatter;
        if (formatter == null) {
            str = formatDateWithLocale(calendar2);
        } else if (formatter instanceof SeslSpinningDatePickerSpinner.DateFormatter) {
            str = ((SeslSpinningDatePickerSpinner.DateFormatter) formatter).format(calendar2, this.mContext);
        } else {
            str = formatter.format(calendar2);
        }
        String dayWithLocale = getDayWithLocale(seslSpinningDatePicker$LunarDate.day);
        String formatDayWithLocale = formatDayWithLocale(calendar2);
        String monthWithLocale = getMonthWithLocale(seslSpinningDatePicker$LunarDate.month);
        String formatMonthWithLocale = formatMonthWithLocale(calendar2);
        StringBuilder sb = new StringBuilder(str);
        int lastIndexOf = sb.lastIndexOf(formatDayWithLocale);
        if (lastIndexOf != -1) {
            sb.replace(lastIndexOf, formatDayWithLocale.length() + lastIndexOf, dayWithLocale);
        }
        int lastIndexOf2 = sb.lastIndexOf(formatMonthWithLocale);
        if (lastIndexOf2 != -1) {
            sb.replace(lastIndexOf2, formatMonthWithLocale.length() + lastIndexOf2, monthWithLocale);
        }
        return sb.toString();
    }

    public final String formatDateForLunarForAccessibility(Calendar calendar) {
        String str;
        Calendar calendar2 = (Calendar) calendar.clone();
        SeslSpinningDatePicker$LunarDate seslSpinningDatePicker$LunarDate = new SeslSpinningDatePicker$LunarDate();
        convertSolarToLunar(calendar, seslSpinningDatePicker$LunarDate);
        SeslSpinningDatePickerSpinner.Formatter formatter = this.mFormatter;
        if (formatter == null) {
            str = formatDateWithLocaleForAccessibility(calendar2);
        } else if (formatter instanceof SeslSpinningDatePickerSpinner.DateFormatter) {
            str = ((SeslSpinningDatePickerSpinner.DateFormatter) formatter).formatForAccessibility(calendar2, this.mContext);
        } else {
            str = formatter.format(calendar2);
        }
        String dayWithLocale = getDayWithLocale(seslSpinningDatePicker$LunarDate.day);
        String formatDayWithLocale = formatDayWithLocale(calendar2);
        String monthWithLocaleForAccessibility = getMonthWithLocaleForAccessibility(seslSpinningDatePicker$LunarDate.month);
        String formatMonthWithLocaleForAccessibility = formatMonthWithLocaleForAccessibility(calendar2);
        StringBuilder sb = new StringBuilder(str);
        int lastIndexOf = sb.lastIndexOf(formatDayWithLocale);
        if (lastIndexOf != -1) {
            sb.replace(lastIndexOf, formatDayWithLocale.length() + lastIndexOf, dayWithLocale);
        }
        int lastIndexOf2 = sb.lastIndexOf(formatMonthWithLocaleForAccessibility);
        if (lastIndexOf2 != -1) {
            sb.replace(lastIndexOf2, formatMonthWithLocaleForAccessibility.length() + lastIndexOf2, monthWithLocaleForAccessibility);
        }
        return sb.toString();
    }

    public final String formatDate(Calendar calendar) {
        SeslSpinningDatePickerSpinner.Formatter formatter = this.mFormatter;
        if (formatter == null) {
            return formatDateWithLocale(calendar);
        }
        if (formatter instanceof SeslSpinningDatePickerSpinner.DateFormatter) {
            return ((SeslSpinningDatePickerSpinner.DateFormatter) formatter).format(calendar, this.mContext);
        }
        return formatter.format(calendar);
    }

    public final String formatDateForAccessibility(Calendar calendar) {
        SeslSpinningDatePickerSpinner.Formatter formatter = this.mFormatter;
        if (formatter == null) {
            return formatDateWithLocale(calendar);
        }
        if (formatter instanceof SeslSpinningDatePickerSpinner.DateFormatter) {
            return ((SeslSpinningDatePickerSpinner.DateFormatter) formatter).formatForAccessibility(calendar, this.mContext);
        }
        return formatter.format(calendar);
    }

    public final void notifyChange(Calendar calendar) {
        if (this.mAccessibilityManager.isEnabled() && !this.mIsStartingAnimation) {
            Calendar wrappedSelectorIndex = getWrappedSelectorIndex(this.mValue);
            if (wrappedSelectorIndex.compareTo(this.mMaxValue) <= 0) {
                if (this.mIsLunar) {
                    formatDateForLunarForAccessibility(wrappedSelectorIndex);
                } else {
                    formatDateForAccessibility(wrappedSelectorIndex);
                }
            }
            this.mDelegator.sendAccessibilityEvent(4);
        }
        SeslSpinningDatePickerSpinner.OnValueChangeListener onValueChangeListener = this.mOnValueChangeListener;
        if (onValueChangeListener == null) {
            return;
        }
        if (this.mIsLunar) {
            SeslSpinningDatePicker$LunarDate seslSpinningDatePicker$LunarDate = new SeslSpinningDatePicker$LunarDate();
            this.mOnValueChangeListener.onValueChange(this.mDelegator, convertSolarToLunar(calendar, null), convertSolarToLunar(this.mValue, seslSpinningDatePicker$LunarDate), seslSpinningDatePicker$LunarDate.isLeapMonth, seslSpinningDatePicker$LunarDate);
            return;
        }
        onValueChangeListener.onValueChange(this.mDelegator, calendar, this.mValue, false, null);
    }

    public final void postChangeCurrentByOneFromLongPress(boolean z, long j) {
        ChangeCurrentByOneFromLongPressCommand changeCurrentByOneFromLongPressCommand = this.mChangeCurrentByOneFromLongPressCommand;
        if (changeCurrentByOneFromLongPressCommand == null) {
            this.mChangeCurrentByOneFromLongPressCommand = new ChangeCurrentByOneFromLongPressCommand();
        } else {
            this.mDelegator.removeCallbacks(changeCurrentByOneFromLongPressCommand);
        }
        this.mIsLongPressed = true;
        this.mLongPressed_FIRST_SCROLL = true;
        this.mChangeCurrentByOneFromLongPressCommand.setStep(z);
        this.mDelegator.postDelayed(this.mChangeCurrentByOneFromLongPressCommand, j);
    }

    public final void removeChangeCurrentByOneFromLongPress() {
        if (this.mIsLongPressed) {
            this.mIsLongPressed = false;
            this.mCurrentScrollOffset = this.mInitialScrollOffset;
        }
        this.mLongPressed_FIRST_SCROLL = false;
        this.mLongPressed_SECOND_SCROLL = false;
        this.mLongPressed_THIRD_SCROLL = false;
        this.mChangeValueBy = 1;
        this.mLongPressUpdateInterval = 300;
        ChangeCurrentByOneFromLongPressCommand changeCurrentByOneFromLongPressCommand = this.mChangeCurrentByOneFromLongPressCommand;
        if (changeCurrentByOneFromLongPressCommand != null) {
            this.mDelegator.removeCallbacks(changeCurrentByOneFromLongPressCommand);
        }
    }

    public final void removeAllCallbacks() {
        if (this.mIsLongPressed) {
            this.mIsLongPressed = false;
            this.mCurrentScrollOffset = this.mInitialScrollOffset;
        }
        this.mLongPressed_FIRST_SCROLL = false;
        this.mLongPressed_SECOND_SCROLL = false;
        this.mLongPressed_THIRD_SCROLL = false;
        this.mChangeValueBy = 1;
        this.mLongPressUpdateInterval = 300;
        ChangeCurrentByOneFromLongPressCommand changeCurrentByOneFromLongPressCommand = this.mChangeCurrentByOneFromLongPressCommand;
        if (changeCurrentByOneFromLongPressCommand != null) {
            this.mDelegator.removeCallbacks(changeCurrentByOneFromLongPressCommand);
        }
        this.mPressedStateHelper.cancel();
    }

    public final boolean ensureScrollWheelAdjusted() {
        return ensureScrollWheelAdjusted(0);
    }

    public final boolean ensureScrollWheelAdjusted(int var1) {
        int var2 = this.mInitialScrollOffset;
        if (var2 == -2147483648) {
            return false;
        } else {
            int var3 = var2 - this.mCurrentScrollOffset;
            if (var3 == 0) {
                this.mIsValueChanged = false;
                return false;
            } else {
                label31:
                {
                    label30:
                    {
                        label37:
                        {
                            this.mPreviousScrollerY = 0;
                            if (!this.mIsValueChanged && var1 != 0) {
                                var1 = Math.abs(var1);
                                var2 = this.mSelectorElementHeight;
                                if (var1 < var2) {
                                    var1 = var2;
                                    if (var3 <= 0) {
                                        break label30;
                                    }
                                    break label37;
                                }
                            }

                            int var4 = Math.abs(var3);
                            var2 = this.mSelectorElementHeight;
                            var1 = var3;
                            if (var4 <= var2 / 2) {
                                break label31;
                            }

                            var1 = var2;
                            if (var3 <= 0) {
                                break label30;
                            }
                        }

                        var1 = -var2;
                    }

                    var1 += var3;
                }

                this.mAdjustScroller.startScroll(0, 0, 0, var1, 300);
                super.mDelegator.invalidate();
                this.mIsValueChanged = false;
                return true;
            }
        }
    }

    public class PressedStateHelper implements Runnable {
        public final int MODE_PRESS = 1;
        public final int MODE_TAPPED = 2;
        public int mManagedButton;
        public int mMode;

        public PressedStateHelper() {
        }

        public void cancel() {
            int right = SeslSpinningDatePickerSpinnerDelegate.this.mDelegator.getRight();
            int bottom = SeslSpinningDatePickerSpinnerDelegate.this.mDelegator.getBottom();
            this.mMode = 0;
            this.mManagedButton = 0;
            SeslSpinningDatePickerSpinnerDelegate.this.mDelegator.removeCallbacks(this);
            if (SeslSpinningDatePickerSpinnerDelegate.this.mIncrementVirtualButtonPressed) {
                SeslSpinningDatePickerSpinnerDelegate.this.mIncrementVirtualButtonPressed = false;
                SeslSpinningDatePickerSpinnerDelegate seslSpinningDatePickerSpinnerDelegate = SeslSpinningDatePickerSpinnerDelegate.this;
                seslSpinningDatePickerSpinnerDelegate.mDelegator.invalidate(0, seslSpinningDatePickerSpinnerDelegate.mBottomSelectionDividerBottom, right, bottom);
            }
            if (SeslSpinningDatePickerSpinnerDelegate.this.mDecrementVirtualButtonPressed) {
                SeslSpinningDatePickerSpinnerDelegate.this.mDecrementVirtualButtonPressed = false;
                SeslSpinningDatePickerSpinnerDelegate seslSpinningDatePickerSpinnerDelegate2 = SeslSpinningDatePickerSpinnerDelegate.this;
                seslSpinningDatePickerSpinnerDelegate2.mDelegator.invalidate(0, 0, right, seslSpinningDatePickerSpinnerDelegate2.mTopSelectionDividerTop);
            }
        }

        public void buttonPressDelayed(int i) {
            cancel();
            this.mMode = 1;
            this.mManagedButton = i;
            SeslSpinningDatePickerSpinnerDelegate.this.mDelegator.postDelayed(this, (long) ViewConfiguration.getTapTimeout());
        }

        public void buttonTapped(int i) {
            cancel();
            this.mMode = 2;
            this.mManagedButton = i;
            SeslSpinningDatePickerSpinnerDelegate.this.mDelegator.post(this);
        }

        public void run() {
            int right = SeslSpinningDatePickerSpinnerDelegate.this.mDelegator.getRight();
            int bottom = SeslSpinningDatePickerSpinnerDelegate.this.mDelegator.getBottom();
            int i = this.mMode;
            if (i == 1) {
                int i2 = this.mManagedButton;
                if (i2 == 1) {
                    SeslSpinningDatePickerSpinnerDelegate.this.mIncrementVirtualButtonPressed = true;
                    SeslSpinningDatePickerSpinnerDelegate seslSpinningDatePickerSpinnerDelegate = SeslSpinningDatePickerSpinnerDelegate.this;
                    seslSpinningDatePickerSpinnerDelegate.mDelegator.invalidate(0, seslSpinningDatePickerSpinnerDelegate.mBottomSelectionDividerBottom, right, bottom);
                } else if (i2 == 2) {
                    SeslSpinningDatePickerSpinnerDelegate.this.mDecrementVirtualButtonPressed = true;
                    SeslSpinningDatePickerSpinnerDelegate seslSpinningDatePickerSpinnerDelegate2 = SeslSpinningDatePickerSpinnerDelegate.this;
                    seslSpinningDatePickerSpinnerDelegate2.mDelegator.invalidate(0, 0, right, seslSpinningDatePickerSpinnerDelegate2.mTopSelectionDividerTop);
                }
            } else if (i == 2) {
                int i3 = this.mManagedButton;
                if (i3 == 1) {
                    if (!SeslSpinningDatePickerSpinnerDelegate.this.mIncrementVirtualButtonPressed) {
                        SeslSpinningDatePickerSpinnerDelegate.this.mDelegator.postDelayed(this, (long) ViewConfiguration.getPressedStateDuration());
                    }
                    SeslSpinningDatePickerSpinnerDelegate.access$2080(SeslSpinningDatePickerSpinnerDelegate.this, 1);
                    SeslSpinningDatePickerSpinnerDelegate seslSpinningDatePickerSpinnerDelegate3 = SeslSpinningDatePickerSpinnerDelegate.this;
                    seslSpinningDatePickerSpinnerDelegate3.mDelegator.invalidate(0, seslSpinningDatePickerSpinnerDelegate3.mBottomSelectionDividerBottom, right, bottom);
                } else if (i3 == 2) {
                    if (!SeslSpinningDatePickerSpinnerDelegate.this.mDecrementVirtualButtonPressed) {
                        SeslSpinningDatePickerSpinnerDelegate.this.mDelegator.postDelayed(this, (long) ViewConfiguration.getPressedStateDuration());
                    }
                    SeslSpinningDatePickerSpinnerDelegate.access$2280(SeslSpinningDatePickerSpinnerDelegate.this, 1);
                    SeslSpinningDatePickerSpinnerDelegate seslSpinningDatePickerSpinnerDelegate4 = SeslSpinningDatePickerSpinnerDelegate.this;
                    seslSpinningDatePickerSpinnerDelegate4.mDelegator.invalidate(0, 0, right, seslSpinningDatePickerSpinnerDelegate4.mTopSelectionDividerTop);
                }
            }
        }
    }

    public class ChangeCurrentByOneFromLongPressCommand implements Runnable {
        public boolean mIncrement;

        public ChangeCurrentByOneFromLongPressCommand() {
        }

        public final void setStep(boolean z) {
            this.mIncrement = z;
        }

        public void run() {
            SeslSpinningDatePickerSpinnerDelegate.this.changeValueByOne(this.mIncrement);
            SeslSpinningDatePickerSpinnerDelegate seslSpinningDatePickerSpinnerDelegate = SeslSpinningDatePickerSpinnerDelegate.this;
            seslSpinningDatePickerSpinnerDelegate.mDelegator.postDelayed(this, seslSpinningDatePickerSpinnerDelegate.mLongPressUpdateInterval);
        }
    }

    public class AccessibilityNodeProviderImpl extends AccessibilityNodeProvider {
        public int mAccessibilityFocusedView = Integer.MIN_VALUE;
        public final int[] mTempArray = new int[2];
        public final Rect mTempRect = new Rect();

        public AccessibilityNodeProviderImpl() {
        }

        public AccessibilityNodeInfo createAccessibilityNodeInfo(int i) {
            int left = SeslSpinningDatePickerSpinnerDelegate.this.mDelegator.getLeft();
            int right = SeslSpinningDatePickerSpinnerDelegate.this.mDelegator.getRight();
            int top = SeslSpinningDatePickerSpinnerDelegate.this.mDelegator.getTop();
            int bottom = SeslSpinningDatePickerSpinnerDelegate.this.mDelegator.getBottom();
            int scrollX = SeslSpinningDatePickerSpinnerDelegate.this.mDelegator.getScrollX();
            int scrollY = SeslSpinningDatePickerSpinnerDelegate.this.mDelegator.getScrollY();
            if (!(SeslSpinningDatePickerSpinnerDelegate.this.mLastFocusedChildVirtualViewId == -1 && SeslSpinningDatePickerSpinnerDelegate.this.mLastHoveredChildVirtualViewId == Integer.MIN_VALUE)) {
                if (i == -1) {
                    return createAccessibilityNodeInfoForDatePickerWidget(scrollX, scrollY, (right - left) + scrollX, (bottom - top) + scrollY);
                }
                if (i == 1) {
                    return createAccessibilityNodeInfoForVirtualButton(1, getVirtualDecrementButtonText(), scrollX, scrollY, scrollX + (right - left), SeslSpinningDatePickerSpinnerDelegate.this.mTopSelectionDividerTop + SeslSpinningDatePickerSpinnerDelegate.this.mSelectionDividerHeight);
                }
                if (i == 2) {
                    return createAccessibiltyNodeInfoForCenter(scrollX, SeslSpinningDatePickerSpinnerDelegate.this.mTopSelectionDividerTop + SeslSpinningDatePickerSpinnerDelegate.this.mSelectionDividerHeight, (right - left) + scrollX, SeslSpinningDatePickerSpinnerDelegate.this.mBottomSelectionDividerBottom - SeslSpinningDatePickerSpinnerDelegate.this.mSelectionDividerHeight);
                }
                if (i == 3) {
                    return createAccessibilityNodeInfoForVirtualButton(3, getVirtualIncrementButtonText(), scrollX, SeslSpinningDatePickerSpinnerDelegate.this.mBottomSelectionDividerBottom - SeslSpinningDatePickerSpinnerDelegate.this.mSelectionDividerHeight, scrollX + (right - left), scrollY + (bottom - top));
                }
            }
            AccessibilityNodeInfo createAccessibilityNodeInfo = super.createAccessibilityNodeInfo(i);
            if (createAccessibilityNodeInfo == null) {
                return AccessibilityNodeInfo.obtain();
            }
            return createAccessibilityNodeInfo;
        }

        @Override // android.view.accessibility.AccessibilityNodeProvider
        public List<AccessibilityNodeInfo> findAccessibilityNodeInfosByText(String str, int i) {
            if (TextUtils.isEmpty(str)) {
                return Collections.emptyList();
            }
            String lowerCase = str.toLowerCase();
            ArrayList arrayList = new ArrayList();
            if (i == -1) {
                findAccessibilityNodeInfosByTextInChild(lowerCase, 1, arrayList);
                findAccessibilityNodeInfosByTextInChild(lowerCase, 2, arrayList);
                findAccessibilityNodeInfosByTextInChild(lowerCase, 3, arrayList);
                return arrayList;
            } else if (i != 1 && i != 2 && i != 3) {
                return super.findAccessibilityNodeInfosByText(str, i);
            } else {
                findAccessibilityNodeInfosByTextInChild(lowerCase, i, arrayList);
                return arrayList;
            }
        }

        public boolean performAction(int i, int i2, Bundle bundle) {
            if (SeslSpinningDatePickerSpinnerDelegate.this.mIsStartingAnimation) {
                return false;
            }
            int right = SeslSpinningDatePickerSpinnerDelegate.this.mDelegator.getRight();
            int bottom = SeslSpinningDatePickerSpinnerDelegate.this.mDelegator.getBottom();
            if (i != -1) {
                if (i != 1) {
                    if (i != 2) {
                        if (i == 3) {
                            if (i2 != 16) {
                                if (i2 != 64) {
                                    if (i2 != 128 || this.mAccessibilityFocusedView != i) {
                                        return false;
                                    }
                                    this.mAccessibilityFocusedView = Integer.MIN_VALUE;
                                    sendAccessibilityEventForVirtualView(i, 65536);
                                    SeslSpinningDatePickerSpinnerDelegate seslSpinningDatePickerSpinnerDelegate = SeslSpinningDatePickerSpinnerDelegate.this;
                                    seslSpinningDatePickerSpinnerDelegate.mDelegator.invalidate(0, seslSpinningDatePickerSpinnerDelegate.mBottomSelectionDividerBottom, right, bottom);
                                    return true;
                                } else if (this.mAccessibilityFocusedView == i) {
                                    return false;
                                } else {
                                    this.mAccessibilityFocusedView = i;
                                    sendAccessibilityEventForVirtualView(i, 32768);
                                    SeslSpinningDatePickerSpinnerDelegate seslSpinningDatePickerSpinnerDelegate2 = SeslSpinningDatePickerSpinnerDelegate.this;
                                    seslSpinningDatePickerSpinnerDelegate2.mDelegator.invalidate(0, seslSpinningDatePickerSpinnerDelegate2.mBottomSelectionDividerBottom, right, bottom);
                                    return true;
                                }
                            } else if (!SeslSpinningDatePickerSpinnerDelegate.this.mDelegator.isEnabled()) {
                                return false;
                            } else {
                                SeslSpinningDatePickerSpinnerDelegate.this.startFadeAnimation(false);
                                SeslSpinningDatePickerSpinnerDelegate.this.changeValueByOne(true);
                                sendAccessibilityEventForVirtualView(i, 1);
                                SeslSpinningDatePickerSpinnerDelegate.this.startFadeAnimation(true);
                                return true;
                            }
                        }
                    } else if (i2 != 16) {
                        if (i2 != 64) {
                            if (i2 != 128 || this.mAccessibilityFocusedView != i) {
                                return false;
                            }
                            this.mAccessibilityFocusedView = Integer.MIN_VALUE;
                            sendAccessibilityEventForVirtualView(i, 65536);
                            SeslSpinningDatePickerSpinnerDelegate seslSpinningDatePickerSpinnerDelegate3 = SeslSpinningDatePickerSpinnerDelegate.this;
                            seslSpinningDatePickerSpinnerDelegate3.mDelegator.invalidate(0, seslSpinningDatePickerSpinnerDelegate3.mTopSelectionDividerTop, right, SeslSpinningDatePickerSpinnerDelegate.this.mBottomSelectionDividerBottom);
                            return true;
                        } else if (this.mAccessibilityFocusedView == i) {
                            return false;
                        } else {
                            this.mAccessibilityFocusedView = i;
                            sendAccessibilityEventForVirtualView(i, 32768);
                            SeslSpinningDatePickerSpinnerDelegate seslSpinningDatePickerSpinnerDelegate4 = SeslSpinningDatePickerSpinnerDelegate.this;
                            seslSpinningDatePickerSpinnerDelegate4.mDelegator.invalidate(0, seslSpinningDatePickerSpinnerDelegate4.mTopSelectionDividerTop, right, SeslSpinningDatePickerSpinnerDelegate.this.mBottomSelectionDividerBottom);
                            return true;
                        }
                    } else if (!SeslSpinningDatePickerSpinnerDelegate.this.mDelegator.isEnabled()) {
                        return false;
                    } else {
                        SeslSpinningDatePickerSpinnerDelegate.this.performClick();
                        return true;
                    }
                } else if (i2 != 16) {
                    if (i2 != 64) {
                        if (i2 != 128 || this.mAccessibilityFocusedView != i) {
                            return false;
                        }
                        this.mAccessibilityFocusedView = Integer.MIN_VALUE;
                        sendAccessibilityEventForVirtualView(i, 65536);
                        SeslSpinningDatePickerSpinnerDelegate seslSpinningDatePickerSpinnerDelegate5 = SeslSpinningDatePickerSpinnerDelegate.this;
                        seslSpinningDatePickerSpinnerDelegate5.mDelegator.invalidate(0, 0, right, seslSpinningDatePickerSpinnerDelegate5.mTopSelectionDividerTop);
                        return true;
                    } else if (this.mAccessibilityFocusedView == i) {
                        return false;
                    } else {
                        this.mAccessibilityFocusedView = i;
                        sendAccessibilityEventForVirtualView(i, 32768);
                        SeslSpinningDatePickerSpinnerDelegate seslSpinningDatePickerSpinnerDelegate6 = SeslSpinningDatePickerSpinnerDelegate.this;
                        seslSpinningDatePickerSpinnerDelegate6.mDelegator.invalidate(0, 0, right, seslSpinningDatePickerSpinnerDelegate6.mTopSelectionDividerTop);
                        return true;
                    }
                } else if (!SeslSpinningDatePickerSpinnerDelegate.this.mDelegator.isEnabled()) {
                    return false;
                } else {
                    SeslSpinningDatePickerSpinnerDelegate.this.startFadeAnimation(false);
                    SeslSpinningDatePickerSpinnerDelegate.this.changeValueByOne(false);
                    sendAccessibilityEventForVirtualView(i, 1);
                    SeslSpinningDatePickerSpinnerDelegate.this.startFadeAnimation(true);
                    return true;
                }
            } else if (i2 != 64) {
                if (i2 != 128) {
                    if (i2 != 4096) {
                        if (i2 == 8192) {
                            if (!SeslSpinningDatePickerSpinnerDelegate.this.mDelegator.isEnabled() || (!SeslSpinningDatePickerSpinnerDelegate.this.getWrapSelectorWheel() && SeslSpinningDatePickerSpinnerDelegate.this.getValue().compareTo(SeslSpinningDatePickerSpinnerDelegate.this.getMinValue()) <= 0)) {
                                return false;
                            }
                            SeslSpinningDatePickerSpinnerDelegate.this.startFadeAnimation(false);
                            SeslSpinningDatePickerSpinnerDelegate.this.changeValueByOne(false);
                            SeslSpinningDatePickerSpinnerDelegate.this.startFadeAnimation(true);
                            return true;
                        }
                    } else if (!SeslSpinningDatePickerSpinnerDelegate.this.mDelegator.isEnabled() || (!SeslSpinningDatePickerSpinnerDelegate.this.getWrapSelectorWheel() && SeslSpinningDatePickerSpinnerDelegate.this.getValue().compareTo(SeslSpinningDatePickerSpinnerDelegate.this.getMaxValue()) >= 0)) {
                        return false;
                    } else {
                        SeslSpinningDatePickerSpinnerDelegate.this.startFadeAnimation(false);
                        SeslSpinningDatePickerSpinnerDelegate.this.changeValueByOne(true);
                        SeslSpinningDatePickerSpinnerDelegate.this.startFadeAnimation(true);
                        return true;
                    }
                } else if (this.mAccessibilityFocusedView != i) {
                    return false;
                } else {
                    this.mAccessibilityFocusedView = Integer.MIN_VALUE;
                    SeslViewReflector.clearAccessibilityFocus(SeslSpinningDatePickerSpinnerDelegate.this.mDelegator);
                    return true;
                }
            } else if (this.mAccessibilityFocusedView == i) {
                return false;
            } else {
                this.mAccessibilityFocusedView = i;
                SeslViewReflector.requestAccessibilityFocus(SeslSpinningDatePickerSpinnerDelegate.this.mDelegator);
                return true;
            }
            return super.performAction(i, i2, bundle);
        }

        public void sendAccessibilityEventForVirtualView(int i, int i2) {
            if (i != 1) {
                if (i == 2) {
                    sendAccessibilityEventForCenter(i2);
                } else if (i == 3 && hasVirtualIncrementButton()) {
                    sendAccessibilityEventForVirtualButton(i, i2, getVirtualIncrementButtonText());
                }
            } else if (hasVirtualDecrementButton()) {
                sendAccessibilityEventForVirtualButton(i, i2, getVirtualDecrementButtonText());
            }
        }

        public final void sendAccessibilityEventForCenter(int i) {
            if (SeslSpinningDatePickerSpinnerDelegate.this.mAccessibilityManager.isEnabled()) {
                AccessibilityEvent obtain = AccessibilityEvent.obtain(i);
                obtain.setPackageName(SeslSpinningDatePickerSpinnerDelegate.this.mContext.getPackageName());
                obtain.getText().add(getVirtualCurrentButtonText() + SeslSpinningDatePickerSpinnerDelegate.this.mContext.getString(R.string.sesl_date_picker_switch_to_calendar_description));
                obtain.setEnabled(SeslSpinningDatePickerSpinnerDelegate.this.mDelegator.isEnabled());
                obtain.setSource(SeslSpinningDatePickerSpinnerDelegate.this.mDelegator, 2);
                SeslSpinningDatePickerSpinner seslSpinningDatePickerSpinner = SeslSpinningDatePickerSpinnerDelegate.this.mDelegator;
                seslSpinningDatePickerSpinner.requestSendAccessibilityEvent(seslSpinningDatePickerSpinner, obtain);
            }
        }

        public final void sendAccessibilityEventForVirtualButton(int i, int i2, String str) {
            if (SeslSpinningDatePickerSpinnerDelegate.this.mAccessibilityManager.isEnabled()) {
                AccessibilityEvent obtain = AccessibilityEvent.obtain(i2);
                obtain.setClassName(Button.class.getName());
                obtain.setPackageName(SeslSpinningDatePickerSpinnerDelegate.this.mContext.getPackageName());
                obtain.getText().add(str);
                obtain.setEnabled(SeslSpinningDatePickerSpinnerDelegate.this.mDelegator.isEnabled());
                obtain.setSource(SeslSpinningDatePickerSpinnerDelegate.this.mDelegator, i);
                SeslSpinningDatePickerSpinner seslSpinningDatePickerSpinner = SeslSpinningDatePickerSpinnerDelegate.this.mDelegator;
                seslSpinningDatePickerSpinner.requestSendAccessibilityEvent(seslSpinningDatePickerSpinner, obtain);
            }
        }

        public final void findAccessibilityNodeInfosByTextInChild(String str, int i, List<AccessibilityNodeInfo> list) {
            if (i == 1) {
                String virtualDecrementButtonText = getVirtualDecrementButtonText();
                if (!TextUtils.isEmpty(virtualDecrementButtonText) && virtualDecrementButtonText.toLowerCase().contains(str)) {
                    list.add(createAccessibilityNodeInfo(1));
                }
            } else if (i == 2) {
                String virtualCurrentButtonText = getVirtualCurrentButtonText();
                if (!TextUtils.isEmpty(virtualCurrentButtonText) && virtualCurrentButtonText.toLowerCase().contains(str)) {
                    list.add(createAccessibilityNodeInfo(2));
                }
            } else if (i == 3) {
                String virtualIncrementButtonText = getVirtualIncrementButtonText();
                if (!TextUtils.isEmpty(virtualIncrementButtonText) && virtualIncrementButtonText.toLowerCase().contains(str)) {
                    list.add(createAccessibilityNodeInfo(3));
                }
            }
        }

        public final AccessibilityNodeInfo createAccessibiltyNodeInfoForCenter(int i, int i2, int i3, int i4) {
            AccessibilityNodeInfo obtain = AccessibilityNodeInfo.obtain();
            obtain.setPackageName(SeslSpinningDatePickerSpinnerDelegate.this.mContext.getPackageName());
            obtain.setSource(SeslSpinningDatePickerSpinnerDelegate.this.mDelegator, 2);
            obtain.setParent(SeslSpinningDatePickerSpinnerDelegate.this.mDelegator);
            obtain.setText(getVirtualCurrentButtonText() + SeslSpinningDatePickerSpinnerDelegate.this.mContext.getString(R.string.sesl_date_picker_switch_to_calendar_description));
            obtain.setClickable(true);
            obtain.setEnabled(SeslSpinningDatePickerSpinnerDelegate.this.mDelegator.isEnabled());
            if (this.mAccessibilityFocusedView != 2) {
                obtain.setAccessibilityFocused(false);
                obtain.addAction(64);
            } else {
                obtain.setAccessibilityFocused(true);
                obtain.addAction(128);
            }
            Rect rect = this.mTempRect;
            rect.set(i, i2, i3, i4);
            obtain.setVisibleToUser(SeslSpinningDatePickerSpinnerDelegate.this.mDelegator.isVisibleToUserWrapper(rect));
            obtain.setBoundsInParent(rect);
            int[] iArr = this.mTempArray;
            SeslSpinningDatePickerSpinnerDelegate.this.mDelegator.getLocationOnScreen(iArr);
            rect.offset(iArr[0], iArr[1]);
            obtain.setBoundsInScreen(rect);
            return obtain;
        }

        public final AccessibilityNodeInfo createAccessibilityNodeInfoForVirtualButton(int i, String str, int i2, int i3, int i4, int i5) {
            AccessibilityNodeInfo obtain = AccessibilityNodeInfo.obtain();
            obtain.setClassName(Button.class.getName());
            obtain.setPackageName(SeslSpinningDatePickerSpinnerDelegate.this.mContext.getPackageName());
            obtain.setSource(SeslSpinningDatePickerSpinnerDelegate.this.mDelegator, i);
            obtain.setParent(SeslSpinningDatePickerSpinnerDelegate.this.mDelegator);
            obtain.setText(str);
            obtain.setClickable(true);
            obtain.setLongClickable(true);
            obtain.setEnabled(SeslSpinningDatePickerSpinnerDelegate.this.mDelegator.isEnabled());
            Rect rect = this.mTempRect;
            rect.set(i2, i3, i4, i5);
            obtain.setVisibleToUser(SeslSpinningDatePickerSpinnerDelegate.this.mDelegator.isVisibleToUserWrapper(rect));
            obtain.setBoundsInParent(rect);
            int[] iArr = this.mTempArray;
            SeslSpinningDatePickerSpinnerDelegate.this.mDelegator.getLocationOnScreen(iArr);
            rect.offset(iArr[0], iArr[1]);
            obtain.setBoundsInScreen(rect);
            if (this.mAccessibilityFocusedView != i) {
                obtain.addAction(64);
            } else {
                obtain.addAction(128);
            }
            if (SeslSpinningDatePickerSpinnerDelegate.this.mDelegator.isEnabled()) {
                obtain.addAction(16);
            }
            return obtain;
        }

        public final AccessibilityNodeInfo createAccessibilityNodeInfoForDatePickerWidget(int i, int i2, int i3, int i4) {
            AccessibilityNodeInfo obtain = AccessibilityNodeInfo.obtain();
            obtain.setClassName(SeslSpinningDatePickerSpinner.class.getName());
            obtain.setPackageName(SeslSpinningDatePickerSpinnerDelegate.this.mContext.getPackageName());
            obtain.setSource(SeslSpinningDatePickerSpinnerDelegate.this.mDelegator);
            if (hasVirtualDecrementButton()) {
                obtain.addChild(SeslSpinningDatePickerSpinnerDelegate.this.mDelegator, 1);
            }
            obtain.addChild(SeslSpinningDatePickerSpinnerDelegate.this.mDelegator, 2);
            if (hasVirtualIncrementButton()) {
                obtain.addChild(SeslSpinningDatePickerSpinnerDelegate.this.mDelegator, 3);
            }
            obtain.setParent((View) SeslSpinningDatePickerSpinnerDelegate.this.mDelegator.getParentForAccessibility());
            obtain.setEnabled(SeslSpinningDatePickerSpinnerDelegate.this.mDelegator.isEnabled());
            obtain.setScrollable(true);
            float field_applicationScale = SeslCompatibilityInfoReflector.getField_applicationScale(SeslSpinningDatePickerSpinnerDelegate.this.mContext.getResources());
            Rect rect = this.mTempRect;
            rect.set(i, i2, i3, i4);
            scaleRect(rect, field_applicationScale);
            obtain.setBoundsInParent(rect);
            obtain.setVisibleToUser(SeslSpinningDatePickerSpinnerDelegate.this.mDelegator.isVisibleToUserWrapper());
            int[] iArr = this.mTempArray;
            SeslSpinningDatePickerSpinnerDelegate.this.mDelegator.getLocationOnScreen(iArr);
            rect.offset(iArr[0], iArr[1]);
            scaleRect(rect, field_applicationScale);
            obtain.setBoundsInScreen(rect);
            if (this.mAccessibilityFocusedView != -1) {
                obtain.addAction(64);
            } else {
                obtain.addAction(128);
            }
            if (SeslSpinningDatePickerSpinnerDelegate.this.mDelegator.isEnabled()) {
                if (SeslSpinningDatePickerSpinnerDelegate.this.getWrapSelectorWheel() || SeslSpinningDatePickerSpinnerDelegate.this.getValue().compareTo(SeslSpinningDatePickerSpinnerDelegate.this.getMaxValue()) < 0) {
                    obtain.addAction(4096);
                }
                if (SeslSpinningDatePickerSpinnerDelegate.this.getWrapSelectorWheel() || SeslSpinningDatePickerSpinnerDelegate.this.getValue().compareTo(SeslSpinningDatePickerSpinnerDelegate.this.getMinValue()) > 0) {
                    obtain.addAction(8192);
                }
            }
            return obtain;
        }

        public final void scaleRect(Rect rect, float f) {
            if (f != 1.0f) {
                rect.left = (int) ((((float) rect.left) * f) + 0.5f);
                rect.top = (int) ((((float) rect.top) * f) + 0.5f);
                rect.right = (int) ((((float) rect.right) * f) + 0.5f);
                rect.bottom = (int) ((((float) rect.bottom) * f) + 0.5f);
            }
        }

        public final boolean hasVirtualDecrementButton() {
            return SeslSpinningDatePickerSpinnerDelegate.this.getWrapSelectorWheel() || SeslSpinningDatePickerSpinnerDelegate.this.getValue().compareTo(SeslSpinningDatePickerSpinnerDelegate.this.getMinValue()) > 0;
        }

        public final boolean hasVirtualIncrementButton() {
            return SeslSpinningDatePickerSpinnerDelegate.this.getWrapSelectorWheel() || SeslSpinningDatePickerSpinnerDelegate.this.getValue().compareTo(SeslSpinningDatePickerSpinnerDelegate.this.getMaxValue()) < 0;
        }

        public final String getVirtualDecrementButtonText() {
            Calendar calendar = (Calendar) SeslSpinningDatePickerSpinnerDelegate.this.mValue.clone();
            calendar.add(5, -1);
            if (SeslSpinningDatePickerSpinnerDelegate.this.mWrapSelectorWheel) {
                calendar = SeslSpinningDatePickerSpinnerDelegate.this.getWrappedSelectorIndex(calendar);
            }
            if (calendar.compareTo(SeslSpinningDatePickerSpinnerDelegate.this.mMinValue) < 0) {
                return null;
            }
            if (SeslSpinningDatePickerSpinnerDelegate.this.mIsLunar) {
                return SeslSpinningDatePickerSpinnerDelegate.this.formatDateForLunarForAccessibility(calendar);
            }
            return SeslSpinningDatePickerSpinnerDelegate.this.formatDateForAccessibility(calendar) + ", " + SeslSpinningDatePickerSpinnerDelegate.this.mPickerContentDescription + ", ";
        }

        public final String getVirtualIncrementButtonText() {
            Calendar calendar = (Calendar) SeslSpinningDatePickerSpinnerDelegate.this.mValue.clone();
            calendar.add(5, 1);
            if (SeslSpinningDatePickerSpinnerDelegate.this.mWrapSelectorWheel) {
                calendar = SeslSpinningDatePickerSpinnerDelegate.this.getWrappedSelectorIndex(calendar);
            }
            if (calendar.compareTo(SeslSpinningDatePickerSpinnerDelegate.this.mMaxValue) > 0) {
                return null;
            }
            if (SeslSpinningDatePickerSpinnerDelegate.this.mIsLunar) {
                return SeslSpinningDatePickerSpinnerDelegate.this.formatDateForLunarForAccessibility(calendar);
            }
            return SeslSpinningDatePickerSpinnerDelegate.this.formatDateForAccessibility(calendar) + ", " + SeslSpinningDatePickerSpinnerDelegate.this.mPickerContentDescription + ", ";
        }

        public final String getVirtualCurrentButtonText() {
            Calendar calendar = (Calendar) SeslSpinningDatePickerSpinnerDelegate.this.mValue.clone();
            if (SeslSpinningDatePickerSpinnerDelegate.this.mWrapSelectorWheel) {
                calendar = SeslSpinningDatePickerSpinnerDelegate.this.getWrappedSelectorIndex(calendar);
            }
            if (calendar.compareTo(SeslSpinningDatePickerSpinnerDelegate.this.mMaxValue) > 0) {
                return null;
            }
            if (SeslSpinningDatePickerSpinnerDelegate.this.mIsLunar) {
                return SeslSpinningDatePickerSpinnerDelegate.this.formatDateForLunarForAccessibility(calendar);
            }
            return SeslSpinningDatePickerSpinnerDelegate.this.formatDateForAccessibility(calendar) + ", " + SeslSpinningDatePickerSpinnerDelegate.this.mPickerContentDescription + ", ";
        }
    }

    public final void clearCalendar(Calendar calendar, Calendar calendar2) {
        calendar.set(1, calendar2.get(1));
        calendar.set(2, calendar2.get(2));
        calendar.set(5, calendar2.get(5));
    }

    public static String formatDateWithLocale(Calendar calendar) {
        return new SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(calendar.getTime());
    }

    public static String formatDateWithLocaleForAccessibility(Calendar calendar) {
        return new SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(calendar.getTime());
    }

    public static String formatDayWithLocale(Calendar calendar) {
        return new SimpleDateFormat("d", Locale.getDefault()).format(calendar.getTime());
    }

    public static String getDayWithLocale(int i) {
        return String.format(Locale.getDefault(), "%d", Integer.valueOf(i));
    }

    public static String formatMonthWithLocale(Calendar calendar) {
        return new SimpleDateFormat("MMM", Locale.getDefault()).format(calendar.getTime());
    }

    public static String formatMonthWithLocaleForAccessibility(Calendar calendar) {
        return new SimpleDateFormat("MMMM", Locale.getDefault()).format(calendar.getTime());
    }

    public final String getMonthWithLocale(int i) {
        return this.mShortMonths[i];
    }

    public final String getMonthWithLocaleForAccessibility(int i) {
        return this.mLongMonths[i];
    }

    public static String formatNumberWithLocale(int i) {
        return String.format(Locale.getDefault(), "%d", Integer.valueOf(i));
    }

    public final boolean isCharacterNumberLanguage() {
        String language = Locale.getDefault().getLanguage();
        return "ar".equals(language) || "fa".equals(language) || "my".equals(language);
    }

    public Calendar convertSolarToLunar(Calendar calendar, SeslSpinningDatePicker$LunarDate seslSpinningDatePicker$LunarDate) {
        Calendar calendar2 = (Calendar) calendar.clone();
        SeslSolarLunarConverterReflector.convertSolarToLunar(this.mPathClassLoader, this.mSolarLunarConverter, calendar.get(1), calendar.get(2), calendar.get(5));
        calendar2.set(SeslSolarLunarConverterReflector.getYear(this.mPathClassLoader, this.mSolarLunarConverter), SeslSolarLunarConverterReflector.getMonth(this.mPathClassLoader, this.mSolarLunarConverter), SeslSolarLunarConverterReflector.getDay(this.mPathClassLoader, this.mSolarLunarConverter));
        if (seslSpinningDatePicker$LunarDate != null) {
            seslSpinningDatePicker$LunarDate.day = SeslSolarLunarConverterReflector.getDay(this.mPathClassLoader, this.mSolarLunarConverter);
            seslSpinningDatePicker$LunarDate.month = SeslSolarLunarConverterReflector.getMonth(this.mPathClassLoader, this.mSolarLunarConverter);
            seslSpinningDatePicker$LunarDate.year = SeslSolarLunarConverterReflector.getYear(this.mPathClassLoader, this.mSolarLunarConverter);
            seslSpinningDatePicker$LunarDate.isLeapMonth = SeslSolarLunarConverterReflector.isLeapMonth(this.mPathClassLoader, this.mSolarLunarConverter);
        }
        return calendar2;
    }

    public final Calendar getCalendarForLocale(Calendar calendar, Locale locale) {
        Calendar instance = Calendar.getInstance(locale);
        if (calendar != null) {
            instance.setTimeInMillis(calendar.getTimeInMillis());
        }
        instance.set(11, 12);
        instance.set(12, 0);
        instance.set(13, 0);
        instance.set(14, 0);
        return instance;
    }

    public final boolean isHighContrastFontEnabled() {
        return SeslViewReflector.isHighContrastTextEnabled(this.mInputText);
    }
}
