package de.dlyt.yanndroid.oneui.sesl.recyclerview;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Build;
import android.util.Log;
import android.view.GestureDetector;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewParent;
import android.view.animation.Interpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.core.view.GestureDetectorCompat;
import androidx.core.view.InputDeviceCompat;
import androidx.core.view.ViewCompat;

import java.util.ArrayList;
import java.util.List;

import de.dlyt.yanndroid.oneui.R;
import de.dlyt.yanndroid.oneui.view.RecyclerView;
import de.dlyt.yanndroid.oneui.view.RecyclerView.OnItemTouchListener;
import de.dlyt.yanndroid.oneui.view.RecyclerView.ViewHolder;

public class ItemTouchHelper extends RecyclerView.ItemDecoration implements RecyclerView.OnChildAttachStateChangeListener {
    public static final int UP = 1;
    public static final int DOWN = 1 << 1;
    public static final int LEFT = 1 << 2;
    public static final int RIGHT = 1 << 3;
    public static final int START = LEFT << 2;
    public static final int END = RIGHT << 2;
    public static final int ACTION_STATE_IDLE = 0;
    @SuppressWarnings("WeakerAccess")
    public static final int ACTION_STATE_SWIPE = 1;
    @SuppressWarnings("WeakerAccess")
    public static final int ACTION_STATE_DRAG = 2;
    @SuppressWarnings("WeakerAccess")
    public static final int ANIMATION_TYPE_SWIPE_SUCCESS = 1 << 1;
    @SuppressWarnings("WeakerAccess")
    public static final int ANIMATION_TYPE_SWIPE_CANCEL = 1 << 2;
    @SuppressWarnings("WeakerAccess")
    public static final int ANIMATION_TYPE_DRAG = 1 << 3;
    private static final String TAG = "ItemTouchHelper";
    private static final boolean DEBUG = false;
    private static final int ACTIVE_POINTER_ID_NONE = -1;
    static final int DIRECTION_FLAG_COUNT = 8;
    private static final int ACTION_MODE_IDLE_MASK = (1 << DIRECTION_FLAG_COUNT) - 1;
    static final int ACTION_MODE_SWIPE_MASK = ACTION_MODE_IDLE_MASK << DIRECTION_FLAG_COUNT;
    static final int ACTION_MODE_DRAG_MASK = ACTION_MODE_SWIPE_MASK << DIRECTION_FLAG_COUNT;
    private static final int PIXELS_PER_SECOND = 1000;
    final List<View> mPendingCleanup = new ArrayList<>();
    private final float[] mTmpPosition = new float[2];
    @SuppressWarnings("WeakerAccess")
    ViewHolder mSelected = null;
    float mInitialTouchX;
    float mInitialTouchY;
    private float mSwipeEscapeVelocity;
    private float mMaxSwipeVelocity;
    float mDx;
    float mDy;
    private float mSelectedStartX;
    private float mSelectedStartY;
    @SuppressWarnings("WeakerAccess")
    int mActivePointerId = ACTIVE_POINTER_ID_NONE;
    @NonNull
    Callback mCallback;
    private int mActionState = ACTION_STATE_IDLE;
    @SuppressWarnings("WeakerAccess")
    int mSelectedFlags;
    @VisibleForTesting
    List<RecoverAnimation> mRecoverAnimations = new ArrayList<>();
    private int mSlop;
    RecyclerView mRecyclerView;
    VelocityTracker mVelocityTracker;
    private List<ViewHolder> mSwapTargets;
    private List<Integer> mDistances;
    private RecyclerView.ChildDrawingOrderCallback mChildDrawingOrderCallback = null;
    @SuppressWarnings("WeakerAccess")
    View mOverdrawChild = null;
    @SuppressWarnings("WeakerAccess")
    int mOverdrawChildPosition = -1;
    @SuppressWarnings("WeakerAccess")
    GestureDetectorCompat mGestureDetector;
    private ItemTouchHelperGestureListener mItemTouchHelperGestureListener;
    private Rect mTmpRect;
    private long mDragScrollStartTimeInMs;
    private int mTouchSlop = 0;
    private int mPagingTouchSlop = 0;
    private boolean mUsePagingTouchSlopForStylus = false;
    private String mStartDraggingText = null;
    private String mMoveDraggingText = null;
    private String mEndDraggingText = null;
    private final int CALLEDBY_ONDRAWOVER = 1;
    private final int CALLEDBY_ONDRAW = 2;
    private final int CALLEDBY_SELECT = 3;

    @SuppressWarnings("WeakerAccess")
    final Runnable mScrollRunnable = new Runnable() {
        @Override
        public void run() {
            if (mSelected != null && scrollIfNecessary()) {
                if (mSelected != null) {
                    moveIfNecessary(mSelected);
                }
                mRecyclerView.removeCallbacks(mScrollRunnable);
                ViewCompat.postOnAnimation(mRecyclerView, this);
            }
        }
    };

    private final OnItemTouchListener mOnItemTouchListener = new OnItemTouchListener() {
        @Override
        public boolean onInterceptTouchEvent(@NonNull RecyclerView recyclerView, @NonNull MotionEvent event) {
            mGestureDetector.onTouchEvent(event);
            if (DEBUG) {
                Log.d(TAG, "intercept: x:" + event.getX() + ",y:" + event.getY() + ", " + event);
            }
            final int action = event.getActionMasked();
            if (action == MotionEvent.ACTION_DOWN) {
                mActivePointerId = event.getPointerId(0);
                mInitialTouchX = event.getX();
                Log.i(TAG, "onInterceptTouchEvent: #1 set mInitialTouchX = " + mInitialTouchX);
                mInitialTouchY = event.getY();
                if (mUsePagingTouchSlopForStylus) {
                    if (Build.VERSION.SDK_INT >= 18 && event.isFromSource(InputDeviceCompat.SOURCE_STYLUS)) {
                        mSlop = mPagingTouchSlop;
                    } else {
                        mSlop = mTouchSlop;
                    }
                }
                obtainVelocityTracker();
                if (mSelected == null) {
                    final RecoverAnimation animation = findAnimation(event);
                    if (animation != null) {
                        Log.i(TAG, "onInterceptTouchEvent: #2 mInitialTouchX = " + mInitialTouchX + " animation.mX = " + animation.mX);
                        mInitialTouchX -= animation.mX;
                        Log.i(TAG, "onInterceptTouchEvent: #2 set mInitialTouchX = " + mInitialTouchX);
                        mInitialTouchY -= animation.mY;
                        endRecoverAnimation(animation.mViewHolder, true);
                        if (mPendingCleanup.remove(animation.mViewHolder.itemView)) {
                            mCallback.clearView(mRecyclerView, animation.mViewHolder);
                        }
                        select(animation.mViewHolder, animation.mActionState);
                        updateDxDy(event, mSelectedFlags, 0);
                    }
                }
            } else if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
                mActivePointerId = ACTIVE_POINTER_ID_NONE;
                select(null, ACTION_STATE_IDLE);
            } else if (mActivePointerId != ACTIVE_POINTER_ID_NONE) {
                // in a non scroll orientation, if distance change is above threshold, we
                // can select the item
                final int index = event.findPointerIndex(mActivePointerId);
                if (DEBUG) {
                    Log.d(TAG, "pointer index " + index);
                }
                if (index >= 0) {
                    checkSelectForSwipe(action, event, index);
                }
            }
            if (mVelocityTracker != null) {
                mVelocityTracker.addMovement(event);
            }
            return mSelected != null;
        }

        @Override
        public void onTouchEvent(@NonNull RecyclerView recyclerView, @NonNull MotionEvent event) {
            mGestureDetector.onTouchEvent(event);
            if (DEBUG) {
                Log.d(TAG, "on touch: x:" + mInitialTouchX + ",y:" + mInitialTouchY + ", :" + event);
            }
            if (mVelocityTracker != null) {
                mVelocityTracker.addMovement(event);
            }
            if (mActivePointerId == ACTIVE_POINTER_ID_NONE) {
                return;
            }
            final int action = event.getActionMasked();
            final int activePointerIndex = event.findPointerIndex(mActivePointerId);
            if (activePointerIndex >= 0) {
                checkSelectForSwipe(action, event, activePointerIndex);
            }
            ViewHolder viewHolder = mSelected;
            if (viewHolder == null) {
                return;
            }
            switch (action) {
                case MotionEvent.ACTION_MOVE: {
                    if (activePointerIndex >= 0) {
                        updateDxDy(event, mSelectedFlags, activePointerIndex);
                        moveIfNecessary(viewHolder);
                        mRecyclerView.removeCallbacks(mScrollRunnable);
                        mScrollRunnable.run();
                        mRecyclerView.invalidate();
                    }
                    break;
                }
                case MotionEvent.ACTION_CANCEL:
                    if (mVelocityTracker != null) {
                        mVelocityTracker.clear();
                    }
                case MotionEvent.ACTION_UP:
                    select(null, ACTION_STATE_IDLE);
                    mActivePointerId = ACTIVE_POINTER_ID_NONE;
                    break;
                case MotionEvent.ACTION_POINTER_UP: {
                    final int pointerIndex = event.getActionIndex();
                    final int pointerId = event.getPointerId(pointerIndex);
                    if (pointerId == mActivePointerId) {
                        final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                        mActivePointerId = event.getPointerId(newPointerIndex);
                        updateDxDy(event, mSelectedFlags, pointerIndex);
                    }
                    break;
                }
            }
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
            if (!disallowIntercept) {
                return;
            }
            select(null, ACTION_STATE_IDLE);
        }
    };

    public ItemTouchHelper(@NonNull Callback callback) {
        mCallback = callback;
    }

    public void setStartDraggingText(String text) {
        mStartDraggingText = text;
    }

    public void setMoveDraggingText(String text) {
        mMoveDraggingText = text;
    }

    public void setEndDraggingText(String text) {
        mEndDraggingText = text;
    }

    private static boolean hitTest(View child, float x, float y, float left, float top) {
        return x >= left && x <= left + child.getWidth() && y >= top && y <= top + child.getHeight();
    }

    public void attachToRecyclerView(@Nullable RecyclerView recyclerView) {
        if (mRecyclerView == recyclerView) {
            return;
        }
        if (mRecyclerView != null) {
            destroyCallbacks();
        }
        mRecyclerView = recyclerView;
        if (recyclerView != null) {
            final Resources resources = recyclerView.getResources();
            mSwipeEscapeVelocity = resources.getDimension(R.dimen.item_touch_helper_swipe_escape_velocity);
            mMaxSwipeVelocity = resources.getDimension(R.dimen.item_touch_helper_swipe_escape_max_velocity);
            setupCallbacks();
        }
    }

    private void setupCallbacks() {
        ViewConfiguration vc = ViewConfiguration.get(mRecyclerView.getContext());
        mSlop = vc.getScaledTouchSlop();
        mTouchSlop = vc.getScaledTouchSlop();
        mPagingTouchSlop = vc.getScaledPagingTouchSlop();
        mRecyclerView.addItemDecoration(this);
        mRecyclerView.addOnItemTouchListener(mOnItemTouchListener);
        mRecyclerView.addOnChildAttachStateChangeListener(this);
        startGestureDetection();
    }

    private void destroyCallbacks() {
        mRecyclerView.removeItemDecoration(this);
        mRecyclerView.removeOnItemTouchListener(mOnItemTouchListener);
        mRecyclerView.removeOnChildAttachStateChangeListener(this);
        final int recoverAnimSize = mRecoverAnimations.size();
        for (int i = recoverAnimSize - 1; i >= 0; i--) {
            final RecoverAnimation recoverAnimation = mRecoverAnimations.get(0);
            recoverAnimation.cancel();
            mCallback.clearView(mRecyclerView, recoverAnimation.mViewHolder);
        }
        mRecoverAnimations.clear();
        mOverdrawChild = null;
        mOverdrawChildPosition = -1;
        releaseVelocityTracker();
        stopGestureDetection();
    }

    private void startGestureDetection() {
        mItemTouchHelperGestureListener = new ItemTouchHelperGestureListener();
        mGestureDetector = new GestureDetectorCompat(mRecyclerView.getContext(), mItemTouchHelperGestureListener);
    }

    private void stopGestureDetection() {
        if (mItemTouchHelperGestureListener != null) {
            mItemTouchHelperGestureListener.doNotReactToLongPress();
            mItemTouchHelperGestureListener = null;
        }
        if (mGestureDetector != null) {
            mGestureDetector = null;
        }
    }

    private void getSelectedDxDy(float[] outPosition, int calledBy) {
        if ((mSelectedFlags & (LEFT | RIGHT)) != 0) {
            outPosition[0] = mSelectedStartX + mDx - mSelected.itemView.getLeft();
            Log.i(TAG, "getSelectedDxDy: #1 calledBy = " + calledBy + " outPosition[0] = " + outPosition[0] + ", mSelectedStartX = " + mSelectedStartX + ", mDx = " + mDx + ", mSelected.itemView.getLeft() = " + mSelected.itemView.getLeft());
        } else {
            outPosition[0] = mSelected.itemView.getTranslationX();
            Log.i(TAG, "getSelectedDxDy: #2 calledBy = " + calledBy + " outPosition[0] = " + mSelected.itemView.getTranslationX());
        }
        if ((mSelectedFlags & (UP | DOWN)) != 0) {
            outPosition[1] = mSelectedStartY + mDy - mSelected.itemView.getTop();
        } else {
            outPosition[1] = mSelected.itemView.getTranslationY();
        }
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        float dx = 0, dy = 0;
        if (mSelected != null) {
            getSelectedDxDy(mTmpPosition, CALLEDBY_ONDRAWOVER);
            dx = mTmpPosition[0];
            dy = mTmpPosition[1];
        }
        mCallback.onDrawOver(c, parent, mSelected, mRecoverAnimations, mActionState, dx, dy);
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        mOverdrawChildPosition = -1;
        float dx = 0, dy = 0;
        if (mSelected != null) {
            getSelectedDxDy(mTmpPosition, CALLEDBY_ONDRAW);
            dx = mTmpPosition[0];
            dy = mTmpPosition[1];
        }
        mCallback.onDraw(c, parent, mSelected, mRecoverAnimations, mActionState, dx, dy);
    }

    @SuppressWarnings("WeakerAccess")
    void select(@Nullable ViewHolder selected, int actionState) {
        if (selected == mSelected && actionState == mActionState) {
            return;
        }
        mDragScrollStartTimeInMs = Long.MIN_VALUE;
        final int prevActionState = mActionState;
        endRecoverAnimation(selected, true);
        mActionState = actionState;
        if (actionState == ACTION_STATE_DRAG) {
            if (selected == null) {
                throw new IllegalArgumentException("Must pass a ViewHolder when dragging");
            }

            mOverdrawChild = selected.itemView;
            addChildDrawingOrderCallback();
        }
        int actionStateMask = (1 << (DIRECTION_FLAG_COUNT + DIRECTION_FLAG_COUNT * actionState)) - 1;
        boolean preventLayout = false;

        if (mSelected != null) {
            final ViewHolder prevSelected = mSelected;
            if (prevSelected.itemView.getParent() != null) {
                final int swipeDir = prevActionState == ACTION_STATE_DRAG ? 0 : swipeIfNecessary(prevSelected);
                releaseVelocityTracker();
                final float targetTranslateX, targetTranslateY;
                int animationType;
                switch (swipeDir) {
                    case LEFT:
                    case RIGHT:
                    case START:
                    case END:
                        targetTranslateY = 0;
                        targetTranslateX = Math.signum(mDx) * mRecyclerView.getWidth();
                        break;
                    case UP:
                    case DOWN:
                        targetTranslateX = 0;
                        targetTranslateY = Math.signum(mDy) * mRecyclerView.getHeight();
                        break;
                    default:
                        targetTranslateX = 0;
                        targetTranslateY = 0;
                }
                if (prevActionState == ACTION_STATE_DRAG) {
                    if (mEndDraggingText != null && !mEndDraggingText.isEmpty()) {
                        mSelected.itemView.announceForAccessibility(mEndDraggingText);
                    } else {
                        mSelected.itemView.announceForAccessibility(mRecyclerView.getContext().getString(R.string.dragndroplist_drag_release, mSelected.getLayoutPosition() + 1));
                    }
                    animationType = ANIMATION_TYPE_DRAG;
                } else if (swipeDir > 0) {
                    animationType = ANIMATION_TYPE_SWIPE_SUCCESS;
                } else {
                    animationType = ANIMATION_TYPE_SWIPE_CANCEL;
                }
                getSelectedDxDy(mTmpPosition, CALLEDBY_SELECT);
                final float currentTranslateX = mTmpPosition[0];
                final float currentTranslateY = mTmpPosition[1];
                final RecoverAnimation rv = new RecoverAnimation(prevSelected, animationType, prevActionState, currentTranslateX, currentTranslateY, targetTranslateX, targetTranslateY) {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        Log.i(TAG, "select: *** Start RecoverAnimation$onAnimationEnd ***");
                        if (this.mOverridden) {
                            Log.i(TAG, "select: *** End RecoverAnimation$onAnimationEnd *** return #1");
                            return;
                        }
                        Log.i(TAG, "select$onAnimationEnd: swipeDir = " + swipeDir);
                        if (swipeDir <= 0) {
                            Log.i(TAG, "select$onAnimationEnd: #2 call mCallback.clearView(mRecyclerView = " + mRecyclerView + ", prevSelected = " + prevSelected + ")");
                            mCallback.clearView(mRecyclerView, prevSelected);
                        } else if (!prevSelected.itemView.isAttachedToWindow()) {
                            Log.i(TAG, "select$onAnimationEnd: #3 call mCallback.clearView(mRecyclerView = " + mRecyclerView + ", prevSelected = " + prevSelected + ")");
                            mCallback.clearView(mRecyclerView, prevSelected);
                        } else {
                            mPendingCleanup.add(prevSelected.itemView);
                            mIsPendingCleanup = true;
                            if (swipeDir > 0) {
                                Log.i(TAG, "select$onAnimationEnd: postDispatchSwipe #4");
                                postDispatchSwipe(this, swipeDir);
                            } else {
                                Log.i(TAG, "select$onAnimationEnd: swipeDir <= 0 #5 do nothing");
                            }
                        }
                        if (mOverdrawChild == prevSelected.itemView) {
                            removeChildDrawingOrderCallbackIfNecessary(prevSelected.itemView);
                        }
                        Log.i(TAG, "select: *** End RecoverAnimation$onAnimationEnd *** #6");
                    }
                };
                final long duration = mCallback.getAnimationDuration(mRecyclerView, animationType, targetTranslateX - currentTranslateX, targetTranslateY - currentTranslateY);
                Log.i(TAG, "select: setDuration = " + duration);
                rv.setDuration(duration);
                mRecoverAnimations.add(rv);
                rv.start();
                preventLayout = true;
            } else {
                removeChildDrawingOrderCallbackIfNecessary(prevSelected.itemView);
                mCallback.clearView(mRecyclerView, prevSelected);
            }
            mSelected = null;
        }
        if (selected != null) {
            mSelectedFlags = (mCallback.getAbsoluteMovementFlags(mRecyclerView, selected) & actionStateMask) >> (mActionState * DIRECTION_FLAG_COUNT);
            mSelectedStartX = selected.itemView.getLeft();
            mSelectedStartY = selected.itemView.getTop();
            mSelected = selected;

            if (actionState == ACTION_STATE_DRAG) {
                mSelected.itemView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            }
        }
        final ViewParent rvParent = mRecyclerView.getParent();
        if (rvParent != null) {
            rvParent.requestDisallowInterceptTouchEvent(mSelected != null);
        }
        if (!preventLayout) {
            mRecyclerView.getLayoutManager().requestSimpleAnimationsInNextLayout();
        }
        mCallback.onSelectedChanged(mSelected, mActionState);
        if (actionState == ACTION_STATE_DRAG) {
            mSelected.itemView.performHapticFeedback(0);
            if (mStartDraggingText != null && !mStartDraggingText.isEmpty()) {
                mSelected.itemView.announceForAccessibility(mStartDraggingText);
            } else {
                mSelected.itemView.announceForAccessibility(mRecyclerView.getContext().getString(R.string.dragndroplist_drag_start, mSelected.getLayoutPosition() + 1));
            }
        }
        mRecyclerView.invalidate();
    }

    @SuppressWarnings("WeakerAccess")
    void postDispatchSwipe(final RecoverAnimation anim, final int swipeDir) {
        mRecyclerView.post(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "postDispatchSwipe$run: mRecyclerView = " + mRecyclerView + ", isAttachedToWindow = " + mRecyclerView.isAttachedToWindow() + ", !anim.mOverridden = " + (!anim.mOverridden) + ", anim.mViewHolder.getAdapterPosition() = " + anim.mViewHolder.getAdapterPosition());
                if (mRecyclerView != null && mRecyclerView.isAttachedToWindow() && !anim.mOverridden && anim.mViewHolder.getAbsoluteAdapterPosition() != RecyclerView.NO_POSITION) {
                    final RecyclerView.ItemAnimator animator = mRecyclerView.getItemAnimator();
                    if ((animator == null || !animator.isRunning(null)) && !hasRunningRecoverAnim()) {
                        Log.i(TAG, "postDispatchSwipe$run: mCallback.onSwiped anim.mViewHolder = " + anim.mViewHolder + ", anim.mViewHolder.itemView = " + anim.mViewHolder.itemView + " swipeDir=" + swipeDir);
                        mCallback.onSwiped(anim.mViewHolder, swipeDir);
                        endRecoverAnimation(anim.mViewHolder, false);
                    } else {
                        mRecyclerView.post(this);
                    }
                } else {
                    Log.i(TAG, "Failed to call mCallback.onSwiped()!, call seslOnSwipeFailed, flag = 0x" + Integer.toHexString(anim.mViewHolder.getFlags()));
                    mCallback.seslOnSwipeFailed(anim.mViewHolder, swipeDir);
                    endRecoverAnimation(anim.mViewHolder, false);
                }
            }
        });
    }

    @SuppressWarnings("WeakerAccess")
    boolean hasRunningRecoverAnim() {
        final int size = mRecoverAnimations.size();
        for (int i = 0; i < size; i++) {
            if (!mRecoverAnimations.get(i).mEnded) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("WeakerAccess")
    boolean scrollIfNecessary() {
        if (mSelected == null) {
            mDragScrollStartTimeInMs = Long.MIN_VALUE;
            return false;
        }
        final long now = System.currentTimeMillis();
        final long scrollDuration = mDragScrollStartTimeInMs == Long.MIN_VALUE ? 0 : now - mDragScrollStartTimeInMs;
        RecyclerView.LayoutManager lm = mRecyclerView.getLayoutManager();
        if (mTmpRect == null) {
            mTmpRect = new Rect();
        }
        int scrollX = 0;
        int scrollY = 0;
        lm.calculateItemDecorationsForChild(mSelected.itemView, mTmpRect);
        if (lm.canScrollHorizontally()) {
            int curX = (int) (mSelectedStartX + mDx);
            final int leftDiff = curX - mTmpRect.left - mRecyclerView.getPaddingLeft();
            if (mDx < 0 && leftDiff < 0) {
                scrollX = leftDiff;
            } else if (mDx > 0) {
                final int rightDiff = curX + mSelected.itemView.getWidth() + mTmpRect.right - (mRecyclerView.getWidth() - mRecyclerView.getPaddingRight());
                if (rightDiff > 0) {
                    scrollX = rightDiff;
                }
            }
        }
        if (lm.canScrollVertically()) {
            int curY = (int) (mSelectedStartY + mDy);
            final int topDiff = curY - mTmpRect.top - mRecyclerView.getPaddingTop();
            if (mDy < 0 && topDiff < 0) {
                scrollY = topDiff;
            } else if (mDy > 0) {
                final int bottomDiff = curY + mSelected.itemView.getHeight() + mTmpRect.bottom - (mRecyclerView.getHeight() - mRecyclerView.getPaddingBottom());
                if (bottomDiff > 0) {
                    scrollY = bottomDiff;
                }
            }
        }
        if (scrollX != 0) {
            scrollX = mCallback.interpolateOutOfBoundsScroll(mRecyclerView, mSelected.itemView.getWidth(), scrollX, mRecyclerView.getWidth(), scrollDuration);
        }
        if (scrollY != 0) {
            scrollY = mCallback.interpolateOutOfBoundsScroll(mRecyclerView, mSelected.itemView.getHeight(), scrollY, mRecyclerView.getHeight(), scrollDuration);
        }
        if (scrollX != 0 || scrollY != 0) {
            if (mDragScrollStartTimeInMs == Long.MIN_VALUE) {
                mDragScrollStartTimeInMs = now;
            }
            mRecyclerView.scrollBy(scrollX, scrollY);
            return true;
        }
        mDragScrollStartTimeInMs = Long.MIN_VALUE;
        return false;
    }

    private List<ViewHolder> findSwapTargets(ViewHolder viewHolder) {
        if (mSwapTargets == null) {
            mSwapTargets = new ArrayList<>();
            mDistances = new ArrayList<>();
        } else {
            mSwapTargets.clear();
            mDistances.clear();
        }
        final int margin = mCallback.getBoundingBoxMargin();
        final int left = Math.round(mSelectedStartX + mDx) - margin;
        final int top = Math.round(mSelectedStartY + mDy) - margin;
        final int right = left + viewHolder.itemView.getWidth() + 2 * margin;
        final int bottom = top + viewHolder.itemView.getHeight() + 2 * margin;
        final int centerX = (left + right) / 2;
        final int centerY = (top + bottom) / 2;
        final RecyclerView.LayoutManager lm = mRecyclerView.getLayoutManager();
        final int childCount = lm.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View other = lm.getChildAt(i);
            if (other == viewHolder.itemView) {
                continue;
            }
            if (other.getBottom() < top || other.getTop() > bottom || other.getRight() < left || other.getLeft() > right) {
                continue;
            }
            final ViewHolder otherVh = mRecyclerView.getChildViewHolder(other);
            if (mCallback.canDropOver(mRecyclerView, mSelected, otherVh)) {
                final int dx = Math.abs(centerX - (other.getLeft() + other.getRight()) / 2);
                final int dy = Math.abs(centerY - (other.getTop() + other.getBottom()) / 2);
                final int dist = dx * dx + dy * dy;

                int pos = 0;
                final int cnt = mSwapTargets.size();
                for (int j = 0; j < cnt; j++) {
                    if (dist > mDistances.get(j)) {
                        pos++;
                    } else {
                        break;
                    }
                }
                mSwapTargets.add(pos, otherVh);
                mDistances.add(pos, dist);
            }
        }
        return mSwapTargets;
    }

    @SuppressWarnings("WeakerAccess")
    void moveIfNecessary(ViewHolder viewHolder) {
        if (mRecyclerView.isLayoutRequested()) {
            return;
        }
        if (mActionState != ACTION_STATE_DRAG) {
            return;
        }

        final float threshold = mCallback.getMoveThreshold(viewHolder);
        final int x = (int) (mSelectedStartX + mDx);
        final int y = (int) (mSelectedStartY + mDy);
        if (Math.abs(y - viewHolder.itemView.getTop()) < viewHolder.itemView.getHeight() * threshold && Math.abs(x - viewHolder.itemView.getLeft()) < viewHolder.itemView.getWidth() * threshold) {
            return;
        }
        List<ViewHolder> swapTargets = findSwapTargets(viewHolder);
        if (swapTargets.size() == 0) {
            return;
        }
        ViewHolder target = mCallback.chooseDropTarget(viewHolder, swapTargets, x, y);
        if (target == null) {
            mSwapTargets.clear();
            mDistances.clear();
            return;
        }
        final int toPosition = target.getAbsoluteAdapterPosition();
        final int fromPosition = viewHolder.getAbsoluteAdapterPosition();
        if (mCallback.onMove(mRecyclerView, viewHolder, target)) {
            mCallback.onMoved(mRecyclerView, viewHolder, fromPosition, target, toPosition, x, y);
            if (mMoveDraggingText != null && !mMoveDraggingText.isEmpty()) {
                mSelected.itemView.announceForAccessibility(mMoveDraggingText);
            } else {
                mSelected.itemView.announceForAccessibility(mRecyclerView.getContext().getString(R.string.dragndroplist_drag_move, toPosition + 1));
            }
        }
    }

    @Override
    public void onChildViewAttachedToWindow(@NonNull View view) {
    }

    @Override
    public void onChildViewDetachedFromWindow(@NonNull View view) {
        removeChildDrawingOrderCallbackIfNecessary(view);
        final ViewHolder holder = mRecyclerView.getChildViewHolder(view);
        if (holder == null) {
            return;
        }
        if (mSelected != null && holder == mSelected) {
            select(null, ACTION_STATE_IDLE);
        } else {
            endRecoverAnimation(holder, false);
            if (mPendingCleanup.remove(holder.itemView)) {
                mCallback.clearView(mRecyclerView, holder);
            }
        }
    }

    @SuppressWarnings("WeakerAccess")
    void endRecoverAnimation(ViewHolder viewHolder, boolean override) {
        final int recoverAnimSize = mRecoverAnimations.size();
        for (int i = recoverAnimSize - 1; i >= 0; i--) {
            final RecoverAnimation anim = mRecoverAnimations.get(i);
            if (anim.mViewHolder == viewHolder) {
                anim.mOverridden |= override;
                if (!anim.mEnded) {
                    anim.cancel();
                }
                mRecoverAnimations.remove(i);
                return;
            }
        }
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.setEmpty();
    }

    @SuppressWarnings("WeakerAccess")
    void obtainVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
        }
        mVelocityTracker = VelocityTracker.obtain();
    }

    private void releaseVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    private ViewHolder findSwipedView(MotionEvent motionEvent) {
        final RecyclerView.LayoutManager lm = mRecyclerView.getLayoutManager();
        if (mActivePointerId == ACTIVE_POINTER_ID_NONE) {
            return null;
        }
        final int pointerIndex = motionEvent.findPointerIndex(mActivePointerId);
        final float dx = motionEvent.getX(pointerIndex) - mInitialTouchX;
        final float dy = motionEvent.getY(pointerIndex) - mInitialTouchY;
        final float absDx = Math.abs(dx);
        final float absDy = Math.abs(dy);

        if (absDx < mSlop && absDy < mSlop) {
            return null;
        }
        if (absDx > absDy && lm.canScrollHorizontally()) {
            return null;
        } else if (absDy > absDx && lm.canScrollVertically()) {
            return null;
        }
        View child = findChildView(motionEvent);
        if (child == null) {
            return null;
        }
        return mRecyclerView.getChildViewHolder(child);
    }

    @SuppressWarnings("WeakerAccess")
    void checkSelectForSwipe(int action, MotionEvent motionEvent, int pointerIndex) {
        if (mSelected != null || action != MotionEvent.ACTION_MOVE || mActionState == ACTION_STATE_DRAG || !mCallback.isItemViewSwipeEnabled()) {
            return;
        }
        if (mRecyclerView.getScrollState() == RecyclerView.SCROLL_STATE_DRAGGING) {
            return;
        }
        final ViewHolder vh = findSwipedView(motionEvent);
        if (vh == null) {
            return;
        }
        final int movementFlags = mCallback.getAbsoluteMovementFlags(mRecyclerView, vh);

        final int swipeFlags = (movementFlags & ACTION_MODE_SWIPE_MASK) >> (DIRECTION_FLAG_COUNT * ACTION_STATE_SWIPE);

        if (swipeFlags == 0) {
            return;
        }

        final float x = motionEvent.getX(pointerIndex);
        final float y = motionEvent.getY(pointerIndex);

        final float dx = x - mInitialTouchX;
        final float dy = y - mInitialTouchY;
        final float absDx = Math.abs(dx);
        final float absDy = Math.abs(dy);

        if (absDx < mSlop && absDy < mSlop) {
            return;
        }
        if (absDx > absDy) {
            if (dx < 0 && (swipeFlags & LEFT) == 0) {
                return;
            }
            if (dx > 0 && (swipeFlags & RIGHT) == 0) {
                return;
            }
        } else {
            if (dy < 0 && (swipeFlags & UP) == 0) {
                return;
            }
            if (dy > 0 && (swipeFlags & DOWN) == 0) {
                return;
            }
        }
        mDx = mDy = 0f;
        mActivePointerId = motionEvent.getPointerId(0);
        select(vh, ACTION_STATE_SWIPE);
    }

    @SuppressWarnings("WeakerAccess")
    View findChildView(MotionEvent event) {
        final float x = event.getX();
        final float y = event.getY();
        if (mSelected != null) {
            final View selectedView = mSelected.itemView;
            if (hitTest(selectedView, x, y, mSelectedStartX + mDx, mSelectedStartY + mDy)) {
                return selectedView;
            }
        }
        for (int i = mRecoverAnimations.size() - 1; i >= 0; i--) {
            final RecoverAnimation anim = mRecoverAnimations.get(i);
            final View view = anim.mViewHolder.itemView;
            if (hitTest(view, x, y, anim.mX, anim.mY)) {
                return view;
            }
        }
        return mRecyclerView.findChildViewUnder(x, y);
    }

    public void startDrag(@NonNull ViewHolder viewHolder) {
        if (!mCallback.hasDragFlag(mRecyclerView, viewHolder)) {
            Log.e(TAG, "Start drag has been called but dragging is not enabled");
            viewHolder.itemView.announceForAccessibility(mRecyclerView.getContext().getString(R.string.dragndroplist_item_cannot_be_dragged, viewHolder.getLayoutPosition() + 1));
            return;
        }
        if (viewHolder.itemView.getParent() != mRecyclerView) {
            Log.e(TAG, "Start drag has been called with a view holder which is not a child of the RecyclerView which is controlled by this ItemTouchHelper.");
            return;
        }
        obtainVelocityTracker();
        mDx = mDy = 0f;
        select(viewHolder, ACTION_STATE_DRAG);
    }

    public void startSwipe(@NonNull ViewHolder viewHolder) {
        if (!mCallback.hasSwipeFlag(mRecyclerView, viewHolder)) {
            Log.e(TAG, "Start swipe has been called but swiping is not enabled");
            return;
        }
        if (viewHolder.itemView.getParent() != mRecyclerView) {
            Log.e(TAG, "Start swipe has been called with a view holder which is not a child of the RecyclerView controlled by this ItemTouchHelper.");
            return;
        }
        obtainVelocityTracker();
        mDx = mDy = 0f;
        select(viewHolder, ACTION_STATE_SWIPE);
    }

    @SuppressWarnings("WeakerAccess")
    RecoverAnimation findAnimation(MotionEvent event) {
        if (mRecoverAnimations.isEmpty()) {
            return null;
        }
        View target = findChildView(event);
        for (int i = mRecoverAnimations.size() - 1; i >= 0; i--) {
            final RecoverAnimation anim = mRecoverAnimations.get(i);
            if (anim.mViewHolder.itemView == target) {
                return anim;
            }
        }
        return null;
    }

    @SuppressWarnings("WeakerAccess")
    void updateDxDy(MotionEvent ev, int directionFlags, int pointerIndex) {
        final float x = ev.getX(pointerIndex);
        final float y = ev.getY(pointerIndex);

        mDx = x - mInitialTouchX;
        Log.i(TAG, "updateDxDy: mDx = " + mDx + " = (x = " + x + " - mInitialTouchX = " + mInitialTouchX + ")");
        mDy = y - mInitialTouchY;
        if ((directionFlags & LEFT) == 0) {
            mDx = Math.max(0, mDx);
            Log.i(TAG, "updateDxDy: direction LEFT mDx = " + mDx);
        }
        if ((directionFlags & RIGHT) == 0) {
            mDx = Math.min(0, mDx);
            Log.i(TAG, "updateDxDy: direction RIGHT mDx = " + mDx);
        }
        if ((directionFlags & UP) == 0) {
            mDy = Math.max(0, mDy);
        }
        if ((directionFlags & DOWN) == 0) {
            mDy = Math.min(0, mDy);
        }
    }

    private int swipeIfNecessary(ViewHolder viewHolder) {
        if (mActionState == ACTION_STATE_DRAG) {
            return 0;
        }
        final int originalMovementFlags = mCallback.getMovementFlags(mRecyclerView, viewHolder);
        final int absoluteMovementFlags = mCallback.convertToAbsoluteDirection(originalMovementFlags, ViewCompat.getLayoutDirection(mRecyclerView));
        final int flags = (absoluteMovementFlags & ACTION_MODE_SWIPE_MASK) >> (ACTION_STATE_SWIPE * DIRECTION_FLAG_COUNT);
        if (flags == 0) {
            return 0;
        }
        final int originalFlags = (originalMovementFlags & ACTION_MODE_SWIPE_MASK) >> (ACTION_STATE_SWIPE * DIRECTION_FLAG_COUNT);
        int swipeDir;
        if (Math.abs(mDx) > Math.abs(mDy)) {
            if ((swipeDir = checkHorizontalSwipe(viewHolder, flags)) > 0) {
                if ((originalFlags & swipeDir) == 0) {
                    return Callback.convertToRelativeDirection(swipeDir, ViewCompat.getLayoutDirection(mRecyclerView));
                }
                return swipeDir;
            }
            if ((swipeDir = checkVerticalSwipe(viewHolder, flags)) > 0) {
                return swipeDir;
            }
        } else {
            if ((swipeDir = checkVerticalSwipe(viewHolder, flags)) > 0) {
                return swipeDir;
            }
            if ((swipeDir = checkHorizontalSwipe(viewHolder, flags)) > 0) {
                if ((originalFlags & swipeDir) == 0) {
                    return Callback.convertToRelativeDirection(swipeDir, ViewCompat.getLayoutDirection(mRecyclerView));
                }
                return swipeDir;
            }
        }
        return 0;
    }

    private int checkHorizontalSwipe(ViewHolder viewHolder, int flags) {
        if ((flags & (LEFT | RIGHT)) != 0) {
            final int dirFlag = mDx > 0 ? RIGHT : LEFT;
            if (mVelocityTracker != null && mActivePointerId > -1) {
                mVelocityTracker.computeCurrentVelocity(PIXELS_PER_SECOND, mCallback.getSwipeVelocityThreshold(mMaxSwipeVelocity));
                final float xVelocity = mVelocityTracker.getXVelocity(mActivePointerId);
                final float yVelocity = mVelocityTracker.getYVelocity(mActivePointerId);
                final int velDirFlag = xVelocity > 0f ? RIGHT : LEFT;
                final float absXVelocity = Math.abs(xVelocity);
                if ((velDirFlag & flags) != 0 && dirFlag == velDirFlag && absXVelocity >= mCallback.getSwipeEscapeVelocity(mSwipeEscapeVelocity) && absXVelocity > Math.abs(yVelocity)) {
                    return velDirFlag;
                }
            }

            final float threshold = mRecyclerView.getWidth() * mCallback.getSwipeThreshold(viewHolder);

            if ((flags & dirFlag) != 0 && Math.abs(mDx) > threshold) {
                return dirFlag;
            }
        }
        return 0;
    }

    private int checkVerticalSwipe(ViewHolder viewHolder, int flags) {
        if ((flags & (UP | DOWN)) != 0) {
            final int dirFlag = mDy > 0 ? DOWN : UP;
            if (mVelocityTracker != null && mActivePointerId > -1) {
                mVelocityTracker.computeCurrentVelocity(PIXELS_PER_SECOND, mCallback.getSwipeVelocityThreshold(mMaxSwipeVelocity));
                final float xVelocity = mVelocityTracker.getXVelocity(mActivePointerId);
                final float yVelocity = mVelocityTracker.getYVelocity(mActivePointerId);
                final int velDirFlag = yVelocity > 0f ? DOWN : UP;
                final float absYVelocity = Math.abs(yVelocity);
                if ((velDirFlag & flags) != 0 && velDirFlag == dirFlag && absYVelocity >= mCallback.getSwipeEscapeVelocity(mSwipeEscapeVelocity) && absYVelocity > Math.abs(xVelocity)) {
                    return velDirFlag;
                }
            }

            final float threshold = mRecyclerView.getHeight() * mCallback
                    .getSwipeThreshold(viewHolder);
            if ((flags & dirFlag) != 0 && Math.abs(mDy) > threshold) {
                return dirFlag;
            }
        }
        return 0;
    }

    private void addChildDrawingOrderCallback() {
        if (Build.VERSION.SDK_INT >= 21) {
            return;
        }
        if (mChildDrawingOrderCallback == null) {
            mChildDrawingOrderCallback = new RecyclerView.ChildDrawingOrderCallback() {
                @Override
                public int onGetChildDrawingOrder(int childCount, int i) {
                    if (mOverdrawChild == null) {
                        return i;
                    }
                    int childPosition = mOverdrawChildPosition;
                    if (childPosition == -1) {
                        childPosition = mRecyclerView.indexOfChild(mOverdrawChild);
                        mOverdrawChildPosition = childPosition;
                    }
                    if (i == childCount - 1) {
                        return childPosition;
                    }
                    return i < childPosition ? i : i + 1;
                }
            };
        }
        mRecyclerView.setChildDrawingOrderCallback(mChildDrawingOrderCallback);
    }

    @SuppressWarnings("WeakerAccess")
    void removeChildDrawingOrderCallbackIfNecessary(View view) {
        if (view == mOverdrawChild) {
            mOverdrawChild = null;
            if (mChildDrawingOrderCallback != null) {
                mRecyclerView.setChildDrawingOrderCallback(null);
            }
        }
    }

    public void seslSetPagingTouchSlopForStylus(boolean use) {
        mUsePagingTouchSlopForStylus = use;
    }

    public boolean seslIsPagingTouchSlopForStylusEnabled() {
        return mUsePagingTouchSlopForStylus;
    }


    public interface ViewDropHandler {
        void prepareForDrop(@NonNull View view, @NonNull View target, int x, int y);
    }

    @SuppressWarnings("UnusedParameters")
    public abstract static class Callback {
        @SuppressWarnings("WeakerAccess")
        public static final int DEFAULT_DRAG_ANIMATION_DURATION = 200;
        @SuppressWarnings("WeakerAccess")
        public static final int DEFAULT_SWIPE_ANIMATION_DURATION = 250;
        static final int RELATIVE_DIR_FLAGS = START | END | ((START | END) << DIRECTION_FLAG_COUNT) | ((START | END) << (2 * DIRECTION_FLAG_COUNT));
        private static final int ABS_HORIZONTAL_DIR_FLAGS = LEFT | RIGHT | ((LEFT | RIGHT) << DIRECTION_FLAG_COUNT) | ((LEFT | RIGHT) << (2 * DIRECTION_FLAG_COUNT));
        private static final long DRAG_SCROLL_ACCELERATION_LIMIT_TIME_MS = 2000;
        private int mCachedMaxScrollSpeed = -1;

        private static final Interpolator sDragScrollInterpolator = new Interpolator() {
            @Override
            public float getInterpolation(float t) {
                return t * t * t * t * t;
            }
        };

        private static final Interpolator sDragViewScrollCapInterpolator = new Interpolator() {
            @Override
            public float getInterpolation(float t) {
                t -= 1.0f;
                return t * t * t * t * t + 1.0f;
            }
        };

        @SuppressWarnings("WeakerAccess")
        @NonNull
        public static ItemTouchUIUtil getDefaultUIUtil() {
            return ItemTouchUIUtilImpl.INSTANCE;
        }

        @SuppressWarnings("WeakerAccess")
        public static int convertToRelativeDirection(int flags, int layoutDirection) {
            int masked = flags & ABS_HORIZONTAL_DIR_FLAGS;
            if (masked == 0) {
                return flags;
            }
            flags &= ~masked;
            if (layoutDirection == ViewCompat.LAYOUT_DIRECTION_LTR) {
                flags |= masked << 2;
                return flags;
            } else {
                flags |= ((masked << 1) & ~ABS_HORIZONTAL_DIR_FLAGS);
                flags |= ((masked << 1) & ABS_HORIZONTAL_DIR_FLAGS) << 2;
            }
            return flags;
        }

        public static int makeMovementFlags(int dragFlags, int swipeFlags) {
            return makeFlag(ACTION_STATE_IDLE, swipeFlags | dragFlags) | makeFlag(ACTION_STATE_SWIPE, swipeFlags) | makeFlag(ACTION_STATE_DRAG, dragFlags);
        }

        @SuppressWarnings("WeakerAccess")
        public static int makeFlag(int actionState, int directions) {
            return directions << (actionState * DIRECTION_FLAG_COUNT);
        }

        public abstract int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull ViewHolder viewHolder);

        @SuppressWarnings("WeakerAccess")
        public int convertToAbsoluteDirection(int flags, int layoutDirection) {
            int masked = flags & RELATIVE_DIR_FLAGS;
            if (masked == 0) {
                return flags;
            }
            flags &= ~masked;
            if (layoutDirection == ViewCompat.LAYOUT_DIRECTION_LTR) {
                flags |= masked >> 2;
                return flags;
            } else {
                flags |= ((masked >> 1) & ~RELATIVE_DIR_FLAGS);
                flags |= ((masked >> 1) & RELATIVE_DIR_FLAGS) >> 2;
            }
            return flags;
        }

        final int getAbsoluteMovementFlags(RecyclerView recyclerView, ViewHolder viewHolder) {
            final int flags = getMovementFlags(recyclerView, viewHolder);
            return convertToAbsoluteDirection(flags, ViewCompat.getLayoutDirection(recyclerView));
        }

        boolean hasDragFlag(RecyclerView recyclerView, ViewHolder viewHolder) {
            final int flags = getAbsoluteMovementFlags(recyclerView, viewHolder);
            return (flags & ACTION_MODE_DRAG_MASK) != 0;
        }

        boolean hasSwipeFlag(RecyclerView recyclerView, ViewHolder viewHolder) {
            final int flags = getAbsoluteMovementFlags(recyclerView, viewHolder);
            return (flags & ACTION_MODE_SWIPE_MASK) != 0;
        }

        @SuppressWarnings("WeakerAccess")
        public boolean canDropOver(@NonNull RecyclerView recyclerView, @NonNull ViewHolder current, @NonNull ViewHolder target) {
            return true;
        }

        public abstract boolean onMove(@NonNull RecyclerView recyclerView, @NonNull ViewHolder viewHolder, @NonNull ViewHolder target);

        public boolean isLongPressDragEnabled() {
            return true;
        }

        public boolean isItemViewSwipeEnabled() {
            return true;
        }

        @SuppressWarnings("WeakerAccess")
        public int getBoundingBoxMargin() {
            return 0;
        }

        @SuppressWarnings("WeakerAccess")
        public float getSwipeThreshold(@NonNull ViewHolder viewHolder) {
            return .5f;
        }

        @SuppressWarnings("WeakerAccess")
        public float getMoveThreshold(@NonNull ViewHolder viewHolder) {
            return .5f;
        }

        @SuppressWarnings("WeakerAccess")
        public float getSwipeEscapeVelocity(float defaultValue) {
            return defaultValue;
        }

        @SuppressWarnings("WeakerAccess")
        public float getSwipeVelocityThreshold(float defaultValue) {
            return defaultValue;
        }

        @SuppressWarnings("WeakerAccess")
        public ViewHolder chooseDropTarget(@NonNull ViewHolder selected, @NonNull List<ViewHolder> dropTargets, int curX, int curY) {
            int right = curX + selected.itemView.getWidth();
            int bottom = curY + selected.itemView.getHeight();
            ViewHolder winner = null;
            int winnerScore = -1;
            final int dx = curX - selected.itemView.getLeft();
            final int dy = curY - selected.itemView.getTop();
            final int targetsSize = dropTargets.size();
            for (int i = 0; i < targetsSize; i++) {
                final ViewHolder target = dropTargets.get(i);
                if (dx > 0) {
                    int diff = target.itemView.getRight() - right;
                    if (diff < 0 && target.itemView.getRight() > selected.itemView.getRight()) {
                        final int score = Math.abs(diff);
                        if (score > winnerScore) {
                            winnerScore = score;
                            winner = target;
                        }
                    }
                }
                if (dx < 0) {
                    int diff = target.itemView.getLeft() - curX;
                    if (diff > 0 && target.itemView.getLeft() < selected.itemView.getLeft()) {
                        final int score = Math.abs(diff);
                        if (score > winnerScore) {
                            winnerScore = score;
                            winner = target;
                        }
                    }
                }
                if (dy < 0) {
                    int diff = target.itemView.getTop() - curY;
                    if (diff > 0 && target.itemView.getTop() < selected.itemView.getTop()) {
                        final int score = Math.abs(diff);
                        if (score > winnerScore) {
                            winnerScore = score;
                            winner = target;
                        }
                    }
                }

                if (dy > 0) {
                    int diff = target.itemView.getBottom() - bottom;
                    if (diff < 0 && target.itemView.getBottom() > selected.itemView.getBottom()) {
                        final int score = Math.abs(diff);
                        if (score > winnerScore) {
                            winnerScore = score;
                            winner = target;
                        }
                    }
                }
            }
            return winner;
        }

        public abstract void onSwiped(@NonNull ViewHolder viewHolder, int direction);

        public void seslOnSwipeFailed(@NonNull final ViewHolder viewHolder, int swipeDir) {
        }

        public void onSelectedChanged(@Nullable ViewHolder viewHolder, int actionState) {
            if (viewHolder != null) {
                ItemTouchUIUtilImpl.INSTANCE.onSelected(viewHolder.itemView);
            }
        }

        private int getMaxDragScroll(RecyclerView recyclerView) {
            if (mCachedMaxScrollSpeed == -1) {
                mCachedMaxScrollSpeed = recyclerView.getResources().getDimensionPixelSize(R.dimen.item_touch_helper_max_drag_scroll_per_frame);
            }
            return mCachedMaxScrollSpeed;
        }

        public void onMoved(@NonNull final RecyclerView recyclerView, @NonNull final ViewHolder viewHolder, int fromPos, @NonNull final ViewHolder target, int toPos, int x, int y) {
            final RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
            if (layoutManager instanceof ViewDropHandler) {
                ((ViewDropHandler) layoutManager).prepareForDrop(viewHolder.itemView, target.itemView, x, y);
                return;
            }

            if (layoutManager.canScrollHorizontally()) {
                final int minLeft = layoutManager.getDecoratedLeft(target.itemView);
                if (minLeft <= recyclerView.getPaddingLeft()) {
                    recyclerView.scrollToPosition(toPos);
                }
                final int maxRight = layoutManager.getDecoratedRight(target.itemView);
                if (maxRight >= recyclerView.getWidth() - recyclerView.getPaddingRight()) {
                    recyclerView.scrollToPosition(toPos);
                }
            }

            if (layoutManager.canScrollVertically()) {
                final int minTop = layoutManager.getDecoratedTop(target.itemView);
                if (minTop <= recyclerView.getPaddingTop()) {
                    recyclerView.scrollToPosition(toPos);
                }
                final int maxBottom = layoutManager.getDecoratedBottom(target.itemView);
                if (maxBottom >= recyclerView.getHeight() - recyclerView.getPaddingBottom()) {
                    recyclerView.scrollToPosition(toPos);
                }
            }
        }

        void onDraw(Canvas c, RecyclerView parent, ViewHolder selected, List<ItemTouchHelper.RecoverAnimation> recoverAnimationList, int actionState, float dX, float dY) {
            final int recoverAnimSize = recoverAnimationList.size();
            for (int i = 0; i < recoverAnimSize; i++) {
                final ItemTouchHelper.RecoverAnimation anim = recoverAnimationList.get(i);
                anim.update();
                final int count = c.save();
                onChildDraw(c, parent, anim.mViewHolder, anim.mX, anim.mY, anim.mActionState, false);
                c.restoreToCount(count);
            }
            if (selected != null) {
                final int count = c.save();
                onChildDraw(c, parent, selected, dX, dY, actionState, true);
                c.restoreToCount(count);
            }
        }

        void onDrawOver(Canvas c, RecyclerView parent, ViewHolder selected, List<ItemTouchHelper.RecoverAnimation> recoverAnimationList, int actionState, float dX, float dY) {
            final int recoverAnimSize = recoverAnimationList.size();
            for (int i = 0; i < recoverAnimSize; i++) {
                final ItemTouchHelper.RecoverAnimation anim = recoverAnimationList.get(i);
                final int count = c.save();
                onChildDrawOver(c, parent, anim.mViewHolder, anim.mX, anim.mY, anim.mActionState, false);
                c.restoreToCount(count);
            }
            if (selected != null) {
                final int count = c.save();
                onChildDrawOver(c, parent, selected, dX, dY, actionState, true);
                c.restoreToCount(count);
            }
            boolean hasRunningAnimation = false;
            for (int i = recoverAnimSize - 1; i >= 0; i--) {
                final RecoverAnimation anim = recoverAnimationList.get(i);
                if (anim.mEnded && !anim.mIsPendingCleanup) {
                    recoverAnimationList.remove(i);
                } else if (!anim.mEnded) {
                    hasRunningAnimation = true;
                }
            }
            if (hasRunningAnimation) {
                parent.invalidate();
            }
        }

        public void clearView(@NonNull RecyclerView recyclerView, @NonNull ViewHolder viewHolder) {
            ItemTouchUIUtilImpl.INSTANCE.clearView(viewHolder.itemView);
        }

        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            ItemTouchUIUtilImpl.INSTANCE.onDraw(c, recyclerView, viewHolder.itemView, dX, dY, actionState, isCurrentlyActive);
        }

        public void onChildDrawOver(@NonNull Canvas c, @NonNull RecyclerView recyclerView, ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            ItemTouchUIUtilImpl.INSTANCE.onDrawOver(c, recyclerView, viewHolder.itemView, dX, dY, actionState, isCurrentlyActive);
        }

        @SuppressWarnings("WeakerAccess")
        public long getAnimationDuration(@NonNull RecyclerView recyclerView, int animationType, float animateDx, float animateDy) {
            final RecyclerView.ItemAnimator itemAnimator = recyclerView.getItemAnimator();
            if (itemAnimator == null) {
                return animationType == ANIMATION_TYPE_DRAG ? DEFAULT_DRAG_ANIMATION_DURATION : DEFAULT_SWIPE_ANIMATION_DURATION;
            } else {
                return animationType == ANIMATION_TYPE_DRAG ? itemAnimator.getMoveDuration() : itemAnimator.getRemoveDuration();
            }
        }

        @SuppressWarnings("WeakerAccess")
        public int interpolateOutOfBoundsScroll(@NonNull RecyclerView recyclerView, int viewSize, int viewSizeOutOfBounds, int totalSize, long msSinceStartScroll) {
            final int maxScroll = getMaxDragScroll(recyclerView);
            final int absOutOfBounds = Math.abs(viewSizeOutOfBounds);
            final int direction = (int) Math.signum(viewSizeOutOfBounds);
            float outOfBoundsRatio = Math.min(1f, 1f * absOutOfBounds / viewSize);
            final int cappedScroll = (int) (direction * maxScroll * sDragViewScrollCapInterpolator.getInterpolation(outOfBoundsRatio));
            final float timeRatio;
            if (msSinceStartScroll > DRAG_SCROLL_ACCELERATION_LIMIT_TIME_MS) {
                timeRatio = 1f;
            } else {
                timeRatio = (float) msSinceStartScroll / DRAG_SCROLL_ACCELERATION_LIMIT_TIME_MS;
            }
            final int value = (int) (cappedScroll * sDragScrollInterpolator.getInterpolation(timeRatio));
            if (value == 0) {
                return viewSizeOutOfBounds > 0 ? 1 : -1;
            }
            return value;
        }
    }

    public abstract static class SimpleCallback extends Callback {
        private int mDefaultSwipeDirs;
        private int mDefaultDragDirs;

        public SimpleCallback(int dragDirs, int swipeDirs) {
            mDefaultSwipeDirs = swipeDirs;
            mDefaultDragDirs = dragDirs;
        }

        @SuppressWarnings({"WeakerAccess", "unused"})
        public void setDefaultSwipeDirs(@SuppressWarnings("unused") int defaultSwipeDirs) {
            mDefaultSwipeDirs = defaultSwipeDirs;
        }

        @SuppressWarnings({"WeakerAccess", "unused"})
        public void setDefaultDragDirs(@SuppressWarnings("unused") int defaultDragDirs) {
            mDefaultDragDirs = defaultDragDirs;
        }

        @SuppressWarnings("WeakerAccess")
        public int getSwipeDirs(@SuppressWarnings("unused") @NonNull RecyclerView recyclerView, @NonNull @SuppressWarnings("unused") ViewHolder viewHolder) {
            return mDefaultSwipeDirs;
        }

        @SuppressWarnings("WeakerAccess")
        public int getDragDirs(@SuppressWarnings("unused") @NonNull RecyclerView recyclerView, @SuppressWarnings("unused") @NonNull ViewHolder viewHolder) {
            return mDefaultDragDirs;
        }

        @Override
        public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull ViewHolder viewHolder) {
            return makeMovementFlags(getDragDirs(recyclerView, viewHolder), getSwipeDirs(recyclerView, viewHolder));
        }
    }

    private class ItemTouchHelperGestureListener extends GestureDetector.SimpleOnGestureListener {
        private boolean mShouldReactToLongPress = true;

        ItemTouchHelperGestureListener() {
        }

        void doNotReactToLongPress() {
            mShouldReactToLongPress = false;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            if (!mShouldReactToLongPress) {
                return;
            }
            View child = findChildView(e);
            if (child != null) {
                ViewHolder vh = mRecyclerView.getChildViewHolder(child);
                if (vh != null) {
                    if (!mCallback.hasDragFlag(mRecyclerView, vh)) {
                        return;
                    }
                    int pointerId = e.getPointerId(0);
                    if (pointerId == mActivePointerId) {
                        final int index = e.findPointerIndex(mActivePointerId);
                        final float x = e.getX(index);
                        final float y = e.getY(index);
                        mInitialTouchX = x;
                        mInitialTouchY = y;
                        mDx = mDy = 0f;
                        if (DEBUG) {
                            Log.d(TAG, "onlong press: x:" + mInitialTouchX + ",y:" + mInitialTouchY);
                        }
                        if (mCallback.isLongPressDragEnabled()) {
                            select(vh, ACTION_STATE_DRAG);
                        }
                    }
                }
            }
        }
    }

    @VisibleForTesting
    static class RecoverAnimation implements Animator.AnimatorListener {
        final float mStartDx;
        final float mStartDy;
        final float mTargetX;
        final float mTargetY;
        final ViewHolder mViewHolder;
        final int mActionState;
        @VisibleForTesting
        final ValueAnimator mValueAnimator;
        final int mAnimationType;
        boolean mIsPendingCleanup;
        float mX;
        float mY;
        boolean mOverridden = false;
        boolean mEnded = false;
        private float mFraction;

        RecoverAnimation(ViewHolder viewHolder, int animationType, int actionState, float startDx, float startDy, float targetX, float targetY) {
            mActionState = actionState;
            mAnimationType = animationType;
            mViewHolder = viewHolder;
            mStartDx = startDx;
            mStartDy = startDy;
            mTargetX = targetX;
            mTargetY = targetY;
            mValueAnimator = ValueAnimator.ofFloat(0f, 1f);
            mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    setFraction(animation.getAnimatedFraction());
                }
            });
            mValueAnimator.setTarget(viewHolder.itemView);
            mValueAnimator.addListener(this);
            setFraction(0f);
        }

        public void setDuration(long duration) {
            mValueAnimator.setDuration(duration);
        }

        public void start() {
            mViewHolder.setIsRecyclable(false);
            mValueAnimator.start();
        }

        public void cancel() {
            mValueAnimator.cancel();
        }

        public void setFraction(float fraction) {
            mFraction = fraction;
        }

        public void update() {
            if (mStartDx == mTargetX) {
                mX = mViewHolder.itemView.getTranslationX();
            } else {
                mX = mStartDx + mFraction * (mTargetX - mStartDx);
            }
            if (mStartDy == mTargetY) {
                mY = mViewHolder.itemView.getTranslationY();
            } else {
                mY = mStartDy + mFraction * (mTargetY - mStartDy);
            }
        }

        @Override
        public void onAnimationStart(Animator animation) {
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            if (!mEnded) {
                mViewHolder.setIsRecyclable(true);
            }
            mEnded = true;
        }

        @Override
        public void onAnimationCancel(Animator animation) {
            setFraction(1f);
        }

        @Override
        public void onAnimationRepeat(Animator animation) {
        }
    }
}
