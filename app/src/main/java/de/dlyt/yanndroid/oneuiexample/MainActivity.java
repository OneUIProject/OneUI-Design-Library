package de.dlyt.yanndroid.oneuiexample;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.util.SeslMisc;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.LinkedHashMap;

import de.dlyt.yanndroid.oneui.BottomNavigationView;
import de.dlyt.yanndroid.oneui.ClassicColorPickerDialog;
import de.dlyt.yanndroid.oneui.DetailedColorPickerDialog;
import de.dlyt.yanndroid.oneui.ThemeColor;
import de.dlyt.yanndroid.oneui.dialog.ProgressDialog;
import de.dlyt.yanndroid.oneui.dialog.SamsungAlertDialog;
import de.dlyt.yanndroid.oneui.layout.DrawerLayout;
import de.dlyt.yanndroid.oneui.layout.ToolbarLayout;
import de.dlyt.yanndroid.oneui.snackbar.Snackbar;
import de.dlyt.yanndroid.oneui.utils.ReflectUtils;
import de.dlyt.yanndroid.oneuiexample.utils.BaseTabFragment;
import de.dlyt.yanndroid.oneuiexample.utils.TabsManager;

import static de.dlyt.yanndroid.oneui.layout.DrawerLayout.DRAWER_LAYOUT;

public class MainActivity extends AppCompatActivity {
    private String[] mTabsTagName;
    private String[] mTabsTitleName;
    private String[] mTabsClassName;

    private boolean mIsLightTheme;
    private String sharedPrefName;

    private Context mContext;
    private FragmentManager mFragmentManager;
    private BaseTabFragment mFragment;
    private TabsManager mTabsManager;

    private DrawerLayout drawerLayout;
    private ToolbarLayout toolbarLayout;
    private BottomNavigationView bnvLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        new ThemeColor(this);
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_main);
        init();
    }

    @Override
    public void attachBaseContext(Context context) {
        // pre-OneUI
        if (Build.VERSION.SDK_INT <= 28) {
            super.attachBaseContext(ThemeColor.createDarkModeContextWrapper(context));
        } else
            super.attachBaseContext(context);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // pre-OneUI
        if (Build.VERSION.SDK_INT <= 28) {
            Resources res = getResources();
            res.getConfiguration().setTo(ThemeColor.createDarkModeConfig(mContext, newConfig));
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (bnvLayout != null) {
            bnvLayout.setResumeStatus(false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (bnvLayout != null) {
            bnvLayout.setResumeStatus(true);
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        View focusedTab = bnvLayout.getFocusedChild();
        int keyCode = event.getKeyCode();

        if (mFragment == null || !mFragment.isResumed()) {
            return super.dispatchKeyEvent(event);
        } else if (focusedTab == null) {
            if (mFragment.onDispatchKeyEvent(event, null) || super.dispatchKeyEvent(event)) {
                return true;
            }
            return false;
        } else if (keyCode != KeyEvent.KEYCODE_N && keyCode != KeyEvent.KEYCODE_A && keyCode != KeyEvent.KEYCODE_D && keyCode != KeyEvent.KEYCODE_FORWARD_DEL) {
            return super.dispatchKeyEvent(event);
        } else {
            if (mFragment.onDispatchKeyEvent(event, focusedTab) || super.dispatchKeyEvent(event)) {
                return true;
            }
            return false;
        }
    }

    @Override
    public void onMultiWindowModeChanged(boolean isMultiWindowMode) {
        super.onMultiWindowModeChanged(isMultiWindowMode);
        for (String tabName : mTabsTagName) {
            BaseTabFragment fragment = (BaseTabFragment) mFragmentManager.findFragmentByTag(tabName);
            if (fragment != null) fragment.onMultiWindowModeChanged(isMultiWindowMode);
        }
    }

    private void init() {
        ReflectUtils.genericInvokeMethod(getWindow().getDecorView(), "semSetRoundedCorners", 0);

        mIsLightTheme = SeslMisc.isLightTheme(mContext);

        drawerLayout = findViewById(R.id.drawer_view);
        toolbarLayout = (ToolbarLayout) drawerLayout.getView(DrawerLayout.TOOLBAR);
        bnvLayout = findViewById(R.id.main_samsung_tabs);

        sharedPrefName = "mainactivity_tabs";
        mTabsTagName = getResources().getStringArray(R.array.mainactivity_tab_tag);
        mTabsTitleName = getResources().getStringArray(R.array.mainactivity_tab_title);
        mTabsClassName = getResources().getStringArray(R.array.mainactivity_tab_class);

        mTabsManager = new TabsManager(mContext, sharedPrefName);
        mTabsManager.initTabPosition();

        mFragmentManager = getSupportFragmentManager();

        //DrawerLayout
        DrawerLayout drawerLayout = findViewById(R.id.drawer_view);
        setSupportActionBar(drawerLayout.getToolbar());
        drawerLayout.setDrawerIconOnClickListener(v -> startActivity(new Intent().setClass(mContext, AboutActivity.class)));

        drawerLayout.setButtonBadges(ToolbarLayout.N_BADGE, DrawerLayout.N_BADGE);
        drawerLayout.setDrawerButtonTooltip(getText(R.string.app_info));

        ToolbarLayout toolbarLayout = (ToolbarLayout) drawerLayout.getView(DrawerLayout.TOOLBAR);

        toolbarLayout.getAppBarLayout().addOnOffsetChangedListener((layout, verticalOffset) -> {
            int totalScrollRange = layout.getTotalScrollRange();
            int inputMethodWindowVisibleHeight = (int) ReflectUtils.genericInvokeMethod(InputMethodManager.class, mContext.getSystemService(INPUT_METHOD_SERVICE), "getInputMethodWindowVisibleHeight");
            LinearLayout nothingLayout = findViewById(R.id.nothing_layout);
            if (nothingLayout != null) {
                if (totalScrollRange != 0) {
                    nothingLayout.setTranslationY(((float) (Math.abs(verticalOffset) - totalScrollRange)) / 2.0f);
                } else {
                    nothingLayout.setTranslationY(((float) (Math.abs(verticalOffset) - inputMethodWindowVisibleHeight)) / 2.0f);
                }
            }
        });

        toolbarLayout.addOverflowButton(false,
                R.drawable.ic_samsung_info,
                R.string.app_info,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(new Intent().setClass(mContext, AboutActivity.class));
                    }
                });
        toolbarLayout.setMoreMenuButton(getMoreMenuButtonList(),
                (adapterView, view2, i, j) -> {
                    toolbarLayout.dismissMoreMenuPopupWindow();

                    String obj = adapterView.getAdapter().getItem(i).toString();
                    if (obj.equals("Set dark theme")) {
                        ThemeColor.setDarkMode(this, ThemeColor.DARK_MODE_ENABLED);
                    }
                    if (obj.equals("Set light theme")) {
                        ThemeColor.setDarkMode(this, ThemeColor.DARK_MODE_DISABLED);
                    }
                });

        //BottomNavigationLayout
        for (String s : mTabsTitleName) {
            bnvLayout.addTab(bnvLayout.newTab().setText(s));
        }

        bnvLayout.addOnTabSelectedListener(new BottomNavigationView.OnTabSelectedListener() {
            public void onTabSelected(BottomNavigationView.Tab tab) {
                int tabPosition = tab.getPosition();
                mTabsManager.setTabPosition(tabPosition);
                setCurrentItem();
                BaseTabFragment fragment = (BaseTabFragment) mFragmentManager.findFragmentByTag(mTabsTagName[tabPosition]);
                if (fragment != null) fragment.onTabSelected();
            }

            public void onTabUnselected(BottomNavigationView.Tab tab) {
                int tabPosition = tab.getPosition();
                BaseTabFragment fragment = (BaseTabFragment) mFragmentManager.findFragmentByTag(mTabsTagName[tabPosition]);
                if (fragment != null) fragment.onTabUnselected();
            }

            public void onTabReselected(BottomNavigationView.Tab tab) {
            }
        });
        bnvLayout.updateWidget(this);
        setCurrentItem();
    }

    private void setCurrentItem() {
        if (bnvLayout.isEnabled()) {
            int tabPosition = mTabsManager.getCurrentTab();
            BottomNavigationView.Tab tab = bnvLayout.getTabAt(tabPosition);
            if (tab != null) {
                tab.select();
                setFragment(tabPosition);

                if (tabPosition == 0) {
                    // MainActivityFirstFragment
                    toolbarLayout.setSubtitle("Design");
                    toolbarLayout.setNavigationButtonVisible(true);
                    ((androidx.drawerlayout.widget.DrawerLayout) drawerLayout.getView(DRAWER_LAYOUT)).setDrawerLockMode(androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_UNLOCKED);
                } else {
                    // MainActivitySecondFragment
                    toolbarLayout.setSubtitle("Preferences");
                    toolbarLayout.setNavigationButtonVisible(false);
                    ((androidx.drawerlayout.widget.DrawerLayout) drawerLayout.getView(DRAWER_LAYOUT)).setDrawerLockMode(androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                }

            }
        }
    }

    private void setFragment(int tabPosition) {
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        String tabName = mTabsTagName[tabPosition];
        Fragment fragment = mFragmentManager.findFragmentByTag(tabName);
        if (mFragment != null) {
            transaction.hide(mFragment);
        }
        if (fragment != null) {
            mFragment = (BaseTabFragment) fragment;
            transaction.show(fragment);
        } else {
            try {
                mFragment = (BaseTabFragment) Class.forName(mTabsClassName[tabPosition]).newInstance();
            } catch (IllegalAccessException | InstantiationException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            transaction.add(R.id.fragment_container, mFragment, tabName);
        }
        transaction.commit();
    }

    // onClick
    public void classicColorPickerDialog(View view) {
        ClassicColorPickerDialog mClassicColorPickerDialog;
        SharedPreferences sharedPreferences = getSharedPreferences("ThemeColor", Context.MODE_PRIVATE);
        String stringColor = sharedPreferences.getString("color", "0381fe");

        int currentColor = Color.parseColor("#" + stringColor);

        try {
            mClassicColorPickerDialog = new ClassicColorPickerDialog(this,
                    new ClassicColorPickerDialog.ColorPickerChangedListener() {
                        @Override
                        public void onColorChanged(int i) {
                            if (currentColor != i)
                                ThemeColor.setColor(MainActivity.this, Color.red(i), Color.green(i), Color.blue(i));
                        }
                    },
                    currentColor);
            mClassicColorPickerDialog.show();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    public void detailedColorPickerDialog(View view) {
        DetailedColorPickerDialog mDetailedColorPickerDialog;
        SharedPreferences sharedPreferences = getSharedPreferences("ThemeColor", Context.MODE_PRIVATE);
        String stringColor = sharedPreferences.getString("color", "0381fe");

        float[] currentColor = new float[3];
        Color.colorToHSV(Color.parseColor("#" + stringColor), currentColor);

        mDetailedColorPickerDialog = new DetailedColorPickerDialog(this, 2, currentColor);
        mDetailedColorPickerDialog.setColorPickerChangeListener(new DetailedColorPickerDialog.ColorPickerChangedListener() {
            @Override
            public void onColorChanged(int i, float[] fArr) {
                if (!(fArr[0] == currentColor[0] && fArr[1] == currentColor[1] && fArr[2] == currentColor[2]))
                    ThemeColor.setColor(MainActivity.this, fArr);
            }

            @Override
            public void onViewModeChanged(int i) {

            }
        });
        mDetailedColorPickerDialog.show();
    }

    public void standardDialog(View view) {
        new SamsungAlertDialog.Builder(this)
                .setTitle("Title")
                .setMessage("Message")
                .setNeutralButton("Maybe", null)
                .setNegativeButton("No", null)
                .setPositiveButton("Yes", null)
                .show();
    }

    public void singleChoiceDialog(View view) {
        CharSequence[] charSequences = {"Choice1", "Choice2", "Choice3"};
        new SamsungAlertDialog.Builder(this)
                .setTitle("Title")
                .setNeutralButton("Maybe", null)
                .setNegativeButton("No", null)
                .setPositiveButton("Yes", null)
                .setSingleChoiceItems(charSequences, 0, null)
                .show();
    }

    public void multiChoiceDialog(View view) {
        CharSequence[] charSequences = {"Choice1", "Choice2", "Choice3"};
        boolean[] booleans = {true, false, true};
        new SamsungAlertDialog.Builder(this)
                .setTitle("Title")
                .setNeutralButton("Maybe", null)
                .setNegativeButton("No", null)
                .setPositiveButton("Yes", null)
                .setMultiChoiceItems(charSequences, booleans, null)
                .show();
    }

    public void progressDialog(View view) {
        ProgressDialog dialog = new ProgressDialog(mContext);
        dialog.setIndeterminate(true);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setTitle("Title");
        dialog.setMessage("ProgressDialog");
        dialog.setButton(ProgressDialog.BUTTON_NEGATIVE, "Cancel", (dialog1, which) -> progressDialogCircleOnly(view));
        dialog.setOnCancelListener(dialog12 -> progressDialogCircleOnly(view));
        dialog.show();
    }

    private void progressDialogCircleOnly(View view) {
        ProgressDialog dialog = new ProgressDialog(mContext);
        dialog.setProgressStyle(ProgressDialog.STYLE_CIRCLE_ONLY);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setOnCancelListener(dialog1 -> Snackbar.make(view, "Text label", Snackbar.LENGTH_SHORT).setAction("Action", new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        }).show());
        dialog.show();
    }


    private LinkedHashMap<String, Integer> getMoreMenuButtonList() {
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        // pre-OneUI
        if (Build.VERSION.SDK_INT <= 28) {
            linkedHashMap.put(mIsLightTheme ? "Set dark theme" : "Set light theme", 0);
        } else {
            linkedHashMap.put("Menu Item 1", 0);
            linkedHashMap.put("Menu Item 2", 87);
            linkedHashMap.put("Menu Item 3", ToolbarLayout.N_BADGE);
        }
        return linkedHashMap;
    }
}
