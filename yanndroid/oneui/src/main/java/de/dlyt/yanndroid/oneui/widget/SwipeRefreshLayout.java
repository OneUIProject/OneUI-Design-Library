package de.dlyt.yanndroid.oneui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;
import android.view.animation.Transformation;
import android.widget.AbsListView;
import android.widget.ListView;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.core.content.ContextCompat;
import androidx.core.view.NestedScrollingChild;
import androidx.core.view.NestedScrollingChild2;
import androidx.core.view.NestedScrollingChild3;
import androidx.core.view.NestedScrollingChildHelper;
import androidx.core.view.NestedScrollingParent;
import androidx.core.view.NestedScrollingParent2;
import androidx.core.view.NestedScrollingParent3;
import androidx.core.view.NestedScrollingParentHelper;
import androidx.core.view.ViewCompat;
import androidx.core.widget.ListViewCompat;
import androidx.reflect.view.SeslHapticFeedbackConstantsReflector;

import de.dlyt.yanndroid.oneui.R;
import de.dlyt.yanndroid.oneui.sesl.swiperefreshlayout.CircleImageView;
import de.dlyt.yanndroid.oneui.sesl.swiperefreshlayout.CircularProgressDrawable;
import de.dlyt.yanndroid.oneui.sesl.swiperefreshlayout.OUI4CircularProgressDrawable;

public class SwipeRefreshLayout extends ViewGroup implements NestedScrollingParent3, NestedScrollingParent2, NestedScrollingChild3, NestedScrollingChild2, NestedScrollingParent, NestedScrollingChild {
    private boolean mIsOneUI4;
    private static final int ALPHA_ANIMATION_DURATION = 300;
    private static final int ANIMATE_TO_START_DURATION = 200;
    private static final int ANIMATE_TO_TRIGGER_DURATION = 200;
    static final int CIRCLE_DIAMETER = 40;
    static final int CIRCLE_DIAMETER_LARGE = 56;
    private static final float DECELERATE_INTERPOLATION_FACTOR = 2.0f;
    public static final int DEFAULT = 1;
    private static final int DEFAULT_CIRCLE_TARGET = 64;
    private static final int OUI4_DEFAULT_CIRCLE_TARGET = 74;
    public static final int DEFAULT_SLINGSHOT_DISTANCE = -1;
    private static final float DRAG_RATE = 0.5f;
    private static final int END_SCALE_DOWN_DURATION = 300;
    private static final int INVALID_POINTER = -1;
    public static final int LARGE = 0;
    private static final int[] LAYOUT_ATTRS = {16842766};
    private static final String LOG_TAG = "SwipeRefreshLayout";
    private static final int MAX_ALPHA = 255;
    private static final float MAX_PROGRESS_ANGLE = 0.82f;
    private static final int SCALE_DOWN_DURATION = 150;
    private static final Interpolator SINE_IN_80 = new PathInterpolator(0.8f, 0.0f, 0.83f, 0.83f);
    private static final Interpolator SINE_OUT_60 = new PathInterpolator(0.17f, 0.17f, 0.4f, 1.0f);
    private static final int STARTING_PROGRESS_ALPHA = 76;
    private static final int OUI4_STARTING_PROGRESS_ALPHA = 51;
    private static final boolean SUPPORT_TOUCH_FEEDBACK = (Build.VERSION.SDK_INT >= 28);
    private boolean mActionDown;
    private int mActivePointerId = -1;
    private Animation mAlphaMaxAnimation;
    private Animation mAlphaStartAnimation;
    private OnChildScrollUpCallback mChildScrollUpCallback;
    private int mCircleDiameter;
    CircleImageView mCircleView;
    private int mCircleViewIndex = -1;
    int mCurrentTargetOffsetTop;
    int mCustomSlingshotDistance;
    private final DecelerateInterpolator mDecelerateInterpolator;
    private boolean mEnableLegacyRequestDisallowInterceptTouch;
    protected int mFrom;
    private float mInitialDownY;
    private float mInitialMotionY;
    private boolean mIsBeingDragged;
    private boolean mIsHaptic = false;
    OnRefreshListener mListener;
    private int mMediumAnimationDuration;
    private boolean mNestedScrollInProgress;
    private final NestedScrollingChildHelper mNestedScrollingChildHelper;
    private final NestedScrollingParentHelper mNestedScrollingParentHelper;
    private final int[] mNestedScrollingV2ConsumedCompat = new int[2];
    boolean mNotify;
    protected int mOriginalOffsetTop;
    private final int[] mParentOffsetInWindow = new int[2];
    private final int[] mParentScrollConsumed = new int[2];
    CircularProgressDrawable mProgress;
    OUI4CircularProgressDrawable mOUI4Progress;
    boolean mRefreshing = false;
    private boolean mReturningToStart;
    boolean mScale = true;
    private Animation mScaleAnimation;
    private Animation mScaleDownAnimation;
    private Animation mScaleDownToStartAnimation;
    int mSpinnerOffsetEnd;
    float mStartingOpacity;
    float mStartingScale;
    private View mTarget;
    private float mTotalDragDistance = -1.0f;
    private float mTotalUnconsumed;
    private int mTouchSlop;
    boolean mUsingCustomStart;

    private Animation.AnimationListener mRefreshListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            if (mRefreshing) {
                if (mIsOneUI4) {
                    mOUI4Progress.setAlpha(MAX_ALPHA);
                    mOUI4Progress.start();
                } else {
                    mProgress.setAlpha(MAX_ALPHA);
                    mProgress.start();
                }

                if (mNotify) {
                    if (mListener != null) {
                        mListener.onRefresh();
                    }
                }
                mCurrentTargetOffsetTop = mCircleView.getTop();
            } else {
                reset();
            }
        }
    };

    void reset() {
        mCircleView.clearAnimation();
        if (mIsOneUI4) {
            mOUI4Progress.stop();
            mCircleView.setVisibility(View.GONE);
            mCircleView.setAlpha(1.0f);
        } else {
            mProgress.stop();
            mCircleView.setVisibility(View.GONE);
        }
        setColorViewAlpha(MAX_ALPHA);
        if (mScale) {
            setAnimationProgress(0);
        } else {
            setTargetOffsetTopAndBottom(mOriginalOffsetTop - mCurrentTargetOffsetTop);
        }
        mCurrentTargetOffsetTop = mCircleView.getTop();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (!enabled) {
            reset();
        }
    }

    static class SavedState extends View.BaseSavedState {
        final boolean mRefreshing;

        SavedState(Parcelable superState, boolean refreshing) {
            super(superState);
            this.mRefreshing = refreshing;
        }

        SavedState(Parcel in) {
            super(in);
            mRefreshing = in.readByte() != 0;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeByte(mRefreshing ? (byte) 1 : (byte) 0);
        }

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
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        return new SavedState(superState, mRefreshing);
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        setRefreshing(savedState.mRefreshing);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        reset();
    }

    private void setColorViewAlpha(int targetAlpha) {
        mCircleView.getBackground().setAlpha(targetAlpha);
        if (mIsOneUI4) {
            mOUI4Progress.setAlpha(targetAlpha);
        } else {
            mProgress.setAlpha(targetAlpha);
        }
    }

    public void setProgressViewOffset(boolean scale, int start, int end) {
        mScale = scale;
        mOriginalOffsetTop = start;
        mSpinnerOffsetEnd = end;
        mUsingCustomStart = true;
        reset();
        mRefreshing = false;
    }

    public int getProgressViewStartOffset() {
        return mOriginalOffsetTop;
    }

    public int getProgressViewEndOffset() {
        return mSpinnerOffsetEnd;
    }

    public void setProgressViewEndTarget(boolean scale, int end) {
        mSpinnerOffsetEnd = end;
        mScale = scale;
        mCircleView.invalidate();
    }

    public void setSlingshotDistance(@Px int slingshotDistance) {
        mCustomSlingshotDistance = slingshotDistance;
    }

    public void setSize(int size) {
        if (size != CircularProgressDrawable.LARGE && size != CircularProgressDrawable.DEFAULT) {
            return;
        }
        final DisplayMetrics metrics = getResources().getDisplayMetrics();
        if (size == CircularProgressDrawable.LARGE) {
            mCircleDiameter = (int) (CIRCLE_DIAMETER_LARGE * metrics.density);
        } else {
            mCircleDiameter = (int) (CIRCLE_DIAMETER * metrics.density);
        }
        mCircleView.setImageDrawable(null);
        if (mIsOneUI4) {
            mOUI4Progress.setStyle(size);
        } else {
            mProgress.setStyle(size);
        }
        mCircleView.setImageDrawable(mOUI4Progress);
    }

    public SwipeRefreshLayout(@NonNull Context context) {
        this(context, null);
    }

    public SwipeRefreshLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        mIsOneUI4 = context.getTheme().obtainStyledAttributes(new int[]{R.attr.isOneUI4}).getBoolean(0, false);

        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

        mMediumAnimationDuration = getResources().getInteger(android.R.integer.config_mediumAnimTime);

        setWillNotDraw(false);
        mDecelerateInterpolator = new DecelerateInterpolator(DECELERATE_INTERPOLATION_FACTOR);

        final DisplayMetrics metrics = getResources().getDisplayMetrics();
        mCircleDiameter = (int) (CIRCLE_DIAMETER * metrics.density);

        createProgressView();
        setChildrenDrawingOrderEnabled(true);
        if (mIsOneUI4) {
            mSpinnerOffsetEnd = (int) (OUI4_DEFAULT_CIRCLE_TARGET * metrics.density);
            mTotalDragDistance = (float) (mSpinnerOffsetEnd + ((int) (metrics.density * 26.0f)));
        } else {
            mSpinnerOffsetEnd = (int) (DEFAULT_CIRCLE_TARGET * metrics.density);
            mTotalDragDistance = (float) (mSpinnerOffsetEnd * 2);
        }

        mNestedScrollingParentHelper = new NestedScrollingParentHelper(this);

        mNestedScrollingChildHelper = new NestedScrollingChildHelper(this);
        setNestedScrollingEnabled(true);

        mOriginalOffsetTop = mCurrentTargetOffsetTop = -mCircleDiameter;
        moveToStart(1.0f);

        final TypedArray a = context.obtainStyledAttributes(attrs, LAYOUT_ATTRS);
        setEnabled(a.getBoolean(0, true));
        a.recycle();
    }

    @Override
    protected int getChildDrawingOrder(int childCount, int i) {
        if (mCircleViewIndex < 0) {
            return i;
        } else if (i == childCount - 1) {
            return mCircleViewIndex;
        } else if (i >= mCircleViewIndex) {
            return i + 1;
        } else {
            return i;
        }
    }

    private void createProgressView() {
        mCircleView = new CircleImageView(getContext());
        if (mIsOneUI4) {
            mOUI4Progress = new OUI4CircularProgressDrawable(getContext());
            mOUI4Progress.setStyle(OUI4CircularProgressDrawable.DEFAULT);
            mCircleView.setImageDrawable(mOUI4Progress);
        } else {
            mProgress = new CircularProgressDrawable(getContext());
            mProgress.setStyle(CircularProgressDrawable.DEFAULT);
            mCircleView.setImageDrawable(mProgress);
        }
        mCircleView.setVisibility(View.GONE);
        addView(mCircleView);
    }

    public void setOnRefreshListener(@Nullable OnRefreshListener listener) {
        mListener = listener;
    }

    public void setRefreshing(boolean refreshing) {
        if (refreshing && mRefreshing != refreshing) {
            mRefreshing = refreshing;
            int endTarget = 0;
            if (!mUsingCustomStart) {
                endTarget = mSpinnerOffsetEnd + mOriginalOffsetTop;
            } else {
                endTarget = mSpinnerOffsetEnd;
            }
            setTargetOffsetTopAndBottom(endTarget - mCurrentTargetOffsetTop);
            mNotify = false;
            startScaleUpAnimation(mRefreshListener);
        } else {
            setRefreshing(refreshing, false);
        }
    }

    private void startScaleUpAnimation(AnimationListener listener) {
        mCircleView.setVisibility(View.VISIBLE);
        if (mIsOneUI4) {
            mOUI4Progress.setAlpha(MAX_ALPHA);
        }
        mScaleAnimation = new Animation() {
            @Override
            public void applyTransformation(float interpolatedTime, Transformation t) {
                setAnimationProgress(interpolatedTime);
            }
        };
        mScaleAnimation.setDuration(mMediumAnimationDuration);
        if (listener != null) {
            mCircleView.setAnimationListener(listener);
        }
        mCircleView.clearAnimation();
        mCircleView.startAnimation(mScaleAnimation);
    }

    void setAnimationProgress(float progress) {
        mCircleView.setScaleX(progress);
        mCircleView.setScaleY(progress);
    }

    private void setRefreshing(boolean refreshing, final boolean notify) {
        if (mRefreshing != refreshing) {
            mNotify = notify;
            ensureTarget();
            mRefreshing = refreshing;
            if (mRefreshing) {
                startRotateAnimation();
            } else {
                startScaleDownAnimation(mRefreshListener);
            }
        }
    }

    private void startRotateAnimation() {
        if (mRefreshing) {
            if (mIsOneUI4) {
                mOUI4Progress.setAlpha(MAX_ALPHA);
                mOUI4Progress.start();
            } else {
                mProgress.setAlpha(MAX_ALPHA);
                mProgress.start();
            }
            if (mNotify) {
                if (mListener != null) {
                    mListener.onRefresh();
                }
            }
            mCurrentTargetOffsetTop = mCircleView.getTop();
        } else {
            reset();
        }
    }

    void startScaleDownAnimation(Animation.AnimationListener listener) {
        mStartingScale = mCircleView.getScaleX();
        if (mIsOneUI4) {
            mStartingOpacity = mCircleView.getAlpha();
            mScaleDownAnimation = new Animation() {
                @Override
                public void applyTransformation(float interpolatedTime, Transformation t) {
                    mCircleView.setAlpha(mStartingOpacity + ((-mStartingOpacity) * interpolatedTime));
                    mCircleView.getBackground().setAlpha((int) ((mStartingOpacity + ((-mStartingOpacity) * interpolatedTime)) * 255.0f));
                    setAnimationProgress(((mStartingScale + ((-mStartingScale) * interpolatedTime)) * 0.8f) + 0.2f);
                    if (interpolatedTime == 1.0f) {
                        mOUI4Progress.stop();
                    }
                }
            };
            mScaleDownAnimation.setDuration(END_SCALE_DOWN_DURATION);
            mScaleDownAnimation.setInterpolator(SINE_OUT_60);
        } else {
            mScaleDownAnimation = new Animation() {
                @Override
                public void applyTransformation(float interpolatedTime, Transformation t) {
                    setAnimationProgress((mStartingScale + ((-mStartingScale) * interpolatedTime)));
                }
            };
            mScaleDownAnimation.setDuration(END_SCALE_DOWN_DURATION);
            mScaleDownAnimation.setInterpolator(SINE_IN_80);
        }
        mCircleView.setAnimationListener(listener);
        mCircleView.clearAnimation();
        mCircleView.startAnimation(mScaleDownAnimation);
    }

    private void startProgressAlphaStartAnimation() {
        if (mIsOneUI4) {
            mAlphaStartAnimation = startAlphaAnimation(mOUI4Progress.getAlpha(), OUI4_STARTING_PROGRESS_ALPHA);
        } else {
            mAlphaStartAnimation = startAlphaAnimation(mProgress.getAlpha(), STARTING_PROGRESS_ALPHA);
        }
    }

    private void startProgressAlphaMaxAnimation() {
        if (mIsOneUI4) {
            mAlphaMaxAnimation = startAlphaAnimation(mOUI4Progress.getAlpha(), MAX_ALPHA);
        } else {
            mAlphaMaxAnimation = startAlphaAnimation(mProgress.getAlpha(), MAX_ALPHA);
        }
    }

    private Animation startAlphaAnimation(final int startingAlpha, final int endingAlpha) {
        Animation alpha;
        if (mIsOneUI4) {
            alpha = new Animation() {
                @Override
                public void applyTransformation(float interpolatedTime, Transformation t) {
                    mOUI4Progress.setAlpha((int) (startingAlpha + ((endingAlpha - startingAlpha) * interpolatedTime)));
                }
            };
        } else {
            alpha = new Animation() {
                @Override
                public void applyTransformation(float interpolatedTime, Transformation t) {
                    mProgress.setAlpha((int) (startingAlpha + ((endingAlpha - startingAlpha) * interpolatedTime)));
                }
            };
        }
        alpha.setDuration(ALPHA_ANIMATION_DURATION);
        mCircleView.setAnimationListener(null);
        mCircleView.clearAnimation();
        mCircleView.startAnimation(alpha);
        return alpha;
    }

    @Deprecated
    public void setProgressBackgroundColor(int colorRes) {
        setProgressBackgroundColorSchemeResource(colorRes);
    }

    public void setProgressBackgroundColorSchemeResource(@ColorRes int colorRes) {
        setProgressBackgroundColorSchemeColor(ContextCompat.getColor(getContext(), colorRes));
    }

    public void setProgressBackgroundColorSchemeColor(@ColorInt int color) {
        mCircleView.setBackgroundColor(color);
    }

    @Deprecated
    public void setColorScheme(@ColorRes int... colors) {
        setColorSchemeResources(colors);
    }

    public void setColorSchemeResources(@ColorRes int... colorResIds) {
        final Context context = getContext();
        int[] colorRes = new int[colorResIds.length];
        for (int i = 0; i < colorResIds.length; i++) {
            colorRes[i] = ContextCompat.getColor(context, colorResIds[i]);
        }
        setColorSchemeColors(colorRes);
    }

    public void setColorSchemeColors(@ColorInt int... colors) {
        ensureTarget();
        if (mIsOneUI4) {
            mOUI4Progress.setColorSchemeColors(colors);
        } else {
            mProgress.setColorSchemeColors(colors);
        }
    }

    public boolean isRefreshing() {
        return mRefreshing;
    }

    private void ensureTarget() {
        if (mTarget == null) {
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                if (!child.equals(mCircleView)) {
                    mTarget = child;
                    break;
                }
            }
        }
    }

    public void setDistanceToTriggerSync(int distance) {
        mTotalDragDistance = distance;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        final int width = getMeasuredWidth();
        final int height = getMeasuredHeight();
        if (getChildCount() == 0) {
            return;
        }
        if (mTarget == null) {
            ensureTarget();
        }
        if (mTarget == null) {
            return;
        }
        final View child = mTarget;
        final int childLeft = getPaddingLeft();
        final int childTop = getPaddingTop();
        final int childWidth = width - getPaddingLeft() - getPaddingRight();
        final int childHeight = height - getPaddingTop() - getPaddingBottom();
        child.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight);
        int circleWidth = mCircleView.getMeasuredWidth();
        int circleHeight = mCircleView.getMeasuredHeight();
        mCircleView.layout((width / 2 - circleWidth / 2), mCurrentTargetOffsetTop, (width / 2 + circleWidth / 2), mCurrentTargetOffsetTop + circleHeight);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mTarget == null) {
            ensureTarget();
        }
        if (mTarget == null) {
            return;
        }
        mTarget.measure(MeasureSpec.makeMeasureSpec(getMeasuredWidth() - getPaddingLeft() - getPaddingRight(), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(getMeasuredHeight() - getPaddingTop() - getPaddingBottom(), MeasureSpec.EXACTLY));
        mCircleView.measure(MeasureSpec.makeMeasureSpec(mCircleDiameter, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(mCircleDiameter, MeasureSpec.EXACTLY));
        mCircleViewIndex = -1;
        for (int index = 0; index < getChildCount(); index++) {
            if (getChildAt(index) == mCircleView) {
                mCircleViewIndex = index;
                break;
            }
        }
    }

    public int getProgressCircleDiameter() {
        return mCircleDiameter;
    }

    public boolean canChildScrollUp() {
        if (mChildScrollUpCallback != null) {
            return mChildScrollUpCallback.canChildScrollUp(this, mTarget);
        }
        if (mTarget instanceof ListView) {
            return ListViewCompat.canScrollList((ListView) mTarget, -1);
        }
        return mTarget.canScrollVertically(-1);
    }

    public void setOnChildScrollUpCallback(@Nullable OnChildScrollUpCallback callback) {
        mChildScrollUpCallback = callback;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        ensureTarget();

        final int action = ev.getActionMasked();
        int pointerIndex;

        if (mReturningToStart && action == MotionEvent.ACTION_DOWN) {
            mReturningToStart = false;
        }

        if (mActionDown && (action == MotionEvent.ACTION_DOWN || (action == MotionEvent.ACTION_MOVE && canChildScrollUp()))) {
            Log.d(LOG_TAG, "onInterceptTouchEvent() refresh cancelled by list scrolling or touch release, mActionDown = false");
            mActionDown = false;
        }

        if (!isEnabled() || mReturningToStart || canChildScrollUp() || mRefreshing || mNestedScrollInProgress) {
            return false;
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                Log.d(LOG_TAG, "onInterceptTouchEvent() ACTION_DOWN!");
                mActionDown = true;
                setTargetOffsetTopAndBottom(mOriginalOffsetTop - mCircleView.getTop());
                mActivePointerId = ev.getPointerId(0);
                mIsBeingDragged = false;

                pointerIndex = ev.findPointerIndex(mActivePointerId);
                if (pointerIndex < 0) {
                    return false;
                }
                mInitialDownY = ev.getY(pointerIndex);
                break;

            case MotionEvent.ACTION_MOVE:
                if (mActivePointerId == INVALID_POINTER) {
                    Log.e(LOG_TAG, "Got ACTION_MOVE event but don't have an active pointer id.");
                    return false;
                }

                pointerIndex = ev.findPointerIndex(mActivePointerId);
                if (pointerIndex < 0) {
                    return false;
                }
                final float y = ev.getY(pointerIndex);
                if (mActionDown) {
                    startDragging(y);
                } else {
                    mIsBeingDragged = false;
                    return false;
                }
                break;

            case MotionEvent.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mIsBeingDragged = false;
                mActivePointerId = INVALID_POINTER;
                Log.d(LOG_TAG, "onInterceptTouchEvent() ACTION_UP_CANCEL!");
                mActionDown = false;
                break;
        }

        return mIsBeingDragged;
    }

    @Deprecated
    public void setLegacyRequestDisallowInterceptTouchEventEnabled(boolean enabled) {
        mEnableLegacyRequestDisallowInterceptTouch = enabled;
    }

    @Override
    public void requestDisallowInterceptTouchEvent(boolean b) {
        if ((Build.VERSION.SDK_INT >= 21 || !(mTarget instanceof AbsListView)) && (mTarget == null || ViewCompat.isNestedScrollingEnabled(mTarget))) {
            super.requestDisallowInterceptTouchEvent(b);
        } else if (!mEnableLegacyRequestDisallowInterceptTouch && getParent() != null) {
            getParent().requestDisallowInterceptTouchEvent(b);
        }
    }

    // NestedScrollingParent 3

    @Override
    public void onNestedScroll(@NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, @ViewCompat.NestedScrollType int type, @NonNull int[] consumed) {
        if (type != ViewCompat.TYPE_TOUCH) {
            return;
        }

        int consumedBeforeParents = consumed[1];
        dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, mParentOffsetInWindow, type, consumed);
        int unconsumedAfterParents = dyUnconsumed - (consumed[1] - consumedBeforeParents);

        int remainingDistanceToScroll;
        if (unconsumedAfterParents == 0) {
            remainingDistanceToScroll = dyUnconsumed + mParentOffsetInWindow[1];
        } else {
            remainingDistanceToScroll = unconsumedAfterParents;
        }

        if (remainingDistanceToScroll < 0 && !canChildScrollUp() && mActionDown) {
            mTotalUnconsumed += Math.abs(remainingDistanceToScroll);
            moveSpinner(mTotalUnconsumed);

            consumed[1] += unconsumedAfterParents;
        }
    }

    // NestedScrollingParent 2

    @Override
    public boolean onStartNestedScroll(View child, View target, int axes, int type) {
        if (type == ViewCompat.TYPE_TOUCH) {
            return onStartNestedScroll(child, target, axes);
        } else {
            return false;
        }
    }

    @Override
    public void onNestedScrollAccepted(View child, View target, int axes, int type) {
        if (type == ViewCompat.TYPE_TOUCH) {
            onNestedScrollAccepted(child, target, axes);
        }
    }

    @Override
    public void onStopNestedScroll(View target, int type) {
        if (type == ViewCompat.TYPE_TOUCH) {
            onStopNestedScroll(target);
        }
    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type) {
        onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type, mNestedScrollingV2ConsumedCompat);
    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed, int type) {
        if (type == ViewCompat.TYPE_TOUCH) {
            onNestedPreScroll(target, dx, dy, consumed);
        }
    }

    // NestedScrollingParent 1

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return isEnabled() && !mReturningToStart && !mRefreshing && (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
    }

    @Override
    public void onNestedScrollAccepted(View child, View target, int axes) {
        mNestedScrollingParentHelper.onNestedScrollAccepted(child, target, axes);
        startNestedScroll(axes & ViewCompat.SCROLL_AXIS_VERTICAL);
        mTotalUnconsumed = 0;
        mNestedScrollInProgress = true;
        if (!canChildScrollUp()) {
            mActionDown = true;
        }
    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        if (dy > 0 && mTotalUnconsumed > 0 && mActionDown) {
            if (dy > mTotalUnconsumed) {
                consumed[1] = (int) mTotalUnconsumed;
                mTotalUnconsumed = 0;
            } else {
                mTotalUnconsumed -= dy;
                consumed[1] = dy;
            }
            moveSpinner(mTotalUnconsumed);
        }

        if (mUsingCustomStart && dy > 0 && mTotalUnconsumed == 0 && Math.abs(dy - consumed[1]) > 0) {
            mCircleView.setVisibility(View.GONE);
        }

        final int[] parentConsumed = mParentScrollConsumed;
        if (dispatchNestedPreScroll(dx - consumed[0], dy - consumed[1], parentConsumed, null)) {
            consumed[0] += parentConsumed[0];
            consumed[1] += parentConsumed[1];
        }
    }

    @Override
    public int getNestedScrollAxes() {
        return mNestedScrollingParentHelper.getNestedScrollAxes();
    }

    @Override
    public void onStopNestedScroll(View target) {
        mNestedScrollingParentHelper.onStopNestedScroll(target);
        mNestedScrollInProgress = false;
        mActionDown = false;
        if (mTotalUnconsumed > 0) {
            finishSpinner(mTotalUnconsumed);
            mTotalUnconsumed = 0;
        }
        stopNestedScroll();
    }

    @Override
    public void onNestedScroll(final View target, final int dxConsumed, final int dyConsumed, final int dxUnconsumed, final int dyUnconsumed) {
        onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, ViewCompat.TYPE_TOUCH, mNestedScrollingV2ConsumedCompat);
    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        return dispatchNestedPreFling(velocityX, velocityY);
    }

    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        return dispatchNestedFling(velocityX, velocityY, consumed);
    }

    // NestedScrollingChild 3

    @Override
    public void dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, @Nullable int[] offsetInWindow, @ViewCompat.NestedScrollType int type, @NonNull int[] consumed) {
        if (type == ViewCompat.TYPE_TOUCH) {
            mNestedScrollingChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow, type, consumed);
        }
    }

    // NestedScrollingChild 2

    @Override
    public boolean startNestedScroll(int axes, int type) {
        return type == ViewCompat.TYPE_TOUCH && startNestedScroll(axes);
    }

    @Override
    public void stopNestedScroll(int type) {
        if (type == ViewCompat.TYPE_TOUCH) {
            stopNestedScroll();
        }
    }

    @Override
    public boolean hasNestedScrollingParent(int type) {
        return type == ViewCompat.TYPE_TOUCH && hasNestedScrollingParent();
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int[] offsetInWindow, int type) {
        return type == ViewCompat.TYPE_TOUCH && mNestedScrollingChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow, type);
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow, int type) {
        return type == ViewCompat.TYPE_TOUCH && dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow);
    }

    // NestedScrollingChild 1

    @Override
    public void setNestedScrollingEnabled(boolean enabled) {
        mNestedScrollingChildHelper.setNestedScrollingEnabled(enabled);
    }

    @Override
    public boolean isNestedScrollingEnabled() {
        return mNestedScrollingChildHelper.isNestedScrollingEnabled();
    }

    @Override
    public boolean startNestedScroll(int axes) {
        return mNestedScrollingChildHelper.startNestedScroll(axes);
    }

    @Override
    public void stopNestedScroll() {
        mNestedScrollingChildHelper.stopNestedScroll();
    }

    @Override
    public boolean hasNestedScrollingParent() {
        return mNestedScrollingChildHelper.hasNestedScrollingParent();
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int[] offsetInWindow) {
        return mNestedScrollingChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow) {
        return mNestedScrollingChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        return mNestedScrollingChildHelper.dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        return mNestedScrollingChildHelper.dispatchNestedPreFling(velocityX, velocityY);
    }

    private boolean isAnimationRunning(Animation animation) {
        return animation != null && animation.hasStarted() && !animation.hasEnded();
    }

    private void moveSpinner(float overscrollTop) {
        if (!mIsOneUI4) {
            mProgress.setArrowEnabled(true);
        }

        float min = Math.min(1.0f, Math.abs(overscrollTop / mTotalDragDistance));
        Math.max(((double) min) - 0.4d, 0.0d);
        float extraOS = Math.abs(overscrollTop) - mTotalDragDistance;

        if (mCustomSlingshotDistance <= 0) {
            if (mUsingCustomStart) {
                mCustomSlingshotDistance = mSpinnerOffsetEnd - mOriginalOffsetTop;
            } else {
                mCustomSlingshotDistance = mSpinnerOffsetEnd;
            }
        }

        Math.pow((double) (Math.max(0.0f, Math.min(extraOS, 2.0f * mCustomSlingshotDistance) / mCustomSlingshotDistance) / 4.0f), 2.0d);

        if (mCircleView.getVisibility() != View.VISIBLE) {
            mCircleView.setVisibility(View.VISIBLE);
        }
        if (!mScale) {
            mCircleView.setScaleX(1f);
            mCircleView.setScaleY(1f);
        }
        if (mScale) {
            if (mIsOneUI4) {
                setAnimationProgress(Math.min(1.0f, ((0.8f * overscrollTop) / (mTotalDragDistance / 4.0f)) + 0.2f));
                mOUI4Progress.setAlpha((int) (Math.min(1.0f, overscrollTop / (mTotalDragDistance / 4.0f)) * 255.0f));
                mCircleView.getBackground().setAlpha((int) (Math.min(1.0f, overscrollTop / (mTotalDragDistance / 4.0f)) * 255.0f));
            } else {
                setAnimationProgress(Math.min(1.0f, overscrollTop / mTotalDragDistance));
            }
        }
        if (overscrollTop < mTotalDragDistance) {
            mIsHaptic = false;
        } else {
            if (mIsOneUI4) {
                if (SUPPORT_TOUCH_FEEDBACK && !mIsHaptic) {
                    performHapticFeedback(SeslHapticFeedbackConstantsReflector.semGetVibrationIndex(108));
                }
                mIsHaptic = true;
                if (mOUI4Progress.getAlpha() < MAX_ALPHA && !isAnimationRunning(mAlphaMaxAnimation)) {
                    startProgressAlphaMaxAnimation();
                }
            } else {
                if (mProgress.getAlpha() < MAX_ALPHA && !isAnimationRunning(mAlphaMaxAnimation)) {
                    startProgressAlphaMaxAnimation();
                }
            }
        }

        if (mIsOneUI4) {
            if (overscrollTop - (mTotalDragDistance / 4.0f) > 0.0f) {
                mOUI4Progress.setScale((overscrollTop - (mTotalDragDistance / 4.0f)) / ((mTotalDragDistance * 3.0f) / 4.0f));
            } else {
                mOUI4Progress.setScale(0.0f);
            }
        } else {
            mProgress.setStartEndTrim(-0.25f, Math.min((float) MAX_PROGRESS_ANGLE, min * MAX_PROGRESS_ANGLE) - 0.25f);
            mProgress.setArrowScale(Math.min(1.0f, min));
            mProgress.setAlpha((int) (255.0f * min));
            mProgress.setProgressRotation(min * 1.75f);
        }
        setTargetOffsetTopAndBottom(mSpinnerOffsetEnd - mCurrentTargetOffsetTop);
    }

    private void finishSpinner(float overscrollTop) {
        if (overscrollTop > mTotalDragDistance) {
            setRefreshing(true, true);
        } else {
            mRefreshing = false;
            startScaleDownAnimation(null);
            if (mIsOneUI4) {
                reset();
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        final int action = ev.getActionMasked();
        int pointerIndex = -1;

        if (mReturningToStart && action == MotionEvent.ACTION_DOWN) {
            mReturningToStart = false;
        }

        if (!isEnabled() || mReturningToStart || canChildScrollUp() || mRefreshing || mNestedScrollInProgress) {
            return false;
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mActivePointerId = ev.getPointerId(0);
                mIsBeingDragged = false;
                break;

            case MotionEvent.ACTION_MOVE: {
                pointerIndex = ev.findPointerIndex(mActivePointerId);
                if (pointerIndex < 0) {
                    Log.e(LOG_TAG, "Got ACTION_MOVE event but have an invalid active pointer id.");
                    return false;
                }
                final float y = ev.getY(pointerIndex);
                if (mActionDown) {
                    startDragging(y);
                    if (mIsBeingDragged) {
                        final float overscrollTop = (y - mInitialMotionY) * DRAG_RATE;
                        if (overscrollTop > 0) {
                            getParent().requestDisallowInterceptTouchEvent(true);
                            moveSpinner(overscrollTop);
                        } else {
                            if (mIsOneUI4) {
                                mOUI4Progress.setAlpha(0);
                                mCircleView.getBackground().setAlpha(0);
                            }
                            return false;
                        }
                    }
                } else {
                    mIsBeingDragged = false;
                    return false;
                }

                break;
            }
            case MotionEvent.ACTION_POINTER_DOWN: {
                pointerIndex = ev.getActionIndex();
                if (pointerIndex < 0) {
                    Log.e(LOG_TAG, "Got ACTION_POINTER_DOWN event but have an invalid action index.");
                    return false;
                }
                mActivePointerId = ev.getPointerId(pointerIndex);
                break;
            }

            case MotionEvent.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;

            case MotionEvent.ACTION_UP: {
                pointerIndex = ev.findPointerIndex(mActivePointerId);
                Log.d(LOG_TAG, "onTouchEvent() ACTION_UP!");
                mActionDown = false;
                if (pointerIndex < 0) {
                    Log.e(LOG_TAG, "Got ACTION_UP event but don't have an active pointer id.");
                    return false;
                }
                if (mIsBeingDragged) {
                    final float y = ev.getY(pointerIndex);
                    final float overscrollTop = (y - mInitialMotionY) * DRAG_RATE;
                    mIsBeingDragged = false;
                    finishSpinner(overscrollTop);
                }
                mActivePointerId = INVALID_POINTER;
                return false;
            }
            case MotionEvent.ACTION_CANCEL:
                Log.d(LOG_TAG, "onTouchEvent() ACTION_CANCEL XXXXXXX");
                mActionDown = false;
                return false;
        }

        return true;
    }

    private void startDragging(float y) {
        final float yDiff = y - mInitialDownY;
        if (yDiff > mTouchSlop && !mIsBeingDragged) {
            mInitialMotionY = mInitialDownY + mTouchSlop;
            mIsBeingDragged = true;
            if (mIsOneUI4) {
                mCircleView.setAlpha(1.0f);
                mOUI4Progress.setAlpha(OUI4_STARTING_PROGRESS_ALPHA);
            } else {
                mProgress.setAlpha(STARTING_PROGRESS_ALPHA);
            }
        }
    }

    private void animateOffsetToCorrectPosition(int from, AnimationListener listener) {
        mFrom = from;
        mAnimateToCorrectPosition.reset();
        mAnimateToCorrectPosition.setDuration(ANIMATE_TO_TRIGGER_DURATION);
        mAnimateToCorrectPosition.setInterpolator(mDecelerateInterpolator);
        if (listener != null) {
            mCircleView.setAnimationListener(listener);
        }
        mCircleView.clearAnimation();
        mCircleView.startAnimation(mAnimateToCorrectPosition);
    }

    private void animateOffsetToStartPosition(int from, AnimationListener listener) {
        if (mScale) {
            startScaleDownReturnToStartAnimation(from, listener);
        } else {
            mFrom = from;
            mAnimateToStartPosition.reset();
            mAnimateToStartPosition.setDuration(ANIMATE_TO_START_DURATION);
            mAnimateToStartPosition.setInterpolator(mDecelerateInterpolator);
            if (listener != null) {
                mCircleView.setAnimationListener(listener);
            }
            mCircleView.clearAnimation();
            mCircleView.startAnimation(mAnimateToStartPosition);
        }
    }

    private final Animation mAnimateToCorrectPosition = new Animation() {
        @Override
        public void applyTransformation(float interpolatedTime, Transformation t) {
            int endTarget;
            if (!mUsingCustomStart) {
                endTarget = mSpinnerOffsetEnd - Math.abs(mOriginalOffsetTop);
            } else {
                endTarget = mSpinnerOffsetEnd;
            }
            int targetTop = (mFrom + (int) ((endTarget - mFrom) * interpolatedTime));
            int offset = targetTop - mCircleView.getTop();
            setTargetOffsetTopAndBottom(offset);
            if (!mIsOneUI4) {
                mProgress.setArrowScale(1.0f - interpolatedTime);
            }

        }
    };

    void moveToStart(float interpolatedTime) {
        int targetTop = (mFrom + (int) ((mOriginalOffsetTop - mFrom) * interpolatedTime));
        int offset = targetTop - mCircleView.getTop();
        setTargetOffsetTopAndBottom(offset);
    }

    private final Animation mAnimateToStartPosition = new Animation() {
        @Override
        public void applyTransformation(float interpolatedTime, Transformation t) {
            moveToStart(interpolatedTime);
        }
    };

    private void startScaleDownReturnToStartAnimation(int from, Animation.AnimationListener listener) {
        mFrom = from;
        mStartingScale = mCircleView.getScaleX();
        mScaleDownToStartAnimation = new Animation() {
            @Override
            public void applyTransformation(float interpolatedTime, Transformation t) {
                float targetScale = (mStartingScale + (-mStartingScale  * interpolatedTime));
                setAnimationProgress(targetScale);
                moveToStart(interpolatedTime);
            }
        };
        mScaleDownToStartAnimation.setDuration(SCALE_DOWN_DURATION);
        if (listener != null) {
            mCircleView.setAnimationListener(listener);
        }
        mCircleView.clearAnimation();
        mCircleView.startAnimation(mScaleDownToStartAnimation);
    }

    void setTargetOffsetTopAndBottom(int offset) {
        mCircleView.bringToFront();
        ViewCompat.offsetTopAndBottom(mCircleView, offset);
        mCurrentTargetOffsetTop = mCircleView.getTop();
    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = ev.getActionIndex();
        final int pointerId = ev.getPointerId(pointerIndex);
        if (pointerId == mActivePointerId) {
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mActivePointerId = ev.getPointerId(newPointerIndex);
        }
    }

    public void seslSetRefreshOnce(boolean once) {
        if (once) {
            if (mIsOneUI4) {
                mOUI4Progress.setOnAnimationEndCallback(new OUI4CircularProgressDrawable.OnAnimationEndCallback() {
                    @Override
                    public void OnAnimationEnd() {
                        setRefreshing(false);
                        Log.d(LOG_TAG, "OnAnimationEnd");
                    }
                });
            } else {
                mProgress.setOnAnimationEndCallback(new CircularProgressDrawable.OnAnimationEndCallback() {
                    @Override
                    public void OnAnimationEnd() {
                        setRefreshing(false);
                        Log.d(LOG_TAG, "OnAnimationEnd");
                    }
                });
            }
        } else {
            if (mIsOneUI4) {
                mOUI4Progress.setOnAnimationEndCallback(null);
            } else {
                mProgress.setOnAnimationEndCallback(null);
            }
        }
    }

    public interface OnRefreshListener {
        void onRefresh();
    }

    public interface OnChildScrollUpCallback {
        boolean canChildScrollUp(@NonNull SwipeRefreshLayout parent, @Nullable View child);
    }
}