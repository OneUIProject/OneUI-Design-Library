package de.dlyt.yanndroid.oneui.recyclerview;

import android.graphics.Rect;
import android.view.View;

public abstract class OrientationHelper {
    public static final int HORIZONTAL = 0;
    public static final int VERTICAL = 1;
    private static final int INVALID_SIZE = Integer.MIN_VALUE;
    protected final SeslRecyclerView.LayoutManager mLayoutManager;
    final Rect mTmpRect;
    private int mLastTotalSpace;

    private OrientationHelper(SeslRecyclerView.LayoutManager layoutManager) {
        mLastTotalSpace = Integer.MIN_VALUE;
        mTmpRect = new Rect();
        mLayoutManager = layoutManager;
    }

    public static OrientationHelper createOrientationHelper(SeslRecyclerView.LayoutManager layoutManager, int orientation) {
        switch (orientation) {
            case HORIZONTAL:
                return createHorizontalHelper(layoutManager);
            case VERTICAL:
                return createVerticalHelper(layoutManager);
            default:
                throw new IllegalArgumentException("invalid orientation");
        }
    }

    public static OrientationHelper createHorizontalHelper(SeslRecyclerView.LayoutManager layoutManager) {
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
                SeslRecyclerView.LayoutParams params = (SeslRecyclerView.LayoutParams) view.getLayoutParams();
                return mLayoutManager.getDecoratedMeasuredWidth(view) + params.leftMargin + params.rightMargin;
            }

            @Override
            public int getDecoratedMeasurementInOther(View view) {
                SeslRecyclerView.LayoutParams params = (SeslRecyclerView.LayoutParams) view.getLayoutParams();
                return mLayoutManager.getDecoratedMeasuredHeight(view) + params.topMargin + params.bottomMargin;
            }

            @Override
            public int getDecoratedEnd(View view) {
                return mLayoutManager.getDecoratedRight(view) + ((SeslRecyclerView.LayoutParams) view.getLayoutParams()).rightMargin;
            }

            @Override
            public int getDecoratedStart(View view) {
                return mLayoutManager.getDecoratedLeft(view) - ((SeslRecyclerView.LayoutParams) view.getLayoutParams()).leftMargin;
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
                return (mLayoutManager.getWidth() - mLayoutManager.getPaddingLeft()) - mLayoutManager.getPaddingRight();
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

    public static OrientationHelper createVerticalHelper(SeslRecyclerView.LayoutManager layoutManager) {
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
                SeslRecyclerView.LayoutParams params = (SeslRecyclerView.LayoutParams) view.getLayoutParams();
                return mLayoutManager.getDecoratedMeasuredHeight(view) + params.topMargin + params.bottomMargin;
            }

            @Override
            public int getDecoratedMeasurementInOther(View view) {
                SeslRecyclerView.LayoutParams params = (SeslRecyclerView.LayoutParams) view.getLayoutParams();
                return mLayoutManager.getDecoratedMeasuredWidth(view) + params.leftMargin + params.rightMargin;
            }

            @Override
            public int getDecoratedEnd(View view) {
                return mLayoutManager.getDecoratedBottom(view) + ((SeslRecyclerView.LayoutParams) view.getLayoutParams()).bottomMargin;
            }

            @Override
            public int getDecoratedStart(View view) {
                return mLayoutManager.getDecoratedTop(view) - ((SeslRecyclerView.LayoutParams) view.getLayoutParams()).topMargin;
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
                return (mLayoutManager.getHeight() - mLayoutManager.getPaddingTop()) - mLayoutManager.getPaddingBottom();
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

    public abstract int getDecoratedEnd(View view);

    public abstract int getDecoratedMeasurement(View view);

    public abstract int getDecoratedMeasurementInOther(View view);

    public abstract int getDecoratedStart(View view);

    public abstract int getEnd();

    public abstract int getEndAfterPadding();

    public abstract int getEndPadding();

    public abstract int getMode();

    public abstract int getModeInOther();

    public abstract int getStartAfterPadding();

    public abstract int getTotalSpace();

    public abstract int getTransformedEndWithDecoration(View view);

    public abstract int getTransformedStartWithDecoration(View view);

    public abstract void offsetChild(View view, int i);

    public abstract void offsetChildren(int i);

    public SeslRecyclerView.LayoutManager getLayoutManager() {
        return mLayoutManager;
    }

    public void onLayoutComplete() {
        mLastTotalSpace = getTotalSpace();
    }

    public int getTotalSpaceChange() {
        if (Integer.MIN_VALUE == mLastTotalSpace) {
            return 0;
        }
        return getTotalSpace() - mLastTotalSpace;
    }
}