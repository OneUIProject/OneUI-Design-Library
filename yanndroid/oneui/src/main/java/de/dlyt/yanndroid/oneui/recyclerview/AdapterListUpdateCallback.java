package de.dlyt.yanndroid.oneui.recyclerview;

public class AdapterListUpdateCallback implements ListUpdateCallback {
    private final SeslRecyclerView.Adapter mAdapter;

    public AdapterListUpdateCallback(SeslRecyclerView.Adapter adapter) {
        mAdapter = adapter;
    }

    @Override
    public void onInserted(int position, int count) {
        mAdapter.notifyItemRangeInserted(position, count);
    }

    @Override
    public void onRemoved(int position, int count) {
        mAdapter.notifyItemRangeRemoved(position, count);
    }

    @Override
    public void onMoved(int fromPosition, int toPosition) {
        mAdapter.notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public void onChanged(int position, int count, Object payload) {
        mAdapter.notifyItemRangeChanged(position, count, payload);
    }
}
