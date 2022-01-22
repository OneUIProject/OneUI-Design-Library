package de.dlyt.yanndroid.oneui.sesl.coordinatorlayout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.coordinatorlayout.widget.DirectedAcyclicGraph;
import androidx.coordinatorlayout.widget.ViewGroupUtils;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.util.ObjectsCompat;
import androidx.core.util.Pools;
import androidx.core.view.GravityCompat;
import androidx.core.view.NestedScrollingParent2;
import androidx.core.view.NestedScrollingParent3;
import androidx.core.view.NestedScrollingParentHelper;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.customview.view.AbsSavedState;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.dlyt.yanndroid.oneui.R;

public class SamsungCoordinatorLayout extends ViewGroup implements NestedScrollingParent2, NestedScrollingParent3 {
    public static final Class<?>[] CONSTRUCTOR_PARAMS;
    public static final Comparator<View> TOP_SORTED_CHILDREN_COMPARATOR;
    public static final String WIDGET_PACKAGE_NAME;
    public static final ThreadLocal<Map<String, Constructor<SamsungCoordinatorLayout.Behavior>>> sConstructors;
    public static final Pools.Pool<Rect> sRectPool;

    static {
        Package var0 = SamsungCoordinatorLayout.class.getPackage();
        String var1;
        if (var0 != null) {
            var1 = var0.getName();
        } else {
            var1 = null;
        }

        WIDGET_PACKAGE_NAME = var1;
        if (Build.VERSION.SDK_INT >= 21) {
            TOP_SORTED_CHILDREN_COMPARATOR = new SamsungCoordinatorLayout.ViewElevationComparator();
        } else {
            TOP_SORTED_CHILDREN_COMPARATOR = null;
        }

        CONSTRUCTOR_PARAMS = new Class[]{Context.class, AttributeSet.class};
        sConstructors = new ThreadLocal();
        sRectPool = new Pools.SynchronizedPool(12);
    }

    public final int[] mBehaviorConsumed;
    public final DirectedAcyclicGraph<View> mChildDag;
    public final List<View> mDependencySortedChildren;
    public final NestedScrollingParentHelper mNestedScrollingParentHelper;
    public final int[] mNestedScrollingV2ConsumedCompat;
    public final List<View> mTempDependenciesList;
    public final List<View> mTempList1;
    public androidx.core.view.OnApplyWindowInsetsListener mApplyWindowInsetsListener;
    public View mBehaviorTouchView;
    public boolean mDisallowInterceptReset;
    public boolean mDrawStatusBarBackground;
    public boolean mIsAttachedToWindow;
    public int[] mKeylines;
    public WindowInsetsCompat mLastInsets;
    public View mLastNestedScrollingChild;
    public boolean mNeedsPreDrawListener;
    public View mNestedScrollingTarget;
    public OnHierarchyChangeListener mOnHierarchyChangeListener;
    public SamsungCoordinatorLayout.OnPreDrawListener mOnPreDrawListener;
    public Paint mScrimPaint;
    public Drawable mStatusBarBackground;

    public SamsungCoordinatorLayout(Context var1) {
        this(var1, (AttributeSet) null);
    }

    public SamsungCoordinatorLayout(Context var1, AttributeSet var2) {
        this(var1, var2, R.attr.coordinatorLayoutStyle);
    }

    @SuppressLint("RestrictedApi")
    public SamsungCoordinatorLayout(Context var1, AttributeSet var2, int var3) {
        super(var1, var2, var3);
        this.mDependencySortedChildren = new ArrayList();
        this.mChildDag = new DirectedAcyclicGraph();
        this.mTempList1 = new ArrayList();
        this.mTempDependenciesList = new ArrayList();
        this.mBehaviorConsumed = new int[2];
        this.mNestedScrollingV2ConsumedCompat = new int[2];
        this.mNestedScrollingParentHelper = new NestedScrollingParentHelper(this);
        byte var4 = 0;
        TypedArray var9;
        if (var3 == 0) {
            var9 = var1.obtainStyledAttributes(var2, R.styleable.SamsungCoordinatorLayout, 0, R.style.Widget_Support_CoordinatorLayout);
        } else {
            var9 = var1.obtainStyledAttributes(var2, R.styleable.SamsungCoordinatorLayout, var3, 0);
        }

        var3 = var9.getResourceId(R.styleable.SamsungCoordinatorLayout_keylines, 0);
        if (var3 != 0) {
            Resources var7 = var1.getResources();
            this.mKeylines = var7.getIntArray(var3);
            float var5 = var7.getDisplayMetrics().density;
            int var6 = this.mKeylines.length;

            for (var3 = var4; var3 < var6; ++var3) {
                int[] var8 = this.mKeylines;
                var8[var3] = (int) ((float) var8[var3] * var5);
            }
        }

        this.mStatusBarBackground = var9.getDrawable(R.styleable.SamsungCoordinatorLayout_statusBarBackground);
        var9.recycle();
        this.setupForInsets();
        super.setOnHierarchyChangeListener(new SamsungCoordinatorLayout.HierarchyChangeListener());
    }

    public static Rect acquireTempRect() {
        Rect var0 = (Rect) sRectPool.acquire();
        Rect var1 = var0;
        if (var0 == null) {
            var1 = new Rect();
        }

        return var1;
    }

    public static int clamp(int var0, int var1, int var2) {
        if (var0 < var1) {
            return var1;
        } else {
            return var0 > var2 ? var2 : var0;
        }
    }

    public static Behavior parseBehavior(Context context, AttributeSet attrs, String name) {
        if (TextUtils.isEmpty(name)) {
            return null;
        }
        final String fullName;
        if (name.startsWith(".")) {
            fullName = context.getPackageName() + name;
        } else if (name.indexOf('.') >= 0) {
            fullName = name;
        } else {
            fullName = !TextUtils.isEmpty(WIDGET_PACKAGE_NAME) ? (WIDGET_PACKAGE_NAME + '.' + name) : name;
        }
        try {
            Map<String, Constructor<Behavior>> constructors = sConstructors.get();
            if (constructors == null) {
                constructors = new HashMap<>();
                sConstructors.set(constructors);
            }
            Constructor<Behavior> c = constructors.get(fullName);
            if (c == null) {
                final Class<Behavior> clazz = (Class<Behavior>) Class.forName(fullName, false, context.getClassLoader());
                c = clazz.getConstructor(CONSTRUCTOR_PARAMS);
                c.setAccessible(true);
                constructors.put(fullName, c);
            }
            return c.newInstance(context, attrs);
        } catch (Exception e) {
            throw new RuntimeException("Could not inflate Behavior subclass " + fullName, e);
        }
    }

    public static void releaseTempRect(Rect var0) {
        var0.setEmpty();
        sRectPool.release(var0);
    }

    public static int resolveAnchoredChildGravity(int var0) {
        int var1 = var0;
        if (var0 == 0) {
            var1 = 17;
        }

        return var1;
    }

    public static int resolveGravity(int var0) {
        int var1 = var0;
        if ((var0 & 7) == 0) {
            var1 = var0 | 8388611;
        }

        var0 = var1;
        if ((var1 & 112) == 0) {
            var0 = var1 | 48;
        }

        return var0;
    }

    public static int resolveKeylineGravity(int var0) {
        int var1 = var0;
        if (var0 == 0) {
            var1 = 8388661;
        }

        return var1;
    }

    public void addPreDrawListener() {
        if (this.mIsAttachedToWindow) {
            if (this.mOnPreDrawListener == null) {
                this.mOnPreDrawListener = new SamsungCoordinatorLayout.OnPreDrawListener();
            }

            this.getViewTreeObserver().addOnPreDrawListener(this.mOnPreDrawListener);
        }

        this.mNeedsPreDrawListener = true;
    }

    public boolean checkLayoutParams(android.view.ViewGroup.LayoutParams var1) {
        boolean var2;
        if (var1 instanceof SamsungCoordinatorLayout.LayoutParams && super.checkLayoutParams(var1)) {
            var2 = true;
        } else {
            var2 = false;
        }

        return var2;
    }

    public final void constrainChildRect(SamsungCoordinatorLayout.LayoutParams var1, Rect var2, int var3, int var4) {
        int var5 = this.getWidth();
        int var6 = this.getHeight();
        var5 = Math.max(this.getPaddingLeft() + var1.leftMargin, Math.min(var2.left, var5 - this.getPaddingRight() - var3 - var1.rightMargin));
        var6 = Math.max(this.getPaddingTop() + var1.topMargin, Math.min(var2.top, var6 - this.getPaddingBottom() - var4 - var1.bottomMargin));
        var2.set(var5, var6, var3 + var5, var4 + var6);
    }

    public final WindowInsetsCompat dispatchApplyWindowInsetsToBehaviors(WindowInsetsCompat var1) {
        if (var1.isConsumed()) {
            return var1;
        } else {
            int var2 = 0;
            int var3 = this.getChildCount();

            WindowInsetsCompat var4;
            while (true) {
                var4 = var1;
                if (var2 >= var3) {
                    break;
                }

                View var5 = this.getChildAt(var2);
                var4 = var1;
                if (ViewCompat.getFitsSystemWindows(var5)) {
                    SamsungCoordinatorLayout.Behavior var6 = ((SamsungCoordinatorLayout.LayoutParams) var5.getLayoutParams()).getBehavior();
                    var4 = var1;
                    if (var6 != null) {
                        var1 = var6.onApplyWindowInsets(this, var5, var1);
                        var4 = var1;
                        if (var1.isConsumed()) {
                            var4 = var1;
                            break;
                        }
                    }
                }

                ++var2;
                var1 = var4;
            }

            return var4;
        }
    }

    public void dispatchDependentViewsChanged(View var1) {
        @SuppressLint("RestrictedApi") List var2 = this.mChildDag.getIncomingEdges(var1);
        if (var2 != null && !var2.isEmpty()) {
            for (int var3 = 0; var3 < var2.size(); ++var3) {
                View var4 = (View) var2.get(var3);
                SamsungCoordinatorLayout.Behavior var5 = ((SamsungCoordinatorLayout.LayoutParams) var4.getLayoutParams()).getBehavior();
                if (var5 != null) {
                    var5.onDependentViewChanged(this, var4, var1);
                }
            }
        }

    }

    public boolean dispatchGenericMotionEvent(MotionEvent var1) {
        for (int var2 = this.getChildCount() - 1; var2 >= 0; --var2) {
            View var3 = this.getChildAt(var2);
            if (var3 instanceof AppBarLayoutBehavior) {
                AppBarLayoutBehavior var4 = (AppBarLayoutBehavior) var3;
                if (var1.getAction() == 8) {
                    if (this.mLastNestedScrollingChild != null) {
                        if (var1.getAxisValue(9) < 0.0F) {
                            var4.seslSetExpanded(false);
                        } else if (var1.getAxisValue(9) > 0.0F && !this.mLastNestedScrollingChild.canScrollVertically(-1)) {
                            var4.seslSetExpanded(true);
                        }
                    } else if (var1.getAxisValue(9) < 0.0F) {
                        var4.seslSetExpanded(false);
                    } else if (var1.getAxisValue(9) > 0.0F) {
                        var4.seslSetExpanded(true);
                    }
                }
                break;
            }
        }

        return super.dispatchGenericMotionEvent(var1);
    }

    public boolean dispatchKeyEvent(KeyEvent var1) {
        if (var1.getKeyCode() == 61 || var1.getKeyCode() == 19 || var1.getKeyCode() == 20 || var1.getKeyCode() == 21 || var1.getKeyCode() == 22) {
            int var2 = this.getChildCount();

            for (int var3 = 0; var3 < var2; ++var3) {
                View var4 = this.getChildAt(var3);
                if (var4 instanceof AppBarLayoutBehavior) {
                    AppBarLayoutBehavior var5 = (AppBarLayoutBehavior) var4;
                    if (!var5.seslIsCollapsed()) {
                        var5.seslSetExpanded(false);
                        break;
                    }
                }
            }
        }

        return super.dispatchKeyEvent(var1);
    }

    public boolean drawChild(Canvas var1, View var2, long var3) {
        SamsungCoordinatorLayout.LayoutParams var5 = (SamsungCoordinatorLayout.LayoutParams) var2.getLayoutParams();
        SamsungCoordinatorLayout.Behavior var6 = var5.mBehavior;
        if (var6 != null) {
            float var7 = var6.getScrimOpacity(this, var2);
            if (var7 > 0.0F) {
                if (this.mScrimPaint == null) {
                    this.mScrimPaint = new Paint();
                }

                this.mScrimPaint.setColor(var5.mBehavior.getScrimColor(this, var2));
                this.mScrimPaint.setAlpha(clamp(Math.round(var7 * 255.0F), 0, 255));
                int var8 = var1.save();
                if (var2.isOpaque()) {
                    var1.clipRect((float) var2.getLeft(), (float) var2.getTop(), (float) var2.getRight(), (float) var2.getBottom(), Region.Op.DIFFERENCE);
                }

                var1.drawRect((float) this.getPaddingLeft(), (float) this.getPaddingTop(), (float) (this.getWidth() - this.getPaddingRight()), (float) (this.getHeight() - this.getPaddingBottom()), this.mScrimPaint);
                var1.restoreToCount(var8);
            }
        }

        return super.drawChild(var1, var2, var3);
    }

    public void drawableStateChanged() {
        super.drawableStateChanged();
        int[] var1 = this.getDrawableState();
        Drawable var2 = this.mStatusBarBackground;
        boolean var3 = false;
        boolean var4 = var3;
        if (var2 != null) {
            var4 = var3;
            if (var2.isStateful()) {
                var4 = false | var2.setState(var1);
            }
        }

        if (var4) {
            this.invalidate();
        }

    }

    public void ensurePreDrawListener() {
        int var1 = this.getChildCount();
        boolean var2 = false;
        int var3 = 0;

        boolean var4;
        while (true) {
            var4 = var2;
            if (var3 >= var1) {
                break;
            }

            if (this.hasDependencies(this.getChildAt(var3))) {
                var4 = true;
                break;
            }

            ++var3;
        }

        if (var4 != this.mNeedsPreDrawListener) {
            if (var4) {
                this.addPreDrawListener();
            } else {
                this.removePreDrawListener();
            }
        }

    }

    public SamsungCoordinatorLayout.LayoutParams generateDefaultLayoutParams() {
        return new SamsungCoordinatorLayout.LayoutParams(-2, -2);
    }

    public SamsungCoordinatorLayout.LayoutParams generateLayoutParams(AttributeSet var1) {
        return new SamsungCoordinatorLayout.LayoutParams(this.getContext(), var1);
    }

    public SamsungCoordinatorLayout.LayoutParams generateLayoutParams(android.view.ViewGroup.LayoutParams var1) {
        if (var1 instanceof SamsungCoordinatorLayout.LayoutParams) {
            return new SamsungCoordinatorLayout.LayoutParams((SamsungCoordinatorLayout.LayoutParams) var1);
        } else {
            return var1 instanceof MarginLayoutParams ? new SamsungCoordinatorLayout.LayoutParams((MarginLayoutParams) var1) : new SamsungCoordinatorLayout.LayoutParams(var1);
        }
    }

    public void getChildRect(View var1, boolean var2, Rect var3) {
        if (!var1.isLayoutRequested() && var1.getVisibility() != View.GONE) {
            if (var2) {
                this.getDescendantRect(var1, var3);
            } else {
                var3.set(var1.getLeft(), var1.getTop(), var1.getRight(), var1.getBottom());
            }

        } else {
            var3.setEmpty();
        }
    }

    public List<View> getDependencies(View var1) {
        @SuppressLint("RestrictedApi") List var2 = this.mChildDag.getOutgoingEdges(var1);
        this.mTempDependenciesList.clear();
        if (var2 != null) {
            this.mTempDependenciesList.addAll(var2);
        }

        return this.mTempDependenciesList;
    }

    public final List<View> getDependencySortedChildren() {
        this.prepareChildren();
        return Collections.unmodifiableList(this.mDependencySortedChildren);
    }

    public List<View> getDependents(View var1) {
        @SuppressLint("RestrictedApi") List var2 = this.mChildDag.getIncomingEdges(var1);
        this.mTempDependenciesList.clear();
        if (var2 != null) {
            this.mTempDependenciesList.addAll(var2);
        }

        return this.mTempDependenciesList;
    }

    @SuppressLint("RestrictedApi")
    public void getDescendantRect(View var1, Rect var2) {
        ViewGroupUtils.getDescendantRect(this, var1, var2);
    }

    public void getDesiredAnchoredChildRect(View var1, int var2, Rect var3, Rect var4) {
        SamsungCoordinatorLayout.LayoutParams var5 = (SamsungCoordinatorLayout.LayoutParams) var1.getLayoutParams();
        int var6 = var1.getMeasuredWidth();
        int var7 = var1.getMeasuredHeight();
        this.getDesiredAnchoredChildRectWithoutConstraints(var1, var2, var3, var4, var5, var6, var7);
        this.constrainChildRect(var5, var4, var6, var7);
    }

    public final void getDesiredAnchoredChildRectWithoutConstraints(View var1, int var2, Rect var3, Rect var4, SamsungCoordinatorLayout.LayoutParams var5, int var6, int var7) {
        int var8 = GravityCompat.getAbsoluteGravity(resolveAnchoredChildGravity(var5.gravity), var2);
        int var9 = GravityCompat.getAbsoluteGravity(resolveGravity(var5.anchorGravity), var2);
        int var10 = var8 & 7;
        int var11 = var8 & 112;
        var2 = var9 & 7;
        var9 &= 112;
        if (var2 != 1) {
            if (var2 != 5) {
                var2 = var3.left;
            } else {
                var2 = var3.right;
            }
        } else {
            var2 = var3.left + var3.width() / 2;
        }

        if (var9 != 16) {
            if (var9 != 80) {
                var9 = var3.top;
            } else {
                var9 = var3.bottom;
            }
        } else {
            var9 = var3.top + var3.height() / 2;
        }

        if (var10 != 1) {
            var8 = var2;
            if (var10 != 5) {
                var8 = var2 - var6;
            }
        } else {
            var8 = var2 - var6 / 2;
        }

        if (var11 != 16) {
            var2 = var9;
            if (var11 != 80) {
                var2 = var9 - var7;
            }
        } else {
            var2 = var9 - var7 / 2;
        }

        var4.set(var8, var2, var6 + var8, var7 + var2);
    }

    public final int getKeyline(int var1) {
        int[] var2 = this.mKeylines;
        StringBuilder var3;
        if (var2 == null) {
            var3 = new StringBuilder();
            var3.append("No keylines defined for ");
            var3.append(this);
            var3.append(" - attempted index lookup ");
            var3.append(var1);
            Log.e("CoordinatorLayout", var3.toString());
            return 0;
        } else if (var1 >= 0 && var1 < var2.length) {
            return var2[var1];
        } else {
            var3 = new StringBuilder();
            var3.append("Keyline index ");
            var3.append(var1);
            var3.append(" out of range for ");
            var3.append(this);
            Log.e("CoordinatorLayout", var3.toString());
            return 0;
        }
    }

    public void getLastChildRect(View var1, Rect var2) {
        var2.set(((SamsungCoordinatorLayout.LayoutParams) var1.getLayoutParams()).getLastChildRect());
    }

    public final WindowInsetsCompat getLastWindowInsets() {
        return this.mLastInsets;
    }

    public int getNestedScrollAxes() {
        return this.mNestedScrollingParentHelper.getNestedScrollAxes();
    }

    public SamsungCoordinatorLayout.LayoutParams getResolvedLayoutParams(View var1) {
        SamsungCoordinatorLayout.LayoutParams var2 = (SamsungCoordinatorLayout.LayoutParams) var1.getLayoutParams();
        if (!var2.mBehaviorResolved) {
            if (var1 instanceof SamsungCoordinatorLayout.AttachedBehavior) {
                SamsungCoordinatorLayout.Behavior var6 = ((SamsungCoordinatorLayout.AttachedBehavior) var1).getBehavior();
                if (var6 == null) {
                    Log.e("CoordinatorLayout", "Attached behavior class is null");
                }

                var2.setBehavior(var6);
                var2.mBehaviorResolved = true;
            } else {
                Class var3 = var1.getClass();

                SamsungCoordinatorLayout.DefaultBehavior var4;
                SamsungCoordinatorLayout.DefaultBehavior var7;
                for (var7 = null; var3 != null; var7 = var4) {
                    var4 = (SamsungCoordinatorLayout.DefaultBehavior) var3.getAnnotation(SamsungCoordinatorLayout.DefaultBehavior.class);
                    var7 = var4;
                    if (var4 != null) {
                        break;
                    }

                    var3 = var3.getSuperclass();
                }

                if (var7 != null) {
                    try {
                        var2.setBehavior((SamsungCoordinatorLayout.Behavior) var7.value().getDeclaredConstructor().newInstance());
                    } catch (Exception var5) {
                        StringBuilder var8 = new StringBuilder();
                        var8.append("Default behavior class ");
                        var8.append(var7.value().getName());
                        var8.append(" could not be instantiated. Did you forget a default constructor?");
                        Log.e("CoordinatorLayout", var8.toString(), var5);
                    }
                }

                var2.mBehaviorResolved = true;
            }
        }

        return var2;
    }

    public Drawable getStatusBarBackground() {
        return this.mStatusBarBackground;
    }

    public void setStatusBarBackground(Drawable var1) {
        Drawable var2 = this.mStatusBarBackground;
        if (var2 != var1) {
            Drawable var3 = null;
            if (var2 != null) {
                var2.setCallback((Drawable.Callback) null);
            }

            if (var1 != null) {
                var3 = var1.mutate();
            }

            this.mStatusBarBackground = var3;
            var1 = this.mStatusBarBackground;
            if (var1 != null) {
                if (var1.isStateful()) {
                    this.mStatusBarBackground.setState(this.getDrawableState());
                }

                DrawableCompat.setLayoutDirection(this.mStatusBarBackground, ViewCompat.getLayoutDirection(this));
                var1 = this.mStatusBarBackground;
                boolean var4;
                if (this.getVisibility() == View.VISIBLE) {
                    var4 = true;
                } else {
                    var4 = false;
                }

                var1.setVisible(var4, false);
                this.mStatusBarBackground.setCallback(this);
            }

            ViewCompat.postInvalidateOnAnimation(this);
        }

    }

    public int getSuggestedMinimumHeight() {
        return Math.max(super.getSuggestedMinimumHeight(), this.getPaddingTop() + this.getPaddingBottom());
    }

    public int getSuggestedMinimumWidth() {
        return Math.max(super.getSuggestedMinimumWidth(), this.getPaddingLeft() + this.getPaddingRight());
    }

    public final void getTopSortedChildren(List<View> var1) {
        var1.clear();
        boolean var2 = this.isChildrenDrawingOrderEnabled();
        int var3 = this.getChildCount();

        for (int var4 = var3 - 1; var4 >= 0; --var4) {
            int var5;
            if (var2) {
                var5 = this.getChildDrawingOrder(var3, var4);
            } else {
                var5 = var4;
            }

            var1.add(this.getChildAt(var5));
        }

        Comparator var6 = TOP_SORTED_CHILDREN_COMPARATOR;
        if (var6 != null) {
            Collections.sort(var1, var6);
        }

    }

    @SuppressLint("RestrictedApi")
    public final boolean hasDependencies(View var1) {
        return this.mChildDag.hasOutgoingEdges(var1);
    }

    public boolean isPointInChildBounds(View var1, int var2, int var3) {
        Rect var4 = acquireTempRect();
        this.getDescendantRect(var1, var4);

        boolean var5;
        try {
            var5 = var4.contains(var2, var3);
        } finally {
            releaseTempRect(var4);
        }

        return var5;
    }

    public final void layoutChild(View var1, int var2) {
        SamsungCoordinatorLayout.LayoutParams var3 = (SamsungCoordinatorLayout.LayoutParams) var1.getLayoutParams();
        Rect var4 = acquireTempRect();
        var4.set(this.getPaddingLeft() + var3.leftMargin, this.getPaddingTop() + var3.topMargin, this.getWidth() - this.getPaddingRight() - var3.rightMargin, this.getHeight() - this.getPaddingBottom() - var3.bottomMargin);
        if (this.mLastInsets != null && ViewCompat.getFitsSystemWindows(this) && !ViewCompat.getFitsSystemWindows(var1)) {
            var4.left += this.mLastInsets.getSystemWindowInsetLeft();
            var4.top += this.mLastInsets.getSystemWindowInsetTop();
            var4.right -= this.mLastInsets.getSystemWindowInsetRight();
            var4.bottom -= this.mLastInsets.getSystemWindowInsetBottom();
        }

        Rect var5 = acquireTempRect();
        GravityCompat.apply(resolveGravity(var3.gravity), var1.getMeasuredWidth(), var1.getMeasuredHeight(), var4, var5, var2);
        var1.layout(var5.left, var5.top, var5.right, var5.bottom);
        releaseTempRect(var4);
        releaseTempRect(var5);
    }

    public final void layoutChildWithAnchor(View var1, View var2, int var3) {
        Rect var4 = acquireTempRect();
        Rect var5 = acquireTempRect();

        try {
            this.getDescendantRect(var2, var4);
            this.getDesiredAnchoredChildRect(var1, var3, var4, var5);
            var1.layout(var5.left, var5.top, var5.right, var5.bottom);
        } finally {
            releaseTempRect(var4);
            releaseTempRect(var5);
        }

    }

    public final void layoutChildWithKeyline(View var1, int var2, int var3) {
        SamsungCoordinatorLayout.LayoutParams var4 = (SamsungCoordinatorLayout.LayoutParams) var1.getLayoutParams();
        int var5 = GravityCompat.getAbsoluteGravity(resolveKeylineGravity(var4.gravity), var3);
        int var6 = var5 & 7;
        int var7 = var5 & 112;
        int var8 = this.getWidth();
        int var9 = this.getHeight();
        int var10 = var1.getMeasuredWidth();
        int var11 = var1.getMeasuredHeight();
        var5 = var2;
        if (var3 == 1) {
            var5 = var8 - var2;
        }

        var2 = this.getKeyline(var5) - var10;
        var3 = 0;
        if (var6 != 1) {
            if (var6 == 5) {
                var2 += var10;
            }
        } else {
            var2 += var10 / 2;
        }

        if (var7 != 16) {
            if (var7 == 80) {
                var3 = var11 + 0;
            }
        } else {
            var3 = 0 + var11 / 2;
        }

        var2 = Math.max(this.getPaddingLeft() + var4.leftMargin, Math.min(var2, var8 - this.getPaddingRight() - var10 - var4.rightMargin));
        var3 = Math.max(this.getPaddingTop() + var4.topMargin, Math.min(var3, var9 - this.getPaddingBottom() - var11 - var4.bottomMargin));
        var1.layout(var2, var3, var10 + var2, var11 + var3);
    }

    public final void offsetChildByInset(View var1, Rect var2, int var3) {
        if (ViewCompat.isLaidOut(var1)) {
            if (var1.getWidth() > 0 && var1.getHeight() > 0) {
                SamsungCoordinatorLayout.LayoutParams var4 = (SamsungCoordinatorLayout.LayoutParams) var1.getLayoutParams();
                SamsungCoordinatorLayout.Behavior var5 = var4.getBehavior();
                Rect var6 = acquireTempRect();
                Rect var7 = acquireTempRect();
                var7.set(var1.getLeft(), var1.getTop(), var1.getRight(), var1.getBottom());
                if (var5 != null && var5.getInsetDodgeRect(this, var1, var6)) {
                    if (!var7.contains(var6)) {
                        StringBuilder var12 = new StringBuilder();
                        var12.append("Rect should be within the child's bounds. Rect:");
                        var12.append(var6.toShortString());
                        var12.append(" | Bounds:");
                        var12.append(var7.toShortString());
                        throw new IllegalArgumentException(var12.toString());
                    }
                } else {
                    var6.set(var7);
                }

                releaseTempRect(var7);
                if (var6.isEmpty()) {
                    releaseTempRect(var6);
                    return;
                }

                int var8;
                int var9;
                boolean var13;
                label58:
                {
                    var8 = GravityCompat.getAbsoluteGravity(var4.dodgeInsetEdges, var3);
                    if ((var8 & 48) == 48) {
                        var3 = var6.top - var4.topMargin - var4.mInsetOffsetY;
                        var9 = var2.top;
                        if (var3 < var9) {
                            this.setInsetOffsetY(var1, var9 - var3);
                            var13 = true;
                            break label58;
                        }
                    }

                    var13 = false;
                }

                boolean var14 = var13;
                int var11;
                if ((var8 & 80) == 80) {
                    int var10 = this.getHeight() - var6.bottom - var4.bottomMargin + var4.mInsetOffsetY;
                    var11 = var2.bottom;
                    var14 = var13;
                    if (var10 < var11) {
                        this.setInsetOffsetY(var1, var10 - var11);
                        var14 = true;
                    }
                }

                if (!var14) {
                    this.setInsetOffsetY(var1, 0);
                }

                label50:
                {
                    if ((var8 & 3) == 3) {
                        var3 = var6.left - var4.leftMargin - var4.mInsetOffsetX;
                        var9 = var2.left;
                        if (var3 < var9) {
                            this.setInsetOffsetX(var1, var9 - var3);
                            var13 = true;
                            break label50;
                        }
                    }

                    var13 = false;
                }

                var14 = var13;
                if ((var8 & 5) == 5) {
                    var8 = this.getWidth() - var6.right - var4.rightMargin + var4.mInsetOffsetX;
                    var11 = var2.right;
                    var14 = var13;
                    if (var8 < var11) {
                        this.setInsetOffsetX(var1, var8 - var11);
                        var14 = true;
                    }
                }

                if (!var14) {
                    this.setInsetOffsetX(var1, 0);
                }

                releaseTempRect(var6);
            }

        }
    }

    public void offsetChildToAnchor(View var1, int var2) {
        SamsungCoordinatorLayout.LayoutParams var3 = (SamsungCoordinatorLayout.LayoutParams) var1.getLayoutParams();
        if (var3.mAnchorView != null) {
            Rect var4;
            Rect var5;
            Rect var6;
            int var8;
            int var9;
            boolean var11;
            label27:
            {
                var4 = acquireTempRect();
                var5 = acquireTempRect();
                var6 = acquireTempRect();
                this.getDescendantRect(var3.mAnchorView, var4);
                boolean var7 = false;
                this.getChildRect(var1, false, var5);
                var8 = var1.getMeasuredWidth();
                var9 = var1.getMeasuredHeight();
                this.getDesiredAnchoredChildRectWithoutConstraints(var1, var2, var4, var6, var3, var8, var9);
                if (var6.left == var5.left) {
                    var11 = var7;
                    if (var6.top == var5.top) {
                        break label27;
                    }
                }

                var11 = true;
            }

            this.constrainChildRect(var3, var6, var8, var9);
            int var12 = var6.left - var5.left;
            var9 = var6.top - var5.top;
            if (var12 != 0) {
                ViewCompat.offsetLeftAndRight(var1, var12);
            }

            if (var9 != 0) {
                ViewCompat.offsetTopAndBottom(var1, var9);
            }

            if (var11) {
                SamsungCoordinatorLayout.Behavior var10 = var3.getBehavior();
                if (var10 != null) {
                    var10.onDependentViewChanged(this, var1, var3.mAnchorView);
                }
            }

            releaseTempRect(var4);
            releaseTempRect(var5);
            releaseTempRect(var6);
        }

    }

    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.resetTouchBehaviors(false);
        if (this.mNeedsPreDrawListener) {
            if (this.mOnPreDrawListener == null) {
                this.mOnPreDrawListener = new SamsungCoordinatorLayout.OnPreDrawListener();
            }

            this.getViewTreeObserver().addOnPreDrawListener(this.mOnPreDrawListener);
        }

        if (this.mLastInsets == null && ViewCompat.getFitsSystemWindows(this)) {
            ViewCompat.requestApplyInsets(this);
        }

        this.mIsAttachedToWindow = true;
    }

    public final void onChildViewsChanged(int var1) {
        int var2 = ViewCompat.getLayoutDirection(this);
        int var3 = this.mDependencySortedChildren.size();
        Rect var4 = acquireTempRect();
        Rect var5 = acquireTempRect();
        Rect var6 = acquireTempRect();

        for (int var7 = 0; var7 < var3; ++var7) {
            View var8 = (View) this.mDependencySortedChildren.get(var7);
            SamsungCoordinatorLayout.LayoutParams var9 = (SamsungCoordinatorLayout.LayoutParams) var8.getLayoutParams();
            if (var1 != 0 || var8.getVisibility() != View.GONE) {
                int var10;
                for (var10 = 0; var10 < var7; ++var10) {
                    View var11 = (View) this.mDependencySortedChildren.get(var10);
                    if (var9.mAnchorDirectChild == var11) {
                        this.offsetChildToAnchor(var8, var2);
                    }
                }

                this.getChildRect(var8, true, var5);
                if (var9.insetEdge != 0 && !var5.isEmpty()) {
                    var10 = GravityCompat.getAbsoluteGravity(var9.insetEdge, var2);
                    int var12 = var10 & 112;
                    if (var12 != 48) {
                        if (var12 == 80) {
                            var4.bottom = Math.max(var4.bottom, this.getHeight() - var5.top);
                        }
                    } else {
                        var4.top = Math.max(var4.top, var5.bottom);
                    }

                    var10 &= 7;
                    if (var10 != 3) {
                        if (var10 == 5) {
                            var4.right = Math.max(var4.right, this.getWidth() - var5.left);
                        }
                    } else {
                        var4.left = Math.max(var4.left, var5.right);
                    }
                }

                if (var9.dodgeInsetEdges != 0 && var8.getVisibility() == View.VISIBLE) {
                    this.offsetChildByInset(var8, var4, var2);
                }

                if (var1 != 2) {
                    this.getLastChildRect(var8, var6);
                    if (var6.equals(var5)) {
                        continue;
                    }

                    this.recordLastChildRect(var8, var5);
                }

                for (var10 = var7 + 1; var10 < var3; ++var10) {
                    View var15 = (View) this.mDependencySortedChildren.get(var10);
                    SamsungCoordinatorLayout.LayoutParams var13 = (SamsungCoordinatorLayout.LayoutParams) var15.getLayoutParams();
                    SamsungCoordinatorLayout.Behavior var16 = var13.getBehavior();
                    if (var16 != null && var16.layoutDependsOn(this, var15, var8)) {
                        if (var1 == 0 && var13.getChangedAfterNestedScroll()) {
                            var13.resetChangedAfterNestedScroll();
                        } else {
                            boolean var14;
                            if (var1 != 2) {
                                var14 = var16.onDependentViewChanged(this, var15, var8);
                            } else {
                                var16.onDependentViewRemoved(this, var15, var8);
                                var14 = true;
                            }

                            if (var1 == 1) {
                                var13.setChangedAfterNestedScroll(var14);
                            }
                        }
                    }
                }
            }
        }

        releaseTempRect(var4);
        releaseTempRect(var5);
        releaseTempRect(var6);
    }

    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.resetTouchBehaviors(false);
        if (this.mNeedsPreDrawListener && this.mOnPreDrawListener != null) {
            this.getViewTreeObserver().removeOnPreDrawListener(this.mOnPreDrawListener);
        }

        View var1 = this.mNestedScrollingTarget;
        if (var1 != null) {
            this.mLastNestedScrollingChild = var1;
            this.onStopNestedScroll(var1);
        }

        this.mIsAttachedToWindow = false;
    }

    public void onDraw(Canvas var1) {
        super.onDraw(var1);
        if (this.mDrawStatusBarBackground && this.mStatusBarBackground != null) {
            WindowInsetsCompat var2 = this.mLastInsets;
            int var3;
            if (var2 != null) {
                var3 = var2.getSystemWindowInsetTop();
            } else {
                var3 = 0;
            }

            if (var3 > 0) {
                this.mStatusBarBackground.setBounds(0, 0, this.getWidth(), var3);
                this.mStatusBarBackground.draw(var1);
            }
        }

    }

    public boolean onInterceptTouchEvent(MotionEvent var1) {
        int var2 = var1.getActionMasked();
        if (var2 == 0) {
            this.resetTouchBehaviors(true);
        }

        boolean var3 = this.performIntercept(var1, 0);
        if (var2 == 1 || var2 == 3) {
            this.resetTouchBehaviors(true);
        }

        return var3;
    }

    public void onLayout(boolean var1, int var2, int var3, int var4, int var5) {
        var4 = ViewCompat.getLayoutDirection(this);
        var3 = this.mDependencySortedChildren.size();

        for (var2 = 0; var2 < var3; ++var2) {
            View var6 = (View) this.mDependencySortedChildren.get(var2);
            if (var6.getVisibility() != View.GONE) {
                SamsungCoordinatorLayout.Behavior var7 = ((SamsungCoordinatorLayout.LayoutParams) var6.getLayoutParams()).getBehavior();
                if (var7 == null || !var7.onLayoutChild(this, var6, var4)) {
                    this.onLayoutChild(var6, var4);
                }
            }
        }

    }

    public void onLayoutChild(View var1, int var2) {
        SamsungCoordinatorLayout.LayoutParams var3 = (SamsungCoordinatorLayout.LayoutParams) var1.getLayoutParams();
        if (!var3.checkAnchorChanged()) {
            View var4 = var3.mAnchorView;
            if (var4 != null) {
                this.layoutChildWithAnchor(var1, var4, var2);
            } else {
                int var5 = var3.keyline;
                if (var5 >= 0) {
                    this.layoutChildWithKeyline(var1, var5, var2);
                } else {
                    this.layoutChild(var1, var2);
                }
            }

        } else {
            throw new IllegalStateException("An anchor may not be changed after CoordinatorLayout measurement begins before layout is complete.");
        }
    }

    public void onMeasure(int var1, int var2) {
        this.prepareChildren();
        this.ensurePreDrawListener();
        int var3 = this.getPaddingLeft();
        int var4 = this.getPaddingTop();
        int var5 = this.getPaddingRight();
        int var6 = this.getPaddingBottom();
        int var7 = ViewCompat.getLayoutDirection(this);
        boolean var8;
        if (var7 == 1) {
            var8 = true;
        } else {
            var8 = false;
        }

        int var9 = MeasureSpec.getMode(var1);
        int var10 = MeasureSpec.getSize(var1);
        int var11 = MeasureSpec.getMode(var2);
        int var12 = MeasureSpec.getSize(var2);
        int var13 = this.getSuggestedMinimumWidth();
        int var14 = this.getSuggestedMinimumHeight();
        boolean var15;
        if (this.mLastInsets != null && ViewCompat.getFitsSystemWindows(this)) {
            var15 = true;
        } else {
            var15 = false;
        }

        int var16 = this.mDependencySortedChildren.size();
        int var17 = 0;

        for (int var18 = 0; var18 < var16; ++var18) {
            View var19 = (View) this.mDependencySortedChildren.get(var18);
            if (var19.getVisibility() != View.GONE) {
                SamsungCoordinatorLayout.LayoutParams var20;
                int var21;
                int var22;
                label73:
                {
                    var20 = (SamsungCoordinatorLayout.LayoutParams) var19.getLayoutParams();
                    var21 = var20.keyline;
                    if (var21 >= 0 && var9 != 0) {
                        var22 = this.getKeyline(var21);
                        var21 = GravityCompat.getAbsoluteGravity(resolveKeylineGravity(var20.gravity), var7) & 7;
                        if (var21 == 3 && !var8 || var21 == 5 && var8) {
                            var21 = Math.max(0, var10 - var5 - var22);
                            break label73;
                        }

                        if (var21 == 5 && !var8 || var21 == 3 && var8) {
                            var21 = Math.max(0, var22 - var3);
                            break label73;
                        }
                    }

                    var21 = 0;
                }

                int var23 = var14;
                if (var15 && !ViewCompat.getFitsSystemWindows(var19)) {
                    int var24 = this.mLastInsets.getSystemWindowInsetLeft();
                    var22 = this.mLastInsets.getSystemWindowInsetRight();
                    int var25 = this.mLastInsets.getSystemWindowInsetTop();
                    var14 = this.mLastInsets.getSystemWindowInsetBottom();
                    var22 = MeasureSpec.makeMeasureSpec(var10 - (var24 + var22), var9);
                    var14 = MeasureSpec.makeMeasureSpec(var12 - (var25 + var14), var11);
                } else {
                    var22 = var1;
                    var14 = var2;
                }

                SamsungCoordinatorLayout.Behavior var26 = var20.getBehavior();
                if (var26 == null || !var26.onMeasureChild(this, var19, var22, var21, var14, 0)) {
                    this.onMeasureChild(var19, var22, var21, var14, 0);
                }

                var13 = Math.max(var13, var3 + var5 + var19.getMeasuredWidth() + var20.leftMargin + var20.rightMargin);
                var14 = Math.max(var23, var4 + var6 + var19.getMeasuredHeight() + var20.topMargin + var20.bottomMargin);
                var17 = View.combineMeasuredStates(var17, var19.getMeasuredState());
            }
        }

        this.setMeasuredDimension(View.resolveSizeAndState(var13, var1, -16777216 & var17), View.resolveSizeAndState(var14, var2, var17 << 16));
    }

    public void onMeasureChild(View var1, int var2, int var3, int var4, int var5) {
        this.measureChildWithMargins(var1, var2, var3, var4, var5);
    }

    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        boolean handled = false;
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View view = getChildAt(i);
            if (view.getVisibility() == GONE) {
                continue;
            }
            final LayoutParams lp = (LayoutParams) view.getLayoutParams();
            if (!lp.isNestedScrollAccepted(ViewCompat.TYPE_TOUCH)) {
                continue;
            }
            final Behavior viewBehavior = lp.getBehavior();
            if (viewBehavior != null) {
                handled |= viewBehavior.onNestedFling(this, view, target, velocityX, velocityY, consumed);
            }
        }
        if (handled) {
            onChildViewsChanged(1);
        }
        return handled;
    }

    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        boolean handled = false;
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View view = getChildAt(i);
            if (view.getVisibility() == GONE) {
                continue;
            }
            final LayoutParams lp = (LayoutParams) view.getLayoutParams();
            if (!lp.isNestedScrollAccepted(ViewCompat.TYPE_TOUCH)) {
                continue;
            }
            final Behavior viewBehavior = lp.getBehavior();
            if (viewBehavior != null) {
                handled |= viewBehavior.onNestedPreFling(this, view, target, velocityX, velocityY);
            }
        }
        return handled;
    }

    @SuppressLint("WrongConstant")
    public void onNestedPreScroll(View var1, int var2, int var3, int[] var4) {
        this.onNestedPreScroll(var1, var2, var3, var4, 0);
    }

    public void onNestedPreScroll(View var1, int var2, int var3, int[] var4, int var5) {
        int var6 = this.getChildCount();
        byte var7 = 0;
        int var10 = var7;
        int var11 = var7;

        int var8;
        for (int var12 = var7; var12 < var6; var10 = var8) {
            View var13 = this.getChildAt(var12);
            int var9;
            if (var13.getVisibility() == GONE) {
                var9 = var11;
                var8 = var10;
            } else {
                SamsungCoordinatorLayout.LayoutParams var14 = (SamsungCoordinatorLayout.LayoutParams) var13.getLayoutParams();
                if (!var14.isNestedScrollAccepted(var5)) {
                    var9 = var11;
                    var8 = var10;
                } else {
                    SamsungCoordinatorLayout.Behavior var16 = var14.getBehavior();
                    var9 = var11;
                    var8 = var10;
                    if (var16 != null) {
                        int[] var15 = this.mBehaviorConsumed;
                        var15[0] = 0;
                        var15[1] = 0;
                        var16.onNestedPreScroll(this, var13, var1, var2, var3, var15, var5);
                        if (var2 > 0) {
                            var8 = Math.max(var11, this.mBehaviorConsumed[0]);
                        } else {
                            var8 = Math.min(var11, this.mBehaviorConsumed[0]);
                        }

                        if (var3 > 0) {
                            var9 = Math.max(var10, this.mBehaviorConsumed[1]);
                        } else {
                            var9 = Math.min(var10, this.mBehaviorConsumed[1]);
                        }

                        var10 = var8;
                        var8 = var9;
                        var7 = 1;
                        var9 = var10;
                    }
                }
            }

            ++var12;
            var11 = var9;
        }

        var4[0] = var11;
        var4[1] = var10;
        if (var7 != 0) {
            this.onChildViewsChanged(1);
        }

    }

    @SuppressLint("WrongConstant")
    public void onNestedScroll(View var1, int var2, int var3, int var4, int var5) {
        this.onNestedScroll(var1, var2, var3, var4, var5, 0);
    }

    @SuppressLint("WrongConstant")
    public void onNestedScroll(View var1, int var2, int var3, int var4, int var5, int var6) {
        this.onNestedScroll(var1, var2, var3, var4, var5, 0, this.mNestedScrollingV2ConsumedCompat);
    }

    public void onNestedScroll(View var1, int var2, int var3, int var4, int var5, int var6, int[] var7) {
        int var8 = this.getChildCount();
        byte var9 = 0;
        int var12 = var9;
        int var13 = var9;

        int var10;
        for (int var14 = var9; var14 < var8; var12 = var10) {
            View var15 = this.getChildAt(var14);
            int var11;
            if (var15.getVisibility() == GONE) {
                var11 = var13;
                var10 = var12;
            } else {
                SamsungCoordinatorLayout.LayoutParams var16 = (SamsungCoordinatorLayout.LayoutParams) var15.getLayoutParams();
                if (!var16.isNestedScrollAccepted(var6)) {
                    var11 = var13;
                    var10 = var12;
                } else {
                    SamsungCoordinatorLayout.Behavior var17 = var16.getBehavior();
                    var11 = var13;
                    var10 = var12;
                    if (var17 != null) {
                        int[] var18 = this.mBehaviorConsumed;
                        var18[0] = 0;
                        var18[1] = 0;
                        var17.onNestedScroll(this, var15, var1, var2, var3, var4, var5, var6, var18);
                        if (var4 > 0) {
                            var10 = Math.max(var13, this.mBehaviorConsumed[0]);
                        } else {
                            var10 = Math.min(var13, this.mBehaviorConsumed[0]);
                        }

                        if (var5 > 0) {
                            var11 = Math.max(var12, this.mBehaviorConsumed[1]);
                        } else {
                            var11 = Math.min(var12, this.mBehaviorConsumed[1]);
                        }

                        var12 = var10;
                        var10 = var11;
                        var9 = 1;
                        var11 = var12;
                    }
                }
            }

            ++var14;
            var13 = var11;
        }

        var7[0] += var13;
        var7[1] += var12;
        if (var9 != 0) {
            this.onChildViewsChanged(1);
        }

    }

    @SuppressLint("WrongConstant")
    public void onNestedScrollAccepted(View var1, View var2, int var3) {
        this.onNestedScrollAccepted(var1, var2, var3, 0);
    }

    public void onNestedScrollAccepted(View var1, View var2, int var3, int var4) {
        this.mNestedScrollingParentHelper.onNestedScrollAccepted(var1, var2, var3, var4);
        this.mNestedScrollingTarget = var2;
        this.mLastNestedScrollingChild = this.mNestedScrollingTarget;
        int var5 = this.getChildCount();

        for (int var6 = 0; var6 < var5; ++var6) {
            View var7 = this.getChildAt(var6);
            SamsungCoordinatorLayout.LayoutParams var8 = (SamsungCoordinatorLayout.LayoutParams) var7.getLayoutParams();
            if (var8.isNestedScrollAccepted(var4)) {
                SamsungCoordinatorLayout.Behavior var9 = var8.getBehavior();
                if (var9 != null) {
                    var9.onNestedScrollAccepted(this, var7, var1, var2, var3, var4);
                }
            }
        }

    }

    public void onRestoreInstanceState(Parcelable var1) {
        if (!(var1 instanceof SamsungCoordinatorLayout.SavedState)) {
            super.onRestoreInstanceState(var1);
        } else {
            SamsungCoordinatorLayout.SavedState var8 = (SamsungCoordinatorLayout.SavedState) var1;
            super.onRestoreInstanceState(var8.getSuperState());
            SparseArray var9 = var8.behaviorStates;
            int var2 = 0;

            for (int var3 = this.getChildCount(); var2 < var3; ++var2) {
                View var4 = this.getChildAt(var2);
                int var5 = var4.getId();
                SamsungCoordinatorLayout.Behavior var6 = this.getResolvedLayoutParams(var4).getBehavior();
                if (var5 != -1 && var6 != null) {
                    Parcelable var7 = (Parcelable) var9.get(var5);
                    if (var7 != null) {
                        var6.onRestoreInstanceState(this, var4, var7);
                    }
                }
            }

        }
    }

    public Parcelable onSaveInstanceState() {
        SamsungCoordinatorLayout.SavedState var1 = new SamsungCoordinatorLayout.SavedState(super.onSaveInstanceState());
        SparseArray var2 = new SparseArray();
        int var3 = this.getChildCount();

        for (int var4 = 0; var4 < var3; ++var4) {
            View var5 = this.getChildAt(var4);
            int var6 = var5.getId();
            SamsungCoordinatorLayout.Behavior var7 = ((SamsungCoordinatorLayout.LayoutParams) var5.getLayoutParams()).getBehavior();
            if (var6 != -1 && var7 != null) {
                Parcelable var8 = var7.onSaveInstanceState(this, var5);
                if (var8 != null) {
                    var2.append(var6, var8);
                }
            }
        }

        var1.behaviorStates = var2;
        return var1;
    }

    @SuppressLint("WrongConstant")
    public boolean onStartNestedScroll(View var1, View var2, int var3) {
        return this.onStartNestedScroll(var1, var2, var3, 0);
    }

    public boolean onStartNestedScroll(View child, View target, int axes, int type) {
        boolean handled = false;
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View view = getChildAt(i);
            if (view.getVisibility() == View.GONE) {
                continue;
            }
            final LayoutParams lp = (LayoutParams) view.getLayoutParams();
            final Behavior viewBehavior = lp.getBehavior();
            if (viewBehavior != null) {
                final boolean accepted = viewBehavior.onStartNestedScroll(this, view, child, target, axes, type);
                handled |= accepted;
                lp.setNestedScrollAccepted(type, accepted);
            } else {
                lp.setNestedScrollAccepted(type, false);
            }
        }
        return handled;
    }

    @SuppressLint("WrongConstant")
    public void onStopNestedScroll(View var1) {
        this.onStopNestedScroll(var1, 0);
    }

    public void onStopNestedScroll(View var1, int var2) {
        this.mNestedScrollingParentHelper.onStopNestedScroll(var1, var2);
        this.mLastNestedScrollingChild = var1;
        int var3 = this.getChildCount();

        for (int var4 = 0; var4 < var3; ++var4) {
            View var5 = this.getChildAt(var4);
            SamsungCoordinatorLayout.LayoutParams var6 = (SamsungCoordinatorLayout.LayoutParams) var5.getLayoutParams();
            if (var6.isNestedScrollAccepted(var2)) {
                SamsungCoordinatorLayout.Behavior var7 = var6.getBehavior();
                if (var7 != null) {
                    var7.onStopNestedScroll(this, var5, var1, var2);
                }

                var6.resetNestedScroll(var2);
                var6.resetChangedAfterNestedScroll();
            }
        }

        this.mNestedScrollingTarget = null;
    }

    public boolean onTouchEvent(MotionEvent ev) {
        boolean handled = false;
        boolean cancelSuper = false;
        MotionEvent cancelEvent = null;
        final int action = ev.getActionMasked();
        if (mBehaviorTouchView != null || (cancelSuper = performIntercept(ev, 1))) {
            final LayoutParams lp = (LayoutParams) mBehaviorTouchView.getLayoutParams();
            final Behavior b = lp.getBehavior();
            if (b != null) {
                handled = b.onTouchEvent(this, mBehaviorTouchView, ev);
            }
        }
        if (mBehaviorTouchView == null) {
            handled |= super.onTouchEvent(ev);
        } else if (cancelSuper) {
            if (cancelEvent == null) {
                final long now = SystemClock.uptimeMillis();
                cancelEvent = MotionEvent.obtain(now, now, MotionEvent.ACTION_CANCEL, 0.0f, 0.0f, 0);
            }
            super.onTouchEvent(cancelEvent);
        }
        if (cancelEvent != null) {
            cancelEvent.recycle();
        }
        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            resetTouchBehaviors(false);
        }
        return handled;
    }

    public final boolean performIntercept(MotionEvent ev, final int type) {
        boolean intercepted = false;
        boolean newBlock = false;
        MotionEvent cancelEvent = null;
        final int action = ev.getActionMasked();
        final List<View> topmostChildList = mTempList1;
        getTopSortedChildren(topmostChildList);
        final int childCount = topmostChildList.size();
        for (int i = 0; i < childCount; i++) {
            final View child = topmostChildList.get(i);
            final LayoutParams lp = (LayoutParams) child.getLayoutParams();
            final Behavior b = lp.getBehavior();
            if ((intercepted || newBlock) && action != MotionEvent.ACTION_DOWN) {
                if (b != null) {
                    if (cancelEvent == null) {
                        final long now = SystemClock.uptimeMillis();
                        cancelEvent = MotionEvent.obtain(now, now, MotionEvent.ACTION_CANCEL, 0.0f, 0.0f, 0);
                    }
                    switch (type) {
                        case 0:
                            b.onInterceptTouchEvent(this, child, cancelEvent);
                            break;
                        case 1:
                            b.onTouchEvent(this, child, cancelEvent);
                            break;
                    }
                }
                continue;
            }
            if (!intercepted && b != null) {
                switch (type) {
                    case 0:
                        intercepted = b.onInterceptTouchEvent(this, child, ev);
                        break;
                    case 1:
                        intercepted = b.onTouchEvent(this, child, ev);
                        break;
                }
                if (intercepted) {
                    mBehaviorTouchView = child;
                }
            }
            final boolean wasBlocking = lp.didBlockInteraction();
            final boolean isBlocking = lp.isBlockingInteractionBelow(this, child);
            newBlock = isBlocking && !wasBlocking;
            if (isBlocking && !newBlock) {
                break;
            }
        }
        topmostChildList.clear();
        return intercepted;
    }

    @SuppressLint("RestrictedApi")
    public final void prepareChildren() {
        this.mDependencySortedChildren.clear();
        this.mChildDag.clear();
        int var1 = this.getChildCount();

        for (int var2 = 0; var2 < var1; ++var2) {
            View var3 = this.getChildAt(var2);
            SamsungCoordinatorLayout.LayoutParams var4 = this.getResolvedLayoutParams(var3);
            var4.findAnchorView(this, var3);
            this.mChildDag.addNode(var3);

            for (int var5 = 0; var5 < var1; ++var5) {
                if (var5 != var2) {
                    View var6 = this.getChildAt(var5);
                    if (var4.dependsOn(this, var3, var6)) {
                        if (!this.mChildDag.contains(var6)) {
                            this.mChildDag.addNode(var6);
                        }

                        this.mChildDag.addEdge(var6, var3);
                    }
                }
            }
        }

        this.mDependencySortedChildren.addAll(this.mChildDag.getSortedList());
        Collections.reverse(this.mDependencySortedChildren);
    }

    public void recordLastChildRect(View var1, Rect var2) {
        ((SamsungCoordinatorLayout.LayoutParams) var1.getLayoutParams()).setLastChildRect(var2);
    }

    public void removePreDrawListener() {
        if (this.mIsAttachedToWindow && this.mOnPreDrawListener != null) {
            this.getViewTreeObserver().removeOnPreDrawListener(this.mOnPreDrawListener);
        }

        this.mNeedsPreDrawListener = false;
    }

    public boolean requestChildRectangleOnScreen(View var1, Rect var2, boolean var3) {
        SamsungCoordinatorLayout.Behavior var4 = ((SamsungCoordinatorLayout.LayoutParams) var1.getLayoutParams()).getBehavior();
        return var4 != null && var4.onRequestChildRectangleOnScreen(this, var1, var2, var3) ? true : super.requestChildRectangleOnScreen(var1, var2, var3);
    }

    public void requestDisallowInterceptTouchEvent(boolean var1) {
        super.requestDisallowInterceptTouchEvent(var1);
        if (var1 && !this.mDisallowInterceptReset) {
            this.resetTouchBehaviors(false);
            this.mDisallowInterceptReset = true;
        }

    }

    public final void resetTouchBehaviors(boolean var1) {
        int var2 = this.getChildCount();

        int var3;
        for (var3 = 0; var3 < var2; ++var3) {
            View var4 = this.getChildAt(var3);
            SamsungCoordinatorLayout.Behavior var5 = ((SamsungCoordinatorLayout.LayoutParams) var4.getLayoutParams()).getBehavior();
            if (var5 != null) {
                long var6 = SystemClock.uptimeMillis();
                MotionEvent var8 = MotionEvent.obtain(var6, var6, 3, 0.0F, 0.0F, 0);
                if (var1) {
                    var5.onInterceptTouchEvent(this, var4, var8);
                } else {
                    var5.onTouchEvent(this, var4, var8);
                }

                var8.recycle();
            }
        }

        for (var3 = 0; var3 < var2; ++var3) {
            ((SamsungCoordinatorLayout.LayoutParams) this.getChildAt(var3).getLayoutParams()).resetTouchBehaviorTracking();
        }

        this.mBehaviorTouchView = null;
        this.mDisallowInterceptReset = false;
    }

    public void setFitsSystemWindows(boolean var1) {
        super.setFitsSystemWindows(var1);
        this.setupForInsets();
    }

    public final void setInsetOffsetX(View var1, int var2) {
        SamsungCoordinatorLayout.LayoutParams var3 = (SamsungCoordinatorLayout.LayoutParams) var1.getLayoutParams();
        int var4 = var3.mInsetOffsetX;
        if (var4 != var2) {
            ViewCompat.offsetLeftAndRight(var1, var2 - var4);
            var3.mInsetOffsetX = var2;
        }

    }

    public final void setInsetOffsetY(View var1, int var2) {
        SamsungCoordinatorLayout.LayoutParams var3 = (SamsungCoordinatorLayout.LayoutParams) var1.getLayoutParams();
        int var4 = var3.mInsetOffsetY;
        if (var4 != var2) {
            ViewCompat.offsetTopAndBottom(var1, var2 - var4);
            var3.mInsetOffsetY = var2;
        }

    }

    public void setOnHierarchyChangeListener(OnHierarchyChangeListener var1) {
        this.mOnHierarchyChangeListener = var1;
    }

    public void setStatusBarBackgroundColor(int var1) {
        this.setStatusBarBackground(new ColorDrawable(var1));
    }

    public void setStatusBarBackgroundResource(int var1) {
        Drawable var2;
        if (var1 != 0) {
            var2 = ContextCompat.getDrawable(this.getContext(), var1);
        } else {
            var2 = null;
        }

        this.setStatusBarBackground(var2);
    }

    public void setVisibility(int var1) {
        super.setVisibility(var1);
        boolean var2;
        if (var1 == 0) {
            var2 = true;
        } else {
            var2 = false;
        }

        Drawable var3 = this.mStatusBarBackground;
        if (var3 != null && var3.isVisible() != var2) {
            this.mStatusBarBackground.setVisible(var2, false);
        }

    }

    public final WindowInsetsCompat setWindowInsets(WindowInsetsCompat var1) {
        WindowInsetsCompat var2 = var1;
        if (!ObjectsCompat.equals(this.mLastInsets, var1)) {
            this.mLastInsets = var1;
            boolean var3 = true;
            boolean var4;
            if (var1 != null && var1.getSystemWindowInsetTop() > 0) {
                var4 = true;
            } else {
                var4 = false;
            }

            this.mDrawStatusBarBackground = var4;
            if (!this.mDrawStatusBarBackground && this.getBackground() == null) {
                var4 = var3;
            } else {
                var4 = false;
            }

            this.setWillNotDraw(var4);
            var2 = this.dispatchApplyWindowInsetsToBehaviors(var1);
            this.requestLayout();
        }

        return var2;
    }

    public final void setupForInsets() {
        if (Build.VERSION.SDK_INT >= 21) {
            if (ViewCompat.getFitsSystemWindows(this)) {
                if (this.mApplyWindowInsetsListener == null) {
                    this.mApplyWindowInsetsListener = new androidx.core.view.OnApplyWindowInsetsListener() {
                        public WindowInsetsCompat onApplyWindowInsets(View var1, WindowInsetsCompat var2) {
                            return SamsungCoordinatorLayout.this.setWindowInsets(var2);
                        }
                    };
                }

                ViewCompat.setOnApplyWindowInsetsListener(this, this.mApplyWindowInsetsListener);
                this.setSystemUiVisibility(1280);
            } else {
                ViewCompat.setOnApplyWindowInsetsListener(this, null);
            }

        }
    }

    public boolean verifyDrawable(Drawable var1) {
        boolean var2;
        if (!super.verifyDrawable(var1) && var1 != this.mStatusBarBackground) {
            var2 = false;
        } else {
            var2 = true;
        }

        return var2;
    }

    public interface AttachedBehavior {
        SamsungCoordinatorLayout.Behavior getBehavior();
    }

    @Deprecated
    @Retention(RetentionPolicy.RUNTIME)
    public @interface DefaultBehavior {
        Class<? extends SamsungCoordinatorLayout.Behavior> value();
    }

    public abstract static class Behavior<V extends View> {
        public Behavior() {
        }

        public Behavior(Context var1, AttributeSet var2) {
        }

        protected boolean dispatchGenericMotionEvent(MotionEvent motionEvent) {
            return false;
        }

        public boolean blocksInteractionBelow(SamsungCoordinatorLayout var1, V var2) {
            boolean var3;
            if (this.getScrimOpacity(var1, var2) > 0.0F) {
                var3 = true;
            } else {
                var3 = false;
            }

            return var3;
        }

        public boolean getInsetDodgeRect(SamsungCoordinatorLayout var1, V var2, Rect var3) {
            return false;
        }

        public int getScrimColor(SamsungCoordinatorLayout var1, V var2) {
            return -16777216;
        }

        public float getScrimOpacity(SamsungCoordinatorLayout var1, V var2) {
            return 0.0F;
        }

        public boolean layoutDependsOn(SamsungCoordinatorLayout var1, V var2, View var3) {
            return false;
        }

        public WindowInsetsCompat onApplyWindowInsets(SamsungCoordinatorLayout var1, V var2, WindowInsetsCompat var3) {
            return var3;
        }

        public void onAttachedToLayoutParams(SamsungCoordinatorLayout.LayoutParams var1) {
        }

        public boolean onDependentViewChanged(SamsungCoordinatorLayout var1, V var2, View var3) {
            return false;
        }

        public void onDependentViewRemoved(SamsungCoordinatorLayout var1, V var2, View var3) {
        }

        public void onDetachedFromLayoutParams() {
        }

        public boolean onInterceptTouchEvent(SamsungCoordinatorLayout var1, V var2, MotionEvent var3) {
            return false;
        }

        public boolean onLayoutChild(SamsungCoordinatorLayout var1, V var2, int var3) {
            return false;
        }

        public boolean onMeasureChild(SamsungCoordinatorLayout var1, V var2, int var3, int var4, int var5, int var6) {
            return false;
        }

        public boolean onNestedFling(SamsungCoordinatorLayout var1, V var2, View var3, float var4, float var5, boolean var6) {
            return false;
        }

        public boolean onNestedPreFling(SamsungCoordinatorLayout var1, V var2, View var3, float var4, float var5) {
            return false;
        }

        @Deprecated
        public void onNestedPreScroll(SamsungCoordinatorLayout var1, V var2, View var3, int var4, int var5, int[] var6) {
        }

        public void onNestedPreScroll(SamsungCoordinatorLayout var1, V var2, View var3, int var4, int var5, int[] var6, int var7) {
            if (var7 == 0) {
                this.onNestedPreScroll(var1, var2, var3, var4, var5, var6);
            }

        }

        @Deprecated
        public void onNestedScroll(SamsungCoordinatorLayout var1, V var2, View var3, int var4, int var5, int var6, int var7) {
        }

        @Deprecated
        public void onNestedScroll(SamsungCoordinatorLayout var1, V var2, View var3, int var4, int var5, int var6, int var7, int var8) {
            if (var8 == 0) {
                this.onNestedScroll(var1, var2, var3, var4, var5, var6, var7);
            }

        }

        public void onNestedScroll(SamsungCoordinatorLayout var1, V var2, View var3, int var4, int var5, int var6, int var7, int var8, int[] var9) {
            var9[0] += var6;
            var9[1] += var7;
            this.onNestedScroll(var1, var2, var3, var4, var5, var6, var7, var8);
        }

        @Deprecated
        public void onNestedScrollAccepted(SamsungCoordinatorLayout var1, V var2, View var3, View var4, int var5) {
        }

        public void onNestedScrollAccepted(SamsungCoordinatorLayout var1, V var2, View var3, View var4, int var5, int var6) {
            if (var6 == 0) {
                this.onNestedScrollAccepted(var1, var2, var3, var4, var5);
            }

        }

        public boolean onRequestChildRectangleOnScreen(SamsungCoordinatorLayout var1, V var2, Rect var3, boolean var4) {
            return false;
        }

        public void onRestoreInstanceState(SamsungCoordinatorLayout var1, V var2, Parcelable var3) {
        }

        public Parcelable onSaveInstanceState(SamsungCoordinatorLayout var1, V var2) {
            return BaseSavedState.EMPTY_STATE;
        }

        @Deprecated
        public boolean onStartNestedScroll(SamsungCoordinatorLayout var1, V var2, View var3, View var4, int var5) {
            return false;
        }

        public boolean onStartNestedScroll(SamsungCoordinatorLayout var1, V var2, View var3, View var4, int var5, int var6) {
            return var6 == 0 ? this.onStartNestedScroll(var1, var2, var3, var4, var5) : false;
        }

        @Deprecated
        public void onStopNestedScroll(SamsungCoordinatorLayout var1, V var2, View var3) {
        }

        public void onStopNestedScroll(SamsungCoordinatorLayout var1, V var2, View var3, int var4) {
            if (var4 == 0) {
                this.onStopNestedScroll(var1, var2, var3);
            }

        }

        public boolean onTouchEvent(SamsungCoordinatorLayout var1, V var2, MotionEvent var3) {
            return false;
        }
    }

    public static class LayoutParams extends MarginLayoutParams {
        public final Rect mLastChildRect = new Rect();
        public int anchorGravity = 0;
        public int dodgeInsetEdges = 0;
        public int gravity = 0;
        public int insetEdge = 0;
        public int keyline = -1;
        public View mAnchorDirectChild;
        public int mAnchorId = -1;
        public View mAnchorView;
        public SamsungCoordinatorLayout.Behavior mBehavior;
        public boolean mBehaviorResolved = false;
        public Object mBehaviorTag;
        public boolean mDidAcceptNestedScrollNonTouch;
        public boolean mDidAcceptNestedScrollTouch;
        public boolean mDidBlockInteraction;
        public boolean mDidChangeAfterNestedScroll;
        public int mInsetOffsetX;
        public int mInsetOffsetY;

        public LayoutParams(int var1, int var2) {
            super(var1, var2);
        }

        public LayoutParams(Context var1, AttributeSet var2) {
            super(var1, var2);
            TypedArray var3 = var1.obtainStyledAttributes(var2, R.styleable.SamsungCoordinatorLayout_Layout);
            this.gravity = var3.getInteger(R.styleable.SamsungCoordinatorLayout_Layout_android_layout_gravity, 0);
            this.mAnchorId = var3.getResourceId(R.styleable.SamsungCoordinatorLayout_Layout_layout_anchor, -1);
            this.anchorGravity = var3.getInteger(R.styleable.SamsungCoordinatorLayout_Layout_layout_anchorGravity, 0);
            this.keyline = var3.getInteger(R.styleable.SamsungCoordinatorLayout_Layout_layout_keyline, -1);
            this.insetEdge = var3.getInt(R.styleable.SamsungCoordinatorLayout_Layout_layout_insetEdge, 0);
            this.dodgeInsetEdges = var3.getInt(R.styleable.SamsungCoordinatorLayout_Layout_layout_dodgeInsetEdges, 0);
            this.mBehaviorResolved = var3.hasValue(R.styleable.SamsungCoordinatorLayout_Layout_layout_behavior);
            if (this.mBehaviorResolved) {
                this.mBehavior = SamsungCoordinatorLayout.parseBehavior(var1, var2, var3.getString(R.styleable.SamsungCoordinatorLayout_Layout_layout_behavior));
            }

            var3.recycle();
            SamsungCoordinatorLayout.Behavior var4 = this.mBehavior;
            if (var4 != null) {
                var4.onAttachedToLayoutParams(this);
            }

        }

        public LayoutParams(android.view.ViewGroup.LayoutParams var1) {
            super(var1);
        }

        public LayoutParams(MarginLayoutParams var1) {
            super(var1);
        }

        public LayoutParams(SamsungCoordinatorLayout.LayoutParams var1) {
            super(var1);
        }

        public boolean checkAnchorChanged() {
            boolean var1;
            if (this.mAnchorView == null && this.mAnchorId != -1) {
                var1 = true;
            } else {
                var1 = false;
            }

            return var1;
        }

        public boolean dependsOn(SamsungCoordinatorLayout var1, View var2, View var3) {
            boolean var5;
            if (var3 != this.mAnchorDirectChild && !this.shouldDodge(var3, ViewCompat.getLayoutDirection(var1))) {
                SamsungCoordinatorLayout.Behavior var4 = this.mBehavior;
                if (var4 == null || !var4.layoutDependsOn(var1, var2, var3)) {
                    var5 = false;
                    return var5;
                }
            }

            var5 = true;
            return var5;
        }

        public boolean didBlockInteraction() {
            if (this.mBehavior == null) {
                this.mDidBlockInteraction = false;
            }

            return this.mDidBlockInteraction;
        }

        public View findAnchorView(SamsungCoordinatorLayout var1, View var2) {
            if (this.mAnchorId == -1) {
                this.mAnchorDirectChild = null;
                this.mAnchorView = null;
                return null;
            } else {
                if (this.mAnchorView == null || !this.verifyAnchorView(var2, var1)) {
                    this.resolveAnchorView(var2, var1);
                }

                return this.mAnchorView;
            }
        }

        public int getAnchorId() {
            return this.mAnchorId;
        }

        public SamsungCoordinatorLayout.Behavior getBehavior() {
            return this.mBehavior;
        }

        public void setBehavior(SamsungCoordinatorLayout.Behavior var1) {
            SamsungCoordinatorLayout.Behavior var2 = this.mBehavior;
            if (var2 != var1) {
                if (var2 != null) {
                    var2.onDetachedFromLayoutParams();
                }

                this.mBehavior = var1;
                this.mBehaviorTag = null;
                this.mBehaviorResolved = true;
                if (var1 != null) {
                    var1.onAttachedToLayoutParams(this);
                }
            }

        }

        public boolean getChangedAfterNestedScroll() {
            return this.mDidChangeAfterNestedScroll;
        }

        public void setChangedAfterNestedScroll(boolean var1) {
            this.mDidChangeAfterNestedScroll = var1;
        }

        public Rect getLastChildRect() {
            return this.mLastChildRect;
        }

        public void setLastChildRect(Rect var1) {
            this.mLastChildRect.set(var1);
        }

        public boolean isBlockingInteractionBelow(SamsungCoordinatorLayout var1, View var2) {
            boolean var3 = this.mDidBlockInteraction;
            if (var3) {
                return true;
            } else {
                SamsungCoordinatorLayout.Behavior var4 = this.mBehavior;
                boolean var5;
                if (var4 != null) {
                    var5 = var4.blocksInteractionBelow(var1, var2);
                } else {
                    var5 = false;
                }

                var5 |= var3;
                this.mDidBlockInteraction = var5;
                return var5;
            }
        }

        public boolean isNestedScrollAccepted(int var1) {
            if (var1 != 0) {
                return var1 != 1 ? false : this.mDidAcceptNestedScrollNonTouch;
            } else {
                return this.mDidAcceptNestedScrollTouch;
            }
        }

        public void resetChangedAfterNestedScroll() {
            this.mDidChangeAfterNestedScroll = false;
        }

        public void resetNestedScroll(int var1) {
            this.setNestedScrollAccepted(var1, false);
        }

        public void resetTouchBehaviorTracking() {
            this.mDidBlockInteraction = false;
        }

        public final void resolveAnchorView(View var1, SamsungCoordinatorLayout var2) {
            this.mAnchorView = var2.findViewById(this.mAnchorId);
            View var3 = this.mAnchorView;
            if (var3 == null) {
                if (var2.isInEditMode()) {
                    this.mAnchorDirectChild = null;
                    this.mAnchorView = null;
                } else {
                    StringBuilder var5 = new StringBuilder();
                    var5.append("Could not find CoordinatorLayout descendant view with id ");
                    var5.append(var2.getResources().getResourceName(this.mAnchorId));
                    var5.append(" to anchor view ");
                    var5.append(var1);
                    throw new IllegalStateException(var5.toString());
                }
            } else if (var3 == var2) {
                if (var2.isInEditMode()) {
                    this.mAnchorDirectChild = null;
                    this.mAnchorView = null;
                } else {
                    throw new IllegalStateException("View can not be anchored to the the parent CoordinatorLayout");
                }
            } else {
                for (ViewParent var4 = var3.getParent(); var4 != var2 && var4 != null; var4 = var4.getParent()) {
                    if (var4 == var1) {
                        if (var2.isInEditMode()) {
                            this.mAnchorDirectChild = null;
                            this.mAnchorView = null;
                            return;
                        }

                        throw new IllegalStateException("Anchor must not be a descendant of the anchored view");
                    }

                    if (var4 instanceof View) {
                        var3 = (View) var4;
                    }
                }

                this.mAnchorDirectChild = var3;
            }
        }

        public void setNestedScrollAccepted(int var1, boolean var2) {
            if (var1 != 0) {
                if (var1 == 1) {
                    this.mDidAcceptNestedScrollNonTouch = var2;
                }
            } else {
                this.mDidAcceptNestedScrollTouch = var2;
            }

        }

        public final boolean shouldDodge(View var1, int var2) {
            int var3 = GravityCompat.getAbsoluteGravity(((SamsungCoordinatorLayout.LayoutParams) var1.getLayoutParams()).insetEdge, var2);
            boolean var4;
            if (var3 != 0 && (GravityCompat.getAbsoluteGravity(this.dodgeInsetEdges, var2) & var3) == var3) {
                var4 = true;
            } else {
                var4 = false;
            }

            return var4;
        }

        public final boolean verifyAnchorView(View var1, SamsungCoordinatorLayout var2) {
            if (this.mAnchorView.getId() != this.mAnchorId) {
                return false;
            } else {
                View var3 = this.mAnchorView;

                for (ViewParent var4 = var3.getParent(); var4 != var2; var4 = var4.getParent()) {
                    if (var4 == null || var4 == var1) {
                        this.mAnchorDirectChild = null;
                        this.mAnchorView = null;
                        return false;
                    }

                    if (var4 instanceof View) {
                        var3 = (View) var4;
                    }
                }

                this.mAnchorDirectChild = var3;
                return true;
            }
        }
    }

    protected static class SavedState extends AbsSavedState {
        public static final Creator<SamsungCoordinatorLayout.SavedState> CREATOR = new ClassLoaderCreator<SamsungCoordinatorLayout.SavedState>() {
            public SamsungCoordinatorLayout.SavedState createFromParcel(Parcel var1) {
                return new SamsungCoordinatorLayout.SavedState(var1, (ClassLoader) null);
            }

            public SamsungCoordinatorLayout.SavedState createFromParcel(Parcel var1, ClassLoader var2) {
                return new SamsungCoordinatorLayout.SavedState(var1, var2);
            }

            public SamsungCoordinatorLayout.SavedState[] newArray(int var1) {
                return new SamsungCoordinatorLayout.SavedState[var1];
            }
        };
        public SparseArray<Parcelable> behaviorStates;

        public SavedState(Parcel var1, ClassLoader var2) {
            super(var1, var2);
            int var3 = var1.readInt();
            int[] var4 = new int[var3];
            var1.readIntArray(var4);
            Parcelable[] var6 = var1.readParcelableArray(var2);
            this.behaviorStates = new SparseArray(var3);

            for (int var5 = 0; var5 < var3; ++var5) {
                this.behaviorStates.append(var4[var5], var6[var5]);
            }

        }

        public SavedState(Parcelable var1) {
            super(var1);
        }

        public void writeToParcel(Parcel var1, int var2) {
            super.writeToParcel(var1, var2);
            SparseArray var3 = this.behaviorStates;
            int var4 = 0;
            int var5;
            if (var3 != null) {
                var5 = var3.size();
            } else {
                var5 = 0;
            }

            var1.writeInt(var5);
            int[] var7 = new int[var5];

            Parcelable[] var6;
            for (var6 = new Parcelable[var5]; var4 < var5; ++var4) {
                var7[var4] = this.behaviorStates.keyAt(var4);
                var6[var4] = (Parcelable) this.behaviorStates.valueAt(var4);
            }

            var1.writeIntArray(var7);
            var1.writeParcelableArray(var6, var2);
        }
    }

    public static class ViewElevationComparator implements Comparator<View> {
        public ViewElevationComparator() {
        }

        public int compare(View var1, View var2) {
            float var3 = ViewCompat.getZ(var1);
            float var4 = ViewCompat.getZ(var2);
            if (var3 > var4) {
                return -1;
            } else {
                return var3 < var4 ? 1 : 0;
            }
        }
    }

    private class HierarchyChangeListener implements OnHierarchyChangeListener {
        public HierarchyChangeListener() {
        }

        public void onChildViewAdded(View var1, View var2) {
            OnHierarchyChangeListener var3 = SamsungCoordinatorLayout.this.mOnHierarchyChangeListener;
            if (var3 != null) {
                var3.onChildViewAdded(var1, var2);
            }

        }

        public void onChildViewRemoved(View var1, View var2) {
            SamsungCoordinatorLayout.this.onChildViewsChanged(2);
            OnHierarchyChangeListener var3 = SamsungCoordinatorLayout.this.mOnHierarchyChangeListener;
            if (var3 != null) {
                var3.onChildViewRemoved(var1, var2);
            }

        }
    }

    public class OnPreDrawListener implements android.view.ViewTreeObserver.OnPreDrawListener {
        public OnPreDrawListener() {
        }

        public boolean onPreDraw() {
            SamsungCoordinatorLayout.this.onChildViewsChanged(0);
            return true;
        }
    }
}
