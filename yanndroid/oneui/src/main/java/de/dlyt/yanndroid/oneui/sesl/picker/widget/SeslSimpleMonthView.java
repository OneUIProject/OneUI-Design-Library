package de.dlyt.yanndroid.oneui.sesl.picker.widget;

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
import android.view.View;
import android.view.accessibility.AccessibilityEvent;

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

public class SeslSimpleMonthView extends View {
    public Paint mAbnormalSelectedDayPaint;
    public final int mAbnormalStartEndDateBackgroundAlpha;
    public final Calendar mCalendar;
    public int mCalendarWidth;
    public Context mContext;
    public int[] mDayColorSet;
    public int mDayNumberDisabledAlpha;
    public Paint mDayNumberPaint;
    public Paint mDayNumberSelectedPaint;
    public int mDayOfWeekStart;
    public int mDaySelectedCircleSize;
    public int mDaySelectedCircleStroke;
    public int mEnabledDayEnd;
    public int mEnabledDayStart;
    public int mEndDay;
    public int mEndMonth;
    public int mEndYear;
    public Paint mHcfEnabledDayNumberPaint;
    public boolean mIsFirstMonth;
    public boolean mIsHcfEnabled;
    public boolean mIsLastMonth;
    public int mIsLeapEndMonth;
    public boolean mIsLeapMonth;
    public int mIsLeapStartMonth;
    public boolean mIsLunar;
    public boolean mIsNextMonthLeap;
    public boolean mIsPrevMonthLeap;
    public boolean mIsRTL;
    public int mLastAccessibilityFocusedView;
    public boolean mLockAccessibilityDelegate;
    public boolean mLostAccessibilityFocus;
    public Calendar mMaxDate;
    public Calendar mMinDate;
    public int mMiniDayNumberTextSize;
    public int mMode;
    public int mMonth;
    public int mNormalTextColor;
    public int mNumCells;
    public int mNumDays;
    public OnDayClickListener mOnDayClickListener;
    public OnDeactivatedDayClickListener mOnDeactivatedDayClickListener;
    public int mPadding;
    public PathClassLoader mPathClassLoader;
    public final int mPrevNextMonthDayNumberAlpha;
    public int mSaturdayTextColor;
    public int mSelectedDay;
    public int mSelectedDayColor;
    public int mSelectedDayNumberTextColor;
    public Object mSolarLunarConverter;
    public int mStartDay;
    public int mStartMonth;
    public int mStartYear;
    public int mSundayTextColor;
    public Calendar mTempDate;
    public final MonthViewTouchHelper mTouchHelper;
    public int mWeekHeight;
    public int mWeekStart;
    public int mYear;

    public interface OnDayClickListener {
        void onDayClick(SeslSimpleMonthView seslSimpleMonthView, int i, int i2, int i3);
    }

    public interface OnDeactivatedDayClickListener {
        void onDeactivatedDayClick(SeslSimpleMonthView seslSimpleMonthView, int i, int i2, int i3, boolean z, boolean z2);
    }

    public static boolean isValidDayOfWeek(int i) {
        return i >= 1 && i <= 7;
    }

    public static boolean isValidMonth(int i) {
        return i >= 0 && i <= 11;
    }

    public SeslSimpleMonthView(Context context) {
        this(context, null);
    }

    public SeslSimpleMonthView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 16843612);
    }

    public SeslSimpleMonthView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet);
        this.mDayColorSet = new int[7];
        this.mMode = 0;
        this.mDayOfWeekStart = 0;
        this.mPadding = 0;
        this.mSelectedDay = -1;
        this.mWeekStart = 1;
        this.mNumDays = 7;
        this.mNumCells = 7;
        this.mEnabledDayStart = 1;
        this.mEnabledDayEnd = 31;
        this.mIsHcfEnabled = false;
        this.mCalendar = Calendar.getInstance();
        this.mMinDate = Calendar.getInstance();
        this.mMaxDate = Calendar.getInstance();
        this.mTempDate = Calendar.getInstance();
        this.mIsLunar = false;
        this.mIsLeapMonth = false;
        this.mPathClassLoader = null;
        this.mIsFirstMonth = false;
        this.mIsLastMonth = false;
        this.mIsPrevMonthLeap = false;
        this.mIsNextMonthLeap = false;
        this.mLastAccessibilityFocusedView = -1;
        this.mLostAccessibilityFocus = false;
        this.mContext = context;
        this.mIsRTL = isRTL();
        Resources resources = context.getResources();
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorPrimaryDark, typedValue, true);
        int i2 = typedValue.resourceId;
        if (i2 != 0) {
            this.mSelectedDayColor = resources.getColor(i2);
        } else {
            this.mSelectedDayColor = typedValue.data;
        }
        this.mSundayTextColor = resources.getColor(R.color.sesl_date_picker_sunday_number_text_color);
        this.mSaturdayTextColor = resources.getColor(R.color.sesl_date_picker_saturday_text_color);
        TypedArray obtainStyledAttributes = this.mContext.obtainStyledAttributes(attributeSet, R.styleable.DatePicker, i, 0);
        this.mNormalTextColor = obtainStyledAttributes.getColor(R.styleable.DatePicker_dayNumberTextColor, resources.getColor(R.color.sesl_date_picker_normal_day_number_text_color));
        this.mSelectedDayNumberTextColor = obtainStyledAttributes.getColor(R.styleable.DatePicker_selectedDayNumberTextColor, resources.getColor(R.color.sesl_date_picker_selected_day_number_text_color));
        this.mDayNumberDisabledAlpha = obtainStyledAttributes.getInteger(R.styleable.DatePicker_dayNumberDisabledAlpha, resources.getInteger(R.integer.sesl_day_number_disabled_alpha_light));
        obtainStyledAttributes.recycle();
        this.mWeekHeight = resources.getDimensionPixelOffset(R.dimen.sesl_date_picker_calendar_week_height);
        this.mDaySelectedCircleSize = resources.getDimensionPixelSize(R.dimen.sesl_date_picker_selected_day_circle_radius);
        this.mDaySelectedCircleStroke = resources.getDimensionPixelSize(R.dimen.sesl_date_picker_selected_day_circle_stroke);
        this.mMiniDayNumberTextSize = resources.getDimensionPixelSize(R.dimen.sesl_date_picker_day_number_text_size);
        this.mCalendarWidth = resources.getDimensionPixelOffset(R.dimen.sesl_date_picker_calendar_view_width);
        this.mPadding = resources.getDimensionPixelOffset(R.dimen.sesl_date_picker_calendar_view_padding);
        MonthViewTouchHelper monthViewTouchHelper = new MonthViewTouchHelper(this);
        this.mTouchHelper = monthViewTouchHelper;
        ViewCompat.setAccessibilityDelegate(this, monthViewTouchHelper);
        setImportantForAccessibility(1);
        this.mLockAccessibilityDelegate = true;
        if (Settings.System.getString(this.mContext.getContentResolver(), "current_sec_active_themepackage") != null) {
            this.mDayNumberDisabledAlpha = resources.getInteger(R.integer.sesl_day_number_theme_disabled_alpha);
        }
        this.mPrevNextMonthDayNumberAlpha = resources.getInteger(R.integer.sesl_day_number_theme_disabled_alpha);
        this.mAbnormalStartEndDateBackgroundAlpha = resources.getInteger(R.integer.sesl_date_picker_abnormal_start_end_date_background_alpha);
        initView();
    }

    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        this.mIsRTL = isRTL();
        this.mTouchHelper.invalidateRoot();
        Resources resources = this.mContext.getResources();
        this.mWeekHeight = resources.getDimensionPixelOffset(R.dimen.sesl_date_picker_calendar_week_height);
        this.mDaySelectedCircleSize = resources.getDimensionPixelSize(R.dimen.sesl_date_picker_selected_day_circle_radius);
        this.mMiniDayNumberTextSize = resources.getDimensionPixelSize(R.dimen.sesl_date_picker_day_number_text_size);
        initView();
    }

    public void setTextColor(String str) {
        if (str == null) {
            str = SeslCscFeatureReflector.getString("CscFeature_Calendar_SetColorOfDays", "XXXXXXR");
        }
        for (int i = 0; i < this.mNumDays; i++) {
            char charAt = str.charAt(i);
            int i2 = (i + 2) % this.mNumDays;
            if (charAt == 'R') {
                this.mDayColorSet[i2] = this.mSundayTextColor;
            } else if (charAt == 'B') {
                this.mDayColorSet[i2] = this.mSaturdayTextColor;
            } else {
                this.mDayColorSet[i2] = this.mNormalTextColor;
            }
        }
    }

    public void setAccessibilityDelegate(AccessibilityDelegate accessibilityDelegate) {
        if (!this.mLockAccessibilityDelegate) {
            super.setAccessibilityDelegate(accessibilityDelegate);
        }
    }

    public void setOnDayClickListener(OnDayClickListener onDayClickListener) {
        this.mOnDayClickListener = onDayClickListener;
    }

    public void setOnDeactivatedDayClickListener(OnDeactivatedDayClickListener onDeactivatedDayClickListener) {
        this.mOnDeactivatedDayClickListener = onDeactivatedDayClickListener;
    }

    public boolean dispatchHoverEvent(MotionEvent motionEvent) {
        return this.mTouchHelper.dispatchHoverEvent(motionEvent) || super.dispatchHoverEvent(motionEvent);
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (motionEvent.getAction() == 1) {
            int dayFromLocation = getDayFromLocation(motionEvent.getX(), motionEvent.getY());
            if ((this.mIsFirstMonth && dayFromLocation < this.mEnabledDayStart) || (this.mIsLastMonth && dayFromLocation > this.mEnabledDayEnd)) {
                return true;
            }
            int i = 11;
            if (dayFromLocation > 0) {
                int i2 = this.mNumCells;
                if (dayFromLocation <= i2) {
                    onDayClick(this.mYear, this.mMonth, dayFromLocation);
                } else if (this.mIsLunar) {
                    int i3 = this.mYear;
                    int i4 = this.mMonth + (!this.mIsNextMonthLeap ? 1 : 0);
                    if (i4 > 11) {
                        i3++;
                        i4 = 0;
                    }
                    onDeactivatedDayClick(i3, i4, dayFromLocation - i2, false);
                } else {
                    Calendar instance = Calendar.getInstance();
                    instance.clear();
                    instance.set(this.mYear, this.mMonth, this.mNumCells);
                    instance.add(5, dayFromLocation - this.mNumCells);
                    onDeactivatedDayClick(instance.get(1), instance.get(2), instance.get(5), false);
                }
            } else if (this.mIsLunar) {
                int i5 = this.mYear;
                int i6 = this.mMonth - (!this.mIsLeapMonth ? 1 : 0);
                if (i6 < 0) {
                    i5--;
                } else {
                    i = i6;
                }
                onDeactivatedDayClick(i5, i, getDaysInMonthLunar(i, i5, this.mIsPrevMonthLeap) + dayFromLocation, true);
            } else {
                Calendar instance2 = Calendar.getInstance();
                instance2.clear();
                instance2.set(this.mYear, this.mMonth, 1);
                instance2.add(5, dayFromLocation - 1);
                onDeactivatedDayClick(instance2.get(1), instance2.get(2), instance2.get(5), true);
            }
        }
        return true;
    }

    public final void initView() {
        Paint paint = new Paint();
        this.mDayNumberSelectedPaint = paint;
        paint.setAntiAlias(true);
        this.mDayNumberSelectedPaint.setColor(this.mSelectedDayColor);
        this.mDayNumberSelectedPaint.setTextAlign(Paint.Align.CENTER);
        this.mDayNumberSelectedPaint.setStrokeWidth((float) this.mDaySelectedCircleStroke);
        this.mDayNumberSelectedPaint.setFakeBoldText(true);
        this.mDayNumberSelectedPaint.setStyle(Paint.Style.FILL);
        Paint paint2 = new Paint(this.mDayNumberSelectedPaint);
        this.mAbnormalSelectedDayPaint = paint2;
        paint2.setColor(this.mNormalTextColor);
        this.mAbnormalSelectedDayPaint.setAlpha(this.mAbnormalStartEndDateBackgroundAlpha);
        Paint paint3 = new Paint();
        this.mDayNumberPaint = paint3;
        paint3.setAntiAlias(true);
        this.mDayNumberPaint.setTextSize((float) this.mMiniDayNumberTextSize);
        this.mDayNumberPaint.setTypeface(Typeface.create("sec-roboto-light", 0));
        this.mDayNumberPaint.setTextAlign(Paint.Align.CENTER);
        this.mDayNumberPaint.setStyle(Paint.Style.FILL);
        this.mDayNumberPaint.setFakeBoldText(false);
        Paint paint4 = new Paint(this.mDayNumberPaint);
        this.mHcfEnabledDayNumberPaint = paint4;
        paint4.setTypeface(Typeface.create("sec-roboto-light", 1));
    }

    public void onDraw(Canvas canvas) {
        drawDays(canvas);
    }

    public void setMonthParams(int i, int i2, int i3, int i4, int i5, int i6, Calendar calendar, Calendar calendar2, int i7, int i8, int i9, int i10, int i11, int i12, int i13, int i14, int i15) {
        Object obj;
        this.mMode = i15;
        if (this.mWeekHeight < 10) {
            this.mWeekHeight = 10;
        }
        this.mSelectedDay = i;
        if (isValidMonth(i2)) {
            this.mMonth = i2;
        }
        this.mYear = i3;
        this.mCalendar.clear();
        this.mCalendar.set(2, this.mMonth);
        this.mCalendar.set(1, this.mYear);
        this.mCalendar.set(5, 1);
        this.mMinDate = calendar;
        this.mMaxDate = calendar2;
        if (!this.mIsLunar || (obj = this.mSolarLunarConverter) == null) {
            this.mDayOfWeekStart = this.mCalendar.get(7);
            this.mNumCells = getDaysInMonth(this.mMonth, this.mYear);
        } else {
            SeslSolarLunarConverterReflector.convertLunarToSolar(this.mPathClassLoader, obj, this.mYear, this.mMonth, 1, this.mIsLeapMonth);
            this.mDayOfWeekStart = SeslSolarLunarConverterReflector.getWeekday(this.mPathClassLoader, this.mSolarLunarConverter, SeslSolarLunarConverterReflector.getYear(this.mPathClassLoader, this.mSolarLunarConverter), SeslSolarLunarConverterReflector.getMonth(this.mPathClassLoader, this.mSolarLunarConverter), SeslSolarLunarConverterReflector.getDay(this.mPathClassLoader, this.mSolarLunarConverter)) + 1;
            this.mNumCells = getDaysInMonthLunar(this.mMonth, this.mYear, this.mIsLeapMonth);
        }
        if (isValidDayOfWeek(i4)) {
            this.mWeekStart = i4;
        } else {
            this.mWeekStart = this.mCalendar.getFirstDayOfWeek();
        }
        int i16 = (this.mMonth == calendar.get(2) && this.mYear == calendar.get(1)) ? calendar.get(5) : i5;
        int i17 = (this.mMonth == calendar2.get(2) && this.mYear == calendar2.get(1)) ? calendar2.get(5) : i6;
        if (i16 > 0 && i17 < 32) {
            this.mEnabledDayStart = i16;
        }
        if (i17 > 0 && i17 < 32 && i17 >= i16) {
            this.mEnabledDayEnd = i17;
        }
        this.mTouchHelper.invalidateRoot();
        this.mStartYear = i7;
        this.mStartMonth = i8;
        this.mStartDay = i9;
        this.mIsLeapStartMonth = i10;
        this.mEndYear = i11;
        this.mEndMonth = i12;
        this.mEndDay = i13;
        this.mIsLeapEndMonth = i14;
    }

    public final int getDaysInMonthLunar(int i, int i2, boolean z) {
        int daysInMonth = getDaysInMonth(i, i2);
        Object obj = this.mSolarLunarConverter;
        if (obj != null) {
            return SeslSolarLunarConverterReflector.getDayLengthOf(this.mPathClassLoader, obj, i2, i, z);
        }
        Log.e("SeslSimpleMonthView", "getDaysInMonthLunar, mSolarLunarConverter is null");
        return daysInMonth;
    }

    public static int getDaysInMonth(int i, int i2) {
        switch (i) {
            case 0:
            case 2:
            case 4:
            case 6:
            case 7:
            case 9:
            case 11:
                return 31;
            case 1:
                if (i2 % 4 != 0) {
                    return 28;
                }
                if (i2 % 100 != 0 || i2 % 400 == 0) {
                    return 29;
                }
                return 28;
            case 3:
            case 5:
            case 8:
            case 10:
                return 30;
            default:
                throw new IllegalArgumentException("Invalid Month");
        }
    }

    public void onMeasure(int i, int i2) {
        super.onMeasure(makeMeasureSpec(i, this.mCalendarWidth), i2);
    }

    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        int i5;
        int i6;
        boolean z2 = this.mLostAccessibilityFocus;
        if (!z2 && this.mLastAccessibilityFocusedView == -1 && (i6 = this.mSelectedDay) != -1) {
            this.mTouchHelper.sendEventForVirtualView(i6 + findDayOffset(), 32768);
        } else if (!z2 && (i5 = this.mLastAccessibilityFocusedView) != -1) {
            this.mTouchHelper.sendEventForVirtualView(i5 + findDayOffset(), 32768);
        }
        if (z) {
            this.mTouchHelper.invalidateRoot();
        }
        super.onLayout(z, i, i2, i3, i4);
    }

    public final int makeMeasureSpec(int i, int i2) {
        if (i2 == -1) {
            return i;
        }
        int size = MeasureSpec.getSize(i);
        int mode = MeasureSpec.getMode(i);
        if (mode == Integer.MIN_VALUE) {
            int min = Math.min(size, i2);
            this.mCalendarWidth = min;
            return MeasureSpec.makeMeasureSpec(min, 1073741824);
        } else if (mode == 0) {
            return MeasureSpec.makeMeasureSpec(i2, 1073741824);
        } else {
            if (mode == 1073741824) {
                this.mCalendarWidth = size;
                return i;
            }
            throw new IllegalArgumentException("Unknown measure mode: " + mode);
        }
    }

    public void onSizeChanged(int i, int i2, int i3, int i4) {
        this.mTouchHelper.invalidateRoot();
    }

    public final void drawDays(Canvas var1) {
        int var2 = this.mWeekHeight * 2 / 3;
        int var3 = this.mCalendarWidth / (this.mNumDays * 2);
        int var4 = this.findDayOffset();
        float var5 = (float) this.mMiniDayNumberTextSize / 2.7F;
        int var6 = this.mStartYear;
        float var7 = (float) this.mStartMonth;
        int var8 = this.mStartDay;
        int var9 = this.mEndYear;
        float var10 = (float) this.mEndMonth;
        int var11 = this.mEndDay;
        boolean var12 = this.mIsLunar;
        float var13 = var7;
        if (var12) {
            var13 = var7;
            if (this.mIsLeapStartMonth == 1) {
                var13 = var7 + 0.5F;
            }
        }

        float var14 = var13;
        var7 = var10;
        if (var12) {
            var7 = var10;
            if (this.mIsLeapEndMonth == 1) {
                var7 = var10 + 0.5F;
            }
        }

        int var15 = this.mYear;
        var13 = (float) this.mMonth;
        var10 = var13;
        if (var12) {
            var10 = var13;
            if (this.mIsLeapMonth) {
                var10 = var13 + 0.5F;
            }
        }

        int var16 = var6 * 10000 + (int) (var14 * 100.0F);
        int var17 = var9 * 10000 + (int) (var7 * 100.0F);
        int var18 = var15 * 10000 + (int) (100.0F * var10);
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
        label404:
        {
            if (!var20) {
                label413:
                {
                    label410:
                    {
                        if (var6 == var9 && var14 == var7 && var15 == var6 && var10 == var14) {
                            var17 = var11;
                        } else {
                            if (var16 < var18 && var18 < var17 && (var15 != var9 || var10 != var7)) {
                                var17 = this.mNumCells + 1;
                                break label410;
                            }

                            if (var15 != var6 || var10 != var14) {
                                if (var15 != var9 || var10 != var7) {
                                    break label413;
                                }

                                var17 = var11;
                                break label410;
                            }

                            var17 = this.mNumCells + 1;
                        }

                        var31 = var8;
                        break label404;
                    }

                    var31 = 0;
                    break label404;
                }
            }

            var31 = -1;
            var17 = -1;
        }

        this.mIsHcfEnabled = this.isHighContrastFontEnabled();
        var18 = 0;
        byte var21 = 1;
        var16 = var4;
        var13 = var5;
        int var22 = var11;
        var11 = var2;

        Paint var25;
        Paint var26;
        for (var2 = var21; var2 <= this.mNumCells; ++var2) {
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
                label380:
                {
                    label322:
                    {
                        label381:
                        {
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

                        var1.drawCircle((float) var24, (float) var11 - var13, (float) this.mDaySelectedCircleSize, this.mDayNumberSelectedPaint);
                        var26.setColor(this.mSelectedDayNumberTextColor);
                    }

                    label382:
                    {
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

                    var1.drawCircle((float) var24, (float) var11 - var13, (float) this.mDaySelectedCircleSize, this.mAbnormalSelectedDayPaint);
                }
            } else {
                float var27;
                float var28;
                if (var31 < var2 && var2 < var17) {
                    var27 = (float) (var24 - var3);
                    var28 = (float) var11;
                    var33 = this.mDaySelectedCircleSize;
                    var28 = var28 - var13 - (float) var33;
                    var1.drawRect(var27, var28, var27 + (float) (var3 * 2), var28 + (float) (var33 * 2), this.mDayNumberSelectedPaint);
                    var26.setColor(this.mSelectedDayNumberTextColor);
                }

                var33 = var31;
                if (var31 != -1 && var31 == var17 && var2 == var31) {
                    var1.drawCircle((float) var24, (float) var11 - var13, (float) this.mDaySelectedCircleSize, this.mDayNumberSelectedPaint);
                    var26.setColor(this.mSelectedDayNumberTextColor);
                    var31 = var31;
                } else if (var17 == var2) {
                    var27 = (float) var11 - var13;
                    if (this.mIsRTL) {
                        var5 = (float) var24;
                    } else {
                        var5 = (float) (var24 - var3);
                    }

                    var31 = this.mDaySelectedCircleSize;
                    var28 = var27 - (float) var31;
                    var1.drawRect(var5, var28, (float) var3 + var5, var28 + (float) (var31 * 2), this.mDayNumberSelectedPaint);
                    var1.drawCircle((float) var24, var27, (float) this.mDaySelectedCircleSize, this.mDayNumberSelectedPaint);
                    var26.setColor(this.mSelectedDayNumberTextColor);
                    var31 = var33;
                } else {
                    var31 = var31;
                    if (var33 == var2) {
                        var27 = (float) var11 - var13;
                        if (this.mIsRTL) {
                            var5 = (float) (var24 - var3);
                        } else {
                            var5 = (float) var24;
                        }

                        var31 = this.mDaySelectedCircleSize;
                        var28 = var27 - (float) var31;
                        var1.drawRect(var5, var28, (float) var3 + var5, var28 + (float) (var31 * 2), this.mDayNumberSelectedPaint);
                        var1.drawCircle((float) var24, var27, (float) this.mDaySelectedCircleSize, this.mDayNumberSelectedPaint);
                        var26.setColor(this.mSelectedDayNumberTextColor);
                        var31 = var33;
                    }
                }
            }

            if (this.mMode == 0 && var2 == var17) {
                var26.setColor(this.mSelectedDayNumberTextColor);
            }

            var1.drawText(String.format("%d", var2), (float) var24, (float) var11, var26);
            ++var16;
            if (var16 == this.mNumDays) {
                var11 += this.mWeekHeight;
                ++var18;
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

            while (true) {
                var31 = var11;
                if (var18 == 6) {
                    break;
                }

                if (this.mIsRTL) {
                    var31 = ((this.mNumDays - 1 - var32) * 2 + 1) * var3 + this.mPadding;
                } else {
                    var31 = (var32 * 2 + 1) * var3 + this.mPadding;
                }

                var6 = this.mWeekStart;
                var8 = this.mNumDays;
                this.mDayNumberPaint.setColor(this.mDayColorSet[(var32 + var6) % var8]);
                this.mDayNumberPaint.setAlpha(this.mPrevNextMonthDayNumberAlpha);
                if (this.mMode != 0 && var17 == this.mNumCells + 1) {
                    if (var2 >= this.mEndDay && this.isNextMonthEndMonth()) {
                        if (var2 == this.mEndDay) {
                            var10 = (float) var16 - var13;
                            if (this.mIsRTL) {
                                var7 = (float) var31;
                            } else {
                                var7 = (float) (var31 - var3);
                            }

                            var8 = this.mDaySelectedCircleSize;
                            var14 = var10 - (float) var8;
                            var1.drawRect(var7, var14, (float) var3 + var7, var14 + (float) (var8 * 2), this.mDayNumberSelectedPaint);
                            var1.drawCircle((float) var31, var10, (float) this.mDaySelectedCircleSize, this.mDayNumberSelectedPaint);
                        }
                    } else {
                        var7 = (float) (var31 - var3);
                        var10 = (float) var16;
                        var8 = this.mDaySelectedCircleSize;
                        var10 = var10 - var13 - (float) var8;
                        var1.drawRect(var7, var10, var7 + (float) (var3 * 2), var10 + (float) (var8 * 2), this.mDayNumberSelectedPaint);
                    }
                }

                if (!this.mIsLunar) {
                    var22 = this.mMonth + 1;
                    var9 = this.mYear;
                    var6 = var22;
                    var8 = var9;
                    if (var22 > 11) {
                        var8 = var9 + 1;
                        var6 = 0;
                    }

                    this.mTempDate.clear();
                    this.mTempDate.set(var8, var6, var2);
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

                if (this.mMode != 0 && var17 == this.mNumCells + 1 && (var2 <= this.mEndDay || !this.isNextMonthEndMonth())) {
                    var26.setColor(this.mSelectedDayNumberTextColor);
                }

                var1.drawText(String.format("%d", var2), (float) var31, (float) var16, var26);
                var31 = var32 + 1;
                if (var31 == this.mNumDays) {
                    var16 += this.mWeekHeight;
                    ++var18;
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
                var18 = this.mYear;
                var2 = this.mMonth - ((this.mIsLeapMonth ? 1 : 0) ^ 1);
                var17 = var18;
                var11 = var2;
                if (var2 < 0) {
                    var17 = var18 - 1;
                    var11 = 11;
                }

                var11 = this.getDaysInMonthLunar(var11, var17, this.mIsPrevMonthLeap) - var4 + 1;
            }

            var17 = var11;

            for (var11 = 0; var11 < var4; ++var11) {
                if (this.mIsRTL) {
                    var18 = ((this.mNumDays - 1 - var11) * 2 + 1) * var3;
                    var2 = this.mPadding;
                } else {
                    var18 = (var11 * 2 + 1) * var3;
                    var2 = this.mPadding;
                }

                var8 = var18 + var2;
                var6 = this.mWeekHeight * 2 / 3;
                var18 = this.mWeekStart;
                var2 = this.mNumDays;
                this.mDayNumberPaint.setColor(this.mDayColorSet[(var18 + var11) % var2]);
                this.mDayNumberPaint.setAlpha(this.mPrevNextMonthDayNumberAlpha);
                if (this.mMode != 0 && var31 == 0) {
                    if (var17 <= this.mStartDay && this.isPrevMonthStartMonth()) {
                        if (var17 == this.mStartDay) {
                            var10 = (float) var6 - var13;
                            if (this.mIsRTL) {
                                var7 = (float) (var8 - var3);
                            } else {
                                var7 = (float) var8;
                            }

                            var2 = this.mDaySelectedCircleSize;
                            var14 = var10 - (float) var2;
                            var1.drawRect(var7, var14, (float) var3 + var7, var14 + (float) (var2 * 2), this.mDayNumberSelectedPaint);
                            var1.drawCircle((float) var8, var10, (float) this.mDaySelectedCircleSize, this.mDayNumberSelectedPaint);
                        }
                    } else {
                        var7 = (float) (var8 - var3);
                        var10 = (float) var6;
                        var2 = this.mDaySelectedCircleSize;
                        var10 = var10 - var13 - (float) var2;
                        var1.drawRect(var7, var10, var7 + (float) (var3 * 2), var10 + (float) (var2 * 2), this.mDayNumberSelectedPaint);
                    }
                }

                if (!this.mIsLunar) {
                    var32 = this.mMonth - 1;
                    var16 = this.mYear;
                    var18 = var32;
                    var2 = var16;
                    if (var32 < 0) {
                        var2 = var16 - 1;
                        var18 = 11;
                    }

                    this.mTempDate.clear();
                    this.mTempDate.set(var2, var18, var17);
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

                if (this.mMode != 0 && var31 == 0 && (var17 >= this.mStartDay || !this.isPrevMonthStartMonth())) {
                    var26.setColor(this.mSelectedDayNumberTextColor);
                }

                var1.drawText(String.format("%d", var17), (float) var8, (float) var6, var26);
                ++var17;
            }
        }

    }

    public final boolean isPrevMonthStartMonth() {
        if (this.mIsLunar) {
            float f = (float) this.mMonth;
            float f2 = (float) this.mStartMonth;
            if (this.mIsLeapMonth) {
                f += 0.5f;
            }
            if (this.mIsLeapStartMonth == 1) {
                f2 += 0.5f;
            }
            float f3 = f - f2;
            int i = this.mYear;
            int i2 = this.mStartYear;
            if (i != i2 || (f3 >= 1.0f && (f3 != 1.0f || this.mIsPrevMonthLeap))) {
                if (i != i2 + 1) {
                    return false;
                }
                float f4 = f3 + 12.0f;
                if (f4 >= 1.0f && (f4 != 1.0f || this.mIsPrevMonthLeap)) {
                    return false;
                }
            }
            return true;
        }
        int i3 = this.mYear;
        int i4 = this.mStartYear;
        return (i3 == i4 && this.mMonth == this.mStartMonth + 1) || (i3 == i4 + 1 && this.mMonth == 0 && this.mStartMonth == 11);
    }

    public final boolean isNextMonthEndMonth() {
        if (this.mIsLunar) {
            float f = (float) this.mMonth;
            float f2 = (float) this.mEndMonth;
            if (this.mIsLeapMonth) {
                f += 0.5f;
            }
            if (this.mIsLeapEndMonth == 1) {
                f2 += 0.5f;
            }
            float f3 = f2 - f;
            int i = this.mYear;
            int i2 = this.mEndYear;
            if (i != i2 || (f3 >= 1.0f && (f3 != 1.0f || this.mIsNextMonthLeap))) {
                if (i != i2 - 1) {
                    return false;
                }
                float f4 = f3 + 12.0f;
                if (f4 >= 1.0f && (f4 != 1.0f || this.mIsNextMonthLeap)) {
                    return false;
                }
            }
            return true;
        }
        int i3 = this.mYear;
        int i4 = this.mEndYear;
        return (i3 == i4 && this.mMonth == this.mEndMonth - 1) || (i3 == i4 - 1 && this.mMonth == 11 && this.mEndMonth == 0);
    }

    public final int findDayOffset() {
        int i = this.mDayOfWeekStart;
        int i2 = this.mWeekStart;
        if (i < i2) {
            i += this.mNumDays;
        }
        return i - i2;
    }

    public final int getDayFromLocation(float f, float f2) {
        int i = this.mPadding;
        if (this.mIsRTL) {
            f = ((float) this.mCalendarWidth) - f;
        }
        float f3 = (float) i;
        if (f < f3) {
            return -1;
        }
        int i2 = this.mCalendarWidth;
        if (f > ((float) (i + i2))) {
            return -1;
        }
        return (((int) (((f - f3) * ((float) this.mNumDays)) / ((float) i2))) - findDayOffset()) + 1 + ((((int) f2) / this.mWeekHeight) * this.mNumDays);
    }

    public final void onDayClick(int i, int i2, int i3) {
        if (this.mOnDayClickListener != null) {
            playSoundEffect(0);
            this.mOnDayClickListener.onDayClick(this, i, i2, i3);
        }
        this.mTouchHelper.sendEventForVirtualView(i3 + findDayOffset(), 1);
    }

    public final void onDeactivatedDayClick(int i, int i2, int i3, boolean z) {
        if (!this.mIsLunar) {
            this.mTempDate.clear();
            this.mTempDate.set(i, i2, i3);
            if (z) {
                Calendar instance = Calendar.getInstance();
                instance.clear();
                instance.set(this.mMinDate.get(1), this.mMinDate.get(2), this.mMinDate.get(5));
                if (this.mTempDate.before(instance)) {
                    return;
                }
            } else if (this.mTempDate.after(this.mMaxDate)) {
                return;
            }
        }
        if (this.mOnDeactivatedDayClickListener != null) {
            playSoundEffect(0);
            this.mOnDeactivatedDayClickListener.onDeactivatedDayClick(this, i, i2, i3, this.mIsLeapMonth, z);
        }
        this.mTouchHelper.sendEventForVirtualView(i3, 1);
    }

    public void clearAccessibilityFocus() {
        this.mTouchHelper.clearFocusedVirtualView();
    }

    public class MonthViewTouchHelper extends ExploreByTouchHelper {
        public final Calendar mTempCalendar = Calendar.getInstance();
        public final Rect mTempRect = new Rect();

        public MonthViewTouchHelper(View view) {
            super(view);
        }

        public void clearFocusedVirtualView() {
            int focusedVirtualView = getFocusedVirtualView();
            if (focusedVirtualView != Integer.MIN_VALUE) {
                getAccessibilityNodeProvider(SeslSimpleMonthView.this).performAction(focusedVirtualView, 128, null);
            }
        }

        @Override // androidx.customview.widget.ExploreByTouchHelper
        public int getVirtualViewAt(float f, float f2) {
            int dayFromLocation = SeslSimpleMonthView.this.getDayFromLocation(f, f2);
            if (SeslSimpleMonthView.this.mIsFirstMonth && dayFromLocation < SeslSimpleMonthView.this.mEnabledDayStart) {
                return Integer.MIN_VALUE;
            }
            if (!SeslSimpleMonthView.this.mIsLastMonth || dayFromLocation <= SeslSimpleMonthView.this.mEnabledDayEnd) {
                return dayFromLocation + SeslSimpleMonthView.this.findDayOffset();
            }
            return Integer.MIN_VALUE;
        }

        @Override // androidx.customview.widget.ExploreByTouchHelper
        public void getVisibleVirtualViews(List<Integer> list) {
            int findDayOffset = SeslSimpleMonthView.this.findDayOffset();
            for (int i = 1; i <= 42; i++) {
                int i2 = i - findDayOffset;
                if ((!SeslSimpleMonthView.this.mIsFirstMonth || i2 >= SeslSimpleMonthView.this.mEnabledDayStart) && (!SeslSimpleMonthView.this.mIsLastMonth || i2 <= SeslSimpleMonthView.this.mEnabledDayEnd)) {
                    list.add(Integer.valueOf(i));
                }
            }
        }

        @Override // androidx.customview.widget.ExploreByTouchHelper
        public void onPopulateEventForVirtualView(int i, AccessibilityEvent accessibilityEvent) {
            int findDayOffset = i - SeslSimpleMonthView.this.findDayOffset();
            if (accessibilityEvent.getEventType() == 32768) {
                SeslSimpleMonthView.this.mLastAccessibilityFocusedView = findDayOffset;
                SeslSimpleMonthView.this.mLostAccessibilityFocus = false;
            }
            if (accessibilityEvent.getEventType() == 65536) {
                SeslSimpleMonthView.this.mLastAccessibilityFocusedView = -1;
                SeslSimpleMonthView.this.mLostAccessibilityFocus = true;
            }
            accessibilityEvent.setContentDescription(getItemDescription(findDayOffset));
        }

        @Override // androidx.customview.widget.ExploreByTouchHelper
        public void onPopulateNodeForVirtualView(int i, AccessibilityNodeInfoCompat accessibilityNodeInfoCompat) {
            int findDayOffset = i - SeslSimpleMonthView.this.findDayOffset();
            getItemBounds(findDayOffset, this.mTempRect);
            accessibilityNodeInfoCompat.setContentDescription(getItemDescription(findDayOffset));
            accessibilityNodeInfoCompat.setBoundsInParent(this.mTempRect);
            accessibilityNodeInfoCompat.addAction(16);
            if (SeslSimpleMonthView.this.mSelectedDay != -1 && findDayOffset == SeslSimpleMonthView.this.mSelectedDay) {
                accessibilityNodeInfoCompat.addAction(4);
                accessibilityNodeInfoCompat.setClickable(true);
                accessibilityNodeInfoCompat.setCheckable(true);
                accessibilityNodeInfoCompat.setChecked(true);
            }
        }

        @Override // androidx.customview.widget.ExploreByTouchHelper
        public boolean onPerformActionForVirtualView(int i, int i2, Bundle bundle) {
            if (i2 != 16) {
                return false;
            }
            int findDayOffset = i - SeslSimpleMonthView.this.findDayOffset();
            if ((SeslSimpleMonthView.this.mIsFirstMonth && findDayOffset < SeslSimpleMonthView.this.mEnabledDayStart) || (SeslSimpleMonthView.this.mIsLastMonth && findDayOffset > SeslSimpleMonthView.this.mEnabledDayEnd)) {
                return true;
            }
            if (findDayOffset <= 0) {
                if (SeslSimpleMonthView.this.mIsLunar) {
                    int i3 = SeslSimpleMonthView.this.mMonth - (!SeslSimpleMonthView.this.mIsLeapMonth ? 1 : 0);
                    if (i3 < 0) {
                        SeslSimpleMonthView seslSimpleMonthView = SeslSimpleMonthView.this;
                        int daysInMonthLunar = seslSimpleMonthView.getDaysInMonthLunar(11, seslSimpleMonthView.mYear - 1, SeslSimpleMonthView.this.mIsLeapMonth);
                        SeslSimpleMonthView seslSimpleMonthView2 = SeslSimpleMonthView.this;
                        seslSimpleMonthView2.onDeactivatedDayClick(seslSimpleMonthView2.mYear - 1, i3, daysInMonthLunar + findDayOffset, true);
                    } else {
                        SeslSimpleMonthView seslSimpleMonthView3 = SeslSimpleMonthView.this;
                        int daysInMonthLunar2 = seslSimpleMonthView3.getDaysInMonthLunar(i3, seslSimpleMonthView3.mYear, SeslSimpleMonthView.this.mIsLeapMonth);
                        SeslSimpleMonthView seslSimpleMonthView4 = SeslSimpleMonthView.this;
                        seslSimpleMonthView4.onDeactivatedDayClick(seslSimpleMonthView4.mYear, i3, daysInMonthLunar2 + findDayOffset, true);
                    }
                } else {
                    Calendar instance = Calendar.getInstance();
                    instance.clear();
                    instance.set(SeslSimpleMonthView.this.mYear, SeslSimpleMonthView.this.mMonth, 1);
                    instance.add(5, findDayOffset - 1);
                    SeslSimpleMonthView.this.onDeactivatedDayClick(instance.get(1), instance.get(2), instance.get(5), true);
                }
            } else if (findDayOffset <= SeslSimpleMonthView.this.mNumCells) {
                SeslSimpleMonthView seslSimpleMonthView5 = SeslSimpleMonthView.this;
                seslSimpleMonthView5.onDayClick(seslSimpleMonthView5.mYear, SeslSimpleMonthView.this.mMonth, findDayOffset);
            } else if (SeslSimpleMonthView.this.mIsLunar) {
                int i4 = SeslSimpleMonthView.this.mMonth + 1;
                if (i4 > 11) {
                    SeslSimpleMonthView seslSimpleMonthView6 = SeslSimpleMonthView.this;
                    seslSimpleMonthView6.onDeactivatedDayClick(seslSimpleMonthView6.mYear + 1, 0, findDayOffset - SeslSimpleMonthView.this.mNumCells, false);
                } else {
                    SeslSimpleMonthView seslSimpleMonthView7 = SeslSimpleMonthView.this;
                    seslSimpleMonthView7.onDeactivatedDayClick(seslSimpleMonthView7.mYear, i4, findDayOffset - SeslSimpleMonthView.this.mNumCells, false);
                }
            } else {
                Calendar instance2 = Calendar.getInstance();
                instance2.clear();
                instance2.set(SeslSimpleMonthView.this.mYear, SeslSimpleMonthView.this.mMonth, SeslSimpleMonthView.this.mNumCells);
                instance2.add(5, findDayOffset - SeslSimpleMonthView.this.mNumCells);
                SeslSimpleMonthView.this.onDeactivatedDayClick(instance2.get(1), instance2.get(2), instance2.get(5), false);
            }
            return true;
        }

        public final void getItemBounds(int i, Rect rect) {
            int i2 = SeslSimpleMonthView.this.mPadding;
            int i3 = SeslSimpleMonthView.this.mWeekHeight;
            int i4 = SeslSimpleMonthView.this.mCalendarWidth / SeslSimpleMonthView.this.mNumDays;
            int findDayOffset = (i - 1) + SeslSimpleMonthView.this.findDayOffset();
            int i5 = findDayOffset / SeslSimpleMonthView.this.mNumDays;
            int i6 = i2 + ((findDayOffset % SeslSimpleMonthView.this.mNumDays) * i4);
            int i7 = ((int) (SeslSimpleMonthView.this.mContext.getResources().getDisplayMetrics().density * -1.0f)) + (i5 * i3);
            rect.set(i6, i7, i4 + i6, i3 + i7);
        }

        public final CharSequence getItemDescription(int i) {
            this.mTempCalendar.set(SeslSimpleMonthView.this.mYear, SeslSimpleMonthView.this.mMonth, i);
            String formatDateTime = DateUtils.formatDateTime(SeslSimpleMonthView.this.mContext, this.mTempCalendar.getTimeInMillis(), 22);
            if (!SeslSimpleMonthView.this.mIsLunar || SeslSimpleMonthView.this.mPathClassLoader == null) {
                return formatDateTime;
            }
            int i2 = SeslSimpleMonthView.this.mYear;
            int i3 = SeslSimpleMonthView.this.mMonth;
            boolean z = SeslSimpleMonthView.this.mIsLeapMonth;
            if (i <= 0) {
                i3 = SeslSimpleMonthView.this.mMonth - (!SeslSimpleMonthView.this.mIsLeapMonth ? 1 : 0);
                z = SeslSimpleMonthView.this.mIsPrevMonthLeap;
                if (i3 < 0) {
                    i2--;
                    i3 = 11;
                }
                i += SeslSimpleMonthView.this.getDaysInMonthLunar(i3, i2, z);
            } else if (i > SeslSimpleMonthView.this.mNumCells) {
                i3 = SeslSimpleMonthView.this.mMonth + (!SeslSimpleMonthView.this.mIsNextMonthLeap ? 1 : 0);
                z = SeslSimpleMonthView.this.mIsNextMonthLeap;
                if (i3 > 11) {
                    i2++;
                    i3 = 0;
                }
                i -= SeslSimpleMonthView.this.mNumCells;
            }
            SeslSolarLunarConverterReflector.convertLunarToSolar(SeslSimpleMonthView.this.mPathClassLoader, SeslSimpleMonthView.this.mSolarLunarConverter, i2, i3, i, z);
            int year = SeslSolarLunarConverterReflector.getYear(SeslSimpleMonthView.this.mPathClassLoader, SeslSimpleMonthView.this.mSolarLunarConverter);
            int month = SeslSolarLunarConverterReflector.getMonth(SeslSimpleMonthView.this.mPathClassLoader, SeslSimpleMonthView.this.mSolarLunarConverter);
            int day = SeslSolarLunarConverterReflector.getDay(SeslSimpleMonthView.this.mPathClassLoader, SeslSimpleMonthView.this.mSolarLunarConverter);
            Calendar instance = Calendar.getInstance();
            instance.set(year, month, day);
            return SeslLunarDateUtilsReflector.buildLunarDateString(SeslSimpleMonthView.this.mPathClassLoader, instance, SeslSimpleMonthView.this.getContext());
        }
    }

    public int getWeekStart() {
        return this.mWeekStart;
    }

    public int getDayOfWeekStart() {
        return this.mDayOfWeekStart - (this.mWeekStart - 1);
    }

    public int getNumDays() {
        return this.mNumDays;
    }

    public void setStartDate(Calendar calendar, int i) {
        this.mStartYear = calendar.get(1);
        this.mStartMonth = calendar.get(2);
        this.mStartDay = calendar.get(5);
        this.mIsLeapStartMonth = i;
    }

    public void setEndDate(Calendar calendar, int i) {
        this.mEndYear = calendar.get(1);
        this.mEndMonth = calendar.get(2);
        this.mEndDay = calendar.get(5);
        this.mIsLeapEndMonth = i;
    }

    public final boolean isRTL() {
        Locale locale = Locale.getDefault();
        if ("ur".equals(locale.getLanguage())) {
            return false;
        }
        byte directionality = Character.getDirectionality(locale.getDisplayName(locale).charAt(0));
        if (directionality == 1 || directionality == 2) {
            return true;
        }
        return false;
    }

    public void setLunar(boolean z, boolean z2, PathClassLoader pathClassLoader) {
        this.mIsLunar = z;
        this.mIsLeapMonth = z2;
        if (z && this.mSolarLunarConverter == null) {
            this.mPathClassLoader = pathClassLoader;
            this.mSolarLunarConverter = SeslFeatureReflector.getSolarLunarConverter(pathClassLoader);
        }
    }

    public void setFirstMonth() {
        this.mIsFirstMonth = true;
    }

    public void setLastMonth() {
        this.mIsLastMonth = true;
    }

    public void setPrevMonthLeap() {
        this.mIsPrevMonthLeap = true;
    }

    public void setNextMonthLeap() {
        this.mIsNextMonthLeap = true;
    }

    public final boolean isHighContrastFontEnabled() {
        return SeslViewReflector.isHighContrastTextEnabled(this);
    }
}
