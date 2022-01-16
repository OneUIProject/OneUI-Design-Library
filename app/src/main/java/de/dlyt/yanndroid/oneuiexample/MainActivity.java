package de.dlyt.yanndroid.oneuiexample;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import de.dlyt.yanndroid.oneui.dialog.AlertDialog;
import de.dlyt.yanndroid.oneui.dialog.ClassicColorPickerDialog;
import de.dlyt.yanndroid.oneui.dialog.DetailedColorPickerDialog;
import de.dlyt.yanndroid.oneui.dialog.ProgressDialog;
import de.dlyt.yanndroid.oneui.layout.DrawerLayout;
import de.dlyt.yanndroid.oneui.layout.ToolbarLayout;
import de.dlyt.yanndroid.oneui.menu.MenuItem;
import de.dlyt.yanndroid.oneui.menu.PopupMenu;
import de.dlyt.yanndroid.oneui.sesl.support.ViewSupport;
import de.dlyt.yanndroid.oneui.sesl.utils.ReflectUtils;
import de.dlyt.yanndroid.oneui.utils.CustomButtonClickListener;
import de.dlyt.yanndroid.oneui.utils.OnSingleClickListener;
import de.dlyt.yanndroid.oneui.utils.ThemeUtil;
import de.dlyt.yanndroid.oneui.view.BottomNavigationView;
import de.dlyt.yanndroid.oneui.view.Snackbar;
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
    private ToolbarLayout toolbarLayout;
    private BottomNavigationView bnvLayout;
    private PopupMenu bnvPopupMenu;

    private ActivityResultLauncher<Intent> activityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mUseAltTheme = false;

        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_main);

        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> toolbarLayout.onSearchModeVoiceInputResult(result));

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

    private void init() {
        ViewSupport.semSetRoundedCorners(getWindow().getDecorView(), 0);

        drawerLayout = findViewById(R.id.drawer_view);
        toolbarLayout = drawerLayout.getToolbarLayout();
        bnvLayout = findViewById(R.id.main_samsung_tabs);

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

        toolbarLayout.inflateToolbarMenu(R.menu.main);
        toolbarLayout.getToolbarMenu().findItem(R.id.theme_toggle).setTitle(mUseOUI4Theme ? "Switch to OneUI 3 Theme" : "Switch to OneUI 4 Theme");
        toolbarLayout.setOnToolbarMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.search:
                    toolbarLayout.showSearchMode();
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
        toolbarLayout.setSearchModeListener(new ToolbarLayout.SearchModeListener() {
            @Override
            public void onSearchOpened(EditText search_edittext) {
            }

            @Override
            public void onSearchDismissed(EditText search_edittext) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void onKeyboardSearchClick(CharSequence s) {
                Toast.makeText(mContext, s, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onVoiceInputClick(Intent intent) {
                activityResultLauncher.launch(intent);
            }
        });

        //BottomNavigationLayout
        Drawable icon = getDrawable(R.drawable.ic_samsung_drawer);
        icon.setColorFilter(getResources().getColor(R.color.sesl_tablayout_text_color), PorterDuff.Mode.SRC_IN);
        for (String s : mTabsTitleName) {
            bnvLayout.addTab(bnvLayout.newTab().setText(s));
        }
        bnvLayout.addTabCustomButton(icon, new CustomButtonClickListener(bnvLayout) {
            @Override
            public void onClick(View v) {
                popupView(v);
            }
        });

        bnvLayout.addOnTabSelectedListener(new BottomNavigationView.OnTabSelectedListener() {
            public void onTabSelected(BottomNavigationView.Tab tab) {
                int tabPosition = tab.getPosition();
                mTabsManager.setTabPosition(tabPosition);
                setCurrentItem();
            }

            public void onTabUnselected(BottomNavigationView.Tab tab) {
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
                    toolbarLayout.getToolbarMenu().findItem(R.id.search).setVisible(true);
                    ((androidx.drawerlayout.widget.DrawerLayout) drawerLayout.findViewById(R.id.drawerLayout)).setDrawerLockMode(androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_UNLOCKED);
                } else {
                    // MainActivitySecondFragment
                    toolbarLayout.setSubtitle("Preferences");
                    toolbarLayout.setNavigationButtonVisible(false);
                    toolbarLayout.getToolbarMenu().findItem(R.id.search).setVisible(false);
                    ((androidx.drawerlayout.widget.DrawerLayout) drawerLayout.findViewById(R.id.drawerLayout)).setDrawerLockMode(androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
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
                    new ClassicColorPickerDialog.ColorPickerChangedListener() {
                        @Override
                        public void onColorChanged(int i) {
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

        float[] currentColor = new float[3];
        Color.colorToHSV(Color.parseColor("#" + stringColor), currentColor);

        mDetailedColorPickerDialog = new DetailedColorPickerDialog(this, 2, currentColor);
        mDetailedColorPickerDialog.setColorPickerChangeListener(new DetailedColorPickerDialog.ColorPickerChangedListener() {
            @Override
            public void onColorChanged(int i, float[] fArr) {
                if (!(fArr[0] == currentColor[0] && fArr[1] == currentColor[1] && fArr[2] == currentColor[2]))
                    ThemeUtil.setColor(MainActivity.this, fArr);
            }

            @Override
            public void onViewModeChanged(int i) {

            }
        });
        mDetailedColorPickerDialog.show();
    }

    public void standardDialog(View view) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Title")
                .setMessage("Message")
                .setNeutralButton("Maybe", null)
                .setNegativeButton("No", null)
                .setPositiveButton("Yes", null)
                .create();
        dialog.show();


        Button positiveBtn = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        if (mUseOUI4Theme) {
            Button negativeBtn = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
            negativeBtn.setTextColor(getResources().getColor(R.color.sesl_functional_red));
            positiveBtn.setTextColor(getResources().getColor(R.color.sesl_functional_green));
        }
        positiveBtn.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                showProgressBarInDialog(dialog);
                new Handler().postDelayed(dialog::dismiss, 700);
            }
        });
    }

    private void showProgressBarInDialog(AlertDialog dialog) {
        Button positiveBtn = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        Button negativeBtn = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        Button neutralBtn = dialog.getButton(DialogInterface.BUTTON_NEUTRAL);

        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        if (negativeBtn != null) negativeBtn.setEnabled(false);
        if (neutralBtn != null) neutralBtn.setEnabled(false);
        if (positiveBtn != null) {
            positiveBtn.setEnabled(false);

            ViewGroup buttonBar = (ViewGroup) positiveBtn.getParent();
            if (buttonBar != null) {
                int buttonIndex = buttonBar.indexOfChild(positiveBtn);

                ViewGroup.LayoutParams lp = positiveBtn.getLayoutParams();
                lp.height = getResources().getDimensionPixelSize(R.dimen.dialog_progress_bar_size);
                lp.width = getResources().getDimensionPixelSize(R.dimen.dialog_progress_bar_size);

                View inflate = LayoutInflater.from(this).inflate(R.layout.dialog_progress_bar, buttonBar, false);
                inflate.setLayoutParams(lp);

                buttonBar.removeView(positiveBtn);
                buttonBar.addView(inflate, buttonIndex);
            }
        }
    }

    public void singleChoiceDialog(View view) {
        CharSequence[] charSequences = {"Choice1", "Choice2", "Choice3"};
        new AlertDialog.Builder(this)
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
        new AlertDialog.Builder(this)
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
                    return true;
                }

                @Override
                public void onMenuItemUpdate(MenuItem menuItem) {

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
