package de.dlyt.yanndroid.oneui.sesl.appbar;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.math.MathUtils;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.List;

import de.dlyt.yanndroid.oneui.sesl.coordinatorlayout.SamsungCoordinatorLayout;

abstract class HeaderScrollingViewBehavior extends ViewOffsetBehavior<View> {
    final Rect tempRect1 = new Rect();
    final Rect tempRect2 = new Rect();
    private int verticalLayoutGap = 0;
    private int overlayTop;

    public HeaderScrollingViewBehavior() {}

    public HeaderScrollingViewBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onMeasureChild(@NonNull SamsungCoordinatorLayout parent, @NonNull View child, int parentWidthMeasureSpec, int widthUsed, int parentHeightMeasureSpec, int heightUsed) {
        final int childLpHeight = child.getLayoutParams().height;
        if (childLpHeight == ViewGroup.LayoutParams.MATCH_PARENT || childLpHeight == ViewGroup.LayoutParams.WRAP_CONTENT) {
            final List<View> dependencies = parent.getDependencies(child);
            final View header = findFirstDependency(dependencies);
            if (header != null) {
                int availableHeight = View.MeasureSpec.getSize(parentHeightMeasureSpec);
                if (availableHeight > 0) {
                    if (ViewCompat.getFitsSystemWindows(header)) {
                        final WindowInsetsCompat parentInsets = parent.getLastWindowInsets();
                        if (parentInsets != null) {
                            availableHeight += parentInsets.getSystemWindowInsetTop() + parentInsets.getSystemWindowInsetBottom();
                        }
                    }
                } else {
                    availableHeight = parent.getHeight();
                }

                int height = availableHeight + getScrollRange(header);
                int headerHeight = header.getMeasuredHeight();
                if (shouldHeaderOverlapScrollingChild()) {
                    child.setTranslationY(-headerHeight);
                } else {
                    height -= headerHeight;
                }
                final int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(height, childLpHeight == ViewGroup.LayoutParams.MATCH_PARENT ? View.MeasureSpec.EXACTLY : View.MeasureSpec.AT_MOST);

                parent.onMeasureChild(child, parentWidthMeasureSpec, widthUsed, heightMeasureSpec, heightUsed);

                return true;
            }
        }
        return false;
    }

    @Override
    protected void layoutChild(@NonNull final SamsungCoordinatorLayout parent, @NonNull final View child, final int layoutDirection) {
        final List<View> dependencies = parent.getDependencies(child);
        final View header = findFirstDependency(dependencies);

        if (header != null) {
            final SamsungCoordinatorLayout.LayoutParams lp = (SamsungCoordinatorLayout.LayoutParams) child.getLayoutParams();
            final Rect available = tempRect1;
            available.set(parent.getPaddingLeft() + lp.leftMargin, header.getBottom() + lp.topMargin, parent.getWidth() - parent.getPaddingRight() - lp.rightMargin, parent.getHeight() + header.getBottom() - parent.getPaddingBottom() - lp.bottomMargin);

            final WindowInsetsCompat parentInsets = parent.getLastWindowInsets();
            if (parentInsets != null && ViewCompat.getFitsSystemWindows(parent) && !ViewCompat.getFitsSystemWindows(child)) {
                available.left += parentInsets.getSystemWindowInsetLeft();
                available.right -= parentInsets.getSystemWindowInsetRight();
            }

            final Rect out = tempRect2;
            GravityCompat.apply(resolveGravity(lp.gravity), child.getMeasuredWidth(), child.getMeasuredHeight(), available, out, layoutDirection);

            final int overlap = getOverlapPixelsForOffset(header);

            child.layout(out.left, out.top - overlap, out.right, out.bottom - overlap);
            verticalLayoutGap = out.top - header.getBottom();
        } else {
            super.layoutChild(parent, child, layoutDirection);
            verticalLayoutGap = 0;
        }
    }

    protected boolean shouldHeaderOverlapScrollingChild() {
        return false;
    }

    float getOverlapRatioForOffset(final View header) {
        return 1f;
    }

    final int getOverlapPixelsForOffset(final View header) {
        return overlayTop == 0 ? 0 : MathUtils.clamp((int) (getOverlapRatioForOffset(header) * overlayTop), 0, overlayTop);
    }

    private static int resolveGravity(int gravity) {
        return gravity == Gravity.NO_GRAVITY ? GravityCompat.START | Gravity.TOP : gravity;
    }

    @Nullable
    abstract View findFirstDependency(List<View> views);

    int getScrollRange(@NonNull View v) {
        return v.getMeasuredHeight();
    }

    final int getVerticalLayoutGap() {
        return verticalLayoutGap;
    }

    public final void setOverlayTop(int overlayTop) {
        this.overlayTop = overlayTop;
    }

    public final int getOverlayTop() {
        return overlayTop;
    }
}
