package de.dlyt.yanndroid.oneui.layout;

import static android.content.Context.INPUT_METHOD_SERVICE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResult;
import androidx.annotation.MenuRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textview.MaterialTextView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

import de.dlyt.yanndroid.oneui.R;
import de.dlyt.yanndroid.oneui.menu.ActionButtonMenuItemView;
import de.dlyt.yanndroid.oneui.menu.BottomBarMenuItemView;
import de.dlyt.yanndroid.oneui.menu.Menu;
import de.dlyt.yanndroid.oneui.menu.MenuItem;
import de.dlyt.yanndroid.oneui.menu.PopupMenu;
import de.dlyt.yanndroid.oneui.sesl.appbar.SamsungAppBarLayout;
import de.dlyt.yanndroid.oneui.sesl.appbar.SamsungCollapsingToolbarLayout;
import de.dlyt.yanndroid.oneui.sesl.support.ViewSupport;
import de.dlyt.yanndroid.oneui.sesl.support.WindowManagerSupport;
import de.dlyt.yanndroid.oneui.sesl.widget.ToolbarImageButton;
import de.dlyt.yanndroid.oneui.widget.RoundLinearLayout;

public class ToolbarLayout extends LinearLayout {
    public static final int N_BADGE = -1;
    private static String TAG = "ToolbarLayout";
    private boolean mIsOneUI4;
    public ViewGroup navigationBadgeBackground;
    public TextView navigationBadgeText;
    private Context mContext;
    private AppCompatActivity mActivity;
    private NumberFormat numberFormat = NumberFormat.getInstance(Locale.getDefault());
    private Drawable mNavigationIcon;
    private int mLayout;
    private CharSequence mTitleExpanded;
    private CharSequence mTitleCollapsed;
    private CharSequence mSubtitle;
    private Boolean mExpandable;
    private Boolean mExpanded;
    private LinearLayout root_layout;
    private SamsungAppBarLayout appBarLayout;
    private SamsungCollapsingToolbarLayout collapsingToolbarLayout;
    private MaterialToolbar toolbar;
    private FrameLayout navigationButtonContainer;
    private ToolbarImageButton navigationButton;
    private MaterialTextView collapsedTitleView;
    private MaterialTextView collapsedSubTitleView;
    private RoundLinearLayout mainContainer;
    private LinearLayout bottomContainer;
    private OnBackPressedCallback onBackPressedCallback;
    private DrawerLayout drawerLayout;
    private boolean navigationButtonVisible;

    public interface OnMenuItemClickListener {
        boolean onMenuItemClick(MenuItem item);
    }

    //toolbar menu
    private LinearLayout actionButtonContainer;
    private Menu toolbarMenu;
    private OnMenuItemClickListener onToolbarMenuItemClickListener = item -> true;

    //select mode
    private Menu selectModeBottomMenu;
    private boolean mSelectMode = false;
    private LinearLayout selectModeCheckboxContainer;
    private CheckBox selectModeCheckbox;
    private OnMenuItemClickListener onSelectModeBottomMenuItemClickListener = item -> true;

    //search mode
    private boolean mSearchMode = false;
    private LinearLayout main_toolbar;
    private LinearLayout search_toolbar;
    private ToolbarImageButton search_navButton;
    private ToolbarImageButton search_action_button;
    private EditText search_edittext;
    private SearchModeListener searchModeListener = new SearchModeListener() {
        @Override
        public void onSearchOpened(EditText search_edittext) {
        }

        @Override
        public void onSearchDismissed(EditText search_edittext) {
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        public void afterTextChanged(Editable s) {
        }

        public void onKeyboardSearchClick(CharSequence s) {
        }

        @Override
        public void onVoiceInputClick(Intent intent) {
        }
    };

    public interface SearchModeListener {
        void onSearchOpened(EditText search_edittext);

        void onSearchDismissed(EditText search_edittext);

        void beforeTextChanged(CharSequence s, int start, int count, int after);

        void onTextChanged(CharSequence s, int start, int before, int count);

        void afterTextChanged(Editable s);

        void onKeyboardSearchClick(CharSequence s);

        void onVoiceInputClick(Intent intent);
    }

    public ToolbarLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        mContext = context;
        mActivity = getActivity();

        mIsOneUI4 = context.getTheme().obtainStyledAttributes(new int[]{R.attr.isOneUI4}).getBoolean(0, false);

        TypedArray attr = mContext.getTheme().obtainStyledAttributes(attrs, R.styleable.ToolBarLayout, 0, 0);

        try {
            mExpandable = attr.getBoolean(R.styleable.ToolBarLayout_expandable, true);
            mExpanded = attr.getBoolean(R.styleable.ToolBarLayout_expanded, true);
            mLayout = attr.getResourceId(R.styleable.ToolBarLayout_android_layout, mExpandable ? R.layout.samsung_appbar_toolbarlayout : R.layout.samsung_toolbar_toolbarlayout);
            mTitleExpanded = attr.getString(R.styleable.ToolBarLayout_title);
            mSubtitle = attr.getString(R.styleable.ToolBarLayout_subtitle);
            mNavigationIcon = attr.getDrawable(R.styleable.ToolBarLayout_navigationIcon);
        } finally {
            attr.recycle();
        }

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(mLayout, this, true);

        root_layout = findViewById(R.id.toolbar_layout_root);
        if (mExpandable) {
            appBarLayout = findViewById(R.id.toolbar_layout_app_bar);
            collapsingToolbarLayout = findViewById(R.id.toolbar_layout_collapsing_toolbar_layout);
            appBarLayout.setExpanded(mExpanded, false);
        }
        toolbar = findViewById(R.id.toolbar_layout_toolbar);

        navigationButtonContainer = findViewById(R.id.toolbar_layout_navigationButton_container);
        navigationButton = findViewById(R.id.toolbar_layout_navigationButton);
        collapsedTitleView = findViewById(R.id.toolbar_layout_collapsed_title);
        collapsedSubTitleView = findViewById(R.id.toolbar_layout_collapsed_subtitle);
        actionButtonContainer = findViewById(R.id.toolbar_layout_action_menu_item_container);

        mainContainer = findViewById(R.id.toolbar_layout_main_container);
        bottomContainer = findViewById(R.id.toolbar_layout_footer);

        selectModeCheckboxContainer = findViewById(R.id.checkbox_withtext);
        selectModeCheckbox = findViewById(R.id.checkbox_all);
        selectModeCheckboxContainer.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                selectModeCheckbox.setChecked(!selectModeCheckbox.isChecked());
            }
        });

        main_toolbar = findViewById(R.id.toolbar_layout_main_toolbar);
        search_toolbar = findViewById(R.id.toolbar_layout_search_toolbar);
        search_navButton = findViewById(R.id.toolbar_layout_search_navigationButton);
        search_action_button = findViewById(R.id.search_view_action_button);
        search_edittext = findViewById(R.id.toolbar_layout_search_field);

        setNavigationButtonIcon(mNavigationIcon);
        setTitle(mTitleExpanded);
        setSubtitle(mSubtitle);

        if (mExpandable) {
            appBarLayout.addOnOffsetChangedListener(new AppBarOffsetListener());
        } else {
            findViewById(R.id.toolbar_layout_collapsed_title_container).setAlpha(1.0f);
        }

        /*back logic*/
        onBackPressedCallback = new OnBackPressedCallback(false) {
            @Override
            public void handleOnBackPressed() {
                if (mSearchMode) dismissSearchMode();
            }
        };

        if (!isInEditMode()){
            mActivity.setSupportActionBar(toolbar);
            mActivity.getSupportActionBar().setDisplayShowTitleEnabled(false);
            mActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            mActivity.getOnBackPressedDispatcher().addCallback(onBackPressedCallback);
        }

        refreshLayout(getResources().getConfiguration());
    }

    void syncWithDrawer(DrawerLayout drawerLayout) {
        this.drawerLayout = drawerLayout;
    }

    private void lockDrawerIfAvailable(boolean lock) {
        if (drawerLayout != null) {
            if (lock) {
                ((androidx.drawerlayout.widget.DrawerLayout) drawerLayout.findViewById(R.id.drawerLayout)).setDrawerLockMode(androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            } else {
                ((androidx.drawerlayout.widget.DrawerLayout) drawerLayout.findViewById(R.id.drawerLayout)).setDrawerLockMode(androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_UNLOCKED);
            }
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
                    setCustomTitleView(child, new SamsungCollapsingToolbarLayout.LayoutParams(params));
                    break;
                case 2:
                    bottomContainer.addView(child, index, params);
                    break;
                case 3:
                    root_layout.addView(child, index, params);
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
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        resetToolbarHeight();
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        refreshLayout(newConfig);
        resetToolbarHeight();
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

    private int getToolbarTopPadding() {
        return mIsOneUI4 ? mContext.getResources().getDimensionPixelSize(R.dimen.sesl4_action_bar_top_padding) : 0;
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
        if (!isInEditMode()) WindowManagerSupport.hideStatusBarForLandscape(mActivity, newConfig.orientation);

        ViewSupport.updateListBothSideMargin(mActivity, mainContainer);
        ViewSupport.updateListBothSideMargin(mActivity, findViewById(R.id.toolbar_layout_bottom_corners));
        ViewSupport.updateListBothSideMargin(mActivity, findViewById(R.id.toolbar_layout_footer_container));

        updateCollapsedSubtitleVisibility();
    }

    private void resetToolbarHeight() {
        if (toolbar != null) {
            toolbar.setPaddingRelative(mSelectMode || mSearchMode || navigationButtonVisible ? 0 : getResources().getDimensionPixelSize(R.dimen.sesl_action_bar_content_inset), getToolbarTopPadding(), 0, 0);

            ViewGroup.LayoutParams lp = toolbar.getLayoutParams();
            lp.height = mContext.getResources().getDimensionPixelSize(mIsOneUI4 ? R.dimen.sesl4_action_bar_default_height : R.dimen.sesl_action_bar_default_height) + getToolbarTopPadding();
            toolbar.setLayoutParams(lp);
        } else
            Log.w(TAG + ".resetToolbarHeight", "toolbar is null.");
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
        mTitleCollapsed = collapsedTitle;
        mTitleExpanded = expandedTitle;
        if (mExpandable) {
            collapsingToolbarLayout.setTitle(expandedTitle);
        }
        collapsedTitleView.setText(collapsedTitle);
    }

    public void setSubtitle(CharSequence subtitle) {
        mSubtitle = subtitle;
        if (mExpandable) {
            collapsingToolbarLayout.setSubtitle(subtitle);
        }
        collapsedSubTitleView.setText(subtitle);

        updateCollapsedSubtitleVisibility();
    }

    private void updateCollapsedSubtitleVisibility() {
        TypedValue outValue = new TypedValue();
        getResources().getValue(mIsOneUI4 ? R.dimen.sesl4_appbar_height_proportion : R.dimen.sesl_appbar_height_proportion, outValue, true);
        if (!mExpandable || outValue.getFloat() == 0.0) {
            collapsedSubTitleView.setVisibility((mSubtitle != null && mSubtitle.length() != 0) ? VISIBLE : GONE);
        } else {
            collapsedSubTitleView.setVisibility(GONE);
        }
    }

    public void setExpanded(boolean expanded, boolean animate) {
        if (mExpandable) {
            mExpanded = expanded;
            appBarLayout.setExpanded(expanded, animate);
        } else
            Log.d(TAG + ".setExpanded", "mExpandable is " + mExpandable);
    }

    public boolean isExpanded() {
        return mExpandable ? !appBarLayout.seslIsCollapsed() : false;
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
    // Select Mode methods
    //
    public void showSelectMode() {
        mSelectMode = true;
        lockDrawerIfAvailable(true);
        if (mSearchMode) dismissSearchMode();
        setNavigationButtonVisible(false);
        selectModeCheckboxContainer.setVisibility(View.VISIBLE);
        selectModeCheckbox.setChecked(false);
        selectModeCheckbox.jumpDrawablesToCurrentState();
        setSelectModeCount(0);
        actionButtonContainer.setVisibility(GONE);

        if (selectModeBottomMenu != null) {
            bottomContainer.setVisibility(GONE);
            findViewById(R.id.toolbar_layout_footer_action_mode).setVisibility(VISIBLE);
        }
    }

    public void setSelectModeBottomMenu(@MenuRes int menuRes, OnMenuItemClickListener listener) {
        setSelectModeBottomMenu(new Menu(menuRes, mContext), listener);
    }

    public void setSelectModeBottomMenu(Menu menu, OnMenuItemClickListener listener) {
        LinearLayout footer_action_mode = findViewById(R.id.toolbar_layout_footer_action_mode);
        footer_action_mode.removeAllViews();

        onSelectModeBottomMenuItemClickListener = listener;
        selectModeBottomMenu = menu;
        Menu overflowMenu = new Menu();

        for (MenuItem menuItem : selectModeBottomMenu.menuItems) {
            if (menuItem.isActionButton()) {
                BottomBarMenuItemView button = new BottomBarMenuItemView(mContext, menuItem);
                button.setOnClickListener(v -> onSelectModeBottomMenuItemClickListener.onMenuItemClick(menuItem));
                menuItem.setMenuItemListener(menuItem1 -> button.updateView());
                footer_action_mode.addView(button);
            } else {
                overflowMenu.addMenuItem(menuItem);
            }
        }

        if (overflowMenu.size() > 0) {
            BottomBarMenuItemView moreButton = new BottomBarMenuItemView(mContext, getResources().getString(R.string.sesl_more_item_label), R.drawable.ic_samsung_more);
            footer_action_mode.addView(moreButton);

            PopupMenu overflowPopupMenu = new PopupMenu(moreButton);
            overflowPopupMenu.inflate(overflowMenu);
            overflowPopupMenu.setAnimationStyle(R.style.BottomMenuPopupAnimStyle);
            overflowPopupMenu.setPopupMenuListener(new PopupMenu.PopupMenuListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    return onSelectModeBottomMenuItemClickListener.onMenuItemClick(item);
                }

                @Override
                public void onMenuItemUpdate(MenuItem menuItem) {

                }
            });

            moreButton.setOnClickListener(v -> {
                int xoff = overflowPopupMenu.getPopupMenuWidth() - moreButton.getWidth() + 7;
                if (getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
                    overflowPopupMenu.show(xoff, 0);
                } else {
                    overflowPopupMenu.show(-xoff, 0);
                }
            });
        }

    }

    public Menu getSelectModeBottomMenu() {
        return selectModeBottomMenu != null ? selectModeBottomMenu : (selectModeBottomMenu = new Menu());
    }

    public void dismissSelectMode() {
        mSelectMode = false;
        lockDrawerIfAvailable(false);
        setNavigationButtonVisible(navigationButtonVisible);
        selectModeCheckboxContainer.setVisibility(View.GONE);
        setTitle(mTitleExpanded, mTitleCollapsed);
        setSubtitle(mSubtitle);
        actionButtonContainer.setVisibility(VISIBLE);

        bottomContainer.setVisibility(VISIBLE);
        findViewById(R.id.toolbar_layout_footer_action_mode).setVisibility(GONE);
    }

    public void setSelectModeCount(int count) {
        String title = count > 0 ? getResources().getString(R.string.selected_check_info, count) : getResources().getString(R.string.settings_import_select_items);

        if (mExpandable) {
            collapsingToolbarLayout.setTitle(title);
        }
        collapsedTitleView.setText(title);

        if (mSubtitle != null && mSubtitle.length() != 0) {
            if (mExpandable) {
                collapsingToolbarLayout.setSubtitle(null);
            }
            collapsedSubTitleView.setText(null);
            collapsedSubTitleView.setVisibility(GONE);
        }

        if (selectModeBottomMenu != null) {
            findViewById(R.id.toolbar_layout_footer_action_mode).setVisibility(count > 0 ? VISIBLE : GONE);
        }
    }

    public void setSelectModeAllCheckedChangeListener(CompoundButton.OnCheckedChangeListener listener) {
        selectModeCheckbox.setOnCheckedChangeListener(listener);
    }

    public void setSelectModeAllChecked(boolean checked) {
        selectModeCheckbox.setChecked(checked);
    }


    //
    // Search Mode methods
    //
    public void showSearchMode() {
        mSearchMode = true;
        lockDrawerIfAvailable(true);
        setNavigationButtonVisible(false);
        onBackPressedCallback.setEnabled(true);
        if (mSelectMode) dismissSelectMode();

        if (mExpandable) {
            collapsingToolbarLayout.setTitle(getResources().getString(R.string.action_search));
        }

        if (mSubtitle != null && mSubtitle.length() != 0) {
            if (mExpandable) {
                collapsingToolbarLayout.setSubtitle(null);
            }
            collapsedSubTitleView.setText(null);
            collapsedSubTitleView.setVisibility(GONE);
        }

        main_toolbar.setVisibility(GONE);
        search_toolbar.setVisibility(VISIBLE);
        bottomContainer.setVisibility(GONE);
        setSearchModeActionButton(true);
        search_navButton.setTooltipText(getResources().getString(R.string.sesl_navigate_up));
        search_navButton.setOnClickListener(v -> dismissSearchMode());
        setExpanded(false, true);

        search_edittext.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                searchModeListener.beforeTextChanged(s, start, count, after);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                setSearchModeActionButton(s.length() == 0);
                searchModeListener.onTextChanged(s, start, before, count);
            }

            @Override
            public void afterTextChanged(Editable s) {
                searchModeListener.afterTextChanged(s);
            }
        });
        search_edittext.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                setEditTextFocus(false);
                searchModeListener.onKeyboardSearchClick(search_edittext.getEditableText());
                return true;
            }
            return false;
        });
        setEditTextFocus(true);

        searchModeListener.onSearchOpened(search_edittext);
    }

    public void setSearchModeListener(SearchModeListener listener) {
        searchModeListener = listener;
    }

    public void dismissSearchMode() {
        searchModeListener.onSearchDismissed(search_edittext);

        mSearchMode = false;
        lockDrawerIfAvailable(false);
        setNavigationButtonVisible(navigationButtonVisible);
        onBackPressedCallback.setEnabled(false);
        setEditTextFocus(false);
        main_toolbar.setVisibility(VISIBLE);
        search_toolbar.setVisibility(GONE);
        bottomContainer.setVisibility(VISIBLE);

        setTitle(mTitleExpanded, mTitleCollapsed);
        setSubtitle(mSubtitle);
    }

    public boolean isSearchMode() {
        return mSearchMode;
    }

    private void setEditTextFocus(boolean focus) {
        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(INPUT_METHOD_SERVICE);
        if (focus) {
            search_edittext.setFocusableInTouchMode(true);
            search_edittext.requestFocus();
            search_edittext.setOnKeyListener((v, keyCode, event) -> {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                    dismissSearchMode();
                    return true;
                }
                return false;
            });
            imm.showSoftInput(search_edittext, InputMethodManager.SHOW_IMPLICIT);
        } else {
            try {
                imm.hideSoftInputFromWindow(mActivity.getCurrentFocus().getWindowToken(), 0);
            } catch (Exception e) {
                Log.d("searchMode", e.getMessage());
            }
            search_edittext.clearFocus();
        }
    }

    private void setSearchModeActionButton(boolean microphone) {
        if (microphone) {

            Intent intent = new Intent("android.speech.action.RECOGNIZE_SPEECH");
            intent.putExtra("android.speech.extra.LANGUAGE_MODEL", "free_form");
            intent.putExtra("android.speech.extra.LANGUAGE", Locale.getDefault());

            search_action_button.setVisibility(intent.resolveActivity(getContext().getPackageManager()) == null ? GONE : VISIBLE);

            search_action_button.setImageResource(R.drawable.ic_samsung_voice_2);
            search_action_button.setTooltipText(getResources().getString(R.string.sesl_searchview_description_voice));
            search_action_button.setOnClickListener(v -> searchModeListener.onVoiceInputClick(intent));
        } else {
            search_action_button.setVisibility(VISIBLE);
            search_action_button.setImageResource(R.drawable.ic_samsung_close);
            search_action_button.setTooltipText(getResources().getString(R.string.sesl_searchview_description_clear));
            search_action_button.setOnClickListener(v -> search_edittext.setText(""));
        }
    }

    public void onSearchModeVoiceInputResult(ActivityResult result) {
        if (result.getResultCode() == Activity.RESULT_OK) {
            final ArrayList<String> matches = result.getData().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (!matches.isEmpty()) {
                String Query = matches.get(0);
                search_edittext.setText(Query);
                search_edittext.setSelection(search_edittext.getText().length());
            }
        }
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
        if (!(mSelectMode || mSearchMode)) navigationButtonVisible = visible;
        toolbar.setPaddingRelative(mSelectMode || mSearchMode || visible ? 0 : getResources().getDimensionPixelSize(R.dimen.sesl_action_bar_content_inset), getToolbarTopPadding(), 0, 0);
    }

    public void setNavigationButtonBadge(int count) {
        if (navigationBadgeBackground == null) {
            navigationBadgeBackground = (ViewGroup) ((LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.navigation_button_badge_layout, navigationButtonContainer, false);
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

    public void setNavigationButtonOnClickListener(OnClickListener listener) {
        navigationButton.setOnClickListener(listener);
    }

    //
    // Toolbar Menu methods
    //
    public void setOnToolbarMenuItemClickListener(OnMenuItemClickListener listener) {
        onToolbarMenuItemClickListener = listener;
    }

    public Menu getToolbarMenu() {
        return toolbarMenu != null ? toolbarMenu : (toolbarMenu = new Menu());
    }

    public void inflateToolbarMenu(@MenuRes int menuRes){
        inflateToolbarMenu(new Menu(menuRes, mContext));
    }

    public void inflateToolbarMenu(Menu menu) {
        actionButtonContainer.removeAllViews();

        toolbarMenu = menu;
        Menu overflowMenu = new Menu();

        for (MenuItem menuItem : toolbarMenu.menuItems) {
            if (menuItem.isActionButton()) {

                ActionButtonMenuItemView button = new ActionButtonMenuItemView(mContext, menuItem);
                button.setOnClickListener(v -> onToolbarMenuItemClickListener.onMenuItemClick(menuItem));
                menuItem.setMenuItemListener(menuItem1 -> button.updateView());

                actionButtonContainer.addView(button);
            } else {
                overflowMenu.addMenuItem(menuItem);
            }
        }

        if (overflowMenu.size() > 0) {
            ActionButtonMenuItemView overflowButton = new ActionButtonMenuItemView(mContext, overflowMenu, getResources().getString(R.string.sesl_more_options), R.drawable.sesl_ic_menu_overflow);
            actionButtonContainer.addView(overflowButton);

            PopupMenu overflowPopupMenu = new PopupMenu(findViewById(R.id.toolbar_layout_popup_window_anchor));
            overflowPopupMenu.inflate(overflowMenu);
            overflowPopupMenu.setPopupMenuListener(new PopupMenu.PopupMenuListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    return onToolbarMenuItemClickListener.onMenuItemClick(item);
                }

                @Override
                public void onMenuItemUpdate(MenuItem menuItem) {
                    overflowButton.updateView();
                }
            });

            overflowButton.setOnClickListener(v -> {
                if (getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
                    overflowPopupMenu.show(overflowPopupMenu.getPopupMenuWidth(), 0);
                } else {
                    overflowPopupMenu.show(-overflowPopupMenu.getPopupMenuWidth(), 0);
                }
            });
        }

    }

    //
    // others
    //
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

            LinearLayout collapsedTitleContainer = findViewById(R.id.toolbar_layout_collapsed_title_container);

            if (appBarLayout.seslIsCollapsed()) {
                collapsedTitleContainer.setAlpha(1.0f);
            } else {
                float collapsedTitleAlpha = ((150.0f / alphaRange) * (((float) layoutPosition) - toolbarTitleAlphaStart));

                if (collapsedTitleAlpha >= 0.0f && collapsedTitleAlpha <= 255.0f) {
                    collapsedTitleAlpha /= 255.0f;
                    collapsedTitleContainer.setAlpha(collapsedTitleAlpha);
                } else if (collapsedTitleAlpha < 0.0f) collapsedTitleContainer.setAlpha(0.0f);
                else collapsedTitleContainer.setAlpha(1.0f);
            }
        }
    }

}
