package de.dlyt.yanndroid.oneui.dialog;

import android.content.Context;

import java.util.Calendar;
import java.util.TimeZone;

import de.dlyt.yanndroid.oneui.sesl.picker.app.SeslDatePickerDialog;

public class DatePickerDialog extends SeslDatePickerDialog {

    public DatePickerDialog(Context context, OnDateSetListener onDateSetListener) {
        this(context, onDateSetListener, Calendar.getInstance(TimeZone.getDefault()).get(1), Calendar.getInstance(TimeZone.getDefault()).get(2), Calendar.getInstance(TimeZone.getDefault()).get(5));
    }

    public DatePickerDialog(Context context, OnDateSetListener onDateSetListener, int year, int month, int day) {
        super(context, onDateSetListener, year, month, day);
    }
}
