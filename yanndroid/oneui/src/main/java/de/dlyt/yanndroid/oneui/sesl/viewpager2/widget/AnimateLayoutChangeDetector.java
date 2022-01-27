package de.dlyt.yanndroid.oneui.sesl.viewpager2.widget;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

import static de.dlyt.yanndroid.oneui.sesl.viewpager2.widget.SeslViewPager2.ORIENTATION_HORIZONTAL;

import android.animation.LayoutTransition;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import de.dlyt.yanndroid.oneui.sesl.recyclerview.LinearLayoutManager;

import java.util.Arrays;
import java.util.Comparator;

final class AnimateLayoutChangeDetector {
    private static final ViewGroup.MarginLayoutParams ZERO_MARGIN_LAYOUT_PARAMS;
    private LinearLayoutManager mLayoutManager;

    static {
        ZERO_MARGIN_LAYOUT_PARAMS = new ViewGroup.MarginLayoutParams(MATCH_PARENT, MATCH_PARENT);
        ZERO_MARGIN_LAYOUT_PARAMS.setMargins(0, 0, 0, 0);
    }

    AnimateLayoutChangeDetector(@NonNull LinearLayoutManager llm) {
        mLayoutManager = llm;
    }

    boolean mayHaveInterferingAnimations() {
        return (!arePagesLaidOutContiguously() || mLayoutManager.getChildCount() <= 1) && hasRunningChangingLayoutTransition();
    }

    private boolean arePagesLaidOutContiguously() {
        int childCount = mLayoutManager.getChildCount();
        if (childCount == 0) {
            return true;
        }

        boolean isHorizontal = mLayoutManager.getOrientation() == ORIENTATION_HORIZONTAL;
        int[][] bounds = new int[childCount][2];
        for (int i = 0; i < childCount; i++) {
            View view = mLayoutManager.getChildAt(i);
            if (view == null) {
                throw new IllegalStateException("null view contained in the view hierarchy");
            }
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            ViewGroup.MarginLayoutParams margin;
            if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
                margin = (ViewGroup.MarginLayoutParams) layoutParams;
            } else {
                margin = ZERO_MARGIN_LAYOUT_PARAMS;
            }
            bounds[i][0] = isHorizontal ? view.getLeft() - margin.leftMargin : view.getTop() - margin.topMargin;
            bounds[i][1] = isHorizontal ? view.getRight() + margin.rightMargin : view.getBottom() + margin.bottomMargin;
        }

        Arrays.sort(bounds, new Comparator<int[]>() {
            @Override
            public int compare(int[] lhs, int[] rhs) {
                return lhs[0] - rhs[0];
            }
        });

        for (int i = 1; i < childCount; i++) {
            if (bounds[i - 1][1] != bounds[i][0]) {
                return false;
            }
        }

        int pageSize = bounds[0][1] - bounds[0][0];
        if (bounds[0][0] > 0 || bounds[childCount - 1][1] < pageSize) {
            return false;
        }
        return true;
    }

    private boolean hasRunningChangingLayoutTransition() {
        int childCount = mLayoutManager.getChildCount();
        for (int i = 0; i < childCount; i++) {
            if (hasRunningChangingLayoutTransition(mLayoutManager.getChildAt(i))) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasRunningChangingLayoutTransition(View view) {
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            LayoutTransition layoutTransition = viewGroup.getLayoutTransition();
            if (layoutTransition != null && layoutTransition.isChangingLayout()) {
                return true;
            }
            int childCount = viewGroup.getChildCount();
            for (int i = 0; i < childCount; i++) {
                if (hasRunningChangingLayoutTransition(viewGroup.getChildAt(i))) {
                    return true;
                }
            }
        }
        return false;
    }
}
