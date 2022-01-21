package de.dlyt.yanndroid.oneui.preference;

import android.os.Bundle;
import android.view.View;

import androidx.core.view.AccessibilityDelegateCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;

import de.dlyt.yanndroid.oneui.sesl.recyclerview.RecyclerViewAccessibilityDelegate;
import de.dlyt.yanndroid.oneui.view.RecyclerView;

public class PreferenceRecyclerViewAccessibilityDelegate extends RecyclerViewAccessibilityDelegate {
    final AccessibilityDelegateCompat mDefaultItemDelegate = super.getItemDelegate();
    final RecyclerView mRecyclerView;
    final AccessibilityDelegateCompat mItemDelegate = new AccessibilityDelegateCompat() {
        @Override
        public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfoCompat info) {
            mDefaultItemDelegate.onInitializeAccessibilityNodeInfo(host, info);
        }

        @Override
        public boolean performAccessibilityAction(View host, int action, Bundle args) {
            return mDefaultItemDelegate.performAccessibilityAction(host, action, args);
        }
    };

    public PreferenceRecyclerViewAccessibilityDelegate(RecyclerView recyclerView) {
        super(recyclerView);
        mRecyclerView = recyclerView;
    }

    @Override
    public AccessibilityDelegateCompat getItemDelegate() {
        return mItemDelegate;
    }
}
