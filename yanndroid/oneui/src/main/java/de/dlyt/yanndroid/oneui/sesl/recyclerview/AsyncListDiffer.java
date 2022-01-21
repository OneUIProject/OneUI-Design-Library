package de.dlyt.yanndroid.oneui.sesl.recyclerview;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;

import de.dlyt.yanndroid.oneui.view.RecyclerView;

public class AsyncListDiffer<T> {
    private static final Executor sMainThreadExecutor = new MainThreadExecutor();
    private final ListUpdateCallback mUpdateCallback;
    @SuppressWarnings("WeakerAccess")
    final AsyncDifferConfig<T> mConfig;
    Executor mMainThreadExecutor;
    @Nullable
    private List<T> mList;
    private final List<ListListener<T>> mListeners = new CopyOnWriteArrayList<>();
    @NonNull
    private List<T> mReadOnlyList = Collections.emptyList();
    @SuppressWarnings("WeakerAccess")
    int mMaxScheduledGeneration;

    public AsyncListDiffer(@NonNull RecyclerView.Adapter adapter, @NonNull DiffUtil.ItemCallback<T> diffCallback) {
        this(new AdapterListUpdateCallback(adapter), new AsyncDifferConfig.Builder<>(diffCallback).build());
    }

    @SuppressWarnings("WeakerAccess")
    public AsyncListDiffer(@NonNull ListUpdateCallback listUpdateCallback, @NonNull AsyncDifferConfig<T> config) {
        mUpdateCallback = listUpdateCallback;
        mConfig = config;
        if (config.getMainThreadExecutor() != null) {
            mMainThreadExecutor = config.getMainThreadExecutor();
        } else {
            mMainThreadExecutor = sMainThreadExecutor;
        }
    }

    @NonNull
    public List<T> getCurrentList() {
        return mReadOnlyList;
    }

    @SuppressWarnings("WeakerAccess")
    public void submitList(@Nullable final List<T> newList) {
        submitList(newList, null);
    }

    @SuppressWarnings("WeakerAccess")
    public void submitList(@Nullable final List<T> newList, @Nullable final Runnable commitCallback) {
        final int runGeneration = ++mMaxScheduledGeneration;

        if (newList == mList) {
            if (commitCallback != null) {
                commitCallback.run();
            }
            return;
        }

        final List<T> previousList = mReadOnlyList;

        if (newList == null) {
            int countRemoved = mList.size();
            mList = null;
            mReadOnlyList = Collections.emptyList();
            mUpdateCallback.onRemoved(0, countRemoved);
            onCurrentListChanged(previousList, commitCallback);
            return;
        }

        if (mList == null) {
            mList = newList;
            mReadOnlyList = Collections.unmodifiableList(newList);
            mUpdateCallback.onInserted(0, newList.size());
            onCurrentListChanged(previousList, commitCallback);
            return;
        }

        final List<T> oldList = mList;
        mConfig.getBackgroundThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                final DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                    @Override
                    public int getOldListSize() {
                        return oldList.size();
                    }

                    @Override
                    public int getNewListSize() {
                        return newList.size();
                    }

                    @Override
                    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                        T oldItem = oldList.get(oldItemPosition);
                        T newItem = newList.get(newItemPosition);
                        if (oldItem != null && newItem != null) {
                            return mConfig.getDiffCallback().areItemsTheSame(oldItem, newItem);
                        }
                        return oldItem == null && newItem == null;
                    }

                    @Override
                    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                        T oldItem = oldList.get(oldItemPosition);
                        T newItem = newList.get(newItemPosition);
                        if (oldItem != null && newItem != null) {
                            return mConfig.getDiffCallback().areContentsTheSame(oldItem, newItem);
                        }
                        if (oldItem == null && newItem == null) {
                            return true;
                        }
                        throw new AssertionError();
                    }

                    @Nullable
                    @Override
                    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
                        T oldItem = oldList.get(oldItemPosition);
                        T newItem = newList.get(newItemPosition);
                        if (oldItem != null && newItem != null) {
                            return mConfig.getDiffCallback().getChangePayload(oldItem, newItem);
                        }
                        throw new AssertionError();
                    }
                });

                mMainThreadExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        if (mMaxScheduledGeneration == runGeneration) {
                            latchList(newList, result, commitCallback);
                        }
                    }
                });
            }
        });
    }

    @SuppressWarnings("WeakerAccess")
    void latchList(@NonNull List<T> newList, @NonNull DiffUtil.DiffResult diffResult, @Nullable Runnable commitCallback) {
        final List<T> previousList = mReadOnlyList;
        mList = newList;
        mReadOnlyList = Collections.unmodifiableList(newList);
        diffResult.dispatchUpdatesTo(mUpdateCallback);
        onCurrentListChanged(previousList, commitCallback);
    }

    private void onCurrentListChanged(@NonNull List<T> previousList, @Nullable Runnable commitCallback) {
        for (ListListener<T> listener : mListeners) {
            listener.onCurrentListChanged(previousList, mReadOnlyList);
        }
        if (commitCallback != null) {
            commitCallback.run();
        }
    }

    public void addListListener(@NonNull ListListener<T> listener) {
        mListeners.add(listener);
    }

    public void removeListListener(@NonNull ListListener<T> listener) {
        mListeners.remove(listener);
    }


    private static class MainThreadExecutor implements Executor {
        final Handler mHandler = new Handler(Looper.getMainLooper());

        MainThreadExecutor() {
        }

        @Override
        public void execute(@NonNull Runnable command) {
            mHandler.post(command);
        }
    }

    public interface ListListener<T> {
        void onCurrentListChanged(@NonNull List<T> previousList, @NonNull List<T> currentList);
    }
}
