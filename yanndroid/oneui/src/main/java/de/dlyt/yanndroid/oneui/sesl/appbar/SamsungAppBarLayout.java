package de.dlyt.yanndroid.oneui.sesl.appbar;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;

import androidx.core.math.MathUtils;
import androidx.core.util.ObjectsCompat;
import androidx.core.view.MotionEventCompat;
import androidx.core.view.NestedScrollingChild;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.customview.view.AbsSavedState;

import com.google.android.material.internal.ContextUtils;
import com.google.android.material.internal.ThemeEnforcement;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import de.dlyt.yanndroid.oneui.R;
import de.dlyt.yanndroid.oneui.layout.CoordinatorLayout;
import de.dlyt.yanndroid.oneui.sesl.utils.ABLBehavior;

@CoordinatorLayout.DefaultBehavior(SamsungAppBarLayout.Behavior.class)
public class SamsungAppBarLayout extends LinearLayout implements ABLBehavior {
    private boolean mIsOneUI4;
    public static final Interpolator SINE_OUT_80_INTERPOLATOR = new PathInterpolator(0.17F, 0.17F, 0.2F, 1.0F);
    public static float mAppBarHeight;
    public boolean liftOnScroll;
    public WeakReference<View> liftOnScrollTargetView;
    public int liftOnScrollTargetViewId;
    public Drawable mBackground;
    public int mBottomPadding;
    public int mCurrentOrientation;
    public int mDownPreScrollRange;
    public int mDownScrollRange;
    public boolean mHaveChildWithInterpolator;
    public float mHeightCustom;
    public float mHeightPercent;
    public boolean mIsSetCollapsedHeight;
    public WindowInsetsCompat mLastInsets;
    public boolean mLiftable;
    public boolean mLiftableOverride;
    public boolean mLifted;
    public List<SamsungAppBarLayout.BaseOnOffsetChangedListener> mListeners;
    public int mPendingAction;
    public int[] mTmpStatesArray;
    public int mTotalScrollRange;

    public SamsungAppBarLayout(Context var1) {
        this(var1, (AttributeSet) null);
    }

    public SamsungAppBarLayout(Context var1, AttributeSet var2) {
        this(var1, var2, 0);
    }

    @SuppressLint({"WrongConstant", "RestrictedApi"})
    public SamsungAppBarLayout(Context var1, AttributeSet var2, int var3) {
        super(var1, var2, var3);

        mIsOneUI4 = var1.getTheme().obtainStyledAttributes(new int[]{R.attr.isOneUI4}).getBoolean(0, false);

        this.mTotalScrollRange = -1;
        this.mDownPreScrollRange = -1;
        this.mDownScrollRange = -1;
        this.mPendingAction = 0;
        this.mLifted = false;
        this.mHeightCustom = 0.0F;
        this.mHeightPercent = 0.0F;
        this.mBottomPadding = 0;
        this.mIsSetCollapsedHeight = false;
        this.setOrientation(1);
        if (Build.VERSION.SDK_INT >= 21) {
            ViewUtilsLollipop.setStateListAnimatorFromAttrs(this, var2, var3, R.style.AppBarLayoutStyle);
        }

        TypedArray var4 = ThemeEnforcement.obtainStyledAttributes(var1, var2, R.styleable.SamsungAppBarLayout, var3, R.style.AppBarLayoutStyle, new int[0]);
        if (var4.hasValue(R.styleable.SamsungAppBarLayout_android_background)) {
            this.mBackground = var4.getDrawable(R.styleable.SamsungAppBarLayout_android_background);
            ViewCompat.setBackground(this, this.mBackground);
        } else {
            this.mBackground = null;
            this.setBackgroundColor(this.getResources().getColor(mIsOneUI4 ? R.color.sesl4_action_bar_background_color : R.color.sesl_action_bar_background_color, var1.getTheme()));
        }

        if (var4.hasValue(R.styleable.SamsungAppBarLayout_expanded)) {
            this.setExpanded(var4.getBoolean(R.styleable.SamsungAppBarLayout_expanded, false), false, false);
        }

        if (Build.VERSION.SDK_INT >= 21 && var4.hasValue(R.styleable.SamsungAppBarLayout_elevation)) {
            ViewUtilsLollipop.setDefaultAppBarLayoutStateListAnimator(this, (float) var4.getDimensionPixelSize(R.styleable.SamsungAppBarLayout_elevation, 0));
        }

        if (var4.hasValue(R.styleable.SamsungAppBarLayout_sesl_layout_heightPercent)) {
            this.mHeightCustom = var4.getFloat(R.styleable.SamsungAppBarLayout_sesl_layout_heightPercent, 0.3967F);
        } else {
            this.mHeightCustom = 0.3967F;
        }

        TypedValue var5 = new TypedValue();
        this.getResources().getValue(mIsOneUI4 ? R.dimen.sesl4_appbar_height_proportion : R.dimen.sesl_appbar_height_proportion, var5, true);
        this.mHeightPercent = var5.getFloat();
        if (!mIsOneUI4 && var4.hasValue(R.styleable.SamsungAppBarLayout_android_paddingBottom)) {
            this.mBottomPadding = var4.getDimensionPixelSize(R.styleable.SamsungAppBarLayout_android_paddingBottom, 0);
            this.setPadding(0, 0, 0, this.mBottomPadding);
        } else {
            this.mBottomPadding = 0;
        }

        if (Build.VERSION.SDK_INT >= 26) {
            if (var4.hasValue(R.styleable.SamsungAppBarLayout_android_keyboardNavigationCluster)) {
                this.setKeyboardNavigationCluster(var4.getBoolean(R.styleable.SamsungAppBarLayout_android_keyboardNavigationCluster, false));
            }

            if (var4.hasValue(R.styleable.SamsungAppBarLayout_android_touchscreenBlocksFocus)) {
                this.setTouchscreenBlocksFocus(var4.getBoolean(R.styleable.SamsungAppBarLayout_android_touchscreenBlocksFocus, false));
            }
        }

        this.liftOnScroll = var4.getBoolean(R.styleable.SamsungAppBarLayout_liftOnScroll, false);
        this.liftOnScrollTargetViewId = var4.getResourceId(R.styleable.SamsungAppBarLayout_liftOnScrollTargetViewId, -1);
        var4.recycle();
        if (this.mBottomPadding > 0) {
            mAppBarHeight = (float) this.getResources().getDimensionPixelSize(R.dimen.sesl_action_bar_height_with_padding);
        } else {
            mAppBarHeight = (float) this.getResources().getDimensionPixelSize(mIsOneUI4 ? R.dimen.sesl4_action_bar_height_with_padding : R.dimen.sesl_action_bar_default_height);
        }

        ViewCompat.setOnApplyWindowInsetsListener(this, new androidx.core.view.OnApplyWindowInsetsListener() {
            public WindowInsetsCompat onApplyWindowInsets(View var1, WindowInsetsCompat var2) {
                return SamsungAppBarLayout.this.onWindowInsetChanged(var2);
            }
        });
        this.mCurrentOrientation = this.getContext().getResources().getConfiguration().orientation;
    }

    private int getWindowHeight() {
        return this.getResources().getDisplayMetrics().heightPixels;
    }

    public void addOnOffsetChangedListener(SamsungAppBarLayout.BaseOnOffsetChangedListener var1) {
        if (this.mListeners == null) {
            this.mListeners = new ArrayList();
        }

        if (var1 != null && !this.mListeners.contains(var1)) {
            this.mListeners.add(var1);
        }

    }

    public void addOnOffsetChangedListener(SamsungAppBarLayout.OnOffsetChangedListener var1) {
        this.addOnOffsetChangedListener((SamsungAppBarLayout.BaseOnOffsetChangedListener) var1);
    }

    public boolean checkLayoutParams(android.view.ViewGroup.LayoutParams var1) {
        return var1 instanceof SamsungAppBarLayout.LayoutParams;
    }

    public final void clearLiftOnScrollTargetView() {
        WeakReference var1 = this.liftOnScrollTargetView;
        if (var1 != null) {
            var1.clear();
        }

        this.liftOnScrollTargetView = null;
    }

    public boolean dispatchGenericMotionEvent(MotionEvent var1) {
        if (var1.getAction() == 8) {
            if (this.liftOnScrollTargetView != null) {
                if (var1.getAxisValue(9) < 0.0F) {
                    this.setExpanded(false);
                } else if (var1.getAxisValue(9) > 0.0F && !this.canScrollVertically(-1)) {
                    this.setExpanded(true);
                }
            } else if (var1.getAxisValue(9) < 0.0F) {
                this.setExpanded(false);
            } else if (var1.getAxisValue(9) > 0.0F) {
                this.setExpanded(true);
            }
        }

        return super.dispatchGenericMotionEvent(var1);
    }

    public void dispatchOffsetUpdates(int var1) {
        List var2 = this.mListeners;
        if (var2 != null) {
            int var3 = 0;

            for (int var4 = var2.size(); var3 < var4; ++var3) {
                SamsungAppBarLayout.BaseOnOffsetChangedListener var5 = (SamsungAppBarLayout.BaseOnOffsetChangedListener) this.mListeners.get(var3);
                if (var5 != null) {
                    var5.onOffsetChanged(this, var1);
                }
            }
        }

    }

    public final Activity findActivityOfContext(Context var1) {
        Activity var2 = null;

        while (var2 == null && var1 != null) {
            if (var1 instanceof Activity) {
                var2 = (Activity) var1;
            } else if (var1 instanceof ContextWrapper) {
                var1 = ((ContextWrapper) var1).getBaseContext();
            } else {
                var1 = null;
            }
        }

        return var2;
    }

    public final View findLiftOnScrollTargetView() {
        WeakReference var1 = this.liftOnScrollTargetView;
        Object var2 = null;
        View var5;
        if (var1 == null && this.liftOnScrollTargetViewId != -1) {
            @SuppressLint("RestrictedApi") Activity var4 = ContextUtils.getActivity(this.getContext());
            if (var4 != null) {
                var5 = var4.findViewById(this.liftOnScrollTargetViewId);
            } else if (this.getParent() instanceof ViewGroup) {
                var5 = ((ViewGroup) this.getParent()).findViewById(this.liftOnScrollTargetViewId);
            } else {
                var5 = null;
            }

            if (var5 != null) {
                this.liftOnScrollTargetView = new WeakReference(var5);
            }
        }

        WeakReference var3 = this.liftOnScrollTargetView;
        var5 = (View) var2;
        if (var3 != null) {
            var5 = (View) var3.get();
        }

        return var5;
    }

    public SamsungAppBarLayout.LayoutParams generateDefaultLayoutParams() {
        return new SamsungAppBarLayout.LayoutParams(-1, -2);
    }

    public SamsungAppBarLayout.LayoutParams generateLayoutParams(AttributeSet var1) {
        return new SamsungAppBarLayout.LayoutParams(this.getContext(), var1);
    }

    public SamsungAppBarLayout.LayoutParams generateLayoutParams(android.view.ViewGroup.LayoutParams var1) {
        if (Build.VERSION.SDK_INT >= 19 && var1 instanceof android.widget.LinearLayout.LayoutParams) {
            return new SamsungAppBarLayout.LayoutParams((android.widget.LinearLayout.LayoutParams) var1);
        } else {
            return var1 instanceof MarginLayoutParams ? new SamsungAppBarLayout.LayoutParams((MarginLayoutParams) var1) : new SamsungAppBarLayout.LayoutParams(var1);
        }
    }

    public float getCollapsedHeight() {
        return mAppBarHeight;
    }

    public void setCollapsedHeight(float var1) {
        StringBuilder var2 = new StringBuilder();
        var2.append("setCollapsedHeight: height :");
        var2.append(var1);
        Log.d("Sesl_AppBarLayout", var2.toString());
        this.mIsSetCollapsedHeight = true;
        mAppBarHeight = var1;
    }

    public int getDownNestedPreScrollRange() {
        int var1 = this.mDownPreScrollRange;
        if (var1 != -1) {
            return var1;
        } else {
            int var2 = this.getChildCount() - 1;

            int var3;
            for (var3 = 0; var2 >= 0; var3 = var1) {
                View var4 = this.getChildAt(var2);
                SamsungAppBarLayout.LayoutParams var5 = (SamsungAppBarLayout.LayoutParams) var4.getLayoutParams();
                int var6 = var4.getMeasuredHeight();
                var1 = var5.scrollFlags;
                if ((var1 & 5) == 5) {
                    var3 += var5.topMargin + var5.bottomMargin;
                    if ((var1 & 8) != 0) {
                        var1 = var3 + ViewCompat.getMinimumHeight(var4);
                    } else {
                        if ((var1 & 2) != 0) {
                            var1 = ViewCompat.getMinimumHeight(var4);
                        } else {
                            var1 = this.getTopInset();
                        }

                        var1 = var3 + (var6 - var1);
                    }
                } else {
                    var1 = var3;
                    if (var3 > 0) {
                        break;
                    }
                }

                --var2;
            }

            var1 = Math.max(0, var3);
            this.mDownPreScrollRange = var1;
            return var1;
        }
    }

    public int getDownNestedScrollRange() {
        int var1 = this.mDownScrollRange;
        if (var1 != -1) {
            return var1;
        } else {
            int var2 = this.getChildCount();
            int var3 = 0;
            var1 = var3;

            int var4;
            while (true) {
                var4 = var1;
                if (var3 >= var2) {
                    break;
                }

                View var5 = this.getChildAt(var3);
                SamsungAppBarLayout.LayoutParams var6 = (SamsungAppBarLayout.LayoutParams) var5.getLayoutParams();
                int var7 = var5.getMeasuredHeight();
                int var8 = var6.topMargin;
                int var9 = var6.bottomMargin;
                int var10 = var6.scrollFlags;
                var4 = var1;
                if ((var10 & 1) == 0) {
                    break;
                }

                var1 += var7 + var8 + var9;
                if ((var10 & 2) != 0) {
                    var4 = var1 - (ViewCompat.getMinimumHeight(var5) + this.getTopInset());
                    break;
                }

                ++var3;
            }

            var1 = Math.max(0, var4);
            this.mDownScrollRange = var1;
            return var1;
        }
    }

    public int getLiftOnScrollTargetViewId() {
        return this.liftOnScrollTargetViewId;
    }

    public void setLiftOnScrollTargetViewId(int var1) {
        this.liftOnScrollTargetViewId = var1;
        this.clearLiftOnScrollTargetView();
    }

    public final int getMinimumHeightForVisibleOverlappingContent() {
        int var1 = this.getTopInset();
        int var2 = ViewCompat.getMinimumHeight(this);
        if (var2 == 0) {
            var2 = this.getChildCount();
            if (var2 >= 1) {
                var2 = ViewCompat.getMinimumHeight(this.getChildAt(var2 - 1));
            } else {
                var2 = 0;
            }

            if (var2 == 0) {
                return this.getHeight() / 3;
            }
        }

        return var2 * 2 + var1;
    }

    public int getPendingAction() {
        return this.mPendingAction;
    }

    @Deprecated
    public float getTargetElevation() {
        return 0.0F;
    }

    @Deprecated
    public void setTargetElevation(float var1) {
        if (Build.VERSION.SDK_INT >= 21) {
            ViewUtilsLollipop.setDefaultAppBarLayoutStateListAnimator(this, var1);
        }

    }

    public final int getTopInset() {
        WindowInsetsCompat var1 = this.mLastInsets;
        int var2;
        if (var1 != null) {
            var2 = var1.getSystemWindowInsetTop();
        } else {
            var2 = 0;
        }

        return var2;
    }

    public final int getTotalScrollRange() {
        int var1 = this.mTotalScrollRange;
        if (var1 != -1) {
            return var1;
        } else {
            int var2 = this.getChildCount();
            int var3 = 0;
            var1 = var3;

            int var4;
            while (true) {
                var4 = var1;
                if (var3 >= var2) {
                    break;
                }

                View var5 = this.getChildAt(var3);
                SamsungAppBarLayout.LayoutParams var6 = (SamsungAppBarLayout.LayoutParams) var5.getLayoutParams();
                int var7 = var5.getMeasuredHeight();
                int var8 = var6.scrollFlags;
                var4 = var1;
                if ((var8 & 1) == 0) {
                    break;
                }

                var1 += var7 + var6.topMargin + var6.bottomMargin;
                if ((var8 & 2) != 0) {
                    var4 = var1 - ViewCompat.getMinimumHeight(var5);
                    break;
                }

                ++var3;
            }

            var1 = Math.max(0, var4 - this.getTopInset());
            this.mTotalScrollRange = var1;
            return var1;
        }
    }

    public int getUpNestedPreScrollRange() {
        return this.getTotalScrollRange();
    }

    public boolean hasChildWithInterpolator() {
        return this.mHaveChildWithInterpolator;
    }

    public final boolean hasCollapsibleChild() {
        int var1 = this.getChildCount();

        for (int var2 = 0; var2 < var1; ++var2) {
            if (((SamsungAppBarLayout.LayoutParams) this.getChildAt(var2).getLayoutParams()).isCollapsible()) {
                return true;
            }
        }

        return false;
    }

    public boolean hasScrollableChildren() {
        boolean var1;
        if (this.getTotalScrollRange() != 0) {
            var1 = true;
        } else {
            var1 = false;
        }

        return var1;
    }

    public final void invalidateScrollRanges() {
        this.mTotalScrollRange = -1;
        this.mDownPreScrollRange = -1;
        this.mDownScrollRange = -1;
    }

    public boolean isCollapsed() {
        return this.mLifted;
    }

    public boolean isLiftOnScroll() {
        return this.liftOnScroll;
    }

    public void setLiftOnScroll(boolean var1) {
        this.liftOnScroll = var1;
    }

    public final boolean isLightTheme() {
        TypedValue var1 = new TypedValue();
        Resources.Theme var2 = this.getContext().getTheme();
        int var3 = R.attr.isLightTheme;
        boolean var4 = true;
        var2.resolveAttribute(var3, var1, true);
        if (var1.data == 0) {
            var4 = false;
        }

        return var4;
    }

    public void onConfigurationChanged(Configuration var1) {
        super.onConfigurationChanged(var1);
        Drawable var2 = this.mBackground;
        if (var2 != null) {
            if (var2 == this.getBackground()) {
                this.setBackground(this.mBackground);
            } else {
                this.setBackground(this.getBackground());
            }
        } else if (this.getBackground() != null) {
            this.mBackground = this.getBackground();
            this.setBackground(this.mBackground);
        } else {
            this.mBackground = null;
            this.setBackgroundColor(this.getResources().getColor(mIsOneUI4 ? R.color.sesl4_action_bar_background_color : R.color.sesl_action_bar_background_color, getContext().getTheme()));
        }

        this.mBottomPadding = mIsOneUI4 ? 0 : this.getContext().getResources().getDimensionPixelSize(R.dimen.sesl_extended_appbar_bottom_padding);
        this.setPadding(0, 0, 0, this.mBottomPadding);
        if (this.mBottomPadding > 0) {
            mAppBarHeight = (float) this.getResources().getDimensionPixelSize(R.dimen.sesl_action_bar_height_with_padding);
        } else {
            mAppBarHeight = (float) this.getResources().getDimensionPixelSize(mIsOneUI4 ? R.dimen.sesl4_action_bar_height_with_padding : R.dimen.sesl_action_bar_default_height);
        }

        TypedValue var3 = new TypedValue();
        this.getResources().getValue(mIsOneUI4 ? R.dimen.sesl4_appbar_height_proportion : R.dimen.sesl_appbar_height_proportion, var3, true);
        this.mHeightPercent = var3.getFloat();
        if (this.mHeightCustom > 0.0F) {
            Log.d("Sesl_AppBarLayout", "onConfigurationChanged");
            this.updateInternalHeight();
        }

        if (this.mLifted || this.mCurrentOrientation == 1 && var1.orientation == 2) {
            this.setExpanded(false, false, true);
        } else {
            this.setExpanded(true, false, true);
        }

        this.mCurrentOrientation = var1.orientation;
    }

    public int[] onCreateDrawableState(int var1) {
        if (this.mTmpStatesArray == null) {
            this.mTmpStatesArray = new int[4];
        }

        int[] var2 = this.mTmpStatesArray;
        int[] var3 = super.onCreateDrawableState(var1 + var2.length);
        if (this.mLiftable) {
            var1 = R.attr.state_liftable;
        } else {
            var1 = -R.attr.state_liftable;
        }

        var2[0] = var1;
        if (this.mLiftable && this.mLifted) {
            var1 = R.attr.state_lifted;
        } else {
            var1 = -R.attr.state_lifted;
        }

        var2[1] = var1;
        if (this.mLiftable) {
            var1 = R.attr.state_collapsible;
        } else {
            var1 = -R.attr.state_collapsible;
        }

        var2[2] = var1;
        if (this.mLiftable && this.mLifted) {
            var1 = R.attr.state_collapsed;
        } else {
            var1 = -R.attr.state_collapsed;
        }

        var2[3] = var1;
        return LinearLayout.mergeDrawableStates(var3, var2);
    }

    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.clearLiftOnScrollTargetView();
    }

    public void onLayout(boolean var1, int var2, int var3, int var4, int var5) {
        super.onLayout(var1, var2, var3, var4, var5);
        this.invalidateScrollRanges();
        var1 = false;
        this.mHaveChildWithInterpolator = false;
        var3 = this.getChildCount();

        for (var2 = 0; var2 < var3; ++var2) {
            if (((SamsungAppBarLayout.LayoutParams) this.getChildAt(var2).getLayoutParams()).getScrollInterpolator() != null) {
                this.mHaveChildWithInterpolator = true;
                break;
            }
        }

        if (!this.mLiftableOverride) {
            if (this.liftOnScroll || this.hasCollapsibleChild()) {
                var1 = true;
            }

            this.setLiftableState(var1);
        }

    }

    public void onMeasure(int var1, int var2) {
        if (!this.mIsSetCollapsedHeight) {
            if (this.mBottomPadding > 0) {
                mAppBarHeight = (float) this.getResources().getDimensionPixelSize(R.dimen.sesl_action_bar_height_with_padding);
            } else {
                mAppBarHeight = (float) this.getResources().getDimensionPixelSize(mIsOneUI4 ? R.dimen.sesl4_action_bar_height_with_padding : R.dimen.sesl_action_bar_default_height);
            }
        }

        if (this.mHeightCustom > 0.0F) {
            this.updateInternalHeight();
        }

        super.onMeasure(var1, var2);
        this.invalidateScrollRanges();
    }

    public WindowInsetsCompat onWindowInsetChanged(WindowInsetsCompat var1) {
        WindowInsetsCompat var2;
        if (ViewCompat.getFitsSystemWindows(this)) {
            var2 = var1;
        } else {
            var2 = null;
        }

        if (!ObjectsCompat.equals(this.mLastInsets, var2)) {
            this.mLastInsets = var2;
            this.invalidateScrollRanges();
        }

        return var1;
    }

    public void removeOnOffsetChangedListener(SamsungAppBarLayout.BaseOnOffsetChangedListener var1) {
        List var2 = this.mListeners;
        if (var2 != null && var1 != null) {
            var2.remove(var1);
        }

    }

    public void removeOnOffsetChangedListener(SamsungAppBarLayout.OnOffsetChangedListener var1) {
        this.removeOnOffsetChangedListener((SamsungAppBarLayout.BaseOnOffsetChangedListener) var1);
    }

    public void resetPendingAction() {
        this.mPendingAction = 0;
    }

    public boolean seslIsCollapsed() {
        return this.mLifted;
    }

    public void seslSetExpanded(boolean var1) {
        this.setExpanded(var1);
    }

    public void setExpanded(boolean var1) {
        this.setExpanded(var1, ViewCompat.isLaidOut(this));
    }

    public void setExpanded(boolean var1, boolean var2) {
        this.setExpanded(var1, var2, true);
    }

    public final void setExpanded(boolean var1, boolean var2, boolean var3) {
        this.setLifted(var1 ^ true);
        byte var4;
        if (var1) {
            var4 = 1;
        } else {
            var4 = 2;
        }

        byte var5 = 0;
        byte var6;
        if (var2) {
            var6 = 4;
        } else {
            var6 = 0;
        }

        if (var3) {
            var5 = 8;
        }

        this.mPendingAction = var4 | var6 | var5;
        this.requestLayout();
    }

    public final boolean setLiftableState(boolean var1) {
        if (this.mLiftable != var1) {
            this.mLiftable = var1;
            this.refreshDrawableState();
            return true;
        } else {
            return false;
        }
    }

    public boolean setLifted(boolean var1) {
        return this.setLiftedState(var1);
    }

    public boolean setLiftedState(boolean var1) {
        if (this.mLifted != var1) {
            this.mLifted = var1;
            this.mLifted = var1;
            this.refreshDrawableState();
            return true;
        } else {
            return false;
        }
    }

    public void setOrientation(int var1) {
        if (var1 == 1) {
            super.setOrientation(var1);
        } else {
            throw new IllegalArgumentException("AppBarLayout is always vertical and does not support horizontal orientation");
        }
    }

    public boolean shouldLift(View var1) {
        View var2 = this.findLiftOnScrollTargetView();
        if (var2 != null) {
            var1 = var2;
        }

        boolean var3;
        if (var1 == null || !var1.canScrollVertically(-1) && var1.getScrollY() <= 0) {
            var3 = false;
        } else {
            var3 = true;
        }

        return var3;
    }

    public final void updateInternalHeight() {
        int var1 = this.getWindowHeight();
        float var2 = (float) var1 * this.mHeightPercent;
        float var3 = var2;
        if (var2 == 0.0F) {
            var3 = mAppBarHeight;
        }

        Activity var4 = this.findActivityOfContext(this.getContext());
        StringBuilder var5 = new StringBuilder();
        var5.append("updateInternalHeight: context:");
        var5.append(this.getContext());
        var5.append(", orientation:");
        var5.append(this.getContext().getResources().getConfiguration().orientation);
        var5.append(" density:");
        var5.append(this.getContext().getResources().getConfiguration().densityDpi);
        var5.append(" ,mHeightPercent");
        var5.append(this.mHeightPercent);
        var5.append(" windowHeight:");
        var5.append(var1);
        var5.append(" activity:");
        var5.append(var4);
        Log.d("Sesl_AppBarLayout", var5.toString());

        CoordinatorLayout.LayoutParams var7;
        try {
            var7 = (CoordinatorLayout.LayoutParams) this.getLayoutParams();
        } catch (ClassCastException var6) {
            Log.e("Sesl_AppBarLayout", Log.getStackTraceString(var6));
            var7 = null;
        }

        if (var7 != null) {
            var7.height = (int) var3;
            var5 = new StringBuilder();
            var5.append("updateInternalHeight: LayoutParams :");
            var5.append(var7);
            var5.append(" ,lp.height :");
            var5.append(var7.height);
            Log.d("Sesl_AppBarLayout", var5.toString());
            this.setLayoutParams(var7);
        }

    }

    public interface BaseOnOffsetChangedListener<T extends SamsungAppBarLayout> {
        void onOffsetChanged(T var1, int var2);
    }

    public interface OnOffsetChangedListener extends SamsungAppBarLayout.BaseOnOffsetChangedListener<SamsungAppBarLayout> {
    }

    protected static class BaseBehavior<T extends SamsungAppBarLayout> extends HeaderBehavior<T> {
        public WeakReference<View> lastNestedScrollingChildRef;
        public int lastStartedType;
        public float mDiffY_Touch;
        public boolean mIsFlingScrollDown = false;
        public boolean mIsFlingScrollUp = false;
        public boolean mIsScrollHold = false;
        public boolean mIsSetStaticDuration = false;
        public float mLastMotionY_Touch;
        public boolean mLifted;
        public boolean mToolisMouse;
        public int mTouchSlop = -1;
        public float mVelocity = 0.0F;
        public ValueAnimator offsetAnimator;
        public int offsetDelta;
        public int offsetToChildIndexOnLayout = -1;
        public boolean offsetToChildIndexOnLayoutIsMinHeight;
        public float offsetToChildIndexOnLayoutPerc;
        public SamsungAppBarLayout.BaseBehavior.BaseDragCallback onDragCallback;
        public float touchX;
        public float touchY;

        public BaseBehavior() {
        }

        public BaseBehavior(Context var1, AttributeSet var2) {
            super(var1, var2);
        }

        public static boolean checkFlag(int var0, int var1) {
            boolean var2;
            if ((var0 & var1) == var1) {
                var2 = true;
            } else {
                var2 = false;
            }

            return var2;
        }

        public static View getAppBarChildOnOffset(SamsungAppBarLayout var0, int var1) {
            int var2 = Math.abs(var1);
            int var3 = var0.getChildCount();

            for (var1 = 0; var1 < var3; ++var1) {
                View var4 = var0.getChildAt(var1);
                if (var2 >= var4.getTop() && var2 <= var4.getBottom()) {
                    return var4;
                }
            }

            return null;
        }

        public final void animateOffsetTo(CoordinatorLayout var1, T var2, int var3, float var4) {
            int var5;
            if (Math.abs(this.mVelocity) > 0.0F && Math.abs(this.mVelocity) <= 3000.0F) {
                var5 = (int) ((double) (3000.0F - Math.abs(this.mVelocity)) * 0.4D);
            } else {
                var5 = 250;
            }

            int var6 = var5;
            if (var5 <= 250) {
                var6 = 250;
            }

            if (this.mIsSetStaticDuration) {
                this.mIsSetStaticDuration = false;
                var6 = 250;
            }

            if (this.mVelocity < 2000.0F) {
                this.animateOffsetWithDuration(var1, var2, var3, var6);
            }

            this.mVelocity = 0.0F;
        }

        public final void animateOffsetWithDuration(final CoordinatorLayout var1, final T var2, int var3, int var4) {
            int var5 = this.getTopBottomOffsetForScrollingSibling();
            if (var5 == var3) {
                ValueAnimator var7 = this.offsetAnimator;
                if (var7 != null && var7.isRunning()) {
                    this.offsetAnimator.cancel();
                }

            } else {
                ValueAnimator var6 = this.offsetAnimator;
                if (var6 == null) {
                    this.offsetAnimator = new ValueAnimator();
                    this.offsetAnimator.setInterpolator(SamsungAppBarLayout.SINE_OUT_80_INTERPOLATOR);
                    this.offsetAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        public void onAnimationUpdate(ValueAnimator var1x) {
                            SamsungAppBarLayout.BaseBehavior.this.setHeaderTopBottomOffset(var1, var2, (Integer) var1x.getAnimatedValue());
                        }
                    });
                } else {
                    var6.cancel();
                }

                this.offsetAnimator.setDuration((long) Math.min(var4, 600));
                this.offsetAnimator.setIntValues(new int[]{var5, var3});
                this.offsetAnimator.start();
            }
        }

        public boolean canDragView(T var1) {
            SamsungAppBarLayout.BaseBehavior.BaseDragCallback var2 = this.onDragCallback;
            if (var2 != null) {
                return var2.canDrag(var1);
            } else {
                WeakReference var5 = this.lastNestedScrollingChildRef;
                boolean var3 = true;
                boolean var4 = var3;
                if (var5 != null) {
                    View var6 = (View) var5.get();
                    if (var6 != null && var6.isShown() && !var6.canScrollVertically(-1)) {
                        var4 = var3;
                    } else {
                        var4 = false;
                    }
                }

                return var4;
            }
        }

        public final boolean canScrollChildren(CoordinatorLayout var1, T var2, View var3) {
            boolean var4;
            if (var2.hasScrollableChildren() && var1.getHeight() - var3.getHeight() <= var2.getHeight()) {
                var4 = true;
            } else {
                var4 = false;
            }

            return var4;
        }

        public final View findFirstScrollingChild(CoordinatorLayout var1) {
            int var2 = var1.getChildCount();

            for (int var3 = 0; var3 < var2; ++var3) {
                View var4 = var1.getChildAt(var3);
                if (var4 instanceof NestedScrollingChild || var4 instanceof ListView || var4 instanceof ScrollView) {
                    return var4;
                }
            }

            return null;
        }

        public final int getChildIndexOnOffset(T var1, int var2) {
            int var3 = var1.getChildCount();

            for (int var4 = 0; var4 < var3; ++var4) {
                View var5 = var1.getChildAt(var4);
                int var6 = var5.getTop();
                int var7 = var5.getBottom();
                SamsungAppBarLayout.LayoutParams var10 = (SamsungAppBarLayout.LayoutParams) var5.getLayoutParams();
                int var8 = var6;
                int var9 = var7;
                if (checkFlag(var10.getScrollFlags(), 32)) {
                    var8 = var6 - var10.topMargin;
                    var9 = var7 + var10.bottomMargin;
                }

                var7 = -var2;
                if (var8 <= var7 && var9 >= var7) {
                    return var4;
                }
            }

            return -1;
        }

        public int getMaxDragOffset(T var1) {
            return -var1.getDownNestedScrollRange();
        }

        public int getTopBottomOffsetForScrollingSibling() {
            return this.getTopAndBottomOffset() + this.offsetDelta;
        }

        public final int interpolateOffset(T var1, int var2) {
            int var3 = Math.abs(var2);
            int var4 = var1.getChildCount();
            byte var5 = 0;

            for (int var6 = 0; var6 < var4; ++var6) {
                View var7 = var1.getChildAt(var6);
                SamsungAppBarLayout.LayoutParams var8 = (SamsungAppBarLayout.LayoutParams) var7.getLayoutParams();
                Interpolator var9 = var8.getScrollInterpolator();
                if (var3 >= var7.getTop() && var3 <= var7.getBottom()) {
                    if (var9 != null) {
                        var4 = var8.getScrollFlags();
                        var6 = var5;
                        int var11;
                        if ((var4 & 1) != 0) {
                            var11 = 0 + var7.getHeight() + var8.topMargin + var8.bottomMargin;
                            var6 = var11;
                            if ((var4 & 2) != 0) {
                                var6 = var11 - ViewCompat.getMinimumHeight(var7);
                            }
                        }

                        var11 = var6;
                        if (ViewCompat.getFitsSystemWindows(var7)) {
                            var11 = var6 - var1.getTopInset();
                        }

                        if (var11 > 0) {
                            var6 = var7.getTop();
                            float var10 = (float) var11;
                            var6 = Math.round(var10 * var9.getInterpolation((float) (var3 - var6) / var10));
                            return Integer.signum(var2) * (var7.getTop() + var6);
                        }
                    }
                    break;
                }
            }

            return var2;
        }

        public final boolean isScrollHoldMode(T var1) {
            if (this.mToolisMouse) {
                return false;
            } else {
                boolean var2 = true;
                int var3 = this.getChildIndexOnOffset(var1, this.getTopBottomOffsetForScrollingSibling());
                boolean var4 = var2;
                if (var3 >= 0) {
                    var4 = var2;
                    if ((((SamsungAppBarLayout.LayoutParams) var1.getChildAt(var3).getLayoutParams()).getScrollFlags() & 65536) == 65536) {
                        var4 = false;
                    }
                }

                return var4;
            }
        }

        public boolean onLayoutChild(CoordinatorLayout var1, T var2, int var3) {
            boolean var4 = super.onLayoutChild(var1, var2, var3);
            int var5 = var2.getPendingAction();
            var3 = this.offsetToChildIndexOnLayout;
            if (var3 >= 0 && (var5 & 8) == 0) {
                View var6 = var2.getChildAt(var3);
                var5 = -var6.getBottom();
                if (this.offsetToChildIndexOnLayoutIsMinHeight) {
                    var3 = ViewCompat.getMinimumHeight(var6) + var2.getTopInset();
                } else {
                    var3 = Math.round((float) var6.getHeight() * this.offsetToChildIndexOnLayoutPerc);
                }

                this.setHeaderTopBottomOffset(var1, var2, var5 + var3);
            } else if (var5 != 0) {
                boolean var7;
                if ((var5 & 4) != 0) {
                    var7 = true;
                } else {
                    var7 = false;
                }

                if ((var5 & 2) != 0) {
                    var5 = -var2.getUpNestedPreScrollRange();
                    if (var7) {
                        this.animateOffsetTo(var1, var2, var5, 0.0F);
                    } else {
                        this.setHeaderTopBottomOffset(var1, var2, var5);
                    }
                } else if ((var5 & 1) != 0) {
                    if (var7) {
                        this.animateOffsetTo(var1, var2, 0, 0.0F);
                    } else {
                        this.setHeaderTopBottomOffset(var1, var2, 0);
                    }
                }
            }

            var2.resetPendingAction();
            this.offsetToChildIndexOnLayout = -1;
            this.setTopAndBottomOffset(MathUtils.clamp(this.getTopAndBottomOffset(), -var2.getTotalScrollRange(), 0));
            this.updateAppBarLayoutDrawableState(var1, var2, this.getTopAndBottomOffset(), 0, false);
            var2.dispatchOffsetUpdates(this.getTopAndBottomOffset());
            return var4;
        }

        @SuppressLint("WrongConstant")
        public boolean onMeasureChild(CoordinatorLayout var1, T var2, int var3, int var4, int var5, int var6) {
            if (((CoordinatorLayout.LayoutParams) var2.getLayoutParams()).height == -2) {
                var1.onMeasureChild(var2, var3, var4, MeasureSpec.makeMeasureSpec(0, 0), var6);
                return true;
            } else {
                return super.onMeasureChild(var1, var2, var3, var4, var5, var6);
            }
        }

        public boolean onNestedPreFling(CoordinatorLayout var1, T var2, View var3, float var4, float var5) {
            this.mVelocity = var5;
            if (var5 < -300.0F) {
                this.mIsFlingScrollDown = true;
                this.mIsFlingScrollUp = false;
            } else {
                if (var5 <= 300.0F) {
                    this.mVelocity = 0.0F;
                    this.mIsFlingScrollDown = false;
                    this.mIsFlingScrollUp = false;
                    return true;
                }

                this.mIsFlingScrollDown = false;
                this.mIsFlingScrollUp = true;
            }

            return super.onNestedPreFling(var1, var2, var3, var4, var5);
        }

        public void onNestedPreScroll(CoordinatorLayout var1, T var2, View var3, int var4, int var5, int[] var6, int var7) {
            if (var5 != 0) {
                int var8;
                if (var5 < 0) {
                    var8 = -var2.getTotalScrollRange();
                    int var9 = var2.getDownNestedPreScrollRange();
                    this.mIsFlingScrollDown = true;
                    this.mIsFlingScrollUp = false;
                    if ((double) var2.getBottom() >= (double) var2.getHeight() * 0.52D) {
                        this.mIsSetStaticDuration = true;
                    }

                    if (var5 < -30) {
                        this.mIsFlingScrollDown = true;
                    } else {
                        this.mVelocity = 0.0F;
                        this.mIsFlingScrollDown = false;
                    }

                    var4 = var8;
                    var8 += var9;
                } else {
                    var4 = -var2.getUpNestedPreScrollRange();
                    this.mIsFlingScrollDown = false;
                    this.mIsFlingScrollUp = true;
                    if ((double) var2.getBottom() <= (double) var2.getHeight() * 0.43D) {
                        this.mIsSetStaticDuration = true;
                    }

                    if (var5 > 30) {
                        this.mIsFlingScrollUp = true;
                    } else {
                        this.mVelocity = 0.0F;
                        this.mIsFlingScrollUp = false;
                    }

                    if (this.getTopAndBottomOffset() == var4) {
                        this.mIsScrollHold = true;
                    }

                    var8 = 0;
                }

                if (var4 != var8) {
                    var6[1] = this.scroll(var1, var2, var5, var4, var8);
                }
            }

            if (var2.isLiftOnScroll()) {
                var2.setLiftedState(var2.shouldLift(var3));
            }

            this.stopNestedScrollIfNeeded(var5, var2, var3, var7);
        }

        @SuppressLint("WrongConstant")
        public void onNestedScroll(CoordinatorLayout var1, T var2, View var3, int var4, int var5, int var6, int var7, int var8) {
            if (this.isScrollHoldMode(var2)) {
                if (var7 < 0 && !this.mIsScrollHold) {
                    this.scroll(var1, var2, var7, -var2.getDownNestedScrollRange(), 0);
                    this.stopNestedScrollIfNeeded(var7, var2, var3, var8);
                } else {
                    ViewCompat.stopNestedScroll(var3, 1);
                }
            } else if (var7 < 0) {
                this.scroll(var1, var2, var7, -var2.getDownNestedScrollRange(), 0);
                this.stopNestedScrollIfNeeded(var7, var2, var3, var8);
            }

        }

        public void onRestoreInstanceState(CoordinatorLayout var1, T var2, Parcelable var3) {
            if (var3 instanceof SamsungAppBarLayout.BaseBehavior.SavedState) {
                SamsungAppBarLayout.BaseBehavior.SavedState var4 = (SamsungAppBarLayout.BaseBehavior.SavedState) var3;
                super.onRestoreInstanceState(var1, var2, var4.getSuperState());
                this.offsetToChildIndexOnLayout = var4.firstVisibleChildIndex;
                this.offsetToChildIndexOnLayoutPerc = var4.firstVisibleChildPercentageShown;
                this.offsetToChildIndexOnLayoutIsMinHeight = var4.firstVisibleChildAtMinimumHeight;
            } else {
                super.onRestoreInstanceState(var1, var2, var3);
                this.offsetToChildIndexOnLayout = -1;
            }

        }

        public Parcelable onSaveInstanceState(CoordinatorLayout var1, T var2) {
            Parcelable var3 = super.onSaveInstanceState(var1, var2);
            int var4 = this.getTopAndBottomOffset();
            int var5 = var2.getChildCount();
            boolean var6 = false;

            for (int var7 = 0; var7 < var5; ++var7) {
                View var9 = var2.getChildAt(var7);
                int var8 = var9.getBottom() + var4;
                if (var9.getTop() + var4 <= 0 && var8 >= 0) {
                    SamsungAppBarLayout.BaseBehavior.SavedState var10 = new SamsungAppBarLayout.BaseBehavior.SavedState(var3);
                    var10.firstVisibleChildIndex = var7;
                    if (var8 == ViewCompat.getMinimumHeight(var9) + var2.getTopInset()) {
                        var6 = true;
                    }

                    var10.firstVisibleChildAtMinimumHeight = var6;
                    var10.firstVisibleChildPercentageShown = (float) var8 / (float) var9.getHeight();
                    return var10;
                }
            }

            return var3;
        }

        public boolean onStartNestedScroll(CoordinatorLayout var1, T var2, View var3, View var4, int var5, int var6) {
            boolean var7;
            if ((var5 & 2) == 0 || !var2.isLiftOnScroll() && !this.canScrollChildren(var1, var2, var3)) {
                var7 = false;
            } else {
                var7 = true;
            }

            if (var7) {
                ValueAnimator var9 = this.offsetAnimator;
                if (var9 != null) {
                    var9.cancel();
                }
            }

            if (var2.mLifted) {
                float var8 = (float) (var2.getHeight() - var2.getTotalScrollRange());
                if (var8 > SamsungAppBarLayout.mAppBarHeight) {
                    StringBuilder var10 = new StringBuilder();
                    var10.append("CollapsedHeight is bigger than AppBarHeight :");
                    var10.append(var8);
                    Log.d("Sesl_AppBarLayout", var10.toString());
                    SamsungAppBarLayout.mAppBarHeight = var8;
                }
            }

            if ((float) var2.getBottom() <= SamsungAppBarLayout.mAppBarHeight) {
                this.mLifted = true;
                var2.mLifted = this.mLifted;
                this.mDiffY_Touch = 0.0F;
            } else {
                this.mLifted = false;
                var2.mLifted = this.mLifted;
            }

            this.lastNestedScrollingChildRef = null;
            this.lastStartedType = var6;
            return var7;
        }

        public void onStopNestedScroll(CoordinatorLayout var1, T var2, View var3, int var4) {
            if (this.getLastInterceptTouchEventEvent() == 3 || this.getLastInterceptTouchEventEvent() == 1 || this.getLastTouchEventEvent() == 3 || this.getLastTouchEventEvent() == 1) {
                this.snapToChildIfNeeded(var1, var2);
            }

            if (this.lastStartedType == 0 || var4 == 1) {
                if (var2.isLiftOnScroll()) {
                    var2.setLiftedState(var2.shouldLift(var3));
                }

                if (this.mIsScrollHold) {
                    this.mIsScrollHold = false;
                }
            }

            this.lastNestedScrollingChildRef = new WeakReference(var3);
        }

        public boolean onTouchEvent(CoordinatorLayout var1, T var2, MotionEvent var3) {
            if (this.mTouchSlop < 0) {
                this.mTouchSlop = ViewConfiguration.get(var1.getContext()).getScaledTouchSlop();
            }

            int var4 = var3.getAction();
            if (var4 != 0) {
                boolean var5 = true;
                float var6;
                if (var4 != 1) {
                    if (var4 == 2) {
                        if (var3 == null || !MotionEventCompat.isFromSource(var3, 8194)) {
                            var5 = false;
                        }

                        this.mToolisMouse = var5;
                        var6 = var3.getY();
                        float var7 = this.mLastMotionY_Touch;
                        if (var6 - var7 != 0.0F) {
                            this.mDiffY_Touch = var6 - var7;
                        }

                        if (Math.abs(this.mDiffY_Touch) > (float) this.mTouchSlop) {
                            this.mLastMotionY_Touch = var6;
                        }

                        return super.onTouchEvent(var1, var2, var3);
                    }

                    if (var4 != 3) {
                        return super.onTouchEvent(var1, var2, var3);
                    }
                }

                if (Math.abs(this.mDiffY_Touch) > 21.0F) {
                    var6 = this.mDiffY_Touch;
                    if (var6 < 0.0F) {
                        this.mIsFlingScrollUp = true;
                        this.mIsFlingScrollDown = false;
                    } else if (var6 > 0.0F) {
                        this.mIsFlingScrollUp = false;
                        this.mIsFlingScrollDown = true;
                    }
                } else {
                    this.touchX = 0.0F;
                    this.touchY = 0.0F;
                    this.mIsFlingScrollUp = false;
                    this.mIsFlingScrollDown = false;
                    this.mLastMotionY_Touch = 0.0F;
                }

                this.snapToChildIfNeeded(var1, var2);
            } else {
                this.touchX = var3.getX();
                this.touchY = var3.getY();
                this.mLastMotionY_Touch = this.touchY;
                this.mDiffY_Touch = 0.0F;
            }

            return super.onTouchEvent(var1, var2, var3);
        }

        public int setHeaderTopBottomOffset(CoordinatorLayout var1, T var2, int var3, int var4, int var5) {
            int var6 = this.getTopBottomOffsetForScrollingSibling();
            byte var7 = 0;
            if (var4 != 0 && var6 >= var4 && var6 <= var5) {
                var4 = MathUtils.clamp(var3, var4, var5);
                var3 = var7;
                if (var6 != var4) {
                    if (var2.hasChildWithInterpolator()) {
                        var3 = this.interpolateOffset(var2, var4);
                    } else {
                        var3 = var4;
                    }

                    boolean var8 = this.setTopAndBottomOffset(var3);
                    var5 = var6 - var4;
                    this.offsetDelta = var4 - var3;
                    if (!var8 && var2.hasChildWithInterpolator()) {
                        var1.dispatchDependentViewsChanged(var2);
                    }

                    var2.dispatchOffsetUpdates(this.getTopAndBottomOffset());
                    byte var9;
                    if (var4 < var6) {
                        var9 = -1;
                    } else {
                        var9 = 1;
                    }

                    this.updateAppBarLayoutDrawableState(var1, var2, var4, var9, false);
                    var3 = var5;
                }
            } else {
                this.offsetDelta = 0;
                var3 = var7;
            }

            return var3;
        }

        public final boolean shouldJumpElevationState(CoordinatorLayout var1, T var2) {
            List var6 = var1.getDependents(var2);
            int var3 = var6.size();
            boolean var4 = false;

            for (int var5 = 0; var5 < var3; ++var5) {
                CoordinatorLayout.Behavior var7 = ((CoordinatorLayout.LayoutParams) ((View) var6.get(var5)).getLayoutParams()).getBehavior();
                if (var7 instanceof SamsungAppBarLayout.ScrollingViewBehavior) {
                    if (((SamsungAppBarLayout.ScrollingViewBehavior) var7).getOverlayTop() != 0) {
                        var4 = true;
                    }

                    return var4;
                }
            }

            return false;
        }

        public final void snapToChildIfNeeded(CoordinatorLayout var1, T var2) {
            int var3 = this.getTopBottomOffsetForScrollingSibling();
            int var4 = this.getChildIndexOnOffset(var2, var3);
            View var5 = var1.getChildAt(1);
            if (var4 >= 0) {
                View var6 = var2.getChildAt(var4);
                SamsungAppBarLayout.LayoutParams var7 = (SamsungAppBarLayout.LayoutParams) var6.getLayoutParams();
                int var8 = var7.getScrollFlags();
                if ((var8 & 4096) == 4096) {
                    return;
                }

                int var9 = -var6.getTop();
                int var10 = -var6.getBottom();
                int var11 = var10;
                if (var4 == var2.getChildCount() - 1) {
                    var11 = var10 + var2.getTopInset();
                }

                int var12;
                if (checkFlag(var8, 2)) {
                    var4 = var11 + ViewCompat.getMinimumHeight(var6);
                    var12 = var9;
                } else {
                    var12 = var9;
                    var4 = var11;
                    if (checkFlag(var8, 5)) {
                        var10 = ViewCompat.getMinimumHeight(var6) + var11;
                        if (var3 < var10) {
                            var12 = var10;
                            var4 = var11;
                        } else {
                            var4 = var10;
                            var12 = var9;
                        }
                    }
                }

                var10 = var12;
                var11 = var4;
                if (checkFlag(var8, 32)) {
                    var10 = var12 + var7.topMargin;
                    var11 = var4 - var7.bottomMargin;
                }

                label71:
                {
                    label70:
                    {
                        double var13 = (double) var3;
                        double var15 = (double) (var11 + var10);
                        if (this.mLifted) {
                            if (var13 >= var15 * 0.52D) {
                                break label70;
                            }
                        } else if (var13 >= var15 * 0.43D) {
                            break label70;
                        }

                        var4 = var11;
                        break label71;
                    }

                    var4 = var10;
                }

                label63:
                {
                    if (this.isScrollHoldMode(var2)) {
                        if (this.mIsFlingScrollUp) {
                            this.mIsFlingScrollUp = false;
                            this.mIsFlingScrollDown = false;
                            var4 = var11;
                        }

                        var11 = var4;
                        if (!this.mIsFlingScrollDown) {
                            break label63;
                        }

                        var11 = var4;
                        if (var5 == null) {
                            break label63;
                        }

                        var11 = var4;
                        if ((float) var5.getTop() <= SamsungAppBarLayout.mAppBarHeight) {
                            break label63;
                        }

                        this.mIsFlingScrollDown = false;
                    } else {
                        if (this.mIsFlingScrollUp) {
                            this.mIsFlingScrollUp = false;
                            this.mIsFlingScrollDown = false;
                            var4 = var11;
                        }

                        var11 = var4;
                        if (!this.mIsFlingScrollDown) {
                            break label63;
                        }

                        var11 = var4;
                        if (var5 == null) {
                            break label63;
                        }

                        var11 = var4;
                        if ((float) var5.getTop() <= SamsungAppBarLayout.mAppBarHeight) {
                            break label63;
                        }

                        this.mIsFlingScrollDown = false;
                    }

                    var11 = var10;
                }

                this.animateOffsetTo(var1, var2, MathUtils.clamp(var11, -var2.getTotalScrollRange(), 0), 0.0F);
            }

        }

        @SuppressLint("WrongConstant")
        public final void stopNestedScrollIfNeeded(int var1, T var2, View var3, int var4) {
            if (var4 == 1) {
                var4 = this.getTopBottomOffsetForScrollingSibling();
                if (var1 < 0 && var4 == 0 || var1 > 0 && var4 == -var2.getDownNestedScrollRange()) {
                    ViewCompat.stopNestedScroll(var3, 1);
                }
            }

        }

        public final void updateAppBarLayoutDrawableState(CoordinatorLayout var1, T var2, int var3, int var4, boolean var5) {
            View var6 = getAppBarChildOnOffset(var2, var3);
            if (var6 != null) {
                int var7 = ((SamsungAppBarLayout.LayoutParams) var6.getLayoutParams()).getScrollFlags();
                boolean var8 = false;
                boolean var9 = var8;
                if ((var7 & 1) != 0) {
                    label51:
                    {
                        int var10 = ViewCompat.getMinimumHeight(var6);
                        if (var4 > 0 && (var7 & 12) != 0) {
                            var9 = var8;
                            if (-var3 < var6.getBottom() - var10 - var2.getTopInset()) {
                                break label51;
                            }
                        } else {
                            var9 = var8;
                            if ((var7 & 2) == 0) {
                                break label51;
                            }

                            var9 = var8;
                            if (-var3 < var6.getBottom() - var10 - var2.getTopInset()) {
                                break label51;
                            }
                        }

                        var9 = true;
                    }
                }

                if (var2.isLiftOnScroll()) {
                    var9 = var2.shouldLift(this.findFirstScrollingChild(var1));
                }

                var9 = var2.setLiftedState(var9);
                if (Build.VERSION.SDK_INT >= 11 && (var5 || var9 && this.shouldJumpElevationState(var1, var2))) {
                    var2.jumpDrawablesToCurrentState();
                }
            }

        }

        public abstract static class BaseDragCallback<T extends SamsungAppBarLayout> {
            public abstract boolean canDrag(T var1);
        }

        protected static class SavedState extends AbsSavedState {
            public static final Creator<SamsungAppBarLayout.BaseBehavior.SavedState> CREATOR = new ClassLoaderCreator<SamsungAppBarLayout.BaseBehavior.SavedState>() {
                public SamsungAppBarLayout.BaseBehavior.SavedState createFromParcel(Parcel var1) {
                    return new SamsungAppBarLayout.BaseBehavior.SavedState(var1, (ClassLoader) null);
                }

                public SamsungAppBarLayout.BaseBehavior.SavedState createFromParcel(Parcel var1, ClassLoader var2) {
                    return new SamsungAppBarLayout.BaseBehavior.SavedState(var1, var2);
                }

                public SamsungAppBarLayout.BaseBehavior.SavedState[] newArray(int var1) {
                    return new SamsungAppBarLayout.BaseBehavior.SavedState[var1];
                }
            };
            public boolean firstVisibleChildAtMinimumHeight;
            public int firstVisibleChildIndex;
            public float firstVisibleChildPercentageShown;

            public SavedState(Parcel var1, ClassLoader var2) {
                super(var1, var2);
                this.firstVisibleChildIndex = var1.readInt();
                this.firstVisibleChildPercentageShown = var1.readFloat();
                boolean var3;
                if (var1.readByte() != 0) {
                    var3 = true;
                } else {
                    var3 = false;
                }

                this.firstVisibleChildAtMinimumHeight = var3;
            }

            public SavedState(Parcelable var1) {
                super(var1);
            }

            public void writeToParcel(Parcel var1, int var2) {
                super.writeToParcel(var1, var2);
                var1.writeInt(this.firstVisibleChildIndex);
                var1.writeFloat(this.firstVisibleChildPercentageShown);
                var1.writeByte((byte) (firstVisibleChildAtMinimumHeight ? 1 : 0));
            }
        }
    }

    public static class Behavior extends SamsungAppBarLayout.BaseBehavior<SamsungAppBarLayout> {
        public Behavior() {
        }

        public Behavior(Context var1, AttributeSet var2) {
            super(var1, var2);
        }
    }

    public static class LayoutParams extends android.widget.LinearLayout.LayoutParams {
        public int scrollFlags = 1;
        public Interpolator scrollInterpolator;

        public LayoutParams(int var1, int var2) {
            super(var1, var2);
        }

        public LayoutParams(Context var1, AttributeSet var2) {
            super(var1, var2);
            TypedArray var3 = var1.obtainStyledAttributes(var2, R.styleable.SamsungAppBarLayout_Layout);
            this.scrollFlags = var3.getInt(R.styleable.SamsungAppBarLayout_Layout_layout_scrollFlags, 0);
            if (var3.hasValue(R.styleable.SamsungAppBarLayout_Layout_layout_scrollInterpolator)) {
                this.scrollInterpolator = AnimationUtils.loadInterpolator(var1, var3.getResourceId(R.styleable.SamsungAppBarLayout_Layout_layout_scrollInterpolator, 0));
            }

            var3.recycle();
        }

        public LayoutParams(android.view.ViewGroup.LayoutParams var1) {
            super(var1);
        }

        public LayoutParams(MarginLayoutParams var1) {
            super(var1);
        }

        public LayoutParams(android.widget.LinearLayout.LayoutParams var1) {
            super(var1);
        }

        public int getScrollFlags() {
            return this.scrollFlags;
        }

        public Interpolator getScrollInterpolator() {
            return this.scrollInterpolator;
        }

        public boolean isCollapsible() {
            int var1 = this.scrollFlags;
            boolean var2 = true;
            if ((var1 & 1) != 1 || (var1 & 10) == 0) {
                var2 = false;
            }

            return var2;
        }
    }

    public static class ScrollingViewBehavior extends HeaderScrollingViewBehavior {
        public ScrollingViewBehavior() {
        }

        public ScrollingViewBehavior(Context var1, AttributeSet var2) {
            super(var1, var2);
            TypedArray var3 = var1.obtainStyledAttributes(var2, R.styleable.ScrollingViewBehavior_Layout);
            this.setOverlayTop(var3.getDimensionPixelSize(R.styleable.ScrollingViewBehavior_Layout_behavior_overlapTop, 0));
            var3.recycle();
        }

        public static int getAppBarLayoutOffset(SamsungAppBarLayout var0) {
            CoordinatorLayout.Behavior var1 = ((CoordinatorLayout.LayoutParams) var0.getLayoutParams()).getBehavior();
            return var1 instanceof SamsungAppBarLayout.BaseBehavior ? ((SamsungAppBarLayout.BaseBehavior) var1).getTopBottomOffsetForScrollingSibling() : 0;
        }

        public SamsungAppBarLayout findFirstDependency(List<View> var1) {
            int var2 = var1.size();

            for (int var3 = 0; var3 < var2; ++var3) {
                View var4 = (View) var1.get(var3);
                if (var4 instanceof SamsungAppBarLayout) {
                    return (SamsungAppBarLayout) var4;
                }
            }

            return null;
        }

        public float getOverlapRatioForOffset(View var1) {
            if (var1 instanceof SamsungAppBarLayout) {
                SamsungAppBarLayout var5 = (SamsungAppBarLayout) var1;
                int var2 = var5.getTotalScrollRange();
                int var3 = var5.getDownNestedPreScrollRange();
                int var4 = getAppBarLayoutOffset(var5);
                if (var3 != 0 && var2 + var4 <= var3) {
                    return 0.0F;
                }

                var2 -= var3;
                if (var2 != 0) {
                    return (float) var4 / (float) var2 + 1.0F;
                }
            }

            return 0.0F;
        }

        public int getScrollRange(View var1) {
            return var1 instanceof SamsungAppBarLayout ? ((SamsungAppBarLayout) var1).getTotalScrollRange() : super.getScrollRange(var1);
        }

        public boolean layoutDependsOn(CoordinatorLayout var1, View var2, View var3) {
            return var3 instanceof SamsungAppBarLayout;
        }

        public final void offsetChildAsNeeded(View var1, View var2) {
            CoordinatorLayout.Behavior var3 = ((CoordinatorLayout.LayoutParams) var2.getLayoutParams()).getBehavior();
            if (var3 instanceof SamsungAppBarLayout.BaseBehavior) {
                SamsungAppBarLayout.BaseBehavior var4 = (SamsungAppBarLayout.BaseBehavior) var3;
                ViewCompat.offsetTopAndBottom(var1, var2.getBottom() - var1.getTop() + var4.offsetDelta + this.getVerticalLayoutGap() - this.getOverlapPixelsForOffset(var2));
            }

        }

        public boolean onDependentViewChanged(CoordinatorLayout var1, View var2, View var3) {
            this.offsetChildAsNeeded(var2, var3);
            this.updateLiftedStateIfNeeded(var2, var3);
            return false;
        }

        public boolean onRequestChildRectangleOnScreen(CoordinatorLayout var1, View var2, Rect var3, boolean var4) {
            SamsungAppBarLayout var5 = this.findFirstDependency(var1.getDependencies(var2));
            if (var5 != null) {
                var3.offset(var2.getLeft(), var2.getTop());
                Rect var6 = super.tempRect1;
                var6.set(0, 0, var1.getWidth(), var1.getHeight());
                if (!var6.contains(var3)) {
                    var5.setExpanded(false, var4 ^ true);
                    return true;
                }
            }

            return false;
        }

        public final void updateLiftedStateIfNeeded(View var1, View var2) {
            if (var2 instanceof SamsungAppBarLayout) {
                SamsungAppBarLayout var3 = (SamsungAppBarLayout) var2;
                if (var3.isLiftOnScroll()) {
                    var3.setLiftedState(var3.shouldLift(var1));
                }
            }

        }
    }
}
