package de.dlyt.yanndroid.oneuiexample.tabs;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import de.dlyt.yanndroid.oneui.RoundFrameLayout;
import de.dlyt.yanndroid.oneui.layout.DrawerLayout;
import de.dlyt.yanndroid.oneui.preference.SeslPreferenceFragmentCompat;
import de.dlyt.yanndroid.oneuiexample.AboutActivity;
import de.dlyt.yanndroid.oneuiexample.R;
import de.dlyt.yanndroid.oneuiexample.fragments.InnerPreferenceFragment;
import de.dlyt.yanndroid.oneuiexample.utils.BaseTabFragment;

public class MainActivitySecondFragment extends BaseTabFragment {
    private AppCompatActivity mActivity;
    private Context mContext;
    private View mRootView;

    private SeslPreferenceFragmentCompat mFragment;
    private FragmentManager mFragmentManager;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (AppCompatActivity) context;
        mContext = mActivity.getApplicationContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_second, container, false);
        return mRootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        init();
    }

    @Override
    public boolean onDispatchKeyEvent(KeyEvent keyEvent, View view) {
        return false;
    }

    @Override
    public void onTabSelected() { }

    @Override
    public void onTabUnselected() { }


    private void init() {
        mFragmentManager = getChildFragmentManager();
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        Fragment fragment = mFragmentManager.findFragmentByTag("root");
        if (mFragment != null) {
            transaction.hide(mFragment);
        }
        if (fragment != null) {
            mFragment = (SeslPreferenceFragmentCompat) fragment;
            transaction.show(fragment);
        } else {
            mFragment = new InnerPreferenceFragment();
            transaction.add(R.id.preference_fragment_container, mFragment, "root");
        }
        transaction.commit();
        mFragmentManager.executePendingTransactions();
    }
}
