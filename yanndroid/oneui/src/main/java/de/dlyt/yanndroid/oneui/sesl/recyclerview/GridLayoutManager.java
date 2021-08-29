package de.dlyt.yanndroid.oneui.sesl.recyclerview;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;

import java.util.Arrays;

import de.dlyt.yanndroid.oneui.view.RecyclerView;

public class GridLayoutManager extends SeslLinearLayoutManager {
    private static final boolean DEBUG = false;
    private static final String TAG = "GridLayoutManager";
    public static final int DEFAULT_SPAN_COUNT = -1;
    boolean mPendingSpanCountChange = false;
    int mSpanCount = DEFAULT_SPAN_COUNT;
    int [] mCachedBorders;
    View[] mSet;
    final SparseIntArray mPreLayoutSpanSizeCache = new SparseIntArray();
    final SparseIntArray mPreLayoutSpanIndexCache = new SparseIntArray();
    SpanSizeLookup mSpanSizeLookup = new DefaultSpanSizeLookup();
    final Rect mDecorInsets = new Rect();
    private boolean mUsingSpansToEstimateScrollBarDimensions;

    public GridLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        Properties properties = getProperties(context, attrs, defStyleAttr, defStyleRes);
        setSpanCount(properties.spanCount);
    }

    public GridLayoutManager(Context context, int spanCount) {
        super(context);
        setSpanCount(spanCount);
    }

    public GridLayoutManager(Context context, int spanCount, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
        setSpanCount(spanCount);
    }

    @Override
    public void setStackFromEnd(boolean stackFromEnd) {
        if (stackFromEnd) {
            throw new UnsupportedOperationException("GridLayoutManager does not support stack from end." + " Consider using reverse layout");
        }
        super.setStackFromEnd(false);
    }

    @Override
    public int getRowCountForAccessibility(RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (mOrientation == HORIZONTAL) {
            return mSpanCount;
        }
        if (state.getItemCount() < 1) {
            return 0;
        }

        return getSpanGroupIndex(recycler, state, state.getItemCount() - 1) + 1;
    }

    @Override
    public int getColumnCountForAccessibility(RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (mOrientation == VERTICAL) {
            return mSpanCount;
        }
        if (state.getItemCount() < 1) {
            return 0;
        }

        return getSpanGroupIndex(recycler, state, state.getItemCount() - 1) + 1;
    }

    @Override
    public void onInitializeAccessibilityNodeInfoForItem(RecyclerView.Recycler recycler, RecyclerView.State state, View host, AccessibilityNodeInfoCompat info) {
        ViewGroup.LayoutParams lp = host.getLayoutParams();
        if (!(lp instanceof LayoutParams)) {
            super.onInitializeAccessibilityNodeInfoForItem(host, info);
            return;
        }
        LayoutParams glp = (LayoutParams) lp;
        int spanGroupIndex = getSpanGroupIndex(recycler, state, glp.getViewLayoutPosition());
        if (mOrientation == HORIZONTAL) {
            info.setCollectionItemInfo(AccessibilityNodeInfoCompat.CollectionItemInfoCompat.obtain(glp.getSpanIndex(), glp.getSpanSize(), spanGroupIndex, 1, false, false));
        } else {
            info.setCollectionItemInfo(AccessibilityNodeInfoCompat.CollectionItemInfoCompat.obtain(spanGroupIndex , 1, glp.getSpanIndex(), glp.getSpanSize(), false, false));
        }
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (state.isPreLayout()) {
            cachePreLayoutSpanMapping();
        }
        super.onLayoutChildren(recycler, state);
        if (DEBUG) {
            validateChildOrder();
        }
        clearPreLayoutSpanMappingCache();
    }

    @Override
    public void onLayoutCompleted(RecyclerView.State state) {
        super.onLayoutCompleted(state);
        mPendingSpanCountChange = false;
    }

    private void clearPreLayoutSpanMappingCache() {
        mPreLayoutSpanSizeCache.clear();
        mPreLayoutSpanIndexCache.clear();
    }

    private void cachePreLayoutSpanMapping() {
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            final LayoutParams lp = (LayoutParams) getChildAt(i).getLayoutParams();
            final int viewPosition = lp.getViewLayoutPosition();
            mPreLayoutSpanSizeCache.put(viewPosition, lp.getSpanSize());
            mPreLayoutSpanIndexCache.put(viewPosition, lp.getSpanIndex());
        }
    }

    @Override
    public void onItemsAdded(RecyclerView recyclerView, int positionStart, int itemCount) {
        mSpanSizeLookup.invalidateSpanIndexCache();
        mSpanSizeLookup.invalidateSpanGroupIndexCache();
    }

    @Override
    public void onItemsChanged(RecyclerView recyclerView) {
        mSpanSizeLookup.invalidateSpanIndexCache();
        mSpanSizeLookup.invalidateSpanGroupIndexCache();
    }

    @Override
    public void onItemsRemoved(RecyclerView recyclerView, int positionStart, int itemCount) {
        mSpanSizeLookup.invalidateSpanIndexCache();
        mSpanSizeLookup.invalidateSpanGroupIndexCache();
    }

    @Override
    public void onItemsUpdated(RecyclerView recyclerView, int positionStart, int itemCount, Object payload) {
        mSpanSizeLookup.invalidateSpanIndexCache();
        mSpanSizeLookup.invalidateSpanGroupIndexCache();
    }

    @Override
    public void onItemsMoved(RecyclerView recyclerView, int from, int to, int itemCount) {
        mSpanSizeLookup.invalidateSpanIndexCache();
        mSpanSizeLookup.invalidateSpanGroupIndexCache();
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        if (mOrientation == HORIZONTAL) {
            return new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        } else {
            return new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    @Override
    public RecyclerView.LayoutParams generateLayoutParams(Context c, AttributeSet attrs) {
        return new LayoutParams(c, attrs);
    }

    @Override
    public RecyclerView.LayoutParams generateLayoutParams(ViewGroup.LayoutParams lp) {
        if (lp instanceof ViewGroup.MarginLayoutParams) {
            return new LayoutParams((ViewGroup.MarginLayoutParams) lp);
        } else {
            return new LayoutParams(lp);
        }
    }

    @Override
    public boolean checkLayoutParams(RecyclerView.LayoutParams lp) {
        return lp instanceof LayoutParams;
    }

    public void setSpanSizeLookup(SpanSizeLookup spanSizeLookup) {
        mSpanSizeLookup = spanSizeLookup;
    }

    public SpanSizeLookup getSpanSizeLookup() {
        return mSpanSizeLookup;
    }

    private void updateMeasurements() {
        int totalSpace;
        if (getOrientation() == VERTICAL) {
            totalSpace = getWidth() - getPaddingRight() - getPaddingLeft();
        } else {
            totalSpace = getHeight() - getPaddingBottom() - getPaddingTop();
        }
        calculateItemBorders(totalSpace);
    }

    @Override
    public void setMeasuredDimension(Rect childrenBounds, int wSpec, int hSpec) {
        if (mCachedBorders == null) {
            super.setMeasuredDimension(childrenBounds, wSpec, hSpec);
        }
        final int width, height;
        final int horizontalPadding = getPaddingLeft() + getPaddingRight();
        final int verticalPadding = getPaddingTop() + getPaddingBottom();
        if (mOrientation == VERTICAL) {
            final int usedHeight = childrenBounds.height() + verticalPadding;
            height = chooseSize(hSpec, usedHeight, getMinimumHeight());
            width = chooseSize(wSpec, mCachedBorders[mCachedBorders.length - 1] + horizontalPadding, getMinimumWidth());
        } else {
            final int usedWidth = childrenBounds.width() + horizontalPadding;
            width = chooseSize(wSpec, usedWidth, getMinimumWidth());
            height = chooseSize(hSpec, mCachedBorders[mCachedBorders.length - 1] + verticalPadding, getMinimumHeight());
        }
        setMeasuredDimension(width, height);
    }

    private void calculateItemBorders(int totalSpace) {
        mCachedBorders = calculateItemBorders(mCachedBorders, mSpanCount, totalSpace);
    }

    static int[] calculateItemBorders(int[] cachedBorders, int spanCount, int totalSpace) {
        if (cachedBorders == null || cachedBorders.length != spanCount + 1 || cachedBorders[cachedBorders.length - 1] != totalSpace) {
            cachedBorders = new int[spanCount + 1];
        }
        cachedBorders[0] = 0;
        int sizePerSpan = totalSpace / spanCount;
        int sizePerSpanRemainder = totalSpace % spanCount;
        int consumedPixels = 0;
        int additionalSize = 0;
        for (int i = 1; i <= spanCount; i++) {
            int itemSize = sizePerSpan;
            additionalSize += sizePerSpanRemainder;
            if (additionalSize > 0 && (spanCount - additionalSize) < sizePerSpanRemainder) {
                itemSize += 1;
                additionalSize -= spanCount;
            }
            consumedPixels += itemSize;
            cachedBorders[i] = consumedPixels;
        }
        return cachedBorders;
    }

    int getSpaceForSpanRange(int startSpan, int spanSize) {
        if (mOrientation == VERTICAL && isLayoutRTL()) {
            return mCachedBorders[mSpanCount - startSpan] - mCachedBorders[mSpanCount - startSpan - spanSize];
        } else {
            return mCachedBorders[startSpan + spanSize] - mCachedBorders[startSpan];
        }
    }

    @Override
    void onAnchorReady(RecyclerView.Recycler recycler, RecyclerView.State state, AnchorInfo anchorInfo, int itemDirection) {
        super.onAnchorReady(recycler, state, anchorInfo, itemDirection);
        updateMeasurements();
        if (state.getItemCount() > 0 && !state.isPreLayout()) {
            ensureAnchorIsInCorrectSpan(recycler, state, anchorInfo, itemDirection);
        }
        ensureViewSet();
    }

    private void ensureViewSet() {
        if (mSet == null || mSet.length != mSpanCount) {
            mSet = new View[mSpanCount];
        }
    }

    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        updateMeasurements();
        ensureViewSet();
        return super.scrollHorizontallyBy(dx, recycler, state);
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        updateMeasurements();
        ensureViewSet();
        return super.scrollVerticallyBy(dy, recycler, state);
    }

    private void ensureAnchorIsInCorrectSpan(RecyclerView.Recycler recycler, RecyclerView.State state, AnchorInfo anchorInfo, int itemDirection) {
        final boolean layingOutInPrimaryDirection = itemDirection == SeslLinearLayoutManager.LayoutState.ITEM_DIRECTION_TAIL;
        int span = getSpanIndex(recycler, state, anchorInfo.mPosition);
        if (layingOutInPrimaryDirection) {
            while (span > 0 && anchorInfo.mPosition > 0) {
                anchorInfo.mPosition--;
                span = getSpanIndex(recycler, state, anchorInfo.mPosition);
            }
        } else {
            final int indexLimit = state.getItemCount() - 1;
            int pos = anchorInfo.mPosition;
            int bestSpan = span;
            while (pos < indexLimit) {
                int next = getSpanIndex(recycler, state, pos + 1);
                if (next > bestSpan) {
                    pos += 1;
                    bestSpan = next;
                } else {
                    break;
                }
            }
            anchorInfo.mPosition = pos;
        }
    }

    @Override
    View findReferenceChild(RecyclerView.Recycler recycler, RecyclerView.State state, int start, int end, int itemCount) {
        ensureLayoutState();
        View invalidMatch = null;
        View outOfBoundsMatch = null;
        final int boundsStart = mOrientationHelper.getStartAfterPadding();
        final int boundsEnd = mOrientationHelper.getEndAfterPadding();
        final int diff = end > start ? 1 : -1;
        for (int i = start; i != end; i += diff) {
            final View view = getChildAt(i);
            final int position = getPosition(view);
            if (position >= 0 && position < itemCount) {
                final int span = getSpanIndex(recycler, state, position);
                if (span != 0) {
                    continue;
                }
                if (((RecyclerView.LayoutParams) view.getLayoutParams()).isItemRemoved()) {
                    if (invalidMatch == null) {
                        invalidMatch = view;
                    }
                } else if (mOrientationHelper.getDecoratedStart(view) >= boundsEnd || mOrientationHelper.getDecoratedEnd(view) < boundsStart) {
                    if (outOfBoundsMatch == null) {
                        outOfBoundsMatch = view;
                    }
                } else {
                    return view;
                }
            }
        }
        return outOfBoundsMatch != null ? outOfBoundsMatch : invalidMatch;
    }

    private int getSpanGroupIndex(RecyclerView.Recycler recycler, RecyclerView.State state, int viewPosition) {
        if (!state.isPreLayout()) {
            return mSpanSizeLookup.getCachedSpanGroupIndex(viewPosition, mSpanCount);
        }
        final int adapterPosition = recycler.convertPreLayoutPositionToPostLayout(viewPosition);
        if (adapterPosition == -1) {
            if (DEBUG) {
                throw new RuntimeException("Cannot find span group index for position " + viewPosition);
            }
            Log.w(TAG, "Cannot find span size for pre layout position. " + viewPosition);
            return 0;
        }
        return mSpanSizeLookup.getCachedSpanGroupIndex(adapterPosition, mSpanCount);
    }

    private int getSpanIndex(RecyclerView.Recycler recycler, RecyclerView.State state, int pos) {
        if (!state.isPreLayout()) {
            return mSpanSizeLookup.getCachedSpanIndex(pos, mSpanCount);
        }
        final int cached = mPreLayoutSpanIndexCache.get(pos, -1);
        if (cached != -1) {
            return cached;
        }
        final int adapterPosition = recycler.convertPreLayoutPositionToPostLayout(pos);
        if (adapterPosition == -1) {
            if (DEBUG) {
                throw new RuntimeException("Cannot find span index for pre layout position. It is" + " not cached, not in the adapter. Pos:" + pos);
            }
            Log.w(TAG, "Cannot find span size for pre layout position. It is" + " not cached, not in the adapter. Pos:" + pos);
            return 0;
        }
        return mSpanSizeLookup.getCachedSpanIndex(adapterPosition, mSpanCount);
    }

    private int getSpanSize(RecyclerView.Recycler recycler, RecyclerView.State state, int pos) {
        if (!state.isPreLayout()) {
            return mSpanSizeLookup.getSpanSize(pos);
        }
        final int cached = mPreLayoutSpanSizeCache.get(pos, -1);
        if (cached != -1) {
            return cached;
        }
        final int adapterPosition = recycler.convertPreLayoutPositionToPostLayout(pos);
        if (adapterPosition == -1) {
            if (DEBUG) {
                throw new RuntimeException("Cannot find span size for pre layout position. It is" + " not cached, not in the adapter. Pos:" + pos);
            }
            Log.w(TAG, "Cannot find span size for pre layout position. It is" + " not cached, not in the adapter. Pos:" + pos);
            return 1;
        }
        return mSpanSizeLookup.getSpanSize(adapterPosition);
    }

    @Override
    void collectPrefetchPositionsForLayoutState(RecyclerView.State state, SeslLinearLayoutManager.LayoutState layoutState, LayoutPrefetchRegistry layoutPrefetchRegistry) {
        int remainingSpan = mSpanCount;
        int count = 0;
        while (count < mSpanCount && layoutState.hasMore(state) && remainingSpan > 0) {
            final int pos = layoutState.mCurrentPosition;
            layoutPrefetchRegistry.addPosition(pos, Math.max(0, layoutState.mScrollingOffset));
            final int spanSize = mSpanSizeLookup.getSpanSize(pos);
            remainingSpan -= spanSize;
            layoutState.mCurrentPosition += layoutState.mItemDirection;
            count++;
        }
    }

    @Override
    void layoutChunk(RecyclerView.Recycler recycler, RecyclerView.State state, SeslLinearLayoutManager.LayoutState layoutState, LayoutChunkResult result) {
        final int otherDirSpecMode = mOrientationHelper.getModeInOther();
        final boolean flexibleInOtherDir = otherDirSpecMode != View.MeasureSpec.EXACTLY;
        final int currentOtherDirSize = getChildCount() > 0 ? mCachedBorders[mSpanCount] : 0;
        if (flexibleInOtherDir) {
            updateMeasurements(); //  reset measurements
        }
        final boolean layingOutInPrimaryDirection = layoutState.mItemDirection == SeslLinearLayoutManager.LayoutState.ITEM_DIRECTION_TAIL;
        int count = 0;
        int remainingSpan = mSpanCount;
        if (!layingOutInPrimaryDirection) {
            int itemSpanIndex = getSpanIndex(recycler, state, layoutState.mCurrentPosition);
            int itemSpanSize = getSpanSize(recycler, state, layoutState.mCurrentPosition);
            remainingSpan = itemSpanIndex + itemSpanSize;
        }
        while (count < mSpanCount && layoutState.hasMore(state) && remainingSpan > 0) {
            int pos = layoutState.mCurrentPosition;
            final int spanSize = getSpanSize(recycler, state, pos);
            if (spanSize > mSpanCount) {
                throw new IllegalArgumentException("Item at position " + pos + " requires " + spanSize + " spans but GridLayoutManager has only " + mSpanCount + " spans.");
            }
            remainingSpan -= spanSize;
            if (remainingSpan < 0) {
                break;
            }
            View view = layoutState.next(recycler);
            if (view == null) {
                break;
            }
            mSet[count] = view;
            count++;
        }

        if (count == 0) {
            result.mFinished = true;
            return;
        }

        int maxSize = 0;
        float maxSizeInOther = 0;

        assignSpans(recycler, state, count, layingOutInPrimaryDirection);
        for (int i = 0; i < count; i++) {
            View view = mSet[i];
            if (layoutState.mScrapList == null) {
                if (layingOutInPrimaryDirection) {
                    addView(view);
                } else {
                    addView(view, 0);
                }
            } else {
                if (layingOutInPrimaryDirection) {
                    addDisappearingView(view);
                } else {
                    addDisappearingView(view, 0);
                }
            }
            calculateItemDecorationsForChild(view, mDecorInsets);

            measureChild(view, otherDirSpecMode, false);
            final int size = mOrientationHelper.getDecoratedMeasurement(view);
            if (size > maxSize) {
                maxSize = size;
            }
            final LayoutParams lp = (LayoutParams) view.getLayoutParams();
            final float otherSize = 1f * mOrientationHelper.getDecoratedMeasurementInOther(view) / lp.mSpanSize;
            if (otherSize > maxSizeInOther) {
                maxSizeInOther = otherSize;
            }
        }
        if (flexibleInOtherDir) {
            guessMeasurement(maxSizeInOther, currentOtherDirSize);
            maxSize = 0;
            for (int i = 0; i < count; i++) {
                View view = mSet[i];
                measureChild(view, View.MeasureSpec.EXACTLY, true);
                final int size = mOrientationHelper.getDecoratedMeasurement(view);
                if (size > maxSize) {
                    maxSize = size;
                }
            }
        }

        for (int i = 0; i < count; i++) {
            final View view = mSet[i];
            if (mOrientationHelper.getDecoratedMeasurement(view) != maxSize) {
                final LayoutParams lp = (LayoutParams) view.getLayoutParams();
                final Rect decorInsets = lp.mDecorInsets;
                final int verticalInsets = decorInsets.top + decorInsets.bottom + lp.topMargin + lp.bottomMargin;
                final int horizontalInsets = decorInsets.left + decorInsets.right + lp.leftMargin + lp.rightMargin;
                final int totalSpaceInOther = getSpaceForSpanRange(lp.mSpanIndex, lp.mSpanSize);
                final int wSpec;
                final int hSpec;
                if (mOrientation == VERTICAL) {
                    wSpec = getChildMeasureSpec(totalSpaceInOther, View.MeasureSpec.EXACTLY, horizontalInsets, lp.width, false);
                    hSpec = View.MeasureSpec.makeMeasureSpec(maxSize - verticalInsets, View.MeasureSpec.EXACTLY);
                } else {
                    wSpec = View.MeasureSpec.makeMeasureSpec(maxSize - horizontalInsets, View.MeasureSpec.EXACTLY);
                    hSpec = getChildMeasureSpec(totalSpaceInOther, View.MeasureSpec.EXACTLY, verticalInsets, lp.height, false);
                }
                measureChildWithDecorationsAndMargin(view, wSpec, hSpec, true);
            }
        }

        result.mConsumed = maxSize;

        int left = 0, right = 0, top = 0, bottom = 0;
        if (mOrientation == VERTICAL) {
            if (layoutState.mLayoutDirection == SeslLinearLayoutManager.LayoutState.LAYOUT_START) {
                bottom = layoutState.mOffset;
                top = bottom - maxSize;
            } else {
                top = layoutState.mOffset;
                bottom = top + maxSize;
            }
        } else {
            if (layoutState.mLayoutDirection == SeslLinearLayoutManager.LayoutState.LAYOUT_START) {
                right = layoutState.mOffset;
                left = right - maxSize;
            } else {
                left = layoutState.mOffset;
                right = left + maxSize;
            }
        }
        for (int i = 0; i < count; i++) {
            View view = mSet[i];
            LayoutParams params = (LayoutParams) view.getLayoutParams();
            if (mOrientation == VERTICAL) {
                if (isLayoutRTL()) {
                    right = getPaddingLeft() + mCachedBorders[mSpanCount - params.mSpanIndex];
                    left = right - mOrientationHelper.getDecoratedMeasurementInOther(view);
                } else {
                    left = getPaddingLeft() + mCachedBorders[params.mSpanIndex];
                    right = left + mOrientationHelper.getDecoratedMeasurementInOther(view);
                }
            } else {
                top = getPaddingTop() + mCachedBorders[params.mSpanIndex];
                bottom = top + mOrientationHelper.getDecoratedMeasurementInOther(view);
            }
            layoutDecoratedWithMargins(view, left, top, right, bottom);
            if (DEBUG) {
                Log.d(TAG, "laid out child at position " + getPosition(view) + ", with l:" + (left + params.leftMargin) + ", t:" + (top + params.topMargin) + ", r:" + (right - params.rightMargin) + ", b:" + (bottom - params.bottomMargin) + ", span:" + params.mSpanIndex + ", spanSize:" + params.mSpanSize);
            }
            if (params.isItemRemoved() || params.isItemChanged()) {
                result.mIgnoreConsumed = true;
            }
            result.mFocusable |= view.hasFocusable();
        }
        Arrays.fill(mSet, null);
    }

    private void measureChild(View view, int otherDirParentSpecMode, boolean alreadyMeasured) {
        final LayoutParams lp = (LayoutParams) view.getLayoutParams();
        final Rect decorInsets = lp.mDecorInsets;
        final int verticalInsets = decorInsets.top + decorInsets.bottom + lp.topMargin + lp.bottomMargin;
        final int horizontalInsets = decorInsets.left + decorInsets.right + lp.leftMargin + lp.rightMargin;
        final int availableSpaceInOther = getSpaceForSpanRange(lp.mSpanIndex, lp.mSpanSize);
        final int wSpec;
        final int hSpec;
        if (mOrientation == VERTICAL) {
            wSpec = getChildMeasureSpec(availableSpaceInOther, otherDirParentSpecMode, horizontalInsets, lp.width, false);
            hSpec = getChildMeasureSpec(mOrientationHelper.getTotalSpace(), getHeightMode(), verticalInsets, lp.height, true);
        } else {
            hSpec = getChildMeasureSpec(availableSpaceInOther, otherDirParentSpecMode, verticalInsets, lp.height, false);
            wSpec = getChildMeasureSpec(mOrientationHelper.getTotalSpace(), getWidthMode(), horizontalInsets, lp.width, true);
        }
        measureChildWithDecorationsAndMargin(view, wSpec, hSpec, alreadyMeasured);
    }

    private void guessMeasurement(float maxSizeInOther, int currentOtherDirSize) {
        final int contentSize = Math.round(maxSizeInOther * mSpanCount);
        calculateItemBorders(Math.max(contentSize, currentOtherDirSize));
    }

    private void measureChildWithDecorationsAndMargin(View child, int widthSpec, int heightSpec, boolean alreadyMeasured) {
        RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) child.getLayoutParams();
        final boolean measure;
        if (alreadyMeasured) {
            measure = shouldReMeasureChild(child, widthSpec, heightSpec, lp);
        } else {
            measure = shouldMeasureChild(child, widthSpec, heightSpec, lp);
        }
        if (measure) {
            child.measure(widthSpec, heightSpec);
        }
    }

    private void assignSpans(RecyclerView.Recycler recycler, RecyclerView.State state, int count, boolean layingOutInPrimaryDirection) {
        int span, start, end, diff;
        if (layingOutInPrimaryDirection) {
            start = 0;
            end = count;
            diff = 1;
        } else {
            start = count - 1;
            end = -1;
            diff = -1;
        }
        span = 0;
        for (int i = start; i != end; i += diff) {
            View view = mSet[i];
            LayoutParams params = (LayoutParams) view.getLayoutParams();
            params.mSpanSize = getSpanSize(recycler, state, getPosition(view));
            params.mSpanIndex = span;
            span += params.mSpanSize;
        }
    }

    public int getSpanCount() {
        return mSpanCount;
    }

    public void setSpanCount(int spanCount) {
        if (spanCount == mSpanCount) {
            return;
        }
        mPendingSpanCountChange = true;
        if (spanCount < 1) {
            throw new IllegalArgumentException("Span count should be at least 1. Provided " + spanCount);
        }
        mSpanCount = spanCount;
        mSpanSizeLookup.invalidateSpanIndexCache();
        requestLayout();
    }


    public abstract static class SpanSizeLookup {
        final SparseIntArray mSpanIndexCache = new SparseIntArray();
        final SparseIntArray mSpanGroupIndexCache = new SparseIntArray();
        private boolean mCacheSpanIndices = false;
        private boolean mCacheSpanGroupIndices = false;

        public abstract int getSpanSize(int position);

        public void setSpanIndexCacheEnabled(boolean cacheSpanIndices) {
            if (!cacheSpanIndices) {
                mSpanGroupIndexCache.clear();
            }
            mCacheSpanIndices = cacheSpanIndices;
        }

        public void setSpanGroupIndexCacheEnabled(boolean cacheSpanGroupIndices)  {
            if (!cacheSpanGroupIndices) {
                mSpanGroupIndexCache.clear();
            }
            mCacheSpanGroupIndices = cacheSpanGroupIndices;
        }

        public void invalidateSpanIndexCache() {
            mSpanIndexCache.clear();
        }

        public void invalidateSpanGroupIndexCache() {
            mSpanGroupIndexCache.clear();
        }

        public boolean isSpanIndexCacheEnabled() {
            return mCacheSpanIndices;
        }

        public boolean isSpanGroupIndexCacheEnabled() {
            return mCacheSpanGroupIndices;
        }

        int getCachedSpanIndex(int position, int spanCount) {
            if (!mCacheSpanIndices) {
                return getSpanIndex(position, spanCount);
            }
            final int existing = mSpanIndexCache.get(position, -1);
            if (existing != -1) {
                return existing;
            }
            final int value = getSpanIndex(position, spanCount);
            mSpanIndexCache.put(position, value);
            return value;
        }

        int getCachedSpanGroupIndex(int position, int spanCount) {
            if (!mCacheSpanGroupIndices) {
                return getSpanGroupIndex(position, spanCount);
            }
            final int existing = mSpanGroupIndexCache.get(position, -1);
            if (existing != -1) {
                return existing;
            }
            final int value = getSpanGroupIndex(position, spanCount);
            mSpanGroupIndexCache.put(position, value);
            return value;
        }

        public int getSpanIndex(int position, int spanCount) {
            int positionSpanSize = getSpanSize(position);
            if (positionSpanSize == spanCount) {
                return 0;
            }
            int span = 0;
            int startPos = 0;
            if (mCacheSpanIndices) {
                int prevKey = findFirstKeyLessThan(mSpanIndexCache, position);
                if (prevKey >= 0) {
                    span = mSpanIndexCache.get(prevKey) + getSpanSize(prevKey);
                    startPos = prevKey + 1;
                }
            }
            for (int i = startPos; i < position; i++) {
                int size = getSpanSize(i);
                span += size;
                if (span == spanCount) {
                    span = 0;
                } else if (span > spanCount) {
                    span = size;
                }
            }
            if (span + positionSpanSize <= spanCount) {
                return span;
            }
            return 0;
        }

        static int findFirstKeyLessThan(SparseIntArray cache, int position) {
            int lo = 0;
            int hi = cache.size() - 1;

            while (lo <= hi) {
                final int mid = (lo + hi) >>> 1;
                final int midVal = cache.keyAt(mid);
                if (midVal < position) {
                    lo = mid + 1;
                } else {
                    hi = mid - 1;
                }
            }
            int index = lo - 1;
            if (index >= 0 && index < cache.size()) {
                return cache.keyAt(index);
            }
            return -1;
        }

        public int getSpanGroupIndex(int adapterPosition, int spanCount) {
            int span = 0;
            int group = 0;
            int start = 0;
            if (mCacheSpanGroupIndices) {
                int prevKey = findFirstKeyLessThan(mSpanGroupIndexCache, adapterPosition);
                if (prevKey != -1) {
                    group = mSpanGroupIndexCache.get(prevKey);
                    start = prevKey + 1;
                    span = getCachedSpanIndex(prevKey, spanCount) + getSpanSize(prevKey);
                    if (span == spanCount) {
                        span = 0;
                        group++;
                    }
                }
            }
            int positionSpanSize = getSpanSize(adapterPosition);
            for (int i = start; i < adapterPosition; i++) {
                int size = getSpanSize(i);
                span += size;
                if (span == spanCount) {
                    span = 0;
                    group++;
                } else if (span > spanCount) {
                    span = size;
                    group++;
                }
            }
            if (span + positionSpanSize > spanCount) {
                group++;
            }
            return group;
        }
    }

    @Override
    public View onFocusSearchFailed(View focused, int direction, RecyclerView.Recycler recycler, RecyclerView.State state) {
        View prevFocusedChild = findContainingItemView(focused);
        if (prevFocusedChild == null) {
            return null;
        }
        LayoutParams lp = (LayoutParams) prevFocusedChild.getLayoutParams();
        final int prevSpanStart = lp.mSpanIndex;
        final int prevSpanEnd = lp.mSpanIndex + lp.mSpanSize;
        View view = super.onFocusSearchFailed(focused, direction, recycler, state);
        if (view == null) {
            return null;
        }
        final int layoutDir = convertFocusDirectionToLayoutDirection(direction);
        final boolean ascend = (layoutDir == SeslLinearLayoutManager.LayoutState.LAYOUT_END) != mShouldReverseLayout;
        final int start, inc, limit;
        if (ascend) {
            start = getChildCount() - 1;
            inc = -1;
            limit = -1;
        } else {
            start = 0;
            inc = 1;
            limit = getChildCount();
        }
        final boolean preferLastSpan = mOrientation == VERTICAL && isLayoutRTL();

        View focusableWeakCandidate = null;
        int focusableWeakCandidateSpanIndex = -1;
        int focusableWeakCandidateOverlap = 0;

        View unfocusableWeakCandidate = null;
        int unfocusableWeakCandidateSpanIndex = -1;
        int unfocusableWeakCandidateOverlap = 0;

        int focusableSpanGroupIndex = getSpanGroupIndex(recycler, state, start);
        for (int i = start; i != limit; i += inc) {
            int spanGroupIndex = getSpanGroupIndex(recycler, state, i);
            View candidate = getChildAt(i);
            if (candidate == prevFocusedChild) {
                break;
            }

            if (candidate.hasFocusable() && spanGroupIndex != focusableSpanGroupIndex) {
                if (focusableWeakCandidate != null) {
                    break;
                }
                continue;
            }

            final LayoutParams candidateLp = (LayoutParams) candidate.getLayoutParams();
            final int candidateStart = candidateLp.mSpanIndex;
            final int candidateEnd = candidateLp.mSpanIndex + candidateLp.mSpanSize;
            if (candidate.hasFocusable() && candidateStart == prevSpanStart && candidateEnd == prevSpanEnd) {
                return candidate;
            }
            boolean assignAsWeek = false;
            if ((candidate.hasFocusable() && focusableWeakCandidate == null) || (!candidate.hasFocusable() && unfocusableWeakCandidate == null)) {
                assignAsWeek = true;
            } else {
                int maxStart = Math.max(candidateStart, prevSpanStart);
                int minEnd = Math.min(candidateEnd, prevSpanEnd);
                int overlap = minEnd - maxStart;
                if (candidate.hasFocusable()) {
                    if (overlap > focusableWeakCandidateOverlap) {
                        assignAsWeek = true;
                    } else if (overlap == focusableWeakCandidateOverlap && preferLastSpan == (candidateStart > focusableWeakCandidateSpanIndex)) {
                        assignAsWeek = true;
                    }
                } else if (focusableWeakCandidate == null && isViewPartiallyVisible(candidate, false, true)) {
                    if (overlap > unfocusableWeakCandidateOverlap) {
                        assignAsWeek = true;
                    } else if (overlap == unfocusableWeakCandidateOverlap && preferLastSpan == (candidateStart > unfocusableWeakCandidateSpanIndex)) {
                        assignAsWeek = true;
                    }
                }
            }

            if (assignAsWeek) {
                if (candidate.hasFocusable()) {
                    focusableWeakCandidate = candidate;
                    focusableWeakCandidateSpanIndex = candidateLp.mSpanIndex;
                    focusableWeakCandidateOverlap = Math.min(candidateEnd, prevSpanEnd) - Math.max(candidateStart, prevSpanStart);
                } else {
                    unfocusableWeakCandidate = candidate;
                    unfocusableWeakCandidateSpanIndex = candidateLp.mSpanIndex;
                    unfocusableWeakCandidateOverlap = Math.min(candidateEnd, prevSpanEnd) - Math.max(candidateStart, prevSpanStart);
                }
            }
        }
        return (focusableWeakCandidate != null) ? focusableWeakCandidate : unfocusableWeakCandidate;
    }

    @Override
    public boolean supportsPredictiveItemAnimations() {
        return mPendingSavedState == null && !mPendingSpanCountChange;
    }

    @Override
    public int computeHorizontalScrollRange(RecyclerView.State state) {
        if (mUsingSpansToEstimateScrollBarDimensions) {
            return computeScrollRangeWithSpanInfo(state);
        } else {
            return super.computeHorizontalScrollRange(state);
        }
    }

    @Override
    public int computeVerticalScrollRange(RecyclerView.State state) {
        if (mUsingSpansToEstimateScrollBarDimensions) {
            return computeScrollRangeWithSpanInfo(state);
        } else {
            return super.computeVerticalScrollRange(state);
        }
    }

    @Override
    public int computeHorizontalScrollOffset(RecyclerView.State state) {
        if (mUsingSpansToEstimateScrollBarDimensions) {
            return computeScrollOffsetWithSpanInfo(state);
        } else {
            return super.computeHorizontalScrollOffset(state);
        }
    }

    @Override
    public int computeVerticalScrollOffset(RecyclerView.State state) {
        if (mUsingSpansToEstimateScrollBarDimensions) {
            return computeScrollOffsetWithSpanInfo(state);
        } else {
            return super.computeVerticalScrollOffset(state);
        }
    }

    public void setUsingSpansToEstimateScrollbarDimensions(boolean useSpansToEstimateScrollBarDimensions) {
        mUsingSpansToEstimateScrollBarDimensions = useSpansToEstimateScrollBarDimensions;
    }

    public boolean isUsingSpansToEstimateScrollbarDimensions() {
        return mUsingSpansToEstimateScrollBarDimensions;
    }

    private int computeScrollRangeWithSpanInfo(RecyclerView.State state) {
        if (getChildCount() == 0 || state.getItemCount() == 0) {
            return 0;
        }
        ensureLayoutState();

        View startChild = findFirstVisibleChildClosestToStart(!isSmoothScrollbarEnabled(), true);
        View endChild = findFirstVisibleChildClosestToEnd(!isSmoothScrollbarEnabled(), true);

        if (startChild == null || endChild == null) {
            return 0;
        }
        if (!isSmoothScrollbarEnabled()) {
            return mSpanSizeLookup.getCachedSpanGroupIndex(state.getItemCount() - 1, mSpanCount) + 1;
        }

        final int laidOutArea = mOrientationHelper.getDecoratedEnd(endChild)
                - mOrientationHelper.getDecoratedStart(startChild);

        final int firstVisibleSpan = mSpanSizeLookup.getCachedSpanGroupIndex(getPosition(startChild), mSpanCount);
        final int lastVisibleSpan = mSpanSizeLookup.getCachedSpanGroupIndex(getPosition(endChild), mSpanCount);
        final int totalSpans = mSpanSizeLookup.getCachedSpanGroupIndex(state.getItemCount() - 1, mSpanCount) + 1;
        final int laidOutSpans = lastVisibleSpan - firstVisibleSpan + 1;

        return (int) (((float) laidOutArea / laidOutSpans) * totalSpans);
    }

    private int computeScrollOffsetWithSpanInfo(RecyclerView.State state) {
        if (getChildCount() == 0 || state.getItemCount() == 0) {
            return 0;
        }
        ensureLayoutState();

        boolean smoothScrollEnabled = isSmoothScrollbarEnabled();
        View startChild = findFirstVisibleChildClosestToStart(!smoothScrollEnabled, true);
        View endChild = findFirstVisibleChildClosestToEnd(!smoothScrollEnabled, true);
        if (startChild == null || endChild == null) {
            return 0;
        }
        int startChildSpan = mSpanSizeLookup.getCachedSpanGroupIndex(getPosition(startChild), mSpanCount);
        int endChildSpan = mSpanSizeLookup.getCachedSpanGroupIndex(getPosition(endChild), mSpanCount);

        final int minSpan = Math.min(startChildSpan, endChildSpan);
        final int maxSpan = Math.max(startChildSpan, endChildSpan);
        final int totalSpans = mSpanSizeLookup.getCachedSpanGroupIndex(state.getItemCount() - 1, mSpanCount) + 1;

        final int spansBefore = mShouldReverseLayout ? Math.max(0, totalSpans - maxSpan - 1) : Math.max(0, minSpan);
        if (!smoothScrollEnabled) {
            return spansBefore;
        }
        final int laidOutArea = Math.abs(mOrientationHelper.getDecoratedEnd(endChild) - mOrientationHelper.getDecoratedStart(startChild));

        final int firstVisibleSpan = mSpanSizeLookup.getCachedSpanGroupIndex(getPosition(startChild), mSpanCount);
        final int lastVisibleSpan = mSpanSizeLookup.getCachedSpanGroupIndex(getPosition(endChild), mSpanCount);
        final int laidOutSpans = lastVisibleSpan - firstVisibleSpan + 1;
        final float avgSizePerSpan = (float) laidOutArea / laidOutSpans;

        return Math.round(spansBefore * avgSizePerSpan + (mOrientationHelper.getStartAfterPadding() - mOrientationHelper.getDecoratedStart(startChild)));
    }

    public static final class DefaultSpanSizeLookup extends SpanSizeLookup {
        @Override
        public int getSpanSize(int position) {
            return 1;
        }

        @Override
        public int getSpanIndex(int position, int spanCount) {
            return position % spanCount;
        }
    }

    public static class LayoutParams extends RecyclerView.LayoutParams {
        public static final int INVALID_SPAN_ID = -1;

        int mSpanIndex = INVALID_SPAN_ID;

        int mSpanSize = 0;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(ViewGroup.MarginLayoutParams source) {
            super(source);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }

        public LayoutParams(RecyclerView.LayoutParams source) {
            super(source);
        }

        public int getSpanIndex() {
            return mSpanIndex;
        }

        public int getSpanSize() {
            return mSpanSize;
        }
    }
}
