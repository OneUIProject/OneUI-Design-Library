package de.dlyt.yanndroid.oneui.sesl.recyclerview;

import androidx.annotation.NonNull;

public class BatchingListUpdateCallback implements ListUpdateCallback {
    private static final int TYPE_NONE = 0;
    private static final int TYPE_ADD = 1;
    private static final int TYPE_REMOVE = 2;
    private static final int TYPE_CHANGE = 3;

    final ListUpdateCallback mWrapped;

    int mLastEventType = TYPE_NONE;
    int mLastEventPosition = -1;
    int mLastEventCount = -1;
    Object mLastEventPayload = null;

    public BatchingListUpdateCallback(@NonNull ListUpdateCallback callback) {
        mWrapped = callback;
    }

    public void dispatchLastEvent() {
        if (mLastEventType == TYPE_NONE) {
            return;
        }
        switch (mLastEventType) {
            case TYPE_ADD:
                mWrapped.onInserted(mLastEventPosition, mLastEventCount);
                break;
            case TYPE_REMOVE:
                mWrapped.onRemoved(mLastEventPosition, mLastEventCount);
                break;
            case TYPE_CHANGE:
                mWrapped.onChanged(mLastEventPosition, mLastEventCount, mLastEventPayload);
                break;
        }
        mLastEventPayload = null;
        mLastEventType = TYPE_NONE;
    }

    @Override
    public void onInserted(int position, int count) {
        if (mLastEventType == TYPE_ADD && position >= mLastEventPosition && position <= mLastEventPosition + mLastEventCount) {
            mLastEventCount += count;
            mLastEventPosition = Math.min(position, mLastEventPosition);
            return;
        }
        dispatchLastEvent();
        mLastEventPosition = position;
        mLastEventCount = count;
        mLastEventType = TYPE_ADD;
    }

    @Override
    public void onRemoved(int position, int count) {
        if (mLastEventType == TYPE_REMOVE && mLastEventPosition >= position && mLastEventPosition <= position + count) {
            mLastEventCount += count;
            mLastEventPosition = position;
            return;
        }
        dispatchLastEvent();
        mLastEventPosition = position;
        mLastEventCount = count;
        mLastEventType = TYPE_REMOVE;
    }

    @Override
    public void onMoved(int fromPosition, int toPosition) {
        dispatchLastEvent(); // moves are not merged
        mWrapped.onMoved(fromPosition, toPosition);
    }

    @Override
    public void onChanged(int position, int count, Object payload) {
        if (mLastEventType == TYPE_CHANGE && !(position > mLastEventPosition + mLastEventCount || position + count < mLastEventPosition || mLastEventPayload != payload)) {
            int previousEnd = mLastEventPosition + mLastEventCount;
            mLastEventPosition = Math.min(position, mLastEventPosition);
            mLastEventCount = Math.max(previousEnd, position + count) - mLastEventPosition;
            return;
        }
        dispatchLastEvent();
        mLastEventPosition = position;
        mLastEventCount = count;
        mLastEventPayload = payload;
        mLastEventType = TYPE_CHANGE;
    }
}
