package de.dlyt.yanndroid.oneui.recyclerview;

import java.util.List;

import de.dlyt.yanndroid.oneui.recyclerview.SeslAdapterHelper.UpdateOp;

import static de.dlyt.yanndroid.oneui.recyclerview.SeslAdapterHelper.UpdateOp.ADD;
import static de.dlyt.yanndroid.oneui.recyclerview.SeslAdapterHelper.UpdateOp.MOVE;
import static de.dlyt.yanndroid.oneui.recyclerview.SeslAdapterHelper.UpdateOp.REMOVE;
import static de.dlyt.yanndroid.oneui.recyclerview.SeslAdapterHelper.UpdateOp.UPDATE;

class SeslOpReorderer {

    final Callback mCallback;

    SeslOpReorderer(Callback callback) {
        mCallback = callback;
    }

    void reorderOps(List<UpdateOp> ops) {
        int badMove;
        while ((badMove = getLastMoveOutOfOrder(ops)) != -1) {
            swapMoveOp(ops, badMove, badMove + 1);
        }
    }

    private void swapMoveOp(List<UpdateOp> list, int badMove, int next) {
        final UpdateOp moveOp = list.get(badMove);
        final UpdateOp nextOp = list.get(next);
        switch (nextOp.cmd) {
            case REMOVE:
                swapMoveRemove(list, badMove, moveOp, next, nextOp);
                break;
            case ADD:
                swapMoveAdd(list, badMove, moveOp, next, nextOp);
                break;
            case UPDATE:
                swapMoveUpdate(list, badMove, moveOp, next, nextOp);
                break;
        }
    }

    void swapMoveRemove(List<UpdateOp> list, int movePos, UpdateOp moveOp, int removePos, UpdateOp removeOp) {
        UpdateOp extraRm = null;
        boolean revertedMove = false;
        final boolean moveIsBackwards;

        if (moveOp.positionStart < moveOp.itemCount) {
            moveIsBackwards = false;
            if (removeOp.positionStart == moveOp.positionStart
                    && removeOp.itemCount == moveOp.itemCount - moveOp.positionStart) {
                revertedMove = true;
            }
        } else {
            moveIsBackwards = true;
            if (removeOp.positionStart == moveOp.itemCount + 1
                    && removeOp.itemCount == moveOp.positionStart - moveOp.itemCount) {
                revertedMove = true;
            }
        }

        if (moveOp.itemCount < removeOp.positionStart) {
            removeOp.positionStart--;
        } else if (moveOp.itemCount < removeOp.positionStart + removeOp.itemCount) {
            removeOp.itemCount--;
            moveOp.cmd = REMOVE;
            moveOp.itemCount = 1;
            if (removeOp.itemCount == 0) {
                list.remove(removePos);
                mCallback.recycleUpdateOp(removeOp);
            }
            return;
        }

        if (moveOp.positionStart <= removeOp.positionStart) {
            removeOp.positionStart++;
        } else if (moveOp.positionStart < removeOp.positionStart + removeOp.itemCount) {
            final int remaining = removeOp.positionStart + removeOp.itemCount - moveOp.positionStart;
            extraRm = mCallback.obtainUpdateOp(REMOVE, moveOp.positionStart + 1, remaining, null);
            removeOp.itemCount = moveOp.positionStart - removeOp.positionStart;
        }

        if (revertedMove) {
            list.set(movePos, removeOp);
            list.remove(removePos);
            mCallback.recycleUpdateOp(moveOp);
            return;
        }

        if (moveIsBackwards) {
            if (extraRm != null) {
                if (moveOp.positionStart > extraRm.positionStart) {
                    moveOp.positionStart -= extraRm.itemCount;
                }
                if (moveOp.itemCount > extraRm.positionStart) {
                    moveOp.itemCount -= extraRm.itemCount;
                }
            }
            if (moveOp.positionStart > removeOp.positionStart) {
                moveOp.positionStart -= removeOp.itemCount;
            }
            if (moveOp.itemCount > removeOp.positionStart) {
                moveOp.itemCount -= removeOp.itemCount;
            }
        } else {
            if (extraRm != null) {
                if (moveOp.positionStart >= extraRm.positionStart) {
                    moveOp.positionStart -= extraRm.itemCount;
                }
                if (moveOp.itemCount >= extraRm.positionStart) {
                    moveOp.itemCount -= extraRm.itemCount;
                }
            }
            if (moveOp.positionStart >= removeOp.positionStart) {
                moveOp.positionStart -= removeOp.itemCount;
            }
            if (moveOp.itemCount >= removeOp.positionStart) {
                moveOp.itemCount -= removeOp.itemCount;
            }
        }

        list.set(movePos, removeOp);
        if (moveOp.positionStart != moveOp.itemCount) {
            list.set(removePos, moveOp);
        } else {
            list.remove(removePos);
        }
        if (extraRm != null) {
            list.add(movePos, extraRm);
        }
    }

    private void swapMoveAdd(List<UpdateOp> list, int move, UpdateOp moveOp, int add, UpdateOp addOp) {
        int offset = 0;
        if (moveOp.itemCount < addOp.positionStart) {
            offset--;
        }
        if (moveOp.positionStart < addOp.positionStart) {
            offset++;
        }
        if (addOp.positionStart <= moveOp.positionStart) {
            moveOp.positionStart += addOp.itemCount;
        }
        if (addOp.positionStart <= moveOp.itemCount) {
            moveOp.itemCount += addOp.itemCount;
        }
        addOp.positionStart += offset;
        list.set(move, addOp);
        list.set(add, moveOp);
    }

    void swapMoveUpdate(List<UpdateOp> list, int move, UpdateOp moveOp, int update, UpdateOp updateOp) {
        UpdateOp extraUp1 = null;
        UpdateOp extraUp2 = null;
        if (moveOp.itemCount < updateOp.positionStart) {
            updateOp.positionStart--;
        } else if (moveOp.itemCount < updateOp.positionStart + updateOp.itemCount) {
            updateOp.itemCount--;
            extraUp1 = mCallback.obtainUpdateOp(UPDATE, moveOp.positionStart, 1, updateOp.payload);
        }
        if (moveOp.positionStart <= updateOp.positionStart) {
            updateOp.positionStart++;
        } else if (moveOp.positionStart < updateOp.positionStart + updateOp.itemCount) {
            final int remaining = updateOp.positionStart + updateOp.itemCount - moveOp.positionStart;
            extraUp2 = mCallback.obtainUpdateOp(UPDATE, moveOp.positionStart + 1, remaining, updateOp.payload);
            updateOp.itemCount -= remaining;
        }
        list.set(update, moveOp);
        if (updateOp.itemCount > 0) {
            list.set(move, updateOp);
        } else {
            list.remove(move);
            mCallback.recycleUpdateOp(updateOp);
        }
        if (extraUp1 != null) {
            list.add(move, extraUp1);
        }
        if (extraUp2 != null) {
            list.add(move, extraUp2);
        }
    }

    private int getLastMoveOutOfOrder(List<UpdateOp> list) {
        boolean foundNonMove = false;
        for (int i = list.size() - 1; i >= 0; i--) {
            final UpdateOp op1 = list.get(i);
            if (op1.cmd == MOVE) {
                if (foundNonMove) {
                    return i;
                }
            } else {
                foundNonMove = true;
            }
        }
        return -1;
    }

    interface Callback {

        UpdateOp obtainUpdateOp(int cmd, int startPosition, int itemCount, Object payload);

        void recycleUpdateOp(UpdateOp op);
    }
}
