package de.dlyt.yanndroid.oneui.widget;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.reflect.content.res.SeslConfigurationReflector;

import de.dlyt.yanndroid.oneui.R;
import de.dlyt.yanndroid.oneui.sesl.support.WindowManagerSupport;
import de.dlyt.yanndroid.oneui.sesl.tabs.SamsungTabLayout;
import de.dlyt.yanndroid.oneui.sesl.utils.ReflectUtils;
import de.dlyt.yanndroid.oneui.utils.CustomButtonClickListener;

public class TabLayout extends SamsungTabLayout {
    private Context mContext;
    public float mScreenWidthPixels;
    public float mTabLayoutPaddingMax;
    public float mTabTextPadding;
    public float mTabTextPaddingSum;

    public TabLayout(@NonNull Context context) {
        this(context, null);
    }

    public TabLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.tabStyle);
    }

    public TabLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;

        Point point = new Point();
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        if (isVisibleNaviBar() || SeslConfigurationReflector.isDexEnabled(getResources().getConfiguration()) || WindowManagerSupport.isMultiWindowMode(getActivity())) {
            wm.getDefaultDisplay().getSize(point);
        } else {
            wm.getDefaultDisplay().getRealSize(point);
        }
        mScreenWidthPixels = (float) point.x;

        mTabTextPadding = getResources().getDimension(R.dimen.tab_layout_default_padding);
        mTabTextPaddingSum = mTabTextPadding * 8.0f;
        mTabLayoutPaddingMax = mScreenWidthPixels * 0.125f;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        setTabLayoutMargin();
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setTabLayoutMargin();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        for (int tabPosition = 0; tabPosition < getTabCount(); tabPosition++) {
            ViewGroup tabView = (ViewGroup) getTabView(tabPosition);
            if (tabView != null) {
                tabView.setEnabled(enabled);
                tabView.setAlpha(enabled ? 1.0f : 0.4f);
            }
        }
    }

    public float calculateTabLayoutPadding(float textWidthSum, float padding) {
        Configuration config = mContext.getResources().getConfiguration();
        int screenWidthDp = config.screenWidthDp;

        if (isDisplayDeviceTypeSub(config) && config.orientation == Configuration.ORIENTATION_PORTRAIT) {
            return padding / 2.0f;
        }
        if (screenWidthDp <= 480) {
            return padding;
        }

        float tabLayoutPaddingMin = ((mScreenWidthPixels - textWidthSum) - mTabTextPaddingSum) / 2.0f;
        if (tabLayoutPaddingMin < mTabLayoutPaddingMax) {
            return padding < tabLayoutPaddingMin ? tabLayoutPaddingMin : padding;
        } else {
            return mTabLayoutPaddingMax;
        }
    }

    private AppCompatActivity getActivity() {
        Context context = getContext();
        while (context instanceof ContextWrapper) {
            if (context instanceof AppCompatActivity) {
                return (AppCompatActivity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }

    private float getTabTextWidth(TextView textView) {
        return textView.getPaint().measureText(textView.getText().toString());
    }

    private ViewGroup getTabViewGroup() {
        if (getChildCount() <= 0) {
            return null;
        }

        View view = getChildAt(0);
        if (view != null && view instanceof ViewGroup) {
            return (ViewGroup) view;
        }
        return null;
    }

    private View getTabView(int position) {
        ViewGroup viewGroup = getTabViewGroup();
        if (viewGroup != null && viewGroup.getChildCount() > position) {
            return viewGroup.getChildAt(position);
        }
        return null;
    }

    private boolean isDisplayDeviceTypeSub(Configuration config) {
        if (config == null) {
            return false;
        }

        Object displayDeviceType = ReflectUtils.genericGetField(Configuration.class, config, "semDisplayDeviceType");
        if (displayDeviceType != null) {
            return ((int) displayDeviceType) == 5; /* Configuration.SEM_DISPLAY_DEVICE_TYPE_SUB */
        } else {
            return false;
        }
    }

    private boolean isVisibleNaviBar() {
        return Settings.Global.getInt(mContext.getContentResolver(), "navigationbar_hide_bar_enabled", 0) == 0;
    }

    private void setTabLayoutMargin() {
        float tabTextWidthSum = 0.0f;
        for (int i = 0; i < getTabCount(); i++) {
            final Tab tab = getTabAt(i);
            tabTextWidthSum += getTabTextWidth(tab.seslGetTextView());
        }

        final int margin = (int) calculateTabLayoutPadding(tabTextWidthSum, mTabTextPadding);
        ((ViewGroup.MarginLayoutParams) getLayoutParams()).setMargins(margin, 0, margin, 0);
    }

    public void addTabCustomButton(Drawable icon, CustomButtonClickListener listener) {
        if (mDepthStyle != 2) {
            Tab tab = newTab().setIcon(icon);
            addTab(tab);

            ViewGroup view = ((ViewGroup) getTabView(tab.getPosition()));
            view.setBackground(getContext().getDrawable(R.drawable.oui_tablayout_custombutton_background));
            view.setOnTouchListener(listener);
        } else {
            Log.w("TabLayout", "addTabCustomButton not supported with DEPTH_TYPE_SUB");
        }
    }
}
