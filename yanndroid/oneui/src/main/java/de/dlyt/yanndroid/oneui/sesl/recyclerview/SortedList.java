package de.dlyt.yanndroid.oneui.sesl.recyclerview;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;

@SuppressWarnings("unchecked")
public class SortedList<T> {
    public static final int INVALID_POSITION = -1;
    private static final int MIN_CAPACITY = 10;
    private static final int CAPACITY_GROWTH = MIN_CAPACITY;
    private static final int INSERTION = 1;
    private static final int DELETION = 1 << 1;
    private static final int LOOKUP = 1 << 2;
    T[] mData;
    private T[] mOldData;
    private int mOldDataStart;
    private int mOldDataSize;
    private int mNewDataStart;
    private Callback mCallback;
    private BatchedCallback mBatchedCallback;
    private int mSize;
    private final Class<T> mTClass;

    public SortedList(@NonNull Class<T> klass, @NonNull Callback<T> callback) {
        this(klass, callback, MIN_CAPACITY);
    }

    public SortedList(@NonNull Class<T> klass, @NonNull Callback<T> callback, int initialCapacity) {
        mTClass = klass;
        mData = (T[]) Array.newInstance(klass, initialCapacity);
        mCallback = callback;
        mSize = 0;
    }

    public int size() {
        return mSize;
    }

    public int add(T item) {
        throwIfInMutationOperation();
        return add(item, true);
    }

    public void addAll(@NonNull T[] items, boolean mayModifyInput) {
        throwIfInMutationOperation();
        if (items.length == 0) {
            return;
        }

        if (mayModifyInput) {
            addAllInternal(items);
        } else {
            addAllInternal(copyArray(items));
        }
    }

    public void addAll(@NonNull T... items) {
        addAll(items, false);
    }

    public void addAll(@NonNull Collection<T> items) {
        T[] copy = (T[]) Array.newInstance(mTClass, items.size());
        addAll(items.toArray(copy), true);
    }

    public void replaceAll(@NonNull T[] items, boolean mayModifyInput) {
        throwIfInMutationOperation();

        if (mayModifyInput) {
            replaceAllInternal(items);
        } else {
            replaceAllInternal(copyArray(items));
        }
    }

    public void replaceAll(@NonNull T... items) {
        replaceAll(items, false);
    }

    public void replaceAll(@NonNull Collection<T> items) {
        T[] copy = (T[]) Array.newInstance(mTClass, items.size());
        replaceAll(items.toArray(copy), true);
    }

    private void addAllInternal(T[] newItems) {
        if (newItems.length < 1) {
            return;
        }

        final int newSize = sortAndDedup(newItems);

        if (mSize == 0) {
            mData = newItems;
            mSize = newSize;
            mCallback.onInserted(0, newSize);
        } else {
            merge(newItems, newSize);
        }
    }

    private void replaceAllInternal(@NonNull T[] newData) {
        final boolean forceBatchedUpdates = !(mCallback instanceof BatchedCallback);
        if (forceBatchedUpdates) {
            beginBatchedUpdates();
        }

        mOldDataStart = 0;
        mOldDataSize = mSize;
        mOldData = mData;

        mNewDataStart = 0;
        int newSize = sortAndDedup(newData);
        mData = (T[]) Array.newInstance(mTClass, newSize);

        while (mNewDataStart < newSize || mOldDataStart < mOldDataSize) {
            if (mOldDataStart >= mOldDataSize) {
                int insertIndex = mNewDataStart;
                int itemCount = newSize - mNewDataStart;
                System.arraycopy(newData, insertIndex, mData, insertIndex, itemCount);
                mNewDataStart += itemCount;
                mSize += itemCount;
                mCallback.onInserted(insertIndex, itemCount);
                break;
            }
            if (mNewDataStart >= newSize) {
                int itemCount = mOldDataSize - mOldDataStart;
                mSize -= itemCount;
                mCallback.onRemoved(mNewDataStart, itemCount);
                break;
            }

            T oldItem = mOldData[mOldDataStart];
            T newItem = newData[mNewDataStart];

            int result = mCallback.compare(oldItem, newItem);
            if (result < 0) {
                replaceAllRemove();
            } else if (result > 0) {
                replaceAllInsert(newItem);
            } else {
                if (!mCallback.areItemsTheSame(oldItem, newItem)) {
                    replaceAllRemove();
                    replaceAllInsert(newItem);
                } else {
                    mData[mNewDataStart] = newItem;
                    mOldDataStart++;
                    mNewDataStart++;
                    if (!mCallback.areContentsTheSame(oldItem, newItem)) {
                        mCallback.onChanged(mNewDataStart - 1, 1, mCallback.getChangePayload(oldItem, newItem));
                    }
                }
            }
        }

        mOldData = null;

        if (forceBatchedUpdates) {
            endBatchedUpdates();
        }
    }

    private void replaceAllInsert(T newItem) {
        mData[mNewDataStart] = newItem;
        mNewDataStart++;
        mSize++;
        mCallback.onInserted(mNewDataStart - 1, 1);
    }

    private void replaceAllRemove() {
        mSize--;
        mOldDataStart++;
        mCallback.onRemoved(mNewDataStart, 1);
    }

    private int sortAndDedup(@NonNull T[] items) {
        if (items.length == 0) {
            return 0;
        }

        Arrays.sort(items, mCallback);

        int rangeStart = 0;
        int rangeEnd = 1;

        for (int i = 1; i < items.length; ++i) {
            T currentItem = items[i];

            int compare = mCallback.compare(items[rangeStart], currentItem);

            if (compare == 0) {
                final int sameItemPos = findSameItem(currentItem, items, rangeStart, rangeEnd);
                if (sameItemPos != INVALID_POSITION) {
                    items[sameItemPos] = currentItem;
                } else {
                    if (rangeEnd != i) {
                        items[rangeEnd] = currentItem;
                    }
                    rangeEnd++;
                }
            } else {
                if (rangeEnd != i) {
                    items[rangeEnd] = currentItem;
                }
                rangeStart = rangeEnd++;
            }
        }
        return rangeEnd;
    }


    private int findSameItem(T item, T[] items, int from, int to) {
        for (int pos = from; pos < to; pos++) {
            if (mCallback.areItemsTheSame(items[pos], item)) {
                return pos;
            }
        }
        return INVALID_POSITION;
    }

    private void merge(T[] newData, int newDataSize) {
        final boolean forceBatchedUpdates = !(mCallback instanceof BatchedCallback);
        if (forceBatchedUpdates) {
            beginBatchedUpdates();
        }

        mOldData = mData;
        mOldDataStart = 0;
        mOldDataSize = mSize;

        final int mergedCapacity = mSize + newDataSize + CAPACITY_GROWTH;
        mData = (T[]) Array.newInstance(mTClass, mergedCapacity);
        mNewDataStart = 0;

        int newDataStart = 0;
        while (mOldDataStart < mOldDataSize || newDataStart < newDataSize) {
            if (mOldDataStart == mOldDataSize) {
                int itemCount = newDataSize - newDataStart;
                System.arraycopy(newData, newDataStart, mData, mNewDataStart, itemCount);
                mNewDataStart += itemCount;
                mSize += itemCount;
                mCallback.onInserted(mNewDataStart - itemCount, itemCount);
                break;
            }

            if (newDataStart == newDataSize) {
                int itemCount = mOldDataSize - mOldDataStart;
                System.arraycopy(mOldData, mOldDataStart, mData, mNewDataStart, itemCount);
                mNewDataStart += itemCount;
                break;
            }

            T oldItem = mOldData[mOldDataStart];
            T newItem = newData[newDataStart];
            int compare = mCallback.compare(oldItem, newItem);
            if (compare > 0) {
                mData[mNewDataStart++] = newItem;
                mSize++;
                newDataStart++;
                mCallback.onInserted(mNewDataStart - 1, 1);
            } else if (compare == 0 && mCallback.areItemsTheSame(oldItem, newItem)) {
                mData[mNewDataStart++] = newItem;
                newDataStart++;
                mOldDataStart++;
                if (!mCallback.areContentsTheSame(oldItem, newItem)) {
                    mCallback.onChanged(mNewDataStart - 1, 1, mCallback.getChangePayload(oldItem, newItem));
                }
            } else {
                mData[mNewDataStart++] = oldItem;
                mOldDataStart++;
            }
        }

        mOldData = null;

        if (forceBatchedUpdates) {
            endBatchedUpdates();
        }
    }

    private void throwIfInMutationOperation() {
        if (mOldData != null) {
            throw new IllegalStateException("Data cannot be mutated in the middle of a batch update operation such as addAll or replaceAll.");
        }
    }

    public void beginBatchedUpdates() {
        throwIfInMutationOperation();
        if (mCallback instanceof BatchedCallback) {
            return;
        }
        if (mBatchedCallback == null) {
            mBatchedCallback = new BatchedCallback(mCallback);
        }
        mCallback = mBatchedCallback;
    }

    public void endBatchedUpdates() {
        throwIfInMutationOperation();
        if (mCallback instanceof BatchedCallback) {
            ((BatchedCallback) mCallback).dispatchLastEvent();
        }
        if (mCallback == mBatchedCallback) {
            mCallback = mBatchedCallback.mWrappedCallback;
        }
    }

    private int add(T item, boolean notify) {
        int index = findIndexOf(item, mData, 0, mSize, INSERTION);
        if (index == INVALID_POSITION) {
            index = 0;
        } else if (index < mSize) {
            T existing = mData[index];
            if (mCallback.areItemsTheSame(existing, item)) {
                if (mCallback.areContentsTheSame(existing, item)) {
                    mData[index] = item;
                    return index;
                } else {
                    mData[index] = item;
                    mCallback.onChanged(index, 1, mCallback.getChangePayload(existing, item));
                    return index;
                }
            }
        }
        addToData(index, item);
        if (notify) {
            mCallback.onInserted(index, 1);
        }
        return index;
    }

    public boolean remove(T item) {
        throwIfInMutationOperation();
        return remove(item, true);
    }

    public T removeItemAt(int index) {
        throwIfInMutationOperation();
        T item = get(index);
        removeItemAtIndex(index, true);
        return item;
    }

    private boolean remove(T item, boolean notify) {
        int index = findIndexOf(item, mData, 0, mSize, DELETION);
        if (index == INVALID_POSITION) {
            return false;
        }
        removeItemAtIndex(index, notify);
        return true;
    }

    private void removeItemAtIndex(int index, boolean notify) {
        System.arraycopy(mData, index + 1, mData, index, mSize - index - 1);
        mSize--;
        mData[mSize] = null;
        if (notify) {
            mCallback.onRemoved(index, 1);
        }
    }

    public void updateItemAt(int index, T item) {
        throwIfInMutationOperation();
        final T existing = get(index);
        boolean contentsChanged = existing == item || !mCallback.areContentsTheSame(existing, item);
        if (existing != item) {
            final int cmp = mCallback.compare(existing, item);
            if (cmp == 0) {
                mData[index] = item;
                if (contentsChanged) {
                    mCallback.onChanged(index, 1, mCallback.getChangePayload(existing, item));
                }
                return;
            }
        }
        if (contentsChanged) {
            mCallback.onChanged(index, 1, mCallback.getChangePayload(existing, item));
        }
        removeItemAtIndex(index, false);
        int newIndex = add(item, false);
        if (index != newIndex) {
            mCallback.onMoved(index, newIndex);
        }
    }

    public void recalculatePositionOfItemAt(int index) {
        throwIfInMutationOperation();
        final T item = get(index);
        removeItemAtIndex(index, false);
        int newIndex = add(item, false);
        if (index != newIndex) {
            mCallback.onMoved(index, newIndex);
        }
    }

    public T get(int index) throws IndexOutOfBoundsException {
        if (index >= mSize || index < 0) {
            throw new IndexOutOfBoundsException("Asked to get item at " + index + " but size is " + mSize);
        }
        if (mOldData != null) {
            if (index >= mNewDataStart) {
                return mOldData[index - mNewDataStart + mOldDataStart];
            }
        }
        return mData[index];
    }

    public int indexOf(T item) {
        if (mOldData != null) {
            int index = findIndexOf(item, mData, 0, mNewDataStart, LOOKUP);
            if (index != INVALID_POSITION) {
                return index;
            }
            index = findIndexOf(item, mOldData, mOldDataStart, mOldDataSize, LOOKUP);
            if (index != INVALID_POSITION) {
                return index - mOldDataStart + mNewDataStart;
            }
            return INVALID_POSITION;
        }
        return findIndexOf(item, mData, 0, mSize, LOOKUP);
    }

    private int findIndexOf(T item, T[] mData, int left, int right, int reason) {
        while (left < right) {
            final int middle = (left + right) / 2;
            T myItem = mData[middle];
            final int cmp = mCallback.compare(myItem, item);
            if (cmp < 0) {
                left = middle + 1;
            } else if (cmp == 0) {
                if (mCallback.areItemsTheSame(myItem, item)) {
                    return middle;
                } else {
                    int exact = linearEqualitySearch(item, middle, left, right);
                    if (reason == INSERTION) {
                        return exact == INVALID_POSITION ? middle : exact;
                    } else {
                        return exact;
                    }
                }
            } else {
                right = middle;
            }
        }
        return reason == INSERTION ? left : INVALID_POSITION;
    }

    private int linearEqualitySearch(T item, int middle, int left, int right) {
        for (int next = middle - 1; next >= left; next--) {
            T nextItem = mData[next];
            int cmp = mCallback.compare(nextItem, item);
            if (cmp != 0) {
                break;
            }
            if (mCallback.areItemsTheSame(nextItem, item)) {
                return next;
            }
        }
        for (int next = middle + 1; next < right; next++) {
            T nextItem = mData[next];
            int cmp = mCallback.compare(nextItem, item);
            if (cmp != 0) {
                break;
            }
            if (mCallback.areItemsTheSame(nextItem, item)) {
                return next;
            }
        }
        return INVALID_POSITION;
    }

    private void addToData(int index, T item) {
        if (index > mSize) {
            throw new IndexOutOfBoundsException("cannot add item to " + index + " because size is " + mSize);
        }
        if (mSize == mData.length) {
            T[] newData = (T[]) Array.newInstance(mTClass, mData.length + CAPACITY_GROWTH);
            System.arraycopy(mData, 0, newData, 0, index);
            newData[index] = item;
            System.arraycopy(mData, index, newData, index + 1, mSize - index);
            mData = newData;
        } else {
            System.arraycopy(mData, index, mData, index + 1, mSize - index);
            mData[index] = item;
        }
        mSize++;
    }

    private T[] copyArray(T[] items) {
        T[] copy = (T[]) Array.newInstance(mTClass, items.length);
        System.arraycopy(items, 0, copy, 0, items.length);
        return copy;
    }

    public void clear() {
        throwIfInMutationOperation();
        if (mSize == 0) {
            return;
        }
        final int prevSize = mSize;
        Arrays.fill(mData, 0, prevSize, null);
        mSize = 0;
        mCallback.onRemoved(0, prevSize);
    }


    public static abstract class Callback<T2> implements Comparator<T2>, ListUpdateCallback {
        @Override
        abstract public int compare(T2 o1, T2 o2);

        abstract public void onChanged(int position, int count);

        @Override
        public void onChanged(int position, int count, Object payload) {
            onChanged(position, count);
        }

        abstract public boolean areContentsTheSame(T2 oldItem, T2 newItem);

        abstract public boolean areItemsTheSame(T2 item1, T2 item2);

        @Nullable
        public Object getChangePayload(T2 item1, T2 item2) {
            return null;
        }
    }

    public static class BatchedCallback<T2> extends Callback<T2> {
        final Callback<T2> mWrappedCallback;
        private final BatchingListUpdateCallback mBatchingListUpdateCallback;

        public BatchedCallback(Callback<T2> wrappedCallback) {
            mWrappedCallback = wrappedCallback;
            mBatchingListUpdateCallback = new BatchingListUpdateCallback(mWrappedCallback);
        }

        @Override
        public int compare(T2 o1, T2 o2) {
            return mWrappedCallback.compare(o1, o2);
        }

        @Override
        public void onInserted(int position, int count) {
            mBatchingListUpdateCallback.onInserted(position, count);
        }

        @Override
        public void onRemoved(int position, int count) {
            mBatchingListUpdateCallback.onRemoved(position, count);
        }

        @Override
        public void onMoved(int fromPosition, int toPosition) {
            mBatchingListUpdateCallback.onMoved(fromPosition, toPosition);
        }

        @Override
        public void onChanged(int position, int count) {
            mBatchingListUpdateCallback.onChanged(position, count, null);
        }

        @Override
        public void onChanged(int position, int count, Object payload) {
            mBatchingListUpdateCallback.onChanged(position, count, payload);
        }

        @Override
        public boolean areContentsTheSame(T2 oldItem, T2 newItem) {
            return mWrappedCallback.areContentsTheSame(oldItem, newItem);
        }

        @Override
        public boolean areItemsTheSame(T2 item1, T2 item2) {
            return mWrappedCallback.areItemsTheSame(item1, item2);
        }

        @Nullable
        @Override
        public Object getChangePayload(T2 item1, T2 item2) {
            return mWrappedCallback.getChangePayload(item1, item2);
        }

        public void dispatchLastEvent() {
            mBatchingListUpdateCallback.dispatchLastEvent();
        }
    }
}
