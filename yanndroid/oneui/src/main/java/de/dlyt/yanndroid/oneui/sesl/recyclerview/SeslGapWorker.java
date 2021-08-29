package de.dlyt.yanndroid.oneui.sesl.recyclerview;

import android.view.View;

import androidx.core.os.TraceCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;

import de.dlyt.yanndroid.oneui.view.RecyclerView;

public final class SeslGapWorker implements Runnable {
    public static final ThreadLocal<SeslGapWorker> sGapWorker = new ThreadLocal<>();
    static Comparator<Task> sTaskComparator = new Comparator<Task>() {
        @Override
        public int compare(Task lhs, Task rhs) {
            if ((lhs.view == null) != (rhs.view == null)) {
                return lhs.view == null ? 1 : -1;
            }

            if (lhs.immediate != rhs.immediate) {
                return lhs.immediate ? -1 : 1;
            }

            int deltaViewVelocity = rhs.viewVelocity - lhs.viewVelocity;
            if (deltaViewVelocity != 0) return deltaViewVelocity;

            int deltaDistanceToItem = lhs.distanceToItem - rhs.distanceToItem;
            if (deltaDistanceToItem != 0) return deltaDistanceToItem;

            return 0;
        }
    };
    ArrayList<RecyclerView> mRecyclerViews = new ArrayList<>();
    long mPostTimeNs;
    public long mFrameIntervalNs;
    private ArrayList<Task> mTasks = new ArrayList<>();

    static boolean isPrefetchPositionAttached(RecyclerView view, int position) {
        final int childCount = view.mChildHelper.getUnfilteredChildCount();
        for (int i = 0; i < childCount; i++) {
            View attachedView = view.mChildHelper.getUnfilteredChildAt(i);
            RecyclerView.ViewHolder holder = RecyclerView.getChildViewHolderInt(attachedView);
            if (holder.mPosition == position && !holder.isInvalid()) {
                return true;
            }
        }
        return false;
    }

    public void add(RecyclerView recyclerView) {
        if (RecyclerView.DEBUG && mRecyclerViews.contains(recyclerView)) {
            throw new IllegalStateException("SeslRecyclerView already present in worker list!");
        }
        mRecyclerViews.add(recyclerView);
    }

    public void remove(RecyclerView recyclerView) {
        boolean removeSuccess = mRecyclerViews.remove(recyclerView);
        if (RecyclerView.DEBUG && !removeSuccess) {
            throw new IllegalStateException("SeslRecyclerView removal failed!");
        }
    }

    public void postFromTraversal(RecyclerView recyclerView, int prefetchDx, int prefetchDy) {
        if (recyclerView.isAttachedToWindow()) {
            if (RecyclerView.DEBUG && !mRecyclerViews.contains(recyclerView)) {
                throw new IllegalStateException("attempting to post unregistered view!");
            }
            if (mPostTimeNs == 0) {
                mPostTimeNs = recyclerView.getNanoTime();
                recyclerView.post(this);
            }
        }

        recyclerView.mPrefetchRegistry.setPrefetchVector(prefetchDx, prefetchDy);
    }

    private void buildTaskList() {
        final int viewCount = mRecyclerViews.size();
        int totalTaskCount = 0;
        for (int i = 0; i < viewCount; i++) {
            RecyclerView view = mRecyclerViews.get(i);
            if (view.getWindowVisibility() == View.VISIBLE) {
                view.mPrefetchRegistry.collectPrefetchPositionsFromView(view, false);
                totalTaskCount += view.mPrefetchRegistry.mCount;
            }
        }

        mTasks.ensureCapacity(totalTaskCount);
        int totalTaskIndex = 0;
        for (int i = 0; i < viewCount; i++) {
            RecyclerView view = mRecyclerViews.get(i);
            if (view.getWindowVisibility() != View.VISIBLE) {
                continue;
            }

            LayoutPrefetchRegistryImpl prefetchRegistry = view.mPrefetchRegistry;
            final int viewVelocity = Math.abs(prefetchRegistry.mPrefetchDx) + Math.abs(prefetchRegistry.mPrefetchDy);
            for (int j = 0; j < prefetchRegistry.mCount * 2; j += 2) {
                final Task task;
                if (totalTaskIndex >= mTasks.size()) {
                    task = new Task();
                    mTasks.add(task);
                } else {
                    task = mTasks.get(totalTaskIndex);
                }
                final int distanceToItem = prefetchRegistry.mPrefetchArray[j + 1];

                task.immediate = distanceToItem <= viewVelocity;
                task.viewVelocity = viewVelocity;
                task.distanceToItem = distanceToItem;
                task.view = view;
                task.position = prefetchRegistry.mPrefetchArray[j];

                totalTaskIndex++;
            }
        }

        Collections.sort(mTasks, sTaskComparator);
    }

    private RecyclerView.ViewHolder prefetchPositionWithDeadline(RecyclerView view, int position, long deadlineNs) {
        if (isPrefetchPositionAttached(view, position)) {
            return null;
        }

        RecyclerView.Recycler recycler = view.mRecycler;
        RecyclerView.ViewHolder holder;
        try {
            view.onEnterLayoutOrScroll();
            holder = recycler.tryGetViewHolderForPositionByDeadline(position, false, deadlineNs);

            if (holder != null) {
                if (holder.isBound() && !holder.isInvalid()) {
                    recycler.recycleView(holder.itemView);
                } else {
                    recycler.addViewHolderToRecycledViewPool(holder, false);
                }
            }
        } finally {
            view.onExitLayoutOrScroll(false);
        }
        return holder;
    }

    private void prefetchInnerRecyclerViewWithDeadline(RecyclerView innerView, long deadlineNs) {
        if (innerView == null) {
            return;
        }

        if (innerView.mDataSetHasChangedAfterLayout && innerView.mChildHelper.getUnfilteredChildCount() != 0) {
            innerView.removeAndRecycleViews();
        }

        final LayoutPrefetchRegistryImpl innerPrefetchRegistry = innerView.mPrefetchRegistry;
        innerPrefetchRegistry.collectPrefetchPositionsFromView(innerView, true);

        if (innerPrefetchRegistry.mCount != 0) {
            try {
                TraceCompat.beginSection(RecyclerView.TRACE_NESTED_PREFETCH_TAG);
                innerView.mState.prepareForNestedPrefetch(innerView.mAdapter);
                for (int i = 0; i < innerPrefetchRegistry.mCount * 2; i += 2) {
                    final int innerPosition = innerPrefetchRegistry.mPrefetchArray[i];
                    prefetchPositionWithDeadline(innerView, innerPosition, deadlineNs);
                }
            } finally {
                TraceCompat.endSection();
            }
        }
    }

    private void flushTaskWithDeadline(Task task, long deadlineNs) {
        long taskDeadlineNs = task.immediate ? RecyclerView.FOREVER_NS : deadlineNs;
        RecyclerView.ViewHolder holder = prefetchPositionWithDeadline(task.view, task.position, taskDeadlineNs);
        if (holder != null && holder.mNestedRecyclerView != null && holder.isBound() && !holder.isInvalid()) {
            prefetchInnerRecyclerViewWithDeadline(holder.mNestedRecyclerView.get(), deadlineNs);
        }
    }

    private void flushTasksWithDeadline(long deadlineNs) {
        for (int i = 0; i < mTasks.size(); i++) {
            final Task task = mTasks.get(i);
            if (task.view == null) {
                break;
            }
            flushTaskWithDeadline(task, deadlineNs);
            task.clear();
        }
    }

    void prefetch(long deadlineNs) {
        buildTaskList();
        flushTasksWithDeadline(deadlineNs);
    }

    @Override
    public void run() {
        try {
            TraceCompat.beginSection(RecyclerView.TRACE_PREFETCH_TAG);

            if (mRecyclerViews.isEmpty()) {
                return;
            }

            final int size = mRecyclerViews.size();
            long latestFrameVsyncMs = 0;
            for (int i = 0; i < size; i++) {
                RecyclerView view = mRecyclerViews.get(i);
                if (view.getWindowVisibility() == View.VISIBLE) {
                    latestFrameVsyncMs = Math.max(view.getDrawingTime(), latestFrameVsyncMs);
                }
            }

            if (latestFrameVsyncMs == 0) {
                return;
            }

            long nextFrameNs = TimeUnit.MILLISECONDS.toNanos(latestFrameVsyncMs) + mFrameIntervalNs;

            prefetch(nextFrameNs);
        } finally {
            mPostTimeNs = 0;
            TraceCompat.endSection();
        }
    }

    static class Task {
        public boolean immediate;
        public int viewVelocity;
        public int distanceToItem;
        public RecyclerView view;
        public int position;

        public void clear() {
            immediate = false;
            viewVelocity = 0;
            distanceToItem = 0;
            view = null;
            position = 0;
        }
    }

    public static class LayoutPrefetchRegistryImpl implements RecyclerView.LayoutManager.LayoutPrefetchRegistry {
        int mPrefetchDx;
        int mPrefetchDy;
        int[] mPrefetchArray;

        int mCount;

        void setPrefetchVector(int dx, int dy) {
            mPrefetchDx = dx;
            mPrefetchDy = dy;
        }

        void collectPrefetchPositionsFromView(RecyclerView view, boolean nested) {
            mCount = 0;
            if (mPrefetchArray != null) {
                Arrays.fill(mPrefetchArray, -1);
            }

            final RecyclerView.LayoutManager layout = view.mLayout;
            if (view.mAdapter != null && layout != null && layout.isItemPrefetchEnabled()) {
                if (nested) {
                    if (!view.mAdapterHelper.hasPendingUpdates()) {
                        layout.collectInitialPrefetchPositions(view.mAdapter.getItemCount(), this);
                    }
                } else {
                    if (!view.hasPendingAdapterUpdates()) {
                        layout.collectAdjacentPrefetchPositions(mPrefetchDx, mPrefetchDy, view.mState, this);
                    }
                }

                if (mCount > layout.mPrefetchMaxCountObserved) {
                    layout.mPrefetchMaxCountObserved = mCount;
                    layout.mPrefetchMaxObservedInInitialPrefetch = nested;
                    view.mRecycler.updateViewCacheSize();
                }
            }
        }

        @Override
        public void addPosition(int layoutPosition, int pixelDistance) {
            if (layoutPosition < 0) {
                throw new IllegalArgumentException("Layout positions must be non-negative");
            }

            if (pixelDistance < 0) {
                throw new IllegalArgumentException("Pixel distance must be non-negative");
            }

            final int storagePosition = mCount * 2;
            if (mPrefetchArray == null) {
                mPrefetchArray = new int[4];
                Arrays.fill(mPrefetchArray, -1);
            } else if (storagePosition >= mPrefetchArray.length) {
                final int[] oldArray = mPrefetchArray;
                mPrefetchArray = new int[storagePosition * 2];
                System.arraycopy(oldArray, 0, mPrefetchArray, 0, oldArray.length);
            }

            mPrefetchArray[storagePosition] = layoutPosition;
            mPrefetchArray[storagePosition + 1] = pixelDistance;

            mCount++;
        }

        public boolean lastPrefetchIncludedPosition(int position) {
            if (mPrefetchArray != null) {
                final int count = mCount * 2;
                for (int i = 0; i < count; i += 2) {
                    if (mPrefetchArray[i] == position) return true;
                }
            }
            return false;
        }

        public void clearPrefetchPositions() {
            if (mPrefetchArray != null) {
                Arrays.fill(mPrefetchArray, -1);
            }
            mCount = 0;
        }
    }
}
