package de.dlyt.yanndroid.oneui.sesl.coordinatorlayout;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
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
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.FloatRange;
import androidx.annotation.IdRes;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.util.ObjectsCompat;
import androidx.core.util.Pools;
import androidx.core.view.GravityCompat;
import androidx.core.view.NestedScrollingParent;
import androidx.core.view.NestedScrollingParent2;
import androidx.core.view.NestedScrollingParent3;
import androidx.core.view.NestedScrollingParentHelper;
import androidx.core.view.ViewCompat;
import androidx.core.view.ViewCompat.NestedScrollType;
import androidx.core.view.ViewCompat.ScrollAxis;
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
    static final Class<?>[] CONSTRUCTOR_PARAMS = new Class[]{Context.class, AttributeSet.class};
    static final int EVENT_NESTED_SCROLL = 1;
    static final int EVENT_PRE_DRAW = 0;
    static final int EVENT_VIEW_REMOVED = 2;
    static final String TAG = "CoordinatorLayout";
    static final Comparator<View> TOP_SORTED_CHILDREN_COMPARATOR;
    private static final int TYPE_ON_INTERCEPT = 0;
    private static final int TYPE_ON_TOUCH = 1;
    static final String WIDGET_PACKAGE_NAME;
    static final ThreadLocal<Map<String, Constructor<Behavior>>> sConstructors = new ThreadLocal<>();
    private static final Pools.Pool<Rect> sRectPool = new Pools.SynchronizedPool(12);
    private androidx.core.view.OnApplyWindowInsetsListener mApplyWindowInsetsListener;
    private final int[] mBehaviorConsumed = new int[2];
    private View mBehaviorTouchView;
    private final DirectedAcyclicGraph<View> mChildDag = new DirectedAcyclicGraph<>();
    private final List<View> mDependencySortedChildren = new ArrayList();
    private boolean mDisallowInterceptReset;
    private boolean mDrawStatusBarBackground;
    private boolean mEnableAutoCollapsingKeyEvent = true;
    private boolean mIsAttachedToWindow;
    private int[] mKeylines;
    private WindowInsetsCompat mLastInsets;
    private View mLastNestedScrollingChild;
    private boolean mNeedsPreDrawListener;
    private final NestedScrollingParentHelper mNestedScrollingParentHelper = new NestedScrollingParentHelper(this);
    private View mNestedScrollingTarget;
    private final int[] mNestedScrollingV2ConsumedCompat = new int[2];
    ViewGroup.OnHierarchyChangeListener mOnHierarchyChangeListener;
    private OnPreDrawListener mOnPreDrawListener;
    private Paint mScrimPaint;
    private Drawable mStatusBarBackground;
    private final List<View> mTempDependenciesList = new ArrayList();
    private final List<View> mTempList1 = new ArrayList();
    private boolean mToolIsMouse;
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({EVENT_PRE_DRAW, EVENT_NESTED_SCROLL, EVENT_VIEW_REMOVED})
    public @interface DispatchChangeEvent {}

    static {
        final Package pkg = SamsungCoordinatorLayout.class.getPackage();
        WIDGET_PACKAGE_NAME = pkg != null ? pkg.getName() : null;
    }

    static {
        if (Build.VERSION.SDK_INT >= 21) {
            TOP_SORTED_CHILDREN_COMPARATOR = new ViewElevationComparator();
        } else {
            TOP_SORTED_CHILDREN_COMPARATOR = null;
        }
    }

    @NonNull
    private static Rect acquireTempRect() {
        Rect rect = sRectPool.acquire();
        if (rect == null) {
            rect = new Rect();
        }
        return rect;
    }

    private static void releaseTempRect(@NonNull Rect rect) {
        rect.setEmpty();
        sRectPool.release(rect);
    }

    public SamsungCoordinatorLayout(@NonNull Context context) {
        this(context, null);
    }

    public SamsungCoordinatorLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.coordinatorLayoutStyle);
    }

    public SamsungCoordinatorLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        final TypedArray a = (defStyleAttr == 0) ? context.obtainStyledAttributes(attrs, R.styleable.SamsungCoordinatorLayout, 0, R.style.Widget_Support_CoordinatorLayout) : context.obtainStyledAttributes(attrs, R.styleable.SamsungCoordinatorLayout, defStyleAttr, 0);
        if (defStyleAttr == 0) {
            ViewCompat.saveAttributeDataForStyleable(this, context, R.styleable.SamsungCoordinatorLayout, attrs, a, 0, R.style.Widget_Support_CoordinatorLayout);
        } else {
            ViewCompat.saveAttributeDataForStyleable(this, context, R.styleable.SamsungCoordinatorLayout, attrs, a, defStyleAttr, 0);
        }

        final int keylineArrayRes = a.getResourceId(R.styleable.SamsungCoordinatorLayout_keylines, 0);
        if (keylineArrayRes != 0) {
            final Resources res = context.getResources();
            mKeylines = res.getIntArray(keylineArrayRes);
            final float density = res.getDisplayMetrics().density;
            final int count = mKeylines.length;
            for (int i = 0; i < count; i++) {
                mKeylines[i] = (int) (mKeylines[i] * density);
            }
        }
        mStatusBarBackground = a.getDrawable(R.styleable.SamsungCoordinatorLayout_statusBarBackground);
        a.recycle();

        setupForInsets();
        super.setOnHierarchyChangeListener(new HierarchyChangeListener());

        if (ViewCompat.getImportantForAccessibility(this) == ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_AUTO) {
            ViewCompat.setImportantForAccessibility(this, ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_YES);
        }
    }

    @Override
    public void setOnHierarchyChangeListener(OnHierarchyChangeListener onHierarchyChangeListener) {
        mOnHierarchyChangeListener = onHierarchyChangeListener;
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        resetTouchBehaviors(false);
        if (mNeedsPreDrawListener) {
            if (mOnPreDrawListener == null) {
                mOnPreDrawListener = new OnPreDrawListener();
            }
            final ViewTreeObserver vto = getViewTreeObserver();
            vto.addOnPreDrawListener(mOnPreDrawListener);
        }
        if (mLastInsets == null && ViewCompat.getFitsSystemWindows(this)) {
            ViewCompat.requestApplyInsets(this);
        }
        mIsAttachedToWindow = true;
    }

    public void seslSetNestedScrollingChild(View view) {
        mLastNestedScrollingChild = view;
    }

    @Override
    public boolean dispatchGenericMotionEvent(MotionEvent ev) {
        for (int i = getChildCount() - 1; i >= 0; i--) {
            final View child = getChildAt(i);

            final Behavior behavior = ((LayoutParams) child.getLayoutParams()).getBehavior();
            if (behavior != null) {
                behavior.dispatchGenericMotionEvent(ev);
            }

            if (child instanceof AppBarLayoutBehavior) {
                final AppBarLayoutBehavior ablBehavior = (AppBarLayoutBehavior) child;

                final boolean isMouseEvent = isMouseEvent(ev);
                if (mToolIsMouse != isMouseEvent) {
                    mToolIsMouse = isMouseEvent;
                    ablBehavior.seslSetIsMouse(isMouseEvent);
                }

                if (ev.getAction() == MotionEvent.ACTION_SCROLL) {
                    if (mLastNestedScrollingChild != null) {
                        if (ev.getAxisValue(MotionEvent.AXIS_VSCROLL) < 0.0F) {
                            ablBehavior.seslSetExpanded(false);
                        } else if (ev.getAxisValue(MotionEvent.AXIS_VSCROLL) > 0.0F && !mLastNestedScrollingChild.canScrollVertically(-1)) {
                            ablBehavior.seslSetExpanded(true);
                        }
                    } else if (ev.getAxisValue(MotionEvent.AXIS_VSCROLL) < 0.0F) {
                        ablBehavior.seslSetExpanded(false);
                    } else if (ev.getAxisValue(MotionEvent.AXIS_VSCROLL) > 0.0F) {
                        ablBehavior.seslSetExpanded(true);
                    }
                }
                break;
            }
        }

        return super.dispatchGenericMotionEvent(ev);
    }

    private boolean isMouseEvent(MotionEvent ev) {
        return ev.getToolType(0) == MotionEvent.TOOL_TYPE_MOUSE;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (mEnableAutoCollapsingKeyEvent) {
            if (event.getKeyCode() == KeyEvent.KEYCODE_TAB || event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP || event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN || event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT || event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT) {
                for (int i = 0; i < getChildCount(); i++) {
                    final View child = getChildAt(i);

                    if (child instanceof AppBarLayoutBehavior) {
                        AppBarLayoutBehavior behavior = (AppBarLayoutBehavior) child;
                        if (!behavior.seslIsCollapsed()) {
                            behavior.seslSetExpanded(false);
                            break;
                        }
                    }
                }
            }
        }

        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        resetTouchBehaviors(false);
        if (mNeedsPreDrawListener && mOnPreDrawListener != null) {
            final ViewTreeObserver vto = getViewTreeObserver();
            vto.removeOnPreDrawListener(mOnPreDrawListener);
        }
        if (mNestedScrollingTarget != null) {
            mLastNestedScrollingChild = mNestedScrollingTarget;
            onStopNestedScroll(mNestedScrollingTarget);
        }
        mIsAttachedToWindow = false;
    }

    public void setStatusBarBackground(@Nullable final Drawable bg) {
        if (mStatusBarBackground != bg) {
            if (mStatusBarBackground != null) {
                mStatusBarBackground.setCallback(null);
            }
            mStatusBarBackground = bg != null ? bg.mutate() : null;
            if (mStatusBarBackground != null) {
                if (mStatusBarBackground.isStateful()) {
                    mStatusBarBackground.setState(getDrawableState());
                }
                DrawableCompat.setLayoutDirection(mStatusBarBackground, ViewCompat.getLayoutDirection(this));
                mStatusBarBackground.setVisible(getVisibility() == VISIBLE, false);
                mStatusBarBackground.setCallback(this);
            }
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    @Nullable
    public Drawable getStatusBarBackground() {
        return mStatusBarBackground;
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();

        final int[] state = getDrawableState();
        boolean changed = false;

        Drawable d = mStatusBarBackground;
        if (d != null && d.isStateful()) {
            changed |= d.setState(state);
        }

        if (changed) {
            invalidate();
        }
    }

    @Override
    protected boolean verifyDrawable(Drawable who) {
        return super.verifyDrawable(who) || who == mStatusBarBackground;
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);

        final boolean visible = visibility == VISIBLE;
        if (mStatusBarBackground != null && mStatusBarBackground.isVisible() != visible) {
            mStatusBarBackground.setVisible(visible, false);
        }
    }

    public void setStatusBarBackgroundResource(@DrawableRes int resId) {
        setStatusBarBackground(resId != 0 ? ContextCompat.getDrawable(getContext(), resId) : null);
    }

    public void setStatusBarBackgroundColor(@ColorInt int color) {
        setStatusBarBackground(new ColorDrawable(color));
    }

    final WindowInsetsCompat setWindowInsets(WindowInsetsCompat insets) {
        if (!ObjectsCompat.equals(mLastInsets, insets)) {
            mLastInsets = insets;
            mDrawStatusBarBackground = insets != null && insets.getSystemWindowInsetTop() > 0;
            setWillNotDraw(!mDrawStatusBarBackground && getBackground() == null);

            insets = dispatchApplyWindowInsetsToBehaviors(insets);
            requestLayout();
        }
        return insets;
    }

    public final WindowInsetsCompat getLastWindowInsets() {
        return mLastInsets;
    }

    @SuppressWarnings("unchecked")
    private void resetTouchBehaviors(boolean intercept) {
        final int childCount = getChildCount();

        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);
            final Behavior behavior = ((LayoutParams) child.getLayoutParams()).getBehavior();

            if (behavior != null) {
                final long now = SystemClock.uptimeMillis();
                final MotionEvent ev = MotionEvent.obtain(now, now, MotionEvent.ACTION_CANCEL, 0.0f, 0.0f, 0);
                if (intercept) {
                    behavior.onInterceptTouchEvent(this, child, ev);
                } else {
                    behavior.onTouchEvent(this, child, ev);
                }
                ev.recycle();
            }
        }

        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);
            final LayoutParams lp = (LayoutParams) child.getLayoutParams();
            lp.resetTouchBehaviorTracking();
        }

        mBehaviorTouchView = null;
        mDisallowInterceptReset = false;
    }

    private void getTopSortedChildren(List<View> out) {
        out.clear();

        final boolean useCustomOrder = isChildrenDrawingOrderEnabled();
        final int childCount = getChildCount();
        for (int i = childCount - 1; i >= 0; i--) {
            final int childIndex = useCustomOrder ? getChildDrawingOrder(childCount, i) : i;
            final View child = getChildAt(childIndex);
            out.add(child);
        }

        if (TOP_SORTED_CHILDREN_COMPARATOR != null) {
            Collections.sort(out, TOP_SORTED_CHILDREN_COMPARATOR);
        }
    }

    //kang
    @SuppressWarnings("unchecked")
    private boolean performIntercept(final MotionEvent ev, final int type) {
        int actionMasked = ev.getActionMasked();
        List<View> list = this.mTempList1;
        getTopSortedChildren(list);
        int size = list.size();
        MotionEvent motionEvent2 = null;
        boolean z = false;
        boolean z2 = false;
        for (int i2 = 0; i2 < size; i2++) {
            View view = list.get(i2);
            LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
            Behavior behavior = layoutParams.getBehavior();
            if (!(z || z2) || actionMasked == 0) {
                if (!z && behavior != null) {
                    if (type == 0) {
                        z = behavior.onInterceptTouchEvent(this, view, ev);
                    } else if (type == 1) {
                        z = behavior.onTouchEvent(this, view, ev);
                    }
                    if (z) {
                        this.mBehaviorTouchView = view;
                    }
                }
                boolean didBlockInteraction = layoutParams.didBlockInteraction();
                boolean isBlockingInteractionBelow = layoutParams.isBlockingInteractionBelow(this, view);
                z2 = isBlockingInteractionBelow && !didBlockInteraction;
                if (isBlockingInteractionBelow && !z2) {
                    break;
                }
            } else if (behavior != null) {
                if (motionEvent2 == null) {
                    long uptimeMillis = SystemClock.uptimeMillis();
                    motionEvent2 = MotionEvent.obtain(uptimeMillis, uptimeMillis, 3, 0.0f, 0.0f, 0);
                }
                if (type == 0) {
                    behavior.onInterceptTouchEvent(this, view, motionEvent2);
                } else if (type == 1) {
                    behavior.onTouchEvent(this, view, motionEvent2);
                }
            }
        }
        list.clear();
        return z;
    }
    //kang

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = ev.getActionMasked();

        if (action == MotionEvent.ACTION_DOWN) {
            for (int i = getChildCount() - 1; i >= 0; i--) {
                final View child = getChildAt(i);

                if (child instanceof AppBarLayoutBehavior) {
                    AppBarLayoutBehavior behavior = (AppBarLayoutBehavior) child;

                    final boolean isMouseEvent = isMouseEvent(ev);
                    if (mToolIsMouse != isMouseEvent) {
                        mToolIsMouse = isMouseEvent;
                        behavior.seslSetIsMouse(isMouseEvent);
                    }
                }
            }

            resetTouchBehaviors(true);
        }

        final boolean intercepted = performIntercept(ev, TYPE_ON_INTERCEPT);

        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            resetTouchBehaviors(true);
        }

        return intercepted;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean onTouchEvent(MotionEvent ev) {
        boolean handled = false;
        boolean cancelSuper = false;
        MotionEvent cancelEvent = null;

        final int action = ev.getActionMasked();

        if (mBehaviorTouchView != null) {
            final LayoutParams lp = (LayoutParams) mBehaviorTouchView.getLayoutParams();
            final Behavior b = lp.getBehavior();
            if (b != null) {
                handled = b.onTouchEvent(this, mBehaviorTouchView, ev);
            }
        } else {
            handled = performIntercept(ev, TYPE_ON_TOUCH);
            cancelSuper = action != MotionEvent.ACTION_DOWN && handled;
        }

        if (mBehaviorTouchView == null || action == MotionEvent.ACTION_CANCEL) {
            handled |= super.onTouchEvent(ev);
        } else if (cancelSuper) {
            final long now = SystemClock.uptimeMillis();
            cancelEvent = MotionEvent.obtain(now, now, MotionEvent.ACTION_CANCEL, 0.0f, 0.0f, 0);
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

    @Override
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        super.requestDisallowInterceptTouchEvent(disallowIntercept);
        if (disallowIntercept && !mDisallowInterceptReset) {
            resetTouchBehaviors(false);
            mDisallowInterceptReset = true;
        }
    }

    private int getKeyline(int index) {
        if (mKeylines == null) {
            Log.e(TAG, "No keylines defined for " + this + " - attempted index lookup " + index);
            return 0;
        }

        if (index < 0 || index >= mKeylines.length) {
            Log.e(TAG, "Keyline index " + index + " out of range for " + this);
            return 0;
        }

        return mKeylines[index];
    }

    @SuppressWarnings("unchecked")
    static Behavior parseBehavior(Context context, AttributeSet attrs, String name) {
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

    LayoutParams getResolvedLayoutParams(View child) {
        final LayoutParams result = (LayoutParams) child.getLayoutParams();
        if (!result.mBehaviorResolved) {
            if (child instanceof AttachedBehavior) {
                Behavior attachedBehavior = ((AttachedBehavior) child).getBehavior();
                if (attachedBehavior == null) {
                    Log.e(TAG, "Attached behavior class is null");
                }
                result.setBehavior(attachedBehavior);
                result.mBehaviorResolved = true;
            } else {
                Class<?> childClass = child.getClass();
                DefaultBehavior defaultBehavior = null;
                while (childClass != null && (defaultBehavior = childClass.getAnnotation(DefaultBehavior.class)) == null) {
                    childClass = childClass.getSuperclass();
                }
                if (defaultBehavior != null) {
                    try {
                        result.setBehavior(defaultBehavior.value().getDeclaredConstructor().newInstance());
                    } catch (Exception e) {
                        Log.e(TAG, "Default behavior class " + defaultBehavior.value().getName() + " could not be instantiated. Did you forget a default constructor?", e);
                    }
                }
                result.mBehaviorResolved = true;
            }
        }
        return result;
    }

    private void prepareChildren() {
        mDependencySortedChildren.clear();
        mChildDag.clear();

        for (int i = 0, count = getChildCount(); i < count; i++) {
            final View view = getChildAt(i);

            final LayoutParams lp = getResolvedLayoutParams(view);
            lp.findAnchorView(this, view);

            mChildDag.addNode(view);

            for (int j = 0; j < count; j++) {
                if (j == i) {
                    continue;
                }
                final View other = getChildAt(j);
                if (lp.dependsOn(this, view, other)) {
                    if (!mChildDag.contains(other)) {
                        mChildDag.addNode(other);
                    }
                    mChildDag.addEdge(other, view);
                }
            }
        }

        mDependencySortedChildren.addAll(mChildDag.getSortedList());
        Collections.reverse(mDependencySortedChildren);
    }

    void getDescendantRect(View descendant, Rect out) {
        ViewGroupUtils.getDescendantRect(this, descendant, out);
    }

    @Override
    protected int getSuggestedMinimumWidth() {
        return Math.max(super.getSuggestedMinimumWidth(), getPaddingLeft() + getPaddingRight());
    }

    @Override
    protected int getSuggestedMinimumHeight() {
        return Math.max(super.getSuggestedMinimumHeight(), getPaddingTop() + getPaddingBottom());
    }

    public void onMeasureChild(View child, int parentWidthMeasureSpec, int widthUsed, int parentHeightMeasureSpec, int heightUsed) {
        measureChildWithMargins(child, parentWidthMeasureSpec, widthUsed, parentHeightMeasureSpec, heightUsed);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        prepareChildren();
        ensurePreDrawListener();

        final int paddingLeft = getPaddingLeft();
        final int paddingTop = getPaddingTop();
        final int paddingRight = getPaddingRight();
        final int paddingBottom = getPaddingBottom();
        final int layoutDirection = ViewCompat.getLayoutDirection(this);
        final boolean isRtl = layoutDirection == ViewCompat.LAYOUT_DIRECTION_RTL;
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        final int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        final int widthPadding = paddingLeft + paddingRight;
        final int heightPadding = paddingTop + paddingBottom;
        int widthUsed = getSuggestedMinimumWidth();
        int heightUsed = getSuggestedMinimumHeight();
        int childState = 0;

        final boolean applyInsets = mLastInsets != null && ViewCompat.getFitsSystemWindows(this);

        final int childCount = mDependencySortedChildren.size();
        for (int i = 0; i < childCount; i++) {
            final View child = mDependencySortedChildren.get(i);
            if (child.getVisibility() == GONE) {
                continue;
            }

            final LayoutParams lp = (LayoutParams) child.getLayoutParams();

            int keylineWidthUsed = 0;
            if (lp.keyline >= 0 && widthMode != MeasureSpec.UNSPECIFIED) {
                final int keylinePos = getKeyline(lp.keyline);
                final int keylineGravity = GravityCompat.getAbsoluteGravity(resolveKeylineGravity(lp.gravity), layoutDirection) & Gravity.HORIZONTAL_GRAVITY_MASK;
                if ((keylineGravity == Gravity.LEFT && !isRtl) || (keylineGravity == Gravity.RIGHT && isRtl)) {
                    keylineWidthUsed = Math.max(0, widthSize - paddingRight - keylinePos);
                } else if ((keylineGravity == Gravity.RIGHT && !isRtl) || (keylineGravity == Gravity.LEFT && isRtl)) {
                    keylineWidthUsed = Math.max(0, keylinePos - paddingLeft);
                }
            }

            int childWidthMeasureSpec = widthMeasureSpec;
            int childHeightMeasureSpec = heightMeasureSpec;
            if (applyInsets && !ViewCompat.getFitsSystemWindows(child)) {
                final int horizInsets = mLastInsets.getSystemWindowInsetLeft() + mLastInsets.getSystemWindowInsetRight();
                final int vertInsets = mLastInsets.getSystemWindowInsetTop() + mLastInsets.getSystemWindowInsetBottom();

                childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(widthSize - horizInsets, widthMode);
                childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(heightSize - vertInsets, heightMode);
            }

            final Behavior b = lp.getBehavior();
            if (b == null || !b.onMeasureChild(this, child, childWidthMeasureSpec, keylineWidthUsed, childHeightMeasureSpec, 0)) {
                onMeasureChild(child, childWidthMeasureSpec, keylineWidthUsed, childHeightMeasureSpec, 0);
            }

            widthUsed = Math.max(widthUsed, widthPadding + child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin);

            heightUsed = Math.max(heightUsed, heightPadding + child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin);
            childState = View.combineMeasuredStates(childState, child.getMeasuredState());
        }

        final int width = View.resolveSizeAndState(widthUsed, widthMeasureSpec, childState & View.MEASURED_STATE_MASK);
        final int height = View.resolveSizeAndState(heightUsed, heightMeasureSpec, childState << View.MEASURED_HEIGHT_STATE_SHIFT);
        setMeasuredDimension(width, height);
    }

    @SuppressWarnings("unchecked")
    private WindowInsetsCompat dispatchApplyWindowInsetsToBehaviors(WindowInsetsCompat insets) {
        if (insets.isConsumed()) {
            return insets;
        }

        for (int i = 0, z = getChildCount(); i < z; i++) {
            final View child = getChildAt(i);
            if (ViewCompat.getFitsSystemWindows(child)) {
                final LayoutParams lp = (LayoutParams) child.getLayoutParams();
                final Behavior b = lp.getBehavior();

                if (b != null) {
                    insets = b.onApplyWindowInsets(this, child, insets);
                    if (insets.isConsumed()) {
                        break;
                    }
                }
            }
        }

        return insets;
    }

    public void onLayoutChild(@NonNull View child, int layoutDirection) {
        final LayoutParams lp = (LayoutParams) child.getLayoutParams();
        if (lp.checkAnchorChanged()) {
            throw new IllegalStateException("An anchor may not be changed after CoordinatorLayout measurement begins before layout is complete.");
        }
        if (lp.mAnchorView != null) {
            layoutChildWithAnchor(child, lp.mAnchorView, layoutDirection);
        } else if (lp.keyline >= 0) {
            layoutChildWithKeyline(child, lp.keyline, layoutDirection);
        } else {
            layoutChild(child, layoutDirection);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int layoutDirection = ViewCompat.getLayoutDirection(this);
        final int childCount = mDependencySortedChildren.size();
        for (int i = 0; i < childCount; i++) {
            final View child = mDependencySortedChildren.get(i);
            if (child.getVisibility() == GONE) {
                continue;
            }

            final LayoutParams lp = (LayoutParams) child.getLayoutParams();
            final Behavior behavior = lp.getBehavior();

            if (behavior == null || !behavior.onLayoutChild(this, child, layoutDirection)) {
                onLayoutChild(child, layoutDirection);
            }
        }
    }

    @Override
    public void onDraw(Canvas c) {
        super.onDraw(c);
        if (mDrawStatusBarBackground && mStatusBarBackground != null) {
            final int inset = mLastInsets != null ? mLastInsets.getSystemWindowInsetTop() : 0;
            if (inset > 0) {
                mStatusBarBackground.setBounds(0, 0, getWidth(), inset);
                mStatusBarBackground.draw(c);
            }
        }
    }

    @Override
    public void setFitsSystemWindows(boolean fitSystemWindows) {
        super.setFitsSystemWindows(fitSystemWindows);
        setupForInsets();
    }

    void recordLastChildRect(View child, Rect r) {
        final LayoutParams lp = (LayoutParams) child.getLayoutParams();
        lp.setLastChildRect(r);
    }

    void getLastChildRect(View child, Rect out) {
        final LayoutParams lp = (LayoutParams) child.getLayoutParams();
        out.set(lp.getLastChildRect());
    }

    void getChildRect(View child, boolean transform, Rect out) {
        if (child.isLayoutRequested() || child.getVisibility() == View.GONE) {
            out.setEmpty();
            return;
        }
        if (transform) {
            getDescendantRect(child, out);
        } else {
            out.set(child.getLeft(), child.getTop(), child.getRight(), child.getBottom());
        }
    }

    private void getDesiredAnchoredChildRectWithoutConstraints(int layoutDirection, Rect anchorRect, Rect out, LayoutParams lp, int childWidth, int childHeight) {
        final int absGravity = GravityCompat.getAbsoluteGravity(resolveAnchoredChildGravity(lp.gravity), layoutDirection);
        final int absAnchorGravity = GravityCompat.getAbsoluteGravity(resolveGravity(lp.anchorGravity), layoutDirection);

        final int hgrav = absGravity & Gravity.HORIZONTAL_GRAVITY_MASK;
        final int vgrav = absGravity & Gravity.VERTICAL_GRAVITY_MASK;
        final int anchorHgrav = absAnchorGravity & Gravity.HORIZONTAL_GRAVITY_MASK;
        final int anchorVgrav = absAnchorGravity & Gravity.VERTICAL_GRAVITY_MASK;

        int left;
        int top;

        switch (anchorHgrav) {
            default:
            case Gravity.LEFT:
                left = anchorRect.left;
                break;
            case Gravity.RIGHT:
                left = anchorRect.right;
                break;
            case Gravity.CENTER_HORIZONTAL:
                left = anchorRect.left + anchorRect.width() / 2;
                break;
        }

        switch (anchorVgrav) {
            default:
            case Gravity.TOP:
                top = anchorRect.top;
                break;
            case Gravity.BOTTOM:
                top = anchorRect.bottom;
                break;
            case Gravity.CENTER_VERTICAL:
                top = anchorRect.top + anchorRect.height() / 2;
                break;
        }

        switch (hgrav) {
            default:
            case Gravity.LEFT:
                left -= childWidth;
                break;
            case Gravity.RIGHT:
                break;
            case Gravity.CENTER_HORIZONTAL:
                left -= childWidth / 2;
                break;
        }

        switch (vgrav) {
            default:
            case Gravity.TOP:
                top -= childHeight;
                break;
            case Gravity.BOTTOM:
                break;
            case Gravity.CENTER_VERTICAL:
                top -= childHeight / 2;
                break;
        }

        out.set(left, top, left + childWidth, top + childHeight);
    }

    private void constrainChildRect(LayoutParams lp, Rect out, int childWidth, int childHeight) {
        final int width = getWidth();
        final int height = getHeight();

        int left = Math.max(getPaddingLeft() + lp.leftMargin, Math.min(out.left, width - getPaddingRight() - childWidth - lp.rightMargin));
        int top = Math.max(getPaddingTop() + lp.topMargin, Math.min(out.top, height - getPaddingBottom() - childHeight - lp.bottomMargin));

        out.set(left, top, left + childWidth, top + childHeight);
    }

    void getDesiredAnchoredChildRect(View child, int layoutDirection, Rect anchorRect, Rect out) {
        final LayoutParams lp = (LayoutParams) child.getLayoutParams();
        final int childWidth = child.getMeasuredWidth();
        final int childHeight = child.getMeasuredHeight();
        getDesiredAnchoredChildRectWithoutConstraints(layoutDirection, anchorRect, out, lp, childWidth, childHeight);
        constrainChildRect(lp, out, childWidth, childHeight);
    }

    private void layoutChildWithAnchor(View child, View anchor, int layoutDirection) {
        final Rect anchorRect = acquireTempRect();
        final Rect childRect = acquireTempRect();
        try {
            getDescendantRect(anchor, anchorRect);
            getDesiredAnchoredChildRect(child, layoutDirection, anchorRect, childRect);
            child.layout(childRect.left, childRect.top, childRect.right, childRect.bottom);
        } finally {
            releaseTempRect(anchorRect);
            releaseTempRect(childRect);
        }
    }

    private void layoutChildWithKeyline(View child, int keyline, int layoutDirection) {
        final LayoutParams lp = (LayoutParams) child.getLayoutParams();
        final int absGravity = GravityCompat.getAbsoluteGravity(resolveKeylineGravity(lp.gravity), layoutDirection);

        final int hgrav = absGravity & Gravity.HORIZONTAL_GRAVITY_MASK;
        final int vgrav = absGravity & Gravity.VERTICAL_GRAVITY_MASK;
        final int width = getWidth();
        final int height = getHeight();
        final int childWidth = child.getMeasuredWidth();
        final int childHeight = child.getMeasuredHeight();

        if (layoutDirection == ViewCompat.LAYOUT_DIRECTION_RTL) {
            keyline = width - keyline;
        }

        int left = getKeyline(keyline) - childWidth;
        int top = 0;

        switch (hgrav) {
            default:
            case Gravity.LEFT:
                break;
            case Gravity.RIGHT:
                left += childWidth;
                break;
            case Gravity.CENTER_HORIZONTAL:
                left += childWidth / 2;
                break;
        }

        switch (vgrav) {
            default:
            case Gravity.TOP:
                break;
            case Gravity.BOTTOM:
                top += childHeight;
                break;
            case Gravity.CENTER_VERTICAL:
                top += childHeight / 2;
                break;
        }

        left = Math.max(getPaddingLeft() + lp.leftMargin, Math.min(left, width - getPaddingRight() - childWidth - lp.rightMargin));
        top = Math.max(getPaddingTop() + lp.topMargin, Math.min(top, height - getPaddingBottom() - childHeight - lp.bottomMargin));

        child.layout(left, top, left + childWidth, top + childHeight);
    }

    private void layoutChild(View child, int layoutDirection) {
        final LayoutParams lp = (LayoutParams) child.getLayoutParams();
        final Rect parent = acquireTempRect();
        parent.set(getPaddingLeft() + lp.leftMargin, getPaddingTop() + lp.topMargin, getWidth() - getPaddingRight() - lp.rightMargin, getHeight() - getPaddingBottom() - lp.bottomMargin);

        if (mLastInsets != null && ViewCompat.getFitsSystemWindows(this) && !ViewCompat.getFitsSystemWindows(child)) {
            parent.left += mLastInsets.getSystemWindowInsetLeft();
            parent.top += mLastInsets.getSystemWindowInsetTop();
            parent.right -= mLastInsets.getSystemWindowInsetRight();
            parent.bottom -= mLastInsets.getSystemWindowInsetBottom();
        }

        final Rect out = acquireTempRect();
        GravityCompat.apply(resolveGravity(lp.gravity), child.getMeasuredWidth(), child.getMeasuredHeight(), parent, out, layoutDirection);
        child.layout(out.left, out.top, out.right, out.bottom);

        releaseTempRect(parent);
        releaseTempRect(out);
    }

    private static int resolveGravity(int gravity) {
        if ((gravity & Gravity.HORIZONTAL_GRAVITY_MASK) == Gravity.NO_GRAVITY) {
            gravity |= GravityCompat.START;
        }
        if ((gravity & Gravity.VERTICAL_GRAVITY_MASK) == Gravity.NO_GRAVITY) {
            gravity |= Gravity.TOP;
        }
        return gravity;
    }

    private static int resolveKeylineGravity(int gravity) {
        return gravity == Gravity.NO_GRAVITY ? GravityCompat.END | Gravity.TOP : gravity;
    }

    private static int resolveAnchoredChildGravity(int gravity) {
        return gravity == Gravity.NO_GRAVITY ? Gravity.CENTER : gravity;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        final LayoutParams lp = (LayoutParams) child.getLayoutParams();
        if (lp.mBehavior != null) {
            final float scrimAlpha = lp.mBehavior.getScrimOpacity(this, child);
            if (scrimAlpha > 0f) {
                if (mScrimPaint == null) {
                    mScrimPaint = new Paint();
                }
                mScrimPaint.setColor(lp.mBehavior.getScrimColor(this, child));
                mScrimPaint.setAlpha(clamp(Math.round(255 * scrimAlpha), 0, 255));

                final int saved = canvas.save();
                if (child.isOpaque()) {
                    canvas.clipRect(child.getLeft(), child.getTop(), child.getRight(), child.getBottom(), Region.Op.DIFFERENCE);
                }
                canvas.drawRect(getPaddingLeft(), getPaddingTop(), getWidth() - getPaddingRight(), getHeight() - getPaddingBottom(), mScrimPaint);
                canvas.restoreToCount(saved);
            }
        }
        return super.drawChild(canvas, child, drawingTime);
    }

    private static int clamp(int value, int min, int max) {
        if (value < min) {
            return min;
        } else if (value > max) {
            return max;
        }
        return value;
    }

    @SuppressWarnings("unchecked")
    final void onChildViewsChanged(@DispatchChangeEvent final int type) {
        final int layoutDirection = ViewCompat.getLayoutDirection(this);
        final int childCount = mDependencySortedChildren.size();
        final Rect inset = acquireTempRect();
        final Rect drawRect = acquireTempRect();
        final Rect lastDrawRect = acquireTempRect();

        for (int i = 0; i < childCount; i++) {
            final View child = mDependencySortedChildren.get(i);
            final LayoutParams lp = (LayoutParams) child.getLayoutParams();
            if (type == EVENT_PRE_DRAW && child.getVisibility() == View.GONE) {
                continue;
            }

            for (int j = 0; j < i; j++) {
                final View checkChild = mDependencySortedChildren.get(j);

                if (lp.mAnchorDirectChild == checkChild) {
                    offsetChildToAnchor(child, layoutDirection);
                }
            }

            getChildRect(child, true, drawRect);

            if (lp.insetEdge != Gravity.NO_GRAVITY && !drawRect.isEmpty()) {
                final int absInsetEdge = GravityCompat.getAbsoluteGravity(lp.insetEdge, layoutDirection);
                switch (absInsetEdge & Gravity.VERTICAL_GRAVITY_MASK) {
                    case Gravity.TOP:
                        inset.top = Math.max(inset.top, drawRect.bottom);
                        break;
                    case Gravity.BOTTOM:
                        inset.bottom = Math.max(inset.bottom, getHeight() - drawRect.top);
                        break;
                }
                switch (absInsetEdge & Gravity.HORIZONTAL_GRAVITY_MASK) {
                    case Gravity.LEFT:
                        inset.left = Math.max(inset.left, drawRect.right);
                        break;
                    case Gravity.RIGHT:
                        inset.right = Math.max(inset.right, getWidth() - drawRect.left);
                        break;
                }
            }

            if (lp.dodgeInsetEdges != Gravity.NO_GRAVITY && child.getVisibility() == View.VISIBLE) {
                offsetChildByInset(child, inset, layoutDirection);
            }

            if (type != EVENT_VIEW_REMOVED) {
                getLastChildRect(child, lastDrawRect);
                if (lastDrawRect.equals(drawRect)) {
                    continue;
                }
                recordLastChildRect(child, drawRect);
            }

            for (int j = i + 1; j < childCount; j++) {
                final View checkChild = mDependencySortedChildren.get(j);
                final LayoutParams checkLp = (LayoutParams) checkChild.getLayoutParams();
                final Behavior b = checkLp.getBehavior();

                if (b != null && b.layoutDependsOn(this, checkChild, child)) {
                    if (type == EVENT_PRE_DRAW && checkLp.getChangedAfterNestedScroll()) {
                        checkLp.resetChangedAfterNestedScroll();
                        continue;
                    }

                    final boolean handled;
                    switch (type) {
                        case EVENT_VIEW_REMOVED:
                            b.onDependentViewRemoved(this, checkChild, child);
                            handled = true;
                            break;
                        default:
                            handled = b.onDependentViewChanged(this, checkChild, child);
                            break;
                    }

                    if (type == EVENT_NESTED_SCROLL) {
                        checkLp.setChangedAfterNestedScroll(handled);
                    }
                }
            }
        }

        releaseTempRect(inset);
        releaseTempRect(drawRect);
        releaseTempRect(lastDrawRect);
    }

    @SuppressWarnings("unchecked")
    private void offsetChildByInset(final View child, final Rect inset, final int layoutDirection) {
        if (!ViewCompat.isLaidOut(child)) {
            return;
        }

        if (child.getWidth() <= 0 || child.getHeight() <= 0) {
            return;
        }

        final LayoutParams lp = (LayoutParams) child.getLayoutParams();
        final Behavior behavior = lp.getBehavior();
        final Rect dodgeRect = acquireTempRect();
        final Rect bounds = acquireTempRect();
        bounds.set(child.getLeft(), child.getTop(), child.getRight(), child.getBottom());

        if (behavior != null && behavior.getInsetDodgeRect(this, child, dodgeRect)) {
            if (!bounds.contains(dodgeRect)) {
                throw new IllegalArgumentException("Rect should be within the child's bounds. Rect:" + dodgeRect.toShortString() + " | Bounds:" + bounds.toShortString());
            }
        } else {
            dodgeRect.set(bounds);
        }

        releaseTempRect(bounds);

        if (dodgeRect.isEmpty()) {
            releaseTempRect(dodgeRect);
            return;
        }

        final int absDodgeInsetEdges = GravityCompat.getAbsoluteGravity(lp.dodgeInsetEdges, layoutDirection);

        boolean offsetY = false;
        if ((absDodgeInsetEdges & Gravity.TOP) == Gravity.TOP) {
            int distance = dodgeRect.top - lp.topMargin - lp.mInsetOffsetY;
            if (distance < inset.top) {
                setInsetOffsetY(child, inset.top - distance);
                offsetY = true;
            }
        }
        if ((absDodgeInsetEdges & Gravity.BOTTOM) == Gravity.BOTTOM) {
            int distance = getHeight() - dodgeRect.bottom - lp.bottomMargin + lp.mInsetOffsetY;
            if (distance < inset.bottom) {
                setInsetOffsetY(child, distance - inset.bottom);
                offsetY = true;
            }
        }
        if (!offsetY) {
            setInsetOffsetY(child, 0);
        }

        boolean offsetX = false;
        if ((absDodgeInsetEdges & Gravity.LEFT) == Gravity.LEFT) {
            int distance = dodgeRect.left - lp.leftMargin - lp.mInsetOffsetX;
            if (distance < inset.left) {
                setInsetOffsetX(child, inset.left - distance);
                offsetX = true;
            }
        }
        if ((absDodgeInsetEdges & Gravity.RIGHT) == Gravity.RIGHT) {
            int distance = getWidth() - dodgeRect.right - lp.rightMargin + lp.mInsetOffsetX;
            if (distance < inset.right) {
                setInsetOffsetX(child, distance - inset.right);
                offsetX = true;
            }
        }
        if (!offsetX) {
            setInsetOffsetX(child, 0);
        }

        releaseTempRect(dodgeRect);
    }

    private void setInsetOffsetX(View child, int offsetX) {
        final LayoutParams lp = (LayoutParams) child.getLayoutParams();
        if (lp.mInsetOffsetX != offsetX) {
            final int dx = offsetX - lp.mInsetOffsetX;
            ViewCompat.offsetLeftAndRight(child, dx);
            lp.mInsetOffsetX = offsetX;
        }
    }

    private void setInsetOffsetY(View child, int offsetY) {
        final LayoutParams lp = (LayoutParams) child.getLayoutParams();
        if (lp.mInsetOffsetY != offsetY) {
            final int dy = offsetY - lp.mInsetOffsetY;
            ViewCompat.offsetTopAndBottom(child, dy);
            lp.mInsetOffsetY = offsetY;
        }
    }

    @SuppressWarnings("unchecked")
    public void dispatchDependentViewsChanged(@NonNull View view) {
        final List<View> dependents = mChildDag.getIncomingEdges(view);
        if (dependents != null && !dependents.isEmpty()) {
            for (int i = 0; i < dependents.size(); i++) {
                final View child = dependents.get(i);
                LayoutParams lp = (LayoutParams) child.getLayoutParams();
                Behavior b = lp.getBehavior();
                if (b != null) {
                    b.onDependentViewChanged(this, child, view);
                }
            }
        }
    }

    @NonNull
    public List<View> getDependencies(@NonNull View child) {
        List<View> result = mChildDag.getOutgoingEdges(child);
        return result == null ? Collections.<View>emptyList() : result;
    }

    @NonNull
    public List<View> getDependents(@NonNull View child) {
        List<View> result = mChildDag.getIncomingEdges(child);
        return result == null ? Collections.<View>emptyList() : result;
    }

    final List<View> getDependencySortedChildren() {
        prepareChildren();
        return Collections.unmodifiableList(mDependencySortedChildren);
    }

    void ensurePreDrawListener() {
        boolean hasDependencies = false;
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);
            if (hasDependencies(child)) {
                hasDependencies = true;
                break;
            }
        }

        if (hasDependencies != mNeedsPreDrawListener) {
            if (hasDependencies) {
                addPreDrawListener();
            } else {
                removePreDrawListener();
            }
        }
    }

    private boolean hasDependencies(View child) {
        return mChildDag.hasOutgoingEdges(child);
    }

    void addPreDrawListener() {
        if (mIsAttachedToWindow) {
            if (mOnPreDrawListener == null) {
                mOnPreDrawListener = new OnPreDrawListener();
            }
            final ViewTreeObserver vto = getViewTreeObserver();
            vto.addOnPreDrawListener(mOnPreDrawListener);
        }

        mNeedsPreDrawListener = true;
    }

    void removePreDrawListener() {
        if (mIsAttachedToWindow) {
            if (mOnPreDrawListener != null) {
                final ViewTreeObserver vto = getViewTreeObserver();
                vto.removeOnPreDrawListener(mOnPreDrawListener);
            }
        }
        mNeedsPreDrawListener = false;
    }

    @SuppressWarnings("unchecked")
    void offsetChildToAnchor(View child, int layoutDirection) {
        final LayoutParams lp = (LayoutParams) child.getLayoutParams();
        if (lp.mAnchorView != null) {
            final Rect anchorRect = acquireTempRect();
            final Rect childRect = acquireTempRect();
            final Rect desiredChildRect = acquireTempRect();

            getDescendantRect(lp.mAnchorView, anchorRect);
            getChildRect(child, false, childRect);

            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();
            getDesiredAnchoredChildRectWithoutConstraints(layoutDirection, anchorRect, desiredChildRect, lp, childWidth, childHeight);
            boolean changed = desiredChildRect.left != childRect.left || desiredChildRect.top != childRect.top;
            constrainChildRect(lp, desiredChildRect, childWidth, childHeight);

            final int dx = desiredChildRect.left - childRect.left;
            final int dy = desiredChildRect.top - childRect.top;

            if (dx != 0) {
                ViewCompat.offsetLeftAndRight(child, dx);
            }
            if (dy != 0) {
                ViewCompat.offsetTopAndBottom(child, dy);
            }

            if (changed) {
                final Behavior b = lp.getBehavior();
                if (b != null) {
                    b.onDependentViewChanged(this, child, lp.mAnchorView);
                }
            }

            releaseTempRect(anchorRect);
            releaseTempRect(childRect);
            releaseTempRect(desiredChildRect);
        }
    }

    public boolean isPointInChildBounds(@NonNull View child, int x, int y) {
        final Rect r = acquireTempRect();
        getDescendantRect(child, r);
        try {
            return r.contains(x, y);
        } finally {
            releaseTempRect(r);
        }
    }

    public boolean doViewsOverlap(@NonNull View first, @NonNull View second) {
        if (first.getVisibility() == VISIBLE && second.getVisibility() == VISIBLE) {
            final Rect firstRect = acquireTempRect();
            getChildRect(first, first.getParent() != this, firstRect);
            final Rect secondRect = acquireTempRect();
            getChildRect(second, second.getParent() != this, secondRect);
            try {
                return !(firstRect.left > secondRect.right || firstRect.top > secondRect.bottom || firstRect.right < secondRect.left || firstRect.bottom < secondRect.top);
            } finally {
                releaseTempRect(firstRect);
                releaseTempRect(secondRect);
            }
        }
        return false;
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        if (p instanceof LayoutParams) {
            return new LayoutParams((LayoutParams) p);
        } else if (p instanceof MarginLayoutParams) {
            return new LayoutParams((MarginLayoutParams) p);
        }
        return new LayoutParams(p);
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams && super.checkLayoutParams(p);
    }

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return onStartNestedScroll(child, target, nestedScrollAxes, ViewCompat.TYPE_TOUCH);
    }

    @Override
    @SuppressWarnings("unchecked")
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

    @Override
    public void onNestedScrollAccepted(View child, View target, int axes) {
        onNestedScrollAccepted(child, target, axes, ViewCompat.TYPE_TOUCH);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onNestedScrollAccepted(View child, View target, int axes, int type) {
        mNestedScrollingParentHelper.onNestedScrollAccepted(child, target, axes, type);
        mNestedScrollingTarget = target;

        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View view = getChildAt(i);
            final LayoutParams lp = (LayoutParams) view.getLayoutParams();
            if (!lp.isNestedScrollAccepted(type)) {
                continue;
            }

            final Behavior viewBehavior = lp.getBehavior();
            if (viewBehavior != null) {
                viewBehavior.onNestedScrollAccepted(this, view, child, target, axes, type);
            }
        }
    }

    @Override
    public void onStopNestedScroll(View target) {
        onStopNestedScroll(target, ViewCompat.TYPE_TOUCH);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onStopNestedScroll(View target, int type) {
        mNestedScrollingParentHelper.onStopNestedScroll(target, type);

        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View view = getChildAt(i);
            final LayoutParams lp = (LayoutParams) view.getLayoutParams();
            if (!lp.isNestedScrollAccepted(type)) {
                continue;
            }

            final Behavior viewBehavior = lp.getBehavior();
            if (viewBehavior != null) {
                viewBehavior.onStopNestedScroll(this, view, target, type);
            }
            lp.resetNestedScroll(type);
            lp.resetChangedAfterNestedScroll();
        }
        mNestedScrollingTarget = null;
    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, ViewCompat.TYPE_TOUCH);
    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type) {
        onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, ViewCompat.TYPE_TOUCH, mNestedScrollingV2ConsumedCompat);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onNestedScroll(@NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, @ViewCompat.NestedScrollType int type, @NonNull int[] consumed) {
        final int childCount = getChildCount();
        boolean accepted = false;
        int xConsumed = 0;
        int yConsumed = 0;

        for (int i = 0; i < childCount; i++) {
            final View view = getChildAt(i);
            if (view.getVisibility() == GONE) {
                continue;
            }

            final LayoutParams lp = (LayoutParams) view.getLayoutParams();
            if (!lp.isNestedScrollAccepted(type)) {
                continue;
            }

            final Behavior viewBehavior = lp.getBehavior();
            if (viewBehavior != null) {

                mBehaviorConsumed[0] = 0;
                mBehaviorConsumed[1] = 0;

                viewBehavior.onNestedScroll(this, view, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type, mBehaviorConsumed);

                xConsumed = dxUnconsumed > 0 ? Math.max(xConsumed, mBehaviorConsumed[0]) : Math.min(xConsumed, mBehaviorConsumed[0]);
                yConsumed = dyUnconsumed > 0 ? Math.max(yConsumed, mBehaviorConsumed[1]) : Math.min(yConsumed, mBehaviorConsumed[1]);

                accepted = true;
            }
        }

        consumed[0] += xConsumed;
        consumed[1] += yConsumed;

        if (accepted) {
            onChildViewsChanged(EVENT_NESTED_SCROLL);
        }
    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        onNestedPreScroll(target, dx, dy, consumed, ViewCompat.TYPE_TOUCH);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed, int  type) {
        int xConsumed = 0;
        int yConsumed = 0;
        boolean accepted = false;

        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View view = getChildAt(i);
            if (view.getVisibility() == GONE) {
                continue;
            }

            final LayoutParams lp = (LayoutParams) view.getLayoutParams();
            if (!lp.isNestedScrollAccepted(type)) {
                continue;
            }

            final Behavior viewBehavior = lp.getBehavior();
            if (viewBehavior != null) {
                mBehaviorConsumed[0] = 0;
                mBehaviorConsumed[1] = 0;
                viewBehavior.onNestedPreScroll(this, view, target, dx, dy, mBehaviorConsumed, type);

                xConsumed = dx > 0 ? Math.max(xConsumed, mBehaviorConsumed[0]) : Math.min(xConsumed, mBehaviorConsumed[0]);
                yConsumed = dy > 0 ? Math.max(yConsumed, mBehaviorConsumed[1]) : Math.min(yConsumed, mBehaviorConsumed[1]);

                accepted = true;
            }
        }

        consumed[0] = xConsumed;
        consumed[1] = yConsumed;

        if (accepted) {
            onChildViewsChanged(EVENT_NESTED_SCROLL);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
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
            onChildViewsChanged(EVENT_NESTED_SCROLL);
        }
        return handled;
    }

    @Override
    @SuppressWarnings("unchecked")
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

    @Override
    public int getNestedScrollAxes() {
        return mNestedScrollingParentHelper.getNestedScrollAxes();
    }

    class OnPreDrawListener implements ViewTreeObserver.OnPreDrawListener {
        @Override
        public boolean onPreDraw() {
            onChildViewsChanged(EVENT_PRE_DRAW);
            return true;
        }
    }

    static class ViewElevationComparator implements Comparator<View> {
        @Override
        public int compare(View lhs, View rhs) {
            final float lz = ViewCompat.getZ(lhs);
            final float rz = ViewCompat.getZ(rhs);
            if (lz > rz) {
                return -1;
            } else if (lz < rz) {
                return 1;
            }
            return 0;
        }
    }

    @Deprecated
    @Retention(RetentionPolicy.RUNTIME)
    public @interface DefaultBehavior {
        Class<? extends Behavior> value();
    }

    public interface AttachedBehavior {
        @NonNull Behavior getBehavior();
    }

    public static abstract class Behavior<V extends View> {
        public Behavior() {
        }

        public Behavior(Context context, AttributeSet attrs) {
        }

        public void onAttachedToLayoutParams(@NonNull SamsungCoordinatorLayout.LayoutParams params) {
        }

        public void onDetachedFromLayoutParams() {
        }

        protected boolean dispatchGenericMotionEvent(@NonNull MotionEvent ev) {
            return false;
        }

        public boolean onInterceptTouchEvent(@NonNull SamsungCoordinatorLayout parent, @NonNull V child, @NonNull MotionEvent ev) {
            return false;
        }

        public boolean onTouchEvent(@NonNull SamsungCoordinatorLayout parent, @NonNull V child, @NonNull MotionEvent ev) {
            return false;
        }

        @ColorInt
        public int getScrimColor(@NonNull SamsungCoordinatorLayout parent, @NonNull V child) {
            return Color.BLACK;
        }

        @FloatRange(from = 0, to = 1)
        public float getScrimOpacity(@NonNull SamsungCoordinatorLayout parent, @NonNull V child) {
            return 0.f;
        }

        public boolean blocksInteractionBelow(@NonNull SamsungCoordinatorLayout parent, @NonNull V child) {
            return getScrimOpacity(parent, child) > 0.f;
        }

        public boolean layoutDependsOn(@NonNull SamsungCoordinatorLayout parent, @NonNull V child, @NonNull View dependency) {
            return false;
        }

        public boolean onDependentViewChanged(@NonNull SamsungCoordinatorLayout parent, @NonNull V child, @NonNull View dependency) {
            return false;
        }

        public void onDependentViewRemoved(@NonNull SamsungCoordinatorLayout parent, @NonNull V child, @NonNull View dependency) {
        }

        public boolean onMeasureChild(@NonNull SamsungCoordinatorLayout parent, @NonNull V child, int parentWidthMeasureSpec, int widthUsed, int parentHeightMeasureSpec, int heightUsed) {
            return false;
        }

        public boolean onLayoutChild(@NonNull SamsungCoordinatorLayout parent, @NonNull V child, int layoutDirection) {
            return false;
        }

        public static void setTag(@NonNull View child, @Nullable Object tag) {
            final LayoutParams lp = (LayoutParams) child.getLayoutParams();
            lp.mBehaviorTag = tag;
        }

        @Nullable
        public static Object getTag(@NonNull View child) {
            final LayoutParams lp = (LayoutParams) child.getLayoutParams();
            return lp.mBehaviorTag;
        }

        @Deprecated
        public boolean onStartNestedScroll(@NonNull SamsungCoordinatorLayout coordinatorLayout, @NonNull V child, @NonNull View directTargetChild, @NonNull View target, @ScrollAxis int axes) {
            return false;
        }

        public boolean onStartNestedScroll(@NonNull SamsungCoordinatorLayout coordinatorLayout, @NonNull V child, @NonNull View directTargetChild, @NonNull View target, @ScrollAxis int axes, @NestedScrollType int type) {
            if (type == ViewCompat.TYPE_TOUCH) {
                return onStartNestedScroll(coordinatorLayout, child, directTargetChild, target, axes);
            }
            return false;
        }

        @Deprecated
        public void onNestedScrollAccepted(@NonNull SamsungCoordinatorLayout coordinatorLayout, @NonNull V child, @NonNull View directTargetChild, @NonNull View target, @ScrollAxis int axes) {
        }

        public void onNestedScrollAccepted(@NonNull SamsungCoordinatorLayout coordinatorLayout, @NonNull V child, @NonNull View directTargetChild, @NonNull View target, @ScrollAxis int axes, @NestedScrollType int type) {
            if (type == ViewCompat.TYPE_TOUCH) {
                onNestedScrollAccepted(coordinatorLayout, child, directTargetChild, target, axes);
            }
        }

        @Deprecated
        public void onStopNestedScroll(@NonNull SamsungCoordinatorLayout coordinatorLayout, @NonNull V child, @NonNull View target) {
        }

        public void onStopNestedScroll(@NonNull SamsungCoordinatorLayout coordinatorLayout, @NonNull V child, @NonNull View target, @NestedScrollType int type) {
            if (type == ViewCompat.TYPE_TOUCH) {
                onStopNestedScroll(coordinatorLayout, child, target);
            }
        }

        @Deprecated
        public void onNestedScroll(@NonNull SamsungCoordinatorLayout coordinatorLayout, @NonNull V child, @NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        }

        @Deprecated
        public void onNestedScroll(@NonNull SamsungCoordinatorLayout coordinatorLayout, @NonNull V child, @NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, @NestedScrollType int type) {
            if (type == ViewCompat.TYPE_TOUCH) {
                onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);
            }
        }

        public void onNestedScroll(@NonNull SamsungCoordinatorLayout coordinatorLayout, @NonNull V child, @NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, @NestedScrollType int type, @NonNull int[] consumed) {
            consumed[0] += dxUnconsumed;
            consumed[1] += dyUnconsumed;
            onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type);
        }

        @Deprecated
        public void onNestedPreScroll(@NonNull SamsungCoordinatorLayout coordinatorLayout, @NonNull V child, @NonNull View target, int dx, int dy, @NonNull int[] consumed) {
        }

        public void onNestedPreScroll(@NonNull SamsungCoordinatorLayout coordinatorLayout, @NonNull V child, @NonNull View target, int dx, int dy, @NonNull int[] consumed, @NestedScrollType int type) {
            if (type == ViewCompat.TYPE_TOUCH) {
                onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed);
            }
        }

        public boolean onNestedFling(@NonNull SamsungCoordinatorLayout coordinatorLayout, @NonNull V child, @NonNull View target, float velocityX, float velocityY, boolean consumed) {
            return false;
        }

        public boolean onNestedPreFling(@NonNull SamsungCoordinatorLayout coordinatorLayout, @NonNull V child, @NonNull View target, float velocityX, float velocityY) {
            return false;
        }

        @NonNull
        public WindowInsetsCompat onApplyWindowInsets(@NonNull SamsungCoordinatorLayout coordinatorLayout, @NonNull V child, @NonNull WindowInsetsCompat insets) {
            return insets;
        }

        public boolean onRequestChildRectangleOnScreen(@NonNull SamsungCoordinatorLayout coordinatorLayout, @NonNull V child, @NonNull Rect rectangle, boolean immediate) {
            return false;
        }

        public void onRestoreInstanceState(@NonNull SamsungCoordinatorLayout parent, @NonNull V child, @NonNull Parcelable state) {
        }

        @Nullable
        public Parcelable onSaveInstanceState(@NonNull SamsungCoordinatorLayout parent, @NonNull V child) {
            return BaseSavedState.EMPTY_STATE;
        }

        public boolean getInsetDodgeRect(@NonNull SamsungCoordinatorLayout parent, @NonNull V child, @NonNull Rect rect) {
            return false;
        }
    }

    public static class LayoutParams extends MarginLayoutParams {
        public int anchorGravity = Gravity.NO_GRAVITY;
        public int dodgeInsetEdges = Gravity.NO_GRAVITY;
        public int gravity = Gravity.NO_GRAVITY;
        public int insetEdge = Gravity.NO_GRAVITY;
        public int keyline = -1;
        View mAnchorDirectChild;
        int mAnchorId = View.NO_ID;
        View mAnchorView;
        Behavior mBehavior;
        boolean mBehaviorResolved = false;
        Object mBehaviorTag;
        private boolean mDidAcceptNestedScrollNonTouch;
        private boolean mDidAcceptNestedScrollTouch;
        private boolean mDidBlockInteraction;
        private boolean mDidChangeAfterNestedScroll;
        int mInsetOffsetX;
        int mInsetOffsetY;
        final Rect mLastChildRect = new Rect();

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        LayoutParams(@NonNull Context context, @Nullable AttributeSet attrs) {
            super(context, attrs);

            final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SamsungCoordinatorLayout_Layout);

            this.gravity = a.getInteger(R.styleable.SamsungCoordinatorLayout_Layout_android_layout_gravity, Gravity.NO_GRAVITY);
            mAnchorId = a.getResourceId(R.styleable.SamsungCoordinatorLayout_Layout_layout_anchor, View.NO_ID);
            this.anchorGravity = a.getInteger(R.styleable.SamsungCoordinatorLayout_Layout_layout_anchorGravity, Gravity.NO_GRAVITY);
            this.keyline = a.getInteger(R.styleable.SamsungCoordinatorLayout_Layout_layout_keyline, -1);

            insetEdge = a.getInt(R.styleable.SamsungCoordinatorLayout_Layout_layout_insetEdge, 0);
            dodgeInsetEdges = a.getInt(R.styleable.SamsungCoordinatorLayout_Layout_layout_dodgeInsetEdges, 0);
            mBehaviorResolved = a.hasValue(R.styleable.SamsungCoordinatorLayout_Layout_layout_behavior);
            if (mBehaviorResolved) {
                mBehavior = parseBehavior(context, attrs, a.getString(R.styleable.SamsungCoordinatorLayout_Layout_layout_behavior));
            }
            a.recycle();

            if (mBehavior != null) {
                mBehavior.onAttachedToLayoutParams(this);
            }
        }

        public LayoutParams(LayoutParams p) {
            super(p);
        }

        public LayoutParams(MarginLayoutParams p) {
            super(p);
        }

        public LayoutParams(ViewGroup.LayoutParams p) {
            super(p);
        }

        @IdRes
        public int getAnchorId() {
            return mAnchorId;
        }

        public void setAnchorId(@IdRes int id) {
            invalidateAnchor();
            mAnchorId = id;
        }

        @Nullable
        public Behavior getBehavior() {
            return mBehavior;
        }

        public void setBehavior(@Nullable Behavior behavior) {
            if (mBehavior != behavior) {
                if (mBehavior != null) {
                    mBehavior.onDetachedFromLayoutParams();
                }

                mBehavior = behavior;
                mBehaviorTag = null;
                mBehaviorResolved = true;

                if (behavior != null) {
                    behavior.onAttachedToLayoutParams(this);
                }
            }
        }

        void setLastChildRect(Rect r) {
            mLastChildRect.set(r);
        }

        Rect getLastChildRect() {
            return mLastChildRect;
        }

        boolean checkAnchorChanged() {
            return mAnchorView == null && mAnchorId != View.NO_ID;
        }

        boolean didBlockInteraction() {
            if (mBehavior == null) {
                mDidBlockInteraction = false;
            }
            return mDidBlockInteraction;
        }

        @SuppressWarnings("unchecked")
        boolean isBlockingInteractionBelow(SamsungCoordinatorLayout parent, View child) {
            if (mDidBlockInteraction) {
                return true;
            }

            return mDidBlockInteraction |= mBehavior != null ? mBehavior.blocksInteractionBelow(parent, child) : false;
        }

        void resetTouchBehaviorTracking() {
            mDidBlockInteraction = false;
        }

        void resetNestedScroll(int type) {
            setNestedScrollAccepted(type, false);
        }

        void setNestedScrollAccepted(int type, boolean accept) {
            switch (type) {
                case ViewCompat.TYPE_TOUCH:
                    mDidAcceptNestedScrollTouch = accept;
                    break;
                case ViewCompat.TYPE_NON_TOUCH:
                    mDidAcceptNestedScrollNonTouch = accept;
                    break;
            }
        }

        boolean isNestedScrollAccepted(int type) {
            switch (type) {
                case ViewCompat.TYPE_TOUCH:
                    return mDidAcceptNestedScrollTouch;
                case ViewCompat.TYPE_NON_TOUCH:
                    return mDidAcceptNestedScrollNonTouch;
            }
            return false;
        }

        boolean getChangedAfterNestedScroll() {
            return mDidChangeAfterNestedScroll;
        }

        void setChangedAfterNestedScroll(boolean changed) {
            mDidChangeAfterNestedScroll = changed;
        }

        void resetChangedAfterNestedScroll() {
            mDidChangeAfterNestedScroll = false;
        }

        @SuppressWarnings("unchecked")
        boolean dependsOn(SamsungCoordinatorLayout parent, View child, View dependency) {
            return dependency == mAnchorDirectChild || shouldDodge(dependency, ViewCompat.getLayoutDirection(parent)) || (mBehavior != null && mBehavior.layoutDependsOn(parent, child, dependency));
        }

        void invalidateAnchor() {
            mAnchorView = mAnchorDirectChild = null;
        }

        View findAnchorView(SamsungCoordinatorLayout parent, View forChild) {
            if (mAnchorId == View.NO_ID) {
                mAnchorView = mAnchorDirectChild = null;
                return null;
            }

            if (mAnchorView == null || !verifyAnchorView(forChild, parent)) {
                resolveAnchorView(forChild, parent);
            }
            return mAnchorView;
        }

        private void resolveAnchorView(final View forChild, final SamsungCoordinatorLayout parent) {
            mAnchorView = parent.findViewById(mAnchorId);
            if (mAnchorView != null) {
                if (mAnchorView == parent) {
                    if (parent.isInEditMode()) {
                        mAnchorView = mAnchorDirectChild = null;
                        return;
                    }
                    throw new IllegalStateException("View can not be anchored to the the parent CoordinatorLayout");
                }

                View directChild = mAnchorView;
                for (ViewParent p = mAnchorView.getParent(); p != parent && p != null; p = p.getParent()) {
                    if (p == forChild) {
                        if (parent.isInEditMode()) {
                            mAnchorView = mAnchorDirectChild = null;
                            return;
                        }
                        throw new IllegalStateException("Anchor must not be a descendant of the anchored view");
                    }
                    if (p instanceof View) {
                        directChild = (View) p;
                    }
                }
                mAnchorDirectChild = directChild;
            } else {
                if (parent.isInEditMode()) {
                    mAnchorView = mAnchorDirectChild = null;
                    return;
                }
                throw new IllegalStateException("Could not find CoordinatorLayout descendant view with id " + parent.getResources().getResourceName(mAnchorId) + " to anchor view " + forChild);
            }
        }

        private boolean verifyAnchorView(View forChild, SamsungCoordinatorLayout parent) {
            if (mAnchorView.getId() != mAnchorId) {
                return false;
            }

            View directChild = mAnchorView;
            for (ViewParent p = mAnchorView.getParent(); p != parent; p = p.getParent()) {
                if (p == null || p == forChild) {
                    mAnchorView = mAnchorDirectChild = null;
                    return false;
                }
                if (p instanceof View) {
                    directChild = (View) p;
                }
            }
            mAnchorDirectChild = directChild;
            return true;
        }

        private boolean shouldDodge(View other, int layoutDirection) {
            LayoutParams lp = (LayoutParams) other.getLayoutParams();
            final int absInset = GravityCompat.getAbsoluteGravity(lp.insetEdge, layoutDirection);
            return absInset != Gravity.NO_GRAVITY && (absInset & GravityCompat.getAbsoluteGravity(dodgeInsetEdges, layoutDirection)) == absInset;
        }
    }

    private class HierarchyChangeListener implements OnHierarchyChangeListener {
        HierarchyChangeListener() {
        }

        @Override
        public void onChildViewAdded(View parent, View child) {
            if (mOnHierarchyChangeListener != null) {
                mOnHierarchyChangeListener.onChildViewAdded(parent, child);
            }
        }

        @Override
        public void onChildViewRemoved(View parent, View child) {
            onChildViewsChanged(EVENT_VIEW_REMOVED);

            if (mOnHierarchyChangeListener != null) {
                mOnHierarchyChangeListener.onChildViewRemoved(parent, child);
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        final SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());

        final SparseArray<Parcelable> behaviorStates = ss.behaviorStates;

        for (int i = 0, count = getChildCount(); i < count; i++) {
            final View child = getChildAt(i);
            final int childId = child.getId();
            final LayoutParams lp = getResolvedLayoutParams(child);
            final Behavior b = lp.getBehavior();

            if (childId != NO_ID && b != null) {
                Parcelable savedState = behaviorStates.get(childId);
                if (savedState != null) {
                    b.onRestoreInstanceState(this, child, savedState);
                }
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Parcelable onSaveInstanceState() {
        final SavedState ss = new SavedState(super.onSaveInstanceState());

        final SparseArray<Parcelable> behaviorStates = new SparseArray<>();
        for (int i = 0, count = getChildCount(); i < count; i++) {
            final View child = getChildAt(i);
            final int childId = child.getId();
            final LayoutParams lp = (LayoutParams) child.getLayoutParams();
            final Behavior b = lp.getBehavior();

            if (childId != NO_ID && b != null) {
                Parcelable state = b.onSaveInstanceState(this, child);
                if (state != null) {
                    behaviorStates.append(childId, state);
                }
            }
        }
        ss.behaviorStates = behaviorStates;
        return ss;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean requestChildRectangleOnScreen(View child, Rect rectangle, boolean immediate) {
        final LayoutParams lp = (LayoutParams) child.getLayoutParams();
        final Behavior behavior = lp.getBehavior();

        if (behavior != null && behavior.onRequestChildRectangleOnScreen(this, child, rectangle, immediate)) {
            return true;
        }

        return super.requestChildRectangleOnScreen(child, rectangle, immediate);
    }

    @SuppressWarnings("deprecation")
    private void setupForInsets() {
        if (Build.VERSION.SDK_INT < 21) {
            return;
        }

        if (ViewCompat.getFitsSystemWindows(this)) {
            if (mApplyWindowInsetsListener == null) {
                mApplyWindowInsetsListener = new androidx.core.view.OnApplyWindowInsetsListener() {
                    @Override
                    public WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat insets) {
                        return setWindowInsets(insets);
                    }
                };
            }
            ViewCompat.setOnApplyWindowInsetsListener(this, mApplyWindowInsetsListener);

            setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        } else {
            ViewCompat.setOnApplyWindowInsetsListener(this, null);
        }
    }

    protected static class SavedState extends AbsSavedState {
        SparseArray<Parcelable> behaviorStates;

        public SavedState(Parcel source, ClassLoader loader) {
            super(source, loader);

            final int size = source.readInt();

            final int[] ids = new int[size];
            source.readIntArray(ids);

            final Parcelable[] states = source.readParcelableArray(loader);

            behaviorStates = new SparseArray<>(size);
            for (int i = 0; i < size; i++) {
                behaviorStates.append(ids[i], states[i]);
            }
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);

            final int size = behaviorStates != null ? behaviorStates.size() : 0;
            dest.writeInt(size);

            final int[] ids = new int[size];
            final Parcelable[] states = new Parcelable[size];

            for (int i = 0; i < size; i++) {
                ids[i] = behaviorStates.keyAt(i);
                states[i] = behaviorStates.valueAt(i);
            }
            dest.writeIntArray(ids);
            dest.writeParcelableArray(states, flags);

        }

        public static final Creator<SavedState> CREATOR =
                new ClassLoaderCreator<SavedState>() {
                    @Override
                    public SavedState createFromParcel(Parcel in, ClassLoader loader) {
                        return new SavedState(in, loader);
                    }

                    @Override
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in, null);
                    }

                    @Override
                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
    }

    public void seslEnableAutoCollapsingKeyEvent(boolean enable) {
        mEnableAutoCollapsingKeyEvent = enable;
    }
}
