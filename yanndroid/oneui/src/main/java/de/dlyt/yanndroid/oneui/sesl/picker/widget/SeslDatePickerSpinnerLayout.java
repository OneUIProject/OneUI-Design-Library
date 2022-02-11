package de.dlyt.yanndroid.oneui.sesl.picker.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.FocusFinder;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.reflect.lunarcalendar.SeslSolarLunarTablesReflector;

import java.text.DateFormatSymbols;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;

import dalvik.system.PathClassLoader;
import de.dlyt.yanndroid.oneui.R;

public class SeslDatePickerSpinnerLayout extends LinearLayout {
    public static final String TAG = SeslDatePickerSpinnerLayout.class.getSimpleName();
    public Context mContext;
    public Calendar mCurrentDate;
    public Locale mCurrentLocale;
    public SeslDatePicker mDatePicker;
    public final View mDayPaddingView;
    public final SeslNumberPicker mDaySpinner;
    public final EditText mDaySpinnerInput;
    public TextView.OnEditorActionListener mEditorActionListener;
    public boolean mIsEditTextMode;
    public boolean mIsLeapMonth;
    public boolean mIsLunar;
    public int mLunarCurrentDay;
    public int mLunarCurrentMonth;
    public int mLunarCurrentYear;
    public int mLunarTempDay;
    public int mLunarTempMonth;
    public int mLunarTempYear;
    public Calendar mMaxDate;
    public Calendar mMinDate;
    public SeslNumberPicker.OnEditTextModeChangedListener mModeChangeListener;
    public final SeslNumberPicker mMonthSpinner;
    public final EditText mMonthSpinnerInput;
    public int mNumberOfMonths;
    public SeslDatePicker.OnEditTextModeChangedListener mOnEditTextModeChangedListener;
    public OnSpinnerDateChangedListener mOnSpinnerDateChangedListener;
    public PathClassLoader mPathClassLoader;
    public EditText[] mPickerTexts;
    public String[] mShortMonths;
    public Object mSolarLunarTables;
    public final LinearLayout mSpinners;
    public Calendar mTempDate;
    public Toast mToast;
    public String mToastText;
    public final View mYearPaddingView;
    public final SeslNumberPicker mYearSpinner;
    public final EditText mYearSpinnerInput;

    public interface OnSpinnerDateChangedListener {
        void onDateChanged(SeslDatePickerSpinnerLayout seslDatePickerSpinnerLayout, int i, int i2, int i3);
    }

    public final void seslLog(String str) {
    }

    public static /* synthetic */ int access$412(SeslDatePickerSpinnerLayout seslDatePickerSpinnerLayout, int i) {
        int i2 = seslDatePickerSpinnerLayout.mLunarTempYear + i;
        seslDatePickerSpinnerLayout.mLunarTempYear = i2;
        return i2;
    }

    public static /* synthetic */ int access$612(SeslDatePickerSpinnerLayout seslDatePickerSpinnerLayout, int i) {
        int i2 = seslDatePickerSpinnerLayout.mLunarTempMonth + i;
        seslDatePickerSpinnerLayout.mLunarTempMonth = i2;
        return i2;
    }

    public static /* synthetic */ int access$812(SeslDatePickerSpinnerLayout seslDatePickerSpinnerLayout, int i) {
        int i2 = seslDatePickerSpinnerLayout.mLunarTempDay + i;
        seslDatePickerSpinnerLayout.mLunarTempDay = i2;
        return i2;
    }

    public SeslDatePickerSpinnerLayout(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 16843612);
    }

    public SeslDatePickerSpinnerLayout(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public SeslDatePickerSpinnerLayout(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mIsLunar = false;
        this.mIsLeapMonth = false;
        this.mPathClassLoader = null;
        this.mModeChangeListener = new SeslNumberPicker.OnEditTextModeChangedListener() {
            /* class androidx.picker.widget.SeslDatePickerSpinnerLayout.AnonymousClass1 */

            @Override // androidx.picker.widget.SeslNumberPicker.OnEditTextModeChangedListener
            public void onEditTextModeChanged(SeslNumberPicker seslNumberPicker, boolean z) {
                SeslDatePickerSpinnerLayout.this.setEditTextMode(z);
                SeslDatePickerSpinnerLayout.this.updateModeState(z);
            }
        };
        this.mPickerTexts = new EditText[3];
        this.mEditorActionListener = new TextView.OnEditorActionListener() {
            /* class androidx.picker.widget.SeslDatePickerSpinnerLayout.AnonymousClass3 */

            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == 6) {
                    SeslDatePickerSpinnerLayout.this.updateInputState();
                    SeslDatePickerSpinnerLayout.this.setEditTextMode(false);
                }
                return false;
            }
        };
        this.mContext = context;
        LayoutInflater.from(context).inflate(R.layout.sesl_date_picker_spinner, (ViewGroup) this, true);
        Locale locale = Locale.getDefault();
        this.mCurrentLocale = locale;
        setCurrentLocale(locale);
        SeslNumberPicker.OnValueChangeListener r12 = new SeslNumberPicker.OnValueChangeListener() {
            public void onValueChange(SeslNumberPicker var1, int var2, int var3) {
                SeslDatePickerSpinnerLayout.this.mTempDate.setTimeInMillis(SeslDatePickerSpinnerLayout.this.mCurrentDate.getTimeInMillis());
                if (SeslDatePickerSpinnerLayout.this.mIsLunar) {
                    SeslDatePickerSpinnerLayout var4 = SeslDatePickerSpinnerLayout.this;
                    var4.mLunarTempYear = var4.mLunarCurrentYear;
                    var4 = SeslDatePickerSpinnerLayout.this;
                    var4.mLunarTempMonth = var4.mLunarCurrentMonth;
                    var4 = SeslDatePickerSpinnerLayout.this;
                    var4.mLunarTempDay = var4.mLunarCurrentDay;
                }

                int var5;
                boolean var6;
                boolean var7;
                SeslDatePickerSpinnerLayout var8;
                label96: {
                    Calendar var9;
                    if (var1.equals(SeslDatePickerSpinnerLayout.this.mDaySpinner)) {
                        var5 = SeslDatePickerSpinnerLayout.this.mTempDate.getActualMaximum(5);
                        if (SeslDatePickerSpinnerLayout.this.mIsLunar) {
                            var8 = SeslDatePickerSpinnerLayout.this;
                            var5 = var8.getLunarMaxDayOfMonth(var8.mTempDate.get(1), SeslDatePickerSpinnerLayout.this.mTempDate.get(2), SeslDatePickerSpinnerLayout.this.mIsLeapMonth);
                        }

                        if (var2 == var5 && var3 == 1 || var2 == 1 && var3 == var5) {
                            SeslDatePickerSpinnerLayout.this.mTempDate.set(5, var3);
                            if (SeslDatePickerSpinnerLayout.this.mIsLunar) {
                                SeslDatePickerSpinnerLayout.this.mLunarTempDay = var3;
                            }
                        } else {
                            var9 = SeslDatePickerSpinnerLayout.this.mTempDate;
                            var2 = var3 - var2;
                            var9.add(5, var2);
                            if (SeslDatePickerSpinnerLayout.this.mIsLunar) {
                                SeslDatePickerSpinnerLayout.access$812(SeslDatePickerSpinnerLayout.this, var2);
                            }
                        }

                        var6 = false;
                    } else {
                        if (var1.equals(SeslDatePickerSpinnerLayout.this.mMonthSpinner)) {
                            if (var2 == 11 && var3 == 0 || var2 == 0 && var3 == 11) {
                                SeslDatePickerSpinnerLayout.this.mTempDate.set(2, var3);
                                if (SeslDatePickerSpinnerLayout.this.mIsLunar) {
                                    SeslDatePickerSpinnerLayout.this.mLunarTempMonth = var3;
                                }
                            } else {
                                var9 = SeslDatePickerSpinnerLayout.this.mTempDate;
                                var2 = var3 - var2;
                                var9.add(2, var2);
                                if (SeslDatePickerSpinnerLayout.this.mIsLunar) {
                                    SeslDatePickerSpinnerLayout.access$612(SeslDatePickerSpinnerLayout.this, var2);
                                }
                            }

                            var6 = false;
                            var7 = true;
                            break label96;
                        }

                        if (!var1.equals(SeslDatePickerSpinnerLayout.this.mYearSpinner)) {
                            throw new IllegalArgumentException();
                        }

                        var9 = SeslDatePickerSpinnerLayout.this.mTempDate;
                        var2 = var3 - var2;
                        var9.add(1, var2);
                        if (SeslDatePickerSpinnerLayout.this.mIsLunar) {
                            SeslDatePickerSpinnerLayout.access$412(SeslDatePickerSpinnerLayout.this, var2);
                        }

                        var6 = true;
                    }

                    var7 = var6;
                }

                if (SeslDatePickerSpinnerLayout.this.mIsLunar) {
                    var8 = SeslDatePickerSpinnerLayout.this;
                    var2 = var8.getLunarMaxDayOfMonth(var8.mLunarTempYear, SeslDatePickerSpinnerLayout.this.mLunarTempMonth, SeslDatePickerSpinnerLayout.this.mIsLeapMonth);
                    if (SeslDatePickerSpinnerLayout.this.mLunarTempDay > var2) {
                        SeslDatePickerSpinnerLayout.this.mLunarTempDay = var2;
                    }

                    if (SeslDatePickerSpinnerLayout.this.mIsLeapMonth) {
                        var8 = SeslDatePickerSpinnerLayout.this;
                        var8.mIsLeapMonth = SeslSolarLunarTablesReflector.isLeapMonth(var8.mPathClassLoader, var8.mSolarLunarTables, SeslDatePickerSpinnerLayout.this.mLunarTempYear, SeslDatePickerSpinnerLayout.this.mLunarTempMonth);
                    }
                }

                var2 = SeslDatePickerSpinnerLayout.this.mTempDate.get(1);
                var5 = SeslDatePickerSpinnerLayout.this.mTempDate.get(2);
                var3 = SeslDatePickerSpinnerLayout.this.mTempDate.get(5);
                if (SeslDatePickerSpinnerLayout.this.mIsLunar) {
                    var2 = SeslDatePickerSpinnerLayout.this.mLunarTempYear;
                    var5 = SeslDatePickerSpinnerLayout.this.mLunarTempMonth;
                    var3 = SeslDatePickerSpinnerLayout.this.mLunarTempDay;
                }

                SeslDatePickerSpinnerLayout.this.setDate(var2, var5, var3);
                if (var6 || var7) {
                    SeslDatePickerSpinnerLayout.this.updateSpinners(false, false, var6, var7);
                }

                SeslDatePickerSpinnerLayout.this.notifyDateChanged();
            }
        };
        this.mSpinners = (LinearLayout) findViewById(R.id.sesl_date_picker_pickers);
        this.mDayPaddingView = findViewById(R.id.sesl_date_picker_spinner_day_padding);
        this.mYearPaddingView = findViewById(R.id.sesl_date_picker_spinner_year_padding);
        SeslNumberPicker seslNumberPicker = (SeslNumberPicker) findViewById(R.id.sesl_date_picker_spinner_day);
        this.mDaySpinner = seslNumberPicker;
        int i3 = R.id.numberpicker_input;
        this.mDaySpinnerInput = (EditText) seslNumberPicker.findViewById(i3);
        seslNumberPicker.setFormatter(SeslNumberPicker.getTwoDigitFormatter());
        seslNumberPicker.setOnValueChangedListener(r12);
        seslNumberPicker.setOnEditTextModeChangedListener(this.mModeChangeListener);
        seslNumberPicker.setMaxInputLength(2);
        seslNumberPicker.setYearDateTimeInputMode();
        SeslNumberPicker seslNumberPicker2 = (SeslNumberPicker) findViewById(R.id.sesl_date_picker_spinner_month);
        this.mMonthSpinner = seslNumberPicker2;
        EditText editText = (EditText) seslNumberPicker2.findViewById(i3);
        this.mMonthSpinnerInput = editText;
        if (usingNumericMonths()) {
            seslNumberPicker2.setMinValue(1);
            seslNumberPicker2.setMaxValue(12);
            seslNumberPicker2.setYearDateTimeInputMode();
            seslNumberPicker2.setMaxInputLength(2);
        } else {
            seslNumberPicker2.setMinValue(0);
            seslNumberPicker2.setMaxValue(this.mNumberOfMonths - 1);
            seslNumberPicker2.setFormatter(null);
            seslNumberPicker2.setDisplayedValues(this.mShortMonths);
            editText.setInputType(1);
            seslNumberPicker2.setMonthInputMode();
        }
        seslNumberPicker2.setOnValueChangedListener(r12);
        seslNumberPicker2.setOnEditTextModeChangedListener(this.mModeChangeListener);
        SeslNumberPicker seslNumberPicker3 = (SeslNumberPicker) findViewById(R.id.sesl_date_picker_spinner_year);
        this.mYearSpinner = seslNumberPicker3;
        this.mYearSpinnerInput = (EditText) seslNumberPicker3.findViewById(i3);
        seslNumberPicker3.setOnValueChangedListener(r12);
        seslNumberPicker3.setOnEditTextModeChangedListener(this.mModeChangeListener);
        seslNumberPicker3.setMaxInputLength(4);
        seslNumberPicker3.setYearDateTimeInputMode();
        Typeface create = Typeface.create("sec-roboto-light", 1);
        seslNumberPicker.setTextTypeface(create);
        seslNumberPicker2.setTextTypeface(create);
        seslNumberPicker3.setTextTypeface(create);
        Resources resources = context.getResources();
        int integer = resources.getInteger(R.integer.sesl_date_picker_spinner_number_text_size);
        int integer2 = resources.getInteger(R.integer.sesl_date_picker_spinner_number_text_size_with_unit);
        this.mToastText = resources.getString(R.string.sesl_number_picker_invalid_value_entered);
        float f = (float) integer;
        seslNumberPicker.setTextSize(f);
        seslNumberPicker3.setTextSize(f);
        String language = this.mCurrentLocale.getLanguage();
        if ("my".equals(language) || "ml".equals(language) || "bn".equals(language) || "ar".equals(language) || "fa".equals(language)) {
            integer = resources.getInteger(R.integer.sesl_date_picker_spinner_long_month_text_size);
        } else if ("ga".equals(language)) {
            integer = resources.getInteger(R.integer.sesl_date_picker_spinner_long_month_text_size) - 1;
        } else if ("hu".equals(language)) {
            integer -= 4;
        }
        if (usingNumericMonths()) {
            seslNumberPicker2.setTextSize(f);
        } else {
            seslNumberPicker2.setTextSize((float) integer);
        }
        if ("ko".equals(language) || "zh".equals(language) || "ja".equals(language)) {
            float f2 = (float) integer2;
            seslNumberPicker.setTextSize(f2);
            seslNumberPicker2.setTextSize(f2);
            seslNumberPicker3.setTextSize(f2);
            seslNumberPicker.setDateUnit(997);
            seslNumberPicker2.setDateUnit(998);
            seslNumberPicker3.setDateUnit(999);
        }
        seslNumberPicker.setPickerContentDescription(context.getResources().getString(R.string.sesl_date_picker_day));
        seslNumberPicker2.setPickerContentDescription(context.getResources().getString(R.string.sesl_date_picker_month));
        seslNumberPicker3.setPickerContentDescription(context.getResources().getString(R.string.sesl_date_picker_year));
        this.mCurrentDate.setTimeInMillis(System.currentTimeMillis());
        init(this.mCurrentDate.get(1), this.mCurrentDate.get(2), this.mCurrentDate.get(5));
        reorderSpinners();
    }

    public void init(int i, int i2, int i3) {
        setDate(i, i2, i3);
        updateSpinners(true, true, true, true);
    }

    public void updateDate(int i, int i2, int i3) {
        if (isNewDate(i, i2, i3)) {
            setDate(i, i2, i3);
            updateSpinners(true, true, true, true);
        }
    }

    public int getYear() {
        if (this.mIsLunar) {
            return this.mLunarCurrentYear;
        }
        return this.mCurrentDate.get(1);
    }

    public int getMonth() {
        if (this.mIsLunar) {
            return this.mLunarCurrentMonth;
        }
        return this.mCurrentDate.get(2);
    }

    public int getDayOfMonth() {
        if (this.mIsLunar) {
            return this.mLunarCurrentDay;
        }
        return this.mCurrentDate.get(5);
    }

    public void setMinDate(long j) {
        this.mMinDate.setTimeInMillis(j);
        if (this.mCurrentDate.before(this.mMinDate)) {
            this.mCurrentDate.setTimeInMillis(this.mMinDate.getTimeInMillis());
        }
        updateSpinners(true, true, true, true);
    }

    public void setMaxDate(long j) {
        this.mMaxDate.setTimeInMillis(j);
        if (this.mCurrentDate.after(this.mMaxDate)) {
            this.mCurrentDate.setTimeInMillis(this.mMaxDate.getTimeInMillis());
        }
        updateSpinners(true, true, true, true);
    }

    public void setEnabled(boolean z) {
        this.mDaySpinner.setEnabled(z);
        this.mMonthSpinner.setEnabled(z);
        this.mYearSpinner.setEnabled(z);
    }

    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        setCurrentLocale(configuration.locale);
    }

    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        onPopulateAccessibilityEvent(accessibilityEvent);
        return true;
    }

    public void onPopulateAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        super.onPopulateAccessibilityEvent(accessibilityEvent);
        accessibilityEvent.getText().add(DateUtils.formatDateTime(this.mContext, this.mCurrentDate.getTimeInMillis(), 20));
    }

    public void setCurrentLocale(Locale locale) {
        this.mTempDate = getCalendarForLocale(this.mTempDate, locale);
        this.mMinDate = getCalendarForLocale(this.mMinDate, locale);
        this.mMaxDate = getCalendarForLocale(this.mMaxDate, locale);
        this.mCurrentDate = getCalendarForLocale(this.mCurrentDate, locale);
        this.mNumberOfMonths = this.mTempDate.getActualMaximum(2) + 1;
        this.mShortMonths = new DateFormatSymbols().getShortMonths();
        int i = 0;
        while (true) {
            String[] strArr = this.mShortMonths;
            if (i >= strArr.length) {
                break;
            }
            strArr[i] = strArr[i].toUpperCase();
            i++;
        }
        if (usingNumericMonths()) {
            this.mShortMonths = new String[this.mNumberOfMonths];
            int i2 = 0;
            while (i2 < this.mNumberOfMonths) {
                int i3 = i2 + 1;
                this.mShortMonths[i2] = String.format("%d", Integer.valueOf(i3));
                i2 = i3;
            }
        }
    }

    public final boolean usingNumericMonths() {
        return Character.isDigit(this.mShortMonths[0].charAt(0));
    }

    public final Calendar getCalendarForLocale(Calendar calendar, Locale locale) {
        if (calendar == null) {
            return Calendar.getInstance(locale);
        }
        long timeInMillis = calendar.getTimeInMillis();
        Calendar instance = Calendar.getInstance(locale);
        instance.setTimeInMillis(timeInMillis);
        return instance;
    }

    public final void reorderSpinners() {
        this.mSpinners.removeAllViews();
        char[] dateFormatOrder = DateFormat.getDateFormatOrder(this.mContext);
        int length = dateFormatOrder.length;
        for (int i = 0; i < length; i++) {
            char c = dateFormatOrder[i];
            if (c == 'M') {
                this.mSpinners.addView(this.mMonthSpinner);
                setImeOptions(this.mMonthSpinner, length, i);
            } else if (c == 'd') {
                this.mSpinners.addView(this.mDaySpinner);
                setImeOptions(this.mDaySpinner, length, i);
            } else if (c == 'y') {
                this.mSpinners.addView(this.mYearSpinner);
                setImeOptions(this.mYearSpinner, length, i);
            } else {
                throw new IllegalArgumentException(Arrays.toString(dateFormatOrder));
            }
        }
        if (dateFormatOrder[0] == 'y') {
            this.mSpinners.addView(this.mYearPaddingView, 0);
            this.mSpinners.addView(this.mDayPaddingView);
        } else {
            this.mSpinners.addView(this.mDayPaddingView, 0);
            this.mSpinners.addView(this.mYearPaddingView);
        }
        char c2 = dateFormatOrder[0];
        char c3 = dateFormatOrder[1];
        if (c2 == 'M') {
            setTextWatcher(0);
        } else if (c2 == 'd') {
            setTextWatcher(1);
        } else if (c2 == 'y') {
            if (c3 == 'd') {
                setTextWatcher(3);
            } else {
                setTextWatcher(2);
            }
        }
    }

    public final boolean isNewDate(int i, int i2, int i3) {
        if (this.mCurrentDate.get(1) == i && this.mCurrentDate.get(2) == i2 && this.mCurrentDate.get(5) == i3) {
            return false;
        }
        return true;
    }

    public final void setDate(int i, int i2, int i3) {
        this.mCurrentDate.set(i, i2, i3);
        if (this.mIsLunar) {
            this.mLunarCurrentYear = i;
            this.mLunarCurrentMonth = i2;
            this.mLunarCurrentDay = i3;
        }
        if (this.mCurrentDate.before(this.mMinDate)) {
            this.mCurrentDate.setTimeInMillis(this.mMinDate.getTimeInMillis());
        } else if (this.mCurrentDate.after(this.mMaxDate)) {
            this.mCurrentDate.setTimeInMillis(this.mMaxDate.getTimeInMillis());
        }
    }

    public final void updateSpinners(boolean z, boolean z2, boolean z3, boolean z4) {
        EditText[] editTextArr;
        int i;
        int i2;
        int i3;
        int i4;
        int i5;
        if (z2) {
            this.mYearSpinner.setMinValue(this.mMinDate.get(1));
            this.mYearSpinner.setMaxValue(this.mMaxDate.get(1));
            this.mYearSpinner.setWrapSelectorWheel(false);
        }
        if (z3) {
            int i6 = 11;
            if (this.mMaxDate.get(1) - this.mMinDate.get(1) == 0) {
                i4 = this.mMinDate.get(2);
                i5 = this.mMaxDate.get(2);
            } else {
                int i7 = this.mCurrentDate.get(1);
                if (this.mIsLunar) {
                    i7 = this.mLunarCurrentYear;
                }
                if (i7 == this.mMinDate.get(1)) {
                    i5 = 11;
                    i4 = this.mMinDate.get(2);
                } else {
                    if (i7 == this.mMaxDate.get(1)) {
                        i6 = this.mMaxDate.get(2);
                    }
                    i5 = i6;
                    i4 = 0;
                }
            }
            if (usingNumericMonths()) {
                i4++;
                i5++;
            }
            this.mMonthSpinner.setDisplayedValues(null);
            this.mMonthSpinner.setMinValue(i4);
            this.mMonthSpinner.setMaxValue(i5);
            if (!usingNumericMonths()) {
                this.mMonthSpinner.setDisplayedValues((String[]) Arrays.copyOfRange(this.mShortMonths, this.mMonthSpinner.getMinValue(), this.mMonthSpinner.getMaxValue() + 1));
            }
        }
        if (z4) {
            int i8 = this.mMaxDate.get(1) - this.mMinDate.get(1);
            int i9 = this.mMaxDate.get(2) - this.mMinDate.get(2);
            if (i8 == 0 && i9 == 0) {
                i = this.mMinDate.get(5);
                i2 = this.mMaxDate.get(5);
            } else {
                int i10 = this.mCurrentDate.get(1);
                int i11 = this.mCurrentDate.get(2);
                if (this.mIsLunar) {
                    i10 = this.mLunarCurrentYear;
                    i11 = this.mLunarCurrentMonth;
                }
                if (i10 == this.mMinDate.get(1) && i11 == this.mMinDate.get(2)) {
                    int i12 = this.mMinDate.get(5);
                    int actualMaximum = this.mCurrentDate.getActualMaximum(5);
                    if (this.mIsLunar) {
                        i2 = getLunarMaxDayOfMonth(i10, i11, this.mIsLeapMonth);
                        i = i12;
                    } else {
                        i = i12;
                        i2 = actualMaximum;
                    }
                } else {
                    if (i10 == this.mMaxDate.get(1) && i11 == this.mMaxDate.get(2)) {
                        i3 = this.mMaxDate.get(5);
                        if (this.mIsLunar) {
                            i2 = Math.min(i3, getLunarMaxDayOfMonth(i10, i11, this.mIsLeapMonth));
                        }
                        i = 1;
                        i2 = i3;
                    } else {
                        i3 = this.mCurrentDate.getActualMaximum(5);
                        if (this.mIsLunar) {
                            i2 = getLunarMaxDayOfMonth(i10, i11, this.mIsLeapMonth);
                        }
                        i = 1;
                        i2 = i3;
                    }
                    i = 1;
                }
            }
            this.mDaySpinner.setMinValue(i);
            this.mDaySpinner.setMaxValue(i2);
        }
        if (z) {
            this.mYearSpinner.setValue(this.mCurrentDate.get(1));
            int i13 = this.mCurrentDate.get(2);
            if (this.mIsLunar) {
                i13 = this.mLunarCurrentMonth;
            }
            if (usingNumericMonths()) {
                this.mMonthSpinner.setValue(i13 + 1);
            } else {
                this.mMonthSpinner.setValue(i13);
            }
            int i14 = this.mCurrentDate.get(5);
            if (this.mIsLunar) {
                i14 = this.mLunarCurrentDay;
            }
            this.mDaySpinner.setValue(i14);
            if (usingNumericMonths()) {
                this.mMonthSpinnerInput.setRawInputType(2);
            }
            if (this.mIsEditTextMode && (editTextArr = this.mPickerTexts) != null) {
                for (EditText editText : editTextArr) {
                    if (editText.hasFocus()) {
                        editText.setSelection(0, 0);
                        editText.selectAll();
                        return;
                    }
                }
            }
        }
    }

    public final void notifyDateChanged() {
        OnSpinnerDateChangedListener onSpinnerDateChangedListener = this.mOnSpinnerDateChangedListener;
        if (onSpinnerDateChangedListener != null) {
            onSpinnerDateChangedListener.onDateChanged(this, getYear(), getMonth(), getDayOfMonth());
        }
    }

    public final void setImeOptions(SeslNumberPicker seslNumberPicker, int i, int i2) {
        ((TextView) seslNumberPicker.findViewById(R.id.numberpicker_input)).setImeOptions(i2 < i + -1 ? 33554437 : 33554438);
    }

    public void updateInputState() {
        InputMethodManager inputMethodManager = (InputMethodManager) this.mContext.getSystemService("input_method");
        if (inputMethodManager == null) {
            return;
        }
        if (inputMethodManager.isActive(this.mYearSpinnerInput)) {
            inputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
            this.mYearSpinnerInput.clearFocus();
        } else if (inputMethodManager.isActive(this.mMonthSpinnerInput)) {
            inputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
            this.mMonthSpinnerInput.clearFocus();
        } else if (inputMethodManager.isActive(this.mDaySpinnerInput)) {
            inputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
            this.mDaySpinnerInput.clearFocus();
        }
    }

    public final void setTextWatcher(int var1) {
        byte var3;
        byte var4;
        byte var5;
        label39: {
            label38: {
                StringBuilder var2 = new StringBuilder();
                var2.append("setTextWatcher() usingNumericMonths  : ");
                var2.append(this.usingNumericMonths());
                var2.append("format  : ");
                var2.append(var1);
                this.seslLog(var2.toString());
                var3 = -1;
                var4 = 0;
                var5 = 2;
                if (var1 != 0) {
                    if (var1 == 1) {
                        var3 = 2;
                        var5 = 0;
                        break label38;
                    }

                    if (var1 == 2) {
                        var3 = 0;
                        break label38;
                    }

                    if (var1 != 3) {
                        var4 = -1;
                        var5 = var4;
                        break label39;
                    }

                    var3 = 0;
                    var4 = 2;
                } else {
                    var3 = 2;
                }

                var5 = 1;
                break label39;
            }

            var4 = 1;
        }

        this.mPickerTexts[var3] = this.mYearSpinner.getEditText();
        this.mPickerTexts[var4] = this.mMonthSpinner.getEditText();
        this.mPickerTexts[var5] = this.mDaySpinner.getEditText();
        this.mPickerTexts[var3].addTextChangedListener(new SeslDatePickerSpinnerLayout.SeslTextWatcher(4, var3, false));
        if (this.usingNumericMonths()) {
            this.mPickerTexts[var4].addTextChangedListener(new SeslDatePickerSpinnerLayout.SeslTextWatcher(2, var4, true));
        } else {
            this.mPickerTexts[var4].addTextChangedListener(new SeslDatePickerSpinnerLayout.SeslTextWatcher(3, var4, true));
        }

        this.mPickerTexts[var5].addTextChangedListener(new SeslDatePickerSpinnerLayout.SeslTextWatcher(2, var5, false));
        if (var1 != 3 || this.usingNumericMonths()) {
            EditText[] var6 = this.mPickerTexts;
            var6[var6.length - 1].setOnEditorActionListener(this.mEditorActionListener);
        }

        this.mPickerTexts[var3].setOnKeyListener(new SeslDatePickerSpinnerLayout.SeslKeyListener());
        this.mPickerTexts[var4].setOnKeyListener(new SeslDatePickerSpinnerLayout.SeslKeyListener());
        this.mPickerTexts[var5].setOnKeyListener(new SeslDatePickerSpinnerLayout.SeslKeyListener());
    }

    public class SeslKeyListener implements OnKeyListener {
        public SeslKeyListener() {
        }

        public boolean onKey(View view, int i, KeyEvent keyEvent) {
            SeslDatePickerSpinnerLayout.this.seslLog(keyEvent.toString());
            if (keyEvent.getAction() != 1) {
                return false;
            }
            if (i != 23) {
                if (i != 61) {
                    if (i != 66 && i != 160) {
                        return false;
                    }
                    if (SeslDatePickerSpinnerLayout.this.isEditTextMode()) {
                        EditText editText = (EditText) view;
                        if ((editText.getImeOptions() & 5) == 5) {
                            View findNextFocus = FocusFinder.getInstance().findNextFocus(SeslDatePickerSpinnerLayout.this.mDatePicker, view, 2);
                            if (findNextFocus == null) {
                                return true;
                            }
                            findNextFocus.requestFocus();
                        } else if ((editText.getImeOptions() & 6) == 6) {
                            SeslDatePickerSpinnerLayout.this.updateInputState();
                            SeslDatePickerSpinnerLayout.this.setEditTextMode(false);
                        }
                    }
                }
                return true;
            }
            if (SeslDatePickerSpinnerLayout.this.getResources().getConfiguration().keyboard == 3) {
            }
            return false;
        }
    }

    public class SeslTextWatcher implements TextWatcher {
        public final int INVALID_POSITION_ID;
        public final int LAST_POSITION_ID;
        public int mChangedLen;
        public int mCheck;
        public int mId;
        public boolean mIsMonth;
        public int mMaxLen;
        public int mNext;
        public String mPrevText;

        public SeslTextWatcher(int i, int i2, boolean z) {
            int i3 = -1;
            this.INVALID_POSITION_ID = -1;
            this.LAST_POSITION_ID = 2;
            this.mChangedLen = 0;
            this.mMaxLen = i;
            this.mId = i2;
            this.mIsMonth = z;
            int i4 = i2 - 1;
            this.mCheck = i4;
            if (i4 < 0) {
                this.mCheck = 2;
            }
            this.mNext = i2 + 1 <= 2 ? i2 + 1 : i3;
        }

        public void afterTextChanged(Editable editable) {
            SeslDatePickerSpinnerLayout seslDatePickerSpinnerLayout = SeslDatePickerSpinnerLayout.this;
            seslDatePickerSpinnerLayout.seslLog("[" + this.mId + "] afterTextChanged: " + editable.toString());
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            SeslDatePickerSpinnerLayout seslDatePickerSpinnerLayout = SeslDatePickerSpinnerLayout.this;
            seslDatePickerSpinnerLayout.seslLog("[" + this.mId + "] beforeTextChanged: " + ((Object) charSequence) + ", " + i + ", " + i2 + ", " + i3);
            this.mPrevText = charSequence.toString();
            this.mChangedLen = i3;
        }

        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            int i4;
            int i5;
            SeslDatePickerSpinnerLayout.this.seslLog("[" + this.mId + "] onTextChanged: " + this.mPrevText + " -> " + ((Object) charSequence));
            int length = charSequence.length();
            String charSequence2 = charSequence.toString();
            String str = (String) SeslDatePickerSpinnerLayout.this.mPickerTexts[this.mId].getTag();
            if (str != null && ("onClick".equals(str) || "onLongClick".equals(str))) {
                SeslDatePickerSpinnerLayout.this.seslLog("[" + this.mId + "] TAG exists: " + str);
            } else if (SeslDatePickerSpinnerLayout.this.mPickerTexts[this.mId].isFocused()) {
                String str2 = "";
                if (this.mIsMonth) {
                    if (SeslDatePickerSpinnerLayout.this.usingNumericMonths() && this.mChangedLen == 1) {
                        SeslDatePickerSpinnerLayout.this.seslLog("[" + this.mId + "] Samsung Keypad Num Month");
                        int minValue = SeslDatePickerSpinnerLayout.this.mMonthSpinner.getMinValue();
                        int parseInt = Integer.parseInt(charSequence2);
                        if (length == this.mMaxLen) {
                            if (parseInt >= minValue) {
                                changeFocus();
                            } else if (Character.getNumericValue(charSequence2.charAt(0)) < 2) {
                                showInvalidValueEnteredToast(Character.toString(charSequence2.charAt(0)), 1);
                            } else {
                                showInvalidValueEnteredToast(str2, 0);
                            }
                        } else if (length <= 0) {
                        } else {
                            if (minValue >= 10 && "0".equals(charSequence2)) {
                                showInvalidValueEnteredToast(str2, 0);
                            } else if (!"1".equals(charSequence2) && !"0".equals(charSequence2)) {
                                if (parseInt < minValue) {
                                    showInvalidValueEnteredToast(str2, 0);
                                } else {
                                    changeFocus();
                                }
                            }
                        }
                    } else if (isNumericStr(this.mPrevText)) {
                    } else {
                        if (length >= this.mMaxLen) {
                            if (!isMeaLanguage()) {
                                changeFocus();
                            } else if (TextUtils.isEmpty(this.mPrevText) && isMonthStr(charSequence2)) {
                                changeFocus();
                            }
                        } else if ((isSwaLanguage() || isFarsiLanguage()) && length > 0 && !isNumericStr(charSequence2)) {
                            changeFocus();
                        }
                    }
                } else if (this.mChangedLen != 1) {
                } else {
                    if (this.mMaxLen < 3) {
                        int minValue2 = SeslDatePickerSpinnerLayout.this.mDaySpinner.getMinValue();
                        int parseInt2 = Integer.parseInt(charSequence2);
                        if (this.mPrevText.length() >= length || length != this.mMaxLen) {
                            if ((minValue2 < 10 || parseInt2 != 0) && ((minValue2 < 20 || !(parseInt2 == 0 || parseInt2 == 1)) && (minValue2 < 30 || !(parseInt2 == 0 || parseInt2 == 1 || parseInt2 == 2)))) {
                                if (parseInt2 > 3) {
                                    if (parseInt2 < minValue2) {
                                        showInvalidValueEnteredToast(str2, 0);
                                        return;
                                    }
                                    changeFocus();
                                }
                                if (SeslDatePickerSpinnerLayout.this.usingNumericMonths()) {
                                    i5 = SeslDatePickerSpinnerLayout.this.mMonthSpinner.getValue() - 1;
                                } else {
                                    i5 = SeslDatePickerSpinnerLayout.this.mMonthSpinner.getValue();
                                }
                                if (SeslDatePickerSpinnerLayout.this.mIsLunar || i5 != 1 || parseInt2 != 3) {
                                    return;
                                }
                                if (parseInt2 < minValue2) {
                                    showInvalidValueEnteredToast(str2, 0);
                                } else {
                                    changeFocus();
                                }
                            } else {
                                showInvalidValueEnteredToast(str2, 0);
                            }
                        } else if (parseInt2 >= minValue2) {
                            changeFocus();
                        } else if (Character.getNumericValue(charSequence2.charAt(0)) < 4) {
                            showInvalidValueEnteredToast(Character.toString(charSequence2.charAt(0)), 1);
                        } else {
                            showInvalidValueEnteredToast(str2, 0);
                        }
                    } else {
                        int minValue3 = SeslDatePickerSpinnerLayout.this.mYearSpinner.getMinValue();
                        int maxValue = SeslDatePickerSpinnerLayout.this.mYearSpinner.getMaxValue();
                        int parseInt3 = Integer.parseInt(charSequence2);
                        if (this.mPrevText.length() >= length || length != this.mMaxLen) {
                            int i6 = length - 1;
                            int pow = (int) (1000.0d / Math.pow(10.0d, (double) i6));
                            if (length != 1) {
                                str2 = charSequence2.substring(0, i6);
                            }
                            if (parseInt3 < minValue3 / pow || parseInt3 > maxValue / pow) {
                                showInvalidValueEnteredToast(str2, i6);
                            }
                        } else if (parseInt3 < minValue3 || parseInt3 > maxValue) {
                            showInvalidValueEnteredToast(charSequence2.substring(0, 3), 3);
                        } else {
                            if (SeslDatePickerSpinnerLayout.this.usingNumericMonths()) {
                                i4 = SeslDatePickerSpinnerLayout.this.mMonthSpinner.getValue() - 1;
                            } else {
                                i4 = SeslDatePickerSpinnerLayout.this.mMonthSpinner.getValue();
                            }
                            SeslDatePickerSpinnerLayout.this.mTempDate.clear();
                            SeslDatePickerSpinnerLayout.this.mTempDate.set(parseInt3, i4, SeslDatePickerSpinnerLayout.this.mDaySpinner.getValue());
                            Calendar instance = Calendar.getInstance();
                            instance.clear();
                            instance.set(SeslDatePickerSpinnerLayout.this.mMinDate.get(1), SeslDatePickerSpinnerLayout.this.mMinDate.get(2), SeslDatePickerSpinnerLayout.this.mMinDate.get(5));
                            if (SeslDatePickerSpinnerLayout.this.mTempDate.before(instance) || SeslDatePickerSpinnerLayout.this.mTempDate.after(SeslDatePickerSpinnerLayout.this.mMaxDate)) {
                                showInvalidValueEnteredToast(charSequence2.substring(0, 3), 3);
                            } else {
                                changeFocus();
                            }
                        }
                    }
                }
            }
        }

        public final void showInvalidValueEnteredToast(String str, int i) {
            SeslDatePickerSpinnerLayout.this.mPickerTexts[this.mId].setText(str);
            if (i != 0) {
                SeslDatePickerSpinnerLayout.this.mPickerTexts[this.mId].setSelection(i);
            }
            if (SeslDatePickerSpinnerLayout.this.mToast == null) {
                SeslDatePickerSpinnerLayout seslDatePickerSpinnerLayout = SeslDatePickerSpinnerLayout.this;
                seslDatePickerSpinnerLayout.mToast = Toast.makeText(seslDatePickerSpinnerLayout.mContext, SeslDatePickerSpinnerLayout.this.mToastText, 0);
            }
            SeslDatePickerSpinnerLayout.this.mToast.show();
        }

        public final boolean isSwaLanguage() {
            String language = SeslDatePickerSpinnerLayout.this.mCurrentLocale.getLanguage();
            return "hi".equals(language) || "ta".equals(language) || "ml".equals(language) || "te".equals(language) || "or".equals(language) || "ne".equals(language) || "as".equals(language) || "bn".equals(language) || "gu".equals(language) || "si".equals(language) || "pa".equals(language) || "kn".equals(language) || "mr".equals(language);
        }

        public final boolean isMeaLanguage() {
            String language = SeslDatePickerSpinnerLayout.this.mCurrentLocale.getLanguage();
            return "ar".equals(language) || "fa".equals(language) || "ur".equals(language);
        }

        public final boolean isFarsiLanguage() {
            return "fa".equals(SeslDatePickerSpinnerLayout.this.mCurrentLocale.getLanguage());
        }

        public final boolean isNumericStr(String str) {
            return !TextUtils.isEmpty(str) && Character.isDigit(str.charAt(0));
        }

        public final boolean isMonthStr(String str) {
            for (int i = 0; i < SeslDatePickerSpinnerLayout.this.mNumberOfMonths; i++) {
                if (str.equals(SeslDatePickerSpinnerLayout.this.mShortMonths[i])) {
                    return true;
                }
            }
            return false;
        }

        public final void changeFocus() {
            AccessibilityManager accessibilityManager = (AccessibilityManager) SeslDatePickerSpinnerLayout.this.mContext.getSystemService("accessibility");
            if (accessibilityManager == null || !accessibilityManager.isTouchExplorationEnabled()) {
                SeslDatePickerSpinnerLayout seslDatePickerSpinnerLayout = SeslDatePickerSpinnerLayout.this;
                seslDatePickerSpinnerLayout.seslLog("[" + this.mId + "] changeFocus() mNext : " + this.mNext + ", mCheck : " + this.mCheck);
                if (this.mNext >= 0) {
                    if (!SeslDatePickerSpinnerLayout.this.mPickerTexts[this.mCheck].isFocused()) {
                        SeslDatePickerSpinnerLayout.this.mPickerTexts[this.mNext].requestFocus();
                    }
                    if (SeslDatePickerSpinnerLayout.this.mPickerTexts[this.mId].isFocused()) {
                        SeslDatePickerSpinnerLayout.this.mPickerTexts[this.mId].clearFocus();
                    }
                }
            }
        }
    }

    public void setOnSpinnerDateChangedListener(SeslDatePicker seslDatePicker, OnSpinnerDateChangedListener onSpinnerDateChangedListener) {
        if (this.mDatePicker == null) {
            this.mDatePicker = seslDatePicker;
        }
        this.mOnSpinnerDateChangedListener = onSpinnerDateChangedListener;
    }

    public final void updateModeState(boolean z) {
        if (this.mIsEditTextMode != z && !z) {
            if (this.mDaySpinner.isEditTextMode()) {
                this.mDaySpinner.setEditTextMode(false);
            }
            if (this.mMonthSpinner.isEditTextMode()) {
                this.mMonthSpinner.setEditTextMode(false);
            }
            if (this.mYearSpinner.isEditTextMode()) {
                this.mYearSpinner.setEditTextMode(false);
            }
        }
    }

    public void setEditTextMode(boolean z) {
        if (this.mIsEditTextMode != z) {
            this.mIsEditTextMode = z;
            InputMethodManager inputMethodManager = (InputMethodManager) this.mContext.getSystemService("input_method");
            this.mDaySpinner.setEditTextMode(z);
            this.mMonthSpinner.setEditTextMode(z);
            this.mYearSpinner.setEditTextMode(z);
            if (inputMethodManager != null) {
                if (!this.mIsEditTextMode) {
                    inputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
                } else {
                    inputMethodManager.showSoftInput(this.mDaySpinner, 0);
                }
            }
            SeslDatePicker.OnEditTextModeChangedListener onEditTextModeChangedListener = this.mOnEditTextModeChangedListener;
            if (onEditTextModeChangedListener != null) {
                onEditTextModeChangedListener.onEditTextModeChanged(this.mDatePicker, z);
            }
        }
    }

    public boolean isEditTextMode() {
        return this.mIsEditTextMode;
    }

    public void setOnEditTextModeChangedListener(SeslDatePicker seslDatePicker, SeslDatePicker.OnEditTextModeChangedListener onEditTextModeChangedListener) {
        if (this.mDatePicker == null) {
            this.mDatePicker = seslDatePicker;
        }
        this.mOnEditTextModeChangedListener = onEditTextModeChangedListener;
    }

    public final int getLunarMaxDayOfMonth(int i, int i2, boolean z) {
        Object obj = this.mSolarLunarTables;
        if (obj == null) {
            return 0;
        }
        return SeslSolarLunarTablesReflector.getDayLengthOf(this.mPathClassLoader, obj, i, i2, z);
    }

    public void requestLayout() {
        super.requestLayout();
        SeslNumberPicker seslNumberPicker = this.mDaySpinner;
        if (seslNumberPicker != null) {
            seslNumberPicker.requestLayout();
        }
        SeslNumberPicker seslNumberPicker2 = this.mYearSpinner;
        if (seslNumberPicker2 != null) {
            seslNumberPicker2.requestLayout();
        }
        SeslNumberPicker seslNumberPicker3 = this.mMonthSpinner;
        if (seslNumberPicker3 != null) {
            seslNumberPicker3.requestLayout();
        }
    }
}
