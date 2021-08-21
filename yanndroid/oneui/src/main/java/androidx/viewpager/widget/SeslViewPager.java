package androidx.viewpager.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.FocusFinder;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityEvent;
import android.view.animation.Interpolator;
import android.widget.Scroller;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import androidx.core.content.ContextCompat;
import androidx.core.view.AccessibilityDelegateCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.customview.view.AbsSavedState;

import de.dlyt.yanndroid.oneui.widget.SamsungEdgeEffect;

public class SeslViewPager extends ViewGroup {
    public static final Comparator<ItemInfo> COMPARATOR = new Comparator<ItemInfo>() {
        public int compare(ItemInfo var1, ItemInfo var2) {
            return var1.position - var2.position;
        }
    };
    public static final int[] LAYOUT_ATTRS = new int[]{16842931};
    public static final Interpolator sInterpolator = new Interpolator() {
        public float getInterpolation(float var1) {
            --var1;
            return var1 * var1 * var1 * var1 * var1 + 1.0F;
        }
    };
    public static final ViewPositionComparator sPositionComparator = new ViewPositionComparator();
    public int mActivePointerId = -1;
    public PagerAdapter mAdapter;
    public List<OnAdapterChangeListener> mAdapterChangeListeners;
    public int mBottomPageBounds;
    public boolean mCalledSuper;
    public int mCloseEnough;
    public int mCurItem;
    public int mDecorChildCount;
    public int mDefaultGutterSize;
    public boolean mDragInGutterEnabled = true;
    public int mDrawingOrder;
    public ArrayList<View> mDrawingOrderedChildren;
    public final Runnable mEndScrollRunnable = new Runnable() {
        @Override
        public void run() {
            setScrollState(0);
            populate();
        }
    };
    public int mExpectedAdapterCount;
    public boolean mFakeDragging;
    public boolean mFirstLayout = true;
    public float mFirstOffset = -3.4028235E38F;
    public int mFlingDistance;
    public int mGutterSize;
    public boolean mInLayout;
    public float mInitialMotionX;
    public float mInitialMotionY;
    public OnPageChangeListener mInternalPageChangeListener;
    public boolean mIsBeingDragged;
    public boolean mIsChangedConfiguration = false;
    public boolean mIsMouseWheelEventSupport = false;
    public boolean mIsScrollStarted;
    public boolean mIsUnableToDrag;
    public final ArrayList<ItemInfo> mItems = new ArrayList();
    public float mLastMotionX;
    public float mLastMotionY;
    public float mLastOffset = 3.4028235E38F;
    public SamsungEdgeEffect mLeftEdge;
    public int mLeftIncr = -1;
    public Drawable mMarginDrawable;
    public int mMaximumVelocity;
    public int mMinimumVelocity;
    public PagerObserver mObserver;
    public int mOffscreenPageLimit = 1;
    public OnPageChangeListener mOnPageChangeListener;
    public List<OnPageChangeListener> mOnPageChangeListeners;
    public int mPageMargin;
    public PageTransformer mPageTransformer;
    public int mPageTransformerLayerType;
    public int mPagingTouchSlop = 0;
    public boolean mPopulatePending;
    public Parcelable mRestoredAdapterState = null;
    public ClassLoader mRestoredClassLoader = null;
    public int mRestoredCurItem = -1;
    public SamsungEdgeEffect mRightEdge;
    public int mScaledTouchSlop = 0;
    public int mScrollState;
    public Scroller mScroller;
    public boolean mScrollingCacheEnabled;
    public final ItemInfo mTempItem = new ItemInfo();
    public final Rect mTempRect = new Rect();
    public int mTopPageBounds;
    public int mTouchSlop;
    public float mTouchSlopRatio = 0.5F;
    public boolean mUsePagingTouchSlopForStylus = false;
    public VelocityTracker mVelocityTracker;

    public SeslViewPager(Context var1) {
        super(var1);
        this.mScrollState = 0;
        this.initViewPager();
    }

    public SeslViewPager(Context var1, AttributeSet var2) {
        super(var1, var2);
        this.mScrollState = 0;
        this.initViewPager();
    }

    private int getClientWidth() {
        return this.getMeasuredWidth() - this.getPaddingLeft() - this.getPaddingRight();
    }

    public static boolean isDecorView(View var0) {
        boolean var1;
        if (var0.getClass().getAnnotation(DecorView.class) != null) {
            var1 = true;
        } else {
            var1 = false;
        }

        return var1;
    }

    private void setScrollingCacheEnabled(boolean var1) {
        if (this.mScrollingCacheEnabled != var1) {
            this.mScrollingCacheEnabled = var1;
        }

    }

    @SuppressLint("WrongConstant")
    public void addFocusables(ArrayList<View> var1, int var2, int var3) {
        if (var1 != null) {
            int var4 = var1.size();
            int var5 = this.getDescendantFocusability();
            if (var5 != 393216) {
                for(int var6 = 0; var6 < this.getChildCount(); ++var6) {
                    View var7 = this.getChildAt(var6);
                    if (var7.getVisibility() == 0) {
                        ItemInfo var8 = this.infoForChild(var7);
                        if (var8 != null && var8.position == this.mCurItem) {
                            var7.addFocusables(var1, var2, var3);
                        }
                    }
                }
            }

            if (var5 != 262144 || var4 == var1.size()) {
                if (!this.isFocusable()) {
                    return;
                }

                if ((var3 & 1) == 1 && this.isInTouchMode() && !this.isFocusableInTouchMode()) {
                    return;
                }

                if (var1 != null) {
                    var1.add(this);
                }
            }

        }
    }

    public ItemInfo addNewItem(int var1, int var2) {
        ItemInfo var3 = new ItemInfo();
        var3.position = var1;
        var3.object = this.mAdapter.instantiateItem(this, var1);
        var3.widthFactor = this.mAdapter.getPageWidth(var1);
        if (var2 >= 0 && var2 < this.mItems.size()) {
            this.mItems.add(var2, var3);
        } else {
            this.mItems.add(var3);
        }

        return var3;
    }

    public void addOnAdapterChangeListener(OnAdapterChangeListener var1) {
        if (this.mAdapterChangeListeners == null) {
            this.mAdapterChangeListeners = new ArrayList();
        }

        this.mAdapterChangeListeners.add(var1);
    }

    public void addOnPageChangeListener(OnPageChangeListener var1) {
        if (this.mOnPageChangeListeners == null) {
            this.mOnPageChangeListeners = new ArrayList();
        }

        this.mOnPageChangeListeners.add(var1);
    }

    @SuppressLint("WrongConstant")
    public void addTouchables(ArrayList<View> var1) {
        for(int var2 = 0; var2 < this.getChildCount(); ++var2) {
            View var3 = this.getChildAt(var2);
            if (var3.getVisibility() == 0) {
                ItemInfo var4 = this.infoForChild(var3);
                if (var4 != null && var4.position == this.mCurItem) {
                    var3.addTouchables(var1);
                }
            }
        }

    }

    public void addView(View var1, int var2, ViewGroup.LayoutParams var3) {
        ViewGroup.LayoutParams var4 = var3;
        if (!this.checkLayoutParams(var3)) {
            var4 = this.generateLayoutParams(var3);
        }

        LayoutParams var5 = (LayoutParams)var4;
        var5.isDecor |= isDecorView(var1);
        if (this.mInLayout) {
            if (var5 != null && var5.isDecor) {
                throw new IllegalStateException("Cannot add pager decor view during layout");
            }

            var5.needsMeasure = true;
            this.addViewInLayout(var1, var2, var4);
        } else {
            super.addView(var1, var2, var4);
        }

    }

    public boolean arrowScroll(int var1) {
        View var2 = this.findFocus();
        boolean var3 = false;
        View var4 = null;
        View var5;
        if (var2 == this) {
            var5 = var4;
        } else {
            label84: {
                if (var2 != null) {
                    ViewParent var9 = var2.getParent();

                    boolean var6;
                    while(true) {
                        if (!(var9 instanceof ViewGroup)) {
                            var6 = false;
                            break;
                        }

                        if (var9 == this) {
                            var6 = true;
                            break;
                        }

                        var9 = var9.getParent();
                    }

                    if (!var6) {
                        StringBuilder var7 = new StringBuilder();
                        var7.append(var2.getClass().getSimpleName());

                        for(var9 = var2.getParent(); var9 instanceof ViewGroup; var9 = var9.getParent()) {
                            var7.append(" => ");
                            var7.append(var9.getClass().getSimpleName());
                        }

                        StringBuilder var11 = new StringBuilder();
                        var11.append("arrowScroll tried to find focus based on non-child current focused view ");
                        var11.append(var7.toString());
                        Log.e("ViewPager", var11.toString());
                        var5 = var4;
                        break label84;
                    }
                }

                var5 = var2;
            }
        }

        var4 = FocusFinder.getInstance().findNextFocus(this, var5, var1);
        if (var4 != null && var4 != var5) {
            int var8;
            int var10;
            if (var1 == 17) {
                var10 = this.getChildRectInPagerCoordinates(this.mTempRect, var4).left;
                var8 = this.getChildRectInPagerCoordinates(this.mTempRect, var5).left;
                if (var5 != null && var10 >= var8) {
                    var3 = this.pageLeft();
                } else {
                    var3 = var4.requestFocus();
                }
            } else if (var1 == 66) {
                var10 = this.getChildRectInPagerCoordinates(this.mTempRect, var4).left;
                var8 = this.getChildRectInPagerCoordinates(this.mTempRect, var5).left;
                if (var5 != null && var10 <= var8) {
                    var3 = this.pageRight();
                } else {
                    var3 = var4.requestFocus();
                }
            }
        } else if (var1 != 17 && var1 != 1) {
            if (var1 == 66 || var1 == 2) {
                var3 = this.pageRight();
            }
        } else {
            var3 = this.pageLeft();
        }

        if (var3) {
            this.playSoundEffect(SoundEffectConstants.getContantForFocusDirection(var1));
        }

        return var3;
    }

    public final void calculatePageOffsets(ItemInfo var1, int var2, ItemInfo var3) {
        int var4 = this.mAdapter.getCount();
        int var5 = this.getClientWidth();
        float var6;
        if (var5 > 0) {
            var6 = (float)this.mPageMargin / (float)var5;
        } else {
            var6 = 0.0F;
        }

        int var7;
        float var8;
        int var9;
        float var10;
        if (var3 != null) {
            var5 = var3.position;
            var7 = var1.position;
            if (var5 < var7) {
                var7 = 0;
                var8 = var3.offset + var3.widthFactor + var6;

                while(true) {
                    var9 = var5 + 1;
                    if (var9 > var1.position || var7 >= this.mItems.size()) {
                        break;
                    }

                    var3 = (ItemInfo)this.mItems.get(var7);

                    while(true) {
                        var5 = var9;
                        var10 = var8;
                        if (var9 <= var3.position) {
                            break;
                        }

                        var5 = var9;
                        var10 = var8;
                        if (var7 >= this.mItems.size() - 1) {
                            break;
                        }

                        ++var7;
                        var3 = (ItemInfo)this.mItems.get(var7);
                    }

                    while(var5 < var3.position) {
                        var10 += this.mAdapter.getPageWidth(var5) + var6;
                        ++var5;
                    }

                    var3.offset = var10;
                    var8 = var10 + var3.widthFactor + var6;
                }
            } else if (var5 > var7) {
                var7 = this.mItems.size() - 1;
                var8 = var3.offset;
                --var5;

                while(var5 >= var1.position && var7 >= 0) {
                    var3 = (ItemInfo)this.mItems.get(var7);

                    while(true) {
                        var9 = var5;
                        var10 = var8;
                        if (var5 >= var3.position) {
                            break;
                        }

                        var9 = var5;
                        var10 = var8;
                        if (var7 <= 0) {
                            break;
                        }

                        --var7;
                        var3 = (ItemInfo)this.mItems.get(var7);
                    }

                    while(var9 > var3.position) {
                        var10 -= this.mAdapter.getPageWidth(var9) + var6;
                        --var9;
                    }

                    var8 = var10 - (var3.widthFactor + var6);
                    var3.offset = var8;
                    var5 = var9 - 1;
                }
            }
        }

        var9 = this.mItems.size();
        var8 = var1.offset;
        var7 = var1.position;
        var5 = var7 - 1;
        if (var7 == 0) {
            var10 = var8;
        } else {
            var10 = -3.4028235E38F;
        }

        this.mFirstOffset = var10;
        var7 = var1.position;
        --var4;
        if (var7 == var4) {
            var10 = var1.offset + var1.widthFactor - 1.0F;
        } else {
            var10 = 3.4028235E38F;
        }

        this.mLastOffset = var10;

        for(var7 = var2 - 1; var7 >= 0; --var5) {
            var3 = (ItemInfo)this.mItems.get(var7);

            while(true) {
                int var11 = var3.position;
                if (var5 <= var11) {
                    var8 -= var3.widthFactor + var6;
                    var3.offset = var8;
                    if (var11 == 0) {
                        this.mFirstOffset = var8;
                    }

                    --var7;
                    break;
                }

                var8 -= this.mAdapter.getPageWidth(var5) + var6;
                --var5;
            }
        }

        var8 = var1.offset + var1.widthFactor + var6;
        var7 = var1.position + 1;
        var5 = var2 + 1;

        for(var2 = var7; var5 < var9; ++var2) {
            var1 = (ItemInfo)this.mItems.get(var5);

            while(true) {
                var7 = var1.position;
                if (var2 >= var7) {
                    if (var7 == var4) {
                        this.mLastOffset = var1.widthFactor + var8 - 1.0F;
                    }

                    var1.offset = var8;
                    var8 += var1.widthFactor + var6;
                    ++var5;
                    break;
                }

                var8 += this.mAdapter.getPageWidth(var2) + var6;
                ++var2;
            }
        }

    }

    public boolean canScroll(View var1, boolean var2, int var3, int var4, int var5) {
        boolean var6 = var1 instanceof ViewGroup;
        boolean var7 = true;
        if (var6) {
            ViewGroup var8 = (ViewGroup)var1;
            int var9 = var1.getScrollX();
            int var10 = var1.getScrollY();

            for(int var11 = var8.getChildCount() - 1; var11 >= 0; --var11) {
                View var12 = var8.getChildAt(var11);
                int var13 = var4 + var9;
                if (var13 >= var12.getLeft() && var13 < var12.getRight()) {
                    int var14 = var5 + var10;
                    if (var14 >= var12.getTop() && var14 < var12.getBottom() && this.canScroll(var12, true, var3, var13 - var12.getLeft(), var14 - var12.getTop())) {
                        return true;
                    }
                }
            }
        }

        if (var2 && var1.canScrollHorizontally(-var3)) {
            var2 = var7;
        } else {
            var2 = false;
        }

        return var2;
    }

    public boolean canScrollHorizontally(int var1) {
        PagerAdapter var2 = this.mAdapter;
        boolean var3 = false;
        boolean var4 = false;
        if (var2 == null) {
            return false;
        } else {
            int var5 = this.getClientWidth();
            int var6 = this.getScrollX();
            if (var1 < 0) {
                if (var6 > (int)((float)var5 * this.mFirstOffset)) {
                    var4 = true;
                }

                return var4;
            } else {
                var4 = var3;
                if (var1 > 0) {
                    var4 = var3;
                    if (var6 < (int)((float)var5 * this.mLastOffset)) {
                        var4 = true;
                    }
                }

                return var4;
            }
        }
    }

    public boolean checkLayoutParams(ViewGroup.LayoutParams var1) {
        boolean var2;
        if (var1 instanceof LayoutParams && super.checkLayoutParams(var1)) {
            var2 = true;
        } else {
            var2 = false;
        }

        return var2;
    }

    public final void completeScroll(boolean var1) {
        boolean var2;
        if (this.mScrollState == 2) {
            var2 = true;
        } else {
            var2 = false;
        }

        if (var2) {
            this.setScrollingCacheEnabled(false);
            if (this.mScroller.isFinished() ^ true) {
                this.mScroller.abortAnimation();
                int var3 = this.getScrollX();
                int var4 = this.getScrollY();
                int var5 = this.mScroller.getCurrX();
                int var6 = this.mScroller.getCurrY();
                if (var3 != var5 || var4 != var6) {
                    this.scrollTo(var5, var6);
                    if (var5 != var3) {
                        this.pageScrolled(var5);
                    }
                }
            }
        }

        this.mPopulatePending = false;
        byte var10 = 0;
        boolean var9 = var2;

        for(int var8 = var10; var8 < this.mItems.size(); ++var8) {
            ItemInfo var7 = (ItemInfo)this.mItems.get(var8);
            if (var7.scrolling) {
                var7.scrolling = false;
                var9 = true;
            }
        }

        if (var9) {
            if (var1) {
                ViewCompat.postOnAnimation(this, this.mEndScrollRunnable);
            } else {
                this.mEndScrollRunnable.run();
            }
        }

    }

    public void computeScroll() {
        this.mIsScrollStarted = true;
        if (!this.mScroller.isFinished() && this.mScroller.computeScrollOffset()) {
            int var1 = this.getScrollX();
            int var2 = this.getScrollY();
            int var3 = this.mScroller.getCurrX();
            int var4 = this.mScroller.getCurrY();
            if (var1 != var3 || var2 != var4) {
                this.scrollTo(var3, var4);
                if (!this.pageScrolled(var3)) {
                    this.mScroller.abortAnimation();
                    this.scrollTo(0, var4);
                }
            }

            ViewCompat.postInvalidateOnAnimation(this);
        } else {
            this.completeScroll(true);
        }
    }

    public void dataSetChanged() {
        int var1 = this.mAdapter.getCount();
        this.mExpectedAdapterCount = var1;
        boolean var2;
        if (this.mItems.size() < this.mOffscreenPageLimit * 2 + 1 && this.mItems.size() < var1) {
            var2 = true;
        } else {
            var2 = false;
        }

        int var3 = this.mCurItem;
        boolean var4 = var2;
        int var12 = var3;
        int var5 = 0;

        int var10;
        for(var3 = var5; var5 < this.mItems.size(); var12 = var10) {
            ItemInfo var6 = (ItemInfo)this.mItems.get(var5);
            int var7 = this.mAdapter.getItemPosition(var6.object);
            int var8;
            int var9;
            if (var7 == -1) {
                var8 = var5;
                var9 = var3;
                var10 = var12;
            } else {
                label74: {
                    if (var7 == -2) {
                        this.mItems.remove(var5);
                        var9 = var5 - 1;
                        var8 = var3;
                        if (var3 == 0) {
                            this.mAdapter.startUpdate(this);
                            var8 = 1;
                        }

                        this.mAdapter.destroyItem(this, var6.position, var6.object);
                        var10 = this.mCurItem;
                        var5 = var9;
                        var3 = var8;
                        if (var10 == var6.position) {
                            var12 = Math.max(0, Math.min(var10, var1 - 1));
                            var3 = var8;
                            var5 = var9;
                        }
                    } else {
                        int var11 = var6.position;
                        var8 = var5;
                        var9 = var3;
                        var10 = var12;
                        if (var11 == var7) {
                            break label74;
                        }

                        if (var11 == this.mCurItem) {
                            var12 = var7;
                        }

                        var6.position = var7;
                    }

                    var4 = true;
                    var8 = var5;
                    var9 = var3;
                    var10 = var12;
                }
            }

            var5 = var8 + 1;
            var3 = var9;
        }

        if (var3 != 0) {
            this.mAdapter.finishUpdate(this);
        }

        Collections.sort(this.mItems, COMPARATOR);
        if (var4) {
            var5 = this.getChildCount();

            for(var3 = 0; var3 < var5; ++var3) {
                LayoutParams var13 = (LayoutParams)this.getChildAt(var3).getLayoutParams();
                if (!var13.isDecor) {
                    var13.widthFactor = 0.0F;
                }
            }

            this.setCurrentItemInternal(var12, false, true);
            this.requestLayout();
        }

    }

    public final int determineTargetPage(int var1, float var2, int var3, int var4) {
        if (Math.abs(var4) > this.mFlingDistance && Math.abs(var3) > this.mMinimumVelocity) {
            if (var3 > 0) {
                var3 = 0;
            } else {
                var3 = this.mLeftIncr;
            }

            var1 -= var3;
        } else {
            float var5;
            if (var1 >= this.mCurItem) {
                var5 = 0.4F;
            } else {
                var5 = 0.6F;
            }

            var1 -= this.mLeftIncr * (int)(var2 + var5);
        }

        var3 = var1;
        if (this.mItems.size() > 0) {
            ItemInfo var6 = (ItemInfo)this.mItems.get(0);
            ArrayList var7 = this.mItems;
            ItemInfo var8 = (ItemInfo)var7.get(var7.size() - 1);
            var3 = constrain(var1, var6.position, var8.position);
        }

        return var3;
    }

    public boolean dispatchKeyEvent(KeyEvent var1) {
        boolean var2;
        if (!super.dispatchKeyEvent(var1) && !this.executeKeyEvent(var1)) {
            var2 = false;
        } else {
            var2 = true;
        }

        return var2;
    }

    public final void dispatchOnPageScrolled(int var1, float var2, int var3) {
        OnPageChangeListener var4 = this.mOnPageChangeListener;
        if (var4 != null) {
            var4.onPageScrolled(var1, var2, var3);
        }

        List var7 = this.mOnPageChangeListeners;
        if (var7 != null) {
            int var5 = 0;

            for(int var6 = var7.size(); var5 < var6; ++var5) {
                var4 = (OnPageChangeListener)this.mOnPageChangeListeners.get(var5);
                if (var4 != null) {
                    var4.onPageScrolled(var1, var2, var3);
                }
            }
        }

        var4 = this.mInternalPageChangeListener;
        if (var4 != null) {
            var4.onPageScrolled(var1, var2, var3);
        }

    }

    public final void dispatchOnPageSelected(int var1) {
        OnPageChangeListener var2 = this.mOnPageChangeListener;
        if (var2 != null) {
            var2.onPageSelected(var1);
        }

        List var5 = this.mOnPageChangeListeners;
        if (var5 != null) {
            int var3 = 0;

            for(int var4 = var5.size(); var3 < var4; ++var3) {
                var2 = (OnPageChangeListener)this.mOnPageChangeListeners.get(var3);
                if (var2 != null) {
                    var2.onPageSelected(var1);
                }
            }
        }

        var2 = this.mInternalPageChangeListener;
        if (var2 != null) {
            var2.onPageSelected(var1);
        }

    }

    public final void dispatchOnScrollStateChanged(int var1) {
        OnPageChangeListener var2 = this.mOnPageChangeListener;
        if (var2 != null) {
            var2.onPageScrollStateChanged(var1);
        }

        List var5 = this.mOnPageChangeListeners;
        if (var5 != null) {
            int var3 = 0;

            for(int var4 = var5.size(); var3 < var4; ++var3) {
                var2 = (OnPageChangeListener)this.mOnPageChangeListeners.get(var3);
                if (var2 != null) {
                    var2.onPageScrollStateChanged(var1);
                }
            }
        }

        var2 = this.mInternalPageChangeListener;
        if (var2 != null) {
            var2.onPageScrollStateChanged(var1);
        }

    }

    @SuppressLint("WrongConstant")
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent var1) {
        if (var1.getEventType() == 4096) {
            return super.dispatchPopulateAccessibilityEvent(var1);
        } else {
            int var2 = this.getChildCount();

            for(int var3 = 0; var3 < var2; ++var3) {
                View var4 = this.getChildAt(var3);
                if (var4.getVisibility() == 0) {
                    ItemInfo var5 = this.infoForChild(var4);
                    if (var5 != null && var5.position == this.mCurItem && var4.dispatchPopulateAccessibilityEvent(var1)) {
                        return true;
                    }
                }
            }

            return false;
        }
    }

    public float distanceInfluenceForSnapDuration(float var1) {
        return (float)Math.sin((double)((var1 - 0.5F) * 0.47123894F));
    }

    public void draw(Canvas var1) {
        boolean var3;
        label41: {
            label44: {
                super.draw(var1);
                int var2 = this.getOverScrollMode();
                var3 = false;
                boolean var4 = false;
                if (var2 != 0) {
                    if (var2 != 1) {
                        break label44;
                    }

                    PagerAdapter var5 = this.mAdapter;
                    if (var5 == null || var5.getCount() <= 1) {
                        break label44;
                    }
                }

                int var9;
                if (!this.mLeftEdge.isFinished()) {
                    var9 = var1.save();
                    int var10 = this.getHeight() - this.getPaddingTop() - this.getPaddingBottom();
                    var2 = this.getWidth();
                    var1.rotate(270.0F);
                    if (this.seslIsLayoutRtl()) {
                        var1.translate((float)(-var10 + this.getPaddingTop()), -(this.mLastOffset + 1.0F) * (float)var2 + 1.6777216E7F);
                    } else {
                        var1.translate((float)(-var10 + this.getPaddingTop()), this.mFirstOffset * (float)var2);
                    }

                    this.mLeftEdge.setSize(var10, var2);
                    var4 = false | this.mLeftEdge.draw(var1);
                    var1.restoreToCount(var9);
                }

                var3 = var4;
                if (!this.mRightEdge.isFinished()) {
                    var2 = var1.save();
                    int var6 = this.getWidth();
                    var9 = this.getHeight();
                    int var7 = this.getPaddingTop();
                    int var8 = this.getPaddingBottom();
                    var1.rotate(90.0F);
                    if (this.seslIsLayoutRtl()) {
                        var1.translate((float)(-this.getPaddingTop()), this.mFirstOffset * (float)var6 - 1.6777216E7F);
                    } else {
                        var1.translate((float)(-this.getPaddingTop()), -(this.mLastOffset + 1.0F) * (float)var6);
                    }

                    this.mRightEdge.setSize(var9 - var7 - var8, var6);
                    var3 = var4 | this.mRightEdge.draw(var1);
                    var1.restoreToCount(var2);
                }
                break label41;
            }

            this.mLeftEdge.finish();
            this.mRightEdge.finish();
        }

        if (var3) {
            ViewCompat.postInvalidateOnAnimation(this);
        }

    }

    public void drawableStateChanged() {
        super.drawableStateChanged();
        Drawable var1 = this.mMarginDrawable;
        if (var1 != null && var1.isStateful()) {
            var1.setState(this.getDrawableState());
        }

    }

    public final void enableLayers(boolean var1) {
        int var2 = this.getChildCount();

        for(int var3 = 0; var3 < var2; ++var3) {
            int var4;
            if (var1) {
                var4 = this.mPageTransformerLayerType;
            } else {
                var4 = 0;
            }

            this.getChildAt(var3).setLayerType(var4, (Paint)null);
        }

    }

    public final void endDrag() {
        this.mIsBeingDragged = false;
        this.mIsUnableToDrag = false;
        VelocityTracker var1 = this.mVelocityTracker;
        if (var1 != null) {
            var1.recycle();
            this.mVelocityTracker = null;
        }

    }

    public boolean executeKeyEvent(KeyEvent var1) {
        boolean var3;
        if (var1.getAction() == 0) {
            int var2 = var1.getKeyCode();
            if (var2 == 21) {
                if (var1.hasModifiers(2)) {
                    var3 = this.pageLeft();
                } else {
                    var3 = this.arrowScroll(17);
                }

                return var3;
            }

            if (var2 == 22) {
                if (var1.hasModifiers(2)) {
                    var3 = this.pageRight();
                } else {
                    var3 = this.arrowScroll(66);
                }

                return var3;
            }

            if (var2 == 61) {
                if (var1.hasNoModifiers()) {
                    var3 = this.arrowScroll(2);
                    return var3;
                }

                if (var1.hasModifiers(1)) {
                    var3 = this.arrowScroll(1);
                    return var3;
                }
            }
        }

        var3 = false;
        return var3;
    }

    public ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams();
    }

    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet var1) {
        return new LayoutParams(this.getContext(), var1);
    }

    public ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams var1) {
        return this.generateDefaultLayoutParams();
    }

    public PagerAdapter getAdapter() {
        return this.mAdapter;
    }

    public int getChildDrawingOrder(int var1, int var2) {
        int var3 = var2;
        if (this.mDrawingOrder == 2) {
            var3 = var1 - 1 - var2;
        }

        return ((LayoutParams)((View)this.mDrawingOrderedChildren.get(var3)).getLayoutParams()).childIndex;
    }

    public final Rect getChildRectInPagerCoordinates(Rect var1, View var2) {
        Rect var3 = var1;
        if (var1 == null) {
            var3 = new Rect();
        }

        if (var2 == null) {
            var3.set(0, 0, 0, 0);
            return var3;
        } else {
            var3.left = var2.getLeft();
            var3.right = var2.getRight();
            var3.top = var2.getTop();
            var3.bottom = var2.getBottom();

            ViewGroup var5;
            for(ViewParent var4 = var2.getParent(); var4 instanceof ViewGroup && var4 != this; var4 = var5.getParent()) {
                var5 = (ViewGroup)var4;
                var3.left += var5.getLeft();
                var3.right += var5.getRight();
                var3.top += var5.getTop();
                var3.bottom += var5.getBottom();
            }

            return var3;
        }
    }

    public int getCurrentItem() {
        return this.mCurItem;
    }

    public int getOffscreenPageLimit() {
        return this.mOffscreenPageLimit;
    }

    public int getPageMargin() {
        return this.mPageMargin;
    }

    public ItemInfo infoForAnyChild(View var1) {
        while(true) {
            ViewParent var2 = var1.getParent();
            if (var2 == this) {
                return this.infoForChild(var1);
            }

            if (!(var2 instanceof View)) {
                return null;
            }

            var1 = (View)var2;
        }
    }

    public ItemInfo infoForChild(View var1) {
        for(int var2 = 0; var2 < this.mItems.size(); ++var2) {
            ItemInfo var3 = (ItemInfo)this.mItems.get(var2);
            if (this.mAdapter.isViewFromObject(var1, var3.object)) {
                return var3;
            }
        }

        return null;
    }

    public final ItemInfo infoForCurrentScrollPosition() {
        int var1 = this.seslGetScrollStart();
        int var2 = this.getClientWidth();
        float var3;
        if (var2 > 0) {
            var3 = (float)var1 / (float)var2;
        } else {
            var3 = 0.0F;
        }

        float var4;
        if (var2 > 0) {
            var4 = (float)this.mPageMargin / (float)var2;
        } else {
            var4 = 0.0F;
        }

        ItemInfo var5 = null;
        float var6 = 0.0F;
        float var7 = var6;
        var1 = 0;
        int var8 = -1;

        ItemInfo var11;
        for(boolean var13 = true; var1 < this.mItems.size(); var5 = var11) {
            ItemInfo var9 = (ItemInfo)this.mItems.get(var1);
            int var10 = var1;
            var11 = var9;
            if (!var13) {
                int var12 = var9.position;
                ++var8;
                var10 = var1;
                var11 = var9;
                if (var12 != var8) {
                    var11 = this.mTempItem;
                    var11.offset = var6 + var7 + var4;
                    var11.position = var8;
                    var11.widthFactor = this.mAdapter.getPageWidth(var11.position);
                    var10 = var1 - 1;
                }
            }

            var6 = var11.offset;
            var7 = var11.widthFactor;
            if (!var13 && var3 < var6) {
                return var5;
            }

            if (var3 < var7 + var6 + var4 || var10 == this.mItems.size() - 1) {
                return var11;
            }

            var8 = var11.position;
            var7 = var11.widthFactor;
            var1 = var10 + 1;
            var13 = false;
        }

        return var5;
    }

    public ItemInfo infoForPosition(int var1) {
        for(int var2 = 0; var2 < this.mItems.size(); ++var2) {
            ItemInfo var3 = (ItemInfo)this.mItems.get(var2);
            if (var3.position == var1) {
                return var3;
            }
        }

        return null;
    }

    @SuppressLint("WrongConstant")
    public void initViewPager() {
        this.setWillNotDraw(false);
        this.setDescendantFocusability(262144);
        this.setFocusable(true);
        Context var1 = this.getContext();
        this.mScroller = new Scroller(var1, sInterpolator);
        ViewConfiguration var2 = ViewConfiguration.get(var1);
        float var3 = var1.getResources().getDisplayMetrics().density;
        this.mTouchSlop = var2.getScaledPagingTouchSlop();
        this.mScaledTouchSlop = var2.getScaledTouchSlop();
        this.mPagingTouchSlop = var2.getScaledPagingTouchSlop();
        this.mMinimumVelocity = (int)(400.0F * var3);
        this.mMaximumVelocity = var2.getScaledMaximumFlingVelocity();
        this.mLeftEdge = new SamsungEdgeEffect(var1);
        this.mRightEdge = new SamsungEdgeEffect(var1);
        this.mLeftEdge.setSeslHostView(this);
        this.mRightEdge.setSeslHostView(this);
        this.mFlingDistance = (int)(25.0F * var3);
        this.mCloseEnough = (int)(2.0F * var3);
        this.mDefaultGutterSize = (int)(var3 * 16.0F);
        ViewCompat.setAccessibilityDelegate(this, new MyAccessibilityDelegate());
        if (ViewCompat.getImportantForAccessibility(this) == 0) {
            ViewCompat.setImportantForAccessibility(this, 1);
        }

        ViewCompat.setOnApplyWindowInsetsListener(this, new androidx.core.view.OnApplyWindowInsetsListener() {
            public final Rect mTempRect = new Rect();

            public WindowInsetsCompat onApplyWindowInsets(View var1, WindowInsetsCompat var2) {
                WindowInsetsCompat var6 = ViewCompat.onApplyWindowInsets(var1, var2);
                if (var6.isConsumed()) {
                    return var6;
                } else {
                    Rect var3 = this.mTempRect;
                    var3.left = var6.getSystemWindowInsetLeft();
                    var3.top = var6.getSystemWindowInsetTop();
                    var3.right = var6.getSystemWindowInsetRight();
                    var3.bottom = var6.getSystemWindowInsetBottom();
                    int var4 = 0;

                    for(int var5 = SeslViewPager.this.getChildCount(); var4 < var5; ++var4) {
                        var2 = ViewCompat.dispatchApplyWindowInsets(SeslViewPager.this.getChildAt(var4), var6);
                        var3.left = Math.min(var2.getSystemWindowInsetLeft(), var3.left);
                        var3.top = Math.min(var2.getSystemWindowInsetTop(), var3.top);
                        var3.right = Math.min(var2.getSystemWindowInsetRight(), var3.right);
                        var3.bottom = Math.min(var2.getSystemWindowInsetBottom(), var3.bottom);
                    }

                    return var6.replaceSystemWindowInsets(var3.left, var3.top, var3.right, var3.bottom);
                }
            }
        });
    }

    public final boolean isGutterDrag(float var1, float var2) {
        boolean var3 = this.mDragInGutterEnabled;
        boolean var4 = false;
        if (var3) {
            return false;
        } else {
            if (var1 >= (float)this.mGutterSize || var2 <= 0.0F) {
                var3 = var4;
                if (var1 <= (float)(this.getWidth() - this.mGutterSize)) {
                    return var3;
                }

                var3 = var4;
                if (var2 >= 0.0F) {
                    return var3;
                }
            }

            var3 = true;
            return var3;
        }
    }

    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mFirstLayout = true;
    }

    public void onDetachedFromWindow() {
        this.removeCallbacks(this.mEndScrollRunnable);
        Scroller var1 = this.mScroller;
        if (var1 != null && !var1.isFinished()) {
            this.mScroller.abortAnimation();
        }

        super.onDetachedFromWindow();
    }

    public void onDraw(Canvas var1) {
        super.onDraw(var1);
        if (this.mPageMargin > 0 && this.mMarginDrawable != null && this.mItems.size() > 0 && this.mAdapter != null) {
            int var2 = this.getScrollX();
            int var3 = this.getWidth();
            float var4 = (float)this.mPageMargin;
            float var5 = (float)var3;
            float var6 = var4 / var5;
            ArrayList var7 = this.mItems;
            int var8 = 0;
            ItemInfo var14 = (ItemInfo)var7.get(0);
            float var9 = var14.offset;
            int var10 = this.mItems.size();
            int var11 = var14.position;

            for(int var12 = ((ItemInfo)this.mItems.get(var10 - 1)).position; var11 < var12; var9 = var4) {
                while(var11 > var14.position && var8 < var10) {
                    var7 = this.mItems;
                    ++var8;
                    var14 = (ItemInfo)var7.get(var8);
                }

                if (var11 == var14.position) {
                    if (this.seslIsLayoutRtl()) {
                        var9 = 1.6777216E7F - var14.offset;
                    } else {
                        var9 = (var14.offset + var14.widthFactor) * var5;
                    }

                    var4 = var14.offset + var14.widthFactor + var6;
                } else {
                    float var13 = this.mAdapter.getPageWidth(var11);
                    if (this.seslIsLayoutRtl()) {
                        var4 = 1.6777216E7F - var9;
                    } else {
                        var4 = (var9 + var13) * var5;
                    }

                    var13 = var9 + var13 + var6;
                    var9 = var4;
                    var4 = var13;
                }

                if ((float)this.mPageMargin + var9 > (float)var2) {
                    this.mMarginDrawable.setBounds(Math.round(var9), this.mTopPageBounds, Math.round((float)this.mPageMargin + var9), this.mBottomPageBounds);
                    this.mMarginDrawable.draw(var1);
                }

                if (var9 > (float)(var2 + var3)) {
                    break;
                }

                ++var11;
            }
        }

    }

    public boolean onGenericMotionEvent(MotionEvent var1) {
        if (this.mIsMouseWheelEventSupport && (var1.getSource() & 2) != 0 && var1.getAction() == 8) {
            float var2 = var1.getAxisValue(9);
            if (var2 > 0.0F) {
                this.setCurrentItem(this.mCurItem - 1, true);
                return true;
            }

            if (var2 < 0.0F) {
                this.setCurrentItem(this.mCurItem + 1, true);
                return true;
            }
        }

        return super.onGenericMotionEvent(var1);
    }

    public boolean onInterceptTouchEvent(MotionEvent var1) {
        int var2 = var1.getAction() & 255;
        if (var2 != 3 && var2 != 1) {
            if (var2 != 0) {
                if (this.mIsBeingDragged) {
                    return true;
                }

                if (this.mIsUnableToDrag) {
                    return false;
                }
            }

            float var7;
            if (var2 != 0) {
                if (var2 != 2) {
                    if (var2 == 6) {
                        this.onSecondaryPointerUp(var1);
                    }
                } else {
                    var2 = this.mActivePointerId;
                    if (var2 != -1) {
                        var2 = var1.findPointerIndex(var2);
                        float var3 = var1.getX(var2);
                        float var4 = var3 - this.mLastMotionX;
                        float var5 = Math.abs(var4);
                        float var6 = var1.getY(var2);
                        var7 = Math.abs(var6 - this.mInitialMotionY);
                        float var8;
                        var2 = (var8 = var4 - 0.0F) == 0.0F ? 0 : (var8 < 0.0F ? -1 : 1);
                        if (var2 != 0 && !this.isGutterDrag(this.mLastMotionX, var4) && this.canScroll(this, false, (int)var4, (int)var3, (int)var6)) {
                            this.mLastMotionX = var3;
                            this.mLastMotionY = var6;
                            this.mIsUnableToDrag = true;
                            return false;
                        }

                        if (var5 > (float)this.mTouchSlop && var5 * this.mTouchSlopRatio > var7) {
                            this.mIsBeingDragged = true;
                            this.requestParentDisallowInterceptTouchEvent(true);
                            this.setScrollState(1);
                            if (var2 > 0) {
                                var7 = this.mInitialMotionX + (float)this.mTouchSlop;
                            } else {
                                var7 = this.mInitialMotionX - (float)this.mTouchSlop;
                            }

                            this.mLastMotionX = var7;
                            this.mLastMotionY = var6;
                            this.setScrollingCacheEnabled(true);
                        } else if (var7 > (float)this.mTouchSlop) {
                            this.mIsUnableToDrag = true;
                        }

                        if (this.mIsBeingDragged && this.performDrag(var3)) {
                            ViewCompat.postInvalidateOnAnimation(this);
                        }
                    }
                }
            } else {
                var7 = var1.getX();
                this.mInitialMotionX = var7;
                this.mLastMotionX = var7;
                var7 = var1.getY();
                this.mInitialMotionY = var7;
                this.mLastMotionY = var7;
                this.mActivePointerId = var1.getPointerId(0);
                this.mIsUnableToDrag = false;
                this.mIsScrollStarted = true;
                if (this.mUsePagingTouchSlopForStylus) {
                    if (var1.isFromSource(16386)) {
                        this.mTouchSlop = this.mPagingTouchSlop;
                    } else {
                        this.mTouchSlop = this.mScaledTouchSlop;
                    }
                }

                this.mScroller.computeScrollOffset();
                if (this.mScrollState == 2 && Math.abs(this.mScroller.getFinalX() - this.mScroller.getCurrX()) > this.mCloseEnough) {
                    this.mScroller.abortAnimation();
                    this.mPopulatePending = false;
                    this.populate();
                    this.mIsBeingDragged = true;
                    this.requestParentDisallowInterceptTouchEvent(true);
                    this.setScrollState(1);
                } else {
                    this.completeScroll(false);
                    this.mIsBeingDragged = false;
                }
            }

            if (this.mVelocityTracker == null) {
                this.mVelocityTracker = VelocityTracker.obtain();
            }

            this.mVelocityTracker.addMovement(var1);
            return this.mIsBeingDragged;
        } else {
            this.resetTouch();
            return false;
        }
    }

    @SuppressLint("WrongConstant")
    public void onLayout(boolean var1, int var2, int var3, int var4, int var5) {
        int var6 = this.getChildCount();
        int var7 = var4 - var2;
        int var8 = var5 - var3;
        var3 = this.getPaddingLeft();
        var2 = this.getPaddingTop();
        int var9 = this.getPaddingRight();
        var5 = this.getPaddingBottom();
        int var10 = this.getScrollX();
        int var11 = 0;

        View var13;
        int var14;
        LayoutParams var18;
        for(int var12 = 0; var12 < var6; var11 = var4) {
            var13 = this.getChildAt(var12);
            var14 = var3;
            int var15 = var9;
            int var16 = var2;
            int var17 = var5;
            var4 = var11;
            if (var13.getVisibility() != 8) {
                var18 = (LayoutParams)var13.getLayoutParams();
                var14 = var3;
                var15 = var9;
                var16 = var2;
                var17 = var5;
                var4 = var11;
                if (var18.isDecor) {
                    var14 = var18.gravity;
                    var4 = var14 & 7;
                    var17 = var14 & 112;
                    if (var4 != 1) {
                        if (var4 != 3) {
                            if (var4 != 5) {
                                var4 = var3;
                                var14 = var3;
                            } else {
                                var4 = var7 - var9 - var13.getMeasuredWidth();
                                var9 += var13.getMeasuredWidth();
                                var14 = var3;
                            }
                        } else {
                            var14 = var13.getMeasuredWidth();
                            var4 = var3;
                            var14 += var3;
                        }
                    } else {
                        var4 = Math.max((var7 - var13.getMeasuredWidth()) / 2, var3);
                        var14 = var3;
                    }

                    if (var17 != 16) {
                        if (var17 != 48) {
                            if (var17 != 80) {
                                var3 = var2;
                            } else {
                                var3 = var8 - var5 - var13.getMeasuredHeight();
                                var5 += var13.getMeasuredHeight();
                            }
                        } else {
                            var17 = var13.getMeasuredHeight();
                            var3 = var2;
                            var2 += var17;
                        }
                    } else {
                        var3 = Math.max((var8 - var13.getMeasuredHeight()) / 2, var2);
                    }

                    var4 += var10;
                    var13.layout(var4, var3, var13.getMeasuredWidth() + var4, var3 + var13.getMeasuredHeight());
                    var4 = var11 + 1;
                    var17 = var5;
                    var16 = var2;
                    var15 = var9;
                }
            }

            ++var12;
            var3 = var14;
            var9 = var15;
            var2 = var16;
            var5 = var17;
        }

        for(var4 = 0; var4 < var6; ++var4) {
            var13 = this.getChildAt(var4);
            if (var13.getVisibility() != 8) {
                var18 = (LayoutParams)var13.getLayoutParams();
                if (!var18.isDecor) {
                    ItemInfo var19 = this.infoForChild(var13);
                    if (var19 != null) {
                        float var20 = (float)(var7 - var3 - var9);
                        var14 = (int)(var19.offset * var20);
                        if (this.seslIsLayoutRtl()) {
                            var14 = 16777216 - var9 - var14 - var13.getMeasuredWidth();
                        } else {
                            var14 += var3;
                        }

                        if (var18.needsMeasure) {
                            var18.needsMeasure = false;
                            var13.measure(MeasureSpec.makeMeasureSpec((int)(var20 * var18.widthFactor), 1073741824), MeasureSpec.makeMeasureSpec(var8 - var2 - var5, 1073741824));
                        }

                        var13.layout(var14, var2, var13.getMeasuredWidth() + var14, var13.getMeasuredHeight() + var2);
                    }
                }
            }
        }

        this.mTopPageBounds = var2;
        this.mBottomPageBounds = var8 - var5;
        this.mDecorChildCount = var11;
        if (this.mFirstLayout || this.mIsChangedConfiguration) {
            this.scrollToItem(this.mCurItem, false, 0, false);
            this.mIsChangedConfiguration = false;
        }

        this.mFirstLayout = false;
    }

    @SuppressLint("WrongConstant")
    public void onMeasure(int var1, int var2) {
        byte var3 = 0;
        this.setMeasuredDimension(ViewGroup.getDefaultSize(0, var1), ViewGroup.getDefaultSize(0, var2));
        var1 = this.getMeasuredWidth();
        this.mGutterSize = Math.min(var1 / 10, this.mDefaultGutterSize);
        int var4 = this.getPaddingLeft();
        int var5 = this.getPaddingRight();
        var2 = this.getMeasuredHeight();
        int var6 = this.getPaddingTop();
        int var7 = this.getPaddingBottom();
        int var8 = this.getChildCount();
        var2 = var2 - var6 - var7;
        var1 = var1 - var4 - var5;
        var6 = 0;

        while(true) {
            boolean var9 = true;
            int var10 = 1073741824;
            View var11;
            LayoutParams var12;
            if (var6 >= var8) {
                MeasureSpec.makeMeasureSpec(var1, 1073741824);
                var4 = MeasureSpec.makeMeasureSpec(var2, 1073741824);
                this.mInLayout = true;
                this.populate();
                this.mInLayout = false;
                var5 = this.getChildCount();

                for(var2 = var3; var2 < var5; ++var2) {
                    var11 = this.getChildAt(var2);
                    if (var11.getVisibility() != 8) {
                        var12 = (LayoutParams)var11.getLayoutParams();
                        if (var12 == null || !var12.isDecor) {
                            var11.measure(MeasureSpec.makeMeasureSpec((int)((float)var1 * var12.widthFactor), 1073741824), var4);
                        }
                    }
                }

                return;
            }

            var11 = this.getChildAt(var6);
            var5 = var2;
            var4 = var1;
            if (var11.getVisibility() != 8) {
                var12 = (LayoutParams)var11.getLayoutParams();
                var5 = var2;
                var4 = var1;
                if (var12 != null) {
                    var5 = var2;
                    var4 = var1;
                    if (var12.isDecor) {
                        var5 = var12.gravity;
                        var4 = var5 & 7;
                        var5 &= 112;
                        boolean var13;
                        if (var5 != 48 && var5 != 80) {
                            var13 = false;
                        } else {
                            var13 = true;
                        }

                        boolean var15 = var9;
                        if (var4 != 3) {
                            if (var4 == 5) {
                                var15 = var9;
                            } else {
                                var15 = false;
                            }
                        }

                        var5 = -2147483648;
                        if (var13) {
                            var4 = -2147483648;
                            var5 = 1073741824;
                        } else if (var15) {
                            var4 = 1073741824;
                        } else {
                            var4 = -2147483648;
                        }

                        int var16 = var12.width;
                        int var14;
                        if (var16 != -2) {
                            if (var16 != -1) {
                                var5 = var16;
                            } else {
                                var5 = var1;
                            }

                            var16 = 1073741824;
                            var14 = var5;
                        } else {
                            var14 = var1;
                            var16 = var5;
                        }

                        var5 = var12.height;
                        if (var5 != -2) {
                            if (var5 != -1) {
                                var4 = var5;
                            } else {
                                var4 = var2;
                            }
                        } else {
                            var10 = var4;
                            var4 = var2;
                        }

                        var11.measure(MeasureSpec.makeMeasureSpec(var14, var16), MeasureSpec.makeMeasureSpec(var4, var10));
                        if (var13) {
                            var5 = var2 - var11.getMeasuredHeight();
                            var4 = var1;
                        } else {
                            var5 = var2;
                            var4 = var1;
                            if (var15) {
                                var4 = var1 - var11.getMeasuredWidth();
                                var5 = var2;
                            }
                        }
                    }
                }
            }

            ++var6;
            var2 = var5;
            var1 = var4;
        }
    }

    public void onPageScrolled(int var1, float var2, int var3) {
        int var4 = this.mDecorChildCount;
        byte var5 = 0;
        View var11;
        if (var4 > 0) {
            int var6 = this.getScrollX();
            var4 = this.getPaddingLeft();
            int var7 = this.getPaddingRight();
            int var8 = this.getWidth();
            int var9 = this.getChildCount();

            for(int var10 = 0; var10 < var9; ++var10) {
                var11 = this.getChildAt(var10);
                LayoutParams var12 = (LayoutParams)var11.getLayoutParams();
                if (var12.isDecor) {
                    int var13 = var12.gravity & 7;
                    if (var13 != 1) {
                        if (var13 != 3) {
                            if (var13 != 5) {
                                var13 = var4;
                                var4 = var4;
                            } else {
                                var13 = var8 - var7 - var11.getMeasuredWidth();
                                var7 += var11.getMeasuredWidth();
                            }
                        } else {
                            int var14 = var11.getWidth() + var4;
                            var13 = var4;
                            var4 = var14;
                        }
                    } else {
                        var13 = Math.max((var8 - var11.getMeasuredWidth()) / 2, var4);
                    }

                    var13 = var13 + var6 - var11.getLeft();
                    if (var13 != 0) {
                        var11.offsetLeftAndRight(var13);
                    }
                }
            }
        }

        this.dispatchOnPageScrolled(var1, var2, var3);
        if (this.mPageTransformer != null) {
            var4 = this.getScrollX();
            var3 = this.getChildCount();

            for(var1 = var5; var1 < var3; ++var1) {
                var11 = this.getChildAt(var1);
                if (!((LayoutParams)var11.getLayoutParams()).isDecor) {
                    var2 = (float)(var11.getLeft() - var4) / (float)this.getClientWidth();
                    this.mPageTransformer.transformPage(var11, var2);
                }
            }
        }

        this.mCalledSuper = true;
    }

    @SuppressLint("WrongConstant")
    public boolean onRequestFocusInDescendants(int var1, Rect var2) {
        int var3 = this.getChildCount();
        int var4 = -1;
        byte var5;
        if ((var1 & 2) != 0) {
            var4 = var3;
            var3 = 0;
            var5 = 1;
        } else {
            --var3;
            var5 = -1;
        }

        for(; var3 != var4; var3 += var5) {
            View var6 = this.getChildAt(var3);
            if (var6.getVisibility() == 0) {
                ItemInfo var7 = this.infoForChild(var6);
                if (var7 != null && var7.position == this.mCurItem && var6.requestFocus(var1, var2)) {
                    return true;
                }
            }
        }

        return false;
    }

    public void onRestoreInstanceState(Parcelable var1) {
        if (!(var1 instanceof SavedState)) {
            super.onRestoreInstanceState(var1);
        } else {
            SavedState var3 = (SavedState)var1;
            super.onRestoreInstanceState(var3.getSuperState());
            PagerAdapter var2 = this.mAdapter;
            if (var2 != null) {
                var2.restoreState(var3.adapterState, var3.loader);
                this.setCurrentItemInternal(var3.position, false, true);
            } else {
                this.mRestoredCurItem = var3.position;
                this.mRestoredAdapterState = var3.adapterState;
                this.mRestoredClassLoader = var3.loader;
            }

        }
    }

    public void onRtlPropertiesChanged(int var1) {
        super.onRtlPropertiesChanged(var1);
        if (var1 == 0) {
            this.mLeftIncr = -1;
        } else {
            this.mLeftIncr = 1;
        }

    }

    public Parcelable onSaveInstanceState() {
        SavedState var1 = new SavedState(super.onSaveInstanceState());
        var1.position = this.mCurItem;
        PagerAdapter var2 = this.mAdapter;
        if (var2 != null) {
            var1.adapterState = var2.saveState();
        }

        return var1;
    }

    public final void onSecondaryPointerUp(MotionEvent var1) {
        int var2 = var1.getActionIndex();
        if (var1.getPointerId(var2) == this.mActivePointerId) {
            byte var4;
            if (var2 == 0) {
                var4 = 1;
            } else {
                var4 = 0;
            }

            this.mLastMotionX = var1.getX(var4);
            this.mActivePointerId = var1.getPointerId(var4);
            VelocityTracker var3 = this.mVelocityTracker;
            if (var3 != null) {
                var3.clear();
            }
        }

    }

    public void onSizeChanged(int var1, int var2, int var3, int var4) {
        super.onSizeChanged(var1, var2, var3, var4);
        if (var1 != var3) {
            var2 = this.mPageMargin;
            this.recomputeScrollPosition(var1, var3, var2, var2);
        }

    }

    public boolean onTouchEvent(MotionEvent var1) {
        if (this.mFakeDragging) {
            return true;
        } else {
            int var2 = var1.getAction();
            boolean var3 = false;
            if (var2 == 0 && var1.getEdgeFlags() != 0) {
                return false;
            } else {
                PagerAdapter var4 = this.mAdapter;
                if (var4 != null && var4.getCount() != 0) {
                    if (this.mVelocityTracker == null) {
                        this.mVelocityTracker = VelocityTracker.obtain();
                    }

                    this.mVelocityTracker.addMovement(var1);
                    var2 = var1.getAction() & 255;
                    float var5;
                    if (var2 != 0) {
                        float var7;
                        if (var2 != 1) {
                            if (var2 != 2) {
                                if (var2 != 3) {
                                    if (var2 != 5) {
                                        if (var2 == 6) {
                                            this.onSecondaryPointerUp(var1);
                                            this.mLastMotionX = var1.getX(var1.findPointerIndex(this.mActivePointerId));
                                        }
                                    } else {
                                        var2 = var1.getActionIndex();
                                        this.mLastMotionX = var1.getX(var2);
                                        this.mActivePointerId = var1.getPointerId(var2);
                                    }
                                } else if (this.mIsBeingDragged) {
                                    this.scrollToItem(this.mCurItem, true, 0, false);
                                    var3 = this.resetTouch();
                                }
                            } else {
                                label69: {
                                    if (!this.mIsBeingDragged) {
                                        var2 = var1.findPointerIndex(this.mActivePointerId);
                                        if (var2 == -1) {
                                            var3 = this.resetTouch();
                                            break label69;
                                        }

                                        var5 = var1.getX(var2);
                                        float var6 = Math.abs(var5 - this.mLastMotionX);
                                        var7 = var1.getY(var2);
                                        float var8 = Math.abs(var7 - this.mLastMotionY);
                                        if (var6 > (float)this.mTouchSlop && var6 > var8) {
                                            this.mIsBeingDragged = true;
                                            this.requestParentDisallowInterceptTouchEvent(true);
                                            var6 = this.mInitialMotionX;
                                            if (var5 - var6 > 0.0F) {
                                                var5 = var6 + (float)this.mTouchSlop;
                                            } else {
                                                var5 = var6 - (float)this.mTouchSlop;
                                            }

                                            this.mLastMotionX = var5;
                                            this.mLastMotionY = var7;
                                            this.setScrollState(1);
                                            this.setScrollingCacheEnabled(true);
                                            ViewParent var10 = this.getParent();
                                            if (var10 != null) {
                                                var10.requestDisallowInterceptTouchEvent(true);
                                            }
                                        }
                                    }

                                    if (this.mIsBeingDragged) {
                                        var3 = false | this.performDrag(var1.getX(var1.findPointerIndex(this.mActivePointerId)));
                                    }
                                }
                            }
                        } else if (this.mIsBeingDragged) {
                            VelocityTracker var11 = this.mVelocityTracker;
                            var11.computeCurrentVelocity(1000, (float)this.mMaximumVelocity);
                            var2 = (int)var11.getXVelocity(this.mActivePointerId);
                            this.mPopulatePending = true;
                            int var9 = this.getClientWidth();
                            var5 = (float)this.seslGetScrollStart();
                            var7 = (float)var9;
                            var5 /= var7;
                            ItemInfo var12 = this.infoForCurrentScrollPosition();
                            var7 = (float)this.mPageMargin / var7;
                            var9 = var12.position;
                            if (this.seslIsLayoutRtl()) {
                                var5 = (var12.offset - var5) / (var12.widthFactor + var7);
                            } else {
                                var5 = (var5 - var12.offset) / (var12.widthFactor + var7);
                            }

                            this.setCurrentItemInternal(this.determineTargetPage(var9, var5, var2, (int)(var1.getX(var1.findPointerIndex(this.mActivePointerId)) - this.mInitialMotionX)), true, true, var2);
                            var3 = this.resetTouch();
                        }
                    } else {
                        this.mScroller.abortAnimation();
                        this.mPopulatePending = false;
                        this.populate();
                        var5 = var1.getX();
                        this.mInitialMotionX = var5;
                        this.mLastMotionX = var5;
                        var5 = var1.getY();
                        this.mInitialMotionY = var5;
                        this.mLastMotionY = var5;
                        this.mActivePointerId = var1.getPointerId(0);
                    }

                    if (var3) {
                        ViewCompat.postInvalidateOnAnimation(this);
                    }

                    return true;
                } else {
                    return false;
                }
            }
        }
    }

    public boolean pageLeft() {
        int var1 = this.mCurItem;
        if (var1 > 0) {
            this.setCurrentItem(var1 + this.mLeftIncr, true);
            return true;
        } else {
            return false;
        }
    }

    public boolean pageRight() {
        PagerAdapter var1 = this.mAdapter;
        if (var1 != null && this.mCurItem < var1.getCount() - 1) {
            this.setCurrentItem(this.mCurItem - this.mLeftIncr, true);
            return true;
        } else {
            return false;
        }
    }

    public final boolean pageScrolled(int var1) {
        if (this.mItems.size() == 0) {
            if (this.mFirstLayout) {
                return false;
            } else {
                this.mCalledSuper = false;
                this.onPageScrolled(0, 0.0F, 0);
                if (this.mCalledSuper) {
                    return false;
                } else {
                    throw new IllegalStateException("onPageScrolled did not call superclass implementation");
                }
            }
        } else {
            int var2 = var1;
            if (this.seslIsLayoutRtl()) {
                var2 = 16777216 - var1;
            }

            ItemInfo var3 = this.infoForCurrentScrollPosition();
            int var4 = this.getClientWidth();
            int var5 = this.mPageMargin;
            float var6 = (float)var5;
            float var7 = (float)var4;
            var6 /= var7;
            var1 = var3.position;
            var7 = ((float)var2 / var7 - var3.offset) / (var3.widthFactor + var6);
            var2 = (int)((float)(var4 + var5) * var7);
            this.mCalledSuper = false;
            this.onPageScrolled(var1, var7, var2);
            if (this.mCalledSuper) {
                return true;
            } else {
                throw new IllegalStateException("onPageScrolled did not call superclass implementation");
            }
        }
    }

    public final boolean performDrag(float var1) {
        boolean var2 = this.seslIsLayoutRtl();
        boolean var3 = false;
        boolean var4 = false;
        boolean var5 = false;
        if (var2) {
            this.mIsChangedConfiguration = false;
        }

        SamsungEdgeEffect var6;
        SamsungEdgeEffect var7;
        if (this.seslIsLayoutRtl()) {
            var6 = this.mRightEdge;
            var7 = this.mLeftEdge;
        } else {
            var6 = this.mLeftEdge;
            var7 = this.mRightEdge;
        }

        float var8 = this.mLastMotionX;
        this.mLastMotionX = var1;
        var1 = (float)this.getScrollX() + (var8 - var1);
        var8 = var1;
        if (this.seslIsLayoutRtl()) {
            var8 = 1.6777216E7F - var1;
        }

        int var9 = this.getClientWidth();
        ItemInfo var10 = (ItemInfo)this.mItems.get(0);
        boolean var11;
        if (var10.position == 0) {
            var11 = true;
        } else {
            var11 = false;
        }

        float var12;
        label62: {
            if (var11) {
                if (this.seslIsLayoutRtl()) {
                    var1 = (float)var9;
                    var1 += this.mFirstOffset * var1;
                    break label62;
                }

                var1 = (float)var9;
                var12 = this.mFirstOffset;
            } else {
                var1 = var10.offset;
                var12 = (float)var9;
            }

            var1 *= var12;
        }

        ArrayList var15 = this.mItems;
        var10 = (ItemInfo)var15.get(var15.size() - 1);
        boolean var13;
        if (var10.position == this.mAdapter.getCount() - 1) {
            var13 = true;
        } else {
            var13 = false;
        }

        label55: {
            float var14;
            if (var13) {
                if (this.seslIsLayoutRtl()) {
                    var12 = (float)var9;
                    var12 += this.mLastOffset * var12;
                    break label55;
                }

                var14 = (float)var9;
                var12 = this.mLastOffset;
            } else {
                var14 = var10.offset;
                var12 = (float)var9;
            }

            var12 = var14 * var12;
        }

        if (var8 < var1) {
            if (var11) {
                var6.onPull(Math.abs(var1 - var8) / (float)var9);
                var5 = true;
            }
        } else {
            var5 = var4;
            var1 = var8;
            if (var8 > var12) {
                var5 = var3;
                if (var13) {
                    var7.onPull(Math.abs(var8 - var12) / (float)var9);
                    var5 = true;
                }

                var1 = var12;
            }
        }

        var8 = var1;
        if (this.seslIsLayoutRtl()) {
            var8 = 1.6777216E7F - var1;
        }

        var1 = this.mLastMotionX;
        int var16 = (int)var8;
        this.mLastMotionX = var1 + (var8 - (float)var16);
        this.scrollTo(var16, this.getScrollY());
        this.pageScrolled(var16);
        return var5;
    }

    public void populate() {
        this.populate(this.mCurItem);
    }

    public void populate(int var1) {
        int var2 = this.mCurItem;
        ItemInfo var3;
        byte var4;
        if (var2 != var1) {
            byte var19;
            if (var2 < var1) {
                var19 = 66;
            } else {
                var19 = 17;
            }

            var3 = this.infoForPosition(this.mCurItem);
            this.mCurItem = var1;
            var4 = var19;
        } else {
            var4 = 2;
            var3 = null;
        }

        if (this.mAdapter == null) {
            this.sortChildDrawingOrder();
        } else if (this.mPopulatePending) {
            this.sortChildDrawingOrder();
        } else if (this.getWindowToken() != null) {
            this.mAdapter.startUpdate(this);
            var1 = this.mOffscreenPageLimit;
            int var5 = Math.max(0, this.mCurItem - var1);
            int var6 = this.mAdapter.getCount();
            int var7 = Math.min(var6 - 1, this.mCurItem + var1);
            if (var6 != this.mExpectedAdapterCount) {
                String var24;
                try {
                    var24 = this.getResources().getResourceName(this.getId());
                } catch (Resources.NotFoundException var18) {
                    var24 = Integer.toHexString(this.getId());
                }

                StringBuilder var21 = new StringBuilder();
                var21.append("The application's PagerAdapter changed the adapter's contents without calling PagerAdapter#notifyDataSetChanged! Expected adapter item count: ");
                var21.append(this.mExpectedAdapterCount);
                var21.append(", found: ");
                var21.append(var6);
                var21.append(" Pager id: ");
                var21.append(var24);
                var21.append(" Pager class: ");
                var21.append(SeslViewPager.class);
                var21.append(" Problematic adapter: ");
                var21.append(this.mAdapter.getClass());
                throw new IllegalStateException(var21.toString());
            } else {
                var1 = 0;

                ItemInfo var8;
                int var9;
                while(true) {
                    if (var1 < this.mItems.size()) {
                        var8 = (ItemInfo)this.mItems.get(var1);
                        var9 = var8.position;
                        var2 = this.mCurItem;
                        if (var9 < var2) {
                            ++var1;
                            continue;
                        }

                        if (var9 == var2) {
                            break;
                        }
                    }

                    var8 = null;
                    break;
                }

                ItemInfo var10 = var8;
                if (var8 == null) {
                    var10 = var8;
                    if (var6 > 0) {
                        var10 = this.addNewItem(this.mCurItem, var1);
                    }
                }

                if (var10 != null) {
                    var2 = var1 - 1;
                    if (var2 >= 0) {
                        var8 = (ItemInfo)this.mItems.get(var2);
                    } else {
                        var8 = null;
                    }

                    int var11 = this.getClientWidth();
                    float var12;
                    float var13;
                    if (var11 <= 0) {
                        var12 = 0.0F;
                    } else {
                        var13 = var10.widthFactor;
                        var12 = (float)this.getPaddingLeft() / (float)var11 + (2.0F - var13);
                    }

                    int var14 = this.mCurItem - 1;

                    float var15;
                    int var16;
                    ItemInfo var17;
                    for(var15 = 0.0F; var14 >= 0; var2 = var9) {
                        label207: {
                            label246: {
                                if (var15 >= var12 && var14 < var5) {
                                    if (var8 == null) {
                                        break;
                                    }

                                    var13 = var15;
                                    var16 = var1;
                                    var17 = var8;
                                    var9 = var2;
                                    if (var14 != var8.position) {
                                        break label207;
                                    }

                                    var13 = var15;
                                    var16 = var1;
                                    var17 = var8;
                                    var9 = var2;
                                    if (var8.scrolling) {
                                        break label207;
                                    }

                                    this.mItems.remove(var2);
                                    this.mAdapter.destroyItem(this, var14, var8.object);
                                    --var2;
                                    --var1;
                                    var13 = var15;
                                    var9 = var1;
                                    var16 = var2;
                                    if (var2 >= 0) {
                                        var8 = (ItemInfo)this.mItems.get(var2);
                                        var13 = var15;
                                        break label246;
                                    }
                                } else if (var8 != null && var14 == var8.position) {
                                    var15 += var8.widthFactor;
                                    --var2;
                                    var13 = var15;
                                    var9 = var1;
                                    var16 = var2;
                                    if (var2 >= 0) {
                                        var8 = (ItemInfo)this.mItems.get(var2);
                                        var13 = var15;
                                        break label246;
                                    }
                                } else {
                                    var15 += this.addNewItem(var14, var2 + 1).widthFactor;
                                    ++var1;
                                    var13 = var15;
                                    var9 = var1;
                                    var16 = var2;
                                    if (var2 >= 0) {
                                        var8 = (ItemInfo)this.mItems.get(var2);
                                        var13 = var15;
                                        break label246;
                                    }
                                }

                                var8 = null;
                                var2 = var16;
                                var1 = var9;
                            }

                            var9 = var2;
                            var17 = var8;
                            var16 = var1;
                        }

                        --var14;
                        var15 = var13;
                        var1 = var16;
                        var8 = var17;
                    }

                    var15 = var10.widthFactor;
                    var9 = var1 + 1;
                    if (var15 < 2.0F) {
                        if (var9 < this.mItems.size()) {
                            var8 = (ItemInfo)this.mItems.get(var9);
                        } else {
                            var8 = null;
                        }

                        if (var11 <= 0) {
                            var12 = 0.0F;
                        } else {
                            var12 = (float)this.getPaddingRight() / (float)var11 + 2.0F;
                        }

                        var2 = this.mCurItem;
                        var17 = var8;

                        while(true) {
                            var16 = var2 + 1;
                            if (var16 >= var6) {
                                break;
                            }

                            label247: {
                                if (var15 >= var12 && var16 > var7) {
                                    if (var17 == null) {
                                        break;
                                    }

                                    var13 = var15;
                                    var2 = var9;
                                    var8 = var17;
                                    if (var16 != var17.position) {
                                        break label247;
                                    }

                                    var13 = var15;
                                    var2 = var9;
                                    var8 = var17;
                                    if (var17.scrolling) {
                                        break label247;
                                    }

                                    this.mItems.remove(var9);
                                    this.mAdapter.destroyItem(this, var16, var17.object);
                                    var13 = var15;
                                    var2 = var9;
                                    if (var9 < this.mItems.size()) {
                                        var8 = (ItemInfo)this.mItems.get(var9);
                                        var13 = var15;
                                        var2 = var9;
                                        break label247;
                                    }
                                } else if (var17 != null && var16 == var17.position) {
                                    var15 += var17.widthFactor;
                                    ++var9;
                                    var13 = var15;
                                    var2 = var9;
                                    if (var9 < this.mItems.size()) {
                                        var8 = (ItemInfo)this.mItems.get(var9);
                                        var13 = var15;
                                        var2 = var9;
                                        break label247;
                                    }
                                } else {
                                    var8 = this.addNewItem(var16, var9);
                                    ++var9;
                                    var15 += var8.widthFactor;
                                    var13 = var15;
                                    var2 = var9;
                                    if (var9 < this.mItems.size()) {
                                        var8 = (ItemInfo)this.mItems.get(var9);
                                        var2 = var9;
                                        var13 = var15;
                                        break label247;
                                    }
                                }

                                var8 = null;
                            }

                            var15 = var13;
                            var9 = var2;
                            var17 = var8;
                            var2 = var16;
                        }
                    }

                    this.calculatePageOffsets(var10, var1, var3);
                    this.mAdapter.setPrimaryItem(this, this.mCurItem, var10.object);
                }

                this.mAdapter.finishUpdate(this);
                var2 = this.getChildCount();

                for(var1 = 0; var1 < var2; ++var1) {
                    View var20 = this.getChildAt(var1);
                    LayoutParams var22 = (LayoutParams)var20.getLayoutParams();
                    var22.childIndex = var1;
                    if (!var22.isDecor && var22.widthFactor == 0.0F) {
                        var3 = this.infoForChild(var20);
                        if (var3 != null) {
                            var22.widthFactor = var3.widthFactor;
                            var22.position = var3.position;
                        }
                    }
                }

                this.sortChildDrawingOrder();
                if (this.hasFocus()) {
                    View var23 = this.findFocus();
                    if (var23 != null) {
                        var8 = this.infoForAnyChild(var23);
                    } else {
                        var8 = null;
                    }

                    if (var8 == null || var8.position != this.mCurItem) {
                        for(var1 = 0; var1 < this.getChildCount(); ++var1) {
                            var23 = this.getChildAt(var1);
                            var3 = this.infoForChild(var23);
                            if (var3 != null && var3.position == this.mCurItem && var23.requestFocus(var4)) {
                                break;
                            }
                        }
                    }
                }

            }
        }
    }

    public final void recomputeScrollPosition(int var1, int var2, int var3, int var4) {
        if (var2 > 0 && !this.mItems.isEmpty()) {
            if (!this.mScroller.isFinished()) {
                this.mScroller.setFinalX(this.getCurrentItem() * this.getClientWidth());
            } else {
                int var5 = this.getPaddingLeft();
                int var6 = this.getPaddingRight();
                int var7 = this.getPaddingLeft();
                int var8 = this.getPaddingRight();
                this.scrollTo((int)((float)this.getScrollX() / (float)(var2 - var7 - var8 + var4) * (float)(var1 - var5 - var6 + var3)), this.getScrollY());
            }
        } else {
            ItemInfo var9 = this.infoForPosition(this.mCurItem);
            float var10;
            if (var9 != null) {
                var10 = Math.min(var9.offset, this.mLastOffset);
            } else {
                var10 = 0.0F;
            }

            var1 = (int)(var10 * (float)(var1 - this.getPaddingLeft() - this.getPaddingRight()));
            if (var1 != this.getScrollX()) {
                this.completeScroll(false);
                this.scrollTo(var1, this.getScrollY());
            }
        }

    }

    public final void removeNonDecorViews() {
        int var2;
        for(int var1 = 0; var1 < this.getChildCount(); var1 = var2 + 1) {
            var2 = var1;
            if (!((LayoutParams)this.getChildAt(var1).getLayoutParams()).isDecor) {
                this.removeViewAt(var1);
                var2 = var1 - 1;
            }
        }

    }

    public void removeOnAdapterChangeListener(OnAdapterChangeListener var1) {
        List var2 = this.mAdapterChangeListeners;
        if (var2 != null) {
            var2.remove(var1);
        }

    }

    public void removeOnPageChangeListener(OnPageChangeListener var1) {
        List var2 = this.mOnPageChangeListeners;
        if (var2 != null) {
            var2.remove(var1);
        }

    }

    public void removeView(View var1) {
        if (this.mInLayout) {
            this.removeViewInLayout(var1);
        } else {
            super.removeView(var1);
        }

    }

    public final void requestParentDisallowInterceptTouchEvent(boolean var1) {
        ViewParent var2 = this.getParent();
        if (var2 != null) {
            var2.requestDisallowInterceptTouchEvent(var1);
        }

    }

    public final boolean resetTouch() {
        this.mActivePointerId = -1;
        this.endDrag();
        this.mLeftEdge.onRelease();
        this.mRightEdge.onRelease();
        boolean var1;
        if (!this.mLeftEdge.isFinished() && !this.mRightEdge.isFinished()) {
            var1 = false;
        } else {
            var1 = true;
        }

        return var1;
    }

    public final void scrollToItem(int var1, boolean var2, int var3, boolean var4) {
        ItemInfo var5 = this.infoForPosition(var1);
        int var7;
        if (var5 != null) {
            float var6 = (float)this.getClientWidth();
            var7 = (int)(constrain(var5.offset, this.mFirstOffset, this.mLastOffset) * var6);
            if (this.seslIsLayoutRtl()) {
                var7 = 16777216 - (int)(var6 * var5.widthFactor + 0.5F) - var7;
            }
        } else {
            var7 = 0;
        }

        if (var2) {
            this.smoothScrollTo(var7, 0, var3);
            if (var4) {
                this.dispatchOnPageSelected(var1);
            }
        } else {
            if (var4) {
                this.dispatchOnPageSelected(var1);
            }

            this.completeScroll(false);
            this.scrollTo(var7, 0);
            this.pageScrolled(var7);
        }

    }

    public final int seslGetScrollStart() {
        return this.seslIsLayoutRtl() ? 16777216 - this.getScrollX() : this.getScrollX();
    }

    public final boolean seslIsLayoutRtl() {
        int var1 = this.getLayoutDirection();
        boolean var2 = true;
        if (var1 != 1) {
            var2 = false;
        }

        return var2;
    }

    public void seslSetConfigurationChanged(boolean var1) {
        this.mIsChangedConfiguration = var1;
    }

    public void seslSetSupportedMouseWheelEvent(boolean var1) {
        this.mIsMouseWheelEventSupport = var1;
    }

    public void setAdapter(PagerAdapter var1) {
        PagerAdapter var2 = this.mAdapter;
        byte var3 = 0;
        int var4;
        if (var2 != null) {
            var2.setViewPagerObserver((DataSetObserver)null);
            this.mAdapter.startUpdate(this);

            for(var4 = 0; var4 < this.mItems.size(); ++var4) {
                ItemInfo var8 = (ItemInfo)this.mItems.get(var4);
                this.mAdapter.destroyItem(this, var8.position, var8.object);
            }

            this.mAdapter.finishUpdate(this);
            this.mItems.clear();
            this.removeNonDecorViews();
            this.mCurItem = 0;
            this.scrollTo(0, 0);
        }

        var2 = this.mAdapter;
        this.mAdapter = var1;
        this.mExpectedAdapterCount = 0;
        if (this.mAdapter != null) {
            if (this.mObserver == null) {
                this.mObserver = new PagerObserver();
            }

            this.mAdapter.setViewPagerObserver(this.mObserver);
            this.mPopulatePending = false;
            boolean var5 = this.mFirstLayout;
            this.mFirstLayout = true;
            this.mExpectedAdapterCount = this.mAdapter.getCount();
            if (this.mRestoredCurItem >= 0) {
                this.mAdapter.restoreState(this.mRestoredAdapterState, this.mRestoredClassLoader);
                this.setCurrentItemInternal(this.mRestoredCurItem, false, true);
                this.mRestoredCurItem = -1;
                this.mRestoredAdapterState = null;
                this.mRestoredClassLoader = null;
            } else if (!var5) {
                this.populate();
            } else {
                this.requestLayout();
            }
        }

        List var6 = this.mAdapterChangeListeners;
        if (var6 != null && !var6.isEmpty()) {
            int var7 = this.mAdapterChangeListeners.size();

            for(var4 = var3; var4 < var7; ++var4) {
                ((OnAdapterChangeListener)this.mAdapterChangeListeners.get(var4)).onAdapterChanged(this, var2, var1);
            }
        }

    }

    public void setCurrentItem(int var1) {
        this.mPopulatePending = false;
        this.setCurrentItemInternal(var1, this.mFirstLayout ^ true, false);
    }

    public void setCurrentItem(int var1, boolean var2) {
        this.mPopulatePending = false;
        this.setCurrentItemInternal(var1, var2, false);
    }

    public void setCurrentItemInternal(int var1, boolean var2, boolean var3) {
        this.setCurrentItemInternal(var1, var2, var3, 0);
    }

    public void setCurrentItemInternal(int var1, boolean var2, boolean var3, int var4) {
        PagerAdapter var5 = this.mAdapter;
        if (var5 != null && var5.getCount() > 0) {
            if (!var3 && this.mCurItem == var1 && this.mItems.size() != 0) {
                this.setScrollingCacheEnabled(false);
            } else {
                var3 = true;
                int var6;
                if (var1 < 0) {
                    var6 = 0;
                } else {
                    var6 = var1;
                    if (var1 >= this.mAdapter.getCount()) {
                        var6 = this.mAdapter.getCount() - 1;
                    }
                }

                int var7 = this.mOffscreenPageLimit;
                var1 = this.mCurItem;
                if (var6 > var1 + var7 || var6 < var1 - var7) {
                    for(var1 = 0; var1 < this.mItems.size(); ++var1) {
                        ((ItemInfo)this.mItems.get(var1)).scrolling = true;
                    }
                }

                if (this.mCurItem == var6) {
                    var3 = false;
                }

                if (this.mFirstLayout) {
                    this.mCurItem = var6;
                    if (var3) {
                        this.dispatchOnPageSelected(var6);
                    }

                    this.requestLayout();
                } else {
                    this.populate(var6);
                    this.scrollToItem(var6, var2, var4, var3);
                }

            }
        } else {
            this.setScrollingCacheEnabled(false);
        }
    }

    public void setDragInGutterEnabled(boolean var1) {
        this.mDragInGutterEnabled = var1;
    }

    public void setOffscreenPageLimit(int var1) {
        int var2 = var1;
        if (var1 < 1) {
            StringBuilder var3 = new StringBuilder();
            var3.append("Requested offscreen page limit ");
            var3.append(var1);
            var3.append(" too small; defaulting to ");
            var3.append(1);
            Log.w("ViewPager", var3.toString());
            var2 = 1;
        }

        if (var2 != this.mOffscreenPageLimit) {
            this.mOffscreenPageLimit = var2;
            this.populate();
        }

    }

    @Deprecated
    public void setOnPageChangeListener(OnPageChangeListener var1) {
        this.mOnPageChangeListener = var1;
    }

    public void setPageMargin(int var1) {
        int var2 = this.mPageMargin;
        this.mPageMargin = var1;
        int var3 = this.getWidth();
        this.recomputeScrollPosition(var3, var3, var1, var2);
        this.requestLayout();
    }

    public void setPageMarginDrawable(int var1) {
        this.setPageMarginDrawable(ContextCompat.getDrawable(this.getContext(), var1));
    }

    public void setPageMarginDrawable(Drawable var1) {
        this.mMarginDrawable = var1;
        if (var1 != null) {
            this.refreshDrawableState();
        }

        boolean var2;
        if (var1 == null) {
            var2 = true;
        } else {
            var2 = false;
        }

        this.setWillNotDraw(var2);
        this.invalidate();
    }

    public void setScrollState(int var1) {
        if (this.mScrollState != var1) {
            this.mScrollState = var1;
            if (this.mPageTransformer != null) {
                boolean var2;
                if (var1 != 0) {
                    var2 = true;
                } else {
                    var2 = false;
                }

                this.enableLayers(var2);
            }

            this.dispatchOnScrollStateChanged(var1);
        }
    }

    public void smoothScrollTo(int var1, int var2, int var3) {
        if (this.getChildCount() == 0) {
            this.setScrollingCacheEnabled(false);
        } else {
            Scroller var4 = this.mScroller;
            boolean var5;
            if (var4 != null && !var4.isFinished()) {
                var5 = true;
            } else {
                var5 = false;
            }

            int var12;
            if (var5) {
                if (this.mIsScrollStarted) {
                    var12 = this.mScroller.getCurrX();
                } else {
                    var12 = this.mScroller.getStartX();
                }

                this.mScroller.abortAnimation();
                this.setScrollingCacheEnabled(false);
            } else {
                var12 = this.getScrollX();
            }

            int var6 = this.getScrollY();
            int var7 = var1 - var12;
            var2 -= var6;
            if (var7 == 0 && var2 == 0) {
                this.completeScroll(false);
                this.populate();
                this.setScrollState(0);
            } else {
                this.setScrollingCacheEnabled(true);
                this.setScrollState(2);
                int var8 = this.getClientWidth();
                var1 = var8 / 2;
                float var9 = (float)Math.abs(var7);
                float var10 = (float)var8;
                float var11 = Math.min(1.0F, var9 * 1.0F / var10);
                var9 = (float)var1;
                var11 = this.distanceInfluenceForSnapDuration(var11);
                var1 = Math.abs(var3);
                if (var1 > 0) {
                    var1 = Math.round(Math.abs((var9 + var11 * var9) / (float)var1) * 1000.0F) * 4;
                } else {
                    var9 = this.mAdapter.getPageWidth(this.mCurItem);
                    var1 = (int)(((float)Math.abs(var7) / (var10 * var9 + (float)this.mPageMargin) + 1.0F) * 100.0F);
                }

                var1 = Math.min(var1, 600);
                this.mIsScrollStarted = false;
                this.mScroller.startScroll(var12, var6, var7, var2, var1);
                ViewCompat.postInvalidateOnAnimation(this);
            }
        }
    }

    public final void sortChildDrawingOrder() {
        if (this.mDrawingOrder != 0) {
            ArrayList var1 = this.mDrawingOrderedChildren;
            if (var1 == null) {
                this.mDrawingOrderedChildren = new ArrayList();
            } else {
                var1.clear();
            }

            int var2 = this.getChildCount();

            for(int var3 = 0; var3 < var2; ++var3) {
                View var4 = this.getChildAt(var3);
                this.mDrawingOrderedChildren.add(var4);
            }

            Collections.sort(this.mDrawingOrderedChildren, sPositionComparator);
        }

    }

    public boolean verifyDrawable(Drawable var1) {
        boolean var2;
        if (!super.verifyDrawable(var1) && var1 != this.mMarginDrawable) {
            var2 = false;
        } else {
            var2 = true;
        }

        return var2;
    }

    @Inherited
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE})
    public @interface DecorView {
    }

    public static class ItemInfo {
        public Object object;
        public float offset;
        public int position;
        public boolean scrolling;
        public float widthFactor;

        public ItemInfo() {
        }
    }

    public static class LayoutParams extends ViewGroup.LayoutParams {
        public int childIndex;
        public int gravity;
        public boolean isDecor;
        public boolean needsMeasure;
        public int position;
        public float widthFactor = 0.0F;

        public LayoutParams() {
            super(-1, -1);
        }

        public LayoutParams(Context var1, AttributeSet var2) {
            super(var1, var2);
            TypedArray var3 = var1.obtainStyledAttributes(var2, SeslViewPager.LAYOUT_ATTRS);
            this.gravity = var3.getInteger(0, 48);
            var3.recycle();
        }
    }

    public class MyAccessibilityDelegate extends AccessibilityDelegateCompat {
        public MyAccessibilityDelegate() {
        }

        public final boolean canScroll() {
            PagerAdapter var1 = SeslViewPager.this.mAdapter;
            boolean var2 = true;
            if (var1 == null || var1.getCount() <= 1) {
                var2 = false;
            }

            return var2;
        }

        @SuppressLint("WrongConstant")
        public void onInitializeAccessibilityEvent(View var1, AccessibilityEvent var2) {
            super.onInitializeAccessibilityEvent(var1, var2);
            var2.setClassName(SeslViewPager.class.getName());
            var2.setScrollable(this.canScroll());
            if (var2.getEventType() == 4096) {
                PagerAdapter var3 = SeslViewPager.this.mAdapter;
                if (var3 != null) {
                    var2.setItemCount(var3.getCount());
                    var2.setFromIndex(SeslViewPager.this.mCurItem);
                    var2.setToIndex(SeslViewPager.this.mCurItem);
                }
            }

        }

        public void onInitializeAccessibilityNodeInfo(View var1, AccessibilityNodeInfoCompat var2) {
            super.onInitializeAccessibilityNodeInfo(var1, var2);
            var2.setClassName(SeslViewPager.class.getName());
            var2.setScrollable(this.canScroll());
            if (SeslViewPager.this.canScrollHorizontally(1)) {
                var2.addAction(4096);
            }

            if (SeslViewPager.this.canScrollHorizontally(-1)) {
                var2.addAction(8192);
            }

        }

        public boolean performAccessibilityAction(View var1, int var2, Bundle var3) {
            if (super.performAccessibilityAction(var1, var2, var3)) {
                return true;
            } else {
                SeslViewPager var4;
                if (var2 != 4096) {
                    if (var2 != 8192) {
                        return false;
                    } else if (SeslViewPager.this.canScrollHorizontally(-1)) {
                        var4 = SeslViewPager.this;
                        var4.setCurrentItem(var4.mCurItem - 1);
                        return true;
                    } else {
                        return false;
                    }
                } else if (SeslViewPager.this.canScrollHorizontally(1)) {
                    var4 = SeslViewPager.this;
                    var4.setCurrentItem(var4.mCurItem + 1);
                    return true;
                } else {
                    return false;
                }
            }
        }
    }

    public interface OnAdapterChangeListener {
        void onAdapterChanged(SeslViewPager var1, PagerAdapter var2, PagerAdapter var3);
    }

    public interface OnPageChangeListener {
        void onPageScrollStateChanged(int var1);

        void onPageScrolled(int var1, float var2, int var3);

        void onPageSelected(int var1);
    }

    public interface PageTransformer {
        void transformPage(View var1, float var2);
    }

    private class PagerObserver extends DataSetObserver {
        public PagerObserver() {
        }

        public void onChanged() {
            SeslViewPager.this.dataSetChanged();
        }

        public void onInvalidated() {
            SeslViewPager.this.dataSetChanged();
        }
    }

    public static class SavedState extends AbsSavedState {
        public static final Creator<SavedState> CREATOR = new ClassLoaderCreator<SavedState>() {
            public SavedState createFromParcel(Parcel var1) {
                return new SavedState(var1, (ClassLoader)null);
            }

            public SavedState createFromParcel(Parcel var1, ClassLoader var2) {
                return new SavedState(var1, var2);
            }

            public SavedState[] newArray(int var1) {
                return new SavedState[var1];
            }
        };
        public Parcelable adapterState;
        public ClassLoader loader;
        public int position;

        public SavedState(Parcel var1, ClassLoader var2) {
            super(var1, var2);
            ClassLoader var3 = var2;
            if (var2 == null) {
                var3 = SavedState.class.getClassLoader();
            }

            this.position = var1.readInt();
            this.adapterState = var1.readParcelable(var3);
            this.loader = var3;
        }

        public SavedState(Parcelable var1) {
            super(var1);
        }

        public String toString() {
            StringBuilder var1 = new StringBuilder();
            var1.append("FragmentPager.SavedState{");
            var1.append(Integer.toHexString(System.identityHashCode(this)));
            var1.append(" position=");
            var1.append(this.position);
            var1.append("}");
            return var1.toString();
        }

        public void writeToParcel(Parcel var1, int var2) {
            super.writeToParcel(var1, var2);
            var1.writeInt(this.position);
            var1.writeParcelable(this.adapterState, var2);
        }
    }

    public static class ViewPositionComparator implements Comparator<View> {
        public ViewPositionComparator() {
        }

        public int compare(View var1, View var2) {
            LayoutParams var5 = (LayoutParams)var1.getLayoutParams();
            LayoutParams var6 = (LayoutParams)var2.getLayoutParams();
            boolean var3 = var5.isDecor;
            if (var3 != var6.isDecor) {
                byte var4;
                if (var3) {
                    var4 = 1;
                } else {
                    var4 = -1;
                }

                return var4;
            } else {
                return var5.position - var6.position;
            }
        }
    }

    /*kang from MathUtils.smali*/
    public static int constrain(int amount, int low, int high) {
        if (amount < low) {
            return low;
        }
        return amount > high ? high : amount;
    }

    public static float constrain(float amount, float low, float high) {
        if (amount < low) {
            return low;
        }
        return amount > high ? high : amount;
    }
}
