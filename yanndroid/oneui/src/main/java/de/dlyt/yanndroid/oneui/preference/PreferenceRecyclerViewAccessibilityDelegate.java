package de.dlyt.yanndroid.oneui.preference;

import android.os.Bundle;
import android.view.View;

import androidx.core.view.AccessibilityDelegateCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;

import de.dlyt.yanndroid.oneui.recyclerview.SeslRecyclerView;
import de.dlyt.yanndroid.oneui.recyclerview.SeslRecyclerViewAccessibilityDelegate;

public class PreferenceRecyclerViewAccessibilityDelegate extends SeslRecyclerViewAccessibilityDelegate {
    final AccessibilityDelegateCompat mDefaultItemDelegate = super.getItemDelegate();
    final SeslRecyclerView mRecyclerView;

    public PreferenceRecyclerViewAccessibilityDelegate(SeslRecyclerView recyclerView) {
        super(recyclerView);
        mRecyclerView = recyclerView;
    }

    @Override
    public AccessibilityDelegateCompat getItemDelegate() {
        return mItemDelegate;
    }

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
}
