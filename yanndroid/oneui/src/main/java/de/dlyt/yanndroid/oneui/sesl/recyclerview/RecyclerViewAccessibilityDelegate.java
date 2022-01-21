package de.dlyt.yanndroid.oneui.sesl.recyclerview;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.AccessibilityDelegateCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.core.view.accessibility.AccessibilityNodeProviderCompat;

import java.util.Map;
import java.util.WeakHashMap;

import de.dlyt.yanndroid.oneui.view.RecyclerView;

public class RecyclerViewAccessibilityDelegate extends AccessibilityDelegateCompat {
    final RecyclerView mRecyclerView;
    private final ItemDelegate mItemDelegate;

    public RecyclerViewAccessibilityDelegate(@NonNull RecyclerView recyclerView) {
        mRecyclerView = recyclerView;
        AccessibilityDelegateCompat itemDelegate = getItemDelegate();
        if (itemDelegate != null && itemDelegate instanceof ItemDelegate) {
            mItemDelegate = (ItemDelegate) itemDelegate;
        } else {
            mItemDelegate = new ItemDelegate(this);
        }
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
        if (!shouldIgnore() && mRecyclerView.getLayoutManager() != null) {
            mRecyclerView.getLayoutManager().onInitializeAccessibilityNodeInfo(info);
        }
    }

    @Override
    public void onInitializeAccessibilityEvent(View host, AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(host, event);
        if (host instanceof RecyclerView && !shouldIgnore()) {
            RecyclerView rv = (RecyclerView) host;
            if (rv.getLayoutManager() != null) {
                rv.getLayoutManager().onInitializeAccessibilityEvent(event);
            }
        }
    }

    @NonNull
    public AccessibilityDelegateCompat getItemDelegate() {
        return mItemDelegate;
    }

    public static class ItemDelegate extends AccessibilityDelegateCompat {
        final RecyclerViewAccessibilityDelegate mRecyclerViewDelegate;
        private Map<View, AccessibilityDelegateCompat> mOriginalItemDelegates = new WeakHashMap<>();

        public ItemDelegate(@NonNull RecyclerViewAccessibilityDelegate recyclerViewDelegate) {
            mRecyclerViewDelegate = recyclerViewDelegate;
        }

        public void saveOriginalDelegate(View itemView) {
            AccessibilityDelegateCompat delegate = ViewCompat.getAccessibilityDelegate(itemView);
            if (delegate != null && delegate != this) {
                mOriginalItemDelegates.put(itemView, delegate);
            }
        }

        public AccessibilityDelegateCompat getAndRemoveOriginalDelegateForItem(View itemView) {
            return mOriginalItemDelegates.remove(itemView);
        }

        @Override
        public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfoCompat info) {
            if (!mRecyclerViewDelegate.shouldIgnore() && mRecyclerViewDelegate.mRecyclerView.getLayoutManager() != null) {
                mRecyclerViewDelegate.mRecyclerView.getLayoutManager().onInitializeAccessibilityNodeInfoForItem(host, info);
                AccessibilityDelegateCompat originalDelegate = mOriginalItemDelegates.get(host);
                if (originalDelegate != null) {
                    originalDelegate.onInitializeAccessibilityNodeInfo(host, info);
                } else {
                    super.onInitializeAccessibilityNodeInfo(host, info);
                }
            } else {
                super.onInitializeAccessibilityNodeInfo(host, info);
            }
        }

        @Override
        public boolean performAccessibilityAction(View host, int action, Bundle args) {
            if (!mRecyclerViewDelegate.shouldIgnore() && mRecyclerViewDelegate.mRecyclerView.getLayoutManager() != null) {
                AccessibilityDelegateCompat originalDelegate = mOriginalItemDelegates.get(host);
                if (originalDelegate != null) {
                    if (originalDelegate.performAccessibilityAction(host, action, args)) {
                        return true;
                    }
                } else if (super.performAccessibilityAction(host, action, args)) {
                    return true;
                }
                return mRecyclerViewDelegate.mRecyclerView.getLayoutManager().performAccessibilityActionForItem(host, action, args);
            } else {
                return super.performAccessibilityAction(host, action, args);
            }
        }

        @Override
        public void sendAccessibilityEvent(@NonNull View host, int eventType) {
            AccessibilityDelegateCompat originalDelegate = mOriginalItemDelegates.get(host);
            if (originalDelegate != null) {
                originalDelegate.sendAccessibilityEvent(host, eventType);
            } else {
                super.sendAccessibilityEvent(host, eventType);
            }
        }

        @Override
        public void sendAccessibilityEventUnchecked(@NonNull View host, @NonNull AccessibilityEvent event) {
            AccessibilityDelegateCompat originalDelegate = mOriginalItemDelegates.get(host);
            if (originalDelegate != null) {
                originalDelegate.sendAccessibilityEventUnchecked(host, event);
            } else {
                super.sendAccessibilityEventUnchecked(host, event);
            }
        }

        @Override
        public boolean dispatchPopulateAccessibilityEvent(@NonNull View host, @NonNull AccessibilityEvent event) {
            AccessibilityDelegateCompat originalDelegate = mOriginalItemDelegates.get(host);
            if (originalDelegate != null) {
                return originalDelegate.dispatchPopulateAccessibilityEvent(host, event);
            } else {
                return super.dispatchPopulateAccessibilityEvent(host, event);
            }
        }

        @Override
        public void onPopulateAccessibilityEvent(@NonNull View host, @NonNull AccessibilityEvent event) {
            AccessibilityDelegateCompat originalDelegate = mOriginalItemDelegates.get(host);
            if (originalDelegate != null) {
                originalDelegate.onPopulateAccessibilityEvent(host, event);
            } else {
                super.onPopulateAccessibilityEvent(host, event);
            }
        }

        @Override
        public void onInitializeAccessibilityEvent(@NonNull View host, @NonNull AccessibilityEvent event) {
            AccessibilityDelegateCompat originalDelegate = mOriginalItemDelegates.get(host);
            if (originalDelegate != null) {
                originalDelegate.onInitializeAccessibilityEvent(host, event);
            } else {
                super.onInitializeAccessibilityEvent(host, event);
            }
        }

        @Override
        public boolean onRequestSendAccessibilityEvent(@NonNull ViewGroup host, @NonNull View child, @NonNull AccessibilityEvent event) {
            AccessibilityDelegateCompat originalDelegate = mOriginalItemDelegates.get(host);
            if (originalDelegate != null) {
                return originalDelegate.onRequestSendAccessibilityEvent(host, child, event);
            } else {
                return super.onRequestSendAccessibilityEvent(host, child, event);
            }
        }

        @Override
        @Nullable
        public AccessibilityNodeProviderCompat getAccessibilityNodeProvider(@NonNull View host) {
            AccessibilityDelegateCompat originalDelegate = mOriginalItemDelegates.get(host);
            if (originalDelegate != null) {
                return originalDelegate.getAccessibilityNodeProvider(host);
            } else {
                return super.getAccessibilityNodeProvider(host);
            }
        }
    }
}

