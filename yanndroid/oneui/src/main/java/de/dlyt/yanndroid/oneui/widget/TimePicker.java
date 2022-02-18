package de.dlyt.yanndroid.oneui.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.EditText;
import android.widget.FrameLayout;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;

import java.util.Locale;

import de.dlyt.yanndroid.oneui.sesl.picker.SeslTimePickerSpinnerDelegate;
import de.dlyt.yanndroid.oneui.sesl.utils.SeslAnimationListener;

public class TimePicker extends FrameLayout {
    public static final int PICKER_AMPM = 2;
    public static final int PICKER_DIVIDER = 3;
    public static final int PICKER_HOUR = 0;
    public static final int PICKER_MINUTE = 1;
    private TimePickerDelegate mDelegate;

    public interface OnEditTextModeChangedListener {
        void onEditTextModeChanged(TimePicker view, boolean edit);
    }

    public interface OnTimeChangedListener {
        void onTimeChanged(TimePicker view, int hourOfDay, int minute);
    }

    public TimePicker(Context context) {
        this(context, null);
    }

    public TimePicker(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.timePickerStyle);
    }

    public TimePicker(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public TimePicker(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mDelegate = new SeslTimePickerSpinnerDelegate(this, context, attrs, defStyleAttr, defStyleRes);
    }

    public void setOnEditTextModeChangedListener(OnEditTextModeChangedListener onEditTextModeChangedListener) {
        mDelegate.setOnEditTextModeChangedListener(onEditTextModeChangedListener);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        if (widthMode == MeasureSpec.AT_MOST) {
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(mDelegate.getDefaultWidth(), MeasureSpec.EXACTLY);
        }
        if (heightMode == MeasureSpec.AT_MOST) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(mDelegate.getDefaultHeight(), MeasureSpec.EXACTLY);
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void setEditTextMode(boolean edit) {
        mDelegate.setEditTextMode(edit);
    }

    public boolean isEditTextMode() {
        return mDelegate.isEditTextMode();
    }

    public void setHour(@IntRange(from = 0, to = 23) int hour) {
        mDelegate.setHour(constrain(hour, 0, 23));
    }

    public int getHour() {
        return mDelegate.getHour();
    }

    public void setMinute(@IntRange(from = 0, to = 59) int minute) {
        mDelegate.setMinute(constrain(minute, 0, 59));
    }

    public int getMinute() {
        return mDelegate.getMinute();
    }

    public void setIs24HourView(@NonNull Boolean is24HourView) {
        if (is24HourView == null) {
            return;
        }

        mDelegate.setIs24Hour(is24HourView);
    }

    public void showMarginLeft(Boolean show) {
        mDelegate.showMarginLeft(show);
    }

    public boolean is24HourView() {
        return mDelegate.is24Hour();
    }

    public void set5MinuteInterval() {
        set5MinuteInterval(true);
    }

    public void set5MinuteInterval(boolean interval) {
        mDelegate.set5MinuteInterval(interval);
    }

    public void setOnTimeChangedListener(OnTimeChangedListener onTimeChangedListener) {
        mDelegate.setOnTimeChangedListener(onTimeChangedListener);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        mDelegate.setEnabled(enabled);
    }

    @Override
    public boolean isEnabled() {
        return mDelegate.isEnabled();
    }

    @Override
    public int getBaseline() {
        return mDelegate.getBaseline();
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDelegate.onConfigurationChanged(newConfig);
    }

    @Override
    protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
        dispatchThawSelfOnly(container);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        return mDelegate.onSaveInstanceState(superState);
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        BaseSavedState ss = (BaseSavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        mDelegate.onRestoreInstanceState(ss);
    }

    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        return mDelegate.dispatchPopulateAccessibilityEvent(event);
    }

    @Override
    public void onPopulateAccessibilityEvent(AccessibilityEvent event) {
        super.onPopulateAccessibilityEvent(event);
        mDelegate.onPopulateAccessibilityEvent(event);
    }

    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        mDelegate.onInitializeAccessibilityEvent(event);
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        mDelegate.onInitializeAccessibilityNodeInfo(info);
    }

    public void setLocale(Locale locale) {
        mDelegate.setCurrentLocale(locale);
    }

    public void startAnimation(int delayMillis, SeslAnimationListener listener) {
        mDelegate.startAnimation(delayMillis, listener);
    }

    public EditText getEditText(int index) {
        return mDelegate.getEditText(index);
    }

    public NumberPicker getNumberPicker(int index) {
        return mDelegate.getNumberPicker(index);
    }

    public void setNumberPickerTextSize(int index, float size) {
        mDelegate.setNumberPickerTextSize(index, size);
    }

    public void setNumberPickerTextTypeface(int index, Typeface typeface) {
        mDelegate.setNumberPickerTextTypeface(index, typeface);
    }

    @Override
    public void requestLayout() {
        super.requestLayout();
        if (mDelegate != null) {
            mDelegate.requestLayout();
        }
    }

    private interface TimePickerDelegate {
        boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event);

        int getBaseline();

        int getDefaultHeight();

        int getDefaultWidth();

        EditText getEditText(int index);

        int getHour();

        int getMinute();

        NumberPicker getNumberPicker(int index);

        boolean is24Hour();

        boolean isEditTextMode();

        boolean isEnabled();

        void onConfigurationChanged(Configuration newConfig);

        void onInitializeAccessibilityEvent(AccessibilityEvent event);

        void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info);

        void onPopulateAccessibilityEvent(AccessibilityEvent event);

        void onRestoreInstanceState(Parcelable state);

        Parcelable onSaveInstanceState(Parcelable superState);

        void requestLayout();

        void set5MinuteInterval(boolean interval);

        void setCurrentLocale(Locale locale);

        void setEditTextMode(boolean edit);

        void setEnabled(boolean enabled);

        void setHour(int hour);

        void setIs24Hour(boolean is24Hour);

        void setMinute(int minute);

        void setNumberPickerTextSize(int index, float size);

        void setNumberPickerTextTypeface(int index, Typeface typeface);

        void setOnEditTextModeChangedListener(OnEditTextModeChangedListener onEditTextModeChangedListener);

        void setOnTimeChangedListener(OnTimeChangedListener onTimeChangedListener);

        void showMarginLeft(boolean show);

        void startAnimation(int delayMillis, SeslAnimationListener listener);
    }

    public static abstract class AbsTimePickerDelegate implements TimePickerDelegate {
        protected Context mContext;
        protected Locale mCurrentLocale;
        protected TimePicker mDelegator;
        protected OnEditTextModeChangedListener mOnEditTextModeChangedListener;
        protected OnTimeChangedListener mOnTimeChangedListener;

        protected AbsTimePickerDelegate(@NonNull TimePicker delegator, @NonNull Context context) {
            mDelegator = delegator;
            mContext = context;
            setCurrentLocale(Locale.getDefault());
        }

        @Override
        public void setCurrentLocale(Locale locale) {
            if (!locale.equals(mCurrentLocale)) {
                mCurrentLocale = locale;
            }
        }
    }

    /*kang from MathUtils.smali*/
    private int constrain(int amount, int low, int high) {
        return amount < low ? low : (amount > high ? high : amount);
    }
}
