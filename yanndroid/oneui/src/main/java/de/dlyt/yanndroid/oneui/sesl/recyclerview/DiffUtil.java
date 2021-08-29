package de.dlyt.yanndroid.oneui.sesl.recyclerview;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.dlyt.yanndroid.oneui.view.RecyclerView;

public class DiffUtil {
    private static final Comparator<Snake> SNAKE_COMPARATOR = new Comparator<Snake>() {
        @Override
        public int compare(Snake o1, Snake o2) {
            int cmpX = o1.x - o2.x;
            return cmpX == 0 ? o1.y - o2.y : cmpX;
        }
    };

    private DiffUtil() {
    }

    @NonNull
    public static DiffResult calculateDiff(@NonNull Callback cb) {
        return calculateDiff(cb, true);
    }

    @NonNull
    public static DiffResult calculateDiff(@NonNull Callback cb, boolean detectMoves) {
        final int oldSize = cb.getOldListSize();
        final int newSize = cb.getNewListSize();

        final List<Snake> snakes = new ArrayList<>();

        final List<Range> stack = new ArrayList<>();

        stack.add(new Range(0, oldSize, 0, newSize));

        final int max = oldSize + newSize + Math.abs(oldSize - newSize);
        final int[] forward = new int[max * 2];
        final int[] backward = new int[max * 2];

        final List<Range> rangePool = new ArrayList<>();
        while (!stack.isEmpty()) {
            final Range range = stack.remove(stack.size() - 1);
            final Snake snake = diffPartial(cb, range.oldListStart, range.oldListEnd, range.newListStart, range.newListEnd, forward, backward, max);
            if (snake != null) {
                if (snake.size > 0) {
                    snakes.add(snake);
                }
                snake.x += range.oldListStart;
                snake.y += range.newListStart;

                final Range left = rangePool.isEmpty() ? new Range() : rangePool.remove(rangePool.size() - 1);
                left.oldListStart = range.oldListStart;
                left.newListStart = range.newListStart;
                if (snake.reverse) {
                    left.oldListEnd = snake.x;
                    left.newListEnd = snake.y;
                } else {
                    if (snake.removal) {
                        left.oldListEnd = snake.x - 1;
                        left.newListEnd = snake.y;
                    } else {
                        left.oldListEnd = snake.x;
                        left.newListEnd = snake.y - 1;
                    }
                }
                stack.add(left);

                final Range right = range;
                if (snake.reverse) {
                    if (snake.removal) {
                        right.oldListStart = snake.x + snake.size + 1;
                        right.newListStart = snake.y + snake.size;
                    } else {
                        right.oldListStart = snake.x + snake.size;
                        right.newListStart = snake.y + snake.size + 1;
                    }
                } else {
                    right.oldListStart = snake.x + snake.size;
                    right.newListStart = snake.y + snake.size;
                }
                stack.add(right);
            } else {
                rangePool.add(range);
            }

        }
        Collections.sort(snakes, SNAKE_COMPARATOR);

        return new DiffResult(cb, snakes, forward, backward, detectMoves);

    }

    private static Snake diffPartial(Callback cb, int startOld, int endOld, int startNew, int endNew, int[] forward, int[] backward, int kOffset) {
        final int oldSize = endOld - startOld;
        final int newSize = endNew - startNew;

        if (endOld - startOld < 1 || endNew - startNew < 1) {
            return null;
        }

        final int delta = oldSize - newSize;
        final int dLimit = (oldSize + newSize + 1) / 2;
        Arrays.fill(forward, kOffset - dLimit - 1, kOffset + dLimit + 1, 0);
        Arrays.fill(backward, kOffset - dLimit - 1 + delta, kOffset + dLimit + 1 + delta, oldSize);
        final boolean checkInFwd = delta % 2 != 0;
        for (int d = 0; d <= dLimit; d++) {
            for (int k = -d; k <= d; k += 2) {
                int x;
                final boolean removal;
                if (k == -d || (k != d && forward[kOffset + k - 1] < forward[kOffset + k + 1])) {
                    x = forward[kOffset + k + 1];
                    removal = false;
                } else {
                    x = forward[kOffset + k - 1] + 1;
                    removal = true;
                }
                int y = x - k;
                while (x < oldSize && y < newSize && cb.areItemsTheSame(startOld + x, startNew + y)) {
                    x++;
                    y++;
                }
                forward[kOffset + k] = x;
                if (checkInFwd && k >= delta - d + 1 && k <= delta + d - 1) {
                    if (forward[kOffset + k] >= backward[kOffset + k]) {
                        Snake outSnake = new Snake();
                        outSnake.x = backward[kOffset + k];
                        outSnake.y = outSnake.x - k;
                        outSnake.size = forward[kOffset + k] - backward[kOffset + k];
                        outSnake.removal = removal;
                        outSnake.reverse = false;
                        return outSnake;
                    }
                }
            }
            for (int k = -d; k <= d; k += 2) {
                final int backwardK = k + delta;
                int x;
                final boolean removal;
                if (backwardK == d + delta || (backwardK != -d + delta && backward[kOffset + backwardK - 1] < backward[kOffset + backwardK + 1])) {
                    x = backward[kOffset + backwardK - 1];
                    removal = false;
                } else {
                    x = backward[kOffset + backwardK + 1] - 1;
                    removal = true;
                }

                int y = x - backwardK;
                while (x > 0 && y > 0 && cb.areItemsTheSame(startOld + x - 1, startNew + y - 1)) {
                    x--;
                    y--;
                }
                backward[kOffset + backwardK] = x;
                if (!checkInFwd && k + delta >= -d && k + delta <= d) {
                    if (forward[kOffset + backwardK] >= backward[kOffset + backwardK]) {
                        Snake outSnake = new Snake();
                        outSnake.x = backward[kOffset + backwardK];
                        outSnake.y = outSnake.x - backwardK;
                        outSnake.size = forward[kOffset + backwardK] - backward[kOffset + backwardK];
                        outSnake.removal = removal;
                        outSnake.reverse = true;
                        return outSnake;
                    }
                }
            }
        }
        throw new IllegalStateException("DiffUtil hit an unexpected case while trying to calculate" + " the optimal path. Please make sure your data is not changing during the" + " diff calculation.");
    }

    public abstract static class Callback {
        public abstract int getOldListSize();

        public abstract int getNewListSize();

        public abstract boolean areItemsTheSame(int oldItemPosition, int newItemPosition);

        public abstract boolean areContentsTheSame(int oldItemPosition, int newItemPosition);

        @Nullable
        public Object getChangePayload(int oldItemPosition, int newItemPosition) {
            return null;
        }
    }

    public abstract static class ItemCallback<T> {
        public abstract boolean areItemsTheSame(@NonNull T oldItem, @NonNull T newItem);

        public abstract boolean areContentsTheSame(@NonNull T oldItem, @NonNull T newItem);

        @SuppressWarnings({"WeakerAccess", "unused"})
        @Nullable
        public Object getChangePayload(@NonNull T oldItem, @NonNull T newItem) {
            return null;
        }
    }

    static class Snake {
        int x;
        int y;
        int size;
        boolean removal;
        boolean reverse;
    }

    static class Range {
        int oldListStart, oldListEnd;
        int newListStart, newListEnd;

        public Range() {
        }

        public Range(int oldListStart, int oldListEnd, int newListStart, int newListEnd) {
            this.oldListStart = oldListStart;
            this.oldListEnd = oldListEnd;
            this.newListStart = newListStart;
            this.newListEnd = newListEnd;
        }
    }

    public static class DiffResult {
        public static final int NO_POSITION = -1;
        private static final int FLAG_NOT_CHANGED = 1;
        private static final int FLAG_CHANGED = FLAG_NOT_CHANGED << 1;
        private static final int FLAG_MOVED_CHANGED = FLAG_CHANGED << 1;
        private static final int FLAG_MOVED_NOT_CHANGED = FLAG_MOVED_CHANGED << 1;
        private static final int FLAG_IGNORE = FLAG_MOVED_NOT_CHANGED << 1;
        private static final int FLAG_OFFSET = 5;
        private static final int FLAG_MASK = (1 << FLAG_OFFSET) - 1;
        private final List<Snake> mSnakes;
        private final int[] mOldItemStatuses;
        private final int[] mNewItemStatuses;
        private final Callback mCallback;
        private final int mOldListSize;
        private final int mNewListSize;
        private final boolean mDetectMoves;

        DiffResult(Callback callback, List<Snake> snakes, int[] oldItemStatuses, int[] newItemStatuses, boolean detectMoves) {
            mSnakes = snakes;
            mOldItemStatuses = oldItemStatuses;
            mNewItemStatuses = newItemStatuses;
            Arrays.fill(mOldItemStatuses, 0);
            Arrays.fill(mNewItemStatuses, 0);
            mCallback = callback;
            mOldListSize = callback.getOldListSize();
            mNewListSize = callback.getNewListSize();
            mDetectMoves = detectMoves;
            addRootSnake();
            findMatchingItems();
        }

        private static PostponedUpdate removePostponedUpdate(List<PostponedUpdate> updates, int pos, boolean removal) {
            for (int i = updates.size() - 1; i >= 0; i--) {
                final PostponedUpdate update = updates.get(i);
                if (update.posInOwnerList == pos && update.removal == removal) {
                    updates.remove(i);
                    for (int j = i; j < updates.size(); j++) {
                        updates.get(j).currentPos += removal ? 1 : -1;
                    }
                    return update;
                }
            }
            return null;
        }

        private void addRootSnake() {
            Snake firstSnake = mSnakes.isEmpty() ? null : mSnakes.get(0);
            if (firstSnake == null || firstSnake.x != 0 || firstSnake.y != 0) {
                Snake root = new Snake();
                root.x = 0;
                root.y = 0;
                root.removal = false;
                root.size = 0;
                root.reverse = false;
                mSnakes.add(0, root);
            }
        }

        private void findMatchingItems() {
            int posOld = mOldListSize;
            int posNew = mNewListSize;
            for (int i = mSnakes.size() - 1; i >= 0; i--) {
                final Snake snake = mSnakes.get(i);
                final int endX = snake.x + snake.size;
                final int endY = snake.y + snake.size;
                if (mDetectMoves) {
                    while (posOld > endX) {
                        findAddition(posOld, posNew, i);
                        posOld--;
                    }
                    while (posNew > endY) {
                        findRemoval(posOld, posNew, i);
                        posNew--;
                    }
                }
                for (int j = 0; j < snake.size; j++) {
                    final int oldItemPos = snake.x + j;
                    final int newItemPos = snake.y + j;
                    final boolean theSame = mCallback.areContentsTheSame(oldItemPos, newItemPos);
                    final int changeFlag = theSame ? FLAG_NOT_CHANGED : FLAG_CHANGED;
                    mOldItemStatuses[oldItemPos] = (newItemPos << FLAG_OFFSET) | changeFlag;
                    mNewItemStatuses[newItemPos] = (oldItemPos << FLAG_OFFSET) | changeFlag;
                }
                posOld = snake.x;
                posNew = snake.y;
            }
        }

        private void findAddition(int x, int y, int snakeIndex) {
            if (mOldItemStatuses[x - 1] != 0) {
                return;
            }
            findMatchingItem(x, y, snakeIndex, false);
        }

        private void findRemoval(int x, int y, int snakeIndex) {
            if (mNewItemStatuses[y - 1] != 0) {
                return;
            }
            findMatchingItem(x, y, snakeIndex, true);
        }

        public int convertOldPositionToNew(@IntRange(from = 0) int oldListPosition) {
            if (oldListPosition < 0 || oldListPosition >= mOldListSize) {
                throw new IndexOutOfBoundsException("Index out of bounds - passed position = " + oldListPosition + ", old list size = " + mOldListSize);
            }
            final int status = mOldItemStatuses[oldListPosition];
            if ((status & FLAG_MASK) == 0) {
                return NO_POSITION;
            } else {
                return status >> FLAG_OFFSET;
            }
        }

        public int convertNewPositionToOld(@IntRange(from = 0) int newListPosition) {
            if (newListPosition < 0 || newListPosition >= mNewListSize) {
                throw new IndexOutOfBoundsException("Index out of bounds - passed position = " + newListPosition + ", new list size = " + mNewListSize);
            }
            final int status = mNewItemStatuses[newListPosition];
            if ((status & FLAG_MASK) == 0) {
                return NO_POSITION;
            } else {
                return status >> FLAG_OFFSET;
            }
        }

        private boolean findMatchingItem(final int x, final int y, final int snakeIndex, final boolean removal) {
            final int myItemPos;
            int curX;
            int curY;
            if (removal) {
                myItemPos = y - 1;
                curX = x;
                curY = y - 1;
            } else {
                myItemPos = x - 1;
                curX = x - 1;
                curY = y;
            }
            for (int i = snakeIndex; i >= 0; i--) {
                final Snake snake = mSnakes.get(i);
                final int endX = snake.x + snake.size;
                final int endY = snake.y + snake.size;
                if (removal) {
                    for (int pos = curX - 1; pos >= endX; pos--) {
                        if (mCallback.areItemsTheSame(pos, myItemPos)) {
                            final boolean theSame = mCallback.areContentsTheSame(pos, myItemPos);
                            final int changeFlag = theSame ? FLAG_MOVED_NOT_CHANGED : FLAG_MOVED_CHANGED;
                            mNewItemStatuses[myItemPos] = (pos << FLAG_OFFSET) | FLAG_IGNORE;
                            mOldItemStatuses[pos] = (myItemPos << FLAG_OFFSET) | changeFlag;
                            return true;
                        }
                    }
                } else {
                    for (int pos = curY - 1; pos >= endY; pos--) {
                        if (mCallback.areItemsTheSame(myItemPos, pos)) {
                            final boolean theSame = mCallback.areContentsTheSame(myItemPos, pos);
                            final int changeFlag = theSame ? FLAG_MOVED_NOT_CHANGED : FLAG_MOVED_CHANGED;
                            mOldItemStatuses[x - 1] = (pos << FLAG_OFFSET) | FLAG_IGNORE;
                            mNewItemStatuses[pos] = ((x - 1) << FLAG_OFFSET) | changeFlag;
                            return true;
                        }
                    }
                }
                curX = snake.x;
                curY = snake.y;
            }
            return false;
        }

        public void dispatchUpdatesTo(@NonNull final RecyclerView.Adapter adapter) {
            dispatchUpdatesTo(new AdapterListUpdateCallback(adapter));
        }

        public void dispatchUpdatesTo(@NonNull ListUpdateCallback updateCallback) {
            final BatchingListUpdateCallback batchingCallback;
            if (updateCallback instanceof BatchingListUpdateCallback) {
                batchingCallback = (BatchingListUpdateCallback) updateCallback;
            } else {
                batchingCallback = new BatchingListUpdateCallback(updateCallback);
                updateCallback = batchingCallback;
            }
            final List<PostponedUpdate> postponedUpdates = new ArrayList<>();
            int posOld = mOldListSize;
            int posNew = mNewListSize;
            for (int snakeIndex = mSnakes.size() - 1; snakeIndex >= 0; snakeIndex--) {
                final Snake snake = mSnakes.get(snakeIndex);
                final int snakeSize = snake.size;
                final int endX = snake.x + snakeSize;
                final int endY = snake.y + snakeSize;
                if (endX < posOld) {
                    dispatchRemovals(postponedUpdates, batchingCallback, endX, posOld - endX, endX);
                }

                if (endY < posNew) {
                    dispatchAdditions(postponedUpdates, batchingCallback, endX, posNew - endY, endY);
                }
                for (int i = snakeSize - 1; i >= 0; i--) {
                    if ((mOldItemStatuses[snake.x + i] & FLAG_MASK) == FLAG_CHANGED) {
                        batchingCallback.onChanged(snake.x + i, 1, mCallback.getChangePayload(snake.x + i, snake.y + i));
                    }
                }
                posOld = snake.x;
                posNew = snake.y;
            }
            batchingCallback.dispatchLastEvent();
        }

        private void dispatchAdditions(List<PostponedUpdate> postponedUpdates, ListUpdateCallback updateCallback, int start, int count, int globalIndex) {
            if (!mDetectMoves) {
                updateCallback.onInserted(start, count);
                return;
            }
            for (int i = count - 1; i >= 0; i--) {
                int status = mNewItemStatuses[globalIndex + i] & FLAG_MASK;
                switch (status) {
                    case 0:
                        updateCallback.onInserted(start, 1);
                        for (PostponedUpdate update : postponedUpdates) {
                            update.currentPos += 1;
                        }
                        break;
                    case FLAG_MOVED_CHANGED:
                    case FLAG_MOVED_NOT_CHANGED:
                        final int pos = mNewItemStatuses[globalIndex + i] >> FLAG_OFFSET;
                        final PostponedUpdate update = removePostponedUpdate(postponedUpdates, pos, true);
                        updateCallback.onMoved(update.currentPos, start);
                        if (status == FLAG_MOVED_CHANGED) {
                            updateCallback.onChanged(start, 1, mCallback.getChangePayload(pos, globalIndex + i));
                        }
                        break;
                    case FLAG_IGNORE:
                        postponedUpdates.add(new PostponedUpdate(globalIndex + i, start, false));
                        break;
                    default:
                        throw new IllegalStateException("unknown flag for pos " + (globalIndex + i) + " " + Long.toBinaryString(status));
                }
            }
        }

        private void dispatchRemovals(List<PostponedUpdate> postponedUpdates, ListUpdateCallback updateCallback, int start, int count, int globalIndex) {
            if (!mDetectMoves) {
                updateCallback.onRemoved(start, count);
                return;
            }
            for (int i = count - 1; i >= 0; i--) {
                final int status = mOldItemStatuses[globalIndex + i] & FLAG_MASK;
                switch (status) {
                    case 0:
                        updateCallback.onRemoved(start + i, 1);
                        for (PostponedUpdate update : postponedUpdates) {
                            update.currentPos -= 1;
                        }
                        break;
                    case FLAG_MOVED_CHANGED:
                    case FLAG_MOVED_NOT_CHANGED:
                        final int pos = mOldItemStatuses[globalIndex + i] >> FLAG_OFFSET;
                        final PostponedUpdate update = removePostponedUpdate(postponedUpdates, pos, false);
                        updateCallback.onMoved(start + i, update.currentPos - 1);
                        if (status == FLAG_MOVED_CHANGED) {
                            updateCallback.onChanged(update.currentPos - 1, 1, mCallback.getChangePayload(globalIndex + i, pos));
                        }
                        break;
                    case FLAG_IGNORE:
                        postponedUpdates.add(new PostponedUpdate(globalIndex + i, start + i, true));
                        break;
                    default:
                        throw new IllegalStateException("unknown flag for pos " + (globalIndex + i) + " " + Long.toBinaryString(status));
                }
            }
        }

        @VisibleForTesting
        List<Snake> getSnakes() {
            return mSnakes;
        }
    }

    private static class PostponedUpdate {
        int posInOwnerList;
        int currentPos;
        boolean removal;

        public PostponedUpdate(int posInOwnerList, int currentPos, boolean removal) {
            this.posInOwnerList = posInOwnerList;
            this.currentPos = currentPos;
            this.removal = removal;
        }
    }
}
