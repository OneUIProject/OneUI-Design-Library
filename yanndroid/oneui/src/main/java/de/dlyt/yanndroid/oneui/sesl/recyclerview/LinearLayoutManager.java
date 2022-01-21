package de.dlyt.yanndroid.oneui.sesl.recyclerview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PointF;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;

import androidx.annotation.NonNull;
import androidx.core.os.TraceCompat;
import androidx.core.view.ViewCompat;

import java.util.List;

import de.dlyt.yanndroid.oneui.view.RecyclerView;

public class LinearLayoutManager extends RecyclerView.LayoutManager implements ItemTouchHelper.ViewDropHandler, RecyclerView.SmoothScroller.ScrollVectorProvider {
    private static final String TAG = "SeslLinearLayoutManager";
    static final boolean DEBUG = false;
    public static final int HORIZONTAL = RecyclerView.HORIZONTAL;
    public static final int VERTICAL = RecyclerView.VERTICAL;
    public static final int INVALID_OFFSET = Integer.MIN_VALUE;
    private static final float MAX_SCROLL_FACTOR = 0.33333334f;
    int mOrientation = RecyclerView.DEFAULT_ORIENTATION;
    private LayoutState mLayoutState;
    OrientationHelper mOrientationHelper;
    private boolean mLastStackFromEnd;
    public boolean mReverseLayout = false;
    boolean mShouldReverseLayout = false;
    public boolean mStackFromEnd = false;
    private boolean mSmoothScrollbarEnabled = true;
    int mPendingScrollPosition = RecyclerView.NO_POSITION;
    int mPendingScrollPositionOffset = INVALID_OFFSET;
    private boolean mRecycleChildrenOnDetach;
    SavedState mPendingSavedState = null;
    final AnchorInfo mAnchorInfo = new AnchorInfo();
    private final LayoutChunkResult mLayoutChunkResult = new LayoutChunkResult();
    private int mInitialPrefetchItemCount = 2;
    private int[] mReusableIntPair = new int[2];

    public LinearLayoutManager(Context context) {
        this(context, RecyclerView.DEFAULT_ORIENTATION, false);
    }

    public LinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
        setOrientation(orientation);
        setReverseLayout(reverseLayout);
    }

    public LinearLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        Properties properties = getProperties(context, attrs, defStyleAttr, defStyleRes);
        setOrientation(properties.orientation);
        setReverseLayout(properties.reverseLayout);
        setStackFromEnd(properties.stackFromEnd);
    }

    @Override
    public boolean isAutoMeasureEnabled() {
        return true;
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    public boolean getRecycleChildrenOnDetach() {
        return mRecycleChildrenOnDetach;
    }

    public void setRecycleChildrenOnDetach(boolean recycleChildrenOnDetach) {
        mRecycleChildrenOnDetach = recycleChildrenOnDetach;
    }

    @Override
    public void onDetachedFromWindow(RecyclerView view, RecyclerView.Recycler recycler) {
        super.onDetachedFromWindow(view, recycler);
        if (mRecycleChildrenOnDetach) {
            removeAndRecycleAllViews(recycler);
            recycler.clear();
        }
    }

    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        if (getChildCount() > 0) {
            event.setFromIndex(findFirstVisibleItemPosition());
            event.setToIndex(findLastVisibleItemPosition());
        }
    }

    @Override
    public Parcelable onSaveInstanceState() {
        if (mPendingSavedState != null) {
            return new SavedState(mPendingSavedState);
        }
        SavedState state = new SavedState();
        if (getChildCount() > 0) {
            ensureLayoutState();
            boolean didLayoutFromEnd = mLastStackFromEnd ^ mShouldReverseLayout;
            state.mAnchorLayoutFromEnd = didLayoutFromEnd;
            if (didLayoutFromEnd) {
                final View refChild = getChildClosestToEnd();
                state.mAnchorOffset = mOrientationHelper.getEndAfterPadding() - mOrientationHelper.getDecoratedEnd(refChild);
                state.mAnchorPosition = getPosition(refChild);
            } else {
                final View refChild = getChildClosestToStart();
                state.mAnchorPosition = getPosition(refChild);
                state.mAnchorOffset = mOrientationHelper.getDecoratedStart(refChild) - mOrientationHelper.getStartAfterPadding();
            }
        } else {
            state.invalidateAnchor();
        }
        return state;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof SavedState) {
            mPendingSavedState = (SavedState) state;
            if (mPendingScrollPosition != RecyclerView.NO_POSITION) {
                mPendingSavedState.invalidateAnchor();
            }
            requestLayout();
            if (DEBUG) {
                Log.d(TAG, "loaded saved state");
            }
        } else if (DEBUG) {
            Log.d(TAG, "invalid saved state class");
        }
    }

    @Override
    public boolean canScrollHorizontally() {
        return mOrientation == HORIZONTAL;
    }

    @Override
    public boolean canScrollVertically() {
        return mOrientation == VERTICAL;
    }

    public void setStackFromEnd(boolean stackFromEnd) {
        assertNotInLayoutOrScroll(null);
        if (mStackFromEnd == stackFromEnd) {
            return;
        }
        mStackFromEnd = stackFromEnd;
        requestLayout();
    }

    public boolean getStackFromEnd() {
        return mStackFromEnd;
    }

    @RecyclerView.Orientation
    public int getOrientation() {
        return mOrientation;
    }

    public void setOrientation(@RecyclerView.Orientation int orientation) {
        if (orientation != HORIZONTAL && orientation != VERTICAL) {
            throw new IllegalArgumentException("invalid orientation:" + orientation);
        }

        assertNotInLayoutOrScroll(null);

        if (orientation != mOrientation || mOrientationHelper == null) {
            mOrientationHelper = OrientationHelper.createOrientationHelper(this, orientation);
            mAnchorInfo.mOrientationHelper = mOrientationHelper;
            mOrientation = orientation;
            requestLayout();
        }
    }

    private void resolveShouldLayoutReverse() {
        if (mOrientation == VERTICAL || !isLayoutRTL()) {
            mShouldReverseLayout = mReverseLayout;
        } else {
            mShouldReverseLayout = !mReverseLayout;
        }
    }

    public boolean getReverseLayout() {
        return mReverseLayout;
    }

    public void setReverseLayout(boolean reverseLayout) {
        assertNotInLayoutOrScroll(null);
        if (reverseLayout == mReverseLayout) {
            return;
        }
        mReverseLayout = reverseLayout;
        requestLayout();
    }

    @Override
    public View findViewByPosition(int position) {
        final int childCount = getChildCount();
        if (childCount == 0) {
            return null;
        }
        final int firstChild = getPosition(getChildAt(0));
        final int viewPosition = position - firstChild;
        if (viewPosition >= 0 && viewPosition < childCount) {
            final View child = getChildAt(viewPosition);
            if (getPosition(child) == position) {
                return child;
            }
        }
        return super.findViewByPosition(position);
    }

    @SuppressWarnings("DeprecatedIsStillUsed")
    @Deprecated
    protected int getExtraLayoutSpace(RecyclerView.State state) {
        if (state.hasTargetScrollPosition()) {
            return mOrientationHelper.getTotalSpace();
        } else {
            return 0;
        }
    }

    protected void calculateExtraLayoutSpace(@NonNull RecyclerView.State state, @NonNull int[] extraLayoutSpace) {
        int extraLayoutSpaceStart = 0;
        int extraLayoutSpaceEnd = 0;

        @SuppressWarnings("deprecation")
        int extraScrollSpace = getExtraLayoutSpace(state);
        if (mLayoutState.mLayoutDirection == LayoutState.LAYOUT_START) {
            extraLayoutSpaceStart = extraScrollSpace;
        } else {
            extraLayoutSpaceEnd = extraScrollSpace;
        }

        extraLayoutSpace[0] = extraLayoutSpaceStart;
        extraLayoutSpace[1] = extraLayoutSpaceEnd;
    }

    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
        LinearSmoothScroller linearSmoothScroller = new LinearSmoothScroller(recyclerView.getContext());
        recyclerView.showGoToTop();
        linearSmoothScroller.setTargetPosition(position);
        startSmoothScroll(linearSmoothScroller);
        Log.d(TAG, "SS pos to : " + position);
    }

    @Override
    public PointF computeScrollVectorForPosition(int targetPosition) {
        if (getChildCount() == 0) {
            return null;
        }
        final int firstChildPos = getPosition(getChildAt(0));
        final int direction = targetPosition < firstChildPos != mShouldReverseLayout ? -1 : 1;
        if (mOrientation == HORIZONTAL) {
            return new PointF(direction, 0);
        } else {
            return new PointF(0, direction);
        }
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (DEBUG) {
            Log.d(TAG, "is pre layout:" + state.isPreLayout());
        }
        if (mPendingSavedState != null || mPendingScrollPosition != RecyclerView.NO_POSITION) {
            if (state.getItemCount() == 0) {
                removeAndRecycleAllViews(recycler);
                return;
            }
        }
        if (mPendingSavedState != null && mPendingSavedState.hasValidAnchor()) {
            mPendingScrollPosition = mPendingSavedState.mAnchorPosition;
        }

        ensureLayoutState();
        mLayoutState.mRecycle = false;
        resolveShouldLayoutReverse();

        final View focused = getFocusedChild();
        if (!mAnchorInfo.mValid || mPendingScrollPosition != RecyclerView.NO_POSITION || mPendingSavedState != null) {
            mAnchorInfo.reset();
            mAnchorInfo.mLayoutFromEnd = mShouldReverseLayout ^ mStackFromEnd;
            updateAnchorInfoForLayout(recycler, state, mAnchorInfo);
            mAnchorInfo.mValid = true;
        } else if (focused != null && (mOrientationHelper.getDecoratedStart(focused) >= mOrientationHelper.getEndAfterPadding() || mOrientationHelper.getDecoratedEnd(focused) <= mOrientationHelper.getStartAfterPadding())) {
            mAnchorInfo.assignFromViewAndKeepVisibleRect(focused, getPosition(focused));
        }
        if (DEBUG) {
            Log.d(TAG, "Anchor info:" + mAnchorInfo);
        }

        mLayoutState.mLayoutDirection = mLayoutState.mLastScrollDelta >= 0 ? LayoutState.LAYOUT_END : LayoutState.LAYOUT_START;
        mReusableIntPair[0] = 0;
        mReusableIntPair[1] = 0;
        calculateExtraLayoutSpace(state, mReusableIntPair);
        int extraForStart = Math.max(0, mReusableIntPair[0]) + mOrientationHelper.getStartAfterPadding();
        int extraForEnd = Math.max(0, mReusableIntPair[1]) + mOrientationHelper.getEndPadding();
        if (state.isPreLayout() && mPendingScrollPosition != RecyclerView.NO_POSITION && mPendingScrollPositionOffset != INVALID_OFFSET) {
            final View existing = findViewByPosition(mPendingScrollPosition);
            if (existing != null) {
                final int current;
                final int upcomingOffset;
                if (mShouldReverseLayout) {
                    current = mOrientationHelper.getEndAfterPadding() - mOrientationHelper.getDecoratedEnd(existing);
                    upcomingOffset = current - mPendingScrollPositionOffset;
                } else {
                    current = mOrientationHelper.getDecoratedStart(existing) - mOrientationHelper.getStartAfterPadding();
                    upcomingOffset = mPendingScrollPositionOffset - current;
                }
                if (upcomingOffset > 0) {
                    extraForStart += upcomingOffset;
                } else {
                    extraForEnd -= upcomingOffset;
                }
            }
        }
        int startOffset;
        int endOffset;
        final int firstLayoutDirection;
        if (mAnchorInfo.mLayoutFromEnd) {
            firstLayoutDirection = mShouldReverseLayout ? LayoutState.ITEM_DIRECTION_TAIL : LayoutState.ITEM_DIRECTION_HEAD;
        } else {
            firstLayoutDirection = mShouldReverseLayout ? LayoutState.ITEM_DIRECTION_HEAD : LayoutState.ITEM_DIRECTION_TAIL;
        }

        onAnchorReady(recycler, state, mAnchorInfo, firstLayoutDirection);
        detachAndScrapAttachedViews(recycler);
        mLayoutState.mInfinite = resolveIsInfinite();
        mLayoutState.mIsPreLayout = state.isPreLayout();
        mLayoutState.mNoRecycleSpace = 0;
        if (mAnchorInfo.mLayoutFromEnd) {
            updateLayoutStateToFillStart(mAnchorInfo);
            mLayoutState.mExtraFillSpace = extraForStart;
            fill(recycler, mLayoutState, state, false);
            startOffset = mLayoutState.mOffset;
            final int firstElement = mLayoutState.mCurrentPosition;
            if (mLayoutState.mAvailable > 0) {
                extraForEnd += mLayoutState.mAvailable;
            }
            updateLayoutStateToFillEnd(mAnchorInfo);
            mLayoutState.mExtraFillSpace = extraForEnd;
            mLayoutState.mCurrentPosition += mLayoutState.mItemDirection;
            fill(recycler, mLayoutState, state, false);
            endOffset = mLayoutState.mOffset;

            if (mLayoutState.mAvailable > 0) {
                extraForStart = mLayoutState.mAvailable;
                updateLayoutStateToFillStart(firstElement, startOffset);
                mLayoutState.mExtraFillSpace = extraForStart;
                fill(recycler, mLayoutState, state, false);
                startOffset = mLayoutState.mOffset;
            }
        } else {
            updateLayoutStateToFillEnd(mAnchorInfo);
            mLayoutState.mExtraFillSpace = extraForEnd;
            fill(recycler, mLayoutState, state, false);
            endOffset = mLayoutState.mOffset;
            final int lastElement = mLayoutState.mCurrentPosition;
            if (mLayoutState.mAvailable > 0) {
                extraForStart += mLayoutState.mAvailable;
            }
            updateLayoutStateToFillStart(mAnchorInfo);
            mLayoutState.mExtraFillSpace = extraForStart;
            mLayoutState.mCurrentPosition += mLayoutState.mItemDirection;
            fill(recycler, mLayoutState, state, false);
            startOffset = mLayoutState.mOffset;

            if (mLayoutState.mAvailable > 0) {
                extraForEnd = mLayoutState.mAvailable;
                updateLayoutStateToFillEnd(lastElement, endOffset);
                mLayoutState.mExtraFillSpace = extraForEnd;
                fill(recycler, mLayoutState, state, false);
                endOffset = mLayoutState.mOffset;
            }
        }

        if (getChildCount() > 0) {
            if (mShouldReverseLayout ^ mStackFromEnd) {
                int fixOffset = fixLayoutEndGap(endOffset, recycler, state, true);
                startOffset += fixOffset;
                endOffset += fixOffset;
                fixOffset = fixLayoutStartGap(startOffset, recycler, state, false);
                startOffset += fixOffset;
                endOffset += fixOffset;
            } else {
                int fixOffset = fixLayoutStartGap(startOffset, recycler, state, true);
                startOffset += fixOffset;
                endOffset += fixOffset;
                fixOffset = fixLayoutEndGap(endOffset, recycler, state, false);
                startOffset += fixOffset;
                endOffset += fixOffset;
            }
        }
        layoutForPredictiveAnimations(recycler, state, startOffset, endOffset);
        if (!state.isPreLayout()) {
            mOrientationHelper.onLayoutComplete();
        } else {
            mAnchorInfo.reset();
        }
        mLastStackFromEnd = mStackFromEnd;
        if (DEBUG) {
            validateChildOrder();
        }
    }

    @Override
    public void onLayoutCompleted(RecyclerView.State state) {
        super.onLayoutCompleted(state);
        mPendingSavedState = null;
        mPendingScrollPosition = RecyclerView.NO_POSITION;
        mPendingScrollPositionOffset = INVALID_OFFSET;
        mAnchorInfo.reset();
    }

    void onAnchorReady(RecyclerView.Recycler recycler, RecyclerView.State state, AnchorInfo anchorInfo, int firstLayoutItemDirection) {
    }

    private void layoutForPredictiveAnimations(RecyclerView.Recycler recycler, RecyclerView.State state, int startOffset, int endOffset) {
        if (!state.willRunPredictiveAnimations() || getChildCount() == 0 || state.isPreLayout() || !supportsPredictiveItemAnimations()) {
            return;
        }
        int scrapExtraStart = 0, scrapExtraEnd = 0;
        final List<RecyclerView.ViewHolder> scrapList = recycler.getScrapList();
        final int scrapSize = scrapList.size();
        final int firstChildPos = getPosition(getChildAt(0));
        for (int i = 0; i < scrapSize; i++) {
            RecyclerView.ViewHolder scrap = scrapList.get(i);
            if (scrap.isRemoved()) {
                continue;
            }
            final int position = scrap.getLayoutPosition();
            final int direction = position < firstChildPos != mShouldReverseLayout ? LayoutState.LAYOUT_START : LayoutState.LAYOUT_END;
            if (direction == LayoutState.LAYOUT_START) {
                scrapExtraStart += mOrientationHelper.getDecoratedMeasurement(scrap.itemView);
            } else {
                scrapExtraEnd += mOrientationHelper.getDecoratedMeasurement(scrap.itemView);
            }
        }

        if (DEBUG) {
            Log.d(TAG, "for unused scrap, decided to add " + scrapExtraStart + " towards start and " + scrapExtraEnd + " towards end");
        }
        mLayoutState.mScrapList = scrapList;
        if (scrapExtraStart > 0) {
            View anchor = getChildClosestToStart();
            updateLayoutStateToFillStart(getPosition(anchor), startOffset);
            mLayoutState.mExtraFillSpace = scrapExtraStart;
            mLayoutState.mAvailable = 0;
            mLayoutState.assignPositionFromScrapList();
            fill(recycler, mLayoutState, state, false);
        }

        if (scrapExtraEnd > 0) {
            View anchor = getChildClosestToEnd();
            updateLayoutStateToFillEnd(getPosition(anchor), endOffset);
            mLayoutState.mExtraFillSpace = scrapExtraEnd;
            mLayoutState.mAvailable = 0;
            mLayoutState.assignPositionFromScrapList();
            fill(recycler, mLayoutState, state, false);
        }
        mLayoutState.mScrapList = null;
    }

    private void updateAnchorInfoForLayout(RecyclerView.Recycler recycler, RecyclerView.State state, AnchorInfo anchorInfo) {
        if (updateAnchorFromPendingData(state, anchorInfo)) {
            if (DEBUG) {
                Log.d(TAG, "updated anchor info from pending information");
            }
            return;
        }

        if (updateAnchorFromChildren(recycler, state, anchorInfo)) {
            if (DEBUG) {
                Log.d(TAG, "updated anchor info from existing children");
            }
            return;
        }
        if (DEBUG) {
            Log.d(TAG, "deciding anchor info for fresh state");
        }
        anchorInfo.assignCoordinateFromPadding();
        anchorInfo.mPosition = mStackFromEnd ? state.getItemCount() - 1 : 0;
    }

    private boolean updateAnchorFromChildren(RecyclerView.Recycler recycler, RecyclerView.State state, AnchorInfo anchorInfo) {
        if (getChildCount() == 0) {
            return false;
        }
        final View focused = getFocusedChild();
        if (focused != null && anchorInfo.isViewValidAsAnchor(focused, state)) {
            anchorInfo.assignFromViewAndKeepVisibleRect(focused, getPosition(focused));
            return true;
        }
        if (mLastStackFromEnd != mStackFromEnd) {
            return false;
        }
        View referenceChild = findReferenceChild(recycler, state, anchorInfo.mLayoutFromEnd, mStackFromEnd);
        if (referenceChild != null) {
            anchorInfo.assignFromView(referenceChild, getPosition(referenceChild));
            if (!state.isPreLayout() && supportsPredictiveItemAnimations()) {
                final int childStart = mOrientationHelper.getDecoratedStart(referenceChild);
                final int childEnd = mOrientationHelper.getDecoratedEnd(referenceChild);
                final int boundsStart = mOrientationHelper.getStartAfterPadding();
                final int boundsEnd = mOrientationHelper.getEndAfterPadding();
                boolean outOfBoundsBefore = childEnd <= boundsStart && childStart < boundsStart;
                boolean outOfBoundsAfter = childStart >= boundsEnd && childEnd > boundsEnd;
                if (outOfBoundsBefore || outOfBoundsAfter) {
                    anchorInfo.mCoordinate = anchorInfo.mLayoutFromEnd ? boundsEnd : boundsStart;
                }
            }
            return true;
        }
        return false;
    }

    private boolean updateAnchorFromPendingData(RecyclerView.State state, AnchorInfo anchorInfo) {
        if (state.isPreLayout() || mPendingScrollPosition == RecyclerView.NO_POSITION) {
            return false;
        }
        if (mPendingScrollPosition < 0 || mPendingScrollPosition >= state.getItemCount()) {
            mPendingScrollPosition = RecyclerView.NO_POSITION;
            mPendingScrollPositionOffset = INVALID_OFFSET;
            if (DEBUG) {
                Log.e(TAG, "ignoring invalid scroll position " + mPendingScrollPosition);
            }
            return false;
        }

        anchorInfo.mPosition = mPendingScrollPosition;
        if (mPendingSavedState != null && mPendingSavedState.hasValidAnchor()) {
            anchorInfo.mLayoutFromEnd = mPendingSavedState.mAnchorLayoutFromEnd;
            if (anchorInfo.mLayoutFromEnd) {
                anchorInfo.mCoordinate = mOrientationHelper.getEndAfterPadding() - mPendingSavedState.mAnchorOffset;
            } else {
                anchorInfo.mCoordinate = mOrientationHelper.getStartAfterPadding() + mPendingSavedState.mAnchorOffset;
            }
            return true;
        }

        if (mPendingScrollPositionOffset == INVALID_OFFSET) {
            View child = findViewByPosition(mPendingScrollPosition);
            if (child != null) {
                final int childSize = mOrientationHelper.getDecoratedMeasurement(child);
                if (childSize > mOrientationHelper.getTotalSpace()) {
                    anchorInfo.assignCoordinateFromPadding();
                    return true;
                }
                final int startGap = mOrientationHelper.getDecoratedStart(child) - mOrientationHelper.getStartAfterPadding();
                if (startGap < 0) {
                    anchorInfo.mCoordinate = mOrientationHelper.getStartAfterPadding();
                    anchorInfo.mLayoutFromEnd = false;
                    return true;
                }
                final int endGap = mOrientationHelper.getEndAfterPadding() - mOrientationHelper.getDecoratedEnd(child);
                if (endGap < 0) {
                    anchorInfo.mCoordinate = mOrientationHelper.getEndAfterPadding();
                    anchorInfo.mLayoutFromEnd = true;
                    return true;
                }
                anchorInfo.mCoordinate = anchorInfo.mLayoutFromEnd ? (mOrientationHelper.getDecoratedEnd(child) + mOrientationHelper.getTotalSpaceChange()) : mOrientationHelper.getDecoratedStart(child);
            } else {
                if (getChildCount() > 0) {
                    int pos = getPosition(getChildAt(0));
                    anchorInfo.mLayoutFromEnd = mPendingScrollPosition < pos == mShouldReverseLayout;
                }
                anchorInfo.assignCoordinateFromPadding();
            }
            return true;
        }
        anchorInfo.mLayoutFromEnd = mShouldReverseLayout;
        if (mShouldReverseLayout) {
            anchorInfo.mCoordinate = mOrientationHelper.getEndAfterPadding() - mPendingScrollPositionOffset;
        } else {
            anchorInfo.mCoordinate = mOrientationHelper.getStartAfterPadding() + mPendingScrollPositionOffset;
        }
        return true;
    }

    private int fixLayoutEndGap(int endOffset, RecyclerView.Recycler recycler, RecyclerView.State state, boolean canOffsetChildren) {
        int gap = mOrientationHelper.getEndAfterPadding() - endOffset;
        int fixOffset = 0;
        if (gap > 0) {
            fixOffset = -scrollBy(-gap, recycler, state);
        } else {
            return 0;
        }
        endOffset += fixOffset;
        if (canOffsetChildren) {
            gap = mOrientationHelper.getEndAfterPadding() - endOffset;
            if (gap > 0) {
                mOrientationHelper.offsetChildren(gap);
                return gap + fixOffset;
            }
        }
        return fixOffset;
    }

    private int fixLayoutStartGap(int startOffset, RecyclerView.Recycler recycler, RecyclerView.State state, boolean canOffsetChildren) {
        int gap = startOffset - mOrientationHelper.getStartAfterPadding();
        int fixOffset = 0;
        if (gap > 0) {
            fixOffset = -scrollBy(gap, recycler, state);
        } else {
            return 0;
        }
        startOffset += fixOffset;
        if (canOffsetChildren) {
            gap = startOffset - mOrientationHelper.getStartAfterPadding();
            if (gap > 0) {
                mOrientationHelper.offsetChildren(-gap);
                return fixOffset - gap;
            }
        }
        return fixOffset;
    }

    private void updateLayoutStateToFillEnd(AnchorInfo anchorInfo) {
        updateLayoutStateToFillEnd(anchorInfo.mPosition, anchorInfo.mCoordinate);
    }

    private void updateLayoutStateToFillEnd(int itemPosition, int offset) {
        mLayoutState.mAvailable = mOrientationHelper.getEndAfterPadding() - offset;
        mLayoutState.mItemDirection = mShouldReverseLayout ? LayoutState.ITEM_DIRECTION_HEAD : LayoutState.ITEM_DIRECTION_TAIL;
        mLayoutState.mCurrentPosition = itemPosition;
        mLayoutState.mLayoutDirection = LayoutState.LAYOUT_END;
        mLayoutState.mOffset = offset;
        mLayoutState.mScrollingOffset = LayoutState.SCROLLING_OFFSET_NaN;
    }

    private void updateLayoutStateToFillStart(AnchorInfo anchorInfo) {
        updateLayoutStateToFillStart(anchorInfo.mPosition, anchorInfo.mCoordinate);
    }

    private void updateLayoutStateToFillStart(int itemPosition, int offset) {
        mLayoutState.mAvailable = offset - mOrientationHelper.getStartAfterPadding();
        mLayoutState.mCurrentPosition = itemPosition;
        mLayoutState.mItemDirection = mShouldReverseLayout ? LayoutState.ITEM_DIRECTION_TAIL : LayoutState.ITEM_DIRECTION_HEAD;
        mLayoutState.mLayoutDirection = LayoutState.LAYOUT_START;
        mLayoutState.mOffset = offset;
        mLayoutState.mScrollingOffset = LayoutState.SCROLLING_OFFSET_NaN;

    }

    protected boolean isLayoutRTL() {
        return getLayoutDirection() == ViewCompat.LAYOUT_DIRECTION_RTL;
    }

    void ensureLayoutState() {
        if (mLayoutState == null) {
            mLayoutState = createLayoutState();
        }
    }

    LayoutState createLayoutState() {
        return new LayoutState();
    }

    @Override
    public void scrollToPosition(int position) {
        mPendingScrollPosition = position;
        mPendingScrollPositionOffset = INVALID_OFFSET;
        if (mPendingSavedState != null) {
            mPendingSavedState.invalidateAnchor();
        }
        if (mRecyclerView != null) {
            mRecyclerView.showGoToTop();
        }
        requestLayout();
    }

    public void scrollToPositionWithOffset(int position, int offset) {
        mPendingScrollPosition = position;
        mPendingScrollPositionOffset = offset;
        if (mPendingSavedState != null) {
            mPendingSavedState.invalidateAnchor();
        }
        if (mRecyclerView != null) {
            mRecyclerView.showGoToTop();
        }
        requestLayout();
    }

    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (mOrientation == VERTICAL) {
            return 0;
        }
        return scrollBy(dx, recycler, state);
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (mOrientation == HORIZONTAL) {
            return 0;
        }
        return scrollBy(dy, recycler, state);
    }

    @Override
    public int computeHorizontalScrollOffset(RecyclerView.State state) {
        return computeScrollOffset(state);
    }

    @Override
    public int computeVerticalScrollOffset(RecyclerView.State state) {
        return computeScrollOffset(state);
    }

    @Override
    public int computeHorizontalScrollExtent(RecyclerView.State state) {
        return computeScrollExtent(state);
    }

    @Override
    public int computeVerticalScrollExtent(RecyclerView.State state) {
        return computeScrollExtent(state);
    }

    @Override
    public int computeHorizontalScrollRange(RecyclerView.State state) {
        return computeScrollRange(state);
    }

    @Override
    public int computeVerticalScrollRange(RecyclerView.State state) {
        return computeScrollRange(state);
    }

    private int computeScrollOffset(RecyclerView.State state) {
        if (getChildCount() == 0) {
            return 0;
        }
        ensureLayoutState();
        return ScrollbarHelper.computeScrollOffset(state, mOrientationHelper, findFirstVisibleChildClosestToStart(!mSmoothScrollbarEnabled, true), findFirstVisibleChildClosestToEnd(!mSmoothScrollbarEnabled, true), this, mSmoothScrollbarEnabled, mShouldReverseLayout);
    }

    private int computeScrollExtent(RecyclerView.State state) {
        if (getChildCount() == 0) {
            return 0;
        }
        ensureLayoutState();
        return ScrollbarHelper.computeScrollExtent(state, mOrientationHelper, findFirstVisibleChildClosestToStart(!mSmoothScrollbarEnabled, true), findFirstVisibleChildClosestToEnd(!mSmoothScrollbarEnabled, true), this, mSmoothScrollbarEnabled);
    }

    private int computeScrollRange(RecyclerView.State state) {
        if (getChildCount() == 0) {
            return 0;
        }
        ensureLayoutState();
        return ScrollbarHelper.computeScrollRange(state, mOrientationHelper, findFirstVisibleChildClosestToStart(!mSmoothScrollbarEnabled, true), findFirstVisibleChildClosestToEnd(!mSmoothScrollbarEnabled, true), this, mSmoothScrollbarEnabled);
    }

    public void setSmoothScrollbarEnabled(boolean enabled) {
        mSmoothScrollbarEnabled = enabled;
    }

    public boolean isSmoothScrollbarEnabled() {
        return mSmoothScrollbarEnabled;
    }

    private void updateLayoutState(int layoutDirection, int requiredSpace, boolean canUseExistingSpace, RecyclerView.State state) {
        mLayoutState.mInfinite = resolveIsInfinite();
        mLayoutState.mLayoutDirection = layoutDirection;
        mReusableIntPair[0] = 0;
        mReusableIntPair[1] = 0;
        calculateExtraLayoutSpace(state, mReusableIntPair);
        int extraForStart = Math.max(0, mReusableIntPair[0]);
        int extraForEnd = Math.max(0, mReusableIntPair[1]);
        boolean layoutToEnd = layoutDirection == LayoutState.LAYOUT_END;
        mLayoutState.mExtraFillSpace = layoutToEnd ? extraForEnd : extraForStart;
        mLayoutState.mNoRecycleSpace = layoutToEnd ? extraForStart : extraForEnd;
        int scrollingOffset;
        if (layoutToEnd) {
            mLayoutState.mExtraFillSpace += mOrientationHelper.getEndPadding();
            final View child = getChildClosestToEnd();
            mLayoutState.mItemDirection = mShouldReverseLayout ? LayoutState.ITEM_DIRECTION_HEAD : LayoutState.ITEM_DIRECTION_TAIL;
            mLayoutState.mCurrentPosition = getPosition(child) + mLayoutState.mItemDirection;
            mLayoutState.mOffset = mOrientationHelper.getDecoratedEnd(child);
            scrollingOffset = mOrientationHelper.getDecoratedEnd(child) - mOrientationHelper.getEndAfterPadding();
        } else {
            final View child = getChildClosestToStart();
            mLayoutState.mExtraFillSpace += mOrientationHelper.getStartAfterPadding();
            mLayoutState.mItemDirection = mShouldReverseLayout ? LayoutState.ITEM_DIRECTION_TAIL : LayoutState.ITEM_DIRECTION_HEAD;
            mLayoutState.mCurrentPosition = getPosition(child) + mLayoutState.mItemDirection;
            mLayoutState.mOffset = mOrientationHelper.getDecoratedStart(child);
            scrollingOffset = -mOrientationHelper.getDecoratedStart(child) + mOrientationHelper.getStartAfterPadding();
        }
        mLayoutState.mAvailable = requiredSpace;
        if (canUseExistingSpace) {
            mLayoutState.mAvailable -= scrollingOffset;
        }
        mLayoutState.mScrollingOffset = scrollingOffset;
    }

    boolean resolveIsInfinite() {
        return mOrientationHelper.getMode() == View.MeasureSpec.UNSPECIFIED && mOrientationHelper.getEnd() == 0;
    }

    void collectPrefetchPositionsForLayoutState(RecyclerView.State state, LayoutState layoutState, LayoutPrefetchRegistry layoutPrefetchRegistry) {
        final int pos = layoutState.mCurrentPosition;
        if (pos >= 0 && pos < state.getItemCount()) { layoutPrefetchRegistry.addPosition(pos, Math.max(0, layoutState.mScrollingOffset));
        }
    }

    @Override
    public void collectInitialPrefetchPositions(int adapterItemCount, LayoutPrefetchRegistry layoutPrefetchRegistry) {
        final boolean fromEnd;
        final int anchorPos;
        if (mPendingSavedState != null && mPendingSavedState.hasValidAnchor()) {
            fromEnd = mPendingSavedState.mAnchorLayoutFromEnd;
            anchorPos = mPendingSavedState.mAnchorPosition;
        } else {
            resolveShouldLayoutReverse();
            fromEnd = mShouldReverseLayout;
            if (mPendingScrollPosition == RecyclerView.NO_POSITION) {
                anchorPos = fromEnd ? adapterItemCount - 1 : 0;
            } else {
                anchorPos = mPendingScrollPosition;
            }
        }

        final int direction = fromEnd ? LayoutState.ITEM_DIRECTION_HEAD : LayoutState.ITEM_DIRECTION_TAIL;
        int targetPos = anchorPos;
        for (int i = 0; i < mInitialPrefetchItemCount; i++) {
            if (targetPos >= 0 && targetPos < adapterItemCount) {
                layoutPrefetchRegistry.addPosition(targetPos, 0);
            } else {
                break;
            }
            targetPos += direction;
        }
    }

    public void setInitialPrefetchItemCount(int itemCount) {
        mInitialPrefetchItemCount = itemCount;
    }

    public int getInitialPrefetchItemCount() {
        return mInitialPrefetchItemCount;
    }

    @Override
    public void collectAdjacentPrefetchPositions(int dx, int dy, RecyclerView.State state, LayoutPrefetchRegistry layoutPrefetchRegistry) {
        int delta = (mOrientation == HORIZONTAL) ? dx : dy;
        if (getChildCount() == 0 || delta == 0) {
            return;
        }

        ensureLayoutState();
        final int layoutDirection = delta > 0 ? LayoutState.LAYOUT_END : LayoutState.LAYOUT_START;
        final int absDelta = Math.abs(delta);
        updateLayoutState(layoutDirection, absDelta, true, state);
        collectPrefetchPositionsForLayoutState(state, mLayoutState, layoutPrefetchRegistry);
    }

    int scrollBy(int delta, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (getChildCount() == 0 || delta == 0) {
            return 0;
        }
        ensureLayoutState();
        mLayoutState.mRecycle = true;
        final int layoutDirection = delta > 0 ? LayoutState.LAYOUT_END : LayoutState.LAYOUT_START;
        final int absDelta = Math.abs(delta);
        updateLayoutState(layoutDirection, absDelta, true, state);
        final int consumed = mLayoutState.mScrollingOffset + fill(recycler, mLayoutState, state, false);
        if (consumed < 0) {
            if (DEBUG) {
                Log.d(TAG, "Don't have any more elements to scroll");
            }
            return 0;
        }
        final int scrolled = absDelta > consumed ? layoutDirection * consumed : delta;
        mOrientationHelper.offsetChildren(-scrolled);
        if (DEBUG) {
            Log.d(TAG, "scroll req: " + delta + " scrolled: " + scrolled);
        }
        mLayoutState.mLastScrollDelta = scrolled;
        if (state.mLayoutStep != RecyclerView.State.STEP_LAYOUT) {
            mRecyclerView.showGoToTop();
        }
        return scrolled;
    }

    @Override
    public void assertNotInLayoutOrScroll(String message) {
        if (mPendingSavedState == null) {
            super.assertNotInLayoutOrScroll(message);
        }
    }

    private void recycleChildren(RecyclerView.Recycler recycler, int startIndex, int endIndex) {
        if (startIndex == endIndex) {
            return;
        }
        if (DEBUG) {
            Log.d(TAG, "Recycling " + Math.abs(startIndex - endIndex) + " items");
        }
        if (endIndex > startIndex) {
            for (int i = endIndex - 1; i >= startIndex; i--) {
                removeAndRecycleViewAt(i, recycler);
            }
        } else {
            for (int i = startIndex; i > endIndex; i--) {
                removeAndRecycleViewAt(i, recycler);
            }
        }
    }

    private void recycleViewsFromStart(RecyclerView.Recycler recycler, int scrollingOffset, int noRecycleSpace) {
        if (scrollingOffset < 0) {
            if (DEBUG) {
                Log.d(TAG, "Called recycle from start with a negative value. This might happen during layout changes but may be sign of a bug");
            }
            return;
        }
        final int limit = scrollingOffset - noRecycleSpace;
        final int childCount = getChildCount();
        if (mShouldReverseLayout) {
            for (int i = childCount - 1; i >= 0; i--) {
                View child = getChildAt(i);
                if (mOrientationHelper.getDecoratedEnd(child) > limit || mOrientationHelper.getTransformedEndWithDecoration(child) > limit) {
                    recycleChildren(recycler, childCount - 1, i);
                    return;
                }
            }
        } else {
            for (int i = 0; i < childCount; i++) {
                View child = getChildAt(i);
                if (mOrientationHelper.getDecoratedEnd(child) > limit || mOrientationHelper.getTransformedEndWithDecoration(child) > limit) {
                    recycleChildren(recycler, 0, i);
                    return;
                }
            }
        }
    }

    private void recycleViewsFromEnd(RecyclerView.Recycler recycler, int scrollingOffset, int noRecycleSpace) {
        final int childCount = getChildCount();
        if (scrollingOffset < 0) {
            if (DEBUG) {
                Log.d(TAG, "Called recycle from end with a negative value. This might happen during layout changes but may be sign of a bug");
            }
            return;
        }
        final int limit = mOrientationHelper.getEnd() - scrollingOffset + noRecycleSpace;
        if (mShouldReverseLayout) {
            for (int i = 0; i < childCount; i++) {
                View child = getChildAt(i);
                if (mOrientationHelper.getDecoratedStart(child) < limit || mOrientationHelper.getTransformedStartWithDecoration(child) < limit) {
                    recycleChildren(recycler, 0, i);
                    return;
                }
            }
        } else {
            for (int i = childCount - 1; i >= 0; i--) {
                View child = getChildAt(i);
                if (mOrientationHelper.getDecoratedStart(child) < limit || mOrientationHelper.getTransformedStartWithDecoration(child) < limit) {
                    recycleChildren(recycler, childCount - 1, i);
                    return;
                }
            }
        }
    }

    private void recycleByLayoutState(RecyclerView.Recycler recycler, LayoutState layoutState) {
        if (!layoutState.mRecycle || layoutState.mInfinite) {
            return;
        }
        int scrollingOffset = layoutState.mScrollingOffset;
        int noRecycleSpace = layoutState.mNoRecycleSpace;
        if (layoutState.mLayoutDirection == LayoutState.LAYOUT_START) {
            recycleViewsFromEnd(recycler, scrollingOffset, noRecycleSpace);
        } else {
            recycleViewsFromStart(recycler, scrollingOffset, noRecycleSpace);
        }
    }

    int fill(RecyclerView.Recycler recycler, LayoutState layoutState, RecyclerView.State state, boolean stopOnFocusable) {
        final int start = layoutState.mAvailable;
        if (layoutState.mScrollingOffset != LayoutState.SCROLLING_OFFSET_NaN) {
            if (layoutState.mAvailable < 0) {
                layoutState.mScrollingOffset += layoutState.mAvailable;
            }
            recycleByLayoutState(recycler, layoutState);
        }
        int remainingSpace = layoutState.mAvailable + layoutState.mExtraFillSpace;
        LayoutChunkResult layoutChunkResult = mLayoutChunkResult;
        while ((layoutState.mInfinite || remainingSpace > 0) && layoutState.hasMore(state)) {
            layoutChunkResult.resetInternal();
            if (RecyclerView.VERBOSE_TRACING) {
                TraceCompat.beginSection("LLM LayoutChunk");
            }
            layoutChunk(recycler, state, layoutState, layoutChunkResult);
            if (RecyclerView.VERBOSE_TRACING) {
                TraceCompat.endSection();
            }
            if (layoutChunkResult.mFinished) {
                break;
            }
            layoutState.mOffset += layoutChunkResult.mConsumed * layoutState.mLayoutDirection;
            if (!layoutChunkResult.mIgnoreConsumed || layoutState.mScrapList != null || !state.isPreLayout()) {
                layoutState.mAvailable -= layoutChunkResult.mConsumed;
                remainingSpace -= layoutChunkResult.mConsumed;
            }

            if (layoutState.mScrollingOffset != LayoutState.SCROLLING_OFFSET_NaN) {
                layoutState.mScrollingOffset += layoutChunkResult.mConsumed;
                if (layoutState.mAvailable < 0) {
                    layoutState.mScrollingOffset += layoutState.mAvailable;
                }
                recycleByLayoutState(recycler, layoutState);
            }
            if (stopOnFocusable && layoutChunkResult.mFocusable) {
                break;
            }
        }
        if (DEBUG) {
            validateChildOrder();
        }
        return start - layoutState.mAvailable;
    }

    void layoutChunk(RecyclerView.Recycler recycler, RecyclerView.State state, LayoutState layoutState, LayoutChunkResult result) {
        View view = layoutState.next(recycler);
        if (view == null) {
            if (DEBUG && layoutState.mScrapList == null) {
                throw new RuntimeException("received null view when unexpected");
            }
            result.mFinished = true;
            return;
        }
        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) view.getLayoutParams();
        if (layoutState.mScrapList == null) {
            if (mShouldReverseLayout == (layoutState.mLayoutDirection == LayoutState.LAYOUT_START)) {
                addView(view);
            } else {
                addView(view, 0);
            }
        } else {
            if (mShouldReverseLayout == (layoutState.mLayoutDirection == LayoutState.LAYOUT_START)) {
                addDisappearingView(view);
            } else {
                addDisappearingView(view, 0);
            }
        }
        measureChildWithMargins(view, 0, 0);
        result.mConsumed = mOrientationHelper.getDecoratedMeasurement(view);
        int left, top, right, bottom;
        if (mOrientation == VERTICAL) {
            if (isLayoutRTL()) {
                right = getWidth() - getPaddingRight();
                left = right - mOrientationHelper.getDecoratedMeasurementInOther(view);
            } else {
                left = getPaddingLeft();
                right = left + mOrientationHelper.getDecoratedMeasurementInOther(view);
            }
            if (layoutState.mLayoutDirection == LayoutState.LAYOUT_START) {
                bottom = layoutState.mOffset;
                top = layoutState.mOffset - result.mConsumed;
            } else {
                top = layoutState.mOffset;
                bottom = layoutState.mOffset + result.mConsumed;
            }
        } else {
            top = getPaddingTop();
            bottom = top + mOrientationHelper.getDecoratedMeasurementInOther(view);

            if (layoutState.mLayoutDirection == LayoutState.LAYOUT_START) {
                right = layoutState.mOffset;
                left = layoutState.mOffset - result.mConsumed;
            } else {
                left = layoutState.mOffset;
                right = layoutState.mOffset + result.mConsumed;
            }
        }
        layoutDecoratedWithMargins(view, left, top, right, bottom);
        if (DEBUG) {
            Log.d(TAG, "laid out child at position " + getPosition(view) + ", with l:" + (left + params.leftMargin) + ", t:" + (top + params.topMargin) + ", r:" + (right - params.rightMargin) + ", b:" + (bottom - params.bottomMargin));
        }
        if (params.isItemRemoved() || params.isItemChanged()) {
            result.mIgnoreConsumed = true;
        }
        result.mFocusable = view.hasFocusable();
    }

    @Override
    public boolean shouldMeasureTwice() {
        return getHeightMode() != View.MeasureSpec.EXACTLY && getWidthMode() != View.MeasureSpec.EXACTLY && hasFlexibleChildInBothOrientations();
    }

    int convertFocusDirectionToLayoutDirection(int focusDirection) {
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
                if (DEBUG) {
                    Log.d(TAG, "Unknown focus request:" + focusDirection);
                }
                return LayoutState.INVALID_LAYOUT;
        }
    }

    private View getChildClosestToStart() {
        return getChildAt(mShouldReverseLayout ? getChildCount() - 1 : 0);
    }

    private View getChildClosestToEnd() {
        return getChildAt(mShouldReverseLayout ? 0 : getChildCount() - 1);
    }

    View findFirstVisibleChildClosestToStart(boolean completelyVisible, boolean acceptPartiallyVisible) {
        if (mShouldReverseLayout) {
            return findOneVisibleChild(getChildCount() - 1, -1, completelyVisible, acceptPartiallyVisible);
        } else {
            return findOneVisibleChild(0, getChildCount(), completelyVisible, acceptPartiallyVisible);
        }
    }

    View findFirstVisibleChildClosestToEnd(boolean completelyVisible, boolean acceptPartiallyVisible) {
        if (mShouldReverseLayout) {
            return findOneVisibleChild(0, getChildCount(), completelyVisible, acceptPartiallyVisible);
        } else {
            return findOneVisibleChild(getChildCount() - 1, -1, completelyVisible, acceptPartiallyVisible);
        }
    }

    View findReferenceChild(RecyclerView.Recycler recycler, RecyclerView.State state, boolean layoutFromEnd, boolean traverseChildrenInReverseOrder) {
        ensureLayoutState();

        int start = 0;
        int end = getChildCount();
        int diff = 1;
        if (traverseChildrenInReverseOrder) {
            start = getChildCount() - 1;
            end = -1;
            diff = -1;
        }

        int itemCount = state.getItemCount();

        final int boundsStart = mOrientationHelper.getStartAfterPadding();
        final int boundsEnd = mOrientationHelper.getEndAfterPadding();

        View invalidMatch = null;
        View bestFirstFind = null;
        View bestSecondFind = null;

        for (int i = start; i != end; i += diff) {
            final View view = getChildAt(i);
            final int position = getPosition(view);
            final int childStart = mOrientationHelper.getDecoratedStart(view);
            final int childEnd = mOrientationHelper.getDecoratedEnd(view);
            if (position >= 0 && position < itemCount) {
                if (((RecyclerView.LayoutParams) view.getLayoutParams()).isItemRemoved()) {
                    if (invalidMatch == null) {
                        invalidMatch = view;
                    }
                } else {
                    boolean outOfBoundsBefore = childEnd <= boundsStart && childStart < boundsStart;
                    boolean outOfBoundsAfter = childStart >= boundsEnd && childEnd > boundsEnd;
                    if (outOfBoundsBefore || outOfBoundsAfter) {
                        if (layoutFromEnd) {
                            if (outOfBoundsAfter) {
                                bestFirstFind = view;
                            } else if (bestSecondFind == null) {
                                bestSecondFind = view;
                            }
                        } else {
                            if (outOfBoundsBefore) {
                                bestFirstFind = view;
                            } else if (bestSecondFind == null) {
                                bestSecondFind = view;
                            }
                        }
                    } else {
                        return view;
                    }
                }
            }
        }
        return bestSecondFind != null ? bestSecondFind : (bestFirstFind != null ? bestFirstFind : invalidMatch);
    }

    private View findPartiallyOrCompletelyInvisibleChildClosestToEnd() {
        return mShouldReverseLayout ? findFirstPartiallyOrCompletelyInvisibleChild() : findLastPartiallyOrCompletelyInvisibleChild();
    }

    private View findPartiallyOrCompletelyInvisibleChildClosestToStart() {
        return mShouldReverseLayout ? findLastPartiallyOrCompletelyInvisibleChild() : findFirstPartiallyOrCompletelyInvisibleChild();
    }

    private View findFirstPartiallyOrCompletelyInvisibleChild() {
        return findOnePartiallyOrCompletelyInvisibleChild(0, getChildCount());
    }

    private View findLastPartiallyOrCompletelyInvisibleChild() {
        return findOnePartiallyOrCompletelyInvisibleChild(getChildCount() - 1, -1);
    }

    public int findFirstVisibleItemPosition() {
        final View child = findOneVisibleChild(0, getChildCount(), false, true);
        return child == null ? RecyclerView.NO_POSITION : getPosition(child);
    }

    public int findFirstCompletelyVisibleItemPosition() {
        final View child = findOneVisibleChild(0, getChildCount(), true, false);
        return child == null ? RecyclerView.NO_POSITION : getPosition(child);
    }

    public int findLastVisibleItemPosition() {
        final View child = findOneVisibleChild(getChildCount() - 1, -1, false, true);
        return child == null ? RecyclerView.NO_POSITION : getPosition(child);
    }

    public int findLastCompletelyVisibleItemPosition() {
        final View child = findOneVisibleChild(getChildCount() - 1, -1, true, false);
        return child == null ? RecyclerView.NO_POSITION : getPosition(child);
    }

    // Returns the first child that is visible in the provided index range, i.e. either partially or
    // fully visible depending on the arguments provided. Completely invisible children are not
    // acceptable by this method, but could be returned
    // using #findOnePartiallyOrCompletelyInvisibleChild
    View findOneVisibleChild(int fromIndex, int toIndex, boolean completelyVisible, boolean acceptPartiallyVisible) {
        ensureLayoutState();
        @ViewBoundsCheck.ViewBounds int preferredBoundsFlag = 0;
        @ViewBoundsCheck.ViewBounds int acceptableBoundsFlag = 0;
        if (completelyVisible) {
            preferredBoundsFlag = (ViewBoundsCheck.FLAG_CVS_GT_PVS | ViewBoundsCheck.FLAG_CVS_EQ_PVS | ViewBoundsCheck.FLAG_CVE_LT_PVE | ViewBoundsCheck.FLAG_CVE_EQ_PVE);
        } else {
            preferredBoundsFlag = (ViewBoundsCheck.FLAG_CVS_LT_PVE | ViewBoundsCheck.FLAG_CVE_GT_PVS);
        }
        if (acceptPartiallyVisible) {
            acceptableBoundsFlag = (ViewBoundsCheck.FLAG_CVS_LT_PVE | ViewBoundsCheck.FLAG_CVE_GT_PVS);
        }
        return (mOrientation == HORIZONTAL) ? mHorizontalBoundCheck.findOneViewWithinBoundFlags(fromIndex, toIndex, preferredBoundsFlag, acceptableBoundsFlag) : mVerticalBoundCheck.findOneViewWithinBoundFlags(fromIndex, toIndex, preferredBoundsFlag, acceptableBoundsFlag);
    }

    View findOnePartiallyOrCompletelyInvisibleChild(int fromIndex, int toIndex) {
        ensureLayoutState();
        final int next = toIndex > fromIndex ? 1 : (toIndex < fromIndex ? -1 : 0);
        if (next == 0) {
            return getChildAt(fromIndex);
        }
        @ViewBoundsCheck.ViewBounds int preferredBoundsFlag = 0;
        @ViewBoundsCheck.ViewBounds int acceptableBoundsFlag = 0;
        if (mOrientationHelper.getDecoratedStart(getChildAt(fromIndex)) < mOrientationHelper.getStartAfterPadding()) {
            preferredBoundsFlag = (ViewBoundsCheck.FLAG_CVS_LT_PVS | ViewBoundsCheck.FLAG_CVE_LT_PVE | ViewBoundsCheck.FLAG_CVE_GT_PVS);
            acceptableBoundsFlag = (ViewBoundsCheck.FLAG_CVS_LT_PVS | ViewBoundsCheck.FLAG_CVE_LT_PVE);
        } else {
            preferredBoundsFlag = (ViewBoundsCheck.FLAG_CVE_GT_PVE | ViewBoundsCheck.FLAG_CVS_GT_PVS | ViewBoundsCheck.FLAG_CVS_LT_PVE);
            acceptableBoundsFlag = (ViewBoundsCheck.FLAG_CVE_GT_PVE | ViewBoundsCheck.FLAG_CVS_GT_PVS);
        }
        return (mOrientation == HORIZONTAL) ? mHorizontalBoundCheck.findOneViewWithinBoundFlags(fromIndex, toIndex, preferredBoundsFlag, acceptableBoundsFlag) : mVerticalBoundCheck.findOneViewWithinBoundFlags(fromIndex, toIndex, preferredBoundsFlag, acceptableBoundsFlag);
    }

    @Override
    public View onFocusSearchFailed(View focused, int direction, RecyclerView.Recycler recycler, RecyclerView.State state) {
        resolveShouldLayoutReverse();
        if (getChildCount() == 0) {
            return null;
        }

        final int layoutDir = convertFocusDirectionToLayoutDirection(direction);
        if (layoutDir == LayoutState.INVALID_LAYOUT) {
            return null;
        }
        ensureLayoutState();
        final int maxScroll = (int) (MAX_SCROLL_FACTOR * mOrientationHelper.getTotalSpace());
        updateLayoutState(layoutDir, maxScroll, false, state);
        mLayoutState.mScrollingOffset = LayoutState.SCROLLING_OFFSET_NaN;
        mLayoutState.mRecycle = false;
        fill(recycler, mLayoutState, state, true);

        final View nextCandidate;
        if (layoutDir == LayoutState.LAYOUT_START) {
            nextCandidate = findPartiallyOrCompletelyInvisibleChildClosestToStart();
        } else {
            nextCandidate = findPartiallyOrCompletelyInvisibleChildClosestToEnd();
        }
        final View nextFocus;
        if (layoutDir == LayoutState.LAYOUT_START) {
            nextFocus = getChildClosestToStart();
        } else {
            nextFocus = getChildClosestToEnd();
        }
        if (nextFocus.hasFocusable()) {
            if (nextCandidate == null) {
                return null;
            }
            return nextFocus;
        }
        return nextCandidate;
    }

    private void logChildren() {
        Log.d(TAG, "internal representation of views on the screen");
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            Log.d(TAG, "item " + getPosition(child) + ", coord:" + mOrientationHelper.getDecoratedStart(child));
        }
        Log.d(TAG, "==============");
    }

    void validateChildOrder() {
        Log.d(TAG, "validating child count " + getChildCount());
        if (getChildCount() < 1) {
            return;
        }
        int lastPos = getPosition(getChildAt(0));
        int lastScreenLoc = mOrientationHelper.getDecoratedStart(getChildAt(0));
        if (mShouldReverseLayout) {
            for (int i = 1; i < getChildCount(); i++) {
                View child = getChildAt(i);
                int pos = getPosition(child);
                int screenLoc = mOrientationHelper.getDecoratedStart(child);
                if (pos < lastPos) {
                    logChildren();
                    throw new RuntimeException("detected invalid position. loc invalid? " + (screenLoc < lastScreenLoc));
                }
                if (screenLoc > lastScreenLoc) {
                    logChildren();
                    throw new RuntimeException("detected invalid location");
                }
            }
        } else {
            for (int i = 1; i < getChildCount(); i++) {
                View child = getChildAt(i);
                int pos = getPosition(child);
                int screenLoc = mOrientationHelper.getDecoratedStart(child);
                if (pos < lastPos) {
                    logChildren();
                    throw new RuntimeException("detected invalid position. loc invalid? " + (screenLoc < lastScreenLoc));
                }
                if (screenLoc < lastScreenLoc) {
                    logChildren();
                    throw new RuntimeException("detected invalid location");
                }
            }
        }
    }

    @Override
    public boolean supportsPredictiveItemAnimations() {
        return mPendingSavedState == null && mLastStackFromEnd == mStackFromEnd;
    }

    @Override
    public void prepareForDrop(@NonNull View view, @NonNull View target, int x, int y) {
        assertNotInLayoutOrScroll("Cannot drop a view during a scroll or layout calculation");
        ensureLayoutState();
        resolveShouldLayoutReverse();
        final int myPos = getPosition(view);
        final int targetPos = getPosition(target);
        final int dropDirection = myPos < targetPos ? LayoutState.ITEM_DIRECTION_TAIL : LayoutState.ITEM_DIRECTION_HEAD;
        if (mShouldReverseLayout) {
            if (dropDirection == LayoutState.ITEM_DIRECTION_TAIL) {
                scrollToPositionWithOffset(targetPos, mOrientationHelper.getEndAfterPadding() - (mOrientationHelper.getDecoratedStart(target) + mOrientationHelper.getDecoratedMeasurement(view)));
            } else {
                scrollToPositionWithOffset(targetPos, mOrientationHelper.getEndAfterPadding() - mOrientationHelper.getDecoratedEnd(target));
            }
        } else {
            if (dropDirection == LayoutState.ITEM_DIRECTION_HEAD) {
                scrollToPositionWithOffset(targetPos, mOrientationHelper.getDecoratedStart(target));
            } else {
                scrollToPositionWithOffset(targetPos, mOrientationHelper.getDecoratedEnd(target) - mOrientationHelper.getDecoratedMeasurement(view));
            }
        }
    }


    static class LayoutState {
        static final String TAG = "LLM#LayoutState";
        static final int LAYOUT_START = -1;
        static final int LAYOUT_END = 1;
        static final int INVALID_LAYOUT = Integer.MIN_VALUE;
        static final int ITEM_DIRECTION_HEAD = -1;
        static final int ITEM_DIRECTION_TAIL = 1;
        static final int SCROLLING_OFFSET_NaN = Integer.MIN_VALUE;
        boolean mRecycle = true;
        int mOffset;
        int mAvailable;
        int mCurrentPosition;
        int mItemDirection;
        int mLayoutDirection;
        int mScrollingOffset;
        int mExtraFillSpace = 0;
        int mNoRecycleSpace = 0;
        boolean mIsPreLayout = false;
        int mLastScrollDelta;
        List<RecyclerView.ViewHolder> mScrapList = null;
        boolean mInfinite;

        boolean hasMore(RecyclerView.State state) {
            return mCurrentPosition >= 0 && mCurrentPosition < state.getItemCount();
        }

        View next(RecyclerView.Recycler recycler) {
            if (mScrapList != null) {
                return nextViewFromScrapList();
            }
            final View view = recycler.getViewForPosition(mCurrentPosition);
            mCurrentPosition += mItemDirection;
            return view;
        }

        private View nextViewFromScrapList() {
            final int size = mScrapList.size();
            for (int i = 0; i < size; i++) {
                final View view = mScrapList.get(i).itemView;
                final RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) view.getLayoutParams();
                if (lp.isItemRemoved()) {
                    continue;
                }
                if (mCurrentPosition == lp.getViewLayoutPosition()) {
                    assignPositionFromScrapList(view);
                    return view;
                }
            }
            return null;
        }

        public void assignPositionFromScrapList() {
            assignPositionFromScrapList(null);
        }

        public void assignPositionFromScrapList(View ignore) {
            final View closest = nextViewInLimitedList(ignore);
            if (closest == null) {
                mCurrentPosition = RecyclerView.NO_POSITION;
            } else {
                mCurrentPosition = ((RecyclerView.LayoutParams) closest.getLayoutParams()).getViewLayoutPosition();
            }
        }

        public View nextViewInLimitedList(View ignore) {
            int size = mScrapList.size();
            View closest = null;
            int closestDistance = Integer.MAX_VALUE;
            if (DEBUG && mIsPreLayout) {
                throw new IllegalStateException("Scrap list cannot be used in pre layout");
            }
            for (int i = 0; i < size; i++) {
                View view = mScrapList.get(i).itemView;
                final RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) view.getLayoutParams();
                if (view == ignore || lp.isItemRemoved()) {
                    continue;
                }
                final int distance = (lp.getViewLayoutPosition() - mCurrentPosition) * mItemDirection;
                if (distance < 0) {
                    continue;
                }
                if (distance < closestDistance) {
                    closest = view;
                    closestDistance = distance;
                    if (distance == 0) {
                        break;
                    }
                }
            }
            return closest;
        }

        void log() {
            Log.d(TAG, "avail:" + mAvailable + ", ind:" + mCurrentPosition + ", dir:" + mItemDirection + ", offset:" + mOffset + ", layoutDir:" + mLayoutDirection);
        }
    }

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
        int mAnchorOffset;
        boolean mAnchorLayoutFromEnd;

        public SavedState() {
        }

        SavedState(Parcel in) {
            mAnchorPosition = in.readInt();
            mAnchorOffset = in.readInt();
            mAnchorLayoutFromEnd = in.readInt() == 1;
        }

        public SavedState(SavedState other) {
            mAnchorPosition = other.mAnchorPosition;
            mAnchorOffset = other.mAnchorOffset;
            mAnchorLayoutFromEnd = other.mAnchorLayoutFromEnd;
        }

        boolean hasValidAnchor() {
            return mAnchorPosition >= 0;
        }

        void invalidateAnchor() {
            mAnchorPosition = RecyclerView.NO_POSITION;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(mAnchorPosition);
            dest.writeInt(mAnchorOffset);
            dest.writeInt(mAnchorLayoutFromEnd ? 1 : 0);
        }
    }

    static class AnchorInfo {
        OrientationHelper mOrientationHelper;
        int mPosition;
        int mCoordinate;
        boolean mLayoutFromEnd;
        boolean mValid;

        AnchorInfo() {
            reset();
        }

        void reset() {
            mPosition = RecyclerView.NO_POSITION;
            mCoordinate = INVALID_OFFSET;
            mLayoutFromEnd = false;
            mValid = false;
        }

        void assignCoordinateFromPadding() {
            mCoordinate = mLayoutFromEnd ? mOrientationHelper.getEndAfterPadding() : mOrientationHelper.getStartAfterPadding();
        }

        @Override
        public String toString() {
            return "AnchorInfo{mPosition=" + mPosition + ", mCoordinate=" + mCoordinate + ", mLayoutFromEnd=" + mLayoutFromEnd + ", mValid=" + mValid + '}';
        }

        boolean isViewValidAsAnchor(View child, RecyclerView.State state) {
            RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) child.getLayoutParams();
            return !lp.isItemRemoved() && lp.getViewLayoutPosition() >= 0 && lp.getViewLayoutPosition() < state.getItemCount();
        }

        public void assignFromViewAndKeepVisibleRect(View child, int position) {
            final int spaceChange = mOrientationHelper.getTotalSpaceChange();
            if (spaceChange >= 0) {
                assignFromView(child, position);
                return;
            }
            mPosition = position;
            if (mLayoutFromEnd) {
                final int prevLayoutEnd = mOrientationHelper.getEndAfterPadding() - spaceChange;
                final int childEnd = mOrientationHelper.getDecoratedEnd(child);
                final int previousEndMargin = prevLayoutEnd - childEnd;
                mCoordinate = mOrientationHelper.getEndAfterPadding() - previousEndMargin;
                if (previousEndMargin > 0) {
                    final int childSize = mOrientationHelper.getDecoratedMeasurement(child);
                    final int estimatedChildStart = mCoordinate - childSize;
                    final int layoutStart = mOrientationHelper.getStartAfterPadding();
                    final int previousStartMargin = mOrientationHelper.getDecoratedStart(child) - layoutStart;
                    final int startReference = layoutStart + Math.min(previousStartMargin, 0);
                    final int startMargin = estimatedChildStart - startReference;
                    if (startMargin < 0) {
                        mCoordinate += Math.min(previousEndMargin, -startMargin);
                    }
                }
            } else {
                final int childStart = mOrientationHelper.getDecoratedStart(child);
                final int startMargin = childStart - mOrientationHelper.getStartAfterPadding();
                mCoordinate = childStart;
                if (startMargin > 0) {
                    final int estimatedEnd = childStart + mOrientationHelper.getDecoratedMeasurement(child);
                    final int previousLayoutEnd = mOrientationHelper.getEndAfterPadding() - spaceChange;
                    final int previousEndMargin = previousLayoutEnd - mOrientationHelper.getDecoratedEnd(child);
                    final int endReference = mOrientationHelper.getEndAfterPadding() - Math.min(0, previousEndMargin);
                    final int endMargin = endReference - estimatedEnd;
                    if (endMargin < 0) {
                        mCoordinate -= Math.min(startMargin, -endMargin);
                    }
                }
            }
        }

        public void assignFromView(View child, int position) {
            if (mLayoutFromEnd) {
                mCoordinate = mOrientationHelper.getDecoratedEnd(child) + mOrientationHelper.getTotalSpaceChange();
            } else {
                mCoordinate = mOrientationHelper.getDecoratedStart(child);
            }

            mPosition = position;
        }
    }

    protected static class LayoutChunkResult {
        public int mConsumed;
        public boolean mFinished;
        public boolean mIgnoreConsumed;
        public boolean mFocusable;

        void resetInternal() {
            mConsumed = 0;
            mFinished = false;
            mIgnoreConsumed = false;
            mFocusable = false;
        }
    }
}
