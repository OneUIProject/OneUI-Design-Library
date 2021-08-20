package de.dlyt.yanndroid.oneuiexample;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import de.dlyt.yanndroid.oneui.BottomNavigationView;
import de.dlyt.yanndroid.oneui.ClassicColorPickerDialog;
import de.dlyt.yanndroid.oneui.DetailedColorPickerDialog;
import de.dlyt.yanndroid.oneui.ThemeColor;
import de.dlyt.yanndroid.oneui.dialog.ProgressDialog;
import de.dlyt.yanndroid.oneui.dialog.SamsungAlertDialog;
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

    private BottomNavigationView bnvLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new ThemeColor(this);
        mContext = this;
        setContentView(R.layout.activity_main);
        init();
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
        bnvLayout = findViewById(R.id.main_samsung_tabs);

        sharedPrefName = "mainactivity_tabs";
        mTabsTagName = getResources().getStringArray(R.array.mainactivity_tab_tag);
        mTabsTitleName = getResources().getStringArray(R.array.mainactivity_tab_title);
        mTabsClassName = getResources().getStringArray(R.array.mainactivity_tab_class);

        mTabsManager = new TabsManager(mContext, sharedPrefName);
        mTabsManager.initTabPosition();

        mFragmentManager = getSupportFragmentManager();

        for (String s: mTabsTitleName) {
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

            public void onTabReselected(BottomNavigationView.Tab tab) { }
        });
        bnvLayout.setup(this);
        setCurrentItem();
    }

    private void setCurrentItem() {
        if (bnvLayout.isEnabled()) {
            int tabPosition = mTabsManager.getCurrentTab();
            BottomNavigationView.Tab tab = bnvLayout.getTabAt(tabPosition);
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
        dialog.setButton(ProgressDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                progressDialogCircleOnly();
            }
        });
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                progressDialogCircleOnly();
            }
        });
        dialog.show();
    }

    private void progressDialogCircleOnly() {
        Toast.makeText(mContext, "Click anywhere to dismiss", Toast.LENGTH_SHORT).show();
        ProgressDialog dialog = new ProgressDialog(mContext);
        dialog.setProgressStyle(ProgressDialog.STYLE_CIRCLE_ONLY);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }
}
