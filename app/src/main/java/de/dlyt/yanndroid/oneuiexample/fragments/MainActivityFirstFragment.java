package de.dlyt.yanndroid.oneuiexample.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import de.dlyt.yanndroid.oneui.sesl.tabs.TabLayoutMediator;
import de.dlyt.yanndroid.oneui.sesl.viewpager2.widget.SeslViewPager2;
import de.dlyt.yanndroid.oneui.view.ViewPager2;
import de.dlyt.yanndroid.oneui.widget.TabLayout;
import de.dlyt.yanndroid.oneuiexample.R;
import de.dlyt.yanndroid.oneuiexample.base.BaseThemeActivity;
import de.dlyt.yanndroid.oneuiexample.tabs.ViewPager2Adapter;

public class MainActivityFirstFragment extends Fragment {
    private BaseThemeActivity mActivity;
    private Context mContext;
    private View mRootView;

    @Override
    public void onAttach(@NonNull Context context) {
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
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getView().setBackgroundColor(getResources().getColor(mActivity.mUseOUI4Theme ? R.color.sesl4_round_and_bgcolor : R.color.sesl_round_and_bgcolor));

        // TabLayout and ViewPager
        FloatingActionButton fab = mActivity.findViewById(R.id.sesl_fab);
        TabLayout subTabs = mRootView.findViewById(R.id.sub_tabs);
        subTabs.seslSetSubTabStyle();
        ViewPager2 viewPager2 = mRootView.findViewById(R.id.viewPager2);
        viewPager2.setAdapter(new ViewPager2Adapter(this));
        viewPager2.registerOnPageChangeCallback(new SeslViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (position == 0 && positionOffset == 0) {
                    fab.show();
                } else {
                    fab.hide();
                }
            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    fab.show();
                } else {
                    fab.hide();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        TabLayoutMediator tlm = new TabLayoutMediator(subTabs, viewPager2, (tab, position) -> {
            String[] tabTitle = {"Views", "Icons", "Nothing"};
            tab.setText(tabTitle[position]);
        });
        tlm.attach();
    }

}
