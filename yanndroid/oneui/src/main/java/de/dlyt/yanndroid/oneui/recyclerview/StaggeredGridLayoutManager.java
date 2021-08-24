package de.dlyt.yanndroid.oneui.recyclerview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

public class StaggeredGridLayoutManager extends SeslRecyclerView.LayoutManager implements SeslRecyclerView.SmoothScroller.ScrollVectorProvider {
    public static final int HORIZONTAL = SeslRecyclerView.HORIZONTAL;
    public static final int VERTICAL = SeslRecyclerView.VERTICAL;
    public static final int GAP_HANDLING_NONE = 0;
    @SuppressWarnings("unused")
    @Deprecated
    public static final int GAP_HANDLING_LAZY = 1;
    public static final int GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS = 2;
    static final boolean DEBUG = false;
    static final int INVALID_OFFSET = Integer.MIN_VALUE;
    private static final String TAG = "StaggeredGridLManager";
    private static final float MAX_SCROLL_FACTOR = 1 / 3f;
    @NonNull
    private final LayoutState mLayoutState;
    private final Rect mTmpRect = new Rect();
    private final AnchorInfo mAnchorInfo = new AnchorInfo();
    Span[] mSpans;
    @NonNull
    OrientationHelper mPrimaryOrientation;
    @NonNull
    OrientationHelper mSecondaryOrientation;
    boolean mReverseLayout = false;
    boolean mShouldReverseLayout = false;
    int mPendingScrollPosition = SeslRecyclerView.NO_POSITION;
    int mPendingScrollPositionOffset = INVALID_OFFSET;
    LazySpanLookup mLazySpanLookup = new LazySpanLookup();
    private int mSpanCount = -1;
    private int mOrientation;
    private int mSizePerSpan;
    private BitSet mRemainingSpans;
    private int mGapStrategy = GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS;
    private boolean mLastLayoutFromEnd;
    private boolean mLastLayoutRTL;
    private SavedState mPendingSavedState;
    private int mFullSizeSpec;
    private boolean mLaidOutInvalidFullSpan = false;
    private final Runnable mCheckForGapsRunnable = new Runnable() {
        @Override
        public void run() {
            checkForGaps();
        }
    };
    private boolean mSmoothScrollbarEnabled = true;
    private int[] mPrefetchDistances;

    @SuppressWarnings("unused")
    public StaggeredGridLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        Properties properties = getProperties(context, attrs, defStyleAttr, defStyleRes);
        setOrientation(properties.orientation);
        setSpanCount(properties.spanCount);
        setReverseLayout(properties.reverseLayout);
        mLayoutState = new LayoutState();
        createOrientationHelpers();
    }

    public StaggeredGridLayoutManager(int spanCount, int orientation) {
        mOrientation = orientation;
        setSpanCount(spanCount);
        mLayoutState = new LayoutState();
        createOrientationHelpers();
    }

    @Override
    public boolean isAutoMeasureEnabled() {
        return mGapStrategy != GAP_HANDLING_NONE;
    }

    private void createOrientationHelpers() {
        mPrimaryOrientation = OrientationHelper.createOrientationHelper(this, mOrientation);
        mSecondaryOrientation = OrientationHelper.createOrientationHelper(this, 1 - mOrientation);
    }

    boolean checkForGaps() {
        if (getChildCount() == 0 || mGapStrategy == GAP_HANDLING_NONE || !isAttachedToWindow()) {
            return false;
        }
        final int minPos, maxPos;
        if (mShouldReverseLayout) {
            minPos = getLastChildPosition();
            maxPos = getFirstChildPosition();
        } else {
            minPos = getFirstChildPosition();
            maxPos = getLastChildPosition();
        }
        if (minPos == 0) {
            View gapView = hasGapsToFix();
            if (gapView != null) {
                mLazySpanLookup.clear();
                requestSimpleAnimationsInNextLayout();
                requestLayout();
                return true;
            }
        }
        if (!mLaidOutInvalidFullSpan) {
            return false;
        }
        int invalidGapDir = mShouldReverseLayout ? LayoutState.LAYOUT_START : LayoutState.LAYOUT_END;
        final LazySpanLookup.FullSpanItem invalidFsi = mLazySpanLookup.getFirstFullSpanItemInRange(minPos, maxPos + 1, invalidGapDir, true);
        if (invalidFsi == null) {
            mLaidOutInvalidFullSpan = false;
            mLazySpanLookup.forceInvalidateAfter(maxPos + 1);
            return false;
        }
        final LazySpanLookup.FullSpanItem validFsi = mLazySpanLookup.getFirstFullSpanItemInRange(minPos, invalidFsi.mPosition, invalidGapDir * -1, true);
        if (validFsi == null) {
            mLazySpanLookup.forceInvalidateAfter(invalidFsi.mPosition);
        } else {
            mLazySpanLookup.forceInvalidateAfter(validFsi.mPosition + 1);
        }
        requestSimpleAnimationsInNextLayout();
        requestLayout();
        return true;
    }

    @Override
    public void onScrollStateChanged(int state) {
        if (state == SeslRecyclerView.SCROLL_STATE_IDLE) {
            checkForGaps();
        }
    }

    @Override
    public void onDetachedFromWindow(SeslRecyclerView view, SeslRecyclerView.Recycler recycler) {
        super.onDetachedFromWindow(view, recycler);

        removeCallbacks(mCheckForGapsRunnable);
        for (int i = 0; i < mSpanCount; i++) {
            mSpans[i].clear();
        }
        view.requestLayout();
    }

    View hasGapsToFix() {
        int startChildIndex = 0;
        int endChildIndex = getChildCount() - 1;
        BitSet mSpansToCheck = new BitSet(mSpanCount);
        mSpansToCheck.set(0, mSpanCount, true);

        final int firstChildIndex, childLimit;
        final int preferredSpanDir = mOrientation == VERTICAL && isLayoutRTL() ? 1 : -1;

        if (mShouldReverseLayout) {
            firstChildIndex = endChildIndex;
            childLimit = startChildIndex - 1;
        } else {
            firstChildIndex = startChildIndex;
            childLimit = endChildIndex + 1;
        }
        final int nextChildDiff = firstChildIndex < childLimit ? 1 : -1;
        for (int i = firstChildIndex; i != childLimit; i += nextChildDiff) {
            View child = getChildAt(i);
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            if (mSpansToCheck.get(lp.mSpan.mIndex)) {
                if (checkSpanForGap(lp.mSpan)) {
                    return child;
                }
                mSpansToCheck.clear(lp.mSpan.mIndex);
            }
            if (lp.mFullSpan) {
                continue;
            }

            if (i + nextChildDiff != childLimit) {
                View nextChild = getChildAt(i + nextChildDiff);
                boolean compareSpans = false;
                if (mShouldReverseLayout) {
                    int myEnd = mPrimaryOrientation.getDecoratedEnd(child);
                    int nextEnd = mPrimaryOrientation.getDecoratedEnd(nextChild);
                    if (myEnd < nextEnd) {
                        return child;
                    } else if (myEnd == nextEnd) {
                        compareSpans = true;
                    }
                } else {
                    int myStart = mPrimaryOrientation.getDecoratedStart(child);
                    int nextStart = mPrimaryOrientation.getDecoratedStart(nextChild);
                    if (myStart > nextStart) {
                        return child;
                    } else if (myStart == nextStart) {
                        compareSpans = true;
                    }
                }
                if (compareSpans) {
                    LayoutParams nextLp = (LayoutParams) nextChild.getLayoutParams();
                    if (lp.mSpan.mIndex - nextLp.mSpan.mIndex < 0 != preferredSpanDir < 0) {
                        return child;
                    }
                }
            }
        }
        return null;
    }

    private boolean checkSpanForGap(Span span) {
        if (mShouldReverseLayout) {
            if (span.getEndLine() < mPrimaryOrientation.getEndAfterPadding()) {
                final View endView = span.mViews.get(span.mViews.size() - 1);
                final LayoutParams lp = span.getLayoutParams(endView);
                return !lp.mFullSpan;
            }
        } else if (span.getStartLine() > mPrimaryOrientation.getStartAfterPadding()) {
            final View startView = span.mViews.get(0);
            final LayoutParams lp = span.getLayoutParams(startView);
            return !lp.mFullSpan;
        }
        return false;
    }

    public int getGapStrategy() {
        return mGapStrategy;
    }

    public void setGapStrategy(int gapStrategy) {
        assertNotInLayoutOrScroll(null);
        if (gapStrategy == mGapStrategy) {
            return;
        }
        if (gapStrategy != GAP_HANDLING_NONE && gapStrategy != GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS) {
            throw new IllegalArgumentException("invalid gap strategy. Must be GAP_HANDLING_NONE " + "or GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS");
        }
        mGapStrategy = gapStrategy;
        requestLayout();
    }

    @Override
    public void assertNotInLayoutOrScroll(String message) {
        if (mPendingSavedState == null) {
            super.assertNotInLayoutOrScroll(message);
        }
    }

    public int getSpanCount() {
        return mSpanCount;
    }

    public void setSpanCount(int spanCount) {
        assertNotInLayoutOrScroll(null);
        if (spanCount != mSpanCount) {
            invalidateSpanAssignments();
            mSpanCount = spanCount;
            mRemainingSpans = new BitSet(mSpanCount);
            mSpans = new Span[mSpanCount];
            for (int i = 0; i < mSpanCount; i++) {
                mSpans[i] = new Span(i);
            }
            requestLayout();
        }
    }

    public void invalidateSpanAssignments() {
        mLazySpanLookup.clear();
        requestLayout();
    }

    private void resolveShouldLayoutReverse() {
        if (mOrientation == VERTICAL || !isLayoutRTL()) {
            mShouldReverseLayout = mReverseLayout;
        } else {
            mShouldReverseLayout = !mReverseLayout;
        }
    }

    boolean isLayoutRTL() {
        return getLayoutDirection() == ViewCompat.LAYOUT_DIRECTION_RTL;
    }

    public boolean getReverseLayout() {
        return mReverseLayout;
    }

    public void setReverseLayout(boolean reverseLayout) {
        assertNotInLayoutOrScroll(null);
        if (mPendingSavedState != null && mPendingSavedState.mReverseLayout != reverseLayout) {
            mPendingSavedState.mReverseLayout = reverseLayout;
        }
        mReverseLayout = reverseLayout;
        requestLayout();
    }

    @Override
    public void setMeasuredDimension(Rect childrenBounds, int wSpec, int hSpec) {
        final int width, height;
        final int horizontalPadding = getPaddingLeft() + getPaddingRight();
        final int verticalPadding = getPaddingTop() + getPaddingBottom();
        if (mOrientation == VERTICAL) {
            final int usedHeight = childrenBounds.height() + verticalPadding;
            height = chooseSize(hSpec, usedHeight, getMinimumHeight());
            width = chooseSize(wSpec, mSizePerSpan * mSpanCount + horizontalPadding, getMinimumWidth());
        } else {
            final int usedWidth = childrenBounds.width() + horizontalPadding;
            width = chooseSize(wSpec, usedWidth, getMinimumWidth());
            height = chooseSize(hSpec, mSizePerSpan * mSpanCount + verticalPadding, getMinimumHeight());
        }
        setMeasuredDimension(width, height);
    }

    @Override
    public void onLayoutChildren(SeslRecyclerView.Recycler recycler, SeslRecyclerView.State state) {
        onLayoutChildren(recycler, state, true);
    }

    @Override
    public void onAdapterChanged(@Nullable SeslRecyclerView.Adapter oldAdapter, @Nullable SeslRecyclerView.Adapter newAdapter) {
        mLazySpanLookup.clear();
        for (int i = 0; i < mSpanCount; i++) {
            mSpans[i].clear();
        }
    }

    private void onLayoutChildren(SeslRecyclerView.Recycler recycler, SeslRecyclerView.State state, boolean shouldCheckForGaps) {
        final AnchorInfo anchorInfo = mAnchorInfo;
        if (mPendingSavedState != null || mPendingScrollPosition != SeslRecyclerView.NO_POSITION) {
            if (state.getItemCount() == 0) {
                removeAndRecycleAllViews(recycler);
                anchorInfo.reset();
                return;
            }
        }

        boolean recalculateAnchor = !anchorInfo.mValid || mPendingScrollPosition != SeslRecyclerView.NO_POSITION || mPendingSavedState != null;
        if (recalculateAnchor) {
            anchorInfo.reset();
            if (mPendingSavedState != null) {
                applyPendingSavedState(anchorInfo);
            } else {
                resolveShouldLayoutReverse();
                anchorInfo.mLayoutFromEnd = mShouldReverseLayout;
            }
            updateAnchorInfoForLayout(state, anchorInfo);
            anchorInfo.mValid = true;
        }
        if (mPendingSavedState == null && mPendingScrollPosition == SeslRecyclerView.NO_POSITION) {
            if (anchorInfo.mLayoutFromEnd != mLastLayoutFromEnd || isLayoutRTL() != mLastLayoutRTL) {
                mLazySpanLookup.clear();
                anchorInfo.mInvalidateOffsets = true;
            }
        }

        if (getChildCount() > 0 && (mPendingSavedState == null || mPendingSavedState.mSpanOffsetsSize < 1)) {
            if (anchorInfo.mInvalidateOffsets) {
                for (int i = 0; i < mSpanCount; i++) {
                    mSpans[i].clear();
                    if (anchorInfo.mOffset != INVALID_OFFSET) {
                        mSpans[i].setLine(anchorInfo.mOffset);
                    }
                }
            } else {
                if (recalculateAnchor || mAnchorInfo.mSpanReferenceLines == null) {
                    for (int i = 0; i < mSpanCount; i++) {
                        mSpans[i].cacheReferenceLineAndClear(mShouldReverseLayout, anchorInfo.mOffset);
                    }
                    mAnchorInfo.saveSpanReferenceLines(mSpans);
                } else {
                    for (int i = 0; i < mSpanCount; i++) {
                        final Span span = mSpans[i];
                        span.clear();
                        span.setLine(mAnchorInfo.mSpanReferenceLines[i]);
                    }
                }
            }
        }
        detachAndScrapAttachedViews(recycler);
        mLayoutState.mRecycle = false;
        mLaidOutInvalidFullSpan = false;
        updateMeasureSpecs(mSecondaryOrientation.getTotalSpace());
        updateLayoutState(anchorInfo.mPosition, state);
        if (anchorInfo.mLayoutFromEnd) {
            setLayoutStateDirection(LayoutState.LAYOUT_START);
            fill(recycler, mLayoutState, state);
            setLayoutStateDirection(LayoutState.LAYOUT_END);
            mLayoutState.mCurrentPosition = anchorInfo.mPosition + mLayoutState.mItemDirection;
            fill(recycler, mLayoutState, state);
        } else {
            setLayoutStateDirection(LayoutState.LAYOUT_END);
            fill(recycler, mLayoutState, state);
            setLayoutStateDirection(LayoutState.LAYOUT_START);
            mLayoutState.mCurrentPosition = anchorInfo.mPosition + mLayoutState.mItemDirection;
            fill(recycler, mLayoutState, state);
        }

        repositionToWrapContentIfNecessary();

        if (getChildCount() > 0) {
            if (mShouldReverseLayout) {
                fixEndGap(recycler, state, true);
                fixStartGap(recycler, state, false);
            } else {
                fixStartGap(recycler, state, true);
                fixEndGap(recycler, state, false);
            }
        }
        boolean hasGaps = false;
        if (shouldCheckForGaps && !state.isPreLayout()) {
            final boolean needToCheckForGaps = mGapStrategy != GAP_HANDLING_NONE && getChildCount() > 0 && (mLaidOutInvalidFullSpan || hasGapsToFix() != null);
            if (needToCheckForGaps) {
                removeCallbacks(mCheckForGapsRunnable);
                if (checkForGaps()) {
                    hasGaps = true;
                }
            }
        }
        if (state.isPreLayout()) {
            mAnchorInfo.reset();
        }
        mLastLayoutFromEnd = anchorInfo.mLayoutFromEnd;
        mLastLayoutRTL = isLayoutRTL();
        if (hasGaps) {
            mAnchorInfo.reset();
            onLayoutChildren(recycler, state, false);
        }
    }

    @Override
    public void onLayoutCompleted(SeslRecyclerView.State state) {
        super.onLayoutCompleted(state);
        mPendingScrollPosition = SeslRecyclerView.NO_POSITION;
        mPendingScrollPositionOffset = INVALID_OFFSET;
        mPendingSavedState = null;
        mAnchorInfo.reset();
    }

    private void repositionToWrapContentIfNecessary() {
        if (mSecondaryOrientation.getMode() == View.MeasureSpec.EXACTLY) {
            return;
        }
        float maxSize = 0;
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            float size = mSecondaryOrientation.getDecoratedMeasurement(child);
            if (size < maxSize) {
                continue;
            }
            LayoutParams layoutParams = (LayoutParams) child.getLayoutParams();
            if (layoutParams.isFullSpan()) {
                size = 1f * size / mSpanCount;
            }
            maxSize = Math.max(maxSize, size);
        }
        int before = mSizePerSpan;
        int desired = Math.round(maxSize * mSpanCount);
        if (mSecondaryOrientation.getMode() == View.MeasureSpec.AT_MOST) {
            desired = Math.min(desired, mSecondaryOrientation.getTotalSpace());
        }
        updateMeasureSpecs(desired);
        if (mSizePerSpan == before) {
            return;
        }
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            final LayoutParams lp = (LayoutParams) child.getLayoutParams();
            if (lp.mFullSpan) {
                continue;
            }
            if (isLayoutRTL() && mOrientation == VERTICAL) {
                int newOffset = -(mSpanCount - 1 - lp.mSpan.mIndex) * mSizePerSpan;
                int prevOffset = -(mSpanCount - 1 - lp.mSpan.mIndex) * before;
                child.offsetLeftAndRight(newOffset - prevOffset);
            } else {
                int newOffset = lp.mSpan.mIndex * mSizePerSpan;
                int prevOffset = lp.mSpan.mIndex * before;
                if (mOrientation == VERTICAL) {
                    child.offsetLeftAndRight(newOffset - prevOffset);
                } else {
                    child.offsetTopAndBottom(newOffset - prevOffset);
                }
            }
        }
    }

    private void applyPendingSavedState(AnchorInfo anchorInfo) {
        Log.d(TAG, "found saved state: " + mPendingSavedState);
        if (mPendingSavedState.mSpanOffsetsSize > 0) {
            if (mPendingSavedState.mSpanOffsetsSize == mSpanCount) {
                for (int i = 0; i < mSpanCount; i++) {
                    mSpans[i].clear();
                    int line = mPendingSavedState.mSpanOffsets[i];
                    if (line != Span.INVALID_LINE) {
                        if (mPendingSavedState.mAnchorLayoutFromEnd) {
                            line += mPrimaryOrientation.getEndAfterPadding();
                        } else {
                            line += mPrimaryOrientation.getStartAfterPadding();
                        }
                    }
                    mSpans[i].setLine(line);
                }
            } else {
                mPendingSavedState.invalidateSpanInfo();
                mPendingSavedState.mAnchorPosition = mPendingSavedState.mVisibleAnchorPosition;
            }
        }
        mLastLayoutRTL = mPendingSavedState.mLastLayoutRTL;
        setReverseLayout(mPendingSavedState.mReverseLayout);
        resolveShouldLayoutReverse();

        if (mPendingSavedState.mAnchorPosition != SeslRecyclerView.NO_POSITION) {
            mPendingScrollPosition = mPendingSavedState.mAnchorPosition;
            anchorInfo.mLayoutFromEnd = mPendingSavedState.mAnchorLayoutFromEnd;
        } else {
            anchorInfo.mLayoutFromEnd = mShouldReverseLayout;
        }
        if (mPendingSavedState.mSpanLookupSize > 1) {
            mLazySpanLookup.mData = mPendingSavedState.mSpanLookup;
            mLazySpanLookup.mFullSpanItems = mPendingSavedState.mFullSpanItems;
        }
    }

    void updateAnchorInfoForLayout(SeslRecyclerView.State state, AnchorInfo anchorInfo) {
        if (updateAnchorFromPendingData(state, anchorInfo)) {
            return;
        }
        if (updateAnchorFromChildren(state, anchorInfo)) {
            return;
        }
        Log.d(TAG, "Deciding anchor info from fresh state");
        anchorInfo.assignCoordinateFromPadding();
        anchorInfo.mPosition = 0;
    }

    private boolean updateAnchorFromChildren(SeslRecyclerView.State state, AnchorInfo anchorInfo) {
        anchorInfo.mPosition = mLastLayoutFromEnd ? findLastReferenceChildPosition(state.getItemCount()) : findFirstReferenceChildPosition(state.getItemCount());
        anchorInfo.mOffset = INVALID_OFFSET;
        return true;
    }

    boolean updateAnchorFromPendingData(SeslRecyclerView.State state, AnchorInfo anchorInfo) {
        if (state.isPreLayout() || mPendingScrollPosition == SeslRecyclerView.NO_POSITION) {
            return false;
        }
        if (mPendingScrollPosition < 0 || mPendingScrollPosition >= state.getItemCount()) {
            mPendingScrollPosition = SeslRecyclerView.NO_POSITION;
            mPendingScrollPositionOffset = INVALID_OFFSET;
            return false;
        }

        if (mPendingSavedState == null || mPendingSavedState.mAnchorPosition == SeslRecyclerView.NO_POSITION || mPendingSavedState.mSpanOffsetsSize < 1) {
            final View child = findViewByPosition(mPendingScrollPosition);
            if (child != null) {
                anchorInfo.mPosition = mShouldReverseLayout ? getLastChildPosition() : getFirstChildPosition();
                if (mPendingScrollPositionOffset != INVALID_OFFSET) {
                    if (anchorInfo.mLayoutFromEnd) {
                        final int target = mPrimaryOrientation.getEndAfterPadding() - mPendingScrollPositionOffset;
                        anchorInfo.mOffset = target - mPrimaryOrientation.getDecoratedEnd(child);
                    } else {
                        final int target = mPrimaryOrientation.getStartAfterPadding() + mPendingScrollPositionOffset;
                        anchorInfo.mOffset = target - mPrimaryOrientation.getDecoratedStart(child);
                    }
                    return true;
                }

                final int childSize = mPrimaryOrientation.getDecoratedMeasurement(child);
                if (childSize > mPrimaryOrientation.getTotalSpace()) {
                    anchorInfo.mOffset = anchorInfo.mLayoutFromEnd ? mPrimaryOrientation.getEndAfterPadding() : mPrimaryOrientation.getStartAfterPadding();
                    return true;
                }

                final int startGap = mPrimaryOrientation.getDecoratedStart(child) - mPrimaryOrientation.getStartAfterPadding();
                if (startGap < 0) {
                    anchorInfo.mOffset = -startGap;
                    return true;
                }
                final int endGap = mPrimaryOrientation.getEndAfterPadding() - mPrimaryOrientation.getDecoratedEnd(child);
                if (endGap < 0) {
                    anchorInfo.mOffset = endGap;
                    return true;
                }
                anchorInfo.mOffset = INVALID_OFFSET;
            } else {
                anchorInfo.mPosition = mPendingScrollPosition;
                if (mPendingScrollPositionOffset == INVALID_OFFSET) {
                    final int position = calculateScrollDirectionForPosition(anchorInfo.mPosition);
                    anchorInfo.mLayoutFromEnd = position == LayoutState.LAYOUT_END;
                    anchorInfo.assignCoordinateFromPadding();
                } else {
                    anchorInfo.assignCoordinateFromPadding(mPendingScrollPositionOffset);
                }
                anchorInfo.mInvalidateOffsets = true;
            }
        } else {
            anchorInfo.mOffset = INVALID_OFFSET;
            anchorInfo.mPosition = mPendingScrollPosition;
        }
        return true;
    }

    void updateMeasureSpecs(int totalSpace) {
        mSizePerSpan = totalSpace / mSpanCount;
        mFullSizeSpec = View.MeasureSpec.makeMeasureSpec(totalSpace, mSecondaryOrientation.getMode());
    }

    @Override
    public boolean supportsPredictiveItemAnimations() {
        return mPendingSavedState == null;
    }

    public int[] findFirstVisibleItemPositions(int[] into) {
        if (into == null) {
            into = new int[mSpanCount];
        } else if (into.length < mSpanCount) {
            throw new IllegalArgumentException("Provided int[]'s size must be more than or equal" + " to span count. Expected:" + mSpanCount + ", array size:" + into.length);
        }
        for (int i = 0; i < mSpanCount; i++) {
            into[i] = mSpans[i].findFirstVisibleItemPosition();
        }
        return into;
    }

    public int[] findFirstCompletelyVisibleItemPositions(int[] into) {
        if (into == null) {
            into = new int[mSpanCount];
        } else if (into.length < mSpanCount) {
            throw new IllegalArgumentException("Provided int[]'s size must be more than or equal" + " to span count. Expected:" + mSpanCount + ", array size:" + into.length);
        }
        for (int i = 0; i < mSpanCount; i++) {
            into[i] = mSpans[i].findFirstCompletelyVisibleItemPosition();
        }
        return into;
    }

    public int[] findLastVisibleItemPositions(int[] into) {
        if (into == null) {
            into = new int[mSpanCount];
        } else if (into.length < mSpanCount) {
            throw new IllegalArgumentException("Provided int[]'s size must be more than or equal" + " to span count. Expected:" + mSpanCount + ", array size:" + into.length);
        }
        for (int i = 0; i < mSpanCount; i++) {
            into[i] = mSpans[i].findLastVisibleItemPosition();
        }
        return into;
    }

    public int[] findLastCompletelyVisibleItemPositions(int[] into) {
        if (into == null) {
            into = new int[mSpanCount];
        } else if (into.length < mSpanCount) {
            throw new IllegalArgumentException("Provided int[]'s size must be more than or equal" + " to span count. Expected:" + mSpanCount + ", array size:" + into.length);
        }
        for (int i = 0; i < mSpanCount; i++) {
            into[i] = mSpans[i].findLastCompletelyVisibleItemPosition();
        }
        return into;
    }

    @Override
    public int computeHorizontalScrollOffset(SeslRecyclerView.State state) {
        return computeScrollOffset(state);
    }

    private int computeScrollOffset(SeslRecyclerView.State state) {
        if (getChildCount() == 0) {
            return 0;
        }
        return ScrollbarHelper.computeScrollOffset(state, mPrimaryOrientation, findFirstVisibleItemClosestToStart(!mSmoothScrollbarEnabled), findFirstVisibleItemClosestToEnd(!mSmoothScrollbarEnabled), this, mSmoothScrollbarEnabled, mShouldReverseLayout);
    }

    @Override
    public int computeVerticalScrollOffset(SeslRecyclerView.State state) {
        return computeScrollOffset(state);
    }

    @Override
    public int computeHorizontalScrollExtent(SeslRecyclerView.State state) {
        return computeScrollExtent(state);
    }

    private int computeScrollExtent(SeslRecyclerView.State state) {
        if (getChildCount() == 0) {
            return 0;
        }
        return ScrollbarHelper.computeScrollExtent(state, mPrimaryOrientation, findFirstVisibleItemClosestToStart(!mSmoothScrollbarEnabled), findFirstVisibleItemClosestToEnd(!mSmoothScrollbarEnabled), this, mSmoothScrollbarEnabled);
    }

    @Override
    public int computeVerticalScrollExtent(SeslRecyclerView.State state) {
        return computeScrollExtent(state);
    }

    @Override
    public int computeHorizontalScrollRange(SeslRecyclerView.State state) {
        return computeScrollRange(state);
    }

    private int computeScrollRange(SeslRecyclerView.State state) {
        if (getChildCount() == 0) {
            return 0;
        }
        return ScrollbarHelper.computeScrollRange(state, mPrimaryOrientation, findFirstVisibleItemClosestToStart(!mSmoothScrollbarEnabled), findFirstVisibleItemClosestToEnd(!mSmoothScrollbarEnabled), this, mSmoothScrollbarEnabled);
    }

    @Override
    public int computeVerticalScrollRange(SeslRecyclerView.State state) {
        return computeScrollRange(state);
    }

    private void measureChildWithDecorationsAndMargin(View child, LayoutParams lp, boolean alreadyMeasured) {
        if (lp.mFullSpan) {
            if (mOrientation == VERTICAL) {
                measureChildWithDecorationsAndMargin(child, mFullSizeSpec, getChildMeasureSpec(getHeight(), getHeightMode(), getPaddingTop() + getPaddingBottom(), lp.height, true), alreadyMeasured);
            } else {
                measureChildWithDecorationsAndMargin(child, getChildMeasureSpec(getWidth(), getWidthMode(), getPaddingLeft() + getPaddingRight(), lp.width, true), mFullSizeSpec, alreadyMeasured);
            }
        } else {
            if (mOrientation == VERTICAL) {
                measureChildWithDecorationsAndMargin(child, getChildMeasureSpec(mSizePerSpan, getWidthMode(), 0, lp.width, false), getChildMeasureSpec(getHeight(), getHeightMode(), getPaddingTop() + getPaddingBottom(), lp.height, true), alreadyMeasured);
            } else {
                measureChildWithDecorationsAndMargin(child, getChildMeasureSpec(getWidth(), getWidthMode(), getPaddingLeft() + getPaddingRight(), lp.width, true), getChildMeasureSpec(mSizePerSpan, getHeightMode(), 0, lp.height, false), alreadyMeasured);
            }
        }
    }

    private void measureChildWithDecorationsAndMargin(View child, int widthSpec, int heightSpec, boolean alreadyMeasured) {
        calculateItemDecorationsForChild(child, mTmpRect);
        LayoutParams lp = (LayoutParams) child.getLayoutParams();
        widthSpec = updateSpecWithExtra(widthSpec, lp.leftMargin + mTmpRect.left, lp.rightMargin + mTmpRect.right);
        heightSpec = updateSpecWithExtra(heightSpec, lp.topMargin + mTmpRect.top, lp.bottomMargin + mTmpRect.bottom);
        final boolean measure = alreadyMeasured ? shouldReMeasureChild(child, widthSpec, heightSpec, lp) : shouldMeasureChild(child, widthSpec, heightSpec, lp);
        if (measure) {
            child.measure(widthSpec, heightSpec);
        }

    }

    private int updateSpecWithExtra(int spec, int startInset, int endInset) {
        if (startInset == 0 && endInset == 0) {
            return spec;
        }
        final int mode = View.MeasureSpec.getMode(spec);
        if (mode == View.MeasureSpec.AT_MOST || mode == View.MeasureSpec.EXACTLY) {
            return View.MeasureSpec.makeMeasureSpec(Math.max(0, View.MeasureSpec.getSize(spec) - startInset - endInset), mode);
        }
        return spec;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof SavedState) {
            mPendingSavedState = (SavedState) state;
            if (mPendingScrollPosition != SeslRecyclerView.NO_POSITION) {
                mPendingSavedState.invalidateAnchorPositionInfo();
                mPendingSavedState.invalidateSpanInfo();
            }
            requestLayout();
        } else {
            Log.d(TAG, "invalid saved state class");
        }
    }

    @Override
    public Parcelable onSaveInstanceState() {
        if (mPendingSavedState != null) {
            return new SavedState(mPendingSavedState);
        }
        SavedState state = new SavedState();
        state.mReverseLayout = mReverseLayout;
        state.mAnchorLayoutFromEnd = mLastLayoutFromEnd;
        state.mLastLayoutRTL = mLastLayoutRTL;

        if (mLazySpanLookup != null && mLazySpanLookup.mData != null) {
            state.mSpanLookup = mLazySpanLookup.mData;
            state.mSpanLookupSize = state.mSpanLookup.length;
            state.mFullSpanItems = mLazySpanLookup.mFullSpanItems;
        } else {
            state.mSpanLookupSize = 0;
        }

        if (getChildCount() > 0) {
            state.mAnchorPosition = mLastLayoutFromEnd ? getLastChildPosition() : getFirstChildPosition();
            state.mVisibleAnchorPosition = findFirstVisibleItemPositionInt();
            state.mSpanOffsetsSize = mSpanCount;
            state.mSpanOffsets = new int[mSpanCount];
            for (int i = 0; i < mSpanCount; i++) {
                int line;
                if (mLastLayoutFromEnd) {
                    line = mSpans[i].getEndLine(Span.INVALID_LINE);
                    if (line != Span.INVALID_LINE) {
                        line -= mPrimaryOrientation.getEndAfterPadding();
                    }
                } else {
                    line = mSpans[i].getStartLine(Span.INVALID_LINE);
                    if (line != Span.INVALID_LINE) {
                        line -= mPrimaryOrientation.getStartAfterPadding();
                    }
                }
                state.mSpanOffsets[i] = line;
            }
        } else {
            state.mAnchorPosition = SeslRecyclerView.NO_POSITION;
            state.mVisibleAnchorPosition = SeslRecyclerView.NO_POSITION;
            state.mSpanOffsetsSize = 0;
        }
        Log.d(TAG, "saved state:\n" + state);
        return state;
    }

    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        if (getChildCount() > 0) {
            final View start = findFirstVisibleItemClosestToStart(false);
            final View end = findFirstVisibleItemClosestToEnd(false);
            if (start == null || end == null) {
                return;
            }
            final int startPos = getPosition(start);
            final int endPos = getPosition(end);
            if (startPos < endPos) {
                event.setFromIndex(startPos);
                event.setToIndex(endPos);
            } else {
                event.setFromIndex(endPos);
                event.setToIndex(startPos);
            }
        }
    }

    int findFirstVisibleItemPositionInt() {
        final View first = mShouldReverseLayout ? findFirstVisibleItemClosestToEnd(true) : findFirstVisibleItemClosestToStart(true);
        return first == null ? SeslRecyclerView.NO_POSITION : getPosition(first);
    }

    View findFirstVisibleItemClosestToStart(boolean fullyVisible) {
        final int boundsStart = mPrimaryOrientation.getStartAfterPadding();
        final int boundsEnd = mPrimaryOrientation.getEndAfterPadding();
        final int limit = getChildCount();
        View partiallyVisible = null;
        for (int i = 0; i < limit; i++) {
            final View child = getChildAt(i);
            final int childStart = mPrimaryOrientation.getDecoratedStart(child);
            final int childEnd = mPrimaryOrientation.getDecoratedEnd(child);
            if (childEnd <= boundsStart || childStart >= boundsEnd) {
                continue;
            }
            if (childStart >= boundsStart || !fullyVisible) {
                return child;
            }
            if (partiallyVisible == null) {
                partiallyVisible = child;
            }
        }
        return partiallyVisible;
    }

    View findFirstVisibleItemClosestToEnd(boolean fullyVisible) {
        final int boundsStart = mPrimaryOrientation.getStartAfterPadding();
        final int boundsEnd = mPrimaryOrientation.getEndAfterPadding();
        View partiallyVisible = null;
        for (int i = getChildCount() - 1; i >= 0; i--) {
            final View child = getChildAt(i);
            final int childStart = mPrimaryOrientation.getDecoratedStart(child);
            final int childEnd = mPrimaryOrientation.getDecoratedEnd(child);
            if (childEnd <= boundsStart || childStart >= boundsEnd) {
                continue;
            }
            if (childEnd <= boundsEnd || !fullyVisible) {
                return child;
            }
            if (partiallyVisible == null) {
                partiallyVisible = child;
            }
        }
        return partiallyVisible;
    }

    private void fixEndGap(SeslRecyclerView.Recycler recycler, SeslRecyclerView.State state, boolean canOffsetChildren) {
        final int maxEndLine = getMaxEnd(Integer.MIN_VALUE);
        if (maxEndLine == Integer.MIN_VALUE) {
            return;
        }
        int gap = mPrimaryOrientation.getEndAfterPadding() - maxEndLine;
        int fixOffset;
        if (gap > 0) {
            fixOffset = -scrollBy(-gap, recycler, state);
        } else {
            return;
        }
        gap -= fixOffset;
        if (canOffsetChildren && gap > 0) {
            mPrimaryOrientation.offsetChildren(gap);
        }
    }

    private void fixStartGap(SeslRecyclerView.Recycler recycler, SeslRecyclerView.State state,
                             boolean canOffsetChildren) {
        final int minStartLine = getMinStart(Integer.MAX_VALUE);
        if (minStartLine == Integer.MAX_VALUE) {
            return;
        }
        int gap = minStartLine - mPrimaryOrientation.getStartAfterPadding();
        int fixOffset;
        if (gap > 0) {
            fixOffset = scrollBy(gap, recycler, state);
        } else {
            return;
        }
        gap -= fixOffset;
        if (canOffsetChildren && gap > 0) {
            mPrimaryOrientation.offsetChildren(-gap);
        }
    }

    private void updateLayoutState(int anchorPosition, SeslRecyclerView.State state) {
        mLayoutState.mAvailable = 0;
        mLayoutState.mCurrentPosition = anchorPosition;
        int startExtra = 0;
        int endExtra = 0;
        if (isSmoothScrolling()) {
            final int targetPos = state.getTargetScrollPosition();
            if (targetPos != SeslRecyclerView.NO_POSITION) {
                if (mShouldReverseLayout == targetPos < anchorPosition) {
                    endExtra = mPrimaryOrientation.getTotalSpace();
                } else {
                    startExtra = mPrimaryOrientation.getTotalSpace();
                }
            }
        }

        final boolean clipToPadding = getClipToPadding();
        if (clipToPadding) {
            mLayoutState.mStartLine = mPrimaryOrientation.getStartAfterPadding() - startExtra;
            mLayoutState.mEndLine = mPrimaryOrientation.getEndAfterPadding() + endExtra;
        } else {
            mLayoutState.mEndLine = mPrimaryOrientation.getEnd() + endExtra;
            mLayoutState.mStartLine = -startExtra;
        }
        mLayoutState.mStopInFocusable = false;
        mLayoutState.mRecycle = true;
        mLayoutState.mInfinite = mPrimaryOrientation.getMode() == View.MeasureSpec.UNSPECIFIED && mPrimaryOrientation.getEnd() == 0;
    }

    private void setLayoutStateDirection(int direction) {
        mLayoutState.mLayoutDirection = direction;
        mLayoutState.mItemDirection = (mShouldReverseLayout == (direction == LayoutState.LAYOUT_START)) ? LayoutState.ITEM_DIRECTION_TAIL : LayoutState.ITEM_DIRECTION_HEAD;
    }

    @Override
    public void offsetChildrenHorizontal(int dx) {
        super.offsetChildrenHorizontal(dx);
        for (int i = 0; i < mSpanCount; i++) {
            mSpans[i].onOffset(dx);
        }
    }

    @Override
    public void offsetChildrenVertical(int dy) {
        super.offsetChildrenVertical(dy);
        for (int i = 0; i < mSpanCount; i++) {
            mSpans[i].onOffset(dy);
        }
    }

    @Override
    public void onItemsRemoved(SeslRecyclerView recyclerView, int positionStart, int itemCount) {
        handleUpdate(positionStart, itemCount, SeslAdapterHelper.UpdateOp.REMOVE);
    }

    @Override
    public void onItemsAdded(SeslRecyclerView recyclerView, int positionStart, int itemCount) {
        handleUpdate(positionStart, itemCount, SeslAdapterHelper.UpdateOp.ADD);
    }

    @Override
    public void onItemsChanged(SeslRecyclerView recyclerView) {
        mLazySpanLookup.clear();
        requestLayout();
    }

    @Override
    public void onItemsMoved(SeslRecyclerView recyclerView, int from, int to, int itemCount) {
        handleUpdate(from, to, SeslAdapterHelper.UpdateOp.MOVE);
    }

    @Override
    public void onItemsUpdated(SeslRecyclerView recyclerView, int positionStart, int itemCount, Object payload) {
        handleUpdate(positionStart, itemCount, SeslAdapterHelper.UpdateOp.UPDATE);
    }

    private void handleUpdate(int positionStart, int itemCountOrToPosition, int cmd) {
        int minPosition = mShouldReverseLayout ? getLastChildPosition() : getFirstChildPosition();
        final int affectedRangeEnd;
        final int affectedRangeStart;

        if (cmd == SeslAdapterHelper.UpdateOp.MOVE) {
            if (positionStart < itemCountOrToPosition) {
                affectedRangeEnd = itemCountOrToPosition + 1;
                affectedRangeStart = positionStart;
            } else {
                affectedRangeEnd = positionStart + 1;
                affectedRangeStart = itemCountOrToPosition;
            }
        } else {
            affectedRangeStart = positionStart;
            affectedRangeEnd = positionStart + itemCountOrToPosition;
        }

        mLazySpanLookup.invalidateAfter(affectedRangeStart);
        switch (cmd) {
            case SeslAdapterHelper.UpdateOp.ADD:
                mLazySpanLookup.offsetForAddition(positionStart, itemCountOrToPosition);
                break;
            case SeslAdapterHelper.UpdateOp.REMOVE:
                mLazySpanLookup.offsetForRemoval(positionStart, itemCountOrToPosition);
                break;
            case SeslAdapterHelper.UpdateOp.MOVE:
                mLazySpanLookup.offsetForRemoval(positionStart, 1);
                mLazySpanLookup.offsetForAddition(itemCountOrToPosition, 1);
                break;
        }

        if (affectedRangeEnd <= minPosition) {
            return;
        }

        int maxPosition = mShouldReverseLayout ? getFirstChildPosition() : getLastChildPosition();
        if (affectedRangeStart <= maxPosition) {
            requestLayout();
        }
    }

    private int fill(SeslRecyclerView.Recycler recycler, LayoutState layoutState, SeslRecyclerView.State state) {
        mRemainingSpans.set(0, mSpanCount, true);
        final int targetLine;

        if (mLayoutState.mInfinite) {
            if (layoutState.mLayoutDirection == LayoutState.LAYOUT_END) {
                targetLine = Integer.MAX_VALUE;
            } else {
                targetLine = Integer.MIN_VALUE;
            }
        } else {
            if (layoutState.mLayoutDirection == LayoutState.LAYOUT_END) {
                targetLine = layoutState.mEndLine + layoutState.mAvailable;
            } else {
                targetLine = layoutState.mStartLine - layoutState.mAvailable;
            }
        }

        updateAllRemainingSpans(layoutState.mLayoutDirection, targetLine);
        Log.d(TAG, "FILLING targetLine: " + targetLine + "," + "remaining spans:" + mRemainingSpans + ", state: " + layoutState);

        final int defaultNewViewLine = mShouldReverseLayout ? mPrimaryOrientation.getEndAfterPadding() : mPrimaryOrientation.getStartAfterPadding();
        boolean added = false;
        while (layoutState.hasMore(state) && (mLayoutState.mInfinite || !mRemainingSpans.isEmpty())) {
            View view = layoutState.next(recycler);
            LayoutParams lp = ((LayoutParams) view.getLayoutParams());
            final int position = lp.getViewLayoutPosition();
            final int spanIndex = mLazySpanLookup.getSpan(position);
            Span currentSpan;
            final boolean assignSpan = spanIndex == LayoutParams.INVALID_SPAN_ID;
            if (assignSpan) {
                currentSpan = lp.mFullSpan ? mSpans[0] : getNextSpan(layoutState);
                mLazySpanLookup.setSpan(position, currentSpan);
                Log.d(TAG, "assigned " + currentSpan.mIndex + " for " + position);
            } else {
                Log.d(TAG, "using " + spanIndex + " for pos " + position);
                currentSpan = mSpans[spanIndex];
            }
            lp.mSpan = currentSpan;
            if (layoutState.mLayoutDirection == LayoutState.LAYOUT_END) {
                addView(view);
            } else {
                addView(view, 0);
            }
            measureChildWithDecorationsAndMargin(view, lp, false);

            final int start;
            final int end;
            if (layoutState.mLayoutDirection == LayoutState.LAYOUT_END) {
                start = lp.mFullSpan ? getMaxEnd(defaultNewViewLine) : currentSpan.getEndLine(defaultNewViewLine);
                end = start + mPrimaryOrientation.getDecoratedMeasurement(view);
                if (assignSpan && lp.mFullSpan) {
                    LazySpanLookup.FullSpanItem fullSpanItem;
                    fullSpanItem = createFullSpanItemFromEnd(start);
                    fullSpanItem.mGapDir = LayoutState.LAYOUT_START;
                    fullSpanItem.mPosition = position;
                    mLazySpanLookup.addFullSpanItem(fullSpanItem);
                }
            } else {
                end = lp.mFullSpan ? getMinStart(defaultNewViewLine) : currentSpan.getStartLine(defaultNewViewLine);
                start = end - mPrimaryOrientation.getDecoratedMeasurement(view);
                if (assignSpan && lp.mFullSpan) {
                    LazySpanLookup.FullSpanItem fullSpanItem;
                    fullSpanItem = createFullSpanItemFromStart(end);
                    fullSpanItem.mGapDir = LayoutState.LAYOUT_END;
                    fullSpanItem.mPosition = position;
                    mLazySpanLookup.addFullSpanItem(fullSpanItem);
                }
            }

            if (lp.mFullSpan && layoutState.mItemDirection == LayoutState.ITEM_DIRECTION_HEAD) {
                if (assignSpan) {
                    mLaidOutInvalidFullSpan = true;
                } else {
                    final boolean hasInvalidGap;
                    if (layoutState.mLayoutDirection == LayoutState.LAYOUT_END) {
                        hasInvalidGap = !areAllEndsEqual();
                    } else {
                        hasInvalidGap = !areAllStartsEqual();
                    }
                    if (hasInvalidGap) {
                        final LazySpanLookup.FullSpanItem fullSpanItem = mLazySpanLookup.getFullSpanItem(position);
                        if (fullSpanItem != null) {
                            fullSpanItem.mHasUnwantedGapAfter = true;
                        }
                        mLaidOutInvalidFullSpan = true;
                    }
                }
            }
            attachViewToSpans(view, lp, layoutState);
            final int otherStart;
            final int otherEnd;
            if (isLayoutRTL() && mOrientation == VERTICAL) {
                otherEnd = lp.mFullSpan ? mSecondaryOrientation.getEndAfterPadding() : mSecondaryOrientation.getEndAfterPadding() - (mSpanCount - 1 - currentSpan.mIndex) * mSizePerSpan;
                otherStart = otherEnd - mSecondaryOrientation.getDecoratedMeasurement(view);
            } else {
                otherStart = lp.mFullSpan ? mSecondaryOrientation.getStartAfterPadding() : currentSpan.mIndex * mSizePerSpan + mSecondaryOrientation.getStartAfterPadding();
                otherEnd = otherStart + mSecondaryOrientation.getDecoratedMeasurement(view);
            }

            if (mOrientation == VERTICAL) {
                layoutDecoratedWithMargins(view, otherStart, start, otherEnd, end);
            } else {
                layoutDecoratedWithMargins(view, start, otherStart, end, otherEnd);
            }

            if (lp.mFullSpan) {
                updateAllRemainingSpans(mLayoutState.mLayoutDirection, targetLine);
            } else {
                updateRemainingSpans(currentSpan, mLayoutState.mLayoutDirection, targetLine);
            }
            recycle(recycler, mLayoutState);
            if (mLayoutState.mStopInFocusable && view.hasFocusable()) {
                if (lp.mFullSpan) {
                    mRemainingSpans.clear();
                } else {
                    mRemainingSpans.set(currentSpan.mIndex, false);
                }
            }
            added = true;
        }
        if (!added) {
            recycle(recycler, mLayoutState);
        }
        final int diff;
        if (mLayoutState.mLayoutDirection == LayoutState.LAYOUT_START) {
            final int minStart = getMinStart(mPrimaryOrientation.getStartAfterPadding());
            diff = mPrimaryOrientation.getStartAfterPadding() - minStart;
        } else {
            final int maxEnd = getMaxEnd(mPrimaryOrientation.getEndAfterPadding());
            diff = maxEnd - mPrimaryOrientation.getEndAfterPadding();
        }
        return diff > 0 ? Math.min(layoutState.mAvailable, diff) : 0;
    }

    private LazySpanLookup.FullSpanItem createFullSpanItemFromEnd(int newItemTop) {
        LazySpanLookup.FullSpanItem fsi = new LazySpanLookup.FullSpanItem();
        fsi.mGapPerSpan = new int[mSpanCount];
        for (int i = 0; i < mSpanCount; i++) {
            fsi.mGapPerSpan[i] = newItemTop - mSpans[i].getEndLine(newItemTop);
        }
        return fsi;
    }

    private LazySpanLookup.FullSpanItem createFullSpanItemFromStart(int newItemBottom) {
        LazySpanLookup.FullSpanItem fsi = new LazySpanLookup.FullSpanItem();
        fsi.mGapPerSpan = new int[mSpanCount];
        for (int i = 0; i < mSpanCount; i++) {
            fsi.mGapPerSpan[i] = mSpans[i].getStartLine(newItemBottom) - newItemBottom;
        }
        return fsi;
    }

    private void attachViewToSpans(View view, LayoutParams lp, LayoutState layoutState) {
        if (layoutState.mLayoutDirection == LayoutState.LAYOUT_END) {
            if (lp.mFullSpan) {
                appendViewToAllSpans(view);
            } else {
                lp.mSpan.appendToSpan(view);
            }
        } else {
            if (lp.mFullSpan) {
                prependViewToAllSpans(view);
            } else {
                lp.mSpan.prependToSpan(view);
            }
        }
    }

    private void recycle(SeslRecyclerView.Recycler recycler, LayoutState layoutState) {
        if (!layoutState.mRecycle || layoutState.mInfinite) {
            return;
        }
        if (layoutState.mAvailable == 0) {
            if (layoutState.mLayoutDirection == LayoutState.LAYOUT_START) {
                recycleFromEnd(recycler, layoutState.mEndLine);
            } else {
                recycleFromStart(recycler, layoutState.mStartLine);
            }
        } else {
            if (layoutState.mLayoutDirection == LayoutState.LAYOUT_START) {
                int scrolled = layoutState.mStartLine - getMaxStart(layoutState.mStartLine);
                final int line;
                if (scrolled < 0) {
                    line = layoutState.mEndLine;
                } else {
                    line = layoutState.mEndLine - Math.min(scrolled, layoutState.mAvailable);
                }
                recycleFromEnd(recycler, line);
            } else {
                int scrolled = getMinEnd(layoutState.mEndLine) - layoutState.mEndLine;
                final int line;
                if (scrolled < 0) {
                    line = layoutState.mStartLine;
                } else {
                    line = layoutState.mStartLine + Math.min(scrolled, layoutState.mAvailable);
                }
                recycleFromStart(recycler, line);
            }
        }

    }

    private void appendViewToAllSpans(View view) {
        for (int i = mSpanCount - 1; i >= 0; i--) {
            mSpans[i].appendToSpan(view);
        }
    }

    private void prependViewToAllSpans(View view) {
        for (int i = mSpanCount - 1; i >= 0; i--) {
            mSpans[i].prependToSpan(view);
        }
    }

    private void updateAllRemainingSpans(int layoutDir, int targetLine) {
        for (int i = 0; i < mSpanCount; i++) {
            if (mSpans[i].mViews.isEmpty()) {
                continue;
            }
            updateRemainingSpans(mSpans[i], layoutDir, targetLine);
        }
    }

    private void updateRemainingSpans(Span span, int layoutDir, int targetLine) {
        final int deletedSize = span.getDeletedSize();
        if (layoutDir == LayoutState.LAYOUT_START) {
            final int line = span.getStartLine();
            if (line + deletedSize <= targetLine) {
                mRemainingSpans.set(span.mIndex, false);
            }
        } else {
            final int line = span.getEndLine();
            if (line - deletedSize >= targetLine) {
                mRemainingSpans.set(span.mIndex, false);
            }
        }
    }

    private int getMaxStart(int def) {
        int maxStart = mSpans[0].getStartLine(def);
        for (int i = 1; i < mSpanCount; i++) {
            final int spanStart = mSpans[i].getStartLine(def);
            if (spanStart > maxStart) {
                maxStart = spanStart;
            }
        }
        return maxStart;
    }

    private int getMinStart(int def) {
        int minStart = mSpans[0].getStartLine(def);
        for (int i = 1; i < mSpanCount; i++) {
            final int spanStart = mSpans[i].getStartLine(def);
            if (spanStart < minStart) {
                minStart = spanStart;
            }
        }
        return minStart;
    }

    boolean areAllEndsEqual() {
        int end = mSpans[0].getEndLine(Span.INVALID_LINE);
        for (int i = 1; i < mSpanCount; i++) {
            if (mSpans[i].getEndLine(Span.INVALID_LINE) != end) {
                return false;
            }
        }
        return true;
    }

    boolean areAllStartsEqual() {
        int start = mSpans[0].getStartLine(Span.INVALID_LINE);
        for (int i = 1; i < mSpanCount; i++) {
            if (mSpans[i].getStartLine(Span.INVALID_LINE) != start) {
                return false;
            }
        }
        return true;
    }

    private int getMaxEnd(int def) {
        int maxEnd = mSpans[0].getEndLine(def);
        for (int i = 1; i < mSpanCount; i++) {
            final int spanEnd = mSpans[i].getEndLine(def);
            if (spanEnd > maxEnd) {
                maxEnd = spanEnd;
            }
        }
        return maxEnd;
    }

    private int getMinEnd(int def) {
        int minEnd = mSpans[0].getEndLine(def);
        for (int i = 1; i < mSpanCount; i++) {
            final int spanEnd = mSpans[i].getEndLine(def);
            if (spanEnd < minEnd) {
                minEnd = spanEnd;
            }
        }
        return minEnd;
    }

    private void recycleFromStart(SeslRecyclerView.Recycler recycler, int line) {
        while (getChildCount() > 0) {
            View child = getChildAt(0);
            if (mPrimaryOrientation.getDecoratedEnd(child) <= line
                    && mPrimaryOrientation.getTransformedEndWithDecoration(child) <= line) {
                LayoutParams lp = (LayoutParams) child.getLayoutParams();
                if (lp.mFullSpan) {
                    for (int j = 0; j < mSpanCount; j++) {
                        if (mSpans[j].mViews.size() == 1) {
                            return;
                        }
                    }
                    for (int j = 0; j < mSpanCount; j++) {
                        mSpans[j].popStart();
                    }
                } else {
                    if (lp.mSpan.mViews.size() == 1) {
                        return;
                    }
                    lp.mSpan.popStart();
                }
                removeAndRecycleView(child, recycler);
            } else {
                return;
            }
        }
    }

    private void recycleFromEnd(SeslRecyclerView.Recycler recycler, int line) {
        final int childCount = getChildCount();
        int i;
        for (i = childCount - 1; i >= 0; i--) {
            View child = getChildAt(i);
            if (mPrimaryOrientation.getDecoratedStart(child) >= line && mPrimaryOrientation.getTransformedStartWithDecoration(child) >= line) {
                LayoutParams lp = (LayoutParams) child.getLayoutParams();
                if (lp.mFullSpan) {
                    for (int j = 0; j < mSpanCount; j++) {
                        if (mSpans[j].mViews.size() == 1) {
                            return;
                        }
                    }
                    for (int j = 0; j < mSpanCount; j++) {
                        mSpans[j].popEnd();
                    }
                } else {
                    if (lp.mSpan.mViews.size() == 1) {
                        return;
                    }
                    lp.mSpan.popEnd();
                }
                removeAndRecycleView(child, recycler);
            } else {
                return;
            }
        }
    }

    private boolean preferLastSpan(int layoutDir) {
        if (mOrientation == HORIZONTAL) {
            return (layoutDir == LayoutState.LAYOUT_START) != mShouldReverseLayout;
        }
        return ((layoutDir == LayoutState.LAYOUT_START) == mShouldReverseLayout) == isLayoutRTL();
    }

    private Span getNextSpan(LayoutState layoutState) {
        final boolean preferLastSpan = preferLastSpan(layoutState.mLayoutDirection);
        final int startIndex, endIndex, diff;
        if (preferLastSpan) {
            startIndex = mSpanCount - 1;
            endIndex = -1;
            diff = -1;
        } else {
            startIndex = 0;
            endIndex = mSpanCount;
            diff = 1;
        }
        if (layoutState.mLayoutDirection == LayoutState.LAYOUT_END) {
            Span min = null;
            int minLine = Integer.MAX_VALUE;
            final int defaultLine = mPrimaryOrientation.getStartAfterPadding();
            for (int i = startIndex; i != endIndex; i += diff) {
                final Span other = mSpans[i];
                int otherLine = other.getEndLine(defaultLine);
                if (otherLine < minLine) {
                    min = other;
                    minLine = otherLine;
                }
            }
            return min;
        } else {
            Span max = null;
            int maxLine = Integer.MIN_VALUE;
            final int defaultLine = mPrimaryOrientation.getEndAfterPadding();
            for (int i = startIndex; i != endIndex; i += diff) {
                final Span other = mSpans[i];
                int otherLine = other.getStartLine(defaultLine);
                if (otherLine > maxLine) {
                    max = other;
                    maxLine = otherLine;
                }
            }
            return max;
        }
    }

    @Override
    public boolean canScrollVertically() {
        return mOrientation == VERTICAL;
    }

    @Override
    public boolean canScrollHorizontally() {
        return mOrientation == HORIZONTAL;
    }

    @Override
    public int scrollHorizontallyBy(int dx, SeslRecyclerView.Recycler recycler, SeslRecyclerView.State state) {
        return scrollBy(dx, recycler, state);
    }

    @Override
    public int scrollVerticallyBy(int dy, SeslRecyclerView.Recycler recycler, SeslRecyclerView.State state) {
        return scrollBy(dy, recycler, state);
    }

    private int calculateScrollDirectionForPosition(int position) {
        if (getChildCount() == 0) {
            return mShouldReverseLayout ? LayoutState.LAYOUT_END : LayoutState.LAYOUT_START;
        }
        final int firstChildPos = getFirstChildPosition();
        return position < firstChildPos != mShouldReverseLayout ? LayoutState.LAYOUT_START : LayoutState.LAYOUT_END;
    }

    @Override
    public PointF computeScrollVectorForPosition(int targetPosition) {
        final int direction = calculateScrollDirectionForPosition(targetPosition);
        PointF outVector = new PointF();
        if (direction == 0) {
            return null;
        }
        if (mOrientation == HORIZONTAL) {
            outVector.x = direction;
            outVector.y = 0;
        } else {
            outVector.x = 0;
            outVector.y = direction;
        }
        return outVector;
    }

    @Override
    public void smoothScrollToPosition(SeslRecyclerView recyclerView, SeslRecyclerView.State state, int position) {
        LinearSmoothScroller scroller = new LinearSmoothScroller(recyclerView.getContext());
        scroller.setTargetPosition(position);
        startSmoothScroll(scroller);
    }

    @Override
    public void scrollToPosition(int position) {
        if (mPendingSavedState != null && mPendingSavedState.mAnchorPosition != position) {
            mPendingSavedState.invalidateAnchorPositionInfo();
        }
        mPendingScrollPosition = position;
        mPendingScrollPositionOffset = INVALID_OFFSET;
        requestLayout();
    }

    public void scrollToPositionWithOffset(int position, int offset) {
        if (mPendingSavedState != null) {
            mPendingSavedState.invalidateAnchorPositionInfo();
        }
        mPendingScrollPosition = position;
        mPendingScrollPositionOffset = offset;
        requestLayout();
    }

    /**
     * @hide
     */
    @Override
    public void collectAdjacentPrefetchPositions(int dx, int dy, SeslRecyclerView.State state, LayoutPrefetchRegistry layoutPrefetchRegistry) {
        int delta = (mOrientation == HORIZONTAL) ? dx : dy;
        if (getChildCount() == 0 || delta == 0) {
            return;
        }
        prepareLayoutStateForDelta(delta, state);

        if (mPrefetchDistances == null || mPrefetchDistances.length < mSpanCount) {
            mPrefetchDistances = new int[mSpanCount];
        }

        int itemPrefetchCount = 0;
        for (int i = 0; i < mSpanCount; i++) {
            int distance = mLayoutState.mItemDirection == LayoutState.LAYOUT_START ? mLayoutState.mStartLine - mSpans[i].getStartLine(mLayoutState.mStartLine) : mSpans[i].getEndLine(mLayoutState.mEndLine) - mLayoutState.mEndLine;
            if (distance >= 0) {
                mPrefetchDistances[itemPrefetchCount] = distance;
                itemPrefetchCount++;
            }
        }
        Arrays.sort(mPrefetchDistances, 0, itemPrefetchCount);

        for (int i = 0; i < itemPrefetchCount && mLayoutState.hasMore(state); i++) {
            layoutPrefetchRegistry.addPosition(mLayoutState.mCurrentPosition, mPrefetchDistances[i]);
            mLayoutState.mCurrentPosition += mLayoutState.mItemDirection;
        }
    }

    void prepareLayoutStateForDelta(int delta, SeslRecyclerView.State state) {
        final int referenceChildPosition;
        final int layoutDir;
        if (delta > 0) {
            layoutDir = LayoutState.LAYOUT_END;
            referenceChildPosition = getLastChildPosition();
        } else {
            layoutDir = LayoutState.LAYOUT_START;
            referenceChildPosition = getFirstChildPosition();
        }
        mLayoutState.mRecycle = true;
        updateLayoutState(referenceChildPosition, state);
        setLayoutStateDirection(layoutDir);
        mLayoutState.mCurrentPosition = referenceChildPosition + mLayoutState.mItemDirection;
        mLayoutState.mAvailable = Math.abs(delta);
    }

    int scrollBy(int dt, SeslRecyclerView.Recycler recycler, SeslRecyclerView.State state) {
        if (getChildCount() == 0 || dt == 0) {
            return 0;
        }

        prepareLayoutStateForDelta(dt, state);
        int consumed = fill(recycler, mLayoutState, state);
        final int available = mLayoutState.mAvailable;
        final int totalScroll;
        if (available < consumed) {
            totalScroll = dt;
        } else if (dt < 0) {
            totalScroll = -consumed;
        } else {
            totalScroll = consumed;
        }
        Log.d(TAG, "asked " + dt + " scrolled" + totalScroll);

        mPrimaryOrientation.offsetChildren(-totalScroll);
        mLastLayoutFromEnd = mShouldReverseLayout;
        mLayoutState.mAvailable = 0;
        recycle(recycler, mLayoutState);
        return totalScroll;
    }

    int getLastChildPosition() {
        final int childCount = getChildCount();
        return childCount == 0 ? 0 : getPosition(getChildAt(childCount - 1));
    }

    int getFirstChildPosition() {
        final int childCount = getChildCount();
        return childCount == 0 ? 0 : getPosition(getChildAt(0));
    }

    private int findFirstReferenceChildPosition(int itemCount) {
        final int limit = getChildCount();
        for (int i = 0; i < limit; i++) {
            final View view = getChildAt(i);
            final int position = getPosition(view);
            if (position >= 0 && position < itemCount) {
                return position;
            }
        }
        return 0;
    }

    private int findLastReferenceChildPosition(int itemCount) {
        for (int i = getChildCount() - 1; i >= 0; i--) {
            final View view = getChildAt(i);
            final int position = getPosition(view);
            if (position >= 0 && position < itemCount) {
                return position;
            }
        }
        return 0;
    }

    @SuppressWarnings("deprecation")
    @Override
    public SeslRecyclerView.LayoutParams generateDefaultLayoutParams() {
        if (mOrientation == HORIZONTAL) {
            return new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        } else {
            return new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    @Override
    public SeslRecyclerView.LayoutParams generateLayoutParams(Context c, AttributeSet attrs) {
        return new LayoutParams(c, attrs);
    }

    @Override
    public SeslRecyclerView.LayoutParams generateLayoutParams(ViewGroup.LayoutParams lp) {
        if (lp instanceof ViewGroup.MarginLayoutParams) {
            return new LayoutParams((ViewGroup.MarginLayoutParams) lp);
        } else {
            return new LayoutParams(lp);
        }
    }

    @Override
    public boolean checkLayoutParams(SeslRecyclerView.LayoutParams lp) {
        return lp instanceof LayoutParams;
    }

    public int getOrientation() {
        return mOrientation;
    }

    public void setOrientation(int orientation) {
        if (orientation != HORIZONTAL && orientation != VERTICAL) {
            throw new IllegalArgumentException("invalid orientation.");
        }
        assertNotInLayoutOrScroll(null);
        if (orientation == mOrientation) {
            return;
        }
        mOrientation = orientation;
        OrientationHelper tmp = mPrimaryOrientation;
        mPrimaryOrientation = mSecondaryOrientation;
        mSecondaryOrientation = tmp;
        requestLayout();
    }

    @Nullable
    @Override
    public View onFocusSearchFailed(View focused, int direction, SeslRecyclerView.Recycler recycler, SeslRecyclerView.State state) {
        if (getChildCount() == 0) {
            return null;
        }

        final View directChild = findContainingItemView(focused);
        if (directChild == null) {
            return null;
        }

        resolveShouldLayoutReverse();
        final int layoutDir = convertFocusDirectionToLayoutDirection(direction);
        if (layoutDir == LayoutState.INVALID_LAYOUT) {
            return null;
        }
        LayoutParams prevFocusLayoutParams = (LayoutParams) directChild.getLayoutParams();
        boolean prevFocusFullSpan = prevFocusLayoutParams.mFullSpan;
        final Span prevFocusSpan = prevFocusLayoutParams.mSpan;
        final int referenceChildPosition;
        if (layoutDir == LayoutState.LAYOUT_END) {
            referenceChildPosition = getLastChildPosition();
        } else {
            referenceChildPosition = getFirstChildPosition();
        }
        updateLayoutState(referenceChildPosition, state);
        setLayoutStateDirection(layoutDir);

        mLayoutState.mCurrentPosition = referenceChildPosition + mLayoutState.mItemDirection;
        mLayoutState.mAvailable = (int) (MAX_SCROLL_FACTOR * mPrimaryOrientation.getTotalSpace());
        mLayoutState.mStopInFocusable = true;
        mLayoutState.mRecycle = false;
        fill(recycler, mLayoutState, state);
        mLastLayoutFromEnd = mShouldReverseLayout;
        if (!prevFocusFullSpan) {
            View view = prevFocusSpan.getFocusableViewAfter(referenceChildPosition, layoutDir);
            if (view != null && view != directChild) {
                return view;
            }
        }

        if (preferLastSpan(layoutDir)) {
            for (int i = mSpanCount - 1; i >= 0; i--) {
                View view = mSpans[i].getFocusableViewAfter(referenceChildPosition, layoutDir);
                if (view != null && view != directChild) {
                    return view;
                }
            }
        } else {
            for (int i = 0; i < mSpanCount; i++) {
                View view = mSpans[i].getFocusableViewAfter(referenceChildPosition, layoutDir);
                if (view != null && view != directChild) {
                    return view;
                }
            }
        }

        boolean shouldSearchFromStart = !mReverseLayout == (layoutDir == LayoutState.LAYOUT_START);
        View unfocusableCandidate = null;
        if (!prevFocusFullSpan) {
            unfocusableCandidate = findViewByPosition(shouldSearchFromStart ? prevFocusSpan.findFirstPartiallyVisibleItemPosition() : prevFocusSpan.findLastPartiallyVisibleItemPosition());
            if (unfocusableCandidate != null && unfocusableCandidate != directChild) {
                return unfocusableCandidate;
            }
        }

        if (preferLastSpan(layoutDir)) {
            for (int i = mSpanCount - 1; i >= 0; i--) {
                if (i == prevFocusSpan.mIndex) {
                    continue;
                }
                unfocusableCandidate = findViewByPosition(shouldSearchFromStart ? mSpans[i].findFirstPartiallyVisibleItemPosition() : mSpans[i].findLastPartiallyVisibleItemPosition());
                if (unfocusableCandidate != null && unfocusableCandidate != directChild) {
                    return unfocusableCandidate;
                }
            }
        } else {
            for (int i = 0; i < mSpanCount; i++) {
                unfocusableCandidate = findViewByPosition(shouldSearchFromStart ? mSpans[i].findFirstPartiallyVisibleItemPosition() : mSpans[i].findLastPartiallyVisibleItemPosition());
                if (unfocusableCandidate != null && unfocusableCandidate != directChild) {
                    return unfocusableCandidate;
                }
            }
        }
        return null;
    }

    private int convertFocusDirectionToLayoutDirection(int focusDirection) {
        switch (focusDirection) {
            case View.FOCUS_BACKWARD:
                if (mOrientation == VERTICAL) {
                    return LayoutState.LAYOUT_START;
                } else if (isLayoutRTL()) {
                    return LayoutState.LAYOUT_END;
                } else {
                    return LayoutState.LAYOUT_START;
                }
            case View.FOCUS_FORWARD:
                if (mOrientation == VERTICAL) {
                    return LayoutState.LAYOUT_END;
                } else if (isLayoutRTL()) {
                    return LayoutState.LAYOUT_START;
                } else {
                    return LayoutState.LAYOUT_END;
                }
            case View.FOCUS_UP:
                return mOrientation == VERTICAL ? LayoutState.LAYOUT_START : LayoutState.INVALID_LAYOUT;
            case View.FOCUS_DOWN:
                return mOrientation == VERTICAL ? LayoutState.LAYOUT_END : LayoutState.INVALID_LAYOUT;
            case View.FOCUS_LEFT:
                return mOrientation == HORIZONTAL ? LayoutState.LAYOUT_START : LayoutState.INVALID_LAYOUT;
            case View.FOCUS_RIGHT:
                return mOrientation == HORIZONTAL ? LayoutState.LAYOUT_END : LayoutState.INVALID_LAYOUT;
            default:
                Log.d(TAG, "Unknown focus request:" + focusDirection);
                return LayoutState.INVALID_LAYOUT;
        }

    }


    public static class LayoutParams extends SeslRecyclerView.LayoutParams {
        public static final int INVALID_SPAN_ID = -1;
        Span mSpan;
        boolean mFullSpan;

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

        public LayoutParams(SeslRecyclerView.LayoutParams source) {
            super(source);
        }

        public boolean isFullSpan() {
            return mFullSpan;
        }

        public void setFullSpan(boolean fullSpan) {
            mFullSpan = fullSpan;
        }

        public final int getSpanIndex() {
            if (mSpan == null) {
                return INVALID_SPAN_ID;
            }
            return mSpan.mIndex;
        }
    }

    static class LazySpanLookup {
        private static final int MIN_SIZE = 10;
        int[] mData;
        List<FullSpanItem> mFullSpanItems;

        int forceInvalidateAfter(int position) {
            if (mFullSpanItems != null) {
                for (int i = mFullSpanItems.size() - 1; i >= 0; i--) {
                    FullSpanItem fsi = mFullSpanItems.get(i);
                    if (fsi.mPosition >= position) {
                        mFullSpanItems.remove(i);
                    }
                }
            }
            return invalidateAfter(position);
        }

        int invalidateAfter(int position) {
            if (mData == null) {
                return SeslRecyclerView.NO_POSITION;
            }
            if (position >= mData.length) {
                return SeslRecyclerView.NO_POSITION;
            }
            int endPosition = invalidateFullSpansAfter(position);
            if (endPosition == SeslRecyclerView.NO_POSITION) {
                Arrays.fill(mData, position, mData.length, LayoutParams.INVALID_SPAN_ID);
                return mData.length;
            } else {
                Arrays.fill(mData, position, endPosition + 1, LayoutParams.INVALID_SPAN_ID);
                return endPosition + 1;
            }
        }

        int getSpan(int position) {
            if (mData == null || position >= mData.length) {
                return LayoutParams.INVALID_SPAN_ID;
            } else {
                return mData[position];
            }
        }

        void setSpan(int position, Span span) {
            ensureSize(position);
            mData[position] = span.mIndex;
        }

        int sizeForPosition(int position) {
            int len = mData.length;
            while (len <= position) {
                len *= 2;
            }
            return len;
        }

        void ensureSize(int position) {
            if (mData == null) {
                mData = new int[Math.max(position, MIN_SIZE) + 1];
                Arrays.fill(mData, LayoutParams.INVALID_SPAN_ID);
            } else if (position >= mData.length) {
                int[] old = mData;
                mData = new int[sizeForPosition(position)];
                System.arraycopy(old, 0, mData, 0, old.length);
                Arrays.fill(mData, old.length, mData.length, LayoutParams.INVALID_SPAN_ID);
            }
        }

        void clear() {
            if (mData != null) {
                Arrays.fill(mData, LayoutParams.INVALID_SPAN_ID);
            }
            mFullSpanItems = null;
        }

        void offsetForRemoval(int positionStart, int itemCount) {
            if (mData == null || positionStart >= mData.length) {
                return;
            }
            ensureSize(positionStart + itemCount);
            System.arraycopy(mData, positionStart + itemCount, mData, positionStart, mData.length - positionStart - itemCount);
            Arrays.fill(mData, mData.length - itemCount, mData.length, LayoutParams.INVALID_SPAN_ID);
            offsetFullSpansForRemoval(positionStart, itemCount);
        }

        private void offsetFullSpansForRemoval(int positionStart, int itemCount) {
            if (mFullSpanItems == null) {
                return;
            }
            final int end = positionStart + itemCount;
            for (int i = mFullSpanItems.size() - 1; i >= 0; i--) {
                FullSpanItem fsi = mFullSpanItems.get(i);
                if (fsi.mPosition < positionStart) {
                    continue;
                }
                if (fsi.mPosition < end) {
                    mFullSpanItems.remove(i);
                } else {
                    fsi.mPosition -= itemCount;
                }
            }
        }

        void offsetForAddition(int positionStart, int itemCount) {
            if (mData == null || positionStart >= mData.length) {
                return;
            }
            ensureSize(positionStart + itemCount);
            System.arraycopy(mData, positionStart, mData, positionStart + itemCount, mData.length - positionStart - itemCount);
            Arrays.fill(mData, positionStart, positionStart + itemCount, LayoutParams.INVALID_SPAN_ID);
            offsetFullSpansForAddition(positionStart, itemCount);
        }

        private void offsetFullSpansForAddition(int positionStart, int itemCount) {
            if (mFullSpanItems == null) {
                return;
            }
            for (int i = mFullSpanItems.size() - 1; i >= 0; i--) {
                FullSpanItem fsi = mFullSpanItems.get(i);
                if (fsi.mPosition < positionStart) {
                    continue;
                }
                fsi.mPosition += itemCount;
            }
        }

        private int invalidateFullSpansAfter(int position) {
            if (mFullSpanItems == null) {
                return SeslRecyclerView.NO_POSITION;
            }
            final FullSpanItem item = getFullSpanItem(position);
            if (item != null) {
                mFullSpanItems.remove(item);
            }
            int nextFsiIndex = -1;
            final int count = mFullSpanItems.size();
            for (int i = 0; i < count; i++) {
                FullSpanItem fsi = mFullSpanItems.get(i);
                if (fsi.mPosition >= position) {
                    nextFsiIndex = i;
                    break;
                }
            }
            if (nextFsiIndex != -1) {
                FullSpanItem fsi = mFullSpanItems.get(nextFsiIndex);
                mFullSpanItems.remove(nextFsiIndex);
                return fsi.mPosition;
            }
            return SeslRecyclerView.NO_POSITION;
        }

        public void addFullSpanItem(FullSpanItem fullSpanItem) {
            if (mFullSpanItems == null) {
                mFullSpanItems = new ArrayList<>();
            }
            final int size = mFullSpanItems.size();
            for (int i = 0; i < size; i++) {
                FullSpanItem other = mFullSpanItems.get(i);
                if (other.mPosition == fullSpanItem.mPosition) {
                    if (DEBUG) {
                        throw new IllegalStateException("two fsis for same position");
                    } else {
                        mFullSpanItems.remove(i);
                    }
                }
                if (other.mPosition >= fullSpanItem.mPosition) {
                    mFullSpanItems.add(i, fullSpanItem);
                    return;
                }
            }
            mFullSpanItems.add(fullSpanItem);
        }

        public FullSpanItem getFullSpanItem(int position) {
            if (mFullSpanItems == null) {
                return null;
            }
            for (int i = mFullSpanItems.size() - 1; i >= 0; i--) {
                final FullSpanItem fsi = mFullSpanItems.get(i);
                if (fsi.mPosition == position) {
                    return fsi;
                }
            }
            return null;
        }

        public FullSpanItem getFirstFullSpanItemInRange(int minPos, int maxPos, int gapDir, boolean hasUnwantedGapAfter) {
            if (mFullSpanItems == null) {
                return null;
            }
            final int limit = mFullSpanItems.size();
            for (int i = 0; i < limit; i++) {
                FullSpanItem fsi = mFullSpanItems.get(i);
                if (fsi.mPosition >= maxPos) {
                    return null;
                }
                if (fsi.mPosition >= minPos && (gapDir == 0 || fsi.mGapDir == gapDir || (hasUnwantedGapAfter && fsi.mHasUnwantedGapAfter))) {
                    return fsi;
                }
            }
            return null;
        }

        @SuppressLint("BanParcelableUsage")
        static class FullSpanItem implements Parcelable {
            public static final Parcelable.Creator<FullSpanItem> CREATOR =
                    new Parcelable.Creator<FullSpanItem>() {
                        @Override
                        public FullSpanItem createFromParcel(Parcel in) {
                            return new FullSpanItem(in);
                        }

                        @Override
                        public FullSpanItem[] newArray(int size) {
                            return new FullSpanItem[size];
                        }
                    };
            int mPosition;
            int mGapDir;
            int[] mGapPerSpan;
            boolean mHasUnwantedGapAfter;

            FullSpanItem(Parcel in) {
                mPosition = in.readInt();
                mGapDir = in.readInt();
                mHasUnwantedGapAfter = in.readInt() == 1;
                int spanCount = in.readInt();
                if (spanCount > 0) {
                    mGapPerSpan = new int[spanCount];
                    in.readIntArray(mGapPerSpan);
                }
            }

            FullSpanItem() {
            }

            int getGapForSpan(int spanIndex) {
                return mGapPerSpan == null ? 0 : mGapPerSpan[spanIndex];
            }

            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(Parcel dest, int flags) {
                dest.writeInt(mPosition);
                dest.writeInt(mGapDir);
                dest.writeInt(mHasUnwantedGapAfter ? 1 : 0);
                if (mGapPerSpan != null && mGapPerSpan.length > 0) {
                    dest.writeInt(mGapPerSpan.length);
                    dest.writeIntArray(mGapPerSpan);
                } else {
                    dest.writeInt(0);
                }
            }

            @Override
            public String toString() {
                return "FullSpanItem{" + "mPosition=" + mPosition + ", mGapDir=" + mGapDir + ", mHasUnwantedGapAfter=" + mHasUnwantedGapAfter + ", mGapPerSpan=" + Arrays.toString(mGapPerSpan) + '}';
            }
        }
    }

    /**
     * @hide
     */
    @SuppressLint("BanParcelableUsage")
    public static class SavedState implements Parcelable {
        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
                    @Override
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }

                    @Override
                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
        int mAnchorPosition;
        int mVisibleAnchorPosition;
        int mSpanOffsetsSize;
        int[] mSpanOffsets;
        int mSpanLookupSize;
        int[] mSpanLookup;
        List<LazySpanLookup.FullSpanItem> mFullSpanItems;
        boolean mReverseLayout;
        boolean mAnchorLayoutFromEnd;
        boolean mLastLayoutRTL;

        public SavedState() {
        }

        SavedState(Parcel in) {
            mAnchorPosition = in.readInt();
            mVisibleAnchorPosition = in.readInt();
            mSpanOffsetsSize = in.readInt();
            if (mSpanOffsetsSize > 0) {
                mSpanOffsets = new int[mSpanOffsetsSize];
                in.readIntArray(mSpanOffsets);
            }

            mSpanLookupSize = in.readInt();
            if (mSpanLookupSize > 0) {
                mSpanLookup = new int[mSpanLookupSize];
                in.readIntArray(mSpanLookup);
            }
            mReverseLayout = in.readInt() == 1;
            mAnchorLayoutFromEnd = in.readInt() == 1;
            mLastLayoutRTL = in.readInt() == 1;
            @SuppressWarnings("unchecked")
            List<LazySpanLookup.FullSpanItem> fullSpanItems = in.readArrayList(LazySpanLookup.FullSpanItem.class.getClassLoader());
            mFullSpanItems = fullSpanItems;
        }

        public SavedState(SavedState other) {
            mSpanOffsetsSize = other.mSpanOffsetsSize;
            mAnchorPosition = other.mAnchorPosition;
            mVisibleAnchorPosition = other.mVisibleAnchorPosition;
            mSpanOffsets = other.mSpanOffsets;
            mSpanLookupSize = other.mSpanLookupSize;
            mSpanLookup = other.mSpanLookup;
            mReverseLayout = other.mReverseLayout;
            mAnchorLayoutFromEnd = other.mAnchorLayoutFromEnd;
            mLastLayoutRTL = other.mLastLayoutRTL;
            mFullSpanItems = other.mFullSpanItems;
        }

        void invalidateSpanInfo() {
            mSpanOffsets = null;
            mSpanOffsetsSize = 0;
            mSpanLookupSize = 0;
            mSpanLookup = null;
            mFullSpanItems = null;
        }

        void invalidateAnchorPositionInfo() {
            mSpanOffsets = null;
            mSpanOffsetsSize = 0;
            mAnchorPosition = SeslRecyclerView.NO_POSITION;
            mVisibleAnchorPosition = SeslRecyclerView.NO_POSITION;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(mAnchorPosition);
            dest.writeInt(mVisibleAnchorPosition);
            dest.writeInt(mSpanOffsetsSize);
            if (mSpanOffsetsSize > 0) {
                dest.writeIntArray(mSpanOffsets);
            }
            dest.writeInt(mSpanLookupSize);
            if (mSpanLookupSize > 0) {
                dest.writeIntArray(mSpanLookup);
            }
            dest.writeInt(mReverseLayout ? 1 : 0);
            dest.writeInt(mAnchorLayoutFromEnd ? 1 : 0);
            dest.writeInt(mLastLayoutRTL ? 1 : 0);
            dest.writeList(mFullSpanItems);
        }
    }

    class Span {
        static final int INVALID_LINE = Integer.MIN_VALUE;
        final int mIndex;
        ArrayList<View> mViews = new ArrayList<>();
        int mCachedStart = INVALID_LINE;
        int mCachedEnd = INVALID_LINE;
        int mDeletedSize = 0;

        Span(int index) {
            mIndex = index;
        }

        int getStartLine(int def) {
            if (mCachedStart != INVALID_LINE) {
                return mCachedStart;
            }
            if (mViews.size() == 0) {
                return def;
            }
            calculateCachedStart();
            return mCachedStart;
        }

        void calculateCachedStart() {
            final View startView = mViews.get(0);
            final LayoutParams lp = getLayoutParams(startView);
            mCachedStart = mPrimaryOrientation.getDecoratedStart(startView);
            if (lp.mFullSpan) {
                LazySpanLookup.FullSpanItem fsi = mLazySpanLookup.getFullSpanItem(lp.getViewLayoutPosition());
                if (fsi != null && fsi.mGapDir == LayoutState.LAYOUT_START) {
                    mCachedStart -= fsi.getGapForSpan(mIndex);
                }
            }
        }

        int getStartLine() {
            if (mCachedStart != INVALID_LINE) {
                return mCachedStart;
            }
            calculateCachedStart();
            return mCachedStart;
        }

        int getEndLine(int def) {
            if (mCachedEnd != INVALID_LINE) {
                return mCachedEnd;
            }
            final int size = mViews.size();
            if (size == 0) {
                return def;
            }
            calculateCachedEnd();
            return mCachedEnd;
        }

        void calculateCachedEnd() {
            final View endView = mViews.get(mViews.size() - 1);
            final LayoutParams lp = getLayoutParams(endView);
            mCachedEnd = mPrimaryOrientation.getDecoratedEnd(endView);
            if (lp.mFullSpan) {
                LazySpanLookup.FullSpanItem fsi = mLazySpanLookup.getFullSpanItem(lp.getViewLayoutPosition());
                if (fsi != null && fsi.mGapDir == LayoutState.LAYOUT_END) {
                    mCachedEnd += fsi.getGapForSpan(mIndex);
                }
            }
        }

        int getEndLine() {
            if (mCachedEnd != INVALID_LINE) {
                return mCachedEnd;
            }
            calculateCachedEnd();
            return mCachedEnd;
        }

        void prependToSpan(View view) {
            LayoutParams lp = getLayoutParams(view);
            lp.mSpan = this;
            mViews.add(0, view);
            mCachedStart = INVALID_LINE;
            if (mViews.size() == 1) {
                mCachedEnd = INVALID_LINE;
            }
            if (lp.isItemRemoved() || lp.isItemChanged()) {
                mDeletedSize += mPrimaryOrientation.getDecoratedMeasurement(view);
            }
        }

        void appendToSpan(View view) {
            LayoutParams lp = getLayoutParams(view);
            lp.mSpan = this;
            mViews.add(view);
            mCachedEnd = INVALID_LINE;
            if (mViews.size() == 1) {
                mCachedStart = INVALID_LINE;
            }
            if (lp.isItemRemoved() || lp.isItemChanged()) {
                mDeletedSize += mPrimaryOrientation.getDecoratedMeasurement(view);
            }
        }

        void cacheReferenceLineAndClear(boolean reverseLayout, int offset) {
            int reference;
            if (reverseLayout) {
                reference = getEndLine(INVALID_LINE);
            } else {
                reference = getStartLine(INVALID_LINE);
            }
            clear();
            if (reference == INVALID_LINE) {
                return;
            }
            if ((reverseLayout && reference < mPrimaryOrientation.getEndAfterPadding()) || (!reverseLayout && reference > mPrimaryOrientation.getStartAfterPadding())) {
                return;
            }
            if (offset != INVALID_OFFSET) {
                reference += offset;
            }
            mCachedStart = mCachedEnd = reference;
        }

        void clear() {
            mViews.clear();
            invalidateCache();
            mDeletedSize = 0;
        }

        void invalidateCache() {
            mCachedStart = INVALID_LINE;
            mCachedEnd = INVALID_LINE;
        }

        void setLine(int line) {
            mCachedEnd = mCachedStart = line;
        }

        void popEnd() {
            final int size = mViews.size();
            View end = mViews.remove(size - 1);
            final LayoutParams lp = getLayoutParams(end);
            lp.mSpan = null;
            if (lp.isItemRemoved() || lp.isItemChanged()) {
                mDeletedSize -= mPrimaryOrientation.getDecoratedMeasurement(end);
            }
            if (size == 1) {
                mCachedStart = INVALID_LINE;
            }
            mCachedEnd = INVALID_LINE;
        }

        void popStart() {
            View start = mViews.remove(0);
            final LayoutParams lp = getLayoutParams(start);
            lp.mSpan = null;
            if (mViews.size() == 0) {
                mCachedEnd = INVALID_LINE;
            }
            if (lp.isItemRemoved() || lp.isItemChanged()) {
                mDeletedSize -= mPrimaryOrientation.getDecoratedMeasurement(start);
            }
            mCachedStart = INVALID_LINE;
        }

        public int getDeletedSize() {
            return mDeletedSize;
        }

        LayoutParams getLayoutParams(View view) {
            return (LayoutParams) view.getLayoutParams();
        }

        void onOffset(int dt) {
            if (mCachedStart != INVALID_LINE) {
                mCachedStart += dt;
            }
            if (mCachedEnd != INVALID_LINE) {
                mCachedEnd += dt;
            }
        }

        public int findFirstVisibleItemPosition() {
            return mReverseLayout ? findOneVisibleChild(mViews.size() - 1, -1, false) : findOneVisibleChild(0, mViews.size(), false);
        }

        public int findFirstPartiallyVisibleItemPosition() {
            return mReverseLayout ? findOnePartiallyVisibleChild(mViews.size() - 1, -1, true) : findOnePartiallyVisibleChild(0, mViews.size(), true);
        }

        public int findFirstCompletelyVisibleItemPosition() {
            return mReverseLayout ? findOneVisibleChild(mViews.size() - 1, -1, true) : findOneVisibleChild(0, mViews.size(), true);
        }

        public int findLastVisibleItemPosition() {
            return mReverseLayout ? findOneVisibleChild(0, mViews.size(), false) : findOneVisibleChild(mViews.size() - 1, -1, false);
        }

        public int findLastPartiallyVisibleItemPosition() {
            return mReverseLayout ? findOnePartiallyVisibleChild(0, mViews.size(), true) : findOnePartiallyVisibleChild(mViews.size() - 1, -1, true);
        }

        public int findLastCompletelyVisibleItemPosition() {
            return mReverseLayout ? findOneVisibleChild(0, mViews.size(), true) : findOneVisibleChild(mViews.size() - 1, -1, true);
        }

        int findOnePartiallyOrCompletelyVisibleChild(int fromIndex, int toIndex, boolean completelyVisible, boolean acceptCompletelyVisible, boolean acceptEndPointInclusion) {
            final int start = mPrimaryOrientation.getStartAfterPadding();
            final int end = mPrimaryOrientation.getEndAfterPadding();
            final int next = toIndex > fromIndex ? 1 : -1;
            for (int i = fromIndex; i != toIndex; i += next) {
                final View child = mViews.get(i);
                final int childStart = mPrimaryOrientation.getDecoratedStart(child);
                final int childEnd = mPrimaryOrientation.getDecoratedEnd(child);
                boolean childStartInclusion = acceptEndPointInclusion ? (childStart <= end) : (childStart < end);
                boolean childEndInclusion = acceptEndPointInclusion ? (childEnd >= start) : (childEnd > start);
                if (childStartInclusion && childEndInclusion) {
                    if (completelyVisible && acceptCompletelyVisible) {
                        if (childStart >= start && childEnd <= end) {
                            return getPosition(child);
                        }
                    } else if (acceptCompletelyVisible) {
                        return getPosition(child);
                    } else if (childStart < start || childEnd > end) {
                        return getPosition(child);
                    }
                }
            }
            return SeslRecyclerView.NO_POSITION;
        }

        int findOneVisibleChild(int fromIndex, int toIndex, boolean completelyVisible) {
            return findOnePartiallyOrCompletelyVisibleChild(fromIndex, toIndex, completelyVisible, true, false);
        }

        int findOnePartiallyVisibleChild(int fromIndex, int toIndex, boolean acceptEndPointInclusion) {
            return findOnePartiallyOrCompletelyVisibleChild(fromIndex, toIndex, false, false, acceptEndPointInclusion);
        }

        public View getFocusableViewAfter(int referenceChildPosition, int layoutDir) {
            View candidate = null;
            if (layoutDir == LayoutState.LAYOUT_START) {
                final int limit = mViews.size();
                for (int i = 0; i < limit; i++) {
                    final View view = mViews.get(i);
                    if ((mReverseLayout && getPosition(view) <= referenceChildPosition) || (!mReverseLayout && getPosition(view) >= referenceChildPosition)) {
                        break;
                    }
                    if (view.hasFocusable()) {
                        candidate = view;
                    } else {
                        break;
                    }
                }
            } else {
                for (int i = mViews.size() - 1; i >= 0; i--) {
                    final View view = mViews.get(i);
                    if ((mReverseLayout && getPosition(view) >= referenceChildPosition) || (!mReverseLayout && getPosition(view) <= referenceChildPosition)) {
                        break;
                    }
                    if (view.hasFocusable()) {
                        candidate = view;
                    } else {
                        break;
                    }
                }
            }
            return candidate;
        }
    }

    class AnchorInfo {
        int mPosition;
        int mOffset;
        boolean mLayoutFromEnd;
        boolean mInvalidateOffsets;
        boolean mValid;
        int[] mSpanReferenceLines;

        AnchorInfo() {
            reset();
        }

        void reset() {
            mPosition = SeslRecyclerView.NO_POSITION;
            mOffset = INVALID_OFFSET;
            mLayoutFromEnd = false;
            mInvalidateOffsets = false;
            mValid = false;
            if (mSpanReferenceLines != null) {
                Arrays.fill(mSpanReferenceLines, -1);
            }
        }

        void saveSpanReferenceLines(Span[] spans) {
            int spanCount = spans.length;
            if (mSpanReferenceLines == null || mSpanReferenceLines.length < spanCount) {
                mSpanReferenceLines = new int[mSpans.length];
            }
            for (int i = 0; i < spanCount; i++) {
                mSpanReferenceLines[i] = spans[i].getStartLine(Span.INVALID_LINE);
            }
        }

        void assignCoordinateFromPadding() {
            mOffset = mLayoutFromEnd ? mPrimaryOrientation.getEndAfterPadding() : mPrimaryOrientation.getStartAfterPadding();
        }

        void assignCoordinateFromPadding(int addedDistance) {
            if (mLayoutFromEnd) {
                mOffset = mPrimaryOrientation.getEndAfterPadding() - addedDistance;
            } else {
                mOffset = mPrimaryOrientation.getStartAfterPadding() + addedDistance;
            }
        }
    }
}
