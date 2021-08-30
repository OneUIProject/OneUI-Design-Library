package de.dlyt.yanndroid.oneui.layout;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.IntDef;
import androidx.annotation.Nullable;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textview.MaterialTextView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Locale;

import de.dlyt.yanndroid.oneui.R;
import de.dlyt.yanndroid.oneui.sesl.appbar.SamsungAppBarLayout;
import de.dlyt.yanndroid.oneui.sesl.appbar.SamsungCollapsingToolbarLayout;
import de.dlyt.yanndroid.oneui.sesl.support.ViewSupport;
import de.dlyt.yanndroid.oneui.sesl.support.WindowManagerSupport;
import de.dlyt.yanndroid.oneui.sesl.widget.ToolbarImageButton;

public class ToolbarLayout extends LinearLayout {
    public static final int APPBAR_LAYOUT = 0;
    public static final int COLLAPSING_TOOLBAR = 1;
    public static final int TOOLBAR = 2;
    public static final int NAVIGATION_ICON = 3;
    public static final int COLLAPSED_TITLE = 4;
    public static final int CONTENT_LAYOUT = 5;
    public static final int N_BADGE = -1;
    private static String TAG = "ToolbarLayout";
    public ViewGroup navigationBadgeBackground;
    public TextView navigationBadgeText;
    public ViewGroup moreOverflowBadgeBackground;
    public TextView moreOverflowBadgeText;
    private Context mContext;
    private NumberFormat numberFormat = NumberFormat.getInstance(Locale.getDefault());
    private PopupWindow moreMenuPopupWindow = null;
    private MoreMenuPopupAdapter moreMenuPopupAdapter;
    private View moreMenuPopupAnchor = null;
    private int moreMenuPopupOffX;
    private Drawable mNavigationIcon;
    private int mLayout;
    private CharSequence mTitle;
    private CharSequence mSubtitle;
    private Boolean mExpandable;
    private Boolean mExpanded;
    private SamsungAppBarLayout appBarLayout;
    private SamsungCollapsingToolbarLayout collapsingToolbarLayout;
    private MaterialToolbar toolbar;
    private FrameLayout navigationButtonContainer;
    private ToolbarImageButton navigationButton;
    private MaterialTextView collapsedTitleView;
    private LinearLayout overflowContainer;
    private FrameLayout moreOverflowButtonContainer;
    private ToolbarImageButton moreOverflowButton;
    private RoundLinearLayout mainContainer;
    private LinearLayout bottomContainer;

    public ToolbarLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        mContext = context;

        TypedArray attr = mContext.getTheme().obtainStyledAttributes(attrs, R.styleable.ToolBarLayout, 0, 0);

        try {
            mExpandable = attr.getBoolean(R.styleable.ToolBarLayout_expandable, true);
            mExpanded = attr.getBoolean(R.styleable.ToolBarLayout_expanded, true);
            mLayout = attr.getResourceId(R.styleable.ToolBarLayout_android_layout, mExpandable ? R.layout.samsung_appbar_toolbarlayout : R.layout.samsung_toolbar_toolbarlayout);
            mTitle = attr.getString(R.styleable.ToolBarLayout_title);
            mSubtitle = attr.getString(R.styleable.ToolBarLayout_subtitle);
            mNavigationIcon = attr.getDrawable(R.styleable.ToolBarLayout_navigationIcon);
        } finally {
            attr.recycle();
        }

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(mLayout, this, true);

        if (mExpandable) {
            appBarLayout = findViewById(R.id.toolbar_layout_app_bar);
            collapsingToolbarLayout = findViewById(R.id.toolbar_layout_collapsing_toolbar_layout);

            appBarLayout.addOnOffsetChangedListener(new AppBarOffsetListener());
        }
        toolbar = findViewById(R.id.toolbar_layout_toolbar);

        navigationButtonContainer = findViewById(R.id.toolbar_layout_navigationButton_container);
        navigationButton = findViewById(R.id.toolbar_layout_navigationButton);
        collapsedTitleView = findViewById(R.id.toolbar_layout_collapsed_title);
        overflowContainer = findViewById(R.id.toolbar_layout_overflow_container);
        moreMenuPopupAnchor = findViewById(R.id.toolbar_layout_popup_window_anchor);

        mainContainer = findViewById(R.id.toolbar_layout_main_container);
        bottomContainer = findViewById(R.id.toolbar_layout_bottom_container);

        setNavigationButtonIcon(mNavigationIcon);
        setTitle(mTitle);
        setSubtitle(mSubtitle);

        refreshLayout(getResources().getConfiguration());

    }

    public View getView(@ToolbarLayoutView int view) {
        switch (view) {
            case APPBAR_LAYOUT:
                return appBarLayout;
            case COLLAPSING_TOOLBAR:
                return collapsingToolbarLayout;
            case TOOLBAR:
                return toolbar;
            case NAVIGATION_ICON:
                return navigationButton;
            case COLLAPSED_TITLE:
                return collapsedTitleView;
            case CONTENT_LAYOUT:
                return mainContainer;
            default:
                return null;
        }
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (mainContainer == null || bottomContainer == null) {
            super.addView(child, index, params);
        } else {
            ToolbarLayout.Drawer_Toolbar_LayoutParams lp = (ToolbarLayout.Drawer_Toolbar_LayoutParams) params;
            switch (lp.layout_location) {
                case 0:
                    mainContainer.addView(child, index, params);
                    break;
                case 1:
                    bottomContainer.addView(child, index, params);
                    break;
            }
        }
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new Drawer_Toolbar_LayoutParams(getContext(), null);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new Drawer_Toolbar_LayoutParams(getContext(), attrs);
    }

    public static class Drawer_Toolbar_LayoutParams extends LayoutParams {

        public Integer layout_location;

        public Drawer_Toolbar_LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
            if (c != null && attrs != null) {
                TypedArray a = c.obtainStyledAttributes(attrs, R.styleable.Drawer_ToolBar_LayoutLocation);
                layout_location = a.getInteger(R.styleable.Drawer_ToolBar_LayoutLocation_layout_location, 0);
                a.recycle();
            }
        }
    }


    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        refreshLayout(newConfig);
    }

    private Activity getActivity() {
        Context context = getContext();
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }

    private float getDIPForPX(int i) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) i, getResources().getDisplayMetrics());
    }

    private int getWindowHeight() {
        try {
            WindowManager windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);

            if (windowManager == null) {
                return 0;
            }

            Point point = new Point();
            windowManager.getDefaultDisplay().getSize(point);
            return point.y;
        } catch (Exception unused) {
            Log.e(TAG + ".getWindowHeight", "Cannot get window height");
            return 0;
        }
    }

    private void refreshLayout(Configuration newConfig) {
        WindowManagerSupport.hideStatusBarForLandscape(getActivity(), newConfig.orientation);

        ViewSupport.updateListBothSideMargin(getActivity(), mainContainer);
        ViewSupport.updateListBothSideMargin(getActivity(),findViewById(R.id.toolbar_layout_bottom_corners));
        ViewSupport.updateListBothSideMargin(getActivity(),findViewById(R.id.toolbar_layout_bottom_container));

        if (mExpandable) {
            resetAppBarHeight();
        }
    }

    private void resetAppBarHeight() {
        if (appBarLayout != null) {
            ViewGroup.LayoutParams params = appBarLayout.getLayoutParams();
            int windowHeight = getWindowHeight();
            int bottomPadding;

            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                appBarLayout.setActivated(false);
                bottomPadding = 0;
                params.height = (int) getResources().getDimension(R.dimen.sesl_action_bar_default_height);
            } else {
                appBarLayout.setActivated(true);
                setExpanded(mExpanded, false);
                bottomPadding = getResources().getDimensionPixelSize(R.dimen.sesl_extended_appbar_bottom_padding);

                TypedValue outValue = new TypedValue();
                getResources().getValue(R.dimen.sesl_appbar_height_proportion, outValue, true);

                params.height = (int) ((float) windowHeight * outValue.getFloat());
            }

            appBarLayout.setLayoutParams(params);
            appBarLayout.setPadding(0, 0, 0, bottomPadding);
        } else
            Log.w(TAG + ".resetAppBarHeight", "appBarLayout is null.");
    }

    //
    // AppBar methods
    //
    public SamsungAppBarLayout getAppBarLayout() {
        return appBarLayout;
    }

    public MaterialToolbar getToolbar() {
        return toolbar;
    }

    public void setTitle(CharSequence title) {
        setTitle(title, title);
    }

    public void setTitle(CharSequence expandedTitle, CharSequence collapsedTitle) {
        mTitle = collapsedTitle;
        if (mExpandable) {
            collapsingToolbarLayout.setTitle(expandedTitle);
        }
        collapsedTitleView.setText(mTitle);
    }

    public void setSubtitle(CharSequence subtitle) {
        mSubtitle = subtitle;
        if (mExpandable) {
            collapsingToolbarLayout.setSubtitle(mSubtitle);
        } else
            Log.d(TAG + ".setAppBarSubtitle", "mExpandable is " + mExpandable);
    }

    public void setExpanded(boolean expanded, boolean animate) {
        if (mExpandable) {
            mExpanded = expanded;
            appBarLayout.setExpanded(expanded, animate);
        } else
            Log.d(TAG + ".setExpanded", "mExpandable is " + mExpandable);
    }

    //
    // Navigation Button methods
    //
    public void setNavigationButtonIcon(Drawable navigationIcon) {
        mNavigationIcon = navigationIcon;
        navigationButton.setImageDrawable(mNavigationIcon);
        setNavigationButtonVisible(navigationIcon != null);
    }

    public void setNavigationButtonVisible(boolean visible) {
        navigationButtonContainer.setVisibility(visible ? View.VISIBLE : View.GONE);
        toolbar.setPaddingRelative(visible ? 0 : getResources().getDimensionPixelSize(R.dimen.sesl_action_bar_content_inset), 0, 0, 0);
    }

    public void setNavigationButtonBadge(int count) {
        if (navigationBadgeBackground == null) {
            navigationBadgeBackground = (ViewGroup) ((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.navigation_button_badge_layout, navigationButtonContainer, false);
            navigationBadgeText = (TextView) navigationBadgeBackground.getChildAt(0);
            navigationBadgeText.setTextSize(0, (float) ((int) getResources().getDimension(R.dimen.sesl_menu_item_badge_text_size)));
            navigationButtonContainer.addView(navigationBadgeBackground);
        }
        if (navigationBadgeText != null) {
            if (count > 0) {
                if (count > 99) {
                    count = 99;
                }
                String countString = numberFormat.format((long) count);
                navigationBadgeText.setText(countString);
                int width = (int) (getResources().getDimension(R.dimen.sesl_badge_default_width) + (float) countString.length() * getResources().getDimension(R.dimen.sesl_badge_additional_width));
                ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) navigationBadgeBackground.getLayoutParams();
                lp.width = width;
                lp.height = (int) getResources().getDimension(R.dimen.sesl_menu_item_badge_size);
                navigationBadgeBackground.setLayoutParams(lp);
                navigationBadgeBackground.setVisibility(View.VISIBLE);
            } else if (count == N_BADGE) {
                navigationBadgeText.setText(getResources().getString(R.string.sesl_action_menu_overflow_badge_text_n));
                navigationBadgeBackground.setVisibility(View.VISIBLE);
            } else {
                navigationBadgeBackground.setVisibility(View.GONE);
            }
        }
    }

    public void setNavigationButtonTooltip(CharSequence tooltipText) {
        navigationButton.setTooltipText(tooltipText);
    }

    public void setNavigationOnClickListener(OnClickListener listener) {
        navigationButton.setOnClickListener(listener);
    }

    //
    // Overflow Buttons methods
    //
    public void addOverflowButton(int iconResId, int tooltipTextResId, View.OnClickListener listener) {
        addOverflowButton(false, iconResId, tooltipTextResId, listener);
    }

    public void addOverflowButton(boolean bigIcon, int iconResId, int tooltipTextResId, View.OnClickListener listener) {
        if (moreMenuPopupWindow != null)
            throw new RuntimeException("Can't add a new Overflow button! Please make sure to add it BEFORE initializing moreMenuPopupWindow.");

        ToolbarImageButton overflowButton = new ToolbarImageButton(mContext);

        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams((int) getResources().getDimension(bigIcon ? R.dimen.overflow_big_button_dimens : R.dimen.sesl_action_button_min_width), (int) getResources().getDimension(R.dimen.sesl_action_bar_default_height));

        overflowButton.setBackgroundResource(R.drawable.sesl_action_bar_item_background);
        overflowButton.setImageResource(iconResId);
        overflowButton.setOnClickListener(listener);
        overflowButton.setTooltipText(getResources().getString(tooltipTextResId));

        overflowContainer.addView(overflowButton, lp);
    }

    public ToolbarImageButton getOverflowIcon(int index) {
        if (overflowContainer != null && overflowContainer.getChildCount() != 0) {
            return (ToolbarImageButton) overflowContainer.getChildAt(index);
        } else {
            Log.w(TAG + ".getOverflowIcon", "overflowContainer is null or contains no icons.");
            return null;
        }
    }

    //
    // More Menu Popup methods
    //
    @SuppressLint("LongLogTag")
    public void showMoreMenuPopupWindow() {
        if (moreMenuPopupWindow != null || !moreMenuPopupWindow.isShowing())
            moreMenuPopupWindow.showAsDropDown(moreMenuPopupAnchor, moreMenuPopupOffX, 0);
        else
            Log.w(TAG + ".showMoreMenuPopupWindow", "moreMenuPopupWindow is null or already shown.");
    }

    @SuppressLint("LongLogTag")
    public void dismissMoreMenuPopupWindow() {
        if (moreMenuPopupWindow != null || moreMenuPopupWindow.isShowing()) {
            moreMenuPopupWindow.dismiss();
        } else
            Log.w(TAG + ".dismissMoreMenuPopupWindow", "moreMenuPopupWindow is null or already hidden.");
    }

    public void setMoreMenuButton(LinkedHashMap<String, Integer> linkedHashMap, AdapterView.OnItemClickListener ocl) {
        if (moreOverflowButton == null) {
            moreOverflowButtonContainer = new FrameLayout(mContext);
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.MATCH_PARENT);
            overflowContainer.addView(moreOverflowButtonContainer, lp);

            moreOverflowButton = new ToolbarImageButton(mContext);

            ViewGroup.LayoutParams lp2 = new ViewGroup.LayoutParams((int) getResources().getDimension(R.dimen.overflow_big_button_dimens), (int) getResources().getDimension(R.dimen.sesl_action_bar_default_height));

            moreOverflowButton.setBackgroundResource(R.drawable.sesl_action_bar_item_background);
            moreOverflowButton.setImageResource(R.drawable.sesl_ic_menu_overflow);
            moreOverflowButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showMoreMenuPopupWindow();
                }
            });
            moreOverflowButton.setTooltipText(getResources().getString(R.string.sesl_more_options));

            moreOverflowButtonContainer.addView(moreOverflowButton, lp2);
        }

        for (int i : linkedHashMap.values()) {
            if (i != 0) {
                initMoreMenuButtonBadge(i);
                break;
            }
        }

        initMoreMenuPopupWindow(linkedHashMap, ocl);
    }

    private void initMoreMenuPopupWindow(LinkedHashMap<String, Integer> linkedHashMap, AdapterView.OnItemClickListener ocl) {
        if (moreMenuPopupWindow != null) {
            if (moreMenuPopupWindow.isShowing()) {
                moreMenuPopupWindow.dismiss();
            }
            moreMenuPopupWindow = null;
        }
        ListView listView = new ListView(mContext);
        moreMenuPopupAdapter = new MoreMenuPopupAdapter(getActivity(), linkedHashMap);
        listView.setAdapter(moreMenuPopupAdapter);
        listView.setDivider(null);
        listView.setSelector(getResources().getDrawable(R.drawable.menu_popup_list_selector, mContext.getTheme()));
        listView.setOnItemClickListener(ocl);

        if (getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
            moreMenuPopupOffX = getMoreMenuPopupWidth(moreMenuPopupAdapter);
        } else {
            moreMenuPopupOffX = -getMoreMenuPopupWidth(moreMenuPopupAdapter);
        }

        moreMenuPopupWindow = new PopupWindow(listView);
        moreMenuPopupWindow.setWidth(getMoreMenuPopupWidth(moreMenuPopupAdapter));
        moreMenuPopupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        moreMenuPopupWindow.setAnimationStyle(R.style.MenuPopupAnimStyle);
        moreMenuPopupWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.sesl_menu_popup_background, mContext.getTheme()));
        moreMenuPopupWindow.setOutsideTouchable(true);
        moreMenuPopupWindow.setElevation(getDIPForPX(12));
        moreMenuPopupWindow.setFocusable(true);
        if (moreMenuPopupWindow.isClippingEnabled()) {
            moreMenuPopupWindow.setClippingEnabled(false);
        }
        moreMenuPopupWindow.getContentView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (keyEvent.getKeyCode() != KeyEvent.KEYCODE_MENU || keyEvent.getAction() != KeyEvent.ACTION_UP || !moreMenuPopupWindow.isShowing()) {
                    return false;
                }
                moreMenuPopupWindow.dismiss();
                return true;
            }
        });
        moreMenuPopupWindow.setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() != MotionEvent.ACTION_OUTSIDE) {
                    return false;
                }
                moreMenuPopupWindow.dismiss();
                return true;
            }
        });
    }

    private void initMoreMenuButtonBadge(int count) {
        if (moreOverflowBadgeBackground == null) {
            moreOverflowBadgeBackground = (ViewGroup) ((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.menu_button_badge_layout, overflowContainer, false);
            moreOverflowBadgeText = (TextView) moreOverflowBadgeBackground.getChildAt(0);
            moreOverflowBadgeText.setTextSize(0, (float) ((int) getResources().getDimension(R.dimen.sesl_menu_item_badge_text_size)));
            moreOverflowButtonContainer.addView(moreOverflowBadgeBackground);
        }
        if (moreOverflowBadgeText != null) {
            if (count > 0) {
                if (count > 99) {
                    count = 99;
                }
                String countString = numberFormat.format((long) count);
                moreOverflowBadgeText.setText(countString);
                int width = (int) (getResources().getDimension(R.dimen.sesl_badge_default_width) + (float) countString.length() * getResources().getDimension(R.dimen.sesl_badge_additional_width));
                ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) moreOverflowBadgeBackground.getLayoutParams();
                lp.width = width;
                lp.height = (int) getResources().getDimension(R.dimen.sesl_menu_item_badge_size);
                lp.setMargins(0, getResources().getDimensionPixelSize(R.dimen.sesl_menu_item_badge_top_margin), 0, 0);
                lp.setMarginStart((int) getDIPForPX(23));
                lp.setMarginEnd((int) getDIPForPX(7));
                moreOverflowBadgeBackground.setLayoutParams(lp);
                moreOverflowBadgeBackground.setVisibility(View.VISIBLE);
            } else if (count == N_BADGE) {
                moreOverflowBadgeText.setText(getResources().getString(R.string.sesl_action_menu_overflow_badge_text_n));
                moreOverflowBadgeBackground.setVisibility(View.VISIBLE);
            } else {
                moreOverflowBadgeBackground.setVisibility(View.GONE);
            }
        }
    }

    private int getMoreMenuPopupWidth(MoreMenuPopupAdapter adapter) {
        int makeMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        View view = null;
        ViewGroup viewGroup = null;
        int measuredWidth = 0;

        int i = 0;
        int count = adapter.getCount();

        while (i < count) {
            ViewGroup linearLayout;
            int itemViewType = adapter.getItemViewType(i);

            if (itemViewType != 0)
                view = null;

            if (viewGroup == null)
                linearLayout = new LinearLayout(mContext);
            else
                linearLayout = viewGroup;

            view = adapter.getView(i, view, linearLayout);
            view.measure(makeMeasureSpec, makeMeasureSpec);
            measuredWidth = view.getMeasuredWidth();
            if (measuredWidth <= 0) {
                measuredWidth = 0;
            }
            i++;
            viewGroup = linearLayout;
        }
        return measuredWidth;
    }

    @IntDef({APPBAR_LAYOUT, COLLAPSING_TOOLBAR, TOOLBAR, NAVIGATION_ICON, COLLAPSED_TITLE, CONTENT_LAYOUT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ToolbarLayoutView {
    }

    private class AppBarOffsetListener implements SamsungAppBarLayout.OnOffsetChangedListener {
        @SuppressLint("Range")
        @Override
        public void onOffsetChanged(SamsungAppBarLayout layout, int verticalOffset) {
            int layoutPosition = Math.abs(appBarLayout.getTop());
            float alphaRange = ((float) collapsingToolbarLayout.getHeight()) * 0.17999999f;
            float toolbarTitleAlphaStart = ((float) collapsingToolbarLayout.getHeight()) * 0.35f;

            if (appBarLayout.getHeight() <= ((int) getResources().getDimension(R.dimen.sesl_action_bar_height_with_padding))) {
                collapsedTitleView.setAlpha(1.0f);
            } else {
                float collapsedTitleAlpha = ((150.0f / alphaRange) * (((float) layoutPosition) - toolbarTitleAlphaStart));

                if (collapsedTitleAlpha >= 0.0f && collapsedTitleAlpha <= 255.0f) {
                    collapsedTitleAlpha /= 255.0f;
                    collapsedTitleView.setAlpha(collapsedTitleAlpha);
                } else if (collapsedTitleAlpha < 0.0f)
                    collapsedTitleView.setAlpha(0.0f);
                else
                    collapsedTitleView.setAlpha(1.0f);
            }
        }
    }

    private class MoreMenuPopupAdapter extends ArrayAdapter {
        ArrayList<String> itemTitle;
        ArrayList<Integer> badgeCount;
        Activity activity;

        public MoreMenuPopupAdapter(Activity instance, LinkedHashMap<String, Integer> linkedHashMap) {
            super(instance, 0);
            activity = instance;
            itemTitle = new ArrayList(linkedHashMap.keySet());
            badgeCount = new ArrayList(linkedHashMap.values());
        }

        public void setArrays(LinkedHashMap<String, Integer> linkedHashMap) {
            itemTitle = new ArrayList(linkedHashMap.keySet());
            badgeCount = new ArrayList(linkedHashMap.values());
        }

        @Override
        public int getCount() {
            return itemTitle.size();
        }

        @Override
        public Object getItem(int position) {
            return itemTitle.get(position);
        }

        @Override
        public View getView(int index, View view, ViewGroup parent) {
            PopupMenuItem itemVar;

            if (view == null) {
                view = ((LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.menu_popup_item_layout, parent, false);
                itemVar = new PopupMenuItem(this);
                itemVar.titleText = view.findViewById(R.id.more_menu_popup_title_text);
                itemVar.badgeIcon = view.findViewById(R.id.more_menu_popup_badge);
                view.setTag(itemVar);
            } else {
                itemVar = (PopupMenuItem) view.getTag();
            }

            itemVar.titleText.setText(itemTitle.get(index));
            if (badgeCount.get(index) > 0) {
                int count = badgeCount.get(index);
                if (count > 99) {
                    count = 99;
                }
                String countString = numberFormat.format((long) count);
                itemVar.badgeIcon.setText(countString);
                int width = (int) (getResources().getDimension(R.dimen.sesl_badge_default_width) + (float) countString.length() * getResources().getDimension(R.dimen.sesl_badge_additional_width));
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) itemVar.badgeIcon.getLayoutParams();
                lp.width = width;
                itemVar.badgeIcon.setLayoutParams(lp);
                itemVar.badgeIcon.setVisibility(View.VISIBLE);
            } else if (badgeCount.get(index) == N_BADGE) {
                itemVar.badgeIcon.setText("N");
                itemVar.badgeIcon.setVisibility(View.VISIBLE);
            } else {
                itemVar.badgeIcon.setVisibility(View.GONE);
            }

            if (getCount() <= 1) {
                view.setBackgroundResource(R.drawable.menu_popup_item_bg_all_round);
            } else if (index == 0) {
                view.setBackgroundResource(R.drawable.menu_popup_item_bg_top_round);
            } else if (index == getCount() - 1) {
                view.setBackgroundResource(R.drawable.menu_popup_item_bg_bottom_round);
            } else {
                view.setBackgroundResource(R.drawable.menu_popup_item_bg_no_round);
            }

            return view;
        }
    }

    private class PopupMenuItem {
        MoreMenuPopupAdapter adapter;
        TextView titleText;
        TextView badgeIcon;

        PopupMenuItem(MoreMenuPopupAdapter instance) {
            adapter = instance;
        }
    }

}
