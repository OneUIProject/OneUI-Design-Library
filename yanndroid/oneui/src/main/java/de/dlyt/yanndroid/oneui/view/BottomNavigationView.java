package de.dlyt.yanndroid.oneui.view;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
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

        Float[] tabCount = new Float[getTabCount()];
        float f = 0.0f;

        for (int tabPosition = 0; tabPosition < getTabCount(); tabPosition++) {
            Tab tab = getTabAt(tabPosition);
            ViewGroup tabView = (ViewGroup) getTabView(tabPosition);
            if (!(tab == null || tabView == null)) {
                TextView textView = tab.seslGetTextView();
                mTextViews.add(textView);
                ViewSupport.setPointerIcon(tabView, 1000);

                tabCount[tabPosition] = getTabTextWidth(tab.seslGetTextView());
                f += tabCount[tabPosition];
            }
        }

        setViewDimens(tabCount, f);
        invalidateTabLayout();
    }

    private void setViewDimens(Float[] fArr, float f) {
        int i;
        int tabCount = getTabCount();
        if (tabCount > 0) {
            int dimensionPixelSize = getResources().getDimensionPixelSize(R.dimen.tablayout_start_end_margin);
            i = getContext().getResources().getDisplayMetrics().widthPixels;
            int i2 = i - (dimensionPixelSize * 2);
            float dimensionPixelSize2 = (float) getResources().getDimensionPixelSize(R.dimen.tablayout_text_padding);
            float f2 = (float) i2;
            float f3 = f2 / ((float) tabCount);
            float f4 = dimensionPixelSize2 * 2.0f;
            float f5 = (0.75f * f2) - f4;
            float f6 = 0.0f;
            int i3 = 0;
            int i4 = 0;
            for (int i5 = 0; i5 < tabCount; i5++) {
                Log.d("BottomNavigationView", "i : " + i5 + ", width : " + fArr[i5]);
                if (f5 < fArr[i5].floatValue()) {
                    i3 = (int) (((float) i3) + (fArr[i5].floatValue() - f5));
                    fArr[i5] = Float.valueOf(f5);
                    i4++;
                    f6 = f5;
                } else if (f6 < fArr[i5].floatValue()) {
                    f6 = fArr[i5].floatValue();
                }
            }
            float f7 = f - ((float) i3);
            setTabMode(0);
            Log.d("BottomNavigationView", "[MODE_SCROLLABLE]");
            Log.d("BottomNavigationView", "availableContentWidth : " + i2 + ", tabTextPaddingLeftRight : " + dimensionPixelSize2);
            ViewGroup viewGroup = (ViewGroup) getChildAt(0);
            int i6 = (tabCount - i4) * 2;
            int i7 = i6 > 0 ? ((int) ((f2 - f7) - ((((float) i4) * dimensionPixelSize2) * 2.0f))) / i6 : 0;
            int i8 = (int) dimensionPixelSize2;
            int i9 = -1;
            boolean z = true;
            if (i7 < i8) {
                i7 = i8;
            } else {
                float f8 = f6 + dimensionPixelSize2 + dimensionPixelSize2;
                if (f3 >= f8) {
                    setTabMode(1);
                    for (int i10 = 0; i10 < tabCount; i10++) {
                        ((ViewGroup) viewGroup.getChildAt(i10)).getChildAt(0).getLayoutParams().width = -1;
                        getTabAt(i10).seslGetTextView().setMaxWidth(i2);
                        getTabAt(i10).seslGetTextView().setMinimumWidth(0);
                        getTabAt(i10).seslGetTextView().setPadding(0, 0, 0, 0);
                    }
                    Log.d("BottomNavigationView", "[MODE_FIXED] TabCount : " + tabCount + ", minNeededTabWidth : " + f3 + ", maxTabWidth : " + f8);
                    return;
                }
            }
            int i11 = 0;
            while (i11 < tabCount) {
                boolean z2 = fArr[i11].floatValue() >= f5 ? z : false;
                int floatValue = (int) (fArr[i11].floatValue() + ((z2 ? dimensionPixelSize2 : (float) i7) * 2.0f));
                ViewGroup.LayoutParams layoutParams = viewGroup.getChildAt(i11).getLayoutParams();
                layoutParams.width = floatValue;
                layoutParams.height = i9;
                viewGroup.getChildAt(i11).setMinimumWidth(floatValue);
                int i12 = z2 ? 0 : (int) (((float) i7) - dimensionPixelSize2);
                getTabAt(i11).seslGetTextView().setMaxWidth((int) f5);
                getTabAt(i11).seslGetTextView().setMinimumWidth(floatValue - ((int) f4));
                getTabAt(i11).seslGetTextView().setPadding(i12, 0, i12, 0);
                Log.d("BottomNavigationView", "params.width : " + layoutParams.width + ", tabWidthList[" + i11 + "] : " + fArr[i11] + ", LeftRightPadding : " + (i7 * 2));
                i11++;
                i9 = -1;
                z = true;
            }
            requestLayout();
        }
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
