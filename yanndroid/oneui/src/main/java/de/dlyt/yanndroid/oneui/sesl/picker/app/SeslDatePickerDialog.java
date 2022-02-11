package de.dlyt.yanndroid.oneui.sesl.picker.app;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

import java.util.Calendar;
import java.util.TimeZone;

import de.dlyt.yanndroid.oneui.R;
import de.dlyt.yanndroid.oneui.dialog.AlertDialog;
import de.dlyt.yanndroid.oneui.sesl.picker.widget.SeslDatePicker;

public class SeslDatePickerDialog extends AlertDialog implements DialogInterface.OnClickListener, SeslDatePicker.OnDateChangedListener {
    public final View.OnFocusChangeListener mBtnFocusChangeListener = new View.OnFocusChangeListener() {

        public void onFocusChange(View view, boolean z) {
            if (SeslDatePickerDialog.this.mDatePicker.isEditTextMode() && z) {
                SeslDatePickerDialog.this.mDatePicker.setEditTextMode(false);
            }
        }
    };
    public final SeslDatePicker mDatePicker;
    public final OnDateSetListener mDateSetListener;
    public InputMethodManager mImm;
    public final SeslDatePicker.ValidationCallback mValidationCallback;

    public interface OnDateSetListener {
        void onDateSet(SeslDatePicker seslDatePicker, int year, int month, int day);
    }

    @Override // androidx.picker.widget.SeslDatePicker.OnDateChangedListener
    public void onDateChanged(SeslDatePicker seslDatePicker, int i, int i2, int i3) {
    }

    public SeslDatePickerDialog(Context context, OnDateSetListener onDateSetListener, int year, int month, int day) {
        super(context);
        SeslDatePicker.ValidationCallback r4 = new SeslDatePicker.ValidationCallback() {

            @Override // androidx.picker.widget.SeslDatePicker.ValidationCallback
            public void onValidationChanged(boolean z) {
                Button button = SeslDatePickerDialog.this.getButton(-1);
                if (button != null) {
                    button.setEnabled(z);
                }
            }
        };
        this.mValidationCallback = r4;
        Context context2 = getContext();
        View inflate = LayoutInflater.from(context2).inflate(R.layout.sesl_date_picker_dialog, (ViewGroup) null);
        setView(inflate);
        setButton(-1, context2.getString(R.string.sesl_picker_done), this);
        setButton(-2, context2.getString(R.string.sesl_picker_cancel), this);
        SeslDatePicker seslDatePicker = (SeslDatePicker) inflate.findViewById(R.id.sesl_datePicker);
        this.mDatePicker = seslDatePicker;
        seslDatePicker.init(year, month, day, this);
        seslDatePicker.setValidationCallback(r4);
        seslDatePicker.setDialogWindow(getWindow());
        seslDatePicker.setDialogPaddingVertical(inflate.getPaddingTop() + inflate.getPaddingBottom());
        this.mDateSetListener = onDateSetListener;
        this.mImm = (InputMethodManager) context2.getSystemService("input_method");
    }

    @Override // androidx.appcompat.app.AlertDialog, androidx.appcompat.app.AppCompatDialog
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        getButton(-1).setOnFocusChangeListener(this.mBtnFocusChangeListener);
        getButton(-2).setOnFocusChangeListener(this.mBtnFocusChangeListener);
    }

    public void onClick(DialogInterface dialogInterface, int i) {
        InputMethodManager inputMethodManager = this.mImm;
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
        }
        if (i == -2) {
            cancel();
        } else if (i == -1 && this.mDateSetListener != null) {
            this.mDatePicker.clearFocus();
            OnDateSetListener onDateSetListener = this.mDateSetListener;
            SeslDatePicker seslDatePicker = this.mDatePicker;
            onDateSetListener.onDateSet(seslDatePicker, seslDatePicker.getYear(), this.mDatePicker.getMonth(), this.mDatePicker.getDayOfMonth());
        }
    }

    public SeslDatePicker getDatePicker() {
        return this.mDatePicker;
    }

    public void updateDate(int i, int i2, int i3) {
        this.mDatePicker.updateDate(i, i2, i3);
    }

    public Bundle onSaveInstanceState() {
        Bundle onSaveInstanceState = super.onSaveInstanceState();
        onSaveInstanceState.putInt("year", this.mDatePicker.getYear());
        onSaveInstanceState.putInt("month", this.mDatePicker.getMonth());
        onSaveInstanceState.putInt("day", this.mDatePicker.getDayOfMonth());
        return onSaveInstanceState;
    }

    public void onRestoreInstanceState(Bundle bundle) {
        super.onRestoreInstanceState(bundle);
        this.mDatePicker.init(bundle.getInt("year"), bundle.getInt("month"), bundle.getInt("day"), this);
    }
}
