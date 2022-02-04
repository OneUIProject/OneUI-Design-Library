package de.dlyt.yanndroid.oneui.sesl.recyclerview;

import android.util.Log;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.annotation.WorkerThread;

public class AsyncListUtil<T> {
    static final String TAG = "AsyncListUtil";
    static final boolean DEBUG = false;
    final Class<T> mTClass;
    final int mTileSize;
    final DataCallback<T> mDataCallback;
    final ViewCallback mViewCallback;
    final TileList<T> mTileList;
    final ThreadUtil.MainThreadCallback<T> mMainThreadProxy;
    final ThreadUtil.BackgroundCallback<T> mBackgroundProxy;
    final int[] mTmpRange = new int[2];
    final int[] mPrevRange = new int[2];
    final int[] mTmpRangeExtended = new int[2];
    boolean mAllowScrollHints;
    private int mScrollHint = ViewCallback.HINT_SCROLL_NONE;
    int mItemCount = 0;
    int mDisplayedGeneration = 0;
    int mRequestedGeneration = mDisplayedGeneration;
    final SparseIntArray mMissingPositions = new SparseIntArray();

    private final ThreadUtil.MainThreadCallback<T> mMainThreadCallback = new ThreadUtil.MainThreadCallback<T>() {
        @Override
        public void updateItemCount(int generation, int itemCount) {
            if (DEBUG) {
                log("updateItemCount: size=%d, gen #%d", itemCount, generation);
            }
            if (!isRequestedGeneration(generation)) {
                return;
            }
            mItemCount = itemCount;
            mViewCallback.onDataRefresh();
            mDisplayedGeneration = mRequestedGeneration;
            recycleAllTiles();

            mAllowScrollHints = false;
            updateRange();
        }

        @Override
        public void addTile(int generation, TileList.Tile<T> tile) {
            if (!isRequestedGeneration(generation)) {
                if (DEBUG) {
                    log("recycling an older generation tile @%d", tile.mStartPosition);
                }
                mBackgroundProxy.recycleTile(tile);
                return;
            }
            TileList.Tile<T> duplicate = mTileList.addOrReplace(tile);
            if (duplicate != null) {
                Log.e(TAG, "duplicate tile @" + duplicate.mStartPosition);
                mBackgroundProxy.recycleTile(duplicate);
            }
            if (DEBUG) {
                log("gen #%d, added tile @%d, total tiles: %d", generation, tile.mStartPosition, mTileList.size());
            }
            int endPosition = tile.mStartPosition + tile.mItemCount;
            int index = 0;
            while (index < mMissingPositions.size()) {
                final int position = mMissingPositions.keyAt(index);
                if (tile.mStartPosition <= position && position < endPosition) {
                    mMissingPositions.removeAt(index);
                    mViewCallback.onItemLoaded(position);
                } else {
                    index++;
                }
            }
        }

        @Override
        public void removeTile(int generation, int position) {
            if (!isRequestedGeneration(generation)) {
                return;
            }
            TileList.Tile<T> tile = mTileList.removeAtPos(position);
            if (tile == null) {
                Log.e(TAG, "tile not found @" + position);
                return;
            }
            if (DEBUG) {
                log("recycling tile @%d, total tiles: %d", tile.mStartPosition, mTileList.size());
            }
            mBackgroundProxy.recycleTile(tile);
        }

        private void recycleAllTiles() {
            if (DEBUG) {
                log("recycling all %d tiles", mTileList.size());
            }
            for (int i = 0; i < mTileList.size(); i++) {
                mBackgroundProxy.recycleTile(mTileList.getAtIndex(i));
            }
            mTileList.clear();
        }

        private boolean isRequestedGeneration(int generation) {
            return generation == mRequestedGeneration;
        }
    };

    private final ThreadUtil.BackgroundCallback<T> mBackgroundCallback = new ThreadUtil.BackgroundCallback<T>() {
        private TileList.Tile<T> mRecycledRoot;
        final SparseBooleanArray mLoadedTiles = new SparseBooleanArray();
        private int mGeneration;
        private int mItemCount;
        private int mFirstRequiredTileStart;
        private int mLastRequiredTileStart;

        @Override
        public void refresh(int generation) {
            mGeneration = generation;
            mLoadedTiles.clear();
            mItemCount = mDataCallback.refreshData();
            mMainThreadProxy.updateItemCount(mGeneration, mItemCount);
        }

        @Override
        public void updateRange(int rangeStart, int rangeEnd, int extRangeStart, int extRangeEnd, int scrollHint) {
            if (DEBUG) {
                log("updateRange: %d..%d extended to %d..%d, scroll hint: %d", rangeStart, rangeEnd, extRangeStart, extRangeEnd, scrollHint);
            }

            if (rangeStart > rangeEnd) {
                return;
            }

            final int firstVisibleTileStart = getTileStart(rangeStart);
            final int lastVisibleTileStart = getTileStart(rangeEnd);

            mFirstRequiredTileStart = getTileStart(extRangeStart);
            mLastRequiredTileStart = getTileStart(extRangeEnd);
            if (DEBUG) {
                log("requesting tile range: %d..%d", mFirstRequiredTileStart, mLastRequiredTileStart);
            }

            if (scrollHint == ViewCallback.HINT_SCROLL_DESC) {
                requestTiles(mFirstRequiredTileStart, lastVisibleTileStart, scrollHint, true);
                requestTiles(lastVisibleTileStart + mTileSize, mLastRequiredTileStart, scrollHint, false);
            } else {
                requestTiles(firstVisibleTileStart, mLastRequiredTileStart, scrollHint, false);
                requestTiles(mFirstRequiredTileStart, firstVisibleTileStart - mTileSize, scrollHint, true);
            }
        }

        private int getTileStart(int position) {
            return position - position % mTileSize;
        }

        private void requestTiles(int firstTileStart, int lastTileStart, int scrollHint, boolean backwards) {
            for (int i = firstTileStart; i <= lastTileStart; i += mTileSize) {
                int tileStart = backwards ? (lastTileStart + firstTileStart - i) : i;
                if (DEBUG) {
                    log("requesting tile @%d", tileStart);
                }
                mBackgroundProxy.loadTile(tileStart, scrollHint);
            }
        }

        @Override
        public void loadTile(int position, int scrollHint) {
            if (isTileLoaded(position)) {
                if (DEBUG) {
                    log("already loaded tile @%d", position);
                }
                return;
            }
            TileList.Tile<T> tile = acquireTile();
            tile.mStartPosition = position;
            tile.mItemCount = Math.min(mTileSize, mItemCount - tile.mStartPosition);
            mDataCallback.fillData(tile.mItems, tile.mStartPosition, tile.mItemCount);
            flushTileCache(scrollHint);
            addTile(tile);
        }

        @Override
        public void recycleTile(TileList.Tile<T> tile) {
            if (DEBUG) {
                log("recycling tile @%d", tile.mStartPosition);
            }
            mDataCallback.recycleData(tile.mItems, tile.mItemCount);

            tile.mNext = mRecycledRoot;
            mRecycledRoot = tile;
        }

        private TileList.Tile<T> acquireTile() {
            if (mRecycledRoot != null) {
                TileList.Tile<T> result = mRecycledRoot;
                mRecycledRoot = mRecycledRoot.mNext;
                return result;
            }
            return new TileList.Tile<T>(mTClass, mTileSize);
        }

        private boolean isTileLoaded(int position) {
            return mLoadedTiles.get(position);
        }

        private void addTile(TileList.Tile<T> tile) {
            mLoadedTiles.put(tile.mStartPosition, true);
            mMainThreadProxy.addTile(mGeneration, tile);
            if (DEBUG) {
                log("loaded tile @%d, total tiles: %d", tile.mStartPosition, mLoadedTiles.size());
            }
        }

        private void removeTile(int position) {
            mLoadedTiles.delete(position);
            mMainThreadProxy.removeTile(mGeneration, position);
            if (DEBUG) {
                log("flushed tile @%d, total tiles: %s", position, mLoadedTiles.size());
            }
        }

        private void flushTileCache(int scrollHint) {
            final int cacheSizeLimit = mDataCallback.getMaxCachedTiles();
            while (mLoadedTiles.size() >= cacheSizeLimit) {
                int firstLoadedTileStart = mLoadedTiles.keyAt(0);
                int lastLoadedTileStart = mLoadedTiles.keyAt(mLoadedTiles.size() - 1);
                int startMargin = mFirstRequiredTileStart - firstLoadedTileStart;
                int endMargin = lastLoadedTileStart - mLastRequiredTileStart;
                if (startMargin > 0 && (startMargin >= endMargin || (scrollHint == ViewCallback.HINT_SCROLL_ASC))) {
                    removeTile(firstLoadedTileStart);
                } else if (endMargin > 0 && (startMargin < endMargin || (scrollHint == ViewCallback.HINT_SCROLL_DESC))){
                    removeTile(lastLoadedTileStart);
                } else {
                    return;
                }
            }
        }

        private void log(String s, Object... args) {
            Log.d(TAG, "[BKGR] " + String.format(s, args));
        }
    };

    void log(String s, Object... args) {
        Log.d(TAG, "[MAIN] " + String.format(s, args));
    }

    public AsyncListUtil(@NonNull Class<T> klass, int tileSize, @NonNull DataCallback<T> dataCallback, @NonNull ViewCallback viewCallback) {
        mTClass = klass;
        mTileSize = tileSize;
        mDataCallback = dataCallback;
        mViewCallback = viewCallback;

        mTileList = new TileList<T>(mTileSize);

        ThreadUtil<T> threadUtil = new MessageThreadUtil<T>();
        mMainThreadProxy = threadUtil.getMainThreadProxy(mMainThreadCallback);
        mBackgroundProxy = threadUtil.getBackgroundProxy(mBackgroundCallback);

        refresh();
    }

    private boolean isRefreshPending() {
        return mRequestedGeneration != mDisplayedGeneration;
    }

    public void onRangeChanged() {
        if (isRefreshPending()) {
            return;
        }
        updateRange();
        mAllowScrollHints = true;
    }

    public void refresh() {
        mMissingPositions.clear();
        mBackgroundProxy.refresh(++mRequestedGeneration);
    }

    @Nullable
    public T getItem(int position) {
        if (position < 0 || position >= mItemCount) {
            throw new IndexOutOfBoundsException(position + " is not within 0 and " + mItemCount);
        }
        T item = mTileList.getItemAt(position);
        if (item == null && !isRefreshPending()) {
            mMissingPositions.put(position, 0);
        }
        return item;
    }

    public int getItemCount() {
        return mItemCount;
    }

    void updateRange() {
        mViewCallback.getItemRangeInto(mTmpRange);
        if (mTmpRange[0] > mTmpRange[1] || mTmpRange[0] < 0) {
            return;
        }
        if (mTmpRange[1] >= mItemCount) {
            return;
        }

        if (!mAllowScrollHints) {
            mScrollHint = ViewCallback.HINT_SCROLL_NONE;
        } else if (mTmpRange[0] > mPrevRange[1] || mPrevRange[0] > mTmpRange[1]) {
            mScrollHint = ViewCallback.HINT_SCROLL_NONE;
        } else if (mTmpRange[0] < mPrevRange[0]) {
            mScrollHint = ViewCallback.HINT_SCROLL_DESC;
        } else if (mTmpRange[0] > mPrevRange[0]) {
            mScrollHint = ViewCallback.HINT_SCROLL_ASC;
        }

        mPrevRange[0] = mTmpRange[0];
        mPrevRange[1] = mTmpRange[1];

        mViewCallback.extendRangeInto(mTmpRange, mTmpRangeExtended, mScrollHint);
        mTmpRangeExtended[0] = Math.min(mTmpRange[0], Math.max(mTmpRangeExtended[0], 0));
        mTmpRangeExtended[1] = Math.max(mTmpRange[1], Math.min(mTmpRangeExtended[1], mItemCount - 1));

        mBackgroundProxy.updateRange(mTmpRange[0], mTmpRange[1], mTmpRangeExtended[0], mTmpRangeExtended[1], mScrollHint);
    }


    public static abstract class DataCallback<T> {
        @WorkerThread
        public abstract int refreshData();

        @WorkerThread
        public abstract void fillData(@NonNull T[] data, int startPosition, int itemCount);

        @WorkerThread
        public void recycleData(@NonNull T[] data, int itemCount) {
        }

        @WorkerThread
        public int getMaxCachedTiles() {
            return 10;
        }
    }

    public static abstract class ViewCallback {
        public static final int HINT_SCROLL_NONE = 0;
        public static final int HINT_SCROLL_DESC = 1;
        public static final int HINT_SCROLL_ASC = 2;

        @UiThread
        public abstract void getItemRangeInto(@NonNull int[] outRange);

        @UiThread
        public void extendRangeInto(@NonNull int[] range, @NonNull int[] outRange, int scrollHint) {
            final int fullRange = range[1] - range[0] + 1;
            final int halfRange = fullRange / 2;
            outRange[0] = range[0] - (scrollHint == HINT_SCROLL_DESC ? fullRange : halfRange);
            outRange[1] = range[1] + (scrollHint == HINT_SCROLL_ASC ? fullRange : halfRange);
        }

        @UiThread
        public abstract void onDataRefresh();

        @UiThread
        public abstract void onItemLoaded(int position);
    }
}
