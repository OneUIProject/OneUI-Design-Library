package de.dlyt.yanndroid.oneui.preference;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.view.AccessibilityDelegateCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;

import de.dlyt.yanndroid.oneui.sesl.recyclerview.RecyclerViewAccessibilityDelegate;
import de.dlyt.yanndroid.oneui.view.RecyclerView;

@Deprecated
public class PreferenceRecyclerViewAccessibilityDelegate extends RecyclerViewAccessibilityDelegate {
    @SuppressWarnings("WeakerAccess")
    final RecyclerView mRecyclerView;
    @SuppressWarnings("WeakerAccess")
    final AccessibilityDelegateCompat mDefaultItemDelegate = super.getItemDelegate();

    @SuppressWarnings("WeakerAccess")
    final AccessibilityDelegateCompat mItemDelegate = new AccessibilityDelegateCompat() {
        @Override
        public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfoCompat info) {
            mDefaultItemDelegate.onInitializeAccessibilityNodeInfo(host, info);
            int position = mRecyclerView.getChildAdapterPosition(host);

            RecyclerView.Adapter adapter = mRecyclerView.getAdapter();
            if (!(adapter instanceof PreferenceGroupAdapter)) {
                return;
            }

            PreferenceGroupAdapter preferenceGroupAdapter = (PreferenceGroupAdapter) adapter;
            Preference preference = preferenceGroupAdapter.getItem(position);
            if (preference == null) {
                return;
            }

            preference.onInitializeAccessibilityNodeInfo(info);
        }

        @Override
        public boolean performAccessibilityAction(View host, int action, Bundle args) {
            return mDefaultItemDelegate.performAccessibilityAction(host, action, args);
        }
    };

    public PreferenceRecyclerViewAccessibilityDelegate(@NonNull RecyclerView recyclerView) {
        super(recyclerView);
        mRecyclerView = recyclerView;
    }

    @NonNull
    @Override
    public AccessibilityDelegateCompat getItemDelegate() {
        return mItemDelegate;
    }
}
