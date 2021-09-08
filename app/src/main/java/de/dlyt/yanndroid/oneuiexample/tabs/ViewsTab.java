package de.dlyt.yanndroid.oneuiexample.tabs;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SeslSpinner;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

import de.dlyt.yanndroid.oneui.view.SeekBar;
import de.dlyt.yanndroid.oneui.view.SwitchBar;
import de.dlyt.yanndroid.oneuiexample.R;

public class ViewsTab extends Fragment {

    private View mRootView;
    private AppCompatActivity mActivity;

    public ViewsTab() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (AppCompatActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_views_tab, container, false);
        return mRootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //SeekBar
        SeekBar seekBar1 = mRootView.findViewById(R.id.seekbar1);
        SeekBar seekBar2 = mRootView.findViewById(R.id.seekbar2);
        seekBar1.setOverlapPointForDualColor(70);
        seekBar2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seslSeekBar, int i, boolean z) {
                seekBar1.setSecondaryProgress(i);
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


        //Spinner
        SeslSpinner spinner = mRootView.findViewById(R.id.spinner);
        List<String> categories = new ArrayList<String>();
        for (int i = 1; i < 16; i++) categories.add("Spinner Item " + i);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(mActivity, android.R.layout.simple_spinner_item, categories);
        dataAdapter.setDropDownViewResource(R.layout.sesl_simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);

    }

}