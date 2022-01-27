package de.dlyt.yanndroid.oneui.sesl.viewpager2.widget;

import static de.dlyt.yanndroid.oneui.sesl.viewpager2.widget.SeslViewPager2.ORIENTATION_HORIZONTAL;

import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;

import androidx.annotation.UiThread;

import de.dlyt.yanndroid.oneui.view.RecyclerView;

final class FakeDrag {
    private final SeslViewPager2 mViewPager;
    private final ScrollEventAdapter mScrollEventAdapter;
    private final RecyclerView mRecyclerView;
    private VelocityTracker mVelocityTracker;
    private int mMaximumVelocity;
    private float mRequestedDragDistance;
    private int mActualDraggedDistance;
    private long mFakeDragBeginTime;

    FakeDrag(SeslViewPager2 viewPager, ScrollEventAdapter scrollEventAdapter, RecyclerView recyclerView) {
        mViewPager = viewPager;
        mScrollEventAdapter = scrollEventAdapter;
        mRecyclerView = recyclerView;
    }

    boolean isFakeDragging() {
        return mScrollEventAdapter.isFakeDragging();
    }

    @UiThread
    boolean beginFakeDrag() {
        if (mScrollEventAdapter.isDragging()) {
            return false;
        }
        mRequestedDragDistance = mActualDraggedDistance = 0;
        mFakeDragBeginTime = SystemClock.uptimeMillis();
        beginFakeVelocityTracker();

        mScrollEventAdapter.notifyBeginFakeDrag();
        if (!mScrollEventAdapter.isIdle()) {
            mRecyclerView.stopScroll();
        }
        addFakeMotionEvent(mFakeDragBeginTime, MotionEvent.ACTION_DOWN, 0, 0);
        return true;
    }

    @UiThread
    boolean fakeDragBy(float offsetPxFloat) {
        if (!mScrollEventAdapter.isFakeDragging()) {
            return false;
        }
        mRequestedDragDistance -= offsetPxFloat;
        int offsetPx = Math.round(mRequestedDragDistance - mActualDraggedDistance);
        mActualDraggedDistance += offsetPx;
        long time = SystemClock.uptimeMillis();

        boolean isHorizontal = mViewPager.getOrientation() == ORIENTATION_HORIZONTAL;
        final int offsetX = isHorizontal ? offsetPx : 0;
        final int offsetY = isHorizontal ? 0 : offsetPx;
        final float x = isHorizontal ? mRequestedDragDistance : 0;
        final float y = isHorizontal ? 0 : mRequestedDragDistance;

        mRecyclerView.scrollBy(offsetX, offsetY);
        addFakeMotionEvent(time, MotionEvent.ACTION_MOVE, x, y);
        return true;
    }

    @UiThread
    boolean endFakeDrag() {
        if (!mScrollEventAdapter.isFakeDragging()) {
            return false;
        }

        mScrollEventAdapter.notifyEndFakeDrag();

        final int pixelsPerSecond = 1000;
        final VelocityTracker velocityTracker = mVelocityTracker;
        velocityTracker.computeCurrentVelocity(pixelsPerSecond, mMaximumVelocity);
        int xVelocity = (int) velocityTracker.getXVelocity();
        int yVelocity = (int) velocityTracker.getYVelocity();
        if (!mRecyclerView.fling(xVelocity, yVelocity)) {
            mViewPager.snapToPage();
        }
        return true;
    }

    private void beginFakeVelocityTracker() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
            final ViewConfiguration configuration = ViewConfiguration.get(mViewPager.getContext());
            mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
        } else {
            mVelocityTracker.clear();
        }
    }

    private void addFakeMotionEvent(long time, int action, float x, float y) {
        final MotionEvent ev = MotionEvent.obtain(mFakeDragBeginTime, time, action, x, y, 0);
        mVelocityTracker.addMovement(ev);
        ev.recycle();
    }
}
