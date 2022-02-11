package de.dlyt.yanndroid.oneui.sesl.picker.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.icu.text.SimpleDateFormat;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeProvider;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.reflect.view.SeslViewReflector;

import java.util.Calendar;
import java.util.Locale;

public class SeslSpinningDatePickerSpinner extends LinearLayout {
    public static final DateFormatter mDateFormatter = new DateFormatter();
    public DatePickerDelegate mDelegate;

    public interface DatePickerDelegate {
        void computeScroll();

        int computeVerticalScrollExtent();

        int computeVerticalScrollOffset();

        int computeVerticalScrollRange();

        boolean dispatchHoverEvent(MotionEvent motionEvent);

        boolean dispatchKeyEvent(KeyEvent keyEvent);

        boolean dispatchKeyEventPreIme(KeyEvent keyEvent);

        boolean dispatchTouchEvent(MotionEvent motionEvent);

        void dispatchTrackballEvent(MotionEvent motionEvent);

        AccessibilityNodeProvider getAccessibilityNodeProvider();

        void onAttachedToWindow();

        void onConfigurationChanged(Configuration configuration);

        void onDetachedFromWindow();

        void onDraw(Canvas canvas);

        void onFocusChanged(boolean z, int i, Rect rect);

        boolean onGenericMotionEvent(MotionEvent motionEvent);

        void onInitializeAccessibilityEvent(AccessibilityEvent accessibilityEvent);

        boolean onInterceptTouchEvent(MotionEvent motionEvent);

        void onLayout(boolean z, int i, int i2, int i3, int i4);

        void onMeasure(int i, int i2);

        void onPopulateAccessibilityEvent(AccessibilityEvent accessibilityEvent);

        boolean onTouchEvent(MotionEvent motionEvent);

        void onWindowFocusChanged(boolean z);

        void onWindowVisibilityChanged(int i);

        void performClick();

        void performLongClick();

        void scrollBy(int i, int i2);

        void setEnabled(boolean z);
    }

    public interface Formatter {
        String format(Calendar calendar);
    }

    public interface OnScrollListener {
        void onScrollStateChange(SeslSpinningDatePickerSpinner seslSpinningDatePickerSpinner, int i);
    }

    public interface OnSpinnerDateClickListener {
        void onSpinnerDateClicked(Calendar calendar, SeslSpinningDatePicker$LunarDate seslSpinningDatePicker$LunarDate);
    }

    public interface OnValueChangeListener {
        void onValueChange(SeslSpinningDatePickerSpinner seslSpinningDatePickerSpinner, Calendar calendar, Calendar calendar2, boolean z, SeslSpinningDatePicker$LunarDate seslSpinningDatePicker$LunarDate);
    }

    public static class DateFormatter implements Formatter {
        public final Object[] mArgs = new Object[1];
        public Locale mCurrentLocale;
        public SimpleDateFormat mFmt;

        public DateFormatter() {
            init(Locale.getDefault());
        }

        public final void init(Locale locale) {
            this.mFmt = createFormatter(locale);
            this.mCurrentLocale = locale;
        }

        @Override // androidx.picker.widget.SeslSpinningDatePickerSpinner.Formatter
        public String format(Calendar calendar) {
            Locale locale = Locale.getDefault();
            if (!this.mCurrentLocale.equals(locale)) {
                init(locale);
            }
            this.mArgs[0] = calendar;
            return this.mFmt.format(calendar.getTime());
        }

        public String formatForAccessibility(Calendar calendar, Context context) {
            return DateUtils.formatDateTime(context, calendar.getTimeInMillis(), 26);
        }

        public String format(Calendar calendar, Context context) {
            this.mArgs[0] = calendar;
            return DateUtils.formatDateTime(context, calendar.getTimeInMillis(), 524314);
        }

        public final SimpleDateFormat createFormatter(Locale locale) {
            if (isSimplifiedChinese(locale)) {
                return new SimpleDateFormat("EEEEE, MMM dd", locale);
            }
            if (isRTL(locale)) {
                return new SimpleDateFormat("EEEEE, MMM dd", locale);
            }
            return new SimpleDateFormat("EEE, MMM dd", locale);
        }

        public final boolean isRTL(Locale locale) {
            byte directionality = Character.getDirectionality(locale.getDisplayName(locale).charAt(0));
            return directionality == 1 || directionality == 2;
        }

        public final boolean isSimplifiedChinese(Locale locale) {
            return locale.getLanguage().equals(Locale.SIMPLIFIED_CHINESE.getLanguage()) && locale.getCountry().equals(Locale.SIMPLIFIED_CHINESE.getCountry());
        }
    }

    public static Formatter getDateFormatter() {
        return mDateFormatter;
    }

    public SeslSpinningDatePickerSpinner(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public SeslSpinningDatePickerSpinner(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public SeslSpinningDatePickerSpinner(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mDelegate = new SeslSpinningDatePickerSpinnerDelegate(this, context, attributeSet, i, i2);
    }

    public void onWindowVisibilityChanged(int i) {
        this.mDelegate.onWindowVisibilityChanged(i);
        super.onWindowVisibilityChanged(i);
    }

    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        this.mDelegate.onLayout(z, i, i2, i3, i4);
    }

    @SuppressLint({"WrongCall"})
    public void superOnMeasure(int i, int i2) {
        super.onMeasure(i, i2);
    }

    public void setMeasuredDimensionWrapper(int i, int i2) {
        setMeasuredDimension(i, i2);
    }

    public void onMeasure(int i, int i2) {
        this.mDelegate.onMeasure(i, i2);
    }

    public boolean dispatchKeyEventPreIme(KeyEvent keyEvent) {
        if (this.mDelegate.dispatchKeyEventPreIme(keyEvent)) {
            return true;
        }
        return super.dispatchKeyEventPreIme(keyEvent);
    }

    public void onWindowFocusChanged(boolean z) {
        super.onWindowFocusChanged(z);
        this.mDelegate.onWindowFocusChanged(z);
    }

    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        return this.mDelegate.onInterceptTouchEvent(motionEvent);
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        return this.mDelegate.onTouchEvent(motionEvent);
    }

    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        this.mDelegate.dispatchTouchEvent(motionEvent);
        return super.dispatchTouchEvent(motionEvent);
    }

    public boolean onGenericMotionEvent(MotionEvent motionEvent) {
        if (this.mDelegate.onGenericMotionEvent(motionEvent)) {
            return true;
        }
        return super.onGenericMotionEvent(motionEvent);
    }

    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        this.mDelegate.onConfigurationChanged(configuration);
    }

    public void onFocusChanged(boolean z, int i, Rect rect) {
        this.mDelegate.onFocusChanged(z, i, rect);
        super.onFocusChanged(z, i, rect);
    }

    public boolean dispatchKeyEvent(KeyEvent keyEvent) {
        if (this.mDelegate.dispatchKeyEvent(keyEvent)) {
            return true;
        }
        return super.dispatchKeyEvent(keyEvent);
    }

    public boolean dispatchTrackballEvent(MotionEvent motionEvent) {
        this.mDelegate.dispatchTrackballEvent(motionEvent);
        return super.dispatchTrackballEvent(motionEvent);
    }

    public boolean dispatchHoverEvent(MotionEvent motionEvent) {
        return this.mDelegate.dispatchHoverEvent(motionEvent);
    }

    public void computeScroll() {
        this.mDelegate.computeScroll();
    }

    public void setEnabled(boolean z) {
        super.setEnabled(z);
        this.mDelegate.setEnabled(z);
    }

    public void scrollBy(int i, int i2) {
        this.mDelegate.scrollBy(i, i2);
    }

    public int computeVerticalScrollOffset() {
        return this.mDelegate.computeVerticalScrollOffset();
    }

    public int computeVerticalScrollRange() {
        return this.mDelegate.computeVerticalScrollRange();
    }

    public int computeVerticalScrollExtent() {
        return this.mDelegate.computeVerticalScrollExtent();
    }

    public boolean performClick() {
        if (super.performClick()) {
            return true;
        }
        this.mDelegate.performClick();
        return true;
    }

    public boolean performLongClick() {
        if (super.performLongClick()) {
            return true;
        }
        this.mDelegate.performLongClick();
        return true;
    }

    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mDelegate.onDetachedFromWindow();
    }

    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mDelegate.onAttachedToWindow();
    }

    public void onDraw(Canvas canvas) {
        this.mDelegate.onDraw(canvas);
    }

    public void onInitializeAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        super.onInitializeAccessibilityEvent(accessibilityEvent);
        this.mDelegate.onInitializeAccessibilityEvent(accessibilityEvent);
    }

    public void onPopulateAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        super.onPopulateAccessibilityEvent(accessibilityEvent);
        this.mDelegate.onPopulateAccessibilityEvent(accessibilityEvent);
    }

    public AccessibilityNodeProvider getAccessibilityNodeProvider() {
        return this.mDelegate.getAccessibilityNodeProvider();
    }

    public int[] getEnableStateSet() {
        return LinearLayout.ENABLED_STATE_SET;
    }

    public boolean isVisibleToUserWrapper() {
        return SeslViewReflector.isVisibleToUser(this);
    }

    public boolean isVisibleToUserWrapper(Rect rect) {
        return SeslViewReflector.isVisibleToUser(this, rect);
    }

    public static class CustomEditText extends EditText {
        public int mAdjustEditTextPosition;

        public CustomEditText(Context context, AttributeSet attributeSet) {
            super(context, attributeSet);
        }

        public void setEditTextPosition(int i) {
            this.mAdjustEditTextPosition = i;
        }

        public void onDraw(Canvas canvas) {
            canvas.translate(0.0f, (float) this.mAdjustEditTextPosition);
            super.onDraw(canvas);
        }
    }

    public static abstract class AbsDatePickerDelegate implements DatePickerDelegate {
        public Context mContext;
        public SeslSpinningDatePickerSpinner mDelegator;

        public AbsDatePickerDelegate(SeslSpinningDatePickerSpinner seslSpinningDatePickerSpinner, Context context) {
            this.mDelegator = seslSpinningDatePickerSpinner;
            this.mContext = context;
        }
    }
}
