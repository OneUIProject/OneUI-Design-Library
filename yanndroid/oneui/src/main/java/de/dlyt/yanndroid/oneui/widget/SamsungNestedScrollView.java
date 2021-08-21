package de.dlyt.yanndroid.oneui.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.FocusFinder;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityEvent;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.OverScroller;
import android.widget.ScrollView;

import androidx.core.view.AccessibilityDelegateCompat;
import androidx.core.view.NestedScrollingChild3;
import androidx.core.view.NestedScrollingChildHelper;
import androidx.core.view.NestedScrollingParent3;
import androidx.core.view.NestedScrollingParentHelper;
import androidx.core.view.ScrollingView;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.core.view.accessibility.AccessibilityRecordCompat;
import androidx.core.widget.EdgeEffectCompat;

import java.util.ArrayList;

public class SamsungNestedScrollView extends FrameLayout implements NestedScrollingParent3, NestedScrollingChild3, ScrollingView {
    public static final AccessibilityDelegate ACCESSIBILITY_DELEGATE = new AccessibilityDelegate();
    public static final int[] SCROLLVIEW_STYLEABLE = new int[]{16843130};
    public int mActivePointerId;
    public final NestedScrollingChildHelper mChildHelper;
    public View mChildToScrollTo;
    public SamsungEdgeEffect mEdgeGlowBottom;
    public SamsungEdgeEffect mEdgeGlowTop;
    public boolean mFillViewport;
    public boolean mIsBeingDragged;
    public boolean mIsLaidOut;
    public boolean mIsLayoutDirty;
    public int mLastMotionY;
    public long mLastScroll;
    public int mLastScrollerY;
    public int mMaximumVelocity;
    public int mMinimumVelocity;
    public int mNestedYOffset;
    public OnScrollChangeListener mOnScrollChangeListener;
    public final NestedScrollingParentHelper mParentHelper;
    public SavedState mSavedState;
    public final int[] mScrollConsumed;
    public final int[] mScrollOffset;
    public OverScroller mScroller;
    public boolean mSmoothScrollingEnabled;
    public final Rect mTempRect;
    public int mTouchSlop;
    public VelocityTracker mVelocityTracker;
    public float mVerticalScrollFactor;

    public SamsungNestedScrollView(Context var1) {
        this(var1, (AttributeSet)null);
    }

    public SamsungNestedScrollView(Context var1, AttributeSet var2) {
        this(var1, var2, 0);
    }

    public SamsungNestedScrollView(Context var1, AttributeSet var2, int var3) {
        super(var1, var2, var3);
        this.mTempRect = new Rect();
        this.mIsLayoutDirty = true;
        this.mIsLaidOut = false;
        this.mChildToScrollTo = null;
        this.mIsBeingDragged = false;
        this.mSmoothScrollingEnabled = true;
        this.mActivePointerId = -1;
        this.mScrollOffset = new int[2];
        this.mScrollConsumed = new int[2];
        this.initScrollView();
        TypedArray var4 = var1.obtainStyledAttributes(var2, SCROLLVIEW_STYLEABLE, var3, 0);
        this.setFillViewport(var4.getBoolean(0, false));
        var4.recycle();
        this.mParentHelper = new NestedScrollingParentHelper(this);
        this.mChildHelper = new NestedScrollingChildHelper(this);
        this.setNestedScrollingEnabled(true);
        ViewCompat.setAccessibilityDelegate(this, ACCESSIBILITY_DELEGATE);
    }

    public static int clamp(int var0, int var1, int var2) {
        if (var1 < var2 && var0 >= 0) {
            return var1 + var0 > var2 ? var2 - var1 : var0;
        } else {
            return 0;
        }
    }

    private float getVerticalScrollFactorCompat() {
        if (this.mVerticalScrollFactor == 0.0F) {
            TypedValue var1 = new TypedValue();
            Context var2 = this.getContext();
            if (!var2.getTheme().resolveAttribute(16842829, var1, true)) {
                throw new IllegalStateException("Expected theme to define listPreferredItemHeight.");
            }

            this.mVerticalScrollFactor = var1.getDimension(var2.getResources().getDisplayMetrics());
        }

        return this.mVerticalScrollFactor;
    }

    public static boolean isViewDescendantOf(View var0, View var1) {
        boolean var2 = true;
        if (var0 == var1) {
            return true;
        } else {
            ViewParent var3 = var0.getParent();
            if (!(var3 instanceof ViewGroup) || !isViewDescendantOf((View)var3, var1)) {
                var2 = false;
            }

            return var2;
        }
    }

    @SuppressLint("WrongConstant")
    public final void abortAnimatedScroll() {
        this.mScroller.abortAnimation();
        this.stopNestedScroll(1);
    }

    public void addView(View var1) {
        if (this.getChildCount() <= 0) {
            super.addView(var1);
        } else {
            throw new IllegalStateException("ScrollView can host only one direct child");
        }
    }

    public void addView(View var1, int var2) {
        if (this.getChildCount() <= 0) {
            super.addView(var1, var2);
        } else {
            throw new IllegalStateException("ScrollView can host only one direct child");
        }
    }

    public void addView(View var1, int var2, LayoutParams var3) {
        if (this.getChildCount() <= 0) {
            super.addView(var1, var2, var3);
        } else {
            throw new IllegalStateException("ScrollView can host only one direct child");
        }
    }

    public void addView(View var1, LayoutParams var2) {
        if (this.getChildCount() <= 0) {
            super.addView(var1, var2);
        } else {
            throw new IllegalStateException("ScrollView can host only one direct child");
        }
    }

    @SuppressLint("WrongConstant")
    public boolean arrowScroll(int var1) {
        View var2 = this.findFocus();
        View var3 = var2;
        if (var2 == this) {
            var3 = null;
        }

        var2 = FocusFinder.getInstance().findNextFocus(this, var3, var1);
        int var4 = this.getMaxScrollAmount();
        int var5;
        if (var2 != null && this.isWithinDeltaOfScreen(var2, var4, this.getHeight())) {
            var2.getDrawingRect(this.mTempRect);
            this.offsetDescendantRectToMyCoords(var2, this.mTempRect);
            var5 = this.computeScrollDeltaToGetChildRectOnScreen(this.mTempRect);
            this.mLastScrollerY = this.getScrollY();
            this.doScrollY(var5);
            var2.requestFocus(var1);
        } else {
            if (var1 == 33 && this.getScrollY() < var4) {
                var5 = this.getScrollY();
            } else {
                var5 = var4;
                if (var1 == 130) {
                    var5 = var4;
                    if (this.getChildCount() > 0) {
                        View var6 = this.getChildAt(0);
                        LayoutParams var7 = (LayoutParams)var6.getLayoutParams();
                        var5 = Math.min(var6.getBottom() + var7.bottomMargin - (this.getScrollY() + this.getHeight() - this.getPaddingBottom()), var4);
                    }
                }
            }

            if (var5 == 0) {
                return false;
            }

            if (var1 != 130) {
                var5 = -var5;
            }

            this.doScrollY(var5);
        }

        if (var3 != null && var3.isFocused() && this.isOffScreen(var3)) {
            var1 = this.getDescendantFocusability();
            this.setDescendantFocusability(131072);
            this.requestFocus();
            this.setDescendantFocusability(var1);
        }

        return true;
    }

    public final boolean canScroll() {
        int var1 = this.getChildCount();
        boolean var2 = false;
        boolean var3 = var2;
        if (var1 > 0) {
            View var4 = this.getChildAt(0);
            LayoutParams var5 = (LayoutParams)var4.getLayoutParams();
            var3 = var2;
            if (var4.getHeight() + var5.topMargin + var5.bottomMargin > this.getHeight() - this.getPaddingTop() - this.getPaddingBottom()) {
                var3 = true;
            }
        }

        return var3;
    }

    public int computeHorizontalScrollExtent() {
        return super.computeHorizontalScrollExtent();
    }

    public int computeHorizontalScrollOffset() {
        return super.computeHorizontalScrollOffset();
    }

    public int computeHorizontalScrollRange() {
        return super.computeHorizontalScrollRange();
    }

    @SuppressLint("WrongConstant")
    public void computeScroll() {
        if (!this.mScroller.isFinished()) {
            this.mScroller.computeScrollOffset();
            int var1 = this.mScroller.getCurrY();
            int var2 = var1 - this.mLastScrollerY;
            this.mLastScrollerY = var1;
            int[] var3 = this.mScrollConsumed;
            boolean var4 = false;
            var3[1] = 0;
            this.dispatchNestedPreScroll(0, var2, var3, (int[])null, 1);
            var1 = var2 - this.mScrollConsumed[1];
            int var5 = this.getScrollRange();
            var2 = var1;
            if (var1 != 0) {
                var2 = this.getScrollY();
                this.overScrollByCompat(0, var1, this.getScrollX(), var2, 0, var5, 0, 0, false);
                var2 = this.getScrollY() - var2;
                var1 -= var2;
                var3 = this.mScrollConsumed;
                var3[1] = 0;
                this.dispatchNestedScroll(0, var2, 0, var1, this.mScrollOffset, 1, var3);
                var2 = var1 - this.mScrollConsumed[1];
            }

            if (var2 != 0) {
                boolean var7;
                label36: {
                    int var6 = this.getOverScrollMode();
                    if (var6 != 0) {
                        var7 = var4;
                        if (var6 != 1) {
                            break label36;
                        }

                        var7 = var4;
                        if (var5 <= 0) {
                            break label36;
                        }
                    }

                    var7 = true;
                }

                if (var7) {
                    this.ensureGlows();
                    if (var2 < 0) {
                        if (this.mEdgeGlowTop.isFinished()) {
                            this.mEdgeGlowTop.onAbsorb((int)this.mScroller.getCurrVelocity());
                        }
                    } else if (this.mEdgeGlowBottom.isFinished()) {
                        this.mEdgeGlowBottom.onAbsorb((int)this.mScroller.getCurrVelocity());
                    }
                }

                this.abortAnimatedScroll();
            }

            if (!this.mScroller.isFinished()) {
                ViewCompat.postInvalidateOnAnimation(this);
            }

        }
    }

    public int computeScrollDeltaToGetChildRectOnScreen(Rect var1) {
        int var2 = this.getChildCount();
        byte var3 = 0;
        if (var2 == 0) {
            return 0;
        } else {
            int var4 = this.getHeight();
            var2 = this.getScrollY();
            int var5 = var2 + var4;
            int var6 = this.getVerticalFadingEdgeLength();
            int var7 = var2;
            if (var1.top > 0) {
                var7 = var2 + var6;
            }

            View var8 = this.getChildAt(0);
            LayoutParams var9 = (LayoutParams)var8.getLayoutParams();
            if (var1.bottom < var8.getHeight() + var9.topMargin + var9.bottomMargin) {
                var6 = var5 - var6;
            } else {
                var6 = var5;
            }

            if (var1.bottom > var6 && var1.top > var7) {
                if (var1.height() > var4) {
                    var2 = var1.top - var7;
                } else {
                    var2 = var1.bottom - var6;
                }

                var2 = Math.min(var2 + 0, var8.getBottom() + var9.bottomMargin - var5);
            } else {
                var2 = var3;
                if (var1.top < var7) {
                    var2 = var3;
                    if (var1.bottom < var6) {
                        if (var1.height() > var4) {
                            var2 = 0 - (var6 - var1.bottom);
                        } else {
                            var2 = 0 - (var7 - var1.top);
                        }

                        var2 = Math.max(var2, -this.getScrollY());
                    }
                }
            }

            return var2;
        }
    }

    public int computeVerticalScrollExtent() {
        return super.computeVerticalScrollExtent();
    }

    public int computeVerticalScrollOffset() {
        return Math.max(0, super.computeVerticalScrollOffset());
    }

    public int computeVerticalScrollRange() {
        int var1 = this.getChildCount();
        int var2 = this.getHeight() - this.getPaddingBottom() - this.getPaddingTop();
        if (var1 == 0) {
            return var2;
        } else {
            View var3 = this.getChildAt(0);
            LayoutParams var4 = (LayoutParams)var3.getLayoutParams();
            var1 = var3.getBottom() + var4.bottomMargin;
            int var5 = this.getScrollY();
            int var6 = Math.max(0, var1 - var2);
            if (var5 < 0) {
                var2 = var1 - var5;
            } else {
                var2 = var1;
                if (var5 > var6) {
                    var2 = var1 + (var5 - var6);
                }
            }

            return var2;
        }
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

    public boolean dispatchNestedFling(float var1, float var2, boolean var3) {
        return this.mChildHelper.dispatchNestedFling(var1, var2, var3);
    }

    public boolean dispatchNestedPreFling(float var1, float var2) {
        return this.mChildHelper.dispatchNestedPreFling(var1, var2);
    }

    @SuppressLint("WrongConstant")
    public boolean dispatchNestedPreScroll(int var1, int var2, int[] var3, int[] var4) {
        return this.dispatchNestedPreScroll(var1, var2, var3, var4, 0);
    }

    public boolean dispatchNestedPreScroll(int var1, int var2, int[] var3, int[] var4, int var5) {
        return this.mChildHelper.dispatchNestedPreScroll(var1, var2, var3, var4, var5);
    }

    public void dispatchNestedScroll(int var1, int var2, int var3, int var4, int[] var5, int var6, int[] var7) {
        this.mChildHelper.dispatchNestedScroll(var1, var2, var3, var4, var5, var6, var7);
    }

    public boolean dispatchNestedScroll(int var1, int var2, int var3, int var4, int[] var5) {
        return this.mChildHelper.dispatchNestedScroll(var1, var2, var3, var4, var5);
    }

    public boolean dispatchNestedScroll(int var1, int var2, int var3, int var4, int[] var5, int var6) {
        return this.mChildHelper.dispatchNestedScroll(var1, var2, var3, var4, var5, var6);
    }

    public final void doScrollY(int var1) {
        if (var1 != 0) {
            if (this.mSmoothScrollingEnabled) {
                this.smoothScrollBy(0, var1);
            } else {
                this.scrollBy(0, var1);
            }
        }

    }

    public void draw(Canvas var1) {
        super.draw(var1);
        if (this.mEdgeGlowTop != null) {
            int var2 = this.getScrollY();
            boolean var3 = this.mEdgeGlowTop.isFinished();
            byte var4 = 0;
            int var5;
            int var6;
            int var7;
            int var8;
            int var9;
            int var10;
            int var11;
            if (!var3) {
                var5 = var1.save();
                var6 = this.getWidth();
                var7 = this.getHeight();
                var8 = Math.min(0, var2);
                if (Build.VERSION.SDK_INT >= 21 && !this.getClipToPadding()) {
                    var9 = 0;
                } else {
                    var6 -= this.getPaddingLeft() + this.getPaddingRight();
                    var9 = this.getPaddingLeft() + 0;
                }

                var10 = var7;
                var11 = var8;
                if (Build.VERSION.SDK_INT >= 21) {
                    var10 = var7;
                    var11 = var8;
                    if (this.getClipToPadding()) {
                        var10 = var7 - (this.getPaddingTop() + this.getPaddingBottom());
                        var11 = var8 + this.getPaddingTop();
                    }
                }

                var1.translate((float)var9, (float)var11);
                this.mEdgeGlowTop.setSize(var6, var10);
                if (this.mEdgeGlowTop.draw(var1)) {
                    ViewCompat.postInvalidateOnAnimation(this);
                }

                var1.restoreToCount(var5);
            }

            if (!this.mEdgeGlowBottom.isFinished()) {
                label41: {
                    var5 = var1.save();
                    var11 = this.getWidth();
                    var8 = this.getHeight();
                    var7 = Math.max(this.getScrollRange(), var2) + var8;
                    if (Build.VERSION.SDK_INT >= 21) {
                        var9 = var4;
                        var6 = var11;
                        if (!this.getClipToPadding()) {
                            break label41;
                        }
                    }

                    var6 = var11 - (this.getPaddingLeft() + this.getPaddingRight());
                    var9 = 0 + this.getPaddingLeft();
                }

                var10 = var7;
                var11 = var8;
                if (Build.VERSION.SDK_INT >= 21) {
                    var10 = var7;
                    var11 = var8;
                    if (this.getClipToPadding()) {
                        var11 = var8 - (this.getPaddingTop() + this.getPaddingBottom());
                        var10 = var7 - this.getPaddingBottom();
                    }
                }

                var1.translate((float)(var9 - var6), (float)var10);
                var1.rotate(180.0F, (float)var6, 0.0F);
                this.mEdgeGlowBottom.setSize(var6, var11);
                if (this.mEdgeGlowBottom.draw(var1)) {
                    ViewCompat.postInvalidateOnAnimation(this);
                }

                var1.restoreToCount(var5);
            }
        }

    }

    @SuppressLint("WrongConstant")
    public final void endDrag() {
        this.mIsBeingDragged = false;
        this.recycleVelocityTracker();
        this.stopNestedScroll(0);
        SamsungEdgeEffect var1 = this.mEdgeGlowTop;
        if (var1 != null) {
            var1.onRelease();
            this.mEdgeGlowBottom.onRelease();
        }

    }

    public final void ensureGlows() {
        if (this.getOverScrollMode() != 2) {
            if (this.mEdgeGlowTop == null) {
                Context var1 = this.getContext();
                this.mEdgeGlowTop = new SamsungEdgeEffect(var1);
                this.mEdgeGlowBottom = new SamsungEdgeEffect(var1);
                this.mEdgeGlowTop.setSeslHostView(this);
                this.mEdgeGlowBottom.setSeslHostView(this);
            }
        } else {
            this.mEdgeGlowTop = null;
            this.mEdgeGlowBottom = null;
        }

    }

    public boolean executeKeyEvent(KeyEvent var1) {
        this.mTempRect.setEmpty();
        boolean var2 = this.canScroll();
        boolean var3 = false;
        boolean var4 = false;
        short var5 = 130;
        if (!var2) {
            var2 = var4;
            if (this.isFocused()) {
                var2 = var4;
                if (var1.getKeyCode() != 4) {
                    View var6 = this.findFocus();
                    View var8 = var6;
                    if (var6 == this) {
                        var8 = null;
                    }

                    var8 = FocusFinder.getInstance().findNextFocus(this, var8, 130);
                    var2 = var4;
                    if (var8 != null) {
                        var2 = var4;
                        if (var8 != this) {
                            var2 = var4;
                            if (var8.requestFocus(130)) {
                                var2 = true;
                            }
                        }
                    }
                }
            }

            return var2;
        } else {
            var2 = var3;
            if (var1.getAction() == 0) {
                int var7 = var1.getKeyCode();
                if (var7 != 19) {
                    if (var7 != 20) {
                        if (var7 != 62) {
                            var2 = var3;
                        } else {
                            if (var1.isShiftPressed()) {
                                var5 = 33;
                            }

                            this.pageScroll(var5);
                            var2 = var3;
                        }
                    } else if (!var1.isAltPressed()) {
                        var2 = this.arrowScroll(130);
                    } else {
                        var2 = this.fullScroll(130);
                    }
                } else if (!var1.isAltPressed()) {
                    var2 = this.arrowScroll(33);
                } else {
                    var2 = this.fullScroll(33);
                }
            }

            return var2;
        }
    }

    public final View findFocusableViewInBounds(boolean var1, int var2, int var3) {
        @SuppressLint("WrongConstant") ArrayList var4 = this.getFocusables(2);
        int var5 = var4.size();
        byte var6 = 0;
        View var7 = null;

        byte var13;
        for(int var8 = var6; var8 < var5; var6 = var13) {
            View var9 = (View)var4.get(var8);
            int var10 = var9.getTop();
            int var11 = var9.getBottom();
            View var12 = var7;
            var13 = var6;
            if (var2 < var11) {
                var12 = var7;
                var13 = var6;
                if (var10 < var3) {
                    byte var14;
                    if (var2 < var10 && var11 < var3) {
                        var14 = 1;
                    } else {
                        var14 = 0;
                    }

                    if (var7 == null) {
                        var12 = var9;
                        var13 = var14;
                    } else {
                        label77: {
                            boolean var15;
                            if (var1 && var10 < var7.getTop() || !var1 && var11 > var7.getBottom()) {
                                var15 = true;
                            } else {
                                var15 = false;
                            }

                            if (var6 != 0) {
                                var12 = var7;
                                var13 = var6;
                                if (var14 == 0) {
                                    break label77;
                                }

                                var12 = var7;
                                var13 = var6;
                                if (!var15) {
                                    break label77;
                                }
                            } else {
                                if (var14 != 0) {
                                    var12 = var9;
                                    var13 = 1;
                                    break label77;
                                }

                                var12 = var7;
                                var13 = var6;
                                if (!var15) {
                                    break label77;
                                }
                            }

                            var12 = var9;
                            var13 = var6;
                        }
                    }
                }
            }

            ++var8;
            var7 = var12;
        }

        return var7;
    }

    public void fling(int var1) {
        if (this.getChildCount() > 0) {
            this.mScroller.fling(this.getScrollX(), this.getScrollY(), 0, var1, 0, 0, -2147483648, 2147483647, 0, 0);
            this.runAnimatedScroll(true);
        }

    }

    public boolean fullScroll(int var1) {
        boolean var2;
        if (var1 == 130) {
            var2 = true;
        } else {
            var2 = false;
        }

        int var3 = this.getHeight();
        Rect var4 = this.mTempRect;
        var4.top = 0;
        var4.bottom = var3;
        if (var2) {
            int var6 = this.getChildCount();
            if (var6 > 0) {
                View var5 = this.getChildAt(var6 - 1);
                LayoutParams var7 = (LayoutParams)var5.getLayoutParams();
                this.mTempRect.bottom = var5.getBottom() + var7.bottomMargin + this.getPaddingBottom();
                var4 = this.mTempRect;
                var4.top = var4.bottom - var3;
            }
        }

        var4 = this.mTempRect;
        return this.scrollAndFocus(var1, var4.top, var4.bottom);
    }

    public float getBottomFadingEdgeStrength() {
        if (this.getChildCount() == 0) {
            return 0.0F;
        } else {
            View var1 = this.getChildAt(0);
            LayoutParams var2 = (LayoutParams)var1.getLayoutParams();
            int var3 = this.getVerticalFadingEdgeLength();
            int var4 = this.getHeight();
            int var5 = this.getPaddingBottom();
            var4 = var1.getBottom() + var2.bottomMargin - this.getScrollY() - (var4 - var5);
            return var4 < var3 ? (float)var4 / (float)var3 : 1.0F;
        }
    }

    public int getMaxScrollAmount() {
        return (int)((float)this.getHeight() * 0.5F);
    }

    public int getNestedScrollAxes() {
        return this.mParentHelper.getNestedScrollAxes();
    }

    public int getScrollRange() {
        int var1 = this.getChildCount();
        int var2 = 0;
        if (var1 > 0) {
            View var3 = this.getChildAt(0);
            LayoutParams var4 = (LayoutParams)var3.getLayoutParams();
            var2 = Math.max(0, var3.getHeight() + var4.topMargin + var4.bottomMargin - (this.getHeight() - this.getPaddingTop() - this.getPaddingBottom()));
        }

        return var2;
    }

    public float getTopFadingEdgeStrength() {
        if (this.getChildCount() == 0) {
            return 0.0F;
        } else {
            int var1 = this.getVerticalFadingEdgeLength();
            int var2 = this.getScrollY();
            return var2 < var1 ? (float)var2 / (float)var1 : 1.0F;
        }
    }

    @SuppressLint("WrongConstant")
    public boolean hasNestedScrollingParent() {
        return this.hasNestedScrollingParent(0);
    }

    public boolean hasNestedScrollingParent(int var1) {
        return this.mChildHelper.hasNestedScrollingParent(var1);
    }

    public final boolean inChild(int var1, int var2) {
        int var3 = this.getChildCount();
        boolean var4 = false;
        boolean var5 = var4;
        if (var3 > 0) {
            var3 = this.getScrollY();
            View var6 = this.getChildAt(0);
            var5 = var4;
            if (var2 >= var6.getTop() - var3) {
                var5 = var4;
                if (var2 < var6.getBottom() - var3) {
                    var5 = var4;
                    if (var1 >= var6.getLeft()) {
                        var5 = var4;
                        if (var1 < var6.getRight()) {
                            var5 = true;
                        }
                    }
                }
            }
        }

        return var5;
    }

    public final void initOrResetVelocityTracker() {
        VelocityTracker var1 = this.mVelocityTracker;
        if (var1 == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        } else {
            var1.clear();
        }

    }

    @SuppressLint("WrongConstant")
    public final void initScrollView() {
        this.mScroller = new OverScroller(this.getContext());
        this.setFocusable(true);
        this.setDescendantFocusability(262144);
        this.setWillNotDraw(false);
        ViewConfiguration var1 = ViewConfiguration.get(this.getContext());
        this.mTouchSlop = var1.getScaledTouchSlop();
        this.mMinimumVelocity = var1.getScaledMinimumFlingVelocity();
        this.mMaximumVelocity = var1.getScaledMaximumFlingVelocity();
    }

    public final void initVelocityTrackerIfNotExists() {
        if (this.mVelocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        }

    }

    public boolean isNestedScrollingEnabled() {
        return this.mChildHelper.isNestedScrollingEnabled();
    }

    public final boolean isOffScreen(View var1) {
        int var2 = this.getHeight();
        boolean var3 = false;
        if (!this.isWithinDeltaOfScreen(var1, 0, var2)) {
            var3 = true;
        }

        return var3;
    }

    public final boolean isWithinDeltaOfScreen(View var1, int var2, int var3) {
        var1.getDrawingRect(this.mTempRect);
        this.offsetDescendantRectToMyCoords(var1, this.mTempRect);
        boolean var4;
        if (this.mTempRect.bottom + var2 >= this.getScrollY() && this.mTempRect.top - var2 <= this.getScrollY() + var3) {
            var4 = true;
        } else {
            var4 = false;
        }

        return var4;
    }

    public void measureChild(View child, int parentWidthMeasureSpec, int parentHeightMeasureSpec) {
        ViewGroup.LayoutParams lp = child.getLayoutParams();

        int childWidthMeasureSpec;
        int childHeightMeasureSpec;

        childWidthMeasureSpec = getChildMeasureSpec(parentWidthMeasureSpec, getPaddingLeft() + getPaddingRight(), lp.width);

        childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);

        child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
    }


    @SuppressLint("WrongConstant")
    public void measureChildWithMargins(View var1, int var2, int var3, int var4, int var5) {
        MarginLayoutParams var6 = (MarginLayoutParams)var1.getLayoutParams();
        var1.measure(FrameLayout.getChildMeasureSpec(var2, this.getPaddingLeft() + this.getPaddingRight() + var6.leftMargin + var6.rightMargin + var3, var6.width), MeasureSpec.makeMeasureSpec(var6.topMargin + var6.bottomMargin, 0));
    }

    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mIsLaidOut = false;
    }

    @SuppressLint("WrongConstant")
    public boolean onGenericMotionEvent(MotionEvent var1) {
        if ((var1.getSource() & 2) != 0 && var1.getAction() == 8 && !this.mIsBeingDragged) {
            float var2 = var1.getAxisValue(9);
            if (var2 != 0.0F) {
                int var3 = (int)(var2 * this.getVerticalScrollFactorCompat());
                int var4 = this.getScrollRange();
                int var5 = this.getScrollY();
                int var6 = var5 - var3;
                if (var6 < 0) {
                    var3 = 0;
                } else {
                    var3 = var6;
                    if (var6 > var4) {
                        var3 = var4;
                    }
                }

                if (var3 != var5) {
                    this.startNestedScroll(var3, 1);
                    if (!this.dispatchNestedPreScroll(0, var3, (int[])null, (int[])null, 1)) {
                        super.scrollTo(this.getScrollX(), var3);
                    }

                    return true;
                }
            }
        }

        return false;
    }

    @SuppressLint("WrongConstant")
    public boolean onInterceptTouchEvent(MotionEvent var1) {
        int var2 = var1.getAction();
        if (var2 == 2 && this.mIsBeingDragged) {
            return true;
        } else {
            var2 &= 255;
            if (var2 != 0) {
                if (var2 != 1) {
                    if (var2 == 2) {
                        var2 = this.mActivePointerId;
                        if (var2 != -1) {
                            int var3 = var1.findPointerIndex(var2);
                            if (var3 == -1) {
                                StringBuilder var4 = new StringBuilder();
                                var4.append("Invalid pointerId=");
                                var4.append(var2);
                                var4.append(" in onInterceptTouchEvent");
                                Log.e("NestedScrollView", var4.toString());
                            } else {
                                var2 = (int)var1.getY(var3);
                                if (Math.abs(var2 - this.mLastMotionY) > this.mTouchSlop && (2 & this.getNestedScrollAxes()) == 0) {
                                    this.mIsBeingDragged = true;
                                    this.mLastMotionY = var2;
                                    this.initVelocityTrackerIfNotExists();
                                    this.mVelocityTracker.addMovement(var1);
                                    this.mNestedYOffset = 0;
                                    ViewParent var5 = this.getParent();
                                    if (var5 != null) {
                                        var5.requestDisallowInterceptTouchEvent(true);
                                    }

                                    return this.mIsBeingDragged;
                                }
                            }

                            return this.mIsBeingDragged;
                        }

                        return this.mIsBeingDragged;
                    }

                    if (var2 != 3) {
                        if (var2 == 6) {
                            this.onSecondaryPointerUp(var1);
                        }

                        return this.mIsBeingDragged;
                    }
                }

                this.mIsBeingDragged = false;
                this.mActivePointerId = -1;
                this.recycleVelocityTracker();
                if (this.mScroller.springBack(this.getScrollX(), this.getScrollY(), 0, 0, 0, this.getScrollRange())) {
                    ViewCompat.postInvalidateOnAnimation(this);
                }

                this.stopNestedScroll(0);
            } else {
                var2 = (int)var1.getY();
                if (!this.inChild((int)var1.getX(), var2)) {
                    this.mIsBeingDragged = false;
                    this.recycleVelocityTracker();
                } else {
                    this.mLastMotionY = var2;
                    this.mActivePointerId = var1.getPointerId(0);
                    this.initOrResetVelocityTracker();
                    this.mVelocityTracker.addMovement(var1);
                    this.mScroller.computeScrollOffset();
                    this.mIsBeingDragged = this.mScroller.isFinished() ^ true;
                    this.startNestedScroll(2, 0);
                }
            }

            return this.mIsBeingDragged;
        }
    }

    public void onLayout(boolean var1, int var2, int var3, int var4, int var5) {
        super.onLayout(var1, var2, var3, var4, var5);
        var2 = 0;
        this.mIsLayoutDirty = false;
        View var6 = this.mChildToScrollTo;
        if (var6 != null && isViewDescendantOf(var6, this)) {
            this.scrollToChild(this.mChildToScrollTo);
        }

        this.mChildToScrollTo = null;
        if (!this.mIsLaidOut) {
            if (this.mSavedState != null) {
                this.scrollTo(this.getScrollX(), this.mSavedState.scrollPosition);
                this.mSavedState = null;
            }

            if (this.getChildCount() > 0) {
                View var7 = this.getChildAt(0);
                LayoutParams var10 = (LayoutParams)var7.getLayoutParams();
                var2 = var7.getMeasuredHeight() + var10.topMargin + var10.bottomMargin;
            }

            int var8 = this.getPaddingTop();
            int var9 = this.getPaddingBottom();
            var4 = this.getScrollY();
            var2 = clamp(var4, var5 - var3 - var8 - var9, var2);
            if (var2 != var4) {
                this.scrollTo(this.getScrollX(), var2);
            }
        }

        this.scrollTo(this.getScrollX(), this.getScrollY());
        this.mIsLaidOut = true;
    }

    @SuppressLint("WrongConstant")
    public void onMeasure(int var1, int var2) {
        super.onMeasure(var1, var2);
        if (this.mFillViewport) {
            if (MeasureSpec.getMode(var2) != 0) {
                if (this.getChildCount() > 0) {
                    View var3 = this.getChildAt(0);
                    LayoutParams var4 = (LayoutParams)var3.getLayoutParams();
                    int var5 = var3.getMeasuredHeight();
                    var2 = this.getMeasuredHeight() - this.getPaddingTop() - this.getPaddingBottom() - var4.topMargin - var4.bottomMargin;
                    if (var5 < var2) {
                        var3.measure(FrameLayout.getChildMeasureSpec(var1, this.getPaddingLeft() + this.getPaddingRight() + var4.leftMargin + var4.rightMargin, var4.width), MeasureSpec.makeMeasureSpec(var2, 1073741824));
                    }
                }

            }
        }
    }

    public boolean onNestedFling(View var1, float var2, float var3, boolean var4) {
        if (!var4) {
            this.dispatchNestedFling(0.0F, var3, true);
            this.fling((int)var3);
            return true;
        } else {
            return false;
        }
    }

    public boolean onNestedPreFling(View var1, float var2, float var3) {
        return this.dispatchNestedPreFling(var2, var3);
    }

    @SuppressLint("WrongConstant")
    public void onNestedPreScroll(View var1, int var2, int var3, int[] var4) {
        this.onNestedPreScroll(var1, var2, var3, var4, 0);
    }

    public void onNestedPreScroll(View var1, int var2, int var3, int[] var4, int var5) {
        this.dispatchNestedPreScroll(var2, var3, var4, (int[])null, var5);
    }

    public void onNestedScroll(View var1, int var2, int var3, int var4, int var5) {
        this.onNestedScrollInternal(var5, 0, (int[])null);
    }

    public void onNestedScroll(View var1, int var2, int var3, int var4, int var5, int var6) {
        this.onNestedScrollInternal(var5, var6, (int[])null);
    }

    public void onNestedScroll(View var1, int var2, int var3, int var4, int var5, int var6, int[] var7) {
        this.onNestedScrollInternal(var5, var6, var7);
    }

    @SuppressLint("WrongConstant")
    public void onNestedScrollAccepted(View var1, View var2, int var3) {
        this.onNestedScrollAccepted(var1, var2, var3, 0);
    }

    @SuppressLint("WrongConstant")
    public void onNestedScrollAccepted(View var1, View var2, int var3, int var4) {
        this.mParentHelper.onNestedScrollAccepted(var1, var2, var3, var4);
        this.startNestedScroll(2, var4);
    }

    public final void onNestedScrollInternal(int var1, int var2, int[] var3) {
        int var4 = this.getScrollY();
        this.scrollBy(0, var1);
        var4 = this.getScrollY() - var4;
        if (var3 != null) {
            var3[1] += var4;
        }

        this.mChildHelper.dispatchNestedScroll(0, var4, 0, var1 - var4, (int[])null, var2, var3);
    }

    public void onOverScrolled(int var1, int var2, boolean var3, boolean var4) {
        super.scrollTo(var1, var2);
    }

    public boolean onRequestFocusInDescendants(int var1, Rect var2) {
        int var3;
        if (var1 == 2) {
            var3 = 130;
        } else {
            var3 = var1;
            if (var1 == 1) {
                var3 = 33;
            }
        }

        View var4;
        if (var2 == null) {
            var4 = FocusFinder.getInstance().findNextFocus(this, (View)null, var3);
        } else {
            var4 = FocusFinder.getInstance().findNextFocusFromRect(this, var2, var3);
        }

        if (var4 == null) {
            return false;
        } else {
            return this.isOffScreen(var4) ? false : var4.requestFocus(var3, var2);
        }
    }

    public void onRestoreInstanceState(Parcelable var1) {
        if (!(var1 instanceof SavedState)) {
            super.onRestoreInstanceState(var1);
        } else {
            SavedState var2 = (SavedState)var1;
            super.onRestoreInstanceState(var2.getSuperState());
            this.mSavedState = var2;
            this.requestLayout();
        }
    }

    public Parcelable onSaveInstanceState() {
        SavedState var1 = new SavedState(super.onSaveInstanceState());
        var1.scrollPosition = this.getScrollY();
        return var1;
    }

    public void onScrollChanged(int var1, int var2, int var3, int var4) {
        super.onScrollChanged(var1, var2, var3, var4);
        OnScrollChangeListener var5 = this.mOnScrollChangeListener;
        if (var5 != null) {
            var5.onScrollChange(this, var1, var2, var3, var4);
        }

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

            this.mLastMotionY = (int)var1.getY(var4);
            this.mActivePointerId = var1.getPointerId(var4);
            VelocityTracker var3 = this.mVelocityTracker;
            if (var3 != null) {
                var3.clear();
            }
        }

    }

    public void onSizeChanged(int var1, int var2, int var3, int var4) {
        super.onSizeChanged(var1, var2, var3, var4);
        View var5 = this.findFocus();
        if (var5 != null && this != var5 && this.isWithinDeltaOfScreen(var5, 0, var4)) {
            var5.getDrawingRect(this.mTempRect);
            this.offsetDescendantRectToMyCoords(var5, this.mTempRect);
            this.doScrollY(this.computeScrollDeltaToGetChildRectOnScreen(this.mTempRect));
        }

    }

    @SuppressLint("WrongConstant")
    public boolean onStartNestedScroll(View var1, View var2, int var3) {
        return this.onStartNestedScroll(var1, var2, var3, 0);
    }

    public boolean onStartNestedScroll(View var1, View var2, int var3, int var4) {
        boolean var5;
        if ((var3 & 2) != 0) {
            var5 = true;
        } else {
            var5 = false;
        }

        return var5;
    }

    @SuppressLint("WrongConstant")
    public void onStopNestedScroll(View var1) {
        this.onStopNestedScroll(var1, 0);
    }

    public void onStopNestedScroll(View var1, int var2) {
        this.mParentHelper.onStopNestedScroll(var1, var2);
        this.stopNestedScroll(var2);
    }

    @SuppressLint("WrongConstant")
    public boolean onTouchEvent(MotionEvent var1) {
        this.initVelocityTrackerIfNotExists();
        MotionEvent var2 = MotionEvent.obtain(var1);
        int var3 = var1.getActionMasked();
        if (var3 == 0) {
            this.mNestedYOffset = 0;
        }

        var2.offsetLocation(0.0F, (float)this.mNestedYOffset);
        ViewParent var7;
        VelocityTracker var14;
        if (var3 != 0) {
            if (var3 != 1) {
                if (var3 != 2) {
                    if (var3 != 3) {
                        if (var3 != 5) {
                            if (var3 == 6) {
                                this.onSecondaryPointerUp(var1);
                                this.mLastMotionY = (int)var1.getY(var1.findPointerIndex(this.mActivePointerId));
                            }
                        } else {
                            var3 = var1.getActionIndex();
                            this.mLastMotionY = (int)var1.getY(var3);
                            this.mActivePointerId = var1.getPointerId(var3);
                        }
                    } else {
                        if (this.mIsBeingDragged && this.getChildCount() > 0 && this.mScroller.springBack(this.getScrollX(), this.getScrollY(), 0, 0, 0, this.getScrollRange())) {
                            ViewCompat.postInvalidateOnAnimation(this);
                        }

                        this.mActivePointerId = -1;
                        this.endDrag();
                    }
                } else {
                    int var4 = var1.findPointerIndex(this.mActivePointerId);
                    if (var4 == -1) {
                        StringBuilder var12 = new StringBuilder();
                        var12.append("Invalid pointerId=");
                        var12.append(this.mActivePointerId);
                        var12.append(" in onTouchEvent");
                        Log.e("NestedScrollView", var12.toString());
                    } else {
                        int var5 = (int)var1.getY(var4);
                        var3 = this.mLastMotionY - var5;
                        int var6 = var3;
                        if (this.dispatchNestedPreScroll(0, var3, this.mScrollConsumed, this.mScrollOffset, 0)) {
                            var6 = var3 - this.mScrollConsumed[1];
                            var2.offsetLocation(0.0F, (float)this.mScrollOffset[1]);
                            this.mNestedYOffset += this.mScrollOffset[1];
                        }

                        var3 = var6;
                        if (!this.mIsBeingDragged) {
                            var3 = var6;
                            if (Math.abs(var6) > this.mTouchSlop) {
                                var7 = this.getParent();
                                if (var7 != null) {
                                    var7.requestDisallowInterceptTouchEvent(true);
                                }

                                this.mIsBeingDragged = true;
                                if (var6 > 0) {
                                    var3 = var6 - this.mTouchSlop;
                                } else {
                                    var3 = var6 + this.mTouchSlop;
                                }
                            }
                        }

                        if (this.mIsBeingDragged) {
                            this.mLastMotionY = var5 - this.mScrollOffset[1];
                            int var8 = this.getScrollY();
                            var5 = this.getScrollRange();
                            var6 = this.getOverScrollMode();
                            boolean var15;
                            if (var6 == 0 || var6 == 1 && var5 > 0) {
                                var15 = true;
                            } else {
                                var15 = false;
                            }

                            if (this.overScrollByCompat(0, var3, 0, this.getScrollY(), 0, var5, 0, 0, true) && !this.hasNestedScrollingParent(0)) {
                                this.mVelocityTracker.clear();
                            }

                            int var9 = this.getScrollY() - var8;
                            int[] var16 = this.mScrollConsumed;
                            var16[1] = 0;
                            this.dispatchNestedScroll(0, var9, 0, var3 - var9, this.mScrollOffset, 0, var16);
                            var9 = this.mLastMotionY;
                            var16 = this.mScrollOffset;
                            this.mLastMotionY = var9 - var16[1];
                            var2.offsetLocation(0.0F, (float)var16[1]);
                            this.mNestedYOffset += this.mScrollOffset[1];
                            if (var15) {
                                var3 -= this.mScrollConsumed[1];
                                this.ensureGlows();
                                var6 = var8 + var3;
                                if (var6 < 0) {
                                    EdgeEffectCompat.onPull(this.mEdgeGlowTop, (float)var3 / (float)this.getHeight(), var1.getX(var4) / (float)this.getWidth());
                                    if (!this.mEdgeGlowBottom.isFinished()) {
                                        this.mEdgeGlowBottom.onRelease();
                                    }
                                } else if (var6 > var5) {
                                    EdgeEffectCompat.onPull(this.mEdgeGlowBottom, (float)var3 / (float)this.getHeight(), 1.0F - var1.getX(var4) / (float)this.getWidth());
                                    if (!this.mEdgeGlowTop.isFinished()) {
                                        this.mEdgeGlowTop.onRelease();
                                    }
                                }

                                SamsungEdgeEffect var13 = this.mEdgeGlowTop;
                                if (var13 != null && (!var13.isFinished() || !this.mEdgeGlowBottom.isFinished())) {
                                    ViewCompat.postInvalidateOnAnimation(this);
                                }
                            }
                        }
                    }
                }
            } else {
                var14 = this.mVelocityTracker;
                var14.computeCurrentVelocity(1000, (float)this.mMaximumVelocity);
                var3 = (int)var14.getYVelocity(this.mActivePointerId);
                if (Math.abs(var3) > this.mMinimumVelocity) {
                    var3 = -var3;
                    float var10 = (float)var3;
                    if (!this.dispatchNestedPreFling(0.0F, var10)) {
                        this.dispatchNestedFling(0.0F, var10, true);
                        this.fling(var3);
                    }
                } else if (this.mScroller.springBack(this.getScrollX(), this.getScrollY(), 0, 0, 0, this.getScrollRange())) {
                    ViewCompat.postInvalidateOnAnimation(this);
                }

                this.mActivePointerId = -1;
                this.endDrag();
            }
        } else {
            if (this.getChildCount() == 0) {
                return false;
            }

            boolean var11 = this.mScroller.isFinished() ^ true;
            this.mIsBeingDragged = var11;
            if (var11) {
                var7 = this.getParent();
                if (var7 != null) {
                    var7.requestDisallowInterceptTouchEvent(true);
                }
            }

            if (!this.mScroller.isFinished()) {
                this.abortAnimatedScroll();
            }

            this.mLastMotionY = (int)var1.getY();
            this.mActivePointerId = var1.getPointerId(0);
            this.startNestedScroll(2, 0);
        }

        var14 = this.mVelocityTracker;
        if (var14 != null) {
            var14.addMovement(var2);
        }

        var2.recycle();
        return true;
    }

    @SuppressLint("WrongConstant")
    public boolean overScrollByCompat(int var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, boolean var9) {
        int var10 = this.getOverScrollMode();
        int var11 = this.computeHorizontalScrollRange();
        int var12 = this.computeHorizontalScrollExtent();
        boolean var13 = false;
        boolean var16;
        if (var11 > var12) {
            var16 = true;
        } else {
            var16 = false;
        }

        boolean var15;
        if (this.computeVerticalScrollRange() > this.computeVerticalScrollExtent()) {
            var15 = true;
        } else {
            var15 = false;
        }

        if (var10 == 0 || var10 == 1 && var16) {
            var16 = true;
        } else {
            var16 = false;
        }

        if (var10 == 0 || var10 == 1 && var15) {
            var15 = true;
        } else {
            var15 = false;
        }

        var3 += var1;
        if (!var16) {
            var1 = 0;
        } else {
            var1 = var7;
        }

        var4 += var2;
        if (!var15) {
            var2 = 0;
        } else {
            var2 = var8;
        }

        boolean var14;
        label66: {
            var7 = -var1;
            var1 += var5;
            var5 = -var2;
            var6 += var2;
            if (var3 <= var1) {
                if (var3 >= var7) {
                    var2 = var3;
                    var14 = false;
                    break label66;
                }

                var1 = var7;
            }

            var14 = true;
            var2 = var1;
        }

        if (var4 > var6) {
            var1 = var6;
            var9 = true;
        } else if (var4 < var5) {
            var9 = true;
            var1 = var5;
        } else {
            var9 = false;
            var1 = var4;
        }

        if (var9 && !this.hasNestedScrollingParent(1)) {
            this.mScroller.springBack(var2, var1, 0, 0, 0, this.getScrollRange());
        }

        this.onOverScrolled(var2, var1, var14, var9);
        if (!var14) {
            var14 = var13;
            if (!var9) {
                return var14;
            }
        }

        var14 = true;
        return var14;
    }

    public boolean pageScroll(int var1) {
        boolean var2;
        if (var1 == 130) {
            var2 = true;
        } else {
            var2 = false;
        }

        int var3 = this.getHeight();
        int var6;
        Rect var7;
        if (var2) {
            this.mTempRect.top = this.getScrollY() + var3;
            var6 = this.getChildCount();
            if (var6 > 0) {
                View var4 = this.getChildAt(var6 - 1);
                LayoutParams var5 = (LayoutParams)var4.getLayoutParams();
                var6 = var4.getBottom() + var5.bottomMargin + this.getPaddingBottom();
                var7 = this.mTempRect;
                if (var7.top + var3 > var6) {
                    var7.top = var6 - var3;
                }
            }
        } else {
            this.mTempRect.top = this.getScrollY() - var3;
            var7 = this.mTempRect;
            if (var7.top < 0) {
                var7.top = 0;
            }
        }

        var7 = this.mTempRect;
        var6 = var7.top;
        var7.bottom = var3 + var6;
        return this.scrollAndFocus(var1, var6, var7.bottom);
    }

    public final void recycleVelocityTracker() {
        VelocityTracker var1 = this.mVelocityTracker;
        if (var1 != null) {
            var1.recycle();
            this.mVelocityTracker = null;
        }

    }

    public void requestChildFocus(View var1, View var2) {
        if (!this.mIsLayoutDirty) {
            this.scrollToChild(var2);
        } else {
            this.mChildToScrollTo = var2;
        }

        super.requestChildFocus(var1, var2);
    }

    public boolean requestChildRectangleOnScreen(View var1, Rect var2, boolean var3) {
        var2.offset(var1.getLeft() - var1.getScrollX(), var1.getTop() - var1.getScrollY());
        return this.scrollToChildRect(var2, var3);
    }

    public void requestDisallowInterceptTouchEvent(boolean var1) {
        if (var1) {
            this.recycleVelocityTracker();
        }

        super.requestDisallowInterceptTouchEvent(var1);
    }

    public void requestLayout() {
        this.mIsLayoutDirty = true;
        super.requestLayout();
    }

    @SuppressLint("WrongConstant")
    public final void runAnimatedScroll(boolean var1) {
        if (var1) {
            this.startNestedScroll(2, 1);
        } else {
            this.stopNestedScroll(1);
        }

        this.mLastScrollerY = this.getScrollY();
        ViewCompat.postInvalidateOnAnimation(this);
    }

    public final boolean scrollAndFocus(int var1, int var2, int var3) {
        int var4 = this.getHeight();
        int var5 = this.getScrollY();
        var4 += var5;
        boolean var6 = false;
        boolean var7;
        if (var1 == 33) {
            var7 = true;
        } else {
            var7 = false;
        }

        View var8 = this.findFocusableViewInBounds(var7, var2, var3);
        Object var9 = var8;
        if (var8 == null) {
            var9 = this;
        }

        if (var2 >= var5 && var3 <= var4) {
            var7 = var6;
        } else {
            if (var7) {
                var2 -= var5;
            } else {
                var2 = var3 - var4;
            }

            this.doScrollY(var2);
            var7 = true;
        }

        if (var9 != this.findFocus()) {
            ((View)var9).requestFocus(var1);
        }

        return var7;
    }

    public void scrollTo(int var1, int var2) {
        if (this.getChildCount() > 0) {
            View var3 = this.getChildAt(0);
            LayoutParams var4 = (LayoutParams)var3.getLayoutParams();
            int var5 = this.getWidth();
            int var6 = this.getPaddingLeft();
            int var7 = this.getPaddingRight();
            int var8 = var3.getWidth();
            int var9 = var4.leftMargin;
            int var10 = var4.rightMargin;
            int var11 = this.getHeight();
            int var12 = this.getPaddingTop();
            int var13 = this.getPaddingBottom();
            int var14 = var3.getHeight();
            int var15 = var4.topMargin;
            int var16 = var4.bottomMargin;
            var1 = clamp(var1, var5 - var6 - var7, var8 + var9 + var10);
            var2 = clamp(var2, var11 - var12 - var13, var14 + var15 + var16);
            if (var1 != this.getScrollX() || var2 != this.getScrollY()) {
                super.scrollTo(var1, var2);
            }
        }

    }

    public final void scrollToChild(View var1) {
        var1.getDrawingRect(this.mTempRect);
        this.offsetDescendantRectToMyCoords(var1, this.mTempRect);
        int var2 = this.computeScrollDeltaToGetChildRectOnScreen(this.mTempRect);
        if (var2 != 0) {
            this.scrollBy(0, var2);
        }

    }

    public final boolean scrollToChildRect(Rect var1, boolean var2) {
        int var3 = this.computeScrollDeltaToGetChildRectOnScreen(var1);
        boolean var4;
        if (var3 != 0) {
            var4 = true;
        } else {
            var4 = false;
        }

        if (var4) {
            if (var2) {
                this.scrollBy(0, var3);
            } else {
                this.smoothScrollBy(0, var3);
            }
        }

        return var4;
    }

    public void setFillViewport(boolean var1) {
        if (var1 != this.mFillViewport) {
            this.mFillViewport = var1;
            this.requestLayout();
        }

    }

    public void setNestedScrollingEnabled(boolean var1) {
        this.mChildHelper.setNestedScrollingEnabled(var1);
    }

    public void setOnScrollChangeListener(OnScrollChangeListener var1) {
        this.mOnScrollChangeListener = var1;
    }

    public void setSmoothScrollingEnabled(boolean var1) {
        this.mSmoothScrollingEnabled = var1;
    }

    public boolean shouldDelayChildPressedState() {
        return true;
    }

    public final void smoothScrollBy(int var1, int var2) {
        if (this.getChildCount() != 0) {
            if (AnimationUtils.currentAnimationTimeMillis() - this.mLastScroll > 250L) {
                View var3 = this.getChildAt(0);
                LayoutParams var4 = (LayoutParams)var3.getLayoutParams();
                int var5 = var3.getHeight();
                int var6 = var4.topMargin;
                int var7 = var4.bottomMargin;
                int var8 = this.getHeight();
                int var9 = this.getPaddingTop();
                int var10 = this.getPaddingBottom();
                var1 = this.getScrollY();
                var2 = Math.max(0, Math.min(var2 + var1, Math.max(0, var5 + var6 + var7 - (var8 - var9 - var10))));
                this.mScroller.startScroll(this.getScrollX(), var1, 0, var2 - var1);
                this.runAnimatedScroll(false);
            } else {
                if (!this.mScroller.isFinished()) {
                    this.abortAnimatedScroll();
                }

                this.scrollBy(var1, var2);
            }

            this.mLastScroll = AnimationUtils.currentAnimationTimeMillis();
        }
    }

    @SuppressLint("WrongConstant")
    public boolean startNestedScroll(int var1) {
        return this.startNestedScroll(var1, 0);
    }

    public boolean startNestedScroll(int var1, int var2) {
        return this.mChildHelper.startNestedScroll(var1, var2);
    }

    @SuppressLint("WrongConstant")
    public void stopNestedScroll() {
        this.stopNestedScroll(0);
    }

    public void stopNestedScroll(int var1) {
        this.mChildHelper.stopNestedScroll(var1);
    }

    public static class AccessibilityDelegate extends AccessibilityDelegateCompat {
        public AccessibilityDelegate() {
        }

        public void onInitializeAccessibilityEvent(View var1, AccessibilityEvent var2) {
            super.onInitializeAccessibilityEvent(var1, var2);
            SamsungNestedScrollView var4 = (SamsungNestedScrollView)var1;
            var2.setClassName(ScrollView.class.getName());
            boolean var3;
            if (var4.getScrollRange() > 0) {
                var3 = true;
            } else {
                var3 = false;
            }

            var2.setScrollable(var3);
            var2.setScrollX(var4.getScrollX());
            var2.setScrollY(var4.getScrollY());
            AccessibilityRecordCompat.setMaxScrollX(var2, var4.getScrollX());
            AccessibilityRecordCompat.setMaxScrollY(var2, var4.getScrollRange());
        }

        public void onInitializeAccessibilityNodeInfo(View var1, AccessibilityNodeInfoCompat var2) {
            super.onInitializeAccessibilityNodeInfo(var1, var2);
            SamsungNestedScrollView var4 = (SamsungNestedScrollView)var1;
            var2.setClassName(ScrollView.class.getName());
            if (var4.isEnabled()) {
                int var3 = var4.getScrollRange();
                if (var3 > 0) {
                    var2.setScrollable(true);
                    if (var4.getScrollY() > 0) {
                        var2.addAction(8192);
                    }

                    if (var4.getScrollY() < var3) {
                        var2.addAction(4096);
                    }
                }
            }

        }

        public boolean performAccessibilityAction(View var1, int var2, Bundle var3) {
            if (super.performAccessibilityAction(var1, var2, var3)) {
                return true;
            } else {
                SamsungNestedScrollView var6 = (SamsungNestedScrollView)var1;
                if (!var6.isEnabled()) {
                    return false;
                } else {
                    int var4;
                    int var5;
                    if (var2 != 4096) {
                        if (var2 != 8192) {
                            return false;
                        } else {
                            var4 = var6.getHeight();
                            var2 = var6.getPaddingBottom();
                            var5 = var6.getPaddingTop();
                            var2 = Math.max(var6.getScrollY() - (var4 - var2 - var5), 0);
                            if (var2 != var6.getScrollY()) {
                                var6.scrollTo(0, var2);
                                return true;
                            } else {
                                return false;
                            }
                        }
                    } else {
                        var4 = var6.getHeight();
                        var2 = var6.getPaddingBottom();
                        var5 = var6.getPaddingTop();
                        var2 = Math.min(var6.getScrollY() + (var4 - var2 - var5), var6.getScrollRange());
                        if (var2 != var6.getScrollY()) {
                            var6.scrollTo(0, var2);
                            return true;
                        } else {
                            return false;
                        }
                    }
                }
            }
        }
    }

    public interface OnScrollChangeListener {
        void onScrollChange(SamsungNestedScrollView var1, int var2, int var3, int var4, int var5);
    }

    public static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel var1) {
                return new SavedState(var1);
            }

            public SavedState[] newArray(int var1) {
                return new SavedState[var1];
            }
        };
        public int scrollPosition;

        public SavedState(Parcel var1) {
            super(var1);
            this.scrollPosition = var1.readInt();
        }

        public SavedState(Parcelable var1) {
            super(var1);
        }

        public String toString() {
            StringBuilder var1 = new StringBuilder();
            var1.append("HorizontalScrollView.SavedState{");
            var1.append(Integer.toHexString(System.identityHashCode(this)));
            var1.append(" scrollPosition=");
            var1.append(this.scrollPosition);
            var1.append("}");
            return var1.toString();
        }

        public void writeToParcel(Parcel var1, int var2) {
            super.writeToParcel(var1, var2);
            var1.writeInt(this.scrollPosition);
        }
    }
}
