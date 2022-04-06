package de.dlyt.yanndroid.oneui.sesl.picker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.provider.Settings;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.customview.widget.ExploreByTouchHelper;
import androidx.reflect.feature.SeslCscFeatureReflector;
import androidx.reflect.lunarcalendar.SeslFeatureReflector;
import androidx.reflect.lunarcalendar.SeslLunarDateUtilsReflector;
import androidx.reflect.lunarcalendar.SeslSolarLunarConverterReflector;
import androidx.reflect.view.SeslViewReflector;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import dalvik.system.PathClassLoader;
import de.dlyt.yanndroid.oneui.R;
import de.dlyt.yanndroid.oneui.widget.DatePicker;

public class SeslSimpleMonthView extends View {
    private static final int DEFAULT_MONTH_LINE = 6;
    private static final int DEFAULT_NUM_DAYS = 7;
    private static final String DEFAULT_WEEK_DAY_STRING_FEATURE = "XXXXXXR";
    private static final int DEFAULT_WEEK_START = 1;
    private static final float DIVISOR_FOR_CIRCLE_POSITION_Y = 2.7f;
    private static final int LEAP_MONTH = 1;
    private static final float LEAP_MONTH_WEIGHT = 0.5f;
    private static final int MAX_MONTH_VIEW_ID = 42;
    private static final int MIN_HEIGHT = 10;
    private static final int MONTH_WEIGHT = 100;
    private static final int SIZE_UNSPECIFIED = -1;
    private static final String TAG = "SeslSimpleMonthView";
    private static final String TAG_CSCFEATURE_CALENDAR_SETCOLOROFDAYS = "CscFeature_Calendar_SetColorOfDays";
    private static final int YEAR_WEIGHT = 10000;
    private Paint mAbnormalSelectedDayPaint;
    private final int mAbnormalStartEndDateBackgroundAlpha;
    private final Calendar mCalendar = Calendar.getInstance();
    private int mCalendarWidth;
    private Context mContext;
    private int[] mDayColorSet = new int[DEFAULT_NUM_DAYS];
    private int mDayNumberDisabledAlpha;
    private Paint mDayNumberPaint;
    private Paint mDayNumberSelectedPaint;
    private int mDayOfWeekStart = 0;
    private int mDaySelectedCircleSize;
    private int mDaySelectedCircleStroke;
    private int mEnabledDayEnd = 31;
    private int mEnabledDayStart = 1;
    private int mEndDay;
    private int mEndMonth;
    private int mEndYear;
    private Paint mHcfEnabledDayNumberPaint;
    private boolean mIsFirstMonth = false;
    private boolean mIsHcfEnabled = false;
    private boolean mIsLastMonth = false;
    private int mIsLeapEndMonth;
    private boolean mIsLeapMonth = false;
    private int mIsLeapStartMonth;
    private boolean mIsLunar = false;
    private boolean mIsNextMonthLeap = false;
    private boolean mIsPrevMonthLeap = false;
    private boolean mIsRTL;
    private int mLastAccessibilityFocusedView = View.NO_ID;
    private boolean mLockAccessibilityDelegate;
    private boolean mLostAccessibilityFocus = false;
    private Calendar mMaxDate = Calendar.getInstance();
    private Calendar mMinDate = Calendar.getInstance();
    private int mMiniDayNumberTextSize;
    private int mMode = DatePicker.DATE_MODE_NONE;
    private int mMonth;
    private int mNormalTextColor;
    private int mNumCells = DEFAULT_NUM_DAYS;
    private int mNumDays = DEFAULT_NUM_DAYS;
    private OnDayClickListener mOnDayClickListener;
    private OnDeactivatedDayClickListener mOnDeactivatedDayClickListener;
    private int mPadding = 0;
    private PathClassLoader mPathClassLoader = null;
    private final int mPrevNextMonthDayNumberAlpha;
    private int mSaturdayTextColor;
    private int mSelectedDay = -1;
    private int mSelectedDayColor;
    private int mSelectedDayNumberTextColor;
    private Object mSolarLunarConverter;
    private int mStartDay;
    private int mStartMonth;
    private int mStartYear;
    private int mSundayTextColor;
    private Calendar mTempDate = Calendar.getInstance();
    private final MonthViewTouchHelper mTouchHelper;
    private int mWeekHeight;
    private int mWeekStart = DEFAULT_WEEK_START;
    private int mYear;

    public SeslSimpleMonthView(Context context) {
        this(context, null);
    }

    SeslSimpleMonthView(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.datePickerStyle);
    }

    SeslSimpleMonthView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs);
        mContext = context;
        mIsRTL = isRTL();

        final Resources resources = context.getResources();

        TypedValue color = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorPrimaryDark, color, true);
        if (color.resourceId != 0) {
            mSelectedDayColor = resources.getColor(color.resourceId);
        } else {
            mSelectedDayColor = color.data;
        }
        mSundayTextColor = resources.getColor(R.color.sesl_date_picker_sunday_number_text_color);
        mSaturdayTextColor = resources.getColor(R.color.sesl_date_picker_saturday_text_color);

        TypedArray a = mContext.obtainStyledAttributes(attrs, R.styleable.DatePicker, defStyleAttr, 0);
        mNormalTextColor = a.getColor(R.styleable.DatePicker_dayNumberTextColor, resources.getColor(R.color.sesl_date_picker_normal_day_number_text_color));
        mSelectedDayNumberTextColor = a.getColor(R.styleable.DatePicker_selectedDayNumberTextColor, resources.getColor(R.color.sesl_date_picker_selected_day_number_text_color));
        mDayNumberDisabledAlpha = a.getInteger(R.styleable.DatePicker_dayNumberDisabledAlpha, resources.getInteger(R.integer.sesl_day_number_disabled_alpha));
        a.recycle();

        mWeekHeight = resources.getDimensionPixelOffset(R.dimen.sesl_date_picker_calendar_week_height);
        mDaySelectedCircleSize = resources.getDimensionPixelSize(R.dimen.sesl_date_picker_selected_day_circle_radius);
        mDaySelectedCircleStroke = resources.getDimensionPixelSize(R.dimen.sesl_date_picker_selected_day_circle_stroke);
        mMiniDayNumberTextSize = resources.getDimensionPixelSize(R.dimen.sesl_date_picker_day_number_text_size);
        mCalendarWidth = resources.getDimensionPixelOffset(R.dimen.sesl_date_picker_calendar_view_width);
        mPadding = resources.getDimensionPixelOffset(R.dimen.sesl_date_picker_calendar_view_padding);

        mTouchHelper = new MonthViewTouchHelper(this);
        ViewCompat.setAccessibilityDelegate(this, mTouchHelper);
        setImportantForAccessibility(ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_YES);
        mLockAccessibilityDelegate = true;

        if (Settings.System.getString(mContext.getContentResolver(), "current_sec_active_themepackage") != null) {
            mDayNumberDisabledAlpha = resources.getInteger(R.integer.sesl_day_number_theme_disabled_alpha);
        }
        mPrevNextMonthDayNumberAlpha = resources.getInteger(R.integer.sesl_day_number_theme_disabled_alpha);
        mAbnormalStartEndDateBackgroundAlpha = resources.getInteger(R.integer.sesl_date_picker_abnormal_start_end_date_background_alpha);

        initView();
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mIsRTL = isRTL();
        mTouchHelper.invalidateRoot();
        mWeekHeight = mContext.getResources().getDimensionPixelOffset(R.dimen.sesl_date_picker_calendar_week_height);
        mDaySelectedCircleSize = mContext.getResources().getDimensionPixelSize(R.dimen.sesl_date_picker_selected_day_circle_radius);
        mMiniDayNumberTextSize = mContext.getResources().getDimensionPixelSize(R.dimen.sesl_date_picker_day_number_text_size);
        initView();
    }

    public void setTextColor() {
        String string = SeslCscFeatureReflector.getString(TAG_CSCFEATURE_CALENDAR_SETCOLOROFDAYS, DEFAULT_WEEK_DAY_STRING_FEATURE);
        for (int i = 0; i < mNumDays; i++) {
            char charAt = string.charAt(i);
            int index = (i + 2) % mNumDays;
            if (charAt == 'R') {
                mDayColorSet[index] = mSundayTextColor;
            } else if (charAt == 'B') {
                mDayColorSet[index] = mSaturdayTextColor;
            } else {
                mDayColorSet[index] = mNormalTextColor;
            }
        }
    }

    @Override
    public void setAccessibilityDelegate(View.AccessibilityDelegate delegate) {
        if (!mLockAccessibilityDelegate) {
            super.setAccessibilityDelegate(delegate);
        }
    }

    public void setOnDayClickListener(OnDayClickListener listener) {
        mOnDayClickListener = listener;
    }

    public void setOnDeactivatedDayClickListener(OnDeactivatedDayClickListener listener) {
        mOnDeactivatedDayClickListener = listener;
    }

    @Override
    public boolean dispatchHoverEvent(MotionEvent event) {
        return mTouchHelper.dispatchHoverEvent(event) || super.dispatchHoverEvent(event);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            final int day = getDayFromLocation(event.getX(), event.getY());
            if (mIsFirstMonth && day < mEnabledDayStart || mIsLastMonth && day > mEnabledDayEnd) {
                return true;
            }

            if (day > 0) {
                if (day > mNumCells) {
                    if (mIsLunar) {
                        int year = mYear;
                        int month = mMonth + (mIsNextMonthLeap ? 0 : LEAP_MONTH);
                        if (month > Calendar.DECEMBER) {
                            year += 1;
                            month = Calendar.JANUARY;
                        }
                        onDeactivatedDayClick(year, month, day - mNumCells, false);
                    } else {
                        Calendar calendar = Calendar.getInstance();
                        calendar.clear();
                        calendar.set(mYear, mMonth, mNumCells);
                        calendar.add(Calendar.DAY_OF_MONTH, day - mNumCells);
                        onDeactivatedDayClick(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), false);
                    }
                } else {
                    onDayClick(mYear, mMonth, day);
                }
            } else if (mIsLunar) {
                int month = Calendar.DECEMBER;
                int year = mYear;
                int i = mMonth - (mIsLeapMonth ? 0 : LEAP_MONTH);
                if (i < 0) {
                    year--;
                } else {
                    month = i;
                }
                onDeactivatedDayClick(year, month, getDaysInMonthLunar(month, year, mIsPrevMonthLeap) + day, true);
            } else {
                Calendar calendar = Calendar.getInstance();
                calendar.clear();
                calendar.set(mYear, mMonth, 1);
                calendar.add(Calendar.DAY_OF_MONTH, day - 1);
                onDeactivatedDayClick(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), true);
            }
        }
        return true;
    }

    private void initView() {
        mDayNumberSelectedPaint = new Paint();
        mDayNumberSelectedPaint.setAntiAlias(true);
        mDayNumberSelectedPaint.setColor(mSelectedDayColor);
        mDayNumberSelectedPaint.setTextAlign(Paint.Align.CENTER);
        mDayNumberSelectedPaint.setStrokeWidth(mDaySelectedCircleStroke);
        mDayNumberSelectedPaint.setFakeBoldText(true);
        mDayNumberSelectedPaint.setStyle(Paint.Style.FILL);

        mAbnormalSelectedDayPaint = new Paint(mDayNumberSelectedPaint);
        mAbnormalSelectedDayPaint.setColor(mNormalTextColor);
        mAbnormalSelectedDayPaint.setAlpha(mAbnormalStartEndDateBackgroundAlpha);

        mDayNumberPaint = new Paint();
        mDayNumberPaint.setAntiAlias(true);
        mDayNumberPaint.setTextSize((float) this.mMiniDayNumberTextSize);
        mDayNumberPaint.setTypeface(Typeface.create("sec-roboto-light", Typeface.NORMAL));
        mDayNumberPaint.setTextAlign(Paint.Align.CENTER);
        mDayNumberPaint.setStyle(Paint.Style.FILL);
        mDayNumberPaint.setFakeBoldText(false);

        mHcfEnabledDayNumberPaint = new Paint(mDayNumberPaint);
        mHcfEnabledDayNumberPaint.setTypeface(Typeface.create("sec-roboto-light", Typeface.BOLD));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawDays(canvas);
    }

    public void setMonthParams(int selectedDay, int month, int year, int weekStart, int enabledDayStart, int enabledDayEnd, Calendar minDate, Calendar maxDate, int startYear, int startMonth, int startDay, int isLeapStartMonth, int endYear, int endMonth, int endDay, int isLeapEndMonth, int mode) {
        mMode = mode;

        if (mWeekHeight < MIN_HEIGHT) {
            mWeekHeight = MIN_HEIGHT;
        }
        mSelectedDay = selectedDay;
        if (isValidMonth(month)) {
            mMonth = month;
        }
        mYear = year;

        mCalendar.clear();
        mCalendar.set(Calendar.MONTH, mMonth);
        mCalendar.set(Calendar.YEAR, mYear);
        mCalendar.set(Calendar.DAY_OF_MONTH, 1);

        mMinDate = minDate;
        mMaxDate = maxDate;

        if (mIsLunar) {
            if (mSolarLunarConverter != null) {
                SeslSolarLunarConverterReflector.convertLunarToSolar(mPathClassLoader, mSolarLunarConverter, mYear, mMonth, 1, mIsLeapMonth);
                final int lunarYear = SeslSolarLunarConverterReflector.getYear(mPathClassLoader, mSolarLunarConverter);
                final int lunarMonth = SeslSolarLunarConverterReflector.getMonth(mPathClassLoader, mSolarLunarConverter);
                final int lunarDay = SeslSolarLunarConverterReflector.getDay(mPathClassLoader, mSolarLunarConverter);
                mDayOfWeekStart = SeslSolarLunarConverterReflector.getWeekday(mPathClassLoader, mSolarLunarConverter, lunarYear, lunarMonth, lunarDay) + 1;
                mNumCells = getDaysInMonthLunar(mMonth, mYear, mIsLeapMonth);
            }
        } else {
            mDayOfWeekStart = mCalendar.get(Calendar.DAY_OF_WEEK);
            mNumCells = getDaysInMonth(mMonth, mYear);
        }

        if (isValidDayOfWeek(weekStart)) {
            mWeekStart = weekStart;
        } else {
            mWeekStart = mCalendar.getFirstDayOfWeek();
        }

        int dayStart = (mMonth == minDate.get(Calendar.MONTH) && mYear == minDate.get(Calendar.YEAR)) ? minDate.get(Calendar.DAY_OF_MONTH) : enabledDayStart;
        int dayEnd = (mMonth == maxDate.get(Calendar.MONTH) && mYear == maxDate.get(Calendar.YEAR)) ? maxDate.get(Calendar.DAY_OF_MONTH) : enabledDayEnd;
        if (dayStart > 0 && dayEnd < 32) {
            mEnabledDayStart = dayStart;
        }
        if (dayEnd > 0 && dayEnd < 32 && dayEnd >= dayStart) {
            mEnabledDayEnd = dayEnd;
        }

        mTouchHelper.invalidateRoot();
        mStartYear = startYear;
        mStartMonth = startMonth;
        mStartDay = startDay;
        mIsLeapStartMonth = isLeapStartMonth;
        mEndYear = endYear;
        mEndMonth = endMonth;
        mEndDay = endDay;
        mIsLeapEndMonth = isLeapEndMonth;
    }

    private int getDaysInMonthLunar(int month, int year, boolean isLeapMonth) {
        if (mSolarLunarConverter != null) {
            return SeslSolarLunarConverterReflector.getDayLengthOf(mPathClassLoader, mSolarLunarConverter, year, month, isLeapMonth);
        } else  {
            Log.e(TAG, "getDaysInMonthLunar, mSolarLunarConverter is null");
            return getDaysInMonth(month, year);
        }
    }

    private static int getDaysInMonth(int month, int year) {
        switch (month) {
            case Calendar.JANUARY:
            case Calendar.MARCH:
            case Calendar.MAY:
            case Calendar.JULY:
            case Calendar.AUGUST:
            case Calendar.OCTOBER:
            case Calendar.DECEMBER:
                return 31;
            case Calendar.APRIL:
            case Calendar.JUNE:
            case Calendar.SEPTEMBER:
            case Calendar.NOVEMBER:
                return 30;
            case Calendar.FEBRUARY:
                return (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) ? 29 : 28;
            default:
                throw new IllegalArgumentException("Invalid Month");
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(makeMeasureSpec(widthMeasureSpec, mCalendarWidth), heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (!mLostAccessibilityFocus && mLastAccessibilityFocusedView == View.NO_ID && mSelectedDay != -1) {
            mTouchHelper.sendEventForVirtualView(mSelectedDay + findDayOffset(), AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED);
        } else if (!mLostAccessibilityFocus && mLastAccessibilityFocusedView != View.NO_ID) {
            mTouchHelper.sendEventForVirtualView(mLastAccessibilityFocusedView + findDayOffset(), AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED);
        }

        if (changed) {
            mTouchHelper.invalidateRoot();
        }

        super.onLayout(changed, left, top, right, bottom);
    }

    private int makeMeasureSpec(int widthMeasureSpec, int heightMeasureSpec) {
        if (heightMeasureSpec == ViewGroup.LayoutParams.MATCH_PARENT) {
            return widthMeasureSpec;
        }

        int size = MeasureSpec.getSize(widthMeasureSpec);
        int mode = MeasureSpec.getMode(widthMeasureSpec);
        if (mode == MeasureSpec.AT_MOST) {
            mCalendarWidth = Math.min(size, heightMeasureSpec);
            return MeasureSpec.makeMeasureSpec(mCalendarWidth, MeasureSpec.EXACTLY);
        } else if (mode == 0) {
            return MeasureSpec.makeMeasureSpec(heightMeasureSpec, MeasureSpec.EXACTLY);
        } else {
            if (mode == MeasureSpec.EXACTLY) {
                mCalendarWidth = size;
                return widthMeasureSpec;
            }
            throw new IllegalArgumentException("Unknown measure mode: " + mode);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mTouchHelper.invalidateRoot();
    }

    // kang
    private void drawDays(Canvas var1) {
        int var2 = this.mWeekHeight * 2 / 3;
        int var3 = this.mCalendarWidth / (this.mNumDays * 2);
        int var4 = this.findDayOffset();
        float var5 = (float)this.mMiniDayNumberTextSize / DIVISOR_FOR_CIRCLE_POSITION_Y;
        int var6 = this.mStartYear;
        float var7 = (float)this.mStartMonth;
        int var8 = this.mStartDay;
        int var9 = this.mEndYear;
        float var10 = (float)this.mEndMonth;
        int var11 = this.mEndDay;
        boolean var12 = this.mIsLunar;
        float var13 = var7;
        if (var12) {
            var13 = var7;
            if (this.mIsLeapStartMonth == 1) {
                var13 = var7 + LEAP_MONTH_WEIGHT;
            }
        }

        float var14 = var13;
        var7 = var10;
        if (var12) {
            var7 = var10;
            if (this.mIsLeapEndMonth == 1) {
                var7 = var10 + LEAP_MONTH_WEIGHT;
            }
        }

        int var15 = this.mYear;
        var13 = (float)this.mMonth;
        var10 = var13;
        if (var12) {
            var10 = var13;
            if (this.mIsLeapMonth) {
                var10 = var13 + LEAP_MONTH_WEIGHT;
            }
        }

        int var16 = var6 * YEAR_WEIGHT + (int)(var14 * MONTH_WEIGHT);
        int var17 = var9 * YEAR_WEIGHT + (int)(var7 * MONTH_WEIGHT);
        int var18 = var15 * YEAR_WEIGHT + (int)(MONTH_WEIGHT * var10);
        boolean var20;
        if (this.mMode != 0) {
            boolean var19;
            if (var16 + var8 > var17 + var11) {
                var19 = true;
            } else {
                var19 = false;
            }

            var20 = var19;
        } else {
            var20 = false;
        }

        int var31;
        label404: {
            if (!var20) {
                label413: {
                    label410: {
                        if (var6 == var9 && var14 == var7 && var15 == var6 && var10 == var14) {
                            var18 = var11;
                        } else {
                            if (var16 < var18 && var18 < var17 && (var15 != var9 || var10 != var7)) {
                                var18 = this.mNumCells + 1;
                                break label410;
                            }

                            if (var15 != var6 || var10 != var14) {
                                if (var15 != var9 || var10 != var7) {
                                    break label413;
                                }

                                var18 = var11;
                                break label410;
                            }

                            var18 = this.mNumCells + 1;
                        }

                        var31 = var8;
                        break label404;
                    }

                    var31 = 0;
                    break label404;
                }
            }

            var31 = -1;
            var18 = -1;
        }

        this.mIsHcfEnabled = this.isHighContrastFontEnabled();
        var17 = 0;
        byte var21 = 1;
        var16 = var4;
        var13 = var5;
        int var22 = var11;
        var11 = var2;

        Paint var25;
        Paint var26;
        for(var2 = var21; var2 <= this.mNumCells; ++var2) {
            int var23;
            int var33;
            if (this.mIsRTL) {
                var33 = ((this.mNumDays - 1 - var16) * 2 + 1) * var3;
                var23 = this.mPadding;
            } else {
                var33 = (var16 * 2 + 1) * var3;
                var23 = this.mPadding;
            }

            int var24 = var33 + var23;
            var33 = this.mWeekStart;
            var23 = this.mNumDays;
            this.mDayNumberPaint.setColor(this.mDayColorSet[(var16 + var33) % var23]);
            if (var2 < this.mEnabledDayStart || var2 > this.mEnabledDayEnd) {
                this.mDayNumberPaint.setAlpha(this.mDayNumberDisabledAlpha);
            }

            var25 = this.mDayNumberPaint;
            var26 = var25;
            if (this.mIsHcfEnabled) {
                var26 = var25;
                if (var25.getAlpha() != this.mDayNumberDisabledAlpha) {
                    this.mHcfEnabledDayNumberPaint.setColor(this.mDayNumberPaint.getColor());
                    var26 = this.mHcfEnabledDayNumberPaint;
                }
            }

            if (var20) {
                label380: {
                    label322: {
                        label381: {
                            if (var6 == var15 && var14 == var10 && var8 == var2) {
                                var33 = this.mMode;
                                if (var33 == 2 || var33 == 3) {
                                    break label381;
                                }
                            }

                            if (var9 != var15 || var7 != var10 || var22 != var2) {
                                break label322;
                            }

                            var33 = this.mMode;
                            if (var33 != 1 && var33 != 3) {
                                break label322;
                            }
                        }

                        var1.drawCircle((float)var24, (float)var11 - var13, (float)this.mDaySelectedCircleSize, this.mDayNumberSelectedPaint);
                        var26.setColor(this.mSelectedDayNumberTextColor);
                    }

                    label382: {
                        if (var9 == var15 && var7 == var10 && var22 == var2) {
                            var33 = this.mMode;
                            if (var33 == 2 || var33 == 3) {
                                break label382;
                            }
                        }

                        if (var6 != var15 || var14 != var10 || var8 != var2) {
                            break label380;
                        }

                        var33 = this.mMode;
                        if (var33 != 1 && var33 != 3) {
                            break label380;
                        }
                    }

                    var1.drawCircle((float)var24, (float)var11 - var13, (float)this.mDaySelectedCircleSize, this.mAbnormalSelectedDayPaint);
                }
            } else {
                float var27;
                float var28;
                if (var31 < var2 && var2 < var18) {
                    var27 = (float)(var24 - var3);
                    var28 = (float)var11;
                    var33 = this.mDaySelectedCircleSize;
                    var28 = var28 - var13 - (float)var33;
                    var1.drawRect(var27, var28, var27 + (float)(var3 * 2), var28 + (float)(var33 * 2), this.mDayNumberSelectedPaint);
                    var26.setColor(this.mSelectedDayNumberTextColor);
                }

                var33 = var31;
                if (var31 != -1 && var31 == var18 && var2 == var31) {
                    var1.drawCircle((float)var24, (float)var11 - var13, (float)this.mDaySelectedCircleSize, this.mDayNumberSelectedPaint);
                    var26.setColor(this.mSelectedDayNumberTextColor);
                    var31 = var31;
                } else if (var18 == var2) {
                    var27 = (float)var11 - var13;
                    if (this.mIsRTL) {
                        var5 = (float)var24;
                    } else {
                        var5 = (float)(var24 - var3);
                    }

                    var31 = this.mDaySelectedCircleSize;
                    var28 = var27 - (float)var31;
                    var1.drawRect(var5, var28, (float)var3 + var5, var28 + (float)(var31 * 2), this.mDayNumberSelectedPaint);
                    var1.drawCircle((float)var24, var27, (float)this.mDaySelectedCircleSize, this.mDayNumberSelectedPaint);
                    var26.setColor(this.mSelectedDayNumberTextColor);
                    var31 = var33;
                } else {
                    var31 = var31;
                    if (var33 == var2) {
                        var27 = (float)var11 - var13;
                        if (this.mIsRTL) {
                            var5 = (float)(var24 - var3);
                        } else {
                            var5 = (float)var24;
                        }

                        var31 = this.mDaySelectedCircleSize;
                        var28 = var27 - (float)var31;
                        var1.drawRect(var5, var28, (float)var3 + var5, var28 + (float)(var31 * 2), this.mDayNumberSelectedPaint);
                        var1.drawCircle((float)var24, var27, (float)this.mDaySelectedCircleSize, this.mDayNumberSelectedPaint);
                        var26.setColor(this.mSelectedDayNumberTextColor);
                        var31 = var33;
                    }
                }
            }

            if (this.mMode == 0 && var2 == var18) {
                var26.setColor(this.mSelectedDayNumberTextColor);
            }

            var1.drawText(String.format("%d", var2), (float)var24, (float)var11, var26);
            ++var16;
            if (var16 == this.mNumDays) {
                var11 += this.mWeekHeight;
                ++var17;
                var16 = 0;
            }
        }

        var2 = var11;
        var11 = var31;
        var31 = var31;
        int var32;
        if (!this.mIsLastMonth) {
            byte var34 = 1;
            var32 = var16;
            var16 = var2;
            var2 = var34;

            while(true) {
                var31 = var11;
                if (var17 == 6) {
                    break;
                }

                if (this.mIsRTL) {
                    var31 = ((this.mNumDays - 1 - var32) * 2 + 1) * var3 + this.mPadding;
                } else {
                    var31 = (var32 * 2 + 1) * var3 + this.mPadding;
                }

                var9 = this.mWeekStart;
                var8 = this.mNumDays;
                this.mDayNumberPaint.setColor(this.mDayColorSet[(var32 + var9) % var8]);
                this.mDayNumberPaint.setAlpha(this.mPrevNextMonthDayNumberAlpha);
                if (this.mMode != 0 && var18 == this.mNumCells + 1) {
                    if (var2 >= this.mEndDay && this.isNextMonthEndMonth()) {
                        if (var2 == this.mEndDay) {
                            var10 = (float)var16 - var13;
                            if (this.mIsRTL) {
                                var7 = (float)var31;
                            } else {
                                var7 = (float)(var31 - var3);
                            }

                            var8 = this.mDaySelectedCircleSize;
                            var14 = var10 - (float)var8;
                            var1.drawRect(var7, var14, (float)var3 + var7, var14 + (float)(var8 * 2), this.mDayNumberSelectedPaint);
                            var1.drawCircle((float)var31, var10, (float)this.mDaySelectedCircleSize, this.mDayNumberSelectedPaint);
                        }
                    } else {
                        var7 = (float)(var31 - var3);
                        var10 = (float)var16;
                        var8 = this.mDaySelectedCircleSize;
                        var10 = var10 - var13 - (float)var8;
                        var1.drawRect(var7, var10, var7 + (float)(var3 * 2), var10 + (float)(var8 * 2), this.mDayNumberSelectedPaint);
                    }
                }

                if (!this.mIsLunar) {
                    var22 = this.mMonth + 1;
                    var6 = this.mYear;
                    var9 = var22;
                    var8 = var6;
                    if (var22 > 11) {
                        var8 = var6 + 1;
                        var9 = 0;
                    }

                    this.mTempDate.clear();
                    this.mTempDate.set(var8, var9, var2);
                    if (this.mTempDate.after(this.mMaxDate)) {
                        this.mDayNumberPaint.setAlpha(this.mDayNumberDisabledAlpha);
                    }
                }

                var25 = this.mDayNumberPaint;
                var26 = var25;
                if (this.mIsHcfEnabled) {
                    var26 = var25;
                    if (var25.getAlpha() != this.mDayNumberDisabledAlpha) {
                        this.mHcfEnabledDayNumberPaint.setColor(this.mDayNumberPaint.getColor());
                        var26 = this.mHcfEnabledDayNumberPaint;
                    }
                }

                if (this.mMode != 0 && var18 == this.mNumCells + 1 && (var2 <= this.mEndDay || !this.isNextMonthEndMonth())) {
                    var26.setColor(this.mSelectedDayNumberTextColor);
                }

                var1.drawText(String.format("%d", var2), (float)var31, (float)var16, var26);
                var31 = var32 + 1;
                if (var31 == this.mNumDays) {
                    var16 += this.mWeekHeight;
                    ++var17;
                    var31 = 0;
                }

                ++var2;
                var32 = var31;
            }
        }

        if (var4 > 0 && !this.mIsFirstMonth) {
            Calendar var35 = Calendar.getInstance();
            var35.clear();
            var35.set(this.mYear, this.mMonth, 1);
            var35.add(5, -var4);
            var11 = var35.get(5);
            if (this.mIsLunar) {
                var17 = this.mYear;
                var2 = this.mMonth - (mIsLeapMonth ? 0 : 1);
                var18 = var17;
                var11 = var2;
                if (var2 < 0) {
                    var18 = var17 - 1;
                    var11 = 11;
                }

                var11 = this.getDaysInMonthLunar(var11, var18, this.mIsPrevMonthLeap) - var4 + 1;
            }

            var18 = var11;

            for(var11 = 0; var11 < var4; ++var11) {
                if (this.mIsRTL) {
                    var2 = ((this.mNumDays - 1 - var11) * 2 + 1) * var3;
                    var17 = this.mPadding;
                } else {
                    var2 = (var11 * 2 + 1) * var3;
                    var17 = this.mPadding;
                }

                var8 = var2 + var17;
                var9 = this.mWeekHeight * 2 / 3;
                var17 = this.mWeekStart;
                var2 = this.mNumDays;
                this.mDayNumberPaint.setColor(this.mDayColorSet[(var17 + var11) % var2]);
                this.mDayNumberPaint.setAlpha(this.mPrevNextMonthDayNumberAlpha);
                if (this.mMode != 0 && var31 == 0) {
                    if (var18 <= this.mStartDay && this.isPrevMonthStartMonth()) {
                        if (var18 == this.mStartDay) {
                            var10 = (float)var9 - var13;
                            if (this.mIsRTL) {
                                var7 = (float)(var8 - var3);
                            } else {
                                var7 = (float)var8;
                            }

                            var2 = this.mDaySelectedCircleSize;
                            var14 = var10 - (float)var2;
                            var1.drawRect(var7, var14, (float)var3 + var7, var14 + (float)(var2 * 2), this.mDayNumberSelectedPaint);
                            var1.drawCircle((float)var8, var10, (float)this.mDaySelectedCircleSize, this.mDayNumberSelectedPaint);
                        }
                    } else {
                        var7 = (float)(var8 - var3);
                        var10 = (float)var9;
                        var2 = this.mDaySelectedCircleSize;
                        var10 = var10 - var13 - (float)var2;
                        var1.drawRect(var7, var10, var7 + (float)(var3 * 2), var10 + (float)(var2 * 2), this.mDayNumberSelectedPaint);
                    }
                }

                if (!this.mIsLunar) {
                    var16 = this.mMonth - 1;
                    var32 = this.mYear;
                    var17 = var16;
                    var2 = var32;
                    if (var16 < 0) {
                        var2 = var32 - 1;
                        var17 = 11;
                    }

                    this.mTempDate.clear();
                    this.mTempDate.set(var2, var17, var18);
                    var35 = Calendar.getInstance();
                    var35.clear();
                    var35.set(this.mMinDate.get(1), this.mMinDate.get(2), this.mMinDate.get(5));
                    if (this.mTempDate.before(this.mMinDate)) {
                        this.mDayNumberPaint.setAlpha(this.mDayNumberDisabledAlpha);
                    }
                }

                var25 = this.mDayNumberPaint;
                var26 = var25;
                if (this.mIsHcfEnabled) {
                    var26 = var25;
                    if (var25.getAlpha() != this.mDayNumberDisabledAlpha) {
                        this.mHcfEnabledDayNumberPaint.setColor(this.mDayNumberPaint.getColor());
                        var26 = this.mHcfEnabledDayNumberPaint;
                    }
                }

                if (this.mMode != 0 && var31 == 0 && (var18 >= this.mStartDay || !this.isPrevMonthStartMonth())) {
                    var26.setColor(this.mSelectedDayNumberTextColor);
                }

                var1.drawText(String.format("%d", var18), (float)var8, (float)var9, var26);
                ++var18;
            }
        }

    }
    // kang

    private boolean isPrevMonthStartMonth() {
        if (mIsLunar) {
            float month = (float) mMonth;
            float startMonth = (float) mStartMonth;
            if (mIsLeapMonth) {
                month += 0.5f;
            }
            if (mIsLeapStartMonth == 1) {
                startMonth += 0.5f;
            }

            float sub = month - startMonth;
            if (mYear != mStartYear || (sub >= 1.0f && (sub != 1.0f || mIsPrevMonthLeap))) {
                if (mYear != mStartYear + 1) {
                    return false;
                }
                float add = sub + 12.0f;
                if (add >= 1.0f && (add != 1.0f || mIsPrevMonthLeap)) {
                    return false;
                }
            }
            return true;
        } else {
            return (mYear == mStartYear && mMonth == mStartMonth + 1) || (mYear == mStartYear + 1 && mMonth == Calendar.JANUARY && mStartMonth == Calendar.DECEMBER);
        }
    }

    private boolean isNextMonthEndMonth() {
        if (this.mIsLunar) {
            float month = (float) mMonth;
            float endMonth = (float) mEndMonth;
            if (mIsLeapMonth) {
                month += 0.5f;
            }
            if (mIsLeapEndMonth == 1) {
                endMonth += 0.5f;
            }

            float sub = endMonth - month;
            if (mYear != mEndYear || (sub >= 1.0f && (sub != 1.0f || mIsNextMonthLeap))) {
                if (mYear != mEndYear - 1) {
                    return false;
                }
                float add = sub + 12.0f;
                if (add >= 1.0f && (add != 1.0f || mIsNextMonthLeap)) {
                    return false;
                }
            }
            return true;
        } else {
            return (mYear == mEndYear && mMonth == mEndMonth - 1) || (mYear == mEndYear - 1 && mMonth == Calendar.DECEMBER && mEndMonth == Calendar.JANUARY);
        }
    }

    private static boolean isValidDayOfWeek(int day) {
        return day >= Calendar.SUNDAY && day <= Calendar.SATURDAY;
    }

    private static boolean isValidMonth(int month) {
        return month >= Calendar.JANUARY && month <= Calendar.DECEMBER;
    }

    private int findDayOffset() {
        int offset = mDayOfWeekStart - mWeekStart;
        if (mDayOfWeekStart < mWeekStart) {
            offset += mNumDays;
        }
        return offset;
    }

    private int getDayFromLocation(float x, float y) {
        if (mIsRTL) {
            x = ((float) mCalendarWidth) - x;
        }
        if (x < mPadding) {
            return -1;
        }
        if (x > ((float) (mPadding + mCalendarWidth))) {
            return -1;
        }
        return (((int) (((x - mPadding) * ((float) mNumDays)) / ((float) mCalendarWidth))) - findDayOffset()) + 1 + ((((int) y) / mWeekHeight) * mNumDays);
    }

    private void onDayClick(int year, int month, int day) {
        if (mOnDayClickListener != null) {
            playSoundEffect(SoundEffectConstants.CLICK);
            mOnDayClickListener.onDayClick(this, year, month, day);
        }
        mTouchHelper.sendEventForVirtualView(day + findDayOffset(), AccessibilityEvent.TYPE_VIEW_CLICKED);
    }

    private void onDeactivatedDayClick(int year, int month, int day, boolean pageChanged) {
        if (!mIsLunar) {
            mTempDate.clear();
            mTempDate.set(year, month, day);
            if (pageChanged) {
                Calendar calendar = Calendar.getInstance();
                calendar.clear();
                calendar.set(mMinDate.get(Calendar.YEAR), mMinDate.get(Calendar.MONTH), mMinDate.get(Calendar.DAY_OF_MONTH));
                if (mTempDate.before(calendar)) {
                    return;
                }
            } else if (mTempDate.after(mMaxDate)) {
                return;
            }
        }
        if (mOnDeactivatedDayClickListener != null) {
            playSoundEffect(SoundEffectConstants.CLICK);
            mOnDeactivatedDayClickListener.onDeactivatedDayClick(this, year, month, day, mIsLeapMonth, pageChanged);
        }
        mTouchHelper.sendEventForVirtualView(day, AccessibilityEvent.TYPE_VIEW_CLICKED);
    }

    public void clearAccessibilityFocus() {
        mTouchHelper.clearFocusedVirtualView();
    }

    private class MonthViewTouchHelper extends ExploreByTouchHelper {
        private final Rect mTempRect = new Rect();
        private final Calendar mTempCalendar = Calendar.getInstance();

        public MonthViewTouchHelper(View host) {
            super(host);
        }

        public void setFocusedVirtualView(int virtualViewId) {
            getAccessibilityNodeProvider(SeslSimpleMonthView.this).performAction(virtualViewId, AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS, null);
        }

        public void clearFocusedVirtualView() {
            int virtualView = getFocusedVirtualView();
            if (virtualView != ExploreByTouchHelper.INVALID_ID) {
                getAccessibilityNodeProvider(SeslSimpleMonthView.this).performAction(virtualView, AccessibilityNodeInfo.ACTION_CLEAR_ACCESSIBILITY_FOCUS, null);
            }
        }

        @Override
        protected int getVirtualViewAt(float x, float y) {
            final int day = getDayFromLocation(x, y);
            if (mIsFirstMonth && day < mEnabledDayStart) {
                return ExploreByTouchHelper.INVALID_ID;
            }
            if (!mIsLastMonth || day <= mEnabledDayEnd) {
                return day + findDayOffset();
            }
            return ExploreByTouchHelper.INVALID_ID;
        }

        @Override
        protected void getVisibleVirtualViews(List<Integer> virtualViewIds) {
            final int dayOffset = findDayOffset();

            for (int i = 1; i <= MAX_MONTH_VIEW_ID; i++) {
                final int day = i - dayOffset;
                if ((!mIsFirstMonth || day >= mEnabledDayStart) && (!mIsLastMonth || day <= mEnabledDayEnd)) {
                    virtualViewIds.add(i);
                }
                virtualViewIds.add(day);
            }
        }

        @Override
        protected void onPopulateEventForVirtualView(int virtualViewId, AccessibilityEvent event) {
            final int day = virtualViewId - findDayOffset();
            if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED) {
                mLastAccessibilityFocusedView = day;
                mLostAccessibilityFocus = false;
            }
            if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUS_CLEARED) {
                mLastAccessibilityFocusedView = View.NO_ID;
                mLostAccessibilityFocus = true;
            }
            event.setContentDescription(getItemDescription(day));
        }

        @Override
        protected void onPopulateNodeForVirtualView(int virtualViewId, AccessibilityNodeInfoCompat node) {
            final int day = virtualViewId - findDayOffset();
            getItemBounds(day, mTempRect);
            node.setContentDescription(getItemDescription(day));
            node.setBoundsInParent(mTempRect);
            node.addAction(AccessibilityNodeInfo.ACTION_CLICK);
            if (mSelectedDay != View.NO_ID && day == mSelectedDay) {
                node.addAction(AccessibilityNodeInfo.ACTION_SELECT);
                node.setClickable(true);
                node.setCheckable(true);
                node.setChecked(true);
            }
        }

        @Override
        protected boolean onPerformActionForVirtualView(int virtualViewId, int action, Bundle arguments) {
            if (action != AccessibilityNodeInfo.ACTION_CLICK) {
                return false;
            }

            int day = virtualViewId - findDayOffset();
            if ((mIsFirstMonth && day < mEnabledDayStart) || (mIsLastMonth && day > mEnabledDayEnd)) {
                return true;
            }

            if (day <= 0) {
                if (mIsLunar) {
                    int month = mMonth - (mIsLeapMonth ? 0 : 1);
                    if (month < 0) {
                        int daysInMonthLunar = getDaysInMonthLunar(Calendar.DECEMBER, mYear - 1, mIsLeapMonth);
                        onDeactivatedDayClick(mYear - 1, month, daysInMonthLunar + day, true);
                    } else {
                        int daysInMonthLunar = getDaysInMonthLunar(month, mYear, mIsLeapMonth);
                        onDeactivatedDayClick(mYear, month, daysInMonthLunar + day, true);
                    }
                } else {
                    Calendar calendar = Calendar.getInstance();
                    calendar.clear();
                    calendar.set(mYear, mMonth, 1);
                    calendar.add(Calendar.DAY_OF_MONTH, day - 1);
                    onDeactivatedDayClick(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), true);
                }
            } else if (day <= mNumCells) {
                onDayClick(mYear, mMonth, day);
            } else if (mIsLunar) {
                int month = mMonth + 1;
                if (month > Calendar.DECEMBER) {
                    onDeactivatedDayClick(mYear + 1, 0, day - mNumCells, false);
                } else {
                    onDeactivatedDayClick(mYear, month, day - mNumCells, false);
                }
            } else {
                Calendar calendar = Calendar.getInstance();
                calendar.clear();
                calendar.set(mYear, mMonth, mNumCells);
                calendar.add(Calendar.DAY_OF_MONTH, day - mNumCells);
                onDeactivatedDayClick(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), false);
            }

            return true;
        }

        private void getItemBounds(int virtualViewId, Rect bounds) {
            int i = mCalendarWidth / mNumDays;
            int day = (virtualViewId - 1) + findDayOffset();
            int i2 = day / mNumDays;

            final int left = mPadding + ((day % mNumDays) * i);
            final int top = ((int) (mContext.getResources().getDisplayMetrics().density * -1.0f)) + (i2 * mWeekHeight);
            final int right = i + left;
            final int bottom = mWeekHeight + top;
            bounds.set(left, top, right, bottom);
        }

        // kang
        private CharSequence getItemDescription(int id) {
            this.mTempCalendar.set(SeslSimpleMonthView.this.mYear, SeslSimpleMonthView.this.mMonth, id);
            String formatDateTime = DateUtils.formatDateTime(SeslSimpleMonthView.this.mContext, this.mTempCalendar.getTimeInMillis(), 22);
            if (!SeslSimpleMonthView.this.mIsLunar || SeslSimpleMonthView.this.mPathClassLoader == null) {
                return formatDateTime;
            }
            int i2 = SeslSimpleMonthView.this.mYear;
            int i3 = SeslSimpleMonthView.this.mMonth;
            boolean z = SeslSimpleMonthView.this.mIsLeapMonth;
            if (id <= 0) {
                i3 = SeslSimpleMonthView.this.mMonth - (!SeslSimpleMonthView.this.mIsLeapMonth ? 1 : 0);
                z = SeslSimpleMonthView.this.mIsPrevMonthLeap;
                if (i3 < 0) {
                    i2--;
                    i3 = 11;
                }
                id += SeslSimpleMonthView.this.getDaysInMonthLunar(i3, i2, z);
            } else if (id > SeslSimpleMonthView.this.mNumCells) {
                i3 = SeslSimpleMonthView.this.mMonth + (!SeslSimpleMonthView.this.mIsNextMonthLeap ? 1 : 0);
                z = SeslSimpleMonthView.this.mIsNextMonthLeap;
                if (i3 > 11) {
                    i2++;
                    i3 = 0;
                }
                id -= SeslSimpleMonthView.this.mNumCells;
            }
            SeslSolarLunarConverterReflector.convertLunarToSolar(SeslSimpleMonthView.this.mPathClassLoader, SeslSimpleMonthView.this.mSolarLunarConverter, i2, i3, id, z);
            int year = SeslSolarLunarConverterReflector.getYear(SeslSimpleMonthView.this.mPathClassLoader, SeslSimpleMonthView.this.mSolarLunarConverter);
            int month = SeslSolarLunarConverterReflector.getMonth(SeslSimpleMonthView.this.mPathClassLoader, SeslSimpleMonthView.this.mSolarLunarConverter);
            int day = SeslSolarLunarConverterReflector.getDay(SeslSimpleMonthView.this.mPathClassLoader, SeslSimpleMonthView.this.mSolarLunarConverter);
            Calendar instance = Calendar.getInstance();
            instance.set(year, month, day);
            return SeslLunarDateUtilsReflector.buildLunarDateString(SeslSimpleMonthView.this.mPathClassLoader, instance, SeslSimpleMonthView.this.getContext());
        }
        // kang
    }

    public int getWeekStart() {
        return mWeekStart;
    }

    public int getDayOfWeekStart() {
        return mDayOfWeekStart - (mWeekStart - 1);
    }

    public int getNumDays() {
        return mNumDays;
    }

    public void setStartDate(Calendar calendar, int isLeapStartMonth) {
        mStartYear = calendar.get(Calendar.YEAR);
        mStartMonth = calendar.get(Calendar.MONTH);
        mStartDay = calendar.get(Calendar.DAY_OF_MONTH);
        mIsLeapStartMonth = isLeapStartMonth;
    }

    public void setEndDate(Calendar calendar, int isLeapStartMonth) {
        mEndYear = calendar.get(Calendar.YEAR);
        mEndMonth = calendar.get(Calendar.MONTH);
        mEndDay = calendar.get(Calendar.DAY_OF_MONTH);
        mIsLeapEndMonth = isLeapStartMonth;
    }

    private boolean isRTL() {
        Locale locale = Locale.getDefault();
        if ("ur".equals(locale.getLanguage())) {
            return false;
        }
        byte directionality = Character.getDirectionality(locale.getDisplayName(locale).charAt(0));
        return directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT || directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC;
    }

    public void setLunar(boolean isLunar, boolean isLeapMonth, PathClassLoader pathClassLoader) {
        mIsLunar = isLunar;
        mIsLeapMonth = isLeapMonth;
        if (isLunar && mSolarLunarConverter == null) {
            mPathClassLoader = pathClassLoader;
            mSolarLunarConverter = SeslFeatureReflector.getSolarLunarConverter(pathClassLoader);
        }
    }

    public void setFirstMonth() {
        mIsFirstMonth = true;
    }

    public void setLastMonth() {
        mIsLastMonth = true;
    }

    public void setPrevMonthLeap() {
        mIsPrevMonthLeap = true;
    }

    public void setNextMonthLeap() {
        mIsNextMonthLeap = true;
    }

    private boolean isHighContrastFontEnabled() {
        return SeslViewReflector.isHighContrastTextEnabled(this);
    }

    public interface OnDayClickListener {
        void onDayClick(SeslSimpleMonthView view, int year, int month, int day);
    }

    public interface OnDeactivatedDayClickListener {
        void onDeactivatedDayClick(SeslSimpleMonthView view, int year, int month, int day, boolean isLeapMonth, boolean pageChanged);
    }
}
