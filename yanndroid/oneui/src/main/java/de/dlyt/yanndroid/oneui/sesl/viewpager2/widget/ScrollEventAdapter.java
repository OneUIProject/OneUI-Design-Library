package de.dlyt.yanndroid.oneui.sesl.viewpager2.widget;

import static de.dlyt.yanndroid.oneui.sesl.viewpager2.widget.SeslViewPager2.ORIENTATION_HORIZONTAL;
import static de.dlyt.yanndroid.oneui.sesl.viewpager2.widget.SeslViewPager2.SCROLL_STATE_DRAGGING;
import static de.dlyt.yanndroid.oneui.sesl.viewpager2.widget.SeslViewPager2.SCROLL_STATE_IDLE;
import static de.dlyt.yanndroid.oneui.sesl.viewpager2.widget.SeslViewPager2.SCROLL_STATE_SETTLING;
import static de.dlyt.yanndroid.oneui.sesl.viewpager2.widget.SeslViewPager2.ScrollState;

import static java.lang.annotation.RetentionPolicy.SOURCE;

import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;

import de.dlyt.yanndroid.oneui.sesl.recyclerview.LinearLayoutManager;
import de.dlyt.yanndroid.oneui.sesl.viewpager2.widget.SeslViewPager2.OnPageChangeCallback;
import de.dlyt.yanndroid.oneui.view.RecyclerView;

import java.lang.annotation.Retention;
import java.util.Locale;

final class ScrollEventAdapter extends RecyclerView.OnScrollListener {
    private static final int STATE_IDLE = 0;
    private static final int STATE_IN_PROGRESS_MANUAL_DRAG = 1;
    private static final int STATE_IN_PROGRESS_SMOOTH_SCROLL = 2;
    private static final int STATE_IN_PROGRESS_IMMEDIATE_SCROLL = 3;
    private static final int STATE_IN_PROGRESS_FAKE_DRAG = 4;
    @Retention(SOURCE)
    @IntDef({STATE_IDLE, STATE_IN_PROGRESS_MANUAL_DRAG, STATE_IN_PROGRESS_SMOOTH_SCROLL, STATE_IN_PROGRESS_IMMEDIATE_SCROLL, STATE_IN_PROGRESS_FAKE_DRAG})
    private @interface AdapterState {
    }
    private static final int NO_POSITION = -1;
    private OnPageChangeCallback mCallback;
    private final @NonNull
    SeslViewPager2 mViewPager;
    private final @NonNull RecyclerView mRecyclerView;
    private final @NonNull LinearLayoutManager mLayoutManager;
    private @AdapterState int mAdapterState;
    private @SeslViewPager2.ScrollState int mScrollState;
    private ScrollEventValues mScrollValues;
    private int mDragStartPosition;
    private int mTarget;
    private boolean mDispatchSelected;
    private boolean mScrollHappened;
    private boolean mDataSetChangeHappened;
    private boolean mFakeDragging;

    ScrollEventAdapter(@NonNull SeslViewPager2 viewPager) {
        mViewPager = viewPager;
        mRecyclerView = mViewPager.mRecyclerView;
        mLayoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
        mScrollValues = new ScrollEventValues();
        resetState();
    }

    private void resetState() {
        mAdapterState = STATE_IDLE;
        mScrollState = SCROLL_STATE_IDLE;
        mScrollValues.reset();
        mDragStartPosition = NO_POSITION;
        mTarget = NO_POSITION;
        mDispatchSelected = false;
        mScrollHappened = false;
        mFakeDragging = false;
        mDataSetChangeHappened = false;
    }

    @Override
    public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
        if ((mAdapterState != STATE_IN_PROGRESS_MANUAL_DRAG || mScrollState != SCROLL_STATE_DRAGGING) && newState == RecyclerView.SCROLL_STATE_DRAGGING) {
            startDrag(false);
            return;
        }

        if (isInAnyDraggingState() && newState == RecyclerView.SCROLL_STATE_SETTLING) {
            if (mScrollHappened) {
                dispatchStateChanged(SCROLL_STATE_SETTLING);
                mDispatchSelected = true;
            }
            return;
        }

        if (isInAnyDraggingState() && newState == RecyclerView.SCROLL_STATE_IDLE) {
            boolean dispatchIdle = false;
            updateScrollEventValues();
            if (!mScrollHappened) {
                if (mScrollValues.mPosition != RecyclerView.NO_POSITION) {
                    dispatchScrolled(mScrollValues.mPosition, 0f, 0);
                }
                dispatchIdle = true;
            } else if (mScrollValues.mOffsetPx == 0) {
                dispatchIdle = true;
                if (mDragStartPosition != mScrollValues.mPosition) {
                    dispatchSelected(mScrollValues.mPosition);
                }
            }
            if (dispatchIdle) {
                dispatchStateChanged(SCROLL_STATE_IDLE);
                resetState();
            }
        }

        if (mAdapterState == STATE_IN_PROGRESS_SMOOTH_SCROLL && newState == RecyclerView.SCROLL_STATE_IDLE && mDataSetChangeHappened) {
            updateScrollEventValues();
            if (mScrollValues.mOffsetPx == 0) {
                if (mTarget != mScrollValues.mPosition) {
                    dispatchSelected(mScrollValues.mPosition == NO_POSITION ? 0 : mScrollValues.mPosition);
                }
                dispatchStateChanged(SCROLL_STATE_IDLE);
                resetState();
            }
        }
    }

    @Override
    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        mScrollHappened = true;
        updateScrollEventValues();

        if (mDispatchSelected) {
            mDispatchSelected = false;
            boolean scrollingForward = dy > 0 || (dy == 0 && dx < 0 == mViewPager.isRtl());

            mTarget = scrollingForward && mScrollValues.mOffsetPx != 0 ? mScrollValues.mPosition + 1 : mScrollValues.mPosition;
            if (mDragStartPosition != mTarget) {
                dispatchSelected(mTarget);
            }
        } else if (mAdapterState == STATE_IDLE) {
            int position = mScrollValues.mPosition;
            dispatchSelected(position == NO_POSITION ? 0 : position);
        }

        dispatchScrolled(mScrollValues.mPosition == NO_POSITION ? 0 : mScrollValues.mPosition, mScrollValues.mOffset, mScrollValues.mOffsetPx);

        if ((mScrollValues.mPosition == mTarget || mTarget == NO_POSITION) && mScrollValues.mOffsetPx == 0 && !(mScrollState == SCROLL_STATE_DRAGGING)) {
            dispatchStateChanged(SCROLL_STATE_IDLE);
            resetState();
        }
    }

    private void updateScrollEventValues() {
        ScrollEventValues values = mScrollValues;

        values.mPosition = mLayoutManager.findFirstVisibleItemPosition();
        if (values.mPosition == RecyclerView.NO_POSITION) {
            values.reset();
            return;
        }
        View firstVisibleView = mLayoutManager.findViewByPosition(values.mPosition);
        if (firstVisibleView == null) {
            values.reset();
            return;
        }

        int leftDecorations = mLayoutManager.getLeftDecorationWidth(firstVisibleView);
        int rightDecorations = mLayoutManager.getRightDecorationWidth(firstVisibleView);
        int topDecorations = mLayoutManager.getTopDecorationHeight(firstVisibleView);
        int bottomDecorations = mLayoutManager.getBottomDecorationHeight(firstVisibleView);

        LayoutParams params = firstVisibleView.getLayoutParams();
        if (params instanceof MarginLayoutParams) {
            MarginLayoutParams margin = (MarginLayoutParams) params;
            leftDecorations += margin.leftMargin;
            rightDecorations += margin.rightMargin;
            topDecorations += margin.topMargin;
            bottomDecorations += margin.bottomMargin;
        }

        int decoratedHeight = firstVisibleView.getHeight() + topDecorations + bottomDecorations;
        int decoratedWidth = firstVisibleView.getWidth() + leftDecorations + rightDecorations;

        boolean isHorizontal = mLayoutManager.getOrientation() == ORIENTATION_HORIZONTAL;
        int start, sizePx;
        if (isHorizontal) {
            sizePx = decoratedWidth;
            start = firstVisibleView.getLeft() - leftDecorations - mRecyclerView.getPaddingLeft();
            if (mViewPager.isRtl()) {
                start = -start;
            }
        } else {
            sizePx = decoratedHeight;
            start = firstVisibleView.getTop() - topDecorations - mRecyclerView.getPaddingTop();
        }

        values.mOffsetPx = -start;
        if (values.mOffsetPx < 0) {
            if (new AnimateLayoutChangeDetector(mLayoutManager).mayHaveInterferingAnimations()) {
                throw new IllegalStateException("Page(s) contain a ViewGroup with a LayoutTransition (or animateLayoutChanges=\"true\"), which interferes with the scrolling animation. Make sure to call getLayoutTransition().setAnimateParentHierarchy(false) on all ViewGroups with a LayoutTransition before an animation is started.");
            }

            throw new IllegalStateException(String.format(Locale.US, "Page can only be offset by a positive amount, not by %d", values.mOffsetPx));
        }
        values.mOffset = sizePx == 0 ? 0 : (float) values.mOffsetPx / sizePx;
    }

    private void startDrag(boolean isFakeDrag) {
        mFakeDragging = isFakeDrag;
        mAdapterState = isFakeDrag ? STATE_IN_PROGRESS_FAKE_DRAG : STATE_IN_PROGRESS_MANUAL_DRAG;
        if (mTarget != NO_POSITION) {
            mTarget = NO_POSITION;
        } else if (mDragStartPosition == NO_POSITION) {
            mDragStartPosition = getPosition();
        }
        dispatchStateChanged(SCROLL_STATE_DRAGGING);
    }

    void notifyDataSetChangeHappened() {
        mDataSetChangeHappened = true;
    }

    void notifyProgrammaticScroll(int target, boolean smooth) {
        mAdapterState = smooth ? STATE_IN_PROGRESS_SMOOTH_SCROLL : STATE_IN_PROGRESS_IMMEDIATE_SCROLL;
        mFakeDragging = false;
        boolean hasNewTarget = mTarget != target;
        mTarget = target;
        dispatchStateChanged(SCROLL_STATE_SETTLING);
        if (hasNewTarget) {
            dispatchSelected(target);
        }
    }

    void notifyBeginFakeDrag() {
        mAdapterState = STATE_IN_PROGRESS_FAKE_DRAG;
        startDrag(true);
    }

    void notifyEndFakeDrag() {
        if (isDragging() && !mFakeDragging) {
            return;
        }
        mFakeDragging = false;
        updateScrollEventValues();
        if (mScrollValues.mOffsetPx == 0) {
            if (mScrollValues.mPosition != mDragStartPosition) {
                dispatchSelected(mScrollValues.mPosition);
            }
            dispatchStateChanged(SCROLL_STATE_IDLE);
            resetState();
        } else {
            dispatchStateChanged(SCROLL_STATE_SETTLING);
        }
    }

    void setOnPageChangeCallback(OnPageChangeCallback callback) {
        mCallback = callback;
    }

    int getScrollState() {
        return mScrollState;
    }

    boolean isIdle() {
        return mScrollState == SCROLL_STATE_IDLE;
    }

    boolean isDragging() {
        return mScrollState == SCROLL_STATE_DRAGGING;
    }

    boolean isFakeDragging() {
        return mFakeDragging;
    }

    private boolean isInAnyDraggingState() {
        return mAdapterState == STATE_IN_PROGRESS_MANUAL_DRAG || mAdapterState == STATE_IN_PROGRESS_FAKE_DRAG;
    }

    double getRelativeScrollPosition() {
        updateScrollEventValues();
        return mScrollValues.mPosition + (double) mScrollValues.mOffset;
    }

    private void dispatchStateChanged(@ScrollState int state) {
        if (mAdapterState == STATE_IN_PROGRESS_IMMEDIATE_SCROLL && mScrollState == SCROLL_STATE_IDLE) {
            return;
        }
        if (mScrollState == state) {
            return;
        }

        mScrollState = state;
        if (mCallback != null) {
            mCallback.onPageScrollStateChanged(state);
        }
    }

    private void dispatchSelected(int target) {
        if (mCallback != null) {
            mCallback.onPageSelected(target);
        }
    }

    private void dispatchScrolled(int position, float offset, int offsetPx) {
        if (mCallback != null) {
            mCallback.onPageScrolled(position, offset, offsetPx);
        }
    }

    private int getPosition() {
        return mLayoutManager.findFirstVisibleItemPosition();
    }

    private static final class ScrollEventValues {
        int mPosition;
        float mOffset;
        int mOffsetPx;

        ScrollEventValues() {
        }

        void reset() {
            mPosition = RecyclerView.NO_POSITION;
            mOffset = 0f;
            mOffsetPx = 0;
        }
    }
}
