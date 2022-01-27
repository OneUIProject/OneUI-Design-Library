package de.dlyt.yanndroid.oneui.sesl.viewpager2.widget;

import androidx.annotation.NonNull;
import androidx.annotation.Px;

import de.dlyt.yanndroid.oneui.sesl.viewpager2.widget.SeslViewPager2.OnPageChangeCallback;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

final class CompositeOnPageChangeCallback extends OnPageChangeCallback {
    @NonNull
    private final List<OnPageChangeCallback> mCallbacks;

    CompositeOnPageChangeCallback(int initialCapacity) {
        mCallbacks = new ArrayList<>(initialCapacity);
    }

    void addOnPageChangeCallback(OnPageChangeCallback callback) {
        mCallbacks.add(callback);
    }

    void removeOnPageChangeCallback(OnPageChangeCallback callback) {
        mCallbacks.remove(callback);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, @Px int positionOffsetPixels) {
        try {
            for (OnPageChangeCallback callback : mCallbacks) {
                callback.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }
        } catch (ConcurrentModificationException ex) {
            throwCallbackListModifiedWhileInUse(ex);
        }
    }

    @Override
    public void onPageSelected(int position) {
        try {
            for (OnPageChangeCallback callback : mCallbacks) {
                callback.onPageSelected(position);
            }
        } catch (ConcurrentModificationException ex) {
            throwCallbackListModifiedWhileInUse(ex);
        }
    }

    @Override
    public void onPageScrollStateChanged(@SeslViewPager2.ScrollState int state) {
        try {
            for (OnPageChangeCallback callback : mCallbacks) {
                callback.onPageScrollStateChanged(state);
            }
        } catch (ConcurrentModificationException ex) {
            throwCallbackListModifiedWhileInUse(ex);
        }
    }

    private void throwCallbackListModifiedWhileInUse(ConcurrentModificationException parent) {
        throw new IllegalStateException("Adding and removing callbacks during dispatch to callbacks is not supported", parent);
    }
}
