package de.dlyt.yanndroid.oneuiexample.tabs;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import de.dlyt.yanndroid.oneui.SwitchBar;
import de.dlyt.yanndroid.oneui.TabLayout;
import de.dlyt.yanndroid.oneui.drawer.OptionButton;
import de.dlyt.yanndroid.oneui.layout.DrawerLayout;
import de.dlyt.yanndroid.oneui.layout.ToolbarLayout;
import de.dlyt.yanndroid.oneuiexample.AboutActivity;
import de.dlyt.yanndroid.oneuiexample.R;
import de.dlyt.yanndroid.oneuiexample.utils.BaseTabFragment;

public class MainActivityFirstFragment extends BaseTabFragment {
    Integer[] imageIDs = {R.drawable.ic_samsung_arrow_down, R.drawable.ic_samsung_arrow_left, R.drawable.ic_samsung_arrow_right, R.drawable.ic_samsung_arrow_up, R.drawable.ic_samsung_attach, R.drawable.ic_samsung_audio, R.drawable.ic_samsung_back, R.drawable.ic_samsung_book, R.drawable.ic_samsung_bookmark, R.drawable.ic_samsung_brush, R.drawable.ic_samsung_camera, R.drawable.ic_samsung_close, R.drawable.ic_samsung_convert, R.drawable.ic_samsung_copy, R.drawable.ic_samsung_delete, R.drawable.ic_samsung_document, R.drawable.ic_samsung_download, R.drawable.ic_samsung_drawer, R.drawable.ic_samsung_edit, R.drawable.ic_samsung_equalizer, R.drawable.ic_samsung_favorite, R.drawable.ic_samsung_group, R.drawable.ic_samsung_help, R.drawable.ic_samsung_image, R.drawable.ic_samsung_image_2, R.drawable.ic_samsung_import, R.drawable.ic_samsung_info, R.drawable.ic_samsung_keyboard, R.drawable.ic_samsung_lock, R.drawable.ic_samsung_mail, R.drawable.ic_samsung_maximize, R.drawable.ic_samsung_minimize, R.drawable.ic_samsung_minus, R.drawable.ic_samsung_more, R.drawable.ic_samsung_move, R.drawable.ic_samsung_mute, R.drawable.ic_samsung_page, R.drawable.ic_samsung_pause, R.drawable.ic_samsung_pdf, R.drawable.ic_samsung_pen, R.drawable.ic_samsung_pen_calligraphy, R.drawable.ic_samsung_pen_calligraphy_brush, R.drawable.ic_samsung_pen_eraser, R.drawable.ic_samsung_pen_fountain, R.drawable.ic_samsung_pen_marker, R.drawable.ic_samsung_pen_marker_round, R.drawable.ic_samsung_pen_pencil, R.drawable.ic_samsung_play, R.drawable.ic_samsung_plus, R.drawable.ic_samsung_rectify, R.drawable.ic_samsung_redo, R.drawable.ic_samsung_remind, R.drawable.ic_samsung_rename, R.drawable.ic_samsung_reorder, R.drawable.ic_samsung_restore, R.drawable.ic_samsung_save, R.drawable.ic_samsung_scan, R.drawable.ic_samsung_search, R.drawable.ic_samsung_selected, R.drawable.ic_samsung_send, R.drawable.ic_samsung_settings, R.drawable.ic_samsung_share, R.drawable.ic_samsung_shuffle, R.drawable.ic_samsung_smart_view, R.drawable.ic_samsung_stop, R.drawable.ic_samsung_tag, R.drawable.ic_samsung_text, R.drawable.ic_samsung_text_2, R.drawable.ic_samsung_time, R.drawable.ic_samsung_undo, R.drawable.ic_samsung_unlock, R.drawable.ic_samsung_voice, R.drawable.ic_samsung_volume, R.drawable.ic_samsung_warning, R.drawable.ic_samsung_web_search};

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
        toolbarLayout.setNavigationButtonBadge(ToolbarLayout.N_BADGE);
        toolbarLayout.setMoreMenuButton(getMoreMenuButtonList(),
                (adapterView, view2, i, j) -> {
                    toolbarLayout.dismissMoreMenuPopupWindow();
                });

        //Library Demo
        demo();

        //Icons
        GridView images = mRootView.findViewById(R.id.images);
        images.setAdapter(new ImageAdapter(mActivity));

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
    public void onTabSelected() { }

    @Override
    public void onTabUnselected() { }


    private void init() {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            mActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    public void demo() {

        // TabLayout
        TabLayout tabLayout = mRootView.findViewById(R.id.tabLayout);
        String[] tabsName = {"Menu", "Big menu", "Very big menu"};
        for (String s: tabsName) {
            tabLayout.addTab(tabLayout.newTab().setText(s));
        }
        tabLayout.updateWidget();


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
        AppCompatSpinner spinner = mRootView.findViewById(R.id.spinner);
        List<String> categories = new ArrayList<String>();
        for (int i = 1; i < 16; i++) categories.add("Spinner Item " + i);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(mActivity, android.R.layout.simple_spinner_item, categories);
        dataAdapter.setDropDownViewResource(R.layout.sesl_simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);


        //OptionButton
        OptionButton optionButton = mRootView.findViewById(R.id.ob_help);
        optionButton.setButtonEnabled(false);

    }

    private LinkedHashMap<String, Integer> getMoreMenuButtonList() {
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        linkedHashMap.put("Menu Item 1", 0);
        linkedHashMap.put("Menu Item 2", 87);
        linkedHashMap.put("Menu Item 3", ToolbarLayout.N_BADGE);
        return linkedHashMap;
    }


    //Adapter for the Icon GridView
    public class ImageAdapter extends BaseAdapter {
        private Context mContext;

        public ImageAdapter(Context c) {
            mContext = c;
        }

        public int getCount() {
            return imageIDs.length;
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView mImageView;
            if (convertView == null) {
                mImageView = new ImageView(mContext);
                mImageView.setLayoutParams(new GridView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                mImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            } else {
                mImageView = (ImageView) convertView;
            }
            mImageView.setImageResource(imageIDs[position]);
            return mImageView;
        }
    }
}
