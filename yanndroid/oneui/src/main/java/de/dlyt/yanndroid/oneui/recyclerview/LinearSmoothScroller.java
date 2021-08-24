package de.dlyt.yanndroid.oneui.recyclerview;

import android.content.Context;
import android.graphics.PointF;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;

public class LinearSmoothScroller extends SeslRecyclerView.SmoothScroller {
    public static final int SNAP_TO_START = -1;
    public static final int SNAP_TO_END = 1;
    public static final int SNAP_TO_ANY = 0;
    private static final String TAG = "LinearSmoothScroller";
    private static final boolean DEBUG = false;
    private static final float MILLISECONDS_PER_INCH = 25f;
    private static final int TARGET_SEEK_SCROLL_DISTANCE_PX = 10000;
    private static final float TARGET_SEEK_EXTRA_SCROLL_RATIO = 1.2f;
    protected final LinearInterpolator mLinearInterpolator = new LinearInterpolator();
    protected final DecelerateInterpolator mDecelerateInterpolator = new DecelerateInterpolator();
    private final float MILLISECONDS_PER_PX;
    protected PointF mTargetVector;
    protected int mInterimTargetDx = 0, mInterimTargetDy = 0;

    public LinearSmoothScroller(Context context) {
        MILLISECONDS_PER_PX = calculateSpeedPerPixel(context.getResources().getDisplayMetrics());
    }

    @Override
    protected void onStart() {
    }

    @Override
    protected void onTargetFound(View targetView, SeslRecyclerView.State state, Action action) {
        final int dx = calculateDxToMakeVisible(targetView, getHorizontalSnapPreference());
        final int dy = calculateDyToMakeVisible(targetView, getVerticalSnapPreference());
        final int distance = (int) Math.sqrt(dx * dx + dy * dy);
        final int time = calculateTimeForDeceleration(distance);
        if (time > 0) {
            action.update(-dx, -dy, time, mDecelerateInterpolator);
        }
    }

    @Override
    protected void onSeekTargetStep(int dx, int dy, SeslRecyclerView.State state, Action action) {
        if (getChildCount() == 0) {
            stop();
            return;
        }
        if (DEBUG && mTargetVector != null && ((mTargetVector.x * dx < 0 || mTargetVector.y * dy < 0))) {
            throw new IllegalStateException("Scroll happened in the opposite direction" + " of the target. Some calculations are wrong");
        }
        mInterimTargetDx = clampApplyScroll(mInterimTargetDx, dx);
        mInterimTargetDy = clampApplyScroll(mInterimTargetDy, dy);

        if (mInterimTargetDx == 0 && mInterimTargetDy == 0) {
            updateActionForInterimTarget(action);
        }

    }

    @Override
    protected void onStop() {
        mInterimTargetDx = mInterimTargetDy = 0;
        mTargetVector = null;
    }

    protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
        return MILLISECONDS_PER_INCH / displayMetrics.densityDpi;
    }

    protected int calculateTimeForDeceleration(int dx) {
        return (int) Math.ceil(calculateTimeForScrolling(dx) / .3356);
    }

    protected int calculateTimeForScrolling(int dx) {
        return (int) Math.ceil(Math.abs(dx) * MILLISECONDS_PER_PX);
    }

    protected int getHorizontalSnapPreference() {
        return mTargetVector == null || mTargetVector.x == 0 ? SNAP_TO_ANY : mTargetVector.x > 0 ? SNAP_TO_END : SNAP_TO_START;
    }

    protected int getVerticalSnapPreference() {
        return mTargetVector == null || mTargetVector.y == 0 ? SNAP_TO_ANY : mTargetVector.y > 0 ? SNAP_TO_END : SNAP_TO_START;
    }

    protected void updateActionForInterimTarget(Action action) {
        PointF scrollVector = computeScrollVectorForPosition(getTargetPosition());
        if (scrollVector == null || (scrollVector.x == 0 && scrollVector.y == 0)) {
            final int target = getTargetPosition();
            action.jumpTo(target);
            stop();
            return;
        }
        normalize(scrollVector);
        mTargetVector = scrollVector;

        mInterimTargetDx = (int) (TARGET_SEEK_SCROLL_DISTANCE_PX * scrollVector.x);
        mInterimTargetDy = (int) (TARGET_SEEK_SCROLL_DISTANCE_PX * scrollVector.y);
        final int time = calculateTimeForScrolling(TARGET_SEEK_SCROLL_DISTANCE_PX);
        action.update((int) (mInterimTargetDx * TARGET_SEEK_EXTRA_SCROLL_RATIO), (int) (mInterimTargetDy * TARGET_SEEK_EXTRA_SCROLL_RATIO), (int) (time * TARGET_SEEK_EXTRA_SCROLL_RATIO), mLinearInterpolator);
    }

    private int clampApplyScroll(int tmpDt, int dt) {
        final int before = tmpDt;
        tmpDt -= dt;
        if (before * tmpDt <= 0) {
            return 0;
        }
        return tmpDt;
    }

    public int calculateDtToFit(int viewStart, int viewEnd, int boxStart, int boxEnd, int snapPreference) {
        switch (snapPreference) {
            case SNAP_TO_START:
                return boxStart - viewStart;
            case SNAP_TO_END:
                return boxEnd - viewEnd;
            case SNAP_TO_ANY:
                final int dtStart = boxStart - viewStart;
                if (dtStart > 0) {
                    return dtStart;
                }
                final int dtEnd = boxEnd - viewEnd;
                if (dtEnd < 0) {
                    return dtEnd;
                }
                break;
            default:
                throw new IllegalArgumentException("snap preference should be one of the" + " constants defined in SmoothScroller, starting with SNAP_");
        }
        return 0;
    }

    public int calculateDyToMakeVisible(View view, int snapPreference) {
        final SeslRecyclerView.LayoutManager layoutManager = getLayoutManager();
        if (layoutManager == null || !layoutManager.canScrollVertically()) {
            return 0;
        }
        final SeslRecyclerView.LayoutParams params = (SeslRecyclerView.LayoutParams) view.getLayoutParams();
        final int top = layoutManager.getDecoratedTop(view) - params.topMargin;
        final int bottom = layoutManager.getDecoratedBottom(view) + params.bottomMargin;
        final int start = layoutManager.getPaddingTop();
        final int end = layoutManager.getHeight() - layoutManager.getPaddingBottom();
        return calculateDtToFit(top, bottom, start, end, snapPreference);
    }

    public int calculateDxToMakeVisible(View view, int snapPreference) {
        final SeslRecyclerView.LayoutManager layoutManager = getLayoutManager();
        if (layoutManager == null || !layoutManager.canScrollHorizontally()) {
            return 0;
        }
        final SeslRecyclerView.LayoutParams params = (SeslRecyclerView.LayoutParams) view.getLayoutParams();
        final int left = layoutManager.getDecoratedLeft(view) - params.leftMargin;
        final int right = layoutManager.getDecoratedRight(view) + params.rightMargin;
        final int start = layoutManager.getPaddingLeft();
        final int end = layoutManager.getWidth() - layoutManager.getPaddingRight();
        return calculateDtToFit(left, right, start, end, snapPreference);
    }

    public PointF computeScrollVectorForPosition(int targetPosition) {
        SeslRecyclerView.LayoutManager layoutManager = getLayoutManager();
        if (layoutManager instanceof ScrollVectorProvider) {
            return ((ScrollVectorProvider) layoutManager).computeScrollVectorForPosition(targetPosition);
        }
        Log.w(TAG, "You should override computeScrollVectorForPosition when the LayoutManager" + " does not implement " + ScrollVectorProvider.class.getCanonicalName());
        return null;
    }
}
