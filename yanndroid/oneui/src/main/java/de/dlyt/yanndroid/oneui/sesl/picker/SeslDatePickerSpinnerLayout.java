package de.dlyt.yanndroid.oneui.sesl.picker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.FocusFinder;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.reflect.lunarcalendar.SeslFeatureReflector;
import androidx.reflect.lunarcalendar.SeslSolarLunarTablesReflector;

import java.text.DateFormatSymbols;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;

import dalvik.system.PathClassLoader;
import de.dlyt.yanndroid.oneui.R;
import de.dlyt.yanndroid.oneui.widget.DatePicker;
import de.dlyt.yanndroid.oneui.widget.NumberPicker;

public class SeslDatePickerSpinnerLayout extends LinearLayout {
    private static final int FORMAT_DDMMYYYY = 1;
    private static final int FORMAT_MMDDYYYY = 0;
    private static final int FORMAT_YYYYDDMM = 3;
    private static final int FORMAT_YYYYMMDD = 2;
    private static final int HUNGARIAN_MONTH_TEXT_SIZE_DIFF = 4;
    private static final int PICKER_DAY = 0;
    private static final int PICKER_MONTH = 1;
    private static final int PICKER_YEAR = 2;
    private static final boolean SESL_DEBUG = false;
    private static final String TAG = "SeslDatePickerSpinnerLayout";
    private Context mContext;
    private Calendar mCurrentDate;
    private Locale mCurrentLocale;
    private DatePicker mDatePicker;
    private final View mDayPaddingView;
    private final NumberPicker mDaySpinner;
    private final EditText mDaySpinnerInput;
    private boolean mIsEditTextMode;
    private boolean mIsLeapMonth = false;
    private boolean mIsLunar = false;
    private int mLunarCurrentDay;
    private int mLunarCurrentMonth;
    private int mLunarCurrentYear;
    private int mLunarTempDay;
    private int mLunarTempMonth;
    private int mLunarTempYear;
    private Calendar mMaxDate;
    private Calendar mMinDate;
    private final NumberPicker mMonthSpinner;
    private final EditText mMonthSpinnerInput;
    private int mNumberOfMonths;
    private DatePicker.OnEditTextModeChangedListener mOnEditTextModeChangedListener;
    private OnSpinnerDateChangedListener mOnSpinnerDateChangedListener;
    PathClassLoader mPathClassLoader = null;
    private EditText[] mPickerTexts = new EditText[3];
    private String[] mShortMonths;
    private Object mSolarLunarTables;
    private final LinearLayout mSpinners;
    private Calendar mTempDate;
    private Toast mToast;
    private String mToastText;
    private final View mYearPaddingView;
    private final NumberPicker mYearSpinner;
    private final EditText mYearSpinnerInput;

    private TextView.OnEditorActionListener mEditorActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                updateInputState();
                setEditTextMode(false);
            }
            return false;
        }
    };

    private NumberPicker.OnEditTextModeChangedListener mModeChangeListener = new NumberPicker.OnEditTextModeChangedListener() {
        @Override
        public void onEditTextModeChanged(NumberPicker numberPicker, boolean edit) {
            setEditTextMode(edit);
            updateModeState(edit);
        }
    };

    public interface OnSpinnerDateChangedListener {
        void onDateChanged(SeslDatePickerSpinnerLayout view, int year, int monthOfYear, int dayOfMonth);
    }

    public SeslDatePickerSpinnerLayout(Context context) {
        this(context, null);
    }

    public SeslDatePickerSpinnerLayout(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.datePickerStyle);
    }

    public SeslDatePickerSpinnerLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public SeslDatePickerSpinnerLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mContext = context;
        LayoutInflater.from(context).inflate(R.layout.sesl_date_picker_spinner, this, true);

        mCurrentLocale = Locale.getDefault();
        setCurrentLocale(mCurrentLocale);

        // kang
        NumberPicker.OnValueChangeListener onValueChangeListener = new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker var1, int var2, int var3) {
                mTempDate.setTimeInMillis(mCurrentDate.getTimeInMillis());
                if (mIsLunar) {
                    mLunarTempYear = mLunarCurrentYear;
                    mLunarTempMonth = mLunarCurrentMonth;
                    mLunarTempDay = mLunarCurrentDay;
                }

                int var5;
                boolean var6;
                boolean var7;

                label96: {
                    Calendar var9;
                    if (var1.equals(mDaySpinner)) {
                        var5 = mTempDate.getActualMaximum(5);
                        if (mIsLunar) {
                            var5 = getLunarMaxDayOfMonth(mTempDate.get(1), mTempDate.get(2), mIsLeapMonth);
                        }

                        if (var2 == var5 && var3 == 1 || var2 == 1 && var3 == var5) {
                            mTempDate.set(5, var3);
                            if (mIsLunar) {
                                mLunarTempDay = var3;
                            }
                        } else {
                            var9 = mTempDate;
                            var2 = var3 - var2;
                            var9.add(5, var2);
                            if (mIsLunar) {
                                mLunarTempDay += var2;
                            }
                        }

                        var6 = false;
                    } else {
                        if (var1.equals(mMonthSpinner)) {
                            if (var2 == 11 && var3 == 0 || var2 == 0 && var3 == 11) {
                                mTempDate.set(2, var3);
                                if (mIsLunar) {
                                    mLunarTempMonth = var3;
                                }
                            } else {
                                var9 = mTempDate;
                                var2 = var3 - var2;
                                var9.add(2, var2);
                                if (mIsLunar) {
                                    mLunarTempMonth += var2;
                                }
                            }

                            var6 = false;
                            var7 = true;
                            break label96;
                        }

                        if (!var1.equals(mYearSpinner)) {
                            throw new IllegalArgumentException();
                        }

                        var9 = mTempDate;
                        var2 = var3 - var2;
                        var9.add(1, var2);
                        if (mIsLunar) {
                            mLunarTempYear += var2;
                        }

                        var6 = true;
                    }

                    var7 = var6;
                }

                if (mIsLunar) {
                    var2 = getLunarMaxDayOfMonth(mLunarTempYear, mLunarTempMonth, mIsLeapMonth);
                    if (mLunarTempDay > var2) {
                        mLunarTempDay = var2;
                    }

                    if (mIsLeapMonth) {
                        mIsLeapMonth = SeslSolarLunarTablesReflector.isLeapMonth(mPathClassLoader, mSolarLunarTables, mLunarTempYear, mLunarTempMonth);
                    }
                }

                var2 = mTempDate.get(1);
                var3 = mTempDate.get(2);
                var5 = mTempDate.get(5);
                if (mIsLunar) {
                    var2 = mLunarTempYear;
                    var3 = mLunarTempMonth;
                    var5 = mLunarTempDay;
                }

                setDate(var2, var3, var5);
                if (var6 || var7) {
                    updateSpinners(false, false, var6, var7);
                }

                notifyDateChanged();
            }
        };
        // kang

        mSpinners = findViewById(R.id.sesl_date_picker_pickers);
        mDayPaddingView = findViewById(R.id.sesl_date_picker_spinner_day_padding);
        mYearPaddingView = findViewById(R.id.sesl_date_picker_spinner_year_padding);

        mDaySpinner = findViewById(R.id.sesl_date_picker_spinner_day);
        mDaySpinnerInput = mDaySpinner.findViewById(R.id.numberpicker_input);
        mDaySpinner.setFormatter(NumberPicker.getTwoDigitFormatter());
        mDaySpinner.setOnValueChangedListener(onValueChangeListener);
        mDaySpinner.setOnEditTextModeChangedListener(mModeChangeListener);
        mDaySpinner.setMaxInputLength(2);
        mDaySpinner.setYearDateTimeInputMode();

        mMonthSpinner = findViewById(R.id.sesl_date_picker_spinner_month);
        mMonthSpinnerInput = mMonthSpinner.findViewById(R.id.numberpicker_input);
        if (usingNumericMonths()) {
            mMonthSpinner.setMinValue(1);
            mMonthSpinner.setMaxValue(12);
            mMonthSpinner.setYearDateTimeInputMode();
            mMonthSpinner.setMaxInputLength(2);
        } else {
            mMonthSpinner.setMinValue(0);
            mMonthSpinner.setMaxValue(mNumberOfMonths - 1);
            mMonthSpinner.setFormatter(null);
            mMonthSpinner.setDisplayedValues(mShortMonths);
            mMonthSpinnerInput.setInputType(EditorInfo.TYPE_CLASS_TEXT);
            mMonthSpinner.setMonthInputMode();
        }
        mMonthSpinner.setOnValueChangedListener(onValueChangeListener);
        mMonthSpinner.setOnEditTextModeChangedListener(mModeChangeListener);

        mYearSpinner = findViewById(R.id.sesl_date_picker_spinner_year);
        mYearSpinnerInput = mYearSpinner.findViewById(R.id.numberpicker_input);
        mYearSpinner.setOnValueChangedListener(onValueChangeListener);
        mYearSpinner.setOnEditTextModeChangedListener(mModeChangeListener);
        mYearSpinner.setMaxInputLength(4);
        mYearSpinner.setYearDateTimeInputMode();

        Typeface typeface = Typeface.create("sec-roboto-light", Typeface.BOLD);
        mDaySpinner.setTextTypeface(typeface);
        mMonthSpinner.setTextTypeface(typeface);
        mYearSpinner.setTextTypeface(typeface);

        mToastText = context.getResources().getString(R.string.sesl_number_picker_invalid_value_entered);

        int calcuatedSpinnerTextSize = context.getResources().getInteger(R.integer.sesl_date_picker_spinner_number_text_size);
        float originalSpinnerTextSize = (float) calcuatedSpinnerTextSize;
        mDaySpinner.setTextSize(originalSpinnerTextSize);
        mYearSpinner.setTextSize(originalSpinnerTextSize);
        String language = mCurrentLocale.getLanguage();
        if ("my".equals(language) || "ml".equals(language) || "bn".equals(language) || "ar".equals(language) || "fa".equals(language)) {
            calcuatedSpinnerTextSize = context.getResources().getInteger(R.integer.sesl_date_picker_spinner_long_month_text_size);
        } else if ("ga".equals(language)) {
            calcuatedSpinnerTextSize = context.getResources().getInteger(R.integer.sesl_date_picker_spinner_long_month_text_size) - 1;
        } else if ("hu".equals(language)) {
            calcuatedSpinnerTextSize -= HUNGARIAN_MONTH_TEXT_SIZE_DIFF;
        }
        if (usingNumericMonths()) {
            mMonthSpinner.setTextSize(originalSpinnerTextSize);
        } else {
            mMonthSpinner.setTextSize((float) calcuatedSpinnerTextSize);
        }
        if ("ko".equals(language) || "zh".equals(language) || "ja".equals(language)) {
            float korTextSize = (float) context.getResources().getInteger(R.integer.sesl_date_picker_spinner_number_text_size_with_unit);
            mDaySpinner.setTextSize(korTextSize);
            mMonthSpinner.setTextSize(korTextSize);
            mYearSpinner.setTextSize(korTextSize);
            mDaySpinner.setDateUnit(NumberPicker.MODE_UNIT_DAY);
            mMonthSpinner.setDateUnit(NumberPicker.MODE_UNIT_MONTH);
            mYearSpinner.setDateUnit(NumberPicker.MODE_UNIT_YEAR);
        }

        mDaySpinner.setPickerContentDescription(context.getResources().getString(R.string.sesl_date_picker_day));
        mMonthSpinner.setPickerContentDescription(context.getResources().getString(R.string.sesl_date_picker_month));
        mYearSpinner.setPickerContentDescription(context.getResources().getString(R.string.sesl_date_picker_year));

        mCurrentDate.setTimeInMillis(System.currentTimeMillis());
        init(mCurrentDate.get(Calendar.YEAR), mCurrentDate.get(Calendar.MONTH), mCurrentDate.get(Calendar.DAY_OF_MONTH));

        reorderSpinners();
    }

    @SuppressLint("LongLogTag")
    public void seslLog(String str) {
        if (SESL_DEBUG) {
            Log.d(TAG, str);
        }
    }

    void init(int year, int monthOfYear, int dayOfMonth) {
        setDate(year, monthOfYear, dayOfMonth);
        updateSpinners(true, true, true, true);
    }

    public void updateDate(int year, int monthOfYear, int dayOfMonth) {
        if (isNewDate(year, monthOfYear, dayOfMonth)) {
            setDate(year, monthOfYear, dayOfMonth);
            updateSpinners(true, true, true, true);
        }
    }

    int getYear() {
        return mIsLunar ? mLunarCurrentYear : mCurrentDate.get(Calendar.YEAR);
    }

    int getMonth() {
        return mIsLunar ? mLunarCurrentMonth : mCurrentDate.get(Calendar.MONTH);
    }

    int getDayOfMonth() {
        return mIsLunar ? mLunarCurrentDay : mCurrentDate.get(Calendar.DAY_OF_MONTH);
    }

    public void setMinDate(long minDate) {
        mMinDate.setTimeInMillis(minDate);
        if (mCurrentDate.before(mMinDate)) {
            mCurrentDate.setTimeInMillis(mMinDate.getTimeInMillis());
        }
        updateSpinners(true, true, true, true);
    }

    Calendar getMinDate() {
        return mMinDate;
    }

    public void setMaxDate(long maxDate) {
        mMaxDate.setTimeInMillis(maxDate);
        if (mCurrentDate.after(mMaxDate)) {
            mCurrentDate.setTimeInMillis(mMaxDate.getTimeInMillis());
        }
        updateSpinners(true, true, true, true);
    }

    Calendar getMaxDate() {
        return mMaxDate;
    }

    @Override
    public void setEnabled(boolean enabled) {
        mDaySpinner.setEnabled(enabled);
        mMonthSpinner.setEnabled(enabled);
        mYearSpinner.setEnabled(enabled);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setCurrentLocale(newConfig.locale);
    }

    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        onPopulateAccessibilityEvent(event);
        return true;
    }

    @Override
    public void onPopulateAccessibilityEvent(AccessibilityEvent event) {
        super.onPopulateAccessibilityEvent(event);
        event.getText().add(DateUtils.formatDateTime(mContext, mCurrentDate.getTimeInMillis(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR));
    }

    // kang
    protected void setCurrentLocale(Locale locale) {
        mTempDate = getCalendarForLocale(mTempDate, locale);
        mMinDate = getCalendarForLocale(mMinDate, locale);
        mMaxDate = getCalendarForLocale(mMaxDate, locale);
        mCurrentDate = getCalendarForLocale(mCurrentDate, locale);
        this.mNumberOfMonths = this.mTempDate.getActualMaximum(2) + 1;
        this.mShortMonths = new DateFormatSymbols().getShortMonths();

        int var2 = 0;
        while(true) {
            String[] var4 = this.mShortMonths;
            if (var2 >= var4.length) {
                if (this.usingNumericMonths()) {
                    this.mShortMonths = new String[this.mNumberOfMonths];

                    int var3;
                    for(var2 = 0; var2 < this.mNumberOfMonths; var2 = var3) {
                        var4 = this.mShortMonths;
                        var3 = var2 + 1;
                        var4[var2] = String.format("%d", var3);
                    }
                }

                return;
            }

            var4[var2] = var4[var2].toUpperCase();
            ++var2;
        }
    }
    // kang

    boolean usingNumericMonths() {
        return Character.isDigit(mShortMonths[0].charAt(0));
    }

    private Calendar getCalendarForLocale(Calendar calendar, Locale locale) {
        if (calendar == null) {
            return Calendar.getInstance(locale);
        } else {
            long timeInMillis = calendar.getTimeInMillis();
            Calendar newCalendar = Calendar.getInstance(locale);
            newCalendar.setTimeInMillis(timeInMillis);
            return newCalendar;
        }
    }

    private void reorderSpinners() {
        mSpinners.removeAllViews();

        char[] dateFormatOrder = DateFormat.getDateFormatOrder(mContext);
        int length = dateFormatOrder.length;
        for (int i = 0; i < length; i++) {
            char c = dateFormatOrder[i];
            if (c == 'M') {
                mSpinners.addView(mMonthSpinner);
                setImeOptions(mMonthSpinner, length, i);
            } else if (c == 'd') {
                this.mSpinners.addView(mDaySpinner);
                setImeOptions(mDaySpinner, length, i);
            } else if (c == 'y') {
                mSpinners.addView(mYearSpinner);
                setImeOptions(mYearSpinner, length, i);
            } else {
                throw new IllegalArgumentException(Arrays.toString(dateFormatOrder));
            }
        }

        if (dateFormatOrder[0] == 'y') {
            mSpinners.addView(mYearPaddingView, 0);
            mSpinners.addView(mDayPaddingView);
        } else {
            mSpinners.addView(mDayPaddingView, 0);
            mSpinners.addView(mYearPaddingView);
        }

        if (dateFormatOrder[0] == 'M') {
            setTextWatcher(0);
        } else if (dateFormatOrder[0] == 'd') {
            setTextWatcher(1);
        } else if (dateFormatOrder[0] == 'y') {
            if (dateFormatOrder[1] == 'd') {
                setTextWatcher(3);
            } else {
                setTextWatcher(2);
            }
        }
    }

    private boolean isNewDate(int year, int month, int day) {
        return mCurrentDate.get(Calendar.YEAR) != year || mCurrentDate.get(Calendar.MONTH) != month || mCurrentDate.get(Calendar.DAY_OF_MONTH) != day;
    }

    void setDate(int year, int month, int day) {
        mCurrentDate.set(year, month, day);
        if (mIsLunar) {
            mLunarCurrentYear = year;
            mLunarCurrentMonth = month;
            mLunarCurrentDay = day;
        }

        if (mCurrentDate.before(mMinDate)) {
            mCurrentDate.setTimeInMillis(mMinDate.getTimeInMillis());
        } else if (mCurrentDate.after(mMaxDate)) {
            mCurrentDate.setTimeInMillis(mMaxDate.getTimeInMillis());
        }
    }

    // kang
    private void updateSpinners(boolean var1, boolean var2, boolean var3, boolean var4) {
        if (var2) {
            this.mYearSpinner.setMinValue(this.mMinDate.get(1));
            this.mYearSpinner.setMaxValue(this.mMaxDate.get(1));
            this.mYearSpinner.setWrapSelectorWheel(false);
        }

        int var5;
        int var6;
        int var7;
        int var8;
        if (var3) {
            var5 = this.mMinDate.get(1);
            var6 = this.mMaxDate.get(1);
            var7 = 11;
            if (var6 - var5 == 0) {
                var6 = this.mMinDate.get(2);
                var7 = this.mMaxDate.get(2);
            } else {
                var6 = this.mCurrentDate.get(1);
                if (this.mIsLunar) {
                    var6 = this.mLunarCurrentYear;
                }

                if (var6 == this.mMinDate.get(1)) {
                    var6 = this.mMinDate.get(2);
                    var7 = 11;
                } else {
                    if (var6 == this.mMaxDate.get(1)) {
                        var7 = this.mMaxDate.get(2);
                    }

                    var6 = 0;
                }
            }

            var8 = var7;
            var5 = var6;
            if (this.usingNumericMonths()) {
                var5 = var6 + 1;
                var8 = var7 + 1;
            }

            this.mMonthSpinner.setDisplayedValues((String[])null);
            this.mMonthSpinner.setMinValue(var5);
            this.mMonthSpinner.setMaxValue(var8);
            if (!this.usingNumericMonths()) {
                String[] var9 = (String[])Arrays.copyOfRange(this.mShortMonths, this.mMonthSpinner.getMinValue(), this.mMonthSpinner.getMaxValue() + 1);
                this.mMonthSpinner.setDisplayedValues(var9);
            }
        }

        if (var4) {
            var8 = this.mMinDate.get(1);
            var6 = this.mMaxDate.get(1);
            var5 = this.mMinDate.get(2);
            var7 = this.mMaxDate.get(2);
            if (var6 - var8 == 0 && var7 - var5 == 0) {
                var6 = this.mMinDate.get(5);
                var7 = this.mMaxDate.get(5);
            } else {
                var5 = this.mCurrentDate.get(1);
                var6 = this.mCurrentDate.get(2);
                if (this.mIsLunar) {
                    var5 = this.mLunarCurrentYear;
                    var6 = this.mLunarCurrentMonth;
                }

                if (var5 == this.mMinDate.get(1) && var6 == this.mMinDate.get(2)) {
                    var7 = this.mMinDate.get(5);
                    var8 = this.mCurrentDate.getActualMaximum(5);
                    if (this.mIsLunar) {
                        var5 = this.getLunarMaxDayOfMonth(var5, var6, this.mIsLeapMonth);
                        var6 = var7;
                        var7 = var5;
                    } else {
                        var6 = var7;
                        var7 = var8;
                    }
                } else {
                    label121: {
                        label122: {
                            if (var5 == this.mMaxDate.get(1) && var6 == this.mMaxDate.get(2)) {
                                var8 = this.mMaxDate.get(5);
                                var7 = var8;
                                if (this.mIsLunar) {
                                    var7 = Math.min(var8, this.getLunarMaxDayOfMonth(var5, var6, this.mIsLeapMonth));
                                    break label122;
                                }
                            } else {
                                var7 = this.mCurrentDate.getActualMaximum(5);
                                if (this.mIsLunar) {
                                    var7 = this.getLunarMaxDayOfMonth(var5, var6, this.mIsLeapMonth);
                                    break label122;
                                }
                            }

                            var6 = 1;
                            break label121;
                        }

                        var6 = 1;
                    }
                }
            }

            this.mDaySpinner.setMinValue(var6);
            this.mDaySpinner.setMaxValue(var7);
        }

        if (var1) {
            this.mYearSpinner.setValue(this.mCurrentDate.get(1));
            var7 = this.mCurrentDate.get(2);
            if (this.mIsLunar) {
                var7 = this.mLunarCurrentMonth;
            }

            if (this.usingNumericMonths()) {
                this.mMonthSpinner.setValue(var7 + 1);
            } else {
                this.mMonthSpinner.setValue(var7);
            }

            var7 = this.mCurrentDate.get(5);
            if (this.mIsLunar) {
                var7 = this.mLunarCurrentDay;
            }

            this.mDaySpinner.setValue(var7);
            if (this.usingNumericMonths()) {
                this.mMonthSpinnerInput.setRawInputType(2);
            }

            if (this.mIsEditTextMode) {
                EditText[] var10 = this.mPickerTexts;
                if (var10 != null) {
                    var6 = var10.length;

                    for(var7 = 0; var7 < var6; ++var7) {
                        EditText var11 = var10[var7];
                        if (var11.hasFocus()) {
                            var11.setSelection(0, 0);
                            var11.selectAll();
                            break;
                        }
                    }
                }
            }

        }
    }
    // kang

    private void notifyDateChanged() {
        if (mOnSpinnerDateChangedListener != null) {
            mOnSpinnerDateChangedListener.onDateChanged(this, getYear(), getMonth(), getDayOfMonth());
        }
    }

    // kang
    private void setImeOptions(NumberPicker view, int i, int i2) {
        ((TextView) view.findViewById(R.id.numberpicker_input)).setImeOptions(i2 < i + -1 ? 33554437 : 33554438);
    }
    // kang

    public void updateInputState() {
        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm == null) {
            return;
        }
        if (imm.isActive(mYearSpinnerInput)) {
            imm.hideSoftInputFromWindow(getWindowToken(), 0);
            mYearSpinnerInput.clearFocus();
        } else if (imm.isActive(mMonthSpinnerInput)) {
            imm.hideSoftInputFromWindow(getWindowToken(), 0);
            mMonthSpinnerInput.clearFocus();
        } else if (imm.isActive(mDaySpinnerInput)) {
            imm.hideSoftInputFromWindow(getWindowToken(), 0);
            mDaySpinnerInput.clearFocus();
        }
    }

    // kang
    private void setTextWatcher(int var1) {
        byte var2;
        byte var3;
        byte var4;
        label39: {
            label38: {
                this.seslLog("setTextWatcher() usingNumericMonths  : " + this.usingNumericMonths() + "format  : " + var1);
                var2 = -1;
                var3 = 0;
                var4 = 2;
                if (var1 != 0) {
                    if (var1 == 1) {
                        var2 = 2;
                        var4 = 0;
                        break label38;
                    }

                    if (var1 == 2) {
                        var2 = 0;
                        break label38;
                    }

                    if (var1 != 3) {
                        var3 = -1;
                        var4 = var3;
                        break label39;
                    }

                    var2 = 0;
                    var3 = 2;
                } else {
                    var2 = 2;
                }

                var4 = 1;
                break label39;
            }

            var3 = 1;
        }

        this.mPickerTexts[var2] = this.mYearSpinner.getEditText();
        this.mPickerTexts[var3] = this.mMonthSpinner.getEditText();
        this.mPickerTexts[var4] = this.mDaySpinner.getEditText();
        this.mPickerTexts[var2].addTextChangedListener(new SeslTextWatcher(4, var2, false));
        if (this.usingNumericMonths()) {
            this.mPickerTexts[var3].addTextChangedListener(new SeslTextWatcher(2, var3, true));
        } else {
            this.mPickerTexts[var3].addTextChangedListener(new SeslTextWatcher(3, var3, true));
        }

        this.mPickerTexts[var4].addTextChangedListener(new SeslTextWatcher(2, var4, false));
        if (var1 != 3 || this.usingNumericMonths()) {
            EditText[] var5 = this.mPickerTexts;
            var5[var5.length - 1].setOnEditorActionListener(this.mEditorActionListener);
        }

        this.mPickerTexts[var2].setOnKeyListener(new SeslKeyListener());
        this.mPickerTexts[var3].setOnKeyListener(new SeslKeyListener());
        this.mPickerTexts[var4].setOnKeyListener(new SeslKeyListener());
    }
    // kang

    private class SeslKeyListener implements View.OnKeyListener {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            seslLog(event.toString());

            if (event.getAction() != KeyEvent.ACTION_UP) {
                return false;
            }

            switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_CENTER:
                    //if (getResources().getConfiguration().keyboard == Configuration.KEYBOARD_12KEY) {
                    //}
                    return false;
                case KeyEvent.KEYCODE_TAB:
                    return true;
                case KeyEvent.KEYCODE_ENTER:
                case KeyEvent.KEYCODE_NUMPAD_ENTER:
                    if (isEditTextMode()) {
                        EditText editText = (EditText) v;
                        if ((editText.getImeOptions() & EditorInfo.IME_ACTION_NEXT) == EditorInfo.IME_ACTION_NEXT) {
                            View findNextFocus = FocusFinder.getInstance().findNextFocus(mDatePicker, v, View.FOCUS_FORWARD);
                            if (findNextFocus == null) {
                                return true;
                            }
                            findNextFocus.requestFocus();
                        } else if ((editText.getImeOptions() & EditorInfo.IME_ACTION_DONE) == EditorInfo.IME_ACTION_DONE) {
                            updateInputState();
                            setEditTextMode(false);
                        }
                    }
            }

            return false;
        }
    }

    private class SeslTextWatcher implements TextWatcher {
        private final int INVALID_POSITION_ID = -1;
        private final int LAST_POSITION_ID = 2;
        private int mChangedLen = 0;
        private int mCheck;
        private int mId;
        private boolean mIsMonth;
        private int mMaxLen;
        private int mNext;
        private String mPrevText;

        private SeslTextWatcher(int maxLength, int index, boolean isMonth) {
            mMaxLen = maxLength;
            mId = index;
            mIsMonth = isMonth;
            mCheck = index - 1;
            if (mCheck < 0) {
                mCheck = LAST_POSITION_ID;
            }
            mNext = index + 1 <= LAST_POSITION_ID ? index + 1 : INVALID_POSITION_ID;
        }

        @Override
        public void afterTextChanged(Editable s) {
            seslLog("[" + mId + "] afterTextChanged: " + s.toString());
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            seslLog("[" + mId + "] beforeTextChanged: " + ((Object) s) + ", " + start + ", " + count + ", " + after);
            mPrevText = s.toString();
            mChangedLen = after;
        }

        // kang
        @Override
        public void onTextChanged(CharSequence var1, int var2, int var3, int var4) {
            seslLog("[" + this.mId + "] onTextChanged: " + this.mPrevText + " -> " + var1);
            var2 = var1.length();
            String var5 = var1.toString();
            String var10 = (String) mPickerTexts[this.mId].getTag();
            if (var10 == null || !"onClick".equals(var10) && !"onLongClick".equals(var10)) {
                if (mPickerTexts[this.mId].isFocused()) {
                    boolean var6 = this.mIsMonth;
                    var10 = "";
                    if (var6) {
                        if (usingNumericMonths() && this.mChangedLen == 1) {
                            seslLog("[" + this.mId + "] Samsung Keypad Num Month");
                            var4 = mMonthSpinner.getMinValue();
                            var3 = Integer.parseInt(var5);
                            if (var2 == this.mMaxLen) {
                                if (var3 < var4) {
                                    if (Character.getNumericValue(var5.charAt(0)) < 2) {
                                        this.showInvalidValueEnteredToast(Character.toString(var5.charAt(0)), 1);
                                    } else {
                                        this.showInvalidValueEnteredToast("", 0);
                                    }

                                    return;
                                }

                                this.changeFocus();
                            } else if (var2 > 0) {
                                if (var4 >= 10 && "0".equals(var5)) {
                                    this.showInvalidValueEnteredToast("", 0);
                                    return;
                                }

                                if (!"1".equals(var5) && !"0".equals(var5)) {
                                    if (var3 < var4) {
                                        this.showInvalidValueEnteredToast("", 0);
                                        return;
                                    }

                                    this.changeFocus();
                                }
                            }
                        } else if (!this.isNumericStr(this.mPrevText)) {
                            if (var2 >= this.mMaxLen) {
                                if (this.isMeaLanguage()) {
                                    if (TextUtils.isEmpty(this.mPrevText) && this.isMonthStr(var5)) {
                                        this.changeFocus();
                                    }
                                } else {
                                    this.changeFocus();
                                }
                            } else if ((this.isSwaLanguage() || this.isFarsiLanguage()) && var2 > 0 && !this.isNumericStr(var5)) {
                                this.changeFocus();
                            }
                        }
                    } else if (this.mChangedLen == 1) {
                        if (this.mMaxLen < 3) {
                            var3 = mDaySpinner.getMinValue();
                            var4 = Integer.parseInt(var5);
                            if (this.mPrevText.length() < var2 && var2 == this.mMaxLen) {
                                if (var4 < var3) {
                                    if (Character.getNumericValue(var5.charAt(0)) < 4) {
                                        this.showInvalidValueEnteredToast(Character.toString(var5.charAt(0)), 1);
                                    } else {
                                        this.showInvalidValueEnteredToast("", 0);
                                    }

                                    return;
                                }

                                this.changeFocus();
                            } else {
                                if (var3 >= 10 && var4 == 0 || var3 >= 20 && (var4 == 0 || var4 == 1) || var3 >= 30 && (var4 == 0 || var4 == 1 || var4 == 2)) {
                                    this.showInvalidValueEnteredToast("", 0);
                                    return;
                                }

                                if (var4 > 3) {
                                    if (var4 < var3) {
                                        this.showInvalidValueEnteredToast("", 0);
                                        return;
                                    }

                                    this.changeFocus();
                                }

                                if (usingNumericMonths()) {
                                    var2 = mMonthSpinner.getValue() - 1;
                                } else {
                                    var2 = mMonthSpinner.getValue();
                                }

                                if (!mIsLunar && var2 == 1 && var4 == 3) {
                                    if (var4 < var3) {
                                        this.showInvalidValueEnteredToast("", 0);
                                        return;
                                    }

                                    this.changeFocus();
                                }
                            }
                        } else {
                            int var7 = mYearSpinner.getMinValue();
                            var4 = mYearSpinner.getMaxValue();
                            var3 = Integer.parseInt(var5);
                            if (this.mPrevText.length() < var2 && var2 == this.mMaxLen) {
                                if (var3 < var7 || var3 > var4) {
                                    this.showInvalidValueEnteredToast(var5.substring(0, 3), 3);
                                    return;
                                }

                                if (usingNumericMonths()) {
                                    var2 = mMonthSpinner.getValue() - 1;
                                } else {
                                    var2 = mMonthSpinner.getValue();
                                }

                                mTempDate.clear();
                                mTempDate.set(var3, var2, mDaySpinner.getValue());
                                Calendar var11 = Calendar.getInstance();
                                var11.clear();
                                var11.set(mMinDate.get(1), mMinDate.get(2), mMinDate.get(5));
                                if (mTempDate.before(var11) || mTempDate.after(mMaxDate)) {
                                    this.showInvalidValueEnteredToast(var5.substring(0, 3), 3);
                                    return;
                                }

                                this.changeFocus();
                            } else {
                                int var8 = var2 - 1;
                                int var9 = (int)(1000.0D / Math.pow(10.0D, (double)var8));
                                if (var2 != 1) {
                                    var10 = var5.substring(0, var8);
                                }

                                if (var3 < var7 / var9 || var3 > var4 / var9) {
                                    this.showInvalidValueEnteredToast(var10, var8);
                                }
                            }
                        }
                    }

                }
            } else {
                seslLog("[" + this.mId + "] TAG exists: " + var10);
            }
        }
        // kang

        private void showInvalidValueEnteredToast(String text, int index) {
            mPickerTexts[mId].setText(text);
            if (index != 0) {
                mPickerTexts[mId].setSelection(index);
            }
            if (mToast == null) {
                mToast = Toast.makeText(mContext, mToastText, Toast.LENGTH_SHORT);
                View view = LayoutInflater.from(mContext).inflate(R.layout.sesl_custom_toast_layout, null);
                ((TextView) view.findViewById(R.id.message)).setText(mToastText);
                mToast.setView(view);
            }
            mToast.show();
        }

        private boolean isSwaLanguage() {
            String language = mCurrentLocale.getLanguage();
            return "hi".equals(language) || "ta".equals(language) || "ml".equals(language) || "te".equals(language) || "or".equals(language) || "ne".equals(language) || "as".equals(language) || "bn".equals(language) || "gu".equals(language) || "si".equals(language) || "pa".equals(language) || "kn".equals(language) || "mr".equals(language);
        }

        private boolean isMeaLanguage() {
            String language = mCurrentLocale.getLanguage();
            return "ar".equals(language) || "fa".equals(language) || "ur".equals(language);
        }

        private boolean isFarsiLanguage() {
            return "fa".equals(mCurrentLocale.getLanguage());
        }

        private boolean isNumericStr(String str) {
            return !TextUtils.isEmpty(str) && Character.isDigit(str.charAt(0));
        }

        private boolean isMonthStr(String str) {
            for (int i = 0; i < mNumberOfMonths; i++) {
                if (str.equals(mShortMonths[i])) {
                    return true;
                }
            }
            return false;
        }

        private void changeFocus() {
            AccessibilityManager accessibilityManager = (AccessibilityManager) mContext.getSystemService(Context.ACCESSIBILITY_SERVICE);
            if (accessibilityManager == null || !accessibilityManager.isTouchExplorationEnabled()) {
                seslLog("[" + mId + "] changeFocus() mNext : " + mNext + ", mCheck : " + mCheck);
                if (mNext >= 0) {
                    if (!mPickerTexts[mCheck].isFocused()) {
                        mPickerTexts[mNext].requestFocus();
                    }
                    if (mPickerTexts[mId].isFocused()) {
                        mPickerTexts[mId].clearFocus();
                    }
                }
            }
        }
    }

    public void setOnSpinnerDateChangedListener(DatePicker view, OnSpinnerDateChangedListener listener) {
        if (mDatePicker == null) {
            mDatePicker = view;
        }
        mOnSpinnerDateChangedListener = listener;
    }

    private void updateModeState(boolean edit) {
        if (mIsEditTextMode != edit && !edit) {
            if (mDaySpinner.isEditTextMode()) {
                mDaySpinner.setEditTextMode(false);
            }
            if (mMonthSpinner.isEditTextMode()) {
                mMonthSpinner.setEditTextMode(false);
            }
            if (mYearSpinner.isEditTextMode()) {
                mYearSpinner.setEditTextMode(false);
            }
        }
    }

    public void setEditTextMode(boolean edit) {
        if (mIsEditTextMode != edit) {
            mIsEditTextMode = edit;

            InputMethodManager inputMethodManager = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            mDaySpinner.setEditTextMode(edit);
            mMonthSpinner.setEditTextMode(edit);
            mYearSpinner.setEditTextMode(edit);
            if (inputMethodManager != null) {
                if (!mIsEditTextMode) {
                    inputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
                } else {
                    inputMethodManager.showSoftInput(mDaySpinner, 0);
                }
            }

            if (mOnEditTextModeChangedListener != null) {
                mOnEditTextModeChangedListener.onEditTextModeChanged(mDatePicker, edit);
            }
        }
    }

    public boolean isEditTextMode() {
        return mIsEditTextMode;
    }

    public void setOnEditTextModeChangedListener(DatePicker view, DatePicker.OnEditTextModeChangedListener listener) {
        if (mDatePicker == null) {
            mDatePicker = view;
        }
        mOnEditTextModeChangedListener = listener;
    }

    public EditText getEditText(int index) {
        if (index == PICKER_DAY) {
            return mDaySpinner.getEditText();
        }
        if (index == PICKER_MONTH) {
            return mMonthSpinner.getEditText();
        }
        if (index != PICKER_YEAR) {
            return mDaySpinner.getEditText();
        }
        return mYearSpinner.getEditText();
    }

    public NumberPicker getNumberPicker(int index) {
        if (index == PICKER_DAY) {
            return mDaySpinner;
        }
        if (index == PICKER_MONTH) {
            return mMonthSpinner;
        }
        if (index != PICKER_YEAR) {
            return mDaySpinner;
        }
        return mYearSpinner;
    }

    public void setLunar(boolean isLunar, boolean isLeapMonth, PathClassLoader pathClassLoader) {
        mIsLunar = isLunar;
        mIsLeapMonth = isLeapMonth;
        if (isLunar && mPathClassLoader == null) {
            mPathClassLoader = pathClassLoader;
            mSolarLunarTables = SeslFeatureReflector.getSolarLunarTables(pathClassLoader);
        }
        updateSpinners(false, true, true, true);
    }

    void setIsLeapMonth(boolean isLeapMonth) {
        mIsLeapMonth = isLeapMonth;
    }

    private int getLunarMaxDayOfMonth(int year, int month, boolean leap) {
        if (mSolarLunarTables == null) {
            return 0;
        }
        return SeslSolarLunarTablesReflector.getDayLengthOf(mPathClassLoader, mSolarLunarTables, year, month, leap);
    }

    @Override
    public void requestLayout() {
        super.requestLayout();
        if (mDaySpinner != null) {
            mDaySpinner.requestLayout();
        }
        if (mYearSpinner != null) {
            mYearSpinner.requestLayout();
        }
        if (mMonthSpinner != null) {
            mMonthSpinner.requestLayout();
        }
    }
}
