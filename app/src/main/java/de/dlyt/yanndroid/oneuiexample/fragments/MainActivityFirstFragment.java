package de.dlyt.yanndroid.oneuiexample.fragments;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.SeslViewPager;

import de.dlyt.yanndroid.oneui.view.TabLayout;
import de.dlyt.yanndroid.oneuiexample.R;
import de.dlyt.yanndroid.oneuiexample.tabs.ViewPagerAdapter;

public class MainActivityFirstFragment extends Fragment {

    private AppCompatActivity mActivity;
    private Context mContext;
    private View mRootView;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (AppCompatActivity) getActivity();
        mContext = mActivity.getApplicationContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_first, container, false);
        return mRootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // TabLayout and ViewPager
        TabLayout tabLayout = mRootView.findViewById(R.id.tabLayout);
        SeslViewPager viewPager = mRootView.findViewById(R.id.viewPager);
        viewPager.setAdapter(new ViewPagerAdapter(getChildFragmentManager()));
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.updateWidget();

        //Fullscreen
        init();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        init();
    }

    private void init() {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            mActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

}
