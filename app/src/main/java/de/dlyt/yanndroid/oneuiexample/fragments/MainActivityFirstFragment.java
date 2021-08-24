package de.dlyt.yanndroid.oneuiexample.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.SeslViewPager;

import java.util.LinkedHashMap;

import de.dlyt.yanndroid.oneui.TabLayout;
import de.dlyt.yanndroid.oneui.drawer.OptionButton;
import de.dlyt.yanndroid.oneui.layout.DrawerLayout;
import de.dlyt.yanndroid.oneui.layout.ToolbarLayout;
import de.dlyt.yanndroid.oneuiexample.AboutActivity;
import de.dlyt.yanndroid.oneuiexample.R;
import de.dlyt.yanndroid.oneuiexample.tabs.ViewPagerAdapter;
import de.dlyt.yanndroid.oneuiexample.utils.BaseTabFragment;

public class MainActivityFirstFragment extends BaseTabFragment {


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

        //DrawerLayout
        DrawerLayout drawerLayout = mRootView.findViewById(R.id.drawer_view);
        mActivity.setSupportActionBar(drawerLayout.getToolbar());
        drawerLayout.setDrawerIconOnClickListener(v -> startActivity(new Intent().setClass(getContext(), AboutActivity.class)));

        drawerLayout.setButtonBadges(ToolbarLayout.N_BADGE, DrawerLayout.N_BADGE);
        drawerLayout.setDrawerButtonTooltip(getText(R.string.app_info));

        ToolbarLayout toolbarLayout = (ToolbarLayout) drawerLayout.getView(DrawerLayout.TOOLBAR);
        toolbarLayout.addOverflowButton(false,
                R.drawable.ic_samsung_info,
                R.string.app_info,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(new Intent().setClass(getContext(), AboutActivity.class));
                    }
                });
        toolbarLayout.setMoreMenuButton(getMoreMenuButtonList(),
                (adapterView, view2, i, j) -> {
                    toolbarLayout.dismissMoreMenuPopupWindow();
                });


        // TabLayout and ViewPager
        TabLayout tabLayout = mRootView.findViewById(R.id.tabLayout);
        SeslViewPager viewPager = mRootView.findViewById(R.id.viewPager);
        viewPager.setAdapter(new ViewPagerAdapter(mActivity.getSupportFragmentManager()));
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.updateWidget();

        //OptionButton
        OptionButton optionButton = mRootView.findViewById(R.id.ob_help);
        optionButton.setButtonEnabled(false);

        //Fullscreen
        init();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        init();
    }

    @Override
    public boolean onDispatchKeyEvent(KeyEvent keyEvent, View view) {
        return false;
    }

    @Override
    public void onTabSelected() {
    }

    @Override
    public void onTabUnselected() {
    }


    private void init() {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            mActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    private LinkedHashMap<String, Integer> getMoreMenuButtonList() {
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        linkedHashMap.put("Menu Item 1", 0);
        linkedHashMap.put("Menu Item 2", 87);
        linkedHashMap.put("Menu Item 3", ToolbarLayout.N_BADGE);
        return linkedHashMap;
    }

}
