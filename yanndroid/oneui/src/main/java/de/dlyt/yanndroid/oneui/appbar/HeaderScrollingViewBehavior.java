package de.dlyt.yanndroid.oneui.appbar;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import java.util.List;

import androidx.core.math.MathUtils;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import de.dlyt.yanndroid.oneui.coordinatorlayout.SamsungCoordinatorLayout;

public abstract class HeaderScrollingViewBehavior extends ViewOffsetBehavior<View> {
    public int overlayTop;
    public final Rect tempRect1 = new Rect();
    public final Rect tempRect2 = new Rect();
    public int verticalLayoutGap = 0;

    public HeaderScrollingViewBehavior() {
    }

    public HeaderScrollingViewBehavior(Context var1, AttributeSet var2) {
        super(var1, var2);
    }

    public static int resolveGravity(int var0) {
        int var1 = var0;
        if (var0 == 0) {
            var1 = 8388659;
        }

        return var1;
    }

    public abstract View findFirstDependency(List<View> var1);

    public final int getOverlapPixelsForOffset(View var1) {
        int var2 = this.overlayTop;
        int var3 = 0;
        if (var2 != 0) {
            float var4 = this.getOverlapRatioForOffset(var1);
            var3 = this.overlayTop;
            var3 = MathUtils.clamp((int)(var4 * (float)var3), 0, var3);
        }

        return var3;
    }

    public abstract float getOverlapRatioForOffset(View var1);

    public final int getOverlayTop() {
        return this.overlayTop;
    }

    public int getScrollRange(View var1) {
        return var1.getMeasuredHeight();
    }

    public final int getVerticalLayoutGap() {
        return this.verticalLayoutGap;
    }

    public void layoutChild(SamsungCoordinatorLayout var1, View var2, int var3) {
        View var4 = this.findFirstDependency(var1.getDependencies(var2));
        if (var4 != null) {
            SamsungCoordinatorLayout.LayoutParams var5 = (SamsungCoordinatorLayout.LayoutParams)var2.getLayoutParams();
            Rect var6 = this.tempRect1;
            var6.set(var1.getPaddingLeft() + var5.leftMargin, var4.getBottom() + var5.topMargin, var1.getWidth() - var1.getPaddingRight() - var5.rightMargin, var1.getHeight() + var4.getBottom() - var1.getPaddingBottom() - var5.bottomMargin);
            WindowInsetsCompat var7 = var1.getLastWindowInsets();
            if (var7 != null && ViewCompat.getFitsSystemWindows(var1) && !ViewCompat.getFitsSystemWindows(var2)) {
                var6.left += var7.getSystemWindowInsetLeft();
                var6.right -= var7.getSystemWindowInsetRight();
            }

            Rect var8 = this.tempRect2;
            GravityCompat.apply(resolveGravity(var5.gravity), var2.getMeasuredWidth(), var2.getMeasuredHeight(), var6, var8, var3);
            var3 = this.getOverlapPixelsForOffset(var4);
            var2.layout(var8.left, var8.top - var3, var8.right, var8.bottom - var3);
            this.verticalLayoutGap = var8.top - var4.getBottom();
        } else {
            super.layoutChild(var1, var2, var3);
            this.verticalLayoutGap = 0;
        }

    }

    public boolean onMeasureChild(SamsungCoordinatorLayout var1, View var2, int var3, int var4, int var5, int var6) {
        int var7 = var2.getLayoutParams().height;
        byte var8 = 0;
        if (var7 == -1 || var7 == -2) {
            View var9 = this.findFirstDependency(var1.getDependencies(var2));
            if (var9 != null) {
                if (ViewCompat.getFitsSystemWindows(var9) && !ViewCompat.getFitsSystemWindows(var2)) {
                    ViewCompat.setFitsSystemWindows(var2, true);
                    if (ViewCompat.getFitsSystemWindows(var2)) {
                        var2.requestLayout();
                        return true;
                    }
                }

                int var10 = View.MeasureSpec.getSize(var5);
                var5 = var10;
                if (var10 == 0) {
                    var5 = var1.getHeight();
                }

                var5 += this.getScrollRange(var9);
                var10 = var9.getMeasuredHeight();
                if (this.shouldHeaderOverlapScrollingChild()) {
                    var2.setTranslationY((float)(-var10));
                } else {
                    var5 -= var10;
                }

                if (var5 < 0) {
                    var5 = var8;
                }

                int var11;
                if (var7 == -1) {
                    var11 = 1073741824;
                } else {
                    var11 = -2147483648;
                }

                var1.onMeasureChild(var2, var3, var4, View.MeasureSpec.makeMeasureSpec(var5, var11), var6);
                return true;
            }
        }

        return false;
    }

    public final void setOverlayTop(int var1) {
        this.overlayTop = var1;
    }

    public boolean shouldHeaderOverlapScrollingChild() {
        return false;
    }
}
