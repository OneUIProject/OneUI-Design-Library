package de.dlyt.yanndroid.oneuiexample;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import de.dlyt.yanndroid.oneui.ColorPickerDialog;
import de.dlyt.yanndroid.oneui.ThemeColor;
import de.dlyt.yanndroid.oneui.tabs.SamsungTabLayout;
import de.dlyt.yanndroid.oneuiexample.utils.BaseTabFragment;
import de.dlyt.yanndroid.oneuiexample.utils.TabsManager;

public class MainActivity extends AppCompatActivity {
    private String[] mTabsTagName;
    private String[] mTabsTitleName;
    private String[] mTabsClassName;

    private String sharedPrefName;

    private Context mContext;
    private FragmentManager mFragmentManager;
    private BaseTabFragment mFragment;
    private TabsManager mTabsManager;

    private SamsungTabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new ThemeColor(this);
        mContext = this;
        setContentView(R.layout.activity_main);
        init();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        View focusedTab = tabLayout.getFocusedChild();
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
        tabLayout = findViewById(R.id.main_samsung_tabs);

        sharedPrefName = "mainactivity_tabs";
        mTabsTagName = getResources().getStringArray(R.array.mainactivity_tab_tag);
        mTabsTitleName = getResources().getStringArray(R.array.mainactivity_tab_title);
        mTabsClassName = getResources().getStringArray(R.array.mainactivity_tab_class);

        mTabsManager = new TabsManager(mContext, sharedPrefName);
        mTabsManager.initTabPosition();

        mFragmentManager = getSupportFragmentManager();

        for (String s: mTabsTitleName) {
            tabLayout.addTab(tabLayout.newTab().setText(s));
        }

        tabLayout.addOnTabSelectedListener(new SamsungTabLayout.OnTabSelectedListener() {
            public void onTabSelected(SamsungTabLayout.Tab tab) {
                int tabPosition = tab.getPosition();
                mTabsManager.setTabPosition(tabPosition);
                setCurrentItem();
                BaseTabFragment fragment = (BaseTabFragment) mFragmentManager.findFragmentByTag(mTabsTagName[tabPosition]);
                if (fragment != null) fragment.onTabSelected();
            }

            public void onTabUnselected(SamsungTabLayout.Tab tab) {
                int tabPosition = tab.getPosition();
                BaseTabFragment fragment = (BaseTabFragment) mFragmentManager.findFragmentByTag(mTabsTagName[tabPosition]);
                if (fragment != null) fragment.onTabUnselected();
            }

            public void onTabReselected(SamsungTabLayout.Tab tab) { }
        });

        //SamsungTabLayout.setup(this);

        setCurrentItem();
    }

    private void setCurrentItem() {
        if (tabLayout.isEnabled()) {
            int tabPosition = mTabsManager.getCurrentTab();
            SamsungTabLayout.Tab tab = tabLayout.getTabAt(tabPosition);
            if (tab != null) {
                tab.select();
                setFragment(tabPosition);
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
    public void colorPickerDialog(View view) {
        ColorPickerDialog mColorPickerDialog;
        SharedPreferences sharedPreferences = getSharedPreferences("ThemeColor", Context.MODE_PRIVATE);
        String stringColor = sharedPreferences.getString("color", "0381fe");

        float[] currentColor = new float[3];
        Color.colorToHSV(Color.parseColor("#" + stringColor), currentColor);

        mColorPickerDialog = new ColorPickerDialog(this, 2, currentColor);
        mColorPickerDialog.setColorPickerChangeListener(new ColorPickerDialog.ColorPickerChangedListener() {
            @Override
            public void onColorChanged(int i, float[] fArr) {
                if (!(fArr[0] == currentColor[0] && fArr[1] == currentColor[1] && fArr[2] == currentColor[2]))
                    ThemeColor.setColor(MainActivity.this, fArr);
            }

            @Override
            public void onViewModeChanged(int i) {

            }
        });
        mColorPickerDialog.show();
    }

    public void standardDialog(View view) {
        Context context = new ContextThemeWrapper(this, R.style.DialogStyle);
        new AlertDialog.Builder(context)
                .setTitle("Title")
                .setIcon(R.drawable.ic_launcher)
                .setMessage("Message")
                .setNeutralButton("Maybe", null)
                .setNegativeButton("No", null)
                .setPositiveButton("Yes", null)
                .show();
    }

    public void singleChoiceDialog(View view) {
        Context context = new ContextThemeWrapper(this, R.style.DialogStyle);
        CharSequence[] charSequences = {"Choice1", "Choice2", "Choice3"};
        new AlertDialog.Builder(context)
                .setTitle("Title")
                .setIcon(R.drawable.ic_launcher)
                .setNeutralButton("Maybe", null)
                .setNegativeButton("No", null)
                .setPositiveButton("Yes", null)
                .setSingleChoiceItems(charSequences, 0, null)
                .show();
    }

    public void multiChoiceDialog(View view) {
        Context context = new ContextThemeWrapper(this, R.style.DialogStyle);
        CharSequence[] charSequences = {"Choice1", "Choice2", "Choice3"};
        boolean[] booleans = {true, false, true};
        new AlertDialog.Builder(context)
                .setTitle("Title")
                .setIcon(R.drawable.ic_launcher)
                .setNeutralButton("Maybe", null)
                .setNegativeButton("No", null)
                .setPositiveButton("Yes", null)
                .setMultiChoiceItems(charSequences, booleans, null)
                .show();
    }
}
