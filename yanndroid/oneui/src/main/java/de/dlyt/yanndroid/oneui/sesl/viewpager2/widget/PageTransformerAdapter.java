package de.dlyt.yanndroid.oneui.sesl.viewpager2.widget;

import android.view.View;

import androidx.annotation.Nullable;

import de.dlyt.yanndroid.oneui.sesl.recyclerview.LinearLayoutManager;
import de.dlyt.yanndroid.oneui.sesl.viewpager2.widget.SeslViewPager2.OnPageChangeCallback;
import de.dlyt.yanndroid.oneui.sesl.viewpager2.widget.SeslViewPager2.PageTransformer;

import java.util.Locale;

final class PageTransformerAdapter extends OnPageChangeCallback {
    private final LinearLayoutManager mLayoutManager;
    private PageTransformer mPageTransformer;

    PageTransformerAdapter(LinearLayoutManager layoutManager) {
        mLayoutManager = layoutManager;
    }

    PageTransformer getPageTransformer() {
        return mPageTransformer;
    }

    void setPageTransformer(@Nullable PageTransformer transformer) {
        mPageTransformer = transformer;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (mPageTransformer == null) {
            return;
        }

        float transformOffset = -positionOffset;
        for (int i = 0; i < mLayoutManager.getChildCount(); i++) {
            View view = mLayoutManager.getChildAt(i);
            if (view == null) {
                throw new IllegalStateException(String.format(Locale.US, "LayoutManager returned a null child at pos %d/%d while transforming pages", i, mLayoutManager.getChildCount()));
            }
            int currPos = mLayoutManager.getPosition(view);
            float viewOffset = transformOffset + (currPos - position);
            mPageTransformer.transformPage(view, viewOffset);
        }
    }

    @Override
    public void onPageSelected(int position) {
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }
}
