package de.dlyt.yanndroid.oneui.sesl.appbar;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;

import de.dlyt.yanndroid.oneui.layout.CoordinatorLayout;

public abstract class HeaderBehavior<V extends View> extends ViewOffsetBehavior<V> {
    public int activePointerId = -1;
    public boolean isBeingDragged;
    public int lastMotionY;
    public int mLastInterceptTouchEvent;
    public int mLastTouchEvent;
    public int touchSlop = -1;
    public VelocityTracker velocityTracker;

    public HeaderBehavior() {
    }

    public HeaderBehavior(Context var1, AttributeSet var2) {
        super(var1, var2);
    }

    public abstract boolean canDragView(V var1);

    public final void ensureVelocityTracker() {
        if (this.velocityTracker == null) {
            this.velocityTracker = VelocityTracker.obtain();
        }

    }

    public int getLastInterceptTouchEventEvent() {
        return this.mLastInterceptTouchEvent;
    }

    public int getLastTouchEventEvent() {
        return this.mLastTouchEvent;
    }

    public abstract int getMaxDragOffset(V var1);

    public abstract int getTopBottomOffsetForScrollingSibling();

    public boolean onInterceptTouchEvent(CoordinatorLayout var1, V var2, MotionEvent var3) {
        if (this.touchSlop < 0) {
            this.touchSlop = ViewConfiguration.get(var1.getContext()).getScaledTouchSlop();
        }

        int var4 = var3.getAction();
        this.mLastInterceptTouchEvent = var4;
        if (var4 == 2 && this.isBeingDragged) {
            return true;
        } else {
            var4 = var3.getActionMasked();
            VelocityTracker var6;
            if (var4 != 0) {
                label43:
                {
                    if (var4 != 1) {
                        if (var4 == 2) {
                            var4 = this.activePointerId;
                            if (var4 != -1) {
                                var4 = var3.findPointerIndex(var4);
                                if (var4 != -1) {
                                    var4 = (int) var3.getY(var4);
                                    if (Math.abs(var4 - this.lastMotionY) > this.touchSlop) {
                                        this.isBeingDragged = true;
                                        this.lastMotionY = var4;
                                    }
                                }
                            }
                            break label43;
                        }

                        if (var4 != 3) {
                            break label43;
                        }
                    }

                    this.isBeingDragged = false;
                    this.activePointerId = -1;
                    var6 = this.velocityTracker;
                    if (var6 != null) {
                        var6.recycle();
                        this.velocityTracker = null;
                    }
                }
            } else {
                this.isBeingDragged = false;
                int var5 = (int) var3.getX();
                var4 = (int) var3.getY();
                if (this.canDragView(var2) && var1.isPointInChildBounds(var2, var5, var4)) {
                    this.lastMotionY = var4;
                    this.activePointerId = var3.getPointerId(0);
                    this.ensureVelocityTracker();
                }
            }

            var6 = this.velocityTracker;
            if (var6 != null) {
                var6.addMovement(var3);
            }

            return this.isBeingDragged;
        }
    }

    public boolean onTouchEvent(CoordinatorLayout var1, V var2, MotionEvent var3) {
        if (this.touchSlop < 0) {
            this.touchSlop = ViewConfiguration.get(var1.getContext()).getScaledTouchSlop();
        }

        this.mLastTouchEvent = var3.getAction();
        int var4 = var3.getActionMasked();
        int var6;
        VelocityTracker var9;
        if (var4 != 0) {
            label45:
            {
                if (var4 != 1) {
                    if (var4 == 2) {
                        var4 = var3.findPointerIndex(this.activePointerId);
                        if (var4 == -1) {
                            return false;
                        }

                        int var5 = (int) var3.getY(var4);
                        var6 = this.lastMotionY - var5;
                        var4 = var6;
                        if (!this.isBeingDragged) {
                            int var7 = Math.abs(var6);
                            int var8 = this.touchSlop;
                            var4 = var6;
                            if (var7 > var8) {
                                this.isBeingDragged = true;
                                if (var6 > 0) {
                                    var4 = var6 - var8;
                                } else {
                                    var4 = var6 + var8;
                                }
                            }
                        }

                        if (this.isBeingDragged) {
                            this.lastMotionY = var5;
                            this.scroll(var1, var2, var4, this.getMaxDragOffset(var2), 0);
                        }
                        break label45;
                    }

                    if (var4 != 3) {
                        break label45;
                    }
                }

                this.isBeingDragged = false;
                this.activePointerId = -1;
                var9 = this.velocityTracker;
                if (var9 != null) {
                    var9.recycle();
                    this.velocityTracker = null;
                }
            }
        } else {
            var6 = (int) var3.getX();
            var4 = (int) var3.getY();
            if (!var1.isPointInChildBounds(var2, var6, var4) || !this.canDragView(var2)) {
                return false;
            }

            this.lastMotionY = var4;
            this.activePointerId = var3.getPointerId(0);
            this.ensureVelocityTracker();
        }

        var9 = this.velocityTracker;
        if (var9 != null) {
            var9.addMovement(var3);
        }

        return true;
    }

    public final int scroll(CoordinatorLayout var1, V var2, int var3, int var4, int var5) {
        return this.setHeaderTopBottomOffset(var1, var2, this.getTopBottomOffsetForScrollingSibling() - var3, var4, var5);
    }

    public int setHeaderTopBottomOffset(CoordinatorLayout var1, V var2, int var3) {
        return this.setHeaderTopBottomOffset(var1, var2, var3, -2147483648, 2147483647);
    }

    public abstract int setHeaderTopBottomOffset(CoordinatorLayout var1, V var2, int var3, int var4, int var5);
}
