package de.dlyt.yanndroid.oneui.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

import androidx.annotation.NonNull;

import de.dlyt.yanndroid.oneui.R;
import de.dlyt.yanndroid.oneui.widget.DatePicker;

public class DatePickerDialog extends AlertDialog implements DialogInterface.OnClickListener, DatePicker.OnDateChangedListener {
    private static final String DAY = "day";
    private static final String MONTH = "month";
    private static final String YEAR = "year";
    private final DatePicker mDatePicker;
    private final OnDateSetListener mDateSetListener;
    private InputMethodManager mImm;

    private final View.OnFocusChangeListener mBtnFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (mDatePicker.isEditTextMode() && hasFocus) {
                mDatePicker.setEditTextMode(false);
            }
        }
    };

    private final DatePicker.ValidationCallback mValidationCallback = new DatePicker.ValidationCallback() {
        @Override
        public void onValidationChanged(boolean valid) {
            Button positiveBtn = getButton(BUTTON_POSITIVE);
            if (positiveBtn != null) {
                positiveBtn.setEnabled(valid);
            }
        }
    };

    public interface OnDateSetListener {
        void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth);
    }

    public DatePickerDialog(Context context, OnDateSetListener listener, int year, int monthOfYear, int dayOfMonth) {
        this(context, 0, listener, year, monthOfYear, dayOfMonth);
    }

    public DatePickerDialog(Context context, int theme, OnDateSetListener listener, int year, int monthOfYear, int dayOfMonth) {
        super(context, resolveDialogTheme(context, theme));

        View view = LayoutInflater.from(getContext()).inflate(R.layout.sesl_date_picker_dialog, null);
        setView(view);
        setButton(BUTTON_POSITIVE, getContext().getString(R.string.sesl_picker_done), this);
        setButton(BUTTON_NEGATIVE, getContext().getString(R.string.sesl_picker_cancel), this);

        mDatePicker = view.findViewById(R.id.sesl_datePicker);
        mDatePicker.init(year, monthOfYear, dayOfMonth, this);
        mDatePicker.setValidationCallback(mValidationCallback);
        mDatePicker.setDialogWindow(getWindow());
        mDatePicker.setDialogPaddingVertical(view.getPaddingTop() + view.getPaddingBottom());
        mDateSetListener = listener;

        mImm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    static int resolveDialogTheme(@NonNull Context context, int theme) {
        if (theme != 0) {
            return theme;
        }
        return context.getTheme().obtainStyledAttributes(new int[]{R.attr.isOneUI4}).getBoolean(0, false) ? R.style.OneUI4_PickerDialogStyle : R.style.PickerDialogStyle;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getButton(BUTTON_POSITIVE).setOnFocusChangeListener(mBtnFocusChangeListener);
        getButton(BUTTON_NEGATIVE).setOnFocusChangeListener(mBtnFocusChangeListener);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (mImm != null) {
            mImm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
        }
        if (which == BUTTON_NEGATIVE) {
            cancel();
        } else if (which == BUTTON_POSITIVE && mDateSetListener != null) {
            mDatePicker.clearFocus();
            mDateSetListener.onDateSet(mDatePicker, mDatePicker.getYear(), mDatePicker.getMonth(), mDatePicker.getDayOfMonth());
        }
    }

    @Override
    public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
    }

    public DatePicker getDatePicker() {
        return mDatePicker;
    }

    public void updateDate(int year, int month, int dayOfMonth) {
        mDatePicker.updateDate(year, month, dayOfMonth);
    }

    @Override
    public Bundle onSaveInstanceState() {
        Bundle superState = super.onSaveInstanceState();
        superState.putInt(YEAR, mDatePicker.getYear());
        superState.putInt(MONTH, mDatePicker.getMonth());
        superState.putInt(DAY, mDatePicker.getDayOfMonth());
        return superState;
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mDatePicker.init(savedInstanceState.getInt(YEAR), savedInstanceState.getInt(MONTH), savedInstanceState.getInt(DAY), this);
    }
}
