package de.dlyt.yanndroid.oneui.sesl.tabs;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.text.Layout;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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

import androidx.appcompat.animation.SeslAnimationUtils;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.TooltipCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.util.Pools;
import androidx.core.view.AccessibilityDelegateCompat;
import androidx.core.view.MarginLayoutParamsCompat;
import androidx.core.view.PointerIconCompat;
import androidx.core.view.ViewCompat;
import androidx.core.widget.TextViewCompat;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.SeslViewPager;

import com.google.android.material.animation.AnimationUtils;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.BadgeUtils;
import com.google.android.material.internal.ViewUtils;
import com.google.android.material.resources.MaterialResources;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.MaterialShapeUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;

import de.dlyt.yanndroid.oneui.R;
import de.dlyt.yanndroid.oneui.view.TabItem;

@SeslViewPager.DecorView
public class SamsungBaseTabLayout extends HorizontalScrollView {
    public static final Pools.Pool<Tab> tabPool = new Pools.SynchronizedPool(16);
    public final int requestedTabMaxWidth;
    public final int requestedTabMinWidth;
    public final int scrollableTabMinWidth;
    public final ArrayList<BaseOnTabSelectedListener> selectedListeners;
    public final SlidingTabIndicator slidingTabIndicator;
    public final int tabBackgroundResId;
    public final RectF tabViewContentBounds;
    public final Pools.Pool<TabView> tabViewPool;
    public final ArrayList<Tab> tabs;
    public AdapterChangeListener adapterChangeListener;
    public int contentInsetStart;
    public BaseOnTabSelectedListener currentVpSelectedListener;
    public boolean inlineLabel;
    public int mBadgeColor;
    public int mBadgeTextColor;
    public Typeface mBoldTypeface;
    public int mDepthStyle;
    public int mIconTextGap;
    public boolean mIsScaledTextSizeType;
    public Typeface mNormalTypeface;
    public int mRequestedTabWidth;
    public int mSubTabIndicatorHeight;
    public int mSubTabSelectedIndicatorColor;
    public int mTabSelectedIndicatorColor;
    public int mode;
    public TabLayoutOnPageChangeListener pageChangeListener;
    public PagerAdapter pagerAdapter;
    public DataSetObserver pagerAdapterObserver;
    public ValueAnimator scrollAnimator;
    public BaseOnTabSelectedListener selectedListener;
    public Tab selectedTab;
    public boolean setupViewPagerImplicitly;
    public int tabGravity;
    public ColorStateList tabIconTint;
    public PorterDuff.Mode tabIconTintMode;
    public int tabIndicatorAnimationDuration;
    public boolean tabIndicatorFullWidth;
    public int tabIndicatorGravity;
    public int tabMaxWidth;
    public int tabPaddingBottom;
    public int tabPaddingEnd;
    public int tabPaddingStart;
    public int tabPaddingTop;
    public ColorStateList tabRippleColorStateList;
    public Drawable tabSelectedIndicator;
    public int tabTextAppearance;
    public ColorStateList tabTextColors;
    public float tabTextMultiLineSize;
    public float tabTextSize;
    public boolean unboundedRipple;
    public SeslViewPager viewPager;

    public SamsungBaseTabLayout(Context var1) {
        this(var1, (AttributeSet) null);
    }

    public SamsungBaseTabLayout(Context var1, AttributeSet var2) {
        this(var1, var2, R.attr.tabStyle);
    }

    @SuppressLint({"RestrictedApi", "WrongConstant"})
    public SamsungBaseTabLayout(Context var1, AttributeSet var2, int var3) {
        super(var1, var2, var3);
        this.tabs = new ArrayList();
        this.tabViewContentBounds = new RectF();
        this.tabMaxWidth = 2147483647;
        this.selectedListeners = new ArrayList();
        this.tabViewPool = new Pools.SimplePool(12);
        this.mIconTextGap = -1;
        this.mDepthStyle = 1;
        this.mBadgeColor = -1;
        this.mBadgeTextColor = -1;
        this.mRequestedTabWidth = -1;
        this.mSubTabSelectedIndicatorColor = -1;
        this.mSubTabIndicatorHeight = 1;
        this.mIsScaledTextSizeType = false;
        this.setHorizontalScrollBarEnabled(false);
        this.slidingTabIndicator = new SlidingTabIndicator(var1);
        super.addView(this.slidingTabIndicator, 0, new LayoutParams(-2, -1));
        int[] var4 = R.styleable.SamsungTabLayout;
        int var5 = R.style.BaseTabLayoutStyle;

        TypedArray var9 = var1.obtainStyledAttributes(var2, var4, var3, var5);
        if (this.getBackground() instanceof ColorDrawable) {
            ColorDrawable var6 = (ColorDrawable) this.getBackground();
            MaterialShapeDrawable var10 = new MaterialShapeDrawable();
            var10.setFillColor(ColorStateList.valueOf(var6.getColor()));
            var10.initializeElevationOverlay(var1);
            var10.setElevation(ViewCompat.getElevation(this));
            ViewCompat.setBackground(this, var10);
        }

        this.slidingTabIndicator.setSelectedIndicatorHeight(var9.getDimensionPixelSize(R.styleable.SamsungTabLayout_tabIndicatorHeight, -1));
        this.mTabSelectedIndicatorColor = var9.getColor(R.styleable.SamsungTabLayout_tabIndicatorColor, 0);
        this.slidingTabIndicator.setSelectedIndicatorColor(this.mTabSelectedIndicatorColor);
        this.slidingTabIndicator.setSelectedIndicatorColor(this.mTabSelectedIndicatorColor);
        this.setSelectedTabIndicator(MaterialResources.getDrawable(var1, var9, R.styleable.SamsungTabLayout_tabIndicator));
        this.setSelectedTabIndicatorGravity(var9.getInt(R.styleable.SamsungTabLayout_tabIndicatorGravity, 0));
        this.setTabIndicatorFullWidth(var9.getBoolean(R.styleable.SamsungTabLayout_tabIndicatorFullWidth, true));
        var3 = var9.getDimensionPixelSize(R.styleable.SamsungTabLayout_tabPadding, 0);
        this.tabPaddingBottom = var3;
        this.tabPaddingEnd = var3;
        this.tabPaddingTop = var3;
        this.tabPaddingStart = var3;
        this.tabPaddingStart = var9.getDimensionPixelSize(R.styleable.SamsungTabLayout_tabPaddingStart, this.tabPaddingStart);
        this.tabPaddingTop = var9.getDimensionPixelSize(R.styleable.SamsungTabLayout_tabPaddingTop, this.tabPaddingTop);
        this.tabPaddingEnd = var9.getDimensionPixelSize(R.styleable.SamsungTabLayout_tabPaddingEnd, this.tabPaddingEnd);
        this.tabPaddingBottom = var9.getDimensionPixelSize(R.styleable.SamsungTabLayout_tabPaddingBottom, this.tabPaddingBottom);
        this.tabTextAppearance = var9.getResourceId(R.styleable.SamsungTabLayout_tabTextAppearance, R.style.TabLayoutTextStyle);
        TypedArray var11 = var1.obtainStyledAttributes(this.tabTextAppearance, androidx.appcompat.R.styleable.TextAppearance);

        try {
            this.tabTextSize = (float) var11.getDimensionPixelSize(androidx.appcompat.R.styleable.TextAppearance_android_textSize, 0);
            this.mIsScaledTextSizeType = var11.getText(androidx.appcompat.R.styleable.TextAppearance_android_textSize).toString().contains("sp");
            this.tabTextColors = MaterialResources.getColorStateList(var1, var11, androidx.appcompat.R.styleable.TextAppearance_android_textColor);
        } finally {
            var11.recycle();
        }

        Resources var12 = this.getResources();
        String var13 = var12.getString(R.string.sesl_font_family_regular);
        this.mBoldTypeface = Typeface.create(var13, 1);
        this.mNormalTypeface = Typeface.create(var13, 0);
        this.mSubTabIndicatorHeight = var12.getDimensionPixelSize(R.dimen.sesl_tablayout_subtab_indicator_height);
        if (var9.hasValue(R.styleable.SamsungTabLayout_tabTextColor)) {
            this.tabTextColors = MaterialResources.getColorStateList(var1, var9, R.styleable.SamsungTabLayout_tabTextColor);
        }

        if (var9.hasValue(R.styleable.SamsungTabLayout_tabSelectedTextColor)) {
            var3 = var9.getColor(R.styleable.SamsungTabLayout_tabSelectedTextColor, 0);
            this.tabTextColors = createColorStateList(this.tabTextColors.getDefaultColor(), var3);
        }

        this.tabIconTint = MaterialResources.getColorStateList(var1, var9, R.styleable.SamsungTabLayout_tabIconTint);
        this.tabIconTintMode = ViewUtils.parseTintMode(var9.getInt(R.styleable.SamsungTabLayout_tabIconTintMode, -1), (PorterDuff.Mode) null);
        this.tabRippleColorStateList = MaterialResources.getColorStateList(var1, var9, R.styleable.SamsungTabLayout_tabRippleColor);
        this.tabIndicatorAnimationDuration = var9.getInt(R.styleable.SamsungTabLayout_tabIndicatorAnimationDuration, 300);
        this.requestedTabMinWidth = var9.getDimensionPixelSize(R.styleable.SamsungTabLayout_tabMinWidth, -1);
        this.requestedTabMaxWidth = var9.getDimensionPixelSize(R.styleable.SamsungTabLayout_tabMaxWidth, -1);
        this.tabBackgroundResId = var9.getResourceId(R.styleable.SamsungTabLayout_tabBackground, 0);
        this.contentInsetStart = var9.getDimensionPixelSize(R.styleable.SamsungTabLayout_tabContentStart, 0);
        this.mode = var9.getInt(R.styleable.SamsungTabLayout_tabMode, 1);
        this.tabGravity = var9.getInt(R.styleable.SamsungTabLayout_tabGravity, 0);
        this.inlineLabel = var9.getBoolean(R.styleable.SamsungTabLayout_tabInlineLabel, false);
        this.unboundedRipple = var9.getBoolean(R.styleable.SamsungTabLayout_tabUnboundedRipple, false);
        var9.recycle();
        this.tabTextMultiLineSize = (float) var12.getDimensionPixelSize(R.dimen.sesl_tab_text_size_2line);
        this.scrollableTabMinWidth = var12.getDimensionPixelSize(R.dimen.sesl_tab_scrollable_min_width);
        this.applyModeAndGravity();
    }

    public static ColorStateList createColorStateList(int var0, int var1) {
        return new ColorStateList(new int[][]{HorizontalScrollView.SELECTED_STATE_SET, HorizontalScrollView.EMPTY_STATE_SET}, new int[]{var1, var0});
    }

    private int getDefaultHeight() {
        int var1 = this.tabs.size();
        boolean var2 = false;
        int var3 = 0;

        boolean var4;
        while (true) {
            var4 = var2;
            if (var3 >= var1) {
                break;
            }

            Tab var5 = (Tab) this.tabs.get(var3);
            if (var5 != null && var5.getIcon() != null && !TextUtils.isEmpty(var5.getText())) {
                var4 = true;
                break;
            }

            ++var3;
        }

        byte var6;
        if (var4 && !this.inlineLabel) {
            var6 = 72;
        } else {
            var6 = 48;
        }

        return var6;
    }

    private int getSelectedTabTextColor() {
        ColorStateList var1 = this.tabTextColors;
        if (var1 != null) {
            int var2 = var1.getDefaultColor();
            return var1.getColorForState(new int[]{16842913, 16842910}, var2);
        } else {
            return -1;
        }
    }

    private int getTabMinWidth() {
        int var1 = this.requestedTabMinWidth;
        return var1 != -1 ? var1 : 0;
    }

    private int getTabScrollRange() {
        return Math.max(0, this.slidingTabIndicator.getWidth() - this.getWidth() - this.getPaddingLeft() - this.getPaddingRight());
    }

    public void addOnTabSelectedListener(OnTabSelectedListener var1) {
        addOnTabSelectedListener((BaseOnTabSelectedListener) var1);
    }

    @Deprecated
    public void addOnTabSelectedListener(BaseOnTabSelectedListener var1) {
        if (!this.selectedListeners.contains(var1)) {
            this.selectedListeners.add(var1);
        }
    }

    public void addTab(Tab var1) {
        this.addTab(var1, this.tabs.isEmpty());
    }

    public void addTab(Tab var1, int var2, boolean var3) {
        if (var1.parent == this) {
            this.configureTab(var1, var2);
            this.addTabView(var1);
            if (var3) {
                var1.select();
            }

        } else {
            throw new IllegalArgumentException("Tab belongs to a different TabLayout.");
        }
    }

    public void addTab(Tab var1, boolean var2) {
        this.addTab(var1, this.tabs.size(), var2);
    }

    public final void addTabFromItemView(TabItem var1) {
        Tab var2 = this.newTab();
        CharSequence var3 = var1.text;
        if (var3 != null) {
            var2.setText(var3);
        }

        Drawable var5 = var1.icon;
        if (var5 != null) {
            var2.setIcon(var5);
        }

        int var4 = var1.customLayout;
        if (var4 != 0) {
            var2.setCustomView(var4);
        }

        if (!TextUtils.isEmpty(var1.getContentDescription())) {
            var2.setContentDescription(var1.getContentDescription());
        }

        this.addTab(var2);
    }

    public final void addTabView(Tab var1) {
        TabView var2 = var1.view;
        var2.setSelected(false);
        var2.setActivated(false);
        this.slidingTabIndicator.addView(var2, var1.getPosition(), this.createLayoutParamsForTabs());
    }

    public void addView(View var1) {
        this.addViewInternal(var1);
    }

    public void addView(View var1, int var2) {
        this.addViewInternal(var1);
    }

    public void addView(View var1, int var2, ViewGroup.LayoutParams var3) {
        this.addViewInternal(var1);
    }

    public void addView(View var1, ViewGroup.LayoutParams var2) {
        this.addViewInternal(var1);
    }

    public final void addViewInternal(View var1) {
        if (var1 instanceof TabItem) {
            this.addTabFromItemView((TabItem) var1);
        } else {
            throw new IllegalArgumentException("Only TabItem instances can be added to TabLayout");
        }
    }

    public final void animateToTab(int var1) {
        if (var1 != -1) {
            if (this.getWindowToken() != null && ViewCompat.isLaidOut(this) && !this.slidingTabIndicator.childrenNeedLayout()) {
                int var2 = this.getScrollX();
                int var3 = this.calculateScrollXForTab(var1, 0.0F);
                if (var2 != var3) {
                    this.ensureScrollAnimator();
                    this.scrollAnimator.setIntValues(new int[]{var2, var3});
                    this.scrollAnimator.start();
                }

                this.slidingTabIndicator.animateIndicatorToPosition(var1, this.tabIndicatorAnimationDuration);
            } else {
                this.setScrollPosition(var1, 0.0F, true);
            }
        }
    }

    public final void applyModeAndGravity() {
        ViewCompat.setPaddingRelative(this.slidingTabIndicator, 0, 0, 0, 0);
        int var1 = this.mode;
        if (var1 != 0) {
            if (var1 == 1 || var1 == 2) {
                this.slidingTabIndicator.setGravity(1);
            }
        } else {
            this.slidingTabIndicator.setGravity(8388611);
        }

        this.updateTabViews(true);
    }

    @SuppressLint("WrongConstant")
    public final int calculateScrollXForTab(int var1, float var2) {
        int var3 = this.mode;
        int var4 = 0;
        if (var3 != 0 && var3 != 2) {
            return 0;
        } else {
            View var5 = this.slidingTabIndicator.getChildAt(var1);
            ++var1;
            View var6;
            if (var1 < this.slidingTabIndicator.getChildCount()) {
                var6 = this.slidingTabIndicator.getChildAt(var1);
            } else {
                var6 = null;
            }

            if (var5 != null) {
                var1 = var5.getWidth();
            } else {
                var1 = 0;
            }

            if (var6 != null) {
                var4 = var6.getWidth();
            }

            var3 = var5.getLeft() + var1 / 2 - this.getWidth() / 2;
            var1 = (int) ((float) (var1 + var4) * 0.5F * var2);
            if (ViewCompat.getLayoutDirection(this) == 0) {
                var1 += var3;
            } else {
                var1 = var3 - var1;
            }

            return var1;
        }
    }

    public final void checkMaxFontScale(TextView var1, int var2) {
        float var3 = this.getResources().getConfiguration().fontScale;
        if (var1 != null && this.mIsScaledTextSizeType && var3 > 1.3F) {
            var1.setTextSize(0, (float) var2 / var3 * 1.3F);
        }

    }

    public final void configureTab(Tab var1, int var2) {
        var1.setPosition(var2);
        this.tabs.add(var2, var1);
        int var3 = this.tabs.size();

        while (true) {
            ++var2;
            if (var2 >= var3) {
                return;
            }

            ((Tab) this.tabs.get(var2)).setPosition(var2);
        }
    }

    public final LinearLayout.LayoutParams createLayoutParamsForTabs() {
        LinearLayout.LayoutParams var1 = new LinearLayout.LayoutParams(-2, -1);
        this.updateTabViewLayoutParams(var1);
        return var1;
    }

    public Tab createTabFromPool() {
        Tab var1 = (Tab) tabPool.acquire();
        Tab var2 = var1;
        if (var1 == null) {
            var2 = new Tab();
        }

        return var2;
    }

    public final TabView createTabView(Tab var1) {
        Pools.Pool var2 = this.tabViewPool;
        TabView var4;
        if (var2 != null) {
            var4 = (TabView) var2.acquire();
        } else {
            var4 = null;
        }

        TabView var3 = var4;
        if (var4 == null) {
            var3 = new TabView(this.getContext());
        }

        if (var3.mMainTabTouchBackground != null) {
            var3.mMainTabTouchBackground.setAlpha(0.0F);
        }

        var3.setTab(var1);
        var3.setFocusable(true);
        var3.setMinimumWidth(this.getTabMinWidth());
        if (TextUtils.isEmpty(var1.contentDesc)) {
            var3.setContentDescription(var1.text);
        } else {
            var3.setContentDescription(var1.contentDesc);
        }

        return var3;
    }

    public final void dispatchTabReselected(Tab var1) {
        for (int var2 = this.selectedListeners.size() - 1; var2 >= 0; --var2) {
            ((BaseOnTabSelectedListener) this.selectedListeners.get(var2)).onTabReselected(var1);
        }

    }

    public final void dispatchTabSelected(Tab var1) {
        for (int var2 = this.selectedListeners.size() - 1; var2 >= 0; --var2) {
            ((BaseOnTabSelectedListener) this.selectedListeners.get(var2)).onTabSelected(var1);
        }

    }

    public final void dispatchTabUnselected(Tab var1) {
        for (int var2 = this.selectedListeners.size() - 1; var2 >= 0; --var2) {
            ((BaseOnTabSelectedListener) this.selectedListeners.get(var2)).onTabUnselected(var1);
        }

    }

    public final void ensureScrollAnimator() {
        if (this.scrollAnimator == null) {
            this.scrollAnimator = new ValueAnimator();
            this.scrollAnimator.setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR);
            this.scrollAnimator.setDuration((long) this.tabIndicatorAnimationDuration);
            this.scrollAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator var1) {
                    SamsungBaseTabLayout.this.scrollTo((Integer) var1.getAnimatedValue(), 0);
                }
            });
        }

    }

    public LayoutParams generateLayoutParams(AttributeSet var1) {
        return this.generateDefaultLayoutParams();
    }

    public int getSelectedTabPosition() {
        Tab var1 = this.selectedTab;
        int var2;
        if (var1 != null) {
            var2 = var1.getPosition();
        } else {
            var2 = -1;
        }

        return var2;
    }

    public Tab getTabAt(int var1) {
        Tab var2;
        if (var1 >= 0 && var1 < this.getTabCount()) {
            var2 = (Tab) this.tabs.get(var1);
        } else {
            var2 = null;
        }

        return var2;
    }

    public int getTabCount() {
        return this.tabs.size();
    }

    public int getTabGravity() {
        return this.tabGravity;
    }

    public void setTabGravity(int var1) {
        if (this.tabGravity != var1) {
            this.tabGravity = var1;
            this.applyModeAndGravity();
        }

    }

    public ColorStateList getTabIconTint() {
        return this.tabIconTint;
    }

    public void setTabIconTint(ColorStateList var1) {
        if (this.tabIconTint != var1) {
            this.tabIconTint = var1;
            this.updateAllTabs();
        }

    }

    public int getTabIndicatorGravity() {
        return this.tabIndicatorGravity;
    }

    public int getTabMaxWidth() {
        return this.tabMaxWidth;
    }

    public int getTabMode() {
        return this.mode;
    }

    public void setTabMode(int var1) {
        if (var1 != this.mode) {
            this.mode = var1;
            this.applyModeAndGravity();
        }

    }

    public ColorStateList getTabRippleColor() {
        return this.tabRippleColorStateList;
    }

    public void setTabRippleColor(ColorStateList var1) {
        if (this.tabRippleColorStateList != var1) {
            this.tabRippleColorStateList = var1;

            for (int var2 = 0; var2 < this.slidingTabIndicator.getChildCount(); ++var2) {
                View var3 = this.slidingTabIndicator.getChildAt(var2);
                if (var3 instanceof TabView) {
                    ((TabView) var3).updateBackgroundDrawable(this.getContext());
                }
            }
        }

    }

    public Drawable getTabSelectedIndicator() {
        return this.tabSelectedIndicator;
    }

    public ColorStateList getTabTextColors() {
        return this.tabTextColors;
    }

    public void setTabTextColors(ColorStateList var1) {
        if (this.tabTextColors != var1) {
            this.tabTextColors = var1;
            this.updateAllTabs();
        }

    }

    public Tab newTab() {
        Tab var1 = this.createTabFromPool();
        var1.parent = this;
        var1.view = this.createTabView(var1);
        return var1;
    }

    public void onAttachedToWindow() {
        TabView tabView;
        super.onAttachedToWindow();
        for (int i = 0; i < getTabCount(); i++) {
            Tab tabAt = getTabAt(i);
            if (!(tabAt == null || (tabView = tabAt.view) == null)) {
                if (tabView.mMainTabTouchBackground != null) {
                    tabAt.view.mMainTabTouchBackground.setAlpha(0.0f);
                }
                if (tabAt.view.mIndicatorView != null) {
                    if (getSelectedTabPosition() == i) {
                        tabAt.view.mIndicatorView.setShow();
                    } else {
                        tabAt.view.mIndicatorView.setHide();
                    }
                }
            }
        }
        MaterialShapeUtils.setParentAbsoluteElevation(this);
        if (this.viewPager == null) {
            ViewParent parent = getParent();
            if (parent instanceof SeslViewPager) {
                setupWithViewPager((SeslViewPager) parent, true, true);
            }
        }
    }

    public void onConfigurationChanged(Configuration var1) {
        super.onConfigurationChanged(var1);

        for (int var2 = 0; var2 < this.getTabCount(); ++var2) {
            Tab var3 = this.getTabAt(var2);
            if (var3 != null) {
                TabView var4 = var3.view;
                if (var4 != null && var4.mMainTabTouchBackground != null) {
                    var3.view.mMainTabTouchBackground.setAlpha(0.0F);
                }
            }
        }

        this.updateBadgePosition();
    }

    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.setupViewPagerImplicitly) {
            setupWithViewPager((SeslViewPager) null);
            this.setupViewPagerImplicitly = false;
        }
    }

    public void onDraw(Canvas var1) {
        for (int var2 = 0; var2 < this.slidingTabIndicator.getChildCount(); ++var2) {
            View var3 = this.slidingTabIndicator.getChildAt(var2);
            if (var3 instanceof TabView) {
                ((TabView) var3).drawBackground(var1);
            }
        }

        super.onDraw(var1);
    }

    public void onLayout(boolean var1, int var2, int var3, int var4, int var5) {
        super.onLayout(var1, var2, var3, var4, var5);
        if (var1) {
            this.updateBadgePosition();
        }

    }

    @SuppressLint({"RestrictedApi", "WrongConstant"})
    public void onMeasure(int var1, int var2) {
        int var3 = (int) ViewUtils.dpToPx(this.getContext(), this.getDefaultHeight());
        int var4 = MeasureSpec.getMode(var2);
        boolean var5 = false;
        if (var4 != -2147483648) {
            if (var4 != 0) {
                var4 = var2;
            } else {
                var4 = MeasureSpec.makeMeasureSpec(var3 + this.getPaddingTop() + this.getPaddingBottom(), 1073741824);
            }
        } else {
            var4 = var2;
            if (this.getChildCount() == 1) {
                var4 = var2;
                if (MeasureSpec.getSize(var2) >= var3) {
                    this.getChildAt(0).setMinimumHeight(var3);
                    var4 = var2;
                }
            }
        }

        var3 = MeasureSpec.getSize(var1);
        if (MeasureSpec.getMode(var1) != 0) {
            var2 = this.requestedTabMaxWidth;
            if (var2 <= 0) {
                var2 = (int) ((float) var3 - ViewUtils.dpToPx(this.getContext(), 56));
            }

            this.tabMaxWidth = var2;
        }

        super.onMeasure(var1, var4);
        if (this.getChildCount() == 1) {
            View var6;
            boolean var7;
            label46:
            {
                label45:
                {
                    var6 = this.getChildAt(0);
                    var1 = this.mode;
                    if (var1 != 0) {
                        if (var1 == 1) {
                            var7 = var5;
                            if (var6.getMeasuredWidth() == this.getMeasuredWidth()) {
                                break label46;
                            }
                            break label45;
                        }

                        if (var1 != 2) {
                            var7 = var5;
                            break label46;
                        }
                    }

                    var7 = var5;
                    if (var6.getMeasuredWidth() >= this.getMeasuredWidth()) {
                        break label46;
                    }
                }

                var7 = true;
            }

            if (var7) {
                var1 = HorizontalScrollView.getChildMeasureSpec(var4, this.getPaddingTop() + this.getPaddingBottom(), var6.getLayoutParams().height);
                var6.measure(MeasureSpec.makeMeasureSpec(this.getMeasuredWidth(), 1073741824), var1);
            }
        }

    }

    public void onVisibilityChanged(View var1, int var2) {
        super.onVisibilityChanged(var1, var2);

        for (var2 = 0; var2 < this.getTabCount(); ++var2) {
            Tab var4 = this.getTabAt(var2);
            if (var4 != null) {
                TabView var3 = var4.view;
                if (var3 != null && var3.mMainTabTouchBackground != null) {
                    var4.view.mMainTabTouchBackground.setAlpha(0.0F);
                }
            }
        }

    }

    public void populateFromPagerAdapter() {
        int currentItem;
        removeAllTabs();
        PagerAdapter pagerAdapter2 = this.pagerAdapter;
        if (pagerAdapter2 != null) {
            int count = pagerAdapter2.getCount();
            for (int i = 0; i < count; i++) {
                Tab newTab = newTab();
                newTab.setText(this.pagerAdapter.getPageTitle(i));
                addTab(newTab, false);
            }
            SeslViewPager viewPager2 = this.viewPager;
            if (viewPager2 != null && count > 0 && (currentItem = viewPager2.getCurrentItem()) != getSelectedTabPosition() && currentItem < getTabCount()) {
                selectTab(getTabAt(currentItem), true, true);
            }
        }
    }

    public boolean releaseFromTabPool(Tab var1) {
        return tabPool.release(var1);
    }

    public void removeAllTabs() {
        for (int var1 = this.slidingTabIndicator.getChildCount() - 1; var1 >= 0; --var1) {
            this.removeTabViewAt(var1);
        }

        Iterator var2 = this.tabs.iterator();

        while (var2.hasNext()) {
            Tab var3 = (Tab) var2.next();
            var2.remove();
            var3.reset();
            this.releaseFromTabPool(var3);
        }

        this.selectedTab = null;
    }

    public void removeOnTabSelectedListener(OnTabSelectedListener var1) {
        removeOnTabSelectedListener((BaseOnTabSelectedListener) var1);
    }

    @Deprecated
    public void removeOnTabSelectedListener(BaseOnTabSelectedListener var1) {
        this.selectedListeners.remove(var1);
    }

    public final void removeTabViewAt(int var1) {
        TabView var2 = (TabView) this.slidingTabIndicator.getChildAt(var1);
        this.slidingTabIndicator.removeViewAt(var1);
        if (var2 != null) {
            var2.reset();
            this.tabViewPool.release(var2);
        }

        this.requestLayout();
    }

    public void selectTab(Tab var1) {
        this.selectTab(var1, true);
    }

    public void selectTab(Tab var1, boolean var2) {
        this.selectTab(var1, var2, true);
    }

    public final void selectTab(Tab var1, boolean var2, boolean var3) {
        SeslViewPager viewPager2;
        if (var1 == null || var1.view.isEnabled() || (viewPager2 = this.viewPager) == null) {
            Tab tab2 = this.selectedTab;
            if (tab2 != var1) {
                int position = var1 != null ? var1.getPosition() : -1;
                if (var2) {
                    if ((tab2 == null || tab2.getPosition() == -1) && position != -1) {
                        setScrollPosition(position, 0.0f, true);
                    } else {
                        animateToTab(position);
                    }
                    if (position != -1) {
                        setSelectedTabView(position, var3);
                    }
                }
                this.selectedTab = var1;
                if (tab2 != null) {
                    dispatchTabUnselected(tab2);
                }
                if (var1 != null) {
                    dispatchTabSelected(var1);
                }
            } else if (tab2 != null) {
                dispatchTabReselected(var1);
                animateToTab(var1.getPosition());
            }
        } else {
            viewPager2.setCurrentItem(getSelectedTabPosition());
        }
    }

    public void setElevation(float var1) {
        super.setElevation(var1);
        MaterialShapeUtils.setElevation(this, var1);
    }

    public void setInlineLabel(boolean var1) {
        if (this.inlineLabel != var1) {
            this.inlineLabel = var1;

            for (int var2 = 0; var2 < this.slidingTabIndicator.getChildCount(); ++var2) {
                View var3 = this.slidingTabIndicator.getChildAt(var2);
                if (var3 instanceof TabView) {
                    ((TabView) var3).updateOrientation();
                }
            }

            this.applyModeAndGravity();
        }

    }

    public void setInlineLabelResource(int var1) {
        this.setInlineLabel(this.getResources().getBoolean(var1));
    }

    @Deprecated
    public void setOnTabSelectedListener(BaseOnTabSelectedListener var1) {
        BaseOnTabSelectedListener var2 = this.selectedListener;
        if (var2 != null) {
            this.removeOnTabSelectedListener(var2);
        }

        this.selectedListener = var1;
        if (var1 != null) {
            this.addOnTabSelectedListener(var1);
        }

    }

    @Deprecated
    public void setOnTabSelectedListener(OnTabSelectedListener var1) {
        this.setOnTabSelectedListener((BaseOnTabSelectedListener) var1);
    }

    public void setPagerAdapter(PagerAdapter var1, boolean var2) {
        PagerAdapter var3 = this.pagerAdapter;
        if (var3 != null) {
            DataSetObserver var4 = this.pagerAdapterObserver;
            if (var4 != null) {
                var3.unregisterDataSetObserver(var4);
            }
        }

        this.pagerAdapter = var1;
        if (var2 && var1 != null) {
            if (this.pagerAdapterObserver == null) {
                this.pagerAdapterObserver = new PagerAdapterObserver();
            }

            var1.registerDataSetObserver(this.pagerAdapterObserver);
        }

        this.populateFromPagerAdapter();
    }

    public void setScrollAnimatorListener(Animator.AnimatorListener var1) {
        this.ensureScrollAnimator();
        this.scrollAnimator.addListener(var1);
    }

    public void setScrollPosition(int var1, float var2, boolean var3) {
        this.setScrollPosition(var1, var2, var3, true);
    }

    public void setScrollPosition(int var1, float var2, boolean var3, boolean var4) {
        int var5 = Math.round((float) var1 + var2);
        if (var5 >= 0 && var5 < this.slidingTabIndicator.getChildCount()) {
            if (var4) {
                this.slidingTabIndicator.setIndicatorPositionFromTabPosition(var1, var2);
            }

            ValueAnimator var6 = this.scrollAnimator;
            if (var6 != null && var6.isRunning()) {
                this.scrollAnimator.cancel();
            }

            this.scrollTo(this.calculateScrollXForTab(var1, var2), 0);
            if (var3) {
                this.setSelectedTabView(var5, true);
            }
        }

    }

    public void setSelectedTabIndicator(int var1) {
        if (var1 != 0) {
            this.setSelectedTabIndicator(AppCompatResources.getDrawable(this.getContext(), var1));
        } else {
            this.setSelectedTabIndicator((Drawable) null);
        }

    }

    public void setSelectedTabIndicator(Drawable var1) {
        if (this.tabSelectedIndicator != var1) {
            this.tabSelectedIndicator = var1;
            ViewCompat.postInvalidateOnAnimation(this.slidingTabIndicator);
        }

    }

    public void setSelectedTabIndicatorColor(int var1) {
        this.mTabSelectedIndicatorColor = var1;
        Iterator var2 = this.tabs.iterator();

        while (true) {
            AbsIndicatorView var3;
            do {
                if (!var2.hasNext()) {
                    return;
                }

                var3 = ((Tab) var2.next()).view.mIndicatorView;
            } while (var3 == null);

            label21:
            {
                if (this.mDepthStyle == 2) {
                    int var4 = this.mSubTabSelectedIndicatorColor;
                    if (var4 != -1) {
                        var3.setSelectedIndicatorColor(var4);
                        break label21;
                    }
                }

                var3.setSelectedIndicatorColor(var1);
            }

            var3.invalidate();
        }
    }

    public void setSelectedTabIndicatorGravity(int var1) {
        if (this.tabIndicatorGravity != var1) {
            this.tabIndicatorGravity = var1;
            ViewCompat.postInvalidateOnAnimation(this.slidingTabIndicator);
        }

    }

    @Deprecated
    public void setSelectedTabIndicatorHeight(int var1) {
        this.slidingTabIndicator.setSelectedIndicatorHeight(var1);
    }

    public final void setSelectedTabView(int var1, boolean var2) {
        int var3 = this.slidingTabIndicator.getChildCount();
        if (var1 < var3) {
            int var4 = 0;

            while (true) {
                boolean var5 = true;
                if (var4 >= var3) {
                    ((Tab) this.tabs.get(var1)).view.setSelected(true);

                    for (var4 = 0; var4 < this.getTabCount(); ++var4) {
                        TabView var8 = ((Tab) this.tabs.get(var4)).view;
                        if (var4 == var1) {
                            if (var8.textView != null) {
                                this.startTextColorChangeAnimation(var8.textView, this.getSelectedTabTextColor());
                                var8.textView.setTypeface(this.mBoldTypeface);
                                var8.textView.setSelected(true);
                            }

                            if (var8.mIndicatorView != null) {
                                if (var2) {
                                    if (var8.mIndicatorView.getAlpha() != 1.0F) {
                                        var8.mIndicatorView.setShow();
                                    }
                                } else {
                                    ((Tab) this.tabs.get(var4)).view.mIndicatorView.setReleased();
                                }
                            }
                        } else {
                            if (var8.mIndicatorView != null) {
                                var8.mIndicatorView.setHide();
                            }

                            if (var8.textView != null) {
                                var8.textView.setTypeface(this.mNormalTypeface);
                                this.startTextColorChangeAnimation(var8.textView, this.tabTextColors.getDefaultColor());
                                var8.textView.setSelected(false);
                            }
                        }
                    }
                    break;
                }

                View var6 = this.slidingTabIndicator.getChildAt(var4);
                boolean var7;
                if (var4 == var1) {
                    var7 = true;
                } else {
                    var7 = false;
                }

                var6.setSelected(var7);
                if (var4 == var1) {
                    var7 = var5;
                } else {
                    var7 = false;
                }

                var6.setActivated(var7);
                ++var4;
            }
        }

    }

    public void setTabIconTintResource(int var1) {
        this.setTabIconTint(AppCompatResources.getColorStateList(this.getContext(), var1));
    }

    public void setTabIndicatorFullWidth(boolean var1) {
        this.tabIndicatorFullWidth = var1;
        ViewCompat.postInvalidateOnAnimation(this.slidingTabIndicator);
    }

    public void setTabRippleColorResource(int var1) {
        this.setTabRippleColor(AppCompatResources.getColorStateList(this.getContext(), var1));
    }

    @Deprecated
    public void setTabsFromPagerAdapter(PagerAdapter var1) {
        this.setPagerAdapter(var1, false);
    }

    public void setUnboundedRipple(boolean var1) {
        if (this.unboundedRipple != var1) {
            this.unboundedRipple = var1;

            for (int var2 = 0; var2 < this.slidingTabIndicator.getChildCount(); ++var2) {
                View var3 = this.slidingTabIndicator.getChildAt(var2);
                if (var3 instanceof TabView) {
                    ((TabView) var3).updateBackgroundDrawable(this.getContext());
                }
            }
        }

    }

    public void setUnboundedRippleResource(int var1) {
        this.setUnboundedRipple(this.getResources().getBoolean(var1));
    }

    public void setupWithViewPager(SeslViewPager var1) {
        this.setupWithViewPager(var1, true);
    }

    public void setupWithViewPager(SeslViewPager var1, boolean var2) {
        this.setupWithViewPager(var1, var2, false);
    }

    public final void setupWithViewPager(SeslViewPager var1, boolean var2, boolean var3) {
        SeslViewPager var4 = this.viewPager;
        if (var4 != null) {
            TabLayoutOnPageChangeListener var5 = this.pageChangeListener;
            if (var5 != null) {
                var4.removeOnPageChangeListener(var5);
            }

            AdapterChangeListener var7 = this.adapterChangeListener;
            if (var7 != null) {
                this.viewPager.removeOnAdapterChangeListener(var7);
            }
        }

        BaseOnTabSelectedListener var8 = this.currentVpSelectedListener;
        if (var8 != null) {
            this.removeOnTabSelectedListener(var8);
            this.currentVpSelectedListener = null;
        }

        if (var1 != null) {
            this.viewPager = var1;
            if (this.pageChangeListener == null) {
                this.pageChangeListener = new TabLayoutOnPageChangeListener(this);
            }

            this.pageChangeListener.reset();
            var1.addOnPageChangeListener(this.pageChangeListener);
            this.currentVpSelectedListener = new ViewPagerOnTabSelectedListener(var1);
            this.addOnTabSelectedListener(this.currentVpSelectedListener);
            PagerAdapter var6 = var1.getAdapter();
            if (var6 != null) {
                this.setPagerAdapter(var6, var2);
            }

            if (this.adapterChangeListener == null) {
                this.adapterChangeListener = new AdapterChangeListener();
            }

            this.adapterChangeListener.setAutoRefresh(var2);
            var1.addOnAdapterChangeListener(this.adapterChangeListener);
            this.setScrollPosition(var1.getCurrentItem(), 0.0F, true);
        } else {
            this.viewPager = null;
            this.setPagerAdapter((PagerAdapter) null, false);
        }

        this.setupViewPagerImplicitly = var3;
    }

    public boolean shouldDelayChildPressedState() {
        boolean var1;
        if (this.getTabScrollRange() > 0) {
            var1 = true;
        } else {
            var1 = false;
        }

        return var1;
    }

    public final void startTextColorChangeAnimation(TextView var1, int var2) {
        if (var1 != null) {
            var1.setTextColor(var2);
        }

    }

    public final void updateAllTabs() {
        int var1 = this.tabs.size();

        for (int var2 = 0; var2 < var1; ++var2) {
            ((Tab) this.tabs.get(var2)).updateView();
        }

    }

    @SuppressLint("WrongConstant")
    public final void updateBadgePosition() {
        ArrayList var1 = this.tabs;
        if (var1 != null && var1.size() != 0) {
            for (int var2 = 0; var2 < this.tabs.size(); ++var2) {
                Tab var10 = (Tab) this.tabs.get(var2);
                TabView var3 = ((Tab) this.tabs.get(var2)).view;
                if (var10 != null && var3 != null) {
                    TextView var4 = var3.textView;
                    if (var3.getWidth() > 0 && var4 != null && var4.getWidth() > 0) {
                        TextView var11 = null;
                        int var5;
                        int var6;
                        if (var3.mNBadgeView != null && var3.mNBadgeView.getVisibility() == 0) {
                            var11 = var3.mNBadgeView;
                            var5 = ((RelativeLayout.LayoutParams) var11.getLayoutParams()).getMarginStart();
                            var6 = this.getContext().getResources().getDimensionPixelSize(R.dimen.sesl_tablayout_subtab_n_badge_xoffset);
                        } else if (var3.mDotBadgeView != null && var3.mDotBadgeView.getVisibility() == 0) {
                            var11 = var3.mDotBadgeView;
                            var5 = ((RelativeLayout.LayoutParams) var11.getLayoutParams()).getMarginStart();
                            var6 = this.getContext().getResources().getDimensionPixelSize(R.dimen.sesl_tablayout_subtab_dot_badge_offset_x);
                        } else {
                            var6 = this.getContext().getResources().getDimensionPixelSize(R.dimen.sesl_tablayout_subtab_n_badge_xoffset);
                            var5 = 0;
                        }

                        if (var11 != null && var11.getVisibility() == 0) {
                            int var7;
                            int var8;
                            int var9;
                            label56:
                            {
                                var11.measure(0, 0);
                                var7 = var11.getMeasuredWidth();
                                var8 = var3.getWidth();
                                if (var5 != 0) {
                                    var9 = var5;
                                    if (var5 >= var4.getRight()) {
                                        break label56;
                                    }
                                }

                                var9 = var4.getRight() + var6;
                            }

                            if (var9 > var8) {
                                var5 = var8 - var7;
                            } else {
                                var6 = var9 + var7;
                                var5 = var9;
                                if (var6 > var8) {
                                    var5 = var9 - (var6 - var8);
                                }
                            }

                            var5 = Math.max(0, var5);
                            RelativeLayout.LayoutParams var12 = (RelativeLayout.LayoutParams) var11.getLayoutParams();
                            var12.setMarginStart(var5);
                            var12.width = var7;
                            var11.setLayoutParams(var12);
                        }
                    }
                }
            }
        }

    }

    public final void updateTabViewLayoutParams(LinearLayout.LayoutParams var1) {
        if (this.mode == 1 && this.tabGravity == 0) {
            var1.width = 0;
            var1.weight = 1.0F;
        } else {
            var1.width = -2;
            var1.weight = 0.0F;
        }

    }

    public void updateTabViews(boolean var1) {
        for (int var2 = 0; var2 < this.slidingTabIndicator.getChildCount(); ++var2) {
            View var3 = this.slidingTabIndicator.getChildAt(var2);
            var3.setMinimumWidth(this.getTabMinWidth());
            this.updateTabViewLayoutParams((LinearLayout.LayoutParams) var3.getLayoutParams());
            if (var1) {
                var3.requestLayout();
            }
        }

        this.updateBadgePosition();
    }

    @Deprecated
    public interface BaseOnTabSelectedListener<T extends Tab> {
        void onTabReselected(T var1);

        void onTabSelected(T var1);

        void onTabUnselected(T var1);
    }

    public interface OnTabSelectedListener extends BaseOnTabSelectedListener<Tab> {
    }

    public static class Tab {
        public CharSequence contentDesc;
        public View customView;
        public Drawable icon;
        public int labelVisibilityMode = 1;
        public SamsungBaseTabLayout parent;
        public int position = -1;
        public Object tag;
        public CharSequence text;
        public TabView view;

        public Tab() {
        }

        public View getCustomView() {
            return this.customView;
        }

        public Tab setCustomView(int var1) {
            this.setCustomView(LayoutInflater.from(this.view.getContext()).inflate(var1, this.view, false));
            return this;
        }

        public Tab setCustomView(View var1) {
            if (this.view.textView != null) {
                this.view.removeAllViews();
            }

            this.customView = var1;
            this.updateView();
            return this;
        }

        public Drawable getIcon() {
            return this.icon;
        }

        public Tab setIcon(Drawable var1) {
            this.icon = var1;
            SamsungBaseTabLayout var2 = this.parent;
            if (var2.tabGravity == 1 || var2.mode == 2) {
                this.parent.updateTabViews(true);
            }

            this.updateView();
            if (BadgeUtils.USE_COMPAT_PARENT && this.view.hasBadgeDrawable() && this.view.badgeDrawable.isVisible()) {
                this.view.invalidate();
            }

            return this;
        }

        public int getPosition() {
            return this.position;
        }

        public void setPosition(int var1) {
            this.position = var1;
        }

        public int getTabLabelVisibility() {
            return this.labelVisibilityMode;
        }

        public CharSequence getText() {
            return this.text;
        }

        public Tab setText(CharSequence var1) {
            if (TextUtils.isEmpty(this.contentDesc) && !TextUtils.isEmpty(var1)) {
                this.view.setContentDescription(var1);
            }

            this.text = var1;
            this.updateView();
            return this;
        }

        public boolean isSelected() {
            SamsungBaseTabLayout var1 = this.parent;
            if (var1 != null) {
                boolean var2;
                if (var1.getSelectedTabPosition() == this.position) {
                    var2 = true;
                } else {
                    var2 = false;
                }

                return var2;
            } else {
                throw new IllegalArgumentException("Tab not attached to a TabLayout");
            }
        }

        public void reset() {
            this.parent = null;
            this.view = null;
            this.tag = null;
            this.icon = null;
            this.text = null;
            this.contentDesc = null;
            this.position = -1;
            this.customView = null;
        }

        public void select() {
            SamsungBaseTabLayout var1 = this.parent;
            if (var1 != null) {
                var1.selectTab(this);
            } else {
                throw new IllegalArgumentException("Tab not attached to a TabLayout");
            }
        }

        public TextView seslGetTextView() {
            TextView var2;
            if (this.customView == null) {
                TabView var1 = this.view;
                if (var1 != null) {
                    var2 = var1.textView;
                    return var2;
                }
            }

            var2 = null;
            return var2;
        }

        public Tab setContentDescription(CharSequence var1) {
            this.contentDesc = var1;
            this.updateView();
            return this;
        }

        public void updateView() {
            TabView var1 = this.view;
            if (var1 != null) {
                var1.update();
            }

        }
    }

    public static class TabLayoutOnPageChangeListener implements SeslViewPager.OnPageChangeListener {
        public final WeakReference<SamsungBaseTabLayout> tabLayoutRef;
        public int previousScrollState;
        public int scrollState;

        public TabLayoutOnPageChangeListener(SamsungBaseTabLayout var1) {
            this.tabLayoutRef = new WeakReference(var1);
        }

        public void onPageScrollStateChanged(int var1) {
            this.previousScrollState = this.scrollState;
            this.scrollState = var1;
        }

        public void onPageScrolled(int var1, float var2, int var3) {
            SamsungBaseTabLayout var4 = (SamsungBaseTabLayout) this.tabLayoutRef.get();
            if (var4 != null) {
                var3 = this.scrollState;
                boolean var5 = false;
                boolean var6;
                if (var3 == 2 && this.previousScrollState != 1) {
                    var6 = false;
                } else {
                    var6 = true;
                }

                if (this.scrollState != 2 || this.previousScrollState != 0) {
                    var5 = true;
                }

                var4.setScrollPosition(var1, var2, var6, var5);
            }

        }

        public void onPageSelected(int var1) {
            SamsungBaseTabLayout var2 = (SamsungBaseTabLayout) this.tabLayoutRef.get();
            if (var2 != null && var2.getSelectedTabPosition() != var1 && var1 < var2.getTabCount()) {
                int var3 = this.scrollState;
                boolean var4;
                if (var3 == 0 || var3 == 2 && this.previousScrollState == 0) {
                    var4 = true;
                } else {
                    var4 = false;
                }

                var2.selectTab(var2.getTabAt(var1), var4);
            }

        }

        public void reset() {
            this.scrollState = 0;
            this.previousScrollState = 0;
        }
    }

    public static class ViewPagerOnTabSelectedListener implements OnTabSelectedListener {
        public final SeslViewPager viewPager;

        public ViewPagerOnTabSelectedListener(SeslViewPager var1) {
            this.viewPager = var1;
        }

        public void onTabReselected(Tab var1) {
        }

        public void onTabSelected(Tab var1) {
            this.viewPager.setCurrentItem(var1.getPosition());
        }

        public void onTabUnselected(Tab var1) {
        }
    }

    private class AdapterChangeListener implements SeslViewPager.OnAdapterChangeListener {
        public boolean autoRefresh;

        public AdapterChangeListener() {
        }

        public void onAdapterChanged(SeslViewPager var1, PagerAdapter var2, PagerAdapter var3) {
            SamsungBaseTabLayout var4 = SamsungBaseTabLayout.this;
            if (var4.viewPager == var1) {
                var4.setPagerAdapter(var3, this.autoRefresh);
            }

        }

        public void setAutoRefresh(boolean var1) {
            this.autoRefresh = var1;
        }
    }

    private class PagerAdapterObserver extends DataSetObserver {
        public PagerAdapterObserver() {
        }

        public void onChanged() {
            SamsungBaseTabLayout.this.populateFromPagerAdapter();
        }

        public void onInvalidated() {
            SamsungBaseTabLayout.this.populateFromPagerAdapter();
        }
    }

    private class SlidingTabIndicator extends LinearLayout {
        public final GradientDrawable defaultSelectionIndicator;
        public final Paint selectedIndicatorPaint;
        public ValueAnimator indicatorAnimator;
        public int indicatorLeft = -1;
        public int indicatorRight = -1;
        public int layoutDirection = -1;
        public int selectedIndicatorHeight;
        public int selectedPosition = -1;
        public float selectionOffset;

        public SlidingTabIndicator(Context var2) {
            super(var2);
            this.setWillNotDraw(false);
            this.selectedIndicatorPaint = new Paint();
            this.defaultSelectionIndicator = new GradientDrawable();
        }

        public void animateIndicatorToPosition(int var1, int var2) {
        }

        public boolean childrenNeedLayout() {
            int var1 = this.getChildCount();

            for (int var2 = 0; var2 < var1; ++var2) {
                if (this.getChildAt(var2).getWidth() <= 0) {
                    return true;
                }
            }

            return false;
        }

        public void draw(Canvas var1) {
            super.draw(var1);
        }

        public void onLayout(boolean var1, int var2, int var3, int var4, int var5) {
            super.onLayout(var1, var2, var3, var4, var5);
            ValueAnimator var6 = this.indicatorAnimator;
            if (var6 != null && var6.isRunning()) {
                this.indicatorAnimator.cancel();
                long var7 = this.indicatorAnimator.getDuration();
                this.animateIndicatorToPosition(this.selectedPosition, Math.round((1.0F - this.indicatorAnimator.getAnimatedFraction()) * (float) var7));
            } else {
                this.updateIndicatorPosition();
            }

        }

        @SuppressLint({"RestrictedApi", "WrongConstant"})
        public void onMeasure(int var1, int var2) {
            super.onMeasure(var1, var2);
            if (MeasureSpec.getMode(var1) == 1073741824) {
                SamsungBaseTabLayout var3 = SamsungBaseTabLayout.this;
                if (var3.tabGravity == 1 || var3.mode == 2) {
                    int var4 = this.getChildCount();
                    byte var5 = 0;
                    int var6 = 0;

                    int var7;
                    int var8;
                    for (var7 = var6; var6 < var4; var7 = var8) {
                        View var9 = this.getChildAt(var6);
                        var8 = var7;
                        if (var9.getVisibility() == 0) {
                            var8 = Math.max(var7, var9.getMeasuredWidth());
                        }

                        ++var6;
                    }

                    if (var7 <= 0) {
                        return;
                    }

                    var6 = (int) ViewUtils.dpToPx(this.getContext(), 16);
                    boolean var11;
                    if (var7 * var4 > this.getMeasuredWidth() - var6 * 2) {
                        var3 = SamsungBaseTabLayout.this;
                        var3.tabGravity = 0;
                        var3.updateTabViews(false);
                        var11 = true;
                    } else {
                        boolean var12 = false;
                        var8 = var5;

                        while (true) {
                            var11 = var12;
                            if (var8 >= var4) {
                                break;
                            }

                            LayoutParams var10 = (LayoutParams) this.getChildAt(var8).getLayoutParams();
                            if (var10.width != var7 || var10.weight != 0.0F) {
                                var10.width = var7;
                                var10.weight = 0.0F;
                                var12 = true;
                            }

                            ++var8;
                        }
                    }

                    if (var11) {
                        super.onMeasure(var1, var2);
                    }
                }

            }
        }

        public void onRtlPropertiesChanged(int var1) {
            super.onRtlPropertiesChanged(var1);
            if (Build.VERSION.SDK_INT < 23 && this.layoutDirection != var1) {
                this.requestLayout();
                this.layoutDirection = var1;
            }

        }

        public void setIndicatorPositionFromTabPosition(int var1, float var2) {
            ValueAnimator var3 = this.indicatorAnimator;
            if (var3 != null && var3.isRunning()) {
                this.indicatorAnimator.cancel();
            }

            this.selectedPosition = var1;
            this.selectionOffset = var2;
            this.updateIndicatorPosition();
        }

        public void setSelectedIndicatorColor(int var1) {
            if (this.selectedIndicatorPaint.getColor() != var1) {
                this.selectedIndicatorPaint.setColor(var1);
                ViewCompat.postInvalidateOnAnimation(this);
            }

        }

        public void setSelectedIndicatorHeight(int var1) {
            if (this.selectedIndicatorHeight != var1) {
                this.selectedIndicatorHeight = var1;
                ViewCompat.postInvalidateOnAnimation(this);
            }

        }

        public final void updateIndicatorPosition() {
        }
    }

    public final class TabView extends LinearLayout {
        public View badgeAnchorView;
        public BadgeDrawable badgeDrawable;
        public Drawable baseBackgroundDrawable;
        public ImageView customIconView;
        public TextView customTextView;
        public View customView;
        public int defaultMaxLines = 2;
        public ImageView iconView;
        public TextView mDotBadgeView;
        public int mIconSize;
        public AbsIndicatorView mIndicatorView;
        public boolean mIsCallPerformClick;
        public View mMainTabTouchBackground;
        public TextView mNBadgeView;
        public OnKeyListener mTabViewKeyListener = new OnKeyListener() {
            public boolean onKey(View var1, int var2, KeyEvent var3) {
                return false;
            }
        };
        public RelativeLayout mTextParentView;
        public Tab tab;
        public TextView textView;

        public TabView(Context var2) {
            super(var2);
            this.updateBackgroundDrawable(var2);
            this.setGravity(17);
            this.setOrientation(SamsungBaseTabLayout.this.inlineLabel ? HORIZONTAL : VERTICAL);
            this.setClickable(true);
            ViewCompat.setPointerIcon(this, PointerIconCompat.getSystemIcon(this.getContext(), 1002));
            ViewCompat.setAccessibilityDelegate(this, (AccessibilityDelegateCompat) null);
            this.setOnKeyListener(this.mTabViewKeyListener);
            if (SamsungBaseTabLayout.this.mDepthStyle == 1) {
                ViewCompat.setPaddingRelative(this, 0, SamsungBaseTabLayout.this.tabPaddingTop, 0, SamsungBaseTabLayout.this.tabPaddingBottom);
            }

            this.mIconSize = this.getResources().getDimensionPixelOffset(R.dimen.sesl_tab_icon_size);
        }

        private BadgeDrawable getBadge() {
            return this.badgeDrawable;
        }

        @SuppressLint("WrongConstant")
        private int getContentWidth() {
            View[] var1 = new View[3];
            TextView var2 = this.textView;
            int var3 = 0;
            var1[0] = var2;
            var1[1] = this.iconView;
            var1[2] = this.customView;
            int var4 = var1.length;
            int var5 = 0;
            int var6 = var5;

            int var10;
            for (int var7 = var5; var3 < var4; var7 = var10) {
                View var11 = var1[var3];
                int var8 = var5;
                int var9 = var6;
                var10 = var7;
                if (var11 != null) {
                    var8 = var5;
                    var9 = var6;
                    var10 = var7;
                    if (var11.getVisibility() == 0) {
                        if (var7 != 0) {
                            var6 = Math.min(var6, var11.getLeft());
                        } else {
                            var6 = var11.getLeft();
                        }

                        if (var7 != 0) {
                            var7 = Math.max(var5, var11.getRight());
                        } else {
                            var7 = var11.getRight();
                        }

                        var10 = 1;
                        var9 = var6;
                        var8 = var7;
                    }
                }

                ++var3;
                var5 = var8;
                var6 = var9;
            }

            return var5 - var6;
        }

        private BadgeDrawable getOrCreateBadge() {
            if (this.badgeDrawable == null) {
                this.badgeDrawable = BadgeDrawable.create(this.getContext());
            }

            this.tryUpdateBadgeAnchor();
            BadgeDrawable var1 = this.badgeDrawable;
            if (var1 != null) {
                return var1;
            } else {
                throw new IllegalStateException("Unable to create badge");
            }
        }

        public final void addOnLayoutChangeListener(final View var1) {
            if (var1 != null) {
                var1.addOnLayoutChangeListener(new OnLayoutChangeListener() {
                    @SuppressLint("WrongConstant")
                    public void onLayoutChange(View var1x, int var2, int var3, int var4, int var5, int var6, int var7, int var8, int var9) {
                        if (var1.getVisibility() == 0) {
                            TabView.this.tryUpdateBadgeDrawableBounds(var1);
                        }

                    }
                });
            }
        }

        public final float approximateLineWidth(Layout var1, int var2, float var3) {
            return var1.getLineWidth(var2) * (var3 / var1.getPaint().getTextSize());
        }

        public final void drawBackground(Canvas var1) {
            Drawable var2 = this.baseBackgroundDrawable;
            if (var2 != null) {
                var2.setBounds(this.getLeft(), this.getTop(), this.getRight(), this.getBottom());
                this.baseBackgroundDrawable.draw(var1);
            }

        }

        public void drawableStateChanged() {
            super.drawableStateChanged();
            int[] var1 = this.getDrawableState();
            Drawable var2 = this.baseBackgroundDrawable;
            if (var2 != null && var2.isStateful()) {
                this.baseBackgroundDrawable.setState(var1);
            }

        }

        public final FrameLayout getCustomParentForBadge(View var1) {
            ImageView var2 = this.iconView;
            FrameLayout var3 = null;
            if (var1 != var2 && var1 != this.textView) {
                return null;
            } else {
                if (BadgeUtils.USE_COMPAT_PARENT) {
                    var3 = (FrameLayout) var1.getParent();
                }

                return var3;
            }
        }

        public Tab getTab() {
            return this.tab;
        }

        public void setTab(Tab var1) {
            if (var1 != this.tab) {
                this.tab = var1;
                this.update();
            }

        }

        public final boolean hasBadgeDrawable() {
            boolean var1;
            if (this.badgeDrawable != null) {
                var1 = true;
            } else {
                var1 = false;
            }

            return var1;
        }

        public void onConfigurationChanged(Configuration var1) {
            super.onConfigurationChanged(var1);
            this.mIconSize = this.getResources().getDimensionPixelOffset(R.dimen.sesl_tab_icon_size);
        }

        public void onInitializeAccessibilityEvent(AccessibilityEvent var1) {
            super.onInitializeAccessibilityEvent(var1);
            var1.setClassName(androidx.appcompat.app.ActionBar.Tab.class.getName());
        }

        @SuppressLint("WrongConstant")
        @TargetApi(14)
        public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo var1) {
            super.onInitializeAccessibilityNodeInfo(var1);
            var1.setClassName(androidx.appcompat.app.ActionBar.Tab.class.getName());
            BadgeDrawable var2 = this.badgeDrawable;
            StringBuilder var4;
            if (var2 != null && var2.isVisible()) {
                CharSequence var3 = this.getContentDescription();
                var4 = new StringBuilder();
                var4.append(var3);
                var4.append(", ");
                var4.append(this.badgeDrawable.getContentDescription());
                var1.setContentDescription(var4.toString());
            }

            TextView var5 = this.mNBadgeView;
            if (var5 != null && var5.getVisibility() == 0 && this.mNBadgeView.getContentDescription() != null) {
                var4 = new StringBuilder();
                var4.append(this.getContentDescription());
                var4.append(", ");
                var4.append(this.mNBadgeView.getContentDescription());
                var1.setContentDescription(var4.toString());
            }

        }

        @SuppressLint("RestrictedApi")
        public void onLayout(boolean var1, int var2, int var3, int var4, int var5) {
            super.onLayout(var1, var2, var3, var4, var5);
            View var6 = this.mMainTabTouchBackground;
            if (var6 != null) {
                var6.setLeft(0);
                View var7 = this.mMainTabTouchBackground;
                RelativeLayout var8 = this.mTextParentView;
                if (var8 != null) {
                    var2 = var8.getWidth();
                } else {
                    var2 = var4 - var2;
                }

                var7.setRight(var2);
                if (this.mMainTabTouchBackground.getAnimation() != null && this.mMainTabTouchBackground.getAnimation().hasEnded()) {
                    this.mMainTabTouchBackground.setAlpha(0.0F);
                }
            }

            if (this.iconView != null && this.tab.icon != null) {
                TextView var9 = this.textView;
                if (var9 != null && this.mIndicatorView != null && this.mTextParentView != null) {
                    var3 = this.mIconSize + var9.getMeasuredWidth();
                    var2 = var3;
                    if (SamsungBaseTabLayout.this.mIconTextGap != -1) {
                        var2 = var3 + SamsungBaseTabLayout.this.mIconTextGap;
                    }

                    var2 = Math.abs((this.getWidth() - var2) / 2);
                    if (ViewUtils.isLayoutRtl(this)) {
                        var2 = -var2;
                        if (this.iconView.getRight() == this.mTextParentView.getRight()) {
                            this.textView.offsetLeftAndRight(var2);
                            this.iconView.offsetLeftAndRight(var2);
                            this.mIndicatorView.offsetLeftAndRight(var2);
                        }
                    } else if (this.iconView.getLeft() == this.mTextParentView.getLeft()) {
                        this.textView.offsetLeftAndRight(var2);
                        this.iconView.offsetLeftAndRight(var2);
                        this.mIndicatorView.offsetLeftAndRight(var2);
                    }
                }
            }

        }

        @SuppressLint("WrongConstant")
        public void onMeasure(int var1, int var2) {
            int var3 = MeasureSpec.getSize(var1);
            int var4 = MeasureSpec.getMode(var1);
            int var5 = SamsungBaseTabLayout.this.getTabMaxWidth();
            int var6;
            if (SamsungBaseTabLayout.this.mRequestedTabWidth != -1) {
                var6 = MeasureSpec.makeMeasureSpec(SamsungBaseTabLayout.this.mRequestedTabWidth, 1073741824);
            } else {
                var6 = var1;
                if (var5 > 0) {
                    label80:
                    {
                        if (var4 != 0) {
                            var6 = var1;
                            if (var3 <= var5) {
                                break label80;
                            }
                        }

                        var6 = MeasureSpec.makeMeasureSpec(SamsungBaseTabLayout.this.tabMaxWidth, -2147483648);
                    }
                }
            }

            super.onMeasure(var6, var2);
            TextView var7 = this.textView;
            if (var7 != null && this.customView == null) {
                SamsungBaseTabLayout var8 = SamsungBaseTabLayout.this;
                float var9 = var8.tabTextSize;
                var8.checkMaxFontScale(var7, (int) var9);
                var4 = this.defaultMaxLines;
                ImageView var15 = this.iconView;
                boolean var13 = true;
                float var10;
                if (var15 != null && var15.getVisibility() == 0) {
                    var1 = 1;
                    var10 = var9;
                } else {
                    var7 = this.textView;
                    var1 = var4;
                    var10 = var9;
                    if (var7 != null) {
                        var1 = var4;
                        var10 = var9;
                        if (var7.getLineCount() > 1) {
                            var10 = SamsungBaseTabLayout.this.tabTextMultiLineSize;
                            var1 = var4;
                        }
                    }
                }

                var9 = this.textView.getTextSize();
                int var11 = this.textView.getLineCount();
                var4 = TextViewCompat.getMaxLines(this.textView);
                float var19;
                int var12 = (var19 = var10 - var9) == 0.0F ? 0 : (var19 < 0.0F ? -1 : 1);
                if (var12 != 0 || var4 >= 0 && var1 != var4) {
                    boolean var14 = var13;
                    if (SamsungBaseTabLayout.this.mode == 1) {
                        var14 = var13;
                        if (var12 > 0) {
                            var14 = var13;
                            if (var11 == 1) {
                                label89:
                                {
                                    Layout var16 = this.textView.getLayout();
                                    if (var16 != null) {
                                        var14 = var13;
                                        if (this.approximateLineWidth(var16, 0, var10) <= (float) (this.getMeasuredWidth() - this.getPaddingLeft() - this.getPaddingRight())) {
                                            break label89;
                                        }
                                    }

                                    var14 = false;
                                }
                            }
                        }
                    }

                    if (var14) {
                        this.textView.setTextSize(0, var10);
                        SamsungBaseTabLayout.this.checkMaxFontScale(this.textView, (int) var10);
                        this.textView.setMaxLines(var1);
                        super.onMeasure(var6, var2);
                    }
                }
            }

            if (this.customTextView == null && this.mTextParentView != null && this.textView != null && this.tab != null) {
                SamsungBaseTabLayout var17 = SamsungBaseTabLayout.this;
                if (var17.mode == 0 && var17.mDepthStyle == 2) {
                    if (var5 > 0) {
                        this.textView.measure(var5, 0);
                    } else {
                        this.textView.measure(0, 0);
                    }

                    var1 = this.textView.getMeasuredWidth();
                    ViewGroup.LayoutParams var18 = this.mTextParentView.getLayoutParams();
                    var18.width = var1 + this.getContext().getResources().getDimensionPixelSize(R.dimen.sesl_tablayout_subtab_side_space) * 2;
                    this.mTextParentView.setLayoutParams(var18);
                    super.onMeasure(MeasureSpec.makeMeasureSpec(var18.width, -2147483648), var2);
                }
            }

        }

        public boolean onTouchEvent(MotionEvent var1) {
            if (this.isEnabled()) {
                return this.tab.getCustomView() != null ? super.onTouchEvent(var1) : this.startTabTouchAnimation(var1, (KeyEvent) null);
            } else {
                return super.onTouchEvent(var1);
            }
        }

        public boolean performClick() {
            if (this.mIsCallPerformClick) {
                this.mIsCallPerformClick = false;
                return true;
            } else {
                boolean var1 = super.performClick();
                if (this.tab != null) {
                    if (!var1) {
                        this.playSoundEffect(0);
                    }

                    this.tab.select();
                    return true;
                } else {
                    return var1;
                }
            }
        }

        public void reset() {
            this.setTab((Tab) null);
            this.setSelected(false);
        }

        public void setEnabled(boolean var1) {
            super.setEnabled(var1);
            View var2 = this.mMainTabTouchBackground;
            if (var2 != null) {
                byte var3;
                if (var1) {
                    var3 = 0;
                } else {
                    var3 = 8;
                }

                var2.setVisibility(var3);
            }

        }

        public void setSelected(boolean var1) {
            if (this.isEnabled()) {
                boolean var2;
                if (this.isSelected() != var1) {
                    var2 = true;
                } else {
                    var2 = false;
                }

                super.setSelected(var1);

                TextView var3 = this.textView;
                if (var3 != null) {
                    var3.setSelected(var1);
                }

                ImageView var4 = this.iconView;
                if (var4 != null) {
                    var4.setSelected(var1);
                }

                View var5 = this.customView;
                if (var5 != null) {
                    var5.setSelected(var1);
                }

                AbsIndicatorView var6 = this.mIndicatorView;
                if (var6 != null) {
                    var6.setSelected(var1);
                }

            }
        }

        public final void showMainTabTouchBackground(int var1) {
            if (this.mMainTabTouchBackground != null && SamsungBaseTabLayout.this.mDepthStyle == 1 && SamsungBaseTabLayout.this.tabBackgroundResId == 0) {
                this.mMainTabTouchBackground.setAlpha(1.0F);
                AnimationSet var2 = new AnimationSet(true);
                var2.setFillAfter(true);
                AlphaAnimation var3;
                if (var1 != 0) {
                    if ((var1 == 1 || var1 == 3) && this.mMainTabTouchBackground.getAnimation() != null) {
                        if (this.mMainTabTouchBackground.getAnimation().hasEnded()) {
                            var3 = new AlphaAnimation(1.0F, 0.0F);
                            var3.setDuration(400L);
                            var3.setFillAfter(true);
                            var2.addAnimation(var3);
                            this.mMainTabTouchBackground.startAnimation(var2);
                        } else {
                            this.mMainTabTouchBackground.getAnimation().setAnimationListener(new Animation.AnimationListener() {
                                public void onAnimationEnd(Animation var1) {
                                    AnimationSet var2 = new AnimationSet(true);
                                    var2.setFillAfter(true);
                                    AlphaAnimation var3 = new AlphaAnimation(1.0F, 0.0F);
                                    var3.setDuration(400L);
                                    var3.setFillAfter(true);
                                    var2.addAnimation(var3);
                                    TabView.this.mMainTabTouchBackground.startAnimation(var3);
                                }

                                public void onAnimationRepeat(Animation var1) {
                                }

                                public void onAnimationStart(Animation var1) {
                                }
                            });
                        }
                    }
                } else {
                    var3 = new AlphaAnimation(0.0F, 1.0F);
                    var3.setDuration(100L);
                    var3.setFillAfter(true);
                    var2.addAnimation(var3);
                    ScaleAnimation var4 = new ScaleAnimation(0.95F, 1.0F, 0.95F, 1.0F, 1, 0.5F, 1, 0.5F);
                    var4.setDuration(350L);
                    var4.setInterpolator(SeslAnimationUtils.SINE_IN_OUT_80);
                    var4.setFillAfter(true);
                    var2.addAnimation(var4);
                    this.mMainTabTouchBackground.startAnimation(var2);
                }
            }

        }

        public final boolean startTabTouchAnimation(MotionEvent var1, KeyEvent var2) {
            if (var1 != null && this.tab.getCustomView() == null && this.textView != null && (var1 != null || var2 != null) && (var1 == null || var2 == null)) {
                int var3 = var1.getAction() & 255;
                TextView var4;
                SamsungBaseTabLayout var5;
                AbsIndicatorView var6;
                Tab var7;
                SamsungBaseTabLayout var8;
                if (var3 != 0) {
                    if (var3 != 1) {
                        if (var3 == 3) {
                            this.textView.setTypeface(SamsungBaseTabLayout.this.mNormalTypeface);
                            var5 = SamsungBaseTabLayout.this;
                            var5.startTextColorChangeAnimation(this.textView, var5.tabTextColors.getDefaultColor());
                            var6 = this.mIndicatorView;
                            if (var6 != null && !var6.isSelected()) {
                                this.mIndicatorView.setHide();
                            }

                            var5 = SamsungBaseTabLayout.this;
                            var7 = var5.getTabAt(var5.getSelectedTabPosition());
                            if (var7 != null) {
                                var4 = var7.view.textView;
                                if (var4 != null) {
                                    var4.setTypeface(SamsungBaseTabLayout.this.mBoldTypeface);
                                    var8 = SamsungBaseTabLayout.this;
                                    var8.startTextColorChangeAnimation(var7.view.textView, var8.getSelectedTabTextColor());
                                }

                                var6 = var7.view.mIndicatorView;
                                if (var6 != null) {
                                    var6.setShow();
                                }
                            }

                            if (SamsungBaseTabLayout.this.mDepthStyle == 1) {
                                this.showMainTabTouchBackground(3);
                            } else {
                                var6 = this.mIndicatorView;
                                if (var6 != null && var6.isSelected()) {
                                    this.mIndicatorView.setReleased();
                                }
                            }
                        }
                    } else {
                        this.showMainTabTouchBackground(1);
                        var6 = this.mIndicatorView;
                        if (var6 != null) {
                            var6.setReleased();
                            this.mIndicatorView.onTouchEvent(var1);
                        }

                        this.performClick();
                        this.mIsCallPerformClick = true;
                    }
                } else {
                    label73:
                    {
                        this.mIsCallPerformClick = false;
                        if (this.tab.position != SamsungBaseTabLayout.this.getSelectedTabPosition()) {
                            TextView var9 = this.textView;
                            if (var9 != null) {
                                var9.setTypeface(SamsungBaseTabLayout.this.mBoldTypeface);
                                var5 = SamsungBaseTabLayout.this;
                                var5.startTextColorChangeAnimation(this.textView, var5.getSelectedTabTextColor());
                                var6 = this.mIndicatorView;
                                if (var6 != null) {
                                    var6.setPressed();
                                }

                                var5 = SamsungBaseTabLayout.this;
                                var7 = var5.getTabAt(var5.getSelectedTabPosition());
                                if (var7 != null) {
                                    var4 = var7.view.textView;
                                    if (var4 != null) {
                                        var4.setTypeface(SamsungBaseTabLayout.this.mNormalTypeface);
                                        var8 = SamsungBaseTabLayout.this;
                                        var8.startTextColorChangeAnimation(var7.view.textView, var8.tabTextColors.getDefaultColor());
                                    }

                                    var6 = var7.view.mIndicatorView;
                                    if (var6 != null) {
                                        var6.setHide();
                                    }
                                }
                                break label73;
                            }
                        }

                        if (this.tab.position == SamsungBaseTabLayout.this.getSelectedTabPosition()) {
                            var6 = this.mIndicatorView;
                            if (var6 != null) {
                                var6.setPressed();
                            }
                        }
                    }

                    this.showMainTabTouchBackground(0);
                }

                return super.onTouchEvent(var1);
            } else {
                return false;
            }
        }

        @SuppressLint({"RestrictedApi", "UnsafeExperimentalUsageError"})
        public final void tryAttachBadgeToAnchor(View var1) {
            if (this.hasBadgeDrawable()) {
                if (var1 != null) {
                    this.setClipChildren(false);
                    this.setClipToPadding(false);
                    BadgeUtils.attachBadgeDrawable(this.badgeDrawable, var1, this.getCustomParentForBadge(var1));
                    this.badgeAnchorView = var1;
                }

            }
        }

        @SuppressLint({"RestrictedApi", "UnsafeExperimentalUsageError"})
        public final void tryRemoveBadgeFromAnchor() {
            if (this.hasBadgeDrawable()) {
                if (this.badgeAnchorView != null) {
                    this.setClipChildren(true);
                    this.setClipToPadding(true);
                    BadgeDrawable var1 = this.badgeDrawable;
                    View var2 = this.badgeAnchorView;
                    var2.getOverlay().remove(var1);
                    this.badgeAnchorView = null;
                }

            }
        }

        public final void tryUpdateBadgeAnchor() {
            if (this.hasBadgeDrawable()) {
                if (this.customView != null) {
                    this.tryRemoveBadgeFromAnchor();
                } else {
                    Tab var1;
                    View var3;
                    if (this.iconView != null) {
                        var1 = this.tab;
                        if (var1 != null && var1.getIcon() != null) {
                            var3 = this.badgeAnchorView;
                            ImageView var4 = this.iconView;
                            if (var3 != var4) {
                                this.tryRemoveBadgeFromAnchor();
                                this.tryAttachBadgeToAnchor(this.iconView);
                            } else {
                                this.tryUpdateBadgeDrawableBounds(var4);
                            }

                            return;
                        }
                    }

                    if (this.textView != null) {
                        var1 = this.tab;
                        if (var1 != null && var1.getTabLabelVisibility() == 1) {
                            var3 = this.badgeAnchorView;
                            TextView var2 = this.textView;
                            if (var3 != var2) {
                                this.tryRemoveBadgeFromAnchor();
                                this.tryAttachBadgeToAnchor(this.textView);
                            } else {
                                this.tryUpdateBadgeDrawableBounds(var2);
                            }

                            return;
                        }
                    }

                    this.tryRemoveBadgeFromAnchor();
                }

            }
        }

        @SuppressLint({"RestrictedApi", "UnsafeExperimentalUsageError"})
        public final void tryUpdateBadgeDrawableBounds(View var1) {
            if (this.hasBadgeDrawable() && var1 == this.badgeAnchorView) {
                BadgeUtils.setBadgeDrawableBounds(this.badgeDrawable, var1, this.getCustomParentForBadge(var1));
            }

        }

        @SuppressLint("WrongConstant")
        public final void update() {
            RelativeLayout relativeLayout;
            int i;
            RelativeLayout relativeLayout2;
            Tab tab2 = this.tab;
            Drawable drawable = null;
            View customView2 = tab2 != null ? tab2.getCustomView() : null;
            if (customView2 != null) {
                ViewParent parent = customView2.getParent();
                if (parent != this) {
                    if (parent != null) {
                        ((ViewGroup) parent).removeView(customView2);
                    }
                    addView(customView2);
                }
                this.customView = customView2;
                TextView textView2 = this.textView;
                if (textView2 != null) {
                    textView2.setVisibility(8);
                }
                ImageView imageView = this.iconView;
                if (imageView != null) {
                    imageView.setVisibility(8);
                    this.iconView.setImageDrawable((Drawable) null);
                }
                this.customTextView = (TextView) customView2.findViewById(android.R.id.text1);
                TextView textView3 = this.customTextView;
                if (textView3 != null) {
                    this.defaultMaxLines = TextViewCompat.getMaxLines(textView3);
                }
                this.customIconView = (ImageView) customView2.findViewById(android.R.id.icon);
            } else {
                View view = this.customView;
                if (view != null) {
                    removeView(view);
                    this.customView = null;
                }
                this.customTextView = null;
                this.customIconView = null;
            }
            boolean z = false;
            if (this.customView == null) {
                if (this.textView == null) {
                    Context context = getContext();
                    int i2 = -2;
                    if (SamsungBaseTabLayout.this.mDepthStyle == 2) {
                        relativeLayout2 = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.sesl_tabs_sub_tab_layout, this, false);
                        if (SamsungBaseTabLayout.this.mode != 0) {
                            i2 = -1;
                        }
                        i = SamsungBaseTabLayout.this.mSubTabIndicatorHeight;
                        this.mIndicatorView = (AbsIndicatorView) relativeLayout2.findViewById(R.id.indicator);
                        if (!(this.mIndicatorView == null || SamsungBaseTabLayout.this.mSubTabSelectedIndicatorColor == -1)) {
                            this.mIndicatorView.setSelectedIndicatorColor(SamsungBaseTabLayout.this.mSubTabSelectedIndicatorColor);
                        }
                    } else {
                        RelativeLayout relativeLayout3 = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.sesl_tabs_main_tab_layout, this, false);
                        if (this.tab.icon == null) {
                            i2 = -1;
                        }
                        this.mIndicatorView = (AbsIndicatorView) relativeLayout3.findViewById(R.id.indicator);
                        AbsIndicatorView AbsIndicatorView = this.mIndicatorView;
                        if (AbsIndicatorView != null) {
                            AbsIndicatorView.setSelectedIndicatorColor(SamsungBaseTabLayout.this.mTabSelectedIndicatorColor);
                        }
                        this.mMainTabTouchBackground = relativeLayout3.findViewById(R.id.main_tab_touch_background);
                        if (this.mMainTabTouchBackground != null && this.tab.icon == null) {
                            ViewCompat.setBackground(this.mMainTabTouchBackground, ContextCompat.getDrawable(context, R.drawable.sesl_tablayout_maintab_touch_background));
                            this.mMainTabTouchBackground.setAlpha(0.0f);
                        }
                        relativeLayout2 = relativeLayout3;
                        i = -1;
                    }
                    relativeLayout2.getLayoutParams().width = i2;
                    addView(relativeLayout2, i2, i);
                    this.textView = (TextView) relativeLayout2.findViewById(R.id.title);
                    this.mTextParentView = relativeLayout2;
                    this.defaultMaxLines = TextViewCompat.getMaxLines(this.textView);
                }
                if (this.iconView == null && (relativeLayout = this.mTextParentView) != null) {
                    this.iconView = (ImageView) relativeLayout.findViewById(R.id.icon);
                }
                if (!(tab2 == null || tab2.getIcon() == null)) {
                    drawable = DrawableCompat.wrap(tab2.getIcon()).mutate();
                }
                if (drawable != null) {
                    DrawableCompat.setTintList(drawable, SamsungBaseTabLayout.this.tabIconTint);
                    PorterDuff.Mode mode = SamsungBaseTabLayout.this.tabIconTintMode;
                    if (mode != null) {
                        DrawableCompat.setTintMode(drawable, mode);
                    }
                }
                TextViewCompat.setTextAppearance(this.textView, SamsungBaseTabLayout.this.tabTextAppearance);
                SamsungBaseTabLayout tabLayout = SamsungBaseTabLayout.this;
                tabLayout.checkMaxFontScale(this.textView, (int) tabLayout.tabTextSize);
                ColorStateList colorStateList = SamsungBaseTabLayout.this.tabTextColors;
                if (colorStateList != null) {
                    this.textView.setTextColor(colorStateList);
                }
                updateTextAndIcon(this.textView, this.iconView);
                tryUpdateBadgeAnchor();
                addOnLayoutChangeListener(this.iconView);
                addOnLayoutChangeListener(this.textView);
            } else if (!(this.customTextView == null && this.customIconView == null)) {
                updateTextAndIcon(this.customTextView, this.customIconView);
            }
            if (tab2 != null && !TextUtils.isEmpty(tab2.contentDesc)) {
                setContentDescription(tab2.contentDesc);
            }
            if (tab2 != null && tab2.isSelected()) {
                z = true;
            }
            setSelected(z);
        }

        public final void updateBackgroundDrawable(Context var1) {
            SamsungBaseTabLayout var2 = SamsungBaseTabLayout.this;
            if (var2.tabBackgroundResId != 0 && var2.mDepthStyle != 2) {
                this.baseBackgroundDrawable = AppCompatResources.getDrawable(var1, SamsungBaseTabLayout.this.tabBackgroundResId);
                Drawable var3 = this.baseBackgroundDrawable;
                if (var3 != null && var3.isStateful()) {
                    this.baseBackgroundDrawable.setState(this.getDrawableState());
                }

                ViewCompat.setBackground(this, this.baseBackgroundDrawable);
            } else {
                this.baseBackgroundDrawable = null;
            }

        }

        public final void updateOrientation() {
            this.setOrientation(SamsungBaseTabLayout.this.inlineLabel ? HORIZONTAL : VERTICAL);
            if (this.customTextView == null && this.customIconView == null) {
                this.updateTextAndIcon(this.textView, this.iconView);
            } else {
                this.updateTextAndIcon(this.customTextView, this.customIconView);
            }

        }

        @SuppressLint({"RestrictedApi", "WrongConstant"})
        public final void updateTextAndIcon(TextView var1, ImageView var2) {
            Tab var3 = this.tab;
            Drawable var10;
            if (var3 != null && var3.getIcon() != null) {
                var10 = DrawableCompat.wrap(this.tab.getIcon()).mutate();
            } else {
                var10 = null;
            }

            Tab var4 = this.tab;
            CharSequence var11;
            if (var4 != null) {
                var11 = var4.getText();
            } else {
                var11 = null;
            }

            if (var2 != null) {
                if (var10 != null) {
                    var2.setImageDrawable(var10);
                    var2.setVisibility(0);
                    this.setVisibility(0);
                } else {
                    var2.setVisibility(8);
                    var2.setImageDrawable((Drawable) null);
                }
            }

            boolean var5 = !TextUtils.isEmpty(var11);
            if (var1 != null) {
                if (var5) {
                    var1.setText(var11);
                    if (this.tab.labelVisibilityMode == 1) {
                        var1.setVisibility(0);
                    } else {
                        var1.setVisibility(8);
                    }

                    this.setVisibility(0);
                } else {
                    var1.setVisibility(8);
                    var1.setText((CharSequence) null);
                }
            }

            if (var2 != null) {
                MarginLayoutParams var12 = (MarginLayoutParams) var2.getLayoutParams();
                int var6;
                if (var5 && var2.getVisibility() == 0) {
                    if (SamsungBaseTabLayout.this.mIconTextGap != -1) {
                        var6 = SamsungBaseTabLayout.this.mIconTextGap;
                    } else {
                        var6 = (int) ViewUtils.dpToPx(this.getContext(), 8);
                    }
                } else {
                    var6 = 0;
                }

                if (var6 != MarginLayoutParamsCompat.getMarginEnd(var12)) {
                    MarginLayoutParamsCompat.setMarginEnd(var12, var6);
                    var12.bottomMargin = 0;
                    var2.setLayoutParams(var12);
                    var2.requestLayout();
                    if (var1 != null) {
                        RelativeLayout.LayoutParams var9 = (RelativeLayout.LayoutParams) var1.getLayoutParams();
                        var9.addRule(13, 0);
                        var9.addRule(15, 1);
                        var9.addRule(17, R.id.icon);
                        var1.setLayoutParams(var9);
                    }
                }
            }

            Tab var7 = this.tab;
            CharSequence var8;
            if (var7 != null) {
                var8 = var7.contentDesc;
            } else {
                var8 = null;
            }

            if (var5) {
                var8 = null;
            }

            TooltipCompat.setTooltipText(this, var8);
        }
    }
}
