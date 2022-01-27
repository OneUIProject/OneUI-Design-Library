package de.dlyt.yanndroid.oneui.sesl.viewpager2.widget;

import android.view.View;

import androidx.annotation.NonNull;

import de.dlyt.yanndroid.oneui.sesl.viewpager2.widget.SeslViewPager2.PageTransformer;

import java.util.ArrayList;
import java.util.List;

public final class CompositePageTransformer implements PageTransformer {
    private final List<PageTransformer> mTransformers = new ArrayList<>();

    public void addTransformer(@NonNull PageTransformer transformer) {
        mTransformers.add(transformer);
    }

    public void removeTransformer(@NonNull PageTransformer transformer) {
        mTransformers.remove(transformer);
    }

    @Override
    public void transformPage(@NonNull View page, float position) {
        for (PageTransformer transformer : mTransformers) {
            transformer.transformPage(page, position);
        }
    }
}
