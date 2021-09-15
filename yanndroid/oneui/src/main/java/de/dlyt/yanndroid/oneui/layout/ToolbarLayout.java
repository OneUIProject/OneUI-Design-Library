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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.IntDef;
import androidx.annotation.MenuRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.SupportMenuInflater;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuItemImpl;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textview.MaterialTextView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import de.dlyt.yanndroid.oneui.R;
import de.dlyt.yanndroid.oneui.sesl.appbar.SamsungAppBarLayout;
import de.dlyt.yanndroid.oneui.sesl.appbar.SamsungCollapsingToolbarLayout;
import de.dlyt.yanndroid.oneui.sesl.support.ViewSupport;
import de.dlyt.yanndroid.oneui.sesl.support.WindowManagerSupport;
import de.dlyt.yanndroid.oneui.sesl.utils.ReflectUtils;
import de.dlyt.yanndroid.oneui.sesl.widget.ActionModeBottomBarButton;
import de.dlyt.yanndroid.oneui.sesl.widget.PopupListView;
import de.dlyt.yanndroid.oneui.sesl.widget.ToolbarImageButton;
import de.dlyt.yanndroid.oneui.view.PopupMenu;

public class ToolbarLayout extends LinearLayout {
    public static final int N_BADGE = -1;
    public static final int APPBAR_LAYOUT = 0;
    public static final int COLLAPSING_TOOLBAR = 1;
    public static final int TOOLBAR = 2;
    public static final int NAVIGATION_BUTTON = 3;
    public static final int COLLAPSED_TITLE = 4;
    public static final int MAIN_CONTENT = 5;
    public static final int FOOTER_CONTENT = 6;
    private static String TAG = "ToolbarLayout";
    public ViewGroup navigationBadgeBackground;
    public TextView navigationBadgeText;
    public ViewGroup moreOverflowBadgeBackground;
    public TextView moreOverflowBadgeText;
    private Context mContext;
    private NumberFormat numberFormat = NumberFormat.getInstance(Locale.getDefault());
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
    private RoundLinearLayout mainContainer;
    private LinearLayout bottomContainer;

    private LinearLayout actionButtonContainer;
    private FrameLayout overflowButtonContainer;
    private ToolbarImageButton overflowButton;
    private PopupWindow overflowMenuPopupWindow = null;
    private OverflowMenuAdapter overflowMenuPopupAdapter;
    private View overflowMenuPopupAnchor = null;
    private int overflowMenuPopupOffX;
    private Menu menu;
    private HashMap<MenuItem, Integer> overflowBadges = new HashMap<>();
    private OnMenuItemClickListener onMenuItemClickListener = item -> {
    };

    private Menu bottomMenu;
    private OnMenuItemClickListener onActionModeItemClickListener = item -> {
    };

    private boolean mActionMode = false;
    private RelativeLayout checkbox_withtext;
    private CheckBox checkbox_all;

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
        }
        toolbar = findViewById(R.id.toolbar_layout_toolbar);

        navigationButtonContainer = findViewById(R.id.toolbar_layout_navigationButton_container);
        navigationButton = findViewById(R.id.toolbar_layout_navigationButton);
        collapsedTitleView = findViewById(R.id.toolbar_layout_collapsed_title);
        actionButtonContainer = findViewById(R.id.toolbar_layout_overflow_container);
        overflowMenuPopupAnchor = findViewById(R.id.toolbar_layout_popup_window_anchor);

        mainContainer = findViewById(R.id.toolbar_layout_main_container);
        bottomContainer = findViewById(R.id.toolbar_layout_footer);

        checkbox_withtext = findViewById(R.id.checkbox_withtext);
        checkbox_all = findViewById(R.id.checkbox_all);

        getActivity().setSupportActionBar(toolbar);
        getActivity().getSupportActionBar().setDisplayShowTitleEnabled(false);
        getActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        setNavigationButtonIcon(mNavigationIcon);
        setTitle(mTitle);
        setSubtitle(mSubtitle);

        if (mExpandable)
            appBarLayout.addOnOffsetChangedListener(new AppBarOffsetListener());

        refreshLayout(getResources().getConfiguration());

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
                    setCustomTitleView(child, new SamsungCollapsingToolbarLayout.LayoutParams(params));
                    break;
                case 2:
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

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        refreshLayout(newConfig);
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
        ViewSupport.updateListBothSideMargin(getActivity(), findViewById(R.id.toolbar_layout_bottom_corners));
        ViewSupport.updateListBothSideMargin(getActivity(), findViewById(R.id.toolbar_layout_footer_container));

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

    public void setCustomTitleView(View view) {
        setCustomTitleView(view, new SamsungCollapsingToolbarLayout.LayoutParams(view.getLayoutParams()));
    }

    public void setCustomTitleView(View view, SamsungCollapsingToolbarLayout.LayoutParams params) {
        if (params == null) {
            params = new SamsungCollapsingToolbarLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        params.seslSetIsTitleCustom(true);
        collapsingToolbarLayout.seslSetCustomTitleView(view, params);
    }

    //
    // Action Mode methods
    //
    public void showActionMode() {
        mActionMode = true;
        setNavigationButtonVisible(false);
        checkbox_withtext.setVisibility(View.VISIBLE);
        setActionModeSelectCount(0);
        overflowButton.setVisibility(GONE);
        actionButtonContainer.setVisibility(GONE);

        if (bottomMenu != null) {
            bottomContainer.setVisibility(GONE);
            findViewById(R.id.toolbar_layout_footer_action_mode).setVisibility(VISIBLE);
        }
    }

    @SuppressLint("RestrictedApi")
    public void setActionModeBottomMenu(@MenuRes int menuRes, OnMenuItemClickListener listener) {
        LinearLayout footer_action_mode = findViewById(R.id.toolbar_layout_footer_action_mode);
        footer_action_mode.removeAllViews();

        onActionModeItemClickListener = listener;

        bottomMenu = new MenuBuilder(getContext());
        MenuInflater menuInflater = new SupportMenuInflater(getContext());
        menuInflater.inflate(menuRes, bottomMenu);

        ArrayList<MenuItem> bottomItems = new ArrayList<>();

        for (int i = 0; i < bottomMenu.size(); i++) {
            MenuItem item = bottomMenu.getItem(i);
            if (((MenuItemImpl) item).requiresActionButton()) {
                ActionModeBottomBarButton button = new ActionModeBottomBarButton(mContext);
                button.setText(item.getTitle());
                button.setIcon(item.getIcon());
                button.setOnClickListener(v -> {
                    onActionModeItemClickListener.onMenuItemClick(item);
                });
                footer_action_mode.addView(button);
            } else {
                bottomItems.add(item);
            }
        }

        if (!bottomItems.isEmpty()) {
            ActionModeBottomBarButton button = new ActionModeBottomBarButton(mContext);
            button.setText(getResources().getString(R.string.sesl_more_item_label));
            button.setIcon(getResources().getDrawable(R.drawable.ic_samsung_more, getContext().getTheme()));
            footer_action_mode.addView(button);

            PopupMenu popupMenu = new PopupMenu(button);
            popupMenu.inflate(bottomItems);
            popupMenu.setOnMenuItemClickListener(item -> {
                popupMenu.dismiss();
                onActionModeItemClickListener.onMenuItemClick(item);
            });

            button.setOnClickListener(v -> {
                popupMenu.show();
            });

        }

    }

    public Menu getActionModeBottomMenu() {
        return bottomMenu;
    }

    public void dismissActionMode() {
        mActionMode = false;
        setNavigationButtonVisible(true);
        checkbox_withtext.setVisibility(View.GONE);
        setTitle(mTitle);
        overflowButton.setVisibility(VISIBLE);
        actionButtonContainer.setVisibility(VISIBLE);

        bottomContainer.setVisibility(VISIBLE);
        findViewById(R.id.toolbar_layout_footer_action_mode).setVisibility(GONE);
    }

    public void setActionModeSelectCount(int count) {
        String title = count > 0 ? getResources().getString(R.string.selected_check_info, count) : getResources().getString(R.string.settings_import_select_items);
        if (mExpandable) collapsingToolbarLayout.setTitle(title);
        collapsedTitleView.setText(title);

        if (bottomMenu != null)
            findViewById(R.id.toolbar_layout_footer_action_mode).setVisibility(count > 0 ? VISIBLE : GONE);
    }

    public void setActionModeSelectAllCheckedChangeListener(CompoundButton.OnCheckedChangeListener listener) {
        checkbox_all.setOnCheckedChangeListener(listener);
    }

    public void setActionModeSelectAllChecked(boolean checked) {
        checkbox_all.setChecked(checked);
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
        toolbar.setPaddingRelative(0, 0, 0, 0);
        toolbar.setPaddingRelative(visible || mActionMode ? 0 : getResources().getDimensionPixelSize(R.dimen.sesl_action_bar_content_inset), 0, 0, 0);
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
    // Menu methods
    //
    public interface OnMenuItemClickListener {
        void onMenuItemClick(MenuItem item);
    }

    public void setOnMenuItemClickListener(OnMenuItemClickListener listener) {
        onMenuItemClickListener = listener;
    }

    public Menu getMenu() {
        return menu;
    }

    @SuppressLint("RestrictedApi")
    public void inflateMenu(@MenuRes int resId) {
        actionButtonContainer.removeAllViews();
        overflowButton = null;

        menu = new MenuBuilder(getContext());
        MenuInflater menuInflater = new SupportMenuInflater(getContext());
        menuInflater.inflate(resId, menu);

        ArrayList<MenuItem> overflowItems = new ArrayList<>();

        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            if (((MenuItemImpl) item).requiresActionButton()) {
                addActionButton(item);
            } else {
                overflowBadges.put(item, 0);
                overflowItems.add(item);
            }
        }

        if (!overflowItems.isEmpty()) setOverflowMenu(overflowItems);
    }

    private void addActionButton(MenuItem item) {
        if (actionButtonContainer.getChildCount() != 0) {
            for (int i = 0; i < actionButtonContainer.getChildCount(); i++) {
                ToolbarImageButton previousBtn = getOverflowIcon(i);
                ViewGroup.LayoutParams lp = previousBtn.getLayoutParams();
                lp.width = getResources().getDimensionPixelSize(R.dimen.overflow_button_size);
                previousBtn.setPaddingRelative(getResources().getDimensionPixelSize(R.dimen.sesl_action_button_padding_horizontal), 0, getResources().getDimensionPixelSize(R.dimen.sesl_action_button_padding_horizontal), 0);
            }
        }

        ToolbarImageButton overflowButton = new ToolbarImageButton(mContext);

        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(getResources().getDimensionPixelSize(R.dimen.sesl_overflow_button_min_width), getResources().getDimensionPixelSize(R.dimen.sesl_action_bar_default_height));

        overflowButton.setBackgroundResource(R.drawable.sesl_action_bar_item_background);
        overflowButton.setImageDrawable(item.getIcon());
        overflowButton.setPaddingRelative(getResources().getDimensionPixelSize(R.dimen.sesl_action_bar_overflow_padding_start), 0, getResources().getDimensionPixelSize(R.dimen.sesl_action_bar_overflow_padding_end), 0);
        overflowButton.setTooltipText(item.getTitle());
        overflowButton.setOnClickListener(v -> onMenuItemClickListener.onMenuItemClick(item));

        actionButtonContainer.addView(overflowButton, lp);
    }

    private void setOverflowMenu(ArrayList<MenuItem> overflowItems) {
        if (overflowButton == null) {
            if (actionButtonContainer.getChildCount() != 0) {
                for (int i = 0; i < actionButtonContainer.getChildCount(); i++) {
                    ToolbarImageButton previousBtn = getOverflowIcon(i);
                    ViewGroup.LayoutParams lp = previousBtn.getLayoutParams();
                    lp.width = getResources().getDimensionPixelSize(R.dimen.overflow_button_size);
                    previousBtn.setPaddingRelative(getResources().getDimensionPixelSize(R.dimen.sesl_action_button_padding_horizontal), 0, getResources().getDimensionPixelSize(R.dimen.sesl_action_button_padding_horizontal), 0);
                }
            }

            overflowButtonContainer = new FrameLayout(mContext);
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.MATCH_PARENT);
            actionButtonContainer.addView(overflowButtonContainer, lp);

            overflowButton = new ToolbarImageButton(mContext);

            ViewGroup.LayoutParams lp2 = new ViewGroup.LayoutParams(getResources().getDimensionPixelSize(R.dimen.sesl_overflow_button_min_width), getResources().getDimensionPixelSize(R.dimen.sesl_action_bar_default_height));

            overflowButton.setBackgroundResource(R.drawable.sesl_action_bar_item_background);
            overflowButton.setImageResource(R.drawable.sesl_ic_menu_overflow);
            overflowButton.setPaddingRelative(getResources().getDimensionPixelSize(R.dimen.sesl_action_bar_overflow_padding_start), 0, getResources().getDimensionPixelSize(R.dimen.sesl_action_bar_overflow_padding_end), 0);
            overflowButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showOverflowMenu();
                }
            });
            overflowButton.setTooltipText(getResources().getString(R.string.sesl_more_options));

            overflowButtonContainer.addView(overflowButton, lp2);
        }

        if (overflowMenuPopupWindow != null) {
            if (overflowMenuPopupWindow.isShowing()) {
                overflowMenuPopupWindow.dismiss();
            }
            overflowMenuPopupWindow = null;
        }
        PopupListView listView = new PopupListView(mContext);
        overflowMenuPopupAdapter = new OverflowMenuAdapter(getActivity(), overflowItems);
        listView.setAdapter(overflowMenuPopupAdapter);
        listView.setMaxHeight(getResources().getDimensionPixelSize(R.dimen.sesl_menu_popup_max_height));
        listView.setDivider(null);
        listView.setSelector(getResources().getDrawable(R.drawable.sesl_list_selector, mContext.getTheme()));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onMenuItemClickListener.onMenuItemClick((MenuItem) overflowMenuPopupAdapter.getItem(position));
            }
        });

        if (getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
            overflowMenuPopupOffX = getOverflowMenuPopupWidth(overflowMenuPopupAdapter);
        } else {
            overflowMenuPopupOffX = -getOverflowMenuPopupWidth(overflowMenuPopupAdapter);
        }

        overflowMenuPopupWindow = new PopupWindow(listView);
        overflowMenuPopupWindow.setWidth(getOverflowMenuPopupWidth(overflowMenuPopupAdapter));
        overflowMenuPopupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        overflowMenuPopupWindow.setAnimationStyle(R.style.MenuPopupAnimStyle);
        overflowMenuPopupWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.sesl_menu_popup_background, mContext.getTheme()));
        overflowMenuPopupWindow.setOutsideTouchable(true);
        overflowMenuPopupWindow.setElevation(getDIPForPX(12));
        overflowMenuPopupWindow.setFocusable(true);
        if (overflowMenuPopupWindow.isClippingEnabled()) {
            overflowMenuPopupWindow.setClippingEnabled(false);
        }
        overflowMenuPopupWindow.getContentView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (keyEvent.getKeyCode() != KeyEvent.KEYCODE_MENU || keyEvent.getAction() != KeyEvent.ACTION_UP || !overflowMenuPopupWindow.isShowing()) {
                    return false;
                }
                overflowMenuPopupWindow.dismiss();
                return true;
            }
        });
        overflowMenuPopupWindow.setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() != MotionEvent.ACTION_OUTSIDE) {
                    return false;
                }
                overflowMenuPopupWindow.dismiss();
                return true;
            }
        });
    }

    @SuppressLint("RestrictedApi")
    public void setOverflowMenuBadge(MenuItem item, Integer badge) {
        if (!((MenuItemImpl) item).requiresActionButton()) {
            overflowBadges.put(item, badge);
            overflowMenuPopupAdapter.notifyDataSetChanged();

            int count = 0;
            boolean n = false;
            for (Integer i : overflowBadges.values()) if (i > 0) count += i;
            showOverflowMenuButtonBadge(count == 0 ? (n ? N_BADGE : 0) : count);

            overflowMenuPopupWindow.setWidth(getOverflowMenuPopupWidth(overflowMenuPopupAdapter));
            if (overflowMenuPopupWindow.isShowing()) overflowMenuPopupWindow.dismiss();
        }
    }

    public Integer getOverflowMenuBadge(MenuItem item) {
        return overflowBadges.get(item);
    }

    private void showOverflowMenuButtonBadge(int count) {
        if (moreOverflowBadgeBackground == null) {
            moreOverflowBadgeBackground = (ViewGroup) ((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.sesl_action_menu_item_badge, overflowButtonContainer, false);
            moreOverflowBadgeText = (TextView) moreOverflowBadgeBackground.getChildAt(0);
            overflowButtonContainer.addView(moreOverflowBadgeBackground);
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
                lp.setMarginEnd(getResources().getDimensionPixelSize(R.dimen.sesl_menu_item_badge_end_margin) - (int) getDIPForPX(4));
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

    @SuppressLint("LongLogTag")
    public void showOverflowMenu() {
        if (overflowMenuPopupWindow != null && !overflowMenuPopupWindow.isShowing()) {
            overflowMenuPopupWindow.showAsDropDown(overflowMenuPopupAnchor, overflowMenuPopupOffX, 0);
            ((View) ReflectUtils.genericGetField(overflowMenuPopupWindow, "mBackgroundView")).setClipToOutline(true);
        } else
            Log.w(TAG + ".showMoreMenuPopupWindow", "moreMenuPopupWindow is null or already shown.");
    }

    @SuppressLint("LongLogTag")
    public void dismissOverflowMenu() {
        if (overflowMenuPopupWindow != null && overflowMenuPopupWindow.isShowing()) {
            overflowMenuPopupWindow.dismiss();
        } else
            Log.w(TAG + ".dismissMoreMenuPopupWindow", "moreMenuPopupWindow is null or already hidden.");
    }

    private ToolbarImageButton getOverflowIcon(int index) {
        if (actionButtonContainer != null && actionButtonContainer.getChildCount() != 0) {
            return (ToolbarImageButton) actionButtonContainer.getChildAt(index);
        } else {
            Log.w(TAG + ".getOverflowIcon", "overflowContainer is null or contains no icons.");
            return null;
        }
    }

    private int getOverflowMenuPopupWidth(OverflowMenuAdapter adapter) {
        int makeMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int popupWidth = 0;

        for (int i = 0; i < adapter.getCount(); i++) {
            View view = adapter.getView(i, null, new LinearLayout(mContext));
            view.measure(makeMeasureSpec, makeMeasureSpec);
            int measuredWidth = view.getMeasuredWidth();
            if (measuredWidth > popupWidth) {
                popupWidth = measuredWidth;
            }
        }
        return popupWidth;

    }

    private class OverflowMenuAdapter extends ArrayAdapter {
        Activity activity;
        ArrayList<MenuItem> overflowItems;

        public OverflowMenuAdapter(Activity instance, ArrayList<MenuItem> overflowItems) {
            super(instance, 0);
            this.activity = instance;
            this.overflowItems = overflowItems;
        }

        @Override
        public int getCount() {
            return overflowItems.size();
        }

        @Override
        public Object getItem(int position) {
            return overflowItems.get(position);
        }

        @Override
        public View getView(int index, View view, ViewGroup parent) {
            TextView titleText;
            TextView badgeIcon;

            view = ((LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.menu_popup_item_layout, parent, false);
            titleText = view.findViewById(R.id.more_menu_popup_title_text);
            titleText.setText(overflowItems.get(index).getTitle());

            badgeIcon = view.findViewById(R.id.more_menu_popup_badge);
            Integer badgeCount = overflowBadges.get(overflowItems.get(index));

            if (badgeCount > 0) {
                int count = badgeCount;
                if (count > 99) {
                    count = 99;
                }
                String countString = numberFormat.format((long) count);
                badgeIcon.setText(countString);
                int width = (int) (getResources().getDimension(R.dimen.sesl_badge_default_width) + (float) countString.length() * getResources().getDimension(R.dimen.sesl_badge_additional_width));
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) badgeIcon.getLayoutParams();
                lp.width = width;
                badgeIcon.setLayoutParams(lp);
                badgeIcon.setVisibility(View.VISIBLE);
            } else if (badgeCount == N_BADGE) {
                badgeIcon.setText("N");
                badgeIcon.setVisibility(View.VISIBLE);
            } else {
                badgeIcon.setVisibility(View.GONE);
            }

            return view;
        }
    }


    public View getView(@ToolbarLayoutView int view) {
        switch (view) {
            case APPBAR_LAYOUT:
                return appBarLayout;
            case COLLAPSING_TOOLBAR:
                return collapsingToolbarLayout;
            case TOOLBAR:
                return toolbar;
            case NAVIGATION_BUTTON:
                return navigationButton;
            case COLLAPSED_TITLE:
                return collapsedTitleView;
            case MAIN_CONTENT:
                return mainContainer;
            case FOOTER_CONTENT:
                return bottomContainer;
            default:
                return null;
        }
    }

    @IntDef({APPBAR_LAYOUT, COLLAPSING_TOOLBAR, TOOLBAR, NAVIGATION_BUTTON, COLLAPSED_TITLE, MAIN_CONTENT, FOOTER_CONTENT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ToolbarLayoutView {
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

}
