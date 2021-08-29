package de.dlyt.yanndroid.oneui.sesl.recyclerview;

import android.os.Bundle;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;

import androidx.core.view.AccessibilityDelegateCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;

import de.dlyt.yanndroid.oneui.view.RecyclerView;

public class SeslRecyclerViewAccessibilityDelegate extends AccessibilityDelegateCompat {
    final RecyclerView mRecyclerView;
    final AccessibilityDelegateCompat mItemDelegate;


    public SeslRecyclerViewAccessibilityDelegate(RecyclerView recyclerView) {
        mRecyclerView = recyclerView;
        mItemDelegate = new ItemDelegate(this);
    }

    boolean shouldIgnore() {
        return mRecyclerView.hasPendingAdapterUpdates();
    }

    @Override
    public boolean performAccessibilityAction(View host, int action, Bundle args) {
        if (super.performAccessibilityAction(host, action, args)) {
            return true;
        }
        if (!shouldIgnore() && mRecyclerView.getLayoutManager() != null) {
            return mRecyclerView.getLayoutManager().performAccessibilityAction(action, args);
        }

        return false;
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfoCompat info) {
        super.onInitializeAccessibilityNodeInfo(host, info);
        info.setClassName(RecyclerView.class.getName());
        if (!shouldIgnore() && mRecyclerView.getLayoutManager() != null) {
            mRecyclerView.getLayoutManager().onInitializeAccessibilityNodeInfo(info);
        }
    }

    @Override
    public void onInitializeAccessibilityEvent(View host, AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(host, event);
        event.setClassName(RecyclerView.class.getName());
        if (host instanceof RecyclerView && !shouldIgnore()) {
            RecyclerView rv = (RecyclerView) host;
            if (rv.getLayoutManager() != null) {
                rv.getLayoutManager().onInitializeAccessibilityEvent(event);
            }
        }
    }

    public AccessibilityDelegateCompat getItemDelegate() {
        return mItemDelegate;
    }

    public static class ItemDelegate extends AccessibilityDelegateCompat {
        final SeslRecyclerViewAccessibilityDelegate mRecyclerViewDelegate;

        public ItemDelegate(SeslRecyclerViewAccessibilityDelegate recyclerViewDelegate) {
            mRecyclerViewDelegate = recyclerViewDelegate;
        }

        @Override
        public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfoCompat info) {
            super.onInitializeAccessibilityNodeInfo(host, info);
            if (!mRecyclerViewDelegate.shouldIgnore() && mRecyclerViewDelegate.mRecyclerView.getLayoutManager() != null) {
                mRecyclerViewDelegate.mRecyclerView.getLayoutManager().onInitializeAccessibilityNodeInfoForItem(host, info);
            }
        }

        @Override
        public boolean performAccessibilityAction(View host, int action, Bundle args) {
            if (super.performAccessibilityAction(host, action, args)) {
                return true;
            }
            if (!mRecyclerViewDelegate.shouldIgnore() && mRecyclerViewDelegate.mRecyclerView.getLayoutManager() != null) {
                return mRecyclerViewDelegate.mRecyclerView.getLayoutManager().performAccessibilityActionForItem(host, action, args);
            }
            return false;
        }
    }
}

