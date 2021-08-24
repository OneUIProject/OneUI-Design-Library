package de.dlyt.yanndroid.oneuiexample.utils;

import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

import androidx.fragment.app.Fragment;

public abstract class BaseTabFragment extends Fragment {
    protected Context mContext;

    public abstract boolean onDispatchKeyEvent(KeyEvent keyEvent, View view);

    public abstract void onTabSelected();

    public abstract void onTabUnselected();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
    }

    public void onMultiWindowModeChanged(boolean isMultiWindowMode) {
    }

}
