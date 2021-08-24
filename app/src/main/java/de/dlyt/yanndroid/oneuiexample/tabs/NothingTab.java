package de.dlyt.yanndroid.oneuiexample.tabs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import de.dlyt.yanndroid.oneuiexample.R;

public class NothingTab extends Fragment {

    public NothingTab() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_nothing_tab, container, false);
    }

}