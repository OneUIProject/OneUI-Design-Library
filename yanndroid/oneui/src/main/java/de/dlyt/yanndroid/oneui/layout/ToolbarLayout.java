package de.dlyt.yanndroid.oneui.layout;

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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResult;
import androidx.annotation.DrawableRes;
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
import de.dlyt.yanndroid.oneui.sesl.widget.ActionModeBottomBarButton;
import de.dlyt.yanndroid.oneui.sesl.widget.ToolbarImageButton;
import de.dlyt.yanndroid.oneui.view.PopupMenu;

import static android.content.Context.INPUT_METHOD_SERVICE;

public class ToolbarLayout extends LinearLayout {
    public static final int N_BADGE = -1;
    public static final int APPBAR_LAYOUT = 0;
    public static final int COLLAPSING_TOOLBAR = 1;
    public static final int TOOLBAR = 2;
    public static final int NAVIGATION_BUTTON = 3;
    public static final int COLLAPSED_TITLE = 4;
    public static final int COLLAPSED_SUBTITLE = 5;
    public static final int MAIN_CONTENT = 6;
    public static final int FOOTER_CONTENT = 7;
    private static String TAG = "ToolbarLayout";
    private boolean mIsOneUI4;
    public ViewGroup navigationBadgeBackground;
    public TextView navigationBadgeText;
    public ViewGroup moreOverflowBadgeBackground;
    public TextView moreOverflowBadgeText;
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
        void onMenuItemClick(MenuItem item);
    }

    //toolbar menu
    private LinearLayout actionButtonContainer;
    private FrameLayout overflowButtonContainer;
    private ToolbarImageButton overflowButton;
    private View overflowMenuPopupAnchor = null;
    private PopupMenu overflowPopupMenu;
    private Menu toolbarMenu;
    private HashMap<MenuItem, Object> toolbarMenuButtons;
    private OnMenuItemClickListener onToolbarMenuItemClickListener = item -> {
    };

    //select mode
    private Menu selectModeBottomMenu;
    private boolean mSelectMode = false;
    private RelativeLayout selectModeCheckboxContainer;
    private CheckBox selectModeCheckbox;
    private OnMenuItemClickListener onSelectModeBottomMenuItemClickListener = item -> {
    };

    //search mode
    private boolean mSearchMode = false;
    private LinearLayout main_toolbar;
    private LinearLayout search_toolbar;
    private ToolbarImageButton search_navButton;
    private ToolbarImageButton search_action_button;
    private EditText search_edittext;
    //private ActivityResultLauncher<Intent> voiceSearchResultLauncher;
    private SearchModeListener searchModeListener = new SearchModeListener() {
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

        if (mExpandable) {
            appBarLayout = findViewById(R.id.toolbar_layout_app_bar);
            collapsingToolbarLayout = findViewById(R.id.toolbar_layout_collapsing_toolbar_layout);

            appBarLayout.setLiftableState(false);
        }
        toolbar = findViewById(R.id.toolbar_layout_toolbar);

        navigationButtonContainer = findViewById(R.id.toolbar_layout_navigationButton_container);
        navigationButton = findViewById(R.id.toolbar_layout_navigationButton);
        collapsedTitleView = findViewById(R.id.toolbar_layout_collapsed_title);
        collapsedSubTitleView = findViewById(R.id.toolbar_layout_collapsed_subtitle);
        actionButtonContainer = findViewById(R.id.toolbar_layout_overflow_container);
        overflowMenuPopupAnchor = findViewById(R.id.toolbar_layout_popup_window_anchor);

        mainContainer = findViewById(R.id.toolbar_layout_main_container);
        bottomContainer = findViewById(R.id.toolbar_layout_footer);

        selectModeCheckboxContainer = findViewById(R.id.checkbox_withtext);
        selectModeCheckbox = findViewById(R.id.checkbox_all);

        main_toolbar = findViewById(R.id.toolbar_layout_main_toolbar);
        search_toolbar = findViewById(R.id.toolbar_layout_search_toolbar);
        search_navButton = findViewById(R.id.toolbar_layout_search_navigationButton);
        search_action_button = findViewById(R.id.search_view_action_button);
        search_edittext = findViewById(R.id.toolbar_layout_search_field);

        mActivity.setSupportActionBar(toolbar);
        mActivity.getSupportActionBar().setDisplayShowTitleEnabled(false);
        mActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
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
        mActivity.getOnBackPressedDispatcher().addCallback(onBackPressedCallback);

        refreshLayout(getResources().getConfiguration());

    }

    void syncWithDrawer(DrawerLayout drawerLayout) {
        this.drawerLayout = drawerLayout;
    }

    private void lockDrawerIfAvailable(boolean lock) {
        if (drawerLayout != null) {
            if (lock) {
                ((androidx.drawerlayout.widget.DrawerLayout) drawerLayout.getView(DrawerLayout.DRAWER_LAYOUT)).setDrawerLockMode(androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            } else {
                ((androidx.drawerlayout.widget.DrawerLayout) drawerLayout.getView(DrawerLayout.DRAWER_LAYOUT)).setDrawerLockMode(androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_UNLOCKED);
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
        return mIsOneUI4 ? getResources().getDimensionPixelSize(R.dimen.sesl4_action_bar_top_padding) : 0;
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
        WindowManagerSupport.hideStatusBarForLandscape(mActivity, newConfig.orientation);

        ViewSupport.updateListBothSideMargin(mActivity, mainContainer);
        ViewSupport.updateListBothSideMargin(mActivity, findViewById(R.id.toolbar_layout_bottom_corners));
        ViewSupport.updateListBothSideMargin(mActivity, findViewById(R.id.toolbar_layout_footer_container));

        if (mExpandable) resetAppBarHeight();

        updateCollapsedSubtitleVisibility();
    }

    private void resetAppBarHeight() {
        if (appBarLayout != null) {
            ViewGroup.LayoutParams params = appBarLayout.getLayoutParams();
            int windowHeight = getWindowHeight();
            int bottomPadding;

            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                appBarLayout.setActivated(false);
                bottomPadding = 0;
                params.height = (int) getResources().getDimension(mIsOneUI4 ? R.dimen.sesl4_action_bar_default_height : R.dimen.sesl_action_bar_default_height);
            } else {
                appBarLayout.setActivated(true);
                setExpanded(mExpanded, false);
                bottomPadding = mIsOneUI4 ? 0 : getResources().getDimensionPixelSize(R.dimen.sesl_extended_appbar_bottom_padding);

                TypedValue outValue = new TypedValue();
                getResources().getValue(mIsOneUI4 ? R.dimen.sesl4_appbar_height_proportion : R.dimen.sesl_appbar_height_proportion, outValue, true);

                params.height = (int) ((float) windowHeight * outValue.getFloat());
            }

            appBarLayout.setLayoutParams(params);
            appBarLayout.setPadding(0, 0, 0, bottomPadding);
        } else
            Log.w(TAG + ".resetAppBarHeight", "appBarLayout is null.");
    }

    private void resetToolbarHeight() {
        if (toolbar != null) {
            toolbar.setPaddingRelative(mSelectMode || mSearchMode || navigationButtonVisible ? 0 : getResources().getDimensionPixelSize(R.dimen.sesl_action_bar_content_inset), getToolbarTopPadding(), 0, 0);

            ViewGroup.LayoutParams lp = toolbar.getLayoutParams();
            lp.height = lp.height + getToolbarTopPadding();
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
        return mExpandable ? !appBarLayout.isCollapsed() : false;
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
        setSelectModeCount(0);
        actionButtonContainer.setVisibility(GONE);

        if (selectModeBottomMenu != null) {
            bottomContainer.setVisibility(GONE);
            findViewById(R.id.toolbar_layout_footer_action_mode).setVisibility(VISIBLE);
        }
    }

    @SuppressLint("RestrictedApi")
    public void setSelectModeBottomMenu(@MenuRes int menuRes, OnMenuItemClickListener listener) {
        LinearLayout footer_action_mode = findViewById(R.id.toolbar_layout_footer_action_mode);
        footer_action_mode.removeAllViews();

        onSelectModeBottomMenuItemClickListener = listener;

        selectModeBottomMenu = new MenuBuilder(getContext());
        MenuInflater menuInflater = new SupportMenuInflater(getContext());
        menuInflater.inflate(menuRes, selectModeBottomMenu);

        ArrayList<MenuItem> bottomItems = new ArrayList<>();

        for (int i = 0; i < selectModeBottomMenu.size(); i++) {
            MenuItem item = selectModeBottomMenu.getItem(i);
            if (((MenuItemImpl) item).requiresActionButton()) {
                ActionModeBottomBarButton button = new ActionModeBottomBarButton(mContext);
                button.setText(item.getTitle());
                button.setIcon(item.getIcon());
                button.setOnClickListener(v -> {
                    onSelectModeBottomMenuItemClickListener.onMenuItemClick(item);
                });
                footer_action_mode.addView(button);
            } else {
                bottomItems.add(item);
            }
        }

        if (!bottomItems.isEmpty()) {
            ActionModeBottomBarButton moreButton = new ActionModeBottomBarButton(mContext);
            moreButton.setText(getResources().getString(R.string.sesl_more_item_label));
            moreButton.setIcon(getResources().getDrawable(R.drawable.ic_samsung_more, getContext().getTheme()));
            footer_action_mode.addView(moreButton);

            PopupMenu actionPopupMenu = new PopupMenu(moreButton);
            actionPopupMenu.inflate(bottomItems);
            actionPopupMenu.setOnMenuItemClickListener(item -> {
                actionPopupMenu.dismiss();
                onSelectModeBottomMenuItemClickListener.onMenuItemClick(item);
            });

            moreButton.setOnClickListener(v -> {
                int xoff = actionPopupMenu.getPopupMenuWidth() - moreButton.getWidth() + 7;
                if (getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
                    actionPopupMenu.show(xoff, 0);
                } else {
                    actionPopupMenu.show(-xoff, 0);
                }
            });

        }

    }

    public Menu getSelectModeBottomMenu() {
        return selectModeBottomMenu;
    }

    public void dismissSelectMode() {
        mSelectMode = false;
        lockDrawerIfAvailable(false);
        setNavigationButtonVisible(navigationButtonVisible);
        selectModeCheckboxContainer.setVisibility(View.GONE);
        setTitle(mTitleExpanded, mTitleCollapsed);
        actionButtonContainer.setVisibility(VISIBLE);

        bottomContainer.setVisibility(VISIBLE);
        findViewById(R.id.toolbar_layout_footer_action_mode).setVisibility(GONE);
    }

    public void setSelectModeCount(int count) {
        String title = count > 0 ? getResources().getString(R.string.selected_check_info, count) : getResources().getString(R.string.settings_import_select_items);
        if (mExpandable) collapsingToolbarLayout.setTitle(title);
        collapsedTitleView.setText(title);

        if (selectModeBottomMenu != null)
            findViewById(R.id.toolbar_layout_footer_action_mode).setVisibility(count > 0 ? VISIBLE : GONE);
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
        if (mExpandable)
            collapsingToolbarLayout.setTitle(getResources().getString(R.string.action_search));
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
    }

    public void setSearchModeListener(SearchModeListener listener) {
        searchModeListener = listener;
    }

    public void dismissSearchMode() {
        mSearchMode = false;
        lockDrawerIfAvailable(false);
        setNavigationButtonVisible(navigationButtonVisible);
        onBackPressedCallback.setEnabled(false);
        setEditTextFocus(false);
        main_toolbar.setVisibility(VISIBLE);
        search_toolbar.setVisibility(GONE);
        bottomContainer.setVisibility(VISIBLE);

        setTitle(mTitleExpanded, mTitleCollapsed);
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
            search_action_button.setImageResource(R.drawable.ic_samsung_voice_2);
            search_action_button.setTooltipText(getResources().getString(R.string.sesl_searchview_description_voice));
            search_action_button.setOnClickListener(v -> {
                Intent intent = new Intent("android.speech.action.RECOGNIZE_SPEECH");
                intent.putExtra("android.speech.extra.LANGUAGE_MODEL", "free_form");
                intent.putExtra("android.speech.extra.LANGUAGE", Locale.getDefault());
                //voiceSearchResultLauncher.launch(intent);
                searchModeListener.onVoiceInputClick(intent);
            });
        } else {
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
        return toolbarMenu;
    }

    @SuppressLint("RestrictedApi")
    public void inflateToolbarMenu(@MenuRes int resId) {
        actionButtonContainer.removeAllViews();
        overflowButton = null;

        toolbarMenuButtons = new HashMap<>();
        toolbarMenu = new MenuBuilder(getContext());
        MenuInflater menuInflater = new SupportMenuInflater(getContext());
        menuInflater.inflate(resId, toolbarMenu);

        ArrayList<MenuItem> overflowItems = new ArrayList<>();

        for (int i = 0; i < toolbarMenu.size(); i++) {
            MenuItem item = toolbarMenu.getItem(i);
            if (((MenuItemImpl) item).requiresActionButton()) {
                addActionButton(item);
            } else {
                overflowItems.add(item);
            }
        }

        if (!overflowItems.isEmpty()) setOverflowMenu(overflowItems);
    }

    private void addActionButton(MenuItem item) {
        if (actionButtonContainer.getChildCount() != 0) {
            for (int i = 0; i < actionButtonContainer.getChildCount(); i++) {
                ToolbarImageButton previousBtn = getOverflowButton(i);
                ViewGroup.LayoutParams lp = previousBtn.getLayoutParams();
                lp.width = getResources().getDimensionPixelSize(R.dimen.overflow_button_size);
                previousBtn.setPaddingRelative(getResources().getDimensionPixelSize(R.dimen.sesl_action_button_padding_horizontal), 0, getResources().getDimensionPixelSize(R.dimen.sesl_action_button_padding_horizontal), 0);
            }
        }

        ToolbarImageButton actionButton = new ToolbarImageButton(mContext);

        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(getResources().getDimensionPixelSize(R.dimen.sesl_overflow_button_min_width), getResources().getDimensionPixelSize(mIsOneUI4 ? R.dimen.sesl4_action_bar_default_height : R.dimen.sesl_action_bar_default_height));

        actionButton.setBackgroundResource(R.drawable.sesl_action_bar_item_background);
        actionButton.setImageDrawable(item.getIcon());
        actionButton.setPaddingRelative(getResources().getDimensionPixelSize(R.dimen.sesl_action_bar_overflow_padding_start), 0, getResources().getDimensionPixelSize(R.dimen.sesl_action_bar_overflow_padding_end), 0);
        actionButton.setTooltipText(item.getTitle());
        actionButton.setOnClickListener(v -> onToolbarMenuItemClickListener.onMenuItemClick(item));

        toolbarMenuButtons.put(item, actionButton);
        actionButtonContainer.addView(actionButton, lp);
    }

    private void setOverflowMenu(ArrayList<MenuItem> overflowItems) {
        overflowPopupMenu = new PopupMenu(overflowMenuPopupAnchor);
        overflowPopupMenu.inflate(overflowItems);
        overflowPopupMenu.setOnMenuItemClickListener(item -> {
            overflowPopupMenu.dismiss();
            onToolbarMenuItemClickListener.onMenuItemClick(item);
        });

        if (overflowButton == null) {
            if (actionButtonContainer.getChildCount() != 0) {
                for (int i = 0; i < actionButtonContainer.getChildCount(); i++) {
                    ToolbarImageButton previousBtn = getOverflowButton(i);
                    ViewGroup.LayoutParams lp = previousBtn.getLayoutParams();
                    lp.width = getResources().getDimensionPixelSize(R.dimen.overflow_button_size);
                    previousBtn.setPaddingRelative(getResources().getDimensionPixelSize(R.dimen.sesl_action_button_padding_horizontal), 0, getResources().getDimensionPixelSize(R.dimen.sesl_action_button_padding_horizontal), 0);
                }
            }

            overflowButtonContainer = new FrameLayout(mContext);
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.MATCH_PARENT);
            actionButtonContainer.addView(overflowButtonContainer, lp);

            overflowButton = new ToolbarImageButton(mContext);

            ViewGroup.LayoutParams lp2 = new ViewGroup.LayoutParams(getResources().getDimensionPixelSize(R.dimen.sesl_overflow_button_min_width), getResources().getDimensionPixelSize(mIsOneUI4 ? R.dimen.sesl4_action_bar_default_height : R.dimen.sesl_action_bar_default_height));

            overflowButton.setBackgroundResource(R.drawable.sesl_action_bar_item_background);
            overflowButton.setImageResource(R.drawable.sesl_ic_menu_overflow);
            overflowButton.setPaddingRelative(getResources().getDimensionPixelSize(R.dimen.sesl_action_bar_overflow_padding_start), 0, getResources().getDimensionPixelSize(R.dimen.sesl_action_bar_overflow_padding_end), 0);
            overflowButton.setOnClickListener(v -> {
                if (getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
                    overflowPopupMenu.show(overflowPopupMenu.getPopupMenuWidth(), 0);
                } else {
                    overflowPopupMenu.show(-overflowPopupMenu.getPopupMenuWidth(), 0);
                }
            });
            overflowButton.setTooltipText(getResources().getString(R.string.sesl_more_options));

            overflowButtonContainer.addView(overflowButton, lp2);
        }
    }

    @SuppressLint("RestrictedApi")
    public void setOverflowMenuBadge(MenuItem item, Integer badge) {
        if (!((MenuItemImpl) item).requiresActionButton()) {
            overflowPopupMenu.setMenuItemBadge(item, badge);
            showOverflowMenuButtonBadge(overflowPopupMenu.getTotalBadgeCount());
        }
    }

    public Integer getOverflowMenuBadge(MenuItem item) {
        return overflowPopupMenu.getMenuItemBadge(item);
    }

    private void showOverflowMenuButtonBadge(int count) {
        if (moreOverflowBadgeBackground == null) {
            moreOverflowBadgeBackground = (ViewGroup) ((LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.sesl_action_menu_item_badge, overflowButtonContainer, false);
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
                if (count > 9)
                    lp.setMarginEnd(getResources().getDimensionPixelSize(R.dimen.sesl_menu_item_badge_end_margin) - (int) getDIPForPX(4));
                else
                    lp.setMarginEnd(getResources().getDimensionPixelSize(R.dimen.sesl_menu_item_badge_end_margin));
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

    private ToolbarImageButton getOverflowButton(int index) {
        if (actionButtonContainer != null && actionButtonContainer.getChildCount() != 0) {
            return (ToolbarImageButton) actionButtonContainer.getChildAt(index);
        } else {
            Log.w(TAG + ".getOverflowIcon", "overflowContainer is null or contains no icons.");
            return null;
        }
    }

    public void setToolbarMenuItemIcon(MenuItem item, Drawable drawable) {
        if (toolbarMenuButtons.containsKey(item)) {
            ((ToolbarImageButton) toolbarMenuButtons.get(item)).setImageDrawable(drawable);
        }
    }

    public void setToolbarMenuItemIcon(MenuItem item, @DrawableRes int resId) {
        if (toolbarMenuButtons.containsKey(item)) {
            ((ToolbarImageButton) toolbarMenuButtons.get(item)).setImageResource(resId);
        }
    }

    public void setToolbarMenuItemTitle(MenuItem item, CharSequence title) {
        if (toolbarMenuButtons.containsKey(item)) {
            ((ToolbarImageButton) toolbarMenuButtons.get(item)).setTooltipText(title);
        } else {
            overflowPopupMenu.setMenuItemTitle(item, title);
        }
    }

    public void setToolbarMenuItemVisibility(MenuItem item, boolean visible) {
        if (toolbarMenuButtons.containsKey(item)) {
            ((ToolbarImageButton) toolbarMenuButtons.get(item)).setVisibility(visible ? VISIBLE : GONE);
        }
    }

    public void setToolbarMenuItemEnabled(MenuItem item, boolean enabled) {
        if (toolbarMenuButtons.containsKey(item)) {
            ((ToolbarImageButton) toolbarMenuButtons.get(item)).setEnabled(enabled);
        } else {
            overflowPopupMenu.setMenuItemEnabled(item, enabled);
        }
    }

    public ToolbarImageButton getToolbarMenuItemView(MenuItem item) {
        if (toolbarMenuButtons.containsKey(item)) {
            return ((ToolbarImageButton) toolbarMenuButtons.get(item));
        }
        return null;
    }

    //
    // others
    //
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
            case COLLAPSED_SUBTITLE:
                return collapsedSubTitleView;
            case MAIN_CONTENT:
                return mainContainer;
            case FOOTER_CONTENT:
                return bottomContainer;
            default:
                return null;
        }
    }

    @IntDef({APPBAR_LAYOUT, COLLAPSING_TOOLBAR, TOOLBAR, NAVIGATION_BUTTON, COLLAPSED_TITLE, COLLAPSED_SUBTITLE, MAIN_CONTENT, FOOTER_CONTENT})
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

            LinearLayout collapsedTitleContainer = findViewById(R.id.toolbar_layout_collapsed_title_container);

            if (appBarLayout.getHeight() <= ((int) getResources().getDimension(mIsOneUI4 ? R.dimen.sesl4_action_bar_height_with_padding : R.dimen.sesl_action_bar_height_with_padding))) {
                collapsedTitleContainer.setAlpha(1.0f);
            } else {
                float collapsedTitleAlpha = ((150.0f / alphaRange) * (((float) layoutPosition) - toolbarTitleAlphaStart));

                if (collapsedTitleAlpha >= 0.0f && collapsedTitleAlpha <= 255.0f) {
                    collapsedTitleAlpha /= 255.0f;
                    collapsedTitleContainer.setAlpha(collapsedTitleAlpha);
                } else if (collapsedTitleAlpha < 0.0f)
                    collapsedTitleContainer.setAlpha(0.0f);
                else
                    collapsedTitleContainer.setAlpha(1.0f);
            }
        }
    }

}
