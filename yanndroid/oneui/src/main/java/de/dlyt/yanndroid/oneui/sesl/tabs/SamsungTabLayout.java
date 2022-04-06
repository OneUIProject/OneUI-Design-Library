package de.dlyt.yanndroid.oneui.sesl.tabs;

import static androidx.viewpager.widget.SeslViewPager.SCROLL_STATE_DRAGGING;
import static androidx.viewpager.widget.SeslViewPager.SCROLL_STATE_IDLE;
import static androidx.viewpager.widget.SeslViewPager.SCROLL_STATE_SETTLING;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.text.Layout;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.BoolRes;
import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.Dimension;
import androidx.annotation.DrawableRes;
import androidx.annotation.IntDef;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.StringRes;
import androidx.appcompat.animation.SeslAnimationUtils;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.util.Pools;
import androidx.core.view.GravityCompat;
import androidx.core.view.MarginLayoutParamsCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityActionCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat.CollectionInfoCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat.CollectionItemInfoCompat;
import androidx.core.widget.TextViewCompat;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.SeslViewPager;

import com.google.android.material.animation.AnimationUtils;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.BadgeUtils;
import com.google.android.material.internal.ThemeEnforcement;
import com.google.android.material.internal.ViewUtils;
import com.google.android.material.resources.MaterialResources;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.MaterialShapeUtils;
import com.google.android.material.theme.overlay.MaterialThemeOverlay;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;

import de.dlyt.yanndroid.oneui.R;
import de.dlyt.yanndroid.oneui.view.Tooltip;

@SeslViewPager.DecorView
public class SamsungTabLayout extends HorizontalScrollView {
    private boolean mIsOneUI4;
    private static final int ANIMATION_DURATION = 300;
    private static final int ANIM_HIDE_DURATION = 400;
    private static final float ANIM_RIPPLE_MINOR_SCALE = 0.95f;
    private static final int ANIM_SHOW_DURATION = 350;
    private static final int BADGE_N_TEXT_SIZE = 11;
    private static final int BADGE_TYPE_DOT = 2;
    private static final int BADGE_TYPE_N = 1;
    private static final int BADGE_TYPE_UNKNOWN = -1;
    @Dimension(unit = Dimension.DP)
    static final int DEFAULT_GAP_TEXT_ICON = 8;
    @Dimension(unit = Dimension.DP)
    private static final int DEFAULT_HEIGHT = 48;
    @Dimension(unit = Dimension.DP)
    private static final int DEFAULT_HEIGHT_WITH_TEXT_ICON = 72;
    private static final int DEPTH_TYPE_MAIN = 1;
    private static final int DEPTH_TYPE_SUB = 2;
    @Dimension(unit = Dimension.DP)
    static final int FIXED_WRAP_GUTTER_MIN = 16;
    public static final int GRAVITY_FILL = 0;
    public static final int GRAVITY_CENTER = 1;
    public static final int GRAVITY_START = 2;
    @IntDef(flag = true, value = {GRAVITY_FILL, GRAVITY_CENTER, GRAVITY_START})
    @Retention(RetentionPolicy.SOURCE)
    public @interface TabGravity {}
    public static final int INDICATOR_ANIMATION_MODE_LINEAR = 0;
    public static final int INDICATOR_ANIMATION_MODE_ELASTIC = 1;
    @IntDef(value = {INDICATOR_ANIMATION_MODE_LINEAR, INDICATOR_ANIMATION_MODE_ELASTIC})
    @Retention(RetentionPolicy.SOURCE)
    public @interface TabIndicatorAnimationMode {}
    public static final int INDICATOR_GRAVITY_BOTTOM = 0;
    public static final int INDICATOR_GRAVITY_CENTER = 1;
    public static final int INDICATOR_GRAVITY_TOP = 2;
    public static final int INDICATOR_GRAVITY_STRETCH = 3;
    @IntDef(value = {INDICATOR_GRAVITY_BOTTOM, INDICATOR_GRAVITY_CENTER, INDICATOR_GRAVITY_TOP, INDICATOR_GRAVITY_STRETCH})
    @Retention(RetentionPolicy.SOURCE)
    public @interface TabIndicatorGravity {}
    private static final int INVALID_WIDTH = -1;
    private static final String LOG_TAG = "TabLayout";
    public static final int MODE_SCROLLABLE = 0;
    public static final int MODE_FIXED = 1;
    public static final int MODE_AUTO = 2;
    public static final int SESL_MODE_FIXED_AUTO = 11;
    public static final int SESL_MODE_WEIGHT_AUTO = 12;
    @IntDef(value = {MODE_SCROLLABLE, MODE_FIXED, MODE_AUTO, SESL_MODE_FIXED_AUTO, SESL_MODE_WEIGHT_AUTO})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Mode {}
    @Dimension(unit = Dimension.DP)
    private static final int SESL_DEFAULT_HEIGHT = 60;
    @Dimension(unit = Dimension.DP)
    private static final int SESL_SUB_DEPTH_DEFAULT_HEIGHT = 56;
    public static final int TAB_LABEL_VISIBILITY_UNLABELED = 0;
    public static final int TAB_LABEL_VISIBILITY_LABELED = 1;
    @IntDef(value = {TAB_LABEL_VISIBILITY_UNLABELED, TAB_LABEL_VISIBILITY_LABELED})
    public @interface LabelVisibility {}
    @Dimension(unit = Dimension.DP)
    private static final int TAB_MIN_WIDTH_MARGIN = 56;
    private AdapterChangeListener adapterChangeListener;
    private int contentInsetStart;
    @Nullable private BaseOnTabSelectedListener currentVpSelectedListener;
    boolean inlineLabel;
    private int mBadgeColor = Color.WHITE;
    private int mBadgeTextColor = Color.WHITE;
    private Typeface mBoldTypeface;
    protected int mDepthStyle = DEPTH_TYPE_MAIN;
    private int mFirstTabGravity;
    private int mIconTextGap = -1;
    private boolean mIsChangedGravityByLocal;
    private boolean mIsOverScreen = false;
    private boolean mIsScaledTextSizeType = false;
    private Typeface mNormalTypeface;
    private int mOverScreenMaxWidth = -1;
    private int mRequestedTabWidth = -1;
    private int mSubTabIndicator2ndHeight = 1;
    private int mSubTabIndicatorHeight = 1;
    private int mSubTabSelectedIndicatorColor = Color.WHITE;
    int mSubTabSubTextAppearance;
    ColorStateList mSubTabSubTextColors;
    int mSubTabTextSize;
    private int mTabMinSideSpace;
    private int mTabSelectedIndicatorColor;
    @Mode int mode;
    private TabLayoutOnPageChangeListener pageChangeListener;
    @Nullable private PagerAdapter pagerAdapter;
    private DataSetObserver pagerAdapterObserver;
    private final int requestedTabMaxWidth;
    private final int requestedTabMinWidth;
    private ValueAnimator scrollAnimator;
    private final int scrollableTabMinWidth;
    private BaseOnTabSelectedListener selectedListener;
    @Nullable private final ArrayList<BaseOnTabSelectedListener> selectedListeners = new ArrayList<>();
    @Nullable private Tab selectedTab;
    private boolean setupViewPagerImplicitly;
    @NonNull final SlidingTabIndicator slidingTabIndicator;
    final int tabBackgroundResId;
    @TabGravity int tabGravity;
    ColorStateList tabIconTint;
    PorterDuff.Mode tabIconTintMode;
    int tabIndicatorAnimationDuration;
    @TabIndicatorAnimationMode int tabIndicatorAnimationMode;
    boolean tabIndicatorFullWidth;
    @TabIndicatorGravity int tabIndicatorGravity;
    private TabIndicatorInterpolator tabIndicatorInterpolator;
    int tabMaxWidth = Integer.MAX_VALUE;
    int tabPaddingBottom;
    int tabPaddingEnd;
    int tabPaddingStart;
    int tabPaddingTop;
    ColorStateList tabRippleColorStateList;
    @NonNull Drawable tabSelectedIndicator = new GradientDrawable();
    private int tabSelectedIndicatorColor = Color.TRANSPARENT;
    int tabTextAppearance;
    ColorStateList tabTextColors;
    float tabTextMultiLineSize;
    float tabTextSize;
    private final Pools.Pool<TabView> tabViewPool = new Pools.SimplePool(12);
    private final ArrayList<Tab> tabs = new ArrayList<>();
    boolean unboundedRipple;
    @Nullable SeslViewPager viewPager;
    private static final int DEF_STYLE_RES = R.style.TabLayoutStyle;
    private static final Pools.Pool<Tab> tabPool = new Pools.SynchronizedPool(16);

    public interface OnTabSelectedListener extends BaseOnTabSelectedListener<Tab> {}

    @Deprecated
    public interface BaseOnTabSelectedListener<T extends Tab> {
        public void onTabSelected(T tab);

        public void onTabUnselected(T tab);

        public void onTabReselected(T tab);
    }

    public SamsungTabLayout(@NonNull Context context) {
        this(context, null);
    }

    public SamsungTabLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.tabStyle);
    }

    @SuppressLint("RestrictedApi")
    public SamsungTabLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(MaterialThemeOverlay.wrap(context, attrs, defStyleAttr, DEF_STYLE_RES), attrs, defStyleAttr);
        context = getContext();
        mIsOneUI4 = context.getTheme().obtainStyledAttributes(new int[]{R.attr.isOneUI4}).getBoolean(0, false);

        setHorizontalScrollBarEnabled(false);

        slidingTabIndicator = new SlidingTabIndicator(context);
        super.addView(slidingTabIndicator, 0, new HorizontalScrollView.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));

        TypedArray a = ThemeEnforcement.obtainStyledAttributes(context, attrs, R.styleable.SamsungTabLayout, defStyleAttr, DEF_STYLE_RES, R.styleable.SamsungTabLayout_tabTextAppearance);

        if (getBackground() instanceof ColorDrawable) {
            ColorDrawable background = (ColorDrawable) getBackground();
            MaterialShapeDrawable materialShapeDrawable = new MaterialShapeDrawable();
            materialShapeDrawable.setFillColor(ColorStateList.valueOf(background.getColor()));
            materialShapeDrawable.initializeElevationOverlay(context);
            materialShapeDrawable.setElevation(ViewCompat.getElevation(this));
            ViewCompat.setBackground(this, materialShapeDrawable);
        }

        setSelectedTabIndicator(MaterialResources.getDrawable(context, a, R.styleable.SamsungTabLayout_tabIndicator));
        setSelectedTabIndicatorColor(a.getColor(R.styleable.SamsungTabLayout_tabIndicatorColor, Color.TRANSPARENT));
        slidingTabIndicator.setSelectedIndicatorHeight(a.getDimensionPixelSize(R.styleable.SamsungTabLayout_tabIndicatorHeight, -1));
        mTabSelectedIndicatorColor = a.getColor(R.styleable.SamsungTabLayout_tabIndicatorColor, Color.TRANSPARENT);
        setSelectedTabIndicatorGravity(a.getInt(R.styleable.SamsungTabLayout_tabIndicatorGravity, INDICATOR_GRAVITY_BOTTOM));
        setTabIndicatorFullWidth(a.getBoolean(R.styleable.SamsungTabLayout_tabIndicatorFullWidth, true));
        setTabIndicatorAnimationMode(a.getInt(R.styleable.SamsungTabLayout_tabIndicatorAnimationMode, INDICATOR_ANIMATION_MODE_LINEAR));

        tabPaddingStart = tabPaddingTop = tabPaddingEnd = tabPaddingBottom = a.getDimensionPixelSize(R.styleable.SamsungTabLayout_tabPadding, 0);
        tabPaddingStart = a.getDimensionPixelSize(R.styleable.SamsungTabLayout_tabPaddingStart, tabPaddingStart);
        tabPaddingTop = a.getDimensionPixelSize(R.styleable.SamsungTabLayout_tabPaddingTop, tabPaddingTop);
        tabPaddingEnd = a.getDimensionPixelSize(R.styleable.SamsungTabLayout_tabPaddingEnd, tabPaddingEnd);
        tabPaddingBottom = a.getDimensionPixelSize(R.styleable.SamsungTabLayout_tabPaddingBottom, tabPaddingBottom);

        tabTextAppearance = a.getResourceId(R.styleable.SamsungTabLayout_tabTextAppearance, R.style.TabLayoutTextStyle);

        final TypedArray ta = context.obtainStyledAttributes(tabTextAppearance, R.styleable.TextAppearance);
        tabTextSize = ta.getDimensionPixelSize(R.styleable.TextAppearance_android_textSize, 0);
        mIsScaledTextSizeType = ta.getText(R.styleable.TextAppearance_android_textSize).toString().contains("sp");
        tabTextColors = MaterialResources.getColorStateList(context, ta, R.styleable.TextAppearance_android_textColor);
        if (VERSION.SDK_INT >= 31) {
            mBoldTypeface = Typeface.create(getResources().getString(R.string.sesl_font_family_medium), Typeface.BOLD);
            mNormalTypeface = Typeface.create(getResources().getString(R.string.sesl_font_family_regular), Typeface.NORMAL);
        } else {
            mBoldTypeface = Typeface.create(getResources().getString(R.string.sesl_font_family_regular), Typeface.BOLD);
            mNormalTypeface = Typeface.create(getResources().getString(R.string.sesl_font_family_regular), Typeface.NORMAL);
        }

        mSubTabIndicatorHeight = getResources().getDimensionPixelSize(R.dimen.sesl_tablayout_subtab_indicator_height);
        mSubTabIndicator2ndHeight = getResources().getDimensionPixelSize(R.dimen.sesl_tablayout_subtab_indicator_2nd_height);
        mTabMinSideSpace = getResources().getDimensionPixelSize(R.dimen.sesl_tab_min_side_space);
        mSubTabSubTextAppearance = a.getResourceId(R.styleable.SamsungTabLayout_seslTabSubTextAppearance, R.style.TabLayoutSubTextStyle);

        final TypedArray ta2 = context.obtainStyledAttributes(mSubTabSubTextAppearance, R.styleable.TextAppearance);
        try {
            mSubTabSubTextColors = MaterialResources.getColorStateList(context, ta2, R.styleable.TextAppearance_android_textColor);
            mSubTabTextSize = ta2.getDimensionPixelSize(R.styleable.TextAppearance_android_textSize, 0);
        } finally {
            ta.recycle();
            ta2.recycle();
        }

        if (a.hasValue(R.styleable.SamsungTabLayout_seslTabSubTextColor)) {
            mSubTabSubTextColors = MaterialResources.getColorStateList(context, a, R.styleable.SamsungTabLayout_seslTabSubTextColor);
        }

        if (a.hasValue(R.styleable.SamsungTabLayout_seslTabSelectedSubTextColor)) {
            final int selected = a.getColor(R.styleable.SamsungTabLayout_seslTabSelectedSubTextColor, 0);
            mSubTabSubTextColors = createColorStateList(mSubTabSubTextColors.getDefaultColor(), selected);
        }

        if (a.hasValue(R.styleable.SamsungTabLayout_tabTextColor)) {
            tabTextColors = MaterialResources.getColorStateList(context, a, R.styleable.SamsungTabLayout_tabTextColor);
        }

        if (a.hasValue(R.styleable.SamsungTabLayout_tabSelectedTextColor)) {
            final int selected = a.getColor(R.styleable.SamsungTabLayout_tabSelectedTextColor, 0);
            tabTextColors = createColorStateList(tabTextColors.getDefaultColor(), selected);
        }

        tabIconTint = MaterialResources.getColorStateList(context, a, R.styleable.SamsungTabLayout_tabIconTint);
        tabIconTintMode = ViewUtils.parseTintMode(a.getInt(R.styleable.SamsungTabLayout_tabIconTintMode, -1), null);

        tabRippleColorStateList = MaterialResources.getColorStateList(context, a, R.styleable.SamsungTabLayout_tabRippleColor);

        tabIndicatorAnimationDuration = a.getInt(R.styleable.SamsungTabLayout_tabIndicatorAnimationDuration, ANIMATION_DURATION);

        requestedTabMinWidth = a.getDimensionPixelSize(R.styleable.SamsungTabLayout_tabMinWidth, INVALID_WIDTH);
        requestedTabMaxWidth = a.getDimensionPixelSize(R.styleable.SamsungTabLayout_tabMaxWidth, INVALID_WIDTH);
        tabBackgroundResId = a.getResourceId(R.styleable.SamsungTabLayout_tabBackground, 0);
        contentInsetStart = a.getDimensionPixelSize(R.styleable.SamsungTabLayout_tabContentStart, 0);
        mode = a.getInt(R.styleable.SamsungTabLayout_tabMode, MODE_FIXED);
        tabGravity = a.getInt(R.styleable.SamsungTabLayout_tabGravity, GRAVITY_FILL);
        mFirstTabGravity = tabGravity;
        inlineLabel = a.getBoolean(R.styleable.SamsungTabLayout_tabInlineLabel, false);
        unboundedRipple = a.getBoolean(R.styleable.SamsungTabLayout_tabUnboundedRipple, false);
        a.recycle();

        final Resources res = getResources();
        tabTextMultiLineSize = res.getDimensionPixelSize(R.dimen.sesl_tab_text_size_2line);
        scrollableTabMinWidth = res.getDimensionPixelSize(R.dimen.sesl_tab_scrollable_min_width);

        applyModeAndGravity();
    }

    public void setSelectedTabIndicatorColor(@ColorInt int color) {
        mTabSelectedIndicatorColor = color;
        for (Tab tab : tabs) {
            final SeslAbsIndicatorView indicatorView = tab.view.mIndicatorView;
            if (indicatorView != null) {
                if (mDepthStyle == DEPTH_TYPE_SUB && mSubTabSelectedIndicatorColor != -1) {
                    indicatorView.setSelectedIndicatorColor(mSubTabSelectedIndicatorColor);
                } else {
                    indicatorView.setSelectedIndicatorColor(color);
                }
                indicatorView.invalidate();
            }
        }
    }

    @Deprecated
    public void setSelectedTabIndicatorHeight(int height) {
        slidingTabIndicator.setSelectedIndicatorHeight(height);
    }

    public void setScrollPosition(int position, float positionOffset, boolean updateSelectedText) {
        setScrollPosition(position, positionOffset, updateSelectedText, true);
    }

    public void setScrollPosition(int position, float positionOffset, boolean updateSelectedText, boolean updateIndicatorPosition) {
        final int roundedPosition = Math.round(position + positionOffset);
        if (roundedPosition < 0 || roundedPosition >= slidingTabIndicator.getChildCount()) {
            return;
        }

        if (updateIndicatorPosition) {
            slidingTabIndicator.setIndicatorPositionFromTabPosition(position, positionOffset);
        }

        if (scrollAnimator != null && scrollAnimator.isRunning()) {
            scrollAnimator.cancel();
        }
        scrollTo(calculateScrollXForTab(position, positionOffset), 0);

        if (updateSelectedText) {
            setSelectedTabView(roundedPosition, true);
        }
    }

    public void addTab(@NonNull Tab tab) {
        addTab(tab, tabs.isEmpty());
    }

    public void addTab(@NonNull Tab tab, int position) {
        addTab(tab, position, tabs.isEmpty());
    }

    public void addTab(@NonNull Tab tab, boolean setSelected) {
        addTab(tab, tabs.size(), setSelected);
    }

    public void addTab(@NonNull Tab tab, int position, boolean setSelected) {
        if (tab.parent != this) {
            throw new IllegalArgumentException("Tab belongs to a different TabLayout.");
        }
        configureTab(tab, position);
        addTabView(tab);

        if (setSelected) {
            tab.select();
        }
    }

    private void addTabFromItemView(@NonNull TabItem item) {
        final Tab tab = newTab();
        if (item.text != null) {
            tab.setText(item.text);
        }
        if (item.icon != null) {
            tab.setIcon(item.icon);
        }
        if (item.customLayout != 0) {
            tab.setCustomView(item.customLayout);
        }
        if (!TextUtils.isEmpty(item.getContentDescription())) {
            tab.setContentDescription(item.getContentDescription());
        }
        if (item.mSubText != null) {
            tab.seslSetSubText(item.mSubText);
        }
        addTab(tab);
    }

    @Deprecated
    public void setOnTabSelectedListener(@Nullable OnTabSelectedListener listener) {
        setOnTabSelectedListener((BaseOnTabSelectedListener) listener);
    }

    @Deprecated
    public void setOnTabSelectedListener(@Nullable BaseOnTabSelectedListener listener) {
        if (selectedListener != null) {
            removeOnTabSelectedListener(selectedListener);
        }
        selectedListener = listener;
        if (listener != null) {
            addOnTabSelectedListener(listener);
        }
    }

    public void addOnTabSelectedListener(@NonNull OnTabSelectedListener listener) {
        addOnTabSelectedListener((BaseOnTabSelectedListener) listener);
    }

    @Deprecated
    public void addOnTabSelectedListener(@Nullable BaseOnTabSelectedListener listener) {
        if (!selectedListeners.contains(listener)) {
            selectedListeners.add(listener);
        }
    }

    public void removeOnTabSelectedListener(@NonNull OnTabSelectedListener listener) {
        removeOnTabSelectedListener((BaseOnTabSelectedListener) listener);
    }

    @Deprecated
    public void removeOnTabSelectedListener(@Nullable BaseOnTabSelectedListener listener) {
        selectedListeners.remove(listener);
    }

    public void clearOnTabSelectedListeners() {
        selectedListeners.clear();
    }

    @NonNull
    public Tab newTab() {
        Tab tab = createTabFromPool();
        tab.parent = this;
        tab.view = createTabView(tab);
        if (tab.id != NO_ID) {
            tab.view.setId(tab.id);
        }

        return tab;
    }

    protected Tab createTabFromPool() {
        Tab tab = tabPool.acquire();
        if (tab == null) {
            tab = new Tab();
        }
        return tab;
    }

    protected boolean releaseFromTabPool(Tab tab) {
        return tabPool.release(tab);
    }

    public int getTabCount() {
        return tabs.size();
    }

    @Nullable
    public Tab getTabAt(int index) {
        return (index < 0 || index >= getTabCount()) ? null : tabs.get(index);
    }

    public int getSelectedTabPosition() {
        return selectedTab != null ? selectedTab.getPosition() : -1;
    }

    public void removeTab(@NonNull Tab tab) {
        if (tab.parent != this) {
            throw new IllegalArgumentException("Tab does not belong to this TabLayout.");
        }

        removeTabAt(tab.getPosition());
    }

    public void removeTabAt(int position) {
        final int selectedTabPosition = selectedTab != null ? selectedTab.getPosition() : 0;
        removeTabViewAt(position);

        final Tab removedTab = tabs.remove(position);
        if (removedTab != null) {
            removedTab.reset();
            releaseFromTabPool(removedTab);
        }

        final int newTabCount = tabs.size();
        for (int i = position; i < newTabCount; i++) {
            tabs.get(i).setPosition(i);
        }

        if (selectedTabPosition == position) {
            selectTab(tabs.isEmpty() ? null : tabs.get(Math.max(0, position - 1)));
        }
    }

    public void removeAllTabs() {
        for (int i = slidingTabIndicator.getChildCount() - 1; i >= 0; i--) {
            removeTabViewAt(i);
        }

        for (final Iterator<Tab> i = tabs.iterator(); i.hasNext(); ) {
            final Tab tab = i.next();
            i.remove();
            tab.reset();
            releaseFromTabPool(tab);
        }

        selectedTab = null;
    }

    public void setTabMode(@Mode int mode) {
        if (mode != this.mode) {
            this.mode = mode;
            applyModeAndGravity();
            updateBadgePosition();
        }
    }

    @Mode
    public int getTabMode() {
        return mode;
    }

    public void setTabGravity(@TabGravity int gravity) {
        if (tabGravity != gravity) {
            tabGravity = gravity;
            applyModeAndGravity();
        }
    }

    @TabGravity
    public int getTabGravity() {
        return tabGravity;
    }

    public void setSelectedTabIndicatorGravity(@TabIndicatorGravity int indicatorGravity) {
        if (tabIndicatorGravity != indicatorGravity) {
            tabIndicatorGravity = indicatorGravity;
            ViewCompat.postInvalidateOnAnimation(slidingTabIndicator);
        }
    }

    @TabIndicatorGravity
    public int getTabIndicatorGravity() {
        return tabIndicatorGravity;
    }

    public void setTabIndicatorAnimationMode(@TabIndicatorAnimationMode int tabIndicatorAnimationMode) {
        this.tabIndicatorAnimationMode = tabIndicatorAnimationMode;
        switch (tabIndicatorAnimationMode) {
            case INDICATOR_ANIMATION_MODE_LINEAR:
                this.tabIndicatorInterpolator = new TabIndicatorInterpolator();
                break;
            case INDICATOR_ANIMATION_MODE_ELASTIC:
                this.tabIndicatorInterpolator = new ElasticTabIndicatorInterpolator();
                break;
            default:
                throw new IllegalArgumentException(tabIndicatorAnimationMode + " is not a valid TabIndicatorAnimationMode");
        }
    }

    @TabIndicatorAnimationMode
    public int getTabIndicatorAnimationMode() {
        return tabIndicatorAnimationMode;
    }

    public void setTabIndicatorFullWidth(boolean tabIndicatorFullWidth) {
        this.tabIndicatorFullWidth = tabIndicatorFullWidth;
        ViewCompat.postInvalidateOnAnimation(slidingTabIndicator);
    }

    public boolean isTabIndicatorFullWidth() {
        return tabIndicatorFullWidth;
    }

    public void setInlineLabel(boolean inline) {
        if (inlineLabel != inline) {
            inlineLabel = inline;
            for (int i = 0; i < slidingTabIndicator.getChildCount(); i++) {
                View child = slidingTabIndicator.getChildAt(i);
                if (child instanceof TabView) {
                    ((TabView) child).updateOrientation();
                }
            }
            applyModeAndGravity();
        }
    }

    public void setInlineLabelResource(@BoolRes int inlineResourceId) {
        setInlineLabel(getResources().getBoolean(inlineResourceId));
    }

    public boolean isInlineLabel() {
        return inlineLabel;
    }

    public void setUnboundedRipple(boolean unboundedRipple) {
        if (this.unboundedRipple != unboundedRipple) {
            this.unboundedRipple = unboundedRipple;
            for (int i = 0; i < slidingTabIndicator.getChildCount(); i++) {
                View child = slidingTabIndicator.getChildAt(i);
                if (child instanceof TabView) {
                    ((TabView) child).updateBackgroundDrawable(getContext());
                }
            }
        }
    }

    public void setUnboundedRippleResource(@BoolRes int unboundedRippleResourceId) {
        setUnboundedRipple(getResources().getBoolean(unboundedRippleResourceId));
    }

    public boolean hasUnboundedRipple() {
        return unboundedRipple;
    }

    public void setTabTextColors(@Nullable ColorStateList textColor) {
        if (tabTextColors != textColor) {
            tabTextColors = textColor;
            updateAllTabs();
        }
    }

    @Nullable
    public ColorStateList getTabTextColors() {
        return tabTextColors;
    }

    public void setTabTextColors(int normalColor, int selectedColor) {
        setTabTextColors(createColorStateList(normalColor, selectedColor));
    }

    public void setTabIconTint(@Nullable ColorStateList iconTint) {
        if (tabIconTint != iconTint) {
            tabIconTint = iconTint;
            updateAllTabs();
        }
    }

    public void setTabIconTintResource(@ColorRes int iconTintResourceId) {
        setTabIconTint(AppCompatResources.getColorStateList(getContext(), iconTintResourceId));
    }

    @Nullable
    public ColorStateList getTabIconTint() {
        return tabIconTint;
    }

    @Nullable
    public ColorStateList getTabRippleColor() {
        return tabRippleColorStateList;
    }

    public void setTabRippleColor(@Nullable ColorStateList color) {
        if (tabRippleColorStateList != color) {
            tabRippleColorStateList = color;
            for (int i = 0; i < slidingTabIndicator.getChildCount(); i++) {
                View child = slidingTabIndicator.getChildAt(i);
                if (child instanceof TabView) {
                    ((TabView) child).updateBackgroundDrawable(getContext());
                }
            }
        }
    }

    public void setTabRippleColorResource(@ColorRes int tabRippleColorResourceId) {
        setTabRippleColor(AppCompatResources.getColorStateList(getContext(), tabRippleColorResourceId));
    }

    @NonNull
    public Drawable getTabSelectedIndicator() {
        return tabSelectedIndicator;
    }

    public void setSelectedTabIndicator(@Nullable Drawable tabSelectedIndicator) {
        if (this.tabSelectedIndicator != tabSelectedIndicator) {
            this.tabSelectedIndicator = tabSelectedIndicator != null ? tabSelectedIndicator : new GradientDrawable();
        }
    }

    public void setSelectedTabIndicator(@DrawableRes int tabSelectedIndicatorResourceId) {
        if (tabSelectedIndicatorResourceId != 0) {
            setSelectedTabIndicator(AppCompatResources.getDrawable(getContext(), tabSelectedIndicatorResourceId));
        } else {
            setSelectedTabIndicator(null);
        }
    }

    public void setupWithViewPager(@Nullable SeslViewPager viewPager) {
        setupWithViewPager(viewPager, true);
    }

    public void setupWithViewPager(@Nullable final SeslViewPager viewPager, boolean autoRefresh) {
        setupWithViewPager(viewPager, autoRefresh, false);
    }

    private void setupWithViewPager(@Nullable final SeslViewPager viewPager, boolean autoRefresh, boolean implicitSetup) {
        if (this.viewPager != null) {
            if (pageChangeListener != null) {
                this.viewPager.removeOnPageChangeListener(pageChangeListener);
            }
            if (adapterChangeListener != null) {
                this.viewPager.removeOnAdapterChangeListener(adapterChangeListener);
            }
        }

        if (currentVpSelectedListener != null) {
            removeOnTabSelectedListener(currentVpSelectedListener);
            currentVpSelectedListener = null;
        }

        if (viewPager != null) {
            this.viewPager = viewPager;

            if (pageChangeListener == null) {
                pageChangeListener = new TabLayoutOnPageChangeListener(this);
            }
            pageChangeListener.reset();
            viewPager.addOnPageChangeListener(pageChangeListener);

            currentVpSelectedListener = new ViewPagerOnTabSelectedListener(viewPager);
            addOnTabSelectedListener(currentVpSelectedListener);

            final PagerAdapter adapter = viewPager.getAdapter();
            if (adapter != null) {
                setPagerAdapter(adapter, autoRefresh);
            }

            if (adapterChangeListener == null) {
                adapterChangeListener = new AdapterChangeListener();
            }
            adapterChangeListener.setAutoRefresh(autoRefresh);
            viewPager.addOnAdapterChangeListener(adapterChangeListener);

            setScrollPosition(viewPager.getCurrentItem(), 0f, true);
        } else {
            this.viewPager = null;
            setPagerAdapter(null, false);
        }

        setupViewPagerImplicitly = implicitSetup;
    }

    @Deprecated
    public void setTabsFromPagerAdapter(@Nullable final PagerAdapter adapter) {
        setPagerAdapter(adapter, false);
    }

    @Override
    public boolean shouldDelayChildPressedState() {
        return getTabScrollRange() > 0;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        for (int i = 0; i < getTabCount(); i++) {
            Tab tab = getTabAt(i);
            if (tab != null && tab.view != null) {
                if (tab.view.mMainTabTouchBackground != null) {
                    tab.view.mMainTabTouchBackground.setAlpha(0.0f);
                }

                if (tab.view.mIndicatorView != null) {
                    if (getSelectedTabPosition() == i) {
                        tab.view.mIndicatorView.setShow();
                    } else {
                        tab.view.mIndicatorView.setHide();
                    }
                }
            }
        }

        MaterialShapeUtils.setParentAbsoluteElevation(this);

        if (viewPager == null) {
            final ViewParent vp = getParent();
            if (vp instanceof SeslViewPager) {
                setupWithViewPager((SeslViewPager) vp, true, true);
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (setupViewPagerImplicitly) {
            setupWithViewPager(null);
            setupViewPagerImplicitly = false;
        }
    }

    private int getTabScrollRange() {
        return Math.max(0, slidingTabIndicator.getWidth() - getWidth() - getPaddingLeft() - getPaddingRight());
    }

    void setPagerAdapter(@Nullable final PagerAdapter adapter, final boolean addObserver) {
        if (pagerAdapter != null && pagerAdapterObserver != null) {
            pagerAdapter.unregisterDataSetObserver(pagerAdapterObserver);
        }

        pagerAdapter = adapter;

        if (addObserver && adapter != null) {
            if (pagerAdapterObserver == null) {
                pagerAdapterObserver = new PagerAdapterObserver();
            }
            adapter.registerDataSetObserver(pagerAdapterObserver);
        }

        populateFromPagerAdapter();
    }

    void populateFromPagerAdapter() {
        removeAllTabs();

        if (pagerAdapter != null) {
            final int adapterCount = pagerAdapter.getCount();
            for (int i = 0; i < adapterCount; i++) {
                addTab(newTab().setText(pagerAdapter.getPageTitle(i)), false);
            }

            if (viewPager != null && adapterCount > 0) {
                final int curItem = viewPager.getCurrentItem();
                if (curItem != getSelectedTabPosition() && curItem < getTabCount()) {
                    selectTab(getTabAt(curItem), true, true);
                }
            }
        }
    }

    private void updateAllTabs() {
        for (int i = 0, z = tabs.size(); i < z; i++) {
            tabs.get(i).updateView();
        }
    }

    @NonNull
    private TabView createTabView(@NonNull final Tab tab) {
        TabView tabView = tabViewPool != null ? tabViewPool.acquire() : null;
        if (tabView == null) {
            tabView = new TabView(getContext());
        }
        if (tabView.mMainTabTouchBackground != null) {
            tabView.mMainTabTouchBackground.setAlpha(0.0f);
        }
        tabView.setTab(tab);
        tabView.setFocusable(true);
        tabView.setMinimumWidth(getTabMinWidth());
        if (TextUtils.isEmpty(tab.contentDesc)) {
            tabView.setContentDescription(tab.text);
        } else {
            tabView.setContentDescription(tab.contentDesc);
        }
        return tabView;
    }

    private void configureTab(@NonNull Tab tab, int position) {
        tab.setPosition(position);
        tabs.add(position, tab);

        final int count = tabs.size();
        for (int i = position + 1; i < count; i++) {
            tabs.get(i).setPosition(i);
        }
    }

    private void addTabView(@NonNull Tab tab) {
        final TabView tabView = tab.view;
        tabView.setSelected(false);
        tabView.setActivated(false);
        slidingTabIndicator.addView(tabView, tab.getPosition(), createLayoutParamsForTabs());
    }

    @Override
    public void addView(View child) {
        addViewInternal(child);
    }

    @Override
    public void addView(View child, int index) {
        addViewInternal(child);
    }

    @Override
    public void addView(View child, ViewGroup.LayoutParams params) {
        addViewInternal(child);
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        addViewInternal(child);
    }

    private void addViewInternal(final View child) {
        if (child instanceof TabItem) {
            addTabFromItemView((TabItem) child);
        } else {
            throw new IllegalArgumentException("Only TabItem instances can be added to TabLayout");
        }
    }

    @NonNull
    private LinearLayout.LayoutParams createLayoutParamsForTabs() {
        final LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
        updateTabViewLayoutParams(lp);
        return lp;
    }

    private void updateTabViewLayoutParams(@NonNull LinearLayout.LayoutParams lp) {
        if (mode == MODE_FIXED && tabGravity == GRAVITY_FILL) {
            lp.width = 0;
            lp.weight = 1;
        } else if (mode == SESL_MODE_FIXED_AUTO || mode == SESL_MODE_WEIGHT_AUTO) {
            lp.width = LinearLayout.LayoutParams.WRAP_CONTENT;
            lp.weight = 0;
        } else {
            lp.width = LinearLayout.LayoutParams.WRAP_CONTENT;
            lp.weight = 0;
        }
    }

    @RequiresApi(VERSION_CODES.LOLLIPOP)
    @Override
    public void setElevation(float elevation) {
        super.setElevation(elevation);

        MaterialShapeUtils.setElevation(this, elevation);
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(@NonNull AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        AccessibilityNodeInfoCompat infoCompat = AccessibilityNodeInfoCompat.wrap(info);
        infoCompat.setCollectionInfo(CollectionInfoCompat.obtain(1, getTabCount(), false, CollectionInfoCompat.SELECTION_MODE_SINGLE));
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        for (int i = 0; i < slidingTabIndicator.getChildCount(); i++) {
            View tabView = slidingTabIndicator.getChildAt(i);
            if (tabView instanceof TabView) {
                ((TabView) tabView).drawBackground(canvas);
            }
        }

        super.onDraw(canvas);
    }

    @SuppressLint("RestrictedApi")
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int idealHeight = Math.round(ViewUtils.dpToPx(getContext(), getDefaultHeight()));
        switch (MeasureSpec.getMode(heightMeasureSpec)) {
            case MeasureSpec.AT_MOST:
                if (getChildCount() == 1 && MeasureSpec.getSize(heightMeasureSpec) >= idealHeight) {
                    getChildAt(0).setMinimumHeight(idealHeight);
                }
                break;
            case MeasureSpec.UNSPECIFIED:
                heightMeasureSpec = MeasureSpec.makeMeasureSpec(idealHeight + getPaddingTop() + getPaddingBottom(), MeasureSpec.EXACTLY);
                break;
            default:
                break;
        }

        final int specWidth = MeasureSpec.getSize(widthMeasureSpec);
        if (MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.UNSPECIFIED) {
            tabMaxWidth = requestedTabMaxWidth > 0 ? requestedTabMaxWidth : (int) (specWidth - ViewUtils.dpToPx(getContext(), TAB_MIN_WIDTH_MARGIN));
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (getChildCount() == 1) {
            final View child = getChildAt(0);
            boolean remeasure = false;

            switch (mode) {
                case MODE_AUTO:
                case MODE_SCROLLABLE:
                    remeasure = child.getMeasuredWidth() < getMeasuredWidth();
                    break;
                case MODE_FIXED:
                    remeasure = child.getMeasuredWidth() != getMeasuredWidth();
                    break;
                case SESL_MODE_FIXED_AUTO:
                case SESL_MODE_WEIGHT_AUTO:
                    remeasure = true;
                    break;
            }

            if (remeasure) {
                int childHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec, getPaddingTop() + getPaddingBottom(), child.getLayoutParams().height);

                int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(getMeasuredWidth(), MeasureSpec.EXACTLY);
                child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
            }

            checkOverScreen();
            if (mIsOverScreen && getChildAt(0).getMeasuredWidth() < getMeasuredWidth()) {
                setPaddingRelative((getMeasuredWidth() - getChildAt(0).getMeasuredWidth()) / 2, 0, 0, 0);
            } else {
                setPaddingRelative(0, 0, 0, 0);
            }
        }
    }

    private void removeTabViewAt(int position) {
        final TabView view = (TabView) slidingTabIndicator.getChildAt(position);
        slidingTabIndicator.removeViewAt(position);
        if (view != null) {
            view.reset();
            tabViewPool.release(view);
        }
        requestLayout();
    }

    private void animateToTab(int newPosition) {
        if (newPosition == Tab.INVALID_POSITION) {
            return;
        }

        if (getWindowToken() == null|| !ViewCompat.isLaidOut(this)|| slidingTabIndicator.childrenNeedLayout()) {
            setScrollPosition(newPosition, 0f, true);
            return;
        }

        final int startScrollX = getScrollX();
        final int targetScrollX = calculateScrollXForTab(newPosition, 0);

        if (startScrollX != targetScrollX) {
            ensureScrollAnimator();

            scrollAnimator.setIntValues(startScrollX, targetScrollX);
            scrollAnimator.start();
        }

        slidingTabIndicator.animateIndicatorToPosition(newPosition, tabIndicatorAnimationDuration);
    }

    @SuppressLint("RestrictedApi")
    private void ensureScrollAnimator() {
        if (scrollAnimator == null) {
            scrollAnimator = new ValueAnimator();
            scrollAnimator.setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR);
            scrollAnimator.setDuration(tabIndicatorAnimationDuration);
            scrollAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(@NonNull ValueAnimator animator) {
                    scrollTo((int) animator.getAnimatedValue(), 0);
                }
            });
        }
    }

    void setScrollAnimatorListener(ValueAnimator.AnimatorListener listener) {
        ensureScrollAnimator();
        scrollAnimator.addListener(listener);
    }

    private void setSelectedTabView(int position, boolean pressed) {
        final int tabCount = slidingTabIndicator.getChildCount();
        if (position < tabCount) {
            for (int i = 0; i < tabCount; i++) {
                final View child = slidingTabIndicator.getChildAt(i);
                child.setSelected(i == position);
                child.setActivated(i == position);
            }

            tabs.get(position).view.setSelected(true);

            for (int i = 0; i < getTabCount(); i++) {
                final TabView tabView = tabs.get(i).view;

                if (i == position) {
                    if (tabView.textView != null) {
                        startTextColorChangeAnimation(tabView.textView, getSelectedTabTextColor());
                        tabView.textView.setTypeface(mBoldTypeface);
                        tabView.textView.setSelected(true);
                    }
                    if (mDepthStyle == DEPTH_TYPE_SUB && tabView.mSubTextView != null) {
                        startTextColorChangeAnimation(tabView.mSubTextView, seslGetSelectedTabSubTextColor());
                        tabView.mSubTextView.setSelected(true);
                    }
                    if (tabView.mIndicatorView != null) {
                        if (!pressed) {
                            tabs.get(i).view.mIndicatorView.setReleased();
                        } else if (tabView.mIndicatorView.getAlpha() != 1.0f) {
                            tabView.mIndicatorView.setShow();
                        }
                    }
                } else {
                    if (tabView.mIndicatorView != null) {
                        tabView.mIndicatorView.setHide();
                    }
                    if (tabView.textView != null) {
                        tabView.textView.setTypeface(mNormalTypeface);
                        startTextColorChangeAnimation(tabView.textView, tabTextColors.getDefaultColor());
                        tabView.textView.setSelected(false);
                    }
                    if (mDepthStyle == DEPTH_TYPE_SUB && tabView.mSubTextView != null) {
                        startTextColorChangeAnimation(tabView.mSubTextView, mSubTabSubTextColors.getDefaultColor());
                        tabView.mSubTextView.setSelected(false);
                    }
                }
            }
        }
    }

    public void selectTab(@Nullable Tab tab) {
        selectTab(tab, true);
    }

    public void selectTab(@Nullable Tab tab, boolean updateIndicator) {
        selectTab(tab, updateIndicator, true);
    }

    public void selectTab(@Nullable final Tab tab, boolean updateIndicator, boolean pressed) {
        if (tab != null && !tab.view.isEnabled()) {
            if (viewPager != null) {
                viewPager.setCurrentItem(getSelectedTabPosition());
                return;
            }
        }

        final Tab currentTab = selectedTab;

        if (currentTab == tab) {
            if (currentTab != null) {
                dispatchTabReselected(tab);
                animateToTab(tab.getPosition());
            }
        } else {
            final int newPosition = tab != null ? tab.getPosition() : Tab.INVALID_POSITION;
            if (updateIndicator) {
                if ((currentTab == null || currentTab.getPosition() == Tab.INVALID_POSITION) && newPosition != Tab.INVALID_POSITION) {
                    setScrollPosition(newPosition, 0f, true);
                } else {
                    animateToTab(newPosition);
                }
                if (newPosition != Tab.INVALID_POSITION) {
                    setSelectedTabView(newPosition, pressed);
                }
            }
            selectedTab = tab;
            if (currentTab != null) {
                dispatchTabUnselected(currentTab);
            }
            if (tab != null) {
                dispatchTabSelected(tab);
            }
        }
    }

    private void dispatchTabSelected(@NonNull final Tab tab) {
        for (int i = selectedListeners.size() - 1; i >= 0; i--) {
            selectedListeners.get(i).onTabSelected(tab);
        }
    }

    private void dispatchTabUnselected(@NonNull final Tab tab) {
        for (int i = selectedListeners.size() - 1; i >= 0; i--) {
            selectedListeners.get(i).onTabUnselected(tab);
        }
    }

    private void dispatchTabReselected(@NonNull final Tab tab) {
        for (int i = selectedListeners.size() - 1; i >= 0; i--) {
            selectedListeners.get(i).onTabReselected(tab);
        }
    }

    private int calculateScrollXForTab(int position, float positionOffset) {
        if (mode == MODE_SCROLLABLE || mode == MODE_AUTO || mode == SESL_MODE_FIXED_AUTO || mode == SESL_MODE_WEIGHT_AUTO) {
            final View selectedChild = slidingTabIndicator.getChildAt(position);
            final View nextChild = position + 1 < slidingTabIndicator.getChildCount() ? slidingTabIndicator.getChildAt(position + 1) : null;
            final int selectedWidth = selectedChild != null ? selectedChild.getWidth() : 0;
            final int nextWidth = nextChild != null ? nextChild.getWidth() : 0;

            int scrollBase = selectedChild.getLeft() + (selectedWidth / 2) - (getWidth() / 2);
            int scrollOffset = (int) ((selectedWidth + nextWidth) * 0.5f * positionOffset);

            return (ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_LTR) ? scrollBase + scrollOffset : scrollBase - scrollOffset;
        }
        return 0;
    }

    private void applyModeAndGravity() {
        ViewCompat.setPaddingRelative(slidingTabIndicator, 0, 0, 0, 0);

        switch (mode) {
            case MODE_AUTO:
            case MODE_FIXED:
                if (tabGravity == GRAVITY_START) {
                    Log.w(LOG_TAG, "GRAVITY_START is not supported with the current tab mode, GRAVITY_CENTER will be used instead");
                }
                slidingTabIndicator.setGravity(Gravity.CENTER_HORIZONTAL);
                break;
            case MODE_SCROLLABLE:
            case SESL_MODE_FIXED_AUTO:
            case SESL_MODE_WEIGHT_AUTO:
                applyGravityForModeScrollable(tabGravity);
                break;
        }

        updateTabViews(true);
    }

    private void applyGravityForModeScrollable(int tabGravity) {
        switch (tabGravity) {
            case GRAVITY_CENTER:
                slidingTabIndicator.setGravity(Gravity.CENTER_HORIZONTAL);
                break;
            case GRAVITY_FILL:
                Log.w(LOG_TAG, "MODE_SCROLLABLE + GRAVITY_FILL is not supported, GRAVITY_START will be used instead");
            case GRAVITY_START:
                slidingTabIndicator.setGravity(GravityCompat.START);
                break;
            default:
                break;
        }
    }

    void updateTabViews(final boolean requestLayout) {
        for (int i = 0; i < slidingTabIndicator.getChildCount(); i++) {
            View child = slidingTabIndicator.getChildAt(i);
            child.setMinimumWidth(getTabMinWidth());
            updateTabViewLayoutParams((LinearLayout.LayoutParams) child.getLayoutParams());
            if (requestLayout) {
                child.requestLayout();
            }
        }
        updateBadgePosition();
    }

    public static class Tab {
        public static final int INVALID_POSITION = -1;
        @Nullable private Object tag;
        @Nullable private Drawable icon;
        @Nullable private CharSequence text;
        @Nullable private CharSequence subText;
        @Nullable private CharSequence contentDesc;
        private int position = INVALID_POSITION;
        @Nullable private View customView;
        private @LabelVisibility int labelVisibilityMode = TAB_LABEL_VISIBILITY_LABELED;
        @Nullable public SamsungTabLayout parent;
        @NonNull public TabView view;
        private int id = NO_ID;

        public Tab() {
        }

        @Nullable
        public Object getTag() {
            return tag;
        }

        @NonNull
        public Tab setTag(@Nullable Object tag) {
            this.tag = tag;
            return this;
        }

        @NonNull
        public Tab setId(int id) {
            this.id = id;
            if (view != null) {
                view.setId(id);
            }
            return this;
        }

        public int getId() {
            return id;
        }

        @Nullable
        public View getCustomView() {
            return customView;
        }

        @NonNull
        public Tab setCustomView(@Nullable View view) {
            if (this.view.textView != null) {
                this.view.removeAllViews();
            }
            customView = view;
            updateView();
            return this;
        }

        @NonNull
        public Tab setCustomView(@LayoutRes int resId) {
            final LayoutInflater inflater = LayoutInflater.from(view.getContext());
            return setCustomView(inflater.inflate(resId, view, false));
        }

        @Nullable
        public Drawable getIcon() {
            return icon;
        }

        public int getPosition() {
            return position;
        }

        void setPosition(int position) {
            this.position = position;
        }

        @Nullable
        public CharSequence getText() {
            return text;
        }

        @Nullable
        public CharSequence seslGetSubText() {
            return subText;
        }

        @SuppressLint("UnsafeOptInUsageError")
        @NonNull
        public Tab setIcon(@Nullable Drawable icon) {
            this.icon = icon;
            if ((parent.tabGravity == GRAVITY_CENTER) || parent.mode == MODE_AUTO) {
                parent.updateTabViews(true);
            }
            updateView();
            if (BadgeUtils.USE_COMPAT_PARENT && view.hasBadgeDrawable() && view.badgeDrawable.isVisible()) {
                view.invalidate();
            }
            return this;
        }

        @NonNull
        public Tab setIcon(@DrawableRes int resId) {
            if (parent == null) {
                throw new IllegalArgumentException("Tab not attached to a TabLayout");
            }
            return setIcon(AppCompatResources.getDrawable(parent.getContext(), resId));
        }

        @NonNull
        public Tab setText(@Nullable CharSequence text) {
            if (TextUtils.isEmpty(contentDesc) && !TextUtils.isEmpty(text)) {
                view.setContentDescription(text);
            }

            this.text = text;
            updateView();
            return this;
        }

        @NonNull
        public Tab seslSetSubText(@Nullable CharSequence subText) {
            this.subText = subText;
            updateView();
            return this;
        }

        @NonNull
        public Tab setText(@StringRes int resId) {
            if (parent == null) {
                throw new IllegalArgumentException("Tab not attached to a TabLayout");
            }
            return setText(parent.getResources().getText(resId));
        }

        @NonNull
        public BadgeDrawable getOrCreateBadge() {
            return view.getOrCreateBadge();
        }

        public void removeBadge() {
            view.removeBadge();
        }

        @Nullable
        public BadgeDrawable getBadge() {
            return view.getBadge();
        }

        @SuppressLint("UnsafeOptInUsageError")
        @NonNull
        public Tab setTabLabelVisibility(@LabelVisibility int mode) {
            this.labelVisibilityMode = mode;
            if ((parent.tabGravity == GRAVITY_CENTER) || parent.mode == MODE_AUTO) {
                parent.updateTabViews(true);
            }
            this.updateView();
            if (BadgeUtils.USE_COMPAT_PARENT && view.hasBadgeDrawable() && view.badgeDrawable.isVisible()) {
                view.invalidate();
            }
            return this;
        }

        @LabelVisibility
        public int getTabLabelVisibility() {
            return this.labelVisibilityMode;
        }

        public void select() {
            if (parent == null) {
                throw new IllegalArgumentException("Tab not attached to a TabLayout");
            }
            parent.selectTab(this);
        }

        public boolean isSelected() {
            if (parent == null) {
                throw new IllegalArgumentException("Tab not attached to a TabLayout");
            }
            int selectedPosition = parent.getSelectedTabPosition();
            return selectedPosition == position;
        }

        @NonNull
        public Tab setContentDescription(@StringRes int resId) {
            if (parent == null) {
                throw new IllegalArgumentException("Tab not attached to a TabLayout");
            }
            return setContentDescription(parent.getResources().getText(resId));
        }

        @NonNull
        public Tab setContentDescription(@Nullable CharSequence contentDesc) {
            this.contentDesc = contentDesc;
            updateView();
            return this;
        }

        @Nullable
        public CharSequence getContentDescription() {
            return (view == null) ? null : view.getContentDescription();
        }

        void updateView() {
            if (view != null) {
                view.update();
            }
        }

        void reset() {
            parent = null;
            view = null;
            tag = null;
            icon = null;
            id = NO_ID;
            text = null;
            contentDesc = null;
            position = INVALID_POSITION;
            customView = null;
            subText = null;
        }

        public TextView seslGetTextView() {
            if (customView != null || view == null) {
                return null;
            }
            return view.textView;
        }

        public TextView seslGetSubTextView() {
            if (customView != null || view == null) {
                return null;
            }
            return view.mSubTextView;
        }
    }

    public final class TabView extends LinearLayout {
        private Tab tab;
        private TextView textView;
        private ImageView iconView;
        @Nullable private View badgeAnchorView;
        @Nullable private BadgeDrawable badgeDrawable;

        @Nullable private View customView;
        @Nullable private TextView customTextView;
        @Nullable private ImageView customIconView;
        @Nullable private Drawable baseBackgroundDrawable;

        private TextView mDotBadgeView;
        private int mIconSize;
        private SeslAbsIndicatorView mIndicatorView;
        private boolean mIsCallPerformClick;
        private View mMainTabTouchBackground;
        private TextView mNBadgeView;
        private TextView mSubTextView;
        private RelativeLayout mTabParentView;

        private int defaultMaxLines = 2;

        View.OnKeyListener mTabViewKeyListener = new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                return false;
            }
        };

        public TabView(@NonNull Context context) {
            super(context);
            updateBackgroundDrawable(context);
            setGravity(Gravity.CENTER);
            setOrientation(inlineLabel ? HORIZONTAL : VERTICAL);
            setClickable(true);
            setOnKeyListener(mTabViewKeyListener);
            if (mDepthStyle == DEPTH_TYPE_MAIN) {
                ViewCompat.setPaddingRelative(this, 0, tabPaddingTop, 0, tabPaddingBottom);
            }
            mIconSize = getResources().getDimensionPixelOffset(R.dimen.sesl_tab_icon_size);
        }

        @SuppressLint("RestrictedApi")
        private void updateBackgroundDrawable(Context context) {
            if (tabBackgroundResId != 0) {
                baseBackgroundDrawable = AppCompatResources.getDrawable(context, tabBackgroundResId);
                if (baseBackgroundDrawable != null && baseBackgroundDrawable.isStateful()) {
                    baseBackgroundDrawable.setState(getDrawableState());
                }
                ViewCompat.setBackground(this, baseBackgroundDrawable);
            } else {
                baseBackgroundDrawable = null;
            }
        }

        private void drawBackground(@NonNull Canvas canvas) {
            if (baseBackgroundDrawable != null) {
                baseBackgroundDrawable.setBounds(getLeft(), getTop(), getRight(), getBottom());
                baseBackgroundDrawable.draw(canvas);
            }
        }

        @Override
        protected void drawableStateChanged() {
            super.drawableStateChanged();
            int[] state = getDrawableState();
            if (baseBackgroundDrawable != null && baseBackgroundDrawable.isStateful()) {
                baseBackgroundDrawable.setState(state);
            }
        }

        @Override
        public boolean performClick() {
            if (mIsCallPerformClick) {
                mIsCallPerformClick = false;
                return true;
            }

            final boolean handled = super.performClick();

            if (tab != null) {
                if (!handled) {
                    playSoundEffect(SoundEffectConstants.CLICK);
                }
                tab.select();
                return true;
            } else {
                return handled;
            }
        }

        @Override
        public void setSelected(final boolean selected) {
            if (isEnabled()) {
                final boolean changed = isSelected() != selected;

                super.setSelected(selected);

                if (changed && selected && Build.VERSION.SDK_INT < 16) {
                    sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_SELECTED);
                }

                if (textView != null) {
                    textView.setSelected(selected);
                }
                if (iconView != null) {
                    iconView.setSelected(selected);
                }
                if (customView != null) {
                    customView.setSelected(selected);
                }
                if (mIndicatorView != null) {
                    mIndicatorView.setSelected(selected);
                }
                if (mSubTextView != null) {
                    mSubTextView.setSelected(selected);
                }
            }
        }

        @Override
        public void onInitializeAccessibilityNodeInfo(@NonNull AccessibilityNodeInfo info) {
            super.onInitializeAccessibilityNodeInfo(info);
            if (badgeDrawable != null && badgeDrawable.isVisible()) {
                CharSequence customContentDescription = getContentDescription();
                info.setContentDescription(customContentDescription + ", " + badgeDrawable.getContentDescription());
            }
            AccessibilityNodeInfoCompat infoCompat = AccessibilityNodeInfoCompat.wrap(info);
            infoCompat.setCollectionItemInfo(CollectionItemInfoCompat.obtain(0,1, tab.getPosition(), 1, false, isSelected()));
            if (isSelected()) {
                infoCompat.setClickable(false);
                infoCompat.removeAction(AccessibilityActionCompat.ACTION_CLICK);
            }
            infoCompat.setRoleDescription(getResources().getString(R.string.item_view_role_description));
            if (mNBadgeView != null && mNBadgeView.getVisibility() == VISIBLE && mNBadgeView.getContentDescription() != null) {
                CharSequence customContentDescription = getContentDescription();
                infoCompat.setContentDescription(customContentDescription + ", " + mNBadgeView.getContentDescription());
            }
        }

        @Override
        public void onMeasure(final int origWidthMeasureSpec, final int origHeightMeasureSpec) {
            final int specWidthSize = MeasureSpec.getSize(origWidthMeasureSpec);
            final int specWidthMode = MeasureSpec.getMode(origWidthMeasureSpec);
            final int maxWidth = getTabMaxWidth();

            int widthMeasureSpec = origWidthMeasureSpec;
            final int heightMeasureSpec = origHeightMeasureSpec;

            if (mode != SESL_MODE_FIXED_AUTO && mode != SESL_MODE_WEIGHT_AUTO) {
                if (mRequestedTabWidth != -1) {
                    widthMeasureSpec = MeasureSpec.makeMeasureSpec(mRequestedTabWidth, MeasureSpec.EXACTLY);
                } else if (maxWidth > 0 && (specWidthMode == MeasureSpec.UNSPECIFIED || specWidthSize > maxWidth)) {
                    widthMeasureSpec = MeasureSpec.makeMeasureSpec(tabMaxWidth, MeasureSpec.AT_MOST);
                }
            } else {
                if (specWidthMode == 0) {
                    widthMeasureSpec = MeasureSpec.makeMeasureSpec(tabMaxWidth, MeasureSpec.UNSPECIFIED);
                } else if (specWidthMode == MeasureSpec.EXACTLY) {
                    widthMeasureSpec = MeasureSpec.makeMeasureSpec(specWidthSize, MeasureSpec.EXACTLY);
                }
            }

            super.onMeasure(widthMeasureSpec, heightMeasureSpec);

            if (textView != null && customView == null) {
                float textSize = tabTextSize;
                checkMaxFontScale(textView, (int) textSize);
                if (mDepthStyle == DEPTH_TYPE_SUB && mSubTextView != null) {
                    checkMaxFontScale(mSubTextView, mSubTabTextSize);
                }
                int maxLines = defaultMaxLines;

                if (iconView != null && iconView.getVisibility() == VISIBLE) {
                    maxLines = 1;
                } else if (textView != null && textView.getLineCount() > 1) {
                    textSize = tabTextMultiLineSize;
                }

                final float curTextSize = textView.getTextSize();
                final int curLineCount = textView.getLineCount();
                final int curMaxLines = TextViewCompat.getMaxLines(textView);

                if (textSize != curTextSize || (curMaxLines >= 0 && maxLines != curMaxLines)) {
                    boolean updateTextView = true;

                    if (mode == MODE_FIXED && textSize > curTextSize && curLineCount == 1) {
                        final Layout layout = textView.getLayout();
                        if (layout == null || approximateLineWidth(layout, 0, textSize) > getMeasuredWidth() - getPaddingLeft() - getPaddingRight()) {
                            updateTextView = false;
                        }
                    }

                    if (updateTextView) {
                        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
                        checkMaxFontScale(textView, (int) textSize);
                        if (mDepthStyle == DEPTH_TYPE_SUB && mSubTextView != null) {
                            checkMaxFontScale(mSubTextView, mSubTabTextSize);
                        }
                        textView.setMaxLines(maxLines);
                        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                    }
                }
            }
            if (customTextView == null && mTabParentView != null && textView != null && tab != null && mode == MODE_SCROLLABLE && mDepthStyle == DEPTH_TYPE_SUB) {
                if (tabMaxWidth > 0) {
                    textView.measure(tabMaxWidth, 0);
                } else {
                    textView.measure(0, 0);
                }

                ViewGroup.LayoutParams lp = mTabParentView.getLayoutParams();
                lp.width = textView.getMeasuredWidth() + (getContext().getResources().getDimensionPixelSize(R.dimen.sesl_tablayout_subtab_side_space) * 2);
                mTabParentView.setLayoutParams(lp);

                super.onMeasure(MeasureSpec.makeMeasureSpec(lp.width, MeasureSpec.AT_MOST), origHeightMeasureSpec);
            }
        }

        void setTab(@Nullable final Tab tab) {
            if (tab != this.tab) {
                this.tab = tab;
                update();
            }
        }

        void reset() {
            setTab(null);
            setSelected(false);
        }

        @SuppressLint("RestrictedApi")
        final void update() {
            final Tab tab = this.tab;
            final View custom = tab != null ? tab.getCustomView() : null;
            if (custom != null) {
                final ViewParent customParent = custom.getParent();
                if (customParent != this) {
                    if (customParent != null) {
                        ((ViewGroup) customParent).removeView(custom);
                    }
                    addView(custom);
                }
                customView = custom;
                if (this.textView != null) {
                    this.textView.setVisibility(GONE);
                }
                if (this.iconView != null) {
                    this.iconView.setVisibility(GONE);
                    this.iconView.setImageDrawable(null);
                }
                if (mSubTextView != null) {
                    mSubTextView.setVisibility(GONE);
                }

                customTextView = custom.findViewById(android.R.id.text1);
                if (customTextView != null) {
                    defaultMaxLines = TextViewCompat.getMaxLines(customTextView);
                }
                customIconView = custom.findViewById(android.R.id.icon);
            } else {
                if (customView != null) {
                    removeView(customView);
                    customView = null;
                }
                customTextView = null;
                customIconView = null;
            }

            if (customView == null && tab != null) {
                if (mTabParentView == null) {
                    if (mDepthStyle == DEPTH_TYPE_SUB) {
                        mTabParentView = (RelativeLayout) LayoutInflater.from(getContext()).inflate(R.layout.sesl_tabs_sub_tab_layout, this, false);
                    } else {
                        mTabParentView = (RelativeLayout) LayoutInflater.from(getContext()).inflate(R.layout.sesl_tabs_main_tab_layout, this, false);
                        mMainTabTouchBackground = mTabParentView.findViewById(R.id.main_tab_touch_background);
                        if (mMainTabTouchBackground != null && tab.icon == null) {
                            ViewCompat.setBackground(mMainTabTouchBackground, ContextCompat.getDrawable(getContext(), R.drawable.sesl_tablayout_maintab_touch_background));
                            mMainTabTouchBackground.setAlpha(0.0f);
                        }
                    }
                }

                if (mIndicatorView == null) {
                    mIndicatorView = mTabParentView.findViewById(R.id.indicator);
                }
                if (mDepthStyle != DEPTH_TYPE_SUB) {
                    if (mIndicatorView != null) {
                        if (!mIsOneUI4) {
                            ViewGroup.LayoutParams lp = mIndicatorView.getLayoutParams();
                            lp.height = (int) ViewUtils.dpToPx(getContext(), 4);
                            mIndicatorView.setLayoutParams(lp);
                        }
                        mIndicatorView.setSelectedIndicatorColor(mTabSelectedIndicatorColor);
                    }
                } else {
                    if (mIndicatorView != null && mSubTabSelectedIndicatorColor != -1) {
                        mIndicatorView.setSelectedIndicatorColor(mSubTabSelectedIndicatorColor);
                    }
                }

                if (textView == null) {
                    textView = mTabParentView.findViewById(R.id.title);
                }
                defaultMaxLines = TextViewCompat.getMaxLines(textView);
                TextViewCompat.setTextAppearance(textView, tabTextAppearance);
                if (isSelected()) {
                    textView.setTypeface(mBoldTypeface);
                } else {
                    textView.setTypeface(mNormalTypeface);
                }
                checkMaxFontScale(textView, (int) tabTextSize);
                textView.setTextColor(tabTextColors);

                if (mDepthStyle == DEPTH_TYPE_SUB) {
                    if (mSubTextView == null) {
                        mSubTextView = mTabParentView.findViewById(R.id.sub_title);
                    }
                    TextViewCompat.setTextAppearance(mSubTextView, mSubTabSubTextAppearance);
                    mSubTextView.setTextColor(mSubTabSubTextColors);
                    if (mSubTextView != null) {
                        checkMaxFontScale(mSubTextView, mSubTabTextSize);
                    }
                }

                if (iconView == null && mTabParentView != null) {
                    iconView = mTabParentView.findViewById(R.id.icon);
                }

                Drawable iconDrawable = (tab != null && tab.getIcon() != null) ? DrawableCompat.wrap(tab.getIcon()).mutate() : null;
                if (iconDrawable != null) {
                    DrawableCompat.setTintList(iconDrawable, tabIconTint);
                    if (tabIconTintMode != null) {
                        DrawableCompat.setTintMode(iconDrawable, tabIconTintMode);
                    }
                }

                seslUpdateTextAndIcon(textView, mSubTextView, iconView);

                //kang
                boolean z;
                int i;
                int i2 = -1;
                CharSequence charSequence = null;
                if (SamsungTabLayout.this.mDepthStyle == 2) {
                    if (mode == MODE_SCROLLABLE) {
                        i2 = -2;
                    }
                    if (tab != null) {
                        charSequence = tab.seslGetSubText();
                    }
                    i = !TextUtils.isEmpty(charSequence) ? SamsungTabLayout.this.mSubTabIndicator2ndHeight : SamsungTabLayout.this.mSubTabIndicatorHeight;
                    z = this.mTabParentView.getHeight() != i;
                } else {
                    z = false;
                    if (this.tab.icon != null) {
                        i = -1;
                        i2 = -2;
                    } else {
                        i = -1;
                    }
                }

                if (mTabParentView.getParent() == null) {
                    addView(mTabParentView, i2, i);
                } else if (z) {
                    removeView(mTabParentView);
                    addView(mTabParentView, i2, i);
                }
                //kang

                tryUpdateBadgeAnchor();
                addOnLayoutChangeListener(iconView);
                addOnLayoutChangeListener(textView);
            } else {
                if (customTextView != null || customIconView != null) {
                    updateTextAndIcon(customTextView, customIconView);
                }
            }

            if (tab != null && !TextUtils.isEmpty(tab.contentDesc)) {
                setContentDescription(tab.contentDesc);
            }
            setSelected(tab != null && tab.isSelected());
        }

        @SuppressLint("UnsafeOptInUsageError")
        private void inflateAndAddDefaultIconView() {
            ViewGroup iconViewParent = this;
            if (BadgeUtils.USE_COMPAT_PARENT) {
                iconViewParent = createPreApi18BadgeAnchorRoot();
                addView(iconViewParent, 0);
            }
            this.iconView = (ImageView) LayoutInflater.from(getContext()).inflate(R.layout.sesl_layout_tab_icon, iconViewParent, false);
            iconViewParent.addView(iconView, 0);
        }

        @SuppressLint("UnsafeOptInUsageError")
        private void inflateAndAddDefaultTextView() {
            ViewGroup textViewParent = this;
            if (BadgeUtils.USE_COMPAT_PARENT) {
                textViewParent = createPreApi18BadgeAnchorRoot();
                addView(textViewParent);
            }
            this.textView = (TextView) LayoutInflater.from(getContext()).inflate(R.layout.sesl_layout_tab_text, textViewParent, false);
            textViewParent.addView(textView);
        }

        @SuppressLint("UnsafeOptInUsageError")
        private void inflateAndAddDefaultSubTextView() {
            ViewGroup textViewParent = this;
            if (BadgeUtils.USE_COMPAT_PARENT) {
                textViewParent = createPreApi18BadgeAnchorRoot();
                addView(textViewParent);
            }
            this.textView = (TextView) LayoutInflater.from(getContext()).inflate(R.layout.sesl_layout_tab_sub_text, textViewParent, false);
            textViewParent.addView(textView);
        }

        @NonNull
        private FrameLayout createPreApi18BadgeAnchorRoot() {
            FrameLayout frameLayout = new FrameLayout(getContext());
            FrameLayout.LayoutParams layoutparams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            frameLayout.setLayoutParams(layoutparams);
            return frameLayout;
        }

        @NonNull
        private BadgeDrawable getOrCreateBadge() {
            if (badgeDrawable == null) {
                badgeDrawable = BadgeDrawable.create(getContext());
            }
            tryUpdateBadgeAnchor();
            if (badgeDrawable == null) {
                throw new IllegalStateException("Unable to create badge");
            }
            return badgeDrawable;
        }

        @Nullable
        private BadgeDrawable getBadge() {
            return badgeDrawable;
        }

        private void removeBadge() {
            if (badgeAnchorView != null) {
                tryRemoveBadgeFromAnchor();
            }
            badgeDrawable = null;
        }

        private void addOnLayoutChangeListener(@Nullable final View view) {
            if (view == null) {
                return;
            }
            view.addOnLayoutChangeListener(new OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    if (view.getVisibility() == VISIBLE) {
                        tryUpdateBadgeDrawableBounds(view);
                    }
                }
            });
        }

        private void tryUpdateBadgeAnchor() {
            if (!hasBadgeDrawable()) {
                return;
            }
            if (customView != null) {
                tryRemoveBadgeFromAnchor();
            } else {
                if (iconView != null && tab != null && tab.getIcon() != null) {
                    if (badgeAnchorView != iconView) {
                        tryRemoveBadgeFromAnchor();
                        tryAttachBadgeToAnchor(iconView);
                    } else {
                        tryUpdateBadgeDrawableBounds(iconView);
                    }
                } else if (textView != null && tab != null && tab.getTabLabelVisibility() == TAB_LABEL_VISIBILITY_LABELED) {
                    if (badgeAnchorView != textView) {
                        tryRemoveBadgeFromAnchor();
                        tryAttachBadgeToAnchor(textView);
                    } else {
                        tryUpdateBadgeDrawableBounds(textView);
                    }
                } else {
                    tryRemoveBadgeFromAnchor();
                }
            }
        }

        @SuppressLint("UnsafeOptInUsageError")
        private void tryAttachBadgeToAnchor(@Nullable View anchorView) {
            if (!hasBadgeDrawable()) {
                return;
            }
            if (anchorView != null) {
                clipViewToPaddingForBadge(false);
                BadgeUtils.attachBadgeDrawable(badgeDrawable, anchorView, getCustomParentForBadge(anchorView));
                badgeAnchorView = anchorView;
            }
        }

        @SuppressLint("UnsafeOptInUsageError")
        private void tryRemoveBadgeFromAnchor() {
            if (!hasBadgeDrawable()) {
                return;
            }
            clipViewToPaddingForBadge(true);
            if (badgeAnchorView != null) {
                BadgeUtils.detachBadgeDrawable(badgeDrawable, badgeAnchorView);
                badgeAnchorView = null;
            }
        }

        private void clipViewToPaddingForBadge(boolean flag) {
            setClipChildren(flag);
            setClipToPadding(flag);
            ViewGroup parent = (ViewGroup) getParent();
            if (parent != null) {
                parent.setClipChildren(flag);
                parent.setClipToPadding(flag);
            }
        }

        final void updateOrientation() {
            setOrientation(inlineLabel ? HORIZONTAL : VERTICAL);
            if (customTextView != null || customIconView != null) {
                updateTextAndIcon(customTextView, customIconView);
            } else {
                updateTextAndIcon(textView, iconView);
            }
        }

        private void seslUpdateTextAndIcon(@Nullable final TextView textView, @Nullable final TextView subTextView, @Nullable final ImageView iconView) {
            updateTextAndIcon(textView, iconView);

            if (subTextView != null) {
                CharSequence subText = tab != null ? tab.seslGetSubText() : null;
                RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) textView.getLayoutParams();
                if (!TextUtils.isEmpty(subText)) {
                    lp.removeRule(13);
                    lp.addRule(2, R.id.center_anchor);
                    subTextView.setText(subText);
                    if (tab.labelVisibilityMode == TAB_LABEL_VISIBILITY_LABELED) {
                        subTextView.setVisibility(VISIBLE);
                    } else {
                        subTextView.setVisibility(GONE);
                    }
                    setVisibility(VISIBLE);
                } else {
                    lp.addRule(13);
                    lp.removeRule(2);
                    subTextView.setVisibility(GONE);
                    subTextView.setText(null);
                }
            }
        }

        @SuppressLint("RestrictedApi")
        private void updateTextAndIcon(@Nullable final TextView textView, @Nullable final ImageView iconView) {
            final Drawable icon = (tab != null && tab.getIcon() != null) ? DrawableCompat.wrap(tab.getIcon()).mutate() : null;
            if (icon != null) {
                DrawableCompat.setTintList(icon, tabIconTint);
                if (tabIconTintMode != null) {
                    DrawableCompat.setTintMode(icon, tabIconTintMode);
                }
            }

            final CharSequence text = tab != null ? tab.getText() : null;

            if (iconView != null) {
                if (icon != null) {
                    iconView.setImageDrawable(icon);
                    iconView.setVisibility(VISIBLE);
                    setVisibility(VISIBLE);
                } else {
                    iconView.setVisibility(GONE);
                    iconView.setImageDrawable(null);
                }
            }

            final boolean hasText = !TextUtils.isEmpty(text);
            if (textView != null) {
                if (hasText) {
                    textView.setText(text);
                    if (tab.labelVisibilityMode == TAB_LABEL_VISIBILITY_LABELED) {
                        textView.setVisibility(VISIBLE);
                    } else {
                        textView.setVisibility(GONE);
                    }
                    setVisibility(VISIBLE);
                } else {
                    textView.setVisibility(GONE);
                    textView.setText(null);
                }
            }

            if (iconView != null) {
                MarginLayoutParams lp = ((MarginLayoutParams) iconView.getLayoutParams());
                int iconMargin = 0;
                if (hasText && iconView.getVisibility() == VISIBLE) {
                    iconMargin = mIconTextGap != -1 ? mIconTextGap : (int) ViewUtils.dpToPx(getContext(), DEFAULT_GAP_TEXT_ICON);
                }
                if (iconMargin != MarginLayoutParamsCompat.getMarginEnd(lp)) {
                    MarginLayoutParamsCompat.setMarginEnd(lp, iconMargin);
                    lp.bottomMargin = 0;
                    iconView.setLayoutParams(lp);
                    iconView.requestLayout();
                    if (textView != null) {
                        RelativeLayout.LayoutParams lp2 = (RelativeLayout.LayoutParams) textView.getLayoutParams();
                        lp2.addRule(13, 0);
                        lp2.addRule(15, 1);
                        lp2.addRule(17, R.id.icon);
                        textView.setLayoutParams(lp2);
                    }
                }
            }

            final CharSequence contentDesc = tab != null ? tab.contentDesc : null;
            if (VERSION.SDK_INT < VERSION_CODES.LOLLIPOP || VERSION.SDK_INT > VERSION_CODES.M) {
                Tooltip.setTooltipText(this, contentDesc);
            }
        }

        @SuppressLint("UnsafeOptInUsageError")
        private void tryUpdateBadgeDrawableBounds(@NonNull View anchor) {
            if (hasBadgeDrawable() && anchor == badgeAnchorView) {
                BadgeUtils.setBadgeDrawableBounds(badgeDrawable, anchor, getCustomParentForBadge(anchor));
            }
        }

        private boolean hasBadgeDrawable() {
            return badgeDrawable != null;
        }

        @SuppressLint("UnsafeOptInUsageError")
        @Nullable
        private FrameLayout getCustomParentForBadge(@NonNull View anchor) {
            if (anchor != iconView && anchor != textView) {
                return null;
            }
            return BadgeUtils.USE_COMPAT_PARENT ? ((FrameLayout) anchor.getParent()) : null;
        }

        int getContentWidth() {
            boolean initialized = false;
            int left = 0;
            int right = 0;

            for (View view : new View[] {textView, iconView, customView}) {
                if (view != null && view.getVisibility() == View.VISIBLE) {
                    left = initialized ? Math.min(left, view.getLeft()) : view.getLeft();
                    right = initialized ? Math.max(right, view.getRight()) : view.getRight();
                    initialized = true;
                }
            }

            return right - left;
        }

        int getContentHeight() {
            boolean initialized = false;
            int top = 0;
            int bottom = 0;

            for (View view : new View[] {textView, iconView, customView}) {
                if (view != null && view.getVisibility() == View.VISIBLE) {
                    top = initialized ? Math.min(top, view.getTop()) : view.getTop();
                    bottom = initialized ? Math.max(bottom, view.getBottom()) : view.getBottom();
                    initialized = true;
                }
            }

            return bottom - top;
        }

        @Nullable
        public Tab getTab() {
            return tab;
        }

        private float approximateLineWidth(@NonNull Layout layout, int line, float textSize) {
            return layout.getLineWidth(line) * (textSize / layout.getPaint().getTextSize());
        }

        @Override
        protected void onConfigurationChanged(Configuration newConfig) {
            super.onConfigurationChanged(newConfig);
            mIconSize = getResources().getDimensionPixelOffset(R.dimen.sesl_tab_icon_size);
        }

        @Override
        public void setEnabled(boolean enabled) {
            super.setEnabled(enabled);
            if (mMainTabTouchBackground != null) {
                mMainTabTouchBackground.setVisibility(enabled ? VISIBLE : GONE);
            }
        }

        @SuppressLint("RestrictedApi")
        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
            super.onLayout(changed, l, t, r, b);
            if (mMainTabTouchBackground != null) {
                mMainTabTouchBackground.setLeft(0);
                mMainTabTouchBackground.setRight(mTabParentView != null ? mTabParentView.getWidth() : r - l);
                if (mMainTabTouchBackground.getAnimation() != null && mMainTabTouchBackground.getAnimation().hasEnded()) {
                    mMainTabTouchBackground.setAlpha(0.0f);
                }
            }

            if (iconView != null && tab.icon != null) {
                if (textView != null && mIndicatorView != null && mTabParentView != null) {
                    int measuredWidth = mIconSize + textView.getMeasuredWidth();
                    if (mIconTextGap != -1) {
                        measuredWidth += mIconTextGap;
                    }

                    int offset = Math.abs((getWidth() - measuredWidth) / 2);
                    if (ViewUtils.isLayoutRtl(this)) {
                        if (iconView.getRight() == mTabParentView.getRight()) {
                            textView.offsetLeftAndRight(-offset);
                            iconView.offsetLeftAndRight(-offset);
                            mIndicatorView.offsetLeftAndRight(-offset);
                        }
                    } else if (iconView.getLeft() == this.mTabParentView.getLeft()) {
                        textView.offsetLeftAndRight(offset);
                        iconView.offsetLeftAndRight(offset);
                        mIndicatorView.offsetLeftAndRight(offset);
                    }
                }
            }
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if (isEnabled()) {
                return tab.getCustomView() != null ? super.onTouchEvent(event) : startTabTouchAnimation(event, null);
            } else {
                return super.onTouchEvent(event);
            }
        }

        private boolean startTabTouchAnimation(MotionEvent event, KeyEvent keyEvent) {
            if (event != null && tab.getCustomView() == null && textView != null && (event != null || keyEvent != null) && (event == null || keyEvent == null)) {
                final int action = event.getAction() & 255;

                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        mIsCallPerformClick = false;
                        if (tab.position != getSelectedTabPosition() && textView != null) {
                            textView.setTypeface(mBoldTypeface);
                            startTextColorChangeAnimation(textView, getSelectedTabTextColor());

                            if (mIndicatorView != null) {
                                mIndicatorView.setPressed();
                            }

                            final Tab tab = getTabAt(getSelectedTabPosition());
                            if (tab != null) {
                                if (tab.view.textView != null) {
                                    tab.view.textView.setTypeface(mNormalTypeface);
                                    startTextColorChangeAnimation(tab.view.textView, tabTextColors.getDefaultColor());
                                }
                                if (tab.view.mIndicatorView != null) {
                                    tab.view.mIndicatorView.setHide();
                                }
                            }
                        } else if (tab.position == getSelectedTabPosition() && mIndicatorView != null) {
                            mIndicatorView.setPressed();
                        }
                        showMainTabTouchBackground(MotionEvent.ACTION_DOWN);
                        break;
                    case MotionEvent.ACTION_UP:
                        showMainTabTouchBackground(MotionEvent.ACTION_UP);
                        if (mIndicatorView != null) {
                            mIndicatorView.setReleased();
                            mIndicatorView.onTouchEvent(event);
                        }
                        performClick();
                        mIsCallPerformClick = true;
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        textView.setTypeface(mNormalTypeface);
                        startTextColorChangeAnimation(textView, tabTextColors.getDefaultColor());

                        if (mIndicatorView != null && !mIndicatorView.isSelected()) {
                            mIndicatorView.setHide();
                        }

                        final Tab tab = getTabAt(getSelectedTabPosition());
                        if (tab != null) {
                            if (tab.view.textView != null) {
                                tab.view.textView.setTypeface(mBoldTypeface);
                                startTextColorChangeAnimation(tab.view.textView, getSelectedTabTextColor());
                            }
                            if (tab.view.mIndicatorView != null) {
                                tab.view.mIndicatorView.setShow();
                            }
                        }
                        if (mDepthStyle == DEPTH_TYPE_MAIN) {
                            showMainTabTouchBackground(MotionEvent.ACTION_CANCEL);
                        } else {
                            if (mIndicatorView != null && mIndicatorView.isSelected()) {
                                mIndicatorView.setReleased();
                            }
                        }
                        break;
                }

                return super.onTouchEvent(event);
            }
            return false;
        }

        private void showMainTabTouchBackground(int action) {
            if (mMainTabTouchBackground != null && mDepthStyle == DEPTH_TYPE_MAIN && tabBackgroundResId == 0) {
                mMainTabTouchBackground.setAlpha(1.0F);

                AnimationSet mainAnimation = new AnimationSet(true);
                mainAnimation.setFillAfter(true);

                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        AlphaAnimation fadeIn = new AlphaAnimation(0.0F, 1.0F);
                        fadeIn.setDuration(100);
                        fadeIn.setFillAfter(true);
                        mainAnimation.addAnimation(fadeIn);

                        ScaleAnimation scale = new ScaleAnimation(ANIM_RIPPLE_MINOR_SCALE, 1.0F, ANIM_RIPPLE_MINOR_SCALE, 1.0F, 1, 0.5F, 1, 0.5F);
                        scale.setDuration(ANIM_SHOW_DURATION);
                        scale.setInterpolator(SeslAnimationUtils.SINE_IN_OUT_80);
                        scale.setFillAfter(true);
                        mainAnimation.addAnimation(scale);

                        mMainTabTouchBackground.startAnimation(mainAnimation);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        if (mMainTabTouchBackground.getAnimation() != null) {
                            if (mMainTabTouchBackground.getAnimation().hasEnded()) {
                                AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.0f);
                                fadeOut.setDuration(ANIM_HIDE_DURATION);
                                fadeOut.setFillAfter(true);
                                mainAnimation.addAnimation(fadeOut);

                                mMainTabTouchBackground.startAnimation(mainAnimation);
                            } else {
                                mMainTabTouchBackground.getAnimation().setAnimationListener(new Animation.AnimationListener() {
                                    @Override
                                    public void onAnimationStart(Animation animation) {
                                    }

                                    @Override
                                    public void onAnimationRepeat(Animation animation) {
                                    }

                                    @Override
                                    public void onAnimationEnd(Animation animation) {
                                        AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.0f);
                                        fadeOut.setDuration(ANIM_HIDE_DURATION);
                                        fadeOut.setFillAfter(true);
                                        mMainTabTouchBackground.startAnimation(fadeOut);
                                    }
                                });
                            }
                        }
                        break;
                }
            }
        }
    }

    class SlidingTabIndicator extends LinearLayout {
        ValueAnimator indicatorAnimator;
        int selectedPosition = -1;
        float selectionOffset;
        private int layoutDirection = -1;

        SlidingTabIndicator(Context context) {
            super(context);
            setWillNotDraw(false);
        }

        void setSelectedIndicatorHeight(int height) {
            Rect bounds = tabSelectedIndicator.getBounds();
            tabSelectedIndicator.setBounds(bounds.left, 0, bounds.right, height);
            this.requestLayout();
        }

        boolean childrenNeedLayout() {
            for (int i = 0, z = getChildCount(); i < z; i++) {
                final View child = getChildAt(i);
                if (child.getWidth() <= 0) {
                    return true;
                }
            }
            return false;
        }

        void setIndicatorPositionFromTabPosition(int position, float positionOffset) {
            if (indicatorAnimator != null && indicatorAnimator.isRunning()) {
                indicatorAnimator.cancel();
            }

            selectedPosition = position;
            selectionOffset = positionOffset;

            final View selectedTitle = getChildAt(selectedPosition);
            final View nextTitle = getChildAt(selectedPosition + 1);

            tweenIndicatorPosition(selectedTitle, nextTitle, selectionOffset);
        }

        float getIndicatorPosition() {
            return selectedPosition + selectionOffset;
        }

        @Override
        public void onRtlPropertiesChanged(int layoutDirection) {
            super.onRtlPropertiesChanged(layoutDirection);

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                if (this.layoutDirection != layoutDirection) {
                    requestLayout();
                    this.layoutDirection = layoutDirection;
                }
            }
        }

        // kang
        @SuppressLint("RestrictedApi")
        @Override
        protected void onMeasure(int i, int i2) {
            int i3;
            super.onMeasure(i, i2);
            if (View.MeasureSpec.getMode(i) == MeasureSpec.EXACTLY) {
                int i4 = 0;
                boolean z = true;
                if (SamsungTabLayout.this.mode == 11 || SamsungTabLayout.this.mode == 12) {
                    SamsungTabLayout.this.checkOverScreen();
                    if (SamsungTabLayout.this.mIsOverScreen) {
                        i3 = SamsungTabLayout.this.mOverScreenMaxWidth;
                    } else {
                        i3 = View.MeasureSpec.getSize(i);
                    }
                    int childCount = getChildCount();
                    int[] iArr = new int[childCount];
                    int i5 = 0;
                    for (int i6 = 0; i6 < childCount; i6++) {
                        View childAt = getChildAt(i6);
                        if (childAt.getVisibility() == VISIBLE) {
                            childAt.measure(View.MeasureSpec.makeMeasureSpec(SamsungTabLayout.this.tabMaxWidth, MeasureSpec.UNSPECIFIED), i2);
                            iArr[i6] = childAt.getMeasuredWidth() + (SamsungTabLayout.this.mTabMinSideSpace * 2);
                            i5 += iArr[i6];
                        }
                    }
                    int i7 = i3 / childCount;
                    if (i5 > i3) {
                        while (i4 < childCount) {
                            ((LinearLayout.LayoutParams) getChildAt(i4).getLayoutParams()).width = iArr[i4];
                            i4++;
                        }
                    } else {
                        if (SamsungTabLayout.this.mode == 11) {
                            int i8 = 0;
                            while (true) {
                                if (i8 >= childCount) {
                                    z = false;
                                    break;
                                } else if (iArr[i8] > i7) {
                                    break;
                                } else {
                                    i8++;
                                }
                            }
                        }
                        if (z) {
                            int i9 = (i3 - i5) / childCount;
                            while (i4 < childCount) {
                                ((LinearLayout.LayoutParams) getChildAt(i4).getLayoutParams()).width = iArr[i4] + i9;
                                i4++;
                            }
                        } else {
                            while (i4 < childCount) {
                                ((LinearLayout.LayoutParams) getChildAt(i4).getLayoutParams()).width = i7;
                                i4++;
                            }
                        }
                    }
                    if (i5 > i3) {
                        i3 = i5;
                    }
                    super.onMeasure(View.MeasureSpec.makeMeasureSpec(i3, MeasureSpec.EXACTLY), i2);
                } else if (SamsungTabLayout.this.tabGravity == 1 || SamsungTabLayout.this.mode == 2 || SamsungTabLayout.this.mFirstTabGravity == 1) {
                    int childCount2 = getChildCount();
                    if (SamsungTabLayout.this.tabGravity == 0 && SamsungTabLayout.this.mFirstTabGravity == 1) {
                        for (int i10 = 0; i10 < childCount2; i10++) {
                            View childAt2 = getChildAt(i10);
                            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) childAt2.getLayoutParams();
                            layoutParams.width = -2;
                            layoutParams.weight = 0.0f;
                            childAt2.measure(View.MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), i2);
                        }
                    }
                    int i11 = 0;
                    for (int i12 = 0; i12 < childCount2; i12++) {
                        View childAt3 = getChildAt(i12);
                        if (childAt3.getVisibility() == VISIBLE) {
                            i11 = Math.max(i11, childAt3.getMeasuredWidth());
                        }
                    }
                    if (i11 > 0) {
                        if (i11 * childCount2 <= getMeasuredWidth() - (((int) ViewUtils.dpToPx(getContext(), 16)) * 2)) {
                            boolean z2 = false;
                            while (i4 < childCount2) {
                                LinearLayout.LayoutParams layoutParams2 = (LinearLayout.LayoutParams) getChildAt(i4).getLayoutParams();
                                if (layoutParams2.width != i11 || layoutParams2.weight != 0.0f) {
                                    layoutParams2.width = i11;
                                    layoutParams2.weight = 0.0f;
                                    z2 = true;
                                }
                                i4++;
                            }
                            if (SamsungTabLayout.this.tabGravity == 0 && SamsungTabLayout.this.mFirstTabGravity == 1) {
                                SamsungTabLayout.this.tabGravity = GRAVITY_CENTER;
                            }
                            z = z2;
                        } else {
                            SamsungTabLayout.this.tabGravity = 0;
                            SamsungTabLayout.this.updateTabViews(false);
                        }
                        if (z) {
                            super.onMeasure(i, i2);
                        }
                    }
                }
            }
        }
        // kang

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
            super.onLayout(changed, l, t, r, b);

            if (indicatorAnimator != null && indicatorAnimator.isRunning()) {
                updateOrRecreateIndicatorAnimation(false, selectedPosition, -1);
            } else {
                jumpIndicatorToSelectedPosition();
            }
        }

        private void jumpIndicatorToSelectedPosition() {
        }

        private void tweenIndicatorPosition(View startTitle, View endTitle, float fraction) {
            boolean hasVisibleTitle = startTitle != null && startTitle.getWidth() > 0;
            if (hasVisibleTitle) {
                tabIndicatorInterpolator.setIndicatorBoundsForOffset(SamsungTabLayout.this, startTitle, endTitle, fraction, tabSelectedIndicator);
            } else {
                tabSelectedIndicator.setBounds(-1, tabSelectedIndicator.getBounds().top, -1, tabSelectedIndicator.getBounds().bottom);
            }

            ViewCompat.postInvalidateOnAnimation(this);
        }

        void animateIndicatorToPosition(final int position, int duration) {
        }

        private void updateOrRecreateIndicatorAnimation(boolean recreateAnimation, final int position, int duration) {
        }

        @Override
        public void draw(@NonNull Canvas canvas) {
            super.draw(canvas);
        }
    }

    @NonNull
    private static ColorStateList createColorStateList(int defaultColor, int selectedColor) {
        final int[][] states = new int[2][];
        final int[] colors = new int[2];
        int i = 0;

        states[i] = SELECTED_STATE_SET;
        colors[i] = selectedColor;
        i++;

        states[i] = EMPTY_STATE_SET;
        colors[i] = defaultColor;
        i++;

        return new ColorStateList(states, colors);
    }

    @Dimension(unit = Dimension.DP)
    private int getDefaultHeight() {
        return mDepthStyle == DEPTH_TYPE_SUB ? SESL_SUB_DEPTH_DEFAULT_HEIGHT : SESL_DEFAULT_HEIGHT;
    }

    private int getTabMinWidth() {
        if (requestedTabMinWidth != INVALID_WIDTH) {
            return requestedTabMinWidth;
        }
        return 0;
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return generateDefaultLayoutParams();
    }

    int getTabMaxWidth() {
        return tabMaxWidth;
    }

    public static class TabLayoutOnPageChangeListener implements SeslViewPager.OnPageChangeListener {
        @NonNull private final WeakReference<SamsungTabLayout> tabLayoutRef;
        private int previousScrollState;
        private int scrollState;

        public TabLayoutOnPageChangeListener(SamsungTabLayout tabLayout) {
            tabLayoutRef = new WeakReference<>(tabLayout);
        }

        @Override
        public void onPageScrollStateChanged(final int state) {
            previousScrollState = scrollState;
            scrollState = state;
        }

        @Override
        public void onPageScrolled(final int position, final float positionOffset, final int positionOffsetPixels) {
            final SamsungTabLayout tabLayout = tabLayoutRef.get();
            if (tabLayout != null) {
                final boolean updateText = scrollState != SCROLL_STATE_SETTLING || previousScrollState == SCROLL_STATE_DRAGGING;
                final boolean updateIndicator = !(scrollState == SCROLL_STATE_SETTLING && previousScrollState == SCROLL_STATE_IDLE);
                tabLayout.setScrollPosition(position, positionOffset, updateText, updateIndicator);
            }
        }

        @Override
        public void onPageSelected(final int position) {
            final SamsungTabLayout tabLayout = tabLayoutRef.get();
            if (tabLayout != null && tabLayout.getSelectedTabPosition() != position && position < tabLayout.getTabCount()) {
                final boolean updateIndicator = scrollState == SCROLL_STATE_IDLE || (scrollState == SCROLL_STATE_SETTLING && previousScrollState == SCROLL_STATE_IDLE);
                tabLayout.selectTab(tabLayout.getTabAt(position), updateIndicator);
            }
        }

        void reset() {
            previousScrollState = scrollState = SCROLL_STATE_IDLE;
        }
    }

    public static class ViewPagerOnTabSelectedListener implements SamsungTabLayout.OnTabSelectedListener {
        private final SeslViewPager viewPager;

        public ViewPagerOnTabSelectedListener(SeslViewPager viewPager) {
            this.viewPager = viewPager;
        }

        @Override
        public void onTabSelected(@NonNull SamsungTabLayout.Tab tab) {
            viewPager.setCurrentItem(tab.getPosition());
        }

        @Override
        public void onTabUnselected(SamsungTabLayout.Tab tab) {
        }

        @Override
        public void onTabReselected(SamsungTabLayout.Tab tab) {
        }
    }

    private class PagerAdapterObserver extends DataSetObserver {
        PagerAdapterObserver() {}

        @Override
        public void onChanged() {
            populateFromPagerAdapter();
        }

        @Override
        public void onInvalidated() {
            populateFromPagerAdapter();
        }
    }

    private class AdapterChangeListener implements SeslViewPager.OnAdapterChangeListener {
        private boolean autoRefresh;

        AdapterChangeListener() {}

        @Override
        public void onAdapterChanged(@NonNull SeslViewPager viewPager, @Nullable PagerAdapter oldAdapter, @Nullable PagerAdapter newAdapter) {
            if (SamsungTabLayout.this.viewPager == viewPager) {
                setPagerAdapter(newAdapter, autoRefresh);
            }
        }

        void setAutoRefresh(boolean autoRefresh) {
            this.autoRefresh = autoRefresh;
        }
    }

    public void seslSetSubTabStyle() {
        if (mDepthStyle == DEPTH_TYPE_MAIN) {
            mDepthStyle = DEPTH_TYPE_SUB;

            tabTextColors = getResources().getColorStateList(R.color.sesl_tablayout_subtab_text_color);

            if (tabs.size() > 0) {
                int selectedTab = getSelectedTabPosition();
                ArrayList tabs = new ArrayList(this.tabs.size());

                for (int i = 0; i < this.tabs.size(); i++) {
                    Tab tab = newTab();
                    tab.text = this.tabs.get(i).text;
                    tab.icon = this.tabs.get(i).icon;
                    tab.customView = this.tabs.get(i).customView;
                    tab.subText = this.tabs.get(i).subText;
                    if (i == selectedTab) {
                        tab.select();
                    }
                    tab.view.update();
                    tabs.add(tab);
                }

                removeAllTabs();

                for (int i = 0; i < this.tabs.size(); i++) {
                    addTab((Tab) tabs.get(i), i == selectedTab);
                    if (this.tabs.get(i) != null) {
                        this.tabs.get(i).view.update();
                    }
                }

                tabs.clear();
            }
        }
    }

    // kang
    private void updateBadgePosition() {
        ArrayList var1 = this.tabs;
        if (var1 != null && var1.size() != 0) {
            for(int var2 = 0; var2 < this.tabs.size(); ++var2) {
                SamsungTabLayout.Tab var11 = (SamsungTabLayout.Tab)this.tabs.get(var2);
                SamsungTabLayout.TabView var3 = ((SamsungTabLayout.Tab)this.tabs.get(var2)).view;
                if (var11 != null && var3 != null) {
                    Object var4 = var3.textView;
                    ImageView var5 = var3.iconView;
                    if (var3.getWidth() > 0) {
                        TextView var12 = null;
                        byte var6 = -1;
                        int var7;
                        int var8;
                        if (var3.mNBadgeView != null && var3.mNBadgeView.getVisibility() == VISIBLE) {
                            var12 = var3.mNBadgeView;
                            var7 = ((android.widget.RelativeLayout.LayoutParams)var12.getLayoutParams()).getMarginStart();
                            var8 = this.getContext().getResources().getDimensionPixelSize(R.dimen.sesl_tablayout_subtab_n_badge_xoffset);
                            var6 = 1;
                        } else if (var3.mDotBadgeView != null && var3.mDotBadgeView.getVisibility() == VISIBLE) {
                            var12 = var3.mDotBadgeView;
                            var7 = ((android.widget.RelativeLayout.LayoutParams)var12.getLayoutParams()).getMarginStart();
                            var8 = this.getContext().getResources().getDimensionPixelSize(R.dimen.sesl_tablayout_subtab_dot_badge_offset_x);
                            var6 = 2;
                        } else {
                            var8 = this.getContext().getResources().getDimensionPixelSize(R.dimen.sesl_tablayout_subtab_n_badge_xoffset);
                            var7 = 0;
                        }

                        if (var12 != null && var12.getVisibility() == VISIBLE) {
                            var12.measure(0, 0);
                            int var9;
                            if (var6 == 1) {
                                var9 = var12.getMeasuredWidth();
                            } else {
                                var9 = this.getResources().getDimensionPixelSize(R.dimen.sesl_tab_badge_dot_size);
                            }

                            if (var4 == null || ((TextView)var4).getWidth() <= 0) {
                                var4 = var5;
                            }

                            if (var4 == null) {
                                return;
                            }

                            int var10;
                            int var14;
                            label70: {
                                var10 = var3.getWidth();
                                if (var7 != 0) {
                                    var14 = var7;
                                    if (var7 >= ((View)var4).getRight()) {
                                        break label70;
                                    }
                                }

                                var14 = ((View)var4).getRight() + var8;
                            }

                            if (var14 > var10) {
                                var7 = var10 - var9;
                            } else {
                                var7 = var14 + var9;
                                if (var7 > var10) {
                                    var7 = var14 - (var7 - var10);
                                } else {
                                    var7 = var14;
                                    if (var14 > ((View)var4).getRight() + var8) {
                                        var7 = ((View)var4).getRight() + var8;
                                    }
                                }
                            }

                            var7 = Math.max(0, var7);
                            android.widget.RelativeLayout.LayoutParams var13 = (android.widget.RelativeLayout.LayoutParams)var12.getLayoutParams();
                            var8 = var13.width;
                            if (var13.getMarginStart() != var7 || var8 != var9) {
                                var13.setMarginStart(var7);
                                var13.width = var9;
                                var12.setLayoutParams(var13);
                            }
                        }
                    }
                }
            }
        }
    }
    // kang

    public void seslSetSubTabSelectedIndicatorColor(@ColorInt int color) {
        mSubTabSelectedIndicatorColor = color;
        setSelectedTabIndicatorColor(color);
    }

    @Deprecated
    public void seslSetTabTextColor(ColorStateList color, boolean refresh) {
        if (tabTextColors != color) {
            tabTextColors = color;
            if (refresh) {
                updateAllTabs();
            } else if (tabs != null) {
                for (int i = 0; i < tabs.size(); i++) {
                    TabView tabView = tabs.get(i).view;
                    if (tabView != null && tabView.textView != null) {
                        tabView.textView.setTextColor(tabTextColors);
                    }
                }
            }
        }
    }

    public void seslSetBadgeColor(@ColorInt int color) {
        mBadgeColor = color;
    }

    public void seslSetBadgeTextColor(@ColorInt int color) {
        mBadgeTextColor = color;
    }

    public void seslSetTabWidth(int width) {
        mRequestedTabWidth = width;
    }

    private int getSelectedTabTextColor() {
        if (tabTextColors != null) {
            return tabTextColors.getColorForState(new int[]{android.R.attr.state_selected, android.R.attr.state_enabled}, tabTextColors.getDefaultColor());
        }
        return Color.WHITE;
    }

    private void startTextColorChangeAnimation(TextView textView, int color) {
        if (textView != null) {
            textView.setTextColor(color);
        }
    }

    public void checkMaxFontScale(TextView textView, int size) {
        float fontScale = getResources().getConfiguration().fontScale;
        if (textView != null && mIsScaledTextSizeType && fontScale > 1.3f) {
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, (((float) size) / fontScale) * 1.3f);
        }
    }

    // kang
    private void createAddBadge(int type, TabView tabView) {
        if (tabView != null && tabView.mTabParentView != null) {
            TextView textView = new TextView(getContext());
            Resources resources = getResources();
            int i2 = -1;
            if (type == BADGE_TYPE_DOT) {
                if (tabView.mDotBadgeView == null) {
                    textView.setVisibility(GONE);
                    ViewCompat.setBackground(textView, resources.getDrawable(R.drawable.sesl_dot_badge));
                    textView.setId(R.id.sesl_badge_dot);
                    int dimensionPixelSize = resources.getDimensionPixelSize(R.dimen.sesl_tab_badge_dot_size);
                    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(dimensionPixelSize, dimensionPixelSize);
                    if (tabView.textView != null) {
                        i2 = tabView.textView.getWidth();
                    }
                    if (i2 > 0 || tabView.iconView == null || tabView.iconView.getVisibility() != VISIBLE) {
                        layoutParams.addRule(6, R.id.title);
                    } else {
                        layoutParams.addRule(6, R.id.icon);
                    }
                    textView.setMinHeight(dimensionPixelSize);
                    textView.setMinWidth(dimensionPixelSize);
                    tabView.mTabParentView.addView(textView, layoutParams);
                    tabView.mDotBadgeView = textView;
                }
            } else if (tabView.mNBadgeView == null) {
                textView.setVisibility(GONE);
                textView.setMinWidth(resources.getDimensionPixelSize(R.dimen.sesl_tab_badge_number_min_width));
                textView.setTextSize(1, BADGE_N_TEXT_SIZE);
                textView.setGravity(17);
                textView.setTextColor(resources.getColor(R.color.sesl_badge_text_color));
                ViewCompat.setBackground(textView, resources.getDrawable(R.drawable.sesl_tab_n_badge));
                textView.setId(R.id.sesl_badge_n);
                textView.setMaxLines(1);
                RelativeLayout.LayoutParams layoutParams2 = new RelativeLayout.LayoutParams(-2, resources.getDimensionPixelSize(R.dimen.sesl_tab_badge_number_height));
                if (tabView.textView != null) {
                    i2 = tabView.textView.getWidth();
                }
                if (i2 > 0 || tabView.iconView == null || tabView.iconView.getVisibility() != VISIBLE) {
                    layoutParams2.addRule(6, R.id.title);
                } else {
                    layoutParams2.addRule(6, R.id.icon);
                }
                layoutParams2.setMargins(0, -resources.getDimensionPixelSize(R.dimen.sesl_tab_badge_offset_y), 0, 0);
                tabView.mTabParentView.addView(textView, layoutParams2);
                tabView.mNBadgeView = textView;
            }
        }
    }
    // kang

    public void seslShowDotBadge(int index, boolean show) {
        if (tabs.get(index) != null && tabs.get(index).view != null) {
            TabView tabView = tabs.get(index).view;

            if (tabView.mDotBadgeView == null) {
                createAddBadge(BADGE_TYPE_DOT, tabView);
            }

            if (tabView.mDotBadgeView != null) {
                if (show) {
                    tabView.mDotBadgeView.setVisibility(VISIBLE);
                    if (mBadgeColor != -1) {
                        DrawableCompat.setTint(tabView.mDotBadgeView.getBackground(), mBadgeColor);
                    }
                    updateBadgePosition();
                } else {
                    tabView.mDotBadgeView.setVisibility(GONE);
                }
            }
        }
    }

    public void seslShowBadge(int index, boolean show, int badge) {
        if (badge != 0) {
            seslShowBadge(index, show, badge == -1 ? "N" : (badge > 99 ? "99" : String.valueOf(badge)));
        }
    }

    public void seslShowBadge(int index, boolean show, String text) {
        seslShowBadge(index, show, text, null);
    }

    public void seslShowBadge(int index, boolean show, String text, String contentDescription) {
        if (mDepthStyle != DEPTH_TYPE_SUB && tabs.get(index) != null && tabs.get(index).view != null) {
            TabView tabView = tabs.get(index).view;

            if (tabView.mNBadgeView == null) {
                createAddBadge(BADGE_TYPE_N, tabView);
            }

            if (tabView.mNBadgeView != null) {
                tabView.mNBadgeView.setText(text);
                if (show) {
                    tabView.mNBadgeView.setVisibility(VISIBLE);

                    if (mBadgeColor != -1) {
                        DrawableCompat.setTint(tabView.mNBadgeView.getBackground(), mBadgeColor);
                    }
                    if (mBadgeTextColor != -1) {
                        tabView.mNBadgeView.setTextColor(mBadgeTextColor);
                    }
                    if (contentDescription != null) {
                        tabView.mNBadgeView.setContentDescription(contentDescription);
                    }

                    updateBadgePosition();
                    tabView.mNBadgeView.requestLayout();
                } else {
                    tabView.mNBadgeView.setVisibility(GONE);
                }
            }
        } else {
            Log.w(LOG_TAG, "seslShowBadge not supported with DEPTH_TYPE_SUB");
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        updateBadgePosition();
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);

        for (int i = 0; i < getTabCount(); i++) {
            Tab tab = getTabAt(i);
            if (tab != null && tab.view != null && tab.view.mMainTabTouchBackground != null) {
                tab.view.mMainTabTouchBackground.setAlpha(0.0f);
            }
        }
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        for (int i = 0; i < getTabCount(); i++) {
            Tab tab = getTabAt(i);
            if (tab != null && tab.view != null && tab.view.mMainTabTouchBackground != null) {
                tab.view.mMainTabTouchBackground.setAlpha(0.0f);
            }
        }

        updateBadgePosition();
    }

    public void seslSetSubTabIndicatorHeight(int height) {
        mSubTabIndicatorHeight = height;
    }

    public void seslSetIconTextGap(int gap) {
        mIconTextGap = gap;
        updateAllTabs();
    }

    public void seslSetTabSubTextColors(ColorStateList color) {
        if (mSubTabSubTextColors != color) {
            mSubTabSubTextColors = color;
            updateAllTabs();
        }
    }

    public ColorStateList seslGetTabSubTextColors() {
        return mSubTabSubTextColors;
    }

    public void seslSetTabSubTextColors(int defaultColor, int selectedColor) {
        seslSetTabSubTextColors(createColorStateList(defaultColor, selectedColor));
    }

    private int seslGetSelectedTabSubTextColor() {
        ColorStateList colorStateList = this.mSubTabSubTextColors;
        if (colorStateList != null) {
            return colorStateList.getColorForState(new int[]{16842913, 16842910}, colorStateList.getDefaultColor());
        }
        return -1;
    }

    // kang
    private void checkOverScreen() {
        int measuredWidth = getMeasuredWidth();
        if (measuredWidth > ((int) (((float) getResources().getInteger(R.integer.sesl_tablayout_over_screen_width_dp)) * (((float) getContext().getResources().getDisplayMetrics().densityDpi) / 160.0f)))) {
            mIsOverScreen = true;
            mOverScreenMaxWidth = (int) (ResourcesCompat.getFloat(getResources(), R.dimen.sesl_tablayout_over_screen_max_width_rate) * ((float) measuredWidth));
        } else  {
            mIsOverScreen = false;
        }
    }
    // kang
}
