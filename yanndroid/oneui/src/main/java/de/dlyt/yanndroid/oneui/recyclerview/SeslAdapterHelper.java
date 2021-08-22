package de.dlyt.yanndroid.oneui.recyclerview;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.core.util.Pools;

class SeslAdapterHelper implements SeslOpReorderer.Callback {
    static final int POSITION_TYPE_INVISIBLE = 0;
    static final int POSITION_TYPE_NEW_OR_LAID_OUT = 1;
    private static final boolean DEBUG = false;
    private static final String TAG = "AHT";
    private Pools.Pool<UpdateOp> mUpdateOpPool = new Pools.SimplePool<UpdateOp>(UpdateOp.POOL_SIZE);
    final ArrayList<UpdateOp> mPendingUpdates = new ArrayList<UpdateOp>();
    final ArrayList<UpdateOp> mPostponedList = new ArrayList<UpdateOp>();
    final Callback mCallback;
    Runnable mOnItemProcessedCallback;
    final boolean mDisableRecycler;
    final SeslOpReorderer mOpReorderer;
    private int mExistingUpdateTypes = 0;

    SeslAdapterHelper(Callback callback) {
        this(callback, false);
    }

    SeslAdapterHelper(Callback callback, boolean disableRecycler) {
        mCallback = callback;
        mDisableRecycler = disableRecycler;
        mOpReorderer = new SeslOpReorderer(this);
    }

    SeslAdapterHelper addUpdateOp(UpdateOp... ops) {
        Collections.addAll(mPendingUpdates, ops);
        return this;
    }

    void reset() {
        recycleUpdateOpsAndClearList(mPendingUpdates);
        recycleUpdateOpsAndClearList(mPostponedList);
        mExistingUpdateTypes = 0;
    }

    void preProcess() {
        mOpReorderer.reorderOps(mPendingUpdates);
        final int count = mPendingUpdates.size();
        for (int i = 0; i < count; i++) {
            UpdateOp op = mPendingUpdates.get(i);
            switch (op.cmd) {
                case UpdateOp.ADD:
                    applyAdd(op);
                    break;
                case UpdateOp.REMOVE:
                    applyRemove(op);
                    break;
                case UpdateOp.UPDATE:
                    applyUpdate(op);
                    break;
                case UpdateOp.MOVE:
                    applyMove(op);
                    break;
            }
            if (mOnItemProcessedCallback != null) {
                mOnItemProcessedCallback.run();
            }
        }
        mPendingUpdates.clear();
    }

    void consumePostponedUpdates() {
        final int count = mPostponedList.size();
        for (int i = 0; i < count; i++) {
            mCallback.onDispatchSecondPass(mPostponedList.get(i));
        }
        recycleUpdateOpsAndClearList(mPostponedList);
        mExistingUpdateTypes = 0;
    }

    private void applyMove(UpdateOp op) {
        postponeAndUpdateViewHolders(op);
    }

    private void applyRemove(UpdateOp op) {
        int tmpStart = op.positionStart;
        int tmpCount = 0;
        int tmpEnd = op.positionStart + op.itemCount;
        int type = -1;
        for (int position = op.positionStart; position < tmpEnd; position++) {
            boolean typeChanged = false;
            SeslRecyclerView.ViewHolder vh = mCallback.findViewHolder(position);
            if (vh != null || canFindInPreLayout(position)) {
                if (type == POSITION_TYPE_INVISIBLE) {
                    UpdateOp newOp = obtainUpdateOp(UpdateOp.REMOVE, tmpStart, tmpCount, null);
                    dispatchAndUpdateViewHolders(newOp);
                    typeChanged = true;
                }
                type = POSITION_TYPE_NEW_OR_LAID_OUT;
            } else {
                if (type == POSITION_TYPE_NEW_OR_LAID_OUT) {
                    UpdateOp newOp = obtainUpdateOp(UpdateOp.REMOVE, tmpStart, tmpCount, null);
                    postponeAndUpdateViewHolders(newOp);
                    typeChanged = true;
                }
                type = POSITION_TYPE_INVISIBLE;
            }
            if (typeChanged) {
                position -= tmpCount;
                tmpEnd -= tmpCount;
                tmpCount = 1;
            } else {
                tmpCount++;
            }
        }
        if (tmpCount != op.itemCount) {
            recycleUpdateOp(op);
            op = obtainUpdateOp(UpdateOp.REMOVE, tmpStart, tmpCount, null);
        }
        if (type == POSITION_TYPE_INVISIBLE) {
            dispatchAndUpdateViewHolders(op);
        } else {
            postponeAndUpdateViewHolders(op);
        }
    }

    private void applyUpdate(UpdateOp op) {
        int tmpStart = op.positionStart;
        int tmpCount = 0;
        int tmpEnd = op.positionStart + op.itemCount;
        int type = -1;
        for (int position = op.positionStart; position < tmpEnd; position++) {
            SeslRecyclerView.ViewHolder vh = mCallback.findViewHolder(position);
            if (vh != null || canFindInPreLayout(position)) {
                if (type == POSITION_TYPE_INVISIBLE) {
                    UpdateOp newOp = obtainUpdateOp(UpdateOp.UPDATE, tmpStart, tmpCount,
                            op.payload);
                    dispatchAndUpdateViewHolders(newOp);
                    tmpCount = 0;
                    tmpStart = position;
                }
                type = POSITION_TYPE_NEW_OR_LAID_OUT;
            } else {
                if (type == POSITION_TYPE_NEW_OR_LAID_OUT) {
                    UpdateOp newOp = obtainUpdateOp(UpdateOp.UPDATE, tmpStart, tmpCount,
                            op.payload);
                    postponeAndUpdateViewHolders(newOp);
                    tmpCount = 0;
                    tmpStart = position;
                }
                type = POSITION_TYPE_INVISIBLE;
            }
            tmpCount++;
        }
        if (tmpCount != op.itemCount) {
            Object payload = op.payload;
            recycleUpdateOp(op);
            op = obtainUpdateOp(UpdateOp.UPDATE, tmpStart, tmpCount, payload);
        }
        if (type == POSITION_TYPE_INVISIBLE) {
            dispatchAndUpdateViewHolders(op);
        } else {
            postponeAndUpdateViewHolders(op);
        }
    }

    private void dispatchAndUpdateViewHolders(UpdateOp op) {
        if (op.cmd == UpdateOp.ADD || op.cmd == UpdateOp.MOVE) {
            throw new IllegalArgumentException("should not dispatch add or move for pre layout");
        }
        if (DEBUG) {
            Log.d(TAG, "dispatch (pre)" + op);
            Log.d(TAG, "postponed state before:");
            for (UpdateOp updateOp : mPostponedList) {
                Log.d(TAG, updateOp.toString());
            }
            Log.d(TAG, "----");
        }

        int tmpStart = updatePositionWithPostponed(op.positionStart, op.cmd);
        if (DEBUG) {
            Log.d(TAG, "pos:" + op.positionStart + ",updatedPos:" + tmpStart);
        }
        int tmpCnt = 1;
        int offsetPositionForPartial = op.positionStart;
        final int positionMultiplier;
        switch (op.cmd) {
            case UpdateOp.UPDATE:
                positionMultiplier = 1;
                break;
            case UpdateOp.REMOVE:
                positionMultiplier = 0;
                break;
            default:
                throw new IllegalArgumentException("op should be remove or update." + op);
        }
        for (int p = 1; p < op.itemCount; p++) {
            final int pos = op.positionStart + (positionMultiplier * p);
            int updatedPos = updatePositionWithPostponed(pos, op.cmd);
            if (DEBUG) {
                Log.d(TAG, "pos:" + pos + ",updatedPos:" + updatedPos);
            }
            boolean continuous = false;
            switch (op.cmd) {
                case UpdateOp.UPDATE:
                    continuous = updatedPos == tmpStart + 1;
                    break;
                case UpdateOp.REMOVE:
                    continuous = updatedPos == tmpStart;
                    break;
            }
            if (continuous) {
                tmpCnt++;
            } else {
                UpdateOp tmp = obtainUpdateOp(op.cmd, tmpStart, tmpCnt, op.payload);
                if (DEBUG) {
                    Log.d(TAG, "need to dispatch separately " + tmp);
                }
                dispatchFirstPassAndUpdateViewHolders(tmp, offsetPositionForPartial);
                recycleUpdateOp(tmp);
                if (op.cmd == UpdateOp.UPDATE) {
                    offsetPositionForPartial += tmpCnt;
                }
                tmpStart = updatedPos;
                tmpCnt = 1;
            }
        }
        Object payload = op.payload;
        recycleUpdateOp(op);
        if (tmpCnt > 0) {
            UpdateOp tmp = obtainUpdateOp(op.cmd, tmpStart, tmpCnt, payload);
            if (DEBUG) {
                Log.d(TAG, "dispatching:" + tmp);
            }
            dispatchFirstPassAndUpdateViewHolders(tmp, offsetPositionForPartial);
            recycleUpdateOp(tmp);
        }
        if (DEBUG) {
            Log.d(TAG, "post dispatch");
            Log.d(TAG, "postponed state after:");
            for (UpdateOp updateOp : mPostponedList) {
                Log.d(TAG, updateOp.toString());
            }
            Log.d(TAG, "----");
        }
    }

    void dispatchFirstPassAndUpdateViewHolders(UpdateOp op, int offsetStart) {
        mCallback.onDispatchFirstPass(op);
        switch (op.cmd) {
            case UpdateOp.REMOVE:
                mCallback.offsetPositionsForRemovingInvisible(offsetStart, op.itemCount);
                break;
            case UpdateOp.UPDATE:
                mCallback.markViewHoldersUpdated(offsetStart, op.itemCount, op.payload);
                break;
            default:
                throw new IllegalArgumentException("only remove and update ops can be dispatched"
                        + " in first pass");
        }
    }

    private int updatePositionWithPostponed(int pos, int cmd) {
        final int count = mPostponedList.size();
        for (int i = count - 1; i >= 0; i--) {
            UpdateOp postponed = mPostponedList.get(i);
            if (postponed.cmd == UpdateOp.MOVE) {
                int start, end;
                if (postponed.positionStart < postponed.itemCount) {
                    start = postponed.positionStart;
                    end = postponed.itemCount;
                } else {
                    start = postponed.itemCount;
                    end = postponed.positionStart;
                }
                if (pos >= start && pos <= end) {
                    if (start == postponed.positionStart) {
                        if (cmd == UpdateOp.ADD) {
                            postponed.itemCount++;
                        } else if (cmd == UpdateOp.REMOVE) {
                            postponed.itemCount--;
                        }
                        pos++;
                    } else {
                        if (cmd == UpdateOp.ADD) {
                            postponed.positionStart++;
                        } else if (cmd == UpdateOp.REMOVE) {
                            postponed.positionStart--;
                        }
                        pos--;
                    }
                } else if (pos < postponed.positionStart) {
                    if (cmd == UpdateOp.ADD) {
                        postponed.positionStart++;
                        postponed.itemCount++;
                    } else if (cmd == UpdateOp.REMOVE) {
                        postponed.positionStart--;
                        postponed.itemCount--;
                    }
                }
            } else {
                if (postponed.positionStart <= pos) {
                    if (postponed.cmd == UpdateOp.ADD) {
                        pos -= postponed.itemCount;
                    } else if (postponed.cmd == UpdateOp.REMOVE) {
                        pos += postponed.itemCount;
                    }
                } else {
                    if (cmd == UpdateOp.ADD) {
                        postponed.positionStart++;
                    } else if (cmd == UpdateOp.REMOVE) {
                        postponed.positionStart--;
                    }
                }
            }
            if (DEBUG) {
                Log.d(TAG, "dispath (step" + i + ")");
                Log.d(TAG, "postponed state:" + i + ", pos:" + pos);
                for (UpdateOp updateOp : mPostponedList) {
                    Log.d(TAG, updateOp.toString());
                }
                Log.d(TAG, "----");
            }
        }
        for (int i = mPostponedList.size() - 1; i >= 0; i--) {
            UpdateOp op = mPostponedList.get(i);
            if (op.cmd == UpdateOp.MOVE) {
                if (op.itemCount == op.positionStart || op.itemCount < 0) {
                    mPostponedList.remove(i);
                    recycleUpdateOp(op);
                }
            } else if (op.itemCount <= 0) {
                mPostponedList.remove(i);
                recycleUpdateOp(op);
            }
        }
        return pos;
    }

    private boolean canFindInPreLayout(int position) {
        final int count = mPostponedList.size();
        for (int i = 0; i < count; i++) {
            UpdateOp op = mPostponedList.get(i);
            if (op.cmd == UpdateOp.MOVE) {
                if (findPositionOffset(op.itemCount, i + 1) == position) {
                    return true;
                }
            } else if (op.cmd == UpdateOp.ADD) {
                final int end = op.positionStart + op.itemCount;
                for (int pos = op.positionStart; pos < end; pos++) {
                    if (findPositionOffset(pos, i + 1) == position) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void applyAdd(UpdateOp op) {
        postponeAndUpdateViewHolders(op);
    }

    private void postponeAndUpdateViewHolders(UpdateOp op) {
        if (DEBUG) {
            Log.d(TAG, "postponing " + op);
        }
        mPostponedList.add(op);
        switch (op.cmd) {
            case UpdateOp.ADD:
                mCallback.offsetPositionsForAdd(op.positionStart, op.itemCount);
                break;
            case UpdateOp.MOVE:
                mCallback.offsetPositionsForMove(op.positionStart, op.itemCount);
                break;
            case UpdateOp.REMOVE:
                mCallback.offsetPositionsForRemovingLaidOutOrNewView(op.positionStart,
                        op.itemCount);
                break;
            case UpdateOp.UPDATE:
                mCallback.markViewHoldersUpdated(op.positionStart, op.itemCount, op.payload);
                break;
            default:
                throw new IllegalArgumentException("Unknown update op type for " + op);
        }
    }

    boolean hasPendingUpdates() {
        return mPendingUpdates.size() > 0;
    }

    boolean hasAnyUpdateTypes(int updateTypes) {
        return (mExistingUpdateTypes & updateTypes) != 0;
    }

    int findPositionOffset(int position) {
        return findPositionOffset(position, 0);
    }

    int findPositionOffset(int position, int firstPostponedItem) {
        int count = mPostponedList.size();
        for (int i = firstPostponedItem; i < count; ++i) {
            UpdateOp op = mPostponedList.get(i);
            if (op.cmd == UpdateOp.MOVE) {
                if (op.positionStart == position) {
                    position = op.itemCount;
                } else {
                    if (op.positionStart < position) {
                        position--;
                    }
                    if (op.itemCount <= position) {
                        position++;
                    }
                }
            } else if (op.positionStart <= position) {
                if (op.cmd == UpdateOp.REMOVE) {
                    if (position < op.positionStart + op.itemCount) {
                        return -1;
                    }
                    position -= op.itemCount;
                } else if (op.cmd == UpdateOp.ADD) {
                    position += op.itemCount;
                }
            }
        }
        return position;
    }

    boolean onItemRangeChanged(int positionStart, int itemCount, Object payload) {
        if (itemCount < 1) {
            return false;
        }
        mPendingUpdates.add(obtainUpdateOp(UpdateOp.UPDATE, positionStart, itemCount, payload));
        mExistingUpdateTypes |= UpdateOp.UPDATE;
        return mPendingUpdates.size() == 1;
    }

    boolean onItemRangeInserted(int positionStart, int itemCount) {
        if (itemCount < 1) {
            return false;
        }
        mPendingUpdates.add(obtainUpdateOp(UpdateOp.ADD, positionStart, itemCount, null));
        mExistingUpdateTypes |= UpdateOp.ADD;
        return mPendingUpdates.size() == 1;
    }

    boolean onItemRangeRemoved(int positionStart, int itemCount) {
        if (itemCount < 1) {
            return false;
        }
        mPendingUpdates.add(obtainUpdateOp(UpdateOp.REMOVE, positionStart, itemCount, null));
        mExistingUpdateTypes |= UpdateOp.REMOVE;
        return mPendingUpdates.size() == 1;
    }

    boolean onItemRangeMoved(int from, int to, int itemCount) {
        if (from == to) {
            return false;
        }
        if (itemCount != 1) {
            throw new IllegalArgumentException("Moving more than 1 item is not supported yet");
        }
        mPendingUpdates.add(obtainUpdateOp(UpdateOp.MOVE, from, to, null));
        mExistingUpdateTypes |= UpdateOp.MOVE;
        return mPendingUpdates.size() == 1;
    }

    void consumeUpdatesInOnePass() {
        consumePostponedUpdates();
        final int count = mPendingUpdates.size();
        for (int i = 0; i < count; i++) {
            UpdateOp op = mPendingUpdates.get(i);
            switch (op.cmd) {
                case UpdateOp.ADD:
                    mCallback.onDispatchSecondPass(op);
                    mCallback.offsetPositionsForAdd(op.positionStart, op.itemCount);
                    break;
                case UpdateOp.REMOVE:
                    mCallback.onDispatchSecondPass(op);
                    mCallback.offsetPositionsForRemovingInvisible(op.positionStart, op.itemCount);
                    break;
                case UpdateOp.UPDATE:
                    mCallback.onDispatchSecondPass(op);
                    mCallback.markViewHoldersUpdated(op.positionStart, op.itemCount, op.payload);
                    break;
                case UpdateOp.MOVE:
                    mCallback.onDispatchSecondPass(op);
                    mCallback.offsetPositionsForMove(op.positionStart, op.itemCount);
                    break;
            }
            if (mOnItemProcessedCallback != null) {
                mOnItemProcessedCallback.run();
            }
        }
        recycleUpdateOpsAndClearList(mPendingUpdates);
        mExistingUpdateTypes = 0;
    }

    public int applyPendingUpdatesToPosition(int position) {
        final int size = mPendingUpdates.size();
        for (int i = 0; i < size; i++) {
            UpdateOp op = mPendingUpdates.get(i);
            switch (op.cmd) {
                case UpdateOp.ADD:
                    if (op.positionStart <= position) {
                        position += op.itemCount;
                    }
                    break;
                case UpdateOp.REMOVE:
                    if (op.positionStart <= position) {
                        final int end = op.positionStart + op.itemCount;
                        if (end > position) {
                            return SeslRecyclerView.NO_POSITION;
                        }
                        position -= op.itemCount;
                    }
                    break;
                case UpdateOp.MOVE:
                    if (op.positionStart == position) {
                        position = op.itemCount;
                    } else {
                        if (op.positionStart < position) {
                            position -= 1;
                        }
                        if (op.itemCount <= position) {
                            position += 1;
                        }
                    }
                    break;
            }
        }
        return position;
    }

    boolean hasUpdates() {
        return !mPostponedList.isEmpty() && !mPendingUpdates.isEmpty();
    }

    static class UpdateOp {
        static final int ADD = 1;
        static final int REMOVE = 1 << 1;
        static final int UPDATE = 1 << 2;
        static final int MOVE = 1 << 3;
        static final int POOL_SIZE = 30;
        int cmd;
        int positionStart;
        Object payload;
        int itemCount;

        UpdateOp(int cmd, int positionStart, int itemCount, Object payload) {
            this.cmd = cmd;
            this.positionStart = positionStart;
            this.itemCount = itemCount;
            this.payload = payload;
        }

        String cmdToString() {
            switch (cmd) {
                case ADD:
                    return "add";
                case REMOVE:
                    return "rm";
                case UPDATE:
                    return "up";
                case MOVE:
                    return "mv";
            }
            return "??";
        }

        @Override
        public String toString() {
            return Integer.toHexString(System.identityHashCode(this))
                    + "[" + cmdToString() + ",s:" + positionStart + "c:" + itemCount
                    + ",p:" + payload + "]";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            UpdateOp op = (UpdateOp) o;

            if (cmd != op.cmd) {
                return false;
            }
            if (cmd == MOVE && Math.abs(itemCount - positionStart) == 1) {
                if (itemCount == op.positionStart && positionStart == op.itemCount) {
                    return true;
                }
            }
            if (itemCount != op.itemCount) {
                return false;
            }
            if (positionStart != op.positionStart) {
                return false;
            }
            if (payload != null) {
                if (!payload.equals(op.payload)) {
                    return false;
                }
            } else if (op.payload != null) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            int result = cmd;
            result = 31 * result + positionStart;
            result = 31 * result + itemCount;
            return result;
        }
    }

    @Override
    public UpdateOp obtainUpdateOp(int cmd, int positionStart, int itemCount, Object payload) {
        UpdateOp op = mUpdateOpPool.acquire();
        if (op == null) {
            op = new UpdateOp(cmd, positionStart, itemCount, payload);
        } else {
            op.cmd = cmd;
            op.positionStart = positionStart;
            op.itemCount = itemCount;
            op.payload = payload;
        }
        return op;
    }

    @Override
    public void recycleUpdateOp(UpdateOp op) {
        if (!mDisableRecycler) {
            op.payload = null;
            mUpdateOpPool.release(op);
        }
    }

    void recycleUpdateOpsAndClearList(List<UpdateOp> ops) {
        final int count = ops.size();
        for (int i = 0; i < count; i++) {
            recycleUpdateOp(ops.get(i));
        }
        ops.clear();
    }

    interface Callback {
        SeslRecyclerView.ViewHolder findViewHolder(int position);
        void offsetPositionsForRemovingInvisible(int positionStart, int itemCount);
        void offsetPositionsForRemovingLaidOutOrNewView(int positionStart, int itemCount);
        void markViewHoldersUpdated(int positionStart, int itemCount, Object payloads);
        void onDispatchFirstPass(UpdateOp updateOp);
        void onDispatchSecondPass(UpdateOp updateOp);
        void offsetPositionsForAdd(int positionStart, int itemCount);
        void offsetPositionsForMove(int from, int to);
    }
}
