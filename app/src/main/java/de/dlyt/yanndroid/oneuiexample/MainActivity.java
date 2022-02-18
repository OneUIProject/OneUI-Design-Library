package de.dlyt.yanndroid.oneuiexample;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import de.dlyt.yanndroid.oneui.dialog.AlertDialog;
import de.dlyt.yanndroid.oneui.dialog.ClassicColorPickerDialog;
import de.dlyt.yanndroid.oneui.dialog.DatePickerDialog;
import de.dlyt.yanndroid.oneui.dialog.DetailedColorPickerDialog;
import de.dlyt.yanndroid.oneui.dialog.ProgressDialog;
import de.dlyt.yanndroid.oneui.dialog.TimePickerDialog;
import de.dlyt.yanndroid.oneui.layout.DrawerLayout;
import de.dlyt.yanndroid.oneui.layout.ToolbarLayout;
import de.dlyt.yanndroid.oneui.menu.MenuItem;
import de.dlyt.yanndroid.oneui.menu.PopupMenu;
import de.dlyt.yanndroid.oneui.sesl.support.ViewSupport;
import de.dlyt.yanndroid.oneui.sesl.tabs.SamsungTabLayout;
import de.dlyt.yanndroid.oneui.sesl.utils.ReflectUtils;
import de.dlyt.yanndroid.oneui.utils.CustomButtonClickListener;
import de.dlyt.yanndroid.oneui.utils.OnSingleClickListener;
import de.dlyt.yanndroid.oneui.utils.ThemeUtil;
import de.dlyt.yanndroid.oneui.view.TipPopup;
import de.dlyt.yanndroid.oneui.view.Toast;
import de.dlyt.yanndroid.oneui.view.Tooltip;
import de.dlyt.yanndroid.oneui.widget.DatePicker;
import de.dlyt.yanndroid.oneui.widget.TabLayout;
import de.dlyt.yanndroid.oneui.widget.TimePicker;
import de.dlyt.yanndroid.oneuiexample.base.BaseThemeActivity;
import de.dlyt.yanndroid.oneuiexample.utils.TabsManager;

public class MainActivity extends BaseThemeActivity {
    private String[] mTabsTagName;
    private String[] mTabsTitleName;
    private String[] mTabsClassName;

    private String sharedPrefName;

    private Context mContext;
    private FragmentManager mFragmentManager;
    private Fragment mFragment;
    private TabsManager mTabsManager;

    private DrawerLayout drawerLayout;
    private TabLayout tabLayout;
    private PopupMenu bnvPopupMenu;
    private TipPopup tipPopup;

    private ActivityResultLauncher<Intent> activityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mUseAltTheme = false;

        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_main);

        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> drawerLayout.onSearchModeVoiceInputResult(result));

        init();
    }

    @Override
    public void attachBaseContext(Context context) {
        // pre-OneUI
        if (Build.VERSION.SDK_INT <= 28) {
            super.attachBaseContext(ThemeUtil.createDarkModeContextWrapper(context));
        } else
            super.attachBaseContext(context);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // pre-OneUI
        if (Build.VERSION.SDK_INT <= 28) {
            Resources res = getResources();
            res.getConfiguration().setTo(ThemeUtil.createDarkModeConfig(mContext, newConfig));
        }
    }

    private void init() {
        ViewSupport.semSetRoundedCorners(getWindow().getDecorView(), 0);

        drawerLayout = findViewById(R.id.drawer_view);
        tabLayout = findViewById(R.id.main_samsung_tabs);

        sharedPrefName = "mainactivity_tabs";
        mTabsTagName = getResources().getStringArray(R.array.mainactivity_tab_tag);
        mTabsTitleName = getResources().getStringArray(R.array.mainactivity_tab_title);
        mTabsClassName = getResources().getStringArray(R.array.mainactivity_tab_class);

        mTabsManager = new TabsManager(mContext, sharedPrefName);
        mTabsManager.initTabPosition();

        mFragmentManager = getSupportFragmentManager();

        //DrawerLayout
        drawerLayout.setDrawerButtonOnClickListener(v -> startActivity(new Intent().setClass(mContext, AboutActivity.class)));
        drawerLayout.setDrawerButtonTooltip(getText(R.string.app_info));
        drawerLayout.setButtonBadges(ToolbarLayout.N_BADGE, DrawerLayout.N_BADGE);
        drawerLayout.getAppBarLayout().addOnOffsetChangedListener((layout, verticalOffset) -> {
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

        drawerLayout.inflateToolbarMenu(R.menu.main);
        drawerLayout.getToolbarMenu().findItem(R.id.theme_toggle).setTitle(mUseOUI4Theme ? "Switch to OneUI 3 Theme" : "Switch to OneUI 4 Theme");
        drawerLayout.setOnToolbarMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.search:
                    drawerLayout.showSearchMode();
                    item.setBadge(item.getBadge() + 1);
                    break;
                case R.id.info:
                    startActivity(new Intent().setClass(mContext, AboutActivity.class));
                    item.setBadge(item.getBadge() + 1);
                    break;
                case R.id.theme_toggle:
                    switchOUITheme();
                    break;
            }

            return true;
        });
        drawerLayout.setSearchModeListener(new ToolbarLayout.SearchModeListener() {
            @Override
            public void onKeyboardSearchClick(CharSequence s) {
                Toast.makeText(mContext, s, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onVoiceInputClick(Intent intent) {
                activityResultLauncher.launch(intent);
            }
        });

        // FAB
        FloatingActionButton fab = findViewById(R.id.sesl_fab);
        fab.setRippleColor(getResources().getColor(mUseOUI4Theme ? R.color.sesl4_ripple_color : R.color.sesl_ripple_color));
        // dummy colors
        if (mUseOUI4Theme) {
            fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.sesl_swipe_refresh_background)));
            fab.setSupportImageTintList(ResourcesCompat.getColorStateList(getResources(), R.color.sesl_tablayout_selected_indicator_color, getTheme()));
        } else {
            fab.setBackgroundTintList(ResourcesCompat.getColorStateList(getResources(), R.color.sesl_tablayout_selected_indicator_color, getTheme()));
            fab.setSupportImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.sesl_white)));
        }
        Tooltip.setTooltipText(fab, "FAB");

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (tipPopup != null && tipPopup.isShowing()) {
                    tipPopup.dismiss(true);
                    tipPopup = null;
                } else {
                    tipPopup = new TipPopup(view, mUseOUI4Theme ? TipPopup.MODE_TRANSLUCENT : TipPopup.MODE_NORMAL);
                    tipPopup.setMessage("This is a TipPopup demo.");
                    tipPopup.setAction("Ok", new OnSingleClickListener() {
                        @Override
                        public void onSingleClick(View view) {
                        }
                    });
                    tipPopup.show(getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL ? TipPopup.DIRECTION_TOP_RIGHT : TipPopup.DIRECTION_TOP_LEFT);

                }
            }
        });

        // TabLayout
        for (String s : mTabsTitleName) {
            tabLayout.addTab(tabLayout.newTab().setText(s));
        }

        Drawable icon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_oui_drawer, getTheme());
        icon.setColorFilter(getResources().getColor(R.color.sesl_tablayout_text_color), PorterDuff.Mode.SRC_IN);
        tabLayout.addTabCustomButton(icon, new CustomButtonClickListener(tabLayout) {
            @Override
            public void onClick(View v) {
                popupView(v);
            }
        });

        tabLayout.addOnTabSelectedListener(new SamsungTabLayout.OnTabSelectedListener() {
            public void onTabSelected(SamsungTabLayout.Tab tab) {
                int tabPosition = tab.getPosition();
                mTabsManager.setTabPosition(tabPosition);
                setCurrentItem();
            }

            public void onTabUnselected(SamsungTabLayout.Tab tab) {
            }

            public void onTabReselected(SamsungTabLayout.Tab tab) {
            }
        });

        setCurrentItem();
    }

    private void setCurrentItem() {
        if (tabLayout.isEnabled()) {
            int tabPosition = mTabsManager.getCurrentTab();
            SamsungTabLayout.Tab tab = tabLayout.getTabAt(tabPosition);
            if (tab != null) {
                tab.select();
                setFragment(tabPosition);

                if (tabPosition == 0) {
                    // MainActivityFirstFragment
                    findViewById(R.id.sesl_fab).setVisibility(View.VISIBLE);
                    drawerLayout.setSubtitle("Design");
                    drawerLayout.setNavigationButtonVisible(true);
                    drawerLayout.getToolbarMenu().findItem(R.id.search).setVisible(true);
                    drawerLayout.setImmersiveScroll(false);
                    ((androidx.drawerlayout.widget.DrawerLayout) drawerLayout.findViewById(R.id.drawerLayout)).setDrawerLockMode(androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_UNLOCKED);
                    tabLayout.seslShowDotBadge(1, true);
                } else {
                    // MainActivitySecondFragment
                    findViewById(R.id.sesl_fab).setVisibility(View.GONE);
                    drawerLayout.setSubtitle("Preferences");
                    drawerLayout.setNavigationButtonVisible(false);
                    drawerLayout.getToolbarMenu().findItem(R.id.search).setVisible(false);
                    drawerLayout.setImmersiveScroll(true);
                    ((androidx.drawerlayout.widget.DrawerLayout) drawerLayout.findViewById(R.id.drawerLayout)).setDrawerLockMode(androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                    tabLayout.seslShowDotBadge(1, false);
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
            mFragment = (Fragment) fragment;
            transaction.show(fragment);
        } else {
            try {
                mFragment = (Fragment) Class.forName(mTabsClassName[tabPosition]).newInstance();
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
                    new ClassicColorPickerDialog.OnColorSetListener() {
                        @Override
                        public void onColorSet(int i) {
                            if (currentColor != i)
                                ThemeUtil.setColor(MainActivity.this, Color.red(i), Color.green(i), Color.blue(i));
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

        int currentColor = Color.parseColor("#" + stringColor);

        try {
            mDetailedColorPickerDialog = new DetailedColorPickerDialog(this,
                    new DetailedColorPickerDialog.OnColorSetListener() {
                        @Override
                        public void onColorSet(int i) {
                            if (currentColor != i)
                                ThemeUtil.setColor(MainActivity.this, Color.red(i), Color.green(i), Color.blue(i));
                        }
                    },
                    currentColor);
            mDetailedColorPickerDialog.show();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    public void datePickerDialog(View view) {
       DatePickerDialog datePickerDialog = new DatePickerDialog(mContext, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker seslDatePicker, int year, int month, int day) {
                Toast.makeText(mContext, "Year: " + year + "\nMonth: " + month + "\nDay: " + day, Toast.LENGTH_SHORT).show();
            }
        }, 2022, 0, 1);
        datePickerDialog.show();
    }

    public void timePickerDialog(View view) {
        TimePickerDialog timePickerDialog = new TimePickerDialog(mContext, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                Toast.makeText(mContext, "Hour: " + hourOfDay + "\nMinute: " + minute, Toast.LENGTH_SHORT).show();
            }
        }, 12, 45, true);
        timePickerDialog.show();
    }

    public void standardDialog(View view) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Title")
                .setMessage("Message")
                .setNeutralButton("Maybe", null)
                .setNegativeButton("No", (dialogInterface, i) -> new Handler().postDelayed(dialogInterface::dismiss, 700))
                .setPositiveButton("Yes", (dialogInterface, i) -> new Handler().postDelayed(dialogInterface::dismiss, 700))
                .setNegativeButtonColor(mUseOUI4Theme ? mContext.getResources().getColor(R.color.sesl_functional_red) : 0)
                .setPositiveButtonColor(mUseOUI4Theme ? mContext.getResources().getColor(R.color.sesl_functional_green) : 0)
                .setPositiveButtonProgress(true)
                .setNegativeButtonProgress(true)
                .create();
        dialog.show();
    }

    public void singleChoiceDialog(View view) {
        CharSequence[] charSequences = {"Choice1", "Choice2", "Choice3"};
        new AlertDialog.Builder(this)
                .setTitle("SingleChoiceItems")
                .setNeutralButton("Maybe", null)
                .setNegativeButton("No", null)
                .setPositiveButton("Yes", null)
                .setSingleChoiceItems(charSequences, 0, null)
                .setOnDismissListener(dialogInterface -> multiChoiceDialog(view))
                .show();
    }

    private void multiChoiceDialog(View view) {
        CharSequence[] charSequences = {"Choice1", "Choice2", "Choice3"};
        boolean[] booleans = {true, false, true};
        new AlertDialog.Builder(this)
                .setTitle("MultiChoiceItems")
                .setNeutralButton("Maybe", null)
                .setNegativeButton("No", null)
                .setPositiveButton("Yes", null)
                .setMultiChoiceItems(charSequences, booleans, null)
                .show();
    }

    public void progressDialogSpinner(View view) {
        ProgressDialog dialog = new ProgressDialog(mContext);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setIndeterminate(true);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setTitle("Title");
        dialog.setMessage("ProgressDialog");
        dialog.setButton(ProgressDialog.BUTTON_NEGATIVE, "Cancel", (DialogInterface.OnClickListener) null);
        dialog.setOnDismissListener(dialogInterface -> progressDialogHorizontal(view));
        dialog.show();
        Toast infoToast = Toast.makeText(mContext, "STYLE_SPINNER", Toast.LENGTH_SHORT);
        infoToast.setGravity(Gravity.CENTER, 0 , 0);
        infoToast.show();
    }

    private void progressDialogHorizontal(View view) {
        ProgressDialog dialog = new ProgressDialog(mContext);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setTitle("Title");
        dialog.setMessage("ProgressDialog");
        dialog.setOnDismissListener(dialogInterface -> progressDialogCircleOnly(view));
        dialog.show();

        Toast infoToast = Toast.makeText(mContext, "STYLE_HORIZONTAL", Toast.LENGTH_SHORT);
        infoToast.setGravity(Gravity.CENTER, 0 , 0);
        infoToast.show();

        dialog.setMax(100);

        new Thread() {
             @Override
            public void run() {
                 try {
                     sleep(1000);

                     dialog.setIndeterminate(false);
                     int fakeProgress = 0;
                     while (fakeProgress < 100) {
                         fakeProgress += 5;
                         dialog.setProgress(fakeProgress);
                         sleep(200);
                     }
                     dialog.dismiss();
                 } catch (InterruptedException e) {
                     dialog.dismiss();
                 }
             }
        }.start();
    }

    private void progressDialogCircleOnly(View view) {
        ProgressDialog dialog = new ProgressDialog(mContext);
        dialog.setProgressStyle(ProgressDialog.STYLE_CIRCLE);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();

        Toast infoToast = Toast.makeText(mContext, "STYLE_CIRCLE", Toast.LENGTH_SHORT);
        infoToast.show();
    }

    private void popupView(View view) {
        if (bnvPopupMenu == null) {
            bnvPopupMenu = new PopupMenu(view);
            bnvPopupMenu.setGroupDividerEnabled(true);
            bnvPopupMenu.inflate(R.menu.bnv_menu);
            bnvPopupMenu.setAnimationStyle(R.style.BottomMenuPopupAnimStyle);
            bnvPopupMenu.setPopupMenuListener(new PopupMenu.PopupMenuListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    item.setBadge(item.getBadge() + 1);
                    return !item.isCheckable();
                }

                @Override
                public void onMenuItemUpdate(MenuItem menuItem) {
                    tabLayout.seslShowBadge(2, true, bnvPopupMenu.getMenu().getTotalBadgeCount());
                }
            });
        }
        int xoff = bnvPopupMenu.getPopupMenuWidth() - view.getWidth() + 7;
        if (getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
            bnvPopupMenu.show(xoff, 0);
        } else {
            bnvPopupMenu.show(-xoff, 0);
        }
    }
}
