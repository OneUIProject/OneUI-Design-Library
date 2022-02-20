package de.dlyt.yanndroid.oneuiexample.tabs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

import de.dlyt.yanndroid.oneui.widget.DatePicker;
import de.dlyt.yanndroid.oneui.widget.NumberPicker;
import de.dlyt.yanndroid.oneui.widget.ProgressBar;
import de.dlyt.yanndroid.oneui.widget.SeekBar;
import de.dlyt.yanndroid.oneui.widget.Spinner;
import de.dlyt.yanndroid.oneui.widget.SpinningDatePicker;
import de.dlyt.yanndroid.oneui.widget.SwitchBar;
import de.dlyt.yanndroid.oneui.widget.TimePicker;
import de.dlyt.yanndroid.oneuiexample.MainActivity;
import de.dlyt.yanndroid.oneuiexample.R;

public class ViewsTab extends Fragment {

    private View mRootView;
    private MainActivity mActivity;

    public ViewsTab() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (MainActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_views_tab, container, false);
        return mRootView;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ProgressBar pgb1 = mRootView.findViewById(R.id.pgb1);
        ProgressBar pgb2 = mRootView.findViewById(R.id.pgb2);
        ProgressBar pgb3 = mRootView.findViewById(R.id.pgb3);
        pgb3.setMode(ProgressBar.MODE_CIRCLE);

        //SeekBar
        SeekBar seekBar1 = mRootView.findViewById(R.id.seekbar1);
        SeekBar seekBar2 = mRootView.findViewById(R.id.seekbar2);
        SeekBar seekBar3 = mRootView.findViewById(R.id.seekbar3);
        seekBar3.setSeamless(true);
        seekBar3.showTickMarkDots(true);

        seekBar1.setOverlapPointForDualColor(70);
        seekBar2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seslSeekBar, int i, boolean z) {
                pgb1.setProgress(i);
                pgb2.setProgress(i);
                pgb3.setProgress(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seslSeekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seslSeekBar) {

            }
        });

        //SwitchBar
        SwitchBar switchbar = mRootView.findViewById(R.id.switchBar);
        switchbar.addOnSwitchChangeListener((switchCompat, z) -> {
            switchbar.setEnabled(false);
            switchbar.setProgressBarVisible(true);

            new Handler().postDelayed(() -> {
                switchbar.setEnabled(true);
                switchbar.setProgressBarVisible(false);
            }, 700);
        });

        LinearLayout colorPaletteContainer = mRootView.findViewById(R.id.palette);
        colorPaletteContainer.setClipToOutline(true);

        //Spinner
        Spinner testSpinner = mRootView.findViewById(R.id.test_spinner);
        List<String> categories = new ArrayList<>();
        for (int i = 1; i < 16; i++) categories.add("Spinner Item " + i);
        ArrayAdapter<String> testAdapter = new ArrayAdapter<String>(mActivity, android.R.layout.simple_spinner_item, categories);
        testAdapter.setDropDownViewResource(R.layout.sesl_simple_spinner_dropdown_item);
        testSpinner.setAdapter(testAdapter);

        //Pickers
        setUpNumberPickers();
    }

    private void setUpNumberPickers() {
        LinearLayout numberPickersContainer = mRootView.findViewById(R.id.numberpickers_container);

        NumberPicker numberPickerOne = mRootView.findViewById(R.id.numberpicker1);
        numberPickerOne.setMinValue(1);
        numberPickerOne.setMaxValue(100);
        numberPickerOne.setValue(50);
        ((EditText) numberPickerOne.findViewById(R.id.numberpicker_input)).setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    numberPickerOne.setEditTextMode(false);
                }
                return false;
            }
        });

        NumberPicker numberPickerTwo = mRootView.findViewById(R.id.numberpicker2);
        numberPickerTwo.setTextTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
        numberPickerTwo.setTextSize(38);
        numberPickerTwo.setMinValue(0);
        numberPickerTwo.setMaxValue(10);
        numberPickerTwo.setValue(8);
        ((EditText) numberPickerTwo.findViewById(R.id.numberpicker_input)).setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    numberPickerTwo.setEditTextMode(false);
                }
                return false;
            }
        });

        NumberPicker numberPickerThree = mRootView.findViewById(R.id.numberpicker3);
        numberPickerThree.setTextTypeface(ResourcesCompat.getFont(getContext(), R.font.samsungsharpsans_bold));
        numberPickerThree.setMinValue(0);
        numberPickerThree.setMaxValue(2);
        numberPickerThree.setDisplayedValues(new String[]{"A", "B", "C"});
        ((EditText) numberPickerThree.findViewById(R.id.numberpicker_input)).setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    numberPickerThree.setEditTextMode(false);
                }
                return false;
            }
        });

        TimePicker timePicker = mRootView.findViewById(R.id.timepicker);

        DatePicker datePicker = mRootView.findViewById(R.id.datepicker);
        datePicker.setFirstDayOfWeek(2);
        datePicker.setMinDate(0);
        datePicker.setMaxDate(4133966209349L);

        SpinningDatePicker spinningDatePicker = mRootView.findViewById(R.id.spinningdatepicker);

        Spinner pickersSpinner = mRootView.findViewById(R.id.pickers_spinner);
        List<String> spinnersItems = new ArrayList<>();
        spinnersItems.add("NumberPicker");
        spinnersItems.add("TimePicker");
        spinnersItems.add("DatePicker");
        spinnersItems.add("SpinningDatePicker");
        ArrayAdapter<String> pickersAdapter = new ArrayAdapter<String>(mActivity, android.R.layout.simple_spinner_item, spinnersItems);
        pickersAdapter.setDropDownViewResource(R.layout.sesl_simple_spinner_dropdown_item);
        pickersSpinner.setAdapter(pickersAdapter);
        pickersSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                boolean numberPickerSel = false, timePickerSel = false, datePickerSel = false, spinningDateSel = false;

                switch (pos) {
                    case 0:
                        numberPickerSel = true;
                        break;
                    case 1:
                        timePickerSel = true;
                        break;
                    case 2:
                        datePickerSel = true;
                        break;
                    case 3:
                        spinningDateSel = true;
                        break;
                }

                numberPickersContainer.setVisibility(numberPickerSel ? View.VISIBLE : View.GONE);
                timePicker.setVisibility(timePickerSel ? View.VISIBLE : View.GONE);
                datePicker.setVisibility(datePickerSel ? View.VISIBLE : View.GONE);
                spinningDatePicker.setVisibility(spinningDateSel ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }
}