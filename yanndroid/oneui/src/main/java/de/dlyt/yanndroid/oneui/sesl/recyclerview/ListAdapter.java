package de.dlyt.yanndroid.oneui.sesl.recyclerview;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import de.dlyt.yanndroid.oneui.view.RecyclerView;

public abstract class ListAdapter<T, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {
    final AsyncListDiffer<T> mDiffer;
    private final AsyncListDiffer.ListListener<T> mListener = new AsyncListDiffer.ListListener<T>() {
        @Override
        public void onCurrentListChanged(@NonNull List<T> previousList, @NonNull List<T> currentList) {
            ListAdapter.this.onCurrentListChanged(previousList, currentList);
        }
    };

    @SuppressWarnings("unused")
    protected ListAdapter(@NonNull DiffUtil.ItemCallback<T> diffCallback) {
        mDiffer = new AsyncListDiffer<>(new AdapterListUpdateCallback(this), new AsyncDifferConfig.Builder<>(diffCallback).build());
        mDiffer.addListListener(mListener);
    }

    @SuppressWarnings("unused")
    protected ListAdapter(@NonNull AsyncDifferConfig<T> config) {
        mDiffer = new AsyncListDiffer<>(new AdapterListUpdateCallback(this), config);
        mDiffer.addListListener(mListener);
    }

    public void submitList(@Nullable List<T> list) {
        mDiffer.submitList(list);
    }

    public void submitList(@Nullable List<T> list, @Nullable final Runnable commitCallback) {
        mDiffer.submitList(list, commitCallback);
    }

    protected T getItem(int position) {
        return mDiffer.getCurrentList().get(position);
    }

    @Override
    public int getItemCount() {
        return mDiffer.getCurrentList().size();
    }

    @NonNull
    public List<T> getCurrentList() {
        return mDiffer.getCurrentList();
    }

    public void onCurrentListChanged(@NonNull List<T> previousList, @NonNull List<T> currentList) {
    }
}
