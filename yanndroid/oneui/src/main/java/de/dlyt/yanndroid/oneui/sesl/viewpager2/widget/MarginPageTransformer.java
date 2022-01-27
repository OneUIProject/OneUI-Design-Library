package de.dlyt.yanndroid.oneui.sesl.viewpager2.widget;

import android.annotation.SuppressLint;
import android.view.View;
import android.view.ViewParent;

import androidx.annotation.NonNull;
import androidx.annotation.Px;
import androidx.core.util.Preconditions;

import de.dlyt.yanndroid.oneui.view.RecyclerView;

public final class MarginPageTransformer implements SeslViewPager2.PageTransformer {
    private final int mMarginPx;

    @SuppressLint("RestrictedApi")
    public MarginPageTransformer(@Px int marginPx) {
        Preconditions.checkArgumentNonnegative(marginPx, "Margin must be non-negative");
        mMarginPx = marginPx;
    }

    @Override
    public void transformPage(@NonNull View page, float position) {
        SeslViewPager2 viewPager = requireViewPager(page);

        float offset = mMarginPx * position;

        if (viewPager.getOrientation() == SeslViewPager2.ORIENTATION_HORIZONTAL) {
            page.setTranslationX(viewPager.isRtl() ? -offset : offset);
        } else {
            page.setTranslationY(offset);
        }
    }

    private SeslViewPager2 requireViewPager(@NonNull View page) {
        ViewParent parent = page.getParent();
        ViewParent parentParent = parent.getParent();

        if (parent instanceof RecyclerView && parentParent instanceof SeslViewPager2) {
            return (SeslViewPager2) parentParent;
        }

        throw new IllegalStateException("Expected the page view to be managed by a ViewPager2 instance.");
    }
}
