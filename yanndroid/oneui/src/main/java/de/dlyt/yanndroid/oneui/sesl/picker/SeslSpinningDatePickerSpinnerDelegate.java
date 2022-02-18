package de.dlyt.yanndroid.oneui.sesl.picker;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.InputDevice;
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
import android.widget.TextView;

import androidx.appcompat.util.SeslMisc;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.FloatValueHolder;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;
import androidx.reflect.content.res.SeslCompatibilityInfoReflector;
import androidx.reflect.content.res.SeslConfigurationReflector;
import androidx.reflect.graphics.SeslPaintReflector;
import androidx.reflect.lunarcalendar.SeslFeatureReflector;
import androidx.reflect.lunarcalendar.SeslSolarLunarConverterReflector;
import androidx.reflect.media.SeslAudioManagerReflector;
import androidx.reflect.media.SeslSemSoundAssistantManagerReflector;
import androidx.reflect.view.SeslHapticFeedbackConstantsReflector;
import androidx.reflect.view.SeslViewReflector;

import java.io.File;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import dalvik.system.PathClassLoader;
import de.dlyt.yanndroid.oneui.R;
import de.dlyt.yanndroid.oneui.sesl.picker.SeslSpinningDatePickerSpinner.OnScrollListener;
import de.dlyt.yanndroid.oneui.sesl.utils.SeslAnimationListener;
import de.dlyt.yanndroid.oneui.widget.SpinningDatePicker;

public class SeslSpinningDatePickerSpinnerDelegate extends SeslSpinningDatePickerSpinner.AbsDatePickerDelegate {
    private static final int DECREASE_BUTTON = 1;
    private static final int DEFAULT_CHANGE_VALUE_BY = 1;
    private static final int DEFAULT_END_YEAR = 2100;
    private static final long DEFAULT_LONG_PRESS_UPDATE_INTERVAL = 300;
    private static final int DEFAULT_START_YEAR = 1902;
    private static final int HCF_UNFOCUSED_TEXT_SIZE_DIFF = 2;
    private static final int INCREASE_BUTTON = 3;
    private static final int INPUT = 2;
    private static final int LONG_PRESSED_SCROLL_COUNT = 10;
    private static final int PICKER_VIBRATE_INDEX = 32;
    private static final int SELECTOR_ADJUSTMENT_DURATION_MILLIS = 300;
    private static final int SELECTOR_MAX_FLING_VELOCITY_ADJUSTMENT = 4;
    private static final int SELECTOR_MIDDLE_ITEM_INDEX = 2;
    private static final int SELECTOR_WHEEL_ITEM_COUNT = 5;
    private static final int SIZE_UNSPECIFIED = -1;
    private static final int SNAP_SCROLL_DURATION = 500;
    private static final int START_ANIMATION_SCROLL_DURATION = 857;
    private static final int START_ANIMATION_SCROLL_DURATION_2016B = 557;
    private static final int TEXT_GAP_COUNT = 3;
    private static final int UNSCALED_DEFAULT_SELECTION_DIVIDER_HEIGHT = 2;
    private final PathInterpolator ALPHA_PATH_INTERPOLATOR = new PathInterpolator(0.17f, 0.17f, 0.83f, 0.83f);
    private final PathInterpolator SIZE_PATH_INTERPOLATOR = new PathInterpolator(0.5f, 0.0f, 0.4f, 1.0f);
    private AccessibilityManager mAccessibilityManager;
    private AccessibilityNodeProviderImpl mAccessibilityNodeProvider;
    private final Scroller mAdjustScroller;
    private float mAlpha = 0.1f;
    private SeslAnimationListener mAnimationListener;
    private AudioManager mAudioManager;
    private int mBottomSelectionDividerBottom;
    private ChangeCurrentByOneFromLongPressCommand mChangeCurrentByOneFromLongPressCommand;
    private ValueAnimator mColorInAnimator;
    private ValueAnimator mColorOutAnimator;
    private final boolean mComputeMaxWidth;
    private float mCurVelocity;
    private int mCurrentScrollOffset;
    private final Scroller mCustomScroller;
    private boolean mDecrementVirtualButtonPressed;
    private final Typeface mDefaultTypeface;
    private ValueAnimator mFadeInAnimator;
    private ValueAnimator mFadeOutAnimator;
    private Scroller mFlingScroller;
    private SeslSpinningDatePickerSpinner.Formatter mFormatter;
    private OverScroller mGravityScroller;
    private HapticPreDrawListener mHapticPreDrawListener;
    private Typeface mHcfFocusedTypefaceBold;
    private final int mHcfUnfocusedTextSizeDiff;
    private final float mHeightRatio;
    private FloatValueHolder mHolder;
    private float mIdleAlpha = 0.1f;
    private boolean mIgnoreMoveEvents;
    private boolean mIncrementVirtualButtonPressed;
    private float mInitialAlpha = 1.0f;
    private final EditText mInputText;
    private boolean mIsHcfEnabled;
    private boolean mIsLeapMonth;
    private boolean mIsLunar;
    private long mLastDownEventTime;
    private float mLastDownEventY;
    private float mLastDownOrMoveEventY;
    private int mLastFocusedChildVirtualViewId;
    private int mLastHoveredChildVirtualViewId;
    private final Typeface mLegacyTypeface;
    private final Scroller mLinearScroller;
    private String[] mLongMonths;
    private int mLongPressCount;
    private boolean mLongPressed_FIRST_SCROLL;
    private boolean mLongPressed_SECOND_SCROLL;
    private boolean mLongPressed_THIRD_SCROLL;
    private final int mMaxHeight;
    private Calendar mMaxValue;
    private int mMaxWidth;
    private int mMaximumFlingVelocity;
    private final int mMinHeight;
    private Calendar mMinValue;
    private final int mMinWidth;
    private int mMinimumFlingVelocity;
    private int mModifiedTxtHeight;
    private OnScrollListener mOnScrollListener;
    private SeslSpinningDatePickerSpinner.OnSpinnerDateClickListener mOnSpinnerDateClickListener;
    private SeslSpinningDatePickerSpinner.OnValueChangeListener mOnValueChangeListener;
    private boolean mPerformClickOnTap;
    private String mPickerContentDescription;
    private int mPickerSoundFastIndex;
    private int mPickerSoundIndex;
    private int mPickerSoundSlowIndex;
    private Typeface mPickerSubTypeface;
    private Typeface mPickerTypeface;
    private int mPickerVibrateIndex;
    private final PressedStateHelper mPressedStateHelper;
    private int mPreviousScrollerY;
    private float mPreviousSpringY;
    private final int mSelectionDividerHeight;
    private int mSelectorElementHeight;
    private int mSelectorTextGapHeight;
    private Paint mSelectorWheelPaint;
    private String[] mShortMonths;
    private boolean mSkipNumbers;
    private SpringAnimation mSpringAnimation;
    private boolean mSpringFlingRunning;
    private int mTextColor;
    private final int mTextColorIdle;
    private final int mTextColorScrolling;
    private int mTextSize;
    private int mTopSelectionDividerTop;
    private int mTouchSlop;
    private Calendar mValue;
    private int mValueChangeOffset;
    private VelocityTracker mVelocityTracker;
    private final Drawable mVirtualButtonFocusedDrawable;
    private boolean mWrapSelectorWheel;
    private long mLongPressUpdateInterval = DEFAULT_LONG_PRESS_UPDATE_INTERVAL;
    private final HashMap<Calendar, String> mSelectorIndexToStringCache = new HashMap<>();
    private final Calendar[] mSelectorIndices = new Calendar[5];
    private int mInitialScrollOffset = Integer.MIN_VALUE;
    private boolean mWrapSelectorWheelPreferred = true;
    private int mScrollState = OnScrollListener.SCROLL_STATE_IDLE;
    private int mChangeValueBy = DEFAULT_CHANGE_VALUE_BY;
    private boolean mIsLongClicked = false;
    private boolean mIsStartingAnimation = false;
    private boolean mReservedStartAnimation = false;
    private boolean mIsLongPressed = false;
    private boolean mCustomTypefaceSet = false;
    private boolean mIsValueChanged = false;
    private PathClassLoader mPathClassLoader = null;
    private Object mSolarLunarConverter = null;
    private float mActivatedAlpha = 0.4f;
    private final float FAST_SCROLL_VELOCITY_START = 1000.0f;

    private ValueAnimator.AnimatorUpdateListener mColorUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            mTextColor = (int) animation.getAnimatedValue();
            mDelegator.invalidate();
        }
    };

    private ValueAnimator.AnimatorUpdateListener mUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            mAlpha = (float) animation.getAnimatedValue();
            mDelegator.invalidate();
        }
    };

    private DynamicAnimation.OnAnimationUpdateListener mSpringAnimationUpdateListener = new DynamicAnimation.OnAnimationUpdateListener() {
        @Override
        public void onAnimationUpdate(DynamicAnimation animation, float value, float velocity) {
            if (!(velocity > 0.0f)) {
                velocity = -velocity;
            }

            mCurVelocity = velocity;

            final float y = value - mPreviousSpringY;
            if (!mSpringFlingRunning && Math.round(y) == 0) {
                animation.cancel();
                ensureScrollWheelAdjusted();
            } else {
                if (Math.round(y) == 0) {
                    mSpringFlingRunning = false;
                }
                scrollBy(0, Math.round(y));
                mPreviousSpringY = value;
                mDelegator.invalidate();
            }
        }
    };

    private DynamicAnimation.OnAnimationEndListener mSpringAnimationEndListener = new DynamicAnimation.OnAnimationEndListener() {
        @Override
        public void onAnimationEnd(DynamicAnimation animation, boolean canceled, float value, float velocity) {
            mSpringFlingRunning = false;
            mGravityScroller.forceFinished(true);
            startFadeAnimation(true);
        }
    };

    public SeslSpinningDatePickerSpinnerDelegate(SeslSpinningDatePickerSpinner spinningDatePickerSpinner, Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(spinningDatePickerSpinner, context);

        final Resources resources = mContext.getResources();

        final int spinnerHeight = resources.getDimensionPixelSize(R.dimen.sesl_number_picker_spinner_height);
        final int spinnerWidth = resources.getDimensionPixelSize(R.dimen.sesl_number_picker_spinner_width);

        mHeightRatio = (mContext.getResources().getDimension(R.dimen.sesl_number_picker_spinner_edit_text_height)) / ((float) spinnerHeight);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.NumberPicker, defStyleAttr, defStyleRes);
        mMinHeight = a.getDimensionPixelSize(R.styleable.NumberPicker_internalMinHeight, SIZE_UNSPECIFIED);
        mMaxHeight = a.getDimensionPixelSize(R.styleable.NumberPicker_internalMaxHeight, spinnerHeight);
        mMinWidth = a.getDimensionPixelSize(R.styleable.NumberPicker_internalMinWidth, spinnerWidth);
        mMaxWidth = a.getDimensionPixelSize(R.styleable.NumberPicker_internalMaxWidth, SIZE_UNSPECIFIED);
        a.recycle();

        mValue = getCalendarForLocale(mValue, Locale.getDefault());
        mMinValue = getCalendarForLocale(mMinValue, Locale.getDefault());
        mMaxValue = getCalendarForLocale(mMaxValue, Locale.getDefault());

        TypedArray a2 = context.obtainStyledAttributes(attrs, R.styleable.DatePicker, defStyleAttr, defStyleRes);
        mMinValue.set(a2.getInt(R.styleable.DatePicker_android_startYear, DEFAULT_START_YEAR), 0, 1);
        mMaxValue.set(a2.getInt(R.styleable.DatePicker_android_endYear, DEFAULT_END_YEAR), 11, 31);
        a2.recycle();

        if (mMinHeight != SIZE_UNSPECIFIED && mMaxHeight != SIZE_UNSPECIFIED && mMinHeight > mMaxHeight) {
            throw new IllegalArgumentException("minHeight > maxHeight");
        }
        if (mMinWidth != SIZE_UNSPECIFIED && mMaxWidth != SIZE_UNSPECIFIED && mMinWidth > mMaxWidth) {
            throw new IllegalArgumentException("minWidth > maxWidth");
        }

        mSelectionDividerHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, UNSCALED_DEFAULT_SELECTION_DIVIDER_HEIGHT, resources.getDisplayMetrics());
        mComputeMaxWidth = mMaxWidth == SIZE_UNSPECIFIED;

        int colorPrimaryDark;
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorPrimaryDark, typedValue, true);
        if (typedValue.resourceId != 0) {
            colorPrimaryDark = ResourcesCompat.getColor(resources, typedValue.resourceId, null);
        } else {
            colorPrimaryDark = typedValue.data;
        }
        mVirtualButtonFocusedDrawable = new ColorDrawable((colorPrimaryDark & ViewCompat.MEASURED_SIZE_MASK) | 855638016 /* #33000000 */);

        if (!SeslMisc.isLightTheme(mContext)) {
            mIdleAlpha = 0.2f;
            mAlpha = 0.2f;
        }

        mPressedStateHelper = new PressedStateHelper();
        mDelegator.setWillNotDraw(false);

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.sesl_spinning_date_picker_spinner, mDelegator, true);

        mInputText = mDelegator.findViewById(R.id.datepicker_input);
        mInputText.setIncludeFontPadding(false);

        mDefaultTypeface = Typeface.defaultFromStyle(Typeface.BOLD);
        mLegacyTypeface = Typeface.create("sec-roboto-condensed-light", Typeface.BOLD);
        mPickerTypeface = Typeface.create("sec-roboto-light", Typeface.BOLD);
        if (mDefaultTypeface.equals(mPickerTypeface)) {
            if (!mLegacyTypeface.equals(mPickerTypeface)) {
                mPickerTypeface = mLegacyTypeface;
            } else {
                mPickerTypeface = Typeface.create("sans-serif-thin", Typeface.BOLD);
            }
        }
        mPickerSubTypeface = Typeface.create(mPickerTypeface, Typeface.NORMAL);

        if (!SeslConfigurationReflector.isDexEnabled(resources.getConfiguration())) {
            final String clockThemeFont = Settings.System.getString(mContext.getContentResolver(), "theme_font_clock");
            if (clockThemeFont != null && !clockThemeFont.isEmpty()) {
                mPickerTypeface = getFontTypeface(clockThemeFont);
                mPickerSubTypeface = Typeface.create(mPickerTypeface, Typeface.NORMAL);
            }
        } else {
            mIdleAlpha = 0.2f;
            mAlpha = 0.2f;
        }

        if (isCharacterNumberLanguage()) {
            mPickerTypeface = mDefaultTypeface;
            mPickerSubTypeface = Typeface.create(mDefaultTypeface, Typeface.NORMAL);
        }

        mIsHcfEnabled = isHighContrastFontEnabled();
        mHcfFocusedTypefaceBold = Typeface.create(mPickerTypeface, Typeface.BOLD);
        mHcfUnfocusedTextSizeDiff = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, HCF_UNFOCUSED_TEXT_SIZE_DIFF, mContext.getResources().getDisplayMetrics());
        setInputTextTypeface();
        mTextColorIdle = mInputText.getTextColors().getColorForState(mDelegator.getEnableStateSet(), Color.WHITE);;
        mTextColorScrolling = ResourcesCompat.getColor(resources, R.color.sesl_number_picker_text_color_scroll, context.getTheme());
        mTextColor = mTextColorIdle;

        ViewConfiguration viewConfiguration = ViewConfiguration.get(context);
        mTouchSlop = viewConfiguration.getScaledTouchSlop();
        mMinimumFlingVelocity = viewConfiguration.getScaledMinimumFlingVelocity() * 2;
        mMaximumFlingVelocity = viewConfiguration.getScaledMaximumFlingVelocity() / 4;
        mTextSize = (int) mInputText.getTextSize();

        mSelectorWheelPaint = new Paint();
        mSelectorWheelPaint.setAntiAlias(true);
        mSelectorWheelPaint.setTextAlign(Paint.Align.CENTER);
        mSelectorWheelPaint.setTextSize((float) mTextSize);
        mSelectorWheelPaint.setTypeface(mPickerTypeface);
        mSelectorWheelPaint.setColor(mTextColor);
        mInitialAlpha = ((float) mSelectorWheelPaint.getAlpha()) / 255.0f;

        mCustomScroller = new Scroller(mContext, SIZE_PATH_INTERPOLATOR, true);
        mLinearScroller = new Scroller(mContext, null, true);
        mFlingScroller = new Scroller(mContext, null, true);
        mAdjustScroller = new Scroller(mContext, new PathInterpolator(0.4f, 0.0f, 0.3f, 1.0f));
        mGravityScroller = new OverScroller(mContext, new DecelerateInterpolator());

        mHolder = new FloatValueHolder();
        mSpringAnimation = new SpringAnimation(mHolder);
        mSpringAnimation.setSpring(new SpringForce());
        mSpringAnimation.setMinimumVisibleChange(1.0f);
        mSpringAnimation.addUpdateListener(mSpringAnimationUpdateListener);
        mSpringAnimation.addEndListener(mSpringAnimationEndListener);
        mSpringAnimation.getSpring().setStiffness(7.0f);
        mSpringAnimation.getSpring().setDampingRatio(0.99f);

        setFormatter(SeslSpinningDatePickerSpinner.getDateFormatter());
        mDelegator.setVerticalScrollBarEnabled(false);
        if (mDelegator.getImportantForAccessibility() == ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_AUTO) {
            mDelegator.setImportantForAccessibility(ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_YES);
        }

        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        mHapticPreDrawListener = new HapticPreDrawListener();
        mPickerVibrateIndex = SeslHapticFeedbackConstantsReflector.semGetVibrationIndex(PICKER_VIBRATE_INDEX);
        mPickerSoundIndex = SeslAudioManagerReflector.getField_SOUND_TIME_PICKER_SCROLL();
        mPickerSoundFastIndex = SeslAudioManagerReflector.getField_SOUND_TIME_PICKER_SCROLL_FAST();
        mPickerSoundSlowIndex = SeslAudioManagerReflector.getField_SOUND_TIME_PICKER_SCROLL_SLOW();
        SeslSemSoundAssistantManagerReflector.setFastAudioOpenMode(mContext, true);

        mDelegator.setFocusableInTouchMode(false);
        mDelegator.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
        if (Build.VERSION.SDK_INT >= 26) {
            mDelegator.setDefaultFocusHighlightEnabled(false);
        }

        mPickerContentDescription = "";

        SeslViewReflector.semSetDirectPenInputEnabled(mInputText, false);

        mAccessibilityManager = (AccessibilityManager) mContext.getSystemService(Context.ACCESSIBILITY_SERVICE);

        mFadeOutAnimator = ValueAnimator.ofFloat(mActivatedAlpha, mIdleAlpha);
        mFadeOutAnimator.setInterpolator(ALPHA_PATH_INTERPOLATOR);
        mFadeOutAnimator.setDuration(200);
        mFadeOutAnimator.setStartDelay(100);
        mFadeOutAnimator.addUpdateListener(mUpdateListener);
        mFadeInAnimator = ValueAnimator.ofFloat(mIdleAlpha, mActivatedAlpha);
        mFadeInAnimator.setInterpolator(ALPHA_PATH_INTERPOLATOR);
        mFadeInAnimator.setDuration(200);
        mFadeInAnimator.addUpdateListener(mUpdateListener);

        mColorInAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), mTextColorIdle, mTextColorScrolling);
        mColorInAnimator.setInterpolator(ALPHA_PATH_INTERPOLATOR);
        mColorInAnimator.setDuration(200);
        mColorInAnimator.addUpdateListener(mColorUpdateListener);
        mColorOutAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), mTextColorScrolling, mTextColorIdle);
        mColorOutAnimator.setInterpolator(ALPHA_PATH_INTERPOLATOR);
        mColorOutAnimator.setDuration(200);
        mColorOutAnimator.setStartDelay(100);
        mColorOutAnimator.addUpdateListener(mColorUpdateListener);

        mShortMonths = new DateFormatSymbols().getShortMonths();
        mLongMonths = new DateFormatSymbols().getMonths();
    }

    @Override
    public void setPickerContentDescription(String contentDescription) {
        mPickerContentDescription = contentDescription;
    }

    @Override
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        mModifiedTxtHeight = Math.max(mInputText.getMeasuredHeight(), (int) Math.floor((double) (((float) mDelegator.getMeasuredHeight()) * mHeightRatio)));

        final int l = (mDelegator.getMeasuredWidth() - mInputText.getMeasuredWidth()) / 2;
        final int t = (mDelegator.getMeasuredHeight() - mModifiedTxtHeight) / 2;
        final int r = mInputText.getMeasuredWidth() + l;
        final int b = mModifiedTxtHeight + t;
        mInputText.layout(l, t, r, b);

        if (changed) {
            initializeSelectorWheel();
            if (mModifiedTxtHeight > mSelectorElementHeight) {
                mTopSelectionDividerTop = mValueChangeOffset;
                mBottomSelectionDividerBottom = mValueChangeOffset * 2;
            } else {
                mTopSelectionDividerTop = t;
                mBottomSelectionDividerBottom = b;
            }
        }
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int newWidthMeasureSpec = makeMeasureSpec(widthMeasureSpec, mMaxWidth);
        final int newHeightMeasureSpec = makeMeasureSpec(heightMeasureSpec, mMaxHeight);
        mDelegator.superOnMeasure(newWidthMeasureSpec, newHeightMeasureSpec);
        final int widthSize = resolveSizeAndStateRespectingMinSize(mMinWidth, mDelegator.getMeasuredWidth(), widthMeasureSpec);
        final int heightSize = resolveSizeAndStateRespectingMinSize(mMinHeight, mDelegator.getMeasuredHeight(), heightMeasureSpec);
        mDelegator.setMeasuredDimensionWrapper(widthSize, heightSize);
    }

    private boolean moveToFinalScrollerPosition(Scroller scroller) {
        scroller.forceFinished(true);
        int amountToScroll = scroller.getFinalY() - scroller.getCurrY();
        int futureScrollOffset = (mCurrentScrollOffset + amountToScroll) % mSelectorElementHeight;
        int overshootAdjustment = mInitialScrollOffset - futureScrollOffset;
        if (overshootAdjustment != 0) {
            if (Math.abs(overshootAdjustment) > mSelectorElementHeight / 2) {
                if (overshootAdjustment > 0) {
                    overshootAdjustment -= mSelectorElementHeight;
                } else {
                    overshootAdjustment += mSelectorElementHeight;
                }
            }
            amountToScroll += overshootAdjustment;
            scrollBy(0, amountToScroll);
            return true;
        }
        return false;
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        if (!mIsStartingAnimation) {
            if (!mFlingScroller.isFinished()) {
                mFlingScroller.forceFinished(true);
            }
            if (!mAdjustScroller.isFinished()) {
                mAdjustScroller.forceFinished(true);
            }
            if (!mGravityScroller.isFinished()) {
                mGravityScroller.forceFinished(true);
            }
            if (mSpringAnimation.isRunning()) {
                mSpringAnimation.cancel();
                mSpringFlingRunning = false;
            }
            ensureScrollWheelAdjusted();
        }

        mIsHcfEnabled = isHighContrastFontEnabled();
        mSelectorWheelPaint.setTextSize((float) mTextSize);
        mSelectorWheelPaint.setTypeface(mPickerTypeface);
        setInputTextTypeface();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (!mDelegator.isEnabled() || mIsStartingAnimation) {
            return false;
        }

        final int action = event.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                removeAllCallbacks();
                mLastDownEventY = mLastDownOrMoveEventY = event.getY();
                mLastDownEventTime = event.getEventTime();
                mIgnoreMoveEvents = false;
                mPerformClickOnTap = false;
                mIsValueChanged = false;
                if (mLastDownEventY < mTopSelectionDividerTop) {
                    startFadeAnimation(false);
                    if (mScrollState == OnScrollListener.SCROLL_STATE_IDLE) {
                        mPressedStateHelper.buttonPressDelayed(PressedStateHelper.BUTTON_DECREMENT);
                    }
                } else if (mLastDownEventY > mBottomSelectionDividerBottom) {
                    startFadeAnimation(false);
                    if (mScrollState == OnScrollListener.SCROLL_STATE_IDLE) {
                        mPressedStateHelper.buttonPressDelayed(PressedStateHelper.BUTTON_INCREMENT);
                    }
                }
                mDelegator.getParent().requestDisallowInterceptTouchEvent(true);
                if (!mFlingScroller.isFinished()) {
                    mFlingScroller.forceFinished(true);
                    mAdjustScroller.forceFinished(true);
                    if (mScrollState == OnScrollListener.SCROLL_STATE_FLING) {
                        mFlingScroller.abortAnimation();
                        mAdjustScroller.abortAnimation();
                    }
                    onScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE);
                } else if (mSpringAnimation.isRunning()) {
                    mGravityScroller.forceFinished(true);
                    mAdjustScroller.forceFinished(true);
                    mSpringAnimation.cancel();
                    mSpringFlingRunning = false;
                    if (mScrollState == OnScrollListener.SCROLL_STATE_FLING) {
                        mGravityScroller.abortAnimation();
                        mAdjustScroller.abortAnimation();
                    }
                    onScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE);
                } else if (!mAdjustScroller.isFinished()) {
                    mFlingScroller.forceFinished(true);
                    mAdjustScroller.forceFinished(true);
                } else if (mLastDownEventY < mTopSelectionDividerTop) {
                    postChangeCurrentByOneFromLongPress(false, ViewConfiguration.getLongPressTimeout());
                } else if (mLastDownEventY > mBottomSelectionDividerBottom) {
                    postChangeCurrentByOneFromLongPress(true, ViewConfiguration.getLongPressTimeout());
                } else {
                    mPerformClickOnTap = true;
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!mDelegator.isEnabled() || mIsStartingAnimation) {
            return false;
        }
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
        int action = event.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_UP: {
                removeChangeCurrentByOneFromLongPress();
                mPressedStateHelper.cancel();
                VelocityTracker velocityTracker = mVelocityTracker;
                velocityTracker.computeCurrentVelocity(1000, mMaximumFlingVelocity);
                int initialVelocity = (int) velocityTracker.getYVelocity();
                int eventY = (int) event.getY();
                int deltaMoveY = (int) Math.abs(eventY - mLastDownEventY);
                if (Math.abs(initialVelocity) > mMinimumFlingVelocity) {
                    if (deltaMoveY > mTouchSlop || !mPerformClickOnTap) {
                        fling(initialVelocity);
                        onScrollStateChange(OnScrollListener.SCROLL_STATE_FLING);
                    } else {
                        mPerformClickOnTap = false;
                        performClick();
                        onScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE);
                    }
                } else {
                    final long timeout = event.getEventTime() - mLastDownEventTime;
                    if (deltaMoveY <= mTouchSlop && timeout < ViewConfiguration.getLongPressTimeout()) {
                        if (mPerformClickOnTap) {
                            mPerformClickOnTap = false;
                            performClick();
                        } else {
                            if (eventY > mBottomSelectionDividerBottom) {
                                changeValueByOne(true);
                                mPressedStateHelper.buttonTapped(PressedStateHelper.BUTTON_INCREMENT);
                            } else if (eventY < mTopSelectionDividerTop) {
                                changeValueByOne(false);
                                mPressedStateHelper.buttonTapped(PressedStateHelper.BUTTON_DECREMENT);
                            } else {
                                ensureScrollWheelAdjusted(eventY);
                            }
                            startFadeAnimation(true);
                        }
                    } else {
                        if (mIsLongClicked) {
                            mIsLongClicked = false;
                        }
                        ensureScrollWheelAdjusted(deltaMoveY);
                        startFadeAnimation(true);
                    }
                    mIsValueChanged = false;
                    onScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE);
                }
                mVelocityTracker.recycle();
                mVelocityTracker = null;
            } break;
            case MotionEvent.ACTION_MOVE: {
                if (mIgnoreMoveEvents) {
                    break;
                }
                float currentMoveY = event.getY();
                if (mScrollState != OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    int deltaDownY = (int) Math.abs(currentMoveY - mLastDownEventY);
                    if (deltaDownY > mTouchSlop) {
                        removeAllCallbacks();
                        startFadeAnimation(false);
                        onScrollStateChange(OnScrollListener.SCROLL_STATE_TOUCH_SCROLL);
                    }
                } else {
                    int deltaMoveY = (int) ((currentMoveY - mLastDownOrMoveEventY));
                    scrollBy(0, deltaMoveY);
                    mDelegator.invalidate();
                }
                mLastDownOrMoveEventY = currentMoveY;
            } break;
            case MotionEvent.ACTION_CANCEL: {
                ensureScrollWheelAdjusted();
                startFadeAnimation(true);
                onScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE);
            } break;
        }
        return true;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        final int action = event.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                removeAllCallbacks();
                break;
        }
        return false;
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        if (!mDelegator.isEnabled() || mIsStartingAnimation) {
            return false;
        }

        if ((event.getSource() & InputDevice.SOURCE_CLASS_POINTER) != 0 && event.getAction() == MotionEvent.ACTION_SCROLL) {
            float axisValue = event.getAxisValue(MotionEvent.AXIS_VSCROLL);
            if (axisValue != 0.0f) {
                startFadeAnimation(false);
                changeValueByOne(axisValue < 0.0f);
                startFadeAnimation(true);
                return true;
            }
        }
        return false;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (!mCustomTypefaceSet) {
            if (isCharacterNumberLanguage()) {
                mInputText.setIncludeFontPadding(true);
                mPickerTypeface = mDefaultTypeface;
                mPickerSubTypeface = Typeface.create(mDefaultTypeface, Typeface.NORMAL);
                mHcfFocusedTypefaceBold = Typeface.create(mPickerTypeface, Typeface.BOLD);
                setInputTextTypeface();
            } else {
                mInputText.setIncludeFontPadding(false);
                setInputTextTypeface();
                tryComputeMaxWidth();
            }
        }
    }

    @Override
    public void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        if (gainFocus) {
            InputMethodManager inputMethodManager = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (inputMethodManager != null) {
                inputMethodManager.hideSoftInputFromWindow(mDelegator.getWindowToken(), 0);
            }

            mLastFocusedChildVirtualViewId = 1;
            if (!mWrapSelectorWheel && getValue() == getMinValue()) {
                mLastFocusedChildVirtualViewId = 2;
            }

            AccessibilityNodeProviderImpl provider = (AccessibilityNodeProviderImpl) getAccessibilityNodeProvider();
            if (mAccessibilityManager.isEnabled() && provider != null) {
                provider.performAction(mLastFocusedChildVirtualViewId, 64, null);
            }
        } else {
            AccessibilityNodeProviderImpl provider = (AccessibilityNodeProviderImpl) getAccessibilityNodeProvider();
            if (mAccessibilityManager.isEnabled() && provider != null) {
                provider.performAction(mLastFocusedChildVirtualViewId, 128, null);
            }
            mLastFocusedChildVirtualViewId = View.NO_ID;
            mLastHoveredChildVirtualViewId = Integer.MIN_VALUE;
        }

        mDelegator.invalidate();
    }

    @Override
    public void onWindowVisibilityChanged(int visibility) {
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        final int keyCode = event.getKeyCode();
        final int action = event.getAction();
        if (keyCode != KeyEvent.KEYCODE_ENTER && keyCode != KeyEvent.KEYCODE_NUMPAD_ENTER) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_UP:
                case KeyEvent.KEYCODE_DPAD_DOWN:
                    switch (action) {
                        case KeyEvent.ACTION_DOWN:
                            if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                                if (mLastFocusedChildVirtualViewId == 1) {
                                    mLastFocusedChildVirtualViewId = 2;
                                    mDelegator.invalidate();
                                    return true;
                                } else if (mLastFocusedChildVirtualViewId == 2) {
                                    if (!mWrapSelectorWheel && getValue() == getMaxValue()) {
                                        return false;
                                    }
                                    mLastFocusedChildVirtualViewId = 3;
                                    mDelegator.invalidate();
                                    return true;
                                }
                            } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                                if (mLastFocusedChildVirtualViewId != 2) {
                                    if (mLastFocusedChildVirtualViewId == 3) {
                                        mLastFocusedChildVirtualViewId = 2;
                                        mDelegator.invalidate();
                                        return true;
                                    }
                                } else if (!mWrapSelectorWheel && getValue() == getMinValue()) {
                                    return false;
                                } else {
                                    mLastFocusedChildVirtualViewId = 1;
                                    mDelegator.invalidate();
                                    return true;
                                }
                            }
                            return false;
                        case KeyEvent.ACTION_UP:
                            if (mAccessibilityManager.isEnabled()) {
                                AccessibilityNodeProviderImpl provider = (AccessibilityNodeProviderImpl) getAccessibilityNodeProvider();
                                if (provider != null) {
                                    provider.performAction(mLastFocusedChildVirtualViewId, 64, null);
                                }
                                return true;
                            }
                            return false;
                    }
                case KeyEvent.KEYCODE_DPAD_LEFT:
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    if (action == KeyEvent.ACTION_DOWN) {
                        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                            View leftView = mDelegator.focusSearch(View.FOCUS_LEFT);
                            if (leftView != null) {
                                leftView.requestFocus(View.FOCUS_LEFT);
                            }
                            return true;
                        } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                            View rightView = mDelegator.focusSearch(View.FOCUS_RIGHT);
                            if (rightView != null) {
                                rightView.requestFocus(View.FOCUS_RIGHT);
                            }
                            return true;
                        }
                    }
                    return false;
                case KeyEvent.KEYCODE_DPAD_CENTER:
                    break;
                default:
                    return false;
            }
        }

        if (action == KeyEvent.ACTION_UP) {
            if (mLastFocusedChildVirtualViewId == 2) {
                performClick();
                removeAllCallbacks();
            } else if (mFlingScroller.isFinished()) {
                if (mLastFocusedChildVirtualViewId == 1) {
                    startFadeAnimation(false);
                    changeValueByOne(false);
                    Calendar calendar = (Calendar) getMinValue().clone();
                    calendar.add(Calendar.DAY_OF_MONTH, 1);
                    if (!mWrapSelectorWheel && getValue().equals(calendar)) {
                        mLastFocusedChildVirtualViewId = 2;
                    }
                    startFadeAnimation(true);
                } else if (mLastFocusedChildVirtualViewId == 3) {
                    startFadeAnimation(false);
                    changeValueByOne(true);
                    Calendar calendar = (Calendar) getMaxValue().clone();
                    calendar.add(Calendar.DAY_OF_MONTH, -1);
                    if (!mWrapSelectorWheel && getValue().equals(calendar)) {
                        mLastFocusedChildVirtualViewId = 2;
                    }
                    startFadeAnimation(true);
                }
            }
        }
        return false;
    }

    @Override
    public boolean dispatchKeyEventPreIme(KeyEvent event) {
        return false;
    }

    @Override
    public void dispatchTrackballEvent(MotionEvent event) {
        final int action = event.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                removeAllCallbacks();
                break;
        }
    }

    @Override
    public boolean dispatchHoverEvent(MotionEvent event) {
        if (mAccessibilityManager.isEnabled()) {
            final int eventY = (int) event.getY();

            int index = 2;
            if (eventY <= mTopSelectionDividerTop) {
                index = 1;
            } else if (mBottomSelectionDividerBottom <= eventY) {
                index = 3;
            }

            final int action = event.getActionMasked();
            switch (action) {
                case MotionEvent.ACTION_HOVER_MOVE:
                case MotionEvent.ACTION_HOVER_ENTER: {
                    updateHoveredVirtualView(index);
                    return index != Integer.MIN_VALUE;
                }
                case MotionEvent.ACTION_HOVER_EXIT: {
                    if (mLastHoveredChildVirtualViewId != Integer.MIN_VALUE) {
                        updateHoveredVirtualView(Integer.MIN_VALUE);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void updateHoveredVirtualView(int id) {
        if (mLastHoveredChildVirtualViewId != id) {
            mLastHoveredChildVirtualViewId = id;
            AccessibilityNodeProviderImpl provider = (AccessibilityNodeProviderImpl) getAccessibilityNodeProvider();
            provider.sendAccessibilityEventForVirtualView(id, 128);
            provider.sendAccessibilityEventForVirtualView(mLastHoveredChildVirtualViewId, 256);
        }
    }

    @Override
    public void setSkipValuesOnLongPressEnabled(boolean enabled) {
        mSkipNumbers = enabled;
    }

    private void playSoundAndHapticFeedback() {
        if (mPickerSoundSlowIndex != 0 && mPickerSoundFastIndex != 0) {
            mAudioManager.playSoundEffect(mCurVelocity > FAST_SCROLL_VELOCITY_START ? mPickerSoundFastIndex : mPickerSoundSlowIndex);
        } else {
            mAudioManager.playSoundEffect(mPickerSoundIndex);
        }
        if (!mHapticPreDrawListener.mSkipHapticCalls) {
            mDelegator.performHapticFeedback(mPickerVibrateIndex);
            mHapticPreDrawListener.mSkipHapticCalls = true;
        }
    }

    private static class HapticPreDrawListener implements ViewTreeObserver.OnPreDrawListener {
        public boolean mSkipHapticCalls;

        private HapticPreDrawListener() {
            mSkipHapticCalls = false;
        }

        @Override
        public boolean onPreDraw() {
            mSkipHapticCalls = false;
            return true;
        }
    }

    @Override
    public void computeScroll() {
        if (!mSpringFlingRunning) {
            Scroller scroller = mFlingScroller;
            if (scroller.isFinished()) {
                scroller = mAdjustScroller;
                if (scroller.isFinished()) {
                    return;
                }
            }
            scroller.computeScrollOffset();
            int currentScrollerY = scroller.getCurrY();
            if (mPreviousScrollerY == 0) {
                mPreviousScrollerY = scroller.getStartY();
            }
            scrollBy(0, currentScrollerY - mPreviousScrollerY);
            mPreviousScrollerY = currentScrollerY;
            if (scroller.isFinished()) {
                onScrollerFinished(scroller);
            } else {
                mDelegator.invalidate();
            }
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (!enabled && mScrollState != OnScrollListener.SCROLL_STATE_IDLE) {
            stopScrollAnimation();
            onScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE);
        }
    }

    // kang
    @Override
    public void scrollBy(int x, int y) {
        Calendar[] var3 = this.mSelectorIndices;
        if (y != 0 && this.mSelectorElementHeight > 0) {
            x = y;
            if (!this.mWrapSelectorWheel) {
                x = y;
                if (this.mCurrentScrollOffset + y > this.mInitialScrollOffset) {
                    x = y;
                    if (var3[2].compareTo(this.mMinValue) <= 0) {
                        this.stopFlingAnimation();
                        x = this.mInitialScrollOffset - this.mCurrentScrollOffset;
                    }
                }
            }

            y = x;
            if (!this.mWrapSelectorWheel) {
                y = x;
                if (this.mCurrentScrollOffset + x < this.mInitialScrollOffset) {
                    y = x;
                    if (var3[2].compareTo(this.mMaxValue) >= 0) {
                        this.stopFlingAnimation();
                        y = this.mInitialScrollOffset - this.mCurrentScrollOffset;
                    }
                }
            }

            this.mCurrentScrollOffset += y;

            while(true) {
                x = this.mCurrentScrollOffset;
                if (x - this.mInitialScrollOffset < this.mValueChangeOffset) {
                    while(true) {
                        x = this.mCurrentScrollOffset;
                        if (x - this.mInitialScrollOffset > -this.mValueChangeOffset) {
                            return;
                        }

                        this.mCurrentScrollOffset = x + this.mSelectorElementHeight;
                        this.incrementSelectorIndices(var3);
                        if (!this.mIsStartingAnimation) {
                            this.setValueInternal(var3[2], true);
                            this.mIsValueChanged = true;
                            x = this.mLongPressCount;
                            if (x > 0) {
                                this.mLongPressCount = x - 1;
                            } else {
                                this.playSoundAndHapticFeedback();
                            }
                        }

                        if (!this.mWrapSelectorWheel && var3[2].compareTo(this.mMaxValue) >= 0) {
                            this.mCurrentScrollOffset = this.mInitialScrollOffset;
                        }
                    }
                }

                this.mCurrentScrollOffset = x - this.mSelectorElementHeight;
                this.decrementSelectorIndices(var3);
                if (!this.mIsStartingAnimation) {
                    this.setValueInternal(var3[2], true);
                    this.mIsValueChanged = true;
                    x = this.mLongPressCount;
                    if (x > 0) {
                        this.mLongPressCount = x - 1;
                    } else {
                        this.playSoundAndHapticFeedback();
                    }
                }

                if (!this.mWrapSelectorWheel && var3[2].compareTo(this.mMinValue) <= 0) {
                    this.mCurrentScrollOffset = this.mInitialScrollOffset;
                }
            }
        }
    }
    // kang

    @Override
    public int computeVerticalScrollOffset() {
        return mCurrentScrollOffset;
    }

    @Override
    public int computeVerticalScrollRange() {
        return (((int) TimeUnit.MILLISECONDS.toDays(mMaxValue.getTimeInMillis() - mMinValue.getTimeInMillis())) + 1) * mSelectorElementHeight;
    }

    @Override
    public int computeVerticalScrollExtent() {
        return mDelegator.getHeight();
    }

    @Override
    public void setOnValueChangedListener(SeslSpinningDatePickerSpinner.OnValueChangeListener onValueChangeListener) {
        mOnValueChangeListener = onValueChangeListener;
    }

    @Override
    public void setOnScrollListener(SeslSpinningDatePickerSpinner.OnScrollListener onScrollListener) {
        mOnScrollListener = onScrollListener;
    }

    @Override
    public void setOnSpinnerDateClickListener(SeslSpinningDatePickerSpinner.OnSpinnerDateClickListener onSpinnerDateClickListener) {
        mOnSpinnerDateClickListener = onSpinnerDateClickListener;
    }

    @Override
    public SeslSpinningDatePickerSpinner.OnSpinnerDateClickListener getOnSpinnerDateClickListener() {
        return mOnSpinnerDateClickListener;
    }

    @Override
    public void setFormatter(SeslSpinningDatePickerSpinner.Formatter formatter) {
        if (formatter == mFormatter) {
            return;
        }
        mFormatter = formatter;
        initializeSelectorWheelIndices();
    }

    @Override
    public void setValue(Calendar calendar) {
        if (!mFlingScroller.isFinished() || mSpringAnimation.isRunning()) {
            stopScrollAnimation();
        }
        setValueInternal(calendar, false);
    }

    @Override
    public void performClick() {
        stopScrollAnimation();
        if (mOnSpinnerDateClickListener != null) {
            Calendar calendar;
            SpinningDatePicker.LunarDate lunarDate;
            if (mIsLunar) {
                lunarDate = new SpinningDatePicker.LunarDate();
                calendar = convertSolarToLunar(mValue, lunarDate);
            } else {
                lunarDate = null;
                calendar = mValue;
            }
            mOnSpinnerDateClickListener.onSpinnerDateClicked(calendar, lunarDate);
        }
    }

    @Override
    public void performClick(boolean changeValue) {
        changeValueByOne(changeValue);
    }

    @Override
    public void performLongClick() {
        mIgnoreMoveEvents = true;
        mIsLongClicked = true;
    }

    // kang
    private void tryComputeMaxWidth() {
        if (this.mComputeMaxWidth) {
            byte var1 = 0;
            float var2 = 0.0F;
            int var3 = 0;

            float var4;
            float var5;
            float var6;
            for(var4 = 0.0F; var3 <= 9; var4 = var6) {
                var5 = this.mSelectorWheelPaint.measureText(formatNumberWithLocale(var3));
                var6 = var4;
                if (var5 > var4) {
                    var6 = var5;
                }

                ++var3;
            }

            float var7 = (float)((int)((float)2 * var4));
            String[] var8 = (new DateFormatSymbols(Locale.getDefault())).getShortWeekdays();
            int var9 = var8.length;
            var3 = 0;

            for(var4 = 0.0F; var3 < var9; var4 = var6) {
                String var10 = var8[var3];
                var5 = this.mSelectorWheelPaint.measureText(var10);
                var6 = var4;
                if (var5 > var4) {
                    var6 = var5;
                }

                ++var3;
            }

            String[] var13 = (new DateFormatSymbols(Locale.getDefault())).getShortMonths();
            var9 = var13.length;

            for(var3 = var1; var3 < var9; var2 = var6) {
                String var12 = var13[var3];
                var5 = this.mSelectorWheelPaint.measureText(var12);
                var6 = var2;
                if (var5 > var2) {
                    var6 = var5;
                }

                ++var3;
            }

            int var11 = (int)(var7 + var4 + var2 + this.mSelectorWheelPaint.measureText(" ") * 2.0F + this.mSelectorWheelPaint.measureText(",")) + this.mInputText.getPaddingLeft() + this.mInputText.getPaddingRight();
            var3 = var11;
            if (this.isHighContrastFontEnabled()) {
                var3 = var11 + (int)Math.ceil((double)(SeslPaintReflector.getHCTStrokeWidth(this.mSelectorWheelPaint) / 2.0F)) * 13;
            }

            if (this.mMaxWidth != var3) {
                var11 = this.mMinWidth;
                if (var3 > var11) {
                    this.mMaxWidth = var3;
                } else {
                    this.mMaxWidth = var11;
                }

                this.mDelegator.invalidate();
            }
        }
    }
    // kang

    @Override
    public boolean getWrapSelectorWheel() {
        return mWrapSelectorWheel;
    }

    @Override
    public void setWrapSelectorWheel(boolean wrap) {
        mWrapSelectorWheelPreferred = wrap;
        updateWrapSelectorWheel();
    }

    private void updateWrapSelectorWheel() {
        final boolean wrappingAllowed = (TimeUnit.MILLISECONDS.toDays(mMaxValue.getTimeInMillis() - mMinValue.getTimeInMillis()) >= mSelectorIndices.length) && mWrapSelectorWheelPreferred;
        if (mWrapSelectorWheel != wrappingAllowed) {
            mWrapSelectorWheel = wrappingAllowed;
            initializeSelectorWheelIndices();
            mDelegator.invalidate();
        }
    }

    @Override
    public void setOnLongPressUpdateInterval(long interval) {
        mLongPressUpdateInterval = interval;
    }

    @Override
    public Calendar getValue() {
        return mValue;
    }

    @Override
    public Calendar getMinValue() {
        return mMinValue;
    }

    @Override
    public void setMinValue(Calendar calendar) {
        if (mMinValue.equals(calendar)) {
            return;
        }
        clearCalendar(mMinValue, calendar);
        if (mMinValue.compareTo(mValue) > 0) {
            clearCalendar(mValue, mMinValue);
        }
        updateWrapSelectorWheel();
        initializeSelectorWheelIndices();
        tryComputeMaxWidth();
        mDelegator.invalidate();
    }

    @Override
    public Calendar getMaxValue() {
        return mMaxValue;
    }

    @Override
    public void setMaxValue(Calendar calendar) {
        if (mMaxValue.equals(calendar)) {
            return;
        }
        clearCalendar(mMaxValue, calendar);
        if (mMaxValue.compareTo(mValue) < 0) {
            clearCalendar(mValue, mMaxValue);
        }
        updateWrapSelectorWheel();
        initializeSelectorWheelIndices();
        tryComputeMaxWidth();
        mDelegator.invalidate();
    }

    @Override
    public int getMaxHeight() {
        return 0;
    }

    @Override
    public int getMaxWidth() {
        return 0;
    }

    @Override
    public int getMinHeight() {
        return 0;
    }

    @Override
    public int getMinWidth() {
        return 0;
    }

    @Override
    public void setTextSize(float size) {
        final int scaledSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, size, mContext.getResources().getDisplayMetrics());
        mTextSize = scaledSize;
        mSelectorWheelPaint.setTextSize((float) scaledSize);
        mInputText.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) mTextSize);
        tryComputeMaxWidth();
    }

    @Override
    public void setSubTextSize(float size) {
    }

    @Override
    public void setTextTypeface(Typeface typeface) {
        mCustomTypefaceSet = true;
        mPickerTypeface = typeface;
        mPickerSubTypeface = Typeface.create(typeface, Typeface.NORMAL);
        mSelectorWheelPaint.setTypeface(mPickerTypeface);
        mHcfFocusedTypefaceBold = Typeface.create(mPickerTypeface, Typeface.BOLD);
        setInputTextTypeface();
        tryComputeMaxWidth();
    }

    private void setInputTextTypeface() {
        if (mIsHcfEnabled) {
            mInputText.setTypeface(mHcfFocusedTypefaceBold);
        } else {
            mInputText.setTypeface(mPickerTypeface);
        }
    }

    private void setHcfTextAppearance(boolean bold) {
        if (mIsHcfEnabled) {
            if (bold) {
                mSelectorWheelPaint.setTypeface(mHcfFocusedTypefaceBold);
            } else {
                mSelectorWheelPaint.setTypeface(mPickerSubTypeface);
            }
        }
    }

    private static Typeface getFontTypeface(String fontFile) {
        if (!new File(fontFile).exists()) {
            return null;
        }
        try {
            return Typeface.createFromFile(fontFile);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public int getPaintFlags() {
        return mSelectorWheelPaint.getFlags();
    }

    @Override
    public void setPaintFlags(int flags) {
        if (mSelectorWheelPaint.getFlags() != flags) {
            mSelectorWheelPaint.setFlags(flags);
            mInputText.setPaintFlags(flags);
            tryComputeMaxWidth();
        }
    }

    @Override
    public void startAnimation(int delayMillis, SeslAnimationListener listener) {
        mAnimationListener = listener;
        mAlpha = mActivatedAlpha;

        mDelegator.post(new Runnable() {
            @Override
            public void run() {
                if (mSelectorElementHeight == 0) {
                    mReservedStartAnimation = true;
                    return;
                }

                mIsStartingAnimation = true;
                mFlingScroller = mCustomScroller;

                // kang
                final int i2 = (int) (((double) SeslSpinningDatePickerSpinnerDelegate.this.mSelectorElementHeight) * 5.4d);
                SeslSpinningDatePickerSpinnerDelegate.this.scrollBy(0, SeslSpinningDatePickerSpinnerDelegate.this.mSelectorElementHeight * 5);
                SeslSpinningDatePickerSpinnerDelegate.this.mDelegator.invalidate();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (!SeslSpinningDatePickerSpinnerDelegate.this.moveToFinalScrollerPosition(SeslSpinningDatePickerSpinnerDelegate.this.mFlingScroller)) {
                                    SeslSpinningDatePickerSpinnerDelegate.this.moveToFinalScrollerPosition(SeslSpinningDatePickerSpinnerDelegate.this.mAdjustScroller);
                                }
                                SeslSpinningDatePickerSpinnerDelegate.this.mPreviousScrollerY = 0;
                                SeslSpinningDatePickerSpinnerDelegate.this.mFlingScroller.startScroll(0, 0, 0, -i2, SeslSpinningDatePickerSpinnerDelegate.START_ANIMATION_SCROLL_DURATION_2016B);
                                SeslSpinningDatePickerSpinnerDelegate.this.mDelegator.invalidate();
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        SeslSpinningDatePickerSpinnerDelegate.this.moveToFinalScrollerPosition(SeslSpinningDatePickerSpinnerDelegate.this.mFlingScroller);
                                        SeslSpinningDatePickerSpinnerDelegate.this.mFlingScroller.abortAnimation();
                                        SeslSpinningDatePickerSpinnerDelegate.this.mAdjustScroller.abortAnimation();
                                        SeslSpinningDatePickerSpinnerDelegate.this.ensureScrollWheelAdjusted();
                                        SeslSpinningDatePickerSpinnerDelegate.this.mFlingScroller = SeslSpinningDatePickerSpinnerDelegate.this.mLinearScroller;
                                        SeslSpinningDatePickerSpinnerDelegate.this.mIsStartingAnimation = false;
                                        SeslSpinningDatePickerSpinnerDelegate.this.mDelegator.invalidate();
                                        SeslSpinningDatePickerSpinnerDelegate.this.startFadeAnimation(true);
                                        if (SeslSpinningDatePickerSpinnerDelegate.this.mAnimationListener != null) {
                                            SeslSpinningDatePickerSpinnerDelegate.this.mAnimationListener.onAnimationEnd();
                                        }
                                    }
                                }, START_ANIMATION_SCROLL_DURATION);
                            }
                        }, 100);
                    }
                }, delayMillis);
                // kang
            }
        });
    }

    private void stopScrollAnimation() {
        mFlingScroller.abortAnimation();
        mAdjustScroller.abortAnimation();
        mGravityScroller.abortAnimation();
        mSpringAnimation.cancel();
        mSpringFlingRunning = false;
        if (!mIsStartingAnimation && !moveToFinalScrollerPosition(mFlingScroller)) {
            moveToFinalScrollerPosition(mAdjustScroller);
        }
        ensureScrollWheelAdjusted();
    }

    private void stopFlingAnimation() {
        mFlingScroller.abortAnimation();
        mAdjustScroller.abortAnimation();
        mGravityScroller.abortAnimation();
        mSpringAnimation.cancel();
        mSpringFlingRunning = false;
    }

    private void startFadeAnimation(boolean fadeOut) {
        if (fadeOut) {
            mFadeOutAnimator.setStartDelay((mFlingScroller.isFinished() ? 0 : mFlingScroller.getDuration()) + 100);
            mColorOutAnimator.setStartDelay(mFlingScroller.isFinished() ? 0 : mFlingScroller.getDuration() + 100);
            mColorOutAnimator.start();
            mFadeOutAnimator.start();
        } else  {
            mFadeInAnimator.setFloatValues(mAlpha, mActivatedAlpha);
            mColorInAnimator.setIntValues(mTextColor, mTextColorScrolling);
            mColorOutAnimator.cancel();
            mFadeOutAnimator.cancel();
            mColorInAnimator.start();
            mFadeInAnimator.start();
        }
    }

    @Override
    public void onDetachedFromWindow() {
        mGravityScroller.abortAnimation();
        mSpringAnimation.cancel();
        mSpringFlingRunning = false;
        removeAllCallbacks();
        mDelegator.getViewTreeObserver().removeOnPreDrawListener(mHapticPreDrawListener);
    }

    @Override
    public void onAttachedToWindow() {
        mDelegator.getViewTreeObserver().addOnPreDrawListener(mHapticPreDrawListener);
    }

    // kang
    public void onDraw(Canvas var1) {
        int var2 = this.mDelegator.getRight();
        int var3 = this.mDelegator.getLeft();
        int var4 = this.mDelegator.getBottom();
        float var5 = (float)(var2 - var3) / 2.0F;
        float var6 = (float)(this.mCurrentScrollOffset - this.mSelectorElementHeight);
        Drawable var7 = this.mVirtualButtonFocusedDrawable;
        if (var7 != null && this.mScrollState == 0) {
            var3 = this.mLastFocusedChildVirtualViewId;
            if (var3 != 1) {
                if (var3 != 2) {
                    if (var3 == 3) {
                        var7.setState(this.mDelegator.getDrawableState());
                        this.mVirtualButtonFocusedDrawable.setBounds(0, this.mBottomSelectionDividerBottom, var2, var4);
                        this.mVirtualButtonFocusedDrawable.draw(var1);
                    }
                } else {
                    var7.setState(this.mDelegator.getDrawableState());
                    this.mVirtualButtonFocusedDrawable.setBounds(0, this.mTopSelectionDividerTop, var2, this.mBottomSelectionDividerBottom);
                    this.mVirtualButtonFocusedDrawable.draw(var1);
                }
            } else {
                var7.setState(this.mDelegator.getDrawableState());
                this.mVirtualButtonFocusedDrawable.setBounds(0, 0, var2, this.mTopSelectionDividerTop);
                this.mVirtualButtonFocusedDrawable.draw(var1);
            }
        }

        Calendar[] var17 = this.mSelectorIndices;
        int var8 = var17.length;

        for(var3 = 0; var3 < var8; ++var3) {
            Calendar var9 = var17[var3];
            String var18 = (String)this.mSelectorIndexToStringCache.get(var9);
            float var10 = this.mAlpha;
            float var11 = this.mIdleAlpha;
            float var12 = var10;
            if (var10 < var11) {
                var12 = var11;
            }

            label38: {
                int var13 = (int)((this.mSelectorWheelPaint.descent() - this.mSelectorWheelPaint.ascent()) / 2.0F + var6 - this.mSelectorWheelPaint.descent());
                int var14 = this.mTopSelectionDividerTop;
                int var15 = this.mInitialScrollOffset;
                if (var6 >= (float)(var14 - var15)) {
                    int var16 = this.mBottomSelectionDividerBottom;
                    if (var6 <= (float)(var15 + var16)) {
                        if (var6 <= (float)(var14 + var16) / 2.0F) {
                            var1.save();
                            var1.clipRect(0, this.mTopSelectionDividerTop, var2, this.mBottomSelectionDividerBottom);
                            this.mSelectorWheelPaint.setColor(this.mTextColor);
                            this.mSelectorWheelPaint.setTypeface(this.mPickerTypeface);
                            var11 = (float)var13;
                            var1.drawText(var18, var5, var11, this.mSelectorWheelPaint);
                            var1.restore();
                            var1.save();
                            var1.clipRect(0, 0, var2, this.mTopSelectionDividerTop);
                            this.mSelectorWheelPaint.setTypeface(this.mPickerSubTypeface);
                            this.mSelectorWheelPaint.setAlpha((int)(var12 * 255.0F * this.mInitialAlpha));
                            var1.drawText(var18, var5, var11, this.mSelectorWheelPaint);
                            var1.restore();
                        } else {
                            var1.save();
                            var1.clipRect(0, this.mTopSelectionDividerTop, var2, this.mBottomSelectionDividerBottom);
                            this.mSelectorWheelPaint.setTypeface(this.mPickerTypeface);
                            this.mSelectorWheelPaint.setColor(this.mTextColor);
                            var11 = (float)var13;
                            var1.drawText(var18, var5, var11, this.mSelectorWheelPaint);
                            var1.restore();
                            var1.save();
                            var1.clipRect(0, this.mBottomSelectionDividerBottom, var2, var4);
                            this.mSelectorWheelPaint.setAlpha((int)(var12 * 255.0F * this.mInitialAlpha));
                            this.mSelectorWheelPaint.setTypeface(this.mPickerSubTypeface);
                            var1.drawText(var18, var5, var11, this.mSelectorWheelPaint);
                            var1.restore();
                        }
                        break label38;
                    }
                }

                var1.save();
                this.mSelectorWheelPaint.setAlpha((int)(var12 * 255.0F * this.mInitialAlpha));
                this.mSelectorWheelPaint.setTypeface(this.mPickerSubTypeface);
                var1.drawText(var18, var5, (float)var13, this.mSelectorWheelPaint);
                var1.restore();
            }

            var6 += (float)this.mSelectorElementHeight;
        }

    }
    // kang

    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        event.setClassName(SeslSpinningDatePickerSpinner.class.getName());
        event.setScrollable(true);
        event.setScrollY(((int) TimeUnit.MILLISECONDS.toDays(mValue.getTimeInMillis() - mMinValue.getTimeInMillis())) * mSelectorElementHeight);
        event.setMaxScrollY(((int) TimeUnit.MILLISECONDS.toDays(mMaxValue.getTimeInMillis() - mMinValue.getTimeInMillis())) * mSelectorElementHeight);
    }

    @Override
    public void onPopulateAccessibilityEvent(AccessibilityEvent event) {
        AccessibilityNodeProviderImpl provider = (AccessibilityNodeProviderImpl) getAccessibilityNodeProvider();
        event.getText().add(provider.getVirtualCurrentButtonText());
    }

    @Override
    public AccessibilityNodeProvider getAccessibilityNodeProvider() {
        if (mAccessibilityNodeProvider == null) {
            mAccessibilityNodeProvider = new AccessibilityNodeProviderImpl();
        }
        return mAccessibilityNodeProvider;
    }

    @Override
    public void setLunar(boolean isLunar, boolean isLeapMonth) {
        mIsLunar = isLunar;
        mIsLeapMonth = isLeapMonth;
        if (isLunar && mSolarLunarConverter == null) {
            mPathClassLoader = SpinningDatePicker.LunarUtils.getPathClassLoader(mContext);
            mSolarLunarConverter = SeslFeatureReflector.getSolarLunarConverter(mPathClassLoader);
        } else  {
            mPathClassLoader = null;
            mSolarLunarConverter = null;
        }
    }

    private int makeMeasureSpec(int measureSpec, int maxSize) {
        if (maxSize == SIZE_UNSPECIFIED) {
            return measureSpec;
        }
        final int size = View.MeasureSpec.getSize(measureSpec);
        final int mode = View.MeasureSpec.getMode(measureSpec);
        switch (mode) {
            case View.MeasureSpec.EXACTLY:
                return measureSpec;
            case View.MeasureSpec.AT_MOST:
                return View.MeasureSpec.makeMeasureSpec(Math.min(size, maxSize), View.MeasureSpec.EXACTLY);
            case View.MeasureSpec.UNSPECIFIED:
                return View.MeasureSpec.makeMeasureSpec(maxSize, View.MeasureSpec.EXACTLY);
            default:
                throw new IllegalArgumentException("Unknown measure mode: " + mode);
        }
    }

    private int resolveSizeAndStateRespectingMinSize(int minSize, int measuredSize, int measureSpec) {
        if (minSize != SIZE_UNSPECIFIED) {
            final int desiredWidth = Math.max(minSize, measuredSize);
            return View.resolveSizeAndState(desiredWidth, measureSpec, 0);
        } else {
            return measuredSize;
        }
    }

    private void initializeSelectorWheelIndices() {
        this.mSelectorIndexToStringCache.clear();
        Calendar[] selectorIndices = mSelectorIndices;
        Calendar value = getValue();
        for (int i = 0; i < mSelectorIndices.length; i++) {
            Calendar calendar = (Calendar) value.clone();
            calendar.add(Calendar.DAY_OF_MONTH, i - 2);
            if (mWrapSelectorWheel) {
                calendar = getWrappedSelectorIndex(calendar);
            }
            selectorIndices[i] = calendar;
            ensureCachedScrollSelectorValue(selectorIndices[i]);
        }
    }

    // kang
    private void setValueInternal(Calendar calendar, boolean notifyChange) {
        Calendar var4;
        if (this.mWrapSelectorWheel) {
            var4 = this.getWrappedSelectorIndex((Calendar)calendar);
        } else {
            if (calendar.compareTo(this.mMinValue) < 0) {
                calendar = (Calendar) this.mMinValue.clone();
            }

            if (calendar.compareTo(this.mMaxValue) > 0) {
                calendar = (Calendar) this.mMaxValue.clone();
            }

            var4 = calendar;
        }

        Calendar var3 = (Calendar)this.mValue.clone();
        this.clearCalendar(this.mValue, var4);
        if (notifyChange) {
            this.notifyChange(var3);
        }

        this.initializeSelectorWheelIndices();
        this.mDelegator.invalidate();
    }

    private void changeValueByOne(boolean increment) {
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
            } else if (increment) {
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
            this.mLongPressUpdateInterval = DEFAULT_LONG_PRESS_UPDATE_INTERVAL;
        }
        int i2 = this.mChangeValueBy;
        this.mLongPressCount = i2 - 1;
        if (increment) {
            this.mFlingScroller.startScroll(0, 0, 0, (-this.mSelectorElementHeight) * i2, i);
        } else {
            this.mFlingScroller.startScroll(0, 0, 0, this.mSelectorElementHeight * i2, i);
        }
        this.mDelegator.invalidate();
    }
    // kang

    private void initializeSelectorWheel() {
        if (this.mIsStartingAnimation) {
            if (!moveToFinalScrollerPosition(mFlingScroller)) {
                moveToFinalScrollerPosition(mAdjustScroller);
            }
            stopScrollAnimation();
        } else {
            initializeSelectorWheelIndices();
        }
        int totalTextHeight = mTextSize * 3;
        float totalTextGapHeight = (mDelegator.getBottom() - mDelegator.getTop()) - totalTextHeight;
        mSelectorTextGapHeight = (int) (totalTextGapHeight / 3 + 0.5f);
        mSelectorElementHeight = mTextSize + mSelectorTextGapHeight;
        if (mModifiedTxtHeight > mSelectorElementHeight) {
            mModifiedTxtHeight = mDelegator.getHeight() / 3;
        }
        mValueChangeOffset = mModifiedTxtHeight;
        mInitialScrollOffset = (mInputText.getTop() + (mModifiedTxtHeight / 2)) - mSelectorElementHeight;;
        mCurrentScrollOffset = mInitialScrollOffset;
        ((SeslSpinningDatePickerSpinner.CustomEditText) mInputText).setEditTextPosition(((int) (((mSelectorWheelPaint.descent() - mSelectorWheelPaint.ascent()) / 2.0f) - mSelectorWheelPaint.descent())) - (mInputText.getBaseline() - (mModifiedTxtHeight / 2)));
        if (mReservedStartAnimation) {
            startAnimation(0, mAnimationListener);
            mReservedStartAnimation = false;
        }
    }

    private void onScrollerFinished(Scroller scroller) {
        if (scroller == mFlingScroller) {
            onScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE);
        }
    }

    private void onScrollStateChange(int scrollState) {
        if (mScrollState == scrollState) {
            return;
        }
        mScrollState = scrollState;
        if (mOnScrollListener != null) {
            mOnScrollListener.onScrollStateChange(mDelegator, scrollState);
        }
    }

    // kang
    private void fling(int velocityY) {
        if (!this.mWrapSelectorWheel && velocityY > 0 && getValue().equals(getMinValue())) {
            startFadeAnimation(true);
        } else if (this.mWrapSelectorWheel || velocityY >= 0 || !getValue().equals(getMaxValue())) {
            this.mPreviousScrollerY = 0;
            float f = (float) velocityY;
            Math.round((((float) Math.abs(velocityY)) / ((float) this.mMaximumFlingVelocity)) * f);
            this.mPreviousSpringY = (float) this.mCurrentScrollOffset;
            this.mSpringAnimation.setStartVelocity(f);
            this.mGravityScroller.forceFinished(true);
            this.mGravityScroller.fling(0, this.mCurrentScrollOffset, 0, velocityY, 0, 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
            int round = Math.round(((float) (this.mGravityScroller.getFinalY() + this.mCurrentScrollOffset)) / ((float) this.mSelectorElementHeight));
            int i3 = this.mSelectorElementHeight;
            int i4 = this.mInitialScrollOffset;
            int i5 = (round * i3) + i4;
            int i2;
            if (velocityY > 0) {
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

    private Calendar getWrappedSelectorIndex(Calendar calendar) {
        if (calendar.compareTo(this.mMaxValue) > 0) {
            Calendar calendar2 = (Calendar) this.mMinValue.clone();
            calendar2.add(5, ((int) TimeUnit.MILLISECONDS.toDays(calendar.getTimeInMillis() - this.mMinValue.getTimeInMillis())) % (((int) TimeUnit.MILLISECONDS.toDays(this.mMaxValue.getTimeInMillis() - this.mMinValue.getTimeInMillis())) + 1));
            return calendar2;
        } else if (calendar.compareTo(this.mMinValue) >= 0) {
            return calendar;
        } else {
            Calendar calendar3 = (Calendar) this.mMaxValue.clone();
            calendar3.add(5, -(((int) TimeUnit.MILLISECONDS.toDays(this.mMaxValue.getTimeInMillis() - calendar.getTimeInMillis())) % (((int) TimeUnit.MILLISECONDS.toDays(this.mMaxValue.getTimeInMillis() - this.mMinValue.getTimeInMillis())) + 1)));
            return calendar3;
        }
    }

    private void incrementSelectorIndices(Calendar[] calendarArr) {
        System.arraycopy(calendarArr, 1, calendarArr, 0, calendarArr.length - 1);
        Calendar calendar = (Calendar) calendarArr[calendarArr.length - 2].clone();
        calendar.add(5, 1);
        if (this.mWrapSelectorWheel && calendar.compareTo(this.mMaxValue) > 0) {
            clearCalendar(calendar, this.mMinValue);
        }
        calendarArr[calendarArr.length - 1] = calendar;
        ensureCachedScrollSelectorValue(calendar);
    }

    private void decrementSelectorIndices(Calendar[] calendarArr) {
        System.arraycopy(calendarArr, 0, calendarArr, 1, calendarArr.length - 1);
        Calendar calendar = (Calendar) calendarArr[1].clone();
        calendar.add(5, -1);
        if (this.mWrapSelectorWheel && calendar.compareTo(this.mMinValue) < 0) {
            clearCalendar(calendar, this.mMaxValue);
        }
        calendarArr[0] = calendar;
        ensureCachedScrollSelectorValue(calendar);
    }
    // kang

    private void ensureCachedScrollSelectorValue(Calendar calendar) {
        String scrollSelectorValue = mSelectorIndexToStringCache.get(calendar);
        if (scrollSelectorValue != null) {
            return;
        }
        if (calendar.compareTo(mMinValue) >= 0 && calendar.compareTo(mMaxValue) <= 0) {
            if (mIsLunar) {
                scrollSelectorValue = formatDateForLunar(calendar);
            } else {
                scrollSelectorValue = formatDate(calendar);
            }
        } else {
            scrollSelectorValue = "";
        }
        mSelectorIndexToStringCache.put(calendar, scrollSelectorValue);
    }

    // kang
    private String formatDateForLunar(Calendar calendar) {
        String str;
        Calendar calendar2 = (Calendar) calendar.clone();
        SpinningDatePicker.LunarDate lunarDate = new SpinningDatePicker.LunarDate();
        convertSolarToLunar(calendar, lunarDate);
        SeslSpinningDatePickerSpinner.Formatter formatter = this.mFormatter;
        if (formatter == null) {
            str = formatDateWithLocale(calendar2);
        } else if (formatter instanceof SeslSpinningDatePickerSpinner.DateFormatter) {
            str = ((SeslSpinningDatePickerSpinner.DateFormatter) formatter).format(calendar2, this.mContext);
        } else {
            str = formatter.format(calendar2);
        }
        String dayWithLocale = getDayWithLocale(lunarDate.day);
        String formatDayWithLocale = formatDayWithLocale(calendar2);
        String monthWithLocale = getMonthWithLocale(lunarDate.month);
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

    private String formatDateForLunarForAccessibility(Calendar calendar) {
        String str;
        Calendar calendar2 = (Calendar) calendar.clone();
        SpinningDatePicker.LunarDate lunarDate = new SpinningDatePicker.LunarDate();
        convertSolarToLunar(calendar, lunarDate);
        SeslSpinningDatePickerSpinner.Formatter formatter = this.mFormatter;
        if (formatter == null) {
            str = formatDateWithLocaleForAccessibility(calendar2);
        } else if (formatter instanceof SeslSpinningDatePickerSpinner.DateFormatter) {
            str = ((SeslSpinningDatePickerSpinner.DateFormatter) formatter).formatForAccessibility(calendar2, this.mContext);
        } else {
            str = formatter.format(calendar2);
        }
        String dayWithLocale = getDayWithLocale(lunarDate.day);
        String formatDayWithLocale = formatDayWithLocale(calendar2);
        String monthWithLocaleForAccessibility = getMonthWithLocaleForAccessibility(lunarDate.month);
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
    // kang

    private String formatDate(Calendar calendar) {
        if (mFormatter == null) {
            return formatDateWithLocale(calendar);
        }
        if (mFormatter instanceof SeslSpinningDatePickerSpinner.DateFormatter) {
            return ((SeslSpinningDatePickerSpinner.DateFormatter) mFormatter).format(calendar, mContext);
        }
        return mFormatter.format(calendar);
    }

    private String formatDateForAccessibility(Calendar calendar) {
        if (mFormatter == null) {
            return formatDateWithLocale(calendar);
        }
        if (mFormatter instanceof SeslSpinningDatePickerSpinner.DateFormatter) {
            return ((SeslSpinningDatePickerSpinner.DateFormatter) mFormatter).formatForAccessibility(calendar, mContext);
        }
        return mFormatter.format(calendar);
    }

    private void validateInputTextView(View v) {
        String str = String.valueOf(((TextView) v).getText());
        Calendar current = getSelectedPos(str.toString());
        if (!TextUtils.isEmpty(str) && !mValue.equals(current)) {
            setValueInternal(current, true);
        }
    }

    private void notifyChange(Calendar calendar) {
        if (mAccessibilityManager.isEnabled() && !mIsStartingAnimation) {
            final Calendar selectorIndex = getWrappedSelectorIndex(mValue);
            if (selectorIndex.compareTo(mMaxValue) <= 0) {
                if (mIsLunar) {
                    formatDateForLunarForAccessibility(selectorIndex);
                } else {
                    formatDateForAccessibility(selectorIndex);
                }
            }
            mDelegator.sendAccessibilityEvent(4);
        }

        if (mOnValueChangeListener != null) {
            if (mIsLunar) {
                SpinningDatePicker.LunarDate lunarDate = new SpinningDatePicker.LunarDate();
                mOnValueChangeListener.onValueChange(mDelegator, convertSolarToLunar(calendar, null), convertSolarToLunar(mValue, lunarDate), lunarDate.isLeapMonth, lunarDate);
            } else  {
                mOnValueChangeListener.onValueChange(mDelegator, calendar, mValue, false, null);
            }
        }
    }

    private void postChangeCurrentByOneFromLongPress(boolean increment, long delayMillis) {
        if (mChangeCurrentByOneFromLongPressCommand == null) {
            mChangeCurrentByOneFromLongPressCommand = new ChangeCurrentByOneFromLongPressCommand();
        } else {
            mDelegator.removeCallbacks(mChangeCurrentByOneFromLongPressCommand);
        }
        mIsLongPressed = true;
        mLongPressed_FIRST_SCROLL = true;
        mChangeCurrentByOneFromLongPressCommand.setStep(increment);
        mDelegator.postDelayed(mChangeCurrentByOneFromLongPressCommand, delayMillis);
    }

    private void removeChangeCurrentByOneFromLongPress() {
        if (mIsLongPressed) {
            mIsLongPressed = false;
            mCurrentScrollOffset = mInitialScrollOffset;
        }
        mLongPressed_FIRST_SCROLL = false;
        mLongPressed_SECOND_SCROLL = false;
        mLongPressed_THIRD_SCROLL = false;
        mChangeValueBy = DEFAULT_CHANGE_VALUE_BY;
        mLongPressUpdateInterval = DEFAULT_LONG_PRESS_UPDATE_INTERVAL;
        if (mChangeCurrentByOneFromLongPressCommand != null) {
            mDelegator.removeCallbacks(mChangeCurrentByOneFromLongPressCommand);
        }
    }

    private void removeAllCallbacks() {
        if (mIsLongPressed) {
            mIsLongPressed = false;
            mCurrentScrollOffset = this.mInitialScrollOffset;
        }
        mLongPressed_FIRST_SCROLL = false;
        mLongPressed_SECOND_SCROLL = false;
        mLongPressed_THIRD_SCROLL = false;
        mChangeValueBy = DEFAULT_CHANGE_VALUE_BY;
        mLongPressUpdateInterval = DEFAULT_LONG_PRESS_UPDATE_INTERVAL;
        if (mChangeCurrentByOneFromLongPressCommand != null) {
            mDelegator.removeCallbacks(this.mChangeCurrentByOneFromLongPressCommand);
        }
        mPressedStateHelper.cancel();
    }

    private Calendar getSelectedPos(String str) {
        Calendar calendar = SeslSpinningDatePickerSpinner.getDateFormatter().parse(str);
        return calendar != null ? calendar : (Calendar) mMinValue.clone();
    }

    private boolean ensureScrollWheelAdjusted() {
        return ensureScrollWheelAdjusted(0);
    }

    // kang
    private boolean ensureScrollWheelAdjusted(int var1) {
        int var2 = this.mInitialScrollOffset;
        if (var2 == -2147483648) {
            return false;
        } else {
            int var3 = var2 - this.mCurrentScrollOffset;
            if (var3 == 0) {
                this.mIsValueChanged = false;
                return false;
            } else {
                label31: {
                    label30: {
                        label37: {
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
                this.mDelegator.invalidate();
                this.mIsValueChanged = false;
                return true;
            }
        }
    }
    // kang

    class PressedStateHelper implements Runnable {
        public static final int BUTTON_DECREMENT = 2;
        public static final int BUTTON_INCREMENT = 1;
        private final int MODE_PRESS = 1;
        private final int MODE_TAPPED = 2;
        private int mManagedButton;
        private int mMode;

        public void cancel() {
            mMode = 0;
            mManagedButton = 0;
            mDelegator.removeCallbacks(this);
            if (mIncrementVirtualButtonPressed) {
                mIncrementVirtualButtonPressed = false;
                mDelegator.invalidate(0, mBottomSelectionDividerBottom, mDelegator.getRight(), mDelegator.getBottom());
            }
            if (mDecrementVirtualButtonPressed) {
                mDecrementVirtualButtonPressed = false;
                mDelegator.invalidate(0, 0, mDelegator.getRight(), mTopSelectionDividerTop);
            }
        }

        public void buttonPressDelayed(int button) {
            cancel();
            mMode = MODE_PRESS;
            mManagedButton = button;
            mDelegator.postDelayed(this, ViewConfiguration.getTapTimeout());
        }

        public void buttonTapped(int button) {
            cancel();
            mMode = MODE_TAPPED;
            mManagedButton = button;
            mDelegator.post(this);
        }

        @Override
        public void run() {
            switch (mMode) {
                case MODE_PRESS: {
                    switch (mManagedButton) {
                        case BUTTON_INCREMENT: {
                            mIncrementVirtualButtonPressed = true;
                            mDelegator.invalidate(0, mBottomSelectionDividerBottom, mDelegator.getRight(), mDelegator.getBottom());
                        } break;
                        case BUTTON_DECREMENT: {
                            mDecrementVirtualButtonPressed = true;
                            mDelegator.invalidate(0, 0, mDelegator.getRight(), mTopSelectionDividerTop);
                        }
                    }
                } break;
                case MODE_TAPPED: {
                    switch (mManagedButton) {
                        case BUTTON_INCREMENT: {
                            if (!mIncrementVirtualButtonPressed) {
                                mDelegator.postDelayed(this, ViewConfiguration.getPressedStateDuration());
                            }
                            mIncrementVirtualButtonPressed ^= true;
                            mDelegator.invalidate(0, mBottomSelectionDividerBottom, mDelegator.getRight(), mDelegator.getBottom());
                        } break;
                        case BUTTON_DECREMENT: {
                            if (!mDecrementVirtualButtonPressed) {
                                mDelegator.postDelayed(this, ViewConfiguration.getPressedStateDuration());
                            }
                            mDecrementVirtualButtonPressed ^= true;
                            mDelegator.invalidate(0, 0, mDelegator.getRight(), mTopSelectionDividerTop);
                        }
                    }
                } break;
            }
        }
    }

    class ChangeCurrentByOneFromLongPressCommand implements Runnable {
        private boolean mIncrement;

        private void setStep(boolean increment) {
            mIncrement = increment;
        }

        @Override
        public void run() {
            changeValueByOne(mIncrement);
            mDelegator.postDelayed(this, mLongPressUpdateInterval);
        }
    }

    class AccessibilityNodeProviderImpl extends AccessibilityNodeProvider {
        private static final int UNDEFINED = Integer.MIN_VALUE;
        private static final int VIRTUAL_VIEW_ID_DECREMENT = 1;
        private static final int VIRTUAL_VIEW_ID_INCREMENT = 3;
        private static final int VIRTUAL_VIEW_ID_CENTER = 2;
        private final Rect mTempRect = new Rect();
        private final int[] mTempArray = new int[2];
        private int mAccessibilityFocusedView = Integer.MIN_VALUE;

        @Override
        public AccessibilityNodeInfo createAccessibilityNodeInfo(int virtualViewId) {
            switch (virtualViewId) {
                case View.NO_ID:
                    return createAccessibilityNodeInfoForDatePickerWidget(mDelegator.getScrollX(), mDelegator.getScrollY(), mDelegator.getScrollX() + (mDelegator.getRight() - mDelegator.getLeft()), mDelegator.getScrollY() + (mDelegator.getBottom() - mDelegator.getTop()));
                case VIRTUAL_VIEW_ID_DECREMENT:
                    return createAccessibilityNodeInfoForVirtualButton(VIRTUAL_VIEW_ID_DECREMENT, getVirtualDecrementButtonText(), mDelegator.getScrollX(), mDelegator.getScrollY(), mDelegator.getScrollX() + (mDelegator.getRight() - mDelegator.getLeft()), mTopSelectionDividerTop + mSelectionDividerHeight);
                case VIRTUAL_VIEW_ID_CENTER:
                    return createAccessibiltyNodeInfoForCenter(mDelegator.getScrollX(), mTopSelectionDividerTop + mSelectionDividerHeight, mDelegator.getScrollX() + (mDelegator.getRight() - mDelegator.getLeft()), mBottomSelectionDividerBottom - mSelectionDividerHeight);
                case VIRTUAL_VIEW_ID_INCREMENT:
                    return createAccessibilityNodeInfoForVirtualButton(VIRTUAL_VIEW_ID_INCREMENT, getVirtualIncrementButtonText(), mDelegator.getScrollX(), mBottomSelectionDividerBottom - mSelectionDividerHeight, mDelegator.getScrollX() + (mDelegator.getRight() - mDelegator.getLeft()), mDelegator.getScrollY() + (mDelegator.getBottom() - mDelegator.getTop()));
            }
            AccessibilityNodeInfo info = super.createAccessibilityNodeInfo(virtualViewId);
            if (info == null) {
                return AccessibilityNodeInfo.obtain();
            }
            return info;
        }

        @Override
        public List<AccessibilityNodeInfo> findAccessibilityNodeInfosByText(String searched, int virtualViewId) {
            if (TextUtils.isEmpty(searched)) {
                return Collections.emptyList();
            }
            String searchedLowerCase = searched.toLowerCase();
            List<AccessibilityNodeInfo> result = new ArrayList<AccessibilityNodeInfo>();
            switch (virtualViewId) {
                case View.NO_ID: {
                    findAccessibilityNodeInfosByTextInChild(searchedLowerCase, VIRTUAL_VIEW_ID_DECREMENT, result);
                    findAccessibilityNodeInfosByTextInChild(searchedLowerCase, VIRTUAL_VIEW_ID_CENTER, result);
                    findAccessibilityNodeInfosByTextInChild(searchedLowerCase, VIRTUAL_VIEW_ID_INCREMENT, result);
                    return result;
                }
                case VIRTUAL_VIEW_ID_DECREMENT:
                case VIRTUAL_VIEW_ID_INCREMENT:
                case VIRTUAL_VIEW_ID_CENTER: {
                    findAccessibilityNodeInfosByTextInChild(searchedLowerCase, virtualViewId, result);
                    return result;
                }
            }
            return super.findAccessibilityNodeInfosByText(searched, virtualViewId);
        }

        @Override
        public boolean performAction(int virtualViewId, int action, Bundle arguments) {
            switch (virtualViewId) {
                case View.NO_ID: {
                    switch (action) {
                        case AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS: {
                            if (mAccessibilityFocusedView != virtualViewId) {
                                mAccessibilityFocusedView = virtualViewId;
                                SeslViewReflector.requestAccessibilityFocus(mDelegator);
                                return true;
                            }
                        } return false;
                        case AccessibilityNodeInfo.ACTION_CLEAR_ACCESSIBILITY_FOCUS: {
                            if (mAccessibilityFocusedView == virtualViewId) {
                                mAccessibilityFocusedView = UNDEFINED;
                                SeslViewReflector.clearAccessibilityFocus(mDelegator);
                                return true;
                            }
                            return false;
                        }
                        case AccessibilityNodeInfo.ACTION_SCROLL_FORWARD: {
                            if (mDelegator.isEnabled() && (getWrapSelectorWheel() || getValue().compareTo(getMaxValue()) < 0)) {
                                startFadeAnimation(false);
                                changeValueByOne(true);
                                startFadeAnimation(true);
                                return true;
                            }
                        } return false;
                        case AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD: {
                            if (mDelegator.isEnabled() && (getWrapSelectorWheel() || getValue().compareTo(getMinValue()) > 0)) {
                                startFadeAnimation(false);
                                changeValueByOne(false);
                                startFadeAnimation(true);
                                return true;
                            }
                        } return false;
                    }
                } break;
                case VIRTUAL_VIEW_ID_CENTER: {
                    switch (action) {
                        case AccessibilityNodeInfo.ACTION_FOCUS: {
                            if (mDelegator.isEnabled() && !mInputText.isFocused()) {
                                return mInputText.requestFocus();
                            }
                        } break;
                        case AccessibilityNodeInfo.ACTION_CLEAR_FOCUS: {
                            if (mDelegator.isEnabled() && mInputText.isFocused()) {
                                mInputText.clearFocus();
                                return true;
                            }
                            return false;
                        }
                        case AccessibilityNodeInfo.ACTION_CLICK: {
                            if (mDelegator.isEnabled()) {
                                performClick();
                                return true;
                            }
                            return false;
                        }
                        case AccessibilityNodeInfo.ACTION_LONG_CLICK: {
                            if (mDelegator.isEnabled()) {
                                performLongClick();
                                return true;
                            }
                            return false;
                        }
                        case AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS: {
                            if (mAccessibilityFocusedView != virtualViewId) {
                                mAccessibilityFocusedView = virtualViewId;
                                sendAccessibilityEventForVirtualView(virtualViewId, AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED);
                                mDelegator.invalidate(0, mTopSelectionDividerTop, mDelegator.getRight(), mBottomSelectionDividerBottom);
                                return true;
                            }
                        } return false;
                        case  AccessibilityNodeInfo.ACTION_CLEAR_ACCESSIBILITY_FOCUS: {
                            if (mAccessibilityFocusedView == virtualViewId) {
                                mAccessibilityFocusedView = UNDEFINED;
                                sendAccessibilityEventForVirtualView(virtualViewId, AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUS_CLEARED);
                                mDelegator.invalidate(0, mTopSelectionDividerTop, mDelegator.getRight(), mBottomSelectionDividerBottom);
                                return true;
                            }
                        } return false;
                        default: {
                            return mInputText.performAccessibilityAction(action, arguments);
                        }
                    }
                } return false;
                case VIRTUAL_VIEW_ID_INCREMENT: {
                    switch (action) {
                        case AccessibilityNodeInfo.ACTION_CLICK: {
                            if (mDelegator.isEnabled()) {
                                startFadeAnimation(false);
                                changeValueByOne(true);
                                sendAccessibilityEventForVirtualView(virtualViewId, AccessibilityEvent.TYPE_VIEW_CLICKED);
                                startFadeAnimation(true);
                                return true;
                            }
                        } return false;
                        case AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS: {
                            if (mAccessibilityFocusedView != virtualViewId) {
                                mAccessibilityFocusedView = virtualViewId;
                                sendAccessibilityEventForVirtualView(virtualViewId, AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED);
                                mDelegator.invalidate(0, mBottomSelectionDividerBottom, mDelegator.getRight(), mDelegator.getBottom());
                                return true;
                            }
                        } return false;
                        case  AccessibilityNodeInfo.ACTION_CLEAR_ACCESSIBILITY_FOCUS: {
                            if (mAccessibilityFocusedView == virtualViewId) {
                                mAccessibilityFocusedView = UNDEFINED;
                                sendAccessibilityEventForVirtualView(virtualViewId, AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUS_CLEARED);
                                mDelegator.invalidate(0, mBottomSelectionDividerBottom, mDelegator.getRight(), mDelegator.getBottom());
                                return true;
                            }
                        } return false;
                    }
                } return false;
                case VIRTUAL_VIEW_ID_DECREMENT: {
                    switch (action) {
                        case AccessibilityNodeInfo.ACTION_CLICK: {
                            if (mDelegator.isEnabled()) {
                                startFadeAnimation(false);
                                changeValueByOne(false);
                                sendAccessibilityEventForVirtualView(virtualViewId, AccessibilityEvent.TYPE_VIEW_CLICKED);
                                startFadeAnimation(true);
                                return true;
                            }
                        } return false;
                        case AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS: {
                            if (mAccessibilityFocusedView != virtualViewId) {
                                mAccessibilityFocusedView = virtualViewId;
                                sendAccessibilityEventForVirtualView(virtualViewId, AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED);
                                mDelegator.invalidate(0, 0, mDelegator.getRight(), mTopSelectionDividerTop);
                                return true;
                            }
                        } return false;
                        case  AccessibilityNodeInfo.ACTION_CLEAR_ACCESSIBILITY_FOCUS: {
                            if (mAccessibilityFocusedView == virtualViewId) {
                                mAccessibilityFocusedView = UNDEFINED;
                                sendAccessibilityEventForVirtualView(virtualViewId, AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUS_CLEARED);
                                mDelegator.invalidate(0, 0, mDelegator.getRight(), mTopSelectionDividerTop);
                                return true;
                            }
                        } return false;
                    }
                } return false;
            }
            return super.performAction(virtualViewId, action, arguments);
        }

        public void sendAccessibilityEventForVirtualView(int virtualViewId, int eventType) {
            switch (virtualViewId) {
                case VIRTUAL_VIEW_ID_DECREMENT: {
                    if (hasVirtualDecrementButton()) {
                        sendAccessibilityEventForVirtualButton(virtualViewId, eventType, getVirtualDecrementButtonText());
                    }
                } break;
                case VIRTUAL_VIEW_ID_CENTER: {
                    sendAccessibilityEventForCenter(eventType);
                } break;
                case VIRTUAL_VIEW_ID_INCREMENT: {
                    if (hasVirtualIncrementButton()) {
                        sendAccessibilityEventForVirtualButton(virtualViewId, eventType, getVirtualIncrementButtonText());
                    }
                } break;
            }
        }

        private void sendAccessibilityEventForCenter(int eventType) {
            if (mAccessibilityManager.isEnabled()) {
                AccessibilityEvent event = AccessibilityEvent.obtain(eventType);
                event.setPackageName(mContext.getPackageName());
                event.getText().add(getVirtualCurrentButtonText() + mContext.getString(R.string.sesl_date_picker_switch_to_calendar_description));
                event.setEnabled(mDelegator.isEnabled());
                event.setSource(mDelegator, VIRTUAL_VIEW_ID_CENTER);
                mDelegator.requestSendAccessibilityEvent(mDelegator, event);
            }
        }

        private void sendAccessibilityEventForVirtualButton(int virtualViewId, int eventType, String text) {
            if (mAccessibilityManager.isEnabled()) {
                AccessibilityEvent event = AccessibilityEvent.obtain(eventType);
                event.setClassName(Button.class.getName());
                event.setPackageName(mContext.getPackageName());
                event.getText().add(text);
                event.setEnabled(mDelegator.isEnabled());
                event.setSource(mDelegator, virtualViewId);
                mDelegator.requestSendAccessibilityEvent(mDelegator, event);
            }
        }

        private void findAccessibilityNodeInfosByTextInChild(String searchedLowerCase, int virtualViewId, List<AccessibilityNodeInfo> outResult) {
            switch (virtualViewId) {
                case VIRTUAL_VIEW_ID_DECREMENT: {
                    String text = getVirtualDecrementButtonText();
                    if (!TextUtils.isEmpty(text) && text.toLowerCase().contains(searchedLowerCase)) {
                        outResult.add(createAccessibilityNodeInfo(VIRTUAL_VIEW_ID_DECREMENT));
                    }
                } return;
                case VIRTUAL_VIEW_ID_CENTER: {
                    String text = getVirtualCurrentButtonText();
                    if (!TextUtils.isEmpty(text) && text.toLowerCase().contains(searchedLowerCase)) {
                        outResult.add(createAccessibilityNodeInfo(VIRTUAL_VIEW_ID_CENTER));
                    }
                } return;
                case VIRTUAL_VIEW_ID_INCREMENT: {
                    String text = getVirtualIncrementButtonText();
                    if (!TextUtils.isEmpty(text) && text.toLowerCase().contains(searchedLowerCase)) {
                        outResult.add(createAccessibilityNodeInfo(VIRTUAL_VIEW_ID_INCREMENT));
                    }
                } return;
            }
        }

        private AccessibilityNodeInfo createAccessibiltyNodeInfoForCenter(int left, int top, int right, int bottom) {
            AccessibilityNodeInfo info = mInputText.createAccessibilityNodeInfo();
            info.setPackageName(mContext.getPackageName());
            info.setSource(mDelegator, VIRTUAL_VIEW_ID_CENTER);
            info.setParent(mDelegator);
            info.setText(getVirtualCurrentButtonText() + mContext.getString(R.string.sesl_date_picker_switch_to_calendar_description));
            info.setClickable(true);
            info.setEnabled(mDelegator.isEnabled());
            if (mAccessibilityFocusedView != VIRTUAL_VIEW_ID_CENTER) {
                info.setAccessibilityFocused(false);
                info.addAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS);
            }
            if (mAccessibilityFocusedView == VIRTUAL_VIEW_ID_CENTER) {
                info.setAccessibilityFocused(true);
                info.addAction(AccessibilityNodeInfo.ACTION_CLEAR_ACCESSIBILITY_FOCUS);
            }
            Rect boundsInParent = mTempRect;
            boundsInParent.set(left, top, right, bottom);
            info.setVisibleToUser(mDelegator.isVisibleToUserWrapper(boundsInParent));
            info.setBoundsInParent(boundsInParent);
            Rect boundsInScreen = boundsInParent;
            int[] locationOnScreen = mTempArray;
            mDelegator.getLocationOnScreen(locationOnScreen);
            boundsInScreen.offset(locationOnScreen[0], locationOnScreen[1]);
            info.setBoundsInScreen(boundsInScreen);
            return info;
        }

        private AccessibilityNodeInfo createAccessibilityNodeInfoForVirtualButton(int virtualViewId, String text, int left, int top, int right, int bottom) {
            AccessibilityNodeInfo info = AccessibilityNodeInfo.obtain();
            info.setClassName(Button.class.getName());
            info.setPackageName(mContext.getPackageName());
            info.setSource(mDelegator, virtualViewId);
            info.setParent(mDelegator);
            info.setText(text);
            AccessibilityNodeInfoCompat.wrap(info).setTooltipText(mPickerContentDescription);
            info.setClickable(true);
            info.setLongClickable(true);
            info.setEnabled(mDelegator.isEnabled());
            Rect boundsInParent = mTempRect;
            boundsInParent.set(left, top, right, bottom);
            info.setVisibleToUser(mDelegator.isVisibleToUserWrapper(boundsInParent));
            info.setBoundsInParent(boundsInParent);
            Rect boundsInScreen = boundsInParent;
            int[] locationOnScreen = mTempArray;
            mDelegator.getLocationOnScreen(locationOnScreen);
            boundsInScreen.offset(locationOnScreen[0], locationOnScreen[1]);
            info.setBoundsInScreen(boundsInScreen);

            if (mAccessibilityFocusedView != virtualViewId) {
                info.addAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS);
            }
            if (mAccessibilityFocusedView == virtualViewId) {
                info.addAction(AccessibilityNodeInfo.ACTION_CLEAR_ACCESSIBILITY_FOCUS);
            }
            if (mDelegator.isEnabled()) {
                info.addAction(AccessibilityNodeInfo.ACTION_CLICK);
            }

            return info;
        }

        private AccessibilityNodeInfo createAccessibilityNodeInfoForDatePickerWidget(int left, int top, int right, int bottom) {
            AccessibilityNodeInfo info = AccessibilityNodeInfo.obtain();
            info.setClassName(android.widget.NumberPicker.class.getName());
            info.setPackageName(mContext.getPackageName());
            info.setSource(mDelegator);

            if (hasVirtualDecrementButton()) {
                info.addChild(mDelegator, VIRTUAL_VIEW_ID_DECREMENT);
            }
            info.addChild(mDelegator, VIRTUAL_VIEW_ID_CENTER);
            if (hasVirtualIncrementButton()) {
                info.addChild(mDelegator, VIRTUAL_VIEW_ID_INCREMENT);
            }

            info.setParent((View) mDelegator.getParentForAccessibility());
            info.setEnabled(mDelegator.isEnabled());
            info.setScrollable(true);

            final float applicationScale = SeslCompatibilityInfoReflector.getField_applicationScale(mContext.getResources());

            Rect boundsInParent = mTempRect;
            boundsInParent.set(left, top, right, bottom);
            scaleRect(boundsInParent, applicationScale);
            info.setBoundsInParent(boundsInParent);

            info.setVisibleToUser(mDelegator.isVisibleToUserWrapper());

            Rect boundsInScreen = boundsInParent;
            int[] locationOnScreen = mTempArray;
            mDelegator.getLocationOnScreen(locationOnScreen);
            boundsInScreen.offset(locationOnScreen[0], locationOnScreen[1]);
            scaleRect(boundsInParent, applicationScale);
            info.setBoundsInScreen(boundsInScreen);

            if (mAccessibilityFocusedView != View.NO_ID) {
                info.addAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS);
            }
            if (mAccessibilityFocusedView == View.NO_ID) {
                info.addAction(AccessibilityNodeInfo.ACTION_CLEAR_ACCESSIBILITY_FOCUS);
            }
            if (mDelegator.isEnabled()) {
                if (getWrapSelectorWheel() || getValue().compareTo(getMaxValue()) < 0) {
                    info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD);
                }
                if (getWrapSelectorWheel() || getValue().compareTo(getMinValue()) > 0) {
                    info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_BACKWARD);
                }
            }

            return info;
        }

        private void scaleRect(Rect rect, float scale) {
            if (scale != 1.0f) {
                rect.left = (int) ((((float) rect.left) * scale) + 0.5f);
                rect.top = (int) ((((float) rect.top) * scale) + 0.5f);
                rect.right = (int) ((((float) rect.right) * scale) + 0.5f);
                rect.bottom = (int) ((((float) rect.bottom) * scale) + 0.5f);
            }
        }

        private boolean hasVirtualDecrementButton() {
            return getWrapSelectorWheel() || getValue().compareTo(getMinValue()) > 0;
        }

        private boolean hasVirtualIncrementButton() {
            return getWrapSelectorWheel() || getValue().compareTo(getMaxValue()) < 0;
        }

        private String getVirtualDecrementButtonText() {
            Calendar calendar = (Calendar) mValue.clone();
            calendar.add(Calendar.DAY_OF_MONTH, -1);
            if (mWrapSelectorWheel) {
                calendar = getWrappedSelectorIndex(calendar);
            }
            if (calendar.compareTo(mMinValue) < 0) {
                return null;
            }
            if (mIsLunar) {
                return formatDateForLunarForAccessibility(calendar);
            } else {
                return formatDateForAccessibility(calendar) + ", " + mPickerContentDescription + ", ";
            }
        }

        private String getVirtualIncrementButtonText() {
            Calendar calendar = (Calendar) mValue.clone();
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            if (mWrapSelectorWheel) {
                calendar = getWrappedSelectorIndex(calendar);
            }
            if (calendar.compareTo(mMaxValue) > 0) {
                return null;
            }
            if (mIsLunar) {
                return formatDateForLunarForAccessibility(calendar);
            } else {
                return formatDateForAccessibility(calendar) + ", " + mPickerContentDescription + ", ";
            }
        }

        private String getVirtualCurrentButtonText() {
            Calendar calendar = (Calendar) mValue.clone();
            if (mWrapSelectorWheel) {
                calendar = getWrappedSelectorIndex(calendar);
            }
            if (calendar.compareTo(mMaxValue) > 0) {
                return null;
            }
            if (mIsLunar) {
                return formatDateForLunarForAccessibility(calendar);
            } else {
                return formatDateForAccessibility(calendar) + ", " + mPickerContentDescription + ", ";
            }
        }
    }

    private void clearCalendar(Calendar oldCalendar, Calendar newCalendar) {
        oldCalendar.set(Calendar.YEAR, newCalendar.get(Calendar.YEAR));
        oldCalendar.set(Calendar.MONTH, newCalendar.get(Calendar.MONTH));
        oldCalendar.set(Calendar.DAY_OF_MONTH, newCalendar.get(Calendar.DAY_OF_MONTH));
    }

    private static String formatDateWithLocale(Calendar calendar) {
        return new SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(calendar.getTime());
    }

    private static String formatDateWithLocaleForAccessibility(Calendar calendar) {
        return new SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(calendar.getTime());
    }

    private static String formatDayWithLocale(Calendar calendar) {
        return new SimpleDateFormat("d", Locale.getDefault()).format(calendar.getTime());
    }

    private static String getDayWithLocale(int day) {
        return String.format(Locale.getDefault(), "%d", day);
    }

    private static String formatMonthWithLocale(Calendar calendar) {
        return new SimpleDateFormat("MMM", Locale.getDefault()).format(calendar.getTime());
    }

    private static String formatMonthWithLocaleForAccessibility(Calendar calendar) {
        return new SimpleDateFormat("MMMM", Locale.getDefault()).format(calendar.getTime());
    }

    private String getMonthWithLocale(int month) {
        return mShortMonths[month];
    }

    private String getMonthWithLocaleForAccessibility(int month) {
        return mLongMonths[month];
    }

    private static String formatNumberWithLocale(int number) {
        return String.format(Locale.getDefault(), "%d", number);
    }

    private boolean isCharacterNumberLanguage() {
        String language = Locale.getDefault().getLanguage();
        return "ar".equals(language) || "fa".equals(language) || "my".equals(language);
    }

    private boolean needCompareEqualMonthLanguage() {
        return "vi".equals(Locale.getDefault().getLanguage());
    }

    @Override
    public Calendar convertLunarToSolar(Calendar calendar, int year, int month, int dayOfMonth) {
        Calendar newCalendar = (Calendar) calendar.clone();
        SeslSolarLunarConverterReflector.convertLunarToSolar(mPathClassLoader, mSolarLunarConverter, year, month, dayOfMonth, mIsLeapMonth);
        newCalendar.set(SeslSolarLunarConverterReflector.getYear(mPathClassLoader, mSolarLunarConverter), SeslSolarLunarConverterReflector.getMonth(mPathClassLoader, mSolarLunarConverter), SeslSolarLunarConverterReflector.getDay(mPathClassLoader, mSolarLunarConverter));
        return newCalendar;
    }

    @Override
    public Calendar convertSolarToLunar(Calendar calendar, SpinningDatePicker.LunarDate lunarDate) {
        Calendar newCalendar = (Calendar) calendar.clone();
        SeslSolarLunarConverterReflector.convertSolarToLunar(mPathClassLoader, mSolarLunarConverter, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        newCalendar.set(SeslSolarLunarConverterReflector.getYear(mPathClassLoader, mSolarLunarConverter), SeslSolarLunarConverterReflector.getMonth(mPathClassLoader, mSolarLunarConverter), SeslSolarLunarConverterReflector.getDay(mPathClassLoader, mSolarLunarConverter));
        if (lunarDate != null) {
            lunarDate.day = SeslSolarLunarConverterReflector.getDay(mPathClassLoader, mSolarLunarConverter);
            lunarDate.month = SeslSolarLunarConverterReflector.getMonth(mPathClassLoader, mSolarLunarConverter);
            lunarDate.year = SeslSolarLunarConverterReflector.getYear(mPathClassLoader, mSolarLunarConverter);
            lunarDate.isLeapMonth = SeslSolarLunarConverterReflector.isLeapMonth(mPathClassLoader, mSolarLunarConverter);
        }
        return newCalendar;
    }

    private Calendar getCalendarForLocale(Calendar calendar, Locale locale) {
        Calendar newCalendar = Calendar.getInstance(locale);
        if (calendar != null) {
            newCalendar.setTimeInMillis(calendar.getTimeInMillis());
        }
        newCalendar.set(Calendar.HOUR_OF_DAY, 12);
        newCalendar.set(Calendar.MINUTE, 0);
        newCalendar.set(Calendar.SECOND, 0);
        newCalendar.set(Calendar.MILLISECOND, 0);
        return newCalendar;
    }

    private boolean isHighContrastFontEnabled() {
        return SeslViewReflector.isHighContrastTextEnabled(mInputText);
    }
}
