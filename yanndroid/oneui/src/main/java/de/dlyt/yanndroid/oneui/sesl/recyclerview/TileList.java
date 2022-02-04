package de.dlyt.yanndroid.oneui.sesl.recyclerview;

import android.util.SparseArray;

import java.lang.reflect.Array;

class TileList<T> {
    final int mTileSize;
    private final SparseArray<Tile<T>> mTiles = new SparseArray<Tile<T>>(10);
    Tile<T> mLastAccessedTile;

    public TileList(int tileSize) {
        mTileSize = tileSize;
    }

    public T getItemAt(int pos) {
        if (mLastAccessedTile == null || !mLastAccessedTile.containsPosition(pos)) {
            final int startPosition = pos - (pos % mTileSize);
            final int index = mTiles.indexOfKey(startPosition);
            if (index < 0) {
                return null;
            }
            mLastAccessedTile = mTiles.valueAt(index);
        }
        return mLastAccessedTile.getByPosition(pos);
    }

    public int size() {
        return mTiles.size();
    }

    public void clear() {
        mTiles.clear();
    }

    public Tile<T> getAtIndex(int index) {
        if (index < 0 || index >= mTiles.size()) {
            return null;
        }
        return mTiles.valueAt(index);
    }

    public Tile<T> addOrReplace(Tile<T> newTile) {
        final int index = mTiles.indexOfKey(newTile.mStartPosition);
        if (index < 0) {
            mTiles.put(newTile.mStartPosition, newTile);
            return null;
        }
        Tile<T> oldTile = mTiles.valueAt(index);
        mTiles.setValueAt(index, newTile);
        if (mLastAccessedTile == oldTile) {
            mLastAccessedTile = newTile;
        }
        return oldTile;
    }

    public Tile<T> removeAtPos(int startPosition) {
        Tile<T> tile = mTiles.get(startPosition);
        if (mLastAccessedTile == tile) {
            mLastAccessedTile = null;
        }
        mTiles.delete(startPosition);
        return tile;
    }


    public static class Tile<T> {
        public final T[] mItems;
        public int mStartPosition;
        public int mItemCount;
        Tile<T> mNext;

        public Tile(Class<T> klass, int size) {
            @SuppressWarnings("unchecked")
            T[] items = (T[]) Array.newInstance(klass, size);
            mItems = items;
        }

        boolean containsPosition(int pos) {
            return mStartPosition <= pos && pos < mStartPosition + mItemCount;
        }

        T getByPosition(int pos) {
            return mItems[pos - mStartPosition];
        }
    }
}
