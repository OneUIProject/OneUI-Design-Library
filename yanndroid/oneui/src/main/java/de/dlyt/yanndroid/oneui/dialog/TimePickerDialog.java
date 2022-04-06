package de.dlyt.yanndroid.oneui.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;

import de.dlyt.yanndroid.oneui.R;
import de.dlyt.yanndroid.oneui.widget.TimePicker;
import de.dlyt.yanndroid.oneui.sesl.utils.SeslAnimationListener;

public class TimePickerDialog extends AlertDialog implements DialogInterface.OnClickListener, TimePicker.OnTimeChangedListener {
    private static final String HOUR = "hour";
    private static final String IS_24_HOUR = "is24hour";
    private static final String MINUTE = "minute";
    private InputMethodManager mImm;
    private final int mInitialHourOfDay;
    private final int mInitialMinute;
    private final boolean mIs24HourView;
    private boolean mIsStartAnimation;
    private final TimePicker mTimePicker;
    private final OnTimeSetListener mTimeSetListener;

    private final View.OnFocusChangeListener mBtnFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (mTimePicker.isEditTextMode() && hasFocus) {
                mTimePicker.setEditTextMode(false);
            }
        }
    };

    public interface OnTimeSetListener {
        void onTimeSet(TimePicker view, int hourOfDay, int minute);
    }

    public TimePickerDialog(Context context, OnTimeSetListener listener, int hourOfDay, int minute, boolean is24HourView) {
        this(context, 0, listener, hourOfDay, minute, is24HourView);
    }

    static int resolveDialogTheme(@NonNull Context context, int theme) {
        if (theme != 0) {
            return theme;
        }
        return context.getTheme().obtainStyledAttributes(new int[]{R.attr.isOneUI4}).getBoolean(0, false) ? R.style.OneUI4_PickerDialogStyle : R.style.PickerDialogStyle;
    }

    public TimePickerDialog(Context context, int theme, OnTimeSetListener listener, int hourOfDay, int minute, boolean is24HourView) {
        super(context, theme);
        mTimeSetListener = listener;
        mInitialHourOfDay = hourOfDay;
        mInitialMinute = minute;
        mIs24HourView = is24HourView;

        View view = LayoutInflater.from(getContext()).inflate(R.layout.sesl_time_picker_spinner_dialog, null);
        setView(view);
        setButton(BUTTON_POSITIVE, getContext().getString(R.string.sesl_picker_done), this);
        setButton(BUTTON_NEGATIVE, getContext().getString(R.string.sesl_picker_cancel), this);

        mTimePicker = view.findViewById(R.id.timePicker);
        mTimePicker.setIs24HourView(is24HourView);
        mTimePicker.setHour(hourOfDay);
        mTimePicker.setMinute(minute);
        mTimePicker.setOnTimeChangedListener(this);

        setTitle(R.string.sesl_time_picker_set_title);
        mImm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getButton(BUTTON_POSITIVE).setOnFocusChangeListener(mBtnFocusChangeListener);
        getButton(BUTTON_NEGATIVE).setOnFocusChangeListener(mBtnFocusChangeListener);
        mIsStartAnimation = true;
        mTimePicker.startAnimation(283, new SeslAnimationListener() {
            @Override
            public void onAnimationEnd() {
                mIsStartAnimation = false;
            }
        });
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == BUTTON_NEGATIVE) {
            if (mImm != null) {
                mImm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
            }
            cancel();
        } else if (which == BUTTON_POSITIVE && !mIsStartAnimation) {
            if (mTimeSetListener != null) {
                mTimePicker.clearFocus();
                mTimeSetListener.onTimeSet(mTimePicker, mTimePicker.getHour(), mTimePicker.getMinute());
            }
            if (mImm != null) {
                mImm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
            }
            dismiss();
        }
    }

    @Override
    public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
    }

    public void updateTime(int hourOfDay, int minute) {
        mTimePicker.setHour(hourOfDay);
        mTimePicker.setMinute(minute);
    }

    @Override
    public Bundle onSaveInstanceState() {
        Bundle superState = super.onSaveInstanceState();
        superState.putInt(HOUR, mTimePicker.getHour());
        superState.putInt(MINUTE, mTimePicker.getMinute());
        superState.putBoolean(IS_24_HOUR, mTimePicker.is24HourView());
        return superState;
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        final int hourOfDay = savedInstanceState.getInt(HOUR);
        final int minute = savedInstanceState.getInt(MINUTE);
        mTimePicker.setIs24HourView(savedInstanceState.getBoolean(IS_24_HOUR));
        mTimePicker.setHour(hourOfDay);
        mTimePicker.setMinute(minute);
    }
}
