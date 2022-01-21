package de.dlyt.yanndroid.oneui.sesl.recyclerview;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import de.dlyt.yanndroid.oneui.view.RecyclerView;

public class DiffUtil {
    private static final Comparator<Diagonal> DIAGONAL_COMPARATOR = new Comparator<Diagonal>() {
        @Override
        public int compare(Diagonal o1, Diagonal o2) {
            return o1.x - o2.x;
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

        final List<Diagonal> diagonals = new ArrayList<>();

        final List<Range> stack = new ArrayList<>();

        stack.add(new Range(0, oldSize, 0, newSize));

        final int max = (oldSize + newSize + 1) / 2;
        final CenteredArray forward = new CenteredArray(max * 2 + 1);
        final CenteredArray backward = new CenteredArray(max * 2 + 1);

        final List<Range> rangePool = new ArrayList<>();
        while (!stack.isEmpty()) {
            final Range range = stack.remove(stack.size() - 1);
            final Snake snake = midPoint(range, cb, forward, backward);
            if (snake != null) {
                if (snake.diagonalSize() > 0) {
                    diagonals.add(snake.toDiagonal());
                }
                final Range left = rangePool.isEmpty() ? new Range() : rangePool.remove(rangePool.size() - 1);
                left.oldListStart = range.oldListStart;
                left.newListStart = range.newListStart;
                left.oldListEnd = snake.startX;
                left.newListEnd = snake.startY;
                stack.add(left);

                final Range right = range;
                right.oldListEnd = range.oldListEnd;
                right.newListEnd = range.newListEnd;
                right.oldListStart = snake.endX;
                right.newListStart = snake.endY;
                stack.add(right);
            } else {
                rangePool.add(range);
            }

        }
        Collections.sort(diagonals, DIAGONAL_COMPARATOR);

        return new DiffResult(cb, diagonals, forward.backingData(), backward.backingData(), detectMoves);
    }

    @Nullable
    private static Snake midPoint(Range range, Callback cb, CenteredArray forward, CenteredArray backward) {
        if (range.oldSize() < 1 || range.newSize() < 1) {
            return null;
        }
        int max = (range.oldSize() + range.newSize() + 1) / 2;
        forward.set(1, range.oldListStart);
        backward.set(1, range.oldListEnd);
        for (int d = 0; d < max; d++) {
            Snake snake = forward(range, cb, forward, backward, d);
            if (snake != null) {
                return snake;
            }
            snake = backward(range, cb, forward, backward, d);
            if (snake != null) {
                return snake;
            }
        }
        return null;
    }

    @Nullable
    private static Snake forward(Range range, Callback cb, CenteredArray forward, CenteredArray backward, int d) {
        boolean checkForSnake = Math.abs(range.oldSize() - range.newSize()) % 2 == 1;
        int delta = range.oldSize() - range.newSize();
        for (int k = -d; k <= d; k += 2) {
            final int startX;
            final int startY;
            int x, y;
            if (k == -d || (k != d && forward.get(k + 1) > forward.get(k - 1))) {
                x = startX = forward.get(k + 1);
            } else {
                startX = forward.get(k - 1);
                x = startX + 1;
            }
            y = range.newListStart + (x - range.oldListStart) - k;
            startY = (d == 0 || x != startX) ? y : y - 1;
            while (x < range.oldListEnd && y < range.newListEnd && cb.areItemsTheSame(x, y)) {
                x++;
                y++;
            }
            forward.set(k, x);
            if (checkForSnake) {
                int backwardsK = delta - k;
                if (backwardsK >= -d + 1 && backwardsK <= d - 1 && backward.get(backwardsK) <= x) {
                    Snake snake = new Snake();
                    snake.startX = startX;
                    snake.startY = startY;
                    snake.endX = x;
                    snake.endY = y;
                    snake.reverse = false;
                    return snake;
                }
            }
        }
        return null;
    }

    @Nullable
    private static Snake backward(Range range, Callback cb, CenteredArray forward, CenteredArray backward, int d) {
        boolean checkForSnake = (range.oldSize() - range.newSize()) % 2 == 0;
        int delta = range.oldSize() - range.newSize();
        for (int k = -d; k <= d; k += 2) {
            final int startX;
            final int startY;
            int x, y;

            if (k == -d || (k != d && backward.get(k + 1) < backward.get(k - 1))) {
                x = startX = backward.get(k + 1);
            } else {
                startX = backward.get(k - 1);
                x = startX - 1;
            }
            y = range.newListEnd - ((range.oldListEnd - x) - k);
            startY = (d == 0 || x != startX) ? y : y + 1;
            while (x > range.oldListStart && y > range.newListStart && cb.areItemsTheSame(x - 1, y - 1)) {
                x--;
                y--;
            }
            backward.set(k, x);
            if (checkForSnake) {
                int forwardsK = delta - k;
                if (forwardsK >= -d && forwardsK <= d && forward.get(forwardsK) >= x) {
                    Snake snake = new Snake();
                    snake.startX = x;
                    snake.startY = y;
                    snake.endX = startX;
                    snake.endY = startY;
                    snake.reverse = true;
                    return snake;
                }
            }
        }
        return null;
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

        @SuppressWarnings({"unused"})
        @Nullable
        public Object getChangePayload(@NonNull T oldItem, @NonNull T newItem) {
            return null;
        }
    }

    static class Diagonal {
        public final int x;
        public final int y;
        public final int size;

        Diagonal(int x, int y, int size) {
            this.x = x;
            this.y = y;
            this.size = size;
        }

        int endX() {
            return x + size;
        }

        int endY() {
            return y + size;
        }
    }

    @SuppressWarnings("WeakerAccess")
    static class Snake {
        public int startX;
        public int startY;
        public int endX;
        public int endY;
        public boolean reverse;

        boolean hasAdditionOrRemoval() {
            return endY - startY != endX - startX;
        }

        boolean isAddition() {
            return endY - startY > endX - startX;
        }

        int diagonalSize() {
            return Math.min(endX - startX, endY - startY);
        }

        @NonNull
        Diagonal toDiagonal() {
            if (hasAdditionOrRemoval()) {
                if (reverse) {
                    return new Diagonal(startX, startY, diagonalSize());
                } else {
                    if (isAddition()) {
                        return new Diagonal(startX, startY + 1, diagonalSize());
                    } else {
                        return new Diagonal(startX + 1, startY, diagonalSize());
                    }
                }
            } else {
                return new Diagonal(startX, startY, endX - startX);
            }
        }
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

        int oldSize() {
            return oldListEnd - oldListStart;
        }

        int newSize() {
            return newListEnd - newListStart;
        }
    }

    public static class DiffResult {
        public static final int NO_POSITION = -1;
        private static final int FLAG_NOT_CHANGED = 1;
        private static final int FLAG_CHANGED = FLAG_NOT_CHANGED << 1;
        private static final int FLAG_MOVED_CHANGED = FLAG_CHANGED << 1;
        private static final int FLAG_MOVED_NOT_CHANGED = FLAG_MOVED_CHANGED << 1;
        private static final int FLAG_MOVED = FLAG_MOVED_CHANGED | FLAG_MOVED_NOT_CHANGED;
        private static final int FLAG_OFFSET = 4;
        private static final int FLAG_MASK = (1 << FLAG_OFFSET) - 1;

        private final List<Diagonal> mDiagonals;
        private final int[] mOldItemStatuses;
        private final int[] mNewItemStatuses;
        private final Callback mCallback;
        private final int mOldListSize;
        private final int mNewListSize;
        private final boolean mDetectMoves;

        DiffResult(Callback callback, List<Diagonal> diagonals, int[] oldItemStatuses, int[] newItemStatuses, boolean detectMoves) {
            mDiagonals = diagonals;
            mOldItemStatuses = oldItemStatuses;
            mNewItemStatuses = newItemStatuses;
            Arrays.fill(mOldItemStatuses, 0);
            Arrays.fill(mNewItemStatuses, 0);
            mCallback = callback;
            mOldListSize = callback.getOldListSize();
            mNewListSize = callback.getNewListSize();
            mDetectMoves = detectMoves;
            addEdgeDiagonals();
            findMatchingItems();
        }

        private void addEdgeDiagonals() {
            Diagonal first = mDiagonals.isEmpty() ? null : mDiagonals.get(0);
            if (first == null || first.x != 0 || first.y != 0) {
                mDiagonals.add(0, new Diagonal(0, 0, 0));
            }
            mDiagonals.add(new Diagonal(mOldListSize, mNewListSize, 0));
        }

        private void findMatchingItems() {
            for (Diagonal diagonal : mDiagonals) {
                for (int offset = 0; offset < diagonal.size; offset++) {
                    int posX = diagonal.x + offset;
                    int posY = diagonal.y + offset;
                    final boolean theSame = mCallback.areContentsTheSame(posX, posY);
                    final int changeFlag = theSame ? FLAG_NOT_CHANGED : FLAG_CHANGED;
                    mOldItemStatuses[posX] = (posY << FLAG_OFFSET) | changeFlag;
                    mNewItemStatuses[posY] = (posX << FLAG_OFFSET) | changeFlag;
                }
            }
            if (mDetectMoves) {
                findMoveMatches();
            }
        }

        private void findMoveMatches() {
            int posX = 0;
            for (Diagonal diagonal : mDiagonals) {
                while (posX < diagonal.x) {
                    if (mOldItemStatuses[posX] == 0) {
                        findMatchingAddition(posX);
                    }
                    posX++;
                }
                posX = diagonal.endX();
            }
        }

        private void findMatchingAddition(int posX) {
            int posY = 0;
            final int diagonalsSize = mDiagonals.size();
            for (int i = 0; i < diagonalsSize; i++) {
                final Diagonal diagonal = mDiagonals.get(i);
                while (posY < diagonal.y) {
                    if (mNewItemStatuses[posY] == 0) {
                        boolean matching = mCallback.areItemsTheSame(posX, posY);
                        if (matching) {
                            boolean contentsMatching = mCallback.areContentsTheSame(posX, posY);
                            final int changeFlag = contentsMatching ? FLAG_MOVED_NOT_CHANGED : FLAG_MOVED_CHANGED;
                            mOldItemStatuses[posX] = (posY << FLAG_OFFSET) | changeFlag;
                            mNewItemStatuses[posY] = (posX << FLAG_OFFSET) | changeFlag;
                            return;
                        }
                    }
                    posY++;
                }
                posY = diagonal.endY();
            }
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
            int currentListSize = mOldListSize;
            final Collection<PostponedUpdate> postponedUpdates = new ArrayDeque<>();
            int posX = mOldListSize;
            int posY = mNewListSize;
            for (int diagonalIndex = mDiagonals.size() - 1; diagonalIndex >= 0; diagonalIndex--) {
                final Diagonal diagonal = mDiagonals.get(diagonalIndex);
                int endX = diagonal.endX();
                int endY = diagonal.endY();
                while (posX > endX) {
                    posX--;
                    int status = mOldItemStatuses[posX];
                    if ((status & FLAG_MOVED) != 0) {
                        int newPos = status >> FLAG_OFFSET;
                        PostponedUpdate postponedUpdate = getPostponedUpdate(postponedUpdates, newPos, false);
                        if (postponedUpdate != null) {
                            int updatedNewPos = currentListSize - postponedUpdate.currentPos;
                            batchingCallback.onMoved(posX, updatedNewPos - 1);
                            if ((status & FLAG_MOVED_CHANGED) != 0) {
                                Object changePayload = mCallback.getChangePayload(posX, newPos);
                                batchingCallback.onChanged(updatedNewPos - 1, 1, changePayload);
                            }
                        } else {
                            postponedUpdates.add(new PostponedUpdate(posX, currentListSize - posX - 1, true));
                        }
                    } else {
                        batchingCallback.onRemoved(posX, 1);
                        currentListSize--;
                    }
                }
                while (posY > endY) {
                    posY--;
                    int status = mNewItemStatuses[posY];
                    if ((status & FLAG_MOVED) != 0) {
                        int oldPos = status >> FLAG_OFFSET;
                        PostponedUpdate postponedUpdate = getPostponedUpdate(postponedUpdates, oldPos, true);
                        if (postponedUpdate == null) {
                            postponedUpdates.add(new PostponedUpdate(posY, currentListSize - posX, false));
                        } else {
                            int updatedOldPos = currentListSize - postponedUpdate.currentPos - 1;
                            batchingCallback.onMoved(updatedOldPos, posX);
                            if ((status & FLAG_MOVED_CHANGED) != 0) {
                                Object changePayload = mCallback.getChangePayload(oldPos, posY);
                                batchingCallback.onChanged(posX, 1, changePayload);
                            }
                        }
                    } else {
                        batchingCallback.onInserted(posX, 1);
                        currentListSize++;
                    }
                }
                posX = diagonal.x;
                posY = diagonal.y;
                for (int i = 0; i < diagonal.size; i++) {
                    if ((mOldItemStatuses[posX] & FLAG_MASK) == FLAG_CHANGED) {
                        Object changePayload = mCallback.getChangePayload(posX, posY);
                        batchingCallback.onChanged(posX, 1, changePayload);
                    }
                    posX++;
                    posY++;
                }
                posX = diagonal.x;
                posY = diagonal.y;
            }
            batchingCallback.dispatchLastEvent();
        }

        @Nullable
        private static PostponedUpdate getPostponedUpdate(Collection<PostponedUpdate> postponedUpdates, int posInList, boolean removal) {
            PostponedUpdate postponedUpdate = null;
            Iterator<PostponedUpdate> itr = postponedUpdates.iterator();
            while (itr.hasNext()) {
                PostponedUpdate update = itr.next();
                if (update.posInOwnerList == posInList && update.removal == removal) {
                    postponedUpdate = update;
                    itr.remove();
                    break;
                }
            }
            while (itr.hasNext()) {
                PostponedUpdate update = itr.next();
                if (removal) {
                    update.currentPos--;
                } else {
                    update.currentPos++;
                }
            }
            return postponedUpdate;
        }
    }

    private static class PostponedUpdate {
        int posInOwnerList;
        int currentPos;
        boolean removal;

        PostponedUpdate(int posInOwnerList, int currentPos, boolean removal) {
            this.posInOwnerList = posInOwnerList;
            this.currentPos = currentPos;
            this.removal = removal;
        }
    }

    static class CenteredArray {
        private final int[] mData;
        private final int mMid;

        CenteredArray(int size) {
            mData = new int[size];
            mMid = mData.length / 2;
        }

        int get(int index) {
            return mData[index + mMid];
        }

        int[] backingData() {
            return mData;
        }

        void set(int index, int value) {
            mData[index + mMid] = value;
        }

        public void fill(int value) {
            Arrays.fill(mData, value);
        }
    }
}
