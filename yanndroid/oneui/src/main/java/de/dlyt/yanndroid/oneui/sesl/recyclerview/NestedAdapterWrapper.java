package de.dlyt.yanndroid.oneui.sesl.recyclerview;

import static de.dlyt.yanndroid.oneui.view.RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Preconditions;

import de.dlyt.yanndroid.oneui.view.RecyclerView;
import de.dlyt.yanndroid.oneui.view.RecyclerView.Adapter;
import de.dlyt.yanndroid.oneui.view.RecyclerView.ViewHolder;

class NestedAdapterWrapper {
    @NonNull
    private final ViewTypeStorage.ViewTypeLookup mViewTypeLookup;
    @NonNull
    private final StableIdStorage.StableIdLookup mStableIdLookup;
    public final Adapter<ViewHolder> adapter;
    @SuppressWarnings("WeakerAccess")
    final Callback mCallback;
    @SuppressWarnings("WeakerAccess")
    int mCachedItemCount;

    private RecyclerView.AdapterDataObserver mAdapterObserver = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onChanged() {
            mCachedItemCount = adapter.getItemCount();
            mCallback.onChanged(NestedAdapterWrapper.this);
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            mCallback.onItemRangeChanged(NestedAdapterWrapper.this, positionStart, itemCount, null);
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, @Nullable Object payload) {
            mCallback.onItemRangeChanged(NestedAdapterWrapper.this, positionStart, itemCount, payload);
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            mCachedItemCount += itemCount;
            mCallback.onItemRangeInserted(NestedAdapterWrapper.this, positionStart, itemCount);
            if (mCachedItemCount > 0 && adapter.getStateRestorationPolicy() == PREVENT_WHEN_EMPTY) {
                mCallback.onStateRestorationPolicyChanged(NestedAdapterWrapper.this);
            }
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            mCachedItemCount -= itemCount;
            mCallback.onItemRangeRemoved(NestedAdapterWrapper.this, positionStart, itemCount);
            if (mCachedItemCount < 1 && adapter.getStateRestorationPolicy() == PREVENT_WHEN_EMPTY) {
                mCallback.onStateRestorationPolicyChanged(NestedAdapterWrapper.this);
            }
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            Preconditions.checkArgument(itemCount == 1, "moving more than 1 item is not supported in RecyclerView");
            mCallback.onItemRangeMoved(NestedAdapterWrapper.this, fromPosition, toPosition);
        }

        @Override
        public void onStateRestorationPolicyChanged() {
            mCallback.onStateRestorationPolicyChanged(NestedAdapterWrapper.this);
        }
    };

    NestedAdapterWrapper(Adapter<ViewHolder> adapter, final Callback callback, ViewTypeStorage viewTypeStorage, StableIdStorage.StableIdLookup stableIdLookup) {
        this.adapter = adapter;
        mCallback = callback;
        mViewTypeLookup = viewTypeStorage.createViewTypeWrapper(this);
        mStableIdLookup = stableIdLookup;
        mCachedItemCount = this.adapter.getItemCount();
        this.adapter.registerAdapterDataObserver(mAdapterObserver);
    }

    void dispose() {
        adapter.unregisterAdapterDataObserver(mAdapterObserver);
        mViewTypeLookup.dispose();
    }

    int getCachedItemCount() {
        return mCachedItemCount;
    }

    int getItemViewType(int localPosition) {
        return mViewTypeLookup.localToGlobal(adapter.getItemViewType(localPosition));
    }

    ViewHolder onCreateViewHolder(ViewGroup parent, int globalViewType) {
        int localType = mViewTypeLookup.globalToLocal(globalViewType);
        return adapter.onCreateViewHolder(parent, localType);
    }

    void onBindViewHolder(ViewHolder viewHolder, int localPosition) {
        adapter.bindViewHolder(viewHolder, localPosition);
    }

    public long getItemId(int localPosition) {
        long localItemId = adapter.getItemId(localPosition);
        return mStableIdLookup.localToGlobal(localItemId);
    }


    interface Callback {
        void onChanged(@NonNull NestedAdapterWrapper wrapper);

        void onItemRangeChanged(@NonNull NestedAdapterWrapper nestedAdapterWrapper, int positionStart, int itemCount);

        void onItemRangeChanged(@NonNull NestedAdapterWrapper nestedAdapterWrapper, int positionStart, int itemCount, @Nullable Object payload);

        void onItemRangeInserted(@NonNull NestedAdapterWrapper nestedAdapterWrapper, int positionStart, int itemCount);

        void onItemRangeRemoved(@NonNull NestedAdapterWrapper nestedAdapterWrapper, int positionStart, int itemCount);

        void onItemRangeMoved(@NonNull NestedAdapterWrapper nestedAdapterWrapper, int fromPosition, int toPosition);

        void onStateRestorationPolicyChanged(NestedAdapterWrapper nestedAdapterWrapper);
    }

}
