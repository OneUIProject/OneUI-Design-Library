package de.dlyt.yanndroid.oneui.view;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.reflect.content.res.SeslConfigurationReflector;

import java.util.ArrayList;

import de.dlyt.yanndroid.oneui.R;
import de.dlyt.yanndroid.oneui.sesl.support.ViewSupport;
import de.dlyt.yanndroid.oneui.sesl.support.WindowManagerSupport;
import de.dlyt.yanndroid.oneui.sesl.tabs.SamsungBaseTabLayout;

public class BottomNavigationView extends SamsungBaseTabLayout implements View.OnSystemUiVisibilityChangeListener {
    private Activity mActivity;
    private boolean mIsResumed = false;
    private ArrayList<TextView> mTextViews;

    public BottomNavigationView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.bottomNaviViewStyle);
    }

    public BottomNavigationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mDepthStyle = 1;
    }

    public void updateWidget(Activity activity) {
        mActivity = activity;
        mTextViews = new ArrayList<>();

        for (int tabPosition = 0; tabPosition < getTabCount(); tabPosition++) {
            Tab tab = getTabAt(tabPosition);
            ViewGroup tabView = (ViewGroup) getTabView(tabPosition);
            if (!(tab == null || tabView == null)) {
                TextView textView = tab.seslGetTextView();
                mTextViews.add(textView);
                ViewSupport.setPointerIcon(tabView, 1000);
            }
        }
        invalidateTabLayout();
    }

    private void invalidateTabLayout() {
        ArrayList<Float> tabTextWidthList = new ArrayList<>();
        float tabTextWidthSum = 0.0f;
        for (int tabPosition = 0; tabPosition < getTabCount(); tabPosition++) {
            float width = getTabTextWidth((TextView) mTextViews.get(tabPosition));
            tabTextWidthList.add(width);
            tabTextWidthSum += width;
        }
        addTabPaddingValue(tabTextWidthList, tabTextWidthSum);
    }

    private float getTabTextWidth(TextView textView) {
        return textView.getPaint().measureText(textView.getText().toString());
    }

    private void addTabPaddingValue(ArrayList<Float> tabTextWidthList, float tabTextWidthSum) {
        float tabTextPadding = (float) getResources().getDimensionPixelSize(R.dimen.bnv_padding);
        float tabTextPaddingSum = tabTextPadding * 8.0f;
        float tabLayoutPadding = (float) getResources().getDimensionPixelSize(R.dimen.tab_layout_padding);

        Window window = mActivity.getWindow();
        Point size = new Point();
        if (isVisibleNaviBar(getContext()) || WindowManagerSupport.isMultiWindowMode(mActivity) || isInSamsungDeXMode(getContext())) {
            window.getWindowManager().getDefaultDisplay().getSize(size);
        } else {
            window.getWindowManager().getDefaultDisplay().getRealSize(size);
        }

        float screenWidthPixels = (float) size.x;
        if (!isMultiWindowMinSize(mActivity, 480, true)) {
            float tabLayoutPaddingMax = screenWidthPixels * 0.125f;
            float tabLayoutPaddingMin = ((screenWidthPixels - tabTextWidthSum) - tabTextPaddingSum) / 2.0f;
            if (tabLayoutPaddingMin >= tabLayoutPaddingMax) {
                tabLayoutPadding = tabLayoutPaddingMax;
            } else if (tabLayoutPadding < tabLayoutPaddingMin) {
                tabLayoutPadding = tabLayoutPaddingMin;
            }
        }

        float widthPixels = screenWidthPixels - (2.0f * tabLayoutPadding);
        if (tabTextWidthSum + tabTextPaddingSum < widthPixels) {
            float paddingLeftRight = (float) Math.ceil((double) (((widthPixels - (tabTextWidthSum + tabTextPaddingSum)) / 8.0f) + tabTextPadding));
            float paddingLastTab = (widthPixels - tabTextWidthSum) - (8.0f * paddingLeftRight);
            for (int i = 0; i < tabTextWidthList.size(); i++) {
                if (paddingLastTab == 0.0f || i != 3) {
                    getTabView(i).setMinimumWidth((int) ((float) tabTextWidthList.get(i) + (2.0f * paddingLeftRight)));
                } else {
                    getTabView(i).setMinimumWidth((int) ((float) tabTextWidthList.get(i) + (2.0f * paddingLeftRight) + paddingLastTab));
                }
            }
        } else {
            for (int i2 = 0; i2 < tabTextWidthList.size(); i2++) {
                getTabView(i2).setMinimumWidth((int) ((float) tabTextWidthList.get(i2) + (2.0f * tabTextPadding)));
            }
        }

        ((MarginLayoutParams) getLayoutParams()).setMargins((int) tabLayoutPadding, 0, (int) tabLayoutPadding, 0);
        requestLayout();
    }

    private View getTabView(int position) {
        ViewGroup viewGroup = getTabViewGroup();
        if (viewGroup == null || viewGroup.getChildCount() <= position) {
            return null;
        }
        return viewGroup.getChildAt(position);
    }

    private ViewGroup getTabViewGroup() {
        if (getChildCount() <= 0) {
            return null;
        }
        View view = getChildAt(0);
        if (view == null || !(view instanceof ViewGroup)) {
            return null;
        }
        return (ViewGroup) view;
    }

    public void setTabLayoutEnabled(boolean enabled) {
        float f;
        setEnabled(enabled);
        for (int tabPosition = 0; tabPosition < getTabCount(); tabPosition++) {
            ViewGroup tabView = (ViewGroup) getTabView(tabPosition);
            if (tabView != null) {
                tabView.setEnabled(enabled);
                if (enabled) {
                    f = 1.0f;
                } else {
                    f = 0.4f;
                }
                tabView.setAlpha(f);
            }
        }
    }

    public void setResumeStatus(boolean isResumed) {
        mIsResumed = isResumed;
    }

    @Override
    public void onSystemUiVisibilityChange(int visibility) {
        if (mIsResumed) {
            invalidateTabLayout();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        invalidateTabLayout();
    }

    private double getDensity(Context context) {
        Configuration configuration = context.getResources().getConfiguration();
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager == null ? null : windowManager.getDefaultDisplay();
        if (display != null) {
            display.getRealMetrics(metrics);
        }
        if (display == null) {
            return 1.0d;
        }
        return ((double) configuration.densityDpi) / ((double) metrics.densityDpi);
    }

    private boolean isInSamsungDeXMode(Context context) {
        return SeslConfigurationReflector.isDexEnabled(context.getResources().getConfiguration());
    }

    private boolean isMultiWindowMinSize(Context context, int minSizeDp, boolean isWidth) {
        Configuration configuration = context.getResources().getConfiguration();
        return ((int) (((double) (isWidth ? configuration.screenWidthDp : configuration.screenHeightDp)) * getDensity(context))) <= minSizeDp;
    }

    private boolean isVisibleNaviBar(Context context) {
        return Settings.Global.getInt(context.getContentResolver(), "navigationbar_hide_bar_enabled", 0) == 0;
    }

}
