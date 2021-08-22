package de.dlyt.yanndroid.oneui.recyclerview;

import android.view.View;

class LayoutState {
    static final int LAYOUT_START = -1;
    static final int LAYOUT_END = 1;
    static final int INVALID_LAYOUT = Integer.MIN_VALUE;
    static final int ITEM_DIRECTION_HEAD = -1;
    static final int ITEM_DIRECTION_TAIL = 1;
    boolean mRecycle = true;
    int mAvailable;
    int mCurrentPosition;
    int mItemDirection;
    int mLayoutDirection;
    int mStartLine = 0;
    int mEndLine = 0;
    boolean mStopInFocusable;
    boolean mInfinite;

    boolean hasMore(SeslRecyclerView.State state) {
        return mCurrentPosition >= 0 && mCurrentPosition < state.getItemCount();
    }

    View next(SeslRecyclerView.Recycler recycler) {
        final View view = recycler.getViewForPosition(mCurrentPosition);
        mCurrentPosition += mItemDirection;
        return view;
    }

    @Override
    public String toString() {
        return "LayoutState{" + "mAvailable=" + mAvailable + ", mCurrentPosition=" + mCurrentPosition + ", mItemDirection=" + mItemDirection + ", mLayoutDirection=" + mLayoutDirection + ", mStartLine=" + mStartLine + ", mEndLine=" + mEndLine + '}';
    }
}
