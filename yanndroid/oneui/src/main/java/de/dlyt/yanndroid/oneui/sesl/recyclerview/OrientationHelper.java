package de.dlyt.yanndroid.oneui.sesl.recyclerview;

import android.graphics.Rect;
import android.view.View;

import de.dlyt.yanndroid.oneui.view.RecyclerView;

public abstract class OrientationHelper {
    private static final int INVALID_SIZE = Integer.MIN_VALUE;
    protected final RecyclerView.LayoutManager mLayoutManager;
    public static final int HORIZONTAL = RecyclerView.HORIZONTAL;
    public static final int VERTICAL = RecyclerView.VERTICAL;
    private int mLastTotalSpace = INVALID_SIZE;
    final Rect mTmpRect = new Rect();

    private OrientationHelper(RecyclerView.LayoutManager layoutManager) {
        mLayoutManager = layoutManager;
    }

    public RecyclerView.LayoutManager getLayoutManager() {
        return mLayoutManager;
    }

    public void onLayoutComplete() {
        mLastTotalSpace = getTotalSpace();
    }

    public int getTotalSpaceChange() {
        return INVALID_SIZE == mLastTotalSpace ? 0 : getTotalSpace() - mLastTotalSpace;
    }

    public abstract int getDecoratedStart(View view);

    public abstract int getDecoratedEnd(View view);

    public abstract int getTransformedEndWithDecoration(View view);

    public abstract int getTransformedStartWithDecoration(View view);

    public abstract int getDecoratedMeasurement(View view);

    public abstract int getDecoratedMeasurementInOther(View view);

    public abstract int getStartAfterPadding();

    public abstract int getEndAfterPadding();

    public abstract int getEnd();

    public abstract void offsetChildren(int amount);

    public abstract int getTotalSpace();

    public abstract void offsetChild(View view, int offset);

    public abstract int getEndPadding();

    public abstract int getMode();

    public abstract int getModeInOther();

    public static OrientationHelper createOrientationHelper(RecyclerView.LayoutManager layoutManager, @RecyclerView.Orientation int orientation) {
        switch (orientation) {
            case HORIZONTAL:
                return createHorizontalHelper(layoutManager);
            case VERTICAL:
                return createVerticalHelper(layoutManager);
        }
        throw new IllegalArgumentException("invalid orientation");
    }

    public static OrientationHelper createHorizontalHelper(RecyclerView.LayoutManager layoutManager) {
        return new OrientationHelper(layoutManager) {
            @Override
            public int getEndAfterPadding() {
                return mLayoutManager.getWidth() - mLayoutManager.getPaddingRight();
            }

            @Override
            public int getEnd() {
                return mLayoutManager.getWidth();
            }

            @Override
            public void offsetChildren(int amount) {
                mLayoutManager.offsetChildrenHorizontal(amount);
            }

            @Override
            public int getStartAfterPadding() {
                return mLayoutManager.getPaddingLeft();
            }

            @Override
            public int getDecoratedMeasurement(View view) {
                final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) view.getLayoutParams();
                return mLayoutManager.getDecoratedMeasuredWidth(view) + params.leftMargin + params.rightMargin;
            }

            @Override
            public int getDecoratedMeasurementInOther(View view) {
                final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) view.getLayoutParams();
                return mLayoutManager.getDecoratedMeasuredHeight(view) + params.topMargin + params.bottomMargin;
            }

            @Override
            public int getDecoratedEnd(View view) {
                final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) view.getLayoutParams();
                return mLayoutManager.getDecoratedRight(view) + params.rightMargin;
            }

            @Override
            public int getDecoratedStart(View view) {
                final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) view.getLayoutParams();
                return mLayoutManager.getDecoratedLeft(view) - params.leftMargin;
            }

            @Override
            public int getTransformedEndWithDecoration(View view) {
                mLayoutManager.getTransformedBoundingBox(view, true, mTmpRect);
                return mTmpRect.right;
            }

            @Override
            public int getTransformedStartWithDecoration(View view) {
                mLayoutManager.getTransformedBoundingBox(view, true, mTmpRect);
                return mTmpRect.left;
            }

            @Override
            public int getTotalSpace() {
                return mLayoutManager.getWidth() - mLayoutManager.getPaddingLeft() - mLayoutManager.getPaddingRight();
            }

            @Override
            public void offsetChild(View view, int offset) {
                view.offsetLeftAndRight(offset);
            }

            @Override
            public int getEndPadding() {
                return mLayoutManager.getPaddingRight();
            }

            @Override
            public int getMode() {
                return mLayoutManager.getWidthMode();
            }

            @Override
            public int getModeInOther() {
                return mLayoutManager.getHeightMode();
            }
        };
    }

    public static OrientationHelper createVerticalHelper(RecyclerView.LayoutManager layoutManager) {
        return new OrientationHelper(layoutManager) {
            @Override
            public int getEndAfterPadding() {
                return mLayoutManager.getHeight() - mLayoutManager.getPaddingBottom();
            }

            @Override
            public int getEnd() {
                return mLayoutManager.getHeight();
            }

            @Override
            public void offsetChildren(int amount) {
                mLayoutManager.offsetChildrenVertical(amount);
            }

            @Override
            public int getStartAfterPadding() {
                return mLayoutManager.getPaddingTop();
            }

            @Override
            public int getDecoratedMeasurement(View view) {
                final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) view.getLayoutParams();
                return mLayoutManager.getDecoratedMeasuredHeight(view) + params.topMargin + params.bottomMargin;
            }

            @Override
            public int getDecoratedMeasurementInOther(View view) {
                final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) view.getLayoutParams();
                return mLayoutManager.getDecoratedMeasuredWidth(view) + params.leftMargin + params.rightMargin;
            }

            @Override
            public int getDecoratedEnd(View view) {
                final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) view.getLayoutParams();
                return mLayoutManager.getDecoratedBottom(view) + params.bottomMargin;
            }

            @Override
            public int getDecoratedStart(View view) {
                final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) view.getLayoutParams();
                return mLayoutManager.getDecoratedTop(view) - params.topMargin;
            }

            @Override
            public int getTransformedEndWithDecoration(View view) {
                mLayoutManager.getTransformedBoundingBox(view, true, mTmpRect);
                return mTmpRect.bottom;
            }

            @Override
            public int getTransformedStartWithDecoration(View view) {
                mLayoutManager.getTransformedBoundingBox(view, true, mTmpRect);
                return mTmpRect.top;
            }

            @Override
            public int getTotalSpace() {
                return mLayoutManager.getHeight() - mLayoutManager.getPaddingTop() - mLayoutManager.getPaddingBottom();
            }

            @Override
            public void offsetChild(View view, int offset) {
                view.offsetTopAndBottom(offset);
            }

            @Override
            public int getEndPadding() {
                return mLayoutManager.getPaddingBottom();
            }

            @Override
            public int getMode() {
                return mLayoutManager.getHeightMode();
            }

            @Override
            public int getModeInOther() {
                return mLayoutManager.getWidthMode();
            }
        };
    }
}
