package de.dlyt.yanndroid.oneuiexample.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import de.dlyt.yanndroid.oneui.widget.TabLayout;
import de.dlyt.yanndroid.oneui.view.ViewPager;
import de.dlyt.yanndroid.oneuiexample.R;
import de.dlyt.yanndroid.oneuiexample.base.BaseThemeActivity;
import de.dlyt.yanndroid.oneuiexample.tabs.ViewPagerAdapter;

public class MainActivityFirstFragment extends Fragment {

    private BaseThemeActivity mActivity;
    private Context mContext;
    private View mRootView;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (BaseThemeActivity) getActivity();
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

        getView().setBackgroundColor(getResources().getColor(mActivity.mUseOUI4Theme ? R.color.sesl4_round_and_bgcolor : R.color.sesl_round_and_bgcolor));

        // TabLayout and ViewPager
        TabLayout tabLayout = mRootView.findViewById(R.id.tabLayout);
        ViewPager viewPager = mRootView.findViewById(R.id.viewPager);
        viewPager.setAdapter(new ViewPagerAdapter(getChildFragmentManager()));
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.updateWidget();
    }

}
